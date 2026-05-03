package org.pgsg.reservation.application.dto.event;

import org.pgsg.reservation.domain.model.reservation.Reservation;

public interface ReservationEventPublisher {
    void publishReservationCompleted(Reservation reservation);
}
