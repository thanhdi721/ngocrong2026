# 32 — Đường vòng cho người chơi lẻ: ba bước phó bản đòi bang hội

> Giải quyết **[27 §9 mục 9](27-du-lieu-nhiem-vu-moi.md#9-chỗ-nghi-ngờ)** — "rủi ro cao nhất của cả
> tuyến": `TASK_21_1` (Doanh Trại), `TASK_35_3` (Con đường rắn độc), `TASK_43_4` (Khí gas) đòi bang
> ≥ 5 thành viên / vào bang ≥ 2 ngày / là bang chủ, khiến người chơi không bang **kẹt cứng tuyến chính**.
>
> **Chủ dự án đã chốt: mở đường vòng.** File này ghi lại đúng những gì đã làm.
>
> Khuôn mẫu áp dụng: **"bước có hai cửa"** của [27 §6.1](27-du-lieu-nhiem-vu-moi.md#61-bước-có-hai-đường-hoàn-thành-theo-khung-giờ-phó-bản)
> (`TASK_36_2` / `TASK_37_1` / `TASK_37_2` — 1 boss = 20–30 quái khi ngoài khung giờ phó bản):
> **một hằng số, một `max_count`, nhiều điều kiện OR**, cửa "nội dung gốc" cộng trọn gói,
> cửa "cày quái" cộng từng con. `data_task` vẫn chỉ lưu `[id, index, count, lastTime]`, **không thêm cờ phụ**.
>
> Mã nguồn: [`services/TaskService.java`](../../SRC/src/nro/models/services/TaskService.java) ·
> [`npc_list/LinhCanh.java`](../../SRC/src/nro/models/npc_list/LinhCanh.java) ·
> Dữ liệu: [`SRC/sql/patch/02-nhiem-vu-moi.sql`](../../SRC/sql/patch/02-nhiem-vu-moi.sql)

## Mục lục

1. [Tóm tắt](#1-tóm-tắt)
2. [Nguyên tắc thiết kế](#2-nguyên-tắc-thiết-kế)
3. [NV 21 — Doanh trại Độc Nhãn](#3-nv-21--doanh-trại-độc-nhãn)
4. [NV 35 — Con đường rắn độc](#4-nv-35--con-đường-rắn-độc)
5. [NV 43 — Khí gas hủy diệt](#5-nv-43--khí-gas-hủy-diệt)
6. [So sánh công sức hai đường](#6-so-sánh-công-sức-hai-đường)
7. [Đã sửa những gì](#7-đã-sửa-những-gì)
8. [Điểm móc nhóm sau phải nối](#8-điểm-móc-nhóm-sau-phải-nối)
9. [Chỗ nghi ngờ](#9-chỗ-nghi-ngờ)

---

## 1. Tóm tắt

| Chỉ số | Giá trị |
|---|---|
| Nhiệm vụ được mở đường vòng | **3** — NV 21, NV 35, NV 43 |
| Bước con được thêm cửa thứ hai | **9** — `TASK_21_1`, `TASK_21_2`, `TASK_35_1`, `TASK_35_2`, `TASK_35_3`, `TASK_43_1`, `TASK_43_2`, `TASK_43_3`, `TASK_43_4` |
| Tổng quái phải cày nếu **không có bang** | NV 21: **300** · NV 35: **320** · NV 43: **380** |
| Khối lượng của người **có bang** | **không đổi một điểm nào** (xem [§6](#6-so-sánh-công-sức-hai-đường)) |
| File Java sửa | 2 — `services/TaskService.java`, `npc_list/LinhCanh.java` |
| Dòng SQL sửa | 12 — 9 dòng `task_sub_template` + 3 dòng `detail` của `task_main_template` |
| Hằng `ConstTask` | **không đổi** |
| Biên dịch | **562 file, sạch** (JDK 17) |

**Vì sao ba bước đó chặn nhiều hơn ba bước.** Điều kiện bang không chỉ chặn đúng bước "phá xong phó
bản": nó chặn **cả lối vào**, nên mọi bước nằm *bên trong* phó bản cũng chết theo. Đếm lại:

| NV | Bước bị chặn thật sự | Vì sao |
|---|---|---|
| 21 | `TASK_21_1`, `TASK_21_2` | bước 2 gặp NPC 26 Độc Nhãn ở **map 57**, nằm trong doanh trại |
| 35 | `TASK_35_1`, `TASK_35_2`, `TASK_35_3` | bước 1 ở **map 143**, bước 2 đếm quái ở **map 141/142/143** |
| 43 | `TASK_43_1`, `TASK_43_2`, `TASK_43_3`, `TASK_43_4` | cả bốn nằm trong **map 147–152** |

Vì thế file này mở **9 cửa**, không phải 3.

---

## 2. Nguyên tắc thiết kế

1. **Không đụng đường cũ.** Người có bang vẫn phá phó bản y như trước, và khối lượng của họ **giữ
   nguyên con số thiết kế** (60 quái CĐRĐ, 80 quái Khí gas, 1 lượt phá doanh trại). Cách giữ: khi
   `max_count` được nâng lên cho đường vòng thì cửa phó bản **cộng trọn phần còn thiếu**
   (`doneTaskAtOnce`), còn quái *trong* phó bản của hai bước "đếm quái" được **nhân hệ số 2**
   (`WEIGHT_MOB_TRONG_PHO_BAN`).
2. **Đường vòng phải nặng hơn, không nhẹ hơn.** Mốc quy đổi lấy theo tiền lệ 27 §6.1 (1 boss = 20–30
   quái) rồi nhân thêm vì một lượt phó bản đáng giá hơn một con boss, và vì **không được để ai bỏ bang
   đi đường vòng cho nhanh**. Cụ thể: 1 lượt phó bản ≈ 100–300 mạng quái ngoài trời.
3. **Chỉ dùng quái/map có thật, đúng mốc sức mạnh.** Mọi id dưới đây đối chiếu trực tiếp với
   `map_template.mobs` và `mob_template` trong `database team2026.sql`. Không map nào vượt quá
   `id 161` (ranh giới dữ liệu thật) và mọi map đều nằm sau mốc khóa của
   `ChangeMapService.checkMapCanJoin` ([bảng mốc khóa map ở doc 26](26-cap-nhat-khoa-map-tinh-nang.md))
   mà người chơi đã qua.
4. **Tên bước nói rõ cả hai cách** (`task_sub_template.NAME`), chi tiết đường vòng nằm ở cột `notify`
   (client hiện khi bắt đầu bước).
5. **Quái trong phó bản của phó bản đó chỉ tính cho bước của chính nó** — không có chuyện phá Doanh
   Trại để lấy tiến độ của Khí gas.

---

## 3. NV 21 — Doanh trại Độc Nhãn

**Mốc sức mạnh**: ~30.000.000 (cộng dồn giữa chương 3).
**Phó bản gốc** (`map/phoban/RedRibbonHQ`, map 53–62): bang **≥ 5 thành viên**, vào bang ≥ 1 ngày,
**≥ 1 đồng đội đứng gần**, 1 lượt/ngày/bang, 30 phút, **95 con quái** + NPC Độc Nhãn.

### 3.1 `TASK_21_1` — "Phá xong Doanh trại Độc Nhãn"

| | |
|---|---|
| **Điều kiện gốc** | `checkDoneTaskDungeon(player, MAP_DOANH_TRAI)` — móc ở chỗ `winDT = true` của `RedRibbonHQ` |
| **Điều kiện thay thế** | giết **300** quái id **43 Thằn lằn xanh, 44 Quỷ đầu nhọn, 45 Quỷ đầu vàng, 46 Quỷ da tím, 47 Quỷ già, 49 Dơi da xanh** ở **map 63 Trại lính Fide, 64 Núi dây leo, 65 Núi cây quỷ, 66 Trại qủy già, 67 Vực chết** |
| **`max_count`** | 1 → **300**; phá xong phó bản cộng trọn 300, mỗi mạng quái cộng 1 |
| **Máu quái** | 80.000 – 140.000 — nằm đúng giữa NV 19 (Nappa/Soldier 40–60 nghìn) và NV 22 (Khỉ 300–350 nghìn) |

**Vì sao là cụm map 63–67 chứ không phải "300 Lính độc nhãn".** Lính độc nhãn (34/35), Sói xám (36),
Robot bay (37), Robot thép (38) **chỉ tồn tại trong hai phó bản đều đòi bang**: Doanh Trại (53–62) và
Hang kho báu (135–138, cần SM ≥ 2 tỷ **và có bang** + item 611). Không có một con Lính độc nhãn nào
ngoài trời, nên phải đổi sang đội quân lính của Fide đóng ở cụm 63–67 — vừa đúng mốc sức mạnh, vừa
đúng mạch chuyện "cắt đường tiếp tế của doanh trại" mà Lính canh giao.

**Vì sao 300.** Một lượt phó bản = 95 quái + phần thưởng nhặt ngọc 5 phút + chi phí xã hội (5 người
cùng bang, mỗi ngày một lượt). Quy đổi 1 lượt ≈ 3,2 lần số quái của lượt đó. Thấp hơn thì bỏ bang đi
cày lợi hơn; cao hơn nhiều thì thành hình phạt.

### 3.2 `TASK_21_2` — "Lấy bản đồ hành quân từ Độc Nhãn"

| | |
|---|---|
| **Điều kiện gốc** | nói chuyện NPC **26 Độc Nhãn** ở **map 57 Tầng 4** (trong doanh trại) |
| **Điều kiện thay thế** | nói chuyện NPC **25 Lính canh** ở **map 27 Rừng Bamboo** |
| **`max_count`** | giữ **1** |

Không tính thêm công sức: 300 mạng quái ở bước trước **đã là** cái giá của tấm bản đồ; Lính canh chỉ
là chỗ nộp. Đây cũng là **van chống kẹt cho người có bang**: phó bản kết thúc sau 30 phút và đá người
chơi ra; ai chưa kịp bấm Độc Nhãn ở map 57 trước đây sẽ kẹt tới lượt phó bản ngày hôm sau.

---

## 4. NV 35 — Con đường rắn độc

**Mốc sức mạnh**: ~4.000.000.000 (cộng dồn giữa chương 5).
**Phó bản gốc** (`map/phoban/SnakeWay`, map 141–144): **có bang và vào bang ≥ 2 ngày**,
**1 lượt / 7 ngày / người**, 30 phút, **21 con quái** (141: 7, 142: 7, 143: 7).

**Cụm map thay thế dùng chung cho cả ba bước**: **73 Thung lũng chết, 74 Đồi cây Fide, 76 Núi đá,
77 Rừng đá, 81 Hang quỷ chim, 82 Núi khỉ đen** — chỉ tính **đúng hai loài của chặng cuối CĐRĐ (map 143)**:
**49 Dơi da xanh** (140.000 máu) và **50 Quỷ chim** (180.000 máu). Khóa map: 73–77 cần `TASK_19_0`,
81/82 cần `TASK_22_0` — người chơi ở NV 35 đã qua cả hai từ lâu.

### 4.1 `TASK_35_1` — "Vào Con đường rắn độc cùng bạn"

| | |
|---|---|
| **Điều kiện gốc** | ở **map 143** và trong khu có ≥ 2 người thật **cùng bang** (`checkDoneTaskTogether(..., requireSameClan = true)`) |
| **Điều kiện thay thế** | ở **map 73/74/76/77/81/82** và trong khu có ≥ 2 người thật, **không cần cùng bang** |
| **`max_count`** | giữ **1** |

Đường vòng **chỉ bỏ ràng buộc bang**, **giữ nguyên ý đồ "không đi một mình"** của thiết kế. Ràng buộc
"phải có người thứ hai online" là rủi ro riêng, đã ghi ở [27 §9 mục 10](27-du-lieu-nhiem-vu-moi.md#9-chỗ-nghi-ngờ)
và **không thuộc phạm vi file này**.

### 4.2 `TASK_35_2` — "Dọn đường qua ba chặng"

| | |
|---|---|
| **Điều kiện gốc** | giết **60** quái id 24/33/25/26/49/50 ở map 141/142/143 |
| **Điều kiện thay thế** | giết **120** con 49/50 ở cụm map 73–82 |
| **`max_count`** | 60 → **120**; quái **trong** phó bản cộng **2** điểm/mạng (⇒ vẫn đúng **60 con** như thiết kế), quái ngoài cộng **1** |

### 4.3 `TASK_35_3` — "Hoàn thành Con đường rắn độc"

| | |
|---|---|
| **Điều kiện gốc** | `checkDoneTaskDungeon(player, MAP_CON_DUONG_RAN_DOC)` — móc ở `SnakeWay.finish()` / `CADICH.leaveMap()` |
| **Điều kiện thay thế** | giết **200** con 49/50 ở cụm map 73–82 |
| **`max_count`** | 1 → **200**; hoàn thành phó bản cộng trọn 200 |

**Vì sao 120 + 200 = 320 mạng.** Con đường rắn độc là phó bản **ngặt nhất tuyến** — 1 lượt trên
**7 ngày**. Nếu đường vòng quá rẻ, không ai chờ 7 ngày nữa; nếu quá đắt thì người chơi lẻ đứng hình
cả tuần. 320 mạng quái 140–180 nghìn máu ở mốc 4 tỷ SM ≈ một buổi cày, tức là **nặng hơn hẳn một lượt
phó bản 21 quái**, nhưng vẫn rẻ hơn việc "chờ tới lượt sau".

> ⚠️ **Lỗi dữ liệu cũ được vá luôn ở đây.** `TASK_35_2` yêu cầu 60 quái nhưng **cả phó bản chỉ có 21
> con** và quái phó bản **không hồi sinh** ([14 §5](../1-he-thong-hien-tai/14-ban-do-pho-ban.md)), mà
> phó bản lại **1 lượt/7 ngày**. Nghĩa là **kể cả người có bang cũng cần 3 lượt = 3 tuần** mới xong
> bước này. Sau thay đổi: 21 quái × 2 điểm = 42 điểm mỗi lượt, phần còn thiếu cày ngoài trời — bước
> này **lần đầu tiên hoàn thành được trong một ngày**.

---

## 5. NV 43 — Khí gas hủy diệt

**Mốc sức mạnh**: ~14.000.000.000 (cộng dồn chương 6).
**Phó bản gốc** (`map/phoban/DestronGas`, map 147–152): **bang chủ mới mở được**, vào bang ≥ 2 ngày,
bang ≥ 5 thành viên, 3 lượt/ngày/bang, 30 phút, **70 quái** ở 147/149/151/152 (+11 con ở 148).

**Cụm map thay thế dùng chung cho cả bốn bước**: **155 Hành tinh ngục tù** (78 Khỉ lông xanh 2 triệu
máu, 79 Taburine Đỏ 3 triệu) và **160 Khu hang động / 161 Bìa rừng nguyên thủy** (80 Cabira 4 triệu,
81 Tobi 5 triệu). Đây là **những con quái thường khỏe nhất còn lại ngoài phó bản**. Map 155 mở từ
`TASK_42_0` (nhiệm vụ ngay trước), map 160/161 đi bằng **Nhẫn thời không sai lệch (992)** đã được trao
ở `TASK_32_0` và **không bị trừ khi dùng** (`services_func/UseItem.java` case 992) — NV 34 bước 5 cũng
bắt quay lại map 160 gặp Bardock nên lối này chắc chắn còn mở.

| Bước | Điều kiện gốc | Điều kiện thay thế | `max_count` |
|---|---|---|---|
| `TASK_43_1` "Dọn sạch khí gas" | 80 quái 73/74/75/76 ở map 147/149/151/152 | **160** quái ở map 155/160/161 | 80 → **160**; quái trong phó bản cộng **2** (⇒ vẫn **80 con**), ngoài cộng **1** |
| `TASK_43_2` "Hạ Dr Lychee" | boss **−208** | **50** quái ở map 155/160/161 | 1 → **50**; hạ boss cộng trọn 50 |
| `TASK_43_3` "Hạ Hatchiyack" | boss **−207** | **70** quái ở map 155/160/161 | 1 → **70**; hạ boss cộng trọn 70 |
| `TASK_43_4` "Hoàn thành Khí gas" | `checkDoneTaskDungeon(..., MAP_KHI_GAS_HUY_DIET)` | **100** quái ở map 155/160/161 | 1 → **100**; hoàn thành phó bản cộng trọn 100 |

**Vì sao không dùng quái khí gas thật.** Kawazu (73), Kinkarn (74), Arbee (75), Cỗ máy hủy diệt (76)
**chỉ có trong map 147–152**, không xuất hiện ở bất kỳ map thường nào.

**Vì sao 50 / 70 cho hai con boss.** Theo tiền lệ 27 §6.1 (1 boss = 20–30 quái), nhân lên vì quái thay
thế ở đây tuy nhiều máu (2–5 triệu) nhưng vẫn chết một đòn ở mốc 14 tỷ SM, và vì Hatchiyack là boss
kết phó bản nên đắt hơn Dr Lychee.

**Tổng 380 mạng** — nặng nhất trong ba đường vòng, đúng thứ tự chương (21 < 35 < 43) và tương xứng với
điều kiện ngặt nhất về mặt tổ chức (phải là **bang chủ** của một bang ≥ 5 người).

---

## 6. So sánh công sức hai đường

| Bước | Người **có bang** (đường cũ) — có đổi không? | Người **không bang** (đường vòng) |
|---|---|---|
| `TASK_21_1` | 1 lượt Doanh Trại (95 quái, 30 phút, cần 5 người, 1 lượt/ngày) — **y nguyên** | **300** quái 80–140 nghìn máu, map 63–67, 24/7 |
| `TASK_21_2` | bấm Độc Nhãn ở map 57 — **y nguyên** (thêm cửa dự phòng ở Lính canh) | bấm Lính canh ở map 27 |
| `TASK_35_1` | map 143, ≥ 2 người cùng bang — **y nguyên** | map 73–82, ≥ 2 người bất kỳ |
| `TASK_35_2` | **60** quái trong phó bản — **y nguyên** (2 điểm/mạng) | **120** quái 140–180 nghìn máu |
| `TASK_35_3` | 1 lượt CĐRĐ (21 quái, 1 lượt/**7 ngày**) — **y nguyên** | **200** quái |
| `TASK_43_1` | **80** quái trong phó bản — **y nguyên** (2 điểm/mạng) | **160** quái 2–5 triệu máu |
| `TASK_43_2` | boss Dr Lychee — **y nguyên** | **50** quái |
| `TASK_43_3` | boss Hatchiyack — **y nguyên** | **70** quái |
| `TASK_43_4` | 1 lượt Khí gas (3 lượt/ngày) — **y nguyên** | **100** quái |

**Ước lượng thời gian** (mốc sức mạnh của chính nhiệm vụ đó, quái chết 1 đòn, tính cả thời gian nhảy
khu và chờ hồi sinh):

| NV | Đường phó bản | Đường vòng | Nhận xét |
|---|---|---|---|
| 21 | 30 phút + **phải gom đủ 5 người cùng bang**, 1 lượt/ngày | 300 quái ≈ 40–60 phút cày liên tục (56 con/khu × 10 khu) | đường vòng **lâu hơn**, bù lại chủ động 24/7 |
| 35 | 30 phút nhưng **chờ tới 7 ngày** mới tới lượt | 320 quái ≈ 45–75 phút (15 con/khu × 10 khu × 6 map) | đường vòng lâu hơn một buổi, nhưng thay cho một tuần chờ |
| 43 | 30 phút, 3 lượt/ngày, **phải là bang chủ** | 380 quái ≈ 50–80 phút (47 con/khu × 10 khu) | đường vòng **rõ ràng nặng hơn** |

Kết luận: **không có động cơ bỏ bang**. Người có bang xong ba nhiệm vụ này trong ~90 phút chơi chia
theo ngày mở phó bản; người đi một mình mất ~3 giờ cày nhưng **không bao giờ kẹt**.

---

## 7. Đã sửa những gì

### 7.1 `SRC/src/nro/models/services/TaskService.java`

| Chỗ | Thay đổi |
|---|---|
| hằng số | thêm `WEIGHT_MOB_TRONG_PHO_BAN = 2` |
| tiện ích mới | `isMapDoanhTraiNgoai(int)` (63–67) · `isMapRanDocNgoai(int)` (73/74/76/77/81/82) · `isMapKhiGasNgoai(int)` (155/160/161) |
| tiện ích mới | `addTaskProgress(Player, int, int)` → cộng n điểm cho **đúng** bước đang làm, trả `boolean` |
| tiện ích mới | `doneTaskAtOnce(Player, int)` → cộng **trọn phần còn thiếu** của bước (đọc `maxCount` từ DB, nên đổi số trong SQL **không phải sửa code**) |
| tiện ích mới | `addRanDocNgoaiProgress(Player)` · `addKhiGasNgoaiProgress(Player)` — cộng 1 điểm cho bước đang dở, dùng else-if để **một mạng quái không cộng đúp** sang bước kế tiếp |
| `checkDoneTaskTalkNpc` | `case ConstNpc.LINH_CANH` nhận thêm `TASK_21_2` |
| `checkDoneTaskKillBoss` | `DR_LYCHEE` / `HATCHIYACK` đổi `doneTask` → `doneTaskAtOnce` |
| `checkDoneTaskKillMob` | thêm `case` 43/44/45/46/47 (NV 21); mở rộng `case` 49, 50 (NV 21 + NV 35); mở rộng 78/79 và gộp 80/81 (NV 43); quái CĐRĐ (24/33/25/26/49/50) và quái khí gas (73/74/75/76) đổi sang cộng **2 điểm** |
| `checkDoneTaskDungeon` | cả ba `case` đổi `doneTask` → `doneTaskAtOnce` |
| `checkDoneTaskTogetherInZone` | `TASK_35_1` có cửa thứ hai: cụm map 73–82, **không cần cùng bang** |

### 7.2 `SRC/src/nro/models/npc_list/LinhCanh.java` — **bắt buộc, nằm ngoài `TaskService`**

`LinhCanh.openBaseMenu` **chưa từng gọi** `checkDoneTaskTalkNpc`, lại `return` ngay ở dòng
`player.clan == null` với câu "Chỉ tiếp các bang hội". Nghĩa là **cả `TASK_21_0` lẫn cửa mới của
`TASK_21_2` đều chết** với người chơi không bang. Đã thêm **một dòng** gọi
`TaskService.gI().checkDoneTaskTalkNpc(player, this)` **trước mọi kiểm tra bang hội**; phần menu phó
bản bên dưới **giữ nguyên 100%**.

### 7.3 `SRC/sql/patch/02-nhiem-vu-moi.sql`

9 dòng `task_sub_template` (đổi `NAME`, `max_count`, `notify`) + 3 dòng `detail` của
`task_main_template` (NV 21, 35, 43). **Không đụng** `ducvupro`, `npc_id`, `map`, bảng `task_main_reward`
hay bất kỳ dòng nào khác — 8 câu kiểm tra §5 của file SQL vẫn đạt (không câu nào phụ thuộc `max_count`).

Tên bước mới (cột `NAME`) đều nói rõ **cả hai cách**, ví dụ:

```
Phá Doanh trại Độc Nhãn cùng bang, hoặc diệt 300 quái cụm Trại lính Fide
Dọn đường qua ba chặng, hoặc diệt 120 Dơi da xanh / Quỷ chim ngoài phó bản
Hạ Hatchiyack, hoặc diệt 70 quái ở map 155/160/161
```

---

## 8. Điểm móc nhóm sau phải nối

Đường vòng dùng lại **đúng những điểm móc đã có** (`Mob.java:169` → `checkDoneTaskKillMob`,
`Player.update:534` → `checkDoneTaskTogetherInZone`, ba phó bản → `checkDoneTaskDungeonForZone`), nên
**không phát sinh điểm móc mới**, trừ một chỗ đã tự làm ở §7.2.

Nhưng ba nhiệm vụ này vẫn **chưa chạy được đầu-cuối** vì hai NPC mở màn chưa được nối
([29 §7](29-taskservice-tuyen-moi.md#7-điểm-móc-nhóm-sau-phải-nối) — đây là việc của nhóm NPC, **không
liên quan tới bang hội**, kẹt cho *mọi* người chơi):

| # | File | Chỗ móc | Gọi gì | Bước đang kẹt |
|---|---|---|---|---|
| 1 | `npc_list/ThanVuTru.java` (NPC 20) | đầu `openBaseMenu`, nhánh `mapId == 48` | `TaskService.gI().checkDoneTaskTalkNpc(player, this)` | `TASK_35_0` |
| 2 | `npc_list/MrPoPo.java` (NPC 67) | đầu `openBaseMenu`, nhánh `mapId == 0` | như trên | `TASK_43_0` |

---

## 9. Chỗ nghi ngờ

| # | Vấn đề | Tôi đã chọn gì | Cần chốt lại / rủi ro |
|---|---|---|---|
| 1 | **Tỉ lệ quy đổi 300 / 200 / 100 điểm cho một lượt phó bản, và 50 / 70 cho một con boss là do tôi tự đặt** | bám tiền lệ 27 §6.1 (1 boss = 20–30 quái) rồi nhân lên theo độ ngặt của từng phó bản | **Phải chơi thử mới biết đúng chưa.** Đổi số chỉ cần sửa `max_count` trong SQL — `doneTaskAtOnce` đọc `maxCount` từ DB nên **không phải biên dịch lại** |
| 2 | **Mũi tên chỉ đường vẫn trỏ vào phó bản** — cột `npc_id` / `map` của 9 dòng giữ nguyên (53, 57, 143, 141, 144, 147, 148) | giữ nguyên để không phá hướng dẫn của người có bang; đường vòng ghi ở `NAME` + `notify` | Người chơi lẻ thấy mũi tên trỏ vào map họ **không vào được**. Nếu chủ dự án muốn, đổi `map` của `TASK_21_1` → 63, `TASK_35_2/35_3` → 77, `TASK_43_1..43_4` → 160 |
| 3 | **`TASK_35_2` yêu cầu 60 quái nhưng phó bản chỉ có 21 con và 1 lượt/7 ngày** (lỗi dữ liệu có sẵn, ảnh hưởng **cả người có bang**) | không hạ yêu cầu; đường vòng cho phép cày bù phần thiếu ở ngoài | Nếu chủ dự án muốn người có bang xong bước này **chỉ bằng phó bản**, phải hạ `max_count` xuống 42 (21 con × 2) — nhưng như thế đường vòng cũng nhẹ theo |
| 4 | **`TASK_43_1` yêu cầu 80 quái, phó bản chỉ có 70 con ngoài map boss** | giữ nguyên (Khí gas 3 lượt/ngày nên 2 lượt là đủ) | Không kẹt, chỉ hơi lệch so với ý đồ "một lượt là xong" |
| 5 | **`TASK_35_1` vẫn cần một người chơi thứ hai** | chỉ bỏ ràng buộc *cùng bang*, giữ ràng buộc *cùng khu* | Server vắng người vẫn kẹt — đây là rủi ro riêng đã ghi ở [27 §9 mục 10](27-du-lieu-nhiem-vu-moi.md#9-chỗ-nghi-ngờ), **chưa xử lý** |
| 6 | **Người không bang bấm Lính canh sẽ xong bước nhưng vẫn nhận câu "Chỉ tiếp các bang hội"** | chấp nhận (không viết lại thoại của NPC phó bản) | Hơi lấn cấn về mặt trình bày. Muốn sạch thì thêm một nhánh thoại riêng cho `getIdTask == TASK_21_2` trong `LinhCanh` |
| 7 | **Ước lượng thời gian ở §6 chưa đo thật** | tính theo số quái/khu × 10 khu, chưa tính chính xác thời gian hồi sinh quái | Nếu quái hồi sinh chậm hơn tôi tưởng, 300–380 mạng có thể thành 2–3 giờ. Cần đo `Mob` respawn trước khi phát hành |
| 8 | **Chủ đề của cụm map 63–67 (lính Fide) không phải Độc Nhãn** | chọn theo **mốc sức mạnh** (yêu cầu số 3 của chủ dự án) vì **không tồn tại Lính độc nhãn ngoài phó bản** | Nếu chủ dự án ưu tiên mạch chuyện hơn mốc sức mạnh, phương án khác là Không tặc (31) / Bulon (22) ở map 29/30/33/34/37/38 — nhưng máu chỉ 3.000–6.000, **quá nhẹ** so với mốc 30 triệu SM |
| 9 | **Map 162 / 165 / 166 mà doc 27 dùng KHÔNG có trong `database team2026.sql`** (dump chỉ tới map 161) | đường vòng **né sạch** ba map đó, chỉ dùng map ≤ 161 có thật | Không ảnh hưởng file này, nhưng `TASK_29_*`, `TASK_32_3`, `TASK_36_*` đang trỏ vào map chưa tồn tại — **nhóm dữ liệu map phải kiểm lại** |
| 10 | **Quái ngoài phó bản cộng đúp khi vừa xong bước** | dùng else-if trong `addRanDocNgoaiProgress` / `addKhiGasNgoaiProgress` nên một mạng chỉ cộng cho một bước | Với quái **trong** phó bản (hệ số 2) thì mạng cuối có thể vượt `max_count` 1 điểm — vô hại, `addDoneSubTask` chỉ so `>=` |
| 11 | **Chưa chạy trên server thật** | chỉ biên dịch sạch (562 file, JDK 17) và đối chiếu id với dump DB | Cần một lượt chơi thử NV 21 / 35 / 43 bằng **cả hai đường**, và một lượt kiểm tra rằng người có bang vẫn xong bước đúng số cũ |

---

*Hết file 32. Xem thêm: [27 §6.1 — khuôn mẫu bước hai cửa](27-du-lieu-nhiem-vu-moi.md#61-bước-có-hai-đường-hoàn-thành-theo-khung-giờ-phó-bản) ·
[29 — kiến trúc `TaskService`](29-taskservice-tuyen-moi.md) ·
[14 — điều kiện phó bản](../1-he-thong-hien-tai/14-ban-do-pho-ban.md) ·
[17 — bang hội](../1-he-thong-hien-tai/17-bang-hoi.md).*
