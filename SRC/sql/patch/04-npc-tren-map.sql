-- =====================================================================
-- 04-npc-tren-map.sql  —  ĐẶT NPC CỦA TUYẾN NHIỆM VỤ MỚI LÊN BẢN ĐỒ
-- Database: team2026        Sinh ngày: 2026-09-18
--
-- Đi kèm: 01-vat-pham-moi.sql, 02-nhiem-vu-moi.sql, 03-reset-tien-do.sql
-- Bàn giao: docs/4-trien-khai/30-boss-npc-bo-sung.md
-- Đặc tả:   docs/2-thiet-ke-nhiem-vu-moi/20a-chi-tiet-nhiem-vu-00-15.md §D
--           docs/2-thiet-ke-nhiem-vu-moi/20b-chi-tiet-nhiem-vu-16-31.md §NV 20, §NV 29
--
-- ---------------------------------------------------------------------
-- FILE NÀY LÀM GÌ
-- ---------------------------------------------------------------------
-- Chỉ ghi lại cột `map_template`.`npcs` của 8 map. KHÔNG đụng người chơi,
-- KHÔNG đụng vật phẩm, KHÔNG đụng nhiệm vụ, KHÔNG xoá dòng nào.
--
-- `npcs` là chuỗi JSON dạng mảng các bộ ba [npc_template_id, x, y]
-- (Manager.loadDatabase dòng ~878: JSONValue.parse(npcs.replaceAll("\"","")),
--  rồi npcId = Byte.parseByte(dtn[0]), npcX = Short.parseShort(dtn[1]),
--  npcY = Short.parseShort(dtn[2])). Định dạng: docs/1-he-thong-hien-tai/
--  02b-database-du-lieu-template.md §5.
--
-- ---------------------------------------------------------------------
-- VÌ SAO BẮT BUỘC  (khối 2)
-- ---------------------------------------------------------------------
--  * NPC 63 Jaco hiện CHỈ đứng ở map 24 (Trái Đất) và 139 (Potaufeu).
--    TaskService.isMapVachNui / isMapTTVT đổi map theo hành tinh
--    (Trái Đất 42 / 24 · Namếc 43 / 25 · Xayda 44 / 26), nên người chơi
--    Namếc và Xayda KHÔNG BAO GIỜ gặp được Jaco -> kẹt cứng ở NV 3 bước 0
--    (TASK_3_0), NV 7 bước 2 (TASK_7_2) và NV 15 bước 2 (TASK_15_2).
--  * NPC 76 Granola chưa đứng ở bất kỳ map nào -> NV 20 bước 2 và bước 5
--    (TASK_20_2 / TASK_20_5) không qua được.
--  * NPC 83 Dr. Myuu chưa đứng ở bất kỳ map nào -> NV 29 bước 3
--    (TASK_29_3) và NV 46 bước 0 (TASK_46_0) không qua được.
--  * NPC 71 Berry ĐÃ có sẵn trên map 160 — file này KHÔNG đụng tới,
--    chỉ cần lớp Java `npc_list/Berry.java` (đã thêm) là chạy.
--
-- ---------------------------------------------------------------------
-- HAI LỖ HỔNG TÌM THÊM ĐƯỢC  (khối 2b)
-- ---------------------------------------------------------------------
-- Đã dò toàn bộ cặp (npc, map) mà TaskService.checkDoneTaskTalkNpc đòi hỏi
-- so với cột `npcs` của cả 169 map. Ngoài 7 chỗ trên còn đúng 2 chỗ thiếu:
--  * NPC 42 Quốc Vương chỉ có ở map 43 (Namếc). TaskService đòi hắn ở cả ba
--    map Vách núi -> người chơi Trái Đất và Xayda kẹt ở NV 33 bước 1 và 5.
--  * NPC 70 Bardock chưa có ở map 14 Làng Kakarot -> kẹt ở NV 39 bước 5.
-- Hai chỗ này nằm ngoài yêu cầu ban đầu nhưng cùng một cột, cùng một kiểu
-- lỗi, nên sửa luôn. Muốn bỏ thì xoá khối (2b) trước khi chạy.
--
-- ---------------------------------------------------------------------
-- TOẠ ĐỘ ĐÃ CHỌN NHƯ THẾ NÀO
-- ---------------------------------------------------------------------
-- Mỗi toạ độ dưới đây đã được kiểm bằng chính thuật toán của server
-- (Map.yPhysicInTop + Manager.readTileMap + data/map/tile_set_info):
-- yPhysicInTop(x, y) == y, nghĩa là NPC đứng đúng trên mặt đất, không lơ
-- lửng và không lún vào đá. Khoảng cách tới NPC gần nhất đều > 60 pixel
-- (bán kính mở hội thoại của Map.getNpc), nên không NPC nào che NPC nào.
--
-- ---------------------------------------------------------------------
-- TRÌNH TỰ CHẠY
-- ---------------------------------------------------------------------
--   1) TẮT SERVER (map_template chỉ được đọc một lần lúc khởi động).
--   2) mysqldump -u root -p team2026 map_template > backup_map_template.sql
--   3) Chạy khối (1) KIỂM TRA TRƯỚC, đối chiếu "gia_tri_cu_mong_doi".
--   4) Chạy khối (2) và (2b) CẬP NHẬT.
--   5) Chạy khối (3) KIỂM TRA SAU.
--   6) Bật server, xem log "Successfully loaded map template (169)".
--
-- File chạy lại được nhiều lần (idempotent): mỗi lệnh ghi đè bằng GIÁ TRỊ
-- ĐẦY ĐỦ cuối cùng, không nối thêm.
-- =====================================================================

SET NAMES utf8mb4;

-- ---------------------------------------------------------------------
-- (1) KIỂM TRA TRƯỚC — chạy trước khi cập nhật.
--     Cột `khop_ban_goc` phải là 1 ở cả 8 dòng. Nếu có dòng nào = 0 thì
--     `npcs` của map đó đã bị sửa so với bản dump gốc: DỪNG LẠI, đọc giá
--     trị hiện tại rồi tự chèn thêm bộ ba mới vào thay vì chạy khối (2).
-- ---------------------------------------------------------------------
SELECT  `id`,
        `NAME`,
        `npcs` AS `gia_tri_hien_tai`,
        CASE `id`
            WHEN  14 THEN '[[9,396,408],[6,252,408],[74,286,409],[84,953,408]]'
            WHEN  25 THEN '[[6,540,336],[16,510,336],[11,348,336],[29,84,336]]'
            WHEN  26 THEN '[[12,228,336],[6,516,336],[16,510,336],[29,84,336]]'
            WHEN  42 THEN '[[21,588,408],[23,1015,408]]'
            WHEN  43 THEN '[[21,780,384],[42,247,432],[23,1034,432]]'
            WHEN  44 THEN '[[21,732,288],[23,1118,432]]'
            WHEN 160 THEN '[[70,1189,432],[71,1239,432]]'
            WHEN 166 THEN '[]'
        END AS `gia_tri_cu_mong_doi`,
        (`npcs` = CASE `id`
            WHEN  14 THEN '[[9,396,408],[6,252,408],[74,286,409],[84,953,408]]'
            WHEN  25 THEN '[[6,540,336],[16,510,336],[11,348,336],[29,84,336]]'
            WHEN  26 THEN '[[12,228,336],[6,516,336],[16,510,336],[29,84,336]]'
            WHEN  42 THEN '[[21,588,408],[23,1015,408]]'
            WHEN  43 THEN '[[21,780,384],[42,247,432],[23,1034,432]]'
            WHEN  44 THEN '[[21,732,288],[23,1118,432]]'
            WHEN 160 THEN '[[70,1189,432],[71,1239,432]]'
            WHEN 166 THEN '[]'
        END) AS `khop_ban_goc`
FROM `map_template`
WHERE `id` IN (14, 25, 26, 42, 43, 44, 160, 166)
ORDER BY `id`;

-- ---------------------------------------------------------------------
-- (2) CẬP NHẬT — phần bắt buộc
-- ---------------------------------------------------------------------

-- --- NPC 63 Jaco: Trạm tàu vũ trụ Namếc / Xayda ----------------------
-- Dùng cho TASK_7_2 và TASK_15_2 (isMapTTVT đổi theo hành tinh).
-- Map 24 (Trái Đất) đã có sẵn [63,99,336] — không đụng tới.

-- map 25 Trạm tàu vũ trụ (Namếc): nền phẳng y=336 từ x=24 tới x=552.
-- Chỗ trống rộng nhất nằm giữa Rồng Omega (x=84) và Cargo (x=348) -> x=216,
-- cách đều 132 pixel cả hai bên.
UPDATE `map_template`
   SET `npcs` = '[[6,540,336],[16,510,336],[11,348,336],[29,84,336],[63,216,336]]'
 WHERE `id` = 25;

-- map 26 Trạm tàu vũ trụ (Xayda): nền phẳng y=336 từ x=24 tới x=552.
-- Chỗ trống giữa Cui (x=228) và Uron (x=510) -> x=360 (cách 132 / 150).
UPDATE `map_template`
   SET `npcs` = '[[12,228,336],[6,516,336],[16,510,336],[29,84,336],[63,360,336]]'
 WHERE `id` = 26;

-- --- NPC 63 Jaco (+ NPC 42 Quốc Vương, xem khối 2b): ba map Vách núi --
-- Dùng cho TASK_3_0 (isMapVachNui đổi theo hành tinh: 42 / 43 / 44).
-- Cả ba map đều đã có Bà Hạt Mít (21) và Ghi danh (23); Jaco được đặt ở
-- thềm thấp phía trái, xa cả hai, và xa con Máy đo sức mạnh (mob 117)
-- mà NV 3 bước 1 bắt đánh.

-- map 42 Vách núi Aru: thềm y=432 trải từ x=192 tới x=480.
UPDATE `map_template`
   SET `npcs` = '[[21,588,408],[23,1015,408],[63,312,432]]'
 WHERE `id` = 42;

-- map 43 Vách núi Moori: thềm y=432 trải từ x=168 tới x=552.
-- Tránh Quốc Vương (npc 42) đang đứng ở x=247 -> chọn x=420 (cách 173).
UPDATE `map_template`
   SET `npcs` = '[[21,780,384],[42,247,432],[23,1034,432],[63,420,432]]'
 WHERE `id` = 43;

-- map 44 Vách núi Kakarot: thềm y=432 trải từ x=192 tới x=552.
UPDATE `map_template`
   SET `npcs` = '[[21,732,288],[23,1118,432],[63,300,432]]'
 WHERE `id` = 44;

-- --- NPC 76 Granola: Khu hang động (map 160) -------------------------
-- Dùng cho TASK_20_2 và TASK_20_5.
-- Đặt cùng gờ đá y=432 với Bardock (x=1189) và Berry (x=1239) — đúng bối
-- cảnh 20b (Berry là người đi cùng Granola). Gờ đá này trải từ x=960 tới
-- x=1272; chọn x=1080 để cách Bardock 109 pixel, cách Berry 159 pixel,
-- đều lớn hơn bán kính 60 của Map.getNpc nên không mở nhầm hội thoại.
UPDATE `map_template`
   SET `npcs` = '[[70,1189,432],[71,1239,432],[76,1080,432]]'
 WHERE `id` = 160;

-- --- NPC 83 Dr. Myuu: Phòng thí nghiệm Myuu (map 166) ----------------
-- Dùng cho TASK_29_3 và TASK_46_0.
-- Map 166 hiện HOÀN TOÀN trống (không mob, không npc, không waypoint).
-- Bệ phẳng rộng nhất ở khu giữa là y=240, trải từ x=576 tới x=672;
-- chọn giữa bệ x=624. (Bệ y=72 x=696..888 cũng hợp lệ nếu chủ dự án
-- muốn Dr. Myuu đứng cao hơn.)
-- !! ĐỌC KHỐI (5): map 166 hiện KHÔNG CÓ ĐƯỜNG VÀO.
UPDATE `map_template`
   SET `npcs` = '[[83,624,240]]'
 WHERE `id` = 166;

-- ---------------------------------------------------------------------
-- (2b) CẬP NHẬT — hai lỗ hổng tìm thêm được (xoá khối này nếu không muốn)
-- ---------------------------------------------------------------------

-- map 42 Vách núi Aru: thêm NPC 42 Quốc Vương cho TASK_33_1 / TASK_33_5.
-- Cùng thềm y=432 với Jaco; x=216 cách Jaco (x=312) 96 pixel.
UPDATE `map_template`
   SET `npcs` = '[[21,588,408],[23,1015,408],[63,312,432],[42,216,432]]'
 WHERE `id` = 42;

-- map 44 Vách núi Kakarot: thêm NPC 42 Quốc Vương.
-- Cùng thềm y=432 với Jaco; x=420 cách Jaco (x=300) 120 pixel.
UPDATE `map_template`
   SET `npcs` = '[[21,732,288],[23,1118,432],[63,300,432],[42,420,432]]'
 WHERE `id` = 44;

-- map 14 Làng Kakarot: thêm NPC 70 Bardock cho TASK_39_5.
-- Nền phẳng liền mạch y=408 từ x=24 tới x=1200; các NPC cũ đứng ở
-- x = 252 / 286 / 396 / 953 -> chọn x=650 (cách chỗ gần nhất 254 pixel).
UPDATE `map_template`
   SET `npcs` = '[[9,396,408],[6,252,408],[74,286,409],[84,953,408],[70,650,408]]'
 WHERE `id` = 14;

-- ---------------------------------------------------------------------
-- (3) KIỂM TRA SAU — cả 8 dòng phải có `dat_yeu_cau` = 1.
-- ---------------------------------------------------------------------
SELECT  `id`,
        `NAME`,
        `npcs`,
        (`npcs` = CASE `id`
            WHEN  14 THEN '[[9,396,408],[6,252,408],[74,286,409],[84,953,408],[70,650,408]]'
            WHEN  25 THEN '[[6,540,336],[16,510,336],[11,348,336],[29,84,336],[63,216,336]]'
            WHEN  26 THEN '[[12,228,336],[6,516,336],[16,510,336],[29,84,336],[63,360,336]]'
            WHEN  42 THEN '[[21,588,408],[23,1015,408],[63,312,432],[42,216,432]]'
            WHEN  43 THEN '[[21,780,384],[42,247,432],[23,1034,432],[63,420,432]]'
            WHEN  44 THEN '[[21,732,288],[23,1118,432],[63,300,432],[42,420,432]]'
            WHEN 160 THEN '[[70,1189,432],[71,1239,432],[76,1080,432]]'
            WHEN 166 THEN '[[83,624,240]]'
        END) AS `dat_yeu_cau`
FROM `map_template`
WHERE `id` IN (14, 25, 26, 42, 43, 44, 160, 166)
ORDER BY `id`;

-- Đếm nhanh các map có Jaco (63) / Granola (76) / Dr. Myuu (83):
-- phải ra đúng 9 dòng — map 24, 25, 26, 42, 43, 44, 139 (Jaco),
-- 160 (Granola) và 166 (Dr. Myuu).
SELECT `id`, `NAME`, `npcs`
FROM `map_template`
WHERE `npcs` REGEXP '\\[(63|76|83),'
ORDER BY `id`;

-- =====================================================================
-- (4) LÙI LẠI — trả 8 map về đúng giá trị của bản dump gốc
--     (database team2026.sql). Bỏ dấu `--` để chạy.
--     Lưu ý: nếu ai đó đã sửa tay `npcs` sau khi chạy file này thì khối
--     lùi lại sẽ ghi đè luôn cả phần sửa tay đó — hãy so với bản
--     mysqldump ở bước 2 của "TRÌNH TỰ CHẠY".
-- =====================================================================
-- UPDATE `map_template` SET `npcs` = '[[9,396,408],[6,252,408],[74,286,409],[84,953,408]]' WHERE `id` = 14;
-- UPDATE `map_template` SET `npcs` = '[[6,540,336],[16,510,336],[11,348,336],[29,84,336]]' WHERE `id` = 25;
-- UPDATE `map_template` SET `npcs` = '[[12,228,336],[6,516,336],[16,510,336],[29,84,336]]' WHERE `id` = 26;
-- UPDATE `map_template` SET `npcs` = '[[21,588,408],[23,1015,408]]'                        WHERE `id` = 42;
-- UPDATE `map_template` SET `npcs` = '[[21,780,384],[42,247,432],[23,1034,432]]'           WHERE `id` = 43;
-- UPDATE `map_template` SET `npcs` = '[[21,732,288],[23,1118,432]]'                        WHERE `id` = 44;
-- UPDATE `map_template` SET `npcs` = '[[70,1189,432],[71,1239,432]]'                       WHERE `id` = 160;
-- UPDATE `map_template` SET `npcs` = '[]'                                                  WHERE `id` = 166;

-- =====================================================================
-- (5) ĐÃ BẬT — chủ dự án chốt: dùng điểm dịch chuyển thường từ map 97
--     ĐƯỜNG VÀO / RA MAP 166 PHÒNG THÍ NGHIỆM MYUU
--
--     Đặt được Dr. Myuu lên map 166 rồi thì vẫn còn một chuyện nữa:
--     map 166 KHÔNG CÓ WAYPOINT NÀO, cả vào lẫn ra, và không có đoạn mã
--     nào trong server đưa người chơi tới đó (đã quét toàn bộ src/:
--     chỉ có ChangeMapService dòng ~1047 KHOÁ map này theo TASK_29_0,
--     không có chỗ nào MỞ đường tới). Nghĩa là NV 29, NV 45, NV 46 vẫn
--     kẹt cho tới khi có đường vào. 20b §NV 29 cũng ghi đúng điều này và
--     đề nghị "thêm waypoint ra map 97".
--
--     CHỐT: cửa thường, không cần vật phẩm "Thẻ từ" — map vẫn được khoá
--     theo bước nhiệm vụ TASK_29_0 trong ChangeMapService nên người chưa
--     tới nhiệm vụ 29 vẫn không vào được.
--     Toạ độ đã kiểm:
--       - map 97 Thành phố phía bắc: nền phẳng liền mạch y=384 từ x=24
--         tới x=1632, hai rìa map đã có waypoint đi 96 và 98, nên cửa vào
--         phòng thí nghiệm phải đặt GIỮA map (ô x 840..888, y 336..384).
--       - map 166: điểm đáp và cửa ra đặt trên bệ y=240 (x 576..672),
--         cùng bệ với Dr. Myuu.
--     Định dạng waypoint: [tên, minX, minY, maxX, maxY, isEnter,
--     isOffline, goMap, goX, goY] (Manager.loadDatabase dòng ~840).
-- =====================================================================
UPDATE `map_template`
   SET `waypoints` = '[["Ngọn núi phía bắc",1656,360,1680,384,0,0,98,60,384],["Cao nguyên",0,360,24,384,0,0,96,1620,168],["Phòng thí nghiệm Myuu",840,336,888,384,1,0,166,624,240]]'
 WHERE `id` = 97;

UPDATE `map_template`
   SET `waypoints` = '[["Thành phố phía bắc",576,192,624,240,1,0,97,864,384]]'
 WHERE `id` = 166;

-- Kiểm tra: phải trả về 2 dòng, map 97 có 3 waypoint và map 166 có 1
SELECT `id`, `waypoints` FROM `map_template` WHERE `id` IN (97, 166);
--
-- -- Lùi lại phần tuỳ chọn:
-- -- UPDATE `map_template` SET `waypoints` = '[["Ngọn núi phía bắc",1656,360,1680,384,0,0,98,60,384],["Cao nguyên",0,360,24,384,0,0,96,1620,168]]' WHERE `id` = 97;
-- -- UPDATE `map_template` SET `waypoints` = '[]' WHERE `id` = 166;

-- =====================================================================
-- Hết file 04.
-- =====================================================================
