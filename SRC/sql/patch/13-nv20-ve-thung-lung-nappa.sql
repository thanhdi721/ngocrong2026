-- =====================================================================
-- 13 — NV 20 / NV 48: CHUYỂN TỪ KHU HANG ĐỘNG VỀ THUNG LŨNG NAPPA (2026-09-19)
-- =====================================================================
-- Khu hang động (160) quái 4–5 triệu máu, người chơi NV 20 sang đó cày sức
-- mạnh quá nhanh. Chủ dự án yêu cầu không cho qua map này ở NV 20.
--
--  * Berry (71) và Granola (76) đứng thêm ở Thung lũng Nappa (68), cạnh Cui —
--    nơi NV 19 kết thúc và có boss Kuku của NV 20.
--  * Cui KHÔNG còn nút đưa sang Khu hang động (đã gỡ trong code).
--    Khu hang động lại chỉ vào được bằng Nhẫn thời không (từ NV 32) như thiết kế.
--  * Berry / Granola ở 160 vẫn giữ (Bardock NV 32+), code vẫn nhận ở 160
--    cho người đang dở bước ở đó.
--
-- Chạy SAU 11. Chạy lại nhiều lần vẫn an toàn. Cần khởi động lại server.
-- Đã chạy bản cũ (Granola [76,324,408] lọt trong vách đá)? Dòng UPDATE thứ 2
-- bên dưới tự sửa vị trí.
-- =====================================================================

-- Map 68 (tính từ data/map/tile_map_data/68): nền y=408 chỉ phẳng ở x=24..263 (Cui x=84,
-- Berry x=204); từ x=264 là vách đá, mặt trên phẳng y=312 ở x=312..600 -> Granola x=456, y=312.
-- Nối thêm vào cuối cột npcs (giữ nguyên NPC đang có); đã có Berry thì bỏ qua.
UPDATE `map_template`
   SET `npcs` = CONCAT(LEFT(`npcs`, CHAR_LENGTH(`npcs`) - 1), ',[71,204,408],[76,456,312]]')
 WHERE `id` = 68 AND `npcs` NOT LIKE '%[71,%' AND `npcs` <> '[]';

-- Sửa vị trí Granola nếu đã chạy bản cũ của patch này.
UPDATE `map_template`
   SET `npcs` = REPLACE(`npcs`, '[76,324,408]', '[76,456,312]')
 WHERE `id` = 68;

-- ---- NV 20 ----------------------------------------------------------
UPDATE `task_sub_template`
   SET `NAME` = 'Tìm Berry, Thung lũng Nappa',
       `notify` = 'Berry đứng cạnh Cui ở Thung lũng Nappa', `map` = 68
 WHERE `task_main_id` = 20 AND `ducvupro` = 70;

UPDATE `task_sub_template`
   SET `notify` = 'Điểm rẽ nhánh: nói chuyện với Berry, chọn Granola hoặc Jaco', `map` = 68
 WHERE `task_main_id` = 20 AND `ducvupro` = 71;

UPDATE `task_sub_template`
   SET `notify` = 'Granola đứng cạnh Berry ở Thung lũng Nappa', `map` = 68
 WHERE `task_main_id` = 20 AND `ducvupro` = 72;

UPDATE `task_sub_template`
   SET `notify` = 'Về Thung lũng Nappa nhận tiền từ Granola', `map` = 68
 WHERE `task_main_id` = 20 AND `ducvupro` = 75;

UPDATE `task_main_template` SET `detail` = 'Granola rủ ngươi săn ba tay chân của Fide, không cần giấy phép.
Chọn phe ở Thung lũng Nappa; theo Granola thì hạ 3 tay chân Fide.
Thưởng: 8 triệu SM, 8 triệu TN, 40 Đá nâng cấp 2'
 WHERE `id` = 20;

-- ---- NV 48 (nhánh Jaco) ----------------------------------------------
UPDATE `task_sub_template`
   SET `NAME` = 'Tìm Berry, Thung lũng Nappa',
       `notify` = 'Berry đứng cạnh Cui ở Thung lũng Nappa', `map` = 68
 WHERE `task_main_id` = 48 AND `ducvupro` = 220;

UPDATE `task_sub_template`
   SET `notify` = 'Điểm rẽ nhánh: nói chuyện với Berry, chọn Granola hoặc Jaco', `map` = 68
 WHERE `task_main_id` = 48 AND `ducvupro` = 221;

-- KIỂM TRA
SELECT `id`, `npcs` FROM `map_template` WHERE `id` = 68;
SELECT `task_main_id`, `ducvupro`, `NAME`, `notify`, `map`
  FROM `task_sub_template` WHERE `task_main_id` IN (20, 48) ORDER BY `task_main_id`, `ducvupro`;
