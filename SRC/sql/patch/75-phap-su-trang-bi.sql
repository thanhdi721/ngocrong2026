-- =====================================================================
-- 75 — PHÁP SƯ TRANG BỊ (2026-09-25)
-- =====================================================================
-- Chức năng mới ở Bà Hạt Mít: nạp chỉ số "Pháp Sư" cho CẢI TRANG, ĐEO LƯNG và
-- LINH THÚ — ba loại đồ vốn không có chỉ số gì, nhất là 138 món vừa mang từ SUMO về.
--
-- Luật (số nằm ở SRC/src/nro/models/combine/PhapSuTrangBi.java):
--   * Nâng : 1 trang bị + 20 Đá Pháp Sư + 200.000.000 vàng, tỉ lệ 100%.
--            Mỗi lần bốc ngẫu nhiên 1 trong 7 dòng, trúng trùng thì cộng dồn; MỘT MÓN chỉ nâng được 6 lần.
--   * Tẩy  : 1 trang bị đã pháp sư + 5 Đá Tẩy Pháp Sư + 500 ngọc -> gỡ sạch mọi dòng Pháp Sư.
--
-- Bảy dòng chỉ số (id option 251–257, nối tiếp cuối bảng nên client không lệch thứ tự):
--   251 Sức đánh, 252 HP, 253 KI, 254 Giáp, 255 Giảm sát thương, 256 Né đòn, 257 Xuyên giáp.
--   NPoint.addOption cộng chúng vào đúng chỗ của các dòng tương đương đang có
--   (50 sức đánh %, 77 HP %, 103 KI %, 47 giáp, 94 giảm sát thương, 108 né đòn, 98/99 xuyên giáp).
--
-- Hai viên đá: icon lấy từ source Bun (đá xanh 30498, đá đỏ 30501), đã chép vào
-- data/icon/x1..x4 với số 20257 và 20258.
--
-- Chạy cùng jar có DataGame.vsItem = 26. Khởi động lại server.
-- TRƯỚC KHI CHẠY: hai câu dưới phải ra 2261 và 250; khác thì DỪNG, báo lại.
-- =====================================================================
SELECT MAX(`id`) AS item_max FROM `item_template`;
SELECT MAX(`id`) AS option_max FROM `item_option_template`;

SET NAMES utf8mb4;

DELETE FROM `item_template`        WHERE `id` BETWEEN 2262 AND 2263;
DELETE FROM `item_option_template` WHERE `id` BETWEEN 251 AND 257;

-- (1) Bảy dòng chỉ số Pháp Sư. Bảng chỉ có hai cột (id, NAME); client đọc danh sách
-- option THEO THỨ TỰ nên id phải nối tiếp 250, không được chen vào giữa.
INSERT INTO `item_option_template` (`id`, `NAME`) VALUES
(251, 'Pháp Sư: Sức đánh +#%'),
(252, 'Pháp Sư: HP +#%'),
(253, 'Pháp Sư: KI +#%'),
(254, 'Pháp Sư: Giáp +#'),
(255, 'Pháp Sư: Giảm #% sát thương'),
(256, 'Pháp Sư: Né đòn +#%'),
(257, 'Pháp Sư: Xuyên giáp +#%');

-- (2) Hai viên đá. is_up_to_up = 1 để gộp chồng trong hành trang.
INSERT INTO `item_template` (`id`, `TYPE`, `gender`, `NAME`, `description`, `level`, `icon_id`,
                             `part`, `is_up_to_up`, `power_require`, `gold`, `gem`, `head`, `body`, `leg`) VALUES
(2262, 27, 3, 'Đá Pháp Sư', 'Dùng để pháp sư cải trang, đeo lưng, linh thú', 0, 20257, -1, 1, 0, 0, 0, -1, -1, -1),
(2263, 27, 3, 'Đá Tẩy Pháp Sư', 'Gỡ sạch chỉ số Pháp Sư trên trang bị', 0, 20258, -1, 1, 0, 0, 0, -1, -1, -1);

-- KIỂM TRA SAU
SELECT `id`, `NAME` FROM `item_option_template` WHERE `id` BETWEEN 251 AND 257;
SELECT COUNT(*) AS so_dong, MAX(`id`) + 1 AS so_id FROM `item_option_template`;
SELECT `id`, `NAME`, `TYPE`, `icon_id`, `is_up_to_up` FROM `item_template` WHERE `id` BETWEEN 2262 AND 2263;
