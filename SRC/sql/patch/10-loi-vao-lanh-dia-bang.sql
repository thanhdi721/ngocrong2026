-- =====================================================================
-- 10-loi-vao-lanh-dia-bang.sql  —  CÂU NHẮC + MŨI TÊN NV 13 BƯỚC 2
-- Database: team2026 (MariaDB 10.4)        Sinh ngày: 2026-09-19
-- Bàn giao: docs/4-trien-khai/45-loi-vao-lanh-dia-bang.md
--
-- Bước "Gặp Giu-ma Đầu Bò" (TASK_13_2, task_main_id 13, ducvupro 44) nằm ở
-- map 153 Lãnh địa Bang Hội — map KHÔNG có cửa đi bộ, chỉ vào qua sư phụ:
-- Quy Lão Kame (map 5) / Trưởng lão Guru (map 13) / Vua Vegeta (map 20)
-- -> "Về khu vực bang".
--   * notify: chỉ rõ đường đi. %10 = tên sư phụ theo hành tinh
--     (TaskService.transformName). Dài nhất (Namếc) 87 ký tự <= 100.
--   * map: 153 -> -9 (ConstTask.MAP_QUY_LAO -> 5 / 13 / 20 theo hành tinh),
--     mũi tên chỉ tới sư phụ thay vì map không đi bộ tới được.
--   * npc_id giữ 47 (Giu-ma Đầu Bò) — hệ thống nhiệm vụ cần đúng NPC này.
--
-- AN TOÀN: chỉ UPDATE 1 dòng, khớp theo task_main_id + ducvupro; không đụng
-- tiến độ người chơi; chạy lại nhiều lần cho cùng kết quả.
--
-- CÁCH CHẠY:
--   mysql -u root -p team2026 < SRC/sql/patch/10-loi-vao-lanh-dia-bang.sql
--   rồi KHỞI ĐỘNG LẠI server (Manager chỉ nạp nhiệm vụ lúc khởi động).
--   Code đi kèm (menu NPC) cần build lại jar.
-- =====================================================================

SET NAMES utf8mb4;

UPDATE `task_sub_template`
SET `notify` = 'Gặp %10, chọn "Về khu vực bang" để vào Lãnh địa Bang Hội, gặp Giu-ma Đầu Bò',
    `map`    = -9
WHERE `task_main_id` = 13 AND `ducvupro` = 44;

-- Kiểm tra: phải ra đúng 1 dòng, npc_id = 47, map = -9.
SELECT `task_main_id`, `NAME`, `notify`, `npc_id`, `map`, `ducvupro`
FROM `task_sub_template`
WHERE `task_main_id` = 13 AND `ducvupro` = 44;
