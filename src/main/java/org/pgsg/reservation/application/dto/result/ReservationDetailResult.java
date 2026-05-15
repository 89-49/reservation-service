package org.pgsg.reservation.application.dto.result;

import org.pgsg.reservation.domain.model.reservation.Reservation;
import org.pgsg.reservation.domain.model.reservation.ReservationStatus;

import java.time.LocalDateTime;
import java.util.UUID;

public record ReservationDetailResult(
        UUID reservationId,
        ReservationStatus status,
        ProductInfo product,
        SellerInfo seller,
        BuyerInfo buyer,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {
    public static record ProductInfo(
            UUID productId,
            String productName,
            Integer price,
            LocalDateTime endTime
    ) {}

    public static record SellerInfo(
            UUID sellerId,
            String sellerName
    ) {}

    public static record BuyerInfo(
            UUID buyerId,
            String buyerName
    ) {}

    public static ReservationDetailResult from(Reservation reservation) {
        return new ReservationDetailResult(
                reservation.getId(),
                reservation.getStatus(),
                new ProductInfo(
                        reservation.getProductInfo().getProductId(),
                        reservation.getProductInfo().getProductName(),
                        reservation.getProductInfo().getProductPrice(),
                        reservation.getProductInfo().getEndTime()
                ),
                new SellerInfo(
                        reservation.getSellerInfo().getSellerId(),
                        reservation.getSellerInfo().getSellerName()
                ),
                reservation.getBuyerInfo() != null ? new BuyerInfo(
                        reservation.getBuyerInfo().getBuyerId(),
                        reservation.getBuyerInfo().getBuyerName()
                ) : null,
                reservation.getCreatedAt(),
                reservation.getModifiedAt()
        );
    }
}