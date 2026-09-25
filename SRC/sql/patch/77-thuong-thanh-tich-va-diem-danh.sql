-- =====================================================================
-- 77 — CHIA LẠI THƯỞNG NHIỆM VỤ THÀNH TÍCH: TỔNG ĐÚNG 30.000 NGỌC (2026-09-25)
-- =====================================================================
-- Cột `money` của bảng này thực chất trả về NGỌC (AchievementService.confirmAchievement:
-- `player.inventory.gem += money`). Chữ "Thỏi Vàng" trên màn hình là do CLIENT tự vẽ,
-- server không đổi được — nhưng người chơi nhận đúng ngọc.
--
-- Trước: tổng 60.335 (riêng 2 mốc chiếm 60.000, lại vượt trần short nên hiện sai số).
-- Sau  : tổng ĐÚNG 30.000, mốc khó ăn nhiều hơn, không mốc nào vượt 32.767.
--
-- Chia theo độ khó:
--   5.000  Tuyệt kỹ thành thạo (7.749 lần) — khó nhất
--   3.800  Trùm kết liễu Boss (2.000 lần)
--   2.500  Hoạt động chăm chỉ (120 giờ)
--   2.000  Trăm trận trăm thắng · Đạt 15 triệu sức mạnh · Chăm sóc đặc biệt
--   1.500  Hỗ trợ đồng đội (10.000 đậu)
--   1.200  Trùm nhặt ngọc
--   1.000  Nông dân chăm chỉ · Thợ săn thiện xạ · Lần đầu nạp ngọc · Đánh bại siêu quái
--          · Kỹ năng thành thạo
--     800  Sức mạnh siêu cấp · Nội công cao cường · Thánh hồi sinh
--     700  Khinh công · Tập luyện bài bản · Trùm nhặt ve chai
--     500  Gia nhập Vệ Binh (mốc đầu, dễ nhất)
--
-- Không cần đổi code, không cần tăng version. Chạy lại vẫn an toàn.
-- =====================================================================

SET NAMES utf8mb4;

UPDATE `achievement_template` SET `money` =  500 WHERE `id` =  1;  -- Gia nhập Vệ Binh
UPDATE `achievement_template` SET `money` =  800 WHERE `id` =  2;  -- Sức mạnh siêu cấp
UPDATE `achievement_template` SET `money` = 1000 WHERE `id` =  3;  -- Nông dân chăm chỉ
UPDATE `achievement_template` SET `money` = 2000 WHERE `id` =  4;  -- Trăm trận trăm thắng
UPDATE `achievement_template` SET `money` =  800 WHERE `id` =  5;  -- Nội công cao cường
UPDATE `achievement_template` SET `money` =  700 WHERE `id` =  6;  -- Khinh công thành thạo
UPDATE `achievement_template` SET `money` = 1000 WHERE `id` =  7;  -- Thợ săn thiện xạ
UPDATE `achievement_template` SET `money` =  700 WHERE `id` =  8;  -- Tập luyện bài bản
UPDATE `achievement_template` SET `money` = 2500 WHERE `id` =  9;  -- Hoạt động chăm chỉ
UPDATE `achievement_template` SET `money` = 1500 WHERE `id` = 10;  -- Hỗ trợ đồng đội
UPDATE `achievement_template` SET `money` =  700 WHERE `id` = 11;  -- Trùm nhặt ve chai
UPDATE `achievement_template` SET `money` = 1000 WHERE `id` = 12;  -- Lần đầu nạp ngọc
UPDATE `achievement_template` SET `money` = 1000 WHERE `id` = 13;  -- Đánh bại siêu quái
UPDATE `achievement_template` SET `money` =  800 WHERE `id` = 14;  -- Thánh hồi sinh
UPDATE `achievement_template` SET `money` = 1000 WHERE `id` = 15;  -- Kỹ năng thành thạo
UPDATE `achievement_template` SET `money` = 1200 WHERE `id` = 16;  -- Trùm nhặt ngọc
UPDATE `achievement_template` SET `money` = 2000 WHERE `id` = 17;  -- Đạt 15 triệu sức mạnh
UPDATE `achievement_template` SET `money` = 5000 WHERE `id` = 18;  -- Tuyệt kỹ thành thạo
UPDATE `achievement_template` SET `money` = 2000 WHERE `id` = 19;  -- Chăm sóc đặc biệt
UPDATE `achievement_template` SET `money` = 3800 WHERE `id` = 20;  -- Trùm kết liễu Boss

-- KIỂM TRA: tong phải = 30000, lon_nhat phải <= 32767 (cột money gửi client bằng short).
SELECT SUM(`money`) AS tong, MAX(`money`) AS lon_nhat, COUNT(*) AS so_moc FROM `achievement_template`;
SELECT `id`, `info1`, `money` FROM `achievement_template` ORDER BY `id`;
