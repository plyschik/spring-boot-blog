package io.plyschik.springbootblog.service;

import io.plyschik.springbootblog.dto.CommentDto;
import io.plyschik.springbootblog.dto.PostsCommentApiResponse;
import io.plyschik.springbootblog.entity.Comment;
import io.plyschik.springbootblog.entity.Post;
import io.plyschik.springbootblog.entity.User;
import io.plyschik.springbootblog.exception.CommentNotFoundException;
import io.plyschik.springbootblog.exception.PostNotFoundException;
import io.plyschik.springbootblog.repository.CommentRepository;
import io.plyschik.springbootblog.security.CommentPermissionsChecker;
import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;

import java.util.List;
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

    public PostsCommentApiResponse getCommentsByPostId(long postId, Pageable pageable, Authentication authentication) throws PostNotFoundException {
        if (!commentRepository.existsById(postId)) {
            throw new PostNotFoundException();
        }

        Page<Comment> commentsFromDatabase = commentRepository.findAllByPostId(postId, pageable);

        List<PostsCommentApiResponse.Comment> comments = commentsFromDatabase.stream()
            .map(comment -> {
                PostsCommentApiResponse.Comment commentDto = modelMapper.map(comment, PostsCommentApiResponse.Comment.class);
                commentDto.setCanEdit(commentPermissionsChecker.checkCommentEditPermissions(authentication, comment.getId()));
                commentDto.setCanDelete(commentPermissionsChecker.checkCommentDeletePermissions(authentication, comment.getId()));

                return commentDto;
            })
            .collect(Collectors.toList());

        PostsCommentApiResponse.Pagination pagination = PostsCommentApiResponse.Pagination.builder()
            .currentPage(commentsFromDatabase.getNumber())
            .totalPages(commentsFromDatabase.getTotalPages())
            .numberOfElements(commentsFromDatabase.getNumberOfElements())
            .totalElements(commentsFromDatabase.getTotalElements())
            .pageSize(commentsFromDatabase.getSize())
            .hasPreviousPage(commentsFromDatabase.hasPrevious())
            .hasNextPage(commentsFromDatabase.hasNext())
            .build();

        return PostsCommentApiResponse.builder()
            .comments(comments)
            .pagination(pagination)
            .build();
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
