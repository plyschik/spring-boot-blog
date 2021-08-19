package io.plyschik.springbootblog.controller;

import io.plyschik.springbootblog.dto.CategoryWithPostsCount;
import io.plyschik.springbootblog.dto.CommentDto;
import io.plyschik.springbootblog.dto.YearArchiveEntry;
import io.plyschik.springbootblog.entity.Category;
import io.plyschik.springbootblog.entity.Post;
import io.plyschik.springbootblog.entity.Tag;
import io.plyschik.springbootblog.entity.User;
import io.plyschik.springbootblog.exception.*;
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
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.servlet.ModelAndView;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.temporal.TemporalAdjusters;
import java.util.List;

@Controller
@RequiredArgsConstructor
class BlogController {
    private final UserService userService;
    private final PostService postService;
    private final CategoryService categoryService;
    private final TagService tagService;

    @GetMapping("/")
    public ModelAndView index(
        @RequestParam(defaultValue = "0") int page,
        @Value("${pagination.post.blog}") int itemsPerPage
    ) {
        Page<Post> posts = postService.getPostsWithAuthorCategoryAndTags(PageRequest.of(page, itemsPerPage));
        List<CategoryWithPostsCount> categories = categoryService.getCategoriesWithPostsCount();
        List<YearArchiveEntry> archive = postService.getPostsArchive();

        ModelAndView modelAndView = new ModelAndView("blog/index");
        modelAndView.addObject("posts", posts);
        modelAndView.addObject("categories", categories);
        modelAndView.addObject("archive", archive);

        return modelAndView;
    }

    @GetMapping("/posts/{id}")
    public ModelAndView singlePost(@PathVariable Long id, Model model) {
        try {
            Post post = postService.getPostByIdWithAuthorCategoryAndTags(id);
            List<CategoryWithPostsCount> categories = categoryService.getCategoriesWithPostsCount();
            List<YearArchiveEntry> archive = postService.getPostsArchive();

            ModelAndView modelAndView = new ModelAndView("blog/single");
            modelAndView.addObject("post", post);
            modelAndView.addObject("categories", categories);
            modelAndView.addObject("archive", archive);

            if (!model.containsAttribute("comment")) {
                modelAndView.addObject("comment", new CommentDto());
            }

            return modelAndView;
        } catch (PostNotFoundException | PostIsNotPublishedException exception) {
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
            List<YearArchiveEntry> archive = postService.getPostsArchive();

            ModelAndView modelAndView = new ModelAndView("blog/posts_by_author");
            modelAndView.addObject("author", author);
            modelAndView.addObject("posts", posts);
            modelAndView.addObject("categories", categories);
            modelAndView.addObject("archive", archive);

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
            List<YearArchiveEntry> archive = postService.getPostsArchive();

            ModelAndView modelAndView = new ModelAndView("blog/posts_by_category");
            modelAndView.addObject("category", category);
            modelAndView.addObject("posts", posts);
            modelAndView.addObject("categories", categories);
            modelAndView.addObject("archive", archive);

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
            List<YearArchiveEntry> archive = postService.getPostsArchive();

            ModelAndView modelAndView = new ModelAndView("blog/posts_by_tag");
            modelAndView.addObject("tag", tag);
            modelAndView.addObject("posts", posts);
            modelAndView.addObject("categories", categories);
            modelAndView.addObject("archive", archive);

            return modelAndView;
        } catch (TagNotFoundException exception) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND);
        }
    }

    @GetMapping("/archive/{year:^[12][0-9]{3}$}/{month:^[2-9]|1[0-2]?$}")
    public ModelAndView postsFromMonth(
        @PathVariable Integer year,
        @PathVariable Integer month,
        @RequestParam(defaultValue = "0") int page,
        @Value("${pagination.post.blog}") int itemsPerPage
    ) {
        LocalDate initialDate = LocalDate.of(year, month, 1);
        LocalDateTime startDate = initialDate.atTime(LocalTime.MIN).with(TemporalAdjusters.firstDayOfMonth());
        LocalDateTime endDate = initialDate.atTime(LocalTime.MAX).with(TemporalAdjusters.lastDayOfMonth());

        Page<Post> posts = postService.getPostsFromDateRange(startDate, endDate, PageRequest.of(page, itemsPerPage));
        List<CategoryWithPostsCount> categories = categoryService.getCategoriesWithPostsCount();
        List<YearArchiveEntry> archive = postService.getPostsArchive();

        ModelAndView modelAndView = new ModelAndView("blog/posts_from_month");
        modelAndView.addObject("startDate", startDate);
        modelAndView.addObject("endDate", endDate);
        modelAndView.addObject("posts", posts);
        modelAndView.addObject("categories", categories);
        modelAndView.addObject("archive", archive);

        return modelAndView;
    }
}
