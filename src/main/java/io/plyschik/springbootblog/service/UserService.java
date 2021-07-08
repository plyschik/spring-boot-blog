package io.plyschik.springbootblog.service;

import io.plyschik.springbootblog.dto.UserDto;
import io.plyschik.springbootblog.entity.User;
import io.plyschik.springbootblog.entity.User.Role;
import io.plyschik.springbootblog.entity.VerificationToken;
import io.plyschik.springbootblog.exception.EmailAddressIsAlreadyTakenException;
import io.plyschik.springbootblog.exception.UserNotFoundException;
import io.plyschik.springbootblog.repository.UserRepository;
import io.plyschik.springbootblog.repository.VerificationTokenRepository;
import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class UserService {
    private final ModelMapper modelMapper;
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final VerificationTokenRepository verificationTokenRepository;
    private final JavaMailSender mailSender;

    public User getUserById(Long id) {
        return userRepository.findById(id).orElseThrow(UserNotFoundException::new);
    }

    public User getUserByEmail(String email) {
        return userRepository.findByEmail(email).orElseThrow(() ->
            new UsernameNotFoundException(String.format("Username %s not found.", email))
        );
    }

    public void signUp(UserDto userDto, Role role) throws EmailAddressIsAlreadyTakenException {
        if (!isUserEmailUnique(userDto.getEmail())) {
            throw new EmailAddressIsAlreadyTakenException();
        }

        User user = modelMapper.map(userDto, User.class);
        user.setPassword(passwordEncoder.encode(userDto.getPassword()));
        user.setRole(role);

        userRepository.save(user);

        String token = createAndStoreVerificationToken(user);
        sendActivationEmail(user.getEmail(), token);
    }

    public boolean isUserEmailUnique(String email) {
        return !userRepository.existsByEmail(email);
    }

    private String createAndStoreVerificationToken(User user) {
        String token = UUID.randomUUID().toString();

        VerificationToken verificationToken = new VerificationToken();
        verificationToken.setToken(token);
        verificationToken.setUser(user);

        verificationTokenRepository.save(verificationToken);

        return token;
    }

    private void sendActivationEmail(String email, String token) {
        SimpleMailMessage message = new SimpleMailMessage();
        message.setTo(email);
        message.setSubject("Account activation");
        message.setText("Activation token: " + token);

        mailSender.send(message);
    }
}
