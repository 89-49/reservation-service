package org.pgsg.reservation.infrastructure.repository.reservationhistory;

import org.pgsg.reservation.domain.model.reservationhistory.ReservationHistory;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.UUID;

public interface JpaReservationHistoryRepository extends JpaRepository<ReservationHistory, UUID> {
}