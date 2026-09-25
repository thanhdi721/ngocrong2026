-- =====================================================================
-- 73 — SỬA 7 MÓN BỊ LỖI HÌNH CỦA ĐỢT MANG RES SUMO VỀ (2026-09-25)
-- =====================================================================
-- Chạy SAU patch 67–72. Lý do từng món:
--
-- 1) Pét cá mập (2161): bên nguồn vẽ con vật ở CẢ BA part (đầu 1 mảnh, thân 1 mảnh,
--    chân 6 mảnh) vì hệ pet của họ chỉ vẽ một part. Client mình vẽ đủ đầu+thân+chân
--    nên hiện ra BA con chồng lên nhau. Xoá hình ở đầu và thân, giữ chân (phần có
--    đủ 6 khung động) -> giống cách các linh thú cũ đang chạy tốt.
--
-- 2) Pet mèo trắng (2218) và Pet mèo trắng 1 (2219): ngoài lỗi chồng hình ở part đầu,
--    trong 9 khung chân có 2 khung KHÔNG PHẢI mèo (một mặt nạ, một con rồng trắng) —
--    kho icon bên nguồn đã lệch số. Thay 2 khung đó bằng khung mèo kề bên, và đổi icon
--    vật phẩm (đang là cái mặt nạ) sang hình mèo.
--
-- 3) Thần Hỏa (2221): hình đeo lưng đúng, nhưng cột icon_id bên nguồn ghi là 1 nên
--    trong hành trang hiện nhầm một icon khác. Lấy khung đầu tiên làm icon.
--
-- 4) Thần Mộc (2222) và Thần Thổ (2223): 6 khung bên nguồn KHÔNG phải hiệu ứng mà là
--    ảnh chụp (một cái thẻ VISA, một ảnh người, mấy ảnh nhỏ) — hỏng sẵn từ nguồn, cả
--    SUMO lẫn Bun đều vậy. Dựng lại bằng chính hiệu ứng của Thần Hỏa đổi màu:
--    xanh lá cho Mộc (20245–20250), vàng đất cho Thổ (20251–20256).
--
-- 5) Cây tre trăm đốt (2225): bên nguồn dùng đúng dải icon 12835–12842 mà bên mình
--    đang dùng cho "Bụi tre", nhưng ảnh ở dải đó bên họ đã bị thay thành mũ có cánh
--    và găng trắng. Trỏ về đúng bộ ảnh bụi tre của mình.
--
-- Chạy cùng jar có DataGame.vsData = 30 và vsItem = 25. Khởi động lại server.
-- Chạy lại nhiều lần vẫn an toàn.
-- =====================================================================

SET NAMES utf8mb4;

-- (1) Pét cá mập: bỏ hình ở đầu và thân
UPDATE `part` SET `DATA` = '[[2955,0,0],[2955,0,0],[2955,0,0]]'  WHERE `id` = 2481 AND `TYPE` = 0;
UPDATE `part` SET `DATA` = '[[2955,0,0],[2955,0,0],[2955,0,0],[2955,0,0],[2955,0,0],[2955,0,0],[2955,0,0],[2955,0,0],[2955,0,0],[2955,0,0],[2955,0,0],[2955,0,0],[2955,0,0],[2955,0,0],[2955,0,0],[2955,0,0],[2955,0,0]]' WHERE `id` = 2482 AND `TYPE` = 1;

-- (2) Pet mèo trắng + Pet mèo trắng 1: bỏ đầu chồng hình, thay 2 khung lạ trong chân
UPDATE `part` SET `DATA` = '[[2955,0,0],[2955,0,0],[2955,0,0]]' WHERE `id` IN (2652, 2655) AND `TYPE` = 0;
UPDATE `part` SET `DATA` = '[[11043,10,1],[11046,-2,-5],[11044,-3,-13],[11046,-2,-5],[11042,-3,-13],[11043,-4,-13],[11044,-3,-13],[11046,-2,-5],[11042,-7,-7],[2955,0,0],[2955,0,0],[2955,0,0],[2955,0,0],[2955,0,0]]' WHERE `id` IN (2654, 2657) AND `TYPE` = 2;
UPDATE `item_template` SET `icon_id` = 11042 WHERE `id` IN (2218, 2219);

-- (3) Thần Hỏa: icon vật phẩm
UPDATE `item_template` SET `icon_id` = 19930 WHERE `id` = 2221;

-- (4) Thần Mộc / Thần Thổ: bộ khung mới (đổi màu từ Thần Hỏa)
UPDATE `flag_bag` SET `icon_data` = '20245,20246,20247,20248,20249,20250', `icon_id` = 20245 WHERE `id` = 159;
UPDATE `flag_bag` SET `icon_data` = '20251,20252,20253,20254,20255,20256', `icon_id` = 20251 WHERE `id` = 160;
UPDATE `item_template` SET `icon_id` = 20245 WHERE `id` = 2222;
UPDATE `item_template` SET `icon_id` = 20251 WHERE `id` = 2223;

-- (5) Cây tre trăm đốt: dùng lại bộ ảnh bụi tre của mình
UPDATE `flag_bag` SET `icon_data` = '12835,12836,12837,12838,12839,12840,12841,12842', `icon_id` = 12843 WHERE `id` = 162;
UPDATE `item_template` SET `icon_id` = 12843 WHERE `id` = 2225;

-- KIỂM TRA SAU
SELECT `id`, `TYPE`, LEFT(`DATA`, 70) AS `data` FROM `part` WHERE `id` IN (2481, 2482, 2652, 2654, 2655, 2657) ORDER BY `id`;
SELECT `id`, `NAME`, `icon_data`, `icon_id` FROM `flag_bag` WHERE `id` IN (158, 159, 160, 162);
SELECT `id`, `NAME`, `icon_id`, `part` FROM `item_template` WHERE `id` IN (2161, 2218, 2219, 2221, 2222, 2223, 2225);
