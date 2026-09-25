-- =====================================================================
-- 36 — GỠ TOÀN BỘ VẬT PHẨM MANG TỪ NGOL (quay về trước patch 35)
-- =====================================================================
DELETE FROM `item_template` WHERE `id` BETWEEN 2032 AND 2074;
DELETE FROM `part`          WHERE `id` BETWEEN 2099 AND 2212;
DELETE FROM `head_avatar`   WHERE `head_id` BETWEEN 2099 AND 2212;

SELECT MAX(`id`) AS `item_lon_nhat` FROM `item_template`;
SELECT MAX(`id`) AS `part_lon_nhat` FROM `part`;
