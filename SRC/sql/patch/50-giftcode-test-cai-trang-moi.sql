-- =====================================================================
-- 50 — GIFTCODE THỬ 40 CẢI TRANG / PET MỚI (2026-09-23)
-- =====================================================================
-- 5 code, mỗi code 8 món (hành trang phải còn 8 ô trống, nếu thiếu server sẽ báo).
-- Mỗi tài khoản nhập được mỗi code một lần. Số lượt: không giới hạn.
-- Nhập ở NPC ADMIN Đẹp Trai -> "Nhập Giftcode".
--
--   testct1 : 2080 Cải trang Quy lão Kame, 2081 Cải trang Tiểu Ngọc Thần Tiên, 2082 Cải trang Ma Din Bư, 2083 Cậu Kilo, 2084 Cải trang Cậu Hít, 2085 Cải trang Lích Tên, 2086 Cải trang Super Black Goku, 2087 Cải Trang Siêu Saiyan Blue
--   testct2 : 2088 Cải trang Radic Noel, 2089 Cải trang Fide đen, 2090 Cải trang Bát Giới, 2091 Cải trang Ngộ Không, 2092 Cải trang Đường Tam Tạng, 2093 Cải trang Bella Quyến Rũ, 2094 Cải Trang Super Vegeta, 2095 Cải Trang Chiến Thần Lục Quang
--   testct3 : 2096 Cải Trang Chiến Thần Kim Hoàng, 2097 Cải Trang Fiona Ngọt Ngào, 2098 Cải trang Vegeta thời trang, 2099 Cải trang GohanBeast, 2100 Cải trang Vegeta, 2101 Cải Trang Goku áo cờ Việt Nam, 2102 Goku Boy Phố, 2103 Goku Nổi Loạn
--   testct4 : 2104 Thần Namek tối thượng, 2105 Cải Trang Goku Super Saiyan White, 2106 Cải Trang Goku Super Saiyan Red, 2107 Cải Trang Goku Super Saiyan God, 2108 Cải Trang Goku Super Saiyan Orange, 2109 Cải Trang Goku Super Saiyan Purple, 2110 Cải Trang Goku Super Saiyan Blue, 2111 Cải Trang Goku Super Saiyan Light Blue
--   testct5 : 2112 Cải Trang Goku Super Saiyan Green, 2113 Cải Trang Saiyan Silver Instinct Costume, 2114 Pet Lôi Thần, 2115 King Kong, 2116 Cá xanh, 2117 Cá cam, 2118 Pet Pikachu, 2119 Lân Linh Lung
--
-- Chạy SAU patch 48 và 49. Chạy lại vẫn an toàn (tự xoá đúng dải id của mình).
-- Cần khởi động lại server (giftcode chỉ nạp lúc khởi động).
-- =====================================================================

DELETE FROM `giftcode` WHERE `id` BETWEEN 101 AND 105;

INSERT INTO `giftcode` (`id`, `code`, `count_left`, `detail`, `datecreate`, `expired`) VALUES
(101, 'testct1', -1, '[{"id":2080,"quantity":1,"options":[]},{"id":2081,"quantity":1,"options":[]},{"id":2082,"quantity":1,"options":[]},{"id":2083,"quantity":1,"options":[]},{"id":2084,"quantity":1,"options":[]},{"id":2085,"quantity":1,"options":[]},{"id":2086,"quantity":1,"options":[]},{"id":2087,"quantity":1,"options":[]}]', NOW(), '2037-12-31 17:00:00'),
(102, 'testct2', -1, '[{"id":2088,"quantity":1,"options":[]},{"id":2089,"quantity":1,"options":[]},{"id":2090,"quantity":1,"options":[]},{"id":2091,"quantity":1,"options":[]},{"id":2092,"quantity":1,"options":[]},{"id":2093,"quantity":1,"options":[]},{"id":2094,"quantity":1,"options":[]},{"id":2095,"quantity":1,"options":[]}]', NOW(), '2037-12-31 17:00:00'),
(103, 'testct3', -1, '[{"id":2096,"quantity":1,"options":[]},{"id":2097,"quantity":1,"options":[]},{"id":2098,"quantity":1,"options":[]},{"id":2099,"quantity":1,"options":[]},{"id":2100,"quantity":1,"options":[]},{"id":2101,"quantity":1,"options":[]},{"id":2102,"quantity":1,"options":[]},{"id":2103,"quantity":1,"options":[]}]', NOW(), '2037-12-31 17:00:00'),
(104, 'testct4', -1, '[{"id":2104,"quantity":1,"options":[]},{"id":2105,"quantity":1,"options":[]},{"id":2106,"quantity":1,"options":[]},{"id":2107,"quantity":1,"options":[]},{"id":2108,"quantity":1,"options":[]},{"id":2109,"quantity":1,"options":[]},{"id":2110,"quantity":1,"options":[]},{"id":2111,"quantity":1,"options":[]}]', NOW(), '2037-12-31 17:00:00'),
(105, 'testct5', -1, '[{"id":2112,"quantity":1,"options":[]},{"id":2113,"quantity":1,"options":[]},{"id":2114,"quantity":1,"options":[]},{"id":2115,"quantity":1,"options":[]},{"id":2116,"quantity":1,"options":[]},{"id":2117,"quantity":1,"options":[]},{"id":2118,"quantity":1,"options":[]},{"id":2119,"quantity":1,"options":[]}]', NOW(), '2037-12-31 17:00:00');

SELECT `id`, `code`, `count_left` FROM `giftcode` WHERE `id` BETWEEN 101 AND 105;
