# 06 — Vật phẩm (Item): cấu trúc, option, hành trang, sinh chỉ số & sử dụng

> Tài liệu spec hệ thống vật phẩm của server **Ngọc Rồng Online – Teamobi2026**.
> Nguồn: mã Java trong `SRC/src/nro/models/**` và dump `database team2026.sql` (bảng `item_template`, `item_option_template`, `item_shop`, `item_shop_option`). Tên vật phẩm tra từ `item_template` (dump có đúng **2000 dòng, id 0 → 1999**; file `item.xlsx` ở gốc dự án có nội dung trùng khớp).
> Đường dẫn file Java viết tắt: `models/` = `SRC/src/nro/models/`. Số dòng là số dòng thực tế trong file tại thời điểm viết.
> Danh sách **đầy đủ** 2000 item và 251 option xem [02b-database-du-lieu-template.md](02b-database-du-lieu-template.md). Nâng cấp / đập đồ / pha lê hoá, rơi đồ, rồng thần có tài liệu riêng — ở đây chỉ nhắc tới phần liên quan tới cấu trúc item.

## Mục lục

1. [File nguồn liên quan](#1-file-nguồn-liên-quan)
2. [Cấu trúc một vật phẩm](#2-cấu-trúc-một-vật-phẩm)
3. [Các TYPE vật phẩm](#3-các-type-vật-phẩm)
4. [Ô trang bị trên người (itemsBody) & điều kiện mặc](#4-ô-trang-bị-trên-người-itemsbody--điều-kiện-mặc)
5. [Option (chỉ số) quan trọng](#5-option-chỉ-số-quan-trọng)
6. [Hành trang, rương đồ: giới hạn, xếp chồng, mở rộng](#6-hành-trang-rương-đồ-giới-hạn-xếp-chồng-mở-rộng)
7. [Tiền tệ & giới hạn](#7-tiền-tệ--giới-hạn)
8. [Ngọc rồng](#8-ngọc-rồng)
9. [Sinh vật phẩm có chỉ số ngẫu nhiên](#9-sinh-vật-phẩm-có-chỉ-số-ngẫu-nhiên)
10. [Sử dụng vật phẩm – UseItem.java](#10-sử-dụng-vật-phẩm--useitemjava)
11. [Ghi chú / điểm cần lưu ý](#11-ghi-chú--điểm-cần-lưu-ý)

---

## 1. File nguồn liên quan

| File | Vai trò |
|---|---|
| `models/item/Item.java` | Lớp `Item` + lớp con `Item.ItemOption`; các hàm nhận diện nhóm item (`isSKH`, `isDTL`, `isDHD`, `isDTS`, `isDoKyGui`…) và bảng option đá/sao pha lê (`getOptionDaPhaLe`). |
| `models/item/ItemTime.java` | Trạng thái & thời hạn các item dùng theo thời gian (bổ huyết, cuồng nộ, thức ăn…). |
| `models/services/ItemService.java` | Tạo item (`createNewItem`, `createItemFromItemShop`…), sinh đồ SKH / thần linh / thiên sứ, kiểm tra hạn dùng. |
| `models/services/InventoryService.java` | Thêm/bớt/sắp xếp item trong hành trang – rương – người, mặc/tháo, mở rộng ô. |
| `models/services_func/UseItem.java` | Xử lý khi người chơi **dùng** / **vứt** item (gói `-43`) và chuyển item giữa các kho (gói `-40`). |
| `models/services/ItemTimeService.java` | Gửi icon thời gian, bật/tắt Tự động luyện tập. |
| `models/consts/ConstItem.java` | 1227 hằng số id item + mảng `LIST_ITEM_CLOTHES`, `TrangBiKichHoat`… |
| `models/player/Inventory.java` | Kho đồ người chơi: `itemsBody`, `itemsBag`, `itemsBox`, `gold`, `gem`, `ruby`, `coupon`, giới hạn. |
| `models/player/SetClothes.java` | Đếm số món set kích hoạt đang mặc. |
| `models/player/NPoint.java` | Áp dụng option & item thời gian vào chỉ số. |
| `models/services/RewardService.java` | `initChiSoItem()` – gán chỉ số gốc cho đồ theo id. |
| `models/npc/MagicTree.java` | Đậu thần: id theo cấp & lượng hồi. |
| `models/database/MrBlue.java`, `PlayerDAO.java` | Đọc/ghi item của người chơi (JSON) & tạo nhân vật mới. |

---

## 2. Cấu trúc một vật phẩm

### 2.1 Lớp `Item` (`models/item/Item.java` dòng 13–35)

| Trường | Kiểu | Ý nghĩa |
|---|---|---|
| `template` | `ItemTemplate` | Template lấy từ `Manager.ITEM_TEMPLATES` (bảng `item_template`). `template == null` ⇒ **ô trống** (`isNotNullItem()` = false). |
| `quantity` | `int` | Số lượng. |
| `quantityGD` | `int` | Số lượng đưa vào giao dịch (chỉ dùng trong `Trade`). |
| `itemOptions` | `List<ItemOption>` | Danh sách option (chỉ số). |
| `createTime` | `long` | Thời điểm tạo (ms). Dùng để trừ **hạn sử dụng** (option 93). Mặc định = `System.currentTimeMillis()` trong constructor. |
| `info`, `content` | `String` | Chuỗi mô tả; `getContent()` = "Yêu cầu sức mạnh X trở lên" (dòng 64). |
| `id`, `text`, `expire`, `options` | — | Khai báo nhưng không dùng trong luồng chính. |

`Item.ItemOption` (dòng 80–133): `optionTemplate` (id + name từ `item_option_template`) và `param` (int). Chuỗi hiển thị = `name` thay `#` bằng `param`.

Hàm nhận diện nhóm (dòng 145–470):

| Hàm | Điều kiện |
|---|---|
| `isSKH()` | Có option id 127–135 (set kích hoạt đời cũ) |
| `isVaiTho()` | template id 0–65 |
| `isDTL()` / `isThanLinh()` | id 555–567 (Đồ Thần Linh) |
| `isDHD()` | id 650–662 (Đồ Hủy Diệt) |
| `isDTS()` | id 1048–1062 (Đồ Thiên Sứ) |
| `isManhTS()` | id 1066–1070 (Mảnh áo/quần/giầy/nhẫn/găng) |
| `isDaNangCap()` | id 1074–1078; `isDaMayMan()` id 1079–1083 |
| `isCongThucVip()` | id 1071–1073, 1084–1086 |
| `isThucAn()` | id 663–667 |
| `isGiayMau()` | id 1505 |
| `isSachTuyetKy()` | 1044, 1211, 1212; `isSachTuyetKy2()` 1278–1280 |
| `canPhaLeHoa()` | `type < 5` hoặc `type == 32` |
| `isDoKyGui()` | có option 86 hoặc 87, hoặc type 14, 15, 6, hoặc id 14–20 |

### 2.2 Template (bảng `item_template`)

| Cột | Dùng trong code |
|---|---|
| `id` | id template (short) |
| `TYPE` | loại item – quyết định ô mặc & cách dùng (mục 3) |
| `gender` | 0 Trái Đất, 1 Namếc, 2 Xayda, **3 = dùng chung** |
| `NAME`, `description` | tên, mô tả |
| `level` | cấp đồ (vd. 13 = Thần Linh, 14 = Hủy Diệt, 15 = Thiên Sứ) |
| `icon_id`, `part`, `head/body/leg` | hình ảnh; `part` của item type 11 là id `flag_bag` hiển thị trên lưng (`Player.getFlagBag()` dòng 998) |
| `is_up_to_up` | 1 = cho phép **xếp chồng** |
| `power_require` | sức mạnh tối thiểu (bị option 21 ghi đè khi mặc) |
| `gold`, `gem` | giá gốc – dùng tính giá **bán** (gold/4) và giá **mua lại** (mục trong tài liệu 07) |

### 2.3 Lưu trữ trong DB người chơi

Mỗi item trong cột `items_body`, `items_bag`, `items_box`, `items_box_lucky_round`, `items_daban` (bảng `player`) là mảng JSON:

```
[ tempId, quantity, "[[optionId,param],[optionId,param],...]", createTime ]
```

`tempId = -1` là ô trống (`PlayerDAO.createNewPlayer` dòng 90–180, đọc lại ở `MrBlue.java` dòng 347–470). Khi đọc, nếu `ItemService.isOutOfDateTime(item)` trả về true ⇒ item bị thay bằng ô trống (hết hạn). `items_daban` chỉ nạp tối đa **20** item.

Nhân vật mới (`PlayerDAO` dòng 85–180):

| Kho | Số ô | Item có sẵn |
|---|---|---|
| Body | 11 | Áo (id 0/1/2 theo hành tinh, option 47 Giáp +2 (XD +3)); Quần (6/7/8, option 6 HP +30 (TĐ) / +20) |
| Bag | 30 | Ô 0: item **63 – Đậu thần cấp 5** x10 với option 2 param 8 (comment code ghi "thỏi vàng" nhưng id 63 là Đậu thần cấp 5) |
| Box | 30 | Ô 0: **Rada cấp 1** (id 12), option 14 Chí mạng +1% |
| Lucky round / Đã bán | 110 / 110 | trống |

### 2.4 Tạo item (`ItemService.java`)

| Hàm | Dòng | Mô tả |
|---|---|---|
| `createItemNull()` | 43 | Item rỗng (ô trống). |
| `createNewItem(tempId[, quantity])` | 74–87 | Item mới, **không option**. Khi thêm vào kho qua `addItemList` mà chưa có option nào sẽ tự thêm option **73** (param 0, dòng trống). |
| `createItemFromItemShop(ItemShop)` | 48–58 | quantity = 1, copy option từ `item_shop_option`. |
| `createItemSetKichHoat(tempId, qty)` | 119–128 | Như `createNewItem` (tên gây nhầm, không thêm option SKH). |
| `copyItem(item)` | 60–72 | Copy sâu (template, quantity, createTime, options). |
| `createItemFromItemMap(ItemMap)` | 350 | Item nhặt từ đất. |
| `getListOptionItemShop(id)` | 660–668 | Lấy option của item **đầu tiên** có cùng template trong tất cả shop – dùng làm chỉ số gốc cho đồ SKH, rương gỗ. |

---

## 3. Các TYPE vật phẩm

Thống kê từ `item_template` (2000 dòng). Cột "Ô mặc" theo `InventoryService.putItemBody` (dòng 336–361); cột "Khi bấm dùng" theo `UseItem.useItem` (dòng 255–940).

| TYPE | Số item | Ý nghĩa (theo dữ liệu & code) | Ví dụ (id: tên) | Ô mặc | Khi bấm "dùng" |
|---|---|---|---|---|---|
| 0 | 45 | Áo | 0 Áo vải 3 lỗ, 555 Áo Thần Linh, 650 Áo Hủy Diệt, 1048 Áo Thiên Sứ | 0 | Mặc (client gửi -40) |
| 1 | 48 | Quần | 6 Quần vải đen, 556 Quần Thần Linh | 1 | Mặc |
| 2 | 45 | Găng | 21 Găng vải đen, 562 Găng Thần Linh | 2 | Mặc |
| 3 | 45 | Giày | 27 Giầy nhựa, 563 Giầy Thần Linh | 3 | Mặc |
| 4 | 17 | Rada / Nhẫn | 12 Rada cấp 1, 561 Nhẫn Thần Linh, 656 Nhẫn Hủy Diệt | 4 | Mặc |
| 5 | 457 | Cải trang / Avatar | 196–210 Avatar, 282–292 Cải trang, 1087 Tanjiro | 5 | Mặc |
| 6 | 11 | Đậu thần | 13, 60–65, 352, 523, 595 | — | `eatPea` |
| 7 | 154 | Sách kỹ năng (cũ, có cấp) | 66 Sách đấm Dragon lv1 … | — | Hỏi xác nhận → `learnSkill` |
| 8 | 3 | Vật phẩm nhiệm vụ | 73 Đùi gà, 75 Đùi heo Xayda, 85 Truyện tranh | — | Không có xử lý |
| 9 | 4 | Vàng (nhặt) | 76, 188, 189, 190 | — | Cộng thẳng vào `gold` khi nhặt |
| 10 | 1 | Ngọc xanh (nhặt) | 77 Ngọc | — | Cộng vào `gem` |
| 11 | 190 | Vật phẩm đeo lưng / cầm tay (flag bag); gồm cả Ngọc rồng Namếc 353–360, Ngọc rồng đen 372–378 | 467 Lồng đèn Ông Sao, 954 Bó Hoa Hồng, 1579 "1 sao" | 8 | Mặc + `sendFlagBag` |
| 12 | 28 | Ngọc rồng (gọi rồng) | 14–20 Ngọc Rồng 1–7 sao, 704 Bí ngô 3 sao, 925–931 Ngọc rồng băng | — | `controllerCallRongThan` |
| 13 | 14 | Bùa | 213–219, 522, 671, 672 | — | Không dùng từ túi; mua là kích hoạt thời gian (mục 10.6) |
| 14 | 5 | Đá nâng cấp | 220 Đá lục bảo, 221 Saphia, 222 Ruby, 223 Titan, 224 Thạch anh tím | — | — |
| 15 | 1 | Mảnh đá vụn | 225 | — | — |
| 16 | 1 | Bình nước phép | 226 | — | — |
| 17 | 3 | Đai lưng | 1752–1754 | — (không nằm trong danh sách mặc) | — |
| 22 | 4 | Vệ tinh | 342–345 | — | Hỏi xác nhận → thả vệ tinh |
| 23 | 50 | Thú cưỡi / ván bay mới | 346 Cân đẩu vân, 746 Xe tuần lộc, 795 Ghế bay | 9 | Mặc |
| 24 | 5 | Thú cưỡi VIP (cũ) | 349–351, 396, 532 Quỷ Chim | 9 | Mặc |
| 25 | 7 | Sách tuyệt kỹ / Gói rađa | 361 Gói 10 Rađa dò ngọc, 1044/1211/1212 Sách tuyệt kỹ 1, 1278–1280 Sách tuyệt kỹ 2 | 10 (đệ tử: 8) | Mặc, rồi **rơi xuống** xử lý theo id (thiếu `break`) |
| 27 | 503 | Vật phẩm hỗ trợ/sự kiện tổng hợp; **pet đi theo** (linh thú) cũng type 27 | 193 Gói 10 Capsule, 457 Thỏi vàng, 570 Rương Gỗ, 892 Thỏ xám | 7 | Xử lý theo id |
| 28 | 14 | Cờ PK | 363 Tháo cờ, 364–371 Cờ màu, 519, 520, 747, 960, 961 | — | — |
| 29 | 77 | Item dùng theo thời gian (bổ trợ, thức ăn) | 381 Cuồng nộ, 382 Bổ huyết, 663 Bánh Pudding, 1776 Hộp quà sự kiện | — | Xử lý theo id |
| 30 | 25 | Sao pha lê | 441–447, 1416–1422, 1426–1434 | — | — |
| 31 | 9 | Bánh Trung Thu / Tết | 465, 466, 472, 473, 752, 753, 1306–1308 | — | — |
| 32 | 8 | Giáp tập luyện | 529–531, 534–536, 1716 | 6 | Mặc |
| 33 | 21 | Thẻ sưu tầm (mảnh quái) | 828 Mảnh Khủng long … 1793 Mảnh khỉ Oorazu 2 | — | `UseCard` (xem tài liệu 07 mục Radar) |
| 34 | 1 | Hồng ngọc (nhặt) | 861 | — | Cộng vào `ruby` |
| 36 | 21 | Danh hiệu | 1286 Kẻ thao túng sói … 1673 Tay nhanh hơn não | — | Qua shop `SANTA_DANH_HIEU` |
| 37 | 25 | Sách kỹ năng (mới, không cấp) | 1319 Sách đấm Dragon … 1343 Sách Cađíc liên hoàn chưởng | — | — |
| 75 | 158 | Item sự kiện đời mới + **~140 dòng trống** (id 1826–1999 tên rỗng) | 1787 Vé riêng tư, 1795 Bình hút năng lượng, 1822 Rada ngọc rồng | — | Xử lý theo id |

> TYPE 18–21, 26, 35, 38–74 không có item nào trong DB. `ConsignShopService.isKyGui` có nhắc type 21 và 72 nhưng DB không có (hàm này cũng không được gọi).

---

## 4. Ô trang bị trên người (itemsBody) & điều kiện mặc

### 4.1 Chỉ số ô (`InventoryService.putItemBody` dòng 289–395)

| Ô | TYPE được mặc | Ghi chú |
|---|---|---|
| 0 | 0 – Áo | |
| 1 | 1 – Quần | |
| 2 | 2 – Găng | |
| 3 | 3 – Giày | |
| 4 | 4 – Rada/Nhẫn | |
| 5 | 5 – Cải trang | |
| 6 | 32 – Giáp tập luyện | |
| 7 | 27 – Pet đi theo | Tháo ra ⇒ huỷ `newPet` (dòng 418–424) |
| 8 | 11 – Đeo lưng | Hiển thị `template.part` làm flag bag |
| 9 | 23, 24 – Thú cưỡi | |
| 10 | 25 – Sách tuyệt kỹ (người) | Đệ tử: ô 8 |
| 12 | (pet follow) | Tháo ô 12 gửi `sendPetFollow(0)` (dòng 415) |

Nếu `itemsBody.size()` nhỏ hơn index cần, code tự thêm ô trống (dòng 382–389). Khi load nếu body có đúng 10 ô sẽ thêm ô thứ 11 (`MrBlue.java` dòng 370).

### 4.2 Điều kiện mặc

| Kiểm tra | Dòng | Thông báo |
|---|---|---|
| TYPE thuộc {0,1,2,3,4,5,32,23,24,11,27,25} | 297–304 | "Trang bị không phù hợp!1" |
| `template.gender < 3` phải trùng `player.gender` | 307–310 | "Trang bị không phù hợp!" |
| Item 691/692/693: nếu đang mặc cả áo (ô 0) và cải trang (ô 5) | 313–319 | "Vui lòng cởi áo để có thể sử dụng!" |
| Sức mạnh ≥ yêu cầu. Nếu item có **option 21** thì yêu cầu = `param × 1.000.000.000` | 322–332 | "Sức mạnh không đủ yêu cầu!" |
| Đệ tử mặc type 11 / 25: đệ phải `type` 2, 3 hoặc 4 (đệ VIP) | 362–369 | "Chỉ đệ tử vip mới sử dụng được vật phẩm này!" |
| Đệ tử không được mặc type 23, 24, 27 | 371–378 | "Đệ tử không thể sử dụng vật phẩm này!" |
| Mặc cho đệ: sức mạnh đệ ≥ **1.500.000** | 439–455 | "Đệ tử phải đạt 1tr5 sức mạnh mới có thể mặc" |

Ngay trước khi mặc, `handleOption210` và `checkOption231` được gọi (mục 5.3, 5.4).

### 4.3 Thao tác chuyển item (gói `-40`, `UseItem.getItem` dòng 91–141)

| type | Hằng | Hàm |
|---|---|---|
| 0 | `ITEM_BOX_TO_BODY_OR_BAG` | `itemBoxToBodyOrBag` – nếu là đồ type 0–5/32 và ô tương ứng trống, đủ SM & đúng hành tinh ⇒ mặc thẳng; ngược lại chuyển vào túi |
| 1 | `ITEM_BAG_TO_BOX` | `itemBagToBox` – **cấm cất Thỏi vàng (457)** vào rương |
| 3 | `ITEM_BODY_TO_BOX` | `itemBodyToBox` (điều kiện `index < 0 \|\| index >= size` bị viết ngược ⇒ gần như không bao giờ chạy, xem Ghi chú) |
| 4 | `ITEM_BAG_TO_BODY` | `itemBagToBody` |
| 5 | `ITEM_BODY_TO_BAG` | `itemBodyToBag` |
| 6 | `ITEM_BAG_TO_PET_BODY` | `itemBagToPetBody` |
| 7 | `ITEM_BODY_PET_TO_BAG` | `itemPetBodyToBag` |

Mọi thao tác đều **huỷ giao dịch đang mở** (`TransactionService.cancelTrade`) và bị chặn nếu người chơi đang giao dịch (`Controller.java` dòng 525–531). Sau thao tác: `setClothes.setup()` (tính lại set), `sendFlagBag`, `point`, `sendSpeedPlayer`.

---

## 5. Option (chỉ số) quan trọng

Bảng đầy đủ 251 option: xem [02b](02b-database-du-lieu-template.md#2-item_option_template--danh-sách-chỉ-số-option-vật-phẩm). Dưới đây là các option có **logic đặc biệt trong code**.

### 5.1 Option chỉ số cộng thẳng (áp dụng trong `NPoint` dòng ~474–650 cho đồ đang mặc; dòng 252–411 cho thẻ radar)

| id | Tên (`item_option_template`) | Ghi chú |
|---|---|---|
| 0 | Tấn công+# | sức đánh cộng |
| 2 | HP, KI+#000 | ×1000; dùng cho **đậu thần** cấp ≥3 |
| 5 | +#% sức đánh chí mạng | |
| 6 / 7 | HP+# / KI+# | |
| 14 | Chí mạng+#% | |
| 22 / 23 | HP+#K / KI+#K | ×1000 |
| 27 / 28 | +# HP/30s, +# KI/30s | |
| 47 | Giáp+# | |
| 48 | HP, KI+# | dùng cho **đậu thần** cấp 1–2 |
| 50 | Sức đánh+#% | |
| 77 / 103 | HP+#% / KI +#% | |
| 80 / 81 | HP+#%/30s, KI+#%/30s | |
| 94 | Giảm #% sát thương | |
| 95 / 96 | Biến #% tấn công thành HP/KI | |
| 97 | Phản #% sát thương | |
| 98 / 99 | Xuyên giáp chưởng / cận chiến | |
| 100 | +#% vàng từ quái | |
| 101 | +#% tiềm năng, sức mạnh | |
| 108 | #% Né đòn | |
| 147 | +#% sức đánh | |
| 16, 114, 148 | tốc độ chạy | |

### 5.2 Option điều khiển / trạng thái

| id | Tên | Logic trong code |
|---|---|---|
| **1** | Thời gian sử dụng # phút | Option "tăng dần": khi add item cùng template vào kho, **cộng dồn param** thay vì cộng quantity (`InventoryService.isItemIncrementalOption` dòng 907–917). Dùng cho Tự động luyện tập (521). |
| **9** | Hiệu lực trong # phút | Giáp tập luyện (param 0 trong shop). |
| **21** | Yêu cầu sức mạnh # tỉ | Ghi đè `power_require` khi mặc: `param × 1e9` (InventoryService dòng 323–328, 486–491). Đồ thần linh rơi ra có 21 = 15–17; đồ thiên sứ 21 = 30. SetClothes đếm `setDHD` khi option 21 có param 80. |
| **30** | Không thể giao dịch | Chặn giao dịch (`Trade.isItemCannotTran` dòng 221–224) và chặn ký gửi (`ConsignShopService.KiGui` dòng 386–392). **Chỉ kiểm tra id, không kiểm tra param** ⇒ `ItemOption(30, 0)` vẫn là không giao dịch được. |
| **31** | Số lượng # | Option "tăng dần" như id 1 (cộng param khi gộp). |
| **63 / 64 / 65** | Còn lại # ngày/giờ/phút | Chỉ để hiển thị thời gian bùa còn lại trong shop bùa (`ShopService.resolveShopBua` dòng 155–164). |
| **66** | Chưa có | Hiển thị cho bùa chưa mua. |
| **72** | Cấp # | Cấp nâng cấp của trang bị (+1…+8), cấp rương gỗ (570), cấp bông tai. Bị ẩn khi hiển thị (`getOptionInfo`). Chi tiết nâng cấp: tài liệu nâng cấp đồ. |
| **73** | (rỗng) | Option "giữ chỗ" – tự thêm khi item không có option nào (`addItemList` dòng 822–824). Dùng như cờ "vĩnh viễn" trong một số hộp quà. |
| **76** | Vip | Chỉ gắn cho cải trang 680 trong `caitrang2011()`; không có logic đọc. |
| **86 / 87** | Ký gửi vàng / Ký gửi ngọc | Cho phép đưa lên **ký gửi** (`itemCanConsign`). Đồ thần linh rơi từ quái/boss có ~21%/30% cơ hội mang 86 hoặc 87 (mục 9.2). |
| **93** | Hạn sử dụng # ngày | Xem 5.3. Không bán được item có 93 > 0 (`ShopService.sellItem` dòng 1085–1088). |
| **102** | # Sao Pha Lê | Số sao pha lê **đã ép** vào đồ (`EpSaoTrangBi`). Ẩn khi hiển thị. |
| **107** | # Sao Pha Lê | Số **lỗ** pha lê hoá của đồ (`PhaLeHoaTrangBi` dòng 185). Ẩn khi hiển thị. |
| 127–135, 233, 237, 241, 245 | Set … | Set kích hoạt (mục 9.5). |
| 136–144, 234, 238–240, 242–244, 246–248 | $(5 món …) | Dòng mô tả hiệu ứng set. |
| **154** | Không thể bán lại | Được gắn cho một số đồ (RewardService, boss sự kiện, shop) nhưng **không có chỗ nào kiểm tra** khi bán. |
| **206 / 207** | Vật phẩm hiếm rơi từ quái / boss (+#%) | Gắn cho đồ thần linh khi tỉ lệ random > 100 (mục 9.2). |
| **209** | Bị rớt cấp # lần | Dùng trong nâng cấp/chuyển hoá. |
| **210** | # Dòng chỉ số ẩn | Khi **mặc lần đầu** được thay bằng N option ngẫu nhiên (5.4). |
| 211 / 212 / 219 | Giám định #/5, Độ bền #/1000, Số lần tẩy | Sách tuyệt kỹ (combine). |
| **213** | Giá bán: # triệu vàng | Khi gửi client: param > 1.000.000 đổi thành option 223 (param/1.000.000), > 1000 đổi thành 222 (param/1000) (`sendItemBags` dòng 580–591). |
| 220 | Hoàn thành #% | Tiến độ danh hiệu trong shop danh hiệu. |
| 228 | Cường hoá tới ô sao pha lê # | Combine. |
| **231** | Hạn sử dụng hoặc vĩnh viễn | Khi mặc: xoá option, 99% thêm HSD 3/7/15/21 ngày (random đều), 1% vĩnh viễn (5.4). |
| 236 | +#% May mắn | Tăng tỉ lệ rơi đồ SKH trong `Mob.java` (dòng 807, 854). |
| **250** | # Kilis | Bình hút năng lượng (1795) cần ≥ 3000 để mở. |

### 5.3 Hạn sử dụng (option 93)

`ItemService.isOutOfDateTime` (dòng 405–422):

```
dayPass = số ngày giữa now và item.createTime
nếu dayPass != 0: param -= dayPass; nếu param <= 0 ⇒ hết hạn; ngược lại createTime = now
```

- Chỉ được gọi **khi load dữ liệu** (đăng nhập: body, bag, box, đồ đã bán, body đệ tử – `MrBlue.java` dòng 362, 390, 429, 487, 863) và trong các manager Top. Không có tiến trình kiểm tra định kỳ khi đang online.
- Item hết hạn bị thay bằng ô trống (xoá hẳn).
- `param = 0` (nhiều hộp quà có ~1% ra HSD 0) ⇒ bị xoá ngay lần đăng nhập đầu tiên sau khi qua ≥1 ngày.

### 5.4 Option ngẫu nhiên khi mặc

`InventoryService.handleOption210` (dòng 1149–1181): xoá option 210 (param = N), chọn N option **khác nhau** từ `{8, 14, 108, 94, 108, 16, 80, 81, 97, 100, 101, 104, 106}`:

| Option | param |
|---|---|
| 8, 14, 108, 94 | 3–5 |
| 16, 80, 81, 97, 100, 101, 104 | 10–25 |
| 106 | 0 |

`checkOption231` (dòng 1197–1211): xoá 231; `Math.random() <= 0.99` ⇒ thêm 93 với param ∈ {3, 7, 15, 21}; còn lại (1%) không có hạn.

### 5.5 Option của đá & sao pha lê khi ép (`Item.getOptionDaPhaLe` dòng 227–291)

| Item | Option thêm |
|---|---|
| 14 Ngọc Rồng 1 sao | 108 Né đòn +2 |
| 15 NR 2 sao | 94 Giảm sát thương +2 |
| 16 NR 3 sao | 50 Sức đánh +3% |
| 17 NR 4 sao | 81 KI +5%/30s |
| 18 NR 5 sao | 80 HP +5%/30s |
| 19 NR 6 sao | 103 KI +5% |
| 20 NR 7 sao | 77 HP +5% |
| 441 / 1416 / 1426 Sao pha lê đỏ | 95 Biến 5% tấn công thành HP |
| 442 / 1417 / 1427 | 96 Biến 5% tấn công thành KI |
| 443 / 1418 / 1428 | 97 Phản 5% sát thương |
| 444 / 1419 / 1429 | 98 Xuyên giáp chưởng 5% |
| 445 / 1420 / 1430 | 99 Xuyên giáp cận chiến 5% |
| 446 / 1421 / 1431 | 100 +5% vàng từ quái |
| 447 / 1422 / 1432 | 101 +5% TN, SM |
| 1433 | 153 5% phát nổ sau khi chết |
| 1434 | 160 +5% TN, SM cho đệ tử |
| khác | option đầu tiên của item |

---

## 6. Hành trang, rương đồ: giới hạn, xếp chồng, mở rộng

### 6.1 Giới hạn (`models/player/Inventory.java` dòng 20–22)

| Hằng | Giá trị |
|---|---|
| `MAX_ITEMS_BAG` | **80** ô hành trang |
| `MAX_ITEMS_BOX` | **100** ô rương đồ |
| `LIMIT_GOLD` | **200.000.000.000** vàng |
| Body | 11 ô (người), tự nới thêm khi cần |
| Đồ đã bán (mua lại) | tối đa **10** item (`BuyBackService.MAX_ITEM_IN_BOX`), load tối đa 20 |

### 6.2 Mở rộng ô (`InventoryService.addItemBag` dòng 781–800)

| Item | Hiệu ứng | Nguồn bán |
|---|---|---|
| **517** Mở rộng hành trang | +1 ô túi nếu `itemsBag.size() < 80`, ngược lại báo "Hành trang của bạn đã đạt tối đa" và **không** thêm | Shop `SANTA_MO_RONG_HANH_TRANG`: 100 ngọc xanh |
| **518** Mở rộng rương đồ | +1 ô rương nếu `< 100` | cùng shop: 50.000.000 vàng |

Item được xử lý ngay khi *thêm vào túi* (tức ngay lúc mua), không nằm lại trong túi. Riêng item 1627 ("TT") trong code có giá động `((size−35)+1)×2` khi size ≥ 35 (`ShopService.opendShop` dòng 71–79) nhưng không có trong shop nào ở DB.

### 6.3 Thêm item vào kho (`addItemBag` dòng 735–815 → `addItemList` dòng 821–881)

Thứ tự xử lý:

1. **Ngọc rồng đen** 372–378 → `BlackBallWarService.pickBlackBall`.
2. **Ngọc rồng Namếc** 353–360 và **Hoá thạch** 362 → `NgocRongNamecService.pickNamekBall`.
3. `addItemSpecial` (dòng 704–733):
   - TYPE 13 (bùa) → cộng thời gian bùa theo shop đang mở: `BUA_1H` 60 phút, `BUA_8H` 480 phút, `BUA_1M` 43.200 phút (30 ngày); shop khác = 0 phút.
   - 453 Chiến thuyền Tennis → `haveTennisSpaceShip = true`.
   - 74 Đùi gà nướng → hồi đầy HP/KI.
4. TYPE 9 vàng: cộng nếu `gold + qty <= LIMIT_GOLD` (khi đang **Chibi typeChibi 0** cộng **gấp đôi**), ngược lại "Vàng sau khi nhặt quá giới hạn cho phép". TYPE 10 ngọc / TYPE 34 hồng ngọc: cộng, kẹp tối đa `Integer.MAX_VALUE`.
5. 517 / 518 mở rộng (6.2).
6. 1765–1771 Bé Rồng Cute không có option 93 ⇒ cập nhật nhiệm vụ danh hiệu `ME_RONG`.
7. `addItemList`:
   - Không option ⇒ thêm option 73.
   - Có option 1 hoặc 31 ⇒ tìm item cùng template, **cộng param** option đó, xong.
   - `is_up_to_up = 1` ⇒ gộp vào item cùng template **và cùng danh sách option (id+param, đúng thứ tự)**; ngoại lệ không cần khớp option: 2074, đá nâng cấp 1074–1078, mảnh thiên sứ 1066–1070. Bỏ qua ô đã ≥ 100.000.000.
     - Gộp **không giới hạn** (cộng thẳng) với: 1066–1070, 457 Thỏi vàng, 610, TYPE 14 (đá 220–224), 2048, 2050–2055, 821 Vé quay ngọc vàng, 2075.
     - Các item khác: tối đa **99.999** mỗi ô, phần dư tràn sang ô mới.
   - Còn dư ⇒ đặt vào ô trống đầu tiên; hết ô ⇒ trả về `false`.

### 6.4 Vứt / xoá / sắp xếp

- Vứt (gói `-43` type 1→2): không được vứt ở map 21, 22, 23 (nhà); không vứt được **Rương Gỗ 570**; **Thỏi vàng 457** không vứt được (trả thông báo tục tĩu – `InventoryService.throwItem` dòng 129–135). Item bị xoá hẳn, không rơi ra đất.
- `sortItems` dồn item lên đầu mỗi lần gửi túi (`sendItemBags`).
- `subQuantityItem` trừ số lượng, ≤ 0 thì xoá ô.

---

## 7. Tiền tệ & giới hạn

| Tiền | Trường | Kiểu / giới hạn | Nguồn chính | Item vật lý |
|---|---|---|---|---|
| Vàng | `inventory.gold` | `long`, tối đa **200 tỷ** (`LIMIT_GOLD`; khi lưu DB cũng bị kẹp – `PlayerDAO` dòng 359) | nhặt từ quái, bán đồ, giao dịch | 76, 188, 189, 190 (type 9) |
| Ngọc xanh | `inventory.gem` | `int`, kẹp `Integer.MAX_VALUE` khi nhặt | nạp, nhặt, quà | 77 (type 10) |
| Hồng ngọc | `inventory.ruby` | `int`, kẹp `Integer.MAX_VALUE` khi nhặt | nạp/quà | 861 (type 34) |
| Coupon / điểm | `inventory.coupon` | `int` | — | — (typeSell 4 trong shop) |
| Thỏi vàng | item **457** | xếp chồng không giới hạn; bán cho NPC shop được **37.000.000 vàng/thỏi** (`Input.BANSLL` dòng 495–520) | mua bán ký gửi, shop VIP | 457 (type 27, `gold` template 500.000.000) |
| Điểm sự kiện | `player.event.getEventPoint()` | — | sự kiện | — |
| Điểm Capsule bang | `clan.capsuleClan` | — | điểm danh bang | — |

Chi tiết nạp & VIP: [19-vip-nap-tien-tien-te.md](19-vip-nap-tien-tien-te.md).

---

## 8. Ngọc rồng

| Nhóm | id | TYPE | Cách dùng |
|---|---|---|---|
| Ngọc Rồng thường 1–7 sao | 14–20 | 12 | Dùng 1/2/3 sao (14/15/16) ⇒ `SummonDragon.openMenuSummonShenron(pl, tempId − 13)`; dùng 4–7 sao ⇒ bảng hướng dẫn "Bạn chỉ có thể gọi rồng từ ngọc 3 sao, 2 sao, 1 sao" (`UseItem.controllerCallRongThan` dòng 1706–1723). Cũng dùng để ép vào đồ (5.5) và ký gửi được. |
| Ngọc rồng băng 1–7 sao | 925–931 | 12 | `Shenron_Service.openMenuSummonShenron(pl, 0)` |
| Ngọc Rồng Namếc 1–7 sao, Ngọc Rồng Namek | 353–360 | 11 | Nhặt ⇒ `NgocRongNamecService.pickNamekBall`; gọi rồng tại NPC Dende map 7 |
| Hoá thạch Ngọc Rồng | 362 | 11 | như trên |
| Ngọc rồng sao đen 1–7 | 372–378 | 11 | Nhặt ⇒ `BlackBallWarService.pickBlackBall` (sự kiện NR sao đen) |
| Bí ngô 3 sao | 704 | 12 | Không có xử lý trong `controllerCallRongThan` (ngoài 2 khoảng id trên) |

Chi tiết điều ước: tài liệu Rồng thần.

---

## 9. Sinh vật phẩm có chỉ số ngẫu nhiên

> Quy ước random: `Util.nextInt(a, b)` = **[a, b]** gồm cả hai đầu; `Util.nextInt(n)` = [0, n); `Util.isTrue(x, y)` = xác suất x/y (`utils/Util.java` dòng 169–219). Các `if/else if (isTrue(...))` lồng nhau cho xác suất **có điều kiện**, đã quy đổi ra xác suất thực trong bảng.

### 9.1 Chỉ số gốc

- Đồ bán trong shop: option lấy từ `item_shop_option` (xem bảng shop trong [07](07-shop-giao-dich-ky-gui.md)).
- `RewardService.initChiSoItem(item)` (dòng 215) → `SetClothes(tempId, type, options)`: gán chỉ số gốc cố định theo id cho đồ type 0–4 (vd. áo 0 Giáp 2, 33 Giáp 4, 3 Giáp 8, 136 Giáp 24, 230 Giáp 200…). Cải trang (type 5) không được thêm gì.

### 9.2 Đồ Thần Linh rơi ra – `ItemService.randDoTL` (quái, dòng 827–954) & `randDoTLBoss` (boss, dòng 956–1086)

**Chọn món** (điều kiện lồng nhau):

| Món | Điều kiện | Xác suất thực |
|---|---|---|
| Nhẫn 561 | 10% | 10% |
| Găng (562/564/566 đều) | 25% của phần còn lại | 22,5% |
| Quần (556/558/560) | 45% còn lại | 30,375% |
| Áo (555/557/559) | 75% còn lại | 27,84% |
| Giày (563/565/567) | phần còn lại | 9,28% |

> Hành tinh của món được chọn **ngẫu nhiên**, không theo hành tinh người nhặt.

**Chỉ số**: `tiLe = nextInt(100, 115)`.

| id | Món | Chỉ số |
|---|---|---|
| 555 | Áo TL TĐ | Giáp (47) = 800 × tiLe/100 |
| 557 | Áo TL NM | Giáp = 850 × tiLe/100 |
| 559 | Áo TL XD | Giáp = 900 × tiLe/100 |
| 556 | Quần TL TĐ | chiso = 52000×tiLe/100 ⇒ HP (22) = chiso/1000 K; +HP/30s (27) = chiso/20 |
| 558 | Quần TL NM | chiso = 50000×tiLe/100 (như trên) |
| 560 | Quần TL XD | chiso = 48000×tiLe/100 |
| 562 / 564 / 566 | Găng TL TĐ/NM/XD | Tấn công (0) = 4400 / 4300 / 4500 × tiLe/100 |
| 563 / 565 | Giày TL TĐ/NM | chiso = 48000 / 50000 ×tiLe/100 ⇒ KI (23) = chiso/1000 K; +KI/30s (28) = chiso/20 |
| 567 | Giày TL XD | chiso = 46000×tiLe/100 ⇒ KI = chiso/1000 K; option 28 = **chiso×150/1000** (khác công thức 2 hành tinh còn lại) |
| 561 | Nhẫn TL | Chí mạng (14) = 14×tiLe/100 (14–16%) |

- Nếu `tiLe > 100` (trừ nhẫn): thêm **206** (quái) hoặc **207** (boss) với param = tiLe − 100 (1–15).
- Option ký gửi: quái `isTrue(30)` rồi `isTrue(70)` ⇒ **21%** có 86 hoặc 87 (50/50); boss **30%**.
- Luôn thêm **21** (yêu cầu SM) = 15–17 tỉ.
- Boss gọi `randDoTLBoss`: Majin Buu 12h (BuiBui, Cadic, Mabu, Goku, Drabura, Yacon…), Cumber, Baby, Black Goku, Siêu Bọ Hung, Cooler. Quái gọi `randDoTL`: `Mob.java` dòng 985, `Hirudegarn` dòng 111.
- Hộp thần linh (1775) dùng `initChiSoItem` (chỉ số cố định) chứ không dùng hàm này (mục 10.7).

### 9.3 Đồ Hủy Diệt (650–662)

Không có hàm random riêng; mua tại shop `BILL` (NPC Bill, id 55) qua `ShopService.buyItemHD` (dòng 1217–1285):

| Bước | Chi tiết |
|---|---|
| Mở shop | `Bill.java` dòng 101: chỉ khi `InventoryService.canOpenBillShop` = mặc đủ **5 món level 13** (Thần Linh) ở ô 0–4 **và** có ≥ **99** một loại thức ăn 663–667 |
| Giá | Áo 800.000.000; Quần 1.000.000.000; Găng 2.000.000.000; Giầy 720.000.000; Nhẫn 2.000.000.000 vàng |
| Chỉ số gốc (DB) | Găng TĐ/NM/XD: Tấn công 8800/8600/9000; Quần HP 104K/100K/96K; Áo Giáp 1600/1700/1800; Giầy KI 96K/100K/92K; Nhẫn Chí mạng 16% |
| Tiêu hao thêm | 99 thức ăn (item level 14) |
| Điều kiện phụ | Trên người phải có ít nhất 1 món level 13 |
| Bonus ngẫu nhiên | `r = nextInt(1,100)`: r ≤ 1 ⇒ +15%; ≤ 15 ⇒ +11–14%; ≤ 35 ⇒ +7–10%; ≤ 60 ⇒ +4–6%; còn lại ⇒ +0–3%. Chỉ áp cho option 0, 22, 23, 14, 27, 28, 47 (`optionCanUpgrade`) và khi bonus > 0 |
| Kết quả | Bỏ option 164; thêm **30** (không giao dịch) |

Set hủy diệt đủ 5 món (`SetClothes.checkSetDes` dòng 221, id 650–662 ở ô 0–4) dùng làm điều kiện Whis học tuyệt kỹ và tăng rơi đồ tại map Ngục Tù (`Mob.java` dòng 1034–1039).

### 9.4 Đồ Thiên Sứ – `ItemService.DoThienSu(itemId, gender)` (dòng 1088–1135)

Gọi từ `combine/CheTaoTrangBiThienSu.java` dòng 79, 160. `Util.highlightsItem(true, v)` = v × 1,1 (hành tinh "hợp" được +10%).

| Món | id | Chỉ số | Hành tinh +10% |
|---|---|---|---|
| Áo | 1048/1049/1050 | Giáp (47) = 2800–4000 | Xayda (gender 2) |
| Quần | 1051–1053 | 80%: HP (22) 120–130K; 20%: 130–150K | Trái Đất |
| Găng | 1054–1056 | 80%: Tấn công (0) 10.350–11.000; 20%: 10.500–11.500 | Xayda |
| Giày | 1057–1059 | 80%: KI (23) 90–110K; 20%: 110–130K | Namếc |
| Nhẫn | 1060–1062 | Chí mạng (14) 18–20% | Namếc |

Luôn thêm **21 = 30** (yêu cầu 30 tỉ SM) và **30** (không giao dịch). Lưu ý tham số `gender` là `template.gender` của **công thức/đồ nguyên liệu**, không phải người chơi.

### 9.5 Đồ Set Kích Hoạt (SKH)

#### 9.5.1 Danh sách set theo hành tinh

| Hành tinh | Option set | Tên set | Dòng mô tả gắn kèm | Hiệu ứng thực thi (đủ 5 món trừ khi ghi khác) | Nguồn code hiệu ứng |
|---|---|---|---|---|---|
| Trái Đất | 127 | Set Thên Xin Hăng | 139 (x2 thời gian chói mắt) | Thái Dương Hạ San: thời gian choáng ×2 | `SkillService` dòng 602–606 |
| Trái Đất | 128 | Set Kirin | 140 (+100% QCKK) | Quả cầu kênh khi: sát thương ×2 | `NPoint` dòng 1465 |
| Trái Đất | 129 | Set Sôngôku | 141 (+100% Kamejoko) | Kamejoko: `percentXDame = 100` | `NPoint` dòng 1374 |
| Trái Đất | 245 | Set Thần Vũ Trụ Kaio | 246 [2] tăng chí mạng; 247 [4] giảm hao Kaioken; 248 [5] | ≥1 món: `crit += 10/100` (=0, không tác dụng); 4 món: Kaioken tốn 5% HP (mặc định 10%); 5 món: tốn 3% HP và Kaioken `percentXDame = 30` | `NPoint` 1237, 1429; `SkillService` 398–406 |
| Namếc | 130 | Set Picolo | 142 (+100% Masenko) | Makankosappo: `dameSkill *= 3/2` (phép chia nguyên = ×1 ⇒ **không tăng**) | `NPoint` 1442 |
| Namếc | 131 | Set Ốc tiêu | 143 (+100% Liên hoàn) | Liên hoàn: `percentXDame = 100` | `NPoint` 1423 |
| Namếc | 132 | Set Pikkoro Daimao | 144 (+100% & bất tử đệ trứng) | Đẻ trứng: sát thương ×4; đệ trứng không tự chết & đánh cả khi mục tiêu < 5% HP | `NPoint` 1475; `MobMe` 32, 43 |
| Namếc | 237 | Set Nail chiến binh Namếc | 238 [2], 239 [4], 240 [5] | ≥2 món: `tlDameCrit +10`; 4 món: Masenko giảm hồi chiêu thêm 20; 5 món: Masenko +80% sát thương, giảm hồi chiêu thêm 50 | `NPoint` 1105, 1411; `SkillService` 1170–1182 |
| Xayda | 133 | Set Kakarot | 136 (+100% Galick) | Galick: `percentXDame = 100` | `NPoint` 1383 |
| Xayda | 134 | Set Ca Đíc | 137 (x5 thời gian khỉ) | Thời gian biến khỉ ×5 | `EffectSkillService` 227 |
| Xayda | 135 | Set Nappa | 138 (+80% HP) | HP max +80% | `NPoint` 790 |
| Xayda | 241 | Set Cađic M | 242 [2], 243 [4], 244 [5] | ≥2 món: HP max +20%; đúng 2 món: tầm nổ +200; đúng 4: nổ +20% HP max & `percentXDame 20`; 5: +50% HP max & `percentXDame 50` | `NPoint` 794, 1395; `SkillService` 740–754 |
| Chung 3 hành tinh | 233 | Set Gohan | 234 (+150% may mắn +30% vàng) | **Không tìm thấy xử lý** trong `SetClothes`/`NPoint` | — |

`SetClothes.setupSKT` (dòng 80–~200) duyệt ô 0–4, với mỗi món lấy **option set đầu tiên** gặp được (break) để đếm. Đủ 5 món set TĐ (songoku/kirin/thienXinHang) ⇒ hiệu ứng 1200; NM (ocTieu/pikkoroDaimao/picolo) ⇒ 10277; Thần Vũ Trụ Kaio ⇒ 10277 + 5017; XD (kakarot/nappa/cadic) ⇒ 1202 (`Service.sendEffPlayer` dòng 2065–2085). Set Nail, Cađic M, Gohan không có hiệu ứng hào quang.

#### 9.5.2 `createItemSKH(itemId, skhId)` (dòng 256–267)

Tạo item + option shop của template + `skhId` (param 1) + các option mô tả `getOptionIdsBySKH` + **30** (không giao dịch).

| skhId | 127 | 128 | 129 | 130 | 131 | 132 | 133 | 134 | 135 | 233 | 237 | 241 | 245 |
|---|---|---|---|---|---|---|---|---|---|---|---|---|---|
| option mô tả thêm (`getOptionIdsBySKH`) | 140 | 139 | 141 | 142 | 143 | 144 | 136 | 137 | 138 | 234 | 238,239,240 | 242,243,244 | 246,247,248 |

> Hàm khác `ItemService.ID()` (dòng 1151) lại map 127→139, 128→140 — hai hàm ngược nhau cho Thên Xin Hăng/Kirin (chỉ ảnh hưởng dòng mô tả).

#### 9.5.3 Capsule Kích hoạt 1 món tự chọn (1655) – `ItemService.OpenSKH` (dòng 130–191)

Người chơi chọn Áo/Quần/Găng/Giày/Rada (menu `CAPSULE_KICH_HOAT` = 1655, `NpcFactory` dòng 361–386). Cần ≥ 1 ô trống. Hành tinh = hành tinh người chơi.

| Hành tinh | Áo | Quần | Găng | Giày | Rada (chung) |
|---|---|---|---|---|---|
| TĐ | 0,3,33,34,136–139,230–233 | 6,9,35,36,140–143,242–245 | 21,24,37,38,144–147,254,256,257 | 27,30,39,40,148–151,266–269 | 12,57,58,59,184–187,278–281 |
| NM | 1,4,41,42,152–155,235,236,237 | 7,10,43,44,156–159,246–249 | 22,25,45,46,160–163,259,260,261 | 28,31,47,48,164–167,270–273 | như trên |
| XD | 2,5,49,50,168–171,238–241 | 8,11,51,52,172,173,**174,174**,250–253 | 23,26,53,54,176–179,262–265 | 29,32,55,56,180–183,274–277 | như trên |

Set chọn đều trong 5 option: TĐ {128, 129, 127, 233, 245}; NM {130, 131, 132, 233, 237}; XD {133, 135, 134, 233, 241} ⇒ mỗi set **20%**. (Mảng quần XD lặp 174 và thiếu 175; áo NM thiếu 234; găng TĐ thiếu 255; găng NM thiếu 258.) `OpenVeTangNgoc` (dòng 193–254) là bản sao y hệt nhưng không được gọi.

#### 9.5.4 SKH rơi từ quái (`Mob.java` dòng 827–946 – chi tiết tỉ lệ ở tài liệu rơi đồ)

- `randTempItemKichHoat(gender)` (dòng 703–720): mảng `items` theo index 0 = áo {0,33 / 1,41 / 2,49}, 1 = quần {6,35 / 7,43 / 8,51}, 2 = giày {27,30 / 28,47 / 29,55}, 3 = găng {21,24 / 22,46 / 23,53}, 4 = rada {12,57}. Chọn type lồng nhau: 10% rada; 23% găng; 23% quần; 23% áo; còn lại giày ⇒ xác suất thực **rada 10%, găng 20,7%, quần 15,94%, áo 12,27%, giày 41,09%** (comment trong code ghi lệch tên). Mỗi loại chỉ có 2 món cấp thấp nhất, chọn 50/50.
- `randOptionItemKichHoat(gender)` (dòng 749–825):

| Kết quả | Xác suất thực | TĐ | NM | XD |
|---|---|---|---|---|
| Set đời mới (4 option) | 30% | 245+246,247,248 | 237+238,239,240 | 241+242,243,244 |
| Set 1 | 35% | 128+140 | 130+142 | 134+137 |
| Set 2 | 17,5% | 127+139 | 131+143 | 135+138 |
| Gohan | 8,75% | 233+234 | 233+234 | 233+234 |
| Set 3 | 8,75% | 129+141 | 132+144 | 133+136 |

- `randomSKHId(gender)` (dòng 330–348): 25% option[0], 35% option[1], 40% option[2] (TĐ 128/129/127, NM 130/131/132, XD 133/135/134). Không thấy nơi gọi ngoài ItemService.
- `randTempItemDoSao`, `randDoSao` (dòng 670–747): chọn đồ "sao" theo hành tinh cho rơi đồ.

### 9.6 Các hàm tạo đồ sự kiện trong `ItemService`

| Hàm | Dòng | Item | Option |
|---|---|---|---|
| `caitrang2011(rating)` | 513–523 | 680 Cải trang (Caulifla) | 76 VIP, 77 HP 24%, 103 KI 25%, 147 SĐ 24%; 99,5% thêm HSD 1–3 ngày |
| `caitrangChristmas` | 526–538 | random 386–394 Nón Noel | 77/103/147: 15–20; 95: 1–10; 5: 1–30; 106; 99,5% HSD 1–3 |
| `phuKien2011` | 542–555 | 954 Bó Hoa Hồng | 77/103/147: 5–9; 1% một dòng = 10; 30; 99,5% HSD 1–3 |
| `phuKienChristmas` | 557–570 | 745 Bông tuyết | như trên |
| `vanBay2011` | 572–580 | 795 Ghế bay | 89, 30; 95% HSD 1–3 |
| `vanBayChrimas` | 610–618 | 746 Xe tuần lộc | 89, 30; 95% HSD 1–3 |
| `daBaoVe()` | 582–586 | 987 Đá bảo vệ | 30 |
| `randomRac()` | 588–595 | 20/19/18 (NR 7/6/5 sao; `nextInt(length−1)` bỏ sót 17) | — |
| `randomRac2()` | 597–608 | 1 trong 23 id đầu của {585, 704, 2048, 379, 384, 385, 381, 828–842, 934, 935} (bỏ sót 935); 1% thay bằng 956 | option đá nếu 220–224 |
| `vatphamsk(hsd)` | 637–658 | 1 trong {2025, 2026, 2036–2040, 2019–2024, 954, 955, 952, 953, 924, 860, 742} | 1 option ∈ {77,80,81,103,50,94,5} param 5–15; 1% thêm option ∈ {14,16,17,19,27,28,47,87}; 99,9% HSD 1–7; option 30 param 0. **Các id ≥ 2000 không tồn tại trong DB** |
| `otpts(tempId, qty)` | 89–117 | đồ type 0–4 | 21=80 + chỉ số cố định (áo Giáp 2000–2500, quần HP 150–200K, găng 18.000–20.000, giày KI 150–200K, rada CM 20–25%). Không được gọi |

---

## 10. Sử dụng vật phẩm – `UseItem.java`

### 10.1 Luồng xử lý

Gói `-43` → `Controller.java` dòng 537–547 (chặn nếu đang giao dịch hoặc bật bảo vệ tài khoản) → `UseItem.doItem` (dòng 152–253):

| type | Ý nghĩa | Xử lý |
|---|---|---|
| 0 `DO_USE_ITEM` | Dùng | index ≠ −1: nếu item **type 7** (sách) / **570** Rương gỗ (chỉ khi đã qua 0h kể từ lần mở trước) / **type 22** (vệ tinh, chặn nếu map đã có > 2 vệ tinh) ⇒ gửi hộp thoại xác nhận; còn lại gọi `useItem`. index = −1: đọc thêm `short` id item ⇒ tìm trong túi rồi `useItem` |
| 1 `DO_THROW_ITEM` | Hỏi vứt | cấm map 21–23; cấm 570 |
| 2 `ACCEPT_THROW_ITEM` | Xác nhận vứt | `InventoryService.throwItem` |
| 3 `ACCEPT_USE_ITEM` | Xác nhận dùng | `useItem` |

`useItem(pl, item, indexBag)` (dòng 255–940):
1. **570 Rương Gỗ** xử lý trước (không cần SM).
2. Nếu `template.strRequire > power` ⇒ "Sức mạnh không đủ yêu cầu".
3. `switch (type)`: 33 → `UseCard`; 7 → `learnSkill`; 6 → `eatPea`; 12 → `controllerCallRongThan`; 23/24/11 → mặc; 25 → mặc **rồi rơi xuống default**; default → `switch (template.id)`.
4. Sau cùng: `TaskService.checkDoneTaskUseItem`, `sendItemBags`.

### 10.2 Đậu thần (type 6) – `eatPea` (dòng 1857–1901)

- Cooldown **1 giây** giữa 2 lần ăn.
- Ăn **hạt đậu đầu tiên** tìm thấy trong túi (không phải hạt được bấm).
- Lượng hồi = option 2 × 1000 hoặc option 48; cộng vào **cả HP và KI** của người chơi.
- Nếu có đệ tử cùng map & còn sống: đệ hồi cùng lượng HP/KI + thể lực `100 × cấp đậu` (cấp lấy từ tên, `substring(13)`), đệ chat "Cám ơn sư phụ".

Bảng hồi theo cấp (`MagicTree.PEA_TEMP`, `PEA_PARAM` dòng 19–20; thu hoạch `addPeaHarvest` dòng 306–317):

| Cấp | id | Option | Hồi HP & KI | Thể lực đệ |
|---|---|---|---|---|
| 1 | 13 | 48 = 100 | 100 | +100 |
| 2 | 60 | 48 = 500 | 500 | +200 |
| 3 | 61 | 2 = 2 | 2.000 | +300 |
| 4 | 62 | 2 = 4 | 4.000 | +400 |
| 5 | 63 | 2 = 8 | 8.000 | +500 |
| 6 | 64 | 2 = 16 | 16.000 | +600 |
| 7 | 65 | 2 = 32 | 32.000 | +700 |
| 8 | 352 | 2 = 64 | 64.000 | +800 |
| 9 | 523 | 2 = 128 | 128.000 | +900 |
| 10 | 595 | 2 = 256 | 256.000 | +1.000 |

Gói 30 đậu (293–299, 596, 597) chỉ là item bán trong shop: khi mua được đổi thành **30 hạt** (`ShopService.buyMagicPean` dòng 1287–1297) – xem tài liệu 07.

### 10.3 Sách kỹ năng (type 7) – `learnSkill` (dòng 1725–1771)

| Điều kiện | Kết quả |
|---|---|
| `template.gender` ≠ hành tinh người chơi và ≠ 3 | "Không thể thực hiện" |
| Cấp sách = **ký tự cuối** của tên (vd. "lv3" → 3) | |
| Skill đã cấp 7 | "Kỹ năng đã đạt tối đa!" |
| Chưa học & sách lv1 | Học mới, trừ 1 sách, gửi sub-command 23 |
| Chưa học & sách lv > 1 | "Vui lòng học … cấp 1 trước!" |
| Đã học cấp N & sách cấp N+1 | Nâng cấp, thêm vào `BoughtSkill`, trừ 1 sách, sub-command 62 |
| Khác | "Vui lòng học … cấp N+1 trước!" |

### 10.4 Vật phẩm dùng theo thời gian – `useItemTime` (dòng 1520–1704)

Sau khi bật: `Service.point`, `sendAllItemTime`, **trừ 1 item** (trừ trường hợp bị chặn và `return` sớm). Thời hạn hết trong `ItemTime.update()` (dòng 118–279).

| id | Tên | Thời gian | Hiệu ứng thực thi | Không dùng chung với |
|---|---|---|---|---|
| 381 | Cuồng nộ | 10 phút (`TIME_ITEM`=600.000 ms) | Sức đánh ×2 (`NPoint` dòng 1120) | 1150 |
| 1150 | Cuồng nộ 2 | 10 phút | Sức đánh ×2,2 | 381 |
| 382 | Bổ huyết | 10 phút | HP max ×2 (dòng 869) | 1152 |
| 1152 | Bổ huyết 2 | 10 phút | HP max ×2,2 | 382 |
| 383 | Bổ khí | 10 phút | KI max ×2 (dòng 1003) | 1151 |
| 1151 | Bổ khí 2 | 10 phút | KI max ×2,2 | 383 |
| 384 | Giáp Xên bọ hung | 10 phút | Sát thương nhận /2 (`Player.java` dòng 1164) | 1153 |
| 1153 | Giáp Xên bọ hung 2 | 10 phút | Sát thương nhận còn 40% | 384 |
| 385 | Ẩn danh | 10 phút (dùng lại chỉ đặt lại mốc, không cộng dồn) | Hạ địch không bị thêm vào danh sách kẻ thù (`FriendAndEnemyService` 279, `Player` 1294) | — |
| 1154 | Ẩn danh 2 | 10 phút | cờ `isUseAnDanh2` (không thấy nơi đọc ngoài hiển thị) | — |
| 379 | Máy dò Capsule kì bí | 30 phút | Quái tempId 58–65: 20% rơi 380 Viên Capsule kì bí (`Mob.java` 614) | — |
| 1635 | Cỏ bốn lá | 30 phút theo mô tả | Map bông tai: +5/+3/+1% rơi mảnh; +15% vàng & một số tỉ lệ (`Mob.java` 634–1060). **`timeLengthCoBonLa` không được gán ⇒ hết hiệu lực ngay lần update kế tiếp** | — |
| 1614 | Ly mía khổng lồ | 10 phút | HP max +10% | 1615, 1616 |
| 1615 | Ly mía thơm | 10 phút | HP max +10%, chí mạng +10 | 1614, 1616 |
| 1616 | Ly mía sầu riêng | 10 phút | HP +10%, sức đánh +10%, giáp +10%, `tlDameCrit` +10, SĐCM +10 | 1614, 1615 |
| 638 | Bình chứa Commeson | 60 phút (`TIME_CMS`) | Sát thương từ quái còn 10% (`Mob.java` 420) | — |
| 579, 1045 | Đuôi khỉ | 30 phút (`TIME_DK`) | Tiềm năng nhận thêm `tn × 2` | — |
| 663–667 | Bánh Pudding, Xúc xích, Kem dâu, Mì ly, Sushi | 10 phút (`TIME_EAT_MEAL`) | Sức đánh +10% (áp cho cả đệ); dùng món khác thì thay icon | — |
| 880 | Cua rang me | 10 phút | Sức đánh +5% (icon 8060) | chỉ 1 món trong 880–882 (`isEatMeal2` ⇒ "Chỉ được sử dụng 1 cái") |
| 881 | Bạch tuộc nướng | 10 phút | `tlDameCrit` +5, SĐCM +5 (icon 8061) | như trên |
| 882 | Tôm tẩm bột chiên xù | 10 phút | HP max +5% (icon 8062) | như trên |
| 1233 | Nồi cơm điện | hiển thị 30 phút (`TIME_NCD`) | `NewSkill.typeItem = 2` (tuyệt kỹ Ma phong ba). **Không có đoạn tắt trong `update()`** | — |
| 764 | Khẩu trang | — | Chỉ bật cờ `isUseKhauTrang`; không có nơi đọc, không tự tắt | — |
| 1532 | Rađa kho báu | 30 phút (`TIME_MAY_DO2`; mô tả ghi 15 phút) | Map 135–138 (Bản đồ kho báu): TN nhận thêm `tn × 2` | — |
| 1628 | Bùa x2 tn,sm đệ tử | 30 phút, **cộng dồn** +30 phút mỗi lần | Đệ tử nhận thêm `tn × 2` | — |

### 10.5 Tự động luyện tập (521) – `useTDLT` (dòng 1773–1779) → `ItemTimeService.turnOnTDLT/turnOffTDLT` (dòng 130–168)

- Bấm lần 1: bật; option 1 (phút) bị trừ tối đa **500 phút** (30.000 giây) mỗi lần bật; `timeTDLT = min(phút dùng×60, 30000) s`.
- Bấm lần 2: tắt, hoàn lại phần phút chưa dùng vào option 1.
- Item không bị xoá khi bật. Nhiều item 521 gộp bằng cách cộng option 1.

### 10.6 Bùa (type 13)

Bùa **không dùng từ túi**: khi mua ở shop bùa, `addItemSpecial` cộng thời gian ngay (6.3). `Charms.addTimeCharms` (dòng 35+) cộng dồn: nếu bùa đã hết hạn thì tính từ hiện tại.

| id | Tên | Mô tả (DB) | Giá 1 giờ / 8 giờ / 1 tháng (ngọc xanh) |
|---|---|---|---|
| 213 | Bùa Trí Tuệ | TN & SM nhận gấp đôi | 5 / 20 / 500 |
| 214 | Bùa Mạnh Mẽ | +150% sức đánh khi đánh quái | 5 / 20 / 500 |
| 215 | Bùa Da Trâu | Bị quái đánh chỉ mất 50% | 3 / 10 / 250 |
| 216 | Bùa Oai Hùng | Quái thủ lĩnh đánh yếu như quái thường | 7 / 28 / 700 |
| 217 | Bùa Bất Tử | Không bị quái đánh chết (còn 1 HP) | 7 / 28 / 700 |
| 218 | Bùa Dẻo Dai | Thể lực không giảm | 1 / 4 / 100 |
| 219 | Bùa Thu Hút | Tự hút vật phẩm của mình | 2 / 10 / 250 |
| 522 | Bùa Đệ Tử | Đệ tự đánh quái, giảm 50% sát thương… | 10 / 60 / 1.500 |
| 671 | Bùa Trí Tuệ x3 | TN & SM gấp 3 | 15 / 60 / 1.500 |
| 672 | Bùa Trí Tuệ x4 | TN & SM gấp 4 | 45 / 180 / 4.500 |

`Charms` còn có các id 2025, 2076, 1387, 3000–3003 (không tồn tại trong DB).

### 10.7 Hộp quà / rương / capsule mở ra item

> "Ô trống" = số ô trống tối thiểu yêu cầu. Xác suất ghi theo đúng code; chỉ số là option được thêm vào item nhận.

| id | Tên | Hàm (dòng) | Ô trống | Phần thưởng & tỉ lệ |
|---|---|---|---|---|
| **570** | Rương Gỗ | `openRuongGo` (943–1077) | `calculateRequiredEmptySlots(level)` | 1 lần/ngày (sau 0h). `level` = option 72. **Level 0**: nhận 1 item 190 (Vàng) số lượng 1. **Level ≥ 1**: Vàng 190 x (100×level ±15%)×1000; nếu level ≥ 9 thêm Ngọc 77 x (100 + (level−9)×20); đồ trang bị x1 (level 5–8: x2; 10–12: x3) lấy từ `LIST_ITEM_CLOTHES[hành tinh ngẫu nhiên][loại ngẫu nhiên][level − (2..4), min 1]` với option shop; 2 item khác nhau (5–8: 3; 10–12: 4) từ {17, 18, 19, 20, 380, 381, 382, 383, 384, 385, 1229} mỗi loại x(1..level); sao pha lê 441–447 x1–3 (level > 9: 2 lượt) option 95+r (param 5, riêng 98/99 = 3); đá nâng cấp 220–224 x(1..2×level) (level > 9: 2 lượt) option 71−r. Hiển thị lần lượt qua menu `RUONG_GO` |
| **380** | Viên Capsule kì bí | `openCSKB` (1491–1518) | 1 | Chọn đều 1 trong 9: 4/9 (44,4%) ⇒ +5.000–20.000 vàng; 1/9 mỗi loại ⇒ 381 Cuồng nộ, 382 Bổ huyết, 383 Bổ khí, 384 Giáp Xên, 385 Ẩn danh |
| **736** | Hộp quà 5 sao | `ItemService.OpenItem736` (424–466) | 2 | r∈[1,100]: ≤50 `randomRac` (NR 5/6/7 sao); ≤70 Ngọc 77 x1–2; ≤80 Đá bảo vệ 987; ≤90 Ghế bay 795 (`vanBay2011`); ≤95 Bó Hoa Hồng 954 (`phuKien2011`); ≤100 Cải trang 680 (`caitrang2011`). `inventory.event++` |
| (648 trong ItemService) | — | `OpenItem648` (468–510) | 2 | Không được gọi (UseItem dùng `NoelItemBox` cho 648) |
| **648** | Hộp quà giáng sinh | `NoelItemBox` (2352–2442) | 1 | 5/90 (≈5,6%): +10–20 **ngọc xanh**. Còn lại chọn đều 1 trong 7: sao pha lê 441–445 (x1–5, option id−346 = 95–99, param 5 hoặc 3), item 381–384 (x1–5), NR 4–7 sao 17–20, Ngọc rồng băng 925–931, Mảnh thiên sứ 1066–1070 (x1–5), 533 Kẹo giáng sinh, 380 Capsule kì bí (x1, option 73) |
| **1170** | Gói quà | `BlackGokuItemBoxEventNoel` (2526–2574) | 1 | 25%: Xe tuần lộc 746 (1% vĩnh viễn, 99% HSD 7–30); 18,75%: CT Noel theo hành tinh (1155 TĐ / 1157 NM / 1156 XD) SĐ/HP/KI 23%; 14,06%: CT Broly (1018/1019/1020) 23%; 42,19%: CT Diệt Quỷ 1087–1091 (SĐ 22%, HP 21%, KI 21%). CT: 95% HSD 1–3 ngày, 5% vĩnh viễn |
| **962** | Cap thời trang 5 ngày | `C5` (2576–2601) | 1 | CT Diệt Quỷ 1087–1091: 50 (1–16), 77 (1–17), 103 (1–15), 95 (1–5), 96 (1–5), 1 option ∈ {94, 97, 108} (3–5); 98% HSD 5 ngày (2% vĩnh viễn) |
| **963** | Cap thời trang 7 ngày | `C7` (2603–2628) | 1 | như trên, 100% HSD 7 ngày |
| **1171** | Túi 7 chú lùn | `ChuLunBox` (2630–2660) | 1 | 1158–1164 Chú lùn: 50=11, 77=13, 103=13, 1 option ∈ {94,97,108} (3–5); 98% HSD 1–3 ngày, 2% vĩnh viễn |
| **1560** | Rương ngọc rồng | `RuongNgocRong` (2792–2829) | 1 | Cần **1561 Chìa khóa vàng** (trừ 1). r=nextInt(0,100): r<85 (85/101) ⇒ NR 4/5/6/7 sao đều; r<95 (10/101) ⇒ NR 3 sao; còn lại (6/101) ⇒ NR 1 hoặc 2 sao |
| **1775** | Hộp thần linh | `OpenHopThanlinh` (1950–2004) | 1 | Chọn đều 1 trong 15 món thần linh của **cả 3 hành tinh** (Nhẫn 561 xuất hiện 3 lần ⇒ 20%; mỗi món còn lại 1/15). Chỉ số `initChiSoItem` + option 30 |
| **1776** | Hộp quà sự kiện | `OpenHopQuaThuong` (2006–2036) | 2 | 1772 búa sơn tinh / 1773 bút thủy tinh (50/50); 50=10, 77=10, 103=10, 14=10; HSD 3 ngày (1% HSD 0) |
| **1777** | Hộp quà sự kiện VIP | `OpenHopQuaVip` (2081–2111) | 2 | 1761 Mị Hoàng Kim / 1731 CT Black Goku Rose / 1732 CT Black Goku (1/3); 50/77/103 = 25, 236 May mắn 10; HSD 3 (1% HSD 0) |
| **1592** | Hộp quà Goku Day VIP (id 27) | `OpenHopQuaGokuDay` (2113–2143) | 2 | 1 trong {1588 CT Goku, 1589, 1595, 1587 CT Goku SSJ3, 1593, 1590 CT Goku SSJ Blue}; 50/77/103 = 25, 210 = 4 dòng ẩn; HSD 3 (1% HSD 0) |
| **1591, 1594** | Hộp quà Goku Day | `OpenHopQuaGokuDayVangNgoc` (2145–2175) | 2 | cùng danh sách; 50/77/103 = 20, 210 = 2; HSD 3 (1% HSD 0) |
| **1840** | Hộp quà Goku Day VIP (id 75) | `OpenHopQuaGokuDayVip` (2177–2215) | 5 | cùng danh sách; 50/77/103 = 27, 210 = 4; `nextInt(0,30) < 50` luôn đúng ⇒ **luôn HSD 0** |
| **1757** | Hộp quà Cađíc VIP | `OpenHopQuaCadic` (2217–2247) | 2 | 1741 CT Cađíc … 1746 CT Cađíc SSJ Blue (1/6); 50/77/103 = 27, 210 = 4; HSD 3 (1% HSD 0) |
| **1821** | Trứng vàng rồng nhí | `OpenTrungRongNhi` (2038–2075) | 5 | Pet 1765–1771 Bé Rồng Cute (1/7); 50 = 10–18, 5 = 5–10, 14 = 5–10, option 30 (param 0/1); HSD 15 (1% HSD 0) |
| **1822** | Rada ngọc rồng | `RadaNgocRong` (1127–1162) | 1 | Đeo lưng 1579–1585 ("1 sao"…"7 sao") 1/7; 50/77/103 = 10–17; 30; HSD 15 (1% HSD 0) |
| **1823** | Rada ngọc rồng Vip | `RadaNgocRongVip` (1164–1200) | 1 | 1586, 1809–1814 ("Super 1 sao"…); 50/77/103 = 10–20; 210 = 3; 30; HSD 15 (1% HSD 0) |
| **1569** | Kho báu hải tặc | `KhoBauHaiTac` (1202–1238) | 1 | CT Hải tặc 618–626 (1/9); 50/77/103 = 10–15; 210 = 2; 30; HSD 15 (1% HSD 0) |
| **1609** | Kem trái cây | `KemTraiCay` (1372–1413) | 5 | 1804 Buma đi biển; 50 = 20–28, 77/103 = 20–30; 30; HSD 15 (1% HSD 0); `point_sukien2 += 1` (đua top) |
| **1608** | Hộp quà thiếu nhi | `QuaThieuNhi` (1415–1459) | (kiểm tra `>= 0` ⇒ không yêu cầu) | 1807 Goku ssj 4 kid, 1599 CT Fide nhí, 1600, 1601, 1602 (1/5); 50/77/103 = 20–25; 210 = 4; 30; **50% HSD 0, 50% HSD 15**; `point_sukien += 1` |
| **1655** | Cápsule Kích hoạt 1 món tự chọn | `CapsuleKichHoat` (2691) → `OpenSKH` | 1 | Mục 9.5.3 |
| **460** | Cục xương | `XuongCho` (2261–2350) | — | Cần boss **Sói Hẹc Quyn** trong khu và sói chưa no. Thả xương, trừ 1; 75%: sao pha lê 441–447 (param 5; 444/445 = 3); 25%: **Phiếu giảm giá 459** (112 = 80%, 93 = 90 ngày, 20 = PIN 0–9999). Sau đó `Thread.sleep(5000)` rồi sói rời map. Cập nhật danh hiệu "Kẻ thao túng sói" |
| **1798–1802** | Tayaki, Kẹo táo, Kem que đôi, Mochi, Ramen | `ThucAnChoThan` (1461–1489) | 5 | Có ≥ 99 cái (chỉ 1799–1802 hợp lệ) ⇒ trừ 99, nhận **1 Phiếu thức ăn 1805**. 1798 Tayaki luôn báo "không đúng loại" |
| **1575** | Pháo bông | `PhaoBong` (1314–1339) | — | +5.000–20.000 vàng + hiệu ứng |
| **1576** | Pháo bông VIP | `PhaoBongVip` (1341–1370) | — | +500.000–2.000.000 vàng + hiệu ứng |
| **727** | Siêu thần thủy (mô tả 1 triệu) | `ItemSieuThanThuy` (2723–2744) | — | 90%: **chết**. 10%: `addSMTN(5.000.000)` × 2 = +10 triệu SM/TN |
| **728** | Siêu thần thủy (mô tả 5 triệu) | như trên | — | 90% chết; 10%: × 10 = +50 triệu |

Hàm có trong file nhưng **không được gọi**: `HopQuaChinhChu` (dòng 2444, ra 1 item trong 637–642 hoặc 1150–1154), `HopQuaNheNhang` (dòng 2485, ra 381–385 hoặc 628–636), `NonNoelDo` (dòng 2662, Nón Noel đỏ 387/390/393), `TuiVang` (dòng 2707, +100.000–10.000.000 vàng + item 190 option 1 = 3–333).

### 10.8 Pet đi theo (type 27) – `PetService.Pet2(pl, head, body, leg)`

Dùng ⇒ `itemBagToBody` (ô 7) rồi tạo `NewPet` với part tương ứng (HP/KI 500.000.000, `PetService.java` dòng 331–351). Chỉ số của pet nằm ở option item.

| id item | Tên | head, body, leg |
|---|---|---|
| 892 | Thỏ xám | 882, 883, 884 |
| 893 | Thỏ trắng | 885, 886, 887 |
| 908 | Ma phong ba | 891, 892, 893 |
| 909 | Thần chết cute | 894, 895, 896 |
| 910 | Bí ngô nhí nhảnh | 897, 898, 899 |
| 916 | Lính bảo vệ tam giác | 925, 926, 927 |
| 917 | Lính bảo vệ vuông | 928, 929, 930 |
| 918 | Lính bảo vệ tròn | 931, 932, 933 |
| 919 | Búp bê | 934, 935, 936 |
| 936 | Tuần lộc nhí | 718, 719, 720 |
| 942 / 943 / 944 | Hổ mặp vàng / trắng / xanh | 966–968 / 969–971 / 972–974 |
| 967 | Sao la | 1050, 1051, 1052 |
| 1008 | Cua đỏ | 1074, 1075, 1076 |
| 1039 / 1040 | Pet Thỏ ốm / Thỏ mập | 1089–1091 / 1092–1094 |
| 1046 | Pet Khỉ Bong Bóng | −1, −1, −1 |
| 1107 | Pet Bí Ma Vương | 1155, 1156, 1157 |
| 1114 | Pet Ma vàng phù thủy | 1158, 1159, 1160 |
| 1188 | Pet mèo đen đuôi vàng | 1183, 1184, 1185 |
| 1202, 1203 | Pet mèo trắng đuôi vàng | 1201, 1202, 1203 |
| 1207 | Pet Minion | 1077, 1078, 1079 |
| 1224 / 1225 / 1226 | Pet Voi Chín Ngà / Gà Chín Cựa / Ngựa Chín Hồng mao | 1227–1229 / 1233–1235 / 1230–1232 |
| 1243 / 1244 | Pet bọ cánh cứng / ngài đêm | 1245–1247 / 1248–1250 |
| 1256 | Pet heo bướm | 1267, 1268, 1269 |
| 1318 | Pet Mông Quỷ | 1299, 1300, 1301 |
| 1347 | Pet Mèo Phù Thủy | 1302, 1303, 1304 |
| 1414 | Pet Shiba | 1341, 1342, 1343 |
| 1435 | Pet chim cánh cụt | 1347, 1348, 1349 |
| 1452 | Pet Ông già Noel | 1365, 1366, 1367 |
| 1458 | Pet Shiba đeo nơ | 1368, 1369, 1370 |
| 1482 | Pet rồng Pikachu thanh long | 1398, 1399, 1400 |
| 1497 | Pet rồng con thần tài | 1401, 1402, 1403 |
| 1550 / 1551 | Pet Godzilla / Pet Kong | 1428–1430 / 1425–1427 |
| 1564 | Pet Po | 1437, 1438, 1439 |
| 1568 | Pet Shimo | 1443, 1444, 1445 |
| 1573 | Xe tăng Santa | 1446, 1447, 1448 |
| 1596, 1597 | Pet Albart / Albart Cup | 1473, 1474, 1475 |
| 1611 | Pet Minion Otto | 1488, 1494, 1495 |
| 1620, 1621 / 1622 | Pet Baby Shark | 1496–1498 / 1488–1490 |
| 1629 / 1630 | Pet Capybara đeo ba lô / xì mũi | 1505–1507 / 1508–1510 |
| 1631 | Pet Zịt vàng bối rối | 1513, 1516, 1517 |
| 1633 | Pet Mini Desutoron Gas | 1523, 1524, 1525 |
| 1654 | Pet Cerberus | 1526, 1529, 1530 |
| 1668 | Pet Capybara hồng | 1550, 1551, 1552 |
| 1682 / 1683 | Pet Hải Ly / Hải Ly Ong Vàng | 1558–1560 / 1561–1563 |
| 1686 | Pet Thỏ Ú | 1572, 1573, 1574 |
| 1727 | Pet Ma cầu mưa | 1616, 1617, 1618 |
| 1729 | Pet Rồng Xanh | 1621, 1622, 1623 |
| 1750 | Pet Khủng Long ngok | 1464, 1465, 1466 |
| 1765–1771 | Bé Rồng Cute (7 màu) | 1662–1664, 1665–1667, 1668–1670, 1671–1673, 1674–1676, 1677–1679, 1680–1682 |
| 1789 | Thỏ may mắn | 1724, 1725, 1726 |

### 10.9 Vật phẩm chức năng khác

| id | Tên | Hàm / dòng | Hiệu ứng |
|---|---|---|---|
| 193 | Gói 10 viên Capsule | `openCapsuleUI` (817–822) | Mở bảng chọn map tàu vận chuyển, **trừ 1**; thiếu `break` nên gọi mở bảng thêm lần nữa |
| 194 | Viên Capsule đặc biệt | 820–822 | Mở bảng chọn map, **không trừ** (dùng vô hạn) |
| — | Chọn map capsule | `choseMapCapsule` (1821–1855) | Chặn khi đang mang NR Namếc; chặn khu > 25 người, map Doanh trại, Mabư, Hủy diệt |
| 361 | Gói 10 Rađa dò ngọc | 298–303 | (type 25 ⇒ bị mặc vào ô 10 trước) `idGo` = 0–6, mở menu dịch chuyển tới NR Namếc, trừ 1 |
| 992 | Nhẫn thời không sai lệch | 293–297 | `type = 2`, `maxTime = 5`, `Transport` ⇒ sang map 80 (nếu đang ở hành tinh thực vật) hoặc 160 (`Controller` -105) |
| 454 / 921 / 1819 | Bông tai Porata cấp 1 / 2 / 3 | `usePorata`, `usePorata2`, `usePorata3` (1781–1814) | Có đệ & chưa hợp thể kiểu 4 ⇒ hợp thể Porata cấp 1/2/3; đang hợp thể ⇒ tách |
| 521 | Tự động luyện tập | mục 10.5 | |
| 401 | Đổi đệ tử | `changePet` (1248–1259) | Đệ mới thường với hành tinh = (hành tinh đệ cũ + 1) mod 3; trừ 1 |
| 402 / 403 / 404 / 759 | Nâng kỹ năng 1/2/3/4 đệ tử | `upSkillPet` (1903–1948) | `SkillUtil.upSkillPet(skills, 0..3)`; thành công trừ 1 |
| 1758 / 1759 / 1760 | Phở Linh Lang / Giải Phóng / Công viên Hòa Bình | 881–931 | Đổi (random lại) skill 2 / 3 / 4 của đệ (`openSkill2/3/4`). **Trừ item trước khi kiểm tra** đệ có skill đó chưa |
| 1795 | Bình hút năng lượng | `changePetRamdom` (1261–1295) | Cần option 250 ≥ 3000 Kilis, đệ Mabư (`typePet == 1`) ≥ 40 tỷ SM ⇒ đổi ngẫu nhiên thành đệ Uub (type 2) / Kid Beer (3) / Jiren (4) (1/3), xoá item |
| 211 / 212 | Nho tím / Nho xanh | `eatGrapes` (1297–1312) | Chỉ dùng khi thể lực ≤ 50%: 211 hồi 100%, 212 hồi +20% |
| 342–345 | Vệ tinh trí lực / trí tuệ / phòng thủ / sinh lực | 753–763 | Thả vệ tinh tại vị trí (tối đa 3 vệ tinh/map), trừ 1 |
| 568 | Quả Trứng | 865–877 | Tạo trứng Mabư nếu chưa có; đang ở nhà (map 21+gender) thì gửi trứng |
| 726 | Mảnh giấy có chữ MA | `ItemManhGiay` (2249–2259) | Menu tìm Đại Ma Vương Pôcôlô ⇒ `SuperDivineWaterService.joinMapThanhThuy` |
| 727 / 728 | Siêu thần thủy | mục 10.7 | |
| 987 | Đá bảo vệ | 862–864 | Chỉ thông báo "Bảo vệ trang bị không bị rớt cấp" (dùng trong nâng cấp) |
| 1787 | Vé riêng tư | `MapRiengTu` (2695–2697) | Bay tới map **164** x = 870. **Không trừ vé** (HSD 3 ngày khi mua) |
| 718 | Vé tặng ngọc | `VeTangNgoc` → menu 900 → `Input.FIND_PLAYER_NAME` / `FIND_PLAYER_GIFT_RUBY` (`Input.java` 342–395) | Nhập tên (online) & số ngọc xanh. Người tặng trả `soGem + 10%`, trừ 1 vé; người nhận được `soGem × 90%` |
| 1652 | Loa to thế giới | `LoaTheGioi` (2703) | Mở menu 901 – **không có handler** cho menu 901 |
| 1505 | Giấy màu | 718–731 → menu `event3` (`NpcFactory` 549–565) | Cần 99 Giấy màu ⇒ trừ 99, nhận 1 Hộp đựng quà 1506 (menu ghi "Giá vàng 2.000.000" nhưng không trừ vàng) |
| 1506–1509 | Hộp đựng quà, Sôcôla Trái Tim, Hoa hồng giấy, Nơ trang trí | menu `event3_1` (`NpcFactory` 566–618) | Cần 5 Sôcôla + 30 Hoa hồng giấy + 1 Nơ + 1 Hộp. Chọn 0 ⇒ 1510 Hộp quà nhẹ nhàng (không trừ Nơ); chọn 1 ⇒ 1511 Hộp quà chỉn chu (trừ Nơ) |
| 2006 | (không có trong DB) | 878–880 | Form đổi tên nhân vật |
| 1822, 1823, 1569, 1609, 1608, 1798–1802, 1776, 1777, 1591–1594, 1840, 1757, 1821, 1775, 648, 1171, 1560, 1170, 736, 460, 962, 963, 1575, 1576 | | mục 10.7 | |
| 33 (type) | Thẻ sưu tầm | `UseCard` (2746–2790) | Xem [07 – Thẻ sưu tầm](07-shop-giao-dich-ky-gui.md#11-thẻ-sưu-tầm-radar) |

Item không có nhánh xử lý nào (dùng không có tác dụng) gồm: type 8, 14, 15, 16, 28, 30, 31, 36, 37 và đa số item type 27/29/75 không liệt kê ở trên (vd. 457 Thỏi vàng, 1561 Chìa khóa vàng, 1229 Bí kíp tuyệt kỹ, Gói 30 đậu thần 293–299).

---

## 11. Ghi chú / điểm cần lưu ý

1. **ID item ≥ 2000 không tồn tại trong DB** nhưng được code tham chiếu: 2006 (đổi tên), 2019–2026, 2036–2040, 2048, 2050–2055, 2074, 2075, 2077, 2132, 2322, Charms 2025/2076/3000–3003. `createNewItem` với id này ⇒ `template = null` ⇒ NullPointerException.
2. **Option 30 chỉ kiểm tra id**, nhiều hàm cố ý đặt `ItemOption(30, 0)` (hoặc biến `khongthegiaodich = 0`) tưởng là "có thể giao dịch" nhưng thực tế vẫn **không giao dịch/không ký gửi được**.
3. **Hạn sử dụng chỉ kiểm tra lúc đăng nhập**; người chơi online liên tục có thể giữ item quá hạn. Nhiều hộp quà có ~1% (Goku Day VIP 1840: 100%; Hộp quà thiếu nhi: 50%) ra HSD 0 ⇒ bị xoá ngay sau 1 ngày.
4. `randomOption < 0.5` với `randomOption` là int 0–99 ⇒ chỉ đúng khi = 0 (1%), comment trong code thường ghi sai tỉ lệ.
5. **Cỏ bốn lá (1635)** không gán `timeLengthCoBonLa` ⇒ hiệu lực tắt ở lần `update()` kế tiếp. **Ly mía 2 và 3** (1615, 1616) hết hạn dựa trên `lastTimeUseNuocMia1` (`ItemTime.java` dòng 242, 248) ⇒ tắt gần như ngay lập tức nếu chưa dùng Ly mía 1. **Nồi cơm điện (1233)** và **Khẩu trang (764)** không bao giờ tự tắt.
6. **Set Gohan (233/234)** không có hiệu ứng; **Set Picolo** `dameSkill *= 3/2` = ×1; **Thần Vũ Trụ Kaio** `crit += 10/100` = +0.
7. `useItem` case 25 thiếu `break` ⇒ sách tuyệt kỹ / Gói 10 Rađa (361) bị mặc rồi chạy tiếp switch theo id. Case 193 thiếu `break` ⇒ mở bảng capsule 2 lần.
8. **Phở 1758–1760** trừ item trước khi kiểm tra điều kiện ⇒ mất item nếu đệ chưa có skill tương ứng.
9. **Vé riêng tư (1787)** không bị trừ khi dùng. **Vé tặng ngọc (718)** thu phí 2 lần (người gửi trả +10%, người nhận chỉ nhận 90%).
10. **Cục xương (460)** gọi `Thread.sleep(5000)` trong luồng xử lý gói tin ⇒ treo luồng của người chơi 5 giây.
11. `InventoryService.itemBodyToBox` có điều kiện `index < 0 || index >= size` ⇒ chỉ chạy khi index không hợp lệ (và sẽ ném exception) – chức năng body → rương thực tế không hoạt động.
12. Thông báo khi vứt Thỏi vàng (457) chứa lời lẽ tục tĩu (`InventoryService.java` dòng 134) – nên thay.
13. Gộp chồng yêu cầu **danh sách option giống hệt theo thứ tự** ⇒ cùng item nhưng option khác thứ tự sẽ chiếm ô riêng. Các item gộp không giới hạn (457, 1066–1070, đá 220–224…) có thể tràn `int` (> 2.147.483.647).
14. Mảng id trong `OpenSKH` có lỗi dữ liệu (lặp 174, thiếu 175/234/255/258) ⇒ vài món không bao giờ ra.
15. `NoelItemBox` đặt tên biến `ruby` nhưng cộng **ngọc xanh**; đặt tên "đá nâng cấp" cho 381–384 nhưng đó là Cuồng nộ/Bổ huyết/Bổ khí/Giáp Xên.
16. `ThucAnChoThan`: case gồm 1798 nhưng điều kiện đổi không có 1798 và lại có 1789 (Thỏ may mắn – vốn đã bị case Pet2 bắt trước).
17. Hàm `OpenVeTangNgoc`, `OpenItem648`, `otpts`, `randomSKHId`, `HopQuaChinhChu`, `HopQuaNheNhang`, `NonNoelDo`, `TuiVang` là code chết.
18. Nhân vật mới nhận item **63 (Đậu thần cấp 5)** x10 với option 2 = 8 (hồi 8.000), comment code ghi nhầm "thỏi vàng".
19. Menu **Loa to thế giới (901)** không có xử lý ⇒ vật phẩm không dùng được.
20. `Siêu thần thủy` mô tả DB "1 triệu/5 triệu" nhưng code cộng 10 triệu/50 triệu với 10% và **90% giết người chơi**.
