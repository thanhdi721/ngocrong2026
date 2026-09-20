-- =====================================================================
-- 34 — SỐ LƯỢNG ĐÁNH QUÁI MỚI (2026-09-20)
-- =====================================================================
-- Áp theo bảng chủ dự án đã điền trong docs/4-trien-khai/50-so-luong-danh-quai.md.
-- Tên bước được ghi lại cho khớp số mới.
--
-- Hai bước có ĐẾM GIỜ được nới thời gian theo tỉ lệ số lượng (sửa trong jar):
--   NV 34 bước 2: 20 Kado / 5 phút   -> 200 Kado / 20 phút
--   NV 42 bước 2: 60 lồng / 10 phút  -> 600 lồng / 45 phút
--
-- Chạy cùng bản jar mới. Chạy lại nhiều lần vẫn an toàn. Khởi động lại server.
-- =====================================================================

UPDATE `task_sub_template`
   SET `max_count` = 10, `NAME` = 'Hạ 10 quái vây thành'
 WHERE `task_main_id` = 18 AND `ducvupro` = 62;

UPDATE `task_sub_template`
   SET `max_count` = 600, `NAME` = 'Hạ 600 Nappa mất trí'
 WHERE `task_main_id` = 19 AND `ducvupro` = 66;

UPDATE `task_sub_template`
   SET `max_count` = 400, `NAME` = 'Hạ 400 Soldier gác kho'
 WHERE `task_main_id` = 19 AND `ducvupro` = 67;

UPDATE `task_sub_template`
   SET `max_count` = 300, `NAME` = 'Cùng bạn hạ 300 Appule'
 WHERE `task_main_id` = 19 AND `ducvupro` = 68;

UPDATE `task_sub_template`
   SET `max_count` = 400, `NAME` = 'Hạ 400 lính khỉ canh đường'
 WHERE `task_main_id` = 22 AND `ducvupro` = 81;

UPDATE `task_sub_template`
   SET `max_count` = 500, `NAME` = 'Hạ 500 Xên con cấp 1-2'
 WHERE `task_main_id` = 24 AND `ducvupro` = 93;

UPDATE `task_sub_template`
   SET `max_count` = 400, `NAME` = 'Hạ 400 Xên con cấp 3-4'
 WHERE `task_main_id` = 24 AND `ducvupro` = 94;

UPDATE `task_sub_template`
   SET `max_count` = 600, `NAME` = 'Hạ 600 Xên con cấp 5-7'
 WHERE `task_main_id` = 28 AND `ducvupro` = 110;

UPDATE `task_sub_template`
   SET `max_count` = 500, `NAME` = 'Hạ 500 Xên con cấp 8'
 WHERE `task_main_id` = 30 AND `ducvupro` = 119;

UPDATE `task_sub_template`
   SET `max_count` = 400, `NAME` = 'Hạ 400 Cabira hoặc Tobi'
 WHERE `task_main_id` = 32 AND `ducvupro` = 132;

UPDATE `task_sub_template`
   SET `max_count` = 1000, `NAME` = 'Hạ 1000 quái ở %3',
       `notify` = 'Lên %3 hạ 1000 con, quái loại nào cũng tính'
 WHERE `task_main_id` = 33 AND `ducvupro` = 137;

UPDATE `task_sub_template`
   SET `max_count` = 500, `NAME` = 'Hạ 500 Tai tím hoặc Abo'
 WHERE `task_main_id` = 34 AND `ducvupro` = 142;

UPDATE `task_sub_template`
   SET `max_count` = 200, `NAME` = 'Hạ 200 Kado trong 20 phút',
       `notify` = 'Còn 20 phút! Kado ở Dòng sông băng, Rừng băng, Hang băng; hết giờ phải đếm lại'
 WHERE `task_main_id` = 34 AND `ducvupro` = 143;

UPDATE `task_sub_template`
   SET `max_count` = 1200, `NAME` = 'Hạ 1200 quái vùng Băng'
 WHERE `task_main_id` = 35 AND `ducvupro` = 149;

UPDATE `task_sub_template`
   SET `max_count` = 1000, `NAME` = 'Hạ 1000 quái vùng Băng'
 WHERE `task_main_id` = 35 AND `ducvupro` = 150;

UPDATE `task_sub_template`
   SET `max_count` = 600, `NAME` = 'Hạ 600 Xên con phía bắc'
 WHERE `task_main_id` = 38 AND `ducvupro` = 163;

UPDATE `task_sub_template`
   SET `max_count` = 400, `NAME` = 'Hạ 400 Khỉ lông vàng'
 WHERE `task_main_id` = 39 AND `ducvupro` = 172;

UPDATE `task_sub_template`
   SET `max_count` = 600, `NAME` = 'Hạ 600 quái vành đai rừng'
 WHERE `task_main_id` = 41 AND `ducvupro` = 181;

UPDATE `task_sub_template`
   SET `max_count` = 600, `NAME` = 'Phá 600 lồng giam, 45 phút',
       `notify` = 'Còn 45 phút! Hạ Khỉ lông xanh, Taburine Đỏ; hết giờ phải đếm lại'
 WHERE `task_main_id` = 42 AND `ducvupro` = 187;

-- Mô tả nhiệm vụ có nhắc số lượng
UPDATE `task_main_template` SET `detail` = 'HP gốc của ngươi đã chạm trần, cơ thể không lớn thêm được nữa.
Hạ 1000 quái ở %3, nhờ sư phụ mở giới hạn rồi đạt 3 tỷ sức mạnh.
Thưởng: 250 triệu SM, 250 triệu TN, 50 Đậu thần cấp 8, 5 Đá bảo vệ'
 WHERE `id` = 33;

UPDATE `task_main_template` SET `detail` = 'Muốn biết Heart đem ký ức đi đâu, phải hỏi người đã chết.
Hạ 1200 rồi 1000 quái vùng Băng (hoặc đi Con đường rắn độc), rồi gặp Thượng Đế ở Thần điện.
Thưởng: 350 triệu SM, 350 triệu TN'
 WHERE `id` = 35;

-- KIỂM TRA
SELECT `task_main_id`, `ducvupro`, `NAME`, `max_count`
  FROM `task_sub_template`
 WHERE (`task_main_id`, `ducvupro`) IN ((18,62),(19,66),(19,67),(19,68),(22,81),(24,93),(24,94),
       (28,110),(30,119),(32,132),(33,137),(34,142),(34,143),(35,149),(35,150),(38,163),(39,172),(41,181),(42,187))
 ORDER BY `task_main_id`, `ducvupro`;
