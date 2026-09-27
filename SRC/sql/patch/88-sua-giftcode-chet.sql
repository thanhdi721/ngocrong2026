-- =====================================================================
-- 88-sua-giftcode-chet.sql — SỬA GIFTCODE ĐẶT count_left = -1 NÊN KHÔNG DÙNG ĐƯỢC
-- Database: team2026 (MariaDB 10.4)        Sinh ngày: 2026-09-27
--
-- ---------------------------------------------------------------------
-- VẤN ĐỀ
-- ---------------------------------------------------------------------
--  `GiftCodeManager.checkUseGiftCode` chặn bằng:
--
--      if (giftCode.countLeft <= 0) { "Giftcode đã hết"; return null; }
--
--  Tức `count_left = -1` KHÔNG phải "không giới hạn" — nó làm mã chết ngay, nhập vào chỉ
--  nhận được câu "Giftcode đã hết". Patch 76 ghi chú nhầm là "số lượt không giới hạn", nên
--  ba mã daps001 / daps002 / datay01 từ đó tới giờ CHƯA BAO GIỜ dùng được.
--
--  File này đổi mọi mã đang để `count_left` âm (hoặc 0) thành 99.999 lượt.
--  Mỗi tài khoản vẫn chỉ dùng được MỘT LẦN mỗi mã — cái đó do `player`.`giftcode` giữ,
--  không liên quan `count_left`.
--
-- ---------------------------------------------------------------------
-- TRÌNH TỰ CHẠY
-- ---------------------------------------------------------------------
--   1) CHỌN ĐÚNG DATABASE `team2026`:
--        mysql -u root -p team2026 < 88-sua-giftcode-chet.sql
--   2) Chạy cả file. Chạy lại nhiều lần vẫn an toàn.
--   3) KHÔNG cần tắt server — nhưng server đã nạp `listGiftCode` lúc khởi động, nên phải
--      KHỞI ĐỘNG LẠI thì số lượt mới có hiệu lực.
--   4) Xem khối (3): `con_ma_chet` phải = 0.
-- =====================================================================

-- ---------------------------------------------------------------------
-- (1) KIỂM TRA TRƯỚC — mã nào đang chết.
-- ---------------------------------------------------------------------
SELECT DATABASE() AS `database_dang_chon_phai_la_team2026`;
SELECT `id`, `code`, `count_left`, `expired`
  FROM `giftcode`
 WHERE `count_left` <= 0
 ORDER BY `id`;

-- ---------------------------------------------------------------------
-- (2) SỬA.
-- ---------------------------------------------------------------------
UPDATE `giftcode`
   SET `count_left` = 99999
 WHERE `count_left` <= 0;

-- ---------------------------------------------------------------------
-- (3) KIỂM TRA SAU — `con_ma_chet` phải = 0.
-- ---------------------------------------------------------------------
SELECT (SELECT COUNT(*) FROM `giftcode` WHERE `count_left` <= 0) AS `con_ma_chet`,
       (SELECT COUNT(*) FROM `giftcode`) AS `tong_so_ma`;

SELECT `id`, `code`, `count_left` FROM `giftcode` ORDER BY `id`;
