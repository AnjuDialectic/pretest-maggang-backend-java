package com.tabeldata.ecommerce.toko.repository;

import com.tabeldata.ecommerce.toko.entity.StoreFollower;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface StoreFollowerRepository extends JpaRepository<StoreFollower, UUID> {
    boolean existsByStoreIdAndCustomerId(UUID storeId, UUID customerId);
    Optional<StoreFollower> findByStoreIdAndCustomerId(UUID storeId, UUID customerId);
    long countByStoreId(UUID storeId);
}
