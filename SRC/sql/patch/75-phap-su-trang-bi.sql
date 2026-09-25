-- =====================================================================
-- 75 — PHÁP SƯ TRANG BỊ (2026-09-25)
-- =====================================================================
-- Chức năng mới ở Bà Hạt Mít: nạp chỉ số "Pháp Sư" cho CẢI TRANG, ĐEO LƯNG và
-- LINH THÚ — ba loại đồ vốn không có chỉ số gì, nhất là 138 món vừa mang từ SUMO về.
--
-- Luật (số nằm ở SRC/src/nro/models/combine/PhapSuTrangBi.java):
--   * Áp dụng cho ÁO, QUẦN, GĂNG, GIÀY, RAĐA (loại 0–4).
--   * Nâng : 1 trang bị + 20 Đá Pháp Sư + 200.000.000 vàng, tỉ lệ 100%.
--            Mỗi lần bốc ngẫu nhiên 1 trong 7 dòng, trúng trùng thì cộng dồn; MỘT MÓN chỉ nâng được 6 lần.
--   * Tẩy  : 1 trang bị đã pháp sư + 5 Đá Tẩy Pháp Sư + 500 ngọc -> TRỪ LẠI đúng phần
--            pháp sư đã cộng, giữ nguyên chỉ số gốc của trang bị.
--
-- Bảy chỉ số dùng lại các dòng CÓ SẴN (50 sức đánh %, 77 HP %, 103 KI %, 47 giáp,
-- 94 giảm sát thương, 108 né đòn, 98 + 99 xuyên giáp); bảng option chỉ thêm đúng MỘT dòng
-- id 251 "Pháp Sư cấp #" làm tem đếm số lần đã nâng.
--
-- Hai viên đá: icon lấy từ source Bun (đá xanh 30498, đá đỏ 30501), đã chép vào
-- data/icon/x1..x4 với số 20257 và 20258.
--
-- Tổng số dòng option sau khi chạy phải là 253 (TRẦN LÀ 255).
-- Chạy cùng jar có DataGame.vsItem = 26. Khởi động lại server.
-- TRƯỚC KHI CHẠY: hai câu dưới phải ra 2261 và 250; khác thì DỪNG, báo lại.
-- =====================================================================
SELECT MAX(`id`) AS item_max FROM `item_template`;
SELECT MAX(`id`) AS option_max FROM `item_option_template`;

SET NAMES utf8mb4;

DELETE FROM `item_template`        WHERE `id` BETWEEN 2262 AND 2263;
DELETE FROM `item_option_template` WHERE `id` BETWEEN 251 AND 257;   -- dọn sạch dải cũ trước khi thêm lại

-- (1) HAI dòng tem (không phải 7 dòng chỉ số).
--
-- VÌ SAO CHỈ MỘT: gói tin gửi bảng option ghi SỐ DÒNG bằng MỘT BYTE
-- (ItemData.updateItemOptionItemplate) và mỗi dòng chỉ số của món đồ cũng ghi id bằng
-- MỘT BYTE. Bảng đang có 251 dòng (id 0–250), tức chỉ còn chỗ tới id 254 và tổng không
-- được quá 255. Bản đầu thêm 7 dòng (id 251–257) làm tổng lên 258 -> byte ghi ra thành 2,
-- client đọc 2 dòng rồi lệch cả gói và hỏng. Nay chỉ thêm đúng 1 dòng.
--
-- Bảy chỉ số Pháp Sư dùng LẠI các dòng sẵn có mà client đã biết:
--   sức đánh 50 · HP 77 · KI 103 · giáp 47 · giảm sát thương 94 · né đòn 108
--   · xuyên giáp 98 + 99 (cộng cả chưởng lẫn cận chiến)
--
-- 251 "Pháp Sư cấp #" hiện cho người chơi thấy đã nâng mấy lần.
-- 252 "Dấu Pháp Sư" KHÔNG có dấu # trong tên nên client chỉ hiện chữ; phần param âm thầm
--     giữ số lần trúng của từng dòng (mã cơ số 7) để lúc tẩy trừ lại cho ĐÚNG phần pháp sư
--     đã cộng — trang bị thường vốn đã có chỉ số riêng, không được xoá lẫn.
INSERT INTO `item_option_template` (`id`, `NAME`) VALUES
(251, 'Pháp Sư cấp #'),
(252, 'Dấu Pháp Sư');

-- (2) Hai viên đá. is_up_to_up = 1 để gộp chồng trong hành trang.
INSERT INTO `item_template` (`id`, `TYPE`, `gender`, `NAME`, `description`, `level`, `icon_id`,
                             `part`, `is_up_to_up`, `power_require`, `gold`, `gem`, `head`, `body`, `leg`) VALUES
(2262, 27, 3, 'Đá Pháp Sư', 'Dùng để pháp sư cải trang, đeo lưng, linh thú', 0, 20257, -1, 1, 0, 0, 0, -1, -1, -1),
(2263, 27, 3, 'Đá Tẩy Pháp Sư', 'Gỡ sạch chỉ số Pháp Sư trên trang bị', 0, 20258, -1, 1, 0, 0, 0, -1, -1, -1);

-- KIỂM TRA SAU
SELECT `id`, `NAME` FROM `item_option_template` WHERE `id` >= 251;
SELECT COUNT(*) AS so_dong, MAX(`id`) + 1 AS so_id FROM `item_option_template`;
SELECT `id`, `NAME`, `TYPE`, `icon_id`, `is_up_to_up` FROM `item_template` WHERE `id` BETWEEN 2262 AND 2263;
