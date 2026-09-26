# 67 — Còi Triệu Hồi Lão Dê

Ngày: 2026-09-26 · Patch SQL: `SRC/sql/patch/79-coi-trieu-hoi-lao-de.sql`
Nối tiếp [66-nhan-chi-ton-va-boss-sieu-than-god.md](66-nhan-chi-ton-va-boss-sieu-than-god.md)

---

## 1. Vòng lặp

```
đánh boss Siêu Thần God  ──10%──►  Còi Triệu Hồi Lão Dê
        │                                   │ thổi
        └──5%──► Gậy Thông Thiên            ▼
                      ▲            boss Lão Dê Hồi Xuân
                      └──5%───────────────┘  (bảng rơi Y HỆT Siêu Thần God)
```

Gậy Thông Thiên dùng cho "thông đít" → leo bảng vàng của Bà Mối (tài liệu 66).

---

## 2. Vật phẩm 2265 — Còi Triệu Hồi Lão Dê

| | |
|---|---|
| id | 2265 (ô liền sau 2264) |
| type | 27, `is_up_to_up = 1` (gộp chồng) |
| icon | **12316** — dùng lại ảnh cái chuông vàng đã có sẵn, **không thêm file ảnh nào** |
| mô tả | *"Thổi lên là cụ tới. Cụ không vui đâu"* |

Thổi bằng nút **Sử dụng** trong hành trang (`UseItem`, `case 2265`).

**Thổi hụt thì KHÔNG mất còi** — còi chỉ bị trừ sau khi lão nhận lời. Các trường hợp hụt:

* map phó bản / Ma Bư / doanh trại / bản đồ kho báu / chiến trường ngọc đen / map 140 / map 111
  → *"Lão không vào chỗ này đâu, ra map thường mà thổi"*
* khu này đã có một ông lão đang đứng → *"Lão đang ở ngay đây rồi, thổi nữa lão giận"*
* cả 5 bản đang bận → *"Cả 5 ông lão đang bận hết rồi, lát nữa thổi lại"*

---

## 3. Boss Lão Dê Hồi Xuân

`SRC/src/nro/models/boss/lao_de/BossLaoDe.java` · `BossID.LAO_DE_HOI_XUAN = -2214`

* **Ngoại hình**: cải trang "Quy Lão Hồi Xuân" (item 2142, part 2424 / 2425 / 2426, mang từ SUMO
  ở patch 67), hào quang **109 Hồng Vân**. Đã kiểm: đủ mảnh 3 / 17 / 14, không thiếu file icon nào
  ở cả 4 mức phóng to.
* **Máu 500 triệu, sát thương 50.000**, bộ chiêu y như bộ Siêu Thần God — cùng bậc thì cùng
  bảng rơi đồ.
* **Không bao giờ tự ra map**: `rest()` để rỗng, chỉ `trieuHoi()` mới gọi được lão.
* **Ra đúng chỗ người thổi**: `joinMap()` được ghi đè, đặt lão cách người thổi ±40 px trong
  cùng khu (kẹp trong lòng map). `leaveMap()` xoá `zoneFinal`, nếu không lần sau lão lại mò
  về đúng khu cũ.
* **Dựng sẵn 5 bản** lúc khởi động (`BossManager.loadBoss`) — tức tối đa 5 khu có lão cùng lúc.
* `trieuHoi()` là **`synchronized`**: hàm này chạy trên luồng người chơi, hai người thổi cùng
  lúc mà không khoá thì cả hai cùng nhìn thấy một ông lão đang nghỉ, cùng đặt chỗ hẹn, rồi một
  người mất còi mà chẳng gọi được ai.
* **Vẫn loa toàn server** khi ra map (máu 500 triệu vượt ngưỡng `THONG_BAO_MAU_MIN`), nên cả
  server biết đường mà chạy tới đánh ké.

### Trò riêng: nhìn trộm

Cứ **10 giây** lão nhìn trộm một người bất kỳ trong khu → người đó **choáng 2 giây**
(`EffectSkillService.startStun`, đúng cơ chế boss CADICH đang dùng) và nhận thông báo
*"Lão Dê vừa nhìn bạn một cái. Bạn đứng hình 2 giây."* Lão thì buông một câu trong 5 câu
có sẵn, kiểu *"Đứng yên cho lão ngắm cái nào"*.

---

## 4. Bảng rơi đồ dùng chung

`SRC/src/nro/models/boss/sieu_than_god/BangRoi.java`

Gom bảng rơi của **cả ba con** (Vegeta, Goku, Lão Dê) về một chỗ, để chúng **chắc chắn** cùng
tỉ lệ — sửa một chỗ là sửa cả ba, không còn cảnh chép qua chép lại rồi lệch nhau.

**Lượt chính — tổng đúng 100 %** (giữ nguyên như patch 78, không đổi một chữ):

| Tỉ lệ | Rơi |
|---|---|
| 40 % | Ngọc Rồng 3 / 4 / 5 sao |
| 40 % | Ngọc Rồng bí ngô 1–7 sao |
| 10 % | Cuồng nộ 2 / Bổ khí 2 / Bổ huyết 2 / Giáp Xên 2 |
| 5 % | Gậy Thông Thiên |
| 5 % | 5 Đá Pháp Sư |

**Lượt phụ — 10 %**: thêm một cái Còi Triệu Hồi Lão Dê.

> Lượt phụ quay **độc lập**, không đụng tới bảng 100 % ở trên: hạ boss vẫn luôn rơi đúng một
> món của bảng chính, chỉ là thỉnh thoảng có thêm cái còi rơi kèm. Đây là chỗ **duy nhất** hai
> boss Siêu Thần God đổi so với patch 78 — cần chỗ nào đó nhả ra còi thì vòng lặp mới chạy được.
> Không muốn thì sửa `TI_LE_COI` về 0 và cho còi ra chỗ khác.

Cả ba con đều có chốt chặn: id vượt cỡ `ITEM_TEMPLATES` (chưa chạy patch) thì bỏ rơi và ghi
log, thay vì văng `IndexOutOfBounds` giữa luồng boss.

---

## 5. Kiểm tra đã làm

* Biên dịch sạch **JDK 21**, 689 file class.
* Dựng CSDL nháp từ dump + toàn bộ patch (bỏ `36-go-vat-pham-ngol.sql` — file lùi lại),
  patch 79 chạy sạch, 5/5 mục kiểm tra `= 1`.
* Part 2424 / 2425 / 2426: đủ mảnh, **không thiếu file icon nào** ở cả 4 mức phóng to.
* Icon 12316 có đủ x1…x4.
* Gói dữ liệu vật phẩm sau khi thêm 2265: **63.049 + 63.136 byte** trên trần 65.000.
  **Còn chỗ cho khoảng 41 vật phẩm nữa.**

## 6. Việc người vận hành phải làm

1. Tắt server.
2. `mysqldump -u root -p team2026 item_template > backup_79.sql`
3. Chạy `SRC/sql/patch/79-coi-trieu-hoi-lao-de.sql`, xem khối (3) phải toàn `1`.
4. Bật server bản jar mới (`vsItem = 28`).
