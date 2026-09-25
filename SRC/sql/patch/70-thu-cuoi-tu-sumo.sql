-- =====================================================================
-- 70 — HAI THÚ CƯỠI MỚI TỪ SOURCE SUMO (2026-09-25)
-- =====================================================================
-- Ảnh đã chép sẵn:
--   data/img_by_name/x1..x4/mount_53_0|1.png, mount_54_0|1.png  (8 khung, khung x2 512x440)
--   data/icon/x1..x4/20243.png, 20244.png                        (icon trong hành trang)
-- Bản x1 bên SUMO không có nên được thu nhỏ một nửa từ x2.
--
-- mount_53 = kỳ lân trắng vàng có cánh, mount_54 = phượng hoàng bảy sắc.
-- Bên SUMO cả hai đều tên "Thú Cưỡi cực vip" (trùng tên món mình đã có) nên đặt lại tên.
--
-- Manager.loadDatabase chỉ nhận thú cưỡi khi img_by_name có dòng "mount_<part>_0",
-- nên phải thêm đủ 4 dòng dưới đây.
--
-- Chạy cùng patch 67, 69 và jar có DataGame.vsItem = 24. Khởi động lại server.
-- TRƯỚC KHI CHẠY: câu dưới phải ra 2259; khác thì DỪNG, báo lại.
-- =====================================================================
SELECT MAX(`id`) AS item_max FROM `item_template`;

DELETE FROM `item_template` WHERE `id` BETWEEN 2260 AND 2261;

INSERT INTO `img_by_name` (`NAME`, `n_frame`) VALUES
('mount_53_0', 8), ('mount_53_1', 8), ('mount_54_0', 8), ('mount_54_1', 8)
ON DUPLICATE KEY UPDATE `n_frame` = VALUES(`n_frame`);

INSERT INTO `item_template` (`id`, `TYPE`, `gender`, `NAME`, `description`, `level`, `icon_id`,
                             `part`, `is_up_to_up`, `power_require`, `gold`, `gem`, `head`, `body`, `leg`) VALUES
(2260, 23, 3, 'Kỳ Lân Bạch Kim', 'Thú cưỡi', 0, 20243, 53, 0, 0, 0, 0, -1, -1, -1),
(2261, 23, 3, 'Phượng Hoàng Thất Sắc', 'Thú cưỡi', 0, 20244, 54, 0, 0, 0, 0, -1, -1, -1);

SELECT `id`, `NAME`, `icon_id`, `part` FROM `item_template` WHERE `id` BETWEEN 2260 AND 2261;
SELECT `NAME`, `n_frame` FROM `img_by_name` WHERE `NAME` LIKE 'mount_5%';
