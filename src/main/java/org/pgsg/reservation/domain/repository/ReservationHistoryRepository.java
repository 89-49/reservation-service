package org.pgsg.reservation.domain.repository;

import org.pgsg.reservation.domain.model.reservationhistory.ReservationHistory;

public interface ReservationHistoryRepository {
    ReservationHistory save(ReservationHistory history);
}