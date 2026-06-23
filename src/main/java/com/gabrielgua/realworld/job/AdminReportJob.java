package com.gabrielgua.realworld.job;

import com.gabrielgua.realworld.domain.model.Article;
import com.gabrielgua.realworld.domain.repository.ArticleRepository;
import com.gabrielgua.realworld.domain.service.EmailService;
import jakarta.mail.MessagingException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Slf4j
@Component
@RequiredArgsConstructor
public class AdminReportJob {
    private final ArticleRepository articleRepository;
    private final EmailService emailService;

    @Scheduled(cron = "0 0 20 * * ?") // Runs at 8 PM every day
    public void sendDailyReport() {
        log.info("Starting daily admin report job");
        
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime startOfDay = now.with(LocalTime.MIN);
        LocalDateTime startOfWeek = now.minusWeeks(1).with(LocalTime.MIN);
        LocalDateTime startOfMonth = now.minusMonths(1).with(LocalTime.MIN);
        
        try {
            // Get article statistics
            Map<String, Object> todayStats = getArticleStats(startOfDay, now);
            Map<String, Object> lastWeekStats = getArticleStats(startOfWeek, now);
            Map<String, Object> lastMonthStats = getArticleStats(startOfMonth, now);
            
            // Get top performing articles
            List<Map<String, Object>> topArticles = getTopArticles();
            
            // Send the report
            emailService.sendAdminReport(
                now,
                todayStats,
                lastWeekStats,
                lastMonthStats,
                topArticles
            );
            
            log.info("Successfully sent daily admin report");
        } catch (MessagingException e) {
            log.error("Failed to send daily admin report", e);
        }
    }
    
    private Map<String, Object> getArticleStats(LocalDateTime start, LocalDateTime end) {
        long totalArticles = articleRepository.countByCreatedAtBetween(start, end);
        long days = java.time.Duration.between(start, end).toDays();
        double averagePerDay = days > 0 ? (double) totalArticles / days : 0;
        
        Map<String, Object> stats = new HashMap<>();
        stats.put("totalArticles", totalArticles);
        stats.put("averagePerDay", String.format("%.1f", averagePerDay));
        return stats;
    }
    
    private List<Map<String, Object>> getTopArticles() {
        List<Article> topArticles = articleRepository.findAll(
            PageRequest.of(0, 5, Sort.by(Sort.Direction.DESC, "favoritesCount"))
        ).getContent();
        
        List<Map<String, Object>> articleStats = new ArrayList<>();
        for (Article article : topArticles) {
            Map<String, Object> stats = new HashMap<>();
            stats.put("title", article.getTitle());
            stats.put("author", article.getAuthor().getUsername());
            stats.put("likes", article.getFavoritesCount());
            stats.put("createdAt", article.getCreatedAt());
            articleStats.add(stats);
        }
        
        return articleStats;
    }
} 