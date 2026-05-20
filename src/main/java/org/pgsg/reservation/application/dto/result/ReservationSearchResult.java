package org.pgsg.reservation.application.dto.result;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import org.pgsg.reservation.domain.model.reservation.Reservation;
import org.pgsg.reservation.domain.model.reservation.ReservationStatus;

import java.time.LocalDateTime;
import java.util.UUID;

public record ReservationSearchResult(
        UUID reservationId,
        String productName,
        String sellerName,
        String buyerName,
        ReservationStatus status,
        LocalDateTime createdAt
) {
    @JsonCreator
    public ReservationSearchResult(
            @JsonProperty("reservationId") UUID reservationId,
            @JsonProperty("productName") String productName,
            @JsonProperty("sellerName") String sellerName,
            @JsonProperty("buyerName") String buyerName,
            @JsonProperty("status") ReservationStatus status,
            @JsonProperty("createdAt") LocalDateTime createdAt
    ) {
        this.reservationId = reservationId;
        this.productName = productName;
        this.sellerName = sellerName;
        this.buyerName = buyerName;
        this.status = status;
        this.createdAt = createdAt;
    }

    public static ReservationSearchResult from(Reservation reservation) {
        return new ReservationSearchResult(
                reservation.getId(),
                reservation.getProductInfo().getProductName(),
                reservation.getSellerInfo().getSellerName(),
                reservation.getBuyerInfo() != null ? reservation.getBuyerInfo().getBuyerName() : null,
                reservation.getStatus(),
                reservation.getCreatedAt()
        );
    }
}