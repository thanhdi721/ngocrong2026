# 14 – Bản đồ, khu (zone), dịch chuyển & phó bản

> Nguồn: source Java tại `SRC/src/nro/models/**` và dump DB `database team2026.sql` (bảng `map_template`, `npc_template`, `mob_template`, `item_template`, `item_option_template`).
> Tài liệu chỉ mô tả những gì đọc được trong code/DB. Chỗ nào không xác định được sẽ ghi rõ **"không tìm thấy trong code"**.
> Tài liệu liên quan: `00-tong-quan.md`, `02b-database-du-lieu-template.md`, `12-nhiem-vu-chinh.md` (id nhiệm vụ), `13-boss*.md` (chi tiết boss), `16-su-kien-minigame-giai-dau.md` (Đại hội võ thuật, võ đài…), `17-bang-hoi.md`, `18-npc.md`.

## Mục lục

1. [Kiến trúc package `map/`](#1-kiến-trúc-package-map)
2. [Cơ chế Map / Zone (khu)](#2-cơ-chế-map--zone-khu)
3. [Danh sách map theo hành tinh](#3-danh-sách-map-theo-hành-tinh)
4. [Dịch chuyển: waypoint, capsule, tàu, Yardrat, NPC](#4-dịch-chuyển-waypoint-capsule-tàu-yardrat-npc)
5. [Tổng quan phó bản / sự kiện theo map](#5-tổng-quan-phó-bản--sự-kiện-theo-map)
6. [Doanh Trại Độc Nhãn (RedRibbonHQ)](#6-doanh-trại-độc-nhãn-redribbonhq)
7. [Bản Đồ Kho Báu / Hang kho báu (BanDoKhoBau)](#7-bản-đồ-kho-báu--hang-kho-báu-bandokhobau)
8. [Con Đường Rắn Độc (SnakeWay)](#8-con-đường-rắn-độc-snakeway)
9. [Khí Gas Hủy Diệt / Destron Gas (DestronGas)](#9-khí-gas-hủy-diệt--destron-gas-destrongas)
10. [Ngọc Rồng Sao Đen (BlackBallWar)](#10-ngọc-rồng-sao-đen-blackballwar)
11. [Mabư 12h (MajinBuuService)](#11-mabư-12h-majinbuuservice)
12. [Mabư 14h (MajinBuu14H)](#12-mabư-14h-majinbuu14h)
13. [Ngọc Rồng Namếc (NgocRongNamecService)](#13-ngọc-rồng-namếc-ngocrongnamecservice)
14. [Siêu Thần Thủy – Tây Karin (SuperDivineWaterService)](#14-siêu-thần-thủy--tây-karin-superdivinewaterservice)
15. [Các map/hoạt động đặc biệt khác](#15-các-maphoạt-động-đặc-biệt-khác)
16. [Ghi chú / điểm cần lưu ý](#16-ghi-chú--điểm-cần-lưu-ý)

---

## 1. Kiến trúc package `map/`

| File | Vai trò |
|---|---|
| `map/Map.java` | Một map template đã khởi tạo: danh sách `zones`, `wayPoints`, `npcs`; `initZone()` (số khu theo loại map), `initMob()`, `initNpc()`, `initItem()` (item đặt sẵn), `initTrapMap()` (bẫy), `initBoss()` (boss cố định theo map), `mapIdNextMabu()`. |
| `map/Zone.java` | Một khu của map: danh sách player/boss/pet/NPC không tương tác, `mobs`, `items`, `trapMaps`, `maBuHolds`; `isFullPlayer()`, `addPlayer()`, `removePlayer()`, `update()` (mob/item/player), `mapInfo()`… |
| `map/WayPoint.java` | Điểm chuyển map: `minX,minY,maxX,maxY,isEnter,isOffline,goMap,goX,goY,name`. |
| `map/ItemMap.java` | Vật phẩm nằm trên đất (tự xóa sau 50 s / 30 phút; ngoại lệ: nhà 21–23, item 78, 726, ngọc 14–20 trong Doanh trại). |
| `map/TrapMap.java` | Bẫy trong map (chỉ map 135). |
| `map/MaBuHold.java` | Ô giữ người bị Mabư nuốt (map 128). |
| `map/service/MapService.java` | Tra map/zone, `getMapCanJoin()`, `getZone()`, danh sách capsule, các hàm `isMapXxx()` phân loại map. |
| `map/service/ChangeMapService.java` | Mọi luồng chuyển map/khu: capsule, tàu, Yardrat, waypoint, kiểm tra điều kiện vào map (`checkMapCanJoin`). |
| `map/service/ItemMapService.java`, `NpcManager.java`, `NpcService.java` | Xóa item map, tra NPC theo id/map, tạo menu/tutorial. |
| `map/phoban/*.java` | Instance phó bản: `RedRibbonHQ` (Doanh trại), `BanDoKhoBau`, `SnakeWay` (Con đường rắn độc), `DestronGas` (Khí gas), `BlackBallWar` (NR sao đen), `MajinBuu14H`. |
| `services_dungeon/*.java` | Service mở/tham gia phó bản: `RedRibbonHQService`, `TreasureUnderSeaService`, `SnakeWayService`, `DestronGasService`, `BlackBallWarService`, `MajinBuuService`, `MajinBuu14HService`, `NgocRongNamecService`, `SuperDivineWaterService`, `TrainingService`. |
| `boss_con_duong_ran_doc/` | Boss CDRD: `SAIBAMEN`, `NADIC`, `CADICH`. |

Nạp map: `server/Manager.java` – `loadDatabase()` đọc `select * from map_template` (cột `id, name, zones, max_player, type, planet_id, bg_type, tile_id, bg_id, waypoints, mobs, npcs`), sau đó `initMap()` tạo `nro.models.map.Map` cho từng template, gọi `initMob`, `initNpc`. Boss cố định theo map được tạo ở `server/ServerManager.java` dòng 122: `Manager.MAPS.forEach(Map::initBoss)`.

Cập nhật zone: `Manager.initMap()` lập lịch mỗi **1 giây**, chia zone thành lô 10 zone chạy song song (`zone.update()`). Ngoài ra `Map.run()` có scheduler 5 giây nhưng không thấy chỗ gọi `map.run()` (không tìm thấy trong code).

## 2. Cơ chế Map / Zone (khu)

### 2.1 Loại map (`consts/ConstMap.java`)

| type | Hằng | Ý nghĩa | Số khu thực tế (`Map.initZone`) |
|---|---|---|---|
| 0 | `MAP_NORMAL` | Map thường | = cột `zones` trong DB |
| 1 | `MAP_OFFLINE` | Map riêng (nhà, phòng tập…) | **1** |
| 2 | `MAP_DOANH_TRAI` | Doanh trại | `RedRibbonHQ.AVAILABLE` = **50** |
| 3 | `MAP_BLACK_BALL_WAR` | NR sao đen | `BlackBallWar.AVAILABLE` = **5** |
| 4 | `MAP_BAN_DO_KHO_BAU` | Kho báu dưới biển | `BanDoKhoBau.AVAILABLE` = **50** |
| 5 | `MAP_MA_BU` | Mabư 12h | `MajinBuuService.AVAILABLE` = 13 (nhưng **không map nào trong DB có type 5**) |
| 6 | `MAP_CON_DUONG_RAN_DOC` | Con đường rắn độc | `SnakeWay.AVAILABLE` = **50** |
| 7 | `MAP_KHI_GAS_HUY_DIET` | Khí gas | `DestronGas.AVAILABLE` = **50** |
| 8 | `MAP_TAY_KARIN` | Siêu thần thủy | = cột `zones` (10) |
| 9 | `MAP_MABU_14H` | Mabư 14h | `MajinBuu14H.AVAILABLE` = **7** |

Với phó bản bang (type 2/4/6/7), mỗi zone thứ *i* của các map cùng loại được gom thành **instance thứ i** (vd `RedRibbonHQService.addMapDoanhTrai(i, zone)`), nên tối đa **50 bang** chạy song song mỗi loại.

### 2.2 Số người / khu

- DB: **mọi map** có `zones = 10`, `max_player = 15` (xem bảng mục 3).
- `Zone.isFullPlayer()` = `players.size() >= maxPlayer` (chỉ đếm player thật, không đếm boss/pet/NPC).
- `Zone.PLAYERS_TIEU_CHUAN_TRONG_MAP = 7`: dùng trong `ChangeMapService.getMapCanJoin(player, mapId)` – ưu tiên khu 0 nếu < 7 người, sau đó khu đầu tiên < 7 người, không có thì trả `null`.
- `MapService.getZone(mapId)` (vào khu bất kỳ, `zoneId = -1`): random khu, lặp lại tới khi gặp khu `< maxPlayer`.
- `ChangeMapService.openZoneUI()`: màu khu = 0 nếu < 5 người, 1 nếu < 8, 2 nếu ≥ 8; hiển thị `numPlayers/maxPlayer`. Không cho mở (trừ admin) ở map offline và phó bản.
- `ChangeMapService.changeZone()`: 
  - Cấm đổi khu (trừ admin/boss) ở: map offline, phó bản (`isMapPhoBan`: DT/BDKB/CDRD/KGHD), Mabư 12h (114–120), Mabư 14h (127–128).
  - Hồi chiêu đổi khu **5 000 ms**; khu đầy → "Khu vực này đã đầy".
- Capsule (`UseItem.choseMapCapsule`): từ chối nếu khu đích có **> 25** người.

### 2.3 Hàm phân loại map (`MapService`)

| Hàm | Map id |
|---|---|
| `isMapOffline` | map có type 1 |
| `isHome` | 21–23 |
| `isMapLuyenTap` | 45–50, trừ 47 |
| `isMapDoanhTrai` | 53–62 |
| `isMapNappa` / `isMapStar` | 63–83 |
| `isMapBlackBallWar` | 85–91 |
| `isMapTuongLai(int)` | 92–94, 96–100, 102, 103 |
| `isMapTuongLai(Map)` | 92–103 (bao gồm 95, 101) |
| `isMapCold(int)` | 105–110 |
| `isMapCold(Map)` | 105–110 **và 152** |
| `isMapBossFinal` | 111 |
| `isMapMaBu` | 114–120 |
| `isMapNguHanhSon` | 122–124 |
| `isMapMabu2H` | 127, 128 |
| `isMapYardart` | 131–133 |
| `isMapBanDoKhoBau` | 135–138 |
| `isMapConDuongRanDoc` | 141–144 |
| `isMapSieuThanhThuy` | 146 |
| `isMapKhiGasHuyDiet` | 147–152, trừ 150 |
| `isMapNgucTu` | 155 |
| `isMapUpPorata` | 156–159 |
| `isMapHanhTinhThucVat` | 160–163 |
| `isMapRiengTu` | 164 |
| `isMapCadic` | ≥ 165 |
| `isMapHuyDiet` | 169–171 (không tồn tại trong DB) |
| `isMapPhoBan` | BDKB ∪ DT ∪ CDRD ∪ KGHD |
| `isMap3Planets` | ≤ 38, trừ 0,7,14,21–26 |
| `isMapUpSKH` | 1,2,3,8,9,11,15,16,17 |

### 2.4 Hiệu ứng khi vào map (`ChangeMapService.changeMap` riêng tư)

- Vào/rời **hành tinh Cold** (`isMapCold(Map)`): thông báo; `NPoint` giảm **50% HP** (và sức đánh) nếu không có `isKhongLanh`.
- Vào **Tương lai**: thông báo "khi chết ở đây bạn sẽ bị giảm sức mạnh và tiềm năng" (`Player.isDie()` giảm 1% SM một lần; `Player` dòng ~1260 trừ 0,1% SM mỗi lần chết ở Tương lai/Cold).
- Vào tầng Mabư 12h (khác 114): nhận item **521 "Tự động luyện tập"** thời hạn `param*5` phút (mục 11).
- Vào map 85–91: gắn cờ NRSĐ (`BlackBallWarService.joinMapBlackBallWar`). Vào 114–120 (trừ 116): gắn cờ 9/10 (`MajinBuuService.joinMapMabu`).
- Đăng nhập (`database/MrBlue.java` ~dòng 274): nếu map lưu là 51, Doanh trại, NRSĐ, 146, 127/128 → đưa về nhà (21+gender, x=300,y=336); map Mabư 12h ngoài giờ mở → về nhà. Lúc lưu (`PlayerDAO` ~dòng 380): nếu đang chết, ở DT/NRSĐ, hoặc không qua `checkMapCanJoin` → lưu vị trí nhà.

## 3. Danh sách map theo hành tinh

Nguồn: bảng `map_template` (178 dòng; không có id 150, 167–182). Cột "Số khu DB → thực tế" áp quy tắc `Map.initZone` ở mục 2.1. Cột "Điều kiện vào" tổng hợp từ `ChangeMapService.checkMapCanJoin()` (kiểm tra `TaskService.getIdTask(player) < ConstTask.TASK_X_0`; `TASK_X_0 = X << 11`, tức nhiệm vụ chính số X, bước 0 – xem `12-nhiem-vu-chinh.md`) và các NPC/item mở map. Admin, pet, boss bỏ qua mọi điều kiện. "—" = không có điều kiện trong code.

> Lưu ý: `planet_id` trong DB không phải lúc nào cũng khớp cốt truyện (vd Doanh trại 53 = Xayda, Tương lai/NR sao đen = Xayda, CDRD 141–143 = Trái Đất). Bảng giữ nguyên giá trị DB.

#### planet_id = 0 (Trái Đất) — 51 map

| ID | Tên map | Nhóm | Loại (type) | Số khu DB → thực tế | Max người/khu | Điều kiện vào (code) | NPC (DB) | Quái (DB) |
|---|---|---|---|---|---|---|---|---|
| 0 | Làng Aru | Thường | 0 thường | 10 → **10** | 15 | — | Bunma, Mr Popo, Tori-Bot, Xe nước mía | Mộc nhân |
| 1 | Đồi hoa cúc | Thường | 0 thường | 10 → **10** | 15 | NV ≥ 1 | Khu vực | Khủng long |
| 2 | Thung lũng tre | Thường | 0 thường | 10 → **10** | 15 | NV ≥ 3 | Khu vực | Khủng long, Khủng long mẹ |
| 3 | Rừng nấm | Thường | 0 thường | 10 → **10** | 15 | NV ≥ 7 | Khu vực | Khủng long, Khủng long mẹ, Thằn lằn bay |
| 4 | Rừng xương | Thường | 0 thường | 10 → **10** | 15 | — | Khu vực | Khủng long mẹ, Thằn lằn bay, Thằn lằn mẹ |
| 5 | Đảo Kamê | Thường | 0 thường | 10 → **10** | 15 | — | Santa, Quy Lão Kame, Bà Hạt Mít, Chi Chi | Ốc mượn hồn |
| 6 | Đông Karin | Thường | 0 thường | 10 → **10** | 15 | NV ≥ 16 | Khu vực | Heo rừng mẹ, Tambourine |
| 21 | Nhà Gôhan | Nhà (chỉ đúng hành tinh) | 1 offline | 10 → **1** | 15 | gender=0 | Đậu thần, Rương đồ, Ông Gôhan, Quả trứng, Dưa hấu, Bò Mộng | — |
| 24 | Trạm tàu vũ trụ | Trạm tàu vũ trụ | 0 thường | 10 → **10** | 15 | NV ≥ 4 | Khu vực, Dr. Brief, Uron, Rồng Omega, Jaco | — |
| 27 | Rừng Bamboo | Thường | 0 thường | 10 → **10** | 15 | NV ≥ 13 | Lính canh, Ca Lích | Heo rừng, Thằn lằn mẹ |
| 28 | Rừng dương xỉ | Thường | 0 thường | 10 → **10** | 15 | NV ≥ 13 | Khu vực | Heo rừng, Thằn lằn mẹ |
| 29 | Nam Kamê | Thường | 0 thường | 10 → **10** | 15 | — | Khu vực | Không tặc, Ốc mượn hồn |
| 30 | Đảo Bulông | Thường | 0 thường | 10 → **10** | 15 | NV ≥ 15 | Khu vực | Bulon, Không tặc |
| 39 | Vách núi Aru | Map offline/placeholder | 1 offline | 10 → **1** | 15 | — | — | — |
| 42 | Vách núi Aru | Vách núi (Máy đo sức mạnh) | 0 thường | 10 → **10** | 15 | NV ≥ 2 | Bà Hạt Mít, Ghi danh | Máy đo sức mạnh |
| 45 | Thần điện | Luyện tập | 1 offline | 10 → **1** | 15 | — | Thượng Đế | — |
| 46 | Tháp Karin | Luyện tập | 1 offline | 10 → **1** | 15 | — | Thần mèo Karin | — |
| 47 | Rừng Karin | Rừng Karin | 0 thường | 10 → **10** | 15 | — | Bò Mộng | — |
| 48 | Hành tinh Kaio | Luyện tập | 1 offline | 10 → **1** | 15 | — | Bill, Whis, Thần Vũ Trụ | — |
| 49 | Phòng tập thời gian | Luyện tập | 1 offline | 10 → **1** | 15 | — | — | — |
| 50 | Thánh địa Kaio | Luyện tập | 1 offline | 10 → **1** | 15 | — | Ôsin, Kibit, Tổ Sư Kaio | — |
| 54 | Tầng 3 | Phó bản Doanh trại | 2 doanh trại | 10 → **50** | 15 | — | — | — |
| 55 | Tầng 1 | Phó bản Doanh trại | 2 doanh trại | 10 → **50** | 15 | — | — | Lính độc nhãn |
| 56 | Tầng 2 | Phó bản Doanh trại | 2 doanh trại | 10 → **50** | 15 | — | — | Lính độc nhãn, Robot thép |
| 57 | Tầng 4 | Phó bản Doanh trại | 2 doanh trại | 10 → **50** | 15 | — | Độc Nhãn | Robot thép |
| 58 | Tường thành 2 | Phó bản Doanh trại | 2 doanh trại | 10 → **50** | 15 | — | — | Không tặc, Lính độc nhãn |
| 59 | Tường thành 3 | Phó bản Doanh trại | 2 doanh trại | 10 → **50** | 15 | — | — | Bulon, Không tặc, Lính độc nhãn |
| 60 | Trại độc nhãn 1 | Phó bản Doanh trại | 2 doanh trại | 10 → **50** | 15 | — | — | Lính độc nhãn, Sói xám |
| 61 | Trại độc nhãn 2 | Phó bản Doanh trại | 2 doanh trại | 10 → **50** | 15 | — | — | Robot bay, Sói xám |
| 62 | Trại độc nhãn 3 | Phó bản Doanh trại | 2 doanh trại | 10 → **50** | 15 | — | — | Lính độc nhãn, Robot bay, Sói xám |
| 78 | Lãnh địa Fize | Khu Fide/Nappa | 0 thường | 10 → **10** | 15 | — | Bunma, Khu vực, Thiên Sứ Whis, Đường Tăng, Thượng Đế, Thần Vũ Trụ | Mộc nhân |
| 84 | Siêu Thị | Siêu thị | 0 thường | 10 → **10** | 15 | — | Bò Mộng, Bunma, Bà Hạt Mít, Uron, Appule, Dende, Dr. Brief, Cửa hàng ký gửi | — |
| 104 | Sân sau siêu thị | Sân sau siêu thị | 0 thường | 10 → **10** | 15 | — | — | — |
| 116 | Thánh địa Kaio | Mabư 12h (nằm trong dải 114–120, map offline) | 1 offline | 10 → **1** | 15 | — | Ôsin, Kibit, Tổ Sư Kaio | — |
| 128 | Bụng Mabư | Mabư 14h | 9 Mabu 14h | 10 → **7** | 15 | — | — | — |
| 135 | Động hải tặc | Phó bản BDKB | 4 BDKB | 10 → **50** | 15 | SM ≥ 2 tỷ + item 611 (Quy Lão) | — | Lính độc nhãn, Sói xám |
| 136 | Hang Bạch Tuộc | Phó bản BDKB | 4 BDKB | 10 → **50** | 15 | — | — | Robot bay, Sói xám, Vua Bạch Tuộc |
| 137 | Động kho báu | Phó bản BDKB | 4 BDKB | 10 → **50** | 15 | — | — | Robot bay, Robot thép |
| 138 | Cảng hải tặc | Phó bản BDKB | 4 BDKB | 10 → **50** | 15 | — | — | Robot bay, Rôbốt bảo vệ |
| 141 | Con đường rắn độc | Phó bản CDRD | 6 CDRD | 10 → **50** | 15 | — | Thượng Đế | Quỷ mập, Quỷ địa ngục |
| 142 | Con đường rắn độc | Phó bản CDRD | 6 CDRD | 10 → **50** | 15 | — | — | Drum, Tambourine |
| 143 | Con đường rắn độc | Phó bản CDRD | 6 CDRD | 10 → **50** | 15 | Thần Vũ Trụ, vào bang ≥2 ngày | — | Dơi da xanh, Quỷ chim |
| 146 | Tây Karin | Siêu thần thủy | 8 Tây Karin | 10 → **10** | 15 | Item 726 Mảnh giấy chữ MA | — | Drum, Quỷ đất mẹ, Tambourine |
| 147 | Sa mạc | Phó bản Khí gas | 7 KGHD | 10 → **50** | 15 | — | — | Arbee, Cỗ máy hủy diệt, Kawazu |
| 153 | Lãnh địa Bang Hội | Lãnh địa bang hội | 0 thường | 10 → **10** | 15 | Có bang (Quy Lão Kame) | Giu-ma Đầu Bò | — |
| 154 | Hành tinh Bill | Hành tinh Bill / ngục tù | 0 thường | 10 → **10** | 15 | NV ≥ 27 | Ôsin, Bill, Whis | — |
| 155 | Hành tinh ngục tù | Hành tinh Bill / ngục tù | 0 thường | 10 → **10** | 15 | NPC Ôsin (map 154) | Ôsin | Khỉ lông xanh, Taburine Đỏ |
| 164 | Map riêng tư | Map riêng tư | 0 thường | 10 → **10** | 15 | Item 1787 Vé riêng tư | Khu vực | Khủng long, Khủng long mẹ, Thằn lằn bay |
| 183 | Thành cổ 1 | Thành cổ | 0 thường | 10 → **10** | 15 | — | Hùng Vương | Heo Xayda mẹ, Khỉ lông đen |
| 184 | Thành cổ 2 | Thành cổ | 0 thường | 10 → **10** | 15 | — | Hùng Vương | Khỉ lông vàng, Khỉ lông đỏ, Quỷ chim |
| 185 | Đấu trường thành cổ | Thành cổ | 0 thường | 10 → **10** | 15 | — | Hùng Vương | — |

#### planet_id = 1 (Namếc) — 21 map

| ID | Tên map | Nhóm | Loại (type) | Số khu DB → thực tế | Max người/khu | Điều kiện vào (code) | NPC (DB) | Quái (DB) |
|---|---|---|---|---|---|---|---|---|
| 7 | Làng Mori | Thường | 0 thường | 10 → **10** | 15 | — | Khu vực, Dende, Tori-Bot, Xe nước mía | Mộc nhân |
| 8 | Đồi nấm tím | Thường | 0 thường | 10 → **10** | 15 | NV ≥ 1 | Khu vực | Lợn lòi |
| 9 | Thị trấn Moori | Thường | 0 thường | 10 → **10** | 15 | NV ≥ 3 | Khu vực | Lợn lòi, Lợn lòi mẹ |
| 10 | Thung lũng Namếc | Thường | 0 thường | 10 → **10** | 15 | NV ≥ 16 | Khu vực | Drum, Heo xanh mẹ |
| 11 | Thung lũng Maima | Thường | 0 thường | 10 → **10** | 15 | NV ≥ 7 | Khu vực | Lợn lòi, Lợn lòi mẹ, Phi long |
| 12 | Vực maima | Thường | 0 thường | 10 → **10** | 15 | — | Khu vực | Lợn lòi mẹ, Phi long, Phi long mẹ |
| 13 | Đảo Guru | Thường | 0 thường | 10 → **10** | 15 | — | Trọng tài, Santa, Trưởng lão Guru | Ốc sên |
| 22 | Nhà Moori | Nhà (chỉ đúng hành tinh) | 1 offline | 10 → **1** | 15 | gender=1 | Đậu thần, Ông Moori, Rương đồ, Quả trứng, Dưa hấu, Bò Mộng | — |
| 25 | Trạm tàu vũ trụ | Trạm tàu vũ trụ | 0 thường | 10 → **10** | 15 | NV ≥ 4 | Khu vực, Uron, Cargo, Rồng Omega | — |
| 31 | Núi hoa vàng | Thường | 0 thường | 10 → **10** | 15 | NV ≥ 13 | Khu vực | Heo da xanh, Phi long mẹ |
| 32 | Núi hoa tím | Thường | 0 thường | 10 → **10** | 15 | NV ≥ 13 | Khu vực | Heo da xanh, Phi long mẹ |
| 33 | Nam Guru | Thường | 0 thường | 10 → **10** | 15 | — | Khu vực | Quỷ đầu to, Ốc sên |
| 34 | Đông Nam Guru | Thường | 0 thường | 10 → **10** | 15 | NV ≥ 15 | Khu vực | Quỷ đầu to, Ukulele |
| 40 | Vách núi Moori | Map offline/placeholder | 1 offline | 10 → **1** | 15 | — | — | — |
| 43 | Vách núi Moori | Vách núi (Máy đo sức mạnh) | 0 thường | 10 → **10** | 15 | NV ≥ 2 | Bà Hạt Mít, Quốc Vương, Ghi danh | Máy đo sức mạnh |
| 95 | 95 | Map offline/placeholder | 1 offline | 10 → **1** | 15 | — | — | — |
| 101 | 101 | Map offline/placeholder | 1 offline | 10 → **1** | 15 | — | — | — |
| 121 | 121 | Map offline/placeholder | 1 offline | 10 → **1** | 15 | — | — | — |
| 125 | 125 | Map offline/placeholder | 1 offline | 10 → **1** | 15 | — | — | — |
| 130 | 130 | Map offline/placeholder | 1 offline | 10 → **1** | 15 | — | — | — |
| 134 | 134 | Map offline/placeholder | 1 offline | 10 → **1** | 15 | — | — | — |

#### planet_id = 2 (Xayda) — 97 map

| ID | Tên map | Nhóm | Loại (type) | Số khu DB → thực tế | Max người/khu | Điều kiện vào (code) | NPC (DB) | Quái (DB) |
|---|---|---|---|---|---|---|---|---|
| 14 | Làng Kakarot | Thường | 0 thường | 10 → **10** | 15 | — | Appule, Khu vực, Tori-Bot, Xe nước mía | Mộc nhân |
| 15 | Đồi hoang | Thường | 0 thường | 10 → **10** | 15 | NV ≥ 1 | Khu vực | Quỷ đất |
| 16 | Làng Plant | Thường | 0 thường | 10 → **10** | 15 | NV ≥ 3 | Khu vực | Quỷ đất, Quỷ đất mẹ |
| 17 | Rừng nguyên sinh | Thường | 0 thường | 10 → **10** | 15 | NV ≥ 7 | Khu vực | Quỷ bay, Quỷ đất, Quỷ đất mẹ |
| 18 | Rừng thông Xayda | Thường | 0 thường | 10 → **10** | 15 | — | Khu vực | Quỷ bay, Quỷ bay mẹ, Quỷ đất mẹ |
| 19 | Thành phố Vegeta | Thường | 0 thường | 10 → **10** | 15 | NV ≥ 16 | Cui, Tapion | Akkuman, Alien |
| 20 | Vách núi đen | Thường | 0 thường | 10 → **10** | 15 | — | Santa, Khu vực, Vua Vegeta | Heo Xayda mẹ |
| 23 | Nhà Broly | Nhà (chỉ đúng hành tinh) | 1 offline | 10 → **1** | 15 | gender=2 | Rương đồ, Ông Paragus, Đậu thần, Quả trứng, Dưa hấu, Bò Mộng | — |
| 26 | Trạm tàu vũ trụ | Trạm tàu vũ trụ | 0 thường | 10 → **10** | 15 | NV ≥ 4 | Cui, Khu vực, Uron, Rồng Omega | — |
| 35 | Rừng cọ | Thường | 0 thường | 10 → **10** | 15 | NV ≥ 13 | Khu vực | Heo Xayda, Quỷ bay mẹ |
| 36 | Rừng đá | Thường | 0 thường | 10 → **10** | 15 | NV ≥ 13 | Khu vực | Heo Xayda, Quỷ bay mẹ |
| 37 | Thung lũng đen | Thường | 0 thường | 10 → **10** | 15 | — | Khu vực | Heo Xayda mẹ, Quỷ địa ngục |
| 38 | Bờ vực đen | Thường | 0 thường | 10 → **10** | 15 | NV ≥ 15 | Khu vực | Quỷ mập, Quỷ địa ngục |
| 41 | Vực Plant | Map offline/placeholder | 1 offline | 10 → **1** | 15 | — | — | — |
| 44 | Vách núi Kakarot | Vách núi (Máy đo sức mạnh) | 0 thường | 10 → **10** | 15 | NV ≥ 2 | Bà Hạt Mít, Ghi danh | Máy đo sức mạnh |
| 51 | Đấu trường | Đấu trường | 0 thường | 10 → **10** | 15 | — | — | — |
| 52 | Đại hội võ thuật | Đại hội võ thuật | 0 thường | 10 → **10** | 15 | — | Ôsin, Kibit, Ghi danh | Mộc nhân |
| 53 | Tường thành 1 | Phó bản Doanh trại | 2 doanh trại | 10 → **50** | 15 | Bang ≥5 TV (Lính canh) | — | Lính độc nhãn |
| 63 | Trại lính Fide | Khu Fide/Nappa | 0 thường | 10 → **10** | 15 | NV ≥ 19 | — | Dơi da xanh, Quỷ da tím, Quỷ đầu vàng |
| 64 | Núi dây leo | Khu Fide/Nappa | 0 thường | 10 → **10** | 15 | NV ≥ 18 | — | Quỷ đầu nhọn, Thằn lằn xanh |
| 65 | Núi cây quỷ | Khu Fide/Nappa | 0 thường | 10 → **10** | 15 | NV ≥ 18 | — | Quỷ đầu nhọn, Quỷ đầu vàng, Thằn lằn xanh |
| 66 | Trại qủy già | Khu Fide/Nappa | 0 thường | 10 → **10** | 15 | NV ≥ 19 | — | Quỷ da tím, Quỷ già |
| 67 | Vực chết | Khu Fide/Nappa | 0 thường | 10 → **10** | 15 | NV ≥ 19 | — | Dơi da xanh, Quỷ già |
| 68 | Thung lũng Nappa | Khu Fide/Nappa | 0 thường | 10 → **10** | 15 | NV ≥ 18 | Cui | Nappa, Tambourine |
| 69 | Vực cấm | Khu Fide/Nappa | 0 thường | 10 → **10** | 15 | NV ≥ 18 | — | Nappa, Soldier, Tambourine |
| 70 | Núi Appule | Khu Fide/Nappa | 0 thường | 10 → **10** | 15 | NV ≥ 18 | — | Appule, Nappa, Soldier |
| 71 | Căn cứ Raspberry | Khu Fide/Nappa | 0 thường | 10 → **10** | 15 | NV ≥ 18 | — | Appule, Raspberry |
| 72 | Thung lũng Raspberry | Khu Fide/Nappa | 0 thường | 10 → **10** | 15 | NV ≥ 18 | — | Appule, Raspberry, Thằn lằn xanh |
| 73 | Thung lũng chết | Khu Fide/Nappa | 0 thường | 10 → **10** | 15 | NV ≥ 19 | — | Cá sấu, Dơi da xanh |
| 74 | Đồi cây Fide | Khu Fide/Nappa | 0 thường | 10 → **10** | 15 | NV ≥ 19 | — | Dơi da xanh, Lính tai dài, Lính đầu trọc |
| 75 | Khe núi tử thần | Khu Fide/Nappa | 0 thường | 10 → **10** | 15 | NV ≥ 19 | — | Lính tai dài, Lính vũ trụ, Lính đầu trọc |
| 76 | Núi đá | Khu Fide/Nappa | 0 thường | 10 → **10** | 15 | NV ≥ 19 | — | Lính tai dài, Lính vũ trụ, Quỷ chim |
| 77 | Rừng đá | Khu Fide/Nappa | 0 thường | 10 → **10** | 15 | NV ≥ 19 | — | Lính vũ trụ, Quỷ chim |
| 79 | Núi khỉ đỏ | Khu Fide/Nappa | 0 thường | 10 → **10** | 15 | NV ≥ 19 | — | Khỉ lông đỏ |
| 80 | Núi khỉ vàng | Khu Fide/Nappa | 0 thường | 10 → **10** | 15 | NV ≥ 20 (thực tế ≥ 21, thiếu `break`) | Goku SSJ | Khỉ lông vàng |
| 81 | Hang quỷ chim | Khu Fide/Nappa | 0 thường | 10 → **10** | 15 | NV ≥ 19 | — | Khỉ lông đen, Lính vũ trụ, Quỷ chim |
| 82 | Núi khỉ đen | Khu Fide/Nappa | 0 thường | 10 → **10** | 15 | NV ≥ 19 | — | Khỉ giáp sắt, Khỉ lông đen, Quỷ chim |
| 83 | Hang khỉ đen | Khu Fide/Nappa | 0 thường | 10 → **10** | 15 | NV ≥ 19 | — | Khỉ giáp sắt, Khỉ lông đỏ |
| 85 | Hành tinh M-2 | NR Sao Đen | 3 NRSĐ | 10 → **5** | 15 | 20h–21h (Rồng Omega) | Rồng 1 sao | Khỉ lông vàng, Quỷ chim |
| 86 | Hành tinh Polaris | NR Sao Đen | 3 NRSĐ | 10 → **5** | 15 | — | Rồng 2 sao | Quỷ chim |
| 87 | Hành tinh Cretaceous | NR Sao Đen | 3 NRSĐ | 10 → **5** | 15 | — | Rồng 3 sao | Khỉ lông vàng, Khỉ lông đỏ, Quỷ chim |
| 88 | Hành tinh Monmaasu | NR Sao Đen | 3 NRSĐ | 10 → **5** | 15 | — | Rồng 4 sao | Khỉ lông đỏ, Quỷ chim |
| 89 | Hành tinh Rudeeze | NR Sao Đen | 3 NRSĐ | 10 → **5** | 15 | — | Rồng 5 sao | Khỉ lông vàng |
| 90 | Hành tinh Gelbo | NR Sao Đen | 3 NRSĐ | 10 → **5** | 15 | — | Rồng 6 sao | Khỉ lông vàng, Quỷ chim |
| 91 | Hành tinh Tigere | NR Sao Đen | 3 NRSĐ | 10 → **5** | 15 | — | Rồng 7 sao | Khỉ lông vàng, Khỉ lông đỏ |
| 92 | Thành phố phía đông | Tương lai | 0 thường | 10 → **10** | 15 | NV ≥ 21 | — | Xên con cấp 1 |
| 93 | Thành phố phía nam | Tương lai | 0 thường | 10 → **10** | 15 | NV ≥ 21 | — | Xên con cấp 2 |
| 94 | Đảo Balê | Tương lai | 0 thường | 10 → **10** | 15 | NV ≥ 21 | — | Xên con cấp 3 |
| 96 | Cao nguyên | Tương lai | 0 thường | 10 → **10** | 15 | NV ≥ 21 | — | Xên con cấp  4 |
| 97 | Thành phố phía bắc | Tương lai | 0 thường | 10 → **10** | 15 | NV ≥ 24 | — | Xên con cấp  5 |
| 98 | Ngọn núi phía bắc | Tương lai | 0 thường | 10 → **10** | 15 | NV ≥ 24 | — | Xên con cấp  6 |
| 99 | Thung lũng phía bắc | Tương lai | 0 thường | 10 → **10** | 15 | NV ≥ 24 | — | Xên con cấp  7 |
| 100 | Thị trấn Ginder | Tương lai | 0 thường | 10 → **10** | 15 | NV ≥ 24 | — | Xên con cấp  8 |
| 102 | Nhà Bunma | Tương lai | 0 thường | 10 → **10** | 15 | NV ≥ 21; Nhiệm vụ ≥ 21 hoặc Calích (NV ≥ 20) | Bunma, Ca Lích, Rương Sưu Tầm | — |
| 103 | Võ đài Xên bọ hung | Tương lai | 0 thường | 10 → **10** | 15 | NV ≥ 27 | — | — |
| 105 | Cánh đồng tuyết | Hành tinh Cold (lạnh) | 0 thường | 10 → **10** | 15 | NV ≥ 27 | — | Tai tím |
| 106 | Rừng tuyết | Hành tinh Cold (lạnh) | 0 thường | 10 → **10** | 15 | NV ≥ 27 | — | Abo, Tai tím |
| 107 | Núi tuyết | Hành tinh Cold (lạnh) | 0 thường | 10 → **10** | 15 | NV ≥ 27 | — | Abo |
| 108 | Dòng sông băng | Hành tinh Cold (lạnh) | 0 thường | 10 → **10** | 15 | NV ≥ 27 | — | Abo, Kado |
| 109 | Rừng băng | Hành tinh Cold (lạnh) | 0 thường | 10 → **10** | 15 | NV ≥ 27 | — | Da xanh, Kado |
| 110 | Hang băng | Hành tinh Cold (lạnh) | 0 thường | 10 → **10** | 15 | NV ≥ 27 | — | Da xanh, Kado |
| 111 | Đông Nam Karin | Tàu Pảy Pảy | 0 thường | 10 → **10** | 15 | SM ≤ 1,5 triệu | — | — |
| 112 | Võ đài Hạt Mít | Võ đài Hạt Mít | 0 thường | 10 → **10** | 15 | — | Bà Hạt Mít | — |
| 113 | Đại hội võ thuật | Đại hội võ thuật | 0 thường | 10 → **10** | 15 | — | Trọng tài | — |
| 114 | Cổng phi thuyền | Mabư 12h | 0 thường | 10 → **10** | 15 | 12h–13h (Ôsin map 52) | Ôsin, Kibit, Babiđây | — |
| 115 | Phòng chờ | Mabư 12h | 0 thường | 10 → **10** | 15 | — | Ôsin, Babiđây | — |
| 117 | Cửa Ải 1 | Mabư 12h | 0 thường | 10 → **10** | 15 | — | Ôsin, Babiđây | — |
| 118 | Cửa Ải 2 | Mabư 12h | 0 thường | 10 → **10** | 15 | — | Ôsin, Babiđây | — |
| 119 | Cửa Ải 3 | Mabư 12h | 0 thường | 10 → **10** | 15 | — | Ôsin, Babiđây | — |
| 120 | Phòng chỉ huy | Mabư 12h | 0 thường | 10 → **10** | 15 | — | Ôsin, Babiđây | — |
| 122 | Ngũ Hành Sơn | Ngũ Hành Sơn | 1 offline | 10 → **1** | 15 | — | — | Khỉ lông vàng, Quỷ chim |
| 123 | Ngũ Hành Sơn | Ngũ Hành Sơn | 0 thường | 10 → **10** | 15 | — | — | Khỉ lông đỏ |
| 124 | Ngũ Hành Sơn | Ngũ Hành Sơn | 0 thường | 10 → **10** | 15 | — | — | Khỉ lông vàng |
| 126 | Thành phố Santa | Hirudegarn | 0 thường | 10 → **10** | 15 | — | Tapion | Hirudegarn, Quỷ chim |
| 127 | Cổng phi thuyền | Mabư 14h | 9 Mabu 14h | 10 → **7** | 15 | 14h–15h (Ôsin map 52) | Ôsin | — |
| 129 | Đại hội võ thuật | Đại hội võ thuật | 0 thường | 10 → **10** | 15 | — | Ghi danh | — |
| 131 | Hành Tinh Yardart | Yardrat | 0 thường | 10 → **10** | 15 | NPC Goku SSJ (map 80) | Goku SSJ | — |
| 132 | Hành Tinh Yardart 2 | Yardrat | 0 thường | 10 → **10** | 15 | — | — | — |
| 133 | Hành Tinh Yardart 3 | Yardrat | 0 thường | 10 → **10** | 15 | — | Goku SSJ | — |
| 139 | Hành tinh Potaufeu | Potaufeu | 0 thường | 10 → **10** | 15 | NPC Jaco (map 24) | Jaco | — |
| 140 | Hang động Potaufeu | Potaufeu | 0 thường | 10 → **10** | 15 | — | Potage | — |
| 144 | Hoang mạc | Phó bản CDRD | 6 CDRD | 10 → **50** | 15 | — | — | — |
| 145 | Võ Đài Siêu Cấp | Võ đài Siêu Cấp | 0 thường | 10 → **10** | 15 | — | Thiên Sứ Whis | — |
| 148 | Lâu đài Lychee | Phó bản Khí gas | 7 KGHD | 10 → **50** | 15 | — | — | Arbee, Kawazu, Kinkarn |
| 149 | Thành phố Santa | Phó bản Khí gas | 7 KGHD | 10 → **50** | 15 | Mr.PôPô, bang chủ, vào bang ≥2 ngày | — | Arbee, Cỗ máy hủy diệt, Kinkarn |
| 151 | Hành tinh bóng tối | Phó bản Khí gas | 7 KGHD | 10 → **50** | 15 | — | — | Arbee, Cỗ máy hủy diệt, Kawazu, Kinkarn |
| 152 | Vùng đất băng giá | Phó bản Khí gas | 7 KGHD | 10 → **50** | 15 | — | — | Arbee, Cỗ máy hủy diệt, Kawazu, Kinkarn |
| 156 | Tây thánh địa | Thánh địa | 0 thường | 10 → **10** | 15 | SM ≥ 40 tỷ (NPC Giu-ma Đầu Bò) | — | Dơi da xanh, Nappa |
| 157 | Đông thánh Địa | Thánh địa | 0 thường | 10 → **10** | 15 | — | — | Quỷ chim, Soldier, Thằn lằn xanh |
| 158 | Bắc thánh địa | Thánh địa | 0 thường | 10 → **10** | 15 | — | — | Abo, Da xanh, Tai tím |
| 159 | Nam thánh Địa | Thánh địa | 0 thường | 10 → **10** | 15 | — | — | Da xanh, Kado |
| 160 | Khu hang động | Hành tinh thực vật | 0 thường | 10 → **10** | 15 | Item 992 Nhẫn thời không sai lệch | Bardock, Berry | Cabira, Tobi |
| 161 | Bìa rừng nguyên thủy | Hành tinh thực vật | 0 thường | 10 → **10** | 15 | — | — | Cabira, Tobi |
| 162 | Rừng nguyên thủy | Hành tinh thực vật | 0 thường | 10 → **10** | 15 | — | — | Cabira, Tobi |
| 163 | Làng Plant nguyên thủy | Hành tinh thực vật | 0 thường | 10 → **10** | 15 | — | — | Cabira, Tobi |
| 165 | Sa mạc hoang vu | Cadic (bình hút năng lượng) | 9 Mabu 14h | 10 → **7** | 15 | NPC Ôsin (map 52) menu Bình hút năng lượng | Ôsin | Cadic M |
| 166 | Phòng thí nghiệm Myuu | Phòng thí nghiệm Myuu | 0 thường | 10 → **10** | 15 | — | — | — |

## 4. Dịch chuyển: waypoint, capsule, tàu, Yardrat, NPC

### 4.1 Các kiểu tàu (`ChangeMapService`)

| Hằng | Giá trị | Ghi chú |
|---|---|---|
| `AUTO_SPACE_SHIP` | -1 | Dùng tàu thường (1) hoặc tàu Tennis (3) nếu `player.haveTennisSpaceShip` (item **453** – `InventoryService` dòng 724). |
| `NON_SPACE_SHIP` | 0 | Đi bộ/waypoint. |
| `DEFAULT_SPACE_SHIP` | 1 | Tàu thường. |
| `TELEPORT_YARDRAT` | 2 | Dịch chuyển tức thời – qua `checkMapCanJoinByYardart` (cấm 122–124 với người thường). |
| `TENNIS_SPACE_SHIP` | 3 | Tàu Tennis. |

`changeMapBySpaceShip(...)`: nếu đang chết → hồi sinh (full HP/MP nếu có tàu Tennis, ngược lại 1 HP/1 MP); nếu còn sống và có tàu Tennis → hồi full HP/MP.

### 4.2 Luồng `changeMap` chung (`ChangeMapService.changeMap(Player, Zone, int, int, int, int, byte)`)

1. Đang giữ **Ngọc rồng Namếc** (`idNRNM != -1`) và chưa qua 30 s từ lúc nhặt → chặn "Không thể chuyển map quá nhanh…". Nếu map đích không thuộc vùng NR Namếc → rơi ngọc.
2. Hủy giao dịch.
3. `MapService.getMapCanJoin()`: map offline → khu 0; phó bản bang khi không có instance → đẩy về nhà (DT), map 5 (BDKB/KGHD) hoặc map 48 (CDRD); trong phó bản chỉ cho qua map kế khi **đã hạ hết quái + boss** ở zone hiện tại (hoặc zone đích đã sạch).
4. Nếu Yardrat → `checkMapCanJoinByYardart`.
5. `checkMapCanJoin()` (điều kiện nhiệm vụ + map nhà theo giới tính + map 111).
6. Map **111**: chặn nếu `power >= 1 500 000` ("Sức mạnh phải dưới 1,5 triệu mới vào được").
7. Không vào được → đẩy lùi vị trí và "Bạn chưa thể đến khu vực này".

### 4.3 Waypoint (`changeMapWaypoint`)

- Dữ liệu waypoint lấy từ cột `waypoints` của `map_template`.
- Map 45 (Thần điện) / 46 (Tháp Karin): nếu đứng trong vùng x 35–685, y 550–560 → rơi xuống map +1 (46 tại x=420 / 47 tại x=636, y=150).
- Trong Khí gas, chuyển giữa 2 map KGHD (không tính 148) → hiệu ứng 5 s (`player.type=3`), xử lý ở `server/Controller.java` case `-105`.
- CDRD: đang ở map 47 đi waypoint sang map 1, nếu đủ điều kiện CDRD → chuyển sang map 144 (mục 8).
- Không đi được mà đang ở phó bản → "Chưa hạ hết đối thủ".

### 4.4 Capsule

| Item | Tên (item_template) | Xử lý (`services_func/UseItem.java`) |
|---|---|---|
| 193 | Gói 10 viên Capsule | `openCapsuleUI` + trừ 1 viên (**thiếu `break` → rơi xuống case 194, gọi `openCapsuleUI` lần 2**) |
| 194 | Viên Capsule đặc biệt ("dùng không giới hạn số lần") | `openCapsuleUI`, không trừ item |

Danh sách map capsule (`MapService.getMapCapsule`), theo thứ tự, bỏ trùng và bỏ map đang đứng:

| # | Map | Ghi chú |
|---|---|---|
| 0 | `mapBeforeCapsule` ("Về chỗ cũ: …") | chỉ khi map trước đó ≠ 21–23 và không phải Tương lai |
| 1 | 21 + gender (hiển thị "Về nhà") | |
| 2 | 47 Rừng Karin | |
| 3 | 45 Thần điện | |
| 4 | 0 Làng Aru | |
| 5 | 7 Làng Mori | |
| 6 | 14 Làng Kakarot | |
| 7 | 5 Đảo Kamê | |
| 8 | 20 Vách núi đen | |
| 9 | 13 Đảo Guru | |
| 10 | 24 + gender (Trạm tàu vũ trụ) | |
| 11 | 27 Rừng Bamboo | |
| 12 | 19 Thành phố Vegeta | |
| 13 | 79 Núi khỉ đỏ | |
| 14 | 84 Siêu Thị | |
| 15 | 154 Hành tinh Bill | |
| 16 | 52 Đại hội võ thuật | |

Khi chọn (`UseItem.choseMapCapsule`):
- Đang giữ NR Namếc → "Không thể mang ngọc rồng này lên Phi thuyền".
- Map cấm: khu > 25 người, Doanh trại, Mabư 12h, `isMapHuyDiet` (169–171).
- Sau đó vẫn qua `checkMapCanJoin` (điều kiện nhiệm vụ).
- Chọn index 0 "Về chỗ cũ" → vào đúng khu cũ; chọn khác → lưu `mapBeforeCapsule`. Đi bằng `changeMapBySpaceShip`.

### 4.5 Dịch chuyển tức thời Yardrat (tới người chơi)

`services/FriendAndEnemyService.goToPlayerWithYardrat` (message 18 trong `Controller`): cần `nPoint.teleport` (trang bị dịch chuyển) hoặc admin; mục tiêu không dùng Ẩn danh; khu mục tiêu chưa đầy; mục tiêu **không** ở map offline, NR sao đen, phó bản bang, Mabư 12h. Map 122–124 bị chặn bởi `checkMapCanJoinByYardart`.

### 4.6 NPC/Item chuyển map

| NPC / Item | Tại map | Đích | Nguồn |
|---|---|---|---|
| Dr. Brief (10) | 24 | 25, 26, 84 | `npc_list/DrDrief.java` |
| Cargo (11) | 25 | 24, 26, 84 | `npc_list/Cargo.java` |
| Cui (12) | 26 | 24, 25, 84 | `npc_list/Cui.java` |
| Cui (12) | 19 | 109 (x=295), 68 (x=90) | `npc_list/Cui.java` |
| Jaco (63) | 24 | 139 Hành tinh Potaufeu | `Jaco.java`, `ChangeMapService.goToPotaufeu` |
| Jaco (63) | 139 | 24 / 25 / 26 | `Jaco.java` |
| Ca Lích (38) | 27–29 (di chuyển ngẫu nhiên sau 25 lần mở menu) | Tương lai: hiệu ứng 60 s → map 102 (NV ≥ 20) | `Calick.java`, `goToTuongLai`, `Player.update` |
| Ca Lích (38) | 102 | 24 (`goToQuaKhu`) | `Calick.java` |
| Goku SSJ (60) | 80 | 131 Yardrat (x=870) | `GokuSSJ.java` |
| Goku SSJ (60) | 131 | "Về chỗ cũ" → 80 | `GokuSSJ.java` |
| Tapion (53) | 19 ↔ 126 | Thành phố Santa / Vegeta | `Tapion.java` |
| Bà Hạt Mít (21) | (menu) | 112 Võ đài Hạt Mít; về 5 (x=1156) | `BaHatMit.java` |
| Ghi danh (23) | | 52 | `GhiDanh.java` |
| Quy Lão Kame (13) | 5 | 153 Lãnh địa bang (cần bang); hang kho báu 135 | `QuyLaoKame.java` |
| Giu-ma Đầu Bò (47) | 153 | SM ≥ 40 tỷ → hiệu ứng 5 s → 156 | `GiuMaDauBo.java`, `Controller` type 5 |
| Thượng Đế (19) | 45 | 49 Phòng tập (thách đấu) | `TrainingService.callBoss` |
| Thần Vũ Trụ (20) | 48 | 45 (x=354), 50, Con đường rắn độc | `ThanVuTru.java` |
| Kibit (45) | 50 | 48 | `Kibit.java` |
| Ôsin (44) | 50 | 48, 154 | `Osin.java` |
| Ôsin (44) | 154 ↔ 155 | Hành tinh ngục tù / quay về | `Osin.java` |
| Ôsin (44) | 52 | 114 (12h), 127 (14h), 165 (bình hút năng lượng) | `Osin.java` |
| Bill (55) | 154 | 50 | `Bill.java` |
| Whis (56) | 164 | 154 | `Whis.java` |
| Rồng Omega (29) | 24/25/26 | Tab chọn 85–91 (20h–21h) | `RongOmega.java` |
| Rồng 1–7 sao (30–36) | 85–91 | Về nhà | `Rong1Sao.java`… |
| Babiđây (46) / Ôsin | 114–119 | tầng kế / về nhà | `Babiday.java` |
| Item 992 "Nhẫn thời không sai lệch" | bất kỳ | hiệu ứng 5 s → 160; nếu đang ở 160–163 → 80 | `UseItem` case 992, `Controller` type 2 |
| Item 1787 "Vé riêng tư" | bất kỳ | 164 (x=870) – **không trừ vé** | `UseItem.MapRiengTu` |
| Item 726 "Mảnh giấy có chữ MA" | bất kỳ | 146 Tây Karin | `UseItem.ItemManhGiay` |
| Item 361 "Gói 10 Rađa dò ngọc" | bất kỳ | tới NR Namếc ngẫu nhiên | `NgocRongNamecService.teleportToNrNamec` |

## 5. Tổng quan phó bản / sự kiện theo map

| Phó bản | Map | NPC mở | Điều kiện chính | Giờ mở | Giới hạn | Thời lượng |
|---|---|---|---|---|---|---|
| Doanh Trại Độc Nhãn | 53–62 | Lính canh (25) – map 27 | Bang ≥ 5 TV, vào bang ≥ 1 ngày, ≥ 1 đồng đội đứng gần, tài khoản đã kích hoạt | Cả ngày | 1 lần/ngày/bang | 30 phút + 5 phút nhặt ngọc |
| Hang kho báu (BDKB) | 135–138 | Quy Lão Kame (13) – map 5 | SM ≥ 2 tỷ, có bang, item 611 | Cả ngày | 3 lần/ngày/người (xem ghi chú) | 30 phút |
| Con đường rắn độc | 141–144 | Thần Vũ Trụ (20) – map 48 | Có bang, vào bang ≥ 2 ngày, đã kích hoạt | Cả ngày | 1 lần/7 ngày/người | 30 phút |
| Khí gas hủy diệt | 147–152 | Mr.PôPô (67) – map 0 | Bang chủ mở, vào bang ≥ 2 ngày, đã kích hoạt | Cả ngày | 3 lần/ngày/bang | 30 phút |
| Ngọc rồng sao đen | 85–91 | Rồng Omega (29) – map 24/25/26 | — | 20:00–21:00 (nhặt từ 20:30) | — | 60 phút |
| Mabư 12h | 114–120 | Ôsin (44) – map 52 | — | 12:00–12:59 | — | ~1 giờ |
| Mabư 14h | 127–128 | Ôsin (44) – map 52 | — | 14:00–14:59 | — | ~1 giờ |
| Ngọc rồng Namếc | 7–13, 25, 31–34, 43 | Dende (8) – map 7 | Có bang, gom 7 viên | Gọi rồng 8h–22h | Đá hóa 24 giờ sau khi ước | — |
| Siêu thần thủy | 146 | Item 726 / Thần mèo Karin | — | Cả ngày | 1 lần/ngày | 15 phút |
| Nhân bản Commeson | 140 | Potage (62) | — | Cả ngày | 1 lần/ngày | 5 phút |

Múi giờ: `TimeUtil` dùng `Asia/Ho_Chi_Minh` cho Mabư/NRSĐ; `Util.isAfterMidnight` dùng `ZoneId.systemDefault()` (so sánh ngày theo giờ máy chủ).

Rơi đồ chung trong phó bản (`mob/Mob.java` ~dòng 744–790, 1010): mọi map `isMapPhoBan` – 1% rơi vàng 80 000–200 000 (item 190 "Vàng"; ×1,15 nếu dùng Cỏ bốn lá). Tiềm năng trong BDKB ×1,5 (`NPoint` ~dòng 1639). Khiên (shield) trong phó bản chịu tối thiểu 10 sát thương thay vì 1 (`Player` ~dòng 1180). Mob phó bản (`isMapPhoBan`) không tự hồi sinh (trừ Bulon DT), không có cơ chế mob cấp cao phản đòn (`Mob.java` dòng 147).

---

## 6. Doanh Trại Độc Nhãn (RedRibbonHQ)

Nguồn: `map/phoban/RedRibbonHQ.java`, `services_dungeon/RedRibbonHQService.java`, `npc_list/LinhCanh.java`, `npc_list/DocNhan.java`, `boss/doanh_trai/*.java`.

### 6.1 Hằng số

| Hằng | Giá trị |
|---|---|
| `N_PLAYER_CLAN` | 5 (thành viên bang tối thiểu) |
| `N_PLAYER_MAP` | 1 (đồng đội cùng bang đứng gần) |
| `AVAILABLE` | 50 instance |
| `TIME_DOANH_TRAI` | 1 800 000 ms = 30 phút |
| `TIME_PICK_DOANH_TRAI` | 300 000 ms = 5 phút |

### 6.2 Điều kiện vào (`LinhCanh.openBaseMenu`, `RedRibbonHQService.joinDoanhTrai`)

NPC **Lính canh** (id 25) ở **map 27 Rừng Bamboo** (DB).

1. Có bang; tài khoản đã kích hoạt (`session.actived`) – "Vui lòng mở thành viên trước".
2. Bang có ≥ 5 thành viên (`clan.getMembers().size()`).
3. Vào bang ≥ 1 ngày (`clanMember.getNumDateFromJoinTimeToToday() < 1` → từ chối).
4. Nếu bang đang có DT → menu "Tham gia" (hiển thị thời gian còn lại).
5. Nếu chưa mở: cần ≥ 1 thành viên cùng bang (khác mình) đứng trong vùng **x 1285–1645** của map 27.
6. Mỗi bang **1 lần/ngày**: `clan.haveGoneDoanhTrai && !Util.isAfterMidnight(clan.lastTimeOpenDoanhTrai)` → từ chối (thông báo tên người mở + giờ mở).
7. Hết 50 instance → "Doanh trại đã đầy…".

Khi mở (`openDoanhTrai`): các thành viên cùng bang trong vùng x 1285–1645, còn sống, vào bang ≥ 1 ngày được kéo vào cùng; tất cả vào **map 53 (Tường thành 1), x=60**. Người vào sau (đang mở) được `updateHPDame()` lại chỉ số quái/boss rồi vào map 53.

### 6.3 Lộ trình map (waypoint DB)

`53 Tường thành 1 → 58 Tường thành 2 → 59 Tường thành 3 → 60 Trại độc nhãn 1 → 61 Trại độc nhãn 2 → 62 Trại độc nhãn 3 → 55 Tầng 1 → 56 Tầng 2 → 54 Tầng 3 → 57 Tầng 4` (map 53 có waypoint ra 27 Rừng Bamboo). Phải hạ hết quái + boss trong zone mới qua được (`MapService.getMapCanJoin`).

### 6.4 Quái & boss

Quái (DB): 53 Lính độc nhãn; 58 Không tặc, Lính độc nhãn; 59 Bulon, Không tặc, Lính độc nhãn; 60 Lính độc nhãn, Sói xám; 61 Sói xám, Robot bay; 62 Lính độc nhãn ×2 loại, Sói xám, Robot bay; 55 Lính độc nhãn; 56 Lính độc nhãn, Robot thép; 57 Robot thép.

Chỉ số khi mở (`RedRibbonHQ.init`), với `totalHp`, `totalDamage` = tổng `hpMax`, `dame` của **thành viên bang đang online** (`clan.membersInGame`):
- Quái: `dame = totalHp / tempId`, `maxHp = totalDamage × tempId` (giới hạn int).
- Boss: `dame = totalHp/20`, `hp = totalDamage×50`, nhân hệ số, dame tối đa 200 000 000:

| Map | Boss (class) | Hệ số | Rơi đồ (`reward`) |
|---|---|---|---|
| 59 Tường thành 3 | Trung uý Trắng (`TrungUyTrang`) | ×1,0 | 50% item 17 Ngọc Rồng 4 sao; 100% item 1824 Cậu Vàng; +5 điểm sự kiện |
| 62 Trại độc nhãn 3 | Trung uý Xanh Lơ (`TrungUyXanhLo`) | ×1,1 | 50% NR 4 sao (17); **100%** item 611 Bản đồ kho báu ×1–3; 100% 1824 Cậu Vàng |
| 55 Tầng 1 | Trung uý Thép (`TrungUyThep`) | ×1,15 | 50% NR 4 sao; 30% item 611 ×1–2; 100% 1824 |
| 54 Tầng 3 | Ninja Áo Tím (`NinjaAoTim`) | ×1,2 | 50% NR 4 sao; 30% item 611 ×1–2; 100% 1824. Tạo 3 phân thân `NinjaClone` (1/10 dame & HP) – 10% NR 4 sao |
| 57 Tầng 4 | 4 × Rôbốt Vệ Sĩ 0..3 (`RobotVeSi`) | ×1,3 | 30% NR 4 sao; 100% 1824 |

Bulon (mob 22) ở DT hồi sinh sau 10 s nếu Trung úy Trắng còn sống (`Mob.update`, cờ `zone.isTUTAlive`).

Rơi từ quái DT (`Mob.java`): 20% item **1778 Cuốn chả giò** (×1,15 với Cỏ bốn lá); 10% item **225 Mảnh đá vụn** (option 74).

### 6.5 Kết thúc & phần thưởng cuối

- Hạ hết quái + boss ở tất cả map → thông báo "Mau đi tìm Độc Nhãn" (`winDT = true`).
- NPC **Độc Nhãn** (26) ở **map 57 Tầng 4**: khi `winDT` → bắt đầu **5 phút nhặt ngọc** (`isTimePicking`), gọi `randomNR()`: mỗi zone của DT rơi 3 viên + 1/2, 1/3, 1/4, 1/5 cơ hội thêm 1 viên. Mỗi viên: 1/500 là item 14–18 (NR 1–5 sao), còn lại item 18–20 (NR 5–7 sao). NR trong DT không tự biến mất sau 50 s (`ItemMap`).
- Hết 30 phút (khi chưa bước vào giai đoạn nhặt) hoặc hết 5 phút nhặt → `finish()` đưa mọi người về nhà (21+gender) bằng tàu, `dispose()` xóa boss/item, đặt `clan.haveGoneDoanhTrai = true`.

---

## 7. Bản Đồ Kho Báu / Hang kho báu (BanDoKhoBau)

Nguồn: `map/phoban/BanDoKhoBau.java`, `services_dungeon/TreasureUnderSeaService.java`, `npc_list/QuyLaoKame.java`, `services_func/Input.java`, `ChangeMapService.goToDBKB`, `server/Controller.java` (case -105, type 1), `boss/ban_do_kho_bau/TrungUyXanhLo.java`.

### 7.1 Hằng số

| Hằng | Giá trị |
|---|---|
| `POWER_CAN_GO_TO_DBKB` | 2 000 000 000 (2 tỷ SM) |
| `AVAILABLE` | 50 |
| `TIME_BAN_DO_KHO_BAU` | 1 800 000 ms = 30 phút |
| Cấp độ | 1–110 (`Input.CHOOSE_LEVEL_BDKB`) |

### 7.2 Mở / tham gia

NPC **Quy Lão Kame** (13), map **5 Đảo Kamê**, menu "Kho báu dưới biển" (chỉ hiện khi có bang).
- Chưa có hang: "Chọn cấp độ" → cần SM ≥ 2 tỷ (hoặc admin) → nhập cấp 1–110 → xác nhận → `openBanDoKhoBau`: cần có bang, bang chưa có hang, **item 611 "Bản đồ kho báu"** trong túi (bị trừ 1), còn instance trống ("Hang kho báu đã đầy, hãy quay lại sau 30 phút").
- Đã có hang: "Đồng ý" (SM ≥ 2 tỷ) → `goToDBKB`.
- Không yêu cầu số thành viên, thời gian vào bang hay chức vụ.
- Giới hạn **3 lần/ngày/người** (`timesPerDayBDKB`, thông báo "Bạn đã vào hang kho báu 3 lần trong hôm nay…") – xem ghi chú mục 16 về logic đếm.
- Vào: hiệu ứng 5 s (`type=1`) → map **135 Động hải tặc** tại (35, 35).
- Menu "Top bang hội" / "Thành tích bang" (`Service.showTopClanBDKB`, `showMyTopClanBDKB`).

### 7.3 Map, quái, bẫy, boss

Waypoint: `135 Động hải tặc ↔ 138 Cảng hải tặc ↔ 136 Hang Bạch Tuộc → 137 Động kho báu`.

| Map | Quái (DB) | Chỉ số khi mở (`init`, `level` = cấp) |
|---|---|---|
| 135 Động hải tặc | Lính độc nhãn, Sói xám | Quái thứ 5 & 10: `lvMob=1`, dame = level×600×tempId×10, HP = level×469 799×tempId. Còn lại: dame = level×200×tempId, HP = level×469 799×tempId |
| 136 Hang Bạch Tuộc | Sói xám, Robot bay, **Vua Bạch Tuộc** (big boss mob 71) | như trên, quái thứ 5 là lvMob 1 |
| 137 Động kho báu | Robot bay, Robot thép | như trên, quái thứ 5 là lvMob 1 |
| 138 Cảng hải tặc | Robot bay, **Rôbốt bảo vệ** (big boss mob 72) | dame = level×31×50×tempId, HP = level×310 799×50×tempId |

(Tất cả giới hạn 2 147 483 647.)

- **Bẫy** map 135 (`Map.initTrapMap`): vùng x=260, y=960, w=740, h=72, effect 49 (xiên); sát thương = `level × 100 000`.
- **Boss**: Trung úy Xanh Lơ (`boss/ban_do_kho_bau/TrungUyXanhLo`) ở map **137**: dame = min(200 000×level, 200 000 000), HP = min(20 000 000×level, 2 000 000 000). Rơi 100% item **705 "Bí ngô 4 sao"** + 5 điểm sự kiện.
- Big boss Vua Bạch Tuộc / Rôbốt bảo vệ: phần thưởng riêng không tìm thấy trong `mob_bigboss/VuaBachTuoc.java`, `RobotBaoVe.java`.

### 7.4 Kết thúc

- Khi hạ hết quái + boss, hoặc đã qua 29 phút → bật cờ sập hang; thông báo mỗi 10 s "Cái hang này sắp sập rồi…"; sau **60 s** → `finish()`.
- `finish()`: ghi thành tích bang (`levelDoneBanDoKhoBau`, `thoiGianHoanThanhBDKB`), đưa người chơi về **map 5, x=1038** bằng tàu.

---

## 8. Con Đường Rắn Độc (SnakeWay)

Nguồn: `map/phoban/SnakeWay.java`, `services_dungeon/SnakeWayService.java`, `npc_list/ThanVuTru.java`, `npc_list/ThuongDe.java`, `npc_list/Karin.java`, `player/Player.java` (~dòng 580), `ChangeMapService.changeMapWaypoint`, `boss_con_duong_ran_doc/*.java`.

### 8.1 Hằng số

| Hằng | Giá trị |
|---|---|
| `POWER_CAN_GO_TO_CDRD` | 2 000 000 000 – **khai báo nhưng không dùng** |
| `AVAILABLE` | 50 |
| `TIME_CON_DUONG_RAN_DOC` | 1 800 000 ms = 30 phút |
| Cấp độ | 1–110 |

### 8.2 Mở / tham gia

NPC **Thần Vũ Trụ** (20), map **48 Hành tinh Kaio** → "Di chuyển" → "Con đường rắn độc":
- Cần có bang; tài khoản đã kích hoạt.
- "Chọn cấp độ": NPC kiểm tra vào bang ≥ 1 ngày, nhưng `SnakeWayService.openConDuongRanDoc` yêu cầu **≥ 2 ngày** (thiếu thì `return` im lặng).
- Không yêu cầu bang chủ, không yêu cầu số thành viên, không kiểm tra sức mạnh.
- Mỗi người **1 lần / 7 ngày**: nếu `!joinCDRD` và chưa đủ 7 ngày từ `lastTimeJoinCDRD` → "Vui lòng đợi … nữa". Khi vào lại đúng instance bang đang mở (`joinCDRD = true`) thì không bị chặn.
- Hết instance → "Con đường rắn độc đã đầy, hãy quay lại sau 30 phút".
- Vào: **map 143**, x = 1055 ± 10.

### 8.3 Lộ trình

1. Map 143 → 142 → 141 (waypoint DB; 3 map đều tên "Con đường rắn độc"). Quái DB: 141 Quỷ mập, Quỷ địa ngục; 142 Tambourine, Drum; 143 Dơi da xanh, Quỷ chim.
   - Chỉ số (`init`): quái thứ 5 mỗi map `lvMob=1`, dame = level×100×tempId×12, HP = level×1000×tempId×12; còn lại dame = level×10×tempId, HP = level×100×tempId.
2. Hạ hết quái 141–143 → `allMobsDead`. Thượng Đế (19) ở map 141 → "về thần điện" → map 45 (295, 408).
3. Thượng Đế ở map 45 (đánh dấu `talkToThuongDe`) → xuống **Thần mèo Karin** map 46: hồi đầy HP/KI, đánh dấu `talkToThanMeo`, "Hãy mau bay xuống chân tháp Karin".
4. Ở **map 47 Rừng Karin**: `Player.update` (mỗi ≥ 5 s) hoặc đi waypoint 47→1 → dịch chuyển Yardrat sang **map 144 Hoang mạc** (x = 300 ± 100, y=312).

### 8.4 Boss ở map 144

Khởi tạo trong `SnakeWay.init()` (BossData cộng thêm cơ bản: dame +10 000, HP +500 000):

| Boss | Số lượng | dame (trước khi cộng) | HP (trước khi cộng) | Rơi đồ |
|---|---|---|---|---|
| Saibamen "Số 1" … "Số 6" (`SAIBAMEN`) | 6 | min(200 000×level, 2e8) | min(2 000 000×level, 2e9) | 100% item 19 Ngọc Rồng 6 sao |
| Nađíc (`NADIC`) | 1 | ×5 so với Saibamen | ×5 | 100% item 19 Ngọc Rồng 6 sao |
| Cađích (`CADICH`) | 1 | ×10 so với Nađíc | ×10 | 50%: item **459 Phiếu giảm giá** (option 112 "Giảm 80% khi mua Avatar/Cải trang", 93 "HSD 90 ngày", 20 "PIN" ngẫu nhiên < 10 000) **và** item **706 Bí ngô 5 sao** |

Khi Cađích rời map (`CADICH.leaveMap`) → `endCDRD = true`.

### 8.5 Kết thúc

- `endCDRD` hoặc đã qua 29 phút → thông báo mỗi 10 s "Trận chiến với người Xayda sẽ kết thúc sau…"; sau **60 s** → `finish()`: ghi thành tích bang (`levelDoneCDRD`, `thoiGianHoanThanhCDRD`), đưa về **map 5, x=1038**.

---

## 9. Khí Gas Hủy Diệt / Destron Gas (DestronGas)

Nguồn: `map/phoban/DestronGas.java`, `services_dungeon/DestronGasService.java`, `npc_list/MrPoPo.java`, `server/Controller.java` (type 3, 4), `boss/khi_gas/DrLychee.java`, `Hatchiyack.java`, `mob/Mob.java` (~dòng 132).

### 9.1 Hằng số

| Hằng | Giá trị |
|---|---|
| `POWER_CAN_GO_TO_KHI_GAS_HUY_DIET` | 2 000 000 000 – **không dùng** |
| `AVAILABLE` | 50 |
| `TIME_KHI_GAS_HUY_DIET` | 1 800 000 ms = 30 phút |
| `N_PLAYER_CLAN` | **0** (NPC báo "ít nhất 5 thành viên" nhưng thực tế không chặn) |
| Cấp độ | 1–110 |

### 9.2 Mở / tham gia

NPC **Mr.PôPô** (67), map **0 Làng Aru**, menu "OK":
- Có bang; vào bang **≥ 2 ngày**; tài khoản đã kích hoạt.
- Bang đang có KGHD → "Đồng ý" đi cùng (không cần là bang chủ).
- Chưa có: **chỉ bang chủ** được chọn cấp độ 1–110.
- Giới hạn **3 lần mở/ngày/bang** (`clan.timesPerDayKGHD`; lần thứ 4 → "Hãy chờ đến ngày mai").
- Hết instance → "Destron Gas đã đầy, hãy quay lại sau 30 phút".
- Vào: hiệu ứng 5 s (`type=4`) → **map 149 Thành phố Santa**, x = 100 ± 10, y = 336.
- Chuyển map trong KGHD (trừ map 148): hiệu ứng 5 s (`type=3`).

### 9.3 Map & quái

Map: 147 Sa mạc, 148 Lâu đài Lychee, 149 Thành phố Santa, 151 Hành tinh bóng tối, 152 Vùng đất băng giá (150 không tồn tại). Quái DB: Kawazu (73), Kinkarn (74), Arbee (75), **Cỗ máy hủy diệt** (76).

Chỉ số (`init`):
- Quái "đặc biệt" (vị trí 0 ở 147, 7 ở 149, 0 ở 151, 0 và 33 ở 152): `lvMob=1`, dame = level×31×5×tempId×10, HP = level×3×6 700×tempId×10.
- Còn lại: `lvMob = 1` nếu là mob 76, dame = level×31×5×tempId, HP = level×5×45×tempId.
- Khi còn Cỗ máy hủy diệt sống trong zone, các chiêu **Liên hoàn, Antomic, Masenko, Kamejoko** chỉ gây 1 sát thương lên quái (`Mob.injured`).
- Map 152 cũng bị tính là **map lạnh** trong `isMapCold(Map)` (giảm 50% HP).

### 9.4 Boss & phần thưởng

- Khi **toàn bộ quái** của instance chết → tạo **Dr Lychee** ở **map 148**: dame = 10 000 + min(1 000×level, 2e8); HP = 1 000 000 + min(15 000 000×level, 2e9).
- Dr Lychee rời map (sau khi chết) → tạo **Hatchiyack** (tên hiển thị trống `" "`) cùng zone với dame/HP = ×1,5 của Dr Lychee.
- Rơi đồ (cả 2 boss): 1 món ở giữa + 2 món cho mỗi người chơi trong zone (x ± 50·i):

| Boss | Item | Option |
|---|---|---|
| Dr Lychee | **738 Cải trang Dr Lychee** | 50 Sức đánh +(P+8..11)%, 77 HP +(P+8..11)%, 103 KI +(P+8..11)%, 94 Giảm (P+0..3)% sát thương, 93 HSD = random(3..P) tối đa 21 ngày, 30 Không thể giao dịch |
| Hatchiyack | **729 Cải trang Hatchiyack** | như trên nhưng option 5 "+(P+0..3)% sức đánh chí mạng" thay cho 94 |

`P = 14` nếu level 0–9; `P = 14 + level/10` nếu level ≤ 110.

### 9.5 Kết thúc

- Hatchiyack rời map (`hatchiyatchDead`) hoặc đã qua 29 phút → "Nơi này sắp nổ tung mau chạy đi", đếm 60 s ("Về làng Aru sau…").
- `finish()`: ghi thành tích bang (`levelDoneKhiGas`, `thoiGianHoanThanhKhiGas`; cấp ≥ 70 tăng `destronGas70CompletionCount`), đưa về **map 0 Làng Aru**.

---

## 10. Ngọc Rồng Sao Đen (BlackBallWar)

Nguồn: `map/phoban/BlackBallWar.java`, `services_dungeon/BlackBallWarService.java`, `utils/TimeUtil.java`, `npc_list/RongOmega.java`, `npc_list/Rong1Sao.java`, `player/RewardBlackBall.java`, `player/NPoint.java`, `player/EffectSkin.java`.

### 10.1 Giờ & hằng số

| Hằng | Giá trị |
|---|---|
| Mở | 20:00:00 (`HOUR_OPEN`) |
| Được nhặt ngọc | sau 20:30:00 (`HOUR_CAN_PICK_DB/MIN_CAN_PICK_DB`) |
| Đóng | 21:00:00 (`HOUR_CLOSE`) |
| Giữ ngọc để thắng | `TIME_WIN` = 300 000 ms = 5 phút |
| Chờ nhặt lại sau khi ngọc rơi | 5 000 ms |
| Số khu mỗi map | 5 |
| Phù hộ x3 / x5 / x7 | 10 000 000 / 30 000 000 / 50 000 000 vàng |
| Thời hạn phần thưởng | `TIME_REWARD` = 79 200 000 ms = 22 giờ |

### 10.2 Map & ngọc

| Map | Tên | Ngọc (item đặt sẵn – `Map.initItem`) | NPC |
|---|---|---|---|
| 85 | Hành tinh M-2 | 372 Ngọc rồng 1 sao đen | Rồng 1 sao (36) |
| 86 | Hành tinh Polaris | 373 (2 sao đen) | Rồng 2 sao (30) |
| 87 | Hành tinh Cretaceous | 374 (3 sao đen) | Rồng 3 sao (31) |
| 88 | Hành tinh Monmaasu | 375 (4 sao đen) | Rồng 4 sao (32) |
| 89 | Hành tinh Rudeeze | 376 (5 sao đen) | Rồng 5 sao (33) |
| 90 | Hành tinh Gelbo | 377 (6 sao đen) | Rồng 6 sao (34) |
| 91 | Hành tinh Tigere | 378 (7 sao đen) | Rồng 7 sao (35) |

Quái (DB): Quỷ chim, Khỉ lông đỏ, Khỉ lông vàng (hồi sinh bình thường 3 s).

### 10.3 Luồng chơi

- **Rồng Omega** (29) ở trạm tàu 24/25/26: trong giờ mở → "Tham gia" mở tab 7 hành tinh (`MapService.getMapBlackBall`) → `BlackBallWarService.changeMap` vào khu ngẫu nhiên. Ngoài giờ: "Trò chơi tìm ngọc hôm nay đã kết thúc, hẹn gặp lại vào 20h ngày mai".
- Vào map: được gán cờ 1–7 (cùng cờ với đồng đội bang trong zone). Nhặt ngọc → cờ 8 cho cả bang trong zone; chết/rời map → rơi ngọc, bang đổi cờ ngẫu nhiên 1–7.
- **Rồng x sao** (NPC trong map): nếu đang giữ ngọc → "Phù hộ": x3/x5/x7 HP-KI, x3/x5 sức đánh (menu không có x7 SĐ); chỉ 1 lần, hiệu lực tối đa **30 phút** (`EffectSkin`, 1 800 000 ms); bị reset khi chết, hoặc trong `exitMap` khi rời một map **không** phải NRSĐ (điều kiện kiểm tra map đang rời). Không giữ ngọc → "Về nhà".
- Giữ ngọc **5 phút liên tục** → thắng (`win`): người giữ + **mọi thành viên bang** (online hoặc load từ DB rồi lưu) nhận phần thưởng sao tương ứng; zone đánh dấu `finishBlackBallWar`, đá toàn bộ người trong zone về trạm tàu (24+gender, x=250).
- 21:00 → đá mọi người còn trong map về trạm tàu.

### 10.4 Phần thưởng (áp dụng 22 giờ, `NPoint`)

| Sao | Hiệu ứng | Hằng |
|---|---|---|
| 1 | +21% sức đánh | `R1S_2` |
| 2 | +35% HP tối đa | `R2S_1` |
| 3 | +35% hút HP | `R3S_1` |
| 4 | +35% phản sát thương | `R4S_2` |
| 5 | +35% sát thương chí mạng | `R5S_1` |
| 6 | +40% KI tối đa | `R6S_1` |
| 7 | +14% né đòn | `R7S_1` |

Nút "Nhận thưởng" ở Rồng Omega (`RewardBlackBall.getReward`) chỉ thông báo "Chỉ Số Tự Cộng Khi Nhặt xong" – không phát item.

---

## 11. Mabư 12h (MajinBuuService)

Nguồn: `services_dungeon/MajinBuuService.java`, `utils/TimeUtil.isMabuOpen`, `npc_list/Osin.java`, `npc_list/Babiday.java`, `player/FightMabu.java`, `map/Map.initBoss/mapIdNextMabu`, `boss/MajinBuu_12h/*.java`, `ChangeMapService.changeMap`.

- **Giờ mở**: 12:00–12:59 (Asia/Ho_Chi_Minh). `HOUR_OPEN_MAP_MABU = 12`.
- **Vào**: Ôsin (44) tại **map 52 Đại hội võ thuật** → "OK" → map **114** (x 100–500). Không có điều kiện sức mạnh/nhiệm vụ.
- **Số khu**: map 114–120 có type 0 trong DB → **10 khu** (hằng `AVAILABLE = 13` không được dùng).
- **Phe**: cờ 9 (phe Ôsin) hoặc 10 (phe Babiđây) ngẫu nhiên khi vào. Mỗi lần update: cờ 9 có 1% bị Babiđây thôi miên sang 10; cờ 10 có 1/50 được Ôsin giải về 9.
- **Điểm tích lũy**: `POINT_MAX = 10` mỗi tầng. Điểm khi hạ boss (`changePoint`): Drabura 10, Bui Bui 10, Bui Bui 2 10, Yacon 10, Drabura 3 20, Mabư 25, Goku 10, Cadic 10 (Goku/Cadic còn +1 khi đánh trúng). Đánh boss cho % điểm (`changePercentPoint`). Đủ điểm → menu con mèo "Mau đi với ta xuống tầng tiếp theo".
- **Tầng** (`mapIdNextMabu`): 114 Cổng phi thuyền → 115 Phòng chờ → 117 Cửa Ải 1 ("không gian cao trọng lực") → 118 Cửa Ải 2 → 119 Cửa Ải 3 → 120 Phòng chỉ huy.
- **Boss cố định mỗi khu** (`Map.initBoss`): 114 Drabura, 115 Bui Bui, 117 Bui Bui 2, 118 Yacon, 119 Drabura 2, 120 Mabư (MABU_12H).
- **Thưởng khi xuống tầng**: item **521 Tự động luyện tập**, option 1 = `param×5` phút với `param = mapId − 114` (trừ 1 nếu mapId > 116): 115 → 5 phút, 117 → 10, 118 → 15, 119 → 20, 120 → 25.
- **Rơi đồ boss** (vd `Mabu.java.reward`): +5 điểm sự kiện; 100% item 190 Vàng ×20 000–30 000; 1% đồ thần linh (`ItemService.randDoTLBoss`); 1% đồ shop (áo/quần/giày 70%, găng/rada 30%) với chỉ số ×100–115% và 1–6 sao pha lê; 10% NR 2–7 sao (item 15–20) ×1–3. Các boss khác trong `boss/MajinBuu_12h/` có cùng khung reward – chi tiết xem tài liệu boss.
- **Ôsin/Babiđây trong tầng**: "Giải trừ phép thuật 1 ngọc" (`isUseGTPT`), "Xuống tầng dưới".
- **Đóng giờ**: sau 13:00 người trong map được đếm 30 s rồi về nhà (`goHome`).

## 12. Mabư 14h (MajinBuu14H)

Nguồn: `map/phoban/MajinBuu14H.java`, `services_dungeon/MajinBuu14HService.java`, `boss/MajinBuu_14h/Mabu2H.java`, `SuperBu.java`, `npc_list/Osin.java`.

- **Giờ mở**: 14:00–14:59 (`TimeUtil.isMabu14HOpen`). Ngoài giờ mỗi instance `finish()` đá người không phải admin ở 127/128 về nhà.
- **Vào**: Ôsin (44) map 52 → `joinMaBu2H`: vào khu đầu tiên của map **127** có < 5 người (duyệt 7 instance), không có thì khu ngẫu nhiên.
- **Map**: 127 Cổng phi thuyền, 128 Bụng Mabư (type 9 → 7 khu). Map **165 Sa mạc hoang vu** cũng có type 9 nên cũng được gom vào instance Mabư 14h (7 khu).
- **Boss mỗi khu**: 127 Mabư (`Mabu2H`, nhiều dạng: Mabư → Super Bư → Bư Tenk → Bư Han → Kid Bư), 128 Super Bư (`SuperBu`, sát thương nhận chuyển sang Mabư ở 127).
- Mabư mỗi 10 s nuốt ngẫu nhiên (1/5 mỗi người) → vào map 128 (4 ô `MaBuHold`/khu); dạng đầu còn hóa socola 30 s (1/5). Sát thương mỗi đòn lên Mabư tối đa 30 000 000; dạng cuối chỉ chết bởi chiêu **Quả cầu kênh khí**. Khi Mabư chết, người bị nuốt ở 128 được đưa về 127.
- **Ôsin ở 127**: "Phù hộ 10 ngọc" (+1 triệu HP, +1 triệu KI, +10k SĐ – theo text; mất khi rời map); "Về Đại Hội Võ Thuật" (map 52).
- **Phần thưởng**: `Mabu2H.reward` / `SuperBu.reward` chỉ +5 điểm sự kiện và kiểm tra nhiệm vụ; thông báo toàn server khi hạ Mabư. Không tìm thấy item rơi trong code.

---

## 13. Ngọc Rồng Namếc (NgocRongNamecService)

Nguồn: `services_dungeon/NgocRongNamecService.java`, `npc_list/Dende.java`, `npc/NpcFactory.java` (`CONFIRM_TELE_NAMEC`), `services_func/UseItem.java` (item 361), `ChangeMapService.changeMap`.

- **Map rải ngọc**: 7, 8, 9, 10, 11, 12, 13, 25, 31, 32, 33, 34, 43. Mỗi viên (item **353–359** "Ngọc Rồng Namek 1–7 Sao") được đặt ở 1 map khác nhau, khu ngẫu nhiên.
- **Làm mới**: mỗi 600 000 ms (10 phút) các viên không ai giữ bị xóa và rải lại (`run`).
- **Nhặt** (`pickNamekBall`): chỉ khi đã qua `tOpenNrNamec`; mỗi người 1 viên; người giữ + đệ tử chuyển **PK_ALL**; không thể đổi map trong 30 s sau khi nhặt; rời khỏi vùng map NR Namếc → rơi ngọc; không lên được capsule.
- **Rađa** item 361 "Gói 10 Rađa dò ngọc": trừ 1, hiện khoảng cách 7 viên; "Đến ngay … 50 ngọc" nhưng code trừ **10 ngọc** và không kiểm tra đủ ngọc.
- **Gọi rồng** – NPC **Dende** (8) tại **map 7 Làng Mori**: người gọi giữ viên **1 sao (353)**; giờ **8:00–22:59** (chặn nếu giờ > 22 hoặc < 8); đã giữ ngọc ≥ 10 phút; `canCallDragonNamec`: cả 7 viên được ghi nhận ở **map 7, cùng khu**, do **7 người cùng bang** giữ.
- Sau khi ước: `tOpenNrNamec = now + 86 400 000` (24 giờ), rải **Hóa thạch Ngọc Rồng (362)** thay ngọc, `reInitNrNamec(86 399 000)`; hết hạn mới rải lại ngọc thật. Nội dung điều ước ở `SummonDragonNamek` (xem tài liệu rồng thần).
- Hướng dẫn (`ConstNpc.HUONG_DAN_NRNM`) nói "phải chờ 7 ngày sau mới có thể nhận điều ước khác" – không tìm thấy kiểm tra 7 ngày này trong `Dende.java`.

## 14. Siêu Thần Thủy – Tây Karin (SuperDivineWaterService)

Nguồn: `services_dungeon/SuperDivineWaterService.java`, `services_func/UseItem.ItemManhGiay`, `npc/NpcFactory.java` (`MENU_OPTION_USE_ITEM726`, `MENU_SIEU_THAN_THUY`), `boss/ma_vuong_picolo/Pocolo.java`, `npc_list/Karin.java`.

- **Vào**: dùng item **726 "Mảnh giấy có chữ MA"** → "Đồng ý" → map **146 Tây Karin** (type 8, 10 khu), khu trống hoàn toàn (0 người + 0 boss) hoặc khu đã gán trước đó; x=70, y=336. Hết khu → "Vui lòng thử lại sau ít phút!".
- **1 lần/ngày**: nếu đã thắng (`winSTT`) trong ngày → "Hãy gặp thần mèo Karin để sử dụng".
- **Thời lượng**: hiệu ứng PK STT 900 000 ms (15 phút); hết hiệu ứng → về nhà.
- **Quái** (DB: Quỷ đất mẹ, Tambourine, Drum), chỉ số theo người vào (`totalHp = hpMax/2`, `totalDamage = dame/2`): Drum (26) dame = totalHp×1,5, HP = totalDamage×1,5; Tambourine (25) ×2; khác ×1.
- Hạ hết quái → boss **Pôcôlô**: dame = min(dame người chơi, 2e8), HP = min(hpMax×5, 2 147 483 647). Pôcôlô tự rời sau 900 000 ms. `Pocolo.reward` rỗng; khi Pôcôlô "phát nổ" (isLaze) người gọi được đánh dấu thắng.
- **Thưởng**: NPC con mèo "Để tôi đưa cậu về" → map 46; **Thần mèo Karin** trao item **727** (SM < 1 tỷ, "tăng 1 triệu SM/TN") hoặc **728** (SM ≥ 1 tỷ, "tăng 5 triệu SM/TN") "Siêu thần thủy", option 30 (không giao dịch) + 93 (HSD 1 ngày).

## 15. Các map/hoạt động đặc biệt khác

| Hoạt động | Map | Điều kiện / cơ chế | Nguồn |
|---|---|---|---|
| Tàu Pảy Pảy | 111 Đông Nam Karin | SM ≤ 1,5 triệu (hai chỗ kiểm tra `>` và `>=`). Boss cố định mỗi khu `TAU_PAY_PAY_DONG_NAM_KARIN` (dame 100, HP 10 000). **Cách vào map: không tìm thấy trong code** (không có waypoint/NPC tới 111). | `ChangeMapService`, `Map.initBoss`, `BossesData` |
| Yardrat | 131, 132, 133 | Goku SSJ tại map 80 → 131. Boss mỗi khu: 131 Tân binh 5, 132 Chiến binh 5, 133 Đội trưởng 5. `Yardart.reward`: 1/`rewardRatio` (mặc định 5) rơi item **590 "Bí kiếp"** (option 31 số lượng 1). Map 131–133 đòn đánh xuyên (`SkillService` dòng 770). | `GokuSSJ.java`, `boss/yardrat/Yardart.java` |
| Nhân bản Commeson | 140 Hang động Potaufeu | NPC Potage (62): 1 lần/ngày (`lastPkCommesonTime`), tạo bản sao người chơi (dame ×10, HP ×10), 5 phút (`setPKCommeson 300000`). Thắng → item **638 "Bình chứa Commeson"** (HSD 30 ngày, không giao dịch). Cách vào map 140 từ 139: không tìm thấy waypoint trong DB. | `Potage.java`, `Service.callNhanBan`, `boss/nhan_ban/NhanBan.java` |
| Thánh địa | 156–159 | Giu-ma Đầu Bò (map 153) – SM ≥ 40 tỷ → 156. | `GiuMaDauBo.java`, `Controller` |
| Hành tinh thực vật | 160–163 | Item 992 Nhẫn thời không sai lệch. | `UseItem`, `Controller` type 2 |
| Map riêng tư | 164 | Item 1787 Vé riêng tư; quái rơi 1634 "Cápsule Vỡ" (1/19 999). | `UseItem.MapRiengTu`, `Mob.java` |
| Cadic – hút năng lượng | 165 Sa mạc hoang vu | Ôsin map 52 menu "Bình hút năng lượng" (khi có item **1795**); Ôsin ở 165: "Bùa hỗ trợ 5 ngọc" hút nhanh gấp đôi 10 phút (tối đa 60 phút). | `Osin.java` |
| Hành tinh Bill / ngục tù | 154, 155 | 154: NV ≥ 27; 155 qua Ôsin tại 154. | `Osin.java`, `checkMapCanJoin` |
| Ngũ Hành Sơn | 122–124 | Không cho Yardrat vào; TNSM ×1 (dòng x2 bị comment). **Lối vào: không tìm thấy trong code** (122 là map offline). | `ChangeMapService.checkMapCanJoinByYardart`, `NPoint` |
| Tương lai | 92–103 | Calích NV ≥ 20 → 102; giảm SM khi chết. | mục 4.6 |
| Hành tinh Cold | 105–110 | NV ≥ 27; −50% HP/SĐ nếu không có kháng lạnh. | `NPoint` |
| Phòng tập thời gian | 49 | Thách đấu Thượng Đế. | `TrainingService.callBoss` |
| Hirudegarn | 126 Thành phố Santa | Tapion tại 19; mob big boss Hirudegarn (70). | `Tapion.java`, `Map.initMob` |
| Đại hội võ thuật, Võ đài Hạt Mít, Võ đài Siêu Cấp, Thành cổ | 52, 112, 113, 129, 145, 183–185 | Xem `16-su-kien-minigame-giai-dau.md`. | `matches/*` |

---

## 16. Ghi chú / điểm cần lưu ý

**Bug / khả năng bug**

1. `UseItem` case **193** (Gói 10 viên Capsule) thiếu `break` → rơi xuống case 194 → `openCapsuleUI` được gọi **2 lần** (gửi 2 message -91).
2. `ChangeMapService.openChangeMapTab`: case `CHANGE_CAPSULE` **thiếu `break`** → sau danh sách capsule còn ghi tiếp danh sách NR sao đen vào cùng message.
3. `checkMapCanJoin` case **80** (Núi khỉ vàng) thiếu `break` → rơi xuống kiểm tra `TASK_21_0` của nhóm 92–102; thực tế cần NV ≥ 21 thay vì 20.
4. `MapService.getAllMaps()` tự gọi lại chính nó → `StackOverflowError` nếu được gọi.
5. `MapService.getZone()` lặp `while` vô hạn nếu **mọi khu** của map đều đầy.
6. `changeZone`: hồi chiêu thực là 5 000 ms nhưng thông báo tính thời gian chờ theo 10 s (`getTimeLeft(..., 10)`).
7. Hang kho báu – đếm lượt/ngày (`ChangeMapService.goToDBKB`, `TreasureUnderSeaService.openBanDoKhoBau`): nhánh `isAfterMidnight(lastTimeJoinBDKB)` đặt `timesPerDayBDKB = 1` nhưng **không cập nhật `lastTimeJoinBDKB`**, nên các lần vào sau trong ngày có thể luôn rơi vào nhánh này → giới hạn 3 lần/ngày có khả năng không bao giờ kích hoạt. Ngoài ra `openBanDoKhoBau` không tăng bộ đếm.
8. `Osin.confirmMenu` map 52: các nhánh `case 114..120` và `case 127` nằm **bên trong** `switch (indexMenu)` của map 52 (so sánh với indexMenu chứ không phải mapId) → menu Ôsin trong tầng Mabư 12h (Giải trừ phép thuật, Xuống tầng) và ở map 127 (Phù hộ 10 ngọc, Về ĐHVT) nhiều khả năng **không hoạt động** qua Ôsin (Babiđây có xử lý riêng ở `Babiday.java`).
9. `NpcFactory` `CONFIRM_TELE_NAMEC`: menu ghi "50 ngọc" nhưng trừ **10 ngọc**, không kiểm tra đủ ngọc .
10. `RedRibbonHQ.updateHPDame`: điều kiện `totalDame/3 * tempId < MAX ? totalDame * tempId : MAX` không nhất quán (so sánh với /3 nhưng gán không chia).
11. `RedRibbonHQ.update`: khi đã vào giai đoạn nhặt ngọc (`isTimePicking`), giới hạn 30 phút tổng bị bỏ qua (chỉ còn 5 phút nhặt).
12. `DestronGas`: `N_PLAYER_CLAN = 0` trong khi NPC báo "ít nhất 5 thành viên"; `POWER_CAN_GO_TO_KHI_GAS_HUY_DIET` và `SnakeWay.POWER_CAN_GO_TO_CDRD` (2 tỷ) khai báo nhưng không dùng.
13. CDRD: NPC Thần Vũ Trụ cho chọn cấp khi vào bang ≥ 1 ngày, nhưng service yêu cầu ≥ 2 ngày và `return` không thông báo. `MrBlue` load CDRD gán `talkToThanMeo` từ `dataArray.get(2)` (trùng chỉ số với `talkToThuongDe`, đúng ra có lẽ là `get(3)`).
14. `MapService.getMapBlackBall()`/`getMapMaBu()` chỉ lấy `zones.get(0)`; `getMapMaBu()` lấy 114–120 nên có cả **116** (Thánh địa Kaio, map offline).
15. `isMapCold(Map)` gồm 152 (KGHD Vùng đất băng giá) còn `isMapCold(int)` thì không → trong KGHD 152 bị giảm 50% HP nhưng rơi đồ/trừ SM theo bản int không áp dụng. Tương tự `isMapTuongLai(Map)` gồm 95, 101 còn bản int thì không.
16. `isMapCadic(mapId >= 165)` gồm cả 166, 183–185.
17. `MajinBuuService.AVAILABLE = 13` không dùng vì không map nào có type 5; map 114–120 dùng 10 khu từ DB.
18. Map 165 có type 9 nên bị gom vào instance Mabư 14h (7 khu) dù là map Cadic.
19. `Controller` case -105 type 0 yêu cầu `maxTime == 30` nhưng `goToTuongLai` không đặt `maxTime` → việc chuyển tới Tương lai thực tế do `Player.update` sau 60 s đảm nhiệm.
20. `NgocRongNamecService` truy cập `Manager.MAPS.get(mapId)` theo **chỉ số list**, chỉ đúng khi map id liên tục từ 0 (DB thiếu id 150 → map ≥ 151 sẽ lệch; hiện các map NR Namếc ≤ 43 nên chưa ảnh hưởng).
21. `DestronGas.update()`: sau `finish()` + `dispose()` (clan = null) hàm vẫn chạy tiếp phần kiểm tra quái trong cùng lần gọi; nếu `callBoss` còn `false` có thể tạo Dr Lychee với `clan = null` (lỗi bị nuốt bởi `catch`).
22. `UseItem.MapRiengTu` (item 1787 Vé riêng tư) **không trừ vé**.
23. `MapService.readTileIndexTileType` đọc `data/map/tile_set_info` nhưng thư mục thực tế là `SRC/data/map/tile_set_Info` (chữ I hoa) → lỗi trên hệ thống file phân biệt hoa/thường (Linux).
24. `Map.run()` (scheduler 5 s) không được gọi; zone được cập nhật bởi `Manager.initMap` mỗi 1 s.

**Hardcode / bất nhất dữ liệu**

- Mọi map trong DB đều `zones = 10`, `max_player = 15`; số khu thực tế do `type` quyết định (hardcode trong `Map.initZone`).
- Waypoint của các map KGHD 147–152, CDRD 141 & 144, Mabư 114–120, 127–128, Yardrat vào/ra, 111, 140 **trống hoặc không có** trong DB; di chuyển giữa các map KGHD (ngoài map vào 149 và boss ở 148) không xác định được từ code/DB.
- Vị trí vùng đứng gần Lính canh (x 1285–1645), tọa độ vào map, id map đích… đều hardcode trong NPC/service.
- Hướng dẫn NRSĐ (`HUONG_DAN_BLACK_BALL_WAR`) nói có cách thắng thứ 2 "sau 30 phút tham gia tàu sẽ đón về và đang giữ ngọc" – không tìm thấy trong code (chỉ có giữ 5 phút).
- Hướng dẫn Doanh trại nói "mỗi vị tướng giữ ngọc 4–6 sao", code: boss rơi NR 4 sao; ngọc nhặt cuối là 1–7 sao (chủ yếu 5–7 sao).
- `planet_id` trong DB lệch cốt truyện ở nhiều map (Doanh trại 53 = Xayda; 54–62 = Trái Đất; NRSĐ/Tương lai/Cold/KGHD 148–152 = Xayda).
