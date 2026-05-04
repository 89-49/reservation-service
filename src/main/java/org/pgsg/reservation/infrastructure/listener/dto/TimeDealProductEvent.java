package org.pgsg.reservation.infrastructure.listener.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonProperty; // 추가됨
import java.time.LocalDateTime;
import java.util.UUID;

public record TimeDealProductEvent(
        UUID productId,

        String name,

        Integer price,

        @JsonFormat(shape = JsonFormat.Shape.ARRAY)
        LocalDateTime endTime,

        UUID sellerId,

        @JsonProperty("sellerNickName")
        String sellerName
) {}