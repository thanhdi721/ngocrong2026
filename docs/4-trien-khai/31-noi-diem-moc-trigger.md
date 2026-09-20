# 31 — Nối điểm móc trigger cho tuyến nhiệm vụ mới

> Bàn giao phần **nối dây**: `TaskService` đã có đủ hàm trigger (xem
> [29 — TaskService tuyến mới](29-taskservice-tuyen-moi.md)), file này ghi lại **ai gọi hàm nào, ở đâu**.
> Đặc tả nguồn: [27 — Dữ liệu tuyến nhiệm vụ mới](27-du-lieu-nhiem-vu-moi.md) ·
> [25 — Bảng id vật phẩm mới](25-bang-id-vat-pham-moi.md) · [22 §0 — Quyết định đã chốt](22-san-sang-code.md)

## Mục lục

1. [Tóm tắt](#1-tóm-tắt)
2. [Bảng điểm móc đã nối](#2-bảng-điểm-móc-đã-nối)
3. [Hàm mới duy nhất được thêm — `OpenPowerService.openPowerByTask`](#3-hàm-mới-duy-nhất-được-thêm--openpowerserviceopenpowerbytask)
4. [Vật phẩm nhiệm vụ: rơi từ quái và hiện trên bản đồ](#4-vật-phẩm-nhiệm-vụ-rơi-từ-quái-và-hiện-trên-bản-đồ)
5. [Điểm móc CHƯA nối được và vì sao](#5-điểm-móc-chưa-nối-được-và-vì-sao)
6. [Chỗ nghi ngờ](#6-chỗ-nghi-ngờ)

---

## 1. Tóm tắt

| Chỉ số | Giá trị |
|---|---|
| Điểm móc theo bảng [29 §7](29-taskservice-tuyen-moi.md#7-điểm-móc-nhóm-sau-phải-nối) | **24** |
| Đã nối trong lượt này | **20** |
| Nhóm khác đã nối sẵn (Berry 71, Granola 76) | **2** |
| Không nối được | **2** (xem [§5](#5-điểm-móc-chưa-nối-được-và-vì-sao)) |
| File Java đã sửa | **20** |
| Hàm mới được thêm | **1** — `OpenPowerService.openPowerByTask(Player)` |
| `services/TaskService.java`, `consts/ConstTask.java` | **KHÔNG đụng tới** |
| `boss/**`, `npc_list/Berry.java`, `npc_list/Granola.java`, `SRC/sql/**` | **KHÔNG đụng tới** |
| Biên dịch | **562 file, sạch** (JDK 17) — 562 vì các nhóm khác đã thêm `boss/quest/*`, `boss/heart/Heart.java`, `npc_list/Berry.java`, `npc_list/Granola.java`, `database/TaskRewardDAO.java`, `task/TaskMainReward.java` |

Mọi chỗ thêm đều có comment `// TUYẾN MỚI:` và **chỉ gọi hàm**, không viết lại điều kiện nhiệm vụ —
mọi luật (kể cả `isCurrentTask`) vẫn nằm trong `TaskService`.

---

## 2. Bảng điểm móc đã nối

Số dòng tính theo cây mã sau khi sửa.

| # | File | Hàm / vị trí | Dòng | Gọi gì | Cho bước |
|---|---|---|---|---|---|
| 1 | `map/phoban/RedRibbonHQ.java` | `update()`, ngay sau `winDT = true` | **241** | `checkDoneTaskDungeonForZone(zoneTask, ConstMap.MAP_DOANH_TRAI)` cho từng khu | `TASK_21_1` |
| 2 | `map/phoban/SnakeWay.java` | `update()`, trong khối `!kickoutcdrd && (endCDRD \|\| hết giờ)`, **bọc thêm `if (endCDRD)`** | **94** | `checkDoneTaskDungeonForZone(zoneTask, ConstMap.MAP_CON_DUONG_RAN_DOC)` | `TASK_35_3` |
| 3 | `map/phoban/DestronGas.java` | `update()`, trong khối `!kickoutkghd && (hatchiyatchDead \|\| hết giờ)`, **bọc thêm `if (hatchiyatchDead)`** | **104** | `checkDoneTaskDungeonForZone(zoneTask, ConstMap.MAP_KHI_GAS_HUY_DIET)` | `TASK_43_4` |
| 4 | `combine/NangCapVatPham.java` | `nangCapVatPham`, **trong nhánh `Util.isTrue(ratioCombine, 100)` = roll THÀNH CÔNG** | **220** | `checkDoneTaskUpgradeItem(player, itemDo)` | `TASK_17_1` |
| 5 | `combine/PhaLeHoaTrangBi.java` | `phaLeHoa`, trong nhánh `if (success)` sau khi option 107 đã tăng | **194** | `checkDoneTaskCombine(player, TaskService.COMBINE_PHA_LE_HOA)` | `TASK_26_1` |
| 6 | `combine/EpSaoTrangBi.java` | `epSaoTrangBi`, sau `sendEffectSuccessCombine` (chỉ chạy khi ép xong) | **164** | `checkDoneTaskCombine(player, TaskService.COMBINE_EP_SAO)` | `TASK_26_2` |
| 7 | `npc_list/BaHatMit.java` | `openBaseMenu`, ngay sau `canOpenNpc` | **41** | `checkDoneTaskCombineByExistingItem(player)` | `TASK_26_1` (chống kẹt) |
| 8 | `shop/ShopService.java` | `buyItem`, **sau** `subMoneyByItemShop` / `subIemByItemShop` **và** `addItemBag` + `sendItemBags` | **888** | `checkDoneTaskBuyItem(player, itemTempId, shop.id)` | `TASK_8_1`, `TASK_14_1` |
| 9 | `matches/ThachDau.java` | `reward(Player plWin)`, dòng đầu | **91** | `checkDoneTaskWinMatch(plWin, 0)` | `TASK_44_2` |
| 10 | `matches/dai_hoi_vo_thuat/DeathOrAliveArena.java` | `reward()`, ngay sau `haveRewardVDST = true` | **206** | `checkDoneTaskWinMatch(player, 1)` | `TASK_44_2` |
| 11 | `matches/dai_hoi_vo_thuat/WorldMartialArtsTournament.java` | `finish()`, ngay sau `plWin.martialArtsTournamentWins++` | **201** | `checkDoneTaskWinMatch(plWin, 2)` | `TASK_44_2` |
| 12 | `services/shenron/SummonDragon.java` | `confirmWish()`, **ngay trước** `shenronLeave(..., WISHED)` | **476** | `checkDoneTaskWishDragon(playerSummonShenron, shenronStar)` | `TASK_39_2` |
| 13 | `npc/MagicTree.java` | `harvestPea()`, **sau** `addPeaHarvest`, truyền `currPeasTemp - currPeas` | **166** | `checkDoneTaskHarvestPea(player, soHat)` | `TASK_11_0` |
| 14 | `services/PetService.java` | `createNewPet(...)`, sau `player.pet = pet` | **330** | `checkDoneTaskHavePet(player)` | `TASK_12_0` |
| 15 | `utils/SkillUtil.java` | `setSkill(Player, Skill)`, cuối hàm | **310** | `checkDoneTaskLearnSkill(pl, skill)` | `TASK_10_1` |
| 16a | `services/OpenPowerService.java` | **hàm MỚI** `openPowerByTask(Player)` | **61–78** | `canOpenPowerByTask` → `limitPower++` → `checkDoneTaskOpenPower` | `TASK_33_3`, `TASK_44_4`, `TASK_47_3`, `TASK_50_3` |
| 16b | `npc_list/QuocVuong.java` | `openBaseMenu` (nút mới) + `confirmMenu` `case 2` | **23**, **75** | `canOpenPowerByTask` → `openPowerByTask` | như trên |
| 16c | `npc_list/ToSuKaio.java` | `showLimitPowerMenu` (nút mới) + `confirmMenu` menu `MENU_NANG_GIOI_HAN` `case 2` | **79**, **59** | `canOpenPowerByTask` → `openPowerByTask` | như trên |
| 17 | `player/NPoint.java` | `increasePoint(byte, short)`, **cuối hàm**, ngay trước `Service.gI().point(player)` | **1939** | `checkDoneTaskBasePoint(player, type)` | `TASK_33_2` (+ `TASK_3_2` qua `type == 2`) |
| 18 | `player/Player.java` | `update()`, cạnh `sendUpdateCountSubTask(this)` (vòng lặp ~1 giây) | **529** | `updateTimedSubTaskTick(this)` · `checkDoneTaskTogetherInZone(this)` · `checkDoneTaskHavePet(this)` | `TASK_7_0`, `TASK_23_2`, `TASK_29_2`, `TASK_34_2`, `TASK_42_2`, `TASK_31_3`, `TASK_49_3`, `TASK_35_1`, `TASK_45_2`, `TASK_12_0` |
| 19 | `npc_list/Berry.java` (NPC 71) | `openBaseMenu` + `confirmMenu` | 43, 69 | `checkDoneTaskTalkNpc` + `checkDoneTaskConfirmMenuNpc` | `TASK_20_1` — **nhóm NPC đã nối, tôi không đụng** |
| 20 | `npc_list/Potage.java` (NPC 62) | menu riêng `MENU_RE_NHANH_NV31 = 2201` ở `openBaseMenu`; `confirmMenu` chuyển tiếp | **18**, **24**, **49** | `checkDoneTaskConfirmMenuNpc(player, this, (byte) select)` | `TASK_31_1` |
| 21 | `npc_list/DaiThienSu.java` (NPC 64) | menu riêng `MENU_RE_NHANH_NV47 = 2202` ở `openBaseMenu` (chỉ map 145); `confirmMenu` chuyển tiếp | **22**, **31**, **48** | `checkDoneTaskConfirmMenuNpc(player, this, (byte) select)` | `TASK_47_0` |
| 22 | `npc_list/Granola.java` (NPC 76) | `openBaseMenu` | 42 | `checkDoneTaskTalkNpc(player, this)` | `TASK_20_2`, `TASK_20_5` — **nhóm NPC đã nối** |
| 23 | `mob/Mob.java` | `dropItemTask`, thêm 2 `case` | **1115**, **1122** | ép rơi item 2014 / 2025 theo `getIdTask` | `TASK_16_3`, `TASK_32_4` |
| 24 | `map/Zone.java` | `getItemMapsForPlayer` | **289**, **300**, **309–316** | lọc theo `getIdTask` | `TASK_45_1`, `TASK_34_3`, `TASK_29_2` |

### 2.1 Ghi chú cách nối B1 (phó bản)

`RedRibbonHQ` đã có sẵn khối `if (allCharactersDead && !winDT)` chạy **đúng một lần** — móc đặt ngay
trong đó. `SnakeWay` và `DestronGas` thì khối `if (!kickoutcdrd/...)` chạy một lần **nhưng cũng chạy khi
HẾT GIỜ**, nên móc được bọc thêm `if (endCDRD)` / `if (hatchiyatchDead)`: **hết giờ mà chưa hạ boss thì
không tính là phá xong phó bản**. Nhờ đó không cần thêm biến cờ mới và không phải sửa
`boss/khi_gas/Hatchiyack.java` (nằm trong danh sách cấm sửa) hay `boss_con_duong_ran_doc/CADICH.java`.

### 2.2 Ghi chú cách nối B5 (thắng đối kháng)

`PVP.java:59,61` gọi `this.reward(pWinner)` — `ThachDau` ghi đè `reward`, nên móc ở đầu
`ThachDau.reward` bắt đúng mọi trận thách đấu thắng. Ba lớp PvP khác (`TraThu`, `LuyenTap`,
`PKCommeson`) **không** được móc, đúng theo [27 §6](27-du-lieu-nhiem-vu-moi.md#6-trigger-mới-phải-viết--điểm-móc)
(chỉ liệt kê 3 nguồn).

### 2.3 Ghi chú cách nối B9 (học kỹ năng)

Doc 29 ghi điểm móc là `services/SkillService`. **Trong mã nguồn thật `SkillService` không có chỗ nào
tăng `skill.point`** — mọi đường học / nâng kỹ năng đều đổ về một chỗ duy nhất:
`utils/SkillUtil.setSkill(Player, Skill)` (6 nơi gọi: `QuyLaoKame` ×2, `TruongLaoGuru` ×2,
`VuaVegeta` ×2, `SkillService.learSkillSpecial`, `UseItem` ×2, `HocTuyetKy`).
Móc đặt ở `SkillUtil.setSkill` để không sót nhánh nào. Xem cảnh báo ở [§6 mục 1](#6-chỗ-nghi-ngờ).

### 2.4 Ghi chú cách nối B14 (rẽ nhánh)

Hai NPC `Potage` (62) và `DaiThienSu` (64) đều **đã có menu khác** đang dùng `ConstNpc.BASE_MENU`.
Để không phá menu cũ (Commeson của Potage, hội thoại của Đại Thiên Sứ), mỗi NPC dùng **một id menu
riêng** khai báo cục bộ trong chính lớp đó:

| NPC | Hằng | Giá trị | Điều kiện hiện menu |
|---|---|---|---|
| `Potage` | `MENU_RE_NHANH_NV31` | 2201 | `mapId == 140` **và** `getIdTask(player) == ConstTask.TASK_31_1` |
| `DaiThienSu` | `MENU_RE_NHANH_NV47` | 2202 | `mapId == 145` **và** `getIdTask(player) == ConstTask.TASK_47_0` |

Hai giá trị 2201 / 2202 đã kiểm: **không trùng** hằng nào trong `ConstNpc` và không trùng id menu
hardcode nào đang dùng (`ToSuKaio` dùng 2001). `confirmMenu` của hai NPC kiểm
`player.idMark.getIndexMenu() == <id riêng>` rồi chuyển thẳng lựa chọn sang
`TaskService.checkDoneTaskConfirmMenuNpc` — **không có một dòng logic nhánh nào nằm ngoài `TaskService`**.

### 2.5 Ghi chú cách nối B10 (mở giới hạn theo nhiệm vụ)

Nút mới **"Phá giới hạn (nhiệm vụ)"** được chèn ở **vị trí 2** của menu, đẩy "Từ chối" xuống vị trí 3:

- `QuocVuong.openBaseMenu`: `Bản thân`(0) · `Đệ tử`(1) · `Phá giới hạn (nhiệm vụ)`(2) · `Từ chối`(3)
- `ToSuKaio.showLimitPowerMenu`: `Bản thân`(0) · `Đệ tử`(1) · `Phá giới hạn (nhiệm vụ)`(2) · `Từ chối`(3)

Chỉ dựng menu 4 nút khi `canOpenPowerByTask(player) == true`; ngược lại giữ nguyên menu 3 nút cũ.
Vị trí 2 ở menu cũ vốn là "Từ chối" và **vốn không có `case` xử lý** (QuocVuong) hoặc rơi vào
`default -> npcChat` (ToSuKaio), nên `case 2` mới thêm vẫn hỏi lại `canOpenPowerByTask` trước khi làm gì:
người chơi không ở đúng bước bấm "Từ chối" thì hành vi y như cũ.

---

## 3. Hàm mới duy nhất được thêm — `OpenPowerService.openPowerByTask`

`SRC/src/nro/models/services/OpenPowerService.java:61`

```java
public boolean openPowerByTask(Player player) {
    if (player == null || !player.isPl() || player.nPoint == null)        return false;
    if (!TaskService.gI().canOpenPowerByTask(player))                     return false;   // + thông báo
    if (player.nPoint.limitPower >= NPoint.MAX_LIMIT)                     return false;   // + thông báo
    player.nPoint.limitPower++;
    Service.gI().sendThongBao(player, "Giới hạn sức mạnh của bạn đã được phá vỡ, tăng lên 1 bậc");
    TaskService.gI().checkDoneTaskOpenPower(player);                                      // gọi SAU khi đã tăng
    return true;
}
```

Khác `openPowerBasic` / `openPowerSpeed` ở ba điểm, đúng yêu cầu:

| | `openPowerBasic` | `openPowerSpeed` | **`openPowerByTask`** |
|---|---|---|---|
| Chi phí | 0 | 50.000.000 vàng | **0** |
| Chờ | 2,4 giờ (`itemTime.isOpenPower`) | không | **không** |
| Yêu cầu | `nPoint.canOpenPower()` (đã chạm trần SM) | — | **đang đứng đúng bước nhiệm vụ** |

**Chống mở nhiều lần** không cần thêm biến lưu: `canOpenPowerByTask` (đã có sẵn trong `TaskService`)
ràng **cặp bước ↔ bậc giới hạn**:

| Bước | `limitPower` phải bằng |
|---|---|
| `TASK_33_3` | 0 |
| `TASK_44_4` | 1 |
| `TASK_47_3` / `TASK_50_3` | 2 |

Nên ngay sau `limitPower++`, lần gọi kế tiếp tự thất bại (bậc không còn khớp), **và** `checkDoneTaskOpenPower`
đẩy nhiệm vụ sang bước sau nên `getIdTask` cũng không còn khớp. Hai lớp chặn độc lập.

---

## 4. Vật phẩm nhiệm vụ: rơi từ quái và hiện trên bản đồ

Nhóm trước đã sửa một phần `Mob.dropItemTask` và `Zone.getItemMapsForPlayer`
(bỏ nhánh Ngọc 7 sao của mốc cũ, sửa id 2001/2002 → 2009/2010). **Phần đó được giữ nguyên hoàn toàn**,
lượt này chỉ *thêm*.

### 4.1 `mob/Mob.dropItemTask` — thêm 2 nhánh

| `case` (ConstMob) | Điều kiện | Rơi | Bước | Nguồn |
|---|---|---|---|---|
| `BULON` (22) / `UKULELE` (23) / `QUY_MAP` (24) | `getIdTask == TASK_16_3` **và** `Util.isTrue(25, 100)` | **2014** Vỏ đạn khắc dấu | `TASK_16_3` (cần 5) | [27 §4 NV 16 b3](27-du-lieu-nhiem-vu-moi.md), [25 dòng 2014](25-bang-id-vat-pham-moi.md) |
| `TOBI` (81) | `getIdTask == TASK_32_4` | **2025** Mảnh Ký Ức Vỡ | `TASK_32_4` (cần 3) | [27 §4 NV 32 b4](27-du-lieu-nhiem-vu-moi.md), [25 dòng 2025](25-bang-id-vat-pham-moi.md) |

Giữ nguyên hai nhánh có sẵn: mob 1/2/3 → **2009** ở `TASK_2_2`; mob 7/8/9 → **2010** ở `TASK_5_0`.

### 4.2 `map/Zone.getItemMapsForPlayer` — thêm 2 lớp lọc

1. **Mở rộng bộ lọc "chỉ chủ nhân thấy"** từ `{2009, 2010}` thành `{2009, 2010, 2014, 2025}` — bốn món
   này đều do quái rơi riêng cho người giết (`playerId` là id người chơi).
2. **Thêm bộ lọc "chỉ hiện khi đang đúng bước"** cho ba món **rải sẵn trên bản đồ** (`playerId = -1`,
   ai cũng nhặt được nếu không lọc):

| Item | Map | Chỉ hiện khi `getIdTask` bằng | Bước |
|---|---|---|---|
| **2008** Mảnh Ký Ức 7 | 78 | `ConstTask.TASK_45_1` | `TASK_45_1` |
| **2026** Mảnh Ký Ức Đóng Băng | 110 | `ConstTask.TASK_34_3` | `TASK_34_3` |
| **2023** Bản thiết kế bản sao | 166 | `ConstTask.TASK_29_2` | `TASK_29_2` |

`getIdTask(player)` được tra **một lần** ở đầu hàm (biến `idTaskOfPlayer`) vì hàm này chạy mỗi lần gửi
danh sách vật phẩm của khu.

---

## 5. Điểm móc CHƯA nối được và vì sao

| # | Điểm móc | Vì sao chưa nối | Cần ai làm |
|---|---|---|---|
| 1 | **Bản đồ kho báu** (`map/phoban/BanDoKhoBau.java`, `ConstMap.MAP_BAN_DO_KHO_BAU = 4`) | `TaskService.checkDoneTaskDungeon` **chỉ nhận 3 loại phó bản**: `MAP_DOANH_TRAI`, `MAP_CON_DUONG_RAN_DOC`, `MAP_KHI_GAS_HUY_DIET`. Không có bước nhiệm vụ nào trong [27 §4](27-du-lieu-nhiem-vu-moi.md) gắn với bản đồ kho báu — nó chỉ xuất hiện ở cột **phần thưởng** của NV 21 ("2×Bản đồ kho báu"). Thêm lời gọi ở đây sẽ là **lời gọi chết** (rơi vào `default: break`) nên tôi **không thêm** | Nếu thiết kế thật sự muốn một bước "hoàn thành bản đồ kho báu" thì phải **thêm `case` vào `TaskService`** (file cấm sửa) và thêm bước vào SQL |
| 2 | **`services_dungeon/` khí gas** | Cờ kết thúc `hatchiyatchDead` nằm ở `map/phoban/DestronGas.java`, còn `DestronGasService` chỉ lo việc **mở** phó bản. Đã nối ở `DestronGas` (điểm móc #3) — `services_dungeon` **không có chỗ nào đúng nghĩa "đã phá xong"** | Không cần làm gì thêm — coi như đã nối qua `DestronGas` |
| 3 | *(không phải điểm móc, nhưng chặn cứng)* **`TASK_6_1` / `TASK_15_1`** | Cần boss `-2000` Kẻ Thu Gom và `-2001` Jaco Vô Thức | Nhóm boss **đã tạo** `boss/quest/KeThuGom.java`, `boss/quest/JacoVoThuc.java` và `BossID.KE_THU_GOM/JACO_VO_THUC` — cần xác nhận đã đăng ký vào `BossesData` + `BossManager` và có `mob_template`/spawn thật |

Ngoài ra, **các mục trong [27 §7.3](27-du-lieu-nhiem-vu-moi.md#73-lỗi-cũ-chặn-cứng--phải-sửa-trước-khi-bật-tuyến)
nằm trong `boss/**` và `npc_list/Osin.java`, `npc_list/Bill.java`, `npc_list/GiuMaDauBo.java`,
`npc_list/QuaTrung.java` không thuộc phạm vi lượt này** (danh sách cấm sửa / nhóm khác). Riêng
`npc_list/Osin.java`, `npc_list/Cui.java`, `npc_list/Jaco.java`… đã có dấu vết sửa của nhóm khác trong
`git status`, cần đối chiếu lại khi gộp.

---

## 6. Chỗ nghi ngờ

| # | Vấn đề | Mức độ | Đề nghị |
|---|---|---|---|
| 1 | ⚠️ **`checkDoneTaskLearnSkill` kiểm SAI trường id.** `TaskService:1668` kiểm `skill.skillId == 1 \|\| 3 \|\| 5`. Nhưng `skillId` là **id dòng kỹ năng theo CẤP** (khóa toàn cục lấy từ JSON cột `skills` của `skill_template`, `Manager.java:459`), không phải id loại kỹ năng. Đối chiếu `database team2026.sql:9793+`: Kamejoko **cấp 1 có `skillId = 7`**, còn `skillId` 1/3/5 là **Chiêu đấm Dragon cấp 2/4/6**. Hằng `Skill.KAMEJOKO = 1`, `MASENKO = 3`, `ANTOMIC = 5` là giá trị của **`skill.template.id`** | **Chặn bước** — `TASK_10_1` (học chưởng cấp 1) sẽ **không xong khi học Kamejoko**, mà lại **xong nhầm khi nâng Chiêu đấm Dragon lên cấp 2** | Sửa trong `TaskService` (file tôi không được đụng) thành `skill.template.id == 1 \|\| 3 \|\| 5`. Điểm móc đã nối đúng chỗ, chỉ chờ sửa điều kiện |
| 2 | **`checkDoneTaskWinMatch` bỏ qua tham số `typePvp`** — mọi loại trận đều tính `TASK_44_2`. Tôi vẫn truyền 0/1/2 để sau này lọc được | Thấp | Nếu chủ dự án muốn `TASK_44_2` chỉ tính đấu trường Sinh Tử thì lọc theo `typePvp` trong `TaskService` |
| 3 | **`TASK_29_2` chưa xử lý hết giờ trọn vẹn.** [27 §4](27-du-lieu-nhiem-vu-moi.md) yêu cầu khi hết 6 phút thì *"count = 0, **xóa item 2023 đang cầm**, **đẩy người chơi về map 97**"*. `updateTimedSubTaskTick` hiện **chỉ đặt `count = 0`** | Trung bình | Hai việc còn lại phải thêm vào `TaskService` (cấm sửa) hoặc làm ở nơi gọi — tôi **không tự thêm** vì đó là logic nhiệm vụ |
| 4 | **`ItemMap` 2023 trên map 166 phải "không tự biến mất sau 50 s"** ([27 §7.4](27-du-lieu-nhiem-vu-moi.md#74-dữ-liệu-map_template-phải-bổ-sung)). Bộ lọc hiển thị tôi đã làm, nhưng **việc rải item và tuổi thọ item chưa có ai làm** | **Chặn bước** | Cần một chỗ sinh `ItemMap` 2023 (5–8 viên/khu) trên map 166 kèm `ItemMapService` không hết hạn |
| 5 | **`Zone.getItemMapsForPlayer` gọi `getIdTask` mỗi lần gửi danh sách vật phẩm.** Hàm chỉ đọc 3 trường, không khóa, không truy vấn DB | Thấp | Chấp nhận được. Nếu đo thấy nóng thì cache theo lượt gửi |
| 6 | **Nút "Phá giới hạn (nhiệm vụ)" làm menu QuocVuong / ToSuKaio đổi số nút.** Client Unity vẽ menu theo số chuỗi server gửi nên không vỡ giao thức, nhưng người chơi sẽ thấy menu **lúc 3 nút lúc 4 nút** | Thấp | Đúng yêu cầu "chỉ hiện khi đang ở đúng bước". Nếu muốn ổn định, luôn hiện 4 nút và báo "chưa đến lúc" khi bấm |
| 7 | **`QuocVuong` chỉ hiện khi SM ≥ 17 tỷ và cần có mặt ở map 42/44** ([27 §7.3](27-du-lieu-nhiem-vu-moi.md#73-lỗi-cũ-chặn-cứng--phải-sửa-trước-khi-bật-tuyến), [27 §7.4](27-du-lieu-nhiem-vu-moi.md#74-dữ-liệu-map_template-phải-bổ-sung)). NV 33 ở mốc ~2,5–3 tỷ | **Chặn bước** `TASK_33_1`, `TASK_33_3` | Việc của nhóm NPC / dữ liệu `map_template` + `npc_template`, không phải điểm móc trigger |
| 8 | **`B13 TASK_35_1` chỉ được kiểm định kỳ trong `Player.update`**, tôi **không** gọi thêm `checkDoneTaskTogether(pl, zone, 2, true)` ngay sau `SnakeWayService.openConDuongRanDoc` | Thấp | Nhịp 1 giây là đủ nhanh; thêm lời gọi thứ hai chỉ để giảm 1 giây độ trễ, đổi lại là một chỗ logic trùng lặp |
| 9 | **`NPoint.increasePoint` type 2 nay gọi `checkDoneTaskNangCS` ba lần** (2 lần cũ trong nhánh type 2 + 1 lần mới ở cuối qua `checkDoneTaskBasePoint`) | Thấp | Vô hại — hàm tự kiểm `isCurrentTask` và điều kiện `dameg >= 30`. Không xóa hai lần gọi cũ để **không đổi hành vi hiện có** |
| 10 | **`TASK_16_3` không ràng buộc map.** [27 §4](27-du-lieu-nhiem-vu-moi.md) chỉ ghi "rơi 25% từ mob 22/23/24", không nói map. Bước trước đó (`TASK_16_2`) diễn ra ở map 30/34/38 | Thấp | Nếu muốn chặt, thêm điều kiện map 30/34/38 vào `dropItemTask`. Hiện đang **cố ý rộng** để chống kẹt |
| 11 | **Chưa chạy thử trên server thật** — mới chỉ biên dịch sạch 562 file (JDK 17) | Trung bình | Cần một lượt chơi thử sau khi import `02-nhiem-vu-moi.sql` + `03-reset-tien-do.sql` |

---

*Hết file 31.*
