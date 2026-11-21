package com.tabeldata.ecommerce;

import com.tabeldata.ecommerce.barang.dto.ProductDtos.ProductDetailResponse;
import com.tabeldata.ecommerce.barang.service.ProductService;
import com.tabeldata.ecommerce.common.exception.InsufficientStockException;
import com.tabeldata.ecommerce.pembelian.dto.PembelianDtos.*;
import com.tabeldata.ecommerce.pembelian.service.PembelianService;
import com.tabeldata.ecommerce.toko.dto.StoreResponses.StoreEtalaseSidebarResponse;
import com.tabeldata.ecommerce.toko.dto.StoreResponses.StoreResponse;
import com.tabeldata.ecommerce.toko.service.StoreService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@TestPropertySource(properties = {
        "spring.datasource.url=jdbc:h2:mem:ecommerce_test;MODE=PostgreSQL;DATABASE_TO_LOWER=TRUE;DEFAULT_NULL_ORDERING=HIGH",
        "spring.datasource.driver-class-name=org.h2.Driver",
        "spring.datasource.username=sa",
        "spring.datasource.password=",
        "spring.jpa.hibernate.ddl-auto=validate",
        "spring.flyway.enabled=true"
})
@Transactional
class EcommerceIntegrationTest {

    private static final UUID CUSTOMER_ID = UUID.fromString("00000000-0000-0000-0000-000000000001");
    private static final UUID ISTORE_ID = UUID.fromString("11111111-1111-1111-1111-111111111111");
    private static final UUID MACBOOK_PRODUCT_ID = UUID.fromString("a1000000-0000-0000-0000-000000000001");
    private static final UUID MACBOOK_VARIANT_ID = UUID.fromString("b1000000-0000-0000-0000-000000000001");
    private static final UUID ADAPTER_VARIANT_ID = UUID.fromString("b1000000-0000-0000-0000-000000000005");
    private static final UUID SEEDED_TX_ID = UUID.fromString("99999999-0000-0000-0000-000000000001");

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private StoreService storeService;

    @Autowired
    private ProductService productService;

    @Autowired
    private PembelianService pembelianService;

    @Test
    void shouldVerifySeededStoreProductAndHistoricalTransaction() {
        StoreResponse store = storeService.getStore(ISTORE_ID, CUSTOMER_ID);
        assertThat(store.name()).isEqualTo("iStorebdg");
        assertThat(store.following()).isTrue();

        StoreEtalaseSidebarResponse sidebar = storeService.listEtalases(ISTORE_ID);
        assertThat(sidebar.totalEtalaseCount()).isEqualTo(13);

        ProductDetailResponse macbook = productService.viewProductDetail(MACBOOK_PRODUCT_ID, CUSTOMER_ID);
        assertThat(macbook.categoryBreadcrumbs()).hasSize(3);
        assertThat(macbook.variants()).hasSize(4);

        TransactionDetailResponse seededTx = pembelianService.getCustomerTransactionDetail(CUSTOMER_ID, SEEDED_TX_ID);
        assertThat(seededTx.invoiceNumber()).isEqualTo("INV/20230210/MPL/3038611559");
        assertThat(seededTx.grandTotal()).isEqualByComparingTo("431300.00");
    }

    @Test
    void shouldCheckoutCartAtomicallyDeductStockAndRestoreOnCancel() {
        CartSummaryResponse cartBefore = pembelianService.getCustomerCart(CUSTOMER_ID);
        assertThat(cartBefore.selectedDistinctItems()).isEqualTo(2);
        assertThat(cartBefore.selectedTotalPrice()).isEqualByComparingTo("36928000.00");

        CheckoutResultResponse checkout = pembelianService.checkoutSelectedCart(
                CUSTOMER_ID,
                new CheckoutRequest("AnterAja", "Reguler", true,
                        new BigDecimal("15000.00"), new BigDecimal("2300.00"),
                        BigDecimal.ZERO, null, "BCA Virtual Account", true)
        );

        assertThat(checkout.invoiceCount()).isEqualTo(1);
        assertThat(checkout.combinedGrandTotal()).isEqualByComparingTo("36930300.00");

        assertThat(productService.getVariantSnapshot(MACBOOK_VARIANT_ID).stock()).isEqualTo(998);
        assertThat(productService.getVariantSnapshot(ADAPTER_VARIANT_ID).stock()).isEqualTo(149);
        assertThat(pembelianService.getCustomerCart(CUSTOMER_ID).totalDistinctItems()).isZero();

        UUID createdTxId = checkout.transactions().get(0).id();
        TransactionDetailResponse cancelled = pembelianService.advanceTransactionStatus(
                createdTxId,
                new UpdateTransactionStatusRequest("DIBATALKAN", null, "Batal oleh pelanggan")
        );
        assertThat(cancelled.status()).isEqualTo("DIBATALKAN");
        assertThat(productService.getVariantSnapshot(MACBOOK_VARIANT_ID).stock()).isEqualTo(999);
        assertThat(productService.getVariantSnapshot(ADAPTER_VARIANT_ID).stock()).isEqualTo(150);
    }

    @Test
    void shouldRejectDirectBuyWhenStockIsInsufficient() {
        assertThatThrownBy(() -> pembelianService.directBuy(
                CUSTOMER_ID,
                new DirectBuyRequest(
                        MACBOOK_VARIANT_ID, 2000, null,
                        "JNE", "YES", false,
                        new BigDecimal("25000.00"), new BigDecimal("2300.00"),
                        BigDecimal.ZERO, "Kota Bandung", "Gopay", true
                )
        )).isInstanceOf(InsufficientStockException.class);
    }

    @Test
    void shouldExposeRestEndpointsViaMockMvc() throws Exception {
        mockMvc.perform(get("/api/v1/stores/{id}", ISTORE_ID)
                        .header("X-Customer-Id", CUSTOMER_ID.toString()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.name").value("iStorebdg"))
                .andExpect(jsonPath("$.data.following").value(true));

        mockMvc.perform(get("/api/v1/products").param("storeId", ISTORE_ID.toString()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.totalElements").value(2));

        mockMvc.perform(get("/api/v1/cart")
                        .header("X-Customer-Id", CUSTOMER_ID.toString()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.selectedDistinctItems").value(2))
                .andExpect(jsonPath("$.data.selectedTotalPrice").value(36928000.00));

        mockMvc.perform(get("/api/v1/transactions/{id}", SEEDED_TX_ID)
                        .header("X-Customer-Id", CUSTOMER_ID.toString()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.invoiceNumber").value("INV/20230210/MPL/3038611559"))
                .andExpect(jsonPath("$.data.grandTotal").value(431300.00));
    }
}
