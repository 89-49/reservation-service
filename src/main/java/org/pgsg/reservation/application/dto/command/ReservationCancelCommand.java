package org.pgsg.reservation.application.dto.command;

import java.util.UUID;

public record ReservationCancelCommand(
        UUID reservationId,
        UUID userId,
        String role,
        String reason
) {
    public static ReservationCancelCommand of(UUID id, UUID userId, String role, String reason) {
        return new ReservationCancelCommand(id, userId, role, reason);
    }
}