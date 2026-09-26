# 68 — Quản lý đồ rơi boss ở cpanel + NPC "Theo Dõi Boss"

Ngày: 2026-09-26 · Patch SQL: `SRC/sql/patch/80-npc-theo-doi-boss.sql`

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
| **Boss đang ra map** | danh sách con nào đang đứng ngoài, kèm **tên map** |
| **Danh sách tất cả** | mọi loại boss, `●` đang ra map / `○` đang nghỉ, 8 con một trang |
| *(bấm vào một con)* | trạng thái, map đang ở, máu, và **bảng rơi kèm tỉ lệ** |

Bảng rơi hiện ở đây đọc **thẳng từ `BangRoiBoss`** — tức đúng bảng mà quản trị vừa sửa ở
cpanel, không phải bảng chép tay nên không bao giờ lệch.

**Chỉ hiện tên map, không hiện khu** — đúng yêu cầu, để không thành công cụ canh boss quá dễ.

Boss chưa khai báo dòng nào thì ghi thật thà: *"Chưa khai báo bảng rơi. Con này rơi theo luật
riêng của nó."*

## 5. Kiểm tra đã làm

* Biên dịch sạch **JDK 21**, 694 file class.
* Script rà lại toàn bộ 179 file boss: **0 lớp** lọt lưới bảng rơi.
* Dựng CSDL nháp từ dump + toàn bộ patch (bỏ `36-go-vat-pham-ngol.sql` — file lùi lại),
  patch 80 chạy sạch, 4/4 mục kiểm tra `= 1`, avatar tra ra đúng 18516.
* Part 2448 / 2449 / 2450 đủ mảnh, không thiếu file icon nào ở cả 4 mức phóng to.
* Tile map 5 tại (914, 408): nền đặc, cùng bệ với Santa.

## 6. Việc người vận hành phải làm

1. Tắt server.
2. `mysqldump -u root -p team2026 npc_template map_template > backup_80.sql`
3. Chạy `SRC/sql/patch/80-npc-theo-doi-boss.sql`, xem khối (3) phải toàn `1`.
4. Bật server bản jar mới (`vsMap = 10`).
5. Vào cpanel tab **Boss** → chọn boss → **Đồ rơi của boss đã chọn...** để chỉnh.
