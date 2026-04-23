CREATE TABLE delivery_address (
    id              UUID PRIMARY KEY,
    user_id         UUID NOT NULL,
    zip_code        CHAR(8) NOT NULL,
    street          VARCHAR(255) NOT NULL,
    number          VARCHAR(20) NOT NULL,
    complement      VARCHAR(255),
    neighborhood    VARCHAR(100) NOT NULL,
    city            VARCHAR(100) NOT NULL,
    state           CHAR(2) NOT NULL,
    latitude        DOUBLE PRECISION,
    longitude       DOUBLE PRECISION,
    created_at      TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    is_active       BOOLEAN NOT NULL DEFAULT TRUE
);

CREATE TABLE sales_order_status (
    id      INT PRIMARY KEY,
    name    VARCHAR(255) NOT NULL
);

CREATE TABLE sales_order (
    id                  UUID PRIMARY KEY,
    user_id             UUID NOT NULL,
    status_id           INT REFERENCES sales_order_status(id),
    created_at          TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE sales_order_item (
    sales_order_id      UUID REFERENCES sales_order(id) NOT NULL,
    product_sku_id      UUID NOT NULL,
    product_sku_name    VARCHAR(50) NOT NULL,
    unit_price          DECIMAL(19, 2) NOT NULL,
    units               INT NOT NULL,
    created_at          TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    is_active           BOOLEAN NOT NULL DEFAULT TRUE,

    PRIMARY KEY (sales_order_id, product_sku_id)
);

CREATE TABLE shipping_status (
    id      INT PRIMARY KEY,
    name    VARCHAR(50) NOT NULL
);

CREATE TABLE shipping (
    id                      UUID PRIMARY KEY,
    status_id               INT REFERENCES shipping_status(id) NOT NULL,
    sales_order_id          UUID UNIQUE REFERENCES sales_order(id) NOT NULL,
    delivery_address_id     UUID REFERENCES delivery_address(id) NOT NULL,
    created_at              TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP
);
