-- =====================================================================
-- 15 — NV 20 / NV 48: HÌNH GRANOLA + GHI RÕ CÁCH KIẾM THẺ (2026-09-19)
-- =====================================================================
--  * NPC 76 Granola đổi sang hình "Cải trang Hit" (item 884: part 520/521/522,
--    avatar 4969) — sát thủ đánh thuê, hợp vai thợ săn tiền thưởng.
--    DataGame.vsData 10 -> 11 để client tải lại.
--  * Bước "Nhặt 3 Thẻ tiền thưởng" / "Nhặt 3 Biên bản truy nã" ghi rõ nguồn.
--    Code: thẻ / biên bản nay rơi cả ở bước hạ boss (trước chỉ rơi ở bước nhặt,
--    3 boss vừa hạ không cho gì); sang bước nhặt thì tự cộng số đã có trong
--    hành trang. Cui ở Thành phố Vegeta dẫn tới boss ở cả bước nhặt.
-- Chạy SAU 06 / 07 / 13. Chạy lại nhiều lần vẫn an toàn. Khởi động lại server.
-- =====================================================================

UPDATE `npc_template`
   SET `head` = 520, `body` = 521, `leg` = 522, `avatar` = 4969
 WHERE `id` = 76;

-- ---- NV 20 ----------------------------------------------------------
UPDATE `task_sub_template`
   SET `NAME` = 'Nhặt 3 Thẻ (rơi từ 3 boss)',
       `notify` = 'Mỗi lần hạ Kuku, Mập Đầu Đinh hoặc Rambo rơi 1 Thẻ tiền thưởng'
 WHERE `task_main_id` = 20 AND `ducvupro` = 74;

UPDATE `task_main_template` SET `detail` = 'Granola rủ ngươi săn ba tay chân của Fide, không cần giấy phép.
Chọn phe ở Thung lũng Nappa. Hạ Kuku, Mập Đầu Đinh, Rambo (Cui ở TP Vegeta dẫn đường), mỗi boss rơi 1 Thẻ tiền thưởng, mang về cho Granola.
Thưởng: 8 triệu SM, 8 triệu TN, 40 Đá nâng cấp 2'
 WHERE `id` = 20;

-- ---- NV 48 ----------------------------------------------------------
UPDATE `task_sub_template`
   SET `NAME` = 'Nhặt 3 Biên bản (từ 3 boss)',
       `notify` = 'Mỗi lần hạ Kuku, Mập Đầu Đinh hoặc Rambo rơi 1 Biên bản truy nã'
 WHERE `task_main_id` = 48 AND `ducvupro` = 224;

UPDATE `task_main_template` SET `detail` = 'Ngươi chọn con đường có giấy tờ.
Báo Jaco, hạ Kuku, Mập Đầu Đinh, Rambo (Cui ở TP Vegeta dẫn đường), mỗi boss rơi 1 Biên bản truy nã, nộp cho Jaco.
Thưởng: 8 triệu SM, 8 triệu TN, 20 Đá nâng cấp 1, 8 Đá bảo vệ'
 WHERE `id` = 48;

-- KIỂM TRA
SELECT `id`, `NAME`, `head`, `body`, `leg`, `avatar` FROM `npc_template` WHERE `id` = 76;
SELECT `task_main_id`, `ducvupro`, `NAME`, `notify` FROM `task_sub_template`
 WHERE (`task_main_id`, `ducvupro`) IN ((20,74),(48,224));
