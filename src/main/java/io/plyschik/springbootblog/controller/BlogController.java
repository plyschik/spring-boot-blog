package io.plyschik.springbootblog.controller;

import io.plyschik.springbootblog.dto.Alert;
import io.plyschik.springbootblog.dto.CommentDto;
import io.plyschik.springbootblog.entity.Category;
import io.plyschik.springbootblog.entity.Post;
import io.plyschik.springbootblog.entity.Tag;
import io.plyschik.springbootblog.entity.User;
import io.plyschik.springbootblog.exception.PostNotFound;
import io.plyschik.springbootblog.exception.UserNotFound;
import io.plyschik.springbootblog.service.*;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.MessageSource;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import javax.validation.Valid;
import java.security.Principal;
import java.util.Optional;

@Controller
@RequiredArgsConstructor
class BlogController {
    private final UserService userService;
    private final PostService postService;
    private final CategoryService categoryService;
    private final TagService tagService;
    private final CommentService commentService;
    private final MessageSource messageSource;

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
    public ModelAndView singlePost(@PathVariable Long id, Model model) {
        Optional<Post> post = postService.getSinglePostWithAuthorCategoryAndTags(id);
        if (post.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND);
        }

        ModelAndView modelAndView = new ModelAndView("single");
        modelAndView.addObject("post", post.get());
        modelAndView.addObject("categories", categoryService.getCategoriesWithPostsCount());

        if (!model.containsAttribute("comment")) {
            modelAndView.addObject("comment", new CommentDto());
        }

        return modelAndView;
    }

    @PostMapping("/posts/{id}/comments")
    public ModelAndView processCommentForm(
        @PathVariable("id") Long postId,
        @ModelAttribute("comment") @Valid CommentDto commentDto,
        BindingResult bindingResult,
        Principal principal,
        RedirectAttributes redirectAttributes
    ) {
        if (bindingResult.hasErrors()) {
            redirectAttributes.addFlashAttribute("comment", commentDto);
            redirectAttributes.addFlashAttribute(
                "org.springframework.validation.BindingResult.comment",
                bindingResult
            );

            return new ModelAndView(String.format("redirect:/posts/%d", postId));
        }

        try {
            commentService.createComment(
                commentDto,
                userService.getByEmail(principal.getName()).orElseThrow(UserNotFound::new),
                postService.getById(postId).orElseThrow(PostNotFound::new)
            );

            redirectAttributes.addFlashAttribute("alert", new Alert(
                "success",
                messageSource.getMessage(
                    "message.comment_has_successfully_created",
                    null,
                    LocaleContextHolder.getLocale()
                )
            ));

            return new ModelAndView(String.format("redirect:/posts/%d", postId));
        } catch (UserNotFound exception) {
            redirectAttributes.addFlashAttribute("alert", new Alert(
                "danger",
                messageSource.getMessage(
                    "message.user_not_found",
                    null,
                    LocaleContextHolder.getLocale()
                )
            ));

            return new ModelAndView("redirect:/");
        } catch (PostNotFound exception) {
            redirectAttributes.addFlashAttribute("alert", new Alert(
                "danger",
                messageSource.getMessage(
                    "message.post_not_found",
                    null,
                    LocaleContextHolder.getLocale()
                )
            ));

            return new ModelAndView("redirect:/");
        }
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
