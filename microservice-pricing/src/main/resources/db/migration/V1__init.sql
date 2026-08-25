CREATE TABLE product (
    id          UUID PRIMARY KEY,
    sku         VARCHAR(255) NOT NULL UNIQUE,
    deleted     BOOLEAN NOT NULL DEFAULT FALSE
);

CREATE INDEX product_sku_idx
    ON product(sku)
    WHERE deleted IS FALSE;

CREATE TABLE price (
    id              UUID PRIMARY KEY,
    product_id      UUID NOT NULL REFERENCES product(id),
    type_id         INTEGER NOT NULL DEFAULT 1,
    value           DECIMAL(19, 2) NOT NULL,
    since           TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    until           TIMESTAMP WITH TIME ZONE,
    active          BOOLEAN NOT NULL DEFAULT FALSE,
    created_at      TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    created_by      UUID NOT NULL,
    deleted         BOOLEAN NOT NULL DEFAULT FALSE
);

CREATE INDEX price_product_id_idx
    ON price(product_id)
    WHERE deleted IS FALSE;
