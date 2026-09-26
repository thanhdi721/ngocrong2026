-- =====================================================================
-- 87-giftcode-thu-luyen-dan.sql — GIFTCODE ĐỂ THỬ TÍNH NĂNG LUYỆN ĐAN
-- Database: team2026 (MariaDB 10.4)        Sinh ngày: 2026-09-27
--
-- Phải chạy SAU: patch 84 (vật phẩm 2276–2289) và patch 85.
--
-- ---------------------------------------------------------------------
-- DANH SÁCH MÃ
-- ---------------------------------------------------------------------
--  Mã 7 ký tự, KHÔNG giới hạn số lượt, nhưng mỗi tài khoản chỉ dùng được MỘT LẦN mỗi mã.
--  Vì vậy mấy nhóm hay dùng lại đều có ba bản 001 / 002 / 003 để thử được ba lượt.
--
--  NGUYÊN LIỆU — ldnl001 / ldnl002 / ldnl003   (5 ô hành trang trống)
--      60 Thanh Vân Thảo · 60 Ngọc Diệp Thảo · 60 Hàn Tinh Quả · 60 Kim Nhung Quả
--      30 Địa Hỏa Tinh
--      -> đủ luyện ~12 mẻ đủ mọi công thức, kể cả Kim Cương Đan (cần 5 mỗi loại + 3 Địa Hỏa)
--
--  ĐAN PHƯƠNG — ldpt001 / ldpt002 / ldpt003    (3 ô trống)
--      10 Sơ Cấp · 10 Trung Cấp · 10 Cao Cấp
--      -> mỗi quyển 3 mẻ, tức 30 mẻ mỗi bậc
--
--  TIỀN — ldtien1 / ldtien2                    (1 ô trống)
--      500 triệu vàng  (Linh Điền 500.000/ô × 6 ô = 3 triệu một lượt gieo)
--      1.000 Linh Thạch (mua đan, ngọc bội, Tụ Linh Phù ở tiệm Tu Tiên)
--
--  THÀNH PHẨM — ldtp001 / ldtp002              (6 ô trống)
--      5 mỗi loại đan thượng phẩm + 5 Đan Phế
--      -> thử ngay hiệu lực 20 phút và mức cộng, không phải luyện
--
-- ---------------------------------------------------------------------
-- LƯU Ý KHI DÙNG
-- ---------------------------------------------------------------------
--  * GiftCodeManager đòi số Ô HÀNH TRANG TRỐNG >= số món trong mã, nếu không thì báo
--    "Cần tối thiểu N ô hành trang trống" và KHÔNG trừ lượt. Dọn túi trước khi nhập.
--  * Không mã nào gắn dòng "không thể giao dịch", đúng như đồ rơi thật.
--
-- ---------------------------------------------------------------------
-- TRÌNH TỰ CHẠY
-- ---------------------------------------------------------------------
--   1) CHỌN ĐÚNG DATABASE `team2026`. Dòng lệnh:
--        mysql -u root -p team2026 < 87-giftcode-thu-luyen-dan.sql
--   2) Chạy cả file. Chạy lại nhiều lần vẫn an toàn (xoá rồi thêm lại).
--   3) KHÔNG cần tắt server: giftcode đọc thẳng từ CSDL mỗi lần nhập.
--   4) Xem khối (3): cột `so_ma` phải = 10.
-- =====================================================================

-- ---------------------------------------------------------------------
-- (1) KIỂM TRA TRƯỚC.
-- ---------------------------------------------------------------------
SELECT DATABASE() AS `database_dang_chon_phai_la_team2026`;
-- Phải ra đủ 14 dòng; thiếu nghĩa là chưa chạy patch 84.
SELECT COUNT(*) AS `so_vat_pham_2276_2289_phai_la_14`
  FROM `item_template` WHERE `id` BETWEEN 2276 AND 2289;

-- ---------------------------------------------------------------------
-- (2) THÊM MÃ.
-- ---------------------------------------------------------------------
DELETE FROM `giftcode` WHERE `code` IN
    ('ldnl001','ldnl002','ldnl003','ldpt001','ldpt002','ldpt003','ldtien1','ldtien2','ldtp001','ldtp002');

INSERT INTO `giftcode` (`id`, `code`, `count_left`, `detail`, `datecreate`, `expired`) VALUES
-- nguyên liệu: 4 linh thảo + Địa Hỏa Tinh
(170, 'ldnl001', -1, '[{"id":2276,"quantity":60,"options":[]},{"id":2277,"quantity":60,"options":[]},{"id":2278,"quantity":60,"options":[]},{"id":2279,"quantity":60,"options":[]},{"id":2280,"quantity":30,"options":[]}]', NOW(), '2037-12-31 17:00:00'),
(171, 'ldnl002', -1, '[{"id":2276,"quantity":60,"options":[]},{"id":2277,"quantity":60,"options":[]},{"id":2278,"quantity":60,"options":[]},{"id":2279,"quantity":60,"options":[]},{"id":2280,"quantity":30,"options":[]}]', NOW(), '2037-12-31 17:00:00'),
(172, 'ldnl003', -1, '[{"id":2276,"quantity":60,"options":[]},{"id":2277,"quantity":60,"options":[]},{"id":2278,"quantity":60,"options":[]},{"id":2279,"quantity":60,"options":[]},{"id":2280,"quantity":30,"options":[]}]', NOW(), '2037-12-31 17:00:00'),
-- đan phương ba bậc
(173, 'ldpt001', -1, '[{"id":2281,"quantity":10,"options":[]},{"id":2282,"quantity":10,"options":[]},{"id":2283,"quantity":10,"options":[]}]', NOW(), '2037-12-31 17:00:00'),
(174, 'ldpt002', -1, '[{"id":2281,"quantity":10,"options":[]},{"id":2282,"quantity":10,"options":[]},{"id":2283,"quantity":10,"options":[]}]', NOW(), '2037-12-31 17:00:00'),
(175, 'ldpt003', -1, '[{"id":2281,"quantity":10,"options":[]},{"id":2282,"quantity":10,"options":[]},{"id":2283,"quantity":10,"options":[]}]', NOW(), '2037-12-31 17:00:00'),
-- vàng (id -1 = vàng, xem GiftCodeService) + Linh Thạch
(176, 'ldtien1', -1, '[{"id":-1,"quantity":500000000,"options":[]},{"id":2266,"quantity":1000,"options":[]}]', NOW(), '2037-12-31 17:00:00'),
(177, 'ldtien2', -1, '[{"id":-1,"quantity":500000000,"options":[]},{"id":2266,"quantity":1000,"options":[]}]', NOW(), '2037-12-31 17:00:00'),
-- thành phẩm: 5 đan thượng phẩm + Đan Phế
(178, 'ldtp001', -1, '[{"id":2285,"quantity":5,"options":[]},{"id":2286,"quantity":5,"options":[]},{"id":2287,"quantity":5,"options":[]},{"id":2288,"quantity":5,"options":[]},{"id":2289,"quantity":5,"options":[]},{"id":2284,"quantity":5,"options":[]}]', NOW(), '2037-12-31 17:00:00'),
(179, 'ldtp002', -1, '[{"id":2285,"quantity":5,"options":[]},{"id":2286,"quantity":5,"options":[]},{"id":2287,"quantity":5,"options":[]},{"id":2288,"quantity":5,"options":[]},{"id":2289,"quantity":5,"options":[]},{"id":2284,"quantity":5,"options":[]}]', NOW(), '2037-12-31 17:00:00');

-- ---------------------------------------------------------------------
-- (3) KIỂM TRA SAU — `so_ma` phải = 10.
-- ---------------------------------------------------------------------
SELECT COUNT(*) AS `so_ma`
  FROM `giftcode` WHERE `code` LIKE 'ld%';

-- Mọi id vật phẩm trong 10 mã đều PHẢI có thật trong item_template (trừ -1 là vàng).
SELECT 'moi id vat pham trong ma deu co that' AS `muc`,
       ((SELECT COUNT(*) FROM `item_template`
          WHERE `id` IN (2266,2276,2277,2278,2279,2280,2281,2282,2283,2284,2285,2286,2287,2288,2289)) = 15) AS `dat`;

SELECT `code`, `detail` FROM `giftcode` WHERE `code` LIKE 'ld%' ORDER BY `id`;

-- ---------------------------------------------------------------------
-- (4) GỠ BỎ — chạy khi thử xong, để người chơi thật không nhặt được mã.
-- ---------------------------------------------------------------------
-- DELETE FROM `giftcode` WHERE `code` IN
--     ('ldnl001','ldnl002','ldnl003','ldpt001','ldpt002','ldpt003','ldtien1','ldtien2','ldtp001','ldtp002');
