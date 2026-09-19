# 45 — Lối vào / lối ra Lãnh địa Bang Hội (map 153)

> Yêu cầu của chủ dự án: NV 13 bước 2 "Gặp Giu-ma Đầu Bò" diễn ra ở **map 153 Lãnh địa Bang Hội** — map **không có cửa đi bộ**. Trước đây chỉ Quy Lão Kame (Trái Đất) đưa vào được, vào rồi không có đường ra, bấm khi chưa đủ điều kiện thì im lặng, câu nhắc nhiệm vụ không chỉ đường.
> Đường dẫn Java viết tắt `models/` = `SRC/src/nro/models/`. Bối cảnh: `docs/1-he-thong-hien-tai/17-bang-hoi.md` §12.
> Đã **biên dịch sạch** (JDK 17, 579 file) và **chạy thử SQL** trên DB tạm (dump `database team2026.sql` → 01 → 02 → … → 10, không lỗi; chạy 10 lần hai; đã xóa DB tạm). Lưu ý: máy thử là MySQL 8.4, file 10 chỉ dùng cú pháp chung với MariaDB 10.4 (`SET NAMES`, `UPDATE`, `SELECT`).

## 1. Logic dùng chung

`models/npc_list/QuyLaoKame.java` có hàm mới **`public static void goToClanTerritory(Player)`** — cả 3 NPC sư phụ đều gọi hàm này (không chép code):

1. `TaskService.gI().getIdTask(player) < ConstTask.TASK_13_0` → thông báo **"Hoàn thành nhiệm vụ 12 để mở Lãnh địa Bang Hội"** (trước đây `ChangeMapService.checkMapCanJoin` chặn map 153 mà không báo gì).
2. Chưa có bang → giữ thông báo cũ **"Bạn cần có bang hội để thực hiện chức năng này."**
3. Đủ điều kiện → `changeMapNonSpaceship(player, 153, random 100..200, 432)` y như trước.

Hàm riêng `handleClanMapChange` của Quy Lão giờ chỉ gọi `goToClanTerritory`.

## 2. Thay đổi menu (trước / sau, chỉ số)

### 2.1 Quy Lão Kame (map 5) — menu "Nói chuyện" (index menu 0)

| Chỉ số | Trước (có bang) | Trước (không bang) | Sau (có bang) | Sau (không bang) |
|---|---|---|---|---|
| 0 | Nhiệm vụ | Nhiệm vụ | Nhiệm vụ | Nhiệm vụ |
| 1 | Học Kỹ năng | Học Kỹ năng | Học Kỹ năng | Học Kỹ năng |
| 2 | Về khu vực bang | — | Về khu vực bang | **Về khu vực bang** (mới hiện) |
| 3 | Kho báu dưới biển | — | Kho báu dưới biển | — |
| 4 | Giải tán Bang hội (bang chủ) | — | Giải tán Bang hội (bang chủ) | — |

Nút luôn nằm ở chỉ số 2 nên `handleMenu0` **không phải đổi**: `case 2` → Về khu vực bang; nhánh `default` vẫn kiểm `player.clan != null` trước khi xử lý 3 (Kho báu) / 4 (Giải tán) — người không bang chỉ có 3 nút nên không thể chọn 3/4. Người chưa có bang giờ bấm được và nhận thông báo (bước 2 mục 1) thay vì không thấy nút.

### 2.2 Trưởng lão Guru (map 13) và Vua Vegeta (map 20) — menu gốc (`BASE_MENU`)

| Chỉ số | Trước | Sau | Xử lý (`handleBaseMenu`) |
|---|---|---|---|
| 0 | Nhiệm vụ | Nhiệm vụ | không đổi |
| 1 | Học Kỹ năng | Học Kỹ năng | không đổi |
| 2 | — | **Về khu vực bang** | **mới:** `QuyLaoKame.goToClanTerritory(player)` |

Nút thêm vào **cuối** danh sách nên chỉ số 0/1 không lệch. Menu học cấp tốc (index 12) không đổi. Người khác hành tinh vẫn nhận câu "Con hãy về hành tinh của mình mà thể hiện" như cũ (không thấy menu).

### 2.3 Giu-ma Đầu Bò (map 153) — menu gốc (`BASE_MENU`)

| Chỉ số | Trước | Sau |
|---|---|---|
| 0 | Khiêu chiến Boss | Khiêu chiến Boss |
| 1 | Điểm danh +1 Capsule Bang | Điểm danh +1 Capsule Bang |
| 2 | OK (đi Porata, cần 40 tỷ SM) | OK |
| 3 | Cửa Hàng Bang hội | Cửa Hàng Bang hội |
| 4 | Từ chối | **Về nhà** → `changeMapBySpaceShip(player, 21 + gender, -1, 250)` (21 Nhà Gôhan / 22 Nhà Moori / 23 Nhà Broly) |
| 5 | — | Từ chối (nhánh `default`, không làm gì) |

- "Về nhà" chèn **trước** "Từ chối" để 0–3 giữ nguyên; "Từ chối" từ 4 thành 5 và vẫn rơi vào `default`.
- `confirmMenu` thêm điều kiện `player.idMark.isBaseMenu()` — NPC này chỉ có một menu, chặn trường hợp một menu khác còn treo gửi nhầm chỉ số.
- **Giữ nguyên** đoạn đầu `openBaseMenu`: `TaskService.gI().checkDoneTaskTalkNpc(player, this)` (sửa kẹt NV 13).

## 3. Câu nhắc NV 13 bước 2

Dòng `task_sub_template` `task_main_id = 13`, `ducvupro = 44` (TASK_13_2 "Gặp Giu-ma Đầu Bò"):

| Cột | Trước | Sau |
|---|---|---|
| `notify` | `Giu-ma Đầu Bò ở Lãnh địa Bang Hội` | `Gặp %10, chọn "Về khu vực bang" để vào Lãnh địa Bang Hội, gặp Giu-ma Đầu Bò` |
| `map` | `153` | `-9` (`ConstTask.MAP_QUY_LAO` → 5 / 13 / 20 theo hành tinh) |
| `npc_id` | 47 | 47 (không đổi — hệ thống nhiệm vụ cần đúng Giu-ma) |

`%10` = `TEN_NPC_QUY_LAO` → Quy Lão Kame / Trưởng lão Guru / Vua Vegeta. Độ dài sau thay thế: tối đa **87 ký tự** (Namếc) ≤ 100. Cột `map` chỉ dùng để client vẽ mũi tên (TaskService gửi `stm.mapId`), không ảnh hưởng điều kiện hoàn thành.

Đã sửa cả `SRC/sql/patch/02-nhiem-vu-moi.sql` (cài mới) và thêm `SRC/sql/patch/10-loi-vao-lanh-dia-bang.sql` (chỉ `UPDATE` 1 dòng, khớp `task_main_id` + `ducvupro`, chạy lại an toàn).
**Chú ý thứ tự:** `06-cap-nhat-chu-nhiem-vu.sql` ghi đè `notify` dòng này về câu cũ, nên cài mới vẫn phải chạy **10 sau 06**.

## 4. Triển khai

1. Build lại jar (code: `QuyLaoKame.java`, `TruongLaoGuru.java`, `VuaVegeta.java`, `GiuMaDauBo.java`).
2. `mysql -u root -p team2026 < SRC/sql/patch/10-loi-vao-lanh-dia-bang.sql` (có thể chạy khi server đang bật).
3. Khởi động lại server (nhiệm vụ chỉ nạp lúc khởi động).

## 5. Kiểm tra trong game

- Nhân vật NV ≤ 12: nói chuyện Quy Lão / Guru / Vegeta → "Về khu vực bang" → thấy "Hoàn thành nhiệm vụ 12 để mở Lãnh địa Bang Hội".
- NV 13, chưa có bang: thấy "Bạn cần có bang hội để thực hiện chức năng này."
- NV 13, có bang, Namếc/Xayda: vào được map 153 từ map 13 / 20.
- Ở map 153: Giu-ma → "Về nhà" → về 21/22/23 đúng hành tinh; "Từ chối" đóng menu.
- NV 13 bước 2: câu nhắc hiện tên sư phụ đúng hành tinh, mũi tên chỉ về map 5 / 13 / 20.
- Quy Lão có bang: "Kho báu dưới biển" (3) và "Giải tán Bang hội" (4) vẫn đúng chức năng.
