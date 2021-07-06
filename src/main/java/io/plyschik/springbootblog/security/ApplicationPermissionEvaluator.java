package io.plyschik.springbootblog.security;

import io.plyschik.springbootblog.entity.Comment;
import io.plyschik.springbootblog.exception.CommentNotFoundException;
import io.plyschik.springbootblog.repository.CommentRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.PermissionEvaluator;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.stereotype.Component;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;

@Component
@RequiredArgsConstructor
public class ApplicationPermissionEvaluator implements PermissionEvaluator {
    private final CommentRepository commentRepository;

    @Override
    public boolean hasPermission(Authentication authentication, Object target, Object permission) {
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean hasPermission(
        Authentication authentication,
        Serializable targetId,
        String targetType,
        Object permission
    ) {
        if (authentication == null || !authentication.isAuthenticated()) {
            return false;
        }

        if (targetType.equals("Comment") && permission.equals("edit")) {
            return checkCommentEditPermissions(authentication, Long.valueOf(String.valueOf(targetId)));
        }

        if (targetType.equals("Comment") && permission.equals("delete")) {
            return checkCommentDeletePermissions(authentication, Long.valueOf(String.valueOf(targetId)));
        }

        return false;
    }

    private boolean checkCommentEditPermissions(Authentication authentication, Long commentId) {
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

            System.out.println(differenceBetweenCurrentDateAndCommentCreatedDate);

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

    private boolean checkCommentDeletePermissions(Authentication authentication, Long commentId) {
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
