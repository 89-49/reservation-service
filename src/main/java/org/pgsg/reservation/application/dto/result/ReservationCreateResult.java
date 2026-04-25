package org.pgsg.reservation.application.dto.result;

import lombok.Builder;
import lombok.Getter;
import java.util.UUID;

@Getter
@Builder
public class ReservationCreateResult {
    private UUID reservationId;
    private String status;
}