package com.dmhlh.entity;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "session_types")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SessionType {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(nullable = false, length = 100)
    private String name;
    
    @Column(length = 500)
    private String description;
    
    @Column(name = "duration_minutes", nullable = false)
    private int durationMinutes = 50;
    
    @Column(length = 50)
    private String icon;
    
    @Column(name = "is_free", nullable = false)
    private boolean free = true;
    
    @Column(nullable = false)
    private boolean active = true;
    
    @Column(name = "display_order")
    private int displayOrder = 0;
    
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;
    
    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
    }
}
