# 34 — TẤT CẢ BOSS TRONG GAME (bảng tra cứu đầy đủ)

> Gom mọi boss của server vào một chỗ: **boss thế giới, boss mini/lang thang, boss sự kiện, boss theo khung giờ, boss luyện tập, boss nhiệm vụ, boss phó bản, boss đấu trường, boss nhân bản**.
> Mỗi boss có: máu và sát thương **từng hình dạng**, phần trăm giảm sát thương, máu hiệu dụng, giáp, kỹ năng, map, thời gian hồi sinh, vật phẩm rơi kèm số lượng và tỉ lệ, cơ chế đặc biệt, lời thoại.
>
> Số liệu đọc trực tiếp từ mã nguồn `SRC/src/nro/models/boss/**` và database, **sau toàn bộ đợt thay đổi tháng 9/2026** (tuyến nhiệm vụ mới, 9 boss mới, cơ chế giảm sát thương). Chỗ nào không tra được đều ghi rõ "không tra được" thay vì đoán.

## Ba điều cần biết trước khi đọc

1. **Boss không có giáp.** Dữ liệu boss chỉ có máu và sát thương; code không bao giờ gán giáp cho boss, nên giáp luôn bằng 0 và phép trừ giáp khi nhận đòn là trừ 0. Ngoại lệ duy nhất: boss Siêu Hạng, vì nó sao chép nguyên chỉ số của người chơi bị thách đấu.
2. **Giảm sát thương là cơ chế mới.** Vì máu tối đa của game bị chặn ở 2,14 tỉ (trường máu là số nguyên 32-bit), boss mạnh được tăng độ dai bằng cách giảm phần trăm sát thương nhận vào. Cột "máu hiệu dụng" = máu ÷ (1 − % giảm) cho biết thực tế phải đánh bao nhiêu.
3. **Boss nhiệm vụ chỉ rơi đồ nhiệm vụ** — không vàng, không trang bị, không Ngọc Rồng. Muốn đồ tốt vẫn phải đánh boss thế giới bản gốc.

## Mục lục lớn

- **Phần A — Boss thế giới, mini, sự kiện, luyện tập** (86 boss)
- **Phần B — Boss nhiệm vụ, phó bản, đấu trường, nhân bản** (61 boss)

---

# PHẦN A — BOSS THẾ GIỚI, MINI, SỰ KIỆN, LUYỆN TẬP

## 34a — Tra cứu boss: thế giới, mini/lang thang, sự kiện, luyện tập

> **Phạm vi**: nửa 1 của bảng tra cứu boss — boss thế giới trên bản đồ thường, boss theo
> khung giờ cố định, boss mini/lang thang, boss sự kiện và boss luyện tập tự động.
> Boss nhiệm vụ (dải `-2000…-2105`, Heart `-108108`), boss phó bản (Doanh trại,
> Bản đồ kho báu, Con đường rắn độc, Khí gas huỷ diệt, Yardrat), đấu trường
> (Đại hội võ thuật 23, Võ đài Hạt Mít, Siêu hạng) và nhân bản nằm ở tài liệu nửa 2.
>
> **Toàn bộ số liệu dưới đây đọc trực tiếp từ mã nguồn hiện tại**, không lấy lại từ
> `docs/1-he-thong-hien-tai/11-boss.md` hay `docs/3-kiem-tra-can-bang/21c-boss-roi-do-can-bang.md`
> (hai tài liệu đó có chỗ đã lệch so với code sau đợt sửa gần đây).

---

### 0. Cách đọc tài liệu này

#### 0.1 Nguồn số liệu

| Thông tin | Đọc từ |
|---|---|
| Tên, giới tính/hành tinh, tạo hình, sát thương, máu, map, kỹ năng, lời thoại, thời gian nghỉ | `SRC/src/nro/models/boss/BossesData.java` (hoặc `BossData` khai báo trực tiếp trong lớp boss) |
| id boss | `SRC/src/nro/models/boss/BossID.java` |
| Vòng đời, nhận sát thương mặc định, thưởng mặc định | `SRC/src/nro/models/boss/Boss.java` |
| Số bản tồn tại cùng lúc, lịch tạo | `SRC/src/nro/models/boss/Boss_Manager/BossManager.java#loadBoss`, `SRC/src/nro/models/map/Map.java#initBoss`, `SRC/src/nro/models/event/EventManager.java` |
| % giảm sát thương nhận vào (cơ chế mới) | `SRC/src/nro/models/boss/BossDamageReduce.java` |
| Rơi đồ Thần Linh | `SRC/src/nro/models/services/ItemService.java#randDoTLBoss` |
| Tên vật phẩm | bảng `item_template` trong `database team2026.sql` |
| Tên map | bảng `map_template` trong `database team2026.sql` |
| Tên kỹ năng | bảng `skill_template` trong `database team2026.sql` |

#### 0.2 Giáp của boss — kiểm chứng bằng code

`BossData` **không có trường giáp**. Đường đi thực tế lúc chạy:

1. `Boss.initBase()` (Boss.java:213-224) chỉ gán `nPoint.mpg`, `nPoint.dameg`, `nPoint.hpg`
   rồi gọi `nPoint.calPoint()`. **Không hề gán `nPoint.defg`.**
2. `NPoint.calPoint()` → `setPointWhenWearClothes()` → `resetPoint()` đặt `defAdd = 0`,
   rồi `setDef()` (NPoint.java:1216-1223) tính `def = defg * 4 + defAdd`.
3. `defg` chỉ được gán ở 4 chỗ trong toàn bộ mã nguồn: `MrBlue` (nạp nhân vật từ DB),
   `PetService` (đệ tử), `NewBot` (bot). **Không chỗ nào gán cho boss** → `defg = 0` (giá trị
   mặc định của `int`).
4. Boss không mặc trang bị nên `defAdd` cũng bằng 0.

**Kết luận: mọi boss trong tài liệu này đều có giáp = 0, tức KHÔNG CÓ GIÁP.**
Những boss gọi `nPoint.subDameInjureWithDeff(damage)` thực chất chỉ thực hiện
`damage - 0`, hàm này không làm gì cả. Nó **không** phải cơ chế giảm sát thương —
cơ chế giảm thật nằm ở phép chia trong `injured()` và ở `BossDamageReduce`.

Tương tự, `nPoint.tlNeDon` (tỉ lệ né đòn) cũng bị `resetPoint()` đưa về 0, nên câu
`Util.isTrue(this.nPoint.tlNeDon, 1000)` trong `Boss.injured` **không bao giờ đúng**:
boss dùng `injured()` mặc định không né đòn. Chỉ những boss ghi đè `injured()` với số
cứng (Cooler 1%, Mabư 20%, boss luyện tập 40%…) mới thật sự có né đòn — mục của từng
boss có ghi rõ.

#### 0.3 Máu hiệu dụng

`Máu hiệu dụng = máu danh nghĩa ÷ (tỉ lệ sát thương thực sự trừ được)`.

Có **hai lớp** giảm sát thương, nhân chồng lên nhau:

- **Lớp cũ**: phép chia/nhân viết cứng trong `injured()` của từng lớp boss
  (`damage / 2`, `damage / 3`, `damage / 7`, `damage * 0.7 / 2`…).
- **Lớp mới**: `Boss.applyDamageReduce()` (Boss.java:753-772) áp `%` lấy từ bảng
  `BossDamageReduce`, đặt **ngay trước `subHP`**, tức **cộng dồn lên trên** lớp cũ.
  Trần an toàn `MAX_PERCENT = 90`, và mọi đòn `damage >= 1` vẫn trừ tối thiểu 1 máu
  (boss không thể bất tử vì làm tròn).

Ngoài ra nhiều boss còn có **trần sát thương mỗi đòn** (`if (damage >= X) damage = X`).
Trần này quyết định **số đòn tối thiểu** để hạ boss, thường quan trọng hơn cả máu.

#### 0.4 Trạng thái `BossDamageReduce` hiện tại

Công tắc `WORLD_BOSS_ENABLED = true`. Bảng áp cho boss thuộc nửa này:

| Hằng số | Boss | Giá trị | Có hiệu lực? |
|---|---|---|---|
| `BLACK_GOKU_TG` | Black Goku (-203) | `{30, 25}` | **Có** |
| `CUMBER_TG` | Cumber (-203999) | `{30, 25}` | **Có** |
| `BABY_TG` | Baby (-925) | `{0, 0, 0}` | Đã nối dây, **cố ý để 0** |
| `COOLER_TG` | Cooler (-29) | `{0, 0}` | Đã nối dây, **cố ý để 0** |

Các bảng còn lại (`KE_THU_GOM`, `JACO_VO_THUC`, `*_NV`, `HEART`) thuộc boss nhiệm vụ →
tài liệu nửa 2. **Không boss nào khác trong nửa này gán `damageReducePercentByLevel`**,
nên `getDamageReducePercent()` trả 0 và `applyDamageReduce` trả nguyên sát thương.

Khi `applyDamageReduce` thực sự cắt sát thương, boss chat một lần cho mỗi hình dạng:
`"Đòn của ngươi yếu đi <n>% trước ta"`.

#### 0.5 Vòng đời chung (Boss.java)

`REST → RESPAWN → JOIN_MAP → CHAT_S → ACTIVE → DIE → CHAT_E → LEAVE_MAP → REST`

- `rest()`: chỉ chuyển sang `RESPAWN` khi hình dạng kế tiếp có `AppearType.DEFAULT_APPEAR`
  **và** đã qua `secondsRest` giây kể từ `lastTimeRest`.
- `respawn()`: `currentLevel++`, vòng lại 0 khi hết mảng → **boss nhiều hình dạng hồi sinh
  lần lượt từng hình dạng, không phải cả chuỗi một lượt**.
- `leaveMap()`: nếu còn hình dạng sau thì giữ nguyên khu và `RESPAWN` ngay (đổi hình dạng);
  nếu đã là hình dạng cuối thì rời map và bắt đầu đếm `secondsRest`.
- `joinMap()`: chọn khu. Nếu `isZone01SpawnDisabled = true` thì bốc ngẫu nhiên khu ≥ 2;
  ngược lại bò từ khu 0 lên, bỏ qua khu > 10 người và khu đã có boss.
- `notifyJoinMap()`: loa toàn server `"BOSS <tên> vừa xuất hiện tại <tên map>"`, trừ khi
  `isNotifyDisabled` hoặc map là 140/111/phó bản/Ma Bư/Ngọc rồng đen.
- `BossManager.run()` quét mỗi **1500 ms**; `OtherBossManager` và các manager phó bản quét
  mỗi **150 ms**.

#### 0.6 Bộ rơi đồ dùng chung

Nhiều boss dùng chung một trong hai bộ:

**Bộ A — "boss Fide/Android/Cell/tiểu đội"** (`reward()` gần như giống hệt nhau):
- 100% rơi **Vàng (id 190)**, số lượng `Util.nextInt(20000, 30001)`.
- 80% rơi 1 viên trong `{18, 19, 20}` = **Ngọc Rồng 5/6/7 sao**, số lượng 1.
- `+5 Point` sự kiện cho người kết liễu.
- Vật phẩm rơi ra đất và **khoá theo `plKill.id`** (chỉ người kết liễu nhặt được trong
  thời gian khoá).

**Bộ B — "boss lớn"** (Black Goku, Cumber, Baby, Cooler, Siêu bọ hung, chuỗi Mabư 12h):
- 100% **Vàng (190)** `Util.nextInt(20000, 30000)`.
- `x%` rơi **đồ Thần Linh** qua `ItemService.randDoTLBoss` (xem 0.7).
- `y%` rơi 1 món trang bị thường có chỉ số shop ×`nextInt(100,115)/100` **+ sao pha lê**
  (80% sao 1-3, 17% sao 4-5, 3% sao 6).
  - Nhóm 0 (70%): Áo/Quần/Giày — id `230,231,232,234,235,236,238,239,240,242,243,244,246,247,248,250,251,252,266,267,268,270,271,272,274,275,276`
  - Nhóm 1 (30%): Găng/Rada — id `254,255,256,258,259,260,262,263,264,278,279,280`
- `z%` rơi ngọc rồng `{15,16,17,18,19,20}` (Ngọc Rồng 2→7 sao), số lượng `nextInt(1,3)`;
  vài boss có thêm `992` = **Nhẫn thời không sai lệch**.
- `+5 Point` sự kiện.

**Không boss nào trong nửa này rơi Ngọc (item 77) trừ nhóm Bojack và tiểu đội Namek**,
và **không boss nào rơi Hồng ngọc/gem**.

#### 0.7 Đồ Thần Linh (`randDoTLBoss`)

Tỉ lệ chọn loại (các mốc `Util.isTrue` viết nối tiếp nhau nên tỉ lệ thực tế là tích luỹ):
Nhẫn 10% → Găng 25%×90% → Quần 45% → Áo 75% → Giày (phần còn lại).

| id | Tên | id | Tên |
|---|---|---|---|
| 555 | Áo Thần Linh | 561 | Nhẫn Thần Linh |
| 556 | Quần Thần Linh | 562 | Găng Thần Linh |
| 557 | Áo Thần Namếc | 563 | Giầy Thần Linh |
| 558 | Quần Thần namếc | 564 | Găng Thần Namếc |
| 559 | Áo Thần Xayda | 565 | Giầy Thần Namếc |
| 560 | Quần Thần Xayda | 566 | Găng Thần Xayda |
| | | 567 | Giầy Thần Xayda |

Chỉ số nhân thêm `Util.nextInt(100, 115)%`; nếu > 100 thì gắn option 207 (đồ hiếm rơi từ quái).

---

### 1. BẢNG TỔNG HỢP

#### 1.1 Boss thế giới (bản đồ thường)

| Tên | id | Máu (từng hình dạng) | Sát thương | Map | Bản cùng lúc | Nghỉ | Rơi chính |
|---|---:|---|---:|---|---:|---|---|
| Kuku | -20 | 500.000 | 9.000 | 68-72 | 5 | 10 phút | Vàng 20k-30k, 80% NR 5/6/7 sao |
| Mập Đầu Đinh | -21 | 1.000.000 | 10.000 | 63-67 | 5 | 10 phút | như trên |
| Rambo | -22 | 1.500.000 | 12.400 | 74-77 | 5 | 10 phút | như trên |
| Số 4 | -23 | 25.000.000 | 10.000 | 79,81,82,83 | 1 (đi kèm TĐT) | theo TĐT | như trên |
| Số 3 | -24 | 30.000.000 | 11.000 | 79,81,82,83 | 1 | theo TĐT | như trên |
| Số 2 | -25 | 30.500.000 | 12.000 | 79,81,82,83 | 1 | theo TĐT | như trên |
| Số 1 | -26 | 40.000.000 | 12.500 | 79,81,82,83 | 1 | theo TĐT | như trên |
| Tiểu đội trưởng | -27 | 50.000.000 | 13.000 | 79,81,82,83 | 1 | 5 phút | như trên |
| Số 4 Namek | -311 | 2.500.000 | 10.000 | 73-77 | 1 | theo TĐT NM | Ngọc ×nhiều + Cải trang 429 + NR 6,7 sao |
| Số 3 Namek | -312 | 3.000.000 | 10.000 | 73-77 | 1 | theo TĐT NM | Ngọc + Cải trang 430 + NR 6,7 sao |
| Số 2 Namek | -313 | 3.500.000 | 12.200 | 73-77 | 1 | theo TĐT NM | Ngọc + Cải trang 431 + NR 6,7 sao |
| Số 1 Namek | -314 | 4.000.000 | 13.200 | 73-77 | 1 | theo TĐT NM | Ngọc + Cải trang 432 + NR 6,7 sao |
| Tiểu đội trưởng Namek | -315 | 5.000.000 | 15.000 | 73-77 | 1 | 5 phút | Ngọc + Cải trang 433 + NR 6,7 sao |
| Fide đại ca | -28 | 10tr / 20tr / 30tr | 22k/25k/30k | 80 | 1 | 10 phút | Vàng + 80% NR 5/6/7 sao |
| Dr.Kôrê | -31 | 2.000.000 | 12.000 | 96,94,93 | 1 | 10 phút | Vàng + 80% NR 5/6/7 sao |
| Android 19 | -30 | 1.000.000 | 12.200 | 96,94,93 | đi kèm Kôrê | — | như trên |
| Android 14 | -33 | 4.000.000 | 12.000 | 104 | 1 | 10 phút | như trên |
| Android 13 | -32 | 3.000.000 | 12.055 | 104 | gọi bởi 14 | — | như trên |
| Android 15 | -34 | 5.000.000 | 12.200 | 104 | đi kèm 14 | — | như trên |
| King Kong | -37 | 20.000.000 | 12.000 | 97,98,99 | 1 | 10 phút | như trên |
| Pic | -35 | 10.000.000 | 17.022 | 97,98,99 | đi kèm KK | — | như trên |
| Poc | -36 | 15.000.000 | 18.000 | 97,98,99 | đi kèm KK | — | như trên |
| Xên bọ hung | -100 | 50tr / 100tr / 150tr | 20k/25k/30k | 100 | 1 | 30 phút | Vàng + 80% NR 5/6/7 sao |
| Siêu Bọ Hung | -101 | 150tr / 200tr | 35k/40k | 103 | 1 | 30 phút | Bộ B (30% trang bị, 80% ngọc rồng) |
| Xên con 1-7 | -102…-108 | 5.000.000 mỗi con | 15.000 | 103 | 7, gọi bởi -101 | — | Vàng + 80% NR 5/6/7 sao |
| Black Goku | -203 | 500tr / 2.000tr | 50k/100k | 102,92,93,94,96-100 | **2** | 5 phút | Bộ B + 992 |
| Cumber | -203999 | 500tr / 2.000tr | 50k/100k | 155 | 1 | 5 phút | Bộ B + 992 |
| Cooler | -29 | 200tr / 500tr | 32k/50k | 110 | 1 | 30 phút | Bộ B (80% ngọc rồng) |
| Baby | -925 | 2.000tr × 3 hình dạng | 200k/250k/30k | 14 | **2** | 15 phút | Bộ B + 1% cải trang Baby |
| Bojack | -320 | 100tr / (chuyển -321) | 300.000 | 97-100, 105-109 | 1 | 15 phút | Ngọc ×nhiều + Cải trang 427 + NR 6,7 sao |
| Bujin | -316 | 20.000.000 | 170.000 | như Bojack | đi kèm | — | Ngọc + Cải trang 423 + NR 6,7 |
| Kogu | -317 | 40.000.000 | 180.000 | như Bojack | đi kèm | — | Ngọc + Cải trang 424 + NR 6,7 |
| Zangya | -318 | 60.000.000 | 207.200 | như Bojack | đi kèm | — | Ngọc + Cải trang 425 + NR 6,7 |
| Bido | -319 | 80.000.000 | 250.200 | như Bojack | đi kèm | — | Ngọc + Cải trang 426 + NR 6,7 |
| Siêu Bojack | -321 | 500.000.000 | 300.000 | như Bojack | 1 | 30 phút | Ngọc + Cải trang 428 + NR 6,7 |
| Fide Vàng | -502 | 1.000.000.000 | 100.000 | 6 | 1 | 5 phút, **chỉ 21h-22h** | Cải trang Fide vàng (629) |
| Death Beam 1-5 | -609…-613 | 500 | 1.000 | 6 | 5, gọi bởi -502 | — | Cải trang Fide vàng (629) |

#### 1.2 Boss theo khung giờ cố định

| Tên | id | Máu | Sát thương | Map | Bản | Khung giờ | Rơi chính |
|---|---:|---:|---:|---|---:|---|---|
| Tàu Pảy Pảy (Đông Nam Karin) | ngẫu nhiên -1.000.000…-100.000 | 10.000 | 100 | 111 | **10** (1/khu) | luôn có | không rơi gì, cho SM/TN |
| Drabura | -233 | 20.000.000 | 10.000 | 114 | **10** | 12h-13h | Bộ B + 10 điểm Mabư |
| Bui Bui | -234 | 40.000.000 | 200.000 | 115 | **10** | 12h-13h | Bộ B + 10 điểm |
| Bui Bui (ải 1) | -238 | 40.000.000 | 200.000 | 117 | **10** | 12h-13h | Bộ B + 10 điểm |
| Ya côn | -235 | 50.000.000 | 200.000 | 118 | **10** | 12h-13h | Bộ B + 10 điểm |
| Drabura 2 | -237 | 20.000.000 | 200.000 | 119 | **10** | 12h-13h | Bộ B + 10 điểm |
| Gôku (bị điều khiển) | -341 | 60.000.000 | 1.000 | 119 | gọi bởi -237 | 12h-13h | Bộ B + 10 điểm |
| Ca Đít | -342 | 60.000.000 | 1.000 | 119 | gọi bởi -237 | 12h-13h | Bộ B + 10 điểm |
| Mabư | -236 | 100.000.000 | 10.000 | 120 | **10** | 12h-13h | Bộ B + **25 điểm** |
| Drabura 3 | -343 | 20.000.000 | 100.000 | 114 | gọi bởi -236 | 12h-13h | Bộ B + 20 điểm |
| Mabư mập (5 hình dạng) | -214 | 50tr/60tr/80tr/100tr/150tr | 500.000 | 127 | **7** (1/khu) | 14h-15h | chỉ +5 Point, không rơi đồ |
| Super Bư (bụng) | -348 | 50.000.000 | 500.000 | 127,128 | **7** | 14h-15h | chỉ +5 Point |

> **Số bản đã kiểm chứng**: `Map.initZone` lấy số khu từ cột `zones` của `map_template`
> **trừ khi** map có `type` đặc biệt. Map 111 và 114-120 đều có `type = 0` (MAP_NORMAL)
> và `zones = 10` → **10 khu, 10 boss mỗi loại**. Map 127 và 128 có `type = 9`
> (`MAP_MABU_14H`) → số khu lấy từ `MajinBuu14H.AVAILABLE = 7` → **7 boss**.
> (`MajinBuuService.AVAILABLE = 13` chỉ áp cho map có `type = 5 MAP_MA_BU`, mà các map
> 114-120 trong DB **không** được đặt type đó.)

#### 1.3 Boss mini / lang thang

| Tên | id | Máu | Sát thương | Map | Bản | Nghỉ | Rơi chính |
|---|---:|---:|---:|---|---:|---|---|
| Sói hẹc quyn | -77 | 10.000 (vô nghĩa) | 1.000 | 73 map thường | 2 | 10 phút | **bất tử**, thưởng qua Cục xương |
| Ở dơ | -78 | 500.000 | 1.000 | 73 map thường | 5 | 600.000 giây ≈ 6,9 ngày | 2× Hộp quà Goku Day |
| Ăn trộm | -365 | `nextInt(100)` | máu/10 | 73 map thường | 5 | 10 phút | Vàng đã trộm ×80% + 2 Hộp quà Goku Day |
| Mặt Trời | -79 | 100 | 1 | 5,7,0,14 | **20** | 10 phút | 50% Mặt trời tí hon (1562) |
| Rồng Nhí | -386998 | 50.000.000 | 1 | 0-20, 24-37 | 0 (không được tạo) | 1 phút | 20%→20% Trứng vàng rồng nhí (1821) |
| Virut | -79 | 100 | 10 | 5,7,0,14 | **0 (code chết)** | 10 phút | 2 Hộp quà Goku Day |

#### 1.4 Boss sự kiện

| Tên | id | Máu | Sát thương | Map | Bản | Trạng thái sự kiện | Rơi chính |
|---|---:|---:|---:|---|---:|---|---|
| Broly | -1822 | `nextInt(500,100000)` khi vào map | máu/100 | 5,13,20,27-38 | **30** | **BẬT** (Default) | không rơi đồ, chết → sinh Super Broly |
| Super Broly | -82282 | `nextInt(1.500.000, 16.070.777)` | máu/100 | theo Broly | sinh động | **BẬT** | tặng **đệ tử thường** nếu chưa có |
| Ma trơi | -349 | 500.000 | 100 | 73 map thường | 0 | Halloween — **TẮT** | Bí ngô (585) |
| Dơi | -350 | 500.000 | 100 | 73 map thường | 0 | Halloween — **TẮT** | Bí ngô (585) |
| Bí ma | -351 | 500.000 | máu người chơi/30-50 | 73 map thường | 0 | Halloween — **TẮT** | Bí ngô (585) |
| Ông già Noel | -353 | 500 | 5.000.000 | 73 map thường | 0 | Noel — **TẮT** | **bất tử**, thả Hộp quà giáng sinh (648) |
| Thủy Tinh | -355 | 50.000.000 | 1.000 | 73 map thường | **10** | Hùng Vương — **BẬT** | Cải trang Thủy Tinh (422) |
| Sơn Tinh | -354 | 50.000.000 | 1.000 | 73 map thường | đi kèm -355 | **BẬT** | Cải trang Sơn Tinh (421) |
| Lân con | -371 (trừ ngẫu nhiên) | 5.000.000 | 5.000 | 73 map thường | 0 | Tết — **TẮT** | không chết, đi theo và tặng quà |
| Khỉ đột | -344 | 100.000.000 | 100.000 | 0-20 | 0 | Trung Thu — **TẮT** | Đuôi khỉ (1045) |
| Nguyệt thần | -345 | 50.000.000 | 1.000 | 0-20 | 0 | Trung Thu — **TẮT** | item 2123 (**không tồn tại trong DB**) |
| Nhật thần | -346 | 50.000.000 | 1.000 | 0-20 | đi kèm -345 | **TẮT** | item 2124 (**không tồn tại trong DB**) |

#### 1.5 Boss luyện tập tự động

| Tên | id | Máu | Sát thương | Map | Gọi bởi | Rơi đồ |
|---|---:|---:|---:|---|---|---|
| Karin | -357 | 500 | 500 | 46 Tháp Karin | NPC Thần Mèo Karin | không |
| Tàu Pảy Pảy | -309 | 1.000 | 500 | 46 Tháp Karin | NPC | không (khoá tới NV 10) |
| Yajirô | -358 | 1.100 | 1.100 | 46 Tháp Karin | NPC | không |
| Mr.PôPô | -359 | 5.100 | 1.100 | 46 Tháp Karin | NPC | không |
| Thượng đế | -360 | 1.000 | 10.000 | 49 Phòng tập thời gian | NPC Thượng Đế | không |
| Khỉ Bubbles | -361 | 30.000 | 30.000 | 48 Hành tinh Kaio | NPC | không |
| Thần Vũ Trụ | -362 | 45.000 | 45.000 | 48 Hành tinh Kaio | NPC Thần Vũ Trụ | không |
| Tổ sư Kaio | -363 | 45.000 | 45.000 | 50 Thánh địa Kaio | NPC Tổ Sư Kaio | không (bất tử, chỉ cộng SM/TN) |
| Whis | -364 | 550.000 × cấp | 10.000 × cấp | 154 Hành tinh Bill | NPC Whis | không |

---

### 2. BOSS THẾ GIỚI TRÊN BẢN ĐỒ THƯỜNG

#### 2.1 Nhóm Nappa — Kuku, Mập Đầu Đinh, Rambo

Ba con độc lập, mỗi con **5 bản** chạy song song (`BossManager.loadBoss`).
Cả ba đều dựng với `isNotifyDisabled = true, isZone01SpawnDisabled = true`
→ **không loa toàn server**, và **chỉ hiện ở khu ≥ 2**.

##### Kuku (-20)

| Mục | Giá trị |
|---|---|
| Nhóm | Boss thế giới (Nappa) |
| Hành tinh | Xayda |
| Hình dạng | 1 |
| Máu | 500.000 |
| Sát thương | 9.000 |
| % giảm sát thương | 0% |
| Máu hiệu dụng | 500.000 |
| Giáp | **không có giáp** (`def = 0`) |
| Né đòn | 0% |
| Tạo hình | head 159 / body 160 / leg 161 |

Kỹ năng:

| id | Tên (skill_template) | Cấp | Hồi (ms) |
|---:|---|---:|---:|
| 3 | Chiêu Masenko | 3 | 1.000 |
| 17 | Liên hoàn | 7 | 1.000 |

- **Map**: 68 Thung lũng Nappa, 69 Vực cấm, 70 Núi Appule, 71 Căn cứ Raspberry,
  72 Thung lũng Raspberry (chọn ngẫu nhiên mỗi lần hồi sinh).
- **Số bản cùng lúc**: 5.
- **Thời gian**: nghỉ 600 giây (10 phút) sau khi rời map. Tự rời map sau **900.000 ms
  (15 phút)** kể từ lúc vào map — lưu ý đoạn "gia hạn khi có người trong khu" **đã bị
  comment**, nên Kuku/Mập Đầu Đinh/Rambo rời map đúng 15 phút bất kể có người đánh hay không.
- **Rơi đồ**: Bộ A — 100% **Vàng (190)** 20.000-30.000; 80% một trong
  **Ngọc Rồng 5/6/7 sao (18/19/20)** ×1. Tất cả rơi ra đất, khoá cho người kết liễu.
  `+5 Point` sự kiện. Gọi `TaskService.checkDoneTaskKillBoss`.
- **Cơ chế đặc biệt**: không có.
- **Lời thoại**: không có thoại mở/kết; giữa trận lặp các câu khiêu khích
  ("Ta sẽ tàn sát khu này trong vòng 5 phút nữa", "Tao đã có lệnh của đại ca Fide rồi"…).

##### Mập Đầu Đinh (-21)

Giống Kuku về mọi cơ chế. Khác:

| Mục | Giá trị |
|---|---|
| Máu | 1.000.000 |
| Sát thương | 10.000 |
| Tạo hình | 165/166/167 |
| Kỹ năng | 4 Chiêu đấm Galick lv7 (1.000 ms); 5 Chiêu Antomic lv7 (10.000 ms) |
| Map | 63 Trại lính Fide, 64 Núi dây leo, 65 Núi cây quỷ, 66 Trại qủy già, 67 Vực chết |

Lời thoại giữa trận: "HAHAHA", "Tao chỉ cần 10 giây để giết hết bọn mày", "Chết hết đi cho tao"…

##### Rambo (-22)

| Mục | Giá trị |
|---|---|
| Máu | 1.500.000 |
| Sát thương | 12.400 |
| Tạo hình | 162/163/164 |
| Kỹ năng | 4 Chiêu đấm Galick lv7 (1.000 ms); 5 Chiêu Antomic lv7 (10.000 ms) |
| Map | 74 Đồi cây Fide, 75 Khe núi tử thần, 76 Núi đá, 77 Rừng đá |
| Thoại kết | "Ôi bạn ơi..." |

---

#### 2.2 Tiểu đội sát thủ (Trái Đất) — TĐT + Số 1/2/3/4

`BossManager.loadBoss` chỉ tạo **1 bản Tiểu đội trưởng (-27)**; bốn con Số 1-4 được
`Boss` sinh tự động từ `bossesAppearTogether` của TĐT và gán `parentBoss = TĐT`.
Tất cả đều `isZone01SpawnDisabled = true` (chỉ hiện khu ≥ 2), loa toàn server bật.

**Cơ chế đội hình** (`AppearType.APPEAR_WITH_ANOTHER`):
1. TĐT ra map → `wakeupAnotherBossWhenAppear()` đánh thức cả 4 đàn em cùng lúc.
2. TĐT lập tức vào trạng thái `AFK` (`doneChatS` ghi đè) → **TĐT đứng im, không đánh**
   cho tới khi đàn em chết hết.
3. Mỗi đàn em khi chết, `doneChatE()` kiểm tra xem còn đồng đội nào sống không; nếu hết
   thì gọi `parentBoss.changeStatus(ACTIVE)` → TĐT mới bắt đầu đánh.
4. `moveTo()` bị chặn khi `currentLevel == 1` — nhưng cả nhóm chỉ có 1 hình dạng nên
   nhánh này không bao giờ chạy (code thừa).

**Chung cho cả 5 con**: giáp 0, né đòn 0%, % giảm sát thương 0%, dùng `Boss.injured` mặc định
→ **máu hiệu dụng = máu danh nghĩa**. Tự rời map sau 15 phút *không có người trong khu*
(ở nhóm này `st` **được** gia hạn khi khu còn người, nên có người đánh thì boss ở lại).

| Tên | id | Máu | Sát thương | Tạo hình | Kỹ năng (id-tên-cấp-hồi ms) |
|---|---:|---:|---:|---|---|
| Số 4 | -23 | 25.000.000 | 10.000 | 168/169/170 | 17 Liên hoàn 7 (1.000); 3 Masenko 7 (1.000); 22 Thôi miên 7 (100.000) |
| Số 3 | -24 | 30.000.000 | 11.000 | 174/175/176 | 17 Liên hoàn 7 (1.000); 5 Antomic 4 (1.000) |
| Số 2 | -25 | 30.500.000 | 12.000 | 171/172/173 | 4 Galick 7 (1.000); 5 Antomic 3 (3.000) |
| Số 1 | -26 | 40.000.000 | 12.500 | 177/178/179 | 17 Liên hoàn 7 (1.000); 1 Kamejoko 4 (10.000) |
| Tiểu đội trưởng | -27 | 50.000.000 | 13.000 | 180/181/182 | 3 Masenko 7 (1.000); 4 Galick 7 (1.000) |

- **Map**: 79 Núi khỉ đỏ, 81 Hang quỷ chim, 82 Núi khỉ đen, 83 Hang khỉ đen.
- **Nghỉ**: TĐT `REST_5_M` = 300 giây. Đàn em không có `secondsRest` riêng (đánh thức theo TĐT).
- **Rơi đồ**: cả 5 con dùng **Bộ A** — Vàng 20.000-30.000 (100%) + 80% Ngọc Rồng 5/6/7 sao ×1,
  khoá cho người kết liễu, `+5 Point`.
- **Cơ chế đặc biệt của TĐT**: mỗi **10 giây** gọi `bodyChangePlayerInMap()` —
  với xác suất 5/10 cho **mỗi người trong khu**, áp hiệu ứng **hoán đổi thân xác**
  (`EffectSkillService.setIsBodyChangeTechnique`), kèm chat "Úm ba la xì bùa".
- **Lời thoại**: thoại giữa trận dùng chung ("Oải rồi hả", "Một mình tao chấp hết tụi bây",
  "Đại ca Fide có nhầm không nhỉ"…). Thoại chết của đàn em: "Fide gọi ta về, ngươi có ngon
  thì chờ ở đây" + gọi tên đồng đội tiếp theo ("Để tao xử nó cho").

---

#### 2.3 Tiểu đội sát thủ Namek — TĐT NM + Số 1/2/3/4 NM

Cùng cơ chế đội hình với 2.2 (`-315` là boss cha, tạo 1 bản trong `loadBoss`).

> **Lưu ý đã sửa trong code**: phần `mapJoin` có ghi chú FIX bỏ hết map đầu game Namếc
> (7-13, 25, 33, 34, 43) vì tiểu đội dame 10.000-15.000 giết người chơi mới 1 đòn;
> nay chuyển sang khu Fide/Nappa.

| Tên | id | Máu | Sát thương | Tạo hình | Kỹ năng |
|---|---:|---:|---:|---|---|
| Số 4 Namek | -311 | 2.500.000 | 10.000 | 168/169/170 | 17 Liên hoàn 7 (1.000); 3 Masenko 7 (1.000); 22 Thôi miên 7 (100.000) |
| Số 3 Namek | -312 | 3.000.000 | 10.000 | 174/175/176 | 17 Liên hoàn 7 (1.000); 5 Antomic 4 (1.000) |
| Số 2 Namek | -313 | 3.500.000 | 12.200 | 171/172/173 | 4 Galick 7 (1.000); 5 Antomic 3 (3.000) |
| Số 1 Namek | -314 | 4.000.000 | 13.200 | 177/178/179 | 17 Liên hoàn 7 (1.000); 1 Kamejoko 4 (10.000) |
| Tiểu đội trưởng Namek | -315 | 5.000.000 | 15.000 | 180/181/182 | 3 Masenko 7 (1.000); 4 Galick 7 (1.000) |

- **Map**: 73 Thung lũng chết, 74 Đồi cây Fide, 75 Khe núi tử thần, 76 Núi đá, 77 Rừng đá.
- **Nghỉ**: TĐT NM 300 giây; tự rời map sau 15 phút không người.
- **Giáp**: không có. **% giảm sát thương**: 0%. Máu hiệu dụng = máu danh nghĩa.
- **Rơi đồ — khác hẳn bản Trái Đất, đây là bảng rơi rất hậu**:
  - **Ngọc (item 77)**: 4 vòng lặp rơi liên tiếp, mỗi ItemMap `nextInt(1, 2..5)` viên.
    Tổng kỳ vọng khoảng **8-25 Ngọc mỗi con**, rơi ra đất khoá theo người kết liễu.
  - **1 Cải trang riêng** (100%, có chỉ số shop):
    -311 → **429 Cải trang thành Số 4**; -312 → **430 Số 3**; -313 → **431 Số 2**;
    -314 → **432 Số 1**; -315 → **433 Tiểu Đội Trưởng Ginyu**.
  - **1 Ngọc Rồng 6 sao (19)** + **1 Ngọc Rồng 7 sao (20)**, 100%.
  - `+5 Point` cho TĐT NM, `+1 Point` cho Số 1-4 NM.
  - **Không** rơi Vàng.
- **Lời thoại**: giữa trận như bản Trái Đất; thoại chết "Cay quá!", "Ta mà lại thua được
  sao?", "Hãy trả thù cho ta!".

---

#### 2.4 Fide đại ca (-28) — boss 3 hình dạng

| Hình dạng | Tên | Máu | Sát thương | % giảm ST | Máu hiệu dụng | Tạo hình |
|---:|---|---:|---:|---:|---:|---|
| 0 | Fide đại ca 1 | 10.000.000 | 22.000 | 0% | 10.000.000 | 183/184/185 |
| 1 | Fide đại ca 2 | 20.000.000 | 25.000 | 0% | 20.000.000 | 186/187/188 |
| 2 | Fide đại ca 3 | 30.000.000 | 30.000 | 0% | 30.000.000 | 189/190/191 |

- **Giáp**: không có. **Né đòn**: 0%. Dùng `Boss.injured` mặc định, không có trần sát thương.
- **Kỹ năng** (cả 3 hình dạng): 3 Chiêu Masenko lv7 (1.000 ms); 4 Chiêu đấm Galick lv7 (1.000 ms).
- **Map**: 80 Núi khỉ vàng. 1 bản.
- **Thời gian**: hình dạng 1 nghỉ 600 giây (10 phút). Hình dạng 2 và 3 là
  `AppearType.ANOTHER_LEVEL` → **không tự hồi sinh độc lập**, chỉ hiện ra khi hình dạng
  trước chết (`leaveMap()` giữ nguyên khu và `RESPAWN` ngay).
  Tự rời map sau 15 phút không có người trong khu.
- **Rơi đồ**: Bộ A, **mỗi hình dạng khi chết đều rơi** — Vàng 20.000-30.000 +
  80% Ngọc Rồng 5/6/7 sao. Tức đánh hết 3 hình dạng được 3 lượt rơi.
- **Cơ chế đặc biệt**: đổi hình dạng khi chết (biến hình), không có cơ chế nào khác.
- **Lời thoại**: mở màn có đối thoại 2 chiều với người chơi trong khu (prefix `-2` = câu
  nói được gán cho một người chơi ngẫu nhiên gần đó): "Fide!!!, với những gì ngươi đã làm
  với người Xayda và Namek..." → "Ta phán ngươi tội: tử hình" → Fide đáp
  "Khẩu khí ngang tàng lắm". Thoại chuyển hình dạng: "Ác quỷ biến hình, hây aaaa...".
  Thoại chết cuối: "Lũ khốn.. Một ngày nào đó ta sẽ quay lại và trả thù các ngươi".

---

#### 2.5 Dr.Kôrê (-31) + Android 19 (-30)

##### Dr.Kôrê (-31)

| Mục | Giá trị |
|---|---|
| Hành tinh | Trái Đất |
| Hình dạng | 1 |
| Máu | 2.000.000 |
| Sát thương | 12.000 |
| % giảm sát thương | 0% |
| Máu hiệu dụng | 2.000.000 (nhưng xem cơ chế hấp thụ) |
| Giáp | không có |
| Tạo hình | 255/256/257 |

Kỹ năng: 22 Thôi miên lv3 (10.000 ms); 1 Chiêu Kamejoko lv7 (10.000 ms); 17 Liên hoàn lv7 (1.000 ms).

- **Map**: 96 Cao nguyên, 94 Đảo Balê, 93 Thành phố phía nam. 1 bản. Nghỉ 10 phút.
  Tự rời sau 15 phút không người.
- **Cơ chế đặc biệt — HẤP THỤ CHƯỞNG (đã được sửa để thực sự chạy)**:
  nếu người chơi đánh bằng **Kamejoko (1) / Masenko (3) / Antomic (5)** thì
  `injured()` trả về **0 sát thương** và **hồi đúng lượng sát thương đó thành máu cho boss**
  (`PlayerService.hoiPhuc(this, damage, 0)`), kèm 20% chat "Hấp thụ.. các ngươi nghĩ sao vậy?".
  → **Chỉ có thể hạ Dr.Kôrê bằng chiêu đấm (Dragon/Demon/Galick/Liên hoàn/Kaioken…).**
  Ghi chú trong code nêu rõ: trước đây hàm này khai báo `int damage` thay vì `long damage`
  và thiếu `@Override` nên **cơ chế hấp thụ chưa từng chạy**; nay đã sửa.
- **Đội hình**: gọi kèm Android 19; `doneChatS()` bật PK cho Android 19 trước.
  `changeToTypePK()` của Kôrê chat "Mau đền mạng cho thằng em trai ta"
  (tức Kôrê chỉ đánh sau khi 19 chết).
- **Thoại giữa trận riêng**: ~1/61 lượt chat sẽ ra lệnh "Hút năng lượng của nó, mau lên"
  và Android 19 đáp "Tuân lệnh đại ca, hê hê hê".
- **Rơi đồ**: Bộ A (Vàng 20.000-30.000 + 80% NR 5/6/7 sao), `+5 Point`.
- **Lời thoại mở**: đối thoại dài với người chơi ("Chúng mày là ai từ đâu tới? Cho tao xin
  cái địa chỉ"), kết bằng "Số 19! Xuất chiêu đi nào" / "Okê đại ca, em sẽ xử lý bọn này
  trong vòng 2 tiếng."

##### Android 19 (-30)

| Mục | Giá trị |
|---|---|
| Máu | 1.000.000 |
| Sát thương | 12.200 |
| % giảm sát thương | 0% |
| Giáp | không có |
| Tạo hình | 249/250/251 |
| Kỹ năng | 1 Kamejoko lv7 (1.000 ms); 17 Liên hoàn lv7 (10.000 ms) |
| Xuất hiện | `APPEAR_WITH_ANOTHER` — cùng Dr.Kôrê, map 96/94/93 |

- **Cơ chế đặc biệt — HẤP THỤ CHƯỞNG 80%**: đánh bằng Kamejoko/Masenko/Antomic →
  sát thương = 0 và boss **hồi 80% lượng sát thương đó**. Giống Kôrê, chỉ hạ được bằng chiêu đấm.
  ⚠️ Hàm này **không kiểm tra null** cho `plAtt.playerSkill.skillSelect.template`
  (khác với bản đã sửa của Kôrê) → có thể ném NPE nếu người tấn công chưa chọn kỹ năng.
- Khi 19 rời map/chết → `wakeupAnotherBossWhenDisappear()` bật PK cho Dr.Kôrê.
- **Rơi đồ**: Bộ A.

---

#### 2.6 Android 14 (-33) + Android 13 (-32) + Android 15 (-34)

Map chung: **104 Sân sau siêu thị**. `loadBoss` tạo 1 bản Android 14; 13 và 15 sinh theo.

| Tên | id | Máu | Sát thương | Tạo hình | Kỹ năng | Kiểu xuất hiện |
|---|---:|---:|---:|---|---|---|
| Android 14 | -33 | 4.000.000 | 12.000 | 246/247/248 | 1 Kamejoko 7 (10.000); 17 Liên hoàn 7 (1.000) | boss cha, nghỉ 10 phút |
| Android 13 | -32 | 3.000.000 | 12.055 | 252/253/254 | 1 Kamejoko 7 (10.000); 17 Liên hoàn 7 (1.000) | `CALL_BY_ANOTHER` |
| Android 15 | -34 | 5.000.000 | 12.200 | 261/262/263 | 1 Kamejoko 7 (10.000); 17 Liên hoàn 7 (1.000) | `APPEAR_WITH_ANOTHER` |

- **Giáp**: không có. **% giảm sát thương**: 0%. Máu hiệu dụng = máu danh nghĩa.
- **Cơ chế đặc biệt — "hồi máu và gọi số 13"**:
  - Khi **Android 14 hoặc Android 15** sắp chết (đòn có `damage >= hp`), đòn đó bị **huỷ**
    (trả 0) và `callApk13()` chạy:
    1. **Android 13 được đánh thức** (`RESPAWN`);
    2. Android 15 bị chuyển sang non-PK và **hồi đầy máu**;
    3. Android 14 cũng chuyển non-PK và **hồi đầy máu**.
  - → **Phải giết Android 13 trước**, nếu không 14 và 15 sẽ hồi đầy máu một lần.
  - Cơ chế chỉ chạy **một lần mỗi lượt** (cờ `callApk13`, reset ở `resetBase()`).
  - **Android 13 cũng bất tử** chừng nào Android 15 hoặc Android 14 còn sống:
    `Android13.injured` kiểm tra nếu `damage >= hp` mà 15 còn sống **hoặc** boss cha
    còn sống thì trả 0.
    ⚠️ Logic ở `Android13.injured` (dòng 40-62) có lỗi: biến `flag` bị đặt về `false`
    khi `this.parentBoss` **chưa chết**, nghĩa là **13 không thể chết chừng nào 14 còn sống**,
    trong khi 14 lại không thể chết trước khi gọi 13 xong. Xem mục Ghi chú.
- **Rơi đồ**: cả 3 con dùng Bộ A (Vàng 20.000-30.000 + 80% NR 5/6/7 sao), `+5 Point`.
- **Thời gian**: Android 14 nghỉ 600 giây. Không có `autoLeaveMap` riêng cho 14/13/15
  → chúng **ở lại map cho tới khi chết**.
- **Lời thoại**: Android 13 có đoạn kể lể nguồn gốc ("Bọn ta là rôbốt sát thủ, sinh ra từ
  máy tính ngài Kôrê... cho một mục tiêu duy nhất là giết Sôngôku!"). Thoại chết của 14:
  "Số 14 và số 15 tiêu tùng cả rồi à?"; của 15: "Thì ra vẫn chỉ là một đống sắt vụn!".

---

#### 2.7 King Kong (-37) + Pic (-35) + Poc (-36)

Map chung: **97 Thành phố phía bắc, 98 Ngọn núi phía bắc, 99 Thung lũng phía bắc**.
`loadBoss` tạo 1 bản King Kong.

| Tên | id | Máu | Sát thương | Tạo hình | Kỹ năng |
|---|---:|---:|---:|---|---|
| King Kong | -37 | 20.000.000 | 12.000 | 243/244/245 | 3 Masenko 7 (1.000); 4 Galick 7 (1.000) |
| Pic | -35 | 10.000.000 | 17.022 | 237/238/239 | 3 Masenko 7 (1.000); 4 Galick 7 (1.000) |
| Poc | -36 | 15.000.000 | 18.000 | 240/241/242 | 3 Masenko 7 (1.000); 4 Galick 7 (1.000) |

- **Giáp**: không có. **% giảm sát thương**: 0%. Máu hiệu dụng = máu danh nghĩa.
- **Thứ tự đánh (cơ chế xếp hàng)**:
  - Cả King Kong và Pic đều `doneChatS()` → `AFK` ngay sau lời thoại mở
    → **chỉ Poc đánh trước**.
  - Poc chết → `doneChatE()` gọi Pic sang `ACTIVE`.
  - Pic chết → `doneChatE()` gọi `parentBoss` (King Kong) sang `ACTIVE`.
  - → Trình tự bắt buộc: **Poc → Pic → King Kong**.
- **Thời gian**: King Kong nghỉ 600 giây. Cả ba tự rời map sau 15 phút không người trong khu.
- **Rơi đồ**: cả ba dùng Bộ A, `+5 Point`.
- **Lời thoại**: Pic mở màn hỏi tung tích Gôku; Poc đối đáp "Đừng tưởng ta đây là con gái
  mà dễ bắt nạt nhé"; King Kong "Mau đền mạng cho những người bạn của ta".

---

#### 2.8 Xên bọ hung (-100) — 3 hình dạng, hấp thụ người chơi

| Hình dạng | Tên | Máu | Sát thương | % giảm ST (bảng) | Chia trong `injured` | Máu hiệu dụng |
|---:|---|---:|---:|---:|---|---:|
| 0 | Xên bọ hung | 50.000.000 | 20.000 | 0% | `damage / 2` | **100.000.000** |
| 1 | Xên hoàn thiện | 100.000.000 | 25.000 | 0% | `damage / 2` | **200.000.000** |
| 2 | Xên hoàn thiện | 150.000.000 | 30.000 | 0% | `damage / 2` | **300.000.000** |

Tổng máu hiệu dụng cả 3 hình dạng: **600 triệu**.

- **Giáp**: không có (`subDameInjureWithDeff` trừ 0). **Né đòn**: 0%.
- **Khiên năng lượng của người chơi**: nếu boss đang có khiên thì sát thương bị chia thêm 4;
  đòn vượt `hpMax` sẽ phá khiên.
- **Kỹ năng**:

| Hình dạng | Kỹ năng (id-tên-cấp-hồi ms) |
|---:|---|
| 0 | 1 Kamejoko 7 (1.000) ×2; 17 Liên hoàn 7 (10.000); 20 Dịch chuyển tức thời 3 (10.000) |
| 1 | 1 Kamejoko 7 (1.000); 1 Kamejoko 7 (5.000); 17 Liên hoàn 7 (10.000) |
| 2 | 1 Kamejoko 7 (1.000); 1 Kamejoko 7 (5.000); 20 Dịch chuyển tức thời 7 (10.000); 17 Liên hoàn 7 (10.000); 22 Thôi miên 3 (100.000) |

- **Map**: 100 Thị trấn Ginder. 1 bản. Nghỉ **1.800 giây (30 phút)** cho hình dạng 0;
  hình dạng 1 và 2 là `ANOTHER_LEVEL` (chỉ hiện khi hình dạng trước chết).
- **Cơ chế đặc biệt — HẤP THỤ NGƯỜI CHƠI** (`hapThu()`, chạy mỗi lượt `active`):
  - Điều kiện: đã qua `Util.nextInt(10000, 20000)` ms kể từ lần trước **và** trúng xác suất **1%**.
  - Chọn 1 người ngẫu nhiên trong khu → boss **dịch chuyển tới chỗ người đó**, rồi:
    - `dameg += 5% sát thương của nạn nhân`
    - `hpg += 2% máu hiện tại của nạn nhân`
    - `critg++`
    - boss hồi máu bằng đúng máu của nạn nhân
    - **nạn nhân bị giết ngay** (`injured(null, hpMax, true, false)`)
  - Thông báo: "Bạn vừa bị <tên> hấp thu!" + chat cả khu
    "Ui cha cha, kinh dị quá. <tên người chơi> vừa bị tên <tên boss> nuốt chửng kìa!!!"
  - → Càng để lâu boss càng mạnh lên vĩnh viễn trong lượt đó.
- **Rơi đồ** (mỗi hình dạng chết đều rơi):
  - 100% **Vàng (190)** `nextInt(20000, 30001)`
  - 80% một trong **Ngọc Rồng 5/6/7 sao** ×1
  - `+5 Point`, cập nhật danh hiệu **Trùm săn boss**
- **Lời thoại**: kịch bản Cell kinh điển ("Ta sẽ hấp thụ số 17 và 18 để đạt được dạng hoàn
  hảo!", "Làm đứt đuôi ta ư? Đừng quên ta có tế bào của Picôlô!!").

---

#### 2.9 Siêu Bọ Hung (-101) + Xên con 1-7 (-102…-108)

##### Siêu Bọ Hung (-101)

| Hình dạng | Tên | Máu | Sát thương | % giảm ST (bảng) | Chia trong `injured` | Máu hiệu dụng |
|---:|---|---:|---:|---:|---|---:|
| 0 | Xên Hoàn Thiện | 150.000.000 | 35.000 | 0% | `damage / 3` | **450.000.000** |
| 1 | Siêu Bọ Hung | 200.000.000 | 40.000 | 0% | `damage / 3` | **600.000.000** |

- **Giáp**: không có. **Né đòn**: 0%. Khiên năng lượng → chia thêm 4.
- **Kỹ năng**:
  - Hình dạng 0: 1 Kamejoko 7 (10.000); 20 Dịch chuyển tức thời 7 (20.000);
    4 Galick 7 (1.000); 6 Thái Dương Hạ San 7 (50.000)
  - Hình dạng 1: 1 Kamejoko 7 (5.000); 20 Dịch chuyển tức thời 3 (30.000);
    4 Galick 7 (1.000); 22 Thôi miên 7 (30.000)
- **Map**: 103 Võ đài Xên bọ hung. 1 bản. Nghỉ 1.800 giây (30 phút) cho cả hai hình dạng.
  Tự rời map sau 15 phút không người trong khu.
- **Cơ chế đặc biệt 1 — GỌI 7 XÊN CON**:
  - Lần đầu tiên nhận một đòn `damage >= hp` (cờ `callCellCon`, reset mỗi lượt):
    đòn bị huỷ (trả 0), boss chuyển `AFK` + non-PK, **hồi đầy máu**, rồi lần lượt chat
    "Hãy đấu với 7 đứa con của ta, chúng đều là siêu cao thủ" → "Cứ chưởng tiếp đi haha"
    → "Liệu mà giữ mạng đấy" (mỗi câu cách 2 giây) và đánh thức **cả 7 Xên con**.
  - Sau khi Xên con cuối cùng chết, `XENCON*.doneChatE()` trả Siêu Bọ Hung về `ACTIVE`.
- **Cơ chế đặc biệt 2 — TỰ NỔ KHI CHẾT**:
  khi máu về 0, thay vì chết bình thường boss gọi `setBom(plAtt)` (`Boss.java:901`):
  - chat "Rồi, rồi, mày xong rồi!", hiện hiệu ứng gồng 2 giây
  - sau 2.500 ms: boss chết, **mọi quái trong khu bị giết**, và **mọi người chơi trong khu
    nhận sát thương bằng `hpMax` của boss** (150-200 triệu) — gần như giết sạch khu.
  - Trong lúc chuẩn bị nổ (`prepareBom = true`), `injured` trả 0 và `update()` dừng.
- **Cơ chế đặc biệt 3 — MC bình luận**: nếu khu có NPC, mỗi 3 giây NPC chat luân phiên
  3 câu ("Thưa quý vị và các bạn, đây đúng là trận đấu trời long đất lở"…) và di chuyển
  ngẫu nhiên mỗi 15 giây.
- **Rơi đồ** (Bộ B):
  - 5% **đồ Thần Linh** (`randDoTLBoss`)
  - 100% **Vàng (190)** 20.000-30.000
  - **30%** một món trang bị thường + sao pha lê (80% sao 1-3 / 17% sao 4-5 / 3% sao 6)
  - **80%** ngọc rồng `{15,16,17,18,19,20}` số lượng `nextInt(1,3)` — kèm `+5 Point`
    (⚠️ `+5 Point` nằm **bên trong** nhánh 80%, tức 20% trường hợp không được điểm)
  - Cập nhật danh hiệu Trùm săn boss, `checkDoneTaskKillBoss`.

##### Xên con 1-7 (-102 … -108)

Bảy con giống hệt nhau:

| Mục | Giá trị |
|---|---|
| Máu | 5.000.000 mỗi con |
| Sát thương | 15.000 |
| % giảm sát thương | 0% |
| Giáp | không có |
| Máu hiệu dụng | 5.000.000 (dùng `Boss.injured` mặc định, không chia) |
| Tạo hình | 264/265/266 |
| Kỹ năng | 1 Kamejoko lv7 (5.000 ms); 4 Galick lv7 (1.000 ms) |
| Map | 103 Võ đài Xên bọ hung |
| Xuất hiện | `CALL_BY_ANOTHER` — chỉ khi Siêu Bọ Hung gọi |
| Vị trí vào map | ngay cạnh boss cha (`parentBoss.location.x ± 100`) |
| Tự rời map | 15 phút không người trong khu |

- **Rơi đồ mỗi con**: 100% **Vàng (190)** `nextInt(20000, 30001)`; 80% một trong
  **Ngọc Rồng 5/6/7 sao** ×1, và `+5 Point` (nằm trong nhánh 80%).
  Cập nhật danh hiệu Trùm săn boss.
- **Tổng nếu giết cả 7**: ~7 lần Vàng 20k-30k (≈175.000 Vàng) + ~5,6 Ngọc Rồng cấp cao.

---

#### 2.10 Black Goku (-203) — boss thế giới có giảm sát thương MỚI

| Hình dạng | Tên | Máu | Sát thương | % giảm (bảng mới) | Chia sẵn trong `injured` | Tỉ lệ ST thực trừ | **Máu hiệu dụng** |
|---:|---|---:|---:|---:|---|---:|---:|
| 0 | Black Goku | 500.000.000 | 50.000 | **30%** | không | 0,70 | **≈ 714.285.714** |
| 1 | Super Black Goku | 2.000.000.000 | 100.000 | **25%** | `damage / 2` | 0,375 | **≈ 5.333.333.333** |

Tổng máu hiệu dụng cả 2 hình dạng: **≈ 6,05 tỉ**.

- **Giáp**: không có.
- **Chuỗi xử lý sát thương thực tế** (`BlackGoku.injured`):
  1. né đòn `tlNeDon/1000` → luôn 0% (boss không có `tlNeDon`);
  2. nếu `currentLevel != 0` → `damage /= 2`;
  3. `damage = subDameInjureWithDeff(damage - Util.nextInt(100000))`
     → **trừ thẳng một lượng ngẫu nhiên 0-99.999 sát thương mỗi đòn** (giáp thì trừ 0);
  4. nếu boss đang có khiên năng lượng → `damage = 1` (và đòn > `hpMax` sẽ phá khiên);
  5. `applyDamageReduce()` — cắt **30%** (hình dạng 0) hoặc **25%** (hình dạng 1);
  6. `subHP`.
- **Chat cảnh báo**: lần đầu mỗi hình dạng, boss chat
  `"Đòn của ngươi yếu đi 30% trước ta"` / `"... 25% trước ta"`.
- **Kỹ năng**:

| Hình dạng | Kỹ năng |
|---:|---|
| 0 | 1 Kamejoko 7 (100 ms); 8 Tái tạo năng lượng 7 (1.000.000 ms); 19 Khiên năng lượng 7 (300.000 ms); 4 Galick 7 (100 ms) |
| 1 | 6 Thái Dương Hạ San 7 (30.000); 8 Tái tạo năng lượng 7 (300.000); 19 Khiên năng lượng 7 (300.000); 1 Kamejoko 7 (100); 4 Galick 7 (100) |

- **Map**: 102 Nhà Bunma, 92 Thành phố phía đông, 93 Thành phố phía nam, 94 Đảo Balê,
  96 Cao nguyên, 97 Thành phố phía bắc, 98 Ngọn núi phía bắc, 99 Thung lũng phía bắc,
  100 Thị trấn Ginder.
- **Số bản cùng lúc**: **2** (`createBoss(BLACK_GOKU, 2)`).
- **Xuất hiện**: `isNotifyDisabled = false` (**có loa toàn server**),
  `isZone01SpawnDisabled = true` (**chỉ hiện khu ≥ 2**). Tên hiển thị được gắn số ngẫu nhiên
  1-100 mỗi lần vào map ("Black Goku 47").
- **Thời gian**: nghỉ 300 giây (5 phút). Tự rời map sau `nextInt(600.000, 900.000)` ms
  (10-15 phút) kể từ lúc vào; đồng hồ được **gia hạn `nextInt(300.000, 900.000)` ms mỗi
  lượt quét nếu khu còn người**, nên có người đánh thì boss ở lại rất lâu.
  Khi hết giờ: 50% `leaveMap()` (chuyển sang hình dạng kế tiếp) / 50% `leaveMapNew()`
  (bỏ hẳn về nghỉ).
- **AI di chuyển riêng**: > 450 px → bay tới; 100-450 px → nhích lại; ≤ 100 px → đánh.
- **Rơi đồ (Bộ B)**:
  - **5%** đồ Thần Linh (`randDoTLBoss`)
  - **100% Vàng (190)** `nextInt(20000, 30000)`
  - **5%** một món trang bị thường (70% Áo/Quần/Giày, 30% Găng/Rada) với chỉ số shop
    ×`nextInt(100,115)%` **+ sao pha lê** (80% sao 1-3, 17% sao 4-5, 3% sao 6)
  - **10%** một trong `{15,16,17,18,19,20, 992}` — Ngọc Rồng 2→7 sao hoặc
    **Nhẫn thời không sai lệch (992)** — số lượng `nextInt(1,3)`
  - `+5 Point`, danh hiệu Trùm săn boss, `checkDoneTaskKillBoss`
  - Toàn bộ **rơi ra đất, khoá theo `plKill.id`** → người kết liễu hưởng.
- **Lời thoại**: "Ta là Sôn Gô Ku" / "Cơ thể này, sức mạnh này" / "Mau chấp nhận số phận đi
  lũ sâu bọ"; chuyển hình dạng "Biến hình! Super Saiyan Rose";
  thoại kết "Chúng ta sẽ gặp lại nhau sớm thôi".

---

#### 2.11 Cumber (-203999)

Chỉ số, cơ chế `injured`, AI và bảng rơi đồ **giống hệt Black Goku**. Khác biệt:

| Mục | Giá trị |
|---|---|
| Tên hình dạng | Cumber / Super Cumber |
| Tạo hình | 1254/1255/1256 → 1257/1255/1256 |
| Máu | 500.000.000 → 2.000.000.000 |
| Sát thương | 50.000 → 100.000 |
| % giảm sát thương | **30% → 25%** (`CUMBER_TG`) |
| Máu hiệu dụng | ≈ 714.285.714 → ≈ 5.333.333.333 (tổng ≈ 6,05 tỉ) |
| Map | **155 Hành tinh ngục tù** (chỉ 1 map) |
| Số bản | **1** |
| Nghỉ | 300 giây |
| Thoại chuyển hình dạng | "Biến hình! Super Saiyan SSJ" |
| Kỹ năng HD 0 | 1 Kamejoko 7 (100); 8 Tái tạo năng lượng 7 (300.000); 19 Khiên năng lượng 7 (300.000); 4 Galick 7 (100) |
| Kỹ năng HD 1 | 6 TDHS 7 (300.000); 8 Tái tạo 7 (300.000); 19 Khiên 7 (300.000); 1 Kamejoko 7 (100); 4 Galick 7 (100) |

Rơi đồ: 5% đồ Thần Linh, 100% Vàng 20k-30k, 5% trang bị + sao pha lê,
10% `{15..20, 992}` ×`nextInt(1,3)`, `+5 Point`.

---

#### 2.12 Cooler (-29)

| Hình dạng | Tên | Máu | Sát thương | % giảm (bảng) | Máu hiệu dụng | Tạo hình |
|---:|---|---:|---:|---:|---:|---|
| 0 | Cooler | 200.000.000 | 32.000 | **0%** (đã nối dây, cố ý tắt) | 200.000.000 | 317/318/319 |
| 1 | Cooler 2 | 500.000.000 | 50.000 | **0%** | 500.000.000 | 320/321/322 |

- **Giáp**: không có. **Né đòn**: `Util.isTrue(10, 1000)` = **1%** (có chat "Xí hụt").
- **Không có trần sát thương mỗi đòn.**
- Trong `BossDamageReduce`, `COOLER_TG = {0, 0}` — ghi chú trong code nói rõ đây là lựa chọn
  **cố ý**: 21c §5.2 xếp Cooler là "thưởng quá hậu so với độ khó" nhưng cách sửa được đề
  xuất là hạ phần thưởng chứ không tăng độ khó. Muốn bật thì đổi thành `{25, 35}`
  (máu hiệu dụng ≈ 1,05 tỉ).
- **Kỹ năng** (cả 2 hình dạng): 4 Chiêu đấm Galick lv1 (2.000 ms); 5 Chiêu Antomic lv1 (6.000 ms).
  → Cooler chỉ dùng kỹ năng cấp 1, sát thương thực tế đến từ `dame` gốc.
- **Map**: 110 Hang băng. 1 bản.
- **Thời gian**: nghỉ **1.800 giây (30 phút)**. Tự rời map sau **900.000 ms (15 phút)**,
  đồng hồ gia hạn khi khu còn người.
- **Rơi đồ (Bộ B, hào phóng nhất trong nhóm boss thế giới)**:
  - **5%** đồ Thần Linh
  - **100% Vàng (190)** `nextInt(20000, 30000)`
  - **5%** trang bị thường + sao pha lê
  - **80%** ngọc rồng `{15,16,17,18,19,20}` (Ngọc Rồng 2→7 sao) ×`nextInt(1,3)`
    — đây là tỉ lệ ngọc rồng cao nhất trong toàn bộ boss thế giới
  - `+5 Point`, danh hiệu Trùm săn boss, `checkDoneTaskKillBoss`
    (dòng `checkDoneTaskKillBoss` có ghi chú FIX: trước đây thiếu, boss chết mà nhiệm vụ
    không được tính)
- **Lời thoại**: "Ta sẽ cho chúng bây biết sức mạnh thực sự của dân tộc Frost Demons";
  giữa trận "Ta chính là Vũ Trụ Đệ Nhất Cao Thủ", "Ta đã giấu hết ngọc rồng rồi, các ngươi
  tìm vô ích hahaha"; chuyển hình dạng "Nãy giờ ta chưa thèm tung hết sức đâu / Biến hình,
  hây aaaa..."; kết "Mọi chuyện chưa kết thúc đâu".

---

#### 2.13 Baby (-925) — boss khó nhất nhóm thế giới

| Hình dạng | Máu | Sát thương | % giảm (bảng) | Chia sẵn trong `injured` | Tỉ lệ ST thực trừ | **Máu hiệu dụng** |
|---:|---:|---:|---:|---|---:|---:|
| 0 | 2.000.000.000 | 200.000 | 0% | `×0,7` rồi `/2` | 0,35 | **≈ 5.714.285.714** |
| 1 | 2.000.000.000 | 250.000 | 0% | `×0,7` rồi `/2` | 0,35 | **≈ 5.714.285.714** |
| 2 | 2.000.000.000 | **30.000** | 0% | `×0,7` rồi `/2` | 0,35 | **≈ 5.714.285.714** |

**Tổng máu hiệu dụng ≈ 17,14 tỉ** — cao nhất trong toàn bộ boss của nửa này.

- Ba hình dạng đều **chạm trần `int`** (2.147.483.647) ở mức 2 tỉ, không thể tăng máu thêm.
- `BABY_TG = {0, 0, 0}` — **cố ý để 0**. Ghi chú trong `BossDamageReduce` giải thích:
  Baby đã giảm sẵn ~65% qua `injured`, cộng thêm nữa là biến Baby thành không thể hạ.
  Muốn bật thì đổi thành `{25, 30, 35}`.
- **Giáp**: không có. **Né đòn**: 0%.
- **Khiên năng lượng**: nếu boss đang có khiên → `damage /= 4` (chứ không phải `= 1`
  như Black Goku), đòn > `hpMax` phá khiên.
- ⚠️ **Sát thương hình dạng 3 tụt xuống 30.000** (từ 250.000) — nhiều khả năng là lỗi gõ
  thiếu số 0 (đáng lẽ 300.000). Xem Ghi chú.
- **Kỹ năng** (cả 3 hình dạng gần như giống nhau — bộ kỹ năng đầy đủ nhất trong game):

| id | Tên | Cấp | Hồi (ms) |
|---:|---|---:|---:|
| 4 | Chiêu đấm Galick | 7 | 100 |
| 0 | Chiêu đấm Dragon | 7 | 100 |
| 2 | Chiêu đấm Demon | 7 | 100 |
| 3 | Chiêu Masenko | 7 | 100 |
| 5 | Chiêu Antomic | 7 | 100 |
| 1 | Chiêu Kamejoko | 7 | 100 |
| 17 | Liên hoàn | 7 | 10.000 |
| 24 | Super Kamejoko | 7 | 10.000 (HD 0) / 20.000 (HD 1, 2) |

- **Map**: **14 Làng Kakarot** (map khởi đầu của hành tinh Xayda). Chỉ 1 map.
- **Số bản cùng lúc**: **2**.
- **Xuất hiện**: `isNotifyDisabled = false` (**có loa**), `isZone01SpawnDisabled = false`
  → **Baby CÓ THỂ ra khu 0/1 của Làng Kakarot** — nơi nhân vật Xayda mới tạo đứng.
  Với 200.000 sát thương, đây là rủi ro cân bằng nghiêm trọng (xem Ghi chú).
- **Thời gian**: nghỉ **900 giây (15 phút)**. **Không có `autoLeaveMap`**
  → Baby ở lại map cho tới khi bị giết.
- **Rơi đồ (Bộ B + riêng)**:
  - **10%** đồ Thần Linh (`randDoTLBoss`) — cao gấp đôi Black Goku/Cumber/Cooler
  - **1%** một trong 3 cải trang: **1785 Cải trang baby**, **1786 Cải trang vegeta baby**,
    **1788 Cải trang baby khỉ** — kèm bộ chỉ số rất mạnh:
    option 50 (+30-40%), 77 (+30-40%), 103 (+30-40%), 94 (+10-20%), 5 (+10-20%),
    204 (+10-20%), 30 (khoá), 93 (+2-5)
  - **100% Vàng (190)** `nextInt(20000, 30000)`
  - **5%** trang bị thường + sao pha lê
  - **10%** `{15,16,17,18,19,20, 992}` ×`nextInt(1,3)`
  - `plKill.bossBabyDefeatParticipationCount++` (đếm số lần hạ Baby của người chơi)
  - `checkDoneTaskKillBoss` (có ghi chú FIX: trước đây thiếu)
  - **Không** cộng Point sự kiện (khác với hầu hết boss khác)
- **Lời thoại**: kịch bản Baby ký sinh Vegeta ("Ta sẽ kí sinh vào người của vegeta để đạt
  được dạng hoàn hảo!"), thoại chuyển hình dạng "Đến lúc rồi!", thoại chết cuối
  "Oái.. không... Cơ thể hoàn hảo của ta!!".

---

#### 2.14 Nhóm Bojack — Bojack, Bujin, Kogu, Zangya, Bido, Siêu Bojack

Map chung cho cả nhóm: **97 Thành phố phía bắc, 98 Ngọn núi phía bắc,
99 Thung lũng phía bắc, 100 Thị trấn Ginder, 105 Cánh đồng tuyết, 106 Rừng tuyết,
107 Núi tuyết, 108 Dòng sông băng, 109 Rừng băng**.

> **Đã sửa trong code**: `mapJoin` có ghi chú FIX bỏ các map đầu game
> (3, 4, 5, 6, 27, 28, 29, 30) vì nhóm Bojack dame 170.000-300.000 giết người chơi mới 1 đòn.

`loadBoss` tạo **1 bản Bojack (-320)** và **1 bản Siêu Bojack (-321)** riêng biệt.
Bujin/Kogu/Zangya/Bido sinh theo Bojack (`APPEAR_WITH_ANOTHER`).

| Tên | id | Máu | Sát thương | Tạo hình | Kỹ năng (id-tên-cấp-hồi ms) |
|---|---:|---:|---:|---|---|
| Bujin | -316 | 20.000.000 | 170.000 | 341/342/343 | 2 Chiêu đấm Demon 7 (1.000); 3 Masenko 7 (1.000) |
| Kogu | -317 | 40.000.000 | 180.000 | 329/330/331 | 8 Tái tạo năng lượng 7 (100.000); 0 Dragon 7 (1.000); 23 Trói 4 (50.000); 5 Antomic 4 (1.000) |
| Zangya | -318 | 60.000.000 | 207.200 | 332/333/334 | 8 Tái tạo 7 (100.000); 4 Galick 7 (1.000); 23 Trói 5 (50.000); 5 Antomic 3 (3.000) |
| Bido | -319 | 80.000.000 | 250.200 | 335/336/337 | 8 Tái tạo 7 (100.000); 0 Dragon 7 (1.000); 1 Kamejoko 4 (10.000) |
| Bojack (HD 0) | -320 | 100.000.000 | 300.000 | 323/324/325 | 8 Tái tạo 7 (100.000); 23 Trói 7 (120.000); 3 Masenko 7 (1.000); 4 Galick 7 (1.000) |
| Bojack (HD 1 "Siêu Bojack") | -320 | 150.000.000 | 300.000 | 326/327/328 | 8 Tái tạo 7 (100.000); 22 Thôi miên 7 (100.000); 19 Khiên năng lượng 7 (100.000); 4 Galick 7 (1.000) |
| Siêu Bojack (boss riêng) | -321 | 500.000.000 | 300.000 | 326/327/328 | 8 Tái tạo 7 (100.000); 23 Trói 3 (60.000); 1 Kamejoko 7 (1.000); 4 Galick 7 (1.000) |

- **Giáp**: không có. **% giảm sát thương**: 0%. Không có trần sát thương.
  Tất cả dùng `Boss.injured` mặc định → **máu hiệu dụng = máu danh nghĩa**.
- **Cơ chế đội hình**: Bojack `doneChatS()` → `AFK` (đứng im) khi `currentLevel == 0`.
  `BUJIN.doneChatE()` kiểm tra Kogu/Zangya/Bido còn sống không; hết mới trả Bojack về `ACTIVE`.
  → **Phải dọn sạch 4 đàn em trước khi Bojack đánh**.
- **Thời gian**: Bojack nghỉ **900 giây (15 phút)**; hình dạng 2 của Bojack là
  `ANOTHER_LEVEL` + nghỉ 1.800 giây. Siêu Bojack (-321) nghỉ **1.800 giây (30 phút)**.
  Tất cả tự rời sau 15 phút không người trong khu.
- **Xuất hiện**: `isZone01SpawnDisabled = true` (khu ≥ 2), có loa toàn server.
- **Rơi đồ — giống kiểu tiểu đội Namek, rất nhiều Ngọc**:
  - **Ngọc (item 77)**: 4 vòng lặp, mỗi ItemMap `Util.nextInt(5, 20)` viên
    → kỳ vọng khoảng **50-80 Ngọc mỗi con** (nhiều hơn hẳn tiểu đội Namek).
  - **1 Cải trang riêng (100%)** kèm chỉ số shop:
    Bujin → **423**; Kogu → **424**; Zangya → **425**; Bido → **426**;
    Bojack → **427**; Siêu Bojack → **428**.
  - **1 Ngọc Rồng 6 sao (19)** + **1 Ngọc Rồng 7 sao (20)** — 100%.
  - `+5 Point`.
  - **Không rơi Vàng, không rơi đồ Thần Linh.**
- **Lời thoại**: ngắn gọn — Bojack chỉ "Hahaha"; Kogu/Zangya chat "Trói" khi dùng kỹ năng
  và "Cứu" khi chết; Bujin/Bido dùng bộ thoại khiêu khích chung.

---

#### 2.15 Fide Vàng (-502) + Death Beam 1-5 — boss 21h

##### Fide Vàng (-502)

| Mục | Giá trị |
|---|---|
| Tên hiển thị | "Fide Vàng <1-100>" |
| Hành tinh | Xayda |
| Hình dạng | 1 |
| Máu | **1.000.000.000** |
| Sát thương | 100.000 |
| % giảm sát thương (bảng) | 0% |
| **Trần sát thương mỗi đòn** | **50.000.000** |
| Số đòn tối thiểu để hạ | **20 đòn** |
| Máu hiệu dụng | 1.000.000.000 (nhưng bị chặn bởi trần 50tr/đòn) |
| Giáp | không có |
| Né đòn | 0% |
| Tạo hình | 502/503/504 |

Kỹ năng — bộ đầy đủ 7 cấp của 3 chưởng:

| id | Tên | Cấp | Hồi (ms) |
|---:|---|---|---:|
| 8 | Tái tạo năng lượng | 1 | 120.000 |
| 4 | Chiêu đấm Galick | 7 | 1.000 |
| 1 | Chiêu Kamejoko | 1→7 (7 mục) | 1.000 mỗi cấp |
| 3 | Chiêu Masenko | 1→7 (7 mục) | 1.000 mỗi cấp |
| 5 | Chiêu Antomic | 1→7 (7 mục) | 1.000 mỗi cấp |

- **Map**: **6 Đông Karin**. 1 bản.
- **Lịch cố định**: `joinMap()` chỉ cho vào map khi `TimeUtil.is21H()` —
  tức **21:00-21:59 giờ Việt Nam**; ngoài khung đó boss quay về `REST`.
  `autoLeaveMap()` gọi `leaveMap()` ngay khi hết 21h.
  `secondsRest` = 300 giây.
- **Khi vào map**: **giết toàn bộ quái trong khu** (`mob.injured(this, 99999999, true)`)
  và đặt cờ `zone.isGoldenFriezaAlive = true`.
- **Cơ chế đặc biệt — 3 trạng thái luân phiên** (đổi mỗi `nextInt(5000, 10000)` ms):
  - **Trạng thái 0 — TỰ NỔ (`setBom`)**: hiện hiệu ứng gồng, sau **2.500 ms**
    gây **2.100.000.000 sát thương xuyên** cho **mọi người chơi trong khu**
    → giết sạch khu. Không tự sát (boss không mất máu).
  - **Trạng thái 1 — GỌI DEATH BEAM**: đánh thức cả 5 Death Beam; chờ tới khi cả 5 về `REST`
    rồi chuyển sang trạng thái 2.
  - **Trạng thái 2 (mặc định) — đánh thường** trong 30 giây.
- **Rơi đồ**: **chỉ 1 món** — **Cải trang Fide vàng (id 629)** ×1, 100%, kèm chỉ số
  option 30 (khoá), 50 (+20%), 77 (+20%), 103 (+20%), 93 (+20).
  Rơi ra đất khoá theo người kết liễu. `+5 Point`.
  **Không rơi Vàng, không ngọc rồng, không đồ Thần Linh.**
- **Lời thoại**: "He he he" / "Ta sẽ xé xác ngươi ra thành trăm mảnh" /
  "Xem các ngươi mạnh đến đâu".

##### Death Beam 1-5 (-609 … -613)

| Mục | Giá trị |
|---|---|
| Tên hiển thị | `"$"` (ký tự đơn — hiển thị như một tia sáng, không phải tên boss) |
| Máu | 500 |
| Sát thương | 1.000 |
| Kỹ năng | **không có** (mảng rỗng) |
| Tạo hình | 609/610/611 |
| Map | 6 Đông Karin (theo boss cha) |
| Số bản | 5, `CALL_BY_ANOTHER` |
| Loại manager | `BossType.SKILLSUMMONED` (`SkillSummonedManager`, quét 150 ms) |

- **Cơ chế đặc biệt — tia truy đuổi bất tử**:
  - `injured()` **luôn trả 0** → **không thể đánh chết**.
  - Vào map ở `parentBoss.location.x ± 100`, **y = 300** (trên cao).
  - Bám theo một người chơi ngẫu nhiên; khi khoảng cách ngang < 5 px thì
    `setDie()` → gây **2.100.000.000 sát thương xuyên** cho người đó (gần như chắc chắn chết).
  - Sau **14.600 ms** thì ngừng truy đuổi, bay lên trên (y -= 30 mỗi 500 ms) và biến mất.
  - Nếu mục tiêu chết/rời khu → chuyển `AFK`, mỗi 3 giây có 50% chọn mục tiêu mới.
- **Rơi đồ**: `reward()` có code rơi **Cải trang Fide vàng (629)** giống boss cha,
  nhưng vì `injured` luôn trả 0 nên **không ai giết được Death Beam** →
  **nhánh reward này không bao giờ chạy**.
- ⚠️ `BossID.DEATH_BEAM_5 = -613` **trùng với** `BossID.HAKAI = -613`.

---

### 3. BOSS THEO KHUNG GIỜ CỐ ĐỊNH

Nhóm này được tạo bởi `Map.initBoss()` — **mỗi khu (zone) của map tương ứng được tạo
một boss riêng**, gán `zoneFinal` cố định. Với `zones = 10` trong `map_template`,
mỗi loại có **10 bản**, mỗi khu 1 con. Chúng đăng ký vào `FinalBossManager`.

> Chuỗi Ma Bư 12h/14h vừa là boss theo giờ vừa nằm trong map dạng phó bản
> (`MapService.isMapMaBu`), nên có thể trùng một phần với tài liệu nửa 2.
> Ở đây ghi đủ chỉ số và cơ chế cốt lõi.

#### 3.1 Tàu Pảy Pảy — Đông Nam Karin

| Mục | Giá trị |
|---|---|
| id | **ngẫu nhiên** trong khoảng -1.000.000 … -100.000 (`Util.randomBossId()`) |
| BossData | `BossesData.TAU_PAY_PAY_DONG_NAM_KARIN` (hằng số id -308 không được dùng làm id thật) |
| Hành tinh | Trái Đất |
| Máu | 10.000 |
| Sát thương | 100 |
| **Trần sát thương mỗi đòn** | **100** → luôn cần đúng **100 đòn** |
| Giáp | không có |
| Né đòn | `Util.isTrue(tlNeDon, 1)` → 0% |
| Tạo hình | 338/339/340 |
| Kỹ năng | 1 Chiêu Kamejoko lv7 (5.000 ms); 4 Chiêu đấm Galick lv7 (1.000 ms) |
| Map | **111 Đông Nam Karin** |
| Số bản | 1 cho mỗi khu (10 khu) |
| Nghỉ | 60 giây |
| Loa | không (map 111 nằm trong danh sách chặn loa) |

- **Cơ chế đặc biệt — bao cát luyện tập**:
  - Mỗi đòn trúng, boss **tự hạ sát thương của mình** xuống `damage / nextInt(500, 1000)`.
  - Người đánh nhận **sức mạnh + tiềm năng** = `damage × nextInt(20, 50)`,
    trần **10.000.000 − nextInt(1.000.000)** mỗi đòn.
  - Nếu người chơi có `power >= 1.500.000` thì thưởng tụt xuống `nextInt(1)` = 0
    → **chỉ hữu ích cho nhân vật dưới 1,5 triệu sức mạnh**.
- **Rơi đồ**: **không có** (`reward()` mặc định của `Boss` chỉ gọi `checkDoneTaskKillBoss`).

#### 3.2 Chuỗi Ma Bư 12h (12:00-12:59)

Bảy map nối tiếp: 114 Cổng phi thuyền → 115 Phòng chờ → 117 Cửa Ải 1 → 118 Cửa Ải 2
→ 119 Cửa Ải 3 → 120 Phòng chỉ huy.

| Tên | id | Máu | Sát thương | Map | Trần ST/đòn | Né đòn | Kỹ năng | Điểm Mabư |
|---|---:|---:|---:|---:|---:|---:|---|---:|
| Drabura | -233 | 20.000.000 | 10.000 | 114 | 20.000.000 | 0% | 4 Galick 7 (1.000) | +10 |
| Bui Bui | -234 | 40.000.000 | 200.000 | 115 | — | 0% | 4 Galick 7 (10.000) | +10 |
| Bui Bui (ải 1) | -238 | 40.000.000 | 200.000 | 117 | — | 0% | 4 Galick 7 (10.000) | +10 |
| Ya côn | -235 | 50.000.000 | 200.000 | 118 | — | 0% | 4 Galick 7 (100) | +10 |
| Drabura 2 | -237 | 20.000.000 | 200.000 | 119 | 20.000.000 | 0% | 4 Galick 7 (1.000) | +10 |
| Gôku | -341 | 60.000.000 | 1.000 | 119 | 20.000.000 | 0% | 4 Galick 7; 1 Kamejoko 7; 8 Tái tạo 7 (1.000.000); 6 TDHS 1 (60.000) | +10 |
| Ca Đít | -342 | 60.000.000 | 1.000 | 119 | 10.000.000 | 0% | 4 Galick 7; 5 Antomic 7; 8 Tái tạo 7 (1.000.000) | +10 |
| Mabư | -236 | 100.000.000 | 10.000 | 120 | 50.000.000 | **20%** | 8 Tái tạo 3 (1.200.000); 4 Galick 7 (1.000) | **+25** |
| Drabura 3 | -343 | 20.000.000 | 100.000 | 114 | 20.000.000 | 0% | 4 Galick 7 (1.000); 8 Tái tạo 7 (10.000.000) | +20 |

- **Giáp**: không có ở tất cả. **% giảm sát thương từ `BossDamageReduce`**: 0% ở tất cả.
- **Nghỉ**: Drabura/Bui Bui/Bui Bui 2/Ya côn nghỉ 60 giây; Drabura 2 nghỉ 300 giây;
  Mabư (-236) nghỉ 60 giây. Gôku, Ca Đít, Drabura 3 là `CALL_BY_ANOTHER`.
- **Cơ chế đặc biệt của Mabư (-236)**:
  - Mỗi 30 giây `petrifyPlayersInTheMap()`: mỗi người trong khu có **10%** bị **hoá đá**
    22 giây, hoặc **20%** bị **biến sôcôla** 30 giây (chat "Úm ba la xì bùa").
  - `rest()` được ghi đè: phát thanh tiến độ hồi sinh (`Service.SendMabu(zoneFinal, percent)`)
    cho cả khu.
  - `leaveMap()` đánh thức Drabura 3 (`bossesAppearTogether`).
  - Mỗi đòn đánh trúng có **20%** cộng 1 điểm phần trăm vào `fightMabu` của người chơi.
- **Cơ chế đặc biệt của Drabura 2 (-237)**: gọi kèm **Gôku** và **Ca Đít** —
  hai NPC bị điều khiển đánh nhau ("Mà ta sẽ để cho các ngươi tự thanh toán lẫn nhau, xin chào").
- **Rơi đồ**: tất cả dùng **Bộ B** với tỉ lệ thấp —
  **1%** đồ Thần Linh, **100% Vàng** 20.000-30.000, **1%** trang bị + sao pha lê,
  **10%** ngọc rồng `{15..20}` ×`nextInt(1,3)`, `+5 Point`, cộng điểm Mabư như bảng trên.

#### 3.3 Ma Bư 14h (14:00-14:59)

##### Mabư mập (-214) — 5 hình dạng

| Hình dạng | Tên | Máu | Sát thương | Tạo hình | Map |
|---:|---|---:|---:|---|---:|
| 0 | Mabư mập | 50.000.000 | 500.000 | 297/298/299 | 127 |
| 1 | Super Bư | 60.000.000 | 500.000 | 421/422/423 | 127, 128 |
| 2 | Bư Tênk | 80.000.000 | 500.000 | 424/425/426 | 127 |
| 3 | Bư Han | 100.000.000 | 500.000 | 427/428/429 | 127 |
| 4 | Kid Bư | 150.000.000 | 500.000 | 439/440/441 | 127 |

- **Kỹ năng** (mọi hình dạng): 1 Chiêu Kamejoko lv3 (5.000 ms); 0 Chiêu đấm Dragon lv7 (1.000 ms).
- **Giáp**: không có. **Né đòn**: `Util.isTrue(10, 100)` = **10%**.
- **Trần sát thương mỗi đòn**: **30.000.000 ± 10.000**.
- **% giảm sát thương (bảng)**: 0%.
- **Cơ chế đặc biệt**:
  - **Ăn người chơi**: mỗi 10 giây, mỗi người trong khu có **20%** bị Mabư nuốt
    (`isMabuHold = true`), boss chat "Măm măm".
  - **Biến sôcôla** (chỉ hình dạng 0): mỗi 10 giây, mỗi người có **20%** bị biến sôcôla 30 giây.
  - Từ hình dạng 1 trở đi: thi thoảng dùng `sendMabuAttackSkill` (chiêu riêng), cách nhau
    `nextInt(5000, 10000)` ms.
  - **Kid Bư (hình dạng cuối) CHỈ CHẾT BẰNG "Quả cầu kênh khi" (skill id 10)**:
    `if (currentLevel == data.length - 1 && skill != QUA_CAU_KENH_KHI) damage = damage >= hp ? 0 : damage`
    → mọi chiêu khác không thể hạ đòn kết liễu.
  - Mỗi đòn có **20%** cộng điểm phần trăm `fightMabu`.
- **Nghỉ**: 600 giây mỗi hình dạng (nhưng bị khoá theo khung giờ 14h).
- **Rơi đồ**: **không rơi vật phẩm** — `reward()` chỉ `+5 Point` và `checkDoneTaskKillBoss`.

##### Super Bư (bụng) (-348)

| Mục | Giá trị |
|---|---|
| Máu | 50.000.000 |
| Sát thương | 500.000 |
| Map | 127 Cổng phi thuyền, **128 Bụng Mabư** |
| Trần sát thương/đòn | 30.000.000 ± 10.000 |
| Nghỉ | 10 giây (`REST_10_S`) |
| Kỹ năng | 1 Kamejoko lv3 (5.000); 0 Dragon lv7 (1.000) |
| Liên kết | `SuperBu` tra `FinalBossManager.getBossById(MABU, 127, zoneId)` để đồng bộ với Mabư ngoài |

---

### 4. BOSS MINI / LANG THANG

#### 4.1 Sói hẹc quyn (-77) — bất tử, chỉ "chết" bằng Cục xương

| Mục | Giá trị |
|---|---|
| Nhóm | Boss mini |
| Hành tinh | Trái Đất |
| Máu danh nghĩa | 10.000 (**không có ý nghĩa**) |
| Sát thương | 1.000 |
| Giáp | không có |
| % giảm sát thương | — |
| Tạo hình | 394/395/396 |
| Kỹ năng | 1 Chiêu Kamejoko lv7 (5.000 ms); 4 Chiêu đấm Galick lv7 (1.000 ms) |
| Số bản | **2** |
| Nghỉ | 600 giây (10 phút) |

- **Map**: 73 map thường — `0-20, 24-37, 63-77, 79-84, 92-94, 96-100, 102-110`.
  `joinMap()` được ghi đè: lọc các khu **≤ 10 người và chưa có Sói** rồi bốc ngẫu nhiên;
  nếu không còn khu hợp lệ thì bỏ lượt (`leaveMapNew()`).
- **Tự rời map**: sau `Util.nextInt(100.000, 300.000)` ms (**1,7-5 phút**), **không gia hạn**.
- **Cơ chế đặc biệt — BẤT TỬ**:
  - `injured()` **luôn trả về 0** → **không thể đánh chết bằng bất kỳ chiêu nào**.
  - `attack()` cũng **không gọi `useSkill`** → Sói **không thật sự gây sát thương**,
    chỉ chạy quanh.
  - Cách "hạ" duy nhất: dùng vật phẩm **Cục xương (id 460)** trong hành trang
    (`UseItem.java:2275-2360`):
    1. Nếu Sói đã ăn xương trong 5 giây gần nhất → "Sói đã no rồi".
    2. Nếu chưa → Sói chat "Ê, Cục xương ngon quá", một Cục xương rơi xuống đất
       (đã đánh dấu `isPickedUp`), trừ 1 Cục xương trong hành trang.
    3. Người dùng nhận ngay **1 trong 2 phần thưởng**:
       - **75%**: một **Sao pha lê** ngẫu nhiên id `441 + rand` với `rand ∈ [0,6]` —
         441 Sao pha lê đỏ, 442 lam, 443 hồng, 444 tím, 445 cam, 446 vàng, 447 lục;
         kèm option `95 + rand` giá trị **5** (hoặc **3** nếu rand = 3 hoặc 4).
       - **25%**: **Phiếu giảm giá (id 459)** với option 112 = 80, 93 = 90,
         20 = `nextInt(10000)`.
    4. Sau 5 giây, Cục xương trên đất biến mất và Sói **rời map** (`leaveMapNew()`).
  - Cập nhật danh hiệu **Kẻ thao túng sói**.
- **`reward()` có code** (rơi 2 **Hộp quà Goku Day** id 1591 và 1594, `+5 Point`)
  nhưng **không bao giờ chạy** vì Sói không bao giờ `die()`.
- **Lời thoại**: không có (mảng thoại rỗng).

#### 4.2 Ở dơ (-78)

⚠️ Lớp `Boss_mini/Odo.java` **không dùng** `BossesData.O_DO` mà khai báo `BossData` riêng
ngay trong hàm dựng. (`BossesData.O_DO` với map 168 chỉ được dùng bởi
`boss/dai_hoi_vo_thuat/ODo.java` — thuộc tài liệu nửa 2; **map 168 không tồn tại trong
`map_template`**, xem Ghi chú.)

| Mục | Giá trị (bản mini đang chạy) |
|---|---|
| Tên hiển thị | "Ở Dơ <1-49>" (số cố định khi khởi tạo lớp) |
| Hành tinh | Trái Đất |
| Máu | **500.000** |
| Sát thương | 1.000 |
| Giáp | không có |
| Tạo hình | 400/401/402 |
| Kỹ năng | 8 Tái tạo năng lượng lv1 (không đặt cool down riêng) |
| Map | 73 map thường (danh sách như 4.1) |
| Số bản | **5** |
| `secondsRest` | **600.000 giây ≈ 6,9 ngày** ⚠️ |

- **Cơ chế nhận sát thương — CỐ ĐỊNH 50.000/đòn**:
  `injured()` bỏ qua hoàn toàn sát thương thật, luôn đặt `damage = 50000`.
  → Máu 500.000 ÷ 50.000 = **đúng 10 đòn**, bất kể người chơi mạnh yếu ra sao.
- **Cơ chế đặc biệt**:
  - **"Bùm Bùm"**: mỗi `nextInt(3000, 5000)` ms, mọi người trong bán kính 200 px
    bị trừ **10% máu tối đa** (không thể chết vì bị chặn ở `hp - 1`),
    boss chat "Bùm Bùm" và người chơi chat một câu trong `textOdo`.
  - **Tự hồi máu**: mỗi 30 giây hồi `nextInt(10, 20)%` máu tối đa,
    chat "Mùi Của Các Ngươi Thơm Quá!! HAHA".
    → Nếu người chơi đánh chậm hơn ~1 đòn/30 giây thì không bao giờ hạ được.
- **Vào map**: luôn vào **khu 0** (`joinMap2()` ép `zoneid = 0`), vị trí ngẫu nhiên.
- **Tự rời map**: 900.000 ms (15 phút) kể từ lúc vào, **không gia hạn**.
- **Rơi đồ**: **1× Hộp quà Goku Day (1591)** + **1× Hộp quà Goku Day (1594)**, 100%,
  khoá cho người kết liễu. `+5 Point`. Cập nhật danh hiệu **Ở dơ**.

#### 4.3 Ăn trộm (-365)

| Mục | Giá trị |
|---|---|
| Tên hiển thị | "Ăn Trộm <1-49>" (đổi mỗi lần vào map) |
| Hành tinh | Trái Đất |
| Máu | **`Util.nextInt(100)` = 0-99**, đặt lại mỗi lần vào map |
| Sát thương | `máu / 10` (0-9) |
| Giáp | không có |
| Tạo hình | 201/202/203 |
| Kỹ năng | 6 Thái Dương Hạ San lv3, hồi 50.000 ms (bản trong lớp) |
| Map | 73 map thường; luôn vào **khu 0** |
| Số bản | **5** |
| Nghỉ | 600 giây (10 phút) |
| Tự rời map | 900.000 ms (15 phút) kể từ khi vào |

- **Cơ chế nhận sát thương — CỐ ĐỊNH 1/đòn**: `injured()` đặt `damage = 1`.
  → cần **đúng `hpMax` đòn** (0-99 đòn). Sau mỗi đòn, boss **phản đòn bằng
  Thái Dương Hạ San** vào người vừa đánh.
- **Cơ chế đặc biệt — TRỘM VÀNG**:
  - Khi ở trong 40 px của một người chơi, mỗi 500 ms boss trộm vàng:
    - túi ≥ 2.000.000 → trộm `nextInt(200.000, 1.000.000)`
    - túi ≥ 1.000.000.000 → trộm `nextInt(4.000, 5.000)`
    - túi ≥ 1.000.000 → trộm `nextInt(1.000, 2.000)`
    - ⚠️ Thứ tự `if` khiến nhánh ≥ 1 tỉ **không bao giờ chạy** (đã bị nhánh ≥ 2 triệu bắt trước).
  - Dừng khi đã trộm quá **10.000.000.000 Vàng**.
  - Boss chat "Haha đã trộm được <số> Vàng".
- **Rơi đồ khi chết**: chỉ rơi **nếu đã trộm được ít nhất 1 Vàng**:
  - **80% số vàng đã trộm**, chia thành **5 đống Vàng (190)** bằng nhau
  - **1× Hộp quà Goku Day (1591)** + **1× Hộp quà Goku Day (1594)**
  - `+5 Point`, danh hiệu **Bị móc sạch túi**
  - → Nếu chưa trộm được đồng nào thì **không rơi gì cả**.

#### 4.4 Mặt Trời (-79)

⚠️ Lớp `Boss_mini/MatTroi.java` được `BossManager.createBoss(BossID.MAT_TROI = -799)` tạo ra,
nhưng **hàm dựng lại truyền `BossID.Virut = -79`** làm id thật của boss.
→ Trong game boss này mang **id -79**, không phải -799.

| Mục | Giá trị |
|---|---|
| Tên hiển thị | "Mặt Trời <1-49>" (đổi mỗi lần vào map) |
| Hành tinh | Trái Đất |
| Máu | **100** (ép lại mỗi lần vào map) |
| Sát thương | **1** (`dameg` bị ép về 1 trong `joinMap`) |
| Giáp | không có |
| Tạo hình | 1501/1502/1503 |
| Kỹ năng | 0 Chiêu đấm Dragon lv7 (1.000 ms) |
| Map | **5 Đảo Kamê, 7 Làng Mori, 0 Làng Aru, 14 Làng Kakarot**; luôn vào **khu 0** |
| Số bản | **20** (nhiều nhất trong game) |
| Nghỉ | 600 giây (10 phút) |
| Tự rời map | 900.000 ms (15 phút) |

- **Cơ chế nhận sát thương — CỐ ĐỊNH 1/đòn** → luôn cần **đúng 100 đòn**.
- **Cơ chế đặc biệt — BỎNG NHIỆT**:
  - Khi đánh trúng ai đó trong 40 px, với xác suất 30% sẽ áp hiệu ứng cho **mọi người
    trong bán kính 200 px**: `ItemTimeService.sendItemTime(player, 12953, 60)` và chat
    `"Nóng vãi lồn, <tên> Đã bị bỏng nhiệt"` (⚠️ câu chat này thô tục, nên sửa).
  - Sau **30 giây**, người bị dính có **80% bị giết ngay** (`injured(null, hp, true, false)`).
  - ⚠️ `checkGlobalEffects()` chỉ được gọi **bên trong nhánh `active()` khi đã quá 15 phút**,
    tức thực tế hiệu ứng chỉ được kiểm tra một lần lúc boss sắp rời map.
- **Rơi đồ**: **50%** rơi **Mặt trời tí hon (id 1562)** ×1, kèm chỉ số
  option 50 (+7-10), 77 (+7-10), 103 (+7-10), 30 (khoá), 93 (+2-5).
  Cập nhật danh hiệu **KOL**. **Không** cộng Point.
- **Lời thoại**: không có.

#### 4.5 Rồng Nhí (-386998)

| Mục | Giá trị |
|---|---|
| Hành tinh | Trái Đất |
| Máu | 50.000.000 |
| Sát thương | 1 |
| Giáp | không có |
| Tạo hình | 1662/1663/1664 |
| Kỹ năng | 4 Chiêu đấm Galick lv5, hồi **1.000.000.000 ms** (≈ 11,6 ngày → thực chất chỉ dùng 1 lần) |
| Map | 0-20, 24-37 |
| Nghỉ | 60 giây |
| Loa | không (`isNotifyDisabled = true`), chỉ khu ≥ 2 |
| Tự rời map | 900.000 ms (15 phút), gia hạn khi khu còn người |

- **Số bản cùng lúc: 0** — `BossManager.createBoss` **có** nhánh `case BossID.RONG_NHI`
  nhưng **`loadBoss()` không gọi nó**, và không có sự kiện nào gọi.
  → **Rồng Nhí hiện không bao giờ xuất hiện trong game.**
- **Rơi đồ (nếu được bật)**: **20%** mới rơi, và trong đó **20%** là
  **Trứng vàng rồng nhí (id 1821)** ×1, **80%** là một trong Ngọc Rồng 5/6/7 sao ×1.
  `+5 Point` (nằm trong nhánh 20%).
- **Lời thoại**: "Tới giờ làm việc" (mở), "Ái chà chà" (giữa trận và khi chết).

#### 4.6 Virut (-79) — code chết

| Mục | Giá trị |
|---|---|
| Tên hiển thị | "Virut <1-49>" |
| Máu | 100 |
| Sát thương | 10 |
| Tạo hình | 651/778/779 |
| Kỹ năng | 0 Chiêu đấm Dragon lv7 (1.000 ms) |
| Map | 5, 7, 0, 14 |
| Nghỉ | 600 giây |
| Số bản | **0** |

- `BossManager.createBoss` có `case BossID.Virut -> new Virut()`, nhưng `BossID.Virut = -79`
  **trùng** với `BossID.XINBATO = -79` và với id mà lớp `MatTroi` dùng.
  `loadBoss()` **không tạo Virut**. → **Boss này không bao giờ được tạo.**
- **Cơ chế (nếu bật)**: giống Mặt Trời nhưng hiệu ứng là **nhiễm bệnh**
  (`sendItemTime(player, 7143, 10)`, chat "Khè Khè, <tên> Đã bị nhiễm"), kéo dài **300 giây**,
  hết giờ thì **80% chết**.
- **Rơi đồ**: 2× **Hộp quà Goku Day** (1591 và 1594), 100%. `+5 Point`.

---

### 5. BOSS SỰ KIỆN

#### 5.1 Trạng thái bật/tắt sự kiện (EventManager.java)

| Sự kiện | Cờ | Lệnh `init()` | Thực tế |
|---|---|---|---|
| Default (Broly) | — | `new Default().init()` | **ĐANG CHẠY** |
| Hùng Vương | `HUNG_VUONG = true` | `new HungVuong().init()` | **ĐANG CHẠY** |
| Nạp thẻ | `TOP_UP = true` | `new TopUp().init()` | ĐANG CHẠY (không có boss) |
| Tết | `LUNNAR_NEW_YEAR = true` | **bị comment** | KHÔNG CHẠY |
| Quốc tế Phụ nữ | `INTERNATIONAL_WOMANS_DAY = true` | **bị comment** | KHÔNG CHẠY |
| Halloween | `HALLOWEEN = true` | **bị comment** | KHÔNG CHẠY |
| Noel | `CHRISTMAS = true` | **bị comment** | KHÔNG CHẠY |
| Trung Thu | `TRUNG_THU = true` | **bị comment** | KHÔNG CHẠY |

⚠️ Ngoài ra, `ServerManager.run()` **chỉ khởi động thread cho**
`BossManager, YardartManager, FinalBossManager, SkillSummonedManager, BrolyManager,
OtherBossManager, RedRibbonHQManager, TreasureUnderSeaManager, SnakeWayManager,
GasDestroyManager`. **Không có thread nào cho `TrungThuEventManager`,
`HalloweenEventManager`, `ChristmasEventManager`, `HungVuongEventManager`,
`LunarNewYearEventManager`** → boss đăng ký vào các manager đó **không bao giờ được
`update()`**, kể cả Thủy Tinh/Sơn Tinh của sự kiện Hùng Vương đang bật. Xem Ghi chú.

#### 5.2 Broly (-1822)

| Mục | Giá trị |
|---|---|
| Tên hiển thị | "Broly <10-100>" |
| Hành tinh | Xayda |
| Máu | **`Util.nextInt(500, 100000)`** — bốc lại mỗi lần vào map |
| Sát thương | `hpMax / 100` (5-1.000) |
| Giáp | không có |
| Tạo hình | 291/292/293 |
| Map | 5 Đảo Kamê, 13 Đảo Guru, 20 Vách núi đen, 27-38 |
| **Số bản** | **30** (`new Default().boss()` → `createBoss(BROLY, 30)`) |
| Manager | `BrolyManager` (thread **có** chạy) |
| Nghỉ | 600 giây |
| Khu xuất hiện | luôn khu ≥ 2 (nếu không còn khu trống thì chuyển thẳng sang `DIE`) |

Kỹ năng: **49 mục** — toàn bộ 7 cấp của Tái tạo năng lượng (8), Dragon (0), Demon (2),
Galick (4), Kamejoko (1), Masenko (3), Antomic (5), hồi 1.000 ms mỗi cấp
(riêng Tái tạo lv7 = 100 ms).

- **Cơ chế nhận sát thương**:
  - **Trần `hpMax / 100` mỗi đòn** (trừ khi đánh bằng **Tự phát nổ (skill 14)**)
    → luôn cần tối thiểu **100 đòn**.
  - Mỗi đòn có **10%** khiến Broly **tăng chỉ số** (`tangChiSo`):
    `hpMax += hpMax / nextInt(10,100)`, trần **16.070.777**; `dame = hpMax / 10`.
    → Broly càng bị đánh càng phình to, sát thương nhảy lên `hpMax/10`.
  - Khi `hpMax` chạm đúng **1.500.000**, `active()` gọi `leaveMap()` ngay.
- **Cơ chế đặc biệt — chết thì biến thành Super Broly**:
  `leaveMap()` tạo một **Super Broly** mới ngay tại vị trí và khu vừa rồi.
  `die()` có ghi chú FIX: trước đây không gọi `reward()` nên người giết không được tính nhiệm vụ.
- **Rơi đồ**: **không rơi vật phẩm**; `reward()` là hàm mặc định của `Boss`
  (chỉ `checkDoneTaskKillBoss`).
- **Lời thoại**: "Haha! ta sẽ giết hết các ngươi" / "Sức mạnh của ta là tuyệt đối" /
  "Vào hết đây!!!"; kết "Các ngươi giỏi lắm. Ta sẽ quay lại."

#### 5.3 Super Broly (-82282)

| Mục | Giá trị |
|---|---|
| Tên hiển thị | "Super Broly <10-100>" |
| Máu | **`Util.nextInt(1.500.000, 16.070.777)`** |
| Sát thương | `hpMax / 100` (15.000-160.707) |
| Tạo hình | 294/295/296 |
| Kỹ năng | như Broly (49 mục, hồi 1.000 ms) |
| Xuất hiện | chỉ sinh ra khi một Broly `leaveMap()`; đứng đúng chỗ Broly vừa biến mất |
| Nghỉ | 1 giây |
| Tự rời map | **300.000 ms (5 phút)**, gia hạn khi khu còn người |

- **Cơ chế nhận sát thương**: trần `hpMax / 100` mỗi đòn (trừ Tự phát nổ);
  **3,3%** mỗi đòn khiến boss tăng chỉ số (`hpMax += hpMax / nextInt(80,100)`, trần 16.070.777).
- **Cơ chế đặc biệt**: Super Broly **tự tạo một đệ tử thường cho chính mình**
  (`PetService.createNormalPet(this)`) khi vào map — tức đi kèm một đệ tử đánh phụ.
- **Rơi đồ**: **không rơi vật phẩm**. Thay vào đó **tặng người kết liễu một đệ tử thường**
  nếu người đó **chưa có đệ tử** (`if (plKill.pet == null) createNormalPet(plKill)`).
  Gọi `checkDoneTaskKillBoss` (có ghi chú FIX).

#### 5.4 Nhóm Halloween — Ma trơi (-349), Dơi (-350), Bí ma (-351)

Ba con có chỉ số giống hệt nhau, chỉ khác tạo hình. **Hiện không được tạo**
(`Halloween().init()` bị comment) và manager cũng không có thread.

| Tên | id | Tạo hình |
|---|---:|---|
| Ma trơi | -349 | 651/652/653 |
| Dơi | -350 | 654/655/656 |
| Bí ma | -351 | 760/761/762 |

| Mục | Giá trị |
|---|---|
| Hành tinh | Xayda |
| Máu | 500.000 |
| Sát thương gốc | 100 (Bí ma ghi đè thành `máu tối đa của mục tiêu / nextInt(30,50)` mỗi đòn) |
| Giáp | không có |
| Né đòn | `Util.isTrue(10, 1000)` = **1%** |
| Chia sát thương | `damage / 7` |
| **Trần sát thương/đòn** | **`hpMax / 50` = 10.000** |
| Máu hiệu dụng | 500.000, nhưng bị chặn ở **tối thiểu 50 đòn** |
| Kỹ năng | 4 Chiêu đấm Galick lv7, hồi `Util.nextInt(5000, 10000)` ms (bốc một lần khi nạp lớp) |
| Map | 73 map thường |
| Nghỉ | 600 giây |
| Loa | không (`isNotifyDisabled = true`), khu ≥ 2 |
| Tự rời map | 900.000 ms, gia hạn khi khu còn người |

- **Cơ chế đặc biệt**: mỗi đòn đánh trúng, boss áp hiệu ứng **Halloween**
  (`EffectSkillService.setIsHalloween(player, 2, 1800000)`) cho mục tiêu trong **30 phút**.
- **Rơi đồ**: **1× Bí ngô (id 585)**, 100%, khoá cho người kết liễu.
  **Không** cộng Point, **không** gọi `checkDoneTaskKillBoss`.
- **Lời thoại**: "Khà khà".

#### 5.5 Ông già Noel (-353)

| Mục | Giá trị |
|---|---|
| Hành tinh | Xayda |
| Máu | **500** |
| Sát thương | **5.000.000** |
| Giáp | không có |
| Tạo hình | 657/658/659 |
| Kỹ năng | 8 Tái tạo năng lượng lv7, hồi `nextInt(5000,10000)` ms |
| Map | 73 map thường (chọn khu đầu tiên có ≤ 10 người và chưa có Ông già Noel) |
| Nghỉ | 60 giây |
| Tự rời map | `nextInt(100.000, 300.000)` ms (1,7-5 phút), **không gia hạn** |
| Số bản hiện tại | **0** (sự kiện Noel bị tắt) |

- **Cơ chế đặc biệt — BẤT TỬ + PHÁT QUÀ**:
  - `injured()` **luôn trả 0** → không thể giết.
  - `attack()` **không dùng kỹ năng**; khi tới gần người chơi (≤ 50 px), mỗi **60 giây**
    gọi `giftBox()`: chat "Hô hô hô" và thả tối đa 3 **Hộp quà giáng sinh (id 648)**
    với tỉ lệ lần lượt **1/3, 1/5, 1/7**, **`playerId = -1` → ai nhặt cũng được**.
  - `reward()` rỗng.
- **Lời thoại**: "Mé ri chịch mệt" ⚠️ (chính tả/thô — nên sửa), "Hô hô hô",
  "Giáng sinh vui vẻ!".

#### 5.6 Thủy Tinh (-355) + Sơn Tinh (-354) — sự kiện Hùng Vương

`HungVuong().init()` gọi `createBoss(THUY_TINH, 10)` → **10 bản Thủy Tinh**;
mỗi bản tự sinh 1 Sơn Tinh đi kèm (`bossesAppearTogether`).

| Tên | id | Máu | Sát thương | Tạo hình |
|---|---:|---:|---:|---|
| Thủy Tinh | -355 | 50.000.000 | 1.000 | 1686/1687/1688 |
| Sơn Tinh | -354 | 50.000.000 | 1.000 | 1683/1684/1685 |

| Mục | Giá trị |
|---|---|
| Hành tinh | Xayda |
| Giáp | không có |
| Né đòn | 0% |
| **Trần sát thương/đòn** | **1.000.000** (đòn > 1 triệu bị ép về `nextInt(900.000, 1.000.000)`) |
| Số đòn tối thiểu | ≈ **50 đòn** mỗi con |
| Kỹ năng | 4 Chiêu đấm Galick lv1 (2.000 ms); 5 Chiêu Antomic lv1 (6.000 ms) |
| Map | 73 map thường |
| Nghỉ | 900 giây (15 phút) |
| Loa | không; Thủy Tinh `isZone01SpawnDisabled = false` (**có thể ra khu 0/1**) |
| Tự rời map | 900.000 ms, gia hạn khi khu còn người |

- **Cơ chế đặc biệt — trả thưởng ở trạng thái AFK**:
  Khi bị giết, `reward()` **không rơi đồ ngay** mà đặt `playerReward = plKill` và chuyển
  boss (hoặc cả nhóm) sang `AFK`. Trong `afk()`, boss thả phần thưởng rồi chat
  "Được! hảo hán!" và rời map sau 3 giây.
- **Rơi đồ**:
  - Thủy Tinh chết → thả **Cải trang Thủy Tinh (id 422)** ×1 cho người kết liễu, kèm option
    77 (+15-20), 103 (+15-20), 50 (+15-20), 94 (+1-10), 14 (+2-5), 154 (đặc biệt), 93 (+1-15).
  - Sơn Tinh chết → thả **Cải trang Sơn Tinh (id 421)** ×1, option 77/103/50 (+15-20),
    94 (+1-10), 95 (+1-15), **1/15 cơ hội thêm option 116**, 154, 93 (+1-15).
  - Cả hai `+5 Point`.
- **Lời thoại**: Thủy Tinh "Trả Mị Nương lại cho ta", "Ta cho nước dâng chìm cả lũ bây giờ";
  Sơn Tinh "Còn lâu á, chậm chân ráng chịu đi cưng", "Ta thách, chiêu này quá quen rồi".

#### 5.7 Lân con (-371) — sự kiện Tết

`id` thực tế = `BossID.LAN_CON - Util.nextInt(1000000)` → một số ngẫu nhiên
trong khoảng -1.000.371 … -371. **Hiện không được tạo** (sự kiện Tết bị tắt).

| Mục | Giá trị |
|---|---|
| Hành tinh | Xayda |
| Máu | 5.000.000 |
| Sát thương | 5.000 |
| Giáp | không có |
| Né đòn | `Util.isTrue(100, 1000)` = **10%** |
| **Trần sát thương/đòn** | **500.000** |
| Tạo hình | 763/764/765 |
| Kỹ năng | 8 Tái tạo năng lượng lv7, hồi `nextInt(5000,10000)` ms |
| Map | 73 map thường | 
| Nghỉ | 60 giây |
| Tự rời map | `nextInt(100.000, 300.000)` ms |

- **Cơ chế đặc biệt — KHÔNG CHẾT, ĐI THEO NGƯỜI CHƠI**:
  - Khi một đòn `damage >= hp` **hoặc** người chơi vừa chat một câu chứa chữ **"thang"**,
    boss **hồi đầy máu**, chuyển non-PK, gán `playerId` = người đó, chat
    "Đi thôi lân con!" và chuyển sang `AFK`.
  - Ở `AFK`, Lân con **bám theo người đó qua các map**; khi khoảng cách ≤ 300 px thì
    `pl.canReward = true`.
  - Khi người chơi nhận quà (`haveReward`), Lân con rời map.
  - Phần thưởng do `RewardService.rewardLancon()` trao (không qua `reward()` của boss):
    một trong **{734 Ngọc Thố, 920 Gậy như ý, 849 Pháo Thăng Thiên, 743 Chổi bay Phù Thủy,
    733 Cân đẩu vân ngũ sắc}** với bộ option ngẫu nhiên (5% ra bộ chỉ số "hiếm" thấp,
    95% ra bộ thường + option 93 `nextInt(1,30)`), luôn có option 89 và 30 (khoá).
    Cần **1 ô hành trang trống**.
- **Cơ chế phụ — húc chết**: khi ở gần (≤ 100 px) và máu boss chưa đầy, mỗi 30 giây có
  **10%** kéo người chơi tới chỗ boss và **giết ngay**
  ("Bạn đã bị Lân con húc chết!").
- **`reward()` rỗng** — mọi phần thưởng đi qua `rewardLancon`.
- **Lời thoại**: "Tùng tùng xèng xèng".

#### 5.8 Khỉ đột (-344) — sự kiện Trung Thu

| Mục | Giá trị |
|---|---|
| Hành tinh | Xayda |
| Máu | 100.000.000 |
| Sát thương | 100.000 |
| Giáp | không có |
| Né đòn | 0% |
| Chia sát thương | `damage / 7` |
| Máu hiệu dụng | **700.000.000** |
| Trần sát thương/đòn | không có |
| Tạo hình | 198/193/194 |
| Kỹ năng | 4 Chiêu đấm Galick lv7 (1.000); 5 Chiêu Antomic lv7 (3.000); **13 Biến hình (Biến khỉ) lv7 (60.000)** |
| Map | **0-20** (toàn bộ map khởi đầu 3 hành tinh) |
| Nghỉ | 900 giây (15 phút) |
| Loa | không; khu ≥ 2 |
| Tự rời map | 900.000 ms, gia hạn khi khu còn người |
| Số bản hiện tại | **0** (sự kiện Trung Thu bị tắt) |

- **Cơ chế đặc biệt**: có kỹ năng **Biến khỉ** — hoá khỉ đột khổng lồ (buff máu và
  sát thương theo `SkillUtil.getPercentHpMonkey/DameMonkey`), hồi 60 giây.
- **Rơi đồ**: **1× Đuôi khỉ (id 1045)**, 100%, khoá cho người kết liễu.
  **Không** cộng Point.
- **Lời thoại**: không có.

#### 5.9 Nguyệt thần (-345) + Nhật thần (-346) — sự kiện Trung Thu

| Tên | id | Máu | Sát thương | Tạo hình |
|---|---:|---:|---:|---|
| Nguyệt thần | -345 | 50.000.000 | 1.000 | 2058/2059/2060 |
| Nhật thần | -346 | 50.000.000 | 1.000 | 2065/2066/2067 |

| Mục | Giá trị |
|---|---|
| Giáp | không có |
| **Trần sát thương/đòn** | **1.000.000** (đòn > 1 triệu → `nextInt(900.000, 1.000.000)`) |
| Kỹ năng | 4 Chiêu đấm Galick lv1 (2.000); 5 Chiêu Antomic lv1 (6.000) |
| Map | **0-20** |
| Nghỉ | 900 giây |
| Loa | không; khu ≥ 2 |
| Tự rời map | 900.000 ms, gia hạn khi khu còn người |
| Số bản hiện tại | **0** |

- **Cơ chế**: giống cặp Thủy Tinh / Sơn Tinh — Nguyệt thần là boss cha,
  Nhật thần `APPEAR_WITH_ANOTHER`; thưởng được thả trong trạng thái `AFK` rồi rời map sau 3 giây.
- **Rơi đồ**:
  - Nguyệt thần → **item id 2123** ×1, option 77/103/50 (+10-20), 94 (+1-10), 14 (+1-10),
    154, 93 (+1-15).
  - Nhật thần → **item id 2124** ×1, cùng bộ option.
  - ⚠️ **Cả hai id 2123 và 2124 KHÔNG tồn tại trong `item_template`** (id lớn nhất là 1999).
    Xem Ghi chú.
  - **Không** cộng Point.
- **Lời thoại**: không có.

---

### 6. BOSS LUYỆN TẬP TỰ ĐỘNG

#### 6.1 Cơ chế chung (`TrainingBoss.java`, `TrainingService.java`)

- **Không tự xuất hiện**. Người chơi nói chuyện với NPC tương ứng → `TrainingService.callBoss`
  tạo boss, ẩn NPC đó với riêng người chơi (`sendHideNpc`), boss vào đúng khu của người chơi.
- Đăng ký vào `OtherBossManager` (`BossType.PHOBAN`), quét **150 ms**.
- `bossStatus` khởi tạo thẳng bằng `RESPAWN` → vào map ngay.
- **Chỉ đánh một người duy nhất** (`playerAtt`), không nhắm người khác.
- **Giáp**: không có (`defg = 0`).
- **Né đòn: 40%** — `Util.isTrue(400, 1000)` trong `TrainingBoss.injured`, có chat "Xí hụt".
- **Không có trần sát thương mỗi đòn** (đoạn giới hạn `hpMax/10` đã bị comment).
- **% giảm sát thương**: 0% (không lớp nào gán `damageReducePercentByLevel`).
- **Tự hồi máu (`buffPea`)**: mỗi **30 giây** hồi `hpMax / 5` máu và đầy KI,
  hiển thị hiệu ứng ăn đậu thần.
- Khi máu < 20%, boss chat ngẫu nhiên "AAAAAAAAA" / "ai da" (mỗi 2 giây).
- **Khi boss chết**: `die()` **không rơi bất kỳ vật phẩm nào**; chỉ chuyển `AFK`, đọc thoại
  kết, và nếu là chế độ **thách đấu** (`isThachDau`) thì `playerAtt.levelLuyenTap++`.
- **Khi người chơi chết**: boss chat "Luyện tập tiếp đi", chuyển `AFK` + non-PK.
- `leaveMap()` trả NPC lại (`luyenTapEnd`) và **xoá boss khỏi manager** (`dispose()`).
- **Lời thoại mở** có 2 phiên bản: chỉ số `[0]` khi luyện tập thường, `[1]` khi thách đấu.

#### 6.2 Bảng chi tiết

| Tên | id | Máu | Sát thương | Map | Tạo hình | Kỹ năng (id-tên-cấp-hồi ms) | Thời gian ở lại sau khi kết thúc |
|---|---:|---:|---:|---|---|---|---|
| Karin | -357 | 500 | 500 | 46 Tháp Karin (x=420) | 89/90/91 | 4 Galick 1 (1.000); 8 Tái tạo 1 (60.000) | 5 giây |
| Tàu Pảy Pảy | -309 | 1.000 | 500 | 46 Tháp Karin | 92/93/94 | 4 Galick 1 (1.000); 8 Tái tạo 1 (60.000) | 5 giây |
| Yajirô | -358 | 1.100 | 1.100 | 46 Tháp Karin (x=320) | 77/78/79 | 4 Galick 1 (1.000); 8 Tái tạo 1 (60.000) | 15 giây |
| Mr.PôPô | -359 | 5.100 | 1.100 | 46 Tháp Karin (x=295) | 77/78/79 | 4 Galick 1 (30.000); 1 Kamejoko 1 (30.000); 6 TDHS 3 (30.000) | 15 giây |
| Thượng đế | -360 | 1.000 | 10.000 | 49 Phòng tập thời gian | 86/87/88 | 0 Dragon 1 (1.000) | 5 giây |
| Khỉ Bubbles | -361 | 30.000 | 30.000 | 48 Hành tinh Kaio | 95/96/97 | 4 Galick 7 (1.000); **13 Biến khỉ 7 (1.800.000)** | 15 giây |
| Thần Vũ Trụ | -362 | 45.000 | 45.000 | 48 Hành tinh Kaio (x=420, y=240) | 98/99/100 | 4 Galick 7 (1.000); 6 TDHS 7 (30.000) | 7 giây (bay lên xuống y 240↔360) |
| Tổ sư Kaio | -363 | 45.000 | 45.000 | 50 Thánh địa Kaio | 448/449/450 | 4 Galick 1 (60.000) | **bất tử, không đánh nhau** |
| Whis | -364 | **550.000 × cấp** | **10.000 × cấp** | 154 Hành tinh Bill | 838/839/840 | 0 Chiêu đấm Dragon 7 (500 ms) | 1 giây |

**Ba trường hợp riêng cần lưu ý:**

- **Tổ sư Kaio (-363)** không phải boss đánh nhau: `injured()` **luôn trả 0** (bất tử) và
  `active()` chỉ cộng `TrainingService.getTnsmMoiPhut(player) / 6` sức mạnh+tiềm năng
  **mỗi 10 giây** cho người luyện tập. Rời map khi người chơi đổi khu.
- **Whis (-364)** có **cấp theo bảng xếp hạng**: `level = playerAtt.traning.getTop() + 1`;
  khi vào map `hpMax *= level`, `dame *= level`, tên hiển thị `"Whis [LV:<level>]"`,
  và `injured()` **chia sát thương cho `level`** → càng lên hạng cao Whis càng dai gấp bội.
  Hạ được Whis thì `plKill.thachdauwhis++` (đếm cho bảng xếp hạng Whis).
  Whis vẫn dùng `buffPea` kế thừa từ `TrainingBoss` (hồi `hpMax/5` mỗi 30 giây).
- **Tàu Pảy Pảy luyện tập (-309)**: `buffPea()` bị **ghi đè rỗng** → **không tự hồi máu**.
  Và nếu người chơi **chưa tới nhiệm vụ 10** (`getIdTask(plAtt) < ConstTask.TASK_10_1`)
  thì `injured()` trả về **100 sát thương giả** mà **không trừ máu** → không thể hạ được
  trước khi tới bước nhiệm vụ "Bái sư" (đoạn này có ghi chú "TUYẾN MỚI" trong code).

- **`secondsRest`**: Karin/Tàu Pảy Pảy/Yajirô/Mr.PôPô/Thượng đế/Khỉ Bubbles/Thần Vũ Trụ/
  Tổ sư Kaio = **1 giây**; Whis = **60 giây**. (Thực tế không dùng vì boss chỉ sống trong
  một phiên luyện tập.)
- **Cơ chế bay lung tung**: Karin bay đổi chỗ mỗi ~5 giây; Mr.PôPô mỗi ~3-4 giây
  (khó đánh trúng hơn).
- **Cơ chế riêng của Thượng đế**: kéo người chơi vào map **49 Phòng tập thời gian**
  khi bắt đầu, và trả về map **45 Thần điện** khi kết thúc (nếu người chơi chưa chết).
- **Lời thoại tiêu biểu**: Karin/Tàu Pảy Pảy "Ta sẽ dạy ngươi vài chiêu"; Mr.PôPô
  "Đánh trúng ta 1 cái coi như ngươi thắng" / "Đánh trúng ta 3 cái coi như ngươi thắng";
  Thượng đế "Ta sẽ dạy võ cho con trong phòng tập thời gian này" → kết "Ta rất tự hào về con";
  Khỉ Bubbles chỉ "Ù ù khẹt khẹt"; Thần Vũ Trụ "Ta sẽ dạy ngươi chiêu kaio-ken" →
  kết "Tại hôm nay ta...ta hơi bị đau bụng".
- **Rơi đồ**: **không boss luyện tập nào rơi vật phẩm**. Phần thưởng thực tế là
  sức mạnh/tiềm năng qua hệ thống luyện tập (`TrainingService.tangTnsmLuyenTap`,
  trần 10.000.000 TNSM) và `levelLuyenTap` khi thắng ở chế độ thách đấu.

---

### 7. GHI CHÚ

#### 7.1 Boss có code nhưng KHÔNG BAO GIỜ được tạo

| Boss | id | Lý do |
|---|---:|---|
| **Rồng Nhí** | -386998 | `BossManager.createBoss` có nhánh xử lý, nhưng `loadBoss()` không gọi và không sự kiện nào gọi |
| **Virut** | -79 | `createBoss` có nhánh nhưng không ai gọi; id lại trùng `XINBATO (-79)` và trùng id mà `MatTroi` dùng |
| **Ma trơi / Dơi / Bí ma** | -349/-350/-351 | `new Halloween().init()` bị comment trong `EventManager` |
| **Ông già Noel** | -353 | `new Christmas().init()` bị comment |
| **Lân con** | -371 | `new LunarNewYear().init()` bị comment |
| **Khỉ đột / Nguyệt thần / Nhật thần** | -344/-345/-346 | `new TrungThu().init()` bị comment |
| **Android 19 (bản độc lập)** | -30 | Chỉ sinh qua `bossesAppearTogether` của Dr.Kôrê, không có bản tự do |
| **Death Beam 1-5** | -609…-613 | Có `reward()` nhưng `injured()` luôn trả 0 nên nhánh thưởng là code chết |
| **`BossesData.CADIC_M`** | (-924) | Khối `BossData` được khai báo nhưng **không lớp nào tham chiếu tới** — code chết hoàn toàn |
| **`BossesData.AN_TROM`** | (-365) | Bị bỏ qua: lớp `Boss_mini/AnTrom` khai báo `BossData` riêng trong hàm dựng |
| **`BossesData.O_DO`** (map 168) | (-78) | Lớp `Boss_mini/Odo` cũng khai báo `BossData` riêng; bản này chỉ dùng cho boss Đại hội võ thuật (nửa 2) |

#### 7.2 Boss đang lỗi / có vấn đề

1. **Thread event manager thiếu.** `ServerManager.run()` **không khởi động thread** cho
   `TrungThuEventManager`, `HalloweenEventManager`, `ChristmasEventManager`,
   `HungVuongEventManager`, `LunarNewYearEventManager`. Boss được `Boss(BossType, ...)`
   đăng ký vào các manager này sẽ **không bao giờ được gọi `update()`**.
   → **Thủy Tinh / Sơn Tinh của sự kiện Hùng Vương hiện đang bật (10 bản) nhưng đứng im
   mãi ở trạng thái `REST`, không bao giờ ra map.** Đây là lỗi nghiêm trọng nhất phát hiện
   được trong nhóm này. Cách sửa: thêm `new Thread(HungVuongEventManager.gI(), "...").start();`
   (và các manager còn lại) vào `ServerManager.run()`, **hoặc** để các manager đó kế thừa
   vòng lặp của `BossManager` đang chạy.

2. **Vật phẩm không tồn tại trong DB**: Nguyệt thần rơi **id 2123**, Nhật thần rơi **id 2124**,
   trong khi `item_template` chỉ có id tới **1999**.
   `ItemService.createNewItem` sẽ không tìm thấy template → drop hỏng (item vô hình hoặc NPE).
   Cần thêm 2 item này vào DB hoặc đổi sang id hợp lệ.

3. **`Android13.injured` khoá chết vòng lặp.** Đoạn (Android13.java:40-62):
   ```java
   if (flag && !this.parentBoss.isDie()) {
       flag = false;
   }
   if (!flag) { return 0; }
   ```
   Kết quả: Android 13 **không thể chết chừng nào Android 14 còn sống**; nhưng Android 14
   lại chỉ chết được sau khi cơ chế `callApk13` đã chạy và 13 chết trước.
   Cần rà lại điều kiện — nhiều khả năng ý đồ ban đầu là "13 bất tử khi **15** còn sống",
   chứ không phải khi **14** còn sống.

4. **`Android19.injured` thiếu kiểm tra null.** Truy cập thẳng
   `plAtt.playerSkill.skillSelect.template.id` mà không kiểm tra null, trong khi bản
   `DrKore` tương đương đã được sửa để kiểm tra. Có thể ném NPE khi người tấn công chưa
   chọn kỹ năng (hoặc khi bị quái/pet đánh).

5. **Sát thương Baby hình dạng 3 = 30.000** trong khi hình dạng 1 và 2 là 200.000 / 250.000.
   Rất giống lỗi gõ thiếu một số 0 (đáng lẽ 300.000).

6. **Baby được phép ra khu 0/1 của Làng Kakarot (map 14).**
   `new Baby()` gọi `super(BossID.BABY, data...)` — hàm dựng **không có** tham số
   `isZone01SpawnDisabled`, nên cờ mặc định `false`. Với 200.000 sát thương và 2 bản chạy
   song song trên đúng map khởi đầu của hành tinh Xayda, đây là rủi ro cân bằng lớn
   (tương tự vấn đề đã được FIX cho nhóm Bojack và tiểu đội Namek).
   Thủy Tinh (-355) cũng đặt `isZone01SpawnDisabled = false` nhưng sát thương chỉ 1.000
   nên ít nguy hiểm hơn.

7. **`Util.isTrue` trong `AnTrom.attack()` xếp sai thứ tự**: nhánh `gold >= 1.000.000.000`
   nằm sau nhánh `gold >= 2.000.000` nên **không bao giờ chạy**; người giàu bị trộm
   200.000-1.000.000 mỗi 500 ms thay vì 4.000-5.000.

8. **Kuku / Mập Đầu Đinh / Rambo rời map đúng 15 phút bất kể có ai đang đánh**, vì đoạn
   gia hạn `st` khi khu còn người **đã bị comment** trong cả 3 lớp. Nếu đây không phải ý đồ
   thì cần bỏ comment.

9. **`+5 Point` nằm sai vị trí** ở `SieuBoHung.reward` và `XENCON*.reward`:
   lệnh cộng điểm nằm **bên trong** nhánh `if (Util.isTrue(80, 100))` của ngọc rồng,
   nên 20% số lần giết boss không được cộng điểm sự kiện.

10. **`BossID.DEATH_BEAM_5 = -613` trùng `BossID.HAKAI = -613`**;
    `BossID.SOI_HEC_QUYN1 = -77` trùng `BossID.SOI_HEC_QUYN = -77`;
    `BossID.O_DO1 = -78` trùng `BossID.O_DO = -78`;
    `BossID.Virut = -79` trùng `BossID.XINBATO = -79`.
    `BossManager.getBossById` / `checkBosses` lọc theo `id` nên các cặp trùng này có thể
    lấy nhầm boss. Đặc biệt `MatTroi` (20 bản, id -79) chia id với Xinbatô của Đại hội võ thuật.

11. **`BossManager.getBoss(int id)` thực chất là `bosses.get(index)`**, không phải tra theo id
    — dễ gây nhầm khi gọi. Hàm đúng là `getBossById`.

12. **Lời thoại thô tục cần rà soát**: `MatTroi` chat `"Nóng vãi lồn, <tên> Đã bị bỏng nhiệt"`;
    `OngGiaNoel` chat `"Mé ri chịch mệt"`.

13. **`nPoint.subDameInjureWithDeff` trả về `int`** trong khi tham số là `long`.
    Với boss không có giáp thì hàm là phép gán thuần, nhưng nếu sát thương vượt
    2.147.483.647 thì phép ép kiểu `(int)` sẽ **tràn số âm**. Hiện chưa gây hại vì hầu hết
    boss có trần sát thương, nhưng là bẫy cần biết.

#### 7.3 Số liệu KHÔNG TRA ĐƯỢC / chưa chắc chắn

- **Cấp của Whis** phụ thuộc `playerAtt.traning.getTop() + 1` — **không tra được** giá trị
  thực tế của `getTop()` (bảng xếp hạng luyện tập) trong phạm vi lần rà này, nên "550.000 × cấp"
  chỉ là công thức chứ không phải con số cụ thể.
- **Ý nghĩa cụ thể của các option id** (50, 77, 93, 94, 95, 103, 107, 112, 116, 154, 204, 207)
  gắn vào vật phẩm rơi: tra ở bảng `item_option_template`, **chưa đối chiếu trong tài liệu này**.
- **`ItemTimeService.sendItemTime(player, 12953, 60)` (Mặt Trời) và `(player, 7143, 10)` (Virut)**:
  hai con số 12953 / 7143 là id hiệu ứng client, **không tra được tên** từ DB.
- **Tỉ lệ thực tế của `randDoTLBoss`**: các mốc `Util.isTrue` viết nối tiếp nên xác suất
  cuối cùng là tích luỹ chứ không phải đúng con số ghi trong comment (comment ghi
  "Găng tay (15%)" nhưng code là `isTrue(25,100)` sau nhánh nhẫn 10%). Con số trong mục 0.7
  là đọc theo code, không theo comment.
- **`BossManager.ratioReward = 10`**: biến public static tồn tại nhưng tôi **không tìm thấy
  chỗ nào dùng** — có thể là tàn dư.
- **Map 168 (dùng bởi `BossesData.O_DO`) và map 165 (dùng bởi `BossesData.CADIC_M`)**
  **không tồn tại** trong `map_template` (id lớn nhất = 161, và id 150 bị thiếu).
  Với hai `BossData` đó, `MapService.getMapWithRandZone()` sẽ không tìm ra map.
  Hiện chưa gây hại vì cả hai đều không được dùng bởi boss thuộc nửa này, nhưng
  `boss/dai_hoi_vo_thuat/ODo.java` (nửa 2) **có** dùng `BossesData.O_DO` → cần nửa 2 kiểm lại.


---

# PHẦN B — BOSS NHIỆM VỤ, PHÓ BẢN, ĐẤU TRƯỜNG, NHÂN BẢN

## 34b — Tra cứu BOSS: nhiệm vụ, phó bản, đấu trường, nhân bản, luyện tập

> **Phạm vi**: nửa 2 của bảng tra cứu boss — boss nhiệm vụ (mới), boss phó bản, boss
> đấu trường/giải đấu, boss nhân bản, boss luyện tập tự động.
> Boss thế giới / boss mini / boss sự kiện nằm ở tài liệu nửa 1.
>
> **Nguồn**: đọc trực tiếp mã nguồn `SRC/src/nro/models/**` ở trạng thái hiện tại
> (nhánh `main`, sau đợt sửa tuyến nhiệm vụ mới), đối chiếu tên vật phẩm/map/kỹ năng
> với `database team2026.sql` và `SRC/sql/patch/01-vat-pham-moi.sql`.
> Mọi số liệu dưới đây lấy từ code, **không lấy từ tài liệu cũ**.

---

### 0. Ba quy ước đọc bảng

#### 0.1. Giáp (phòng thủ) của boss

`BossData` **không có trường giáp**. Lúc chạy, giáp được tính trong `NPoint.setDef()`:

```
def = defg * 4 + defAdd   (+10% nếu đang dùng Nước Mía 3)
```

`Boss.initBase()` chỉ gán `mpg`, `dameg`, `hpg` — **không bao giờ gán `defg`**, và
`NPoint.resetPoint()` đặt `defAdd = 0`. Vì vậy:

> **Với gần như toàn bộ boss trong tài liệu này: giáp thực tế = 0.**

Hai ngoại lệ **duy nhất** (boss dùng lại nguyên `nPoint` của một người chơi thật):

| Boss | Giáp thực tế |
|---|---|
| **Rival** (đấu Siêu Hạng, `boss/sieu_hang/Rival.java`) | Bằng đúng giáp của người chơi bị sao chép — `SuperRank.joinMap()` gán `this.nPoint = player.nPoint` |
| Boss **Nhân Bản** (`boss/nhan_ban/NhanBan.java`) | **0** — chỉ sao chép máu/sát thương ×10, không sao chép `nPoint` |

Hệ quả: `nPoint.subDameInjureWithDeff(dame)` (trừ giáp) là **phép trừ 0** ở mọi boss
trừ Rival — nó chỉ còn tác dụng chặn sàn `dame < 0 → dame = 1`.

#### 0.2. % giảm sát thương nhận vào (cơ chế MỚI)

Bảng `boss/BossDamageReduce.java`, đọc theo `Boss.currentLevel`, áp dụng trong
`Boss.applyDamageReduce()` **sau** khiên/giáp và **ngay trước** khi trừ máu.
Trần cứng `MAX_PERCENT = 90`. Sát thương tối thiểu luôn ≥ 1.

**Chỉ những boss dưới đây có gán `damageReducePercentByLevel`:**
8 boss nhiệm vụ + Heart (trong tài liệu này) và 4 boss thế giới (tài liệu nửa 1).
**Toàn bộ boss phó bản, đấu trường, nhân bản, luyện tập đều để `null` → 0%.**
(Nhiều con trong số đó có cơ chế chia sát thương **riêng** viết thẳng trong `injured()`
— xem cột "Cơ chế" của từng mục.)

Boss còn bắn một câu chat một lần cho mỗi hình dạng: `"Đòn của ngươi yếu đi N% trước ta"`.

**Máu hiệu dụng = máu danh nghĩa ÷ (1 − %/100).**

#### 0.3. Số bản cùng lúc

Mọi map liên quan trong `map_template` đều có **10 khu (zone)**, sức chứa **15 người/khu**.

---

### 1. BẢNG TỔNG HỢP TOÀN BỘ BOSS NỬA 2

#### 1.1. Boss nhiệm vụ (dải −2000/−2001 và −2100…−2105) + Heart

| # | Tên | Id | Số hình dạng | Nhiệm vụ | Map | Máu danh nghĩa (tổng) | Máu hiệu dụng (tổng) | Giảm ST | Rơi đồ |
|---|---|---|---|---|---|---|---|---|---|
| 1 | Kẻ Thu Gom | −2000 | 1 | NV 6 b.1 | 4 / 12 / 18 | 80.000 | 80.000 | 0% | Không |
| 2 | Jaco Mất Ký Ức → Jaco Vô Thức | −2001 | 2 | NV 15 b.1 | 27 / 31 / 35 | 3.700.000 | 3.700.000 | 0% | Không |
| 3 | Xên bọ hung (bản NV) | −2100 | 3 | NV 30 b.2/b.3 | 100 | 2.790.000 | ~3.107.444 | 8/10/12% | Không |
| 4 | Cooler (bản NV) | −2101 | 2 | NV 34 b.4 | 110 | 6.300.000 | ~7.215.686 | 10/15% | Không |
| 5 | Mabư (bản NV) | −2102 | 5 | NV 37 b.1 | 127 | 10.000.000 | ~11.918.284 | 12→20% | Không |
| 6 | Black Goku (bản NV) | −2103 | 2 | NV 38 b.2/b.3 | 102, 92–100 | 6.700.000 | ~8.147.059 | 15/20% | Không |
| 7 | Baby (bản NV) | −2104 | 3 | NV 39 b.6 | 14 | 5.700.000 | ~7.286.303 | 18/22/25% | Không |
| 8 | Cumber (bản NV) | −2105 | 2 | NV 42 b.3/b.4 | 155 | 8.100.000 | ~11.609.477 | 28/32% | Không |
| 9 | **Heart** (4 hình dạng) | −108108 | 4 | NV 46 / 47 / 50 | 166 → 145 (→155) | 7.500.000.000 | **~14.666.208.791** | 20/35/50/65% | 1× id 2029 ở hình dạng 3 |

#### 1.2. Boss phó bản

| # | Tên | Id | Phó bản | Map | Máu | Sát thương | Giảm ST (bảng) | Cơ chế riêng |
|---|---|---|---|---|---|---|---|---|
| 10 | Drabura | −233 | Mabư 12h | 114 | 20.000.000 | 10.000 | 0% | Né 10%; trần 20 tr/đòn |
| 11 | Bui Bui | −234 | Mabư 12h | 115 | 40.000.000 | 200.000 | 0% | Né 20% |
| 12 | Bui Bui (2) | −238 | Mabư 12h | 117 | 40.000.000 | 200.000 | 0% | Né 20% |
| 13 | Ya côn | −235 | Mabư 12h | 118 | 50.000.000 | 200.000 | 0% | Né 20%; tàng hình |
| 14 | Drabura (2) | −237 | Mabư 12h | 119 | 20.000.000 | 200.000 | 0% | Né 20%; gọi Gôku + Ca Đít |
| 15 | Gôku | −341 | Mabư 12h | 119 | 60.000.000 | 1.000 | 0% | Gọi kèm; trần 20 tr/đòn |
| 16 | Ca Đít | −342 | Mabư 12h | 119 | 60.000.000 | 1.000 | 0% | Gọi kèm; trần 10 tr/đòn |
| 17 | Mabư | −236 | Mabư 12h | 120 | 100.000.000 | 10.000 | 0% | Né 20%; trần 50 tr/đòn; gọi Drabura 3 |
| 18 | Drabura (3) | −343 | Mabư 12h | 114 | 20.000.000 | 100.000 | 0% | Gọi kèm Mabư; trần 20 tr/đòn |
| 19 | **Mabư 14h** (5 hình dạng) | −214 | Mabư 14h | 127 | 50+60+80+100+150 tr | 500.000 | 0% | Ăn người chơi; hoá sôcôla; trần 30 tr/đòn; **hình dạng 5 chỉ chết bằng Quả cầu kênh khí** |
| 20 | Super Bư (trong bụng) | −348 | Mabư 14h | 128 | 50.000.000 | 500.000 | 0% | Sát thương dội ngược sang −214 |
| 21 | Số 1…Số 6 (Saibamen) | −18 … −23 | Con đường rắn độc | 144 | theo cấp | theo cấp | 0% | **Nhận `dame/7`**; chết là tự nổ gây `hpMax × 100` |
| 22 | Nađíc | −16 | Con đường rắn độc | 144 | theo cấp | theo cấp | 0% | Tái tạo năng lượng |
| 23 | Cađích | −15 | Con đường rắn độc | 144 | theo cấp | theo cấp | 0% | Dưới 50% máu → biến khỉ, **×2 máu tối đa**; choáng 5 s |
| 24 | Dr Lychee | −208 | Khí gas huỷ diệt | 148 | theo cấp | theo cấp | 0% | Né `level/1000`; giảm thêm `level/10` % | 
| 25 | Hatchiyack | −207 | Khí gas huỷ diệt | 148 | Lychee ×1,5 | Lychee ×1,5 | 0% | Né `(level+10)/1000`; giảm thêm `level/5` % |
| 26 | Trung uý Trắng | −4 | Doanh trại Độc Nhãn | 59 | theo bang | theo bang | 0% | Né 20%; `dame/2`; **bất tử khi Bulon còn sống** |
| 27 | Trung uý Thép | −5 | Doanh trại Độc Nhãn | 55 | theo bang ×1,15 | theo bang ×1,15 | 0% | Né 20%; `dame/2` |
| 28 | Trung uý Xanh Lơ | −6 | Doanh trại Độc Nhãn | 62 | theo bang ×1,1 | theo bang ×1,1 | 0% | Né 20%; `dame/2` |
| 29 | Ninja Áo Tím | −7 | Doanh trại Độc Nhãn | 54 | theo bang ×1,2 | theo bang ×1,2 | 0% | Né 30%; `dame/2`; 50% máu → gọi 4–6 phân thân |
| 30 | Ninja Áo Tím (phân thân) | −9 … −14 | Doanh trại Độc Nhãn | 54 | 1/10 boss mẹ | 1/10 boss mẹ | 0% | Né 20%; `dame/2` |
| 31 | Rôbốt Vệ Sĩ 00…03 | −8 … −11 | Doanh trại Độc Nhãn | 57 | theo bang ×1,3 | theo bang ×1,3 | 0% | `dame/2`; 4 con cùng lúc |
| 32 | Trung úy Xanh Lơ (kho báu) | −6 | Bản đồ kho báu | 137 | 20 tr × cấp | 200.000 × cấp | 0% | Không |

#### 1.3. Boss đấu trường / giải đấu

| # | Tên | Id | Giải | Map | Máu | Sát thương | Giảm ST |
|---|---|---|---|---|---|---|---|
| 33 | Sói hẹc quyn | −77 | ĐHVT 23 (vòng 1) | 129 | 10.000 | 1.000 | 0% |
| 34 | Ở dơ | −78 | ĐHVT 23 (vòng 2) | 129 | 25.000 | 3.000 | 0% |
| 35 | Xinbatô | −79 | ĐHVT 23 (vòng 3) | 129 | 50.000 | 6.000 | 0% |
| 36 | Cha pa | −80 | ĐHVT 23 (vòng 4) | 129 | 100.000 | 9.000 | 0% |
| 37 | Pon put | −81 | ĐHVT 23 (vòng 5) | 129 | 250.000 | 10.000 | 0% |
| 38 | Chan xư | −82 | ĐHVT 23 (vòng 6) | 129 | 500.000 | 10.000 | 0% |
| 39 | Tàu Pảy Pảy | −83 | ĐHVT 23 (vòng 7) | 129 | 2.000.000 | 15.000 | 0% |
| 40 | Yamcha | −84 | ĐHVT 23 (vòng 8) | 129 | 5.000.000 | 15.000 | 0% |
| 41 | Jacky Chun | −85 | ĐHVT 23 (vòng 9) | 129 | 25.000.000 | 20.000 | 0% |
| 42 | Thiên xin hăng | −86 | ĐHVT 23 (vòng 10) | 129 | 75.000.000 | 22.000 | 0% |
| 43 | Thiên xin hăng (phân thân) | −88 … −91 | ĐHVT 23 (vòng 10) | 129 | 20.000.000 | 5.000 | 0% |
| 44 | Liu Liu | −87 | ĐHVT 23 (vòng 11) | 129 | 150.000.000 | 30.000 | 0% |
| 45 | PôCôLô | −92 | ĐHVT 23 (vòng 12) | 129 | 100.000.000 | 50.000 | 0% |
| 46 | Đracula | −93 | Võ đài Hạt Mít (v.1) | 112 | **111% máu người chơi** | 1.000 | 0% |
| 47 | Người vô hình | −94 | Võ đài Hạt Mít (v.2) | 112 | **112% máu người chơi** | 1.000 | 0% |
| 48 | Bông băng | −95 | Võ đài Hạt Mít (v.3) | 112 | **113% máu người chơi** | 2.000 | 0% |
| 49 | Vua Quỷ Sa tăng | −96 | Võ đài Hạt Mít (v.4) | 112 | **114% máu người chơi** | 2.000 | 0% |
| 50 | Thỏ Đầu Bạc | −97 | Võ đài Hạt Mít (v.5) | 112 | **115% máu người chơi** | 3.000 | 0% |
| 51 | Rival (đối thủ Siêu Hạng) | −(id đối thủ) | Siêu Hạng | 113 | = `hpg` đối thủ | = `dameg` đối thủ | 0% |

#### 1.4. Boss nhân bản và luyện tập tự động

| # | Tên | Id | Nhóm | Map | Máu | Sát thương | Giảm ST |
|---|---|---|---|---|---|---|---|
| 52 | Bản sao Commeson | `createIdBossClone(idNV)` | Nhân bản | 140 | **máu người chơi ×10** | **sát thương ×10** | 0% |
| 53 | Tàu Pảy Pảy (dạy chiêu) | −385 | Luyện tập | 46 | 1.000 | 500 | 0% |
| 54 | Karin | −357 | Luyện tập | 46 | 500 | 500 | 0% |
| 55 | Yajirô | −358 | Luyện tập | 46 | 1.100 | 1.100 | 0% |
| 56 | Mr.PôPô | −359 | Luyện tập | 46 | 5.100 | 1.100 | 0% |
| 57 | Thượng đế | −360 | Luyện tập | 49 | 1.000 | 10.000 | 0% |
| 58 | Khỉ Bubbles | −361 | Luyện tập | 48 | 30.000 | 30.000 | 0% |
| 59 | Thần Vũ Trụ | −362 | Luyện tập | 48 | 45.000 | 45.000 | 0% |
| 60 | Tổ sư Kaio | −363 | Luyện tập | 50 | 45.000 | 45.000 | 0% |
| 61 | Whis | −364 | Luyện tập | 154 | 550.000 × cấp | 10.000 × cấp | 0% (nhưng `dame/cấp`) |

**Tổng: 61 loại boss.**

---

## PHẦN A — BOSS NHIỆM VỤ (MỚI)

### A0. Cơ chế chung của lớp `QuestBoss`

Tệp: `SRC/src/nro/models/boss/quest/QuestBoss.java`. Áp dụng cho **cả 9 boss**
(8 boss nhiệm vụ + Heart).

| Mục | Giá trị thực tế trong code |
|---|---|
| Thông báo toàn server | **Tắt** (`isNotifyDisabled = true` trong constructor `super(id, true, false, data)`) |
| Thời gian nghỉ sau khi chết | **Ngẫu nhiên 15–30 phút** (`REST_MIN_SECONDS = 900`, `REST_MAX_SECONDS = 1800`, bốc lại mỗi lần chết) |
| `secondsRest` trong `BossData` | **BỊ BỎ QUA** — `QuestBoss.rest()` ghi đè hoàn toàn `Boss.rest()`, không đọc `secondsRest`. Các giá trị `REST_1_M`, `REST_2_M`, `REST_5_M`, `REST_10_M` ghi trong `BossesData` là **số chết** |
| Hồi sinh ở đâu | **Khu ngẫu nhiên** trong danh sách `mapJoin` của hình dạng sắp tới (`findRandomZone`, thử tối đa 30 lượt/tick, giãn cách quét 3 giây) |
| Điều kiện hồi sinh | **Không có điều kiện về người chơi.** Chủ dự án chốt 18/09/2026: boss luôn có mặt, ai vào được map là đánh được |
| Không đè boss thế giới | `isZoneOccupied()` — không vào khu đang có boss cùng id, hoặc có boss thế giới bản gốc (`worldBossIds`). Boss lang thang (Ăn Trộm, Ở Dơ…) **không** tính |
| Khu vắng người | **Boss KHÔNG tự rời map** — `autoLeaveMap()` bị ghi đè thành lệnh rỗng (chỉ dời mốc `lastTimeHavePlayer`) |
| Đổi map giữa 2 hình dạng | `joinMap()` giữ nguyên trạng thái `JOIN_MAP` và chờ; quá **10 phút** (`JOIN_TIMEOUT`) không tìm được khu hợp lệ thì bỏ lượt, về nghỉ |
| Số bản mỗi loại | **3** (đặt trong `BossManager.loadBoss()`) |
| Sát thương nhận vào | `injured()` riêng: né đòn → trừ giáp (=0) → khiên năng lượng → **`applyDamageReduce()`** → trừ máu |
| Rơi đồ | `reward()` gọi `TaskService.checkDoneTaskKillBoss()` rồi `dropQuestItem()`. **KHÔNG vàng, KHÔNG trang bị, KHÔNG Ngọc Rồng, KHÔNG đồ Thần Linh** — đã kiểm chứng: không có lời gọi `randDoTLBoss`, không có item 190, không có id 14–20 ở bất kỳ lớp con nào |
| Vật phẩm nhiệm vụ | `getQuestItemId(level)` mặc định trả `−1` (không rơi gì). **Chỉ Heart ghi đè** |
| Gắn nhãn vật phẩm rơi | Luôn kèm `ItemOption(30, 0)` = **Không thể giao dịch** |

---

### A1. Kẻ Thu Gom — id −2000

| Mục | Giá trị |
|---|---|
| Lớp | `boss/quest/KeThuGom.java` |
| Nhóm | Boss cốt truyện chương 1 (dải −2000…−2099), **không có bản gốc trong game** |
| Dùng ở | **NV 6 bước 1** (`TASK_6_1`) — `TaskService.checkDoneTaskKillBoss` case `BOSS_KE_THU_GOM` |
| Số hình dạng | 1 |
| `worldBossIds` | rỗng (không cần tránh boss nào) |

#### Hình dạng

| # | Tên | Máu | Sát thương | % giảm ST | Máu hiệu dụng |
|---|---|---|---|---|---|
| 0 | Kẻ Thu Gom | 80.000 | 400 | 0% | 80.000 |

#### Giáp
**0** (xem §0.1). `QuestBoss.injured` có gọi `subDameInjureWithDeff` nhưng def = 0.

#### Kỹ năng

| Id | Tên | Cấp | Hồi chiêu |
|---|---|---|---|
| 0 | Chiêu đấm Dragon | 3 | 1.000 ms |

#### Map xuất hiện

| Map | Tên |
|---|---|
| 4 | Rừng xương |
| 12 | Vực maima |
| 18 | Rừng thông Xayda |

3 bản chạy song song, mỗi bản vào một khu ngẫu nhiên trong 3 map trên (10 khu/map).

#### Thời gian
Chết → nghỉ ngẫu nhiên 15–30 phút → hiện lại ở khu ngẫu nhiên. Không giới hạn lượt/ngày.

#### Rơi đồ
**Không rơi gì.** Phần thưởng NV 6 trao qua `rewardDoneTask`.

#### Cơ chế đặc biệt
Không có. Tạo hình mượn NPC 26 "Độc Nhãn" (outfit 144/145/146) nên không cần tài nguyên client mới.

#### Lời thoại
- Xuất hiện: *"Đừng phiền. Tôi chỉ đang dọn dẹp. Mấy thứ này chúng nó có giữ cũng chẳng để làm gì."*
- Khi đánh: *"Ngươi còn nhớ mẹ ngươi tên gì không? Thấy chưa, tôi giúp ngươi nhẹ hơn thôi mà."*
- Khi chết: *"Ông chủ... sẽ tự đến lấy... phần của ngài..."*

---

### A2. Jaco Vô Thức — id −2001

| Mục | Giá trị |
|---|---|
| Lớp | `boss/quest/JacoVoThuc.java` |
| Nhóm | Boss cốt truyện chương 2, **không có bản gốc** |
| Dùng ở | **NV 15 bước 1** (`TASK_15_1`) — **chỉ tính khi hạ hình dạng CUỐI** (`currentLevel == 1`) |
| Số hình dạng | 2 |

#### Hình dạng

| # | Tên | Máu | Sát thương | % giảm ST | Máu hiệu dụng |
|---|---|---|---|---|---|
| 0 | Jaco Mất Ký Ức | 1.200.000 | 3.000 | 0% | 1.200.000 |
| 1 | Jaco Vô Thức | 2.500.000 | 5.000 | 0% | 2.500.000 |

#### Giáp
**0**.

#### Kỹ năng

| Hình dạng | Id | Tên | Cấp | Hồi chiêu |
|---|---|---|---|---|
| 0 | 0 | Chiêu đấm Dragon | 5 | 1.000 ms |
| 0 | 10 | Quả cầu kênh khí | 3 | 8.000 ms |
| 1 | 0 | Chiêu đấm Dragon | 7 | 1.000 ms |
| 1 | 10 | Quả cầu kênh khí | 5 | 6.000 ms |
| 1 | 20 | Dịch chuyển tức thời | 3 | 15.000 ms |

#### Map xuất hiện

| Map | Tên |
|---|---|
| 27 | Rừng Bamboo |
| 31 | Núi hoa vàng |
| 35 | Rừng cọ |

3 bản song song.

#### Thời gian
Nghỉ ngẫu nhiên 15–30 phút giữa hai lượt (hình dạng 1 nối tiếp ngay sau hình dạng 0 vì `AppearType.ANOTHER_LEVEL`).

#### Rơi đồ
**Không rơi gì.** "Mảnh Ký Ức 2" (id 2003) trao qua `rewardDoneTask`.

#### Cơ chế đặc biệt
Hai hình dạng dùng **cùng một tạo hình** (outfit 624/625/626, mượn NPC 63 Jaco) — chỉ đổi tên, máu và chiêu.

#### Lời thoại
- Xuất hiện: *"Đứng im. Cảnh sát vũ trụ đây. Ngươi là... ngươi là... ngươi là ai?"*
- Khi đánh (hd 0): *"Ta có một người bạn. Ta nhớ là có. Nhưng ta không nhớ mặt nó."*
- Chuyển hình dạng: *"Đừng lại gần! Ta không điều khiển được tay mình nữa!"*
- Khi đánh (hd 1): *"Đừng gọi tên ta nữa! Mỗi lần ngươi gọi là ta lại đau!"*
- Khi chết: *"...Ngươi vẫn gọi đúng tên ta. Cảm ơn. Cảm ơn vì đã không quên ta."*

---

### A3. Xên bọ hung (bản nhiệm vụ) — id −2100

| Mục | Giá trị |
|---|---|
| Lớp | `boss/quest/XenBoHungNhiemVu.java` |
| Nhóm | Bản nhiệm vụ của boss thế giới **−100 Xên bọ hung** (bản gốc giữ nguyên HP 50/100/150 triệu) |
| Dùng ở | **NV 30**: hạ hình dạng 0 hoặc 1 → `TASK_30_2`; hạ hình dạng 2 → `TASK_30_3` |
| `worldBossIds` | `{BossID.XEN_BO_HUNG}` (−100) |

#### Hình dạng

| # | Tên | Máu | Sát thương | % giảm ST | Máu hiệu dụng |
|---|---|---|---|---|---|
| 0 | Xên bọ hung | 800.000 | 12.000 | **8%** | ≈ 869.565 |
| 1 | Xên hoàn thiện | 930.000 | 12.500 | **10%** | ≈ 1.033.333 |
| 2 | Xên hoàn thiện | 1.060.000 | 13.000 | **12%** | ≈ 1.204.545 |

#### Giáp
**0**.

#### Kỹ năng

| Hình dạng | Id | Tên | Cấp | Hồi chiêu |
|---|---|---|---|---|
| 0 | 1 | Chiêu Kamejoko | 7 | 1.000 ms |
| 0 | 17 | Liên hoàn | 7 | 10.000 ms |
| 0 | 20 | Dịch chuyển tức thời | 3 | 10.000 ms |
| 1 | 1 | Chiêu Kamejoko | 7 | 1.000 ms |
| 1 | 17 | Liên hoàn | 7 | 10.000 ms |
| 2 | 1 | Chiêu Kamejoko | 7 | 1.000 ms |
| 2 | 20 | Dịch chuyển tức thời | 7 | 10.000 ms |
| 2 | 17 | Liên hoàn | 7 | 10.000 ms |

#### Map xuất hiện
Map **100 — Thị trấn Ginder** (cả 3 hình dạng). 3 bản song song, mỗi bản một khu ngẫu nhiên trong 10 khu.

#### Thời gian
Nghỉ ngẫu nhiên 15–30 phút. `REST_1_M` ghi trong `BossesData` **không được dùng**.

#### Rơi đồ
**Không rơi gì.**

#### Cơ chế đặc biệt
Không đứng chung khu với **−100** (bản thế giới). Bản gốc `−100` vẫn giữ nguyên `dame/2` và bảng rơi đồ; bản nhiệm vụ **không** có `dame/2`.

#### Lời thoại
*"Lại một kẻ nữa đi tìm mẫu 07"* → *"Ta có thể tái tạo mọi bộ phận cơ thể!"* →
*"Chưa xong... ta còn hình dạng khác"* → *"Đây mới là dạng hoàn hảo"* →
*"Đồ khốn kiếp!! Rồi ngươi sẽ phải trả giá"*.

---

### A4. Cooler (bản nhiệm vụ) — id −2101

| Mục | Giá trị |
|---|---|
| Lớp | `boss/quest/CoolerNhiemVu.java` |
| Nhóm | Bản nhiệm vụ của boss thế giới **−29 Cooler** |
| Dùng ở | **NV 34 bước 4** (`TASK_34_4`) — hạ **bất kỳ hình dạng nào** đều tính |
| `worldBossIds` | `{BossID.COOLER}` (−29) |

#### Hình dạng

| # | Tên | Máu | Sát thương | % giảm ST | Máu hiệu dụng |
|---|---|---|---|---|---|
| 0 | Cooler | 3.000.000 | 18.700 | **10%** | ≈ 3.333.333 |
| 1 | Cooler 2 | 3.300.000 | 18.700 | **15%** | ≈ 3.882.353 |

#### Giáp
**0**.

#### Kỹ năng (cả 2 hình dạng giống nhau)

| Id | Tên | Cấp | Hồi chiêu |
|---|---|---|---|
| 4 | Chiêu đấm Galick | 1 | 2.000 ms |
| 5 | Chiêu Antomic | 1 | 6.000 ms |

#### Map xuất hiện
Map **110 — Hang băng**. 3 bản song song.

#### Thời gian
Nghỉ ngẫu nhiên 15–30 phút.

#### Rơi đồ
**Không rơi gì.** Bản gốc −29 vẫn là nguồn đồ Thần Linh / Ngọc Rồng.

#### Cơ chế đặc biệt
Không đứng chung khu với −29.

#### Lời thoại
*"Ngươi lần theo dấu băng tới tận đây à?"* → *"Ta chính là Vũ Trụ Đệ Nhất Cao Thủ"* →
*"Biến hình, hây aaaa..."* → *"Chúng mày nghĩ kiến lại thắng nổi khủng long sao"* →
*"Mọi chuyện chưa kết thúc đâu"*.

---

### A5. Mabư (bản nhiệm vụ) — id −2102

| Mục | Giá trị |
|---|---|
| Lớp | `boss/quest/MabuNhiemVu.java` |
| Nhóm | Bản nhiệm vụ của boss phó bản **−214 Mabư 14h** |
| Dùng ở | **NV 37 bước 1** (`TASK_37_1`) — **chỉ tính khi hạ hình dạng 5** (`currentLevel == 4`) |
| `worldBossIds` | `{BossID.MABU (−214), BossID.SUPERBU (−348)}` |

#### Hình dạng

| # | Tên | Máu | Sát thương | % giảm ST | Máu hiệu dụng |
|---|---|---|---|---|---|
| 0 | Mabư mập | 2.000.000 | 24.000 | **12%** | ≈ 2.272.727 |
| 1 | Super Bư | 2.000.000 | 24.000 | **14%** | ≈ 2.325.581 |
| 2 | Bư Tênk | 2.000.000 | 24.000 | **16%** | ≈ 2.380.952 |
| 3 | Bư Han | 2.000.000 | 24.000 | **18%** | ≈ 2.439.024 |
| 4 | Kid Bư | 2.000.000 | 24.000 | **20%** | 2.500.000 |

#### Giáp
**0**.

#### Kỹ năng (giống nhau ở cả 5 hình dạng)

| Id | Tên | Cấp | Hồi chiêu |
|---|---|---|---|
| 1 | Chiêu Kamejoko | 3 | 5.000 ms |
| 0 | Chiêu đấm Dragon | 7 | 1.000 ms |

#### Map xuất hiện
Map **127 — Cổng phi thuyền**. 3 bản song song.

#### Thời gian
Nghỉ ngẫu nhiên 15–30 phút. **Không phụ thuộc khung giờ 14h** (khác bản phó bản gốc).

#### Rơi đồ
**Không rơi gì.**

#### Cơ chế đặc biệt (khác bản gốc −214)
Bản nhiệm vụ **bỏ hết** ba cơ chế chặn đường của bản phó bản:
- Không ăn người chơi (không nuốt vào map 128).
- Không né 10%, không trần 30 triệu sát thương/đòn.
- **Hình dạng cuối KHÔNG bắt buộc dùng Quả cầu kênh khí** — đánh thường hạ được.

#### Lời thoại
*"Mabư đói bụng rồi"* → *"Khí công pháo" / "Úm ba la xì bùa"* → *"Biến hình"* (mỗi lần đổi hình dạng) →
hình dạng 5: *"Bư nhỏ lại rồi, nhưng dữ hơn"* → *"Bư thua rồi..."*.

---

### A6. Black Goku (bản nhiệm vụ) — id −2103

| Mục | Giá trị |
|---|---|
| Lớp | `boss/quest/BlackGokuNhiemVu.java` |
| Nhóm | Bản nhiệm vụ của boss thế giới **−203 Black Goku** |
| Dùng ở | **NV 38**: hạ hình dạng 0 → `TASK_38_2`; hạ hình dạng 1 → `TASK_38_3` (hai bước độc lập) |
| `worldBossIds` | `{BossID.BLACK_GOKU}` (−203) |

#### Hình dạng

| # | Tên | Máu | Sát thương | % giảm ST | Máu hiệu dụng |
|---|---|---|---|---|---|
| 0 | Black Goku | 3.100.000 | 28.500 | **15%** | ≈ 3.647.059 |
| 1 | Super Black Goku | 3.600.000 | 28.500 | **20%** | 4.500.000 |

#### Giáp
**0**.

#### Kỹ năng

| Hình dạng | Id | Tên | Cấp | Hồi chiêu |
|---|---|---|---|---|
| 0 | 1 | Chiêu Kamejoko | 7 | 100 ms |
| 0 | 19 | Khiên năng lượng | 7 | 300.000 ms |
| 0 | 4 | Chiêu đấm Galick | 7 | 100 ms |
| 1 | 6 | Thái Dương Hạ San | 7 | 30.000 ms |
| 1 | 19 | Khiên năng lượng | 7 | 300.000 ms |
| 1 | 1 | Chiêu Kamejoko | 7 | 100 ms |
| 1 | 4 | Chiêu đấm Galick | 7 | 100 ms |

#### Map xuất hiện (9 map, cả hai hình dạng)

| Map | Tên |
|---|---|
| 102 | Nhà Bunma |
| 92 | Thành phố phía đông |
| 93 | Thành phố phía nam |
| 94 | Đảo Balê |
| 96 | Cao nguyên |
| 97 | Thành phố phía bắc |
| 98 | Ngọn núi phía bắc |
| 99 | Thung lũng phía bắc |
| 100 | Thị trấn Ginder |

3 bản song song, mỗi bản tự bốc một khu ngẫu nhiên trong 90 khu.

#### Thời gian
Nghỉ ngẫu nhiên 15–30 phút.

#### Rơi đồ
**Không rơi gì.**

#### Cơ chế đặc biệt
Bỏ **Tái tạo năng lượng** so với bản gốc, và không có `dame/2` ở hình dạng 2.
Lưu ý: boss này **vẫn có Khiên năng lượng cấp 7** — khiên chặn mọi đòn xuống còn 1 sát thương trừ khi đòn đó lớn hơn `hpMax` (phá khiên).

#### Lời thoại
*"Ta là Sôn Gô Ku" / "Mau chấp nhận số phận đi lũ sâu bọ"* → *"Các ngươi chỉ có vậy thôi sao?"* →
*"Biến hình! Super Saiyan Rose"* → *"Sức mạnh của ta là không có giới hạn"* →
*"Chúng ta sẽ gặp lại nhau sớm thôi"*.

---

### A7. Baby (bản nhiệm vụ) — id −2104

| Mục | Giá trị |
|---|---|
| Lớp | `boss/quest/BabyNhiemVu.java` |
| Nhóm | Bản nhiệm vụ của boss thế giới **−925 Baby** |
| Dùng ở | **NV 39 bước 6** (`TASK_39_6`) — hạ **bất kỳ hình dạng nào** đều tính |
| `worldBossIds` | `{BossID.BABY}` (−925) |

#### Hình dạng

| # | Tên | Máu | Sát thương | % giảm ST | Máu hiệu dụng |
|---|---|---|---|---|---|
| 0 | Baby | 1.900.000 | 30.000 | **18%** | ≈ 2.317.073 |
| 1 | Baby | 1.900.000 | 30.000 | **22%** | ≈ 2.435.897 |
| 2 | Baby | 1.900.000 | 30.000 | **25%** | ≈ 2.533.333 |

#### Giáp
**0**.

#### Kỹ năng

| Hình dạng | Id | Tên | Cấp | Hồi chiêu |
|---|---|---|---|---|
| 0 | 4 | Chiêu đấm Galick | 7 | 100 ms |
| 0 | 3 | Chiêu Masenko | 7 | 100 ms |
| 0 | 1 | Chiêu Kamejoko | 7 | 100 ms |
| 0 | 17 | Liên hoàn | 7 | 10.000 ms |
| 1 | 4 | Chiêu đấm Galick | 7 | 100 ms |
| 1 | 5 | Chiêu Antomic | 7 | 100 ms |
| 1 | 1 | Chiêu Kamejoko | 7 | 100 ms |
| 1 | 17 | Liên hoàn | 7 | 10.000 ms |
| 2 | 4 | Chiêu đấm Galick | 7 | 100 ms |
| 2 | 2 | Chiêu đấm Demon | 7 | 100 ms |
| 2 | 1 | Chiêu Kamejoko | 7 | 100 ms |
| 2 | 24 | Super Kamejoko | 7 | 20.000 ms |

#### Map xuất hiện
Map **14 — Làng Kakarot**. 3 bản song song.

#### Thời gian
Nghỉ ngẫu nhiên 15–30 phút.

#### Rơi đồ
**Không rơi gì.** Bản gốc −925 giữ nguyên 1% cải trang Baby.

#### Cơ chế đặc biệt
Bỏ hệ số `dame × 0,7 / 2` của bản gốc (bản gốc vì thế có ~17,1 tỉ máu hiệu dụng).

#### Lời thoại
*"Hôm nay sẽ là ngày đáng nhớ đây!"* → *"Ngươi không thể thắng nổi ta! Từ bỏ đi!!"* →
*"Ta muốn thử xem sức mạnh này đến đâu..."* → *"Đây là dạng hoàn hảo của ta"* →
*"Cơ thể hoàn hảo của ta!!"*.

---

### A8. Cumber (bản nhiệm vụ) — id −2105

| Mục | Giá trị |
|---|---|
| Lớp | `boss/quest/CumberNhiemVu.java` |
| Nhóm | Bản nhiệm vụ của boss thế giới **−203999 Cumber** |
| Dùng ở | **NV 42**: hạ hình dạng 0 → `TASK_42_3`; hạ hình dạng 1 → `TASK_42_4` |
| `worldBossIds` | `{BossID.CUMBER}` (−203999) |

#### Hình dạng

| # | Tên | Máu | Sát thương | % giảm ST | Máu hiệu dụng |
|---|---|---|---|---|---|
| 0 | Cumber | 3.700.000 | 37.200 | **28%** | ≈ 5.138.889 |
| 1 | Super Cumber | 4.400.000 | 37.200 | **32%** | ≈ 6.470.588 |

#### Giáp
**0**.

#### Kỹ năng

| Hình dạng | Id | Tên | Cấp | Hồi chiêu |
|---|---|---|---|---|
| 0 | 1 | Chiêu Kamejoko | 7 | 100 ms |
| 0 | 19 | Khiên năng lượng | 7 | 300.000 ms |
| 0 | 4 | Chiêu đấm Galick | 7 | 100 ms |
| 1 | 6 | Thái Dương Hạ San | 7 | 300.000 ms |
| 1 | 19 | Khiên năng lượng | 7 | 300.000 ms |
| 1 | 1 | Chiêu Kamejoko | 7 | 100 ms |
| 1 | 4 | Chiêu đấm Galick | 7 | 100 ms |

#### Map xuất hiện
Map **155 — Hành tinh ngục tù** (chung map với bản gốc, **không bao giờ chung khu**). 3 bản song song.

#### Thời gian
Nghỉ ngẫu nhiên 15–30 phút.

#### Rơi đồ
**Không rơi gì.**

#### Cơ chế đặc biệt
Đây là boss nhiệm vụ **dai nhất** trong 8 con (28–32%). Cùng map 155 với Heart hình dạng 4 (nhánh NV 50) — hai boss khác id nên vẫn có thể ở chung khu.

#### Lời thoại
*"Heart nuôi ta bằng ký ức của tù nhân"* → *"Các ngươi chỉ có vậy thôi sao?"* →
*"Biến hình! Super Saiyan SSJ"* → *"Sức mạnh của ta là không có giới hạn"* →
*"Chúng ta sẽ gặp lại nhau sớm thôi"*.

---

### A9. HEART — id −108108 (boss kết truyện, 4 hình dạng)

| Mục | Giá trị |
|---|---|
| Lớp | `boss/heart/Heart.java` (kế thừa `QuestBoss`) |
| Nhóm | Boss kết truyện tuyến nhiệm vụ mới, **không có bản gốc**, `worldBossIds` rỗng |
| Số bản | 3 (`BossManager.loadBoss()`) |
| Tạo hình | Mượn `npc_template` 108 — outfit 2109 / 2110 / 2111 cho **cả 4 hình dạng** |

#### Hình dạng (đầy đủ)

| # | Tên | Máu danh nghĩa | Sát thương | % giảm ST | **Máu hiệu dụng** | Map | Dùng ở |
|---|---|---|---|---|---|---|---|
| 0 | Heart | 1.500.000.000 | 150.000 | **20%** | 1.875.000.000 | 166 | NV 46 bước 1 (`TASK_46_1`) |
| 1 | Heart Hư Không | 2.000.000.000 | 250.000 | **35%** | ≈ 3.076.923.077 | 145 | NV 46 bước 3 (`TASK_46_3`) |
| 2 | Heart Toàn Ký | 2.000.000.000 | 400.000 | **50%** | 4.000.000.000 | 145 | NV 46 bước 4 (`TASK_46_4`) |
| 3 | Hư Không Vô Danh | 2.000.000.000 | 500.000 | **65%** | ≈ 5.714.285.714 | 145 **hoặc** 155 | NV 47 bước 5 (`TASK_47_5`) nếu ở map 145; NV 50 bước 5 (`TASK_50_5`) nếu ở map **155** |
| | **Tổng** | **7.500.000.000** | | | **≈ 14.666.208.791** | | |

> Ba hình dạng cuối đều là **2.000.000.000** — sát trần `int` 2.147.483.647.
> Đây chính là lý do `BossDamageReduce` ra đời: không thể tăng máu nữa, chỉ có thể
> tăng % giảm sát thương.

#### Giáp
**0**.

#### Kỹ năng

**Hình dạng 0 — Heart**

| Id | Tên | Cấp | Hồi chiêu |
|---|---|---|---|
| 22 | Thôi miên | 7 | 60.000 ms |
| 23 | Trói | 7 | 90.000 ms |
| 19 | Khiên năng lượng | 7 | 300.000 ms |
| 3 | Chiêu Masenko | 7 | 100 ms |
| 4 | Chiêu đấm Galick | 7 | 100 ms |

**Hình dạng 1 — Heart Hư Không**

| Id | Tên | Cấp | Hồi chiêu |
|---|---|---|---|
| 6 | Thái Dương Hạ San | 7 | 30.000 ms |
| 8 | Tái tạo năng lượng | 7 | 300.000 ms |
| 19 | Khiên năng lượng | 7 | 300.000 ms |
| 20 | Dịch chuyển tức thời | 7 | 20.000 ms |
| 1 | Chiêu Kamejoko | 7 | 100 ms |
| 4 | Chiêu đấm Galick | 7 | 100 ms |

**Hình dạng 2 — Heart Toàn Ký**

| Id | Tên | Cấp | Hồi chiêu |
|---|---|---|---|
| 6 | Thái Dương Hạ San | 7 | 30.000 ms |
| 8 | Tái tạo năng lượng | 7 | 200.000 ms |
| 19 | Khiên năng lượng | 7 | 200.000 ms |
| 17 | Liên hoàn | 7 | 10.000 ms |
| 24 | Super Kamejoko | 7 | 10.000 ms |
| 5 | Chiêu Antomic | 7 | 100 ms |
| 4 | Chiêu đấm Galick | 7 | 100 ms |

**Hình dạng 3 — Hư Không Vô Danh**

| Id | Tên | Cấp | Hồi chiêu |
|---|---|---|---|
| 6 | Thái Dương Hạ San | 7 | 25.000 ms |
| 8 | Tái tạo năng lượng | 7 | 200.000 ms |
| 19 | Khiên năng lượng | 7 | 200.000 ms |
| 22 | Thôi miên | 7 | 60.000 ms |
| 17 | Liên hoàn | 7 | 10.000 ms |
| 24 | Super Kamejoko | 7 | 8.000 ms |
| 4 | Chiêu đấm Galick | 7 | 100 ms |

#### Map xuất hiện

| Map | Tên | Hình dạng |
|---|---|---|
| 166 | Phòng thí nghiệm Myuu | 0 |
| 145 | Võ Đài Siêu Cấp | 1, 2, 3 |
| 155 | Hành tinh ngục tù | 3 (nhánh NV 50) |

3 bản song song.

#### Thời gian
- Giữa các hình dạng: nối tiếp ngay (`AppearType.ANOTHER_LEVEL`), **không nghỉ**.
- Sau khi hạ hình dạng cuối: nghỉ **ngẫu nhiên 15–30 phút**, rồi hiện lại từ hình dạng 0 ở khu ngẫu nhiên map 166.
- `REST_10_M` ghi trong `BossesData.HEART` **không được dùng**.

#### Rơi đồ

| Hình dạng | Vật phẩm | Số lượng | Tỉ lệ | Ai nhận |
|---|---|---|---|---|
| 0, 1, 3 | — | — | — | — |
| **2 (Heart Toàn Ký)** | **id 2029 — Ống nghiệm Myuu** | 1 | **100%** | Người **kết liễu** (`ItemMap(..., plKill.id)`), kèm `ItemOption(30,0)` = không giao dịch được |

> Không vàng, không trang bị, không Ngọc Rồng — giống mọi `QuestBoss`.

#### Cơ chế đặc biệt

1. **Đổi map giữa các hình dạng: 166 → 145.** `QuestBoss.joinMap()` thấy khu hiện tại
   không nằm trong `mapJoin` của hình dạng mới thì **rời map ngay** (`exitMap`) và **giữ
   nguyên trạng thái `JOIN_MAP`**, thử lại mỗi 3 giây — đủ thời gian cho người chơi đi
   từ 166 sang 145 (NV 46 bước 2). Quá **10 phút** không tìm được khu thì bỏ lượt.
2. **Hình dạng 3 có 2 map (`{145, 155}`).** Map thật do người chơi quyết định; `TaskService`
   phân biệt bằng `mapId == 155` → `TASK_50_5`, ngược lại → `TASK_47_5`.
3. **Không thông báo toàn server** khi xuất hiện/chết (kế thừa `isNotifyDisabled` của `QuestBoss`).
4. Ba hình dạng cuối đều có **Tái tạo năng lượng cấp 7** và **Khiên năng lượng cấp 7**.

#### Lời thoại (tóm tắt)

- **Heart**: *"Ta không ghét ai cả. Ta chỉ thấy các ngươi đau, và ta biết chỗ cơn đau nằm.
  Đưa sáu mảnh đó cho ta, rồi ngươi sẽ ngủ ngon."* — có đối thoại với **Tiến sĩ Myuu**
  (*"Đừng nghe hắn! Hắn nói với ta y hệt vậy!"*). Chết: *"Lên Võ Đài Siêu Cấp đi. Ta đợi ở đó."*
- **Heart Hư Không**: *"Đây mới là ta. Phần còn lại chỉ là bộ đồ."* — tranh luận đạo đức với
  một **thiên sứ** (*"Ai trong hai ta đang hủy diệt ít hơn?"*). Chết: *"Chưa xong đâu. Mảnh thứ bảy vẫn trong ngực ta."*
- **Heart Toàn Ký**: *"Ta đã xóa hai vạn hành tinh. Không hành tinh nào khóc cả — vì không ai
  còn nhớ để mà khóc."* Chết: *"Ngươi không giết ta. Ngươi chỉ mở cái hũ ra. Giờ thì tới lượt ngươi chọn..."*
- **Hư Không Vô Danh**: *"Ta là phần bị bỏ lại. Không tên, không chủ. Ngươi trả ta cho ai đây?"*
  Chết: *"Được rồi... ta đi đây. Nhớ giùm ta nhé."*

---

## PHẦN B — BOSS PHÓ BẢN

### B1. Phó bản Mabư 12h (map 114 → 120)

| Mục | Giá trị |
|---|---|
| Tệp | `services_dungeon/MajinBuuService.java`, `map/Map.java:initBoss()` |
| Khung giờ | **12:00 – 12:59** giờ Việt Nam (`TimeUtil.isMabuOpen()`) |
| Số bản | `AVAILABLE = 13`; boss sinh **1 con mỗi khu** — 10 khu/map → **10 bản mỗi loại** |
| Ra khỏi giờ | Ngoài khung giờ: `goHome` đếm ngược 30 giây rồi đưa về phi thuyền |
| Cơ chế bùa | NPC Babiđây (tempId 46) thôi miên 1% mỗi tick (cờ 9 → 10); NPC Ôsin (tempId 44) giải bùa 2% (cờ 10 → 9) |
| Giới hạn lượt | Không có giới hạn lượt/ngày trong code |

**Lối đi**: 114 Cổng phi thuyền → 115 Phòng chờ → (116) → 117 Cửa Ải 1 → 118 Cửa Ải 2 →
119 Cửa Ải 3 → 120 Phòng chỉ huy (`Map.mapIdNextMabu`).

#### Bảng rơi đồ **dùng chung cho cả 9 boss 12h**

Mọi lớp trong `boss/MajinBuu_12h/` có `reward()` giống hệt nhau:

| Thứ tự | Vật phẩm | Số lượng | Tỉ lệ | Ai nhận |
|---|---|---|---|---|
| 1 | **+5 Điểm sự kiện** (`event.addEventPoint(5)`) | 5 | 100% | Người kết liễu |
| 2 | Đồ Thần Linh ngẫu nhiên (`randDoTLBoss`) | 1 | **1%** | Người kết liễu |
| 3 | **id 190 — Vàng** | **20.000 – 30.000** | **100%** | Người kết liễu |
| 4 | Trang bị: 70% nhóm Áo/Quần/Giày `{230,231,232,234,…,276}`, 30% nhóm Găng/Rada `{254,…,280}` — kèm chỉ số shop ×(100–115)% và sao pha lê (80% → 1–3 sao, 17% → 4–5 sao, 3% → 6 sao) | 1 | **1%** | Người kết liễu |
| 5 | Ngọc Rồng `{15,16,17,18,19,20}` = NR 2→7 sao | 1–3 | **10%** | Người kết liễu |
| 6 | `fightMabu.changePoint(10)` (điểm đua Mabư) | +10 | 100% | Người kết liễu |

> Chú thích trong code ghi "30% xác suất để rơi đồ" và "80% xác suất rơi ngọc rồng"
> **không khớp** với `Util.isTrue(1,100)` / `Util.isTrue(10,100)` thực tế = **1%** và **10%**.
> Tin theo code.

#### B1.1. Drabura — id −233

| Mục | Giá trị |
|---|---|
| Map | **114 — Cổng phi thuyền**, 1 con/khu |
| Máu | 20.000.000 · Sát thương 10.000 · **Giảm ST 0%** · Giáp 0 |
| Kỹ năng | 4 Chiêu đấm Galick cấp 7, hồi 1.000 ms |
| Nghỉ | `REST_1_M` = 60 giây |
| Cơ chế | Né 10%; **trần 20.000.000 sát thương mỗi đòn** |
| Nhiệm vụ | NV 36 bước 2 — hạ Drabura cộng **trọn gói 20** (`addDoneSubTask(20)`) |
| Thoại | Chết: *"Đừng vội mừng, ta sẽ hồi sinh và thịt hết bọn mi"* |

#### B1.2. Bui Bui — id −234

| Mục | Giá trị |
|---|---|
| Map | **115 — Phòng chờ** |
| Máu | 40.000.000 · Sát thương 200.000 · 0% · Giáp 0 |
| Kỹ năng | 4 Chiêu đấm Galick cấp 7, hồi 10.000 ms |
| Nghỉ | 60 giây |
| Cơ chế | Né 20% (`Util.isTrue(200,1000)`); không trần sát thương |
| Thoại | *"Hãy xem đây nhóc"* |

#### B1.3. Bui Bui (bản 2) — id −238

| Mục | Giá trị |
|---|---|
| Map | **117 — Cửa Ải 1** |
| Máu | 40.000.000 · Sát thương 200.000 · 0% · Giáp 0 |
| Kỹ năng | 4 Chiêu đấm Galick cấp 7, hồi 10.000 ms |
| Nghỉ | 60 giây |
| Cơ chế | Né 20% |
| Thoại | *"Trọng lực bây giờ đã tăng gấp 10 lần / Đó là điều kiện không gian lý tưởng của ta / Nhưng lại rất bất lợi cho bọn mi"* |

#### B1.4. Ya côn — id −235

| Mục | Giá trị |
|---|---|
| Map | **118 — Cửa Ải 2** |
| Máu | 50.000.000 · Sát thương 200.000 · 0% · Giáp 0 |
| Kỹ năng | 4 Chiêu đấm Galick cấp 7, hồi 100 ms |
| Nghỉ | 60 giây |
| Cơ chế | Né 20%; **tàng hình/dịch chuyển** (`setPos2` mỗi 10 giây) và chọc ghẹo người chơi |
| Thoại | Không có thoại trong `BossData` |

#### B1.5. Drabura (bản 2) — id −237

| Mục | Giá trị |
|---|---|
| Map | **119 — Cửa Ải 3** |
| Máu | 20.000.000 · Sát thương 200.000 · 0% · Giáp 0 |
| Kỹ năng | 4 Chiêu đấm Galick cấp 7, hồi 1.000 ms |
| Nghỉ | `REST_5_M` = 300 giây |
| Cơ chế | Né 20%; **trần 20 triệu/đòn**; sát thương bị kẹp về đúng máu còn lại (không "overkill"); **gọi kèm Gôku (−341) và Ca Đít (−342)** qua `bossesAppearTogether` |
| Thoại | *"Ta đã trở lại, lợi hại gấp hai, hahaha"* → chết: *"Hêhê..ta chẳng cần tốn sức đánh với các ngươi nữa / Mà ta sẽ để cho các ngươi tự thanh toán lẫn nhau, xin chào"* + hai câu của NPC đồng hành |

#### B1.6. Gôku — id −341 (boss gọi kèm)

| Mục | Giá trị |
|---|---|
| Map | **119 — Cửa Ải 3** · `AppearType.CALL_BY_ANOTHER` (chỉ hiện khi Drabura 2 gọi) |
| Máu | 60.000.000 · Sát thương 1.000 · 0% · Giáp 0 |
| Kỹ năng | 4 Galick cấp 7 (1.000 ms); 1 Kamejoko cấp 7 (1.000 ms); 8 Tái tạo năng lượng cấp 7 (1.000.000 ms); 6 Thái Dương Hạ San cấp 1 (60.000 ms) |
| Cơ chế | Né theo `tlNeDon`; **trần 20 triệu/đòn** |
| Thoại | *"Tỉnh lại đi Cađíc!"*, *"Cađíc! dừng tay lại! Cậu điên mất rồi!"*, xen kẽ vài câu đùa (*"Đừng lùa gà nữa"*, *"Một Server là quá đủ rồi!"*) |

#### B1.7. Ca Đít — id −342 (boss gọi kèm)

| Mục | Giá trị |
|---|---|
| Map | **119 — Cửa Ải 3** · `CALL_BY_ANOTHER` |
| Máu | 60.000.000 · Sát thương 1.000 · 0% · Giáp 0 |
| Kỹ năng | 4 Galick cấp 7 (1.000 ms); 5 Antomic cấp 7 (1.000 ms); 8 Tái tạo năng lượng cấp 7 (1.000.000 ms) |
| Cơ chế | Né theo `tlNeDon`; **trần 10 triệu/đòn** |
| Thoại | *"Chúng ta sẽ 1 mất 1 còn!"*, *"Kakalốt! Ta chờ đợi giây phút này đã từ lâu!"*, *"Từ giờ hãy gọi ta là CađícMẹc"* |

#### B1.8. Mabư — id −236 (boss cuối 12h)

| Mục | Giá trị |
|---|---|
| Map | **120 — Phòng chỉ huy** |
| Máu | 100.000.000 · Sát thương 10.000 · 0% · Giáp 0 |
| Kỹ năng | 8 Tái tạo năng lượng cấp 3 (1.200.000 ms); 4 Chiêu đấm Galick cấp 7 (1.000 ms) |
| Nghỉ | 60 giây |
| Cơ chế | Né 20%; **trần 50.000.000 ± 10.000 sát thương/đòn**; **gọi kèm Drabura 3 (−343)** |
| Nhiệm vụ | **NV 37 bước 1** (`TASK_37_1`) |
| Thoại | *"Bư! Bư! Bư!"*, *"Oe Oe"* |

#### B1.9. Drabura (bản 3) — id −343 (boss gọi kèm Mabư)

| Mục | Giá trị |
|---|---|
| Map | **114 — Cổng phi thuyền** (`mapJoin`) · `CALL_BY_ANOTHER` |
| Máu | 20.000.000 · Sát thương 100.000 · 0% · Giáp 0 |
| Kỹ năng | 4 Galick cấp 7 (1.000 ms); 8 Tái tạo năng lượng cấp 7 (10.000.000 ms) |
| Cơ chế | Né theo `tlNeDon`; **trần 20 triệu/đòn** |
| Nhiệm vụ | **NV 37 bước 2** — hạ Drabura 3 cộng **trọn gói 30** |
| Thoại | Chết: *"Đừng vội mừng, ta sẽ hồi sinh và thịt hết bọn mi"* |

---

### B2. Phó bản Mabư 14h (map 127 / 128)

| Mục | Giá trị |
|---|---|
| Tệp | `map/phoban/MajinBuu14H.java`, `services_dungeon/MajinBuu14HService.java` |
| Khung giờ | **14:00 – 14:59** giờ Việt Nam (`TimeUtil.isMabu14HOpen()`) |
| Số bản | `AVAILABLE = 7` instance; boss sinh **1 con mỗi khu** map 127 và map 128 |
| Vào phó bản | `joinMaBu2H` — ưu tiên khu map 127 còn **dưới 5 người**; hết thì vào khu ngẫu nhiên map 127 |
| Ổ giữ người trong bụng | Mỗi khu map 128 có **4 slot `MaBuHold`** |
| Hết giờ | Toàn bộ người chơi (trừ admin) bị đẩy về phi thuyền |
| Giới hạn lượt | Không có giới hạn lượt/ngày trong code |

#### B2.1. Mabư 14h — id −214 (5 hình dạng)

| # | Tên | Máu | Sát thương | % giảm ST | Máu hiệu dụng | Map |
|---|---|---|---|---|---|---|
| 0 | Mabư mập | 50.000.000 | 500.000 | 0% | 50.000.000 | 127 |
| 1 | Super Bư | 60.000.000 | 500.000 | 0% | 60.000.000 | 127, 128 |
| 2 | Bư Tênk | 80.000.000 | 500.000 | 0% | 80.000.000 | 127 |
| 3 | Bư Han | 100.000.000 | 500.000 | 0% | 100.000.000 | 127 |
| 4 | Kid Bư | 150.000.000 | 500.000 | 0% | 150.000.000 | 127 |
| | **Tổng** | **440.000.000** | | | **440.000.000** | |

**Giáp**: 0.

**Kỹ năng** (giống nhau cả 5 hình dạng): 1 Chiêu Kamejoko cấp 3 (5.000 ms); 0 Chiêu đấm Dragon cấp 7 (1.000 ms).

**Thời gian**: `REST_10_M` = 600 giây nghỉ giữa các lượt (dùng `Boss.rest()` bình thường — **không** phải `QuestBoss`).

**Cơ chế đặc biệt** (`boss/MajinBuu_14h/Mabu2H.java`):
- **Né 10%** mọi đòn.
- **Trần 30.000.000 ± 10.000 sát thương mỗi đòn.**
- **Ăn người chơi**: mỗi **10 giây**, mỗi người trong khu có **20% (1/5)** bị nuốt vào **map 128 — Bụng Mabư** (`isMabuHold = true`).
- **Hoá sôcôla** (chỉ hình dạng 0): mỗi 10 giây, mỗi người trong khu có **20%** bị `setSocola` **30 giây** (kèm hiệu ứng 4133).
- **Hình dạng 5 (Kid Bư) CHỈ chết bằng `Skill.QUA_CAU_KENH_KHI`** — mọi chiêu khác bị kẹp `damage = 0` nếu đòn đó đủ để giết.
- Khi chết: giải phóng toàn bộ người bị nuốt (đưa từ map 128 về map 127), và **kéo theo Super Bư (−348) cùng khu chết luôn**.
- Có `ServerNotify` toàn server khi chết.

**Rơi đồ**: `reward()` chỉ trao **+5 Điểm sự kiện** và gọi `TaskService.checkDoneTaskKillBoss`. **Không rơi vật phẩm nào.**

**Nhiệm vụ**: **NV 37 bước 1** — chỉ khi hạ **hình dạng 5** (`currentLevel == 4`).

**Thoại**: *"Măm măm"* (khi ăn người), *"Úm ba la xì bùa"* (khi hoá sôcôla), *"Xí hụt"* (né), *"Khí công pháo"*, *"Biến hình"*.

#### B2.2. Super Bư (trong bụng) — id −348

| Mục | Giá trị |
|---|---|
| Lớp | `boss/MajinBuu_14h/SuperBu.java`, dữ liệu `SUPER_BU_BUNG` |
| Map | **128 — Bụng Mabư**, 1 con/khu |
| Máu | 50.000.000 · Sát thương 500.000 · **0%** · Giáp 0 |
| Kỹ năng | 1 Chiêu Kamejoko cấp 3 (5.000 ms); 0 Chiêu đấm Dragon cấp 7 (1.000 ms) |
| Nghỉ | `REST_10_S` = 10 giây |
| Cơ chế | Né 10%; trần 30 triệu/đòn; **mỗi đòn đánh vào Super Bư được chuyển tiếp nguyên vẹn sang Mabư (−214) cùng khu** (`boss.injured(...)` ở đầu hàm) — đây là cách người bị nuốt vẫn góp sát thương cho trận ngoài |
| Rơi đồ | **+5 Điểm sự kiện**, không vật phẩm |
| Nhiệm vụ | **NV 37 bước 2** — hạ Super Bư cộng **trọn gói 30** |
| Thoại | *"Khí công pháo"* |

---

### B3. Phó bản Con Đường Rắn Độc (map 143 → 144)

| Mục | Giá trị |
|---|---|
| Tệp | `map/phoban/SnakeWay.java`, `services_dungeon/SnakeWayService.java` |
| Điều kiện vào | Phải có **bang hội**; đã vào bang **≥ 2 ngày**; **sức mạnh ≥ 2.000.000.000** (`POWER_CAN_GO_TO_CDRD`); cấp phó bản **1–110** |
| Giới hạn lượt | **Mỗi người 1 lần / 7 ngày** (`lastTimeJoinCDRD`) |
| Số bản | `AVAILABLE = 50` instance song song |
| Thời lượng | **30 phút** (`TIME_CON_DUONG_RAN_DOC = 1.800.000 ms`); trước khi hết 1 phút thì đếm ngược đuổi ra |
| Map | 143 Con đường rắn độc (vào) → **144 Hoang mạc** (nơi có boss) |
| Tính hoàn thành | **Chỉ khi hạ được Cađích** (`endCDRD`) mới ghi nhận `checkDoneTaskDungeonForZone(MAP_CON_DUONG_RAN_DOC)`; hết giờ **không tính** |

#### Công thức chỉ số boss (theo cấp phó bản `level` 1–110)

```
bossDamage    = 200.000 × level      (kẹp trần 200.000.000)
bossMaxHealth = 2.000.000 × level    (kẹp trần 2.000.000.000)

Saibamen (6 con) : dame = 10.000 + bossDamage       hp = 500.000 + bossMaxHealth
sau đó bossDamage ×= 5,  bossMaxHealth ×= 5   (kẹp trần lại)
Nađíc            : dame = 10.000 + bossDamage       hp = 500.000 + bossMaxHealth
sau đó bossDamage ×= 10, bossMaxHealth ×= 10  (kẹp trần lại)
Cađích           : dame = 10.000 + bossDamage       hp = 500.000 + bossMaxHealth
```

Ví dụ cấp 50: Saibamen 10 tr dame / 100,5 tr máu; Nađíc 50 tr / 500,5 tr; Cađích 200 tr (chạm trần) / 2,0005 tỉ (chạm trần).

#### B3.1. Saibamen "Số 1"…"Số 6" — id −18 … −23

| Mục | Giá trị |
|---|---|
| Lớp | `boss_con_duong_ran_doc/SAIBAMEN.java`; id = `BossID.SAIBAMEN (−17) − i`, i = 6…1 → **−23 … −18** |
| Số con | **6 con cùng lúc** trong 1 khu map 144 |
| Máu / sát thương | Xem công thức trên · **Giảm ST (bảng) 0%** · Giáp 0 |
| Kỹ năng | **Chiêu đấm Galick cấp 1 → 7** (7 chiêu, dùng hồi chiêu mặc định của `skill_template`) |
| Vị trí | `x = 420 + idboss × 15`, `y = 342` |
| Thứ tự đánh | Con "Số 1" vào trận trước (trễ 1,5 giây); các con còn lại vào khi `9 − số boss còn sống == idboss` |
| **Cơ chế nhận sát thương** | **`damage / 7`** rồi mới trừ giáp; khiên năng lượng thì chia thêm 4 |
| **Cơ chế chết** | **Tự nổ**: choáng người kết liễu 3,5 giây, chờ 2,5 giây rồi gây **`hpMax × 100`** sát thương lên **mọi người trong khu** |
| Rơi đồ | **id 19 — Ngọc Rồng 6 sao**, số lượng 1, tỉ lệ **100%**, người kết liễu |
| Thoại | Không có thoại cố định; khi nổ: *"He he he"*, và mọi người nhận thông báo *"<tên> coi chừng đấy!"* |

#### B3.2. Nađíc — id −16

| Mục | Giá trị |
|---|---|
| Lớp | `boss_con_duong_ran_doc/NADIC.java` |
| Map | **144 — Hoang mạc**, vị trí `x=470, y=312` |
| Máu / sát thương | Công thức trên (×5 so với Saibamen) · 0% · Giáp 0 |
| Kỹ năng | 4 Chiêu đấm Galick cấp 7 (1.000 ms); 8 Tái tạo năng lượng cấp 5 (10.000 ms) |
| Kích hoạt | Khi số boss còn sống **< 3** |
| Cơ chế | Dùng `Boss.injured` mặc định (không chia sát thương). Khi chết, **đẩy Cađích di chuyển** về phía mình |
| Rơi đồ | **id 19 — Ngọc Rồng 6 sao**, 1 cái, **100%**, người kết liễu |
| Thoại | *"Ốp la...Xay da da!"* (khi tái tạo); chết: *"Sếp hãy giết nó, trả thù cho em!"* |

#### B3.3. Cađích — id −15 (boss cuối)

| Mục | Giá trị |
|---|---|
| Lớp | `boss_con_duong_ran_doc/CADICH.java` |
| Map | **144 — Hoang mạc**, vị trí `x=490, y=312` |
| Máu / sát thương | Công thức trên (×10 so với Nađíc) · **Giảm ST (bảng) 0%** · Giáp 0 |
| Kỹ năng | 4 Galick cấp 7 (1.000 ms); 3 Masenko cấp 1 (1.000 ms); 5 Antomic cấp 1 (1.000 ms); 1 Kamejoko cấp 4 (1.000 ms); 13 Biến hình cấp 1 (1.000 ms) |
| Kích hoạt | Khi số boss còn sống **< 2** |
| **Cơ chế đặc biệt** | ① Né theo `tlNeDon`. ② Khiên năng lượng → `damage / 4`. ③ **Dưới 50% máu → gồng Biến Khỉ 2 giây**, trong lúc gồng **miễn nhiễm hoàn toàn** (`return 0`), sau đó **máu tối đa ×2** (kẹp trần 2.000.000.000) và **hồi đầy máu**. ④ Mỗi 10 giây có 5% tung đòn **choáng 5 giây** kèm thoại. |
| Rơi đồ (50%) | **id 459 — Phiếu giảm giá** ×1 với các chỉ số `112=80`, `93=90` (HSD), `20=nextInt(10000)`; **và** **id 706 — Bí ngô 5 sao** ×1. Cùng một lần tung 50% — trúng thì rơi cả hai. Người kết liễu nhận |
| Kết thúc | Hạ Cađích → `endCDRD = true` → ghi nhận phá xong phó bản cho **mọi người trong instance** |
| Thoại | *"Vĩnh biệt chú mày nhé, Na đíc"*; *"Ha ha ha, ha ha ha"* (bắt đầu biến khỉ); *"Thế nào <tên>? Mi đã thấy phép biến hình của người Xayda rồi chứ?"*; *"Tuyệt chiêu hủy diệt của môn phái Xayda"*; rời map: *"Tốt lắm phi thuyền đã đến đón ta"* |

---

### B4. Phó bản Khí Gas Hủy Diệt (Destron Gas)

| Mục | Giá trị |
|---|---|
| Tệp | `map/phoban/DestronGas.java`, `services_dungeon/DestronGasService.java` |
| Điều kiện vào | Có bang hội; vào bang **≥ 2 ngày**; **chỉ bang chủ** mới mở được; cấp **1–110**; sức mạnh ≥ 2.000.000.000 (`POWER_CAN_GO_TO_KHI_GAS_HUY_DIET`) |
| Giới hạn lượt | **3 lượt/ngày cho mỗi bang** (`timesPerDayKGHD`, reset sau nửa đêm) |
| Số bản | `AVAILABLE = 50` |
| Thời lượng | **30 phút**; còn 1 phút thì báo *"Nơi này sắp nổ tung mau chạy đi"* |
| Map | 147 Sa mạc, 149 Thành phố Santa, 151 Hành tinh bóng tối, 152 Vùng đất băng giá, **148 Lâu đài Lychee** (nơi 2 boss) |
| Điều kiện gọi boss | **Toàn bộ quái trong mọi map phải chết** thì Dr Lychee mới xuất hiện ở map 148 |
| Tính hoàn thành | **Chỉ khi hạ Hatchiyack** (`hatchiyatchDead`) |
| Thành tích | Hoàn thành cấp **≥ 70** thì `destronGas70CompletionCount++` cho từng người |

#### B4.1. Dr Lychee — id −208

| Mục | Giá trị |
|---|---|
| Lớp | `boss/khi_gas/DrLychee.java` |
| Map | **148 — Lâu đài Lychee**, vị trí `x=480, y=295` (rồi đi tới 480,480) |
| Máu | `1.000.000 + min(15.000.000 × level, 2.000.000.000)` |
| Sát thương | `10.000 + min(1.000 × level, 200.000.000)` |
| Giảm ST (bảng `BossDamageReduce`) | **0%** |
| Giáp | 0 |
| Kỹ năng | **Chiêu đấm Demon cấp 1 → 7** (7 chiêu, hồi chiêu mặc định) |
| Nghỉ | 60 giây |
| **Cơ chế nhận sát thương riêng** | ① Né `level/1000` (cấp 110 → 11%). ② Cộng nhiễu `nextInt(−100 × level, 0)` vào sát thương. ③ **Giảm thêm `level/10` %** (cấp 110 → −11%). ④ Khiên năng lượng → `damage = 1` |
| Rơi đồ | **id 738 — Cải trang Dr Lychee**, **1 + 2 × (số người trong khu)** cái, **100%**, `playerId = −1` → **ai nhặt cũng được**. Chỉ số: `50`, `77`, `103` = `ParamMax + nextInt(8,11)`; `94` = `ParamMax + nextInt(0,3)`; `93` (HSD) = `nextInt(3,ParamMax)` kẹp trần 21; `30 = 0` (không giao dịch). `ParamMax = 14` khi cấp ≤ 9, `= 14 + level/10` khi cấp ≤ 110, `= 26` khi cao hơn |
| Nhiệm vụ | **NV 43 bước 2** (`TASK_43_2`) — cộng **trọn gói** một lần |
| Cơ chế đặc biệt | **Khi rời map, tự sinh Hatchiyack** với `dame × 1,5` và `hpMax × 1,5` (kẹp trần) |
| Thoại | *"Ta đợi các ngươi mãi"*, *"Bọn xayda các ngươi mau đền tội đi"*, *"Đại bác báo thù..."*, *"Heyyyyyyyy Yaaaaa"*; chết: *"Các ngươi khá lắm / Hatchiyack sẽ báo thù cho ta"* |

#### B4.2. Hatchiyack — id −207

| Mục | Giá trị |
|---|---|
| Lớp | `boss/khi_gas/Hatchiyack.java` |
| Map | **148 — Lâu đài Lychee** |
| Tên hiển thị | **`" "` (một dấu cách)** — tên rỗng, xem mục Ghi chú |
| Máu | `min(máu Dr Lychee × 1,5, 2.000.000.000)` |
| Sát thương | `min(sát thương Dr Lychee × 1,5, 200.000.000)` |
| Giảm ST (bảng) | **0%** · Giáp 0 |
| Kỹ năng | **Chiêu đấm Demon cấp 1 → 7** |
| Nghỉ | 60 giây |
| **Cơ chế nhận sát thương riêng** | ① Né `(level + 10)/1000` (cấp 110 → 12%). ② Nhiễu `nextInt(−200 × level, 0)`. ③ **Giảm thêm `level/5` %** (cấp 110 → −22%, **gấp đôi Dr Lychee**). ④ Khiên → `damage = 1` |
| Rơi đồ | **id 729 — Cải trang Hatchiyack**, **1 + 2 × (số người trong khu)** cái, **100%**, `playerId = −1` (ai nhặt cũng được). Chỉ số giống Dr Lychee nhưng dòng thứ 4 là `ItemOption(5, ParamMax + nextInt(0,3))` thay vì `94` |
| Nhiệm vụ | **NV 43 bước 3** (`TASK_43_3`) — cộng trọn gói |
| Cơ chế đặc biệt | Khi rời map → `hatchiyatchDead = true` → phó bản tính là **phá xong** |
| Thoại | *"Các ngươi dám hạ sư phụ ta"*, *"Ta sẽ tiêu diệt hết các ngươi"*, *"Đại bác báo thù..."*; chết: *"Các ngươi khó mà rời khỏi nơi đây"* |

---

### B5. Phó bản Doanh Trại Độc Nhãn (Red Ribbon HQ)

| Mục | Giá trị |
|---|---|
| Tệp | `map/phoban/RedRibbonHQ.java`, `services_dungeon/RedRibbonHQService.java` |
| Điều kiện vào | Có bang hội; vào bang **≥ 1 ngày**; bang phải đủ **`N_PLAYER_CLAN = 5`** thành viên (hằng số khai báo nhưng **không thấy kiểm tra trong `joinDoanhTrai`**) |
| Giới hạn lượt | **1 lượt/ngày cho mỗi bang** (`haveGoneDoanhTrai` + `isAfterMidnight`) |
| Số bản | `AVAILABLE = 50` |
| Thời lượng | **30 phút**; sau khi thắng có thêm **5 phút nhặt đồ** (`TIME_PICK_DOANH_TRAI = 300.000 ms`) |
| Map | 53 Tường thành 1, 58 Tường thành 2, **59 Tường thành 3**, **55 Tầng 1**, 56 Tầng 2, **54 Tầng 3**, **57 Tầng 4**, 60/61/**62** Trại độc nhãn 1/2/3 |
| Tính hoàn thành | Toàn bộ quái **và** boss trong mọi map chết → `winDT` → ghi nhận `TASK_21_1` (`MAP_DOANH_TRAI`) cho mọi người trong instance, kèm thông báo *"Mau đi tìm Độc Nhãn"* |

#### Công thức chỉ số (theo tổng chỉ số các thành viên bang **đang online**)

```
totalDamage = Σ nPoint.dame  của membersInGame
totalHp     = Σ nPoint.hpMax của membersInGame

dame = totalHp / 20          hp = totalDamage × 50

Trung uý Trắng   (map 59): dame × 1,00   hp × 1,00
Trung uý Xanh Lơ (map 62): dame × 1,10   hp × 1,10
Trung uý Thép    (map 55): dame × 1,15   hp × 1,15
Ninja Áo Tím     (map 54): dame × 1,20   hp × 1,20
Rôbốt Vệ Sĩ ×4   (map 57): dame × 1,30   hp × 1,30

Trần: dame ≤ 200.000.000 ; hp ≤ 2.000.000.000 (lúc khởi tạo dùng trần 2.147.483.647)
```

Chỉ số được **tính lại** mỗi khi có người vào thêm (`updateHPDame()`), và khi đó boss **hồi đầy máu**.
Riêng phân thân Ninja (id −9 … −14) bị chia 10 ở `updateHPDame`.

#### Bảng rơi đồ chung của phó bản

Tất cả boss doanh trại đều trao **+5 Điểm sự kiện** cho người kết liễu.

| Boss | Vật phẩm | Số lượng | Tỉ lệ |
|---|---|---|---|
| Trung uý Trắng | id 17 — Ngọc Rồng 4 sao | 1 | 50% |
| | **id 1824 — Cậu Vàng** | 1 | **100%** |
| Trung uý Xanh Lơ | id 17 — Ngọc Rồng 4 sao | 1 | 50% |
| | **id 611 — Bản đồ kho báu** | **1–3** | **100%** |
| | id 1824 — Cậu Vàng | 1 | 100% |
| Trung uý Thép | id 17 — Ngọc Rồng 4 sao | 1 | 50% |
| | id 611 — Bản đồ kho báu | 1–2 | 30% |
| | id 1824 — Cậu Vàng | 1 | 100% |
| Ninja Áo Tím | id 17 — Ngọc Rồng 4 sao | 1 | 50% |
| | id 611 — Bản đồ kho báu | 1–2 | 30% |
| | id 1824 — Cậu Vàng | 1 | 100% |
| Ninja Áo Tím (phân thân) | id 17 — Ngọc Rồng 4 sao | 1 | 10% |
| Rôbốt Vệ Sĩ | id 17 — Ngọc Rồng 4 sao | 1 | 30% |
| | id 1824 — Cậu Vàng | 1 | 100% |

Toàn bộ vật phẩm rơi theo `plKill.id` → **chỉ người kết liễu nhặt được**.
Ngoài ra phó bản có hàm `randomNR()` rải Ngọc Rồng ra map (3 viên chắc chắn + 4 viên xác suất 1/2, 1/3, 1/4, 1/5 mỗi khu; 1/500 ra NR 1–5 sao, còn lại NR 5–7 sao) — **ai nhặt cũng được** (`playerId = −1`).

#### B5.1. Trung uý Trắng — id −4

| Mục | Giá trị |
|---|---|
| Map | **59 — Tường thành 3**, vị trí `x=198, y=456` |
| Chỉ số | `dame × 1,00`, `hp × 1,00` (công thức trên) · Giảm ST (bảng) **0%** · Giáp 0 |
| Kỹ năng | 23 chiêu: Demon 3/6, Dragon 7/1, Galick 5, Kamejoko 7→1, Antomic 1→7, Masenko 1/5/6 — hồi chiêu **1…23 ms** (gần như không hồi), cộng Kamejoko 7 hồi 1.000 ms |
| Nghỉ | 60 giây |
| **Cơ chế đặc biệt** | ① Né 20%. ② **`damage / 2`**. ③ Khiên → chia thêm 2. ④ **BẤT TỬ khi Bulon còn sống**: `injured` chỉ chạy nếu `!zone.isbulon1Alive && !zone.isbulon2Alive` — phải diệt hai con Bulon trước. ⑤ Chỉ đánh khi người chơi nằm trong `x ∈ [755, 1060]` |
| Thoại | *"Xem mi dùng cách nào hạ được ta"*, *"Ha ha ha"*, *"Bulon đâu tiêu diệt hết bọn chúng cho ta"* |

#### B5.2. Trung uý Thép — id −5

| Mục | Giá trị |
|---|---|
| Map | **55 — Tầng 1**, vị trí `x=884, y=312` |
| Chỉ số | `dame × 1,15`, `hp × 1,15` · 0% · Giáp 0 |
| Kỹ năng | Giống Trung uý Trắng nhưng **bỏ nhánh Kamejoko 7→1** (bị comment), giữ Demon 3/6, Dragon 7/1, Galick 5, Antomic 1→7, Masenko 1/5/6, Kamejoko 7 (1.000 ms) |
| Nghỉ | 60 giây |
| Cơ chế | Né 20%; **`damage / 2`**; khiên chia thêm 2; chỉ đánh trong `x ∈ [640, 980]`, không bắn khi người chơi ở `y < 220` (leo cao) |
| Thoại | *"Nếu bọn mi muốn lên tiếp tầng lầu trên / Phải bước qua xác chết của ta đã"* |

#### B5.3. Trung uý Xanh Lơ (doanh trại) — id −6

| Mục | Giá trị |
|---|---|
| Map | **62 — Trại độc nhãn 3**, vị trí `x=1210, y=384` |
| Chỉ số | `dame × 1,10`, `hp × 1,10` · 0% · Giáp 0 |
| Kỹ năng | Bộ 23 chiêu như Trung uý Trắng **+ 6 Thái Dương Hạ San cấp 7 (60.000 ms)** |
| Nghỉ | 60 giây |
| Cơ chế | Né 20%; **`damage / 2`**; khiên chia thêm 2; **đứng yên (AFK) cho tới khi người chơi lại gần trong 500 đơn vị** |
| Thoại | *"Xem các ngươi mạnh đến đâu"*, *"He he he"* |

#### B5.4. Ninja Áo Tím — id −7

| Mục | Giá trị |
|---|---|
| Map | **54 — Tầng 3**, vị trí `x=190, y=312` |
| Chỉ số | `dame × 1,20`, `hp × 1,20` · 0% · Giáp 0 |
| Kỹ năng | Bộ 23 chiêu như Trung uý Trắng |
| Nghỉ | 60 giây |
| **Cơ chế đặc biệt** | ① Né **30%**. ② `damage / 2`. ③ Khiên chia thêm 2. ④ **Xuống ≤ 50% máu: 80% (4/5) cơ hội gọi 4 phân thân (−9…−12), thêm 50% nữa gọi 2 con (−13, −14) → 4 hoặc 6 phân thân.** Đòn kích hoạt gọi phân thân **không gây sát thương** (`return 0`). Chỉ gọi **một lần duy nhất** |
| Thoại | *"Ta sẽ xé xác ngươi ra thành trăm mảnh"*, *"Ha ha ha"* |

#### B5.5. Ninja Áo Tím (phân thân) — id −9, −10, −11, −12, −13, −14

| Mục | Giá trị |
|---|---|
| Lớp | `boss/doanh_trai/NinjaClone.java` |
| Map | **54 — Tầng 3**, xuất hiện quanh boss mẹ (`±200`) |
| Chỉ số | **`dame / 10`, `hpMax / 10`** của Ninja Áo Tím tại thời điểm gọi |
| Giảm ST (bảng) | 0% · Giáp 0 |
| Kỹ năng | Bộ 22 chiêu (giống boss mẹ, **bỏ** Kamejoko 7 hồi 1.000 ms cuối) |
| Cơ chế | Né 20%; `damage / 2`; khiên chia thêm 2 |
| Rơi đồ | id 17 Ngọc Rồng 4 sao ×1, **10%** |
| Thoại | Giống boss mẹ |

#### B5.6. Rôbốt Vệ Sĩ 00 / 01 / 02 / 03 — id −8, −9, −10, −11

| Mục | Giá trị |
|---|---|
| Lớp | `boss/doanh_trai/RobotVeSi.java`; id = `BossID.ROBOT_VE_SI (−8) − i`, i = 0…3 |
| Map | **57 — Tầng 4**, vị trí `x=300, y=312`; **4 con cùng lúc** |
| Chỉ số | `dame × 1,30`, `hp × 1,30` · 0% · Giáp 0 |
| Kỹ năng | Bộ 23 chiêu như Trung uý Trắng |
| Nghỉ | 60 giây |
| Cơ chế | Né theo `tlNeDon`; **`damage / 2`**; khiên chia thêm 2; đứng AFK rồi **nhảy xuống thẳng đầu người chơi** (`setPos(pl.x ± 100, 0)`) |
| Thoại | Không có |

---

### B6. Phó bản Bản Đồ Kho Báu (Hang kho báu)

| Mục | Giá trị |
|---|---|
| Tệp | `map/phoban/BanDoKhoBau.java`, `services_dungeon/TreasureUnderSeaService.java` |
| Điều kiện vào | Có bang hội; **có vật phẩm id 611 — Bản đồ kho báu** trong hành trang (bị trừ 1 khi mở); cấp **1–110**; sức mạnh ≥ 2.000.000.000 (`POWER_CAN_GO_TO_DBKB`) |
| Giới hạn lượt | **3 lượt/ngày cho mỗi người** (`timesPerDayBDKB`) |
| Số bản | `AVAILABLE = 50` |
| Thời lượng | **30 phút**; khi hết quái + boss thì đếm ngược 1 phút đuổi ra |
| Map | 135 Động hải tặc (có **bẫy**, sát thương bẫy = `level × 100.000`), 136 Hang Bạch Tuộc, **137 Động kho báu** (boss), 138 Cảng hải tặc |

#### B6.1. Trung úy Xanh Lơ (bản kho báu) — id −6

| Mục | Giá trị |
|---|---|
| Lớp | `boss/ban_do_kho_bau/TrungUyXanhLo.java` |
| Máu | `min(20.000.000 × level, 2.000.000.000)` |
| Sát thương | `min(200.000 × level, 200.000.000)` |
| Giảm ST (bảng) | **0%** · Giáp 0 |
| Kỹ năng | **Chiêu đấm Demon cấp 1 → 7** (hồi chiêu mặc định) |
| Map thực tế | Được tạo trong **khu map 137 — Động kho báu**, vị trí `x=198, y=456` |
| `mapJoin` khai báo | **`{103}` — Võ đài Xên bọ hung** (sai, nhưng vô hại vì `joinMap()` ghi đè, xem Ghi chú) |
| Nghỉ | 60 giây |
| Cơ chế | Dùng `Boss.injured` mặc định, **không** có chia sát thương riêng. Đứng AFK cho tới khi người chơi vào trong **200 đơn vị** |
| Rơi đồ | **id 705 — Bí ngô 4 sao**, 1 cái, **100%**, người kết liễu. Kèm **+5 Điểm sự kiện** |
| Thoại | *"Các ngươi tới số rồi mới gặp phải ta"*, *"He he he"*, *"Xem các ngươi mạnh đến đâu"* |

---

## PHẦN C — BOSS ĐẤU TRƯỜNG / GIẢI ĐẤU

### C1. Đại hội võ thuật lần 23 — map 129

| Mục | Giá trị |
|---|---|
| Tệp | `matches/dai_hoi_vo_thuat/The23rdMartialArtCongress.java` + `...Service.java` + `matches/giai_dau/The23rdMartialArtCongressManager.java` |
| Lớp boss nền | `boss/dai_hoi_vo_thuat/The23rdMartialArtCongress.java` (trừu tượng) |
| Map | **129 — Đại hội võ thuật** (10 khu). **Mỗi khu chỉ nhận 1 lượt thi đấu** (`getMapChallenge` chỉ trả khu có `< 1` boss) → tối đa **10 người thi cùng lúc** |
| Hình thức | **1 người đấu solo 12 vòng liên tiếp**, tiến độ lưu ở `player.levelWoodChest` — **bắt đầu lại từ đúng vòng đã đạt** |
| Thời gian mỗi vòng | **181 giây**; trước mỗi vòng có **13 giây** dàn cảnh (choáng 14 giây cả hai bên, hồi đầy HP/MP, NPC dẫn chương trình) |
| Rơi khỏi võ đài | `y > 264` và `x ∉ (150, 630)` → xử thua ngay |
| Thua | Chết, hết giờ, hoặc rơi đài. **Không mất gì** — HP/MP hồi đầy khi kết thúc |
| Rơi đồ | **Không boss nào rơi vật phẩm** — lớp nền không ghi đè `reward()`, và `The23rdMartialArtCongress.reward()` (phía ván đấu) chỉ nâng `levelWoodChest` |
| Giới hạn lượt | Không giới hạn lượt/ngày trong code (`lastTimePKDHVT23` chỉ để ghi mốc) |

#### Chỉ số các vòng

| Vòng | Boss | Id | Máu | Sát thương | Giảm ST | Giáp | Kỹ năng | Nghỉ |
|---|---|---|---|---|---|---|---|---|
| 1 | Sói hẹc quyn | −77 | 10.000 | 1.000 | 0% | 0 | Kamejoko 7 (5.000 ms), Galick 7 (1.000 ms) | 600 s |
| 2 | Ở dơ | −78 | 25.000 | 3.000 | 0% | 0 | như trên | 5 s |
| 3 | Xinbatô | −79 | 50.000 | 6.000 | 0% | 0 | như trên | 5 s |
| 4 | Cha pa | −80 | 100.000 | 9.000 | 0% | 0 | như trên | 5 s |
| 5 | Pon put | −81 | 250.000 | 10.000 | 0% | 0 | như trên | 5 s |
| 6 | Chan xư | −82 | 500.000 | 10.000 | 0% | 0 | như trên | 5 s |
| 7 | Tàu Pảy Pảy | −83 | 2.000.000 | 15.000 | 0% | 0 | như trên | 5 s |
| 8 | Yamcha | −84 | 5.000.000 | 15.000 | 0% | 0 | như trên | 5 s |
| 9 | Jacky Chun | −85 | 25.000.000 | 20.000 | 0% | 0 | như trên | 5 s |
| 10 | Thiên xin hăng | −86 | 75.000.000 | 22.000 | 0% | 0 | như trên | 5 s |
| 11 | Liu Liu | −87 | 150.000.000 | 30.000 | 0% | 0 | như trên | 5 s |
| 12 | PôCôLô | −92 | 100.000.000 | 50.000 | 0% | 0 | như trên | 5 s |

> Toàn bộ 12 boss dùng **cùng một bộ 2 kỹ năng**: **1 Chiêu Kamejoko cấp 7** (hồi 5.000 ms)
> và **4 Chiêu đấm Galick cấp 7** (hồi 1.000 ms).
> `mapJoin` của cả 12 boss khai báo **`{168}`** — map này **không tồn tại** trong `map_template`;
> vô hại vì `joinMap()` ghi đè, boss luôn vào đúng khu của người chơi (`x=435, y=264`).

#### Cơ chế đặc biệt theo boss

| Boss | Cơ chế |
|---|---|
| **Chan xư (−82)** | Chỉ bắt đầu đánh sau **10 giây**. Mỗi 10 giây có **20% (1/5)** tung đòn **choáng ngẫu nhiên 1–10 giây** (hiệu ứng 3779), kèm thoại *"Đứng hình"* / *"Nhất dương chỉ"*. Khi người chơi đang choáng thì **chí mạng = 100%**, hết choáng thì **chí mạng = 0%** |
| **Thiên xin hăng (−86)** | Mỗi **30 giây** tung **Phân thân**: sinh **4 bản sao** id −88, −89, −90, −91 (`THIEN_XIN_HANG_CLONE`…`CLONE3`), mỗi con **20.000.000 máu / 5.000 sát thương**, **sống đúng 10 giây** rồi tự biến mất. Bản thân boss được `removeStun` liên tục (**miễn nhiễm choáng**) |
| 10 boss còn lại | Không có cơ chế riêng; dùng `Boss.injured` mặc định (**không** né, **không** chia sát thương) |

#### Thoại
Các boss ĐHVT **không có lời thoại riêng** trong `BossesData` (cả ba mảng `textS/textM/textE` đều rỗng).
Lời thoại đến từ **NPC dẫn chương trình**: *"Trận đấu giữa X vs Y sắp diễn ra"* → *"Xin quý vị khán giả cho 1 tràng pháo tay cổ vũ cho 2 đấu thủ nào"* → *"Mọi người hãy ổn định chỗ ngồi, trận đấu sẽ bắt đầu sau 3 giây nữa"* → *"Trận đấu bắt đầu"*. Cả hai đấu thủ chat *"OK"*.
Vô địch (qua vòng 12): *"Chúc mừng <tên> vừa vô địch giải"*.

---

### C2. Võ đài sinh tử Hạt Mít — map 112

| Mục | Giá trị |
|---|---|
| Tệp | `matches/dai_hoi_vo_thuat/DeathOrAliveArena.java` + `...Service.java` |
| Lớp boss nền | `boss/vo_dai_hat_mit/DeathOrAliveArena.java` (trừu tượng) |
| Map | **112 — Võ đài Hạt Mít** (10 khu, mỗi khu 1 trận) |
| **Phí vào** | **Vàng** — lần đầu `player.thoiVangVoDaiSinhTu`, **mỗi lần vào tăng thêm 10** (thông báo trong code ghi nhầm "không đủ ngọc") |
| Hình thức | 1 người đấu **5 vòng liên tiếp**, **không lưu tiến độ** — thua là làm lại từ vòng 1 |
| Thời gian mỗi vòng | **181 giây**, dàn cảnh 5 giây |
| Rơi khỏi võ đài | `y > 336` và `x ∉ (322, 614)` → xử thua |
| Thắng cả 5 vòng | `haveRewardVDST = true`, ghi kỷ lục `timePKVDST` (tổng thời gian), và ghi nhận **NV 44 bước 2** (`checkDoneTaskWinMatch(player, 1)`). NPC Bà Hạt Mít: *"Đây là phần thưởng cho con."* |
| Cá cược | Khán giả cược cho người chơi hoặc cho Bà Hạt Mít; quỹ = `(cượcBaHatMit + cượcPlayer) × 900.000` vàng, chia theo số phiếu bên thắng |
| Giới hạn lượt | Không giới hạn lượt/ngày; chỉ có phí vàng tăng dần |

#### Chỉ số các vòng

**Máu boss được tính lại lúc vào map**: `hpMax = playerAtt.hpMax / 100 × (100 + (|id| − 82))`.
Máu ghi trong `BossesData` (5.000 / 10.000 / 15.000 / 20.000 / 30.000) **chỉ là giá trị khởi tạo, bị ghi đè ngay**.

| Vòng | Boss | Id | Máu thực tế | Sát thương | Giảm ST | Giáp | Kỹ năng | Nghỉ |
|---|---|---|---|---|---|---|---|---|
| 1 | Đracula | −93 | **111% máu người chơi** | 1.000 | 0% | 0 | 8 Tái tạo năng lượng cấp 1 (60.000 ms) | 1 s |
| 2 | Người vô hình | −94 | **112% máu người chơi** | 1.000 | 0% | 0 | như trên | 1 s |
| 3 | Bông băng | −95 | **113% máu người chơi** | 2.000 | 0% | 0 | như trên | 1 s |
| 4 | Vua Quỷ Sa tăng | −96 | **114% máu người chơi** | 2.000 | 0% | 0 | như trên | 1 s |
| 5 | Thỏ Đầu Bạc | −97 | **115% máu người chơi** | 3.000 | 0% | 0 | như trên | 1 s |

#### Cơ chế đặc biệt (chung, trong `DeathOrAliveArena.injured`)
- **Né 10%** (`Util.isTrue(100, 1000)`).
- **Trần sát thương mỗi đòn = `hpMax / 10`** → **luôn cần ít nhất 10 đòn** để hạ, bất kể sức đánh.
- Người đang cầm Ngọc Rồng Namek (`idNRNM != -1`) chỉ gây **1 sát thương**.

#### Cơ chế riêng theo boss

| Boss | Cơ chế |
|---|---|
| **Đracula (−93)** | **Hút máu**: mỗi **15 giây**, khi máu boss > `hpMax/30`, hút **10% máu tối đa của người chơi** và cộng thẳng vào máu mình |
| **Người vô hình (−94)** | **Tàng hình / dịch chuyển** định kỳ (`tanHinh`) — di chuyển đột ngột, khó nhắm |
| **Bông băng (−95)** | Ghi đè `attack()` nhưng dùng lại `hutMau/tanHinh/bayLungTung` rỗng — thực tế **không có cơ chế riêng** |
| **Vua Quỷ Sa tăng (−96)** | **Bay lung tung** (`bayLungTung` + `lastTimeBay`) |
| **Thỏ Đầu Bạc (−97)** | Ghi đè `attack()`, không có cơ chế riêng |

#### Rơi đồ
**Không boss nào rơi vật phẩm.** Không ghi đè `reward()`. Phần thưởng đến từ NPC Bà Hạt Mít sau khi vô địch và từ tiền cược.

#### Thoại
Cả 5 boss dùng **chung một bộ thoại**: *"He he he"*, *"Ta sẽ xé xác ngươi ra thành trăm mảnh"*, *"Xem các ngươi mạnh đến đâu"*.
Trước mỗi vòng boss nói *"Sẵn sàng chưa?"*; NPC Bà Hạt Mít: *"Khá lắm, chuẩn bị đánh tiếp nào"*, *"Con tắc kè màu xanh màu đỏ...Em bắt về em nấu cà ri...Ồ là la ýe..."*, khi thua: *"Người tiếp theo chuẩn bị."*

---

### C3. Đấu Siêu Hạng (Super Rank) — map 113

| Mục | Giá trị |
|---|---|
| Tệp | `matches/dai_hoi_vo_thuat/SuperRank.java`, `SuperRankService.java`; boss `boss/sieu_hang/SuperRank.java` + `Rival.java` |
| Map | **113 — Đại hội võ thuật** (10 khu) |
| Boss | **Rival** — bản sao **một người chơi thật khác** tải từ CSDL, id boss = **`−(id người chơi bị sao chép)`** |
| Điều kiện thách đấu | Phải đứng ở map 113; đối thủ phải **hạng cao hơn**; nếu đối thủ **hạng < 10** thì chỉ được thách đấu **cách tối đa 2 hạng**; **không** thách đấu chính mình; cả hai bên **không đang thi đấu / chờ thi đấu** |
| **Phí** | 1 vé `superRank.ticket`, hết vé thì **1 ngọc** |
| Thời lượng | **180 giây** (`timeDown = 180`), dàn cảnh 5 giây trước trận |
| Vị trí | Người chơi `x=334, y=264`; Rival `x=434, y=264` |

#### Chỉ số Rival

| Mục | Giá trị |
|---|---|
| Máu | **`nPoint.hpg` của người chơi bị sao chép** (máu gốc, chưa cộng trang bị) |
| Sát thương | **`nPoint.dameg` của người chơi bị sao chép** |
| Giảm ST (bảng) | **0%** |
| **Giáp** | **= giáp thật của người chơi bị sao chép** — `SuperRank.joinMap()` gán `this.nPoint = player.nPoint` (đây là boss **duy nhất** có giáp ≠ 0 trong tài liệu này) |
| Kỹ năng | **Toàn bộ kỹ năng của người chơi bị sao chép** đúng cấp và đúng hồi chiêu, **trừ**: 14 Tự phát nổ, 10 Quả cầu kênh khí, 11 Makankosappo, 7 Trị thương |
| `mapJoin` | `{113}` |
| Nghỉ | 60 giây (không dùng — boss chỉ sống trong 1 trận) |

#### Cơ chế đặc biệt
- **Ăn đậu thần**: mỗi **5 giây**, nếu máu chưa đầy thì Rival **ăn đậu** (`UseItem.eatPea`).
- MP luôn đầy (`nPoint.mp = nPoint.mpMax` mỗi nhịp đánh).
- AI biết **không lãng phí chiêu khống chế**: nếu người chơi đang dính hiệu ứng, hoặc bản thân đang bật khiên và còn > 50% máu, thì đổi sang chiêu số 0 thay vì Khiên/Thái Dương/Thôi miên/Trói.
- `injured()` mô phỏng **đầy đủ hệ thống chỉ số của người chơi**: vô hiệu chưởng, tỉ lệ né (trần 90%), tỉ lệ giáp (trần 86%), xuyên giáp chưởng/chí mạng, Giáp Xên (`/2`) và Giáp Xên 2 (`×40%`), khiên năng lượng (`damage = 1`), chặn tấn công người **vừa nạp lần đầu**.
- Kết trận thắng/thua có chat `ConstSuperRank.TEXT_CLONE_THUA` / `TEXT_CLONE_THANG`.

#### Rơi đồ
**Không rơi vật phẩm.** Phần thưởng là **thứ hạng** và ngọc trả theo hạng mỗi ngày:

| Hạng | Thưởng |
|---|---|
| 1 | +100 ngọc/ngày |
| 2 – 10 | +20 ngọc/ngày |
| 11 – 100 | +5 ngọc/ngày |
| 101 – 1000 | +1 ngọc/ngày |

#### Thoại
Không có thoại trong `BossData` (sao chép từ người chơi). Thoại từ hệ thống: *"Trận đấu bắt đầu"*, `TEXT_SAN_SANG_CHUA` ("Sẵn sàng chưa?").

---

## PHẦN D — BOSS NHÂN BẢN

### D1. Bản sao Commeson — id `Util.createIdBossClone(idNhanVat)`

| Mục | Giá trị |
|---|---|
| Lớp | `boss/nhan_ban/NhanBan.java`; tạo trong `services/Service.callNhanBan()` |
| Gọi từ | **NPC Potage** (`npc_list/Potage.java`) ở map **140 — Hang động Potaufeu** |
| Nhóm | Boss nhân bản (`isCopy = true`) |
| Map | **140 — Hang động Potaufeu**, xuất hiện cách người chơi `±200` |
| **Máu** | **`maxint(máu tối đa người chơi × 10)`** — kẹp trần `int` |
| **Sát thương** | **`maxint(sát thương người chơi × 10)`** |
| Giảm ST (bảng) | **0%** |
| **Giáp** | **0** (chỉ sao chép máu/sát thương, **không** sao chép `nPoint`) |
| Kỹ năng | **Toàn bộ kỹ năng của người chơi** đúng cấp + đúng hồi chiêu, **trừ** 14 Tự phát nổ và 23 Trói |
| Tạo hình | Sao chép đầu/thân/chân/cờ/hào quang/hiệu ứng của người chơi; tên = tên người chơi |
| Nghỉ (`secondsRest`) | 60 giây (không dùng) |

#### Thời gian / giới hạn lượt
- **1 lần/ngày** (`Util.isAfterMidnight(player.lastPkCommesonTime)`; admin bỏ qua).
- **5 phút** để hạ (`EffectSkillService.setPKCommeson(player, 300000)`). Hết 5 phút → *"Bạn đã thất bại, ngày mai hãy thử sức tiếp"*, boss rời map.
- Mỗi người **chỉ 1 bản sao tại một thời điểm**; người khác gọi trong lúc đó sẽ thấy *"Đang có 1 nhân bản của <tên> hãy chờ kết quả trận đấu"*.

#### Rơi đồ

| Vật phẩm | Số lượng | Tỉ lệ | Ai nhận | Chỉ số |
|---|---|---|---|---|
| **id 638 — Bình chứa Commeson** | 1 | **100%** | Người kết liễu | `ItemOption(93, 30)` = HSD 30 ngày; `ItemOption(30, 0)` = không giao dịch |

Ngoài ra `reward()` gọi `TaskService.checkDoneTaskKillBoss` → hạ bản sao của chính mình ghi nhận
**NV 31 bước 4** (`TASK_31_4`) **và** **NV 49 bước 4** (`TASK_49_4`).

#### Cơ chế đặc biệt
- Ngay khi kích hoạt, cả boss và người chơi bị ép sang chế độ `PK_PVP`.
- Đây là boss **PvP tay đôi**: `PKCommeson(playerAtt, this)` khoá trận với đúng một người.
- Khi chết có `ServerNotify` toàn server: *"<tên> đã đánh bại bản sao Commeson, mọi người đều ngưỡng mộ"*.
- NPC Potage còn phục vụ **điểm rẽ nhánh NV 31 ↔ NV 49**: khi người chơi đang ở `TASK_31_1`, NPC mở menu riêng (id 2201) *"Bản sao của ngươi đã bị khuất phục. Ngươi muốn tiêu diệt nó, hay thu nhận nó?"* — hai nút **"Tiêu diệt"** / **"Thu nhận"**.

#### Lời thoại
- Xuất hiện: *"Boss nhân bản đã xuất hiện rồi"* (loại thoại −2, hệ thống)
- Khi đánh: *"Ta sẽ thay thế ngươi, haha"*
- Khi chết: *"Lần khác ta sẽ xử đẹp ngươi"*

---

## PHẦN E — BOSS LUYỆN TẬP TỰ ĐỘNG

### E0. Cơ chế chung của lớp `TrainingBoss`

Tệp: `boss/luyen_tap_tu_dong/TrainingBoss.java`; điều phối ở `services_dungeon/TrainingService.java`.

| Mục | Giá trị |
|---|---|
| Cách gọi | `TrainingService.callBoss(player, bossID, isThachDau)` — gọi từ NPC tương ứng; NPC bị **ẩn đi** trong lúc boss đứng sân, hiện lại khi boss rời |
| Hai chế độ | **Luyện tập** (`isThachDau = false`) và **Thách đấu** (`isThachDau = true`) — khác nhau ở câu chào (`textS[0]` vs `textS[1]`) và ở chỗ thắng khi thách đấu thì `levelLuyenTap++` |
| **Né đòn** | **40%** (`Util.isTrue(400, 1000)`) ở hầu hết boss — cực cao, cố ý để trận kéo dài |
| Giảm ST (bảng) | **0%** ở toàn bộ 9 boss |
| Giáp | **0** |
| **Hồi máu** | `buffPea()`: mỗi **30 giây** hồi **20% máu tối đa** và đầy MP (trừ Tàu Pảy Pảy đã ghi đè thành rỗng) |
| Người chơi chết | Boss **không giết** — chỉ chuyển sang AFK, chat *"Luyện tập tiếp đi"*, bỏ chế độ PK |
| Rơi đồ | **Không boss nào rơi vật phẩm và không cho vàng.** `die()` chỉ tăng `levelLuyenTap` khi đang thách đấu |
| Sức mạnh nhận được | Qua `TrainingService.tangTnsmLuyenTap` / `tnsmLuyenTapUp` (tính theo thời gian offline), không qua boss |

**Bảng sức mạnh mỗi phút theo `levelLuyenTap`**: 0→20, 1→40, 2→80, 3→160, 4→320, 5→640, ≥6→max(1280, `tnsmLuyenTap`). Trần `tnsmLuyenTap` = 10.000.000. Qua nửa đêm bị trừ 1/3.

#### E1. Tàu Pảy Pảy (dạy chiêu) — id −385

| Mục | Giá trị |
|---|---|
| Map | **46 — Tháp Karin** |
| Máu 1.000 · Sát thương 500 · 0% · Giáp 0 | |
| Kỹ năng | 4 Chiêu đấm Galick cấp 1 (1.000 ms); 8 Tái tạo năng lượng cấp 1 (60.000 ms) |
| Nghỉ | 1 giây |
| **Cơ chế đặc biệt** | ① Né 40%. ② **Nếu nhiệm vụ hiện tại `< TASK_10_1` thì mọi đòn chỉ gây 100 sát thương** — chốt chặn NV 10 "Bái sư". ③ **Không** có `buffPea` (đã ghi đè rỗng). ④ AFK 5 giây rồi tự rời map bằng phi thuyền |
| Thoại | Luyện tập: *"Ta sẽ dạy ngươi vài chiêu"* / Thách đấu: *"Ta sẽ đánh hết sức, ngươi cẩn thận nhé"*; *"Haizzzzz"*, *"Hahaha"*, *"Xem đây"*; chết: *"OK ta chịu thua"* |

#### E2. Karin — id −357

| Mục | Giá trị |
|---|---|
| Map | **46 — Tháp Karin**, vị trí `x=420, y=408` · NPC ẩn: Thần Mèo Karin |
| Máu 500 · Sát thương 500 · 0% · Giáp 0 | |
| Kỹ năng | 4 Galick cấp 1 (1.000 ms); 8 Tái tạo năng lượng cấp 1 (60.000 ms) |
| Nghỉ | 1 giây |
| Cơ chế | Né 40%; hồi 20% máu mỗi 30 giây; **bay nhảy liên tục** (`bayLungTung` mỗi 5 và 5,5 giây, nhảy cao −100); AFK 5 giây rồi rời map |
| Thoại | Giống Tàu Pảy Pảy (dùng chung bộ thoại) |

#### E3. Yajirô — id −358

| Mục | Giá trị |
|---|---|
| Map | **46 — Tháp Karin**, vị trí `x=320, y=408` |
| Máu 1.100 · Sát thương 1.100 · 0% · Giáp 0 | |
| Kỹ năng | 4 Galick cấp 1 (1.000 ms); 8 Tái tạo năng lượng cấp 1 (60.000 ms) |
| Nghỉ | 1 giây · AFK **15 giây** rồi rời map |
| Cơ chế | Né 40%; hồi 20% máu mỗi 30 giây |
| Thoại | *"Ngon nhào vô"* / *"Cho mi biết sự lợi hại của ta"*; chết: *"Ngươi thật lợi hại"* |

#### E4. Mr.PôPô — id −359

| Mục | Giá trị |
|---|---|
| Map | **46 — Tháp Karin**, vị trí `x=295, y=408` |
| Máu 5.100 · Sát thương 1.100 · 0% · Giáp 0 | |
| Kỹ năng | 4 Galick cấp 1 (30.000 ms); 1 Kamejoko cấp 1 (30.000 ms); 6 Thái Dương Hạ San cấp 3 (30.000 ms) |
| Nghỉ | 1 giây · AFK 15 giây rồi rời map |
| Cơ chế | Né 40%; hồi 20% máu mỗi 30 giây; **bay né liên tục** (mỗi 3 và 4 giây) |
| Thoại | Luyện tập: *"Đánh trúng ta 1 cái coi như ngươi thắng"* / Thách đấu: *"Đánh trúng ta 3 cái coi như ngươi thắng"*; chết: *"Thua thì thua"* |

#### E5. Thượng đế — id −360

| Mục | Giá trị |
|---|---|
| Map | **49 — Phòng tập thời gian**, vị trí `x=408, y=436`. Khi gọi boss, người chơi **được đưa thẳng vào map 49** |
| Máu 1.000 · **Sát thương 10.000** · 0% · Giáp 0 | |
| Kỹ năng | 0 Chiêu đấm Dragon cấp 1 (1.000 ms) |
| Nghỉ | 1 giây · AFK 5 giây rồi rời map |
| Cơ chế | Né 40%; hồi 20% máu mỗi 30 giây; chờ 4 giây mới chào; **khi kết thúc mà người chơi chưa chết thì được đưa sang map 45** |
| Thoại | *"Ta sẽ dạy võ cho con trong phòng tập thời gian này"* / *"Con hãy đánh hết sức nhé, ta sẽ không nương tay đâu"*; chết: *"Ta rất tự hào về con"* |

#### E6. Khỉ Bubbles — id −361

| Mục | Giá trị |
|---|---|
| Map | **48 — Hành tinh Kaio** |
| Máu 30.000 · Sát thương 30.000 · 0% · Giáp 0 | |
| Kỹ năng | 4 Chiêu đấm Galick cấp 7 (1.000 ms); **13 Biến hình cấp 7 (1.800.000 ms)** |
| Nghỉ | 1 giây · AFK 15 giây rồi rời map |
| Cơ chế | Né 40%; hồi 20% máu mỗi 30 giây |
| Thoại | *"Ù ù khẹt khẹt"*, *"khẹt khẹt"*, *"ù ù khẹc khẹc"*, *"éc éc"*; chết: *"Éc Éc Éc Éc!"* |

#### E7. Thần Vũ Trụ — id −362

| Mục | Giá trị |
|---|---|
| Map | **48 — Hành tinh Kaio**, vị trí `x=420, y=240` · NPC ẩn: Thần Vũ Trụ |
| Máu 45.000 · Sát thương 45.000 · 0% · Giáp 0 | |
| Kỹ năng | 4 Galick cấp 7 (1.000 ms); 6 Thái Dương Hạ San cấp 7 (30.000 ms) |
| Nghỉ | 1 giây · AFK 7 giây rồi rời map |
| Cơ chế | Né 40%; hồi 20% máu mỗi 30 giây; lúc AFK **đổi độ cao qua lại 240 ↔ 360** mỗi giây |
| Thoại | *"Ta sẽ dạy ngươi chiêu kaio-ken"* / *"Ngươi cũng to gan lắm"*; chết: *"Tại hôm nay ta...ta hơi bị đau bụng"* |

#### E8. Tổ sư Kaio — id −363

| Mục | Giá trị |
|---|---|
| Map | **50 — Thánh địa Kaio**, xuất hiện ngay chỗ người chơi |
| Máu 45.000 · Sát thương 45.000 · 0% · Giáp 0 | |
| Kỹ năng | 4 Chiêu đấm Galick cấp 1 (60.000 ms) |
| Nghỉ | 1 giây |
| **Cơ chế đặc biệt** | **Hoàn toàn bất tử** — `injured()` ghi đè thành `return 0`. Không phải boss để đánh: mỗi **10 giây** cộng cho người chơi `getTnsmMoiPhut(player) / 6` sức mạnh tiềm năng. Đây là **cỗ máy luyện tập**, không phải đối thủ |
| Thoại | Không có thoại trong `BossData` |

#### E9. Whis — id −364

| Mục | Giá trị |
|---|---|
| Map | **154 — Hành tinh Bill**, vị trí `x=725, y=312` (rồi nhảy về 341, 320) |
| Tên hiển thị | **`Whis [LV:<cấp>]`** với `cấp = player.traning.getTop() + 1` |
| **Máu** | **550.000 × cấp** |
| **Sát thương** | **10.000 × cấp** |
| Giảm ST (bảng) | 0% · Giáp 0 |
| Kỹ năng | 0 Chiêu đấm Dragon cấp 7 (500 ms) |
| Nghỉ | 60 giây |
| **Cơ chế đặc biệt** | ① Né 40%. ② **Mọi đòn bị chia cho `cấp`** (`damage /= level`) — cấp càng cao càng khó gấp bội (máu ×cấp **và** sát thương nhận ÷cấp → độ khó thực tế **tăng theo bình phương cấp**). ③ Không có `buffPea` trong `injured` riêng nhưng vẫn kế thừa từ `attack()`. ④ Thắng thì ghi kỷ lục `traning.top`, `traning.time` (thời gian hạ) và `lastTime` vào bảng `TraningDAO`, đồng thời `plKill.thachdauwhis++` và bật cờ xếp hạng `Manager.isTopWhisChanged` |
| Nhiệm vụ | **NV 44 bước 3** (`TASK_44_3`) |
| Rơi đồ | **Không rơi vật phẩm**, chỉ ghi nhận kỷ lục và bảng xếp hạng |
| Thoại | *"Ta sẽ dạy ngươi vài chiêu"*; *"Xem đây"*, *"Haizzzzz"*, *"Hahaha"*, *"AAAAAAAAAA"*; chết: chọn ngẫu nhiên *"OK ta chịu thua"* / *"Ta rất tự hào về con"* / *"Tại hôm nay ta...ta hơi bị đau bụng"* / *"Thua thì thua"* |

---

## GHI CHÚ — điểm cần biết, số liệu chưa chắc, lỗi phát hiện

### G1. Boss chưa được tạo / chưa nối dây

| Mục | Tình trạng |
|---|---|
| Không có boss nào trong nửa 2 bị thiếu lớp Java | Cả 61 loại đều có lớp và đều được `BossManager.createBoss` / phó bản / dịch vụ tạo ra |
| `boss/Boss_mini/SoiHecQuyn.java` và `boss/dai_hoi_vo_thuat/SoiHecQuyn.java` | Hai lớp khác nhau dùng **cùng id −77** và **cùng `BossesData.SOI_HEC_QUYN`**. Bản mini là boss thế giới (73 map, nghỉ 600 giây), bản ĐHVT là đối thủ vòng 1. Không xung đột lúc chạy vì hai nhóm quản lý khác nhau, nhưng **cần lưu ý khi sửa `BossesData.SOI_HEC_QUYN`: sửa một chỗ là đổi cả hai** |

### G2. Trùng id thật sự — cần xử lý

| Id | Hai bên dùng | Rủi ro |
|---|---|---|
| **−9, −10, −11** | `RobotVeSi` (map 57, `ROBOT_VE_SI − i` với i = 1,2,3) **và** `NinjaClone` (map 54, `NINJA_AO_TIM1…3`) | Cùng một instance phó bản có thể có 2 boss trùng id ở 2 map khác nhau. Hàm `RedRibbonHQ.updateHPDame()` xử lý theo `boss.zone.map.mapId` nên **hiện tại không sai**, nhưng điều kiện `if (boss.id >= -14 && boss.id <= -9)` (chia 10 cho phân thân Ninja) nằm **trong nhánh map 54** nên vẫn đúng. Mọi tra cứu boss **theo id không kèm map** ở doanh trại sẽ nhầm |
| **−6** | `doanh_trai/TrungUyXanhLo` (map 62) **và** `ban_do_kho_bau/TrungUyXanhLo` (map 137) | Hai phó bản khác nhau, hai manager khác nhau (`RedRibbonHQManager` vs `TreasureUnderSeaManager`) nên không đụng nhau lúc chạy |
| **−77** | `Boss_mini/SoiHecQuyn` và `dai_hoi_vo_thuat/SoiHecQuyn` | Xem G1 |
| **−83** | `dai_hoi_vo_thuat/TauPayPay` (`TAU_PAY_PAY = −83`) và `BossID.TAU_PAIPAI = −385` (luyện tập) — **khác id**, không trùng | Không vấn đề |

### G3. Mã chết / chú thích lệch so với code

| Vị trí | Nội dung |
|---|---|
| `BossManager.loadBoss()` dòng 174–180 | Chú thích còn ghi *"QuestBoss.rest() chỉ cho RESPAWN khi quét thấy một người chơi đang ở đúng nhiệm vụ của nó"*. **Sai với code hiện tại** — `findRandomZone()` không còn quét người chơi. Chú thích chưa được cập nhật theo quyết định 18/09/2026 |
| `QuestBoss.isPlayerOnQuestStep()` | **Không được gọi ở bất kỳ đâu** — mã chết sau khi bỏ luật "chỉ hiện cho người đang làm nhiệm vụ" |
| `QuestBoss.taskIdRequired` / `getTaskIdRequired()` | Chỉ còn được `isPlayerOnQuestStep` (mã chết) dùng, và `Heart` ghi đè. **Không ảnh hưởng hành vi** |
| `QuestBoss.IDLE_LEAVE` và `lastTimeHavePlayer` | Khai báo nhưng **không dùng để quyết định gì** (`autoLeaveMap()` đã bị vô hiệu hoá) |
| `BossData.secondsRest` của 9 boss nhiệm vụ | `REST_1_M`, `REST_2_M`, `REST_5_M`, `REST_10_M` ghi trong `BossesData` **bị `QuestBoss.rest()` bỏ qua hoàn toàn** — thời gian nghỉ thật luôn là ngẫu nhiên 15–30 phút |
| `mapJoin = {168}` của 12 boss ĐHVT | **Map 168 không tồn tại** trong `map_template`. Vô hại vì `joinMap()` ghi đè, nhưng là số rác |
| `mapJoin = {103}` của `ban_do_kho_bau/TrungUyXanhLo` | Boss thật nằm ở map 137. Vô hại vì `joinMap()` ghi đè |
| Máu trong `BossesData` của 5 boss Võ đài Hạt Mít | 5.000 / 10.000 / 15.000 / 20.000 / 30.000 **bị ghi đè ngay khi vào map** bằng `% máu người chơi`. Chỉ có ý nghĩa trong khoảnh khắc khởi tạo |
| Chú thích tỉ lệ rơi đồ của 9 boss Mabư 12h | Ghi *"30% xác suất để rơi đồ"* và *"80% xác suất rơi ngọc rồng"* nhưng code là `Util.isTrue(1,100)` = **1%** và `Util.isTrue(10,100)` = **10%**. Bảng trong tài liệu này ghi theo **code** |
| Chú thích `// Xác suất rơi item 1560 (50%)` ở 4 boss doanh trại | Item thật là **17** (Ngọc Rồng 4 sao), không phải 1560. Chú thích sai, code đúng |
| Chú thích `// Xác suất rơi item 611 (30%)` ở Trung uý Xanh Lơ | Code là `Util.isTrue(100, 100)` = **100%**, không phải 30% |
| Thông báo `"Bạn không có đủ ngọc !"` ở võ đài Hạt Mít | Phí thực tế trừ **vàng** (`player.inventory.gold`), không phải ngọc. Thông báo sai |
| `DestronGas.N_PLAYER_CLAN = 0` | Điều kiện *"bang phải có ≥ N người"* thực tế **luôn đúng** vì N = 0 |
| `RedRibbonHQ.N_PLAYER_CLAN = 5` | Hằng số khai báo nhưng **không thấy được kiểm tra** trong `RedRibbonHQService.joinDoanhTrai()` |
| `Hatchiyack` có `name = " "` | Tên boss là **một dấu cách** — hiển thị rỗng trên client. Có thể là cố ý (giấu tên cho đến khi xuất hiện) hoặc là lỗi; **không tra được ý định** |

### G4. Số liệu KHÔNG TRA ĐƯỢC hoặc cần điều kiện

| Mục | Ghi chú |
|---|---|
| **Vật phẩm id 2029 "Ống nghiệm Myuu"** | **Chưa có trong `database team2026.sql`** — mới chỉ nằm trong `SRC/sql/patch/01-vat-pham-moi.sql` (32 vật phẩm id 2000–2031). Nếu chưa chạy patch thì `QuestBoss.dropQuestItem()` sẽ **âm thầm bỏ qua** (có kiểm tra `itemId >= Manager.ITEM_TEMPLATES.size()`), Heart hình dạng 3 sẽ **không rơi gì**. Sau khi chạy patch còn phải sửa `DataGame.vsItem` (9 → 10) và khởi động lại server |
| Máu/sát thương boss phó bản theo bang (doanh trại) | **Không có số tuyệt đối** — phụ thuộc hoàn toàn vào tổng chỉ số thành viên đang online. Tài liệu này chỉ ghi công thức |
| Máu/sát thương boss phó bản theo cấp (CDRD, Khí gas, Kho báu) | Ghi công thức đầy đủ. Giá trị cụ thể tuỳ cấp 1–110 người chơi chọn |
| Thời gian hạ boss thực tế (phút/người) | **Không tính trong tài liệu này** — cần mốc sức mạnh người chơi, xem `docs/4-trien-khai/33-giam-sat-thuong-boss.md` (chưa đối chiếu lại) |
| Giới hạn lượt/ngày của ĐHVT 23, Võ đài Hạt Mít, Mabư 12h, Mabư 14h | **Không có** trong code. Võ đài Hạt Mít chỉ chặn bằng **phí vàng tăng dần 10 mỗi lượt** |
| Bảng `randDoTLBoss` (đồ Thần Linh rơi từ boss 12h, 1%) | Danh sách vật phẩm cụ thể nằm trong `ItemService.randDoTLBoss` — **chưa đọc**, nằm ngoài phạm vi nửa 2 |
| Danh sách chi tiết 39 id trang bị rơi từ boss 12h (230…280) | Đã ghi dải id trong bảng; **chưa tra tên từng món** |
| Hồi chiêu mặc định của các chiêu khai báo 2 phần tử (`{skill, level}`) | Lấy từ `skill_template` theo cấp — ví dụ Galick, Demon trong `Util.addArray(FULL_*)`. **Không ghi số cụ thể** trong tài liệu này |
| Ý nghĩa các `ItemOption` id (50, 77, 93, 94, 103, 107, 112, 5, 20, 30) | Chỉ tra được chắc chắn: **30 = không thể giao dịch**, **93 = hạn sử dụng (ngày)**, **107 = sao pha lê**. Các id còn lại **không tra được** trong phạm vi tài liệu này |

### G5. Điều đã kiểm chứng và ĐÚNG với nguyên tắc đã chốt

1. **Boss nhiệm vụ CHỈ rơi đồ nhiệm vụ** — đã kiểm tra toàn bộ 9 lớp con của `QuestBoss`:
   không lớp nào ghi đè `reward()`, không lớp nào gọi `randDoTLBoss`, không có item 190 (Vàng),
   không có id 14–20 (Ngọc Rồng), không có trang bị. **Chỉ Heart** ghi đè `getQuestItemId()`
   và chỉ trả **id 2029** ở đúng hình dạng 3. ✔
2. **Nghỉ ngẫu nhiên 15–30 phút, hiện lại ở khu ngẫu nhiên** — `REST_MIN_SECONDS = 900`,
   `REST_MAX_SECONDS = 1800`, `findRandomZone()` bốc map ngẫu nhiên rồi khu ngẫu nhiên. ✔
3. **Luôn có mặt, không tự rời map khi khu vắng** — `autoLeaveMap()` bị vô hiệu hoá. ✔
4. **Không đè lên boss thế giới** — `isZoneOccupied()` chặn theo `worldBossIds` + id trùng. ✔
5. **Heart đổi map 166 → 145** — `joinMap()` giữ trạng thái `JOIN_MAP`, chờ tối đa 10 phút. ✔
6. **BossData không có trường giáp; giáp lúc chạy = 0** với mọi boss trừ Rival. ✔

---

*Tài liệu này đọc từ mã nguồn ngày 18/09/2026. Mọi số liệu đều trích từ code, không suy diễn.*
