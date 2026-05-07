package org.pgsg.reservation.application.dto.command;

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
}