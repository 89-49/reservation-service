package org.pgsg.reservation.application.service;

import lombok.RequiredArgsConstructor;
import org.pgsg.reservation.application.dto.command.ReservationCreateCommand;
import org.pgsg.reservation.application.dto.result.ReservationCreateResult;
import org.pgsg.reservation.domain.model.reservation.*;
import org.pgsg.reservation.domain.service.ReservationDomainService;
import org.pgsg.reservation.domain.repository.ReservationRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class ReservationService {

    private final ReservationRepository reservationRepository;
    private final ReservationDomainService reservationDomainService;
    // private final ProductClient productClient; // 추후 구현 예정

    // 도메인 서비스 호출 전까지의 작업은 트랜잭션 밖으로 분리(추후 고도화 작업시)
    @Transactional
    public ReservationCreateResult createReservation(ReservationCreateCommand command) {

        // 구매자 정보(VO) 생성
        BuyerInfo buyer = BuyerInfo.of(command.getBuyerId(), command.getBuyerNickname());

        // 상품 및 판매자 정보 조회(추후 구현 예정)
        // ProductResponse productResponse = productClient.getProductDetails(command.getProductId());

        // 임시로 생성(추후 상품 판매 정보 조회시 수정 필요)
        SellerInfo seller = SellerInfo.of(
                UUID.randomUUID(), // productResponse.getSellerId()
                "임시 판매자" // productResponse.getSellerName()
        );
        ProductInfo product = ProductInfo.of(
                command.getProductId(),
                50000,             // productResponse.getPrice()
                "타임딜 특가 상품"    // productResponse.getName()
        );

        // 도메인 서비스 호출
        Reservation reservation = reservationDomainService.createReservation(buyer, seller, product);

        // ransaction outbox 패턴에 기반한 이벤트 발송 로직 추가 예정

        // DB 저장
        Reservation savedReservation = reservationRepository.save(reservation);

        // 5. [결과 반환] Result DTO로 변환하여 Controller로 전달
        return ReservationCreateResult.builder()
                .reservationId(savedReservation.getId())
                .status(savedReservation.getStatus().name())
                .build();
    }
}