package io.plyschik.springbootblog;

import io.plyschik.springbootblog.dto.ValidationErrorApiResponse;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Function;
import java.util.function.Predicate;

@RestControllerAdvice
public class RestGlobalExceptionHandler {
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ValidationErrorApiResponse> handleValidationErrorException(
        MethodArgumentNotValidException exception
    ) {
        List<ValidationErrorApiResponse.ValidationError> errors = new ArrayList<>();

        exception.getBindingResult().getFieldErrors().stream()
            .filter(distinctByKey(FieldError::getField))
            .forEach(error -> errors.add(new ValidationErrorApiResponse.ValidationError(
                error.getField(),
                error.getDefaultMessage()
            )));

        ValidationErrorApiResponse response = ValidationErrorApiResponse.builder()
            .errors(errors)
            .build();

        return ResponseEntity.badRequest().body(response);
    }

    private static <T> Predicate<T> distinctByKey(Function<? super T, ?> keyExtractor) {
        Set<Object> seen = ConcurrentHashMap.newKeySet();

        return t -> seen.add(keyExtractor.apply(t));
    }
}
