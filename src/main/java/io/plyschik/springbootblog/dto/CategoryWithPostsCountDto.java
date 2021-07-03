package io.plyschik.springbootblog.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class CategoryWithPostsCountDto {
    private Long id;
    private String name;
    Long postsCount;
}
