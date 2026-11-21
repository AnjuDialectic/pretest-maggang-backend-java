# Soal test maggang Backend engineer dengan Springboot

Berikut adalah soal/pertanyaan yang perlu dijawab oleh peserta maggang

## knowledge base

1. Apa yang anda ketahui tentang Rest API?
2. Apa yang anda ketahui tentang Server side and Client side processing?
3. Apa yang anda ketahui tentang Monolith dan Microservices, berikan contohnya?
4. Apa yang anda ketahui tentang Design pattern inversion of Control serta Dependency Injection?
5. Apa yang anda ketahui tentang Java programming dan Spring framework khususnya spring-boot?

## Design modules

Dalam suatu schenario ada requirement membuat aplikasi e-commerse seperti Tokopedia seperti berikut:

1. Catalog, pelanggan mencari product di toko
    ![catalog](imgs/catalog.png)
2. Item, bisa melihat detail informasi produk
    ![items](imgs/item.png)
3. Cart, pelanggan bisa menambahkan produk yang ingin di beli ke keranjang
    ![cart](imgs/cart.png)
4. Setelah di checkout, masuk ke list transaction
    ![list-transaction](imgs/list-transaction.png)
5. Kita juga bisa liat detail transactionya
    ![detail-transaction](imgs/detail-transaction.png)

Kemudian temen-temen buat design database, module (monolith/microservices) berdasarkan gambar atau schenario tersebut. Serta jelakan mengapa menggunakan design tersebut.

## Praktek

Berdasarkan analisa tersebut, buat project monorepo (pada repository ini) dengan menggunakan framework springboot seperti berikut specifikasinya:

- Database: `PostgreSQL 15`
- JDK version: `Oracle JDK 17 or later`
- Springboot version: `3.0.x`

terkait design system Toko, Barang, Pembelian pada ecommerse tersebut.

---

## Jawaban & Dokumentasi Pengerjaan

1. **Bagian 1 — Knowledge Base**: [docs/01-knowledge-base.md](docs/01-knowledge-base.md)
   - Jawaban lengkap 5 soal teori (REST API, Server-side vs Client-side Processing, Monolith vs Microservices beserta contoh, IoC & Dependency Injection, serta Java & Spring Boot).
2. **Bagian 2 — Design Modules & Database**: [docs/02-design-modules.md](docs/02-design-modules.md)
   - Analisis 5 referensi UI (`catalog.png`, `item.png`, `cart.png`, `list-transaction.png`, `detail-transaction.png`), ERD PostgreSQL 15 (14 tabel + strategi indexing), dan alasan pemilihan arsitektur **Modular Monolith Monorepo** dengan *Port-Adapter Boundaries*.
3. **Bagian 3 — Implementasi Monorepo Spring Boot 3.0.13**:
   - `common-module`: Envelope `ApiResponse`, `PageResponse`, `GlobalExceptionHandler`, `BaseAuditEntity`, serta kontrak lintas modul (`StorePort`, `ProductPort`).
   - `toko-module`: Manajemen profil toko (`Store`), badge (`POWER_MERCHANT_PRO`), sidebar `Etalase Toko` (3 filter sistem + etalase kustom), dan `Follow/Unfollow`.
   - `barang-module`: Katalog produk (filter & sort), detail produk + breadcrumb kategori bertingkat + matriks varian SKU 2 dimensi, reservasi stok atomik (`PESSIMISTIC_WRITE` / `SELECT ... FOR UPDATE`), serta `Wishlist`.
   - `pembelian-module`: Keranjang belanja dikelompokkan per toko (`Pilih Semua`, catatan item, pindah ke wishlist), checkout multi-toko & beli langsung secara atomik (`@Transactional`), rincian finansial lengkap (`subtotal_product`, `shipping_cost`, `shipping_discount` bebas ongkir, `service_fee`, `grand_total`), daftar & detail invoice beserta riwayat status, pembatalan dengan pengembalian stok, dan `Beli Lagi`.
   - `ecommerce-app`: Runner utama Spring Boot, konfigurasi Flyway (`V1__init_schema.sql` & `V2__seed_data.sql`), OpenAPI/Swagger UI, serta integration test (`EcommerceIntegrationTest`).

### Cara Menjalankan Aplikasi

1. **Jalankan Database PostgreSQL 15 via Docker Compose**:
   ```bash
   docker compose up -d postgres
   ```
2. **Jalankan Aplikasi Spring Boot**:
   ```bash
   ./mvnw -pl ecommerce-app spring-boot:run
   ```
   *(Flyway otomatis mengeksekusi migrasi skema `V1` dan data awal `V2` sesuai tangkapan layar soal).*
3. **Jalankan Unit & Integration Test**:
   ```bash
   ./mvnw clean test
   ```
4. **Akses Dokumentasi Interaktif OpenAPI / Swagger UI**:
   - Swagger UI: `http://localhost:8080/swagger-ui.html`
   - Default Header Simulasi Pelanggan: `X-Customer-Id: 00000000-0000-0000-0000-000000000001`

### Ringkasan Endpoint Utama

| Modul | Method & Endpoint | Deskripsi |
|---|---|---|
| **Toko** | `GET /api/v1/stores` | Pencarian & daftar toko aktif |
| **Toko** | `GET /api/v1/stores/{storeId}` | Detail profil toko (`iStorebdg`, rating, jam operasional, status `following`) |
| **Toko** | `GET /api/v1/stores/{storeId}/etalases` | Sidebar `Etalase Toko` (3 filter sistem + etalase kustom) |
| **Toko** | `POST /api/v1/stores/{storeId}/follow` | Mengikuti toko (`X-Customer-Id`) |
| **Barang** | `GET /api/v1/products` | Katalog produk (filter `storeId`, `etalaseId`, `categoryId`, `systemFilter=ALL\|SOLD\|PREORDER`, `q`, `sort`) |
| **Barang** | `GET /api/v1/products/{productId}` | Detail produk, breadcrumb kategori, galeri foto, matriks varian SKU & stok |
| **Barang** | `PATCH /api/v1/products/{productId}/variants/{variantId}/stock` | Update stok & harga varian (SKU) |
| **Barang** | `GET /api/v1/wishlists` | Daftar wishlist pelanggan |
| **Pembelian** | `GET /api/v1/cart` | Lihat keranjang dikelompokkan per toko beserta ringkasan belanja |
| **Pembelian** | `POST /api/v1/cart/items` | Tambah produk/varian ke keranjang |
| **Pembelian** | `PATCH /api/v1/cart/select-all` | Centang / lepas centang `Pilih Semua` |
| **Pembelian** | `POST /api/v1/transactions/checkout` | Checkout item terpilih secara atomik & kurangi stok |
| **Pembelian** | `POST /api/v1/transactions/direct-buy` | Beli langsung 1 SKU tanpa lewat keranjang |
| **Pembelian** | `GET /api/v1/transactions` | Daftar transaksi (filter `statusGroup=ALL\|ONGOING\|SUCCESS\|FAILED`, tanggal, keyword) |
| **Pembelian** | `GET /api/v1/transactions/{transactionId}` | Detail invoice, snapshot produk, info kurir (`BEBAS ONGKIR`), rincian pembayaran & histori status |
| **Pembelian** | `POST /api/v1/transactions/{transactionId}/buy-again` | Masukkan kembali seluruh item transaksi ke keranjang (`Beli Lagi`) |
