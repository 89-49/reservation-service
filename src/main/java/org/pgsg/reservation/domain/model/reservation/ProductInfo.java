package org.pgsg.reservation.domain.model.reservation;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import lombok.*;

import java.time.LocalDateTime;
import java.util.UUID;

@Embeddable
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(staticName = "of")
public class ProductInfo {
    @Column(nullable = false, unique = true)
    private UUID productId;
    private Integer productPrice;
    private String productName;
    private LocalDateTime endTime;
}