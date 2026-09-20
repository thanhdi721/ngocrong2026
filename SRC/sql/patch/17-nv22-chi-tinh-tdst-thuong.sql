-- =====================================================================
-- 17 — NV 22: CHỈ TÍNH TIỂU ĐỘI SÁT THỦ BẢN THƯỜNG (2026-09-20)
-- =====================================================================
-- Chủ dự án chốt: bước "Hạ 5 boss Tiểu đội sát thủ" chỉ tính tiểu đội sát thủ
-- bản thường (Núi khỉ đỏ, Hang quỷ chim, Núi khỉ đen, Hang khỉ đen).
-- Tiểu đội Namếc không tính cho nhiệm vụ và đã được trả về các map Namếc
-- như bản gốc (BossesData: 7, 8, 9, 10, 11, 12, 13, 25, 33, 34, 43).
-- Chỉ sửa chữ nhắc việc; phần tính toán nằm trong jar.
-- Chạy lại nhiều lần vẫn an toàn. Cần khởi động lại server.
-- =====================================================================

UPDATE `task_sub_template`
   SET `notify` = 'Tiểu đội sát thủ ở Núi khỉ đỏ, Hang quỷ chim, Núi khỉ đen, Hang khỉ đen (bản Namếc không tính)'
 WHERE `task_main_id` = 22 AND `ducvupro` = 82;

SELECT `task_main_id`, `ducvupro`, `NAME`, `notify` FROM `task_sub_template`
 WHERE `task_main_id` = 22 ORDER BY `ducvupro`;
