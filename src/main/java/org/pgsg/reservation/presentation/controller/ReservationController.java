package org.pgsg.reservation.presentation.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.pgsg.reservation.application.dto.command.ReservationCreateCommand;
import org.pgsg.reservation.application.dto.result.ReservationCreateResult;
import org.pgsg.reservation.application.service.ReservationService;
import org.pgsg.reservation.presentation.dto.request.ReservationCreateRequest;
import org.pgsg.reservation.presentation.dto.response.ReservationResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/reservations")
public class ReservationController {

    private final ReservationService reservationService;

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
}
