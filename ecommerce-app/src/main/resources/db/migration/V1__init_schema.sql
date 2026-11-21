-- =====================================================================
-- V1: Schema Database Sistem E-Commerce (Toko, Barang, Pembelian)
-- Target Engine: PostgreSQL 15
-- =====================================================================

-- 1. Domain Toko
CREATE TABLE stores (
    id                       UUID PRIMARY KEY,
    name                     VARCHAR(120) NOT NULL,
    slug                     VARCHAR(150) NOT NULL UNIQUE,
    logo_url                 VARCHAR(500),
    badge                    VARCHAR(30)  NOT NULL DEFAULT 'REGULAR',
    city                     VARCHAR(100) NOT NULL,
    is_online                BOOLEAN      NOT NULL DEFAULT TRUE,
    rating_avg               NUMERIC(3,2) NOT NULL DEFAULT 0.00,
    review_count             INTEGER      NOT NULL DEFAULT 0,
    avg_process_time_minutes INTEGER      NOT NULL DEFAULT 180,
    operating_hours          VARCHAR(100) NOT NULL DEFAULT '09:00 - 21:00 WIB',
    description              TEXT,
    is_active                BOOLEAN      NOT NULL DEFAULT TRUE,
    created_at               TIMESTAMP WITH TIME ZONE  NOT NULL DEFAULT NOW(),
    updated_at               TIMESTAMP WITH TIME ZONE  NOT NULL DEFAULT NOW()
);

CREATE TABLE store_etalases (
    id            UUID PRIMARY KEY,
    store_id      UUID         NOT NULL REFERENCES stores(id) ON DELETE CASCADE,
    name          VARCHAR(120) NOT NULL,
    slug          VARCHAR(150) NOT NULL,
    display_order INTEGER      NOT NULL DEFAULT 0,
    created_at    TIMESTAMP WITH TIME ZONE  NOT NULL DEFAULT NOW(),
    updated_at    TIMESTAMP WITH TIME ZONE  NOT NULL DEFAULT NOW(),
    CONSTRAINT uq_store_etalase_slug UNIQUE (store_id, slug)
);

CREATE TABLE store_followers (
    id          UUID PRIMARY KEY,
    store_id    UUID        NOT NULL REFERENCES stores(id) ON DELETE CASCADE,
    customer_id UUID        NOT NULL,
    created_at  TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT NOW(),
    CONSTRAINT uq_store_follower UNIQUE (store_id, customer_id)
);

-- 2. Domain Barang
CREATE TABLE categories (
    id         UUID PRIMARY KEY,
    parent_id  UUID REFERENCES categories(id) ON DELETE SET NULL,
    name       VARCHAR(120) NOT NULL,
    slug       VARCHAR(150) NOT NULL UNIQUE,
    level      INTEGER      NOT NULL DEFAULT 1,
    created_at TIMESTAMP WITH TIME ZONE  NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMP WITH TIME ZONE  NOT NULL DEFAULT NOW()
);

CREATE TABLE products (
    id             UUID PRIMARY KEY,
    store_id       UUID          NOT NULL REFERENCES stores(id),
    category_id    UUID          REFERENCES categories(id),
    etalase_id     UUID          REFERENCES store_etalases(id) ON DELETE SET NULL,
    name           VARCHAR(200)  NOT NULL,
    slug           VARCHAR(220)  NOT NULL,
    condition      VARCHAR(20)   NOT NULL DEFAULT 'NEW',
    weight_gram    INTEGER       NOT NULL DEFAULT 1000,
    description    TEXT,
    important_info TEXT,
    is_preorder    BOOLEAN       NOT NULL DEFAULT FALSE,
    preorder_days  INTEGER,
    campaign_label VARCHAR(80),
    min_price      NUMERIC(19,2) NOT NULL DEFAULT 0.00,
    max_price      NUMERIC(19,2) NOT NULL DEFAULT 0.00,
    view_count     BIGINT        NOT NULL DEFAULT 0,
    sold_count     INTEGER       NOT NULL DEFAULT 0,
    rating_avg     NUMERIC(3,2)  NOT NULL DEFAULT 5.00,
    review_count   INTEGER       NOT NULL DEFAULT 0,
    is_active      BOOLEAN       NOT NULL DEFAULT TRUE,
    created_at     TIMESTAMP WITH TIME ZONE   NOT NULL DEFAULT NOW(),
    updated_at     TIMESTAMP WITH TIME ZONE   NOT NULL DEFAULT NOW(),
    CONSTRAINT uq_product_store_slug UNIQUE (store_id, slug)
);

CREATE TABLE product_images (
    id            UUID PRIMARY KEY,
    product_id    UUID         NOT NULL REFERENCES products(id) ON DELETE CASCADE,
    image_url     VARCHAR(500) NOT NULL,
    is_primary    BOOLEAN      NOT NULL DEFAULT FALSE,
    display_order INTEGER      NOT NULL DEFAULT 0
);

CREATE TABLE product_variants (
    id                   UUID PRIMARY KEY,
    product_id           UUID          NOT NULL REFERENCES products(id) ON DELETE CASCADE,
    sku                  VARCHAR(100)  NOT NULL UNIQUE,
    variant_group1_name  VARCHAR(60),
    variant_group1_value VARCHAR(80),
    variant_group2_name  VARCHAR(60),
    variant_group2_value VARCHAR(80),
    variant_label        VARCHAR(180),
    price                NUMERIC(19,2) NOT NULL DEFAULT 0.00,
    stock                INTEGER       NOT NULL DEFAULT 0,
    weight_gram          INTEGER,
    image_url            VARCHAR(500),
    is_default           BOOLEAN       NOT NULL DEFAULT FALSE,
    is_active            BOOLEAN       NOT NULL DEFAULT TRUE,
    created_at           TIMESTAMP WITH TIME ZONE   NOT NULL DEFAULT NOW(),
    updated_at           TIMESTAMP WITH TIME ZONE   NOT NULL DEFAULT NOW()
);

CREATE TABLE wishlist_items (
    id          UUID PRIMARY KEY,
    customer_id UUID        NOT NULL,
    product_id  UUID        NOT NULL REFERENCES products(id) ON DELETE CASCADE,
    created_at  TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT NOW(),
    CONSTRAINT uq_wishlist_customer_product UNIQUE (customer_id, product_id)
);

-- 3. Domain Pembelian
CREATE TABLE customers (
    id              UUID PRIMARY KEY,
    full_name       VARCHAR(120) NOT NULL,
    email           VARCHAR(160) NOT NULL UNIQUE,
    phone           VARCHAR(30),
    default_address TEXT,
    created_at      TIMESTAMP WITH TIME ZONE  NOT NULL DEFAULT NOW(),
    updated_at      TIMESTAMP WITH TIME ZONE  NOT NULL DEFAULT NOW()
);

CREATE TABLE carts (
    id          UUID PRIMARY KEY,
    customer_id UUID        NOT NULL UNIQUE REFERENCES customers(id) ON DELETE CASCADE,
    created_at  TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT NOW(),
    updated_at  TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT NOW()
);

CREATE TABLE cart_items (
    id                 UUID PRIMARY KEY,
    cart_id            UUID        NOT NULL REFERENCES carts(id) ON DELETE CASCADE,
    store_id           UUID        NOT NULL,
    product_id         UUID        NOT NULL,
    product_variant_id UUID        NOT NULL REFERENCES product_variants(id),
    quantity           INTEGER     NOT NULL DEFAULT 1,
    note               VARCHAR(255),
    is_selected        BOOLEAN     NOT NULL DEFAULT TRUE,
    created_at         TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT NOW(),
    updated_at         TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT NOW(),
    CONSTRAINT uq_cart_variant UNIQUE (cart_id, product_variant_id)
);

CREATE TABLE transactions (
    id                   UUID PRIMARY KEY,
    invoice_number       VARCHAR(60)   NOT NULL UNIQUE,
    customer_id          UUID          NOT NULL REFERENCES customers(id),
    store_id             UUID          NOT NULL REFERENCES stores(id),
    store_name_snapshot  VARCHAR(120)  NOT NULL,
    store_badge_snapshot VARCHAR(30)   NOT NULL DEFAULT 'REGULAR',
    transaction_type     VARCHAR(30)   NOT NULL DEFAULT 'BELANJA',
    status               VARCHAR(40)   NOT NULL DEFAULT 'MENUNGGU_PEMBAYARAN',
    subtotal_product     NUMERIC(19,2) NOT NULL DEFAULT 0.00,
    shipping_cost        NUMERIC(19,2) NOT NULL DEFAULT 0.00,
    shipping_discount    NUMERIC(19,2) NOT NULL DEFAULT 0.00,
    service_fee          NUMERIC(19,2) NOT NULL DEFAULT 0.00,
    promo_discount       NUMERIC(19,2) NOT NULL DEFAULT 0.00,
    grand_total          NUMERIC(19,2) NOT NULL DEFAULT 0.00,
    courier_name         VARCHAR(60)   NOT NULL,
    courier_service      VARCHAR(60)   NOT NULL,
    is_free_shipping     BOOLEAN       NOT NULL DEFAULT FALSE,
    tracking_number      VARCHAR(80),
    shipping_address     TEXT          NOT NULL,
    payment_method       VARCHAR(60),
    purchased_at         TIMESTAMP WITH TIME ZONE   NOT NULL DEFAULT NOW(),
    created_at           TIMESTAMP WITH TIME ZONE   NOT NULL DEFAULT NOW(),
    updated_at           TIMESTAMP WITH TIME ZONE   NOT NULL DEFAULT NOW()
);

CREATE TABLE transaction_items (
    id                     UUID PRIMARY KEY,
    transaction_id         UUID          NOT NULL REFERENCES transactions(id) ON DELETE CASCADE,
    product_id             UUID          NOT NULL,
    product_variant_id     UUID          NOT NULL,
    product_name_snapshot  VARCHAR(200)  NOT NULL,
    variant_label_snapshot VARCHAR(180),
    product_image_snapshot VARCHAR(500),
    unit_price_snapshot    NUMERIC(19,2) NOT NULL,
    quantity               INTEGER       NOT NULL,
    subtotal               NUMERIC(19,2) NOT NULL,
    note                   VARCHAR(255)
);

CREATE TABLE transaction_status_histories (
    id             UUID PRIMARY KEY,
    transaction_id UUID         NOT NULL REFERENCES transactions(id) ON DELETE CASCADE,
    status         VARCHAR(40)  NOT NULL,
    description    VARCHAR(255) NOT NULL,
    changed_at     TIMESTAMP WITH TIME ZONE  NOT NULL DEFAULT NOW()
);

-- Indexes
CREATE INDEX idx_stores_city_active ON stores (city, is_active);
CREATE INDEX idx_etalases_store_order ON store_etalases (store_id, display_order);
CREATE INDEX idx_categories_parent ON categories (parent_id);
CREATE INDEX idx_products_store_etalase_active ON products (store_id, etalase_id, is_active);
CREATE INDEX idx_products_store_created ON products (store_id, created_at DESC);
CREATE INDEX idx_products_store_sold ON products (store_id, sold_count DESC);
CREATE INDEX idx_products_category ON products (category_id);
CREATE INDEX idx_variants_product_active ON product_variants (product_id, is_active);
CREATE INDEX idx_cart_items_cart_store ON cart_items (cart_id, store_id);
CREATE INDEX idx_tx_customer_purchased ON transactions (customer_id, purchased_at DESC);
CREATE INDEX idx_tx_customer_status_date ON transactions (customer_id, status, purchased_at DESC);
CREATE INDEX idx_tx_store_status ON transactions (store_id, status);
CREATE INDEX idx_tx_items_tx ON transaction_items (transaction_id);
CREATE INDEX idx_tx_history_tx_time ON transaction_status_histories (transaction_id, changed_at DESC);
