package io.plyschik.springbootblog.dto.api;

import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class PaginationApiDto {
    private int currentPage;
    private int totalPages;
    private boolean hasPreviousPage;
    private boolean hasNextPage;
}
