package org.pgsg.reservation.presentation.dto.response;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import org.pgsg.reservation.application.dto.result.ReservationCreateResult;

import java.util.UUID;

@Getter
@Builder
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class ReservationResponse {

    private UUID reservationId;
    private String status;

    public static ReservationResponse from(ReservationCreateResult result) {
        return ReservationResponse.builder()
                .reservationId(result.getReservationId())
                .status(result.getStatus())
                .build();
    }
}
