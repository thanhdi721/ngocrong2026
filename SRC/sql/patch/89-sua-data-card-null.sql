-- =====================================================================
-- 89-sua-data-card-null.sql — VÁ NHÂN VẬT KHÔNG ĐĂNG NHẬP ĐƯỢC
-- Database: team2026 (MariaDB 10.4)        Sinh ngày: 2026-09-27
--
-- ---------------------------------------------------------------------
-- TRIỆU CHỨNG
-- ---------------------------------------------------------------------
--   Tạo nhân vật xong, đăng nhập là server văng:
--
--     java.lang.NullPointerException: Cannot invoke "org.json.simple.JSONArray.size()"
--         because "dataArray" is null
--         at nro.models.database.MrBlue.loadPlayer(MrBlue.java:1026)
--
--   Người chơi kẹt luôn, thử lại bao nhiêu lần cũng vậy.
--
-- ---------------------------------------------------------------------
-- NGUYÊN NHÂN
-- ---------------------------------------------------------------------
--   Cột `player`.`data_card` bản gốc là:
--       VARCHAR(10000) NOT NULL DEFAULT '[]'
--
--   Patch 78 đổi nó sang TEXT để nới chỗ cho dòng `player` (bảng đó đã sát trần
--   65.535 byte của InnoDB, không đổi thì KHÔNG thêm được cột `thong_dit`).
--   Nhưng TEXT trong MariaDB **không mang được DEFAULT**, nên sau khi đổi cột
--   thành `TEXT NULL` và MẤT giá trị mặc định '[]'.
--
--   `PlayerDAO` lúc tạo nhân vật KHÔNG ghi cột này, nó trông vào DEFAULT. Thế là
--   nhân vật mới có `data_card` = NULL, `JSONValue.parse(null)` trả null, và
--   `MrBlue.loadPlayer` gọi `.size()` trên null.
--
--   Bản jar mới đã vá cả hai đầu:
--     * PlayerDAO ghi thẳng '[]' khi tạo nhân vật;
--     * MrBlue coi NULL như danh sách rỗng, không văng nữa.
--
--   File này dọn nốt những nhân vật ĐÃ bị tạo hỏng trước đó.
--
-- ---------------------------------------------------------------------
-- TRÌNH TỰ CHẠY
-- ---------------------------------------------------------------------
--   1) TẮT SERVER.
--   2) CHỌN ĐÚNG DATABASE `team2026` TRƯỚC KHI CHẠY.
--      phpMyAdmin: bấm vào tên `team2026` ở khung BÊN TRÁI, rồi mới mở thẻ SQL / Import.
--        Đứng ở màn hình gốc hay ở `information_schema` mà chạy thì file này TỰ DỪNG
--        và in ra câu nhắc, chứ không sửa bậy vào database khác.
--      Dòng lệnh (chắc ăn nhất):
--        mysql -u root -p team2026 < 89-sua-data-card-null.sql
--   3) Chạy cả file. Chạy lại nhiều lần vẫn an toàn.
--   4) Xem khối (3): `con_nhan_vat_hong` phải = 0.
--   5) Bật server BẢN JAR MỚI (nếu chạy jar cũ thì nhân vật tạo sau lại hỏng tiếp).
-- =====================================================================

-- ---------------------------------------------------------------------
-- (1) KIỂM TRA TRƯỚC — có bao nhiêu nhân vật đang hỏng.
-- ---------------------------------------------------------------------
SELECT DATABASE() AS `database_dang_chon_phai_la_team2026`;

-- Hai câu dưới chỉ chạy khi đang ở đúng database có bảng `player`.
SET @co_bang := (SELECT COUNT(*) FROM `information_schema`.`tables`
                  WHERE `table_schema` = DATABASE() AND `table_name` = 'player');
SET @sql := IF(@co_bang = 0,
    'SELECT ''DUNG LAI: database dang chon KHONG co bang `player`.'' AS `loi`',
    'SELECT COUNT(*) AS `so_nhan_vat_data_card_NULL` FROM `player` WHERE `data_card` IS NULL');
PREPARE st FROM @sql; EXECUTE st; DEALLOCATE PREPARE st;

SET @sql := IF(@co_bang = 0,
    'SELECT ''(bo qua)'' AS `ghi_chu`',
    'SELECT `id`, `account_id`, `name`, `create_time` FROM `player` '
    'WHERE `data_card` IS NULL ORDER BY `id` DESC LIMIT 20');
PREPARE st FROM @sql; EXECUTE st; DEALLOCATE PREPARE st;

-- Kiểu cột hiện tại (sau patch 78 phải là `text`).
SELECT `COLUMN_TYPE`, `IS_NULLABLE`, `COLUMN_DEFAULT`
  FROM `information_schema`.`COLUMNS`
 WHERE `TABLE_SCHEMA` = DATABASE() AND `TABLE_NAME` = 'player'
   AND `COLUMN_NAME` = 'data_card';

-- ---------------------------------------------------------------------
-- (2) VÁ.
-- ---------------------------------------------------------------------
-- CHẶN CHẠY NHẦM DATABASE: câu UPDATE bám vào database ĐANG CHỌN. Nếu trong
-- phpMyAdmin bạn đang đứng ở `information_schema` (hoặc bất kỳ database nào khác)
-- thì nó nhắm vào bảng `player` của database ĐÓ — hoặc báo
-- "#1044 Access denied ... to database 'information_schema'", hoặc tệ hơn là sửa
-- nhầm dữ liệu của một máy chủ khác. Nên phải kiểm bảng `player` có thật trong
-- database đang chọn TRƯỚC, rồi mới quyết định chạy.
SET @co_bang := (SELECT COUNT(*) FROM `information_schema`.`tables`
                  WHERE `table_schema` = DATABASE() AND `table_name` = 'player');
SET @sql := IF(@co_bang = 0,
    'SELECT ''DUNG LAI: database dang chon KHONG co bang `player`. '
    'Hay bam vao team2026 o khung ben trai phpMyAdmin roi chay lai file nay.'' AS `loi`',
    'UPDATE `player` SET `data_card` = ''[]'' '
    'WHERE `data_card` IS NULL OR TRIM(`data_card`) = '''' OR TRIM(`data_card`) NOT LIKE ''[%''');
PREPARE st FROM @sql;
EXECUTE st;
DEALLOCATE PREPARE st;

-- ---------------------------------------------------------------------
-- (3) KIỂM TRA SAU — `con_nhan_vat_hong` phải = 0.
-- ---------------------------------------------------------------------
SET @sql := IF(@co_bang = 0,
    'SELECT ''chua chay gi ca - chon sai database'' AS `ket_qua`',
    'SELECT (SELECT COUNT(*) FROM `player` WHERE `data_card` IS NULL '
    'OR TRIM(`data_card`) = '''' OR TRIM(`data_card`) NOT LIKE ''[%'') AS `con_nhan_vat_hong`, '
    '(SELECT COUNT(*) FROM `player`) AS `tong_nhan_vat`');
PREPARE st FROM @sql;
EXECUTE st;
DEALLOCATE PREPARE st;
