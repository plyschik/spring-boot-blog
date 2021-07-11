package io.plyschik.springbootblog.service;

import io.plyschik.springbootblog.dto.CommentDto;
import io.plyschik.springbootblog.dto.api.CommentApiDto;
import io.plyschik.springbootblog.dto.api.CommentsResponse;
import io.plyschik.springbootblog.dto.api.PaginationApiDto;
import io.plyschik.springbootblog.dto.api.UserApiDto;
import io.plyschik.springbootblog.entity.Comment;
import io.plyschik.springbootblog.entity.Post;
import io.plyschik.springbootblog.entity.User;
import io.plyschik.springbootblog.exception.CommentNotFoundException;
import io.plyschik.springbootblog.repository.CommentRepository;
import io.plyschik.springbootblog.security.CommentPermissionsChecker;
import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;

import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class CommentService {
    private final ModelMapper modelMapper;
    private final CommentRepository commentRepository;
    private final CommentPermissionsChecker commentPermissionsChecker;

    public Comment getComment(long id) throws CommentNotFoundException {
        return commentRepository.findById(id).orElseThrow(CommentNotFoundException::new);
    }

    public Page<Comment> getCommentsByPost(Post post, Pageable pageable) {
        return commentRepository.findAllByPostOrderByCreatedAtDesc(post, pageable);
    }

    public CommentsResponse getCommentsByPost(Post post, Pageable pageable, Authentication authentication) {
        Page<Comment> comments = commentRepository.findAllByPostOrderByCreatedAtDesc(post, pageable);

        CommentsResponse response = new CommentsResponse();
        response.setComments(comments.stream().map(comment -> {
            CommentApiDto commentApiDto = new CommentApiDto();
            commentApiDto.setId(comment.getId());
            commentApiDto.setContent(comment.getContent());
            commentApiDto.setCreatedAt(comment.getCreatedAt());

            User user = comment.getUser();
            UserApiDto userApiDto = new UserApiDto();
            userApiDto.setFirstName(user.getFirstName());
            userApiDto.setLastName(user.getLastName());
            commentApiDto.setUser(userApiDto);

            commentApiDto.setCanEdit(commentPermissionsChecker.checkCommentEditPermissions(authentication, comment.getId()));
            commentApiDto.setCanEdit(commentPermissionsChecker.checkCommentDeletePermissions(authentication, comment.getId()));

            return commentApiDto;
        }).collect(Collectors.toList()));

        PaginationApiDto paginationApiDto = new PaginationApiDto();
        paginationApiDto.setCurrentPage(comments.getNumber());
        paginationApiDto.setTotalPages(comments.getTotalPages());
        paginationApiDto.setHasPreviousPage(comments.hasPrevious());
        paginationApiDto.setHasNextPage(comments.hasNext());
        response.setPagination(paginationApiDto);

        return response;
    }

    public CommentDto getCommentForEdit(long id) throws CommentNotFoundException {
        Comment comment = commentRepository.findById(id).orElseThrow(CommentNotFoundException::new);

        return modelMapper.map(comment, CommentDto.class);
    }

    public void createComment(CommentDto commentDto, User user, Post post) {
        Comment comment = modelMapper.map(commentDto, Comment.class);
        comment.setUser(user);
        comment.setPost(post);

        commentRepository.save(comment);
    }

    public void updateComment(long id, CommentDto commentDto) throws CommentNotFoundException {
        Comment comment = commentRepository.findById(id).orElseThrow(CommentNotFoundException::new);
        modelMapper.map(commentDto, comment);

        commentRepository.save(comment);
    }

    public void deleteComment(long id) {
        commentRepository.deleteById(id);
    }
}
