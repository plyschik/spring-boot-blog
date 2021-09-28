package io.plyschik.springbootblog;

import io.plyschik.springbootblog.dto.Alert;
import io.plyschik.springbootblog.dto.ValidationErrorApiResponse;
import lombok.RequiredArgsConstructor;
import org.hibernate.QueryException;
import org.springframework.context.MessageSource;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Function;
import java.util.function.Predicate;

@ControllerAdvice
@RequiredArgsConstructor
public class GlobalExceptionHandler {
    private final MessageSource messageSource;

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

    @ExceptionHandler({
        MethodArgumentTypeMismatchException.class,
        QueryException.class
    })
    public ModelAndView handleHibernateQueryException(RedirectAttributes redirectAttributes) {
        redirectAttributes.addFlashAttribute(
            "alert",
            new Alert("danger", messageSource.getMessage(
                "message.unexpected_error_occurred",
                null,
                LocaleContextHolder.getLocale()
            ))
        );

        return new ModelAndView("redirect:/");
    }

    private static <T> Predicate<T> distinctByKey(Function<? super T, ?> keyExtractor) {
        Set<Object> seen = ConcurrentHashMap.newKeySet();

        return t -> seen.add(keyExtractor.apply(t));
    }
}
