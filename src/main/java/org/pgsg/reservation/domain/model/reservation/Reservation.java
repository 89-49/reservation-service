package org.pgsg.reservation.domain.model.reservation;

import jakarta.persistence.*;
import lombok.*;
import org.pgsg.common.domain.BaseEntity;
import org.pgsg.reservation.domain.exception.ReservationException;
import org.pgsg.reservation.domain.exception.ReservationErrorCode;
import org.pgsg.reservation.domain.model.reservationcandidate.ReservationCandidate;
import org.pgsg.reservation.domain.model.reservationcandidate.ReservationStatus;
import java.util.Objects;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

@Entity
@Getter
@Table(name = "p_reservations")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Reservation extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    //낙관적 락을 위한 버전 추가(추후 분산 락으로 수정예정)
    @Version
    private Long version;

    @Enumerated(EnumType.STRING)
    private ReservationStatus status;

    @Embedded
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
        validatePendingStatus();
        this.candidates.add(candidate);
    }

    public void removeCandidate(ReservationCandidate candidate) {
        validatePendingStatus();
        this.candidates.remove(candidate);
    }

    // 예약 생성
    public static Reservation create(BuyerInfo buyer, SellerInfo seller, ProductInfo product) {
        Objects.requireNonNull(buyer, "buyer must not be null");
        Objects.requireNonNull(seller, "seller must not be null");
        Objects.requireNonNull(product, "product must not be null");
        Reservation reservation = new Reservation();
        reservation.buyerInfo = buyer;
        reservation.sellerInfo = seller;
        reservation.productInfo = product;
        reservation.status = ReservationStatus.PENDING;
        return reservation;
    }

    // 예약 완료: PENDING 상태일 때만 가능
    public void complete() {
        validatePendingStatus();
        this.status = ReservationStatus.COMPLETED;
    }

    // 예약 만료: PENDING 상태일 때만 가능
    public void expire() {
        validatePendingStatus();
        this.status = ReservationStatus.EXPIRED;
    }

    // 예약 취소: 취소 사유 업데이트 포함
    public void cancel() {
        validatePendingStatus();
        this.status = ReservationStatus.CANCELLED;
    }

    // 다음 순번 구매자로 교체
    public void changeToNextBuyer(ReservationCandidate nextCandidate) {
        // 예약 자체가 변경 가능한 상태인지 확인
        validatePendingStatus();
        // 인자 유효성 검사
        if (nextCandidate == null) {
            throw new IllegalArgumentException("nextBuyer must not be null");
        }
        // 해당 후보자가 실제 이 예약의 후보 명단에 포함되어 있는지 검증
        if (!this.candidates.contains(nextCandidate)) {
            throw new ReservationException(ReservationErrorCode.INVALID_STATUS); // 혹은 "후보자가 아닙니다" 에러
        }
        // 후보자의 상태가 '대기' 중인지 확인
        if (nextCandidate.getStatus() != SelectStatus.WAITING) {
            throw new ReservationException(ReservationErrorCode.CANNOT_CHANGE_STATUS);
        }
        nextCandidate.selected();
        // 검증 완료 후 구매자 정보 교체
        this.buyerInfo = BuyerInfo.of(nextCandidate.getCandidateId(), nextCandidate.getCandidateNickname());
    }

    private void validatePendingStatus() {
        if (this.status != ReservationStatus.PENDING) {
            throw new ReservationException(ReservationErrorCode.CANNOT_CHANGE_STATUS);
        }
    }
}