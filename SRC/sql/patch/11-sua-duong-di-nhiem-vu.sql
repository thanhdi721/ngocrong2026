-- =====================================================================
-- 11 — SỬA ĐƯỜNG ĐI CÁC BƯỚC NHIỆM VỤ BỊ KẸT (2026-09-19)
-- =====================================================================
-- Rà toàn bộ bước "gặp NPC / tới map" của NV 0–50 theo cổng đi bộ trong
-- map_template + các NPC dịch chuyển trong code. 4 chỗ không có đường vào:
--
--  1) NV 14 b0 "Gặp Bunma ở Nhà Bunma" (map 102): Nhà Bunma chỉ tới được
--     bằng cỗ máy thời gian của Ca Lích, mở từ NV 24 -> kẹt cứng ở NV 14.
--     => Đổi sang Bunma (NPC 7) ở Siêu Thị (map 84) — cùng map với quầy Uron
--        của bước 1. Code: TaskService (case BUNMA thêm TASK_14_0).
--  2) NV 20 b0 / NV 48 b0 (Khu hang động 160): chỉ vào được bằng Nhẫn thời
--     không (có từ NV 32). => Cui ở Thung lũng Nappa (68) đưa tới từ NV 20;
--        Berry ở Khu hang động có nút "Về Thung lũng Nappa".
--  3) NV 45 b0 (Lãnh địa Fize 78): không có cổng vào.
--     => Whis ở Hành tinh Bill (154) có nút "Lãnh địa Fize" từ NV 45.
--  4) NV 46 b2, NV 47 b5 (Võ Đài Siêu Cấp 145): không có cổng vào.
--     => Dr. Myuu (166) đưa tới từ NV 46 b2; Thiên Sứ Whis ở Lãnh địa Fize
--        đưa tới (từ NV 46), và ở 145 có nút "Về Lãnh địa Fize".
--
-- Chạy SAU 06 / 07. Chạy lại nhiều lần vẫn an toàn (chỉ UPDATE).
-- Nếu sau này chạy lại 06 thì phải chạy lại 07, 08, 10 rồi 11.
-- Cần khởi động lại server sau khi chạy.
-- =====================================================================

-- (1) NV 14 bước 0 -> Bunma ở Siêu Thị
UPDATE `task_sub_template`
   SET `NAME` = 'Gặp Bunma ở Siêu Thị',
       `notify` = 'Bunma có tin mới, gặp cô ở Siêu Thị',
       `npc_id` = 7,
       `map` = 84
 WHERE `task_main_id` = 14 AND `ducvupro` = 45;

UPDATE `task_main_template` SET `detail` = 'Có kẻ đang đóng hộp ký ức đem bán như hàng hóa.
Gặp Bunma ở Siêu Thị, mua ở quầy Uron, chặn heo chở hàng ở %16.
Thưởng: 400.000 SM, 400.000 TN, 1 Hộp Ký Ức Bị Đánh Cắp, 10 Đá nâng cấp 1, 3 Đá bảo vệ'
 WHERE `id` = 14;

-- (2) NV 20 / NV 48 bước 0 -> nhờ Cui đưa tới Khu hang động
UPDATE `task_sub_template`
   SET `notify` = 'Nhờ Cui ở Thung lũng Nappa đưa tới Khu hang động gặp Berry'
 WHERE `task_main_id` IN (20, 48) AND `ducvupro` IN (70, 220);

-- (3) NV 45 bước 0 -> nhờ Whis đưa tới Lãnh địa Fize
UPDATE `task_sub_template`
   SET `notify` = 'Nhờ Whis ở Hành tinh Bill đưa tới Lãnh địa Fize'
 WHERE `task_main_id` = 45 AND `ducvupro` = 203;

-- (4) NV 46 bước 2 -> Dr. Myuu mở cổng tới Võ Đài Siêu Cấp
UPDATE `task_sub_template`
   SET `notify` = 'Heart chạy tới Võ Đài Siêu Cấp. Nhờ Dr. Myuu mở cổng đuổi theo'
 WHERE `task_main_id` = 46 AND `ducvupro` = 210;

-- KIỂM TRA: phải ra 6 dòng, dòng NV 14 có npc_id = 7, map = 84
SELECT `task_main_id`, `ducvupro`, `NAME`, `notify`, `npc_id`, `map`
  FROM `task_sub_template`
 WHERE (`task_main_id`, `ducvupro`) IN ((14,45),(20,70),(48,220),(45,203),(46,210),(47,219));
