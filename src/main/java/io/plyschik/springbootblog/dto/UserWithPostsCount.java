package io.plyschik.springbootblog.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class UserWithPostsCount {
    private Long id;
    private String fullName;
    private Long postsCount;
}
