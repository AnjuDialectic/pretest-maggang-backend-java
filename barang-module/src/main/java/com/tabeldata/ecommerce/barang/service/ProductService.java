package com.tabeldata.ecommerce.barang.service;

import com.tabeldata.ecommerce.barang.dto.ProductDtos.*;
import com.tabeldata.ecommerce.barang.entity.Product;
import com.tabeldata.ecommerce.barang.entity.Product.Category;
import com.tabeldata.ecommerce.barang.entity.Product.ProductImage;
import com.tabeldata.ecommerce.barang.entity.Product.WishlistItem;
import com.tabeldata.ecommerce.barang.entity.ProductVariant;
import com.tabeldata.ecommerce.barang.repository.ProductRepository;
import com.tabeldata.ecommerce.common.api.PageResponse;
import com.tabeldata.ecommerce.common.exception.BusinessRuleException;
import com.tabeldata.ecommerce.common.exception.ConflictException;
import com.tabeldata.ecommerce.common.exception.InsufficientStockException;
import com.tabeldata.ecommerce.common.exception.ResourceNotFoundException;
import com.tabeldata.ecommerce.common.port.ProductPort;
import com.tabeldata.ecommerce.common.port.StorePort;
import com.tabeldata.ecommerce.common.port.StorePort.StoreSummary;
import com.tabeldata.ecommerce.common.util.SlugUtils;
import jakarta.persistence.EntityManager;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.*;

@Service
public class ProductService implements ProductPort {

    private final ProductRepository productRepo;
    private final StorePort storePort;
    private final EntityManager em;

    public ProductService(ProductRepository productRepo, StorePort storePort, EntityManager em) {
        this.productRepo = productRepo;
        this.storePort = storePort;
        this.em = em;
    }

    @Transactional(readOnly = true)
    public PageResponse<CatalogCardResponse> browseCatalog(
            UUID storeId,
            UUID categoryId,
            UUID etalaseId,
            String systemFilter,
            String keyword,
            BigDecimal minPrice,
            BigDecimal maxPrice,
            String sortBy,
            int page,
            int size
    ) {
        boolean preorderOnly = "PREORDER".equalsIgnoreCase(systemFilter);
        boolean soldOnly = "SOLD".equalsIgnoreCase(systemFilter);
        String cleanKeyword = (keyword == null || keyword.isBlank()) ? null : keyword.trim();

        Page<Product> resultPage = productRepo.searchCatalog(
                storeId,
                categoryId,
                etalaseId,
                preorderOnly,
                soldOnly,
                cleanKeyword,
                minPrice,
                maxPrice,
                PageRequest.of(page, size, resolveCatalogSort(sortBy))
        );

        Set<UUID> storeIds = new LinkedHashSet<>();
        resultPage.getContent().forEach(p -> storeIds.add(p.getStoreId()));
        Map<UUID, StoreSummary> storeMap = storePort.getStoreSummaries(storeIds);

        return PageResponse.from(resultPage.map(p -> {
            StoreSummary s = storeMap.get(p.getStoreId());
            return new CatalogCardResponse(
                    p.getId(),
                    p.getName(),
                    p.getSlug(),
                    p.resolvePrimaryImageUrl(),
                    p.getMinPrice(),
                    p.getMaxPrice(),
                    p.getRatingAvg(),
                    p.getReviewCount(),
                    p.getSoldCount(),
                    p.isPreorder(),
                    p.getCampaignLabel(),
                    p.getStoreId(),
                    s != null ? s.name() : null,
                    s != null ? s.badge() : null,
                    s != null ? s.city() : null
            );
        }));
    }

    @Transactional
    public ProductDetailResponse viewProductDetail(UUID productId, UUID viewerCustomerId) {
        Product p = loadActiveProduct(productId);
        productRepo.incrementViewCount(productId);
        p.setViewCount(p.getViewCount() + 1);
        return assembleDetail(p, viewerCustomerId);
    }

    @Transactional
    public ProductDetailResponse registerProduct(CreateProductRequest req) {
        StoreSummary store = storePort.getStoreSummary(req.storeId());
        if (!store.isActive()) {
            throw new BusinessRuleException("Toko tidak aktif");
        }
        if (req.etalaseId() != null && !storePort.existsEtalaseInStore(req.storeId(), req.etalaseId())) {
            throw new BusinessRuleException("Etalase tidak ditemukan pada toko ini");
        }
        if (req.categoryId() != null && em.find(Category.class, req.categoryId()) == null) {
            throw new ResourceNotFoundException("Kategori tidak ditemukan: " + req.categoryId());
        }

        Product p = new Product();
        p.setStoreId(req.storeId());
        p.setCategoryId(req.categoryId());
        p.setEtalaseId(req.etalaseId());
        p.setName(req.name().trim());
        p.setSlug(allocateProductSlug(req.storeId(), req.name()));
        p.setCondition(req.condition() == null ? "NEW" : req.condition());
        p.setWeightGram(req.weightGram());
        p.setDescription(req.description());
        p.setImportantInfo(req.importantInfo());
        p.setPreorder(req.preorder());
        p.setPreorderDays(req.preorder() ? req.preorderDays() : null);
        p.setCampaignLabel(req.campaignLabel());
        p.setActive(true);

        if (req.images() != null) {
            for (ImageInput imgIn : req.images()) {
                ProductImage img = new ProductImage();
                img.setProduct(p);
                img.setImageUrl(imgIn.imageUrl().trim());
                img.setPrimary(imgIn.primary());
                img.setDisplayOrder(imgIn.displayOrder());
                p.getImages().add(img);
            }
        }

        for (VariantInput vIn : req.variants()) {
            if (productRepo.existsVariantBySku(vIn.sku().trim())) {
                throw new ConflictException("SKU sudah terdaftar: " + vIn.sku());
            }
            ProductVariant v = new ProductVariant();
            v.setProduct(p);
            v.setSku(vIn.sku().trim());
            v.setVariantGroup1Name(vIn.variantGroup1Name());
            v.setVariantGroup1Value(vIn.variantGroup1Value());
            v.setVariantGroup2Name(vIn.variantGroup2Name());
            v.setVariantGroup2Value(vIn.variantGroup2Value());
            v.setVariantLabel(resolveVariantLabel(vIn));
            v.setPrice(vIn.price());
            v.setStock(vIn.stock());
            v.setWeightGram(vIn.weightGram() != null ? vIn.weightGram() : req.weightGram());
            v.setImageUrl(vIn.imageUrl());
            v.setDefaultVariant(vIn.defaultVariant());
            v.setActive(vIn.active() == null || vIn.active());
            p.getVariants().add(v);
        }

        recalculatePriceBounds(p);
        return assembleDetail(productRepo.save(p), null);
    }

    @Transactional
    public ProductDetailResponse modifyProduct(UUID productId, UpdateProductRequest req) {
        Product p = loadActiveProduct(productId);
        if (req.etalaseId() != null && !storePort.existsEtalaseInStore(p.getStoreId(), req.etalaseId())) {
            throw new BusinessRuleException("Etalase tidak ditemukan pada toko ini");
        }
        p.setCategoryId(req.categoryId());
        p.setEtalaseId(req.etalaseId());
        p.setName(req.name().trim());
        p.setCondition(req.condition() == null ? p.getCondition() : req.condition());
        p.setWeightGram(req.weightGram());
        p.setDescription(req.description());
        p.setImportantInfo(req.importantInfo());
        p.setPreorder(req.preorder());
        p.setPreorderDays(req.preorder() ? req.preorderDays() : null);
        p.setCampaignLabel(req.campaignLabel());
        if (req.active() != null) {
            p.setActive(req.active());
        }
        return assembleDetail(productRepo.save(p), null);
    }

    @Transactional
    public VariantDto patchVariantStockAndPrice(UUID productId, UUID variantId, VariantStockUpdateRequest req) {
        Product p = loadActiveProduct(productId);
        ProductVariant target = p.getVariants().stream()
                .filter(v -> v.getId().equals(variantId))
                .findFirst()
                .orElseThrow(() -> new ResourceNotFoundException("Varian tidak ditemukan pada produk ini"));

        if (req.price() != null) {
            target.setPrice(req.price());
        }
        if (req.stock() != null) {
            target.setStock(req.stock());
        }
        if (req.active() != null) {
            target.setActive(req.active());
        }
        recalculatePriceBounds(p);
        productRepo.save(p);
        return toVariantDto(target);
    }

    @Transactional(readOnly = true)
    public List<CategoryDto> listCategories() {
        return em.createQuery("SELECT c FROM Category c ORDER BY c.level ASC, c.name ASC", Category.class)
                .getResultList()
                .stream()
                .map(c -> new CategoryDto(c.getId(), c.getParentId(), c.getName(), c.getSlug(), c.getLevel()))
                .toList();
    }

    @Transactional(readOnly = true)
    public List<WishlistEntryDto> listCustomerWishlist(UUID customerId) {
        List<WishlistItem> items = em.createQuery(
                        "SELECT w FROM WishlistItem w WHERE w.customerId = :cid ORDER BY w.createdAt DESC",
                        WishlistItem.class)
                .setParameter("cid", customerId)
                .getResultList();
        if (items.isEmpty()) {
            return List.of();
        }
        List<UUID> pIds = items.stream().map(WishlistItem::getProductId).toList();
        Map<UUID, Product> productMap = new HashMap<>();
        productRepo.findAllById(pIds).forEach(p -> productMap.put(p.getId(), p));

        return items.stream()
                .filter(w -> productMap.containsKey(w.getProductId()))
                .map(w -> {
                    Product p = productMap.get(w.getProductId());
                    return new WishlistEntryDto(
                            w.getId(),
                            p.getId(),
                            p.getName(),
                            p.resolvePrimaryImageUrl(),
                            p.getMinPrice(),
                            w.getCreatedAt()
                    );
                })
                .toList();
    }

    @Override
    @Transactional
    public void addToWishlist(UUID customerId, UUID productId) {
        loadActiveProduct(productId);
        Long count = em.createQuery(
                        "SELECT COUNT(w) FROM WishlistItem w WHERE w.customerId = :cid AND w.productId = :pid",
                        Long.class)
                .setParameter("cid", customerId)
                .setParameter("pid", productId)
                .getSingleResult();
        if (count == 0) {
            WishlistItem w = new WishlistItem();
            w.setCustomerId(customerId);
            w.setProductId(productId);
            em.persist(w);
        }
    }

    @Transactional
    public void removeFromWishlist(UUID customerId, UUID productId) {
        em.createQuery("DELETE FROM WishlistItem w WHERE w.customerId = :cid AND w.productId = :pid")
                .setParameter("cid", customerId)
                .setParameter("pid", productId)
                .executeUpdate();
    }

    @Override
    @Transactional(readOnly = true)
    public VariantSnapshot getVariantSnapshot(UUID variantId) {
        ProductVariant v = productRepo.findVariantById(variantId)
                .orElseThrow(() -> new ResourceNotFoundException("Varian produk tidak ditemukan: " + variantId));
        return toSnapshot(v);
    }

    @Override
    @Transactional(readOnly = true)
    public Map<UUID, VariantSnapshot> getVariantSnapshots(Collection<UUID> variantIds) {
        Map<UUID, VariantSnapshot> map = new LinkedHashMap<>();
        if (variantIds == null || variantIds.isEmpty()) {
            return map;
        }
        for (ProductVariant v : productRepo.findVariantsByIds(variantIds)) {
            map.put(v.getId(), toSnapshot(v));
        }
        return map;
    }

    @Override
    @Transactional
    public void reserveStock(List<StockAdjustment> adjustments) {
        Map<UUID, Integer> requestedByVariant = aggregateAdjustments(adjustments);
        if (requestedByVariant.isEmpty()) {
            return;
        }
        List<ProductVariant> locked = productRepo.lockVariantsByIds(requestedByVariant.keySet());
        if (locked.size() != requestedByVariant.size()) {
            throw new ResourceNotFoundException("Sebagian varian produk tidak ditemukan saat reservasi stok");
        }
        for (ProductVariant v : locked) {
            int qty = requestedByVariant.get(v.getId());
            Product p = v.getProduct();
            String displayName = v.getVariantLabel() == null || v.getVariantLabel().isBlank()
                    ? p.getName()
                    : p.getName() + " - " + v.getVariantLabel();
            if (!p.isActive() || !v.isActive()) {
                throw new BusinessRuleException("Varian produk sedang tidak aktif: " + displayName);
            }
            if (v.getStock() < qty) {
                throw new InsufficientStockException(v.getId(), displayName, qty, v.getStock());
            }
            v.setStock(v.getStock() - qty);
            p.setSoldCount(p.getSoldCount() + qty);
        }
    }

    @Override
    @Transactional
    public void releaseStock(List<StockAdjustment> adjustments) {
        Map<UUID, Integer> releaseByVariant = aggregateAdjustments(adjustments);
        if (releaseByVariant.isEmpty()) {
            return;
        }
        for (ProductVariant v : productRepo.lockVariantsByIds(releaseByVariant.keySet())) {
            int qty = releaseByVariant.get(v.getId());
            v.setStock(v.getStock() + qty);
            Product p = v.getProduct();
            p.setSoldCount(Math.max(0, p.getSoldCount() - qty));
        }
    }

    private Map<UUID, Integer> aggregateAdjustments(List<StockAdjustment> adjustments) {
        Map<UUID, Integer> sorted = new TreeMap<>();
        if (adjustments != null) {
            for (StockAdjustment adj : adjustments) {
                if (adj.quantity() <= 0) {
                    throw new BusinessRuleException("Kuantitas perubahan stok harus lebih dari 0");
                }
                sorted.merge(adj.variantId(), adj.quantity(), Integer::sum);
            }
        }
        return sorted;
    }

    private Product loadActiveProduct(UUID productId) {
        Product p = productRepo.findById(productId)
                .orElseThrow(() -> new ResourceNotFoundException("Produk tidak ditemukan: " + productId));
        if (!p.isActive()) {
            throw new ResourceNotFoundException("Produk tidak aktif: " + productId);
        }
        return p;
    }

    private ProductDetailResponse assembleDetail(Product p, UUID viewerCustomerId) {
        StoreSummary store = storePort.getStoreSummary(p.getStoreId());
        String etalaseName = p.getEtalaseId() == null
                ? null
                : storePort.findEtalaseName(p.getEtalaseId()).orElse(null);

        List<BreadcrumbItem> breadcrumbs = buildCategoryBreadcrumbs(p.getCategoryId());
        List<ImageDto> images = p.getImages().stream()
                .map(i -> new ImageDto(i.getId(), i.getImageUrl(), i.isPrimary(), i.getDisplayOrder()))
                .toList();

        List<VariantDto> variants = p.getVariants().stream()
                .sorted(Comparator.comparing(ProductVariant::isDefaultVariant).reversed()
                        .thenComparing(ProductVariant::getPrice))
                .map(this::toVariantDto)
                .toList();

        List<VariantGroupDto> groups = extractVariantGroups(p.getVariants());
        int totalStock = p.getVariants().stream()
                .filter(ProductVariant::isActive)
                .mapToInt(ProductVariant::getStock)
                .sum();

        Boolean wishlisted = null;
        if (viewerCustomerId != null) {
            Long cnt = em.createQuery(
                            "SELECT COUNT(w) FROM WishlistItem w WHERE w.customerId = :cid AND w.productId = :pid",
                            Long.class)
                    .setParameter("cid", viewerCustomerId)
                    .setParameter("pid", p.getId())
                    .getSingleResult();
            wishlisted = cnt > 0;
        }

        String weightDisplay = p.getWeightGram() >= 1000 && p.getWeightGram() % 1000 == 0
                ? (p.getWeightGram() / 1000) + " kg"
                : p.getWeightGram() + " gr";

        return new ProductDetailResponse(
                p.getId(),
                p.getName(),
                p.getSlug(),
                p.getCondition(),
                p.getWeightGram(),
                weightDisplay,
                p.getDescription(),
                p.getImportantInfo(),
                p.isPreorder(),
                p.getPreorderDays(),
                p.getCampaignLabel(),
                p.getMinPrice(),
                p.getMaxPrice(),
                p.getViewCount(),
                p.getSoldCount(),
                p.getRatingAvg(),
                p.getReviewCount(),
                totalStock,
                p.getEtalaseId(),
                etalaseName,
                breadcrumbs,
                store,
                images,
                groups,
                variants,
                wishlisted,
                p.getCreatedAt()
        );
    }

    private List<BreadcrumbItem> buildCategoryBreadcrumbs(UUID categoryId) {
        if (categoryId == null) {
            return List.of();
        }
        LinkedList<BreadcrumbItem> chain = new LinkedList<>();
        UUID cursor = categoryId;
        int guard = 0;
        while (cursor != null && guard++ < 6) {
            Category cat = em.find(Category.class, cursor);
            if (cat == null) {
                break;
            }
            chain.addFirst(new BreadcrumbItem(cat.getId(), cat.getName(), cat.getSlug(), cat.getLevel()));
            cursor = cat.getParentId();
        }
        return chain;
    }

    private List<VariantGroupDto> extractVariantGroups(List<ProductVariant> variants) {
        String g1Name = null;
        Set<String> g1Values = new LinkedHashSet<>();
        String g2Name = null;
        Set<String> g2Values = new LinkedHashSet<>();

        for (ProductVariant v : variants) {
            if (v.getVariantGroup1Name() != null && v.getVariantGroup1Value() != null) {
                g1Name = v.getVariantGroup1Name();
                g1Values.add(v.getVariantGroup1Value());
            }
            if (v.getVariantGroup2Name() != null && v.getVariantGroup2Value() != null) {
                g2Name = v.getVariantGroup2Name();
                g2Values.add(v.getVariantGroup2Value());
            }
        }
        List<VariantGroupDto> groups = new ArrayList<>();
        if (g1Name != null) {
            groups.add(new VariantGroupDto(g1Name, List.copyOf(g1Values)));
        }
        if (g2Name != null) {
            groups.add(new VariantGroupDto(g2Name, List.copyOf(g2Values)));
        }
        return groups;
    }

    private VariantDto toVariantDto(ProductVariant v) {
        return new VariantDto(
                v.getId(),
                v.getSku(),
                v.getVariantGroup1Name(),
                v.getVariantGroup1Value(),
                v.getVariantGroup2Name(),
                v.getVariantGroup2Value(),
                v.getVariantLabel(),
                v.getPrice(),
                v.getStock(),
                v.effectiveWeightGram(),
                v.effectiveImageUrl(),
                v.isDefaultVariant(),
                v.isActive()
        );
    }

    private VariantSnapshot toSnapshot(ProductVariant v) {
        Product p = v.getProduct();
        return new VariantSnapshot(
                v.getId(),
                p.getId(),
                p.getStoreId(),
                p.getName(),
                v.getVariantLabel(),
                v.getSku(),
                v.effectiveImageUrl(),
                v.getPrice(),
                v.getStock(),
                v.effectiveWeightGram(),
                p.isActive(),
                v.isActive()
        );
    }

    private void recalculatePriceBounds(Product p) {
        List<BigDecimal> activePrices = p.getVariants().stream()
                .filter(ProductVariant::isActive)
                .map(ProductVariant::getPrice)
                .toList();
        if (activePrices.isEmpty()) {
            activePrices = p.getVariants().stream().map(ProductVariant::getPrice).toList();
        }
        BigDecimal min = activePrices.stream().min(BigDecimal::compareTo).orElse(BigDecimal.ZERO);
        BigDecimal max = activePrices.stream().max(BigDecimal::compareTo).orElse(min);
        p.setMinPrice(min);
        p.setMaxPrice(max);
    }

    private String resolveVariantLabel(VariantInput vIn) {
        if (vIn.variantLabel() != null && !vIn.variantLabel().isBlank()) {
            return vIn.variantLabel().trim();
        }
        if (vIn.variantGroup1Value() != null && vIn.variantGroup2Value() != null) {
            return vIn.variantGroup1Value().trim() + ", " + vIn.variantGroup2Value().trim();
        }
        return vIn.variantGroup1Value();
    }

    private String allocateProductSlug(UUID storeId, String name) {
        String base = SlugUtils.toSlug(name);
        if (base.isBlank()) {
            base = "product";
        }
        String candidate = base;
        int seq = 1;
        while (productRepo.existsByStoreIdAndSlug(storeId, candidate)) {
            candidate = base + "-" + seq++;
        }
        return candidate;
    }

    private Sort resolveCatalogSort(String sortBy) {
        if (sortBy == null) {
            return Sort.by(Sort.Direction.DESC, "createdAt");
        }
        return switch (sortBy.toUpperCase()) {
            case "OLDEST" -> Sort.by(Sort.Direction.ASC, "createdAt");
            case "PRICE_ASC" -> Sort.by(Sort.Direction.ASC, "minPrice");
            case "PRICE_DESC" -> Sort.by(Sort.Direction.DESC, "minPrice");
            case "BEST_SELLING" -> Sort.by(Sort.Direction.DESC, "soldCount");
            case "HIGHEST_RATING" -> Sort.by(Sort.Direction.DESC, "ratingAvg");
            default -> Sort.by(Sort.Direction.DESC, "createdAt");
        };
    }
}
