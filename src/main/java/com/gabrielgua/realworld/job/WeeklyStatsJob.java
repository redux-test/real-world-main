package com.gabrielgua.realworld.job;

import com.gabrielgua.realworld.domain.model.Article;
import com.gabrielgua.realworld.domain.model.User;
import com.gabrielgua.realworld.domain.repository.ArticleRepository;
import com.gabrielgua.realworld.domain.repository.UserRepository;
import com.gabrielgua.realworld.domain.service.EmailService;
import jakarta.mail.MessagingException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
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
public class WeeklyStatsJob {
    private final UserRepository userRepository;
    private final ArticleRepository articleRepository;
    private final EmailService emailService;

    @Scheduled(cron = "0 0 9 ? * MON") // Runs at 9 AM every Monday
    public void sendWeeklyStats() {
        log.info("Starting weekly stats job");
        
        LocalDateTime weekStart = LocalDateTime.now().minusWeeks(1).with(LocalTime.MIN);
        LocalDateTime weekEnd = LocalDateTime.now().with(LocalTime.MAX);
        
        List<User> usersWithArticles = userRepository.findUsersWithArticles();
        
        for (User user : usersWithArticles) {
            try {
                List<Article> userArticles = articleRepository.findByAuthorId(user.getId());
                
                if (userArticles.isEmpty()) {
                    continue;
                }
                
                int totalLikes = 0;
                List<Map<String, Object>> articleStats = new ArrayList<>();
                
                for (Article article : userArticles) {
                    int articleLikes = article.getFavoritesCount();
                    totalLikes += articleLikes;
                    
                    Map<String, Object> stats = new HashMap<>();
                    stats.put("title", article.getTitle());
                    stats.put("likes", articleLikes);
                    stats.put("createdAt", article.getCreatedAt());
                    articleStats.add(stats);
                }
                
                double averageLikes = userArticles.isEmpty() ? 0 : (double) totalLikes / userArticles.size();
                
                emailService.sendStatsEmail(
                    user.getEmail(),
                    user.getProfile().getUsername(),
                    articleStats,
                    userArticles.size(),
                    totalLikes,
                    averageLikes
                );
                
                log.info("Sent weekly stats to user ID: {}", user.getId());
            } catch (MessagingException e) {
                log.error("Failed to send weekly stats to user ID: {}", user.getId(), e);
            }
        }
        
        log.info("Completed weekly stats job. Processed {} users", usersWithArticles.size());
    }
}
