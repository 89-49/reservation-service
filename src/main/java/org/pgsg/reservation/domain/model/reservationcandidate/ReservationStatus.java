package org.pgsg.reservation.domain.model.reservationcandidate;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.pgsg.reservation.domain.exception.ReservationException;
import org.pgsg.reservation.domain.exception.ReservationErrorCode;

import java.util.Arrays;

@Getter
@AllArgsConstructor
public enum ReservationStatus {
    PENDING("임시 예약"),
    EXPIRED("예약 만료"),
    CANCELLED("예약 취소"),
    COMPLETED("예약 완료");

    private final String description;

    // 설계서의 "행위" 부분 구현: 문자열로 enum을 찾는 기능
    public static ReservationStatus find(String statusName) {
        return Arrays.stream(values())
                .filter(s -> s.name().equalsIgnoreCase(statusName))
                .findFirst()
                .orElseThrow(() -> new ReservationException(ReservationErrorCode.INVALID_STATUS));
    }
}