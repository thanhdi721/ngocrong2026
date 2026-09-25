# 63 — Boss ra map mà không ai hay (2026-09-25)

## Lỗi

`Boss.canSendNotify()` cũ chỉ cần lớp con bật cờ `isNotifyDisabled` là **câm vĩnh viễn**.
Đếm lại thì có **hơn 20 lớp boss** bật cờ đó, trong đó nhiều con rất đáng báo:

* **Lốp Trưởng** (2 tỉ máu) và cả bộ 8 con — tính năng mới nhất mà ra map không ai biết;
* **Heart** 1,5 tỉ máu, cả nhóm boss nhiệm vụ 1,9–3,7 triệu máu (Cumber NV, Black Goku NV,
  Cooler NV, Baby NV, Mabư NV) — `QuestBoss` bật cờ với lý do "tránh kéo đám đông tới farm",
  nhưng chính chủ dự án đã chốt (18/09) là mấy con này **ai vào map cũng đánh được**;
* **Kuku, Rambo, Mập Đầu Đinh** (0,5–1,5 triệu máu, mỗi loại 5 bản);
* boss sự kiện Sơn Tinh / Thủy Tinh / Nguyệt Thần / Nhật Thần / Khỉ Đột.

Ngoài ra **43 lớp boss tự viết lại `joinMap()`** (chép thân hàm gốc rồi sửa) và trong bản chép
đó **thiếu hẳn dòng gọi `notifyJoinMap()`** — Ăn Trộm, Ô Đô, Mặt Trời, Virut, Sói Héc Quyn,
Broly, Dr. Lychee, Hatchiyack, Yardart, Ma Vương Pícôlô.

## Sửa

Bỏ cách "tắt theo từng lớp", đổi sang **xét theo MÁU** — một ngưỡng duy nhất, chỉnh trong
cpanel tab "Rơi đồ boss":

```
Thông báo boss — máu tối thiểu = 1.000.000   (BossDropConfig.THONG_BAO_MAU_MIN)
```

`canSendNotify()` nay trả về true khi: boss **không phải lâu la** của boss khác
(`parentBoss == null`), **không** ở map 140 / 111 / phó bản / Ma Bư / chiến trường ngọc đen,
và **máu tối đa ≥ ngưỡng**.

Với ngưỡng mặc định 1 triệu:

| Báo | Im |
|---|---|
| Lốp Trưởng (2 tỉ), Heart (1,5 tỉ), Fu (1 tỉ) | Ăn Trộm (100 máu), Virut (100), Mặt Trời (100) |
| Rambo (1,5 tr), Mập Đầu Đinh (1 tr) | Kuku (0,5 tr), Sói Héc Quyn (10 k), Ô Đô (25 k) |
| Boss nhiệm vụ 1,9–3,7 tr, Rồng Nhí (50 tr) | Kẻ Thu Gom (80 k), Jaco Vô Thức |

Muốn ồn hơn thì hạ ngưỡng (10.000 là báo gần như mọi con), muốn yên thì nâng lên 10 triệu —
sửa trong cpanel, ăn ngay, không cần khởi động lại.

Đồng thời thêm `notifyJoinMap()` vào 10 lớp tự viết `joinMap()` nói trên, để chúng cũng đi
qua đúng một cái ngưỡng đó thay vì im mãi.

**Nữ Thần Băng Tinh** có 1 tỉ máu nhưng chỉ là đạo cụ của màn Lốp Trưởng nên chặn riêng
(ghi đè `notifyJoinMap()` rỗng), tránh mỗi lượt hiện ba dòng thông báo.

## File

| | |
|---|---|
| `boss/Boss.java` | viết lại `canSendNotify()` |
| `boss/BossDropConfig.java` | thêm ô "Thông báo boss — máu tối thiểu" |
| `boss/Boss_mini/*`, `Broly/Broly`, `khi_gas/*`, `yardrat/Yardart`, `ma_vuong_picolo/Pocolo` | thêm `notifyJoinMap()` |
| `boss/lop_truong/NuThanBangTinh.java` | chặn thông báo riêng |

Không cần SQL, không đổi version dữ liệu.

## Chưa kiểm tra được

Chưa vào game. Cần xem lượng thông báo có vừa mắt không — nếu dày quá thì nâng ngưỡng trong
cpanel chứ không phải sửa code.
