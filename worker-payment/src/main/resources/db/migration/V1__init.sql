CREATE TABLE payment_status (
    id      INTEGER PRIMARY KEY,
    name    VARCHAR(25) NOT NULL UNIQUE
);

CREATE TABLE payment (
    id              UUID PRIMARY KEY,
    provider_id     VARCHAR(255) UNIQUE,
    user_id         UUID NOT NULL,
    sales_order_id  UUID NOT NULL,
    status_id       INTEGER REFERENCES payment_status(id) NOT NULL,
    amount          DECIMAL(19, 2) NOT NULL,
    created_at      TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at      TIMESTAMP WITH TIME ZONE
);

CREATE INDEX payment_user_id_idx
    ON payment(user_id);

CREATE INDEX payment_provider_id_idx
    ON payment(provider_id);

CREATE INDEX payment_sales_order_id_idx
    ON payment(sales_order_id);