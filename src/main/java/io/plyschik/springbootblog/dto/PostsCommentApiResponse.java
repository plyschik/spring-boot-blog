package io.plyschik.springbootblog.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.List;

@Getter
@Setter
@Builder
public class PostsCommentApiResponse {
    private List<Comment> comments;
    private Pagination pagination;

    @Getter
    @Setter
    public static class Comment {
        private long id;
        private String content;

        @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
        private LocalDateTime createdAt;

        private User user;
        private boolean canEdit;
        private boolean canDelete;
    }

    @Getter
    @Setter
    public static class User {
        private String firstName;
        private String lastName;
    }

    @Getter
    @Setter
    @Builder
    public static class Pagination {
        private int currentPage;
        private int totalPages;
        private int numberOfElements;
        private long totalElements;
        private int pageSize;
        private boolean hasPreviousPage;
        private boolean hasNextPage;
    }
}
