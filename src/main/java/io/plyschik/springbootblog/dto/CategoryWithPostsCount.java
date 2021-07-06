package io.plyschik.springbootblog.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CategoryWithPostsCount {
    private Long id;
    private String name;
    private Long postsCount;
}
