package com.tabeldata.ecommerce.pembelian.service;

import com.tabeldata.ecommerce.common.api.PageResponse;
import com.tabeldata.ecommerce.common.exception.BusinessRuleException;
import com.tabeldata.ecommerce.common.exception.InsufficientStockException;
import com.tabeldata.ecommerce.common.exception.ResourceNotFoundException;
import com.tabeldata.ecommerce.common.port.ProductPort;
import com.tabeldata.ecommerce.common.port.ProductPort.StockAdjustment;
import com.tabeldata.ecommerce.common.port.ProductPort.VariantSnapshot;
import com.tabeldata.ecommerce.common.port.StorePort;
import com.tabeldata.ecommerce.common.port.StorePort.StoreSummary;
import com.tabeldata.ecommerce.pembelian.dto.PembelianDtos.*;
import com.tabeldata.ecommerce.pembelian.entity.Cart;
import com.tabeldata.ecommerce.pembelian.entity.Cart.CartItem;
import com.tabeldata.ecommerce.pembelian.entity.Cart.Customer;
import com.tabeldata.ecommerce.pembelian.entity.Transaction;
import com.tabeldata.ecommerce.pembelian.entity.Transaction.TransactionItem;
import com.tabeldata.ecommerce.pembelian.repository.TransactionRepository;
import jakarta.persistence.EntityManager;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.concurrent.ThreadLocalRandom;

@Service
public class PembelianService {

    private static final BigDecimal DEFAULT_SHIPPING_COST = new BigDecimal("15000.00");
    private static final BigDecimal DEFAULT_SERVICE_FEE = new BigDecimal("2300.00");
    private static final DateTimeFormatter INV_DATE_FMT = DateTimeFormatter.ofPattern("yyyyMMdd");

    private final TransactionRepository txRepo;
    private final ProductPort productPort;
    private final StorePort storePort;
    private final EntityManager em;

    public PembelianService(
            TransactionRepository txRepo,
            ProductPort productPort,
            StorePort storePort,
            EntityManager em
    ) {
        this.txRepo = txRepo;
        this.productPort = productPort;
        this.storePort = storePort;
        this.em = em;
    }

    @Transactional
    public CartSummaryResponse getCustomerCart(UUID customerId) {
        Cart cart = obtainOrCreateCart(customerId);
        return buildCartSummary(cart);
    }

    @Transactional
    public CartSummaryResponse addItemToCart(UUID customerId, AddCartItemRequest req) {
        Cart cart = obtainOrCreateCart(customerId);
        VariantSnapshot snap = productPort.getVariantSnapshot(req.productVariantId());
        if (!snap.isPurchasable()) {
            throw new BusinessRuleException("Produk atau varian sedang tidak tersedia");
        }

        Optional<CartItem> existingOpt = cart.getItems().stream()
                .filter(it -> it.getProductVariantId().equals(req.productVariantId()))
                .findFirst();

        int targetQty = existingOpt.map(it -> it.getQuantity() + req.quantity()).orElse(req.quantity());
        if (targetQty > snap.stock()) {
            throw new InsufficientStockException(snap.variantId(), snap.productName(), targetQty, snap.stock());
        }

        if (existingOpt.isPresent()) {
            CartItem existing = existingOpt.get();
            existing.setQuantity(targetQty);
            if (req.note() != null) {
                existing.setNote(req.note().trim());
            }
            existing.setSelected(true);
        } else {
            CartItem item = new CartItem();
            item.setCart(cart);
            item.setStoreId(snap.storeId());
            item.setProductId(snap.productId());
            item.setProductVariantId(snap.variantId());
            item.setQuantity(req.quantity());
            item.setNote(req.note() != null ? req.note().trim() : null);
            item.setSelected(true);
            cart.getItems().add(item);
        }
        em.flush();
        return buildCartSummary(cart);
    }

    @Transactional
    public CartSummaryResponse modifyCartItem(UUID customerId, UUID cartItemId, UpdateCartItemRequest req) {
        Cart cart = obtainOrCreateCart(customerId);
        CartItem target = findItemInCart(cart, cartItemId);

        if (req.quantity() != null) {
            VariantSnapshot snap = productPort.getVariantSnapshot(target.getProductVariantId());
            if (req.quantity() > snap.stock()) {
                throw new InsufficientStockException(snap.variantId(), snap.productName(), req.quantity(), snap.stock());
            }
            target.setQuantity(req.quantity());
        }
        if (req.note() != null) {
            target.setNote(req.note().isBlank() ? null : req.note().trim());
        }
        if (req.selected() != null) {
            target.setSelected(req.selected());
        }
        em.flush();
        return buildCartSummary(cart);
    }

    @Transactional
    public CartSummaryResponse selectAllCartItems(UUID customerId, boolean selected) {
        Cart cart = obtainOrCreateCart(customerId);
        cart.getItems().forEach(it -> it.setSelected(selected));
        em.flush();
        return buildCartSummary(cart);
    }

    @Transactional
    public CartSummaryResponse removeCartItem(UUID customerId, UUID cartItemId) {
        Cart cart = obtainOrCreateCart(customerId);
        CartItem target = findItemInCart(cart, cartItemId);
        cart.getItems().remove(target);
        em.flush();
        return buildCartSummary(cart);
    }

    @Transactional
    public CartSummaryResponse removeSelectedCartItems(UUID customerId) {
        Cart cart = obtainOrCreateCart(customerId);
        cart.getItems().removeIf(CartItem::isSelected);
        em.flush();
        return buildCartSummary(cart);
    }

    @Transactional
    public CartSummaryResponse moveCartItemToWishlist(UUID customerId, UUID cartItemId) {
        Cart cart = obtainOrCreateCart(customerId);
        CartItem target = findItemInCart(cart, cartItemId);
        productPort.addToWishlist(customerId, target.getProductId());
        cart.getItems().remove(target);
        em.flush();
        return buildCartSummary(cart);
    }

    @Transactional
    public CheckoutResultResponse checkoutSelectedCart(UUID customerId, CheckoutRequest req) {
        Customer customer = requireCustomer(customerId);
        Cart cart = obtainOrCreateCart(customerId);
        List<CartItem> selectedItems = cart.getItems().stream()
                .filter(CartItem::isSelected)
                .toList();
        if (selectedItems.isEmpty()) {
            throw new BusinessRuleException("Pilih minimal 1 barang di keranjang untuk checkout");
        }

        List<StockAdjustment> adjustments = selectedItems.stream()
                .map(it -> new StockAdjustment(it.getProductVariantId(), it.getQuantity()))
                .toList();
        productPort.reserveStock(adjustments);

        Set<UUID> variantIds = new LinkedHashSet<>();
        Set<UUID> storeIds = new LinkedHashSet<>();
        for (CartItem it : selectedItems) {
            variantIds.add(it.getProductVariantId());
            storeIds.add(it.getStoreId());
        }
        Map<UUID, VariantSnapshot> variantMap = productPort.getVariantSnapshots(variantIds);
        Map<UUID, StoreSummary> storeMap = storePort.getStoreSummaries(storeIds);

        Map<UUID, List<CartItem>> groupedByStore = new LinkedHashMap<>();
        for (CartItem it : selectedItems) {
            groupedByStore.computeIfAbsent(it.getStoreId(), k -> new ArrayList<>()).add(it);
        }

        String address = resolveShippingAddress(req.shippingAddress(), customer);
        boolean freeShipping = req.freeShipping() == null || req.freeShipping();
        BigDecimal shippingCost = req.shippingCostPerStore() != null ? req.shippingCostPerStore() : DEFAULT_SHIPPING_COST;
        BigDecimal totalServiceFee = req.serviceFee() != null ? req.serviceFee() : DEFAULT_SERVICE_FEE;
        BigDecimal totalPromo = req.promoDiscount() != null ? req.promoDiscount() : BigDecimal.ZERO;
        boolean instantPay = req.instantPay() == null || req.instantPay();

        List<TransactionDetailResponse> createdInvoices = new ArrayList<>();
        BigDecimal combinedTotal = BigDecimal.ZERO;
        int groupIndex = 0;

        for (Map.Entry<UUID, List<CartItem>> entry : groupedByStore.entrySet()) {
            UUID storeId = entry.getKey();
            StoreSummary store = storeMap.get(storeId);
            if (store == null || !store.isActive()) {
                throw new BusinessRuleException("Toko tujuan sedang tidak aktif");
            }

            BigDecimal storeServiceFee = groupIndex == 0 ? totalServiceFee : BigDecimal.ZERO;
            BigDecimal storePromo = groupIndex == 0 ? totalPromo : BigDecimal.ZERO;
            groupIndex++;

            Transaction tx = createStoreTransaction(
                    customerId,
                    store,
                    entry.getValue(),
                    variantMap,
                    req.courierName(),
                    req.courierService(),
                    freeShipping,
                    shippingCost,
                    storeServiceFee,
                    storePromo,
                    address,
                    req.paymentMethod(),
                    instantPay
            );
            Transaction saved = txRepo.save(tx);
            combinedTotal = combinedTotal.add(saved.getGrandTotal());
            createdInvoices.add(toDetailResponse(saved));
        }

        cart.getItems().removeIf(CartItem::isSelected);
        return new CheckoutResultResponse(createdInvoices.size(), combinedTotal, createdInvoices);
    }

    @Transactional
    public TransactionDetailResponse directBuy(UUID customerId, DirectBuyRequest req) {
        Customer customer = requireCustomer(customerId);
        productPort.reserveStock(List.of(new StockAdjustment(req.productVariantId(), req.quantity())));

        VariantSnapshot snap = productPort.getVariantSnapshot(req.productVariantId());
        StoreSummary store = storePort.getStoreSummary(snap.storeId());
        if (!store.isActive()) {
            throw new BusinessRuleException("Toko tidak aktif");
        }

        CartItem pseudoItem = new CartItem();
        pseudoItem.setStoreId(snap.storeId());
        pseudoItem.setProductId(snap.productId());
        pseudoItem.setProductVariantId(snap.variantId());
        pseudoItem.setQuantity(req.quantity());
        pseudoItem.setNote(req.note());

        boolean freeShipping = req.freeShipping() == null || req.freeShipping();
        BigDecimal shippingCost = req.shippingCost() != null ? req.shippingCost() : DEFAULT_SHIPPING_COST;
        BigDecimal serviceFee = req.serviceFee() != null ? req.serviceFee() : DEFAULT_SERVICE_FEE;
        BigDecimal promo = req.promoDiscount() != null ? req.promoDiscount() : BigDecimal.ZERO;
        boolean instantPay = req.instantPay() == null || req.instantPay();

        Transaction tx = createStoreTransaction(
                customerId,
                store,
                List.of(pseudoItem),
                Map.of(snap.variantId(), snap),
                req.courierName(),
                req.courierService(),
                freeShipping,
                shippingCost,
                serviceFee,
                promo,
                resolveShippingAddress(req.shippingAddress(), customer),
                req.paymentMethod(),
                instantPay
        );
        return toDetailResponse(txRepo.save(tx));
    }

    @Transactional(readOnly = true)
    public PageResponse<TransactionListCardDto> listCustomerTransactions(
            UUID customerId,
            String statusGroup,
            String specificStatus,
            String transactionType,
            LocalDate startDate,
            LocalDate endDate,
            String keyword,
            int page,
            int size
    ) {
        List<String> statuses = resolveStatusFilter(statusGroup, specificStatus);
        boolean filterStatus = !statuses.isEmpty();
        Collection<String> queryStatuses = filterStatus ? statuses : List.of("__NONE__");

        String cleanType = (transactionType == null || transactionType.isBlank() || "ALL".equalsIgnoreCase(transactionType))
                ? null
                : transactionType.trim().toUpperCase();
        OffsetDateTime fromTime = startDate != null
                ? startDate.atStartOfDay().atOffset(ZoneOffset.ofHours(7))
                : null;
        OffsetDateTime toTime = endDate != null
                ? endDate.plusDays(1).atStartOfDay().minusNanos(1).atOffset(ZoneOffset.ofHours(7))
                : null;
        String cleanKeyword = (keyword == null || keyword.isBlank()) ? null : keyword.trim();

        Page<Transaction> txPage = txRepo.searchCustomerTransactions(
                customerId,
                filterStatus,
                queryStatuses,
                cleanType,
                fromTime,
                toTime,
                cleanKeyword,
                PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "purchasedAt"))
        );

        return PageResponse.from(txPage.map(this::toListCardDto));
    }

    @Transactional(readOnly = true)
    public Map<String, Long> countWaitingPaymentSummary(UUID customerId) {
        return Map.of("waitingPaymentCount", txRepo.countWaitingPayment(customerId));
    }

    @Transactional(readOnly = true)
    public TransactionDetailResponse getCustomerTransactionDetail(UUID customerId, UUID transactionId) {
        Transaction tx = txRepo.findByIdAndCustomerId(transactionId, customerId)
                .orElseThrow(() -> new ResourceNotFoundException("Transaksi tidak ditemukan: " + transactionId));
        return toDetailResponse(tx);
    }

    @Transactional
    public TransactionDetailResponse advanceTransactionStatus(
            UUID transactionId,
            UpdateTransactionStatusRequest req
    ) {
        Transaction tx = txRepo.findById(transactionId)
                .orElseThrow(() -> new ResourceNotFoundException("Transaksi tidak ditemukan: " + transactionId));
        String prevStatus = tx.getStatus();
        String nextStatus = req.status().trim().toUpperCase();

        if ("SELESAI".equals(prevStatus) || "DIBATALKAN".equals(prevStatus)) {
            throw new BusinessRuleException("Transaksi dengan status " + prevStatus + " sudah bersifat final");
        }

        if ("DIBATALKAN".equals(nextStatus)) {
            List<StockAdjustment> releases = tx.getItems().stream()
                    .map(it -> new StockAdjustment(it.getProductVariantId(), it.getQuantity()))
                    .toList();
            productPort.releaseStock(releases);
        }

        if (req.trackingNumber() != null && !req.trackingNumber().isBlank()) {
            tx.setTrackingNumber(req.trackingNumber().trim());
        }
        tx.setStatus(nextStatus);
        String desc = (req.description() != null && !req.description().isBlank())
                ? req.description().trim()
                : defaultStatusDescription(nextStatus);
        tx.appendHistory(nextStatus, desc);

        return toDetailResponse(txRepo.save(tx));
    }

    @Transactional
    public CartSummaryResponse buyAgainFromTransaction(UUID customerId, UUID transactionId) {
        Transaction tx = txRepo.findByIdAndCustomerId(transactionId, customerId)
                .orElseThrow(() -> new ResourceNotFoundException("Transaksi tidak ditemukan: " + transactionId));
        for (TransactionItem it : tx.getItems()) {
            addItemToCart(customerId, new AddCartItemRequest(it.getProductVariantId(), it.getQuantity(), it.getNote()));
        }
        return getCustomerCart(customerId);
    }

    private Transaction createStoreTransaction(
            UUID customerId,
            StoreSummary store,
            List<CartItem> storeItems,
            Map<UUID, VariantSnapshot> variantMap,
            String courierName,
            String courierService,
            boolean freeShipping,
            BigDecimal shippingCost,
            BigDecimal serviceFee,
            BigDecimal promoDiscount,
            String shippingAddress,
            String paymentMethod,
            boolean instantPay
    ) {
        Transaction tx = new Transaction();
        tx.setInvoiceNumber(generateUniqueInvoice());
        tx.setCustomerId(customerId);
        tx.setStoreId(store.id());
        tx.setStoreNameSnapshot(store.name());
        tx.setStoreBadgeSnapshot(store.badge());
        tx.setTransactionType("BELANJA");

        String initialStatus = instantPay ? "DIBAYAR" : "MENUNGGU_PEMBAYARAN";
        tx.setStatus(initialStatus);

        BigDecimal subtotalProduct = BigDecimal.ZERO;
        for (CartItem ci : storeItems) {
            VariantSnapshot snap = variantMap.get(ci.getProductVariantId());
            BigDecimal lineTotal = snap.price().multiply(BigDecimal.valueOf(ci.getQuantity()));
            subtotalProduct = subtotalProduct.add(lineTotal);

            TransactionItem ti = new TransactionItem();
            ti.setTransaction(tx);
            ti.setProductId(snap.productId());
            ti.setProductVariantId(snap.variantId());
            ti.setProductNameSnapshot(snap.productName());
            ti.setVariantLabelSnapshot(snap.variantLabel());
            ti.setProductImageSnapshot(snap.imageUrl());
            ti.setUnitPriceSnapshot(snap.price());
            ti.setQuantity(ci.getQuantity());
            ti.setSubtotal(lineTotal);
            ti.setNote(ci.getNote());
            tx.getItems().add(ti);
        }

        BigDecimal shippingDiscount = freeShipping ? shippingCost : BigDecimal.ZERO;
        BigDecimal rawTotal = subtotalProduct
                .add(shippingCost)
                .subtract(shippingDiscount)
                .add(serviceFee)
                .subtract(promoDiscount);
        BigDecimal grandTotal = rawTotal.compareTo(BigDecimal.ZERO) < 0 ? BigDecimal.ZERO : rawTotal;

        tx.setSubtotalProduct(subtotalProduct);
        tx.setShippingCost(shippingCost);
        tx.setShippingDiscount(shippingDiscount);
        tx.setServiceFee(serviceFee);
        tx.setPromoDiscount(promoDiscount);
        tx.setGrandTotal(grandTotal);
        tx.setCourierName(courierName.trim());
        tx.setCourierService(courierService.trim());
        tx.setFreeShipping(freeShipping);
        tx.setShippingAddress(shippingAddress);
        tx.setPaymentMethod(paymentMethod == null || paymentMethod.isBlank() ? " Gopay / Virtual Account" : paymentMethod.trim());
        tx.setPurchasedAt(OffsetDateTime.now());

        tx.appendHistory("MENUNGGU_PEMBAYARAN", "Pesanan dibuat dan stok berhasil diamankan");
        if (instantPay) {
            tx.appendHistory("DIBAYAR", "Pembayaran diverifikasi otomatis, diteruskan ke penjual");
        }
        return tx;
    }

    private Cart obtainOrCreateCart(UUID customerId) {
        requireCustomer(customerId);
        return txRepo.findCartWithItemsByCustomerId(customerId).orElseGet(() -> {
            Cart created = new Cart();
            created.setCustomerId(customerId);
            em.persist(created);
            em.flush();
            return created;
        });
    }

    private Customer requireCustomer(UUID customerId) {
        Customer c = em.find(Customer.class, customerId);
        if (c == null) {
            throw new ResourceNotFoundException("Customer tidak ditemukan: " + customerId);
        }
        return c;
    }

    private CartItem findItemInCart(Cart cart, UUID cartItemId) {
        return cart.getItems().stream()
                .filter(it -> it.getId().equals(cartItemId))
                .findFirst()
                .orElseThrow(() -> new ResourceNotFoundException("Item keranjang tidak ditemukan: " + cartItemId));
    }

    private CartSummaryResponse buildCartSummary(Cart cart) {
        if (cart.getItems().isEmpty()) {
            return new CartSummaryResponse(cart.getId(), cart.getCustomerId(), false, 0, 0, 0, BigDecimal.ZERO, List.of());
        }

        Set<UUID> variantIds = new LinkedHashSet<>();
        Set<UUID> storeIds = new LinkedHashSet<>();
        for (CartItem it : cart.getItems()) {
            variantIds.add(it.getProductVariantId());
            storeIds.add(it.getStoreId());
        }

        Map<UUID, VariantSnapshot> variantMap = productPort.getVariantSnapshots(variantIds);
        Map<UUID, StoreSummary> storeMap = storePort.getStoreSummaries(storeIds);

        Map<UUID, List<CartItemDto>> grouped = new LinkedHashMap<>();
        int selectedTotalQty = 0;
        int selectedDistinct = 0;
        BigDecimal selectedPrice = BigDecimal.ZERO;

        for (CartItem it : cart.getItems()) {
            VariantSnapshot snap = variantMap.get(it.getProductVariantId());
            BigDecimal unitPrice = snap != null ? snap.price() : BigDecimal.ZERO;
            BigDecimal lineSubtotal = unitPrice.multiply(BigDecimal.valueOf(it.getQuantity()));
            boolean purchasable = snap != null && snap.isPurchasable() && snap.stock() >= it.getQuantity();

            if (it.isSelected()) {
                selectedDistinct++;
                selectedTotalQty += it.getQuantity();
                selectedPrice = selectedPrice.add(lineSubtotal);
            }

            CartItemDto dto = new CartItemDto(
                    it.getId(),
                    it.getProductId(),
                    it.getProductVariantId(),
                    snap != null ? snap.productName() : "Produk Tidak Tersedia",
                    snap != null ? snap.variantLabel() : null,
                    snap != null ? snap.imageUrl() : null,
                    unitPrice,
                    it.getQuantity(),
                    lineSubtotal,
                    snap != null ? snap.stock() : 0,
                    it.getNote(),
                    it.isSelected(),
                    purchasable
            );
            grouped.computeIfAbsent(it.getStoreId(), k -> new ArrayList<>()).add(dto);
        }

        List<CartStoreGroupDto> storeGroups = new ArrayList<>();
        for (Map.Entry<UUID, List<CartItemDto>> entry : grouped.entrySet()) {
            StoreSummary s = storeMap.get(entry.getKey());
            boolean groupAllSelected = entry.getValue().stream().allMatch(CartItemDto::selected);
            storeGroups.add(new CartStoreGroupDto(
                    entry.getKey(),
                    s != null ? s.name() : "Toko",
                    s != null ? s.badge() : "REGULAR",
                    s != null ? s.city() : "-",
                    groupAllSelected,
                    entry.getValue()
            ));
        }

        boolean cartAllSelected = !cart.getItems().isEmpty()
                && cart.getItems().stream().allMatch(CartItem::isSelected);

        return new CartSummaryResponse(
                cart.getId(),
                cart.getCustomerId(),
                cartAllSelected,
                cart.getItems().size(),
                selectedTotalQty,
                selectedDistinct,
                selectedPrice,
                storeGroups
        );
    }

    private TransactionListCardDto toListCardDto(Transaction tx) {
        List<TransactionItemDto> itemDtos = tx.getItems().stream().map(this::toItemDto).toList();
        TransactionItemDto primary = itemDtos.isEmpty() ? null : itemDtos.get(0);
        int extra = Math.max(0, itemDtos.size() - 1);
        return new TransactionListCardDto(
                tx.getId(),
                tx.getInvoiceNumber(),
                tx.getTransactionType(),
                tx.getStatus(),
                formatStatusLabel(tx.getStatus()),
                tx.getPurchasedAt(),
                tx.getStoreId(),
                tx.getStoreNameSnapshot(),
                tx.getStoreBadgeSnapshot(),
                primary,
                extra,
                tx.getGrandTotal()
        );
    }

    private TransactionDetailResponse toDetailResponse(Transaction tx) {
        return new TransactionDetailResponse(
                tx.getId(),
                tx.getInvoiceNumber(),
                tx.getTransactionType(),
                tx.getStatus(),
                formatStatusLabel(tx.getStatus()),
                tx.getPurchasedAt(),
                tx.getCustomerId(),
                tx.getStoreId(),
                tx.getStoreNameSnapshot(),
                tx.getStoreBadgeSnapshot(),
                tx.getItems().stream().map(this::toItemDto).toList(),
                tx.getCourierName(),
                tx.getCourierService(),
                tx.isFreeShipping(),
                tx.getTrackingNumber(),
                tx.getShippingAddress(),
                tx.getPaymentMethod(),
                tx.getSubtotalProduct(),
                tx.getShippingCost(),
                tx.getShippingDiscount(),
                tx.getServiceFee(),
                tx.getPromoDiscount(),
                tx.getGrandTotal(),
                tx.getStatusHistories().stream()
                        .map(h -> new StatusHistoryDto(h.getId(), h.getStatus(), h.getDescription(), h.getChangedAt()))
                        .toList()
        );
    }

    private TransactionItemDto toItemDto(TransactionItem it) {
        return new TransactionItemDto(
                it.getId(),
                it.getProductId(),
                it.getProductVariantId(),
                it.getProductNameSnapshot(),
                it.getVariantLabelSnapshot(),
                it.getProductImageSnapshot(),
                it.getUnitPriceSnapshot(),
                it.getQuantity(),
                it.getSubtotal(),
                it.getNote()
        );
    }

    private String resolveShippingAddress(String overrideAddress, Customer customer) {
        if (overrideAddress != null && !overrideAddress.isBlank()) {
            return overrideAddress.trim();
        }
        if (customer.getDefaultAddress() != null && !customer.getDefaultAddress().isBlank()) {
            return customer.getDefaultAddress();
        }
        return "Alamat Utama Pelanggan, Kota Bandung";
    }

    private String generateUniqueInvoice() {
        String datePart = LocalDate.now().format(INV_DATE_FMT);
        String candidate;
        do {
            long num = ThreadLocalRandom.current().nextLong(1_000_000_000L, 9_999_999_999L);
            candidate = "INV/" + datePart + "/MPL/" + num;
        } while (txRepo.existsByInvoiceNumber(candidate));
        return candidate;
    }

    private List<String> resolveStatusFilter(String statusGroup, String specificStatus) {
        if (specificStatus != null && !specificStatus.isBlank()) {
            return List.of(specificStatus.trim().toUpperCase());
        }
        if (statusGroup == null || statusGroup.isBlank() || "ALL".equalsIgnoreCase(statusGroup)) {
            return List.of();
        }
        return switch (statusGroup.trim().toUpperCase()) {
            case "ONGOING", "BERLANGSUNG" -> List.of(
                    "MENUNGGU_PEMBAYARAN", "DIBAYAR", "DIPROSES", "DIKIRIM", "TIBA_DI_TUJUAN"
            );
            case "SUCCESS", "BERHASIL" -> List.of("SELESAI");
            case "FAILED", "TIDAK_BERHASIL" -> List.of("DIBATALKAN");
            default -> List.of(statusGroup.trim().toUpperCase());
        };
    }

    private String formatStatusLabel(String status) {
        return switch (status) {
            case "MENUNGGU_PEMBAYARAN" -> "Menunggu Pembayaran";
            case "DIBAYAR" -> "Dibayar";
            case "DIPROSES" -> "Diproses Penjual";
            case "DIKIRIM" -> "Sedang Dikirim";
            case "TIBA_DI_TUJUAN" -> "Tiba di Tujuan";
            case "SELESAI" -> "Selesai";
            case "DIBATALKAN" -> "Dibatalkan";
            default -> status;
        };
    }

    private String defaultStatusDescription(String status) {
        return switch (status) {
            case "DIBAYAR" -> "Pembayaran telah diterima";
            case "DIPROSES" -> "Pesanan sedang dikemas oleh penjual";
            case "DIKIRIM" -> "Paket telah diserahkan ke kurir pengiriman";
            case "TIBA_DI_TUJUAN" -> "Paket telah tiba di alamat tujuan";
            case "SELESAI" -> "Transaksi selesai dan dana diteruskan ke penjual";
            case "DIBATALKAN" -> "Transaksi dibatalkan dan stok dikembalikan ke katalog";
            default -> "Status diperbarui menjadi " + status;
        };
    }
}
