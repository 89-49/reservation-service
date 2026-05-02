package org.pgsg.reservation.presentation.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.pgsg.config.security.UserDetailsImpl;
import org.pgsg.reservation.application.dto.command.ReservationCancelCommand;
import org.pgsg.reservation.application.dto.command.ReservationCreateCommand;
import org.pgsg.reservation.application.dto.info.ReservationCancelInfo;
import org.pgsg.reservation.application.dto.query.ReservationSearchQuery;
import org.pgsg.reservation.application.dto.result.ReservationCreateResult;
import org.pgsg.reservation.application.service.ReservationService;
import org.pgsg.reservation.domain.model.reservation.ReservationStatus;
import org.pgsg.reservation.presentation.dto.request.ReservationAdminCancelRequest;
import org.pgsg.reservation.presentation.dto.request.ReservationCancelRequest;
import org.pgsg.reservation.presentation.dto.request.ReservationCreateRequest;
import org.pgsg.reservation.presentation.dto.request.ReservationSearchRequest;
import org.pgsg.reservation.presentation.dto.response.ReservationCancelResponse;
import org.pgsg.reservation.presentation.dto.response.ReservationCandidateResponse;
import org.pgsg.reservation.presentation.dto.response.ReservationDetailResponse;
import org.pgsg.reservation.presentation.dto.response.ReservationResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/reservations")
public class ReservationController {

    private final ReservationService reservationService;

    // 예약 생성
    @PostMapping
    public ResponseEntity<ReservationResponse> createReservation(
            @Valid @RequestBody ReservationCreateRequest request,
            @RequestHeader(value = "X-User-Id") UUID userId,
            @RequestHeader(value = "X-User-Nickname") String nickname
    ) {

        ReservationCreateCommand command = ReservationCreateCommand.builder()
                .productId(request.getProductId())
                .buyerId(userId)
                .buyerNickname(nickname)
                .build();

        ReservationCreateResult result = reservationService.createReservation(command);

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(ReservationResponse.from(result));
    }

    // 예약 목록 조회
    @GetMapping
    public ResponseEntity<Page<ReservationResponse>> getReservations(
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

        Page<ReservationResponse> responses = reservationService.getSearchReservations(userId, role, query, pageable)
                .map(ReservationResponse::from);

        // 통일된 응답 규격으로 반환 (조회는 200 OK)
        return ResponseEntity
                .status(HttpStatus.OK)
                .body(responses);
    }

    // 예약 상세 목록 조회
    @GetMapping("/{reservationId}")
    public ResponseEntity<ReservationDetailResponse> getReservationDetail(
            @PathVariable UUID reservationId,
            @RequestHeader("X-User-Id") UUID userId,
            @RequestHeader("X-User-Role") String role
    ) {

        ReservationDetailResponse response = reservationService.getReservationDetail(reservationId, userId, role);

        return ResponseEntity.ok(response);
    }

    // 예약 신청
    @PostMapping("/{reservationId}")
    public ResponseEntity<ReservationCandidateResponse> applyReservation(
            @PathVariable UUID reservationId,
            @RequestHeader("X-User-Id") UUID userId,
            @RequestHeader("X-User-Nickname") String nickname
    ) {
        ReservationCandidateResponse response = reservationService.applyReservation(reservationId, userId, nickname);

        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    // 구매자 사유 취소 (구매자/관리자)
    @PatchMapping("/{reservationId}/cancel/buyer")
    public ResponseEntity<ReservationCancelResponse> cancelByBuyer(
            @PathVariable UUID reservationId,
            @RequestBody ReservationCancelRequest request,
            @AuthenticationPrincipal UserDetailsImpl userDetails // AuthUser를 UserDetailsImpl로 통일
    ) {
        ReservationCancelCommand command = ReservationCancelCommand.of(
                reservationId,
                userDetails.getUuid(),
                userDetails.getUserRole(),
                request.reason()
        );

        ReservationCancelInfo info = reservationService.cancelByBuyer(command);
        ReservationCancelResponse response = ReservationCancelResponse.of(info, "구매자 사유 취소가 완료되었습니다.");

        return ResponseEntity.status(HttpStatus.OK).body(response);
    }

    // 판매자 사유로 인한 취소(판매자,관리자)
    @PatchMapping("/{reservationId}/cancel/seller")
    public ResponseEntity<ReservationCancelResponse> cancelBySeller(
            @PathVariable UUID reservationId,
            @RequestBody ReservationCancelRequest request,
            @AuthenticationPrincipal UserDetailsImpl userDetails
    ) {
        ReservationCancelCommand command = ReservationCancelCommand.of(
                reservationId,
                userDetails.getUuid(),
                userDetails.getUserRole(),
                request.reason()
        );

        ReservationCancelInfo info = reservationService.cancelBySeller(command);
        ReservationCancelResponse response = ReservationCancelResponse.of(info, "판매자 사유 취소가 완료되었습니다.");

        return ResponseEntity.status(HttpStatus.OK).body(response);
    }

    // 예약 만료(관리자만 조정 가능)
    @PatchMapping("/{reservationId}/expire")
    public ResponseEntity<ReservationCancelResponse> expireByAdmin(
            @PathVariable UUID reservationId,
            @RequestBody ReservationAdminCancelRequest request,
            @AuthenticationPrincipal UserDetailsImpl userDetails
    ) {
        ReservationCancelInfo info = reservationService.expireByAdmin(
                reservationId,
                request,
                userDetails.getUuid(),
                userDetails.getUserRole()
        );

        String message = (request.targetStatus() == ReservationStatus.CANCELLED_BY_BUYER)
                ? "관리자 권한으로 구매자 사유 취소(승계) 처리가 완료되었습니다."
                : "관리자 권한으로 판매자 사유 취소(종료) 처리가 완료되었습니다.";

        return ResponseEntity.ok(ReservationCancelResponse.of(info, message));
    }

    // 예약 완료
    @PatchMapping("/{reservationId}/complete")
    public ResponseEntity<ReservationCancelResponse> completeReservation(
            @PathVariable UUID reservationId,
            @RequestHeader("X-User-Id") UUID userId,
            @RequestHeader("X-User-Role") String role
    ) {
        ReservationCancelInfo info = reservationService.completeReservation(
                reservationId,
                userId,
                role
        );

        String message = "판매자 채팅 수락에 따라 예약 완료 처리가 완료되었습니다.";

        return ResponseEntity.ok(ReservationCancelResponse.of(info, message));
    }
}
