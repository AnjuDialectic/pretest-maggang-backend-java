package com.tabeldata.ecommerce.toko.entity;

import com.tabeldata.ecommerce.common.entity.BaseAuditEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;

import java.math.BigDecimal;
import java.util.UUID;

@Entity
@Table(name = "stores")
public class Store extends BaseAuditEntity {

    @Id
    @Column(name = "id", nullable = false, updatable = false)
    private UUID id;

    @Column(name = "name", nullable = false, length = 120)
    private String name;

    @Column(name = "slug", nullable = false, unique = true, length = 150)
    private String slug;

    @Column(name = "logo_url", length = 500)
    private String logoUrl;

    @Enumerated(EnumType.STRING)
    @Column(name = "badge", nullable = false, length = 30)
    private StoreBadge badge = StoreBadge.REGULAR;

    @Column(name = "city", nullable = false, length = 100)
    private String city;

    @Column(name = "is_online", nullable = false)
    private boolean online = true;

    @Column(name = "rating_avg", nullable = false, precision = 3, scale = 2)
    private BigDecimal ratingAvg = BigDecimal.ZERO;

    @Column(name = "review_count", nullable = false)
    private int reviewCount = 0;

    @Column(name = "avg_process_time_minutes", nullable = false)
    private int avgProcessTimeMinutes = 180;

    @Column(name = "operating_hours", nullable = false, length = 100)
    private String operatingHours = "09:00 - 21:00 WIB";

    @Column(name = "description", columnDefinition = "TEXT")
    private String description;

    @Column(name = "is_active", nullable = false)
    private boolean active = true;

    @PrePersist
    protected void assignId() {
        if (this.id == null) {
            this.id = UUID.randomUUID();
        }
    }

    public UUID getId() { return id; }
    public void setId(UUID id) { this.id = id; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public String getSlug() { return slug; }
    public void setSlug(String slug) { this.slug = slug; }
    public String getLogoUrl() { return logoUrl; }
    public void setLogoUrl(String logoUrl) { this.logoUrl = logoUrl; }
    public StoreBadge getBadge() { return badge; }
    public void setBadge(StoreBadge badge) { this.badge = badge; }
    public String getCity() { return city; }
    public void setCity(String city) { this.city = city; }
    public boolean isOnline() { return online; }
    public void setOnline(boolean online) { this.online = online; }
    public BigDecimal getRatingAvg() { return ratingAvg; }
    public void setRatingAvg(BigDecimal ratingAvg) { this.ratingAvg = ratingAvg; }
    public int getReviewCount() { return reviewCount; }
    public void setReviewCount(int reviewCount) { this.reviewCount = reviewCount; }
    public int getAvgProcessTimeMinutes() { return avgProcessTimeMinutes; }
    public void setAvgProcessTimeMinutes(int avgProcessTimeMinutes) { this.avgProcessTimeMinutes = avgProcessTimeMinutes; }
    public String getOperatingHours() { return operatingHours; }
    public void setOperatingHours(String operatingHours) { this.operatingHours = operatingHours; }
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    public boolean isActive() { return active; }
    public void setActive(boolean active) { this.active = active; }
}
