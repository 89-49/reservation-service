package org.pgsg.reservation.domain.model.reservationcandidate;

import jakarta.persistence.*;
import lombok.*;
import org.pgsg.common.domain.BaseEntity;
import org.pgsg.reservation.domain.model.reservation.Reservation;
import org.pgsg.reservation.domain.model.reservation.SelectStatus;

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
        ReservationCandidate candidate = new ReservationCandidate();
        candidate.reservation = reservation;
        candidate.candidateId = candidateId;
        candidate.candidateNickname = candidateNickname;
        return candidate;
    }

    // 후보 선정: 판매자가 후보를 구매자로 최종 선택했을 때 사용
    public void selected() {
        this.status = SelectStatus.SELECTED;
    }

    // 후보 취소: 후보자가 대기를 철회하거나 선정에서 제외될 때 사용
    public void cancel() {
        this.status = SelectStatus.CANCELLED;
    }
}