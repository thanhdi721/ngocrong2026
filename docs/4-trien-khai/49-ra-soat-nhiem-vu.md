# 49 — Rà soát toàn tuyến nhiệm vụ (2026-09-20)

## Cách rà
1. Đối chiếu 237 bước trong `02-nhiem-vu-moi.sql` (cộng các patch sau) với mã nguồn:
   mỗi bước phải có ít nhất một chỗ gọi `TASK_<nv>_<bước>`. **Kết quả: 0 bước thiếu code.**
2. Phân loại điều kiện từng bước: hành động (đánh quái, hạ boss, nói chuyện, nhặt đồ,
   dùng đồ, vào map) hay **trạng thái** (HP gốc, sức mạnh, bậc giới hạn, đang ở bang,
   đủ 7 ngọc). Nhóm "trạng thái" là nhóm hay kẹt.

## Lỗi nhóm "điều kiện đã đạt sẵn" (đã sửa)

Bước chỉ được xét khi có sự kiện tương ứng. Ai đạt điều kiện **trước** khi bước đó thành
bước hiện tại thì không còn sự kiện nào để kích hoạt → kẹt vĩnh viễn.

Ví dụ thật: HP gốc 220.220 trước khi tới bước "Nâng HP gốc lên 220.000" → bước đứng mãi 0/1.

Sửa: thêm `TaskService.checkPassiveTaskConditions(player)`, gọi định kỳ trong `Player.update`
cùng chỗ với `checkDoneTaskTogetherInZone`. Bao các bước:

| Bước | Điều kiện trạng thái |
|---|---|
| `TASK_33_1` | HP gốc ≥ trần của bậc giới hạn |
| `TASK_10_2`, `TASK_23_0`, `TASK_31_5`, `TASK_49_5`, `TASK_33_3` | mốc sức mạnh |
| `TASK_13_0` | đang ở trong bang |
| `TASK_39_1` | đủ 7 viên Ngọc Rồng trong hành trang |
| `TASK_33_2`, `TASK_44_4`, `TASK_47_3`, `TASK_50_3` | đã mở giới hạn tới bậc tương ứng |

Bước "tới map X" không cần thêm gì: `checkDoneTaskGoToMap` được gọi cả khi di chuyển,
nên đứng sẵn ở map rồi đi một bước là xong.

## Còn lại: bước cần NGƯỜI CHƠI KHÁC (chưa sửa, cần chủ dự án quyết)

| Bước | Yêu cầu |
|---|---|
| NV 13 b1 | cùng bạn bang hạ 30 quái mẹ |
| NV 31 b3 / NV 49 b3 | 1 người chơi khác cùng khu ở Võ đài Xên bọ hung |
| NV 35 b1 | 1 người cùng bang trong phó bản, hoặc 1 người chơi khác ở cụm map thay thế |
| NV 44 b2 | thắng 1 trận đấu (thách đấu / võ đài / đại hội) |
| NV 45 b2 | 1 người chơi khác ở Lãnh địa Fize |

Server ít người online là kẹt. Đề xuất: mở lối solo (đứng đủ 2 phút ở đúng map, hoặc hạ N quái)
giống lối solo đã mở cho các bước phó bản bang hội.
