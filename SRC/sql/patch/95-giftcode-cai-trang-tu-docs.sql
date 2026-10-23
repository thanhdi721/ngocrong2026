-- =====================================================================
-- 95 — GIFTCODE THỬ 8 CẢI TRANG CỦA PATCH 94 (2026-10-23)
-- =====================================================================
-- Mã: ctdocs1  (hành trang phải còn 8 ô trống). Mỗi tài khoản nhập được một lần.
-- Nhập ở NPC ADMIN Đẹp Trai -> "Nhập Giftcode".
-- count_left = 99999, KHÔNG để -1 (-1 làm mã chết ngay, xem patch 88).
--  2290  Cải trang Hắc Y Song Đao         part 2661/2662/2663  icon 20263..20295 (vật phẩm 20294, avatar 20295)
--  2291  Cải trang Vương Lâm              part 2664/2665/2666  icon 20319..20351 (vật phẩm 20350, avatar 20351)
--  2292  Cải trang Kiếm Sĩ Hồng Lôi       part 2667/2668/2669  icon 20352..20384 (vật phẩm 20383, avatar 20384)
--  2293  Cải trang Huyết Nhẫn Saiyan      part 2670/2671/2672  icon 20385..20417 (vật phẩm 20416, avatar 20417)
--  2294  Cải trang Lam Hồn Chiến Thần     part 2673/2674/2675  icon 20482..20514 (vật phẩm 20513, avatar 20514)
--  2295  Cải trang Tử Ma Chiến Thần       part 2676/2677/2678  icon 20515..20547 (vật phẩm 20546, avatar 20547)
--  2296  Cải trang Ngọc Long Chiến Thần   part 2679/2680/2681  icon 20548..20580 (vật phẩm 20579, avatar 20580)
--  2297  Cải trang Kim Lôi Chiến Thần     part 2682/2683/2684  icon 20581..20613 (vật phẩm 20612, avatar 20613)
--
-- Chạy SAU patch 94. Chạy lại vẫn an toàn. Khởi động lại server.
-- =====================================================================

DELETE FROM `giftcode` WHERE `id` = 180 OR `code` = 'ctdocs1';

INSERT INTO `giftcode` (`id`, `code`, `count_left`, `detail`, `datecreate`, `expired`) VALUES
(180, 'ctdocs1', 99999, '[{"id":2290,"quantity":1,"options":[]},{"id":2291,"quantity":1,"options":[]},{"id":2292,"quantity":1,"options":[]},{"id":2293,"quantity":1,"options":[]},{"id":2294,"quantity":1,"options":[]},{"id":2295,"quantity":1,"options":[]},{"id":2296,"quantity":1,"options":[]},{"id":2297,"quantity":1,"options":[]}]', NOW(), '2037-12-31 17:00:00');

SELECT `code`, LEFT(`detail`, 60) FROM `giftcode` WHERE `code` = 'ctdocs1';
