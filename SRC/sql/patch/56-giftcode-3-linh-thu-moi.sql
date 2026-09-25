-- =====================================================================
-- 56 — GIFTCODE THỬ 3 LINH THÚ MỚI (2026-09-23)
-- =====================================================================
--   testct6 : 2120 Cua Bong Bóng, 2121 Trâu ngáo, 2122 Lân Đần
-- Cần 3 ô hành trang trống. Mỗi tài khoản nhập một lần, số lượt không giới hạn.
-- Chạy SAU patch 54. Cần khởi động lại server (giftcode chỉ nạp lúc khởi động).
-- =====================================================================

DELETE FROM `giftcode` WHERE `id` = 106;

INSERT INTO `giftcode` (`id`, `code`, `count_left`, `detail`, `datecreate`, `expired`) VALUES
(106, 'testct6', -1, '[{"id":2120,"quantity":1,"options":[]},{"id":2121,"quantity":1,"options":[]},{"id":2122,"quantity":1,"options":[]}]', NOW(), '2037-12-31 17:00:00');

SELECT `id`, `code`, `count_left` FROM `giftcode` WHERE `id` = 106;
