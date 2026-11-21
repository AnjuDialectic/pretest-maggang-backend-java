package com.tabeldata.ecommerce.toko.dto;

import com.tabeldata.ecommerce.toko.entity.Store;
import com.tabeldata.ecommerce.toko.entity.StoreBadge;
import com.tabeldata.ecommerce.toko.entity.StoreEtalase;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;

public final class StoreResponses {

    private StoreResponses() {}

    public record StoreResponse(
            UUID id,
            String name,
            String slug,
            String logoUrl,
            StoreBadge badge,
            String city,
            boolean online,
            BigDecimal ratingAvg,
            int reviewCount,
            int avgProcessTimeMinutes,
            String avgProcessTimeDisplay,
            String operatingHours,
            String description,
            long followerCount,
            long customEtalaseCount,
            Boolean following,
            OffsetDateTime createdAt
    ) {
        public static StoreResponse from(Store s, long followers, long etalases, Boolean following) {
            String display = s.getAvgProcessTimeMinutes() < 60
                    ? "± " + s.getAvgProcessTimeMinutes() + " menit"
                    : "± " + Math.round(s.getAvgProcessTimeMinutes() / 60.0f) + " jam";
            return new StoreResponse(
                    s.getId(), s.getName(), s.getSlug(), s.getLogoUrl(), s.getBadge(),
                    s.getCity(), s.isOnline(), s.getRatingAvg(), s.getReviewCount(),
                    s.getAvgProcessTimeMinutes(), display, s.getOperatingHours(),
                    s.getDescription(), followers, etalases, following, s.getCreatedAt()
            );
        }
    }

    public record StoreEtalaseResponse(UUID id, UUID storeId, String name, String slug, int displayOrder) {
        public static StoreEtalaseResponse from(StoreEtalase e) {
            return new StoreEtalaseResponse(e.getId(), e.getStore().getId(), e.getName(), e.getSlug(), e.getDisplayOrder());
        }
    }

    public record SystemEtalaseItem(String code, String label) {}

    public record StoreEtalaseSidebarResponse(
            UUID storeId,
            int totalEtalaseCount,
            List<SystemEtalaseItem> systemEtalases,
            List<StoreEtalaseResponse> customEtalases
    ) {}
}
