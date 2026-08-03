package com.fellowlodge.api.controller;

import com.fellowlodge.api.common.ApiResponse;
import com.fellowlodge.api.dto.booking.AddToCartRequest;
import com.fellowlodge.api.dto.booking.CartItemRequest;
import com.fellowlodge.api.dto.booking.CartResponse;
import com.fellowlodge.api.dto.booking.CheckoutRequest;
import com.fellowlodge.api.entity.Guest;
import com.fellowlodge.api.entity.Reservation;
import com.fellowlodge.api.security.SecurityUtils;
import com.fellowlodge.api.service.BookingCartService;
import com.fellowlodge.api.service.GuestService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/booking")
@RequiredArgsConstructor
public class BookingController {

    private final BookingCartService bookingCartService;
    private final GuestService guestService;

    @GetMapping("/cart")
    public ApiResponse<CartResponse> getCart(@RequestHeader(value = "X-Session-Id", required = false) String sessionId) {
        return ApiResponse.ok(bookingCartService.getOrCreateCart(currentUserId(), currentGuestId(), sessionId));
    }

    @PostMapping("/cart/items")
    public ApiResponse<CartResponse> addItems(@Valid @RequestBody AddToCartRequest request,
                                              @RequestHeader(value = "X-Session-Id", required = false) String sessionId) {
        return ApiResponse.ok(bookingCartService.addItems(currentUserId(), currentGuestId(), sessionId, request));
    }

    @PostMapping("/cart/items/single")
    public ApiResponse<CartResponse> addItem(@Valid @RequestBody CartItemRequest request,
                                             @RequestHeader(value = "X-Session-Id", required = false) String sessionId) {
        return ApiResponse.ok(bookingCartService.addItem(currentUserId(), currentGuestId(), sessionId, request));
    }

    @GetMapping("/cart/{cartId}")
    public ApiResponse<CartResponse> viewCart(@PathVariable UUID cartId,
                                              @RequestHeader(value = "X-Session-Id", required = false) String sessionId) {
        return ApiResponse.ok(bookingCartService.viewCart(cartId, currentUserId(), currentGuestId(), sessionId));
    }

    @DeleteMapping("/cart/{cartId}/items/{itemId}")
    public ApiResponse<Void> removeItem(@PathVariable UUID cartId, @PathVariable UUID itemId,
                                        @RequestHeader(value = "X-Session-Id", required = false) String sessionId) {
        bookingCartService.removeItem(cartId, itemId, currentUserId(), currentGuestId(), sessionId);
        return ApiResponse.deleted("Item removed from cart");
    }

    @DeleteMapping("/cart/{cartId}/items")
    public ApiResponse<Void> clearCart(@PathVariable UUID cartId,
                                       @RequestHeader(value = "X-Session-Id", required = false) String sessionId) {
        bookingCartService.clearCart(cartId, currentUserId(), currentGuestId(), sessionId);
        return ApiResponse.deleted("Cart cleared");
    }

    @PostMapping("/cart/checkout")
    public ApiResponse<List<Reservation>> checkout(@Valid @RequestBody CheckoutRequest request,
                                                   @RequestHeader(value = "X-Session-Id", required = false) String sessionId) {
        return ApiResponse.ok("Checkout complete",
                bookingCartService.checkout(request, currentUserId(), currentGuestId(), sessionId));
    }

    private UUID currentUserId() {
        return SecurityUtils.currentUserId();
    }

    private UUID currentGuestId() {
        UUID userId = currentUserId();
        if (userId == null) {
            return null;
        }
        Guest guest = guestService.findByUserId(userId);
        return guest == null ? null : guest.getId();
    }
}
