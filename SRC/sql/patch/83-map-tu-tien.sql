-- =====================================================================
-- 83-map-tu-tien.sql — MAP TU TIÊN + 10 VẬT PHẨM + NPC "TU TIÊN"
-- Database: team2026 (MariaDB 10.4)        Sinh ngày: 2026-09-26
--
-- Bản thiết kế: docs/4-trien-khai/69-map-tu-tien-ban-thiet-ke.md
--
-- Code đi kèm:
--   tu_tien/TuTien (luật map, rơi Linh Thạch), boss/BossDropConfig (3 ô chỉnh cpanel),
--   npc_list/TuTienNPC, consts/ConstNpc (TU_TIEN = 89), npc/NpcFactory,
--   mob/Mob (trần sát thương, chặn exp, chặn vàng, hồi sinh riêng),
--   map/service/MapService.isMapTuTien, map/service/ChangeMapService (tự bật cờ đen),
--   services/PlayerService.hoiSinh (chặn hồi sinh), item/ItemTime + player/NPoint (bùa chí mạng),
--   services/InventoryService.putItemBody (ngọc bội chỉ đệ tử),
--   services_func/UseItem (dùng đan + bùa), data/DataGame (vsMap 10 -> 11, vsItem 30 -> 31).
--
-- Phải chạy TRƯỚC: patch 80 (npc_template 88), patch 82.
--
-- ---------------------------------------------------------------------
-- FILE NÀY LÀM GÌ
-- ---------------------------------------------------------------------
--  (a) Thêm `map_template` 208 "Nam Thiên Môn" — map tu tiên.
--      Bộ tile 13 + nền 12: ĐÚNG bộ mà Thần Điện / Tháp Karin / Hành tinh Kaio
--      đang dùng, nên KHÔNG phải thêm một file ảnh nào, client đã có sẵn.
--      File bản đồ `SRC/data/map/tile_map_data/208` lấy từ source SUMO (đã chép).
--      2 khu, mỗi khu 8 con quái 20 triệu máu -> tổng 16 con.
--  (b) Thêm 10 `item_template` 2266–2275 (Linh Thạch, 5 đan, 3 ngọc bội, 1 bùa).
--      ICON: 5 đan dùng bộ "bình luyện đan" 24883–24895 (cùng một bộ nên nhìn đồng bộ),
--      3 ngọc bội dùng ảnh bùa ngọc có tua đỏ. Ảnh gốc của bùa ngọc mang id 15533/15534/
--      15535 nhưng BÊN MÌNH ba id đó đã là quần áo, nên ĐÃ ĐÁNH SỐ LẠI thành
--      20260 / 20261 / 20262 (ba ô trống) trước khi chép vào data/icon.
--  (c) Thêm `npc_template` 89 "Tu Tiên", ngoại hình cải trang "Goku Thiên Sứ"
--      (item 2194, part 2580/2581/2582), avatar 19479.
--  (d) Nối [89,450,288] vào `map_template`.`npcs` của map 5 Đảo Kamê.
--
-- VÌ SAO CHỌN MAP 208:
--  Trong 5 map "tiên giới" của SUMO thì 208 rộng nhất (1200 × 600 px) và nền liền
--  một dải 1200 px, đủ chỗ rải 8 con cách nhau 137 px. (207 Thái Cực Điện chỉ 840 px,
--  nền liền 648 px — chật.)
--
-- VÌ SAO LÀ NPC ID 89:
--  Client và NpcFactory tra template theo VỊ TRÍ trong npc_template => id phải bằng
--  vị trí. Sau patch 80 dải 0..88 liên tục, 89 là ô trống kế tiếp. Zone ghi tempId
--  bằng writeByte nên id phải <= 127; 89 thoả.
--  Chỗ đứng x = 450 trên map 5: đã tính lại tile, là nền đặc ở y = 288, và cách
--  NPC gần nhất (Bà Mối x = 380) đúng 70 px > bán kính 60 px của Map.getNpc.
--
-- VÌ SAO LÀ ITEM 2266–2275:
--  ITEM_TEMPLATES tra theo chỉ số nên id phải liên tục. Id lớn nhất hiện là 2265.
--
-- TRẦN NGÂN SÁCH: sau patch này bảng vật phẩm nặng ~63,3 KB mỗi gói (trần 65 KB),
-- còn chỗ cho khoảng 30 món nữa.
--
-- ---------------------------------------------------------------------
-- TRÌNH TỰ CHẠY
-- ---------------------------------------------------------------------
--   1) TẮT SERVER.
--   2) mysqldump -u root -p team2026 map_template item_template npc_template > backup_83.sql
--   3) Chạy cả file. Chạy lại nhiều lần vẫn an toàn.
--   4) Xem khối (3): mọi cột `dat` phải = 1.
--   5) Bật server bản code mới (vsMap = 11, vsItem = 31).
-- =====================================================================

SET NAMES utf8mb4;

-- ---------------------------------------------------------------------
-- (1) KIỂM TRA TRƯỚC
-- ---------------------------------------------------------------------
SELECT MAX(`id`) AS `item_id_lon_nhat_phai_la_2265` FROM `item_template`;
SELECT (SELECT COUNT(*) FROM `npc_template` WHERE `id` < 89) AS `so_id_duoi_89_phai_la_89`,
       (SELECT `NAME` FROM `npc_template` WHERE `id` = 89) AS `ten_hien_tai`;
SELECT `id`, `NAME` FROM `map_template` WHERE `id` = 208;
-- Cải trang gốc của NPC và avatar theo head 2580 (mong đợi 19479).
SELECT `id`, `NAME`, `head`, `body`, `leg` FROM `item_template` WHERE `id` = 2194;
SELECT `head_id`, `avatar_id` FROM `head_avatar` WHERE `head_id` = 2580;
-- Map mẫu dùng chung bộ tile 13 / nền 12 (để đối chiếu).
SELECT `id`, `NAME`, `data`, `tile_id`, `bg_id` FROM `map_template` WHERE `id` IN (45, 46, 48);

-- ---------------------------------------------------------------------
-- (2) CẬP NHẬT
-- ---------------------------------------------------------------------

-- (2a) Map 208 "Nam Thiên Môn".
--   data = [type, planet, bg_type, tile_id, bg_id] — giữ đúng thứ tự như các map khác.
--   zones = 2  -> 2 khu, mỗi khu một bộ 8 con quái (tổng 16).
--   mobs  = 8 dòng [tempId, level, hp, x, y]; y = 432 là mặt nền của map này.
--           tempId 27 = Akkuman, 44 = Quỷ đầu nhọn (đều là quái đi bộ, type 1).
--           hp = 20.000.000; SÁT THƯƠNG quái KHÔNG đặt ở đây mà do code gán (50.000),
--           vì cột này không có chỗ cho sát thương.
DELETE FROM `map_template` WHERE `id` = 208;
INSERT INTO `map_template`
  (`id`, `NAME`, `zones`, `max_player`, `data`, `type`, `planet_id`, `bg_type`, `tile_id`, `bg_id`,
   `waypoints`, `mobs`, `npcs`, `is_map_double`) VALUES
(208, 'Nam Thiên Môn', 2, 15, '[0,0,0,13,12]', 0, 0, 0, 13, 12,
 '[]',
 '[[27,7,20000000,120,432],[44,7,20000000,257,432],[27,7,20000000,394,432],[44,7,20000000,531,432],[27,7,20000000,668,432],[44,7,20000000,805,432],[27,7,20000000,942,432],[44,7,20000000,1079,432]]',
 '[]', 0);

-- (2b) 10 vật phẩm.
--   type 27 = vật phẩm thường (Linh Thạch, gộp chồng được)
--   type 29 = vật phẩm dùng theo thời gian (5 đan + bùa) — cùng loại với Cuồng nộ / Bổ huyết
--   type  4 = ô rađa (3 ngọc bội) — code chặn thêm: CHỈ đệ tử mới đeo được
DELETE FROM `item_template` WHERE `id` BETWEEN 2266 AND 2275;
INSERT INTO `item_template` (`id`, `TYPE`, `gender`, `NAME`, `description`, `level`, `icon_id`,
                             `part`, `is_up_to_up`, `power_require`, `gold`, `gem`, `head`, `body`, `leg`) VALUES
(2266, 27, 3, 'Linh Thạch',        'Tinh hoa trời đất, dùng ở NPC Tu Tiên',        0, 30498, -1, 1, 0, 0, 0, -1, -1, -1),
(2267, 29, 3, 'Luyện Khí Đan',     'Tăng 20% sức đánh trong 10 phút',              0, 24885, -1, 1, 0, 0, 0, -1, -1, -1),
(2268, 29, 3, 'Hộ Thể Đan',        'Tăng 30% HP trong 10 phút',                    0, 24883, -1, 1, 0, 0, 0, -1, -1, -1),
(2269, 29, 3, 'Tụ Khí Đan',        'Tăng 30% KI trong 10 phút',                    0, 24888, -1, 1, 0, 0, 0, -1, -1, -1),
(2270, 29, 3, 'Kim Cương Đan',     'Giảm 50% sát thương trong 10 phút',            0, 24895, -1, 1, 0, 0, 0, -1, -1, -1),
(2271, 29, 3, 'Phá Quân Đan',      'Tăng 10% chí mạng trong 10 phút',              0, 24884, -1, 1, 0, 0, 0, -1, -1, -1),
(2272,  4, 3, 'Ngọc Bội Hộ Mệnh',  'Chỉ đệ tử đeo được. HP +10.000',               0, 20260, -1, 0, 0, 0, 0, -1, -1, -1),
(2273,  4, 3, 'Ngọc Bội Tụ Linh',  'Chỉ đệ tử đeo được. KI +10.000',               0, 20261, -1, 0, 0, 0, 0, -1, -1, -1),
(2274,  4, 3, 'Ngọc Bội Phá Quân', 'Chỉ đệ tử đeo được. Sức đánh +5.000',          0, 20262, -1, 0, 0, 0, 0, -1, -1, -1),
(2275, 29, 3, 'Tụ Linh Phù',       'Tăng 50% tỉ lệ rơi Linh Thạch trong 30 phút',  0, 30202, -1, 1, 0, 0, 0, -1, -1, -1);

-- (2c) npc_template 89 "Tu Tiên".
INSERT INTO `npc_template` (`id`, `NAME`, `head`, `body`, `leg`, `avatar`)
VALUES (89, 'Tu Tiên', 2580, 2581, 2582,
        COALESCE((SELECT `avatar_id` FROM `head_avatar` WHERE `head_id` = 2580 LIMIT 1), 19479))
ON DUPLICATE KEY UPDATE
    `NAME` = VALUES(`NAME`), `head` = VALUES(`head`), `body` = VALUES(`body`),
    `leg` = VALUES(`leg`), `avatar` = VALUES(`avatar`);

-- (2d) Đặt NPC lên map 5 — chỉ khi chưa có.
UPDATE `map_template`
   SET `npcs` = CASE
                  WHEN TRIM(`npcs`) = '[]' THEN '[[89,450,288]]'
                  ELSE CONCAT(LEFT(TRIM(`npcs`), CHAR_LENGTH(TRIM(`npcs`)) - 1), ',[89,450,288]]')
                END
 WHERE `id` = 5 AND `npcs` NOT LIKE '%[89,%';

-- ---------------------------------------------------------------------
-- (3) KIỂM TRA SAU — mọi cột `dat` phải = 1.
-- ---------------------------------------------------------------------
SELECT 'map 208 Nam Thien Mon' AS `muc`,
       (SELECT COUNT(*) FROM `map_template`
         WHERE `id` = 208 AND `tile_id` = 13 AND `bg_id` = 12 AND `zones` = 2) AS `dat`
UNION ALL
SELECT 'map 208 co dung 8 con quai',
       ((CHAR_LENGTH(`mobs`) - CHAR_LENGTH(REPLACE(`mobs`, '20000000', ''))) / CHAR_LENGTH('20000000') = 8)
  FROM `map_template` WHERE `id` = 208
UNION ALL
SELECT 'du 10 vat pham 2266-2275',
       ((SELECT COUNT(*) FROM `item_template` WHERE `id` BETWEEN 2266 AND 2275) = 10)
UNION ALL
SELECT 'item id lien tuc toi 2275',
       ((SELECT COUNT(*) FROM `item_template` WHERE `id` BETWEEN 0 AND 2275) = 2276)
UNION ALL
SELECT 'npc_template 89 Tu Tien',
       (SELECT COUNT(*) FROM `npc_template`
         WHERE `id` = 89 AND `NAME` = 'Tu Tiên' AND `head` = 2580 AND `body` = 2581 AND `leg` = 2582)
UNION ALL
SELECT 'id npc 0..89 lien tuc',
       ((SELECT COUNT(*) FROM `npc_template` WHERE `id` BETWEEN 0 AND 89) = 90)
UNION ALL
SELECT 'map 5 co dung 1 NPC 89',
       ((CHAR_LENGTH(`npcs`) - CHAR_LENGTH(REPLACE(`npcs`, '[89,', ''))) / CHAR_LENGTH('[89,') = 1)
  FROM `map_template` WHERE `id` = 5
UNION ALL
SELECT 'du 3 part cua NPC Tu Tien',
       ((SELECT COUNT(*) FROM `part` WHERE `id` IN (2580, 2581, 2582)) = 3);

SELECT `id`, `NAME`, `npcs` FROM `map_template` WHERE `id` = 5;

-- ---------------------------------------------------------------------
-- (4) LÙI LẠI  (bỏ comment rồi chạy; TẮT SERVER trước)
-- ---------------------------------------------------------------------
-- UPDATE `map_template` SET `npcs` = REPLACE(REPLACE(`npcs`, ',[89,450,288]', ''), '[[89,450,288]]', '[]') WHERE `id` = 5;
-- DELETE FROM `npc_template` WHERE `id` = 89 AND `NAME` = 'Tu Tiên';
-- DELETE FROM `item_template` WHERE `id` BETWEEN 2266 AND 2275;
-- DELETE FROM `map_template` WHERE `id` = 208;
