package org.pgsg.reservation.application.dto.event;

import org.pgsg.reservation.domain.model.reservation.Reservation;
import java.util.UUID;

public record ReservationCompletedEvent(
        UUID reservationId,
        UUID productId,
        String productName,
        Integer productPrice,
        UUID sellerId,
        String sellerNickName,
        UUID buyerId,
        String buyerNickName
) {
    public static ReservationCompletedEvent from(Reservation reservation) {
        return new ReservationCompletedEvent(
                reservation.getId(),
                reservation.getProductInfo().getProductId(),
                reservation.getProductInfo().getProductName(),
                reservation.getProductInfo().getProductPrice(),
                reservation.getSellerInfo().getSellerId(),
                reservation.getSellerInfo().getSellerName(),
                reservation.getBuyerInfo() != null ? reservation.getBuyerInfo().getBuyerId() : null,
                reservation.getBuyerInfo() != null ? reservation.getBuyerInfo().getBuyerName() : "Unknown"
        );
    }
}