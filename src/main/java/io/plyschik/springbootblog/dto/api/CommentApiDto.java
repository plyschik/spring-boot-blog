package io.plyschik.springbootblog.dto.api;

import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
public class CommentApiDto {
    private Long id;
    private String content;
    private LocalDateTime createdAt;
    private UserApiDto user;
    private boolean canEdit;
    private boolean canDelete;
}
