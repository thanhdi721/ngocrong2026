-- =====================================================================
-- 03-reset-tien-do.sql  —  ĐƯA TOÀN BỘ NGƯỜI CHƠI VỀ NHIỆM VỤ 0
-- Database: team2026        Sinh ngày: 2026-09-18
--
-- Đi kèm: 02-nhiem-vu-moi.sql (phải chạy TRƯỚC file này)
-- Quyết định gốc: docs/4-trien-khai/22-san-sang-code.md §0 mục 10
--                 -> "Quà bù khi reset người chơi: KHÔNG CÓ"
--
-- ---------------------------------------------------------------------
-- !!!!!!!!!!!!!!  CẢNH BÁO — FILE NÀY XÓA TIẾN ĐỘ NGƯỜI CHƠI  !!!!!!!!!!
-- ---------------------------------------------------------------------
-- File này ghi đè cột `player`.`data_task` của MỌI nhân vật trên server.
-- Toàn bộ tiến độ nhiệm vụ chính của tuyến cũ BIẾN MẤT và KHÔNG LẤY LẠI
-- ĐƯỢC nếu chưa sao lưu.
--
--   mysqldump -u root -p team2026 player > backup_player_$(date +%F).sql
--
-- KHÔNG có quà bù (chủ dự án đã chốt). File này CHỈ đặt lại tiến độ,
-- không cộng sức mạnh, không cộng vật phẩm, không đụng vào hành trang,
-- rương đồ, trang bị, vàng, ngọc hay bang hội.
--
-- ---------------------------------------------------------------------
-- VÌ SAO BẮT BUỘC PHẢI RESET
-- ---------------------------------------------------------------------
-- `data_task` là JSON [taskId, index, count, lastTime].
-- MrBlue.loadPlayer (dòng ~722) đọc thẳng:
--
--     TaskMain taskMain = TaskService.gI().getTaskMainById(player, <taskId>);
--     taskMain.index = <index>;
--     taskMain.subTasks.get(taskMain.index).count = <count>;
--
-- Nhiệm vụ mới có SỐ BƯỚC KHÁC nhiệm vụ cũ cùng id (ví dụ nhiệm vụ 0 cũ có
-- 6 bước, mới có 5 bước). Người chơi đang ở [0, 5, ...] sẽ làm
-- `subTasks.get(5)` ném IndexOutOfBoundsException => KHÔNG ĐĂNG NHẬP ĐƯỢC.
-- Người chơi ở taskId 29 (id lớn nhất của tuyến cũ) thì id vẫn tồn tại
-- nhưng nội dung nhiệm vụ đã khác hẳn => tiến độ vô nghĩa.
--
-- ---------------------------------------------------------------------
-- TRÌNH TỰ CHẠY — ĐỌC KỸ
-- ---------------------------------------------------------------------
--   1) TẮT SERVER trước khi chạy. Nếu server còn chạy, người chơi đang
--      online sẽ ghi đè lại `data_task` cũ khi PlayerDAO lưu định kỳ /
--      lúc thoát, và reset coi như không có tác dụng với họ.
--   2) mysqldump player  (sao lưu)
--   3) mysql team2026 < 01-vat-pham-moi.sql
--   4) mysql team2026 < 02-nhiem-vu-moi.sql
--   5) mysql team2026 < 03-reset-tien-do.sql   (file này)
--   6) build lại 20.jar, bật server
-- =====================================================================

SET NAMES utf8mb4;

-- ---------------------------------------------------------------------
-- (1) ẢNH CHỤP TRƯỚC KHI RESET — chạy trước, lưu kết quả lại để đối chiếu.
--     Đây cũng là số liệu duy nhất còn lại về tiến độ cũ nếu sau này chủ
--     dự án đổi ý và muốn phát quà bù thủ công.
-- ---------------------------------------------------------------------
SELECT CAST(JSON_EXTRACT(`data_task`, '$[0]') AS UNSIGNED) AS task_id_cu,
       COUNT(*)                                            AS so_nhan_vat
FROM `player`
WHERE JSON_VALID(`data_task`)
GROUP BY task_id_cu
ORDER BY task_id_cu;

-- Bản dự phòng cho bản MySQL/MariaDB không có JSON_EXTRACT:
-- SELECT SUBSTRING_INDEX(SUBSTRING_INDEX(`data_task`, ',', 1), '[', -1) AS task_id_cu,
--        COUNT(*) AS so_nhan_vat
-- FROM `player` GROUP BY task_id_cu ORDER BY CAST(task_id_cu AS UNSIGNED);

-- ---------------------------------------------------------------------
-- (2) BẢNG SAO LƯU TIẾN ĐỘ CŨ — rẻ tiền, giữ lại phòng khi cần.
--     Không bắt buộc, nhưng nên chạy: 1 dòng / nhân vật, vài MB là cùng.
-- ---------------------------------------------------------------------
DROP TABLE IF EXISTS `player_task_backup_2026`;
CREATE TABLE `player_task_backup_2026` (
  `id`             int(11)   NOT NULL,
  `name`           varchar(20) NOT NULL,
  `data_task_cu`   text      NOT NULL,
  `backup_time`    timestamp NOT NULL DEFAULT current_timestamp(),
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

INSERT INTO `player_task_backup_2026` (`id`, `name`, `data_task_cu`)
SELECT `id`, `name`, `data_task` FROM `player`;

-- ---------------------------------------------------------------------
-- (3) RESET — đưa TẤT CẢ về nhiệm vụ 0, bước 0, count 0, lastTime 0.
--
--     Định dạng phải đúng 4 phần tử: [taskId, index, count, lastTime].
--     MrBlue.loadPlayer chấp nhận cả mảng 3 phần tử (khi đó lastTime =
--     System.currentTimeMillis()), nhưng ghi đủ 4 cho sạch — bước đếm giờ
--     B12 đọc phần tử thứ 4 này.
--
--     KHÔNG CÓ QUÀ BÙ. Không có UPDATE nào cộng sức mạnh / tiềm năng /
--     vàng / ngọc / vật phẩm ở đây, và cũng KHÔNG ĐƯỢC thêm vào.
-- ---------------------------------------------------------------------
START TRANSACTION;

UPDATE `player` SET `data_task` = '[0,0,0,0]';

COMMIT;

-- =====================================================================
-- (4) KIỂM TRA SAU KHI RESET — chạy cả 4 câu.
-- =====================================================================

-- 4.1 Mọi nhân vật phải về đúng một giá trị.  KỲ VỌNG: đúng 1 dòng,
--     gia_tri = '[0,0,0,0]', so_nhan_vat = tổng số nhân vật của server.
SELECT `data_task` AS gia_tri, COUNT(*) AS so_nhan_vat
FROM `player`
GROUP BY `data_task`;

-- 4.2 Không còn nhân vật nào ở nhiệm vụ khác 0.  KỲ VỌNG: 0 dòng trả về.
SELECT `id`, `name`, `data_task`
FROM `player`
WHERE `data_task` <> '[0,0,0,0]';

-- 4.3 Nhiệm vụ 0 của tuyến mới phải tồn tại và có ít nhất 1 bước, nếu không
--     mọi nhân vật sẽ không đăng nhập được.  KỲ VỌNG: so_buoc = 5.
SELECT m.id, m.`NAME`, COUNT(s.ducvupro) AS so_buoc
FROM `task_main_template` m
LEFT JOIN `task_sub_template` s ON s.task_main_id = m.id
WHERE m.id = 0
GROUP BY m.id, m.`NAME`;

-- 4.4 Số dòng sao lưu phải bằng số nhân vật.  KỲ VỌNG: hai số bằng nhau.
SELECT (SELECT COUNT(*) FROM `player`)                     AS so_nhan_vat,
       (SELECT COUNT(*) FROM `player_task_backup_2026`)    AS so_dong_sao_luu;

-- =====================================================================
-- (5) LÙI LẠI — chỉ dùng khi buộc phải quay về tuyến cũ.
--     Phải nạp lại dump tuyến nhiệm vụ cũ TRƯỚC, rồi mới trả tiến độ về,
--     nếu không người chơi sẽ đứng ở index không tồn tại.
-- =====================================================================
-- SOURCE backup_task_YYYY-MM-DD.sql;
-- START TRANSACTION;
-- UPDATE `player` p
--   JOIN `player_task_backup_2026` b ON b.id = p.id
--   SET p.`data_task` = b.`data_task_cu`;
-- COMMIT;

-- Khi đã chắc chắn không lùi nữa thì dọn bảng sao lưu:
-- DROP TABLE `player_task_backup_2026`;
