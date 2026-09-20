-- =====================================================================
-- 29 — NV 45: GHI RÕ ĐƯỜNG TỚI LÃNH ĐỊA FIZE (2026-09-20)
-- =====================================================================
-- Map 78 Lãnh địa Fize không có cửa đi bộ: phải nhờ Whis ở Hành tinh Bill (map 154).
-- Đường: Thần điện -> Thượng Đế -> Hành tinh Kaio -> Thần Vũ Trụ -> Thánh địa Kaio
--        -> Ôsin -> Hành tinh Bill -> Whis -> nút "Lãnh địa Fize" (hiện từ NV 45).
-- Chỉ sửa chữ nhắc việc. Chạy lại nhiều lần vẫn an toàn. Khởi động lại server.
-- =====================================================================

UPDATE `task_sub_template`
   SET `notify` = 'Nhờ Whis ở Hành tinh Bill đưa sang Lãnh địa Fize (Ôsin ở Thánh địa Kaio đưa tới chỗ Bill)'
 WHERE `task_main_id` = 45 AND `ducvupro` = 203;

UPDATE `task_main_template` SET `detail` = 'Ký ức cần hai người mới thành ký ức.
Nhờ Whis ở Hành tinh Bill đưa sang Lãnh địa Fize, nhặt mảnh thứ bảy rồi hợp nhất 7 mảnh.
Thưởng: 950 triệu SM, 950 triệu TN, 5 Đá ngũ sắc, 50 Đậu thần cấp 8'
 WHERE `id` = 45;

SELECT `ducvupro`, `NAME`, `notify` FROM `task_sub_template`
 WHERE `task_main_id` = 45 ORDER BY `ducvupro`;
