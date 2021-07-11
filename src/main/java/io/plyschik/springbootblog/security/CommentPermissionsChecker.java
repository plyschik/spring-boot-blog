package io.plyschik.springbootblog.security;

import io.plyschik.springbootblog.entity.Comment;
import io.plyschik.springbootblog.exception.CommentNotFoundException;
import io.plyschik.springbootblog.repository.CommentRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;

@Component
@RequiredArgsConstructor
public class CommentPermissionsChecker {
    private final CommentRepository commentRepository;

    public boolean checkCommentEditPermissions(Authentication authentication, Long commentId) {
        if (authentication == null) {
            return false;
        }

        if (authentication.getAuthorities().contains(new SimpleGrantedAuthority("ROLE_MODERATOR"))) {
            return true;
        }

        if (authentication.getAuthorities().contains(new SimpleGrantedAuthority("ROLE_ADMINISTRATOR"))) {
            return true;
        }

        try {
            Comment comment = commentRepository.findById(commentId).orElseThrow(CommentNotFoundException::new);

            long differenceBetweenCurrentDateAndCommentCreatedDate = ChronoUnit.SECONDS.between(
                comment.getCreatedAt(),
                LocalDateTime.now()
            );

            if (comment.getUser().getEmail().equals(authentication.getName())) {
                if (differenceBetweenCurrentDateAndCommentCreatedDate <= 120) {
                    return true;
                }
            }

            return false;
        } catch (CommentNotFoundException exception) {
            return false;
        }
    }

    public boolean checkCommentDeletePermissions(Authentication authentication, Long commentId) {
        if (authentication == null) {
            return false;
        }

        if (authentication.getAuthorities().contains(new SimpleGrantedAuthority("ROLE_MODERATOR"))) {
            return true;
        }

        if (authentication.getAuthorities().contains(new SimpleGrantedAuthority("ROLE_ADMINISTRATOR"))) {
            return true;
        }

        try {
            Comment comment = commentRepository.findById(commentId).orElseThrow(CommentNotFoundException::new);

            return comment.getUser().getEmail().equals(authentication.getName());
        } catch (CommentNotFoundException exception) {
            return false;
        }
    }
}
