package org.pgsg.reservation.infrastructure.event;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.MDC;
import org.pgsg.reservation.application.dto.event.ReservationCompletedEvent;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;
import org.pgsg.common.event.OutboxEvent;
import org.pgsg.common.event.Events;

import java.util.UUID;

@Component
@RequiredArgsConstructor
@Slf4j
public class ReservationEventBridge {

    private final KafkaTemplate<String, Object> kafkaTemplate;

    @Value("${topics.reservation.completed}")
    private String topicName;

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void handleReservationCompleted(ReservationCompletedEvent event) {
        log.info("커밋 완료 확인: 카프카로 이벤트를 전송합니다. ID: {}", event.reservationId());

        String mdcTraceId = MDC.get("traceId");
        UUID correlationId;

        try {
            if (mdcTraceId != null && mdcTraceId.length() == 36) {
                correlationId = UUID.fromString(mdcTraceId);
            } else {
                correlationId = UUID.randomUUID();
            }
        } catch (IllegalArgumentException e) {
            correlationId = UUID.randomUUID();
        }

        OutboxEvent outboxEvent = new OutboxEvent(
                correlationId,
                event.reservationId(),
                "reservation",
                topicName,
                event
        );

        Events.trigger(outboxEvent);
    }
}
