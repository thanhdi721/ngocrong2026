-- =====================================================================
-- 81-dong-ti-le-roi.sql — DÒNG CHỈ SỐ "Tỉ lệ rơi #%" (item_option_template 253)
-- Database: team2026 (MariaDB 10.4)        Sinh ngày: 2026-09-26
--
-- Code đi kèm: shop/ShopService.moBangXem (khung tiệm CHỈ ĐỂ XEM),
--              npc_list/TheoDoiBoss (bảng đồ rơi hiện bằng giao diện tiệm),
--              data/DataGame (vsItem 28 -> 29).
--
-- Phải chạy TRƯỚC: patch 75 (dòng 251, 252), patch 80 (NPC Theo Dõi Boss).
--
-- ---------------------------------------------------------------------
-- FILE NÀY LÀM GÌ
-- ---------------------------------------------------------------------
--  Thêm đúng MỘT dòng `item_option_template`: 253 'Tỉ lệ rơi #%'.
--  NPC "Theo Dõi Boss" dùng dòng này để hiện tỉ lệ rơi ngay dưới tên vật phẩm,
--  y như dòng "Không thể giao dịch" của tiệm.
--
-- ---------------------------------------------------------------------
-- CẨN THẬN — ĐÂY LÀ CHỖ DỄ LÀM HỎNG CLIENT NHẤT
-- ---------------------------------------------------------------------
--  Số dòng của bảng này được gửi cho client bằng writeByte (ItemData
--  .updateItemOptionItemplate) => TỐI ĐA 255 DÒNG. Vượt 255 là số bị cuộn về
--  0 và client đứng ở màn "Xin chờ" / vỡ hình toàn bộ vật phẩm.
--
--  Sau patch 75 bảng có 253 dòng (id 0..252). Patch này thêm id 253 -> 254 dòng.
--  => CHỈ CÒN ĐÚNG MỘT Ô TRỐNG (id 254). Lần sau muốn thêm dòng chỉ số nữa thì
--     phải bỏ bớt dòng cũ trước.
--
--  Id dòng cũng gửi bằng writeByte nên id phải <= 255. 253 thoả.
--
-- ---------------------------------------------------------------------
-- TRÌNH TỰ CHẠY
-- ---------------------------------------------------------------------
--   1) TẮT SERVER (item_option_template chỉ đọc lúc khởi động).
--   2) mysqldump -u root -p team2026 item_option_template > backup_81.sql
--   3) Chạy cả file. Chạy lại nhiều lần vẫn an toàn.
--   4) Xem khối (3): mọi cột `dat` phải = 1, `so_dong` phải = 254.
--   5) Bật server bản code mới (vsItem = 29) -> client tải lại bảng chỉ số.
-- =====================================================================

SET NAMES utf8mb4;

-- ---------------------------------------------------------------------
-- (1) KIỂM TRA TRƯỚC — `so_dong_hien_tai` phải = 253, `id_lon_nhat` phải = 252.
-- ---------------------------------------------------------------------
SELECT COUNT(*) AS `so_dong_hien_tai`, MAX(`id`) AS `id_lon_nhat`
  FROM `item_option_template`;

-- ---------------------------------------------------------------------
-- (2) CẬP NHẬT
-- ---------------------------------------------------------------------
DELETE FROM `item_option_template` WHERE `id` = 253;
INSERT INTO `item_option_template` (`id`, `NAME`) VALUES
(253, 'Tỉ lệ rơi #%');

-- ---------------------------------------------------------------------
-- (3) KIỂM TRA SAU — `dat` phải = 1 và `so_dong` phải = 254 (TRẦN LÀ 255).
-- ---------------------------------------------------------------------
SELECT 'dong 253 Ti le roi' AS `muc`,
       (SELECT COUNT(*) FROM `item_option_template`
         WHERE `id` = 253 AND `NAME` = 'Tỉ lệ rơi #%') AS `dat`
UNION ALL
SELECT 'id 0..253 lien tuc',
       ((SELECT COUNT(*) FROM `item_option_template` WHERE `id` BETWEEN 0 AND 253) = 254);

SELECT COUNT(*) AS `so_dong_phai_la_254_tran_255` FROM `item_option_template`;

-- ---------------------------------------------------------------------
-- (4) LÙI LẠI  (bỏ comment rồi chạy; TẮT SERVER trước)
-- ---------------------------------------------------------------------
-- DELETE FROM `item_option_template` WHERE `id` = 253;
