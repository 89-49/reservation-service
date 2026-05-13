package org.pgsg.reservation.application.dto.event;

import java.util.UUID;

public record ReservationFailedEvent(
        UUID Id,
        String reason
) {}
