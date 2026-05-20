package org.pgsg.reservation.application.dto.command;

import java.util.UUID;

public record ReservationApplyCommand(
        UUID reservationId,
        UUID userId,
        String nickname
) {
    public static ReservationApplyCommand of(UUID reservationId, UUID userId, String nickname) {
        return new ReservationApplyCommand(reservationId, userId, nickname);
    }
}