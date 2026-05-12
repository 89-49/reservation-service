package org.pgsg.reservation.infrastructure.event;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.pgsg.common.event.Events;
import org.pgsg.common.event.OutboxEvent;
import org.pgsg.reservation.application.dto.event.ReservationCancelledEvent;
import org.pgsg.reservation.application.dto.event.ReservationCompletedEvent;
import org.pgsg.reservation.application.dto.event.ReservationFailedEvent;
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

    @Value("${topics.reservation.cancelled}")
    private String buyerCancelledTopicName;

    @Value("${topics.reservation.failed}")
    private String failureTopicName;

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
                    correlationId,    // correlationId
                    domainId,         // domainId (UUID 타입)
                    "RESERVATION",    // domainType
                    completedTopicName,// eventType (토픽명)
                    event             // payload
            ));

            log.info("예약 완료 Outbox 등록 완료 - correlationId: {}", correlationId);

        } catch (Exception e) {
            log.error("Outbox 등록 실패 - 예약 ID: {}", reservation.getId(), e);
            throw new RuntimeException("이벤트 발행 중 오류 발생", e);
        }
    }

    // 상품 생성 실패시 상품에 이벤트 발송
    public void publishReservationCreationFailed(UUID productId, String reason) {
        try {
            log.info("예약 생성 실패 Outbox 등록 시작 - 상품 ID: {}", productId);

            // 엔티티가 없으므로 correlationId와 domainId 모두 productId를 활용하거나,
            // 별도의 랜덤 UUID를 domainId로 생성합니다.
            UUID correlationId = UUID.fromString(productId.toString());
            UUID domainId = UUID.randomUUID(); // Outbox PK용

            ReservationFailedEvent event = new ReservationFailedEvent(productId, reason);

            Events.trigger(new OutboxEvent(
                    correlationId,    // 상품 서비스가 식별할 ID
                    domainId,         // Outbox 식별 ID
                    "RESERVATION",
                    failureTopicName, // 실패 전용 토픽
                    event
            ));

            log.info("예약 생성 실패 Outbox 등록 완료 - productId: {}", productId);

        } catch (Exception e) {
            log.error("실패 알림 Outbox 등록 중 오류 발생 - 상품 ID: {}", productId, e);
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

    // 거래 완료 생성 실패시 발송하는 이벤트
    public void publishTradeConfirmFailed(UUID reservationId, String reason) {
        log.info("예약 생성 실패 Outbox 등록 시작 - 예약 ID: {}", reservationId);

        UUID correlationId = UUID.fromString(reservationId.toString());
        UUID domainId = UUID.randomUUID();

        // 실패 전용 DTO: ReservationFailedEvent
        ReservationFailedEvent event = new ReservationFailedEvent(correlationId, reason);

        Events.trigger(new OutboxEvent(
                correlationId,
                domainId,
                "TRADE", // 도메인 타입 구분
                "prod-trade-rollback", // 거래 서비스가 구독할 토픽명
                event
        ));
    }
}