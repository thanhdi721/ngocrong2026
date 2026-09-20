# 30 — Boss và NPC bổ sung cho tuyến nhiệm vụ mới

> Vá nốt những chỗ **chặn cứng** mà [29 §9 mục 1](29-taskservice-tuyen-moi.md) đã báo:
> `TASK_6_1` và `TASK_15_1` không qua được vì boss −2000 / −2001 chưa tồn tại,
> và một loạt bước A3 không qua được vì NPC chưa có lớp Java hoặc chưa đứng trên map nào.
>
> Đặc tả gốc: [20a §C, §D](../2-thiet-ke-nhiem-vu-moi/20a-chi-tiet-nhiem-vu-00-15.md) ·
> [20b §NV 20, §NV 29](../2-thiet-ke-nhiem-vu-moi/20b-chi-tiet-nhiem-vu-16-31.md) ·
> Kiến trúc boss nhiệm vụ: [28](28-boss-ban-nhiem-vu.md) ·
> Id vật phẩm: [25](25-bang-id-vat-pham-moi.md)

## Mục lục

1. [Tóm tắt](#1-tóm-tắt)
2. [Hai boss mới](#2-hai-boss-mới)
3. [NPC: hằng số, lớp Java và cách đăng ký](#3-npc-hằng-số-lớp-java-và-cách-đăng-ký)
4. [Đặt NPC lên bản đồ — toạ độ và lý do](#4-đặt-npc-lên-bản-đồ--toạ-độ-và-lý-do)
5. [Danh sách file đã thêm / đã sửa](#5-danh-sách-file-đã-thêm--đã-sửa)
6. [Những thứ thiết kế cần mà code vẫn chưa có](#6-những-thứ-thiết-kế-cần-mà-code-vẫn-chưa-có)
7. [Chỗ nghi ngờ / cần chủ dự án quyết](#7-chỗ-nghi-ngờ--cần-chủ-dự-án-quyết)

---

## 1. Tóm tắt

| Hạng mục | Kết quả |
|---|---|
| Boss mới đã dựng | **2** — −2000 Kẻ Thu Gom (1 hình dạng), −2001 Jaco (2 hình dạng) |
| Dải id đã dùng | **−2000, −2001** (còn trống −2002 … −2099) |
| `BossData` mới | **3** |
| Hằng `ConstNpc` mới | **2** — `BERRY = 71`, `GRANOLA = 76` |
| Lớp NPC mới | **2** — `npc_list/Berry.java`, `npc_list/Granola.java` |
| Lớp NPC sửa | **1** — `npc_list/Jaco.java` (trước đây **không gọi trigger nhiệm vụ nào**) |
| NPC đặt lên map | **9 vị trí mới** trên **8 map** (SQL `04-npc-tren-map.sql`) |
| Biên dịch | **Sạch, 562 file** (JDK 17; trước đó 558 + 4 file mới) |

**Kiểm tra trùng id boss:** đã quét lại toàn bộ `BossID.java`. Hai id −2000 / −2001 chưa
được dùng ở đâu; id âm gần nhất hai phía là `SON_TINH = -354` và `XEN_BO_HUNG_NV = -2100`.
Giá trị cũng khớp đúng hai hằng cục bộ `TaskService.BOSS_KE_THU_GOM` / `BOSS_JACO_VO_THUC`
mà [29 §8 mục 5](29-taskservice-tuyen-moi.md) đã khai sẵn — `TaskService` **không phải sửa một dòng nào**.

**Kiểm tra trùng id NPC:** `ConstNpc` trước đây nhảy cóc 70 → 72 → 74 → 75, hai số **71 và 76
chưa có hằng nào chiếm**. Bảng `npc_template` thì **đã có sẵn** cả hai (71 Berry, 76 Granola),
nên **không cần thêm tài nguyên client**.

---

## 2. Hai boss mới

Cả hai kế thừa `boss/quest/QuestBoss.java` — đúng kiến trúc đã chốt ở [28 §2](28-boss-ban-nhiem-vu.md).
Điểm khác duy nhất so với 6 con −2100…−2105: hai con này **không có bản gốc trong game**,
nên `worldBossIds` để rỗng (không có boss thế giới nào phải tránh đè lên).

### 2.1 Bảng chỉ số

| Boss id | `currentLevel` | Tên hiển thị | Tạo hình (head/body/leg) | HP | Dame | Skill | Map | Nghỉ | Dùng ở | Rơi gì |
|---|---|---|---|---:|---:|---|---|---|---|---|
| **−2000** | 0 | Kẻ Thu Gom | 144 / 145 / 146 *(mượn NPC 26 Độc Nhãn)* | 80.000 | 400 | Chiêu đấm Dragon 3 | 4 Rừng xương · 12 Vực maima · 18 Rừng thông Xayda | `REST_2_M` (120 s) | **NV 6 bước 1** (`TASK_6_1`) | **không rơi gì** |
| **−2001** | 0 | Jaco Mất Ký Ức | 624 / 625 / 626 *(mượn NPC 63 Jaco)* | 1.200.000 | 3.000 | Dragon 5, Quả cầu kênh khí 3 | 27 Rừng Bamboo · 31 Núi hoa vàng · 35 Rừng cọ | `REST_5_M` (300 s) | *(bước đệm — chưa tính xong nhiệm vụ)* | **không rơi gì** |
| **−2001** | 1 | Jaco Vô Thức | 624 / 625 / 626 | 2.500.000 | 5.000 | Dragon 7, Quả cầu kênh khí 5, Dịch chuyển tức thời 3 | *(theo hình dạng 0)* | — (`ANOTHER_LEVEL`) | **NV 15 bước 1** (`TASK_15_1`) | **không rơi gì** |

Mọi con số lấy **nguyên** theo [20a §C](../2-thiet-ke-nhiem-vu-moi/20a-chi-tiet-nhiem-vu-00-15.md)
và bảng chi tiết trong phần NV 15 của cùng file. Đã đối chiếu lại:

- **−2000 HP 80.000 / dame 400** so với mốc sức mạnh ~34.000 khi người chơi tới NV 6 —
  hạ trong khoảng 1 phút, và 400 dame không one-shot người chơi chương 1.
- **−2001 tổng HP 3,7 triệu / dame 3.000 → 5.000** so với mốc ~2.000.000 SM ở NV 15.
  20a ghi rõ lý do dame để thấp hơn Dr.Kôrê (−31, dame 12.000) rất nhiều: ở mốc này
  người chơi còn mặc đồ cấp 4–5, HP thực chỉ 20.000–40.000.

HP là `int[]` (trần 2.147.483.647) — cả ba con số đều nằm rất xa trần, **không có nguy cơ tràn số âm**.

### 2.2 Hai nguyên tắc đã chốt, và chỗ code tôn trọng chúng

> **NT1 — boss trong bước nhiệm vụ bắt buộc chỉ rơi ĐỒ NHIỆM VỤ.** Không trang bị, không vàng.
> **NT2 — đồ xịn vẫn chỉ đến từ boss thế giới bản gốc.**

Cả hai con **không rơi gì cả**: `getQuestItemId(level)` giữ nguyên mặc định `-1` của `QuestBoss`,
nên `dropQuestItem` thoát ngay. Đây là cố ý, đúng 20a §NV 15:

> "Mảnh Ký Ức #2 trao qua `rewardDoneTask`, **không** trao qua `Boss.reward`, tránh người chơi nhặt hụt."

Phần thưởng của NV 6 và NV 15 nằm trong bảng `task_main_reward` (file `02-nhiem-vu-moi.sql`),
`TaskService.applyReward` trao thẳng vào hành trang — không ai cướp được, không rơi xuống đất.

### 2.3 Chống farm — bốn lớp chặn, thừa hưởng nguyên từ `QuestBoss`

1. **Không có người đúng bước thì boss không tồn tại.** `QuestBoss.rest()` quét
   `Client.getPlayersSnapshot()` mỗi 3 giây, chỉ đổi sang `RESPAWN` khi thấy một người chơi có
   `playerTask.taskMain.id` nằm trong danh sách (`{6}` cho −2000, `{15}` cho −2001) **và** đang đứng
   trong map hợp lệ **và** khu đó chưa có bản khác cùng id. Người chơi 90 tỉ SM đi ngang map 4
   sẽ **không** thấy con −2000 nào.
2. **Không có gì để farm.** Bảng rơi đồ rỗng (§2.2).
3. **Không có thông báo toàn server.** `isNotifyDisabled = true` và `QuestBoss.die()` bỏ hẳn câu
   `ServerNotify "Đã tiêu diệt được…"`.
4. **Tự dọn dẹp.** Khu trống người 5 phút → rời map. Chờ khu hợp lệ quá 10 phút → bỏ lượt, về `REST`.

### 2.4 Đè lên boss khác?

| Boss | `mapJoin` | Boss khác dùng chung map | Xử lý |
|---|---|---|---|
| −2000 | 4, 12, 18 | chỉ boss lang thang (Ăn Trộm −365, Ở Dơ) | `isZoneOccupied` bỏ qua boss lang thang → không chặn nhau. Ba map này **không có boss thế giới cố định nào**. |
| −2001 | 27, 31, 35 | không có boss nào | trống hẳn |

Chiều ngược lại cũng an toàn: `Boss.joinMap()` của boss thế giới vốn đã bỏ qua mọi khu
`!getBosses().isEmpty()`, nên chúng tự tránh khu đang có boss nhiệm vụ.

### 2.5 Cách đăng ký

**`models/boss/BossID.java`** — thêm 2 hằng (chỉ thêm, không đụng id cũ):

```java
//========================BOSS RIÊNG CHƯƠNG 1–2 (tuyến nhiệm vụ mới)========================
public static final int KE_THU_GOM = -2000;       // NV 6 bước 1 — 1 hình dạng
public static final int JACO_VO_THUC = -2001;     // NV 15 bước 1 — 2 hình dạng
```

**`models/boss/BossesData.java`** — thêm 3 `BossData` (`KE_THU_GOM`, `JACO_VO_THUC_1`,
`JACO_VO_THUC_2`), đặt ngay **trước** khối Heart. Hình dạng đầu dùng `secondsRest`,
hình dạng sau dùng `AppearType.ANOTHER_LEVEL` để `rest()` không bao giờ gọi thẳng nó ra.

**`models/boss/Boss_Manager/BossManager.java`** — 2 `import`, 2 `case` trong `createBoss(int)`,
2 dòng trong `loadBoss()`:

```java
this.createBoss(BossID.KE_THU_GOM, 3);
this.createBoss(BossID.JACO_VO_THUC, 3);
```

> **Vì sao 3 bản chứ không phải 1 như 20a đề xuất.** NV 6 và NV 15 nằm trong chương 1–2 —
> **mọi** nhân vật mới đều phải đi qua, nên đây là hai cửa đông người nhất cả tuyến.
> Với 1 bản + hồi sinh 120 s (−2000) hoặc 300 s (−2001), 50 người cùng làm NV 15 phải xếp
> hàng hơn 4 tiếng. Ba bản + mỗi bản vào một khu khác nhau thì hàng đợi gần như biến mất.
> Con số 3 chỉnh tự do ở `loadBoss()`. Giống hệt lý lẽ ở [28 §6.3](28-boss-ban-nhiem-vu.md).

**Gọi `checkDoneTaskKillBoss`:** không thêm chỗ nào mới — `QuestBoss.reward(Player)` đã gọi
sẵn cho mọi lớp con. `TaskService` đã có `case BOSS_KE_THU_GOM` và `case BOSS_JACO_VO_THUC`
(bao gồm điều kiện `currentLevel == 1` của −2001), nên hai bước này chạy ngay khi server khởi động lại.

---

## 3. NPC: hằng số, lớp Java và cách đăng ký

### 3.1 Bảng bốn NPC được nhắc trong yêu cầu

| NPC | id | `npc_template` có sẵn? | Hằng `ConstNpc` | Lớp `npc_list/` | Đăng ký `NpcFactory` | Dùng ở bước nào | Tình trạng |
|---|---:|---|---|---|---|---|---|
| **Berry** | 71 | ✔ head 1015 / body 1016 / leg 1017, avatar 9076 | **thêm mới** `BERRY = 71` | **thêm mới** `Berry.java` | **thêm mới** | `TASK_20_0`, `TASK_20_1` (rẽ nhánh), `TASK_48_0` | xong |
| **Granola** | 76 | ✔ head 2018 / body 2019 / leg 2020, avatar 15233 | **thêm mới** `GRANOLA = 76` | **thêm mới** `Granola.java` | **thêm mới** | `TASK_20_2`, `TASK_20_5` | xong |
| **Jaco** | 63 | ✔ head 624 / body 625 / leg 626, avatar 5833 | đã có `JACO = 63` | **đã có nhưng hỏng** | đã có | `TASK_3_0`, `TASK_7_2`, `TASK_15_2`, `TASK_48_2`, `TASK_48_5` | **đã sửa** |
| **Dr. Myuu** | 83 | ✔ head 258 / body 259 / leg 260, avatar 12097 | đã có `DR_MYUU = 83` | ✔ đã có, **đã gọi** `checkDoneTaskTalkNpc` | đã có | `TASK_29_3`, `TASK_46_0` | **chỉ thiếu chỗ đứng** → SQL |

> **Đính chính so với 20a §D và 20b §NV 29.** Hai tài liệu thiết kế ghi "NPC 63 Jaco chưa có class"
> và "`npc_list/DrMyuu.java` chưa bao giờ được spawn, cần thêm nhánh `checkDoneTaskTalkNpc`".
> Theo nguyên tắc **tin mã nguồn**, đọc lại thì thấy:
> - `npc_list/Jaco.java` **có tồn tại** và **có** đăng ký ở `NpcFactory` (`case ConstNpc.JACO`).
>   Lỗi thật là: nó **không gọi** `TaskService.checkDoneTaskTalkNpc`, và `openBaseMenu` chỉ có
>   `case 24` / `case 139` nên ở map 25, 26, 42, 43, 44 nó **im lặng hoàn toàn** — bấm vào không ra gì.
> - `npc_list/DrMyuu.java` **đã** gọi `checkDoneTaskTalkNpc` ngay đầu `openBaseMenu`.
>   Nó chỉ thiếu **chỗ đứng trên bản đồ** — sửa hoàn toàn bằng SQL, không phải bằng Java.

### 3.2 Ba lớp NPC

**`npc_list/Berry.java` (mới)** — điểm rẽ nhánh 1 của cả tuyến.
Bắt chước đúng khuôn của `Potage` (rẽ nhánh 2) và `DaiThienSu` (rẽ nhánh 3):
menu rẽ nhánh dùng **id menu riêng** `MENU_RE_NHANH_NV20 = 2200` thay vì `BASE_MENU`,
để không lẫn với bất kỳ menu nào khác của NPC.

```java
if (TaskService.gI().checkDoneTaskTalkNpc(player, this)) return;   // TASK_20_0 / TASK_48_0
if (TaskService.gI().getIdTask(player) == ConstTask.TASK_20_1) {
    this.createOtherMenu(player, MENU_RE_NHANH_NV20, "...", "Đi theo\nGranola", "Báo cho\nJaco");
}
```

`confirmMenu` chuyển thẳng lựa chọn sang `TaskService.checkDoneTaskConfirmMenuNpc(player, this, (byte) select)`.
**Thứ tự nút bắt buộc**: `0` = Granola (giữ nhiệm vụ 20), `1` = Jaco (`switchTaskBranch` sang 48) —
đúng thứ tự mà `TaskService` đang chờ.

**`npc_list/Granola.java` (mới)** — hai bước A3 thuần (`TASK_20_2`, `TASK_20_5`).
Gọi `checkDoneTaskTalkNpc` rồi mở một menu tối thiểu một nút; có thêm một câu nhắc riêng khi
người chơi đang ở giữa chừng (`TASK_20_3` / `TASK_20_4`) để không bị cụt hội thoại.

**`npc_list/Jaco.java` (sửa)** — hai thay đổi:
1. Gọi `TaskService.gI().checkDoneTaskTalkNpc(player, this)` **đầu tiên** trong `openBaseMenu`,
   thoát sớm nếu trả `true` (vừa xong một bước) — cùng khuôn với `Cui`, `DrMyuu`, `BaHatMit`.
2. Thêm nhánh `case 25, 26` và `case 42, 43, 44` cho `openBaseMenu` / `confirmMenu`, để Jaco
   có hội thoại ở những map mới. Hai nhánh cũ (`24` đi Potaufeu, `139` tàu vũ trụ) **giữ nguyên
   từng dòng**, không đụng.

Đăng ký ở `NpcFactory.createNPC`:

```java
case ConstNpc.BERRY   -> new Berry(mapId, status, cx, cy, tempId, avatar);
case ConstNpc.GRANOLA -> new Granola(mapId, status, cx, cy, tempId, avatar);
```

> **`ConstNpc.BERRY` / `GRANOLA` không xung đột với `TaskService.NPC_BERRY` / `NPC_GRANOLA`.**
> Hai hằng cục bộ trong `TaskService` cùng giá trị 71 / 76 và vẫn được `switch` ở đó dùng.
> Có thể đổi `TaskService` sang dùng hằng chung sau, nhưng **file đó đang trong danh sách cấm sửa**
> nên lần này để nguyên — biên dịch và chạy đều đúng.

---

## 4. Đặt NPC lên bản đồ — toạ độ và lý do

File: **`SRC/sql/patch/04-npc-tren-map.sql`**. Chỉ ghi lại cột `map_template`.`npcs` của **8 map**.

### 4.1 Định dạng và cách đọc

`npcs` là chuỗi JSON `[[npc_template_id, x, y], …]`
([02b §5](../1-he-thong-hien-tai/02b-database-du-lieu-template.md)).
`Manager.loadDatabase` (dòng ~878) đọc bằng `JSONValue.parse(npcs.replaceAll("\"",""))`
rồi `Byte.parseByte(dtn[0])`, `Short.parseShort(dtn[1])`, `Short.parseShort(dtn[2])`.
Mọi id dưới đây (63, 70, 76, 83) đều lọt trong `byte`.

### 4.2 Chín vị trí mới

| Map | Tên map | NPC thêm | x | y | Vì sao chỗ này | Bước được mở |
|---:|---|---|---:|---:|---|---|
| 25 | Trạm tàu vũ trụ (Namếc) | 63 Jaco | 216 | 336 | nền phẳng y=336 (x 24→552); khoảng trống rộng nhất giữa Rồng Omega (84) và Cargo (348), cách đều 132 px | `TASK_7_2`, `TASK_15_2` |
| 26 | Trạm tàu vũ trụ (Xayda) | 63 Jaco | 360 | 336 | cùng nền y=336; giữa Cui (228) và Uron (510) | `TASK_7_2`, `TASK_15_2` |
| 42 | Vách núi Aru | 63 Jaco | 312 | 432 | thềm thấp y=432 (x 192→480), xa Bà Hạt Mít (588) và Ghi danh (1015), xa con Máy đo sức mạnh (mob 117 ở x=777) | `TASK_3_0` |
| 42 | Vách núi Aru | 42 Quốc Vương | 216 | 432 | cùng thềm, cách Jaco 96 px | `TASK_33_1`, `TASK_33_5` |
| 43 | Vách núi Moori | 63 Jaco | 420 | 432 | thềm y=432 (x 168→552); tránh Quốc Vương đã đứng ở 247 (cách 173 px) | `TASK_3_0` |
| 44 | Vách núi Kakarot | 63 Jaco | 300 | 432 | thềm y=432 (x 192→552) | `TASK_3_0` |
| 44 | Vách núi Kakarot | 42 Quốc Vương | 420 | 432 | cùng thềm, cách Jaco 120 px | `TASK_33_1`, `TASK_33_5` |
| 160 | Khu hang động | 76 Granola | 1080 | 432 | cùng gờ đá y=432 (x 960→1272) với Bardock (1189) và Berry (1239) — đúng bối cảnh 20b; cách Bardock 109 px, Berry 159 px | `TASK_20_2`, `TASK_20_5` |
| 166 | Phòng thí nghiệm Myuu | 83 Dr. Myuu | 624 | 240 | map trống hoàn toàn; bệ phẳng rộng nhất ở khu giữa là y=240 (x 576→672), chọn giữa bệ | `TASK_29_3`, `TASK_46_0` |
| 14 | Làng Kakarot | 70 Bardock | 650 | 408 | nền phẳng y=408 (x 24→1200); NPC cũ ở 252 / 286 / 396 / 953 → chỗ trống lớn nhất | `TASK_39_5` |

**Toạ độ được kiểm bằng chính thuật toán của server**, không ước lượng bằng mắt: đã đọc
`data/map/tile_map_data/<mapId>` và `data/map/tile_set_info` đúng như `Manager.readTileMap` /
`readTileIndexTileType`, rồi chạy lại `Map.yPhysicInTop(x, y)`. Cả 9 điểm đều thoả
`yPhysicInTop(x, y) == y` — NPC đứng đúng mặt đất, không lơ lửng, không lún vào đá.
(Phép kiểm này cũng cho kết quả `OK` với **mọi** NPC sẵn có trên 8 map đó, nên thuật toán đọc là đúng.)

Khoảng cách tới NPC gần nhất đều **lớn hơn 60 pixel** — bán kính mở hội thoại trong
`Map.getNpc(player, tempId)` — nên không có chuyện bấm vào NPC này lại mở menu NPC kia.

### 4.3 NPC 71 Berry — không cần SQL

Map 160 **đã có sẵn** `[71, 1239, 432]` trong `npcs`. Trước đây Berry vẫn hiện ra trên màn hình
nhưng rơi vào nhánh `default` của `NpcFactory.createNPC` nên không có `openBaseMenu` riêng.
Thêm lớp `Berry.java` là đủ, SQL **không đụng tới** map 160 ngoài việc chèn Granola.

### 4.4 Cấu trúc file SQL

| Khối | Nội dung |
|---|---|
| (1) | `SELECT` kiểm tra trước — cột `khop_ban_goc` phải bằng 1 ở cả 8 dòng, nếu không thì `npcs` đã bị sửa tay so với bản dump và **không được chạy tiếp** |
| (2) | 7 lệnh `UPDATE` bắt buộc (Jaco ×5 map, Granola, Dr. Myuu) |
| (2b) | 3 lệnh `UPDATE` cho hai lỗ hổng tìm thêm được (§6) — xoá khối này nếu chủ dự án không muốn |
| (3) | `SELECT` kiểm tra sau + một câu đếm nhanh 9 map có NPC 63 / 76 / 83 |
| (4) | Khối **lùi lại**, đã comment sẵn, trả 8 map về đúng giá trị bản dump gốc |
| (5) | Khối **tuỳ chọn đã comment**: waypoint vào/ra map 166 (xem §7 mục 1) |

Chạy lại nhiều lần được: mỗi `UPDATE` ghi **giá trị đầy đủ cuối cùng**, không nối thêm.
Phải **tắt server** trước khi chạy vì `map_template` chỉ được đọc một lần lúc khởi động.

---

## 5. Danh sách file đã thêm / đã sửa

### File mới (5)

| File | Nội dung |
|---|---|
| `SRC/src/nro/models/boss/quest/KeThuGom.java` | −2000, 1 hình dạng, `TASK = {6}` |
| `SRC/src/nro/models/boss/quest/JacoVoThuc.java` | −2001, 2 hình dạng, `TASK = {15}` |
| `SRC/src/nro/models/npc_list/Berry.java` | NPC 71, menu rẽ nhánh 1 (`MENU_RE_NHANH_NV20 = 2200`) |
| `SRC/src/nro/models/npc_list/Granola.java` | NPC 76, hai bước A3 |
| `SRC/sql/patch/04-npc-tren-map.sql` | 9 vị trí NPC mới trên 8 map, kèm kiểm tra và khối lùi lại |

### File đã sửa (6)

| File | Sửa gì |
|---|---|
| `SRC/src/nro/models/boss/BossID.java` | thêm 2 hằng id (chỉ thêm) |
| `SRC/src/nro/models/boss/BossesData.java` | thêm 3 `BossData` (chỉ thêm) |
| `SRC/src/nro/models/boss/Boss_Manager/BossManager.java` | 2 `import`, 2 `case`, 2 dòng `loadBoss()` |
| `SRC/src/nro/models/consts/ConstNpc.java` | thêm `BERRY = 71`, `GRANOLA = 76` (chỉ thêm) |
| `SRC/src/nro/models/npc/NpcFactory.java` | 2 `import`, 2 `case` |
| `SRC/src/nro/models/npc_list/Jaco.java` | gọi `checkDoneTaskTalkNpc`; thêm nhánh map 25/26/42/43/44 |

**Không đụng tới**: `services/TaskService.java`, `consts/ConstTask.java`,
`SRC/sql/patch/01|02|03-*.sql`, `map/service/ChangeMapService.java`, `shop/**`, `database/**`,
và toàn bộ lớp boss thế giới bản gốc.

**Biên dịch**: `javac -nowarn -encoding UTF-8 -cp "lib/*"` trên **562 file** — **sạch**,
chỉ còn 2 dòng `Note:` về unchecked vốn có từ trước.

---

## 6. Những thứ thiết kế cần mà code vẫn chưa có

Đã dò **toàn bộ** cặp *(npc, map)* mà `TaskService.checkDoneTaskTalkNpc` đòi hỏi, đối chiếu với
cột `npcs` của cả 169 map trong bản dump. Kết quả: đúng **9 chỗ thiếu**, và file SQL này vá cả 9.

| # | Thiếu gì | Hậu quả nếu bỏ qua | Đã vá? |
|---|---|---|---|
| 1 | NPC 63 Jaco ở map 42, 43, 44 | người chơi **cả ba hành tinh** kẹt `TASK_3_0` (NV 3 bước 0) | ✔ khối (2) |
| 2 | NPC 63 Jaco ở map 25, 26 | người chơi **Namếc / Xayda** kẹt `TASK_7_2` và `TASK_15_2` | ✔ khối (2) |
| 3 | NPC 76 Granola ở map 160 | kẹt `TASK_20_2` / `TASK_20_5` | ✔ khối (2) |
| 4 | NPC 83 Dr. Myuu ở map 166 | kẹt `TASK_29_3` / `TASK_46_0` | ✔ khối (2) — nhưng xem §7 mục 1 |
| 5 | NPC 42 Quốc Vương ở map 42 và 44 | người chơi **Trái Đất / Xayda** kẹt `TASK_33_1` và `TASK_33_5` (hắn chỉ có ở map 43 Namếc) | ✔ khối (2b) |
| 6 | NPC 70 Bardock ở map 14 Làng Kakarot | kẹt `TASK_39_5` | ✔ khối (2b) |

### 6.1 Việc thiết kế cần mà **chưa** làm trong lần này

| # | Việc | Thuộc doc | Vì sao chưa làm |
|---|---|---|---|
| a | **Map 166 không có đường vào.** Không waypoint, không đoạn mã nào đưa người chơi tới. `ChangeMapService` chỉ **khoá** map này theo `TASK_29_0`, không có chỗ nào **mở** đường tới. | 20b §NV 29 ghi đúng điều này | Quyết định thiết kế (cửa đặt ở đâu, có cần vật phẩm "Thẻ từ" không). Đã soạn sẵn SQL waypoint 97 ↔ 166 trong khối (5), **đã comment**, chờ chủ dự án duyệt. **Đây là chỗ chặn nghiêm trọng nhất còn lại.** |
| b | **Map 165 Sa mạc hoang vu cũng không có waypoint nào.** Chỉ vào được qua menu "Bình hút năng lượng" của Ôsin ở map 52 (chuỗi sự kiện Mabư). | `TASK_36_4` nhận map 165 làm "đường vòng" | Đường vòng này chỉ mở khi chuỗi Mabư mở — cần chủ dự án xác nhận có chấp nhận không. |
| c | **Rơi item nhiệm vụ trên map 166** (item 2074 "Bản thiết kế bản sao" ×5, `TASK_29_2`) | 20b §NV 29 ghi chú (b) | Phải sinh `ItemMap` khi người chơi vào map — thuộc `Zone` / `ChangeMapService`, **nằm trong danh sách cấm sửa** lần này. |
| d | **Menu "Nở trứng" ở NPC 50 Quả Trứng** — nguồn đệ tử miễn phí cho `TASK_12_0` | 20a §D và §G mục 3 | Ảnh hưởng tới `PetService`; là một tính năng kinh tế riêng, nên tách ra. |
| e | **`npc_list/DaiThienSu` gọi `checkDoneTaskConfirmMenuNpc`** cho `TASK_47_0` | [29 §7 mục 21](29-taskservice-tuyen-moi.md) | Đã có nhóm khác làm — kiểm lại trước khi phát hành. |
| f | **Mob chương 2 quá yếu** so với mốc SM (mob 10/11/12 chỉ 1.000 HP trong khi người chơi lên 2 triệu SM ở NV 15) | 20a §G mục 5 | Sửa cột `mobs` của `map_template`, là một việc cân bằng riêng. |

---

## 7. Chỗ nghi ngờ / cần chủ dự án quyết

| # | Vấn đề | Tôi đã chọn gì | Vì sao cần bạn xem lại |
|---|---|---|---|
| 1 | **Map 166 vẫn chưa có đường vào.** Đặt được Dr. Myuu lên đó rồi nhưng người chơi không tới được. | Vá phần NPC; soạn sẵn waypoint 97 ↔ 166 ở khối (5) của SQL nhưng **để comment** | Cửa vào phòng thí nghiệm là chuyện kể chuyện: 20b nói người chơi "đột nhập" bằng **Thẻ từ giả (item 2073)**, tức là có thể bạn muốn một cửa **có điều kiện** (kiểm item trong `ChangeMapService`) chứ không phải waypoint thường. Map 97 lại đã dùng hết hai rìa cho waypoint đi 96 và 98 nên cửa buộc phải nằm **giữa map** — trông hơi lạ. **Cần bạn chốt**, nếu không NV 29, 45, 46 kẹt cứng. |
| 2 | **Mỗi boss 3 bản** thay vì 1 bản như 20a §C đề xuất | 3 | 20a viết "loadBoss() ×1", nhưng NV 6 và NV 15 là cửa mà **100% nhân vật** phải qua, khác hẳn mấy con −21xx ở chương 3–4. Nếu server bạn ít người thì hạ về 1–2 cho nhẹ; đông thì có thể nâng lên 5. |
| 3 | **Hình dạng 1 của −2001 dùng y hệt tạo hình hình dạng 0** | giữ nguyên `{624, 625, 626}` | 20a gợi ý "nếu muốn khác form 0 thì gán thêm `aura`/`eff` bằng một id hào quang **đã có sẵn** trong data client". Tôi không dám tự chọn id hào quang vì chưa dò được danh sách id nào chắc chắn có trong client bạn đang phát hành. Nói một id là tôi gắn. |
| 4 | **Hai boss không rơi gì cả** | không rơi | Đúng 20a §NV 15 ("trao qua `rewardDoneTask`") và đúng NT1. Nhưng hạ boss mà **không thấy gì rơi ra** thì cảm giác hụt — nếu muốn, có thể cho −2001 rơi 1 viên đậu thần (item 13) qua `getQuestItemId`, vẫn không phạm NT1 vì đậu thần không phải trang bị. Cần bạn xác nhận. |
| 5 | **Dr. Myuu đứng ở bệ y=240 (x=624)** của map 166 | bệ giữa map | Map 166 là một hang/phòng nhiều tầng rời rạc, tôi chọn bệ phẳng rộng nhất ở khu giữa. Bệ y=72 (x 696→888) rộng hơn (10 ô) nếu bạn muốn hắn đứng cao, nhìn xuống. Cả hai đều đã kiểm `yPhysicInTop`. |
| 6 | **Sửa luôn 2 chỗ ngoài yêu cầu** (Quốc Vương ở map 42/44, Bardock ở map 14) | sửa, đặt riêng ở khối (2b) | Cùng một cột, cùng một kiểu lỗi, cùng một lần tắt server — nhưng vẫn là thay đổi ngoài phạm vi. Xoá khối (2b) trước khi chạy là bỏ được, phần còn lại vẫn đúng. |
| 7 | **`MapService.isMapCadic(mapId)` trả `true` cho mọi map ≥ 165**, tức là gồm cả **166 Phòng thí nghiệm Myuu** | không sửa (ngoài phạm vi) | Hậu quả hiện tại nhỏ: Heart (−108108, `mapJoin[0] = 166`) bị loại khỏi danh sách boss mà `BossManager` gửi cho người chơi, và `Mob.java` dòng ~196 chạy nhánh "Cadic" trên map 166. Map 166 chưa có mob nên chưa lộ ra. Nhưng nếu sau này thêm map id ≥ 167 thì lỗi lan rộng — nên đổi thành `mapId == 165`. |
| 8 | **Chưa chạy thử trên server thật** | chỉ biên dịch sạch (562 file, JDK 17) và kiểm toạ độ bằng thuật toán của server | Cần một lượt chơi thử từ NV 0 tới NV 20 bằng **cả ba hành tinh** (Jaco và Quốc Vương đổi map theo hành tinh, đây chính là chỗ hay sót) sau khi import 01 → 04. |
| 9 | **`TaskService` vẫn dùng hằng cục bộ `NPC_BERRY` / `NPC_GRANOLA`** dù `ConstNpc` giờ đã có `BERRY` / `GRANOLA` | để nguyên | `TaskService.java` nằm trong danh sách cấm sửa lần này. Hai bên cùng giá trị nên chạy đúng, chỉ là trùng lặp. Lần dọn dẹp sau nên gộp về `ConstNpc`. |

---

*Hết file 30. Mã nguồn:
[`KeThuGom.java`](../../SRC/src/nro/models/boss/quest/KeThuGom.java) ·
[`JacoVoThuc.java`](../../SRC/src/nro/models/boss/quest/JacoVoThuc.java) ·
[`Berry.java`](../../SRC/src/nro/models/npc_list/Berry.java) ·
[`Granola.java`](../../SRC/src/nro/models/npc_list/Granola.java) ·
[`04-npc-tren-map.sql`](../../SRC/sql/patch/04-npc-tren-map.sql)*
