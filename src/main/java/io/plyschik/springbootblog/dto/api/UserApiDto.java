package io.plyschik.springbootblog.dto.api;

import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class UserApiDto {
    private String firstName;
    private String lastName;
}
