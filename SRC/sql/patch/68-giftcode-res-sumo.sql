-- =====================================================================
-- 68 — GIFTCODE THỬ 72 CẢI TRANG + 24 LINH THÚ MANG TỪ SUMO/BUN (2026-09-25)
-- =====================================================================
-- Chạy SAU patch 67. Mã đều đúng 7 ký tự, mỗi mã 10 món, cần 10 ô hành trang trống.
-- Mỗi tài khoản nhập mỗi mã một lần, số lượt không giới hạn.
--
--   sumoct1 (10 món): Majin Bư, Vegito Ultra, Vegito, Goten, Cải trang thành Whis, Thần Hủy Diệt Quitela, Obito Hủy Diệt, GoKu Tà Ác, Vegeta Evo, Goku MUI
--   sumoct2 (10 món): Android 21, Thần Hủy Diệt Mini, Rồng Omega, Vegeta SSJ4, Gogeta SSJ, Zeno Sama, Quy Lão Hồi Xuân, Gogeta Blue, Nữ Thần, Nữ Thần Mùa Hè
--   sumoct3 (10 món): Siêu Thần Namec, Siêu Broly, Siêu Jiren, Siêu Hit, Siêu Goku Vô Cực, Siêu Goten, Siêu Namec, Goku Blue, Hóa Khỉ, Siêu Santa
--   sumoct4 (10 món): Siêu Ốc Tiêu, Trunks, Trunks SSJ, Fu, Hóa Thần, Super Broly, OG73 Seventhree, Obito Lục Đạo, Ma Hề, Black myth Wukong
--   sumoct5 (10 món): Đường Huyền Trang, Ngộ Năng, Trư Nương, Bá Đinh, Kaido, Arthur, Sakura Hakari, Yumi Misaki, Chú Bộ Đội SuMo, Chú Bộ Đội Siêu Việt
--   sumoct6 (10 món): Tank51, Goku Thiên Sứ, Super Fide, Siêu Xên, Android 20, Bardock SSJ, Bardock, Super Broly SSJ4, Gotenks I, Gotenks II
--   sumoct7 (10 món): Gotenks III, Goku SSJ4 White Ultra, Vegeta SSJ4 White Ultra, Gohan SSJ4 White Ultra, Santa Chiến Công, Broly SSJ3, Jiren, Vegeta Cổ Trang, Thiên Sứ Cognac, Vegeta Ultra
--   sumoct8 ( 2 món): Cậu bé ham chơi, Cửu vĩ hồ ly
--   sumolt1 (10 món): Linh Hồn Hắc Ám, Tam Muội Chân Hỏa, Pét cánh cụt, Pét zuka, Pét Bánh Pao, Pét Gà chống, Pét cá mập, Naruto, Obito, Pét Obito
--   sumolt2 (10 món): Pét Bạch kim, Pét Cừu, Pét Thỏ Cuti, Pét Sư Tử S, Pét Sư Tử SS, Pét SS, Pét Hao Thiên Khuyển, Super Black Broly, Hắc Hóa Super Broly, Pét Fide
--   sumolt3 ( 4 món): Pét Sói Băng, Pét Sói, Pet mèo trắng, Pet mèo trắng 1
--
-- =====================================================================

DELETE FROM `giftcode` WHERE `code` LIKE 'sumoct%' OR `code` LIKE 'sumolt%';

INSERT INTO `giftcode` (`id`, `code`, `count_left`, `detail`, `datecreate`, `expired`) VALUES
(134, 'sumoct1', -1, '[{"id":2124,"quantity":1,"options":[]},{"id":2127,"quantity":1,"options":[]},{"id":2128,"quantity":1,"options":[]},{"id":2129,"quantity":1,"options":[]},{"id":2130,"quantity":1,"options":[]},{"id":2131,"quantity":1,"options":[]},{"id":2132,"quantity":1,"options":[]},{"id":2133,"quantity":1,"options":[]},{"id":2134,"quantity":1,"options":[]},{"id":2135,"quantity":1,"options":[]}]', NOW(), '2037-12-31 17:00:00'),
(135, 'sumoct2', -1, '[{"id":2136,"quantity":1,"options":[]},{"id":2137,"quantity":1,"options":[]},{"id":2138,"quantity":1,"options":[]},{"id":2139,"quantity":1,"options":[]},{"id":2140,"quantity":1,"options":[]},{"id":2141,"quantity":1,"options":[]},{"id":2142,"quantity":1,"options":[]},{"id":2143,"quantity":1,"options":[]},{"id":2144,"quantity":1,"options":[]},{"id":2145,"quantity":1,"options":[]}]', NOW(), '2037-12-31 17:00:00'),
(136, 'sumoct3', -1, '[{"id":2146,"quantity":1,"options":[]},{"id":2147,"quantity":1,"options":[]},{"id":2148,"quantity":1,"options":[]},{"id":2149,"quantity":1,"options":[]},{"id":2150,"quantity":1,"options":[]},{"id":2151,"quantity":1,"options":[]},{"id":2152,"quantity":1,"options":[]},{"id":2153,"quantity":1,"options":[]},{"id":2154,"quantity":1,"options":[]},{"id":2155,"quantity":1,"options":[]}]', NOW(), '2037-12-31 17:00:00'),
(137, 'sumoct4', -1, '[{"id":2156,"quantity":1,"options":[]},{"id":2162,"quantity":1,"options":[]},{"id":2163,"quantity":1,"options":[]},{"id":2164,"quantity":1,"options":[]},{"id":2165,"quantity":1,"options":[]},{"id":2166,"quantity":1,"options":[]},{"id":2167,"quantity":1,"options":[]},{"id":2170,"quantity":1,"options":[]},{"id":2173,"quantity":1,"options":[]},{"id":2174,"quantity":1,"options":[]}]', NOW(), '2037-12-31 17:00:00'),
(138, 'sumoct5', -1, '[{"id":2175,"quantity":1,"options":[]},{"id":2176,"quantity":1,"options":[]},{"id":2177,"quantity":1,"options":[]},{"id":2178,"quantity":1,"options":[]},{"id":2179,"quantity":1,"options":[]},{"id":2180,"quantity":1,"options":[]},{"id":2181,"quantity":1,"options":[]},{"id":2182,"quantity":1,"options":[]},{"id":2184,"quantity":1,"options":[]},{"id":2185,"quantity":1,"options":[]}]', NOW(), '2037-12-31 17:00:00'),
(139, 'sumoct6', -1, '[{"id":2186,"quantity":1,"options":[]},{"id":2194,"quantity":1,"options":[]},{"id":2195,"quantity":1,"options":[]},{"id":2197,"quantity":1,"options":[]},{"id":2198,"quantity":1,"options":[]},{"id":2199,"quantity":1,"options":[]},{"id":2200,"quantity":1,"options":[]},{"id":2201,"quantity":1,"options":[]},{"id":2202,"quantity":1,"options":[]},{"id":2203,"quantity":1,"options":[]}]', NOW(), '2037-12-31 17:00:00'),
(140, 'sumoct7', -1, '[{"id":2204,"quantity":1,"options":[]},{"id":2205,"quantity":1,"options":[]},{"id":2206,"quantity":1,"options":[]},{"id":2207,"quantity":1,"options":[]},{"id":2208,"quantity":1,"options":[]},{"id":2209,"quantity":1,"options":[]},{"id":2212,"quantity":1,"options":[]},{"id":2213,"quantity":1,"options":[]},{"id":2214,"quantity":1,"options":[]},{"id":2215,"quantity":1,"options":[]}]', NOW(), '2037-12-31 17:00:00'),
(141, 'sumoct8', -1, '[{"id":2216,"quantity":1,"options":[]},{"id":2217,"quantity":1,"options":[]}]', NOW(), '2037-12-31 17:00:00'),
(142, 'sumolt1', -1, '[{"id":2125,"quantity":1,"options":[]},{"id":2126,"quantity":1,"options":[]},{"id":2157,"quantity":1,"options":[]},{"id":2158,"quantity":1,"options":[]},{"id":2159,"quantity":1,"options":[]},{"id":2160,"quantity":1,"options":[]},{"id":2161,"quantity":1,"options":[]},{"id":2168,"quantity":1,"options":[]},{"id":2169,"quantity":1,"options":[]},{"id":2171,"quantity":1,"options":[]}]', NOW(), '2037-12-31 17:00:00'),
(143, 'sumolt2', -1, '[{"id":2172,"quantity":1,"options":[]},{"id":2183,"quantity":1,"options":[]},{"id":2187,"quantity":1,"options":[]},{"id":2188,"quantity":1,"options":[]},{"id":2189,"quantity":1,"options":[]},{"id":2190,"quantity":1,"options":[]},{"id":2191,"quantity":1,"options":[]},{"id":2192,"quantity":1,"options":[]},{"id":2193,"quantity":1,"options":[]},{"id":2196,"quantity":1,"options":[]}]', NOW(), '2037-12-31 17:00:00'),
(144, 'sumolt3', -1, '[{"id":2210,"quantity":1,"options":[]},{"id":2211,"quantity":1,"options":[]},{"id":2218,"quantity":1,"options":[]},{"id":2219,"quantity":1,"options":[]}]', NOW(), '2037-12-31 17:00:00');

SELECT `code`, LEFT(`detail`,60) FROM `giftcode` WHERE `code` LIKE 'sumo%';
