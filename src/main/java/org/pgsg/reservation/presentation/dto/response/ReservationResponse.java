package org.pgsg.reservation.presentation.dto.response;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import org.pgsg.reservation.application.dto.result.ReservationCreateResult;
import org.pgsg.reservation.application.dto.result.ReservationSearchResult;

import java.time.LocalDateTime;
import java.util.UUID;

@Getter
@Builder
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class ReservationResponse {

    private UUID reservationId;
    private String status;

    // 목록 조회를 위해 추가된 필드들
    private String productName;
    private String sellerName;
    private String buyerName;
    private LocalDateTime createdAt;

    /**
     * 예약 생성 결과로부터 변환
     */
    public static ReservationResponse from(ReservationCreateResult result) {
        return ReservationResponse.builder()
                .reservationId(result.getReservationId())
                .status(result.getStatus())
                .build();
    }

    /**
     * 예약 목록 조회 결과로부터 변환
     */
    public static ReservationResponse from(ReservationSearchResult result) {
        return ReservationResponse.builder()
                .reservationId(result.reservationId())
                .status(result.status().name())
                .productName(result.productName())
                .sellerName(result.sellerName())
                .buyerName(result.buyerName())
                .createdAt(result.createdAt())
                .build();
    }
}