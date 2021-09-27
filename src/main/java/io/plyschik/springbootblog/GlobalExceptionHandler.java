package io.plyschik.springbootblog;

import io.plyschik.springbootblog.dto.Alert;
import io.plyschik.springbootblog.exception.CategoryNotFoundException;
import io.plyschik.springbootblog.exception.PostNotFoundException;
import io.plyschik.springbootblog.exception.TagNotFoundException;
import io.plyschik.springbootblog.exception.UserNotFoundException;
import lombok.RequiredArgsConstructor;
import org.hibernate.QueryException;
import org.springframework.context.MessageSource;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@ControllerAdvice
@RequiredArgsConstructor
public class GlobalExceptionHandler {
    private final MessageSource messageSource;

    @ExceptionHandler(QueryException.class)
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

    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public ModelAndView handleInvalidRequestException() {
        return new ModelAndView("errors/invalid_request");
    }

    @ExceptionHandler({
        UserNotFoundException.class,
        PostNotFoundException.class,
        CategoryNotFoundException.class,
        TagNotFoundException.class
    })
    public ModelAndView handleResourceNotFoundException(RuntimeException exception) {
        return new ModelAndView("errors/resource_not_found")
            .addObject("exception", exception);
    }
}
