-- =====================================================================
-- 19 — NV 49: BỎ BƯỚC "DÙNG BÌNH CHỨA COMMESON" (2026-09-20)
-- =====================================================================
-- Bình chứa Commeson (638) là vật phẩm dùng được bất cứ lúc nào, người chơi lỡ
-- dùng sớm là kẹt bước. Chủ dự án chốt: bỏ hẳn bước này.
--
-- NV 49 còn 6 bước:
--   0 Nghe Potage kể sự thật
--   1 Chọn số phận bản sao
--   2 Tới Võ đài Xên bọ hung
--   3 Rủ 1 người vào võ đài
--   4 Đánh gục bản sao
--   5 Đạt 2 tỷ sức mạnh   (trước là bước 6)
--
-- Bình vẫn được tặng khi chọn nhánh "thu nhận bản sao", nhưng chỉ là quà, dùng
-- lúc nào cũng được.
--
-- Người đang ở bước "Dùng Bình chứa Commeson" sẽ chuyển thành bước "Đạt 2 tỷ
-- sức mạnh"; ai đã ở bước cuối thì giữ nguyên bước cuối.
--
-- PHẢI chạy cùng lúc với bản jar mới (code NV 49 đã đổi số bước).
-- Chạy lại nhiều lần vẫn an toàn. Cần khởi động lại server.
-- =====================================================================

DELETE FROM `task_sub_template` WHERE `task_main_id` = 49 AND `ducvupro` = 231;
DELETE FROM `task_main_reward`  WHERE `task_id` = 49 AND `sub_index` = 6;

UPDATE `task_main_template` SET `detail` = 'Ngươi chọn không giết.
Đánh gục bản sao của chính mình trên Võ đài Xên bọ hung rồi luyện tới 2 tỷ sức mạnh.
Thưởng: 230 triệu SM, 230 triệu TN, 3 Sao pha lê lục, 5 Đá ngũ sắc, 10 Đá bảo vệ, 1 Mảnh Ký Ức 4'
 WHERE `id` = 49;

-- KIỂM TRA: phải còn đúng 6 bước, không còn bước dùng bình
SELECT `ducvupro`, `NAME`, `max_count`, `npc_id`, `map`
  FROM `task_sub_template` WHERE `task_main_id` = 49 ORDER BY `ducvupro`;
SELECT `sub_index`, `sm`, `tn` FROM `task_main_reward` WHERE `task_id` = 49 ORDER BY `sub_index`;
