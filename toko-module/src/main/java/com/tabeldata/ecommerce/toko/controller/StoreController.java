package com.tabeldata.ecommerce.toko.controller;

import com.tabeldata.ecommerce.common.api.ApiResponse;
import com.tabeldata.ecommerce.common.api.PageResponse;
import com.tabeldata.ecommerce.toko.dto.StoreRequests.CreateStoreRequest;
import com.tabeldata.ecommerce.toko.dto.StoreRequests.EtalaseRequest;
import com.tabeldata.ecommerce.toko.dto.StoreRequests.UpdateStoreRequest;
import com.tabeldata.ecommerce.toko.dto.StoreResponses.StoreEtalaseResponse;
import com.tabeldata.ecommerce.toko.dto.StoreResponses.StoreEtalaseSidebarResponse;
import com.tabeldata.ecommerce.toko.dto.StoreResponses.StoreResponse;
import com.tabeldata.ecommerce.toko.entity.StoreBadge;
import com.tabeldata.ecommerce.toko.service.StoreService;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/v1/stores")
@Tag(name = "Toko (Storefront & Etalase)")
public class StoreController {

    private final StoreService tokoSvc;

    public StoreController(StoreService tokoSvc) {
        this.tokoSvc = tokoSvc;
    }

    @GetMapping
    public ApiResponse<PageResponse<StoreResponse>> browseToko(
            @RequestParam(required = false) String q,
            @RequestParam(required = false) String city,
            @RequestParam(required = false) StoreBadge badge,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "12") int size
    ) {
        var pageReq = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "ratingAvg"));
        return ApiResponse.ok(tokoSvc.listStores(q, city, badge, pageReq));
    }

    @GetMapping("/{storeId}")
    public ApiResponse<StoreResponse> fetchProfilToko(
            @PathVariable UUID storeId,
            @RequestHeader(value = "X-Customer-Id", required = false) UUID viewerId
    ) {
        return ApiResponse.ok(tokoSvc.getStore(storeId, viewerId));
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ApiResponse<StoreResponse> registerToko(@Valid @RequestBody CreateStoreRequest payload) {
        return ApiResponse.ok("Toko berhasil dibuat", tokoSvc.createStore(payload));
    }

    @PutMapping("/{storeId}")
    public ApiResponse<StoreResponse> modifyToko(
            @PathVariable UUID storeId,
            @Valid @RequestBody UpdateStoreRequest payload
    ) {
        return ApiResponse.ok("Profil toko diperbarui", tokoSvc.updateStore(storeId, payload));
    }

    @DeleteMapping("/{storeId}")
    public ApiResponse<Void> archiveToko(@PathVariable UUID storeId) {
        tokoSvc.deactivateStore(storeId);
        return ApiResponse.okMessage("Toko dinonaktifkan");
    }

    @GetMapping("/{storeId}/etalases")
    public ApiResponse<StoreEtalaseSidebarResponse> fetchSidebarEtalase(@PathVariable UUID storeId) {
        return ApiResponse.ok(tokoSvc.listEtalases(storeId));
    }

    @PostMapping("/{storeId}/etalases")
    @ResponseStatus(HttpStatus.CREATED)
    public ApiResponse<StoreEtalaseResponse> registerEtalase(
            @PathVariable UUID storeId,
            @Valid @RequestBody EtalaseRequest payload
    ) {
        return ApiResponse.ok("Etalase ditambahkan", tokoSvc.addEtalase(storeId, payload));
    }

    @PutMapping("/{storeId}/etalases/{etalaseId}")
    public ApiResponse<StoreEtalaseResponse> modifyEtalase(
            @PathVariable UUID storeId,
            @PathVariable UUID etalaseId,
            @Valid @RequestBody EtalaseRequest payload
    ) {
        return ApiResponse.ok("Etalase diperbarui", tokoSvc.editEtalase(storeId, etalaseId, payload));
    }

    @DeleteMapping("/{storeId}/etalases/{etalaseId}")
    public ApiResponse<Void> eraseEtalase(@PathVariable UUID storeId, @PathVariable UUID etalaseId) {
        tokoSvc.removeEtalase(storeId, etalaseId);
        return ApiResponse.okMessage("Etalase dihapus");
    }

    @PostMapping("/{storeId}/follow")
    public ApiResponse<Void> ikutiToko(
            @PathVariable UUID storeId,
            @RequestHeader("X-Customer-Id") UUID customerId
    ) {
        tokoSvc.follow(storeId, customerId);
        return ApiResponse.okMessage("Berhasil mengikuti toko");
    }

    @DeleteMapping("/{storeId}/follow")
    public ApiResponse<Void> berhentiIkutiToko(
            @PathVariable UUID storeId,
            @RequestHeader("X-Customer-Id") UUID customerId
    ) {
        tokoSvc.unfollow(storeId, customerId);
        return ApiResponse.okMessage("Berhenti mengikuti toko");
    }
}
