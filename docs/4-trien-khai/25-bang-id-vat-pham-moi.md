# 25 — Bảng id vật phẩm mới (CHỐT)

> Bảng đánh số cuối cùng cho toàn bộ vật phẩm của tuyến nhiệm vụ chính mới.
> **Đây là nguồn sự thật duy nhất về id vật phẩm mới.** Mọi file khác (20a, 20b, 20c, 21b)
> dùng id cũ và **đã lỗi thời** — xem §6 để tra cứu đối chiếu.
>
> File SQL đi kèm: [`SRC/sql/patch/01-vat-pham-moi.sql`](../../SRC/sql/patch/01-vat-pham-moi.sql)
> Quyết định gốc: [22 §0](22-san-sang-code.md) · Đặc tả kỹ thuật: [21b](../3-kiem-tra-can-bang/21b-item-moi-dac-ta.md)

## Mục lục

1. [Tóm tắt](#1-tóm-tắt)
2. [Vì sao id phải liên tục — đã tự kiểm chứng](#2-vì-sao-id-phải-liên-tục--đã-tự-kiểm-chứng)
3. [BẢNG CHỐT — 32 vật phẩm, id 2000–2031](#3-bảng-chốt--32-vật-phẩm-id-20002031)
4. [Code cần vá vì hardcode id ≥ 2000](#4-code-cần-vá-vì-hardcode-id--2000)
5. [Chặn bán / giao dịch / ký gửi / vứt — cách đã chọn](#5-chặn-bán--giao-dịch--ký-gửi--vứt--cách-đã-chọn)
6. [Bảng đối chiếu id cũ → id mới](#6-bảng-đối-chiếu-id-cũ--id-mới)
7. [Việc cần làm khi lên server](#7-việc-cần-làm-khi-lên-server)
8. [Điểm chưa chắc chắn](#8-điểm-chưa-chắc-chắn)

---

## 1. Tóm tắt

| Chỉ số | Giá trị |
|---|---|
| Số vật phẩm mới | **32** |
| Dải id | **2000 – 2031, liên tục, không hở** |
| Dòng trống đệm phải thêm | **0** (id đã được nén thành một khối liền) |
| `item_template` sau khi import | **2032 dòng, id 0 – 2031** |
| Phân bố TYPE | 23 × TYPE 8 · 7 × TYPE 27 · 2 × TYPE 36 |
| Icon riêng (ảnh mới) | 9 món — 2000…2008 dùng icon 20000…20008 |
| Icon mượn | 23 món, đã kiểm tra tồn tại đủ ở x1, x2, x3, x4 |
| Dòng thêm vào `data_badges` | 2 |
| Dòng thêm vào `item_shop` | 4 |

**So với [21b §3.6](../3-kiem-tra-can-bang/21b-item-moi-dac-ta.md) đã tiết kiệm được 90 dòng trống**:
21b đề xuất rải id trong dải 2000–2121 rồi lấp 90 dòng rỗng; bản chốt này nén lại còn 2000–2031.

---

## 2. Vì sao id phải liên tục — đã tự kiểm chứng

Hai ràng buộc độc lập, cả hai đều đã đọc lại trong mã nguồn:

**(a) Tra vật phẩm theo CHỈ SỐ MẢNG, không theo id** —
`SRC/src/nro/models/services/ItemService.java:360`

```java
public Template.ItemTemplate getTemplate(int id) {
    return Manager.ITEM_TEMPLATES.get(id);   // ArrayList.get() = chỉ số, KHÔNG phải id
}
```

`Manager.ITEM_TEMPLATES` là `ArrayList`, nạp tuần tự ở `Manager.java` (lô 750 dòng).
Chỉ số mảng bằng id **chỉ khi** id trong DB liên tục từ 0.

**(b) Gửi xuống client theo thứ tự, KHÔNG kèm id** —
`SRC/src/nro/models/data/ItemData.java:65-93` gửi đúng 9 trường mỗi item
(`type, gender, name, description, level, strRequire, iconID, part, isUpToUp`) — **không có `id`**.
Client dựng lại bảng item bằng thứ tự nhận được.

**Hậu quả nếu hở 1 id**: mọi item từ chỗ hở trở đi lệch một bậc — client hiện sai tên và icon
của tất cả vật phẩm phía sau, và item cuối cùng ném `IndexOutOfBoundsException` khi có người nhặt.

**Số liệu DB thật** (đã đếm lại từ `database team2026.sql`): `item_template` có **đúng 2000 dòng,
id 0–1999, không thiếu id nào, không trùng id nào**. Vì vậy id mới **bắt buộc bắt đầu từ 2000**.

> **Đã vá thêm một rủi ro liên quan**: `Manager.java` nạp bằng
> `SELECT * FROM item_template LIMIT ? OFFSET ?` **không có `ORDER BY`**. Thứ tự đúng hiện nay
> chỉ nhờ InnoDB trả theo khóa chính — sẽ hỏng sau `OPTIMIZE TABLE`, đổi engine hoặc nâng cấp MySQL,
> và khi đó **toàn bộ bảng item của server lệch**. Đã thêm `ORDER BY id` (§4, mục vá số 9).

---

## 3. BẢNG CHỐT — 32 vật phẩm, id 2000–2031

Quy ước áp dụng cho **mọi** dòng: `gender = 3` (dùng chung 3 hành tinh) · `power_require = 0` ·
`gold = 0` · `gem = 0` · `head = body = leg = -1` · `level = 1` (danh hiệu = 0) ·
`part = -1` (trừ 2 danh hiệu, `part = idEffect`).

**Option bắt buộc gắn ở MỌI nơi sinh ra item**: `30` "Không thể giao dịch", param 0.
Thiếu ở một nguồn phát thì item cùng id sẽ khác option ⇒ `addItemList` không gộp chồng được ⇒ tốn ô hành trang.

Cột "Bán / GD / Ký gửi": **B** = bán cho NPC, **GD** = giao dịch với người chơi, **KG** = ký gửi.

### 3.1 Nhóm A — 9 món trục truyện, ảnh riêng (icon 20000–20008)

| id | Tên | TYPE | icon_id | Icon lấy từ đâu | Dùng ở nhiệm vụ | B | GD | KG |
|---|---|---|---|---|---|---|---|---|
| 2000 | Lõi Hư Không | **27** | 20000 | **Ảnh riêng** (chủ dự án vẽ) | Thưởng NV 45 b3. NV 47 (nhánh A) dùng ở map 145 → trừ đi, trao 2001. NV 50 (nhánh B) dùng → **giữ vĩnh viễn** làm cờ hậu truyện | ✗ | ✗ | ✗ |
| 2001 | Vỏ Lõi rỗng | 8 | 20001 | **Ảnh riêng** | Trao ở NV 47 b1. Kỷ vật vĩnh viễn, không tiêu đi | ✗ | ✗ | ✗ |
| 2002 | Mảnh Ký Ức 1 | **27** | 20002 | **Ảnh riêng** | Thưởng NV 7; **dùng** ở NV 40 b3 (trigger A12) | ✗ | ✗ | ✗ |
| 2003 | Mảnh Ký Ức 2 | 8 | 20003 | **Ảnh riêng** | Thưởng NV 15 | ✗ | ✗ | ✗ |
| 2004 | Mảnh Ký Ức 3 | 8 | 20004 | **Ảnh riêng** | Thưởng NV 23 | ✗ | ✗ | ✗ |
| 2005 | Mảnh Ký Ức 4 | 8 | 20005 | **Ảnh riêng** | Thưởng NV 31 **hoặc** NV 49 (điểm rẽ nhánh) | ✗ | ✗ | ✗ |
| 2006 | Mảnh Ký Ức 5 | 8 | 20006 | **Ảnh riêng** | NPC 29 Rồng Omega trao ở NV 39 b3 | ✗ | ✗ | ✗ |
| 2007 | Mảnh Ký Ức 6 | 8 | 20007 | **Ảnh riêng** | NPC 70 Bardock trao ở NV 39 b5 | ✗ | ✗ | ✗ |
| 2008 | Mảnh Ký Ức 7 | 8 | 20008 | **Ảnh riêng** | Nhặt trên map 78, chỉ hiện với người ở `TASK_45_1` | ✗ | ✗ | ✗ |

> **7 Mảnh Ký Ức bắt buộc nằm liên tiếp 2002 → 2008** để NV 45 đếm đủ bộ bằng một vòng lặp đơn giản.
> Chỉ **2002** là TYPE 27 (vì NV 40 bắt bấm "Dùng"); 6 mảnh còn lại TYPE 8.
>
> **Tên hiển thị dùng "Mảnh Ký Ức 1…7", KHÔNG dùng "#1…#7"** như thiết kế gốc:
> ký tự `#` là ký tự thay thế tham số trong chuỗi option (`Số lượng #`, `Hạn sử dụng # ngày`),
> và không một dòng nào trong toàn bộ `item_template` hiện tại chứa `#`. Tránh cho chắc.

### 3.2 Nhóm B — chương 1–2 (20a, NV 0–15)

| id | Tên | TYPE | icon_id | Mượn icon của item | Dùng ở nhiệm vụ | up | B | GD | KG |
|---|---|---|---|---|---|---|---|---|---|
| 2009 | Mảnh Vỡ Hư Không | 8 | 1421 | 225 Mảnh đá vụn | NV 2 b2 — rơi từ mob 1/2/3 khi ở `TASK_2_2` | ✔ | ✗ | ✗ | ✗ |
| 2010 | Kỷ Vật Của Ông | **27** | 9067 | 992 Nhẫn thời không sai lệch | NV 5 b0–2 — rơi từ mob 7/8/9 ở `TASK_5_0`; **dùng** ở b1 (A12) | | ✗ | ✗ | ✗ |
| 2011 | Máy Dò Ký Ức | 8 | 1089 | 12 Rada cấp 1 | Thưởng NV 8. **Vật chứng giữ mãi**, không tiêu đi | | ✗ | ✗ | ✗ |
| 2012 | Hộp Ký Ức Bị Đánh Cắp | 8 | 7222 | 796 Hộp Capsule | Thưởng NV 14 → nộp ở NV 15 | | ✗ | ✗ | ✗ |
| 2013 | Hạt Giống Hy Vọng | **27** | 241 | 13 Đậu thần cấp 1 | NV 11 b1 — **dùng** để nâng cây đậu thần lên cấp 2 | | ✗ | ✗ | ✗ |

> `power_require = 0` là **bắt buộc** với 2013: NV 11 ở chương 2, sức mạnh người chơi còn thấp;
> `UseItem.useItem:267` chặn nút "Dùng" nếu `power_require > sức mạnh` ⇒ kẹt nhiệm vụ vĩnh viễn.

### 3.3 Nhóm C — chương 3 (20b, NV 16–23) và NV 48

| id | Tên | TYPE | icon_id | Mượn icon của item | Dùng ở nhiệm vụ | up | B | GD | KG |
|---|---|---|---|---|---|---|---|---|---|
| 2014 | Vỏ đạn khắc dấu | 8 | 1421 | 225 Mảnh đá vụn | NV 16 b3 — rơi 25% từ mob 22/23/24 ở `TASK_16_3`, cần 5 cái | ✔ | ✗ | ✗ | ✗ |
| 2015 | Búa rèn cũ | **27** | 1417 | 223 Đá Titan | NV 17 b0 NPC trao → b2 **dùng** (A12) | | ✗ | ✗ | ✗ |
| 2016 | Thẻ tiền thưởng Granola | 8 | 5428 | 611 Bản đồ kho báu | NV 20 b4 — rơi 100% từ boss −20/−21/−22 ở `TASK_20_4`, cần 3 cái | ✔ | ✗ | ✗ | ✗ |
| 2017 | Biên bản truy nã Ngân Hà | 8 | **5207** | 590 Bí kiếp | NV 48 b4 — rơi 100% từ boss −20/−21/−22 ở `TASK_48_4`, cần 3 cái | ✔ | ✗ | ✗ | ✗ |
| 2018 | Máy đo ký ức | 8 | 6467 | 674 Đá ngũ sắc | NV 22 b3 — rơi 100% từ boss −27 Tiểu đội trưởng ở `TASK_22_3` | | ✗ | ✗ | ✗ |

> **2017 đã đổi icon so với 21b**: 21b cho cả 2016 và 2017 dùng icon 5428 ⇒ hai vật phẩm khác nhau
> trông giống hệt nhau. Đổi 2017 sang **5207** (icon của item 590 "Bí kiếp" — hình cuộn giấy, hợp với
> "biên bản truy nã"). Đã kiểm tra file `5207.png` có đủ ở x1, x2, x3, x4.

### 3.4 Nhóm D — chương 4 (20b, NV 24–31)

| id | Tên | TYPE | icon_id | Mượn icon của item | Dùng ở nhiệm vụ | up | B | GD | KG |
|---|---|---|---|---|---|---|---|---|---|
| 2019 | Lõi năng lượng Android | 8 | 8620 | 935 Đá xanh lam | NV 25 b2 — rơi 100% từ boss −30/−31 ở `TASK_25_2`, cần 3 cái | ✔ | ✗ | ✗ | ✗ |
| 2020 | Mẫu kim loại có ký ức | **27** | 2288 | 362 Hóa thạch Ngọc Rồng | NV 26 b0 — NPC 21 Bà Hạt Mít trao; **dùng** → 3 dòng thoại → tự xóa | | ✗ | ✗ | ✗ |
| 2021 | Mảnh giáp khắc tên | 8 | 10197 | 1066 Mảnh áo | NV 28 b3 — rơi 100% từ boss −37 King Kong ở `TASK_28_3` | | ✗ | ✗ | ✗ |
| 2022 | Thẻ từ phòng thí nghiệm | 8 | 9406 | 987 Đá bảo vệ | NV 29 b0 — NPC 37 Bunma trao; điều kiện vào map 166 | | ✗ | ✗ | ✗ |
| 2023 | Bản thiết kế bản sao | 8 | 12846 | 1560 Rương ngọc rồng | NV 29 — sinh 5–8 `ItemMap` trên map 166, nhặt đủ 5 bản trong 6 phút | ✔ | ✗ | ✗ | ✗ |

> ⚠️ **Cả 5 id 2019–2023 (và 2024–2026 bên dưới) nằm trong mảng hardcode `ItemService.vatphamsk:638`.**
> Đã vá — xem §4 mục 1. Không vá thì mở "Hộp quà giáng sinh" (item 648) sẽ phát ra vật phẩm nhiệm vụ.

### 3.5 Nhóm E — chương 5–6 (20c, NV 32–47)

| id | Tên | TYPE | icon_id | Mượn icon của item | Dùng ở nhiệm vụ | up | B | GD | KG |
|---|---|---|---|---|---|---|---|---|---|
| 2024 | Lõi Ký Ức chưa hoàn chỉnh | **27** | 9650 | 1015 Ngọc rồng Siêu Cấp | NV 45 b1 trao → b3 **dùng**: kiểm tra đủ 7 mảnh 2002–2008 → trừ cả 7 + trừ 2024 → trao 2000 | | ✗ | ✗ | ✗ |
| 2025 | Mảnh Ký Ức Vỡ | 8 | 6467 | 674 Đá ngũ sắc | NV 32 b4–5 — rơi từ mob 81 Tobi ở `TASK_32_4`, cần 3 cái, nộp NPC 70 Bardock (map 160) | ✔ | ✗ | ✗ | ✗ |
| 2026 | Mảnh Ký Ức Đóng Băng | 8 | 8620 | 935 Đá xanh lam | NV 34 b3–5 — rơi ở map 110, chỉ hiện với người ở `TASK_34_3` | | ✗ | ✗ | ✗ |
| 2027 | Mảnh Bùa Babiđây | 8 | 7743 | 861 Hồng ngọc | NV 36 b3 (đường vòng ngoài giờ phó bản) — rơi từ Cadic M ở map 165 | | ✗ | ✗ | ✗ |
| 2028 | Lõi Phép Babiđây | 8 | 5829 | 638 Bình chứa Commeson | NV 37 b3–4 — rơi 100% cho người kết liễu Mabư ở b2 | | ✗ | ✗ | ✗ |
| 2029 | Ống nghiệm Myuu | 8 | 6849 | 727 Siêu thần thủy | NV 46 b4 — rơi từ Heart form 3 (boss −108108) | | ✗ | ✗ | ✗ |

### 3.6 Nhóm F — 2 danh hiệu

| id | Tên | TYPE | icon_id | Mượn icon của item | `part` = idEffect | Dùng ở | B | GD | KG |
|---|---|---|---|---|---|---|---|---|---|
| 2030 | Người Trả Ký Ức | 36 | 11614 | 1293 Cao thủ siêu hạng | **257** | Trao khi xong **NV 47** (nhánh A) | ✗ | ✗ | ✗ |
| 2031 | Kẻ Giữ Hư Không | 36 | 11617 | 1291 Trùm săn Boss | **258** | Trao khi xong **NV 50** (nhánh B) | ✗ | ✗ | ✗ |

Danh hiệu **không phải item trong hành trang** — dòng `item_template` chỉ là hình đại diện trong
shop 26 (NPC 39 Santa). Bản ghi thật nằm ở `data_badges` (2 dòng) và `item_shop` (4 dòng),
cả hai đã có trong file SQL.

**Chỉ số cả hai danh hiệu: 12% Sức đánh + 12% HP + 12% KI** (option 50 / 77 / 103 — ba option duy nhất
`NPoint` thực sự áp dụng). Ngang mức danh hiệu "X-mas" hiện có. **Cố ý bằng nhau** để chọn nhánh là
lựa chọn kể chuyện, không phải lựa chọn sức mạnh.

⚠️ **`idEffect` 257/258 CHƯA CÓ TÀI NGUYÊN.** Đã kiểm tra thực tế:
`data/effdata/DataEffect_257|258` và `data/effect/x1..x4/ImgEffect_257|258.png` **đều không tồn tại**
(260/261 mà 20c đề xuất cũng không có). `DataGame.sendEffectTemplate` lặng lẽ `return` khi thiếu file
⇒ danh hiệu **vẫn cộng chỉ số nhưng không hiện hiệu ứng trên đầu nhân vật**.
**Cần vẽ 10 file** (2 `DataEffect` + 8 `ImgEffect` cho x1–x4) trước khi phát hành.

**Trao vĩnh viễn, không đi qua `BadgesTaskService`** (hàm đó cứng 30 ngày):

```java
// 36500 ngày ~ 100 năm, coi như vĩnh viễn
new BadgesData(player, 257, 36500);          // NV 47
BadgesService.turnOnBadges(player, 257);
```

> ⚠️ **KHÔNG gọi thêm `player.dataBadges.add(...)`** — constructor `BadgesData(Player,int,int)` dòng 39
> đã tự `add` rồi. Đây chính là lỗi đang có ở `BadgesTaskService.updateDoneTask` và
> `ShopService.buyDanhHieu` khiến chỉ số danh hiệu bị nhân đôi.

---

## 4. Code cần vá vì hardcode id ≥ 2000

Mã nguồn có sẵn nhắc tới nhiều id ≥ 2000 **không tồn tại trong DB team2026** (di sản của một bản
server khác). Chừng nào `item_template` chỉ tới 1999 thì các đoạn đó chết lặng
(`IndexOutOfBoundsException` bị `try/catch` nuốt). **Thêm item id ≥ 2000 là chúng sống lại.**

### 4.1 Đã vá — 11 chỗ

| # | File : dòng | Hàm | Vấn đề | Đã làm gì |
|---|---|---|---|---|
| 1 | `services/ItemService.java` : 638 | `vatphamsk` | Mảng chọn ngẫu nhiên chứa **2019–2026, 2036–2040**. Gọi từ `OpenItem648` ⇒ mở "Hộp quà giáng sinh" (item 648) sẽ phát **vật phẩm nhiệm vụ 2019–2026** kèm option ngẫu nhiên | Bỏ toàn bộ 13 id ≥ 2000, giữ 7 id có thật: `{954, 955, 952, 953, 924, 860, 742}` |
| 2 | `services/ItemService.java` : 598 | `randomRac2` | Mảng chứa id **2048** (không có trong DB) | Bỏ id 2048. *(2048 nằm ngoài dải 2000–2031 nên chưa gây hại, nhưng là mìn cho lần mở rộng sau)* |
| 3 | `services_func/UseItem.java` : 878 | `useItem` | `case 2006:` mở form **đổi tên nhân vật**. Id 2006 nay là **"Mảnh Ký Ức 5"** ⇒ bấm "Dùng" vào mảnh ký ức sẽ mở form đổi tên và **trừ mất mảnh** | Bỏ hẳn `case 2006` (DB không có item "thẻ đổi tên" nào) |
| 4 | `services_func/Input.java` : 431 | `doInput` / `CHANGE_NAME_BY_ITEM` | `findItem(itemsBag, 2006)` — trừ "Mảnh Ký Ức 5" làm thẻ đổi tên | Thay bằng hằng số mới `ID_THE_DOI_TEN = -1` (tính năng TẮT). Muốn mở lại: tạo item mới **ngoài dải 2000–2031** rồi đặt id vào hằng số đó |
| 5 | `services_func/Input.java` : 532 | `doInput` / `TANG_NGOC_HONG` | `findItemBag(player, 2002)` — trừ **"Mảnh Ký Ức 1"** làm vé tặng ngọc. Còn gọi `subQuantityItemsBag(null)` khi không có vé | Đổi sang item **718** (Vé tặng ngọc thật, `ConstItem.VE_TANG_NGOC`) + thêm kiểm tra null. *(Nhánh này hiện không có đường vào vì `createFormTangRuby` không ai gọi — vá để phòng gói tin giả)* |
| 6 | `mob/Mob.java` : 1089, 1097 | `dropItemTask` | **Nhóm khác đã code sẵn** drop `2001` (Mảnh Vỡ Hư Không) và `2002` (Kỷ Vật Của Ông) theo id cũ của 21b. Với bảng chốt mới, 2001 = "Vỏ Lõi rỗng" và 2002 = "Mảnh Ký Ức 1" ⇒ **rơi nhầm món** | Đổi **2001 → 2009**, **2002 → 2010** |
| 7 | `map/Zone.java` : 294 | `getItemMapsForPlayer` | Cùng lý do: lọc hiển thị theo id `2001 \|\| 2002` | Đổi **2001 → 2009**, **2002 → 2010** |
| 8 | `shop/ShopService.java` : `showConfirmSellItem` + `sellItem` | | Chặn bán vật phẩm nhiệm vụ — xem §5 | Thêm 2 khối `if (ItemService.isTaskItem(...))` |
| 9 | `services/InventoryService.java` : `throwItem` · `services_func/UseItem.java` : `DO_THROW_ITEM` | | Chặn vứt vật phẩm nhiệm vụ | Thêm 2 khối chặn (cả 2 chặng) |
| 10 | `services/InventoryService.java` : `putItemBody` | | 7 món TYPE 27 mặc được vào **ô 7 (ô pet)** ⇒ rời hành trang ⇒ `subQuantityItemsBag` không tìm thấy ⇒ **kẹt bước nộp** | Chặn mặc mọi id trong dải 2000–2031 |
| 11 | `server/Manager.java` : 629 | `loadDatabase` | `SELECT * FROM item_template LIMIT ? OFFSET ?` **không có `ORDER BY`** — thứ tự đúng chỉ nhờ may mắn | Thêm `ORDER BY id` |

**Hằng số mới dùng chung** — `SRC/src/nro/models/services/ItemService.java`:

```java
public static final int ID_TASK_ITEM_MIN = 2000;
public static final int ID_TASK_ITEM_MAX = 2031;

public static boolean isTaskItem(int templateId) {
    return templateId >= ID_TASK_ITEM_MIN && templateId <= ID_TASK_ITEM_MAX;
}
```

> **Khi mở rộng dải vật phẩm nhiệm vụ về sau, phải sửa `ID_TASK_ITEM_MAX` cùng lúc**,
> nếu không những item mới sẽ bán/vứt/mặc được.

Toàn bộ 11 chỗ vá đã **biên dịch sạch** (`javac` 548 file, không lỗi, không cảnh báo mới).

### 4.2 Chưa vá — dải cấm cho lần mở rộng sau

Những chỗ dưới đây **nằm ngoài dải 2000–2031 nên hiện vẫn chết lặng, không gây hại**.
Chúng sẽ sống lại nếu `item_template` được mở rộng quá id tương ứng.

| File : dòng | Hàm | Id | Chuyện gì xảy ra nếu id đó tồn tại |
|---|---|---|---|
| `services/InventoryService.java` : 842 | `addItemList` | **2074** | Item 2074 được gộp chồng **kể cả khi option khác nhau** |
| `services/InventoryService.java` : 847–850 | `addItemList` | **2048, 2050–2055, 2075** | Các id này gộp **không giới hạn** (bỏ trần 99.999/ô) |
| `services/InventoryService.java` : 1074, 1079 | `findItemTVC` | **2077** | Hàm hiện không có nơi nào gọi ⇒ vô hại, nhưng đừng dùng lại id |
| `services/shenron/SummonDragonNamek.java` : 248, 255, 263 | | **2053** | Điều ước rồng Namếc **tạo thẳng item 2053** |
| `services/RewardService.java` : 1719 | | **2148–2152** | `createNewItem(Util.nextInt(2148, 2152))` |
| `boss/event_trung_thu/NguyetThan.java` : 47 · `NhatThan.java` : 45 | | **2123, 2124** | Boss trung thu rơi `ItemMap` id 2123/2124 |
| `player/NPoint.java` : 665 | `setOutfitFusion` | **2133, 2134** | Cặp cải trang này bật cờ `isGogeta` |
| `database/MrBlue.java` : 428, 871 | `loadPlayer` | **2132** | **Thu hồi (xóa)** item 2132 nếu `createTime` trong 15/03–28/03/2024 |
| `database/MrBlue.java` : 486 | `loadPlayer` | **2322** | **Thu hồi** item 2322 nếu `createTime` trong 06/02–28/06/2025 |
| `player/Charms.java` : 97, 105 | `addTimeCharms` | **2025, 2076** | Vô hại: hàm chỉ chạy cho item **TYPE 13 (bùa)**, mà 2025 của ta là TYPE 8 |

**Danh sách id CẤM DÙNG cho lần mở rộng sau (trừ khi vá code trước):**

```
2036 2037 2038 2039 2040
2048 2050 2051 2052 2053 2054 2055
2074 2075 2076 2077
2123 2124 2132 2133 2134
2148 2149 2150 2151 2152
2322
```

*(2019–2026 đã được giải phóng nhờ mục vá số 1; 2006 và 2002 nhờ mục 3, 4, 5.)*

---

## 5. Chặn bán / giao dịch / ký gửi / vứt — cách đã chọn

### 5.1 `gold = 0` KHÔNG chặn được bán

`ShopService.sellItem`:

```java
int cost = item.template.gold;
...
cost /= 4;
if (cost == 0) { cost = 1; }      // <<<<<< ép giá tối thiểu lên 1 vàng
cost *= quantity;
```

⇒ đặt `gold = 0` thì item **vẫn bán được 1 vàng/cái**. Người chơi lỡ tay bán mất vật phẩm nhiệm vụ
là **kẹt nhiệm vụ**, và danh sách "Đã bán" chỉ giữ **10 món gần nhất**.

Hai cách chặn có sẵn trong code đều **không dùng được**:
- Option 93 "Hạn sử dụng # ngày" chặn bán, nhưng làm item **tự biến mất khi hết hạn** ⇒ mất tiến độ.
- `is_sell` là cột của `item_shop`, không phải `item_template` ⇒ không liên quan.

### 5.2 Cách đã chọn: chặn theo DẢI ID trong code

Dùng chính cơ chế đang chặn id 570 "Rương Gỗ", nhưng thay id đơn bằng dải:

| Hành vi | Chặn ở đâu | Cách |
|---|---|---|
| **Bán cho NPC** | `ShopService.showConfirmSellItem` **và** `ShopService.sellItem` | `if (ItemService.isTaskItem(item.template.id)) { báo "Bạn không thể bán vật phẩm nhiệm vụ"; return; }` — chặn **cả hai chặng** vì client sửa được có thể gửi thẳng gói bán |
| **Vứt** | `UseItem` case `DO_THROW_ITEM` **và** `InventoryService.throwItem` | như trên, thông báo "Không thể bỏ vật phẩm nhiệm vụ." |
| **Mặc vào người** | `InventoryService.putItemBody` | Chặn ngay đầu hàm, trước cả kiểm tra TYPE |
| **Giao dịch** | *(không cần vá)* | TYPE 8 đã bị `Trade.isItemCannotTran` chặn sẵn (`case 8`). 7 món TYPE 27 được chặn bằng **option 30** gắn lúc tạo item |
| **Ký gửi** | *(không cần vá)* | `ConsignShopService.itemCanConsign` chỉ cho TYPE 6/14/15, id 14–20, hoặc option 86/87 ⇒ TYPE 8/27/36 đều không ký gửi được |

**Vì sao chặn theo dải mà không theo từng id**: một hằng số duy nhất
(`ItemService.ID_TASK_ITEM_MIN/MAX`) đúng cho cả 32 món và mọi món thêm sau, không thể quên sót một id.
Đánh đổi: **phải nhớ sửa `ID_TASK_ITEM_MAX` mỗi khi nới dải**.

### 5.3 Vì sao chọn TYPE như vậy

Đọc từ `InventoryService.putItemBody:289-392`, `UseItem.useItem:255+`,
`Trade.isItemCannotTran:220-252`, `ConsignShopService.itemCanConsign:334-345`:

| Nhu cầu | TYPE chọn | Lý do |
|---|---|---|
| Chỉ mang / nộp NPC, không bấm được | **8** | Đúng nghĩa "vật phẩm nhiệm vụ" (`Trade.java` ghi rõ `case 8: //vật phẩm nhiệm vụ`). Không mặc được, không giao dịch, không ký gửi. Vẫn rơi xuống `switch(template.id)` trong `UseItem` nếu sau này cần thêm xử lý |
| Bắt buộc bấm "Dùng" (trigger A12) | **27** + option 30 | Đã có tiền lệ chắc chắn: item 992 TYPE 27 bấm "Dùng" được. TYPE 27 mặc định **giao dịch được** ⇒ bắt buộc option 30; và mặc được vào ô 7 ⇒ đã vá `putItemBody` |
| Danh hiệu | **36** | Cơ chế riêng qua `data_badges` + shop 26 |

**KHÔNG dùng TYPE 11** như 20a đề xuất: TYPE 11 trong server này là **"đồ đeo lưng / flag bag"** —
item mặc vào **ô 8** và `Player.getFlagBag()` lấy `template.part` làm id lá cờ hiển thị trên lưng.
Với `part = -1` client sẽ vẽ lá cờ id −1 ⇒ lỗi hiển thị.

---

## 6. Bảng đối chiếu id cũ → id mới

Dùng khi đọc 20a / 20b / 20c / 21b — những file đó vẫn ghi id cũ.

| Tên vật phẩm | Id trong 20a/20b/20c | Id trong 21b (sau sửa) | **ID CHỐT** |
|---|---|---|---|
| Lõi Hư Không | 2101 | 2101 | **2000** |
| Vỏ Lõi rỗng | 2102 | 2102 | **2001** |
| Mảnh Ký Ức #1 | 2010 (có chỗ ghi 2000/2001) | 2010 | **2002** |
| Mảnh Ký Ức #2 | 2011 | 2011 | **2003** |
| Mảnh Ký Ức #3 | 2012 | 2012 | **2004** |
| Mảnh Ký Ức #4 | 2013 | 2013 | **2005** |
| Mảnh Ký Ức #5 | 2014 | 2014 | **2006** |
| Mảnh Ký Ức #6 | 2015 | 2015 | **2007** |
| Mảnh Ký Ức #7 | 2016 | 2016 | **2008** |
| Mảnh Vỡ Hư Không | 2001 | 2001 | **2009** |
| Kỷ Vật Của Ông | 2002 | 2002 | **2010** |
| Máy Dò Ký Ức | 2003 | 2003 | **2011** |
| Hộp Ký Ức Bị Đánh Cắp | 2004 | 2004 | **2012** |
| Hạt Giống Hy Vọng | 2006 | 2006 | **2013** |
| Vỏ đạn khắc dấu | 2040 | 2045 | **2014** |
| Búa rèn cũ | 2041 | 2041 | **2015** |
| Thẻ tiền thưởng Granola | 2042 | 2042 | **2016** |
| Biên bản truy nã Ngân Hà | 2043 | 2043 | **2017** |
| Máy đo ký ức | 2044 | 2044 | **2018** |
| Lõi năng lượng Android | 2070 | 2070 | **2019** |
| Mẫu kim loại có ký ức | 2071 | 2071 | **2020** |
| Mảnh giáp khắc tên | 2072 | 2072 | **2021** |
| Thẻ từ phòng thí nghiệm | 2073 | 2073 | **2022** |
| Bản thiết kế bản sao | 2074 | 2076 | **2023** |
| Lõi Ký Ức chưa hoàn chỉnh | 2100 | 2100 | **2024** |
| Mảnh Ký Ức Vỡ | 2103 | 2103 | **2025** |
| Mảnh Ký Ức Đóng Băng | 2104 | 2104 | **2026** |
| Mảnh Bùa Babiđây | 2105 | 2105 | **2027** |
| Lõi Phép Babiđây | 2106 | 2106 | **2028** |
| Ống nghiệm Myuu | 2107 | 2107 | **2029** |
| Danh hiệu "Người Trả Ký Ức" | 2120 | 2120 | **2030** |
| Danh hiệu "Kẻ Giữ Hư Không" | 2121 | 2121 | **2031** |

**Những id trong thiết kế cũ đã bị loại bỏ, KHÔNG còn tồn tại**: 2005, 2007, 2008, 2009, 2017,
2018, 2045, 2076, và toàn bộ dải trống 2046–2069, 2077–2099, 2108–2119.
*(2109/2110/2111 trong 20c là `part_template` — tạo hình của NPC 108 Heart — **không phải id vật phẩm**.)*

---

## 7. Việc cần làm khi lên server

Theo thứ tự. Bỏ sót bước nào cũng gây lỗi thấy được ngay.

| # | Việc | Ở đâu | Bỏ qua thì sao |
|---|---|---|---|
| 1 | **Sao lưu DB** | `mysqldump -u root -p team2026 item_template data_badges item_shop > backup_item_$(date +%F).sql` | Không lùi lại được |
| 2 | **Import file patch** | `mysql -u root -p team2026 < SRC/sql/patch/01-vat-pham-moi.sql` | — |
| 3 | **Chạy 5 câu kiểm tra** ở cuối file SQL | Mục (4) của file patch | Nếu 4.1/4.2 sai thì **ĐỪNG khởi động server** — bảng item đã lệch |
| 4 | **TĂNG PHIÊN BẢN DỮ LIỆU ITEM** | `SRC/src/nro/models/data/DataGame.java` **dòng 37**: `public static byte vsItem = 9;` → **`= 10`** | Client dùng bảng item đang **cache**: item mới hiện **tên rỗng, icon trắng, mô tả rỗng**; client cũ có thể **văng khỏi game** khi gặp id vượt mảng nó đang giữ |
| 5 | **Build lại `20.jar`** | `javac -encoding UTF-8 -cp "lib/*" ...` rồi đóng gói | Code vá ở §4 không có tác dụng |
| 6 | **Khởi động lại server** | | Tăng `vsItem` mà không restart = không có tác dụng |
| 7 | **Vẽ 10 file hiệu ứng danh hiệu** | `data/effdata/DataEffect_257`, `DataEffect_258` và `data/effect/x1..x4/ImgEffect_257.png`, `ImgEffect_258.png` | Danh hiệu **"tàng hình"**: cộng chỉ số nhưng không có hiệu ứng trên đầu |

> ✅ **Bước 4 ĐÃ ĐƯỢC LÀM SẴN trong bản giao này** (`vsItem = 10`, kèm comment `// FIX:`).
> Vẫn phải **kiểm tra lại trước khi build**, và **mỗi lần thêm/bớt/sửa dòng `item_template` về sau
> phải tăng `vsItem` thêm 1** (tối đa 127 vì là `byte`).
>
> Chỉ cần tăng **`vsItem`**. `vsData`, `vsMap`, `vsSkill`, `vsRes` **không đổi**, vì chỉ mượn icon sẵn có.
> Nếu thêm file hiệu ứng ở bước 7 thì **không cần** tăng `vsData` — effect được gửi theo yêu cầu
> (`Controller` gói `-66`), không nằm trong gói version.

### Danh sách test tối thiểu sau khi lên

1. Đăng nhập bằng tài khoản **chưa vào từ lúc đổi `vsItem`** → xem có tải lại bảng item không.
2. `/addit 2000` … `/addit 2031` → tên đúng, **icon hiện**, mô tả đúng. Test ở **cả 4 mức zoom x1–x4**.
3. Xem thông tin item → phải hiện dòng option **"Không thể giao dịch"**.
4. Thử **giao dịch** với tài khoản thứ hai → bị từ chối.
5. Thử **bán** ở NPC Bunma → phải hiện "Bạn không thể bán vật phẩm nhiệm vụ" (không phải "1 vàng").
6. Thử **vứt** → "Không thể bỏ vật phẩm nhiệm vụ."
7. Thử **mặc** món TYPE 27 (2000, 2002, 2010, 2013, 2015, 2020, 2024) → "Không thể trang bị vật phẩm nhiệm vụ!"
8. Thử **ký gửi** → item không được liệt kê.
9. Bấm **"Dùng"** trên 7 món TYPE 27 → **xem §8**, đây là điểm phải test kỹ nhất.
10. **Mở "Hộp quà giáng sinh" (item 648) vài chục lần** → không được ra vật phẩm nhiệm vụ nào.
11. Test trên **client bản cũ nhất** mà server còn phục vụ.

---

## 8. Điểm chưa chắc chắn

| # | Điều chưa chắc | Vì sao | Cần làm gì |
|---|---|---|---|
| 1 | **Client có hiện nút "Dùng" cho item TYPE 8 không?** | Không có mã nguồn client trong kho. Phía server thì **cả TYPE 8 và TYPE 27 đều rơi xuống `switch(template.id)`** trong `UseItem.useItem`, nên server không phân biệt. Đây là lý do 7 món bắt buộc bấm "Dùng" vẫn giữ TYPE 27 (có tiền lệ chắc chắn: item 992) thay vì chuyển hết sang TYPE 8 | **Test thật trên client**. Nếu TYPE 8 cũng hiện nút "Dùng" thì nên chuyển cả 7 món sang TYPE 8 và bỏ được option 30 lẫn mục vá `putItemBody` |
| 2 | **Ngưỡng số lượng item template client bản cũ chịu được** | `ItemData` gửi `writeShort` nên server chịu tới 32.767, nhưng client cũ có thể cấp phát mảng cố định | Test trên client bản cũ nhất trước khi phát hành |
| 3 | **Nhánh `TANG_NGOC_HONG` trong `Input.java`** | Logic vốn đã lẫn lộn `inventory.ruby` với `subGem()`. Tôi **chỉ sửa id item** (2002 → 718) và thêm kiểm tra null, **không đụng phần tiền tệ** vì đó là việc của nhóm sửa lỗi kinh tế (24a) | Nhóm 24a rà lại toàn bộ nhánh này |
| 4 | **`ItemService.randomRac2` còn lỗi `Util.nextInt(racs.length - 1)`** | Phần tử **cuối mảng không bao giờ được chọn**. Là lỗi cũ, không liên quan vật phẩm nhiệm vụ | Để nhóm 24a quyết, vì sửa sẽ đổi tỉ lệ rơi đồ của hộp quà |
| 5 | **Icon 11614 / 11617 của 2 danh hiệu trùng với item 1293 / 1291** | Không gây lỗi kỹ thuật (`getItemIdByIcon` chỉ dùng cho `iconSpec` của shop), nhưng người chơi dễ nhầm hai danh hiệu mới với hai danh hiệu cũ | Nên vẽ icon riêng khi vẽ 10 file hiệu ứng ở bước 7 |
| 6 | **`Mob.java` và `Zone.java` đang do nhóm khác sửa song song** | Tôi đã đổi 4 chỗ id (2001→2009, 2002→2010) trong hai file đó. Nếu nhóm kia sửa tiếp cùng vùng có thể xung đột | Báo nhóm đó **đọc §6 trước khi viết thêm id vật phẩm**, và nhắc họ vẫn phải gắn **option 30** ở mọi chỗ tạo item |
| 7 | **Chưa chạy thử SQL trên MySQL thật** | Máy này chưa nạp database `team2026`. Cú pháp, số cột (15), tính liên tục của id và độ dài `description` đã được kiểm bằng script, nhưng chưa `import` thật | Chạy bước 2 + 3 ở §7 trên bản sao DB trước khi làm trên bản chính |

---

*Hết file 25. Đặc tả kỹ thuật gốc: [21b](../3-kiem-tra-can-bang/21b-item-moi-dac-ta.md). File SQL: [`SRC/sql/patch/01-vat-pham-moi.sql`](../../SRC/sql/patch/01-vat-pham-moi.sql).*
