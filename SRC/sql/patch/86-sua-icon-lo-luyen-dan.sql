-- =====================================================================
-- 86-sua-icon-lo-luyen-dan.sql — SỬA ICON PART CỦA "LÒ LUYỆN ĐAN"
-- Database: team2026 (MariaDB 10.4)        Sinh ngày: 2026-09-27
--
-- ---------------------------------------------------------------------
-- VÌ SAO CÓ FILE NÀY
-- ---------------------------------------------------------------------
--  Patch 84 bản đầu đặt icon ảnh lò là 33001. SAI: `Manager.loadDatabase` đọc icon
--  trong `part`.`DATA` bằng Short.parseShort, mà 33001 > 32767 (Short.MAX_VALUE),
--  nên server văng ngay lúc nạp CSDL và KHÔNG LÊN ĐƯỢC:
--
--      java.lang.NumberFormatException: Value out of range. Value:"33001" Radix:10
--          at nro.models.server.Manager.loadDatabase(Manager.java:492)
--
--  Ảnh đã được đánh số lại thành 32700 trong `SRC/data/icon/x1..x4` (32700 là ô trống;
--  icon lớn nhất đang có trong res là 32667). File này sửa nốt dòng `part` cho khớp.
--
--  File này SỬA LUÔN CỠ LÒ: ảnh mới là hình vuông 52 / 105 / 157 / 211 px (bản đầu chỉ
--  36 px, nhỏ hơn một NPC người). Hai số -8,-2 giữ đáy và tâm lò đúng chỗ cũ:
--      tâm x: 0 + 36/2 = 18  ->  -8 + 52/2 = 18
--      đáy y: 0 + 50   = 50  ->  -2 + 52   = 50
--  Lò đứng cao hay thấp lệch thì chỉ cần sửa hai số đó, không phải sửa ảnh.
--
--  QUAN TRỌNG: sửa CSDL thôi chưa đủ. Client giữ CACHE bảng `part` và `npc_template`,
--  chỉ tải lại khi thấy số hiệu bản dữ liệu đổi. Bản jar đi kèm đã tăng
--  vsData 30 -> 31 (mang bảng `part`) và vsMap 11 -> 12 (mang `npc_template`).
--  Chạy file này mà vẫn chạy jar cũ thì lò vẫn TÀNG HÌNH: có tên, có mũi tên chọn,
--  không có hình — đúng hiện tượng đã gặp.
--
--  Ai CHƯA chạy patch 84 thì không cần file này — patch 84 đã sửa sẵn, chạy nó là đủ.
--  Ai ĐÃ chạy patch 84 bản cũ thì chạy file này (hoặc chạy lại cả patch 84, cũng ra
--  kết quả y hệt vì khối part của nó là DELETE rồi INSERT).
--
-- ---------------------------------------------------------------------
-- TRÌNH TỰ CHẠY
-- ---------------------------------------------------------------------
--   1) TẮT SERVER.
--   2) CHỌN ĐÚNG DATABASE `team2026` TRƯỚC KHI CHẠY. Trong phpMyAdmin: bấm vào tên
--      `team2026` ở khung bên trái, rồi mới mở thẻ SQL.
--      Dòng lệnh: mysql -u root -p team2026 < 86-sua-icon-lo-luyen-dan.sql
--   3) Chạy cả file. Chạy lại nhiều lần vẫn an toàn.
--   4) Xem khối (3): cột `dat` phải = 1.
-- =====================================================================

-- ---------------------------------------------------------------------
-- (1) KIỂM TRA TRƯỚC — đang là gì.
-- ---------------------------------------------------------------------
SELECT DATABASE() AS `database_dang_chon_phai_la_team2026`;
SELECT `id`, `TYPE`, `DATA` FROM `part` WHERE `id` = 2658;

-- ---------------------------------------------------------------------
-- (2) SỬA.
-- ---------------------------------------------------------------------
UPDATE `part`
   SET `DATA` = '[[32700,-8,-2],[32700,-8,-2],[32700,-8,-2]]'
 WHERE `id` = 2658 AND `DATA` <> '[[32700,-8,-2],[32700,-8,-2],[32700,-8,-2]]';

-- ---------------------------------------------------------------------
-- (3) KIỂM TRA SAU — cột `dat` phải = 1.
-- ---------------------------------------------------------------------
SELECT 'part 2658 dung icon 32700, da chinh toa do' AS `muc`,
       ((SELECT COUNT(*) FROM `part`
          WHERE `id` = 2658 AND `DATA` = '[[32700,-8,-2],[32700,-8,-2],[32700,-8,-2]]') = 1) AS `dat`
UNION ALL
SELECT 'khong con part nao tham chieu icon > 32767',
       ((SELECT COUNT(*) FROM `part` WHERE `DATA` LIKE '%33001%') = 0);
