-- =====================================================================
-- 41 — CHỈNH VỊ TRÍ ĐẦU 3 CẢI TRANG MỚI (2026-09-22)
-- =====================================================================
-- Client vẽ ĐẦU TRƯỚC rồi mới vẽ thân, nên đầu đặt thấp bị thân che mặt:
--   2077 Hắc Ảnh Sát Thần : đầu sang phải 2, lên 4  (khăn đỏ che cằm, đầu lệch trái)
--   2078 Tử Lôi Chiến Thần: đầu lên 12              (thân che gần hết mặt)
--   2079 Nữ Thần Băng Tinh: đầu lên 5               (lộ cổ cho tự nhiên)
-- Chỉ sửa 3 khung đầu của mỗi bộ (mảnh đầu + mảnh tóc), thân / chân giữ nguyên.
-- Patch 40 cũng đã sửa theo, chạy 40 lại vẫn ra đúng kết quả này.
--
-- Chạy cùng jar có DataGame.vsData = 15 (client tải lại part). Chạy lại vẫn an toàn.
-- =====================================================================

UPDATE `part` SET `DATA` = '[[29768,-11,-51],[29771,-20,-47],[2955,0,0]]' WHERE `id` = 2223 AND `TYPE` = 0;
UPDATE `part` SET `DATA` = '[[29769,-11,-51],[29771,-20,-47],[2955,0,0]]' WHERE `id` = 2224 AND `TYPE` = 0;
UPDATE `part` SET `DATA` = '[[29770,-11,-51],[29771,-20,-47],[2955,0,0]]' WHERE `id` = 2225 AND `TYPE` = 0;
UPDATE `part` SET `DATA` = '[[29803,-27,-63],[29806,-26,-66],[2955,0,0]]' WHERE `id` = 2228 AND `TYPE` = 0;
UPDATE `part` SET `DATA` = '[[29804,-27,-63],[29806,-26,-66],[2955,0,0]]' WHERE `id` = 2229 AND `TYPE` = 0;
UPDATE `part` SET `DATA` = '[[29805,-27,-63],[29806,-26,-66],[2955,0,0]]' WHERE `id` = 2230 AND `TYPE` = 0;
UPDATE `part` SET `DATA` = '[[30694,-39,-57],[30697,-30,-51],[2955,0,0]]' WHERE `id` = 2233 AND `TYPE` = 0;
UPDATE `part` SET `DATA` = '[[30695,-39,-57],[30697,-30,-51],[2955,0,0]]' WHERE `id` = 2234 AND `TYPE` = 0;
UPDATE `part` SET `DATA` = '[[30696,-39,-57],[30697,-30,-51],[2955,0,0]]' WHERE `id` = 2235 AND `TYPE` = 0;

SELECT `id`, `DATA` FROM `part` WHERE `id` IN (2223,2224,2225,2228,2229,2230,2233,2234,2235);
