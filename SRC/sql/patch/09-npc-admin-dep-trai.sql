-- =====================================================================
-- 09-npc-admin-dep-trai.sql  —  NPC "ADMIN Đẹp Trai" ĐỔI VND Ở NHÀ 3 HÀNH TINH
-- Database: team2026 (MariaDB 10.4)        Sinh ngày: 2026-09-19
--
-- Bàn giao: docs/4-trien-khai/44-npc-admin-dep-trai.md
-- Code đi kèm: consts/ConstNpc.ADMIN_DEP_TRAI (= 85), npc_list/AdminDepTrai.java,
--              npc/NpcFactory.java, data/DataGame.vsMap 2 -> 3.
--
-- ---------------------------------------------------------------------
-- FILE NÀY LÀM GÌ
-- ---------------------------------------------------------------------
--  (a) Thêm dòng `npc_template` id 85 "ADMIN Đẹp Trai",
--      ngoại hình = cải trang "CT Goku SSJ Blue" (item_template 1590):
--      head 1457 / body 1459 / leg 1460.
--      avatar 13268 = `head_avatar`.avatar_id của head 1457 (tra trong dump).
--  (b) Nối bộ ba [85, x, y] vào cuối cột `map_template`.`npcs` của
--      map 21 Nhà Gôhan, 22 Nhà Moori, 23 Nhà Broly. GIỮ NGUYÊN mọi NPC cũ.
--
-- VÌ SAO LÀ ID 85 (KHÔNG PHẢI 111):
--  * Client và NpcFactory.createNPC tra template theo VỊ TRÍ trong danh sách
--    npc_template (Manager.NPC_TEMPLATES.get(tempId); DataGame.updateMap gửi
--    cả danh sách theo thứ tự) => id PHẢI bằng vị trí. id 0..84 liên tục, 85
--    là ô trống đầu tiên. id 111 sẽ nằm ở vị trí 93 -> sai hình / văng lỗi.
--  * Zone ghi tempId bằng writeByte, Manager đọc Byte.parseByte / getByte
--    => id phải <= 127. 85 thoả.
--  * Hệ quả phụ: 8 dòng 103..110 (vốn đã lệch vị trí, không map nào dùng)
--    dời từ vị trí 85..92 sang 86..93 — vẫn không dùng được như trước.
--
-- TOẠ ĐỘ (kiểm bằng đúng thuật toán server: Manager.readTileMap +
-- data/map/tile_set_Info + Map.yPhysicInTop): cả ba map rộng 768 px, nền
-- phẳng y=336 liên tục từ x=24 đến x=743, yPhysicInTop(x,336) == 336.
--   map 21: x=636  (Bò Mộng 573 -> 63 px, Quả trứng 700 -> 64 px; cửa ra 456..528)
--   map 22: x=444  (Đậu thần 372 -> 72 px, Ông Moori 516 -> 72 px; cửa ra 168..240)
--   map 23: x=636  (Dưa hấu/Bò Mộng 570 -> 66 px, Quả trứng 700 -> 64 px; cửa 432..504)
-- Mọi khoảng cách > 60 px (bán kính Map.getNpc) => không mở nhầm NPC.
--
-- ---------------------------------------------------------------------
-- TRÌNH TỰ CHẠY
-- ---------------------------------------------------------------------
--   1) TẮT SERVER (npc_template, map_template chỉ đọc lúc khởi động).
--   2) mysqldump -u root -p team2026 npc_template map_template > backup_09.sql
--   3) Chạy cả file. Chạy lại nhiều lần vẫn an toàn (idempotent).
--   4) Xem khối (3) KIỂM TRA SAU: mọi cột `dat` = 1.
--   5) Bật server bản code mới (vsMap = 3) -> client tự tải lại danh sách NPC.
-- =====================================================================

SET NAMES utf8mb4;

-- ---------------------------------------------------------------------
-- (1) KIỂM TRA TRƯỚC
-- ---------------------------------------------------------------------
-- Id 85 phải đang TRỐNG hoặc đã là "ADMIN Đẹp Trai" (do lần chạy trước).
-- Nếu `ten_hien_tai` là một NPC khác: DỪNG LẠI, đừng chạy tiếp.
SELECT 85 AS `id`,
       (SELECT `NAME` FROM `npc_template` WHERE `id` = 85) AS `ten_hien_tai`,
       (SELECT COUNT(*) FROM `npc_template` WHERE `id` < 85) AS `so_id_duoi_85_phai_la_85`;

-- Avatar theo head 1457 (mong đợi 13268).
SELECT `head_id`, `avatar_id` FROM `head_avatar` WHERE `head_id` = 1457;

-- Giá trị `npcs` hiện tại của 3 nhà.
SELECT `id`, `NAME`, `npcs` FROM `map_template` WHERE `id` IN (21, 22, 23) ORDER BY `id`;

-- ---------------------------------------------------------------------
-- (2) CẬP NHẬT
-- ---------------------------------------------------------------------

-- (2a) npc_template 85. Chạy lại chỉ ghi đè cùng giá trị.
INSERT INTO `npc_template` (`id`, `NAME`, `head`, `body`, `leg`, `avatar`)
VALUES (85, 'ADMIN Đẹp Trai', 1457, 1459, 1460,
        COALESCE((SELECT `avatar_id` FROM `head_avatar` WHERE `head_id` = 1457 LIMIT 1), 13268))
ON DUPLICATE KEY UPDATE
    `NAME`   = VALUES(`NAME`),
    `head`   = VALUES(`head`),
    `body`   = VALUES(`body`),
    `leg`    = VALUES(`leg`),
    `avatar` = VALUES(`avatar`);

-- (2b) Nối [85,x,y] vào cuối `npcs` — chỉ khi map CHƯA có NPC 85.
-- Mẫu kiểm '[85,' khớp đúng id 85 vì mỗi bộ ba bắt đầu bằng '[' + id + ','.
-- '[]' (map trống) cũng xử lý được, dù 3 map này hiện đều có NPC.
UPDATE `map_template`
   SET `npcs` = CASE
                  WHEN TRIM(`npcs`) = '[]' THEN '[[85,636,336]]'
                  ELSE CONCAT(LEFT(TRIM(`npcs`), CHAR_LENGTH(TRIM(`npcs`)) - 1), ',[85,636,336]]')
                END
 WHERE `id` = 21 AND `npcs` NOT LIKE '%[85,%';

UPDATE `map_template`
   SET `npcs` = CASE
                  WHEN TRIM(`npcs`) = '[]' THEN '[[85,444,336]]'
                  ELSE CONCAT(LEFT(TRIM(`npcs`), CHAR_LENGTH(TRIM(`npcs`)) - 1), ',[85,444,336]]')
                END
 WHERE `id` = 22 AND `npcs` NOT LIKE '%[85,%';

UPDATE `map_template`
   SET `npcs` = CASE
                  WHEN TRIM(`npcs`) = '[]' THEN '[[85,636,336]]'
                  ELSE CONCAT(LEFT(TRIM(`npcs`), CHAR_LENGTH(TRIM(`npcs`)) - 1), ',[85,636,336]]')
                END
 WHERE `id` = 23 AND `npcs` NOT LIKE '%[85,%';

-- ---------------------------------------------------------------------
-- (3) KIỂM TRA SAU — mọi cột `dat` phải = 1.
-- ---------------------------------------------------------------------
SELECT 'npc_template 85' AS `muc`,
       (SELECT COUNT(*) FROM `npc_template`
         WHERE `id` = 85 AND `NAME` = 'ADMIN Đẹp Trai'
           AND `head` = 1457 AND `body` = 1459 AND `leg` = 1460) AS `dat`
UNION ALL
SELECT CONCAT('map ', `id`, ' co dung 1 NPC 85'),
       ((CHAR_LENGTH(`npcs`) - CHAR_LENGTH(REPLACE(`npcs`, '[85,', ''))) / CHAR_LENGTH('[85,') = 1)
  FROM `map_template` WHERE `id` IN (21, 22, 23)
UNION ALL
SELECT 'id 0..85 lien tuc',
       ((SELECT COUNT(*) FROM `npc_template` WHERE `id` BETWEEN 0 AND 85) = 86);

SELECT `id`, `NAME`, `npcs` FROM `map_template` WHERE `id` IN (21, 22, 23) ORDER BY `id`;

-- ---------------------------------------------------------------------
-- (4) LÙI LẠI  (bỏ comment rồi chạy; TẮT SERVER trước)
--     Sau khi lùi, phải đưa code về bản không có NPC 85 hoặc để nguyên code
--     (NpcFactory chỉ tạo NPC 85 khi map có [85,x,y], nên để code cũng được).
-- ---------------------------------------------------------------------
-- UPDATE `map_template`
--    SET `npcs` = REPLACE(REPLACE(REPLACE(`npcs`, ',[85,636,336]', ''), ',[85,444,336]', ''), '[[85,636,336]]', '[]')
--  WHERE `id` IN (21, 22, 23);
-- UPDATE `map_template` SET `npcs` = REPLACE(`npcs`, '[[85,444,336]]', '[]') WHERE `id` = 22;
-- DELETE FROM `npc_template` WHERE `id` = 85 AND `NAME` = 'ADMIN Đẹp Trai';
-- SELECT `id`, `npcs` FROM `map_template` WHERE `id` IN (21, 22, 23);
