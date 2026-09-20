# 48 — Cpanel: 3 tab mới (Boss, Vàng rơi, Top vàng/ngọc) — 2026-09-20

## Tab "Boss" (`cpanel/BossTab.java`)

Bảng toàn bộ boss trong bộ nhớ (`BossManager.getBosses()`), tự làm mới 3 giây:
id, tên, trạng thái, hình dạng (`currentLevel`), map + khu, HP hiện tại / tối đa.
Lọc theo tên boss, số map hoặc id; lọc nhanh "chỉ boss đang ở map".

Thao tác:
- **Hồi sinh ngay**: `currentLevel = -1` rồi `changeStatus(RESPAWN)` — boss ra map theo đúng vòng đời sẵn có.
- **Ép biến mất**: `leaveMap()` + `changeStatus(REST)` — boss nghỉ rồi tự hồi sinh theo `secondsRest` của nó.
- **Hồi sinh toàn bộ boss của map N**: duyệt `data[0].getMapJoin()`.
- **Ép biến mất toàn bộ boss đang lọc**.

Thao tác chạy trên luồng cpanel, giống cách lệnh admin trong game vẫn làm từ luồng mạng.

## Tab "Vàng rơi" (`cpanel/GoldDropTab.java` + `mob/GoldDropConfig.java`)

Tỉ lệ vàng rơi từ quái tách khỏi `Mob.dropItemMap` sang `GoldDropConfig`, sửa được lúc
server đang chạy. 5 nhóm map, mỗi nhóm có `rate/per` và `min..max`:

| Nhóm | Mặc định (đúng như code cũ) | Ước tính vàng/giờ (1.500 quái) |
|---|---|---|
| 3 hành tinh | 1/20 × 500–3.000 | ~131 nghìn |
| Nappa / Fide | 1/100 × 2.000–6.000 | ~60 nghìn |
| Map Băng 105–110 | 30/100 × 150.000–250.000 | ~90 triệu |
| Map Tương Lai | 15/100 × 80.000–150.000 | ~26 triệu |
| Phó bản | 1/100 × 80.000–200.000 | ~2,1 triệu |

Lưu vào `data/golddrop.properties`, nạp lúc khởi động trong `ServerManager` (thiếu file thì
dùng mặc định trên). Vàng boss rơi (20.000–30.000/con, trong các lớp `boss/**`) không thuộc bảng này.

## Tab "Top vàng/ngọc" (`cpanel/TopTab.java`)

Chỉ đọc dữ liệu từ database.

- Xếp hạng theo: Vàng, Ngọc, Hồng ngọc, Thỏi vàng (quét `items_bag` + `items_box`, id 457 và 1535),
  Sức mạnh (`data_point[1]`), VNĐ (`account.vnd`).
- **Kiểm tra số dư bất thường**: vàng < 0 hoặc > 100 tỷ, ngọc / hồng ngọc < 0 hoặc > 1 tỷ,
  thỏi vàng > 1 triệu. Ngưỡng nằm ở các hằng `MAX_*` đầu file.
- **Tổng vàng / ngọc toàn server**: để theo dõi lạm phát theo thời gian.

Số liệu lấy từ database nên người chơi đang online có thể chênh tới lần lưu kế tiếp
(tab Server có nút "Lưu toàn bộ dữ liệu").

## Chỉnh số boss (`boss/BossTuning.java` + `cpanel/BossTuneDialog.java`)

Nút "Chỉnh máu / sát thương..." (hoặc bấm đúp một dòng) trong tab Boss mở bảng theo
từng hình dạng: **Máu**, **Sát thương**, **% chặn sát thương** (0–`BossDamageReduce.MAX_PERCENT`),
**Nghỉ (giây)**.

- Sát thương chiêu của boss lấy từ `nPoint.dameg` = `BossData.dame`, nên hạ ô "Sát thương"
  là hạ luôn sát thương mọi chiêu.
- Sửa thẳng `BossData` (dùng chung cho mọi bản sao cùng boss) + cập nhật boss đang ở map:
  `nPoint.hpg/dameg` đổi theo, máu hiện tại giữ đúng tỉ lệ cũ.
- `% chặn sát thương` ghi vào `Boss.damageReducePercentByLevel` qua setter mới.
- Lưu ở `data/bosstuning.properties`, khoá `<id boss>.<hình dạng>.<hp|dame|reduce|rest>`;
  nạp trong `ServerManager` ngay sau `BossManager.loadBoss()`. Mục nào không có trong file
  thì giữ số gốc trong mã nguồn. Nút "Bỏ chỉnh số của boss này" xoá khoá tương ứng.
- Nút "Chỉnh hàng loạt theo %": nhân máu / sát thương của toàn bộ boss đang lọc
  (100 = giữ nguyên, 50 = một nửa), tự lưu vào file.
