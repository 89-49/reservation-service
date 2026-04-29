package org.pgsg.reservation.domain.service;

import lombok.RequiredArgsConstructor;
import org.pgsg.reservation.domain.exception.ReservationErrorCode;
import org.pgsg.reservation.domain.exception.ReservationException;
import org.pgsg.reservation.domain.model.reservation.*;
import org.pgsg.reservation.domain.model.reservationcandidate.ReservationCandidate;
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

    /**
     * 상세 조회 권한 검증
     * 특정 사용자가 해당 예약에 접근할 수 있는지 비즈니스 규칙 검사
     */
    public void validateDetailAccess(Reservation reservation, UUID userId, String role) {
        // 정책 획득
        SearchPolicy policy = this.getReservations(userId, role);

        // 관리자 판단 로직
        boolean isAdmin = !policy.isBuyerFilter() && !policy.isSellerFilter();

        if (!isAdmin) {
            // 본인 확인 (구매자 혹은 판매자 본인인지 체크)
            boolean isOwner = reservation.getBuyerInfo().getBuyerId().equals(userId) ||
                    reservation.getSellerInfo().getSellerId().equals(userId);

            if (!isOwner) {
                throw new RuntimeException("해당 예약을 조회할 권한이 없습니다.");
            }
        }
    }

    /**
     * 구매자 예약 신청
     * 특정 사용자가 해당 예약에 접근할 수 있는지 비즈니스 규칙 검사
     */
    public ReservationCandidate addCandidate(Reservation reservation, UUID userId, String nickname) {
        // 이미 후보자로 등록되어 있는지 확인
        boolean isAlreadyApplied = reservation.getCandidates().stream()
                .anyMatch(c -> c.getCandidateId().equals(userId));
        if (isAlreadyApplied) {
            throw new ReservationException(ReservationErrorCode.ALREADY_APPLIED);
        }

        // 예약이 활성 상태(AVAILABLE)인지 확인
        if (reservation.getStatus() != ReservationStatus.AVAILABLE) {
            throw new ReservationException(ReservationErrorCode.CANNOT_CHANGE_STATUS);
        }

        // 후보자 생성 및 애그리거트에 추가
        ReservationCandidate candidate = ReservationCandidate.create(reservation, userId, nickname);
        reservation.addCandidate(candidate);

        // 만약 첫 번째 후보자라면 바로 구매자로 선정하는 로직
        reservation.changeToNextBuyer(candidate);

        return candidate;
    }
}