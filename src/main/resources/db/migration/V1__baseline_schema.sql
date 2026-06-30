-- Baseline Schema Migration for RealWorld Application
-- This migration creates the complete database schema with all tables, indexes, and foreign keys

-- ============================================================================
-- PRIMARY TABLES
-- ============================================================================

-- Users table: stores user authentication and profile information
CREATE TABLE users (
    id BIGINT NOT NULL AUTO_INCREMENT,
    username VARCHAR(255) NOT NULL,
    email VARCHAR(255) NOT NULL,
    bio VARCHAR(255),
    password VARCHAR(255) NOT NULL,
    token VARCHAR(255),
    image VARCHAR(255),
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    
    PRIMARY KEY (id),
    UNIQUE KEY uk_users_username (username),
    UNIQUE KEY uk_users_email (email),
    INDEX idx_users_username (username),
    INDEX idx_users_email (email)
) ENGINE=InnoDB DEFAULT CHARSET=UTF8MB4;

-- Profiles table: stores public profile information for users
CREATE TABLE profiles (
    user_id BIGINT NOT NULL,
    username VARCHAR(255) NOT NULL,
    bio VARCHAR(255),
    image VARCHAR(255),
    
    PRIMARY KEY (user_id),
    INDEX idx_profiles_username (username),
    CONSTRAINT fk_profiles_user FOREIGN KEY (user_id) REFERENCES users (id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=UTF8MB4;

-- Articles table: stores blog articles/posts
CREATE TABLE articles (
    id BIGINT NOT NULL AUTO_INCREMENT,
    title VARCHAR(255) NOT NULL,
    slug VARCHAR(255) NOT NULL,
    description VARCHAR(255) NOT NULL,
    body VARCHAR(512) NOT NULL,
    favorites_count INT NOT NULL DEFAULT 0,
    created_at DATETIME NOT NULL,
    updated_at DATETIME NOT NULL,
    author_id BIGINT NOT NULL,
    
    PRIMARY KEY (id),
    UNIQUE KEY uk_articles_slug (slug),
    INDEX idx_articles_author (author_id),
    INDEX idx_articles_created_at (created_at),
    CONSTRAINT fk_articles_author FOREIGN KEY (author_id) REFERENCES profiles (user_id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=UTF8MB4;

-- Comments table: stores comments on articles
CREATE TABLE comments (
    id BIGINT NOT NULL AUTO_INCREMENT,
    created_at DATETIME NOT NULL,
    updated_at DATETIME NOT NULL,
    body VARCHAR(255) NOT NULL,
    author_id BIGINT NOT NULL,
    article_id BIGINT NOT NULL,
    
    PRIMARY KEY (id),
    INDEX idx_comments_article (article_id),
    INDEX idx_comments_author (author_id),
    CONSTRAINT fk_comments_author FOREIGN KEY (author_id) REFERENCES profiles (user_id) ON DELETE CASCADE,
    CONSTRAINT fk_comments_article FOREIGN KEY (article_id) REFERENCES articles (id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=UTF8MB4;

-- Tags table: stores article tags
CREATE TABLE tags (
    id BIGINT NOT NULL AUTO_INCREMENT,
    name VARCHAR(255) NOT NULL,
    
    PRIMARY KEY (id),
    UNIQUE KEY uk_tags_name (name),
    INDEX idx_tags_name (name)
) ENGINE=InnoDB DEFAULT CHARSET=UTF8MB4;

-- ============================================================================
-- JOIN TABLES (Many-to-Many Relationships)
-- ============================================================================

-- Articles-Tags join table: associates articles with tags
CREATE TABLE articles_tags (
    article_id BIGINT NOT NULL,
    tag_id BIGINT NOT NULL,
    
    PRIMARY KEY (article_id, tag_id),
    INDEX idx_articles_tags_tag (tag_id),
    CONSTRAINT fk_articles_tags_article FOREIGN KEY (article_id) REFERENCES articles (id) ON DELETE CASCADE,
    CONSTRAINT fk_articles_tags_tags FOREIGN KEY (tag_id) REFERENCES tags (id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=UTF8MB4;

-- Profiles-Following join table: tracks which profiles follow which other profiles
CREATE TABLE profiles_following (
    profile_id BIGINT NOT NULL,
    following_id BIGINT NOT NULL,
    
    PRIMARY KEY (profile_id, following_id),
    INDEX idx_profiles_following_following (following_id),
    CONSTRAINT fk_profiles_following_profiles FOREIGN KEY (profile_id) REFERENCES profiles (user_id) ON DELETE CASCADE,
    CONSTRAINT fk_profiles_following_following FOREIGN KEY (following_id) REFERENCES profiles (user_id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=UTF8MB4;

-- Profiles-Articles join table: tracks which profiles have favorited which articles
CREATE TABLE profiles_articles (
    profile_id BIGINT NOT NULL,
    article_id BIGINT NOT NULL,
    
    PRIMARY KEY (profile_id, article_id),
    INDEX idx_profiles_articles_article (article_id),
    CONSTRAINT fk_profiles_articles_profiles FOREIGN KEY (profile_id) REFERENCES profiles (user_id) ON DELETE CASCADE,
    CONSTRAINT fk_profiles_articles_articles FOREIGN KEY (article_id) REFERENCES articles (id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=UTF8MB4;

-- ============================================================================
-- SCHEMA SUMMARY
-- ============================================================================
-- Primary Tables: 5 (users, profiles, articles, comments, tags)
-- Join Tables: 3 (articles_tags, profiles_following, profiles_articles)
-- Foreign Keys: 10 (all with CASCADE delete rules)
-- Indexes: 8 performance indexes (plus unique constraints)
-- ============================================================================
