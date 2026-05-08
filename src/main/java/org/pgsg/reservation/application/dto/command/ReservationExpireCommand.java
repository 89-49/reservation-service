package org.pgsg.reservation.application.dto.command;

import org.pgsg.reservation.domain.model.reservation.ReservationStatus;
import org.pgsg.reservation.presentation.dto.request.ReservationAdminCancelRequest;

import java.util.UUID;

public record ReservationExpireCommand(
        UUID reservationId,
        UUID userId,
        String role,
        ReservationStatus targetStatus,
        String reason
) {
    public static ReservationExpireCommand of(UUID reservationId, UUID userId, String role, ReservationAdminCancelRequest request) {
        return new ReservationExpireCommand(
                reservationId,
                userId,
                role,
                request.targetStatus(),
                request.reason()
        );
    }
}
