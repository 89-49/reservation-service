package org.pgsg.reservation.domain.dto;

import org.pgsg.reservation.domain.model.reservation.ReservationStatus;
import org.pgsg.reservation.domain.model.reservation.SearchPolicy;

import java.time.LocalDateTime;
import java.util.UUID;

public record ReservationSearchCriteria(
        String sellerName,
        String buyerName,
        String productName,
        ReservationStatus status,
        UUID productId,
        LocalDateTime startDateTime,
        LocalDateTime endDateTime,
        SearchPolicy policy
) {
}