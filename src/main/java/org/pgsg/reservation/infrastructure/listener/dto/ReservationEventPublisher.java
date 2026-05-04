package org.pgsg.reservation.infrastructure.listener.dto;

import org.pgsg.reservation.domain.model.reservation.Reservation;

public interface ReservationEventPublisher {
    void publishReservationCompleted(Reservation reservation);
}
