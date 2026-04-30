package org.pgsg.reservation.domain.service;

import lombok.RequiredArgsConstructor;
import org.pgsg.reservation.domain.exception.ReservationErrorCode;
import org.pgsg.reservation.domain.exception.ReservationException;
import org.pgsg.reservation.domain.model.reservation.*;
import org.pgsg.reservation.domain.model.reservationcandidate.ReservationCandidate;
import org.pgsg.reservation.domain.model.reservationcandidate.ReservationCandidateStatus;
import org.pgsg.reservation.domain.model.reservationhistory.ReservationHistory;
import org.springframework.stereotype.Service;

import java.util.Comparator;
import java.util.Locale;
import java.util.Optional;
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

        // 현재 구매자가 없는 상태(reopen된 상태)라면 내가 바로 구매자가 변경
        boolean shouldBeSelectedImmediately = (reservation.getBuyerInfo() == null);

        // 후보자 생성 및 애그리거트에 추가
        ReservationCandidate candidate = ReservationCandidate.create(reservation, userId, nickname);

        reservation.addCandidate(candidate);

        // 만약 첫 번째 후보자라면 바로 구매자로 선정하는 로직
        if (shouldBeSelectedImmediately) {
            reservation.changeToNextBuyer(candidate);
        }

        return candidate;
    }

    /**
     * 구매자 사유 취소 도메인 로직
     * 구매자 혹은 관리자가 호출하며, 취소 후 다음 대기자에게 예약 권한을 승계함
     */
    public ReservationHistory cancelByBuyer(Reservation reservation, UUID userId, String role, String reason) {
        validateReason(reason);

        // 권한 및 상태 검증
        reservationValidator.validateCancelByBuyer(reservation, userId, role);

        ReservationStatus previousStatus = reservation.getStatus();

        // 엔티티 상태 변경
        reservation.cancelByBuyer();

        // 다음 구매자 승계 처리
        handleNextBuyerSequence(reservation);

        // 이력 객체 생성
        return ReservationHistory.of(
                reservation.getId(),
                previousStatus,
                reservation.getStatus(),
                reason,
                userId
        );
    }

    /**
     * 판매자 사유 취소 도메인 로직
     * 판매자 혹은 관리자가 호출하며, 취소 후 예약 비활성화 후 상품 삭제 요청
     */
    public ReservationHistory cancelBySeller(Reservation reservation, UUID userId, String role, String reason) {
        validateReason(reason);

        // 권한 및 상태 검증
        reservationValidator.validateCancelBySeller(reservation, userId, role);

        ReservationStatus previousStatus = reservation.getStatus();

        // 엔티티 상태 변경
        reservation.cancelBySeller();

        // 이력 객체 생성
        return ReservationHistory.of(
                reservation.getId(),
                previousStatus,
                reservation.getStatus(),
                reason,
                userId
        );
    }

    /**
     * 예약 만료 로직
     * 예약이 만료 될 시 작동 혹은 시스템 문제로 관리자 임의 실행
     */
    public ReservationHistory expireByAdmin(
            Reservation reservation,
            UUID adminId,
            String role,
            ReservationStatus targetStatus,
            String reason
    ) {
        // 관리자 권한 및 취소 가능 상태인지 확인
        reservationValidator.validateSearchRequest(adminId, role);
        reservationValidator.validateCommonCancel(reservation, adminId);

        // 이력 기록을 위해 변경 전 상태 보관
        ReservationStatus previousStatus = reservation.getStatus();

        // 상태 변경
        if (targetStatus == ReservationStatus.CANCELLED_BY_BUYER) {
            // 구매자 사유 취소로 취급 -> 차순위 승계(handleNextBuyer) 로직 작동
            reservation.cancelByBuyer();

            // 여기서 직접 차순위 로직을 호출하거나, 이벤트를 발행
            handleNextBuyerSequence(reservation);

        } else if (targetStatus == ReservationStatus.CANCELLED_BY_SELLER) {
            // 판매자 사유 취소로 취급 -> 승계 없이 최종 종료
            reservation.cancelBySeller();
        } else {
            // 그 외 정의되지 않은 상태 변경 시도 시 예외
            throw new ReservationException(ReservationErrorCode.INVALID_INPUT);
        }

        // 3. 결과물(History) 생성 및 반환
        return ReservationHistory.of(
                reservation.getId(),
                previousStatus,
                reservation.getStatus(),
                reason,
                adminId
        );
    }

    /**
     * 다음 구매자 승계 내부 로직
     */
    private void handleNextBuyerSequence(Reservation reservation) {
        // WAITING 상태 중 생성일 오름차순 -> ID 오름차순
        // 대기자 중 가장 우선 우선순위가 높은 사람을 찾음
        Optional<ReservationCandidate> nextCandidate = reservation.getCandidates().stream()
                .filter(c -> c.getStatus() == ReservationCandidateStatus.WAITING)
                .min(Comparator.comparing(ReservationCandidate::getCreatedAt)
                        .thenComparing(ReservationCandidate::getId));

        if (nextCandidate.isPresent()) {
            // a. 대기자가 있으면 승계 처리
            reservation.changeToNextBuyer(nextCandidate.get());
        } else {
            // b. 대기자가 없으면 다시 누구나 신청 가능한 상태로 복구
            reservation.reopen();
        }
    }

    private void validateReason(String reason) {
        if (reason == null || reason.isBlank()) {
            throw new ReservationException(ReservationErrorCode.INVALID_INPUT);
        }
    }
}