package org.pgsg.reservation.domain.repository;

import org.pgsg.reservation.application.dto.query.ReservationSearchQuery;
import org.pgsg.reservation.application.dto.result.ReservationSearchResult;
import org.pgsg.reservation.domain.model.reservation.Reservation;
import org.pgsg.reservation.domain.model.reservation.SearchPolicy;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.Optional;
import java.util.UUID;

public interface ReservationRepository {
    Reservation save(Reservation reservation);

    // QueryDSL을 활용한 동적 검색 구현
    Page<ReservationSearchResult> searchReservations(
            SearchPolicy policy,
            ReservationSearchQuery query,
            Pageable pageable
    );

    Optional<Reservation> findById(UUID id);
}