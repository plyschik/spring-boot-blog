package io.plyschik.springbootblog.controller;

import io.plyschik.springbootblog.TestUtils;
import io.plyschik.springbootblog.dto.CommentDto;
import io.plyschik.springbootblog.entity.*;
import io.plyschik.springbootblog.exception.CommentNotFound;
import io.plyschik.springbootblog.repository.CommentRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import javax.transaction.Transactional;
import java.util.Date;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
class CommentControllerIntegrationTest {
    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private TestUtils testUtils;

    @Autowired
    private CommentRepository commentRepository;

    @Test
    @WithMockUser(value = "foo.bar@sbb.net")
    public void shouldReturnFormValidationErrorWhenContentFieldIsInvalid() throws Exception {
        User user = testUtils.createUser(
            "john.doe@sbb.net",
            "password",
            "John",
            "Doe",
            Role.ADMINISTRATOR
        );

        Post post = testUtils.createPost(
            "Post title",
            "Content",
            new Date(),
            user
        );

        testUtils.createUser(
            "foo.bar@sbb.net",
            "password",
            "Foo",
            "Bar",
            Role.USER
        );

        mockMvc.perform(post("/posts/{id}/comments", post.getId())
            .contentType(MediaType.APPLICATION_FORM_URLENCODED)
            .with(csrf())
            .param("content", "") // can not be blank
        )
            .andExpect(flash().attributeExists(
                "comment",
                "org.springframework.validation.BindingResult.comment"
            ))
            .andExpect(redirectedUrlTemplate("/posts/{id}", post.getId()));

        mockMvc.perform(post("/posts/{id}/comments", post.getId())
            .contentType(MediaType.APPLICATION_FORM_URLENCODED)
            .with(csrf())
            .param("content", "a") // should be minimum 2 characters
        )
            .andExpect(flash().attributeExists(
                "comment",
                "org.springframework.validation.BindingResult.comment"
            ))
            .andExpect(redirectedUrlTemplate("/posts/{id}", post.getId()));

        mockMvc.perform(post("/posts/{id}/comments", post.getId())
            .contentType(MediaType.APPLICATION_FORM_URLENCODED)
            .with(csrf())
            .param("content", "a".repeat(65536)) // max 65535 characters
        )
            .andExpect(flash().attributeExists(
                "comment",
                "org.springframework.validation.BindingResult.comment"
            ))
            .andExpect(redirectedUrlTemplate("/posts/{id}", post.getId()));
    }

    @Test
    @WithMockUser(value = "foo.bar@sbb.net")
    public void shouldPersistCommentWhenContentFieldIsValidAndUserIsAuthenticated() throws Exception {
        User user = testUtils.createUser(
            "john.doe@sbb.net",
            "password",
            "John",
            "Doe",
            Role.ADMINISTRATOR
        );

        Post post = testUtils.createPost(
            "Post title",
            "Content",
            new Date(),
            user
        );

        testUtils.createUser(
            "foo.bar@sbb.net",
            "password",
            "Foo",
            "Bar",
            Role.USER
        );

        mockMvc.perform(post("/posts/{id}/comments", post.getId())
            .contentType(MediaType.APPLICATION_FORM_URLENCODED)
            .with(csrf())
            .param("content", "Test comment")
            .sessionAttr("comment", new CommentDto())
        )
            .andExpect(flash().attributeExists("alert"))
            .andExpect(redirectedUrlTemplate("/posts/{id}", post.getId()));

        Comment comment = commentRepository.findAll().stream().findFirst().orElseThrow();

        assertEquals("Test comment", comment.getContent());
    }

    @Test
    @WithMockUser(value = "foo.bar@sbb.net")
    public void shouldReturnToPostWhenCommentToEditNotExists() throws Exception {
        User user = testUtils.createUser(
            "john.doe@sbb.net",
            "password",
            "John",
            "Doe",
            Role.ADMINISTRATOR
        );

        Post post = testUtils.createPost(
            "Post title",
            "Content",
            new Date(),
            user
        );

        testUtils.createUser(
            "foo.bar@sbb.net",
            "password",
            "Foo",
            "Bar",
            Role.USER
        );

        mockMvc.perform(get("/posts/{id}/comments/1", post.getId()))
            .andExpect(status().isNotFound());
    }

    @Test
    @WithMockUser(value = "foo.bar@sbb.net")
    public void shouldReturnFormValidationErrorWhenContentFieldIsInvalidDuringEdit() throws Exception {
        User johnDoe = testUtils.createUser(
            "john.doe@sbb.net",
            "password",
            "John",
            "Doe",
            Role.ADMINISTRATOR
        );

        Post post = testUtils.createPost(
            "Post title",
            "Content",
            new Date(),
            johnDoe
        );

        User fooBar = testUtils.createUser(
            "foo.bar@sbb.net",
            "password",
            "Foo",
            "Bar",
            Role.USER
        );

        Comment comment = testUtils.createComment(
            "Comment",
            new Date(),
            fooBar,
            post
        );

        mockMvc.perform(post("/posts/{postId}/comments/{commentId}/edit", post.getId(), comment.getId())
            .contentType(MediaType.APPLICATION_FORM_URLENCODED)
            .with(csrf())
            .param("content", "") // can not be blank
        )
            .andExpect(flash().attributeExists(
                "comment",
                "org.springframework.validation.BindingResult.comment"
            ))
            .andExpect(redirectedUrlTemplate(
                "/posts/{postId}/comments/{commentId}/edit",
                post.getId(),
                comment.getId()
            ));

        mockMvc.perform(post("/posts/{postId}/comments/{commentId}/edit", post.getId(), comment.getId())
            .contentType(MediaType.APPLICATION_FORM_URLENCODED)
            .with(csrf())
            .param("content", "a") // should be minimum 2 characters
        )
            .andExpect(flash().attributeExists(
                "comment",
                "org.springframework.validation.BindingResult.comment"
            ))
            .andExpect(redirectedUrlTemplate(
                "/posts/{postId}/comments/{commentId}/edit",
                post.getId(),
                comment.getId()
            ));

        mockMvc.perform(post("/posts/{postId}/comments/{commentId}/edit", post.getId(), comment.getId())
            .contentType(MediaType.APPLICATION_FORM_URLENCODED)
            .with(csrf())
            .param("content", "a".repeat(65536)) // max 65535 characters
        )
            .andExpect(flash().attributeExists(
                "comment",
                "org.springframework.validation.BindingResult.comment"
            ))
            .andExpect(redirectedUrlTemplate(
                "/posts/{postId}/comments/{commentId}/edit",
                post.getId(),
                comment.getId()
            ));
    }

    @Test
    @WithMockUser(value = "foo.bar@sbb.net")
    public void shouldUpdateCommentWhenContentFieldIsValid() throws Exception {
        User johnDoe = testUtils.createUser(
            "john.doe@sbb.net",
            "password",
            "John",
            "Doe",
            Role.ADMINISTRATOR
        );

        Post post = testUtils.createPost(
            "Post title",
            "Content",
            new Date(),
            johnDoe
        );

        User fooBar = testUtils.createUser(
            "foo.bar@sbb.net",
            "password",
            "Foo",
            "Bar",
            Role.USER
        );

        Comment comment = testUtils.createComment(
            "Comment",
            new Date(),
            fooBar,
            post
        );

        mockMvc.perform(post("/posts/{postId}/comments/{commentId}/edit", post.getId(), comment.getId())
            .contentType(MediaType.APPLICATION_FORM_URLENCODED)
            .with(csrf())
            .param("content", "Updated comment")
        )
            .andExpect(flash().attributeExists("alert"))
            .andExpect(redirectedUrlTemplate("/posts/{postId}", post.getId()));

        Comment updatedComment = commentRepository.findById(comment.getId()).orElseThrow(CommentNotFound::new);

        assertEquals("Updated comment", updatedComment.getContent());
    }

    @Test
    @WithMockUser(value = "john.doe@sbb.net", roles = {"ADMINISTRATOR"})
    public void shouldRedirectToPostWhenCommentNotExists() throws Exception {
        User johnDoe = testUtils.createUser(
            "john.doe@sbb.net",
            "password",
            "John",
            "Doe",
            Role.ADMINISTRATOR
        );

        Post post = testUtils.createPost(
            "Post title",
            "Content",
            new Date(),
            johnDoe
        );

        mockMvc.perform(get("/posts/{postId}/comments/1/delete", post.getId()))
            .andExpect(flash().attributeExists("alert"))
            .andExpect(redirectedUrlTemplate("/posts/{postId}", post.getId()));
    }

    @Test
    @WithMockUser(value = "john.doe@sbb.net", roles = {"ADMINISTRATOR"})
    public void shouldDeleteCommentWhenCommentExists() throws Exception {
        User johnDoe = testUtils.createUser(
            "john.doe@sbb.net",
            "password",
            "John",
            "Doe",
            Role.ADMINISTRATOR
        );

        Post post = testUtils.createPost(
            "Post title",
            "Content",
            new Date(),
            johnDoe
        );

        User fooBar = testUtils.createUser(
            "foo.bar@sbb.net",
            "password",
            "Foo",
            "Bar",
            Role.USER
        );

        Comment comment = testUtils.createComment(
            "Comment",
            new Date(),
            fooBar,
            post
        );

        mockMvc.perform(post("/posts/{postId}/comments/{commentId}/delete", post.getId(), comment.getId()).with(csrf()))
            .andExpect(flash().attributeExists("alert"))
            .andExpect(redirectedUrlTemplate("/posts/{postId}", post.getId()));

        assertFalse(commentRepository.existsById(comment.getId()));
    }
}
