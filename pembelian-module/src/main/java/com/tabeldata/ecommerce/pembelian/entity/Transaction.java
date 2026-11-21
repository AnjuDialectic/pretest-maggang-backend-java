package com.tabeldata.ecommerce.pembelian.entity;

import com.tabeldata.ecommerce.common.entity.BaseAuditEntity;
import jakarta.persistence.*;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Entity
@Table(name = "transactions")
public class Transaction extends BaseAuditEntity {

    @Id
    @Column(name = "id", nullable = false, updatable = false)
    private UUID id;

    @Column(name = "invoice_number", nullable = false, unique = true, length = 60)
    private String invoiceNumber;

    @Column(name = "customer_id", nullable = false)
    private UUID customerId;

    @Column(name = "store_id", nullable = false)
    private UUID storeId;

    @Column(name = "store_name_snapshot", nullable = false, length = 120)
    private String storeNameSnapshot;

    @Column(name = "store_badge_snapshot", nullable = false, length = 30)
    private String storeBadgeSnapshot = "REGULAR";

    @Column(name = "transaction_type", nullable = false, length = 30)
    private String transactionType = "BELANJA";

    @Column(name = "status", nullable = false, length = 40)
    private String status = "MENUNGGU_PEMBAYARAN";

    @Column(name = "subtotal_product", nullable = false, precision = 19, scale = 2)
    private BigDecimal subtotalProduct = BigDecimal.ZERO;

    @Column(name = "shipping_cost", nullable = false, precision = 19, scale = 2)
    private BigDecimal shippingCost = BigDecimal.ZERO;

    @Column(name = "shipping_discount", nullable = false, precision = 19, scale = 2)
    private BigDecimal shippingDiscount = BigDecimal.ZERO;

    @Column(name = "service_fee", nullable = false, precision = 19, scale = 2)
    private BigDecimal serviceFee = BigDecimal.ZERO;

    @Column(name = "promo_discount", nullable = false, precision = 19, scale = 2)
    private BigDecimal promoDiscount = BigDecimal.ZERO;

    @Column(name = "grand_total", nullable = false, precision = 19, scale = 2)
    private BigDecimal grandTotal = BigDecimal.ZERO;

    @Column(name = "courier_name", nullable = false, length = 60)
    private String courierName;

    @Column(name = "courier_service", nullable = false, length = 60)
    private String courierService;

    @Column(name = "is_free_shipping", nullable = false)
    private boolean freeShipping = false;

    @Column(name = "tracking_number", length = 80)
    private String trackingNumber;

    @Column(name = "shipping_address", nullable = false, columnDefinition = "TEXT")
    private String shippingAddress;

    @Column(name = "payment_method", length = 60)
    private String paymentMethod;

    @Column(name = "purchased_at", nullable = false)
    private OffsetDateTime purchasedAt;

    @OneToMany(mappedBy = "transaction", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<TransactionItem> items = new ArrayList<>();

    @OneToMany(mappedBy = "transaction", cascade = CascadeType.ALL, orphanRemoval = true)
    @OrderBy("changedAt DESC")
    private List<TransactionStatusHistory> statusHistories = new ArrayList<>();

    @PrePersist
    protected void ensureTxDefaults() {
        if (this.id == null) {
            this.id = UUID.randomUUID();
        }
        if (this.purchasedAt == null) {
            this.purchasedAt = OffsetDateTime.now();
        }
    }

    public void appendHistory(String newStatus, String desc) {
        TransactionStatusHistory h = new TransactionStatusHistory();
        h.setTransaction(this);
        h.setStatus(newStatus);
        h.setDescription(desc);
        h.setChangedAt(OffsetDateTime.now());
        this.statusHistories.add(h);
    }

    public UUID getId() { return id; }
    public void setId(UUID id) { this.id = id; }
    public String getInvoiceNumber() { return invoiceNumber; }
    public void setInvoiceNumber(String invoiceNumber) { this.invoiceNumber = invoiceNumber; }
    public UUID getCustomerId() { return customerId; }
    public void setCustomerId(UUID customerId) { this.customerId = customerId; }
    public UUID getStoreId() { return storeId; }
    public void setStoreId(UUID storeId) { this.storeId = storeId; }
    public String getStoreNameSnapshot() { return storeNameSnapshot; }
    public void setStoreNameSnapshot(String storeNameSnapshot) { this.storeNameSnapshot = storeNameSnapshot; }
    public String getStoreBadgeSnapshot() { return storeBadgeSnapshot; }
    public void setStoreBadgeSnapshot(String storeBadgeSnapshot) { this.storeBadgeSnapshot = storeBadgeSnapshot; }
    public String getTransactionType() { return transactionType; }
    public void setTransactionType(String transactionType) { this.transactionType = transactionType; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    public BigDecimal getSubtotalProduct() { return subtotalProduct; }
    public void setSubtotalProduct(BigDecimal subtotalProduct) { this.subtotalProduct = subtotalProduct; }
    public BigDecimal getShippingCost() { return shippingCost; }
    public void setShippingCost(BigDecimal shippingCost) { this.shippingCost = shippingCost; }
    public BigDecimal getShippingDiscount() { return shippingDiscount; }
    public void setShippingDiscount(BigDecimal shippingDiscount) { this.shippingDiscount = shippingDiscount; }
    public BigDecimal getServiceFee() { return serviceFee; }
    public void setServiceFee(BigDecimal serviceFee) { this.serviceFee = serviceFee; }
    public BigDecimal getPromoDiscount() { return promoDiscount; }
    public void setPromoDiscount(BigDecimal promoDiscount) { this.promoDiscount = promoDiscount; }
    public BigDecimal getGrandTotal() { return grandTotal; }
    public void setGrandTotal(BigDecimal grandTotal) { this.grandTotal = grandTotal; }
    public String getCourierName() { return courierName; }
    public void setCourierName(String courierName) { this.courierName = courierName; }
    public String getCourierService() { return courierService; }
    public void setCourierService(String courierService) { this.courierService = courierService; }
    public boolean isFreeShipping() { return freeShipping; }
    public void setFreeShipping(boolean freeShipping) { this.freeShipping = freeShipping; }
    public String getTrackingNumber() { return trackingNumber; }
    public void setTrackingNumber(String trackingNumber) { this.trackingNumber = trackingNumber; }
    public String getShippingAddress() { return shippingAddress; }
    public void setShippingAddress(String shippingAddress) { this.shippingAddress = shippingAddress; }
    public String getPaymentMethod() { return paymentMethod; }
    public void setPaymentMethod(String paymentMethod) { this.paymentMethod = paymentMethod; }
    public OffsetDateTime getPurchasedAt() { return purchasedAt; }
    public void setPurchasedAt(OffsetDateTime purchasedAt) { this.purchasedAt = purchasedAt; }
    public List<TransactionItem> getItems() { return items; }
    public List<TransactionStatusHistory> getStatusHistories() { return statusHistories; }

    @Entity(name = "TransactionItem")
    @Table(name = "transaction_items")
    public static class TransactionItem {
        @Id
        @Column(name = "id", nullable = false, updatable = false)
        private UUID id;

        @ManyToOne(fetch = FetchType.LAZY, optional = false)
        @JoinColumn(name = "transaction_id", nullable = false)
        private Transaction transaction;

        @Column(name = "product_id", nullable = false)
        private UUID productId;

        @Column(name = "product_variant_id", nullable = false)
        private UUID productVariantId;

        @Column(name = "product_name_snapshot", nullable = false, length = 200)
        private String productNameSnapshot;

        @Column(name = "variant_label_snapshot", length = 180)
        private String variantLabelSnapshot;

        @Column(name = "product_image_snapshot", length = 500)
        private String productImageSnapshot;

        @Column(name = "unit_price_snapshot", nullable = false, precision = 19, scale = 2)
        private BigDecimal unitPriceSnapshot;

        @Column(name = "quantity", nullable = false)
        private int quantity;

        @Column(name = "subtotal", nullable = false, precision = 19, scale = 2)
        private BigDecimal subtotal;

        @Column(name = "note", length = 255)
        private String note;

        @PrePersist
        protected void initId() {
            if (this.id == null) this.id = UUID.randomUUID();
        }

        public UUID getId() { return id; }
        public void setId(UUID id) { this.id = id; }
        public Transaction getTransaction() { return transaction; }
        public void setTransaction(Transaction transaction) { this.transaction = transaction; }
        public UUID getProductId() { return productId; }
        public void setProductId(UUID productId) { this.productId = productId; }
        public UUID getProductVariantId() { return productVariantId; }
        public void setProductVariantId(UUID productVariantId) { this.productVariantId = productVariantId; }
        public String getProductNameSnapshot() { return productNameSnapshot; }
        public void setProductNameSnapshot(String productNameSnapshot) { this.productNameSnapshot = productNameSnapshot; }
        public String getVariantLabelSnapshot() { return variantLabelSnapshot; }
        public void setVariantLabelSnapshot(String variantLabelSnapshot) { this.variantLabelSnapshot = variantLabelSnapshot; }
        public String getProductImageSnapshot() { return productImageSnapshot; }
        public void setProductImageSnapshot(String productImageSnapshot) { this.productImageSnapshot = productImageSnapshot; }
        public BigDecimal getUnitPriceSnapshot() { return unitPriceSnapshot; }
        public void setUnitPriceSnapshot(BigDecimal unitPriceSnapshot) { this.unitPriceSnapshot = unitPriceSnapshot; }
        public int getQuantity() { return quantity; }
        public void setQuantity(int quantity) { this.quantity = quantity; }
        public BigDecimal getSubtotal() { return subtotal; }
        public void setSubtotal(BigDecimal subtotal) { this.subtotal = subtotal; }
        public String getNote() { return note; }
        public void setNote(String note) { this.note = note; }
    }

    @Entity(name = "TransactionStatusHistory")
    @Table(name = "transaction_status_histories")
    public static class TransactionStatusHistory {
        @Id
        @Column(name = "id", nullable = false, updatable = false)
        private UUID id;

        @ManyToOne(fetch = FetchType.LAZY, optional = false)
        @JoinColumn(name = "transaction_id", nullable = false)
        private Transaction transaction;

        @Column(name = "status", nullable = false, length = 40)
        private String status;

        @Column(name = "description", nullable = false, length = 255)
        private String description;

        @Column(name = "changed_at", nullable = false)
        private OffsetDateTime changedAt;

        @PrePersist
        protected void initId() {
            if (this.id == null) this.id = UUID.randomUUID();
            if (this.changedAt == null) this.changedAt = OffsetDateTime.now();
        }

        public UUID getId() { return id; }
        public Transaction getTransaction() { return transaction; }
        public void setTransaction(Transaction transaction) { this.transaction = transaction; }
        public String getStatus() { return status; }
        public void setStatus(String status) { this.status = status; }
        public String getDescription() { return description; }
        public void setDescription(String description) { this.description = description; }
        public OffsetDateTime getChangedAt() { return changedAt; }
        public void setChangedAt(OffsetDateTime changedAt) { this.changedAt = changedAt; }
    }
}
