package org.pgsg.reservation.domain.service;

import org.pgsg.reservation.domain.model.reservation.*;
import org.pgsg.reservation.domain.exception.*;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Component

public class ReservationValidator {

    public void validate(BuyerInfo buyer, SellerInfo seller, ProductInfo product) {

        if (buyer == null || seller == null || product == null) {
            throw new ReservationException(ReservationErrorCode.INVALID_INPUT);
        }

        if (buyer.getBuyerId() == null || seller.getSellerId() == null) {
            throw new ReservationException(ReservationErrorCode.INVALID_INPUT);
        }

        // 본인 상품 예약 금지 규칙
        if (isSamePerson(buyer, seller)) {
            throw new ReservationException(ReservationErrorCode.INVALID_STATUS);
        }

        // 검증 규칙 추가 예정
    }

    public void validateSearchRequest(UUID userId, String role) {
        if (userId == null || role == null || role.isBlank()) {
            // 권한 정보가 부족할 때 던지는 예외
            throw new ReservationException(ReservationErrorCode.UNAUTHORIZED_ACCESS);
        }
    }

    // 본인 상품 예약 금지 규칙
    private boolean isSamePerson(BuyerInfo buyer, SellerInfo seller) {
        return buyer.getBuyerId().equals(seller.getSellerId());
    }

}
