package io.plyschik.springbootblog.service;

import io.plyschik.springbootblog.entity.Category;
import io.plyschik.springbootblog.entity.User;
import io.plyschik.springbootblog.exception.CategoryNotFoundException;
import io.plyschik.springbootblog.exception.UserNotFoundException;
import io.plyschik.springbootblog.repository.PasswordResetTokenRepository;
import io.plyschik.springbootblog.repository.UserRepository;
import io.plyschik.springbootblog.repository.VerificationTokenRepository;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.modelmapper.ModelMapper;
import org.springframework.data.domain.Sort;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.thymeleaf.spring5.SpringTemplateEngine;

import java.util.ArrayList;
import java.util.Optional;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {
    @Mock
    private ModelMapper modelMapper;

    @Mock
    private SpringTemplateEngine templateEngine;

    @Mock
    private JavaMailSender mailSender;

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private VerificationTokenRepository verificationTokenRepository;

    @Mock
    private PasswordResetTokenRepository passwordResetTokenRepository;

    @InjectMocks
    private UserService userService;

    @Test
    public void getUserByIdIdShouldCallFindByIdMethodFromUserRepositoryAndReturnUserWhenExists() {
        Mockito.when(userRepository.findById(1L)).thenReturn(Optional.of(new User()));

        User user = userService.getUserById(1);

        Assertions.assertNotNull(user);
        Mockito.verify(userRepository, Mockito.times(1)).findById(1L);
    }

    @Test
    public void getUserByIdShouldCallFindByIdMethodFromUserRepositoryAndThrowUserNotFoundExceptionWhenNotExists() {
        Mockito.when(userRepository.findById(1L)).thenReturn(Optional.empty());

        Assertions.assertThrows(
            UserNotFoundException.class,
            () -> userService.getUserById(1)
        );

        Mockito.verify(userRepository, Mockito.times(1)).findById(1L);
    }

    @Test
    public void getUserByEmailShouldCallFindByEmailMethodFromUserRepositoryAndReturnUserWhenExists() {
        Mockito.when(userRepository.findByEmail("test@test.test")).thenReturn(Optional.of(new User()));

        User user = userService.getUserByEmail("test@test.test");

        Assertions.assertNotNull(user);
        Mockito.verify(userRepository, Mockito.times(1)).findByEmail("test@test.test");
    }

    @Test
    public void getUserByEmailShouldCallFindByEmailMethodFromUserRepositoryAndThrowUserNotFoundExceptionWhenNotExists() {
        Mockito.when(userRepository.findByEmail("test@test.test")).thenReturn(Optional.empty());

        Assertions.assertThrows(
            UsernameNotFoundException.class,
            () -> userService.getUserByEmail("test@test.test")
        );

        Mockito.verify(userRepository, Mockito.times(1)).findByEmail("test@test.test");
    }

    @Test
    public void getUsersWithPostsCountShouldReturnSortedUsersListWithPostsCount() {
        Mockito.when(userRepository.findAllWithPostsCount(Mockito.any(Sort.class))).thenReturn(new ArrayList<>());

        userService.getUsersWithPostsCount(Sort.unsorted());

        Mockito.verify(userRepository, Mockito.times(1)).findAllWithPostsCount(Sort.unsorted());
    }

    @Test
    public void isUserEmailUniqueShouldCallExistsByEmailFromUserRepository() {
        Mockito.when(userRepository.existsByEmail("test@test.test")).thenReturn(false);
        Assertions.assertTrue(userService.isUserEmailUnique("test@test.test"));

        Mockito.when(userRepository.existsByEmail("test@test.test")).thenReturn(true);
        Assertions.assertFalse(userService.isUserEmailUnique("test@test.test"));

        Mockito.verify(userRepository, Mockito.times(2)).existsByEmail("test@test.test");
    }
}
