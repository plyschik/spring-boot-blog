package io.plyschik.springbootblog.service;

import io.plyschik.springbootblog.entity.User;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.Collections;

@Service
@RequiredArgsConstructor
public class UserDetailsServiceImplementation implements UserDetailsService {
    private final UserService userService;

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        User user = userService.getUserByEmail(username);

        return new org.springframework.security.core.userdetails.User(
            user.getEmail(),
            user.getPassword(),
            user.isActivated(),
            true,
            true,
            true,
            Collections.singletonList(new SimpleGrantedAuthority(user.getRole().toString()))
        );
    }
}
