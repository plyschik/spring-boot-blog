package io.plyschik.springbootblog.dto;

import lombok.Data;
import org.hibernate.validator.constraints.Length;

import javax.validation.constraints.NotBlank;

@Data
public class TagDto {
    @NotBlank
    @Length(min = 2, max = 30)
    private String name;
}
