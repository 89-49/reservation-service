package org.pgsg.reservation.presentation.dto.response;

import lombok.Builder;
import org.pgsg.reservation.application.dto.info.ReservationStateInfo;
import org.pgsg.reservation.domain.model.reservation.ReservationStatus;

import java.time.LocalDateTime;
import java.util.UUID;

@Builder
public record ReservationStateResponse(
        UUID reservationId,
        ReservationStatus status,
        LocalDateTime updatedAt,
        String message
) {
    public static ReservationStateResponse of(ReservationStateInfo info, String message) {
        return ReservationStateResponse.builder()
                .reservationId(info.reservationId())
                .status(info.status())
                .updatedAt(info.updatedAt())
                .message(message)
                .build();
    }
}