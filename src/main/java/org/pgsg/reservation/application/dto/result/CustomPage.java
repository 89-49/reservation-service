package org.pgsg.reservation.application.dto.result;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.data.domain.Page;

import java.util.List;
import java.util.function.Function;
import java.util.stream.Collectors;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class CustomPage<T> {
    private List<T> content;
    private int pageNumber;
    private int pageSize;
    private long totalElements;

    public static <T> CustomPage<T> from(Page<T> page) {
        return new CustomPage<>(
                page.getContent(),
                page.getNumber(),
                page.getSize(),
                page.getTotalElements()
        );
    }

    public <U> CustomPage<U> map(Function<? super T, ? extends U> converter) {
        List<U> convertedContent = this.content.stream()
                .map(converter)
                .collect(Collectors.toList());
        return new CustomPage<>(convertedContent, this.pageNumber, this.pageSize, this.totalElements);
    }
}