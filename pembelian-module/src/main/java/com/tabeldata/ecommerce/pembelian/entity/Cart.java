package com.tabeldata.ecommerce.pembelian.entity;

import com.tabeldata.ecommerce.common.entity.BaseAuditEntity;
import jakarta.persistence.*;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Entity
@Table(name = "carts")
public class Cart extends BaseAuditEntity {

    @Id
    @Column(name = "id", nullable = false, updatable = false)
    private UUID id;

    @Column(name = "customer_id", nullable = false, unique = true)
    private UUID customerId;

    @OneToMany(mappedBy = "cart", cascade = CascadeType.ALL, orphanRemoval = true)
    @OrderBy("createdAt ASC")
    private List<CartItem> items = new ArrayList<>();

    @PrePersist
    protected void ensureCartId() {
        if (this.id == null) {
            this.id = UUID.randomUUID();
        }
    }

    public UUID getId() { return id; }
    public void setId(UUID id) { this.id = id; }
    public UUID getCustomerId() { return customerId; }
    public void setCustomerId(UUID customerId) { this.customerId = customerId; }
    public List<CartItem> getItems() { return items; }

    @Entity(name = "CartItem")
    @Table(name = "cart_items")
    public static class CartItem extends BaseAuditEntity {
        @Id
        @Column(name = "id", nullable = false, updatable = false)
        private UUID id;

        @ManyToOne(fetch = FetchType.LAZY, optional = false)
        @JoinColumn(name = "cart_id", nullable = false)
        private Cart cart;

        @Column(name = "store_id", nullable = false)
        private UUID storeId;

        @Column(name = "product_id", nullable = false)
        private UUID productId;

        @Column(name = "product_variant_id", nullable = false)
        private UUID productVariantId;

        @Column(name = "quantity", nullable = false)
        private int quantity = 1;

        @Column(name = "note", length = 255)
        private String note;

        @Column(name = "is_selected", nullable = false)
        private boolean selected = true;

        @PrePersist
        protected void initId() {
            if (this.id == null) this.id = UUID.randomUUID();
        }

        public UUID getId() { return id; }
        public void setId(UUID id) { this.id = id; }
        public Cart getCart() { return cart; }
        public void setCart(Cart cart) { this.cart = cart; }
        public UUID getStoreId() { return storeId; }
        public void setStoreId(UUID storeId) { this.storeId = storeId; }
        public UUID getProductId() { return productId; }
        public void setProductId(UUID productId) { this.productId = productId; }
        public UUID getProductVariantId() { return productVariantId; }
        public void setProductVariantId(UUID productVariantId) { this.productVariantId = productVariantId; }
        public int getQuantity() { return quantity; }
        public void setQuantity(int quantity) { this.quantity = quantity; }
        public String getNote() { return note; }
        public void setNote(String note) { this.note = note; }
        public boolean isSelected() { return selected; }
        public void setSelected(boolean selected) { this.selected = selected; }
    }

    @Entity(name = "Customer")
    @Table(name = "customers")
    public static class Customer extends BaseAuditEntity {
        @Id
        @Column(name = "id", nullable = false, updatable = false)
        private UUID id;

        @Column(name = "full_name", nullable = false, length = 120)
        private String fullName;

        @Column(name = "email", nullable = false, unique = true, length = 160)
        private String email;

        @Column(name = "phone", length = 30)
        private String phone;

        @Column(name = "default_address", columnDefinition = "TEXT")
        private String defaultAddress;

        @PrePersist
        protected void initId() {
            if (this.id == null) this.id = UUID.randomUUID();
        }

        public UUID getId() { return id; }
        public void setId(UUID id) { this.id = id; }
        public String getFullName() { return fullName; }
        public void setFullName(String fullName) { this.fullName = fullName; }
        public String getEmail() { return email; }
        public void setEmail(String email) { this.email = email; }
        public String getPhone() { return phone; }
        public void setPhone(String phone) { this.phone = phone; }
        public String getDefaultAddress() { return defaultAddress; }
        public void setDefaultAddress(String defaultAddress) { this.defaultAddress = defaultAddress; }
    }
}
