package org.pgsg.reservation.presentation.dto.response;

import lombok.Builder;
import org.pgsg.reservation.application.dto.info.ReservationCancelInfo;
import org.pgsg.reservation.domain.model.reservation.ReservationStatus;

import java.time.LocalDateTime;
import java.util.UUID;

@Builder
public record ReservationCancelResponse(
        UUID reservationId,
        ReservationStatus status,
        LocalDateTime updatedAt,
        String message
) {
    public static ReservationCancelResponse of(ReservationCancelInfo info, String message) {
        return ReservationCancelResponse.builder()
                .reservationId(info.reservationId())
                .status(info.status())
                .updatedAt(info.updatedAt())
                .message(message)
                .build();
    }
}