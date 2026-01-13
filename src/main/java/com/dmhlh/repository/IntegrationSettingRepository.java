package com.dmhlh.repository;

import com.dmhlh.entity.IntegrationSetting;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface IntegrationSettingRepository extends JpaRepository<IntegrationSetting, Long> {
    
    Optional<IntegrationSetting> findBySettingKey(String key);
    
    List<IntegrationSetting> findAllByOrderBySettingKeyAsc();
    
    boolean existsBySettingKey(String key);
}
