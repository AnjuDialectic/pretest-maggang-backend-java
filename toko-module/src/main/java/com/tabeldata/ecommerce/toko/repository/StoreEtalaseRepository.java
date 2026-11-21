package com.tabeldata.ecommerce.toko.repository;

import com.tabeldata.ecommerce.toko.entity.StoreEtalase;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface StoreEtalaseRepository extends JpaRepository<StoreEtalase, UUID> {
    List<StoreEtalase> findByStoreIdOrderByDisplayOrderAscNameAsc(UUID storeId);
    Optional<StoreEtalase> findByIdAndStoreId(UUID id, UUID storeId);
    boolean existsByIdAndStoreId(UUID id, UUID storeId);
    boolean existsByStoreIdAndSlug(UUID storeId, String slug);
    long countByStoreId(UUID storeId);
}
