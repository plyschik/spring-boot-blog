package io.plyschik.springbootblog.security;

import io.plyschik.springbootblog.entity.Comment;
import io.plyschik.springbootblog.entity.User;
import io.plyschik.springbootblog.exception.CommentNotFound;
import io.plyschik.springbootblog.repository.CommentRepository;
import io.plyschik.springbootblog.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Component("AuthorizationComponent")
@RequiredArgsConstructor
public class AuthorizationComponent {
    private final UserRepository userRepository;
    private final CommentRepository commentRepository;

    public boolean canAuthenticatedUserEditComment(org.springframework.security.core.userdetails.User principal, Long commentId) {
        if (principal.getAuthorities().contains(new SimpleGrantedAuthority("ROLE_MODERATOR"))) {
            return true;
        }

        if (principal.getAuthorities().contains(new SimpleGrantedAuthority("ROLE_ADMINISTRATOR"))) {
            return true;
        }

        try {
            Comment comment = commentRepository.findById(commentId).orElseThrow(CommentNotFound::new);

            return comment.getUser().getEmail().equals(principal.getUsername());
        } catch (CommentNotFound exception) {
            return false;
        }
    }
}
