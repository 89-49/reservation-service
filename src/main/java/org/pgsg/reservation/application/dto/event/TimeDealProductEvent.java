package org.pgsg.reservation.application.dto.event;

import com.fasterxml.jackson.annotation.JsonFormat;
import java.time.LocalDateTime;
import java.util.UUID;

public record TimeDealProductEvent(
        UUID productId,
        String name,
        Integer price,

        // JSON 배열 [YYYY, M, D, H, m, s] 형식을 LocalDateTime으로 매핑
        @JsonFormat(shape = JsonFormat.Shape.ARRAY)
        LocalDateTime endTime,

        UUID sellerId,
        String sellerName
) {}