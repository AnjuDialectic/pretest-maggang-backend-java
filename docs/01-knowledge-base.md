# Bagian 1 — Knowledge Base (Backend Java & Spring Boot)

Dokumen ini berisi jawaban lengkap atas 5 pertanyaan teori pada bagian **Knowledge Base** di [README.md](../README.md), dilengkapi konteks implementasi nyata pada Java dan Spring Boot.

---

## 1. Apa yang anda ketahui tentang REST API?

**REST (Representational State Transfer)** adalah gaya arsitektur (*architectural style*) untuk membangun layanan web yang diperkenalkan oleh Roy Fielding (2000). **REST API** (atau *RESTful API*) adalah antarmuka pemrograman aplikasi yang menerapkan prinsip-prinsip REST di atas protokol HTTP agar sistem yang berbeda (web, mobile, maupun antar-service backend) dapat saling bertukar data secara terstandarisasi—umumnya dalam format JSON.

### 6 Prinsip Utama Arsitektur REST
1. **Client-Server Separation**: Memisahkan tanggung jawab UI/pengalaman pengguna (client) dengan penyimpanan data dan logika bisnis (server), sehingga keduanya dapat berkembang dan di-deploy secara independen.
2. **Stateless**: Setiap request dari client ke server harus membawa seluruh informasi yang diperlukan untuk memproses request tersebut (misalnya token autentikasi pada header `Authorization` atau `X-Customer-Id`, parameter filter, dan payload). Server tidak menyimpan *session state* client di memori, sehingga memudahkan *horizontal scaling*.
3. **Cacheable**: Respons dari server harus secara eksplisit menyatakan apakah respons tersebut dapat di-cache (`Cache-Control`, `ETag`, `Last-Modified`) guna mengurangi latensi dan beban server pada resource yang sering dibaca (seperti katalog produk).
4. **Uniform Interface**: Antarmuka yang konsisten melalui identifikasi resource berbasis URI (`/api/v1/products/{id}`), manipulasi melalui representasi (JSON), pesan yang *self-descriptive* (HTTP method, status code, `Content-Type`), serta konsep HATEOAS bila diperlukan.
5. **Layered System**: Client tidak perlu tahu apakah ia terhubung langsung ke server aplikasi atau melewati perantara seperti *Load Balancer*, *API Gateway*, *CDN*, atau *Reverse Proxy*.
6. **Code on Demand (Opsional)**: Server dapat mengirimkan kode yang dapat dieksekusi ke client (jarang dipakai pada API JSON modern).

### Pemetaan HTTP Method, Idempotency & Status Code
| HTTP Method | Operasi CRUD | Safe? | Idempotent? | Contoh Endpoint E-Commerce |
|---|---|---|---|---|
| `GET` | Membaca resource | Ya | Ya | `GET /api/v1/products?storeId=...&sort=LATEST` |
| `POST` | Membuat resource / aksi non-idempotent | Tidak | Tidak | `POST /api/v1/transactions/checkout` |
| `PUT` | Mengganti seluruh representasi resource | Tidak | Ya | `PUT /api/v1/stores/{storeId}` |
| `PATCH` | Memperbarui sebagian atribut resource | Tidak | Tergantung | `PATCH /api/v1/cart/items/{id}` |
| `DELETE` | Menghapus resource | Tidak | Ya | `DELETE /api/v1/cart/items/{id}` |

**HTTP Status Code yang tepat** menjadi kontrak penting antara backend dan client:
- `200 OK` (sukses baca/update), `201 Created` (sukses membuat resource baru), `204 No Content` (sukses hapus tanpa body).
- `400 Bad Request` (payload/validasi input salah), `401 Unauthorized` (belum terautentikasi), `403 Forbidden` (tidak punya hak akses), `404 Not Found` (resource tidak ditemukan), `409 Conflict` (konflik state/stok/duplikasi data), `422 Unprocessable Entity` (pelanggaran aturan bisnis).
- `500 Internal Server Error` (kesalahan tak terduga di server).

### Contoh Implementasi REST API di Spring Boot 3
```java
@RestController
@RequestMapping("/api/v1/products")
public class ProductController {

    private final ProductService productService;

    public ProductController(ProductService productService) {
        this.productService = productService;
    }

    @GetMapping("/{productId}")
    public ResponseEntity<ApiResponse<ProductDetailResponse>> getProductDetail(
            @PathVariable UUID productId) {
        ProductDetailResponse detail = productService.getProductDetail(productId);
        return ResponseEntity.ok(ApiResponse.ok("Detail produk ditemukan", detail));
    }

    @PostMapping
    public ResponseEntity<ApiResponse<ProductDetailResponse>> createProduct(
            @Valid @RequestBody CreateProductRequest request) {
        ProductDetailResponse created = productService.createProduct(request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.ok("Produk berhasil dibuat", created));
    }
}
```

---

## 2. Apa yang anda ketahui tentang Server-side dan Client-side Processing?

**Server-side processing** dan **Client-side processing** merujuk pada **di mana eksekusi komputasi, validasi, rendering, dan pemrosesan data dilakukan** dalam arsitektur aplikasi web/mobile.

### Server-side Processing
Pemrosesan dijalankan di infrastruktur server (backend, misalnya aplikasi Spring Boot + PostgreSQL) sebelum hasil akhirnya dikirim kembali ke client melalui jaringan (HTML jadi atau JSON API).
- **Tanggung jawab utama**:
  - Autentikasi & otorisasi (validasi JWT/session, RBAC).
  - Eksekusi *core business logic* yang sensitif (kalkulasi harga final, validasi & pengurangan stok secara transaksional, pembuatan nomor invoice, integrasi payment gateway).
  - Akses database, enkripsi data sensitif, audit logging, dan *rate limiting*.
  - *Server-Side Rendering (SSR)* seperti Thymeleaf/JSP atau Next.js SSR untuk kebutuhan SEO dan *initial load* cepat.
- **Kelebihan**: Aman karena kode & aturan bisnis tersembunyi dari client, konsisten lintas perangkat, optimal untuk SEO (pada SSR).
- **Kekurangan**: Menambah beban CPU/memori server dan memerlukan *round-trip* jaringan untuk setiap aksi yang membutuhkan data server.

### Client-side Processing
Pemrosesan dijalankan langsung di perangkat pengguna (browser via JavaScript/TypeScript/React/Vue/Angular atau aplikasi mobile native/Flutter).
- **Tanggung jawab utama**:
  - Interaksi UI yang responsif tanpa reload halaman (*Single Page Application* / SPA): toggle checkbox `Pilih Semua` di keranjang, ganti pilihan varian (`Inter M2 Pro` × `16/512GB Silver`), filter/sort sementara di memori, animasi, dan *optimistic UI*.
  - Validasi input awal (*client-side validation*) agar pengguna mendapat umpan balik instan sebelum request dikirim ke server.
  - Manajemen state lokal (localStorage, IndexedDB, caching respons API).
- **Kelebihan**: Pengalaman pengguna sangat responsif, mengurangi beban rendering di server, hemat bandwidth setelah aset awal dimuat.
- **Kekurangan**: **Tidak boleh dipercaya untuk keamanan atau aturan bisnis kritis** karena kode client dapat dimodifikasi (via DevTools, Postman, atau intercept HTTP). Bergantung pada spesifikasi perangkat pengguna.

### Perbandingan & Sinergi pada E-Commerce
| Aspek | Client-Side Processing | Server-Side Processing |
|---|---|---|
| **Lokasi Eksekusi** | Browser / Mobile Device pengguna | Server Backend (JVM / Spring Boot) |
| **Keamanan & Kepercayaan** | *Untrusted* (mudah dimanipulasi user) | *Trusted* (*source of truth* aturan bisnis) |
| **Contoh di Fitur Cart & Checkout** | Menghitung estimasi subtotal instan saat tombol `+`/`-` diklik & validasi input angka > 0 | Mengambil harga asli dari DB, mengunci & memvalidasi stok (`PESSIMISTIC_WRITE`), menghitung biaya layanan/ongkir resmi, dan membuat invoice dalam `@Transactional` |

**Kesimpulan praktis**: Aplikasi modern menerapkan pendekatan **hybrid**—Client-side untuk kecepatan UX dan responsivitas UI, sementara Server-side menjadi *single source of truth* untuk keamanan, integritas data, dan aturan bisnis.

---

## 3. Apa yang anda ketahui tentang Monolith dan Microservices, berikan contohnya?

**Monolith** dan **Microservices** adalah dua pendekatan arsitektur dalam mengorganisasi, membangun, dan men-deploy sistem perangkat lunak.

### Arsitektur Monolith
Seluruh komponen fungsional aplikasi (UI/API layer, seluruh domain bisnis, dan data access layer) dibangun, dikemas, dan di-deploy sebagai **satu unit deployment tunggal** (misalnya satu file `.jar` / `.war` pada Spring Boot) yang umumnya berbagi satu database.

- **Varian penting — Modular Monolith**: Tetap 1 unit deployment, namun secara kode dipecah tegas menjadi modul-modul domain independen dengan batas kontrak (*interface/port*) yang jelas—menghindari *spaghetti code* (*Big Ball of Mud*) sekaligus menjaga kesederhanaan operasional.
- **Kelebihan**:
  - Pengembangan awal cepat, debugging & refactoring lintas domain mudah dilakukan di 1 IDE.
  - Mendukung transaksi **ACID** secara langsung (`@Transactional`) tanpa kompleksitas transaksi terdistribusi.
  - Operasional, monitoring, dan deployment sederhana (cukup 1 pipeline CI/CD dan 1 runtime).
- **Kekurangan**:
  - *Scaling* hanya bisa dilakukan pada seluruh aplikasi secara bersamaan (tidak bisa *scale* modul pencarian saja tanpa ikut *scale* modul transaksi).
  - Risiko *tight coupling* jika disiplin batas modul tidak dijaga; satu bug/memory leak di satu fitur dapat menjatuhkan seluruh aplikasi.
  - Waktu build & deploy semakin lambat ketika tim dan codebase membesar drastis.

### Arsitektur Microservices
Sistem dipecah menjadi sekumpulan **layanan kecil yang otonom** berdasarkan *Bounded Context* (Domain-Driven Design). Setiap service memiliki **codebase/modul sendiri**, **database sendiri (*database per service*)**, dan **dapat di-deploy serta di-scale secara independen**, berkomunikasi lewat jaringan (HTTP/REST, gRPC, atau *Message Broker* seperti Kafka/RabbitMQ).

- **Kelebihan**:
  - *Independent scaling* dan *independent deployment*: tim A bisa merilis pembaruan `barang-service` berkali-kali sehari tanpa menyentuh `pembelian-service`.
  - *Fault isolation*: gangguan pada service ulasan/notifikasi tidak melumpuhkan proses checkout utama.
  - Kebebasan teknologi (*polyglot*): service pencarian bisa memakai Elasticsearch + Go/Java, sementara transaksi tetap di Spring Boot + PostgreSQL.
- **Kekurangan**:
  - Kompleksitas operasional tinggi: butuh API Gateway, Service Discovery, Distributed Tracing (OpenTelemetry/Zipkin), Centralized Logging, Circuit Breaker (Resilience4j), dan orkestrasi container (Kubernetes).
  - Tidak ada transaksi ACID lintas database secara langsung; harus menggunakan pola **Saga** (*Choreography/Orchestration*), *Transactional Outbox*, dan *Eventual Consistency*.
  - Latensi jaringan dan risiko *partial failure* antar-service.

### Contoh Konkret (Kasus E-Commerce seperti Tokopedia)
1. **Contoh Monolith / Modular Monolith (Fase Awal / Skala Tim Kecil–Menengah)**:
   - Satu aplikasi Spring Boot (`ecommerce-app.jar`) yang terdiri dari 3 modul internal: `toko-module`, `barang-module`, dan `pembelian-module` terhubung ke PostgreSQL 15.
   - Saat customer klik **Beli (2)** pada keranjang `iStorebdg`, `CheckoutService` langsung memanggil `ProductPort.reserveStock(...)` dalam satu `@Transactional` PostgreSQL. Jika stok MacBook habis saat item ke-2 diproses, seluruh perubahan otomatis di-*rollback* dengan konsistensi kuat (*strong consistency*).
   - Contoh nyata di industri: **Shopify** (terkenal sukses menjalankan *Modular Monolith* skala sangat besar sebelum memecah hanya bagian tertentu) dan **Stack Overflow**.
2. **Contoh Microservices (Fase Hyper-Scale / Banyak Squad Independen)**:
   - Sistem dipecah menjadi service terpisah:
     - `store-service` (port 8081, DB Toko) — profil toko, etalase, follower.
     - `catalog-service` (port 8082, DB Produk + Elasticsearch) — katalog, pencarian cepat, varian, stok; di-*scale* hingga puluhan replica saat event *Flash Sale 11.11*.
     - `order-service` (port 8083, DB Transaksi) — keranjang, checkout, invoice.
     - `payment-service` (port 8084, DB Pembayaran) & `shipping-service` (port 8085, integrasi kurir AnterAja/JNE).
   - Saat checkout, `order-service` mereservasi stok ke `catalog-service`, lalu menerbitkan event `OrderCreated` ke Apache Kafka untuk diproses oleh `payment-service` dan `notification-service` secara asinkron (*Saga Pattern*).
   - Contoh nyata di industri: **Tokopedia**, **Netflix**, **Uber**, dan **Amazon**.

---

## 4. Apa yang anda ketahui tentang Design Pattern Inversion of Control (IoC) serta Dependency Injection (DI)?

### Inversion of Control (IoC)
**Inversion of Control (IoC)** adalah prinsip desain perangkat lunak di mana **kendali atas pembuatan objek, siklus hidup (*lifecycle*), dan aliran eksekusi dibalik (*inverted*)** dari kode aplikasi kita ke sebuah wadah/framework eksternal (*IoC Container* — pada Spring disebut `ApplicationContext`).

- **Tanpa IoC**: Class kita sendiri yang bertanggung jawab membuat (`new`) dan merangkai seluruh dependensinya → menyebabkan *tight coupling*, sulit di-unit-test, dan sulit mengganti implementasi.
- **Dengan IoC**: Kita hanya mendeklarasikan dependensi yang dibutuhkan; **Spring IoC Container** yang menginstansiasi *Bean*, mengelola scope (`singleton`, `prototype`, `request`), memanggil lifecycle hook (`@PostConstruct`, `@PreDestroy`), serta menyuntikkan dependensi yang tepat.

### Dependency Injection (DI)
**Dependency Injection (DI)** adalah **pola desain (*design pattern*) sekaligus mekanisme konkret untuk mengimplementasikan IoC**, di mana objek menerima dependensinya dari luar alih-alih membuatnya sendiri di dalam class.

Hubungan keduanya: **IoC adalah prinsip besarnya (*the principle*)**, sedangkan **DI adalah implementasi spesifiknya (*the pattern*)** dalam mengelola dependensi antar objek.

### 3 Cara Dependency Injection di Spring
1. **Constructor Injection (Sangat Direkomendasikan)**: Dependensi disuntikkan melalui constructor. Mendukung field `final` (*immutable*), menjamin objek selalu berada dalam state valid saat dibuat, mendeteksi *circular dependency* lebih awal, dan sangat mudah di-test tanpa Spring context (cukup `new Service(mockRepo)`).
2. **Setter Injection**: Dependensi disuntikkan via method setter (`@Autowired`). Cocok hanya untuk dependensi opsional.
3. **Field Injection (`@Autowired` langsung di field)**: Tidak direkomendasikan di production karena menyembunyikan dependensi, melanggar prinsip *immutability*, dan menyulitkan unit testing murni tanpa refleksi.

### Contoh Perbandingan Kode di Java / Spring Boot

**Tanpa IoC & DI (Tightly Coupled — sulit diganti & di-test):**
```java
public class CheckoutService {
    // Class ini terikat erat pada implementasi konkret
    private final PostgresProductRepository productRepository = new PostgresProductRepository();

    public void checkout(UUID variantId, int qty) {
        productRepository.decrementStock(variantId, qty);
    }
}
```

**Dengan IoC & Constructor Dependency Injection di Spring Boot (Loosely Coupled):**
```java
// 1. Kontrak abstraksi (interface)
public interface ProductPort {
    void reserveStock(List<StockAdjustment> adjustments);
}

// 2. Implementasi dikelola oleh Spring IoC Container sebagai Bean
@Component
public class ProductPortAdapter implements ProductPort {
    private final ProductVariantRepository variantRepository;

    public ProductPortAdapter(ProductVariantRepository variantRepository) {
        this.variantRepository = variantRepository;
    }

    @Override
    public void reserveStock(List<StockAdjustment> adjustments) {
        // Implementasi kunci stok & pengurangan stok
    }
}

// 3. Consumer hanya bergantung pada interface; Spring menyuntikkan implementasinya
@Service
public class CheckoutService {
    private final ProductPort productPort;

    // Constructor Injection (otomatis di-inject Spring jika hanya 1 constructor)
    public CheckoutService(ProductPort productPort) {
        this.productPort = productPort;
    }

    @Transactional
    public void processCheckout(List<StockAdjustment> items) {
        productPort.reserveStock(items);
    }
}
```
Dengan pola ini, saat membuat unit test dengan Mockito, kita cukup menyuntikkan mock `ProductPort` lewat constructor tanpa perlu menyalakan database maupun Spring Context.

---

## 5. Apa yang anda ketahui tentang Java Programming dan Spring Framework khususnya Spring Boot?

### Java Programming Language
**Java** adalah bahasa pemrograman berorientasi objek (*Object-Oriented*), *statically-typed*, dan *compiled-to-bytecode* yang berjalan di atas **JVM (Java Virtual Machine)** dengan prinsip *"Write Once, Run Anywhere"*.
- **Keunggulan utama untuk Backend Enterprise**:
  - **Strong Typing & Safety**: Kesalahan tipe data terdeteksi sejak *compile time*, memudahkan pemeliharaan codebase berskala besar.
  - **Automatic Memory Management**: *Garbage Collector* modern (G1GC, ZGC) mengelola alokasi dan pembersihan memori secara otomatis.
  - **Concurrency & Performa Tinggi**: JIT Compiler (*Just-In-Time*) mengoptimalkan eksekusi *hotspot* hingga mendekati performa native; dukungan multithreading matang (hingga *Virtual Threads* di Java modern).
  - **Fitur Modern Java 17 LTS** (basis minimal Spring Boot 3): `record` (DTO ringkas & immutable), *Sealed Classes*, *Pattern Matching for `instanceof`*, *Switch Expressions*, *Text Blocks*, serta *Stream API* & `Optional`.
  - Ekosistem pustaka dan komunitas enterprise yang sangat matang (Maven/Gradle, Hibernate, Flyway, Kafka client, JUnit 5, Mockito).

### Spring Framework
**Spring Framework** adalah framework aplikasi enterprise paling populer di ekosistem Java yang dirancang untuk membangun aplikasi modular, *loosely coupled*, dan *testable*.
- **Fondasi inti Spring Framework**:
  - **Core Container (IoC & DI)**: Mengelola bean dan dependensi melalui `ApplicationContext`.
  - **AOP (Aspect-Oriented Programming)**: Memisahkan *cross-cutting concerns* seperti manajemen transaksi (`@Transactional`), logging, dan security dari logika bisnis inti.
  - **Spring Web MVC / WebFlux**: Membangun REST API berbasis servlet (blocking) maupun reaktif (non-blocking).
  - **Spring Data**: Abstraksi akses data yang memangkas boilerplate DAO/JDBC/JPA (`JpaRepository`).
  - **Spring Security**: Autentikasi & otorisasi komprehensif (JWT, OAuth2, RBAC).

### Spring Boot (Khususnya Spring Boot 3.0.x)
Sebelum Spring Boot hadir, konfigurasi Spring tradisional memerlukan banyak XML/Java config manual, pengelolaan versi dependency yang rawan konflik, serta deployment `.war` ke server eksternal (Tomcat/JBoss). **Spring Boot** hadir dengan filosofi **Convention over Configuration** untuk membangun aplikasi Spring *production-ready* secara cepat.

- **Fitur Utama Spring Boot**:
  1. **Auto-Configuration (`@SpringBootApplication`)**: Secara otomatis mengonfigurasi bean (DataSource, EntityManager, DispatcherServlet, Jackson, Flyway) berdasarkan library yang ada di classpath dan properti di `application.yml`.
  2. **Starter Dependencies**: Paket dependency terkurasi yang kompatibel satu sama lain (mis. `spring-boot-starter-web`, `spring-boot-starter-data-jpa`, `spring-boot-starter-validation`, `spring-boot-starter-test`).
  3. **Embedded Server**: Mengemas server (Tomcat/Jetty/Undertow) langsung di dalam *executable JAR* (`java -jar app.jar`), sangat ideal untuk container/Docker dan cloud deployment.
  4. **Production-Ready Features (`Spring Boot Actuator`)**: Endpoint monitoring bawaan seperti `/actuator/health`, `/actuator/metrics`, dan `/actuator/info`.
  5. **Externalized Configuration & Profiles**: Konfigurasi fleksibel per environment (`application.yml`, `application-prod.yml`, `application-test.yml`, serta environment variables).
- **Pembaruan Penting di Spring Boot 3.0.x**:
  - Mensyaratkan minimal **Java 17** dan berbasis **Spring Framework 6**.
  - Migrasi namespace dari `javax.*` ke **`jakarta.*`** (Jakarta EE 9/10 — misalnya `jakarta.persistence.*` dan `jakarta.validation.*`).
  - Dukungan bawaan *Observability* (Micrometer Tracing) serta kesiapan kompilasi *Ahead-of-Time (AOT)* / GraalVM Native Image.
