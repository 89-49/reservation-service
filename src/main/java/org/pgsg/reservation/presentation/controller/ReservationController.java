package org.pgsg.reservation.presentation.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.pgsg.reservation.application.dto.command.ReservationCancelCommand;
import org.pgsg.reservation.application.dto.command.ReservationCreateCommand;
import org.pgsg.reservation.application.dto.info.ReservationCancelInfo;
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
        ReservationCreateCommand command = ReservationCreateCommand.builder()
                .productId(request.getProductId())
                .productName(request.getProductName())
                .price(request.getPrice())
                .endTime(request.getEndTime())
                .sellerId(request.getSellerId())
                .sellerName(request.getSellerNickname())
                .build();

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
    @PostMapping("/{reservationId}")
    public ReservationCandidateResponse applyReservation(
            @PathVariable UUID reservationId,
            @RequestHeader("X-User-Id") UUID userId,
            @RequestHeader("X-User-Nickname") String nickname
    ) {
        return reservationService.applyReservation(reservationId, userId, nickname);
    }

    // 구매자 사유 취소 (구매자/관리자)
    @PatchMapping("/{reservationId}/cancel/buyer")
    public ReservationCancelResponse cancelByBuyer(
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

        ReservationCancelInfo info = reservationService.cancelByBuyer(command);

        return ReservationCancelResponse.of(info, "구매자 사유 취소가 완료되었습니다.");
    }

    // 결제 완료
    @PatchMapping("/{reservationId}/paymentconfirm")
    public ReservationCancelInfo confirmPayment(
            @PathVariable UUID reservationId,
            @RequestHeader("X-User-Id") UUID userId,
            @RequestHeader("X-User-Role") String role
    ) {
        return reservationService.confirmPayment(
                reservationId,
                userId,
                role
        );
    }

    // 판매자 사유로 인한 취소(판매자,관리자)
    @PatchMapping("/{reservationId}/cancel/seller")
    public ReservationCancelResponse cancelBySeller(
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

        ReservationCancelInfo info = reservationService.cancelBySeller(command);

        return ReservationCancelResponse.of(info, "판매자 사유 취소가 완료되었습니다.");
    }

    // 예약 만료(관리자만 조정 가능)
    @PatchMapping("/{reservationId}/expire")
    public ReservationCancelResponse expireByAdmin(
            @PathVariable UUID reservationId,
            @RequestBody ReservationAdminCancelRequest request,
            @RequestHeader("X-User-Id") UUID userId,
            @RequestHeader("X-User-Role") String role
    ) {
        ReservationCancelInfo info = reservationService.expireByAdmin(
                reservationId,
                request,
                userId,
                role
        );

        String message = (request.targetStatus() == ReservationStatus.CANCELLED_BY_BUYER)
                ? "관리자 권한으로 구매자 사유 취소(승계) 처리가 완료되었습니다."
                : "관리자 권한으로 판매자 사유 취소(종료) 처리가 완료되었습니다.";

        return ReservationCancelResponse.of(info, message);
    }

    // 예약 완료
    @PatchMapping("/{reservationId}/complete")
    public ReservationCancelResponse completeReservation(
            @PathVariable UUID reservationId,
            @RequestHeader("X-User-Id") UUID userId,
            @RequestHeader("X-User-Role") String role
    ) {
        ReservationCancelInfo info = reservationService.completeReservation(
                reservationId,
                userId,
                role
        );

        return ReservationCancelResponse.of(info, "판매자 채팅 수락에 따라 예약 완료 처리가 완료되었습니다.");
    }

    // 거래 완료
    @PatchMapping("/{reservationId}/tradeconfirm")
    public ReservationCancelResponse confirmTrade(
            @PathVariable UUID reservationId,
            @RequestHeader("X-User-Id") UUID userId,
            @RequestHeader("X-User-Role") String role
    ) {
        ReservationCancelInfo info = reservationService.confirmTrade(
                reservationId
        );

        return ReservationCancelResponse.of(info, "거래가 성공적으로 완료되어 확정 처리되었습니다.");
    }
}