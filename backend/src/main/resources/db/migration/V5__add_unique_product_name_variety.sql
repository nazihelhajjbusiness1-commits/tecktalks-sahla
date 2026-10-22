CREATE UNIQUE INDEX uk_products_name_variety
    ON products (LOWER(name), LOWER(variety));
