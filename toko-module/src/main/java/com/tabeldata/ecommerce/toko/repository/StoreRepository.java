package com.tabeldata.ecommerce.toko.repository;

import com.tabeldata.ecommerce.toko.entity.Store;
import com.tabeldata.ecommerce.toko.entity.StoreBadge;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface StoreRepository extends JpaRepository<Store, UUID> {

    Optional<Store> findBySlug(String slug);

    boolean existsBySlug(String slug);

    List<Store> findAllByIdIn(Collection<UUID> ids);

    @Query("""
            SELECT s FROM Store s
            WHERE s.active = true
              AND (:city IS NULL OR LOWER(s.city) LIKE LOWER(CONCAT('%', :city, '%')))
              AND (:badge IS NULL OR s.badge = :badge)
              AND (:q IS NULL OR LOWER(s.name) LIKE LOWER(CONCAT('%', :q, '%')))
            """)
    Page<Store> searchActiveStores(
            @Param("q") String query,
            @Param("city") String city,
            @Param("badge") StoreBadge badge,
            Pageable pageable
    );
}
