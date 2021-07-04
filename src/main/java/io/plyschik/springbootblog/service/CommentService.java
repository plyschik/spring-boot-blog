package io.plyschik.springbootblog.service;

import io.plyschik.springbootblog.dto.CategoryDto;
import io.plyschik.springbootblog.dto.CommentDto;
import io.plyschik.springbootblog.entity.Category;
import io.plyschik.springbootblog.entity.Comment;
import io.plyschik.springbootblog.entity.Post;
import io.plyschik.springbootblog.entity.User;
import io.plyschik.springbootblog.exception.CategoryAlreadyExists;
import io.plyschik.springbootblog.exception.CategoryNotFound;
import io.plyschik.springbootblog.exception.CommentNotFound;
import io.plyschik.springbootblog.repository.CommentRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.Date;

@Service
@RequiredArgsConstructor
public class CommentService {
    private final CommentRepository commentRepository;

    public Comment getById(Long id) throws CommentNotFound {
        return commentRepository.findById(id).orElseThrow(CommentNotFound::new);
    }

    public Page<Comment> getCommentsByPost(Post post, Pageable pageable) {
        return commentRepository.findAllByPostOrderByCreatedAtDesc(post, pageable);
    }

    public boolean existsById(Long id) {
        return commentRepository.existsById(id);
    }

    public CommentDto getCommentForEdit(Long id) throws CommentNotFound {
        Comment comment = commentRepository.findById(id).orElseThrow(CommentNotFound::new);

        CommentDto commentDto = new CommentDto();
        commentDto.setContent(comment.getContent());

        return commentDto;
    }

    public void createComment(CommentDto commentDto, User user, Post post) {
        Comment comment = new Comment();
        comment.setContent(commentDto.getContent());
        comment.setCreatedAt(new Date());
        comment.setUser(user);
        comment.setPost(post);

        commentRepository.save(comment);
    }

    public void updateComment(long id, CommentDto commentDto) throws CommentNotFound {
        Comment comment = commentRepository.findById(id).orElseThrow(CommentNotFound::new);
        comment.setContent(commentDto.getContent());

        commentRepository.save(comment);
    }

    public void deleteComment(long id) throws CommentNotFound {
        Comment comment = commentRepository.findById(id).orElseThrow(CommentNotFound::new);

        commentRepository.delete(comment);
    }
}
