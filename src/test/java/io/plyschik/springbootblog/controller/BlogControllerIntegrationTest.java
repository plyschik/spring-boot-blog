package io.plyschik.springbootblog.controller;

import io.plyschik.springbootblog.TestUtils;
import io.plyschik.springbootblog.entity.Category;
import io.plyschik.springbootblog.entity.Post;
import io.plyschik.springbootblog.entity.Tag;
import io.plyschik.springbootblog.entity.User;
import io.plyschik.springbootblog.entity.User.Role;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.web.servlet.MockMvc;

import javax.transaction.Transactional;
import java.util.Date;

import static org.hamcrest.Matchers.containsString;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
class BlogControllerIntegrationTest {
    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private TestUtils testUtils;

    @Test
    public void shouldReturnPostsList() throws Exception {
        mockMvc.perform(get("/"))
            .andExpect(status().isOk())
            .andExpect(view().name("blog/index"))
            .andExpect(model().attributeExists("posts", "categories"));
    }

    @Test
    public void shouldReturnNotFoundStatusWhenPostNotFound() throws Exception {
        mockMvc.perform(get("/posts/{id}", 1))
            .andExpect(status().isNotFound());
    }

    @Test
    public void shouldShowSinglePost() throws Exception {
        User user = testUtils.createUser(
            "administrator@sbb.net",
            "password",
            "John",
            "Doe",
            Role.ADMINISTRATOR
        );

        Post post = testUtils.createPost(
            "Test title",
            "Test content",
            new Date(),
            user
        );

        mockMvc.perform(get("/posts/{id}", post.getId()))
            .andExpect(status().isOk())
            .andExpect(view().name("blog/single"))
            .andExpect(model().attributeExists("post", "categories"))
            .andExpect(content().string(containsString(post.getTitle())))
            .andExpect(content().string(containsString(post.getContent())));
    }

    @Test
    public void shouldReturnPostsListByAuthor() throws Exception {
        User user = testUtils.createUser(
            "administrator@sbb.net",
            "password",
            "John",
            "Doe",
            Role.ADMINISTRATOR
        );

        mockMvc.perform(get("/authors/{id}/posts", user.getId()))
            .andExpect(status().isOk())
            .andExpect(view().name("blog/posts_by_author"))
            .andExpect(model().attributeExists("author", "posts", "categories"));
    }

    @Test
    public void shouldReturnNotFoundStatusWhenAuthorNotFound() throws Exception {
        mockMvc.perform(get("/authors/{id}/posts", 1))
            .andExpect(status().isNotFound());
    }

    @Test
    public void shouldReturnPostsListByCategory() throws Exception {
        Category category = testUtils.createCategory("Test category");

        mockMvc.perform(get("/categories/{id}/posts", category.getId()))
            .andExpect(status().isOk())
            .andExpect(view().name("blog/posts_by_category"))
            .andExpect(model().attributeExists("category", "posts", "categories"));
    }

    @Test
    public void shouldReturnNotFoundStatusWhenCategoryNotFound() throws Exception {
        mockMvc.perform(get("/categories/{id}/posts", 1))
            .andExpect(status().isNotFound());
    }

    @Test
    public void shouldReturnPostsListWithTag() throws Exception {
        Tag tag = testUtils.createTag("Test tag");

        mockMvc.perform(get("/tags/{id}/posts", tag.getId()))
            .andExpect(status().isOk())
            .andExpect(view().name("blog/posts_by_tag"))
            .andExpect(model().attributeExists("tag", "posts", "categories"));
    }

    @Test
    public void shouldReturnNotFoundStatusWhenTagNotFound() throws Exception {
        mockMvc.perform(get("/tags/{id}/posts", 1))
            .andExpect(status().isNotFound());
    }
}
