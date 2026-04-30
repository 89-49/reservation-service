package org.pgsg.reservation.infrastructure.repository;

import lombok.RequiredArgsConstructor;
import org.pgsg.reservation.domain.model.reservationhistory.ReservationHistory;
import org.pgsg.reservation.domain.repository.ReservationHistoryRepository;
import org.pgsg.reservation.infrastructure.repository.reservationhistory.JpaReservationHistoryRepository;
import org.springframework.stereotype.Repository;

@Repository
@RequiredArgsConstructor
public class ReservationHistoryRepositoryImpl implements ReservationHistoryRepository {

    private final JpaReservationHistoryRepository jpaRepository;

    @Override
    public ReservationHistory save(ReservationHistory history) {
        return jpaRepository.save(history);
    }
}