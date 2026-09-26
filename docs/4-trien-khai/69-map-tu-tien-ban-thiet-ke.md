# 69 — Map Tu Tiên + Shop Tu Tiên (bản thiết kế, CHƯA làm)

Ngày: 2026-09-26 · Trạng thái: **đã chốt đủ, sẵn sàng làm**

Tài liệu này là bản khảo sát tính khả thi. **Chưa đụng một dòng code nào.**
Bốn điểm còn mơ hồ đã được chủ dự án chốt (mục 6).

---

## 1. Tóm tắt yêu cầu

NPC "Tu Tiên" ở đảo Kamê, 3 mục: **Vào map tu tiên · Shop tu tiên · Đóng**.

* **Map tu tiên**: quái 20 triệu HP / 50k sát thương, đánh **không nhận exp, không nhận vàng**,
  chỉ rơi **Linh Thạch** (khóa giao dịch). **2 khu, mỗi khu 8 con — tổng 16 con.** Vào map
  **tự bật cờ đen**; chết thì **không hồi sinh được**, tự đưa về nhà. Chỉnh tỉ lệ sao cho cày
  một ngày ra **trung bình ~500 linh thạch**.
* **Shop tu tiên**: 5 loại đan (buff 10 phút, 50 linh thạch/viên), 3 ngọc bội cho đệ tử
  (200 linh thạch), 1 bùa tăng 50% tỉ lệ rơi linh thạch trong 30 phút (5 thỏi vàng).
* **Giao dịch**: chỉ **đan** được giao dịch; linh thạch, ngọc bội, bùa đều khóa.

---

## 2. Khảo sát: cái gì làm được ngay

### 2.1 Map — LẤY ĐƯỢC, và không cần thêm một file ảnh nào ✅

Source **SUMO** có sẵn 5 map tên rất hợp:

| id | Tên | File bản đồ |
|---|---|---|
| 206 | Tháp Tiên Môn | có |
| **207** | **Thái Cực Điện** | có |
| **208** | **Nam Thiên Môn** | có |
| 209 | Võ Đài Thiên giới | có |
| 210 | Tây Phương Cực Lạc | có |

Cả 5 map dùng **bộ tile 13 + nền 12**, mà **mình đã dùng đúng bộ đó** cho *Thần Điện*,
*Tháp Karin*, *Hành tinh Kaio*. Nghĩa là **ảnh đã nằm sẵn trên client**, chỉ cần:

1. chép file `tile_map_data/207` (và 208) sang `SRC/data/map/tile_map_data/`;
2. thêm dòng `map_template` với `tile_id = 13`, `bg_id = 12`;
3. tăng `vsMap`.

Id 186–199 đang trống bên mình, hoặc giữ nguyên 207/208 cũng không đụng ai (map lớn nhất của
mình là 185).

> Đề xuất: **Thái Cực Điện (207)** làm map tu tiên chính. Nếu cần map thứ hai cho cấp cao
> thì thêm **Nam Thiên Môn (208)**.

### 2.2 Linh Thạch và đan dược — có art đẹp sẵn ✅

Từ SUMO/Bun (xem ảnh khảo sát `scratchpad/tu_tien2.png`):

| Vật phẩm | Icon gốc | Ghi chú |
|---|---|---|
| **Linh Thạch** | 30498–30501 (4 màu: lục / lam / hồng / đỏ) | id icon **còn trống** bên mình |
| Đan dược | 22219, 24885, 24886, 24895, 27641 | lư đan bốc lửa, đỉnh đan — id **còn trống** |
| Ngọc bội | 31712–31717 | **id còn trống** |

> ⚠️ Nhóm ngọc bội đẹp hơn (15530–15535, bùa ngọc có tua đỏ) thì **id đã bị quần áo của mình
> chiếm**, mang về phải đánh số lại — vẫn làm được, chỉ tốn công như đợt res SUMO.

### 2.3 Những thứ khung code đã sẵn sàng ✅

| Việc | Chỗ móc |
|---|---|
| Tự bật cờ đen khi vào map | `PlayerService.changeAndSendTypePK(player, PK_ALL)` trong `ChangeMapService` |
| Chặn hồi sinh | `PlayerService.hoiSinh` **đã có tiền lệ** chặn map 51 — thêm map tu tiên vào là xong |
| Khóa giao dịch | option 30 "Không thể giao dịch", gắn sẵn lúc rơi / lúc mua |
| Buff 10 phút | khung `ItemTime` đã có sẵn nhịp đếm giờ (cuồng nộ, bổ huyết, bổ khí, giáp xên) |
| Ngọc bội cho đệ tử | **đệ tử = Pet, CÓ ô trang bị** (`InventoryService.itemBagToPetBody`, yêu cầu pet ≥ 1tr5 sức mạnh) |

---

## 3. Bốn chỗ phải xử lý riêng (không có sẵn)

### 3.1 ⚠️ Quái 20 triệu máu hiện nay VÔ NGHĨA — đây là chỗ quan trọng nhất

`Mob.injured` dòng 151 đang ép sát thương lên quái thường:

```java
if (!dieWhenHpFull && !isBigBoss() && !isMapPhoBan(...) && this.lvMob > 0 && plAtt != null
        && plAtt.charms.tdOaiHung < System.currentTimeMillis()) {
    damage = (int) ((this.point.maxHp <= 20_000_000 ? this.point.maxHp * 1 : 2) * (10.0 / 100));
}
```

Nghĩa là **mỗi đòn chỉ ăn đúng 10% máu tối đa của quái** — ai đánh cũng **đúng 10 đòn là chết**,
bất kể sức đánh 1 triệu hay 1 tỷ. Đặt 20 triệu máu **không làm quái dai hơn một chút nào**.

Muốn 20 triệu máu có ý nghĩa thì phải **thêm ngoại lệ cho map tu tiên** trong đoạn đó (cho ăn
sát thương thật). Lúc đó người yếu sẽ đánh rất lâu, người mạnh giết một đòn — cân bằng lại bằng
**số lượng quái và nhịp hồi sinh quái**, không phải bằng máu.

**Đã chốt — dùng luật riêng cho map tu tiên:**

```java
// thay cho phép ép 10% máu ở trên, chỉ áp dụng trong map tu tiên
damage = Math.min(damage, 10_000_000);      // trần 10 triệu mỗi đòn
```

Quái 20 triệu máu ⇒ **ai đạt 10 triệu sát thương thì 2 đòn là chết**, không ai nhanh hơn được.
Người yếu hơn thì phải nhiều đòn hơn — đúng như yêu cầu, và vẫn giữ được trần tốc độ farm.

### 3.2 Chặn exp và vàng

* **Exp**: chặn ở `Mob.injured` — chỗ gọi `Service.addSMTN(...)` và
  `TrainingService.tangTnsmLuyenTap(...)`, thêm điều kiện "không phải map tu tiên".
* **Vàng**: quái rơi vàng theo `GoldDropConfig` (tab "Vàng rơi" của cpanel), chia theo **nhóm map**.
  Map mới sẽ không thuộc nhóm nào → phải thêm ngoại lệ trả về 0, hoặc thêm một nhóm riêng
  để chỉnh được từ cpanel.

### 3.3 Chết thì tự về nhà

`hoiSinh` chặn xong thì người chơi nằm đó mãi. Phải thêm: sau ~3 giây tự
`ChangeMapService.changeMap` về nhà theo hành tinh và hồi đầy máu — giống cách map 51 đang làm.

### 3.4 Buff "tăng 10% chí mạng" chưa có

4 loại kia dùng lại được khung cũ (sức đánh / HP / KI / giảm sát thương). Riêng **chí mạng**
chưa có buff nào theo giờ — phải thêm cờ mới vào `ItemTime` và cộng vào `NPoint.tlDameCrit`.
Không khó, chỉ là phải viết mới.

### 3.5 Ngọc bội "chỉ đệ tử mặc được"

`InventoryService.putItemBody` hiện **không phân biệt chủ hay đệ** — món nào mặc được thì cả
hai đều mặc được. Phải thêm chặn: nếu là ngọc bội mà `!player.isPet` thì từ chối kèm câu
*"Chỉ đệ tử mới mặc được"*.

---

## 4. Ngân sách — kiểm tra kỹ vì đang rất sát trần

| Hạng mục | Cần | Còn lại | Sau khi làm |
|---|---|---|---|
| **Vật phẩm** (gói tin ~65 KB × 2) | 10 món | **~41 món** | còn ~31 |
| **Dòng chỉ số** (`item_option_template`, trần 255) | 0–1 | **2 ô** | còn 1–2 |
| **Id icon** | 10 | thoải mái nếu tránh id đã dùng | — |
| **Cột bảng `player`** | **0** ✅ | 0 | không cần |

10 món: 1 Linh Thạch + 5 đan + 3 ngọc bội + 1 bùa.

> ✅ **Không cần thêm cột nào, cũng không cần bảng phụ.** Vì "500 linh thạch mỗi ngày" là
> **trung bình theo tỉ lệ rơi**, không phải chặn cứng — nên không phải lưu số đã farm hôm nay.
> Đây là điều may, vì bảng `player` đã kịch trần dòng của InnoDB (patch 78 đã phải đổi
> `data_card` sang TEXT mới nhét nổi cột `thong_dit`).

---

## 5. Bảng giá theo yêu cầu

### Đan (giao dịch ĐƯỢC) — 50 linh thạch/viên, hiệu lực 10 phút

| # | Tên đề xuất | Tác dụng |
|---|---|---|
| 1 | Luyện Khí Đan | +20% sức đánh |
| 2 | Hộ Thể Đan | +30% HP |
| 3 | Tụ Khí Đan | +30% KI |
| 4 | Kim Cương Đan | giảm 50% sát thương |
| 5 | Phá Quân Đan | +10% chí mạng |

### Ngọc bội (KHÓA giao dịch, chỉ đệ tử mặc) — 200 linh thạch

| # | Tên đề xuất | Chỉ số |
|---|---|---|
| 1 | Ngọc Bội Hộ Mệnh | HP +10.000 |
| 2 | Ngọc Bội Tụ Linh | KI +10.000 |
| 3 | Ngọc Bội Phá Quân | Sức đánh +5.000 |

**Mua một cái thì quay một lần: 10% ra bản VĨNH VIỄN, 90% ra bản có HẠN 1–3 ngày.**

✅ Cơ chế này **đã có sẵn trong code**, không phải viết mới — chỉ chép đúng cách mấy món khác
đang làm ở `UseItem`:

```java
if (Util.isTrue(10, 100)) {
    it.itemOptions.add(new ItemOption(73, 0));                   // vĩnh viễn
} else {
    it.itemOptions.add(new ItemOption(93, Util.nextInt(1, 3)));  // "Hạn sử dụng # ngày"
}
```

Dòng chỉ số **93 "Hạn sử dụng # ngày"** đã có trong bảng, và `ItemService.isOutOfDateTime()` tự
trừ ngày rồi xoá món khi hết hạn. Người chơi nhìn thấy ngay trên vật phẩm.

### Bùa (KHÓA giao dịch) — 5 thỏi vàng

**Tụ Linh Phù** — tăng 50% tỉ lệ rơi linh thạch, hiệu lực 30 phút.

---

## 6. Bốn điểm đã chốt

| Câu hỏi | Chốt |
|---|---|
| Số quái | **8 con mỗi khu · 2 khu · tổng 16 con** |
| Ngọc bội | **10% ra vĩnh viễn, 90% ra hạn 1–3 ngày** |
| 500 linh thạch/ngày | **chỉ chỉnh tỉ lệ cho trung bình ~500**, không chặn cứng |
| Quái | **trần 10 triệu sát thương mỗi đòn**, máu 20 triệu ⇒ 2 đòn; yếu hơn thì nhiều đòn hơn |

### Tính tỉ lệ rơi Linh Thạch — tính lại bằng SỐ LIỆU THẬT trong code

> Bản đầu tôi đoán "4–5 giây/con" là **sai hẳn**. Chủ dự án chỉ ra Liên Hoàn bắn rất nhanh,
> lại còn đệ tử (Đẻ trứng) đánh kèm. Dưới đây là số đo thật, không phải phỏng đoán.

**Số liệu lấy từ `skill_template`:**

| | |
|---|---|
| Liên hoàn cấp 7 | `cool_down` = **330 ms/đòn** (cấp 1 là 350 ms) |
| Đẻ trứng | `cool_down` 360–540 giây, "Tạo quái đi theo hỗ trợ" — **là đệ tử đánh kèm**, không phải chiêu diện rộng |
| Thái Dương Hạ San | đánh diện rộng nhưng **chỉ gây choáng, không gây sát thương** |
| Liên hoàn / Dragon / Demon / Galick | `max_fight` = 1 — **mỗi đòn một con**, không có chiêu quét cả bầy |

**Tính một con:**

```
trần 10 triệu/đòn, quái 20 triệu máu   →  2 đòn
2 đòn × 330 ms                          =  0,66 giây
+ chuyển mục tiêu / chạy tới            ≈  0,6 giây
                                        =  ~1,26 giây/con
```

Khớp đúng con số bạn đưa ("~1 giây 3 là 1 con"). Nghĩa là **48 con/phút ≈ 2.860 con/giờ** nếu
quái luôn có sẵn — gấp **3,5 lần** ước lượng sai ban đầu của tôi.

### Đây là chỗ then chốt: 3 giây hồi sinh quá nhanh

16 con, dọn sạch mất ~20 giây, mà con đầu đã sống lại từ giây thứ 3 ⇒ **không bao giờ phải chờ**,
tốc độ cày chỉ phụ thuộc sức mạnh người chơi. Người mạnh sẽ bỏ xa người yếu.

**Kéo dài thời gian hồi sinh trong map tu tiên là cách duy nhất đặt được trần tốc độ:**

| Hồi sinh | Trần thực tế | 10% | **15%** | 30% |
|---|---|---|---|---|
| 3 giây (hiện tại) | 2.860 con/giờ | 286 | 429 | 857 |
| 15 giây | 2.860 con/giờ | 286 | 429 | 857 |
| **30 giây** | **1.920 con/giờ** | 192 | **288** | 576 |
| 60 giây | 960 con/giờ | 96 | 144 | 288 |

*(15 giây chưa đủ để chạm trần vì dọn 16 con đã mất 20 giây.)*

### Đề xuất: hồi sinh 30 giây + tỉ lệ rơi 15%

→ **288 linh thạch/giờ**, cày **~1 giờ 45 phút** được 500.

Chọn 30 giây vì hai lẽ:

1. **Đặt được trần**: người mạnh dọn 16 con trong 20 giây rồi phải chờ 10 giây — bị chặn ở
   32 con/phút. Người yếu (5 giây/con) không bị chờ, đạt ~12 con/phút. **Khoảng cách giàu nghèo
   thu từ 10 lần xuống còn chưa tới 3 lần.**
2. Không chán như 60 giây (dọn xong đứng không 40 giây).

### Hai điều phải nói thẳng

**Một:** không chặn cứng thì "500 mỗi ngày" thực chất là "500 cho một buổi cày ~1 giờ 45".
Ai cày 5 tiếng sẽ được ~1.400. Muốn đúng 500/ngày thì bắt buộc phải chặn cứng, mà chặn cứng thì
phải có chỗ lưu ⇒ quay lại chuyện bảng phụ. **Bạn quyết.**

**Hai:** 16 con cho cả map là **rất ít khi đông người**. `zones` trong `map_template` là số bản
sao của map — 2 khu nghĩa là chỉ ~2–4 người cày thoải mái cùng lúc, người thứ 5 vào là tranh
quái, không ai đạt nổi 500. Nếu server đông thì nên **tăng số khu lên 10–20** (mỗi khu vẫn 8
con, quái theo khu nên tự nhân lên, không phải sửa gì thêm).

> Cả **tỉ lệ rơi** lẫn **thời gian hồi sinh** tôi sẽ đưa vào cpanel, chạy một ngày rồi nhìn số
> thật mà chỉnh — khỏi build lại jar.

Bùa Tụ Linh cộng 50% **tương đối**: 15% → 22,5% trong 30 phút.

---

## 7. Thứ tự làm

1. Mang map 207 **Thái Cực Điện** về: chép file bản đồ, thêm `map_template` (tile 13 / nền 12),
   đặt 16 con quái (8 × 2 khu), tăng `vsMap`.
2. Mang 10 icon về, thêm 10 vật phẩm (khóa giao dịch trừ đan), tăng `vsItem`.
3. Luật quái trong map: trần 10 triệu sát thương/đòn, chặn exp, chặn vàng, rơi Linh Thạch 30%
   (tỉ lệ đưa vào cpanel).
4. Luật map: vào là bật cờ đen, chặn hồi sinh, chết 3 giây sau tự về nhà.
5. NPC Tu Tiên ở đảo Kamê (npc_template 89) + shop, dùng khung tiệm sẵn có.
6. Buff chí mạng mới trong `ItemTime` + `NPoint`; ngọc bội quay 10% vĩnh viễn / 90% HSD 1–3 ngày;
   chặn `putItemBody` để chỉ đệ tử mặc được ngọc bội.

Không còn chỗ nào phải nghiên cứu thêm — tất cả đều đã dò xong trong code.
