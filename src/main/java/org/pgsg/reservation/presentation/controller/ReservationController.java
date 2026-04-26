package org.pgsg.reservation.presentation.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.pgsg.reservation.application.dto.command.ReservationCreateCommand;
import org.pgsg.reservation.application.dto.query.ReservationSearchQuery;
import org.pgsg.reservation.application.dto.result.ReservationCreateResult;
import org.pgsg.reservation.application.service.ReservationService;
import org.pgsg.reservation.presentation.dto.request.ReservationCreateRequest;
import org.pgsg.reservation.presentation.dto.request.ReservationSearchRequest;
import org.pgsg.reservation.presentation.dto.response.ReservationResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
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

        // 3. 통일된 응답 규격으로 반환 (조회는 200 OK)
        return ResponseEntity
                .status(HttpStatus.OK)
                .body(responses);
    }
}
