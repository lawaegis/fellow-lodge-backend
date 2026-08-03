package com.fellowlodge.api.controller;

import com.fellowlodge.api.common.ApiResponse;
import com.fellowlodge.api.dto.portal.RestaurantOrderRequest;
import com.fellowlodge.api.entity.MenuCategory;
import com.fellowlodge.api.entity.MenuItem;
import com.fellowlodge.api.entity.RestaurantOrder;
import com.fellowlodge.api.enums.RestaurantOrderStatus;
import com.fellowlodge.api.security.SecurityUtils;
import com.fellowlodge.api.service.MenuCategoryService;
import com.fellowlodge.api.service.MenuItemService;
import com.fellowlodge.api.service.PublicCatalogService;
import com.fellowlodge.api.service.RestaurantService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.UUID;

/**
 * Restaurant module for the guest portal. Menu browsing is public; placing an
 * order requires an authenticated user and is scoped to the caller. Staff with
 * RESTAURANT_ORDERS:WRITE move orders through the kitchen workflow.
 */
@RestController
@RequestMapping("/api/restaurant")
@RequiredArgsConstructor
public class RestaurantController {

    private final RestaurantService restaurantService;
    private final PublicCatalogService catalogService;
    private final MenuCategoryService menuCategoryService;
    private final MenuItemService menuItemService;

    @GetMapping("/categories")
    public ApiResponse<List<MenuCategory>> categories() {
        return ApiResponse.ok(menuCategoryService.findActive());
    }

    @GetMapping("/menu")
    public ApiResponse<List<MenuItem>> menu() {
        return ApiResponse.ok(catalogService.menuItems());
    }

    @GetMapping("/menu/{id}")
    public ApiResponse<MenuItem> menuItem(@PathVariable UUID id) {
        return ApiResponse.ok(menuItemService.findById(id));
    }

    @PostMapping("/orders")
    @PreAuthorize("isAuthenticated()")
    @ResponseStatus(HttpStatus.CREATED)
    public ApiResponse<RestaurantOrder> placeOrder(@Valid @RequestBody RestaurantOrderRequest request) {
        return ApiResponse.created("Order placed", restaurantService.placeOrder(request, SecurityUtils.currentUserId()));
    }

    @GetMapping("/orders")
    @PreAuthorize("isAuthenticated()")
    public ApiResponse<List<RestaurantOrder>> orders(@RequestParam(required = false) String status) {
        if (isGuest()) {
            return ApiResponse.ok(restaurantService.findByUserId(SecurityUtils.currentUserId()));
        }
        return ApiResponse.ok(restaurantService.findAll());
    }

    @GetMapping("/orders/{id}")
    @PreAuthorize("isAuthenticated()")
    public ApiResponse<RestaurantOrder> orderById(@PathVariable UUID id) {
        return ApiResponse.ok(resolveOrder(id));
    }

    @PostMapping("/orders/{id}/cancel")
    @PreAuthorize("isAuthenticated()")
    public ApiResponse<RestaurantOrder> cancel(@PathVariable UUID id) {
        return ApiResponse.ok("Order cancelled", restaurantService.cancel(resolveOrder(id).getId()));
    }

    @PostMapping("/orders/{id}/status")
    @PreAuthorize("hasAuthority('RESTAURANT_ORDERS:WRITE')")
    public ApiResponse<RestaurantOrder> setStatus(@PathVariable UUID id, @RequestParam RestaurantOrderStatus status) {
        return ApiResponse.ok("Order status updated", restaurantService.setStatus(id, status));
    }

    private RestaurantOrder resolveOrder(UUID id) {
        RestaurantOrder order = restaurantService.findById(id);
        if (isGuest() && !order.getUserId().equals(SecurityUtils.currentUserId())) {
            throw new com.fellowlodge.api.common.exception.ResourceNotFoundException("Restaurant order", id);
        }
        return order;
    }

    private boolean isGuest() {
        return "Guest".equals(SecurityUtils.currentRole());
    }
}
