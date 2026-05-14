package org.pgsg.reservation.domain.model.reservation;

import java.util.UUID;

public record SearchPolicy(
        UUID accessUserId,
        boolean isUserFilter
) {
    public SearchPolicy {
        if (isUserFilter && accessUserId == null) {
            throw new IllegalArgumentException("accessUserId is required for user filter.");
        }
    }

    public static SearchPolicy user(UUID userId) {
        return new SearchPolicy(userId, true);
    }

    public static SearchPolicy all() {
        return new SearchPolicy(null, false);
    }
}