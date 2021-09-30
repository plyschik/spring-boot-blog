package io.plyschik.springbootblog.service;

import io.plyschik.springbootblog.exception.PostNotFoundException;
import io.plyschik.springbootblog.repository.CommentRepository;
import io.plyschik.springbootblog.security.CommentPermissionsChecker;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.modelmapper.ModelMapper;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.Authentication;

@ExtendWith(MockitoExtension.class)
class CommentServiceTest {
    @Mock
    private ModelMapper modelMapper;

    @Mock
    private UserService userService;

    @Mock
    private PostService postService;

    @Mock
    private CommentRepository commentRepository;

    @Mock
    private CommentPermissionsChecker commentPermissionsChecker;

    @InjectMocks
    private CommentService commentService;

    @Test
    public void getCommentsByPostIdShouldThrowPostNotFoundExceptionWhenPostNotExists() {
        Mockito.when(postService.existsById(1)).thenReturn(false);

        Assertions.assertThrows(
            PostNotFoundException.class,
            () -> commentService.getCommentsByPostId(
                1,
                Pageable.unpaged(),
                Mockito.mock(Authentication.class)
            )
        );

        Mockito.verify(postService, Mockito.times(1)).existsById(1L);
    }

    @Test
    public void deleteCommentShouldDeleteCommentById() {
        commentService.deleteCommentById(1);

        Mockito.verify(commentRepository, Mockito.times(1)).deleteById(1L);
    }
}
