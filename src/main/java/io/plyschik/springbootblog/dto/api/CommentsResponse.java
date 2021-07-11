package io.plyschik.springbootblog.dto.api;

import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
public class CommentsResponse {
    private List<CommentApiDto> comments;
    private PaginationApiDto pagination;
}
