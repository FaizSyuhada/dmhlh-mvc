package com.dmhlh.entity;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;

@Entity
@Table(name = "forum_tags")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ForumTag {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(nullable = false, unique = true, length = 50)
    private String name;
    
    @Column(length = 7)
    private String color = "#6B7280";
    
    @Column(length = 255)
    private String description;
    
    @Column(name = "post_count")
    private int postCount = 0;
    
    @ManyToMany(mappedBy = "tags", fetch = FetchType.LAZY)
    @Builder.Default
    private Set<ForumThread> threads = new HashSet<>();
    
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;
    
    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
    }
    
    // Get actual thread count for trending display
    public int getActualPostCount() {
        if (threads != null && !threads.isEmpty()) {
            return (int) threads.stream()
                .filter(t -> t.getStatus() == ForumThread.Status.ACTIVE)
                .count();
        }
        return postCount;
    }
}
