package com.dmhlh.repository;

import com.dmhlh.entity.ForumSettings;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface ForumSettingsRepository extends JpaRepository<ForumSettings, Long> {
    
    @Query("SELECT s FROM ForumSettings s WHERE s.id = 1")
    Optional<ForumSettings> findSettings();
    
    default ForumSettings getSettings() {
        return findSettings().orElseGet(() -> {
            ForumSettings settings = new ForumSettings();
            settings.setAllowPosting(true);
            settings.setMaxPostLength(5000);
            settings.setMaxTitleLength(200);
            settings.setAllowAnonymous(true);
            return settings;
        });
    }
}
