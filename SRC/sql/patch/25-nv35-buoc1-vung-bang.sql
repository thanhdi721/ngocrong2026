-- =====================================================================
-- 25 — NV 35 BƯỚC 1 CŨNG TÍNH QUÁI VÙNG BĂNG (2026-09-20)
-- =====================================================================
-- Bước "Tích 120 điểm diệt quái" trước đây chỉ tính quái trong Con đường rắn độc
-- (mỗi con 2 điểm) hoặc Dơi da xanh / Quỷ chim ở 6 map cũ. Nay quái NÀO ở vùng
-- Băng (map 105–110) cũng tính 1 điểm, giống bước 2 — người chơi cày một chỗ là xong cả hai bước.
--
-- Chạy cùng bản jar mới. Chạy lại nhiều lần vẫn an toàn. Khởi động lại server.
-- =====================================================================

UPDATE `task_sub_template`
   SET `NAME` = 'Hạ 120 quái vùng Băng',
       `notify` = 'Quái nào ở vùng Băng cũng tính 1; trong Con đường rắn độc mỗi con tính 2',
       `map` = 105
 WHERE `task_main_id` = 35 AND `ducvupro` = 149;

UPDATE `task_main_template` SET `detail` = 'Muốn biết Heart đem ký ức đi đâu, phải hỏi người đã chết.
Hạ 120 rồi 100 quái vùng Băng (hoặc đi Con đường rắn độc), rồi gặp Thượng Đế ở Thần điện.
Thưởng: 350 triệu SM, 350 triệu TN, 5 Đá nâng cấp 5'
 WHERE `id` = 35;

SELECT `ducvupro`, `NAME`, `max_count`, `notify`, `map`
  FROM `task_sub_template` WHERE `task_main_id` = 35 ORDER BY `ducvupro`;
