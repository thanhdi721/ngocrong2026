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
