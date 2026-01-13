package com.dmhlh.entity;

import jakarta.persistence.*;
import lombok.*;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

@Entity
@Table(name = "counsellor_availability")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CounsellorAvailability {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "counsellor_id", nullable = false)
    private User counsellor;
    
    @Column(name = "day_of_week", nullable = false, columnDefinition = "TINYINT")
    private byte dayOfWeek; // 1=Monday, 7=Sunday
    
    @Column(name = "start_time", nullable = false)
    private LocalTime startTime;
    
    @Column(name = "end_time", nullable = false)
    private LocalTime endTime;
    
    @Column(name = "is_recurring", nullable = false)
    private boolean recurring = true;
    
    @Column(name = "specific_date")
    private LocalDate specificDate;
    
    @Column(name = "is_available", nullable = false)
    private boolean available = true;
    
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;
    
    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
    }
    
    public DayOfWeek getDayOfWeekEnum() {
        return DayOfWeek.of((int) dayOfWeek);
    }
    
    public String getDayName() {
        return getDayOfWeekEnum().name().charAt(0) + 
               getDayOfWeekEnum().name().substring(1).toLowerCase();
    }
}
