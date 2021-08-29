package io.plyschik.springbootblog.controller;

import io.plyschik.springbootblog.dto.Alert;
import io.plyschik.springbootblog.dto.CommentDto;
import io.plyschik.springbootblog.dto.PostsCommentApiResponse;
import io.plyschik.springbootblog.exception.CommentNotFoundException;
import io.plyschik.springbootblog.exception.PostNotFoundException;
import io.plyschik.springbootblog.exception.UserNotFoundException;
import io.plyschik.springbootblog.service.CommentService;
import io.plyschik.springbootblog.service.PostService;
import io.plyschik.springbootblog.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.context.MessageSource;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.data.web.SortDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import javax.validation.Valid;
import java.security.Principal;

@Controller
@RequiredArgsConstructor
public class CommentController {
    private final UserService userService;
    private final PostService postService;
    private final CommentService commentService;
    private final MessageSource messageSource;

    @GetMapping("/api/posts/{id}/comments")
    public ResponseEntity<PostsCommentApiResponse> getPostComments(
        @PathVariable Long id,
        @PageableDefault(size = 5) @SortDefault(sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable,
        Authentication authentication
    ) {
        try {
            PostsCommentApiResponse response = commentService.getCommentsByPostId(id, pageable, authentication);

            return ResponseEntity.ok(response);
        } catch (PostNotFoundException exception) {
            return ResponseEntity.notFound().build();
        }
    }

    @PostMapping("/api/posts/{postId}/comments")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<PostsCommentApiResponse.Comment> createComment(
        Authentication authentication,
        @PathVariable long postId,
        @Valid @RequestBody CommentDto commentDto
    ) {
        try {
            PostsCommentApiResponse.Comment comment = commentService.createComment(
                authentication,
                postId,
                commentDto
            );

            return ResponseEntity.ok(comment);
        } catch (UserNotFoundException | PostNotFoundException exception) {
            return ResponseEntity.badRequest().build();
        }
    }

    @PostMapping("/posts/{id}/comments")
    @PreAuthorize("isAuthenticated()")
    public ModelAndView processCommentCreateForm(
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
                userService.getUserByEmail(principal.getName()),
                postService.getPostById(postId)
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
        } catch (UserNotFoundException exception) {
            redirectAttributes.addFlashAttribute("alert", new Alert(
                "danger",
                messageSource.getMessage(
                    "message.user_not_found",
                    null,
                    LocaleContextHolder.getLocale()
                )
            ));

            return new ModelAndView("redirect:/");
        } catch (PostNotFoundException exception) {
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

    @PreAuthorize("hasPermission(#commentId, 'Comment', 'edit')")
    @GetMapping("/posts/{postId}/comments/{commentId}/edit")
    public ModelAndView commentEditForm(@PathVariable Long postId, @PathVariable Long commentId, Model model) {
        try {
            ModelAndView modelAndView = new ModelAndView("comment/edit");
            modelAndView.addObject("postId", postId);
            modelAndView.addObject("commentId", commentId);

            if (!model.containsAttribute("comment")) {
                modelAndView.addObject("comment", commentService.getCommentForEdit(commentId));
            }

            return modelAndView;
        } catch (CommentNotFoundException exception) {
            return new ModelAndView(String.format("redirect:/posts/%d", postId));
        }
    }

    @PreAuthorize("hasPermission(#commentId, 'Comment', 'edit')")
    @PostMapping("/posts/{postId}/comments/{commentId}/edit")
    public ModelAndView processEditForm(
        @PathVariable Long postId,
        @PathVariable Long commentId,
        @ModelAttribute("comment") @Valid CommentDto commentDto,
        BindingResult bindingResult,
        RedirectAttributes redirectAttributes
    ) {
        try {
            if (bindingResult.hasErrors()) {
                redirectAttributes.addFlashAttribute("comment", commentDto);
                redirectAttributes.addFlashAttribute(
                    "org.springframework.validation.BindingResult.comment",
                    bindingResult
                );

                return new ModelAndView(String.format("redirect:/posts/%d/comments/%d/edit", postId, commentId));
            }

            commentService.updateComment(commentId, commentDto);

            redirectAttributes.addFlashAttribute(
                "alert",
                new Alert("success", messageSource.getMessage(
                    "message.comment_has_been_successfully_updated",
                    null,
                    LocaleContextHolder.getLocale()
                ))
            );

            return new ModelAndView(String.format("redirect:/posts/%d", postId));
        } catch (CommentNotFoundException exception) {
            redirectAttributes.addFlashAttribute(
                "alert",
                new Alert("danger", messageSource.getMessage(
                    "message.comment_not_found",
                    null,
                    LocaleContextHolder.getLocale()
                ))
            );

            return new ModelAndView(String.format("redirect:/posts/%d", postId));
        }
    }

    @PreAuthorize("hasPermission(#commentId, 'Comment', 'delete')")
    @GetMapping("/posts/{postId}/comments/{commentId}/delete")
    public ModelAndView deleteConfirmation(
        @PathVariable long postId,
        @PathVariable long commentId,
        RedirectAttributes redirectAttributes
    ) {
        try {
            ModelAndView modelAndView = new ModelAndView("comment/delete");
            modelAndView.addObject("postId", postId);
            modelAndView.addObject("comment", commentService.getComment(commentId));

            return modelAndView;
        } catch (CommentNotFoundException exception) {
            redirectAttributes.addFlashAttribute(
                "alert",
                new Alert("danger", messageSource.getMessage(
                    "message.comment_not_found",
                    null,
                    LocaleContextHolder.getLocale()
                ))
            );

            return new ModelAndView(String.format("redirect:/posts/%d", postId));
        }
    }

    @PreAuthorize("hasPermission(#commentId, 'Comment', 'delete')")
    @PostMapping("/posts/{postId}/comments/{commentId}/delete")
    public ModelAndView delete(
        @PathVariable long postId,
        @PathVariable long commentId,
        RedirectAttributes redirectAttributes
    ) {
        try {
            commentService.deleteComment(commentId);

            redirectAttributes.addFlashAttribute(
                "alert",
                new Alert("success", messageSource.getMessage(
                    "message.comment_has_been_successfully_deleted",
                    null,
                    LocaleContextHolder.getLocale()
                ))
            );

            return new ModelAndView(String.format("redirect:/posts/%d", postId));
        } catch (EmptyResultDataAccessException exception) {
            redirectAttributes.addFlashAttribute(
                "alert",
                new Alert("danger", messageSource.getMessage(
                    "message.comment_not_found",
                    null,
                    LocaleContextHolder.getLocale()
                ))
            );

            return new ModelAndView(String.format("redirect:/posts/%d", postId));
        }
    }
}
