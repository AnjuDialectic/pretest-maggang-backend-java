package com.tabeldata.ecommerce.pembelian.dto;

import jakarta.validation.constraints.*;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;

public final class PembelianDtos {

    private PembelianDtos() {}

    public record AddCartItemRequest(
            @NotNull(message = "productVariantId wajib diisi") UUID productVariantId,
            @Min(value = 1, message = "Quantity minimal 1") int quantity,
            @Size(max = 255) String note
    ) {}

    public record UpdateCartItemRequest(
            @Min(value = 1, message = "Quantity minimal 1") Integer quantity,
            @Size(max = 255) String note,
            Boolean selected
    ) {}

    public record SelectAllCartRequest(boolean selected) {}

    public record CartItemDto(
            UUID id,
            UUID productId,
            UUID productVariantId,
            String productName,
            String variantLabel,
            String imageUrl,
            BigDecimal unitPrice,
            int quantity,
            BigDecimal subtotal,
            int availableStock,
            String note,
            boolean selected,
            boolean purchasable
    ) {}

    public record CartStoreGroupDto(
            UUID storeId,
            String storeName,
            String storeBadge,
            String storeCity,
            boolean allSelected,
            List<CartItemDto> items
    ) {}

    public record CartSummaryResponse(
            UUID cartId,
            UUID customerId,
            boolean allSelected,
            int totalDistinctItems,
            int selectedTotalQuantity,
            int selectedDistinctItems,
            BigDecimal selectedTotalPrice,
            List<CartStoreGroupDto> storeGroups
    ) {}

    public record CheckoutRequest(
            @NotBlank(message = "Nama kurir wajib diisi") @Size(max = 60) String courierName,
            @NotBlank(message = "Layanan kurir wajib diisi") @Size(max = 60) String courierService,
            Boolean freeShipping,
            @DecimalMin("0.0") BigDecimal shippingCostPerStore,
            @DecimalMin("0.0") BigDecimal serviceFee,
            @DecimalMin("0.0") BigDecimal promoDiscount,
            @Size(max = 500) String shippingAddress,
            @Size(max = 60) String paymentMethod,
            Boolean instantPay
    ) {}

    public record DirectBuyRequest(
            @NotNull UUID productVariantId,
            @Min(1) int quantity,
            @Size(max = 255) String note,
            @NotBlank @Size(max = 60) String courierName,
            @NotBlank @Size(max = 60) String courierService,
            Boolean freeShipping,
            @DecimalMin("0.0") BigDecimal shippingCost,
            @DecimalMin("0.0") BigDecimal serviceFee,
            @DecimalMin("0.0") BigDecimal promoDiscount,
            @Size(max = 500) String shippingAddress,
            @Size(max = 60) String paymentMethod,
            Boolean instantPay
    ) {}

    public record UpdateTransactionStatusRequest(
            @NotBlank @Pattern(regexp = "MENUNGGU_PEMBAYARAN|DIBAYAR|DIPROSES|DIKIRIM|TIBA_DI_TUJUAN|SELESAI|DIBATALKAN")
            String status,
            @Size(max = 80) String trackingNumber,
            @Size(max = 255) String description
    ) {}

    public record TransactionItemDto(
            UUID id,
            UUID productId,
            UUID productVariantId,
            String productName,
            String variantLabel,
            String imageUrl,
            BigDecimal unitPrice,
            int quantity,
            BigDecimal subtotal,
            String note
    ) {}

    public record StatusHistoryDto(
            UUID id,
            String status,
            String description,
            OffsetDateTime changedAt
    ) {}

    public record TransactionListCardDto(
            UUID id,
            String invoiceNumber,
            String transactionType,
            String status,
            String statusDisplay,
            OffsetDateTime purchasedAt,
            UUID storeId,
            String storeName,
            String storeBadge,
            TransactionItemDto primaryItem,
            int additionalItemCount,
            BigDecimal grandTotal
    ) {}

    public record TransactionDetailResponse(
            UUID id,
            String invoiceNumber,
            String transactionType,
            String status,
            String statusDisplay,
            OffsetDateTime purchasedAt,
            UUID customerId,
            UUID storeId,
            String storeName,
            String storeBadge,
            List<TransactionItemDto> items,
            String courierName,
            String courierService,
            boolean freeShipping,
            String trackingNumber,
            String shippingAddress,
            String paymentMethod,
            BigDecimal subtotalProduct,
            BigDecimal shippingCost,
            BigDecimal shippingDiscount,
            BigDecimal serviceFee,
            BigDecimal promoDiscount,
            BigDecimal grandTotal,
            List<StatusHistoryDto> statusHistories
    ) {}

    public record CheckoutResultResponse(
            int invoiceCount,
            BigDecimal combinedGrandTotal,
            List<TransactionDetailResponse> transactions
    ) {}
}
