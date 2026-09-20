-- =====================================================================
-- 27 — NV 39: BƯỚC "GỌI RỒNG THẦN VÀ ƯỚC" TÍNH Ở MỌI MAP (2026-09-20)
-- =====================================================================
-- Trước đây bước này chỉ tính khi ước ĐÚNG Rồng Thần 1 Sao và ĐỨNG Ở LÀNG QUÊ NHÀ.
-- Gọi rồng ở map khác là mất 7 viên ngọc mà bước vẫn 0/1 (phải đi gom lại 7 viên).
-- Nay gọi rồng ở đâu, loại rồng nào cũng tính; ai đã lỡ ước rồi thì server tự cho qua bước
-- (đang ở bước này mà trong hành trang không còn đủ 7 viên = đã ước).
--
-- Chỉ sửa chữ nhắc việc; phần tính nằm trong jar.
-- Chạy lại nhiều lần vẫn an toàn. Khởi động lại server.
-- =====================================================================

UPDATE `task_sub_template`
   SET `notify` = 'Dùng 7 viên ngọc gọi Rồng Thần rồi ước một điều, gọi ở đâu cũng được'
 WHERE `task_main_id` = 39 AND `ducvupro` = 170;

UPDATE `task_main_template` SET `detail` = 'Bardock thấy trước cái chết của ngươi và chọn đổi chỗ cho ngươi.
Gom 7 viên Ngọc Rồng, gọi Rồng Thần và ước, rồi hạ Baby ở Làng Kakarot.
Thưởng: 1 tỷ SM, 1 tỷ TN, 10 Đá bảo vệ, 50 Đậu thần cấp 8'
 WHERE `id` = 39;

SELECT `ducvupro`, `NAME`, `notify`, `map` FROM `task_sub_template`
 WHERE `task_main_id` = 39 ORDER BY `ducvupro`;
