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
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import javax.validation.Valid;

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

    @PreAuthorize("hasPermission(#commentId, 'Comment', 'edit')")
    @PatchMapping("/api/posts/{postId}/comments/{commentId}")
    public ResponseEntity<PostsCommentApiResponse.Comment> updateComment(
        Authentication authentication,
        @PathVariable Long postId,
        @PathVariable Long commentId,
        @Valid @RequestBody CommentDto commentDto
    ) {
        try {
            if (!postService.existsById(postId)) {
                throw new PostNotFoundException();
            }

            PostsCommentApiResponse.Comment comment = commentService.updateComment(
                authentication,
                commentId,
                commentDto
            );

            return ResponseEntity.ok(comment);
        } catch (PostNotFoundException | CommentNotFoundException exception) {
            return ResponseEntity.notFound().build();
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
