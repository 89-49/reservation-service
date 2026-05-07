package org.pgsg.reservation.domain.exception;

import org.pgsg.common.exception.CustomException;
import org.pgsg.common.exception.ErrorCode;

public class ReservationException extends CustomException {

    public ReservationException(ErrorCode errorCode) {
        super(errorCode, null);
    }

    public ReservationException(ErrorCode errorCode, String field) {
        super(errorCode, field);
    }

    public ReservationException(ErrorCode errorCode, Throwable cause) {
        super(errorCode, null);
        this.initCause(cause);
    }
}