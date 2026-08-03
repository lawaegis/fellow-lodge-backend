package com.fellowlodge.api.controller;

import com.fellowlodge.api.common.ApiResponse;
import com.fellowlodge.api.entity.Setting;
import com.fellowlodge.api.service.SettingService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/settings")
@RequiredArgsConstructor
public class SettingController {

    private final SettingService settingService;

    @GetMapping
    @PreAuthorize("hasAuthority('SETTINGS:READ')")
    public ApiResponse<List<Setting>> findAll() {
        return ApiResponse.ok(settingService.findAll());
    }

    @GetMapping("/category/{category}")
    @PreAuthorize("hasAuthority('SETTINGS:READ')")
    public ApiResponse<List<Setting>> byCategory(@PathVariable String category) {
        return ApiResponse.ok(settingService.findByCategory(category));
    }

    @GetMapping("/key/{key}")
    @PreAuthorize("hasAuthority('SETTINGS:READ')")
    public ApiResponse<Setting> findByKey(@PathVariable String key) {
        return ApiResponse.ok(settingService.findByKey(key));
    }

    @PostMapping
    @PreAuthorize("hasAuthority('SETTINGS:WRITE')")
    @ResponseStatus(HttpStatus.CREATED)
    public ApiResponse<Setting> create(@Valid @RequestBody Setting setting) {
        return ApiResponse.created("Setting created", settingService.create(setting));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAuthority('SETTINGS:WRITE')")
    public ApiResponse<Setting> update(@PathVariable UUID id, @Valid @RequestBody Setting setting) {
        return ApiResponse.ok("Setting updated", settingService.update(id, setting));
    }

    @PutMapping("/key/{key}")
    @PreAuthorize("hasAuthority('SETTINGS:WRITE')")
    public ApiResponse<Setting> set(@PathVariable String key,
                                    @RequestParam String value,
                                    @RequestParam(required = false) String category,
                                    @RequestParam(required = false) String description) {
        return ApiResponse.ok("Setting updated", settingService.set(key, value, category, description));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAuthority('SETTINGS:WRITE')")
    public ApiResponse<Void> delete(@PathVariable UUID id) {
        settingService.delete(id);
        return ApiResponse.deleted("Setting deleted");
    }
}
