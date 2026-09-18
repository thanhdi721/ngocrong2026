# 12 — Nhiệm vụ chính (Task Main)

> Tài liệu spec được tổng hợp **chỉ từ code và dữ liệu thật** của dự án:
> - Code: `SRC/src/nro/models/services/TaskService.java`, `SRC/src/nro/models/consts/ConstTask.java`, `SRC/src/nro/models/task/*.java`, cùng các nơi gọi (`Mob.java`, `Zone.java`, `NPoint.java`, `ClanService.java`, `ChangeMapService.java`, `npc_list/*`, `boss/*`…).
> - DB: `database team2026.sql` — bảng `task_main_template`, `task_sub_template`; tên map/NPC/mob/item tra theo `map_template`, `npc_template`, `mob_template`, `item_template`.
>
> Đường dẫn file Java trong tài liệu được viết tương đối từ `SRC/src/nro/models/`.

## Mục lục

1. [Tổng quan kiến trúc](#1-tổng-quan-kiến-trúc)
2. [Cấu trúc dữ liệu: taskId / index / count](#2-cấu-trúc-dữ-liệu-taskid--index--count)
3. [ConstTask chứa gì](#3-consttask-chứa-gì)
4. [Nạp, lưu và gửi nhiệm vụ](#4-nạp-lưu-và-gửi-nhiệm-vụ)
5. [Cơ chế tiến độ: `doneTask` → `addDoneSubTask` → `sendNextTaskMain`](#5-cơ-chế-tiến-độ-donetask--adddonesubtask--sendnexttaskmain)
6. [Các loại điều kiện (trigger) và nơi gọi](#6-các-loại-điều-kiện-trigger-và-nơi-gọi)
7. [Khác biệt theo hành tinh](#7-khác-biệt-theo-hành-tinh)
8. [Phần thưởng](#8-phần-thưởng)
9. [Chức năng / map được mở khóa theo nhiệm vụ](#9-chức-năng--map-được-mở-khóa-theo-nhiệm-vụ)
10. [Danh sách đầy đủ nhiệm vụ chính (0 → 29)](#10-danh-sách-đầy-đủ-nhiệm-vụ-chính-0--29)
11. [Lệnh admin liên quan](#11-lệnh-admin-liên-quan)
12. [Ghi chú / điểm cần lưu ý](#12-ghi-chú--điểm-cần-lưu-ý)

---

## 1. Tổng quan kiến trúc

| Thành phần | File | Vai trò |
|---|---|---|
| `TaskMain` | `task/TaskMain.java` | Một nhiệm vụ chính: `id`, `index` (bước con hiện tại), `name`, `detail`, `subTasks`, `lastTime` |
| `SubTaskMain` | `task/SubTaskMain.java` | Một bước con: `name`, `count`, `maxCount`, `notify`, `npcId` (byte), `mapId` (short) |
| `TaskPlayer` | `task/TaskPlayer.java` | Gói nhiệm vụ của người chơi: `taskMain`, `sideTask`, `clanTask` |
| `TaskService` | `services/TaskService.java` | Singleton `gI()`; toàn bộ logic kiểm tra/hoàn thành/thưởng |
| `ConstTask` | `consts/ConstTask.java` | Hằng số id bước (`TASK_x_y`), placeholder map/NPC/tên, cấp độ + thưởng nhiệm vụ phụ |
| `Manager.TASKS` | `server/Manager.java` | Danh sách template nhiệm vụ nạp từ DB khi khởi động |

Luồng tổng quát:

```
Sự kiện game (giết quái, nói chuyện NPC, nhặt đồ, lên SM, vào map, hạ boss…)
   └─ TaskService.checkDoneTaskXxx(player, ...)
        └─ doneTask(player, ConstTask.TASK_x_y)      // chỉ có tác dụng nếu đúng bước hiện tại
             ├─ addDoneSubTask(player, 1)             // count++ ; đủ maxCount → index++
             │     ├─ còn bước → sendNextSubTask (msg 41)
             │     └─ hết bước → sendNextTaskMain → rewardDoneTask + chuyển task id kế tiếp (msg 40)
             └─ switch(idTaskCustom): hội thoại NPC (npcSay / createTutorial), trao/thu item
```

## 2. Cấu trúc dữ liệu: taskId / index / count

### 2.1. Bảng DB

| Bảng | Cột | Ý nghĩa |
|---|---|---|
| `task_main_template` | `id`, `NAME`, `detail` | Nhiệm vụ chính; `detail` chứa placeholder `%1..%14` |
| `task_sub_template` | `task_main_id`, `NAME`, `max_count`, `notify`, `npc_id`, `map`, `ducvupro` | Bước con. `npc_id`/`map` âm là placeholder theo hành tinh. `ducvupro` là số thứ tự tăng dần (1..125) **không được code đọc** |

DB hiện có **30 nhiệm vụ chính** (id 0 → 29) và **125 bước con**.

### 2.2. Mã định danh bước (`idTaskCustom`)

`TaskService.getIdTask(Player)` và `isCurrentTask(...)` tính:

```java
(player.playerTask.taskMain.id << 10) + player.playerTask.taskMain.index << 1
```

Do toán tử `+` ưu tiên hơn `<<`, biểu thức thực chất là `((taskId << 10) + index) << 1` = `(taskId × 1024 + index) × 2`.
Ví dụ: `TASK_0_0 = 0`, `TASK_1_0 = 2048`, `TASK_29_0 = 59392` (khớp `ConstTask.java`).

Hệ quả: mã tăng đơn điệu theo (taskId, index) nên code dùng so sánh `getIdTask(player) < ConstTask.TASK_x_y` để khóa tính năng/map "cho tới khi đạt bước x_y".

`getIdTask` trả `-1` cho pet (không phải bot), boss, hoặc khi `playerTask`/`taskMain` null.

### 2.3. Tiến độ người chơi

| Trường | Ý nghĩa |
|---|---|
| `taskMain.id` | id nhiệm vụ chính hiện tại |
| `taskMain.index` | chỉ số bước con hiện tại trong `subTasks` |
| `subTasks[index].count` | số lần đã hoàn thành của bước hiện tại |
| `subTasks[index].maxCount` | lấy từ `task_sub_template.max_count` |

## 3. ConstTask chứa gì

File `consts/ConstTask.java` (6356 dòng) gồm:

| Nhóm | Hằng số | Giá trị |
|---|---|---|
| Cấp độ nhiệm vụ phụ | `EASY, NORMAL, HARD, VERY_HARD, HELL` | 0, 1, 2, 3, 4 |
| Vàng thưởng nhiệm vụ phụ | `GOLD_EASY, GOLD_NORMAL, GOLD_HARD, GOLD_VERY_HARD, GOLD_HELL` | 5.000; 20.000; 50.000; 200.000; **25** |
| Giới hạn/ngày | `MAX_SIDE_TASK` / `MAX_CLAN_TASK` | 10 / 5 |
| Placeholder map (`task_sub_template.map`) | `MAP_NHA=-2, MAP_200=-3, MAP_VACH_NUI=-4, MAP_500=-5, MAP_TTVT=-6, MAP_QUAI_BAY_600=-7, MAP_LANG=-8, MAP_QUY_LAO=-9` | xem §7 |
| Placeholder NPC (`task_sub_template.npc_id`) | `NPC_NHA=-2, NPC_TTVT=-3, NPC_SHOP_LANG=-4, NPC_QUY_LAO=-5` | xem §7 |
| Placeholder văn bản | `TEN_LANG=%1, TEN_NPC_NHA=%2, TEN_MAP_200=%3, TEN_QUAI_200=%4, TEN_VACH_NUI=%5, TEN_MAP_500=%6, TEN_NPC_TTVT=%7, TEN_NPC_SHOP_LANG=%8, TEN_QUAI_BAY_600=%9, TEN_NPC_QUY_LAO=%10, TEN_MAP_QUY_LAO=%11, TEN_QUAI_3000=%12, TEN_MAP_600=%13, TEN_QUAI_1000=%14` | xem §7 |
| Mã bước | `TASK_0_0` … `TASK_299_20` (6300 hằng) | `((x<<10)+y)<<1`, sinh sẵn cho x = 0..299, y = 0..20 |

Chỉ các hằng `TASK_0_*` → `TASK_28_*` thực sự được dùng trong code (ngoài ra `TauPayPay.java` có tham chiếu `TASK_31_1` nhưng đã bị comment).

## 4. Nạp, lưu và gửi nhiệm vụ

| Việc | Nơi xử lý | Chi tiết |
|---|---|---|
| Nạp template | `server/Manager.java` (khối `//load task`) | `SELECT ... FROM task_main_template JOIN task_sub_template ON id = task_main_id` (**không có ORDER BY**); gom bước con theo id liên tiếp |
| Tạo instance cho người chơi | `TaskService.getTaskMainById(player, id)` | Copy template (`new TaskMain(tm)`), rồi `transformName` cho `detail/name/notify`, `transformMapId`, `transformNpcId` theo hành tinh |
| Tải từ DB người chơi | `database/MrBlue.java` (cột `data_task`) | JSON `[id, index, count, lastTime]` |
| Lưu | `database/PlayerDAO.java` | ghi lại `[taskMain.id, index, subTasks[index].count, lastTime]` |
| Gửi toàn bộ nhiệm vụ | `sendTaskMain` — Message **40** | id, index, name, detail, danh sách bước (name, npcId, mapId, notify), count hiện tại, danh sách maxCount |
| Cập nhật count | `sendUpdateCountSubTask` — Message **43** | Ngoài ra được gọi định kỳ trong `Player.update` |
| Sang bước kế | `sendNextSubTask` — Message **41** | |
| Thông báo bước hiện tại | `sendInfoCurrentTask` | "Nhiệm vụ hiện tại của bạn là …" |
| Lời chào tân thủ | `server/Controller.java` | Khi đăng nhập và `getIdTask == TASK_0_0` → tutorial "Nhiệm vụ đầu tiên của bạn là di chuyển…" |
| Xem nhiệm vụ tại sư phụ | `npc_list/QuyLaoKame.java`, `TruongLaoGuru.java`, `VuaVegeta.java` | Menu "Nhiệm vụ" hiện tên bước hiện tại |

Chỉ số chỉ lưu `count` của bước hiện tại — các bước trước không cần lưu vì đã xong.

## 5. Cơ chế tiến độ: `doneTask` → `addDoneSubTask` → `sendNextTaskMain`

1. **`doneTask(player, idTaskCustom)`** (private): nếu `isCurrentTask` sai → trả `false`, không làm gì. Nếu đúng → `addDoneSubTask(player, 1)` rồi chạy `switch(idTaskCustom)` để phát hội thoại/trao-thu item (lưu ý: switch chạy **sau** khi đã tăng count/đổi bước), cuối cùng `InventoryService.sendItemBags`.
2. **`addDoneSubTask(player, n)`**: `count += n`; nếu `count >= maxCount` → `index++`; nếu `index >= subTasks.size()` → `sendNextTaskMain`, ngược lại `sendNextSubTask`. Nếu chưa đủ → `sendUpdateCountSubTask`.
3. **`sendNextTaskMain(player)`**: gọi `rewardDoneTask` cho nhiệm vụ vừa xong, sau đó chọn nhiệm vụ kế:
   - `id == 3` → `getTaskMainById(player, player.gender + 4)` (Trái Đất → 4, Namếc → 5, Xayda → 6).
   - `id ∈ {4,5,6}` → 7.
   - còn lại → `id + 1`.
   Rồi `sendTaskMain` + thông báo "Nhiệm vụ tiếp theo của bạn là …".

Các bước "đạt X sức mạnh" chỉ được kiểm tra khi `NPoint.powerUp` được gọi (tức khi SM tăng), không kiểm tra lại lúc vừa sang bước.

## 6. Các loại điều kiện (trigger) và nơi gọi

| Hàm trong `TaskService` | Loại | Được gọi từ | Ghi chú |
|---|---|---|---|
| `checkDoneTaskKillMob(player, mob)` | Đánh quái (theo `mob.tempId`) | `mob/Mob.java` khi mob chết (người đánh cuối `plAtt`) | Bỏ qua boss/bot/pet. Task 14, 15 yêu cầu `player.clan != null`, x2 tiến độ nếu ≥ 2 (`NMEMBER_DO_TASK_TOGETHER`) người cùng bang trong zone |
| `checkDoneTaskKillBoss(player, boss)` | Giết boss (theo `boss.id`, có boss xét `currentLevel`) | `boss/Boss.java` và các lớp boss riêng (Kuku, Rambo, SO1–4, TDT, Fide, Android*, Pic/Poc/KingKong, XenBoHung, XENCON1–7, SieuBoHung, Drabura*, BuiBui*, Yacon, Mabu…) | Cũng cộng thành tích `TRUM_KET_LIEU_BOSS` |
| `checkDoneTaskTalkNpc(player, npc)` | Nói chuyện NPC | `openBaseMenu` của các NPC trong `npc_list/` | Trả `true` nếu hoàn thành một bước → nhiều NPC không mở menu thường trong lần bấm đó |
| `checkDoneTaskPickItem(player, itemMap)` | Nhặt item | `map/Zone.java` (pickItem) | item 73 Đùi gà → `TASK_2_0`; 78 Đứa bé → `TASK_3_1`; 380 Viên Capsule kì bí → `TASK_27_1`; 77 → thành tích nhặt ngọc |
| `checkDoneTaskPower(player, power)` | Mốc sức mạnh | `player/NPoint.powerUp` | 16.000 → 7_0; 40.000 → 8_0 & 11_0; 200.000 → 10_0; 500.000 → 11_0; 550.000 → 11_1; 600.000 → 11_2; 600.000.000 → 20_0; 2.000.000.000 → 21_0 |
| `checkDoneTaskGoToMap(player, zone)` | Vào map | `ChangeMapService` (sau khi vào map), `PlayerService.playerMove` | 39/40/41 (x ≥ 635) → 0_0; 21/22/23 → 0_1 & 12_0; 0/7/14 → 8_0; 5/13/20 → 9_0; 19 → 17_0; 93 → 23_0; 104 → 24_0; 97 → 25_0; 100 → 26_0; 103 → 27_2 |
| `checkDoneTaskUseTiemNang(player)` | Dùng tiềm năng | `NPoint.doUseTiemNang` | → 3_0 |
| `checkDoneTaskGetItemBox(player)` | Lấy đồ từ rương | `services_func/UseItem.java` (ITEM_BOX_TO_BODY_OR_BAG) | → 0_3 |
| `checkDoneTaskConfirmMenuNpc(player, npc, select)` | Chọn menu Đậu thần | `npc_list/DauThan.java` | → 0_4 |
| `checkDoneTaskJoinClan(player)` | Vào bang | `ClanService` (khi bang có ≥ 2 thành viên) | → 13_0 |
| `checkDoneTaskFind7Stars(player)` | Tìm ngọc 7 sao | `Mob.getItemMobReward` | → 8_1 |
| `checkDoneTaskNangCS(player)` | Sức đánh gốc | `NPoint.increasePoint` (type 2) | `dameg ≥ 35.000` → 27_0 |
| `checkDoneTaskUseItem(player, item)` | Dùng item | `UseItem.java` | `switch` rỗng — hiện không có bước nào |

### 6.1. NPC → các bước hoàn thành khi nói chuyện (`checkDoneTaskTalkNpc`)

| NPC (ConstNpc / id) | Bước |
|---|---|
| `QUY_LAO_KAME` (13) | Nếu người chơi Trái Đất: 9_1, 10_2, 11_3, 12_2, 13_1, 14_3, 15_3, 16_3. **Mọi hành tinh**: 13_0, 22_2, 17_1, 18_5, 19_3, 20_6, 21_4 (nằm ngoài điều kiện gender do cách đặt ngoặc) |
| `TRUONG_LAO_GURU` (14) | Chỉ người Namếc: 9_1, 10_2, 11_3, 12_2, 13_1, 14_3, 15_3, 16_3, 13_0, 22_2, 17_1, 18_5, 19_3, 20_6, 21_4 |
| `VUA_VEGETA` (15) | Chỉ người Xayda: như Guru |
| `ONG_GOHAN` (0), `ONG_MOORI` (2), `ONG_PARAGUS` (1) | 0_2, 0_5, 1_1, 2_1, 3_2, 4_3, 5_3, 6_3, 7_3, 8_2, 11_3, 12_1, 22_0 (không kiểm tra đúng ông của hành tinh mình) |
| `DR_DRIEF` (10), `CARGO` (11), `CUI` (12) | 17_1, với điều kiện đang ở map 19 |
| `BUNMA` (7), `DENDE` (8), `APPULE` (9) | 7_2 |
| `BUNMA_TL` (37) | 22_3, 22_5, 23_4, 24_4, 25_5, 26_5, 27_5, 28_5 |
| `CALICK` (38) | 22_1 |
| `THAN_MEO_KARIN` (18) | 27_0 |
| `OSIN` (44) | 28_0, 28_7 |

## 7. Khác biệt theo hành tinh

Người chơi có `gender`: 0 = Trái Đất, 1 = Namếc, 2 = Xayda.

### 7.1. Nhánh nhiệm vụ

- Sau nhiệm vụ 3, nhiệm vụ 4/5/6 là 3 biến thể **cùng tên "Nhiệm vụ thử thách"**, chỉ khác thứ tự: hành tinh của mình đánh trước (TĐ: Khủng long mẹ → Lợn lòi mẹ → Quỷ đất mẹ; NM: Lợn lòi mẹ → Khủng long mẹ → Quỷ đất mẹ; XD: Quỷ đất mẹ → Khủng long mẹ → Lợn lòi mẹ). Tất cả hội tụ về nhiệm vụ 7.
- TASK_7_1: quái bay theo hành tinh (TĐ mob 7 Thằn lằn bay, NM mob 8 Phi long, XD mob 9 Quỷ bay).
- TASK_8_1: rơi Ngọc Rồng 7 sao từ quái mẹ theo hành tinh (TĐ mob 11 Phi long mẹ, NM mob 12 Quỷ bay mẹ, XD mob 10 Thằn lằn mẹ).
- TASK_10_2: sách kỹ năng theo hành tinh (94/101/108).

### 7.2. `transformMapId` (placeholder map)

| Placeholder | Trái Đất | Namếc | Xayda |
|---|---|---|---|
| `MAP_NHA` (-2) | 21 Nhà Gôhan | 22 Nhà Moori | 23 Nhà Broly |
| `MAP_200` (-3) | 1 Đồi hoa cúc | 8 Đồi nấm tím | 15 Đồi hoang |
| `MAP_VACH_NUI` (-4) | 39 Vách núi Aru | 40 Vách núi Moori | 41 Vực Plant |
| `MAP_TTVT` (-6) | 24 Trạm tàu vũ trụ | 25 Trạm tàu vũ trụ | 26 Trạm tàu vũ trụ |
| `MAP_QUAI_BAY_600` (-7) | 3 Rừng nấm | 11 Thung lũng Maima | 17 Rừng nguyên sinh |
| `MAP_LANG` (-8) | 0 Làng Aru | 7 Làng Mori | 14 Làng Kakarot |
| `MAP_QUY_LAO` (-9) | 5 Đảo Kamê | 13 Đảo Guru | 20 Vách núi đen |
| `MAP_500` (-5) | **không xử lý** (giữ -5) | | |

### 7.3. `transformNpcId` (placeholder NPC)

| Placeholder | Trái Đất | Namếc | Xayda |
|---|---|---|---|
| `NPC_NHA` (-2) | 0 Ông Gôhan | 2 Ông Moori | 1 Ông Paragus |
| `NPC_TTVT` (-3) | 10 Dr. Brief | 11 Cargo | 12 Cui |
| `NPC_SHOP_LANG` (-4) | 7 Bunma | 8 Dende | 9 Appule |
| `NPC_QUY_LAO` (-5) | 13 Quy Lão Kame | 14 Trưởng lão Guru | 15 Vua Vegeta |

### 7.4. `transformName` (placeholder văn bản)

| Placeholder | Trái Đất | Namếc | Xayda |
|---|---|---|---|
| `%1` TEN_LANG | Làng Aru | Làng Mori | Làng Kakarot |
| `%2` TEN_NPC_NHA | ông Gôhan | ông Moori | ông Paragus |
| `%3` TEN_MAP_200 | Đồi hoa cúc | Đồi nấm tím | Đồi hoang |
| `%4` TEN_QUAI_200 | khủng long | lợn lòi | quỷ đất |
| `%5` TEN_VACH_NUI | Vách núi Aru | Vách núi Moori | Vách núi Kakarot |
| `%6` TEN_MAP_500 | Thung lũng tre | Thị trấn Moori | Làng Plant |
| `%7` TEN_NPC_TTVT | Dr. Brief | Cargo | Cui |
| `%8` TEN_NPC_SHOP_LANG | Bunma | Dende | Appule |
| `%9` TEN_QUAI_BAY_600 | thằn lằn bay | phi long | quỷ bay |
| `%10` TEN_NPC_QUY_LAO | Quy Lão Kame | Trưởng lão Guru | Vua Vegeta |
| `%11` TEN_MAP_QUY_LAO | Đảo Kamê | Đảo Guru | Vách núi đen |
| `%12` TEN_QUAI_3000 | ốc mượn hồn | ốc sên | heo Xayda mẹ |
| `%13` TEN_MAP_600 | Rừng nấm | Thung lũng Namếc | Rừng nguyên sinh |
| `%14` TEN_QUAI_1000 | phi long mẹ | quỷ bay mẹ | thằn lằn mẹ |

Thứ tự `replaceAll` trong code là `%14, %13, %10, %11, %12` rồi mới `%1..%9`, nên `%1` không "ăn" nhầm `%10..%14`.

## 8. Phần thưởng

### 8.1. Thưởng khi hoàn thành toàn bộ một nhiệm vụ — `TaskService.rewardDoneTask`

Thưởng tính theo `taskMain.id` **vừa hoàn thành**:

1. `switch(id)`: 0 → 500; 1 → 1.000; 2 → 1.200; 3 → 3.000; 4 → 7.000; 5 → 20.000 — mỗi giá trị cộng **vào cả sức mạnh (type 0) và tiềm năng (type 1)** qua `Service.addSMTN`.
2. Nếu `0 < id < 25`: `addSMTN(type 2, 500 × (id+1))` (cộng cả SM và TN) và cộng vàng: `id < 5 ? 100.000 × (id+1) : 500.000`.

`Service.addSMTN` cắt theo giới hạn sức mạnh (`getPowerLimit`): nếu đã chạm giới hạn thì không cộng.


### 8.2. Thưởng/tác động trong từng bước (trong `switch` của `doneTask`)

| Bước | Tác động | Item (tra `item_template`) |
|---|---|---|
| TASK_2_1 | Trừ 10 Đùi gà; rơi Đùi gà nướng cho riêng người chơi (`dropItemMapForMe`) | 73 Đùi gà; 74 Đùi gà nướng |
| TASK_3_2 | Trừ 1 Đứa bé | 78 Đứa bé |
| TASK_7_2 | Nhận 75 Gói Capsule | 193 Gói 10 viên Capsule (x75) |
| TASK_10_2 | Nhận sách kỹ năng 2 theo hành tinh | 94 Sách Kamejoko lv1 (TĐ) / 101 Sách Masenko lv1 (NM) / 108 Sách Antomic lv1 (XD) |
| TASK_4_3/5_3/6_3 | Chỉ có hội thoại "Ông cho con cuốn bí kíp này…" — **không trao item nào** | — |

Các bước còn lại chỉ phát hội thoại (`npcSay`/`createTutorial`) hoặc không làm gì.

### 8.3. Bảng thưởng tổng hợp theo nhiệm vụ (theo code)

| Task | Tên (DB) | SM + TN (switch, type 0 & 1) | SM + TN (type 2 = 500×(id+1)) | Tổng SM / Tổng TN | Vàng | Nhiệm vụ kế tiếp |
|---|---|---|---|---|---|---|
| 0 | Nhiệm vụ đầu tiên | 500 | — | 500 | — | 1 |
| 1 | Nhiệm vụ tập luyện | 1.000 | 1.000 | 2.000 | 200.000 | 2 |
| 2 | Nhiệm vụ tìm thức ăn | 1.200 | 1.500 | 2.700 | 300.000 | 3 |
| 3 | Nhiệm vụ sao băng | 3.000 | 2.000 | 5.000 | 400.000 | gender + 4 (TĐ→4, NM→5, XD→6) |
| 4 | Nhiệm vụ thử thách | 7.000 | 2.500 | 9.500 | 500.000 | 7 |
| 5 | Nhiệm vụ thử thách | 20.000 | 3.000 | 23.000 | 500.000 | 7 |
| 6 | Nhiệm vụ thử thách | — | 3.500 | 3.500 | 500.000 | 7 |
| 7 | Nhiệm vụ giải cứu | — | 4.000 | 4.000 | 500.000 | 8 |
| 8 | Nhiệm vụ tìm ngọc | — | 4.500 | 4.500 | 500.000 | 9 |
| 9 | Nhiệm vụ bái sư | — | 5.000 | 5.000 | 500.000 | 10 |
| 10 | Nhiệm vụ thử sức | — | 5.500 | 5.500 | 500.000 | 11 |
| 11 | Nhiệm vụ gia tăng sức mạnh | — | 6.000 | 6.000 | 500.000 | 12 |
| 12 | Nhiệm vụ xin phép | — | 6.500 | 6.500 | 500.000 | 13 |
| 13 | Nhiệm vụ gia nhập bang hội | — | 7.000 | 7.000 | 500.000 | 14 |
| 14 | Nhiệm vụ bang hội đầu tiên | — | 7.500 | 7.500 | 500.000 | 15 |
| 15 | Nhiệm vụ bang hội thứ 2 | — | 8.000 | 8.000 | 500.000 | 16 |
| 16 | Tiêu diệt quái vật | — | 8.500 | 8.500 | 500.000 | 17 |
| 17 | Nhiệm vụ giúp đỡ Cui | — | 9.000 | 9.000 | 500.000 | 18 |
| 18 | Nhiệm vụ bất khả thi | — | 9.500 | 9.500 | 500.000 | 19 |
| 19 | Nhiệm vụ tìm diệt đệ tử | — | 10.000 | 10.000 | 500.000 | 20 |
| 20 | Nhiệm vụ Tiểu đội sát thủ | — | 10.500 | 10.500 | 500.000 | 21 |
| 21 | Nhiệm vụ chạm trán Fide đại ca | — | 11.000 | 11.000 | 500.000 | 22 |
| 22 | Chú bé đến từ tương lai | — | 11.500 | 11.500 | 500.000 | 23 |
| 23 | Chạm chán Robot sát thủ lần 1 | — | 12.000 | 12.000 | 500.000 | 24 |
| 24 | Chạm trán Robot sát thủ lần 2 | — | 12.500 | 12.500 | 500.000 | 25 |
| 25 | Chạm trán Robot sát thủ lần 3 | — | — | — | — | 26 |
| 26 | Chạm trán Xên bọ hung | — | — | — | — | 27 |
| 27 | Cuộc dạo chơi của xên | — | — | — | — | 28 |
| 28 | Cuộc đối đầu không cân sức | — | — | — | — | 29 |
| 29 | .. | — | — | — | — | — (cuối) |

> "Tổng SM / Tổng TN" = mỗi con số được cộng vào **cả** sức mạnh lẫn tiềm năng. Không có ngọc (gem) nào được thưởng từ nhiệm vụ chính trong code.
> Phần "Thưởng …" ghi trong cột `detail` của DB (vd. "Thưởng 200tr vàng", "Thưởng 50.000.000 sức mạnh") **không khớp** với code — client chỉ hiển thị văn bản, server thưởng theo bảng trên.

## 9. Chức năng / map được mở khóa theo nhiệm vụ

### 9.1. Khóa map — `map/service/ChangeMapService.checkMapCanJoin`

Admin, pet, boss bỏ qua kiểm tra. Nếu `getIdTask(player) <` ngưỡng thì không vào được map (trả `null`).

| Map bị khóa | Mở khi `getIdTask` ≥ |
|---|---|
| 1 Đồi hoa cúc, 8 Đồi nấm tím, 15 Đồi hoang | `TASK_1_0` |
| 42 Vách núi Aru, 43 Vách núi Moori, 44 Vách núi Kakarot | `TASK_2_0` |
| 2 Thung lũng tre, 9 Thị trấn Moori, 16 Làng Plant | `TASK_3_0` |
| 24 Trạm tàu vũ trụ, 25 Trạm tàu vũ trụ, 26 Trạm tàu vũ trụ | `TASK_4_0` |
| 3 Rừng nấm, 11 Thung lũng Maima, 17 Rừng nguyên sinh | `TASK_7_0` |
| 27 Rừng Bamboo, 28 Rừng dương xỉ, 31 Núi hoa vàng, 32 Núi hoa tím, 35 Rừng cọ, 36 Rừng đá | `TASK_13_0` |
| 30 Đảo Bulông, 34 Đông Nam Guru, 38 Bờ vực đen | `TASK_15_0` |
| 6 Đông Karin, 10 Thung lũng Namếc, 19 Thành phố Vegeta | `TASK_16_0` |
| 68 Thung lũng Nappa, 69 Vực cấm, 70 Núi Appule, 71 Căn cứ Raspberry, 72 Thung lũng Raspberry, 64 Núi dây leo, 65 Núi cây quỷ | `TASK_18_0` |
| 63 Trại lính Fide, 66 Trại qủy già, 67 Vực chết, 73 Thung lũng chết, 74 Đồi cây Fide, 75 Khe núi tử thần, 76 Núi đá, 77 Rừng đá, 81 Hang quỷ chim, 82 Núi khỉ đen, 83 Hang khỉ đen, 79 Núi khỉ đỏ | `TASK_19_0` |
| 80 Núi khỉ vàng | `TASK_20_0` (thiếu `break` nên còn rơi xuống kiểm tra `TASK_21_0`) |
| 102 Nhà Bunma, 92 Thành phố phía đông, 93 Thành phố phía nam, 94 Đảo Balê, 96 Cao nguyên | `TASK_21_0` |
| 97 Thành phố phía bắc, 98 Ngọn núi phía bắc, 99 Thung lũng phía bắc, 100 Thị trấn Ginder | `TASK_24_0` |
| 105 Cánh đồng tuyết, 106 Rừng tuyết, 107 Núi tuyết, 108 Dòng sông băng, 109 Rừng băng, 110 Hang băng, 103 Võ đài Xên bọ hung, 154 Hành tinh Bill | `TASK_27_0` |

Ngoài ra (cùng hàm, không phụ thuộc nhiệm vụ): map 111 Đông Nam Karin chặn người có sức mạnh > 1.500.000; map nhà 21/22/23 chỉ vào được nhà đúng hành tinh.

### 9.2. Tính năng / NPC / item phụ thuộc nhiệm vụ

| Tính năng | Điều kiện | File / hàm |
|---|---|---|
| Mời / đồng ý vào bang hội | `getIdTask ≥ TASK_10_0` (nếu chưa: "chưa thể vào bang lúc này") | `services/ClanService.sendInviteClan`, `acceptJoinClan` |
| NPC Ca Lích hiển thị trong map | `getIdTask ≥ TASK_21_0` | `map/service/NpcManager.getNpcsByMapPlayer` |
| Mở menu Ca Lích / "Đi đến Tương lai" | `getIdTask ≥ TASK_20_0` | `npc_list/Calick.openBaseMenu`, `confirmMenu` |
| Item 78 Đứa bé hiển thị trên map | chỉ khi `getIdTask == TASK_3_1` | `map/Zone.getItemMapsForPlayer` |
| Item 74 Đùi gà nướng hiển thị trên map | `getIdTask ≥ TASK_3_0` | `map/Zone.getItemMapsForPlayer` |
| Rơi Đùi gà (73) từ Khủng long/Lợn lòi/Quỷ đất | `getIdTask == TASK_2_0` | `mob/Mob.dropItemTask` |
| Rơi Ngọc Rồng 7 sao (20) | `getIdTask == TASK_8_1` | `mob/Mob.dropItemTask`, `getItemMobReward` |
| Túi lưng (flagBag) 28 | `getIdTask == TASK_3_2` | `player/Player.getFlagBag` |
| Tàu Pảy Pảy nhận sát thương thật | chỉ khi `getIdTask == TASK_10_1`, ngược lại mỗi đòn chỉ trừ 100 | `boss/luyen_tap_tu_dong/TauPayPay.injured` |
| Dr. Brief / Cargo / Cui không mở menu tàu vũ trụ | đang ở nhiệm vụ 7 → thoại "Hãy lên đường cứu đứa bé nhà tôi" | `npc_list/DrDrief`, `Cargo`, `Cui` |
| Cui (map 19) dịch chuyển tới boss Kuku / Mập đầu đinh / Rambo (50.000.000 vàng) | đang ở TASK_19_0 / 19_1 / 19_2 | `npc_list/Cui` |

Chuỗi mô tả nhiệm vụ 2 trong DB ghi "Học được kỹ năng bay", nhưng server **không có** code mở kỹ năng bay theo nhiệm vụ.

## 10. Danh sách đầy đủ nhiệm vụ chính (0 → 29)

Cột "NPC"/"Map" là giá trị gửi cho client (từ DB, sau transform theo hành tinh). Cột "Điều kiện hoàn thành thực tế" là điều kiện server kiểm tra (từ `TaskService`); nơi nào DB và code lệch nhau đều được ghi rõ.

### Nhiệm vụ 0 — Nhiệm vụ đầu tiên

Mô tả (cột `detail`, placeholder `%n` được `transformName` thay theo hành tinh):

> Chi tiết nhiệm vụ

| Index | Hằng số ConstTask | Tên bước (DB) | max_count | NPC (DB `npc_id`) | Map (DB `map`) | Điều kiện hoàn thành thực tế (code) |
|---|---|---|---|---|---|---|
| 0 | `TASK_0_0 = 0` | Di chuyển tới mũi tên chỉ dẫn | 1 | — | — | Vào map 39/40/41 và tọa độ x ≥ 635 — `checkDoneTaskGoToMap` |
| 1 | `TASK_0_1 = 2` | Hãy đi đến nhà %2 ở bên phải | 1 | NPC_NHA (0 Ông Gôhan / 2 Ông Moori / 1 Ông Paragus) | Nhà (21 Nhà Gôhan / 22 Nhà Moori / 23 Nhà Broly) | Vào map nhà 21/22/23 — `checkDoneTaskGoToMap` (ngoài ra `Player.update` tự đặt index=2 nếu đang ở nhà khi còn TASK_0_0/0_1) |
| 2 | `TASK_0_2 = 4` | Nói chuyện với %2 | 1 | NPC_NHA (0 Ông Gôhan / 2 Ông Moori / 1 Ông Paragus) | Nhà (21 Nhà Gôhan / 22 Nhà Moori / 23 Nhà Broly) | Nói chuyện ông nhà (Gôhan/Moori/Paragus) — `checkDoneTaskTalkNpc` |
| 3 | `TASK_0_3 = 6` | Mở rương đồ | 1 | 3 Rương đồ | Nhà (21 Nhà Gôhan / 22 Nhà Moori / 23 Nhà Broly) | Lấy vật phẩm từ rương đồ (ITEM_BOX_TO_BODY_OR_BAG) — `UseItem` → `checkDoneTaskGetItemBox` |
| 4 | `TASK_0_4 = 8` | Thu hoạch đậu thần | 1 | 4 Đậu thần | Nhà (21 Nhà Gôhan / 22 Nhà Moori / 23 Nhà Broly) | Chọn “thu hoạch” (select 0) ở menu Đậu thần khi cây ở trạng thái `MAGIC_TREE_NON_UPGRADE_LEFT_PEA/FULL_PEA` — `checkDoneTaskConfirmMenuNpc` |
| 5 | `TASK_0_5 = 10` | Báo cáo với %2 | 1 | NPC_NHA (0 Ông Gôhan / 2 Ông Moori / 1 Ông Paragus) | Nhà (21 Nhà Gôhan / 22 Nhà Moori / 23 Nhà Broly) | Nói chuyện ông nhà |

**Thưởng khi xong cả nhiệm vụ (`rewardDoneTask`)**: +500 sức mạnh, +500 tiềm năng

### Nhiệm vụ 1 — Nhiệm vụ tập luyện

Mô tả (cột `detail`, placeholder `%n` được `transformName` thay theo hành tinh):

> Mộc nhân được đặt nhiều tại %1, ngay trước nhà %2  
> Hãy đánh ngã 5 mộc nhân,   
> sau đó quay về nhà báo cáo với ông %2  
> Để đánh, hãy chạm nhanh 2 lần vào đối tượng  
> Thưởng 1000 sức mạnh  
> Thưởng 1000 tiềm năng  
> Thưởng 200tr vàng

| Index | Hằng số ConstTask | Tên bước (DB) | max_count | NPC (DB `npc_id`) | Map (DB `map`) | Điều kiện hoàn thành thực tế (code) |
|---|---|---|---|---|---|---|
| 0 | `TASK_1_0 = 2048` | Đánh ngã 5 mộc nhân | 5 | — | — | Hạ mob 0 Mộc nhân — `checkDoneTaskKillMob` |
| 1 | `TASK_1_1 = 2050` | Báo cáo với %2 | 1 | NPC_NHA (0 Ông Gôhan / 2 Ông Moori / 1 Ông Paragus) | Nhà (21 Nhà Gôhan / 22 Nhà Moori / 23 Nhà Broly) | Nói chuyện ông nhà |

**Thưởng khi xong cả nhiệm vụ (`rewardDoneTask`)**: +2.000 sức mạnh, +2.000 tiềm năng; +200.000 vàng

### Nhiệm vụ 2 — Nhiệm vụ tìm thức ăn

Mô tả (cột `detail`, placeholder `%n` được `transformName` thay theo hành tinh):

> Tìm đến %3, tiêu diệt bọn quái %4 và nhặt về 10 đùi gà  
> Thưởng 1500 sức mạnh  
> Thưởng 1500 tiềm năng  
> Thưởng 300tr vàng  
> Học được kỹ năng bay

| Index | Hằng số ConstTask | Tên bước (DB) | max_count | NPC (DB `npc_id`) | Map (DB `map`) | Điều kiện hoàn thành thực tế (code) |
|---|---|---|---|---|---|---|
| 0 | `TASK_2_0 = 4096` | Thu thập 10 đùi gà | 10 | — | MAP_200 (1 Đồi hoa cúc / 8 Đồi nấm tím / 15 Đồi hoang) | Nhặt item 73 Đùi gà — `checkDoneTaskPickItem`. Đùi gà chỉ rơi (cho riêng người chơi) khi hạ mob 1 Khủng long / 2 Lợn lòi / 3 Quỷ đất lúc đang ở TASK_2_0 (`Mob.dropItemTask`) |
| 1 | `TASK_2_1 = 4098` | Báo cáo với %2 | 1 | NPC_NHA (0 Ông Gôhan / 2 Ông Moori / 1 Ông Paragus) | Nhà (21 Nhà Gôhan / 22 Nhà Moori / 23 Nhà Broly) | Nói chuyện ông nhà → trừ 10 Đùi gà (73), rơi item 74 Đùi gà nướng cho riêng mình |

**Thưởng khi xong cả nhiệm vụ (`rewardDoneTask`)**: +2.700 sức mạnh, +2.700 tiềm năng; +300.000 vàng

### Nhiệm vụ 3 — Nhiệm vụ sao băng

Mô tả (cột `detail`, placeholder `%n` được `transformName` thay theo hành tinh):

> Đi khám phá xem vật thể lạ vừa rơi xuống hành tinh  
> Thưởng 2000 sức mạnh  
> Thưởng 2000 tiềm năng  
> Thưởng 400tr vàng

| Index | Hằng số ConstTask | Tên bước (DB) | max_count | NPC (DB `npc_id`) | Map (DB `map`) | Điều kiện hoàn thành thực tế (code) |
|---|---|---|---|---|---|---|
| 0 | `TASK_3_0 = 6144` | Sử dụng tiềm năng | 1 | — | — | Cộng điểm tiềm năng thành công (`NPoint.doUseTiemNang`) — `checkDoneTaskUseTiemNang` |
| 1 | `TASK_3_1 = 6146` | Đi khám phá vật thể lạ | 1 | — | MAP_VACH_NUI (39 Vách núi Aru / 40 Vách núi Moori / 41 Vực Plant) | Nhặt item 78 Đứa bé (item chỉ hiển thị cho người đang ở TASK_3_1 — `Zone.getItemMapsForPlayer`) |
| 2 | `TASK_3_2 = 6148` | Báo cáo với %2 | 1 | NPC_NHA (0 Ông Gôhan / 2 Ông Moori / 1 Ông Paragus) | Nhà (21 Nhà Gôhan / 22 Nhà Moori / 23 Nhà Broly) | Nói chuyện ông nhà → trừ 1 item 78; trong lúc ở bước này flagBag = 28 (`Player.getFlagBag`) |

**Thưởng khi xong cả nhiệm vụ (`rewardDoneTask`)**: +5.000 sức mạnh, +5.000 tiềm năng; +400.000 vàng

### Nhiệm vụ 4 — Nhiệm vụ thử thách

Mô tả (cột `detail`, placeholder `%n` được `transformName` thay theo hành tinh):

> Khủng long mẹ sống tại Trái Đất  
> Lợn lòi mẹ sống tại Namếc  
> Quỷ đất mẹ sống tại Xayda  
> Dùng tàu vũ trụ để di chuyển sang hành  
> khác  
> -Thưởng 4.000 sức mạnh  
> -Thưởng 4.000 tiềm năng

| Index | Hằng số ConstTask | Tên bước (DB) | max_count | NPC (DB `npc_id`) | Map (DB `map`) | Điều kiện hoàn thành thực tế (code) |
|---|---|---|---|---|---|---|
| 0 | `TASK_4_0 = 8192` | Đánh 3 con khủng long mẹ | 3 | — | MAP_500 (-5, **không được transform**) | Hạ mob 4 Khủng long mẹ |
| 1 | `TASK_4_1 = 8194` | Đánh 3 con lợn lòi mẹ | 3 | — | MAP_500 (-5, **không được transform**) | Hạ mob 5 Lợn lòi mẹ |
| 2 | `TASK_4_2 = 8196` | Đánh 3 con quỷ đất mẹ | 3 | — | MAP_500 (-5, **không được transform**) | Hạ mob 6 Quỷ đất mẹ |
| 3 | `TASK_4_3 = 8198` | Báo cáo với %2 | 1 | NPC_NHA (0 Ông Gôhan / 2 Ông Moori / 1 Ông Paragus) | Nhà (21 Nhà Gôhan / 22 Nhà Moori / 23 Nhà Broly) | Nói chuyện ông nhà |

**Thưởng khi xong cả nhiệm vụ (`rewardDoneTask`)**: +9.500 sức mạnh, +9.500 tiềm năng; +500.000 vàng

### Nhiệm vụ 5 — Nhiệm vụ thử thách

Mô tả (cột `detail`, placeholder `%n` được `transformName` thay theo hành tinh):

> Lợn lòi mẹ sống tại Namếc  
> Khủng long mẹ sống tại Trái Đất  
> Quỷ đất mẹ sống tại Xayda  
> Dùng tàu vũ trụ để di chuyển sang hành  
> khác  
> -Thưởng 4.000 sức mạnh  
> -Thưởng 4.000 tiềm năng

| Index | Hằng số ConstTask | Tên bước (DB) | max_count | NPC (DB `npc_id`) | Map (DB `map`) | Điều kiện hoàn thành thực tế (code) |
|---|---|---|---|---|---|---|
| 0 | `TASK_5_0 = 10240` | Đánh 3 con lợn lòi mẹ | 3 | — | MAP_500 (-5, **không được transform**) | Hạ mob 5 Lợn lòi mẹ |
| 1 | `TASK_5_1 = 10242` | Đánh 3 con khủng long mẹ | 3 | — | MAP_500 (-5, **không được transform**) | Hạ mob 4 Khủng long mẹ |
| 2 | `TASK_5_2 = 10244` | Đánh 3 con quỷ đất mẹ | 3 | — | MAP_500 (-5, **không được transform**) | Hạ mob 6 Quỷ đất mẹ |
| 3 | `TASK_5_3 = 10246` | Báo cáo với %2 | 1 | NPC_NHA (0 Ông Gôhan / 2 Ông Moori / 1 Ông Paragus) | Nhà (21 Nhà Gôhan / 22 Nhà Moori / 23 Nhà Broly) | Nói chuyện ông nhà |

**Thưởng khi xong cả nhiệm vụ (`rewardDoneTask`)**: +23.000 sức mạnh, +23.000 tiềm năng; +500.000 vàng

### Nhiệm vụ 6 — Nhiệm vụ thử thách

Mô tả (cột `detail`, placeholder `%n` được `transformName` thay theo hành tinh):

> Quỷ đất mẹ sống tại Xayda  
> Khủng long mẹ sống tại Trái Đất  
> Lợn lòi mẹ sống tại Namếc  
> Dùng tàu vũ trụ để di chuyển sang hành  
> khác  
> -Thưởng 4.000 sức mạnh  
> -Thưởng 4.000 tiềm năng

| Index | Hằng số ConstTask | Tên bước (DB) | max_count | NPC (DB `npc_id`) | Map (DB `map`) | Điều kiện hoàn thành thực tế (code) |
|---|---|---|---|---|---|---|
| 0 | `TASK_6_0 = 12288` | Đánh 3 con quỷ đất mẹ | 3 | — | MAP_500 (-5, **không được transform**) | Hạ mob 6 Quỷ đất mẹ |
| 1 | `TASK_6_1 = 12290` | Đánh 3 con khủng long mẹ | 3 | — | MAP_500 (-5, **không được transform**) | Hạ mob 4 Khủng long mẹ |
| 2 | `TASK_6_2 = 12292` | Đánh 3 con lợn lòi mẹ | 3 | — | MAP_500 (-5, **không được transform**) | Hạ mob 5 Lợn lòi mẹ |
| 3 | `TASK_6_3 = 12294` | Báo cáo với %2 | 1 | NPC_NHA (0 Ông Gôhan / 2 Ông Moori / 1 Ông Paragus) | Nhà (21 Nhà Gôhan / 22 Nhà Moori / 23 Nhà Broly) | Nói chuyện ông nhà |

**Thưởng khi xong cả nhiệm vụ (`rewardDoneTask`)**: +3.500 sức mạnh, +3.500 tiềm năng; +500.000 vàng

### Nhiệm vụ 7 — Nhiệm vụ giải cứu

Mô tả (cột `detail`, placeholder `%n` được `transformName` thay theo hành tinh):

> Đến khu vực %13,  
> Hạ 20 con %9  
> - Thưởng 8.000 sức mạnh  
> - Thưởng 8.000 tiềm năng

| Index | Hằng số ConstTask | Tên bước (DB) | max_count | NPC (DB `npc_id`) | Map (DB `map`) | Điều kiện hoàn thành thực tế (code) |
|---|---|---|---|---|---|---|
| 0 | `TASK_7_0 = 14336` | Đạt 16.000 sức mạnh | 1 | — | — | Sức mạnh ≥ 16.000 — `checkDoneTaskPower` |
| 1 | `TASK_7_1 = 14338` | Đánh bại 20 con %9 | 20 | — | MAP_QUAI_BAY_600 (3 Rừng nấm / 11 Thung lũng Maima / 17 Rừng nguyên sinh) | Hạ quái bay theo hành tinh: Trái Đất mob 7 Thằn lằn bay, Namếc mob 8 Phi long, Xayda mob 9 Quỷ bay |
| 2 | `TASK_7_2 = 14340` | Nói chuyện với %8 | 1 | NPC_SHOP_LANG (7 Bunma / 8 Dende / 9 Appule) | MAP_LANG (0 Làng Aru / 7 Làng Mori / 14 Làng Kakarot) | Nói chuyện NPC shop làng (Bunma/Dende/Appule) → nhận item 193 “Gói 10 viên Capsule” x75 |
| 3 | `TASK_7_3 = 14342` | Báo cáo với %2 | 1 | NPC_NHA (0 Ông Gôhan / 2 Ông Moori / 1 Ông Paragus) | Nhà (21 Nhà Gôhan / 22 Nhà Moori / 23 Nhà Broly) | Nói chuyện ông nhà |

**Thưởng khi xong cả nhiệm vụ (`rewardDoneTask`)**: +4.000 sức mạnh, +4.000 tiềm năng; +500.000 vàng

### Nhiệm vụ 8 — Nhiệm vụ tìm ngọc

Mô tả (cột `detail`, placeholder `%n` được `transformName` thay theo hành tinh):

> Ngọc rồng 7 sao đang bị bọn  
> %14 cướp đi.  
> Đánh bại chúng để tìm lại.  
> - Thưởng 15.000 sức mạnh  
> - Thưởng 15.000 tiềm năng

| Index | Hằng số ConstTask | Tên bước (DB) | max_count | NPC (DB `npc_id`) | Map (DB `map`) | Điều kiện hoàn thành thực tế (code) |
|---|---|---|---|---|---|---|
| 0 | `TASK_8_0 = 16384` | Đạt 40.000 sức mạnh | 1 | — | — | Sức mạnh ≥ 40.000 **hoặc** vào map làng 0/7/14 (`checkDoneTaskGoToMap`) |
| 1 | `TASK_8_1 = 16386` | Tìm viên ngọc rồng 7 sao | 1 | — | MAP_200 (1 Đồi hoa cúc / 8 Đồi nấm tím / 15 Đồi hoang) | Hạ quái mẹ theo hành tinh (TĐ mob 11 Phi long mẹ / NM mob 12 Quỷ bay mẹ / XD mob 10 Thằn lằn mẹ) → rơi item 20 Ngọc Rồng 7 sao và tự hoàn thành (`Mob.getItemMobReward` → `checkDoneTaskFind7Stars`) |
| 2 | `TASK_8_2 = 16388` | Đem ngọc về cho %2 | 1 | NPC_NHA (0 Ông Gôhan / 2 Ông Moori / 1 Ông Paragus) | Nhà (21 Nhà Gôhan / 22 Nhà Moori / 23 Nhà Broly) | Nói chuyện ông nhà |

**Thưởng khi xong cả nhiệm vụ (`rewardDoneTask`)**: +4.500 sức mạnh, +4.500 tiềm năng; +500.000 vàng

### Nhiệm vụ 9 — Nhiệm vụ bái sư

Mô tả (cột `detail`, placeholder `%n` được `transformName` thay theo hành tinh):

> Tìm đường tới %11, trò chuyện với %10 và xin làm đệ tử

| Index | Hằng số ConstTask | Tên bước (DB) | max_count | NPC (DB `npc_id`) | Map (DB `map`) | Điều kiện hoàn thành thực tế (code) |
|---|---|---|---|---|---|---|
| 0 | `TASK_9_0 = 18432` | Lên đường | 1 | — | MAP_QUY_LAO (5 Đảo Kamê / 13 Đảo Guru / 20 Vách núi đen) | Vào map 5/13/20 — `checkDoneTaskGoToMap` |
| 1 | `TASK_9_1 = 18434` | Chào hỏi %10 | 1 | NPC_QUY_LAO (13 Quy Lão Kame / 14 Trưởng lão Guru / 15 Vua Vegeta) | MAP_QUY_LAO (5 Đảo Kamê / 13 Đảo Guru / 20 Vách núi đen) | Nói chuyện sư phụ (Quy Lão/Guru/Vegeta, đúng hành tinh) |

**Thưởng khi xong cả nhiệm vụ (`rewardDoneTask`)**: +5.000 sức mạnh, +5.000 tiềm năng; +500.000 vàng

### Nhiệm vụ 10 — Nhiệm vụ thử sức

Mô tả (cột `detail`, placeholder `%n` được `transformName` thay theo hành tinh):

> Tiêu diệt %12 thể hiện sức mạnh cho %10 thấy

| Index | Hằng số ConstTask | Tên bước (DB) | max_count | NPC (DB `npc_id`) | Map (DB `map`) | Điều kiện hoàn thành thực tế (code) |
|---|---|---|---|---|---|---|
| 0 | `TASK_10_0 = 20480` | Đạt 200k sức mạnh | 1 | — | — | Sức mạnh ≥ 200.000 |
| 1 | `TASK_10_1 = 20482` | Diệt 10 con %12 | 10 | — | MAP_QUY_LAO (5 Đảo Kamê / 13 Đảo Guru / 20 Vách núi đen) | Hạ mob 13 Ốc mượn hồn / 14 Ốc sên / 15 Heo Xayda mẹ (loại nào cũng tính, không phân hành tinh) |
| 2 | `TASK_10_2 = 20484` | Báo cáo với %10 | 1 | NPC_QUY_LAO (13 Quy Lão Kame / 14 Trưởng lão Guru / 15 Vua Vegeta) | MAP_QUY_LAO (5 Đảo Kamê / 13 Đảo Guru / 20 Vách núi đen) | Nói chuyện sư phụ → nhận Sách kỹ năng lv1: 94 Kamejoko (TĐ) / 101 Masenko (NM) / 108 Antomic (XD) |

**Thưởng khi xong cả nhiệm vụ (`rewardDoneTask`)**: +5.500 sức mạnh, +5.500 tiềm năng; +500.000 vàng

### Nhiệm vụ 11 — Nhiệm vụ gia tăng sức mạnh

Mô tả (cột `detail`, placeholder `%n` được `transformName` thay theo hành tinh):

> Ra xã hội làm ăn bươn trải, liều thì ăn nhiều, không liều ăn ít, riêng cờ bạc không phải là thứ để chúng ta liều.  
> Anh tặng chú 1 câu "Cờ bạc người không chơi là người thắng, Người chơi không bao giờ thắng".  
> Đạt các mốc sức mạnh sau

| Index | Hằng số ConstTask | Tên bước (DB) | max_count | NPC (DB `npc_id`) | Map (DB `map`) | Điều kiện hoàn thành thực tế (code) |
|---|---|---|---|---|---|---|
| 0 | `TASK_11_0 = 22528` | Đạt 500k sức mạnh | 1 | 54 Lý Tiểu Nương | 5 Đảo Kamê | Sức mạnh ≥ 40.000 (code kiểm tra cả ≥40.000 và ≥500.000 cho cùng TASK_11_0) |
| 1 | `TASK_11_1 = 22530` | Đạt 550k sức mạnh | 1 | — | — | Sức mạnh ≥ 550.000 |
| 2 | `TASK_11_2 = 22532` | Đạt 600k sức mạnh | 1 | — | — | Sức mạnh ≥ 600.000 |
| 3 | `TASK_11_3 = 22534` | Đi về nhà ông %2 | 1 | 105 Tiến Bry | 42 Vách núi Aru | Nói chuyện sư phụ **hoặc** ông nhà (cả hai case đều chứa TASK_11_3) |

**Thưởng khi xong cả nhiệm vụ (`rewardDoneTask`)**: +6.000 sức mạnh, +6.000 tiềm năng; +500.000 vàng

### Nhiệm vụ 12 — Nhiệm vụ xin phép

Mô tả (cột `detail`, placeholder `%n` được `transformName` thay theo hành tinh):

> Quay về nhà xin ông %2 cho phép tham gia bang hội

| Index | Hằng số ConstTask | Tên bước (DB) | max_count | NPC (DB `npc_id`) | Map (DB `map`) | Điều kiện hoàn thành thực tế (code) |
|---|---|---|---|---|---|---|
| 0 | `TASK_12_0 = 24576` | Đi về nhà %2 | 1 | — | Nhà (21 Nhà Gôhan / 22 Nhà Moori / 23 Nhà Broly) | Vào map nhà 21/22/23 |
| 1 | `TASK_12_1 = 24578` | Nói chuyện - xin phép gia nhập bang hội | 1 | NPC_NHA (0 Ông Gôhan / 2 Ông Moori / 1 Ông Paragus) | Nhà (21 Nhà Gôhan / 22 Nhà Moori / 23 Nhà Broly) | Nói chuyện ông nhà |
| 2 | `TASK_12_2 = 24580` | Báo cáo lại cho %10 | 1 | NPC_QUY_LAO (13 Quy Lão Kame / 14 Trưởng lão Guru / 15 Vua Vegeta) | MAP_QUY_LAO (5 Đảo Kamê / 13 Đảo Guru / 20 Vách núi đen) | Nói chuyện sư phụ |

**Thưởng khi xong cả nhiệm vụ (`rewardDoneTask`)**: +6.500 sức mạnh, +6.500 tiềm năng; +500.000 vàng

### Nhiệm vụ 13 — Nhiệm vụ gia nhập bang hội

Mô tả (cột `detail`, placeholder `%n` được `transformName` thay theo hành tinh):

> Gia nhập 1 bang hội cùng những người đồng đội thiện chí\nCùng làm nhiệm với nhau không quản khó khăn

| Index | Hằng số ConstTask | Tên bước (DB) | max_count | NPC (DB `npc_id`) | Map (DB `map`) | Điều kiện hoàn thành thực tế (code) |
|---|---|---|---|---|---|---|
| 0 | `TASK_13_0 = 26624` | Tạo hoặc gia nhập bang hội có 2 thành viên | 1 | 13 Quy Lão Kame | 5 Đảo Kamê | Gia nhập/được chấp nhận vào bang có ≥ 2 thành viên (`ClanService.checkDoneTaskJoinClan`) — **hoặc** nói chuyện Quy Lão Kame (mọi hành tinh) |
| 1 | `TASK_13_1 = 26626` | Báo cáo cho %10 | 1 | NPC_QUY_LAO (13 Quy Lão Kame / 14 Trưởng lão Guru / 15 Vua Vegeta) | MAP_QUY_LAO (5 Đảo Kamê / 13 Đảo Guru / 20 Vách núi đen) | Nói chuyện sư phụ |

**Thưởng khi xong cả nhiệm vụ (`rewardDoneTask`)**: +7.000 sức mạnh, +7.000 tiềm năng; +500.000 vàng

### Nhiệm vụ 14 — Nhiệm vụ bang hội đầu tiên

Mô tả (cột `detail`, placeholder `%n` được `transformName` thay theo hành tinh):

> Tiến trình sẽ nhanh gấp đôi nếu cùng phối hợp với 1 người đồng đội lên đường làm nhiệm vụ  
> Gợi ý:  
> Heo rừng xuất hiện tại rừng Bamboo  
> Heo da xanh xuất   
> hiện tại núi hoa vàng  
> Heo xayda xuất hiện tại rừng cọ  
> Hãy tới trạm tàu vũ trụ để có thể di chuyển qua các map

| Index | Hằng số ConstTask | Tên bước (DB) | max_count | NPC (DB `npc_id`) | Map (DB `map`) | Điều kiện hoàn thành thực tế (code) |
|---|---|---|---|---|---|---|
| 0 | `TASK_14_0 = 28672` | Tiêu diệt 30 con heo rừng | 30 | — | 27 Rừng Bamboo | Hạ mob 16 Heo rừng — cần có bang; nếu ≥2 người cùng bang trong zone thì +2/con |
| 1 | `TASK_14_1 = 28674` | Tiêu diệt 30 con heo da xanh | 30 | — | 31 Núi hoa vàng | Hạ mob 17 Heo da xanh — như trên |
| 2 | `TASK_14_2 = 28676` | Tiêu diệt 30 con heo xayda | 30 | — | 35 Rừng cọ | Hạ mob 18 Heo Xayda — như trên |
| 3 | `TASK_14_3 = 28678` | Quay về %11 báo cáo nhiệm vụ | 1 | NPC_QUY_LAO (13 Quy Lão Kame / 14 Trưởng lão Guru / 15 Vua Vegeta) | MAP_QUY_LAO (5 Đảo Kamê / 13 Đảo Guru / 20 Vách núi đen) | Nói chuyện sư phụ |

**Thưởng khi xong cả nhiệm vụ (`rewardDoneTask`)**: +7.500 sức mạnh, +7.500 tiềm năng; +500.000 vàng

### Nhiệm vụ 15 — Nhiệm vụ bang hội thứ 2

Mô tả (cột `detail`, placeholder `%n` được `transformName` thay theo hành tinh):

> Tiến trình sẽ nhanh gấp đôi nếu cùng phối hợp với 1 người đồng đội lên đường làm nhiệm vụ

| Index | Hằng số ConstTask | Tên bước (DB) | max_count | NPC (DB `npc_id`) | Map (DB `map`) | Điều kiện hoàn thành thực tế (code) |
|---|---|---|---|---|---|---|
| 0 | `TASK_15_0 = 30720` | Tiêu diệt 30 bulon | 30 | — | 30 Đảo Bulông | Hạ mob 22 Bulon — cần có bang; ≥2 người cùng bang trong zone thì +2/con |
| 1 | `TASK_15_1 = 30722` | Tiêu diệt 30 ukulele | 30 | — | 34 Đông Nam Guru | Hạ mob 23 Ukulele — như trên |
| 2 | `TASK_15_2 = 30724` | Tiêu diệt 30 quỷ mập | 30 | — | 38 Bờ vực đen | Hạ mob 24 Quỷ mập — như trên |
| 3 | `TASK_15_3 = 30726` | Quay về %11 báo cáo nhiệm vụ | 1 | NPC_QUY_LAO (13 Quy Lão Kame / 14 Trưởng lão Guru / 15 Vua Vegeta) | MAP_QUY_LAO (5 Đảo Kamê / 13 Đảo Guru / 20 Vách núi đen) | Nói chuyện sư phụ |

**Thưởng khi xong cả nhiệm vụ (`rewardDoneTask`)**: +8.000 sức mạnh, +8.000 tiềm năng; +500.000 vàng

### Nhiệm vụ 16 — Tiêu diệt quái vật

Mô tả (cột `detail`, placeholder `%n` được `transformName` thay theo hành tinh):

> Tới các hành tinh tiêu diệt quái vật, giải cứu thường dân

| Index | Hằng số ConstTask | Tên bước (DB) | max_count | NPC (DB `npc_id`) | Map (DB `map`) | Điều kiện hoàn thành thực tế (code) |
|---|---|---|---|---|---|---|
| 0 | `TASK_16_0 = 32768` | Tiêu diệt Tambourine | 1 | — | 6 Đông Karin | Hạ mob 25 Tambourine |
| 1 | `TASK_16_1 = 32770` | Tiêu diệt Drum | 1 | — | 10 Thung lũng Namếc | Hạ mob 26 Drum |
| 2 | `TASK_16_2 = 32772` | Tiêu diệt Akkuman | 1 | — | 19 Thành phố Vegeta | Hạ mob 27 Akkuman |
| 3 | `TASK_16_3 = 32774` | Quay về %11 báo cáo nhiệm vụ | 1 | NPC_QUY_LAO (13 Quy Lão Kame / 14 Trưởng lão Guru / 15 Vua Vegeta) | MAP_QUY_LAO (5 Đảo Kamê / 13 Đảo Guru / 20 Vách núi đen) | Nói chuyện sư phụ |

**Thưởng khi xong cả nhiệm vụ (`rewardDoneTask`)**: +8.500 sức mạnh, +8.500 tiềm năng; +500.000 vàng

### Nhiệm vụ 17 — Nhiệm vụ giúp đỡ Cui

Mô tả (cột `detail`, placeholder `%n` được `transformName` thay theo hành tinh):

> Tìm đường tới thành phố Vegeta, gặp và nói chuyện với Cui

| Index | Hằng số ConstTask | Tên bước (DB) | max_count | NPC (DB `npc_id`) | Map (DB `map`) | Điều kiện hoàn thành thực tế (code) |
|---|---|---|---|---|---|---|
| 0 | `TASK_17_0 = 34816` | Tới Thành Phố Vegeta | 1 | — | 19 Thành phố Vegeta | Vào map 19 Thành phố Vegeta |
| 1 | `TASK_17_1 = 34818` | Nói chuyện với Cui | 1 | 12 Cui | 19 Thành phố Vegeta | Nói chuyện Cui/Dr. Brief/Cargo khi đang ở map 19, hoặc Quy Lão Kame (mọi hành tinh), hoặc Guru/Vegeta (đúng hành tinh) |

**Thưởng khi xong cả nhiệm vụ (`rewardDoneTask`)**: +9.000 sức mạnh, +9.000 tiềm năng; +500.000 vàng

### Nhiệm vụ 18 — Nhiệm vụ bất khả thi

Mô tả (cột `detail`, placeholder `%n` được `transformName` thay theo hành tinh):

> Đạt 50 triệu sức mạnh  
> Tiêu diệt bọn tay sai của Fide tại Xayda  
> - Thưởng 50.000.000 sức mạnh  
> - Thưởng 50.000.000 tiềm năng

| Index | Hằng số ConstTask | Tên bước (DB) | max_count | NPC (DB `npc_id`) | Map (DB `map`) | Điều kiện hoàn thành thực tế (code) |
|---|---|---|---|---|---|---|
| 0 | `TASK_18_0 = 36864` | Tiêu diệt 500 Nappa | 500 | — | — | Hạ mob 39 Nappa |
| 1 | `TASK_18_1 = 36866` | Tiêu diệt 400 Soldier | 400 | — | — | Hạ mob 40 Soldier |
| 2 | `TASK_18_2 = 36868` | Tiêu diệt 300 Appule | 300 | — | — | Hạ mob 41 Appule |
| 3 | `TASK_18_3 = 36870` | Tiêu diệt 200 Raspberry | 200 | — | — | Hạ mob 42 Raspberry |
| 4 | `TASK_18_4 = 36872` | Tiêu diệt 100 Thằn lằn xanh | 100 | — | — | Hạ mob 43 Thằn lằn xanh |
| 5 | `TASK_18_5 = 36874` | Báo cáo với %10 | 1 | NPC_QUY_LAO (13 Quy Lão Kame / 14 Trưởng lão Guru / 15 Vua Vegeta) | — | Nói chuyện sư phụ |

**Thưởng khi xong cả nhiệm vụ (`rewardDoneTask`)**: +9.500 sức mạnh, +9.500 tiềm năng; +500.000 vàng

### Nhiệm vụ 19 — Nhiệm vụ tìm diệt đệ tử

Mô tả (cột `detail`, placeholder `%n` được `transformName` thay theo hành tinh):

> Tiêu diệt bọn đệ tử Kuku, Mập Đầu Đinh,  
> Rambo của Fide đại ca tại Xayda  
> Cui có thể biết vị trí của chúng, nếu tìm  
> không thấy hãy đến gặp Cui tại thành  
> phố Vegeta  
> - Thưởng 20.000.000 sức mạnh  
> - Thưởng 20.000.000 tiềm năng

| Index | Hằng số ConstTask | Tên bước (DB) | max_count | NPC (DB `npc_id`) | Map (DB `map`) | Điều kiện hoàn thành thực tế (code) |
|---|---|---|---|---|---|---|
| 0 | `TASK_19_0 = 38912` | Tiêu diệt Kuku | 1 | — | — | Hạ boss Kuku (BossID -20). Cui (map 19) có menu dịch chuyển tới boss giá 50.000.000 vàng |
| 1 | `TASK_19_1 = 38914` | Tiêu diệt Mập đầu đinh | 1 | — | — | Hạ boss Mập đầu đinh (-21) |
| 2 | `TASK_19_2 = 38916` | Tiêu diệt Rambo | 1 | — | — | Hạ boss Rambo (-22) |
| 3 | `TASK_19_3 = 38918` | Trả nhiệm vụ cho %10 | 1 | NPC_QUY_LAO (13 Quy Lão Kame / 14 Trưởng lão Guru / 15 Vua Vegeta) | — | Nói chuyện sư phụ |

**Thưởng khi xong cả nhiệm vụ (`rewardDoneTask`)**: +10.000 sức mạnh, +10.000 tiềm năng; +500.000 vàng

### Nhiệm vụ 20 — Nhiệm vụ Tiểu đội sát thủ

Mô tả (cột `detail`, placeholder `%n` được `transformName` thay theo hành tinh):

> Tiêu diệt Tiểu Đội Sát Thủ do Fide đại  
> ca gọi đến tại Xayda  
> - Thưởng 20.000.000 sức mạnh  
> - Thưởng 20.000.000 tiềm năng

| Index | Hằng số ConstTask | Tên bước (DB) | max_count | NPC (DB `npc_id`) | Map (DB `map`) | Điều kiện hoàn thành thực tế (code) |
|---|---|---|---|---|---|---|
| 0 | `TASK_20_0 = 40960` | Đạt 600 tr sức mạnh | 1 | — | — | Sức mạnh ≥ 600.000.000 |
| 1 | `TASK_20_1 = 40962` | Tiêu diệt Số 4 | 1 | — | — | Hạ boss Số 4 (-23) |
| 2 | `TASK_20_2 = 40964` | Tiêu diệt Số 3 | 1 | — | — | Hạ boss Số 3 (-24) |
| 3 | `TASK_20_3 = 40966` | Tiêu diệt Số 2 | 1 | — | — | Hạ boss Số 2 (-25) |
| 4 | `TASK_20_4 = 40968` | Tiêu diệt Số 1 | 1 | — | — | Hạ boss Số 1 (-26) |
| 5 | `TASK_20_5 = 40970` | Tiêu diệt Tiểu Đội Trưởng | 1 | — | — | Hạ boss Tiểu đội trưởng (-27) |
| 6 | `TASK_20_6 = 40972` | Báo cáo với %10 | 1 | NPC_QUY_LAO (13 Quy Lão Kame / 14 Trưởng lão Guru / 15 Vua Vegeta) | — | Nói chuyện sư phụ |

**Thưởng khi xong cả nhiệm vụ (`rewardDoneTask`)**: +10.500 sức mạnh, +10.500 tiềm năng; +500.000 vàng

### Nhiệm vụ 21 — Nhiệm vụ chạm trán Fide đại ca

Mô tả (cột `detail`, placeholder `%n` được `transformName` thay theo hành tinh):

> Luyện tập đạt 2 tỷ sức mạnh  
> Lên đường tìm diệt Fide đại ca

| Index | Hằng số ConstTask | Tên bước (DB) | max_count | NPC (DB `npc_id`) | Map (DB `map`) | Điều kiện hoàn thành thực tế (code) |
|---|---|---|---|---|---|---|
| 0 | `TASK_21_0 = 43008` | Đạt 2 tỷ sức mạnh | 1 | — | — | Sức mạnh ≥ 2.000.000.000 |
| 1 | `TASK_21_1 = 43010` | Tiêu diệt Fide cấp 1 | 1 | — | — | Hạ boss Fide (-28) ở currentLevel 0 |
| 2 | `TASK_21_2 = 43012` | Tiêu diệt Fide cấp 2 | 1 | — | — | Hạ Fide currentLevel 1 |
| 3 | `TASK_21_3 = 43014` | Tiêu diệt Fide cấp 3 | 1 | — | — | Hạ Fide currentLevel 2 |
| 4 | `TASK_21_4 = 43016` | Báo cáo với %10 | 1 | NPC_QUY_LAO (13 Quy Lão Kame / 14 Trưởng lão Guru / 15 Vua Vegeta) | — | Nói chuyện sư phụ |

**Thưởng khi xong cả nhiệm vụ (`rewardDoneTask`)**: +11.000 sức mạnh, +11.000 tiềm năng; +500.000 vàng

### Nhiệm vụ 22 — Chú bé đến từ tương lai

Mô tả (cột `detail`, placeholder `%n` được `transformName` thay theo hành tinh):

> Đến trái đất, rừng bamboo, rừng dương  
> xỉ, nam Kamê tìm người lạ  
> Đến đảo rùa đưa thuốc cho Quy Lão  
> Theo Ca Lích đến tương lai  
> Giúp họ diệt bọn bọ hung con  
> - Thưởng 1.000.000 sức mạnh  
> - Thưởng 1.000.000 tiềm năng

| Index | Hằng số ConstTask | Tên bước (DB) | max_count | NPC (DB `npc_id`) | Map (DB `map`) | Điều kiện hoàn thành thực tế (code) |
|---|---|---|---|---|---|---|
| 0 | `TASK_22_0 = 45056` | Báo cáo với %2 | 1 | NPC_NHA (0 Ông Gôhan / 2 Ông Moori / 1 Ông Paragus) | Nhà (21 Nhà Gôhan / 22 Nhà Moori / 23 Nhà Broly) | Nói chuyện ông nhà |
| 1 | `TASK_22_1 = 45058` | Đi tìm vị khách lạ | 1 | 38 Ca Lích | — | Nói chuyện Ca Lích (NPC 38) |
| 2 | `TASK_22_2 = 45060` | Đưa thuốc trợ tim cho Quy Lão | 1 | 13 Quy Lão Kame | 5 Đảo Kamê | Nói chuyện Quy Lão Kame (hoặc Guru/Vegeta đúng hành tinh) |
| 3 | `TASK_22_3 = 45062` | Đến tương lai gặp Bunma | 1 | 37 Bunma | 102 Nhà Bunma | Nói chuyện Bunma tương lai (NPC 37) |
| 4 | `TASK_22_4 = 45064` | Diệt 1000 xên con cấp 1 | 1000 | — | — | Hạ mob 58 Xên con cấp 1 |
| 5 | `TASK_22_5 = 45066` | Báo với Bunma tương lai | 1 | 37 Bunma | 102 Nhà Bunma | Nói chuyện Bunma tương lai |

**Thưởng khi xong cả nhiệm vụ (`rewardDoneTask`)**: +11.500 sức mạnh, +11.500 tiềm năng; +500.000 vàng

### Nhiệm vụ 23 — Chạm chán Robot sát thủ lần 1

Mô tả (cột `detail`, placeholder `%n` được `transformName` thay theo hành tinh):

> Hãy đến thành phố phía nam  
> đảo balê hoặc cao nguyên  
> Cùng 2 đồng bang diệt 900 Xên con cấp 3  
> Báo với Bunma tương lai  
> - Thưởng 1.000.000 sức mạnh  
> - Thưởng 1.000.000 tiềm năng

| Index | Hằng số ConstTask | Tên bước (DB) | max_count | NPC (DB `npc_id`) | Map (DB `map`) | Điều kiện hoàn thành thực tế (code) |
|---|---|---|---|---|---|---|
| 0 | `TASK_23_0 = 47104` | Đến điểm hẹn tìm Rôbốt Sát Thủ | 1 | — | 97 Thành phố phía bắc | Vào map **93** Thành phố phía nam (code) — DB ghi map 97 |
| 1 | `TASK_23_1 = 47106` | Tiêu diệt Số 2 (Android 19) | 1 | — | — | Hạ boss Android 19 (-30) |
| 2 | `TASK_23_2 = 47108` | Tiêu diệt Số 1 (Android 20) | 1 | — | 97 Thành phố phía bắc | Hạ boss Dr. Kore (-31) |
| 3 | `TASK_23_3 = 47110` | Diệt 900 xên con cấp 3 | 900 | — | — | Hạ mob 60 Xên con cấp 3 |
| 4 | `TASK_23_4 = 47112` | Báo với Bunma tương lai | 1 | 37 Bunma | 102 Nhà Bunma | Nói chuyện Bunma tương lai |

**Thưởng khi xong cả nhiệm vụ (`rewardDoneTask`)**: +12.000 sức mạnh, +12.000 tiềm năng; +500.000 vàng

### Nhiệm vụ 24 — Chạm trán Robot sát thủ lần 2

Mô tả (cột `detail`, placeholder `%n` được `transformName` thay theo hành tinh):

> Trở về quá khứ, đến sân sau siêu thị  
> Tiêu diệt bọn Rôbốt sát thủ  
> Báo với Bunma tương lai  
> - Thưởng 1.000.000 sức mạnh  
> - Thưởng 1.000.000 tiềm năng

| Index | Hằng số ConstTask | Tên bước (DB) | max_count | NPC (DB `npc_id`) | Map (DB `map`) | Điều kiện hoàn thành thực tế (code) |
|---|---|---|---|---|---|---|
| 0 | `TASK_24_0 = 49152` | Đến sân sau siêu thị | 1 | — | 104 Sân sau siêu thị | Vào map 104 Sân sau siêu thị |
| 1 | `TASK_24_1 = 49154` | Tiêu diệt Android 15 | 1 | — | — | Hạ boss Android 15 (-34) |
| 2 | `TASK_24_2 = 49156` | Tiêu diệt Android 14 | 1 | — | — | Hạ boss Android 14 (-33) |
| 3 | `TASK_24_3 = 49158` | Tiêu diệt Android 13 | 1 | — | — | Hạ boss Android 13 (-32) |
| 4 | `TASK_24_4 = 49160` | Báo với Bunma tương lai | 1 | 37 Bunma | 102 Nhà Bunma | Nói chuyện Bunma tương lai |

**Thưởng khi xong cả nhiệm vụ (`rewardDoneTask`)**: +12.500 sức mạnh, +12.500 tiềm năng; +500.000 vàng

### Nhiệm vụ 25 — Chạm trán Robot sát thủ lần 3

Mô tả (cột `detail`, placeholder `%n` được `transformName` thay theo hành tinh):

> Đến thành phố, ngọn núi, thung lũng phía Bắc  
> Tiêu diệt bọn Rôbốt sát thủ  
> Cùng 2 đồng bang diệt 800 Xên con cấp 5  
> Báo với Bunma tương lai  
> - Thưởng 1.000.000 sức mạnh  
> - Thưởng 1.000.000 tiềm năng

| Index | Hằng số ConstTask | Tên bước (DB) | max_count | NPC (DB `npc_id`) | Map (DB `map`) | Điều kiện hoàn thành thực tế (code) |
|---|---|---|---|---|---|---|
| 0 | `TASK_25_0 = 51200` | Đi tìm Píc Póc | 1 | — | — | Vào map 97 Thành phố phía bắc |
| 1 | `TASK_25_1 = 51202` | Tiêu diệt Póc | 1 | — | — | Hạ boss Pôc (-36) |
| 2 | `TASK_25_2 = 51204` | Tiêu diệt Píc | 1 | — | — | Hạ boss Pic (-35) |
| 3 | `TASK_25_3 = 51206` | Tiêu Diệt Kinh Kong | 1 | — | — | Hạ boss King Kong (-37) |
| 4 | `TASK_25_4 = 51208` | Diệt 800 xên con cấp 5 | 800 | — | — | Hạ mob 62 Xên con cấp 5 |
| 5 | `TASK_25_5 = 51210` | Báo với Bunma tương lai | 1 | 37 Bunma | 102 Nhà Bunma | Nói chuyện Bunma tương lai |

**Thưởng khi xong cả nhiệm vụ (`rewardDoneTask`)**: không có

### Nhiệm vụ 26 — Chạm trán Xên bọ hung

Mô tả (cột `detail`, placeholder `%n` được `transformName` thay theo hành tinh):

> Đến thị trấn Ginder  
> Tiêu diệt Xên Bọ Hung cấp 1  
> Tiêu diệt Xên Bọ Hung cấp 2  
> Tiêu diệt Xên Bọ Hung hoàn thiện  
> Cùng 2 đồng bang diệt 700 Xên con cấp 8  
> Báo với Bunma tương lai  
> - Thưởng 1.000.000 sức mạnh  
> - Thưởng 1.000.000 tiềm năng

| Index | Hằng số ConstTask | Tên bước (DB) | max_count | NPC (DB `npc_id`) | Map (DB `map`) | Điều kiện hoàn thành thực tế (code) |
|---|---|---|---|---|---|---|
| 0 | `TASK_26_0 = 53248` | Đến thị trấn Ginder | 1 | — | — | Vào map 100 Thị trấn Ginder |
| 1 | `TASK_26_1 = 53250` | Tiêu diệt Xên Bọ Hung cấp 1 | 1 | — | — | Hạ Xên Bọ Hung (-100) currentLevel 0 |
| 2 | `TASK_26_2 = 53252` | Tiêu diệt Xên Bọ Hung cấp 2 | 1 | — | — | Hạ Xên Bọ Hung currentLevel 1 |
| 3 | `TASK_26_3 = 53254` | Tiêu diệt Xên Bọ Hung hoàn thiện | 1 | — | — | Hạ Xên Bọ Hung currentLevel 2 |
| 4 | `TASK_26_4 = 53256` | Diệt 700 xên con cấp 8 | 700 | — | — | Hạ mob 65 Xên con cấp 8 |
| 5 | `TASK_26_5 = 53258` | Báo với Bunma tương lai | 1 | 37 Bunma | 102 Nhà Bunma | Nói chuyện Bunma tương lai |

**Thưởng khi xong cả nhiệm vụ (`rewardDoneTask`)**: không có

### Nhiệm vụ 27 — Cuộc dạo chơi của xên

Mô tả (cột `detail`, placeholder `%n` được `transformName` thay theo hành tinh):

> Cẩn thận !!!  
> Những vị khách không mời mà tới  
> thường tỏ ra nguy hiểm

| Index | Hằng số ConstTask | Tên bước (DB) | max_count | NPC (DB `npc_id`) | Map (DB `map`) | Điều kiện hoàn thành thực tế (code) |
|---|---|---|---|---|---|---|
| 0 | `TASK_27_0 = 55296` | Nâng sức đánh gốc lên 10K | 1 | — | — | Nói chuyện Thần mèo Karin (NPC 18) **hoặc** cộng tiềm năng sức đánh khi `dameg` ≥ 35.000 (`checkDoneTaskNangCS`) |
| 1 | `TASK_27_1 = 55298` | Thu thập Capsule kì bí | 50 | — | — | Nhặt item 380 Viên Capsule kì bí (rơi 20% từ mob tempId 58–65 khi đang dùng Máy dò `itemTime.isUseMayDo`) |
| 2 | `TASK_27_2 = 55300` | Đến võ đài xên bọ hung | 1 | — | — | Vào map 103 Võ đài Xên bọ hung |
| 3 | `TASK_27_3 = 55302` | Tiêu diệt 7 đứa con của xên | 7 | — | — | Hạ boss Xên con 1–7 (BossID -102…-108), mỗi con +1 |
| 4 | `TASK_27_4 = 55304` | Tiêu diệt Siêu Bọ Hung | 1 | — | — | Hạ boss Siêu Bọ Hung (-101) ở currentLevel 1 |
| 5 | `TASK_27_5 = 55306` | Báo với Bunma tương lai | 1 | 37 Bunma | 102 Nhà Bunma | Nói chuyện Bunma tương lai |

**Thưởng khi xong cả nhiệm vụ (`rewardDoneTask`)**: không có

### Nhiệm vụ 28 — Cuộc đối đầu không cân sức

Mô tả (cột `detail`, placeholder `%n` được `transformName` thay theo hành tinh):

> Vào lúc 12h trưa các ngày, bạn đến gặp NPC Ô sin tại map Đại hội võ thuật.

| Index | Hằng số ConstTask | Tên bước (DB) | max_count | NPC (DB `npc_id`) | Map (DB `map`) | Điều kiện hoàn thành thực tế (code) |
|---|---|---|---|---|---|---|
| 0 | `TASK_28_0 = 57344` | Đi theo Ôsin | 1 | 44 Ôsin | — | Nói chuyện Ôsin (NPC 44) |
| 1 | `TASK_28_1 = 57346` | Hạ vua địa ngục Drabura | 10 | — | — | Hạ boss Drabura / Drabura 2 / Drabura 3 |
| 2 | `TASK_28_2 = 57348` | Hạ Pui Pui | 10 | — | — | Hạ boss Bui Bui / Bui Bui 2 |
| 3 | `TASK_28_3 = 57350` | Hạ Pui Pui lần 2 | 10 | — | — | Hạ boss Bui Bui / Bui Bui 2 |
| 4 | `TASK_28_4 = 57352` | Hạ Yacôn | 10 | — | — | Hạ boss Yacôn |
| 5 | `TASK_28_5 = 57354` | Hạ Drabura lần 2 | 10 | — | — | Hạ boss Drabura / Drabura 2 / Drabura 3 |
| 6 | `TASK_28_6 = 57356` | Hạ Mabư | 10 | — | — | Hạ boss Mabư 12h (MABU_12H) |
| 7 | `TASK_28_7 = 57358` | Báo cáo với Ôsin | 1 | 44 Ôsin | — | Nói chuyện Ôsin |

**Thưởng khi xong cả nhiệm vụ (`rewardDoneTask`)**: không có

### Nhiệm vụ 29 — ..

Mô tả (cột `detail`, placeholder `%n` được `transformName` thay theo hành tinh):

> ...

| Index | Hằng số ConstTask | Tên bước (DB) | max_count | NPC (DB `npc_id`) | Map (DB `map`) | Điều kiện hoàn thành thực tế (code) |
|---|---|---|---|---|---|---|
| 0 | `TASK_29_0 = 59392` | bạn đã xong nhiệm vụ rồi | -1 | — | — | Không có trigger — nhiệm vụ cuối |

**Thưởng khi xong cả nhiệm vụ (`rewardDoneTask`)**: không có


## 11. Lệnh admin liên quan

`server/Command.java` — lệnh có tham số `n<id>` (chỉ admin, `Command.check` yêu cầu `player.isAdmin()`):

```java
player.playerTask.taskMain.id = idTask - 1;
player.playerTask.taskMain.index = 0;
TaskService.gI().sendNextTaskMain(player);
```

Tức là đặt id = `idTask − 1` rồi gọi `sendNextTaskMain` → người chơi **nhận thưởng của nhiệm vụ `idTask − 1`** và chuyển sang nhiệm vụ kế theo quy tắc nhánh (vd. `n4` → id 3 → nhảy sang `gender + 4`, không nhất thiết là 4).

## 12. Ghi chú / điểm cần lưu ý

1. **Toán tử `<<`**: `getIdTask` = `((id<<10)+index)<<1`; mọi so sánh khóa map/tính năng dựa vào tính đơn điệu này. Giới hạn index ≤ 20 theo hằng sinh sẵn, và `index` lưu dạng `byte` khi load (`Byte.parseByte`).
2. **Nạp template không có `ORDER BY`** (`Manager.java`): thứ tự bước con phụ thuộc thứ tự vật lý trong bảng. Cột `ducvupro` (1..125) có vẻ là cột thứ tự nhưng không được dùng. Nếu DB bị sắp xếp lại, các bước có thể đảo thứ tự hoặc bị tách nhiệm vụ.
3. **`MAP_500` (-5) không được `transformMapId` xử lý** → các bước 4_0..6_2 gửi mapId = -5 cho client.
4. **TASK_11_0 "Đạt 500k sức mạnh"** nhưng `checkDoneTaskPower` đã hoàn thành nó ở mốc **40.000** (dòng `if (power >= 40000) doneTask(TASK_11_0)`), mốc 500.000 trở nên thừa.
5. **TASK_8_0 "Đạt 40.000 sức mạnh"** cũng hoàn thành khi chỉ cần **vào map làng 0/7/14**.
6. **TASK_13_0 "Tạo hoặc gia nhập bang hội có 2 thành viên"** có thể hoàn thành chỉ bằng **nói chuyện Quy Lão Kame** (vì `doneTask(TASK_13_0)` nằm ngoài điều kiện gender và không kiểm tra bang). Tương tự 22_2, 17_1, 18_5, 19_3, 20_6, 21_4 với Quy Lão Kame ở mọi hành tinh.
7. **Ông nhà không kiểm tra hành tinh**: bất kỳ Ông Gôhan/Moori/Paragus nào cũng xác nhận được các bước "báo cáo ông nhà" (thực tế map nhà đã chặn khác hành tinh).
8. **TASK_23_0**: DB ghi map 97, code hoàn thành khi vào map **93** (Thành phố phía nam). TASK_23_2 DB ghi "Tiêu diệt Số 1 (Android 20)" nhưng code dùng `BossID.DR_KORE`.
9. **TASK_27_0**: tên bước "Nâng sức đánh gốc lên 10K", notify ghi 35K, code kiểm tra `dameg ≥ 35.000` (hoặc nói chuyện Thần mèo Karin). Thoại Karin đòi "500 viên capsule" nhưng `max_count` của 27_1 là **50**.
10. **Nhiệm vụ 28**: thoại Bunma tương lai (TASK_27_5) nói "Hạ 25 Drabura … Hạ 50 Mabư" nhưng DB `max_count` = **10** cho mỗi bước. TASK_28_2 và 28_3 cùng mục tiêu Bui Bui / Bui Bui 2; 28_1 và 28_5 cùng Drabura/Drabura 2/Drabura 3.
11. **Nhiệm vụ 29** (`'..'`, bước "bạn đã xong nhiệm vụ rồi", `max_count = -1`) là điểm cuối, không có trigger nào.
12. **Thưởng lệch hành tinh ở nhiệm vụ 4/5/6**: `rewardDoneTask` có case 4 (7.000) và 5 (20.000) nhưng **không có case 6** → người Xayda nhận ít SM/TN hơn (3.500) so với Namếc (23.000) và Trái Đất (9.500) dù làm nhiệm vụ tương đương.
13. **Nhiệm vụ 0 và 25–29 không được vàng/SMTN type 2**; 25–28 không có thưởng nào từ `rewardDoneTask`.
14. **Văn bản `detail` lệch thưởng thực tế** (vd. nhiệm vụ 1 "200tr vàng" nhưng code cho 200.000 vàng; nhiệm vụ 18 "50.000.000 sức mạnh" nhưng code cho 9.500).
15. **Thiếu `break`** (hiện vô hại vì case rơi xuống đều rỗng): `TASK_7_3` → `TASK_8_0`, `TASK_8_2` → `TASK_9_0`, `TASK_22_5` → `TASK_23_0`. Trong `checkMapCanJoin`, `case 80` thiếu `break` nên map 80 Núi khỉ vàng thực tế yêu cầu `TASK_21_0`.
16. **Hội thoại placeholder/chưa hoàn thiện**: TASK_20_6 nói "NgocRongOnline", TASK_21_4 nói "null\nnull\nnull", TASK_22_0 "Ngon", TASK_22_2 "I a cờ bú", TASK_24_4 "Quá ghê gớm =))". `task_main_template.id = 13` có chuỗi `\n` dạng literal (bị escape 2 lần).
17. **`TaskService.loadTask()`** chứa kiểm tra IP cứng `36.50.135.149` và gọi `System.exit(1)` nếu sai IP. Hiện lời gọi trong `server/ServerManager.java` đã bị comment — nếu bật lại, server sẽ tự tắt trên máy khác.
18. `checkDoneTaskUseItem` có `switch` rỗng; `canNhanCayThong` là field của singleton (xem tài liệu 13).
19. Tiến độ bước chỉ lưu `count` dạng `short` (`Short.parseShort`) — `max_count` lớn nhất hiện tại là 1000 nên an toàn.
20. Task 14/15 **không tính** nếu người chơi chưa có bang (`player.clan == null`), kể cả khi đang ở đúng bước.
