package org.pgsg.reservation.domain.service;

import org.pgsg.reservation.domain.model.reservation.Reservation;
import org.pgsg.reservation.domain.model.reservation.BuyerInfo;
import org.pgsg.reservation.domain.model.reservation.SellerInfo;
import org.pgsg.reservation.domain.model.reservation.ProductInfo;
import org.pgsg.reservation.domain.exception.ReservationException;
import org.pgsg.reservation.domain.exception.ReservationErrorCode;
import org.springframework.stereotype.Service;

import java.util.Objects;

@Service
public class ReservationDomainService {

    /**
     * 예약 생성 로직
     * 각 VO들을 조합하여 예약 엔티티를 생성하고, 도메인 규칙을 검증
     */
    public Reservation createReservation(BuyerInfo buyer, SellerInfo seller, ProductInfo product) {

        // 기본 유효성 검사
        validateInputs(buyer, seller, product);

        // 판매자와 구매자가 동일인인지 확인
        // 본인이 등록한 타임딜 상품을 본인이 예약하는 어뷰징을 방지합니다.
        if (isSamePerson(buyer, seller)) {
            throw new ReservationException(ReservationErrorCode.INVALID_STATUS);
        }

        // 예약 엔티티 생성
        // 상태는 Reservation.create() 내부에서 PENDING으로 설정됩니다.
        return Reservation.create(buyer, seller, product);
    }

    // 유효성 검증 로직
    private void validateInputs(BuyerInfo buyer, SellerInfo seller, ProductInfo product) {
        Objects.requireNonNull(buyer, "구매자 정보가 없습니다.");
        Objects.requireNonNull(seller, "판매자 정보가 없습니다.");
        Objects.requireNonNull(product, "상품 정보가 없습니다.");
    }

    // 판매자와 구매자가 동일인인지 확인하는 로직
    private boolean isSamePerson(BuyerInfo buyer, SellerInfo seller) {
        return buyer.getBuyerId().equals(seller.getSellerId());
    }
}