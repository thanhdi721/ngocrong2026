# 47 — Rà đường đi các bước nhiệm vụ (2026-09-19)

Cách rà: với mỗi bước "gặp NPC / tới map" của NV 0–50 (237 bước trong `02-nhiem-vu-moi.sql`),
kiểm (1) NPC có đứng trên map đó không (map_template gốc + patch 04), (2) map có cổng đi bộ
hoặc NPC dịch chuyển tới được **ở đúng mốc nhiệm vụ đó** (khoá trong `ChangeMapService.checkMapCanJoin`).

Kết quả: mọi NPC đều đứng đúng map. Có 4 chỗ không có đường vào:

| # | Bước | Map | Vì sao kẹt | Cách sửa |
|---|---|---|---|---|
| 1 | NV 14 b0 Gặp Bunma | 102 Nhà Bunma | Chỉ tới bằng máy thời gian Ca Lích, mở từ NV 24 | Đổi sang Bunma (NPC 7) ở Siêu Thị 84 — `TaskService` case `BUNMA`; patch 11 |
| 2 | NV 20 b0, NV 48 b0 | 160 Khu hang động | Chỉ vào bằng Nhẫn thời không (có từ NV 32) | Cui ở Thung lũng Nappa 68 có nút "Khu hang động" (từ NV 20); Berry có nút "Về Thung lũng Nappa" |
| 3 | NV 45 b0 | 78 Lãnh địa Fize | Không có cổng vào | Whis ở Hành tinh Bill 154 có nút "Lãnh địa Fize" (từ NV 45) |
| 4 | NV 46 b2, NV 47 b5 | 145 Võ Đài Siêu Cấp | Không có cổng vào | Dr. Myuu (166) có nút "Tới Võ Đài Siêu Cấp" (từ NV 46 b2); Thiên Sứ Whis ở 78 đưa tới (từ NV 46), ở 145 có nút "Về Lãnh địa Fize" |

Sửa kèm: Thiên Sứ Whis trước đây xong bước nhiệm vụ vẫn mở đè menu lên câu "Việc tiếp theo".

Các tuyến dịch chuyển khác đã kiểm, dùng được: Tapion 19↔126 (đóng 23h–1h), Jaco 24→139 Potaufeu,
Thượng Đế 45→48, Thần Vũ Trụ 48→50, Ôsin 52→114/165, 50→154/155, Ca Lích →102 (từ NV 24), cổng 97→166.

## Bỏ các bước dính khung giờ (patch 12)

Thành phố Santa (126) chỉ vào được qua Tapion theo giờ; phi thuyền Mabư cũng theo giờ.

| NV | Trước | Sau |
|---|---|---|
| 18 | b3 "Tới Thành phố Santa", b4 nghe Tapion ở 126 | **Xoá b3**; "Nghe Tapion kể chuyện" (nay là `TASK_18_3`) ở Thành phố Vegeta 19. Bỏ trigger vào map 126 |
| 22 | b0, b4 gặp Tapion ở 126 | Gặp Tapion ở 19 (126 vẫn tính) |
| 36 | Đường vòng Sa mạc cần Bình hút năng lượng 1795 (patch 08 đã bỏ bình khỏi thưởng) | Ôsin ở 52 có nút "Sa mạc hoang vu" ngoài giờ Mabư cho NV 36–37 |
| 37 | b1 chỉ Mabư / Hirudegarn (đều theo giờ) | b1 max 30: Mabư / Hirudegarn cộng trọn 30, hoặc 30 Cadic M ở 165; Lõi Phép (b3) rơi cả từ Cadic M ở 165 |

## NV 20 / NV 48 về Thung lũng Nappa (patch 13)

Chủ dự án không muốn người chơi NV 20 vào Khu hang động (160) vì cày sức mạnh quá nhanh.
Berry (71) và Granola (76) đứng thêm ở Thung lũng Nappa (68) cạnh Cui; mọi bước gặp
Berry / Granola làm ở 68 (code vẫn nhận ở 160 cho người đang dở). Gỡ nút "Khu hang động"
của Cui (thay cho mục 2 ở trên). Khu hang động lại chỉ vào bằng Nhẫn thời không từ NV 32.
