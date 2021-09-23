package io.plyschik.springbootblog.controller;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.plyschik.springbootblog.dto.CategoryWithPostsCount;
import io.plyschik.springbootblog.dto.YearArchiveEntry;
import io.plyschik.springbootblog.entity.Category;
import io.plyschik.springbootblog.entity.Post;
import io.plyschik.springbootblog.entity.Tag;
import io.plyschik.springbootblog.entity.User;
import io.plyschik.springbootblog.service.CategoryService;
import io.plyschik.springbootblog.service.PostService;
import io.plyschik.springbootblog.service.TagService;
import io.plyschik.springbootblog.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.context.MessageSource;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.ModelAndView;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.temporal.TemporalAdjusters;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Controller
@RequiredArgsConstructor
class BlogController {
    private final MessageSource messageSource;
    private final ObjectMapper objectMapper;
    private final UserService userService;
    private final PostService postService;
    private final CategoryService categoryService;
    private final TagService tagService;

    @GetMapping("/")
    public ModelAndView index(@RequestParam(required = false, defaultValue = "0") int page) {
        Page<Post> posts = postService.getPostsWithAuthorCategoryAndTags(
            PageRequest.of(page, 5, Sort.by(Sort.Order.desc("id")))
        );
        List<CategoryWithPostsCount> categories = categoryService.getTop5CategoriesWithPostsCount();
        List<YearArchiveEntry> archive = postService.getPostsArchive();

        return new ModelAndView("blog/index")
            .addObject("posts", posts)
            .addObject("categories", categories)
            .addObject("archive", archive);
    }

    @GetMapping("/posts/{id:^[1-9][0-9]*$}")
    public ModelAndView singlePost(@PathVariable long id) throws JsonProcessingException {
        Post post = postService.getPostWithAuthorCategoryAndTags(id);
        List<CategoryWithPostsCount> categories = categoryService.getTop5CategoriesWithPostsCount();
        List<YearArchiveEntry> archive = postService.getPostsArchive();
        Map<String, String> i18n = new HashMap<>() {{
            put("loading", messageSource.getMessage(
                "message.comments.loading",
                null,
                LocaleContextHolder.getLocale()
            ));
            put("only_authenticated_users_can_create_comments", messageSource.getMessage(
                "message.only_authenticated_users_can_create_comments",
                null,
                LocaleContextHolder.getLocale()
            ));
            put("empty_list", messageSource.getMessage(
                "message.comments.empty_list",
                null,
                LocaleContextHolder.getLocale()
            ));
            put("comments", messageSource.getMessage(
                "header.comments",
                null,
                LocaleContextHolder.getLocale()
            ));
            put("comment", messageSource.getMessage(
                "label.comment",
                null,
                LocaleContextHolder.getLocale()
            ));
            put("create", messageSource.getMessage(
                "label.create",
                null,
                LocaleContextHolder.getLocale()
            ));
            put("comment_edit", messageSource.getMessage(
                "header.comment_edit",
                null,
                LocaleContextHolder.getLocale()
            ));
            put("update", messageSource.getMessage(
                "label.update",
                null,
                LocaleContextHolder.getLocale()
            ));
            put("cancel", messageSource.getMessage(
                "label.cancel",
                null,
                LocaleContextHolder.getLocale()
            ));
            put("confirmation", messageSource.getMessage(
                "label.confirmation",
                null,
                LocaleContextHolder.getLocale()
            ));
            put("delete_message", messageSource.getMessage(
                "header.comment.delete",
                null,
                LocaleContextHolder.getLocale()
            ));
            put("confirm", messageSource.getMessage(
                "label.confirm",
                null,
                LocaleContextHolder.getLocale()
            ));
            put("page", messageSource.getMessage(
                "label.pagination.page",
                null,
                LocaleContextHolder.getLocale()
            ));
            put("of", messageSource.getMessage(
                "label.pagination.of",
                null,
                LocaleContextHolder.getLocale()
            ));
            put("edit", messageSource.getMessage(
                "label.edit",
                null,
                LocaleContextHolder.getLocale()
            ));
            put("delete", messageSource.getMessage(
                "label.delete",
                null,
                LocaleContextHolder.getLocale()
            ));
        }};

        return new ModelAndView("blog/single")
            .addObject("post", post)
            .addObject("categories", categories)
            .addObject("archive", archive)
            .addObject("i18n", objectMapper.writeValueAsString(i18n));
    }

    @GetMapping("/authors/{id:^[1-9][0-9]*$}/posts")
    public ModelAndView postsFromAuthor(
        @PathVariable long id,
        @RequestParam(required = false, defaultValue = "0") int page
    ) {
        User author = userService.getUserById(id);
        Page<Post> posts = postService.getPostsWithAuthorCategoryAndTagsByUserId(
            id,
            PageRequest.of(page, 5, Sort.by(Sort.Order.desc("id")))
        );
        List<CategoryWithPostsCount> categories = categoryService.getTop5CategoriesWithPostsCount();
        List<YearArchiveEntry> archive = postService.getPostsArchive();

        return new ModelAndView("blog/posts_by_author")
            .addObject("author", author)
            .addObject("posts", posts)
            .addObject("categories", categories)
            .addObject("archive", archive);
    }

    @GetMapping("/categories/{id:^[1-9][0-9]*$}/posts")
    public ModelAndView postsFromCategory(
        @PathVariable long id,
        @RequestParam(required = false, defaultValue = "0") int page
    ) {
        Category category = categoryService.getCategoryById(id);
        Page<Post> posts = postService.getPostsWithAuthorCategoryAndTagsByCategoryId(
            id,
            PageRequest.of(page, 5, Sort.by(Sort.Order.desc("id")))
        );
        List<CategoryWithPostsCount> categories = categoryService.getTop5CategoriesWithPostsCount();
        List<YearArchiveEntry> archive = postService.getPostsArchive();

        return new ModelAndView("blog/posts_by_category")
            .addObject("category", category)
            .addObject("posts", posts)
            .addObject("categories", categories)
            .addObject("archive", archive);
    }

    @GetMapping("/tags/{id:^[1-9][0-9]*$}/posts")
    public ModelAndView postsFromTag(
        @PathVariable long id,
        @RequestParam(required = false, defaultValue = "0") int page
    ) {
        Tag tag = tagService.getTagById(id);
        Page<Post> posts = postService.getPostsWithAuthorCategoryAndTagsByTagId(
            id,
            PageRequest.of(page, 5, Sort.by(Sort.Order.desc("id")))
        );
        List<CategoryWithPostsCount> categories = categoryService.getTop5CategoriesWithPostsCount();
        List<YearArchiveEntry> archive = postService.getPostsArchive();

        return new ModelAndView("blog/posts_by_tag")
            .addObject("tag", tag)
            .addObject("posts", posts)
            .addObject("categories", categories)
            .addObject("archive", archive);
    }

    @GetMapping("/archive/{year:^[12][0-9]{3}$}/{month:^[2-9]|1[0-2]?$}")
    public ModelAndView postsFromDataRange(
        @PathVariable int year,
        @PathVariable int month,
        @RequestParam(required = false, defaultValue = "0") int page
    ) {
        LocalDate initialDate = LocalDate.of(year, month, 1);
        LocalDateTime startDate = initialDate.with(TemporalAdjusters.firstDayOfMonth()).atTime(LocalTime.MIN);
        LocalDateTime endDate = initialDate.with(TemporalAdjusters.lastDayOfMonth()).atTime(LocalTime.MAX);

        Page<Post> posts = postService.getPostsWithAuthorCategoryAndTagsFromDateRange(
            startDate,
            endDate,
            PageRequest.of(page, 5, Sort.by(Sort.Order.desc("id")))
        );
        List<CategoryWithPostsCount> categories = categoryService.getTop5CategoriesWithPostsCount();
        List<YearArchiveEntry> archive = postService.getPostsArchive();

        return new ModelAndView("blog/posts_from_month")
            .addObject("startDate", startDate)
            .addObject("endDate", endDate)
            .addObject("posts", posts)
            .addObject("categories", categories)
            .addObject("archive", archive);
    }
}
