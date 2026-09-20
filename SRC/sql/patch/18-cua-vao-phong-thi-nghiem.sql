-- =====================================================================
-- 18 — CỬA VÀO PHÒNG THÍ NGHIỆM MYUU (NV 29) — 2026-09-20
-- =====================================================================
-- Map 166 "Phòng thí nghiệm Myuu" vốn KHÔNG có cổng vào, không NPC, không quái
-- (map trống trong bản gốc). NV 29 bước 1 "Vào Phòng thí nghiệm Myuu" vì vậy
-- không vào được.
--
-- Patch này (trích từ patch 04, chạy riêng được nếu 04 chưa chạy):
--   * map 97 Thành phố phía bắc: thêm cửa ở giữa map (ô x 840–888, y 336–384,
--     nền y = 384) dẫn sang map 166, điểm đáp (624, 240).
--   * map 166: cửa ra ở bệ y = 240 (x 576–624) về lại map 97 tại (864, 384).
--   * map 166: đặt NPC 83 Dr. Myuu ở giữa bệ (624, 240) cho NV 29 bước 3 và NV 46.
--   * Ghi rõ vị trí cửa vào trong lời nhắc của bước.
--
-- Điều kiện vào map 166 nằm trong jar: phải đang ở NV 29 trở đi (TASK_29_0).
-- Chạy lại nhiều lần vẫn an toàn (ghi đè đúng một giá trị). Khởi động lại server.
-- =====================================================================

UPDATE `map_template`
   SET `waypoints` = '[["Ngọn núi phía bắc",1656,360,1680,384,0,0,98,60,384],["Cao nguyên",0,360,24,384,0,0,96,1620,168],["Phòng thí nghiệm Myuu",840,336,888,384,1,0,166,624,240]]'
 WHERE `id` = 97;

UPDATE `map_template`
   SET `waypoints` = '[["Thành phố phía bắc",576,192,624,240,1,0,97,864,384]]'
 WHERE `id` = 166;

UPDATE `map_template`
   SET `npcs` = '[[83,624,240]]'
 WHERE `id` = 166 AND `npcs` NOT LIKE '%[83,%';

UPDATE `task_sub_template`
   SET `notify` = 'Cửa vào nằm giữa Thành phố phía bắc; mang theo Thẻ từ giả của Bunma'
 WHERE `task_main_id` = 29 AND `ducvupro` = 115;

-- KIỂM TRA: map 97 phải có 3 cổng, map 166 phải có 1 cổng và NPC 83
SELECT `id`, `NAME`, `waypoints`, `npcs` FROM `map_template` WHERE `id` IN (97, 166);
