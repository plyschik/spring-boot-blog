package io.plyschik.springbootblog.controller;

import io.plyschik.springbootblog.entity.Category;
import io.plyschik.springbootblog.entity.Post;
import io.plyschik.springbootblog.entity.Tag;
import io.plyschik.springbootblog.entity.User;
import io.plyschik.springbootblog.service.CategoryService;
import io.plyschik.springbootblog.service.PostService;
import io.plyschik.springbootblog.service.TagService;
import io.plyschik.springbootblog.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.servlet.ModelAndView;

import java.util.Optional;

@Controller
@RequiredArgsConstructor
class HomeController {
    private final UserService userService;
    private final PostService postService;
    private final CategoryService categoryService;
    private final TagService tagService;

    @GetMapping("/")
    public ModelAndView index(
        @RequestParam(defaultValue = "0") int page,
        @Value("${pagination.post.blog}") int itemsPerPage
    ) {
        ModelAndView modelAndView = new ModelAndView("index");
        modelAndView.addObject(
            "posts",
            postService.getPaginatedPostsWithAuthorCategoryAndTags(PageRequest.of(page, itemsPerPage))
        );
        modelAndView.addObject("categories", categoryService.getCategoriesWithPostsCount());

        return modelAndView;
    }

    @GetMapping("/posts/{id}")
    public ModelAndView singlePost(@PathVariable Long id) {
        Optional<Post> post = postService.getSinglePostWithAuthorCategoryAndTags(id);

        if (post.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND);
        }

        return new ModelAndView("single", "post", post.get());
    }

    @GetMapping("/authors/{id}/posts")
    public ModelAndView postsFromAuthor(
        @PathVariable Long id,
        @RequestParam(defaultValue = "0") int page,
        @Value("${pagination.post.blog}") int itemsPerPage
    ) {
        Optional<User> author = userService.getUserById(id);
        if (author.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND);
        }

        Page<Post> posts = postService.getPostsByAuthorId(id, PageRequest.of(page, itemsPerPage));

        ModelAndView modelAndView = new ModelAndView("posts_by_author");
        modelAndView.addObject("author", author.get());
        modelAndView.addObject("posts", posts);
        modelAndView.addObject("categories", categoryService.getCategoriesWithPostsCount());

        return modelAndView;
    }

    @GetMapping("/categories/{id}/posts")
    public ModelAndView postsFromCategory(
        @PathVariable Long id,
        @RequestParam(defaultValue = "0") int page,
        @Value("${pagination.post.blog}") int itemsPerPage
    ) {
        Optional<Category> category = categoryService.getById(id);
        if (category.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND);
        }

        Page<Post> posts = postService.getPostsByCategoryId(id, PageRequest.of(page, itemsPerPage));

        ModelAndView modelAndView = new ModelAndView("posts_by_category");
        modelAndView.addObject("category", category.get());
        modelAndView.addObject("posts", posts);
        modelAndView.addObject("categories", categoryService.getCategoriesWithPostsCount());

        return modelAndView;
    }

    @GetMapping("/tags/{id}/posts")
    public ModelAndView postsFromTag(
        @PathVariable Long id,
        @RequestParam(defaultValue = "0") int page,
        @Value("${pagination.post.blog}") int itemsPerPage
    ) {
        Optional<Tag> tag = tagService.getById(id);
        if (tag.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND);
        }

        Page<Post> posts = postService.getPostsByTagId(id, PageRequest.of(page, itemsPerPage));

        ModelAndView modelAndView = new ModelAndView("posts_by_tag");
        modelAndView.addObject("tag", tag.get());
        modelAndView.addObject("posts", posts);
        modelAndView.addObject("categories", categoryService.getCategoriesWithPostsCount());

        return modelAndView;
    }
}
