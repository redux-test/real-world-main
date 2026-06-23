package com.gabrielgua.realworld.domain.service;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class EmailService {
    private final JavaMailSender mailSender;
    private final TemplateEngine templateEngine;

    public void sendWelcomeEmail(String to, String username) throws MessagingException {
        Context context = new Context();
        context.setVariable("username", username);
        
        String emailContent = templateEngine.process("welcome-email", context);
        
        MimeMessage message = mailSender.createMimeMessage();
        MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");
        
        helper.setTo(to);
        helper.setSubject("Welcome to RealWorld!");
        helper.setText(emailContent, true);
        
        mailSender.send(message);
    }

    public void sendStatsEmail(String to, String username, List<Map<String, Object>> articles, 
                             int totalArticles, int totalLikes, double averageLikes) throws MessagingException {
        Context context = new Context();
        context.setVariable("username", username);
        context.setVariable("articles", articles);
        context.setVariable("totalArticles", totalArticles);
        context.setVariable("totalLikes", totalLikes);
        context.setVariable("averageLikes", String.format("%.1f", averageLikes));
        
        String emailContent = templateEngine.process("stats-email", context);
        
        MimeMessage message = mailSender.createMimeMessage();
        MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");
        
        helper.setTo(to);
        helper.setSubject("Your Weekly Article Stats");
        helper.setText(emailContent, true);
        
        mailSender.send(message);
    }

    public void sendAdminReport(LocalDateTime generationDate,
                              Map<String, Object> todayStats,
                              Map<String, Object> lastWeekStats,
                              Map<String, Object> lastMonthStats,
                              List<Map<String, Object>> topArticles) throws MessagingException {
        Context context = new Context();
        context.setVariable("generationDate", generationDate);
        context.setVariable("todayStats", todayStats);
        context.setVariable("lastWeekStats", lastWeekStats);
        context.setVariable("lastMonthStats", lastMonthStats);
        context.setVariable("topArticles", topArticles);
        
        String emailContent = templateEngine.process("admin-report-email", context);
        
        MimeMessage message = mailSender.createMimeMessage();
        MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");
        
        helper.setTo("admin@realworld.com");
        helper.setSubject("RealWorld Platform Usage Report - " + generationDate.toLocalDate());
        helper.setText(emailContent, true);
        
        mailSender.send(message);
    }
} 