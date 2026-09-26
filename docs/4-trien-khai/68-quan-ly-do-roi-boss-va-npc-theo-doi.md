# 68 — Quản lý đồ rơi boss ở cpanel + NPC "Theo Dõi Boss"

Ngày: 2026-09-26 · Patch SQL: `SRC/sql/patch/80-npc-theo-doi-boss.sql` + `81` + `82`

---

## 1. Vấn đề

Trước đây đồ rơi của boss **viết cứng trong từng lớp boss**: 85 file, 237 lệnh
`dropItemMap`, mỗi con một kiểu (có con lặp `for`, có con random số lượng). Muốn đổi tỉ lệ
một món là phải sửa code rồi build lại jar — cpanel không đụng tới được.

## 2. Cách làm: một bảng rơi đọc chung

`SRC/src/nro/models/boss/drop/BangRoiBoss.java` + `MucRoi.java`

Mỗi boss (tra theo `boss.id`, tức hằng số trong `BossID`) có một danh sách mục rơi:

| Cột | Ý nghĩa |
|---|---|
| **Id vật phẩm** | một hoặc nhiều id cách nhau bởi dấu phẩy (`16,17,18`) — trúng thì bốc ngẫu nhiên một cái |
| **SL nhỏ nhất / lớn nhất** | số lượng, random trong khoảng |
| **Tỉ lệ %** | 0–100 |
| **Nhóm** | `0` = quay riêng · `> 0` = các dòng cùng số nhóm chung MỘT vòng quay 100 %, tỉ lệ là phần của vòng |
| **Ghi chú** | ghi cho người đọc, không ảnh hưởng gì |

Cột **Nhóm** là thứ cho phép diễn tả cả hai kiểu rơi mà game đang dùng: "tung 10 % xem có rơi
không" (nhóm 0) và "chắc chắn rơi một món, 40 % ngọc rồng / 5 % gậy…" (nhóm 1).

Sửa xong bấm **Áp dụng** là có hiệu lực ngay lần hạ boss kế tiếp; bấm **Áp dụng và lưu** thì
ghi `data/bossdrop_table.json` để khởi động lại vẫn còn. File không có thì dùng bảng gốc viết
trong code.

### Hai nguồn đồ rơi: "gốc" và "thêm"

| Nguồn | Ở đâu | Sửa được không | Có thả thật không |
|---|---|---|---|
| **Gốc** | `reward()` viết tay trong từng lớp boss, được **chép lại** thành khai báo ở `BangRoiBoss.khaiBaoGoc()` | không (phải sửa code boss) | **có** — code boss vẫn thả |
| **Thêm** | bảng `BangRoiBoss`, sửa ở cpanel, lưu `data/bossdrop_table.json` | có | **có** — `BangRoiBoss.roi` thả |

`khaiBaoGoc()` là **bản chép tay chỉ để hiện lên cho người xem**; `roi()` KHÔNG thả nó, vì code
của boss vẫn đang thả. Thả nữa là rơi đôi.

> ⚠️ **Sửa `reward()` của boss nào thì nhớ sửa `khaiBaoGoc()` cho khớp.** Không có cách nào đọc
> ngược từ code ra bảng: 85 file boss, 237 lệnh thả đồ, mỗi con một kiểu (lặp `for`, random số
> lượng, gắn chỉ số shop nhân thêm 100–115 %, gắn sao pha lê theo bậc…).

Đã chép sẵn cho toàn bộ boss thế giới đang có: Tiểu Đội Trưởng (2 bản), Kuku, Map Đầu Đinh,
Rambo, Fide, Dr.Kore, Android 14, King Kong, Xên Bọ Hung, Xên Hoàn Thiện, Cooler, Black Goku,
Cumber, Baby, Bojack, Super Bojack, Golden Frieza, Ăn Trộm, Ở Dơ, Sói Hẹc Quyn, Mặt Trời, và
bộ Lốp Trưởng. Riêng **Lốp Trưởng đọc thẳng `BossDropConfig`** (tab "Rơi đồ boss") nên không
bao giờ lệch.

Đồ Thần Linh không có id cố định (bốc ngẫu nhiên lúc rơi) nên hiện thành một dòng riêng
"Đồ Thần Linh ngẫu nhiên — theo máu boss (1–5%)".

### Quan hệ với đồ rơi cũ

Bảng này là phần **THÊM**, chạy **sau** `reward()` của boss. Boss cũ giữ nguyên đồ rơi viết
trong code của chúng — thêm dòng ở đây là boss đó rơi thêm, **không mất gì**.

Riêng **năm con mới** (Fu Thời Không, Fu Hợp Thể, Vegeta Siêu Thần God, Goku Siêu Thần God,
Lão Dê Hồi Xuân) đã được **gỡ hết phần rơi viết cứng** và chuyển vào bảng này, nên với chúng
bảng này là **toàn bộ** đồ rơi — sửa từ cpanel là đổi được sạch.

### Móc vào đâu để boss nào cũng chạy

Chỗ này là phần khó nhất, xin ghi rõ để sau khỏi phải dò lại:

* `die()` **không** dùng được một mình — **31 lớp boss ghi đè `die()` mà không gọi `super.die()`**
  (Ma Bư 12h/14h, doanh trại, phó bản, Broly, Pocolo, Whis, boss nhiệm vụ…).
* `reward()` cũng không — hơn 40 lớp ghi đè mà không gọi `super.reward()`.
* Thứ **mọi** đường chết đều đi qua là `changeStatus(BossStatus.DIE)`, và `changeStatus` chỉ
  được định nghĩa ở đúng `Boss.java`, không lớp con nào ghi đè.

Nên: **thả đồ ở `Boss.changeStatus()` lúc chuyển sang DIE**, còn người hạ boss thì ghi nhớ ở
`Boss.setDie(Player)` và `Boss.die(Player)` — hai chỗ mà gần như mọi lớp đều đi qua ít nhất
một. Chỉ còn đúng hai con `MatTroi` và `Virut` lọt lưới (ghi đè cả `die()` lẫn `injured()` mà
không gọi `setDie`), đã thêm tay một dòng `ghiNhoNguoiHa(plKill)` cho hai file đó.

**Đã rà lại bằng script: 0 lớp boss còn lọt lưới.**

## 3. cpanel — tab Boss, nút "Đồ rơi của boss đã chọn..."

`SRC/src/nro/models/cpanel/BossDropTableDialog.java`

Chọn một boss trong bảng rồi bấm nút → hộp thoại sửa bảng rơi của đúng con đó:
thêm dòng, xoá dòng, sửa id / số lượng / tỉ lệ / nhóm, áp dụng, lưu file, về mặc định.

Cột **Tên** tự tra tên vật phẩm từ bảng đang chạy để khỏi nhập nhầm id.

Chặn trước khi áp dụng: id trống, tỉ lệ ngoài 0–100, số lượng vô lý, và **tổng một nhóm quay
chung vượt 100 %** (những dòng cuối nhóm sẽ không bao giờ trúng).

## 4. NPC 88 "Theo Dõi Boss"

`SRC/src/nro/models/npc_list/TheoDoiBoss.java`

* Đứng **bên trái Santa** ở đảo Kamê (map 5): Santa ở x = 984, NPC mới ở **x = 914**, cách
  70 px — hơn bán kính 60 px của `Map.getNpc` nên bấm không lộn NPC. Đã tính lại tile map 5:
  ô (914, 408) là nền đặc.
* Ngoại hình: cải trang **"Siêu Goku Vô Cực"** (item 2150, part 2448 / 2449 / 2450, mang từ
  SUMO ở patch 67), avatar 18516. Đủ mảnh 3 / 17 / 14, không thiếu file icon nào.

### Menu

| Mục | Nội dung |
|---|---|
| **Boss đang ra map** | **menu nhiều trang**, mỗi nút một con kèm **tên map** ngay dưới tên |
| **Danh sách tất cả** | **menu nhiều trang**, `●` đang ra map / `○` đang nghỉ, 8 con một trang |
| *(bấm vào một con)* | trạng thái, map đang ở, máu, và **bảng rơi kèm tỉ lệ** |

Bảng rơi hiện ở đây đọc **thẳng từ `BangRoiBoss.xem()`** (gốc + thêm) — tức đúng bảng mà quản trị vừa sửa ở
cpanel, không phải bảng chép tay nên không bao giờ lệch.

> Cả hai danh sách đều là **menu nhiều trang**, không phải một khối chữ. Server có hơn trăm bản
> boss — in hết ra là tràn khung, phải cắt bớt ("… và 5 loại nữa") và người chơi không bấm vào
> con nào được. Hai dải menu riêng: `THEO_DOI_BOSS_TRANG` 1040–1059 cho danh sách tất cả,
> `THEO_DOI_BOSS_TRANG_RA` 1060–1079 cho danh sách đang ra map.

### Bảng đồ rơi hiện bằng giao diện TIỆM, không phải chữ chay

> **Bảng chỉ liệt kê MÓN, không hiện tỉ lệ** (chủ dự án chốt). Xem tỉ lệ thì vào cpanel.
> Dòng chỉ số 253 "Tỉ lệ rơi #%" thêm ở patch 81 vì vậy không còn ai dùng và đã được
> **patch 82 gỡ lại**, trả ô đó về cho bảng chỉ số (bảng này trần 255 dòng).

`ShopService.moBangXem(...)` + patch 81 (`item_option_template` 253 `'Tỉ lệ rơi #%'`)

Bấm vào một con boss thì bảng đồ rơi mở ra đúng khung tiệm quen thuộc: **icon thật của từng
vật phẩm, tên vật phẩm, dòng chữ màu ghi tỉ lệ**, bấm vào món nào thì hiện khung mô tả như lúc
xem hàng trong tiệm.

Khung này **chỉ để xem, không mua bán được**:

* gửi bằng gói tin tiệm type 4 (khung "Phần thưởng") với thẻ `ITEMS_REWARD` — `takeItem` gặp
  thẻ này thì trả về ngay, bấm dòng nào cũng không nhận được gì;
* **không** đăng ký `idMark.setShopOpen`, nên không có đường nào lọt sang `buyItem`.

Một dòng khai nhiều id (kiểu `16,17,18` = Ngọc Rồng 3–5 sao) được tách thành nhiều ô, mỗi ô
một vật phẩm thật, kèm chữ "1 trong 3 món — 40%".

> **Vì sao danh sách BOSS vẫn là menu chữ.** Trong gói tin tiệm, tiêu đề mỗi dòng **bắt buộc**
> là tên vật phẩm — client tra `item_template` theo id chứ server không gửi chữ tự do cho dòng.
> Muốn mỗi dòng mang tên một con boss thì phải đẻ ra khoảng 50 vật phẩm giả, trong khi ngân
> sách gói tin vật phẩm **chỉ còn chỗ cho ~41 món**. Hai chỗ đặt chữ tự do duy nhất là **tên
> tab** và **dòng chữ màu** của từng dòng, và cả hai đã được tận dụng: tab mang tên boss +
> map, dòng chữ màu mang tỉ lệ.

### Ngân sách còn lại sau patch 81

Sau khi patch 82 gỡ lại dòng 253, `item_option_template` về **253 dòng trên trần 255**
(số dòng gửi bằng `writeByte`) — **còn 2 ô trống**.

**Chỉ hiện tên map, không hiện khu** — đúng yêu cầu, để không thành công cụ canh boss quá dễ.

Boss chưa khai báo dòng nào thì ghi thật thà: *"Chưa khai báo bảng rơi. Con này rơi theo luật
riêng của nó."*

## 5. Sửa lỗi "nút bị ẩn" ở cpanel

`SRC/src/nro/models/cpanel/WrapLayout.java`

Tab **Boss** giờ có 7 nút thao tác. `FlowLayout` gốc vẫn xếp tất cả thành **một hàng** khi tính
kích thước mong muốn, mà panel lại nằm trong `BorderLayout.NORTH` nên chỉ cao đúng một hàng:
nút nào vượt quá bề ngang cửa sổ là **bị cắt mất, không bấm được**.

Thay bằng `WrapLayout` — bản `FlowLayout` biết xuống dòng, tính lại chiều cao theo bề ngang
thật của khung chứa. Đã đổi cho cả 8 panel nút của cpanel (`BossTab`, `BossDropTab`,
`BossDropTableDialog`, `AccountTab`, `CharacterTab`, `GoldDropTab`, `OnlineTab`,
`BossTuneDialog`) để sau này thêm nút nữa cũng không tái diễn.

Đo lại bằng script (dựng đúng panel đó rồi kiểm tra biên từng nút):

| Panel | Bề ngang | FlowLayout cũ | WrapLayout mới |
|---|---|---|---|
| Tab Boss (7 nút) | 1100 px | **3 nút bị cắt** | đủ (panel cao 96 px thay vì 63) |
| Tab Boss (7 nút) | 860 px | **4 nút bị cắt** | đủ |
| Rơi đồ boss (6 nút) | 860 px | **1 nút bị cắt** | đủ |
| Bảng đồ rơi (6 nút) | 860 px | **1 nút bị cắt** | đủ |

## 6. Kiểm tra đã làm

* Biên dịch sạch **JDK 21**, 695 file class.
* Script rà lại toàn bộ 179 file boss: **0 lớp** lọt lưới bảng rơi.
* Dựng CSDL nháp từ dump + toàn bộ patch (bỏ `36-go-vat-pham-ngol.sql` — file lùi lại),
  patch 80 chạy sạch, 4/4 mục kiểm tra `= 1`, avatar tra ra đúng 18516.
* Part 2448 / 2449 / 2450 đủ mảnh, không thiếu file icon nào ở cả 4 mức phóng to.
* Tile map 5 tại (914, 408): nền đặc, cùng bệ với Santa.

## 7. Việc người vận hành phải làm

1. Tắt server.
2. `mysqldump -u root -p team2026 npc_template map_template item_option_template > backup_80.sql`
3. Chạy `SRC/sql/patch/80-npc-theo-doi-boss.sql`, xem khối (3) phải toàn `1`.
4. Chạy `SRC/sql/patch/81-dong-ti-le-roi.sql` rồi `82-bo-dong-ti-le-roi.sql`
   (81 thêm dòng 253, 82 gỡ lại — chạy cả hai, hoặc bỏ qua cả hai, đều ra `so_dong` = **253**).
5. Bật server bản jar mới (`vsMap = 10`, `vsItem = 30`).
6. Vào cpanel tab **Boss** → chọn boss → **Đồ rơi của boss đã chọn...** để chỉnh.
