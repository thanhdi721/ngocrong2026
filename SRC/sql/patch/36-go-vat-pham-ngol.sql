-- =====================================================================
-- 36 — GỠ TOÀN BỘ VẬT PHẨM MANG TỪ NGOL (quay về trạng thái trước patch 35)
-- =====================================================================
-- Dùng khi client không đăng nhập được sau khi chạy patch 35.
-- Ảnh icon đã chép vào data/icon KHÔNG cần xoá (không ảnh hưởng gì).
-- Sau khi chạy: khởi động lại server rồi thử đăng nhập.
-- =====================================================================

DELETE FROM `item_template` WHERE `id` BETWEEN 2032 AND 2074;
DELETE FROM `part`          WHERE `id` BETWEEN 2099 AND 2212;

SELECT COUNT(*) AS `con_item_ngol` FROM `item_template` WHERE `id` >= 2032;
SELECT MAX(`id`) AS `item_lon_nhat` FROM `item_template`;
SELECT MAX(`id`) AS `part_lon_nhat` FROM `part`;
