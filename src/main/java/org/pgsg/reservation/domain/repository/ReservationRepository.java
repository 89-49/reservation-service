package org.pgsg.reservation.domain.repository;

import org.pgsg.reservation.domain.dto.ReservationSearchCriteria;
import org.pgsg.reservation.domain.model.reservation.Reservation;
import org.pgsg.reservation.domain.model.reservation.ReservationStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface ReservationRepository {
    Reservation save(Reservation reservation);

    // QueryDSL을 활용한 동적 검색 구현
    Page<Reservation> findByCriteria(ReservationSearchCriteria criteria, Pageable pageable);

    Optional<Reservation> findById(UUID id);

    // 예약 만료 스케줄링용
    List<Reservation> findAllByStatusInAndModifiedAtBeforeOrProductInfoEndTimeBefore(
            List<ReservationStatus> statuses,
            LocalDateTime threshold,
            LocalDateTime now
    );

    // 상품 존재 여부 확인
    boolean existsByProductId(UUID productId);
}