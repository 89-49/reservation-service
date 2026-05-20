package org.pgsg.reservation.application.dto.info;

import org.pgsg.reservation.domain.model.reservationcandidate.ReservationCandidate;
import java.time.LocalDateTime;
import java.util.UUID;

public record ReservationCandidateInfo(
        UUID candidateId,
        String candidateNickname,
        String status,
        LocalDateTime createdAt
) {
    public static ReservationCandidateInfo from(ReservationCandidate candidate) {
        return new ReservationCandidateInfo(
                candidate.getCandidateId(),
                candidate.getCandidateNickname(),
                candidate.getStatus().name(),
                candidate.getCreatedAt()
        );
    }
}