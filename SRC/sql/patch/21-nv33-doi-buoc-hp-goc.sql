-- =====================================================================
-- 21 — NV 33: ĐỔI BƯỚC "NÂNG HP GỐC LÊN 220.000" (2026-09-20)
-- =====================================================================
-- Bước cũ có điều kiện là TRẠNG THÁI (HP gốc đã chạm trần) nên ai đạt sẵn từ
-- trước là kẹt. Chủ dự án chốt: đổi sang bước đánh quái cho chắc.
--
-- NV 33 (5 bước):
--   0 Tới Vách núi (theo hành tinh)
--   1 Hạ 100 quái ở %3   <-- MỚI (Đồi hoa cúc / Đồi nấm tím / Đồi hoang, quái nào cũng tính)
--   2 Mở giới hạn sức mạnh (bấm Quốc Vương, miễn phí)
--   3 Đạt 3 tỷ sức mạnh
--   4 Báo cáo với Quốc Vương
--
-- Người đang đứng ở bước 1 sẽ đếm lại từ 0/100.
-- PHẢI chạy cùng bản jar mới. Chạy lại nhiều lần vẫn an toàn. Khởi động lại server.
-- =====================================================================

UPDATE `task_sub_template`
   SET `NAME` = 'Hạ 100 quái ở %3',
       `max_count` = 100,
       `notify` = 'Lên %3 hạ 100 con, quái loại nào cũng tính',
       `npc_id` = -1,
       `map` = -3
 WHERE `task_main_id` = 33 AND `ducvupro` = 137;

UPDATE `task_main_template` SET `detail` = 'HP gốc của ngươi đã chạm trần, cơ thể không lớn thêm được nữa.
Hạ 100 quái ở %3, nhờ Quốc Vương ở vách núi mở giới hạn rồi đạt 3 tỷ sức mạnh.
Thưởng: 250 triệu SM, 250 triệu TN, 50 Đậu thần cấp 8, 5 Đá bảo vệ'
 WHERE `id` = 33;

-- KIỂM TRA
SELECT `ducvupro`, `NAME`, `max_count`, `notify`, `npc_id`, `map`
  FROM `task_sub_template` WHERE `task_main_id` = 33 ORDER BY `ducvupro`;
