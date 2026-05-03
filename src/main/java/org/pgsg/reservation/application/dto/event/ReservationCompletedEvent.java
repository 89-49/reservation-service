package org.pgsg.reservation.application.dto.event;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import org.pgsg.reservation.domain.model.reservation.Reservation;

import java.util.UUID;

@JsonAutoDetect(fieldVisibility = JsonAutoDetect.Visibility.ANY)
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
                reservation.getBuyerInfo().getBuyerId(),
                reservation.getBuyerInfo().getBuyerName()
        );
    }
}
