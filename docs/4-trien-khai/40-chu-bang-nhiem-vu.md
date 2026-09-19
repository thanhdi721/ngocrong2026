# 40 — Chữ bảng nhiệm vụ: rút gọn tên bước, mô tả 3 phần, hiện được số lượng

> File SQL: [`02-nhiem-vu-moi.sql`](../../SRC/sql/patch/02-nhiem-vu-moi.sql) (cài mới) ·
> [`05-sua-huong-dan-tan-thu.sql`](../../SRC/sql/patch/05-sua-huong-dan-tan-thu.sql) (NV 0–3) ·
> [`06-cap-nhat-chu-nhiem-vu.sql`](../../SRC/sql/patch/06-cap-nhat-chu-nhiem-vu.sql) (server đang chạy)
>
> **Chỉ đổi chữ.** Không đổi số bước, thứ tự, `max_count`, `npc_id`, `map`, `ducvupro`,
> bảng thưởng, và không sửa dòng Java nào.

## 1. Vấn đề

Ảnh chụp bảng nhiệm vụ NV 4:

```
Thứ bò ra từ vết nứt
- Dọn đường vào Thung lũng tre: hạ 12 khủng lon      <- bị cắt, không thấy (0/12)
```

Client (Unity, không sửa được) nhận `count` và danh sách `maxCount` qua message 40/43 rồi
**tự nối "(x/y)" vào cuối dòng bước hiện tại**. Khung chỉ hiện khoảng **46 ký tự/dòng**
(tính cả "- " đầu dòng). Tên bước cũ dài tới **79 ký tự** (NV 21) nên phần số lượng bị đẩy
ra ngoài khung. Cách sửa duy nhất phía server: **rút ngắn tên bước**.

Sau khi sửa, cùng dòng đó hiển thị:

```
Thứ bò ra từ vết nứt
- Hạ 12 khủng long (3/12)
- Hạ 15 khủng long mẹ
- Về kể cho ông Gôhan
Lũ thú ở Thung lũng tre biến dạng, mắt trắng dã như bị rút hồn.
Dọn bầy khủng long ở Thung lũng tre rồi về kể cho ông Gôhan.
Thưởng: 6.000 SM, 6.000 TN, 2 Gói Capsule
```

## 2. Quy tắc đã áp dụng

### 2.1 Tên bước (`task_sub_template.NAME`)

| # | Quy tắc |
|---|---|
| 1 | **Tối đa 28 ký tự** sau khi thay placeholder bằng phương án **dài nhất** của từng placeholder (ví dụ `%1` → "Làng Kakarot", `%2` → "ông Paragus", `%5` → "Vách núi Kakarot", `%10` → "Trưởng lão Guru"). 28 + " (300/300)" vẫn nằm gọn trong 46 ký tự. |
| 2 | Động từ đứng đầu, nói rõ phải làm gì: "Hạ …", "Nhặt …", "Gặp …", "Tới …", "Dùng …", "Về báo %2". |
| 3 | `max_count` > 1 thì **có đúng con số đó trong tên** để khi client nối "(3/12)" đọc thành "Hạ 12 khủng long (3/12)". Script kiểm tra cả 237 bước. |
| 4 | Không nhồi cốt truyện vào tên bước (bỏ tiền tố "Dọn đường vào …:", "Lần theo tiếng rít:", "Đốt kho tiếp tế:" …). Cốt truyện nằm ở `detail`. |
| 5 | Không dùng `#`; chỉ dùng placeholder đã có (`%1`–`%13`). **Không dùng `%14`**: `%14` cho TĐ ra "phi long mẹ" trong khi quái mẹ ở Rừng xương thật ra là **Thằn lằn mẹ** (mob 10) — lệch dữ liệu. |
| 6 | Tên quái / boss / map / NPC đúng dữ liệu thật của bước (đã đối chiếu `TaskService.checkDoneTaskKillMob`, `Mob.dropItemTask`, `mob_template`, `map_template`, `npc_template`). Ví dụ: NV 4 bước 1 đếm mob 4/5/6 → "Hạ 15 %4 mẹ" = "khủng long mẹ / lợn lòi mẹ / quỷ đất mẹ" khớp tên mob; NV 7 bước 0 đếm mob 10/11/12 (quái mẹ) → "Hạ 10 quái mẹ trong 3 phút" chứ không ghi "Vòi Hư Không" (tên truyện, không phải tên quái người chơi thấy); NV 14 bước 2 đếm Heo rừng / Heo da xanh / Heo Xayda → "Hạ 20 heo chở hàng". |

Bước có đường vòng (điểm quy đổi) ghi rõ đơn vị để con số khớp tiến độ:
"Tích 120 điểm diệt quái" (NV 35), "Tích 160 điểm diệt quái" (NV 43) — câu nhắc giải thích
trong phó bản mỗi quái 2 điểm, ngoài phó bản 1 điểm.

### 2.2 Mô tả (`task_main_template.detail`)

Ba dòng, ngăn bằng xuống dòng như file cũ, **tổng ≤ 200 ký tự** (tính placeholder dài nhất):

1. Câu cốt truyện ngắn, có hồn — ≤ 80 ký tự.
2. Câu mục tiêu tóm tắt — ≤ 70 ký tự.
3. Dòng thưởng gọn: `Thưởng: 6.000 SM, 6.000 TN, 2 Gói Capsule`.

Dòng thưởng được **sinh tự động từ `task_main_reward` dòng `sub_index = -1`** (thưởng hoàn
thành nhiệm vụ; thưởng theo bước không ghi), không gõ tay:

* SM/TN: dưới 1 triệu ghi đủ số (`6.000`), từ 1 triệu ghi `3 triệu`, `80 triệu`, từ 1 tỷ ghi
  `1 tỷ`, `1,1 tỷ`, `1,5 tỷ` — cùng giá trị, chỉ ngắn hơn.
* Vật phẩm theo thứ tự trong cột `items`, tên rút gọn: "Gói 10 viên Capsule" → "Gói Capsule",
  "Gói 30 đậu thần cấp 3" → "Gói 30 đậu cấp 3", "Đá nâng cấp cấp N" → "Đá nâng cấp N",
  2030/2031 → "danh hiệu Người Trả Ký Ức" / "danh hiệu Kẻ Giữ Hư Không".
* NV 3 và NV 10 có thêm dòng thưởng theo hành tinh (gender 0/1/2) → "1 bộ đồ vải cấp 1",
  "1 sách đấm lv1".

Nhờ sinh tự động, mô tả cũ **sai thưởng** đã được sửa theo đúng bảng thưởng, ví dụ NV 46
(mô tả cũ ghi 100 mỗi loại Mảnh + không có đậu; bảng thưởng thật: 5 Đá ngũ sắc, 10 Đá bảo vệ,
60 Đậu thần cấp 8) và NV 47 / NV 50 (mô tả cũ ghi 150 mỗi loại Mảnh; thật: danh hiệu, 20 Đá
bảo vệ, 99 Đậu thần cấp 8, 10 Đá ngũ sắc).

Tên nhiệm vụ (`NAME`) giữ nguyên — cái dài nhất "Thử thách của Thần Hủy Diệt" 27 ký tự.

### 2.3 Câu nhắc (`task_sub_template.notify`)

* Tối đa 100 ký tự, một câu gợi ý hữu ích: đi đâu, gặp ai, quái nào, đường vòng khi không có bang.
* NV 0–3: bước nào bản gốc để trống thì **vẫn để trống** (không chen thông báo vào hướng dẫn tân
  thủ viết cứng trong client). 4 bước có sẵn câu nhắc được viết lại gọn.
* NV 4 trở đi: mọi bước đều có câu nhắc (trước đây phần lớn để trống).

## 3. Kết quả tự kiểm tra (script Python)

| Kiểm tra | Kết quả |
|---|---|
| Tên bước ≤ 28 ký tự (placeholder dài nhất) | **Đạt** — dài nhất **28**: "Qua rắn độc hoặc hạ 200 quái", "Hạ 3 tên Poc, Pic, King Kong", "Khoe cây mới với %2" (→ ông Paragus), "Bái %10 làm thầy" (→ Trưởng lão Guru). Bản cũ dài nhất 79. |
| `max_count` > 1 thì tên có đúng con số | Đạt, 237/237 |
| Không `#`, không placeholder lạ, không `%14` | Đạt |
| `detail` ≤ 200, câu truyện ≤ 80, câu mục tiêu ≤ 70 | Đạt — dài nhất NV 31 = 197 |
| Dòng thưởng khớp `task_main_reward` sub_index −1 (SM, TN, từng vật phẩm, số lượng, thứ tự, dòng theo hành tinh) | Đạt 51/51 |
| `notify` ≤ 100 | Đạt — dài nhất 93 |
| NV 0–3: notify trống giữ trống | Đạt |
| So với trước khi sửa: `task_main_id`, `max_count`, `npc_id`, `map`, `ducvupro`, số bước, id nhiệm vụ | **Y hệt** (02: 237 bước, 05: 13 bước); mọi dòng khác của 02/05 ngoài chữ nhiệm vụ không đổi |
| 05 khớp 02 cho NV 0–3 | Đạt |
| 06 chỉ có UPDATE/SELECT, 51 + 237 câu UPDATE, khớp chữ của 02 | Đạt |

**Import thử** (máy có MySQL 8.4, không có MariaDB; file 06 chỉ dùng `UPDATE`, `CRC32`,
`CHAR_LENGTH`, `SUM` — đều có trong MariaDB 10.4): tạo DB tạm → `database team2026.sql` → 01 →
02 → 05 → 06, **không lỗi**; kiểm tra K1–K5 của 06 đúng kỳ vọng; chạy 06 lần hai cho cùng kết
quả. Chạy thêm 06 trên DB đã nạp **bản 02/05 cũ** (cả bản trong git HEAD lẫn bản chưa commit)
→ K2 `dung_main = 1, dung_sub = 1`. DB tạm đã xóa.

## 4. Cách chạy file 06 (server đang chạy)

1. (Nên) sao lưu:
   `mysqldump -u root -p team2026 task_main_template task_sub_template > backup_truoc_06_$(date +%F).sql`
2. Chạy: `mysql -u root -p team2026 < SRC/sql/patch/06-cap-nhat-chu-nhiem-vu.sql`
   (hoặc dán vào tab SQL của phpMyAdmin).
3. Xem mục KIỂM TRA cuối file:
   * K1: 51 nhiệm vụ, 237 bước.
   * K2: `dung_main = 1`, `dung_sub = 1` (dấu vân tay CRC32 của toàn bộ chữ mới).
   * K3 ≤ 28, K4 ≤ 200 (đo trên chữ còn placeholder), K5 = 0.
4. **Khởi động lại server** — `Manager` chỉ nạp nhiệm vụ lúc khởi động. Không cần build lại jar.

An toàn: chỉ `UPDATE` 4 cột chữ, khớp theo `id` và `task_main_id + ducvupro`; không
DELETE/INSERT, không đụng `data_task` người chơi nên tiến độ giữ nguyên; chạy lại nhiều lần
vẫn cho cùng kết quả. Server cài mới chỉ cần 02 (+ 05) bản đã sửa; chạy thêm 06 cũng không sao.

## 5. Mô tả mẫu (sau khi thay placeholder TĐ)

| NV | Mô tả mới |
|---|---|
| 0 | Ngươi tỉnh dậy bên vách núi, trong ngực le lói ánh sáng lạ. / Về nhà ông Gôhan, lấy rađa trong rương rồi hái đậu thần. / Thưởng: 2.000 SM, 2.000 TN, 5 Đậu thần cấp 1, 1 Gói Capsule |
| 4 | Lũ thú ở Thung lũng tre biến dạng, mắt trắng dã như bị rút hồn. / Dọn bầy khủng long ở Thung lũng tre rồi về kể cho ông Gôhan. / Thưởng: 6.000 SM, 6.000 TN, 2 Gói Capsule |
| 21 | Bản đồ hành quân của Fide nằm trong Doanh trại Độc Nhãn. / Phá doanh trại cùng bang, hoặc một mình hạ 300 quái Trại lính Fide. / Thưởng: 9 triệu SM, 9 triệu TN, 20 Đá nâng cấp 3, 2 Bản đồ kho báu |
| 47 | Ngươi chọn trả hết ký ức cho vũ trụ. / Trả Lõi, mở giới hạn lần cuối, hạ Hư Không Vô Danh. / Thưởng: 1,5 tỷ SM, 1,5 tỷ TN, 1 danh hiệu Người Trả Ký Ức, 20 Đá bảo vệ, 99 Đậu thần cấp 8, 10 Đá ngũ sắc |

("/" = xuống dòng.)

## 6. Bảng đối chiếu tên bước cũ → mới (toàn bộ 237 bước)

"Dài" = số ký tự sau khi thay placeholder bằng phương án dài nhất. Bản cũ lấy từ file 02 trước
lần sửa này.

| NV | ducvupro | Tên cũ | Dài | Tên mới | Dài | max_count |
|---|---|---|---|---|---|---|
| 0 | 1 | Gượng dậy, đi theo mũi tên chỉ dẫn | 34 | Đi theo mũi tên chỉ dẫn | 23 | 1 |
| 0 | 2 | Tìm về nhà %2 ở bên phải | 33 | Về nhà %2 | 18 | 1 |
| 0 | 3 | Gặp %2 đang đứng đợi | 29 | Gặp %2 | 15 | 1 |
| 0 | 4 | Mở rương đồ lấy rađa | 20 | Mở rương đồ lấy rađa | 20 | 1 |
| 0 | 5 | Thu hoạch đậu thần cho ông | 26 | Thu hoạch đậu thần | 18 | 1 |
| 0 | 6 | Quay lại báo cáo với %2 | 32 | Về báo %2 | 18 | 1 |
| 1 | 7 | Đánh ngã 5 mộc nhân ở %1 | 34 | Đánh ngã 5 mộc nhân | 19 | 5 |
| 1 | 8 | Về khoe thành quả với %2 | 33 | Về khoe với %2 | 23 | 1 |
| 2 | 9 | Hạ lũ %4, nhặt 10 đùi gà | 32 | Hạ %4 lấy 10 đùi gà | 27 | 10 |
| 2 | 10 | Mang 10 đùi gà về cho %2 | 33 | Đưa đùi gà cho %2 | 26 | 1 |
| 3 | 11 | Cộng điểm tiềm năng cho mạnh lên | 32 | Cộng điểm tiềm năng | 19 | 1 |
| 3 | 12 | Đi xem vật thể lạ vừa rơi xuống | 31 | Tìm vật thể lạ rơi xuống | 24 | 1 |
| 3 | 13 | Đưa thứ tìm được về cho %2 | 35 | Đưa vật lạ cho %2 | 26 | 1 |
| 4 | 15 | Dọn đường vào %6: hạ 12 %4 | 46 | Hạ 12 %4 | 16 | 12 |
| 4 | 16 | Hạ 15 quái mẹ biến dạng | 23 | Hạ 15 %4 mẹ | 19 | 15 |
| 4 | 17 | Kể lại cho %2 | 22 | Về kể cho %2 | 21 | 1 |
| 5 | 18 | Tìm Kỷ Vật Của Ông trong %13 | 41 | Tìm Kỷ Vật Của Ông | 18 | 1 |
| 5 | 19 | Lau sạch kỷ vật | 15 | Lau sạch Kỷ Vật | 15 | 1 |
| 5 | 20 | Đưa kỷ vật cho %2 | 26 | Đưa kỷ vật cho %2 | 26 | 1 |
| 6 | 21 | Lần theo tiếng rít: hạ 15 %9 | 38 | Hạ 15 %9 | 18 | 15 |
| 6 | 22 | Hạ Kẻ Thu Gom | 13 | Hạ Kẻ Thu Gom | 13 | 1 |
| 6 | 23 | Báo lại cho %2 | 23 | Về báo %2 | 18 | 1 |
| 7 | 24 | Chặt 10 Vòi Hư Không trong 3 phút | 33 | Hạ 10 quái mẹ trong 3 phút | 26 | 10 |
| 7 | 25 | Chạy tới Trạm tàu vũ trụ | 24 | Chạy tới Trạm tàu vũ trụ | 24 | 1 |
| 7 | 26 | Nói chuyện với Jaco | 19 | Gặp Jaco | 8 | 1 |
| 8 | 27 | Gặp Bunma ở Siêu Thị | 20 | Gặp Bunma ở Siêu Thị | 20 | 1 |
| 8 | 28 | Mua 1 Rada cấp 1 ở shop | 23 | Mua 1 Rada cấp 1 | 16 | 1 |
| 8 | 29 | Moi 25 lõi cảm biến từ quái mẹ | 30 | Hạ 25 quái mẹ lấy lõi | 21 | 25 |
| 9 | 30 | Bay tới %11 | 20 | Bay tới %11 | 20 | 1 |
| 9 | 31 | Dọn sạch 20 %12 quanh nhà sư phụ | 41 | Hạ 20 %12 | 18 | 20 |
| 9 | 32 | Nói chuyện với %10 | 30 | Gặp %10 | 19 | 1 |
| 10 | 33 | Xin %10 nhận làm đệ tử | 34 | Bái %10 làm thầy | 28 | 1 |
| 10 | 34 | Học chưởng cấp 1 | 16 | Học chưởng cấp 1 | 16 | 1 |
| 10 | 35 | Đạt 250.000 sức mạnh | 20 | Đạt 250.000 sức mạnh | 20 | 1 |
| 11 | 36 | Thu hoạch 5 hạt đậu thần | 24 | Thu hoạch 5 hạt đậu | 19 | 5 |
| 11 | 37 | Gieo Hạt Giống Hy Vọng | 22 | Gieo Hạt Giống Hy Vọng | 22 | 1 |
| 11 | 38 | Khoe cây mới với %2 | 28 | Khoe cây mới với %2 | 28 | 1 |
| 12 | 39 | Nhận đệ tử đầu tiên | 19 | Nở trứng nhận đệ tử | 19 | 1 |
| 12 | 40 | Cùng đệ tử diệt 25 quái mẹ | 26 | Cùng đệ tử hạ 25 quái mẹ | 24 | 25 |
| 12 | 41 | Giới thiệu đệ tử với %2 | 32 | Dẫn đệ tử gặp %2 | 25 | 1 |
| 13 | 42 | Gia nhập một bang hội | 21 | Gia nhập 1 bang hội | 19 | 1 |
| 13 | 43 | Cùng bạn cùng bang diệt 30 quái mẹ | 34 | Cùng bạn bang hạ 30 quái mẹ | 27 | 30 |
| 13 | 44 | Gặp Giu-ma Đầu Bò | 17 | Gặp Giu-ma Đầu Bò | 17 | 1 |
| 14 | 45 | Nghe Bunma báo tin ở Nhà Bunma | 30 | Gặp Bunma ở Nhà Bunma | 21 | 1 |
| 14 | 46 | Mua một món hàng ở quầy Uron | 28 | Mua 1 món ở quầy Uron | 21 | 1 |
| 14 | 47 | Chặn 20 con thú tải hàng | 24 | Hạ 20 heo chở hàng | 18 | 20 |
| 15 | 48 | Tới điểm hẹn: hạ 20 quái mẹ | 27 | Hạ 20 quái mẹ ở điểm hẹn | 24 | 20 |
| 15 | 49 | Đánh thức Jaco | 14 | Đánh bại Jaco mất ký ức | 23 | 1 |
| 15 | 50 | Gặp lại Jaco ở Trạm tàu vũ trụ | 30 | Gặp Jaco ở Trạm tàu vũ trụ | 26 | 1 |
| 16 | 51 | Lần theo tín hiệu về phía Nam | 29 | Đi về vùng đất phía Nam | 23 | 1 |
| 16 | 52 | Dọn sạch 40 quái chắn đường | 27 | Hạ 40 quái chắn đường | 21 | 40 |
| 16 | 53 | Tiêu diệt 30 quái canh bờ biển | 30 | Hạ 30 quái canh bờ biển | 23 | 30 |
| 16 | 54 | Nhặt 5 Vỏ đạn khắc dấu | 22 | Nhặt 5 Vỏ đạn khắc dấu | 22 | 5 |
| 16 | 55 | Mang vỏ đạn về cho %10 | 34 | Về gặp %10 | 22 | 1 |
| 17 | 56 | Gặp Bà Hạt Mít ở %5 | 33 | Gặp Bà Hạt Mít | 14 | 1 |
| 17 | 57 | Nâng một trang bị lên +2 | 24 | Nâng 1 trang bị lên +2 | 22 | 1 |
| 17 | 58 | Dùng Búa rèn cũ | 15 | Dùng Búa rèn cũ | 15 | 1 |
| 17 | 59 | Khoe vũ khí mới với %10 | 35 | Về khoe với %10 | 27 | 1 |
| 18 | 60 | Tới Thành phố Vegeta | 20 | Tới Thành phố Vegeta | 20 | 1 |
| 18 | 61 | Nói chuyện với người lạ thổi nhạc | 33 | Gặp người lạ thổi nhạc | 22 | 1 |
| 18 | 62 | Diệt 30 quái đang vây thành | 27 | Hạ 30 quái vây thành | 20 | 30 |
| 18 | 63 | Đi cùng Tapion tới Thành phố Santa | 34 | Tới Thành phố Santa | 19 | 1 |
| 18 | 64 | Nghe Tapion kể chuyện | 21 | Nghe Tapion kể chuyện | 21 | 1 |
| 19 | 65 | Gặp Cui ở Thung lũng Nappa | 26 | Gặp Cui ở Thung lũng Nappa | 26 | 1 |
| 19 | 66 | Hạ 60 Nappa mất trí | 19 | Hạ 60 Nappa mất trí | 19 | 60 |
| 19 | 67 | Hạ 40 Soldier gác kho | 21 | Hạ 40 Soldier gác kho | 21 | 40 |
| 19 | 68 | Cùng người khác hạ 30 Appule | 28 | Cùng bạn hạ 30 Appule | 21 | 30 |
| 19 | 69 | Báo cáo với Cui | 15 | Báo cáo với Cui | 15 | 1 |
| 20 | 70 | Tìm kẻ lạ ở Khu hang động | 25 | Gặp Berry ở Khu hang động | 25 | 1 |
| 20 | 71 | Chọn cách xử lý: Granola hay Jaco | 33 | Chọn: Granola hay Jaco | 22 | 1 |
| 20 | 72 | Bắt tay với Granola | 19 | Bắt tay với Granola | 19 | 1 |
| 20 | 73 | Thanh toán 3 tay chân của Fide | 30 | Hạ 3 tay chân của Fide | 22 | 3 |
| 20 | 74 | Nhặt 3 Thẻ tiền thưởng | 22 | Nhặt 3 Thẻ tiền thưởng | 22 | 3 |
| 20 | 75 | Nhận tiền thưởng từ Granola | 27 | Nhận thưởng từ Granola | 22 | 1 |
| 21 | 76 | Gặp Lính canh ở Rừng Bamboo | 27 | Gặp Lính canh ở Rừng Bamboo | 27 | 1 |
| 21 | 77 | Phá Doanh trại Độc Nhãn cùng bang, hoặc diệt 300 quái cụm Trại lính Fide | 72 | Phá trại hoặc hạ 300 quái | 25 | 300 |
| 21 | 78 | Lấy bản đồ hành quân từ Độc Nhãn, hoặc từ Lính canh ở Rừng Bamboo | 65 | Lấy bản đồ hành quân | 20 | 1 |
| 21 | 79 | Mang bản đồ về cho %10 | 34 | Về gặp %10 | 22 | 1 |
| 22 | 80 | Hỏi Tapion về máy đo lạ | 23 | Hỏi Tapion về máy đo lạ | 23 | 1 |
| 22 | 81 | Hạ 40 lính khỉ canh đường | 25 | Hạ 40 lính khỉ canh đường | 25 | 40 |
| 22 | 82 | Hạ trọn Tiểu đội sát thủ | 24 | Hạ 5 tên Tiểu đội sát thủ | 25 | 5 |
| 22 | 83 | Nhặt Máy đo ký ức | 17 | Nhặt Máy đo ký ức | 17 | 1 |
| 22 | 84 | Đưa máy đo cho Tapion | 21 | Đưa máy đo cho Tapion | 21 | 1 |
| 23 | 85 | Đạt 80.000.000 sức mạnh | 23 | Đạt 80.000.000 sức mạnh | 23 | 1 |
| 23 | 86 | Tới Núi khỉ vàng | 16 | Tới Núi khỉ vàng | 16 | 1 |
| 23 | 87 | Đốt kho tiếp tế: 25 Khỉ lông vàng trong 5 phút | 46 | Hạ 25 Khỉ lông vàng, 5 phút | 27 | 25 |
| 23 | 88 | Hạ Fide đại ca — hai dạng đầu | 29 | Hạ 2 dạng đầu của Fide | 22 | 2 |
| 23 | 89 | Hạ Fide đại ca — dạng cuối | 26 | Hạ Fide dạng cuối | 17 | 1 |
| 23 | 90 | Tuyên bố liên minh với %10 | 38 | Về gặp %10 | 22 | 1 |
| 24 | 91 | Nghe Bunma nói về sóng cơ khí | 29 | Gặp Bunma ở Nhà Bunma | 21 | 1 |
| 24 | 92 | Tới Thành phố phía đông | 23 | Tới Thành phố phía đông | 23 | 1 |
| 24 | 93 | Diệt 50 Xên con cấp 1-2 | 23 | Hạ 50 Xên con cấp 1-2 | 21 | 50 |
| 24 | 94 | Diệt 40 Xên con cấp 3-4 | 23 | Hạ 40 Xên con cấp 3-4 | 21 | 40 |
| 24 | 95 | Báo lại cho Bunma | 17 | Báo lại cho Bunma | 17 | 1 |
| 25 | 96 | Tới Cao nguyên tìm hai bác sĩ | 29 | Tới Cao nguyên | 14 | 1 |
| 25 | 97 | Hạ Android 19 rồi Dr.Kôrê | 25 | Hạ 2 boss: Android 19, Kôrê | 27 | 2 |
| 25 | 98 | Nhặt 3 Lõi năng lượng Android | 29 | Nhặt 3 Lõi năng lượng | 21 | 3 |
| 25 | 99 | Đưa lõi cho Bunma mổ xẻ | 23 | Đưa lõi cho Bunma | 17 | 1 |
| 26 | 100 | Gặp Bà Hạt Mít ở Đảo Kamê | 25 | Gặp Bà Hạt Mít ở Đảo Kamê | 25 | 1 |
| 26 | 101 | Pha lê hóa một trang bị | 23 | Pha lê hóa 1 trang bị | 21 | 1 |
| 26 | 102 | Ép 1 Sao pha lê vào trang bị đó | 31 | Ép 1 Sao pha lê vào đồ | 22 | 1 |
| 26 | 103 | Dùng Mẫu kim loại có ký ức | 26 | Dùng Mẫu kim loại có ký ức | 26 | 1 |
| 26 | 104 | Kể lại cho %10 những gì ngươi nghe được | 51 | Về kể cho %10 | 25 | 1 |
| 27 | 105 | Hỏi Ca Lích về container lạ | 27 | Hỏi Ca Lích về container | 24 | 1 |
| 27 | 106 | Tới sân sau siêu thị | 20 | Tới Sân sau siêu thị | 20 | 1 |
| 27 | 107 | Hạ ba cỗ máy mẫu | 16 | Hạ 3 Android 13, 14, 15 | 23 | 3 |
| 27 | 108 | Báo cáo với Ca Lích | 19 | Báo cáo với Ca Lích | 19 | 1 |
| 28 | 109 | Tới Thành phố phía bắc | 22 | Tới Thành phố phía bắc | 22 | 1 |
| 28 | 110 | Dọn 60 Xên con cấp 5-7 quanh cửa hầm | 36 | Hạ 60 Xên con cấp 5-7 | 21 | 60 |
| 28 | 111 | Hạ Poc, Pic rồi King Kong | 25 | Hạ 3 tên Poc, Pic, King Kong | 28 | 3 |
| 28 | 112 | Nhặt Mảnh giáp có khắc tên | 26 | Nhặt Mảnh giáp khắc tên | 23 | 1 |
| 28 | 113 | Đưa mảnh giáp cho Bunma | 23 | Đưa mảnh giáp cho Bunma | 23 | 1 |
| 29 | 114 | Lấy thẻ từ giả của Bunma | 24 | Lấy thẻ từ giả của Bunma | 24 | 1 |
| 29 | 115 | Đột nhập Phòng thí nghiệm Myuu | 30 | Vào Phòng thí nghiệm Myuu | 25 | 1 |
| 29 | 116 | Giật 5 Bản thiết kế trong 6 phút | 32 | Nhặt 5 Bản thiết kế, 6 phút | 27 | 5 |
| 29 | 117 | Đối mặt Dr. Myuu | 16 | Đối mặt Dr. Myuu | 16 | 1 |
| 30 | 118 | Tới Thị trấn Ginder | 19 | Tới Thị trấn Ginder | 19 | 1 |
| 30 | 119 | Diệt 50 Xên con cấp 8 | 21 | Hạ 50 Xên con cấp 8 | 19 | 50 |
| 30 | 120 | Hạ Xên bọ hung — hai dạng đầu | 29 | Hạ 2 dạng đầu Xên bọ hung | 25 | 2 |
| 30 | 121 | Hạ Xên hoàn thiện | 17 | Hạ Xên hoàn thiện | 17 | 1 |
| 30 | 122 | Báo với Bunma về mẫu 07 | 23 | Báo cho Bunma về mẫu 07 | 23 | 1 |
| 31 | 123 | Nghe Potage nói sự thật | 23 | Nghe Potage kể sự thật | 22 | 1 |
| 31 | 124 | Quyết định số phận bản sao | 26 | Chọn số phận bản sao | 20 | 1 |
| 31 | 125 | Tới Võ đài Xên bọ hung | 22 | Tới Võ đài Xên bọ hung | 22 | 1 |
| 31 | 126 | Gọi một nhân chứng vào võ đài | 29 | Rủ 1 người vào võ đài | 21 | 1 |
| 31 | 127 | Tiêu diệt Bản sao của ngươi | 27 | Hạ bản sao của ngươi | 20 | 1 |
| 31 | 128 | Đạt 2.000.000.000 sức mạnh | 26 | Đạt 2 tỷ sức mạnh | 17 | 1 |
| 32 | 129 | Gặp Bunma ở Nhà Bunma | 21 | Gặp Bunma ở Nhà Bunma | 21 | 1 |
| 32 | 130 | Dùng Nhẫn thời không sai lệch | 29 | Dùng Nhẫn thời không | 20 | 1 |
| 32 | 131 | Nói chuyện với Bardock | 22 | Gặp Bardock | 11 | 1 |
| 32 | 132 | Dọn sạch hang động nguyên thủy | 30 | Hạ 40 Cabira hoặc Tobi | 22 | 40 |
| 32 | 133 | Nhặt 3 Mảnh Ký Ức Vỡ | 20 | Nhặt 3 Mảnh Ký Ức Vỡ | 20 | 3 |
| 32 | 134 | Báo cáo với Bardock | 19 | Báo cáo với Bardock | 19 | 1 |
| 33 | 135 | Tới vách núi của hành tinh bạn | 30 | Tới %5 | 20 | 1 |
| 33 | 136 | Nói chuyện với Quốc Vương | 25 | Gặp Quốc Vương | 14 | 1 |
| 33 | 137 | Nâng HP gốc chạm trần 220.000 | 29 | Nâng HP gốc lên 220.000 | 23 | 1 |
| 33 | 138 | Mở giới hạn sức mạnh | 20 | Mở giới hạn sức mạnh | 20 | 1 |
| 33 | 139 | Đạt 3 tỷ sức mạnh | 17 | Đạt 3 tỷ sức mạnh | 17 | 1 |
| 33 | 140 | Báo cáo với Quốc Vương | 22 | Báo cáo với Quốc Vương | 22 | 1 |
| 34 | 141 | Tới Cánh đồng tuyết | 19 | Tới Cánh đồng tuyết | 19 | 1 |
| 34 | 142 | Diệt bọn canh băng | 18 | Hạ 50 Tai tím hoặc Abo | 22 | 50 |
| 34 | 143 | Hạ 20 Kado trong 5 phút | 23 | Hạ 20 Kado trong 5 phút | 23 | 20 |
| 34 | 144 | Nhặt Mảnh Ký Ức Đóng Băng | 25 | Nhặt Mảnh Ký Ức Đóng Băng | 25 | 1 |
| 34 | 145 | Hạ Cooler cả hai dạng | 21 | Hạ Cooler cả 2 dạng | 19 | 2 |
| 34 | 146 | Báo cáo với Bardock | 19 | Báo cáo với Bardock | 19 | 1 |
| 35 | 147 | Nói chuyện với Thần Vũ Trụ | 26 | Gặp Thần Vũ Trụ | 15 | 1 |
| 35 | 148 | Vào Con đường rắn độc cùng bạn bang, hoặc sát cánh 1 người khác ở Hang quỷ chim | 79 | Rủ 1 người đi cùng | 18 | 1 |
| 35 | 149 | Dọn đường qua ba chặng, hoặc diệt 120 Dơi da xanh / Quỷ chim ngoài phó bản | 74 | Tích 120 điểm diệt quái | 23 | 120 |
| 35 | 150 | Hoàn thành Con đường rắn độc, hoặc diệt 200 Dơi da xanh / Quỷ chim | 66 | Qua rắn độc hoặc hạ 200 quái | 28 | 200 |
| 35 | 151 | Gặp Thượng Đế ở Thần điện | 25 | Gặp Thượng Đế ở Thần điện | 25 | 1 |
| 36 | 152 | Gặp Ôsin ở Đại hội võ thuật | 27 | Gặp Ôsin ở Đại hội võ thuật | 27 | 1 |
| 36 | 153 | Vào Cổng phi thuyền | 19 | Vào Cổng phi thuyền | 19 | 1 |
| 36 | 154 | Hạ Drabura hoặc 20 Cadic M | 26 | Hạ Drabura hoặc 20 Cadic M | 26 | 20 |
| 36 | 155 | Xuống tới Cửa Ải 1 | 18 | Xuống Cửa Ải 1 | 14 | 1 |
| 36 | 156 | Nói chuyện với Babiđây | 22 | Nói chuyện với Babiđây | 22 | 1 |
| 37 | 157 | Gặp Ôsin ở Đại hội võ thuật | 27 | Gặp Ôsin ở Đại hội võ thuật | 27 | 1 |
| 37 | 158 | Hạ Mabư | 7 | Hạ Mabư | 7 | 1 |
| 37 | 159 | Hạ Drabura 3 hoặc 30 Quỷ chim | 29 | Hạ Drabura 3 / 30 Quỷ chim | 26 | 30 |
| 37 | 160 | Nhặt Lõi Phép Babiđây | 21 | Nhặt Lõi Phép Babiđây | 21 | 1 |
| 37 | 161 | Mang Lõi Phép cho Kibit | 23 | Mang Lõi Phép cho Kibit | 23 | 1 |
| 38 | 162 | Gặp Bunma ở Tương lai | 21 | Gặp Bunma ở Nhà Bunma | 21 | 1 |
| 38 | 163 | Dọn sạch bọn Xên con phía bắc | 29 | Hạ 60 Xên con phía bắc | 22 | 60 |
| 38 | 164 | Hạ Black Goku | 13 | Hạ Black Goku | 13 | 1 |
| 38 | 165 | Hạ Super Black Goku | 19 | Hạ Super Black Goku | 19 | 1 |
| 38 | 166 | Nhặt Nhẫn thời không sai lệch | 29 | Nhặt Nhẫn thời không | 20 | 1 |
| 38 | 167 | Báo cáo với Bunma | 17 | Báo cáo với Bunma | 17 | 1 |
| 39 | 168 | Nói chuyện với Bardock | 22 | Gặp Bardock | 11 | 1 |
| 39 | 169 | Gom đủ 7 viên Ngọc Rồng | 23 | Gom đủ 7 viên Ngọc Rồng | 23 | 7 |
| 39 | 170 | Gọi Rồng Thần và ước | 20 | Gọi Rồng Thần và ước | 20 | 1 |
| 39 | 171 | Hỏi Rồng Omega về sao đen | 25 | Hỏi Rồng Omega về sao đen | 25 | 1 |
| 39 | 172 | Diệt bầy khỉ ở Núi khỉ vàng | 27 | Hạ 40 Khỉ lông vàng | 19 | 40 |
| 39 | 173 | Gặp Bardock ở Làng Kakarot | 26 | Gặp Bardock ở Làng Kakarot | 26 | 1 |
| 39 | 174 | Hạ Baby cả ba dạng | 18 | Hạ Baby cả 3 dạng | 17 | 3 |
| 40 | 175 | Nói chuyện với Thần Vũ Trụ | 26 | Gặp Thần Vũ Trụ | 15 | 1 |
| 40 | 176 | Lên Thánh địa Kaio | 18 | Lên Thánh địa Kaio | 18 | 1 |
| 40 | 177 | Nói chuyện với Tổ Sư Kaio | 25 | Gặp Tổ Sư Kaio | 14 | 1 |
| 40 | 178 | Đưa Mảnh Ký Ức cho Tổ Sư Kaio | 29 | Dùng Mảnh Ký Ức 1 | 17 | 1 |
| 40 | 179 | Nghe Kibit kể phần còn lại | 26 | Nghe Kibit kể tiếp | 18 | 1 |
| 41 | 180 | Nhận lời dặn của Tổ Sư Kaio | 27 | Nghe Tổ Sư Kaio dặn | 19 | 1 |
| 41 | 181 | Dọn dẹp vành đai rừng | 21 | Hạ 60 quái vành đai rừng | 24 | 60 |
| 41 | 182 | Hạ Broly | 8 | Hạ Broly | 8 | 1 |
| 41 | 183 | Hạ Super Broly | 14 | Hạ Super Broly | 14 | 1 |
| 41 | 184 | Báo lại với Tổ Sư Kaio | 22 | Báo lại với Tổ Sư Kaio | 22 | 1 |
| 42 | 185 | Hỏi Ôsin đường tới ngục tù | 26 | Hỏi Ôsin đường tới ngục tù | 26 | 1 |
| 42 | 186 | Tới Hành tinh ngục tù | 21 | Tới Hành tinh ngục tù | 21 | 1 |
| 42 | 187 | Phá 60 lồng giam trong 10 phút | 30 | Phá 60 lồng giam, 10 phút | 25 | 60 |
| 42 | 188 | Hạ Cumber | 9 | Hạ Cumber | 9 | 1 |
| 42 | 189 | Hạ Super Cumber | 15 | Hạ Super Cumber | 15 | 1 |
| 42 | 190 | Báo cáo với Ôsin | 16 | Báo cáo với Ôsin | 16 | 1 |
| 43 | 191 | Gặp Mr Popo ở Làng Aru | 22 | Gặp Mr Popo ở Làng Aru | 22 | 1 |
| 43 | 192 | Dọn sạch khí gas, hoặc diệt 160 quái ở Hành tinh ngục tù / thực vật | 67 | Tích 160 điểm diệt quái | 23 | 160 |
| 43 | 193 | Hạ Dr Lychee, hoặc diệt 50 quái ở map 155/160/161 | 49 | Hạ Dr Lychee hoặc 50 quái | 25 | 50 |
| 43 | 194 | Hạ Hatchiyack, hoặc diệt 70 quái ở map 155/160/161 | 50 | Hạ Hatchiyack hoặc 70 quái | 26 | 70 |
| 43 | 195 | Hoàn thành Khí gas hủy diệt, hoặc diệt 100 quái ở map 155/160/161 | 65 | Xong khí gas hoặc 100 quái | 26 | 100 |
| 43 | 196 | Báo cáo với Thượng Đế | 21 | Báo cáo với Thượng Đế | 21 | 1 |
| 44 | 197 | Nhờ Ôsin đưa tới hành tinh Bill | 31 | Nhờ Ôsin tới hành tinh Bill | 27 | 1 |
| 44 | 198 | Nói chuyện với Bill | 19 | Nói chuyện với Bill | 19 | 1 |
| 44 | 199 | Thắng một trận đấu | 18 | Thắng 1 trận đấu | 16 | 1 |
| 44 | 200 | Thách đấu Whis | 14 | Đánh bại Whis | 13 | 1 |
| 44 | 201 | Mở giới hạn sức mạnh lần hai | 28 | Mở giới hạn lần hai | 19 | 1 |
| 44 | 202 | Nghe Whis dặn dò | 16 | Nghe Whis dặn dò | 16 | 1 |
| 45 | 203 | Tới Lãnh địa Fize | 17 | Tới Lãnh địa Fize | 17 | 1 |
| 45 | 204 | Nhặt Mảnh Ký Ức thứ bảy | 23 | Nhặt Mảnh Ký Ức thứ bảy | 23 | 1 |
| 45 | 205 | Làm lễ hợp nhất cùng một người khác | 35 | Rủ 1 người làm lễ hợp nhất | 26 | 1 |
| 45 | 206 | Hợp nhất bảy mảnh | 17 | Hợp nhất 7 mảnh | 15 | 1 |
| 45 | 207 | Nghe Thiên Sứ Whis giải thích | 29 | Nghe Thiên Sứ Whis | 18 | 1 |
| 46 | 208 | Nói chuyện với Dr. Myuu | 23 | Gặp Dr. Myuu | 12 | 1 |
| 46 | 209 | Hạ Heart | 8 | Hạ Heart | 8 | 1 |
| 46 | 210 | Đuổi theo Heart tới Võ Đài Siêu Cấp | 35 | Đuổi tới Võ Đài Siêu Cấp | 24 | 1 |
| 46 | 211 | Hạ Heart Hư Không | 17 | Hạ Heart Hư Không | 17 | 1 |
| 46 | 212 | Hạ Heart Toàn Ký | 16 | Hạ Heart Toàn Ký | 16 | 1 |
| 46 | 213 | Nói chuyện với Thiên Sứ Whis | 28 | Gặp Thiên Sứ Whis | 17 | 1 |
| 47 | 214 | Quyết định số phận của Lõi | 26 | Chọn số phận của Lõi | 20 | 1 |
| 47 | 215 | Trả Lõi Hư Không | 16 | Trả Lõi Hư Không | 16 | 1 |
| 47 | 216 | Lên Thánh địa Kaio | 18 | Lên Thánh địa Kaio | 18 | 1 |
| 47 | 217 | Mở giới hạn sức mạnh lần cuối | 29 | Mở giới hạn lần cuối | 20 | 1 |
| 47 | 218 | Nghe Tổ Sư Kaio nói lời cuối | 28 | Nghe lời cuối Tổ Sư Kaio | 24 | 1 |
| 47 | 219 | Hạ Hư Không Vô Danh | 19 | Hạ Hư Không Vô Danh | 19 | 1 |
| 48 | 220 | Tìm kẻ lạ ở Khu hang động | 25 | Gặp Berry ở Khu hang động | 25 | 1 |
| 48 | 221 | Chọn cách xử lý: Granola hay Jaco | 33 | Chọn: Granola hay Jaco | 22 | 1 |
| 48 | 222 | Trình báo cảnh sát vũ trụ Jaco | 30 | Gặp Jaco ở Trạm tàu vũ trụ | 26 | 1 |
| 48 | 223 | Thi hành lệnh truy nã 3 mục tiêu | 32 | Hạ 3 tên bị truy nã | 19 | 3 |
| 48 | 224 | Nhặt 3 Biên bản truy nã | 23 | Nhặt 3 Biên bản truy nã | 23 | 3 |
| 48 | 225 | Nộp biên bản cho Jaco | 21 | Nộp biên bản cho Jaco | 21 | 1 |
| 49 | 226 | Nghe Potage nói sự thật | 23 | Nghe Potage kể sự thật | 22 | 1 |
| 49 | 227 | Quyết định số phận bản sao | 26 | Chọn số phận bản sao | 20 | 1 |
| 49 | 228 | Tới Võ đài Xên bọ hung | 22 | Tới Võ đài Xên bọ hung | 22 | 1 |
| 49 | 229 | Gọi một nhân chứng vào võ đài | 29 | Rủ 1 người vào võ đài | 21 | 1 |
| 49 | 230 | Đánh gục Bản sao của ngươi | 26 | Đánh gục bản sao | 16 | 1 |
| 49 | 231 | Thu nhận nó bằng Bình chứa Commeson | 35 | Dùng Bình chứa Commeson | 23 | 1 |
| 49 | 232 | Đạt 2.000.000.000 sức mạnh | 26 | Đạt 2 tỷ sức mạnh | 17 | 1 |
| 50 | 233 | Quyết định số phận của Lõi | 26 | Chọn số phận của Lõi | 20 | 1 |
| 50 | 234 | Hấp thụ Lõi Hư Không | 20 | Hấp thụ Lõi Hư Không | 20 | 1 |
| 50 | 235 | Về Hành tinh ngục tù | 20 | Về Hành tinh ngục tù | 20 | 1 |
| 50 | 236 | Mở giới hạn sức mạnh lần cuối | 29 | Mở giới hạn lần cuối | 20 | 1 |
| 50 | 237 | Nghe Ôsin nói lời cuối | 22 | Nghe lời cuối của Ôsin | 22 | 1 |
| 50 | 238 | Hạ Hư Không Vô Danh | 19 | Hạ Hư Không Vô Danh | 19 | 1 |

## 7. Không thay đổi

* Không sửa file Java nào; `transformName` giữ nguyên bảng placeholder (tài liệu 12 §7.4).
* Không đổi `task_main_reward`, `data_task`, cơ chế bước, id nhiệm vụ.
* Chú thích cuối mỗi dòng bước NV 4+ trong 02 (`-- TASK_x_y = … A1 <tên>`) được cập nhật theo tên mới;
  chú thích NV 0–3 ("GỐC …") giữ nguyên.
