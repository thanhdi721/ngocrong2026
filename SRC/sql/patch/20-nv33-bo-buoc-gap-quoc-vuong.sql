-- =====================================================================
-- 20 — NV 33: BỎ BƯỚC "GẶP QUỐC VƯƠNG" (2026-09-20)
-- =====================================================================
-- Chủ dự án chốt: bỏ bước chào hỏi, vào thẳng phần việc.
--
-- NV 33 còn 5 bước:
--   0 Tới Vách núi (theo hành tinh)
--   1 Nâng HP gốc lên 220.000      (trước là bước 2)
--   2 Mở giới hạn sức mạnh          (trước là bước 3, nhờ Quốc Vương, miễn phí)
--   3 Đạt 3 tỷ sức mạnh             (trước là bước 4)
--   4 Báo cáo với Quốc Vương        (trước là bước 5)
--
-- Vẫn cần bấm Quốc Vương ở bước 2 và bước 4 — bản jar mới đã sửa lỗi bấm NPC
-- không phản hồi (xem ghi chú trong patch / docs 47).
--
-- Người đang ở bước "Gặp Quốc Vương" sẽ thành bước "Nâng HP gốc lên 220.000";
-- ai ở bước sau đó thì lùi một bước tương ứng (có thể phải làm lại 1 bước).
--
-- PHẢI chạy cùng lúc với bản jar mới (code NV 33 đã đổi số bước).
-- Chạy lại nhiều lần vẫn an toàn. Cần khởi động lại server.
-- =====================================================================

DELETE FROM `task_sub_template` WHERE `task_main_id` = 33 AND `ducvupro` = 136;
DELETE FROM `task_main_reward`  WHERE `task_id` = 33 AND `sub_index` = 5;

UPDATE `task_sub_template`
   SET `notify` = 'Quốc Vương đứng ở %5, bấm vào ông để mở giới hạn (miễn phí)'
 WHERE `task_main_id` = 33 AND `ducvupro` = 138;

UPDATE `task_main_template` SET `detail` = 'HP gốc của ngươi đã chạm trần, cơ thể không lớn thêm được nữa.
Nâng HP gốc lên 220.000, nhờ Quốc Vương ở vách núi mở giới hạn rồi đạt 3 tỷ sức mạnh.
Thưởng: 250 triệu SM, 250 triệu TN, 50 Đậu thần cấp 8, 5 Đá bảo vệ'
 WHERE `id` = 33;

-- KIỂM TRA: phải còn đúng 5 bước
SELECT `ducvupro`, `NAME`, `notify`, `npc_id`, `map`
  FROM `task_sub_template` WHERE `task_main_id` = 33 ORDER BY `ducvupro`;
SELECT `sub_index`, `sm`, `tn` FROM `task_main_reward` WHERE `task_id` = 33 ORDER BY `sub_index`;
