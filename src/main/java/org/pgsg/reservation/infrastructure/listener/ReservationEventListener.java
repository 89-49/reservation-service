package org.pgsg.reservation.infrastructure.listener;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.pgsg.common.messaging.annotation.IdempotentConsumer;
import org.pgsg.reservation.application.dto.command.ReservationConfirmCommand;
import org.pgsg.reservation.application.service.ReservationService;
import org.pgsg.reservation.infrastructure.listener.dto.TradeCompletedEvent;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

import java.util.UUID;


@Slf4j
@Component
@RequiredArgsConstructor
public class ReservationEventListener {

    private final ReservationService reservationService;
    private final ObjectMapper objectMapper;

    private static final UUID SYSTEM_ID = UUID.fromString("00000000-0000-0000-0000-000000000000");
    private static final String SYSTEM_ROLE = "ADMIN";

    /**
     * 거래 서비스로부터 거래 완료 이벤트를 수신
     */
    @IdempotentConsumer("reservation-completed:trade-service")
    @KafkaListener(
            topics = "prod-trade-completed",
            groupId = "reservation-group"
    )
    public void handleTradeCompleted(ConsumerRecord<String, String> record) {

        try {
            TradeCompletedEvent event = objectMapper.readValue(record.value(), TradeCompletedEvent.class);
            log.info("거래 완료 이벤트 수신 - Trade ID: {}, Reservation ID: {}",
                    event.tradeId(), event.reservationId());

            ReservationConfirmCommand command = new ReservationConfirmCommand(
                    event.reservationId(),
                    SYSTEM_ID,
                    SYSTEM_ROLE
            );

            reservationService.confirmTrade(command);

            log.info("예약 확정 처리 성공 - Reservation ID: {}", event.reservationId());
        }catch (Exception e) {
            log.error("예약 확정 처리 실패 - Reservation ID: {}, Error: {}",
                    record.topic(), e.getMessage());
        }
    }
}