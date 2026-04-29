package org.pgsg.reservation.infrastructure.repository.reservation;

import org.pgsg.reservation.domain.model.reservation.Reservation;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.UUID;

public interface ReservationJpaRepository extends JpaRepository<Reservation, UUID> {
}