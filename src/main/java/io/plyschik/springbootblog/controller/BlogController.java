package io.plyschik.springbootblog.controller;

import io.plyschik.springbootblog.dto.CategoryWithPostsCount;
import io.plyschik.springbootblog.dto.CommentDto;
import io.plyschik.springbootblog.entity.*;
import io.plyschik.springbootblog.exception.CategoryNotFoundException;
import io.plyschik.springbootblog.exception.PostNotFoundException;
import io.plyschik.springbootblog.exception.TagNotFoundException;
import io.plyschik.springbootblog.exception.UserNotFoundException;
import io.plyschik.springbootblog.service.*;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.servlet.ModelAndView;

import java.util.List;

@Controller
@RequiredArgsConstructor
class BlogController {
    private final UserService userService;
    private final PostService postService;
    private final CategoryService categoryService;
    private final TagService tagService;
    private final CommentService commentService;

    @GetMapping("/")
    public ModelAndView index(
        @RequestParam(defaultValue = "0") int page,
        @Value("${pagination.post.blog}") int itemsPerPage
    ) {
        Page<Post> posts = postService.getPostsWithAuthorCategoryAndTags(PageRequest.of(page, itemsPerPage));
        List<CategoryWithPostsCount> categories = categoryService.getCategoriesWithPostsCount();

        ModelAndView modelAndView = new ModelAndView("blog/index");
        modelAndView.addObject("posts", posts);
        modelAndView.addObject("categories", categories);

        return modelAndView;
    }

    @GetMapping("/posts/{id}")
    public ModelAndView singlePost(
        @PathVariable Long id,
        @RequestParam(defaultValue = "0") int page,
        @Value("${pagination.comments}") int itemsPerPage,
        Model model
    ) {
        try {
            Post post = postService.getPostByIdWithAuthorCategoryAndTags(id);
            List<CategoryWithPostsCount> categories = categoryService.getCategoriesWithPostsCount();
            Page<Comment> comments = commentService.getCommentsByPost(post, PageRequest.of(page, itemsPerPage));

            ModelAndView modelAndView = new ModelAndView("blog/single");
            modelAndView.addObject("post", post);
            modelAndView.addObject("categories", categories);
            modelAndView.addObject("comments", comments);

            if (!model.containsAttribute("comment")) {
                modelAndView.addObject("comment", new CommentDto());
            }

            return modelAndView;
        } catch (PostNotFoundException exception) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND);
        }
    }

    @GetMapping("/authors/{id}/posts")
    public ModelAndView postsFromAuthor(
        @PathVariable Long id,
        @RequestParam(defaultValue = "0") int page,
        @Value("${pagination.post.blog}") int itemsPerPage
    ) {
        try {
            User author = userService.getUserById(id);
            Page<Post> posts = postService.getPostsByUserId(id, PageRequest.of(page, itemsPerPage));
            List<CategoryWithPostsCount> categories = categoryService.getCategoriesWithPostsCount();

            ModelAndView modelAndView = new ModelAndView("blog/posts_by_author");
            modelAndView.addObject("author", author);
            modelAndView.addObject("posts", posts);
            modelAndView.addObject("categories", categories);

            return modelAndView;
        } catch (UserNotFoundException exception) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND);
        }
    }

    @GetMapping("/categories/{id}/posts")
    public ModelAndView postsFromCategory(
        @PathVariable Long id,
        @RequestParam(defaultValue = "0") int page,
        @Value("${pagination.post.blog}") int itemsPerPage
    ) {
        try {
            Category category = categoryService.getCategoryById(id);
            Page<Post> posts = postService.getPostsByCategoryId(id, PageRequest.of(page, itemsPerPage));
            List<CategoryWithPostsCount> categories = categoryService.getCategoriesWithPostsCount();

            ModelAndView modelAndView = new ModelAndView("blog/posts_by_category");
            modelAndView.addObject("category", category);
            modelAndView.addObject("posts", posts);
            modelAndView.addObject("categories", categories);

            return modelAndView;
        } catch (CategoryNotFoundException exception) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND);
        }
    }

    @GetMapping("/tags/{id}/posts")
    public ModelAndView postsFromTag(
        @PathVariable Long id,
        @RequestParam(defaultValue = "0") int page,
        @Value("${pagination.post.blog}") int itemsPerPage
    ) {
        try {
            Tag tag = tagService.getTagById(id);
            Page<Post> posts = postService.getPostsByTagId(id, PageRequest.of(page, itemsPerPage));
            List<CategoryWithPostsCount> categories = categoryService.getCategoriesWithPostsCount();

            ModelAndView modelAndView = new ModelAndView("blog/posts_by_tag");
            modelAndView.addObject("tag", tag);
            modelAndView.addObject("posts", posts);
            modelAndView.addObject("categories", categories);

            return modelAndView;
        } catch (TagNotFoundException exception) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND);
        }
    }
}
