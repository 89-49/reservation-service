package org.pgsg.reservation.application.dto.event;

import org.pgsg.reservation.domain.model.reservation.Reservation;

import java.util.UUID;

public record ReservationCancelledEvent(
        UUID reservationId,
        UUID productId,
        String reason
) {
    public static ReservationCancelledEvent from(Reservation reservation, String reason) {
        return new ReservationCancelledEvent(
                reservation.getId(),
                reservation.getProductInfo().getProductId(),
                reason
        );
    }
}
