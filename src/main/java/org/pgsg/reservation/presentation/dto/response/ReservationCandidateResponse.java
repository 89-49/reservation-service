package org.pgsg.reservation.presentation.dto.response;

import org.pgsg.reservation.domain.model.reservationcandidate.ReservationCandidate;
import java.time.LocalDateTime;
import java.util.UUID;

public record ReservationCandidateResponse(
        Long id,                        // 예약후보 고유 순번
        UUID candidateId,              // 예약후보자 회원 ID
        String candidateNickname,      // 예약후보자 닉네임
        String status,                 // 선정 상태 (WAITING, SELECTED, CANCELLED)
        LocalDateTime createdAt        // 예약후보 등록 일자
) {
    public static ReservationCandidateResponse from(ReservationCandidate candidate) {
        return new ReservationCandidateResponse(
                candidate.getId(),               // 엔티티의 id 필드 매핑
                candidate.getCandidateId(),       // 후보자 UUID 매핑
                candidate.getCandidateNickname(), // 후보자 닉네임 매핑
                candidate.getStatus().name(),     // Enum을 String으로 변환
                candidate.getCreatedAt()          // BaseEntity로부터 상속받은 생성일자
        );
    }
}