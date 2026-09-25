-- =====================================================================
-- 38 — SỬA PART 1919 BỊ GHI NHẦM THÀNH 1949 (2026-09-21)
-- =====================================================================
-- Trong DB gốc, dòng part nằm giữa 1918 và 1920 (chân của "Cải trang Gốc" 1846,
-- dữ liệu bắt đầu bằng [[21326,...) bị gõ nhầm id = 1949. Kết quả:
--   * không có part 1919, có HAI part 1949;
--   * server nạp part theo thứ tự id -> từ vị trí 1919 tới 1948 client nhận LỆCH
--     MỘT part: mọi cải trang / NPC / boss dùng part 1919–1948 bị ghép sai hình.
-- Bảng `part` lại KHÔNG có khoá chính nên MySQL không chặn được lỗi này.
--
-- Patch trả dòng đó về id 1919 (chỉ khi đúng là đang thiếu 1919), rồi thêm khoá
-- chính để sau này không thể trùng id nữa. Jar mới cũng tự xếp part theo id và
-- báo lỗi trong log nếu còn trùng / hổng.
--
-- Chạy TRƯỚC patch 39. Chạy lại nhiều lần vẫn an toàn. Khởi động lại server.
-- =====================================================================

SET @co_1919 := (SELECT COUNT(*) FROM `part` WHERE `id` = 1919);
UPDATE `part` SET `id` = 1919
 WHERE `id` = 1949 AND `DATA` LIKE '[[21326,%' AND @co_1919 = 0;

-- KIỂM TRA: phải KHÔNG còn dòng nào (không trùng id)
SELECT `id`, COUNT(*) AS so_dong FROM `part` GROUP BY `id` HAVING COUNT(*) > 1;

-- Khoá chính: chỉ thêm khi chưa có. Nếu câu dưới báo "Duplicate entry" nghĩa là
-- vẫn còn id trùng (xem kết quả KIỂM TRA ở trên) -> gửi lại cho người sửa.
SET @co_pk := (SELECT COUNT(*) FROM information_schema.table_constraints
                WHERE table_schema = DATABASE() AND table_name = 'part' AND constraint_type = 'PRIMARY KEY');
SET @sql := IF(@co_pk = 0, 'ALTER TABLE `part` ADD PRIMARY KEY (`id`)', 'SELECT ''part đã có khoá chính''');
PREPARE st FROM @sql; EXECUTE st; DEALLOCATE PREPARE st;

SELECT `id`, `TYPE`, LEFT(`DATA`, 40) FROM `part` WHERE `id` IN (1918, 1919, 1920, 1949) ORDER BY `id`;
