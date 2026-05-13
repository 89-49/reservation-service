package org.pgsg.reservation.infrastructure.listener;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.pgsg.common.messaging.annotation.IdempotentConsumer;
import org.pgsg.reservation.application.dto.command.ReservationConfirmCommand;
import org.pgsg.reservation.application.service.ReservationService;
import org.pgsg.reservation.infrastructure.listener.dto.TradeCompletedEvent;
import org.pgsg.reservation.infrastructure.event.ReservationEventBridge;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

import java.util.UUID;


@Slf4j
@Component
@RequiredArgsConstructor
public class ReservationEventListener {

    private final ReservationService reservationService;
    private final ObjectMapper objectMapper;
    private final ReservationEventBridge bridge;

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
        TradeCompletedEvent event;
        UUID reservationId = null;
        try {
            JsonNode root = objectMapper.readTree(record.value());
            try {
                reservationId = UUID.fromString(root.get("reservationId").asText());
            } catch (IllegalArgumentException e) {
                log.warn("유효하지 않은 UUID 형식의 reservationId 수신: {}", root.get("reservationId").asText());
            }
            event = objectMapper.treeToValue(root, TradeCompletedEvent.class);

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
                    (reservationId != null) ? reservationId : "Unknown", e.getMessage());

            // 실패 알림 발송
            // 이 알림을 보고 거래 서비스나 관리자 서비스가 후속 조치(거래 롤백 등)를 할 수 있습니다.
            if (reservationId != null) {
                // 사실 여기서는 productId를 알기 어려울 수 있으니,
                // 브릿지에 reservationId를 받는 실패 메서드를 하나 더 만들거나
                // event에서 productId를 꺼낼 수 있도록 설계하는 것이 좋을 수도?
                bridge.publishTradeConfirmFailed(
                        reservationId, // UUID 타입
                        "CONFIRM_FAIL: " + e.getMessage()
                );
            }
        }
    }
}