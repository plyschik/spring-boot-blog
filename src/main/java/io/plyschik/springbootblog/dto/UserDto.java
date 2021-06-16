package io.plyschik.springbootblog.dto;

import io.plyschik.springbootblog.validation.UniqueEmail;
import lombok.Data;
import org.hibernate.validator.constraints.Length;

import javax.validation.constraints.Email;
import javax.validation.constraints.NotBlank;

@Data
public class UserDto {
    @NotBlank
    @Email
    @UniqueEmail
    private String email;

    @NotBlank
    @Length(min = 4)
    private String password;

    @NotBlank
    @Length(min = 2, max = 30)
    private String firstName;

    @NotBlank
    @Length(min = 2, max = 30)
    private String lastName;
}
