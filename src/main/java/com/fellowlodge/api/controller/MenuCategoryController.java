package com.fellowlodge.api.controller;

import com.fellowlodge.api.common.ApiResponse;
import com.fellowlodge.api.common.PageResponse;
import com.fellowlodge.api.entity.MenuCategory;
import com.fellowlodge.api.service.MenuCategoryService;
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
@RequestMapping("/api/menu-categories")
@RequiredArgsConstructor
public class MenuCategoryController {

    private final MenuCategoryService menuCategoryService;

    @GetMapping
    @PreAuthorize("hasAuthority('MENU:READ')")
    public ApiResponse<List<MenuCategory>> findAll(@RequestParam(defaultValue = "0") int page,
                                                   @RequestParam(defaultValue = "20") int size,
                                                   @RequestParam(required = false) String sort,
                                                   @RequestParam(required = false) Boolean active) {
        Page<MenuCategory> result = menuCategoryService.findAll(page, size, sort, active);
        return ApiResponse.ok(result.getContent(), PageResponse.from(result));
    }

    @GetMapping("/all")
    @PreAuthorize("hasAuthority('MENU:READ')")
    public ApiResponse<List<MenuCategory>> findAll() {
        return ApiResponse.ok(menuCategoryService.findAll());
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAuthority('MENU:READ')")
    public ApiResponse<MenuCategory> findById(@PathVariable UUID id) {
        return ApiResponse.ok(menuCategoryService.findById(id));
    }

    @PostMapping
    @PreAuthorize("hasAuthority('MENU:WRITE')")
    @ResponseStatus(HttpStatus.CREATED)
    public ApiResponse<MenuCategory> create(@Valid @RequestBody MenuCategory category) {
        return ApiResponse.created("Menu category created", menuCategoryService.create(category));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAuthority('MENU:WRITE')")
    public ApiResponse<MenuCategory> update(@PathVariable UUID id, @Valid @RequestBody MenuCategory category) {
        return ApiResponse.ok("Menu category updated", menuCategoryService.update(id, category));
    }

    @PostMapping("/{id}/active")
    @PreAuthorize("hasAuthority('MENU:WRITE')")
    public ApiResponse<MenuCategory> setActive(@PathVariable UUID id, @RequestParam boolean active) {
        return ApiResponse.ok("Menu category updated", menuCategoryService.setActive(id, active));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAuthority('MENU:DELETE')")
    public ApiResponse<Void> delete(@PathVariable UUID id) {
        menuCategoryService.delete(id);
        return ApiResponse.deleted("Menu category deleted");
    }
}
