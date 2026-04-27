package org.pgsg.reservation.domain.model.reservation;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.pgsg.reservation.domain.exception.ReservationException;
import org.pgsg.reservation.domain.exception.ReservationErrorCode;

import java.util.Arrays;

@Getter
@AllArgsConstructor
public enum ReservationStatus {
    PENDING("임시 예약"),
    PAID("결제 완료"),
    EXPIRED("예약 만료"),
    CANCELLED("예약 취소"),
    COMPLETED("예약 완료");

    private final String description;

    public static ReservationStatus find(String statusName) {
        return Arrays.stream(values())
                .filter(s -> s.name().equalsIgnoreCase(statusName))
                .findFirst()
                .orElseThrow(() -> new ReservationException(ReservationErrorCode.INVALID_STATUS));
    }
}