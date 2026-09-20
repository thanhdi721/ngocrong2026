# 29 — `TaskService` viết lại cho tuyến nhiệm vụ mới

> Tài liệu bàn giao của phần **lõi** việc thay tuyến nhiệm vụ chính.
> Đặc tả nguồn: [27 — Dữ liệu tuyến nhiệm vụ mới](27-du-lieu-nhiem-vu-moi.md) ·
> [28 — Boss bản nhiệm vụ](28-boss-ban-nhiem-vu.md) · [25 — Id vật phẩm mới](25-bang-id-vat-pham-moi.md) ·
> [22 §0 — Quyết định đã chốt](22-san-sang-code.md) · [12 — Cơ chế cũ](../1-he-thong-hien-tai/12-nhiem-vu-chinh.md)
> Dữ liệu: [`SRC/sql/patch/02-nhiem-vu-moi.sql`](../../SRC/sql/patch/02-nhiem-vu-moi.sql)

## Mục lục

1. [Tóm tắt](#1-tóm-tắt)
2. [File đã thêm / đã sửa](#2-file-đã-thêm--đã-sửa)
3. [Kiến trúc mới của `TaskService`](#3-kiến-trúc-mới-của-taskservice)
4. [Bảng hàm trigger](#4-bảng-hàm-trigger)
5. [Cách nạp và áp dụng bảng thưởng](#5-cách-nạp-và-áp-dụng-bảng-thưởng)
6. [Bốn lỗi cũ đã sửa](#6-bốn-lỗi-cũ-đã-sửa)
7. [Điểm móc nhóm sau phải nối](#7-điểm-móc-nhóm-sau-phải-nối)
8. [Mâu thuẫn tài liệu ↔ mã nguồn](#8-mâu-thuẫn-tài-liệu--mã-nguồn)
9. [Chỗ nghi ngờ](#9-chỗ-nghi-ngờ)

---

## 1. Tóm tắt

| Chỉ số | Giá trị |
|---|---|
| Bước con đã cài điều kiện | **235 / 238** |
| 3 bước còn lại | `TASK_48_1`, `TASK_49_1`, `TASK_50_0` — **cố ý** không có điều kiện: đây là bước đệm để `subTasks.size()` của task nhánh khớp với task gốc, `switchTaskBranch` / `switchTaskMain` nhảy thẳng qua chúng |
| Hàm trigger mới (B1–B14) | **14 mã trigger**, cài bằng **28 phương thức public** mới |
| Hàm public cũ | giữ nguyên tên + chữ ký, **không nơi gọi nào phải sửa** |
| Biên dịch | **558 file, sạch** (JDK 17) — tăng 2 file so với 556 vì thêm `TaskMainReward` và `TaskRewardDAO` |
| `consts/ConstTask.java` | **không đổi** — hằng `TASK_0_0 → TASK_299_20` và mọi hằng phụ (`MAP_*`, `NPC_*`, `TEN_*`) đã đủ dùng |

---

## 2. File đã thêm / đã sửa

### File mới (2)

| File | Nội dung |
|---|---|
| `SRC/src/nro/models/task/TaskMainReward.java` | Mô hình một dòng bảng `task_main_reward` + lớp con `RewardItem` (id, số lượng, danh sách option) |
| `SRC/src/nro/models/database/TaskRewardDAO.java` | Nạp toàn bộ bảng vào `HashMap` lúc khởi động, tra theo khóa `taskId * 1000 + (subIndex + 1)`; có sẵn biến thể `get(taskId, subIndex, gender)` lọc `gender IN (-1, gender)` |

### File đã sửa (2)

| File | Thay đổi |
|---|---|
| `SRC/src/nro/models/services/TaskService.java` | Viết lại toàn bộ phần nhiệm vụ chính (phần **nhiệm vụ phụ / nhiệm vụ bang** giữ nguyên 100%) |
| `SRC/src/nro/models/server/Manager.java` | (a) thêm `ORDER BY task_main_template.id, task_sub_template.ducvupro` vào câu nạp task — [27 §9 mục 14](27-du-lieu-nhiem-vu-moi.md#9-chỗ-nghi-ngờ) cảnh báo JOIN không `ORDER BY` có thể trả xen kẽ và `TaskService` tra nhầm `index`; (b) gọi `TaskRewardDAO.load(ConnectionDatabase)` ngay sau khi nạp task |

> **`database/MrBlue.java` KHÔNG bị sửa.** Bảng thưởng nạp qua `Manager`, không cần đụng `MrBlue`.
> `MrBlue:721-730` đã sẵn đọc `data_task[3]` thành `TaskMain.lastTime`, và `PlayerDAO:676-679` đã ghi
> đủ 4 phần tử — trigger **B12** (bước có giới hạn thời gian) dùng được ngay, không cần đổi lưu trữ.

---

## 3. Kiến trúc mới của `TaskService`

### 3.1 Luồng tiến độ

```
<sự kiện game>  →  checkDoneTaskXxx(...)      // bảng điều kiện, tra theo doc 27 §4
                        ↓  isCurrentTask?
                   doneTask(player, TASK_a_b)
                        ↓
                   addDoneSubTask(player, n)
                        ↓ count >= maxCount
                   rewardDoneSubTask(taskId, indexVừaXong)   ← MỚI: thưởng theo BƯỚC
                        ↓
              index++ ; lastTime = 0
                        ↓
        index < size ?  sendNextSubTask        → recheckPassiveSubTask
                     :  sendNextTaskMain
                            ↓
                       rewardDoneTask()        ← thưởng hoàn thành CẢ nhiệm vụ
                       getNextTaskMainId()     ← bảng chuyển tiếp
                       sendTaskMain() + thông báo + recheckPassiveSubTask
```

Ba điểm khác tuyến cũ:

1. **Thưởng theo bước** (`sub_index >= 0`) được trả trong `addDoneSubTask`, đúng lúc bước vừa xong —
   trước đây chỉ có thưởng theo cả nhiệm vụ.
2. **`lastTime` được xóa mỗi lần sang bước mới**, để đồng hồ của trigger B12 không dính lại từ bước trước.
3. **`recheckPassiveSubTask`** chạy sau mỗi lần sang bước. Tuyến cũ có lỗi kinh điển: bước "đạt X sức mạnh"
   chỉ kiểm khi `NPoint.powerUp` được gọi, nên người chơi đã dư sức mạnh vẫn đứng im tới lần tăng SM kế tiếp.
   Nay các bước **thụ động** được kiểm lại ngay: `TASK_10_2`, `TASK_23_0`, `TASK_31_5`, `TASK_33_4`,
   `TASK_49_6` (mốc sức mạnh), `TASK_12_0` (đã có đệ tử), `TASK_39_1` (đã đủ 7 viên Ngọc Rồng).

### 3.2 Bảng chuyển tiếp task id — `getNextTaskMainId(int)`

```
id 20 → 21        id 48 → 21
id 31 → 32        id 49 → 32
id 47 → -1 (HẾT)  id 50 → -1 (HẾT)
còn lại → id + 1
```

`sendNextTaskMain` gặp `-1` thì **không** gọi `getTaskMainById(id + 1)` (48 là nhánh của NV 20, 51 không
tồn tại). Thay vào đó: trao thưởng kết, ghim `index` vào bước cuối, gửi lại `sendTaskMain` (message 40) để
người chơi vẫn xem lại được nhiệm vụ cuối, rồi báo "Bạn đã hoàn thành toàn bộ tuyến nhiệm vụ chính!".

**Sau NV 3 KHÔNG còn rẽ theo hành tinh.** Tuyến cũ có `case 3: getTaskMainById(player, player.gender + 4)`
vì NV 4/5/6 là ba biến thể theo hành tinh. Tuyến mới dùng **một bản ghi dùng chung cho cả 3 hành tinh**;
khác biệt hành tinh nằm ở hai chỗ khác:

- **phần thưởng** — cột `gender` của `task_main_reward` (NV 3 trao trang bị cấp 1, NV 10 trao sách chưởng);
- **placeholder** — `transformMapId` / `transformNpcId` / `transformName` đổi map/NPC/tên theo hành tinh khi nạp.

### 3.3 Ba điểm rẽ nhánh — `switchTaskBranch` / `switchTaskMain`

| Điểm rẽ | Bước | NPC | Chọn A | Chọn B |
|---|---|---|---|---|
| 1 | `TASK_20_1` | 71 Berry, map 160 | `doneTask(TASK_20_1)` → giữ task **20**, index 2 | `switchTaskBranch(player, 48)` → task **48**, index 2 |
| 2 | `TASK_31_1` | 62 Potage, map 140 | `doneTask(TASK_31_1)` → giữ task **31**, index 2 | `switchTaskBranch(player, 49)` → task **49**, index 2 **+ trao 1 Bình chứa Commeson (638)** bản không hạn sử dụng |
| 3 | `TASK_47_0` | 64 Thiên Sứ Whis, map 145 | `doneTask(TASK_47_0)` → giữ task **47**, index 1 | `switchTaskMain(player, 50, 1)` → task **50**, index 1 |

Cả hai hàm đều: nạp `getTaskMainById(player, newId)`, đặt `index`, đặt `count = 0` cho mọi bước,
`lastTime = 0`, gán vào `player.playerTask.taskMain`, rồi `sendTaskMain` (message 40) + thông báo +
`recheckPassiveSubTask`.

Item 638 trao ở điểm rẽ 2 **không** nằm trong bảng thưởng, vì bước `TASK_49_1` không bao giờ chạy qua
`addDoneSubTask` (bị nhảy qua). `switchTaskBranch` tự trao, kèm option 30 "Không thể giao dịch".

### 3.4 Tiện ích nội bộ mới

| Hàm | Việc |
|---|---|
| `getCurrentSubTask(player)` | lấy `SubTaskMain` đang làm, null-safe |
| `isMapNha / isMap200 / isMap500 / isMapVachNui / isMapTTVT / isMapLang / isMapQuyLao` | so map hiện tại với placeholder theo hành tinh — **dùng chính `transformMapId`** nên chỉ có một nguồn sự thật |
| `isMapVanhDaiRung(mapId)` | map 27..38 (NV 41 bước 1) |
| `isMapConDuongRanDoc(mapId)` | map 141/142/143 (NV 35 bước 2) |
| `hasAllMemoryShards(player)` | đủ bộ 7 Mảnh Ký Ức 2002..2008 trong hành trang (NV 45 bước 3) |
| `subItem / addItemToBag` | trừ / trao vật phẩm, tự gắn option 30 khi trao |
| `countPlayerInZone(player, requireSameClan)` | đếm **người thật** trong khu (`!isBot && !isPet && !isBoss && isPl()`) |

---

## 4. Bảng hàm trigger

### 4.1 A1–A12 — hàm đã có, chỉ viết lại bảng điều kiện

| Hàm (giữ nguyên chữ ký) | Kiểu | Số bước phụ trách | Ai gọi (đã nối sẵn) |
|---|---|---|---|
| `checkDoneTaskKillMob(Player, Mob)` | A1 | 37 bước | `mob/Mob.java:169` |
| `checkDoneTaskKillBoss(Player, Boss)` | A2 | 33 bước | `boss/Boss.java` + 48 chỗ trong `boss/**` |
| `checkDoneTaskTalkNpc(Player, Npc)` → `boolean` | A3 | 81 bước | `openBaseMenu` của 22 lớp trong `npc_list/` |
| `checkDoneTaskPickItem(Player, ItemMap)` | A4 | 15 bước | `map/Zone.java:413` |
| `checkDoneTaskPower(Player, long)` | A5 | 5 bước | `player/NPoint.java:1865` |
| `checkDoneTaskGoToMap(Player, Zone)` | A6 | 27 bước | `ChangeMapService:539`, `PlayerService:203` |
| `checkDoneTaskUseTiemNang(Player)` | A7 | `TASK_1_2` | `NPoint.doUseTiemNang:1949` |
| `checkDoneTaskGetItemBox(Player)` | A8 | `TASK_0_1` | `services_func/UseItem.java:106` |
| `checkDoneTaskConfirmMenuNpc(Player, Npc, byte)` | A9 + B14 | `TASK_0_3` + 3 điểm rẽ (4 hằng) | `npc_list/DauThan.java:30` (còn Berry / Potage / DaiThienSu **chưa nối**) |
| `checkDoneTaskJoinClan(Player)` | A10 | `TASK_13_0` | `services/ClanService.java:980` |
| `checkDoneTaskNangCS(Player)` | A11 | `TASK_3_2` | `NPoint.increasePoint` type 2 (1902, 1908) |
| `checkDoneTaskUseItem(Player, Item)` | A12 | 11 bước | `services_func/UseItem.java:947` |

Hai hàm nữa giữ lại cho tương thích:

- `checkDoneTaskFind7Stars(Player)` — nay chỉ chuyển tiếp sang `checkDoneTaskCollect7Stars`.
- `checkDoneTaskCollect7Stars(Player)` — **mới**, phục vụ `TASK_39_1`: đếm lại số loại Ngọc Rồng 14..20
  đang có trong hành trang (mỗi loại tính đúng 1 lần) rồi bù chênh lệch, thay vì cộng dồn theo lượt nhặt.

### 4.2 B1–B14 — hàm mới

| Mã | Hàm mới | Kiểu | Bước dùng | **AI PHẢI GỌI** |
|---|---|---|---|---|
| **B1** | `checkDoneTaskDungeon(Player, int typeMap)`<br>`checkDoneTaskDungeonForZone(Zone, int typeMap)` | phó bản | `TASK_21_1`, `TASK_35_3`, `TASK_43_4` | `map/phoban/RedRibbonHQ` (chỗ `winDT = true`) · `SnakeWay.finish()` + `CADICH.leaveMap()` (chỗ `endCDRD = true`) · `DestronGas.finish()` / cờ `hatchiyatchDead`. Dùng bản `...ForZone` để duyệt **mọi** người trong instance |
| **B2** | `checkDoneTaskUpgradeItem(Player, Item)` | nâng cấp | `TASK_17_1` | `combine/NangCapVatPham` nhánh roll THÀNH CÔNG. Điều kiện `item.getOptionParam(72) >= 2` đã kiểm sẵn bên trong |
| **B3** | `checkDoneTaskCombine(Player, int type)`<br>`checkDoneTaskCombineByExistingItem(Player)` | ghép đồ | `TASK_26_1`, `TASK_26_2` | `combine/PhaLeHoaTrangBi` → `checkDoneTaskCombine(pl, TaskService.COMBINE_PHA_LE_HOA)`; `combine/EpSaoTrangBi` → `COMBINE_EP_SAO`. Gọi `...ByExistingItem` khi mở menu Bà Hạt Mít để **chống kẹt** (người đã sẵn có đồ option 107 ≥ 1) |
| **B4** | `checkDoneTaskBuyItem(Player, int itemTemplateId, int shopId)` | mua shop | `TASK_8_1`, `TASK_14_1` | `shop/ShopService` — gọi **sau khi** đã trừ tiền và `addItemBag` thành công |
| **B5** | `checkDoneTaskWinMatch(Player, int typePvp)` | thắng PvP | `TASK_44_2` | `matches/ThachDau.reward(winner)` · `DeathOrAliveArena` (chỗ `haveRewardVDST = true`) · `WorldMartialArtsTournament` (chỗ `martialArtsTournamentWins++`) |
| **B6** | `checkDoneTaskWishDragon(Player, int starType)` | ước rồng | `TASK_39_2` | `services/shenron/SummonDragon.confirmWish`, **ngay trước** `lastTimeShenronAppeared = now`. Chỉ tính `starType == 1` và map 0/7/14 |
| **B7** | `checkDoneTaskHarvestPea(Player, int soHat)` | hái đậu | `TASK_11_0` | `npc/MagicTree.harvestPea` **sau** `addPeaHarvest`. Truyền đúng số hạt vừa hái (cộng `soHat`, không phải 1) |
| **B8** | `checkDoneTaskHavePet(Player)` | có đệ tử | `TASK_12_0` | `services/PetService.createNewPet` **và** `player/Player.update` (định kỳ). Đã tự gọi trong `recheckPassiveSubTask` |
| **B9** | `checkDoneTaskLearnSkill(Player, Skill)` | học chưởng | `TASK_10_1` | `services/SkillService` sau khi `skill.point` tăng. Điều kiện `skillId ∈ {1, 3, 5}` và `point >= 1` đã kiểm bên trong |
| **B10** | `checkDoneTaskOpenPower(Player)`<br>`canOpenPowerByTask(Player)` | mở giới hạn | `TASK_33_3` (0→1), `TASK_44_4` (1→2), `TASK_47_3` / `TASK_50_3` (2→3) | `services/OpenPowerService` — hàm MỚI `openPowerByTask(Player)` phải hỏi `canOpenPowerByTask` trước, tăng `limitPower`, rồi gọi `checkDoneTaskOpenPower`. Lộ qua menu "Phá giới hạn (nhiệm vụ)" ở `npc_list/QuocVuong` và `npc_list/ToSuKaio` |
| **B11** | `checkDoneTaskBasePoint(Player, int type)` | chỉ số gốc | `TASK_33_2` (HP gốc chạm trần) | `player/NPoint.increasePoint(type, point)` — gọi **cuối** hàm với `type` vừa nâng. `type == 2` được chuyển tiếp sang `checkDoneTaskNangCS` để không phá hành vi cũ |
| **B12** | `getTimeLimitOfSubTask(int)`<br>`startTimedSubTask(Player, int)`<br>`updateTimedSubTask(Player, int)` → `boolean`<br>`resetTimedSubTask(Player)`<br>`updateTimedSubTaskTick(Player)`<br>`getRemainTimeOfSubTask(Player)` | có giờ | `TASK_7_0` (180.000 ms), `TASK_23_2` (300.000), `TASK_29_2` (360.000), `TASK_34_2` (300.000), `TASK_42_2` (600.000) | **đã tự cài đầy đủ** — `checkDoneTaskKillMob` / `checkDoneTaskPickItem` gọi `updateTimedSubTask` trước khi cộng tiến độ; `checkDoneTaskGoToMap` gọi `startTimedSubTask` khi vào map 166 và `resetTimedSubTask` khi rời. Nhóm sau chỉ cần gọi thêm `updateTimedSubTaskTick(player)` trong `Player.update` để hết giờ được phát hiện cả khi người chơi đứng im |
| **B13** | `countPlayerInZone(Player, boolean)`<br>`checkDoneTaskTogether(Player, Zone, int nMember, boolean requireSameClan)`<br>`checkDoneTaskTogetherInZone(Player)` | cùng người khác | `TASK_13_1`, `TASK_19_3`, `TASK_31_3` / `TASK_49_3`, `TASK_35_1`, `TASK_45_2` | **đã tự cài đầy đủ** cho `TASK_13_1` và `TASK_19_3` (nằm trong `checkDoneTaskKillMob`). Ba bước còn lại kiểm định kỳ: nhóm sau gọi `checkDoneTaskTogetherInZone(player)` trong `Player.update`; riêng `TASK_35_1` có thể gọi thẳng `checkDoneTaskTogether(pl, zone, 2, true)` ngay sau `SnakeWayService.openConDuongRanDoc` thành công |
| **B14** | `switchTaskBranch(Player, int newTaskId)`<br>`switchTaskMain(Player, int newTaskId, int newIndex)`<br>`getNextTaskMainId(int)` | rẽ nhánh | `TASK_20_1`, `TASK_31_1`, `TASK_47_0` | `checkDoneTaskConfirmMenuNpc` đã xử lý cả ba menu. Nhóm sau chỉ cần cho `npc_list/Berry` (71), `npc_list/Potage` (62), `npc_list/DaiThienSu` (64) **mở menu 2 nút** và gọi `TaskService.gI().checkDoneTaskConfirmMenuNpc(player, this, (byte) select)` trong `confirmMenu` |

### 4.3 Luật chi tiết của B12 (bước có giới hạn thời gian)

| Bước | Hạn | Quy tắc |
|---|---|---|
| `TASK_7_0` | 180.000 ms | đồng hồ bấm ở điểm tiến độ **đầu tiên**; quá hạn → `count = 0`, `lastTime = now`, làm lại |
| `TASK_23_2` | 300.000 ms | như trên |
| `TASK_34_2` | 300.000 ms | như trên |
| `TASK_42_2` | 600.000 ms | như trên |
| `TASK_29_2` | 360.000 ms | đồng hồ bấm **khi vào map 166** (`startTimedSubTask`); **rời map 166 → `count = 0`, `lastTime = 0`** (`resetTimedSubTask`) |

Thời gian còn lại lấy bằng `getRemainTimeOfSubTask(player)`; `Player.update` đã gửi message **43**
(`sendUpdateCountSubTask`) mỗi giây nên client vẫn thấy tiến độ nhảy về 0 khi hết giờ.

> Doc 27 §6 đề xuất "gửi thời gian còn lại qua message 43". Message 43 hiện **chỉ có một trường
> `short count`** — thêm trường sẽ vỡ giao thức với client Unity đã đóng gói. Vì vậy phần đếm ngược
> hiển thị bằng `sendThongBao` ("Hết giờ! Bước nhiệm vụ được tính lại từ đầu"), còn `getRemainTimeOfSubTask`
> để sẵn cho nhóm client nếu sau này nâng cấp giao thức.

### 4.4 Luật chi tiết của B13 (bước làm cùng người khác)

| Bước | Số người | Cùng bang? | Cách tính |
|---|---|---|---|
| `TASK_13_1` | ≥ 2 | **Có** | mỗi mạng quái mẹ +1; **≥ 3 người cùng bang trong khu thì +2** |
| `TASK_19_3` | ≥ 2 | Không | mỗi Appule ở map 71/72 **+2** (doc 27 yêu cầu ×2 tiến độ) |
| `TASK_31_3` / `TASK_49_3` | ≥ 2 | Không | kiểm định kỳ ở map 103 |
| `TASK_35_1` | ≥ 2 | **Có** | kiểm ở map 143 |
| `TASK_45_2` | ≥ 2 | Không | kiểm ở map 78 |

"Người thật" = `pl != null && !pl.isBot && !pl.isPet && !pl.isBoss && pl.isPl()`, tính **cả bản thân**
người chơi, nên `nMember = 2` nghĩa là "có thêm 1 người nữa".

---

## 5. Cách nạp và áp dụng bảng thưởng

### 5.1 Nạp

`Manager` (khối nạp template, ngay sau `Successfully loaded task`):

```java
nro.models.database.TaskRewardDAO.load(ConnectionDatabase);
```

`TaskRewardDAO.load(Connection)` chạy đúng một câu:

```sql
select task_id, sub_index, gender, sm, tn, gold, gem, ruby, items, text
from task_main_reward order by task_id, sub_index, gender
```

rồi đổ vào `HashMap<Integer, List<TaskMainReward>>`, khóa `taskId * 1000 + (subIndex + 1)`.
Cột `items` là JSON `[[itemId, soLuong, [[optionId, param], ...]], ...]`, phân tích bằng
`org.json.simple` (đã có sẵn trong `lib/`).

**Nếu bảng chưa được import**, `load` nuốt exception, ghi log và để bảng rỗng — server vẫn chạy,
chỉ là nhiệm vụ không phát thưởng. Không có nguy cơ chết lúc khởi động.

### 5.2 Áp dụng — `applyReward(player, taskId, subIndex)`

1. Lấy mọi dòng có `gender IN (-1, player.gender)` — **cộng dồn**, không lấy dòng đầu tiên rồi thôi.
   Quên bước này là người chơi **không nhận được trang bị NV 3 / sách chưởng NV 10**
   ([27 §9 mục 11](27-du-lieu-nhiem-vu-moi.md#9-chỗ-nghi-ngờ)).
2. `sm` → `Service.addSMTN(player, 0, sm, false)`; `tn` → `addSMTN(player, 1, tn, false)`.
   (Không dùng type 2 để tránh cộng đúp — dòng `gender = -1` đã tách riêng `sm` và `tn`.)
3. `gold` / `gem` / `ruby` → cộng thẳng vào `player.inventory`, gửi `Service.sendMoney` một lần ở cuối.
4. `items` → `giveRewardItem` từng mục.
5. `text` → `Service.sendThongBao` (dòng mô tả đã sinh sẵn trong SQL, không gõ tay).

Hai nơi gọi:

| Nơi | Dòng bảng |
|---|---|
| `rewardDoneTask(player)` trong `sendNextTaskMain` | `sub_index = -1` |
| `rewardDoneSubTask(player, taskId, index)` trong `addDoneSubTask` | `sub_index = index vừa xong` |

### 5.3 Danh hiệu 2030 / 2031 — KHÔNG `addItemBag`

`giveRewardItem` nhận ra hai id đặc biệt và đi nhánh riêng:

```java
int idEffect = (templateId == 2030) ? 257 : 258;
new BadgesData(player, idEffect, 36500);   // 36500 ngày ~ 100 năm
BadgesService.turnOnBadges(player, idEffect);
player.nPoint.calPoint();
Service.gI().point(player);
```

**Không** gọi thêm `player.dataBadges.add(...)` — constructor `BadgesData(Player, int, int)` dòng 39
đã tự `add`. Đây chính là lỗi nhân đôi chỉ số đang có ở `BadgesTaskService.updateDoneTask` và
`ShopService.buyDanhHieu`; tuyến mới **không lặp lại**.

⚠️ `idEffect` 257 / 258 **chưa có tài nguyên** (`DataEffect_257|258`, `ImgEffect_257|258.png` x1–x4).
Danh hiệu vẫn cộng chỉ số nhưng **tàng hình** cho tới khi vẽ đủ 10 file — xem
[25 §3.6](25-bang-id-vat-pham-moi.md#36-nhóm-f--2-danh-hiệu).

### 5.4 Vật phẩm trao / trừ **không** qua bảng thưởng

Những chỗ bảng không làm được nằm trong `switch` của `doneTask` và trong `checkDoneTaskUseItem`:

| Bước | Việc |
|---|---|
| `TASK_5_2` | trừ 1 Kỷ Vật Của Ông (2010) |
| `TASK_32_5` | trừ 3 Mảnh Ký Ức Vỡ (2025) |
| `TASK_34_5` | trừ 1 Mảnh Ký Ức Đóng Băng (2026) |
| `TASK_37_4` | trừ 1 Lõi Phép Babiđây (2028) |
| `TASK_11_1` | nâng `magicTree.level` lên 2 rồi trừ 1 Hạt Giống Hy Vọng (2013) |
| `TASK_17_2` | trừ 1 Búa rèn cũ (2015) |
| `TASK_26_3` | phát 3 dòng thoại rồi trừ 1 Mẫu kim loại (2020) |
| `TASK_45_3` | kiểm đủ 7 mảnh 2002–2008 → trừ cả 7 + trừ 2024 → **trao 1 Lõi Hư Không (2000)** |
| `TASK_47_1` | trừ 2000 → **trao 1 Vỏ Lõi rỗng (2001)** |
| `TASK_50_1` | **GIỮ NGUYÊN** 2000 làm cờ hậu truyện |
| điểm rẽ 2 | `switchTaskBranch(player, 49)` trao 1 Bình chứa Commeson (638) |

`TASK_5_1` (lau kỷ vật) và `TASK_40_3` (đưa Mảnh Ký Ức 1 cho Tổ Sư Kaio) **cố ý không trừ item** —
kỷ vật còn phải nộp ở `TASK_5_2`, còn Mảnh Ký Ức 1 phải giữ tới NV 45.

---

## 6. Bốn lỗi cũ đã sửa

| # | Lỗi | Hậu quả | Đã sửa thế nào |
|---|---|---|---|
| 1 | **`TASK_13_0` lọt ra ngoài ngoặc `gender`** trong `checkDoneTaskTalkNpc` (khối `QUY_LAO_KAME`) | mọi hành tinh đều hoàn thành được bước "gia nhập bang" chỉ bằng cách bấm Quy Lão Kame — và bước đó đáng ra thuộc `checkDoneTaskJoinClan` | `TASK_13_0` **chỉ còn** ở `checkDoneTaskJoinClan`, có thêm điều kiện `player.clan != null`. Ba khối NPC sư phụ nay dùng chung một `case` với một lần kiểm `npc.tempId == transformNpcId(player, NPC_QUY_LAO)`, không còn ngoặc lồng nhau để lọt |
| 2 | **`power >= 40000 → TASK_11_0` thừa** trong `checkDoneTaskPower` | `TASK_11_0` bị hoàn thành ở mốc 40.000 trong khi dòng ngay dưới đặt mốc 500.000 — mốc thiết kế bị vô hiệu | Xóa hẳn. Bảng mốc mới chỉ còn 5 dòng: 250.000 → `TASK_10_2`; 80.000.000 → `TASK_23_0`; 2.000.000.000 → `TASK_31_5` + `TASK_49_6`; 3.000.000.000 → `TASK_33_4` |
| 3 | **`transformMapId`: `MAP_VACH_NUI` (-4) trỏ 39/40/41** | Jaco (NV 3), Bà Hạt Mít (NV 17), Quốc Vương (NV 33) đứng ở **42/43/44**; map 39–41 và 42–44 trùng tên trong `map_template` nên mũi tên chỉ đường dẫn người chơi sai chỗ mà không ai nhận ra | trả về **42 / 43 / 44** |
| 4 | **`transformMapId`: `MAP_500` (-5) không được xử lý** | placeholder -5 rơi thẳng xuống client, mũi tên chỉ đường của NV 4 không hiện | trả về **2 / 9 / 16** (Thung lũng tre / Thị trấn Moori / Làng Plant) |

Ba chỗ nhỏ khác đã dọn luôn:

- `checkDoneTaskGetItemBox` có `!player.isBot && !player.isBot` (lặp, thiếu một điều kiện) → thay bằng `player.isPl()`.
- `checkDoneTaskTalkNpc` **không kiểm đúng ông của hành tinh mình** ở nhóm `ONG_GOHAN/ONG_MOORI/ONG_PARAGUS` → nay kiểm.
- `isCurrentTask` viết `(id << 10) + index << 1` — đúng nhờ thứ tự ưu tiên toán tử, nhưng dễ đọc nhầm → thêm ngoặc tường minh `(((id << 10) + index) << 1)`, **không đổi giá trị**.

---

## 7. Điểm móc nhóm sau phải nối

Tất cả hàm dưới đây **tự kiểm `isCurrentTask` bên trong**, gọi thừa cũng không sao.

| # | File | Chỗ móc | Gọi gì | Bước được mở |
|---|---|---|---|---|
| 1 | `map/phoban/RedRibbonHQ.java` | ngay sau `winDT = true` (dòng ~238) | `TaskService.gI().checkDoneTaskDungeonForZone(zone, ConstMap.MAP_DOANH_TRAI)` | `TASK_21_1` |
| 2 | `map/phoban/SnakeWay.java` + `boss_con_duong_ran_doc/CADICH.java` | chỗ đặt `endCDRD = true` | `checkDoneTaskDungeonForZone(zone, ConstMap.MAP_CON_DUONG_RAN_DOC)` | `TASK_35_3` |
| 3 | `map/phoban/DestronGas.java` | `finish()` / cờ `hatchiyatchDead` | `checkDoneTaskDungeonForZone(zone, ConstMap.MAP_KHI_GAS_HUY_DIET)` | `TASK_43_4` |
| 4 | `combine/NangCapVatPham` | nhánh roll thành công | `checkDoneTaskUpgradeItem(player, item)` | `TASK_17_1` |
| 5 | `combine/PhaLeHoaTrangBi` | roll thành công | `checkDoneTaskCombine(player, TaskService.COMBINE_PHA_LE_HOA)` | `TASK_26_1` |
| 6 | `combine/EpSaoTrangBi` | ép thành công | `checkDoneTaskCombine(player, TaskService.COMBINE_EP_SAO)` | `TASK_26_2` |
| 7 | `npc_list/BaHatMit` | `openBaseMenu` | `checkDoneTaskCombineByExistingItem(player)` (chống kẹt) | `TASK_26_1` |
| 8 | `shop/ShopService` | sau khi trừ tiền **và** `addItemBag` thành công | `checkDoneTaskBuyItem(player, itemTemplateId, shopId)` | `TASK_8_1`, `TASK_14_1` |
| 9 | `matches/ThachDau.reward(winner)` | đầu hàm | `checkDoneTaskWinMatch(winner, 0)` | `TASK_44_2` |
| 10 | `matches/dai_hoi_vo_thuat/DeathOrAliveArena` | chỗ `haveRewardVDST = true` | `checkDoneTaskWinMatch(player, 1)` | `TASK_44_2` |
| 11 | `WorldMartialArtsTournament` | chỗ `martialArtsTournamentWins++` | `checkDoneTaskWinMatch(player, 2)` | `TASK_44_2` |
| 12 | `services/shenron/SummonDragon.confirmWish` | **ngay trước** `lastTimeShenronAppeared = now` | `checkDoneTaskWishDragon(player, starType)` | `TASK_39_2` |
| 13 | `npc/MagicTree.harvestPea` | sau `addPeaHarvest` | `checkDoneTaskHarvestPea(player, soHat)` | `TASK_11_0` |
| 14 | `services/PetService.createNewPet` | cuối hàm | `checkDoneTaskHavePet(player)` | `TASK_12_0` |
| 15 | `services/SkillService` | sau khi `skill.point` tăng | `checkDoneTaskLearnSkill(player, skill)` | `TASK_10_1` |
| 16 | `services/OpenPowerService` | hàm MỚI `openPowerByTask(Player)` | hỏi `canOpenPowerByTask(player)` → `limitPower++` → `checkDoneTaskOpenPower(player)` | `TASK_33_3`, `TASK_44_4`, `TASK_47_3`, `TASK_50_3` |
| 17 | `player/NPoint.increasePoint(type, point)` | cuối hàm | `checkDoneTaskBasePoint(player, type)` | `TASK_33_2` |
| 18 | `player/Player.update` | cạnh `sendUpdateCountSubTask(this)` (dòng ~528) | `updateTimedSubTaskTick(this)` · `checkDoneTaskTogetherInZone(this)` · `checkDoneTaskHavePet(this)` | `TASK_7_0`, `TASK_23_2`, `TASK_29_2`, `TASK_34_2`, `TASK_42_2`, `TASK_31_3`, `TASK_49_3`, `TASK_35_1`, `TASK_45_2`, `TASK_12_0` |
| 19 | `npc_list/Berry.java` (**chưa tồn tại**, NPC 71) | `confirmMenu` | `checkDoneTaskConfirmMenuNpc(player, this, (byte) select)` với menu 2 nút | `TASK_20_1` |
| 20 | `npc_list/Potage.java` (NPC 62) | `confirmMenu` | như trên | `TASK_31_1` |
| 21 | `npc_list/DaiThienSu.java` (NPC 64) | `confirmMenu` (**hiện rỗng**) | như trên | `TASK_47_0` |
| 22 | `npc_list/Granola.java` (**chưa tồn tại**, NPC 76) | `openBaseMenu` | `checkDoneTaskTalkNpc(player, this)` | `TASK_20_2`, `TASK_20_5` |
| 23 | `mob/Mob.dropItemTask` | theo bảng rơi của doc 27 | không gọi hàm nào, chỉ ép rơi đúng item khi `isCurrentTask` | `TASK_2_2`, `TASK_5_0`, `TASK_16_3`, `TASK_32_4` … |
| 24 | `map/Zone.getItemMapsForPlayer` | lọc theo `getIdTask` | ẩn/hiện `ItemMap` 2008 (map 78), 2026 (map 110), 2023 (map 166) | `TASK_45_1`, `TASK_34_3`, `TASK_29_2` |

Ngoài ra, **[27 §7.3](27-du-lieu-nhiem-vu-moi.md#73-lỗi-cũ-chặn-cứng--phải-sửa-trước-khi-bật-tuyến)** còn
13 lỗi chặn cứng nằm ngoài `TaskService` (Cooler / Baby / Broly / SuperBroly / DrLychee / Hatchiyack không
gọi `checkDoneTaskKillBoss`; `Osin.confirmMenu`; `Bill.confirmMenu`; `QuocVuong`; `GiuMaDauBo`; `QuaTrung`).
`TaskService` đã sẵn sàng cho cả 13 chỗ đó — chỉ chờ được gọi.

---

## 8. Mâu thuẫn tài liệu ↔ mã nguồn

Theo nguyên tắc **"tin mã nguồn"**:

| # | Doc 27 nói | Mã nguồn thật | Đã làm |
|---|---|---|---|
| 1 | `TASK_23_3` / `TASK_23_4`: Fide "currentLevel 1 rồi 2" / "currentLevel 3" | `BossID.FIDE` (-28) chạy `currentLevel` **0 / 1 / 2** (xem `checkDoneTaskKillBoss` cũ) | `currentLevel == 2` → `TASK_23_4`, còn lại → `TASK_23_3` |
| 2 | `TASK_30_2` / `TASK_30_3`: Xên bọ hung "currentLevel 1 rồi 2" / "currentLevel 3" | `BossID.XEN_BO_HUNG` (-100) chạy **0 / 1 / 2**; doc 28 §6.4 cũng ghi `-2100` là 0/1 rồi 2 | `currentLevel == 2` → `TASK_30_3`, còn lại → `TASK_30_2` |
| 3 | `TASK_21_2`: "npc 26 VÀ mapId == 57 VÀ **`zone.winDT == true`**" | `winDT` là trường của `map/phoban/RedRibbonHQ`, một **`Runnable`** điều khiển phó bản — **không** phải trường của `Zone` cũng không phải của `Map`. `TaskService` không với tới | Bỏ điều kiện `winDT`. An toàn vì bước trước đó (`TASK_21_1`, trigger B1) đã đòi phá xong doanh trại. Ghi rõ trong `isWinDoanhTrai()` |
| 4 | `TASK_36_2` đường vòng: "mob **95** / 118 (Cadic M)" ở map 165 | `ConstMob` đặt tên **95 = `THO_CON`**, 118 = `CADIC_M` | Nhận cả 95 và 118 nhưng **chỉ ở map 165**, nên dù tên hằng sai thì cũng không lẫn sang chỗ khác. **Cần kiểm lại `mob_template` của map 165** |
| 5 | `TASK_6_1` boss **−2000** "Kẻ Thu Gom"; `TASK_15_1` boss **−2001** "Jaco Vô Thức" | **Hai boss này chưa tồn tại**: không có trong `BossID.java`, `BossesData.java`, `BossManager.java`, `boss/quest/` (chỉ có 6 boss −2100…−2105 + Heart) | Khai báo hằng cục bộ `TaskService.BOSS_KE_THU_GOM = -2000` / `BOSS_JACO_VO_THUC = -2001` và cài sẵn `case`. **Hai bước này KHÔNG xong được** cho tới khi nhóm boss tạo hai con đó |
| 6 | NPC **71 Berry**, **76 Granola** | `ConstNpc` **không có** hai hằng này, `npc_list/` **không có** hai lớp này | Khai báo `TaskService.NPC_BERRY = 71` / `NPC_GRANOLA = 76`. Khi nhóm NPC thêm hằng vào `ConstNpc`, nên đổi sang dùng hằng chung |
| 7 | B12 "gửi thời gian còn lại qua message **43**" | Message 43 chỉ có **một trường `short count`**; thêm trường là vỡ giao thức với client đã đóng gói | Không đổi message 43. Có `getRemainTimeOfSubTask(player)` để dùng khi client được nâng cấp |
| 8 | Doc 12 §5: "`id == 3` → `gender + 4`" (tuyến cũ) | — | Bỏ hẳn, thay bằng `getNextTaskMainId`. Tuyến mới không còn NV 4/5/6 theo hành tinh |

---

## 9. Chỗ nghi ngờ

| # | Vấn đề | Tôi đã chọn gì | Cần chốt lại |
|---|---|---|---|
| 1 | **`TASK_6_1` và `TASK_15_1` kẹt cứng** vì boss −2000 / −2001 chưa tồn tại (mục 8.5) | cài sẵn `case`, chờ boss | **Chặn phát hành.** Không có hai boss này thì người chơi **không qua nổi NV 6**, tức là không ai đi quá chương 1 |
| 2 | **Thưởng theo bước gửi `sendThongBao` mỗi lần** (237 dòng thưởng đều có cột `text`) | gửi đúng như thiết kế | 238 thông báo trong cả tuyến là chấp nhận được, nhưng bước có `max_count` lớn thì chỉ gửi 1 lần lúc xong bước — không spam. Nếu chủ dự án thấy ồn, bỏ dòng `sendThongBao(reward.text)` trong `applyReward` |
| 3 | **`TASK_16_0` (đi về phía Nam) kiểm gender**: map 29 chỉ tính cho Trái Đất, 33 cho Namếc, 37 cho Xayda | theo đúng doc 27 | Nếu người chơi Namếc đi lạc sang map 29 thì bước không tính. Có nên bỏ ràng buộc gender cho bước "đi map" không? |
| 4 | **`TASK_36_1` nhận cả map 114 lẫn 165** trong cùng một `case` | theo doc 27 §6.1 (hai cửa) | Người chơi vô tình đi qua Sa mạc hoang vu (165) vì việc khác cũng xong bước. Đây là **tính năng chống kẹt**, không phải lỗi, nhưng nên biết |
| 5 | **`TASK_41_1` nhận mob {16, 22, 32, 33, 24} ở map 27–38** | doc 27 ghi "map 27-38" | Dải 12 map rất rộng, gồm cả map của chương 2 (`TASK_14_2` dùng map 27/31/35). Không đụng nhau vì khác bước, nhưng nếu muốn chặt hơn thì phải liệt kê map cụ thể |
| 6 | **`TASK_38_1` nhận mob {62,63,64,65} ở map 97–100**, trong khi `TASK_28_1` nhận {62,63,64} ở 97/98/99 và `TASK_30_1` nhận 65 ở 100 | ba bước cùng vùng quái, phân biệt bằng `isCurrentTask` | Đúng theo doc, nhưng người chơi sẽ thấy ba nhiệm vụ khác nhau bắt cày cùng một bầy quái. Cân nhắc đổi vùng của `TASK_38_1` |
| 7 | **`isWinDoanhTrai` luôn trả true** (mục 8.3) | bỏ kiểm | Nếu muốn chặt: `RedRibbonHQ` phải ghi cờ vào `Zone` (thêm `public boolean winDT` vào `Zone`), rồi bật lại kiểm tra ở đây |
| 8 | **`recheckPassiveSubTask` có thể đệ quy** qua chuỗi `doneTask → addDoneSubTask → sendNextSubTask → recheckPassiveSubTask` | chấp nhận | Hiện **có giới hạn**: chỉ 7 bước là "thụ động" và không bước nào đứng ngay sau bước thụ động khác, nên chuỗi dừng sau 1–2 vòng. Nếu sau này thêm bước thụ động liền kề, phải thêm cờ chống đệ quy |
| 9 | **`checkDoneTaskWinMatch` tính MỌI loại trận** (tham số `typePvp` chưa dùng để lọc) | doc 27 liệt kê 3 nguồn, đều tính | Nếu chủ dự án muốn `TASK_44_2` chỉ tính đấu trường Sinh Tử, thêm điều kiện theo `typePvp` |
| 10 | **`TaskRewardDAO` là ảnh chụp lúc khởi động**, sửa bảng trong DB không có hiệu lực tới khi restart | giống mọi bảng template khác của `Manager` | Nếu cần nạp nóng, thêm lệnh admin gọi `TaskRewardDAO.load()` (bản không tham số đã có sẵn) |
| 11 | **Không xóa `switch` thưởng hardcode cũ mà thay hẳn** — `rewardDoneTask` giờ chỉ gọi `applyReward` | đúng yêu cầu | Người chơi **cũ** đang ở giữa tuyến cũ sẽ nhận thưởng theo bảng mới ngay khi restart. Kế hoạch reset tiến độ (`SRC/sql/patch/03-reset-tien-do.sql`) phải chạy **cùng lúc** với 02 |
| 12 | **`Byte.parseByte` đọc task id** ở `MrBlue:723` | không đụng (`MrBlue` nằm trong danh sách cấm sửa) | Task id tối đa của tuyến mới là **50**, vẫn lọt trong `byte` (-128..127). Nếu sau này thêm nhiệm vụ vượt 127 thì phải sửa `MrBlue` |
| 13 | **`TaskMain(TaskMain)` không chép `lastTime`** | không sửa (mọi nơi tạo bản sao đều muốn `lastTime = 0`) | `MrBlue` đặt `lastTime` sau khi gọi `getTaskMainById`, nên tiến độ B12 vẫn khôi phục đúng |
| 14 | **`TASK_29_2` bắt đầu đếm giờ khi VÀO map 166** chứ không phải khi nhặt viên đầu tiên | doc 27 ghi "rời map 166 -> count = 0, lastTime = 0", hàm ý đồng hồ gắn với map | Nếu muốn đồng hồ chạy từ viên đầu tiên, bỏ lời gọi `startTimedSubTask` trong `checkDoneTaskGoToMap` |
| 15 | **Chưa chạy thử trên server thật** | chỉ biên dịch sạch (558 file, JDK 17) | Cần một lượt chơi thử từ NV 0 tới NV 7 sau khi import 02 + 03 và sau khi nhóm boss tạo −2000 / −2001 |

---

*Hết file 29. Mã nguồn: [`TaskService.java`](../../SRC/src/nro/models/services/TaskService.java) ·
[`TaskRewardDAO.java`](../../SRC/src/nro/models/database/TaskRewardDAO.java) ·
[`TaskMainReward.java`](../../SRC/src/nro/models/task/TaskMainReward.java)*
