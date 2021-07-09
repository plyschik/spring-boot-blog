package io.plyschik.springbootblog.service;

import io.plyschik.springbootblog.dto.UserDto;
import io.plyschik.springbootblog.entity.User;
import io.plyschik.springbootblog.entity.User.Role;
import io.plyschik.springbootblog.entity.VerificationToken;
import io.plyschik.springbootblog.exception.EmailAddressIsAlreadyTakenException;
import io.plyschik.springbootblog.exception.UserNotFoundException;
import io.plyschik.springbootblog.exception.VerificationTokenExpiredException;
import io.plyschik.springbootblog.exception.VerificationTokenNotFoundException;
import io.plyschik.springbootblog.repository.UserRepository;
import io.plyschik.springbootblog.repository.VerificationTokenRepository;
import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
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
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class UserService {
    private final ModelMapper modelMapper;
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final VerificationTokenRepository verificationTokenRepository;
    private final JavaMailSender mailSender;
    private final SpringTemplateEngine templateEngine;

    public User getUserById(Long id) {
        return userRepository.findById(id).orElseThrow(UserNotFoundException::new);
    }

    public User getUserByEmail(String email) {
        return userRepository.findByEmail(email).orElseThrow(() ->
            new UsernameNotFoundException(String.format("Username %s not found.", email))
        );
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
            token = UUID.randomUUID().toString();
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
}
