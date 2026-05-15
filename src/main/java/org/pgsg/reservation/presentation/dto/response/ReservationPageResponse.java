package org.pgsg.reservation.presentation.dto.response;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import org.pgsg.reservation.application.dto.result.CustomPage;
import java.util.List;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED, force = true)
@AllArgsConstructor
public class ReservationPageResponse<T> {
    private final List<T> content;
    private final int pageNumber;
    private final long totalElements;
    private final int totalPages;

    private ReservationPageResponse(CustomPage<T> page) {
        this.content = page.getContent();
        this.pageNumber = page.getPageNumber();
        this.totalElements = page.getTotalElements();
        this.totalPages = (int) Math.ceil((double) page.getTotalElements() / page.getPageSize());
    }

    public static <T> ReservationPageResponse<T> from(CustomPage<T> page) {
        return new ReservationPageResponse<>(page);
    }
}