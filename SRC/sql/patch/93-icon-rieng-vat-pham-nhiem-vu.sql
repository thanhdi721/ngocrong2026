-- =====================================================================
-- 93-icon-rieng-vat-pham-nhiem-vu.sql — 21 VẬT PHẨM NHIỆM VỤ CÓ ICON RIÊNG
-- Database: team2026 (MariaDB 10.4)        Sinh ngày: 2026-09-28
--
-- ---------------------------------------------------------------------
-- VẤN ĐỀ
-- ---------------------------------------------------------------------
--   21 vật phẩm nhiệm vụ id 2009–2029 đang MƯỢN icon của món khác (patch 01 mục 4).
--
--   Ba cặp trùng icon nhau, nhìn y hệt trong hành trang:
--       icon  1421  2009 Mảnh Vỡ Hư Không       <-> 2014 Vỏ đạn khắc dấu
--       icon  6467  2018 Máy đo ký ức           <-> 2025 Mảnh Ký Ức Vỡ
--       icon  8620  2019 Lõi năng lượng Android <-> 2026 Mảnh Ký Ức Đóng Băng
--
--   Bốn món trùng với đồ người chơi gặp hằng ngày, rất dễ dùng nhầm:
--       2013 Hạt Giống Hy Vọng         trùng Đậu thần cấp 1
--       2011 Máy Dò Ký Ức              trùng Rada cấp 1
--       2012 Hộp Ký Ức Bị Đánh Cắp     trùng Hộp Capsule
--       2024 Lõi Ký Ức chưa hoàn chỉnh trùng Ngọc rồng Siêu Cấp
--
-- ---------------------------------------------------------------------
-- CÁCH SỬA
-- ---------------------------------------------------------------------
--   21 icon mới id 32705–32725 trong SRC/data/icon/x1..x4, mỗi icon đủ 4 mức
--   22x22 / 44x44 / 66x66 / 88x88 — đúng bội số 2/3/4, nền trong suốt.
--   Ảnh gốc: docs/icon-new-nhiem-vu/ ; prompt: docs/4-trien-khai/72-prompt-icon-nhiem-vu.md
--
--   Đổi sang id MỚI chứ không ghi đè lên icon đang mượn: client lưu ảnh theo id
--   vào bộ nhớ đệm, ghi đè thì máy cũ vẫn vẽ ảnh cũ, mà còn hỏng luôn icon món gốc.
--   Id icon phải <= 32767 vì gói tin ghi bằng short.
--
--   CHƯA LÀM: 2030 và 2031 (hai danh hiệu) vẫn mượn icon 11614 / 11617 vì chưa có
--   ảnh. Dải 32726–32727 để trống sẵn cho hai món đó.
--
--   PHẢI TĂNG `vsItem`? CÓ — bản jar đi kèm đã tăng 34 -> 35.
--
-- ---------------------------------------------------------------------
-- TRÌNH TỰ CHẠY
-- ---------------------------------------------------------------------
--   1) TẮT SERVER.
--   2) Chép đè thư mục `SRC/data/icon` (84 file mới: 21 icon x 4 mức).
--   3) CHỌN ĐÚNG DATABASE `team2026`. Chắc ăn nhất:
--        mysql -u root -p team2026 < 93-icon-rieng-vat-pham-nhiem-vu.sql
--   4) Chạy cả file. Chạy lại nhiều lần vẫn an toàn.
--   5) Xem khối (3): cả ba cột `dat` phải = 1.
--   6) Bật server bản jar mới (vsItem = 35).
-- =====================================================================

-- ---------------------------------------------------------------------
-- (0) CHẶN CHẠY NHẦM DATABASE
-- ---------------------------------------------------------------------
SELECT DATABASE() AS `database_dang_chon_phai_la_team2026`;
SET @co_bang := (SELECT COUNT(*) FROM `information_schema`.`tables`
                  WHERE `table_schema` = DATABASE() AND `table_name` = 'item_template');
SET @sql := IF(@co_bang = 0,
    'SELECT ''DUNG LAI: database dang chon KHONG co bang `item_template`. '
    'Hay bam vao team2026 o khung ben trai phpMyAdmin roi chay lai file nay. '
    'Cac lenh ben duoi se BAO LOI va KHONG ghi gi ca.'' AS `loi`',
    'SELECT ''database dung roi, chay tiep'' AS `ghi_chu`');
PREPARE st FROM @sql; EXECUTE st; DEALLOCATE PREPARE st;

-- ---------------------------------------------------------------------
-- (1) KIỂM TRA TRƯỚC
-- ---------------------------------------------------------------------
SELECT COUNT(*) AS `so_mon_2009_2029_phai_la_21`
  FROM `item_template` WHERE `id` BETWEEN 2009 AND 2029;

SELECT `icon_id`, COUNT(*) AS `so_mon`,
       GROUP_CONCAT(CONCAT(`id`, ':', `NAME`) ORDER BY `id` SEPARATOR '  |  ') AS `cap_dang_trung`
  FROM `item_template` WHERE `id` BETWEEN 2009 AND 2029
 GROUP BY `icon_id` HAVING `so_mon` > 1;

-- ---------------------------------------------------------------------
-- (2) SỬA
-- ---------------------------------------------------------------------
UPDATE `item_template` SET `icon_id` = 32705 WHERE `id` = 2009; -- Mảnh Vỡ Hư Không          (cũ 1421)
UPDATE `item_template` SET `icon_id` = 32706 WHERE `id` = 2010; -- Kỷ Vật Của Ông            (cũ 9067)
UPDATE `item_template` SET `icon_id` = 32707 WHERE `id` = 2011; -- Máy Dò Ký Ức              (cũ 1089)
UPDATE `item_template` SET `icon_id` = 32708 WHERE `id` = 2012; -- Hộp Ký Ức Bị Đánh Cắp     (cũ 7222)
UPDATE `item_template` SET `icon_id` = 32709 WHERE `id` = 2013; -- Hạt Giống Hy Vọng         (cũ 241)
UPDATE `item_template` SET `icon_id` = 32710 WHERE `id` = 2014; -- Vỏ đạn khắc dấu           (cũ 1421)
UPDATE `item_template` SET `icon_id` = 32711 WHERE `id` = 2015; -- Búa rèn cũ                (cũ 1417)
UPDATE `item_template` SET `icon_id` = 32712 WHERE `id` = 2016; -- Thẻ tiền thưởng Granola   (cũ 5428)
UPDATE `item_template` SET `icon_id` = 32713 WHERE `id` = 2017; -- Biên bản truy nã Ngân Hà  (cũ 5207)
UPDATE `item_template` SET `icon_id` = 32714 WHERE `id` = 2018; -- Máy đo ký ức              (cũ 6467)
UPDATE `item_template` SET `icon_id` = 32715 WHERE `id` = 2019; -- Lõi năng lượng Android    (cũ 8620)
UPDATE `item_template` SET `icon_id` = 32716 WHERE `id` = 2020; -- Mẫu kim loại có ký ức     (cũ 2288)
UPDATE `item_template` SET `icon_id` = 32717 WHERE `id` = 2021; -- Mảnh giáp khắc tên        (cũ 10197)
UPDATE `item_template` SET `icon_id` = 32718 WHERE `id` = 2022; -- Thẻ từ phòng thí nghiệm   (cũ 9406)
UPDATE `item_template` SET `icon_id` = 32719 WHERE `id` = 2023; -- Bản thiết kế bản sao      (cũ 12846)
UPDATE `item_template` SET `icon_id` = 32720 WHERE `id` = 2024; -- Lõi Ký Ức chưa hoàn chỉnh (cũ 9650)
UPDATE `item_template` SET `icon_id` = 32721 WHERE `id` = 2025; -- Mảnh Ký Ức Vỡ             (cũ 6467)
UPDATE `item_template` SET `icon_id` = 32722 WHERE `id` = 2026; -- Mảnh Ký Ức Đóng Băng      (cũ 8620)
UPDATE `item_template` SET `icon_id` = 32723 WHERE `id` = 2027; -- Mảnh Bùa Babiđây          (cũ 7743)
UPDATE `item_template` SET `icon_id` = 32724 WHERE `id` = 2028; -- Lõi Phép Babiđây          (cũ 5829)
UPDATE `item_template` SET `icon_id` = 32725 WHERE `id` = 2029; -- Ống nghiệm Myuu           (cũ 6849)

-- ---------------------------------------------------------------------
-- (3) KIỂM TRA SAU — cả ba cột `dat` phải = 1.
-- ---------------------------------------------------------------------
SELECT 'du 21 mon da doi sang dai 32705-32725' AS `muc`,
       ((SELECT COUNT(*) FROM `item_template`
          WHERE `id` BETWEEN 2009 AND 2029
            AND `icon_id` = `id` + 30696) = 21) AS `dat`
UNION ALL
SELECT 'khong con cap nao trung icon trong nhom nhiem vu',
       ((SELECT COUNT(*) FROM (
            SELECT `icon_id` FROM `item_template`
             WHERE `id` BETWEEN 2009 AND 2029
             GROUP BY `icon_id` HAVING COUNT(*) > 1) AS `x`) = 0)
UNION ALL
SELECT 'khong mon nao khac dung chung 21 icon moi',
       ((SELECT COUNT(*) FROM `item_template`
          WHERE `icon_id` BETWEEN 32705 AND 32725
            AND `id` NOT BETWEEN 2009 AND 2029) = 0);

SELECT `id`, `NAME`, `icon_id` FROM `item_template`
 WHERE `id` BETWEEN 2009 AND 2031 ORDER BY `id`;

-- ---------------------------------------------------------------------
-- (4) LÙI LẠI — trả về icon mượn như cũ, rồi trả vsItem về 34 và build lại.
-- ---------------------------------------------------------------------
-- UPDATE `item_template` SET `icon_id` =  1421 WHERE `id` IN (2009, 2014);
-- UPDATE `item_template` SET `icon_id` =  9067 WHERE `id` = 2010;
-- UPDATE `item_template` SET `icon_id` =  1089 WHERE `id` = 2011;
-- UPDATE `item_template` SET `icon_id` =  7222 WHERE `id` = 2012;
-- UPDATE `item_template` SET `icon_id` =   241 WHERE `id` = 2013;
-- UPDATE `item_template` SET `icon_id` =  1417 WHERE `id` = 2015;
-- UPDATE `item_template` SET `icon_id` =  5428 WHERE `id` = 2016;
-- UPDATE `item_template` SET `icon_id` =  5207 WHERE `id` = 2017;
-- UPDATE `item_template` SET `icon_id` =  6467 WHERE `id` IN (2018, 2025);
-- UPDATE `item_template` SET `icon_id` =  8620 WHERE `id` IN (2019, 2026);
-- UPDATE `item_template` SET `icon_id` =  2288 WHERE `id` = 2020;
-- UPDATE `item_template` SET `icon_id` = 10197 WHERE `id` = 2021;
-- UPDATE `item_template` SET `icon_id` =  9406 WHERE `id` = 2022;
-- UPDATE `item_template` SET `icon_id` = 12846 WHERE `id` = 2023;
-- UPDATE `item_template` SET `icon_id` =  9650 WHERE `id` = 2024;
-- UPDATE `item_template` SET `icon_id` =  7743 WHERE `id` = 2027;
-- UPDATE `item_template` SET `icon_id` =  5829 WHERE `id` = 2028;
-- UPDATE `item_template` SET `icon_id` =  6849 WHERE `id` = 2029;
