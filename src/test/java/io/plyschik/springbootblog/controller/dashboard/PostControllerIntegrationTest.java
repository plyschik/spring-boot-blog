package io.plyschik.springbootblog.controller.dashboard;

import io.plyschik.springbootblog.TestUtils;
import io.plyschik.springbootblog.dto.PostDto;
import io.plyschik.springbootblog.entity.Post;
import io.plyschik.springbootblog.entity.User;
import io.plyschik.springbootblog.entity.User.Role;
import io.plyschik.springbootblog.exception.PostNotFoundException;
import io.plyschik.springbootblog.repository.PostRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
class PostControllerIntegrationTest {
    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private TestUtils testUtils;

    @Autowired
    private PostRepository postRepository;

    @Test
    @WithMockUser
    public void shouldReturnForbiddenWhenUserRoleIsUser() throws Exception {
        mockMvc.perform(get("/dashboard/posts"))
            .andExpect(status().isForbidden());

        mockMvc.perform(get("/dashboard/posts/create"))
            .andExpect(status().isForbidden());

        mockMvc.perform(post("/dashboard/posts/create"))
            .andExpect(status().isForbidden());

        mockMvc.perform(get("/dashboard/posts/1/edit"))
            .andExpect(status().isForbidden());

        mockMvc.perform(post("/dashboard/posts/1/edit"))
            .andExpect(status().isForbidden());

        mockMvc.perform(get("/dashboard/posts/1/delete"))
            .andExpect(status().isForbidden());

        mockMvc.perform(post("/dashboard/posts/1/delete"))
            .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(roles = {"MODERATOR"})
    public void shouldReturnForbiddenWhenUserRoleIsModerator() throws Exception {
        mockMvc.perform(get("/dashboard/posts"))
            .andExpect(status().isForbidden());

        mockMvc.perform(get("/dashboard/posts/create"))
            .andExpect(status().isForbidden());

        mockMvc.perform(post("/dashboard/posts/create"))
            .andExpect(status().isForbidden());

        mockMvc.perform(get("/dashboard/posts/1/edit"))
            .andExpect(status().isForbidden());

        mockMvc.perform(post("/dashboard/posts/1/edit"))
            .andExpect(status().isForbidden());

        mockMvc.perform(get("/dashboard/posts/1/delete"))
            .andExpect(status().isForbidden());

        mockMvc.perform(post("/dashboard/posts/1/delete"))
            .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(roles = {"ADMINISTRATOR"})
    public void shouldReturnPostsList() throws Exception {
        mockMvc.perform(get("/dashboard/posts"))
            .andExpect(status().isOk())
            .andExpect(view().name("dashboard/post/list"))
            .andExpect(model().attributeExists("posts"));
    }

    @Test
    @WithMockUser(value = "test", roles = {"ADMINISTRATOR"})
    public void shouldReturnFormValidationErrorWhenTitleFieldIsInvalid() throws Exception {
        mockMvc.perform(post("/dashboard/posts/create")
            .contentType(MediaType.APPLICATION_FORM_URLENCODED)
            .with(csrf())
            .param("title", "") // can not be blank
            .sessionAttr("post", new PostDto())
        )
            .andExpect(status().isOk())
            .andExpect(view().name("dashboard/post/create"))
            .andExpect(model().attributeHasFieldErrors("post", "title"));

        mockMvc.perform(post("/dashboard/posts/create")
            .contentType(MediaType.APPLICATION_FORM_URLENCODED)
            .with(csrf())
            .param("title", "a") // should be minimum 2 characters
            .sessionAttr("post", new PostDto())
        )
            .andExpect(status().isOk())
            .andExpect(view().name("dashboard/post/create"))
            .andExpect(model().attributeHasFieldErrors("post", "title"));

        mockMvc.perform(post("/dashboard/posts/create")
            .contentType(MediaType.APPLICATION_FORM_URLENCODED)
            .with(csrf())
            .param("title", "a".repeat(121)) // max 120 characters
            .sessionAttr("post", new PostDto())
        )
            .andExpect(status().isOk())
            .andExpect(view().name("dashboard/post/create"))
            .andExpect(model().attributeHasFieldErrors("post", "title"));
    }

    @Test
    @WithMockUser(value = "test", roles = {"ADMINISTRATOR"})
    public void shouldReturnFormValidationErrorWhenContentRawFieldIsInvalid() throws Exception {
        mockMvc.perform(post("/dashboard/posts/create")
            .contentType(MediaType.APPLICATION_FORM_URLENCODED)
            .with(csrf())
            .param("contentRaw", "") // can not be blank
            .sessionAttr("post", new PostDto())
        )
            .andExpect(status().isOk())
            .andExpect(view().name("dashboard/post/create"))
            .andExpect(model().attributeHasFieldErrors("post", "contentRaw"));

        mockMvc.perform(post("/dashboard/posts/create")
            .contentType(MediaType.APPLICATION_FORM_URLENCODED)
            .with(csrf())
            .param("contentRaw", "aaa") // minimum 4 characters
            .sessionAttr("post", new PostDto())
        )
            .andExpect(status().isOk())
            .andExpect(view().name("dashboard/post/create"))
            .andExpect(model().attributeHasFieldErrors("post", "contentRaw"));

        mockMvc.perform(post("/dashboard/posts/create")
            .contentType(MediaType.APPLICATION_FORM_URLENCODED)
            .with(csrf())
            .param("contentRaw", "a".repeat(65536)) // maximum 65535 characters
            .sessionAttr("post", new PostDto())
        )
            .andExpect(status().isOk())
            .andExpect(view().name("dashboard/post/create"))
            .andExpect(model().attributeHasFieldErrors("post", "contentRaw"));
    }

    @Test
    @WithMockUser(value = "administrator@sbb.net", roles = {"ADMINISTRATOR"})
    public void shouldPersistPostWhenFormFieldsAreValid() throws Exception {
        testUtils.createUser(
            "administrator@sbb.net",
            "password",
            "John",
            "Doe",
            Role.ADMINISTRATOR
        );

        mockMvc.perform(post("/dashboard/posts/create")
            .contentType(MediaType.APPLICATION_FORM_URLENCODED)
            .with(csrf())
            .param("title", "Title")
            .param("contentRaw", "Content")
            .sessionAttr("post", new PostDto())
        )
            .andExpect(flash().attributeExists("alert"))
            .andExpect(redirectedUrl("/dashboard/posts"));

        Post post = postRepository.findAll().stream().findFirst().orElseThrow();

        assertEquals("Title", post.getTitle());
        assertEquals("Content", post.getContentRaw());
    }

    @Test
    @WithMockUser(value = "administrator@sbb.net", roles = {"ADMINISTRATOR"})
    public void shouldReturnFilledEditFormWhenPostExists() throws Exception {
        User user = testUtils.createUser(
            "administrator@sbb.net",
            "password",
            "John",
            "Doe",
            Role.ADMINISTRATOR
        );

        Post post = testUtils.createPost("Title", "Content", LocalDateTime.now(), user);

        mockMvc.perform(get("/dashboard/posts/{id}/edit", post.getId()))
            .andExpect(status().isOk())
            .andExpect(model().attributeExists("id", "post"));
    }

    @Test
    @WithMockUser(value = "administrator@sbb.net", roles = {"ADMINISTRATOR"})
    public void shouldRedirectToPostsListWhenPostNotExists() throws Exception {
        mockMvc.perform(get("/dashboard/posts/{id}/edit", 1))
            .andExpect(view().name("errors/resource_not_found"));
    }

    @Test
    @WithMockUser(value = "test", roles = {"ADMINISTRATOR"})
    public void shouldReturnEditFormValidationErrorWhenTitleFieldIsInvalid() throws Exception {
        User user = testUtils.createUser(
            "administrator@sbb.net",
            "password",
            "John",
            "Doe",
            Role.ADMINISTRATOR
        );

        Post post = testUtils.createPost("Title", "Content", LocalDateTime.now(), user);

        mockMvc.perform(post("/dashboard/posts/{id}/edit", post.getId())
            .contentType(MediaType.APPLICATION_FORM_URLENCODED)
            .with(csrf())
            .param("title", "") // can not be blank
        )
            .andExpect(status().isOk())
            .andExpect(view().name("dashboard/post/edit"))
            .andExpect(model().attributeHasFieldErrors("post", "title"));

        mockMvc.perform(post("/dashboard/posts/{id}/edit", post.getId())
            .contentType(MediaType.APPLICATION_FORM_URLENCODED)
            .with(csrf())
            .param("title", "a") // should be minimum 2 characters
        )
            .andExpect(status().isOk())
            .andExpect(view().name("dashboard/post/edit"))
            .andExpect(model().attributeHasFieldErrors("post", "title"));

        mockMvc.perform(post("/dashboard/posts/{id}/edit", post.getId())
            .contentType(MediaType.APPLICATION_FORM_URLENCODED)
            .with(csrf())
            .param("title", "a".repeat(121)) // max 120 characters
        )
            .andExpect(status().isOk())
            .andExpect(view().name("dashboard/post/edit"))
            .andExpect(model().attributeHasFieldErrors("post", "title"));
    }

    @Test
    @WithMockUser(value = "test", roles = {"ADMINISTRATOR"})
    public void shouldReturnEditFormValidationErrorWhenContentFieldIsInvalid() throws Exception {
        User user = testUtils.createUser(
            "administrator@sbb.net",
            "password",
            "John",
            "Doe",
            Role.ADMINISTRATOR
        );

        Post post = testUtils.createPost("Title", "Content", LocalDateTime.now(), user);

        mockMvc.perform(post("/dashboard/posts/{id}/edit", post.getId())
            .contentType(MediaType.APPLICATION_FORM_URLENCODED)
            .with(csrf())
            .param("contentRaw", "") // can not be blank
        )
            .andExpect(status().isOk())
            .andExpect(view().name("dashboard/post/edit"))
            .andExpect(model().attributeHasFieldErrors("post", "contentRaw"));

        mockMvc.perform(post("/dashboard/posts/{id}/edit", post.getId())
            .contentType(MediaType.APPLICATION_FORM_URLENCODED)
            .with(csrf())
            .param("contentRaw", "aaa") // minimum 4 characters
        )
            .andExpect(status().isOk())
            .andExpect(view().name("dashboard/post/edit"))
            .andExpect(model().attributeHasFieldErrors("post", "contentRaw"));

        mockMvc.perform(post("/dashboard/posts/{id}/edit", post.getId())
            .contentType(MediaType.APPLICATION_FORM_URLENCODED)
            .with(csrf())
            .param("contentRaw", "a".repeat(65536)) // maximum 65535 characters
        )
            .andExpect(status().isOk())
            .andExpect(view().name("dashboard/post/edit"))
            .andExpect(model().attributeHasFieldErrors("post", "contentRaw"));
    }

    @Test
    @WithMockUser(value = "administrator@sbb.net", roles = {"ADMINISTRATOR"})
    public void shouldUpdatePostWhenFormFieldsAreValid() throws Exception {
        User user = testUtils.createUser(
            "administrator@sbb.net",
            "password",
            "John",
            "Doe",
            Role.ADMINISTRATOR
        );

        Post post = testUtils.createPost("Title", "Content", LocalDateTime.now(), user);

        mockMvc.perform(post("/dashboard/posts/{id}/edit", post.getId())
            .contentType(MediaType.APPLICATION_FORM_URLENCODED)
            .with(csrf())
            .param("title", "Updated title")
            .param("contentRaw", "Updated content")
        )
            .andExpect(flash().attributeExists("alert"))
            .andExpect(redirectedUrl("/dashboard/posts"));

        Post updatedPost = postRepository.findById(post.getId()).orElseThrow(PostNotFoundException::new);

        assertEquals("Updated title", updatedPost.getTitle());
        assertEquals("Updated content", updatedPost.getContentRaw());
    }

    @Test
    @WithMockUser(value = "administrator@sbb.net", roles = {"ADMINISTRATOR"})
    public void shouldReturnDeleteConfirmViewWhenPostExists() throws Exception {
        User user = testUtils.createUser(
            "administrator@sbb.net",
            "password",
            "John",
            "Doe",
            Role.ADMINISTRATOR
        );

        Post post = testUtils.createPost("Title", "Content", LocalDateTime.now(), user);

        mockMvc.perform(get("/dashboard/posts/{id}/delete", post.getId()))
            .andExpect(status().isOk())
            .andExpect(view().name("dashboard/post/delete"))
            .andExpect(model().attributeExists("post"));
    }

    @Test
    @WithMockUser(value = "administrator@sbb.net", roles = {"ADMINISTRATOR"})
    public void shouldRedirectToPostListWhenPostNotExists() throws Exception {
        mockMvc.perform(get("/dashboard/posts/{id}/delete", 1))
            .andExpect(view().name("errors/resource_not_found"));
    }

    @Test
    @WithMockUser(value = "administrator@sbb.net", roles = {"ADMINISTRATOR"})
    public void shouldRedirectToPostsListWhenPostNotExistsAfterConfirmation() throws Exception {
        mockMvc.perform(post("/dashboard/posts/{id}/delete", 1).with(csrf()))
            .andExpect(flash().attributeExists("alert"))
            .andExpect(redirectedUrl("/dashboard/posts"));
    }

    @Test
    @WithMockUser(value = "administrator@sbb.net", roles = {"ADMINISTRATOR"})
    public void shouldDeletePostWhenConfirmed() throws Exception {
        User user = testUtils.createUser(
            "administrator@sbb.net",
            "password",
            "John",
            "Doe",
            Role.ADMINISTRATOR
        );

        Post post = testUtils.createPost("Title", "Content", LocalDateTime.now(), user);

        mockMvc.perform(post("/dashboard/posts/{id}/delete", post.getId()).with(csrf()))
            .andExpect(flash().attributeExists("alert"))
            .andExpect(redirectedUrl("/dashboard/posts"));

        assertFalse(postRepository.existsById(post.getId()));
    }
}
