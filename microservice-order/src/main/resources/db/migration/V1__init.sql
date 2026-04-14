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
    is_active       BOOLEAN NOT NULL
);

CREATE TABLE order_status (
    id      INT PRIMARY KEY,
    name    VARCHAR(255) NOT NULL
);

CREATE TABLE sale_order (
    id                  UUID PRIMARY KEY,
    status_id           INT REFERENCES order_status(id),
    delivery_address_id UUID REFERENCES delivery_address(id),
    user_id             UUID NOT NULL,
    created_at          TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    is_active           BOOLEAN NOT NULL
);

CREATE TABLE order_item (
    order_id        UUID REFERENCES sale_order(id) NOT NULL,
    product_sku_id  UUID NOT NULL,
    price           DECIMAL(19, 2) NOT NULL,
    units           INT NOT NULL,
    created_at      TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    is_active       BOOLEAN NOT NULL,

    PRIMARY KEY (order_id, product_sku_id)
);
