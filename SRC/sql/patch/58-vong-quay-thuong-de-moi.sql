-- =====================================================================
-- 58 — VÒNG QUAY THƯỢNG ĐẾ KIỂU MỚI (2026-09-23)
-- =====================================================================
-- * Chỉ còn MỘT vòng quay, giá 1 Thỏi vàng (vật phẩm 457) cho 1 lượt.
-- * Bảng thưởng mới nằm trong jar (RewardService.rollLuckyRound), tổng đúng 100%:
--     73,20%  Vàng 5.000–50.000
--      1,88%  Mảnh Đội trưởng Vàng
--      0,42%  Đá nâng cấp (lục bảo / saphia / ruby / titan / thạch anh) ×1–5
--      0,12%  Ngọc Rồng 5–6 sao ×1–5
--      0,12%  Mảnh thú cưỡi ×1–5
--      1,00%  mỗi loại sách nâng kỹ năng đệ tử 2 / 3 / 4 / 5  (4%)
--      4,00%  Cỏ bốn lá ×1–5
--      3,50%  mỗi loại Cuồng nộ 2 / Bổ huyết 2 / Bổ khí 2 / Giáp Xên bọ hung 2  (14%)
--      2,26%  Bùa x2 tn,sm đệ tử
-- * Mốc lượt quay (quà vào RƯƠNG PHỤ, đều khoá giao dịch):
--     1.000  Xe xanh Chi Chi              10% SĐ, 10% HP, 10% KI, 5% chí mạng
--     3.000  Bồ cào 9 răng                10% SĐ, 10% HP, 10% KI, 5% chí mạng
--     5.000  Xe đỏ Bun ma                 20% SĐ, 20% HP, 20% KI, 10% chí mạng, 10% SĐ chí mạng
--     5.000  Thanh Long Yển Nguyệt đao    20% SĐ, 20% HP, 20% KI, 10% chí mạng, 10% SĐ chí mạng
--    10.000  Cải trang Goku SSJ3 Hắc Kim  40% SĐ, 60% HP, 60% KI, 15% chí mạng, 30% SĐ chí mạng
--    12.000  Cải trang Goku SSJ4 Huyết Hỏa 45% SĐ, 70% HP, 70% KI, 25% chí mạng, 40% SĐ chí mạng
--
-- Patch này thêm cột lưu số lượt quay và vật phẩm "Nâng kỹ năng 5 đệ tử" (2123).
-- Chạy cùng jar mới. Chạy lại nhiều lần vẫn an toàn. Khởi động lại server.
-- =====================================================================

-- 1. Cột lưu số lượt quay + mốc đã nhận, dạng "số lượt|cờ mốc"
SET @co_cot := (SELECT COUNT(*) FROM information_schema.columns
                 WHERE table_schema = DATABASE() AND table_name = 'player' AND column_name = 'vqtd');
SET @sql := IF(@co_cot = 0, 'ALTER TABLE `player` ADD COLUMN `vqtd` TEXT NULL', 'SELECT ''cột vqtd đã có''');
PREPARE st FROM @sql; EXECUTE st; DEALLOCATE PREPARE st;

-- 2. Sách nâng kỹ năng 5 đệ tử (nối tiếp id 2122)
DELETE FROM `item_template` WHERE `id` = 2123;
INSERT INTO `item_template` (`id`, `TYPE`, `gender`, `NAME`, `description`, `level`, `icon_id`, `part`, `is_up_to_up`, `power_require`, `gold`, `gem`, `head`, `body`, `leg`) VALUES
(2123, 27, 3, 'Nâng kỹ năng 5 đệ tử', 'Nâng chiêu 5 của đệ tử lên 1 cấp', 1, 7101, -1, 1, 0, 0, 800, -1, -1, -1);

-- KIỂM TRA
SELECT `id`, `NAME`, `TYPE` FROM `item_template` WHERE `id` = 2123;
SHOW COLUMNS FROM `player` LIKE 'vqtd';
