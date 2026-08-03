package com.fellowlodge.api.repository;

import com.fellowlodge.api.entity.SupportTicket;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface SupportTicketRepository extends JpaRepository<SupportTicket, UUID> {

    List<SupportTicket> findByUserIdOrderByCreatedAtDesc(UUID userId);

    boolean existsByTicketNumber(String ticketNumber);
}
