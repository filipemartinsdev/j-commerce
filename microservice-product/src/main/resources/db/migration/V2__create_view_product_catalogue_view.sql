CREATE VIEW product_catalogue_view AS
    SELECT
        p.id AS id,
        sku.name AS name,
        p.description AS description,
        cat.id AS category_id,
        cat.name AS category_name,
        COALESCE(sku.stock_count, 0) AS stock_count,
        sku.current_price_value AS current_price_value,
        sku.original_price_value AS original_price_value,

        pt.id AS current_price_type_id,
        pt.name AS current_price_type_name

    FROM product p

    JOIN product_category cat
    ON cat.id = p.category_id

    LEFT JOIN LATERAL (
        SELECT
            s.name,
            st.units AS stock_count,
            pc.price AS current_price_value,
            pc.price_type_id,
            pc.price_type_id AS current_price_type_id,
            po.price AS original_price_value
        FROM product_sku s

        LEFT JOIN product_stock st
        ON st.product_sku_id = s.id

        JOIN product_sku_price pc
        ON pc.product_sku_id = s.id
            AND pc.is_active IS TRUE
            AND (pc.end_at IS NULL OR pc.end_at > CURRENT_TIMESTAMP)

        LEFT JOIN product_sku_price po
        ON po.product_sku_id = s.id
            AND po.price_type_id = 1
            AND po.is_active IS TRUE
            AND (po.end_at IS NULL OR po.end_at > CURRENT_TIMESTAMP)

        WHERE s.product_id = p.id
            AND s.is_active IS TRUE
            AND pc.price > 0

        ORDER BY
            (st.units > 0) DESC,
            pc.price_type_id DESC,
            s.created_at ASC
            LIMIT 1
    ) sku ON TRUE

    JOIN product_price_type pt
    ON pt.id = sku.price_type_id

    WHERE p.is_active IS TRUE;
