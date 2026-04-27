package org.pgsg.reservation.application.dto.result;

import org.pgsg.reservation.domain.model.reservation.ReservationStatus;

import java.time.LocalDateTime;
import java.util.UUID;

public record ReservationSearchResult(
        UUID reservationId,
        String productName,
        String sellerName,
        String buyerName,
        ReservationStatus status, // 도메인 Enum 직접 사용
        LocalDateTime createdAt
) {
}