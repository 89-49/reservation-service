package org.pgsg.reservation.presentation.dto.response;

import org.pgsg.reservation.application.dto.result.ReservationDetailResult;
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
            int price,
            LocalDateTime endTime
    ) {}

    public record SellerDetailInfo(
            UUID sellerId,
            String sellerName
    ) {}

    public record BuyerDetailInfo(
            UUID buyerId,
            String buyerName
    ) {}

    public static ReservationDetailResponse from(ReservationDetailResult result) {
        return new ReservationDetailResponse(
                result.reservationId(),
                result.status(),
                new ProductDetailInfo(result.product().productId(), result.product().productName(), result.product().price(),result.product().endTime()),
                new SellerDetailInfo(result.seller().sellerId(), result.seller().sellerName()),
                result.buyer() != null ? new BuyerDetailInfo(
                        result.buyer().buyerId(),
                        result.buyer().buyerName()
                ) : null,
                result.createdAt(),
                result.updatedAt()
        );
    }
}
