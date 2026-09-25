-- =====================================================================
-- 63-npc-goku-noi-loan.sql — NPC "GoKu Nỗi Loạn" ĐỨNG CẠNH CHI CHI (ĐẢO KAMÊ)
-- Database: team2026 (MariaDB 10.4)        Sinh ngày: 2026-09-24
--
-- Code đi kèm: consts/ConstNpc.GOKU_NOI_LOAN (= 86), npc_list/GokuNoiLoan.java,
--              npc/NpcFactory.java, player/Player.auraNpc, database/MrBlue +
--              PlayerDAO (cột aura_npc), server/Manager.ensureSchema,
--              data/DataGame.vsMap 7 -> 8.
-- Phải chạy TRƯỚC: patch 62 (ảnh + dòng img_by_name của hào quang 95).
--
-- ---------------------------------------------------------------------
-- FILE NÀY LÀM GÌ
-- ---------------------------------------------------------------------
--  (a) Thêm cột `player`.`aura_npc` (mặc định -1 = tắt) để nhớ ai đang bật
--      hào quang. Server cũng tự thêm cột này lúc khởi động, chạy ở đây cho chắc.
--  (b) Thêm dòng `npc_template` id 86 "GoKu Nỗi Loạn", ngoại hình = cải trang
--      "Goku Nổi Loạn" (item_template 2103): head 2307 / body 2308 / leg 2309,
--      avatar 17778 = head_avatar của head 2307 (patch 55).
--  (c) Nối [86, 310, 288] vào cuối `map_template`.`npcs` của map 5 Đảo Kamê.
--      Chi Chi đứng ở (240, 288) nên NPC mới cách 70 px — hơn bán kính 60 px
--      của Map.getNpc, bấm không lộn NPC.
--
-- VÌ SAO LÀ ID 86:
--  * Client và NpcFactory tra template theo VỊ TRÍ trong npc_template
--    (Manager.NPC_TEMPLATES.get(tempId)) => id phải bằng vị trí. Hiện 0..85
--    liên tục (85 là ADMIN Đẹp Trai, patch 09), 86 là ô trống kế tiếp.
--  * Zone ghi tempId bằng writeByte => id phải <= 127. 86 thoả.
--
-- LƯU Ý: NPC chỉ mang được cải trang (head/body/leg). Gói tin NPC không có
-- chỗ cho đồ đeo lưng và hào quang, nên bản thân NPC KHÔNG cầm Thanh Long
-- Yển Nguyệt đao và KHÔNG có hào quang; hào quang là thứ NPC bật cho NGƯỜI CHƠI.
--
-- ---------------------------------------------------------------------
-- TRÌNH TỰ CHẠY
-- ---------------------------------------------------------------------
--   1) TẮT SERVER (npc_template, map_template chỉ đọc lúc khởi động).
--   2) mysqldump -u root -p team2026 npc_template map_template > backup_63.sql
--   3) Chạy cả file. Chạy lại nhiều lần vẫn an toàn.
--   4) Xem khối (3): mọi cột `dat` phải = 1.
--   5) Bật server bản code mới (vsMap = 8) -> client tự tải lại danh sách NPC.
-- =====================================================================

SET NAMES utf8mb4;

-- ---------------------------------------------------------------------
-- (1) KIỂM TRA TRƯỚC
-- ---------------------------------------------------------------------
-- `ten_hien_tai` phải RỖNG hoặc đã là "GoKu Nỗi Loạn"; `so_id_duoi_86` phải = 86.
SELECT 86 AS `id`,
       (SELECT `NAME` FROM `npc_template` WHERE `id` = 86) AS `ten_hien_tai`,
       (SELECT COUNT(*) FROM `npc_template` WHERE `id` < 86) AS `so_id_duoi_86_phai_la_86`;

-- Cải trang gốc và avatar theo head 2307 (mong đợi 17778).
SELECT `id`, `NAME`, `head`, `body`, `leg` FROM `item_template` WHERE `id` = 2103;
SELECT `head_id`, `avatar_id` FROM `head_avatar` WHERE `head_id` = 2307;

-- Hào quang 95 phải có dòng img_by_name (patch 62), nếu trống thì chạy patch 62 trước.
SELECT `NAME`, `n_frame` FROM `img_by_name` WHERE `NAME` IN ('aura_95_0', 'aura_95_1');

-- NPC hiện có của đảo Kamê.
SELECT `id`, `NAME`, `npcs` FROM `map_template` WHERE `id` = 5;

-- ---------------------------------------------------------------------
-- (2) CẬP NHẬT
-- ---------------------------------------------------------------------

-- (2a) Cột nhớ hào quang đang bật. Viết kiểu này để chạy được cả MariaDB lẫn MySQL 8
-- (MySQL 8 không hiểu "ADD COLUMN IF NOT EXISTS"), và chạy lại nhiều lần vẫn an toàn.
SET @co := (SELECT COUNT(*) FROM information_schema.columns
             WHERE table_schema = DATABASE() AND table_name = 'player' AND column_name = 'aura_npc');
SET @sql := IF(@co = 0,
               'ALTER TABLE `player` ADD COLUMN `aura_npc` INT NOT NULL DEFAULT -1',
               'SELECT ''cot aura_npc da co''');
PREPARE st FROM @sql; EXECUTE st; DEALLOCATE PREPARE st;

-- (2b) npc_template 86. Chạy lại chỉ ghi đè cùng giá trị.
INSERT INTO `npc_template` (`id`, `NAME`, `head`, `body`, `leg`, `avatar`)
VALUES (86, 'GoKu Nỗi Loạn', 2307, 2308, 2309,
        COALESCE((SELECT `avatar_id` FROM `head_avatar` WHERE `head_id` = 2307 LIMIT 1), 17778))
ON DUPLICATE KEY UPDATE
    `NAME`   = VALUES(`NAME`),
    `head`   = VALUES(`head`),
    `body`   = VALUES(`body`),
    `leg`    = VALUES(`leg`),
    `avatar` = VALUES(`avatar`);

-- (2c) Nối [86,310,288] vào cuối `npcs` của map 5 — chỉ khi map CHƯA có NPC 86.
UPDATE `map_template`
   SET `npcs` = CASE
                  WHEN TRIM(`npcs`) = '[]' THEN '[[86,310,288]]'
                  ELSE CONCAT(LEFT(TRIM(`npcs`), CHAR_LENGTH(TRIM(`npcs`)) - 1), ',[86,310,288]]')
                END
 WHERE `id` = 5 AND `npcs` NOT LIKE '%[86,%';

-- ---------------------------------------------------------------------
-- (3) KIỂM TRA SAU — mọi cột `dat` phải = 1.
-- ---------------------------------------------------------------------
SELECT 'npc_template 86' AS `muc`,
       (SELECT COUNT(*) FROM `npc_template`
         WHERE `id` = 86 AND `NAME` = 'GoKu Nỗi Loạn'
           AND `head` = 2307 AND `body` = 2308 AND `leg` = 2309) AS `dat`
UNION ALL
SELECT 'map 5 co dung 1 NPC 86',
       ((CHAR_LENGTH(`npcs`) - CHAR_LENGTH(REPLACE(`npcs`, '[86,', ''))) / CHAR_LENGTH('[86,') = 1)
  FROM `map_template` WHERE `id` = 5
UNION ALL
SELECT 'id 0..86 lien tuc',
       ((SELECT COUNT(*) FROM `npc_template` WHERE `id` BETWEEN 0 AND 86) = 87)
UNION ALL
SELECT 'co cot player.aura_npc',
       ((SELECT COUNT(*) FROM information_schema.columns
          WHERE table_schema = DATABASE() AND table_name = 'player'
            AND column_name = 'aura_npc') = 1);

SELECT `id`, `NAME`, `npcs` FROM `map_template` WHERE `id` = 5;

-- ---------------------------------------------------------------------
-- (4) LÙI LẠI  (bỏ comment rồi chạy; TẮT SERVER trước)
-- ---------------------------------------------------------------------
-- UPDATE `map_template` SET `npcs` = REPLACE(REPLACE(`npcs`, ',[86,310,288]', ''), '[[86,310,288]]', '[]') WHERE `id` = 5;
-- DELETE FROM `npc_template` WHERE `id` = 86 AND `NAME` = 'GoKu Nỗi Loạn';
-- UPDATE `player` SET `aura_npc` = -1;
-- SELECT `id`, `npcs` FROM `map_template` WHERE `id` = 5;
