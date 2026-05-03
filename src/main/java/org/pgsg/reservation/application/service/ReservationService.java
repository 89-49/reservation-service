package org.pgsg.reservation.application.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.pgsg.reservation.application.dto.command.ReservationCancelCommand;
import org.pgsg.reservation.application.dto.command.ReservationCreateCommand;
import org.pgsg.reservation.application.dto.event.ReservationEventPublisher;
import org.pgsg.reservation.application.dto.info.ReservationCancelInfo;
import org.pgsg.reservation.application.dto.query.ReservationSearchQuery;
import org.pgsg.reservation.application.dto.result.ReservationCreateResult;
import org.pgsg.reservation.application.dto.result.ReservationSearchResult;
import org.pgsg.reservation.domain.dto.ReservationSearchCriteria;
import org.pgsg.reservation.domain.exception.ReservationErrorCode;
import org.pgsg.reservation.domain.exception.ReservationException;
import org.pgsg.reservation.domain.model.reservation.*;
import org.pgsg.reservation.domain.model.reservationcandidate.ReservationCandidate;
import org.pgsg.reservation.domain.model.reservationhistory.ReservationHistory;
import org.pgsg.reservation.domain.repository.ReservationHistoryRepository;
import org.pgsg.reservation.domain.service.ReservationDomainService;
import org.pgsg.reservation.domain.repository.ReservationRepository;
import org.pgsg.reservation.presentation.dto.request.ReservationAdminCancelRequest;
import org.pgsg.reservation.presentation.dto.response.ReservationCandidateResponse;
import org.pgsg.reservation.presentation.dto.response.ReservationDetailResponse;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class ReservationService {

    private final ReservationRepository reservationRepository;
    private final ReservationDomainService reservationDomainService;
    private final ReservationHistoryRepository reservationHistoryRepository;
    private final ReservationEventPublisher reservationEventPublisher;
    // private final ProductClient productClient; // 추후 구현 예정

    // 도메인 서비스 호출 전까지의 작업은 트랜잭션 밖으로 분리(추후 고도화 작업시)
    @Transactional
    public ReservationCreateResult createReservation(ReservationCreateCommand command) {
        try {
            if (reservationRepository.existsByProductId(command.getProductId())) {
                throw new ReservationException(ReservationErrorCode.ALREADY_EXISTS);
            }

            SellerInfo seller = SellerInfo.of(command.getSellerId(), "test");
            ProductInfo product = ProductInfo.of(
                    command.getProductId(),
                    command.getPrice(),
                    command.getProductName(),
                    command.getEndTime()
            );

            Reservation reservation = reservationDomainService.createReservation(
                    seller,
                    product,
                    command.getEndTime()
            );

            Reservation saved = reservationRepository.save(reservation);

            return ReservationCreateResult.from(saved);

        } catch (Throwable t) {
            t.printStackTrace();
            throw t;
        }
    }

    // 예약 목록 조회
    @Transactional(readOnly = true)
    public Page<ReservationSearchResult> getSearchReservations(
            UUID userId,
            String role,
            ReservationSearchQuery query,
            Pageable pageable
    ) {
        // 권한에 따른 조회 범위 결정 로직을 도메인 모델로 전달
        SearchPolicy policy = reservationDomainService.getReservations(userId, role);

        ReservationSearchCriteria criteria = new ReservationSearchCriteria(
                query.sellerName(),
                query.buyerName(),
                query.productName(),
                query.status(),
                query.productId(),
                null, // startDateTime
                null, // endDateTime
                policy
        );

        Page<Reservation> reservations = reservationRepository.findByCriteria(criteria, pageable);

        // Repository(QueryDSL)에 정책과 검색 조건을 함께 전달
        return reservations.map(ReservationSearchResult::from);
    }


    // 예약 상세 조회
    @Transactional(readOnly = true)
    public ReservationDetailResponse getReservationDetail(UUID reservationId, UUID userId, String role) {
        // 엔티티 조회
        Reservation reservation = reservationRepository.findById(reservationId)
                .orElseThrow(() -> new RuntimeException("해당 예약을 찾을 수 없습니다. ID: " + reservationId));

        // 도메인 서비스를 통한 권한 검증
        reservationDomainService.validateDetailAccess(reservation, userId, role);

        return ReservationDetailResponse.from(reservation);
    }

    // 예약 신청
    @Transactional
    public ReservationCandidateResponse applyReservation(UUID reservationId, UUID userId, String nickname) {
        Reservation savedReservation;

        // 예약 엔티티 조회
        Reservation reservation = reservationRepository.findById(reservationId)
                .orElseThrow(() -> new ReservationException(ReservationErrorCode.RESERVATION_NOT_FOUND));

        // 도메인 서비스를 통한 신청 로직 (후보자 생성 및 추가)
        ReservationCandidate candidate = reservationDomainService.addCandidate(reservation, userId, nickname);

        // 변경사항 저장과 예외 감시
        try {
            savedReservation = reservationRepository.saveAndFlush(reservation);
        } catch (DataIntegrityViolationException e) {
            if (isDuplicateApplyViolation(e)) {
                throw new ReservationException(ReservationErrorCode.ALREADY_APPLIED);
            }
            throw e;
        }

        // 방금 저장된 예약에서 추하한 후보자 찾기
        ReservationCandidate savedCandidate = savedReservation.getCandidates().stream()
                .filter(c -> c.getCandidateId().equals(userId))
                .findFirst()
                .orElse(candidate);

        return ReservationCandidateResponse.from(savedCandidate);
    }

    // 구매자 취소 처리 로직
    @Transactional
    public ReservationCancelInfo cancelByBuyer(ReservationCancelCommand command) {
        Reservation reservation = findById(command.reservationId());

        ReservationHistory history = reservationDomainService.cancelByBuyer(
                reservation,
                command.userId(),
                command.role(),
                command.reason()
        );

        reservationHistoryRepository.save(history);

        return ReservationCancelInfo.from(reservation);
    }

    // 판매자 취소 로직
    @Transactional
    public ReservationCancelInfo cancelBySeller(ReservationCancelCommand command) {
        Reservation reservation = findById(command.reservationId());

        ReservationHistory history = reservationDomainService.cancelBySeller(
                reservation,
                command.userId(),
                command.role(),
                command.reason()
        );

        reservationHistoryRepository.save(history);

        return ReservationCancelInfo.from(reservation);
    }

    // 예약 만료
    @Transactional
    public ReservationCancelInfo expireByAdmin(
            UUID reservationId,
            ReservationAdminCancelRequest request,
            UUID adminId,
            String role
    ) {
        Reservation reservation = findById(reservationId);

        ReservationHistory history = reservationDomainService.expireByAdmin(
                reservation,
                adminId,
                role,
                request.targetStatus(),
                request.reason()
        );

        reservationHistoryRepository.save(history);

        return ReservationCancelInfo.from(reservation);
    }

    // 예약 완료
    @Transactional
    public ReservationCancelInfo completeReservation(UUID reservationId, UUID userId, String role) {
        // 예약 엔티티 조회
        Reservation reservation = findById(reservationId);

        ReservationHistory history = reservationDomainService.completeReservation(reservation,userId,role);

        reservationHistoryRepository.save(history);

        reservationEventPublisher.publishReservationCompleted(reservation);

        return ReservationCancelInfo.from(reservation);
    }

    /**
     * 공통 ID 조회 메서드
     */
    private Reservation findById(UUID reservationId) {
        return reservationRepository.findById(reservationId)
                .orElseThrow(() -> new ReservationException(ReservationErrorCode.RESERVATION_NOT_FOUND));
    }

    private boolean isDuplicateApplyViolation(DataIntegrityViolationException e) {
        String message = e.getMostSpecificCause().getMessage();
        return message != null && message.contains("uk_reservation_candidate_user_id");
    }

}