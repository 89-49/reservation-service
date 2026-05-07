package org.pgsg.reservation.presentation.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.pgsg.reservation.application.dto.command.*;
import org.pgsg.reservation.application.dto.info.ReservationCandidateInfo;
import org.pgsg.reservation.application.dto.info.ReservationStateInfo;
import org.pgsg.reservation.application.dto.query.ReservationSearchQuery;
import org.pgsg.reservation.application.dto.result.ReservationCreateResult;
import org.pgsg.reservation.application.dto.result.ReservationDetailResult;
import org.pgsg.reservation.application.service.ReservationService;
import org.pgsg.reservation.domain.model.reservation.ReservationStatus;
import org.pgsg.reservation.presentation.dto.request.ReservationAdminCancelRequest;
import org.pgsg.reservation.presentation.dto.request.ReservationCancelRequest;
import org.pgsg.reservation.presentation.dto.request.ReservationCreateRequest;
import org.pgsg.reservation.presentation.dto.request.ReservationSearchRequest;
import org.pgsg.reservation.presentation.dto.response.*;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/reservations")
public class ReservationController {

    private final ReservationService reservationService;

    // 예약 생성
    @PostMapping
    public ReservationResponse createReservation(
            @Valid @RequestBody ReservationCreateRequest request
    ) {
        ReservationCreateCommand command = ReservationCreateCommand.of(request);

        ReservationCreateResult result = reservationService.createReservation(command);

        return ReservationResponse.from(result);
    }

    // 예약 목록 조회
    @GetMapping
    public ReservationPageResponse<ReservationResponse> getReservations(
            @RequestHeader("X-User-Id") UUID userId,
            @RequestHeader("X-User-Role") String role,
            @ModelAttribute ReservationSearchRequest request,
            @PageableDefault(size = 10) Pageable pageable
    ) {

        ReservationSearchQuery query = new ReservationSearchQuery(
                request.getSellerName(),
                request.getBuyerName(),
                request.getProductName(),
                request.getStatus(),
                request.getProductId()
        );

        return ReservationPageResponse.from(
                reservationService.getSearchReservations(userId, role, query, pageable)
                        .map(ReservationResponse::from)
        );
    }

    // 예약 상세 목록 조회
    @GetMapping("/{reservationId}")
    public ReservationDetailResponse getReservationDetail(
            @PathVariable UUID reservationId,
            @RequestHeader("X-User-Id") UUID userId,
            @RequestHeader("X-User-Role") String role
    ) {
        ReservationDetailResult result = reservationService.getReservationDetail(reservationId, userId, role);

        return ReservationDetailResponse.from(result);
    }

    // 예약 신청
    @PatchMapping("/{reservationId}")
    public ReservationCandidateResponse applyReservation(
            @PathVariable UUID reservationId,
            @RequestHeader("X-User-Id") UUID userId,
            @RequestHeader("X-User-Nickname") String nickname
    ) {
        ReservationApplyCommand command = ReservationApplyCommand.of(reservationId, userId, nickname);

        ReservationCandidateInfo info = reservationService.applyReservation(command);

        return ReservationCandidateResponse.of(info, "예약 신청이 성공적으로 완료되었습니다.");
    }

    // 구매자 사유 취소 (구매자/관리자)
    @PatchMapping("/{reservationId}/cancel/buyer")
    public ReservationStateResponse cancelByBuyer(
            @PathVariable UUID reservationId,
            @RequestBody ReservationCancelRequest request,
            @RequestHeader("X-User-Id") UUID userId,
            @RequestHeader("X-User-Role") String role
    ) {
        ReservationCancelCommand command = ReservationCancelCommand.of(
                reservationId,
                userId,
                role,
                request.reason()
        );

        ReservationStateInfo info = reservationService.cancelByBuyer(command);

        return ReservationStateResponse.of(info, "구매자 사유 취소가 완료되었습니다.");
    }

    // 결제 완료
    @PatchMapping("/{reservationId}/paymentconfirm")
    public ReservationStateResponse confirmPayment(
            @PathVariable UUID reservationId,
            @RequestHeader("X-User-Id") UUID userId,
            @RequestHeader("X-User-Role") String role
    ) {
        ReservationConfirmCommand command = ReservationConfirmCommand.of(
                reservationId,
                userId,
                role
        );

        ReservationStateInfo info = reservationService.confirmPayment(command);

        return ReservationStateResponse.of(info, "구매자의 결제 완료에 따라 예약 완료 처리가 완료되었습니다.");
    }

    // 판매자 사유로 인한 취소(판매자,관리자)
    @PatchMapping("/{reservationId}/cancel/seller")
    public ReservationStateResponse cancelBySeller(
            @PathVariable UUID reservationId,
            @RequestBody ReservationCancelRequest request,
            @RequestHeader("X-User-Id") UUID userId,
            @RequestHeader("X-User-Role") String role
    ) {
        ReservationCancelCommand command = ReservationCancelCommand.of(
                reservationId,
                userId,
                role,
                request.reason()
        );

        ReservationStateInfo info = reservationService.cancelBySeller(command);

        return ReservationStateResponse.of(info, "판매자 사유 취소가 완료되었습니다.");
    }

    // 예약 만료(관리자만 조정 가능)
    @PatchMapping("/{reservationId}/expire")
    public ReservationStateResponse expireByAdmin(
            @PathVariable UUID reservationId,
            @RequestBody ReservationAdminCancelRequest request,
            @RequestHeader("X-User-Id") UUID userId,
            @RequestHeader("X-User-Role") String role
    ) {
        ReservationExpireCommand command = ReservationExpireCommand.of(
                reservationId,
                userId,
                role,
                request
        );

        ReservationStateInfo info = reservationService.expireByAdmin(command);

        String message = (request.targetStatus() == ReservationStatus.CANCELLED_BY_BUYER)
                ? "관리자 권한으로 구매자 사유 취소(승계) 처리가 완료되었습니다."
                : "관리자 권한으로 판매자 사유 취소(종료) 처리가 완료되었습니다.";

        return ReservationStateResponse.of(info, message);
    }

    // 예약 완료
    @PatchMapping("/{reservationId}/complete")
    public ReservationStateResponse completeReservation(
            @PathVariable UUID reservationId,
            @RequestHeader("X-User-Id") UUID userId,
            @RequestHeader("X-User-Role") String role
    ) {

         ReservationConfirmCommand command = ReservationConfirmCommand.of(
                 reservationId,
                 userId,
                 role
         );

        ReservationStateInfo info = reservationService.completeReservation(command);

        return ReservationStateResponse.of(info, "판매자 채팅 수락에 따라 예약 완료 처리가 완료되었습니다.");
    }

    // 거래 완료
    @PatchMapping("/{reservationId}/tradeconfirm")
    public ReservationStateResponse confirmTrade(
            @PathVariable UUID reservationId,
            @RequestHeader("X-User-Id") UUID userId,
            @RequestHeader("X-User-Role") String role
    ) {
        ReservationStateInfo info = reservationService.confirmTrade(reservationId);

        return ReservationStateResponse.of(info, "거래가 성공적으로 완료되어 확정 처리되었습니다.");
    }
}