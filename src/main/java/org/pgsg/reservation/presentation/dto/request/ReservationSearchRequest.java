package org.pgsg.reservation.presentation.dto.request;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.pgsg.reservation.domain.model.reservationcandidate.ReservationStatus;
import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
public class ReservationSearchRequest {
    private String sellerName;
    private String buyerName;
    private String productName;
    private ReservationStatus status;
    private UUID productId;
}