# 18 — NPC (Non-Player Character)

> Tài liệu mô tả toàn bộ hệ thống NPC của server Ngọc Rồng Online (Teamobi2026): cơ chế khởi tạo, lớp nền, cây đậu thần và **menu từng NPC**.
> Nguồn: `SRC/src/nro/models/npc/**`, `SRC/src/nro/models/npc_list/**` (59 file), `SRC/src/nro/models/consts/ConstNpc.java`, `SRC/src/nro/models/event/{VuaHung,NoiBanh,XeNuocMia}.java`, `SRC/src/nro/models/server/MenuController.java`, `SRC/src/nro/models/map/service/NpcManager.java` và DB dump `database team2026.sql` (bảng `npc_template`, `map_template.npcs`, `shop`, `item_template`).
> Tài liệu chỉ đọc code, không sửa. Chỗ nào không xác định được từ code/DB sẽ ghi rõ **(không xác định)**.
>
> Tham chiếu chéo: bang hội → `17-bang-hoi.md`; phó bản/bản đồ (Doanh trại, BDKB, Khí gas, CĐRĐ, Mabư, NRSĐ) → `14-ban-do-pho-ban.md`; sự kiện/minigame/giải đấu (ĐHVT, Siêu hạng, Võ đài sinh tử, Con số may mắn, Chọn ai đây, Hùng Vương...) → `16-su-kien-minigame-giai-dau.md`; VIP/nạp tiền → `19-vip-nap-tien-tien-te.md`; shop/vật phẩm, nâng cấp đồ (CombineService), rồng thần, boss, nhiệm vụ → các tài liệu tương ứng trong `docs/`.

## Mục lục

1. [Bảng tổng hợp NPC](#1-bảng-tổng-hợp-npc)
2. [Kiến trúc: NpcFactory, Npc, BaseMenu, MenuController](#2-kiến-trúc)
3. [Cây đậu thần (MagicTree + DauThan)](#3-cây-đậu-thần-magictree)
4. [Trứng Mabư / Dưa hấu (MabuEgg, DuaHauEgg) và NonInteractiveNPC](#4-trứng-mabư--dưa-hấu-và-noninteractivenpc)
5. [NPC ảo: Con Mèo (5) và Rồng Thiêng (24)](#5-npc-ảo-con-mèo-5-và-rồng-thiêng-24)
6. [Chi tiết menu từng NPC](#6-chi-tiết-menu-từng-npc)
7. [NPC sự kiện nằm ngoài npc_list](#7-npc-sự-kiện-nằm-ngoài-npc_list)
8. [NPC không có class riêng](#8-npc-không-có-class-riêng)
9. [Ghi chú / điểm cần lưu ý](#9-ghi-chú--điểm-cần-lưu-ý)

---

## 1. Bảng tổng hợp NPC

- **ID** = hằng số trong `ConstNpc` = `npc_template.id`.
- **Tên** = `npc_template.NAME`.
- **Map (DB)** = các map có NPC này trong cột `map_template.npcs` (định dạng `[npcId, x, y]`), tên map tra theo `map_template.NAME`. NPC được sinh ra tại `Map.java:172` qua `NpcFactory.createNPC(...)` cho mọi zone của map.
- "—" ở cột Map = không có trong `map_template` (NPC không được spawn tự động).

| ID | Hằng `ConstNpc` | Tên (DB) | Class | Map (DB) |
|---|---|---|---|---|
| 0 | ONG_GOHAN | Ông Gôhan | `OngGohan` | 21 Nhà Gôhan |
| 1 | ONG_PARAGUS | Ông Paragus | `OngParagus` | 23 Nhà Broly |
| 2 | ONG_MOORI | Ông Moori | `OngMoori` | 22 Nhà Moori |
| 3 | RUONG_DO | Rương đồ | `RuongDo` | 21, 22, 23 |
| 4 | DAU_THAN | Đậu thần | `DauThan` (+`npc/MagicTree`) | 21, 22, 23 |
| 5 | CON_MEO | Con mèo | NPC ảo trong `NpcFactory.createNpcConMeo` | — (mapId = -1) |
| 6 | KHU_VUC | Khu vực | mặc định (anonymous `Npc`) | 1, 2, 3, 4, 6–12, 14–18, 20, 24–26, 28–38, 78, 122–124, 164 |
| 7 | BUNMA | Bunma | `Bulma` | 0 Làng Aru, 78 Lãnh địa Fize, 84 Siêu Thị |
| 8 | DENDE | Dende | `Dende` | 7 Làng Mori, 84 Siêu Thị |
| 9 | APPULE | Appule | `Appule` | 14 Làng Kakarot, 84 Siêu Thị |
| 10 | DR_DRIEF | Dr. Brief | `DrDrief` | 24 Trạm tàu vũ trụ, 84 Siêu Thị |
| 11 | CARGO | Cargo | `Cargo` | 25 Trạm tàu vũ trụ |
| 12 | CUI | Cui | `Cui` | 19 Thành phố Vegeta, 26 Trạm tàu vũ trụ, 68 Thung lũng Nappa |
| 13 | QUY_LAO_KAME | Quy Lão Kame | `QuyLaoKame` | 5 Đảo Kamê |
| 14 | TRUONG_LAO_GURU | Trưởng lão Guru | `TruongLaoGuru` | 13 Đảo Guru |
| 15 | VUA_VEGETA | Vua Vegeta | `VuaVegeta` | 20 Vách núi đen |
| 16 | URON | Uron | `Uron` | 24, 25, 26 Trạm tàu vũ trụ, 84 Siêu Thị |
| 17 | BO_MONG | Bò Mộng | `BoMong` | 21, 22, 23, 47 Rừng Karin, 84 Siêu Thị |
| 18 | THAN_MEO_KARIN | Thần mèo Karin | `Karin` | 46 Tháp Karin |
| 19 | THUONG_DE | Thượng Đế | `ThuongDe` | 45 Thần điện, 78 Lãnh địa Fize, 141 Con đường rắn độc |
| 20 | THAN_VU_TRU | Thần Vũ Trụ | `ThanVuTru` | 48 Hành tinh Kaio, 78 Lãnh địa Fize |
| 21 | BA_HAT_MIT | Bà Hạt Mít | `BaHatMit` | 5 Đảo Kamê, 42 Vách núi Aru, 43 Vách núi Moori, 44 Vách núi Kakarot, 84 Siêu Thị, 112 Võ đài Hạt Mít |
| 22 | TRONG_TAI | Trọng tài | `TrongTai` | 13 Đảo Guru, 113 Đại hội võ thuật (2 lần) |
| 23 | GHI_DANH | Ghi danh | `GhiDanh` | 42, 43, 44, 52 Đại hội võ thuật (2 lần), 129 Đại hội võ thuật (2 lần) |
| 24 | RONG_THIENG | Rồng Thiêng | NPC ảo `NpcFactory.createNpcRongThieng` | — |
| 25 | LINH_CANH | Lính canh | `LinhCanh` | 27 Rừng Bamboo |
| 26 | DOC_NHAN | Độc Nhãn | `DocNhan` | 57 Tầng 4 |
| 27 | RONG_THIENG_NAMEC | Rồng Thần Namec | mặc định | — |
| 28 | CUA_HANG_KY_GUI | Cửa hàng ký gửi | `KyGui` | 84 Siêu Thị |
| 29 | RONG_OMEGA | Rồng Omega | `RongOmega` | 24, 25, 26 Trạm tàu vũ trụ |
| 30 | RONG_2S | Rồng 2 sao | `Rong2Sao` (kế thừa `Rong1Sao`) | 86 Hành tinh Polaris |
| 31 | RONG_3S | Rồng 3 sao | `Rong3Sao` | 87 Hành tinh Cretaceous |
| 32 | RONG_4S | Rồng 4 sao | `Rong4Sao` | 88 Hành tinh Monmaasu |
| 33 | RONG_5S | Rồng 5 sao | `Rong5Sao` | 89 Hành tinh Rudeeze |
| 34 | RONG_6S | Rồng 6 sao | `Rong6Sao` | 90 Hành tinh Gelbo |
| 35 | RONG_7S | Rồng 7 sao | `Rong7Sao` | 91 Hành tinh Tigere |
| 36 | RONG_1S | Rồng 1 sao | `Rong1Sao` | 85 Hành tinh M-2 |
| 37 | BUNMA_TL | Bunma (tương lai) | `BulmaTuongLai` | 102 Nhà Bunma |
| 38 | CALICK | Ca Lích | `Calick` | 27 Rừng Bamboo, 102 Nhà Bunma (di chuyển động, xem mục 6) |
| 39 | SANTA | Santa | `Santa` | 5 Đảo Kamê, 13 Đảo Guru, 20 Vách núi đen |
| 40 | MABU_MAP | Mabư mập | mặc định | — |
| 41 | TRUNG_THU | Trung thu | mặc định | — |
| 42 | QUOC_VUONG | Quốc Vương | `QuocVuong` | 43 Vách núi Moori |
| 43 | TO_SU_KAIO | Tổ Sư Kaio | `ToSuKaio` | 50 Thánh địa Kaio, 116 Thánh địa Kaio |
| 44 | OSIN | Ôsin | `Osin` | 50, 52, 114, 115, 116, 117, 118, 119, 120, 127, 154, 155, 165 |
| 45 | KIBIT | Kibit | `Kibit` | 50, 52, 114, 116 |
| 46 | BABIDAY | Babiđây | `Babiday` | 114, 115, 117, 118, 119, 120 |
| 47 | GIUMA_DAU_BO | Giu-ma Đầu Bò | `GiuMaDauBo` | 153 Lãnh địa Bang Hội |
| 48 | NGO_KHONG | Ngộ Không | mặc định | 122 Ngũ Hành Sơn |
| 49 | DUONG_TANG | Đường Tăng | mặc định | 78 Lãnh địa Fize, 122, 123 Ngũ Hành Sơn (+ `LunarNewYear` tạo tại map 0) |
| 50 | QUA_TRUNG | Quả trứng | `QuaTrung` (+`npc/MabuEgg`) | 21, 22, 23 |
| 51 | DUA_HAU | Dưa hấu | `DuaHau` (+`npc/DuaHauEgg`) | 21, 22, 23 |
| 52 | HUNG_VUONG | Hùng Vương | `event/VuaHung` | 183 Thành cổ 1, 184 Thành cổ 2, 185 Đấu trường thành cổ |
| 53 | TAPION | Tapion | `Tapion` | 19 Thành phố Vegeta, 126 Thành phố Santa |
| 54 | LY_TIEU_NUONG | Lý Tiểu Nương | `LyTieuNuong` | — |
| 55 | BILL | Bill | `Bill` | 48 Hành tinh Kaio, 154 Hành tinh Bill |
| 56 | WHIS | Whis | `Whis` | 48 Hành tinh Kaio, 154 Hành tinh Bill |
| 57 | CHAMPA | Champa | mặc định | — |
| 58 | VADOS | Vados | `Vados` | — |
| 59 | TRONG_TAI_2 | Trọng tài | mặc định | — |
| 60 | GOKU_SSJ | Goku SSJ | `GokuSSJ` | 80 Núi khỉ vàng, 131 Hành Tinh Yardart |
| 61 | GOKU_SSJ_2 | Goku SSJ | `GokuSSJ2` | 133 Hành Tinh Yardart 3 |
| 62 | POTAGE | Potage | `Potage` | 140 Hang động Potaufeu |
| 63 | JACO | Jaco | `Jaco` | 24 Trạm tàu vũ trụ, 139 Hành tinh Potaufeu |
| 64 | DAI_THIEN_SU | Thiên Sứ Whis | `DaiThienSu` | 78 Lãnh địa Fize, 145 Võ Đài Siêu Cấp |
| 65 | YARIROBE | Yarirobe | mặc định | — |
| 66 | NOI_BANH | Nồi bánh | `event/NoiBanh` | — |
| 67 | MR_POPO | Mr Popo | `MrPoPo` | 0 Làng Aru |
| 68 | PANCHY | Panchy | mặc định | — |
| 69 | THO_DAI_CA | Thỏ Đại Ca | mặc định | — |
| 70 | BARDOCK | Bardock | `Bardock` | 160 Khu hang động |
| 71 | (không có hằng) | Berry | mặc định | 160 Khu hang động |
| 72 | CAY_NEU | Đặc Cầu | mặc định | — |
| 74 | TORIBOT | Tori-Bot | `ToriBot` | 0 Làng Aru, 7 Làng Mori, 14 Làng Kakarot |
| 75 | EVENT | Thỏ Đỏ ChiChi | mặc định | — |
| 81 | CHI_CHI | Chi Chi | `ChiChi` | 5 Đảo Kamê |
| 82 | RUONG_SUU_TAM | Rương Sưu Tầm | mặc định | 102 Nhà Bunma |
| 83 | DR_MYUU | Dr. Myuu | `DrMyuu` | — |
| 84 | XE_NUOC_MIA | Xe nước mía | `event/XeNuocMia` | 0 Làng Aru, 7 Làng Mori, 14 Làng Kakarot |

Các id còn lại trong `npc_template` (73 Fide, 76 Granola, 77 Quả trứng linh thú, 78 Ông già Noel, 79 Cây thông Noel, 80 Npc, 103 Chú Bé Đần, 104 Khá BảnH, 105 Tiến Bry, 106 Bulma Tết Nguyên Đán, 107 Bill Bí Ngô, 108 Heart, 109 Bulma Bunny, 110 Bunma Rực Rỡ) không có hằng `ConstNpc`, không có class và không xuất hiện trong `map_template` → không dùng trong code hiện tại.

---

## 2. Kiến trúc

### 2.1. `NpcFactory.createNPC(mapId, status, cx, cy, tempId)` — `npc/NpcFactory.java`

- Lấy `avatar` từ `Manager.NPC_TEMPLATES.get(tempId).avatar` (cột `npc_template.avatar`).
- `switch (tempId)` ánh xạ **hằng `ConstNpc` → class**: 62 nhánh `case` (59 class trong `npc_list/` + 3 class trong `event/`: `VuaHung`, `NoiBanh`, `XeNuocMia`).
- `default` → tạo `Npc` ẩn danh: `openBaseMenu` gọi `super.openBaseMenu` (menu mặc định), `confirmMenu` rỗng.
- Lỗi khởi tạo → `Logger.logException(..., "Lỗi load npc")` và trả `null`.
- Gọi từ `Map.java:172` (khi load map từ `map_template.npcs`) và `event/Event.java:26` (`createNpc(mapId, npcId, x, y)` cho sự kiện, ví dụ `LunarNewYear` tạo Đường Tăng (49) tại map 0, x=850, y=432).
- Hai NPC "ảo" được tạo riêng (mapId = -1): `createNpcRongThieng()` và `createNpcConMeo()` (gọi tại `Manager.java:178`), xem mục 5.
- Cả 59 file trong `npc_list` đều có nhánh trong `createNPC`; tuy vậy `Vados`, `DrMyuu`, `LyTieuNuong` không được spawn trong `map_template`, còn `DaiThienSu` được spawn nhưng không có menu (mục 9).

### 2.2. `Npc` (abstract, `npc/Npc.java`) implements `IAtionNpc`

| Thành phần | Mô tả |
|---|---|
| Trường | `mapId, map, status, cx, cy, tempId, avartar, baseMenu, indexChat, timeChat, lastChatTime`. Constructor tự thêm NPC vào `Manager.NPCS`. |
| `initBaseMenu(text)` | Phân tích chuỗi dạng `?npcSay|menu1|menu2` (`<>` → xuống dòng) thành `BaseMenu`. Không thấy nơi gọi trong các class NPC. |
| `createOtherMenu(player, indexMenu, npcSay, menus...)` | Gán `player.idMark.indexMenu = indexMenu` rồi gửi message **32** (short npcId, UTF npcSay, byte số lựa chọn, các UTF). Bản overload có `Object` lưu thêm vào `NpcFactory.PLAYERID_OBJECT`. |
| `openBaseMenu(player)` | Mặc định: nếu `baseMenu != null` → `BaseMenu.openMenu`; nếu không → "Ta có thể giúp gì cho ngươi ?" + 1 lựa chọn "Từ chối". Gán `indexMenu = ConstNpc.BASE_MENU` (31072002). |
| `npcChat(player / zone / text)` | Message **124**: NPC nói với 1 người / 1 zone / toàn bộ zone của map. |
| `canOpenNpc(player)` | (1) Nếu là **Đậu thần (4)**: chỉ cho mở khi người chơi đứng ở map 21/22/23, ngược lại "Không thể thực hiện". (2) Nếu cùng map **và** (khoảng cách ≤ 60px **hoặc** map không phải map Ngọc Rồng Sao Đen) → hợp lệ, `idMark.setNpcChose(this)`. (3) Nếu là **Lý Tiểu Nương (54)** → luôn hợp lệ. (4) Còn lại: "Không thể thực hiện khi đứng quá xa". |

`IAtionNpc` chỉ có 2 hàm: `openBaseMenu(Player)` và `confirmMenu(Player, int select)`.

`BaseMenu` (`npc/BaseMenu.java`): `npcId, npcSay, menuSelect[]`, `openMenu(player)` gửi message 32.

### 2.3. Luồng mở menu — `server/MenuController.java`

- `openMenuNPC(session, idnpc, player)`: hủy giao dịch; nếu là **Calick** và người chơi không ở map 102 → lấy NPC Calick toàn cục (`NpcManager.getNpc`); nếu là **Lý Tiểu Nương** → lấy NPC toàn cục; còn lại lấy NPC trong map hiện tại. Không có → `hideWaitDialog`.
- `doSelectMenu(player, npcId, select)`: `RONG_THIENG` và `CON_MEO` luôn lấy NPC toàn cục; Calick/Lý Tiểu Nương như trên; còn lại theo map → `npc.confirmMenu(player, select)`. Logic menu con được phân nhánh bằng `player.idMark.getIndexMenu()`.

### 2.4. Ẩn NPC theo điều kiện — `map/service/NpcManager.getNpcsByMapPlayer`

| NPC | Bị ẩn khi |
|---|---|
| Quả trứng (50) | `player.mabuEgg == null` và đang ở nhà mình (map 21 + gender) |
| Dưa hấu (51) | `player.DuaHauEgg == null` và đang ở nhà mình |
| Calick (38) | Nhiệm vụ < `TASK_21_0` (43008) |
| Quốc Vương (42) | Sức mạnh < 17.000.000.000 |

### 2.5. Cửa hàng ký gửi (ConsignShop)

NPC 28 (`KyGui`) chỉ là cổng mở `ConsignShopService.gI().openShopKyGui(pl)` (package `shop_ky_gui`: `ConsignItem`, `ConsignShopManager`, `ConsignShopService`). Các điểm lấy từ code:
- Chỉ tài khoản đã **kích hoạt thành viên** (`session.actived`) mới mở được.
- Mua: trừ `goldSell` vàng hoặc `gemSell` ngọc (`ConsignShopService.java:132-142`).
- Người bán nhận tiền trừ **10% phí** (`:305-310`: vàng trả dưới dạng item `tvAdd` với `quantity = goldSell - 10%`, ngọc cộng thẳng `gemSell - 10%`).
- "Up top" vật phẩm (menu Con Mèo `UP_TOP_ITEM` = 527): kiểm tra `gem >= 50` nhưng chỉ trừ **5 ngọc**, `isUpTop += 1`.
- Text hướng dẫn trong NPC: "Chỉ với 5 ngọc, giá trị ký gửi 10k-200Tr vàng hoặc 2-2k ngọc" (giới hạn giá thực tế không kiểm tra trong file NPC — chi tiết xem tài liệu vật phẩm/shop).

### 2.6. Bảng `shop` (DB) — NPC sở hữu shop

| id | npc_id | tag_name | type_shop | Được mở bởi |
|---|---|---|---|---|
| 1 | 7 | BUNMA | 0 | Bulma |
| 2 | 8 | DENDE | 0 | Dende |
| 3 | 9 | APPULE | 0 | Appule |
| 4 | 16 | URON | 0 | Uron |
| 5 | 39 | SANTA_HEAD | 0 | Santa (Tiệm hớt tóc) |
| 6/7/8 | 21 | BUA_1H / BUA_8H / BUA_1M | 0 | Bà Hạt Mít |
| 9 | 39 | SANTA | 0 | Santa |
| 10 | 37 | BUNMA_FUTURE | 0 | Bunma tương lai (map 102) |
| 11 | 55 | BILL | 0 | Bill (nhánh không tới được — mục 9) |
| 12 | 39 | SANTA_RUONG | 0 | không NPC nào mở |
| 13 | 39 | SANTA_HSD | 0 | không NPC nào mở |
| 14 | 44 | OSIN | 0 | không NPC nào mở |
| 15 | 37 | BUNMA_LINHTHU | 0 | không NPC nào mở |
| 16 | 18 | KARIN | 3 | Bunma tương lai (chỉ nhánh map 104/5) |
| 17 | 37 | BULMA_TL | 0 | không NPC nào mở |
| 19 | 103 | CHUBEDAN | 3 | không NPC nào mở |
| 20 | 56 | THIEN_SU | 3 | Whis |
| 21 | 106 | BULMA_EVENT | 3 | không NPC nào mở |
| 22 | 39 | SANTA_MO_RONG_HANH_TRANG | 0 | Santa |
| 23 | 39 | SANTA_HAN_SU_DUNG | 0 | Santa |
| 25 | 13 | QUY_LAO | 1 | Quy Lão / Guru / Vua Vegeta (Học kỹ năng) |
| 26 | 39 | SANTA_DANH_HIEU | 0 | Santa |
| 29 | 13 | QDDN | 3 | không NPC nào mở |
| 30 | 39 | SANTA_GIAM_GIA_1 | 0 | Santa (khi có Phiếu giảm giá) |
| 31 | 39 | SHOP_VIP | 3 | Santa (nhánh không tới được — mục 9) |
| 32 | 75 | DOI_SKILL_DE | 3 | không NPC nào mở |
| 33 | 81 | SHOP_CHI_CHI | 0 | Chi Chi |
| 34 | 13 | SHOP_DOI_DIEM | 3 | Quy Lão Kame |
| 35 | 47 | SHOP_CLAN | 3 | Giu-ma Đầu Bò |
| 36 | 55 | SHOP_SU_KIEN_VL | 3 | Bill |

Hai "shop" đặc biệt không có trong DB, xử lý cứng trong `ShopService.opendShop`: `ITEMS_DABAN` (mua lại đồ đã bán, `openShopType8`) và `ITEMS_LUCKY_ROUND` (rương phụ vòng quay, `openShopType4`). `ShopService.getShop` tìm theo `tag_name`, **không kiểm tra `npc_id`**.

---

## 3. Cây đậu thần (MagicTree)

Nguồn: `npc/MagicTree.java`, `npc_list/DauThan.java`. Đậu thần là NPC 4 đặt ở nhà (map 21/22/23), dữ liệu cây lưu theo người chơi (`player.magicTree`, cột `player.data_magic_tree`).

### 3.1. Công thức

| Đại lượng | Công thức / hằng |
|---|---|
| Cấp tối đa | `MAX_LEVEL = 10` |
| Số đậu tối đa | `(level - 1) * 2 + 5` |
| Thời gian ra 1 hạt | `level * 60` giây |
| Vàng nâng cấp | `PEA_UPGRADE[level-1][3]` × **1.000** (cấp ≤ 3, hiển thị "k") hoặc × **1.000.000** (cấp ≥ 4, hiển thị "Tr") |
| Thời gian nâng cấp | `PEA_UPGRADE[level-1]` = {ngày, giờ, phút} |
| Ngọc nâng cấp nhanh | `UPGRADE_GEM[level-1]` |
| Ngọc kết hạt nhanh | `HARVEST_GEM[level-1]` |
| Item đậu thu hoạch | `PEA_TEMP[level-1]` |
| Chỉ số gắn vào đậu | cấp 1–2: option 48 "HP, KI+#" với `PEA_PARAM`; cấp ≥ 3: option 2 "HP, KI+#000" với `PEA_PARAM` |

### 3.2. Bảng theo cấp (cấp hiện tại của cây)

| Cấp | Max đậu | Giây/hạt | Nâng lên cấp kế: thời gian | Vàng nâng cấp | Ngọc nâng nhanh | Ngọc kết hạt nhanh | Đậu nhận (item id – tên) | Hồi HP, KI |
|---|---|---|---|---|---|---|---|---|
| 1 | 5 | 60 | 10 phút | 5.000 | 20 | 1 | 13 – Đậu thần cấp 1 | +100 |
| 2 | 7 | 120 | 1 giờ 40 phút | 10.000 | 50 | 2 | 60 – Đậu thần cấp 2 | +500 |
| 3 | 9 | 180 | 16 giờ 40 phút | 100.000 | 120 | 5 | 61 – Đậu thần cấp 3 | +2.000 |
| 4 | 11 | 240 | 6 ngày 22 giờ | 1.000.000 | 300 | 7 | 62 – Đậu thần cấp 4 | +4.000 |
| 5 | 13 | 300 | 13 ngày 21 giờ | 10.000.000 | 800 | 9 | 63 – Đậu thần cấp 5 | +8.000 |
| 6 | 15 | 360 | 27 ngày 18 giờ | 20.000.000 | 1.500 | 12 | 64 – Đậu thần cấp 6 | +16.000 |
| 7 | 17 | 420 | 55 ngày 13 giờ | 50.000.000 | 3.000 | 15 | 65 – Đậu thần cấp 7 | +32.000 |
| 8 | 19 | 480 | 69 ngày 10 giờ | 100.000.000 | 6.000 | 20 | 352 – Đậu thần cấp 8 | +64.000 |
| 9 | 21 | 540 | 104 ngày 4 giờ | 300.000.000 | 7.500 | 25 | 523 – Đậu thần cấp 9 | +128.000 |
| 10 | 23 | 600 | (tối đa) | — | (10.000, không dùng được) | 30 | 595 – Đậu thần cấp 10 | +256.000 |

Ghi chú: "Hồi HP, KI" là giá trị option gắn vào item khi thu hoạch (`addPeaHarvest`); hiệu ứng khi ăn đậu nằm ở phần dùng item (ngoài phạm vi file này).

Hình cây theo hành tinh `ID_MAGIC_TREE[gender][level-1]`: Trái Đất 84–90, Namếc 371–377, Xayda 378–384 (cấp 7–10 dùng chung hình cấp 7). Vị trí cây `POS_MAGIC_TREE`: (348,336) / (372,336) / (348,336).

### 3.3. Cơ chế

- **`update()`**: nếu không đang nâng cấp và chưa đầy → cộng `(now - lastTimeHarvest)/giây_mỗi_hạt` hạt; đầy thì reset mốc thời gian. Nếu đang nâng cấp và đã đủ thời gian → `level++`, `isUpgrade = false`.
- **Menu cây (`openMenuTree`, message -34 sub 1)**:
  - Không nâng cấp: "Thu\nhoạch"; nếu level < 10: "Nâng cấp\n{d}d{h}h{m}'\n{vàng} k|Tr\nvàng"; nếu chưa đầy: "Kết hạt\nnhanh\n{HARVEST_GEM} ngọc". `indexMenu` = `MAGIC_TREE_NON_UPGRADE_LEFT_PEA` (chưa đầy) hoặc `MAGIC_TREE_NON_UPGRADE_FULL_PEA` (đầy).
  - Đang nâng cấp: "Nâng cấp\nnhanh\n{UPGRADE_GEM} ngọc", "Hủy\nnâng cấp\nhồi {vàng/2} k|Tr\nvàng". `indexMenu = MAGIC_TREE_UPGRADE`.
- **`DauThan.confirmMenu`** (gọi `TaskService.checkDoneTaskConfirmMenuNpc` trước):

| indexMenu | select | Hành động |
|---|---|---|
| NON_UPGRADE_LEFT_PEA | 0 | `harvestPea()` |
| | 1 | level = 10 → `fastRespawnPea()`; ngược lại → hỏi "Bạn có chắc chắn nâng cấp cây đậu?" (OK / Từ chối) |
| | 2 | `fastRespawnPea()` |
| NON_UPGRADE_FULL_PEA | 0 / 1 | Thu hoạch / hỏi xác nhận nâng cấp |
| MAGIC_TREE_CONFIRM_UPGRADE | 0 | `upgradeMagicTree()`: đủ vàng → trừ vàng, `isUpgrade = true`; thiếu → "Bạn không đủ vàng để nâng cấp, còn thiếu X vàng nữa" |
| MAGIC_TREE_UPGRADE | 0 | `fastUpgradeMagicTree()`: trừ `UPGRADE_GEM` ngọc, `level++` ngay |
| | 1 | hỏi "Bạn có chắc chắn hủy nâng cấp cây đậu?" |
| MAGIC_TREE_CONFIRM_UNUPGRADE | 0 | `unupgradeMagicTree()`: **hoàn lại 100% vàng** (menu ghi 1/2 — xem mục 9) |

- **Thu hoạch (`harvestPea` → `addPeaHarvest`)**: tạo item đậu theo cấp với số lượng = số hạt hiện có, thêm vào **hành trang**, phần dư đẩy tiếp vào **rương đồ** (`addItemBox`); hạt không chứa được sẽ ở lại trên cây. Thông báo "Bạn vừa thu hoạch được N hạt …".
- **Kết hạt nhanh (`fastRespawnPea`)**: trừ `HARVEST_GEM[level-1]` ngọc, đặt số hạt = max. Thiếu → "Bạn không đủ gem để kết hạt nhanh, còn thiếu …".
- **Hủy nâng cấp**: không bắt buộc đang trong thời gian nâng cấp; `isUpgrade = false`.

---

## 4. Trứng Mabư / Dưa hấu và NonInteractiveNPC

### 4.1. `MabuEgg` (npc/MabuEgg.java) — dùng bởi NPC Quả trứng (50)

- Thời gian ấp mặc định `DEFAULT_TIME_DONE = 864.000.000 ms` = **10 ngày**.
- `sendMabuEgg()`: message -122, npc id 50, icon 4664, số giây còn lại.
- `openEgg(gender)`: yêu cầu **đã có đệ tử** (nếu không: "Yêu cầu phải có đệ tử"); hủy trứng, `Thread.sleep(4000)`, rồi `PetService.createMabuPet` / `changeMabuPet` theo hành tinh chọn, đưa người chơi về map `gender*7` (0 Làng Aru / 7 Làng Mori / 14 Làng Kakarot).
- `destroyEgg()`: message -117 sub 101, `player.mabuEgg = null`.
- `subTimeDone(d,h,m,s)`: giảm thời gian ấp.

### 4.2. `DuaHauEgg` (npc/DuaHauEgg.java) — dùng bởi NPC Dưa hấu (51) và Hùng Vương (52)

- `TIME_DONE = 86.400.000 ms` = **24 giờ**. Icon theo thời gian còn lại: ≤ 6h → 4672, ≤ 12h → 4671, ≤ 18h → 4670, còn lại 4669 (gửi cho cả map).
- `openEgg()`: nếu chín → thêm **1 item 569 "Dưa Hấu"**, reset chu kỳ 24h (cây trồng lại liên tục); chưa chín → "Dưa hấu chưa chín, vui lòng đợi thêm."
- `plant()` ném `UnsupportedOperationException` (chưa cài đặt).

### 4.3. `NonInteractiveNPC` (npc/NonInteractiveNPC.java)

Là `Player` giả (không phải `Npc`), sinh ra ở mọi zone của map để làm đối thủ luyện tập / trang trí. Tự hồi sinh khi chết. Khỉ Bubbles di chuyển ngẫu nhiên x ∈ [250, 470] và chat "ù ù khẹc khẹc"/"khẹc khẹc"/"éc éc" (xác suất 2/3 mỗi 5 giây). Trọng Tài ở map 52 chat nội dung `WorldMartialArtsTournamentManager.chatText` mỗi 10 giây.

| Map | Tên | id | head/body/leg | HP | Vị trí |
|---|---|---|---|---|---|
| 45 Thần điện | Mr.PôPô | `BossID.MRPOPO` | 83/84/85 | 5.100 | (295, 408) |
| 46 Tháp Karin | Yajirô | `BossID.YAJIRO` | 77/78/79 | 1.100 | (320, 408) |
| 48 Hành tinh Kaio | Khỉ Bubbles | `BossID.KHI_BUBBLES` | 95/96/97 | 30.000 | (360, 240) |
| 51 Đấu trường | Trọng Tài | -114 | 114/115/116 | 500 | (383, 112) |
| 52 Đại hội võ thuật | Trọng Tài | -114 | 114/115/116 | 500 | (373 zone 0 / 301 zone khác, 336) |
| 129 Đại hội võ thuật | Trọng Tài | -114 | 114/115/116 | 500 | (385, 264) |
| 103 Võ đài Xên bọ hung | Trọng Tài | -114 | 114/115/116 | 500 | (401, 288) |
| 146 Tây Karin | Yajirô | -77 | 77/78/79 | 1.100 | (100, 336) |

---

## 5. NPC ảo: Con Mèo (5) và Rồng Thiêng (24)

Không có vị trí trên map (mapId = -1); các service dùng `NpcService.createMenuConMeo(...)` / `createMenuRongThieng(...)` để hiện menu hệ thống, còn lựa chọn được xử lý trong `NpcFactory`.

### 5.1. Rồng Thiêng — `NpcFactory.createNpcRongThieng().confirmMenu`

| indexMenu | Hành động |
|---|---|
| `SHOW_SHENRON_NAMEK_CONFIRM` (31720020) | `SummonDragonNamek.showConfirmShenron` |
| `SHENRON_NAMEK_CONFIRM` (31720021) | 0: `confirmWish()`; 1: `sendBlackGokuhesNamec` (chọn lại) |
| `SHOW_SHENRON_EVENT_CONFIRM` / `SHENRON_EVENT_CONFIRM` | Rồng sự kiện `player.shenronEvent` (xác nhận / chọn lại) |
| `SHENRON_CONFIRM` (501) | 0: `SummonDragon.confirmWish()`; 1: `reOpenShenronWishes` |
| `SHENRON_1_1` / `SHENRON_1_2` / `SHENRON_1_3` | Lựa chọn cuối cùng chuyển trang điều ước 1 sao (`SHENRON_1_STAR_WISHES_1` ↔ `_2`); lựa chọn khác rơi xuống `default` |
| default | `SummonDragon.showConfirmShenron` |

Chi tiết điều ước: xem tài liệu rồng thần.

### 5.2. Con Mèo — `NpcFactory.createNpcConMeo().confirmMenu` (avatar 351)

| indexMenu | Lựa chọn → hành động |
|---|---|
| `SUMMON_SHENRON_EVENT` (31720022) | 0: `Shenron_Service.summonShenron` |
| `MAKE_MATCH_PVP` (502) | `PVPService.sendInvitePVP(select)` |
| `MAKE_FRIEND` (503) | 0: chấp nhận kết bạn |
| 206783 | 0 form bot quái, 1 bot item, 2 bot boss, 3 bot tấn công người chơi |
| `CAPSULE_KICH_HOAT` | 0 Áo, 1 Quần, 2 Găng, 3 Giày, 4 Rada → `ItemService.OpenSKH(player, 1655, slot)` (item 1655 "Cápsule Kích hoạt 1 món tự chọn") |
| 900 | 0: form tìm người chơi |
| `REVENGE` (504) | 0: chấp nhận thù |
| `TUTORIAL_SUMMON_DRAGON` (505) / `SUMMON_SHENRON` (506) | Hướng dẫn / 1: `SummonDragon.summonShenron` |
| `MENU_OPTION_USE_ITEM726` (726) | 0: `SuperDivineWaterService.joinMapThanhThuy` |
| `MENU_SIEU_THAN_THUY` (2006) | 0: về map 46 Tháp Karin, x 300–400, y 408 |
| `TAP_TU_DONG_CONFIRM` (31720026) | 0: bay về vị trí offline cuối |
| `INTRINSIC` (507) | 0 xem nội tại, 1 mở nội tại, 2 mở nội tại VIP; `CONFIRM_OPEN_INTRINSIC(_VIP)` xác nhận |
| `CONFIRM_LEAVE_CLAN` (510) / `CONFIRM_NHUONG_PC` (511) / `CONFIRM_DISSOLUTION_CLAN` (517) | Rời bang / nhường phó chủ / giải tán bang (xóa DB, xóa khỏi `Manager.CLANS`) |
| `BAN_PLAYER` (513), `BUFF_PET` (514), `MENU_FIND_PLAYER` (516), `MENU_ADMIN` (512), `SUB_MENU` | Chức năng admin: ban, phát đệ tử, dịch chuyển tới/kéo người chơi, đổi tên, kick; `MENU_ADMIN`: 0 nhận ngọc rồng 14–20, 1 tạo đệ tử, 2 bảo trì sau 5 giây (chỉ `isAdmin`), 3 tìm người chơi, 4 danh sách boss |
| 671 | 0: xác nhận học kỹ năng: thời gian theo cấp sách = 15 phút / 30 phút / 1 giờ / 1 ngày / 3 ngày / 7 ngày / 15 ngày (cấp 1→7), trừ `LearnSkill.Potential` tiềm năng |
| `event3` | Đổi 99 Giấy màu (1505) → 1 Hộp đựng quà (1506) |
| `event3_1` | Cần 5 Socola (1507), 30 Hoa hồng giấy (1508), 1 Nơ trang trí (1509), 1 Hộp đựng quà (1506): 0 → Hộp quà nhẹ nhàng (1510), 1 → Hộp quà chỉn chu (1511, tốn thêm nơ) |
| `CONFIRM_REMOVE_ALL_ITEM_LUCKY_ROUND` (515) | 0: xóa hết rương phụ vòng quay |
| `CONFIRM_TELE_NAMEC` (522) | 0: dịch chuyển tới NR Namếc, trừ 10 ngọc |
| `MA_BAO_VE` (528) | 0: kích hoạt bảo vệ tài khoản (500.000 vàng lần đầu) hoặc bật/tắt |
| `UP_TOP_ITEM` (527) | Đẩy top ký gửi (xem 2.5) |
| `RUONG_GO` (531) | Hiển thị lần lượt vật phẩm nhận từ Rương gỗ |
| `MENU_XUONG_TANG_DUOI` (2007) | Đủ điểm Mabư và không ở map 120 → xuống tầng kế |
| `BUY_BACK` (533) | Đã comment, không làm gì |

---

## 6. Chi tiết menu từng NPC

Quy ước: **openBaseMenu** = menu gốc khi bấm vào NPC; **confirmMenu** = xử lý lựa chọn. Chỉ số `[n]` = vị trí `select`. "Nhiều NPC gọi `TaskService.checkDoneTaskTalkNpc`" = nếu người chơi đang có nhiệm vụ nói chuyện với NPC thì hoàn thành nhiệm vụ và **không** mở menu.

### 6.1. Ông Gôhan (0), Ông Paragus (1), Ông Moori (2)
File: `OngGohan.java`, `OngParagus.java`, `OngMoori.java` (2 class sau kế thừa `OngGohan`). Map 21/23/22.
- openBaseMenu: kiểm tra nhiệm vụ; nếu không → tutorial "Con cố gắng theo **Quy Lão Kame** / **Vua Vegeta** / **Trưởng Lão Guru** học thành tài, đừng lo lắng cho ta."
- confirmMenu: rỗng (case 0 không làm gì).

### 6.2. Rương đồ (3)
File: `RuongDo.java`. Map 21/22/23. openBaseMenu → `InventoryService.sendItemBox` (mở rương đồ). confirmMenu rỗng.

### 6.3. Đậu thần (4)
File: `DauThan.java`. Xem mục 3.

### 6.4. Bunma (7), Dende (8), Appule (9) — cửa hàng theo hành tinh
Files: `Bulma.java`, `Dende.java`, `Appule.java`.

| NPC | Hành tinh được mua | Câu từ chối | Menu |
|---|---|---|---|
| Bunma | Trái Đất (gender 0) | "Xin lỗi cưng, chị chỉ bán đồ cho người Trái Đất" | [0] "Cửa\nhàng" → shop `BUNMA` (allGender = true); [1] "Mua lại vật phẩm đã bán" (chỉ hiện khi `itemsDaBan` không rỗng) → `ITEMS_DABAN` |
| Dende | Namếc (gender 1) | "Xin lỗi anh, em chỉ bán đồ cho dân tộc Namếc" | [0] shop `DENDE`; [1] `ITEMS_DABAN` |
| Appule | Xayda (gender 2) | "Về hành tinh hạ đẳng của ngươi mà mua đồ cùi nhé…" | [0] shop `APPULE`; [1] `ITEMS_DABAN` |

**Dende – gọi Rồng Thần Namếc**: nếu `player.idNRNM != -1` (đang cầm ngọc rồng Namếc) **và** đứng ở map 7 → menu (indexMenu 1) "Ồ, ngọc rồng namếc, bạn thật là may mắn…":
- [0] "Hướng\ndẫn\nGọi Rồng" → `ConstNpc.HUONG_DAN_NRNM`.
- [1] "Gọi rồng" → điều kiện: cầm **Ngọc Rồng Namek 1 Sao (item 353)**; giờ 8h–22h (`getCurrHour() > 22 || < 8` bị chặn); đã nhặt ngọc ≥ 10 phút (600.000 ms) ("Ngọc bẩn quá…"); đủ 7 viên tại chỗ (`canCallDragonNamec`). Thành công → reset NR Namếc 24h (`tOpenNrNamec = now + 86.400.000`, `reInitNrNamec(86.399.000)`) và `SummonDragonNamek.summonNamec`.
- [2] "Từ chối".

### 6.5. Dr. Brief (10)
File: `DrDrief.java`. Map DB: 24, 84 (code còn nhánh map 153).

| Map | Menu | Hành động |
|---|---|---|
| 84 Siêu Thị | 1 nút theo hành tinh: "Đến\nTrái Đất" / "Đến\nNamếc" / "Đến\nXayda" | Mọi select → tàu vũ trụ tới map `gender + 24` (24/25/26) |
| 24 (mặc định) | Nếu nhiệm vụ chính id = 7: tutorial "Hãy lên đường cứu đứa bé nhà tôi…". Ngược lại: [0] "Đến\nNamếc" → map 25; [1] "Đến\nXayda" → map 26; [2] "Siêu thị" → map 84 | `changeMapBySpaceShip` |
| 153 Lãnh địa Bang Hội | Bang chủ: [0] "Chức năng\nbang hội", [1] "Nhiệm vụ Bang\n[left/5]", [2] "Đảo Kame", [3] "Từ chối". Thành viên: [0] Nhiệm vụ Bang, [1] Đảo Kame, [2] Từ chối. Không có bang: [0] Đảo Kame, [1] Từ chối | Xem dưới |

Map 153 chi tiết (`MAX_CLAN_TASK = 5`):
- "Chức năng bang hội" → menu 1: [0] "Đổi tên\ntên bang\nviết tắt" (`Input.createFormBangHoi`); [1] "Chọn ngẫu nhiên tên bang viết tắt" (2–4 ký tự, cần `canUpdateClan`); [2] "Nâng cấp Bang hội" → `MENU_CLAN_UP`: cần capsule bang theo `ClanService.capsule(clan)`: cấp 1→100, 2→300, 3→500, 4→700, 5→900, 6→1.100, 7→1.300, 8→1.500, 9→1.700, 10→1.900, 11→2.100; lên cấp: +1 thành viên tối đa, (cấp > 1) +1 ô rương bang, mở bùa bang cấp kế; chặn khi `level > 10`.
- "Nhiệm vụ Bang": chưa có → `TaskService.changeClanTask(random 0..5)`; đã xong → "Nhận\nthưởng" (thưởng `(level+1)*10` capsule bang, `payClanTask`); chưa xong → "OK" / "Hủy bỏ\nNhiệm vụ\nnày" → xác nhận `removeClanTask` (mất 1 lượt/ngày).
- "Đảo Kame" → tàu vũ trụ tới map 5.

### 6.6. Cargo (11)
File: `Cargo.java`. Map 25. Nhiệm vụ id 7 → tutorial như Dr. Brief. Menu: [0] "Đến\nTrái Đất" → 24; [1] "Đến\nXayda" → 26; [2] "Siêu thị" → 84.

### 6.7. Cui (12)
File: `Cui.java`. `COST_FIND_BOSS = 50.000.000` vàng.

| Map | Menu | Hành động |
|---|---|---|
| 26 Trạm tàu vũ trụ | [0] "Đến\nTrái Đất" → 24; [1] "Đến\nNamếc" → 25; [2] "Siêu thị" → 84 | tàu vũ trụ |
| 19 Thành phố Vegeta | Nhiệm vụ `TASK_19_0`: [0] "Đến chỗ\nKuku\n(50Tr vàng)", [1] "Đến Cold", [2] "Đến\nNappa", [3] "Từ chối". `TASK_19_1`: [0] "Đến chỗ\nMập đầu đinh…"; `TASK_19_2`: [0] "Đến chỗ\nRambo…". Mặc định: [0] "Đến Cold", [1] "Đến\nNappa", [2] "Từ chối" | "Đến chỗ boss": boss còn sống, không ở phó bản, zone chưa đầy → trừ 50Tr vàng và dịch chuyển tới boss; boss chết → "Chết rồi ba..."; "Đến Cold" → map 109 Rừng băng (y 295); "Đến Nappa" → map 68 Thung lũng Nappa (y 90) |
| 68 Thung lũng Nappa | "Ngươi muốn về Thành Phố Vegeta": [0] "Đồng ý" → map 19 (y 1100), [1] "Từ chối" | tàu vũ trụ |

Tất cả nhánh: nhiệm vụ chính id 7 → tutorial "Hãy lên đường cứu đứa bé…".

### 6.8. Quy Lão Kame (13)
File: `QuyLaoKame.java`. Map 5 Đảo Kamê.

**openBaseMenu** (sau kiểm tra nhiệm vụ), "Con muốn hỏi gì nào?":
- Nếu `player.canReward`: chỉ [0] "Giao\nLân con" → mọi select gọi `RewardService.rewardLancon`.
- Ngược lại: [0] "Nói\nchuyện"; [1] "Đổi điểm\nsự kiện\n[điểm]"; [2] "Nhận quà\nKOL"; [3] "Nhận quà\nKOL VIP"; [4] "Giao\nRùa con" (chỉ khi có item 874 Rùa con).

| Lựa chọn | Hành động |
|---|---|
| [0] Nói chuyện | (Nếu đang học kỹ năng và đã hết thời gian → hoàn tất học.) Menu 0 "Chào con, ta rất vui khi gặp con…": [0] "Nhiệm vụ" → tutorial tên subtask hiện tại; [1] "Học\nKỹ năng" → đang học: menu 12 (xem dưới), chưa học: shop `QUY_LAO`; có bang: [2] "Về khu\nvực bang" → map 153 (x 100–200, y 432); [3] "Kho báu\ndưới biển"; bang chủ: [4] "Giải tán\nBang hội" → "Con có chắc muốn giải tán bang hội không?" → Đồng ý → `Input.createFormGiaiTanBangHoi` |
| [1] Đổi điểm sự kiện | shop `SHOP_DOI_DIEM` |
| [2]/[3] Nhận quà KOL / KOL VIP | Hiện nhiệm vụ KOL theo `kolQuestStage` / `kolVIPQuestStage` (bảng dưới). Đủ tiến độ → "Nhận thưởng"/"Đóng"; nhận: trừ vật phẩm yêu cầu (loại thu thập), phát thưởng, tăng stage. KOL VIP cần có item **1825 "Vé Nhệm Vụ KOL Vip"** |
| [4] Giao Rùa con | "Cảm ơn cậu đã cứu con rùa của ta…" → "Nhận quà"/"Đóng" (indexMenu 1 **không có xử lý** — mục 9) |

**Menu 12 – đang học kỹ năng** ("Con đang học kỹ năng X cấp N, Thời gian còn lại …"): [0] "Học Cấp tốc {ngọc} ngọc" với `ngọc = 5 + (thời gian còn lại / 10 phút)` nếu ≥ 2 khoảng 10 phút, ngược lại 5 → trừ ngọc, học ngay; [1] "Huỷ" → hỏi "…nhận lại 50% số tiềm năng không?" (menu 13 **không có xử lý**); [2] "Bỏ qua".

**Kho báu dưới biển** (`BanDoKhoBau.POWER_CAN_GO_TO_DBKB = 2.000.000.000`):
- Bang đang có BDKB → `MENU_OPENED_DBKB`: [0] "Top\nBang hội" (`showTopClanBDKB`), [1] "Thành tích\nBang" (`showMyTopClanBDKB`), [2] "Đồng ý" → cần SM ≥ 2 tỷ (hoặc admin) → `goToDBKB` (tối đa 3 lần/ngày), [3] "Từ chối".
- Chưa có → `MENU_OPEN_DBKB`: [0] Top, [1] "Thành tích\nBang" (cũng gọi `showTopClanBDKB`), [2] "Chọn\ncấp độ" → SM ≥ 2 tỷ → `Input.createFormChooseLevelBDKB`; `MENU_ACCEPT_GO_TO_BDKB`: [0] `TreasureUnderSeaService.openBanDoKhoBau(level)`.

**Nhiệm vụ KOL** (item tra `item_template`):

| Bậc | Yêu cầu | Thưởng KOL | Thưởng KOL VIP |
|---|---|---|---|
| 1 | Thu thập 100 item 1778 "Cuốn chả giò" (quái doanh trại) | 5 × 1821 Trứng vàng rồng nhí | 10 × 1821 |
| 2 | Thu thập 10 item 1824 "Cậu Vàng" (text: chai cuke 2 lít, boss doanh trại) | 5 × 1592 Hộp quà Goku Day VIP, 5 × 1757 Hộp quà Cađíc VIP | 10 × 1592, 10 × 1757 |
| 3 | Hoàn thành Destron Gas cấp 70 × 20 lần | 1 × 1360 Cờ Ma đèn nhảy múa | 1 × 1360 |
| 4 | Thắng 10 trận đại hội võ thuật | 1 × 1654 Pet Cerberus | 1 × 1654 |
| 5 | Hoàn thành 30 nhiệm vụ siêu khó hàng ngày | 10 × 1822 Rada ngọc rồng | 20 × 1822 |
| 6 | Tham gia hạ boss Baby 5 lần | 1 × 1797 Cải trang Picolo, 5 × 1592, 5 × 1757 | 1 × 1797, 10 × 1592, 10 × 1757 |
| 7 | Hạ 100.000 quái bằng tự động luyện tập | 10 × 1592, 9.999 × 664 Xúc xích, 5 × 1757 | 20 × 1592, 10 × 1757 |

Option gắn cho thưởng: 1360 → (73), 77:13, 103:13, 50:13, 101:20, 30, 93:90 ngày; 1654 → (73), 50:16, 77:15, 103:15, 106, 30, 93:30 ngày; 1797 → (73), 50:25, 103:30, 30, 93:90 ngày.

### 6.9. Trưởng lão Guru (14) và Vua Vegeta (15)
Files: `TruongLaoGuru.java` (map 13), `VuaVegeta.java` (map 20). Chỉ tiếp **Namếc** / **Xayda**; sai hành tinh → "Con hãy về hành tinh của mình mà thể hiện".
- Menu "Chào con, ta rất vui khi gặp được con…": [0] "Nhiệm vụ" → tutorial tên subtask; [1] "Học\nKỹ năng" → giống Quy Lão (menu 12 học cấp tốc / huỷ; chưa học → shop `QUY_LAO`).

### 6.10. Uron (16)
File: `Uron.java`. Map 24, 25, 26, 84. Bấm là mở thẳng shop `URON` (allGender = false). Không có menu.

### 6.11. Bò Mộng (17)
File: `BoMong.java`. Chỉ hoạt động ở **map 47 Rừng Karin và 84 Siêu Thị** (ở nhà 21–23 không mở menu). Kiểm tra nhiệm vụ trước.

Menu "Ngươi muốn có thêm ngọc thì chịu khó làm vài nhiệm vụ sẽ được ngọc thưởng":
| [n] | Text | Hành động |
|---|---|---|
| 0 | "Nhiệm vụ\nhàng ngày" | Đang có: hiển thị tên/cấp/tiến độ, còn lại X/10 (`MAX_SIDE_TASK = 10`) → "Trả nhiệm\nvụ" (`paySideTask`) / "Hủy nhiệm\nvụ" (`removeSideTask`). Chưa có: "Dễ" / "Bình thường" / "Khó" → `changeSideTask(0/1/2)` / "Từ chối" |
| 1 | "Nhiệm vụ\nthành tích" | `AchievementService.openAchievementUI` |
| 2 | "Nạp Ngọc" | `Input.createFormTradeGem`: tỉ lệ 1 VNĐ (số dư `session.vnd`) = 1 ngọc, tối thiểu 10.000, tối đa 5.000.000 |
| 3 | "Điểm danh" | 1 lần/ngày (`lastCheckIn`): **+10.000 ngọc** và **100 × Thỏi vàng (457)** không giao dịch được |
| 4 | "Từ chối" | — |

### 6.12. Thần mèo Karin (18)
File: `Karin.java`. Map 46 Tháp Karin. Kiểm tra nhiệm vụ trước.

Thứ tự ưu tiên openBaseMenu:
1. Vừa thắng Siêu thần thủy trong ngày (`winSTT`) → "…Ta sẽ cho mi uống thuốc 'Tăng lực siêu thần thủy'" [0] "Đồng ý" → nhận item **727** (SM < 1 tỷ) hoặc **728** (SM ≥ 1 tỷ) "Siêu thần thủy", không giao dịch, HSD 1 ngày (cần 1 ô trống).
2. Con đường rắn độc: đã hạ hết quái và đã nói với Thần Mèo → "Hãy mau bay xuống chân tháp Karin" [OK]; chưa nói → "Cầm lấy hai hạt đậu cuối cùng của ta đây…" [0] "Cám ơn\nsư phụ" → hồi đầy HP/KI (1 lần), `talkToThanMeo = true`.
3. Luyện tập theo `levelLuyenTap`:

| levelLuyenTap | Menu | Hành động |
|---|---|---|
| 0 | [0] Đăng ký/Hủy tập tự động, [1] "Nhiệm vụ", [2] "Tập luyện\nvới\nThần Mèo", [3] "Thách đấu\nThần Mèo" | [1] NPC chat "..."; [2] → xác nhận (+20 SM/phút) → `TrainingService.callBoss(KARIN, false)`; [3] → xác nhận → `callBoss(KARIN, true)` (thắng được tập với Yajirô 40 SM/phút) |
| 1 | [0] tập tự động, [1] "Tập luyện với Yajirô", [2] "Thách đấu Yajirô" | [1] 40 SM/phút `callBoss(YAJIRO,false)`; [2] `callBoss(YAJIRO,true)` (thắng → 80 SM/phút) |
| ≥ 2 | [0] tập tự động, [1] "Tập luyện với Thần Mèo", [2] "Tập luyện với Yajirô" | [1] `callBoss(KARIN,false)`; [2] `callBoss(YAJIRO,false)` |

Đăng ký tập tự động (menu 2001, dùng chung cho Karin/Thượng Đế/Thần Vũ Trụ): [0] "Hướng\ndẫn\nthêm" → `ConstNpc.TAP_TU_DONG`; [1] "Đồng ý\n1 ngọc\nmỗi lần" → `dangKyTapTuDong = true`, `mapIdDangTapTuDong = mapId`; [2] "Không\nđồng ý". Tốc độ thực tế theo `TrainingService.getTnsmMoiPhut`: level 0→20, 1→40, 2→80, 3→160, 4→320, 5→640, ≥6→max(1.280, `tnsmLuyenTap`); offline > 30 phút mới tính, tối đa 86.400 giây (text menu ghi cố định "1280 sức mạnh mỗi phút").

### 6.13. Thượng Đế (19)
File: `ThuongDe.java`. Code xử lý map **45 Thần điện** và **141 Con đường rắn độc** (map 78 trong DB không có nhánh).

**Map 45**: nếu đang ở CĐRĐ đã hạ hết quái và chưa gặp Thượng Đế → "Hãy xuống gặp thần mèo Karin" [OK] (select 0 → `talkToThuongDe = true`). Ngược lại theo `levelLuyenTap`:

| levelLuyenTap | Menu |
|---|---|
| 2 | [0] tập tự động, [1] "Tập luyện\nvới\nMr.PôPô", [2] "Thách đấu\nMr.PôPô", [3] "Đến\nKaio", [4] "Quay ngọc\nMay mắn" |
| 3 | [0], [1] "Tập luyện\nvới\nThượng Đế", [2] "Thách đấu\nThượng Đế", [3], [4] |
| khác | [0], [1] "Tập luyện với Mr.PôPô", [2] "Tập luyện với Thượng Đế", [3], [4] |

- [1] level 3 → 160 SM/phút `callBoss(THUONG_DE,false)`; khác → 80 SM/phút `callBoss(MRPOPO,false)`.
- [2] level 2 → thách đấu `callBoss(MRPOPO,true)`; level 3 → `callBoss(THUONG_DE,true)` (thắng → 320 SM/phút); khác → `callBoss(THUONG_DE,false)`.
- [3] "Đến Kaio" → tàu vũ trụ tới map 48 Hành tinh Kaio (y 354).
- [4] "Quay ngọc May mắn" → `MENU_CHOOSE_LUCKY_ROUND`: [0] "Quay bằng\nvàng" (`LuckyRound.openCrackBallUI`), [1] "Vòng quay\nđặc biệt" (`openCrackBallVipUI`, cũng USING_GOLD), [2] "Rương phụ\n(N món)" → shop `ITEMS_LUCKY_ROUND`, [3] "Xóa hết\ntrong rương" → xác nhận (Con Mèo 515), [4] "Đóng".
- Lưu ý: nhánh xác nhận 2002/2003 **không kiểm tra `select`** — bấm "Không đồng ý" vẫn gọi boss (mục 9).

**Map 141**: "Hãy nắm lấy tay ta mau!" [0] "về\nthần điện" → cần bang đang có CĐRĐ và đã hạ hết quái ("Chưa hạ hết đối thủ") → dịch chuyển map 45 (295, 408).

### 6.14. Thần Vũ Trụ (20)
File: `ThanVuTru.java`. Chỉ xử lý **map 48 Hành tinh Kaio** (map 78 không có nhánh).

| levelLuyenTap | Menu |
|---|---|
| 4 | [0] tập tự động, [1] "Tập luyện\nvới\nBubbles", [2] "Thách đấu\nBubbles", [3] "Di chuyển" |
| 5 | [0], [1] "Tập luyện với Thần Vũ Trụ", [2] "Thách đấu Thần Vũ Trụ", [3] |
| khác | [0], [1] "Tập luyện với Bubbles", [2] "Tập luyện với Thần Vũ Trụ", [3] |

- [1] level 5 → 640 SM/phút `callBoss(THAN_VU_TRU,false)`; khác → 320 SM/phút `callBoss(KHI_BUBBLES,false)`.
- [2] level 4 → `callBoss(KHI_BUBBLES,true)` (thắng → 640); level 5 → `callBoss(THAN_VU_TRU,true)` (thắng → 1.280); khác → `callBoss(THAN_VU_TRU,false)`.
- [3] "Di chuyển" → "Ta sẽ đưa con đi": [0] "Về\nthần điện" → map 45; [1] "Thánh địa\nKaio" → map 50 (318, 336); [2] "Con\nđường\nrắn độc" → cần bang + kích hoạt thành viên → menu 2: [0] "Top\nBang hội", [1] "Thành tích\nBang" (cả hai `showTopClanCDRD`), [2] "Chọn\ncấp độ"/"Đồng ý" → cần vào bang ≥ 1 ngày → chưa có CĐRĐ: `Input.createFormChooseLevelCDRD`; đã có: `SnakeWayService.openConDuongRanDoc(0)`; [3] "Từ chối". Menu 3: [0] mở CĐRĐ với cấp đã chọn.

### 6.15. Bà Hạt Mít (21)
File: `BaHatMit.java`.

**Map 5 Đảo Kamê** — "Ngươi tìm ta có việc gì?":
| [n] | Text | Hành động |
|---|---|---|
| 0 | "Chức năng\npha lê" | Menu 3: [0] "Ép sao\ntrang bị" (`EP_SAO_TRANG_BI`), [1] "Pha lê\nhóa\ntrang bị" (`PHA_LE_HOA_TRANG_BI`), [2] "Nâng cấp\nSao pha lê", [3] "Đánh bóng\nSao pha lê", [4] "Cường hóa\nlỗ sao\npha lê", [5] "Tạo đá\nHematite" |
| 1 | "Chuyển hóa\nTrang bị" | Menu 4: [0] "Chuyển hóa\nVàng", [1] "Chuyển hóa\nNgọc" |
| 2 | "Võ đài\nSinh tử" | Sang map 112 (x 100–300, y 408) |
| 3 | "Phân rã\nTrang bị\nKích hoạt" | `PHAN_RA_TRANG_BI_KH` |
| 4 | "Tái tạo\nCapsule\nKích hoạt" | `TAI_TAO_CAPSULE_KH` |

`MENU_START_COMBINE` (menu do CombineService tạo) ở map 5: [0] `startCombine`, [1] `startCombineVip(player, 10)`, [2] `startCombineVip(player, 100)`. Chi phí/tỉ lệ: xem tài liệu nâng cấp đồ.

**Map 112 Võ đài Hạt Mít** (reset lượt sau nửa đêm: `haveRewardVDST = false`, `thoiVangVoDaiSinhTu = 0`):
- Có thưởng → "Đây là phần thưởng cho con." [0] "1 vệ tinh\nngẫu nhiên" → 1 item ngẫu nhiên 342–345 (Vệ tinh trí lực / trí tuệ / phòng thủ / sinh lực), HSD 30 ngày.
- Mình đang thi đấu: [0] "Top 100" (không làm gì), [1] "Đồng ý\nN thỏi vàng" → NPC chat "Không thể thực hiện", [2] "Từ chối", [3] "Về\nđảo rùa" → map 5 (x 1156).
- Người khác đang thi đấu: [0] "Top 100" (trống), [1] "Bình chọn" → `DAT_CUOC_HAT_MIT`: "Phí bình chọn là 1 triệu vàng… 90% tổng tiền chia cho phe đúng": [0] cho người thi đấu, [1] cho Hạt Mít (mỗi lần 1.000.000 vàng); [2] "Đồng ý N thỏi vàng" → `DeathOrAliveArenaService.startChallenge`; [3] Từ chối; [4] Về đảo rùa.
- Trống: [0] "Top 100" (trống), [1] "Đồng ý\nN thỏi vàng" → `startChallenge`, [2] Từ chối, [3] Về đảo rùa.

**Map 174, 181** (không tồn tại trong `map_template`): [0] "Quay về" → map 5, [1] "Từ chối".

**Map 42, 43, 44, 84** (nhánh default) — menu động:
| Vị trí logic | Text | Điều kiện hiện | Hành động |
|---|---|---|---|
| 0 | "Thưởng\nBùa 1h\nngẫu nhiên" | Còn lượt `DailyGift NHAN_BUA_MIEN_PHI` | Bùa ngẫu nhiên item 213–219 thêm 60 phút, 1 lần/ngày |
| 1 | "Sách\nTuyệt Kỹ" | luôn | Menu `MENU_SACH_TUYET_KY`: [0] "Đóng thành\nSách cũ" (`CheTaoCuonSachCu`), [1] "Đổi Sách\nTuyệt kỹ" (`DoiSachTuyetKy`; xác nhận [1] cần item 1794 "Con dấu"), [2] "Giám định\nSách", [3] "Tẩy\nSách", [4] "Nâng cấp\nSách\nTuyệt kỹ", [5] "Hồi phục\nSách", [6] "Phân rã\nSách" |
| 2 | "Cửa hàng\nBùa" | luôn | "Bùa\n1 giờ" → `BUA_1H`, "Bùa\n8 giờ" → `BUA_8H`, "Bùa\n1 tháng" → `BUA_1M`, "Đóng" |
| 3 | "Nâng cấp\nVật phẩm" | luôn | `NANG_CAP_VAT_PHAM` (xác nhận [1] → `NangCapVatPham.nangCapVatPham`) |
| 4 | "Nâng cấp\nBông tai\nPorata" / "Mở chỉ số\nBông tai\nPorata cấp\n2" | Có item 454 hoặc 921 (Bông tai Porata) | Có BT cấp 2 → `NANG_CHI_SO_BONG_TAI`, không → `NANG_CAP_BONG_TAI` |
| 5 | "Làm phép\nNhập đá" | luôn | `LAM_PHEP_NHAP_DA` |
| 6 | "Nhập\nNgọc Rồng" | luôn | `NHAP_NGOC_RONG` |
| 7 | "Nâng cấp\nBông tai\nPorata cấp\n3" / "Mở chỉ số…cấp\n3" | Có BT cấp 2 (hoặc 921) hoặc item 1819 (BT cấp 3) | Có 1819 → `NANG_CHI_SO_BONG_TAI3`; có BT2 → `NANG_CAP_BONG_TAI3` |

`confirmMenu` bù chỉ số: không còn lượt bùa miễn phí → `select++`; không có 454/921 và `select ≥ 4` → `select++`.

### 6.16. Trọng tài (22)
File: `TrongTai.java`. Map 113 Đại hội võ thuật (map 13 dùng menu mặc định).
- Đang chờ ghép trận (`SuperRankManager.awaiting`): "Vui lòng chờ, số thứ tự của bạn là N": [0] "OK", [1] "Về\nĐại Hội\nVõ Thuật" → map 52 (y 336).
- Bình thường ("Đại hội võ thuật Siêu Hạng diễn ra 24/7…"): [0] "Top 100\nCao Thủ" → `SuperRankService.topList(0)`; [1] "Hướng\ndẫn\nthêm" → `THONG_TIN_SIEU_HANG` (Top 1: 100 ngọc, Top 2–10: 20, Top 11–100: 5, Top 101–1000: 1; 1 vé miễn phí/ngày, tích tối đa 3; hết vé 1 ngọc/trận); [2] "Miễn phí\nCòn N vé" hoặc "Thi đấu" → `topList(1)` (danh sách 11 đối thủ quanh hạng); [3] "Ưu tiên\nđấu ngay" → `topList(2)`; [4] "Về Đại Hội Võ Thuật" → map 52.

### 6.17. Ghi danh (23)
File: `GhiDanh.java`.

**Map 42, 43, 44 (Vách núi) – Máy đấm**: "Tính điểm máy đấm nào các thí sinh…": [0] "Top 100\n Trái đất", [1] "Top 100\nNamek", [2] "Top 100\nXayda" (`Manager.Topmaydam`), [3] "Xem điểm" → "Điểm hiện tại của bạn là: {point_maydam}", [4] "Đóng".

**Map 52 – Đại hội võ thuật** (`WorldMartialArtsTournamentService.menu/confirm`):
- Đang trong vòng đấu → tutorial "Bạn được vào vòng N…".
- Đang cho đăng ký & có giải: [0] "Thông tin\nChi tiết" (`THONG_TIN_DAI_HOI_VO_THUAT`), [1] "Đăng kí"/"Hủy\nđăng kí" → xác nhận "Giải {tên}\n({gem} ngọc)" hoặc "(Xk vàng)" cho Ngoại hạng, [2] "Giải\nSiêu Hạng" → map 113, [3] "Đại Hội\nVõ Thuật\nLần thứ\n23" → map 129.
- Không đăng ký: [0] Thông tin, [1] "Giải Siêu Hạng" → map 113, [2] "ĐHVT lần 23" → map 129, [3] "Đóng".
- Lịch/lệ phí trong text: Nhi đồng 8,14,18h (2 ngọc); Siêu cấp 1 9,13,19h (4 ngọc); Siêu cấp 2 10,15,20h (6 ngọc); Siêu cấp 3 11,16,21h (8 ngọc); Ngoại hạng 12,17,22,23h (10.000 vàng); vô địch 5 đá nâng cấp.

**Map 129 – ĐHVT lần thứ 23**: sau nửa đêm reset `goldChallenge = 50.000`, `rubyChallenge = 2`, `levelWoodChest = 0`.
| [n] | Text | Hành động |
|---|---|---|
| 0 | "Hướng\ndẫn\nthêm" | `NPC_DHVT23` |
| 1 | "Thi đấu\n{rubyChallenge} ngọc" | Cần đã mở rương báu vật (`finditemWoodChest`), chưa vô địch (level ≠ 12), đủ **ngọc** → bắt đầu; sau mỗi lần: `goldChallenge ×2`, `rubyChallenge +2` |
| 2 | "Thi đấu\n{goldChallenge} vàng" | Như trên nhưng trừ vàng |
| 3 | "Nhận\nthưởng\nRương Cấp\nN" (khi level > 0) | Xác nhận "Phần thưởng của bạn đang ở cấp N / 12…" → 1 × item 570 "Rương Gỗ" option 72 = cấp, không giao dịch; reset cấp về 0 |
| 3/4 | "Về\nĐại Hội\nVõ Thuật" | map 52 (y 336) |

### 6.18. Lính canh (25) — Doanh trại độc nhãn
File: `LinhCanh.java`. Map 27 Rừng Bamboo. Hằng `RedRibbonHQ`: `N_PLAYER_CLAN = 5`, `N_PLAYER_MAP = 1`, `TIME_DOANH_TRAI = 1.800.000 ms` (30 phút).

Điều kiện theo thứ tự: có bang ("Chỉ tiếp các bang hội, miễn tiếp khách vãng lai") → kích hoạt thành viên → bang ≥ 5 thành viên → vào bang ≥ 1 ngày → nếu bang đang đánh: menu "Tham gia"/"Không"/"Hướng dẫn thêm" → cần ≥ 1 đồng đội cùng bang đứng trong x ∈ [1285, 1645] → mỗi bang 1 lần/ngày (thông báo tên người mở và giờ) và mỗi người 1 lần/ngày → menu "Vào\n(miễn phí)" / "Không" / "Hướng\ndẫn\nthêm".
- `MENU_JOIN_DOANH_TRAI`: [0] `RedRibbonHQService.joinDoanhTrai`, [2] `HUONG_DAN_DOANH_TRAI`.
- `IGNORE_MENU`: [1] hướng dẫn.

### 6.19. Độc Nhãn (26)
File: `DocNhan.java`. Map 57 Tầng 4. Chưa thắng → "Bọn mi đừng hòng thoát khỏi nơi đây". Đã thắng → tutorial "Ta chịu thua…" và (lần đầu) bật 5 phút tìm ngọc: `isTimePicking = true`, `randomNR()`, gửi đồng hồ. confirmMenu rỗng. Truy cập `player.clan.doanhTrai` không kiểm tra null (mục 9).

### 6.20. Cửa hàng ký gửi (28)
File: `KyGui.java`. Map 84. "Cửa hàng chúng tôi chuyên mua bán hàng hiệu…": [0] "Hướng\ndẫn\nthêm", [1] "Mua bán\nKý gửi" → cần kích hoạt thành viên ("Bạn chưa kích hoạt thành viên!!!") → `ConsignShopService.openShopKyGui`, [2] "Từ chối". Xem 2.5.

### 6.21. Rồng Omega (29) — Ngọc Rồng Sao Đen
File: `RongOmega.java`. Map 24, 25, 26. Giờ mở `BlackBallWar`: 20:00–21:00, được nhặt ngọc từ 20:30.
- Đang mở: nếu đã tới giờ nhặt và có thưởng còn hạn → [0] "Hướng\ndẫn\nthêm", [1] "Tham gia", [2] "Nhận\nthưởng", [3] "Từ chối"; ngược lại [0] Hướng dẫn, [1] Tham gia, [2] Từ chối.
- Chưa mở: có thưởng → [0] Hướng dẫn, [1] "Nhận\nthưởng", [2] Từ chối; không → [0] Hướng dẫn, [1] Từ chối.
- Hướng dẫn → `HUONG_DAN_BLACK_BALL_WAR` (1 sao: +21% sức đánh; 2: +35% HP; 3: 35% tấn công thành HP; 4: phản 35% sát thương; 5: +35% chí mạng; 6: KI +40%; 7: 14% né đòn).
- Tham gia → `ConstMap.CHANGE_BLACK_BALL` + mở tab chọn map.
- Nhận thưởng → danh sách "Nhận\nthưởng\nN sao" → `rewardBlackBall.getRewardSelect(select)`.

### 6.22. Rồng 1–7 sao (36, 30–35)
File: `Rong1Sao.java`; `Rong2Sao`…`Rong7Sao` kế thừa, không thêm gì. Map 85–91.
- Đang giữ ngọc sao đen → "Phù hộ"/"Từ chối" → menu `MENU_OPTION_PHU_HP`: [0] "x3 HP\n10Tr vàng", [1] "x5 HP\n30Tr vàng", [2] "x7 HP\n50Tr vàng", [3] "x3 SD\n10Tr vàng", [4] "x5 SD\n30Tr vàng", [5] "Từ chối" (`BlackBallWar.COST_X3/X5/X7`). Đã phù hộ → "Bạn đã được phù hộ rồi!".
- Không giữ ngọc → [0] "Về nhà" → map `21 + gender` (y 250); [1] "Từ chối" → NPC chat "Để ta xem ngươi trụ được bao lâu".

### 6.23. Bunma tương lai (37)
File: `BulmaTuongLai.java`. Kiểm tra nhiệm vụ trước.
- Map 102 Nhà Bunma: npcSay "learn", [0] "Cửa hàng" → shop `BUNMA_FUTURE`.
- Map 104 / 5 (không có trong DB): npcSay "build", [0] "Cửa hàng" → shop `KARIN`, [1] "Đóng".

### 6.24. Ca Lích (38)
File: `Calick.java`. Map DB 27, 102; chỉ xuất hiện với người có nhiệm vụ ≥ `TASK_21_0` (ẩn), mở menu cần ≥ `TASK_20_0` (40960). **Không gọi `canOpenNpc`**.
- Map 102: "Chào chú, cháu có thể giúp gì?" [0] "Kể\nChuyện" → `CALICK_KE_CHUYEN`; [1] "Quay về\nQuá khứ" → `goToQuaKhu` = tàu vũ trụ tới map 24.
- Map khác: [0] "Kể\nChuyện", [1] "Đi đến\nTương lai" → `goToTuongLai`: hiệu ứng 60 giây rồi (trong `Player.update`) tàu vũ trụ tới map 102 (x 60–200); [2] "Từ chối" → "Không thể thực hiện".
- Di chuyển: mỗi lần chọn "Đi đến Tương lai" tăng `count`; đủ `COUNT_CHANGE = 25` → Calick chuyển sang map ngẫu nhiên 27–29 (`MapService.getMapForCalich`), vị trí x ngẫu nhiên. Nếu NPC đã rời map → "Calích đã rời khỏi map!".

### 6.25. Santa (39)
File: `Santa.java`. Map 5, 13, 20 (confirm chỉ xử lý 3 map này). "Xin chào, ta có một số vật phẩm đặc biệt cậu có muốn xem không?"

| Không có Phiếu giảm giá (459) | Có Phiếu giảm giá (≥ 1) | Hành động |
|---|---|---|
| [0] "Cửa hàng" | [0] "Cửa hàng" | shop `SANTA` |
| — | [1] "Giảm giá\n80%" | shop `SANTA_GIAM_GIA_1` |
| [1] "Mở rộng\nHành trang\nRương đồ" | [2] | shop `SANTA_MO_RONG_HANH_TRANG` |
| [2] "Nhập mã\nquà tặng" | [3] | `Input.createFormGiftCode` |
| [3] "Cửa hàng\nHạn sử dụng" | [4] | shop `SANTA_HAN_SU_DUNG` |
| [4] "Tiệm\nHớt tóc" | [5] | shop `SANTA_HEAD` |
| [5] "Danh\nhiệu" | [6] | shop `SANTA_DANH_HIEU` |

Nhánh `case 6` khi không có phiếu mở `SHOP_VIP` nhưng menu chỉ có 6 mục → không bấm được.

### 6.26. Quốc Vương (42)
File: `QuocVuong.java`. Map 43. Chỉ hiện khi SM ≥ 17 tỷ. `MAX_LIMIT_CUSTOM = 5`, `OpenPowerService.COST_SPEED_OPEN_LIMIT_POWER = 50.000.000`. **openBaseMenu không gọi `canOpenNpc`**.
- "Con muốn nâng giới hạn sức mạnh cho bản thân hay đệ tử?": [0] "Bản thân", [1] "Đệ tử", [2] "Từ chối".
- Bản thân (limitPower < 5): "…mở giới hạn sức mạnh của bản thân lên {getPowerNextLimit}" → [0] "Nâng\ngiới hạn\nsức mạnh" (`openPowerBasic`), [1] "Nâng ngay\n50Tr vàng" (`openPowerSpeed`, trừ 50Tr vàng), [2] "Đóng". Đạt 5 → "Sức mạnh của con đã đạt tới giới hạn hiện tại".
- Đệ tử (có đệ, limitPower < 5): [0] "Nâng ngay\n50Tr **ngọc**" → thực tế trừ **50Tr vàng**; [1] "Đóng".

### 6.27. Tổ Sư Kaio (43)
File: `ToSuKaio.java`. Map 50, 116.
- "Tập luyện với Tổ sư Kaio sẽ tăng {getTnsmMoiPhut} sức mạnh mỗi phút…": [0] Đăng ký/Hủy tập tự động (menu 2001: "Đồng ý\n1 vàng\nmỗi lần"), [1] "Đồng ý\nluyện tập" → `callBoss(TO_SU_KAIO,false)`, [2] "Nâng\nGiới hạn\nSức mạnh", [3] "Không\nđồng ý".
- Nâng giới hạn (`MENU_NANG_GIOI_HAN`): [0] "Bản thân": limitPower ∈ [5, 9) → [0] `openPowerBasic`, [1] "Nâng ngay 50Tr vàng"; < 5 → "Yêu cầu đạt 50 tỷ sức mạnh"; ≥ `NPoint.MAX_LIMIT` (9) → "đã đạt tới giới hạn". [1] "Đệ tử": đệ limitPower ∈ [5, 9) → trừ 50Tr vàng `openPowerSpeed(pet)` trực tiếp (không qua xác nhận); [2] → NPC chat "Khi nào con cần thì quay lại gặp ta!".

### 6.28. Ôsin (44)
File: `Osin.java`. Gọi `checkDoneTaskTalkNpc` (không chặn menu).

| Map | Menu | Hành động |
|---|---|---|
| 50 Thánh địa Kaio | [0] "Đến\nKaio", [1] "Đến\nhành tinh\nBill", [2] "Từ chối" | [0] map 48 (354, 240); [1] map 154 (200, 312) |
| 154 Hành tinh Bill | [0] "Đến\nhành tinh\nngục tù", [1] "Từ chối" | map 155 (111, 792) |
| 155 Hành tinh ngục tù | [0] "Quay về", [1] "Từ chối" | map 154 (200, 312) |
| 165 Sa mạc hoang vu | "Mau hút năng lượng tà ác…": [0] "Về nhà" → map 52; [1] "Bùa hỗ\ntrợ" → "Đồng ý\n5 ngọc": x2 tốc độ hút 10 phút, cộng dồn tối đa 60 phút (`itemTime.isUseKilis`); [2] "Từ chối" | |
| 52 Đại hội võ thuật | Giờ Mabư 14h–15h (`isMabu14HOpen`) hoặc 12h–13h (`isMabuOpen`, `HOUR_OPEN_MAP_MABU = 12`): [0] "OK", ([1] "Bình hút năng lượng" nếu có item 1795), [cuối] "Từ chối". Ngoài giờ: "Vào lúc 12h tôi sẽ bí mật…" [0] "Ok", ([1] "Bình hút năng lượng") | OK: 14h → `MajinBuu14HService.joinMaBu2H`; 12h → map 114 (x 100–500). Bình hút năng lượng → "Cadic đã bị phù thủy Babidi thôi miên…" [0] "Ok" → map 165 |
| 114, 115, 117–120 (Mabư) | Chỉ phe `cFlag == 9`: [0] "Hướng\ndẫn\nthêm", ("Giải trừ\nphép thuật\n1 ngọc" nếu chưa dùng), ("Xuống\nTầng dưới" nếu đủ điểm và map ≠ 120) | **Không hoạt động** – xem mục 9 |
| 127 Cổng phi thuyền | Chưa phù hộ: [0] "Phù hộ\n10 ngọc" (+1Tr HP, +1Tr KI, +10k SĐ), [1] "Từ chối", [2] "Về\nĐại Hội\nVõ Thuật"; đã phù hộ: [0] Từ chối, [1] Về ĐHVT | **Không hoạt động** – xem mục 9 |
| khác (116) | menu mặc định | — |

### 6.29. Kibit (45)
File: `Kibit.java`.
- Map 50: [0] "Đến\nKaio" → map 48 (354, 240); [1] "Từ chối".
- Map 52: chỉ [0] "Từ chối".
- Map 114: chỉ phe `cFlag == 9`: [0] "Về nhà" → tàu vũ trụ map `21 + gender`; [1] "Từ chối".
- Map 116: không có menu (default rỗng → không phản hồi).

### 6.30. Babiđây (46)
File: `Babiday.java`. Map 114, 115, 117–120. Chỉ phe `cFlag == 10` ("Ngươi hãy về phe của mình mà thể hiện").
- "Bọn Kaiô do con nhóc Ôsin cầm đầu đã có mặt tại đây…" (indexMenu `GO_UPSTAIRS_MENU` = 10000): [0] "Hướng\ndẫn\nthêm" → `HUONG_DAN_MAP_MA_BU`; ("Giải trừ\nphép thuật\n1 ngọc" nếu chưa dùng) → bật `isUseGTPT` (sức đánh theo điểm tích lũy) — **không trừ ngọc**; ("Xuống\nTầng dưới" nếu `pointMabu ≥ POINT_MAX` và map ≠ 120) → `mapIdNextMabu`; "Về nhà" → map `21 + gender`. Code bù chỉ số theo trạng thái hiện/ẩn mục.

### 6.31. Giu-ma Đầu Bò (47)
File: `GiuMaDauBo.java`. Map 153 Lãnh địa Bang Hội. "Ngươi đang muốn tìm mảnh vỡ và mảnh hồn bông tai Porata…"

| [n] | Text | Hành động |
|---|---|---|
| 0 | "Khiêu chiến\nBoss" | Chỉ **bang chủ**, cần ≥ 3 thành viên bang online cùng zone → tạo mob **Gấu Tướng Cướp** (`ConstMob.GAU_TUONG_CUOP = 77`) HP 2.000.000.000, pDame 50, 0 tiềm năng, không giới hạn số lần |
| 1 | "Điểm danh\n+1 Capsule\nBang" | Có bang, `event.luotNhanCapsuleBang != 0` → +1 capsule bang, +1 memberPoint/clanPoint cho người điểm danh; lượt về 0 |
| 2 | "OK" | SM ≥ 40.000.000.000 → `player.type = 5`, `maxTime = 5`, `Service.Transport` (message -105). Đích đến do client xử lý **(không xác định từ server)**; thiếu SM → "KHÔNG ĐỦ SỨC MẠNH" |
| 3 | "Cửa Hàng\nBang hội" | shop `SHOP_CLAN` |
| 4 | "Từ chối" | — |

### 6.32. Quả trứng (50)
File: `QuaTrung.java`. Map nhà 21–23, chỉ đúng nhà của mình (`21 + gender`). `COST_AP_TRUNG_NHANH = 1.000.000.000` vàng.
- Chưa nở ("Bư bư bư..."): [0] "Hủy bỏ\ntrứng" → xác nhận → `destroyEgg`; [1] "Ấp nhanh\n1 Tỷ vàng" → trừ 1 tỷ, `timeDone = 0`; [2] "Đóng".
- Nở được: [0] "Nở" → "Bạn có chắc chắn cho trứng nở? Đệ tử của bạn sẽ được thay thế bằng đệ Mabư": [0] "Đệ mabư\nTrái Đất", [1] "Đệ mabư\nNamếc", [2] "Đệ mabư\nXayda", [3] Từ chối → `mabuEgg.openEgg(gender)`; [1] "Hủy bỏ\ntrứng"; [2] "Đóng".

### 6.33. Dưa hấu (51)
File: `DuaHau.java`. **Không gọi `canOpenNpc`**.
- Chín: "Dưa hấu đã chín, bạn có thể thu hoạch!" [0] "Thu hoạch" → `DuaHauEgg.openEgg()` (+1 Dưa Hấu 569), [1] "Từ chối".
- Chưa chín: "Dưa hấu đang lớn.\nCòn khoảng HH:MM:SS nữa sẽ chín." [0] "Đóng".
- `CO_THE_THU` = `CO_THE_THU_HOACH` = 501 nên nhánh xử lý khớp.

### 6.34. Tapion (53)
File: `Tapion.java`.
- Map 19: "Ác quỷ truyền thuyết Hirudegarn đã thoát khỏi phong ấn ngàn năm…" [0] "OK" → nếu giờ ∈ [1, 23) → map 126 Thành phố Santa (x 100–300, y 360); ngoài giờ → "Vui lòng quay lại vào lúc 22h"; [1] "Từ chối".
- Map 126: "Tôi sẽ đưa bạn về" [0] "OK" → map 19 (x 900–1100, y 360).

### 6.35. Lý Tiểu Nương (54) — Minigame
File: `LyTieuNuong.java`. Không có trong `map_template`; mở từ xa qua `MenuController` + `NpcManager.getNpc` (nếu không spawn thì `null` → không mở được). `canOpenNpc` luôn true với NPC này; confirm chỉ xử lý khi `this.mapId == 5`.

Menu "Mini game.": [0] "Kéo\nBúa\nBao", [1] "Con số\nmay mắn\nthỏi vàng", [2] "Con số\nmay mắn\nngọc xanh", [3] "Chọn ai đây", [4] "Đóng".
- **Kéo Búa Bao**: mức cược [0] 1 Tỷ, [1] 5 Tỷ, [2] 10 Tỷ vàng → chọn "Kéo"/"Búa"/"Bao"/"Hủy"; máy random; thắng +96% tiền cược, thua −100%, hòa không đổi. Lưu ý: bảng thắng thua trong `getResult` coi Kéo thắng Búa, Búa thắng Bao, Bao thắng Kéo.
- **Con số may mắn (thỏi vàng / ngọc xanh)**: hiện kết quả giải trước, lịch sử 10 kết quả, tổng giải, giây còn lại, số đã chọn; [0] "Cập nhật", [1] "1 Số\n{cost}" → form nhập số, [2] "Ngẫu nhiên\n1 số lẻ", [3] "Ngẫu nhiên\n1 số chẵn", [4] "Hướng\ndẫn\nthêm" (8h–21h59, tối đa 10 số 0–99, 5 phút/lượt), [5] "Đóng". `cost` lấy từ `MiniGame` (xem `16-su-kien-minigame-giai-dau.md`).
- **Chọn ai đây**: [0] "Thể lệ" (6 giải, tối đa 10 lần mỗi giải, 5 phút/lượt), [1] "Chọn\nVàng" → "Thường\n1 triệu\nvàng" (1.000.000) / "VIP\n10 triệu\nvàng" (10.000.000); [2] "Chọn\nngọc xanh" → "Thường\n10 ngọc\nxanh" / "VIP\n100 ngọc\nxanh" (text ghi thưởng là hồng ngọc). Hết thời gian lượt thì tự đặt lại +300.000 ms.

### 6.36. Bill (55)
File: `Bill.java`. Map 48, 154.
- Map 154: "..." [0] "Về\nthánh địa\nKaio" → map 50 (318, 336); [1] "Từ chối".
- Map 48: "Chưa tới giờ thi đấu, xem hướng dẫn để biết thêm chi tiết":
  - [0] "Nói\nchuyện" → nếu đủ 5 món đồ Thần + 99 thức ăn (`canOpenBillShop`): "Đói bụng quá… mang cho ta 99 phần đồ ăn ta sẽ cho một món đồ Hủy Diệt…" [OK]/[Từ chối]; ngược lại "Ngươi trang bị đủ bộ 5 món trang bị Thần và mang 99 phần đồ ăn tới đây…" [OK]. Shop `BILL` **không mở được** (mục 9).
  - [1] "Hướng\ndẫn\nthêm" → `HUONG_DAN_BILL` (ĐHVT liên vũ trụ: T2 6h, T3 13h, T4 15h, T5 17h, T6 18h, T7 12h, CN 10h; Top 10: 1 phiếu giảm giá + 1 capsule vàng; Top 1 thêm Rađa cấp 13).
  - [2] "Đổi thức ăn\nlấy phiếu ăn" → "Đồng ý" → mỗi 99 cái của từng loại 1798 Tayaki / 1799 Kẹo táo / 1800 Kem que đôi / 1801 Mochi / 1802 Ramen → 1 × **1805 Phiếu thức ăn**.
  - [3] "Đổi phiếu ăn\nlấy quà" → shop `SHOP_SU_KIEN_VL`.
  - [4] "Từ chối".

### 6.37. Whis (56)
File: `Whis.java`. Map 48, 154 (code còn map 164).
- Map 48: "Coming Soon" (không có nút).
- Map 164 Map riêng tư: [0] "Quay về" → `changeMapInYard` map 154 (x 758); [1] "Từ chối".
- Map 154: "Thử đánh với ta xem nào…":

| [n] | Text | Hành động |
|---|---|---|
| 0 | "Nói chuyện" | "Ta sẽ giúp ngươi chế tạo trang bị thiên sứ": [0] "Shop thiên sứ" → shop `THIEN_SU`; [1] "Chế tạo" → cần mặc đủ 5 món Hủy Diệt (`checkSetDes`) → `CHE_TAO_TRANG_BI_THIEN_SU` (xác nhận [0] `startCombine`); [2] "Từ chối" |
| 1 | "Học\ntuyệt kỹ" | Tuyệt kỹ theo hành tinh: Trái Đất **Super kamejoko**, Namếc **Ma phong ba**, Xayda **Ca đíc liên hoàn chưởng**. Giá **10.000.000 vàng + 99 ngọc**, SM ≥ 60 tỷ, cần item 1229 "Bí kíp tuyệt kỹ". Học mới: cần 9.999 bí kíp, tỉ lệ `isTrue(15,15)` = 100%. Nâng cấp: cần 999 bí kíp, skill `currLevel ≥ 1000`, point < 9; tỉ lệ 1/30; thành công trừ 999, thất bại trừ 99 bí kíp ("Ngu dốt!") |
| 2 | "Top 100" | `Manager.Topwhis` |
| 3 | "[LV:{top+1}]" | `TrainingService.callBoss(WHIS, false)` |

### 6.38. Vados (58)
File: `Vados.java`. Không override `openBaseMenu` → menu mặc định "Ta có thể giúp gì cho ngươi ?"/"Từ chối"; `confirmMenu` rỗng. Không có trong `map_template`.

### 6.39. Goku SSJ (60)
File: `GokuSSJ.java`.
- Map 80 Núi khỉ vàng: "Ta mới hạ Fide, nhưng nó đã kịp đào 1 cái lỗ…" [0] "Chuồn" → tàu vũ trụ map 131 (x 870).
- Map 131 Hành Tinh Yardart: "Đây là đâu? Xong cmnr" [0] "Bó tay", [1] "Về chỗ cũ" → map 80 (x 870).

### 6.40. Goku SSJ 2 (61)
File: `GokuSSJ2.java`. Map 133. "Hãy cố gắng luyện tập\nThu thập 9.999 bí kiếp để đổi trang phục Yardrat nhé!" [0] "Nhận\nthưởng" → cần tổng param option 31 của item 590 "Bí kiếp" ≥ 9.999 → trừ 9.999, nhận item `592 + gender` "Cải trang Yardrat" với Giáp+400, Phản 10% sát thương, Chí mạng+10%; [1] "OK". Thiếu thì không thông báo.

### 6.41. Potage (62)
File: `Potage.java`. Map 140.
- Đang có nhân bản của người chơi → "Đang có 1 nhân bản của X hãy chờ kết quả trận đấu" [OK].
- Ngược lại: "Hãy giúp ta đánh bại bản sao… 5 phút… Phần thưởng cho ngươi là 1 bình Commeson": [0] "Hướng\ndẫn\nthêm", [1] "OK" → 1 lần/ngày (admin bỏ qua) → `Service.callNhanBan`; [2] "Từ chối".

### 6.42. Jaco (63)
File: `Jaco.java`.
- Map 24: "Gô Tên, Calích và Monaka đang gặp chuyện ở hành tinh Potaufeu…" [0] "Đến\nPotaufeu" → tàu vũ trụ map 139 (x 60–200); [1] "Từ chối".
- Map 139: [0] "Đến\nTrái Đất" → 24, [1] "Đến\nNamếc" → 25, [2] "Đến\nXayda" → 26, [3] "Từ chối".

### 6.43. Thiên Sứ Whis / Đại Thiên Sứ (64)
File: `DaiThienSu.java`. Map 78, 145. `openBaseMenu` rỗng, `confirmMenu` rỗng → bấm không có phản hồi.

### 6.44. Mr Popo (67) — Khí gas hủy diệt (Destron Gas)
File: `MrPoPo.java`. Map 0 Làng Aru. "Thượng Đế vừa phát hiện ra 1 loại khí… Destron Gas…"
- Có bang: [0] "Thông tin\nChi tiết" (`HUONG_DAN_KHI_GAS_HUY_DIET`), [1] "Top 100\nBang hội", [2] "Thành tích\nBang" (cả hai `showTopClanKhiGas`), [3] "OK", [4] "Từ chối".
- Không bang: [0] Thông tin, [1] Top 100, [2] "OK", [3] "Từ chối" — nhưng select 2 lại gọi Top, select 3 không làm gì.
- "OK": vào bang ≥ 2 ngày; kích hoạt thành viên; bang đang có Khí gas → "Bang hội của cậu đang tham gia Destron Gas cấp độ N… cậu có muốn đi cùng họ không?" [Đồng ý] → `DestronGasService.openKhiGasHuyDiet(0)`; chưa có → chỉ bang chủ; bang ≥ `DestronGas.N_PLAYER_CLAN` (= **0**, text ghi 5) → `Input.createFormChooseLevelKGHD`.

### 6.45. Bardock (70)
File: `Bardock.java`. Map 160. "Người tìm ta có việc gì?" [0] "ok" → không làm gì (chỉ dùng cho nhiệm vụ nói chuyện).

### 6.46. Tori-Bot (74) — Mùa VIP
File: `ToriBot.java`. Map 0, 7, 14. **openBaseMenu không gọi `canOpenNpc`**. Mùa VIP: 05/06 00:00:00 → 05/12 23:59:59 của **năm hiện tại** (tính khi class load). Chi tiết quyền lợi VIP: `19-vip-nap-tien-tien-te.md`.
- Hết mùa: "Ngươi tìm ta có việc gì?" [Đóng]; confirm → "Mùa VIP đã kết thúc…".
- Trong mùa: thông tin mùa + thời gian còn lại + "(Bạn đã mua: N/4 lần)": [0] "Vip 1", [1] "Vip 2", [2] "Vip 3", [3] "Vip 4" → menu chi tiết với nút "{giá}\nđiểm mùa [vnd]" / "Đóng". **Mọi select trong menu 1–4 (kể cả "Đóng") đều gọi `BuyVip`**.
- `BuyVip`: tối đa 4 lần/mùa; trừ `session.vnd`; `pl.vip = max(vip, cấp)`; `timevip` +30 ngày; tạo đệ tử thường nếu chưa có (VIP1–3).

| Gói | Giá (vnd) | Vật phẩm thực tế trong code |
|---|---|---|
| VIP 1 | 50.000 | 200 × 457 Thỏi vàng; 10 × 459 Phiếu giảm giá; 1252 Ve Sầu Xên (50/77/103 = 10, HSD 30 ngày); 1248 Bọ Cánh Cứng (10, HSD 30); 1256 "Pet heo bướm" (10, HSD 30 – text ghi Búa hắc hường); 5 × 987 Đá bảo vệ |
| VIP 2 | 100.000 | 500 thỏi vàng; 10 phiếu; 1252 (12, HSD 30); 1248 (12, HSD 30); 1254 Búa hắc hường (12, HSD 30); 10 đá bảo vệ; 584 Cải trang (50/77 = 24, 117 = 15, HSD 30) |
| VIP 3 | 150.000 | 700 thỏi vàng; 10 phiếu; 1252, 1248, 1254 (12, vĩnh viễn); 30 đá bảo vệ; 584 (vĩnh viễn); 2 × 1655 Capsule kích hoạt; 10 × 956 "Mảnh Đội trưởng Vàng" |
| VIP 4 | 200.000 | 1.000 thỏi vàng; 10 phiếu; 1 × 568 Quả Trứng (đệ Mabư); 50 đá bảo vệ; 1554 Tàu ngầm 19 Cam (50/77/103 = 15, 14 = 10); 1771 Bé Rồng Cute (50/77 = 18, 5 = 18, 14 = 10, 236 = 15); 1772 búa sơn tinh (15, 236 = 15); 1557 Hắc Mị Nương (50/77/117/236 = 25); 5 × 1655; 20 × 1204 "Mảnh Rồng thần Namếc" |

### 6.47. Chi Chi (81)
File: `ChiChi.java`. Map 5 (confirm chỉ xử lý map 5). "Bạn muốn hỏi chi?"
- [0] "Top\nHộp quà\nthiếu nhi\n2025" → [0] Top 100 (`Manager.Topsukien`), [1] "Xem điểm" (`point_sukien`), [2] Đóng.
- [1] "Top\nNước mía" → Top `Topsukien1`, điểm `point_sukien1`.
- [2] "Top\nKem trái cây" → Top `Topsukien2`, điểm `point_sukien2`.
- [3] "Cửa hàng" → shop `SHOP_CHI_CHI`.
- [4] "Đóng".
Thời gian kết thúc/trao giải trong text là placeholder "(....)".

### 6.48. Dr. Myuu (83)
File: `DrMyuu.java`. Không có trong `map_template`. "Năm 740, ta tìm thấy các kí sinh trùng của King Tuffle…" [0] "Đồng ý" (không làm gì), [1] "Từ chối".

---

## 7. NPC sự kiện nằm ngoài npc_list

Được `NpcFactory` ánh xạ nhưng code ở `nro/models/event/`. Chi tiết sự kiện: `16-su-kien-minigame-giai-dau.md`.

### 7.1. Hùng Vương (52) — `event/VuaHung.java` (map 183–185)
- Phiên trồng dưa: mỗi 1 giờ tính từ lúc server khởi động, 15 phút đầu mở, tối đa 10 người/phiên (danh sách `nguoiDaTrong` không bao giờ bị xóa). Chưa có cây → "Trồng\nDưa Hấu" → `DuaHauEgg.createDuaHauEgg`.
- Menu chính: [0] "Đổi\nDưa hấu" → đổi Dưa Hấu (569) + Tem (1558) lấy **ngọc**: 1 quả + 1 tem → 5; 10 + 2 → 40; 20 + 3 → 120; 25 + 4 → 185; 30 + 5 → 250 (text nút ghi "thỏi"). [1] "Dâng\nsính lễ" (9 Ngà voi 1220, 9 Cựa gà 1221, 9 Hồng mao 1222 + 1.000.000 vàng → Hộp quà 1776). [2] "Dâng\nsính lễ\nxịn" (cùng vật phẩm + 10 ngọc → 1777). [3] "Dâng\nbánh dầy" (1 × 1542 → ngẫu nhiên 381–385, 1635). [4] "Dâng\nbánh chưng" (1 × 1556 → ngẫu nhiên 1150–1154, 1635, 1423, 1438, 1634).

### 7.2. Nồi bánh (66) — `event/NoiBanh.java` (không có trong map_template)
[0] "Tự nấu\nbánh" → [0] "Nấu\nBánh Dầy": trừ 99 × 1546, 10 × 1547, 10 × 1545, 1 × 1544 → 1 × 1542 (HSD 30 ngày); [1] "Nấu\nBánh Chưng": trừ 99 × 1546, 99 × 1548, 99 × 1549 → 1 × 1556 (HSD 30 ngày). Chỉ kiểm tra "có item" chứ không kiểm tra đủ số lượng.

### 7.3. Xe nước mía (84) — `event/XeNuocMia.java` (map 0, 7, 14)
[0] "Mua 1 ly\nnước mía": 5 Khúc mía (1612) + 2 Nước đá (1613) + 5.000.000 vàng → 1 item ngẫu nhiên 1614/1615/1616, +1 `point_sukien1`. [1] "Mua 10 ly\nnước mía": 50 + 20 + 50.000.000 vàng → 10 ly ngẫu nhiên, +10 điểm.

---

## 8. NPC không có class riêng

Đi vào nhánh `default` của `NpcFactory.createNPC`: menu "Ta có thể giúp gì cho ngươi ?" + "Từ chối", không xử lý lựa chọn.

| ID | Tên | Có spawn trong map_template |
|---|---|---|
| 6 | Khu vực | Có (hầu hết map thường, dùng làm điểm chọn khu) |
| 27 | Rồng Thần Namec | Không |
| 40 | Mabư mập | Không |
| 41 | Trung thu | Không |
| 48 | Ngộ Không | 122 |
| 49 | Đường Tăng | 78, 122, 123 (+ sự kiện Tết) |
| 57 | Champa | Không |
| 59 | Trọng tài | Không |
| 65 | Yarirobe | Không |
| 68 | Panchy | Không |
| 69 | Thỏ Đại Ca | Không |
| 71 | Berry | 160 |
| 72 | Đặc Cầu (CAY_NEU) | Không |
| 75 | Thỏ Đỏ ChiChi (EVENT) | Không |
| 82 | Rương Sưu Tầm | 102 |

---

## 9. Ghi chú / điểm cần lưu ý

### 9.1. Menu lỗi / nhánh không tới được
1. **Ôsin (44) ở map Mabư 114–120 và 127**: `case 114…120` và `case 127` bị đặt **lồng trong** `case 52 → switch (indexMenu)` của `Osin.confirmMenu`, trong khi `this.mapId` của Ôsin ở các map đó khác 52 và `indexMenu` là 10000 / BASE_MENU → "Giải trừ phép thuật", "Xuống tầng dưới", "Phù hộ 10 ngọc", "Về ĐHVT" **không bao giờ chạy**. (Phe Babiđây dùng `Babiday.java` thì chạy.)
2. **Bill (55)**: `confirmMenu` switch theo `mapId` có `case 2` (map 2) thay vì `indexMenu 2`; "Nói chuyện" đặt `indexMenu` 100/101 không ai xử lý → shop `BILL` (đồ Hủy Diệt) **không mở được**.
3. **Quy Lão Kame (13)**: "Giao Rùa con" tạo menu `indexMenu = 1` nhưng `confirmMenu` không có `case 1` → không nhận quà. Menu `13` ("Huỷ học kỹ năng… nhận lại 50% tiềm năng") không có xử lý ở cả Quy Lão, Guru, Vua Vegeta.
4. **Santa (39)**: nhánh `SHOP_VIP` (select 6 khi không có phiếu giảm giá) không hiển thị trên menu.
5. **Thượng Đế (19) / Thần Vũ Trụ (20)**: menu xác nhận 2002/2003 không kiểm tra `select` → bấm "Không đồng ý" vẫn gọi boss luyện tập. Hai NPC này cùng Thiên Sứ Whis (64) đặt ở map 78 nhưng code chỉ xử lý map 45/141 và 48 → **không có menu** ở map 78.
6. **Tori-Bot (74)**: ở menu chi tiết VIP, bấm "Đóng" (select 1) vẫn mua VIP vì `case 1..4` không kiểm tra `select`.
7. **Mr Popo (67)**: khi không có bang, nút "OK" (select 2) lại mở Top bang hội. `DestronGas.N_PLAYER_CLAN = 0` nên điều kiện "ít nhất 5 thành viên" không có tác dụng.
8. **Dr. Brief (10) map 153**: người chơi không có bang thấy nút "Đảo Kame" nhưng `confirmMenu` chỉ xử lý khi `clan != null` → không làm gì. Ngoài ra DB không đặt Dr. Brief ở map 153.
9. **Quốc Vương (42)**: nút đệ tử ghi "Nâng ngay 50Tr ngọc" nhưng trừ **vàng**; thông báo thiếu ghi "ngọc".
10. **Ghi danh (23) map 129**: thiếu ngọc thì thông báo dùng `inventory.ruby` để tính số còn thiếu (nhánh level = 0).
11. **Tapion (53)**: cho vào khi giờ ∈ [1, 23) nhưng thông báo ngoài giờ lại là "quay lại vào lúc 22h".
12. **Cây đậu thần**: nút "Hủy nâng cấp" ghi hoàn 1/2 vàng nhưng `unupgradeMagicTree` hoàn **100%** → có thể bấm nâng cấp/hủy tùy ý không mất phí. `UPGRADE_GEM[9] = 10000` không bao giờ dùng.
13. **Whis học tuyệt kỹ**: `Util.isTrue(15, 15)` luôn đúng → học mới 100% thành công. Không kiểm tra lại ngọc/vàng giữa các lần (đã kiểm tra trước). Map 48 "Coming Soon" không có nút.
14. **Độc Nhãn (26)**: `player.clan.doanhTrai` không kiểm tra null → NPE nếu người không trong doanh trại mở được.
15. **KOL VIP**: chỉ kiểm tra có "Vé Nhệm Vụ KOL Vip" (1825), **không trừ vé**. Nhiệm vụ KOL loại thu thập (bậc 2) dùng item 1824 tên DB là "Cậu Vàng" trong khi text ghi "chai cuke 2 lít".
16. **Ký gửi – Up top**: kiểm tra `gem >= 50` nhưng chỉ trừ 5 ngọc.
17. **Mabư egg**: `openEgg` gọi `Thread.sleep(4000)` trên luồng xử lý message.
18. **Nồi bánh (66)**: không kiểm tra đủ số lượng nguyên liệu trước khi trừ (chỉ kiểm tra khác null).
19. **Hùng Vương (52)**: danh sách `nguoiDaTrong` là static và không reset → sau 10 người, không ai trồng được nữa đến khi restart; nút đổi dưa ghi "thỏi" nhưng cộng **ngọc**.
20. **World Martial Arts (Ghi danh map 52)**: khi `canReg = true` nhưng không có giải (`tour == -1`), menu là [Thông tin, Siêu hạng, ĐHVT 23, Đóng] nhưng select 2 lại đưa tới map 113 thay vì 129.

### 9.2. Kiểm tra quyền / khoảng cách
- `Npc.canOpenNpc` chỉ kiểm tra khoảng cách ≤ 60px ở **map Ngọc Rồng Sao Đen**; map thường chỉ cần cùng map.
- Không gọi `canOpenNpc` khi mở menu: `QuocVuong`, `ToriBot`, `Calick`, `DuaHau`, `event/VuaHung`, `event/NoiBanh`, `event/XeNuocMia` (confirm vẫn có ở đa số, trừ `Calick` và `DuaHau`).
- Lý Tiểu Nương (54) được coi là mở hợp lệ từ bất kỳ đâu, nhưng NPC không có trong `map_template` nên thực tế `NpcManager.getNpc` trả `null` (tính năng minigame qua NPC **đang tắt** trừ khi được spawn thêm).

### 9.3. NPC/chức năng tắt hoặc trống
- `DaiThienSu` (64): `openBaseMenu` rỗng.
- `Vados` (58), `DrMyuu` (83), `Bardock` (70), Ông Gôhan/Paragus/Moori: không có chức năng ngoài hội thoại/nhiệm vụ.
- `BuyBack` trong Con Mèo đã comment. `DuaHauEgg.plant()` chưa cài đặt.
- Các shop DB không NPC nào mở: `SANTA_RUONG`, `SANTA_HSD`, `OSIN`, `BUNMA_LINHTHU`, `BULMA_TL`, `CHUBEDAN`, `BULMA_EVENT`, `QDDN`, `DOI_SKILL_DE`.
- Map được code nhắc tới nhưng không có trong `map_template`: 174, 181 (Bà Hạt Mít), 104 (có trong DB là "Sân sau siêu thị" nhưng không đặt Bunma TL), 164 có trong DB nhưng Whis không đặt ở đó.

### 9.4. Giá trị hardcode đáng chú ý
- Bò Mộng "Điểm danh": **10.000 ngọc + 100 thỏi vàng/ngày** cho mọi người chơi.
- Nạp ngọc: 1 VNĐ = 1 ngọc (10.000–5.000.000).
- Cui tìm boss 50.000.000 vàng; Quả trứng ấp nhanh 1.000.000.000 vàng; Nâng giới hạn SM 50.000.000 vàng; Phù hộ NRSĐ 10/30/50 triệu vàng; Kéo Búa Bao 1/5/10 tỷ vàng (thắng nhận 96%).
- Giu-ma Đầu Bò: triệu hồi Gấu Tướng Cướp HP 2 tỷ **không giới hạn số lần**; nút "OK" yêu cầu 40 tỷ SM.
- Mùa VIP Tori-Bot cố định 05/06–05/12 mỗi năm.
- Nhiều chuỗi hướng dẫn (lịch ĐHVT, phần thưởng Siêu hạng, NRSĐ, Bill…) là text cứng trong `ConstNpc`, có thể không khớp với logic thực tế ở các service.
