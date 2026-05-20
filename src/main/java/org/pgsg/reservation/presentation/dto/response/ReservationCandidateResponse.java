package org.pgsg.reservation.presentation.dto.response;

import org.pgsg.reservation.application.dto.info.ReservationCandidateInfo;

public record ReservationCandidateResponse(
        ReservationCandidateInfo info,
        String message
) {
    public static ReservationCandidateResponse of(ReservationCandidateInfo info, String message) {
        return new ReservationCandidateResponse(info, message);
    }
}