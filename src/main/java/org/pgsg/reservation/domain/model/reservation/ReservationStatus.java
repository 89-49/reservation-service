package org.pgsg.reservation.domain.model.reservation;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.pgsg.reservation.domain.exception.ReservationException;
import org.pgsg.reservation.domain.exception.ReservationErrorCode;

import java.util.Arrays;

@Getter
@AllArgsConstructor
public enum ReservationStatus {
    AVAILABLE("예약 활성화", true),
    PENDING("임시 예약(결제 전)", true),
    PAID("결제 완료(채팅 전)", true),
    COMPLETED("예약 완료(채팅 수락)", true),
    CANCELLED_BY_BUYER("구매자 사유 취소", true),  // 변경 가능
    CANCELLED_BY_SELLER("판매자 사유 취소", false), // 변경 불가
    CLOSED("최종 종료", false);                   // 변경 불가

    private final String description;
    private final boolean isMutable; // 상태 변경이 가능한 상태인지 여부

    public static ReservationStatus find(String statusName) {
        return Arrays.stream(values())
                .filter(s -> s.name().equalsIgnoreCase(statusName))
                .findFirst()
                .orElseThrow(() -> new ReservationException(ReservationErrorCode.INVALID_STATUS));
    }

    /**
     * 판매자 취소와 같은 종료 상태인지 확인
     */
    public boolean isFinalStatus() {
        return !this.isMutable;
    }
}