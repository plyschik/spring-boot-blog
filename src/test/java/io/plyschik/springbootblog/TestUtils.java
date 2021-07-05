package io.plyschik.springbootblog;

import io.plyschik.springbootblog.entity.*;
import io.plyschik.springbootblog.entity.User.Role;
import io.plyschik.springbootblog.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.Date;

@Component
@RequiredArgsConstructor
public class TestUtils {
    private final UserRepository userRepository;
    private final PostRepository postRepository;
    private final CategoryRepository categoryRepository;
    private final TagRepository tagRepository;
    private final CommentRepository commentRepository;

    public User createUser(String email, String password, String firstName, String lastName, Role role) {
        User user = new User();
        user.setEmail(email);
        user.setPassword(password);
        user.setFirstName(firstName);
        user.setLastName(lastName);
        user.setRole(role);

        return userRepository.save(user);
    }

    public Post createPost(String title, String content, Date date, User user) {
        Post post = new Post();
        post.setTitle(title);
        post.setContent(content);
//        post.setCreatedAt(date);
        post.setUser(user);

        return postRepository.save(post);
    }

    public Category createCategory(String name) {
        Category category = new Category();
        category.setName(name);

        return categoryRepository.save(category);
    }

    public Tag createTag(String name) {
        Tag tag = new Tag();
        tag.setName(name);

        return tagRepository.save(tag);
    }

    public Comment createComment(String content, Date date, User user, Post post) {
        Comment comment = new Comment();
        comment.setContent(content);
//        comment.setCreatedAt(date);
        comment.setUser(user);
        comment.setPost(post);

        return commentRepository.save(comment);
    }
}
