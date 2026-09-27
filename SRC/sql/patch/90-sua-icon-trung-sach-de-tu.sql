-- =====================================================================
-- 90-sua-icon-trung-sach-de-tu.sql — HAI CẶP VẬT PHẨM ĐỆ TỬ TRÙNG ICON
-- Database: team2026 (MariaDB 10.4)        Sinh ngày: 2026-09-27
--
-- ---------------------------------------------------------------------
-- VẤN ĐỀ
-- ---------------------------------------------------------------------
--   Trong hành trang có hai cặp vật phẩm KHÁC NHAU mà hiện y hệt nhau:
--
--     icon 7099   403 "Nâng kỹ năng 2 đệ tử"  (mua ở shop)
--                2066 "Đổi Skill 2 Đệ Tử"
--
--     icon 7101   759 "Nâng kỹ năng 4 đệ tử"  (mua ở shop)
--                2123 "Nâng kỹ năng 5 đệ tử"  (thưởng vòng quay Thượng Đế)
--
--   Người chơi cầm hai món cạnh nhau không phân biệt nổi, rất dễ dùng nhầm.
--
--   Vì sao ra nông nỗi này:
--     * Bộ "Đổi Skill" dùng ảnh thẻ bài 27143-27146, nhưng 27143 CHƯA BAO GIỜ
--       được kéo về res (không có trong cả SUMO lẫn Bun), nên 2066 phải mượn tạm 7099.
--     * Bộ "Nâng kỹ năng" chỉ có 4 màu cuộn giấy 7098-7101 cho 4 chiêu; lúc patch 58
--       thêm chiêu 5 thì không còn màu nào nên mượn lại 7101.
--
-- ---------------------------------------------------------------------
-- CÁCH SỬA
-- ---------------------------------------------------------------------
--   Đã dựng hai ảnh mới trong `SRC/data/icon/x1..x4` bằng cách đổi tông màu
--   ảnh cùng bộ, nên nhìn vẫn đúng một nhà:
--
--     32702  cuộn giấy XANH DƯƠNG  -> 2123 "Nâng kỹ năng 5 đệ tử"
--     32703  thẻ bài XANH LỤC      -> 2066 "Đổi Skill 2 Đệ Tử"
--
--   Hai số này phải <= 32767 vì gói tin ghi icon bằng short (writeShort).
--   Icon lớn nhất đang dùng trước đó là 32701.
--
--   PHẢI BUILD LẠI JAR? Không. Nhưng PHẢI tăng `vsItem` để client tải lại bảng
--   vật phẩm — bản jar đi kèm đã tăng 32 -> 33. Chạy jar cũ thì client vẫn vẽ icon cũ.
--
-- ---------------------------------------------------------------------
-- TRÌNH TỰ CHẠY
-- ---------------------------------------------------------------------
--   1) TẮT SERVER.
--   2) CHỌN ĐÚNG DATABASE `team2026`. Chắc ăn nhất là dòng lệnh:
--        mysql -u root -p team2026 < 90-sua-icon-trung-sach-de-tu.sql
--   3) Chạy cả file. Chạy lại nhiều lần vẫn an toàn.
--   4) Xem khối (3): cả hai cột `dat` phải = 1.
--   5) Bật server bản jar mới (vsItem = 33).
-- =====================================================================

-- ---------------------------------------------------------------------
-- (0) CHẶN CHẠY NHẦM DATABASE
-- ---------------------------------------------------------------------
SELECT DATABASE() AS `database_dang_chon_phai_la_team2026`;
SET @co_bang := (SELECT COUNT(*) FROM `information_schema`.`tables`
                  WHERE `table_schema` = DATABASE() AND `table_name` = 'item_template');
SET @sql := IF(@co_bang = 0,
    'SELECT ''DUNG LAI: database dang chon KHONG co bang `item_template`. '
    'Hay chon team2026 roi chay lai. Cac lenh ben duoi se BAO LOI va KHONG ghi gi ca.'' AS `loi`',
    'SELECT ''database dung roi, chay tiep'' AS `ghi_chu`');
PREPARE st FROM @sql; EXECUTE st; DEALLOCATE PREPARE st;

-- ---------------------------------------------------------------------
-- (1) KIỂM TRA TRƯỚC — nhìn hai cặp đang trùng.
-- ---------------------------------------------------------------------
SELECT `icon_id`, COUNT(*) AS `so_mon`,
       GROUP_CONCAT(CONCAT(`id`, ':', `NAME`) ORDER BY `id` SEPARATOR '  |  ') AS `cac_mon`
  FROM `item_template`
 WHERE `id` IN (402, 403, 404, 759, 2066, 2067, 2068, 2069, 2123)
 GROUP BY `icon_id`
 HAVING `so_mon` > 1;

-- ---------------------------------------------------------------------
-- (2) SỬA.
-- ---------------------------------------------------------------------
UPDATE `item_template` SET `icon_id` = 32702 WHERE `id` = 2123;
UPDATE `item_template` SET `icon_id` = 32703 WHERE `id` = 2066;

-- ---------------------------------------------------------------------
-- (3) KIỂM TRA SAU — cả hai cột `dat` phải = 1.
-- ---------------------------------------------------------------------
SELECT 'hai mon da co icon rieng' AS `muc`,
       ((SELECT COUNT(*) FROM `item_template`
          WHERE (`id` = 2123 AND `icon_id` = 32702)
             OR (`id` = 2066 AND `icon_id` = 32703)) = 2) AS `dat`
UNION ALL
SELECT 'khong con cap nao trung icon trong nhom de tu',
       ((SELECT COUNT(*) FROM (
            SELECT `icon_id` FROM `item_template`
             WHERE `id` IN (402, 403, 404, 759, 2066, 2067, 2068, 2069, 2123)
             GROUP BY `icon_id` HAVING COUNT(*) > 1) AS `x`) = 0);

SELECT `id`, `NAME`, `icon_id` FROM `item_template`
 WHERE `id` IN (402, 403, 404, 759, 2123, 2066, 2067, 2068, 2069) ORDER BY `id`;
