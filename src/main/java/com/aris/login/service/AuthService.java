package com.aris.login.service;

import com.aris.login.dto.RegisterRequest;
import com.aris.login.entities.NotificationEmail;
import com.aris.login.entities.User;
import com.aris.login.entities.VerificationToken;
import com.aris.login.exception.SpringLoginException;
import com.aris.login.repository.UserRepo;
import com.aris.login.repository.VerificationRepo;
import lombok.AllArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

@Service
@AllArgsConstructor
@Transactional
public class AuthService {

    private final UserRepo userRepo;
    private final PasswordEncoder passwordEncoder;
    private final VerificationRepo verificationRepo;
    private final MailService mailService;


    public void signUp(RegisterRequest registerRequest){
        User user = new User();
        user.setUsername(registerRequest.getUsername());
        user.setPassword(encodePassword(registerRequest.getPassword()));
        user.setMail(registerRequest.getMail());
        user.setCreated(Instant.now());
        user.setActive(false);

        userRepo.save(user);

        String token = generateVerification(user);
        mailService.sendMail(new NotificationEmail("Please activate your account!", user.getMail(), "http://localhost:8080/api/auth/accountVerification/" + token));

    }

    private String generateVerification(User user) {
        String token = UUID.randomUUID().toString();
        VerificationToken verificationToken = new VerificationToken();
        verificationToken.setToken(token);
        verificationToken.setUser(user);

        verificationRepo.save(verificationToken);
        return token;
    }

    public String encodePassword(String password){
        return passwordEncoder.encode(password);
    }

    public void verifyAccount(String token) {
        Optional<VerificationToken> verificationToken = verificationRepo.findByToken(token);
        verificationToken.orElseThrow(()->  new SpringLoginException("Invalid token"));
        fetchUserAndEnable(verificationToken.get());
    }
    public void fetchUserAndEnable(VerificationToken verificationToken){
        String username = verificationToken.getUser().getUsername();
        User user = userRepo.findByUsername(username).orElseThrow(() -> new SpringLoginException("Invalid username"));
        user.setActive(true);
        userRepo.save(user);
    }
}
