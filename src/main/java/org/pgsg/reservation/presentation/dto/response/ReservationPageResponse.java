package org.pgsg.reservation.presentation.dto.response;

import lombok.Getter;
import org.springframework.data.domain.Page;
import java.util.List;

@Getter
public class ReservationPageResponse<T> {
    private final List<T> content;
    private final int pageNumber;
    private final long totalElements;
    private final int totalPages;

    private ReservationPageResponse(Page<T> page) {
        this.content = page.getContent();
        this.pageNumber = page.getNumber();
        this.totalElements = page.getTotalElements();
        this.totalPages = page.getTotalPages();
    }

    public static <T> ReservationPageResponse<T> from(Page<T> page) {
        return new ReservationPageResponse<>(page);
    }
}