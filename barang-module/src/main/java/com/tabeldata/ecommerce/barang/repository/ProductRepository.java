package com.tabeldata.ecommerce.barang.repository;

import com.tabeldata.ecommerce.barang.entity.Product;
import com.tabeldata.ecommerce.barang.entity.ProductVariant;
import jakarta.persistence.LockModeType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.math.BigDecimal;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface ProductRepository extends JpaRepository<Product, UUID> {

    boolean existsByStoreIdAndSlug(UUID storeId, String slug);

    @Query("""
            SELECT p FROM Product p
            WHERE p.active = true
              AND (:storeId IS NULL OR p.storeId = :storeId)
              AND (:categoryId IS NULL OR p.categoryId = :categoryId)
              AND (:etalaseId IS NULL OR p.etalaseId = :etalaseId)
              AND (:preorderOnly = false OR p.preorder = true)
              AND (:soldOnly = false OR p.soldCount > 0)
              AND (:keyword IS NULL OR LOWER(p.name) LIKE LOWER(CONCAT('%', :keyword, '%')))
              AND (:minPrice IS NULL OR p.minPrice >= :minPrice)
              AND (:maxPrice IS NULL OR p.minPrice <= :maxPrice)
            """)
    Page<Product> searchCatalog(
            @Param("storeId") UUID storeId,
            @Param("categoryId") UUID categoryId,
            @Param("etalaseId") UUID etalaseId,
            @Param("preorderOnly") boolean preorderOnly,
            @Param("soldOnly") boolean soldOnly,
            @Param("keyword") String keyword,
            @Param("minPrice") BigDecimal minPrice,
            @Param("maxPrice") BigDecimal maxPrice,
            Pageable pageable
    );

    @Modifying
    @Query("UPDATE Product p SET p.viewCount = p.viewCount + 1 WHERE p.id = :productId")
    void incrementViewCount(@Param("productId") UUID productId);

    @Query("SELECT v FROM ProductVariant v JOIN FETCH v.product WHERE v.id = :variantId")
    Optional<ProductVariant> findVariantById(@Param("variantId") UUID variantId);

    @Query("SELECT v FROM ProductVariant v JOIN FETCH v.product WHERE v.id IN :ids")
    List<ProductVariant> findVariantsByIds(@Param("ids") Collection<UUID> ids);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT v FROM ProductVariant v JOIN FETCH v.product WHERE v.id IN :ids ORDER BY v.id ASC")
    List<ProductVariant> lockVariantsByIds(@Param("ids") Collection<UUID> ids);

    @Query("SELECT COUNT(v) > 0 FROM ProductVariant v WHERE LOWER(v.sku) = LOWER(:sku)")
    boolean existsVariantBySku(@Param("sku") String sku);
}
