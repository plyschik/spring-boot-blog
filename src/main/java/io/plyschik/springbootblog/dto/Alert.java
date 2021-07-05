package io.plyschik.springbootblog.dto;

import lombok.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class Alert {
    private String type;
    private String message;
}
