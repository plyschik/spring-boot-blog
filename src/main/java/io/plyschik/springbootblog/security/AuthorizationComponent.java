package io.plyschik.springbootblog.security;

import io.plyschik.springbootblog.entity.Comment;
import io.plyschik.springbootblog.exception.CommentNotFoundException;
import io.plyschik.springbootblog.repository.CommentRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.stereotype.Component;

import java.util.Date;
import java.util.concurrent.TimeUnit;

@Component("AuthorizationComponent")
@RequiredArgsConstructor
public class AuthorizationComponent {
    private final CommentRepository commentRepository;

    public boolean canAuthenticatedUserEditComment(
        org.springframework.security.core.userdetails.User principal,
        Long commentId
    ) {
        if (principal.getAuthorities().contains(new SimpleGrantedAuthority("ROLE_MODERATOR"))) {
            return true;
        }

        if (principal.getAuthorities().contains(new SimpleGrantedAuthority("ROLE_ADMINISTRATOR"))) {
            return true;
        }

        try {
            Comment comment = commentRepository.findById(commentId).orElseThrow(CommentNotFoundException::new);

            return comment.getUser().getEmail().equals(principal.getUsername());
        } catch (CommentNotFoundException exception) {
            return false;
        }
    }

    public boolean canAuthenticatedUserDeleteComment(
        org.springframework.security.core.userdetails.User principal,
        Long commentId
    ) {
        if (principal.getAuthorities().contains(new SimpleGrantedAuthority("ROLE_MODERATOR"))) {
            return true;
        }

        if (principal.getAuthorities().contains(new SimpleGrantedAuthority("ROLE_ADMINISTRATOR"))) {
            return true;
        }

        try {
            Comment comment = commentRepository.findById(commentId).orElseThrow(CommentNotFoundException::new);

            long differenceBetweenCurrentDateAndCommentCreatedDate = TimeUnit.MILLISECONDS.toMinutes(
                new Date().getTime() - comment.getCreatedAt().getTime()
            );

            if (comment.getUser().getEmail().equals(principal.getUsername())) {
                if (differenceBetweenCurrentDateAndCommentCreatedDate <= 5) {
                    return true;
                }
            }

            return false;
        } catch (CommentNotFoundException exception) {
            return false;
        }
    }
}
