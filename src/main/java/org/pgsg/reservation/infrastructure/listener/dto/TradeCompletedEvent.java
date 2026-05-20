package org.pgsg.reservation.infrastructure.listener.dto;

import java.util.UUID;

public record TradeCompletedEvent(
        UUID tradeId,
        UUID reservationId,
        UUID productId
) {}