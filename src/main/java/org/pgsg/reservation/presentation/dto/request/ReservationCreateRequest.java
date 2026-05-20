package org.pgsg.reservation.presentation.dto.request;

import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.UUID;

@Getter
@NoArgsConstructor
public class ReservationCreateRequest {

    @NotNull(message = "상품 ID는 필수입니다.")
    private UUID productId;

    @NotNull(message = "상품 이름은 필수입니다.")
    private String productName;

    @NotNull(message = "가격은 필수입니다.")
    private Integer price;

    @NotNull(message = "종료 시간은 필수 입니다.")
    private LocalDateTime endTime;

    @NotNull(message = "판매자 ID는 필수입니다.")
    private UUID sellerId;

    @NotNull(message = "판매자 닉네임은 필수입니다.")
    private String sellerNickname;
}