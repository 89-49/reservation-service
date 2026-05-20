package org.pgsg.reservation.domain.model.reservationcandidate;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.pgsg.reservation.domain.exception.ReservationException;
import org.pgsg.reservation.domain.exception.ReservationErrorCode;
import java.util.Arrays;

@Getter
@AllArgsConstructor
public enum ReservationCandidateStatus {
    WAITING("예약후보 선정 대기 중"),
    SELECTED("예약후보로 선정 완료됨"),
    CANCELLED("예약후보로 선정되었다가 취소");

    private final String description;

    public static ReservationCandidateStatus find(String statusName) {
        return Arrays.stream(values())
                .filter(s -> s.name().equalsIgnoreCase(statusName))
                .findFirst()
                .orElseThrow(() -> new ReservationException(ReservationErrorCode.INVALID_SELECT_STATUS));
    }
}