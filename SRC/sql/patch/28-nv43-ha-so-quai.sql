-- =====================================================================
-- 28 — NV 43: HẠ SỐ QUÁI CỦA LỐI THAY THẾ (2026-09-20)
-- =====================================================================
-- Ba bước giữa của NV 43 đều có 2 lối: hạ boss trong phó bản "Khí gas hủy diệt"
-- (cần bang hội) HOẶC cày quái ngoài. Hạ boss vẫn cộng TRỌN bước trong một lần
-- (xử lý trong jar, không đổi). Chủ dự án chốt: số quái của lối thay thế quá nặng.
--
--   Bước 2: 50 -> 15 quái   (hoặc hạ boss Dr Lychee)
--   Bước 3: 70 -> 20 quái   (hoặc hạ boss Hatchiyack)
--   Bước 4: 100 -> 25 quái  (hoặc hoàn thành phó bản Khí gas)
--
-- Quái tính ở: Hành tinh ngục tù (155), Khu hang động (160), Bìa rừng (161).
-- Chạy lại nhiều lần vẫn an toàn. Khởi động lại server (không cần jar mới).
-- =====================================================================

UPDATE `task_sub_template`
   SET `NAME` = 'Hạ boss Lychee hoặc 15 quái', `max_count` = 15,
       `notify` = 'Hạ boss Dr Lychee là xong ngay; không có bang thì hạ 15 quái ở Hành tinh ngục tù / Khu hang động / Bìa rừng'
 WHERE `task_main_id` = 43 AND `ducvupro` = 193;

UPDATE `task_sub_template`
   SET `NAME` = 'Hạ Hatchiyack hoặc 20 quái', `max_count` = 20,
       `notify` = 'Hạ boss Hatchiyack là xong ngay; không có bang thì hạ 20 quái ở Hành tinh ngục tù / Khu hang động / Bìa rừng'
 WHERE `task_main_id` = 43 AND `ducvupro` = 194;

UPDATE `task_sub_template`
   SET `NAME` = 'Xong khí gas hoặc 25 quái', `max_count` = 25,
       `notify` = 'Hoàn thành phó bản Khí gas là xong ngay; không có bang thì hạ 25 quái ở Hành tinh ngục tù / Khu hang động / Bìa rừng'
 WHERE `task_main_id` = 43 AND `ducvupro` = 195;

SELECT `ducvupro`, `NAME`, `max_count`, `notify`
  FROM `task_sub_template` WHERE `task_main_id` = 43 ORDER BY `ducvupro`;
