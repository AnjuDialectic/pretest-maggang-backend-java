package com.tabeldata.ecommerce.common.port;

import java.math.BigDecimal;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * Cross-module contract exposed by barang-module and consumed by pembelian-module.
 */
public interface ProductPort {

    record VariantSnapshot(
            UUID variantId,
            UUID productId,
            UUID storeId,
            String productName,
            String variantLabel,
            String sku,
            String imageUrl,
            BigDecimal price,
            int stock,
            int weightGram,
            boolean productActive,
            boolean variantActive
    ) {
        public boolean isPurchasable() {
            return productActive && variantActive;
        }
    }

    record StockAdjustment(UUID variantId, int quantity) {}

    VariantSnapshot getVariantSnapshot(UUID variantId);

    Map<UUID, VariantSnapshot> getVariantSnapshots(Collection<UUID> variantIds);

    void reserveStock(List<StockAdjustment> adjustments);

    void releaseStock(List<StockAdjustment> adjustments);

    void addToWishlist(UUID customerId, UUID productId);
}
