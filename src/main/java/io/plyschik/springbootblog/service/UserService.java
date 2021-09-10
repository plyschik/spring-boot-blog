package io.plyschik.springbootblog.service;

import io.plyschik.springbootblog.dto.ForgotPasswordDto;
import io.plyschik.springbootblog.dto.PasswordResetDto;
import io.plyschik.springbootblog.dto.UserDto;
import io.plyschik.springbootblog.dto.UserWithPostsCount;
import io.plyschik.springbootblog.entity.PasswordResetToken;
import io.plyschik.springbootblog.entity.User;
import io.plyschik.springbootblog.entity.User.Role;
import io.plyschik.springbootblog.entity.VerificationToken;
import io.plyschik.springbootblog.exception.*;
import io.plyschik.springbootblog.repository.PasswordResetTokenRepository;
import io.plyschik.springbootblog.repository.UserRepository;
import io.plyschik.springbootblog.repository.VerificationTokenRepository;
import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.data.domain.Sort;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.scheduling.annotation.Async;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;
import org.thymeleaf.context.Context;
import org.thymeleaf.spring5.SpringTemplateEngine;

import javax.mail.MessagingException;
import javax.mail.internet.MimeMessage;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class UserService {
    private final ModelMapper modelMapper;
    private final SpringTemplateEngine templateEngine;
    private final JavaMailSender mailSender;
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final VerificationTokenRepository verificationTokenRepository;
    private final PasswordResetTokenRepository passwordResetTokenRepository;

    public User getUserById(Long id) {
        return userRepository.findById(id).orElseThrow(UserNotFoundException::new);
    }

    public User getUserByEmail(String email) {
        return userRepository.findByEmail(email).orElseThrow(() ->
            new UsernameNotFoundException(String.format("Username %s not found.", email))
        );
    }

    public List<UserWithPostsCount> getUsersWithPostsCount(Sort sort) {
        return userRepository.findAllWithPostsCount(sort);
    }

    public void signUp(UserDto userDto, Role role) throws EmailAddressIsAlreadyTakenException, MessagingException {
        if (!isUserEmailUnique(userDto.getEmail())) {
            throw new EmailAddressIsAlreadyTakenException();
        }

        User user = modelMapper.map(userDto, User.class);
        user.setPassword(passwordEncoder.encode(userDto.getPassword()));
        user.setRole(role);
        userRepository.save(user);

        String verificationToken = createAndPersistVerificationToken(user);
        sendActivationEmail(user, verificationToken);
    }

    public boolean isUserEmailUnique(String email) {
        return !userRepository.existsByEmail(email);
    }

    private String createAndPersistVerificationToken(User user) {
        String token;

        do {
            token = generateRandomToken();
        } while (verificationTokenRepository.existsByToken(token));

        VerificationToken verificationToken = new VerificationToken();
        verificationToken.setToken(token);
        verificationToken.setUser(user);
        verificationTokenRepository.save(verificationToken);

        return token;
    }

    @Async
    protected void sendActivationEmail(User user, String token) throws MessagingException {
        String baseUrl = ServletUriComponentsBuilder.fromCurrentContextPath().build().toUriString();

        Context context = new Context();
        context.setVariable("userFullName", user.fullName());
        context.setVariable("link", String.format("%s/verification/%s", baseUrl, token));
        String template = templateEngine.process("email/account_activation", context);

        MimeMessage message = mailSender.createMimeMessage();
        MimeMessageHelper mimeMessageHelper = new MimeMessageHelper(message, "utf-8");
        mimeMessageHelper.setTo(user.getEmail());
        mimeMessageHelper.setSubject("Account activation");
        mimeMessageHelper.setText(template, true);

        mailSender.send(message);
    }

    public void processAccountActivation(String token) throws VerificationTokenNotFoundException {
        VerificationToken verificationToken = verificationTokenRepository.findByToken(token)
            .orElseThrow(VerificationTokenNotFoundException::new);

        if (verificationToken.isExpired()) {
            verificationTokenRepository.delete(verificationToken);

            throw new VerificationTokenExpiredException();
        }

        User user = verificationToken.getUser();
        user.setActivated(true);
        userRepository.save(user);

        verificationTokenRepository.delete(verificationToken);
    }

    public void processPasswordResetRequest(ForgotPasswordDto forgotPasswordDto)
        throws UserNotFoundException, PasswordResetRequestHasBeenAlreadySentException, MessagingException {
        User user = userRepository.findByEmail(forgotPasswordDto.getEmail()).orElseThrow(UserNotFoundException::new);

        if (passwordResetTokenRepository.existsByUser(user)) {
            throw new PasswordResetRequestHasBeenAlreadySentException();
        }

        String token = createAndPersistPasswordResetToken(user);
        sendPasswordResetRequestEmail(user, token);
    }

    private String createAndPersistPasswordResetToken(User user) {
        String token;

        do {
            token = generateRandomToken();
        } while (passwordResetTokenRepository.existsByToken(token));

        PasswordResetToken passwordResetToken = new PasswordResetToken();
        passwordResetToken.setToken(token);
        passwordResetToken.setUser(user);

        passwordResetTokenRepository.save(passwordResetToken);

        return token;
    }

    @Async
    protected void sendPasswordResetRequestEmail(User user, String token) throws MessagingException {
        String baseUrl = ServletUriComponentsBuilder.fromCurrentContextPath().build().toUriString();

        Context context = new Context();
        context.setVariable("userFullName", user.fullName());
        context.setVariable("link", String.format("%s/auth/password-reset/%s", baseUrl, token));
        String template = templateEngine.process("email/password_reset", context);

        MimeMessage message = mailSender.createMimeMessage();
        MimeMessageHelper mimeMessageHelper = new MimeMessageHelper(message, "utf-8");
        mimeMessageHelper.setTo(user.getEmail());
        mimeMessageHelper.setSubject("Password reset");
        mimeMessageHelper.setText(template, true);

        mailSender.send(message);
    }

    public boolean isPasswordResetTokenValid(String token) {
        Optional<PasswordResetToken> passwordResetToken = passwordResetTokenRepository.findByToken(token);
        if (passwordResetToken.isEmpty()) {
            return false;
        }

        return !passwordResetToken.get().isExpired();
    }

    public void updatePasswordByPasswordResetToken(String token, PasswordResetDto passwordResetDto) {
        PasswordResetToken passwordResetToken = passwordResetTokenRepository.getByToken(token);

        User user = passwordResetToken.getUser();
        user.setPassword(passwordEncoder.encode(passwordResetDto.getPassword()));
        userRepository.save(user);

        passwordResetTokenRepository.delete(passwordResetToken);
    }

    private String generateRandomToken() {
        return UUID.randomUUID().toString();
    }
}
