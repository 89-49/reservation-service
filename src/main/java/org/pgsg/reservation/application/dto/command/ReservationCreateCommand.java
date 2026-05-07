package org.pgsg.reservation.application.dto.command;

import org.pgsg.reservation.infrastructure.listener.dto.TimeDealProductEvent;
import org.pgsg.reservation.presentation.dto.request.ReservationCreateRequest;

import java.time.LocalDateTime;
import java.util.UUID;

public record ReservationCreateCommand (
        UUID productId,
        UUID sellerId,
        String sellerName,
        String productName,
        Integer price,
        LocalDateTime endTime
){
        public static ReservationCreateCommand of(ReservationCreateRequest request) {
            return new ReservationCreateCommand(
                    request.getProductId(),
                    request.getSellerId(),
                    request.getSellerNickname(), // 명칭 매핑 (Nickname -> Name)
                    request.getProductName(),
                    request.getPrice(),
                    request.getEndTime()
            );
        }

    public static ReservationCreateCommand from(TimeDealProductEvent event) {
        return new ReservationCreateCommand(
                event.productId(),
                event.sellerId(),
                event.sellerName(),
                event.name(),
                event.price(),
                event.endTime()
        );
    }
    }