-- =====================================================================
-- 26 — NV 37: BỎ BƯỚC CUỐI "MANG LÕI PHÉP CHO KIBIT" (2026-09-20)
-- =====================================================================
-- Chủ dự án chốt: bỏ bước này. NV 37 còn 4 bước và hoàn thành ngay khi nhặt
-- được Lõi Phép Babiđây:
--   0 Gặp Ôsin ở Đại hội võ thuật
--   1 Hạ Mabư hoặc 30 Cadic M
--   2 Hạ Drabura 3 / 30 Quỷ chim
--   3 Nhặt Lõi Phép Babiđây   <-- bước cuối, xong là hết nhiệm vụ
--
-- Bước bị bỏ là bước CUỐI nên các bước khác giữ nguyên số thứ tự.
-- Kibit vẫn dùng cho NV 40 bước "Nghe Kibit kể tiếp".
-- Chạy cùng bản jar mới. Chạy lại nhiều lần vẫn an toàn. Khởi động lại server.
-- =====================================================================

DELETE FROM `task_sub_template` WHERE `task_main_id` = 37 AND `ducvupro` = 161;
DELETE FROM `task_main_reward`  WHERE `task_id` = 37 AND `sub_index` = 4;

UPDATE `task_main_template` SET `detail` = 'Tầng cuối phi thuyền không phải kho, nó là cái bụng.
Hạ Mabư rồi nhặt Lõi Phép Babiđây.
Thưởng: 450 triệu SM, 450 triệu TN, 5 Đá bảo vệ, 5 Đá ngũ sắc'
 WHERE `id` = 37;

SELECT `ducvupro`, `NAME`, `max_count`, `npc_id`, `map`
  FROM `task_sub_template` WHERE `task_main_id` = 37 ORDER BY `ducvupro`;
