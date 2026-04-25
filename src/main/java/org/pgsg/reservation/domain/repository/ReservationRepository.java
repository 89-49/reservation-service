package org.pgsg.reservation.domain.repository;

import org.pgsg.reservation.domain.model.reservation.Reservation;
import java.util.Optional;
import java.util.UUID;

public interface ReservationRepository {
    Reservation save(Reservation reservation);
}