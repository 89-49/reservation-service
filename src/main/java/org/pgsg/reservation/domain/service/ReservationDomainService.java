package org.pgsg.reservation.domain.service;

import lombok.RequiredArgsConstructor;
import org.pgsg.reservation.domain.model.reservation.Reservation;
import org.pgsg.reservation.domain.model.reservation.BuyerInfo;
import org.pgsg.reservation.domain.model.reservation.SellerInfo;
import org.pgsg.reservation.domain.model.reservation.ProductInfo;
import org.springframework.stereotype.Service;


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
}