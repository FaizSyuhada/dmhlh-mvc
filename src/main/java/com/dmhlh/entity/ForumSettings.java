package com.dmhlh.entity;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "forum_settings")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ForumSettings {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(name = "allow_posting", nullable = false)
    private boolean allowPosting = true;
    
    @Column(name = "max_post_length", nullable = false)
    private int maxPostLength = 5000;
    
    @Column(name = "max_title_length", nullable = false)
    private int maxTitleLength = 200;
    
    @Column(name = "banned_words", columnDefinition = "TEXT")
    private String bannedWords; // Comma-separated list - Hard block, post rejected
    
    @Column(name = "moderation_words", columnDefinition = "TEXT")
    private String moderationWords; // Comma-separated list - Soft block, goes to pending review
    
    @Column(name = "require_moderation", nullable = false)
    private boolean requireModeration = false;
    
    @Column(name = "allow_anonymous", nullable = false)
    private boolean allowAnonymous = true;
    
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
    
    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
}
