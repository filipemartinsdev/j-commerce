CREATE VIEW product_catalogue_summary_view AS
    SELECT
        sku.id AS id,
        sku.product_id AS product_id,
        sku.sku AS sku,
        sku.name AS name,
        sku.created_at AS created_at,
        sku.updated_at AS updated_at,
        st.units AS stock_count,
        origPrice.price AS original_price,
        curr.price AS current_price,
        pt.id AS current_price_type_id,
        pt.name AS current_price_type_name
    FROM product_sku sku

    JOIN product_stock st
    ON st.product_sku_id = sku.id

    JOIN product_sku_price origPrice
    ON origPrice.product_sku_id = sku.id
        AND origPrice.price_type_id = 1
        AND origPrice.start_at <= CURRENT_TIMESTAMP
        AND (origPrice.end_at > CURRENT_TIMESTAMP OR origPrice.end_at IS NULL)
        AND origPrice.is_active IS TRUE

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

    WHERE sku.is_active IS TRUE;