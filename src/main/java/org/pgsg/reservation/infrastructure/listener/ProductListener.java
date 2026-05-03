package org.pgsg.reservation.infrastructure.listener;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.pgsg.reservation.application.dto.command.ReservationCreateCommand;
import org.pgsg.reservation.application.dto.event.TimeDealProductEvent;
import org.pgsg.reservation.application.service.ReservationService;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class ProductListener {

    private final ReservationService reservationService;

    /**
     * 상품 서비스에서 타임딜 상품이 생성되었을 때의 이벤트를 수신합니다.
     */
    @KafkaListener(topics = "prod-product-created",groupId = "product-group")
    public void handleTimeDealEvent(TimeDealProductEvent event) {
        ReservationCreateCommand command = ReservationCreateCommand.builder()
                .productId(event.productId())
                .productName(event.name())
                .price(event.price())
                .endTime(event.endTime())
                .sellerId(event.sellerId())
                .sellerName(event.sellerName())
                .build();

        reservationService.createReservation(command);
    }

}