package io.plyschik.springbootblog.service;

import io.plyschik.springbootblog.dto.UserDto;
import io.plyschik.springbootblog.entity.User;
import io.plyschik.springbootblog.entity.User.Role;
import io.plyschik.springbootblog.exception.EmailAddressIsAlreadyTakenException;
import io.plyschik.springbootblog.exception.UserNotFoundException;
import io.plyschik.springbootblog.repository.UserRepository;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class UserService {
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public UserService(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    public User getUserById(Long id) {
        return userRepository.findById(id).orElseThrow(UserNotFoundException::new);
    }

    public Optional<User> getByEmail(String email) {
        return userRepository.findByEmail(email);
    }

    public User getUserByEmail(String email) {
        return userRepository.findByEmail(email)
            .orElseThrow(() -> new UsernameNotFoundException(String.format("Username %s not found.", email)));
    }

    public void signUp(UserDto userDto, Role role) throws EmailAddressIsAlreadyTakenException {
        if (!isAccountEmailUnique(userDto.getEmail())) {
            throw new EmailAddressIsAlreadyTakenException();
        }

        User user = new User();
        user.setEmail(userDto.getEmail());
        user.setPassword(passwordEncoder.encode(userDto.getPassword()));
        user.setFirstName(userDto.getFirstName());
        user.setLastName(userDto.getLastName());
        user.setRole(role);

        userRepository.save(user);
    }

    public boolean isAccountEmailUnique(String email) {
        return !userRepository.existsByEmail(email);
    }
}
