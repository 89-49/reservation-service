package org.pgsg.reservation.application.dto.query;

import org.pgsg.reservation.domain.model.reservationcandidate.ReservationStatus;

import java.util.UUID;


public record ReservationSearchQuery(
        String sellerName,
        String buyerName,
        String productName,
        ReservationStatus status,
        UUID productId
) {}