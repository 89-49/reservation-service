package org.pgsg.reservation.domain.model.reservation;

import jakarta.persistence.*;
import lombok.*;
import org.pgsg.common.domain.BaseEntity;
import org.pgsg.reservation.domain.exception.ReservationException;
import org.pgsg.reservation.domain.exception.ReservationErrorCode;
import org.pgsg.reservation.domain.model.reservationcandidate.ReservationCandidate;
import org.pgsg.reservation.domain.model.reservationcandidate.ReservationStatus;

import java.util.ArrayList;
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

    @Enumerated(EnumType.STRING)
    private ReservationStatus status;

    @Embedded
    private BuyerInfo buyerInfo;

    @Embedded
    private SellerInfo sellerInfo;

    @Embedded
    private ProductInfo productInfo;

    private String cancelInfo; // 초기값 null

    // 예약 후보자들
    @OneToMany(mappedBy = "reservation", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<ReservationCandidate> candidates = new ArrayList<>();

    // 예약 생성
    public static Reservation create(BuyerInfo buyer, SellerInfo seller, ProductInfo product) {
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
    public void cancel(String reason) {
        validatePendingStatus();
        this.status = ReservationStatus.CANCELLED;
        this.cancelInfo = reason;
    }

    // 다음 순번 구매자로 교체
    public void changeToNextBuyer(BuyerInfo nextBuyer) {
        this.buyerInfo = nextBuyer;
    }

    private void validatePendingStatus() {
        if (this.status != ReservationStatus.PENDING) {
            throw new ReservationException(ReservationErrorCode.CANNOT_CHANGE_STATUS);
        }
    }
}