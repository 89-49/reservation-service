package org.pgsg.reservation.domain.exception;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.pgsg.common.exception.ErrorCode;

@Getter
@RequiredArgsConstructor
public enum ReservationErrorCode implements ErrorCode {

    // 유효성 검사 (R001~R003 계열)
    INVALID_INPUT("[reservation.validation.invalid-input]", "input"),
    INVALID_STATUS("[reservation.validation.invalid-status]", "status"),
    INVALID_SELECT_STATUS("[reservation.validation.invalid-select-status]", "selectStatus"),

    // 서비스 예외 - 권한 및 상태 (R004~R005 계열)
    CANNOT_CHANGE_STATUS("[reservation.exception.cannot-change-status]", "status"),
    UNAUTHORIZED_ACCESS("[reservation.exception.access-denied]", "role"),

    // 서비스 예외 - 조회 실패 (R006 계열)
    RESERVATION_NOT_FOUND("[reservation.exception.not-found.reservation]", "reservationId"),

    // 서비스 예외 - 충돌 및 중복 (R007~R009 계열)
    DUPLICATE_RESERVATION("[reservation.exception.conflict.duplicate-reservation]", "productId"),
    ALREADY_APPLIED("[reservation.exception.conflict.already-applied]", "buyerId"),
    ALREADY_EXISTS("[reservation.exception.conflict.already-exists]", "productId");

    private final String errorKey;
    private final String field;

    public static ReservationErrorCode fromErrorKey(String errorKey) {
        for (ReservationErrorCode errorCode : values()) {
            if (errorCode.getErrorKey().equals(errorKey)) {
                return errorCode;
            }
        }
        return null;
    }
}