package org.pgsg.reservation.domain.model.reservationhistory;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.pgsg.reservation.domain.model.reservation.ReservationStatus;

import java.util.UUID;

@Entity
@Getter
@Table(name = "p_reservation_histories")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class ReservationHistory {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(nullable = false)
    private UUID reservationId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ReservationStatus previousStatus;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ReservationStatus newStatus;

    private String comment; // 변경 사유

    @Column(nullable = false)
    private UUID changedBy; // 상태를 변경한 사용자 ID

    // 상태 변경 이력을 생성
    public static ReservationHistory of(UUID reservationId, ReservationStatus previousStatus,
                                        ReservationStatus newStatus, String comment, UUID changedBy) {
        if (comment == null || comment.isBlank()) {
            throw new IllegalArgumentException("comment must not be blank");
        }
        return new ReservationHistory(null, reservationId, previousStatus, newStatus, comment, changedBy);
    }
    }
}