-- =====================================================================
-- 72 — GIFTCODE ĐỢT 2 CHO RES SUMO (2026-09-25)
-- =====================================================================
-- Chạy SAU patch 67, 69, 70. Mã đúng 7 ký tự, mỗi mã tối đa 10 món,
-- cần đủ ô hành trang trống (thiếu thì server báo, không mất đồ).
--
-- Mỗi tài khoản chỉ nhập được MỖI MÃ một lần. Patch 68/71 đã phát đủ 138 món;
-- bộ "sumo2*" dưới đây là BẢN SAO của đúng những món đó, để thử lại lần nữa
-- trên cùng tài khoản mà không phải xoá lịch sử giftcode.
--
-- sumovip: 10 món tuyển — 5 cải trang đẹp nhất, 2 thú cưỡi mới, 3 đồ đeo lưng bự.
--
--   sumovip (10 món): Vegito Ultra, Goku MUI, Zeno Sama, Gogeta Blue, Kaido, Kỳ Lân Bạch Kim, Phượng Hoàng Thất Sắc, Thiên Long Đao, Cánh VIP, Pháp trận Tru Tiên
--   sumo2c1 (10 món): Majin Bư, Vegito Ultra, Vegito, Goten, Cải trang thành Whis, Thần Hủy Diệt Quitela, Obito Hủy Diệt, GoKu Tà Ác, Vegeta Evo, Goku MUI
--   sumo2c2 (10 món): Android 21, Thần Hủy Diệt Mini, Rồng Omega, Vegeta SSJ4, Gogeta SSJ, Zeno Sama, Quy Lão Hồi Xuân, Gogeta Blue, Nữ Thần, Nữ Thần Mùa Hè
--   sumo2c3 (10 món): Siêu Thần Namec, Siêu Broly, Siêu Jiren, Siêu Hit, Siêu Goku Vô Cực, Siêu Goten, Siêu Namec, Goku Blue, Hóa Khỉ, Siêu Santa
--   sumo2c4 (10 món): Siêu Ốc Tiêu, Trunks, Trunks SSJ, Fu, Hóa Thần, Super Broly, OG73 Seventhree, Obito Lục Đạo, Ma Hề, Black myth Wukong
--   sumo2c5 (10 món): Đường Huyền Trang, Ngộ Năng, Trư Nương, Bá Đinh, Kaido, Arthur, Sakura Hakari, Yumi Misaki, Chú Bộ Đội SuMo, Chú Bộ Đội Siêu Việt
--   sumo2c6 (10 món): Tank51, Goku Thiên Sứ, Super Fide, Siêu Xên, Android 20, Bardock SSJ, Bardock, Super Broly SSJ4, Gotenks I, Gotenks II
--   sumo2c7 (10 món): Gotenks III, Goku SSJ4 White Ultra, Vegeta SSJ4 White Ultra, Gohan SSJ4 White Ultra, Santa Chiến Công, Broly SSJ3, Jiren, Vegeta Cổ Trang, Thiên Sứ Cognac, Vegeta Ultra
--   sumo2c8 ( 2 món): Cậu bé ham chơi, Cửu vĩ hồ ly
--   sumo2l1 (10 món): Linh Hồn Hắc Ám, Tam Muội Chân Hỏa, Pét cánh cụt, Pét zuka, Pét Bánh Pao, Pét Gà chống, Pét cá mập, Naruto, Obito, Pét Obito
--   sumo2l2 (10 món): Pét Bạch kim, Pét Cừu, Pét Thỏ Cuti, Pét Sư Tử S, Pét Sư Tử SS, Pét SS, Pét Hao Thiên Khuyển, Super Black Broly, Hắc Hóa Super Broly, Pét Fide
--   sumo2l3 ( 4 món): Pét Sói Băng, Pét Sói, Pet mèo trắng, Pet mèo trắng 1
--   sumo2d1 (10 món): Vòng sáng thiên thần 2, Thần Hỏa, Thần Mộc, Thần Thổ, Kiếm Vip, Cây tre trăm đốt, Hào quang Thần, Hào quang Hit, Thiên Long Đao, Cánh VIP
--   sumo2d2 (10 món): Hào quang VIP, Cá xám, Đao Quan Vũ, Ngọc Thanh Long, Quạt Tiêu, Hồn Hoàn, Đao truy hồn, Dao Găm, Trượng Thiên Sứ, Hào quang kaioken
--   sumo2d3 (10 món): Samehada, Quạt Gunbai, Danh Hiệu Giang Hồ, Kiếm đỏ, Kiếm xanh, Cánh thời trang 3, Cánh thời trang 7, Cánh thời trang 8, Cánh thời trang 6, Cánh thời trang 5
--   sumo2d4 (10 món): Fashion Wing 2, Fashion Wing 4, Câu Hồn Ma S, Câu Hồn Ma SS, Ngũ Hành Kỳ, Linh Hồn S, Linh Hồn SS, Linh Hồn SSS, Kiếm Linh, Pháp trận Tru Tiên
--   sumo2t1 ( 2 món): Kỳ Lân Bạch Kim, Phượng Hoàng Thất Sắc
-- =====================================================================

DELETE FROM `giftcode` WHERE `code` LIKE 'sumovip' OR `code` LIKE 'sumo2%';

INSERT INTO `giftcode` (`id`, `code`, `count_left`, `detail`, `datecreate`, `expired`) VALUES
(150, 'sumovip', -1, '[{"id":2127,"quantity":1,"options":[]},{"id":2135,"quantity":1,"options":[]},{"id":2141,"quantity":1,"options":[]},{"id":2143,"quantity":1,"options":[]},{"id":2179,"quantity":1,"options":[]},{"id":2260,"quantity":1,"options":[]},{"id":2261,"quantity":1,"options":[]},{"id":2228,"quantity":1,"options":[]},{"id":2229,"quantity":1,"options":[]},{"id":2259,"quantity":1,"options":[]}]', NOW(), '2037-12-31 17:00:00'),
(151, 'sumo2c1', -1, '[{"id":2124,"quantity":1,"options":[]},{"id":2127,"quantity":1,"options":[]},{"id":2128,"quantity":1,"options":[]},{"id":2129,"quantity":1,"options":[]},{"id":2130,"quantity":1,"options":[]},{"id":2131,"quantity":1,"options":[]},{"id":2132,"quantity":1,"options":[]},{"id":2133,"quantity":1,"options":[]},{"id":2134,"quantity":1,"options":[]},{"id":2135,"quantity":1,"options":[]}]', NOW(), '2037-12-31 17:00:00'),
(152, 'sumo2c2', -1, '[{"id":2136,"quantity":1,"options":[]},{"id":2137,"quantity":1,"options":[]},{"id":2138,"quantity":1,"options":[]},{"id":2139,"quantity":1,"options":[]},{"id":2140,"quantity":1,"options":[]},{"id":2141,"quantity":1,"options":[]},{"id":2142,"quantity":1,"options":[]},{"id":2143,"quantity":1,"options":[]},{"id":2144,"quantity":1,"options":[]},{"id":2145,"quantity":1,"options":[]}]', NOW(), '2037-12-31 17:00:00'),
(153, 'sumo2c3', -1, '[{"id":2146,"quantity":1,"options":[]},{"id":2147,"quantity":1,"options":[]},{"id":2148,"quantity":1,"options":[]},{"id":2149,"quantity":1,"options":[]},{"id":2150,"quantity":1,"options":[]},{"id":2151,"quantity":1,"options":[]},{"id":2152,"quantity":1,"options":[]},{"id":2153,"quantity":1,"options":[]},{"id":2154,"quantity":1,"options":[]},{"id":2155,"quantity":1,"options":[]}]', NOW(), '2037-12-31 17:00:00'),
(154, 'sumo2c4', -1, '[{"id":2156,"quantity":1,"options":[]},{"id":2162,"quantity":1,"options":[]},{"id":2163,"quantity":1,"options":[]},{"id":2164,"quantity":1,"options":[]},{"id":2165,"quantity":1,"options":[]},{"id":2166,"quantity":1,"options":[]},{"id":2167,"quantity":1,"options":[]},{"id":2170,"quantity":1,"options":[]},{"id":2173,"quantity":1,"options":[]},{"id":2174,"quantity":1,"options":[]}]', NOW(), '2037-12-31 17:00:00'),
(155, 'sumo2c5', -1, '[{"id":2175,"quantity":1,"options":[]},{"id":2176,"quantity":1,"options":[]},{"id":2177,"quantity":1,"options":[]},{"id":2178,"quantity":1,"options":[]},{"id":2179,"quantity":1,"options":[]},{"id":2180,"quantity":1,"options":[]},{"id":2181,"quantity":1,"options":[]},{"id":2182,"quantity":1,"options":[]},{"id":2184,"quantity":1,"options":[]},{"id":2185,"quantity":1,"options":[]}]', NOW(), '2037-12-31 17:00:00'),
(156, 'sumo2c6', -1, '[{"id":2186,"quantity":1,"options":[]},{"id":2194,"quantity":1,"options":[]},{"id":2195,"quantity":1,"options":[]},{"id":2197,"quantity":1,"options":[]},{"id":2198,"quantity":1,"options":[]},{"id":2199,"quantity":1,"options":[]},{"id":2200,"quantity":1,"options":[]},{"id":2201,"quantity":1,"options":[]},{"id":2202,"quantity":1,"options":[]},{"id":2203,"quantity":1,"options":[]}]', NOW(), '2037-12-31 17:00:00'),
(157, 'sumo2c7', -1, '[{"id":2204,"quantity":1,"options":[]},{"id":2205,"quantity":1,"options":[]},{"id":2206,"quantity":1,"options":[]},{"id":2207,"quantity":1,"options":[]},{"id":2208,"quantity":1,"options":[]},{"id":2209,"quantity":1,"options":[]},{"id":2212,"quantity":1,"options":[]},{"id":2213,"quantity":1,"options":[]},{"id":2214,"quantity":1,"options":[]},{"id":2215,"quantity":1,"options":[]}]', NOW(), '2037-12-31 17:00:00'),
(158, 'sumo2c8', -1, '[{"id":2216,"quantity":1,"options":[]},{"id":2217,"quantity":1,"options":[]}]', NOW(), '2037-12-31 17:00:00'),
(159, 'sumo2l1', -1, '[{"id":2125,"quantity":1,"options":[]},{"id":2126,"quantity":1,"options":[]},{"id":2157,"quantity":1,"options":[]},{"id":2158,"quantity":1,"options":[]},{"id":2159,"quantity":1,"options":[]},{"id":2160,"quantity":1,"options":[]},{"id":2161,"quantity":1,"options":[]},{"id":2168,"quantity":1,"options":[]},{"id":2169,"quantity":1,"options":[]},{"id":2171,"quantity":1,"options":[]}]', NOW(), '2037-12-31 17:00:00'),
(160, 'sumo2l2', -1, '[{"id":2172,"quantity":1,"options":[]},{"id":2183,"quantity":1,"options":[]},{"id":2187,"quantity":1,"options":[]},{"id":2188,"quantity":1,"options":[]},{"id":2189,"quantity":1,"options":[]},{"id":2190,"quantity":1,"options":[]},{"id":2191,"quantity":1,"options":[]},{"id":2192,"quantity":1,"options":[]},{"id":2193,"quantity":1,"options":[]},{"id":2196,"quantity":1,"options":[]}]', NOW(), '2037-12-31 17:00:00'),
(161, 'sumo2l3', -1, '[{"id":2210,"quantity":1,"options":[]},{"id":2211,"quantity":1,"options":[]},{"id":2218,"quantity":1,"options":[]},{"id":2219,"quantity":1,"options":[]}]', NOW(), '2037-12-31 17:00:00'),
(162, 'sumo2d1', -1, '[{"id":2220,"quantity":1,"options":[]},{"id":2221,"quantity":1,"options":[]},{"id":2222,"quantity":1,"options":[]},{"id":2223,"quantity":1,"options":[]},{"id":2224,"quantity":1,"options":[]},{"id":2225,"quantity":1,"options":[]},{"id":2226,"quantity":1,"options":[]},{"id":2227,"quantity":1,"options":[]},{"id":2228,"quantity":1,"options":[]},{"id":2229,"quantity":1,"options":[]}]', NOW(), '2037-12-31 17:00:00'),
(163, 'sumo2d2', -1, '[{"id":2230,"quantity":1,"options":[]},{"id":2231,"quantity":1,"options":[]},{"id":2232,"quantity":1,"options":[]},{"id":2233,"quantity":1,"options":[]},{"id":2234,"quantity":1,"options":[]},{"id":2235,"quantity":1,"options":[]},{"id":2236,"quantity":1,"options":[]},{"id":2237,"quantity":1,"options":[]},{"id":2238,"quantity":1,"options":[]},{"id":2239,"quantity":1,"options":[]}]', NOW(), '2037-12-31 17:00:00'),
(164, 'sumo2d3', -1, '[{"id":2240,"quantity":1,"options":[]},{"id":2241,"quantity":1,"options":[]},{"id":2242,"quantity":1,"options":[]},{"id":2243,"quantity":1,"options":[]},{"id":2244,"quantity":1,"options":[]},{"id":2245,"quantity":1,"options":[]},{"id":2246,"quantity":1,"options":[]},{"id":2247,"quantity":1,"options":[]},{"id":2248,"quantity":1,"options":[]},{"id":2249,"quantity":1,"options":[]}]', NOW(), '2037-12-31 17:00:00'),
(165, 'sumo2d4', -1, '[{"id":2250,"quantity":1,"options":[]},{"id":2251,"quantity":1,"options":[]},{"id":2252,"quantity":1,"options":[]},{"id":2253,"quantity":1,"options":[]},{"id":2254,"quantity":1,"options":[]},{"id":2255,"quantity":1,"options":[]},{"id":2256,"quantity":1,"options":[]},{"id":2257,"quantity":1,"options":[]},{"id":2258,"quantity":1,"options":[]},{"id":2259,"quantity":1,"options":[]}]', NOW(), '2037-12-31 17:00:00'),
(166, 'sumo2t1', -1, '[{"id":2260,"quantity":1,"options":[]},{"id":2261,"quantity":1,"options":[]}]', NOW(), '2037-12-31 17:00:00');

SELECT `code`, LEFT(`detail`, 50) AS `detail` FROM `giftcode` WHERE `code` LIKE 'sumo%';
