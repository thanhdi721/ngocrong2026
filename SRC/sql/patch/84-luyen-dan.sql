-- =====================================================================
-- 84-luyen-dan.sql — LUYỆN ĐAN: 14 VẬT PHẨM + NPC "LÒ LUYỆN ĐAN" + 2 BOSS MỚI
-- Database: team2026 (MariaDB 10.4)        Sinh ngày: 2026-09-27
--
-- Bản thiết kế: docs/4-trien-khai/71-luyen-dan.md
--
-- Code đi kèm:
--   tu_tien/LuyenDan (công thức + nổ lò), npc_list/LoLuyenDan (NPC 90),
--   tu_tien/TuTien.roiLinhThao (quái map 208 rơi linh thảo),
--   boss/BossID + boss/BossesData + boss/tay_du/BossTayDu (2 boss mới),
--   boss/drop/BangRoiBoss (Địa Hỏa Tinh + đan phương vào bảng rơi),
--   item/ItemTime + player/NPoint + player/Player (đan thượng phẩm 20 phút),
--   services_func/UseItem (dùng đan thượng phẩm + Đan Phế),
--   consts/ConstNpc (LO_LUYEN_DAN = 90), npc/NpcFactory,
--   data/DataGame (vsItem 31 -> 32).
--
-- Phải chạy TRƯỚC: patch 83.
--
-- ---------------------------------------------------------------------
-- FILE NÀY LÀM GÌ
-- ---------------------------------------------------------------------
--  (a) Thêm 14 `item_template` 2276–2289:
--        2276–2279  4 linh thảo   — QUÁI map Tu Tiên (208) rơi, GIAO DỊCH ĐƯỢC
--        2280       Địa Hỏa Tinh  — BOSS rơi, là xúc tác bắt buộc của mọi công thức
--        2281–2283  3 đan phương  — BOSS rơi, tiêu hao một lần mỗi mẻ luyện
--        2284       Đan Phế       — phần thưởng an ủi khi nổ lò, ăn vào không được gì
--        2285–2289  5 đan thượng phẩm — luyện ra, mạnh hơn đan tiệm và kéo 20 phút
--  (b) Thêm 3 `part` 2658/2659/2660 + `npc_template` 90 "Lò Luyện Đan".
--      Đây là NPC dạng VẬT THỂ, làm y hệt "Cây thông Noel" (npc 79): phần đầu vẽ
--      một icon to, phần thân và chân để trong suốt (icon 2955).
--      Icon 32700 là ảnh lò, do ta tự dựng từ icon 24895 (bình luyện đan của SUMO),
--      hình vuông 52 / 105 / 157 / 211 px theo bốn mức phóng to — TO HƠN khung của
--      icon 15041 (cây thông, 36×50) cho bằng cỡ một NPC người.
--      Hai số -8,-2 giữ cho ĐÁY và TÂM của lò trùng đúng chỗ đáy/tâm khung cây thông:
--        tâm  x = 0 + 36/2 = 18   ->  -8 + 52/2 = 18   (khớp)
--        đáy  y = 0 + 50    = 50  ->  -2 + 52   = 50   (khớp)
--      Sửa hai số đó là dời lò, không phải sửa ảnh.
--      SỐ 32700 KHÔNG ĐƯỢC VƯỢT 32767: `part`.`DATA` đọc icon bằng Short.parseShort
--      (Manager.loadDatabase), id lớn hơn là văng NumberFormatException lúc nạp CSDL
--      và server không lên được. Icon lớn nhất đang có trong res là 32667.
--  (c) Nối [90,170,288] vào `map_template`.`npcs` của map 5 Đảo Kamê.
--
-- VÌ SAO LÀ ITEM 2276–2289:
--  ITEM_TEMPLATES tra theo chỉ số nên id phải liên tục. Id lớn nhất sau patch 83 là 2275.
--
-- VÌ SAO LÀ NPC ID 90:
--  Client và NpcFactory tra template theo VỊ TRÍ trong npc_template => id phải bằng
--  vị trí. Sau patch 83 dải 0..89 liên tục, 90 là ô trống kế tiếp. Zone ghi tempId
--  bằng writeByte nên id phải <= 127; 90 thoả.
--
-- VÌ SAO ĐỨNG Ở x = 170, y = 288 TRÊN MAP 5:
--  Thềm trên của đảo Kamê là cột 0..19 của lưới ô 24 px, tức x = 0..479. Trên thềm đó
--  đã có 81 (x=240), 86 (x=310), 87 (x=380), 89 (x=450) — cách nhau đúng 70 px, không
--  còn khe nào ở giữa. x = 170 là ô trống kế bên trái NPC 81, vẫn là nền đặc
--  (lưới ô [12][7] khác 0) và cách NPC gần nhất 70 px > bán kính 60 px của Map.getNpc.
--  Muốn dời chỗ thì chỉ cần sửa số trong khối (2d), không đụng code.
--
-- VÌ SAO KHÔNG THÊM DÒNG `item_option_template` NÀO:
--  Bảng đó gửi bằng writeByte nên tối đa 255 dòng, hiện đã 253. Hai ô cuối để dành.
--  Vì thế đan thượng phẩm là VẬT PHẨM RIÊNG chứ không phải "phẩm chất" gắn vào đan cũ.
--
-- TRẦN NGÂN SÁCH: mô tả vật phẩm cố ý viết ngắn. Sau patch này bảng vật phẩm nặng
-- ~64 KB mỗi gói (trần 65 KB) — xem khối (3), cột `uoc_luong_byte`.
--
-- ---------------------------------------------------------------------
-- TRÌNH TỰ CHẠY
-- ---------------------------------------------------------------------
--   1) TẮT SERVER.
--   2) mysqldump -u root -p team2026 item_template npc_template part map_template > backup_84.sql
--   3) Chạy cả file. Chạy lại nhiều lần vẫn an toàn.
--   4) Xem khối (3): mọi cột `dat` phải = 1.
--   5) Bật server bản code mới (vsItem = 32).
-- =====================================================================

-- ---------------------------------------------------------------------
-- (1) KIỂM TRA TRƯỚC — nhìn rồi hẵng chạy khối (2).
-- ---------------------------------------------------------------------
SELECT MAX(`id`) AS `item_id_lon_nhat_phai_la_2275` FROM `item_template`;
SELECT (SELECT COUNT(*) FROM `npc_template` WHERE `id` < 90) AS `so_id_duoi_90_phai_la_90`,
       (SELECT `NAME` FROM `npc_template` WHERE `id` = 90) AS `ten_hien_tai`;
SELECT MAX(`id`) AS `part_id_lon_nhat_phai_la_2657` FROM `part`;
SELECT COUNT(*) AS `so_dong_option_phai_la_253` FROM `item_option_template`;
-- Mẫu để đối chiếu: part của "Cây thông Noel" — cùng kiểu NPC vật thể.
SELECT `id`, `TYPE`, `DATA` FROM `part` WHERE `id` IN (2003, 2004, 2005);
SELECT `id`, `NAME`, `head`, `body`, `leg`, `avatar` FROM `npc_template` WHERE `id` = 79;

-- ---------------------------------------------------------------------
-- (2) THÊM DỮ LIỆU.
-- ---------------------------------------------------------------------

-- (2a) 14 vật phẩm 2276–2289.
--      TYPE 27 = nguyên liệu (chỉ nằm trong túi), TYPE 29 = vật phẩm dùng được.
--      `is_up_to_up` = 1 để xếp chồng trong hành trang.
DELETE FROM `item_template` WHERE `id` BETWEEN 2276 AND 2289;
INSERT INTO `item_template` (`id`, `TYPE`, `gender`, `NAME`, `description`, `level`, `icon_id`,
                             `part`, `is_up_to_up`, `power_require`, `gold`, `gem`, `head`, `body`, `leg`) VALUES
-- linh thảo — quái map Tu Tiên rơi, cho giao dịch
(2276, 27, 3, 'Thanh Vân Thảo',   'Linh thảo. Dùng để luyện đan',       0, 24872, -1, 1, 0, 0, 0, -1, -1, -1),
(2277, 27, 3, 'Ngọc Diệp Thảo',   'Linh thảo. Dùng để luyện đan',       0, 24874, -1, 1, 0, 0, 0, -1, -1, -1),
(2278, 27, 3, 'Hàn Tinh Quả',     'Linh thảo. Dùng để luyện đan',       0, 24891, -1, 1, 0, 0, 0, -1, -1, -1),
(2279, 27, 3, 'Kim Nhung Quả',    'Linh thảo. Dùng để luyện đan',       0, 24892, -1, 1, 0, 0, 0, -1, -1, -1),
-- xúc tác + đan phương — boss rơi
(2280, 27, 3, 'Địa Hỏa Tinh',     'Lửa mồi lò. Boss rơi',               0, 24881, -1, 1, 0, 0, 0, -1, -1, -1),
(2281, 27, 3, 'Đan Phương Sơ Cấp','Công thức. Dùng một lần',            0, 30192, -1, 1, 0, 0, 0, -1, -1, -1),
(2282, 27, 3, 'Đan Phương Trung Cấp','Công thức. Dùng một lần',         0, 30194, -1, 1, 0, 0, 0, -1, -1, -1),
(2283, 27, 3, 'Đan Phương Cao Cấp','Công thức. Dùng một lần',           0, 30196, -1, 1, 0, 0, 0, -1, -1, -1),
-- sản phẩm của lò
(2284, 29, 3, 'Đan Phế',          'Cục than. Nổ lò thì được cái này',   0, 24887, -1, 1, 0, 0, 0, -1, -1, -1),
(2285, 29, 3, 'Luyện Khí Đan Thượng Phẩm','Sức đánh +30%, 20 phút',     0, 24896, -1, 1, 0, 0, 0, -1, -1, -1),
(2286, 29, 3, 'Hộ Thể Đan Thượng Phẩm','HP +45%, 20 phút',              0, 24890, -1, 1, 0, 0, 0, -1, -1, -1),
(2287, 29, 3, 'Tụ Khí Đan Thượng Phẩm','KI +45%, 20 phút',              0, 24889, -1, 1, 0, 0, 0, -1, -1, -1),
(2288, 29, 3, 'Kim Cương Đan Thượng Phẩm','Chịu đòn -60%, 20 phút',     0, 24886, -1, 1, 0, 0, 0, -1, -1, -1),
(2289, 29, 3, 'Phá Quân Đan Thượng Phẩm','Chí mạng +15%, 20 phút',      0, 24882, -1, 1, 0, 0, 0, -1, -1, -1);

-- (2b) 3 part của NPC vật thể "Lò Luyện Đan".
--      Số mảnh BẮT BUỘC: đầu 3, thân 17, chân 14 (xem Manager.writePartData).
--      2955 = ảnh trong suốt.
DELETE FROM `part` WHERE `id` BETWEEN 2658 AND 2660;
INSERT INTO `part` (`id`, `TYPE`, `DATA`) VALUES
(2658, 0, '[[2955,0,0],[32700,-8,-2],[2955,0,0]]'),
(2659, 1, '[[2955,0,0],[2955,0,0],[2955,0,0],[2955,0,0],[2955,0,0],[2955,0,0],[2955,0,0],[2955,0,0],[2955,0,0],[2955,0,0],[2955,0,0],[2955,0,0],[2955,0,0],[2955,0,0],[2955,0,0],[2955,0,0],[2955,0,0]]'),
(2660, 2, '[[2955,0,0],[2955,0,0],[2955,0,0],[2955,0,0],[2955,0,0],[2955,0,0],[2955,0,0],[2955,0,0],[2955,0,0],[2955,0,0],[2955,0,0],[2955,0,0],[2955,0,0],[2955,0,0]]');

-- (2c) npc_template 90 "Lò Luyện Đan".
--      avatar = 0 giống hai NPC vật thể sẵn có (78 Ông già Noel, 79 Cây thông Noel):
--      gói menu (lệnh 32) chỉ gửi tempId, client tự dựng ảnh từ part nên không cần avatar.
INSERT INTO `npc_template` (`id`, `NAME`, `head`, `body`, `leg`, `avatar`)
VALUES (90, 'Lò Luyện Đan', 2658, 2659, 2660, 0)
ON DUPLICATE KEY UPDATE
    `NAME` = VALUES(`NAME`), `head` = VALUES(`head`), `body` = VALUES(`body`),
    `leg` = VALUES(`leg`), `avatar` = VALUES(`avatar`);

-- (2d) Đặt NPC lên map 5 — chỉ khi chưa có.
UPDATE `map_template`
   SET `npcs` = CASE
                  WHEN TRIM(`npcs`) = '[]' THEN '[[90,170,288]]'
                  ELSE CONCAT(LEFT(TRIM(`npcs`), CHAR_LENGTH(TRIM(`npcs`)) - 1), ',[90,170,288]]')
                END
 WHERE `id` = 5 AND `npcs` NOT LIKE '%[90,%';

-- ---------------------------------------------------------------------
-- (3) KIỂM TRA SAU — mọi cột `dat` phải = 1.
-- ---------------------------------------------------------------------
SELECT 'them du 14 vat pham' AS `muc`,
       ((SELECT COUNT(*) FROM `item_template` WHERE `id` BETWEEN 2276 AND 2289) = 14) AS `dat`
UNION ALL
SELECT 'item id lien tuc toi 2289',
       ((SELECT COUNT(*) FROM `item_template` WHERE `id` BETWEEN 0 AND 2289) = 2290)
UNION ALL
SELECT 'part 2658/2659/2660 dung so manh',
       ((SELECT COUNT(*) FROM `part`
          WHERE (`id` = 2658 AND `TYPE` = 0
                 AND (CHAR_LENGTH(`DATA`) - CHAR_LENGTH(REPLACE(`DATA`, '],[', ''))) / 3 = 2)
             OR (`id` = 2659 AND `TYPE` = 1
                 AND (CHAR_LENGTH(`DATA`) - CHAR_LENGTH(REPLACE(`DATA`, '],[', ''))) / 3 = 16)
             OR (`id` = 2660 AND `TYPE` = 2
                 AND (CHAR_LENGTH(`DATA`) - CHAR_LENGTH(REPLACE(`DATA`, '],[', ''))) / 3 = 13)) = 3)
UNION ALL
SELECT 'part id van lien tuc toi 2660',
       ((SELECT COUNT(*) FROM `part` WHERE `id` BETWEEN 2658 AND 2660) = 3)
UNION ALL
SELECT 'npc_template 90 Lo Luyen Dan',
       (SELECT COUNT(*) FROM `npc_template`
         WHERE `id` = 90 AND `NAME` = 'Lò Luyện Đan' AND `head` = 2658)
UNION ALL
SELECT 'npc_template van lien tuc toi 90',
       ((SELECT COUNT(*) FROM `npc_template` WHERE `id` BETWEEN 0 AND 90) = 91)
UNION ALL
SELECT 'map 5 co NPC 90',
       ((SELECT COUNT(*) FROM `map_template` WHERE `id` = 5 AND `npcs` LIKE '%[90,170,288]%') = 1)
UNION ALL
SELECT 'KHONG them dong option nao (van 253)',
       ((SELECT COUNT(*) FROM `item_option_template`) = 253);

-- Độ nặng gói vật phẩm: tổng phải < 130.000 byte (2 gói × 65.000 — xem
-- data/ItemData.MAX_PACKET_BYTES). Công thức lấy đúng ItemData.sizeOf:
--   1 type + 1 gender + (2 + tên) + (2 + mô tả) + 1 level + 4 strRequire
--   + 2 icon + 2 part + 1 isUpToUp = 16 + tên + mô tả, đếm bằng BYTE (LENGTH),
--   không phải bằng ký tự (CHAR_LENGTH) — chữ có dấu chiếm 2-3 byte.
SELECT SUM(16 + LENGTH(CONVERT(`NAME` USING utf8mb4))
              + LENGTH(CONVERT(IFNULL(`description`, '') USING utf8mb4)))
           AS `tong_byte_phai_duoi_130000`
  FROM `item_template`;

-- ---------------------------------------------------------------------
-- (4) GỠ BỎ — chỉ chạy khi muốn quay lại trước patch này.
-- ---------------------------------------------------------------------
-- DELETE FROM `item_template` WHERE `id` BETWEEN 2276 AND 2289;
-- DELETE FROM `part` WHERE `id` BETWEEN 2658 AND 2660;
-- DELETE FROM `npc_template` WHERE `id` = 90;
-- UPDATE `map_template`
--    SET `npcs` = REPLACE(REPLACE(`npcs`, ',[90,170,288]', ''), '[[90,170,288]]', '[]')
--  WHERE `id` = 5;
