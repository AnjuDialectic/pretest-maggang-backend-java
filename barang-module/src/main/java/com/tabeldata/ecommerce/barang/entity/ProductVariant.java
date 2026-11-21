package com.tabeldata.ecommerce.barang.entity;

import com.tabeldata.ecommerce.common.entity.BaseAuditEntity;
import jakarta.persistence.*;

import java.math.BigDecimal;
import java.util.UUID;

@Entity
@Table(name = "product_variants")
public class ProductVariant extends BaseAuditEntity {

    @Id
    @Column(name = "id", nullable = false, updatable = false)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "product_id", nullable = false)
    private Product product;

    @Column(name = "sku", nullable = false, unique = true, length = 100)
    private String sku;

    @Column(name = "variant_group1_name", length = 60)
    private String variantGroup1Name;

    @Column(name = "variant_group1_value", length = 80)
    private String variantGroup1Value;

    @Column(name = "variant_group2_name", length = 60)
    private String variantGroup2Name;

    @Column(name = "variant_group2_value", length = 80)
    private String variantGroup2Value;

    @Column(name = "variant_label", length = 180)
    private String variantLabel;

    @Column(name = "price", nullable = false, precision = 19, scale = 2)
    private BigDecimal price = BigDecimal.ZERO;

    @Column(name = "stock", nullable = false)
    private int stock = 0;

    @Column(name = "weight_gram")
    private Integer weightGram;

    @Column(name = "image_url", length = 500)
    private String imageUrl;

    @Column(name = "is_default", nullable = false)
    private boolean defaultVariant = false;

    @Column(name = "is_active", nullable = false)
    private boolean active = true;

    @PrePersist
    protected void ensureVariantId() {
        if (this.id == null) {
            this.id = UUID.randomUUID();
        }
    }

    public int effectiveWeightGram() {
        return weightGram != null && weightGram > 0 ? weightGram : product.getWeightGram();
    }

    public String effectiveImageUrl() {
        return imageUrl != null && !imageUrl.isBlank() ? imageUrl : product.resolvePrimaryImageUrl();
    }

    public UUID getId() { return id; }
    public void setId(UUID id) { this.id = id; }
    public Product getProduct() { return product; }
    public void setProduct(Product product) { this.product = product; }
    public String getSku() { return sku; }
    public void setSku(String sku) { this.sku = sku; }
    public String getVariantGroup1Name() { return variantGroup1Name; }
    public void setVariantGroup1Name(String variantGroup1Name) { this.variantGroup1Name = variantGroup1Name; }
    public String getVariantGroup1Value() { return variantGroup1Value; }
    public void setVariantGroup1Value(String variantGroup1Value) { this.variantGroup1Value = variantGroup1Value; }
    public String getVariantGroup2Name() { return variantGroup2Name; }
    public void setVariantGroup2Name(String variantGroup2Name) { this.variantGroup2Name = variantGroup2Name; }
    public String getVariantGroup2Value() { return variantGroup2Value; }
    public void setVariantGroup2Value(String variantGroup2Value) { this.variantGroup2Value = variantGroup2Value; }
    public String getVariantLabel() { return variantLabel; }
    public void setVariantLabel(String variantLabel) { this.variantLabel = variantLabel; }
    public BigDecimal getPrice() { return price; }
    public void setPrice(BigDecimal price) { this.price = price; }
    public int getStock() { return stock; }
    public void setStock(int stock) { this.stock = stock; }
    public Integer getWeightGram() { return weightGram; }
    public void setWeightGram(Integer weightGram) { this.weightGram = weightGram; }
    public String getImageUrl() { return imageUrl; }
    public void setImageUrl(String imageUrl) { this.imageUrl = imageUrl; }
    public boolean isDefaultVariant() { return defaultVariant; }
    public void setDefaultVariant(boolean defaultVariant) { this.defaultVariant = defaultVariant; }
    public boolean isActive() { return active; }
    public void setActive(boolean active) { this.active = active; }
}
