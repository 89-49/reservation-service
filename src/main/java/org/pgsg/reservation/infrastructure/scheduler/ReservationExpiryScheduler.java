package org.pgsg.reservation.infrastructure.scheduler;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.pgsg.reservation.application.dto.command.ReservationExpireCommand;
import org.pgsg.reservation.application.service.ReservationService;
import org.pgsg.reservation.domain.model.reservation.Reservation;
import org.pgsg.reservation.domain.model.reservation.ReservationStatus;
import org.pgsg.reservation.domain.repository.ReservationRepository;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Slf4j
@Component
@RequiredArgsConstructor
public class ReservationExpiryScheduler {

    private final ReservationService reservationService;
    private final ReservationRepository reservationRepository;

    // 시스템 자동 처리를 위한 가상의 관리자 정보
    private static final UUID SYSTEM_ID = UUID.fromString("00000000-0000-0000-0000-000000000000");
    private static final String SYSTEM_ROLE = "ADMIN";

    /**
     * 매 1분마다 만료된 예약을 체크하여 자동 취소 처리
     */
    @Scheduled(cron = "0 * * * * *")
    public void autoExpireReservations() {
        LocalDateTime threshold = LocalDateTime.now().minusMinutes(60);
        List<ReservationStatus> targetStatuses = List.of(ReservationStatus.PENDING, ReservationStatus.PAID);

        // 1시간 동안 상태 변화가 없는 예약들 조회
        List<Reservation> expiredReservations = reservationRepository.findAllByStatusInAndModifiedAtBefore(
                targetStatuses,
                threshold
        );

        if (expiredReservations.isEmpty()) return;

        log.info("시스템 자동 만료 처리 시작: {}건", expiredReservations.size());

        for (Reservation reservation : expiredReservations) {
            try {
                ReservationExpireCommand command = createExpireCommand(reservation);
                reservationService.expireByAdmin(command);

                log.info("예약 자동 만료 완료 (ID: {}, 상태: {})", reservation.getId(), reservation.getStatus());
            } catch (Exception e) {
                log.error("예약 자동 만료 처리 중 오류 발생 (ID: {}): {}", reservation.getId(), e.getMessage());
            }
        }
    }

    /**
     * 예약 상태에 따른 적절한 취소 요청 객체를 생성하는 메서드
     */
    private ReservationExpireCommand createExpireCommand(Reservation reservation) {
        ReservationStatus targetStatus;
        String reason;

        if (reservation.getStatus() == ReservationStatus.PENDING) {
            targetStatus = ReservationStatus.CANCELLED_BY_BUYER;
            reason = "결제 제한 시간(1시간) 초과로 인한 자동 취소";
        } else if (reservation.getStatus() == ReservationStatus.PAID) {
            targetStatus = ReservationStatus.CANCELLED_BY_SELLER;
            reason = "채팅 수락 제한 시간(1시간) 초과로 인한 자동 취소";
        } else {
            throw new IllegalStateException("자동 만료 대상이 아닌 상태입니다: " + reservation.getStatus());
        }

        return new ReservationExpireCommand(
                reservation.getId(),
                SYSTEM_ID,
                SYSTEM_ROLE,
                targetStatus,
                reason
        );
    }
}