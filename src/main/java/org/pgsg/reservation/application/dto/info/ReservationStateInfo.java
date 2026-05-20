package org.pgsg.reservation.application.dto.info;

import lombok.Builder;
import org.pgsg.reservation.domain.model.reservation.Reservation;
import org.pgsg.reservation.domain.model.reservation.ReservationStatus;
import java.time.LocalDateTime;
import java.util.UUID;

@Builder
public record ReservationStateInfo(
        UUID reservationId,
        ReservationStatus status,
        LocalDateTime updatedAt
) {
    public static ReservationStateInfo from(Reservation reservation) {
        return ReservationStateInfo.builder()
                .reservationId(reservation.getId())
                .status(reservation.getStatus())
                .updatedAt(reservation.getModifiedAt())
                .build();
    }
}