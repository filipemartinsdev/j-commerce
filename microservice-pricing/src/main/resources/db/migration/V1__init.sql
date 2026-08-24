CREATE TABLE sku_mirror (
    id          UUID PRIMARY KEY,
    sku         VARCHAR(255) NOT NULL UNIQUE,
    deleted     BOOLEAN NOT NULL DEFAULT FALSE
);

CREATE INDEX sku_mirror_sku_idx
    ON sku_mirror(sku)
    WHERE deleted IS FALSE;

CREATE TABLE price (
    id              UUID PRIMARY KEY,
    sku_mirror_id   UUID NOT NULL REFERENCES sku_mirror(id),
    value           DECIMAL(19, 2) NOT NULL,
    since           TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    until           TIMESTAMP WITH TIME ZONE,
    active          BOOLEAN NOT NULL DEFAULT FALSE,
    type            VARCHAR(50) NOT NULL DEFAULT 'COMMON',
    created_at      TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    created_by      UUID NOT NULL,
    deleted         BOOLEAN NOT NULL DEFAULT FALSE
);

CREATE INDEX price_sku_mirror_id_idx
    ON price(sku_mirror_id)
    WHERE deleted IS FALSE;
