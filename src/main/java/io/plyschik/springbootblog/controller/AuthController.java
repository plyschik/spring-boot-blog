package io.plyschik.springbootblog.controller;

import io.plyschik.springbootblog.dto.UserDto;
import io.plyschik.springbootblog.entity.Role;
import io.plyschik.springbootblog.exception.EmailAddressIsAlreadyTaken;
import io.plyschik.springbootblog.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.context.MessageSource;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import javax.validation.Valid;

@Controller
@RequiredArgsConstructor
public class AuthController {
    private final UserService userService;
    private final MessageSource messageSource;

    @GetMapping("/auth/signup")
    public String signUpForm(Model model) {
        model.addAttribute("user", new UserDto());

        return "auth/signup";
    }

    @PostMapping("/auth/signup")
    public String signUpFormProcess(
        @ModelAttribute("user") @Valid UserDto userDto,
        BindingResult bindingResult,
        RedirectAttributes redirectAttributes
    ) {
        if (bindingResult.hasErrors()) {
            return "auth/signup";
        }

        try {
            userService.signUp(userDto, Role.USER);
        } catch (EmailAddressIsAlreadyTaken exception) {
            bindingResult.rejectValue(
                "email",
                "error.email",
                messageSource.getMessage(
                    "message.email_is_already_taken",
                    null,
                    LocaleContextHolder.getLocale()
                )
            );

            return "auth/signup";
        }

        redirectAttributes.addFlashAttribute(
            "message",
            messageSource.getMessage(
                "message.account_has_been_successfully_created",
                null,
                LocaleContextHolder.getLocale()
            )
        );

        return "redirect:/auth/signin";
    }
}
