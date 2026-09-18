# 07 — Cửa hàng (Shop), Giao dịch & Ký gửi

> Tài liệu spec hệ thống **cửa hàng NPC**, **mua/bán/mua lại**, **giao dịch giữa người chơi**, **cửa hàng ký gửi** và **thẻ sưu tầm (radar)** của server Ngọc Rồng Online – Teamobi2026.
> Nguồn: `SRC/src/nro/models/shop/**`, `services_func/Trade.java`, `services_func/TransactionService.java`, `services_func/BuyBackService.java`, `shop_ky_gui/**`, `radar/**`, `services/RadarService.java` và các bảng `shop`, `tab_shop`, `item_shop`, `item_shop_option`, `shop_ky_gui`, `radar`, `history_transaction` trong `database team2026.sql`.
> Đường dẫn Java viết tắt `models/` = `SRC/src/nro/models/`. Bảng danh sách item trong shop (mục 8) được **sinh tự động bằng script** từ dump DB, không chỉnh tay.
> Cấu trúc item/option: [06-vat-pham.md](06-vat-pham.md).

## Mục lục

1. [File nguồn](#1-file-nguồn)
2. [Mô hình dữ liệu shop](#2-mô-hình-dữ-liệu-shop)
3. [Loại shop & loại tiền](#3-loại-shop--loại-tiền)
4. [Mở shop: NPC → tag → điều kiện](#4-mở-shop-npc--tag--điều-kiện)
5. [Mua vật phẩm (luồng `takeItem` / `buyItem`)](#5-mua-vật-phẩm-luồng-takeitem--buyitem)
6. [Bán vật phẩm cho shop](#6-bán-vật-phẩm-cho-shop)
7. [Mua lại vật phẩm đã bán & rương phần thưởng](#7-mua-lại-vật-phẩm-đã-bán--rương-phần-thưởng)
8. [Danh sách toàn bộ shop & item](#8-danh-sách-toàn-bộ-shop--item)
9. [Giao dịch giữa người chơi (Trade)](#9-giao-dịch-giữa-người-chơi-trade)
10. [Cửa hàng ký gửi](#10-cửa-hàng-ký-gửi)
11. [Thẻ sưu tầm (Radar)](#11-thẻ-sưu-tầm-radar)
12. [Ghi chú / điểm cần lưu ý](#12-ghi-chú--điểm-cần-lưu-ý)

---

## 1. File nguồn

| File | Vai trò |
|---|---|
| `models/database/ShopDAO.java` | Nạp `shop` → `tab_shop` → `item_shop` (chỉ `is_sell = 1`, sắp `create_time DESC`) → `item_shop_option` vào `Manager.SHOPS` khi khởi động. |
| `models/shop/Shop.java` | Shop (id, npcId, tagName, typeShop, tabShops). Constructor `Shop(shop, player)` bọc tab đặc biệt theo id. |
| `models/shop/TabShop.java`, `ItemShop.java` | Tab và item trong shop. |
| `models/shop/TabShopUron.java`, `TabShopHangDoc.java`, `TabShopSanta.java`, `TabShopMuaAvatar.java`, `TabShopHocKynang.java`, `TabShopDanhHieu.java`, `TabShopSoHuu.java` | Lọc item theo người chơi. |
| `models/shop/ShopService.java` | Mở shop, mua, bán, mua lại, rương vòng quay. |
| `models/shop/ShopTab.java` | **Không liên quan shop**: khoá IP – đọc `server.ip` trong `Config.properties`, nếu IP máy khác thì `Runtime.halt(0)` sau 3 giây. |
| `models/services_func/BuyBackService.java` | Lưu item đã bán để mua lại. |
| `models/services_func/TransactionService.java`, `Trade.java` | Giao dịch người chơi (gói `-86`). |
| `models/database/HistoryTransactionDAO.java` | Ghi lịch sử giao dịch. |
| `models/shop_ky_gui/ConsignItem.java`, `ConsignShopManager.java`, `ConsignShopService.java` | Ký gửi (gói `-100`). |
| `models/npc_list/KyGui.java` | NPC Cửa hàng ký gửi (`ConstNpc.CUA_HANG_KY_GUI` = 28). |
| `models/radar/*.java`, `models/services/RadarService.java` | Thẻ sưu tầm (gói `127`). |
| `models/server/Controller.java` | Điều phối gói tin: `6` mua, `7` bán, `-86` giao dịch, `-100` ký gửi, `127` radar. |

---

## 2. Mô hình dữ liệu shop

### 2.1 Bảng DB

| Bảng | Cột | Ý nghĩa |
|---|---|---|
| `shop` (32 dòng) | `id`, `npc_id`, `tag_name`, `type_shop` | `tag_name` là khoá code dùng để mở shop; `type_shop` xem mục 3 |
| `tab_shop` (58 dòng) | `id`, `shop_id`, `NAME` | Tên tab; `<>` được thay bằng xuống dòng |
| `item_shop` (826 dòng) | `id`, `tab_id`, `temp_id`, `is_new`, `is_sell`, `type_sell`, `cost`, `icon_spec`, `create_time` | `is_sell = 0` ⇒ không nạp (17 dòng); `type_sell` loại tiền; `icon_spec` icon vật phẩm dùng làm tiền (shop đặc biệt) |
| `item_shop_option` (1420 dòng) | `id`, `item_shop_id`, `option_id`, `param` | Option gắn vào item khi mua |

Thống kê `type_sell` trong DB: `1` (ngọc xanh) 564 dòng, `0` (vàng) 262 dòng; không có `3`/`4`.

### 2.2 Nạp dữ liệu (`ShopDAO.java` dòng 20–130)

- `select * from shop order by npc_id asc` → mỗi shop nạp tab `order by id`.
- Với tab **41, 42, 43** (shop `QUY_LAO`), truy vấn item bằng `tab_id − 31` ⇒ **dùng lại item của tab 10, 11, 12** (shop `URON`) (dòng 81–84).
- Item: `select * from item_shop where is_sell = 1 and tab_id = ? order by create_time desc`.

### 2.3 Tab đặc biệt (`Shop(Shop, Player)` dòng 31–57)

Khi mở shop với `allGender = false`, mỗi tab được bọc:

| Tab id | Lớp | Lọc |
|---|---|---|
| 10, 11, 12 | `TabShopUron` | Item `gender == player.gender` hoặc `== 3`; ẩn **Gói 30 đậu thần** không đúng cấp (mục 2.4); ẩn 453 nếu đã có Chiến thuyền Tennis; ẩn 454 nếu đã có Bông tai (454/921 trong túi/rương) |
| 13 | `TabShopHangDoc` | Theo gender; lọc gói đậu |
| 17 | `TabShopSanta` | Theo gender; lọc gói đậu |
| 19 | `TabShopMuaAvatar` | Theo gender; lọc gói đậu |
| 41, 42, 43 | `TabShopHocKynang` | Theo gender; ẩn sách có id trong `player.BoughtSkill` |
| 44 | `TabShopDanhHieu` | Theo gender; ẩn danh hiệu đang sở hữu; thêm option **220 "Hoàn thành #%"** = tiến độ nhiệm vụ danh hiệu |
| 45 | `TabShopSoHuu` | Chỉ hiện danh hiệu **đang sở hữu**; option 93 = số ngày còn lại; tên tab + số lượng |
| khác | `TabShop` | Không lọc |

Với `allGender = true` dùng `Shop(Shop)` ⇒ không lọc gì.

### 2.4 Gói 30 đậu thần theo cấp cây đậu

`listDauThan = {293, 294, 295, 296, 297, 298, 299, 596, 597, 598}`; `idDauCanBuy(player)` = `listDauThan[level]` với cây cấp 1–9, `listDauThan[9]` với cấp 10 ⇒ cây cấp *n* chỉ thấy gói của cấp *n+1* (598 là "Avatar", không phải gói đậu). Cây cấp khác 1–10 ⇒ ném `IllegalArgumentException` (không mở được shop).

Khi mua gói (`ShopService.buyMagicPean` dòng 1287–1297), cặp {hạt, gói} = {13,293}, {60,294}, {61,295}, {62,296}, {63,297}, {64,298}, {65,299}, {352,596}, {523,597}: nhận **30 hạt** id tương ứng, option hồi = `PEA_PARAM[cấp cây − 1]` (option 48 nếu cây cấp ≤ 2, ngược lại option 2).

---

## 3. Loại shop & loại tiền

### 3.1 `type_shop` (`ShopService.java` dòng 45–47)

| Giá trị | Hằng | Gửi client | Cách trả tiền |
|---|---|---|---|
| 0 | `NORMAL_SHOP` | `openShopType0` – ghi giá vàng hoặc ngọc | `subMoneyByItemShop` theo `type_sell` |
| 1 | `KINANG_SHOP` | `openShopType1` – ghi **tiềm năng** = `powRequire` của skill ở cấp sách | Học kỹ năng bằng tiềm năng (`learnKyNang`) |
| 3 | `SPEC_SHOP` | `openShopType3` – ghi `iconSpec` + `cost` | `subIemByItemShop`: trả bằng **vật phẩm** có icon = `icon_spec` |
| 4 | (hằng số gửi) | `openShopType4` | Rương phần thưởng Vòng quay (`ITEMS_LUCKY_ROUND`) |
| 8 | (hằng số gửi) | `openShopType8` | Mua lại đồ đã bán (`ITEMS_DABAN`) |

### 3.2 `type_sell` (dòng 40–43)

| Giá trị | Hằng | Trừ |
|---|---|---|
| 0 | `COST_GOLD` | `inventory.gold` |
| 1 | `COST_GEM` | `inventory.gem` (ngọc xanh) |
| 3 | `COST_RUBY` | `inventory.ruby` – nhưng `subMoneyByItemShop` **kiểm tra nhầm `gem < ruby`** (dòng 519) |
| 4 | `COST_COUPON` | `inventory.coupon` |

### 3.3 Trả bằng vật phẩm (`subIemByItemShop` dòng 969–1009)

`itSpec = ItemService.getItemIdByIcon(icon_spec)` = **template đầu tiên** có `icon_id` trùng:

| Item tiền | Hành vi |
|---|---|
| 76, 188, 189, 190 (vàng) | Trừ `gold` |
| 77 (ngọc) | Kiểm tra `gem >= cost` nhưng **không trừ** |
| khác | Tìm item trong túi, đủ số lượng ⇒ trừ `cost` cái |

Icon đang dùng trong DB: 4028 → **457 Thỏi vàng**; 7223 → 1559 Capsule 1 món kích hoạt (nhưng shop bang dùng điểm, mục 5.8); 15485 → 1805 Phiếu thức ăn; 14116 → không có item (shop đổi điểm dùng điểm sự kiện); 0 → 1826 (item rỗng) ⇒ không mua được.

---

## 4. Mở shop: NPC → tag → điều kiện

`ShopService.opendShop(player, tagName, allGender)` (dòng 59–98). Shop bùa đi qua `resolveShopBua` (hiện thời gian bùa còn lại bằng option 63/64/65). Các thao tác mua/bán bị chặn khi server đang bảo trì, đang giao dịch, hoặc bật bảo vệ tài khoản (`Controller.java` dòng 273–312).

| tag_name | Shop id | NPC mở (file) | Điều kiện / menu | allGender |
|---|---|---|---|---|
| `BUNMA` | 1 | Bunma (`npc_list/Bulma.java` dòng 45–50) | Chỉ người **Trái Đất** | true |
| `DENDE` | 2 | Dende (`Dende.java` 55–60) | Chỉ người **Namếc** | true |
| `APPULE` | 3 | Appule (`Appule.java` 45–50) | Chỉ người **Xayda** | true |
| `ITEMS_DABAN` | — | Bunma/Dende/Appule – "Mua lại vật phẩm đã bán" | Menu mua lại chỉ hiện với người **khác hành tinh** của NPC (dòng 27–34) | true |
| `URON` | 4 | Uron (`Uron.java` 15) | Mở ngay khi nói chuyện | false |
| `QUY_LAO` | 25 | Quy Lão Kame (`QuyLaoKame.java` 259, 291), Trưởng lão Guru (`TruongLaoGuru.java` 84), Vua Vegeta (`VuaVegeta.java` 83) | "Học kỹ năng" | false |
| `SHOP_DOI_DIEM` | 34 | Quy Lão Kame (`QuyLaoKame.java` 161) | menu select 1 | false |
| `BUA_1H` / `BUA_8H` / `BUA_1M` | 6 / 7 / 8 | Bà Hạt Mít (`BaHatMit.java` 428–436) | Menu "Cửa hàng Bùa" | true |
| `SANTA` | 9 | Santa (`Santa.java` 58–62) | Chỉ tại map 5, 13, 20 | false |
| `SANTA_GIAM_GIA_1` | 30 | Santa | Chỉ hiện khi túi có **Phiếu giảm giá 459** | false |
| `SANTA_MO_RONG_HANH_TRANG` | 22 | Santa | "Mở rộng Hành trang Rương đồ" | false |
| `SANTA_HAN_SU_DUNG` | 23 | Santa | "Cửa hàng Hạn sử dụng" | false |
| `SANTA_HEAD` | 5 | Santa | "Tiệm Hớt tóc" | false |
| `SANTA_DANH_HIEU` | 26 | Santa | "Danh hiệu" | false |
| `SHOP_VIP` | 31 | Santa – nhánh `select == 6` khi **không** có phiếu giảm giá | Menu chỉ có 6 mục (0–5) ⇒ **không bấm được** | false |
| `BILL` | 11 | Bill (`Bill.java` 101) | `select == 100` và `canOpenBillShop` (5 món thần linh + 99 thức ăn) | true |
| `SHOP_SU_KIEN_VL` | 36 | Bill map 48 (`Bill.java` 88) | "Đổi phiếu ăn lấy quà" | true |
| `THIEN_SU` | 20 | Whis map 154 (`Whis.java` 97) | "Shop thiên sứ" | false |
| `SHOP_CLAN` | 35 | Giu-ma Đầu Bò (`GiuMaDauBo.java` 132) | "Cửa Hàng Bang hội" | false |
| `SHOP_CHI_CHI` | 33 | Chi Chi map 5 (`ChiChi.java` 53) | "Cửa hàng" | false |
| `KARIN` | 16 | `BulmaTuongLai.java` 47 (map 104 hoặc 5) | Shop rỗng trong DB | true |
| `BUNMA_FUTURE` | 10 | `BulmaTuongLai.java` 54 (map 102) | | true |
| `ITEMS_LUCKY_ROUND` | — | Thượng Đế (`ThuongDe.java`) | Rương vật phẩm vòng quay | true |
| `BUNMA_LINHTHU`, `SANTA_RUONG`, `SANTA_HSD`, `OSIN`, `BULMA_TL`, `CHUBEDAN`, `BULMA_EVENT`, `DOI_SKILL_DE`, `QDDN` | 15, 12, 13, 14, 17, 19, 21, 32, 29 | **Không có code nào mở** | — | — |

---

## 5. Mua vật phẩm (luồng `takeItem` / `buyItem`)

Gói `6` → `ShopService.takeItem(player, type, tempId)` (dòng 462–496). Shop được xác định bằng `player.idMark.getTagNameShop()` / `getShopOpen()`; item được tìm bằng **temp_id** (`Shop.getItemShop`) ⇒ nếu một shop có 2 dòng cùng temp_id, luôn lấy dòng **đầu tiên**.

### 5.1 Rẽ nhánh theo tag

| Tag | Hàm |
|---|---|
| `ITEMS_LUCKY_ROUND` | `getItemSideBoxLuckyRound` (mục 7.2) |
| `ITEMS_REWARD` | bỏ qua |
| `ITEMS_DABAN` | `buyItemDaBan` (mục 7.1) |
| `BILL` | `buyItemHD` (5.6) |
| `BUA_1H`/`BUA_8H`/`BUA_1M` | `buyItemBua` (5.2) |
| `SANTA_HEAD` | Đổi kiểu tóc: `player.head = template.head` – **miễn phí**, không nhận item |
| còn lại | `buyItem` (5.3) |

Cuối cùng gửi lại tiền (`sendMoney`).

### 5.2 Mua bùa – `buyItemBua` (dòng 575–588)

Trừ tiền (`subMoneyByItemShop`) ⇒ `addItemBag` ⇒ `addItemSpecial` cộng thời gian bùa: 60 phút / 480 phút / 43.200 phút (30 ngày) theo tag ⇒ mở lại shop để cập nhật thời gian còn lại. Không kiểm tra ô trống (bùa không chiếm ô).

### 5.3 `buyItem` (dòng 660–892) – thứ tự kiểm tra

| # | Điều kiện | Hành vi |
|---|---|---|
| 1 | `is == null` | "Không thể thực hiện" |
| 2 | `tabShop.id == 30` | Đổi bằng **Phiếu giảm giá 459**: trừ 1 phiếu, nhận item (không trừ tiền). *Tab 30 thuộc shop `BULMA_EVENT` không ai mở; tab "Giảm giá 80%" thực tế là tab **50** ⇒ không đi nhánh này mà trả **giá đầy đủ** trong DB* |
| 3 | `tabShop.id == 44` | `buyDanhHieu` (5.7) |
| 4 | `tabShop.id == 45` | `changeDanhHieu` (5.7) |
| 5 | `tabShop.id == 49` | "Shop chipi": nhận item **miễn phí**, 5% option 73 (vĩnh viễn), 95% HSD 3–7 ngày; không kiểm tra ô trống (tab 49 thuộc shop `QDDN` – không NPC nào mở) |
| 6 | `shop.typeShop == KINANG_SHOP` | `learnKyNang` (5.5) |
| 7 | Túi không còn ô trống | "Hành trang đã đầy" |
| 8 | `itemTempId == 711` mà không có cải trang **710** (Quy Lão Kame) ở body/túi/rương | từ chối |
| 9 | Mua **1524** (TĐLT 3) khi `autoTrainState != 2`; mua **1523** (TĐLT 2) khi `autoTrainState != 1` | "Bạn cần mua Tự động luyện tập 2/1 trước!" |
| 10 | `tabShop.id == 59` | Đổi **điểm sự kiện** (5.8) |
| 11 | `tabShop.id` ∈ {60, 61, 62} | Đổi **điểm Capsule Bang** (5.8) |
| 12 | `NORMAL_SHOP` | `subMoneyByItemShop` |
| 13 | `SPEC_SHOP` | `subIemByItemShop` |
| 14 | Tạo item | `createItemFromItemShop` (quantity 1 + option shop) ⇒ `buyMagicPean` (gói đậu ⇒ 30 hạt) ⇒ 1523/1524 được thay bằng **item 521** + option shop |
| 15 | `addItemBag` | "Mua thành công …". Mua 1524 ⇒ `autoTrainState = 0`; 521 ⇒ `= 1` (nếu chưa là 2); 1523 ⇒ `= 2` |

> Hàm `buyItem` **không kiểm tra** `power_require`, hành tinh (ngoài lọc hiển thị) hay số lượng mua – mỗi lần mua 1 cái.

### 5.4 Giá & ô trống

- Không có giảm giá theo VIP. Option 112 "Giảm #% khi mua Avatar hoặc Cải trang" trên Phiếu giảm giá không được đọc ở đâu.
- Kiểm tra ô trống **trước** khi trừ tiền (bước 7). Riêng `addItemBag` thất bại (vd. vàng vượt giới hạn) vẫn đã trừ tiền.

### 5.5 Học kỹ năng – shop `QUY_LAO` (`learnKyNang` dòng 596–658)

| Bước | Chi tiết |
|---|---|
| Kiểm tra | `power >= template.strRequire`; `tiemNang >= cost` (cost trong DB là giá ngọc 10–…); chưa học cấp này; cấp sách = cấp hiện tại + 1; chưa có trong `BoughtSkill` |
| Tiềm năng cần | `powRequire` của skill template ở cấp sách (hiển thị cho client) |
| Thời gian học theo cấp 1…7 | 15 phút, 30 phút, 1 giờ, 1 ngày, 3 ngày, 7 ngày, 15 ngày (`time[]` dòng 629) |
| Xác nhận | Menu 671 (`NpcFactory.java` 529–548): đặt `LearnSkill.Time = now + time[level−1]`, **trừ `tiemNang` = powRequire** (không kiểm tra lại đủ hay không) |
| Học cấp tốc / huỷ | Quy Lão / Guru / Vua Vegeta: "Học Cấp tốc X ngọc", "Huỷ" nhận lại 50% tiềm năng |

### 5.6 Shop Bill – đồ Hủy Diệt (`buyItemHD` dòng 1217–1285)

Xem chi tiết [06 mục 9.3](06-vat-pham.md#93-đồ-hủy-diệt-650662). Tóm tắt: cần ≥1 ô trống; trừ tiền bằng `subMoneyByItemShopV2`; item level 14 cần thêm **99 thức ăn** 663–667; phải mặc ít nhất 1 món level 13; random +0–15% chỉ số; thêm option 30.

### 5.7 Danh hiệu (shop `SANTA_DANH_HIEU`)

| Tab | Hàm | Logic |
|---|---|---|
| 44 "Danh Hiệu" | `buyDanhHieu` (923–946) | Tiến độ nhiệm vụ danh hiệu phải = 100% (`BadgesTaskService.sendPercenBadgesTask`); chưa sở hữu ⇒ thêm `BadgesData(idEffect, 30)` (30 ngày). **Không trừ giá** (DB ghi 2 ngọc) |
| 45 "Sở Hữu" | `changeDanhHieu` (948–967) | Cooldown 3 giây; đổi danh hiệu đang dùng (`BadgesService.turnOnBadges`), tính lại chỉ số. Miễn phí |

### 5.8 Đổi điểm

**Tab 59 – `SHOP_DOI_DIEM` (điểm sự kiện, `buyItem` dòng 736–785)** – giá hard-code, bỏ qua `cost` DB:

| temp_id | Tên | Điểm |
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
| 1592 | Hộp quà Goku Day VIP | 99 (không có trong tab) |
| 1608 | Hộp quà thiếu nhi | 9 |
| khác | — | 0 (miễn phí) |

**Tab 60/61/62 – `SHOP_CLAN` (điểm Capsule Bang `clan.capsuleClan`, dòng 787–863)** – trừ điểm **của bang**:

| temp_id | Điểm | Ghi chú so với `cost` DB |
|---|---|---|
| 1794 Con dấu, 1204 Mảnh Rồng thần Namếc, 1423 Hematite, 1438 Dùi đục, 1439 Đá mài, 987 Đá bảo vệ, 1635 Cỏ bốn lá | 1 | khớp |
| 1790 Mẹ Rồng | 2 | không có trong tab |
| 1791 Mảnh khỉ Oorazu | 3 | DB = 2 |
| 1792 Mảnh khỉ Oorazu 1 | 4 | DB = 3 |
| 1793 Mảnh khỉ Oorazu 2 | **−1 ⇒ "không thể mua"** | DB = 4 |
| 1634 Cápsule Vỡ | 10 | khớp |
| Pet tab 61 (1620, 1748, 1750, 1729, 1727, 1714, 1683, 1682, 1668, 1629, 1630, 1631, 1573, 1550, 1551) và ván bay tab 62 (1541, 1563, 1724, 1733, 1734, 1749) | 50 | khớp |
| khác | −1 ⇒ không mua được | |

Yêu cầu có bang; bang đủ điểm.

---

## 6. Bán vật phẩm cho shop

Gói `7` (`Controller.java` dòng 293–312): action 0 ⇒ `showConfirmSellItem`, action khác ⇒ `sellItem`. Chỉ bán khi **đang mở một shop** (bất kỳ).

### 6.1 Giá bán (`ShopService.sellItem` dòng 1065–1130)

```
quantity = item.quantity (bán cả chồng)
cost = template.gold
nếu id == 457 (Thỏi vàng): quantity = 1 (giá = template.gold = 500.000.000 cho 1 thỏi, trừ 1 thỏi)
ngược lại: cost = cost / 4
nếu cost == 0: cost = 1
cost = cost × quantity
```

| Điều kiện chặn | Thông báo |
|---|---|
| id 570 Rương Gỗ | "Bạn không thể bán vật phẩm này" |
| Item có option 93 với param > 0 (tìm **item đầu tiên cùng template trong túi**, `InventoryService.getParam`) | "Bạn không thể bán vật phẩm có hạn sử dụng" |
| `gold + cost > 200 tỷ` | "Vàng sau khi bán vượt quá giới hạn" |

- Bán từ body (`where = 0`) hoặc túi (`where = 1`). Client version < 220: index túi bị trừ `(itemsBody.size() − 7)`.
- Sau khi bán (trừ thỏi vàng) item được lưu vào danh sách mua lại (`BuyBackService.addItem`).
- Bán tại `BUNMA`/`DENDE`/`APPULE` ⇒ tính thành tựu `TRUM_NHAT_VE_CHAI`.

### 6.2 Bán Thỏi vàng

`showConfirmSellItem` với item 457 mở form nhập số lượng `BANSLL` (`Input.java` dòng 495–520): mỗi thỏi **37.000.000 vàng**; nếu vượt 200 tỷ báo số lượng tối đa bán được. (Nếu client gửi thẳng lệnh bán không qua form thì `sellItem` bán 1 thỏi với giá 500.000.000.)

---

## 7. Mua lại vật phẩm đã bán & rương phần thưởng

### 7.1 Mua lại – `ITEMS_DABAN`

- `BuyBackService.addItem` (dòng 27–36): giữ tối đa **10** item gần nhất (vượt ⇒ xoá item cũ nhất); lưu DB cột `items_daban`; khi load chỉ lấy 20 và bỏ item hết hạn.
- Giá (`openShopType8` dòng 430–431, `buyItemDaBan` dòng 1178–1215):

```
giaNgoc = template.gem / 2
giaVang = (giaNgoc == 0) ? (template.gold/2 > 0 ? template.gold/2 : quantity × 100) : 0
```

- Trừ tiền **trước** khi kiểm tra ô trống ⇒ túi đầy vẫn mất tiền mà không nhận item.

### 7.2 Rương vòng quay – `ITEMS_LUCKY_ROUND`

`getItemSideBoxLuckyRound` (dòng 1132–1176) trên `inventory.itemsBoxCrackBall` (tối đa 110 ô khi tạo nhân vật): type 0 nhận 1 món (cần ô trống), 1 xoá 1 món, 2 nhận hết (dừng ở món nào không thêm được). Xoá toàn bộ qua menu `CONFIRM_REMOVE_ALL_ITEM_LUCKY_ROUND`.

---

## 8. Danh sách toàn bộ shop & item

### 8.1 Tổng quan

| id | tag_name | NPC (npc_id) | type | Tab (id: tên – số item đang bán) | Có NPC mở? |
|---|---|---|---|---|---|
| 1 | BUNMA | Bunma (7) | 0 | 1: Áo Quần – 24; 2: Phụ kiện – 42; 3: Đặc biệt – 3 | Có |
| 2 | DENDE | Dende (8) | 0 | 4: Áo Quần – 24; 5: Phụ kiện – 42; 6: Đặc biệt – 3 | Có |
| 3 | APPULE | Appule (9) | 0 | 7: Áo Quần – 24; 8: Phụ kiện – 42; 9: Đặc biệt – 3 | Có |
| 4 | URON | Uron (16) | 0 | 10: Sách Võ – 41; 11: Sách Chưởng – 56; 12: Sách Đặc biệt – 56; 19: Phụ kiện – 50 | Có |
| 5 | SANTA_HEAD | Santa (39) | 0 | 13: Tiệm Hớt tóc – 9 | Có |
| 6 | BUA_1H | Bà Hạt Mít (21) | 0 | 14: Bùa 1 giờ – 10 | Có |
| 7 | BUA_8H | Bà Hạt Mít (21) | 0 | 15: Bùa 8 giờ – 10 | Có |
| 8 | BUA_1M | Bà Hạt Mít (21) | 0 | 16: Bùa 1 tháng – 10 | Có |
| 9 | SANTA | Santa (39) | 0 | 17: Cửa Hàng – 15; 18: Cải Trang – 74; 34: Hỗ Trợ – 13 | Có |
| 10 | BUNMA_FUTURE | Bunma (37) | 0 | 20: Cửa Hàng – 2 | Có |
| 11 | BILL | Bill (55) | 0 | 21: Trái Đất – 5; 22: Namếc – 5; 23: Xay da – 5 | Có (điều kiện) |
| 12 | SANTA_RUONG | Santa (39) | 0 | 29 – 0 | Không |
| 13 | SANTA_HSD | Santa (39) | 0 | 35: Ngọc – 0; 36: Vàng – 0 | Không |
| 14 | OSIN | Ôsin (44) | 0 | 39 – 0 | Không |
| 15 | BUNMA_LINHTHU | Bunma (37) | 0 | 31 – 0 | Không |
| 16 | KARIN | Thần mèo Karin (18) | 3 | 32 – 0 | Có (rỗng) |
| 17 | BULMA_TL | Bunma (37) | 0 | 33: Cửa Hàng Tương Lai – 1 | Không |
| 19 | CHUBEDAN | Chú Bé Đần (103) | 3 | (không có tab) | Không |
| 20 | THIEN_SU | Whis (56) | 3 | 37: Cửa hàng – 16 | Có |
| 21 | BULMA_EVENT | Bulma Tết Nguyên Đán (106) | 3 | 30: Cửa hàng EVENT – 9; 38 – 0; 40 – 0 | Không |
| 22 | SANTA_MO_RONG_HANH_TRANG | Santa (39) | 0 | 46: Cửa Hàng – 2 | Có |
| 23 | SANTA_HAN_SU_DUNG | Santa (39) | 0 | 47: Ngọc – 6; 48: Vàng – 3 | Có |
| 25 | QUY_LAO | Quy Lão Kame (13) | 1 | 41/42/43 (dùng item tab 10/11/12) | Có |
| 26 | SANTA_DANH_HIEU | Santa (39) | 0 | 44: Danh Hiệu – 19; 45: Sở Hữu – 19 | Có |
| 29 | QDDN | Quy Lão Kame (13) | 3 | 49: Đổi Thưởng – 12; 51: Event – 6 | Không |
| 30 | SANTA_GIAM_GIA_1 | Santa (39) | 0 | 50: Giảm giá 80% – 18 | Có (cần phiếu) |
| 31 | SHOP_VIP | Santa (39) | 3 | 52: Cải trang – 19; 53: Đeo Lưng – 21; 54: Ván bay – 5; 55: Pet – 5; 56: glt – 1 | Không bấm được |
| 32 | DOI_SKILL_DE | Thỏ Đỏ ChiChi (75) | 3 | 57: Đổi Skill Đệ – 3 | Không |
| 33 | SHOP_CHI_CHI | Chi Chi (81) | 0 | 58: Shop sự kiện – 3 | Có |
| 34 | SHOP_DOI_DIEM | Quy Lão Kame (13) | 3 | 59: Đổi thưởng – 10 | Có |
| 35 | SHOP_CLAN | Giu-ma Đầu Bò (47) | 3 | 60: Cửa Hàng Item – 11; 61: Cửa Hàng Pet – 15; 62: Cửa Hàng Ván Bay – 6 | Có |
| 36 | SHOP_SU_KIEN_VL | Bill (55) | 3 | 63: Đổi Thưởng – 5 | Có |

Ngoài ra `item_shop` có dòng thuộc **tab_id 24 và 26** không tồn tại trong `tab_shop` (không bao giờ được nạp) – liệt kê ở cuối 8.2.

### 8.2 Chi tiết từng shop

Quy ước cột: **HT** = hành tinh của template (TĐ/NM/XD/Chung). **Giá** & **Loại tiền** lấy từ `item_shop.cost`/`type_sell`; với shop `type_shop = 3` cột "Trả bằng" là item quy đổi từ `icon_spec`. Cột **Option** là option gắn khi mua (bỏ option 73). Thứ tự dòng theo `item_shop.id` (client hiển thị theo `create_time DESC`). Với shop đổi điểm (tab 59–62) và danh hiệu (44, 45) giá thực tế **không lấy từ cột Giá** – xem mục 5.7, 5.8.

#### Shop id=1 — `BUNMA` (NPC 7 – Bunma, type_shop=0)


**Tab 1 – "Áo Quần"**: 24 item đang bán

| item_shop.id | temp_id | Tên | HT | Giá | Loại tiền | Option |
|---|---|---|---|---|---|---|
| 1 | 0 | Áo vải 3 lỗ | TĐ | 500 | Vàng | Giáp+2 (#47) |
| 2 | 33 | Áo thun 3 lỗ | TĐ | 5.000 | Vàng | Giáp+4 (#47) |
| 3 | 3 | Áo vải dày | TĐ | 10.000 | Vàng | Giáp+8 (#47) |
| 4 | 34 | Áo thun dày | TĐ | 20.000 | Vàng | Giáp+16 (#47) |
| 5 | 136 | Áo vải Kame | TĐ | 50.000 | Vàng | Giáp+24 (#47) |
| 6 | 137 | Áo thun Kame | TĐ | 100.000 | Vàng | Giáp+40 (#47) |
| 7 | 138 | Áo võ Kame | TĐ | 200.000 | Vàng | Giáp+60 (#47) |
| 8 | 139 | Áo võ Goku | TĐ | 500.000 | Vàng | Giáp+90 (#47) |
| 9 | 230 | Áo bạc Goku | TĐ | 2.000.000 | Vàng | Giáp+200 (#47) |
| 10 | 231 | Áo vàng Goku | TĐ | 5.800.000 | Vàng | Giáp+250 (#47) |
| 11 | 232 | Áo da Calic | TĐ | 17.000.000 | Vàng | Giáp+300 (#47) |
| 12 | 233 | Áo jean Calic | TĐ | 52.000.000 | Vàng | Giáp+400 (#47) |
| 13 | 6 | Quần vải đen | TĐ | 400 | Vàng | HP+30 (#6) |
| 14 | 35 | Quần thun đen | TĐ | 4.000 | Vàng | HP+150 (#6); +12 HP/30s (#27) |
| 15 | 9 | Quần vải dày | TĐ | 8.000 | Vàng | HP+300 (#6); +40 HP/30s (#27) |
| 16 | 36 | Quần thun dày | TĐ | 18.000 | Vàng | HP+600 (#6); +120 HP/30s (#27) |
| 17 | 140 | Quần vải Kame | TĐ | 45.000 | Vàng | HP+1400 (#6); +280 HP/30s (#27) |
| 18 | 141 | Quần thun Kame | TĐ | 90.000 | Vàng | HP+3000 (#6); +600 HP/30s (#27) |
| 19 | 142 | Quần võ Kame | TĐ | 180.000 | Vàng | HP+6000 (#6); +1200 HP/30s (#27) |
| 20 | 143 | Quần võ goku | TĐ | 400.000 | Vàng | HP+10000 (#6); +2000 HP/30s (#27) |
| 21 | 242 | Quần bạc Goku | TĐ | 2.000.000 | Vàng | HP+14000 (#6); +2500 HP/30s (#27) |
| 22 | 243 | Quần vàng Goku | TĐ | 5.800.000 | Vàng | HP+18000 (#6); +3000 HP/30s (#27) |
| 23 | 244 | Quần da Calic | TĐ | 17.000.000 | Vàng | HP+22000 (#6); +3500 HP/30s (#27) |
| 24 | 245 | Quần jean Calic | TĐ | 52.000.000 | Vàng | HP+26000 (#6); +4000 HP/30s (#27) |

**Tab 2 – "Phụ kiện"**: 42 item đang bán

| item_shop.id | temp_id | Tên | HT | Giá | Loại tiền | Option |
|---|---|---|---|---|---|---|
| 25 | 21 | Găng vải đen | TĐ | 700 | Vàng | Tấn công+4 (#0) |
| 26 | 24 | Găng thun đen | TĐ | 7.000 | Vàng | Tấn công+7 (#0) |
| 27 | 37 | Găng vải dày | TĐ | 14.000 | Vàng | Tấn công+14 (#0) |
| 28 | 38 | Găng thun dày | TĐ | 30.000 | Vàng | Tấn công+28 (#0) |
| 29 | 144 | Găng vải Kame | TĐ | 60.000 | Vàng | Tấn công+55 (#0) |
| 30 | 145 | Găng thun Kame | TĐ | 120.000 | Vàng | Tấn công+110 (#0) |
| 31 | 146 | Găng võ Kame | TĐ | 240.000 | Vàng | Tấn công+220 (#0) |
| 32 | 147 | Găng võ goku | TĐ | 600.000 | Vàng | Tấn công+530 (#0) |
| 33 | 254 | Găng bạc Goku | TĐ | 6.800.000 | Vàng | Tấn công+680 (#0) |
| 34 | 255 | Găng vàng Goku | TĐ | 20.000.000 | Vàng | Tấn công+1000 (#0) |
| 35 | 256 | Găng da Calic | TĐ | 60.000.000 | Vàng | Tấn công+1500 (#0) |
| 36 | 257 | Găng jean Calic | TĐ | 150.000.000 | Vàng | Tấn công+2200 (#0) |
| 37 | 27 | Giầy nhựa | TĐ | 300 | Vàng | KI+10 (#7) |
| 38 | 30 | Giầy cao su | TĐ | 3.000 | Vàng | KI+25 (#7); +5 KI/30s (#28) |
| 39 | 39 | Giày nhựa đế dày | TĐ | 6.000 | Vàng | KI+120 (#7); +24 KI/30s (#28) |
| 40 | 40 | Giày cao su đế dày | TĐ | 15.000 | Vàng | KI+250 (#7); +50 KI/30s (#28) |
| 41 | 148 | Giày nhựa Kame | TĐ | 30.000 | Vàng | KI+500 (#7); +100 KI/30s (#28) |
| 42 | 149 | Giày cao su Kame | TĐ | 70.000 | Vàng | KI+1200 (#7); +240 KI/30s (#28) |
| 43 | 150 | Giày võ kame | TĐ | 150.000 | Vàng | KI+2400 (#7); +480 KI/30s (#28) |
| 44 | 151 | Giày võ goku | TĐ | 350.000 | Vàng | KI+5000 (#7); +1000 KI/30s (#28) |
| 45 | 266 | Giày bạc Goku | TĐ | 1.300.000 | Vàng | KI+9000 (#7); +1500 KI/30s (#28) |
| 46 | 267 | Giày vàng Goku | TĐ | 3.800.000 | Vàng | KI+14000 (#7); +2000 KI/30s (#28) |
| 47 | 268 | Giày da Calic | TĐ | 12.000.000 | Vàng | KI+19000 (#7); +2500 KI/30s (#28) |
| 48 | 269 | Giày jean Calic | TĐ | 34.000.000 | Vàng | KI+24000 (#7); +3000 KI/30s (#28) |
| 49 | 12 | Rada cấp 1 | Chung | 600 | Vàng | Chí mạng+1% (#14) |
| 50 | 57 | Rada cấp 2 | Chung | 6.000 | Vàng | Chí mạng+2% (#14) |
| 51 | 58 | Rada cấp 3 | Chung | 12.000 | Vàng | Chí mạng+3% (#14) |
| 52 | 59 | Rada cấp 4 | Chung | 24.000 | Vàng | Chí mạng+4% (#14) |
| 53 | 184 | Rada cấp 5 | Chung | 55.000 | Vàng | Chí mạng+5% (#14) |
| 54 | 185 | Rada cấp 6 | Chung | 110.000 | Vàng | Chí mạng+6% (#14) |
| 55 | 186 | Rada cấp 7 | Chung | 220.000 | Vàng | Chí mạng+7% (#14) |
| 56 | 187 | Rada cấp 8 | Chung | 550.000 | Vàng | Chí mạng+8% (#14) |
| 57 | 278 | Rada cấp 9 | Chung | 4.200.000 | Vàng | Chí mạng+9% (#14) |
| 58 | 279 | Rada cấp 10 | Chung | 20.000.000 | Vàng | Chí mạng+10% (#14) |
| 59 | 280 | Rada cấp 11 | Chung | 100.000.000 | Vàng | Chí mạng+11% (#14) |
| 60 | 281 | Rada cấp 12 | Chung | 150.000.000 | Vàng | Chí mạng+12% (#14) |
| 61 | 529 | Giáp tập luyện cấp 1 | Chung | 30 | Ngọc xanh | Hiệu lực trong 0 phút (#9) |
| 62 | 530 | Giáp tập luyện cấp 2 | Chung | 300 | Ngọc xanh | Hiệu lực trong 0 phút (#9) |
| 63 | 531 | Giáp tập luyện cấp 3 | Chung | 3.000 | Ngọc xanh | Hiệu lực trong 0 phút (#9) |
| 64 | 534 | Giáp tập luyện cấp 1 | Chung | 10.000.000 | Vàng | Hiệu lực trong 0 phút (#9) |
| 65 | 535 | Giáp tập luyện cấp 2 | Chung | 100.000.000 | Vàng | Hiệu lực trong 0 phút (#9) |
| 66 | 536 | Giáp tập luyện cấp 3 | Chung | 1.000.000.000 | Vàng | Hiệu lực trong 0 phút (#9) |

**Tab 3 – "Đặc biệt"**: 3 item đang bán

| item_shop.id | temp_id | Tên | HT | Giá | Loại tiền | Option |
|---|---|---|---|---|---|---|
| 72 | 521 | Tự động luyện tập | Chung | 2 | Ngọc xanh | Thời gian sử dụng 20 phút (#1) |
| 592 | 194 | Viên Capsule đặc biệt | Chung | 1.000 | Ngọc xanh | — |
| 635 | 361 | Gói 10 Rađa dò ngọc | Chung | 5.000.000 | Vàng | — |

#### Shop id=2 — `DENDE` (NPC 8 – Dende, type_shop=0)


**Tab 4 – "Áo Quần"**: 24 item đang bán

| item_shop.id | temp_id | Tên | HT | Giá | Loại tiền | Option |
|---|---|---|---|---|---|---|
| 73 | 1 | Áo sợi len | NM | 500 | Vàng | Giáp+2 (#47) |
| 74 | 41 | Áo sợi gai | NM | 5.000 | Vàng | Giáp+4 (#47) |
| 75 | 4 | Áo len Pico | NM | 10.000 | Vàng | Giáp+8 (#47) |
| 76 | 42 | Áo thun Pico | NM | 20.000 | Vàng | Giáp+16 (#47) |
| 77 | 152 | Áo choàng len | NM | 50.000 | Vàng | Giáp+24 (#47) |
| 78 | 153 | Áo choàng thun | NM | 100.000 | Vàng | Giáp+40 (#47) |
| 79 | 154 | Áo vải Pico | NM | 200.000 | Vàng | Giáp+60 (#47) |
| 80 | 155 | Áo da Pico | NM | 500.000 | Vàng | Giáp+90 (#47) |
| 81 | 234 | Áo sắt Tron | NM | 2.000.000 | Vàng | Giáp+200 (#47) |
| 82 | 235 | Áo đồng Tron | NM | 5.800.000 | Vàng | Giáp+250 (#47) |
| 83 | 236 | Áo bạc Zealot | NM | 17.000.000 | Vàng | Giáp+300 (#47) |
| 84 | 237 | Áo vàng Zealot | NM | 52.000.000 | Vàng | Giáp+400 (#47) |
| 85 | 7 | Quần sợi len | NM | 400 | Vàng | HP+20 (#6) |
| 86 | 43 | Quần sợi gai | NM | 4.000 | Vàng | HP+25 (#6); +10 HP/30s (#27) |
| 87 | 10 | Quần vải thô Pico | NM | 8.000 | Vàng | HP+120 (#6); +28 HP/30s (#27) |
| 88 | 44 | Quần thun Pico | NM | 18.000 | Vàng | HP+250 (#6); +100 HP/30s (#27) |
| 89 | 156 | Quần len cứng | NM | 45.000 | Vàng | HP+600 (#6); +240 HP/30s (#27) |
| 90 | 157 | Quần thun cứng | NM | 90.000 | Vàng | HP+1200 (#6); +480 HP/30s (#27) |
| 91 | 158 | Quần vải cứng Pico | NM | 180.000 | Vàng | HP+2400 (#6); +960 HP/30s (#27) |
| 92 | 159 | Quần vải mềm Pico | NM | 400.000 | Vàng | HP+4800 (#6); +1800 HP/30s (#27) |
| 93 | 246 | Quần sắt Tron | NM | 2.000.000 | Vàng | HP+13000 (#6); +2200 HP/30s (#27) |
| 94 | 247 | Quần đồng Tron | NM | 5.800.000 | Vàng | HP+17000 (#6); +2700 HP/30s (#27) |
| 95 | 248 | Quần bạc Zealot | NM | 17.000.000 | Vàng | HP+21000 (#6); +3200 HP/30s (#27) |
| 96 | 249 | Quần vàng Zealot | NM | 52.000.000 | Vàng | HP+25000 (#6); +3700 HP/30s (#27) |

**Tab 5 – "Phụ kiện"**: 42 item đang bán

| item_shop.id | temp_id | Tên | HT | Giá | Loại tiền | Option |
|---|---|---|---|---|---|---|
| 97 | 22 | Găng sợi len | NM | 700 | Vàng | Tấn công+3 (#0) |
| 98 | 46 | Găng sợi gai | NM | 7.000 | Vàng | Tấn công+6 (#0) |
| 99 | 25 | Găng len Pico | NM | 14.000 | Vàng | Tấn công+12 (#0) |
| 100 | 45 | Găng thun Pico | NM | 30.000 | Vàng | Tấn công+24 (#0) |
| 101 | 160 | Găng len cứng | NM | 60.000 | Vàng | Tấn công+50 (#0) |
| 102 | 161 | Găng thun cứng | NM | 120.000 | Vàng | Tấn công+100 (#0) |
| 103 | 162 | Găng vải Pico | NM | 240.000 | Vàng | Tấn công+200 (#0) |
| 104 | 163 | Găng da Pico | NM | 600.000 | Vàng | Tấn công+500 (#0) |
| 105 | 258 | Găng sắt Tron | NM | 6.800.000 | Vàng | Tấn công+630 (#0) |
| 106 | 259 | Găng đồng Tron | NM | 20.000.000 | Vàng | Tấn công+950 (#0) |
| 107 | 260 | Găng bạc Zealot | NM | 60.000.000 | Vàng | Tấn công+1450 (#0) |
| 108 | 261 | Găng vàng Zealot | NM | 150.000.000 | Vàng | Tấn công+2150 (#0) |
| 109 | 28 | Giầy sợi len | NM | 300 | Vàng | KI+15 (#7) |
| 110 | 47 | Giầy sợi gai | NM | 3.000 | Vàng | KI+30 (#7); +6 KI/30s (#28) |
| 111 | 31 | Giầy nhựa Pico | NM | 6.000 | Vàng | KI+150 (#7); +30 KI/30s (#28) |
| 112 | 48 | Giầy cao su Pico | NM | 15.000 | Vàng | KI+300 (#7); +60 KI/30s (#28) |
| 113 | 164 | Giày nhựa cứng | NM | 30.000 | Vàng | KI+600 (#7); +120 KI/30s (#28) |
| 114 | 165 | Giày cao su cứng | NM | 70.000 | Vàng | KI+1500 (#7); +300 KI/30s (#28) |
| 115 | 166 | Giày da Pico | NM | 150.000 | Vàng | KI+3000 (#7); +600 KI/30s (#28) |
| 116 | 167 | Giày sắt Pico | NM | 350.000 | Vàng | KI+6000 (#7); +1200 KI/30s (#28) |
| 117 | 270 | Giày sắt Tron | NM | 1.300.000 | Vàng | KI+10000 (#7); +1700 KI/30s (#28) |
| 118 | 271 | Giày đồng Tron | NM | 3.800.000 | Vàng | KI+15000 (#7); +2200 KI/30s (#28) |
| 119 | 272 | Giày bạc Zealot | NM | 12.000.000 | Vàng | KI+20000 (#7); +2700 KI/30s (#28) |
| 120 | 273 | Giày vàng Zealot | NM | 34.000.000 | Vàng | KI+25000 (#7); +3200 KI/30s (#28) |
| 121 | 12 | Rada cấp 1 | Chung | 600 | Vàng | Chí mạng+1% (#14) |
| 122 | 57 | Rada cấp 2 | Chung | 6.000 | Vàng | Chí mạng+2% (#14) |
| 123 | 58 | Rada cấp 3 | Chung | 12.000 | Vàng | Chí mạng+3% (#14) |
| 124 | 59 | Rada cấp 4 | Chung | 24.000 | Vàng | Chí mạng+4% (#14) |
| 125 | 184 | Rada cấp 5 | Chung | 55.000 | Vàng | Chí mạng+5% (#14) |
| 126 | 185 | Rada cấp 6 | Chung | 110.000 | Vàng | Chí mạng+6% (#14) |
| 127 | 186 | Rada cấp 7 | Chung | 220.000 | Vàng | Chí mạng+7% (#14) |
| 128 | 187 | Rada cấp 8 | Chung | 550.000 | Vàng | Chí mạng+8% (#14) |
| 129 | 278 | Rada cấp 9 | Chung | 4.200.000 | Vàng | Chí mạng+9% (#14) |
| 130 | 279 | Rada cấp 10 | Chung | 20.000.000 | Vàng | Chí mạng+10% (#14) |
| 131 | 280 | Rada cấp 11 | Chung | 100.000.000 | Vàng | Chí mạng+11% (#14) |
| 132 | 281 | Rada cấp 12 | Chung | 150.000.000 | Vàng | Chí mạng+12% (#14) |
| 133 | 529 | Giáp tập luyện cấp 1 | Chung | 30 | Ngọc xanh | Hiệu lực trong 0 phút (#9) |
| 134 | 530 | Giáp tập luyện cấp 2 | Chung | 300 | Ngọc xanh | Hiệu lực trong 0 phút (#9) |
| 135 | 531 | Giáp tập luyện cấp 3 | Chung | 3.000 | Ngọc xanh | Hiệu lực trong 0 phút (#9) |
| 136 | 534 | Giáp tập luyện cấp 1 | Chung | 10.000.000 | Vàng | Hiệu lực trong 0 phút (#9) |
| 137 | 535 | Giáp tập luyện cấp 2 | Chung | 100.000.000 | Vàng | Hiệu lực trong 0 phút (#9) |
| 138 | 536 | Giáp tập luyện cấp 3 | Chung | 1.000.000.000 | Vàng | Hiệu lực trong 0 phút (#9) |

**Tab 6 – "Đặc biệt"**: 3 item đang bán

| item_shop.id | temp_id | Tên | HT | Giá | Loại tiền | Option |
|---|---|---|---|---|---|---|
| 140 | 194 | Viên Capsule đặc biệt | Chung | 1.000 | Ngọc xanh | — |
| 144 | 521 | Tự động luyện tập | Chung | 2 | Ngọc xanh | Thời gian sử dụng 20 phút (#1) |
| 636 | 361 | Gói 10 Rađa dò ngọc | Chung | 5.000.000 | Vàng | — |

#### Shop id=3 — `APPULE` (NPC 9 – Appule, type_shop=0)


**Tab 7 – "Áo Quần"**: 24 item đang bán

| item_shop.id | temp_id | Tên | HT | Giá | Loại tiền | Option |
|---|---|---|---|---|---|---|
| 145 | 2 | Áo vải thô | XD | 500 | Vàng | Giáp+3 (#47) |
| 146 | 49 | Áo thun thô | XD | 5.000 | Vàng | Giáp+5 (#47) |
| 147 | 5 | Áo giáp sắt | XD | 10.000 | Vàng | Giáp+10 (#47) |
| 148 | 50 | Áo giáp đồng | XD | 20.000 | Vàng | Giáp+20 (#47) |
| 149 | 168 | Áo giáp bạc | XD | 50.000 | Vàng | Giáp+30 (#47) |
| 150 | 169 | Áo giáp vàng | XD | 100.000 | Vàng | Giáp+50 (#47) |
| 151 | 170 | Áo lông Xayda | XD | 200.000 | Vàng | Giáp+70 (#47) |
| 152 | 171 | Áo khoác Xayda | XD | 500.000 | Vàng | Giáp+100 (#47) |
| 153 | 238 | Áo lông đỏ | XD | 2.000.000 | Vàng | Giáp+230 (#47) |
| 154 | 239 | Áo siêu xayda | XD | 5.800.000 | Vàng | Giáp+280 (#47) |
| 155 | 240 | Áo Kaio | XD | 17.000.000 | Vàng | Giáp+330 (#47) |
| 156 | 241 | Áo lưỡng long | XD | 52.000.000 | Vàng | Giáp+450 (#47) |
| 157 | 8 | Quần vải thô | XD | 400 | Vàng | HP+20 (#6) |
| 158 | 51 | Quần thun thô | XD | 4.000 | Vàng | HP+20 (#6); +8 HP/30s (#27) |
| 159 | 11 | Quần giáp sắt | XD | 8.000 | Vàng | HP+100 (#6); +20 HP/30s (#27) |
| 160 | 52 | Quần giáp đồng | XD | 18.000 | Vàng | HP+200 (#6); +80 HP/30s (#27) |
| 161 | 172 | Quần giáp bạc | XD | 45.000 | Vàng | HP+500 (#6); +200 HP/30s (#27) |
| 162 | 173 | Quần giáp vàng | XD | 90.000 | Vàng | HP+1000 (#6); +400 HP/30s (#27) |
| 163 | 174 | Quần lông Xayda | XD | 180.000 | Vàng | HP+2000 (#6); +800 HP/30s (#27) |
| 164 | 175 | Quần da Xayda | XD | 400.000 | Vàng | HP+4000 (#6); +1600 HP/30s (#27) |
| 165 | 250 | Quần lông đỏ | XD | 2.000.000 | Vàng | HP+12000 (#6); +2100 HP/30s (#27) |
| 166 | 251 | Quần siêu Xayda | XD | 5.800.000 | Vàng | HP+16000 (#6); +2600 HP/30s (#27) |
| 167 | 252 | Quần Kaio | XD | 17.000.000 | Vàng | HP+20000 (#6); +3100 HP/30s (#27) |
| 168 | 253 | Quần lưỡng long | XD | 52.000.000 | Vàng | HP+24000 (#6); +3600 HP/30s (#27) |

**Tab 8 – "Phụ kiện"**: 42 item đang bán

| item_shop.id | temp_id | Tên | HT | Giá | Loại tiền | Option |
|---|---|---|---|---|---|---|
| 169 | 23 | Găng vải thô | XD | 700 | Vàng | Tấn công+5 (#0) |
| 170 | 53 | Găng thun thô | XD | 7.000 | Vàng | Tấn công+8 (#0) |
| 171 | 26 | Găng sắt | XD | 14.000 | Vàng | Tấn công+16 (#0) |
| 172 | 54 | Găng đồng | XD | 30.000 | Vàng | Tấn công+32 (#0) |
| 173 | 176 | Găng bạc | XD | 60.000 | Vàng | Tấn công+60 (#0) |
| 174 | 177 | Găng vàng | XD | 120.000 | Vàng | Tấn công+120 (#0) |
| 175 | 178 | Găng lông Xayda | XD | 240.000 | Vàng | Tấn công+240 (#0) |
| 176 | 179 | Găng da Xayda | XD | 600.000 | Vàng | Tấn công+560 (#0) |
| 177 | 262 | Găng lông đỏ | XD | 6.800.000 | Vàng | Tấn công+700 (#0) |
| 178 | 263 | Găng siêu Xayda | XD | 20.000.000 | Vàng | Tấn công+1050 (#0) |
| 179 | 264 | Găng Kaio | XD | 60.000.000 | Vàng | Tấn công+1550 (#0) |
| 180 | 265 | Găng lưỡng long | XD | 150.000.000 | Vàng | Tấn công+2250 (#0) |
| 181 | 29 | Giầy vải thô | XD | 300 | Vàng | KI+10 (#7) |
| 182 | 55 | Giầy cao su thô | XD | 3.000 | Vàng | KI+20 (#7); +4 KI/30s (#28) |
| 183 | 32 | Giầy sắt | XD | 6.000 | Vàng | KI+100 (#7); +20 KI/30s (#28) |
| 184 | 56 | Giầy đồng | XD | 15.000 | Vàng | KI+200 (#7); +40 KI/30s (#28) |
| 185 | 180 | Giày bạc | XD | 30.000 | Vàng | KI+400 (#7); +80 KI/30s (#28) |
| 186 | 181 | Giày vàng | XD | 70.000 | Vàng | KI+1000 (#7); +200 KI/30s (#28) |
| 187 | 182 | Giày lông Xayda | XD | 150.000 | Vàng | KI+2000 (#7); +400 KI/30s (#28) |
| 188 | 183 | Giày da Xayda | XD | 350.000 | Vàng | KI+4000 (#7); +800 KI/30s (#28) |
| 189 | 274 | Giày lông đỏ | XD | 1.300.000 | Vàng | KI+8000 (#7); +1300 KI/30s (#28) |
| 190 | 275 | Giày siêu Xayda | XD | 3.800.000 | Vàng | KI+13000 (#7); +1800 KI/30s (#28) |
| 191 | 276 | Giày Kaio | XD | 12.000.000 | Vàng | KI+18000 (#7); +2300 KI/30s (#28) |
| 192 | 277 | Giày lưỡng long | XD | 34.000.000 | Vàng | KI+23000 (#7); +2800 KI/30s (#28) |
| 193 | 12 | Rada cấp 1 | Chung | 600 | Vàng | Chí mạng+1% (#14) |
| 194 | 57 | Rada cấp 2 | Chung | 6.000 | Vàng | Chí mạng+2% (#14) |
| 195 | 58 | Rada cấp 3 | Chung | 12.000 | Vàng | Chí mạng+3% (#14) |
| 196 | 59 | Rada cấp 4 | Chung | 24.000 | Vàng | Chí mạng+4% (#14) |
| 197 | 184 | Rada cấp 5 | Chung | 55.000 | Vàng | Chí mạng+5% (#14) |
| 198 | 185 | Rada cấp 6 | Chung | 110.000 | Vàng | Chí mạng+6% (#14) |
| 199 | 186 | Rada cấp 7 | Chung | 220.000 | Vàng | Chí mạng+7% (#14) |
| 200 | 187 | Rada cấp 8 | Chung | 550.000 | Vàng | Chí mạng+8% (#14) |
| 201 | 278 | Rada cấp 9 | Chung | 4.200.000 | Vàng | Chí mạng+9% (#14) |
| 202 | 279 | Rada cấp 10 | Chung | 20.000.000 | Vàng | Chí mạng+10% (#14) |
| 203 | 280 | Rada cấp 11 | Chung | 100.000.000 | Vàng | Chí mạng+11% (#14) |
| 204 | 281 | Rada cấp 12 | Chung | 150.000.000 | Vàng | Chí mạng+12% (#14) |
| 205 | 529 | Giáp tập luyện cấp 1 | Chung | 30 | Ngọc xanh | Hiệu lực trong 0 phút (#9) |
| 206 | 530 | Giáp tập luyện cấp 2 | Chung | 300 | Ngọc xanh | Hiệu lực trong 0 phút (#9) |
| 207 | 531 | Giáp tập luyện cấp 3 | Chung | 3.000 | Ngọc xanh | Hiệu lực trong 0 phút (#9) |
| 208 | 534 | Giáp tập luyện cấp 1 | Chung | 10.000.000 | Vàng | Hiệu lực trong 0 phút (#9) |
| 209 | 535 | Giáp tập luyện cấp 2 | Chung | 100.000.000 | Vàng | Hiệu lực trong 0 phút (#9) |
| 210 | 536 | Giáp tập luyện cấp 3 | Chung | 1.000.000.000 | Vàng | Hiệu lực trong 0 phút (#9) |

**Tab 9 – "Đặc biệt"**: 3 item đang bán

| item_shop.id | temp_id | Tên | HT | Giá | Loại tiền | Option |
|---|---|---|---|---|---|---|
| 212 | 194 | Viên Capsule đặc biệt | Chung | 1.000 | Ngọc xanh | — |
| 216 | 521 | Tự động luyện tập | Chung | 2 | Ngọc xanh | Thời gian sử dụng 20 phút (#1) |
| 637 | 361 | Gói 10 Rađa dò ngọc | Chung | 5.000.000 | Vàng | — |

#### Shop id=4 — `URON` (NPC 16 – Uron, type_shop=0)


**Tab 10 – "Sách Võ"**: 41 item đang bán

| item_shop.id | temp_id | Tên | HT | Giá | Loại tiền | Option |
|---|---|---|---|---|---|---|
| 218 | 67 | Sách đấm Dragon lv2 | TĐ | 10 | Ngọc xanh | Sức đánh+110% (#50) |
| 219 | 68 | Sách đấm Dragon lv3 | TĐ | 15 | Ngọc xanh | Sức đánh+120% (#50) |
| 220 | 69 | Sách đấm Dragon lv4 | TĐ | 20 | Ngọc xanh | Sức đánh+130% (#50) |
| 221 | 70 | Sách đấm Dragon lv5 | TĐ | 25 | Ngọc xanh | Sức đánh+140% (#50) |
| 222 | 71 | Sách đấm Dragon lv6 | TĐ | 30 | Ngọc xanh | Sức đánh+150% (#50) |
| 223 | 72 | Sách đấm Dragon lv7 | TĐ | 35 | Ngọc xanh | Sức đánh+160% (#50) |
| 224 | 300 | Kaioken lv1 | TĐ | 40 | Ngọc xanh | Sức đánh+160% (#50) |
| 225 | 301 | Kaioken lv2 | TĐ | 50 | Ngọc xanh | Sức đánh+170% (#50) |
| 226 | 302 | Kaioken lv3 | TĐ | 60 | Ngọc xanh | Sức đánh+180% (#50) |
| 227 | 303 | Kaioken lv4 | TĐ | 70 | Ngọc xanh | Sức đánh+190% (#50) |
| 228 | 304 | Kaioken lv5 | TĐ | 80 | Ngọc xanh | Sức đánh+200% (#50) |
| 229 | 305 | Kaioken lv6 | TĐ | 90 | Ngọc xanh | Sức đánh+210% (#50) |
| 230 | 306 | Kaioken lv7 | TĐ | 100 | Ngọc xanh | Sức đánh+220% (#50) |
| 231 | 488 | Sách Dịch Chuyển lv1 | TĐ | 100 | Ngọc xanh | Tới ngay mục tiêu và gây choáng trong 1000 mili giây (#118) |
| 232 | 489 | Sách Dịch Chuyển lv2 | TĐ | 120 | Ngọc xanh | Tới ngay mục tiêu và gây choáng trong 1500 mili giây (#118) |
| 233 | 490 | Sách Dịch Chuyển lv3 | TĐ | 140 | Ngọc xanh | Tới ngay mục tiêu và gây choáng trong 2000 mili giây (#118) |
| 234 | 491 | Sách Dịch Chuyển lv4 | TĐ | 160 | Ngọc xanh | Tới ngay mục tiêu và gây choáng trong 2500 mili giây (#118) |
| 235 | 492 | Sách Dịch Chuyển lv5 | TĐ | 180 | Ngọc xanh | Tới ngay mục tiêu và gây choáng trong 3000 mili giây (#118) |
| 236 | 493 | Sách Dịch Chuyển lv6 | TĐ | 200 | Ngọc xanh | Tới ngay mục tiêu và gây choáng trong 3500 mili giây (#118) |
| 237 | 494 | Sách Dịch Chuyển lv7 | TĐ | 220 | Ngọc xanh | Tới ngay mục tiêu và gây choáng trong 4000 mili giây (#118) |
| 273 | 79 | Sách đấm Demon lv1 | NM | 5 | Ngọc xanh | Sức đánh+95% (#50) |
| 274 | 80 | Sách đấm Demon lv2 | NM | 10 | Ngọc xanh | Sức đánh+105% (#50) |
| 275 | 81 | Sách đấm Demon lv3 | NM | 15 | Ngọc xanh | Sức đánh+115% (#50) |
| 276 | 82 | Sách đấm Demon lv4 | NM | 20 | Ngọc xanh | Sức đánh+125% (#50) |
| 277 | 83 | Sách đấm Demon lv5 | NM | 25 | Ngọc xanh | Sức đánh+135% (#50) |
| 278 | 84 | Sách đấm Demon lv6 | NM | 30 | Ngọc xanh | Sức đánh+145% (#50) |
| 279 | 86 | Sách đấm Demon lv7 | NM | 35 | Ngọc xanh | Sức đánh+155% (#50) |
| 280 | 481 | Sách Liên hoàn lv1 | NM | 100 | Ngọc xanh | Sức đánh+160% (#50) |
| 281 | 482 | Sách Liên hoàn lv2 | NM | 120 | Ngọc xanh | Sức đánh+165% (#50) |
| 282 | 483 | Sách Liên hoàn lv3 | NM | 140 | Ngọc xanh | Sức đánh+170% (#50) |
| 283 | 484 | Sách Liên hoàn lv4 | NM | 160 | Ngọc xanh | Sức đánh+175% (#50) |
| 284 | 485 | Sách Liên hoàn lv5 | NM | 180 | Ngọc xanh | Sức đánh+180% (#50) |
| 285 | 486 | Sách Liên hoàn lv6 | NM | 200 | Ngọc xanh | Sức đánh+185% (#50) |
| 286 | 487 | Sách Liên hoàn lv7 | NM | 220 | Ngọc xanh | Sức đánh+190% (#50) |
| 322 | 87 | Sách đấm Galick lv1 | XD | 5 | Ngọc xanh | Sức đánh+100% (#50) |
| 323 | 88 | Sách đấm Galick lv2 | XD | 10 | Ngọc xanh | Sức đánh+110% (#50) |
| 324 | 89 | Sách đấm Galick lv3 | XD | 15 | Ngọc xanh | Sức đánh+120% (#50) |
| 325 | 90 | Sách đấm Galick lv4 | XD | 20 | Ngọc xanh | Sức đánh+130% (#50) |
| 326 | 91 | Sách đấm Galick lv5 | XD | 25 | Ngọc xanh | Sức đánh+140% (#50) |
| 327 | 92 | Sách đấm Galick lv6 | XD | 30 | Ngọc xanh | Sức đánh+150% (#50) |
| 328 | 93 | Sách đấm Galick lv7 | XD | 35 | Ngọc xanh | Sức đánh+160% (#50) |

**Tab 11 – "Sách Chưởng"**: 56 item đang bán

| item_shop.id | temp_id | Tên | HT | Giá | Loại tiền | Option |
|---|---|---|---|---|---|---|
| 238 | 94 | Sách Kamejoko lv1 | TĐ | 10 | Ngọc xanh | Sức đánh+150% (#50) |
| 239 | 95 | Sách Kamejoko lv2 | TĐ | 20 | Ngọc xanh | Sức đánh+200% (#50) |
| 240 | 96 | Sách Kamejoko lv3 | TĐ | 30 | Ngọc xanh | Sức đánh+250% (#50) |
| 241 | 97 | Sách Kamejoko lv4 | TĐ | 40 | Ngọc xanh | Sức đánh+300% (#50) |
| 242 | 98 | Sách Kamejoko lv5 | TĐ | 50 | Ngọc xanh | Sức đánh+350% (#50) |
| 243 | 99 | Sách Kamejoko lv6 | TĐ | 60 | Ngọc xanh | Sức đánh+400% (#50) |
| 244 | 100 | Sách Kamejoko lv7 | TĐ | 70 | Ngọc xanh | Sức đánh+450% (#50) |
| 245 | 495 | Sách Thôi Miên lv1 | TĐ | 100 | Ngọc xanh | Ru ngủ trong 5 giây (#121); Tỉnh giấc bị yếu đi -25% sức đánh trong 10 giây (#124) |
| 246 | 496 | Sách Thôi Miên lv2 | TĐ | 120 | Ngọc xanh | Ru ngủ trong 6 giây (#121); Tỉnh giấc bị yếu đi -30% sức đánh trong 10 giây (#124) |
| 247 | 497 | Sách Thôi Miên lv3 | TĐ | 140 | Ngọc xanh | Ru ngủ trong 7 giây (#121); Tỉnh giấc bị yếu đi -35% sức đánh trong 10 giây (#124) |
| 248 | 498 | Sách Thôi Miên lv4 | TĐ | 160 | Ngọc xanh | Ru ngủ trong 8 giây (#121); Tỉnh giấc bị yếu đi -40% sức đánh trong 10 giây (#124) |
| 249 | 499 | Sách Thôi Miên lv5 | TĐ | 180 | Ngọc xanh | Ru ngủ trong 9 giây (#121); Tỉnh giấc bị yếu đi -45% sức đánh trong 10 giây (#124) |
| 250 | 500 | Sách Thôi Miên lv6 | TĐ | 200 | Ngọc xanh | Ru ngủ trong 10 giây (#121); Tỉnh giấc bị yếu đi -50% sức đánh trong 10 giây (#124) |
| 251 | 501 | Sách Thôi Miên lv7 | TĐ | 220 | Ngọc xanh | Ru ngủ trong 11 giây (#121); Tỉnh giấc bị yếu đi -55% sức đánh trong 10 giây (#124) |
| 287 | 101 | Sách Masenko lv1 | NM | 10 | Ngọc xanh | Sức đánh+100% (#50) |
| 288 | 102 | Sách Masenko lv2 | NM | 20 | Ngọc xanh | Sức đánh+110% (#50) |
| 289 | 103 | Sách Masenko lv3 | NM | 30 | Ngọc xanh | Sức đánh+120% (#50) |
| 290 | 104 | Sách Masenko lv4 | NM | 40 | Ngọc xanh | Sức đánh+130% (#50) |
| 291 | 105 | Sách Masenko lv5 | NM | 50 | Ngọc xanh | Sức đánh+140% (#50) |
| 292 | 106 | Sách Masenko lv6 | NM | 60 | Ngọc xanh | Sức đánh+150% (#50) |
| 293 | 107 | Sách Masenko lv7 | NM | 70 | Ngọc xanh | Sức đánh+160% (#50) |
| 294 | 328 | Makankosappo lv1 | NM | 100 | Ngọc xanh | Sức hủy diệt+70% (#78) |
| 295 | 329 | Makankosappo lv2 | NM | 120 | Ngọc xanh | Sức hủy diệt+80% (#78) |
| 296 | 330 | Makankosappo lv3 | NM | 140 | Ngọc xanh | Sức hủy diệt+90% (#78) |
| 297 | 331 | Makankosappo lv4 | NM | 160 | Ngọc xanh | Sức hủy diệt+100% (#78) |
| 298 | 332 | Makankosappo lv5 | NM | 180 | Ngọc xanh | Sức hủy diệt+110% (#78) |
| 299 | 333 | Makankosappo lv6 | NM | 200 | Ngọc xanh | Sức hủy diệt+120% (#78) |
| 300 | 334 | Makankosappo lv7 | NM | 220 | Ngọc xanh | Sức hủy diệt+130% (#78) |
| 301 | 474 | Sách Biến Sôcôla lv1 | NM | 100 | Ngọc xanh | Biến sôcôla làm yếu đi -15% sức đánh trong 30 giây (#126) |
| 302 | 475 | Sách Biến Sôcôla lv2 | NM | 120 | Ngọc xanh | Biến sôcôla làm yếu đi -17% sức đánh trong 30 giây (#126) |
| 303 | 476 | Sách Biến Sôcôla lv3 | NM | 140 | Ngọc xanh | Biến sôcôla làm yếu đi -19% sức đánh trong 30 giây (#126) |
| 304 | 477 | Sách Biến Sôcôla lv4 | NM | 160 | Ngọc xanh | Biến sôcôla làm yếu đi -21% sức đánh trong 30 giây (#126) |
| 305 | 478 | Sách Biến Sôcôla lv5 | NM | 180 | Ngọc xanh | Biến sôcôla làm yếu đi -23% sức đánh trong 30 giây (#126) |
| 306 | 479 | Sách Biến Sôcôla lv6 | NM | 200 | Ngọc xanh | Biến sôcôla làm yếu đi -25% sức đánh trong 30 giây (#126) |
| 307 | 480 | Sách Biến Sôcôla lv7 | NM | 220 | Ngọc xanh | Biến sôcôla làm yếu đi -27% sức đánh trong 30 giây (#126) |
| 329 | 108 | Sách Antomic lv1 | XD | 10 | Ngọc xanh | Sức đánh+100% (#50) |
| 330 | 109 | Sách Antomic lv2 | XD | 20 | Ngọc xanh | Sức đánh+110% (#50) |
| 331 | 110 | Sách Antomic lv3 | XD | 30 | Ngọc xanh | Sức đánh+120% (#50) |
| 332 | 111 | Sách Antomic lv4 | XD | 40 | Ngọc xanh | Sức đánh+130% (#50) |
| 333 | 112 | Sách Antomic lv5 | XD | 50 | Ngọc xanh | Sức đánh+140% (#50) |
| 334 | 113 | Sách Antomic lv6 | XD | 60 | Ngọc xanh | Sức đánh+150% (#50) |
| 335 | 114 | Sách Antomic lv7 | XD | 70 | Ngọc xanh | Sức đánh+160% (#50) |
| 336 | 321 | Bom hi sinh lv1 | XD | 40 | Ngọc xanh | Sức hủy diệt+100% (#78) |
| 337 | 322 | Bom hi sinh lv2 | XD | 50 | Ngọc xanh | Sức hủy diệt+105% (#78) |
| 338 | 323 | Bom hi sinh lv3 | XD | 60 | Ngọc xanh | Sức hủy diệt+110% (#78) |
| 339 | 324 | Bom hi sinh lv4 | XD | 70 | Ngọc xanh | Sức hủy diệt+115% (#78) |
| 340 | 325 | Bom hi sinh lv5 | XD | 80 | Ngọc xanh | Sức hủy diệt+120% (#78) |
| 341 | 326 | Bom hi sinh lv6 | XD | 90 | Ngọc xanh | Sức hủy diệt+125% (#78) |
| 342 | 327 | Bom hi sinh lv7 | XD | 100 | Ngọc xanh | Sức hủy diệt+130% (#78) |
| 343 | 502 | Sách Trói lv1 | XD | 100 | Ngọc xanh | Trói gô mục tiêu trong 5 giây (#123) |
| 344 | 503 | Sách Trói lv2 | XD | 120 | Ngọc xanh | Trói gô mục tiêu trong 10 giây (#123) |
| 345 | 504 | Sách Trói lv3 | XD | 140 | Ngọc xanh | Trói gô mục tiêu trong 15 giây (#123) |
| 346 | 505 | Sách Trói lv4 | XD | 160 | Ngọc xanh | Trói gô mục tiêu trong 20 giây (#123) |
| 347 | 506 | Sách Trói lv5 | XD | 180 | Ngọc xanh | Trói gô mục tiêu trong 25 giây (#123) |
| 348 | 507 | Sách Trói lv6 | XD | 200 | Ngọc xanh | Trói gô mục tiêu trong 30 giây (#123) |
| 349 | 508 | Sách Trói lv7 | XD | 220 | Ngọc xanh | Trói gô mục tiêu trong 35 giây (#123) |

**Tab 12 – "Sách Đặc biệt"**: 56 item đang bán

| item_shop.id | temp_id | Tên | HT | Giá | Loại tiền | Option |
|---|---|---|---|---|---|---|
| 252 | 115 | Thái Dương Hạ San lv1 | TĐ | 30 | Ngọc xanh | Gây mù xung quanh trong 3 giây (#119) |
| 253 | 116 | Thái Dương Hạ San lv2 | TĐ | 40 | Ngọc xanh | Gây mù xung quanh trong 4 giây (#119) |
| 254 | 117 | Thái Dương Hạ San lv3 | TĐ | 50 | Ngọc xanh | Gây mù xung quanh trong 5 giây (#119) |
| 255 | 118 | Thái Dương Hạ San lv4 | TĐ | 60 | Ngọc xanh | Gây mù xung quanh trong 6 giây (#119) |
| 256 | 119 | Thái Dương Hạ San lv5 | TĐ | 70 | Ngọc xanh | Gây mù xung quanh trong 7 giây (#119) |
| 257 | 120 | Thái Dương Hạ San lv6 | TĐ | 80 | Ngọc xanh | Gây mù xung quanh trong 8 giây (#119) |
| 258 | 121 | Thái Dương Hạ San lv7 | TĐ | 90 | Ngọc xanh | Gây mù xung quanh trong 9 giây (#119) |
| 259 | 307 | Quả cầu Kênh Khi lv1 | TĐ | 100 | Ngọc xanh | Ra đòn sau 360 giây (#120) |
| 260 | 308 | Quả cầu Kênh Khi lv2 | TĐ | 120 | Ngọc xanh | Ra đòn sau 350 giây (#120) |
| 261 | 309 | Quả cầu Kênh Khi lv3 | TĐ | 140 | Ngọc xanh | Ra đòn sau 340 giây (#120) |
| 262 | 310 | Quả cầu Kênh Khi lv4 | TĐ | 160 | Ngọc xanh | Ra đòn sau 330 giây (#120) |
| 263 | 311 | Quả cầu Kênh Khi lv5 | TĐ | 180 | Ngọc xanh | Ra đòn sau 320 giây (#120) |
| 264 | 312 | Quả cầu Kênh Khi lv6 | TĐ | 200 | Ngọc xanh | Ra đòn sau 310 giây (#120) |
| 265 | 313 | Quả cầu Kênh Khi lv7 | TĐ | 220 | Ngọc xanh | Ra đòn sau 300 giây (#120) |
| 266 | 434 | Khiên năng lượng lv1 | Chung | 100 | Ngọc xanh | Bảo vệ trong 15 giây (#122) |
| 267 | 435 | Khiên năng lượng lv2 | Chung | 200 | Ngọc xanh | Bảo vệ trong 20 giây (#122) |
| 268 | 436 | Khiên năng lượng lv3 | Chung | 300 | Ngọc xanh | Bảo vệ trong 25 giây (#122) |
| 269 | 437 | Khiên năng lượng lv4 | Chung | 400 | Ngọc xanh | Bảo vệ trong 30 giây (#122) |
| 270 | 438 | Khiên năng lượng lv5 | Chung | 500 | Ngọc xanh | Bảo vệ trong 35 giây (#122) |
| 271 | 439 | Khiên năng lượng lv6 | Chung | 600 | Ngọc xanh | Bảo vệ trong 40 giây (#122) |
| 272 | 440 | Khiên năng lượng lv7 | Chung | 700 | Ngọc xanh | Bảo vệ trong 45 giây (#122) |
| 308 | 122 | Sách học Trị thương lv1 | NM | 30 | Ngọc xanh | Phục hồi 50% HP và KI cho đồng đội (#173) |
| 309 | 123 | Sách học Trị thương lv2 | NM | 40 | Ngọc xanh | Phục hồi 55% HP và KI cho đồng đội (#173) |
| 310 | 124 | Sách học Trị thương lv3 | NM | 50 | Ngọc xanh | Phục hồi 60% HP và KI cho đồng đội (#173) |
| 311 | 125 | Sách học Trị thương lv4 | NM | 60 | Ngọc xanh | Phục hồi 65% HP và KI cho đồng đội (#173) |
| 312 | 126 | Sách học Trị thương lv5 | NM | 70 | Ngọc xanh | Phục hồi 70% HP và KI cho đồng đội (#173) |
| 313 | 127 | Sách học Trị thương lv6 | NM | 80 | Ngọc xanh | Phục hồi 75% HP và KI cho đồng đội (#173) |
| 314 | 128 | Sách học Trị thương lv7 | NM | 90 | Ngọc xanh | Phục hồi 80% HP và KI cho đồng đội (#173) |
| 315 | 335 | Đẻ trứng lv1 | NM | 60 | Ngọc xanh | Đệ tử 50% sức đánh (#79) |
| 316 | 336 | Đẻ trứng lv2 | NM | 70 | Ngọc xanh | Đệ tử 55% sức đánh (#79) |
| 317 | 337 | Đẻ trứng lv3 | NM | 80 | Ngọc xanh | Đệ tử 60% sức đánh (#79) |
| 318 | 338 | Đẻ trứng lv4 | NM | 90 | Ngọc xanh | Đệ tử 65% sức đánh (#79) |
| 319 | 339 | Đẻ trứng lv5 | NM | 100 | Ngọc xanh | Đệ tử 70% sức đánh (#79) |
| 320 | 340 | Đẻ trứng lv6 | NM | 110 | Ngọc xanh | Đệ tử 75% sức đánh (#79) |
| 321 | 341 | Đẻ trứng lv7 | NM | 120 | Ngọc xanh | Đệ tử 80% sức đánh (#79) |
| 350 | 129 | Tái tạo năng lượng lv1 | XD | 30 | Ngọc xanh | Sức đánh+100% (#50) |
| 351 | 130 | Tái tạo năng lượng lv2 | XD | 40 | Ngọc xanh | Sức đánh+110% (#50) |
| 352 | 131 | Tái tạo năng lượng lv3 | XD | 50 | Ngọc xanh | Sức đánh+120% (#50) |
| 353 | 132 | Tái tạo năng lượng lv4 | XD | 60 | Ngọc xanh | Sức đánh+130% (#50) |
| 354 | 133 | Tái tạo năng lượng lv5 | XD | 70 | Ngọc xanh | Sức đánh+140% (#50) |
| 355 | 134 | Tái tạo năng lượng lv6 | XD | 80 | Ngọc xanh | Sức đánh+150% (#50) |
| 356 | 135 | Tái tạo năng lượng lv7 | XD | 90 | Ngọc xanh | Sức đánh+160% (#50) |
| 357 | 314 | Hóa khỉ khổng lồ lv1 | XD | 100 | Ngọc xanh | Tấn công+109% (#49); HP+40% (#77) |
| 358 | 315 | Hóa khỉ khổng lồ lv2 | XD | 120 | Ngọc xanh | Tấn công+110% (#49); HP+50% (#77) |
| 359 | 316 | Hóa khỉ khổng lồ lv3 | XD | 140 | Ngọc xanh | Tấn công+110% (#49); HP+60% (#77) |
| 360 | 317 | Hóa khỉ khổng lồ lv4 | XD | 160 | Ngọc xanh | Tấn công+112% (#49); HP+70% (#77) |
| 361 | 318 | Hóa khỉ khổng lồ lv5 | XD | 170 | Ngọc xanh | Tấn công+113% (#49); HP+80% (#77) |
| 362 | 319 | Hóa khỉ khổng lồ lv6 | XD | 180 | Ngọc xanh | Tấn công+114% (#49); HP+90% (#77) |
| 363 | 320 | Hóa khỉ khổng lồ lv7 | XD | 200 | Ngọc xanh | Tấn công+115% (#49); HP+100% (#77) |
| 364 | 509 | Sách Huýt Sáo lv1 | XD | 100 | Ngọc xanh | Tăng và hồi phục 40% HP tạm thời cho mình và xung quanh trong 30 giây (#125) |
| 365 | 510 | Sách Huýt Sáo lv2 | XD | 120 | Ngọc xanh | Tăng và hồi phục 50% HP tạm thời cho mình và xung quanh trong 30 giây (#125) |
| 366 | 511 | Sách Huýt Sáo lv3 | XD | 140 | Ngọc xanh | Tăng và hồi phục 60% HP tạm thời cho mình và xung quanh trong 30 giây (#125) |
| 367 | 512 | Sách Huýt Sáo lv4 | XD | 160 | Ngọc xanh | Tăng và hồi phục 70% HP tạm thời cho mình và xung quanh trong 30 giây (#125) |
| 368 | 513 | Sách Huýt Sáo lv5 | XD | 180 | Ngọc xanh | Tăng và hồi phục 80% HP tạm thời cho mình và xung quanh trong 30 giây (#125) |
| 369 | 514 | Sách Huýt Sáo lv6 | XD | 200 | Ngọc xanh | Tăng và hồi phục 90% HP tạm thời cho mình và xung quanh trong 30 giây (#125) |
| 370 | 515 | Sách Huýt Sáo lv7 | XD | 220 | Ngọc xanh | Tăng và hồi phục 100% HP tạm thời cho mình và xung quanh trong 30 giây (#125) |

**Tab 19 – "Phụ kiện"**: 50 item đang bán, 9 item is_sell=0 (ẩn)

| item_shop.id | temp_id | Tên | HT | Giá | Loại tiền | Option |
|---|---|---|---|---|---|---|
| 700 | 849 | Pháo Thăng Thiên | Chung | 999 | Ngọc xanh | Dùng để bay không tốn KI (#84); +25% tốc độ chạy (#148) |
| 701 | 761 | Avatar đeo khẩu trang | TĐ | 70 | Ngọc xanh | HP+15% (#77); HP+15%/30s (#80); Sức đánh+10% (#50); Hạn sử dụng 7 ngày (#93) |
| 702 | 204 | Avatar | TĐ | 99 | Ngọc xanh | Thên Xin Hăng (#57) |
| 703 | 205 | Avatar | TĐ | 199 | Ngọc xanh | Pic (#58) |
| 704 | 203 | Avatar | TĐ | 299 | Ngọc xanh | Ca Lích (#56) |
| 705 | 210 | Avatar | TĐ | 299 | Ngọc xanh | Sôn Gô Tên (#60) |
| 706 | 201 | Avatar | TĐ | 499 | Ngọc xanh | Mr. Santa (#54) |
| 707 | 688 | Avatar | TĐ | 599 | Ngọc xanh | Giảm 50% sức đánh, HP, KI và +100% SM, TN, vàng từ quái (#155) |
| 708 | 689 | Avatar | TĐ | 599 | Ngọc xanh | Giảm 50% mọi sát thương khi KI dưới 20% (#157) |
| 709 | 690 | Avatar | TĐ | 599 | Ngọc xanh | Cộng dồn 1% sức đánh khi chỉ dùng chiêu đấm, tối đa 50% (#156) |
| 710 | 763 | Avatar đeo khẩu trang | XD | 70 | Ngọc xanh | HP+15% (#77); HP+15%/30s (#80); Sức đánh+10% (#50); Hạn sử dụng 7 ngày (#93) |
| 711 | 200 | Avatar | XD | 199 | Ngọc xanh | Broly (#53) |
| 712 | 209 | Avatar | XD | 299 | Ngọc xanh | Sôn Gô Ku (#61) |
| 713 | 196 | Avatar | XD | 499 | Ngọc xanh | Sôn Gô Ku ss1 (#51) |
| 714 | 198 | Avatar | XD | 499 | Ngọc xanh | Ca Đic siêu ss1 (#52) |
| 715 | 685 | Avatar | XD | 599 | Ngọc xanh | Giảm 50% mọi sát thương khi KI dưới 20% (#157) |
| 716 | 686 | Avatar | XD | 599 | Ngọc xanh | Giảm 50% sức đánh, HP, KI và +100% SM, TN, vàng từ quái (#155) |
| 717 | 687 | Avatar | XD | 599 | Ngọc xanh | Cộng dồn 1% sức đánh khi chỉ dùng chiêu đấm, tối đa 50% (#156) |
| 718 | 197 | Avatar | XD | 999 | Ngọc xanh | Sôn Gô Ku ss3 (#51) |
| 719 | 199 | Avatar | XD | 999 | Ngọc xanh | Ca Đic siêu ss3 (#52) |
| 720 | 202 | Avatar | XD | 999 | Ngọc xanh | Broly ss1 (#55) |
| 721 | 762 | Avatar đeo khẩu trang | NM | 70 | Ngọc xanh | HP+15% (#77); HP+15%/30s (#80); Sức đánh+10% (#50); Hạn sử dụng 7 ngày (#93) |
| 722 | 206 | Avatar | NM | 199 | Ngọc xanh | Siêu na mếc 1 (#59) |
| 723 | 207 | Avatar | NM | 199 | Ngọc xanh | Siêu na mếc 2 (#59) |
| 724 | 208 | Avatar | NM | 199 | Ngọc xanh | Siêu na mếc 3 (#59) |
| 725 | 682 | Avatar | NM | 599 | Ngọc xanh | Giảm 50% sức đánh, HP, KI và +100% SM, TN, vàng từ quái (#155) |
| 726 | 683 | Avatar | NM | 599 | Ngọc xanh | Cộng dồn 1% sức đánh khi chỉ dùng chiêu đấm, tối đa 50% (#156) |
| 727 | 684 | Avatar | NM | 599 | Ngọc xanh | Giảm 50% mọi sát thương khi KI dưới 20% (#157) |
| 728 | 226 | Bình nước phép | Chung | 10 | Ngọc xanh | Dùng để làm phép (#75) |
| 729 | 342 | Vệ tinh trí lực | Chung | 5 | Ngọc xanh | KI+5%/30s (#81) |
| 730 | 343 | Vệ tinh trí tuệ | Chung | 5 | Ngọc xanh | Tăng 20% sức mạnh và tiềm năng nhận được khi đánh quái (#83) |
| 731 | 344 | Vệ tinh phòng thủ | Chung | 5 | Ngọc xanh | Không bị quái chủ động đánh và giảm 20% sát thương khi bị đánh (#82) |
| 732 | 345 | Vệ tinh sinh lực | Chung | 5 | Ngọc xanh | HP+5%/30s (#80) |
| 733 | 347 | Phi Long | NM | 50 | Ngọc xanh | Dùng để bay không tốn KI (#84) |
| 734 | 744 | Cột nhà | Chung | 1.000 | Ngọc xanh | Ngầu +20% sức đánh lên quái khi bay với Cải Trang Tàu Pảy Pảy (#166); Dùng để bay không tốn KI (#84); +25% tốc độ chạy (#148) |
| 735 | 795 | Ghế bay | Chung | 1.500 | Ngọc xanh | Ngầu +22% sức đánh lên quái khi bay với Cải Trang nhà Fide (#170); Dùng để bay không tốn KI (#84); +25% tốc độ chạy (#148) |
| 736 | 350 | Phi Long VIP | NM | 500 | Ngọc xanh | Dùng để bay và phục hồi KI (#85) |
| 737 | 532 | Quỷ Chim | Chung | 1.000 | Ngọc xanh | Dùng để bay và phục hồi HP, KI (#89) |
| 738 | 460 | Cục xương | Chung | 1 | Ngọc xanh | Ném cho Sói Hẹc Quyn (#113) |
| 739 | 297 | Gói 30 đậu thần cấp 5 | Chung | 10 | Ngọc xanh | HP, KI+8000 (#48) |
| 740 | 298 | Gói 30 đậu thần cấp 6 | Chung | 10 | Ngọc xanh | HP, KI+16000 (#48) |
| 741 | 299 | Gói 30 đậu thần cấp 7 | Chung | 10 | Ngọc xanh | HP, KI+32000 (#48) |
| 742 | 596 | Gói 30 đậu thần cấp 8 | Chung | 10 | Ngọc xanh | HP, KI+64000 (#48) |
| 743 | 597 | Gói 30 đậu thần cấp 9 | Chung | 10 | Ngọc xanh | HP, KI+128000 (#48) |
| 744 | 296 | Gói 30 đậu thần cấp 4 | Chung | 10 | Ngọc xanh | HP, KI+4000 (#48) |
| 745 | 295 | Gói 30 đậu thần cấp 3 | Chung | 10 | Ngọc xanh | HP, KI+2000 (#48) |
| 746 | 294 | Gói 30 đậu thần cấp 2 | Chung | 10 | Ngọc xanh | HP, KI+500 (#48) |
| 747 | 293 | Gói 30 đậu thần cấp 1 | Chung | 10 | Ngọc xanh | HP, KI+100 (#48) |
| 748 | 453 | Chiến thuyền Tennis | Chung | 50 | Ngọc xanh | Không thể giao dịch (#30) |
| 749 | 454 | Bông tai Porata | Chung | 1.000 | Ngọc xanh | Không thể giao dịch (#30) |

Item is_sell=0 (không load): 346 Cân đẩu vân, 348 Ván bay, 347 Phi Long, 532 Quỷ Chim, 349 Cân đẩu vân VIP, 350 Phi Long VIP, 351 Ván bay VIP, 453 Chiến thuyền Tennis, 454 Bông tai Porata

#### Shop id=5 — `SANTA_HEAD` (NPC 39 – Santa, type_shop=0)


**Tab 13 – "Tiệm Hớt tóc"**: 9 item đang bán, 8 item is_sell=0 (ẩn)

| item_shop.id | temp_id | Tên | HT | Giá | Loại tiền | Option |
|---|---|---|---|---|---|---|
| 371 | 412 | Avatar | TĐ | 0 | Vàng | — |
| 372 | 413 | Avatar | TĐ | 0 | Vàng | — |
| 373 | 414 | Avatar | TĐ | 0 | Vàng | — |
| 374 | 415 | Avatar | NM | 0 | Vàng | — |
| 375 | 416 | Avatar | NM | 0 | Vàng | — |
| 376 | 417 | Avatar | NM | 0 | Vàng | — |
| 377 | 418 | Avatar | XD | 0 | Vàng | — |
| 378 | 419 | Avatar | XD | 0 | Vàng | — |
| 379 | 420 | Avatar | XD | 0 | Vàng | — |

Item is_sell=0 (không load): 682 Avatar, 683 Avatar, 684 Avatar, 685 Avatar, 686 Avatar, 687 Avatar, 689 Avatar, 690 Avatar

#### Shop id=6 — `BUA_1H` (NPC 21 – Bà Hạt Mít, type_shop=0)


**Tab 14 – "Bùa 1 giờ"**: 10 item đang bán

| item_shop.id | temp_id | Tên | HT | Giá | Loại tiền | Option |
|---|---|---|---|---|---|---|
| 380 | 672 | Bùa Trí Tuệ x4 | Chung | 45 | Ngọc xanh | Chưa có (#66) |
| 381 | 671 | Bùa Trí Tuệ x3 | Chung | 15 | Ngọc xanh | Chưa có (#66) |
| 382 | 522 | Bùa Đệ Tử | Chung | 10 | Ngọc xanh | Chưa có (#66) |
| 383 | 219 | Bùa Thu Hút | Chung | 2 | Ngọc xanh | Chưa có (#66) |
| 384 | 218 | Bùa Dẻo Dai | Chung | 1 | Ngọc xanh | Chưa có (#66) |
| 385 | 217 | Bùa Bất Tử | Chung | 7 | Ngọc xanh | Chưa có (#66) |
| 386 | 216 | Bùa Oai Hùng | Chung | 7 | Ngọc xanh | Chưa có (#66) |
| 387 | 215 | Bùa Da Trâu | Chung | 3 | Ngọc xanh | Chưa có (#66) |
| 388 | 214 | Bùa Mạnh Mẽ | Chung | 5 | Ngọc xanh | Chưa có (#66) |
| 389 | 213 | Bùa Trí Tuệ | Chung | 5 | Ngọc xanh | Chưa có (#66) |

#### Shop id=7 — `BUA_8H` (NPC 21 – Bà Hạt Mít, type_shop=0)


**Tab 15 – "Bùa 8 giờ"**: 10 item đang bán

| item_shop.id | temp_id | Tên | HT | Giá | Loại tiền | Option |
|---|---|---|---|---|---|---|
| 390 | 672 | Bùa Trí Tuệ x4 | Chung | 180 | Ngọc xanh | Chưa có (#66) |
| 391 | 671 | Bùa Trí Tuệ x3 | Chung | 60 | Ngọc xanh | Chưa có (#66) |
| 392 | 522 | Bùa Đệ Tử | Chung | 60 | Ngọc xanh | Chưa có (#66) |
| 393 | 219 | Bùa Thu Hút | Chung | 10 | Ngọc xanh | Chưa có (#66) |
| 394 | 218 | Bùa Dẻo Dai | Chung | 4 | Ngọc xanh | Chưa có (#66) |
| 395 | 217 | Bùa Bất Tử | Chung | 28 | Ngọc xanh | Chưa có (#66) |
| 396 | 216 | Bùa Oai Hùng | Chung | 28 | Ngọc xanh | Chưa có (#66) |
| 397 | 215 | Bùa Da Trâu | Chung | 10 | Ngọc xanh | Chưa có (#66) |
| 398 | 214 | Bùa Mạnh Mẽ | Chung | 20 | Ngọc xanh | Chưa có (#66) |
| 399 | 213 | Bùa Trí Tuệ | Chung | 20 | Ngọc xanh | Chưa có (#66) |

#### Shop id=8 — `BUA_1M` (NPC 21 – Bà Hạt Mít, type_shop=0)


**Tab 16 – "Bùa 1 tháng"**: 10 item đang bán

| item_shop.id | temp_id | Tên | HT | Giá | Loại tiền | Option |
|---|---|---|---|---|---|---|
| 400 | 672 | Bùa Trí Tuệ x4 | Chung | 4.500 | Ngọc xanh | Chưa có (#66) |
| 401 | 671 | Bùa Trí Tuệ x3 | Chung | 1.500 | Ngọc xanh | Chưa có (#66) |
| 402 | 522 | Bùa Đệ Tử | Chung | 1.500 | Ngọc xanh | Chưa có (#66) |
| 403 | 219 | Bùa Thu Hút | Chung | 250 | Ngọc xanh | Chưa có (#66) |
| 404 | 218 | Bùa Dẻo Dai | Chung | 100 | Ngọc xanh | Chưa có (#66) |
| 405 | 217 | Bùa Bất Tử | Chung | 700 | Ngọc xanh | Chưa có (#66) |
| 406 | 216 | Bùa Oai Hùng | Chung | 700 | Ngọc xanh | Chưa có (#66) |
| 407 | 215 | Bùa Da Trâu | Chung | 250 | Ngọc xanh | Chưa có (#66) |
| 408 | 214 | Bùa Mạnh Mẽ | Chung | 500 | Ngọc xanh | Chưa có (#66) |
| 409 | 213 | Bùa Trí Tuệ | Chung | 500 | Ngọc xanh | Chưa có (#66) |

#### Shop id=9 — `SANTA` (NPC 39 – Santa, type_shop=0)


**Tab 17 – "Cửa Hàng"**: 15 item đang bán

| item_shop.id | temp_id | Tên | HT | Giá | Loại tiền | Option |
|---|---|---|---|---|---|---|
| 483 | 759 | Nâng kỹ năng 4 đệ tử | Chung | 500 | Ngọc xanh | — |
| 484 | 404 | Nâng kỹ năng 3 đệ tử | Chung | 200 | Ngọc xanh | — |
| 485 | 403 | Nâng kỹ năng 2 đệ tử | Chung | 150 | Ngọc xanh | — |
| 486 | 402 | Nâng kỹ năng 1 đệ tử | Chung | 100 | Ngọc xanh | — |
| 487 | 400 | Đặt tên đệ tử | Chung | 20 | Ngọc xanh | — |
| 488 | 401 | Đổi đệ tử | Chung | 100 | Ngọc xanh | — |
| 602 | 1532 | Rađa kho báu | Chung | 2 | Ngọc xanh | Không thể giao dịch (#30) |
| 603 | 962 | Cap thời trang 5 ngày | Chung | 150.000.000 | Vàng | Không thể giao dịch (#30) |
| 604 | 963 | Cap thời trang 7 ngày | Chung | 99 | Ngọc xanh | Không thể giao dịch (#30) |
| 605 | 1281 | Trang sách cũ | Chung | 10.000.000 | Vàng | Không thể giao dịch (#30) |
| 607 | 1282 | Bìa sách | Chung | 10.000.000 | Vàng | Không thể giao dịch (#30) |
| 608 | 1285 | Kìm bấm giấy | Chung | 5.000.000 | Vàng | Không thể giao dịch (#30) |
| 609 | 1284 | Bùa giám định | Chung | 5.000.000 | Vàng | Không thể giao dịch (#30) |
| 910 | 935 | Đá xanh lam | Chung | 150 | Ngọc xanh | — |
| 911 | 933 | Mảnh vỡ bông tai | Chung | 50 | Ngọc xanh | — |

**Tab 18 – "Cải Trang"**: 74 item đang bán

| item_shop.id | temp_id | Tên | HT | Giá | Loại tiền | Option |
|---|---|---|---|---|---|---|
| 417 | 284 | Cải trang | Chung | 500 | Ngọc xanh | HP+10% (#77) |
| 418 | 285 | Cải trang | TĐ | 1.000 | Ngọc xanh | HP+10% (#77) |
| 419 | 286 | Cải trang | NM | 200 | Ngọc xanh | Sức đánh+10% (#50) |
| 420 | 287 | Cải trang | NM | 500 | Ngọc xanh | Sức đánh+10% (#50) |
| 421 | 290 | Cải trang | Chung | 300 | Ngọc xanh | Chí mạng+2% (#14); Sức đánh+12% (#50); Giảm 8% sát thương (#94) |
| 422 | 291 | Cải trang | TĐ | 500 | Ngọc xanh | HP+10% (#77) |
| 423 | 292 | Cải trang | Chung | 300 | Ngọc xanh | Chí mạng+2% (#14); Sức đánh+12% (#50); Giảm 8% sát thương (#94) |
| 424 | 405 | Cải trang | Chung | 1.000 | Ngọc xanh | Sức đánh+10% (#50) |
| 425 | 406 | Cải trang | Chung | 1.100 | Ngọc xanh | Sức đánh+11% (#50) |
| 426 | 407 | Cải trang | Chung | 1.200 | Ngọc xanh | Sức đánh+12% (#50) |
| 427 | 423 | Cải trang | Chung | 50 | Ngọc xanh | Sức đánh+13% (#50); Hạn sử dụng 7 ngày (#93) |
| 428 | 424 | Cải trang | Chung | 50 | Ngọc xanh | Sức đánh+13% (#50); Hạn sử dụng 7 ngày (#93) |
| 429 | 425 | Cải trang | Chung | 50 | Ngọc xanh | Sức đánh+13% (#50); Hạn sử dụng 7 ngày (#93) |
| 430 | 426 | Cải trang | Chung | 50 | Ngọc xanh | Sức đánh+13% (#50); Hạn sử dụng 7 ngày (#93) |
| 431 | 427 | Cải trang | Chung | 50 | Ngọc xanh | Sức đánh+14% (#50); Hạn sử dụng 7 ngày (#93) |
| 432 | 428 | Cải trang | Chung | 50 | Ngọc xanh | Sức đánh+15% (#50); Hạn sử dụng 7 ngày (#93) |
| 433 | 429 | Cải trang | Chung | 50 | Ngọc xanh | Sức đánh+12% (#50); Giảm 8% sát thương (#94); Hạn sử dụng 30 ngày (#93) |
| 434 | 430 | Cải trang | Chung | 50 | Ngọc xanh | Sức đánh+12% (#50); Giảm 8% sát thương (#94); Hạn sử dụng 30 ngày (#93) |
| 435 | 431 | Cải trang | Chung | 50 | Ngọc xanh | Sức đánh+13% (#50); Giảm 8% sát thương (#94); Hạn sử dụng 30 ngày (#93) |
| 436 | 432 | Cải trang | Chung | 50 | Ngọc xanh | Sức đánh+14% (#50); Giảm 8% sát thương (#94); Hạn sử dụng 30 ngày (#93) |
| 437 | 433 | Cải trang | Chung | 50 | Ngọc xanh | Sức đánh+15% (#50); Giảm 8% sát thương (#94); Hạn sử dụng 30 ngày (#93) |
| 438 | 448 | Cải trang | Chung | 2.000 | Ngọc xanh | Biến 50% tấn công quái thành HP (#104) |
| 439 | 449 | Cải trang | Chung | 2.000 | Ngọc xanh | Vô hình khi không đánh quái và boss (#105) |
| 440 | 450 | Cải trang | Chung | 2.000 | Ngọc xanh | Không ảnh hưởng bởi cái lạnh (#106) |
| 443 | 455 | Cải trang | Chung | 2.000 | Ngọc xanh | Hôi, giảm 10% HP (#109); Dò pha lê (#110) |
| 444 | 458 | Cải trang | Chung | 2.000 | Ngọc xanh | Phân tâm (#111); Dò pha lê (#110) |
| 445 | 461 | Cải trang | Chung | 2.000 | Ngọc xanh | +100% TĐ chạy (#114); +10% tiềm năng, sức mạnh (#101); Dò pha lê (#110) |
| 446 | 524 | Cải trang | Chung | 1.200 | Ngọc xanh | Vô hiệu và biến 80% sát thương chưởng thành KI (#3); Hồi phục 1% KI khi bị đánh (#4); +10% sức đánh chí mạng (#5) |
| 447 | 525 | Cải trang | Chung | 1.500 | Ngọc xanh | Vô hiệu và biến 100% sát thương chưởng thành KI (#3); Hồi phục 2% KI khi bị đánh (#4); +15% sức đánh chí mạng (#5) |
| 448 | 526 | Cải trang | Chung | 1.700 | Ngọc xanh | Hút 1% HP, KI xung quanh mỗi 5 giây (#8); Sức đánh+14% (#50) |
| 449 | 527 | Cải trang | Chung | 1.850 | Ngọc xanh | Hút 2% HP, KI xung quanh mỗi 5 giây (#8); Sức đánh+16% (#50); KI +15% (#103) |
| 450 | 528 | Cải trang | Chung | 2.000 | Ngọc xanh | Hút 3% HP, KI xung quanh mỗi 5 giây (#8); Sức đánh+18% (#50); HP+15% (#77); KI +15% (#103) |
| 451 | 549 | Cải trang | Chung | 3.000 | Ngọc xanh | Hút 4% HP, KI xung quanh mỗi 5 giây (#8); Sức đánh+20% (#50); HP+17% (#77); KI +17% (#103) |
| 452 | 550 | Cải trang | Chung | 120 | Ngọc xanh | Tấn công+24% khi đánh quái (#19) |
| 453 | 551 | Cải trang | Chung | 110 | Ngọc xanh | Tấn công+22% khi đánh quái (#19) |
| 454 | 552 | Cải trang | Chung | 100 | Ngọc xanh | Tấn công+20% khi đánh quái (#19) |
| 455 | 575 | Cải trang | Chung | 2.000 | Ngọc xanh | Sức đánh+19% (#50); HP+16% (#77); KI +16% (#103); Làm tăng trọng lực, gây chậm mọi người xung quanh (#24) |
| 456 | 576 | Cải trang | Chung | 2.000 | Ngọc xanh | Sức đánh+20% (#50); HP+17% (#77); KI +17% (#103); Tàng hình mỗi 5 giây (#25) |
| 457 | 577 | Cải trang | Chung | 2.000 | Ngọc xanh | Sức đánh+21% (#50); HP+18% (#77); KI +18% (#103); Hóa đá mọi người xung quanh mỗi 30 giây (#26) |
| 458 | 578 | Cải trang | Chung | 2.000 | Ngọc xanh | Sức đánh+22% (#50); HP+19% (#77); KI +19% (#103); Biến Sôcôla mọi người xung quanh mỗi 30 giây (#29) |
| 459 | 583 | Cải trang Póc | Chung | 1.600 | Ngọc xanh | Sức đánh+8% (#50); HP+8% (#77); Giảm 8% sát thương (#94); (Chỉ số tăng khi ở gần Android Sát Thủ khác loại) (#150) |
| 460 | 601 | Cải trang Hợp Thể | TĐ | 3.000 | Ngọc xanh | Sức đánh+21% (#50); HP+18% (#77); KI +18% (#103); Chỉ có tác dụng khi hợp thể (#38) |
| 461 | 602 | Cải trang Hợp Thể | NM | 3.000 | Ngọc xanh | Sức đánh+21% (#50); HP+18% (#77); KI +18% (#103); Chỉ có tác dụng khi hợp thể (#38) |
| 462 | 604 | Cải trang VIP | TĐ | 10.000 | Ngọc xanh | Sức đánh+23% (#50); HP+20% (#77); KI +20% (#103) |
| 463 | 605 | Cải trang VIP | NM | 10.000 | Ngọc xanh | Sức đánh+23% (#50); HP+20% (#77); KI +20% (#103) |
| 464 | 607 | Cải trang Chan Xư | TĐ | 1.000 | Ngọc xanh | Tấn công+21% khi đánh quái (#19); HP+18% (#77); KI +18% (#103) |
| 465 | 608 | Cải trang Lão Cận | NM | 1.000 | Ngọc xanh | Tấn công+21% khi đánh quái (#19); HP+18% (#77); KI +18% (#103) |
| 466 | 612 | Cải trang Arale | Chung | 2.000.000.000 | Vàng | Sức đánh+10% (#50); +33% TĐ chạy (#114); $(Ở gần 1 CT Dr Slum khác loại +20% sức đánh +66% tốc độ chạy) (#145); $(Ở gần 2 CT Dr Slum khác loại +30% sức đánh +100% tốc độ chạy) (#146) |
| 467 | 613 | Cải trang Gatchan | Chung | 2.000.000.000 | Vàng | Sức đánh+10% (#50); +33% TĐ chạy (#114); $(Ở gần 1 CT Dr Slum khác loại +20% sức đánh +66% tốc độ chạy) (#145); $(Ở gần 2 CT Dr Slum khác loại +30% sức đánh +100% tốc độ chạy) (#146) |
| 468 | 614 | Cải trang Obotchaman | Chung | 2.000.000.000 | Vàng | Sức đánh+10% (#50); +33% TĐ chạy (#114); $(Ở gần 1 CT Dr Slum khác loại +20% sức đánh +66% tốc độ chạy) (#145); $(Ở gần 2 CT Dr Slum khác loại +30% sức đánh +100% tốc độ chạy) (#146) |
| 469 | 615 | Cải trang | Chung | 300 | Ngọc xanh | Chí mạng+2% (#14); +50% TĐ chạy (#114); Sức đánh+10% (#50); Giảm 5% sát thương (#94) |
| 470 | 616 | Cải trang | Chung | 300 | Ngọc xanh | Chí mạng+2% (#14); Sức đánh+12% (#50); Giảm 8% sát thương (#94) |
| 471 | 617 | Cải trang | Chung | 400 | Ngọc xanh | Chí mạng+3% (#14); Sức đánh+13% (#50); Giảm 9% sát thương (#94) |
| 472 | 630 | Cải trang Frost 1 | Chung | 1.100 | Ngọc xanh | Sức đánh+10% (#50); Không ảnh hưởng bởi cái lạnh (#106) |
| 473 | 631 | Cải trang Frost 2 | Chung | 1.200 | Ngọc xanh | Sức đánh+11% (#50); Không ảnh hưởng bởi cái lạnh (#106) |
| 474 | 632 | Cải trang Frost 3 | Chung | 1.300 | Ngọc xanh | Sức đánh+12% (#50); Không ảnh hưởng bởi cái lạnh (#106) |
| 475 | 633 | Cải trang Píc | Chung | 1.700 | Ngọc xanh | Sức đánh+8% (#50); HP+8% (#77); Giảm 8% sát thương (#94); (Chỉ số tăng khi ở gần Android Sát Thủ khác loại) (#150) |
| 476 | 634 | Cải trang King kong | Chung | 1.800 | Ngọc xanh | Sức đánh+8% (#50); HP+8% (#77); Giảm 8% sát thương (#94); (Chỉ số tăng khi ở gần Android Sát Thủ khác loại) (#150) |
| 477 | 640 | Cải trang Hợp Thể | TĐ | 300 | Ngọc xanh | Sức đánh+21% (#50); HP+18% (#77); KI +18% (#103); Chỉ có tác dụng khi hợp thể (#38); Hạn sử dụng 45 ngày (#93) |
| 478 | 641 | Cải trang Hợp Thể | NM | 300 | Ngọc xanh | Sức đánh+21% (#50); HP+18% (#77); KI +18% (#103); Chỉ có tác dụng khi hợp thể (#38); Hạn sử dụng 45 ngày (#93) |
| 479 | 647 | Cải trang Saibamen | Chung | 3.500 | Ngọc xanh | Sức đánh+20% (#50); HP+20% (#77); KI +20% (#103); 50% tỉ lệ phát nổ sau khi chết (#153) |
| 480 | 724 | Cải trang | Chung | 5.000 | Ngọc xanh | Sức đánh+20% (#50); HP+17% (#77); KI +17% (#103); Cute hồi 2% KI/s bản thân và xung quanh (#162) |
| 500 | 288 | Cải trang | XD | 200 | Ngọc xanh | Chí mạng+2% (#14) |
| 501 | 289 | Cải trang | XD | 300 | Ngọc xanh | Chí mạng+2% (#14) |
| 502 | 603 | Cải trang Hợp Thể | XD | 3.000 | Ngọc xanh | Sức đánh+21% (#50); HP+18% (#77); KI +18% (#103); Chỉ có tác dụng khi hợp thể (#38) |
| 503 | 606 | Cải trang VIP | XD | 10.000 | Ngọc xanh | Sức đánh+23% (#50); HP+20% (#77); KI +20% (#103) |
| 504 | 609 | Cải trang Xayda | XD | 1.000 | Ngọc xanh | Tấn công+21% khi đánh quái (#19); HP+18% (#77); KI +18% (#103) |
| 505 | 639 | Cải trang Hợp Thể | XD | 300 | Ngọc xanh | Sức đánh+21% (#50); HP+18% (#77); KI +18% (#103); Chỉ có tác dụng khi hợp thể (#38); Hạn sử dụng 45 ngày (#93) |
| 912 | 282 | Cải trang | Chung | 500 | Ngọc xanh | Sức đánh+10% (#50) |
| 913 | 1255 | Cải trang Mabư Còm | Chung | 5.000 | Ngọc xanh | Sức đánh+24% (#50); HP+22% (#77); KI +22% (#103); Phản 5% sát thương (#97); +30% TN, SM cho đệ tử khi sư phụ mặc (#160) |
| 914 | 879 | Cải trang Thống Chế Kilo | Chung | 299 | Ngọc xanh | Sức đánh+22% (#50); KI +18% (#103); +3 KI/30s (#28); +2% sức đánh, tối đa 10% khi ở gần Cải Trang tộc Demons Frost (#179); Không ảnh hưởng bởi cái lạnh (#106); Hạn sử dụng 30 ngày (#93) |
| 915 | 878 | Cải trang Cooler vàng | Chung | 299 | Ngọc xanh | Sức đánh+23% (#50); KI +19% (#103); +3 KI/30s (#28); +2% sức đánh, tối đa 10% khi ở gần Cải Trang tộc Demons Frost (#179); Không ảnh hưởng bởi cái lạnh (#106); Hạn sử dụng 30 ngày (#93) |
| 916 | 1566 | CT Lích Tên béo | Chung | 99 | Ngọc xanh | 4 Dòng chỉ số ẩn (#210); Chỉ có tác dụng khi hợp thể (#38) |
| 917 | 885 | CT Lích Tên béo | Chung | 99.000.000 | Vàng | 4 Dòng chỉ số ẩn (#210); Chỉ có tác dụng khi hợp thể (#38) |

**Tab 34 – "Hỗ Trợ"**: 13 item đang bán

| item_shop.id | temp_id | Tên | HT | Giá | Loại tiền | Option |
|---|---|---|---|---|---|---|
| 492 | 521 | Tự động luyện tập | Chung | 2 | Ngọc xanh | Thời gian sử dụng 20 phút (#1) |
| 493 | 529 | Giáp tập luyện cấp 1 | Chung | 30 | Ngọc xanh | Hiệu lực trong 0 phút (#9) |
| 494 | 530 | Giáp tập luyện cấp 2 | Chung | 300 | Ngọc xanh | Hiệu lực trong 0 phút (#9) |
| 495 | 531 | Giáp tập luyện cấp 3 | Chung | 3.000 | Ngọc xanh | Hiệu lực trong 0 phút (#9) |
| 496 | 534 | Giáp tập luyện cấp 1 | Chung | 10.000.000 | Vàng | Hiệu lực trong 0 phút (#9) |
| 497 | 535 | Giáp tập luyện cấp 2 | Chung | 100.000.000 | Vàng | Hiệu lực trong 0 phút (#9) |
| 498 | 536 | Giáp tập luyện cấp 3 | Chung | 1.000.000.000 | Vàng | Hiệu lực trong 0 phút (#9) |
| 906 | 1628 | Bùa x2 tn,sm đệ tử | Chung | 5 | Ngọc xanh | — |
| 907 | 1653 | Loa to liên vũ trụ | Chung | 15 | Ngọc xanh | — |
| 908 | 1652 | Loa to thế giới | Chung | 5 | Ngọc xanh | — |
| 909 | 1716 | Giáp tập luyện cấp 4 | Chung | 9.999 | Ngọc xanh | HP+10% (#77); Giảm 3% sát thương (#94); Hiệu lực trong 0 phút (#9) |
| 954 | 1787 | Vé riêng tư | Chung | 1.999 | Ngọc xanh | Không thể giao dịch (#30); Hạn sử dụng 3 ngày (#93) |
| 989 | 1635 | Cỏ bốn lá | Chung | 5 | Ngọc xanh | Không thể giao dịch (#30) |

#### Shop id=10 — `BUNMA_FUTURE` (NPC 37 – Bunma, type_shop=0)


**Tab 20 – "Cửa Hàng"**: 2 item đang bán

| item_shop.id | temp_id | Tên | HT | Giá | Loại tiền | Option |
|---|---|---|---|---|---|---|
| 515 | 379 | Máy dò Capsule kì bí | Chung | 5.000.000 | Vàng | — |
| 567 | 597 | Gói 30 đậu thần cấp 9 | Chung | 100 | Ngọc xanh | — |

#### Shop id=11 — `BILL` (NPC 55 – Bill, type_shop=0)


**Tab 21 – "Trái Đất"**: 5 item đang bán

| item_shop.id | temp_id | Tên | HT | Giá | Loại tiền | Option |
|---|---|---|---|---|---|---|
| 774 | 657 | Găng Hủy Diệt | TĐ | 2.000.000.000 | Vàng | Tấn công+8800 (#0) |
| 775 | 656 | Nhẫn Hủy Diệt | Chung | 2.000.000.000 | Vàng | Chí mạng+16% (#14) |
| 776 | 651 | Quần Hủy Diệt | TĐ | 1.000.000.000 | Vàng | HP+104K (#22) |
| 777 | 650 | Áo Hủy Diệt | TĐ | 800.000.000 | Vàng | Giáp+1600 (#47) |
| 778 | 658 | Giầy Hủy Diệt | TĐ | 720.000.000 | Vàng | KI+96K (#23) |

**Tab 22 – "Namếc"**: 5 item đang bán

| item_shop.id | temp_id | Tên | HT | Giá | Loại tiền | Option |
|---|---|---|---|---|---|---|
| 779 | 659 | Găng Hủy Diệt | NM | 2.000.000.000 | Vàng | Tấn công+8600 (#0) |
| 780 | 656 | Nhẫn Hủy Diệt | Chung | 2.000.000.000 | Vàng | Chí mạng+16% (#14) |
| 781 | 653 | Quần Hủy Diệt | NM | 1.000.000.000 | Vàng | HP+100K (#22) |
| 782 | 652 | Áo Hủy Diệt | NM | 800.000.000 | Vàng | Giáp+1700 (#47) |
| 783 | 660 | Giầy Hủy Diệt | NM | 720.000.000 | Vàng | KI+100K (#23) |

**Tab 23 – "Xay da"**: 5 item đang bán

| item_shop.id | temp_id | Tên | HT | Giá | Loại tiền | Option |
|---|---|---|---|---|---|---|
| 784 | 661 | Găng Hủy Diệt | XD | 2.000.000.000 | Vàng | Tấn công+9000 (#0) |
| 785 | 656 | Nhẫn Hủy Diệt | Chung | 2.000.000.000 | Vàng | Chí mạng+16% (#14) |
| 786 | 655 | Quần Hủy Diệt | XD | 1.000.000.000 | Vàng | HP+96K (#22) |
| 787 | 654 | Áo Hủy Diệt | XD | 800.000.000 | Vàng | Giáp+1800 (#47) |
| 788 | 662 | Giầy Hủy Diệt | XD | 720.000.000 | Vàng | KI+92K (#23) |

#### Shop id=12 — `SANTA_RUONG` (NPC 39 – Santa, type_shop=0)


**Tab 29 – "Cửa   Hàng"**: 0 item đang bán


#### Shop id=13 — `SANTA_HSD` (NPC 39 – Santa, type_shop=0)


**Tab 35 – "Ngọc"**: 0 item đang bán


**Tab 36 – "Vàng"**: 0 item đang bán


#### Shop id=14 — `OSIN` (NPC 44 – Ôsin, type_shop=0)


**Tab 39 – "Cửa hàng"**: 0 item đang bán


#### Shop id=15 — `BUNMA_LINHTHU` (NPC 37 – Bunma, type_shop=0)


**Tab 31 – "Cửa hàng"**: 0 item đang bán


#### Shop id=16 — `KARIN` (NPC 18 – Thần mèo Karin, type_shop=3)


**Tab 32 – "Cửa hàng"**: 0 item đang bán


#### Shop id=17 — `BULMA_TL` (NPC 37 – Bunma, type_shop=0)


**Tab 33 – "Của Hàng Tương Lai"**: 1 item đang bán

| item_shop.id | temp_id | Tên | HT | Giá | Loại tiền | Option |
|---|---|---|---|---|---|---|
| 650 | 379 | Máy dò Capsule kì bí | Chung | 100 | Ngọc xanh | — |

#### Shop id=19 — `CHUBEDAN` (NPC 103 – Chú Bé Đần, type_shop=3)

_Không có tab nào trong `tab_shop`._


#### Shop id=20 — `THIEN_SU` (NPC 56 – Whis, type_shop=3)


**Tab 37 – "Cửa hàng"**: 16 item đang bán

| item_shop.id | temp_id | Tên | HT | Giá | Trả bằng (icon_spec → item) | Option |
|---|---|---|---|---|---|---|
| 820 | 1074 | Đá nâng cấp cấp 1 | Chung | 1 | icon 4028 → 457 Thỏi vàng | Không thể giao dịch (#30) |
| 821 | 1075 | Đá nâng cấp cấp 2 | Chung | 2 | icon 4028 → 457 Thỏi vàng | Không thể giao dịch (#30) |
| 822 | 1076 | Đá nâng cấp cấp 3 | Chung | 3 | icon 4028 → 457 Thỏi vàng | Không thể giao dịch (#30) |
| 823 | 1077 | Đá nâng cấp cấp 4 | Chung | 4 | icon 4028 → 457 Thỏi vàng | Không thể giao dịch (#30) |
| 824 | 1078 | Đá nâng cấp cấp 5 | Chung | 5 | icon 4028 → 457 Thỏi vàng | Không thể giao dịch (#30) |
| 825 | 1079 | Đá may mắn cấp 1 | Chung | 1 | icon 4028 → 457 Thỏi vàng | Không thể giao dịch (#30) |
| 826 | 1080 | Đá may mắn cấp 2 | Chung | 2 | icon 4028 → 457 Thỏi vàng | Không thể giao dịch (#30) |
| 827 | 1081 | Đá may mắn cấp 3 | Chung | 3 | icon 4028 → 457 Thỏi vàng | Không thể giao dịch (#30) |
| 828 | 1082 | Đá may mắn cấp 4 | Chung | 4 | icon 4028 → 457 Thỏi vàng | Không thể giao dịch (#30) |
| 829 | 1083 | Đá may mắn cấp 5 | Chung | 5 | icon 4028 → 457 Thỏi vàng | Không thể giao dịch (#30) |
| 830 | 1071 | Công thức | TĐ | 10 | icon 4028 → 457 Thỏi vàng | Không thể giao dịch (#30) |
| 831 | 1072 | Công thức | NM | 10 | icon 4028 → 457 Thỏi vàng | Không thể giao dịch (#30) |
| 832 | 1073 | Công thức | XD | 10 | icon 4028 → 457 Thỏi vàng | Không thể giao dịch (#30) |
| 833 | 1084 | Công thức VIP | TĐ | 20 | icon 4028 → 457 Thỏi vàng | Không thể giao dịch (#30) |
| 834 | 1085 | Công thức VIP | NM | 20 | icon 4028 → 457 Thỏi vàng | Không thể giao dịch (#30) |
| 835 | 1086 | Công thức VIP | XD | 20 | icon 4028 → 457 Thỏi vàng | Không thể giao dịch (#30) |

#### Shop id=21 — `BULMA_EVENT` (NPC 106 – Bulma Tết Nguyên Đán, type_shop=3)


**Tab 30 – "Cửa hàng EVENT"**: 9 item đang bán

| item_shop.id | temp_id | Tên | HT | Giá | Trả bằng (icon_spec → item) | Option |
|---|---|---|---|---|---|---|
| 660 | 1171 | Túi 7 chú lùn | Chung | 349 | icon 0 → 1826  | — |
| 661 | 649 | Tất,vớ giáng sinh | Chung | 20 | icon 0 → 1826  | — |
| 662 | 386 | Nón Noel Xám | TĐ | 500 | icon 0 → 1826  | HP+10% (#77); Không ảnh hưởng bởi cái lạnh (#106); Hạn sử dụng 30 ngày (#93) |
| 663 | 389 | Nón Noel Xám | NM | 500 | icon 0 → 1826  | HP+10% (#77); Không ảnh hưởng bởi cái lạnh (#106); Hạn sử dụng 30 ngày (#93) |
| 664 | 392 | Nón Noel Xám | XD | 500 | icon 0 → 1826  | HP+10% (#77); Không ảnh hưởng bởi cái lạnh (#106); Hạn sử dụng 30 ngày (#93) |
| 665 | 388 | Nón Noel Xanh | TĐ | 100 | icon 0 → 1826  | HP+30% (#77); HP+50%/30s (#80); Không ảnh hưởng bởi cái lạnh (#106); Hạn sử dụng 30 ngày (#93) |
| 666 | 391 | Nón Noel Xanh | NM | 100 | icon 0 → 1826  | HP+30% (#77); HP+50%/30s (#80); Không ảnh hưởng bởi cái lạnh (#106); Hạn sử dụng 30 ngày (#93) |
| 667 | 394 | Nón Noel Xanh | XD | 100 | icon 0 → 1826  | HP+30% (#77); HP+50%/30s (#80); Không ảnh hưởng bởi cái lạnh (#106); Hạn sử dụng 30 ngày (#93) |
| 668 | 1168 | Kẹo đường | Chung | 50 | icon 0 → 1826  | — |

**Tab 38 – "Điểm Sự Kiện"**: 0 item đang bán


**Tab 40 – "Vật Phẩm Đeo Lưng"**: 0 item đang bán


#### Shop id=22 — `SANTA_MO_RONG_HANH_TRANG` (NPC 39 – Santa, type_shop=0)


**Tab 46 – "Cửa   Hàng"**: 2 item đang bán

| item_shop.id | temp_id | Tên | HT | Giá | Loại tiền | Option |
|---|---|---|---|---|---|---|
| 490 | 517 | Mở rộng hành trang | Chung | 100 | Ngọc xanh | — |
| 491 | 518 | Mở rộng rương đồ | Chung | 50.000.000 | Vàng | — |

#### Shop id=23 — `SANTA_HAN_SU_DUNG` (NPC 39 – Santa, type_shop=0)


**Tab 47 – "Ngọc"**: 6 item đang bán

| item_shop.id | temp_id | Tên | HT | Giá | Loại tiền | Option |
|---|---|---|---|---|---|---|
| 641 | 1103 | Marron | Chung | 299 | Ngọc xanh | Sức đánh+20% (#50); HP+20% (#77); KI +20% (#103); +20% tốc độ chạy (#148); Hút 4% HP, KI xung quanh mỗi 5 giây (#8); Không thể bán lại (#154); Hạn sử dụng 30 ngày (#93) |
| 642 | 1043 | Thỏ đen Android 18 | Chung | 299 | Ngọc xanh | Sức đánh+20% (#50); HP+20% (#77); KI +20% (#103); +20% tốc độ chạy (#148); Xinh +2% sức đánh, tối đa 18% khi ở gần Cải trang Thỏ khác (#196); Không thể bán lại (#154); Hạn sử dụng 20 ngày (#93) |
| 643 | 1042 | Thỏ đỏ Chi Chi | Chung | 299 | Ngọc xanh | Sức đánh+20% (#50); HP+20% (#77); KI +22% (#103); +20% tốc độ chạy (#148); Xinh +2% sức đánh, tối đa 18% khi ở gần Cải trang Thỏ khác (#196); Không thể bán lại (#154); Hạn sử dụng 20 ngày (#93) |
| 644 | 1041 | Thỏ hồng Bun ma | Chung | 299 | Ngọc xanh | Sức đánh+20% (#50); HP+20% (#77); KI +20% (#103); +20% tốc độ chạy (#148); Xinh +2% sức đánh, tối đa 18% khi ở gần Cải trang Thỏ khác (#196); Không thể bán lại (#154); Hạn sử dụng 20 ngày (#93) |
| 645 | 974 | Số 1 Mabư | Chung | 299 | Ngọc xanh | Sức đánh+20% (#50); KI +20% (#103); HP+20% (#77); +20% tốc độ chạy (#148); +2% sức đánh, tối đa 11% khi ở gần Cải trang cầu thủ khác (#193); Không thể bán lại (#154); Hạn sử dụng 20 ngày (#93) |
| 646 | 937 | Cải trang Mabư Noel | Chung | 299 | Ngọc xanh | Sức đánh+20% (#50); HP+20% (#77); KI +20% (#103); HP+15%/30s (#80); Không ảnh hưởng bởi cái lạnh (#106); Biến kẹo 30 giây (#186); Không thể bán lại (#154); Hạn sử dụng 30 ngày (#93) |

**Tab 48 – "Vàng"**: 3 item đang bán

| item_shop.id | temp_id | Tên | HT | Giá | Loại tiền | Option |
|---|---|---|---|---|---|---|
| 670 | 1531 | Cánh thiên thần 3 | Chung | 129.000.000 | Vàng | Sức đánh+3% (#50); HP+3% (#77); KI +3% (#103); +30% TN, SM cho đệ tử khi sư phụ mặc (#160); Không thể giao dịch (#30); Hạn sử dụng 7 ngày (#93) |
| 671 | 1553 | CT Goku SSJ4 | Chung | 129.000.000 | Vàng | Sức đánh+20% (#50); HP+17% (#77); KI +17% (#103); +10% sức đánh chí mạng (#5); Không ảnh hưởng bởi cái lạnh (#106); Không thể giao dịch (#30); Hạn sử dụng 15 ngày (#93) |
| 672 | 765 | Cải trang Gohan Bư | Chung | 200.000.000 | Vàng | Sức đánh+22% (#50); HP+19% (#77); KI +19% (#103); HP+5%/30s (#80); KI+5%/30s (#81); $(Tối đa +2% tất cả khi ở gần Mabư mập) (#177); Hạn sử dụng 30 ngày (#93) |

#### Shop id=25 — `QUY_LAO` (NPC 13 – Quy Lão Kame, type_shop=1)


**Tab 41 – "Sách Võ"** (dùng lại item của tab 10): 41 item đang bán

| item_shop.id | temp_id | Tên | HT | cost (DB) | Option |
|---|---|---|---|---|---|
| 218 | 67 | Sách đấm Dragon lv2 | TĐ | 10 | Sức đánh+110% (#50) |
| 219 | 68 | Sách đấm Dragon lv3 | TĐ | 15 | Sức đánh+120% (#50) |
| 220 | 69 | Sách đấm Dragon lv4 | TĐ | 20 | Sức đánh+130% (#50) |
| 221 | 70 | Sách đấm Dragon lv5 | TĐ | 25 | Sức đánh+140% (#50) |
| 222 | 71 | Sách đấm Dragon lv6 | TĐ | 30 | Sức đánh+150% (#50) |
| 223 | 72 | Sách đấm Dragon lv7 | TĐ | 35 | Sức đánh+160% (#50) |
| 224 | 300 | Kaioken lv1 | TĐ | 40 | Sức đánh+160% (#50) |
| 225 | 301 | Kaioken lv2 | TĐ | 50 | Sức đánh+170% (#50) |
| 226 | 302 | Kaioken lv3 | TĐ | 60 | Sức đánh+180% (#50) |
| 227 | 303 | Kaioken lv4 | TĐ | 70 | Sức đánh+190% (#50) |
| 228 | 304 | Kaioken lv5 | TĐ | 80 | Sức đánh+200% (#50) |
| 229 | 305 | Kaioken lv6 | TĐ | 90 | Sức đánh+210% (#50) |
| 230 | 306 | Kaioken lv7 | TĐ | 100 | Sức đánh+220% (#50) |
| 231 | 488 | Sách Dịch Chuyển lv1 | TĐ | 100 | Tới ngay mục tiêu và gây choáng trong 1000 mili giây (#118) |
| 232 | 489 | Sách Dịch Chuyển lv2 | TĐ | 120 | Tới ngay mục tiêu và gây choáng trong 1500 mili giây (#118) |
| 233 | 490 | Sách Dịch Chuyển lv3 | TĐ | 140 | Tới ngay mục tiêu và gây choáng trong 2000 mili giây (#118) |
| 234 | 491 | Sách Dịch Chuyển lv4 | TĐ | 160 | Tới ngay mục tiêu và gây choáng trong 2500 mili giây (#118) |
| 235 | 492 | Sách Dịch Chuyển lv5 | TĐ | 180 | Tới ngay mục tiêu và gây choáng trong 3000 mili giây (#118) |
| 236 | 493 | Sách Dịch Chuyển lv6 | TĐ | 200 | Tới ngay mục tiêu và gây choáng trong 3500 mili giây (#118) |
| 237 | 494 | Sách Dịch Chuyển lv7 | TĐ | 220 | Tới ngay mục tiêu và gây choáng trong 4000 mili giây (#118) |
| 273 | 79 | Sách đấm Demon lv1 | NM | 5 | Sức đánh+95% (#50) |
| 274 | 80 | Sách đấm Demon lv2 | NM | 10 | Sức đánh+105% (#50) |
| 275 | 81 | Sách đấm Demon lv3 | NM | 15 | Sức đánh+115% (#50) |
| 276 | 82 | Sách đấm Demon lv4 | NM | 20 | Sức đánh+125% (#50) |
| 277 | 83 | Sách đấm Demon lv5 | NM | 25 | Sức đánh+135% (#50) |
| 278 | 84 | Sách đấm Demon lv6 | NM | 30 | Sức đánh+145% (#50) |
| 279 | 86 | Sách đấm Demon lv7 | NM | 35 | Sức đánh+155% (#50) |
| 280 | 481 | Sách Liên hoàn lv1 | NM | 100 | Sức đánh+160% (#50) |
| 281 | 482 | Sách Liên hoàn lv2 | NM | 120 | Sức đánh+165% (#50) |
| 282 | 483 | Sách Liên hoàn lv3 | NM | 140 | Sức đánh+170% (#50) |
| 283 | 484 | Sách Liên hoàn lv4 | NM | 160 | Sức đánh+175% (#50) |
| 284 | 485 | Sách Liên hoàn lv5 | NM | 180 | Sức đánh+180% (#50) |
| 285 | 486 | Sách Liên hoàn lv6 | NM | 200 | Sức đánh+185% (#50) |
| 286 | 487 | Sách Liên hoàn lv7 | NM | 220 | Sức đánh+190% (#50) |
| 322 | 87 | Sách đấm Galick lv1 | XD | 5 | Sức đánh+100% (#50) |
| 323 | 88 | Sách đấm Galick lv2 | XD | 10 | Sức đánh+110% (#50) |
| 324 | 89 | Sách đấm Galick lv3 | XD | 15 | Sức đánh+120% (#50) |
| 325 | 90 | Sách đấm Galick lv4 | XD | 20 | Sức đánh+130% (#50) |
| 326 | 91 | Sách đấm Galick lv5 | XD | 25 | Sức đánh+140% (#50) |
| 327 | 92 | Sách đấm Galick lv6 | XD | 30 | Sức đánh+150% (#50) |
| 328 | 93 | Sách đấm Galick lv7 | XD | 35 | Sức đánh+160% (#50) |

**Tab 42 – "Sách Chưởng"** (dùng lại item của tab 11): 56 item đang bán

| item_shop.id | temp_id | Tên | HT | cost (DB) | Option |
|---|---|---|---|---|---|
| 238 | 94 | Sách Kamejoko lv1 | TĐ | 10 | Sức đánh+150% (#50) |
| 239 | 95 | Sách Kamejoko lv2 | TĐ | 20 | Sức đánh+200% (#50) |
| 240 | 96 | Sách Kamejoko lv3 | TĐ | 30 | Sức đánh+250% (#50) |
| 241 | 97 | Sách Kamejoko lv4 | TĐ | 40 | Sức đánh+300% (#50) |
| 242 | 98 | Sách Kamejoko lv5 | TĐ | 50 | Sức đánh+350% (#50) |
| 243 | 99 | Sách Kamejoko lv6 | TĐ | 60 | Sức đánh+400% (#50) |
| 244 | 100 | Sách Kamejoko lv7 | TĐ | 70 | Sức đánh+450% (#50) |
| 245 | 495 | Sách Thôi Miên lv1 | TĐ | 100 | Ru ngủ trong 5 giây (#121); Tỉnh giấc bị yếu đi -25% sức đánh trong 10 giây (#124) |
| 246 | 496 | Sách Thôi Miên lv2 | TĐ | 120 | Ru ngủ trong 6 giây (#121); Tỉnh giấc bị yếu đi -30% sức đánh trong 10 giây (#124) |
| 247 | 497 | Sách Thôi Miên lv3 | TĐ | 140 | Ru ngủ trong 7 giây (#121); Tỉnh giấc bị yếu đi -35% sức đánh trong 10 giây (#124) |
| 248 | 498 | Sách Thôi Miên lv4 | TĐ | 160 | Ru ngủ trong 8 giây (#121); Tỉnh giấc bị yếu đi -40% sức đánh trong 10 giây (#124) |
| 249 | 499 | Sách Thôi Miên lv5 | TĐ | 180 | Ru ngủ trong 9 giây (#121); Tỉnh giấc bị yếu đi -45% sức đánh trong 10 giây (#124) |
| 250 | 500 | Sách Thôi Miên lv6 | TĐ | 200 | Ru ngủ trong 10 giây (#121); Tỉnh giấc bị yếu đi -50% sức đánh trong 10 giây (#124) |
| 251 | 501 | Sách Thôi Miên lv7 | TĐ | 220 | Ru ngủ trong 11 giây (#121); Tỉnh giấc bị yếu đi -55% sức đánh trong 10 giây (#124) |
| 287 | 101 | Sách Masenko lv1 | NM | 10 | Sức đánh+100% (#50) |
| 288 | 102 | Sách Masenko lv2 | NM | 20 | Sức đánh+110% (#50) |
| 289 | 103 | Sách Masenko lv3 | NM | 30 | Sức đánh+120% (#50) |
| 290 | 104 | Sách Masenko lv4 | NM | 40 | Sức đánh+130% (#50) |
| 291 | 105 | Sách Masenko lv5 | NM | 50 | Sức đánh+140% (#50) |
| 292 | 106 | Sách Masenko lv6 | NM | 60 | Sức đánh+150% (#50) |
| 293 | 107 | Sách Masenko lv7 | NM | 70 | Sức đánh+160% (#50) |
| 294 | 328 | Makankosappo lv1 | NM | 100 | Sức hủy diệt+70% (#78) |
| 295 | 329 | Makankosappo lv2 | NM | 120 | Sức hủy diệt+80% (#78) |
| 296 | 330 | Makankosappo lv3 | NM | 140 | Sức hủy diệt+90% (#78) |
| 297 | 331 | Makankosappo lv4 | NM | 160 | Sức hủy diệt+100% (#78) |
| 298 | 332 | Makankosappo lv5 | NM | 180 | Sức hủy diệt+110% (#78) |
| 299 | 333 | Makankosappo lv6 | NM | 200 | Sức hủy diệt+120% (#78) |
| 300 | 334 | Makankosappo lv7 | NM | 220 | Sức hủy diệt+130% (#78) |
| 301 | 474 | Sách Biến Sôcôla lv1 | NM | 100 | Biến sôcôla làm yếu đi -15% sức đánh trong 30 giây (#126) |
| 302 | 475 | Sách Biến Sôcôla lv2 | NM | 120 | Biến sôcôla làm yếu đi -17% sức đánh trong 30 giây (#126) |
| 303 | 476 | Sách Biến Sôcôla lv3 | NM | 140 | Biến sôcôla làm yếu đi -19% sức đánh trong 30 giây (#126) |
| 304 | 477 | Sách Biến Sôcôla lv4 | NM | 160 | Biến sôcôla làm yếu đi -21% sức đánh trong 30 giây (#126) |
| 305 | 478 | Sách Biến Sôcôla lv5 | NM | 180 | Biến sôcôla làm yếu đi -23% sức đánh trong 30 giây (#126) |
| 306 | 479 | Sách Biến Sôcôla lv6 | NM | 200 | Biến sôcôla làm yếu đi -25% sức đánh trong 30 giây (#126) |
| 307 | 480 | Sách Biến Sôcôla lv7 | NM | 220 | Biến sôcôla làm yếu đi -27% sức đánh trong 30 giây (#126) |
| 329 | 108 | Sách Antomic lv1 | XD | 10 | Sức đánh+100% (#50) |
| 330 | 109 | Sách Antomic lv2 | XD | 20 | Sức đánh+110% (#50) |
| 331 | 110 | Sách Antomic lv3 | XD | 30 | Sức đánh+120% (#50) |
| 332 | 111 | Sách Antomic lv4 | XD | 40 | Sức đánh+130% (#50) |
| 333 | 112 | Sách Antomic lv5 | XD | 50 | Sức đánh+140% (#50) |
| 334 | 113 | Sách Antomic lv6 | XD | 60 | Sức đánh+150% (#50) |
| 335 | 114 | Sách Antomic lv7 | XD | 70 | Sức đánh+160% (#50) |
| 336 | 321 | Bom hi sinh lv1 | XD | 40 | Sức hủy diệt+100% (#78) |
| 337 | 322 | Bom hi sinh lv2 | XD | 50 | Sức hủy diệt+105% (#78) |
| 338 | 323 | Bom hi sinh lv3 | XD | 60 | Sức hủy diệt+110% (#78) |
| 339 | 324 | Bom hi sinh lv4 | XD | 70 | Sức hủy diệt+115% (#78) |
| 340 | 325 | Bom hi sinh lv5 | XD | 80 | Sức hủy diệt+120% (#78) |
| 341 | 326 | Bom hi sinh lv6 | XD | 90 | Sức hủy diệt+125% (#78) |
| 342 | 327 | Bom hi sinh lv7 | XD | 100 | Sức hủy diệt+130% (#78) |
| 343 | 502 | Sách Trói lv1 | XD | 100 | Trói gô mục tiêu trong 5 giây (#123) |
| 344 | 503 | Sách Trói lv2 | XD | 120 | Trói gô mục tiêu trong 10 giây (#123) |
| 345 | 504 | Sách Trói lv3 | XD | 140 | Trói gô mục tiêu trong 15 giây (#123) |
| 346 | 505 | Sách Trói lv4 | XD | 160 | Trói gô mục tiêu trong 20 giây (#123) |
| 347 | 506 | Sách Trói lv5 | XD | 180 | Trói gô mục tiêu trong 25 giây (#123) |
| 348 | 507 | Sách Trói lv6 | XD | 200 | Trói gô mục tiêu trong 30 giây (#123) |
| 349 | 508 | Sách Trói lv7 | XD | 220 | Trói gô mục tiêu trong 35 giây (#123) |

**Tab 43 – "Sách Đặc biệt"** (dùng lại item của tab 12): 56 item đang bán

| item_shop.id | temp_id | Tên | HT | cost (DB) | Option |
|---|---|---|---|---|---|
| 252 | 115 | Thái Dương Hạ San lv1 | TĐ | 30 | Gây mù xung quanh trong 3 giây (#119) |
| 253 | 116 | Thái Dương Hạ San lv2 | TĐ | 40 | Gây mù xung quanh trong 4 giây (#119) |
| 254 | 117 | Thái Dương Hạ San lv3 | TĐ | 50 | Gây mù xung quanh trong 5 giây (#119) |
| 255 | 118 | Thái Dương Hạ San lv4 | TĐ | 60 | Gây mù xung quanh trong 6 giây (#119) |
| 256 | 119 | Thái Dương Hạ San lv5 | TĐ | 70 | Gây mù xung quanh trong 7 giây (#119) |
| 257 | 120 | Thái Dương Hạ San lv6 | TĐ | 80 | Gây mù xung quanh trong 8 giây (#119) |
| 258 | 121 | Thái Dương Hạ San lv7 | TĐ | 90 | Gây mù xung quanh trong 9 giây (#119) |
| 259 | 307 | Quả cầu Kênh Khi lv1 | TĐ | 100 | Ra đòn sau 360 giây (#120) |
| 260 | 308 | Quả cầu Kênh Khi lv2 | TĐ | 120 | Ra đòn sau 350 giây (#120) |
| 261 | 309 | Quả cầu Kênh Khi lv3 | TĐ | 140 | Ra đòn sau 340 giây (#120) |
| 262 | 310 | Quả cầu Kênh Khi lv4 | TĐ | 160 | Ra đòn sau 330 giây (#120) |
| 263 | 311 | Quả cầu Kênh Khi lv5 | TĐ | 180 | Ra đòn sau 320 giây (#120) |
| 264 | 312 | Quả cầu Kênh Khi lv6 | TĐ | 200 | Ra đòn sau 310 giây (#120) |
| 265 | 313 | Quả cầu Kênh Khi lv7 | TĐ | 220 | Ra đòn sau 300 giây (#120) |
| 266 | 434 | Khiên năng lượng lv1 | Chung | 100 | Bảo vệ trong 15 giây (#122) |
| 267 | 435 | Khiên năng lượng lv2 | Chung | 200 | Bảo vệ trong 20 giây (#122) |
| 268 | 436 | Khiên năng lượng lv3 | Chung | 300 | Bảo vệ trong 25 giây (#122) |
| 269 | 437 | Khiên năng lượng lv4 | Chung | 400 | Bảo vệ trong 30 giây (#122) |
| 270 | 438 | Khiên năng lượng lv5 | Chung | 500 | Bảo vệ trong 35 giây (#122) |
| 271 | 439 | Khiên năng lượng lv6 | Chung | 600 | Bảo vệ trong 40 giây (#122) |
| 272 | 440 | Khiên năng lượng lv7 | Chung | 700 | Bảo vệ trong 45 giây (#122) |
| 308 | 122 | Sách học Trị thương lv1 | NM | 30 | Phục hồi 50% HP và KI cho đồng đội (#173) |
| 309 | 123 | Sách học Trị thương lv2 | NM | 40 | Phục hồi 55% HP và KI cho đồng đội (#173) |
| 310 | 124 | Sách học Trị thương lv3 | NM | 50 | Phục hồi 60% HP và KI cho đồng đội (#173) |
| 311 | 125 | Sách học Trị thương lv4 | NM | 60 | Phục hồi 65% HP và KI cho đồng đội (#173) |
| 312 | 126 | Sách học Trị thương lv5 | NM | 70 | Phục hồi 70% HP và KI cho đồng đội (#173) |
| 313 | 127 | Sách học Trị thương lv6 | NM | 80 | Phục hồi 75% HP và KI cho đồng đội (#173) |
| 314 | 128 | Sách học Trị thương lv7 | NM | 90 | Phục hồi 80% HP và KI cho đồng đội (#173) |
| 315 | 335 | Đẻ trứng lv1 | NM | 60 | Đệ tử 50% sức đánh (#79) |
| 316 | 336 | Đẻ trứng lv2 | NM | 70 | Đệ tử 55% sức đánh (#79) |
| 317 | 337 | Đẻ trứng lv3 | NM | 80 | Đệ tử 60% sức đánh (#79) |
| 318 | 338 | Đẻ trứng lv4 | NM | 90 | Đệ tử 65% sức đánh (#79) |
| 319 | 339 | Đẻ trứng lv5 | NM | 100 | Đệ tử 70% sức đánh (#79) |
| 320 | 340 | Đẻ trứng lv6 | NM | 110 | Đệ tử 75% sức đánh (#79) |
| 321 | 341 | Đẻ trứng lv7 | NM | 120 | Đệ tử 80% sức đánh (#79) |
| 350 | 129 | Tái tạo năng lượng lv1 | XD | 30 | Sức đánh+100% (#50) |
| 351 | 130 | Tái tạo năng lượng lv2 | XD | 40 | Sức đánh+110% (#50) |
| 352 | 131 | Tái tạo năng lượng lv3 | XD | 50 | Sức đánh+120% (#50) |
| 353 | 132 | Tái tạo năng lượng lv4 | XD | 60 | Sức đánh+130% (#50) |
| 354 | 133 | Tái tạo năng lượng lv5 | XD | 70 | Sức đánh+140% (#50) |
| 355 | 134 | Tái tạo năng lượng lv6 | XD | 80 | Sức đánh+150% (#50) |
| 356 | 135 | Tái tạo năng lượng lv7 | XD | 90 | Sức đánh+160% (#50) |
| 357 | 314 | Hóa khỉ khổng lồ lv1 | XD | 100 | Tấn công+109% (#49); HP+40% (#77) |
| 358 | 315 | Hóa khỉ khổng lồ lv2 | XD | 120 | Tấn công+110% (#49); HP+50% (#77) |
| 359 | 316 | Hóa khỉ khổng lồ lv3 | XD | 140 | Tấn công+110% (#49); HP+60% (#77) |
| 360 | 317 | Hóa khỉ khổng lồ lv4 | XD | 160 | Tấn công+112% (#49); HP+70% (#77) |
| 361 | 318 | Hóa khỉ khổng lồ lv5 | XD | 170 | Tấn công+113% (#49); HP+80% (#77) |
| 362 | 319 | Hóa khỉ khổng lồ lv6 | XD | 180 | Tấn công+114% (#49); HP+90% (#77) |
| 363 | 320 | Hóa khỉ khổng lồ lv7 | XD | 200 | Tấn công+115% (#49); HP+100% (#77) |
| 364 | 509 | Sách Huýt Sáo lv1 | XD | 100 | Tăng và hồi phục 40% HP tạm thời cho mình và xung quanh trong 30 giây (#125) |
| 365 | 510 | Sách Huýt Sáo lv2 | XD | 120 | Tăng và hồi phục 50% HP tạm thời cho mình và xung quanh trong 30 giây (#125) |
| 366 | 511 | Sách Huýt Sáo lv3 | XD | 140 | Tăng và hồi phục 60% HP tạm thời cho mình và xung quanh trong 30 giây (#125) |
| 367 | 512 | Sách Huýt Sáo lv4 | XD | 160 | Tăng và hồi phục 70% HP tạm thời cho mình và xung quanh trong 30 giây (#125) |
| 368 | 513 | Sách Huýt Sáo lv5 | XD | 180 | Tăng và hồi phục 80% HP tạm thời cho mình và xung quanh trong 30 giây (#125) |
| 369 | 514 | Sách Huýt Sáo lv6 | XD | 200 | Tăng và hồi phục 90% HP tạm thời cho mình và xung quanh trong 30 giây (#125) |
| 370 | 515 | Sách Huýt Sáo lv7 | XD | 220 | Tăng và hồi phục 100% HP tạm thời cho mình và xung quanh trong 30 giây (#125) |

#### Shop id=26 — `SANTA_DANH_HIEU` (NPC 39 – Santa, type_shop=0)


**Tab 44 – "Danh Hiệu"**: 19 item đang bán

| item_shop.id | temp_id | Tên | HT | Giá | Loại tiền | Option |
|---|---|---|---|---|---|---|
| 750 | 1286 | Kẻ thao túng sói | Chung | 2 | Ngọc xanh | Sức đánh+10% (#50); +5% sức đánh chí mạng (#5); Hạn sử dụng 30 ngày (#93); Hoàn thành 0% (#220) |
| 751 | 1287 | Nước anh bao | Chung | 2 | Ngọc xanh | HP+5% (#77); KI +5% (#103); Hạn sử dụng 30 ngày (#93); Hoàn thành 0% (#220); Sức đánh+5% (#50) |
| 752 | 1289 | Đại gia mới nhú | Chung | 2 | Ngọc xanh | Sức đánh+15% (#50); Hạn sử dụng 30 ngày (#93) |
| 753 | 1290 | Trùm ước rồng | Chung | 2 | Ngọc xanh | HP+6% (#77); Hạn sử dụng 30 ngày (#93); Hoàn thành 0% (#220) |
| 754 | 1291 | Trùm săn Boss | Chung | 2 | Ngọc xanh | Sức đánh+5% (#50); Hạn sử dụng 30 ngày (#93); Hoàn thành 0% (#220) |
| 755 | 1292 | Thánh đập đồ +7 | Chung | 2 | Ngọc xanh | HP+10% (#77); KI +10% (#103); Hạn sử dụng 30 ngày (#93); Hoàn thành 0% (#220) |
| 756 | 1293 | Cao thủ siêu hạng | Chung | 2 | Ngọc xanh | HP+8% (#77); Hạn sử dụng 3 ngày (#93); Hoàn thành 0% (#220) |
| 757 | 1294 | Nông dân chăm chỉ | Chung | 2 | Ngọc xanh | HP+5% (#77); KI +5% (#103); Hạn sử dụng 30 ngày (#93); Hoàn thành 0% (#220) |
| 758 | 1295 | Ông thần ve chai | Chung | 2 | Ngọc xanh | 3% Né đòn (#108); HP+3% (#77); KI +3% (#103); Hạn sử dụng 30 ngày (#93); Hoàn thành 0% (#220) |
| 759 | 1296 | Bị móc sạch túi | Chung | 2 | Ngọc xanh | 5% Né đòn (#108); HP+5% (#77); KI +5% (#103); Hạn sử dụng 30 ngày (#93); Hoàn thành 0% (#220) |
| 760 | 1299 | Fan cứng | Chung | 2 | Ngọc xanh | Sức đánh+3% (#50); HP+3% (#77); KI +3% (#103); Hạn sử dụng 30 ngày (#93); Hoàn thành 0% (#220) |
| 761 | 1300 | Thánh ở dơ | Chung | 2 | Ngọc xanh | Sức đánh+3% (#50); HP+3% (#77); KI +3% (#103); Hạn sử dụng 30 ngày (#93); Hoàn thành 0% (#220) |
| 901 | 1392 | Gõ đầu trẻ | Chung | 2 | Ngọc xanh | Sức đánh+10% (#50); HP+10% (#77); KI +10% (#103); Hạn sử dụng 30 ngày (#93) |
| 902 | 1393 | Gõ đầu trẻ | Chung | 2 | Ngọc xanh | Sức đánh+10% (#50); HP+10% (#77); KI +10% (#103); Hạn sử dụng 30 ngày (#93) |
| 903 | 1394 | Gõ đầu trẻ | Chung | 2 | Ngọc xanh | Sức đánh+10% (#50); HP+10% (#77); KI +10% (#103); Hạn sử dụng 10 ngày (#93) |
| 904 | 1457 | Danh hiệu X-mas | Chung | 2 | Ngọc xanh | Sức đánh+12% (#50); HP+12% (#77); KI +12% (#103); Hạn sử dụng 30 ngày (#93) |
| 905 | 1514 | Em xinh, em đẹp | Chung | 2 | Ngọc xanh | Sức đánh+11% (#50); HP+11% (#77); KI +11% (#103); Đẹp +5% SĐ cho mình và người xung quanh (#117); Hạn sử dụng 30 ngày (#93) |
| 955 | 1790 | Mẹ Rồng | Chung | 2 | Ngọc xanh | Sức đánh+13% (#50); HP+13% (#77); +7% sức đánh chí mạng (#5); Hạn sử dụng 30 ngày (#93) |
| 997 | 1297 | KOL | Chung | 2 | Ngọc xanh | Sức đánh+10% (#50); HP+10% (#77); KI +10% (#103); Hạn sử dụng 30 ngày (#93) |

**Tab 45 – "Sở Hữu "**: 19 item đang bán

| item_shop.id | temp_id | Tên | HT | Giá | Loại tiền | Option |
|---|---|---|---|---|---|---|
| 762 | 1286 | Kẻ thao túng sói | Chung | 2 | Ngọc xanh | Sức đánh+10% (#50); +5% sức đánh chí mạng (#5); Hạn sử dụng 30 ngày (#93) |
| 763 | 1287 | Nước anh bao | Chung | 2 | Ngọc xanh | HP+5% (#77); KI +5% (#103); Sức đánh+5% (#50); Hạn sử dụng 30 ngày (#93) |
| 764 | 1289 | Đại gia mới nhú | Chung | 2 | Ngọc xanh | Sức đánh+15% (#50); Hạn sử dụng 30 ngày (#93) |
| 765 | 1290 | Trùm ước rồng | Chung | 2 | Ngọc xanh | HP+6% (#77); Hạn sử dụng 30 ngày (#93) |
| 766 | 1291 | Trùm săn Boss | Chung | 2 | Ngọc xanh | Sức đánh+5% (#50); Hạn sử dụng 30 ngày (#93) |
| 767 | 1292 | Thánh đập đồ +7 | Chung | 2 | Ngọc xanh | HP+10% (#77); KI +10% (#103); Hạn sử dụng 30 ngày (#93) |
| 768 | 1293 | Cao thủ siêu hạng | Chung | 2 | Ngọc xanh | HP+8% (#77); Hạn sử dụng 3 ngày (#93) |
| 769 | 1294 | Nông dân chăm chỉ | Chung | 2 | Ngọc xanh | HP+5% (#77); KI +5% (#103); Hạn sử dụng 30 ngày (#93) |
| 770 | 1295 | Ông thần ve chai | Chung | 2 | Ngọc xanh | 3% Né đòn (#108); HP+3% (#77); Hạn sử dụng 30 ngày (#93) |
| 771 | 1296 | Bị móc sạch túi | Chung | 2 | Ngọc xanh | 5% Né đòn (#108); HP+5% (#77); Hạn sử dụng 5 ngày (#93) |
| 772 | 1299 | Fan cứng | Chung | 2 | Ngọc xanh | Sức đánh+3% (#50); HP+3% (#77); KI +3% (#103); Hạn sử dụng 30 ngày (#93) |
| 773 | 1300 | Thánh ở dơ | Chung | 2 | Ngọc xanh | Sức đánh+3% (#50); HP+3% (#77); KI +3% (#103); Hạn sử dụng 3 ngày (#93) |
| 948 | 1392 | Gõ đầu trẻ | Chung | 2 | Ngọc xanh | Sức đánh+10% (#50); HP+10% (#77); KI +10% (#103); Hạn sử dụng 30 ngày (#93) |
| 949 | 1393 | Gõ đầu trẻ | Chung | 2 | Ngọc xanh | Sức đánh+10% (#50); HP+10% (#77); KI +10% (#103); Hạn sử dụng 30 ngày (#93) |
| 950 | 1394 | Gõ đầu trẻ | Chung | 2 | Ngọc xanh | Sức đánh+10% (#50); HP+10% (#77); KI +10% (#103); Hạn sử dụng 30 ngày (#93) |
| 951 | 1457 | Danh hiệu X-mas | Chung | 2 | Ngọc xanh | Sức đánh+12% (#50); HP+12% (#77); KI +12% (#103); Hạn sử dụng 30 ngày (#93) |
| 952 | 1514 | Em xinh, em đẹp | Chung | 2 | Ngọc xanh | Sức đánh+11% (#50); HP+11% (#77); KI +11% (#103); Đẹp +11% SĐ cho mình và người xung quanh (#117); Hạn sử dụng 30 ngày (#93) |
| 956 | 1790 | Mẹ Rồng | Chung | 2 | Ngọc xanh | Sức đánh+13% (#50); HP+13% (#77); +7% sức đánh chí mạng (#5); Hạn sử dụng 30 ngày (#93) |
| 998 | 1297 | KOL | Chung | 2 | Ngọc xanh | Sức đánh+10% (#50); HP+10% (#77); KI +10% (#103); Hạn sử dụng 30 ngày (#93) |

#### Shop id=29 — `QDDN` (NPC 13 – Quy Lão Kame, type_shop=3)


**Tab 49 – "Đổi Thưởng"**: 12 item đang bán

| item_shop.id | temp_id | Tên | HT | Giá | Trả bằng (icon_spec → item) | Option |
|---|---|---|---|---|---|---|
| 802 | 1632 | CT Himmel | Chung | 199 | icon 14116 → None ? | — |
| 803 | 1587 | CT Goku SSJ3 | Chung | 199 | icon 14116 → None ? | — |
| 804 | 1590 | CT Goku SSJ Blue | Chung | 199 | icon 14116 → None ? | — |
| 805 | 1731 | CT Black Goku Rose | Chung | 299 | icon 14116 → None ? | — |
| 806 | 1001 | Phóng lợn | Chung | 59 | icon 14116 → None ? | — |
| 807 | 1023 | Quạt ba tiêu | Chung | 69 | icon 14116 → None ? | — |
| 808 | 1022 | Búa Stormbreaker | Chung | 79 | icon 14116 → None ? | — |
| 809 | 1021 | Búa Mjolnir | Chung | 99 | icon 14116 → None ? | — |
| 810 | 1114 | Pet Ma vàng phù thủy | Chung | 59 | icon 14116 → None ? | — |
| 811 | 1107 | Pet Bí Ma Vương | Chung | 99 | icon 14116 → None ? | — |
| 812 | 1465 | Máy bay trực thăng Noel | Chung | 79 | icon 14116 → None ? | — |
| 813 | 1477 | Thỏi vàng bay | Chung | 199 | icon 14116 → None ? | — |

**Tab 51 – "Event"**: 6 item đang bán

| item_shop.id | temp_id | Tên | HT | Giá | Trả bằng (icon_spec → item) | Option |
|---|---|---|---|---|---|---|
| 814 | 1525 | Túi hạt giống Hoa Hồng | Chung | 5 | icon 0 → 1826  | Không thể giao dịch (#30); Phản 30% sát thương (#97) |
| 815 | 1528 | Chậu đất | Chung | 2.000.000 | icon 0 → 1826  | Không thể giao dịch (#30); Phản 30% sát thương (#97) |
| 816 | 1529 | Thuốc tăng trưởng | Chung | 1 | icon 0 → 1826  | Không thể giao dịch (#30); Phản 30% sát thương (#97) |
| 817 | 1521 | Thiệp mừng 8-3 | Chung | 50 | icon 0 → 1826  | Không thể giao dịch (#30); Phản 30% sát thương (#97) |
| 818 | 722 | Capsule hồng | Chung | 399 | icon 0 → 1826  | Không thể giao dịch (#30); Phản 30% sát thương (#97) |
| 819 | 1509 | Nơ trang trí | Chung | 5 | icon 0 → 1826  | Không thể giao dịch (#30); Phản 30% sát thương (#97) |

#### Shop id=30 — `SANTA_GIAM_GIA_1` (NPC 39 – Santa, type_shop=0)


**Tab 50 – "Giảm giá 80%"**: 18 item đang bán

| item_shop.id | temp_id | Tên | HT | Giá | Loại tiền | Option |
|---|---|---|---|---|---|---|
| 930 | 458 | Cải trang | Chung | 400 | Ngọc xanh | Phân tâm (#111); Dò pha lê (#110); Không thể bán lại (#154) |
| 931 | 455 | Cải trang | Chung | 400 | Ngọc xanh | Hôi, giảm 10% HP (#109); Dò pha lê (#110); Không thể bán lại (#154) |
| 932 | 452 | Cải trang | Chung | 400 | Ngọc xanh | +20% tiềm năng, sức mạnh (#101); Không thể bán lại (#154) |
| 933 | 451 | Cải trang | Chung | 400 | Ngọc xanh | +20% vàng từ quái (#100); Không thể bán lại (#154) |
| 934 | 450 | Cải trang | Chung | 400 | Ngọc xanh | Không ảnh hưởng bởi cái lạnh (#106); Không thể bán lại (#154) |
| 935 | 449 | Cải trang | Chung | 400 | Ngọc xanh | Vô hình khi không đánh quái và boss (#105); Không thể bán lại (#154) |
| 936 | 448 | Cải trang | Chung | 400 | Ngọc xanh | Biến 50% tấn công quái thành HP (#104); Không thể bán lại (#154) |
| 937 | 433 | Cải trang | Chung | 10 | Ngọc xanh | Sức đánh+15% (#50); Không thể bán lại (#154) |
| 938 | 432 | Cải trang | Chung | 10 | Ngọc xanh | Sức đánh+14% (#50); Không thể bán lại (#154) |
| 939 | 431 | Cải trang | Chung | 10 | Ngọc xanh | Sức đánh+13% (#50); Không thể bán lại (#154) |
| 940 | 430 | Cải trang | Chung | 10 | Ngọc xanh | Sức đánh+12% (#50); Không thể bán lại (#154) |
| 941 | 429 | Cải trang | Chung | 10 | Ngọc xanh | Sức đánh+12% (#50); Không thể bán lại (#154) |
| 942 | 428 | Cải trang | Chung | 10 | Ngọc xanh | Sức đánh+15% (#50); Không thể bán lại (#154) |
| 943 | 427 | Cải trang | Chung | 10 | Ngọc xanh | Sức đánh+14% (#50); Không thể bán lại (#154) |
| 944 | 426 | Cải trang | Chung | 10 | Ngọc xanh | Sức đánh+13% (#50); Không thể bán lại (#154) |
| 945 | 425 | Cải trang | Chung | 10 | Ngọc xanh | Sức đánh+13% (#50); Không thể bán lại (#154) |
| 946 | 424 | Cải trang | Chung | 10 | Ngọc xanh | Sức đánh+13% (#50); Không thể bán lại (#154) |
| 947 | 423 | Cải trang | Chung | 10 | Ngọc xanh | Sức đánh+13% (#50); Không thể bán lại (#154) |

#### Shop id=31 — `SHOP_VIP` (NPC 39 – Santa, type_shop=3)


**Tab 52 – "Cải trang"**: 19 item đang bán

| item_shop.id | temp_id | Tên | HT | Giá | Trả bằng (icon_spec → item) | Option |
|---|---|---|---|---|---|---|
| 836 | 1731 | CT Black Goku Rose | Chung | 300 | icon 4028 → 457 Thỏi vàng | Sức đánh+30% (#50); HP+30% (#77); KI +30% (#103); +30% sức đánh chí mạng (#5); Không ảnh hưởng bởi cái lạnh (#106) |
| 837 | 1732 | CT Black Goku | Chung | 300 | icon 4028 → 457 Thỏi vàng | Sức đánh+30% (#50); HP+30% (#77); KI +30% (#103); +30% sức đánh chí mạng (#5); Không ảnh hưởng bởi cái lạnh (#106) |
| 838 | 1553 | CT Goku SSJ4 | Chung | 300 | icon 4028 → 457 Thỏi vàng | Sức đánh+30% (#50); HP+30% (#77); KI +30% (#103); +30% sức đánh chí mạng (#5); Không ảnh hưởng bởi cái lạnh (#106) |
| 839 | 1693 | CT Cađíc SSJ4 | Chung | 300 | icon 4028 → 457 Thỏi vàng | Sức đánh+30% (#50); HP+30% (#77); KI +30% (#103); +30% sức đánh chí mạng (#5); Không ảnh hưởng bởi cái lạnh (#106) |
| 840 | 1632 | CT Himmel | Chung | 300 | icon 4028 → 457 Thỏi vàng | Sức đánh+30% (#50); HP+30% (#77); KI +30% (#103); +30% sức đánh chí mạng (#5); Không ảnh hưởng bởi cái lạnh (#106) |
| 841 | 1741 | CT Cađíc | Chung | 300 | icon 4028 → 457 Thỏi vàng | Sức đánh+30% (#50); HP+30% (#77); KI +30% (#103); +30% sức đánh chí mạng (#5); Không ảnh hưởng bởi cái lạnh (#106) |
| 842 | 1742 | CT Cađíc SSJ | Chung | 300 | icon 4028 → 457 Thỏi vàng | Sức đánh+30% (#50); HP+30% (#77); KI +30% (#103); +30% sức đánh chí mạng (#5); Không ảnh hưởng bởi cái lạnh (#106) |
| 843 | 1743 | CT Cađíc SSJ2 | Chung | 300 | icon 4028 → 457 Thỏi vàng | Sức đánh+30% (#50); HP+30% (#77); KI +30% (#103); +30% sức đánh chí mạng (#5); Không ảnh hưởng bởi cái lạnh (#106) |
| 844 | 1476 | Cải trang Bunma rực rỡ | Chung | 300 | icon 4028 → 457 Thỏi vàng | Sức đánh+30% (#50); HP+30% (#77); KI +30% (#103); +30% sức đánh chí mạng (#5); Không ảnh hưởng bởi cái lạnh (#106) |
| 845 | 1657 | CT Gohan đi biển | Chung | 300 | icon 4028 → 457 Thỏi vàng | Sức đánh+30% (#50); HP+30% (#77); KI +30% (#103); +30% sức đánh chí mạng (#5); Không ảnh hưởng bởi cái lạnh (#106) |
| 846 | 1667 | CT Gohan kính mát | Chung | 300 | icon 4028 → 457 Thỏi vàng | Sức đánh+30% (#50); HP+30% (#77); KI +30% (#103); +30% sức đánh chí mạng (#5); Không ảnh hưởng bởi cái lạnh (#106) |
| 847 | 1503 | CT Lý Tiểu Nương hầu gái xanh | Chung | 300 | icon 4028 → 457 Thỏi vàng | Sức đánh+30% (#50); HP+30% (#77); KI +30% (#103); +30% sức đánh chí mạng (#5); Không ảnh hưởng bởi cái lạnh (#106) |
| 848 | 1504 | CT Lý Tiểu Nương hầu gái hồng | Chung | 300 | icon 4028 → 457 Thỏi vàng | Sức đánh+30% (#50); HP+30% (#77); KI +30% (#103); +30% sức đánh chí mạng (#5); Không ảnh hưởng bởi cái lạnh (#106) |
| 870 | 1744 | CT Cađíc SSJ2 M | Chung | 300 | icon 4028 → 457 Thỏi vàng | Sức đánh+30% (#50); HP+30% (#77); KI +30% (#103); +30% sức đánh chí mạng (#5); Không ảnh hưởng bởi cái lạnh (#106) |
| 871 | 1745 | CT Cađíc SSJ3 | Chung | 300 | icon 4028 → 457 Thỏi vàng | Sức đánh+30% (#50); HP+30% (#77); KI +30% (#103); +30% sức đánh chí mạng (#5); Không ảnh hưởng bởi cái lạnh (#106) |
| 872 | 1746 | CT Cađíc SSJ Blue | Chung | 300 | icon 4028 → 457 Thỏi vàng | Sức đánh+30% (#50); HP+30% (#77); KI +30% (#103); +30% sức đánh chí mạng (#5); Không ảnh hưởng bởi cái lạnh (#106) |
| 873 | 1587 | CT Goku SSJ3 | Chung | 300 | icon 4028 → 457 Thỏi vàng | Sức đánh+30% (#50); HP+30% (#77); KI +30% (#103); +30% sức đánh chí mạng (#5); Không ảnh hưởng bởi cái lạnh (#106) |
| 874 | 1590 | CT Goku SSJ Blue | Chung | 300 | icon 4028 → 457 Thỏi vàng | Sức đánh+30% (#50); HP+30% (#77); KI +30% (#103); +30% sức đánh chí mạng (#5); Không ảnh hưởng bởi cái lạnh (#106) |
| 875 | 1557 | Hắc Mị Nương | Chung | 300 | icon 4028 → 457 Thỏi vàng | Sức đánh+30% (#50); HP+30% (#77); KI +30% (#103); +30% sức đánh chí mạng (#5); Không ảnh hưởng bởi cái lạnh (#106) |

**Tab 53 – "Deo Lưng"**: 21 item đang bán

| item_shop.id | temp_id | Tên | HT | Giá | Trả bằng (icon_spec → item) | Option |
|---|---|---|---|---|---|---|
| 849 | 1669 | Balo Capybara | Chung | 100 | icon 4028 → 457 Thỏi vàng | Sức đánh+10% (#50); HP+10% (#77); KI +10% (#103) |
| 850 | 1670 | Balo Capybara hồng | Chung | 100 | icon 4028 → 457 Thỏi vàng | Sức đánh+10% (#50); HP+10% (#77); KI +10% (#103) |
| 851 | 1675 | Lồng đèn kéo quân NRO | Chung | 100 | icon 4028 → 457 Thỏi vàng | Sức đánh+10% (#50); HP+10% (#77); KI +10% (#103) |
| 852 | 1679 | Cờ Olympic | Chung | 100 | icon 4028 → 457 Thỏi vàng | Sức đánh+10% (#50); HP+10% (#77); KI +10% (#103) |
| 853 | 1680 | Hỏa tiêm thương | Chung | 100 | icon 4028 → 457 Thỏi vàng | Sức đánh+10% (#50); HP+10% (#77); KI +10% (#103) |
| 854 | 1751 | Diều rồng băng | Chung | 100 | icon 4028 → 457 Thỏi vàng | Sức đánh+10% (#50); HP+10% (#77); KI +10% (#103) |
| 855 | 1502 | Thanh Long Yển Nguyệt đao | Chung | 100 | icon 4028 → 457 Thỏi vàng | Sức đánh+10% (#50); HP+10% (#77); KI +10% (#103) |
| 856 | 1539 | Bụi tre | Chung | 100 | icon 4028 → 457 Thỏi vàng | Sức đánh+10% (#50); HP+10% (#77); KI +10% (#103) |
| 857 | 1531 | Cánh thiên thần 3 | Chung | 100 | icon 4028 → 457 Thỏi vàng | Sức đánh+10% (#50); HP+10% (#77); KI +10% (#103) |
| 858 | 1562 | Mặt trời tí hon | Chung | 100 | icon 4028 → 457 Thỏi vàng | Sức đánh+10% (#50); HP+10% (#77); KI +10% (#103) |
| 859 | 1574 | Cánh sấm sét | Chung | 100 | icon 4028 → 457 Thỏi vàng | Sức đánh+10% (#50); HP+10% (#77); KI +10% (#103) |
| 860 | 1577 | Cánh sấm sét hoàng kim | Chung | 100 | icon 4028 → 457 Thỏi vàng | Sức đánh+10% (#50); HP+10% (#77); KI +10% (#103) |
| 861 | 1687 | Cờ Goku bay | Chung | 100 | icon 4028 → 457 Thỏi vàng | Sức đánh+10% (#50); HP+10% (#77); KI +10% (#103) |
| 862 | 1688 | Cờ Hắc Vô Thường | Chung | 100 | icon 4028 → 457 Thỏi vàng | Sức đánh+10% (#50); HP+10% (#77); KI +10% (#103) |
| 863 | 1689 | Cờ Bạch Vô Thường | Chung | 100 | icon 4028 → 457 Thỏi vàng | Sức đánh+10% (#50); HP+10% (#77); KI +10% (#103) |
| 864 | 1467 | Gấu bắc cực | Chung | 100 | icon 4028 → 457 Thỏi vàng | Sức đánh+10% (#50); HP+10% (#77); KI +10% (#103) |
| 865 | 1722 | Cánh Thiên thần - Ác quỷ | Chung | 100 | icon 4028 → 457 Thỏi vàng | Sức đánh+10% (#50); HP+10% (#77); KI +10% (#103) |
| 866 | 1681 | Cờ Hải Ly xe máy | Chung | 100 | icon 4028 → 457 Thỏi vàng | Sức đánh+10% (#50); HP+10% (#77); KI +10% (#103) |
| 867 | 1702 | Lưỡi hái hồng | Chung | 100 | icon 4028 → 457 Thỏi vàng | Sức đánh+10% (#50); HP+10% (#77); KI +10% (#103) |
| 868 | 1713 | Khủng Long Thơ Mộng | Chung | 100 | icon 4028 → 457 Thỏi vàng | Sức đánh+10% (#50); HP+10% (#77); KI +10% (#103) |
| 869 | 1735 | Đeo lưng dụng cụ học tập | Chung | 100 | icon 4028 → 457 Thỏi vàng | Sức đánh+10% (#50); HP+10% (#77); KI +10% (#103) |

**Tab 54 – "Ván bay"**: 5 item đang bán

| item_shop.id | temp_id | Tên | HT | Giá | Trả bằng (icon_spec → item) | Option |
|---|---|---|---|---|---|---|
| 876 | 1603 | Tên lửa cá mập | Chung | 50 | icon 4028 → 457 Thỏi vàng | Sức đánh+5% (#50); HP+5% (#77); KI +5% (#103) |
| 877 | 1733 | Thú cưỡi Thích Kim Quy | Chung | 50 | icon 4028 → 457 Thỏi vàng | Sức đánh+5% (#50); HP+5% (#77); KI +5% (#103) |
| 878 | 1734 | Thú cưỡi Phong Xích Lan | Chung | 50 | icon 4028 → 457 Thỏi vàng | Sức đánh+5% (#50); HP+5% (#77); KI +5% (#103) |
| 879 | 1724 | Ván bay Sọ Dừa | Chung | 50 | icon 4028 → 457 Thỏi vàng | Sức đánh+5% (#50); HP+5% (#77); KI +5% (#103) |
| 880 | 1711 | Cân Đẩu Vân Thơ Mộng | Chung | 50 | icon 4028 → 457 Thỏi vàng | Sức đánh+5% (#50); HP+5% (#77); KI +5% (#103) |

**Tab 55 – "Pet"**: 5 item đang bán

| item_shop.id | temp_id | Tên | HT | Giá | Trả bằng (icon_spec → item) | Option |
|---|---|---|---|---|---|---|
| 881 | 1550 | Pet Godzilla | Chung | 150 | icon 4028 → 457 Thỏi vàng | Sức đánh+15% (#50); HP+15% (#77); KI +15% (#103) |
| 882 | 1551 | Pet Kong | Chung | 150 | icon 4028 → 457 Thỏi vàng | Sức đánh+15% (#50); HP+15% (#77); KI +15% (#103) |
| 883 | 1573 | Xe tăng Santa | Chung | 150 | icon 4028 → 457 Thỏi vàng | Sức đánh+15% (#50); HP+15% (#77); KI +15% (#103) |
| 884 | 1631 | Pet Zịt vàng bối rối | Chung | 150 | icon 4028 → 457 Thỏi vàng | Sức đánh+15% (#50); HP+15% (#77); KI +15% (#103) |
| 885 | 1458 | Pet Shiba đeo nơ | Chung | 150 | icon 4028 → 457 Thỏi vàng | Sức đánh+15% (#50); HP+15% (#77); KI +15% (#103) |

**Tab 56 – "glt"**: 1 item đang bán

| item_shop.id | temp_id | Tên | HT | Giá | Trả bằng (icon_spec → item) | Option |
|---|---|---|---|---|---|---|
| 886 | 1716 | Giáp tập luyện cấp 4 | Chung | 80 | icon 4028 → 457 Thỏi vàng | Hiệu lực trong 0 phút (#9) |

#### Shop id=32 — `DOI_SKILL_DE` (NPC 75 – Thỏ Đỏ ChiChi, type_shop=3)


**Tab 57 – "Đổi Skill Đệ"**: 3 item đang bán

| item_shop.id | temp_id | Tên | HT | Giá | Trả bằng (icon_spec → item) | Option |
|---|---|---|---|---|---|---|
| 887 | 1758 | Phở Linh Lang | Chung | 10 | icon 4028 → 457 Thỏi vàng | — |
| 888 | 1759 | Phở Giải Phóng | Chung | 20 | icon 4028 → 457 Thỏi vàng | — |
| 889 | 1760 | Phở Công viên Hòa Bình | Chung | 30 | icon 4028 → 457 Thỏi vàng | — |

#### Shop id=33 — `SHOP_CHI_CHI` (NPC 81 – Chi Chi, type_shop=0)


**Tab 58 – "Shop sự kiện"**: 3 item đang bán

| item_shop.id | temp_id | Tên | HT | Giá | Loại tiền | Option |
|---|---|---|---|---|---|---|
| 896 | 1609 | Kem trái cây | Chung | 50 | Ngọc xanh | Không thể giao dịch (#30) |
| 897 | 1613 | Nước đá | Chung | 5 | Ngọc xanh | Không thể giao dịch (#30) |
| 918 | 1631 | Pet Zịt vàng bối rối | Chung | 1.500.000.000 | Vàng | Sức đánh+7% (#50); HP+8% (#77); KI +8% (#103); +10% May mắn (#236); +10% tiềm năng, sức mạnh (#101); Không thể giao dịch (#30) |

#### Shop id=34 — `SHOP_DOI_DIEM` (NPC 13 – Quy Lão Kame, type_shop=3)


**Tab 59 – "Đổi thưởng"**: 10 item đang bán

| item_shop.id | temp_id | Tên | HT | Giá | Trả bằng (icon_spec → item) | Option |
|---|---|---|---|---|---|---|
| 923 | 1731 | CT Black Goku Rose | Chung | 999 | icon 14116 → None ? | Sức đánh+33% (#50); KI +29% (#103); +18% sức đánh chí mạng (#5); Chí mạng+15% (#14); Không thể giao dịch (#30); Hạn sử dụng hoặc vĩnh viễn (#231) |
| 924 | 1713 | Khủng Long Thơ Mộng | Chung | 499 | icon 14116 → None ? | Sức đánh+18% (#50); HP+15% (#77); KI +15% (#103); Cute +3% SĐ cho mình và người xung quanh (#226); +15% May mắn (#236); Không thể giao dịch (#30); Hạn sử dụng hoặc vĩnh viễn (#231) |
| 925 | 1682 | Pet Hải Ly | Chung | 499 | icon 14116 → None ? | Sức đánh+15% (#50); HP+15% (#77); KI +18% (#103); +15% May mắn (#236); Không thể giao dịch (#30); Hạn sử dụng hoặc vĩnh viễn (#231) |
| 926 | 1698 | CT Urôn Trư Bát Giới | Chung | 499 | icon 14116 → None ? | Sức đánh+26% (#50); HP+25% (#77); KI +25% (#103); +45% TĐ chạy (#114); Giảm 15% sát thương (#94); +15% May mắn (#236); Không thể giao dịch (#30); Hạn sử dụng hoặc vĩnh viễn (#231) |
| 927 | 1711 | Cân Đẩu Vân Thơ Mộng | Chung | 750 | icon 14116 → None ? | Sức đánh+8% (#50); Hạn sử dụng 30 ngày (#93); HP+8% (#77); KI +8% (#103); Dùng để bay và phục hồi KI (#85); Cute +3% SĐ cho mình và người xung quanh (#226); +10% May mắn (#236); Hạn sử dụng hoặc vĩnh viễn (#231) |
| 928 | 1840 | Hộp quà Goku Day VIP | Chung | 99 | icon 14116 → None ? | Không thể giao dịch (#30); Hạn sử dụng 30 ngày (#93) |
| 929 | 1757 | Hộp quà Cađíc VIP | Chung | 99 | icon 14116 → None ? | Không thể giao dịch (#30); Hạn sử dụng 0 ngày (#93) |
| 953 | 1821 | Trứng vàng rồng nhí | Chung | 199 | icon 14116 → None ? | Không thể giao dịch (#30) |
| 995 | 1567 | CT Frieren | Chung | 999 | icon 14116 → None ? | Sức đánh+23% (#50); HP+20% (#77); KI +20% (#103); +20% sức đánh chí mạng (#5); +5 HP/30s (#27); Đẹp +25% SĐ cho mình và người xung quanh (#117); Không thể giao dịch (#30); Hạn sử dụng hoặc vĩnh viễn (#231) |
| 996 | 1608 | Hộp quà thiếu nhi | Chung | 9 | icon 14116 → None ? | Không thể giao dịch (#30); Hạn sử dụng 30 ngày (#93) |

#### Shop id=35 — `SHOP_CLAN` (NPC 47 – Giu-ma Đầu Bò, type_shop=3)


**Tab 60 – "Cừa Hàng Item"**: 11 item đang bán

| item_shop.id | temp_id | Tên | HT | Giá | Trả bằng (icon_spec → item) | Option |
|---|---|---|---|---|---|---|
| 957 | 1794 | Con dấu | Chung | 1 | icon 7223 → 1559 Capsule 1 món kích hoạt | Không thể giao dịch (#30) |
| 958 | 1204 | Mảnh Rồng thần Namếc | Chung | 1 | icon 7223 → 1559 Capsule 1 món kích hoạt | Không thể giao dịch (#30) |
| 959 | 1791 | Mảnh khỉ Oorazu | Chung | 2 | icon 7223 → 1559 Capsule 1 món kích hoạt | Không thể giao dịch (#30) |
| 960 | 1792 | Mảnh khỉ Oorazu 1 | Chung | 3 | icon 7223 → 1559 Capsule 1 món kích hoạt | Không thể giao dịch (#30) |
| 961 | 1793 | Mảnh khỉ Oorazu 2 | Chung | 4 | icon 7223 → 1559 Capsule 1 món kích hoạt | Không thể giao dịch (#30) |
| 962 | 1634 | Cápsule Vỡ | Chung | 10 | icon 7223 → 1559 Capsule 1 món kích hoạt | Không thể giao dịch (#30) |
| 963 | 1439 | Đá mài | Chung | 1 | icon 7223 → 1559 Capsule 1 món kích hoạt | Không thể giao dịch (#30) |
| 964 | 1438 | Dùi đục | Chung | 1 | icon 7223 → 1559 Capsule 1 món kích hoạt | Không thể giao dịch (#30) |
| 965 | 1423 | Hematite | Chung | 1 | icon 7223 → 1559 Capsule 1 món kích hoạt | Không thể giao dịch (#30) |
| 966 | 987 | Đá bảo vệ | Chung | 1 | icon 7223 → 1559 Capsule 1 món kích hoạt | Không thể giao dịch (#30) |
| 967 | 1635 | Cỏ bốn lá | Chung | 1 | icon 7223 → 1559 Capsule 1 món kích hoạt | Không thể giao dịch (#30) |

**Tab 61 – "Của Hàng Pet"**: 15 item đang bán

| item_shop.id | temp_id | Tên | HT | Giá | Trả bằng (icon_spec → item) | Option |
|---|---|---|---|---|---|---|
| 968 | 1620 | Pet Baby Shark | Chung | 50 | icon 7223 → 1559 Capsule 1 món kích hoạt | Sức đánh+10% (#50); HP+10% (#77); KI +10% (#103) |
| 969 | 1748 | Pet tuần lộc | Chung | 50 | icon 7223 → 1559 Capsule 1 món kích hoạt | Sức đánh+10% (#50); HP+10% (#77); KI +10% (#103) |
| 970 | 1750 | Pet Khủng Long ngok | Chung | 50 | icon 7223 → 1559 Capsule 1 món kích hoạt | Sức đánh+10% (#50); HP+10% (#77); KI +10% (#103) |
| 971 | 1729 | Pet Rồng Xanh | Chung | 50 | icon 7223 → 1559 Capsule 1 món kích hoạt | Sức đánh+10% (#50); HP+10% (#77); KI +10% (#103) |
| 972 | 1727 | Pet Ma cầu mưa | Chung | 50 | icon 7223 → 1559 Capsule 1 món kích hoạt | Sức đánh+10% (#50); HP+10% (#77); KI +10% (#103) |
| 973 | 1714 | Pet Giru | Chung | 50 | icon 7223 → 1559 Capsule 1 món kích hoạt | Sức đánh+10% (#50); HP+10% (#77); KI +10% (#103) |
| 974 | 1682 | Pet Hải Ly | Chung | 50 | icon 7223 → 1559 Capsule 1 món kích hoạt | Sức đánh+10% (#50); HP+10% (#77); KI +10% (#103) |
| 975 | 1683 | Pet Hải Ly Ong Vàng | Chung | 50 | icon 7223 → 1559 Capsule 1 món kích hoạt | Sức đánh+10% (#50); HP+10% (#77); KI +10% (#103) |
| 976 | 1668 | Pet Capybara hồng | Chung | 50 | icon 7223 → 1559 Capsule 1 món kích hoạt | Sức đánh+10% (#50); HP+10% (#77); KI +10% (#103) |
| 977 | 1629 | Pet Capybara đeo ba lô | Chung | 50 | icon 7223 → 1559 Capsule 1 món kích hoạt | Sức đánh+10% (#50); HP+10% (#77); KI +10% (#103) |
| 978 | 1630 | Pet Capybara xì mũi | Chung | 50 | icon 7223 → 1559 Capsule 1 món kích hoạt | Sức đánh+10% (#50); HP+10% (#77); KI +10% (#103) |
| 979 | 1631 | Pet Zịt vàng bối rối | Chung | 50 | icon 7223 → 1559 Capsule 1 món kích hoạt | Sức đánh+10% (#50); HP+10% (#77); KI +10% (#103) |
| 980 | 1573 | Xe tăng Santa | Chung | 50 | icon 7223 → 1559 Capsule 1 món kích hoạt | Sức đánh+10% (#50); HP+10% (#77); KI +10% (#103) |
| 981 | 1550 | Pet Godzilla | Chung | 50 | icon 7223 → 1559 Capsule 1 món kích hoạt | Sức đánh+10% (#50); HP+10% (#77); KI +10% (#103) |
| 982 | 1551 | Pet Kong | Chung | 50 | icon 7223 → 1559 Capsule 1 món kích hoạt | Sức đánh+10% (#50); HP+10% (#77); KI +10% (#103) |

**Tab 62 – "Cửa Hàng Ván Bay"**: 6 item đang bán

| item_shop.id | temp_id | Tên | HT | Giá | Trả bằng (icon_spec → item) | Option |
|---|---|---|---|---|---|---|
| 983 | 1541 | Môtô Bun ma | Chung | 50 | icon 7223 → 1559 Capsule 1 món kích hoạt | Sức đánh+10% (#50); HP+10% (#77); KI +10% (#103) |
| 984 | 1563 | Ván bay té nước | Chung | 50 | icon 7223 → 1559 Capsule 1 món kích hoạt | Sức đánh+10% (#50); HP+10% (#77); KI +10% (#103) |
| 985 | 1724 | Ván bay Sọ Dừa | Chung | 50 | icon 7223 → 1559 Capsule 1 món kích hoạt | Sức đánh+10% (#50); HP+10% (#77); KI +10% (#103) |
| 986 | 1733 | Thú cưỡi Thích Kim Quy | Chung | 50 | icon 7223 → 1559 Capsule 1 món kích hoạt | Sức đánh+10% (#50); HP+10% (#77); KI +10% (#103) |
| 987 | 1734 | Thú cưỡi Phong Xích Lan | Chung | 50 | icon 7223 → 1559 Capsule 1 món kích hoạt | Sức đánh+10% (#50); HP+10% (#77); KI +10% (#103) |
| 988 | 1749 | Thú cưỡi Cây thông | Chung | 50 | icon 7223 → 1559 Capsule 1 món kích hoạt | Sức đánh+10% (#50); HP+10% (#77); KI +10% (#103) |

#### Shop id=36 — `SHOP_SU_KIEN_VL` (NPC 55 – Bill, type_shop=3)


**Tab 63 – "Đổi Thưởng"**: 5 item đang bán

| item_shop.id | temp_id | Tên | HT | Giá | Trả bằng (icon_spec → item) | Option |
|---|---|---|---|---|---|---|
| 990 | 1569 | Kho báu hải tặc | Chung | 9 | icon 15485 → 1805 Phiếu thức ăn | Không thể giao dịch (#30) |
| 991 | 1822 | Rada ngọc rồng | Chung | 19 | icon 15485 → 1805 Phiếu thức ăn | Hạn sử dụng 30 ngày (#93) |
| 992 | 1806 | Kem que | Chung | 55 | icon 15485 → 1805 Phiếu thức ăn | Sức đánh+15% (#50); HP+18% (#77); KI +15% (#103); +9% sức đánh chí mạng (#5); +5 KI/30s (#28); Không thể giao dịch (#30); Hạn sử dụng hoặc vĩnh viễn (#231) |
| 993 | 1620 | Pet Baby Shark | Chung | 19 | icon 15485 → 1805 Phiếu thức ăn | 4 Dòng chỉ số ẩn (#210); Hạn sử dụng hoặc vĩnh viễn (#231) |
| 994 | 1775 | Hộp thần linh | Chung | 99 | icon 15485 → 1805 Phiếu thức ăn | Không thể giao dịch (#30); Hạn sử dụng 30 ngày (#93) |

#### Item thuộc tab không tồn tại trong `tab_shop`

- tab_id=24: 559 Áo Thần Xayda (555 Vàng), 546 Cải trang (555 Vàng), 1087 Tanjiro (555 Vàng), 1088 Inosuke Hashibira (555 Vàng), 1089 Inosuke (555 Vàng), 1090 Zenitsu (555 Vàng), 1091 Nezuko (555 Vàng), 864 Cải trang Chaien (1 Vàng), 1034 Nến (123 Ngọc xanh), 999 Mèo mun (5 Ngọc xanh), 1000 Xiên cá (5 Ngọc xanh), 1001 Phóng lợn (5 Ngọc xanh), 1007 Ván lướt sóng (5 Ngọc xanh), 1013 Kiếm ánh sáng (5 Ngọc xanh), 1021 Búa Mjolnir (5 Ngọc xanh), 1022 Búa Stormbreaker (5 Ngọc xanh), 1023 Quạt ba tiêu (5 Ngọc xanh), 1028 Dao răng cưa (5 Ngọc xanh), 1030 Cờ Hoa đăng (5 Ngọc xanh), 1031 Cờ Hoa sen (5 Ngọc xanh), 1047 Lồng đèn lon (5 Ngọc xanh)
- tab_id=26: 555 Áo Thần Linh (100 Vàng), 556 Quần Thần Linh (100 Vàng), 657 Găng Hủy Diệt (100 Vàng), 563 Giầy Thần Linh (100 Vàng), 561 Nhẫn Thần Linh (100 Vàng)

---

## 9. Giao dịch giữa người chơi (Trade)

### 9.1 Hằng số (`Trade.java` dòng 27–29, `TransactionService.java` dòng 29)

| Hằng | Giá trị | Ý nghĩa |
|---|---|---|
| `TIME_TRADE` | 180.000 ms (**3 phút**) | Quá thời gian kể từ lúc mở bảng ⇒ tự huỷ (`Trade.update`) |
| `TIME_DELAY_TRADE` | 10.000 ms (**10 giây**) | Khoảng cách tối thiểu giữa 2 lần mời/kết thúc giao dịch (tính cho cả 2 người) |
| `QUANLITY_MAX` | 2.000.000.000 | Số lượng item tối đa 1 lần thêm |
| `MAX_GOLD_TRADE_PER_TIME` | **10.000.000** vàng | Số vàng tối đa đặt vào 1 giao dịch |
| Luồng nền | 300 ms/lần | `TransactionService.run` gọi `update()` mọi giao dịch |

### 9.2 Gói `-86` – `TransactionService.controller` (dòng 53–175)

| action | Hằng | Xử lý |
|---|---|---|
| 0 | `SEND_INVITE_TRADE` | Mời người chơi `playerId` cùng map |
| 1 | `ACCEPT_TRADE` | Chấp nhận lời mời ⇒ tạo `Trade` & mở bảng (chỉ khi người kia đã mời mình: `plMap.idMark.getPlayerTradeId() == pl.id`) |
| 2 | `ADD_ITEM_TRADE` | Thêm item (index túi) hoặc vàng (index −1) |
| 3 | `CANCEL_TRADE` | Huỷ |
| 5 | `LOCK_TRADE` | Khoá & gửi danh sách của mình cho đối phương |
| 7 | `ACCEPT` | Đồng ý; khi cả hai đồng ý (`accept == 2`) ⇒ thực hiện |

### 9.3 Điều kiện

| Điều kiện | Nguồn | Thông báo |
|---|---|---|
| Không bật **bảo vệ tài khoản** (`baovetaikhoan`) | dòng 59–62 | "Chức năng bảo vệ đã được bật…" |
| Tài khoản đã **kích hoạt thành viên** (`session.actived`) khi mời/chấp nhận | dòng 70–74 | "Truy Cập: {DOMAIN} Để Mở Thành Viên" |
| Khi thêm item cũng yêu cầu `actived` | `Trade.addItemTrade` dòng 92, 161–165 | "VUI LÒNG KÍCH HOẠT TÀI KHOẢN…" |
| Đối phương ở **cùng map** và là người chơi thật (`isPl()`) | dòng 76–77 | — |
| Đối phương không bật `tradeWVP` | dòng 78 | im lặng |
| Cả hai không trong giao dịch khác | dòng 81–121 | "Không thể thực hiện" |
| Qua 10 giây kể từ lần giao dịch trước của **cả hai** | dòng 87–110 | "Thử lại sau …" |
| `PlayerDAO.checkLogout` = false cho cả hai (chống login trùng) | dòng 89–103 | kick session |
| Server không bảo trì khi khoá/đồng ý | dòng 149–161 | huỷ giao dịch |
| Mọi thao tác dùng/chuyển item, mua/bán shop, ký gửi khi đang giao dịch | `UseItem.getItem/doItem`, `Controller` | "Không thể thực hiện" / tự huỷ |

### 9.4 Thêm item/vàng (`Trade.addItemTrade` dòng 91–166)

- Vàng (`index = −1`): `0 ≤ quantity ≤ 10.000.000`, ghi đè số vàng đặt trước đó.
- Item: `quantity ≤ item.quantity` (quantity 0 ⇒ 1). Item bị từ chối (`removeItemTrade`, "Không thể giao dịch vật phẩm này") nếu `isItemCannotTran`.
- Số lượng > 99 bị **tách thành nhiều gói 99** + phần dư.
- Thao tác trên **bản sao túi** (`itemsBag1/2`), túi thật chỉ bị thay khi giao dịch thành công.

### 9.5 Vật phẩm không giao dịch được (`Trade.isItemCannotTran` dòng 220–254)

| Điều kiện | Ghi chú |
|---|---|
| Có option **30** (bất kể param) | |
| id **454, 921** (Bông tai Porata cấp 1, 2) | 1819 (cấp 3) không nằm trong danh sách, nhưng shop bán kèm option 30 |
| TYPE 27 **chỉ chặn id 590** (Bí kiếp) | Các item type 27 khác (Thỏi vàng 457, pet, hộp quà…) **giao dịch được** nếu không có option 30 |
| TYPE 5 cải trang, 6 đậu thần, 7 sách skill, 8 vật phẩm nhiệm vụ, 11 đeo lưng, 13 bùa, 22 vệ tinh, 23 thú cưỡi, 24 thú cưỡi VIP, 28 cờ, 31 bánh trung thu/tết, 32 giáp tập luyện | |
| Rương Gỗ 570 | Chỉ báo "Không thể giao dịch Rương Gỗ" nhưng **không return** ⇒ nếu không vướng điều kiện khác vẫn được thêm vào |

Các TYPE còn lại **giao dịch được**: 0–4 (trang bị), 9, 10, 12 (ngọc rồng), 14–17, 25, 29, 30 (sao pha lê), 33 (thẻ), 34, 36, 37, 75.

### 9.6 Thực hiện (`Trade.startTrade` dòng 353–401)

1. `gold1 + goldTrade2 > 200 tỷ` ⇒ thất bại (người 1 vượt giới hạn); tương tự người 2.
2. Thêm từng item của người 1 vào bản sao túi người 2 (`addItemList`, có gộp chồng) – thiếu chỗ ⇒ thất bại; sau đó ngược lại. Nếu người 2 là **bot** thì bỏ qua bước thêm.
3. Thành công: cộng/trừ vàng hai bên, **thay túi thật bằng bản sao**, gửi túi & tiền.
4. Ghi `history_transaction` (`HistoryTransactionDAO.insert` dòng 80): tên 2 người, item mỗi bên, túi trước/sau, vàng trước/sau, thời điểm. Lịch sử cũ hơn **3 ngày** bị xoá khi khởi động server (`deleteHistory`, `ServerManager.java` dòng 71).
5. `sendNotifyTrade`: đặt `lastTimeTrade` cho cả hai (bắt đầu 10 giây chờ).

**Phí giao dịch: không có** (không trừ vàng/ngọc/thỏi vàng nào).

---

## 10. Cửa hàng ký gửi

### 10.1 Thành phần

| Thành phần | Chi tiết |
|---|---|
| NPC | `KyGui` (npc template 28 "Cửa hàng ký gửi"). Menu "Mua bán Ký gửi" chỉ mở khi tài khoản **đã kích hoạt**. Hướng dẫn trong NPC ghi "Chỉ với 5 ngọc, Giá trị ký gửi 10k-200Tr vàng hoặc 2-2k ngọc" – **không khớp code** |
| Bộ nhớ | `ConsignShopManager.listItem` (toàn server, trong RAM) |
| Nạp | `Manager.java` dòng 715–738: `SELECT * FROM shop_ky_gui` |
| Lưu | `ConsignShopManager.save()` – `TRUNCATE shop_ky_gui` rồi `INSERT` lại toàn bộ; gọi sau mỗi thao tác và khi tắt server (`ServerManager.java` dòng 285) |
| Tab | `tabName = {"Áo Quần", "Găng Tay", "Phụ Kiện", "Linh tinh", ""}` – tab 4 (rỗng) là "của tôi" |

### 10.2 Bảng `shop_ky_gui`

| Cột | Kiểu | Ý nghĩa |
|---|---|---|
| `id` | int | id tin đăng (`getMaxId() + 1`) |
| `player_id` | int | người bán |
| `tab` | int | 0–3 (xem 10.4) |
| `item_id` | int | template id |
| `gold` | int | giá (−1 nếu bán bằng ngọc) |
| `gem` | int | giá ngọc xanh (−1 nếu bán bằng vàng) |
| `quantity` | int | số lượng |
| `itemOption` | text | JSON `[{"id":..,"param":..}]` |
| `isUpTop` | int | ưu tiên hiển thị (sắp giảm dần) |
| `isBuy` | int | 1 = đã có người mua, chờ người bán nhận tiền |

Dữ liệu mẫu trong dump (3 dòng): người chơi 1154 bán NR 6 sao (19) giá 10.000.000; 1179 bán NR 6 sao giá 50; 1184 bán NR 7 sao (20) giá 1 – đều `gold`, chưa bán.

### 10.3 Gói `-100` (`Controller.java` dòng 84–135)

Chặn nếu đang giao dịch hoặc bật bảo vệ tài khoản.

| action | Đọc thêm | Hàm |
|---|---|---|
| 0 | `short idItem` (index túi), `byte moneyType`, `int money`, `int/byte quantity` (client ≥ 220 dùng int) | `KiGui` (chỉ khi quantity > 0) |
| 1 | `short id` | `claimOrDel(…, 1)` – huỷ bán |
| 2 | `short id` | `claimOrDel(…, 2)` – nhận tiền |
| 3 | `short id`, byte, int (bỏ qua) | `buyItem` |
| 4 | `byte tab`, `byte page` | `openShopKyGui(pl, tab, page)` |
| 5 | `short id` | `upItemToTop` |

### 10.4 Điều kiện item được ký gửi (`itemCanConsign` dòng 334–346)

Item trong túi thoả **một** trong:
- có option **86** (Ký gửi vàng) hoặc **87** (Ký gửi ngọc);
- TYPE **14** (đá nâng cấp 220–224), **15** (Mảnh đá vụn), **6** (đậu thần);
- id **14–20** (Ngọc Rồng 1–7 sao).

và **không** có option 30 (`KiGui` dòng 386–392).

Tab tự xếp (`getTabKiGui` dòng 366–376): type 0–2 ⇒ tab 0 "Áo Quần"; 3–4 ⇒ tab 1 "Găng Tay"; 29 ⇒ tab 2 "Phụ Kiện"; còn lại ⇒ tab 3 "Linh tinh". (Tên tab không khớp loại: găng type 2 vào "Áo Quần", giày/rada vào "Găng Tay".)

### 10.5 Đăng bán – `KiGui` (dòng 378–439)

| Bước | Chi tiết |
|---|---|
| Phí đăng | Trừ **1 Thỏi vàng (457)** – phải có 1 chồng ≥ 1 thỏi; thiếu ⇒ "Bạn cần có ít nhất 1 thỏi vàng để làm phí đăng bán". **Phí bị trừ trước mọi kiểm tra khác** |
| Kiểm tra | option 30 ⇒ từ chối; `money <= 0` hoặc `quantity > item.quantity` ⇒ đóng (không hoàn phí) |
| Giới hạn số lượng / giá | Code `quantity > 99 && quantity < 0`, `money > 100000 && money < 0`, `money > 1000000 && money < 0` – điều kiện **không bao giờ đúng** ⇒ **không có giới hạn** |
| moneyType 0 | Đăng giá `gold = money`, `gem = −1` |
| moneyType 1 | Đăng giá `gem = money`, `gold = −1` |
| Kết quả | Trừ `quantity` khỏi túi, thêm `ConsignItem` (isUpTop 0, isBuy false), lưu DB |
| Thời hạn | **Không có** – tin đăng tồn tại tới khi bán/huỷ |
| Số tin tối đa / người | **Không giới hạn** |

### 10.6 Mua – `buyItem` (dòng 115–163)

| Bước | Chi tiết |
|---|---|
| Điều kiện | Sức mạnh người mua ≥ **17.000.000.000** (17 tỷ); tin tồn tại & chưa bán; không mua tin của chính mình |
| Trả tiền | `goldSell > 0` ⇒ trừ `goldSell` **vàng** (`inventory.gold`); ngược lại `gemSell > 0` ⇒ trừ ngọc xanh |
| Nhận item | Tạo item (quantity, options) ⇒ `addItemBag` (không kiểm tra ô trống; túi đầy thì item mất) |
| Tin đăng | `isBuy = true`, lưu DB – chờ người bán nhận tiền |

### 10.7 Nhận tiền / huỷ – `claimOrDel` (dòng 270–321)

| action | Điều kiện | Kết quả |
|---|---|---|
| 1 huỷ | tin của mình, chưa bán | Trả item về túi (không kiểm tra ô trống), xoá tin. **Không hoàn phí** |
| 2 nhận tiền | tin của mình, đã bán | Bán bằng vàng ⇒ nhận item **Thỏi vàng 457** số lượng `goldSell − goldSell×10/100`; bán bằng ngọc ⇒ `gem += gemSell − 10%`. Xoá tin |

**Phí bán: 10%** (làm tròn xuống theo phép chia nguyên).

### 10.8 Đẩy tin lên đầu

- `upItemToTop` (dòng 235–248): kiểm tra quyền sở hữu, hiện menu `UP_TOP_ITEM` "…Yêu cầu 5 Ngọc Xanh".
- Xử lý thực tế ở `NpcFactory.java` dòng 689–711: yêu cầu `gem >= 50` nhưng **chỉ trừ 5 ngọc**, `isUpTop += 1` (cộng dồn), **không gọi save**.
- `StartupItemToTop` (dòng 250–268, phí 2 thỏi vàng, đặt `isUpTop = 1`) **không được gọi**.

### 10.9 Hiển thị

- `openShopKyGui(pl)` (dòng 441–527): 4 tab thường hiển thị tối đa 21 tin đầu (index 0–20), tin chưa bán, sắp `isUpTop` giảm dần, số trang = `size/20 + 1`; tab 4 liệt kê tin của mình (trạng thái 0/1/2 = không phải của mình/đang bán/đã bán) + item trong túi đủ điều kiện ký gửi.
- `openShopKyGui(pl, tab, page)` (dòng 183–233): mỗi trang 20 tin từ `page×20` đến `page×20+20` (gồm cả hai đầu ⇒ 21 tin); danh sách đếm trang **loại trừ tin của mình** nhưng danh sách gửi thì không.

---

## 11. Thẻ sưu tầm (Radar)

### 11.1 Dữ liệu

- Item thẻ: TYPE **33** "Mảnh …" (id 828–842, 859, 956, 1204, 1791–1793). Bảng `radar` dùng **id = id item**.
- `Manager.java` dòng ~880–908 nạp vào `RadarService.RADAR_TEMPLATE`; lớp `RadarCard` (Id, IconId, Rank, Max, Type 0 = quái / 1 = nhân vật, Template = mob_id, Head/Body/Leg/Bag, Name, Info, Options, Require, RequireLevel, AuraId).
- Thẻ của người chơi: `player.Cards` (`Card`: Id, Amount, MaxAmount, Level, Used, Options) lưu JSON (`PlayerDAO` dòng 816, đọc `MrBlue` dòng 939).

### 11.2 Bảng `radar` (21 dòng)

`options` là danh sách `{id, param, activeCard}`; option có hiệu lực khi `activeCard == Level` của thẻ (hoặc Level = −1 và activeCard = 0). Tất cả `max = 120`, `require = −1`.

| id | Tên thẻ | Rank | Loại (mob_id / part) | activeCard 0 | activeCard 1 | activeCard 2 | aura_id |
|---|---|---|---|---|---|---|---|
| 828 | Thẻ Khủng long | 0 | quái 1 | HP+1000 | HP+2000 | HP+3000 | 1 |
| 829 | Thẻ Lợn lòi | 0 | quái 2 | KI+1000 | KI+2000 | KI+3000 | −1 |
| 830 | Thẻ Quỷ đất | 0 | quái 3 | Tấn công+10 | +20 | +30 | −1 |
| 831 | Thẻ Khủng long mẹ | 1 | quái 4 | HP+10000 | +20000 | +30000 | −1 |
| 832 | Thẻ Lợn lòi mẹ | 1 | quái 5 | KI+10000 | +20000 | +30000 | −1 |
| 833 | Thẻ Quỷ đất mẹ | 1 | quái 6 | Tấn công+100 | +200 | +300 | −1 |
| 834 | Thẻ Thằn lằn bay | 2 | quái 7 | HP+2%/30s | 3% | 5% | −1 |
| 835 | Thẻ Phi long | 2 | quái 8 | KI+2%/30s | 3% | 5% | −1 |
| 836 | Thẻ Quỷ bay | 2 | quái 9 | Giáp+50 | +100 | +150 | −1 |
| 837 | Thẻ Lính độc nhãn | 3 | quái 34 | HP+10000 | +20000 | +30000 | −1 |
| 838 | Thẻ lính độc nhãn | 3 | quái 35 | KI+10000 | +20000 | +30000 | −1 |
| 839 | Thẻ sói xám | 3 | quái 36 | Tấn công+500 | +700 | +900 | −1 |
| 840 | Thẻ trung úy trắng | 4 | part 141/142/143 | Giảm 5% sát thương | HP+5% | KI+5% | −1 |
| 841 | Thẻ ninja tím | 4 | part 123/124/125 | Giảm 5% sát thương | HP+5% | KI+5% | −1 |
| 842 | Thẻ trung úy xanh lơ | 4 | part 135/136/137 | Giảm 5% sát thương | HP+5% | KI+5% | −1 |
| 859 | Thẻ Độc Nhãn | 4 | part 144/145/146 | Giảm 5% sát thương | HP+5% | KI+5% | −1 |
| 956 | Thẻ Đội Trưởng Vàng | 4 | part 994/995/996 | Giảm 5% sát thương | HP+5% | KI+5% | 0 |
| 1204 | Thẻ Rồng Thần Namek | 5 | part 1204/1205/1206 | Sức đánh+5% | HP+5% | KI+5% | 1 |
| 1791 | Thẻ Oozaru | 5 | part 1727/1728/1729 | Chí mạng+5% | Sức đánh+5% | HP+10%, KI+5%, Giảm 3% sát thương | 2 |
| 1792 | Thẻ Oozarun 1 | 6 | part 1730/1731/1732 | Chí mạng+5% | Sức đánh+7% | HP+12%, KI+7%, Giảm 7% | 3 |
| 1793 | Thẻ Oozarun 2 | 6 | part 1733/1734/1735 | Chí mạng+5% | Sức đánh+10% | HP+15%, KI+10%, Giảm 7% | 4 |

### 11.3 Dùng thẻ – `UseItem.UseCard` (`UseItem.java` dòng 2746–2790)

| Trường hợp | Kết quả |
|---|---|
| Không có template radar trùng id | Bỏ qua |
| `Require != −1` và chưa có thẻ yêu cầu ở `RequireLevel` | "Bạn cần sưu tầm … ở cấp độ … mới có thể sử dụng thẻ này" (DB hiện không thẻ nào có yêu cầu) |
| Chưa có thẻ | Tạo `Card(id, Amount = 1, MaxAmount = template.Max, Level = −1)`, trừ 1 item |
| Đã có, `Level >= 2` | "Thẻ này đã đạt cấp tối đa" (không trừ) |
| Đã có | `Amount++`; nếu `Amount >= MaxAmount` (120): `Amount = 0`, `Level` −1 ⇒ 1, hoặc +1; trừ 1 item |

⇒ Cấp thẻ: **−1** (1–119 mảnh đầu) → **1** (sau 120 mảnh đầu tiên) → **2** (thêm 120 mảnh) = tối đa. Tổng 240 mảnh để max.

### 11.4 Kích hoạt & hiệu ứng (gói `127`, `Controller.java` dòng 138–166)

| action | Xử lý |
|---|---|
| 0 | `RadarService.sendRadar` – gửi toàn bộ template + tiến độ thẻ |
| 1 | Bật/tắt thẻ `id`: bỏ qua nếu `Level == 0`; bật khi chưa có thẻ nào khác `Used == 1` ⇒ **chỉ dùng 1 thẻ cùng lúc** ("Số thẻ sử dụng đã đạt tối đa") |

Chỉ số: `NPoint` dòng 252–411 lấy **thẻ đang Used**, cộng option có `active == Level` (hoặc `Level == −1 && active == 0`). Hỗ trợ option 0, 2, 3, 5, 6, 7, 8, 14, 16/114/148, 18, 19, 22, 23, 27, 28, 33–36, 47, 48, 49, 50, 77, 80, 81, 88, 94–101, 103–109, 111, 116, 117, 147, 156, 162, 173, 211, 226, 153.

Hào quang: `Player.getAura()` (dòng 732–745) – nếu sở hữu thẻ 956, 1204, 1791, 1792, 1793 (hoặc 1142) với **Level > 1** (tức cấp 2) thì hiển thị `aura_id` của thẻ đầu tiên thoả (không cần đang Used).

---

## 12. Ghi chú / điểm cần lưu ý

1. **Ký gửi – lệch đơn vị tiền nghiêm trọng**: người mua trả `goldSell` **vàng**, người bán lại nhận `goldSell × 90%` **Thỏi vàng** (mỗi thỏi bán NPC được 37.000.000 vàng) ⇒ có thể tạo vàng vô hạn (bán NR 1 sao giá 1.000 "vàng" ⇒ nhận 900 thỏi). Dữ liệu mẫu trong DB (giá 1, 50, 10.000.000) cho thấy đang bị hiểu nhầm.
2. Ký gửi: giới hạn giá/số lượng bị viết `&&` thay vì `||` ⇒ **không có giới hạn**; phí đăng 1 thỏi vàng bị trừ trước khi kiểm tra và **không hoàn** khi đăng lỗi hoặc huỷ; mua/huỷ/nhận không kiểm tra ô trống; không có thời hạn tin đăng; up top yêu cầu 50 ngọc nhưng trừ 5 và không lưu DB; lời NPC ("5 ngọc", "10k–200Tr vàng") không khớp code.
3. `ConsignShopManager.save()` dùng `TRUNCATE` + `String.format` chèn giá trị trực tiếp vào SQL mỗi thao tác ⇒ nếu lỗi giữa chừng có thể mất toàn bộ dữ liệu ký gửi; hiệu năng kém khi nhiều tin.
4. **Giao dịch**: thông báo `FAIL_NOT_ENOUGH_BAG_P1` được gán khi **túi người 2 đầy** (và ngược lại) ⇒ tên người thiếu chỗ bị hiển thị sai. Rương Gỗ 570 chỉ báo lỗi mà không `return`. Bông tai cấp 3 (1819) không nằm trong danh sách cấm cứng. `LOCK_TRADE`/`ACCEPT` khi bảo trì gọi `trade.cancelTrade()` trước khi kiểm tra `trade != null`.
5. Không có phí giao dịch; giới hạn vàng mỗi giao dịch 10 triệu nhưng **Thỏi vàng (457) giao dịch không giới hạn** ⇒ giới hạn vàng gần như vô nghĩa.
6. `subMoneyByItemShop`: `COST_RUBY` kiểm tra `gem` thay vì `ruby`. `subIemByItemShop`: tiền là ngọc (77) chỉ kiểm tra, **không trừ**.
7. Shop danh hiệu (tab 44/45) không trừ giá; tab 49 (`QDDN`) cho item **miễn phí** không kiểm tra ô trống (hiện không NPC nào mở); tab đổi điểm 59 trả 0 điểm cho item ngoài danh sách hard-code.
8. Tab "Giảm giá 80%" là tab **50** nhưng code xử lý phiếu giảm giá ở tab **30** ⇒ tab 50 bán **giá đầy đủ**, phiếu không bị trừ.
9. `SHOP_CLAN`: giá hard-code lệch DB (1791: 3 vs 2; 1792: 4 vs 3; 1793 không bán được).
10. `SHOP_VIP` (Santa) không bấm được do menu thiếu mục thứ 7; 9 shop trong DB (`BUNMA_LINHTHU`, `SANTA_RUONG`, `SANTA_HSD`, `OSIN`, `BULMA_TL`, `CHUBEDAN`, `BULMA_EVENT`, `DOI_SKILL_DE`, `QDDN`) không có code mở; `item_shop` có tab 24, 26 không tồn tại.
11. `learnKyNang` so sánh tiềm năng với `cost` DB (giá ngọc) nhưng trừ `powRequire`; menu 671 không kiểm tra lại tiềm năng ⇒ có thể âm tiềm năng.
12. Bán Thỏi vàng: form bán cho 37.000.000/thỏi, nhưng `sellItem` trực tiếp (nếu client gửi action ≠ 0) cho 500.000.000/thỏi (`template.gold`, không chia 4).
13. Kiểm tra "item có HSD không bán được" dùng `getParam(pl, 93, templateId)` – lấy item **đầu tiên cùng template trong túi**, không phải item đang bán ⇒ có thể chặn nhầm hoặc bỏ lọt. Option 154 "Không thể bán lại" không được kiểm tra.
14. Mua lại (`buyItemDaBan`) trừ tiền trước khi kiểm tra túi trống ⇒ mất tiền nếu túi đầy.
15. `TabShopUron/HangDoc/Santa/MuaAvatar.idDauCanBuy` ném exception nếu cấp cây đậu ngoài 1–10; gói đậu cấp 10 trỏ tới id 598 (Avatar); gói mua được là cấp cây **+1** nhưng lượng hồi tính theo cấp cây hiện tại.
16. `ShopTab.java` là cơ chế khoá IP: server tự tắt nếu IP máy không trùng `server.ip` trong `Config.properties` (không liên quan shop dù tên file).
17. Thẻ radar: `MaxAmount` kiểu `byte` (tối đa 127) – nếu sửa `max` trong DB > 127 sẽ tràn. Thẻ cấp −1 vẫn bật được (chỉ chặn Level 0).
