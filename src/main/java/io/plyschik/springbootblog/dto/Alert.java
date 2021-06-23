package io.plyschik.springbootblog.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
public class Alert {
    private String type;
    private String message;
}
