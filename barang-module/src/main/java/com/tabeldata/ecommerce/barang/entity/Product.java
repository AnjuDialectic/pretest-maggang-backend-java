package com.tabeldata.ecommerce.barang.entity;

import com.tabeldata.ecommerce.common.entity.BaseAuditEntity;
import jakarta.persistence.*;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Entity
@Table(name = "products")
public class Product extends BaseAuditEntity {

    @Id
    @Column(name = "id", nullable = false, updatable = false)
    private UUID id;

    @Column(name = "store_id", nullable = false)
    private UUID storeId;

    @Column(name = "category_id")
    private UUID categoryId;

    @Column(name = "etalase_id")
    private UUID etalaseId;

    @Column(name = "name", nullable = false, length = 200)
    private String name;

    @Column(name = "slug", nullable = false, length = 220)
    private String slug;

    @Column(name = "condition", nullable = false, length = 20)
    private String condition = "NEW";

    @Column(name = "weight_gram", nullable = false)
    private int weightGram = 1000;

    @Column(name = "description", columnDefinition = "TEXT")
    private String description;

    @Column(name = "important_info", columnDefinition = "TEXT")
    private String importantInfo;

    @Column(name = "is_preorder", nullable = false)
    private boolean preorder = false;

    @Column(name = "preorder_days")
    private Integer preorderDays;

    @Column(name = "campaign_label", length = 80)
    private String campaignLabel;

    @Column(name = "min_price", nullable = false, precision = 19, scale = 2)
    private BigDecimal minPrice = BigDecimal.ZERO;

    @Column(name = "max_price", nullable = false, precision = 19, scale = 2)
    private BigDecimal maxPrice = BigDecimal.ZERO;

    @Column(name = "view_count", nullable = false)
    private long viewCount = 0;

    @Column(name = "sold_count", nullable = false)
    private int soldCount = 0;

    @Column(name = "rating_avg", nullable = false, precision = 3, scale = 2)
    private BigDecimal ratingAvg = new BigDecimal("5.00");

    @Column(name = "review_count", nullable = false)
    private int reviewCount = 0;

    @Column(name = "is_active", nullable = false)
    private boolean active = true;

    @OneToMany(mappedBy = "product", cascade = CascadeType.ALL, orphanRemoval = true)
    @OrderBy("displayOrder ASC")
    private List<ProductImage> images = new ArrayList<>();

    @OneToMany(mappedBy = "product", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<ProductVariant> variants = new ArrayList<>();

    @PrePersist
    protected void ensureProductId() {
        if (this.id == null) {
            this.id = UUID.randomUUID();
        }
    }

    public String resolvePrimaryImageUrl() {
        return images.stream()
                .filter(ProductImage::isPrimary)
                .map(ProductImage::getImageUrl)
                .findFirst()
                .orElseGet(() -> images.isEmpty() ? null : images.get(0).getImageUrl());
    }

    public UUID getId() { return id; }
    public void setId(UUID id) { this.id = id; }
    public UUID getStoreId() { return storeId; }
    public void setStoreId(UUID storeId) { this.storeId = storeId; }
    public UUID getCategoryId() { return categoryId; }
    public void setCategoryId(UUID categoryId) { this.categoryId = categoryId; }
    public UUID getEtalaseId() { return etalaseId; }
    public void setEtalaseId(UUID etalaseId) { this.etalaseId = etalaseId; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public String getSlug() { return slug; }
    public void setSlug(String slug) { this.slug = slug; }
    public String getCondition() { return condition; }
    public void setCondition(String condition) { this.condition = condition; }
    public int getWeightGram() { return weightGram; }
    public void setWeightGram(int weightGram) { this.weightGram = weightGram; }
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    public String getImportantInfo() { return importantInfo; }
    public void setImportantInfo(String importantInfo) { this.importantInfo = importantInfo; }
    public boolean isPreorder() { return preorder; }
    public void setPreorder(boolean preorder) { this.preorder = preorder; }
    public Integer getPreorderDays() { return preorderDays; }
    public void setPreorderDays(Integer preorderDays) { this.preorderDays = preorderDays; }
    public String getCampaignLabel() { return campaignLabel; }
    public void setCampaignLabel(String campaignLabel) { this.campaignLabel = campaignLabel; }
    public BigDecimal getMinPrice() { return minPrice; }
    public void setMinPrice(BigDecimal minPrice) { this.minPrice = minPrice; }
    public BigDecimal getMaxPrice() { return maxPrice; }
    public void setMaxPrice(BigDecimal maxPrice) { this.maxPrice = maxPrice; }
    public long getViewCount() { return viewCount; }
    public void setViewCount(long viewCount) { this.viewCount = viewCount; }
    public int getSoldCount() { return soldCount; }
    public void setSoldCount(int soldCount) { this.soldCount = soldCount; }
    public BigDecimal getRatingAvg() { return ratingAvg; }
    public void setRatingAvg(BigDecimal ratingAvg) { this.ratingAvg = ratingAvg; }
    public int getReviewCount() { return reviewCount; }
    public void setReviewCount(int reviewCount) { this.reviewCount = reviewCount; }
    public boolean isActive() { return active; }
    public void setActive(boolean active) { this.active = active; }
    public List<ProductImage> getImages() { return images; }
    public List<ProductVariant> getVariants() { return variants; }

    @Entity(name = "ProductImage")
    @Table(name = "product_images")
    public static class ProductImage {
        @Id
        @Column(name = "id", nullable = false, updatable = false)
        private UUID id;

        @ManyToOne(fetch = FetchType.LAZY, optional = false)
        @JoinColumn(name = "product_id", nullable = false)
        private Product product;

        @Column(name = "image_url", nullable = false, length = 500)
        private String imageUrl;

        @Column(name = "is_primary", nullable = false)
        private boolean primary;

        @Column(name = "display_order", nullable = false)
        private int displayOrder;

        @PrePersist
        protected void initId() {
            if (this.id == null) this.id = UUID.randomUUID();
        }

        public UUID getId() { return id; }
        public void setId(UUID id) { this.id = id; }
        public Product getProduct() { return product; }
        public void setProduct(Product product) { this.product = product; }
        public String getImageUrl() { return imageUrl; }
        public void setImageUrl(String imageUrl) { this.imageUrl = imageUrl; }
        public boolean isPrimary() { return primary; }
        public void setPrimary(boolean primary) { this.primary = primary; }
        public int getDisplayOrder() { return displayOrder; }
        public void setDisplayOrder(int displayOrder) { this.displayOrder = displayOrder; }
    }

    @Entity(name = "Category")
    @Table(name = "categories")
    public static class Category extends BaseAuditEntity {
        @Id
        @Column(name = "id", nullable = false, updatable = false)
        private UUID id;

        @Column(name = "parent_id")
        private UUID parentId;

        @Column(name = "name", nullable = false, length = 120)
        private String name;

        @Column(name = "slug", nullable = false, unique = true, length = 150)
        private String slug;

        @Column(name = "level", nullable = false)
        private int level = 1;

        @PrePersist
        protected void initId() {
            if (this.id == null) this.id = UUID.randomUUID();
        }

        public UUID getId() { return id; }
        public void setId(UUID id) { this.id = id; }
        public UUID getParentId() { return parentId; }
        public void setParentId(UUID parentId) { this.parentId = parentId; }
        public String getName() { return name; }
        public void setName(String name) { this.name = name; }
        public String getSlug() { return slug; }
        public void setSlug(String slug) { this.slug = slug; }
        public int getLevel() { return level; }
        public void setLevel(int level) { this.level = level; }
    }

    @Entity(name = "WishlistItem")
    @Table(name = "wishlist_items")
    public static class WishlistItem {
        @Id
        @Column(name = "id", nullable = false, updatable = false)
        private UUID id;

        @Column(name = "customer_id", nullable = false)
        private UUID customerId;

        @Column(name = "product_id", nullable = false)
        private UUID productId;

        @Column(name = "created_at", nullable = false, updatable = false)
        private OffsetDateTime createdAt;

        @PrePersist
        protected void initWishlist() {
            if (this.id == null) this.id = UUID.randomUUID();
            if (this.createdAt == null) this.createdAt = OffsetDateTime.now();
        }

        public UUID getId() { return id; }
        public UUID getCustomerId() { return customerId; }
        public void setCustomerId(UUID customerId) { this.customerId = customerId; }
        public UUID getProductId() { return productId; }
        public void setProductId(UUID productId) { this.productId = productId; }
        public OffsetDateTime getCreatedAt() { return createdAt; }
    }
}
