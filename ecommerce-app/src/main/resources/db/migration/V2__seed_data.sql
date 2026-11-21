-- =====================================================================
-- V2: Seed Data Sesuai 5 Referensi UI (imgs/catalog, item, cart, transaction)
-- =====================================================================

-- 1. Customer Default (Gunakan ID ini di header X-Customer-Id: 00000000-0000-0000-0000-000000000001)
INSERT INTO customers (id, full_name, email, phone, default_address) VALUES
('00000000-0000-0000-0000-000000000001', 'Dimas Pratama', 'dimas.pratama@example.com', '081234567890', 'Jl. Soekarno Hatta No. 104, Kota Bandung, Jawa Barat 40223');

-- 2. Stores (iStorebdg & emilab dari imgs/catalog.png dan imgs/list-transaction.png)
INSERT INTO stores (id, name, slug, logo_url, badge, city, is_online, rating_avg, review_count, avg_process_time_minutes, operating_hours, description, is_active) VALUES
('11111111-1111-1111-1111-111111111111', 'iStorebdg', 'istorebdg', 'https://images.tokopedia.net/img/istorebdg-logo.png', 'POWER_MERCHANT_PRO', 'Kota Bandung', TRUE, 5.00, 1420, 180, 'Buka 24 jam', 'Authorized & Trusted Apple Product Retailer di Kota Bandung. Garansi Resmi iBox & Internasional.', TRUE),
('22222222-2222-2222-2222-222222222222', 'emilab', 'emilab', 'https://images.tokopedia.net/img/emilab-logo.png', 'POWER_MERCHANT_PRO', 'Jakarta Barat', TRUE, 4.90, 980, 120, '08:00 - 22:00 WIB', 'Pusat aksesoris gadget dan home living original.', TRUE);

-- Follower (iStorebdg berstatus Following pada catalog.png)
INSERT INTO store_followers (id, store_id, customer_id) VALUES
('11111111-0000-0000-0000-000000000001', '11111111-1111-1111-1111-111111111111', '00000000-0000-0000-0000-000000000001');

-- 3. 10 Custom Etalase untuk iStorebdg (+ 3 filter sistem = Etalase Toko (13) di catalog.png)
INSERT INTO store_etalases (id, store_id, name, slug, display_order) VALUES
('e1000000-0000-0000-0000-000000000001', '11111111-1111-1111-1111-111111111111', 'iPhone dan Aksesoris', 'iphone-dan-aksesoris', 1),
('e1000000-0000-0000-0000-000000000002', '11111111-1111-1111-1111-111111111111', 'iPad dan Aksesoris', 'ipad-dan-aksesoris', 2),
('e1000000-0000-0000-0000-000000000003', '11111111-1111-1111-1111-111111111111', 'Watch dan Aksesoris', 'watch-dan-aksesoris', 3),
('e1000000-0000-0000-0000-000000000004', '11111111-1111-1111-1111-111111111111', 'MacBook dan Aksesoris', 'macbook-dan-aksesoris', 4),
('e1000000-0000-0000-0000-000000000005', '11111111-1111-1111-1111-111111111111', 'AirPods', 'airpods', 5),
('e1000000-0000-0000-0000-000000000006', '11111111-1111-1111-1111-111111111111', 'Gaming Gear', 'gaming-gear', 6),
('e1000000-0000-0000-0000-000000000007', '11111111-1111-1111-1111-111111111111', 'Brand Lainnya', 'brand-lainnya', 7),
('e1000000-0000-0000-0000-000000000008', '11111111-1111-1111-1111-111111111111', 'Adapter dan Kabel', 'adapter-dan-kabel', 8),
('e1000000-0000-0000-0000-000000000009', '11111111-1111-1111-1111-111111111111', 'Apple TV', 'apple-tv', 9),
('e1000000-0000-0000-0000-000000000010', '11111111-1111-1111-1111-111111111111', 'Barang Bekas', 'barang-bekas', 10);

-- 4. Hierarchy Kategori (Breadcrumb: Komputer & Laptop > Laptop > Laptop Consumer pada item.png)
INSERT INTO categories (id, parent_id, name, slug, level) VALUES
('c1000000-0000-0000-0000-000000000001', NULL, 'Komputer & Laptop', 'komputer-laptop', 1),
('c1000000-0000-0000-0000-000000000002', 'c1000000-0000-0000-0000-000000000001', 'Laptop', 'laptop', 2),
('c1000000-0000-0000-0000-000000000003', 'c1000000-0000-0000-0000-000000000002', 'Laptop Consumer', 'laptop-consumer', 3),
('c1000000-0000-0000-0000-000000000004', 'c1000000-0000-0000-0000-000000000001', 'Aksesoris Komputer & Charger', 'aksesoris-komputer-charger', 2);

-- 5. Products (Sesuai catalog.png, item.png, cart.png, detail-transaction.png)
INSERT INTO products (id, store_id, category_id, etalase_id, name, slug, condition, weight_gram, description, important_info, is_preorder, preorder_days, campaign_label, min_price, max_price, view_count, sold_count, rating_avg, review_count, is_active) VALUES
(
    'a1000000-0000-0000-0000-000000000001',
    '11111111-1111-1111-1111-111111111111',
    'c1000000-0000-0000-0000-000000000003',
    'e1000000-0000-0000-0000-000000000004',
    'Apple MacBook Pro 2023 14" 16" M2 Pro M2 Max 1TB 512GB Garansi',
    'apple-macbook-pro-2023-14-16-m2-pro-m2-max-1tb-512gb-garansi',
    'NEW',
    3000,
    'MEMBELI BERARTI SUDAH MEMBACA DESKRIPSI DAN SETUJU KETENTUAN TOKO. Tersedia varian Inter & iBox M2 Pro / M2 Max 14 inch dan 16 inch.',
    'Wajib video unboxing tanpa jeda untuk klaim garansi.',
    TRUE,
    14,
    'PreOrder',
    34999000.00,
    56999000.00,
    411,
    1,
    5.00,
    1,
    TRUE
),
(
    'a1000000-0000-0000-0000-000000000002',
    '11111111-1111-1111-1111-111111111111',
    'c1000000-0000-0000-0000-000000000004',
    'e1000000-0000-0000-0000-000000000008',
    'Apple 20W USB-C Power Adapter Charger iPhone Original Resmi iBox',
    'apple-20w-usb-c-power-adapter-charger-iphone-original-resmi-ibox',
    'NEW',
    250,
    'Apple 20W USB-C Power Adapter Original 100% Garansi Resmi TAM / iBox.',
    'Segel Greenpeel original Apple.',
    FALSE,
    NULL,
    'Apple Campaign',
    429000.00,
    429000.00,
    820,
    56,
    5.00,
    48,
    TRUE
),
(
    'a1000000-0000-0000-0000-000000000003',
    '22222222-2222-2222-2222-222222222222',
    'c1000000-0000-0000-0000-000000000004',
    NULL,
    'Krisbow Sync Ace Set 2 Pcs Gagang Pel Dengan Kepala Pel - MH01',
    'krisbow-sync-ace-set-2-pcs-gagang-pel-dengan-kepala-pel-mh01',
    'NEW',
    1500,
    'Set alat pel lantai praktis Krisbow Sync Ace lengkap dengan 2 kepala pel microfiber.',
    'Pengiriman aman menggunakan bubble wrap tebal.',
    FALSE,
    NULL,
    NULL,
    429000.00,
    429000.00,
    190,
    14,
    4.90,
    12,
    TRUE
);

-- 6. Product Images
INSERT INTO product_images (id, product_id, image_url, is_primary, display_order) VALUES
('f1000000-0000-0000-0000-000000000001', 'a1000000-0000-0000-0000-000000000001', 'https://images.tokopedia.net/img/macbook-pro-2023-main.jpg', TRUE, 1),
('f1000000-0000-0000-0000-000000000002', 'a1000000-0000-0000-0000-000000000001', 'https://images.tokopedia.net/img/macbook-pro-2023-ports.jpg', FALSE, 2),
('f1000000-0000-0000-0000-000000000003', 'a1000000-0000-0000-0000-000000000002', 'https://images.tokopedia.net/img/apple-20w-usb-c-adapter.jpg', TRUE, 1),
('f1000000-0000-0000-0000-000000000004', 'a1000000-0000-0000-0000-000000000003', 'https://images.tokopedia.net/img/krisbow-sync-ace-mh01.jpg', TRUE, 1);

-- 7. Product Variants (Matriks 2 level MacBook Pro di item.png + Default SKU Adapter & Krisbow)
INSERT INTO product_variants (id, product_id, sku, variant_group1_name, variant_group1_value, variant_group2_name, variant_group2_value, variant_label, price, stock, weight_gram, image_url, is_default, is_active) VALUES
('b1000000-0000-0000-0000-000000000001', 'a1000000-0000-0000-0000-000000000001', 'MBP23-INT-M2P-16-512-SLV', 'Pilih varian', 'Inter M2 Pro', 'Pilih ukuran', '16/512GB Silver', 'Inter M2 Pro, 16/512GB Silver', 36499000.00, 999, 3000, 'https://images.tokopedia.net/img/macbook-pro-2023-main.jpg', TRUE, TRUE),
('b1000000-0000-0000-0000-000000000002', 'a1000000-0000-0000-0000-000000000001', 'MBP23-INT-M2P-16-512-GRY', 'Pilih varian', 'Inter M2 Pro', 'Pilih ukuran', '16/512GB Grey', 'Inter M2 Pro, 16/512GB Grey', 34999000.00, 25, 3000, 'https://images.tokopedia.net/img/macbook-pro-2023-main.jpg', FALSE, TRUE),
('b1000000-0000-0000-0000-000000000003', 'a1000000-0000-0000-0000-000000000001', 'MBP23-INT-M2P-16-1TB-SLV', 'Pilih varian', 'Inter M2 Pro', 'Pilih ukuran', '16/1TB Silver', 'Inter M2 Pro, 16/1TB Silver', 39999000.00, 15, 3000, 'https://images.tokopedia.net/img/macbook-pro-2023-main.jpg', FALSE, TRUE),
('b1000000-0000-0000-0000-000000000004', 'a1000000-0000-0000-0000-000000000001', 'MBP23-IBX-M2M-32-1TB-GRY', 'Pilih varian', 'iBox M2 Max', 'Pilih ukuran', '32/1TB Grey', 'iBox M2 Max, 32/1TB Grey', 56999000.00, 10, 3000, 'https://images.tokopedia.net/img/macbook-pro-2023-main.jpg', FALSE, TRUE),
('b1000000-0000-0000-0000-000000000005', 'a1000000-0000-0000-0000-000000000002', 'APL-20W-USBC-IBOX', NULL, NULL, NULL, NULL, NULL, 429000.00, 150, 250, 'https://images.tokopedia.net/img/apple-20w-usb-c-adapter.jpg', TRUE, TRUE),
('b1000000-0000-0000-0000-000000000006', 'a1000000-0000-0000-0000-000000000003', 'KRISBOW-PEL-MH01', 'Tipe', 'MH01', NULL, NULL, 'MH01', 429000.00, 40, 1500, 'https://images.tokopedia.net/img/krisbow-sync-ace-mh01.jpg', TRUE, TRUE);

-- 8. Active Cart & 2 Cart Items (Sesuai imgs/cart.png: Rp429.000 + Rp36.499.000 = Rp36.928.000)
INSERT INTO carts (id, customer_id) VALUES
('d1000000-0000-0000-0000-000000000001', '00000000-0000-0000-0000-000000000001');

INSERT INTO cart_items (id, cart_id, store_id, product_id, product_variant_id, quantity, note, is_selected) VALUES
('d2000000-0000-0000-0000-000000000001', 'd1000000-0000-0000-0000-000000000001', '11111111-1111-1111-1111-111111111111', 'a1000000-0000-0000-0000-000000000002', 'b1000000-0000-0000-0000-000000000005', 1, NULL, TRUE),
('d2000000-0000-0000-0000-000000000002', 'd1000000-0000-0000-0000-000000000001', '11111111-1111-1111-1111-111111111111', 'a1000000-0000-0000-0000-000000000001', 'b1000000-0000-0000-0000-000000000001', 1, 'Packing kayu & asuransi', TRUE);

-- 9. Historical Transaction (Sesuai imgs/list-transaction.png & detail-transaction.png: INV/20230210/MPL/3038611559)
INSERT INTO transactions (
    id, invoice_number, customer_id, store_id, store_name_snapshot, store_badge_snapshot,
    transaction_type, status, subtotal_product, shipping_cost, shipping_discount, service_fee, promo_discount, grand_total,
    courier_name, courier_service, is_free_shipping, tracking_number, shipping_address, payment_method, purchased_at
) VALUES (
    '99999999-0000-0000-0000-000000000001',
    'INV/20230210/MPL/3038611559',
    '00000000-0000-0000-0000-000000000001',
    '22222222-2222-2222-2222-222222222222',
    'emilab',
    'POWER_MERCHANT_PRO',
    'BELANJA',
    'SELESAI',
    429000.00,
    15000.00,
    15000.00,
    2300.00,
    0.00,
    431300.00,
    'AnterAja',
    'Reguler',
    TRUE,
    '1000678492102',
    'Jl. Soekarno Hatta No. 104, Kota Bandung, Jawa Barat 40223',
    'BCA Virtual Account',
    '2023-02-10 11:56:00+07'
);

INSERT INTO transaction_items (
    id, transaction_id, product_id, product_variant_id,
    product_name_snapshot, variant_label_snapshot, product_image_snapshot,
    unit_price_snapshot, quantity, subtotal, note
) VALUES (
    '99999999-1111-1111-1111-000000000001',
    '99999999-0000-0000-0000-000000000001',
    'a1000000-0000-0000-0000-000000000003',
    'b1000000-0000-0000-0000-000000000006',
    'Krisbow Sync Ace Set 2 Pcs Gagang Pel Dengan Kepala Pel',
    'MH01',
    'https://images.tokopedia.net/img/krisbow-sync-ace-mh01.jpg',
    429000.00,
    1,
    429000.00,
    NULL
);

INSERT INTO transaction_status_histories (id, transaction_id, status, description, changed_at) VALUES
('99999999-2222-2222-2222-000000000001', '99999999-0000-0000-0000-000000000001', 'MENUNGGU_PEMBAYARAN', 'Pesanan dibuat', '2023-02-10 11:55:00+07'),
('99999999-2222-2222-2222-000000000002', '99999999-0000-0000-0000-000000000001', 'DIBAYAR', 'Pembayaran telah diverifikasi', '2023-02-10 11:56:00+07'),
('99999999-2222-2222-2222-000000000003', '99999999-0000-0000-0000-000000000001', 'DIPROSES', 'Pesanan sedang dikemas penjual', '2023-02-10 13:10:00+07'),
('99999999-2222-2222-2222-000000000004', '99999999-0000-0000-0000-000000000001', 'DIKIRIM', 'Paket diserahkan ke kurir AnterAja - Reguler', '2023-02-10 16:40:00+07'),
('99999999-2222-2222-2222-000000000005', '99999999-0000-0000-0000-000000000001', 'SELESAI', 'Pesanan telah diterima pembeli dan transaksi selesai', '2023-02-12 14:20:00+07');
