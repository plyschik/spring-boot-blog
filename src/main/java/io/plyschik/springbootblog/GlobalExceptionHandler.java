package io.plyschik.springbootblog;

import io.plyschik.springbootblog.dto.Alert;
import lombok.RequiredArgsConstructor;
import org.hibernate.QueryException;
import org.springframework.context.MessageSource;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@ControllerAdvice
@RequiredArgsConstructor
public class GlobalExceptionHandler {
    private final MessageSource messageSource;

    @ExceptionHandler(QueryException.class)
    public String handleHibernateQueryException(RedirectAttributes redirectAttributes) {
        redirectAttributes.addFlashAttribute(
            "alert",
            new Alert("danger", messageSource.getMessage(
                "message.unexpected_error_occurred",
                null,
                LocaleContextHolder.getLocale()
            ))
        );

        return "redirect:/";
    }
}
