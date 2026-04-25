package org.pgsg.reservation.infrastructure.repository;

import lombok.RequiredArgsConstructor;
import org.pgsg.reservation.domain.model.reservation.Reservation;
import org.pgsg.reservation.domain.repository.ReservationRepository;
import org.springframework.stereotype.Repository;

@Repository
@RequiredArgsConstructor
public class ReservationRepositoryImpl implements ReservationRepository {

    private final ReservationJpaRepository reservationJpaRepository;

    @Override
    public Reservation save(Reservation reservation) {
        return reservationJpaRepository.save(reservation);
    }
}
