-- =====================================================================
-- 71 — GIFTCODE THỬ 40 ĐỒ ĐEO LƯNG + 2 THÚ CƯỠI TỪ SUMO (2026-09-25)
-- =====================================================================
-- Chạy SAU patch 69 và 70. Mã đúng 7 ký tự, mỗi mã tối đa 10 món.
--
--   sumodl1 (10 món): Vòng sáng thiên thần 2, Thần Hỏa, Thần Mộc, Thần Thổ, Kiếm Vip, Cây tre trăm đốt, Hào quang Thần, Hào quang Hit, Thiên Long Đao, Cánh VIP
--   sumodl2 (10 món): Hào quang VIP, Cá xám, Đao Quan Vũ, Ngọc Thanh Long, Quạt Tiêu, Hồn Hoàn, Đao truy hồn, Dao Găm, Trượng Thiên Sứ, Hào quang kaioken
--   sumodl3 (10 món): Samehada, Quạt Gunbai, Danh Hiệu Giang Hồ, Kiếm đỏ, Kiếm xanh, Cánh thời trang 3, Cánh thời trang 7, Cánh thời trang 8, Cánh thời trang 6, Cánh thời trang 5
--   sumodl4 (10 món): Fashion Wing 2, Fashion Wing 4, Câu Hồn Ma S, Câu Hồn Ma SS, Ngũ Hành Kỳ, Linh Hồn S, Linh Hồn SS, Linh Hồn SSS, Kiếm Linh, Pháp trận Tru Tiên
--   sumotc1 ( 2 món): Kỳ Lân Bạch Kim, Phượng Hoàng Thất Sắc
-- =====================================================================

DELETE FROM `giftcode` WHERE `code` LIKE 'sumodl%' OR `code` LIKE 'sumotc%';

INSERT INTO `giftcode` (`id`, `code`, `count_left`, `detail`, `datecreate`, `expired`) VALUES
(145, 'sumodl1', -1, '[{"id":2220,"quantity":1,"options":[]},{"id":2221,"quantity":1,"options":[]},{"id":2222,"quantity":1,"options":[]},{"id":2223,"quantity":1,"options":[]},{"id":2224,"quantity":1,"options":[]},{"id":2225,"quantity":1,"options":[]},{"id":2226,"quantity":1,"options":[]},{"id":2227,"quantity":1,"options":[]},{"id":2228,"quantity":1,"options":[]},{"id":2229,"quantity":1,"options":[]}]', NOW(), '2037-12-31 17:00:00'),
(146, 'sumodl2', -1, '[{"id":2230,"quantity":1,"options":[]},{"id":2231,"quantity":1,"options":[]},{"id":2232,"quantity":1,"options":[]},{"id":2233,"quantity":1,"options":[]},{"id":2234,"quantity":1,"options":[]},{"id":2235,"quantity":1,"options":[]},{"id":2236,"quantity":1,"options":[]},{"id":2237,"quantity":1,"options":[]},{"id":2238,"quantity":1,"options":[]},{"id":2239,"quantity":1,"options":[]}]', NOW(), '2037-12-31 17:00:00'),
(147, 'sumodl3', -1, '[{"id":2240,"quantity":1,"options":[]},{"id":2241,"quantity":1,"options":[]},{"id":2242,"quantity":1,"options":[]},{"id":2243,"quantity":1,"options":[]},{"id":2244,"quantity":1,"options":[]},{"id":2245,"quantity":1,"options":[]},{"id":2246,"quantity":1,"options":[]},{"id":2247,"quantity":1,"options":[]},{"id":2248,"quantity":1,"options":[]},{"id":2249,"quantity":1,"options":[]}]', NOW(), '2037-12-31 17:00:00'),
(148, 'sumodl4', -1, '[{"id":2250,"quantity":1,"options":[]},{"id":2251,"quantity":1,"options":[]},{"id":2252,"quantity":1,"options":[]},{"id":2253,"quantity":1,"options":[]},{"id":2254,"quantity":1,"options":[]},{"id":2255,"quantity":1,"options":[]},{"id":2256,"quantity":1,"options":[]},{"id":2257,"quantity":1,"options":[]},{"id":2258,"quantity":1,"options":[]},{"id":2259,"quantity":1,"options":[]}]', NOW(), '2037-12-31 17:00:00'),
(149, 'sumotc1', -1, '[{"id":2260,"quantity":1,"options":[]},{"id":2261,"quantity":1,"options":[]}]', NOW(), '2037-12-31 17:00:00');

SELECT `code`, LEFT(`detail`,60) FROM `giftcode` WHERE `code` LIKE 'sumo%';
