package org.pgsg.reservation.infrastructure.repository.reservation;

import org.pgsg.reservation.domain.model.reservation.Reservation;
import org.pgsg.reservation.domain.model.reservation.ReservationStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

public interface JpaReservationRepository extends JpaRepository<Reservation, UUID> {
    List<Reservation> findAllByStatusAndExpirationTimeBefore(ReservationStatus status, LocalDateTime now);
}