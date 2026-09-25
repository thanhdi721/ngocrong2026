# 64 — Lượt soi thứ hai: HUNR và Bun còn tính năng gì (2026-09-25)

Bổ sung cho [60-tinh-nang-tu-sumo-bun.md](60-tinh-nang-tu-sumo-bun.md). Lần này soi
**source HUNR** (`/Users/phanthanhdi/Downloads/HUNR_Server/Hunr2026`, 504 file java — trước
giờ mới chỉ lấy res chứ chưa đọc code) và rà lại phần còn sót của Bun.

## Đáng lấy nhất: hệ LINH THÚ của HUNR

`com/ngocrong/combine/LinhThu` — 5 lớp, ~1.100 dòng, đúng thứ 24 con linh thú vừa mang về
đang thiếu (chúng không có chỉ số gì):

| Lớp | Việc | Nguyên liệu |
|---|---|---|
| `ApLinhThu` (97) | ấp Quả trứng linh thú ra linh thú | trứng + n Hồn linh thú |
| `NangCapLinhThu` (256) | nâng cấp 1 → 7 trong cùng một bậc | linh thú + Hồn linh thú |
| `NangBacLinhThu` | bậc 1 cấp 7 → bậc 2 | + Thăng tinh thạch |
| `NangChiSoLinhThu` (417) | cộng chỉ số cho linh thú bậc 2 | + Đá ma thuật |
| `XoaChiSoLinhThu` | xoá chỉ số để làm lại | + Đá GALLERY |

Đây là vòng lặp nuôi pet hoàn chỉnh (ấp → nuôi → lên bậc → rèn chỉ số → làm lại), khung y hệt
mấy chức năng ghép đồ bên mình nên viết lại không khó. Cần thêm 4 vật phẩm: Hồn linh thú,
Thăng tinh thạch, Đá ma thuật, Đá GALLERY.

## Bộ boss mang về được

| Bộ | Ở đâu | Số con | Bên mình |
|---|---|---|---|
| **Team Hải Tặc** (Luffy, Zoro, Nami, Sanji, Chopper, Robin, Franky, Brook, Usopp) | HUNR + Bun | 9 (639 dòng) | chưa có |
| **Team Tây Du** (Ngộ Không, Bát Giới, Đường Tăng) | HUNR + Bun | 3 (330 dòng) | chưa có |
| **Zamasu Fusion**, **Bardock Thời Không**, **Chilled** | HUNR | 3 | chưa có (chỉ có dữ liệu Zamasu) |
| Team Bojack | HUNR | 7 | **đã có** |

Hình cho hai team này nằm sẵn trong đợt res đã mang về (Kaido, Luffy… trong danh sách cải trang
SUMO) hoặc lấy tiếp từ HUNR.

## Thứ khác của HUNR

| Tính năng | Cỡ | Nhận xét |
|---|---|---|
| **Tự trao thưởng top** (`top/AutoReward`) | 240 dòng | đọc bảng `rewardtop`, phát quà rồi đánh dấu đã phát — thay việc trao tay |
| **Thống kê** (`statistic/StatisticService`) | | ghi số liệu người chơi |
| **Mở rộng map** (`map/expansion`, `blackdragon`) | | thêm vùng bản đồ mới |
| **Chat thoại** (`voicechat` + `server/voice`) | ~400 dòng | **cần client hỗ trợ**, bên mình là client Unity riêng — rủi ro cao |
| **Chống hack nhiều lớp** (`security/multilayer`) | 6 lớp + challenges | server ra câu đố, client phải trả lời đúng — **client phải có cùng thuật toán**, không mang sang được |

## Đã có rồi (khỏi ngó)

Con Số May Mắn và Kéo Búa Bao (bên mình ở `services_func/MiniGame`), vòng quay Thượng Đế
(HUNR gọi là `crackball` — mình đã làm lại tháng này), cây đậu thần, bùa (`Charms`),
hiệu ứng da (`EffectSkin`), chiêu đặc biệt (`SkillSpecial`), sổ thẻ rađa, Mabu 14h, đại hội
võ thuật, Whis, Cumber.

Bên Bun soi lại lần hai thì hết: những gì còn lại (BauCua, TaiXiu, PetFollow, EffectFlagBag,
MiniPet) đã nằm trong danh sách ở tài liệu 60.

## Đề nghị

1. **Hệ linh thú** — đáng làm nhất, nối thẳng vào 24 con vừa mang về và vào bộ Đá Pháp Sư
   (cùng một mạch "săn boss lấy đá → rèn đồ").
2. **Team Hải Tặc / Tây Du** — nội dung boss theo nhóm, mang về nhanh vì khung boss giống nhau.
3. **Tự trao thưởng top** — nhẹ, đỡ việc tay.
4. Chat thoại và chống hack nhiều lớp: **không nên**, cả hai đều phải sửa client.

## Chưa kiểm tra được

Mới đọc code chứ chưa chạy thử cái nào; các con số dòng là để ước lượng công, không phải
cam kết là chép nguyên được.
