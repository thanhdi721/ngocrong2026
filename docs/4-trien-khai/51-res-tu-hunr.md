# 51 — Res bên source HUNR mà server mình chưa có (2026-09-23)

Nguồn: `/Users/phanthanhdi/Downloads/HUNR_Server/Hunr2026` — SQL `sql/SQL_HUNR_2025.sql`, ảnh `resources/image/{1..4}`.

So sánh theo TÊN vật phẩm với `item_template` bên mình (dump + patch 01, 35, 40).


Phân loại:

* **A — mang về được ngay**: có đủ 3 part (đầu / thân / chân) và mọi icon của part đều có file ảnh.
* **B — mang về được**: thú cưỡi có đủ ảnh `mount_<id>_0/1`.
* **C — chưa đủ ảnh**: chỉ có dòng vật phẩm và icon, thiếu part hoặc thiếu ảnh riêng; mang về sẽ không hiện hình.

Lưu ý khi port: id icon và id part bên HUNR trùng dải với mình nên phải đánh số lại, giống lần mang đồ từ NGOL (xem `tools/port_item_from_ngol.py` và patch 35). Part bên HUNR lưu dạng JSON `{"dx","dy","id"}`, phải đổi sang dạng mảng `[[icon,dx,dy],...]` của mình.


## Cải trang — mình chưa có 79 món


### A — đủ 3 part + icon (41)

| id HUNR | Tên | part đầu/thân/chân | icon |
|---|---|---|---|
| 710 | Cải trang Quy lão Kame | 554/555/556 | 5175 |
| 760 | Cải trang Tiểu Ngọc Thần Tiên | 772/773/774 | 7102 |
| 765 | Cải trang Ma Din Bư | 427/428/429 | 4261 |
| 879 | Cậu Kilo | 712/713/714 | 6618 |
| 884 | Cải trang Cậu Hít | 520/521/522 | 4969 |
| 885 | Cải trang Lích Tên | 888/889/890 | 8147 |
| 904 | Cải trang Super Black Goku | 906/880/881 | 8248 |
| 911 | Cải Trang Siêu Saiyan Blue  | 542/523/524 | 5087 |
| 924 | Cải trang Radic Noel | 940/941/942 | 8514 |
| 2016 | Cải trang Fide đen | 1965/1966/1967 | 23300 |
| 2083 | Cải trang Bát Giới | 465/466/464 | 4498 |
| 2084 | Cải trang Ngộ Không | 457/458/459 | 4543 |
| 2085 | Cải trang Đường Tam Tạng | 467/468/469 | 4521 |
| 2104 | Cải trang Bella Quyến Rũ | 2117/2118/2119 | 16607 |
| 2116 | Cải Trang Super Vegeta | 2111/2112/2113 | 25816 |
| 2121 | Cải Trang Chiến Thần Lục Quang | 2129/2130/2131 | 13237 |
| 2122 | Cải Trang Chiến Thần Kim Hoàng | 2123/2124/2125 | 13135 |
| 2123 | Cải Trang Fiona Ngọt Ngào | 2120/2121/2122 | 16640 |
| 2269 | Cải trang sự kiện | 2111/2112/2113 | 25816 |
| 2317 | Cải trang Vegeta thời trang | 1298/1299/1300 | 15450 |
| 2318 | Cải trang GohanBeast | 1301/1302/1303 | 15298 |
| 2335 | Cải trang Gotenk SSJ | 1311/1313/1314 | 26113 |
| 2336 | Cải trang Gotenk SSJ 3 | 1312/1313/1314 | 26115 |
| 2402 | Cải trang Vegeta | 1333/1334/1335 | 14402 |
| 2438 | Cải Trang Goku áo cờ Việt Nam | 1351/1352/1353 | 9429 |
| 2449 | Goku Boy Phố  | 2150/2151/2152 | 17500 |
| 2450 | Goku Nổi Loạn  | 2153/2154/2155 | 22242 |
| 2451 | Thần Namek tối thượng | 2156/2157/2158 | 22273 |
| 2452 | Cải Trang Goku Super Saiyan White | 2159/2160/2161 | 27070 |
| 2453 | Cải Trang Goku Super Saiyan Red | 2162/2163/2164 | 27103 |
| 2454 | Cải Trang Goku Super Saiyan God | 2165/2166/2167 | 27136 |
| 2455 | Cải Trang Goku Super Saiyan Orange | 2168/2169/2170 | 27169 |
| 2456 | Cải Trang Goku Super Saiyan Purple | 2171/2172/2173 | 27220 |
| 2457 | Cải Trang Goku Super Saiyan Blue | 2174/2175/2176 | 27257 |
| 2458 | Cải Trang Goku Super Saiyan Light Blue | 2177/2178/2179 | 27322 |
| 2459 | Cải Trang Goku Super Saiyan Green | 2180/2181/2182 | 27355 |
| 2460 | Cải Trang Saiyan Silver Instinct Costume | 2183/2184/2185 | 27400 |
| 2461 | Cải Trang Super Trunks | 2186/2187/2188 | 27465 |
| 2473 | dsdd | 2186/2189/2190 | 23100 |
| 2474 | ư3ewqe | 2191/2194/2195 | 23200 |
| 2475 | 434343 | 2196/2199/2200 | 23300 |

### C — chưa đủ ảnh (38)

| id HUNR | Tên | part đầu/thân/chân | icon |
|---|---|---|---|
| 989 | S | 1053/1054/1055 | 9407 |
| 990 | S | 1056/1057/1058 | 9433 |
| 991 | S | 1059/1060/1061 | 9458 |
| 1040 | Cải trang Elec | 2006/2007/2008 | 15078 |
| 1041 | Cải trang Gas | 2009/2010/2011 | 15109 |
| 1042 | Cải trang thành Macki | 2012/2013/2014 | 15140 |
| 1043 | Cải trang thành Granola | 2018/2019/2020 | 15202 |
| 1049 | Cải trang Ironman | 2069/2070/2071 | 11080 |
| 1053 | Cải trang Gojo Satoru | 1216/1217/1218 | 16006 |
| 1054 | Cải trang Heat | 2048/2049/2050 | 11394 |
| 1055 | Cải trang Black Panther | 2066/2067/2068 | 11040 |
| 1058 | Cải trang Anubis | 2072/2073/2074 | 13270 |
| 1059 | Cải trang Cucumber Black | 1204/1205/1206 | 11113 |
| 1060 | Cải trang Cucumber Đại Đế | 1207/1208/1209 | 11145 |
| 1983 | Cải trang Tết 2024 | 1974/1975/1976 | 23032 |
| 2020 | Cải trang Janemba cha cha cha | 1991/1992/1993 | 16387 |
| 2033 | Cải trang Bunma tóc cam | 1989/1987/1988 | 20114 |
| 2034 | Cải trang Bunma tóc xanh | 1986/1987/1988 | 20081 |
| 2035 | Cải trang Bunma tóc tím | 1990/1987/1988 | 20118 |
| 2063 | Cải trang Vua lỳ đòn | 1983/1984/1985 | 11658 |
| 2064 | Cải trang Bư gầy trơ xương | 1980/1981/1982 | 11455 |
| 2069 | Cải trang anh trai Bông Băng | 1953/1954/1955 | 12192 |
| 2102 | Cải trang Drabura Forst | — | 11053 |
| 2120 | Cải Trang Thiên Long Tử Thần | 2126/2127/2128 | 14036 |
| 2148 | Cải Trang Goku Rose Tết | 2138/2139/2140 | 16441 |
| 2229 | Cải trang Baby Vegeta | 1231/1232/1233 | 14569 |
| 2244 | Bill Bé Nhỏ | 1245/1246/1247 | 12793 |
| 2246 | Bách Ngọc Tiên | 1248/1249/1250 | 18177 |
| 2247 | Hắc Long Saiyan | 1251/1252/1253 | 18209 |
| 2248 | Thiên tài Công nghệ | 1263/1264/1265 | 18242 |
| 2253 | Saiyan Cuồng Nộ | 1254/1255/1256 | 18347 |
| 2254 | Thần lửa hủy diệt | 1257/1258/1259 | 18384 |
| 2255 | Zamasu Fusion | 1286/1287/1288 | 22910 |
| 2287 | Goku SSJ | 1289/1290/1291 | 25857 |
| 2289 | Cải trang Thần Lửa | 1260/1261/1262 | 18408 |
| 2312 | Cải trang Zamasu Hắc Ám | 1292/1293/1294 | 15125 |
| 2319 | Cải trang Nak Vệ Thần | — | 0 |
| 2343 | Cải trang Vegeta Blue | 1307/1308/1309 | 14462 |

## Pet / linh thú — mình chưa có 34 món


### A — đủ 3 part + icon (7)

| id HUNR | Tên | part đầu/thân/chân | icon |
|---|---|---|---|
| 2103 | Pet Kẻ Xâm Lăng | 2114/2115/2116 | 15828 |
| 2189 | Pet Lôi Thần | 2147/2148/2149 | 12993 |
| 2313 | King Kong | 1295/1296/1297 | 12853 |
| 2403 | Cá xanh | 1336/1337/1338 | 15354 |
| 2404 | Cá cam | 1339/1340/1341 | 15355 |
| 2448 | Pet Pikachu | 1354/1355/1356 | 15643 |
| 2486 | Lân Linh Lung | 2201/2202/2203 | 23520 |

### C — chưa đủ ảnh (27)

| id HUNR | Tên | part đầu/thân/chân | icon |
|---|---|---|---|
| 1034 | Hiệp sĩ | — | 15042 |
| 1035 | Tiểu Ác Ma | — | 15044 |
| 1989 | Linh thú Tết 2024 | — | 31232 |
| 1993 | Linh thú Tết 2024 | — | 15039 |
| 2032 | Linh Thú Băng Thạch | — | 15048 |
| 2072 | Linh Thú Vua khè lửa | — | 15315 |
| 2119 | Linh Thú Tiểu Thần Miêu | — | 20550 |
| 2209 | Bunny Baby | 1280/1281/1282 | 14796 |
| 2210 | Baby Rồng Xanh | 1283/1284/1285 | 14797 |
| 2232 | Baby Rồng Xanh Secret | 1280/1237/1238 | 14907 |
| 2233 | BearChu Secret  | 1280/1239/1240 | 14799 |
| 2234 | Buny Baby Secret | 1280/1241/1242 | 14899 |
| 2235 | Panda Chan | 1280/1243/1244 | 14823 |
| 2236 | Panda Chan | 1280/1243/1244 | 14823 |
| 2240 | Thần Linh Bé Nhỏ | — | 20546 |
| 2241 | Quỷ Kiếm Đỏ | — | 20548 |
| 2242 | Cáo Linh Thần | — | 20550 |
| 2252 | Tiểu xà vương | 1266/1267/1268 | 18304 |
| 2258 | Tiểu miêu linh | — | 20550 |
| 2366 | Dreamlet | — | 15056 |
| 2367 | Firenix | — | 15060 |
| 2368 | Bông bay | — | 15066 |
| 2369 | Ghost | — | 15070 |
| 2370 | Darkflame Phoenix | — | 22008 |
| 2371 | Shadow Lynx | — | 22010 |
| 2372 | Shadowfire Raven | — | 22012 |
| 2373 | Blue Shadow Bee | — | 22014 |

## Thú cưỡi — mình chưa có 21 món


### B — đủ ảnh thú cưỡi (2)

| id HUNR | Tên | part đầu/thân/chân | icon |
|---|---|---|---|
| 2190 | Dải Ngân Hà | mount 34 | 12996 |
| 2191 | Cầu vòng bay | mount 21 | 11792 |

### C — chưa đủ ảnh (19)

| id HUNR | Tên | part đầu/thân/chân | icon |
|---|---|---|---|
| 1987 | Rồng đỏ | mount 30017 | 31230 |
| 1988 | Rồng vàng | mount 30018 | 31232 |
| 2022 | Phượng Hoàng Ma Pháp | mount 30019 | 16453 |
| 2031 | Thú cưỡi Chim Hồng | mount 30020 | 31238 |
| 2044 | Hoả long thần vương | mount 30021 | 14917 |
| 2062 | Thú cưỡi ngựa tiến vua | mount 30029 | 31278 |
| 2066 | Đầu chó sừng Lân | mount 30023 | 31257 |
| 2114 | Thú Cưỡi Phượng Hoàng | mount 30014 | 16452 |
| 2117 | Xe Ngựa Hoàng Gia | mount 30029 | 31278 |
| 2149 | Ngọc Sen Hồng | mount 30017 | 11527 |
| 2150 | Ngọc Sen Vàng Phú Quý | mount 30018 | 11528 |
| 2256 | Cá Chép Thần Kỳ | — | 18411 |
| 2257 | Xe ngựa lộng lãy | — | 18412 |
| 2259 | Hắc kỳ lân | — | 18410 |
| 2288 | Hỏa Ngọc Phi Tiễn | — | 9130 |
| 2315 | Cá mập siêu việt | — | 9143 |
| 2331 | Thú cưỡi Thần Long | — | 9160 |
| 2354 | Quả cầu nước | — | 9159 |
| 2447 | Thú cưỡi Hỏa Quy | — | 9149 |

## Hào quang — mình chưa có 3 món


### C — chưa đủ ảnh (3)

| id HUNR | Tên | part đầu/thân/chân | icon |
|---|---|---|---|
| 2230 | Hào Quang Ám Lang | — | 14935 |
| 2353 | Hào quang Hỏa thần | — | 11545 |
| 2439 | Hào quang sao rơi | — | 9428 |

## Ngọc bội — mình chưa có 6 món


### C — chưa đủ ảnh (6)

| id HUNR | Tên | part đầu/thân/chân | icon |
|---|---|---|---|
| 2093 | Ngọc bội Đại Hải trình | — | 31275 |
| 2094 | Ngọc bội Luffy | — | 31268 |
| 2099 | Ngọc bội Thần Công | — | 31273 |
| 2151 | Ngọc Thần Công | — | 20270 |
| 2152 | Ngọc Thần Long | — | 20271 |
| 2396 | Hắc hỏa ấn | — | 20271 |

## Danh hiệu — mình chưa có 49 món


### C — chưa đủ ảnh (49)

| id HUNR | Tên | part đầu/thân/chân | icon |
|---|---|---|---|
| 1025 | Top 1 Sức Mạnh | — | 7101 |
| 1026 | Top 2 Sức Mạnh | — | 7100 |
| 1027 | Top 3 Sức Mạnh | — | 7099 |
| 1028 | Top Sức Mạnh | — | 7098 |
| 1974 | Danh hiệu Top 1 | — | 9653 |
| 1975 | Danh hiệu Top 2  | — | 9655 |
| 1976 | Danh hiệu Top 3  | — | 9657 |
| 1977 | Danh hiệu Top 1 Nạp | — | 15530 |
| 1978 | Danh hiệu Top 2 Nạp | — | 15530 |
| 1979 | Danh hiệu Top 3 Nạp | — | 15530 |
| 1980 | Danh hiệu Top 1 Server | — | 15529 |
| 1981 | Danh hiệu Top 2 Server | — | 15529 |
| 1982 | Danh hiệu Top 3 Server | — | 15529 |
| 1998 | Danh hiệu Fan Cứng | — | 15623 |
| 2036 | Danh hiệu Thiên Tử | — | 11571 |
| 2037 | Danh hiệu Cửu Thiên | — | 31239 |
| 2038 | Danh hiệu Thiên Ưng | — | 31240 |
| 2067 | Danh hiệu Thần Hộ Vệ | — | 31258 |
| 2068 | Danh hiệu Thần Thiên Sứ | — | 31261 |
| 2113 | Danh hiệu Thần Long Bất Diệt | — | 0 |
| 2158 | Danh Hiệu Bé Ngoan | — | 31263 |
| 2159 | Danh Hiệu Phong Ba | — | 31265 |
| 2177 | Top 1 Đại Gia | — | 32226 |
| 2178 | Top 2 Đại Gia | — | 32228 |
| 2179 | Top 3 Đại Gia | — | 32230 |
| 2180 | Top 1 Nhiệm Vụ | — | 32232 |
| 2181 | Top 2 Nhiệm Vụ | — | 32234 |
| 2182 | Top 3 Nhiệm Vụ | — | 32236 |
| 2183 | Top 1 Sức Mạnh | — | 32238 |
| 2184 | Top 2 Sức Mạnh | — | 32240 |
| 2185 | Top 3 Sức Mạnh | — | 32242 |
| 2186 | Top 1 Vòng Quay | — | 32244 |
| 2187 | Top 2 Vòng Quay | — | 32246 |
| 2188 | Top 3 Vòng Quay | — | 32248 |
| 2198 | Danh hiệu | — | 16660 |
| 2260 | Danh hiệu Bất Bại | — | 18413 |
| 2310 | Danh hiệu Hồi Ức | — | 9139 |
| 2316 | Danh hiệu Thần Thoại | — | 21960 |
| 2322 | Danh hiệu Cày Top 1 | — | 9167 |
| 2323 | Danh hiệu Cày Top 2 | — | 9165 |
| 2324 | Danh hiệu Cày Top 3 | — | 9163 |
| 2325 | Danh hiệu Thánh Úp Đệ | — | 9161 |
| 2332 | Danh hiệu VIP 1 | — | 11564 |
| 2333 | Danh hiệu VIP 2 | — | 11565 |
| 2334 | Danh hiệu VIP 3 | — | 11566 |
| 2358 | Danh hiệu Top Săn Boss | — | 9651 |
| 2436 | Danh hiệu Quốc Khánh | — | 9426 |
| 2481 | Danh Hiệu Bính Ngọ 2026 | — | 3 |
| 2482 | Danh Hiệu Siêu Sao | — | 3 |

## Tổng

| Nhóm | Số món |
|---|---|
| A — mang về được ngay | 48 |
| B — thú cưỡi đủ ảnh | 2 |
| C — chưa đủ ảnh | 142 |

## Những thứ bên mình đang NHIỀU hơn HUNR

* Ảnh theo tên (`img_by_name`): mình 288 file, HUNR chỉ 56. Hào quang mình có 67 bộ, HUNR 3. Thú cưỡi mình 46 bộ, HUNR 21.
* Cờ đeo lưng (loại 28): hai bên đều 14 món, trùng tên hết — không thiếu gì.
* Thư mục res của HUNR gửi kèm thiếu nhiều ảnh so với DB của chính họ: bảng `nr_image_by_name` có 122 dòng nhưng chỉ có 56 file. Vì vậy phần lớn thú cưỡi mới (mã 30014–30029) không có ảnh.

## Đã mang về (patch 48, 2026-09-23)

Công cụ: `SRC/tools/port_item_from_hunr.py` (chạy `--apply` để chép ảnh + ghi patch).
Patch: `SRC/sql/patch/48-cai-trang-tu-hunr.sql` — 34 cải trang, vật phẩm 2080–2113, part 2238–2339.

* 4.208 file ảnh mới trong `data/icon/x1..x4`; 1.052 icon đều được đánh số lại vì trùng dải với mình.
* HUNR thiếu nhiều ảnh mức x1 (9.427 / 12.557), nên mức thiếu được suy ra từ mức lớn nhất đang có.
* HUNR không có bảng `head_avatar`, nên 34 avatar được tự dựng: ghép mảnh đầu theo dx/dy rồi phóng to 256px.
* `vsItem` 16, `vsData` 22.

Không mang được, do dữ liệu gốc bên HUNR sai:

| Món | Lý do |
|---|---|
| Cải trang Gotenk SSJ, Gotenk SSJ 3 | part thân / chân khai báo nhầm thành loại đầu (3 mảnh) |
| Cải Trang Super Trunks | 2187 / 2188 là khung đầu động, không có part thân / chân |
| Cải trang Drabura Forst, Nak Vệ Thần | không có part nào (head/body/leg = -1) |
| Cải trang sự kiện | trùng hệt part với Super Vegeta |

## Đợt 2 (patch 49, 50)

* `48-cai-trang-tu-hunr.sql` — 34 cải trang, id 2080–2113, part 2238–2339.
* `49-cai-trang-pet-tu-hunr.sql` — 6 pet mang về dưới dạng CẢI TRANG (bên mình không có hệ pet riêng),
  id 2114–2119, part 2340–2357: Pet Lôi Thần, King Kong, Cá xanh, Cá cam, Pet Pikachu, Lân Linh Lung.
* `50-giftcode-test-cai-trang-moi.sql` — 5 giftcode `testct1`…`testct5`, mỗi code 8 món, đủ 40 món mới.
* `vsItem` 17, `vsData` 23.

Không mang được thêm:

| Món | Lý do |
|---|---|
| Pet Kẻ Xâm Lăng | part "đầu" bên HUNR khai là loại thân (17 mảnh) |
| Thú cưỡi Dải Ngân Hà, Cầu vòng bay | ảnh mount 34 và 21 bên mình ĐÃ có và đã có vật phẩm dùng, chỉ khác tên |
| Hiệp sĩ, Bunny Baby, Baby Rồng Xanh, Panda Chan, … | thiếu part hoặc thiếu icon bên HUNR |

## Sửa: 6 món HUNR là LINH THÚ, không phải cải trang (patch 51)

Bản đầu của patch 49 để chúng là cải trang (loại 5) nên mặc vào thì nhân vật biến thành con thú.
Bên mình linh thú là vật phẩm **loại 27, đeo ô số 7**; part khai trong `Player.sendNewPet()`,
cột head/body/leg để -1, không dùng `head_avatar`.

| id | Tên | part |
|---|---|---|
| 2114 | Pet Lôi Thần | 2340/2341/2342 |
| 2115 | King Kong | 2343/2344/2345 |
| 2116 | Cá xanh | 2346/2347/2348 |
| 2117 | Cá cam | 2349/2350/2351 |
| 2118 | Pet Pikachu | 2352/2353/2354 |
| 2119 | Lân Linh Lung | 2355/2356/2357 |

Jar cũng sửa lỗi cũ: `sendNewPet()` chỉ chạy lúc đăng nhập nên đeo linh thú giữa phiên phải
thoát ra vào lại mới thấy. Nay đeo vào ô 7 là hiện ngay (`InventoryService.itemBagToBody`).
