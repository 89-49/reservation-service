package org.pgsg.reservation.domain.model.reservation;

import jakarta.persistence.*;
import lombok.*;
import org.pgsg.common.domain.BaseEntity;
import org.pgsg.reservation.domain.exception.ReservationException;
import org.pgsg.reservation.domain.exception.ReservationErrorCode;
import org.pgsg.reservation.domain.model.reservationcandidate.ReservationCandidate;
import org.pgsg.reservation.domain.model.reservationcandidate.ReservationCandidateStatus;

import java.time.LocalDateTime;
import java.util.*;

@Entity
@Getter
@Table(name = "p_reservations")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Reservation extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    // 낙관적 락을 위한 버전 추가(추후 분산 락으로 수정 예정)
    @Version
    private Long version;

    @Enumerated(EnumType.STRING)
    private ReservationStatus status;

    @Embedded
    @AttributeOverrides({
            @AttributeOverride(name = "buyerId", column = @Column(name = "buyer_id", nullable = true)),
            @AttributeOverride(name = "buyerName", column = @Column(name = "buyer_name", nullable = true))
    })
    private BuyerInfo buyerInfo;

    @Embedded
    private SellerInfo sellerInfo;

    @Embedded
    private ProductInfo productInfo;

    // 예약 후보자들
    @OneToMany(mappedBy = "reservation", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<ReservationCandidate> candidates = new ArrayList<>();

    public List<ReservationCandidate> getCandidates() {
        return Collections.unmodifiableList(candidates);
    }

    public void addCandidate(ReservationCandidate candidate) {
        validateStatus(ReservationStatus.AVAILABLE);
        this.candidates.add(candidate);
    }

    public void removeCandidate(ReservationCandidate candidate) {
        //validateStatus(ReservationStatus.AVAILABLE);
        this.candidates.remove(candidate);
    }

    // 예약 생성: 초기 상태 AVAILABLE
    public static Reservation create(BuyerInfo buyer, SellerInfo seller, ProductInfo product, LocalDateTime endTime) {
        Objects.requireNonNull(seller, "seller must not be null");
        Objects.requireNonNull(product, "product must not be null");

        Reservation reservation = new Reservation();
        reservation.buyerInfo = buyer;
        reservation.sellerInfo = seller;
        reservation.productInfo = product;
        reservation.status = ReservationStatus.AVAILABLE;
        reservation.createdAt = LocalDateTime.now();

        return reservation;
    }

    // 임시 예약: AVAILABLE 상태에서 결제 대기(점유) 단계로 진입
    public void markAsPending() {
        validateStatus(ReservationStatus.AVAILABLE);
        this.status = ReservationStatus.PENDING;
    }

    // 결제 완료: PENDING(임시 예약) 상태일 때만 가능
    public void markAsPaid() {
        validateStatus(ReservationStatus.PENDING);
        this.status = ReservationStatus.PAID;
    }

    // 예약 완료: PAID(결제 완료) 상태에서 채팅 수락 시
    public void complete() {
        validateStatus(ReservationStatus.PAID);
        this.status = ReservationStatus.COMPLETED;
    }

    // 최종 종료: 완료된 거래를 아카이브 상태로 변경
    public void close() {
        validateStatus(ReservationStatus.COMPLETED);
        this.status = ReservationStatus.CLOSED;
    }

    // 거래 복구: COMPLETED 상태에서 취소 발생 시 AVAILABLE로 복구
    public void rollbackToAvailable() {
        validateStatus(ReservationStatus.COMPLETED);
        this.status = ReservationStatus.AVAILABLE;
    }

    // 구매자 취소: PENDING 또는 PAID 상태에서 구매자가 취소
    public void cancelByBuyer() {
        validateStatus(ReservationStatus.PENDING, ReservationStatus.PAID);
        this.status = ReservationStatus.CANCELLED_BY_BUYER;
    }

    // 대기자가 없을 경우, 다시 누구나 신청 가능한 상태로 복구
    public void reopen() {
        // 현재 상태가 취소된 상태여야 재오픈 가능 (보안 및 로직 검증)
        validateStatus(ReservationStatus.CANCELLED_BY_BUYER);

        this.status = ReservationStatus.AVAILABLE;
        this.buyerInfo = null; // 기존 구매자 정보 제거
    }

    // 판매자 취소: PENDING 또는 PAID 상태에서 판매자가 취소(영구 종료)
    public void cancelBySeller() {
        validateStatus(ReservationStatus.PENDING, ReservationStatus.PAID);
        this.status = ReservationStatus.CANCELLED_BY_SELLER;
    }

    // 다음 순번 구매자로 교체
    public void changeToNextBuyer(ReservationCandidate nextCandidate) {
        // 현재 변경 가능한 상태인지 검증 (AVAILABLE 혹은 이전 구매자 취소 상태)
        validateStatus(ReservationStatus.AVAILABLE, ReservationStatus.CANCELLED_BY_BUYER);

        // 인자 및 후보자 유효성 검증
        if (nextCandidate == null) {
            throw new IllegalArgumentException("nextBuyer must not be null");
        }
        if (!this.candidates.contains(nextCandidate)) {
            throw new ReservationException(ReservationErrorCode.INVALID_STATUS);
        }
        if (nextCandidate.getStatus() != ReservationCandidateStatus.WAITING) {
            throw new ReservationException(ReservationErrorCode.CANNOT_CHANGE_STATUS);
        }

        // 순서 변경: 먼저 예약 상태를 PENDING으로 전환하여 selected()의 검증을 통과시킴
        this.status = ReservationStatus.PENDING;
        this.buyerInfo = BuyerInfo.of(nextCandidate.getCandidateId(), nextCandidate.getCandidateNickname());

        // 후보자 상태를 SELECTED로 변경
        nextCandidate.selected();
    }

    // 상태 검증: 여러 허용 상태 중 하나라도 만족하는지 확인
    private void validateStatus(ReservationStatus... expectedStatuses) {
        // 판매자 취소나 최종 종료 상태인 경우 절대 변경 불가
        if (this.status != null && !this.status.isMutable()) {
            throw new ReservationException(ReservationErrorCode.CANNOT_CHANGE_STATUS);
        }

        boolean isValid = Arrays.stream(expectedStatuses)
                .anyMatch(expected -> this.status == expected);

        if (!isValid) {
            throw new ReservationException(ReservationErrorCode.CANNOT_CHANGE_STATUS);
        }
    }
}