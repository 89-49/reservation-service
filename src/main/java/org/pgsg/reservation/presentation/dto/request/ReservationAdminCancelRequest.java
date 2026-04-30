package org.pgsg.reservation.presentation.dto.request;

import org.pgsg.reservation.domain.model.reservation.ReservationStatus;

public record ReservationAdminCancelRequest(
        ReservationStatus targetStatus,
        String reason
) {}