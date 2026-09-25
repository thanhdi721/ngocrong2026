-- =====================================================================
-- 76 — GIFTCODE ĐÁ PHÁP SƯ ĐỂ THỬ (2026-09-25)
-- =====================================================================
-- Chạy SAU patch 75. Mã 7 ký tự, số lượt không giới hạn, mỗi tài khoản một lần.
--   daps001 : 200 Đá Pháp Sư  (đủ 10 lần nâng)
--   daps002 : 200 Đá Pháp Sư
--   datay01 :  50 Đá Tẩy Pháp Sư (đủ 10 lần tẩy)
-- Đá gộp chồng nên mỗi mã chỉ tốn 1 ô hành trang.
-- =====================================================================

DELETE FROM `giftcode` WHERE `code` IN ('daps001', 'daps002', 'datay01');

INSERT INTO `giftcode` (`id`, `code`, `count_left`, `detail`, `datecreate`, `expired`) VALUES
(167, 'daps001', -1, '[{"id":2262,"quantity":200,"options":[]}]', NOW(), '2037-12-31 17:00:00'),
(168, 'daps002', -1, '[{"id":2262,"quantity":200,"options":[]}]', NOW(), '2037-12-31 17:00:00'),
(169, 'datay01', -1, '[{"id":2263,"quantity":50,"options":[]}]', NOW(), '2037-12-31 17:00:00');

SELECT `code`, `detail` FROM `giftcode` WHERE `code` LIKE 'da%';
