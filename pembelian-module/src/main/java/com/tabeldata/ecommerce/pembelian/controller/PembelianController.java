package com.tabeldata.ecommerce.pembelian.controller;

import com.tabeldata.ecommerce.common.api.ApiResponse;
import com.tabeldata.ecommerce.common.api.PageResponse;
import com.tabeldata.ecommerce.pembelian.dto.PembelianDtos.*;
import com.tabeldata.ecommerce.pembelian.service.PembelianService;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1")
@Tag(name = "Pembelian (Cart, Checkout & Transactions)")
public class PembelianController {

    private final PembelianService pembelianSvc;

    public PembelianController(PembelianService pembelianSvc) {
        this.pembelianSvc = pembelianSvc;
    }

    @GetMapping("/cart")
    public ApiResponse<CartSummaryResponse> fetchKeranjang(
            @RequestHeader("X-Customer-Id") UUID customerId
    ) {
        return ApiResponse.ok(pembelianSvc.getCustomerCart(customerId));
    }

    @PostMapping("/cart/items")
    public ApiResponse<CartSummaryResponse> tambahItemKeranjang(
            @RequestHeader("X-Customer-Id") UUID customerId,
            @Valid @RequestBody AddCartItemRequest payload
    ) {
        return ApiResponse.ok("Barang ditambahkan ke keranjang",
                pembelianSvc.addItemToCart(customerId, payload));
    }

    @PatchMapping("/cart/items/{cartItemId}")
    public ApiResponse<CartSummaryResponse> ubahItemKeranjang(
            @RequestHeader("X-Customer-Id") UUID customerId,
            @PathVariable UUID cartItemId,
            @Valid @RequestBody UpdateCartItemRequest payload
    ) {
        return ApiResponse.ok("Item keranjang diperbarui",
                pembelianSvc.modifyCartItem(customerId, cartItemId, payload));
    }

    @PatchMapping("/cart/select-all")
    public ApiResponse<CartSummaryResponse> pilihSemuaKeranjang(
            @RequestHeader("X-Customer-Id") UUID customerId,
            @RequestBody SelectAllCartRequest payload
    ) {
        return ApiResponse.ok(pembelianSvc.selectAllCartItems(customerId, payload.selected()));
    }

    @DeleteMapping("/cart/items/{cartItemId}")
    public ApiResponse<CartSummaryResponse> hapusSatuItemKeranjang(
            @RequestHeader("X-Customer-Id") UUID customerId,
            @PathVariable UUID cartItemId
    ) {
        return ApiResponse.ok("Item dihapus dari keranjang",
                pembelianSvc.removeCartItem(customerId, cartItemId));
    }

    @DeleteMapping("/cart/items")
    public ApiResponse<CartSummaryResponse> hapusItemTerpilihKeranjang(
            @RequestHeader("X-Customer-Id") UUID customerId
    ) {
        return ApiResponse.ok("Item terpilih dihapus dari keranjang",
                pembelianSvc.removeSelectedCartItems(customerId));
    }

    @PostMapping("/cart/items/{cartItemId}/move-to-wishlist")
    public ApiResponse<CartSummaryResponse> pindahkanKeWishlist(
            @RequestHeader("X-Customer-Id") UUID customerId,
            @PathVariable UUID cartItemId
    ) {
        return ApiResponse.ok("Item dipindahkan ke Wishlist",
                pembelianSvc.moveCartItemToWishlist(customerId, cartItemId));
    }

    @PostMapping("/transactions/checkout")
    @ResponseStatus(HttpStatus.CREATED)
    public ApiResponse<CheckoutResultResponse> prosesCheckoutKeranjang(
            @RequestHeader("X-Customer-Id") UUID customerId,
            @Valid @RequestBody CheckoutRequest payload
    ) {
        return ApiResponse.ok("Checkout berhasil",
                pembelianSvc.checkoutSelectedCart(customerId, payload));
    }

    @PostMapping("/transactions/direct-buy")
    @ResponseStatus(HttpStatus.CREATED)
    public ApiResponse<TransactionDetailResponse> prosesBeliLangsung(
            @RequestHeader("X-Customer-Id") UUID customerId,
            @Valid @RequestBody DirectBuyRequest payload
    ) {
        return ApiResponse.ok("Beli langsung berhasil",
                pembelianSvc.directBuy(customerId, payload));
    }

    @GetMapping("/transactions")
    public ApiResponse<PageResponse<TransactionListCardDto>> daftarTransaksiPelanggan(
            @RequestHeader("X-Customer-Id") UUID customerId,
            @RequestParam(defaultValue = "ALL") String statusGroup,
            @RequestParam(required = false) String status,
            @RequestParam(defaultValue = "ALL") String type,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate,
            @RequestParam(required = false) String q,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size
    ) {
        return ApiResponse.ok(pembelianSvc.listCustomerTransactions(
                customerId, statusGroup, status, type, startDate, endDate, q, page, size
        ));
    }

    @GetMapping("/transactions/waiting-payment-count")
    public ApiResponse<Map<String, Long>> hitungMenungguPembayaran(
            @RequestHeader("X-Customer-Id") UUID customerId
    ) {
        return ApiResponse.ok(pembelianSvc.countWaitingPaymentSummary(customerId));
    }

    @GetMapping("/transactions/{transactionId}")
    public ApiResponse<TransactionDetailResponse> detailTransaksi(
            @RequestHeader("X-Customer-Id") UUID customerId,
            @PathVariable UUID transactionId
    ) {
        return ApiResponse.ok(pembelianSvc.getCustomerTransactionDetail(customerId, transactionId));
    }

    @PatchMapping("/transactions/{transactionId}/status")
    public ApiResponse<TransactionDetailResponse> ubahStatusTransaksi(
            @PathVariable UUID transactionId,
            @Valid @RequestBody UpdateTransactionStatusRequest payload
    ) {
        return ApiResponse.ok("Status transaksi diperbarui",
                pembelianSvc.advanceTransactionStatus(transactionId, payload));
    }

    @PostMapping("/transactions/{transactionId}/buy-again")
    public ApiResponse<CartSummaryResponse> beliLagiDariTransaksi(
            @RequestHeader("X-Customer-Id") UUID customerId,
            @PathVariable UUID transactionId
    ) {
        return ApiResponse.ok("Item transaksi berhasil dimasukkan kembali ke keranjang",
                pembelianSvc.buyAgainFromTransaction(customerId, transactionId));
    }
}
