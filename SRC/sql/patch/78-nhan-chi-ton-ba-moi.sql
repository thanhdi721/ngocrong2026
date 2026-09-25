-- =====================================================================
-- 78-nhan-chi-ton-ba-moi.sql — NHẪN CHÍ TÔN + NPC "BÀ MỐI" + CỘT thong_dit
-- Database: team2026 (MariaDB 10.4)        Sinh ngày: 2026-09-25
--
-- Code đi kèm:
--   boss/BossID (VEGETA_SIEU_THAN_GOD, GOKU_SIEU_THAN_GOD),
--   boss/BossesData, boss/sieu_than_god/BossSieuThanGod,
--   boss/Boss_Manager/BossManager.loadBoss,
--   services/ThongDitService, player/Player (soLanThong/soLanBiThong),
--   player/NPoint (cộng % vào hpMax/mpMax/dame),
--   matches/PVPService (thêm mục "Thông đít" vào menu thách đấu),
--   npc/NpcFactory, npc_list/BaMoi, consts/ConstNpc (BA_MOI = 87),
--   database/MrBlue + PlayerDAO + server/Manager (cột thong_dit),
--   data/DataGame (vsItem 26 -> 27, vsMap 8 -> 9).
--
-- Phải chạy TRƯỚC: patch 63 (npc_template 86), patch 75 (item 2262/2263).
--
-- ---------------------------------------------------------------------
-- FILE NÀY LÀM GÌ
-- ---------------------------------------------------------------------
--  (a0) NỚI CHỖ TRƯỚC ĐÃ: đổi `player`.`data_card` từ VARCHAR(10000) sang TEXT.
--      Bảng `player` đã KỊCH trần 65.535 byte một dòng của InnoDB — riêng các cột
--      VARCHAR cộng lại đã 65.040 byte — nên thêm BẤT KỲ cột nào cũng văng lỗi
--      1118 "Row size too large", kể cả cột TEXT (TEXT vẫn tốn ~20 byte con trỏ).
--      `data_card` khai VARCHAR(10000) utf8mb4 = 40.000 byte trong khi nó chỉ
--      chứa một chuỗi JSON ngắn; đổi sang TEXT trả lại ~40 KB ngân sách dòng,
--      vừa đủ cho cột mới và cho các lần thêm cột sau này.
--      An toàn: cột này không nằm trong index nào, PlayerDAO ghi bằng
--      JSONValue.toJSONString và MrBlue đọc bằng rs.getString — không phụ thuộc
--      kiểu VARCHAR. TEXT chứa tới 65.535 byte, nhiều hơn mức VARCHAR(10000)
--      utf8mb4 chứa được (40.000 byte), nên không mất dữ liệu.
--
--  (a) Thêm cột `player`.`thong_dit` lưu "số lần đi thông|số lần bị thông"
--      (NULL = chưa thông ai, đọc ra 0|0). Kiểu TEXT, cùng lý do ở trên.
--      Server cũng tự làm cả (a0) lẫn (a) lúc khởi động (Manager.ensureSchema),
--      chạy ở đây cho chắc.
--  (b) Thêm `item_template` 2264 "Nhẫn Chí Tôn", icon 30583 (ảnh mang từ
--      SrcBun, đã chép vào data/icon/x1..x4/30583.png). is_up_to_up = 1 để
--      gộp chồng trong hành trang. Vật phẩm chỉ có một việc: mỗi lần "thông
--      đít" tiêu đúng 1 cái.
--  (c) Thêm `npc_template` 87 "Bà Mối" — ngoại hình lấy từ "Cải trang Bunma
--      rực rỡ" (item 1476, part 1380/1381/1382), avatar 12387.
--  (d) Nối [87,380,288] vào cuối `map_template`.`npcs` của map 5 Đảo Kamê.
--      Chi Chi ở x=240, GoKu Nỗi Loạn ở x=310 -> Bà Mối x=380, cách 70 px,
--      hơn bán kính 60 px của Map.getNpc nên bấm không lộn NPC.
--
-- VÌ SAO LÀ ID 87:
--  * Client và NpcFactory tra template theo VỊ TRÍ trong npc_template
--    (Manager.NPC_TEMPLATES.get(tempId)) => id phải bằng vị trí. Sau patch 63
--    dải 0..86 liên tục, 87 là ô trống kế tiếp. (Các dòng 103..110 nằm sau
--    chỗ hở nên vốn đã không dùng được, không ảnh hưởng.)
--  * Zone ghi tempId bằng writeByte => id phải <= 127. 87 thoả.
--
-- VÌ SAO LÀ ITEM 2264:
--  * ITEM_TEMPLATES là ArrayList tra theo chỉ số => id phải liên tục.
--    Id lớn nhất hiện nay là 2263 (patch 75), nên 2264 là ô kế tiếp.
--
-- HAI BOSS MỚI KHÔNG CẦN DÒNG SQL NÀO: BossesData khai trong code, ngoại hình
-- dùng lại part của cải trang "Vegeta god" (2126/2127/2128) và "Goku SSJ God"
-- (2147/2148/2149) đã có từ patch 35.
--
-- ---------------------------------------------------------------------
-- TRÌNH TỰ CHẠY
-- ---------------------------------------------------------------------
--   1) TẮT SERVER (npc_template, map_template, item_template chỉ đọc lúc khởi động).
--   2) mysqldump -u root -p team2026 npc_template map_template item_template player > backup_78.sql
--   3) Chạy cả file. Chạy lại nhiều lần vẫn an toàn.
--   4) Xem khối (3): mọi cột `dat` phải = 1.
--   5) Bật server bản code mới (vsItem = 27, vsMap = 9) -> client tự tải lại.
-- =====================================================================

SET NAMES utf8mb4;

-- ---------------------------------------------------------------------
-- (1) KIỂM TRA TRƯỚC
-- ---------------------------------------------------------------------
-- `so_id_duoi_87` phải = 87 (dải 0..86 liên tục, không hở).
SELECT 87 AS `npc_id`,
       (SELECT `NAME` FROM `npc_template` WHERE `id` = 87) AS `ten_hien_tai`,
       (SELECT COUNT(*) FROM `npc_template` WHERE `id` < 87) AS `so_id_duoi_87_phai_la_87`;

-- Id vật phẩm lớn nhất phải là 2263 (patch 75) trước khi thêm 2264.
SELECT MAX(`id`) AS `item_id_lon_nhat_phai_la_2263` FROM `item_template`;

-- Cải trang gốc của Bà Mối và avatar theo head 1380 (mong đợi 12387).
SELECT `id`, `NAME`, `head`, `body`, `leg` FROM `item_template` WHERE `id` = 1476;
SELECT `head_id`, `avatar_id` FROM `head_avatar` WHERE `head_id` = 1380;

-- Part của hai boss mới phải có sẵn (patch 35).
SELECT `id`, `NAME`, `head`, `body`, `leg` FROM `item_template` WHERE `id` IN (2041, 2048);

-- NPC hiện có của đảo Kamê.
SELECT `id`, `NAME`, `npcs` FROM `map_template` WHERE `id` = 5;

-- ---------------------------------------------------------------------
-- (2) CẬP NHẬT
-- ---------------------------------------------------------------------

-- (2a0) Nới chỗ: data_card VARCHAR(10000) -> TEXT. Chỉ đổi khi còn là VARCHAR.
-- ALTER này viết lại cả bảng `player`, vài giây với vài nghìn nhân vật.
SET @kieu := (SELECT `data_type` FROM information_schema.columns
               WHERE table_schema = DATABASE() AND table_name = 'player' AND column_name = 'data_card');
SET @sql := IF(@kieu = 'varchar',
               'ALTER TABLE `player` MODIFY COLUMN `data_card` TEXT NULL',
               'SELECT ''data_card da la TEXT''');
PREPARE st FROM @sql; EXECUTE st; DEALLOCATE PREPARE st;

-- (2a) Cột nhớ số lần thông / bị thông. Viết kiểu này để chạy được cả MariaDB lẫn
-- MySQL 8 (MySQL 8 không hiểu "ADD COLUMN IF NOT EXISTS"), chạy lại vẫn an toàn.
SET @co := (SELECT COUNT(*) FROM information_schema.columns
             WHERE table_schema = DATABASE() AND table_name = 'player' AND column_name = 'thong_dit');
SET @sql := IF(@co = 0,
               'ALTER TABLE `player` ADD COLUMN `thong_dit` TEXT NULL',
               'SELECT ''cot thong_dit da co''');
PREPARE st FROM @sql; EXECUTE st; DEALLOCATE PREPARE st;

-- (2b) Vật phẩm 2264 "Nhẫn Chí Tôn".
DELETE FROM `item_template` WHERE `id` = 2264;
INSERT INTO `item_template` (`id`, `TYPE`, `gender`, `NAME`, `description`, `level`, `icon_id`,
                             `part`, `is_up_to_up`, `power_require`, `gold`, `gem`, `head`, `body`, `leg`) VALUES
(2264, 27, 3, 'Nhẫn Chí Tôn', 'Dùng để thông đít đồng chí của bạn', 0, 30583, -1, 1, 0, 0, 0, -1, -1, -1);

-- (2c) npc_template 87 "Bà Mối". Chạy lại chỉ ghi đè cùng giá trị.
INSERT INTO `npc_template` (`id`, `NAME`, `head`, `body`, `leg`, `avatar`)
VALUES (87, 'Bà Mối', 1380, 1381, 1382,
        COALESCE((SELECT `avatar_id` FROM `head_avatar` WHERE `head_id` = 1380 LIMIT 1), 12387))
ON DUPLICATE KEY UPDATE
    `NAME`   = VALUES(`NAME`),
    `head`   = VALUES(`head`),
    `body`   = VALUES(`body`),
    `leg`    = VALUES(`leg`),
    `avatar` = VALUES(`avatar`);

-- (2d) Nối [87,380,288] vào cuối `npcs` của map 5 — chỉ khi map CHƯA có NPC 87.
UPDATE `map_template`
   SET `npcs` = CASE
                  WHEN TRIM(`npcs`) = '[]' THEN '[[87,380,288]]'
                  ELSE CONCAT(LEFT(TRIM(`npcs`), CHAR_LENGTH(TRIM(`npcs`)) - 1), ',[87,380,288]]')
                END
 WHERE `id` = 5 AND `npcs` NOT LIKE '%[87,%';

-- ---------------------------------------------------------------------
-- (3) KIỂM TRA SAU — mọi cột `dat` phải = 1.
-- ---------------------------------------------------------------------
SELECT 'item 2264 Nhan Chi Ton' AS `muc`,
       (SELECT COUNT(*) FROM `item_template`
         WHERE `id` = 2264 AND `NAME` = 'Nhẫn Chí Tôn' AND `icon_id` = 30583 AND `is_up_to_up` = 1) AS `dat`
UNION ALL
SELECT 'item_template id lien tuc toi 2264',
       ((SELECT COUNT(*) FROM `item_template` WHERE `id` BETWEEN 0 AND 2264) = 2265)
UNION ALL
SELECT 'npc_template 87 Ba Moi',
       (SELECT COUNT(*) FROM `npc_template`
         WHERE `id` = 87 AND `NAME` = 'Bà Mối' AND `head` = 1380 AND `body` = 1381 AND `leg` = 1382)
UNION ALL
SELECT 'id npc 0..87 lien tuc',
       ((SELECT COUNT(*) FROM `npc_template` WHERE `id` BETWEEN 0 AND 87) = 88)
UNION ALL
SELECT 'map 5 co dung 1 NPC 87',
       ((CHAR_LENGTH(`npcs`) - CHAR_LENGTH(REPLACE(`npcs`, '[87,', ''))) / CHAR_LENGTH('[87,') = 1)
  FROM `map_template` WHERE `id` = 5
UNION ALL
SELECT 'data_card da doi sang TEXT',
       ((SELECT `data_type` FROM information_schema.columns
          WHERE table_schema = DATABASE() AND table_name = 'player'
            AND column_name = 'data_card') = 'text')
UNION ALL
SELECT 'co cot player.thong_dit',
       ((SELECT COUNT(*) FROM information_schema.columns
          WHERE table_schema = DATABASE() AND table_name = 'player'
            AND column_name = 'thong_dit') = 1);

SELECT `id`, `NAME`, `npcs` FROM `map_template` WHERE `id` = 5;

-- ---------------------------------------------------------------------
-- (4) LÙI LẠI  (bỏ comment rồi chạy; TẮT SERVER trước)
-- ---------------------------------------------------------------------
-- UPDATE `map_template` SET `npcs` = REPLACE(REPLACE(`npcs`, ',[87,380,288]', ''), '[[87,380,288]]', '[]') WHERE `id` = 5;
-- DELETE FROM `npc_template` WHERE `id` = 87 AND `NAME` = 'Bà Mối';
-- DELETE FROM `item_template` WHERE `id` = 2264;
-- UPDATE `player` SET `thong_dit` = NULL;
-- SELECT `id`, `npcs` FROM `map_template` WHERE `id` = 5;
