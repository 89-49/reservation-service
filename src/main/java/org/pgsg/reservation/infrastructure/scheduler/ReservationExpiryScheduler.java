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
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime threshold = now.minusMinutes(60); // 1시간 경과 기준
        List<ReservationStatus> targetStatuses = List.of(ReservationStatus.AVAILABLE,ReservationStatus.PENDING, ReservationStatus.PAID);

        // 1시간 경과했거나,endTime이 지났거나 둘 중 하나라도 해당되는 데이터 조회
        List<Reservation> expiredReservations = reservationRepository.findAllByStatusInAndModifiedAtBeforeOrProductInfoEndTimeBefore(
                targetStatuses,
                threshold,
                now
        );

        if (expiredReservations.isEmpty()) return;

        log.info("시스템 자동 만료 대상 발견: {}건", expiredReservations.size());

        for (Reservation reservation : expiredReservations) {
            try {
                // 종료 사유를 판단하여 커맨드 생성
                ReservationExpireCommand command = createExpireCommand(reservation, now);
                reservationService.expireByAdmin(command);

                log.info("예약 자동 종료 완료 (ID: {}, 종료사유: {})", reservation.getId(), command.reason());
            } catch (Exception e) {
                log.error("예약 자동 만료 처리 중 오류 발생 (ID: {}): {}", reservation.getId(), e.getMessage());
            }
        }
    }

    /**
     * 예약 상태에 따른 적절한 취소 요청 객체를 생성하는 메서드
     */
    private ReservationExpireCommand createExpireCommand(Reservation reservation, LocalDateTime now) {
        ReservationStatus targetStatus;
        String reason;

        // endTime이 먼저 지났는지 확인
        boolean isEndTimeExpired = reservation.getProductInfo().getEndTime().isBefore(now);

        if (isEndTimeExpired) {
            targetStatus = (reservation.getStatus() == ReservationStatus.PENDING)
                    ? ReservationStatus.CANCELLED_BY_BUYER : ReservationStatus.CANCELLED_BY_SELLER;
            reason = "타임딜 종료 시간 도달로 인한 자동 종료";
        } else {
            // 1시간 경과로 인한 종료인 경우
            if (reservation.getStatus() == ReservationStatus.PENDING) {
                targetStatus = ReservationStatus.CANCELLED_BY_BUYER;
                reason = "결제 제한 시간(1시간) 초과로 인한 자동 취소";
            } else {
                targetStatus = ReservationStatus.CANCELLED_BY_SELLER;
                reason = "채팅 수락 제한 시간(1시간) 초과로 인한 자동 취소";
            }
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