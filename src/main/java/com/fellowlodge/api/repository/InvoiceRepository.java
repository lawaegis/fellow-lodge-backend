package com.fellowlodge.api.repository;

import com.fellowlodge.api.entity.Invoice;
import com.fellowlodge.api.enums.InvoiceStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface InvoiceRepository extends JpaRepository<Invoice, UUID>, JpaSpecificationExecutor<Invoice> {

    Optional<Invoice> findByInvoiceNumber(String invoiceNumber);

    List<Invoice> findByReservationId(UUID reservationId);

    List<Invoice> findByGuestId(UUID guestId);

    List<Invoice> findByStatus(InvoiceStatus status);
}
