package com.tabeldata.ecommerce.pembelian.repository;

import com.tabeldata.ecommerce.pembelian.entity.Cart;
import com.tabeldata.ecommerce.pembelian.entity.Transaction;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.OffsetDateTime;
import java.util.Collection;
import java.util.Optional;
import java.util.UUID;

public interface TransactionRepository extends JpaRepository<Transaction, UUID> {

    boolean existsByInvoiceNumber(String invoiceNumber);

    Optional<Transaction> findByIdAndCustomerId(UUID id, UUID customerId);

    @Query("""
            SELECT DISTINCT t FROM Transaction t
            LEFT JOIN t.items it
            WHERE t.customerId = :customerId
              AND (:filterStatus = false OR t.status IN :statuses)
              AND (:txType IS NULL OR t.transactionType = :txType)
              AND (:fromTime IS NULL OR t.purchasedAt >= :fromTime)
              AND (:toTime IS NULL OR t.purchasedAt <= :toTime)
              AND (:keyword IS NULL
                   OR LOWER(t.invoiceNumber) LIKE LOWER(CONCAT('%', :keyword, '%'))
                   OR LOWER(t.storeNameSnapshot) LIKE LOWER(CONCAT('%', :keyword, '%'))
                   OR LOWER(it.productNameSnapshot) LIKE LOWER(CONCAT('%', :keyword, '%')))
            """)
    Page<Transaction> searchCustomerTransactions(
            @Param("customerId") UUID customerId,
            @Param("filterStatus") boolean filterStatus,
            @Param("statuses") Collection<String> statuses,
            @Param("txType") String txType,
            @Param("fromTime") OffsetDateTime fromTime,
            @Param("toTime") OffsetDateTime toTime,
            @Param("keyword") String keyword,
            Pageable pageable
    );

    @Query("SELECT COUNT(t) FROM Transaction t WHERE t.customerId = :customerId AND t.status = 'MENUNGGU_PEMBAYARAN'")
    long countWaitingPayment(@Param("customerId") UUID customerId);

    @Query("SELECT c FROM Cart c LEFT JOIN FETCH c.items WHERE c.customerId = :customerId")
    Optional<Cart> findCartWithItemsByCustomerId(@Param("customerId") UUID customerId);
}
