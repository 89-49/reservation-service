package org.pgsg.reservation.domain.service;

import org.pgsg.reservation.domain.model.reservation.*;
import org.pgsg.reservation.domain.exception.*;
import org.springframework.stereotype.Component;

@Component

public class ReservationValidator {

    public void validate(BuyerInfo buyer, SellerInfo seller, ProductInfo product) {

        if (buyer == null || seller == null || product == null) {
            throw new ReservationException(ReservationErrorCode.INVALID_INPUT);
        }

        // 본인 상품 예약 금지 규칙
        if (isSamePerson(buyer, seller)) {
            throw new ReservationException(ReservationErrorCode.INVALID_STATUS);
        }

        // 검증 규칙 추가 예정
    }

    // 본인 상품 예약 금지 규칙
    private boolean isSamePerson(BuyerInfo buyer, SellerInfo seller) {
        return buyer.getBuyerId().equals(seller.getSellerId());
    }
}
