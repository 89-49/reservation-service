package org.pgsg.reservation.application.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.pgsg.reservation.application.dto.command.*;
import org.pgsg.reservation.application.dto.result.CustomPage;
import org.pgsg.reservation.application.dto.result.ReservationDetailResult;
import org.pgsg.reservation.infrastructure.listener.dto.ReservationEventPublisher;
import org.pgsg.reservation.application.dto.info.ReservationStateInfo;
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
import org.pgsg.reservation.application.dto.info.ReservationCandidateInfo;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.cache.annotation.Caching;
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

    // 도메인 서비스 호출 전까지의 작업은 트랜잭션 밖으로 분리(추후 고도화 작업시)
    @Transactional
    public ReservationCreateResult createReservation(ReservationCreateCommand command) {
        try {
            if (reservationRepository.existsByProductId(command.productId())) {
                throw new ReservationException(ReservationErrorCode.ALREADY_EXISTS);
            }

            SellerInfo seller = SellerInfo.of(command.sellerId(), "test");
            ProductInfo product = ProductInfo.of(
                    command.productId(),
                    command.price(),
                    command.productName(),
                    command.endTime()
            );

            Reservation reservation = reservationDomainService.createReservation(
                    seller,
                    product
            );

            Reservation saved;
            try {
                saved = reservationRepository.save(reservation);
            }catch (DataIntegrityViolationException e) {
                throw new ReservationException(ReservationErrorCode.ALREADY_EXISTS);
            }

            return ReservationCreateResult.from(saved);

        } catch (DataIntegrityViolationException e) {
            // 동시에 들어온 요청으로 인해 유니크 제약 조건 위반 시 409 에러 발생
            throw new ReservationException(ReservationErrorCode.ALREADY_EXISTS);
        }
    }

    // 예약 목록 조회
    @Transactional(readOnly = true)
    @Cacheable(value = "reservations",
            key = "#userId.toString() + ':' + #role + ':' + " +
                    "(#query.sellerName() != null ? #query.sellerName() : '') + ':' + " +
                    "(#query.buyerName() != null ? #query.buyerName() : '') + ':' + " +
                    "(#query.productName() != null ? #query.productName() : '') + ':' + " +
                    "(#query.status() != null ? #query.status().name() : '') + ':' + " +
                    "(#query.productId() != null ? #query.productId().toString() : '') + ':' + " +
                    "#pageable.pageNumber + ':' + #pageable.pageSize")
    public CustomPage<ReservationSearchResult> getSearchReservations(
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

        return CustomPage.from(reservations.map(ReservationSearchResult::from));
    }


    // 예약 상세 조회
    @Cacheable(
            value = "reservationDetail",
            key = "#reservationId.toString()",
            unless = "#result == null"
    )
    @Transactional(readOnly = true)
    public ReservationDetailResult getReservationDetail(UUID reservationId, UUID userId, String role) {
        // 엔티티 조회
        Reservation reservation = reservationRepository.findById(reservationId)
                .orElseThrow(() -> new RuntimeException("해당 예약을 찾을 수 없습니다. ID: " + reservationId));
        // 도메인 서비스를 통한 권한 검증
        reservationDomainService.validateDetailAccess(reservation, userId, role);

        return ReservationDetailResult.from(reservation);
    }

    // 예약 신청
    @Transactional
    @Caching(evict = {
            @CacheEvict(value = "reservationDetail", key = "#command.reservationId().toString()"),
    })
    public ReservationCandidateInfo proceedApplyTransaction(ReservationApplyCommand command) {

        // 예약 엔티티 조회
        Reservation reservation = reservationRepository.findById(command.reservationId())
                .orElseThrow(() -> new ReservationException(ReservationErrorCode.RESERVATION_NOT_FOUND));

        // 도메인 서비스를 통한 신청 로직 (후보자 생성 및 추가)
        ReservationCandidate candidate = reservationDomainService.addCandidate(reservation, command.userId(), command.nickname());

        // 변경사항 저장과 예외 감시
        try {
            reservationRepository.save(reservation);
        } catch (DataIntegrityViolationException e) {
            if (isDuplicateApplyViolation(e)) {
                throw new ReservationException(ReservationErrorCode.ALREADY_APPLIED);
            }
            throw e;
        }
        return ReservationCandidateInfo.from(candidate);
    }

    // 구매자 취소 처리 로직
    @Transactional
    @Caching(evict = {
            @CacheEvict(value = "reservationDetail", key = "#command.reservationId().toString()"),
    })
    public ReservationStateInfo cancelByBuyer(ReservationCancelCommand command) {
        Reservation reservation = findById(command.reservationId());

        ReservationHistory history = reservationDomainService.cancelByBuyer(
                reservation,
                command.userId(),
                command.role(),
                command.reason()
        );

        reservationHistoryRepository.save(history);

        return ReservationStateInfo.from(reservation);
    }

    // 추후 결제 시스템 연동시 트리거 발동
    @Transactional
    @Caching(evict = {
            @CacheEvict(value = "reservationDetail", key = "#command.reservationId().toString()"),
    })
    public ReservationStateInfo confirmPayment(ReservationConfirmCommand command) {
        Reservation reservation = findById(command.reservationId());

        ReservationHistory history = reservationDomainService.confirmPayment(
                reservation,
                command.userId(),
                command.role()
        );

        if (history != null) {
            reservationHistoryRepository.save(history);
        } else {
            log.info("이미 결제 완료 처리된 예약입니다(중복 요청 무시): reservationId={}", reservation.getId());
        }

        return ReservationStateInfo.from(reservation);
    }

    // 판매자 취소 로직
    @Transactional
    @Caching(evict = {
            @CacheEvict(value = "reservationDetail", key = "#command.reservationId().toString()"),
    })
    public ReservationStateInfo cancelBySeller(ReservationCancelCommand command) {
        Reservation reservation = findById(command.reservationId());

        ReservationHistory history = reservationDomainService.cancelBySeller(
                reservation,
                command.userId(),
                command.role(),
                command.reason()
        );

        reservationHistoryRepository.save(history);
        reservationEventPublisher.publishReservationCancelled(reservation,command.reason());

        return ReservationStateInfo.from(reservation);
    }

    // 예약 만료
    @Transactional
    @Caching(evict = {
            @CacheEvict(value = "reservationDetail", key = "#command.reservationId().toString()"),
    })
    public ReservationStateInfo expireByAdmin(ReservationExpireCommand command) {
        Reservation reservation = findById(command.reservationId());

        ReservationHistory history = reservationDomainService.expireByAdmin(
                reservation,
                command.userId(),
                command.role(),
                command.targetStatus(),
                command.reason()
        );

        reservationHistoryRepository.save(history);

        return ReservationStateInfo.from(reservation);
    }

    // 예약 완료
    @Transactional
    @Caching(evict = {
            @CacheEvict(value = "reservationDetail", allEntries = true),
    })
    public ReservationStateInfo completeReservation(ReservationConfirmCommand command) {
        // 예약 엔티티 조회
        Reservation reservation = findById(command.reservationId());

        ReservationHistory history = reservationDomainService.completeReservation(reservation,command.userId(),command.role());

        reservationHistoryRepository.save(history);
        reservationEventPublisher.publishReservationCompleted(reservation);

        return ReservationStateInfo.from(reservation);
    }

    // 거래 완료
    @Transactional
    @Caching(evict = {
            @CacheEvict(value = "reservationDetail", key = "#command.reservationId().toString()"),
    })
    public ReservationStateInfo confirmTrade(ReservationConfirmCommand command) {

        Reservation reservation = findById(command.reservationId());

        ReservationHistory history = reservationDomainService.confirmTrade(
                reservation,
                command.userId(),
                command.role()
        );

        if (history != null) {
            reservationHistoryRepository.save(history);
        }

        return ReservationStateInfo.from(reservation);
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