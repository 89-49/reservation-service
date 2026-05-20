package org.pgsg.reservation.infrastructure.repository;

import org.pgsg.common.domain.Outbox;
import org.pgsg.common.domain.OutboxRepository; // 중요: 공통 모듈의 인터페이스
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.UUID;

@Repository
public interface MyReservationOutboxRepository
        extends JpaRepository<Outbox, UUID>, OutboxRepository {
}