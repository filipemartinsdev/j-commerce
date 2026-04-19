CREATE VIEW shopping_cart_item_product_sku_resume AS
    SELECT
        sci.id AS id,
        sci.user_id AS user_id,
        sku.id AS product_sku_id,
        sku.name AS product_sku_name,
        curr.price AS current_price,
        orig_price.price AS original_price,
        pt.id AS price_type_id,
        pt.name AS price_type_name,
        sci.units AS units
    FROM shopping_cart_item sci

    JOIN product_sku sku
        ON sku.id = sci.product_sku_id
        AND sku.is_active IS TRUE

    JOIN product_sku_price orig_price
        ON orig_price.product_sku_id = sku.id
        AND orig_price.price_type_id = 1
        AND orig_price.start_at <= CURRENT_TIMESTAMP
        AND (orig_price.end_at > CURRENT_TIMESTAMP OR orig_price.end_at IS NULL)
        AND orig_price.is_active IS TRUE

    LEFT JOIN LATERAL (
        SELECT
            lp.price,
            lp.price_type_id
        FROM product_sku_price lp
        WHERE lp.product_sku_id = sku.id
            AND lp.price_type_id >= 2
            AND lp.start_at <= CURRENT_TIMESTAMP
            AND (lp.end_at > CURRENT_TIMESTAMP OR lp.end_at IS NULL)
            AND lp.is_active IS TRUE
        ORDER BY lp.price_type_id DESC
        LIMIT 1
    ) curr ON TRUE

    LEFT JOIN product_price_type pt
        ON pt.id = curr.price_type_id

    JOIN product p
        ON p.id = sku.product_id
        AND p.is_active IS TRUE

    WHERE sci.is_active IS TRUE;