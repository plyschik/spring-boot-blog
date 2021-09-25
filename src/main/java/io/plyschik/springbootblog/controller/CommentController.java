package io.plyschik.springbootblog.controller;

import io.plyschik.springbootblog.dto.CommentDto;
import io.plyschik.springbootblog.dto.PostsCommentApiResponse;
import io.plyschik.springbootblog.exception.PostNotFoundException;
import io.plyschik.springbootblog.exception.UserNotFoundException;
import io.plyschik.springbootblog.service.CommentService;
import io.plyschik.springbootblog.service.PostService;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;

@Controller
@RequiredArgsConstructor
public class CommentController {
    private final PostService postService;
    private final CommentService commentService;

    @GetMapping("/api/posts/{postId:^[1-9][0-9]*$}/comments")
    public ResponseEntity<PostsCommentApiResponse> getPostComments(
        @PathVariable long postId,
        @RequestParam(required = false, defaultValue = "0") int page,
        Authentication authentication
    ) {
        PostsCommentApiResponse response = commentService.getCommentsByPostId(
            postId,
            PageRequest.of(page, 5, Sort.by(Sort.Order.desc("id"))),
            authentication
        );

        return ResponseEntity.ok(response);
    }

    @PostMapping("/api/posts/{postId:^[1-9][0-9]*$}/comments")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<PostsCommentApiResponse.Comment> createComment(
        @PathVariable long postId,
        @Valid @RequestBody CommentDto commentDto,
        Authentication authentication
    ) {
        PostsCommentApiResponse.Comment response = commentService.createComment(
            postId,
            commentDto,
            authentication
        );

        return ResponseEntity.ok(response);
    }

    @PreAuthorize("hasPermission(#commentId, 'Comment', 'edit')")
    @PatchMapping("/api/posts/{postId}/comments/{commentId}")
    public ResponseEntity<PostsCommentApiResponse.Comment> updateComment(
        @PathVariable long postId,
        @PathVariable long commentId,
        @Valid @RequestBody CommentDto commentDto,
        Authentication authentication
    ) {
        if (!postService.existsById(postId)) {
            throw new PostNotFoundException();
        }

        PostsCommentApiResponse.Comment response = commentService.updateComment(
            commentId,
            commentDto,
            authentication
        );

        return ResponseEntity.ok(response);
    }

    @PreAuthorize("hasPermission(#commentId, 'Comment', 'delete')")
    @DeleteMapping("/api/posts/{postId}/comments/{commentId}")
    public ResponseEntity<Void> deleteComment(@PathVariable long postId, @PathVariable long commentId) {
        if (!postService.existsById(postId)) {
            throw new PostNotFoundException();
        }

        commentService.deleteCommentById(commentId);

        return ResponseEntity.noContent().build();
    }

    @ExceptionHandler({
        UserNotFoundException.class,
        PostNotFoundException.class,
        EmptyResultDataAccessException.class
    })
    public ResponseEntity<?> handleResourceNotFoundException() {
        return ResponseEntity.notFound().build();
    }
}
