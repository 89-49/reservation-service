package org.pgsg.reservation.application.dto.event;

import org.pgsg.reservation.domain.model.reservation.Reservation;

import java.util.UUID;

public record ReservationCompletedEvent(
        UUID reservationId,
        UUID buyerId,
        String buyerNickname,
        UUID sellerId,
        String sellerNickname,
        UUID productId,
        String productName,
        String status
) {
    public static ReservationCompletedEvent from(Reservation reservation) {
        return new ReservationCompletedEvent(
                reservation.getId(),
                reservation.getBuyerInfo().getBuyerId(),
                reservation.getBuyerInfo().getBuyerName(),
                reservation.getSellerInfo().getSellerId(),
                reservation.getSellerInfo().getSellerName(),
                reservation.getProductInfo().getProductId(),
                reservation.getProductInfo().getProductName(),
                reservation.getStatus().name()
        );
    }
}
