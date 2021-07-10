package io.plyschik.springbootblog.controller;

import io.plyschik.springbootblog.dto.Alert;
import io.plyschik.springbootblog.dto.ForgotPasswordDto;
import io.plyschik.springbootblog.dto.UserDto;
import io.plyschik.springbootblog.entity.User.Role;
import io.plyschik.springbootblog.exception.*;
import io.plyschik.springbootblog.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.context.MessageSource;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import javax.mail.MessagingException;
import javax.validation.Valid;

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
        } catch (MessagingException exception) {
            redirectAttributes.addFlashAttribute("alert", new Alert(
                "danger",
                messageSource.getMessage(
                    "message.something_went_wrong_try_again",
                    null,
                    LocaleContextHolder.getLocale()
                )
            ));

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

    @GetMapping("/verification/{token}")
    public ModelAndView accountActivation(@PathVariable String token, RedirectAttributes redirectAttributes) {
        try {
            userService.processAccountActivation(token);

            redirectAttributes.addFlashAttribute(
                "alert",
                new Alert(
                    "success",
                    messageSource.getMessage(
                        "message.account.verification.account_has_been_successfully_activated",
                        null,
                        LocaleContextHolder.getLocale()
                    )
                )
            );

            return new ModelAndView("redirect:/auth/signin");
        } catch (VerificationTokenNotFoundException exception) {
            redirectAttributes.addFlashAttribute(
                "alert",
                new Alert(
                    "danger",
                    messageSource.getMessage(
                        "message.account.verification.token_not_found",
                        null,
                        LocaleContextHolder.getLocale()
                    )
                )
            );

            return new ModelAndView("redirect:/");
        } catch (VerificationTokenExpiredException exception) {
            redirectAttributes.addFlashAttribute(
                "alert",
                new Alert(
                    "danger",
                    messageSource.getMessage(
                        "message.account.verification.token_expired",
                        null,
                        LocaleContextHolder.getLocale()
                    )
                )
            );

            return new ModelAndView("redirect:/");
        }
    }

    @GetMapping("/auth/forgot-password")
    public ModelAndView showPasswordResetRequestForm() {
        return new ModelAndView("auth/forgot_password", "forgotPassword", new ForgotPasswordDto());
    }

    @PostMapping("/auth/forgot-password")
    public ModelAndView processPasswordResetRequestForm(
        @Valid @ModelAttribute("forgotPassword") ForgotPasswordDto forgotPasswordDto,
        BindingResult bindingResult,
        RedirectAttributes redirectAttributes
    ) {
        if (bindingResult.hasErrors()) {
            return new ModelAndView("auth/forgot_password");
        }

        try {
            userService.processPasswordResetRequest(forgotPasswordDto);

            redirectAttributes.addFlashAttribute(
                "alert",
                new Alert(
                    "success",
                    messageSource.getMessage(
                        "message.password_reset_request_has_been_successfully_created",
                        null,
                        LocaleContextHolder.getLocale()
                    )
                )
            );

            return new ModelAndView("redirect:/auth/forgot-password");
        } catch (UserNotFoundException exception) {
            bindingResult.rejectValue(
                "email",
                "error.email",
                messageSource.getMessage(
                    "message.user_with_this_email_not_exists",
                    null,
                    LocaleContextHolder.getLocale()
                )
            );

            return new ModelAndView("auth/forgot_password");
        } catch (PasswordResetRequestHasBeenAlreadySentException exception) {
            redirectAttributes.addFlashAttribute(
                "alert",
                new Alert(
                    "danger",
                    messageSource.getMessage(
                        "message.password_reset_request_has_been_already_sent",
                        null,
                        LocaleContextHolder.getLocale()
                    )
                )
            );

            return new ModelAndView("redirect:/auth/forgot-password");
        } catch (MessagingException exception) {
            redirectAttributes.addFlashAttribute("alert", new Alert(
                "danger",
                messageSource.getMessage(
                    "message.something_went_wrong_try_again",
                    null,
                    LocaleContextHolder.getLocale()
                )
            ));

            return new ModelAndView("redirect:/auth/forgot-password");
        }
    }
}
