# 21b — Đặc tả VẬT PHẨM MỚI (id ≥ 2000) cho tuyến nhiệm vụ mới

> **Mục đích**: trả lời chính xác ba câu hỏi của chủ dự án — *phải tạo cái gì*, *tạo thế nào cho chuẩn*,
> và *dùng "prompt/quy trình" nào để lần sau tạo item mới mà không làm hỏng game*.
>
> **Phạm vi**: chỉ nói về vật phẩm. Nhiệm vụ/boss/thưởng xem [20](../2-thiet-ke-nhiem-vu-moi/20-thiet-ke-nhiem-vu-moi.md),
> [20a](../2-thiet-ke-nhiem-vu-moi/20a-chi-tiet-nhiem-vu-00-15.md), [20b](../2-thiet-ke-nhiem-vu-moi/20b-chi-tiet-nhiem-vu-16-31.md), [20c](../2-thiet-ke-nhiem-vu-moi/20c-chi-tiet-nhiem-vu-32-47.md).
> Kiểm toán vật phẩm thưởng ở 21a; cân bằng rơi đồ boss ở 21c.
>
> **Nguồn kiểm chứng**: `database team2026.sql` (bảng `item_template`, `item_option_template`, `data_badges`,
> `item_shop`, `tab_shop`, `flag_bag`), mã nguồn trong `SRC/src/nro/**`, và thư mục tài nguyên `SRC/data/icon`,
> `SRC/data/effect`, `SRC/data/effdata`. Mọi số dòng dưới đây là số dòng thật trong bản mã hiện tại.
>
> **Ngày lập**: 17/09/2026. **Không sửa code, không sửa file 20***.

---

## Mục lục

- [0. Cơ sở kỹ thuật đã tự kiểm chứng (đọc trước khi làm bất cứ gì)](#0-cơ-sở-kỹ-thuật-đã-tự-kiểm-chứng-đọc-trước-khi-làm-bất-cứ-gì)
  - [0.1 Bảng `item_template` — 15 cột](#01-bảng-item_template--15-cột)
  - [0.2 Luật SẮT: id vật phẩm phải LIÊN TỤC từ 0](#02-luật-sắt-id-vật-phẩm-phải-liên-tục-từ-0)
  - [0.3 Client nhận gì, không nhận gì](#03-client-nhận-gì-không-nhận-gì)
  - [0.4 Icon lấy từ đâu](#04-icon-lấy-từ-đâu)
  - [0.5 Option không nằm trong `item_template`](#05-option-không-nằm-trong-item_template)
  - [0.6 TYPE nào làm được gì (bảng quyết định)](#06-type-nào-làm-được-gì-bảng-quyết-định)
  - [0.7 Cái gì quyết định "bán được / giao dịch được / ký gửi được"](#07-cái-gì-quyết-định-bán-được--giao-dịch-được--ký-gửi-được)
- [1. Quy trình tạo một item mới cho đúng — checklist 14 bước](#1-quy-trình-tạo-một-item-mới-cho-đúng--checklist-14-bước)
- [2. "Prompt chuẩn" để tạo item mới](#2-prompt-chuẩn-để-tạo-item-mới)
- [3. Đặc tả từng item mới](#3-đặc-tả-từng-item-mới)
  - [3.0 Bảng tổng hợp 32 item](#30-bảng-tổng-hợp-32-item)
  - [3.1 Nhóm A — vật phẩm nhiệm vụ chương 1–2 (20a)](#31-nhóm-a--vật-phẩm-nhiệm-vụ-chương-12-20a)
  - [3.2 Nhóm B — 7 Mảnh Ký Ức (2010–2016)](#32-nhóm-b--7-mảnh-ký-ức-20102016)
  - [3.3 Nhóm C — vật phẩm chương 3–4 (20b, 2040–2074)](#33-nhóm-c--vật-phẩm-chương-34-20b-20402074)
  - [3.4 Nhóm D — vật phẩm chương 5–6 (20c, 2100–2107)](#34-nhóm-d--vật-phẩm-chương-56-20c-21002107)
  - [3.5 Nhóm E — 2 danh hiệu (2120–2121)](#35-nhóm-e--2-danh-hiệu-21202121)
  - [3.6 Câu lệnh INSERT mẫu](#36-câu-lệnh-insert-mẫu)
- [4. Kiểm tra trùng lặp và va chạm](#4-kiểm-tra-trùng-lặp-và-va-chạm)
- [5. Phương án KHÔNG tạo item mới](#5-phương-án-không-tạo-item-mới)
- [6. Danh hiệu 2120 / 2121 — đặc tả riêng](#6-danh-hiệu-2120--2121--đặc-tả-riêng)
- [7. Ghi chú / điểm cần chủ dự án quyết](#7-ghi-chú--điểm-cần-chủ-dự-án-quyết)

---

## 0. Cơ sở kỹ thuật đã tự kiểm chứng (đọc trước khi làm bất cứ gì)

### 0.1 Bảng `item_template` — 15 cột

`database team2026.sql` dòng **4717–4733**:

| # | Cột | Kiểu DB | Server đọc bằng | Ghi chú giới hạn |
|---|---|---|---|---|
| 1 | `id` | int(11) | `rs.getShort("id")` | **short → tối đa 32.767**. Nhưng xem §0.2: còn bị ràng buộc chặt hơn nhiều |
| 2 | `TYPE` | int(11) | `rs.getByte("type")` | **byte → tối đa 127** |
| 3 | `gender` | smallint(6) | `rs.getByte("gender")` | 0 Trái Đất · 1 Namếc · 2 Xayda · **3 = dùng chung** |
| 4 | `NAME` | varchar(255) | `rs.getString` | gửi bằng `writeUTF` |
| 5 | `description` | **varchar(75)** | `rs.getString` | gửi bằng `writeUTF`. **KHÔNG được NULL** (xem bước 9 §1) |
| 6 | `level` | int(11) DEFAULT 0 | `rs.getByte("level")` | byte |
| 7 | `icon_id` | int(11) | `rs.getShort` | short; file `SRC/data/icon/x{1..4}/<icon_id>.png` |
| 8 | `part` | int(11) | `rs.getShort` | short. Với TYPE 11 = id `flag_bag`; với TYPE 23 = id ảnh `mount_<part>_0`; với TYPE 36 (danh hiệu) trong DB hiện tại luôn = `idEffect` |
| 9 | `is_up_to_up` | tinyint(1) | `rs.getBoolean` | 1 = cho phép xếp chồng |
| 10 | `power_require` | int(11) | `rs.getInt` | **chặn cả nút "Dùng"**, xem bước 7 §1 |
| 11 | `gold` | int(11) DEFAULT 0 | `rs.getInt` | giá bán cho NPC = `gold / 4` |
| 12 | `gem` | int(11) DEFAULT 0 | `rs.getInt` | **không gửi xuống client**, chỉ dùng phía server |
| 13 | `head` | int(11) DEFAULT -1 | `rs.getInt` | tạo hình (cải trang TYPE 5) |
| 14 | `body` | int(11) DEFAULT -1 | `rs.getInt` | tạo hình |
| 15 | `leg` | int(11) DEFAULT -1 | `rs.getInt` | tạo hình |

Nơi nạp: `SRC/src/nro/models/server/Manager.java` dòng **629–659** —
`SELECT * FROM item_template LIMIT ? OFFSET ?`, lô 750 dòng, đổ vào `Manager.ITEM_TEMPLATES` (`ArrayList`, khai báo dòng 89).

Số liệu thật trong DB hiện tại: **đúng 2000 dòng, id 0 → 1999, liên tục, không thiếu id nào, không trùng id nào.**
Trong đó **141 dòng là ô trống đặt sẵn** (`NAME = ''`), nằm ở các dải
`1826–1834`, `1836–1839`, `1871–1899`, `1901–1999`, tất cả đều có dạng
`(<id>, 75, 3, '', '', 0, 0, 0, 0, 0, 0, 0, -1, -1, -1)`.
Dòng có tên cuối cùng là **1900 "Ghost Rider"**.

> **Vì sao có 141 dòng rỗng?** Không phải rác. Đó chính là cách server này giữ cho id liên tục — xem §0.2.

### 0.2 Luật SẮT: id vật phẩm phải LIÊN TỤC từ 0

`SRC/src/nro/models/services/ItemService.java` dòng **360–362**:

```java
public Template.ItemTemplate getTemplate(int id) {
    return Manager.ITEM_TEMPLATES.get(id);
}
```

`ITEM_TEMPLATES` là `ArrayList`. `getTemplate(id)` **lấy theo CHỈ SỐ MẢNG, không tra theo id**.
Toàn bộ server đi qua hàm này: `ItemService.createNewItem`, `createItemSetKichHoat`, `otpts`,
`new Item(short)`, `new ItemMap(zone, tempId, ...)` (`SRC/src/nro/models/map/ItemMap.java` dòng 40)…

Hệ quả bắt buộc:

1. **Chỉ số mảng phải bằng id.** Điều này chỉ đúng khi id trong DB liên tục 0,1,2,…,N-1.
2. Nếu bạn `INSERT` id **2001 mà bỏ trống 2000**, thì phần tử ở chỉ số 2000 là item id 2001,
   chỉ số 2001 là item 2002… → **mọi item từ đó trở đi bị lệch một bậc**.
   `getTemplate(2001)` trả về nhầm item, và `getTemplate(id lớn nhất)` ném `IndexOutOfBoundsException`.
3. `Manager` nạp bằng `LIMIT ? OFFSET ?` **không có `ORDER BY`**. Với InnoDB, thứ tự trả về theo khóa chính
   nên hiện tại đúng, nhưng đây là may mắn chứ không phải bảo đảm. Xem khuyến nghị ở §7.

**Kết luận thực hành**: thêm item mới = phải lấp đầy **mọi id** từ 2000 đến id lớn nhất bạn dùng.
Id nào chưa dùng thì chèn **dòng trống** theo đúng mẫu 141 dòng sẵn có (`TYPE 75`, `NAME ''`).

### 0.3 Client nhận gì, không nhận gì

`SRC/src/nro/models/data/ItemData.java`:

| Hàm | Dòng | Gửi gì |
|---|---|---|
| `updateItem(session)` | 12–17 | gọi lần lượt: option template → `arr_head_2_frames` → `updateItemTemplate(session, 750)` → `updateItemTemplate(session, 750, ITEM_TEMPLATES.size())` |
| `updateItemTemplate(session, count)` | 37–63 | lệnh con `1` "reload itemtemplate", gửi **750 item đầu** |
| `updateItemTemplate(session, start, end)` | 65–93 | lệnh con `2` "add itemtemplate", gửi từ chỉ số 750 đến hết |

Mỗi item gửi đúng **9 trường, theo thứ tự chỉ số mảng, KHÔNG gửi id**:

```
type (byte) · gender (byte) · name (UTF) · description (UTF) · level (byte)
· strRequire (int) · iconID (short) · part (short) · isUpToUp (boolean)
```

**Không gửi**: `id`, `gold`, `gem`, `head`, `body`, `leg`.

→ Client dựng lại bảng item bằng **thứ tự nhận được**. Đây là lý do thứ hai (sau §0.2) buộc id phải liên tục:
nếu server có lỗ hổng id, client và server hiểu id item khác nhau → người chơi thấy tên/icon của item khác.

Kích hoạt gửi lại: `SRC/src/nro/models/server/Controller.java` dòng **757** (`case 8: ItemData.updateItem(_session)`),
client chỉ gửi yêu cầu này khi thấy **`DataGame.vsItem` khác với bản nó đang lưu**.
`DataGame.vsItem` hiện = **9** (`SRC/src/nro/models/data/DataGame.java` dòng 37, kiểu `byte`),
được gửi trong `sendVersionGame` dòng 51.

### 0.4 Icon lấy từ đâu

`DataGame.sendIcon(session, id)` dòng **299–316**:

```java
final byte[] icon = FileIO.readFile("data/icon/x" + session.zoomLevel + "/" + id + ".png");
if (icon == null) { return; }   // lặng lẽ bỏ qua
```

Client tự hỏi xin icon qua gói `-67` (`Controller.java` dòng 420–422). Thư mục thật:

| Thư mục | Số file |
|---|---|
| `SRC/data/icon/x1` | 15.121 |
| `SRC/data/icon/x2` | 16.358 |
| `SRC/data/icon/x3` | 16.358 |
| `SRC/data/icon/x4` | 16.478 |

**x1 thiếu hơn 1.300 file so với x2/x3/x4** → phải kiểm tra icon tồn tại ở **cả 4 mức zoom**, không chỉ một.

Cách kiểm tra (chạy từ thư mục dự án):

```bash
for z in x1 x2 x3 x4; do
  [ -f "SRC/data/icon/$z/1421.png" ] && echo "$z OK" || echo "$z THIẾU";
done
```

Đã kiểm tra: **toàn bộ 30 icon mà 20a/20b/20c đề xuất mượn đều có đủ ở x1, x2, x3, x4** — xem §3.0.

### 0.5 Option không nằm trong `item_template`

`item_option_template` chỉ có **2 cột**: `id`, `NAME` (`database team2026.sql` dòng 2160–2163).
Bảng hiện có **251 dòng, id 0 → 250**.
`Manager.java` dòng 675–683 nạp bằng `select id, name from item_option_template`.

Option của một item cụ thể được **sinh trong code lúc tạo item**, không có bảng nào gắn option vào template
(trừ `item_shop_option` cho item bán trong shop). Xem [06 §2.4](../1-he-thong-hien-tai/06-vat-pham.md#24-tạo-item-itemservicejava):
`ItemService.createNewItem()` tạo item **không option**; `InventoryService.addItemList` (dòng 821–824)
nếu thấy danh sách option rỗng thì tự thêm option **73** (dòng trống).

Các option id có liên quan tới vật phẩm nhiệm vụ (tra thật từ DB):

| id | NAME |
|---|---|
| 30 | `Không thể giao dịch` |
| 31 | `Số lượng #` |
| 73 | *(chuỗi rỗng — dòng trắng)* |
| 93 | `Hạn sử dụng # ngày` |
| 50 | `Sức đánh+#%` |
| 77 | `HP+#%` |
| 103 | `KI +#%` |
| 108 | `#% Né đòn` |
| 220 | `Hoàn thành #%` |

### 0.6 TYPE nào làm được gì (bảng quyết định)

Tổng hợp từ `InventoryService.putItemBody` (dòng 289–392), `UseItem.useItem` (dòng 255+),
`Trade.isItemCannotTran` (dòng 220–252), `ConsignShopService.itemCanConsign` (dòng 334–345),
và thống kê TYPE ở [02b §3.1](../1-he-thong-hien-tai/02b-database-du-lieu-template.md#31-thống-kê-theo-type-và-ý-nghĩa-type) / [06 §3](../1-he-thong-hien-tai/06-vat-pham.md#3-các-type-vật-phẩm).

| TYPE | Số item đang có | Mặc được vào ô | Bấm "Dùng" | Giao dịch | Ký gửi | Hợp cho vật phẩm nhiệm vụ? |
|---|---|---|---|---|---|---|
| **8** — Vật phẩm nhiệm vụ | 3 (73 Đùi gà, 75 Đùi heo Xayda, 85 Truyện tranh) | **Không** ("Trang bị không phù hợp!1") | rơi xuống `switch(id)` → có thể thêm case | **KHÔNG** (`case 8` trong `isItemCannotTran`) | **Không** | ✅ **Đúng nhất** |
| 11 — Đeo lưng / flag bag | 190 | **Ô 8**, và `Player.getFlagBag()` dòng 998–1010 lấy `template.part` làm id cờ đeo lưng | mặc vào ô 8 + `sendFlagBag` | KHÔNG | Không | ⚠️ Chỉ khi muốn item hiện trên lưng; `part` phải là id thật trong bảng `flag_bag` |
| 27 — Hỗ trợ / sự kiện / pet | 503 | **Ô 7 (ô pet đi theo)** | `switch(id)` | **CÓ giao dịch được** (trừ id 590) | Không | ⚠️ Dùng được nhưng **mặc định giao dịch được** → bắt buộc gắn option 30 |
| 36 — Danh hiệu | 21 | Không | Không (mua/đổi qua shop Santa) | Không | Không | Dành riêng cho danh hiệu, xem §6 |
| 75 — Sự kiện đời mới | 158 (gồm 141 dòng trống) | Không | `switch(id)` | Không | Không | Dùng cho **dòng trống lấp chỗ** |

### 0.7 Cái gì quyết định "bán được / giao dịch được / ký gửi được"

**Giao dịch** — `SRC/src/nro/models/services_func/Trade.java` dòng **220–252**, hàm `isItemCannotTran`:

1. Item có option **30** → cấm giao dịch (bất kể TYPE).
2. id 454, 921 → cấm.
3. TYPE ∈ {5, 6, 7, **8**, **11**, 13, 22, 23, 24, 28, 31, 32} → cấm.
4. **TYPE 27 → CHO PHÉP** (chỉ id 590 bị cấm).
5. Còn lại → cho phép.

**Ký gửi** — `SRC/src/nro/models/shop_ky_gui/ConsignShopService.java` dòng **334–345**, `itemCanConsign`:
chỉ cho ký gửi khi có option 86/87, hoặc TYPE ∈ {6, 14, 15}, hoặc id 14–20.
→ **Mọi item TYPE 8 / 11 / 27 / 36 đều không ký gửi được.**

**Bán cho NPC** — `SRC/src/nro/models/shop/ShopService.java` dòng **1065–1130**, `sellItem`:

```java
int cost = item.template.gold;
if (item.template.id == 457) { quantity = 1; } else { cost /= 4; }
if (cost == 0) { cost = 1; }      // <<<<<<
cost *= quantity;
```

> ⚠️ **ĐIỂM QUAN TRỌNG NHẤT VỀ `gold`**
> - Đặt `gold > 0` ⇒ item **bán lại được với giá `gold / 4`** ⇒ trở thành **nguồn in vàng**.
>   Ví dụ nếu bạn vô ý cho "Mảnh Ký Ức" `gold = 500.000.000` như Thỏi vàng 457, mỗi mảnh bán được
>   125 triệu vàng, và tuyến nhiệm vụ phát ra hàng chục nghìn mảnh ⇒ lạm phát tức thì.
> - Đặt `gold = 0` **KHÔNG chặn được việc bán** — vì dòng `if (cost == 0) cost = 1;` vẫn cho bán
>   với giá **1 vàng × số lượng**. Nghĩa là người chơi vẫn **bán mất vật phẩm nhiệm vụ**, và kẹt nhiệm vụ.
> - Chỉ có **hai** cách chặn bán thật sự trong code hiện tại:
>   (a) id nằm trong danh sách cứng như **570 Rương Gỗ** (`sellItem` dòng 1081–1084 và
>       `showConfirmSellItem` dòng 1026–1029); hoặc
>   (b) item có **option 93 "Hạn sử dụng # ngày"** với param > 0 → thông báo
>       "Bạn không thể bán vật phẩm có hạn sử dụng" (`sellItem` dòng 1085–1088, qua `InventoryService.getParam` dòng 961).
>       Nhưng option 93 làm item **tự biến mất khi hết hạn** (`ItemService.isOutOfDateTime` dòng 405–425) —
>       **không dùng được cho vật phẩm nhiệm vụ dài hơi**.
> - An ủi duy nhất: item bán đi được đẩy vào danh sách **"Đã bán"** (`BuyBackService.addItem`, giữ **10 món gần nhất**)
>   nên người chơi còn cơ hội mua lại. Nhưng chỉ 10 món, và mất phí.

**Vứt** — hai chặng: `UseItem.getItem` case `DO_THROW_ITEM` dòng **209–226** chặn map 21/22/23 (nhà) và item 570;
`InventoryService.throwItem` dòng **116–136** chặn lại item 570 và 457.
Vật phẩm nhiệm vụ mới **vứt được và mất hẳn** (không rơi ra đất). Cần chặn thủ công nếu muốn an toàn.

---

## 1. Quy trình tạo một item mới cho đúng — checklist 14 bước

> Làm theo đúng thứ tự. Cột cuối là **cái gì hỏng nếu làm sai bước đó**.

### Bước 1 — Chốt id, và lấp đầy mọi id ở giữa

- Id lớn nhất đang có trong DB: **1999**. Id mới bắt đầu từ **2000**.
- Viết ra danh sách id thật sẽ dùng, rồi **bổ sung dòng trống cho MỌI id còn thiếu** giữa 2000 và id lớn nhất.
- Mẫu dòng trống (sao y 141 dòng đã có sẵn trong DB):
  `(<id>, 75, 3, '', '', 0, 0, 0, 0, 0, 0, 0, -1, -1, -1)`

| Sai thế nào | Hậu quả |
|---|---|
| Bỏ trống id ở giữa (ví dụ có 2001 nhưng không có 2000) | `ItemService.getTemplate(id)` trả **nhầm item**; client hiển thị sai tên/icon toàn bộ item phía sau; món cuối cùng gây `IndexOutOfBoundsException` khi có người nhặt/nhận. **Đây là lỗi nghiêm trọng nhất.** |
| Dùng id > 32767 | `rs.getShort("id")` tràn số → id âm → hỏng toàn bộ |
| Dùng id đã có (0–1999) | `INSERT` báo lỗi khóa chính (may mắn), hoặc `REPLACE` làm mất item cũ của người chơi |

### Bước 2 — Chọn TYPE

| Nhu cầu | TYPE nên dùng |
|---|---|
| Chỉ nhặt / mang / đưa NPC, không bấm được | **8** |
| Phải bấm "Dùng" để kích hoạt (trigger A12 `checkDoneTaskUseItem`) | **27** + bắt buộc option 30 |
| Phải hiện trên lưng nhân vật | **11**, và `part` = id có thật trong bảng `flag_bag` |
| Danh hiệu | **36** (xem §6) |
| Dòng trống lấp chỗ | **75** |

| Sai thế nào | Hậu quả |
|---|---|
| Đặt TYPE 27 mà quên option 30 | Người chơi **giao dịch được vật phẩm nhiệm vụ** (Trade.java cho phép TYPE 27) → mua bán chợ đen, phá tiến độ |
| Đặt TYPE 27 cho item không nên mặc | Người chơi **mặc được vào ô 7 (ô pet)** (`putItemBody` dòng 351–353) → item rời khỏi hành trang → `subQuantityItemsBag` không tìm thấy → **kẹt bước nộp nhiệm vụ** |
| Đặt TYPE 11 với `part = -1` | Item mặc vào ô 8, `getFlagBag()` trả **-1** → client vẽ cờ đeo lưng không tồn tại → có thể **crash/lỗi hiển thị** |
| Đặt TYPE > 127 | `rs.getByte("type")` tràn số |
| Đặt TYPE lạ (ví dụ 40) | Không mặc được, không dùng được, không giao dịch được — thực ra vẫn chạy, nhưng **không ai kiểm soát được về sau** |

### Bước 3 — Chọn icon

- Mượn `icon_id` của một item đã có ⇒ **không phải thêm file ảnh**.
- Kiểm tra bằng lệnh ở §0.4 — phải **có đủ ở cả x1, x2, x3, x4**.
- Nếu muốn icon riêng: thêm file `<id>.png` vào **cả 4** thư mục `SRC/data/icon/x1..x4`, kích thước theo đúng tỉ lệ zoom của các file lân cận.

| Sai thế nào | Hậu quả |
|---|---|
| Icon không tồn tại | `sendIcon` `return` lặng lẽ → **ô hành trang trống trơn** (item vô hình, vẫn nhặt/dùng được nhưng người chơi không nhìn thấy) |
| Icon chỉ có ở x2/x3/x4 mà thiếu x1 | Người chơi dùng zoom 1 (máy yếu / màn nhỏ) thấy item vô hình, người khác thấy bình thường → bug rất khó tái hiện |
| Icon trùng với item khác | Không sao về mặt kỹ thuật (`getItemIdByIcon` chỉ dùng cho `iconSpec` của shop, `ShopService.java` dòng 971), nhưng **người chơi nhầm lẫn hai item** |

### Bước 4 — Đặt `gold`

- Vật phẩm nhiệm vụ: **`gold = 0`**. Không có ngoại lệ.
- Không bao giờ chép `gold` từ item gốc mình mượn icon.

| Sai thế nào | Hậu quả |
|---|---|
| `gold > 0` | Người chơi bán lại được `gold/4` mỗi cái ⇒ **nguồn in vàng tỉ lệ thuận với số lượng nhiệm vụ phát ra**. Với 48 nhiệm vụ × hàng nghìn người chơi, đây là rủi ro kinh tế lớn nhất của cả bản cập nhật |
| `gold = 0` nhưng tưởng là đã chặn bán | **Vẫn bán được 1 vàng/cái** → người chơi lỡ tay bán mất vật phẩm nhiệm vụ → kẹt. Phải chặn riêng (xem bước 12) |

### Bước 5 — Đặt `gem`

- `gem = 0`. Cột này **không gửi xuống client**; phía server nó chỉ ảnh hưởng khi item được bán trong shop bằng ngọc.

| Sai thế nào | Hậu quả |
|---|---|
| `gem > 0` cho item nhiệm vụ | Nếu về sau ai đó thêm item vào shop, nó có giá ngọc bất ngờ |

### Bước 6 — Đặt `part`

| TYPE | `part` phải là |
|---|---|
| 8, 27, 36 (không đeo) | **-1** (theo đúng thông lệ của 992, 796, 674… trong DB) |
| 11 (đeo lưng) | id có thật trong bảng `flag_bag` |
| 23 (thú cưỡi) | số sao cho tồn tại ảnh `mount_<part>_0` (`Manager.java` dòng 708–711) |
| 36 (danh hiệu) | **= `idEffect` của dòng `data_badges` tương ứng** (thông lệ DB: item 1291 `part=220`, 1293 `part=222`) |

| Sai thế nào | Hậu quả |
|---|---|
| TYPE 11 mà `part = -1` | Cờ đeo lưng id -1 → lỗi hiển thị phía client |
| TYPE 23 mà `part` không có ảnh mount | Item không vào `MAP_MOUNT_NUM` → cưỡi không hiện hình |

### Bước 7 — Đặt `power_require`

- Vật phẩm nhiệm vụ: **`power_require = 0`**.
- `UseItem.useItem` (dòng 267) bọc toàn bộ nhánh xử lý trong
  `if (item.template.strRequire <= pl.nPoint.power)`, ngược lại báo "Sức mạnh không đủ yêu cầu".

| Sai thế nào | Hậu quả |
|---|---|
| `power_require > 0` cho item nhiệm vụ chương 1 | Tân thủ **không bấm "Dùng" được** ⇒ trigger A12 `checkDoneTaskUseItem` (`TaskService.java` dòng 305) không bao giờ chạy ⇒ **kẹt nhiệm vụ vĩnh viễn** |
| `power_require > 0` cho đồ mặc | `putItemBody` dòng 322–332 chặn mặc |

### Bước 8 — Đặt `level`, `is_up_to_up`, `gender`, `head/body/leg`

- `level`: vật phẩm nhiệm vụ đặt **1** (theo thông lệ 992, 796, 674…); dòng trống đặt 0.
- `is_up_to_up`: **1** nếu cần gom nhiều cái vào một ô (ví dụ "nhặt 5 Vỏ đạn"); **0** nếu chỉ có 1 cái.
- `gender`: **3** (dùng chung). Nếu đặt 0/1/2 thì `putItemBody` dòng 305–310 chặn người khác hành tinh mặc, và
  `TabShopDanhHieu` dòng 28 lọc theo `gender`.
- `head/body/leg`: **-1** cho mọi thứ không phải cải trang.

| Sai thế nào | Hậu quả |
|---|---|
| `is_up_to_up = 0` cho item cần nhặt nhiều cái | Mỗi cái chiếm **một ô hành trang** riêng; hành trang tân thủ chỉ 30 ô ⇒ hết ô ⇒ `addItemList` trả `false` ⇒ item rơi vào hư không, người chơi **không nhặt được** |
| `is_up_to_up = 1` nhưng mỗi lần tạo gắn option khác nhau | `addItemList` dòng 842 yêu cầu **danh sách option giống hệt** (`checkListsEqual`) mới gộp ⇒ vẫn tách ô |
| `gender` ≠ 3 | Chỉ một hành tinh nhận được item ⇒ hai hành tinh còn lại kẹt nhiệm vụ |

### Bước 9 — Viết `description` (≤ 75 ký tự, KHÔNG NULL)

- Cột là `varchar(75)`. Hiện **không có dòng nào trong DB để NULL**.
- `ItemData` gửi `msg.writer().writeUTF(itemTemplate.description)`.

| Sai thế nào | Hậu quả |
|---|---|
| Để `NULL` | `writeUTF(null)` ném `NullPointerException`; khối `try/catch` ở `ItemData` dòng 60–62 chỉ in stack trace → **gói tin item template không bao giờ được gửi** → client **treo ở màn hình tải dữ liệu**, không vào được game |
| Viết > 75 ký tự | MySQL cắt cụt (hoặc báo lỗi ở chế độ STRICT) → mô tả cụt lủn |
| Dùng ký tự xuống dòng `\r\n` | Chấp nhận được (item 956 đang có), nhưng làm vỡ khung mô tả ở client cũ |

### Bước 10 — Chuẩn bị option sẽ gắn khi tạo item (nếu có)

Option **không** khai trong `item_template`. Phải thêm ở **mọi chỗ sinh ra item**:

```java
Item it = ItemService.gI().createNewItem((short) 2001, 1);
it.itemOptions.add(new Item.ItemOption(30, 0));   // Không thể giao dịch
InventoryService.gI().addItemBag(player, it);
InventoryService.gI().sendItemBags(player);
```

| Sai thế nào | Hậu quả |
|---|---|
| Quên option 30 ở **một** trong các chỗ tạo item (thưởng nhiệm vụ / rơi từ quái / rơi từ boss / NPC tặng) | Item tạo ở chỗ đó **giao dịch được**, chỗ khác thì không → item cùng id nhưng khác option ⇒ `addItemList` **không gộp chồng được** ⇒ tốn ô hành trang, và người chơi phát hiện lỗ hổng |
| Gắn option 93 (hạn sử dụng) cho vật phẩm nhiệm vụ | Item **tự biến mất** khi qua ngày (`isOutOfDateTime` dòng 405–425) ⇒ mất tiến độ |

### Bước 11 — Chạy SQL, sao lưu trước

```bash
mysqldump -u root -p <db> item_template > backup_item_template_$(date +%F).sql
```

Chạy `INSERT` (xem §3.6), rồi **kiểm tra lại tính liên tục**:

```sql
SELECT COUNT(*) AS so_dong, MIN(id) AS nho_nhat, MAX(id) AS lon_nhat FROM item_template;
-- so_dong phải = lon_nhat + 1 và nho_nhat phải = 0
SELECT a.id + 1 AS id_bi_thieu
FROM item_template a
LEFT JOIN item_template b ON b.id = a.id + 1
WHERE b.id IS NULL AND a.id < (SELECT MAX(id) FROM item_template);
-- phải trả về 0 dòng
```

### Bước 12 — Vá code bảo vệ (nên làm cùng lúc)

Ba chỗ nên sửa để item nhiệm vụ không bị mất oan (mỗi chỗ 2–4 dòng):

| File | Hàm | Việc cần thêm |
|---|---|---|
| `shop/ShopService.java` dòng 1026 & 1081 | `showConfirmSellItem`, `sellItem` | Chặn bán id thuộc dải vật phẩm nhiệm vụ, giống cách đang chặn id 570 |
| `services_func/UseItem.java` dòng 209–226 **và** `services/InventoryService.java` dòng 116–136 | `getItem` case `DO_THROW_ITEM`, `throwItem` | Chặn vứt id thuộc dải vật phẩm nhiệm vụ (chặn ở cả hai chặng, giống cách đang chặn id 570) |
| `services/InventoryService.java` dòng 296–300 | `putItemBody` | Chặn mặc TYPE 27 nếu id thuộc dải vật phẩm nhiệm vụ (giữ item trong hành trang) |

### Bước 13 — Tăng phiên bản data client

`SRC/src/nro/models/data/DataGame.java` dòng 37: `public static byte vsItem = 9;` → đổi thành **10**.

| Sai thế nào | Hậu quả |
|---|---|
| Không tăng `vsItem` | Client **dùng bảng item cũ đang cache**: item mới hiện **tên rỗng / icon trắng / mô tả rỗng**, và với client cũ hơn có thể **văng khỏi game** khi gặp id vượt kích thước mảng nó đang giữ. Người chơi mới cài thì thấy đúng, người chơi cũ thì sai → báo lỗi loạn |
| Tăng `vsItem` nhưng không khởi động lại server | Không có tác dụng |
| Tăng vượt 127 | `writeByte` tràn số |

Chỉ cần tăng **`vsItem`**. `vsData` (ảnh/effect), `vsMap`, `vsSkill`, `vsRes` **không cần đổi** nếu bạn chỉ mượn icon sẵn có
và không thêm hiệu ứng mới. Nếu có thêm file `data/effect/*` cho danh hiệu (§6) thì phải tính thêm.

### Bước 14 — Khởi động lại và test

Danh sách test tối thiểu cho **mỗi** item mới:

1. Đăng nhập bằng tài khoản **chưa từng vào từ lúc đổi `vsItem`** → xem có tải lại bảng item không.
2. `/addit <id>` (lệnh admin, xem [15](../1-he-thong-hien-tai/15-lenh-admin-gm.md)) → item vào hành trang: **tên đúng, icon hiện, mô tả đúng**.
3. Rê chuột/bấm xem thông tin → dòng option hiển thị đúng (option 30 phải hiện "Không thể giao dịch").
4. Thử **giao dịch** với tài khoản thứ hai → phải bị từ chối.
5. Thử **bán** ở NPC Bunma → kiểm tra thông báo giá (nếu chưa vá bước 12 sẽ thấy "1 vàng").
6. Thử **ký gửi** → item không được liệt kê.
7. Thử **vứt** → đúng như thiết kế.
8. Nhặt item từ đất (nếu là item rơi) → `checkDoneTaskPickItem` chạy đúng.
9. Bấm **"Dùng"** (nếu là item dùng được) → `checkDoneTaskUseItem` chạy đúng.
10. Test ở **cả 4 mức zoom** (x1 → x4) để bắt lỗi icon thiếu.
11. Test trên **client bản cũ** (`session.version` thấp) nếu server còn phục vụ bản cũ.

---

## 2. "Prompt chuẩn" để tạo item mới

> Chép nguyên khối dưới đây, điền vào, đưa cho người/AI làm item. Điền **đủ 18 mục** thì không thể làm sai.

```text
=== YÊU CẦU TẠO VẬT PHẨM MỚI — SERVER NGỌC RỒNG TEAMOBI2026 ===

Trước khi làm, bắt buộc đọc: docs/21b-item-moi-dac-ta.md §0 (cơ sở kỹ thuật) và §1 (checklist 14 bước).
Bắt buộc tuân thủ: id trong item_template phải LIÊN TỤC, không được để lỗ hổng.

1.  TÊN HIỂN THỊ (NAME, ≤ 40 ký tự cho đẹp):
2.  MÔ TẢ (description, ≤ 75 ký tự, KHÔNG để trống, KHÔNG để NULL):
3.  CÔNG DỤNG trong game (một câu, người chơi hiểu được):
4.  TYPE đề xuất (8 = chỉ mang/nộp · 27 = bấm Dùng được · 11 = đeo lưng · 36 = danh hiệu):
5.  BẤM "DÙNG" ĐƯỢC KHÔNG?   [ ] Có  → khi dùng thì xảy ra chuyện gì: ..................
                              [ ] Không
6.  GIAO DỊCH ĐƯỢC KHÔNG?    [ ] Có   [ ] Không  (nếu Không và TYPE = 27 thì BẮT BUỘC gắn option 30)
7.  KÝ GỬI ĐƯỢC KHÔNG?       [ ] Có   [ ] Không  (mặc định: TYPE 8/11/27/36 đều KHÔNG ký gửi được)
8.  BÁN CHO NPC ĐƯỢC KHÔNG?  [ ] Có, giá gốc gold = ........ (người chơi thu về gold/4)
                              [ ] Không → phải vá ShopService.sellItem + showConfirmSellItem
    (Lưu ý: gold = 0 KHÔNG chặn bán — vẫn bán được 1 vàng/cái.)
9.  VỨT ĐƯỢC KHÔNG?          [ ] Có   [ ] Không → phải vá InventoryService.throwItem
10. XẾP CHỒNG:  is_up_to_up = [ ] 1 (gộp ô)  [ ] 0 (mỗi cái một ô)
                Số lượng tối đa dự kiến người chơi giữ cùng lúc: ........
                (Trần mặc định 99.999/ô; muốn không giới hạn phải thêm id vào
                 InventoryService.addItemList dòng 847–850.)
11. ICON MƯỢN CỦA ITEM NÀO?  item id ........ ("........"), icon_id = ........
    Đã kiểm tra tồn tại ở SRC/data/icon/x1, x2, x3, x4?  [ ] Rồi
12. CÁC CỘT CÒN LẠI:
    gender = ........ (3 = dùng chung)      level = ........
    part = ........ (-1 nếu không đeo)      power_require = ........ (0 cho vật phẩm nhiệm vụ)
    gem = ........ (0)                      head/body/leg = -1 / -1 / -1
13. OPTION CẦN GẮN KHI TẠO ITEM (tra id thật trong item_option_template):
    - option ........ param ........   (ví dụ: 30 / 0 = Không thể giao dịch)
    Liệt kê ĐẦY ĐỦ mọi chỗ trong code sẽ tạo ra item này, để gắn option giống hệt nhau ở mọi chỗ:
    ............................................................
14. XUẤT HIỆN Ở ĐÂU?
    - Nhiệm vụ / bước: ..........  (hằng ConstTask: ..........)
    - Boss / quái rơi ra: ..........  (tỉ lệ: ........%)
    - NPC tặng: ..........
    - Shop: ..........
15. SỐ LƯỢNG DỰ KIẾN PHÁT RA (ước lượng/người chơi và ước lượng/toàn server mỗi ngày):
16. ITEM BỊ TIÊU ĐI BẰNG CÁCH NÀO? (nộp NPC? dùng là mất? hợp nhất? giữ mãi làm kỷ vật?)
17. ID ĐỀ XUẤT: ........
    - Đã kiểm tra chưa tồn tại trong item_template?           [ ] Rồi
    - Đã kiểm tra không trùng id hardcode trong SRC/src?      [ ] Rồi  (danh sách cấm: §4.3 của 21b)
    - Các id còn trống ở giữa sẽ được lấp bằng dòng trống?    [ ] Rồi, gồm các id: ........
18. PHƯƠNG ÁN THAY THẾ nếu không muốn đụng data client:
    dùng item sẵn có id ........ ("........") thay cho item này. Rủi ro đã chấp nhận: ........

=== ĐẦU RA MONG MUỐN ===
a) Câu INSERT đầy đủ 15 cột cho item mới + các dòng trống lấp chỗ.
b) Câu SQL kiểm tra tính liên tục của id (§1 bước 11).
c) Danh sách chính xác các file/hàm/dòng code cần sửa, kèm đoạn code thêm vào.
d) Giá trị mới của DataGame.vsItem.
e) Kịch bản test 11 mục theo §1 bước 14.
```

---

## 3. Đặc tả từng item mới

### 3.0 Bảng tổng hợp 32 item

Gom đủ từ [20a §B](../2-thiet-ke-nhiem-vu-moi/20a-chi-tiet-nhiem-vu-00-15.md), [20b §5.2](../2-thiet-ke-nhiem-vu-moi/20b-chi-tiet-nhiem-vu-16-31.md) và [20c §6](../2-thiet-ke-nhiem-vu-moi/20c-chi-tiet-nhiem-vu-32-47.md),
theo bảng chia dải đã chuẩn hóa ở [20 §9.2b](../2-thiet-ke-nhiem-vu-moi/20-thiet-ke-nhiem-vu-moi.md).

| id | Tên | File | TYPE thiết kế đề xuất | **TYPE khuyến nghị** | Icon mượn (item gốc) | Icon có đủ x1–x4 |
|---|---|---|---|---|---|---|
| 2001 | Mảnh Vỡ Hư Không | 20a | 11 | **8** | 1421 (225 Mảnh đá vụn) | ✅ |
| 2002 | Kỷ Vật Của Ông | 20a | 11 | **27** (phải dùng được) | 9067 (992 Nhẫn thời không sai lệch) | ✅ |
| 2003 | Máy Dò Ký Ức | 20a | 11 | **8** | 1089 (12 Rada cấp 1) | ✅ |
| 2004 | Hộp Ký Ức Bị Đánh Cắp | 20a | 11 | **8** | 7222 (796 Hộp Capsule) | ✅ |
| 2006 | Hạt Giống Hy Vọng | 20a | 27 | **27** | 241 (13 Đậu thần cấp 1) | ✅ |
| 2010 | Mảnh Ký Ức #1 | 20a | 11 | **27** (dùng ở NV 40) | 419 (14 Ngọc Rồng 1 sao) | ✅ |
| 2011 | Mảnh Ký Ức #2 | 20a | 11 | **8** | 420 (15 Ngọc Rồng 2 sao) | ✅ |
| 2012 | Mảnh Ký Ức #3 | 20b | 11 | **8** | 421 (16 Ngọc Rồng 3 sao) | ✅ |
| 2013 | Mảnh Ký Ức #4 | 20b | 11 | **8** | 422 (17 Ngọc Rồng 4 sao) | ✅ |
| 2014 | Mảnh Ký Ức #5 | 20c | 27 | **8** | 423 (18 Ngọc Rồng 5 sao) | ✅ |
| 2015 | Mảnh Ký Ức #6 | 20c | 27 | **8** | 424 (19 Ngọc Rồng 6 sao) | ✅ |
| 2016 | Mảnh Ký Ức #7 | 20c | 27 | **8** | 425 (20 Ngọc Rồng 7 sao) | ✅ |
| **2040** ⚠️ | Vỏ đạn khắc dấu | 20b | 27 | **8** — *đổi id sang 2045* | 1421 (225 Mảnh đá vụn) | ✅ |
| 2041 | Búa rèn cũ | 20b | 27 | **27** | 1417 (223 Đá Titan) | ✅ |
| 2042 | Thẻ tiền thưởng Granola | 20b | 27 | **8** | 5428 (611 Bản đồ kho báu) | ✅ |
| 2043 | Biên bản truy nã Ngân Hà | 20b | 27 | **8** | 5428 (611 Bản đồ kho báu) | ✅ |
| 2044 | Máy đo ký ức | 20b | 27 | **8** | 6467 (674 Đá ngũ sắc) | ✅ |
| 2070 | Lõi năng lượng Android | 20b | 27 | **8** | 8620 (935 Đá xanh lam) | ✅ |
| 2071 | Mẫu kim loại có ký ức | 20b | 27 | **27** | 2288 (362 Hóa thạch Ngọc Rồng) | ✅ |
| 2072 | Mảnh giáp khắc tên | 20b | 27 | **8** | 10197 (1066 Mảnh áo) | ✅ |
| 2073 | Thẻ từ phòng thí nghiệm | 20b | 27 | **8** | 9406 (987 Đá bảo vệ) | ✅ |
| **2074** ⚠️ | Bản thiết kế bản sao | 20b | 27 | **8** — *đổi id sang 2076* | 12846 (1560 Rương ngọc rồng) | ✅ |
| 2100 | Lõi Ký Ức chưa hoàn chỉnh | 20c | 27 | **27** | 9650 (1015 Ngọc rồng Siêu Cấp) | ✅ |
| 2101 | Lõi Hư Không | 20c | 27 | **27** | 2321 (378 Ngọc rồng 7 sao đen) | ✅ |
| 2102 | Vỏ Lõi rỗng | 20c | 27 | **8** | 12846 (1560 Rương ngọc rồng) | ✅ |
| 2103 | Mảnh Ký Ức Vỡ | 20c | 27 | **8** | 6467 (674 Đá ngũ sắc) | ✅ |
| 2104 | Mảnh Ký Ức Đóng Băng | 20c | 27 | **8** | 8620 (935 Đá xanh lam) | ✅ |
| 2105 | Mảnh Bùa Babiđây | 20c | 27 | **8** | 7743 (861 Hồng ngọc) | ✅ |
| 2106 | Lõi Phép Babiđây | 20c | 27 | **8** | 5829 (638 Bình chứa Commeson) | ✅ |
| 2107 | Ống nghiệm Myuu | 20c | 27 | **8** | 6849 (727 Siêu thần thủy) | ✅ |
| 2120 | Người Trả Ký Ức | 20c | 36 | **36** | 11614 (1293 Cao thủ siêu hạng) | ✅ |
| 2121 | Kẻ Giữ Hư Không | 20c | 36 | **36** | 11617 (1291 Trùm săn Boss) | ✅ |

**Tổng: 32 item thật.** Cộng thêm **90 dòng trống** để lấp kín dải 2000–2121 (§3.6) ⇒ `item_template` sẽ có **2122 dòng, id 0–2121**.

> ⚠️ Hai id **2040** và **2074** **va chạm với id đã bị hardcode trong mã nguồn** — xem §4.3.
> Khuyến nghị đổi sang **2045** và **2076**. Trong các bảng chi tiết dưới đây tôi giữ id gốc do 20b đề xuất
> và ghi rõ id thay thế.

**Vì sao khuyến nghị TYPE 8 thay cho TYPE 11 / 27 mà thiết kế đề xuất:**

- **TYPE 11 là "đeo lưng / flag bag"**, không phải "vật phẩm nhiệm vụ". Item TYPE 11 mặc được vào **ô 8**
  và `Player.getFlagBag()` (dòng 998–1010) lấy `template.part` làm id lá cờ hiển thị trên lưng.
  Với `part = -1` như 20a đề xuất, client nhận id cờ -1 → lỗi hiển thị.
  (Nhãn "vật phẩm nhiệm vụ" ở comment `Trade.java` dòng 238 là **case 8**, không phải case 11.)
- **TYPE 27 mặc định GIAO DỊCH ĐƯỢC** (`Trade.isItemCannotTran` dòng 231–235) và **mặc được vào ô 7 (ô pet)**.
- **TYPE 8** là đúng nghĩa: không mặc được, không giao dịch được, không ký gửi được, và vẫn rơi xuống
  `switch(item.template.id)` trong `UseItem` nên **vẫn thêm case "dùng" được nếu cần**.
- Ngoại lệ: 5 item **bắt buộc bấm "Dùng"** (2002, 2006, 2041, 2071, 2100, 2101, 2010) nên giữ **TYPE 27**
  cho chắc chắn client hiện nút "Dùng" (đã có tiền lệ: item 992 TYPE 27 dùng được), **kèm option 30**.

### 3.1 Nhóm A — vật phẩm nhiệm vụ chương 1–2 (20a)

#### 2001 — Mảnh Vỡ Hư Không

| id | TYPE | gender | NAME | description | level | icon_id | part | is_up_to_up | power_require | gold | gem | head | body | leg |
|---|---|---|---|---|---|---|---|---|---|---|---|---|---|---|
| 2001 | 8 | 3 | Mảnh Vỡ Hư Không | Mảnh vỡ lạ rơi ra từ quái. Mang về cho người cần nó. | 1 | 1421 | -1 | 1 | 0 | 0 | 0 | -1 | -1 | -1 |

- **Option gắn khi tạo**: `30` (Không thể giao dịch), param 0.
- **Giao dịch / ký gửi / bán**: TYPE 8 ⇒ `Trade.isItemCannotTran` `case 8` chặn giao dịch;
  `ConsignShopService.itemCanConsign` không liệt kê TYPE 8 ⇒ không ký gửi;
  **bán được 1 vàng** (do `cost == 0 → 1`) ⇒ **cần vá bước 12**.
- **Cách nhận**: rơi từ quái (`Mob.dropItemTask`, mẫu Đùi gà 73 ở dòng 1076–1082) khi người chơi đang ở
  `TASK_2_2`, với `mob.tempId ∈ {1,2,3}`. Item gắn chủ (`playerId`) để chỉ chủ nhân thấy —
  mẫu item 726 ở `Zone.getItemMapsForPlayer` dòng 299–301.
- **Cách tiêu đi**: `checkDoneTaskPickItem` `case 2001 → TASK_2_2`; item bị trừ ở bước nộp bằng
  `InventoryService.subQuantityItemsBag`.
- **Thay thế nếu không tạo item mới**: item **225 Mảnh đá vụn** (TYPE 15).

#### 2002 — Kỷ Vật Của Ông

| id | TYPE | gender | NAME | description | level | icon_id | part | is_up_to_up | power_require | gold | gem | head | body | leg |
|---|---|---|---|---|---|---|---|---|---|---|---|---|---|---|
| 2002 | 27 | 3 | Kỷ Vật Của Ông | Chiếc nhẫn cũ. Lau sạch rồi đưa cho người trong nhà. | 1 | 9067 | -1 | 0 | 0 | 0 | 0 | -1 | -1 | -1 |

- **Option gắn khi tạo**: `30` param 0 — **bắt buộc**, vì TYPE 27 mặc định giao dịch được.
- **Vì sao TYPE 27**: nhiệm vụ 5 bước 1 là trigger **A12** (`checkDoneTaskUseItem`) ⇒ item phải bấm "Dùng" được.
- **Rủi ro TYPE 27**: mặc được vào **ô 7** ⇒ nên vá `putItemBody` (bước 12) để giữ item trong hành trang.
- **Cách nhận**: rơi từ mob `tempId ∈ {7, 8, 9}` khi đang ở `TASK_5_0`.
- **Cách tiêu đi**: bước 1 dùng (không trừ), bước 2 nộp NPC 0/2/1 → `subQuantityItemsBag(..., 1)`.
- **Thay thế**: item **992 Nhẫn thời không sai lệch** — nhưng 992 **đang có công dụng dịch chuyển**
  (`UseItem` dòng 293–297) ⇒ dùng lại sẽ gây dịch chuyển ngoài ý muốn. Xem §5.

#### 2003 — Máy Dò Ký Ức

| id | TYPE | gender | NAME | description | level | icon_id | part | is_up_to_up | power_require | gold | gem | head | body | leg |
|---|---|---|---|---|---|---|---|---|---|---|---|---|---|---|
| 2003 | 8 | 3 | Máy Dò Ký Ức | Thiết bị dò tìm dấu vết ký ức bị đánh cắp. | 1 | 1089 | -1 | 0 | 0 | 0 | 0 | -1 | -1 | -1 |

- **Option**: `30` param 0.
- **Cách nhận**: phần thưởng hoàn thành **NV 8** (trong `switch(idTaskCustom)` của `doneTask`).
- **Cách tiêu đi**: là vật chứng — giữ trong hành trang làm điều kiện cho chuỗi sau; không tiêu.
  ⇒ **cần chặn bán/vứt**, nếu không người chơi mất là kẹt.
- **Thay thế**: item **1822 Rada ngọc rồng** (TYPE 75, `part = 0`).

#### 2004 — Hộp Ký Ức Bị Đánh Cắp

| id | TYPE | gender | NAME | description | level | icon_id | part | is_up_to_up | power_require | gold | gem | head | body | leg |
|---|---|---|---|---|---|---|---|---|---|---|---|---|---|---|
| 2004 | 8 | 3 | Hộp Ký Ức Bị Đánh Cắp | Vật chứng lấy được từ kẻ thu gom. Dẫn tới nhiệm vụ 15. | 1 | 7222 | -1 | 0 | 0 | 0 | 0 | -1 | -1 | -1 |

- **Option**: `30` param 0.
- **Cách nhận**: thưởng hoàn thành **NV 14**. **Cách tiêu đi**: nộp ở NV 15.
- **Thay thế**: item **796 Hộp Capsule**.

#### 2006 — Hạt Giống Hy Vọng

| id | TYPE | gender | NAME | description | level | icon_id | part | is_up_to_up | power_require | gold | gem | head | body | leg |
|---|---|---|---|---|---|---|---|---|---|---|---|---|---|---|
| 2006 | 27 | 3 | Hạt Giống Hy Vọng | Gieo tại nhà để cây đậu thần lớn thêm một cấp. | 1 | 241 | -1 | 0 | **0** | 0 | 0 | -1 | -1 | -1 |

- **Option**: `30` param 0.
- **`power_require` bắt buộc = 0**: NV 11 nằm ở chương 2, người chơi sức mạnh còn thấp;
  `UseItem.useItem` dòng 267 sẽ chặn nếu `power_require` > sức mạnh ⇒ kẹt nhiệm vụ.
- **Cách nhận**: trao ở thưởng bước 0 của NV 11.
- **Cách tiêu đi**: `checkDoneTaskUseItem` `case 2006` → nâng `magicTree.level` lên 2 → `doneTask(TASK_11_1)` → trừ item.
- **Thay thế**: item **568 Quả Trứng** (TYPE 27, icon 2164) — hiện **không có case xử lý nào** trong `UseItem`
  nên an toàn để gán nghĩa mới.

### 3.2 Nhóm B — 7 Mảnh Ký Ức (2010–2016)

Bảy item cùng một khuôn, chỉ khác tên + icon. **Bắt buộc liên tiếp 2010 → 2016** để
`InventoryService` đếm đủ bộ ở NV 45 bằng một vòng lặp đơn giản.

| id | TYPE | gender | NAME | description | level | icon_id | part | is_up_to_up | power_require | gold | gem | head | body | leg |
|---|---|---|---|---|---|---|---|---|---|---|---|---|---|---|
| 2010 | **27** | 3 | Mảnh Ký Ức #1 | Một mảnh ký ức của vũ trụ. Cần đủ bảy mảnh. | 1 | 419 | -1 | 0 | 0 | 0 | 0 | -1 | -1 | -1 |
| 2011 | 8 | 3 | Mảnh Ký Ức #2 | Một mảnh ký ức của vũ trụ. Cần đủ bảy mảnh. | 1 | 420 | -1 | 0 | 0 | 0 | 0 | -1 | -1 | -1 |
| 2012 | 8 | 3 | Mảnh Ký Ức #3 | Một mảnh ký ức của vũ trụ. Cần đủ bảy mảnh. | 1 | 421 | -1 | 0 | 0 | 0 | 0 | -1 | -1 | -1 |
| 2013 | 8 | 3 | Mảnh Ký Ức #4 | Một mảnh ký ức của vũ trụ. Cần đủ bảy mảnh. | 1 | 422 | -1 | 0 | 0 | 0 | 0 | -1 | -1 | -1 |
| 2014 | 8 | 3 | Mảnh Ký Ức #5 | Một mảnh ký ức của vũ trụ. Cần đủ bảy mảnh. | 1 | 423 | -1 | 0 | 0 | 0 | 0 | -1 | -1 | -1 |
| 2015 | 8 | 3 | Mảnh Ký Ức #6 | Một mảnh ký ức của vũ trụ. Cần đủ bảy mảnh. | 1 | 424 | -1 | 0 | 0 | 0 | 0 | -1 | -1 | -1 |
| 2016 | 8 | 3 | Mảnh Ký Ức #7 | Một mảnh ký ức của vũ trụ. Cần đủ bảy mảnh. | 1 | 425 | -1 | 0 | 0 | 0 | 0 | -1 | -1 | -1 |

- **2010 phải là TYPE 27** vì [20c NV 40 bước 3](../2-thiet-ke-nhiem-vu-moi/20c-chi-tiet-nhiem-vu-32-47.md) yêu cầu **dùng** Mảnh Ký Ức #1
  (trigger A12). Sáu mảnh còn lại chỉ cần mang ⇒ TYPE 8.
- **Option gắn khi tạo cả 7**: `30` param 0. **Giống hệt nhau ở mọi nguồn phát** (thưởng NV 7, NV 15, NV 23,
  NV 31/49, NPC 29, NPC 70, nhặt ở map 78) — nếu lệch option thì không gộp chồng được.
- **`is_up_to_up = 0`**: mỗi mảnh chỉ có đúng 1 cái, không cần gộp; để 0 cho dễ đếm và dễ nhìn trong hành trang.
- **Giao dịch / ký gửi**: TYPE 8 và TYPE 27 + option 30 ⇒ **đều không giao dịch được**, **đều không ký gửi được**.
  Đây là điểm **KHÁC** với phương án thay thế bằng Ngọc Rồng 14–20: id 14–20 **ký gửi được**
  (`itemCanConsign` liệt kê rõ `it.template.id >= 14 && it.template.id <= 20`) và TYPE 12 **không nằm trong
  danh sách cấm giao dịch** ⇒ nếu dùng Ngọc Rồng làm Mảnh Ký Ức thì người chơi **bán/ký gửi mảnh nhiệm vụ được**.
- **Cách nhận**: #1 thưởng NV 7 · #2 thưởng NV 15 · #3 thưởng NV 23 · #4 thưởng NV 31 hoặc 49 ·
  #5 từ NPC 29 Rồng Omega (NV 39 b3) · #6 từ NPC 70 Bardock (NV 39 b5) · #7 nhặt trên map 78 (NV 45 b1,
  chỉ hiện với người đang ở `TASK_45_1` — mẫu item 78 ở `Zone.getItemMapsForPlayer` dòng 288–292).
- **Cách tiêu đi**: NV 45 bước 3 — dùng item 2100, code kiểm tra đủ 7 item 2010–2016 trong hành trang,
  **trừ cả 7** rồi trao item 2101.

### 3.3 Nhóm C — vật phẩm chương 3–4 (20b, 2040–2074)

| id | TYPE | gender | NAME | description | level | icon_id | part | is_up_to_up | power_require | gold | gem | head | body | leg |
|---|---|---|---|---|---|---|---|---|---|---|---|---|---|---|
| **2040** ⚠️→2045 | 8 | 3 | Vỏ đạn khắc dấu | Vỏ đạn có khắc ký hiệu lạ. Nhặt đủ 5 cái. | 1 | 1421 | -1 | **1** | 0 | 0 | 0 | -1 | -1 | -1 |
| 2041 | 27 | 3 | Búa rèn cũ | Búa rèn của người thợ già. Dùng một lần rồi hỏng. | 1 | 1417 | -1 | 0 | 0 | 0 | 0 | -1 | -1 | -1 |
| 2042 | 8 | 3 | Thẻ tiền thưởng Granola | Thẻ tiền thưởng của thợ săn. Nhặt đủ 3 cái. | 1 | 5428 | -1 | **1** | 0 | 0 | 0 | -1 | -1 | -1 |
| 2043 | 8 | 3 | Biên bản truy nã Ngân Hà | Biên bản truy nã của cảnh sát vũ trụ. Nhặt đủ 3 cái. | 1 | 5428 | -1 | **1** | 0 | 0 | 0 | -1 | -1 | -1 |
| 2044 | 8 | 3 | Máy đo ký ức | Máy đo do Tiểu đội trưởng mang theo. | 1 | 6467 | -1 | 0 | 0 | 0 | 0 | -1 | -1 | -1 |
| 2070 | 8 | 3 | Lõi năng lượng Android | Lõi năng lượng moi từ người máy. Nhặt đủ 3 cái. | 1 | 8620 | -1 | **1** | 0 | 0 | 0 | -1 | -1 | -1 |
| 2071 | 27 | 3 | Mẫu kim loại có ký ức | Mẩu kim loại còn lưu ký ức. Dùng để nghe lại. | 1 | 2288 | -1 | 0 | 0 | 0 | 0 | -1 | -1 | -1 |
| 2072 | 8 | 3 | Mảnh giáp khắc tên | Mảnh giáp có khắc một cái tên đã mờ. | 1 | 10197 | -1 | 0 | 0 | 0 | 0 | -1 | -1 | -1 |
| 2073 | 8 | 3 | Thẻ từ phòng thí nghiệm | Thẻ từ giả của Bunma. Cần có để vào phòng thí nghiệm. | 1 | 9406 | -1 | 0 | 0 | 0 | 0 | -1 | -1 | -1 |
| **2074** ⚠️→2076 | 8 | 3 | Bản thiết kế bản sao | Bản thiết kế đánh cắp được. Nhặt đủ 5 bản trong 6 phút. | 1 | 12846 | -1 | **1** | 0 | 0 | 0 | -1 | -1 | -1 |

**Chi tiết theo từng item:**

| id | Cách người chơi nhận | Cách item bị tiêu đi | Option gắn khi tạo |
|---|---|---|---|
| 2040 (→2045) | Rơi 25% từ mob 22/23/24 khi đang ở `TASK_16_3` (`Mob.dropItemTask`) | `checkDoneTaskPickItem` đếm đủ 5 → trừ khi nộp | 30 / 0 |
| 2041 | NPC trao ở bước 0 của NV 17 | `checkDoneTaskUseItem` `case 2041` → `doneTask(TASK_17_2)` → trừ 1 | 30 / 0 |
| 2042 | Rơi 100% từ boss −20/−21/−22 khi ở `TASK_20_4` (trong `Boss.reward`) | Nộp NPC → trừ 3 | 30 / 0 |
| 2043 | Rơi 100% từ boss −20/−21/−22 khi ở `TASK_48_4` | Nộp NPC → trừ 3 | 30 / 0 |
| 2044 | Rơi 100% từ boss −27 Tiểu đội trưởng (`TDT.java` hàm `reward`) khi ở `TASK_22_3` | Nộp NPC → trừ 1 | 30 / 0 |
| 2070 | Rơi 100% từ boss −30/−31 (`DrKore.java` / `Android19.java` hàm `reward`) khi ở `TASK_25_2` | Nộp NPC → trừ 3 | 30 / 0 |
| 2071 | NPC 21 Bà Hạt Mít trao ở bước 0 NV 26 | `checkDoneTaskUseItem` `case 2071` → phát 3 dòng thoại → tự xóa | 30 / 0 |
| 2072 | Rơi 100% từ boss −37 King Kong (`KingKong.reward`) khi ở `TASK_28_3` | Nộp NPC → trừ 1 | 30 / 0 |
| 2073 | NPC 37 Bunma trao ở bước 0 NV 29 | Là điều kiện vào map 166; trừ sau khi xong NV 29 | 30 / 0 |
| 2074 (→2076) | Sinh sẵn 5–8 `ItemMap` trên map 166 khi người chơi vào; nhặt trong 6 phút | Hết giờ → xóa hết và `count = 0`; đủ 5 → trừ khi nộp | 30 / 0 |

> **Cảnh báo riêng cho 2074**: id này **đã bị hardcode** trong `InventoryService.addItemList` dòng 842
> (`itemAdd.template.id != 2074` — nghĩa là item 2074 được **gộp chồng kể cả khi option khác nhau**).
> Nếu bạn vẫn dùng id 2074, hành vi gộp sẽ khác các item còn lại. Khuyến nghị đổi sang **2076**.
> Chi tiết §4.3.

### 3.4 Nhóm D — vật phẩm chương 5–6 (20c, 2100–2107)

| id | TYPE | gender | NAME | description | level | icon_id | part | is_up_to_up | power_require | gold | gem | head | body | leg |
|---|---|---|---|---|---|---|---|---|---|---|---|---|---|---|
| 2100 | 27 | 3 | Lõi Ký Ức chưa hoàn chỉnh | Lõi rỗng. Dùng khi đã đủ bảy Mảnh Ký Ức. | 1 | 9650 | -1 | 0 | 0 | 0 | 0 | -1 | -1 | -1 |
| 2101 | 27 | 3 | Lõi Hư Không | Lõi chứa toàn bộ ký ức vũ trụ. Quyết định số phận nó. | 1 | 2321 | -1 | 0 | 0 | 0 | 0 | -1 | -1 | -1 |
| 2102 | 8 | 3 | Vỏ Lõi rỗng | Vỏ lõi sau khi ký ức đã trả về vũ trụ. Kỷ vật. | 1 | 12846 | -1 | 0 | 0 | 0 | 0 | -1 | -1 | -1 |
| 2103 | 8 | 3 | Mảnh Ký Ức Vỡ | Mảnh vỡ nhặt ở làng Plant. Nhặt đủ 3 cái. | 1 | 6467 | -1 | **1** | 0 | 0 | 0 | -1 | -1 | -1 |
| 2104 | 8 | 3 | Mảnh Ký Ức Đóng Băng | Mảnh ký ức bị đóng băng trong hang. | 1 | 8620 | -1 | 0 | 0 | 0 | 0 | -1 | -1 | -1 |
| 2105 | 8 | 3 | Mảnh Bùa Babiđây | Mảnh bùa phép của Babiđây. Đường vòng xuống cửa ải. | 1 | 7743 | -1 | 0 | 0 | 0 | 0 | -1 | -1 | -1 |
| 2106 | 8 | 3 | Lõi Phép Babiđây | Lõi phép rơi ra khi Mabư gục xuống. | 1 | 5829 | -1 | 0 | 0 | 0 | 0 | -1 | -1 | -1 |
| 2107 | 8 | 3 | Ống nghiệm Myuu | Ống nghiệm của Dr. Myuu, lấy từ Heart. | 1 | 6849 | -1 | 0 | 0 | 0 | 0 | -1 | -1 | -1 |

**Chi tiết theo từng item:**

| id | Cách người chơi nhận | Cách item bị tiêu đi | Option gắn khi tạo |
|---|---|---|---|
| 2100 | Trao tự động ở NV 45 bước 1 (cùng lúc nhặt Mảnh #7) | `checkDoneTaskUseItem` `case 2100`: kiểm tra đủ 7 item 2010–2016 → trừ cả 7 + trừ 2100 → trao 2101 | 30 / 0 |
| 2101 | Nhận khi xong NV 45 bước 3 | **Nhánh A (NV 47)**: dùng ở map 145 → **trừ 2101**, trao 2102. **Nhánh B (NV 50)**: dùng ở map 145 → **giữ nguyên**, chỉ bật cờ hiển thị | 30 / 0 |
| 2102 | Trao ở NV 47 bước 1 | Không tiêu — kỷ vật vĩnh viễn ⇒ **phải chặn bán/vứt** | 30 / 0 |
| 2103 | Rơi từ mob 81 Tobi khi ở `TASK_32_4` (`Mob.dropItemTask`) | Nộp NPC 70 Bardock ở map 160 → trừ 3 | 30 / 0 |
| 2104 | Rơi ở map 110, chỉ hiện với người ở `TASK_34_3` (`Zone.getItemMapsForPlayer`) | Nộp NPC 70 Bardock → trừ 1 | 30 / 0 |
| 2105 | Rơi từ Cadic M ở map 165 (đường vòng của NV 36 khi ngoài giờ phó bản) | Nhặt là xong bước; trừ khi nộp | 30 / 0 |
| 2106 | Rơi 100% cho người kết liễu Mabư ở NV 37 bước 2 (`Mabu.java` / `Mabu2H.java` / `SuperBu.java` hàm `reward`, và `Mob.getItemMobReward` cho mob 70) | Nộp NPC → trừ 1 | 30 / 0 |
| 2107 | Rơi từ Heart form 3 (boss −108108) ở NV 46 bước 4 | Nộp NPC → trừ 1 | 30 / 0 |

> **Lưu ý về 2101 nhánh B**: item được giữ lại vĩnh viễn làm "cờ hậu truyện".
> Vì `sellItem` vẫn bán được 1 vàng và `throwItem` vẫn vứt được, người chơi nhánh B **có thể tự tay xóa cờ của mình**.
> Bắt buộc vá bước 12 cho id này, nếu không sẽ có khiếu nại.

### 3.5 Nhóm E — 2 danh hiệu (2120–2121)

| id | TYPE | gender | NAME | description | level | icon_id | part | is_up_to_up | power_require | gold | gem | head | body | leg |
|---|---|---|---|---|---|---|---|---|---|---|---|---|---|---|
| 2120 | 36 | 3 | Người Trả Ký Ức | Danh hiệu dành cho người đã trả ký ức về cho vũ trụ. | 0 | 11614 | *(= idEffect, xem §6)* | 0 | 0 | 0 | 0 | -1 | -1 | -1 |
| 2121 | 36 | 3 | Kẻ Giữ Hư Không | Danh hiệu dành cho người đã giữ lại Lõi Hư Không. | 0 | 11617 | *(= idEffect, xem §6)* | 0 | 0 | 0 | 0 | -1 | -1 | -1 |

Danh hiệu **không phải item trong hành trang** — xem đặc tả đầy đủ ở **§6**.

### 3.6 Câu lệnh INSERT mẫu

> **Sao lưu trước.** Chạy trọn gói trong một transaction.

```sql
START TRANSACTION;

-- ============================================================
-- (1) 32 VẬT PHẨM THẬT  (dải 2000–2121)
--     Cột: id, TYPE, gender, NAME, description, level, icon_id,
--           part, is_up_to_up, power_require, gold, gem, head, body, leg
-- ============================================================
INSERT INTO `item_template`
(`id`,`TYPE`,`gender`,`NAME`,`description`,`level`,`icon_id`,`part`,`is_up_to_up`,`power_require`,`gold`,`gem`,`head`,`body`,`leg`) VALUES
-- Nhóm A — chương 1–2 (20a)
(2001,  8, 3, 'Mảnh Vỡ Hư Không',         'Mảnh vỡ lạ rơi ra từ quái. Mang về cho người cần nó.',      1,  1421, -1, 1, 0, 0, 0, -1, -1, -1),
(2002, 27, 3, 'Kỷ Vật Của Ông',           'Chiếc nhẫn cũ. Lau sạch rồi đưa cho người trong nhà.',      1,  9067, -1, 0, 0, 0, 0, -1, -1, -1),
(2003,  8, 3, 'Máy Dò Ký Ức',             'Thiết bị dò tìm dấu vết ký ức bị đánh cắp.',                1,  1089, -1, 0, 0, 0, 0, -1, -1, -1),
(2004,  8, 3, 'Hộp Ký Ức Bị Đánh Cắp',    'Vật chứng lấy được từ kẻ thu gom. Dẫn tới nhiệm vụ 15.',    1,  7222, -1, 0, 0, 0, 0, -1, -1, -1),
(2006, 27, 3, 'Hạt Giống Hy Vọng',        'Gieo tại nhà để cây đậu thần lớn thêm một cấp.',            1,   241, -1, 0, 0, 0, 0, -1, -1, -1),
-- Nhóm B — 7 Mảnh Ký Ức (BẮT BUỘC liên tiếp 2010–2016)
(2010, 27, 3, 'Mảnh Ký Ức #1',            'Một mảnh ký ức của vũ trụ. Cần đủ bảy mảnh.',               1,   419, -1, 0, 0, 0, 0, -1, -1, -1),
(2011,  8, 3, 'Mảnh Ký Ức #2',            'Một mảnh ký ức của vũ trụ. Cần đủ bảy mảnh.',               1,   420, -1, 0, 0, 0, 0, -1, -1, -1),
(2012,  8, 3, 'Mảnh Ký Ức #3',            'Một mảnh ký ức của vũ trụ. Cần đủ bảy mảnh.',               1,   421, -1, 0, 0, 0, 0, -1, -1, -1),
(2013,  8, 3, 'Mảnh Ký Ức #4',            'Một mảnh ký ức của vũ trụ. Cần đủ bảy mảnh.',               1,   422, -1, 0, 0, 0, 0, -1, -1, -1),
(2014,  8, 3, 'Mảnh Ký Ức #5',            'Một mảnh ký ức của vũ trụ. Cần đủ bảy mảnh.',               1,   423, -1, 0, 0, 0, 0, -1, -1, -1),
(2015,  8, 3, 'Mảnh Ký Ức #6',            'Một mảnh ký ức của vũ trụ. Cần đủ bảy mảnh.',               1,   424, -1, 0, 0, 0, 0, -1, -1, -1),
(2016,  8, 3, 'Mảnh Ký Ức #7',            'Một mảnh ký ức của vũ trụ. Cần đủ bảy mảnh.',               1,   425, -1, 0, 0, 0, 0, -1, -1, -1),
-- Nhóm C — chương 3–4 (20b)   [2040 và 2074: xem cảnh báo §4.3]
(2040,  8, 3, 'Vỏ đạn khắc dấu',          'Vỏ đạn có khắc ký hiệu lạ. Nhặt đủ 5 cái.',                 1,  1421, -1, 1, 0, 0, 0, -1, -1, -1),
(2041, 27, 3, 'Búa rèn cũ',               'Búa rèn của người thợ già. Dùng một lần rồi hỏng.',         1,  1417, -1, 0, 0, 0, 0, -1, -1, -1),
(2042,  8, 3, 'Thẻ tiền thưởng Granola',  'Thẻ tiền thưởng của thợ săn. Nhặt đủ 3 cái.',               1,  5428, -1, 1, 0, 0, 0, -1, -1, -1),
(2043,  8, 3, 'Biên bản truy nã Ngân Hà', 'Biên bản truy nã của cảnh sát vũ trụ. Nhặt đủ 3 cái.',      1,  5428, -1, 1, 0, 0, 0, -1, -1, -1),
(2044,  8, 3, 'Máy đo ký ức',             'Máy đo do Tiểu đội trưởng mang theo.',                      1,  6467, -1, 0, 0, 0, 0, -1, -1, -1),
(2070,  8, 3, 'Lõi năng lượng Android',   'Lõi năng lượng moi từ người máy. Nhặt đủ 3 cái.',           1,  8620, -1, 1, 0, 0, 0, -1, -1, -1),
(2071, 27, 3, 'Mẫu kim loại có ký ức',    'Mẩu kim loại còn lưu ký ức. Dùng để nghe lại.',             1,  2288, -1, 0, 0, 0, 0, -1, -1, -1),
(2072,  8, 3, 'Mảnh giáp khắc tên',       'Mảnh giáp có khắc một cái tên đã mờ.',                      1, 10197, -1, 0, 0, 0, 0, -1, -1, -1),
(2073,  8, 3, 'Thẻ từ phòng thí nghiệm',  'Thẻ từ giả của Bunma. Cần có để vào phòng thí nghiệm.',     1,  9406, -1, 0, 0, 0, 0, -1, -1, -1),
(2074,  8, 3, 'Bản thiết kế bản sao',     'Bản thiết kế đánh cắp được. Nhặt đủ 5 bản trong 6 phút.',   1, 12846, -1, 1, 0, 0, 0, -1, -1, -1),
-- Nhóm D — chương 5–6 (20c)
(2100, 27, 3, 'Lõi Ký Ức chưa hoàn chỉnh','Lõi rỗng. Dùng khi đã đủ bảy Mảnh Ký Ức.',                  1,  9650, -1, 0, 0, 0, 0, -1, -1, -1),
(2101, 27, 3, 'Lõi Hư Không',             'Lõi chứa toàn bộ ký ức vũ trụ. Quyết định số phận nó.',     1,  2321, -1, 0, 0, 0, 0, -1, -1, -1),
(2102,  8, 3, 'Vỏ Lõi rỗng',              'Vỏ lõi sau khi ký ức đã trả về vũ trụ. Kỷ vật.',            1, 12846, -1, 0, 0, 0, 0, -1, -1, -1),
(2103,  8, 3, 'Mảnh Ký Ức Vỡ',            'Mảnh vỡ nhặt ở làng Plant. Nhặt đủ 3 cái.',                 1,  6467, -1, 1, 0, 0, 0, -1, -1, -1),
(2104,  8, 3, 'Mảnh Ký Ức Đóng Băng',     'Mảnh ký ức bị đóng băng trong hang.',                       1,  8620, -1, 0, 0, 0, 0, -1, -1, -1),
(2105,  8, 3, 'Mảnh Bùa Babiđây',         'Mảnh bùa phép của Babiđây. Đường vòng xuống cửa ải.',       1,  7743, -1, 0, 0, 0, 0, -1, -1, -1),
(2106,  8, 3, 'Lõi Phép Babiđây',         'Lõi phép rơi ra khi Mabư gục xuống.',                       1,  5829, -1, 0, 0, 0, 0, -1, -1, -1),
(2107,  8, 3, 'Ống nghiệm Myuu',          'Ống nghiệm của Dr. Myuu, lấy từ Heart.',                    1,  6849, -1, 0, 0, 0, 0, -1, -1, -1),
-- Nhóm E — 2 danh hiệu (part = idEffect, xem §6)
(2120, 36, 3, 'Người Trả Ký Ức',          'Danh hiệu dành cho người đã trả ký ức về cho vũ trụ.',      0, 11614, 257, 0, 0, 0, 0, -1, -1, -1),
(2121, 36, 3, 'Kẻ Giữ Hư Không',          'Danh hiệu dành cho người đã giữ lại Lõi Hư Không.',         0, 11617, 258, 0, 0, 0, 0, -1, -1, -1);

-- ============================================================
-- (2) 90 DÒNG TRỐNG LẤP CHỖ  — BẮT BUỘC, xem §0.2
--     Mẫu giống hệt 141 dòng trống có sẵn (id 1826–1999)
--     MariaDB 10.2+ / MySQL 8+ (có WITH RECURSIVE)
-- ============================================================
INSERT INTO `item_template`
(`id`,`TYPE`,`gender`,`NAME`,`description`,`level`,`icon_id`,`part`,`is_up_to_up`,`power_require`,`gold`,`gem`,`head`,`body`,`leg`)
WITH RECURSIVE seq AS (
    SELECT 2000 AS n
    UNION ALL
    SELECT n + 1 FROM seq WHERE n < 2121
)
SELECT n, 75, 3, '', '', 0, 0, 0, 0, 0, 0, 0, -1, -1, -1
FROM seq
WHERE n NOT IN (SELECT id FROM item_template);

-- ============================================================
-- (3) KIỂM TRA TRƯỚC KHI COMMIT
-- ============================================================
SELECT COUNT(*) AS so_dong, MIN(id) AS min_id, MAX(id) AS max_id FROM item_template;
--  kỳ vọng: so_dong = 2122, min_id = 0, max_id = 2121

SELECT a.id + 1 AS id_bi_thieu
FROM item_template a
LEFT JOIN item_template b ON b.id = a.id + 1
WHERE b.id IS NULL AND a.id < (SELECT MAX(id) FROM item_template);
--  kỳ vọng: 0 dòng

SELECT id, NAME FROM item_template WHERE description IS NULL;
--  kỳ vọng: 0 dòng

SELECT id, NAME, CHAR_LENGTH(description) AS n FROM item_template
WHERE id >= 2000 AND CHAR_LENGTH(description) > 75;
--  kỳ vọng: 0 dòng

COMMIT;
```

> Nếu MySQL/MariaDB của bạn **không hỗ trợ `WITH RECURSIVE`**, thay khối (2) bằng 90 dòng `INSERT` liệt kê tay
> cho các id: **2000, 2005, 2007, 2008, 2009, 2017–2039, 2045–2069, 2075–2099, 2108–2119**
> (tổng 5 + 23 + 25 + 25 + 12 = **90** id).

**Nếu chọn phương án đổi id để tránh va chạm (§4.3)** — thay hai dòng và điều chỉnh vùng trống:

```sql
-- thay dòng 2040 bằng:
(2045,  8, 3, 'Vỏ đạn khắc dấu',      'Vỏ đạn có khắc ký hiệu lạ. Nhặt đủ 5 cái.',               1,  1421, -1, 1, 0, 0, 0, -1, -1, -1),
-- thay dòng 2074 bằng:
(2076,  8, 3, 'Bản thiết kế bản sao', 'Bản thiết kế đánh cắp được. Nhặt đủ 5 bản trong 6 phút.', 1, 12846, -1, 1, 0, 0, 0, -1, -1, -1),
```

---

## 4. Kiểm tra trùng lặp và va chạm

### 4.1 Id lớn nhất hiện có và khoảng trống

| Chỉ số | Giá trị (kiểm tra ngày 17/09/2026) |
|---|---|
| Số dòng `item_template` | **2000** |
| Id nhỏ nhất / lớn nhất | **0 / 1999** |
| Id bị thiếu trong 0–1999 | **0 (không thiếu id nào)** |
| Id bị trùng | **0** |
| Dòng có `NAME = ''` (ô trống đặt sẵn) | **141**, ở các dải `1826–1834`, `1836–1839`, `1871–1899`, `1901–1999` |
| Dòng có tên cuối cùng | **1900 — "Ghost Rider"** (TYPE 27) |
| Dòng nào có `description IS NULL` | **0** |

⇒ **Toàn bộ 32 id đề xuất (2001–2121) đều chưa tồn tại trong DB.** Không có xung đột khóa chính.

### 4.2 Giả định về ngưỡng id trong code

Đã rà toàn bộ `SRC/src` tìm các phép so sánh id item với một ngưỡng:

| Nơi | Nội dung | Có chặn id ≥ 2000 không? |
|---|---|---|
| `Item.java` dòng 148–430 | `isVaiTho()` 0–65, `isDTL()` 555–567, `isDHD()` 650–662, `isDTS()` 1048–1062, `isManhTS()` 1066–1070, `isDaNangCap()` 1074–1078, `isDaMayMan()` 1079–1083, `isCongThucVip()` 1071–1073 & 1084–1086, `isThucAn()` 663–667, `isSachTuyetKy2()` 1278–1280 | **Không** — đều là dải đóng, id ≥ 2000 rơi ra ngoài, trả `false`. An toàn |
| `ConsignShopService.java` dòng 341 | `it.template.id >= 14 && <= 20` | Không |
| `combine/*.java` | các dải 441–447, 555–567, 1416–1422… | Không |
| `NewBot.java` dòng 15 | `new int[Manager.ITEM_TEMPLATES.size()][4]` | Mảng cấp phát **theo kích thước thật lúc nạp lớp** ⇒ tự động nở theo. An toàn, miễn là lớp `NewBot` được nạp **sau** `Manager.loadDatabase()` (hiện đúng như vậy) |
| `ItemData.java` dòng 16 | `updateItemTemplate(session, 750, Manager.ITEM_TEMPLATES.size())` | Tự động theo kích thước. An toàn |
| `ItemData.java` dòng 46, 73–74 | `writeShort(count)`, `writeShort(start)`, `writeShort(end)` | short — 2122 < 32767. An toàn |
| `Manager.java` dòng 637 | `itemTemp.id = rs.getShort("id")` | short. An toàn tới 32767 |

**Không có chỗ nào trong code giả định `id item < 2000`.** Rào cản duy nhất là **tính liên tục** của id (§0.2).

### 4.3 ⚠️ Các id ≥ 2000 ĐÃ bị hardcode trong code — DANH SÁCH CẤM

Đây là phát hiện quan trọng nhất của mục 4. Mã nguồn hiện tại **đã nhắc tên 23 id ≥ 2000 không tồn tại trong DB**
(di sản từ một bản server khác). Chừng nào `item_template` chỉ có tới 1999, các đoạn này **chết lặng**
(ném `IndexOutOfBoundsException` và bị `try/catch` nuốt). **Ngay khi bạn thêm item id ≥ 2000, chúng sống lại.**

| File : dòng | Hàm | Id bị hardcode | Chuyện gì xảy ra khi id đó tồn tại |
|---|---|---|---|
| `services/ItemService.java` : **638** | `vatphamsk(boolean)` | **2025, 2026, 2036, 2037, 2038, 2039, 2040, 2019, 2020, 2021, 2022, 2023, 2024** | Hàm chọn ngẫu nhiên 1 trong 20 id rồi trao cho người chơi. Được gọi từ `OpenItem648` (dòng 488) — **mở "Hộp quà giáng sinh" (item 648)**. Nếu 2019–2026 / 2036–2040 tồn tại, người mở hộp sẽ **nhận được vật phẩm nhiệm vụ (hoặc dòng trống không tên)** kèm option ngẫu nhiên 77/80/81/103/50/94/5 |
| `services/ItemService.java` : **598** | `randomRac2()` | **2048** | Cùng đường, cùng hộp 648 |
| `services/InventoryService.java` : **842** | `addItemList` | **2074** | Item 2074 được gộp chồng **kể cả khi option khác nhau** (ngoại lệ giống đá nâng cấp / mảnh thiên sứ) |
| `services/InventoryService.java` : **847–850** | `addItemList` | **2048, 2050, 2051, 2052, 2053, 2054, 2055, 2075** | Các id này **gộp không giới hạn** (bỏ trần 99.999/ô) |
| `services/InventoryService.java` : **1074, 1079** | `findItemTVC` | **2077** | Hàm kiểm tra người chơi có item 2077 trong túi/rương. Hiện **không có nơi nào gọi** ⇒ vô hại, nhưng đừng dùng lại id này |
| `player/NPoint.java` : **665** | `setOutfitFusion` | **2133, 2134** | Nếu ô cải trang của chủ và của đệ là cặp 2133/2134 ⇒ bật cờ `isGogeta`. Ngoài dải đề xuất |
| `database/MrBlue.java` : **415, 849** | `loadPlayer` | **2132** | **Thu hồi (xóa) item 2132** nếu `createTime` nằm trong 15/03/2024 – 28/03/2024. Ngoài dải đề xuất |
| `database/MrBlue.java` : **473** | `loadPlayer` | **2322** | **Thu hồi item 2322** nếu `createTime` nằm trong 06/02/2025 – 28/06/2025. Ngoài dải đề xuất |

> [02b §3.3 ghi chú cuối](../1-he-thong-hien-tai/02b-database-du-lieu-template.md) đã nêu 2132 và 2322 — **xác nhận đúng**,
> và ngoài hai id đó còn **21 id nữa** cần tránh.

**Danh sách id CẤM DÙNG (trừ khi sửa code trước):**

```
2019 2020 2021 2022 2023 2024 2025 2026
2036 2037 2038 2039 2040
2048 2050 2051 2052 2053 2054 2055
2074 2075 2077
2132 2133 2134
2322
```

**Va chạm thật với thiết kế hiện tại: 2 item.**

| Item của thiết kế | Xung đột với | Khuyến nghị |
|---|---|---|
| **2040** Vỏ đạn khắc dấu (20b) | `ItemService.vatphamsk` dòng 638 | Đổi sang **2045** |
| **2074** Bản thiết kế bản sao (20b) | `InventoryService.addItemList` dòng 842 | Đổi sang **2076** |

**Va chạm gián tiếp**: 21 id còn lại trong danh sách cấm sẽ trở thành **dòng trống không tên** khi bạn lấp đầy
dải 2000–2121. Khi đó `vatphamsk` / `randomRac2` sẽ **trao cho người chơi một item không tên, icon trắng**
thay vì ném ngoại lệ như hiện nay. Hai cách xử lý:

1. **Cách nhẹ (khuyến nghị)** — sửa hai mảng cho sạch, xóa mọi id ≥ 2000:
   - `ItemService.java` dòng 638: bỏ `2025, 2026, 2036, 2037, 2038, 2039, 2040, 2019, 2020, 2021, 2022, 2023, 2024`,
     giữ lại `954, 955, 952, 953, 924, 860, 742`.
   - `ItemService.java` dòng 598: bỏ `2048`.
   *(Lưu ý thêm: cả hai hàm dùng `Util.nextInt(racs.length - 1)` nên phần tử cuối mảng không bao giờ được chọn — lỗi cũ, không phải do item mới.)*
2. **Cách nhanh** — không sửa code, nhưng phải chấp nhận item 648 "Hộp quà giáng sinh" có thể trả ra đồ rỗng.
   Chỉ chấp nhận được nếu item 648 hiện không có nguồn phát nào cho người chơi.

### 4.4 Trùng lặp nội bộ giữa 3 file thiết kế

Rà chéo 20a / 20b / 20c, còn tồn tại các mâu thuẫn **chưa được dọn** (không sửa trong file này, chỉ báo cáo):

| # | Mâu thuẫn | Ở đâu | Đúng theo bảng chuẩn [20 §9.2b] |
|---|---|---|---|
| 1 | Bảng dải id ghi "2001–2007 = 7 Mảnh Ký Ức" | 20b dòng 95 | Sai — phải là **2010–2016** |
| 2 | Bảng §6 ghi "2000–2003 = Mảnh Ký Ức #1…#4" | 20c dòng ~993 | Sai — phải là **2010–2013** |
| 3 | NV 40 bước 3 ghi "dùng item **2000** Mảnh Ký Ức #1" | 20c dòng 463, và bảng trigger dòng 1072 | Sai — phải là **2010** |
| 4 | Ghi chú "Id **2014** để trống có chủ ý, dành cho Nhật ký Bardock" trong khi chính bảng đó gán 2014 = Mảnh Ký Ức #5 | 20c dòng 1010 | Tự mâu thuẫn. Nếu muốn thêm "Nhật ký Bardock" thì dùng id trống khác (ví dụ **2017**) |
| 5 | Dải "2020–2039 = vật phẩm riêng của 20a" | 20b dòng 96 | Trùng danh sách cấm §4.3 (2019–2026, 2036–2040). **Không được dùng dải này** |
| 6 | Id **2005**, **2007–2009** bỏ trống trong dải "2001–2006" mà 20a không dùng hết | 20a dòng 622 | Không sao, nhưng vẫn **phải chèn dòng trống** |
| 7 | 20a đặt Mảnh Ký Ức TYPE **11**, 20c đặt TYPE **27** cho cùng bộ 7 mảnh | 20a §B vs 20c §6 | Phải thống nhất. Khuyến nghị §3.2 |

### 4.5 Bảng khác trong DB có tham chiếu id item

| Bảng | Cột tham chiếu | Id lớn nhất đang dùng | Cần thêm dòng cho item mới? |
|---|---|---|---|
| `item_shop` | `temp_id` | 1840 (826 dòng, id 1–998) | **Có**, chỉ cho 2 danh hiệu 2120/2121 (§6) |
| `item_shop_option` | theo `item_shop.id` | — | Có, nếu danh hiệu cần hiển thị option trong shop |
| `shop_ky_gui` | `item_id` | dữ liệu người chơi | Không |
| `data_badges` | `idItem` | 1790 | **Có**, 2 dòng (§6) |
| `flag_bag` | `id` (đích của `item_template.part` khi TYPE 11) | — | Không, vì khuyến nghị không dùng TYPE 11 |
| `player.items_bag` / `items_box` / `items_body` / `items_daban` | `tempId` trong JSON | dữ liệu người chơi | Không |

---

## 5. Phương án KHÔNG tạo item mới

Đây là câu hỏi số 1 mà [20 §10](../2-thiet-ke-nhiem-vu-moi/20-thiet-ke-nhiem-vu-moi.md) đang chờ chủ dự án chốt.
Nếu chọn phương án này thì **không phải đụng `item_template`, không phải tăng `vsItem`, không phải test client cũ**.

### 5.1 Bảng ánh xạ đầy đủ

| Item mới | Thay bằng item sẵn có | TYPE thật | Giao dịch được? | Ký gửi được? | Bán được? | Rủi ro riêng |
|---|---|---|---|---|---|---|
| 2001 Mảnh Vỡ Hư Không | **225 Mảnh đá vụn** | 15 | Không (`case 15`? — **KHÔNG**, TYPE 15 **không** trong danh sách cấm ⇒ **giao dịch được**) | **CÓ** (`itemCanConsign` liệt kê TYPE 15) | 1 vàng | Là nguyên liệu đập đồ hiện hành ⇒ người chơi **bán/ký gửi mất** vật phẩm nhiệm vụ mà không biết |
| 2002 Kỷ Vật Của Ông | **992 Nhẫn thời không sai lệch** | 27 | Có | Không | 1 vàng | **Đã có công dụng**: dùng là **dịch chuyển sang map 80/160** (`UseItem` dòng 293–297) ⇒ bấm "Dùng" trong nhiệm vụ sẽ **ném người chơi đi map khác** |
| 2003 Máy Dò Ký Ức | **1822 Rada ngọc rồng** | 75 | Không | Không | 1 vàng | Item sự kiện, `part = 0`; nếu sự kiện cũ phát lại sẽ lẫn |
| 2004 Hộp Ký Ức Bị Đánh Cắp | **796 Hộp Capsule** | 27 | Có | Không | 1 vàng | Trùng công dụng với hộp capsule thật |
| 2006 Hạt Giống Hy Vọng | **568 Quả Trứng** | 27 | Có | Không | 1 vàng | Hiện **không có case xử lý** ⇒ an toàn nhất trong nhóm thay thế |
| 2010–2016 (7 Mảnh Ký Ức) | **14, 15, 16, 17, 18, 19, 20 Ngọc Rồng 1–7 sao** | 12 | **CÓ** | **CÓ** (id 14–20 được liệt kê thẳng) | 1 vàng | Nặng nhất: bấm "Dùng" là **gọi Rồng Thần** (`UseItem case 12`); người chơi **ước rồng mất luôn mảnh nhiệm vụ**; ngoài ra ngọc rồng là **hàng hóa có giá** trên chợ ⇒ mua đủ 7 mảnh bằng tiền, bỏ qua toàn bộ tuyến. Và phần thưởng cuối tuyến (2 bộ Ngọc Rồng) phải đổi sang thứ khác |
| 2040 Vỏ đạn khắc dấu | **225 Mảnh đá vụn** | 15 | Có | Có | 1 vàng | Như 2001 |
| 2041 Búa rèn cũ | **796 Hộp Capsule** | 27 | Có | Không | 1 vàng | `UseItem` chưa có case 796 ⇒ thêm được |
| 2042 / 2043 | **611 Bản đồ kho báu** | 27 | Có | Không | 1 vàng | **Hai nhiệm vụ khác nhau dùng chung một id** ⇒ phải phân biệt bằng **số lượng**, rất dễ lẫn khi người chơi làm cả hai nhánh |
| 2044 / 2103 | **674 Đá ngũ sắc** | 27 | Có | Không | 1 vàng | Đá ngũ sắc là **nguyên liệu pha lê hóa** đang có giá; người chơi sẽ tiêu mất |
| 2070 / 2104 | **935 Đá xanh lam** | 27 | Có | Không | 1 vàng | Trùng nguyên liệu |
| 2071 | **362 Hóa thạch Ngọc Rồng** | 11 | Không | Không | 1 vàng | **Là item ngọc rồng Namếc** — `InventoryService.addItemSpecial` đưa 362 vào `NgocRongNamecService.pickNamekBall` ⇒ **xung đột hệ thống ngọc rồng Namếc** |
| 2072 | **1066 Mảnh áo** | 27 | Có | Không | 1 vàng | Là **mảnh Thiên Sứ**, gộp không giới hạn (`addItemList` dòng 847); người chơi ghép đồ Thiên Sứ sẽ tiêu mất |
| 2073 | **987 Đá bảo vệ** | 27 | Có | Không | 1 vàng | Đá bảo vệ dùng khi nâng cấp đồ ⇒ tiêu mất |
| 2074 | **1560 Rương ngọc rồng** | 27 | Có | Không | 1 vàng | — |
| 2100 | **1015 Ngọc rồng Siêu Cấp** | 27 | Có | Không | 1 vàng | — |
| 2101 Lõi Hư Không | **1015 Ngọc rồng Siêu Cấp** | 27 | Có | Không | 1 vàng | **Trùng với 2100** nếu cả hai cùng thay bằng 1015 ⇒ phải chọn item khác cho một trong hai. Đề xuất 2101 → **378 Ngọc rồng 7 sao đen** (TYPE 11) nhưng 378 lại thuộc **hệ thống ngọc rồng đen** (`BlackBallWarService.pickBlackBall`) ⇒ cũng xung đột |
| 2102 Vỏ Lõi rỗng | **1560 Rương ngọc rồng** | 27 | Có | Không | 1 vàng | Trùng với 2074 |
| 2105 | **861 Hồng ngọc** | **34** | — | — | — | ❌ **KHÔNG DÙNG ĐƯỢC**: TYPE 34 khi nhặt được **cộng thẳng vào `inventory.ruby`** (không vào hành trang) — [06 §6.3 mục 4](../1-he-thong-hien-tai/06-vat-pham.md#6-hành-trang-rương-đồ-giới-hạn-xếp-chồng-mở-rộng). Phải chọn item khác |
| 2106 | **638 Bình chứa Commeson** | 27 | Có | Không | 1 vàng | 20b §5.3 đã định dùng 638 cho NV 49 ⇒ trùng |
| 2107 | **727 Siêu thần thủy** | 27 | Có | Không | 1 vàng | ⚠️ `power_require = 100.000.000` ⇒ người chơi dưới 100 triệu SM **không bấm "Dùng" được** |
| 2120 danh hiệu | **1288 Chiến thần khóa nick** (đổi tên) | 36 | — | — | — | Xem §6.4 |
| 2121 danh hiệu | **1673 Tay nhanh hơn não** (đổi tên) | 36 | — | — | — | Xem §6.4 |

### 5.2 Rủi ro chung của phương án "không tạo item mới"

1. **Trùng công dụng** — mọi item thay thế đều đã có nghĩa trong game. `UseItem.useItem`
   xử lý theo `switch(item.template.id)`, nên gán thêm nghĩa nhiệm vụ cho một id đang dùng
   ⇒ **hai nhánh xử lý tranh nhau**, hoặc phải viết `if` lồng theo tiến độ nhiệm vụ (khó bảo trì).
2. **Người chơi bán/ký gửi nhầm** — 225, 674, 935, 987, 1066, 14–20 đều là **hàng hóa có giá** trên chợ ký gửi.
   Người chơi có sẵn thói quen bán chúng ⇒ kẹt nhiệm vụ, và đội hỗ trợ phải trả item thủ công.
3. **Lẫn với đồ sự kiện** — 1822, 1560, 796, 611 là đồ sự kiện; khi sự kiện cũ chạy lại sẽ phát thêm ⇒
   người chơi "tự nhiên xong bước nhiệm vụ" mà không làm gì.
4. **Không phân biệt được hai nhánh** — 2042/2043 và 2100/2101/2102 dùng chung id
   ⇒ chỉ phân biệt bằng số lượng, rất mong manh.
5. **Xung đột hệ thống riêng** — 362 (ngọc rồng Namếc), 378 (ngọc rồng đen), 861 (hồng ngọc = tiền tệ)
   **không thể dùng lại**, vì `InventoryService.addItemSpecial` (dòng 704–733) chặn trước.
6. **Mất trải nghiệm** — người chơi không có cảm giác "vật phẩm của tuyến truyện"; 7 Mảnh Ký Ức biến thành
   7 viên ngọc rồng quen thuộc, làm hỏng điểm nhấn chính của thiết kế.

### 5.3 Phương án lai (khuyến nghị)

Tạo item mới **chỉ cho 9 item then chốt**, còn lại dùng item sẵn có:

| Nhóm | Xử lý |
|---|---|
| **Tạo mới** (9): 2010–2016 (7 Mảnh Ký Ức), 2100 (Lõi Ký Ức), 2101 (Lõi Hư Không) | Đây là trục truyện, không được phép lẫn với đồ khác |
| **Dùng lại** (23 item còn lại) | Theo bảng §5.1, ưu tiên các id **chưa có case xử lý nào** trong `UseItem`: 568, 796, 611, 1560 |

Với phương án lai, `item_template` chỉ cần tới **id 2101** ⇒ **102 dòng thêm** (9 thật + 93 trống),
và vẫn phải tăng `vsItem`.

---

## 6. Danh hiệu 2120 / 2121 — đặc tả riêng

### 6.1 Cơ chế danh hiệu trong code

Danh hiệu **không phải item trong hành trang**. Nó là một bản ghi `BadgesData` trong `player.dataBadges`.
Item TYPE 36 chỉ là **hình đại diện của danh hiệu trong shop**.

| Thành phần | File | Vai trò |
|---|---|---|
| `BagesTemplate` | `player_badges/BagesTemplate.java` | Nạp từ bảng `data_badges`: `id`, `idEffect`, `idItem`, `NAME`, `options` (JSON `[{id,param}]`). Nạp ở `Manager.java` dòng **772–792** |
| `BadgesData` | `player_badges/BadgesData.java` | Danh hiệu người chơi sở hữu: `idBadGes` (**= `idEffect`**), `timeofUseBadges` (mốc hết hạn, ms), `isUse` |
| `BadgesService.turnOnBadges` | `player_badges/BadgesService.java` | Bật 1 danh hiệu, tắt các danh hiệu khác |
| `BadgesTaskService` | `task/BadgesTaskService.java` | Cộng tiến độ nhiệm vụ danh hiệu, tự trao danh hiệu với hạn **30 ngày** |
| `TabShopDanhHieu` / `TabShopSoHuu` | `shop/*.java` | Tab **44 "Danh<>Hiệu"** và tab **45 "Sở Hữu<>"** của shop id **26** (NPC 39 Santa) |
| `BagesTemplate.sendListItemOption` | dòng 62–71 | Chỉ lấy option của danh hiệu đang `isUse` → `NPoint` áp dụng |

**Quan trọng**:
- `BadgesData(Player, id, days)` (dòng 32–41) **tự cộng thời hạn** `days × 24h` và **tự `player.dataBadges.add(this)`**.
- `NPoint` **chỉ áp dụng option 50 (Sức đánh %), 77 (HP %), 103 (KI %), 108 (Né đòn %)** —
  xem [13 §4.2 và ghi chú 30](../1-he-thong-hien-tai/13-nhiem-vu-phu-thanh-tich-danh-hieu.md). Option 5 (chí mạng), 117 (Đẹp) **không có hiệu lực**.
- **Option 93 "Hạn sử dụng # ngày"** trong cột `Options` chỉ để **hiển thị số ngày còn lại** ở tab 45;
  thời hạn thật nằm ở `BadgesData.timeofUseBadges`.

### 6.2 Trạng thái bảng `data_badges` hiện tại

`database team2026.sql` dòng **999–1027**: **18 dòng**, `id` 1–18 (thiếu id 10, 11),
`idEffect` đang dùng: `218, 219, 220, 221, 222, 223, 224, 225, 226, 228, 240, 242, 243, 247, 253, 256`.

Cấu trúc bảng — **5 cột**:

| Cột | Kiểu |
|---|---|
| `id` | int(11) |
| `idEffect` | int(11) |
| `idItem` | int(11) — trỏ tới `item_template.id` TYPE 36 |
| `NAME` | text |
| `Options` | text — JSON `[{"param":N,"id":M}, ...]` |

### 6.3 ⚠️ `idEffect` 260 / 261 mà 20c đề xuất KHÔNG có tài nguyên

`DataGame.sendEffectTemplate(session, id, ...)` dòng 264–290 đọc **hai** file:

```java
final byte[] effData = FileIO.readFile("data/effdata/DataEffect_" + idT);
final byte[] effImg  = FileIO.readFile("data/effect/x" + session.zoomLevel + "/ImgEffect_" + idT + ".png");
if (effData == null || effImg == null) { return; }   // lặng lẽ bỏ qua
```

Kiểm kê thư mục tài nguyên thật:

| Nguồn | Các id hiệu ứng trong khoảng 210–300 |
|---|---|
| `SRC/data/effdata` | 210, 212, 213, 214, 218–226, 228, 230–254, **256**, 280–284 |
| `SRC/data/effect/x1` | 210, 212, 213, 214, 218–225, 228, 230–254 *(**thiếu 226, 256, 280–284**)* |
| `SRC/data/effect/x2` | như x1 + 226, 256, 280, 284 |
| `SRC/data/effect/x3` | như x2 |
| `SRC/data/effect/x4` | như x2 + 281, 282, 283 |

⇒ **Không tồn tại `DataEffect_260` / `ImgEffect_260.png`, cũng không có 261.**
Nếu đặt `idEffect = 260/261`, danh hiệu **vẫn cộng chỉ số bình thường** (vì option lấy từ `data_badges`),
nhưng **không có hiệu ứng nào hiện trên đầu nhân vật** — người chơi sẽ báo "danh hiệu tàng hình".

**Ba lựa chọn, theo thứ tự khuyến nghị:**

| # | Cách làm | Việc phải làm | Đánh giá |
|---|---|---|---|
| **A** | **Thêm tài nguyên hiệu ứng mới** `idEffect = 257` và `258` | Thêm `SRC/data/effdata/DataEffect_257`, `DataEffect_258` và `SRC/data/effect/x1..x4/ImgEffect_257.png`, `ImgEffect_258.png` (**đủ 4 zoom**). Không cần đổi `vsData` vì effect được gửi theo yêu cầu (`Controller` gói `-66`) | ✅ Sạch nhất. 257/258 hiện **hoàn toàn trống** |
| **B** | **Mượn hiệu ứng đã có nhưng chưa dùng cho danh hiệu nào** | Chọn trong: 210, 212, 213, 214, 230–239, 241, 244, 245, 246, 248–252, 254 (đều có đủ ở cả 4 zoom và không nằm trong `data_badges`) | ⚠️ Cần kiểm tra các effect này không đang được dùng cho skill/aura khác |
| **C** | Giữ 260/261 như 20c ghi | Không làm gì | ❌ Danh hiệu không có hiệu ứng |

**Đặc tả này chọn phương án A: `idEffect = 257` cho 2120 và `258` cho 2121.**

### 6.4 Đặc tả đầy đủ 2 danh hiệu

#### Dòng cần thêm vào `item_template` (đã có trong §3.6)

| id | TYPE | gender | NAME | description | level | icon_id | part | is_up_to_up | power_require | gold | gem | head | body | leg |
|---|---|---|---|---|---|---|---|---|---|---|---|---|---|---|
| 2120 | 36 | 3 | Người Trả Ký Ức | Danh hiệu dành cho người đã trả ký ức về cho vũ trụ. | 0 | 11614 | **257** | 0 | 0 | 0 | 0 | -1 | -1 | -1 |
| 2121 | 36 | 3 | Kẻ Giữ Hư Không | Danh hiệu dành cho người đã giữ lại Lõi Hư Không. | 0 | 11617 | **258** | 0 | 0 | 0 | 0 | -1 | -1 | -1 |

> `part = idEffect` là **thông lệ nhất quán của DB hiện tại**: item 1291 "Trùm săn Boss" `part = 220` = `idEffect` 220;
> item 1293 "Cao thủ siêu hạng" `part = 222` = `idEffect` 222; 1288 `part = 217`; 1673 `part = 254`.
> Server không đọc `part` cho TYPE 36, nhưng client nhận `part` (`ItemData` dòng 86) — giữ đúng thông lệ để an toàn.
>
> `icon_id` 11614 và 11617 **trùng với item 1293 và 1291**. Không gây lỗi kỹ thuật
> (`getItemIdByIcon` chỉ dùng cho `iconSpec` của shop, `ShopService` dòng 971), nhưng
> **nên vẽ icon riêng** để người chơi phân biệt được hai danh hiệu kết thúc tuyến với hai danh hiệu cũ.

#### Dòng cần thêm vào `data_badges`

```sql
INSERT INTO `data_badges` (`id`, `idEffect`, `idItem`, `NAME`, `Options`) VALUES
(19, 257, 2120, 'Người Trả Ký Ức',
 '[{\"param\":12,\"id\":50},{\"param\":12,\"id\":77},{\"param\":12,\"id\":103}]'),
(20, 258, 2121, 'Kẻ Giữ Hư Không',
 '[{\"param\":12,\"id\":50},{\"param\":12,\"id\":77},{\"param\":12,\"id\":103}]');
```

Giải thích `Options`:

| option id | Ý nghĩa (`item_option_template`) | param | NPoint có áp dụng? |
|---|---|---|---|
| 50 | `Sức đánh+#%` | 12 | ✅ |
| 77 | `HP+#%` | 12 | ✅ |
| 103 | `KI +#%` | 12 | ✅ |
| ~~93~~ | ~~`Hạn sử dụng # ngày`~~ | — | **CỐ Ý BỎ** — xem dưới |

Mức 12% ngang với danh hiệu **"X-mas"** (`data_badges` id 15, idEffect 247: 12/12/12) — đang là mức cao nhất
trong bảng hiện tại. Hai danh hiệu **có chỉ số bằng hệt nhau**, chỉ khác tên + hiệu ứng, đúng ý 20c
(để lựa chọn nhánh là lựa chọn kể chuyện, không phải lựa chọn sức mạnh).

#### Thời hạn: 30 ngày hay vĩnh viễn?

- `BadgesTaskService.updateDoneTask` tạo danh hiệu bằng `new BadgesData(player, idBadgesReward, 30)` ⇒ **30 ngày**.
- `ShopService.buyDanhHieu` cũng dùng **30**.
- Với danh hiệu **kết thúc tuyến 48 nhiệm vụ**, 30 ngày là quá ngắn.

**Cách trao vĩnh viễn, không đụng `BadgesTaskService`**: trong `switch(idTaskCustom)` của `doneTask`
(nơi trao thưởng NV 47 / NV 50), gọi thẳng:

```java
// 36500 ngày ≈ 100 năm — coi như vĩnh viễn
new BadgesData(player, 257, 36500);     // NV 47 — Người Trả Ký Ức
BadgesService.turnOnBadges(player, 257);
```

> ⚠️ **Không được gọi thêm `player.dataBadges.add(...)`** sau đó: constructor `BadgesData(Player,int,int)`
> dòng 39 **đã tự add rồi**. Đây chính là lỗi đang tồn tại ở `BadgesTaskService.updateDoneTask` và
> `ShopService.buyDanhHieu` khiến **chỉ số danh hiệu bị nhân đôi**
> ([13 ghi chú 18](../1-he-thong-hien-tai/13-nhiem-vu-phu-thanh-tich-danh-hieu.md)).
>
> ⚠️ Nếu vẫn muốn hiển thị "còn N ngày" ở tab 45 thì thêm `{"param":36500,"id":93}` vào `Options` —
> nhưng option 93 trong `Options` chỉ là **chữ hiển thị**, không phải thời hạn thật.

#### Dòng cần thêm vào `item_shop` (để danh hiệu hiện trong shop Santa)

Tab **44 "Danh<>Hiệu"** và tab **45 "Sở Hữu<>"** (cùng thuộc shop id **26**) hiện mỗi tab liệt kê **19 item danh hiệu**.
Nếu không thêm, người chơi **không xem/không đổi được** hai danh hiệu mới ở NPC 39 Santa.
`item_shop` hiện có 826 dòng, `id` lớn nhất = **998**.

```sql
INSERT INTO `item_shop` (`id`, `tab_id`, `temp_id`, `is_new`, `is_sell`, `type_sell`, `cost`, `icon_spec`) VALUES
(999,  44, 2120, 1, 0, 0, 0, 0),
(1000, 44, 2121, 1, 0, 0, 0, 0),
(1001, 45, 2120, 1, 0, 0, 0, 0),
(1002, 45, 2121, 1, 0, 0, 0, 0);
```

> `TabShopDanhHieu` (dòng 45) thêm option **220 "Hoàn thành #%"** vào mỗi item;
> `TabShopSoHuu` (dòng 38) dùng option **93** để hiện số ngày còn lại.
> Cả hai gọi `BagesTemplate.fineIdEffectbyIdItem(itemShop.temp.id)` ⇒ **bắt buộc phải có dòng `data_badges`
> với `idItem` khớp**, nếu không hàm trả **-1** và phần trăm luôn bằng 0
> (đúng lỗi đang xảy ra với các danh hiệu 1286, 1287, 1300 — [13 ghi chú 21](../1-he-thong-hien-tai/13-nhiem-vu-phu-thanh-tich-danh-hieu.md)).

#### Tổng kết: 2 danh hiệu cần thêm bao nhiêu dòng vào bảng nào

| Bảng | Số dòng thêm | Nội dung |
|---|---|---|
| `item_template` | **2** | id 2120, 2121 — TYPE 36 |
| `data_badges` | **2** | id 19 (idEffect 257 → idItem 2120), id 20 (idEffect 258 → idItem 2121) |
| `item_shop` | **4** | 2 dòng tab 44 + 2 dòng tab 45 |
| `task_badges_template` | **0** | Không cần — danh hiệu trao thẳng từ `doneTask`, không đi qua nhiệm vụ danh hiệu hằng ngày |
| **Tài nguyên client** | **10 file** | `data/effdata/DataEffect_257`, `DataEffect_258` + `data/effect/x1..x4/ImgEffect_257.png`, `ImgEffect_258.png` |

#### Phương án không tạo danh hiệu mới

Dùng lại 2 danh hiệu **đã có item TYPE 36 nhưng chưa gắn nhiệm vụ nào**:

| Dùng lại | item id | TYPE | icon_id | part | Có dòng `data_badges` không? |
|---|---|---|---|---|---|
| Chiến thần khóa nick | 1288 | 36 | 11607 | 217 | **Không** → vẫn phải thêm dòng `data_badges` mới với `idEffect` có tài nguyên |
| Tay nhanh hơn não | 1673 | 36 | 13634 | 254 | **Không** → như trên |

⇒ Tiết kiệm được **2 dòng `item_template`** và việc tăng `vsItem` (nếu đây là item mới duy nhất),
nhưng **vẫn phải thêm `data_badges` + `item_shop` + tài nguyên hiệu ứng**, và phải **đổi tên trong DB**
(người chơi cũ có thể thấy lạ). Lợi ích nhỏ, rủi ro nhầm lẫn lớn ⇒ **không khuyến nghị**.

---

## 7. Ghi chú / điểm cần chủ dự án quyết

### 7.1 Ba việc BẮT BUỘC, không phải tùy chọn

| # | Việc | Vì sao bắt buộc |
|---|---|---|
| 1 | **Lấp đầy mọi id trống trong dải 2000 → id lớn nhất** bằng dòng trống TYPE 75 | `ItemService.getTemplate(id)` = `ITEM_TEMPLATES.get(id)` lấy theo chỉ số mảng (§0.2). Bỏ sót một id là **lệch toàn bộ bảng item** |
| 2 | **Tăng `DataGame.vsItem` từ 9 → 10** | Không tăng thì client cũ dùng bảng item đang cache; item mới hiện rỗng hoặc gây văng (§1 bước 13) |
| 3 | **Đổi id 2040 → 2045 và 2074 → 2076**, hoặc dọn hai mảng ở `ItemService.java` dòng 598 & 638 | Hai id này đã bị hardcode trong code hiện tại (§4.3) |

### 7.2 Hai lỗi kỹ thuật nên sửa nhân dịp này

| # | Chỗ | Vấn đề | Đề xuất |
|---|---|---|---|
| 1 | `Manager.java` dòng 629 | `SELECT * FROM item_template LIMIT ? OFFSET ?` **không có `ORDER BY`**. Thứ tự trả về đang đúng nhờ khóa chính InnoDB, nhưng không có gì bảo đảm sau khi `OPTIMIZE TABLE` / đổi engine / nâng cấp MySQL. Nếu thứ tự lệch, **toàn bộ bảng item của server lệch** | Đổi thành `SELECT * FROM item_template ORDER BY id LIMIT ? OFFSET ?`. **1 dòng, rủi ro 0, phòng được sự cố nặng nhất** |
| 2 | `ShopService.java` dòng 1041 & 1096 | `if (cost == 0) cost = 1;` khiến **mọi** item `gold = 0` đều bán được 1 vàng, kể cả vật phẩm nhiệm vụ | Thêm danh sách id vật phẩm nhiệm vụ vào chỗ đang chặn id 570 (§1 bước 12) |

### 7.3 Năm câu cần chủ dự án trả lời

1. **Tạo item mới hay dùng item sẵn có?**
   - Tạo mới (32 item + 90 dòng trống): đẹp, sạch, nhưng **phải tăng `vsItem`** ⇒ mọi client tải lại bảng item một lần.
   - Dùng lại: không đụng client, nhưng **6 nhóm rủi ro ở §5.2**, trong đó nặng nhất là 7 Mảnh Ký Ức = 7 viên Ngọc Rồng
     (người chơi ước rồng là mất, và mua được trên chợ).
   - **Khuyến nghị của tôi: phương án lai §5.3** — tạo mới 9 item trục truyện, dùng lại 23 item phụ.

2. **TYPE cho vật phẩm nhiệm vụ: theo thiết kế (11 / 27) hay theo khuyến nghị (8 / 27)?**
   Thiết kế 20a đặt TYPE **11** cho vật phẩm nhiệm vụ, nhưng TYPE 11 trong server này là **"đeo lưng / flag bag"**
   và `part = -1` sẽ gây lỗi hiển thị. TYPE **8** mới đúng nghĩa "vật phẩm nhiệm vụ" (§0.6).

3. **Danh hiệu kết thúc tuyến: 30 ngày hay vĩnh viễn?**
   Nếu vĩnh viễn thì trao thẳng bằng `new BadgesData(player, idEffect, 36500)` trong `doneTask`, không đi qua
   `BadgesTaskService` (§6.4). Cần chốt vì ảnh hưởng cách viết code.

4. **`idEffect` của 2 danh hiệu: làm hiệu ứng mới (257/258) hay mượn hiệu ứng cũ?**
   20c ghi 260/261 nhưng **hai id đó không có file tài nguyên nào** (§6.3). Nếu làm mới thì cần
   **10 file ảnh/dữ liệu hiệu ứng**, phải có người vẽ.

5. **Có chặn bán / chặn vứt vật phẩm nhiệm vụ không?**
   Nếu không vá, người chơi **bán được vật phẩm nhiệm vụ với giá 1 vàng** và **vứt được**, rồi kẹt nhiệm vụ.
   Danh sách "Đã bán" chỉ giữ **10 món gần nhất** nên không cứu được mọi trường hợp.
   Vá 3 chỗ (§1 bước 12) tốn khoảng 10 dòng code.

### 7.4 Những điều đã kiểm chứng và những điều CHƯA kiểm chứng được

**Đã kiểm chứng trực tiếp từ code/DB/tài nguyên:**

- 15 cột `item_template`, kiểu dữ liệu, cách server đọc từng cột.
- `item_option_template` chỉ có 2 cột, 251 dòng, id 0–250.
- Id lớn nhất `item_template` = **1999**, không thiếu id, không trùng id, 141 dòng trống có sẵn.
- `getTemplate(id)` lấy theo chỉ số mảng ⇒ luật id liên tục.
- Client nhận 9 trường/item, theo thứ tự, không nhận id / gold / gem / head / body / leg.
- `vsItem = 9`; icon đọc từ `data/icon/x{zoom}/<icon_id>.png`; x1 có 15.121 file, x2/x3 16.358, x4 16.478.
- **Cả 30 icon mà 20a/20b/20c đề xuất mượn đều có đủ ở x1–x4**, và `icon_id` ghi trong 3 file thiết kế **đều khớp đúng** với item gốc.
- TYPE 8 / 11 / 27 / 36 khác nhau thế nào về mặc / dùng / giao dịch / ký gửi.
- `gold = 0` không chặn bán (`cost == 0 → 1`).
- 23 id ≥ 2000 đang bị hardcode trong 5 file mã nguồn.
- `data_badges` 18 dòng, cấu trúc 5 cột, `idEffect` 260/261 **không có tài nguyên**; 257/258 hoàn toàn trống.
- Tab shop 44 "Danh<>Hiệu" và 45 "Sở Hữu<>" thuộc shop 26, mỗi tab 19 item danh hiệu.

**Chưa kiểm chứng được (không có mã nguồn client trong kho này):**

- Client có hiển thị nút **"Dùng"** cho item TYPE 8 hay không. Đây là lý do tôi khuyến nghị giữ **TYPE 27**
  cho 5 item bắt buộc bấm "Dùng", thay vì chuyển hết sang TYPE 8. **Cần test thực tế trên client trước khi chốt.**
- Client xử lý thế nào khi `part = -1` với item TYPE 11 (suy đoán là lỗi hiển thị, chưa xác nhận).
- Ngưỡng số lượng item template tối đa mà client bản cũ chịu được. `ItemData` gửi `writeShort` nên server
  chịu được tới 32.767, nhưng **client cũ có thể cấp phát mảng cố định**. **Phải test trên client bản cũ nhất
  mà server còn phục vụ** trước khi phát hành.

---

*Hết file 21b. Kiểm toán vật phẩm thưởng: 21a. Cân bằng rơi đồ boss: 21c.*
