package org.pgsg.reservation.infrastructure.event;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.pgsg.common.event.Events;
import org.pgsg.common.event.OutboxEvent;
import org.pgsg.reservation.application.dto.event.ReservationCancelledEvent;
import org.pgsg.reservation.application.dto.event.ReservationCompletedEvent;
import org.pgsg.reservation.infrastructure.listener.dto.ReservationEventPublisher;
import org.pgsg.reservation.domain.model.reservation.Reservation;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Component
@RequiredArgsConstructor
@Slf4j
public class ReservationEventBridge implements ReservationEventPublisher {

    @Value("${topics.reservation.completed}")
    private String completedTopicName;

    @Value("prod-reservation-buyercancelled")
    private String buyerCancelledTopicName;

    @Override
    public void publishReservationCompleted(Reservation reservation) {
        try {
            log.info("예약 완료 Outbox 등록 시작 - 예약 ID: {}", reservation.getId());

            // Product 서비스에서 식별자로 쓸 ID (상품 ID)
            UUID correlationId = UUID.fromString(reservation.getProductInfo().getProductId().toString());

            // Outbox 테이블의 PK로 저장될 ID (예약 ID)
            UUID domainId = UUID.fromString(reservation.getId().toString());

            // 페이로드 준비
            ReservationCompletedEvent event = ReservationCompletedEvent.from(reservation);

            Events.trigger(new OutboxEvent(
                    correlationId,    // 1. correlationId
                    domainId,         // 2. domainId (UUID 타입)
                    "RESERVATION",    // 3. domainType
                    completedTopicName,        // 4. eventType (토픽명)
                    event             // 5. payload
            ));

            log.info("예약 완료 Outbox 등록 완료 - correlationId: {}", correlationId);

        } catch (Exception e) {
            log.error("Outbox 등록 실패 - 예약 ID: {}", reservation.getId(), e);
            throw new RuntimeException("이벤트 발행 중 오류 발생", e);
        }
    }

    @Override
    public void publishReservationCancelled(Reservation reservation, String reason) {
        try {
            log.info("판매자 사유로 인한 취소 Outbox 등록 시작 - 예약 ID: {}", reservation.getId());

            // Product/Trade 서비스에서 식별자로 쓸 ID (상품 ID)
            UUID correlationId = UUID.fromString(reservation.getProductInfo().getProductId().toString());

            // Outbox 테이블의 PK로 저장될 ID (예약 ID)
            UUID domainId = UUID.fromString(reservation.getId().toString());

            ReservationCancelledEvent event = ReservationCancelledEvent.from(reservation, reason);

            Events.trigger(new OutboxEvent(
                    correlationId,    // 연관 ID (상품 ID)
                    domainId,         // 도메인 ID (예약 ID)
                    "RESERVATION",    // 도메인 타입
                    buyerCancelledTopicName,   // 이벤트 타입 (취소 토픽명)
                    event             // 페이로드
            ));

            log.info("판매자 사유로 인한 취소 Outbox 등록 완료 - correlationId: {}", correlationId);

        } catch (Exception e) {
            log.error("취소 Outbox 등록 실패 - 예약 ID: {}", reservation.getId(), e);
            throw new RuntimeException("취소 이벤트 발행 중 오류 발생", e);
        }
    }
}