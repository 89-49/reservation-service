package org.pgsg.reservation.presentation.dto.request;

import jakarta.validation.constraints.NotBlank;

public record ReservationCancelRequest(
        @NotBlank(message = "취소 사유를 입력해주세요.")
        String reason
) {}