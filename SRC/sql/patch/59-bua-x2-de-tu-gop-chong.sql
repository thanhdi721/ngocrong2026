-- =====================================================================
-- 59 — BÙA X2 ĐỆ TỬ (1628) CHO GỘP CHỒNG (2026-09-23)
-- =====================================================================
-- Vật phẩm 1628 để is_up_to_up = 0 nên mỗi cái chiếm một ô riêng, quay vòng quay
-- ra nhiều cái là đầy rương phụ. Các bùa cùng kiểu (Cuồng nộ 2 = 1150, Bổ khí 2 = 1151,
-- Bổ huyết 2 = 1152, Giáp Xên bọ hung 2 = 1153) đều là 1 và dùng chung luồng useItemTime,
-- nên đổi 1628 thành 1 là an toàn.
--
-- Sau khi chạy: đồ mới nhận sẽ tự gộp. Mấy cái ĐANG nằm rời trong hành trang / rương phụ
-- chỉ gộp khi có cái mới cộng vào, đây là cách cộng dồn sẵn có của server.
--
-- Chạy cùng jar có DataGame.vsItem = 23. Chạy lại vẫn an toàn. Khởi động lại server.
-- =====================================================================

UPDATE `item_template` SET `is_up_to_up` = 1 WHERE `id` = 1628;

SELECT `id`, `NAME`, `TYPE`, `is_up_to_up` FROM `item_template` WHERE `id` IN (1150, 1151, 1152, 1153, 1628);
