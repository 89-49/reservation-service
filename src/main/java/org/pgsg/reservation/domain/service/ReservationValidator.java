package org.pgsg.reservation.domain.service;

import org.pgsg.reservation.domain.model.reservation.*;
import org.pgsg.reservation.domain.exception.*;
import org.springframework.stereotype.Component;

import java.util.Objects;
import java.util.UUID;

@Component

public class ReservationValidator {

    // 예약 생성
    public void validate(SellerInfo seller, ProductInfo product) {
        if (seller == null || product == null || seller.getSellerId() == null) {
            throw new ReservationException(ReservationErrorCode.INVALID_INPUT);
        }
    }

    public void validateSearchRequest(UUID userId, String role) {
        if (userId == null || role == null || role.isBlank()) {
            // 권한 정보가 부족할 때 던지는 예외
            throw new ReservationException(ReservationErrorCode.UNAUTHORIZED_ACCESS);
        }
    }

    // 구매자,관리자 취소 권한 및 상태 검증
    public void validateCancelByBuyer(Reservation reservation, UUID userId, String role) {
        validateCommonCancel(reservation, userId);

        boolean isBuyer = reservation.getBuyerInfo() != null &&
                Objects.equals(reservation.getBuyerInfo().getBuyerId(), userId);

        boolean isAdmin = "ADMIN".equalsIgnoreCase(role);

        if (!isBuyer && !isAdmin) {
            throw new ReservationException(ReservationErrorCode.UNAUTHORIZED_ACCESS);
        }
    }

    // 판매자,관리자 취소 권한 및 상태 검증
    public void validateCancelBySeller(Reservation reservation, UUID userId, String role) {
        validateCommonCancel(reservation, userId);

        boolean isSeller = reservation.getSellerInfo() != null &&
                Objects.equals(reservation.getSellerInfo().getSellerId(), userId);

        boolean isAdmin = "ADMIN".equalsIgnoreCase(role);

        if (!isSeller && !isAdmin) {
            throw new ReservationException(ReservationErrorCode.UNAUTHORIZED_ACCESS);
        }
    }

    // 공통 취소 가능 상태 검증
    public void validateCommonCancel(Reservation reservation, UUID userId) {
        if (reservation == null || userId == null) {
            throw new ReservationException(ReservationErrorCode.INVALID_INPUT);
        }

        // 이미 종료된 상태이거나 취소 불가능한 상태인지 확인
        if (reservation.getStatus() == null || !reservation.getStatus().isMutable()) {
            throw new ReservationException(ReservationErrorCode.CANNOT_CHANGE_STATUS);
        }
    }

    // 본인 상품 예약 금지 규칙
    private boolean isSamePerson(BuyerInfo buyer, SellerInfo seller) {
        return buyer.getBuyerId().equals(seller.getSellerId());
    }
}
