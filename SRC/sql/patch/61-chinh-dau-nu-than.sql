-- =====================================================================
-- 61 — CHỈNH ĐẦU NỮ THẦN BĂNG TINH (bản nhỏ của boss) — 2026-09-24
-- =====================================================================
-- Đầu hạ xuống 8, sang phải 2 cho khớp thân. Chỉ đụng part 2367 (bản nhỏ dùng cho
-- nhân vật boss), cải trang 2079 người chơi mặc KHÔNG đổi.
-- Patch 60 đã sửa theo, chạy thứ tự nào cũng ra kết quả này.
-- Chạy cùng jar có DataGame.vsData = 28. Chạy lại vẫn an toàn.
-- =====================================================================

UPDATE `part` SET `DATA` = '[[14771,-19,-27],[14803,-14,-24],[2955,0,0]]' WHERE `id` = 2367 AND `TYPE` = 0;

SELECT `id`, `DATA` FROM `part` WHERE `id` = 2367;
