
USE laptop_store;

-- 1. Them tags phuc vu AI tu van
ALTER TABLE laptops
    ADD COLUMN tags TEXT;

-- 2. Khong cho phep ton kho NULL
ALTER TABLE laptops
    MODIFY COLUMN quantity INT NOT NULL DEFAULT 0;

-- 3. Rang buoc gia va ton kho khong am
ALTER TABLE laptops
    ADD CONSTRAINT chk_laptop_price
        CHECK (price >= 0);

ALTER TABLE laptops
    ADD CONSTRAINT chk_laptop_quantity
        CHECK (quantity >= 0);

-- 4. Cap nhat rang buoc lich su kho
ALTER TABLE stock_movements
    MODIFY COLUMN laptop_id INT NOT NULL,
    MODIFY COLUMN movement_type VARCHAR(20) NOT NULL;

ALTER TABLE stock_movements
    ADD CONSTRAINT chk_movement_quantity
        CHECK (quantity > 0);

ALTER TABLE stock_movements
    ADD CONSTRAINT chk_movement_type
        CHECK (movement_type IN ('IMPORT', 'EXPORT'));

-- 5. Doi CASCADE thanh RESTRICT
ALTER TABLE stock_movements
DROP FOREIGN KEY stock_movements_ibfk_1,
ADD CONSTRAINT fk_stock_laptop
FOREIGN KEY (laptop_id)
REFERENCES laptops(laptop_id)
ON DELETE RESTRICT;
