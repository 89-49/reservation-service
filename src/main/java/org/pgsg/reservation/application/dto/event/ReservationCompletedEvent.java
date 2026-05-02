package org.pgsg.reservation.application.dto.event;

import org.pgsg.reservation.domain.model.reservation.Reservation;

import java.util.UUID;

public record ReservationCompletedEvent(
        UUID reservationId,
        UUID productId,
        String productName,
        Integer productPrice,    // [추가] 누락된 필드
        UUID sellerId,
        String sellerNickName,   // [수정] N 대문자로 변경 (JSON 키와 일치)
        UUID buyerId,
        String buyerNickName     // [수정] N 대문자로 변경 (JSON 키와 일치)
) {
    public static ReservationCompletedEvent from(Reservation reservation) {
        return new ReservationCompletedEvent(
                reservation.getId(),
                reservation.getProductInfo().getProductId(),
                reservation.getProductInfo().getProductName(),
                reservation.getProductInfo().getProductPrice(),
                reservation.getSellerInfo().getSellerId(),
                reservation.getSellerInfo().getSellerName(),
                reservation.getBuyerInfo().getBuyerId(),
                reservation.getBuyerInfo().getBuyerName()
        );
    }
}
