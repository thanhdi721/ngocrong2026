# 65 — Chỉnh phần thưởng thành tích / điểm danh / nhiệm vụ hàng ngày (2026-09-25)

## 1. Nhiệm vụ thành tích — tổng đúng 30.000 ngọc

`sql/patch/77-thuong-thanh-tich-va-diem-danh.sql` chia lại cột `achievement_template.money`
cho 20 mốc, tổng **30.000**, mốc khó ăn nhiều hơn (cao nhất 5.000 cho "Tuyệt kỹ thành thạo",
thấp nhất 500 cho "Gia nhập Vệ Binh"). Trước đó tổng là 60.335.

Hai điều đáng ghi:

* Cột `money` **vốn đã trả NGỌC** (`AchievementService.confirmAchievement`:
  `player.inventory.gem += money`). Chữ "Thỏi Vàng" trên màn hình do **client tự vẽ**,
  server không đổi được.
* Mốc cũ 50.000 **hiện sai số** vì `openAchievementUI` gửi cột này bằng `writeShort`
  (trần 32.767). Sau khi chia lại, mốc lớn nhất 5.000 nên hết lỗi.

## 2. Điểm danh — 2.000 ngọc + 10 thỏi vàng khóa

`npc_list/BoMong.java`, hai hằng số `NGOC_DIEM_DANH` và `THOI_VANG_DIEM_DANH` ngay đầu lớp.
Trước là 10.000 ngọc + 100 thỏi vàng. Thỏi vàng vẫn gắn option 30 "Không thể giao dịch".

Thêm một chốt: **hết ô hành trang thì báo và không ghi ngày điểm danh** — bản cũ vẫn ghi ngày
nên người chơi mất luôn phần thưởng hôm đó.

## 3. Nhiệm vụ hàng ngày — vàng theo độ khó

`consts/ConstTask.java`:

| Độ khó | Vàng cũ | Vàng mới | Vật phẩm (giữ nguyên) |
|---|---|---|---|
| Dễ | 5.000 | **5.000.000** | Bí ngô 7 sao |
| Bình thường | 20.000 | **10.000.000** | Bí ngô 6 sao |
| Khó | 50.000 | **15.000.000** | Bí ngô 5 sao |
| Rất khó | 200.000 | **20.000.000** | Bí ngô 4 sao |
| Địa ngục | **25** | **25.000.000** | Bí ngô 3 sao (còn dưới 15 lượt thì ra Cây thông) |

Cấp Địa ngục cũ ghi **25 vàng** — ít hơn cả cấp Dễ, gần như chắc chắn là gõ thiếu số.

Mỗi ngày tối đa **10 lượt** (`ConstTask.MAX_SIDE_TASK`), nên trần vàng từ nhiệm vụ ngày là
**250 triệu/ngày** nếu làm toàn cấp Địa ngục. Trần vàng mang trên người vẫn là 200 tỉ.

Phần thưởng vật phẩm vẫn là **bí ngô Halloween** — đồ sự kiện cũ, nếu muốn đổi sang Đá Pháp Sư
hay thứ khác thì sửa biến `ngocBi` trong `TaskService.paySideTask`.

## Chưa kiểm tra được

Chưa vào game: cần xem bảng thành tích hiện đúng số mới, điểm danh nhận đúng 2.000 ngọc +
10 thỏi vàng khóa, và nộp nhiệm vụ ngày ra đúng mức vàng.
