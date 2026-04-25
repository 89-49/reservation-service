package org.pgsg.reservation.presentation.dto.request;

import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Getter
@NoArgsConstructor
public class ReservationCreateRequest {

    @NotNull(message = "상품 ID는 필수입니다.")
    private UUID productId;
}