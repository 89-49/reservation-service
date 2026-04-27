package org.pgsg.reservation.domain.model.reservation;

import java.util.UUID;

public record SearchPolicy(
        UUID accessUserId,
        boolean isBuyerFilter,
        boolean isSellerFilter
) {
}