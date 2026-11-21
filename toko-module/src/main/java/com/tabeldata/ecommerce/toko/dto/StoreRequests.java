package com.tabeldata.ecommerce.toko.dto;

import com.tabeldata.ecommerce.toko.entity.StoreBadge;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public final class StoreRequests {

    private StoreRequests() {}

    public record CreateStoreRequest(
            @NotBlank(message = "Nama toko wajib diisi")
            @Size(max = 120) String name,
            @Size(max = 500) String logoUrl,
            StoreBadge badge,
            @NotBlank(message = "Kota toko wajib diisi")
            @Size(max = 100) String city,
            Boolean online,
            @Min(1) @Max(10080) Integer avgProcessTimeMinutes,
            @Size(max = 100) String operatingHours,
            String description
    ) {}

    public record UpdateStoreRequest(
            @NotBlank(message = "Nama toko wajib diisi")
            @Size(max = 120) String name,
            @Size(max = 500) String logoUrl,
            StoreBadge badge,
            @NotBlank(message = "Kota toko wajib diisi")
            @Size(max = 100) String city,
            Boolean online,
            @Min(1) @Max(10080) Integer avgProcessTimeMinutes,
            @Size(max = 100) String operatingHours,
            String description
    ) {}

    public record EtalaseRequest(
            @NotBlank(message = "Nama etalase wajib diisi")
            @Size(max = 120) String name,
            @Min(0) Integer displayOrder
    ) {}
}
