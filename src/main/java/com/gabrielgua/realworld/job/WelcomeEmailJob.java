package com.gabrielgua.realworld.job;

import com.gabrielgua.realworld.domain.model.User;
import com.gabrielgua.realworld.domain.repository.UserRepository;
import com.gabrielgua.realworld.domain.service.EmailService;
import jakarta.mail.MessagingException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
public class WelcomeEmailJob {
    private final UserRepository userRepository;
    private final EmailService emailService;

    @Scheduled(cron = "0 0 18 * * ?") // Runs at 6 PM every day
    public void sendWelcomeEmails() {
        log.info("Starting welcome email job");
        
        LocalDateTime startOfDay = LocalDateTime.now().with(LocalTime.MIN);
        LocalDateTime endOfDay = LocalDateTime.now().with(LocalTime.MAX);
        
        List<User> newUsers = userRepository.findByCreatedAtBetween(startOfDay, endOfDay);
        
        for (User user : newUsers) {
            try {
                emailService.sendWelcomeEmail(user.getEmail(), user.getProfile().getUsername());
                log.info("Sent welcome email to user ID: {}", user.getId());
            } catch (MessagingException e) {
                log.error("Failed to send welcome email to user ID: {}", user.getId(), e);
            }
        }
        
        log.info("Completed welcome email job. Processed {} users", newUsers.size());
    }
}
