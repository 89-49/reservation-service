package org.pgsg.reservation.application.dto.result;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
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
    @JsonCreator
    public ReservationDetailResult(
            @JsonProperty("reservationId") UUID reservationId,
            @JsonProperty("status") ReservationStatus status,
            @JsonProperty("product") ProductInfo product,
            @JsonProperty("seller") SellerInfo seller,
            @JsonProperty("buyer") BuyerInfo buyer,
            @JsonProperty("createdAt") LocalDateTime createdAt,
            @JsonProperty("updatedAt") LocalDateTime updatedAt
            ) {
        this.reservationId = reservationId;
        this.status = status;
        this.product = product;
        this.seller = seller;
        this.buyer = buyer;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }

    public static record ProductInfo(
            UUID productId,
            String productName,
            Integer price,
            LocalDateTime endTime
    ) {
        @JsonCreator
        public ProductInfo(
                @JsonProperty("productId") UUID productId,
                @JsonProperty("productName") String productName,
                @JsonProperty("price") Integer price,
                @JsonProperty("endTime") LocalDateTime endTime
        ) {
            this.productId = productId;
            this.productName = productName;
            this.price = price;
            this.endTime = endTime;
        }
    }

    public static record SellerInfo(
            UUID sellerId,
            String sellerName
    ) {
        @JsonCreator
        public SellerInfo(
                @JsonProperty("sellerId") UUID sellerId,
                @JsonProperty("sellerName") String sellerName
        ) {
            this.sellerId = sellerId;
            this.sellerName = sellerName;
        }
    }

    public static record BuyerInfo(
            UUID buyerId,
            String buyerName
    ) {
        @JsonCreator
        public BuyerInfo(
                @JsonProperty("buyerId") UUID buyerId,
                @JsonProperty("buyerName") String buyerName
        ) {
            this.buyerId = buyerId;
            this.buyerName = buyerName;
        }
    }

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