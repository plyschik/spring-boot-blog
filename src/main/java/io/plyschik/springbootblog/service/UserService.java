package io.plyschik.springbootblog.service;

import io.plyschik.springbootblog.dto.UserDto;
import io.plyschik.springbootblog.entity.Role;
import io.plyschik.springbootblog.entity.User;
import io.plyschik.springbootblog.exception.EmailAddressIsAlreadyTaken;
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

    public Optional<User> getUserById(Long id) {
        return userRepository.findById(id);
    }

    public Optional<User> getByEmail(String email) {
        return userRepository.findByEmail(email);
    }

    public User getUserByEmail(String email) {
        return userRepository.findByEmail(email)
            .orElseThrow(() -> new UsernameNotFoundException(String.format("Username %s not found.", email)));
    }

    public void signUp(UserDto userDto, Role role) throws EmailAddressIsAlreadyTaken {
        if (!isAccountEmailUnique(userDto.getEmail())) {
            throw new EmailAddressIsAlreadyTaken();
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
