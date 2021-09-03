package io.plyschik.springbootblog.dto;

import lombok.*;

import java.util.List;

@Getter
@Setter
@Builder
public class ValidationErrorApiResponse {
    private List<ValidationError> errors;

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ValidationError {
        private String field;
        private String message;
    }
}
