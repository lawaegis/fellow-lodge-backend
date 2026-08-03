package com.fellowlodge.api.service;

import com.fellowlodge.api.common.exception.InvalidOperationException;
import com.fellowlodge.api.common.exception.ResourceNotFoundException;
import com.fellowlodge.api.dto.portal.RestaurantOrderItemRequest;
import com.fellowlodge.api.dto.portal.RestaurantOrderRequest;
import com.fellowlodge.api.entity.MenuItem;
import com.fellowlodge.api.entity.RestaurantOrder;
import com.fellowlodge.api.entity.RestaurantOrderItem;
import com.fellowlodge.api.enums.OrderType;
import com.fellowlodge.api.enums.RestaurantOrderStatus;
import com.fellowlodge.api.repository.RestaurantOrderRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.ThreadLocalRandom;

/**
 * Restaurant ordering for the guest portal. Menu reads are public
 * (/api/restaurant/menu); placing an order requires an authenticated user and
 * is always scoped to the caller. Staff may list all orders and move their
 * status through the kitchen workflow.
 */
@Service
@RequiredArgsConstructor
public class RestaurantService {

    private final RestaurantOrderRepository orderRepository;
    private final MenuItemService menuItemService;
    private final SettingService settingService;
    private final GuestService guestService;

    public List<RestaurantOrder> findByUserId(UUID userId) {
        return orderRepository.findByUserIdOrderByPlacedAtDesc(userId);
    }

    public List<RestaurantOrder> findAll() {
        return orderRepository.findAllByOrderByPlacedAtDesc();
    }

    public RestaurantOrder findById(UUID id) {
        return orderRepository.findWithItemsById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Restaurant order", id));
    }

    @Transactional
    public RestaurantOrder placeOrder(RestaurantOrderRequest request, UUID userId) {
        if (request.items() == null || request.items().isEmpty()) {
            throw new InvalidOperationException("An order must contain at least one item.");
        }

        RestaurantOrder order = new RestaurantOrder();
        order.setOrderNumber(generateOrderNumber());
        order.setUserId(userId);
        if (userId != null) {
            var guest = guestService.findByUserId(userId);
            if (guest != null) {
                order.setGuestId(guest.getId());
            }
        }
        order.setOrderType(parseOrderType(request.orderType()));
        order.setSpecialRequests(request.specialRequests());
        order.setGuestName(request.guestName());
        order.setGuestEmail(request.guestEmail());
        order.setGuestPhone(request.guestPhone());
        order.setPlacedAt(LocalDateTime.now());

        BigDecimal subtotal = BigDecimal.ZERO;
        for (RestaurantOrderItemRequest itemRequest : request.items()) {
            MenuItem menuItem = menuItemService.findById(itemRequest.menuItemId());
            if (!menuItem.isAvailable() || !menuItem.isActive()) {
                throw new InvalidOperationException(
                        "Menu item '" + menuItem.getName() + "' is not currently available.");
            }
            RestaurantOrderItem item = new RestaurantOrderItem();
            item.setOrder(order);
            item.setMenuItemId(menuItem.getId());
            item.setItemName(menuItem.getName());
            item.setUnitPrice(menuItem.getPrice());
            item.setQuantity(itemRequest.quantity());
            item.setLineTotal(menuItem.getPrice()
                    .multiply(BigDecimal.valueOf(itemRequest.quantity()))
                    .setScale(2, RoundingMode.HALF_UP));
            item.setNotes(itemRequest.notes());
            order.getItems().add(item);
            subtotal = subtotal.add(item.getLineTotal());
        }

        order.setSubtotal(subtotal.setScale(2, RoundingMode.HALF_UP));
        BigDecimal taxRate = new BigDecimal(settingService.getValue("tax.rate", "0"));
        BigDecimal tax = subtotal.multiply(taxRate)
                .divide(BigDecimal.valueOf(100), 2, RoundingMode.HALF_UP);
        order.setTaxAmount(tax);
        order.setTotalAmount(subtotal.add(tax).setScale(2, RoundingMode.HALF_UP));
        return orderRepository.save(order);
    }

    @Transactional
    public RestaurantOrder setStatus(UUID id, RestaurantOrderStatus status) {
        RestaurantOrder order = findById(id);
        if (order.getStatus() == RestaurantOrderStatus.Completed
                || order.getStatus() == RestaurantOrderStatus.Cancelled) {
            throw new InvalidOperationException("A completed or cancelled order cannot be changed.");
        }
        order.setStatus(status);
        return orderRepository.save(order);
    }

    @Transactional
    public RestaurantOrder cancel(UUID id) {
        RestaurantOrder order = findById(id);
        if (order.getStatus() != RestaurantOrderStatus.Placed
                && order.getStatus() != RestaurantOrderStatus.Preparing) {
            throw new InvalidOperationException("Only placed or preparing orders can be cancelled.");
        }
        order.setStatus(RestaurantOrderStatus.Cancelled);
        return orderRepository.save(order);
    }

    private OrderType parseOrderType(String value) {
        if (!StringUtils.hasText(value)) {
            return OrderType.DineIn;
        }
        try {
            return OrderType.valueOf(value);
        } catch (IllegalArgumentException ex) {
            return OrderType.DineIn;
        }
    }

    private String generateOrderNumber() {
        String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMddHHmmss"));
        int random = ThreadLocalRandom.current().nextInt(1000, 9999);
        String candidate;
        do {
            candidate = "ORD-" + timestamp + "-" + random;
        } while (orderRepository.existsByOrderNumber(candidate));
        return candidate;
    }
}
