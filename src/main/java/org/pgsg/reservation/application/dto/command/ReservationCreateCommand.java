package org.pgsg.reservation.application.dto.command;

import lombok.Builder;
import lombok.Getter;
import java.util.UUID;

@Getter
@Builder
public class ReservationCreateCommand {
    private UUID productId;
    private UUID buyerId;
    private String buyerNickname;
}