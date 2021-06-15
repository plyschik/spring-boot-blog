package io.plyschik.springbootblog.controller;

import io.plyschik.springbootblog.dto.UserDto;
import io.plyschik.springbootblog.service.UserService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import javax.validation.Valid;

@Controller
public class AuthController {
    private final UserService userService;

    public AuthController(UserService userService) {
        this.userService = userService;
    }

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

        userService.signUp(userDto);

        redirectAttributes.addFlashAttribute(
            "message",
            "User account has been successfully created! Now you can sign in."
        );

        return "redirect:/auth/signin";
    }
}
