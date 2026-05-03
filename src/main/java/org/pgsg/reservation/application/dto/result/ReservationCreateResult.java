package org.pgsg.reservation.application.dto.result;

import lombok.Builder;
import lombok.Getter;
import org.pgsg.reservation.domain.model.reservation.Reservation;

import java.time.LocalDateTime;
import java.util.UUID;

@Getter
@Builder
public class ReservationCreateResult {
    private UUID reservationId;
    private String status;
    private String productName;
    private String sellerName;
    private String buyerName;
    private LocalDateTime createdAt;

    public static ReservationCreateResult from(Reservation reservation) {
        return ReservationCreateResult.builder()
                .reservationId(reservation.getId())
                .status(reservation.getStatus().name())
                .productName(reservation.getProductInfo().getProductName())
                .sellerName(reservation.getSellerInfo().getSellerName())
                .buyerName(reservation.getBuyerInfo() != null ? reservation.getBuyerInfo().getBuyerName() : null)
                .createdAt(reservation.getCreatedAt())
                .build();
    }
}