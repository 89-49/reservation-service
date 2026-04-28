package org.pgsg.reservation.presentation.dto.response;

import org.pgsg.reservation.domain.model.reservation.Reservation;
import org.pgsg.reservation.domain.model.reservation.ReservationStatus;

import java.time.LocalDateTime;
import java.util.UUID;

public record ReservationDetailResponse(
        UUID reservationId,
        ReservationStatus status,
        ProductDetailInfo product,
        SellerDetailInfo seller,
        BuyerDetailInfo buyer,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {
    public record ProductDetailInfo(
            UUID productId,
            String productName,
            int price
    ) {}

    public record SellerDetailInfo(
            UUID sellerId,
            String sellerName
    ) {}

    public record BuyerDetailInfo(
            UUID buyerId,
            String buyerName
    ) {}

    public static ReservationDetailResponse from(Reservation reservation) {
        return new ReservationDetailResponse(
                reservation.getId(),
                reservation.getStatus(),
                new ProductDetailInfo(
                        reservation.getProductInfo().getProductId(),
                        reservation.getProductInfo().getProductName(),
                        reservation.getProductInfo().getProductPrice()
                ),
                new SellerDetailInfo(
                        reservation.getSellerInfo().getSellerId(),
                        reservation.getSellerInfo().getSellerName()
                ),
                new BuyerDetailInfo(
                        reservation.getBuyerInfo().getBuyerId(),
                        reservation.getBuyerInfo().getBuyerName()
                ),
                reservation.getCreatedAt(),
                reservation.getModifiedAt()
        );
    }
}
