package org.pgsg.reservation.application.dto.command;

import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;
import java.util.UUID;

@Getter
@Builder
public class ReservationCreateCommand {
    private UUID productId;

    private UUID sellerId;      // 판매자 ID
    private String sellerName;  // 판매자 이름 (또는 닉네임)
    private String productName; // 상품명
    private Integer price;      // 가격
    private LocalDateTime endTime; // 종료 시간
}