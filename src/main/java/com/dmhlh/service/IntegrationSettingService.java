package com.dmhlh.service;

import com.dmhlh.entity.IntegrationSetting;
import com.dmhlh.repository.IntegrationSettingRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
public class IntegrationSettingService {
    
    private final IntegrationSettingRepository repository;
    
    public IntegrationSettingService(IntegrationSettingRepository repository) {
        this.repository = repository;
    }
    
    public List<IntegrationSetting> getAllSettings() {
        return repository.findAllByOrderBySettingKeyAsc();
    }
    
    public Optional<IntegrationSetting> findByKey(String key) {
        return repository.findBySettingKey(key);
    }
    
    public String getValue(String key, String defaultValue) {
        return repository.findBySettingKey(key)
            .map(IntegrationSetting::getSettingValue)
            .orElse(defaultValue);
    }
    
    @Transactional
    public IntegrationSetting createOrUpdate(String key, String value, boolean isSecret, String description) {
        IntegrationSetting setting = repository.findBySettingKey(key)
            .orElseGet(() -> IntegrationSetting.builder()
                .settingKey(key)
                .build());
        
        setting.setSettingValue(value);
        setting.setSecret(isSecret);
        if (description != null) {
            setting.setDescription(description);
        }
        
        return repository.save(setting);
    }
    
    @Transactional
    public void delete(Long id) {
        repository.deleteById(id);
    }
}
