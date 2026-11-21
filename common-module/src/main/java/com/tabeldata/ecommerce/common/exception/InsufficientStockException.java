package com.tabeldata.ecommerce.common.exception;

import java.util.UUID;

public class InsufficientStockException extends RuntimeException {
    private final UUID variantId;
    private final int requested;
    private final int available;

    public InsufficientStockException(UUID variantId, String productName, int requested, int available) {
        super(String.format(
                "Stok tidak mencukupi untuk '%s' (diminta: %d, tersedia: %d)",
                productName, requested, available
        ));
        this.variantId = variantId;
        this.requested = requested;
        this.available = available;
    }

    public UUID getVariantId() {
        return variantId;
    }

    public int getRequested() {
        return requested;
    }

    public int getAvailable() {
        return available;
    }
}
