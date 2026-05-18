package org.pgsg.reservation.presentation.facade;

import lombok.RequiredArgsConstructor;
import org.pgsg.reservation.application.dto.command.ReservationApplyCommand;
import org.pgsg.reservation.application.dto.info.ReservationCandidateInfo;
import org.pgsg.reservation.application.service.ReservationService;
import org.pgsg.reservation.domain.exception.ReservationErrorCode;
import org.pgsg.reservation.domain.exception.ReservationException;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.stereotype.Component;

import java.util.concurrent.TimeUnit;

@Component
@RequiredArgsConstructor
public class ReservationFacade {

    private final RedissonClient redissonClient;
    private final ReservationService reservationService;

    public ReservationCandidateInfo applyReservation(ReservationApplyCommand command) {
        // 락의 키를 예약 자원 ID(reservationId) 단위로 설정하여 정밀하게 제어
        String lockKey = "lock:reservation:" + command.reservationId();
        RLock lock = redissonClient.getLock(lockKey);

        try {
            // waitTime: 10초 (락 획득을 위해 대기할 시간)
            // leaseTime: -1초 (-1로 설정하여 Watchdog 활성화)
            boolean available = lock.tryLock(10, -1, TimeUnit.SECONDS);

            if (!available) {
                // 락 획득 실패 시 커넥션을 소모하지 않고 바로 예외를 던져 Tomcat 스레드 블로킹 방지
                throw new ReservationException(ReservationErrorCode.RESERVATION_BUSY);
            }

            // 락을 안전하게 획득한 단 하나의 스레드만 실제 DB 트랜잭션 서비스로 진입
            return reservationService.proceedApplyTransaction(command);

        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new ReservationException(ReservationErrorCode.RESERVATION_INTERRUPTED);
        } finally {
            // 작업 완료 후 트랜잭션이 완전히 '커밋'된 시점에 안전하게 락을 해제
            if (lock.isHeldByCurrentThread()) {
                lock.unlock();
            }
        }
    }
}