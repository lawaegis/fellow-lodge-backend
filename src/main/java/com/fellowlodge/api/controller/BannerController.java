package com.fellowlodge.api.controller;

import com.fellowlodge.api.common.ApiResponse;
import com.fellowlodge.api.common.PageResponse;
import com.fellowlodge.api.entity.Banner;
import com.fellowlodge.api.service.BannerService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/banners")
@RequiredArgsConstructor
public class BannerController {

    private final BannerService bannerService;

    @GetMapping
    @PreAuthorize("hasAuthority('BANNERS:READ')")
    public ApiResponse<List<Banner>> findAll(@RequestParam(defaultValue = "0") int page,
                                             @RequestParam(defaultValue = "20") int size,
                                             @RequestParam(required = false) String sort,
                                             @RequestParam(required = false) Boolean active) {
        Page<Banner> result = bannerService.findAll(page, size, sort, active);
        return ApiResponse.ok(result.getContent(), PageResponse.from(result));
    }

    @GetMapping("/all")
    @PreAuthorize("hasAuthority('BANNERS:READ')")
    public ApiResponse<List<Banner>> findAll() {
        return ApiResponse.ok(bannerService.findAll());
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAuthority('BANNERS:READ')")
    public ApiResponse<Banner> findById(@PathVariable UUID id) {
        return ApiResponse.ok(bannerService.findById(id));
    }

    @PostMapping
    @PreAuthorize("hasAuthority('BANNERS:WRITE')")
    @ResponseStatus(HttpStatus.CREATED)
    public ApiResponse<Banner> create(@Valid @RequestBody Banner banner) {
        return ApiResponse.created("Banner created", bannerService.create(banner));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAuthority('BANNERS:WRITE')")
    public ApiResponse<Banner> update(@PathVariable UUID id, @Valid @RequestBody Banner banner) {
        return ApiResponse.ok("Banner updated", bannerService.update(id, banner));
    }

    @PostMapping("/{id}/active")
    @PreAuthorize("hasAuthority('BANNERS:WRITE')")
    public ApiResponse<Banner> setActive(@PathVariable UUID id, @RequestParam boolean active) {
        return ApiResponse.ok("Banner updated", bannerService.setActive(id, active));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAuthority('BANNERS:DELETE')")
    public ApiResponse<Void> delete(@PathVariable UUID id) {
        bannerService.delete(id);
        return ApiResponse.deleted("Banner deleted");
    }
}
