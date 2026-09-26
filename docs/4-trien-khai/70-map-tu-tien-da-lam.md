# 70 — Map Tu Tiên: đã làm xong

Ngày: 2026-09-26 · Patch SQL: `SRC/sql/patch/83-map-tu-tien.sql`
Bản thiết kế: [69-map-tu-tien-ban-thiet-ke.md](69-map-tu-tien-ban-thiet-ke.md)

---

## 1. Đổi so với bản thiết kế: dùng map 208 thay vì 207

Đo lại 5 map "tiên giới" của SUMO thì **Nam Thiên Môn (208)** rộng nhất:

| id | Tên | Kích thước | Bề ngang có nền |
|---|---|---|---|
| 206 | Tháp Tiên Môn | 696 × 504 | 480 px |
| 207 | Thái Cực Điện | 840 × 600 | 648 px |
| **208** | **Nam Thiên Môn** | **1200 × 600** | **1200 px — nền liền một dải** |
| 209 | Võ Đài Thiên Giới | 1056 × 576 | 1056 px |
| 210 | Tây Phương Cực Lạc | 1056 × 336 | 1056 px |

207 chỉ có 648 px nền, rải 8 con là chen chúc. 208 rải 8 con cách nhau 137 px vẫn thoải mái.

**Không thêm một file ảnh nào**: map dùng bộ tile 13 + nền 12 — đúng bộ mà *Thần Điện*,
*Tháp Karin*, *Hành tinh Kaio* đang dùng, client đã có sẵn. Chỉ chép file bản đồ
`data/map/tile_map_data/208` từ SUMO.

## 2. Map và quái

| | |
|---|---|
| Map | 208 "Nam Thiên Môn", **2 khu** |
| Quái | 8 con mỗi khu (**tổng 16**), xen kẽ Akkuman (27) và Quỷ Đầu Nhọn (44) — cả hai đi bộ |
| Vị trí | y = 432 (mặt nền), x = 120 … 1079, cách nhau 137 px |
| Máu | 20.000.000 |
| Sát thương quái | **50.000** |
| Trần sát thương lên quái | **10.000.000 mỗi đòn** ⇒ mạnh mấy cũng 2 đòn, yếu hơn thì nhiều đòn hơn |
| Hồi sinh | **30 giây** (map thường là 3 giây) |
| Rơi | **chỉ Linh Thạch, 15%** — không vàng, không đồ thường |
| Kinh nghiệm | **không có** |

### Vì sao phải gán sát thương quái bằng code

`map_template.mobs` chỉ có `[tempId, level, hp, x, y]` — **không có cột sát thương**. Để trống
thì `MobPoint.getDameAttack()` lấy **5% máu tối đa**: với 20 triệu máu thành **1 triệu sát
thương một đòn**, một phát là chết người chơi. Nên `TuTien.chuanBiQuai()` chạy lúc server dựng
xong bản đồ và gán `point.dame = 50.000` cho cả 16 con.

### Chặn sạch đồ rơi ở một chỗ

Chặn ngay đầu `Mob.getItemMobReward` và trả về luôn danh sách chỉ có Linh Thạch. Làm vậy thì
vàng, ngọc, đồ sự kiện, đồ kích hoạt… đều tắt hết cùng lúc, khỏi phải đi vá từng nhánh bên dưới
(hàm đó dài hơn 200 dòng với cả chục nhánh rơi đồ).

> **Một phát hiện khi đọc code: cảnh báo ở tài liệu 69 mục 3.1 là SAI.** Tôi tưởng mọi quái đều
> bị ép "10% máu tối đa mỗi đòn". Thật ra nhánh đó còn đòi `lvMob > 0`, mà `lvMob` **chỉ được
> gán cho quái phó bản** (Bản Đồ Kho Báu, Snake Way, Destron Gas, Red Ribbon) — quái map thường
> luôn để 0 nên **vẫn ăn sát thương thật**. Vì vậy trần 10 triệu là luật mới hoàn toàn, không
> phải sửa luật cũ.

## 3. Luật map

* **Vào map là tự bật cờ đen.** Móc ở `ChangeMapService` **sau** gói `mapInfo (-24)`, không đặt
  trong `goToMap` — `goToMap` chạy trước khi client biết map mới, gói đổi cờ gửi lúc đó dễ rơi mất.
* **Chết thì không hồi sinh tại chỗ**: `PlayerService.hoiSinh` chặn thẳng, `TuTien.kiemTraChet`
  đếm **3 giây** rồi đá về nhà theo hành tinh (map 21/22/23) và hồi đầy máu.
* Quái **đánh trả giãn 2,5 giây một lần**. Liên Hoàn bắn 330 ms/đòn — gọi đánh trả mỗi đòn thì
  quái xả liên tục, người chơi chết oan.

## 4. Mười vật phẩm (2266–2275)

| id | Tên | Loại | Giao dịch | Tác dụng |
|---|---|---|---|---|
| 2266 | Linh Thạch | 27 | **khóa** | tiền tệ của map |
| 2267 | Luyện Khí Đan | 29 | ✅ được | +20% sức đánh, 10 phút |
| 2268 | Hộ Thể Đan | 29 | ✅ được | +30% HP |
| 2269 | Tụ Khí Đan | 29 | ✅ được | +30% KI |
| 2270 | Kim Cương Đan | 29 | ✅ được | giảm 50% sát thương |
| 2271 | Phá Quân Đan | 29 | ✅ được | +10% chí mạng |
| 2272 | Ngọc Bội Hộ Mệnh | 4 | **khóa** | HP +10.000, **chỉ đệ tử** |
| 2273 | Ngọc Bội Tụ Linh | 4 | **khóa** | KI +10.000, **chỉ đệ tử** |
| 2274 | Ngọc Bội Phá Quân | 4 | **khóa** | Sức đánh +5.000, **chỉ đệ tử** |
| 2275 | Tụ Linh Phù | 29 | **khóa** | +50% tỉ lệ rơi Linh Thạch, 30 phút |

### Icon — đã phải làm lại một lượt

Lượt đầu tôi lấy icon theo **tên vật phẩm trong CSDL của SUMO/Bun**, và dính bẫy: tên bên đó
**không khớp với ảnh**. Bốn icon sai:

| Định dùng cho | Id lấy nhầm | Ảnh thật là gì |
|---|---|---|
| Phá Quân Đan | 27641 (SUMO gọi "Ngộ Năng") | **cái đầu người heo** |
| Ngọc Bội Hộ Mệnh | 31712 ("Ngọc Bội Nguyên Tố") | **đầu tóc xù** |
| Ngọc Bội Tụ Linh | 31713 | **đầu tóc xù** khác |
| Ngọc Bội Phá Quân | 31714 | **thân người** |

Ba id 3171x bên SUMO là **mảnh nhân vật (part)** chứ không phải icon vật phẩm — tên trong
`item_template` của họ trỏ sai.

Đã đổi sang bộ đúng, và lần này **xem ảnh từng cái trước khi chép**:

* **5 đan**: bộ bình luyện đan 24883–24895, phối màu theo tác dụng — đỏ (sức đánh), xanh lá
  (HP), xanh dương (KI), vàng (giáp), tím (chí mạng).
* **3 ngọc bội**: bùa ngọc tròn có tua đỏ, cùng ba màu xanh lá / xanh dương / đỏ cho khớp đan.
  Ảnh gốc mang id 15533/15534/15535 nhưng **ba id đó bên mình đã là quần áo**, nên đã
  **đánh số lại thành 20260 / 20261 / 20262** trước khi chép — đúng cách đã làm ở đợt res SUMO.

Đã kiểm lại: cả 10 icon đủ 4 mức phóng to, **không id nào trùng với vật phẩm sẵn có**.

**Đúng yêu cầu: chỉ đan giao dịch được**, còn lại gắn dòng chỉ số 30 "Không thể giao dịch" ngay
lúc rơi / lúc mua.

Bốn buff đầu dùng lại khung `ItemTime` sẵn có (`TIME_ITEM` đúng bằng 10 phút). **Buff chí mạng
là mới** — thêm cờ vào `ItemTime`, cộng vào `NPoint.tlDameCrit`. Cả 6 đều có đồng hồ đếm ngược
trên màn hình (`ItemTimeService`).

### Ngọc bội — chỉ đệ tử đeo được

Ngọc bội để **type 4 (ô rađa)** vì đệ tử chắc chắn mặc được ô đó. Nhưng như vậy chủ nhân cũng
đeo được, nên chặn thêm hai chỗ:

* `InventoryService.putItemBody`: không phải đệ tử thì từ chối, báo *"Ngọc bội chỉ đệ tử đeo được!"*
* `PhapSuTrangBi.phapSuDuoc`: loại ngọc bội ra, không cho pháp sư (nó cũng là type 4).

Mua thì quay **10% ra bản vĩnh viễn** (dòng 73), **90% ra hạn 1–3 ngày** (dòng 93) — dùng đúng
cơ chế HSD sẵn có, `ItemService.isOutOfDateTime` tự trừ ngày và xoá món.

## 5. NPC Tu Tiên (npc_template 89)

Đứng ở đảo Kamê map 5, **x = 450, y = 288** — đã tính lại tile: nền đặc, cách Bà Mối (x = 380)
đúng 70 px > bán kính 60 px của `Map.getNpc` nên bấm không lộn NPC. Ngoại hình cải trang
**"Goku Thiên Sứ"** (part 2580/2581/2582), avatar 19479.

Menu chỉ có ba nút: **Vào map tu tiên · Shop tu tiên · Đóng**.

### Tiệm dùng ĐÚNG giao diện tiệm của client

`TuTien.moTiem` dựng một `Shop` ngay trong code (không lấy từ bảng `shop` của CSDL) rồi gửi
bằng gói tin tiệm chuẩn — **icon thật, tên, giá, dòng chữ xanh**, y như tiệm Mr. Satan.

Ba tab:

| Tab | id | Hàng |
|---|---|---|
| `Đan / 50 LT` | 90 | 5 viên đan |
| `Ngọc bội / 200 LT` | 91 | 3 viên ngọc bội (hiện sẵn `HP+10000`, `Không thể giao dịch`) |
| `Tụ Linh Phù / 5 thỏi vàng` | 92 | bùa |

**Trả tiền**: gói tin tiệm chỉ có hai ô tiền (vàng / ngọc), không có ô nào cho Linh Thạch. Nên
số tiền hiện ở **cột ngọc**, còn **đơn vị thật ghi ngay trên tên tab**. Khi bấm mua,
`ShopService.buyItem` **chặn lại trước khi lọt sang luồng trừ vàng/ngọc** và gọi `TuTien.mua`
— giống hệt cách tab 30 (phiếu giảm giá) và tab 44 (danh hiệu) đang làm. **Ngọc xanh của người
chơi không bao giờ bị đụng tới.**

Ba chốt an toàn:

* Chỉ mua được món **có thật trong tiệm server vừa gửi** (`shop.getItemShop`), client không
  gửi id tuỳ ý được.
* **Giá đọc từ đối tượng tiệm của server**, không nhận từ client.
* **Nhét vào túi trước, nhét được mới trừ tiền** — không có đường nào mất tiền mà hụt đồ.

Tab id 90–92 là dải riêng, không đụng tab nào sẵn có (10–13, 17, 19, 30, 41–45).

## 6. Ba núm chỉnh trong cpanel — tab "Rơi đồ boss"

| Núm | Mặc định |
|---|---|
| Tu Tiên — tỉ lệ rơi Linh Thạch (%) | 15 |
| Tu Tiên — giây hồi sinh quái | 30 |
| Tu Tiên — bùa Tụ Linh cộng thêm (%) | 50 |
| Tu Tiên — trần sát thương lên quái | 10.000.000 |
| Tu Tiên — sát thương của quái | 50.000 |

Chạy một ngày, nhìn số thật rồi chỉnh, **không phải build lại jar**. (Riêng "sát thương của
quái" chỉ ăn sau khi khởi động lại, vì nó gán một lần lúc dựng map.)

## 7. Kiểm tra đã làm

* Biên dịch sạch **JDK 21**, **698 file class**.
* Dựng CSDL nháp từ dump + toàn bộ patch, chạy patch 83: **8/8 mục kiểm tra = 1**.
* Part NPC 2580/2581/2582: đủ mảnh 3 / 17 / 14, **không thiếu file icon nào** ở cả 4 mức phóng to.
* 10 icon mới: **đủ cả 4 mức** phóng to (x1 sinh bằng cách thu nhỏ x2 khi nguồn thiếu).
* Id icon đã đối chiếu: **cả 10 id đều còn trống** bên mình, không đè lên ảnh nào.
* Mob template 27 (Akkuman) và 44 (Quỷ Đầu Nhọn): có thật, đều type 1 (đi bộ).
* Tile map 5 tại x = 450: nền đặc, đúng chỗ đặt NPC.
* **Gói vật phẩm: 63.431 + 63.485 byte / trần 65.000 — còn chỗ cho ~33 món.**
* Bảng chỉ số vẫn **253 dòng / trần 255** — tính năng này không tốn dòng nào.
* Đã sửa quyền file bản đồ 208 về 644 cho khớp các file cùng thư mục.

## 8. Bốn lỗi đã bắt được trong lúc tự soát

1. **Quái đánh trả mỗi đòn** — Liên Hoàn 330 ms/đòn nên quái xả liên tục. Đã giãn 2,5 giây/lần.
2. **Chết là bị đá về nhà ngay lập tức**, không có 3 giây như thiết kế (vì `lucChetTuTien` khởi
   tạo bằng 0 nên điều kiện đếm giờ đúng ngay lần đầu). Đã sửa thành: lần đầu thấy chết thì ghi
   mốc giờ, đủ 3 giây mới đá.
3. **Bật cờ đen đặt sai chỗ** — trong `goToMap`, chạy trước gói `mapInfo`. Đã dời ra sau.
4. **`chuanBiQuai` gọi `MapService.gI()` ngay trong hàm dựng của `Manager`** — dễ vòng khởi tạo.
   Đã đổi sang duyệt thẳng `Manager.MAPS`.

## 9. Việc người vận hành phải làm

1. Tắt server.
2. `mysqldump -u root -p team2026 map_template item_template npc_template > backup_83.sql`
3. Chạy `SRC/sql/patch/83-map-tu-tien.sql`, xem khối (3) phải toàn `1`.
4. Bật server bản jar mới (**vsMap = 11, vsItem = 31**).
5. Vào cpanel tab **Rơi đồ boss** để chỉnh 5 núm Tu Tiên nếu cần.

## 10. Hai điều đã nói ở tài liệu 69, nhắc lại

* Không chặn cứng theo ngày, nên "500 mỗi ngày" thực chất là **500 cho một buổi cày ~1 giờ 45**.
  Ai cày 5 tiếng sẽ được nhiều hơn.
* **16 con cho cả map là ít khi đông người.** 2 khu chỉ đủ cho ~2–4 người cày thoải mái. Server
  đông thì tăng `map_template.zones` của map 208 lên 10–20 — quái theo khu nên tự nhân lên,
  không phải sửa gì thêm trong code.
