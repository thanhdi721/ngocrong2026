# 62 — Hai boss Fu ở Nam Kamê (2026-09-25)

Nguồn đá cho chức năng [Pháp sư trang bị](61-phap-su-trang-bi.md).

## Luật

| | |
|---|---|
| Map | **29 Nam Kamê** (20 khu) |
| Máu | **1.000.000.000** mỗi con |
| Sát thương | **200.000** |
| Nhịp ra | **15 phút một lượt**, tính từ lúc con trước rời map |
| Mỗi lượt | **đúng MỘT con**, bốc ngẫu nhiên 50/50 — không bao giờ có hai con cùng lúc |
| Rơi | **5 Đá Pháp Sư** + **1 Đá Tẩy Pháp Sư** |
| Chiêu | y như Cumber: Thái Dương Hạ San, Tái tạo năng lượng, Khiên năng lượng, Kamejoko, Galick |
| Cách đánh | cũng chép từ Cumber: xa > 450 thì bay tới, 100–450 thì lượn qua lượn lại, dưới 100 thì tung chiêu |

## Hai con

| Tên | Hình | Hào quang |
|---|---|---|
| **Fu Thời Không** | cải trang "Fu" (item 2164, part 2490/2491/2492) | 104 Khí Tử Điện |
| **Fu Hợp Thể** | "Cải trang Hợp Thể" (item 639, part 627/628/629) | 103 Khí Huyết Đỏ |

**Lưu ý về cải trang**: server mình **không có** bộ nào tên "Fu hợp thể". Con thứ hai đang mượn
"Cải trang Hợp Thể" (tóc đỏ, đồ đen đỏ) cho hợp cặp. Muốn đổi sang bộ khác thì sửa đúng một
dòng `outfit` của `BossesData.FU_HOP_THE`.

## Cách "mỗi lượt một con" hoạt động

`BossFu.rest()` thay cho nhịp nghỉ mặc định của từng con:

1. Còn con nào ngoài map thì thôi (kể cả lúc đang diễn cảnh chết).
2. Chưa đủ 15 phút kể từ `lanKetThuc` thì thôi.
3. Bốc một con cho lượt này (`sapRa`), chỉ con được bốc mới chuyển sang `RESPAWN`.

`leaveMap()` và `die()` đều ghi lại `lanKetThuc` và xoá `sapRa`, nên đồng hồ 15 phút luôn chạy
từ lúc sân trống.

## File

| | |
|---|---|
| `SRC/src/nro/models/boss/fu/BossFu.java` | mới — điều phối + cách đánh + rơi đồ |
| `SRC/src/nro/models/boss/BossesData.java` | thêm `FU` và `FU_HOP_THE` |
| `SRC/src/nro/models/boss/BossID.java` | `FU = -2210`, `FU_HOP_THE = -2211` |
| `SRC/src/nro/models/boss/Boss_Manager/BossManager.java` | gọi `BossFu.taoCaHai()` lúc nạp boss |

Không cần SQL và không phải tăng version — hình, hào quang, hai viên đá đều đã có sẵn
(patch 67, 74, 75).

## Chưa kiểm tra được

Chưa vào game. Cần xem: đúng 15 phút ra một con, không bao giờ thấy hai con cùng lúc, hào quang
hiện đúng, và hạ xong nhặt đủ 5 + 1 viên đá.
