-- =====================================================================
-- 24 — NV 35 BƯỚC CUỐI: ĐỔI ĐƯỜNG VÒNG SANG CÀY QUÁI VÙNG BĂNG (2026-09-20)
-- =====================================================================
-- Con đường rắn độc 7 ngày mới vào được 1 lần và mỗi lần chỉ ~40 quái, nên mốc
-- "hạ 200 quái" của đường vòng cũ (chỉ tính Dơi da xanh / Quỷ chim ở 6 map) quá nặng.
--
-- Bước "Qua rắn độc hoặc hạ 200 quái" đổi thành:
--   "Hạ 100 quái vùng Băng" — quái NÀO ở map 105–110 cũng tính.
--   Ai vào được Con đường rắn độc thì hoàn thành phó bản là xong ngay bước này.
--   Hai loài cũ (Dơi da xanh / Quỷ chim ở map 73/74/76/77/81/82) vẫn tính như trước.
--
-- Chạy cùng bản jar mới. Chạy lại nhiều lần vẫn an toàn. Khởi động lại server.
-- =====================================================================

UPDATE `task_sub_template`
   SET `NAME` = 'Hạ 100 quái vùng Băng',
       `max_count` = 100,
       `notify` = 'Quái nào ở Cánh đồng tuyết đến Hang băng cũng tính; qua Con đường rắn độc thì xong ngay',
       `npc_id` = -1,
       `map` = 105
 WHERE `task_main_id` = 35 AND `ducvupro` = 150;

UPDATE `task_main_template` SET `detail` = 'Muốn biết Heart đem ký ức đi đâu, phải hỏi người đã chết.
Tích điểm diệt quái, hạ 100 quái vùng Băng (hoặc đi Con đường rắn độc), rồi gặp Thượng Đế ở Thần điện.
Thưởng: 350 triệu SM, 350 triệu TN, 5 Đá nâng cấp 5'
 WHERE `id` = 35;

SELECT `ducvupro`, `NAME`, `max_count`, `notify`, `map`
  FROM `task_sub_template` WHERE `task_main_id` = 35 ORDER BY `ducvupro`;
