package io.plyschik.springbootblog.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class PostWithRelationshipsCount {
    private Long id;
    private String title;
    private boolean published;
    private String author;
    private String category;
    private Long tagsCount;
    private Long commentsCount;
    private LocalDateTime createdAt;
}
