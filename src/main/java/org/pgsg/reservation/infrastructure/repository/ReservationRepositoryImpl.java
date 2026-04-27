package org.pgsg.reservation.infrastructure.repository;

import com.querydsl.core.types.Projections;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.core.types.dsl.PathBuilder;
import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.RequiredArgsConstructor;
import org.pgsg.reservation.application.dto.query.ReservationSearchQuery;
import org.pgsg.reservation.application.dto.result.ReservationSearchResult;
import org.pgsg.reservation.domain.model.reservation.Reservation;
import org.pgsg.reservation.domain.model.reservation.SearchPolicy;
import org.pgsg.reservation.domain.model.reservation.ReservationStatus;
import org.pgsg.reservation.domain.repository.ReservationRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
@RequiredArgsConstructor
public class ReservationRepositoryImpl implements ReservationRepository {

    private final ReservationJpaRepository reservationJpaRepository;
    private final JPAQueryFactory queryFactory;

    private final PathBuilder<Reservation> reservation =
            new PathBuilder<>(Reservation.class, "reservation");

    @Override
    public Reservation save(Reservation reservationEntity) {
        return reservationJpaRepository.save(reservationEntity);
    }

    @Override
    public Optional<Reservation> findById(UUID id) {
        return reservationJpaRepository.findById(id);
    }

    @Override
    public Page<ReservationSearchResult> searchReservations(
            SearchPolicy policy,
            ReservationSearchQuery query,
            Pageable pageable
    ) {
        // 검색 조건 공통화
        BooleanExpression[] predicates = {
                applyPolicyFilter(policy),
                statusEq(query.status()),
                productNameContains(query.productName()),
                productIdEq(query.productId()),
                sellerNameEq(query.sellerName()),
                buyerNameEq(query.buyerName())
        };

        List<ReservationSearchResult> content = queryFactory
                .select(Projections.constructor(ReservationSearchResult.class,
                        reservation.get("id", UUID.class),
                        reservation.get("productInfo").get("productName", String.class),
                        reservation.get("sellerInfo").get("sellerName", String.class),
                        reservation.get("buyerInfo").get("buyerName", String.class),
                        reservation.get("status", ReservationStatus.class),
                        reservation.get("createdAt", java.time.LocalDateTime.class)
                ))
                .from(reservation)
                .where(predicates) // 정리된 조건 적용
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
                ? reservation.get("productInfo").getString("productName").containsIgnoreCase(productName)
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
                ? reservation.get("sellerInfo").getString("sellerName").containsIgnoreCase(sellerName)
                : null;
    }

    private BooleanExpression buyerNameEq(String buyerName) {
        String normalized = buyerName == null ? null : buyerName.trim();
        return (normalized != null && !normalized.isBlank())
                ? reservation.get("buyerInfo").getString("buyerName").containsIgnoreCase(buyerName)
                : null;
    }
}
