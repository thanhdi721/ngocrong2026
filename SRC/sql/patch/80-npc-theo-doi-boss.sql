-- =====================================================================
-- 80-npc-theo-doi-boss.sql — NPC "Theo Dõi Boss" ĐỨNG BÊN TRÁI SANTA (ĐẢO KAMÊ)
-- Database: team2026 (MariaDB 10.4)        Sinh ngày: 2026-09-26
--
-- Code đi kèm: consts/ConstNpc.THEO_DOI_BOSS_NPC (= 88) + THEO_DOI_BOSS/…_TRANG,
--              npc_list/TheoDoiBoss, npc/NpcFactory,
--              boss/drop/BangRoiBoss + boss/drop/MucRoi (bảng rơi đọc chung),
--              cpanel/BossDropTableDialog + cpanel/BossTab (nút "Đồ rơi..."),
--              boss/Boss.die (gọi BangRoiBoss.roi),
--              data/DataGame (vsMap 9 -> 10).
--
-- Phải chạy TRƯỚC: patch 67 (part 2448–2450 + head_avatar), patch 78 (npc_template 87).
--
-- ---------------------------------------------------------------------
-- FILE NÀY LÀM GÌ
-- ---------------------------------------------------------------------
--  (a) Thêm `npc_template` 88 "Theo Dõi Boss" — ngoại hình lấy từ cải trang
--      "Siêu Goku Vô Cực" (item 2150, part 2448/2449/2450, mang từ SUMO ở
--      patch 67), avatar 18516.
--  (b) Nối [88,914,408] vào cuối `map_template`.`npcs` của map 5 Đảo Kamê.
--      Santa (npc 39) đứng ở (984, 408) -> NPC mới cách 70 px về BÊN TRÁI,
--      hơn bán kính 60 px của Map.getNpc nên bấm không lộn NPC.
--      Đã tính lại tile map 5: ô (914, 408) là nền đặc.
--
-- VÌ SAO LÀ ID 88:
--  Client và NpcFactory tra template theo VỊ TRÍ trong npc_template
--  (Manager.NPC_TEMPLATES.get(tempId)) => id phải bằng vị trí. Sau patch 78
--  dải 0..87 liên tục, 88 là ô trống kế tiếp. Zone ghi tempId bằng writeByte
--  nên id phải <= 127; 88 thoả.
--
-- NPC NÀY KHÔNG CẦN BẢNG DỮ LIỆU NÀO KHÁC: danh sách boss đọc thẳng từ
-- BossManager lúc chạy, bảng rơi đọc từ BangRoiBoss (file
-- data/bossdrop_table.json, cpanel ghi ra).
--
-- ---------------------------------------------------------------------
-- TRÌNH TỰ CHẠY
-- ---------------------------------------------------------------------
--   1) TẮT SERVER (npc_template, map_template chỉ đọc lúc khởi động).
--   2) mysqldump -u root -p team2026 npc_template map_template > backup_80.sql
--   3) Chạy cả file. Chạy lại nhiều lần vẫn an toàn.
--   4) Xem khối (3): mọi cột `dat` phải = 1.
--   5) Bật server bản code mới (vsMap = 10) -> client tự tải lại danh sách NPC.
-- =====================================================================

SET NAMES utf8mb4;

-- ---------------------------------------------------------------------
-- (1) KIỂM TRA TRƯỚC
-- ---------------------------------------------------------------------
-- `so_id_duoi_88` phải = 88 (dải 0..87 liên tục, không hở).
SELECT 88 AS `npc_id`,
       (SELECT `NAME` FROM `npc_template` WHERE `id` = 88) AS `ten_hien_tai`,
       (SELECT COUNT(*) FROM `npc_template` WHERE `id` < 88) AS `so_id_duoi_88_phai_la_88`;

-- Cải trang gốc và avatar theo head 2448 (mong đợi 18516).
SELECT `id`, `NAME`, `head`, `body`, `leg` FROM `item_template` WHERE `id` = 2150;
SELECT `head_id`, `avatar_id` FROM `head_avatar` WHERE `head_id` = 2448;

-- Santa phải đang đứng ở map 5 tại x = 984.
SELECT `id`, `NAME`, `npcs` FROM `map_template` WHERE `id` = 5;

-- ---------------------------------------------------------------------
-- (2) CẬP NHẬT
-- ---------------------------------------------------------------------

-- (2a) npc_template 88. Chạy lại chỉ ghi đè cùng giá trị.
INSERT INTO `npc_template` (`id`, `NAME`, `head`, `body`, `leg`, `avatar`)
VALUES (88, 'Theo Dõi Boss', 2448, 2449, 2450,
        COALESCE((SELECT `avatar_id` FROM `head_avatar` WHERE `head_id` = 2448 LIMIT 1), 18516))
ON DUPLICATE KEY UPDATE
    `NAME`   = VALUES(`NAME`),
    `head`   = VALUES(`head`),
    `body`   = VALUES(`body`),
    `leg`    = VALUES(`leg`),
    `avatar` = VALUES(`avatar`);

-- (2b) Nối [88,914,408] vào cuối `npcs` của map 5 — chỉ khi map CHƯA có NPC 88.
UPDATE `map_template`
   SET `npcs` = CASE
                  WHEN TRIM(`npcs`) = '[]' THEN '[[88,914,408]]'
                  ELSE CONCAT(LEFT(TRIM(`npcs`), CHAR_LENGTH(TRIM(`npcs`)) - 1), ',[88,914,408]]')
                END
 WHERE `id` = 5 AND `npcs` NOT LIKE '%[88,%';

-- ---------------------------------------------------------------------
-- (3) KIỂM TRA SAU — mọi cột `dat` phải = 1.
-- ---------------------------------------------------------------------
SELECT 'npc_template 88 Theo Doi Boss' AS `muc`,
       (SELECT COUNT(*) FROM `npc_template`
         WHERE `id` = 88 AND `NAME` = 'Theo Dõi Boss'
           AND `head` = 2448 AND `body` = 2449 AND `leg` = 2450) AS `dat`
UNION ALL
SELECT 'id npc 0..88 lien tuc',
       ((SELECT COUNT(*) FROM `npc_template` WHERE `id` BETWEEN 0 AND 88) = 89)
UNION ALL
SELECT 'map 5 co dung 1 NPC 88',
       ((CHAR_LENGTH(`npcs`) - CHAR_LENGTH(REPLACE(`npcs`, '[88,', ''))) / CHAR_LENGTH('[88,') = 1)
  FROM `map_template` WHERE `id` = 5
UNION ALL
SELECT 'du 3 part cua Sieu Goku Vo Cuc',
       ((SELECT COUNT(*) FROM `part` WHERE `id` IN (2448, 2449, 2450)) = 3);

SELECT `id`, `NAME`, `npcs` FROM `map_template` WHERE `id` = 5;

-- ---------------------------------------------------------------------
-- (4) LÙI LẠI  (bỏ comment rồi chạy; TẮT SERVER trước)
-- ---------------------------------------------------------------------
-- UPDATE `map_template` SET `npcs` = REPLACE(REPLACE(`npcs`, ',[88,914,408]', ''), '[[88,914,408]]', '[]') WHERE `id` = 5;
-- DELETE FROM `npc_template` WHERE `id` = 88 AND `NAME` = 'Theo Dõi Boss';
