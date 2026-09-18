# 13 — Nhiệm vụ phụ (hàng ngày), nhiệm vụ bang, thành tích và danh hiệu

> Tài liệu spec tổng hợp **chỉ từ code và DB thật**:
> - Code (tương đối từ `SRC/src/nro/models/`): `services/TaskService.java` (phần SIDE TASK / CLAN TASK), `task/SideTask*.java`, `task/ClanTask*.java`, `task/BadgesTask*.java`, `player_badges/*.java`, `services/AchievementService.java`, `player/Achievement.java`, `consts/ConstTask.java`, `consts/ConstAchievement.java`, `consts/ConstTaskBadges.java`, `npc_list/BoMong.java`, `npc_list/DrDrief.java`, `shop/ShopService.java`, `shop/TabShopDanhHieu.java`, `shop/TabShopSoHuu.java`.
> - DB `database team2026.sql`: `side_task_template` (59 dòng), `clan_task_template` (59 dòng), `achievement_template` (20 dòng), `task_badges_template` (18 dòng), `data_badges` (16 dòng); tên tra theo `mob_template`, `item_template`, `item_option_template`, `npc_template`, `map_template`, `shop`, `tab_shop`, `item_shop`.

## Mục lục

1. [Nhiệm vụ hàng ngày (Bò Mộng)](#1-nhiệm-vụ-hàng-ngày-bò-mộng)
   1. [Dữ liệu & cấp độ](#11-dữ-liệu--cấp-độ)
   2. [Nhận nhiệm vụ](#12-nhận-nhiệm-vụ)
   3. [Tiến độ](#13-tiến-độ)
   4. [Trả nhiệm vụ & phần thưởng](#14-trả-nhiệm-vụ--phần-thưởng)
   5. [Hủy nhiệm vụ](#15-hủy-nhiệm-vụ)
   6. [Lưu / tải / reset ngày](#16-lưu--tải--reset-ngày)
   7. [Danh sách mẫu nhiệm vụ hàng ngày](#17-danh-sách-mẫu-nhiệm-vụ-hàng-ngày)
   8. [Các chức năng khác của Bò Mộng](#18-các-chức-năng-khác-của-bò-mộng)
2. [Nhiệm vụ bang hội](#2-nhiệm-vụ-bang-hội)
3. [Thành tích (Achievement)](#3-thành-tích-achievement)
4. [Danh hiệu (Badges)](#4-danh-hiệu-badges)
5. [NPC giao nhiệm vụ khác](#5-npc-giao-nhiệm-vụ-khác)
6. [Ghi chú / điểm cần lưu ý](#6-ghi-chú--điểm-cần-lưu-ý)

---

## 1. Nhiệm vụ hàng ngày (Bò Mộng)

### 1.1. Dữ liệu & cấp độ

| Thành phần | Mô tả |
|---|---|
| `SideTaskTemplate` | `id`, `name` (chứa `%1` = số lượng), `count[5][2]` = khoảng [min, max] cho 5 cấp độ, nạp từ cột `max_count_lv1..lv5` dạng `"min-max"` (`Manager.java`) |
| `SideTask` (của người chơi) | `template`, `count`, `maxCount`, `level`, `leftTask` (mặc định `MAX_SIDE_TASK = 10`), `receivedTime`, cờ thông báo `notify0..notify90` |

| Hằng `ConstTask` | Giá trị | Chuỗi `getLevel()` | Vàng thưởng | Item thưởng |
|---|---|---|---|---|
| `EASY` | 0 | dễ | `GOLD_EASY` = 5.000 | 708 Bí ngô 7 sao |
| `NORMAL` | 1 | bình thường | `GOLD_NORMAL` = 20.000 | 707 Bí ngô 6 sao |
| `HARD` | 2 | khó | `GOLD_HARD` = 50.000 | 706 Bí ngô 5 sao |
| `VERY_HARD` | 3 | rất khó | `GOLD_VERY_HARD` = 200.000 | 705 Bí ngô 4 sao |
| `HELL` | 4 | địa ngục | `GOLD_HELL` = **25** | 704 Bí ngô 3 sao, hoặc 822 Cây thông (xem §1.4) |

### 1.2. Nhận nhiệm vụ

- NPC **Bò Mộng** (`npc_template` id 17, `npc_list/BoMong.java`). Menu nhiệm vụ chỉ mở ở **map 47 Rừng Karin** và **map 84 Siêu Thị** (NPC còn được đặt ở map 21/22/23 nhưng không có menu). Trước khi mở menu, NPC gọi `checkDoneTaskTalkNpc` (không có case cho Bò Mộng).
- Menu gốc: "Nhiệm vụ hàng ngày", "Nhiệm vụ thành tích", "Nạp Ngọc", "Điểm danh", "Từ chối".
- Chọn "Nhiệm vụ hàng ngày" khi **chưa có** nhiệm vụ → menu cấp độ: **"Dễ", "Bình thường", "Khó", "Từ chối"** → `TaskService.changeSideTask(player, select)` với select 0/1/2.
  - **Cấp "Rất khó" (3) và "Địa ngục" (4) không có trên menu** → người chơi không nhận được qua NPC.
- `changeSideTask`:
  1. `sideTask.renew()`: nếu `receivedTime` thuộc ngày trước (`Util.isAfterMidnight`) → `leftTask = 10`, `receivedTime = now`.
  2. Nếu `leftTask > 0`: `reset()`, chọn **ngẫu nhiên 1 mẫu** trong toàn bộ `SIDE_TASKS_TEMPLATE`, `maxCount = random[min, max]` theo cấp, `leftTask--`, lưu `level`, `receivedTime`; thông báo "Bạn nhận được nhiệm vụ: …".
  3. Nếu hết lượt: "Bạn đã nhận hết nhiệm vụ hôm nay. Hãy chờ tới ngày mai rồi nhận tiếp".
- Khi **đang có** nhiệm vụ, chọn "Nhiệm vụ hàng ngày" hiển thị: tên, cấp độ, `count/maxCount (%)`, "Số nhiệm vụ còn lại trong ngày: leftTask/10", với 2 nút "Trả nhiệm vụ" và "Hủy nhiệm vụ".

**Số lần/ngày**: tối đa 10 lượt nhận (`MAX_SIDE_TASK`), trừ lượt **ngay khi nhận**.

### 1.3. Tiến độ

| Hàm | Gọi từ | Điều kiện |
|---|---|---|
| `checkDoneSideTaskKillMob(player, mob)` | `mob/Mob.java` khi mob chết (người đánh cuối) | So `template.id` với `mob.tempId` theo bảng §1.7 (id 0–57) → `count++` |
| `checkDoneSideTaskPickItem(player, itemMap)` | `map/Zone.java` khi nhặt | Mẫu id 58 "Nhặt %1 vàng": item có `itemTemplate.type == 9` → `count += quantity` |

`notifyProcessSideTask` thông báo mỗi khi vượt mốc 0/10/…/90% ("Nhiệm vụ: … đã hoàn thành: x/y (p%)"); khi đạt 100%: "Chúc mừng bạn đã hoàn thành nhiệm vụ, bây giờ hãy quay về Bò Mộng trả nhiệm vụ."

### 1.4. Trả nhiệm vụ & phần thưởng

`TaskService.paySideTask(player)`:

1. Có template và `isDone()` (`count >= maxCount`), nếu không: "Bạn chưa hoàn thành nhiệm vụ".
2. Theo cấp độ chọn `goldReward` và `ngocBi` (bảng §1.1). Với `HELL`: nếu `leftTask < 15` (luôn đúng vì tối đa 10) thì `cayThong = 822`.
3. Cần **`getCountEmptyBag > 1`** (tức ≥ 2 ô trống), nếu không: "Hành trang không đủ chỗ trống."
4. Trao item:
   - Nếu `cayThong != -1` **và** `canNhanCayThong` (field của singleton, khởi tạo `true`): tắt cờ, tạo 822 Cây thông với option: 50% có option ngẫu nhiên 11/12/13 param 100; option 24; option 110 param 118–126; option 110 param 110–113; option 93 = 30 (hạn 30 ngày).
   - Ngược lại: 1 Bí ngô (704–708) kèm option 93 = 30 (**Hạn sử dụng 30 ngày**).
5. `inventory.addGold(goldReward)` và thông báo "Bạn nhận được … vàng"; `sideTask.reset()`.
6. Tiến độ danh hiệu `NONG_DAN_CHAM_CHI` (task badges id 6) được cộng **3 lần** mỗi lần trả (1 trong switch cấp độ, 1 trong nhánh trao item, 1 sau khi cộng vàng).

### 1.5. Hủy nhiệm vụ

`TaskService.removeSideTask`: thông báo "Bạn vừa hủy bỏ nhiệm vụ …" và `reset()`. **Không hỏi xác nhận, không hoàn lại lượt** (lượt đã bị trừ khi nhận).

### 1.6. Lưu / tải / reset ngày

| Việc | Nơi | Nội dung |
|---|---|---|
| Lưu | `database/PlayerDAO.java` | JSON `[templateId (-1 nếu không có), receivedTime, count, maxCount, leftTask, level]` |
| Tải | `database/MrBlue.java` (cột `data_side_task`) | Chỉ khôi phục nếu `receivedTime` **cùng ngày** (so chuỗi `dd-MM-yyyy`); khác ngày → bỏ nhiệm vụ đang làm, `leftTask` về 10 |
| Reset lượt | `SideTask.renew()` (chỉ gọi trong `changeSideTask`) | Sang ngày mới → `leftTask = 10` |

### 1.7. Danh sách mẫu nhiệm vụ hàng ngày

Cột "Mục tiêu" là mob thật được so sánh trong `checkDoneSideTaskKillMob` (hằng `ConstMob` → `mob_template`). Người chơi nhận được Lv1–Lv3 qua Bò Mộng.

| id | Tên mẫu (`side_task_template.NAME`) | Mục tiêu (ConstMob → mob_template) | Lv1 Dễ | Lv2 Bình thường | Lv3 Khó | Lv4 Rất khó | Lv5 Địa ngục |
|---|---|---|---|---|---|---|---|
| 0 | Tiêu diệt %1 khủng long | mob 1 Khủng long | 1-20 | 20-100 | 100-500 | 500-2000 | 2000-5000 |
| 1 | Tiêu diệt %1 lợn lòi | mob 2 Lợn lòi | 1-20 | 20-100 | 100-500 | 500-2000 | 2000-5000 |
| 2 | Tiêu diệt %1 quỷ đất | mob 3 Quỷ đất | 1-20 | 20-100 | 100-500 | 500-2000 | 2000-5000 |
| 3 | Tiêu diệt %1 khủng long mẹ | mob 4 Khủng long mẹ | 1-20 | 20-100 | 100-500 | 500-2000 | 2000-5000 |
| 4 | Tiêu diệt %1 lợn lòi mẹ | mob 5 Lợn lòi mẹ | 1-20 | 20-100 | 100-500 | 500-2000 | 2000-5000 |
| 5 | Tiêu diệt %1 quỷ đất mẹ | mob 6 Quỷ đất mẹ | 1-20 | 20-100 | 100-500 | 500-2000 | 2000-5000 |
| 6 | Tiêu diệt %1 thằn lằn bay | mob 7 Thằn lằn bay | 1-20 | 20-100 | 100-500 | 500-2000 | 2000-5000 |
| 7 | Tiêu diệt %1 phi long | mob 8 Phi long | 1-20 | 20-100 | 100-500 | 500-2000 | 2000-5000 |
| 8 | Tiêu diệt %1 quỷ bay | mob 9 Quỷ bay | 1-20 | 20-100 | 100-500 | 500-2000 | 2000-5000 |
| 9 | Tiêu diệt %1 thằn lằn mẹ | mob 10 Thằn lằn mẹ | 1-20 | 20-100 | 100-500 | 500-2000 | 2000-5000 |
| 10 | Tiêu diệt %1 phi long mẹ | mob 11 Phi long mẹ | 1-20 | 20-100 | 100-500 | 500-2000 | 2000-5000 |
| 11 | Tiêu diệt %1 quỷ bay mẹ | mob 12 Quỷ bay mẹ | 1-20 | 20-100 | 100-500 | 500-2000 | 2000-5000 |
| 12 | Tiêu diệt %1 heo rừng | mob 16 Heo rừng | 1-20 | 20-100 | 100-500 | 500-2000 | 2000-5000 |
| 13 | Tiêu diệt %1 heo da xanh | mob 17 Heo da xanh | 1-20 | 20-100 | 100-500 | 500-2000 | 2000-5000 |
| 14 | Tiêu diệt %1 heo xayda | mob 18 Heo Xayda | 1-20 | 20-100 | 100-500 | 500-2000 | 2000-5000 |
| 15 | Tiêu diệt %1 ốc mượn hồn | mob 13 Ốc mượn hồn | 1-20 | 20-100 | 100-500 | 500-2000 | 2000-5000 |
| 16 | Tiêu diệt %1 ốc sên | mob 14 Ốc sên | 1-20 | 20-100 | 100-500 | 500-2000 | 2000-5000 |
| 17 | Tiêu diệt %1 heo xayda mẹ | mob 15 Heo Xayda mẹ | 1-20 | 20-100 | 100-500 | 500-2000 | 2000-5000 |
| 18 | Tiêu diệt %1 không tặc | mob 31 Không tặc | 1-20 | 20-100 | 100-500 | 500-2000 | 2000-5000 |
| 19 | Tiêu diệt %1 quỷ đầu to | mob 32 Quỷ đầu to | 1-20 | 20-100 | 100-500 | 500-2000 | 2000-5000 |
| 20 | Tiêu diệt %1 quỷ địa ngục | mob 33 Quỷ địa ngục | 1-20 | 20-100 | 100-500 | 500-2000 | 2000-5000 |
| 21 | Tiêu diệt %1 heo rừng mẹ | mob 19 Heo rừng mẹ | 1-20 | 20-100 | 100-500 | 500-2000 | 2000-5000 |
| 22 | Tiêu diệt %1 heo xanh mẹ | mob 20 Heo xanh mẹ | 1-20 | 20-100 | 100-500 | 500-2000 | 2000-5000 |
| 23 | Tiêu diệt %1 alien | mob 21 Alien | 1-20 | 20-100 | 100-500 | 500-2000 | 2000-5000 |
| 24 | Tiêu diệt %1 tambourine | mob 25 Tambourine | 1-5 | 5-20 | 20-100 | 100-500 | 500-1000 |
| 25 | Tiêu diệt %1 drum | mob 26 Drum | 1-5 | 5-20 | 20-100 | 100-500 | 500-1000 |
| 26 | Tiêu diệt %1 akkuman | mob 27 Akkuman | 1-5 | 5-20 | 20-100 | 100-500 | 500-1000 |
| 27 | Tiêu diệt %1 nappa | mob 39 Nappa | 1-5 | 5-20 | 20-100 | 100-500 | 500-1000 |
| 28 | Tiêu diệt %1 soldier | mob 40 Soldier | 1-5 | 5-20 | 20-100 | 100-500 | 500-1000 |
| 29 | Tiêu diệt %1 appule | mob 41 Appule | 1-5 | 5-20 | 20-100 | 100-500 | 500-1000 |
| 30 | Tiêu diệt %1 raspberry | mob 42 Raspberry | 1-5 | 5-20 | 20-100 | 100-500 | 500-1000 |
| 31 | Tiêu diệt %1 thằn lằn xanh | mob 43 Thằn lằn xanh | 1-5 | 5-20 | 20-100 | 100-500 | 500-1000 |
| 32 | Tiêu diệt %1 quỷ đầu nhọn | mob 44 Quỷ đầu nhọn | 1-5 | 5-20 | 20-100 | 100-500 | 500-1000 |
| 33 | Tiêu diệt %1 quỷ đầu vàng | mob 45 Quỷ đầu vàng | 1-5 | 5-20 | 20-100 | 100-500 | 500-1000 |
| 34 | Tiêu diệt %1 quỷ da tím | mob 46 Quỷ da tím | 1-5 | 5-20 | 20-100 | 100-500 | 500-1000 |
| 35 | Tiêu diệt %1 quỷ già | mob 47 Quỷ già | 1-5 | 5-20 | 20-100 | 100-500 | 500-1000 |
| 36 | Tiêu diệt %1 cá sấu | mob 48 Cá sấu | 1-5 | 5-20 | 20-100 | 100-500 | 500-1000 |
| 37 | Tiêu diệt %1 dơi da xanh | mob 49 Dơi da xanh | 1-5 | 5-20 | 20-100 | 100-500 | 500-1000 |
| 38 | Tiêu diệt %1 quỷ chim | mob 50 Quỷ chim | 1-5 | 5-20 | 20-100 | 100-500 | 500-1000 |
| 39 | Tiêu diệt %1 lính đầu trọc | mob 51 Lính đầu trọc | 1-5 | 5-20 | 20-100 | 100-500 | 500-1000 |
| 40 | Tiêu diệt %1 lính tai dài | mob 52 Lính tai dài | 1-5 | 5-20 | 20-100 | 100-500 | 500-1000 |
| 41 | Tiêu diệt %1 lính vũ trụ | mob 53 Lính vũ trụ | 1-5 | 5-20 | 20-100 | 100-500 | 500-1000 |
| 42 | Tiêu diệt %1 khỉ lông đen | mob 54 Khỉ lông đen | 1-5 | 5-20 | 20-100 | 100-500 | 500-1000 |
| 43 | Tiêu diệt %1 khỉ giáp sắt | mob 55 Khỉ giáp sắt | 1-5 | 5-20 | 20-100 | 100-500 | 500-1000 |
| 44 | Tiêu diệt %1 khỉ lông đỏ | mob 56 Khỉ lông đỏ | 1-5 | 5-20 | 20-100 | 100-500 | 500-1000 |
| 45 | Tiêu diệt %1 khỉ lông vàng | mob 57 Khỉ lông vàng | 1-5 | 5-20 | 20-100 | 100-500 | 500-1000 |
| 46 | Tiêu diệt %1 xên con cấp 1 | mob 58 Xên con cấp 1 | 1-5 | 5-20 | 20-100 | 100-500 | 500-1000 |
| 47 | Tiêu diệt %1 xên con cấp 2 | mob 59 Xên con cấp 2 | 1-5 | 5-20 | 20-100 | 100-500 | 500-1000 |
| 48 | Tiêu diệt %1 xên con cấp 3 | mob 60 Xên con cấp 3 | 1-5 | 5-20 | 20-100 | 100-500 | 500-1000 |
| 49 | Tiêu diệt %1 xên con cấp 4 | mob 61 Xên con cấp  4 | 1-5 | 5-20 | 20-100 | 100-500 | 500-1000 |
| 50 | Tiêu diệt %1 xên con cấp 5 | mob 62 Xên con cấp  5 | 1-5 | 5-20 | 20-100 | 100-500 | 500-1000 |
| 51 | Tiêu diệt %1 xên con cấp 6 | mob 63 Xên con cấp  6 | 1-5 | 5-20 | 20-100 | 100-500 | 500-1000 |
| 52 | Tiêu diệt %1 xên con cấp 7 | mob 64 Xên con cấp  7 | 1-5 | 5-20 | 20-100 | 100-500 | 500-1000 |
| 53 | Tiêu diệt %1 xên con cấp 8 | mob 65 Xên con cấp  8 | 1-5 | 5-20 | 20-100 | 100-500 | 500-1000 |
| 54 | Tiêu diệt %1 tai tím | mob 66 Tai tím | 1-5 | 5-20 | 20-100 | 100-500 | 500-1000 |
| 55 | Tiêu diệt %1 abo | mob 67 Abo | 1-5 | 5-20 | 20-100 | 100-500 | 500-1000 |
| 56 | Tiêu diệt %1 kado | mob 68 Kado | 1-5 | 5-20 | 20-100 | 100-500 | 500-1000 |
| 57 | Tiêu diệt %1 da xanh | mob 69 Da xanh | 1-5 | 5-20 | 20-100 | 100-500 | 500-1000 |
| 58 | Nhặt %1 vàng | Nhặt item `type == 9` (vàng) — cộng theo `quantity` | 1000-3000 | 3000-20000 | 20000-100000 | 100000-10000000 | 10000000-100000000 |

### 1.8. Các chức năng khác của Bò Mộng

| Nút | Hành vi (`BoMong.confirmMenu`) |
|---|---|
| Nhiệm vụ thành tích | `AchievementService.openAchievementUI` (xem §3) |
| Nạp Ngọc | `Input.createFormTradeGem` — đổi VNĐ sang ngọc (cũng cộng tiến độ danh hiệu 1 và 16, xem §4) |
| Điểm danh | 1 lần/ngày (so `lastCheckIn` theo ngày): **+10.000 ngọc** và **100 Thỏi vàng** (item 457, option 30 "Không thể giao dịch") |

---

## 2. Nhiệm vụ bang hội

### 2.1. Dữ liệu

`ClanTaskTemplate` / `ClanTask` có cấu trúc **giống hệt** nhiệm vụ hàng ngày (template + khoảng số lượng 5 cấp), nhưng `leftTask` mặc định `MAX_CLAN_TASK = 5`. Bảng `clan_task_template` có 59 mẫu, cùng mục tiêu với `side_task_template`, chỉ khác tiền tố tên ("Hạ %1 …" thay vì "Tiêu diệt %1 …").

### 2.2. Nhận / xem / hủy / trả

- NPC **Dr. Brief** (`npc_template` id 10) tại **map 153 Lãnh địa Bang Hội** (`npc_list/DrDrief.java`). Nút "Nhiệm vụ Bang [leftTask/5]" chỉ hiện khi người chơi **có bang** (bang chủ: nút thứ 2; thành viên: nút thứ 1).
- Chưa có nhiệm vụ → `TaskService.changeClanTask(npc, player, Util.nextInt(5))`: cấp độ **ngẫu nhiên 0–4** (cả 5 cấp, người chơi không chọn), mẫu ngẫu nhiên, `maxCount` random trong khoảng. `renew()` reset lượt khi sang ngày. **Không trừ lượt khi nhận**. Hết lượt → "Đã hết nhiệm vụ cho hôm nay, hãy chờ đến ngày mai".
- Đang có, chưa xong → menu "Nhiệm vụ hiện tại: … Đã hạ được N" với "OK" / "Hủy bỏ Nhiệm vụ này" → xác nhận "Nếu hủy nhiệm vụ bạn sẽ mất 1 lượt nhiệm vụ trong ngày." → `removeClanTask`: `leftTask--`, `reset()`.
- Đã xong → "Nhiệm vụ đã hoàn thành, hãy nhận (level+1)×10 capsule bang" → `payClanTask`.
- Tiến độ: `checkDoneClanTaskKillMob` / `checkDoneClanTaskPickItem` (cùng quy tắc với §1.3), khi 100%: "Tiếp theo hãy về Bang hội báo cáo." — **không** yêu cầu đang ở trong bang khi đánh quái.

### 2.3. Phần thưởng — `TaskService.payClanTask`

| Cấp độ | Tên (`getLevel`) | Capsule bang |
|---|---|---|
| 0 | dễ | 10 |
| 1 | bình thường | 20 |
| 2 | khó | 30 |
| 3 | rất khó | 40 |
| 4 | địa ngục | 50 |

- `leftTask--`, `reset()`, thông báo "Bạn vừa nhận được N capsule bang."
- Nếu `player.clan != null`: `clan.capsuleClan += N`; thành viên đó `memberPoint += N`, `clanPoint += N`; gửi lại thông tin bang. Capsule bang dùng để nâng cấp bang tại Dr. Brief ("Nâng cấp Bang hội", `ClanService.capsule(clan)`).

### 2.4. Lưu / tải

`PlayerDAO` lưu JSON `[templateId, receivedTime, count, maxCount, leftTask, level]`; `MrBlue` (cột `data_clan_task`) chỉ khôi phục nếu cùng ngày.

### 2.5. Danh sách mẫu nhiệm vụ bang

| id | Tên mẫu (`clan_task_template.NAME`) | Mục tiêu (ConstMob → mob_template) | Lv1 Dễ | Lv2 Bình thường | Lv3 Khó | Lv4 Rất khó | Lv5 Địa ngục |
|---|---|---|---|---|---|---|---|
| 0 | Hạ %1 khủng long | mob 1 Khủng long | 1-20 | 20-100 | 100-500 | 500-2000 | 2000-5000 |
| 1 | Hạ %1 lợn lòi | mob 2 Lợn lòi | 1-20 | 20-100 | 100-500 | 500-2000 | 2000-5000 |
| 2 | Hạ %1 quỷ đất | mob 3 Quỷ đất | 1-20 | 20-100 | 100-500 | 500-2000 | 2000-5000 |
| 3 | Hạ %1 khủng long mẹ | mob 4 Khủng long mẹ | 1-20 | 20-100 | 100-500 | 500-2000 | 2000-5000 |
| 4 | Hạ %1 lợn lòi mẹ | mob 5 Lợn lòi mẹ | 1-20 | 20-100 | 100-500 | 500-2000 | 2000-5000 |
| 5 | Hạ %1 quỷ đất mẹ | mob 6 Quỷ đất mẹ | 1-20 | 20-100 | 100-500 | 500-2000 | 2000-5000 |
| 6 | Hạ %1 thằn lằn bay | mob 7 Thằn lằn bay | 1-20 | 20-100 | 100-500 | 500-2000 | 2000-5000 |
| 7 | Hạ %1 phi long | mob 8 Phi long | 1-20 | 20-100 | 100-500 | 500-2000 | 2000-5000 |
| 8 | Hạ %1 quỷ bay | mob 9 Quỷ bay | 1-20 | 20-100 | 100-500 | 500-2000 | 2000-5000 |
| 9 | Hạ %1 thằn lằn mẹ | mob 10 Thằn lằn mẹ | 1-20 | 20-100 | 100-500 | 500-2000 | 2000-5000 |
| 10 | Hạ %1 phi long mẹ | mob 11 Phi long mẹ | 1-20 | 20-100 | 100-500 | 500-2000 | 2000-5000 |
| 11 | Hạ %1 quỷ bay mẹ | mob 12 Quỷ bay mẹ | 1-20 | 20-100 | 100-500 | 500-2000 | 2000-5000 |
| 12 | Hạ %1 heo rừng | mob 16 Heo rừng | 1-20 | 20-100 | 100-500 | 500-2000 | 2000-5000 |
| 13 | Hạ %1 heo da xanh | mob 17 Heo da xanh | 1-20 | 20-100 | 100-500 | 500-2000 | 2000-5000 |
| 14 | Hạ %1 heo xayda | mob 18 Heo Xayda | 1-20 | 20-100 | 100-500 | 500-2000 | 2000-5000 |
| 15 | Hạ %1 ốc mượn hồn | mob 13 Ốc mượn hồn | 1-20 | 20-100 | 100-500 | 500-2000 | 2000-5000 |
| 16 | Hạ %1 ốc sên | mob 14 Ốc sên | 1-20 | 20-100 | 100-500 | 500-2000 | 2000-5000 |
| 17 | Hạ %1 heo xayda mẹ | mob 15 Heo Xayda mẹ | 1-20 | 20-100 | 100-500 | 500-2000 | 2000-5000 |
| 18 | Hạ %1 không tặc | mob 31 Không tặc | 1-20 | 20-100 | 100-500 | 500-2000 | 2000-5000 |
| 19 | Hạ %1 quỷ đầu to | mob 32 Quỷ đầu to | 1-20 | 20-100 | 100-500 | 500-2000 | 2000-5000 |
| 20 | Hạ %1 quỷ địa ngục | mob 33 Quỷ địa ngục | 1-20 | 20-100 | 100-500 | 500-2000 | 2000-5000 |
| 21 | Hạ %1 heo rừng mẹ | mob 19 Heo rừng mẹ | 1-20 | 20-100 | 100-500 | 500-2000 | 2000-5000 |
| 22 | Hạ %1 heo xanh mẹ | mob 20 Heo xanh mẹ | 1-20 | 20-100 | 100-500 | 500-2000 | 2000-5000 |
| 23 | Hạ %1 alien | mob 21 Alien | 1-20 | 20-100 | 100-500 | 500-2000 | 2000-5000 |
| 24 | Hạ %1 tambourine | mob 25 Tambourine | 1-5 | 5-20 | 20-100 | 100-500 | 500-1000 |
| 25 | Hạ %1 drum | mob 26 Drum | 1-5 | 5-20 | 20-100 | 100-500 | 500-1000 |
| 26 | Hạ %1 akkuman | mob 27 Akkuman | 1-5 | 5-20 | 20-100 | 100-500 | 500-1000 |
| 27 | Hạ %1 nappa | mob 39 Nappa | 1-5 | 5-20 | 20-100 | 100-500 | 500-1000 |
| 28 | Hạ %1 soldier | mob 40 Soldier | 1-5 | 5-20 | 20-100 | 100-500 | 500-1000 |
| 29 | Hạ %1 appule | mob 41 Appule | 1-5 | 5-20 | 20-100 | 100-500 | 500-1000 |
| 30 | Hạ %1 raspberry | mob 42 Raspberry | 1-5 | 5-20 | 20-100 | 100-500 | 500-1000 |
| 31 | Hạ %1 thằn lằn xanh | mob 43 Thằn lằn xanh | 1-5 | 5-20 | 20-100 | 100-500 | 500-1000 |
| 32 | Hạ %1 quỷ đầu nhọn | mob 44 Quỷ đầu nhọn | 1-5 | 5-20 | 20-100 | 100-500 | 500-1000 |
| 33 | Hạ %1 quỷ đầu vàng | mob 45 Quỷ đầu vàng | 1-5 | 5-20 | 20-100 | 100-500 | 500-1000 |
| 34 | Hạ %1 quỷ da tím | mob 46 Quỷ da tím | 1-5 | 5-20 | 20-100 | 100-500 | 500-1000 |
| 35 | Hạ %1 quỷ già | mob 47 Quỷ già | 1-5 | 5-20 | 20-100 | 100-500 | 500-1000 |
| 36 | Hạ %1 cá sấu | mob 48 Cá sấu | 1-5 | 5-20 | 20-100 | 100-500 | 500-1000 |
| 37 | Hạ %1 dơi da xanh | mob 49 Dơi da xanh | 1-5 | 5-20 | 20-100 | 100-500 | 500-1000 |
| 38 | Hạ %1 quỷ chim | mob 50 Quỷ chim | 1-5 | 5-20 | 20-100 | 100-500 | 500-1000 |
| 39 | Hạ %1 lính đầu trọc | mob 51 Lính đầu trọc | 1-5 | 5-20 | 20-100 | 100-500 | 500-1000 |
| 40 | Hạ %1 lính tai dài | mob 52 Lính tai dài | 1-5 | 5-20 | 20-100 | 100-500 | 500-1000 |
| 41 | Hạ %1 lính vũ trụ | mob 53 Lính vũ trụ | 1-5 | 5-20 | 20-100 | 100-500 | 500-1000 |
| 42 | Hạ %1 khỉ lông đen | mob 54 Khỉ lông đen | 1-5 | 5-20 | 20-100 | 100-500 | 500-1000 |
| 43 | Hạ %1 khỉ giáp sắt | mob 55 Khỉ giáp sắt | 1-5 | 5-20 | 20-100 | 100-500 | 500-1000 |
| 44 | Hạ %1 khỉ lông đỏ | mob 56 Khỉ lông đỏ | 1-5 | 5-20 | 20-100 | 100-500 | 500-1000 |
| 45 | Hạ %1 khỉ lông vàng | mob 57 Khỉ lông vàng | 1-5 | 5-20 | 20-100 | 100-500 | 500-1000 |
| 46 | Hạ %1 xên con cấp 1 | mob 58 Xên con cấp 1 | 1-5 | 5-20 | 20-100 | 100-500 | 500-1000 |
| 47 | Hạ %1 xên con cấp 2 | mob 59 Xên con cấp 2 | 1-5 | 5-20 | 20-100 | 100-500 | 500-1000 |
| 48 | Hạ %1 xên con cấp 3 | mob 60 Xên con cấp 3 | 1-5 | 5-20 | 20-100 | 100-500 | 500-1000 |
| 49 | Hạ %1 xên con cấp 4 | mob 61 Xên con cấp  4 | 1-5 | 5-20 | 20-100 | 100-500 | 500-1000 |
| 50 | Hạ %1 xên con cấp 5 | mob 62 Xên con cấp  5 | 1-5 | 5-20 | 20-100 | 100-500 | 500-1000 |
| 51 | Hạ %1 xên con cấp 6 | mob 63 Xên con cấp  6 | 1-5 | 5-20 | 20-100 | 100-500 | 500-1000 |
| 52 | Hạ %1 xên con cấp 7 | mob 64 Xên con cấp  7 | 1-5 | 5-20 | 20-100 | 100-500 | 500-1000 |
| 53 | Hạ %1 xên con cấp 8 | mob 65 Xên con cấp  8 | 1-5 | 5-20 | 20-100 | 100-500 | 500-1000 |
| 54 | Hạ %1 tai tím | mob 66 Tai tím | 1-5 | 5-20 | 20-100 | 100-500 | 500-1000 |
| 55 | Hạ %1 abo | mob 67 Abo | 1-5 | 5-20 | 20-100 | 100-500 | 500-1000 |
| 56 | Hạ %1 kado | mob 68 Kado | 1-5 | 5-20 | 20-100 | 100-500 | 500-1000 |
| 57 | Hạ %1 da xanh | mob 69 Da xanh | 1-5 | 5-20 | 20-100 | 100-500 | 500-1000 |
| 58 | Nhặt %1 vàng | Nhặt item `type == 9` (vàng) — cộng theo `quantity` | 1000-3000 | 3000-20000 | 20000-100000 | 100000-10000000 | 10000000-100000000 |

---

## 3. Thành tích (Achievement)

### 3.1. Cơ chế

| Thành phần | Mô tả |
|---|---|
| `AchievementTemplate` | `info1`, `info2`, `money`, `maxCount` (nạp `select * from achievement_template`, thứ tự theo DB) |
| `AchievementQuest` | `completed` (long), `isRecieve` (bool) — mỗi phần tử ứng với index template |
| Tải | `MrBlue.java` cột `data_achievement`: mảng `[[completed, isRecieve], …]`; thiếu phần tử → `(0, false)` |
| Mở UI | Bò Mộng → "Nhiệm vụ thành tích" → `AchievementService.openAchievementUI` (Message **-76**, sub 0): mỗi dòng gửi info1, `info2 (completed/maxCount)`, money, `isFinish`, `isRecieve` |
| Nhận thưởng | `Controller` (Message -76) → `AchievementService.confirmAchievement(player, index)`: cần ≥ 1 ô trống hành trang; `achievement.reward(index)` (đặt `isRecieve = true`); **`inventory.gem += money`** ("Bạn vừa nhận được N ngọc.") |
| Cộng tiến độ | `AchievementService.checkDoneTask(player, aId)` → `achievement.done(aId, 1)`; riêng `LAN_DAU_NAP_NGOC` dùng `doneNotAdd` (gán giá trị) |
| Đọc tiến độ | `Achievement.getCompleted(index)`: index 0, 1, 16 ← `nPoint.power`; index 2 ← `magicTree.level`; index 8 trả `completed / 3.600.000` (giờ) |

Phần thưởng thành tích là **ngọc (gem)**, nhận **một lần** cho mỗi thành tích (không reset).

### 3.2. Danh sách đầy đủ

| Index | Hằng `ConstAchievement` | info1 (tên) | info2 (điều kiện hiển thị) | max_count | Thưởng (ngọc) | Cách đếm trong code |
|---|---|---|---|---|---|---|
| 0 (id DB 1) | `GIA_NHAP_VE_BINH` | Gia nhập Vệ Binh | Đạt cấp Vệ Binh | 340.000 | 10.000 | `getCompleted` gán = `nPoint.power` (sức mạnh hiện tại) |
| 1 (id DB 2) | `SUC_MANH_SIEU_CAP` | Sức mạnh siêu cấp | Đạt cấp %1 | 1.500.000 | 50 | `getCompleted` gán = `nPoint.power`; `%1` → Siêu nhân / Siêu Namếc / Siêu Xayda |
| 2 (id DB 3) | `NONG_DAN_CHAM_CHI` | Nông dân chăm chỉ | Cây đậu thần đạt cấp 5 | 5 | 20 | `getCompleted` gán = `magicTree.level` (cấp cây đậu thần) |
| 3 (id DB 4) | `TRAM_TRAN_TRAM_THANG` | Trăm trận trăm thắng | Thắng 100 người khác nhau | 100 | 20 | +1 mỗi lần thắng Thách đấu (`matches/ThachDau.java`) — không kiểm tra "người khác nhau" |
| 4 (id DB 5) | `NOI_CONG_CAO_CUONG` | Nội công cao cường | Chưởng 2.000 phát | 2.000 | 10 | +1 mỗi lần dùng Kamejoko / Masenko / Antomic (`SkillService` → `checkDoneTaskUseSkill`) |
| 5 (id DB 6) | `KHINH_CONG_THANH_THAO` | Khinh công thành thạo | Bay 20.000 mét | 20.000 | 10 | Khi di chuyển bay (`Controller`, b == 1): cộng `abs(dx / 10)` nếu giá trị < 10 (`checkDoneTaskFly`) |
| 6 (id DB 7) | `THO_SAN_THIEN_XA` | Thợ săn thiện xạ | Hạ 1.000 quái trên không | 1.000 | 10 | +1 khi hạ mob có `type == 4` (quái bay) — `checkDoneTaskKillMob` |
| 7 (id DB 8) | `TAP_LUYEN_BAI_BAN` | Tập luyện bài bản | Hạ 1.000 người rơm | 1.000 | 10 | +1 khi hạ mob `tempId == 0` (Mộc nhân) |
| 8 (id DB 9) | `HOAT_DONG_CHAM_CHI` | Hoạt động chăm chỉ | Chơi hơn 120 giờ | 120 | 20 | `Player.update` cộng 1000 (ms) mỗi lượt cập nhật; hiển thị/so sánh theo giờ (`completed / 3.600.000`) |
| 9 (id DB 10) | `HO_TRO_DONG_DOI` | Hỗ trợ đồng đội | Cho 10.000 đậu thần | 10.000 | 50 | +1 mỗi lần cho đậu thần trong bang (`ClanService`) |
| 10 (id DB 11) | `TRUM_NHAT_VE_CHAI` | Trùm nhặt ve chai | Bán cho %2 200 món đồ | 200 | 10 | +1 mỗi lần bán đồ tại shop tag `BUNMA` / `DENDE` / `APPULE` (`ShopService`); `%2` → Bunma/Dende/Appule |
| 11 (id DB 12) | `LAN_DAU_NAP_NGOC` | Lần đầu nạp ngọc | Nạp ít nhất 150 ngọc | 150 | 50 | Khi đăng nhập và `session.tongnap > 0`: đặt completed = `tongnap` (`doneNotAdd`, `Controller`) |
| 12 (id DB 13) | `DANH_BAI_SIEU_QUAI` | Đánh bại siêu quái | Hạ 100 siêu quái | 100 | 10 | +1 khi hạ mob có `lvMob > 0` (siêu quái) |
| 13 (id DB 14) | `THANH_HOI_SINH` | Thánh hồi sinh | Hồi sinh tại chỗ 200 lần | 200 | 50 | +1 mỗi lần `Service.hsChar` được gọi (hồi sinh) |
| 14 (id DB 15) | `KY_NANG_THANH_THAO` | Kỹ năng thành thạo | Dùng chiêu đặc biệt 1000 lần | 1.000 | 20 | +1 mỗi lần dùng skill **không** thuộc Kamejoko/Masenko/Antomic và không thuộc Dragon, Demon, Galick, Liên hoàn, Kaioken, Dịch chuyển tức thời |
| 15 (id DB 16) | `TRUM_NHAT_NGOC` | Trùm nhặt ngọc | Nhặt 1000 ngọc | 1.000 | 20 | +1 mỗi lần nhặt item 77 (ngọc) — tính theo lượt nhặt, không theo số lượng (`TaskService.checkDoneTaskPickItem`) |
| 16 (id DB 17) | `DAT_15_TRIEU_SUC_MANH` | Đạt 15 triệu sức mạnh | Dành cho tân thủ từ 19/7/2023 | 15.000.000 | 100 | `getCompleted` gán = `nPoint.power` |
| 17 (id DB 18) | `TUYET_KY_THANH_THAO` | Tuyệt kỹ thành thạo | Dùng tuyệt kỹ (skill thứ 9) 7749 lần | 7.749 | 50.000 | +1 khi dùng Super Kame / Liên hoàn chưởng / Ma phong ba (`SkillService`) |
| 18 (id DB 19) | `CHAM_SOC_DAC_BIET` | Chăm sóc đặc biệt | Được Namếc hồi sinh 2K lần | 2.000 | 15 | +1 khi được hồi sinh bằng Trị thương lúc đang chết (`SkillService`) |
| 19 (id DB 20) | `TRUM_KET_LIEU_BOSS` | Trùm kết liễu Boss | Đánh đòn cuối hạ Boss 2K lần | 2.000 | 20 | +1 mỗi lần hạ boss (`TaskService.checkDoneTaskKillBoss`) |

`ConstAchievement` dùng index 0-based (thứ tự nạp), còn cột `id` trong DB bắt đầu từ 1.

---

## 4. Danh hiệu (Badges)

### 4.1. Mô hình dữ liệu

| Thành phần | File | Mô tả |
|---|---|---|
| `BagesTemplate` | `player_badges/BagesTemplate.java` | Từ `data_badges`: `id`, `idEffect` (id hiệu ứng/ảnh hiển thị trên đầu), `idItem` (item đại diện trong shop), `NAME`, `options` (JSON `[{id, param}]` → `Item.ItemOption`) |
| `BadgesData` | `player_badges/BadgesData.java` | Danh hiệu người chơi sở hữu: `idBadGes` (= `idEffect`), `timeofUseBadges` (thời điểm hết hạn, ms), `isUse` (đang đeo) |
| `Badges` | `player_badges/Badges.java` | `idBadges` đang hiển thị, `lastTimeSendBadges` |
| `BadgesTaskTemplate` | `task/BadgesTaskTemplate.java` | Từ `task_badges_template`: `id`, `name`, `count` (= `maxCount`), `idbadgesReward` |
| `BadgesTask` | `task/BadgesTask.java` | Tiến độ người chơi: `id`, `count`, `countMax`, `idBadgesReward` |
| `BadgesTaskService` | `task/BadgesTaskService.java` | Tạo/reset, cộng tiến độ, tự trao danh hiệu, tính % và số ngày còn lại |
| `BadgesService` | `player_badges/BadgesService.java` | `turnOnBadges`: bật 1 danh hiệu, tắt các danh hiệu khác |

Lưu người chơi: cột `dataBadges` (JSON `{idBadGes, timeofUseBadges, isUse}`) và `dataTaskBadges` (JSON `{id, count, countMax, idBadgesReward}`), tải trong `MrBlue.java`; nếu lỗi parse `dataTaskBadges` → `createAndResetTask`.

### 4.2. Danh sách danh hiệu (`data_badges`) và chỉ số

Tên option tra theo `item_option_template` (ký hiệu `#` thay bằng param).

| id | idEffect | idItem (item_template) | Tên | Chỉ số (Options) |
|---|---|---|---|---|
| 1 | 218 | 1289 Đại gia mới nhú | Đại gia mới nhú | Sức đánh+15% (opt 50=15); Hạn sử dụng 30 ngày (opt 93=30) |
| 2 | 219 | 1290 Trùm ước rồng | Trùm ước rồng | HP+6% (opt 77=6); Hạn sử dụng 30 ngày (opt 93=30) |
| 3 | 220 | 1291 Trùm săn Boss | Trùm săn boss | Sức đánh+5% (opt 50=5); Hạn sử dụng 30 ngày (opt 93=30) |
| 4 | 221 | 1292 Thánh đập đồ +7 | Thánh đập đồ +7 | HP+10% (opt 77=10); KI +10% (opt 103=10); Hạn sử dụng 30 ngày (opt 93=30) |
| 5 | 222 | 1293 Cao thủ siêu hạng | Cao thủ siêu hạng | HP+8% (opt 77=8); Hạn sử dụng 30 ngày (opt 93=30) |
| 6 | 223 | 1294 Nông dân chăm chỉ | Nông dân chăm chỉ | HP+5% (opt 77=5); KI +5% (opt 103=5); Hạn sử dụng 30 ngày (opt 93=30) |
| 7 | 224 | 1295 Ông thần ve chai | Ông thần ve chai | 3% Né đòn (opt 108=3); Hạn sử dụng 30 ngày (opt 93=30) |
| 8 | 225 | 1296 Bị móc sạch túi | Bị móc sạch túi | 5% Né đòn (opt 108=5); HP+5% (opt 77=5); KI +5% (opt 103=5); Hạn sử dụng 30 ngày (opt 93=30) |
| 9 | 228 | 1299 Fan cứng | Fan cứng | Sức đánh+3% (opt 50=3); HP+3% (opt 77=3); KI +3% (opt 103=3); Hạn sử dụng 30 ngày (opt 93=30) |
| 12 | 242 | 1392 Gõ đầu trẻ | Gõ đầu trẻ | HP+10% (opt 77=10); KI +10% (opt 103=10); +10% sức đánh chí mạng (opt 5=10); Hạn sử dụng 30 ngày (opt 93=30) |
| 13 | 243 | 1393 Gõ đầu trẻ | Gõ đầu trẻ | HP+10% (opt 77=10); KI +10% (opt 103=10); +10% sức đánh chí mạng (opt 5=10); Hạn sử dụng 30 ngày (opt 93=30) |
| 14 | 240 | 1394 Gõ đầu trẻ | Gõ đầu trẻ | HP+10% (opt 77=10); KI +10% (opt 103=10); +10% sức đánh chí mạng (opt 5=10); Hạn sử dụng 30 ngày (opt 93=30) |
| 15 | 247 | 1457 Danh hiệu X-mas | X-mas | Sức đánh+12% (opt 50=12); HP+12% (opt 77=12); KI +12% (opt 103=12); Hạn sử dụng 30 ngày (opt 93=30) |
| 16 | 253 | 1514 Em xinh, em đẹp | Em xinh, em đẹp | Sức đánh+11% (opt 50=11); HP+11% (opt 77=11); KI +11% (opt 103=11); Đẹp +5% SĐ cho mình và người xung quanh (opt 117=5); Hạn sử dụng 30 ngày (opt 93=30) |
| 17 | 256 | 1790 Mẹ Rồng | Mẹ Rồng | Sức đánh+13% (opt 50=13); HP+13% (opt 77=13); +7% sức đánh chí mạng (opt 5=7); Hạn sử dụng 30 ngày (opt 93=30) |
| 18 | 226 | 1297 KOL | KOL | Sức đánh+10% (opt 50=10); HP+10% (opt 77=10); Sát thương chuẩn 103% (opt 10=103); Hạn sử dụng 30 ngày (opt 93=30) |

**Chỉ số thực sự được áp dụng** (`player/NPoint.java`, qua `BagesTemplate.sendListItemOption` — chỉ lấy option của các danh hiệu có `isUse == true`):

| Option | Áp dụng | Công thức |
|---|---|---|
| 50 Sức đánh+#% | Tính sát thương | `dame += dame × param / 100` |
| 77 HP+#% | Tính HP tối đa | `hpMax += hpMax × param / 100` |
| 103 KI +#% | Tính KI tối đa | `mpMax += mpMax × param / 100` |
| 108 #% Né đòn | Tính né đòn | `tlNeDon += tlNeDon × param / 100` (nhân theo tỉ lệ né hiện có, không cộng thẳng %) |
| 5, 10, 117, 93 | **Không được NPoint đọc** | option 93 chỉ dùng hiển thị hạn; 5/10/117 không có hiệu lực |

### 4.3. Nhiệm vụ danh hiệu (`task_badges_template`) — cách nhận

| Task id | Hằng `ConstTaskBadges` | Tên nhiệm vụ (DB) | maxCount | idBadgesReward (= idEffect) | Danh hiệu (`data_badges`) | Item hiển thị | Cách cộng tiến độ (code) |
|---|---|---|---|---|---|---|---|
| 1 | `DAI_GIA_MOI_NHU` | Nạp Tích luỹ 1 Triệu Trong Ngày | 1.000.000 | 218 | id 1: Đại gia mới nhú | 1289 Đại gia mới nhú | `Input` — đổi VNĐ → thỏi vàng / TRADE_GEM (nạp ngọc): cộng số tiền |
| 2 | `TRUM_UOC_RONG` | Ước Rồng Thần 1 Sao X100 Lần | 100 | 219 | id 2: Trùm ước rồng | 1290 Trùm ước rồng | `SummonDragon.activeShenron` khi rồng xuất hiện: +1 |
| 3 | `TRUM_SAN_BOSS` | Hạ Gục Cumber, Black Goku, Cooler, Xên ( 300 Lần ) | 300 | 220 | id 3: Trùm săn boss | 1291 Trùm săn Boss | Hạ boss Cumber, Black Goku, Cooler, Xên Bọ Hung, Siêu Bọ Hung, Xên con 1–7: +1 |
| 4 | `THANH_DAP_DO_7` | Đập 5 Trang Bị +7 Trong Ngày | 5 | 221 | id 4: Thánh đập đồ +7 | 1292 Thánh đập đồ +7 | `NangCapVatPham`: nâng cấp thành công khi `level == 7`: +1 |
| 5 | `CAO_THU_SIEU_HANG` | Top 1 Đại Hội Võ Đài Siêu Hạng | 1 | 222 | id 5: Cao thủ siêu hạng | 1293 Cao thủ siêu hạng | `SuperRank.reward` (mỗi ngày) nếu đang TOP 1 Siêu Hạng: +1 |
| 6 | `NONG_DAN_CHAM_CHI` | Hoàn Thành 10 Nhiệm Vụ Siêu Khó Tại Bò Mộng | 10 | 223 | id 6: Nông dân chăm chỉ | 1294 Nông dân chăm chỉ | `TaskService.paySideTask` (Bò Mộng): +3 mỗi lần trả nhiệm vụ (mọi cấp độ) |
| 7 | `KE_THAO_TUNG_SOI` | Đánh Bại, Hoặc Cho Xương Sói 20 Lần | 20 | 1286 | **không có dòng data_badges** | — | Hạ Sói Hẹc Quyn (`SoiHecQuyn`) hoặc cho xương (`UseItem`): +1 |
| 8 | `NUOC_ANH_BAO` | Hoàn Thành Nhiệm Vụ 5 Lần Cho Xinbato Nước | 5 | 1287 | **không có dòng data_badges** | — | **Không có code nào cộng** (hằng `NUOC_ANH_BAO` không được dùng) |
| 9 | `ONG_THAN_VE_CHAI` | Nhặt Đồ Trong Ngày 500 Lần | 500 | 224 | id 7: Ông thần ve chai | 1295 Ông thần ve chai | `Mob.hutItem` — mỗi lần hạ quái (không phải nhặt đồ): +1 |
| 10 | `BI_MOC_SACH_TUI` | Tiêu diệt 30 Boss Ăn Trộm | 30 | 225 | id 8: Bị móc sạch túi | 1296 Bị móc sạch túi | Hạ boss Ăn Trộm (`AnTrom.die` +1 và `reward` +1 nếu có vàng) |
| 11 | `O_DO` | Tiêu Diệt 30 Boss Ở Dơ | 30 | 1300 | **không có dòng data_badges** | — | Hạ boss Ở Dơ (`Odo`): +1 |
| 12 | `GO_DAU_TRE` | Mở Rương Gỗ Cấp 12 | 10 | 240 | id 14: Gõ đầu trẻ | 1394 Gõ đầu trẻ | `UseItem.openRuongGo` (mở Rương Gỗ, mọi cấp): +1 cho cả 3 task 12/13/14 |
| 13 | `GO_DAU_TRE1` | Mở Rương Gỗ Cấp 12 | 20 | 242 | id 12: Gõ đầu trẻ | 1392 Gõ đầu trẻ | như task 12 |
| 14 | `GO_DAU_TRE2` | Mở Rương Gỗ Cấp 12 | 30 | 243 | id 13: Gõ đầu trẻ | 1393 Gõ đầu trẻ | như task 12 |
| 15 | `XSMAX` | Đạt 500 Điểm Sự Kiện | 500 | 247 | id 15: X-mas | 1457 Danh hiệu X-mas | Dùng Pháo bông / Pháo bông VIP / Kem trái cây / Quà thiếu nhi (`UseItem`): +1 |
| 16 | `EM_XINH_EM_DEP` | Nạp tích lũy 2 triệu trong ngày | 2.000.000 | 253 | id 16: Em xinh, em đẹp | 1514 Em xinh, em đẹp | như task 1 (cộng số tiền) |
| 17 | `ME_RONG` | Sở hữu 7 rồng nhí vĩnh viễn | 7 | 256 | id 17: Mẹ Rồng | 1790 Mẹ Rồng | Thêm Bé Rồng Cute (item 1765–1771) không có option hạn sử dụng vào túi (`InventoryService.addItemBag`) hoặc `Inventory.checkAndUpdateMeRongBadges`: +1 |
| 18 | `KOL` | Fan cứng KOL | 1 | 226 | id 18: KOL | 1297 KOL | Hạ boss Mặt Trời (`MatTroi.reward`): +1 |

### 4.4. Vòng đời tiến độ & nhận danh hiệu

1. **Tạo / reset hằng ngày**: `BadgesTaskService.createAndResetTask` xóa sạch `dataTaskBadges` và tạo lại từ `TASKS_BADGES_TEMPLATE` với `count = 0`. Được gọi trong `PlayerService.dailyLogin` khi sang ngày mới (so `firstTimeLogin`) → **mọi tiến độ danh hiệu reset mỗi ngày** (khớp các tên "… Trong Ngày").
2. **Cộng tiến độ**: `updateCountBagesTask(player, id, amount)` — cộng dồn, chặn ở `countMax`.
3. **Tự trao**: `updateDoneTask(player)` được `Player.update` gọi định kỳ. Với mỗi task đã đủ:
   - Nếu người chơi **đã sở hữu** danh hiệu `idBadgesReward` → `return` (thoát hàm).
   - Ngược lại: `new BadgesData(player, idBadgesReward, 30)` (hạn **30 ngày**, tự đặt `isUse = true` và tắt `isUse` của mọi danh hiệu khác), thêm vào `dataBadges`, đặt `count = 0`.
4. **Nhận qua shop**: NPC **Santa** (`npc_template` 39, map 5/13/20) → shop tag `SANTA_DANH_HIEU` (shop id 26):
   - Tab **44 "Danh Hiệu"** (`TabShopDanhHieu`): liệt kê các item danh hiệu chưa sở hữu, gắn option 220 "Hoàn thành #%" = `sendPercenBadgesTask`. Mua → `ShopService.buyDanhHieu`: yêu cầu 100%, chưa sở hữu; tạo `BadgesData(..., 30)`. **Không trừ tiền** (hàm return trước bước thanh toán).
   - Tab **45 "Sở Hữu"** (`TabShopSoHuu`): liệt kê danh hiệu đã có, option 93 hiển thị số ngày còn lại (`BadgesTaskService.sendDay`). Chọn → `ShopService.changeDanhHieu`: đổi danh hiệu đang đeo (`BadgesService.turnOnBadges`), cooldown 3 giây, tính lại chỉ số.
   - Item bày bán tab 44/45 (`item_shop`): 1286, 1287, 1289–1297, 1299, 1300, 1392–1394, 1457, 1514, 1790.
5. **Hiển thị & hết hạn**: `Player.autoSendBadges` xóa các `BadgesData` quá `timeofUseBadges`; với danh hiệu `isUse` đặt `badges.idBadges = idBadGes`; mỗi 10 giây gửi `Service.sendBadgesPlayer` (Message **24**, sub 2) cho cả map và `nPoint.update()`.

---

## 5. NPC giao nhiệm vụ khác

| NPC | Map | Nội dung liên quan nhiệm vụ | File |
|---|---|---|---|
| Bò Mộng (17) | 47 Rừng Karin, 84 Siêu Thị | Nhiệm vụ hàng ngày, thành tích, nạp ngọc, điểm danh | `npc_list/BoMong.java` |
| Dr. Brief (10) | 153 Lãnh địa Bang Hội | Nhiệm vụ bang | `npc_list/DrDrief.java` |
| Santa (39) | 5, 13, 20 | Shop danh hiệu (nhận / đổi danh hiệu) | `npc_list/Santa.java` |
| Quy Lão Kame (13) / Trưởng lão Guru (14) / Vua Vegeta (15) | 5 / 13 / 20 | Menu "Nhiệm vụ" hiển thị bước nhiệm vụ chính hiện tại | `npc_list/QuyLaoKame.java`, `TruongLaoGuru.java`, `VuaVegeta.java` |
| Thần mèo Karin (18) | 46 Tháp Karin | Có nút "Nhiệm vụ" nhưng chỉ `npcChat("...")`; nói chuyện hoàn thành TASK_27_0 của nhiệm vụ chính | `npc_list/Karin.java` |
| Ôsin (44) | 52 Đại hội võ thuật … | Nhiệm vụ chính 28 | `npc_list/Osin.java` |
| Xinbato | — | `task_badges_template` id 8 nhắc "Hoàn Thành Nhiệm Vụ 5 Lần Cho Xinbato Nước" nhưng **không có code nhiệm vụ Xinbato** nào cộng tiến độ | — |

Chi tiết nhiệm vụ chính: xem `docs/12-nhiem-vu-chinh.md`.

---

## 6. Ghi chú / điểm cần lưu ý

**Nhiệm vụ hàng ngày**

1. Menu Bò Mộng chỉ có Dễ/Bình thường/Khó → cấp **Rất khó** và **Địa ngục** (và phần thưởng Bí ngô 4/3 sao, Cây thông) **không thể nhận** qua gameplay; tên danh hiệu "Hoàn Thành 10 Nhiệm Vụ Siêu Khó Tại Bò Mộng" vì vậy không khớp — code đếm mọi cấp.
2. `GOLD_HELL = 25` vàng (nhiều khả năng là lỗi nhập, nhỏ hơn cả cấp Dễ).
3. `canNhanCayThong` là field **instance của singleton** `TaskService`, không phải theo người chơi → cả server chỉ 1 người nhận được Cây thông mỗi lần khởi động. Điều kiện `leftTask < 15` luôn đúng.
4. Mẫu nhiệm vụ chọn ngẫu nhiên trong toàn bộ 59 mẫu, **không lọc theo sức mạnh/hành tinh** → có thể nhận "Tiêu diệt xên con cấp 8" khi chưa tới map đó; cách duy nhất là hủy (mất lượt).
5. Hủy nhiệm vụ không có xác nhận và không hoàn lượt; lượt bị trừ ngay khi nhận.
6. Mẫu id 58 "Nhặt %1 vàng" cộng theo `quantity` với mọi item `type == 9`; Lv4–Lv5 lên tới 10.000.000–100.000.000 và `count` là `int`.
7. Cần **≥ 2 ô trống** để trả (điều kiện `> 1`), thông báo lỗi không nói rõ số ô.
8. Tiến độ danh hiệu "Nông dân chăm chỉ" bị cộng **3 lần**/lần trả (switch cấp độ + nhánh trao item — Cây thông hoặc Bí ngô + sau khi cộng vàng) → chỉ cần 4 lần trả là đạt mốc 10.

**Nhiệm vụ bang**

9. Cấp độ random 0–4 (có cả "địa ngục" 2000–5000 con), người chơi không chọn được; muốn đổi phải hủy và mất 1 lượt. Khác nhiệm vụ Bò Mộng, lượt bang chỉ bị trừ khi **trả hoặc hủy**, không trừ khi nhận.
10. `payClanTask` vẫn trừ lượt và báo "nhận capsule" kể cả khi người chơi đã rời bang (`clan == null`) → capsule bị mất.
11. Vòng lặp gửi thông tin bang sau khi trả dùng `ClanService.sendMyClan(player)` thay vì `pl` → các thành viên khác đang online không được cập nhật (chỉ người trả nhận lại nhiều lần).

**Thành tích**

12. `confirmAchievement` **không kiểm tra** `canReward` / `isRecieve` ở server → một client sửa gói tin có thể nhận ngọc nhiều lần hoặc khi chưa đạt.
13. `TRAM_TRAN_TRAM_THANG` mô tả "100 người khác nhau" nhưng code +1 mỗi trận thắng thách đấu.
14. `KHINH_CONG_THANH_THAO`: chỉ cộng khi `abs(dx/10) < 10` (bước di chuyển < 100px), bước dài không được tính.
15. `HOAT_DONG_CHAM_CHI` cộng 1000 mỗi lượt `Player.update` và quy đổi `/3.600.000` → chỉ đúng "giờ chơi" nếu vòng update chạy đúng 1 giây/lần.
16. `TRUM_NHAT_NGOC` đếm theo lượt nhặt item 77, không theo số ngọc.
17. Thành tích index 0 "Gia nhập Vệ Binh" thưởng **10.000 ngọc**, index 17 "Tuyệt kỹ thành thạo" thưởng **50.000 ngọc** — trường `money` gửi client dạng `writeShort` (tối đa 32.767) nên 50.000 sẽ hiển thị sai trên UI (server vẫn cộng đủ `int`).

**Danh hiệu**

18. **Nhân đôi danh hiệu**: constructor `BadgesData(Player, id, days)` đã tự `player.dataBadges.add(this)`, nhưng `BadgesTaskService.updateDoneTask` và `ShopService.buyDanhHieu` lại `add` thêm lần nữa → cùng một object xuất hiện 2 lần, `sendListItemOption` cộng option 2 lần → **chỉ số danh hiệu bị nhân đôi**.
19. `updateDoneTask` dùng `return` khi gặp danh hiệu đã sở hữu → các task danh hiệu khác đứng sau trong danh sách cũng không được xét trong lượt đó.
20. `updateDoneTask` trao ngay khi đủ nên nút mua ở tab 44 hầu như chỉ dùng khi bị kẹt ở ghi chú 19; sau khi trao `count = 0` → % hiển thị quay về 0.
21. Task danh hiệu 7 (1286 Kẻ thao túng sói), 8 (1287 Nước anh bao), 11 (1300 Thánh ở dơ) trỏ tới idEffect **không có trong `data_badges`** → danh hiệu được thêm vào người chơi nhưng không có option, `listEffect` không map được item; trong shop `fineIdEffectbyIdItem` trả -1 nên % luôn 0 và không mua được.
22. Task 8 `NUOC_ANH_BAO` không có code cộng tiến độ.
23. Task 9 "Nhặt Đồ Trong Ngày 500 Lần" thực tế cộng ở `Mob.hutItem` — tức **mỗi lần hạ quái** (kể cả khi không nhặt).
24. Task 12/13/14 "Mở Rương Gỗ Cấp 12" cộng cả ba cùng lúc ở đầu `openRuongGo`, **không kiểm tra cấp rương** và cộng trước cả khi tìm thấy rương.
25. Task 15 "Đạt 500 Điểm Sự Kiện" thực tế cộng +1 mỗi lần dùng Pháo bông / Pháo bông VIP / Kem trái cây / Quà thiếu nhi, không liên quan điểm sự kiện.
26. Task 4 "Đập 5 Trang Bị +7": điều kiện là biến `level == 7` đọc **trước** khi tăng cấp, tức nâng thành công từ +7 lên +8.
27. Task 10 Ăn Trộm cộng 2 lần/boss khi boss có vàng (`die` và `reward`).
28. Task 17 "Sở hữu 7 rồng nhí vĩnh viễn" cộng mỗi lần thêm Bé Rồng Cute không hạn vào túi (có thể trùng loại), và tiến độ bị reset mỗi ngày.
29. `data_badges` id 18 KOL có option `{"param":103,"id":10}` → "Sát thương chuẩn 103%" — nhiều khả năng bị đảo (dự định `id 103 = 10` tức KI +10%); dù sao option 10 không được NPoint áp dụng.
30. Option 5 (chí mạng), 117 (Đẹp) của các danh hiệu Gõ đầu trẻ, Mẹ Rồng, Em xinh em đẹp **không có hiệu lực** vì NPoint chỉ đọc 50/77/103/108.
31. `data_badges` id 9 "Fan cứng" (idEffect 228, item 1299) có trong shop nhưng **không có task** nào trao → không thể nhận.
32. `Player.autoSendBadges` không đặt lại `badges.idBadges = -1` khi danh hiệu hết hạn/bị xóa → hiệu ứng cũ có thể vẫn được gửi cho map.
33. Tiến độ danh hiệu reset theo `dailyLogin` (khi đăng nhập ngày mới), không reset lúc 0h cho người đang online.
