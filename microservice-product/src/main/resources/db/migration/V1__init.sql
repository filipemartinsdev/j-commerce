CREATE TABLE product_category (
    id          SERIAL PRIMARY KEY,
    name        VARCHAR(50) NOT NULL,
    description VARCHAR(255),
    is_active   BOOLEAN NOT NULL DEFAULT TRUE
);

CREATE TABLE product (
    id          UUID PRIMARY KEY,
    name        VARCHAR(255) NOT NULL,
    description TEXT,
    category_id INT REFERENCES product_category(id) NOT NULL,
    created_at  TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at  TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    is_active   BOOLEAN NOT NULL DEFAULT TRUE
);

CREATE INDEX product_category_id_active_idx
    ON product(category_id)
    WHERE is_active IS TRUE;

CREATE TABLE product_sku (
    id          UUID PRIMARY KEY,
    product_id  UUID REFERENCES product(id) NOT NULL,
    sku         VARCHAR(50) NOT NULL,
    name        VARCHAR(255) NOT NULL,
    created_at  TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at  TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    is_active   BOOLEAN NOT NULL DEFAULT TRUE
);

CREATE UNIQUE INDEX product_sku_active_uk
    ON product_sku(sku)
    WHERE is_active IS TRUE;

CREATE TABLE product_price_type (
    id          INT PRIMARY KEY,
    name        VARCHAR(50) UNIQUE NOT NULL
);

CREATE TABLE product_sku_price (
    id              UUID PRIMARY KEY,
    product_sku_id  UUID REFERENCES product_sku(id) NOT NULL,
    price           DECIMAL(19, 2) NOT NULL,
    price_type_id   INT REFERENCES product_price_type(id) NOT NULL,
    start_at        TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    end_at          TIMESTAMP WITH TIME ZONE DEFAULT NULL,
    created_at      TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    is_active       BOOLEAN NOT NULL DEFAULT TRUE
);

CREATE INDEX product_sku_price_lookup
    ON product_sku_price (product_sku_id, price_type_id DESC)
    WHERE is_active IS TRUE;

CREATE TABLE product_stock (
    id              UUID PRIMARY KEY,
    product_sku_id  UUID REFERENCES product_sku(id) UNIQUE NOT NULL,
    units           INT NOT NULL DEFAULT 0,
    created_at      TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    created_by      UUID NOT NULL,
    updated_at      TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    is_active       BOOLEAN NOT NULL DEFAULT TRUE
);

CREATE INDEX product_stock_product_sku_id_idx
    ON product_stock(product_sku_id);

CREATE TABLE stock_movement_type (
    id      INT PRIMARY KEY,
    name    VARCHAR(50) NOT NULL
);

CREATE TABLE stock_movement (
    id              UUID PRIMARY KEY,
    product_sku_id  UUID REFERENCES product_sku(id) NOT NULL,
    units           INT NOT NULL,
    type_id         INT REFERENCES stock_movement_type(id) NOT NULL,
    created_at      TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    created_by      UUID NOT NULL
);

CREATE INDEX stock_movement_product_sku_id_idx
    ON stock_movement(product_sku_id);

CREATE INDEX stock_movement_type_idx
    ON stock_movement(type_id);

CREATE TABLE shopping_cart_item (
    user_id         UUID NOT NULL,
    product_sku_id  UUID REFERENCES product_sku(id) NOT NULL,
    units           INT NOT NULL DEFAULT 1,
    created_at      TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    is_active       BOOLEAN NOT NULL DEFAULT TRUE,

    PRIMARY KEY (user_id, product_sku_id)
);

CREATE TABLE wishlist_item (
    user_id         UUID NOT NULL,
    product_sku_id  UUID REFERENCES product_sku(id) NOT NULL,
    units           INT NOT NULL DEFAULT 1,
    created_at      TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    is_active       BOOLEAN NOT NULL DEFAULT TRUE,

    PRIMARY KEY (user_id, product_sku_id)
);


