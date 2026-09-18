# 16 — Sự kiện, minigame, giải đấu, giftcode & quà hằng ngày

> Mọi thông tin trong tài liệu này lấy từ mã nguồn `SRC/src/nro/models/**` và file dump `database team2026.sql`.
> Đường dẫn file Java tính từ `SRC/src/nro/models/`. Tên item/map/NPC tra theo `item_template`, `map_template`, `npc_template` trong dump.
> Chỗ nào code và dữ liệu không đủ để kết luận thì ghi rõ **"không xác định"**.
>
> Tài liệu liên quan: [00-tong-quan.md](00-tong-quan.md) (luồng khởi động, thread), [02b-database-du-lieu-template.md](02b-database-du-lieu-template.md), [17-bang-hoi.md](17-bang-hoi.md), [18-npc.md](18-npc.md), [19-vip-nap-tien-tien-te.md](19-vip-nap-tien-tien-te.md), và các tài liệu về boss, vật phẩm/shop, nâng cấp đồ.

## Mục lục

1. [Tổng quan nhanh](#1-tổng-quan-nhanh)
2. [Cơ chế sự kiện (event / event_list / ievent)](#2-cơ-chế-sự-kiện-event--event_list--ievent)
3. [Boss sự kiện (chỉ liệt kê tham chiếu)](#3-boss-sự-kiện-chỉ-liệt-kê-tham-chiếu)
4. [Nội dung từng sự kiện theo mùa](#4-nội-dung-từng-sự-kiện-theo-mùa)
5. [Điểm sự kiện & đua top sự kiện](#5-điểm-sự-kiện--đua-top-sự-kiện)
6. [Minigame tại NPC Lý Tiểu Nương](#6-minigame-tại-npc-lý-tiểu-nương)
7. [Vòng quay may mắn (LuckyRound – Thượng Đế)](#7-vòng-quay-may-mắn-luckyround--thượng-đế)
8. [PVP: thách đấu, luyện tập, trả thù](#8-pvp-thách-đấu-luyện-tập-trả-thù)
9. [Đại hội võ thuật thế giới (giải theo giờ)](#9-đại-hội-võ-thuật-thế-giới-giải-theo-giờ)
10. [Đại hội võ thuật lần thứ 23 (Rương Gỗ)](#10-đại-hội-võ-thuật-lần-thứ-23-rương-gỗ)
11. [Võ đài Sinh Tử (Võ đài Hạt Mít)](#11-võ-đài-sinh-tử-võ-đài-hạt-mít)
12. [Giải Siêu Hạng (super_rank)](#12-giải-siêu-hạng-super_rank)
13. [Giftcode](#13-giftcode)
14. [Quà đăng nhập / điểm danh / quà miễn phí hằng ngày](#14-quà-đăng-nhập--điểm-danh--quà-miễn-phí-hằng-ngày)
15. [Ghi chú / điểm cần lưu ý](#15-ghi-chú--điểm-cần-lưu-ý)

---

## 1. Tổng quan nhanh

| Hạng mục | NPC / map | Trạng thái trong code hiện tại | File chính |
|---|---|---|---|
| Hệ thống bật sự kiện | — | `EventManager.init()` gọi khi server khởi động; chỉ **Default**, **HungVuong**, **TopUp** đang chạy | `event/EventManager.java` |
| Sự kiện Hùng Vương (NPC) | Hùng Vương (npc 52) – map 183/184/185 | NPC có trong `map_template` | `event/VuaHung.java` |
| Xe nước mía (hè) | Xe nước mía (npc 84) – map 0, 7, 14 | NPC có trong `map_template` | `event/XeNuocMia.java` |
| Nồi bánh (Tết/Hùng Vương) | Nồi bánh (npc 66) | **Không** có trong `map_template` | `event/NoiBanh.java` |
| Minigame (Kéo búa bao, Con số may mắn, Chọn ai đây) | Lý Tiểu Nương (npc 54), logic chỉ chạy khi `mapId == 5` | NPC 54 **không** có trong `map_template` (xem §6) | `npc_list/LyTieuNuong.java`, `minigame/*` |
| Vòng quay may mắn | Thượng Đế (npc 19) – map 45 Thần điện | Hoạt động | `services_func/LuckyRound.java` |
| Đại hội võ thuật thế giới | Ghi danh (npc 23) – map 52; thi đấu map 51 | Chạy 8h–23h | `matches/giai_dau/WorldMartialArtsTournamentManager.java` |
| ĐHVT lần 23 | Ghi danh – map 129 | 24/7 | `matches/dai_hoi_vo_thuat/The23rdMartialArtCongress*.java` |
| Võ đài Sinh Tử | Bà Hạt Mít (npc 21) – map 112 (vào từ map 5) | 24/7 | `matches/dai_hoi_vo_thuat/DeathOrAliveArena*.java` |
| Giải Siêu Hạng | Trọng tài (npc 22) – map 113 | 24/7 | `matches/dai_hoi_vo_thuat/SuperRank*.java` |
| Giftcode | Santa (npc 39) – map 5, 13, 20 | Hoạt động, 1 code trong DB | `services/GiftCodeService.java`, `managers/GiftCodeManager.java` |
| Điểm danh / quà miễn phí | Bò Mộng, Bà Hạt Mít, Giu-ma Đầu Bò, rơi quái | Hoạt động | `services/PlayerService.dailyLogin`, `daily_Giftcode/*` |

Các thread minigame/giải đấu được khởi động trong `server/ServerManager.run()`:
`SuperRankManager`, `The23rdMartialArtCongressManager`, `DeathOrAliveArenaManager`, `WorldMartialArtsTournamentManager`, `ShenronEventManager`, `ChonAiDay_Gem`, `ChonAiDay_Gold`, `ConSoMayManGold`, `ConSoMayManGem`.

---

## 2. Cơ chế sự kiện (event / event_list / ievent)

### 2.1 Kiến trúc

| Thành phần | Vai trò |
|---|---|
| `ievent/IEvent.java` | Interface: `init()`, `npc()`, `createNpc(mapId, npcId, x, y)`, `boss()`, `createBoss(bossId, total...)`, `itemMap()`, `itemBoss()` |
| `event/Event.java` | Lớp trừu tượng. `init()` gọi lần lượt `npc() → boss() → itemMap() → itemBoss()`. `createNpc` thêm NPC vào map bằng `NpcFactory.createNPC(mapId, 1, x, y, npcId)`. `createBoss(bossId, n)` gọi `BossManager.gI().createBoss(bossId)` n lần (mặc định 1) |
| `event_list/*.java` | Mỗi sự kiện là 1 lớp con của `Event`, chỉ override `npc()`/`boss()` |
| `event/EventManager.java` | Cờ bật/tắt tĩnh + `init()` khởi tạo sự kiện |
| `server/ServerManager.run()` | Gọi `EventManager.gI().init()` sau khi load boss (`BossManager.gI().loadBoss()`) |

Không có bảng cấu hình sự kiện trong DB; bật/tắt **hoàn toàn bằng code** (phải sửa và build lại). Biến `ServerManager.EVENT_SEVER = 0` và các hằng `consts/ConstEvent.java` (KHONG_CO_SU_KIEN=0, HALLOWEEN=1, 20_11=2, NOEL=3, TET=4, HUNG_VUONG=5, TRUNG_THU=6, HE=7, VALENTINE=8) được khai báo nhưng **không được dùng** ở đâu.

### 2.2 Cờ bật và trạng thái thực tế (`EventManager.init`)

| Cờ | Giá trị cờ | Lời gọi trong `init()` | Thực tế có chạy? | Nội dung khi chạy |
|---|---|---|---|---|
| (luôn chạy) | — | `new Default().init()` | **Có** | Tạo 30 boss `BROLY` |
| `LUNNAR_NEW_YEAR` | true | `// new LunarNewYear().init();` (comment) | Không | NPC 49 (Đường Tăng) tại map 0 (850,432); 10 boss Lân con |
| `INTERNATIONAL_WOMANS_DAY` | true | comment | Không | Đọc bảng `event` (name `international_womens_day`) |
| `HALLOWEEN` | true | comment | Không | 10 Bí ma + 10 Ma trơi + 10 Dơi |
| `CHRISTMAS` | true | comment | Không | 30 Ông già Noel |
| `HUNG_VUONG` | true | `new HungVuong().init()` | **Có** | 10 boss Thủy Tinh (kèm Sơn Tinh xuất hiện cùng) |
| `TRUNG_THU` | true | comment | Không | 10 Khỉ đột + 10 Nguyệt thần (kèm Nhật thần) |
| `TOP_UP` | true | `new TopUp().init()` | **Có** nhưng rỗng | `npc()` trống, không làm gì |

Cách bật một sự kiện: bỏ comment dòng tương ứng trong `EventManager.init()` (cờ boolean đều đang `true`, nên cờ không có tác dụng thực tế). Muốn tắt Hùng Vương/TopUp: đặt cờ `false` hoặc comment dòng.

### 2.3 Sự kiện 8/3 (`InternationalWomensDay` + `database/EventDAO.java`)

- `init()` gọi `EventDAO.loadInternationalWomensDayEvent()`: `SELECT data FROM event WHERE name='international_womens_day'`, JSON gồm `damePrecent`, `hpPrecent`, `mpPrecent`, `papPrecent` → nạp vào các biến `remainingTimeToIncreaseDame/HP/MP/PotentialAndPower`. `EventDAO.save()` ghi ngược lại.
- Bảng `event` **không tồn tại** trong file dump; các getter của các biến này **không được dùng** ở đâu khác → sự kiện 8/3 hiện không có hiệu lực kể cả khi bật.
- Vật phẩm 8/3 còn lại trong DB: tab shop 51 (shop `QDDN`, npc 13): Thiệp mừng 8-3 (1521) 50 ngọc, Túi hạt giống Hoa Hồng (1525) 5 ngọc, Chậu đất (1528) 2.000.000 vàng, Thuốc tăng trưởng (1529) 1 ngọc, Capsule hồng (722) 399 ngọc, Nơ trang trí (1509) 5 ngọc. Không tìm thấy code nào mở shop tag `QDDN` → shop này không truy cập được.

---

## 3. Boss sự kiện (chỉ liệt kê tham chiếu)

Chi tiết chỉ số/AI xem tài liệu Boss. Dữ liệu từ `boss/BossesData.java`, `boss/BossID.java`, các lớp boss.

| Sự kiện | Boss (BossID) | HP | File | Phần thưởng khi hạ |
|---|---|---|---|---|
| Hùng Vương | Thủy Tinh (-355) | 50.000.000 | `boss/event_hung_vuong/ThuyTinh.java` | +5 điểm sự kiện; rơi item 422 "Cải trang": SĐ/HP/KI +15–20%, giảm sát thương 1–10%, chí mạng +2–5%, không thể bán, HSD 1–15 ngày |
| Hùng Vương | Sơn Tinh (-354), xuất hiện cùng Thủy Tinh | 50.000.000 | `boss/event_hung_vuong/SonTinh.java` | +5 điểm sự kiện; rơi item 421 "Cải trang": SĐ/HP/KI +15–20%, giảm ST 1–10%, biến 1–15% tấn công thành HP, 1/15 có "Kháng TDHS", không bán, HSD 1–15 ngày |
| Trung thu | Khỉ đột (-344) | 100.000.000 | `boss/event_trung_thu/KhiDot.java` | Rơi Đuôi khỉ (1045) |
| Trung thu | Nguyệt thần (-345) + Nhật thần (-346) | 50.000.000 mỗi con | `boss/event_trung_thu/NguyetThan.java`, `NhatThan.java` | Rơi item 2123 / 2124 (SĐ/HP/KI 10–20%, …) — **2 id này không có trong `item_template`** |
| Halloween | Bí ma (-351), Ma trơi (-349), Dơi (-350) | 500.000 | `boss/event/Halloween/*.java` | Rơi Bí ngô (585) |
| Noel | Ông già Noel (-353) | 500 | `boss/event_noel/OngGiaNoel.java` | `reward()` rỗng; mỗi 60 giây thả 3 Hộp quà giáng sinh (648) với xác suất 1/3, 1/5, 1/7 |
| Tết | Lân con (-371) | 5.000.000 | `boss/event_tet/LanCon.java` | Không rơi đồ; xem §4.5 |
| (luôn) | Broly × 30 | — | `event_list/Default.java` | Xem tài liệu Boss |

Map xuất hiện của Thủy Tinh/Sơn Tinh/Halloween/Noel/Lân con: map 0–20, 24–37, 63–77, 79–84, 92–94, 96–100, 102–110. Khỉ đột/Nguyệt thần/Nhật thần: map 0–20.

Điểm quan trọng: các boss có `BossType` sự kiện (`TRUNGTHU_EVENT`, `HALLOWEEN_EVENT`, `CHRISTMAS_EVENT`, `HUNGVUONG_EVENT`, `TET_EVENT`) được đưa vào các manager riêng (`boss/Boss_Manager/*EventManager.java`, đều kế thừa `BossManager`). Trong `ServerManager.run()` **không có** `new Thread(...)` cho các manager này (chỉ có BossManager, Yardart, Final, SkillSummoned, Broly, Other, RedRibbonHQ, TreasureUnderSea, SnakeWay, GasDestroy). Vòng `update()` của boss chạy trong `BossManager.run()` theo danh sách của từng manager → nhiều khả năng boss sự kiện (kể cả Thủy Tinh của Hùng Vương đang bật) **không được update nên không xuất hiện**. Cần kiểm chứng khi chạy thật.

Ngoài boss sự kiện, **70 lớp boss thường** gọi `plKill.event.addEventPoint(diem)` khi bị hạ: 66 boss cho 5 điểm, 4 boss cho 1 điểm (xem §5).

---

## 4. Nội dung từng sự kiện theo mùa

### 4.1 Hùng Vương — NPC Hùng Vương (`event/VuaHung.java`)

NPC tempId 52 (`ConstNpc.HUNG_VUONG` → `NpcFactory` tạo `VuaHung`), đặt tại map 183 "Thành cổ 1", 184 "Thành cổ 2", 185 "Đấu trường thành cổ".

**a) Trồng dưa hấu**

| Thông số | Giá trị (code) |
|---|---|
| Phiên trồng | Mỗi giờ tính từ lúc **class VuaHung được nạp** (`START_TIME = System.currentTimeMillis()`), không theo giờ đồng hồ; chỉ trồng được trong **15 phút đầu** mỗi phiên |
| Giới hạn | `SO_LUONG_TOI_DA = 10` người. Danh sách `nguoiDaTrong` là `static`, **không bao giờ bị xóa** → sau 10 lượt trồng toàn server, không ai trồng được nữa cho tới khi restart |
| Điều kiện | Chưa có cây (`player.DuaHauEgg == null`), chưa trồng trong danh sách |
| Cây dưa | `npc/DuaHauEgg.java`: `TIME_DONE = 86.400.000 ms` (24 giờ). NPC Dưa hấu (npc 51) ở nhà (map 21/22/23). Thu hoạch → 1 **Dưa Hấu** (569), sau đó tự đếm lại 24 giờ |
| Ghi chú | `VuaHung` dùng `THOI_GIAN_TRONG_CAY_MS = 6 giờ` chỉ để hiển thị câu "Cây dưa đã sẵn sàng…", không khớp 24 giờ thực tế |

**b) Đổi Dưa hấu + Tem** (`DoiDuaHau`) — menu ghi "thỏi" nhưng code cộng **ngọc** (`player.inventory.gem`):

| Lựa chọn | Dưa Hấu (569) | Tem chứng nhận Mai An Tiêm (1558) | Nhận |
|---|---|---|---|
| 1 | 1 | 1 | 5 ngọc |
| 2 | 10 | 2 | 40 ngọc |
| 3 | 20 | 3 | 120 ngọc |
| 4 | 25 | 4 | 185 ngọc |
| 5 | 30 | 5 | 250 ngọc |

**c) Dâng sính lễ**

| Lựa chọn | Nguyên liệu | Phí | Nhận |
|---|---|---|---|
| Dâng sính lễ | 9 Ngà voi (1220) + 9 Cựa gà (1221) + 9 Hồng mao (1222) | 1.000.000 vàng | 1 Hộp quà sự kiện (1776) |
| Dâng sính lễ xịn | như trên | 10 ngọc | 1 Hộp quà sự kiện VIP (1777) |
| Dâng bánh dầy | 1 Bánh dầy (1542) | — | Ngẫu nhiên đều 1 trong: Cuồng nộ (381), Bổ huyết (382), Bổ khí (383), Giáp Xên bọ hung (384), Ẩn danh (385), Cỏ bốn lá (1635) |
| Dâng bánh chưng | 1 Bánh chưng Lang Liêu (1556) | — | Ngẫu nhiên đều 1 trong: Cuồng nộ 2 (1150), Bổ khí 2 (1151), Bổ huyết 2 (1152), Giáp Xên bọ hung 2 (1153), Cỏ bốn lá (1635), Ẩn danh 2 (1154), Hematite (1423), Dùi đục (1438), Cápsule Vỡ (1634) |

**d) Mở hộp quà** (`services_func/UseItem.java`)

| Hộp | Hàm | Kết quả |
|---|---|---|
| Hộp quà sự kiện (1776) | `OpenHopQuaThuong` | Ngẫu nhiên búa sơn tinh (1772) / bút thủy tinh (1773); SĐ +10%, HP +10%, KI +10%, chí mạng +10%; HSD 3 ngày (1% trường hợp `randomOption == 0` → option HSD 0 ngày) |
| Hộp quà sự kiện VIP (1777) | `OpenHopQuaVip` | Ngẫu nhiên Mị Hoàng Kim (1761) / CT Black Goku Rose (1731) / CT Black Goku (1732); SĐ/HP/KI +25%, May mắn +10%; HSD 3 ngày (tương tự) |

Cả 2 yêu cầu `getCountEmptyBag > 1` (thông báo lỗi ghi "5 ô").

**Nguồn nguyên liệu**: không tìm thấy nguồn (drop/shop/code) cho Ngà voi, Cựa gà, Hồng mao (1220–1222), Tem Mai An Tiêm (1558), và nguyên liệu nấu bánh (1544–1549) → **không xác định** (có thể chỉ phát qua admin/giftcode).

### 4.2 Nồi bánh (`event/NoiBanh.java`)

NPC tempId 66 — **không đặt trong map nào** của `map_template`.

| Món | Kiểm tra | Trừ | Nhận |
|---|---|---|---|
| Bánh Dầy | Chỉ kiểm tra **có tồn tại** item (không kiểm tra số lượng) | 99 Cơm nếp (1546), 10 Bột gạo (1547), 10 Muối tiêu (1545), 1 Chả lụa (1544) | Bánh dầy (1542), HSD 30 ngày |
| Bánh Chưng | như trên | 99 Cơm nếp (1546), 99 Đậu xanh (1548), 99 Thịt tươi (1549) | Bánh chưng Lang Liêu (1556), HSD 30 ngày |

### 4.3 Sự kiện hè: Xe nước mía (`event/XeNuocMia.java`)

NPC tempId 84 tại map 0 "Làng Aru", 7 "Làng Mori", 14 "Làng Kakarot".

| Lựa chọn | Nguyên liệu | Vàng | Nhận | Điểm top |
|---|---|---|---|---|
| Mua 1 ly | 5 Khúc mía (1612) + 2 Nước đá (1613) | 5.000.000 | 1 ly ngẫu nhiên đều: Ly mía khổng lồ (1614) / Ly mía thơm (1615) / Ly mía sầu riêng (1616) | `point_sukien1 += 1` |
| Mua 10 ly | 50 Khúc mía + 20 Nước đá | 50.000.000 | 10 ly ngẫu nhiên | `point_sukien1 += 10` |

Nguồn: **Khúc mía** rơi 5% mỗi lần giết quái ở map 0–163 (`mob/Mob.java`, `MapService.AllMap`). **Nước đá** bán ở shop `SHOP_CHI_CHI` (npc 81 Chi Chi, map 5) tab 58 giá 5 ngọc.

Hiệu ứng ly mía (`UseItem.useItemTime`, `player/NPoint.java`, `item/ItemTime.java`): thời hạn 10 phút (`TIME_NUOC_MIA1/2/3 = 600.000 ms`), chỉ dùng 1 loại cùng lúc.

| Ly | Hiệu ứng tìm thấy trong NPoint |
|---|---|
| Ly mía khổng lồ (1614) | HP tối đa +10% |
| Ly mía thơm (1615) | HP tối đa +10%, chí mạng +10 |
| Ly mía sầu riêng (1616) | HP tối đa +10%, sức đánh +10%, giáp +10%, sát thương chí mạng +10 |

### 4.4 Sự kiện thiếu nhi / Kem trái cây / Pháo bông (UseItem)

| Vật phẩm | Nguồn | Khi dùng | Điểm top |
|---|---|---|---|
| Hộp quà thiếu nhi (1608) | Shop đổi điểm sự kiện (Quy Lão Kame, tab 59) giá **9 điểm sự kiện** | `QuaThieuNhi`: ngẫu nhiên đều Goku ssj 4 kid (1807) / CT Fide nhí (1599) / CT Xên nhí (1600) / CT Mabư nhí (1601) / CT Android 21 kid (1602); SĐ/HP/KI +20–25%, 4 dòng chỉ số ẩn, không giao dịch, 50% HSD 15 ngày – 50% option HSD 0 | `point_sukien += 1` |
| Kem trái cây (1609) | Shop Chi Chi tab 58, 50 ngọc | `KemTraiCay` (cần > 4 ô trống): Buma đi biển (1804) SĐ +20–28%, HP/KI +20–30%, không giao dịch, HSD 15 ngày (1% option HSD 0) | `point_sukien2 += 1` |
| Pháo bông (1575) | Không xác định | +5.000–20.000 vàng + hiệu ứng | (đã comment) |
| Pháo bông VIP (1576) | Không xác định | +500.000–2.000.000 vàng + hiệu ứng | (đã comment) |

Top các điểm này xem ở NPC Chi Chi (§5.2).

### 4.5 Tết — Lân con (`boss/event_tet/LanCon.java`, `npc_list/QuyLaoKame.java`, `RewardService.rewardLancon`)

(Sự kiện đang tắt.)
- Đánh Lân con: sát thương mỗi đòn bị giới hạn 500.000. Khi đòn đánh ≥ HP còn lại **hoặc** câu chat gần nhất của người chơi chứa chữ `"thang"` → Lân con hồi đầy máu, chuyển AFK và **đi theo** người chơi đó (nếu người chơi cách > 300px hoặc đổi map VIP thì mất trạng thái nhận thưởng).
- Mang Lân con đến Quy Lão Kame (menu "Giao Lân con") → `rewardLancon` (cần 1 ô trống): ngẫu nhiên đều Ngọc Thố (734), Gậy như ý (920), Pháo Thăng Thiên (849), Chổi bay Phù Thủy (743), Cân đẩu vân ngũ sắc (733):
  - 5%: 1 option chỉ số (HP/HP 30s/…/SĐ/giảm ST/SĐ chí mạng) 1–5% + 1 option đặc biệt 1–2 (không HSD).
  - 95%: 1 option chỉ số 1–10%, 10% có thêm option đặc biệt 1–10, HSD 1–30 ngày.
  - Luôn có option 89 và "Không thể giao dịch".
- `LunarNewYear.npc()` còn đặt NPC 49 (Đường Tăng) tại map 0.

### 4.6 Noel

- Ông già Noel thả **Hộp quà giáng sinh (648)**. Muốn nhặt phải có **Tất, vớ giáng sinh (649)** trong hành trang và mỗi lần nhặt trừ 1 Tất (`map/Zone.java`, `InventoryService.findItemTatVoGiangSinh`). Tất bán ở tab 30 shop `BULMA_EVENT` (npc 106) giá 20 ngọc — nhưng không tìm thấy code mở shop `BULMA_EVENT` và NPC 106 không có trong map.
- Mở Hộp quà giáng sinh (`UseItem.NoelItemBox`, cần 1 ô trống): 5/90 (~5,6%) nhận 10–20 ngọc; còn lại ngẫu nhiên đều 1 trong 7 nhóm: Sao pha lê (441–445, SL 1–5), đá 381–384 (SL 1–5), Ngọc rồng 4–7 sao (17–20), Ngọc rồng băng (925–931), mảnh (1066–1070, SL 1–5), Kẹo giáng sinh (533), Viên Capsule kì bí (380).
- Gói quà (1170) → `BlackGokuItemBoxEventNoel`: Xe tuần lộc (746) / Noel 2022 Goku-Cađíc-Pôcôlô (1155–1157, theo hành tinh) / Cải trang Broly (1018–1020) / Tanjiro…Nezuko (1087–1091); nguồn không xác định.
- Túi 7 chú lùn (1171, shop BULMA_EVENT 349 ngọc) → `ChuLunBox`: item 1158–1164, SĐ 11/HP 13/KI 13 + 1 option ngẫu nhiên 3–5, 98% HSD 1–3 ngày.

### 4.7 Halloween / Trung thu / 20-11 (Valentine)

- **Halloween**: chỉ có boss rơi Bí ngô (585). Không tìm thấy nơi đổi Bí ngô (585 chỉ xuất hiện trong danh sách "rác" `ItemService`). Hiệu ứng Halloween trong `player/EffectSkin.java` đã bị comment.
- **Trung thu**: boss rơi Đuôi khỉ (1045 – item dùng theo thời gian) và 2123/2124 (không tồn tại). NPC "Trung thu" (npc 41) không được đặt trong map.
- **Gói quà giấy màu** (`UseItem` case 1505–1509, `NpcFactory` menu `event3`/`event3_1`):
  - Dùng Giấy màu (1505): cần 99 Giấy màu → 1 Hộp đựng quà (1506). (Menu ghi "Giá vàng 2.000.000" nhưng code **không trừ vàng**.)
  - Dùng 1506/1507/1508/1509: cần 5 Sôcôla Trái Tim (1507) + 30 Hoa hồng giấy (1508) + 1 Nơ trang trí (1509) + 1 Hộp đựng quà (1506). "Hộp quà nhẹ nhàng" → 1510 (không trừ Nơ); "Hộp quà chỉn chu" → 1511 (trừ cả Nơ). Công dụng của 1510/1511: không tìm thấy trong code.

---

## 5. Điểm sự kiện & đua top sự kiện

### 5.1 Điểm sự kiện (`player/PlayerEvent.eventPoint`, cột `player.event_point`)

| Nguồn | Số điểm | File |
|---|---|---|
| Hạ boss thường (66 loại) / (4 loại) | 5 / 1 | các lớp `boss/**` – `reward()` |
| Hạ Thủy Tinh, Sơn Tinh | 5 mỗi con | `boss/event_hung_vuong/*` |
| Đổi VND → thỏi vàng (`Input` case đổi thỏi vàng) | `(VND / 10.000) × 50` | `services_func/Input.java` |
| Đổi VND → ngọc (`TRADE_GEM`) | `(VND / 10.000) × 50` | `services_func/Input.java` |

**Tiêu điểm**: Quy Lão Kame → "Đổi điểm sự kiện" → shop `SHOP_DOI_DIEM` (tab 59 "Đổi thưởng"). Giá thực tế lấy từ `switch` trong `ShopService` (chỉ áp dụng cho tab 59), không dùng cột `cost`:

| Item | Tên | Giá (điểm sự kiện) |
|---|---|---|
| 1567 | CT Frieren | 999 |
| 1731 | CT Black Goku Rose | 999 |
| 1711 | Cân Đẩu Vân Thơ Mộng | 750 |
| 1713 | Khủng Long Thơ Mộng | 499 |
| 1682 | Pet Hải Ly | 499 |
| 1698 | CT Urôn Trư Bát Giới | 499 |
| 1821 | Trứng vàng rồng nhí | 199 |
| 1840 | Hộp quà Goku Day VIP | 99 |
| 1757 | Hộp quà Cađíc VIP | 99 |
| 1592 | Hộp quà Goku Day VIP | 99 (có trong switch, không có trong tab 59 của DB) |
| 1608 | Hộp quà thiếu nhi | 9 |

### 5.2 Top sự kiện (NPC Chi Chi – npc 81, map 5)

| Menu | Cột điểm | Tăng khi | Truy vấn |
|---|---|---|---|
| Top Hộp quà thiếu nhi 2025 | `point_sukien` | Mở Hộp quà thiếu nhi | `Manager.queryTopsukien` (top 100) |
| Top Nước mía | `point_sukien1` | Mua nước mía ở Xe nước mía | `queryTopsukien1` |
| Top Kem trái cây | `point_sukien2` | Dùng Kem trái cây | `queryTopsukien2` |

Top được cập nhật lại mỗi 3 giây nếu có cờ thay đổi (`ServerManager.startTopUpdater`). Không có code trao thưởng top ("Kết thúc và trao giải sau (....)" là chuỗi cố định). Lỗi: nhánh `isTopSukien2Changed` lại gán vào `Topsukien1` bằng `queryTopsukien1` , đồng thời lúc khởi động `Manager` không load `Topsukien2` → top Kem trái cây luôn rỗng.

---

## 6. Minigame tại NPC Lý Tiểu Nương

`npc_list/LyTieuNuong.java` (npc tempId 54). Menu gốc: "Kéo Búa Bao", "Con số may mắn thỏi vàng", "Con số may mắn ngọc xanh", "Chọn ai đây". Mọi xử lý nằm trong `if (this.mapId == 5)`. `MenuController` và `Npc.canOpenNpc` có ngoại lệ cho phép mở NPC này từ bất kỳ đâu, nhưng NPC phải tồn tại trong `Manager.NPCS`; trong dump, NPC 54 **không** nằm trong cột `npcs` của map nào → theo dữ liệu hiện tại, minigame **không truy cập được** trừ khi thêm NPC 54 vào map 5.

### 6.1 Kéo – Búa – Bao

| Mục | Giá trị |
|---|---|
| Mức cược | 1 tỷ / 5 tỷ / 10 tỷ vàng (phải có đủ vàng) |
| Máy chọn | `Math.random()*3` – đều 1/3 |
| Thắng | Cộng **96%** tiền cược (thuế 4%) |
| Thua | Trừ 100% tiền cược |
| Hòa | Không đổi |
| Thời gian | Tức thì, không giới hạn lượt |
| Kỳ vọng | (0,96 − 1)/3 ≈ **−1,33%** tiền cược mỗi ván |

### 6.2 Con số may mắn (`minigame/ConSoMayManGold.java`, `ConSoMayManGem.java`, `services_func/MiniGame.java`)

`MiniGame` chỉ là singleton giữ 2 instance `MiniGame_S1_Gold`, `MiniGame_S1_Gem`.

| Thông số | Bản thỏi vàng | Bản ngọc xanh |
|---|---|---|
| Vé mỗi số | 1 Thỏi vàng (457) | 5 ngọc |
| Thưởng khi trúng | 90 Thỏi vàng | 450 ngọc |
| Khoảng số | `min=0`, `max=100`, kết quả `Util.nextInt(0,100)` → **101 giá trị (0–100)** |  |
| Chu kỳ 1 ván | 50 giây đặt cược (đếm `second` 50→0) + 10 giây chờ → ~60 giây, chạy 24/7 |  |
| Chọn số thủ công | Form nhập, hợp lệ khi `0 ≤ số ≤ 100` **và** `second > 10` (tức ~40 giây đầu) |  |
| Ngẫu nhiên số lẻ / chẵn | Lẻ: 1–99; chẵn: 0–100; được đến hết giai đoạn đặt cược |  |
| Giới hạn | Tối đa 10 số/người/ván, không trùng số |  |
| Tỷ lệ trúng / số | 1/101 |  |
| Hoàn trả lý thuyết | 90/101 ≈ 89,1% (thuế ~10,9%) |  |

Lưu ý khác biệt text: NPC ghi "Thời gian từ 8h đến hết 21h59", "số từ 0 đến 99", "mỗi lượt 5 phút"; form nhập ghi "giá 1.000.000 vàng" — **code không có giới hạn giờ, cho 0–100, ván ~60 giây, vé là 1 thỏi vàng**.

Trả thưởng (`ResetGame` → `strFinish`): chỉ người **đang online** mới nhận; người offline chỉ được ghi tên. Xem lỗi nhân thưởng ở §15.

### 6.3 Chọn ai đây (`minigame/ChonAiDay_Gold.java`, `ChonAiDay_Gem.java`)

| Thông số | Bản vàng | Bản ngọc xanh |
|---|---|---|
| Vé "Thường" | 1.000.000 vàng/lần | 10 ngọc/lần |
| Vé "VIP" | 10.000.000 vàng/lần | 100 ngọc/lần |
| Giới hạn số lần | Không giới hạn (text ghi "tối đa 10 lần") | như bên |
| Thời gian 1 lượt | `TIME_CHONAIDAY = 300.000 ms` = 5 phút, 24/7 | như bên |
| Số giải | 2 giải (Thường, VIP) cho mỗi loại tiền (text ghi "6 giải") | |
| Chọn người thắng | Sắp xếp người chơi theo tỉ lệ góp giảm dần, lấy **ngẫu nhiên đều** 1 người trong **top 5** (không theo tỉ lệ góp) | như bên |
| Thưởng giải Thường | 80% tổng quỹ (thuế 20%) | 80% tổng quỹ |
| Thưởng giải VIP | 90% tổng quỹ (thuế 10%) | 90% tổng quỹ |
| Chỉ 1 người tham gia | Vẫn "thắng" 80%/90% quỹ của chính mình | Hoàn 90% số ngọc đã đặt |
| Thông báo | Chat thế giới tên người thắng | Chat thế giới; text ghi "hồng ngọc" nhưng cộng vào `inventory.gem` (ngọc xanh) |

"Cơ hội trúng" hiển thị = `ceil(góp / tổng × 100)%` (`Player.percentGold`, `percentGem`) — chỉ là tỉ lệ góp, không phải xác suất thật.

---

## 7. Vòng quay may mắn (LuckyRound – Thượng Đế)

NPC Thượng Đế (npc 19) tại map 45 "Thần điện", menu "Quay ngọc May mắn" (`npc_list/ThuongDe.java`, `ConstNpc.MENU_CHOOSE_LUCKY_ROUND`):

| Lựa chọn | Hành động |
|---|---|
| Quay bằng vàng | `openCrackBallUI(USING_GOLD)` – 7 ô icon 419–425 |
| Vòng quay đặc biệt | `openCrackBallVipUI(USING_GOLD)` – 7 ô cùng icon 419; **vẫn dùng vàng**, cùng bảng thưởng |
| Rương phụ (n món) | Shop `ITEMS_LUCKY_ROUND` – lấy đồ từ `itemsBoxCrackBall` |
| Xóa hết trong rương | Xóa toàn bộ rương phụ (không khôi phục) |

| Thông số (`LuckyRound.java`) | Giá trị |
|---|---|
| Giá vàng | `PRICE_GOLD = 250.000.000` vàng/viên |
| Giá ngọc | `PRICE_GEM = 4` ngọc/viên (`USING_GEM = 7`) — **không có menu nào mở** |
| Giá vé | 1 Vé quay ngọc vàng (821)/viên (`USING_TICKET = 1`) — **không có menu nào mở** |
| Rương phụ tối đa | 100 món (`MAX_ITEM_IN_BOX`) |
| Số viên mỗi lần | Do client gửi (`count`) |

**Bảng thưởng mỗi viên** (`RewardService.getListItemLuckyRound`; tham số `vip` không được dùng). Xác suất tính từ chuỗi `if/else` trong code:

| Nhánh | Kết quả | Xác suất tổng / viên |
|---|---|---|
| Nhánh A (50%) | Bunma tóc xanh neon (1208), SĐ/HP/KI +20–25%, không bán lại, 99% HSD 3–15 ngày | 2,50% |
|  | Bunma tóc nâu băng đô (1209), như trên | 2,375% |
|  | Bunma tóc tím thắt bím (1210), như trên | 2,256% |
|  | Cải trang Hit (884), SĐ/HP/KI 0–19%, SĐ chí mạng 0–29%, không bán, 99% HSD 3–15 ngày | 2,143% |
|  | Cải trang Mị Nương (860), SĐ/HP 0–23%, "Đẹp +15%", không bán, 99% HSD 3–15 ngày | 2,036% |
|  | Bó Hoa Vàng (955), SĐ/HP/KI +1–10%, 99% HSD 3–15 ngày | 0,387% |
|  | Mảnh Đội trưởng Vàng (956) | 1,915% |
|  | Vàng (189) 5.000–50.000 | 36,39% |
| Nhánh B (50%) | Phụ kiện: Lồng đèn (467–471, 800, 801, 803, 804), Cánh dơi Dracula (741), Bông tuyết (745), Xiên cá (1000); 20% bốc lại có thêm Mèo mun (999), Phóng lợn (1001). 1 option ngẫu nhiên 5–10, không giao dịch, HSD 1–30 ngày | 12,5% |
|  | Ngọc Rồng 5–7 sao (18–20) × 1–5 | 0,125% |
|  | Đá nâng cấp 220–224 × 1–5 | 0,41% |
|  | Mảnh 828–842 × 1–5 | 0,12% |
|  | Vàng (189) 5.000–50.000 | 11,84% |
|  | Vàng (189) 5.000–12.000 (do `itemRand` khi `success=false`, ghi đè cả phần thưởng đã chọn) | 25,0% |

Tổng: ~73% ra vàng (tối đa 50.000 vàng) cho 250 triệu vàng/viên.

---

## 8. PVP: thách đấu, luyện tập, trả thù

`matches/PVPService.java`, `matches/PVP.java` và các lớp con. `PVP` khi khởi tạo gán `p.pvp`, gọi `start()` (đổi cờ PK) và thêm vào `PVPManager`. `lose()` → `finish()`, `reward(người thắng)`, `sendResult()`, `dispose()` (trả về NON_PK).

| Loại (`TYPE_PVP`) | Lớp | Khởi tạo | Chi phí / thưởng |
|---|---|---|---|
| THACH_DAU | `ThachDau` | Mời đối thủ cùng map (message -59 type 3), chọn mức cược | Mức cược: **1.000.000 / 10.000.000 / 100.000.000 vàng**; cả hai phải đủ vàng. Thắng nhận `cược/100×80` (80%). Thua (chết hoặc bỏ chạy) bị trừ 100% cược. Người thắng là p1 → cập nhật thành tựu "Trăm trận trăm thắng" |
| LUYEN_TAP | `LuyenTap` | Mời (type 4), cờ `PK_PVP_2` | Không cược, không thưởng; báo nếu 2 người khác cấp |
| TRA_THU | `TraThu` | Từ danh sách kẻ thù (`openSelectRevenge`) | **1 ngọc**, tự do trả thù trong 5 phút (`300.000 ms`); dịch chuyển tới chỗ kẻ thù, 3 giây sau bắt đầu. Hạ được kẻ thù thì xóa khỏi danh sách |
| (THACH_DAU) | `DHVT` | Dùng nội bộ cho ĐHVT/Siêu hạng/Võ đài | Không thưởng |
| (THACH_DAU) | `PKCommeson` | Nội bộ | Bỏ chạy: "Bạn đã thất bại, ngày mai hãy thử sức tiếp" |

Ghi chú `ThachDau`: `goldThachDau` được gán **sau** `super(...)`, mà constructor `PVP` gọi `start()` ngay → lúc `start()` giá trị cược vẫn là 0 nên **không trừ vàng lúc bắt đầu**. Kết quả thực tế: người thắng +80% cược (tiền "sinh ra"), người thua −100% cược.

`TOP.java` chỉ là DTO (lombok builder) cho bảng xếp hạng.

---

## 9. Đại hội võ thuật thế giới (giải theo giờ)

Files: `matches/giai_dau/WorldMartialArtsTournamentManager.java` (thread, 1 giây/lần), `matches/dai_hoi_vo_thuat/WorldMartialArtsTournamentService.java`, `WorldMartialArtsTournament.java` (trận), `consts/ConstTournament.java`. NPC Ghi danh (npc 23) tại map 52 "Đại hội võ thuật"; trận đấu ở map 51 "Đấu trường" (mỗi trận 1 khu).

### 9.1 Lịch & lệ phí

| Giải | Giờ | Điều kiện sức mạnh | Lệ phí | Thưởng mỗi vòng thắng (theo text) |
|---|---|---|---|---|
| Nhi đồng | 8h, 14h, 18h | < 1.500.000 | 2 ngọc | 2 ngọc |
| Siêu cấp 1 | 9h, 13h, 19h | 1.500.000 – < 15.000.000 | 4 ngọc | 4 ngọc |
| Siêu cấp 2 | 10h, 15h, 20h | 15.000.000 – < 150.000.000 | 6 ngọc | 6 ngọc |
| Siêu cấp 3 | 11h, 16h, 21h | 150.000.000 – < 1.500.000.000 | 8 ngọc | 8 ngọc |
| Ngoại hạng | 12h, 17h, 22h, 23h | Không giới hạn (mọi sức mạnh) | 10.000 vàng | 10.000 vàng |

Mỗi người chỉ đăng ký được đúng giải khớp sức mạnh của mình hoặc Ngoại hạng. Người đã vô địch trong ngày (`listChamp`, xóa lúc qua nửa đêm) không đăng ký lại được. Hủy đăng ký **không hoàn phí**.

### 9.2 Mốc thời gian trong giờ

| Phút | Diễn biến |
|---|---|
| 00–24 | Mở đăng ký (`MINS_MAX_CAN_REG = 25`) |
| < 30 | Mỗi phút báo "Trận đấu của bạn sẽ diễn ra trong vòng X phút" |
| 30 (`MINS_START`) | Bắt cặp vòng 1. Ai **không đứng ở map 52** bị truất quyền. Số lẻ → người cuối được đi thẳng vòng sau |
| Giữa các vòng | Người thắng vào `listWait`; khi hết trận, chờ 240 giây (4 phút, có nhắc mỗi 30 giây/mỗi phút) rồi bắt cặp vòng tiếp |
| > 57 (`MINS_END`) | Kết thúc mọi trận còn lại, reset `round` |

### 9.3 Luật 1 trận (`WorldMartialArtsTournament`)

- Vòng lặp mỗi ~150 ms. Giới thiệu 23 nhịp (~3,5 giây, hồi đầy HP/MP ở nhịp 6), sau đó thi đấu `timeDown = 181` nhịp (~27 giây theo nhịp 150 ms).
- Thua khi: chết (`typeEnd=2`), rơi khỏi võ đài (`x < 158`, `x > 610` hoặc `y > 320`, `typeEnd=1`), rời map 51/khu (`typeEnd=3`).
- Hết giờ: ai **nhận ít sát thương hơn** (`totalDamageTaken`) thắng; bằng nhau → player_2 thắng.
- Người thắng về map 52; người thua về nhà (map 21 + gender).

### 9.4 Thưởng

| Mốc | Code thực tế |
|---|---|
| Thắng mỗi vòng | Thông báo "nhận thưởng X vàng/ngọc" nhưng code chỉ `gold++` hoặc `gem++` (**+1**); thêm rơi-nhặt Vàng (190) ngẫu nhiên 10.000–1.000.000; `martialArtsTournamentWins++` |
| Vô địch | Rơi-nhặt Ngọc (77) × 50; 5 đá nâng cấp: Đá lục bảo (220), Đá Saphia (221), Đá Ruby (222), Đá Titan (223), Đá thạch anh tím (224) mỗi loại 1 (kèm option "Dùng để nâng cấp …"); thông báo toàn server |

Menu Ghi danh còn chuyển sang "Giải Siêu Hạng" (map 113) và "ĐHVT lần thứ 23" (map 129).

---

## 10. Đại hội võ thuật lần thứ 23 (Rương Gỗ)

Files: `npc_list/GhiDanh.java` (map 129), `matches/dai_hoi_vo_thuat/The23rdMartialArtCongressService.java`, `The23rdMartialArtCongress.java`, `matches/giai_dau/The23rdMartialArtCongressManager.java`, `services_func/UseItem.openRuongGo`.

| Mục | Giá trị |
|---|---|
| Thời gian | 24/7 |
| Lệ phí khởi điểm mỗi ngày | 2 ngọc **hoặc** 50.000 vàng |
| Tăng phí | Sau mỗi lần thi (dù trả bằng gì): phí vàng ×2, phí ngọc +2 |
| Reset | Khi mở menu sau nửa đêm so với `lastTimePKDHVT23`: phí về 50.000 vàng / 2 ngọc, `levelWoodChest = 0` |
| Điều kiện | Không có Rương Gỗ (570) trong hành trang/rương đồ ("Hãy mở rương báu vật trước"); chưa đạt cấp 12 |
| Vào trận | Map 129 khu ngẫu nhiên không có boss; bắt đầu từ vòng = `levelWoodChest` hiện tại |
| Mỗi vòng | 13 giây chuẩn bị (choáng cả 2 bên 14 giây, hồi đầy HP/MP; vòng 4, 6, 8, 10 hồi chiêu), 180 giây thi đấu |
| Thua | Chết, hết giờ, rơi khỏi đài (`y > 264` và `x ∉ (150, 630)`), rời khu |

Thứ tự đối thủ (vòng 0→11): Sói Hẹc Quyn, Ô Dô, Xinbatô, Cha Pa, Pon Put, Chan Xu, Tàu Pay Pay, Yamcha, Jacky Chun, Thiên Xin Hăng, Liu Liu, Pôcôlô. Thắng vòng 12 → "vô địch" (không có thưởng thêm).

**Nhận thưởng**: menu "Nhận thưởng Rương Cấp N" → 1 Rương Gỗ (570) option "Cấp N", không giao dịch; `levelWoodChest` về 0, ghi `lastTimeRewardWoodChest`. Rương chỉ **mở được từ ngày hôm sau** (`UseItem`: nếu chưa qua nửa đêm → "Hãy chờ đến ngày mai").

**Nội dung Rương Gỗ cấp L** (`openRuongGo`):

| Thành phần | Công thức |
|---|---|
| L = 0 | 1 Vàng (190) số lượng 1 |
| Vàng (190) | `(100×L ± 15%) × 1000` |
| Ngọc (77) | Chỉ khi L ≥ 9: `100 + (L−9)×20` |
| Trang bị (`randClothes(L)`, kèm option shop) | 1 món; 2 món nếu L 5–8; 3 món nếu L 10–12 |
| Vật phẩm ngẫu nhiên không trùng từ {Ngọc rồng 4–7 sao (17–20), Viên Capsule kì bí (380), Cuồng nộ, Bổ huyết, Bổ khí, Giáp Xên, Ẩn danh (381–385), Bí kíp tuyệt kỹ (1229)} | 2 loại; 3 nếu L 5–8; 4 nếu L 10–12; mỗi loại SL 1..L |
| Sao pha lê 441–447 (option +5, riêng loại thứ 4/5 là +3) | 1 loại (2 nếu L > 9), SL 1–3 |
| Đá nâng cấp 220–224 | 1 loại (2 nếu L > 9), SL 1..2L |

---

## 11. Võ đài Sinh Tử (Võ đài Hạt Mít)

Files: `npc_list/BaHatMit.java` (map 5 → nút "Võ đài Sinh tử" đưa tới map 112 "Võ đài Hạt Mít"), `matches/dai_hoi_vo_thuat/DeathOrAliveArenaService.java`, `DeathOrAliveArena.java`, `matches/giai_dau/DeathOrAliveArenaManager.java` (tick 1 giây).

| Mục | Giá trị |
|---|---|
| Thời gian | 24/7 |
| Lệ phí | Menu ghi "`thoiVangVoDaiSinhTu` thỏi vàng" nhưng code trừ **vàng** (`inventory.gold`). Lần đầu mỗi ngày 0, mỗi lần đăng ký +10 (0, 10, 20… vàng). Reset về 0 và xóa thưởng chưa nhận khi qua nửa đêm (`lastTimePKVoDaiSinhTu`) |
| Khu | Khu trống đầu tiên của map 112 |
| Mỗi vòng | 5 giây chờ, 180 giây thi đấu |
| Thua | Chết, hết giờ, rơi đài (`y > 336` và `x ∉ (322, 614)`), mất cờ `isPKDHVT`, rời map |
| Đối thủ | Vòng 1 Dracula → 2 Người vô hình → 3 Bông băng → 4 Vua quỷ sa tăng → 5 Thỏ đầu bạc (`boss/vo_dai_hat_mit/*`) |
| Vô địch (thắng 5 vòng) | `haveRewardVDST = true`; lưu thời gian tốt nhất `timePKVDST` |
| Nhận thưởng | Bà Hạt Mít map 112 → "1 vệ tinh ngẫu nhiên": Vệ tinh trí lực/trí tuệ/phòng thủ/sinh lực (342–345), HSD 30 ngày, cần 1 ô trống. Dùng: đặt trên map, tối đa 3 vệ tinh/map |

**Bình chọn (cá cược khán giả)** – chỉ khi có người đang thi ở khu:

| Mục | Giá trị |
|---|---|
| Phí | 1.000.000 vàng/phiếu, chọn phe người chơi hoặc phe Hạt Mít, không giới hạn |
| Quỹ | `(phiếu Hạt Mít + phiếu người chơi) × 900.000` (thuế 10%) |
| Chia | Quỹ ÷ tổng phiếu phe đúng, mỗi người nhận `số phiếu × phần chia` |
| Thời điểm chia | **Sau mỗi vòng** người chơi thắng (phe người chơi thắng) hoặc khi người chơi thua (phe Hạt Mít thắng) — không phải "khi trận đấu kết thúc" như text |

Menu "Top 100" ở map 112 không có xử lý (chọn không làm gì).

---

## 12. Giải Siêu Hạng (super_rank)

Files: `npc_list/TrongTai.java` (map 113), `matches/dai_hoi_vo_thuat/SuperRankService.java`, `SuperRank.java`, `managers/SuperRankManager.java` (tick 500 ms), `database/SuperRankDAO.java`, `player/SuperRank.java`, `consts/ConstSuperRank.java`. Bảng DB `super_rank` (id, player_id, name, rank, last_pk_time, last_reward_time, ticket, win, lose, history, info) – dump có **1.346 dòng**.

### 12.1 Menu Trọng tài

| Nút | Hành động |
|---|---|
| Top 100 Cao Thủ | `topList(type 0)` – 100 người có số hạng nhỏ nhất (hạng 1–100); từ danh sách này **không thách đấu được** ("Không thể thách đấu ở Top 100") |
| Hướng dẫn thêm | `ConstNpc.THONG_TIN_SIEU_HANG` |
| Miễn phí (còn N vé) / Thi đấu | `topList(type 1)` – 11 người có hạng ≤ max(hạng mình, 10), sắp xếp giảm dần (tức những người ngay trên mình); thách đấu → nếu khu hiện tại bận thì vào hàng chờ, xong thì đấu ngay trong khu |
| Ưu tiên đấu ngay | `topList(type 2)` – đấu ở khu trống đầu tiên của map 113 |
| Về Đại Hội Võ Thuật | Về map 52 |

### 12.2 Luật

| Mục | Giá trị |
|---|---|
| Thời gian | 24/7 |
| Hạng khởi đầu | Lần đầu vào game: hạng = hạng lớn nhất hiện có + 1 (`SuperRankDAO.getRank`, insert trong `Controller`) |
| Ràng buộc đối thủ | Chỉ đấu người có **số hạng nhỏ hơn** (cao hơn) mình; nếu đối thủ top < 10 thì chênh lệch tối đa 2 hạng; không đấu chính mình/người đang đấu/đang chờ |
| Đối thủ | Bản sao (`boss/sieu_hang/Rival`) dựng từ dữ liệu người chơi kia (load từ DB) |
| Diễn biến | 5 giây giới thiệu, 180 giây thi đấu; thua khi chết/rời khu/hết giờ; thắng khi bản sao chết hoặc rời map |
| Kết quả hạng | Thắng: **đổi hạng** với đối thủ. Thua: giữ nguyên |
| Vé | Mặc định 3; mỗi ngày +1 (tối đa 3) khi `loadData` phát hiện đã qua nửa đêm so với `last_pk_time` |
| Phí thực tế trong code (`updateResults`) | Thua còn vé: −1 vé. Thua hết vé: −3 ngọc (nếu gem > 0). Thắng khi hết vé: −2 ngọc. Thắng còn vé: không mất vé. (Text hướng dẫn ghi "hết vé trả 1 ngọc") |
| Thông báo | Lên top ≤ 10 → thông báo toàn server |

### 12.3 Thưởng hằng ngày

`Player.update()` → nếu `Util.isAfterMidnight(superRank.lastRewardTime)` thì `SuperRank.reward()` (tức lần update đầu tiên sau nửa đêm khi online; text "chốt ngẫu nhiên 20h–23h" không khớp code):

| Hạng | Ngọc/ngày |
|---|---|
| 1 | 100 (kèm cập nhật nhiệm vụ danh hiệu "Cao thủ siêu hạng") |
| 2–10 | 20 |
| 11–100 | 5 |
| 101–1000 | 1 |
| > 1000 | 0 |

---

## 13. Giftcode

Files: `npc_list/Santa.java` (nút nhập giftcode; vị trí nút thay đổi tùy người chơi có vé giảm giá), `services_func/Input.java` (`GIFT_CODE = 501`, form "Gift-code"), `services/GiftCodeService.java`, `managers/GiftCodeManager.java`, `player_system/GiftCode.java`, `player/GiftCode.java`, load trong `server/Manager.java`.

### 13.1 Cơ chế

| Bước | Chi tiết |
|---|---|
| Load | Khi khởi động: `SELECT * FROM giftcode`; `count_left = -1` → coi như 999.999.999 lượt. `detail` là JSON `[{id, quantity, options:[{id,param}]}]`. Thêm code mới vào DB cần **restart** |
| So khớp | `code.equals(...)` – phân biệt hoa/thường |
| Hết lượt | `countLeft <= 0` → "Giftcode đã hết" |
| Mỗi nhân vật 1 lần | Danh sách code đã dùng lưu trong cột `player.giftcode` (JSON) → "Tham lam!" |
| Ô trống | Cần ≥ số dòng trong `detail` |
| Trừ lượt | `countLeft -= 1`, `UPDATE giftcode SET count_left` ngay, thêm code vào danh sách của nhân vật |
| Hạn dùng | `GiftCode.timeCode()` so sánh `datecreate > expired` (**không so với thời điểm hiện tại**). Kiểm tra hạn diễn ra **sau khi** đã trừ lượt |
| Phát quà | id `-1`: vàng (trần 2.000.000.000); `-2`: ngọc (trần 200.000.000); `-3`: ruby "ngọc khóa" (trần 200.000.000); id khác: item với options trong DB, số lượng `quantity`. Riêng Thỏi vàng (457) nếu thuộc type 0–5 sẽ không được thêm (457 là type 27 nên vẫn thêm bình thường) |
| Kết quả | Bảng thông báo "Bạn vừa nhận được: …" |

Admin có hàm `GiftCodeManager.checkInfomationGiftCode` liệt kê code, lượt còn, ngày tạo/hết hạn.

### 13.2 Danh sách code trong DB

Bảng `giftcode` có **1 code**:

| id | code | count_left | datecreate | expired | Quà |
|---|---|---|---|---|---|
| 1 | `tanthu` | 9003 | 2025-06-08 13:02:44 | 2030-01-01 06:12:53 | 50 Thỏi vàng (457); 10 Cuồng nộ (381); 10 Bổ huyết (382); 10 Bổ khí (383); 10 Giáp Xên bọ hung (384); 10 Ẩn danh (385) — tất cả có option 30 "Không thể giao dịch". Cần 6 ô trống |

Cột `datecreate` có `ON UPDATE current_timestamp()` → mỗi lần có người nhập code (UPDATE `count_left`) thì `datecreate` trong DB bị đổi thành thời điểm đó. Hệ quả: sau ngày `expired`, lần restart kế tiếp code mới bị coi là hết hạn (và chỉ nếu đã có người dùng sau ngày hết hạn).

---

## 14. Quà đăng nhập / điểm danh / quà miễn phí hằng ngày

### 14.1 Reset hằng ngày (`services/PlayerService.dailyLogin`)

Chỉ được gọi khi **load nhân vật lúc đăng nhập** (`database/MrBlue.java`). Nếu ngày hiện tại > ngày `firstTimeLogin`:

| Reset | Tác dụng |
|---|---|
| `BadgesTaskService.createAndResetTask` | Nhiệm vụ danh hiệu |
| `DailyGiftService.addAndReset` | Tạo lại 2 mục `DailyGiftData`: id 0 `NHAN_NGOC_MIEN_PHI`, id 1 `NHAN_BUA_MIEN_PHI` (`daNhan=false`), lưu cột `dailyGift` |
| `event.luotNhanNgocMienPhi = 1` | Lượt nhận ngọc miễn phí |
| `event.luotNhanCapsuleBang = 1` | Lượt điểm danh bang |
| `lastCheckIn = null` | Điểm danh Bò Mộng |
| Sau 5/7 hằng năm | Reset VIP mua trong mùa (xem 19-vip-nap-tien-tien-te.md) |

Người chơi online xuyên qua nửa đêm **không** được reset các lượt này cho tới khi đăng nhập lại (riêng Bò Mộng tự so ngày nên vẫn điểm danh được).

### 14.2 Các phần quà

| Quà | Nơi nhận | Điều kiện | Nội dung | File |
|---|---|---|---|---|
| Điểm danh ngày | Bò Mộng (map 47 Rừng Karin / 84 Siêu Thị) → "Điểm danh" | 1 lần/ngày (so `lastCheckIn` với ngày hiện tại) | **10.000 ngọc** + **100 Thỏi vàng** (không giao dịch) | `npc_list/BoMong.java` |
| Bùa 1 giờ miễn phí | Bà Hạt Mít (map 42, 43, 44, 84) → "Thưởng Bùa 1h ngẫu nhiên" | `DailyGift` id 1 chưa nhận | Ngẫu nhiên bùa id 213–219 (Bùa Trí Tuệ … Bùa Thu Hút), thời hạn 60 phút. Mỗi 5 phút có text nhắc | `npc_list/BaHatMit.java`, `Player.sendTextTimeDaiLyGift` |
| Ngọc miễn phí khi đánh quái | Quái đầu tiên bị giết trong ngày tại map 0–163 | `luotNhanNgocMienPhi == 1` | Rơi 2 × Ngọc (77) số lượng 1 (= 2 ngọc) | `mob/Mob.java` |
| Điểm danh bang | Giu-ma Đầu Bò → "Điểm danh +1 Capsule Bang" | Có bang, `luotNhanCapsuleBang == 1` | Bang +1 `capsuleClan`; bản thân +1 memberPoint, +1 clanPoint | `npc_list/GiuMaDauBo.java` (xem 17-bang-hoi.md) |
| Thưởng Siêu Hạng | Tự động | Xem §12.3 | 1–100 ngọc | `player/SuperRank.java` |

`DailyGiftService` có mục `NHAN_NGOC_MIEN_PHI` (id 0) nhưng không có code nào kiểm tra/cập nhật mục này. Lớp `server/AutoUpdate_NhanQuaFree.java` (hẹn giờ gọi `ServerManager.resetNhanQuaHangNgay()` – đặt lại cột `checkNhanQua` qua JDBC cứng `jdbc:mysql://localhost:3306/ngocrong`, user `root`) **không được khởi tạo ở đâu**.

Không tìm thấy cơ chế "quà online theo thời gian" (tích phút online) trong code.

---

## 15. Ghi chú / điểm cần lưu ý

**Lỗi / rủi ro kinh tế nghiêm trọng**

1. **Con số may mắn nhân thưởng**: `ResetGame` gọi `strFinish(g.id)` cho **mỗi vé** trong danh sách; mỗi lần gọi `strFinish` lại tìm vé trúng và cộng thưởng. Người trúng có N vé trong ván sẽ nhận thưởng N lần. Mua đủ 10 số: xác suất trúng 10/101, nhận 10 × 90 = 900 thỏi vàng → kỳ vọng ~89 thỏi cho 10 thỏi bỏ ra (bản ngọc tương tự: 4.500 ngọc cho 50 ngọc). **Có thể bị khai thác** nếu NPC được mở.
2. **Điểm danh Bò Mộng** cho 10.000 ngọc + 100 thỏi vàng mỗi ngày — rất lớn so với các nguồn khác (Siêu hạng top 1 chỉ 100 ngọc/ngày).
3. **Thách đấu PVP** không trừ cược lúc bắt đầu (lỗi thứ tự khởi tạo `ThachDau`), người thắng nhận 80% "từ không khí" → vàng được sinh thêm mỗi trận.
4. **Vòng quay may mắn**: `goldNeed = count * PRICE_GOLD` là `int`; với `count ≥ 9` sẽ tràn số (âm) → có thể quay mà không bị trừ/đúng số vàng (phụ thuộc client gửi `count`).
5. **Chọn ai đây bản vàng** dùng `int` cho tổng quỹ: vượt ~2,147 tỷ (≈2.147 vé Thường hoặc 214 vé VIP) sẽ tràn số.
6. **Siêu hạng**: `updateResults` load người thắng/thua từ DB (`MrBlue.loadById`, bản sao offline) rồi trừ ngọc/vé và `SuperRankDAO.updatePlayer` ghi đè DB; dữ liệu của người chơi đang online trong RAM có thể ghi đè ngược lại khi lưu → phí ngọc có thể không thực sự bị trừ. Cần kiểm chứng.

**Không khớp giữa text hiển thị và code**

7. Con số may mắn: text "8h–21h59, 0–99, 5 phút/lượt, 1.000.000 vàng" ↔ code "24/7, 0–100, ~60 giây, 1 thỏi vàng".
8. Chọn ai đây: text "6 giải, tối đa 10 lần" ↔ code 2 giải/loại tiền, không giới hạn; bản ngọc ghi "hồng ngọc" nhưng cộng ngọc xanh.
9. ĐHVT thế giới: text thưởng mỗi vòng 2/4/6/8 ngọc hoặc 10.000 vàng ↔ code chỉ +1 ngọc/+1 vàng (kèm vàng rơi 10.000–1.000.000).
10. Siêu hạng: text "chốt 20h–23h", "hết vé trả 1 ngọc" ↔ code trao sau nửa đêm, thua −3 ngọc / thắng −2 ngọc khi hết vé.
11. Võ đài Sinh Tử: text "thỏi vàng" ↔ code trừ vàng (0, 10, 20…); thông báo thiếu tiền ghi "ngọc".
12. Hùng Vương: menu đổi dưa ghi "thỏi" ↔ code cộng ngọc; hiển thị cây 6 giờ ↔ cây thật 24 giờ.
13. Gói Hộp đựng quà ghi "Giá vàng 2.000.000" ↔ code không trừ vàng.

**Tính năng không hoạt động / thiếu dữ liệu**

14. NPC Lý Tiểu Nương (54), Nồi bánh (66), Bulma Tết (106), Trung thu (41) không có trong `map_template` → minigame, nấu bánh, shop `BULMA_EVENT` không truy cập được. Shop `QDDN`, `BULMA_EVENT` không có code mở.
15. Các manager boss sự kiện (`*EventManager`) không được start thread → boss sự kiện (kể cả Thủy Tinh/Sơn Tinh của Hùng Vương đang bật) có thể không hoạt động.
16. Nguyệt thần/Nhật thần rơi item 2123/2124 không có trong `item_template` → có thể gây lỗi khi rơi đồ nếu bật Trung thu.
17. Bảng `event` (sự kiện 8/3) không có trong dump; dữ liệu load được cũng không được dùng.
18. Danh sách `nguoiDaTrong` của Hùng Vương không reset → tối đa 10 lượt trồng dưa cho tới khi restart; phiên trồng tính từ lúc server khởi động, không theo giờ thực.
19. Nguồn Ngà voi/Cựa gà/Hồng mao, Tem Mai An Tiêm, nguyên liệu bánh, Pháo bông: **không xác định**.
20. Top Kem trái cây luôn rỗng: `Manager` không load `Topsukien2` lúc khởi động và `ServerManager.updateTop` gán nhầm vào `Topsukien1` bằng `queryTopsukien1`. Không có code trao giải đua top.
21. `NoiBanh` chỉ kiểm tra có item, không kiểm tra đủ số lượng trước khi trừ.

**Vận hành**

22. Bật/tắt sự kiện chỉ bằng sửa code `EventManager.init()` rồi build lại; cờ boolean hiện đều `true` nên không có tác dụng.
23. Giftcode thêm/sửa trong DB cần restart server; hạn dùng thực chất không được kiểm tra theo ngày hiện tại (xem §13).
24. `dailyLogin` chỉ chạy khi đăng nhập → người treo máy qua đêm cần thoát/vào lại để nhận bùa miễn phí, ngọc miễn phí, điểm danh bang.
25. `ConSoMayMan*.dataKQ_CSMM` lưu lịch sử kết quả không giới hạn (tăng ~1.440 phần tử/ngày mỗi bản) cho tới khi restart.
