-- =====================================================================
-- 16 — NV 21: BỎ PHÓ BẢN DOANH TRẠI, THAY BẰNG 3 BƯỚC CÀY QUÁI (2026-09-20)
-- =====================================================================
-- Phó bản Doanh trại Độc Nhãn cần 5 người nên người chơi solo bị kẹt.
-- NV 21 nay 6 bước, làm một mình được hết:
--   0 Gặp Lính canh ở Rừng Bamboo
--   1 Hạ 500 Quỷ già        (Trại quỷ già 66, Vực chết 67)
--   2 Hạ 600 Lính tai dài   (Đồi cây Fide 74, Khe núi tử thần 75, Núi đá 76)
--   3 Hạ 700 Lính đầu trọc  (Đồi cây Fide 74, Khe núi tử thần 75)
--   4 Lấy bản đồ hành quân  (Lính canh ở Rừng Bamboo)
--   5 Về gặp sư phụ
-- Ai có bang vẫn đi phó bản được: phá xong doanh trại là xong TRỌN bước cày
-- quái đang dở (xử lý trong TaskService.checkDoneTaskDungeon).
--
-- `ducvupro` là khoá chính nên không chèn được số xen giữa 76..79 -> xoá 4 dòng
-- cũ, ghi lại cả 6 dòng ở dải 301..306 (thứ tự bước tính theo ducvupro tăng dần
-- trong cùng nhiệm vụ nên đổi dải số không ảnh hưởng gì).
--
-- LƯU Ý người chơi đang dở NV 21: tiến độ lưu theo SỐ BƯỚC, nên ai đang ở bước
-- 2 (lấy bản đồ) hoặc 3 (về gặp sư phụ) sẽ lùi thành bước cày quái tương ứng.
--
-- PHẢI chạy cùng lúc với bản jar mới. Chạy lại nhiều lần vẫn an toàn.
-- Cần khởi động lại server.
-- =====================================================================

DELETE FROM `task_sub_template`
 WHERE `task_main_id` = 21 AND `ducvupro` IN (76, 77, 78, 79, 301, 302, 303, 304, 305, 306);

INSERT INTO `task_sub_template` (`task_main_id`, `NAME`, `max_count`, `notify`, `npc_id`, `map`, `ducvupro`) VALUES
(21, 'Gặp Lính canh ở Rừng Bamboo', 1, 'Lính canh đứng ở Rừng Bamboo', 25, 27, 301),
(21, 'Hạ 500 Quỷ già', 500, 'Quỷ già ở Trại quỷ già và Vực chết', -1, 66, 302),
(21, 'Hạ 600 Lính tai dài', 600, 'Lính tai dài ở Đồi cây Fide, Khe núi tử thần, Núi đá', -1, 74, 303),
(21, 'Hạ 700 Lính đầu trọc', 700, 'Lính đầu trọc ở Đồi cây Fide và Khe núi tử thần', -1, 74, 304),
(21, 'Lấy bản đồ hành quân', 1, 'Hỏi Lính canh ở Rừng Bamboo', 25, 27, 305),
(21, 'Về gặp %10', 1, 'Mang bản đồ hành quân về cho %10', -5, -9, 306);

UPDATE `task_main_template` SET `NAME` = 'Quét sạch tàn quân Fide', `detail` = 'Bản đồ hành quân của Fide nằm trong tay đám tàn quân.
Dọn 500 Quỷ già, 600 Lính tai dài, 700 Lính đầu trọc rồi hỏi Lính canh lấy bản đồ.
Thưởng: 9 triệu SM, 9 triệu TN, 20 Đá nâng cấp 3, 2 Bản đồ kho báu'
 WHERE `id` = 21;

-- Thưởng cho 2 bước mới (bằng các bước cũ: 900.000 SM / 900.000 TN)
INSERT INTO `task_main_reward` (`task_id`, `sub_index`, `gender`, `sm`, `tn`, `gold`, `gem`, `ruby`, `items`, `text`) VALUES
(21, 4, -1, 900000, 900000, 0, 0, 0, '[]', 'Thưởng 900.000 sức mạnh. Thưởng 900.000 tiềm năng'),
(21, 5, -1, 900000, 900000, 0, 0, 0, '[]', 'Thưởng 900.000 sức mạnh. Thưởng 900.000 tiềm năng')
ON DUPLICATE KEY UPDATE `sm` = VALUES(`sm`), `tn` = VALUES(`tn`), `items` = VALUES(`items`), `text` = VALUES(`text`);

-- KIỂM TRA: phải ra đúng 6 bước theo thứ tự
SELECT `ducvupro`, `NAME`, `max_count`, `npc_id`, `map`
  FROM `task_sub_template` WHERE `task_main_id` = 21 ORDER BY `ducvupro`;
SELECT `sub_index`, `sm`, `tn` FROM `task_main_reward` WHERE `task_id` = 21 ORDER BY `sub_index`;
