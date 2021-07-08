package io.plyschik.springbootblog.controller;

import io.plyschik.springbootblog.dto.UserDto;
import io.plyschik.springbootblog.entity.User.Role;
import io.plyschik.springbootblog.exception.EmailAddressIsAlreadyTakenException;
import io.plyschik.springbootblog.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.context.MessageSource;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import javax.validation.Valid;
import java.util.UUID;

@Controller
@RequiredArgsConstructor
public class AuthController {
    private final UserService userService;
    private final MessageSource messageSource;

    @GetMapping("/auth/signup")
    public ModelAndView signUpForm() {
        return new ModelAndView("auth/signup", "user", new UserDto());
    }

    @PostMapping("/auth/signup")
    public ModelAndView signUpFormProcess(
        @ModelAttribute("user") @Valid UserDto userDto,
        BindingResult bindingResult,
        RedirectAttributes redirectAttributes
    ) {
        if (bindingResult.hasErrors()) {
            return new ModelAndView("auth/signup");
        }

        try {
            userService.signUp(userDto, Role.USER);
        } catch (EmailAddressIsAlreadyTakenException exception) {
            bindingResult.rejectValue(
                "email",
                "error.email",
                messageSource.getMessage(
                    "message.email_is_already_taken",
                    null,
                    LocaleContextHolder.getLocale()
                )
            );

            return new ModelAndView("auth/signup");
        }

        redirectAttributes.addFlashAttribute(
            "message",
            messageSource.getMessage(
                "message.account_has_been_successfully_created",
                null,
                LocaleContextHolder.getLocale()
            )
        );

        return new ModelAndView("redirect:/auth/signin");
    }
}
