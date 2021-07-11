package io.plyschik.springbootblog.security;

import lombok.RequiredArgsConstructor;
import org.springframework.security.access.PermissionEvaluator;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Component;

import java.io.Serializable;

@Component
@RequiredArgsConstructor
public class ApplicationPermissionEvaluator implements PermissionEvaluator {
    private final CommentPermissionsChecker commentPermissionsChecker;

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
            return commentPermissionsChecker.checkCommentEditPermissions(
                authentication,
                Long.valueOf(String.valueOf(targetId))
            );
        }

        if (targetType.equals("Comment") && permission.equals("delete")) {
            return commentPermissionsChecker.checkCommentDeletePermissions(
                authentication,
                Long.valueOf(String.valueOf(targetId))
            );
        }

        return false;
    }
}
