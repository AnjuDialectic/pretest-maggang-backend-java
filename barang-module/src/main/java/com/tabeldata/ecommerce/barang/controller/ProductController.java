package com.tabeldata.ecommerce.barang.controller;

import com.tabeldata.ecommerce.barang.dto.ProductDtos.*;
import com.tabeldata.ecommerce.barang.service.ProductService;
import com.tabeldata.ecommerce.common.api.ApiResponse;
import com.tabeldata.ecommerce.common.api.PageResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1")
@Tag(name = "Barang (Catalog, SKU Variant & Wishlist)")
public class ProductController {

    private final ProductService barangSvc;

    public ProductController(ProductService barangSvc) {
        this.barangSvc = barangSvc;
    }

    @GetMapping("/products")
    public ApiResponse<PageResponse<CatalogCardResponse>> browseKatalog(
            @RequestParam(required = false) UUID storeId,
            @RequestParam(required = false) UUID categoryId,
            @RequestParam(required = false) UUID etalaseId,
            @RequestParam(defaultValue = "ALL") String systemFilter,
            @RequestParam(required = false) String q,
            @RequestParam(required = false) BigDecimal minPrice,
            @RequestParam(required = false) BigDecimal maxPrice,
            @RequestParam(defaultValue = "NEWEST") String sort,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "12") int size
    ) {
        return ApiResponse.ok(barangSvc.browseCatalog(
                storeId, categoryId, etalaseId, systemFilter, q, minPrice, maxPrice, sort, page, size
        ));
    }

    @GetMapping("/products/{productId}")
    public ApiResponse<ProductDetailResponse> fetchDetailBarang(
            @PathVariable UUID productId,
            @RequestHeader(value = "X-Customer-Id", required = false) UUID viewerId
    ) {
        return ApiResponse.ok(barangSvc.viewProductDetail(productId, viewerId));
    }

    @PostMapping("/products")
    @ResponseStatus(HttpStatus.CREATED)
    public ApiResponse<ProductDetailResponse> registerBarang(@Valid @RequestBody CreateProductRequest payload) {
        return ApiResponse.ok("Produk berhasil ditambahkan", barangSvc.registerProduct(payload));
    }

    @PutMapping("/products/{productId}")
    public ApiResponse<ProductDetailResponse> modifyBarang(
            @PathVariable UUID productId,
            @Valid @RequestBody UpdateProductRequest payload
    ) {
        return ApiResponse.ok("Produk diperbarui", barangSvc.modifyProduct(productId, payload));
    }

    @PatchMapping("/products/{productId}/variants/{variantId}/stock")
    public ApiResponse<VariantDto> patchStokDanHargaVarian(
            @PathVariable UUID productId,
            @PathVariable UUID variantId,
            @Valid @RequestBody VariantStockUpdateRequest payload
    ) {
        return ApiResponse.ok("Stok/harga varian diperbarui",
                barangSvc.patchVariantStockAndPrice(productId, variantId, payload));
    }

    @GetMapping("/categories")
    public ApiResponse<List<CategoryDto>> fetchSemuaKategori() {
        return ApiResponse.ok(barangSvc.listCategories());
    }

    @GetMapping("/wishlists")
    public ApiResponse<List<WishlistEntryDto>> fetchWishlistPelanggan(
            @RequestHeader("X-Customer-Id") UUID customerId
    ) {
        return ApiResponse.ok(barangSvc.listCustomerWishlist(customerId));
    }

    @PostMapping("/wishlists/{productId}")
    public ApiResponse<Void> simpanKeWishlist(
            @PathVariable UUID productId,
            @RequestHeader("X-Customer-Id") UUID customerId
    ) {
        barangSvc.addToWishlist(customerId, productId);
        return ApiResponse.okMessage("Produk disimpan ke Wishlist");
    }

    @DeleteMapping("/wishlists/{productId}")
    public ApiResponse<Void> hapusDariWishlist(
            @PathVariable UUID productId,
            @RequestHeader("X-Customer-Id") UUID customerId
    ) {
        barangSvc.removeFromWishlist(customerId, productId);
        return ApiResponse.okMessage("Produk dihapus dari Wishlist");
    }
}
