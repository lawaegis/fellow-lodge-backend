package com.fellowlodge.api.service;

import com.fellowlodge.api.common.exception.ResourceNotFoundException;
import com.fellowlodge.api.dto.portal.SupportTicketRequest;
import com.fellowlodge.api.entity.Guest;
import com.fellowlodge.api.entity.SupportTicket;
import com.fellowlodge.api.enums.SupportTicketPriority;
import com.fellowlodge.api.enums.SupportTicketStatus;
import com.fellowlodge.api.repository.SupportTicketRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.ThreadLocalRandom;

/**
 * Guest support requests submitted from the portal. Tickets are always scoped
 * to the submitting user; staff can read all of them via the support endpoint.
 */
@Service
@RequiredArgsConstructor
public class SupportService {

    private final SupportTicketRepository ticketRepository;
    private final GuestService guestService;

    public List<SupportTicket> findByUserId(UUID userId) {
        return ticketRepository.findByUserIdOrderByCreatedAtDesc(userId);
    }

    public List<SupportTicket> findAll() {
        return ticketRepository.findAll(Sort.by(Sort.Direction.DESC, "createdAt"));
    }

    public SupportTicket findById(UUID id) {
        return ticketRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Support ticket", id));
    }

    public long countOpen() {
        return ticketRepository.findAll().stream()
                .filter(t -> t.getStatus() == SupportTicketStatus.Open
                        || t.getStatus() == SupportTicketStatus.InProgress)
                .count();
    }

    @Transactional
    public SupportTicket setStatus(UUID id, SupportTicketStatus status) {
        SupportTicket ticket = findById(id);
        ticket.setStatus(status);
        return ticketRepository.save(ticket);
    }

    @Transactional
    public SupportTicket setPriority(UUID id, SupportTicketPriority priority) {
        SupportTicket ticket = findById(id);
        ticket.setPriority(priority);
        return ticketRepository.save(ticket);
    }

    @Transactional
    public SupportTicket create(SupportTicketRequest request, UUID userId) {
        SupportTicket ticket = new SupportTicket();
        ticket.setTicketNumber(generateTicketNumber());
        ticket.setUserId(userId);
        Guest guest = guestService.findByUserId(userId);
        if (guest != null) {
            ticket.setGuestId(guest.getId());
        }
        ticket.setSubject(request.subject());
        ticket.setMessage(request.message());
        if (StringUtils.hasText(request.category())) {
            ticket.setCategory(request.category());
        }
        ticket.setPriority(parsePriority(request.priority()));
        ticket.setStatus(SupportTicketStatus.Open);
        return ticketRepository.save(ticket);
    }

    private SupportTicketPriority parsePriority(String value) {
        if (!StringUtils.hasText(value)) {
            return SupportTicketPriority.Medium;
        }
        try {
            return SupportTicketPriority.valueOf(value);
        } catch (IllegalArgumentException ex) {
            return SupportTicketPriority.Medium;
        }
    }

    private String generateTicketNumber() {
        String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMddHHmmss"));
        int random = ThreadLocalRandom.current().nextInt(1000, 9999);
        String candidate;
        do {
            candidate = "TCK-" + timestamp + "-" + random;
        } while (ticketRepository.existsByTicketNumber(candidate));
        return candidate;
    }
}
