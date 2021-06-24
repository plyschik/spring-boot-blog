package io.plyschik.springbootblog.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.validator.constraints.Length;

import javax.validation.constraints.NotBlank;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class PostDto {
    @NotBlank
    @Length(min = 2, max = 120)
    private String title;

    @NotBlank
    @Length(min = 4, max = 65535)
    private String content;
}
