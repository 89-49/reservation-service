package org.pgsg.reservation.domain.service;

import lombok.RequiredArgsConstructor;
import org.pgsg.reservation.domain.exception.ReservationErrorCode;
import org.pgsg.reservation.domain.exception.ReservationException;
import org.pgsg.reservation.domain.model.reservation.*;
import org.springframework.stereotype.Service;

import java.util.Locale;
import java.util.UUID;


@Service
@RequiredArgsConstructor
public class ReservationDomainService {

    private final ReservationValidator reservationValidator;

    /**
     * 예약 생성 로직
     * 각 VO들을 조합하여 예약 엔티티를 생성하고, 도메인 규칙을 검증
     */
    public Reservation createReservation(BuyerInfo buyer, SellerInfo seller, ProductInfo product) {

        reservationValidator.validate(buyer, seller, product);

        return Reservation.create(buyer, seller, product);
    }

    /**
     * 예약 목록 조회 정책 획득 로직
     * 권한에 따른 조회 범위를 검증하고 정책(Policy)을 결정
     */
    public SearchPolicy getReservations(UUID userId, String role) {
        // 권한 데이터가 비어있는지 검증
        reservationValidator.validateSearchRequest(userId, role);

        // 역할에 따른 필터링
        String normalizedRole = role.trim().toUpperCase(Locale.ROOT);
        if ("ADMIN".equals(normalizedRole)) {
            return new SearchPolicy(userId, false, false);
        }
        if ("SELLER".equals(normalizedRole)) {
            return new SearchPolicy(userId, false, true);
        }
        if ("BUYER".equals(normalizedRole)) {
            return new SearchPolicy(userId, true, false);
        }
        throw new ReservationException(ReservationErrorCode.UNAUTHORIZED_ACCESS);
    }
}