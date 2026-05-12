package org.pgsg.reservation.infrastructure.listener;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.pgsg.common.messaging.annotation.IdempotentConsumer;
import org.pgsg.reservation.application.dto.command.ReservationCreateCommand;
import org.pgsg.reservation.infrastructure.listener.dto.TimeDealProductEvent;
import org.pgsg.reservation.application.service.ReservationService;
import org.pgsg.reservation.domain.exception.ReservationErrorCode;
import org.pgsg.reservation.domain.exception.ReservationException;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;
import org.pgsg.reservation.infrastructure.event.ReservationEventBridge;

import java.util.Objects;

@Slf4j
@Component
@RequiredArgsConstructor
public class ProductListener {

    private final ReservationService reservationService;
    private final ObjectMapper objectMapper;
    private final ReservationEventBridge eventBridge;

    /**
     * 상품 서비스에서 타임딜 상품이 생성되었을 때의 이벤트를 수신합니다.
     */
    @IdempotentConsumer("product-completed:product-service")
    @KafkaListener(topics = "prod-product-created",groupId = "product-group")
    public void handleTimeDealEvent(ConsumerRecord<String, String> record) {
        TimeDealProductEvent event = null;

        try {
            event = objectMapper.readValue(record.value(), TimeDealProductEvent.class);
            log.info("타임딜 상품 생성 이벤트 수신 - Product ID: {}, Name: {}",
                    event.productId(), event.name());

            ReservationCreateCommand command = new ReservationCreateCommand(
                    event.productId(), event.sellerId(), event.sellerName(),
                    event.name(), event.price(), event.endTime()
            );

            reservationService.createReservation(command);
        }catch (ReservationException e) {
            if (e.getErrorCode() == ReservationErrorCode.ALREADY_EXISTS) {
                log.info("중복 예약 생성 이벤트 무시 (이미 존재): productId={}", Objects.requireNonNull(event).productId());
                return;
            }

            log.error("예약 생성 비즈니스 실패: {}", e.getMessage());
            eventBridge.publishReservationCreationFailed(Objects.requireNonNull(event).productId(), e.getMessage());

        } catch (Exception e) {
            log.error("시스템 장애로 인한 예약 생성 실패: {}", e.getMessage());
            if (event != null) {
                eventBridge.publishReservationCreationFailed(event.productId(), "SYSTEM_ERROR: " + e.getMessage());
            }
        }
    }

}