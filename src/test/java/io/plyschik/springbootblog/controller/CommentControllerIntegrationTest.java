package io.plyschik.springbootblog.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.plyschik.springbootblog.TestUtils;
import io.plyschik.springbootblog.dto.CommentDto;
import io.plyschik.springbootblog.entity.Comment;
import io.plyschik.springbootblog.entity.Post;
import io.plyschik.springbootblog.entity.User;
import io.plyschik.springbootblog.entity.User.Role;
import io.plyschik.springbootblog.exception.CommentNotFoundException;
import io.plyschik.springbootblog.repository.CommentRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import javax.transaction.Transactional;
import java.time.LocalDateTime;
import java.util.Date;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
class CommentControllerIntegrationTest {
    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

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
            LocalDateTime.now(),
            user
        );

        testUtils.createUser(
            "foo.bar@sbb.net",
            "password",
            "Foo",
            "Bar",
            Role.USER
        );

        mockMvc.perform(post("/api/posts/{id}/comments", post.getId())
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(new CommentDto(""))) // can not be blank
        ).andExpect(status().isBadRequest());

        mockMvc.perform(post("/api/posts/{id}/comments", post.getId())
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(new CommentDto("a"))) // should be minimum 2 characters
        ).andExpect(status().isBadRequest());

        mockMvc.perform(post("/api/posts/{id}/comments", post.getId())
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(new CommentDto("a".repeat(65536)))) // max 65535 characters
        ).andExpect(status().isBadRequest());
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
            LocalDateTime.now(),
            user
        );

        testUtils.createUser(
            "foo.bar@sbb.net",
            "password",
            "Foo",
            "Bar",
            Role.USER
        );

        mockMvc.perform(post("/api/posts/{id}/comments", post.getId())
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(new CommentDto("Test comment")))
        ).andExpect(status().isOk());

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
            LocalDateTime.now(),
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
            LocalDateTime.now(),
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

        mockMvc.perform(patch("/api/posts/{postId}/comments/{commentId}", post.getId(), comment.getId())
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(new CommentDto(""))) // can not be blank
        ).andExpect(status().isBadRequest());

        mockMvc.perform(patch("/api/posts/{postId}/comments/{commentId}", post.getId(), comment.getId())
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(new CommentDto("a"))) // should be minimum 2 characters
        ).andExpect(status().isBadRequest());

        mockMvc.perform(patch("/api/posts/{postId}/comments/{commentId}", post.getId(), comment.getId())
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(new CommentDto("a".repeat(65536)))) // max 65535 characters
        ).andExpect(status().isBadRequest());
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
            LocalDateTime.now(),
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

        mockMvc.perform(patch("/api/posts/{postId}/comments/{commentId}", post.getId(), comment.getId())
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(new CommentDto("Updated comment")))
        ).andExpect(status().isOk());

        Comment updatedComment = commentRepository.findById(comment.getId()).orElseThrow(CommentNotFoundException::new);

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
            LocalDateTime.now(),
            johnDoe
        );

        mockMvc.perform(delete("/api/posts/{postId}/comments/1", post.getId()))
            .andExpect(status().isNotFound());
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
            LocalDateTime.now(),
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

        mockMvc.perform(delete("/api/posts/{postId}/comments/{commentId}", post.getId(), comment.getId()))
            .andExpect(status().isNoContent());

        assertFalse(commentRepository.existsById(comment.getId()));
    }
}
