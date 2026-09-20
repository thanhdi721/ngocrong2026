-- =====================================================================
-- 12 — BỎ CÁC BƯỚC PHẢI VÀO THÀNH PHỐ SANTA / PHI THUYỀN THEO GIỜ (2026-09-19)
-- =====================================================================
-- Thành phố Santa (126) chỉ vào được qua Tapion trong khung giờ, phi thuyền
-- Mabư cũng theo giờ -> người chơi kẹt nhiệm vụ tới khi đến giờ.
--
--  NV 18: XOÁ bước "Tới Thành phố Santa". "Nghe Tapion kể chuyện" làm ở
--         Thành phố Vegeta (19). NV 18 còn 4 bước.
--         Người đang đứng ở bước cũ 3 hoặc 4: lúc đăng nhập server tự kéo về
--         bước "Nghe Tapion kể chuyện", không mất tiến độ.
--  NV 22: hai bước gặp Tapion chuyển về Thành phố Vegeta (19).
--  NV 36 / 37: Ôsin ở Đại hội võ thuật đưa thẳng tới Sa mạc hoang vu ngoài
--         khung giờ Mabư (trước đây phải có Bình hút năng lượng, mà patch 08
--         đã bỏ bình khỏi thưởng NV 36). NV 37 bước 1 thêm đường vòng
--         "30 Cadic M ở Sa mạc hoang vu" (max_count 1 -> 30; Mabư vẫn xong ngay).
--
-- Code đi kèm: TaskService (Tapion, bỏ trigger map 126, NV 37), QuestDrop, Osin.
-- Chạy SAU 06 / 07 / 08 / 11. Chạy lại nhiều lần vẫn an toàn.
-- Nếu sau này chạy lại 06 thì phải chạy lại 07, 08, 10, 11 rồi 12.
-- PHẢI chạy patch này CÙNG LÚC với bản jar mới (code NV 18 đã đổi số bước).
-- Cần khởi động lại server.
-- =====================================================================

-- ---- NV 18 ----------------------------------------------------------
DELETE FROM `task_sub_template` WHERE `task_main_id` = 18 AND `ducvupro` = 63;
DELETE FROM `task_main_reward`  WHERE `task_id` = 18 AND `sub_index` = 4;

UPDATE `task_sub_template`
   SET `notify` = 'Tapion đợi ngươi ở Thành phố Vegeta', `npc_id` = 53, `map` = 19
 WHERE `task_main_id` = 18 AND `ducvupro` = 64;

UPDATE `task_main_template` SET `detail` = 'Ở Thành phố Vegeta có một người mà không ai nhớ nổi.
Tìm Tapion, giải vây thành rồi nghe anh kể chuyện.
Thưởng: 6 triệu SM, 6 triệu TN, 20 Đá nâng cấp 2'
 WHERE `id` = 18;

-- ---- NV 22 ----------------------------------------------------------
UPDATE `task_sub_template`
   SET `notify` = 'Tapion ở Thành phố Vegeta', `map` = 19
 WHERE `task_main_id` = 22 AND `ducvupro` = 80;

UPDATE `task_sub_template`
   SET `notify` = 'Mang máy đo về Thành phố Vegeta cho Tapion', `map` = 19
 WHERE `task_main_id` = 22 AND `ducvupro` = 84;

-- ---- NV 36 ----------------------------------------------------------
UPDATE `task_sub_template`
   SET `notify` = 'Cổng mở 12h-12h59. Sai giờ thì nhờ Ôsin đưa qua Sa mạc hoang vu'
 WHERE `task_main_id` = 36 AND `ducvupro` = 153;

-- ---- NV 37 ----------------------------------------------------------
UPDATE `task_sub_template`
   SET `NAME` = 'Hạ Mabư hoặc 30 Cadic M', `max_count` = 30,
       `notify` = 'Boss Mabư trong phi thuyền, hoặc 30 Cadic M ở Sa mạc hoang vu (nhờ Ôsin)',
       `map` = 165
 WHERE `task_main_id` = 37 AND `ducvupro` = 158;

UPDATE `task_sub_template`
   SET `notify` = 'Hạ boss Drabura 3, hoặc 30 Quỷ chim ở Hang quỷ chim, Núi khỉ đen', `map` = 81
 WHERE `task_main_id` = 37 AND `ducvupro` = 159;

UPDATE `task_sub_template`
   SET `notify` = 'Hạ thêm 1 Quỷ chim hoặc Cadic M ở Sa mạc, Lõi Phép sẽ rơi', `map` = 81
 WHERE `task_main_id` = 37 AND `ducvupro` = 160;

-- KIỂM TRA: NV 18 phải còn 4 dòng; không còn dòng nào map = 126
SELECT `task_main_id`, `ducvupro`, `NAME`, `max_count`, `npc_id`, `map`
  FROM `task_sub_template`
 WHERE `task_main_id` IN (18, 22, 37) ORDER BY `task_main_id`, `ducvupro`;
SELECT COUNT(*) AS `con_map_126` FROM `task_sub_template` WHERE `map` = 126;
