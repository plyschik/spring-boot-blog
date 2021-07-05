package io.plyschik.springbootblog.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CategoryWithPostsCountDto {
    private Long id;
    private String name;
    Long postsCount;
}
