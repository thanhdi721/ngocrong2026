-- =====================================================================
-- 47 — 2079 Nữ Thần: đầu lên 3 sang trái 1, tắt đầu động (hết nháy)
-- =====================================================================
-- Patch 40 và các patch chỉnh trước đã sửa theo -> chạy thứ tự nào cũng ra kết quả cuối này.
-- Chạy cùng jar có DataGame.vsData = 21 và vsItem = 15 (bảng đầu động đi trong gói vật phẩm). Chạy lại vẫn an toàn.
-- =====================================================================

UPDATE `part` SET `DATA` = '[[30694,-38,-64],[30697,-29,-58],[2955,0,0]]' WHERE `id` = 2233 AND `TYPE` = 0;
UPDATE `part` SET `DATA` = '[[30695,-38,-64],[30697,-29,-58],[2955,0,0]]' WHERE `id` = 2234 AND `TYPE` = 0;
UPDATE `part` SET `DATA` = '[[30696,-38,-64],[30697,-29,-58],[2955,0,0]]' WHERE `id` = 2235 AND `TYPE` = 0;

-- Tắt đầu động: 3 khung đầu có hào quang khác màu hẳn nhau (tím / hồng / xanh) nên
-- client quay vòng trông như đầu nháy liên tục. Bỏ dòng này -> đầu đứng yên ở khung 1.
DELETE FROM `array_head_2_frames` WHERE `id` = 56 AND `data` = '[2233,2234,2235]';

SELECT `id`, LEFT(`DATA`, 60) FROM `part` WHERE `id` IN (2233, 2234, 2235);
