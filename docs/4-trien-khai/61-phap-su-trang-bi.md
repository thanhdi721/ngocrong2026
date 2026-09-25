# 61 — Pháp sư trang bị (2026-09-25)

Lấy ý tưởng từ `PHAP_SU_TRANG_BI` bên source Bun, **viết lại** cho hợp server mình chứ
không bê nguyên.

## Luật

**Nâng** — Bà Hạt Mít (đảo Kamê) → *Pháp sư Trang bị*:
* Đặt **1 trang bị** + **20 Đá Pháp Sư**, trả **200.000.000 vàng**, tỉ lệ **100%**.
* Mỗi lần **bốc ngẫu nhiên 1 trong 7 dòng** rồi cộng vào đó; trúng trùng dòng cũ thì **cộng dồn**.
* **Một món chỉ nâng được 6 lần.** Hết 6 lần mà chỉ số không ưng thì chỉ còn cách **tẩy sạch
  rồi nâng lại từ đầu** — đó là chỗ tạo nhu cầu đi săn đá.

**Tẩy** — *Tẩy Pháp sư*: đặt trang bị đã pháp sư + **5 Đá Tẩy Pháp Sư**, trả **500 ngọc**,
gỡ **sạch** mọi dòng Pháp Sư (không đụng các dòng khác của món đồ), món đồ về 0/6.

## Bảy dòng — dùng LẠI option có sẵn

| Dòng | Id option | Mỗi lần trúng | Nếu dồn hết 6 lần |
|---|---|---|---|
| Sức đánh | 50 | +2% | +12% |
| HP | 77 | +2% | +12% |
| KI | 103 | +2% | +12% |
| Giáp | 47 | +100 | +600 |
| Giảm sát thương | 94 | +1% | +6% |
| Né đòn | 108 | +1% | +6% |
| Xuyên giáp | 98 + 99 | +1% (cả chưởng lẫn cận chiến) | +6% |

Bảng option chỉ thêm **đúng một dòng mới**: id 251 `Pháp Sư cấp #` làm tem đếm số lần đã nâng.

**Vì sao không đẻ 7 dòng riêng** (bản đầu tôi làm vậy và nó **làm hỏng client**): gói tin gửi
bảng option ghi **số dòng bằng MỘT byte** (`ItemData.updateItemOptionItemplate`), và mỗi dòng
chỉ số của món đồ cũng ghi **id bằng một byte**. Bảng đang có 251 dòng (id 0–250) nên trần là
**255**. Thêm 7 dòng thành 258 → byte ghi ra là **2** → client đọc 2 dòng rồi lệch cả gói.
Sau khi sửa: **252 dòng, id lớn nhất 251** — còn dư 3 chỗ.

Hệ quả của việc dùng chung id: lúc tẩy không phân biệt được dòng nào do pháp sư cộng, nên
chức năng **chỉ nhận món chưa có chỉ số nào** (cải trang / đeo lưng / linh thú bên mình vốn
không có), và tẩy thì gỡ sạch cả 9 id kể trên.

## Xác suất (mô phỏng 200.000 món)

Bốc 6 lần trong 7 dòng nên gần như không ai dồn được hết vào một dòng:

| Số lần trúng cùng một dòng cụ thể | Tỉ lệ | Ví dụ sức đánh |
|---|---|---|
| 0 | 39,7% | +0% |
| 1 | 39,7% | +2% |
| 2 | 16,5% | +4% |
| 3 | 3,65% | +6% |
| 4 | 0,46% | +8% |
| 5 | 0,03% | +10% |

Nhìn theo "dòng dồn được nhiều nhất của món đồ": 66,9% số món chỉ dồn được 2 lần vào một dòng,
25,4% được 3 lần, 3,25% được 4 lần. Muốn một món "chuẩn sức đánh" thì trung bình phải làm lại
mấy chục lần — mỗi lần làm lại tốn 6 × (200tr + 20 đá) để nâng, cộng 5 đá tẩy + 500 ngọc.

## Đồ nào pháp sư được

**Cải trang (type 5), đeo lưng (type 11), linh thú (type 27 có đủ 3 part)** — đúng ba họ đồ
của mình **vốn không có chỉ số gì**, nhất là 138 món vừa mang từ SUMO về. Bên Bun cho pháp sư
"trang bị pet / phụ kiện bang" — hai thứ server mình không có nên bỏ.

## Những chỗ KHÔNG bê theo Bun

* **Id option**: Bun dùng 197–200, mà bên mình 197–200 đang là "Tấn công +#% lên tộc Xayda",
  "Giảm #% sát thương từ tộc Trái Đất/Namếc/Xayda". Bê nguyên là trang bị cộng nhầm kháng hệ.
  Đã cấp id mới **251–257** nối tiếp cuối bảng (bảng option client đọc **theo thứ tự**).
* **Cách cộng**: Bun cộng thẳng 10 lần mỗi lần ép và không có trần. Bên mình 1 cấp/lần, có trần,
  vì giá anh/chị chốt là 200tr + 20 đá mỗi lần.
* **Bảng `item_option_template` của mình chỉ có (id, NAME)** — không có cột TYPE như bên họ.

## File

| | |
|---|---|
| `SRC/src/nro/models/combine/PhapSuTrangBi.java` | toàn bộ luật (mới) |
| `SRC/src/nro/models/combine/CombineService.java` | hằng số 991/992 + 4 chỗ rẽ nhánh |
| `SRC/src/nro/models/npc_list/BaHatMit.java` | 2 nút mới trong menu map 5 |
| `SRC/src/nro/models/player/NPoint.java` | 7 `case` cộng chỉ số |
| `SRC/src/nro/data/...` → `DataGame` | `vsItem` 25 → 26 |
| `sql/patch/75-phap-su-trang-bi.sql` | 7 dòng option + 2 viên đá |
| `sql/patch/76-giftcode-da-phap-su.sql` | `daps001`, `daps002` (200 đá), `datay01` (50 đá tẩy) |
| `data/icon/x1..x4/20257, 20258` | icon hai viên đá, lấy từ Bun (đá xanh 30498, đá đỏ 30501) |

## Cân bằng — chỗ cần anh/chị quyết

Một món 6 lần nâng tốn **1,2 tỷ vàng + 120 đá**. Tổng chỉ số cộng thêm của một món luôn là
6 lần bốc, chỉ khác ở chỗ rơi vào dòng nào — trung bình mỗi món được khoảng +4% sức đánh,
+4% HP, +4% KI, +200 giáp, +2% mỗi dòng phòng thủ. Pháp sư được **3 món** (cải trang + đeo lưng
+ linh thú) nên tổng cộng cỡ **+12% mỗi loại** nếu chia đều, hoặc lệch hẳn về một dòng nếu may.

Thấy mạnh quá / yếu quá thì chỉnh hai chỗ ở đầu `PhapSuTrangBi.java`: `SO_LAN_TOI_DA` (số lần
nâng mỗi món) và cột "mỗi lần trúng" trong mảng `DONG`.

**Chưa quyết**: Đá Pháp Sư hiện chỉ có qua giftcode. Cần chọn nguồn thật — bán ở shop, rơi từ
boss (thêm vào tab "Rơi đồ boss" của cpanel), hay phần thưởng sự kiện.

## Chưa kiểm tra được

Chưa vào game. Đã dịch bằng JDK 21 (681 file .class) và chạy thử toàn bộ 76 patch trên DB nháp
dựng từ dump: không câu nào lỗi, bảng option vẫn liên tục 0–257.

## Tự soát lại (2026-09-25)

Đã dò những chỗ dễ gãy, ba thứ phải sửa thêm:

1. **Đồ gộp chồng được**: bên mình có 6 cải trang (CT Cađíc các loại) và 1 đeo lưng
   (Diều rồng băng) để `is_up_to_up = 1`. Nạp chỉ số lên một CHỒNG nhiều cái vừa vô lý vừa
   dễ sinh chuyện khi gộp/tách, nên `phapSuDuoc` bỏ hẳn đồ gộp chồng được.
2. **Đặt nhiều món cùng lúc**: trước đây lặng lẽ chỉ xử món đầu tiên. Nay đếm, quá 1 món thì
   báo "Mỗi lần chỉ pháp sư được MỘT món".
3. **Quên chạy patch 75**: bảng option tra theo **chỉ số mảng**
   (`ITEM_OPTION_TEMPLATES.get(id)`), chạy jar mới mà chưa chạy patch là văng
   IndexOutOfBounds ngay khi ép. Nay kiểm tra trước, báo "hãy chạy patch 75" và ghi log.

Những chỗ đã soi và thấy không sao:

* Hai viên đá không bị nhận nhầm là trang bị (type 27 nhưng `head = -1`, lại gộp chồng được).
* `combineNew.itemsCombine` giữ **tham chiếu** tới đúng món trong hành trang (khung
  `nguyenLieuConHopLe` đối chiếu bằng `==`), nên sửa `itemOptions` là ăn vào đồ thật và được lưu.
* Đồ gộp chồng chỉ gộp khi **danh sách option giống hệt** (`checkListsEqual`), nên không có
  chuyện gộp mất chỉ số — dù vậy vẫn chặn ở mục 1 cho chắc.
* `param` gửi cho client bằng `writeShort`; số lớn nhất ở đây là Giáp +600, thừa chỗ.
* Chỉ số chỉ ăn khi **mặc lên người**: `NPoint.setPointWhenWearClothes` duyệt toàn bộ
  `itemsBody` nên cải trang / đeo lưng / linh thú đều được tính.
* Bấm "Từ chối" rơi vào `startCombineVip` — hàm này chỉ xử Pha lê hóa, với kiểu khác thì
  không làm gì ngoài việc xoá trạng thái, đúng như mong đợi.
* 30 hằng số trong `CombineService` không có giá trị nào trùng nhau (lỗi này từng làm gãy build).

## Sự cố ngày 25/09 — bản đầu làm hỏng client

Bản đầu thêm **7 dòng option mới (id 251–257)**. Bảng option lên **258 dòng**, mà
`ItemData.updateItemOptionItemplate` ghi số dòng bằng **một byte**: `258 & 0xFF = 2`.
Client nhận gói "bảng chỉ số", đọc đúng 2 dòng rồi lệch toàn bộ phần còn lại → hỏng.
Hai id 256 và 257 còn tệ hơn: khi gửi kèm món đồ chúng biến thành 0 và 1.

Đã sửa: bảy chỉ số dùng lại option sẵn có, bảng chỉ thêm một dòng (251). Kiểm tra lại sau khi
chạy hết 76 patch: **252 dòng, id lớn nhất 251** — dưới trần 255.

### Trần khác cần nhớ (đã rà lại cả bộ)

| Thứ | Giới hạn | Đang dùng |
|---|---|---|
| Số dòng `item_option_template` | 255 (1 byte) | **252** |
| Id option gửi kèm món đồ | 255 (1 byte) | 251 |
| Id `flag_bag` | 255 (1 byte) | 196 |
| Số dòng `npc_template` | 127 (1 byte có dấu) | 95 |
| Id hào quang | 127 (`getAura` trả byte) | 115 |
| Id icon | 32.767 (`Short.parseShort`) | 32.667 |
| Id vật phẩm | 32.767 (short) | 2.263 |
| **Dữ liệu vật phẩm gửi client** | **2 gói × 65.535 byte** | **127.726 byte — còn dư 2.274** |

Dòng cuối là chỗ sát trần nhất: chỉ còn chỗ cho **khoảng 50 vật phẩm nữa**. Muốn thêm nhiều
thì phải rút gọn cột `description` của đám vật phẩm cũ (vài món đang dài 100–150 ký tự).
