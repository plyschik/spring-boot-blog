package io.plyschik.springbootblog.service;

import io.plyschik.springbootblog.entity.User;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

@ExtendWith(MockitoExtension.class)
class UserDetailsServiceImplementationTest {
    @Mock
    private UserService userService;

    @InjectMocks
    private UserDetailsServiceImplementation userDetailsServiceImplementation;

    @Test
    public void loadUserByUsernameShouldReturnUserDetailsObject() {
        User user = new User();
        user.setEmail("test@test.test");
        user.setPassword("password");
        user.setRole(User.Role.USER);

        Mockito.when(userService.getUserByEmail("test@test.test")).thenReturn(user);

        UserDetails userDetails = userDetailsServiceImplementation.loadUserByUsername("test@test.test");

        Assertions.assertThat(userDetails.getUsername()).isEqualTo("test@test.test");
        Assertions.assertThat(userDetails.getPassword()).isEqualTo("password");
        Assertions.assertThat(
            userDetails.getAuthorities().contains(new SimpleGrantedAuthority(User.Role.USER.toString()))
        ).isTrue();
        Assertions.assertThat(userDetails.isEnabled()).isFalse();
        Assertions.assertThat(userDetails.isAccountNonExpired()).isTrue();
        Assertions.assertThat(userDetails.isCredentialsNonExpired()).isTrue();
        Assertions.assertThat(userDetails.isAccountNonLocked()).isTrue();
    }
}
