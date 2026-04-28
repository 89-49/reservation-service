package org.pgsg.reservation.domain.model.reservation;

import java.util.UUID;

public record SearchPolicy(
        UUID accessUserId,
        boolean isBuyerFilter,
        boolean isSellerFilter
) {
    public SearchPolicy {
        if (isBuyerFilter && isSellerFilter) {
            throw new IllegalArgumentException("Filter flags are mutually exclusive.");
        }

        if ((isBuyerFilter || isSellerFilter) && accessUserId == null) {
            throw new IllegalArgumentException("accessUserId is required for buyer/seller filter.");
        }

        if (!isBuyerFilter && !isSellerFilter && accessUserId != null) {
            throw new IllegalArgumentException("accessUserId must be null when no filter is applied.");
        }
    }

    public static SearchPolicy buyer(UUID userId) {
        return new SearchPolicy(userId, true, false);
    }

    public static SearchPolicy seller(UUID userId) {
        return new SearchPolicy(userId, false, true);
    }

    public static SearchPolicy all() {
        return new SearchPolicy(null, false, false);
    }
}