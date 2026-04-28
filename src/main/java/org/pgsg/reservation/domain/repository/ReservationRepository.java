package org.pgsg.reservation.domain.repository;

import org.pgsg.reservation.domain.dto.ReservationSearchCriteria;
import org.pgsg.reservation.domain.model.reservation.Reservation;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.Optional;
import java.util.UUID;

public interface ReservationRepository {
    Reservation save(Reservation reservation);

    // QueryDSL을 활용한 동적 검색 구현
    Page<Reservation> findByCriteria(ReservationSearchCriteria criteria, Pageable pageable);

    Optional<Reservation> findById(UUID id);
}