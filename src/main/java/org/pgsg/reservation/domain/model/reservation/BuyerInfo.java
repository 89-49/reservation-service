package org.pgsg.reservation.domain.model.reservation;

import jakarta.persistence.Embeddable;
import lombok.*;
import java.util.UUID;

@Embeddable
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(staticName = "of")
public class BuyerInfo {
    private UUID buyerId;
    private String buyerName;
}