-- =====================================================================
-- 08-bo-de-tu-khoi-nhiem-vu.sql  —  GỠ MỌI THỨ LIÊN QUAN ĐỆ TỬ KHỎI TUYẾN NHIỆM VỤ CHÍNH
-- Database: team2026        Chạy trên: MariaDB 10.4 (chỉ UPDATE / INSERT / SELECT cơ bản,
--                           JSON_VALID / JSON_EXTRACT — KHÔNG dùng JSON_TABLE)
-- Tài liệu: docs/4-trien-khai/43-bo-de-tu-khoi-nhiem-vu.md
--
-- Vì sao: người chơi kẹt ở NV 12 "Bạn đồng hành" bước "Nở trứng nhận đệ tử (0/1)".
-- Chủ dự án chốt: đệ tử vẫn chỉ nhận bằng cách săn Super Broly => bỏ đệ tử khỏi tuyến chính.
--   * NV 12 giữ tên "Bạn đồng hành", giữ 3 bước, nhưng "bạn" là Jaco:
--       0  Gặp Jaco ở Trạm tàu vũ trụ   (nói chuyện NPC 63, map 24/25/26 theo hành tinh)
--       1  Cùng Jaco hạ 25 quái mẹ      (thằn lằn mẹ / phi long mẹ / quỷ bay mẹ, map nào cũng tính)
--       2  Đưa Jaco về gặp %2           (nói chuyện ông ở nhà)
--   * Thay thưởng chỉ dùng được cho đệ tử:
--       NV 12  : 1 Đổi đệ tử (401) + 1 Nâng kỹ năng 1 đệ tử (402) -> 1 Gói 30 đậu thần cấp 3 + 5 Đá nâng cấp cấp 1
--       NV 34  : 1 Bông tai Porata (454)                          -> 5 Đá bảo vệ + 3 Đá ngũ sắc
--       NV 36.0: 1 Bình hút năng lượng (1795)                     -> 10 Đậu thần cấp 8
--       NV 37  : 1 Bông tai Porata cấp 2 (921)                    -> 5 Đá bảo vệ + 5 Đá ngũ sắc
--       NV 39  : 1 Bông tai Porata cấp 3 (1819) + 50 Đậu cấp 8    -> 10 Đá bảo vệ + 50 Đậu thần cấp 8
--   * Người chơi đang ở NV 12 (bất kỳ bước nào) được đưa về ĐẦU NV 12: data_task = [12,0,0,0].
--
-- ---------------------------------------------------------------------
-- !!!!!!!!!!!!!!!!!!!!!!!!!!!!  CẢNH BÁO  !!!!!!!!!!!!!!!!!!!!!!!!!!!!!!
-- ---------------------------------------------------------------------
-- 1) TẮT SERVER TRƯỚC khi chạy. Server đang chạy sẽ ghi đè `player.data_task` từ bộ nhớ
--    lúc người chơi thoát/lưu định kỳ => lệnh đưa về đầu NV 12 bị mất, người chơi quay lại
--    đúng chỉ số bước CŨ trên bước MỚI.
-- 2) SAO LƯU TRƯỚC:
--      mysqldump -u root -p team2026 task_main_template task_sub_template task_main_reward player \
--        > backup_truoc_08_$(date +%F).sql
-- 3) Chạy SAU 02 / 05 / 06 / 07 (server đang chạy đã có đủ). Cài mới: 01 -> 02 -> 03 -> 05 -> 06
--    -> 07 -> 08 (06 và 07 ghi lại chữ NV 12 cũ, nên 08 LUÔN chạy SAU CÙNG).
-- 4) Đi kèm jar mới (TaskService: Jaco ở Trạm tàu vũ trụ hoàn thành TASK_12_0; bỏ trigger
--    "có đệ tử"). Jar cũ + file này => bước 0 NV 12 lại kẹt (không ai hoàn thành được).
--
-- AN TOÀN / CHẠY LẠI:
--   * Mọi câu là UPDATE đặt giá trị cố định + INSERT IGNORE vào bảng sao lưu => chạy lại
--     bao nhiêu lần cũng cho cùng kết quả.
--   * KHÔNG đổi số bước, ducvupro, id nhiệm vụ; không xóa dòng nào.
--   * Bảng `player_task_backup_08` giữ data_task GỐC (lần chạy đầu) của người chơi bị đưa
--     về đầu NV 12 — lần chạy sau không ghi đè.
--
-- CÁCH CHẠY:
--   mysql -u root -p team2026 < SRC/sql/patch/08-bo-de-tu-khoi-nhiem-vu.sql
--   (hoặc dán vào tab SQL của phpMyAdmin), xem mục KIỂM TRA cuối file, rồi khởi động
--   server bằng jar mới (Manager chỉ nạp nhiệm vụ lúc khởi động).
-- =====================================================================

SET NAMES utf8mb4;

-- ---------------------------------------------------------------------
-- (0) Sao lưu tiến độ người chơi đang ở NV 12 (DDL để ngoài transaction)
-- ---------------------------------------------------------------------
CREATE TABLE IF NOT EXISTS `player_task_backup_08` (
  `id`           int(11)     NOT NULL,
  `name`         varchar(20) NOT NULL,
  `data_task_cu` text        NOT NULL,
  `backup_time`  timestamp   NOT NULL DEFAULT current_timestamp(),
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

INSERT IGNORE INTO `player_task_backup_08` (`id`, `name`, `data_task_cu`)
SELECT `id`, `name`, `data_task`
FROM `player`
WHERE JSON_VALID(`data_task`)
  AND CAST(JSON_EXTRACT(`data_task`, '$[0]') AS UNSIGNED) = 12;

START TRANSACTION;

-- ---------------------------------------------------------------------
-- (1) NV 12 — mô tả + 3 bước mới (giữ nguyên số bước và ducvupro 39/40/41)
-- ---------------------------------------------------------------------
UPDATE `task_main_template` SET `NAME` = 'Bạn đồng hành', `detail` = 'Jaco xin đi cùng ngươi: hai cái đầu thì quên chậm hơn một.
Gặp Jaco ở trạm tàu, cùng hạ quái mẹ ở %15 rồi về nhà.
Thưởng: 200.000 SM, 200.000 TN, 1 Gói 30 đậu cấp 3, 5 Đá nâng cấp 1'
WHERE `id` = 12;

-- Bước 0: TASK_12_0 — nói chuyện Jaco (63) ở Trạm tàu vũ trụ; mũi tên -6 = 24/25/26
UPDATE `task_sub_template`
   SET `NAME` = 'Gặp Jaco ở Trạm tàu vũ trụ', `max_count` = 1,
       `notify` = 'Jaco đợi ở Trạm tàu vũ trụ hành tinh ngươi, hắn muốn đi cùng ngươi',
       `npc_id` = 63, `map` = -6
 WHERE `task_main_id` = 12 AND `ducvupro` = 39;

-- Bước 1: TASK_12_1 — hạ 25 quái mẹ (mob 10/11/12) ở mọi map; mũi tên -11 = 4/12/18
UPDATE `task_sub_template`
   SET `NAME` = 'Cùng Jaco hạ 25 quái mẹ', `max_count` = 25,
       `notify` = 'Jaco đi tuần cùng ngươi: %14 ở %15; quái mẹ loại nào, map nào cũng tính',
       `npc_id` = -1, `map` = -11
 WHERE `task_main_id` = 12 AND `ducvupro` = 40;

-- Bước 2: TASK_12_2 — nói chuyện ông ở nhà; -2 = NPC ông / map nhà theo hành tinh
UPDATE `task_sub_template`
   SET `NAME` = 'Đưa Jaco về gặp %2', `max_count` = 1,
       `notify` = 'Về nhà giới thiệu Jaco với %2',
       `npc_id` = -2, `map` = -2
 WHERE `task_main_id` = 12 AND `ducvupro` = 41;

-- ---------------------------------------------------------------------
-- (2) Mô tả NV 34 / 37 / 39 — dòng thưởng khớp task_main_reward sub_index -1
-- ---------------------------------------------------------------------
UPDATE `task_main_template` SET `detail` = 'Một Mảnh Ký Ức bị đóng băng trong Hang băng, Cooler canh giữ.
Vượt vùng tuyết, nhặt Mảnh Ký Ức Đóng Băng rồi hạ Cooler.
Thưởng: 300 triệu SM, 300 triệu TN, 5 Đá bảo vệ, 3 Đá ngũ sắc'
WHERE `id` = 34;

UPDATE `task_main_template` SET `detail` = 'Tầng cuối phi thuyền không phải kho, nó là cái bụng.
Hạ Mabư, lấy Lõi Phép Babiđây mang tới Kibit ở Thánh địa Kaio.
Thưởng: 450 triệu SM, 450 triệu TN, 5 Đá bảo vệ, 5 Đá ngũ sắc'
WHERE `id` = 37;

UPDATE `task_main_template` SET `detail` = 'Bardock thấy trước cái chết của ngươi và chọn đổi chỗ cho ngươi.
Gom 7 viên Ngọc Rồng, ước, rồi hạ Baby ở Làng Kakarot.
Thưởng: 1 tỷ SM, 1 tỷ TN, 10 Đá bảo vệ, 50 Đậu thần cấp 8'
WHERE `id` = 39;

-- ---------------------------------------------------------------------
-- (3) Bảng thưởng — thay vật phẩm chỉ dùng cho đệ tử (SM/TN giữ nguyên, vàng/ngọc = 0)
-- ---------------------------------------------------------------------
UPDATE `task_main_reward`
   SET `items` = '[[295,1,[]],[1074,5,[]]]', `gold` = 0, `gem` = 0, `ruby` = 0,
       `text` = 'Thưởng 200.000 sức mạnh. Thưởng 200.000 tiềm năng. Thưởng 1 Gói 30 đậu thần cấp 3, 5 Đá nâng cấp cấp 1'
 WHERE `task_id` = 12 AND `sub_index` = -1 AND `gender` = -1;

UPDATE `task_main_reward`
   SET `items` = '[[987,5,[]],[674,3,[]]]', `gold` = 0, `gem` = 0, `ruby` = 0,
       `text` = 'Thưởng 300.000.000 sức mạnh. Thưởng 300.000.000 tiềm năng. Thưởng 5 Đá bảo vệ, 3 Đá ngũ sắc'
 WHERE `task_id` = 34 AND `sub_index` = -1 AND `gender` = -1;

UPDATE `task_main_reward`
   SET `items` = '[[352,10,[]]]', `gold` = 0, `gem` = 0, `ruby` = 0,
       `text` = 'Thưởng 40.000.000 sức mạnh. Thưởng 40.000.000 tiềm năng. Thưởng 10 Đậu thần cấp 8'
 WHERE `task_id` = 36 AND `sub_index` = 0 AND `gender` = -1;

UPDATE `task_main_reward`
   SET `items` = '[[987,5,[]],[674,5,[]]]', `gold` = 0, `gem` = 0, `ruby` = 0,
       `text` = 'Thưởng 450.000.000 sức mạnh. Thưởng 450.000.000 tiềm năng. Thưởng 5 Đá bảo vệ, 5 Đá ngũ sắc'
 WHERE `task_id` = 37 AND `sub_index` = -1 AND `gender` = -1;

UPDATE `task_main_reward`
   SET `items` = '[[987,10,[]],[352,50,[]]]', `gold` = 0, `gem` = 0, `ruby` = 0,
       `text` = 'Thưởng 1.000.000.000 sức mạnh. Thưởng 1.000.000.000 tiềm năng. Thưởng 10 Đá bảo vệ, 50 Đậu thần cấp 8'
 WHERE `task_id` = 39 AND `sub_index` = -1 AND `gender` = -1;

-- ---------------------------------------------------------------------
-- (4) Đưa người chơi đang ở NV 12 về đầu NV 12
--     Bước 0 cũ (có đệ tử) và bước 0 mới (gặp Jaco) khác nhau; bước 1 cũ đếm quái
--     "cùng đệ tử" => đặt lại hết cho khỏi lệch chỉ số bước / số đếm.
-- ---------------------------------------------------------------------
UPDATE `player`
   SET `data_task` = '[12,0,0,0]'
 WHERE JSON_VALID(`data_task`)
   AND CAST(JSON_EXTRACT(`data_task`, '$[0]') AS UNSIGNED) = 12
   AND `data_task` <> '[12,0,0,0]';

COMMIT;

-- =====================================================================
-- (5) KIỂM TRA — chạy xong xem từng câu, TẤT CẢ phải đúng kỳ vọng.
-- =====================================================================

-- K1: NV 12 có đúng 3 bước mới.  KỲ VỌNG: 3 dòng
--     39 | Gặp Jaco ở Trạm tàu vũ trụ | 1  | 63 | -6
--     40 | Cùng Jaco hạ 25 quái mẹ    | 25 | -1 | -11
--     41 | Đưa Jaco về gặp %2         | 1  | -2 | -2
SELECT ducvupro, `NAME`, max_count, npc_id, `map`, notify
FROM `task_sub_template` WHERE task_main_id = 12 ORDER BY ducvupro;

-- K2: tổng số nhiệm vụ / bước không đổi.  KỲ VỌNG: 51 và 237
SELECT (SELECT COUNT(*) FROM `task_main_template`) AS so_nhiem_vu,
       (SELECT COUNT(*) FROM `task_sub_template`)  AS so_buoc;

-- K3: không dòng thưởng nào còn vật phẩm chỉ dùng cho đệ tử
--     (400 thẻ đặt tên, 401 đổi đệ, 402-404/759 nâng kỹ năng đệ, 454/921/1819 bông tai Porata,
--      1628 bùa x2 đệ, 1758-1760 đổi chiêu đệ, 1795 bình hút năng lượng).  KỲ VỌNG: 0 dòng
SELECT task_id, sub_index, gender, items
FROM `task_main_reward`
WHERE items REGEXP '\\[(400|401|402|403|404|454|759|921|1628|1758|1759|1760|1795|1819),';

-- K4: không vàng / ngọc / hồng ngọc.  KỲ VỌNG: 0 dòng
SELECT task_id, sub_index, gender, gold, gem, ruby
FROM `task_main_reward` WHERE gold <> 0 OR gem <> 0 OR ruby <> 0;

-- K5: chữ nhiệm vụ không còn nhắc đệ tử / bông tai Porata.
--     KỲ VỌNG: đúng 1 dòng — NV 10 ducvupro 33 "xin làm đệ tử" (người chơi làm đệ tử của sư phụ,
--     không phải đệ tử của người chơi).
SELECT 'sub' AS bang, task_main_id AS nv, ducvupro, `NAME`, notify
FROM `task_sub_template`
WHERE `NAME` LIKE '%đệ tử%' OR notify LIKE '%đệ tử%' OR `NAME` LIKE '%Porata%' OR notify LIKE '%Porata%'
UNION ALL
SELECT 'main', id, NULL, `NAME`, detail
FROM `task_main_template`
WHERE detail LIKE '%đệ tử%' OR detail LIKE '%Porata%' OR detail LIKE '%trứng%';

-- K6: dòng thưởng theo bước khớp số bước (không thừa sub_index).  KỲ VỌNG: 0 dòng
SELECT r.task_id, r.sub_index
FROM `task_main_reward` r
JOIN (SELECT task_main_id, COUNT(*) AS n FROM `task_sub_template` GROUP BY task_main_id) c
  ON c.task_main_id = r.task_id
WHERE r.sub_index >= c.n;

-- K7: người chơi ở NV 12 đều đứng đầu nhiệm vụ.  KỲ VỌNG: 0 dòng
SELECT `id`, `name`, `data_task`
FROM `player`
WHERE JSON_VALID(`data_task`)
  AND CAST(JSON_EXTRACT(`data_task`, '$[0]') AS UNSIGNED) = 12
  AND `data_task` <> '[12,0,0,0]';

-- K8: số người chơi đã được đưa về đầu NV 12 (sao lưu ở player_task_backup_08).
SELECT COUNT(*) AS so_nguoi_choi_nv12_da_sao_luu FROM `player_task_backup_08`;

-- K9: độ dài chữ (đo trên chữ còn placeholder).  KỲ VỌNG: ten_buoc_dai_nhat <= 28, mo_ta_dai_nhat <= 200
SELECT (SELECT MAX(CHAR_LENGTH(`NAME`)) FROM `task_sub_template` WHERE task_main_id = 12) AS ten_buoc_dai_nhat,
       (SELECT MAX(CHAR_LENGTH(detail)) FROM `task_main_template` WHERE id IN (12, 34, 37, 39)) AS mo_ta_dai_nhat;

-- =====================================================================
-- (6) GỠ BỎ — chỉ khi cần lùi lại: nạp lại backup_truoc_08_*.sql, hoặc trả tiến độ:
--   UPDATE `player` p JOIN `player_task_backup_08` b ON b.id = p.id SET p.data_task = b.data_task_cu;
--   (chỉ đúng khi chạy cùng jar CŨ và dữ liệu nhiệm vụ CŨ.)
-- =====================================================================
