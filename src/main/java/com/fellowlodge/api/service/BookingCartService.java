package com.fellowlodge.api.service;

import com.fellowlodge.api.common.exception.InvalidOperationException;
import com.fellowlodge.api.common.exception.ResourceNotFoundException;
import com.fellowlodge.api.dto.booking.AddToCartRequest;
import com.fellowlodge.api.dto.booking.CartItemRequest;
import com.fellowlodge.api.dto.booking.CartItemResponse;
import com.fellowlodge.api.dto.booking.CartResponse;
import com.fellowlodge.api.dto.booking.CheckoutRequest;
import com.fellowlodge.api.entity.BookingCart;
import com.fellowlodge.api.entity.BookingItem;
import com.fellowlodge.api.entity.Guest;
import com.fellowlodge.api.entity.Invoice;
import com.fellowlodge.api.entity.Payment;
import com.fellowlodge.api.entity.Promotion;
import com.fellowlodge.api.entity.Reservation;
import com.fellowlodge.api.entity.Room;
import com.fellowlodge.api.entity.RoomType;
import com.fellowlodge.api.enums.BookingCartStatus;
import com.fellowlodge.api.enums.InvoiceStatus;
import com.fellowlodge.api.enums.PaymentMethod;
import com.fellowlodge.api.enums.PaymentStatus;
import com.fellowlodge.api.enums.ReservationSource;
import com.fellowlodge.api.enums.ReservationStatus;
import com.fellowlodge.api.enums.RoomStatus;
import com.fellowlodge.api.repository.BookingCartRepository;
import com.fellowlodge.api.repository.BookingItemRepository;
import com.fellowlodge.api.repository.GuestRepository;
import com.fellowlodge.api.repository.InvoiceRepository;
import com.fellowlodge.api.repository.PaymentRepository;
import com.fellowlodge.api.repository.PromotionRepository;
import com.fellowlodge.api.repository.ReservationRepository;
import com.fellowlodge.api.repository.RoomRepository;
import com.fellowlodge.api.repository.RoomTypeRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.ThreadLocalRandom;

/**
 * Manages the guest portal shopping cart and its conversion into paid bookings.
 */
@Service
@RequiredArgsConstructor
public class BookingCartService {

    private final BookingCartRepository cartRepository;
    private final BookingItemRepository itemRepository;
    private final RoomTypeRepository roomTypeRepository;
    private final RoomRepository roomRepository;
    private final GuestRepository guestRepository;
    private final ReservationRepository reservationRepository;
    private final PaymentRepository paymentRepository;
    private final InvoiceRepository invoiceRepository;
    private final PromotionRepository promotionRepository;
    private final ReservationService reservationService;
    private final NotificationService notificationService;

    public CartResponse getOrCreateCart(UUID userId, UUID guestId, String sessionId) {
        BookingCart cart = findActiveCart(userId, guestId, sessionId);
        return toResponse(cart);
    }

    @Transactional
    public CartResponse addItems(UUID userId, UUID guestId, String sessionId, AddToCartRequest request) {
        BookingCart cart = findActiveCart(userId, guestId, sessionId);
        for (CartItemRequest itemRequest : request.items()) {
            addItem(cart, itemRequest);
        }
        return toResponse(cart);
    }

    @Transactional
    public CartResponse addItem(UUID userId, UUID guestId, String sessionId, CartItemRequest request) {
        BookingCart cart = findActiveCart(userId, guestId, sessionId);
        addItem(cart, request);
        return toResponse(cart);
    }

    @Transactional
    public void removeItem(UUID cartId, UUID itemId, UUID userId, UUID guestId, String sessionId) {
        BookingCart cart = requireOwnedCart(cartId, userId, guestId, sessionId);
        BookingItem item = itemRepository.findById(itemId)
                .orElseThrow(() -> new ResourceNotFoundException("Cart item", itemId));
        if (!item.getCartId().equals(cart.getId())) {
            throw new InvalidOperationException("Item does not belong to this cart.");
        }
        itemRepository.delete(item);
    }

    @Transactional
    public void clearCart(UUID cartId, UUID userId, UUID guestId, String sessionId) {
        requireOwnedCart(cartId, userId, guestId, sessionId);
        itemRepository.deleteByCartId(cartId);
    }

    public CartResponse viewCart(UUID cartId, UUID userId, UUID guestId, String sessionId) {
        return toResponse(requireOwnedCart(cartId, userId, guestId, sessionId));
    }

    @Transactional
    public List<Reservation> checkout(CheckoutRequest request, UUID userId, UUID guestId, String sessionId) {
        BookingCart cart = requireOwnedCart(request.cartId(), userId, guestId, sessionId);
        List<BookingItem> items = itemRepository.findByCartId(cart.getId());
        if (items.isEmpty()) {
            throw new InvalidOperationException("Cart is empty. Add rooms before checking out.");
        }

        Guest guest = resolveGuest(cart, request);
        PaymentMethod method = parseMethod(request.paymentMethod());
        BigDecimal discount = applyPromotion(request.promoCode());
        List<Reservation> created = new ArrayList<>();

        for (BookingItem item : items) {
            if (reservationRepository.hasOverlap(item.getRoomId(), item.getCheckInDate(),
                    item.getCheckOutDate(), null)) {
                throw new InvalidOperationException("Room is no longer available for "
                        + item.getCheckInDate() + " to " + item.getCheckOutDate() + ". Remove it from your cart.");
            }
            Reservation reservation = new Reservation();
            reservation.setGuestId(guest.getId());
            reservation.setRoomTypeId(item.getRoomTypeId());
            reservation.setRoomId(item.getRoomId());
            reservation.setCheckInDate(item.getCheckInDate());
            reservation.setCheckOutDate(item.getCheckOutDate());
            reservation.setNumberOfGuests(item.getNumberOfGuests());
            reservation.setTotalAmount(item.getTotalAmount());
            reservation.setDiscountPercent(discount);
            reservation.setSpecialRequests(request.specialRequests());
            reservation.setSource(ReservationSource.WEB);
            reservation.setStatus(ReservationStatus.Confirmed);
            reservation.setBookedBy(userId);
            Reservation saved = reservationRepository.save(reservation);

            markRoomReserved(saved);
            paymentRepository.save(buildPayment(saved, guest.getId(), method, userId));
            invoiceRepository.save(buildInvoice(saved));
            created.add(saved);
        }

        cart.setStatus(BookingCartStatus.CheckedOut);
        cartRepository.save(cart);
        notificationService.notifyGuest(guest.getId(), "Booking confirmed",
                "Your booking has been confirmed. Confirmation for " + created.size() + " reservation(s).");
        return created;
    }

    // ==================== INTERNALS ====================

    private BookingCart requireOwnedCart(UUID cartId, UUID userId, UUID guestId, String sessionId) {
        BookingCart cart = cartRepository.findById(cartId)
                .orElseThrow(() -> new ResourceNotFoundException("Cart", cartId));
        if (!isOwnedBy(cart, userId, guestId, sessionId)) {
            // 404 keeps the existence of other carts private.
            throw new ResourceNotFoundException("Cart", cartId);
        }
        return cart;
    }

    private boolean isOwnedBy(BookingCart cart, UUID userId, UUID guestId, String sessionId) {
        if (cart.getUserId() != null) {
            return userId != null && cart.getUserId().equals(userId);
        }
        if (cart.getGuestId() != null) {
            return guestId != null && cart.getGuestId().equals(guestId);
        }
        return cart.getSessionId() != null && sessionId != null && cart.getSessionId().equals(sessionId);
    }

    private BookingCart findActiveCart(UUID userId, UUID guestId, String sessionId) {
        if (userId != null) {
            return cartRepository.findByUserIdAndStatus(userId, BookingCartStatus.Active)
                    .orElseGet(() -> createCart(userId, null, null));
        }
        if (guestId != null) {
            return cartRepository.findByGuestIdAndStatus(guestId, BookingCartStatus.Active)
                    .orElseGet(() -> createCart(null, guestId, null));
        }
        if (sessionId != null) {
            return cartRepository.findBySessionIdAndStatus(sessionId, BookingCartStatus.Active)
                    .orElseGet(() -> createCart(null, null, sessionId));
        }
        throw new InvalidOperationException("Unable to identify a cart. Sign in or provide a session.");
    }

    private BookingCart createCart(UUID userId, UUID guestId, String sessionId) {
        BookingCart cart = new BookingCart();
        cart.setUserId(userId);
        cart.setGuestId(guestId);
        cart.setSessionId(sessionId);
        return cartRepository.save(cart);
    }

    private void addItem(BookingCart cart, CartItemRequest request) {
        if (!request.checkOutDate().isAfter(request.checkInDate())) {
            throw new InvalidOperationException("Check-out date must be after check-in date.");
        }
        RoomType type = request.roomTypeId() != null
                ? roomTypeRepository.findById(request.roomTypeId())
                .orElseThrow(() -> new ResourceNotFoundException("Room type", request.roomTypeId()))
                : null;
        Room room = null;
        if (request.roomId() != null) {
            room = roomRepository.findById(request.roomId())
                    .orElseThrow(() -> new ResourceNotFoundException("Room", request.roomId()));
            if (room.getStatus() != RoomStatus.Available) {
                throw new InvalidOperationException("Room " + room.getRoomNumber() + " is not available.");
            }
            if (reservationRepository.hasOverlap(room.getId(), request.checkInDate(), request.checkOutDate(), null)) {
                throw new InvalidOperationException("Room " + room.getRoomNumber()
                        + " is already reserved for the selected dates.");
            }
        } else if (type == null) {
            throw new InvalidOperationException("Select a room or a room type.");
        }

        long nights = ReservationService.nightsBetween(request.checkInDate(), request.checkOutDate());
        BigDecimal pricePerNight = room != null ? room.getPricePerNight() : type.getBasePrice();
        BigDecimal total = pricePerNight.multiply(BigDecimal.valueOf(nights))
                .multiply(BigDecimal.valueOf(request.quantity()))
                .setScale(2, RoundingMode.HALF_UP);

        BookingItem item = new BookingItem();
        item.setCartId(cart.getId());
        item.setRoomId(room == null ? null : room.getId());
        item.setRoomTypeId(room != null ? room.getRoomTypeId() : type.getId());
        item.setCheckInDate(request.checkInDate());
        item.setCheckOutDate(request.checkOutDate());
        item.setNumberOfGuests(request.numberOfGuests());
        item.setQuantity(request.quantity());
        item.setPricePerNight(pricePerNight);
        item.setTotalAmount(total);
        itemRepository.save(item);
    }

    private Guest resolveGuest(BookingCart cart, CheckoutRequest request) {
        if (cart.getGuestId() != null) {
            return guestRepository.findById(cart.getGuestId())
                    .orElseThrow(() -> new ResourceNotFoundException("Guest", cart.getGuestId()));
        }
        if (cart.getUserId() != null) {
            return guestRepository.findByUserId(cart.getUserId())
                    .orElseGet(() -> createGuestFromRequest(request));
        }
        return createGuestFromRequest(request);
    }

    private Guest createGuestFromRequest(CheckoutRequest request) {
        if (request.guestName() == null || request.guestName().isBlank()) {
            throw new InvalidOperationException("Guest name is required for checkout.");
        }
        Guest guest = new Guest();
        String name = request.guestName().trim();
        int space = name.indexOf(' ');
        if (space > 0) {
            guest.setFirstName(name.substring(0, space));
            guest.setLastName(name.substring(space + 1));
        } else {
            guest.setFirstName(name);
            guest.setLastName("");
        }
        guest.setEmail(request.guestEmail());
        guest.setPhone(request.guestPhone());
        return guestRepository.save(guest);
    }

    private BigDecimal applyPromotion(String promoCode) {
        if (promoCode == null || promoCode.isBlank()) {
            return BigDecimal.ZERO;
        }
        Promotion promotion = promotionRepository.findByCodeIgnoreCase(promoCode)
                .filter(p -> p.isActive())
                .filter(p -> p.getValidFrom() == null || !p.getValidFrom().isAfter(LocalDate.now()))
                .filter(p -> p.getValidTo() == null || !p.getValidTo().isBefore(LocalDate.now()))
                .orElseThrow(() -> new InvalidOperationException("Promotion code is invalid or expired."));
        return promotion.getDiscountPercent();
    }

    private void markRoomReserved(Reservation reservation) {
        if (reservation.getRoomId() == null) {
            return;
        }
        roomRepository.findById(reservation.getRoomId()).ifPresent(room -> {
            if (room.getStatus() == RoomStatus.Available) {
                room.setStatus(RoomStatus.Reserved);
                roomRepository.save(room);
            }
        });
    }

    private Payment buildPayment(Reservation reservation, UUID guestId, PaymentMethod method, UUID bookedBy) {
        Payment payment = new Payment();
        payment.setReservationId(reservation.getId());
        payment.setGuestId(guestId);
        payment.setAmount(reservation.getTotalAmount());
        payment.setPaymentMethod(method);
        payment.setPaymentStatus(PaymentStatus.Completed);
        payment.setReferenceNumber("PAY-" + System.currentTimeMillis());
        payment.setReceivedBy(bookedBy);
        return payment;
    }

    private Invoice buildInvoice(Reservation reservation) {
        Invoice invoice = new Invoice();
        invoice.setReservationId(reservation.getId());
        invoice.setGuestId(reservation.getGuestId());
        invoice.setInvoiceNumber("INV-" + LocalDate.now().format(DateTimeFormatter.ofPattern("yyyyMMdd"))
                + "-" + ThreadLocalRandom.current().nextInt(1000, 9999));
        invoice.setSubtotal(reservation.getTotalAmount());
        invoice.setTaxAmount(BigDecimal.ZERO);
        invoice.setDiscountAmount(BigDecimal.ZERO);
        invoice.setTotalAmount(reservation.getTotalAmount());
        invoice.setStatus(InvoiceStatus.Sent);
        invoice.setDueDate(LocalDate.now().plusDays(14));
        return invoice;
    }

    private PaymentMethod parseMethod(String value) {
        if (value == null || value.isBlank()) {
            return PaymentMethod.Online;
        }
        try {
            return PaymentMethod.valueOf(value);
        } catch (IllegalArgumentException e) {
            return PaymentMethod.Online;
        }
    }

    private CartResponse toResponse(BookingCart cart) {
        List<BookingItem> items = itemRepository.findByCartId(cart.getId());
        List<CartItemResponse> responses = new ArrayList<>();
        BigDecimal subtotal = BigDecimal.ZERO;
        int nightsTotal = 0;
        for (BookingItem item : items) {
            RoomType type = item.getRoomTypeId() == null ? null
                    : roomTypeRepository.findById(item.getRoomTypeId()).orElse(null);
            Room room = item.getRoomId() == null ? null
                    : roomRepository.findById(item.getRoomId()).orElse(null);
            long nights = ReservationService.nightsBetween(item.getCheckInDate(), item.getCheckOutDate());
            responses.add(CartItemResponse.builder()
                    .itemId(item.getId())
                    .roomTypeId(item.getRoomTypeId())
                    .roomTypeName(type == null ? "Room" : type.getName())
                    .roomId(item.getRoomId())
                    .roomNumber(room == null ? null : room.getRoomNumber())
                    .checkInDate(item.getCheckInDate())
                    .checkOutDate(item.getCheckOutDate())
                    .numberOfGuests(item.getNumberOfGuests())
                    .quantity(item.getQuantity())
                    .nights((int) nights)
                    .pricePerNight(item.getPricePerNight())
                    .totalAmount(item.getTotalAmount())
                    .build());
            subtotal = subtotal.add(item.getTotalAmount());
            nightsTotal += nights * item.getQuantity();
        }
        return CartResponse.builder()
                .cartId(cart.getId())
                .items(responses)
                .subtotal(subtotal)
                .discount(BigDecimal.ZERO)
                .total(subtotal)
                .nightsTotal(nightsTotal)
                .build();
    }
}
