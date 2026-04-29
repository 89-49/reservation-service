package org.pgsg.reservation.domain.model.reservationcandidate;

import jakarta.persistence.*;
import lombok.*;
import org.pgsg.common.domain.BaseEntity;
import org.pgsg.reservation.domain.exception.ReservationErrorCode;
import org.pgsg.reservation.domain.exception.ReservationException;
import org.pgsg.reservation.domain.model.reservation.Reservation;
import org.pgsg.reservation.domain.model.reservation.ReservationStatus;
import java.util.Objects;

import java.util.UUID;

@Entity
@Getter
@Table(name = "p_reservation_candidates", uniqueConstraints = {
        @UniqueConstraint(
                name = "uk_reservation_candidate_user_id",
                columnNames = {"reservation_id", "candidateId"}
        )
})
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class ReservationCandidate extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "reservation_id")
    private Reservation reservation;

    private UUID candidateId;      // 후보자 식별자
    private String candidateNickname; // 후보자 닉네임

    @Enumerated(EnumType.STRING)
    private ReservationCandidateStatus status = ReservationCandidateStatus.WAITING;

    // 예약 후보 생성
    public static ReservationCandidate create(Reservation reservation, UUID candidateId, String candidateNickname) {
        verify(reservation, candidateId, candidateNickname);
        ReservationCandidate candidate = new ReservationCandidate();
        candidate.reservation = reservation;
        candidate.candidateId = candidateId;
        candidate.candidateNickname = candidateNickname;
        return candidate;
    }

    // 유효성 검사
    private static void verify(Reservation reservation, UUID candidateId, String candidateNickname) {
        Objects.requireNonNull(reservation, "reservation must not be null");
        Objects.requireNonNull(candidateId, "candidateId must not be null");
        if (candidateNickname == null || candidateNickname.isBlank()) {
            throw new IllegalArgumentException("candidateNickname must not be blank");
        }
    }

    // 후보 선정: 판매자가 후보를 구매자로 최종 선택했을 때 사용
    public void selected() {
        validateReservationPendingStatus();
        if (this.status != ReservationCandidateStatus.WAITING) {
            throw new ReservationException(ReservationErrorCode.CANNOT_CHANGE_STATUS);
        }
        this.status = ReservationCandidateStatus.SELECTED;
    }

    // 후보 취소: 후보자가 대기를 철회하거나 선정에서 제외될 때 사용
    public void cancel() {
        if (this.status == ReservationCandidateStatus.CANCELLED) {
            throw new ReservationException(ReservationErrorCode.CANNOT_CHANGE_STATUS);
        }

        // 상태 변경
        this.status = ReservationCandidateStatus.CANCELLED;

        // 부모 예약 엔티티의 후보 리스트에서 제거
        if (this.reservation != null) {
            this.reservation.removeCandidate(this);
        }
    }

    // Reservation에서 예약 상태(PENDING)이어야 후보자 선정 가능
    private void validateReservationPendingStatus() {
        if (this.reservation == null || this.reservation.getStatus() != ReservationStatus.PENDING) {
            throw new ReservationException(ReservationErrorCode.CANNOT_CHANGE_STATUS);
        }
    }
}