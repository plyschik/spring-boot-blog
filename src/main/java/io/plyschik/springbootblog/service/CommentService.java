package io.plyschik.springbootblog.service;

import io.plyschik.springbootblog.dto.CommentDto;
import io.plyschik.springbootblog.dto.PostsCommentApiResponse;
import io.plyschik.springbootblog.entity.Comment;
import io.plyschik.springbootblog.exception.CommentNotFoundException;
import io.plyschik.springbootblog.exception.PostNotFoundException;
import io.plyschik.springbootblog.exception.UserNotFoundException;
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
    private final UserService userService;
    private final PostService postService;
    private final CommentRepository commentRepository;
    private final CommentPermissionsChecker commentPermissionsChecker;

    public PostsCommentApiResponse getCommentsByPostId(
        long postId,
        Pageable pageable,
        Authentication authentication
    ) throws PostNotFoundException {
        if (!postService.existsById(postId)) {
            throw new PostNotFoundException();
        }

        Page<Comment> commentsFromDatabase = commentRepository.findAllByPostId(postId, pageable);
        List<PostsCommentApiResponse.Comment> comments = commentsFromDatabase.stream()
            .map(comment -> {
                PostsCommentApiResponse.Comment commentDto = modelMapper.map(
                    comment,
                    PostsCommentApiResponse.Comment.class
                );
                commentDto.setCanEdit(commentPermissionsChecker.checkCommentEditPermissions(
                    authentication,
                    comment.getId()
                ));
                commentDto.setCanDelete(commentPermissionsChecker.checkCommentDeletePermissions(
                    authentication,
                    comment.getId()
                ));

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

    public PostsCommentApiResponse.Comment createComment(
        long postId,
        CommentDto commentDto,
        Authentication authentication
    ) throws UserNotFoundException, PostNotFoundException {
        Comment comment = modelMapper.map(commentDto, Comment.class);
        comment.setUser(userService.getUserByEmail(authentication.getName()));
        comment.setPost(postService.getPostById(postId));
        commentRepository.save(comment);

        PostsCommentApiResponse.Comment dto = modelMapper.map(comment, PostsCommentApiResponse.Comment.class);
        dto.setCanEdit(commentPermissionsChecker.checkCommentEditPermissions(authentication, comment.getId()));
        dto.setCanDelete(commentPermissionsChecker.checkCommentDeletePermissions(authentication, comment.getId()));

        return dto;
    }

    public PostsCommentApiResponse.Comment updateComment(
        long commentId,
        CommentDto commentDto,
        Authentication authentication
    ) throws CommentNotFoundException {
        Comment comment = commentRepository.findById(commentId).orElseThrow(CommentNotFoundException::new);
        modelMapper.map(commentDto, comment);

        PostsCommentApiResponse.Comment dto = modelMapper.map(
            commentRepository.save(comment),
            PostsCommentApiResponse.Comment.class
        );
        dto.setCanEdit(commentPermissionsChecker.checkCommentEditPermissions(authentication, comment.getId()));
        dto.setCanDelete(commentPermissionsChecker.checkCommentDeletePermissions(authentication, comment.getId()));

        return dto;
    }

    public void deleteCommentById(long id) {
        commentRepository.deleteById(id);
    }
}
