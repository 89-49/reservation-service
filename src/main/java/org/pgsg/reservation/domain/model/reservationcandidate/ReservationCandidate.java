package org.pgsg.reservation.domain.model.reservationcandidate;

import jakarta.persistence.*;
import lombok.*;
import org.pgsg.common.domain.BaseEntity;
import org.pgsg.reservation.domain.exception.ReservationErrorCode;
import org.pgsg.reservation.domain.exception.ReservationException;
import org.pgsg.reservation.domain.model.reservation.Reservation;
import org.pgsg.reservation.domain.model.reservation.SelectStatus;
import java.util.Objects;

import java.util.UUID;

@Entity
@Getter
@Table(name = "p_reservation_candidates", uniqueConstraints = {
        @UniqueConstraint(
                name = "uk_reservation_candidate_nickname",
                columnNames = {"reservation_id", "candidateNickname"}
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
    private SelectStatus status = SelectStatus.WAITING;

    // 예약 후보 생성
    public static ReservationCandidate of(Reservation reservation, UUID candidateId, String candidateNickname) {
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
        if (this.status != SelectStatus.WAITING) {
            throw new ReservationException(ReservationErrorCode.CANNOT_CHANGE_STATUS);
        }
        this.status = SelectStatus.SELECTED;
    }

    // 후보 취소: 후보자가 대기를 철회하거나 선정에서 제외될 때 사용
    public void cancel() {
        if (this.status == SelectStatus.CANCELLED) {
            throw new ReservationException(ReservationErrorCode.CANNOT_CHANGE_STATUS);
        }
        this.status = SelectStatus.CANCELLED;
    }


}