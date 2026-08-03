package com.fellowlodge.api.repository;

import com.fellowlodge.api.entity.Transaction;
import com.fellowlodge.api.enums.TransactionType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

public interface TransactionRepository extends JpaRepository<Transaction, UUID>, JpaSpecificationExecutor<Transaction> {

    List<Transaction> findByGuestId(UUID guestId);

    List<Transaction> findByInvoiceId(UUID invoiceId);

    @Query("""
            select coalesce(sum(t.amount), 0) from Transaction t
            where t.transactionType = :type
              and t.status = com.fellowlodge.api.enums.TransactionStatus.Completed
            """)
    BigDecimal sumByType(@Param("type") TransactionType type);

    @Query("""
            select coalesce(sum(t.amount), 0) from Transaction t
            where t.transactionType = :type
              and t.status = com.fellowlodge.api.enums.TransactionStatus.Completed
              and t.createdAt between :start and :end
            """)
    BigDecimal sumByTypeBetween(@Param("type") TransactionType type,
                                @Param("start") LocalDateTime start,
                                @Param("end") LocalDateTime end);
}
