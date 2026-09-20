-- =====================================================================
-- 23 — BỎ 4 BƯỚC "RỦ 1 NGƯỜI ĐI CÙNG" (2026-09-20)
-- =====================================================================
-- Server ít người online thì không ai rủ được ai, bước này kẹt vô thời hạn.
-- Chủ dự án chốt: bỏ hẳn.
--
--   NV 31 bước 3 "Rủ 1 người vào võ đài"        -> NV 31 còn 5 bước
--   NV 35 bước 1 "Rủ 1 người đi cùng"           -> NV 35 còn 4 bước
--   NV 45 bước 2 "Rủ 1 người làm lễ hợp nhất"   -> NV 45 còn 4 bước
--   NV 49 bước 3 "Rủ 1 người vào võ đài"        -> NV 49 còn 5 bước
--
-- Người đang đứng đúng bước bị xoá sẽ chuyển sang bước kế tiếp của nhiệm vụ đó.
-- PHẢI chạy cùng bản jar mới (code đã dời số bước). Chạy lại nhiều lần vẫn an toàn.
-- Cần khởi động lại server.
-- =====================================================================

DELETE FROM `task_sub_template` WHERE `task_main_id` = 31 AND `ducvupro` = 126;
DELETE FROM `task_sub_template` WHERE `task_main_id` = 35 AND `ducvupro` = 148;
DELETE FROM `task_sub_template` WHERE `task_main_id` = 45 AND `ducvupro` = 205;
DELETE FROM `task_sub_template` WHERE `task_main_id` = 49 AND `ducvupro` = 229;

DELETE FROM `task_main_reward` WHERE `task_id` = 31 AND `sub_index` = 5;
DELETE FROM `task_main_reward` WHERE `task_id` = 35 AND `sub_index` = 4;
DELETE FROM `task_main_reward` WHERE `task_id` = 45 AND `sub_index` = 4;
DELETE FROM `task_main_reward` WHERE `task_id` = 49 AND `sub_index` = 5;

UPDATE `task_main_template` SET `detail` = 'Muốn biết Heart đem ký ức đi đâu, phải hỏi người đã chết.
Đi Con đường rắn độc (hoặc hạ quái ở cụm map thay thế), rồi gặp Thượng Đế ở Thần điện.
Thưởng: 350 triệu SM, 350 triệu TN, 5 Đá nâng cấp 5'
 WHERE `id` = 35;

UPDATE `task_main_template` SET `detail` = 'Ký ức cần hai người mới thành ký ức.
Nhặt mảnh thứ bảy ở Lãnh địa Fize rồi hợp nhất 7 mảnh.
Thưởng: 950 triệu SM, 950 triệu TN, 5 Đá ngũ sắc, 50 Đậu thần cấp 8'
 WHERE `id` = 45;

-- KIỂM TRA: không còn bước nào tên bắt đầu bằng "Rủ 1 người"
SELECT `task_main_id`, `ducvupro`, `NAME` FROM `task_sub_template`
 WHERE `task_main_id` IN (31, 35, 45, 49) ORDER BY `task_main_id`, `ducvupro`;
SELECT COUNT(*) AS `con_buoc_ru_nguoi` FROM `task_sub_template` WHERE `NAME` LIKE 'Rủ 1 người%';
