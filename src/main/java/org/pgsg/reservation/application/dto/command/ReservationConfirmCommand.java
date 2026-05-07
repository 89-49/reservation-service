package org.pgsg.reservation.application.dto.command;

import java.util.UUID;

public record ReservationConfirmCommand(
    UUID reservationId,
    UUID userId,
    String role
){
    public static ReservationConfirmCommand of(UUID reservationId, UUID userId, String role) {
        return new ReservationConfirmCommand(reservationId,userId,role);
    }
}
