package org.pgsg.reservation.infrastructure.repository.reservation;

import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.core.types.dsl.PathBuilder;
import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.RequiredArgsConstructor;
import org.pgsg.reservation.domain.dto.ReservationSearchCriteria;
import org.pgsg.reservation.domain.model.reservation.Reservation;
import org.pgsg.reservation.domain.model.reservation.SearchPolicy;
import org.pgsg.reservation.domain.model.reservation.ReservationStatus;
import org.pgsg.reservation.domain.repository.ReservationRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
@RequiredArgsConstructor
public class ReservationRepositoryImpl implements ReservationRepository {

    private final JpaReservationRepository reservationJpaRepository;
    private final JPAQueryFactory queryFactory;

    private final PathBuilder<Reservation> reservation =
            new PathBuilder<>(Reservation.class, "reservation");

    @Override
    public Reservation save(Reservation reservationEntity) {
        return reservationJpaRepository.save(reservationEntity);
    }

    @Override
    public Reservation saveAndFlush(Reservation reservationEntity) {
        return reservationJpaRepository.saveAndFlush(reservationEntity);
    }

    @Override
    @Transactional(readOnly = true)
    public List<Reservation> findAllByStatusInAndModifiedAtBefore(
            List<ReservationStatus> statuses,
            LocalDateTime threshold
    ){
        return queryFactory
                .selectFrom(reservation)
                .where(
                        statusIn(statuses),
                        modifiedAtBefore(threshold)
                )
                .fetch();
    }

    @Override
    public Optional<Reservation> findById(UUID id) {
        return reservationJpaRepository.findById(id);
    }

    @Override
    public Page<Reservation> findByCriteria(
            ReservationSearchCriteria criteria,
            Pageable pageable
    ) {
        // 검색 조건 공통화
        BooleanExpression[] predicates = {
                applyPolicyFilter(criteria.policy()),
                statusEq(criteria.status()),
                productNameContains(criteria.productName()),
                productIdEq(criteria.productId()),
                sellerNameEq(criteria.sellerName()),
                buyerNameEq(criteria.buyerName())
        };

        List<Reservation> content = queryFactory
                .selectFrom(reservation)
                .where(predicates)
                .offset(pageable.getOffset())
                .limit(pageable.getPageSize())
                .orderBy(reservation.getDateTime("createdAt", java.time.LocalDateTime.class).desc())
                .fetch();

        Long total = queryFactory
                .select(reservation.count())
                .from(reservation)
                .where(predicates) // 정리된 조건 적용
                .fetchOne();

        return new PageImpl<>(content, pageable, total != null ? total : 0L);
    }

    private BooleanExpression applyPolicyFilter(SearchPolicy policy) {
        if (policy == null) return null;
        if (policy.isBuyerFilter()) {
            return reservation.get("buyerInfo").get("buyerId", UUID.class).eq(policy.accessUserId());
        }
        if (policy.isSellerFilter()) {
            return reservation.get("sellerInfo").get("sellerId", UUID.class).eq(policy.accessUserId());
        }
        return null;
    }

    private BooleanExpression statusEq(ReservationStatus status) {
        return status != null ? reservation.get("status", ReservationStatus.class).eq(status) : null;
    }

    private BooleanExpression productNameContains(String productName) {
        String normalized = productName == null ? null : productName.trim();
        return (normalized != null && !normalized.isBlank())
                ? reservation.get("productInfo").getString("productName").containsIgnoreCase(normalized)
                : null;
    }

    private BooleanExpression productIdEq(UUID productId) {
        return productId != null
                ? reservation.get("productInfo").get("productId", UUID.class).eq(productId)
                : null;
    }

    private BooleanExpression sellerNameEq(String sellerName) {
        String normalized = sellerName == null ? null : sellerName.trim();
        return (normalized != null && !normalized.isBlank())
                ? reservation.get("sellerInfo").getString("sellerName").containsIgnoreCase(normalized)
                : null;
    }

    private BooleanExpression buyerNameEq(String buyerName) {
        String normalized = buyerName == null ? null : buyerName.trim();
        return (normalized != null && !normalized.isBlank())
                ? reservation.get("buyerInfo").getString("buyerName").containsIgnoreCase(normalized)
                : null;
    }

    private BooleanExpression statusIn(List<ReservationStatus> statuses) {
        return statuses != null && !statuses.isEmpty()
                ? reservation.get("status", ReservationStatus.class).in(statuses)
                : null;
    }

    private BooleanExpression modifiedAtBefore(LocalDateTime threshold) {
        return threshold != null
                ? reservation.getDateTime("modifiedAt", LocalDateTime.class).before(threshold)
                : null;
    }
}