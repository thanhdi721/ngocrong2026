-- =====================================================================
-- 05-sua-huong-dan-tan-thu.sql  —  SỬA LỖI NGƯỜI MỚI MẤT HẾT GIAO DIỆN
-- Database: team2026        Chạy trên: MariaDB 10.4 (không dùng JSON_TABLE
--                           hay hàm chỉ MySQL 8 mới có)
-- Tài liệu: docs/4-trien-khai/39-sua-loi-mat-giao-dien-tan-thu.md
--
-- DÀNH CHO SERVER ĐÃ IMPORT 02-nhiem-vu-moi.sql (bản cũ). Server cài mới
-- chỉ cần chạy 02-nhiem-vu-moi.sql bản đã sửa; chạy thêm file này cũng
-- không sao (mọi lệnh đều cho cùng một kết quả nếu chạy lại).
--
-- File này:
--   (1) sao lưu data_task của người chơi đang ở NV 0–3
--   (2) xóa rồi nạp lại ĐÚNG các dòng task_main_template / task_sub_template
--       của NV 0–3 theo CƠ CHẾ TUYẾN GỐC (6 / 2 / 2 / 3 bước) — chỉ đổi chữ
--   (3) nạp lại dòng thưởng theo BƯỚC (sub_index >= 0) của NV 0–3 cho khớp
--       số bước mới; dòng thưởng hoàn thành (sub_index = -1) giữ nguyên
--   (4) đổi map của 6 bước NV 17 / NV 33 từ -4 sang placeholder mới -10
--       (-4 nay lại là 39/40/41 như tuyến gốc, -10 = 42/43/44)
--   (5) đưa MỌI người chơi đang ở NV 0–3 về ĐẦU nhiệm vụ đó [id,0,0,0]
--
-- ---------------------------------------------------------------------
-- !!!!!!!!!!!!!!!!!!!!!!  CẢNH BÁO — SAO LƯU TRƯỚC  !!!!!!!!!!!!!!!!!!!!!
-- ---------------------------------------------------------------------
--   mysqldump -u root -p team2026 task_main_template task_sub_template \
--     task_main_reward player > backup_truoc_05_$(date +%F).sql
--
-- THỨ TỰ TRIỂN KHAI:
--   1) build lại jar từ mã nguồn đã sửa (TaskService, ConstTask, Player,
--      Zone, Mob, ChangeMapService)
--   2) TẮT server (bảo trì). Nếu server còn chạy, luồng tự lưu sẽ ghi đè
--      data_task vừa sửa bằng chỉ số bước cũ trong bộ nhớ.
--   3) sao lưu như trên, rồi chạy file này
--   4) chạy mục KIỂM TRA ở cuối file, tất cả phải đúng kỳ vọng
--   5) thay jar mới, khởi động lại server (Manager nạp lại nhiệm vụ lúc khởi động)
-- =====================================================================

SET NAMES utf8mb4;

-- ---------------------------------------------------------------------
-- (1) SAO LƯU data_task CỦA NGƯỜI CHƠI SẮP BỊ ĐƯA VỀ ĐẦU NHIỆM VỤ
--     (CREATE TABLE tự commit ngầm nên đặt NGOÀI transaction)
-- ---------------------------------------------------------------------
CREATE TABLE IF NOT EXISTS `player_task_backup_05` (
  `stt`          int(11)     NOT NULL AUTO_INCREMENT,
  `id`           int(11)     NOT NULL,
  `name`         varchar(20) NOT NULL,
  `data_task_cu` text        NOT NULL,
  `backup_time`  timestamp   NOT NULL DEFAULT current_timestamp(),
  PRIMARY KEY (`stt`),
  KEY `id` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

INSERT INTO `player_task_backup_05` (`id`, `name`, `data_task_cu`)
SELECT `id`, `name`, `data_task`
FROM `player`
WHERE JSON_VALID(`data_task`)
  AND CAST(JSON_EXTRACT(`data_task`, '$[0]') AS UNSIGNED) <= 3;

START TRANSACTION;

-- ---------------------------------------------------------------------
-- (2a) NV 0–3 — task_main_template (chỉ đổi chữ)
-- ---------------------------------------------------------------------
DELETE FROM `task_main_template` WHERE `id` IN (0, 1, 2, 3);

INSERT INTO `task_main_template` (`id`, `NAME`, `detail`) VALUES
(0, 'Người duy nhất còn nhớ', 'Ngươi tỉnh dậy bên vách núi, trong ngực le lói ánh sáng lạ.
Về nhà %2, lấy rađa trong rương rồi hái đậu thần.
Thưởng: 2.000 SM, 2.000 TN, 5 Đậu thần cấp 1, 1 Gói Capsule'),
(1, 'Bài học của ông', 'Ông quên tên ngươi, nhưng tay ông vẫn nhớ cách dạy võ.
Đánh ngã 5 mộc nhân ở %1 rồi về khoe với %2.
Thưởng: 3.000 SM, 3.000 TN, 1 Rada cấp 1'),
(2, 'Vết nứt đầu tiên', 'Trời trên %3 rách toạc, lũ %4 phát điên phá nát ruộng làng.
Hạ %4, nhặt 10 đùi gà mang về cho %2.
Thưởng: 4.000 SM, 4.000 TN, 10 Đậu thần cấp 1'),
(3, 'Cảnh sát vũ trụ Jaco', 'Tiếng nổ vang từ vách núi, có thứ gì vừa rơi xuống.
Cộng tiềm năng, tìm vật thể lạ rồi mang về cho %2.
Thưởng: 5.000 SM, 5.000 TN, 1 bộ đồ vải cấp 1');

-- ---------------------------------------------------------------------
-- (2b) NV 0–3 — task_sub_template: CƠ CHẾ Y NGUYÊN TUYẾN GỐC
--      cùng số bước, thứ tự, max_count, npc_id, map (kể cả placeholder âm).
--      ducvupro 1..13 như dump gốc; NV 4 bắt đầu ở 15 nên 14 bỏ trống
--      (Manager nạp theo ORDER BY task_main_template.id, ducvupro).
-- ---------------------------------------------------------------------
DELETE FROM `task_sub_template` WHERE `task_main_id` IN (0, 1, 2, 3);

INSERT INTO `task_sub_template` (`task_main_id`, `NAME`, `max_count`, `notify`, `npc_id`, `map`, `ducvupro`) VALUES
(0, 'Đi theo mũi tên chỉ dẫn', 1, '', -1, -1, 1),
(0, 'Về nhà %2', 1, '', -2, -2, 2),
(0, 'Gặp %2', 1, '', -2, -2, 3),
(0, 'Mở rương đồ lấy rađa', 1, '', 3, -2, 4),
(0, 'Thu hoạch đậu thần', 1, '', 4, -2, 5),
(0, 'Về báo %2', 1, '', -2, -2, 6),
(1, 'Đánh ngã 5 mộc nhân', 5, 'Mộc nhân cũ vẫn đứng ở %1. Đánh ngã 5 con cho ông xem', -1, -1, 7),
(1, 'Về khoe với %2', 1, 'Mộc nhân đổ cả rồi. Về khoe với %2 thôi', -2, -2, 8),
(2, 'Hạ %4 lấy 10 đùi gà', 10, 'Lên %3 hạ lũ %4, nhặt đủ 10 đùi gà', -1, -3, 9),
(2, 'Đưa đùi gà cho %2', 1, 'Đủ 10 đùi gà rồi. Mang về cho %2 kẻo ông đói', -2, -2, 10),
(3, 'Cộng điểm tiềm năng', 1, '', -1, -1, 11),
(3, 'Tìm vật thể lạ rơi xuống', 1, '', -1, -4, 12),
(3, 'Đưa vật lạ cho %2', 1, 'Mang thứ vừa tìm được về cho %2 xem', -2, -2, 13);

-- ---------------------------------------------------------------------
-- (3) THƯỞNG THEO BƯỚC của NV 0–3 — chỉ sức mạnh / tiềm năng, KHÔNG vàng/ngọc.
--     Dòng sub_index = -1 (thưởng hoàn thành, có vật phẩm) giữ nguyên.
-- ---------------------------------------------------------------------
DELETE FROM `task_main_reward` WHERE `task_id` IN (0, 1, 2, 3) AND `sub_index` >= 0;

INSERT INTO `task_main_reward`
(`task_id`, `sub_index`, `gender`, `sm`, `tn`, `gold`, `gem`, `ruby`, `items`, `text`) VALUES
(0, 0, -1, 200, 200, 0, 0, 0, '[]', 'Thưởng 200 sức mạnh. Thưởng 200 tiềm năng'),
(0, 1, -1, 200, 200, 0, 0, 0, '[]', 'Thưởng 200 sức mạnh. Thưởng 200 tiềm năng'),
(0, 2, -1, 200, 200, 0, 0, 0, '[]', 'Thưởng 200 sức mạnh. Thưởng 200 tiềm năng'),
(0, 3, -1, 200, 200, 0, 0, 0, '[]', 'Thưởng 200 sức mạnh. Thưởng 200 tiềm năng'),
(0, 4, -1, 200, 200, 0, 0, 0, '[]', 'Thưởng 200 sức mạnh. Thưởng 200 tiềm năng'),
(0, 5, -1, 200, 200, 0, 0, 0, '[]', 'Thưởng 200 sức mạnh. Thưởng 200 tiềm năng'),
(1, 0, -1, 300, 300, 0, 0, 0, '[]', 'Thưởng 300 sức mạnh. Thưởng 300 tiềm năng'),
(1, 1, -1, 300, 300, 0, 0, 0, '[]', 'Thưởng 300 sức mạnh. Thưởng 300 tiềm năng'),
(2, 0, -1, 400, 400, 0, 0, 0, '[]', 'Thưởng 400 sức mạnh. Thưởng 400 tiềm năng'),
(2, 1, -1, 400, 400, 0, 0, 0, '[]', 'Thưởng 400 sức mạnh. Thưởng 400 tiềm năng'),
(3, 0, -1, 500, 500, 0, 0, 0, '[]', 'Thưởng 500 sức mạnh. Thưởng 500 tiềm năng'),
(3, 1, -1, 500, 500, 0, 0, 0, '[]', 'Thưởng 500 sức mạnh. Thưởng 500 tiềm năng'),
(3, 2, -1, 500, 500, 0, 0, 0, '[]', 'Thưởng 500 sức mạnh. Thưởng 500 tiềm năng');

-- ---------------------------------------------------------------------
-- (4) PLACEHOLDER MỚI -10 (MAP_VACH_NUI_LANG = 42/43/44) cho NV 4 trở đi.
--     Chỉ NV 17 (Bà Hạt Mít, ducvupro 56, 57) và NV 33 (Quốc Vương,
--     ducvupro 135, 136, 138, 140) dùng -4 với nghĩa 42/43/44.
--     Chỉ đổi cột `map`; số bước / chỉ số bước KHÔNG đổi nên người chơi
--     đang ở NV 17 / NV 33 không bị ảnh hưởng tiến độ.
-- ---------------------------------------------------------------------
UPDATE `task_sub_template`
SET `map` = -10
WHERE `task_main_id` IN (17, 33) AND `map` = -4;

-- ---------------------------------------------------------------------
-- (5) ĐƯA NGƯỜI CHƠI ĐANG Ở NV 0–3 VỀ ĐẦU NHIỆM VỤ ĐÓ
--     data_task = [taskId, index, count, lastTime] -> [taskId, 0, 0, 0]
--     Người đang đứng ở map nhà với [0,0,0,0] sẽ được Player.update tự đẩy
--     sang bước 2 "Nói chuyện với ông" (đoạn giải cứu của tuyến gốc).
-- ---------------------------------------------------------------------
UPDATE `player`
SET `data_task` = CONCAT('[', CAST(JSON_EXTRACT(`data_task`, '$[0]') AS UNSIGNED), ',0,0,0]')
WHERE JSON_VALID(`data_task`)
  AND CAST(JSON_EXTRACT(`data_task`, '$[0]') AS UNSIGNED) <= 3;

COMMIT;

-- =====================================================================
-- KIỂM TRA — chạy hết, TẤT CẢ phải đúng kỳ vọng rồi mới khởi động server
-- =====================================================================

-- K1. Số bước NV 0–3.  KỲ VỌNG: 0 -> 6, 1 -> 2, 2 -> 2, 3 -> 3
SELECT task_main_id, COUNT(*) AS so_buoc, MIN(ducvupro) AS tu, MAX(ducvupro) AS den
FROM `task_sub_template`
WHERE task_main_id IN (0, 1, 2, 3)
GROUP BY task_main_id
ORDER BY task_main_id;

-- K2. Tổng số bước.  KỲ VỌNG: 237
SELECT COUNT(*) AS tong_so_buoc FROM `task_sub_template`;

-- K3. Không trùng ducvupro.  KỲ VỌNG: 0 dòng
SELECT ducvupro, COUNT(*) AS n FROM `task_sub_template`
GROUP BY ducvupro HAVING COUNT(*) > 1;

-- K4. -4 chỉ còn ở NV 3 bước 1; -10 đúng 6 dòng của NV 17 / NV 33.
--     KỲ VỌNG: (map -4: task 3, 1 dòng) và (map -10: task 17 -> 2, task 33 -> 4)
SELECT `map`, task_main_id, COUNT(*) AS n
FROM `task_sub_template`
WHERE `map` IN (-4, -10)
GROUP BY `map`, task_main_id
ORDER BY `map`, task_main_id;

-- K5. Dòng thưởng theo bước khớp số bước.  KỲ VỌNG: 0 dòng
SELECT r.task_id, r.sub_index, 'thua dong thuong' AS loi
FROM `task_main_reward` r
JOIN (SELECT task_main_id, COUNT(*) AS n FROM `task_sub_template` GROUP BY task_main_id) c
  ON c.task_main_id = r.task_id
WHERE r.sub_index >= c.n;

-- K6. Thưởng không có vàng / ngọc / hồng ngọc.  KỲ VỌNG: 0 dòng
SELECT task_id, sub_index, gender FROM `task_main_reward`
WHERE gold <> 0 OR gem <> 0 OR ruby <> 0;

-- K7. Không còn người chơi NV 0–3 bị lệch chỉ số bước.  KỲ VỌNG: 0 dòng
SELECT `id`, `name`, `data_task` FROM `player`
WHERE JSON_VALID(`data_task`)
  AND CAST(JSON_EXTRACT(`data_task`, '$[0]') AS UNSIGNED) <= 3
  AND CAST(JSON_EXTRACT(`data_task`, '$[1]') AS UNSIGNED) <> 0;

-- K8. Phân bố người chơi theo nhiệm vụ (xem cho biết)
SELECT CAST(JSON_EXTRACT(`data_task`, '$[0]') AS UNSIGNED) AS task_id, COUNT(*) AS so_nhan_vat
FROM `player`
WHERE JSON_VALID(`data_task`)
GROUP BY task_id
ORDER BY task_id;

-- =====================================================================
-- GỠ BỎ (chỉ khi cần lùi lại): nạp lại file mysqldump đã sao lưu ở trên.
-- Riêng data_task người chơi có thể trả lại từ bảng sao lưu:
--   UPDATE `player` p
--   JOIN (SELECT b.id, b.data_task_cu FROM `player_task_backup_05` b
--         JOIN (SELECT id, MAX(stt) AS stt FROM `player_task_backup_05` GROUP BY id) m
--           ON m.stt = b.stt) x ON x.id = p.id
--   SET p.data_task = x.data_task_cu;
-- =====================================================================
