-- =====================================================================
-- 30 — NV 45: CHUYỂN TỪ "LÃNH ĐỊA FIZE" SANG HÀNH TINH NGỤC TÙ (2026-09-20)
-- =====================================================================
-- Map 78 "Lãnh địa Fize" KHÔNG có file địa hình (data/map/tile_map_data/78 không tồn tại).
-- Server không tính được mặt đất của map này nên vừa dịch chuyển vào là client treo
-- (quay vòng mãi). Đây cũng là lý do bản gốc không có cửa nào dẫn vào map đó.
--
-- Đã rà: trong 8 map thiếu địa hình (78, 95, 101, 116, 121, 125, 130, 134) chỉ NV 45 dùng map 78.
--
-- NV 45 chuyển sang Hành tinh ngục tù (155) — có địa hình, có quái, đi được bằng Ôsin
-- ở Thánh địa Kaio (đường quen từ NV 42):
--   0 Tới Hành tinh ngục tù
--   1 Nhặt Mảnh Ký Ức thứ bảy   (rơi 30% từ Khỉ lông xanh / Taburine Đỏ ở map 155)
--   2 Hợp nhất 7 mảnh
--   3 Nghe Thiên Sứ Whis        (NPC 64 đặt thêm ở map 155)
--
-- Chạy cùng bản jar mới. Chạy lại nhiều lần vẫn an toàn. Khởi động lại server.
-- =====================================================================

-- Thiên Sứ Whis (64) đứng cạnh Ôsin (44) ở map 155; nền y = 792.
UPDATE `map_template`
   SET `npcs` = CONCAT(LEFT(TRIM(`npcs`), CHAR_LENGTH(TRIM(`npcs`)) - 1), ',[64,247,792]]')
 WHERE `id` = 155 AND `npcs` NOT LIKE '%[64,%' AND TRIM(`npcs`) <> '[]';

UPDATE `task_sub_template`
   SET `NAME` = 'Tới Hành tinh ngục tù',
       `notify` = 'Nhờ Ôsin ở Thánh địa Kaio đưa tới Hành tinh ngục tù', `map` = 155
 WHERE `task_main_id` = 45 AND `ducvupro` = 203;

UPDATE `task_sub_template`
   SET `notify` = 'Mảnh thứ bảy rơi từ quái ở Hành tinh ngục tù', `map` = 155
 WHERE `task_main_id` = 45 AND `ducvupro` = 204;

UPDATE `task_sub_template`
   SET `notify` = 'Dùng Lõi Ký Ức chưa hoàn chỉnh khi đã đủ 7 Mảnh Ký Ức', `npc_id` = -1, `map` = 155
 WHERE `task_main_id` = 45 AND `ducvupro` = 206;

UPDATE `task_sub_template`
   SET `notify` = 'Thiên Sứ Whis đứng cạnh Ôsin ở Hành tinh ngục tù', `npc_id` = 64, `map` = 155
 WHERE `task_main_id` = 45 AND `ducvupro` = 207;

UPDATE `task_main_template` SET `detail` = 'Ký ức cần hai người mới thành ký ức.
Tới Hành tinh ngục tù, nhặt mảnh thứ bảy từ quái rồi hợp nhất 7 mảnh.
Thưởng: 950 triệu SM, 950 triệu TN, 5 Đá ngũ sắc, 50 Đậu thần cấp 8'
 WHERE `id` = 45;

SELECT `id`, `npcs` FROM `map_template` WHERE `id` = 155;
SELECT `ducvupro`, `NAME`, `notify`, `npc_id`, `map`
  FROM `task_sub_template` WHERE `task_main_id` = 45 ORDER BY `ducvupro`;
