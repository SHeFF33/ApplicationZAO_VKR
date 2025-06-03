-- Добавляем новые колонки
ALTER TABLE sum ADD COLUMN region_name VARCHAR(255);
ALTER TABLE sum ADD COLUMN prices_on_regions_id BIGINT;

-- Добавляем внешний ключ
ALTER TABLE sum ADD CONSTRAINT fk_sum_prices_on_regions 
    FOREIGN KEY (prices_on_regions_id) 
    REFERENCES prices_on_regions(id);

-- Удаляем старый внешний ключ и колонку
ALTER TABLE sum DROP CONSTRAINT IF EXISTS fk_sum_price;
ALTER TABLE sum DROP COLUMN IF EXISTS price_id; 