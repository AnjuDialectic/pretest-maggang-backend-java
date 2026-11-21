package com.tabeldata.ecommerce.toko.service;

import com.tabeldata.ecommerce.common.api.PageResponse;
import com.tabeldata.ecommerce.common.exception.ConflictException;
import com.tabeldata.ecommerce.common.exception.ResourceNotFoundException;
import com.tabeldata.ecommerce.common.port.StorePort;
import com.tabeldata.ecommerce.common.util.SlugUtils;
import com.tabeldata.ecommerce.toko.dto.StoreRequests.CreateStoreRequest;
import com.tabeldata.ecommerce.toko.dto.StoreRequests.EtalaseRequest;
import com.tabeldata.ecommerce.toko.dto.StoreRequests.UpdateStoreRequest;
import com.tabeldata.ecommerce.toko.dto.StoreResponses.StoreEtalaseResponse;
import com.tabeldata.ecommerce.toko.dto.StoreResponses.StoreEtalaseSidebarResponse;
import com.tabeldata.ecommerce.toko.dto.StoreResponses.StoreResponse;
import com.tabeldata.ecommerce.toko.dto.StoreResponses.SystemEtalaseItem;
import com.tabeldata.ecommerce.toko.entity.Store;
import com.tabeldata.ecommerce.toko.entity.StoreBadge;
import com.tabeldata.ecommerce.toko.entity.StoreEtalase;
import com.tabeldata.ecommerce.toko.entity.StoreFollower;
import com.tabeldata.ecommerce.toko.repository.StoreEtalaseRepository;
import com.tabeldata.ecommerce.toko.repository.StoreFollowerRepository;
import com.tabeldata.ecommerce.toko.repository.StoreRepository;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

@Service
public class StoreService implements StorePort {

    private static final List<SystemEtalaseItem> DEFAULT_FILTERS = List.of(
            new SystemEtalaseItem("ALL", "Semua Produk"),
            new SystemEtalaseItem("SOLD", "Produk Terjual"),
            new SystemEtalaseItem("PREORDER", "Preorder")
    );

    private final StoreRepository storeRepository;
    private final StoreEtalaseRepository etalaseRepository;
    private final StoreFollowerRepository followerRepository;

    public StoreService(
            StoreRepository storeRepository,
            StoreEtalaseRepository etalaseRepository,
            StoreFollowerRepository followerRepository
    ) {
        this.storeRepository = storeRepository;
        this.etalaseRepository = etalaseRepository;
        this.followerRepository = followerRepository;
    }

    @Transactional(readOnly = true)
    public PageResponse<StoreResponse> listStores(String q, String city, StoreBadge badge, Pageable pageable) {
        String keyword = blankToNull(q);
        String cityFilter = blankToNull(city);
        return PageResponse.from(
                storeRepository.searchActiveStores(keyword, cityFilter, badge, pageable)
                        .map(store -> buildResponse(store, null))
        );
    }

    @Transactional(readOnly = true)
    public StoreResponse getStore(UUID storeId, UUID viewerCustomerId) {
        Store store = loadActiveStore(storeId);
        Boolean following = viewerCustomerId == null
                ? null
                : followerRepository.existsByStoreIdAndCustomerId(storeId, viewerCustomerId);
        return buildResponse(store, following);
    }

    @Transactional
    public StoreResponse createStore(CreateStoreRequest request) {
        Store store = new Store();
        applyStoreFields(
                store,
                request.name(),
                request.logoUrl(),
                request.badge() == null ? StoreBadge.REGULAR : request.badge(),
                request.city(),
                request.online() == null || request.online(),
                request.avgProcessTimeMinutes() == null ? 180 : request.avgProcessTimeMinutes(),
                blankToNull(request.operatingHours()) == null ? "09:00 - 21:00 WIB" : request.operatingHours().trim(),
                request.description()
        );
        store.setSlug(allocateSlug(request.name()));
        store.setActive(true);
        return buildResponse(storeRepository.save(store), null);
    }

    @Transactional
    public StoreResponse updateStore(UUID storeId, UpdateStoreRequest request) {
        Store store = loadActiveStore(storeId);
        applyStoreFields(
                store,
                request.name(),
                request.logoUrl(),
                request.badge() == null ? store.getBadge() : request.badge(),
                request.city(),
                request.online() == null ? store.isOnline() : request.online(),
                request.avgProcessTimeMinutes() == null ? store.getAvgProcessTimeMinutes() : request.avgProcessTimeMinutes(),
                blankToNull(request.operatingHours()) == null ? store.getOperatingHours() : request.operatingHours().trim(),
                request.description()
        );
        return buildResponse(storeRepository.save(store), null);
    }

    @Transactional
    public void deactivateStore(UUID storeId) {
        Store store = loadActiveStore(storeId);
        store.setActive(false);
        storeRepository.save(store);
    }

    @Transactional(readOnly = true)
    public StoreEtalaseSidebarResponse listEtalases(UUID storeId) {
        loadActiveStore(storeId);
        List<StoreEtalaseResponse> customItems = etalaseRepository
                .findByStoreIdOrderByDisplayOrderAscNameAsc(storeId)
                .stream()
                .map(StoreEtalaseResponse::from)
                .toList();
        int totalCount = DEFAULT_FILTERS.size() + customItems.size();
        return new StoreEtalaseSidebarResponse(storeId, totalCount, DEFAULT_FILTERS, customItems);
    }

    @Transactional
    public StoreEtalaseResponse addEtalase(UUID storeId, EtalaseRequest request) {
        Store store = loadActiveStore(storeId);
        String slug = SlugUtils.toSlug(request.name());
        if (etalaseRepository.existsByStoreIdAndSlug(storeId, slug)) {
            throw new ConflictException("Nama etalase sudah digunakan pada toko ini");
        }
        StoreEtalase item = new StoreEtalase();
        item.setStore(store);
        item.setName(request.name().trim());
        item.setSlug(slug);
        item.setDisplayOrder(request.displayOrder() == null ? 0 : request.displayOrder());
        return StoreEtalaseResponse.from(etalaseRepository.save(item));
    }

    @Transactional
    public StoreEtalaseResponse editEtalase(UUID storeId, UUID etalaseId, EtalaseRequest request) {
        loadActiveStore(storeId);
        StoreEtalase item = etalaseRepository.findByIdAndStoreId(etalaseId, storeId)
                .orElseThrow(() -> new ResourceNotFoundException("Etalase tidak ditemukan"));
        String slug = SlugUtils.toSlug(request.name());
        if (!slug.equals(item.getSlug()) && etalaseRepository.existsByStoreIdAndSlug(storeId, slug)) {
            throw new ConflictException("Nama etalase sudah digunakan pada toko ini");
        }
        item.setName(request.name().trim());
        item.setSlug(slug);
        if (request.displayOrder() != null) {
            item.setDisplayOrder(request.displayOrder());
        }
        return StoreEtalaseResponse.from(etalaseRepository.save(item));
    }

    @Transactional
    public void removeEtalase(UUID storeId, UUID etalaseId) {
        loadActiveStore(storeId);
        StoreEtalase item = etalaseRepository.findByIdAndStoreId(etalaseId, storeId)
                .orElseThrow(() -> new ResourceNotFoundException("Etalase tidak ditemukan"));
        etalaseRepository.delete(item);
    }

    @Transactional
    public void follow(UUID storeId, UUID customerId) {
        loadActiveStore(storeId);
        if (!followerRepository.existsByStoreIdAndCustomerId(storeId, customerId)) {
            StoreFollower entry = new StoreFollower();
            entry.setStoreId(storeId);
            entry.setCustomerId(customerId);
            followerRepository.save(entry);
        }
    }

    @Transactional
    public void unfollow(UUID storeId, UUID customerId) {
        loadActiveStore(storeId);
        followerRepository.findByStoreIdAndCustomerId(storeId, customerId)
                .ifPresent(followerRepository::delete);
    }

    @Override
    @Transactional(readOnly = true)
    public StoreSummary getStoreSummary(UUID storeId) {
        Store store = storeRepository.findById(storeId)
                .orElseThrow(() -> new ResourceNotFoundException("Toko tidak ditemukan: " + storeId));
        return toSummary(store);
    }

    @Override
    @Transactional(readOnly = true)
    public Map<UUID, StoreSummary> getStoreSummaries(Collection<UUID> storeIds) {
        Map<UUID, StoreSummary> map = new LinkedHashMap<>();
        if (storeIds == null || storeIds.isEmpty()) {
            return map;
        }
        for (Store store : storeRepository.findAllByIdIn(storeIds)) {
            map.put(store.getId(), toSummary(store));
        }
        return map;
    }

    @Override
    @Transactional(readOnly = true)
    public boolean existsEtalaseInStore(UUID storeId, UUID etalaseId) {
        return etalaseRepository.existsByIdAndStoreId(etalaseId, storeId);
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<String> findEtalaseName(UUID etalaseId) {
        return etalaseRepository.findById(etalaseId).map(StoreEtalase::getName);
    }

    private Store loadActiveStore(UUID storeId) {
        Store store = storeRepository.findById(storeId)
                .orElseThrow(() -> new ResourceNotFoundException("Toko tidak ditemukan: " + storeId));
        if (!store.isActive()) {
            throw new ResourceNotFoundException("Toko tidak aktif: " + storeId);
        }
        return store;
    }

    private void applyStoreFields(
            Store target,
            String name,
            String logoUrl,
            StoreBadge badge,
            String city,
            boolean online,
            int avgProcessMinutes,
            String operatingHours,
            String description
    ) {
        target.setName(name.trim());
        target.setLogoUrl(logoUrl);
        target.setBadge(badge);
        target.setCity(city.trim());
        target.setOnline(online);
        target.setAvgProcessTimeMinutes(avgProcessMinutes);
        target.setOperatingHours(operatingHours);
        target.setDescription(description);
    }

    private StoreResponse buildResponse(Store store, Boolean following) {
        return StoreResponse.from(
                store,
                followerRepository.countByStoreId(store.getId()),
                etalaseRepository.countByStoreId(store.getId()),
                following
        );
    }

    private StoreSummary toSummary(Store store) {
        return new StoreSummary(
                store.getId(),
                store.getName(),
                store.getSlug(),
                store.getLogoUrl(),
                store.getBadge().name(),
                store.getCity(),
                store.isOnline(),
                store.isActive()
        );
    }

    private String allocateSlug(String name) {
        String base = SlugUtils.toSlug(name);
        if (base.isBlank()) {
            base = "store";
        }
        String candidate = base;
        int seq = 1;
        while (storeRepository.existsBySlug(candidate)) {
            candidate = base + "-" + seq++;
        }
        return candidate;
    }

    private String blankToNull(String value) {
        return (value == null || value.isBlank()) ? null : value.trim();
    }
}
