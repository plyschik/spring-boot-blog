package io.plyschik.springbootblog.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.validator.constraints.Length;

import javax.validation.constraints.Email;
import javax.validation.constraints.NotBlank;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class UserDto {
    @NotBlank
    @Email
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
