package com.fellowlodge.api.service;

import com.fellowlodge.api.common.exception.DuplicateResourceException;
import com.fellowlodge.api.common.exception.ResourceNotFoundException;
import com.fellowlodge.api.entity.Setting;
import com.fellowlodge.api.repository.SettingRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class SettingService {

    private final SettingRepository settingRepository;

    public List<Setting> findAll() {
        return settingRepository.findAll(Sort.by(Sort.Direction.ASC, "key"));
    }

    public List<Setting> findByCategory(String category) {
        return settingRepository.findByCategory(category);
    }

    public Setting findByKey(String key) {
        return settingRepository.findByKey(key)
                .orElseThrow(() -> new ResourceNotFoundException("Setting with key " + key));
    }

    private Setting findById(UUID id) {
        return settingRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Setting", id));
    }

    public String getValue(String key, String defaultValue) {
        return settingRepository.findByKey(key).map(Setting::getValue).orElse(defaultValue);
    }

    @Transactional
    public Setting set(String key, String value, String category, String description) {
        Setting setting = settingRepository.findByKey(key).orElseGet(() -> {
            Setting created = new Setting();
            created.setKey(key);
            return created;
        });
        setting.setValue(value);
        if (StringUtils.hasText(category)) {
            setting.setCategory(category);
        }
        if (StringUtils.hasText(description)) {
            setting.setDescription(description);
        }
        return settingRepository.save(setting);
    }

    @Transactional
    public Setting update(UUID id, Setting updated) {
        Setting setting = findById(id);
        if (StringUtils.hasText(updated.getValue())) {
            setting.setValue(updated.getValue());
        }
        if (StringUtils.hasText(updated.getCategory())) {
            setting.setCategory(updated.getCategory());
        }
        if (StringUtils.hasText(updated.getDescription())) {
            setting.setDescription(updated.getDescription());
        }
        return settingRepository.save(setting);
    }

    @Transactional
    public void delete(UUID id) {
        settingRepository.delete(findById(id));
    }

    @Transactional
    public Setting create(Setting setting) {
        if (settingRepository.existsByKey(setting.getKey())) {
            throw new DuplicateResourceException("A setting already exists with key: " + setting.getKey());
        }
        return settingRepository.save(setting);
    }
}
