package com.tabeldata.ecommerce.common.port;

import java.util.Collection;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

/**
 * Cross-module contract exposed by toko-module and consumed by barang-module and pembelian-module.
 * Keeps modules loosely coupled and ready for extraction into independent microservices.
 */
public interface StorePort {

    record StoreSummary(
            UUID id,
            String name,
            String slug,
            String logoUrl,
            String badge,
            String city,
            boolean isOnline,
            boolean isActive
    ) {}

    StoreSummary getStoreSummary(UUID storeId);

    Map<UUID, StoreSummary> getStoreSummaries(Collection<UUID> storeIds);

    boolean existsEtalaseInStore(UUID storeId, UUID etalaseId);

    Optional<String> findEtalaseName(UUID etalaseId);
}
