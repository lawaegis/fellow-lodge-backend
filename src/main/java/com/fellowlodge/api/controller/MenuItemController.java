package com.fellowlodge.api.controller;

import com.fellowlodge.api.common.ApiResponse;
import com.fellowlodge.api.common.PageResponse;
import com.fellowlodge.api.entity.MenuItem;
import com.fellowlodge.api.service.MenuItemService;
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
@RequestMapping("/api/menu-items")
@RequiredArgsConstructor
public class MenuItemController {

    private final MenuItemService menuItemService;

    @GetMapping
    @PreAuthorize("hasAuthority('MENU:READ')")
    public ApiResponse<List<MenuItem>> findAll(@RequestParam(defaultValue = "0") int page,
                                               @RequestParam(defaultValue = "20") int size,
                                               @RequestParam(required = false) String sort,
                                               @RequestParam(required = false) Boolean active,
                                               @RequestParam(required = false) UUID categoryId) {
        if (categoryId != null) {
            return ApiResponse.ok(menuItemService.findByCategoryId(categoryId));
        }
        Page<MenuItem> result = menuItemService.findAll(page, size, sort, active);
        return ApiResponse.ok(result.getContent(), PageResponse.from(result));
    }

    @GetMapping("/all")
    @PreAuthorize("hasAuthority('MENU:READ')")
    public ApiResponse<List<MenuItem>> findAll() {
        return ApiResponse.ok(menuItemService.findAll());
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAuthority('MENU:READ')")
    public ApiResponse<MenuItem> findById(@PathVariable UUID id) {
        return ApiResponse.ok(menuItemService.findById(id));
    }

    @PostMapping
    @PreAuthorize("hasAuthority('MENU:WRITE')")
    @ResponseStatus(HttpStatus.CREATED)
    public ApiResponse<MenuItem> create(@Valid @RequestBody MenuItem item) {
        return ApiResponse.created("Menu item created", menuItemService.create(item));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAuthority('MENU:WRITE')")
    public ApiResponse<MenuItem> update(@PathVariable UUID id, @Valid @RequestBody MenuItem item) {
        return ApiResponse.ok("Menu item updated", menuItemService.update(id, item));
    }

    @PostMapping("/{id}/active")
    @PreAuthorize("hasAuthority('MENU:WRITE')")
    public ApiResponse<MenuItem> setActive(@PathVariable UUID id, @RequestParam boolean active) {
        return ApiResponse.ok("Menu item updated", menuItemService.setActive(id, active));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAuthority('MENU:DELETE')")
    public ApiResponse<Void> delete(@PathVariable UUID id) {
        menuItemService.delete(id);
        return ApiResponse.deleted("Menu item deleted");
    }
}
