package org.pgsg.reservation.infrastructure.event;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.pgsg.reservation.application.dto.event.ReservationCompletedEvent;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

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

        kafkaTemplate.send(topicName, event);
    }
}
