-- =====================================================================
-- 22 — NV 33: CHUYỂN HAI BƯỚC TỪ QUỐC VƯƠNG VỀ SƯ PHỤ (2026-09-20)
-- =====================================================================
-- Quốc Vương bấm không mở được menu nên chủ dự án chốt: chuyển sang sư phụ
-- (Quy Lão Kame / Trưởng Lão Guru / Vua Vegeta — npc -5, map -9).
--
-- NV 33 (5 bước):
--   0 Tới Vách núi
--   1 Hạ 100 quái ở %3
--   2 Nhờ sư phụ mở giới hạn   <-- ĐỔI NPC (nút "Phá giới hạn" hiện trong menu sư phụ)
--   3 Đạt 3 tỷ sức mạnh
--   4 Báo cáo với sư phụ        <-- ĐỔI NPC
--
-- Quốc Vương vẫn còn ở vách núi và vẫn mở giới hạn bằng vàng như cũ.
-- PHẢI chạy cùng bản jar mới. Chạy lại nhiều lần vẫn an toàn. Khởi động lại server.
-- =====================================================================

UPDATE `task_sub_template`
   SET `NAME` = 'Nhờ sư phụ mở giới hạn',
       `notify` = 'Gặp sư phụ, chọn "Phá giới hạn" — lần này miễn phí',
       `npc_id` = -5, `map` = -9
 WHERE `task_main_id` = 33 AND `ducvupro` = 138;

UPDATE `task_sub_template`
   SET `NAME` = 'Báo cáo với sư phụ',
       `notify` = 'Về chỗ sư phụ báo cáo',
       `npc_id` = -5, `map` = -9
 WHERE `task_main_id` = 33 AND `ducvupro` = 140;

UPDATE `task_main_template` SET `detail` = 'HP gốc của ngươi đã chạm trần, cơ thể không lớn thêm được nữa.
Hạ 100 quái ở %3, nhờ sư phụ mở giới hạn rồi đạt 3 tỷ sức mạnh.
Thưởng: 250 triệu SM, 250 triệu TN, 50 Đậu thần cấp 8, 5 Đá bảo vệ'
 WHERE `id` = 33;

SELECT `ducvupro`, `NAME`, `notify`, `npc_id`, `map`
  FROM `task_sub_template` WHERE `task_main_id` = 33 ORDER BY `ducvupro`;
