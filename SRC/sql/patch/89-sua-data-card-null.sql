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
--   2) CHỌN ĐÚNG DATABASE `team2026`:
--        mysql -u root -p team2026 < 89-sua-data-card-null.sql
--   3) Chạy cả file. Chạy lại nhiều lần vẫn an toàn.
--   4) Xem khối (3): `con_nhan_vat_hong` phải = 0.
--   5) Bật server BẢN JAR MỚI (nếu chạy jar cũ thì nhân vật tạo sau lại hỏng tiếp).
-- =====================================================================

-- ---------------------------------------------------------------------
-- (1) KIỂM TRA TRƯỚC — có bao nhiêu nhân vật đang hỏng.
-- ---------------------------------------------------------------------
SELECT DATABASE() AS `database_dang_chon_phai_la_team2026`;

SELECT COUNT(*) AS `so_nhan_vat_data_card_NULL`
  FROM `player` WHERE `data_card` IS NULL;

SELECT `id`, `account_id`, `name`, `create_time`
  FROM `player` WHERE `data_card` IS NULL
 ORDER BY `id` DESC LIMIT 20;

-- Kiểu cột hiện tại (sau patch 78 phải là `text`).
SELECT `COLUMN_TYPE`, `IS_NULLABLE`, `COLUMN_DEFAULT`
  FROM `information_schema`.`COLUMNS`
 WHERE `TABLE_SCHEMA` = DATABASE() AND `TABLE_NAME` = 'player'
   AND `COLUMN_NAME` = 'data_card';

-- ---------------------------------------------------------------------
-- (2) VÁ.
-- ---------------------------------------------------------------------
-- Nhận cả NULL lẫn chuỗi rỗng / chuỗi rác — bộ đọc JSON đều trả null cho mấy thứ đó.
UPDATE `player`
   SET `data_card` = '[]'
 WHERE `data_card` IS NULL
    OR TRIM(`data_card`) = ''
    OR TRIM(`data_card`) NOT LIKE '[%';

-- ---------------------------------------------------------------------
-- (3) KIỂM TRA SAU — `con_nhan_vat_hong` phải = 0.
-- ---------------------------------------------------------------------
SELECT (SELECT COUNT(*) FROM `player`
         WHERE `data_card` IS NULL OR TRIM(`data_card`) = ''
            OR TRIM(`data_card`) NOT LIKE '[%')        AS `con_nhan_vat_hong`,
       (SELECT COUNT(*) FROM `player`)                 AS `tong_nhan_vat`;
