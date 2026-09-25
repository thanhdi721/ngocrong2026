-- =====================================================================
-- 52 — SỬA ICON CẢI TRANG "GOKU NỔI LOẠN" (2026-09-23)
-- =====================================================================
-- Bên HUNR, icon của món này trỏ nhầm vào MỘT MẢNH THÂN (hình tay áo trắng) chứ
-- không phải chân dung, nên trong hành trang hiện ra cái tay áo.
-- Đổi sang mảnh ĐẦU của chính bộ đó, giống 29 bộ còn lại.
-- Patch 48 đã sửa theo. Chạy cùng jar có DataGame.vsItem tăng. Chạy lại vẫn an toàn.
-- =====================================================================

UPDATE `item_template` SET `icon_id` = 17342 WHERE `id` = 2103;

SELECT `id`, `NAME`, `icon_id` FROM `item_template` WHERE `id` = 2103;
