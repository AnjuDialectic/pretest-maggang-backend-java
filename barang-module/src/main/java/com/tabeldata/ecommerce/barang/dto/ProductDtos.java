package com.tabeldata.ecommerce.barang.dto;

import com.tabeldata.ecommerce.common.port.StorePort.StoreSummary;
import jakarta.validation.Valid;
import jakarta.validation.constraints.*;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;

public final class ProductDtos {

    private ProductDtos() {}

    public record ImageInput(
            @NotBlank @Size(max = 500) String imageUrl,
            boolean primary,
            int displayOrder
    ) {}

    public record VariantInput(
            @NotBlank @Size(max = 100) String sku,
            @Size(max = 60) String variantGroup1Name,
            @Size(max = 80) String variantGroup1Value,
            @Size(max = 60) String variantGroup2Name,
            @Size(max = 80) String variantGroup2Value,
            @Size(max = 180) String variantLabel,
            @NotNull @DecimalMin("0.0") BigDecimal price,
            @Min(0) int stock,
            @Min(1) Integer weightGram,
            @Size(max = 500) String imageUrl,
            boolean defaultVariant,
            Boolean active
    ) {}

    public record CreateProductRequest(
            @NotNull UUID storeId,
            UUID categoryId,
            UUID etalaseId,
            @NotBlank @Size(max = 200) String name,
            @Pattern(regexp = "NEW|USED") String condition,
            @Min(1) int weightGram,
            String description,
            String importantInfo,
            boolean preorder,
            @Min(1) Integer preorderDays,
            @Size(max = 80) String campaignLabel,
            @Valid List<ImageInput> images,
            @NotEmpty @Valid List<VariantInput> variants
    ) {}

    public record UpdateProductRequest(
            UUID categoryId,
            UUID etalaseId,
            @NotBlank @Size(max = 200) String name,
            @Pattern(regexp = "NEW|USED") String condition,
            @Min(1) int weightGram,
            String description,
            String importantInfo,
            boolean preorder,
            @Min(1) Integer preorderDays,
            @Size(max = 80) String campaignLabel,
            Boolean active
    ) {}

    public record VariantStockUpdateRequest(
            @DecimalMin("0.0") BigDecimal price,
            @Min(0) Integer stock,
            Boolean active
    ) {}

    public record CatalogCardResponse(
            UUID id,
            String name,
            String slug,
            String primaryImageUrl,
            BigDecimal minPrice,
            BigDecimal maxPrice,
            BigDecimal ratingAvg,
            int reviewCount,
            int soldCount,
            boolean preorder,
            String campaignLabel,
            UUID storeId,
            String storeName,
            String storeBadge,
            String storeCity
    ) {}

    public record BreadcrumbItem(UUID id, String name, String slug, int level) {}

    public record ImageDto(UUID id, String imageUrl, boolean primary, int displayOrder) {}

    public record VariantGroupDto(String groupName, List<String> options) {}

    public record VariantDto(
            UUID id,
            String sku,
            String variantGroup1Name,
            String variantGroup1Value,
            String variantGroup2Name,
            String variantGroup2Value,
            String variantLabel,
            BigDecimal price,
            int stock,
            int weightGram,
            String imageUrl,
            boolean defaultVariant,
            boolean active
    ) {}

    public record ProductDetailResponse(
            UUID id,
            String name,
            String slug,
            String condition,
            int weightGram,
            String weightDisplay,
            String description,
            String importantInfo,
            boolean preorder,
            Integer preorderDays,
            String campaignLabel,
            BigDecimal minPrice,
            BigDecimal maxPrice,
            long viewCount,
            int soldCount,
            BigDecimal ratingAvg,
            int reviewCount,
            int totalStock,
            UUID etalaseId,
            String etalaseName,
            List<BreadcrumbItem> categoryBreadcrumbs,
            StoreSummary store,
            List<ImageDto> images,
            List<VariantGroupDto> variantGroups,
            List<VariantDto> variants,
            Boolean wishlisted,
            OffsetDateTime createdAt
    ) {}

    public record CategoryDto(UUID id, UUID parentId, String name, String slug, int level) {}

    public record WishlistEntryDto(
            UUID wishlistId,
            UUID productId,
            String productName,
            String primaryImageUrl,
            BigDecimal minPrice,
            OffsetDateTime addedAt
    ) {}
}
