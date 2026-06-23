package com.gabrielgua.realworld.job;

import com.gabrielgua.realworld.domain.model.Article;
import com.gabrielgua.realworld.domain.repository.ArticleRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import java.time.LocalDateTime;
import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
public class SearchSyncJob {
    private final ArticleRepository articleRepository;
    private final RestTemplate restTemplate;

    @Value("${search.service.url}")
    private String essServiceUrl;

    @Scheduled(fixedRate = 300000) // Runs every 5 minutes
    public void syncArticlesToSearch() {
        log.info("Starting article sync to search service");
        
        LocalDateTime lastSync = LocalDateTime.now().minusMinutes(5);
        List<Article> newArticles = articleRepository.findByCreatedAtAfter(lastSync);
        
        if (newArticles.isEmpty()) {
            log.info("No new articles to sync");
            return;
        }
        
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        
        for (Article article : newArticles) {
            try {
                HttpEntity<Article> request = new HttpEntity<>(article, headers);
                //Send the article to ESS service.
                Boolean success = restTemplate.postForObject(
                    essServiceUrl + "/articles",
                    request,
                    Boolean.class
                );
                
                if (Boolean.TRUE.equals(success)) {
                    log.info("Successfully synced article: {}", article.getTitle());
                } else {
                    log.warn("Failed to sync article: {}", article.getTitle());
                }
            } catch (Exception e) {
                log.error("Error syncing article: {}", article.getTitle(), e);
            }
        }
        
        log.info("Completed article sync. Processed {} articles", newArticles.size());
    }
} 
