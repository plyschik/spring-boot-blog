package io.plyschik.springbootblog.controller;

import io.plyschik.springbootblog.dto.UserDto;
import io.plyschik.springbootblog.service.UserService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import javax.transaction.Transactional;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
class AuthControllerIntegrationTest {
    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private UserService userService;

    @Test
    public void shouldReturnSignUpForm() throws Exception {
        mockMvc.perform(get("/auth/signup"))
            .andExpect(status().isOk())
            .andExpect(view().name("auth/signup"))
            .andExpect(model().attributeExists("user"));
    }

    @Test
    public void shouldReturnFormValidationErrorsWhenAllFieldsAreBlank() throws Exception {
        mockMvc.perform(post("/auth/signup")
            .contentType(MediaType.APPLICATION_FORM_URLENCODED)
            .with(csrf())
            .param("email", "")
            .param("password", "")
            .param("firstName", "")
            .param("lastName", "")
            .sessionAttr("user", new UserDto())
        )
            .andExpect(status().isOk())
            .andExpect(view().name("auth/signup"))
            .andExpect(model().attributeHasFieldErrors("user", "email"))
            .andExpect(model().attributeHasFieldErrors("user", "password"))
            .andExpect(model().attributeHasFieldErrors("user", "firstName"))
            .andExpect(model().attributeHasFieldErrors("user", "lastName"));
    }

    @Test
    public void shouldReturnEmailValidationErrorWhenEmailIsInvalid() throws Exception {
        mockMvc.perform(post("/auth/signup")
            .contentType(MediaType.APPLICATION_FORM_URLENCODED)
            .with(csrf())
            .param("email", "test")
            .sessionAttr("user", new UserDto())
        )
            .andExpect(status().isOk())
            .andExpect(view().name("auth/signup"))
            .andExpect(model().attributeHasFieldErrors("user", "email"));
    }

    @Test
    public void shouldReturnEmailValidationErrorWhenEmailIsAlreadyTaken() throws Exception {
        userService.signUp(new UserDto("john.doe@sbb.net", "password", "John", "Doe"));

        mockMvc.perform(post("/auth/signup")
            .contentType(MediaType.APPLICATION_FORM_URLENCODED)
            .with(csrf())
            .param("email", "john.doe@sbb.net")
            .param("password", "password")
            .param("firstName", "John")
            .param("lastName", "Doe")
            .sessionAttr("user", new UserDto())
        )
            .andExpect(status().isOk())
            .andExpect(view().name("auth/signup"))
            .andExpect(model().attributeHasFieldErrors("user", "email"));
    }

    @Test
    public void shouldReturnPasswordValidationErrorWhenPasswordLengthIsLessThan4() throws Exception {
        mockMvc.perform(post("/auth/signup")
            .contentType(MediaType.APPLICATION_FORM_URLENCODED)
            .with(csrf())
            .param("password", "abc")
            .sessionAttr("user", new UserDto())
        )
            .andExpect(status().isOk())
            .andExpect(view().name("auth/signup"))
            .andExpect(model().attributeHasFieldErrors("user", "password"));
    }

    @Test
    public void shouldReturnFirstNameValidationErrorWhenFirstNameLengthIsLessThan2() throws Exception {
        mockMvc.perform(post("/auth/signup")
            .contentType(MediaType.APPLICATION_FORM_URLENCODED)
            .with(csrf())
            .param("firstName", "a")
            .sessionAttr("user", new UserDto())
        )
            .andExpect(status().isOk())
            .andExpect(view().name("auth/signup"))
            .andExpect(model().attributeHasFieldErrors("user", "firstName"));
    }

    @Test
    public void shouldReturnFirstNameValidationErrorWhenFirstNameLengthIsGreaterThan30() throws Exception {
        mockMvc.perform(post("/auth/signup")
            .contentType(MediaType.APPLICATION_FORM_URLENCODED)
            .with(csrf())
            .param("firstName", "abcabcabcabcabcabcabcabcabcabca")
            .sessionAttr("user", new UserDto())
        )
            .andExpect(status().isOk())
            .andExpect(view().name("auth/signup"))
            .andExpect(model().attributeHasFieldErrors("user", "firstName"));
    }

    @Test
    public void shouldReturnLastNameValidationErrorWhenLastNameLengthIsLessThan2() throws Exception {
        mockMvc.perform(post("/auth/signup")
            .contentType(MediaType.APPLICATION_FORM_URLENCODED)
            .with(csrf())
            .param("lastName", "a")
            .sessionAttr("user", new UserDto())
        )
            .andExpect(status().isOk())
            .andExpect(view().name("auth/signup"))
            .andExpect(model().attributeHasFieldErrors("user", "lastName"));
    }

    @Test
    public void shouldReturnLastNameValidationErrorWhenLastNameLengthIsGreaterThan30() throws Exception {
        mockMvc.perform(post("/auth/signup")
            .contentType(MediaType.APPLICATION_FORM_URLENCODED)
            .with(csrf())
            .param("lastName", "abcabcabcabcabcabcabcabcabcabca")
            .sessionAttr("user", new UserDto())
        )
            .andExpect(status().isOk())
            .andExpect(view().name("auth/signup"))
            .andExpect(model().attributeHasFieldErrors("user", "lastName"));
    }

    @Test
    public void shouldRedirectToSignInFormWhenFormFieldsAreValid() throws Exception {
        mockMvc.perform(post("/auth/signup")
            .contentType(MediaType.APPLICATION_FORM_URLENCODED)
            .with(csrf())
            .param("email", "john.doe@sbb.net")
            .param("password", "password")
            .param("firstName", "John")
            .param("lastName", "Doe")
            .sessionAttr("user", new UserDto())
        )
            .andExpect(flash().attributeExists("message"))
            .andExpect(redirectedUrl("/auth/signin"));
    }
}
