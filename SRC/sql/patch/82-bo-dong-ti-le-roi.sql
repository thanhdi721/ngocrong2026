-- =====================================================================
-- 82-bo-dong-ti-le-roi.sql — GỠ DÒNG "Tỉ lệ rơi #%" (item_option_template 253)
-- Database: team2026 (MariaDB 10.4)        Sinh ngày: 2026-09-26
--
-- Code đi kèm: npc_list/TheoDoiBoss (bỏ dòng tỉ lệ khỏi bảng đồ rơi),
--              data/DataGame (vsItem 29 -> 30).
--
-- Chạy SAU: patch 81 (đã thêm dòng 253).
--
-- ---------------------------------------------------------------------
-- VÌ SAO GỠ
-- ---------------------------------------------------------------------
--  Bảng đồ rơi ở NPC "Theo Dõi Boss" nay chỉ liệt kê MÓN, không hiện tỉ lệ nữa
--  (xem tỉ lệ thì vào cpanel), nên dòng chỉ số 253 không còn ai dùng.
--
--  Bảng `item_option_template` gửi số dòng cho client bằng writeByte => TRẦN 255
--  DÒNG. Sau patch 81 đang là 254, tức chỉ còn MỘT ô trống. Gỡ dòng thừa này trả
--  lại ô đó cho lần sau cần thêm chỉ số thật.
--
--  CHƯA CHẠY PATCH 81 thì chạy file này cũng không sao, nó chỉ xoá nếu có.
--
-- ---------------------------------------------------------------------
-- TRÌNH TỰ CHẠY
-- ---------------------------------------------------------------------
--   1) TẮT SERVER.
--   2) mysqldump -u root -p team2026 item_option_template > backup_82.sql
--   3) Chạy cả file. Chạy lại nhiều lần vẫn an toàn.
--   4) Xem khối (3): `so_dong` phải = 253, `con_dong_253` phải = 0.
--   5) Bật server bản code mới (vsItem = 30).
-- =====================================================================

SET NAMES utf8mb4;

-- ---------------------------------------------------------------------
-- (1) KIỂM TRA TRƯỚC
-- ---------------------------------------------------------------------
SELECT COUNT(*) AS `so_dong_hien_tai`, MAX(`id`) AS `id_lon_nhat`
  FROM `item_option_template`;

-- ---------------------------------------------------------------------
-- (2) CẬP NHẬT
-- ---------------------------------------------------------------------
-- Xoá dòng 253. Không ai gán chỉ số này cho vật phẩm nào (nó chỉ hiện trong khung
-- xem của NPC), nên không có dữ liệu người chơi nào bị ảnh hưởng.
DELETE FROM `item_option_template` WHERE `id` = 253;

-- ---------------------------------------------------------------------
-- (3) KIỂM TRA SAU — `so_dong` phải = 253 và `con_dong_253` phải = 0.
-- ---------------------------------------------------------------------
SELECT COUNT(*) AS `so_dong_phai_la_253`,
       (SELECT COUNT(*) FROM `item_option_template` WHERE `id` = 253) AS `con_dong_253`,
       MAX(`id`) AS `id_lon_nhat_phai_la_252`
  FROM `item_option_template`;

-- ---------------------------------------------------------------------
-- (4) LÙI LẠI  (bỏ comment rồi chạy; TẮT SERVER trước)
-- ---------------------------------------------------------------------
-- INSERT INTO `item_option_template` (`id`, `NAME`) VALUES (253, 'Tỉ lệ rơi #%');
