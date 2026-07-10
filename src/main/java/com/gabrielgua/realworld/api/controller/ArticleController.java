package com.gabrielgua.realworld.api.controller;

import com.gabrielgua.realworld.api.assembler.ArticleAssembler;
import com.gabrielgua.realworld.api.model.article.ArticleWrapper;
import com.gabrielgua.realworld.api.model.article.ArticleRegister;
import com.gabrielgua.realworld.api.model.article.ArticleResponse;
import com.gabrielgua.realworld.api.model.article.ArticleUpdate;
import com.gabrielgua.realworld.api.security.AuthUtils;
import com.gabrielgua.realworld.api.security.authorization.CheckSecurity;
import com.gabrielgua.realworld.domain.model.Tag;
import com.gabrielgua.realworld.domain.service.ArticleService;
import com.gabrielgua.realworld.domain.service.TagService;
import com.gabrielgua.realworld.domain.service.UserService;
import com.gabrielgua.realworld.infra.spec.ArticleSpecification;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/articles")
public class ArticleController {

    private final AuthUtils authUtils;
    private final TagService tagService;
    private final UserService userService;
    private final ArticleService articleService;
    private final ArticleAssembler articleAssembler;

    private static final String DEFAULT_FILTER_LIMIT = "20";
    private static final String DEFAULT_FILTER_OFFSET = "0";
    private static final Sort DEFAULT_FILTER_SORT = Sort.by(Sort.Direction.DESC, "createdAt");
    
    @GetMapping
    @CheckSecurity.Public.canRead
    public ArticleWrapper getAll(
            ArticleSpecification filter,
            @RequestParam(required = false, defaultValue = DEFAULT_FILTER_LIMIT) int limit,
            @RequestParam(required = false, defaultValue = DEFAULT_FILTER_OFFSET) int offset) {

        log.debug("Fetching articles with limit: {}, offset: {}, authenticated: {}", 
                 limit, offset, authUtils.isAuthenticated());

        Pageable pageable = PageRequest.of(offset, limit, DEFAULT_FILTER_SORT);
        var articles = articleService.listAll(filter, pageable).getContent();

        // Return differentiated responses based on authentication status
        if (authUtils.isAuthenticated()) {
            try {
                var profile = userService.getCurrentUser().getProfile();
                log.debug("Returning personalized article feed for authenticated user");
                return articleAssembler.toCollectionModel(profile, articles);
            } catch (Exception e) {
                log.warn("Failed to get current user profile, falling back to public response: {}", e.getMessage());
                return articleAssembler.toCollectionModel(articles);
            }
        }

        log.debug("Returning public article feed for unauthenticated user");
        return articleAssembler.toCollectionModel(articles);
    }

    @GetMapping("/feed")
    @CheckSecurity.Public.canRead
    public ArticleWrapper getFeed(
            @RequestParam(required = false, defaultValue = DEFAULT_FILTER_LIMIT) int limit,
            @RequestParam(required = false, defaultValue = DEFAULT_FILTER_OFFSET) int offset
    ) {
        log.debug("Fetching personalized feed with limit: {}, offset: {}, authenticated: {}", 
                 limit, offset, authUtils.isAuthenticated());

        // For feed endpoint, authentication is required for personalized content
        if (!authUtils.isAuthenticated()) {
            log.debug("Unauthenticated user accessing feed, returning global articles");
            // Return global articles for unauthenticated users instead of empty feed
            Pageable pageable = PageRequest.of(offset, limit, DEFAULT_FILTER_SORT);
            var articles = articleService.listAll(new ArticleSpecification(), pageable).getContent();
            return articleAssembler.toCollectionModel(articles);
        }

        try {
            var profile = userService.getCurrentUser().getProfile();
            Pageable pageable = PageRequest.of(offset, limit, DEFAULT_FILTER_SORT);
            var articles = articleService.getFeedByUser(profile, pageable);

            log.debug("Returning personalized feed for user profile: {}", profile.getUsername());
            return articleAssembler.toCollectionModel(profile, articles);
        } catch (Exception e) {
            log.error("Error fetching personalized feed: {}", e.getMessage());
            // Fallback to global articles if personalized feed fails
            Pageable pageable = PageRequest.of(offset, limit, DEFAULT_FILTER_SORT);
            var articles = articleService.listAll(new ArticleSpecification(), pageable).getContent();
            return articleAssembler.toCollectionModel(articles);
        }
    }

    @GetMapping("/{slug}")
    @CheckSecurity.Public.canRead
    public ArticleResponse getBySlug(@PathVariable String slug) {
        log.debug("Fetching article by slug: {}, authenticated: {}", slug, authUtils.isAuthenticated());
        
        var article = articleService.getBySlug(slug);

        // Return differentiated responses based on authentication status
        if (authUtils.isAuthenticated()) {
            try {
                var profile = userService.getCurrentUser().getProfile();
                log.debug("Returning personalized article response for authenticated user");
                return articleAssembler.toResponse(profile, article);
            } catch (Exception e) {
                log.warn("Failed to get current user profile for article {}, falling back to public response: {}", 
                        slug, e.getMessage());
                return articleAssembler.toResponse(article);
            }
        }

        log.debug("Returning public article response for unauthenticated user");
        return articleAssembler.toResponse(article);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @CheckSecurity.Protected.canManage
    public ArticleResponse save(@RequestBody ArticleRegister register) {
        var profile = userService.getCurrentUser().getProfile();

        List<Tag> tags = new ArrayList<>();
        if (register.getTagList() != null) {
            tags = tagService.saveAll(register.getTagList().stream().toList());
        }

        var article = articleAssembler.toEntity(register);
        return articleAssembler.toResponse(profile, articleService.save(article, profile, tags));
    }

    @PutMapping("/{slug}")
    @CheckSecurity.Articles.canManage
    public ArticleResponse update(@PathVariable String slug, @RequestBody ArticleUpdate update) {
        var article = articleService.getBySlug(slug);
        articleAssembler.copyToEntity(update, article);

        return articleAssembler.toResponse(articleService.save(article));
    }

    @DeleteMapping("/{slug}")
    @CheckSecurity.Articles.canManage
    public void delete(@PathVariable String slug) {
        var article = articleService.getBySlug(slug);
        articleService.delete(article);
    }
}