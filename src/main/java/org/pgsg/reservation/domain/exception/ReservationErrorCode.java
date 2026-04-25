package org.pgsg.reservation.domain.exception;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum ReservationErrorCode {

    // 400 Bad Request
    INVALID_STATUS(400, "잘못된 예약 상태입니다."),
    INVALID_SELECT_STATUS(400, "잘못된 선정 상태입니다."),
    CANNOT_CHANGE_STATUS(400, "현재 상태에서는 변경할 수 없는 요청입니다."),

    // 404 Not Found
    RESERVATION_NOT_FOUND(404, "해당 예약을 찾을 수 없습니다."),

    // 409 Conflict (중복 등 비즈니스 규칙 위반)
    DUPLICATE_RESERVATION(409, "이미 해당 상품에 대한 예약 진행 내역이 존재합니다.");

    private final int status;
    private final String message;
}