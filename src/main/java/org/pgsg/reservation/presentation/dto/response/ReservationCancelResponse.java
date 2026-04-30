package org.pgsg.reservation.presentation.dto.response;

import lombok.Builder;
import org.pgsg.reservation.domain.model.reservation.ReservationStatus;

import java.time.LocalDateTime;
import java.util.UUID;

@Builder
public record ReservationCancelResponse(
        UUID reservationId,
        ReservationStatus status,
        LocalDateTime updatedAt,
        String message
) {
    public static ReservationCancelResponse of(Object reservation, String message) {
        return ReservationCancelResponse.builder()
                .reservationId(((org.pgsg.reservation.domain.model.reservation.Reservation) reservation).getId())
                .status(((org.pgsg.reservation.domain.model.reservation.Reservation) reservation).getStatus())
                .updatedAt(((org.pgsg.reservation.domain.model.reservation.Reservation) reservation).getModifiedAt())
                .message(message)
                .build();
    }
}