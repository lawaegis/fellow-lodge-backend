package com.fellowlodge.api.service;

import com.fellowlodge.api.common.exception.InvalidOperationException;
import com.fellowlodge.api.common.exception.ResourceNotFoundException;
import com.fellowlodge.api.entity.Guest;
import com.fellowlodge.api.entity.Notification;
import com.fellowlodge.api.entity.User;
import com.fellowlodge.api.enums.NotificationType;
import com.fellowlodge.api.repository.GuestRepository;
import com.fellowlodge.api.repository.NotificationRepository;
import com.fellowlodge.api.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

/**
 * In-app notifications for staff users and portal guests.
 */
@Service
@RequiredArgsConstructor
public class NotificationService {

    private final NotificationRepository notificationRepository;
    private final UserRepository userRepository;
    private final GuestRepository guestRepository;

    public Page<Notification> findAll(int page, int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createdAt"));
        return notificationRepository.findAll(pageable);
    }

    public List<Notification> findByUserId(UUID userId) {
        return notificationRepository.findByUserIdOrderByCreatedAtDesc(userId);
    }

    public List<Notification> findByGuestId(UUID guestId) {
        return notificationRepository.findByGuestIdOrderByCreatedAtDesc(guestId);
    }

    public long countUnreadByUserId(UUID userId) {
        return notificationRepository.countByUserIdAndReadFalse(userId);
    }

    @Transactional
    public Notification create(Notification notification) {
        return notificationRepository.save(notification);
    }

    @Transactional
    public void markAsRead(UUID notificationId, UUID userId) {
        Notification notification = notificationRepository.findById(notificationId)
                .orElseThrow(() -> new ResourceNotFoundException("Notification", notificationId));
        if (userId != null && notification.getUserId() != null && !notification.getUserId().equals(userId)) {
            throw new InvalidOperationException("Notification does not belong to this user.");
        }
        notification.setRead(true);
        notificationRepository.save(notification);
    }

    @Transactional
    public void markAllAsRead(UUID userId) {
        List<Notification> unread = notificationRepository.findByUserIdAndReadFalseOrderByCreatedAtDesc(userId);
        unread.forEach(n -> n.setRead(true));
        notificationRepository.saveAll(unread);
    }

    @Transactional
    public void delete(UUID id) {
        notificationRepository.deleteById(id);
    }

    @Transactional
    public void notifyUser(UUID userId, String title, String message, NotificationType type) {
        Notification notification = new Notification();
        notification.setUserId(userId);
        notification.setTitle(title);
        notification.setMessage(message);
        notification.setType(type == null ? NotificationType.Info : type);
        notificationRepository.save(notification);
    }

    @Transactional
    public void notifyGuest(UUID guestId, String title, String message) {
        if (guestId == null) {
            return;
        }
        Guest guest = guestRepository.findById(guestId).orElse(null);
        if (guest == null) {
            return;
        }
        Notification notification = new Notification();
        notification.setGuestId(guestId);
        if (guest.getUserId() != null) {
            notification.setUserId(guest.getUserId());
        }
        notification.setTitle(title);
        notification.setMessage(message);
        notification.setType(NotificationType.Info);
        notificationRepository.save(notification);
    }

    @Transactional
    public void notifyAdmins(String title, String message) {
        userRepository.findAll().stream()
                .filter(User::isActive)
                .forEach(user -> {
                    Notification notification = new Notification();
                    notification.setUserId(user.getId());
                    notification.setTitle(title);
                    notification.setMessage(message);
                    notification.setType(NotificationType.Info);
                    notificationRepository.save(notification);
                });
    }
}
