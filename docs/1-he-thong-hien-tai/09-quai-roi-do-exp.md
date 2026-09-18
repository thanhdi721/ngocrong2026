# 09 — Quái, rơi đồ (drop) & kinh nghiệm (EXP / tiềm năng)

> Spec đối chiếu từ source: `SRC/src/nro/models/mob/*` (`Mob.java`, `MobPoint.java`, `BigBoss.java`, `MobMe.java`, `MobEffectSkill.java`), `mob_bigboss/*` (9 file), `services/RewardService.java`, cùng các hàm liên quan (`map/Map.java`, `map/Zone.java`, `map/service/MapService.java`, `services/ItemService.java`, `player/NPoint.java`, `services/Service.java`, `server/Manager.java`) và DB `database team2026.sql` (`mob_template`, `map_template`, `item_template`, `item_option_template`).

## Mục lục

1. [Cấu trúc dữ liệu quái](#1-cấu-trúc-dữ-liệu-quái)
2. [HP & sát thương của quái](#2-hp--sát-thương-của-quái)
3. [Vòng cập nhật, hồi sinh, hồi máu](#3-vòng-cập-nhật-hồi-sinh-hồi-máu)
4. [AI tấn công](#4-ai-tấn-công)
5. [Nhận sát thương & các trường hợp đặc biệt](#5-nhận-sát-thương--các-trường-hợp-đặc-biệt)
6. [Quái tinh anh (siêu quái / `lvMob`)](#6-quái-tinh-anh-siêu-quái--lvmob)
7. [Big Boss map (mob_bigboss)](#7-big-boss-map-mob_bigboss)
8. [Công thức tiềm năng / sức mạnh khi đánh quái](#8-công-thức-tiềm-năng--sức-mạnh-khi-đánh-quái)
9. [Bảng rơi đồ khi giết quái (đầy đủ)](#9-bảng-rơi-đồ-khi-giết-quái)
10. [Quái đệ tử (MobMe – chiêu Đẻ trứng)](#10-quái-đệ-tử-mobme)
11. [RewardService (vòng quay, quà hộp, chỉ số đồ)](#11-rewardservice)
12. [Danh sách `mob_template`](#12-danh-sách-mob_template)
13. [Bố trí quái theo map (`map_template.mobs`)](#13-bố-trí-quái-theo-map)
14. [Ghi chú / điểm cần lưu ý](#14-ghi-chú--điểm-cần-lưu-ý)

---

## 1. Cấu trúc dữ liệu quái

### 1.1 Bảng `mob_template` (DB) → `Template.MobTemplate`

Nạp ở `server/Manager.java` dòng ~741–755 (`select * from mob_template`).

| Cột DB | Ý nghĩa | Dùng ở đâu |
|---|---|---|
| `id` | tempId quái | `Mob.tempId` |
| `TYPE` | loại quái (0 = đứng yên/mộc nhân, 1 = thường, 4 = bay…) | `Mob.type`; AI chủ động (mục 4) |
| `NAME` | tên | client |
| `hp` | HP mẫu (**không** dùng để khởi tạo quái trên map, HP lấy từ `map_template`) | — |
| `range_move`, `speed`, `dart_Type` | di chuyển / đạn | client |
| `percent_dame` | % HP quy ra sát thương | `Mob.pDame` |
| `percent_tiem_nang` | % HP quy ra tiềm năng tối đa | `Mob.pTiemNang` |

### 1.2 Quái trên map (`map_template.mobs`)

Mỗi phần tử có dạng `[tempId, level, hp, x, y]` (parse ở `Manager.java` dòng ~841–849). `Map.initMob()` (`map/Map.java` dòng 193–240) tạo 1 bản quái cho **mỗi khu (zone)** của map:

- `level`, `hp` (HP tối đa) lấy theo map; `pDame`, `pTiemNang`, `type` lấy theo `mob_template`.
- Theo tempId, class được tạo: 70 → `Hirudegarn`, 71 → `VuaBachTuoc`, 72 → `RobotBaoVe`, 77 → `GauTuongCuop`, 82 → `VoiChinNga`, 83 → `GaChinCua`, 84 → `NguaChinLmao`, 85 → `Piano`, 117 → `MayDoSucManh`, còn lại → `Mob`.

### 1.3 Thuộc tính `Mob` (`mob/Mob.java` dòng 34–63)

| Trường | Mặc định | Ý nghĩa |
|---|---|---|
| `id` | index trong map | id quái trong khu |
| `tempId`, `name`, `level` | | |
| `point` (`MobPoint`) | | `hp`, `maxHp`, `dame` |
| `effectSkill` (`MobEffectSkill`) | | trạng thái: choáng (`isStun`), thôi miên, mù DCTT, trói (`isAnTroi`), socola, biến bình… |
| `pDame`, `pTiemNang` | | từ `mob_template` |
| `maxTiemNang` | `hpFull × (pTiemNang ± 2)/100` | được tính trong `setTiemNang()` nhưng **không được dùng** khi cộng tiềm năng |
| `lvMob` | 0 | cấp "siêu quái" (mục 6) |
| `status` | 5 | 5 = sống, 0 = chết |
| `timeAttack` | 2000 ms | nhịp tấn công |
| `temporaryEnemies` | | danh sách người đã đánh quái (quái đánh trả) |
| `lastTimeDie`, `lastTimePhucHoi` | | mốc hồi sinh / hồi máu |

---

## 2. HP & sát thương của quái

### 2.1 HP

HP tối đa = giá trị `hp` trong `map_template.mobs` (xem bảng mục 13). Cột `hp` trong `mob_template` chỉ là mẫu.

### 2.2 Sát thương (`MobPoint.getDameAttack`, `mob/MobPoint.java` dòng 36–40)

```
nếu point.dame != 0 (quái đệ tử MobMe):   dame ± dame/100
ngược lại: hpFull × random[pDame−1, pDame+1] / 100  +  random[−level×10, level×10]
```

Ví dụ: quái HP 200, `pDame = 5`, level 2 → 200×(4..6)/100 + (−20..20) = 8..12 ± 20.

### 2.3 Hệ số khi quái đánh người chơi (`Mob.mobAttackPlayer`, dòng 394–432)

| Điều kiện | Ảnh hưởng |
|---|---|
| Người chơi có Bùa da trâu (`charms.tdDaTrau`) | sát thương ÷ 2 |
| Mục tiêu là đệ tử, sư phụ có Bùa đệ tử (`charms.tdDeTu`) | ÷ 2 |
| Quái có `lvMob > 0` và không ở phó bản | sát thương = **10% HP tối đa** của người chơi |
| Vệ tinh phòng thủ đang bật (`satellite.isDefend`) | −20% |
| `itemTime.isUseCMS` | × 0,1 |
| Sau đó | `player.injured(...)`, rồi phản sát thương theo `nPoint.tlPST` (%) |

---

## 3. Vòng cập nhật, hồi sinh, hồi máu

- **Tick:** `Manager` chạy scheduler mỗi **1 giây**, cập nhật mọi zone theo lô 10 zone (`server/Manager.java` dòng 199–239) → `Zone.update()` → `udMob()` → `Mob.update()`.
- `Mob.update()` (dòng 256–312):

| Loại map (`zone.map.type`) | Hồi sinh |
|---|---|
| `MAP_DOANH_TRAI` | Chỉ quái **Bulon** (tempId 22) hồi sinh sau **10 giây** khi Tướng (`zone.isTUTAlive`) còn sống |
| `MAP_BAN_DO_KHO_BAU`, `MAP_CON_DUONG_RAN_DOC`, `MAP_KHI_GAS_HUY_DIET`, `MAP_TAY_KARIN` | Không tự hồi sinh (do class phó bản/tính năng tự `hoiSinh`: `BanDoKhoBau`, `SnakeWay`, `DestronGas`, `RedRibbonHQ`, `SuperDivineWaterService`) |
| Map thường | Hồi sinh sau **3 giây** kể từ lúc chết (`Util.canDoWithTime(lastTimeDie, 3000)`), HP đầy |

- **Hồi máu khi còn sống:** mỗi **30 giây**, nếu `hp < maxHp` hồi **10% maxHp** (`hoi_hp(maxHp/10)`).
- **Ngoại lệ Golden Frieza:** nếu `zone.isGoldenFriezaAlive` và `TimeUtil.is21H()` → mọi quái trong khu bị giết (`startDie`) và không hồi sinh.
- Big boss (`isBigBoss()`) **không** dùng cơ chế hồi sinh này (mục 7).
- Đang bảo trì (`Maintenance.isRunning`) → không hồi sinh.

---

## 4. AI tấn công

`Mob.attack()` (dòng 321–340), gọi mỗi tick:

1. **Chọn mục tiêu** `getPlayerCanAttack()`:
   - Ưu tiên `getFirstPlayerCanAttack()`: người chơi/đệ tử **đã từng đánh quái** (`temporaryEnemies`), còn sống, không phải boss, không có vệ tinh phòng thủ bật, không tàng hình (`effectSkin.isVoHinh`), khoảng cách ≤ **300** → `timeAttack = 1000 ms`.
   - Nếu không có: quét người chơi không phải boss trong khu, khoảng cách ≤ **100**, với điều kiện quái **chủ động**: `tempId > 18` hoặc (`tempId > 9` và `type == 4`) (big boss luôn thỏa) → `timeAttack = 2000 ms`.
   - Không tấn công đệ tử mới (`isNewPet`).
2. **Điều kiện được đánh:** quái sống; không dính hiệu ứng khống chế (`isAnTroi || isBlindDCTT || isStun || isThoiMien`); tempId ∉ {0 Mộc nhân, 117 Máy đo sức mạnh, 103 Bù nhìn ma quái, 76 Cỗ máy hủy diệt}; không phải big boss; `lvMob < 1` (hoặc đang ở phó bản); đã qua `timeAttack` kể từ đòn trước.
3. **Boss đánh quái:** khi `plAtt.isBoss` và `tempId > 0`, quái đánh trả boss với xác suất 50%, tối đa 1 lần / 2,5 giây (dòng 151–154).

Tóm tắt (theo `mob_template`): tempId 1–9 và 13–18 là **bị động** – chỉ đánh khi bị đánh; Thằn lằn mẹ (10), Phi long mẹ (11), Quỷ bay mẹ (12) (TYPE 4) và mọi quái tempId ≥ 19 là **chủ động** – tự đánh người đứng trong 100px.

---

## 5. Nhận sát thương & các trường hợp đặc biệt

`Mob.injured(plAtt, damage, dieWhenHpFull)` (dòng 117–221):

| Trường hợp | Xử lý |
|---|---|
| Sát thương > HP còn lại | cắt bằng HP còn lại |
| Quái đang đầy máu và 1 đòn đủ giết (không phải map 164) | sát thương = HP − 1 (không thể "one-hit" quái đầy máu) |
| Mộc nhân (0) / Bù nhìn ma quái (103), không phải map 164 | tối đa **10% maxHp** mỗi đòn |
| Map Khí gas hủy diệt (147–152, trừ 150) còn Cỗ máy hủy diệt (76) sống | chiêu Liên hoàn, Antomic, Masenko, Kamejoko chỉ gây **1** sát thương |
| Quái `lvMob > 0` (không phải big boss, không phó bản), người đánh **không** có Bùa oai hùng (`charms.tdOaiHung`) | sát thương = 10% maxHp (nếu maxHp ≤ 20 triệu) hoặc `(int)(2 × 0,1)` = 0 (nếu > 20 triệu); quái đánh trả ngay |
| Sát thương > 2.147.483.647 | cắt về 2.147.483.647 |
| Quái chết | `status = 0`, `setDie()`, xóa `temporaryEnemies`, gửi drop (mục 9), kiểm tra nhiệm vụ (`TaskService.checkDoneTaskKillMob`, side task, clan task, `AchievementService`). Quái id 13/14 trong khu → cờ `isbulon1Alive/isbulon2Alive = false` |
| Mỗi đòn trúng (có người đánh) | Cộng tiềm năng/sức mạnh (mục 8), cộng `tnsmLuyenTap`, cộng `total_damage_maydam`; tắt vệ tinh phòng thủ của người đánh |
| Map Cadic (mapId ≥ 165) | Mỗi đòn: xác suất 1/333 (10/333 nếu `itemTime.isUseKilis`) +1 option 250 "Kilis" cho **Bình hút năng lượng** (1795) trong túi |

---

## 6. Quái tinh anh (siêu quái / `lvMob`)

`Mob.lvMob()` (dòng 469–478), gọi khi gửi gói hồi sinh:

```java
this.lvMob = (tempId > 12 && tempId < 34 && !isBigBoss()) ? (Util.isTrue(0, 10000) ? 1 : 0) : 0;
hp = lvMob > 0 ? (maxHp <= 20_000_000 ? maxHp × 10 : 2_000_000_000) : maxHp;
```

- Chỉ quái tempId 13–33, và mỗi khu tối đa 1 con.
- Tỉ lệ `Util.isTrue(0, 10000)` = **0%** → trong code hiện tại **không bao giờ xuất hiện siêu quái**.
- Nếu bật lại: HP ×10 (tối đa 2 tỷ), đánh người = 10% HP tối đa của người chơi, người không có Bùa oai hùng chỉ gây 10% maxHp mỗi đòn.

---

## 7. Big Boss map (mob_bigboss)

`Mob.isBigBoss()`: tempId ∈ {70, 71, 72, 77, 82, 83, 84, 85, 117}. Big boss không dùng AI thường, không tự hồi sinh theo mục 3. Khi bị kết liễu, chúng vẫn đi qua `Mob.injured` → `sendMobDieAffterAttacked` nên **người kết liễu vẫn được roll bảng rơi mục 9**, cộng thêm phần thưởng riêng (nếu có).

`BigBoss.update()` (`mob/BigBoss.java`): nếu Golden Frieza + 21h → chết; sau đó chỉ `effectSkill.update()` + `attack()`.

| tempId | Tên (DB) | Map xuất hiện (`map_template`) | HP (map) | Nhịp đánh | Đặc điểm (file) |
|---|---|---|---|---|---|
| 70 | Hirudegarn | 126 Thành phố Santa | 40.000.000 | 3 giây | `Hirudegarn.java` – xem 7.1 |
| 71 | Vua Bạch Tuộc | 136 Hang Bạch Tuộc | 40.732.000 | 1 giây | Chỉ đánh người có `x` trong (440, 950); `getPlayerCanAttack`. Dame theo công thức 2.2. Chết → gửi animation 7; không có code hồi sinh trong class |
| 72 | Rôbốt bảo vệ | 138 Cảng hải tặc | 40.732.000 | 1 giây | Dịch chuyển tới mục tiêu và đánh; chết → animation 6 |
| 77 | Gấu tướng cướp | Gọi bởi NPC **Giữ Ma Đầu Bò** (`npc_list/GiuMaDauBo.java`) | 2.000.000.000 | 300 ms | Xem 7.2 |
| 82 | Voi Chín Ngà | Không có trong `map_template` | — | 3 giây | Đánh 1 người trong 50/100px, nếu không có thì bay tới người ngẫu nhiên |
| 83 | Gà Chín Cựa | Không có trong `map_template` | — | 3 giây | Như Voi Chín Ngà |
| 84 | Ngựa Chín Lmao | Không có trong `map_template` | — | 3 giây | Như Voi Chín Ngà |
| 85 | Piano | Không có trong `map_template` | — | 3 giây | Action 11/12: 1 người trong 50/100px; action 13/14: **tất cả** người trong 150px |
| 117 | Máy đo sức mạnh | 42 Vách núi Aru, 43 Vách núi Moori, 44 Vách núi Kakarot | 2.000.000.000 | Không đánh | Xem 7.3 |

### 7.1 Hirudegarn

- **Nhận sát thương:** mọi đòn bị thay bằng `hp/100` (tối thiểu 1) → cần ~100+ đòn/giai đoạn, không phụ thuộc lực đánh.
- **3 giai đoạn:** chết lần 1 (`lvMob 0→1`), sau 5 giây hồi đầy máu; chết lần 2 (`1→2`) hồi đầy; chết lần 3 (`2→3`) biến mất (tọa độ −1000). Sau **15 giây** kể từ lần chết cuối → xuất hiện lại ở `x` ngẫu nhiên 100–900, `lvMob = 0`.
- **Rơi đồ mỗi lần chuyển giai đoạn** (3 lần/chu kỳ), rơi tự do (`playerId = -1`, ai nhặt cũng được):

| Vật phẩm | Số lượng / tỉ lệ |
|---|---|
| Vàng (item 190) | **30 cục**, mỗi cục 32.000 vàng (100%) |
| Quả Trứng (568) | 20% |
| Bình hút năng lượng (1795) + option 250 = 0 | 20% |
| Áo Thần Linh ngẫu nhiên (555/557/559) với bộ option lấy từ `ItemService.randDoTL` | 1/40 (2,5%) |

- **Tấn công (mỗi 3 s):** chọn action ngẫu nhiên trong {1, 2, 3, 7} (khi `lvMob ≥ 2`: {1, 2}); sau action 7 luôn là action 0. Action 1: dịch chuyển tới 1 người ngẫu nhiên và đánh người đó; action 0/2: đánh **tất cả** người trong khu; action 3: dịch chuyển. Sát thương theo công thức 2.2 (HP 40 triệu, `pDame` 5 → khoảng 1,6–2,4 triệu).

### 7.2 Gấu tướng cướp

- **Triệu hồi:** NPC Giữ Ma Đầu Bò → "Khiêu chiến Boss": người gọi phải là **bang chủ**, có ≥ 3 thành viên bang trong cùng khu. Quái được tạo tại chỗ với HP 2 tỷ, level 1, `pDame = 50`, `pTiemNang = 0`.
- **Nhận sát thương:** chỉ 5% sát thương (`damage × 0,05`, tối thiểu 1); ghi nhận tổng sát thương theo người.
- **Tấn công mỗi 300 ms:** action 11–15 chọn mục tiêu trong 50/100/150/150/200px; sát thương cố định **19.999.999**. Không có mục tiêu gần → bay tới người ngẫu nhiên.
- **Không tự hồi sinh** (update chỉ gọi attack khi còn sống).
- **Phần thưởng khi chết (`rewardContributors`):** tổng **50 Capsule bang** chia theo tỉ lệ sát thương: `floor(tỉLệ × 50)`, tối thiểu 1/người; phần dư cộng cho người gây sát thương cao nhất. Chỉ người **có bang** được tính. Mỗi capsule: `memberPoint +1`, `clanPoint +1`, `clan.capsuleClan +1`.

### 7.3 Máy đo sức mạnh

- HP 2 tỷ, **hồi đầy mỗi 1 giây** (scheduler riêng) → không thể giết.
- Mỗi **10.000.000** sát thương thực (tính dồn theo người) = **10 điểm Máy Đấm** (`point_maydam`), cập nhật BXH (`Manager.isTopMaydamChanged`).
- `percent_tiem_nang = 0`.

---

## 8. Công thức tiềm năng / sức mạnh khi đánh quái

Mỗi đòn trúng quái: `Service.addSMTN(plAtt, 2, getTiemNangForPlayer(plAtt, damage), true)` → **cộng cả sức mạnh và tiềm năng** (type 2).

### 8.1 `Mob.getTiemNangForPlayer(pl, dame)` (dòng 223–254)

```
levelPlayer = Service.getCurrLevel(pl)   // theo sức mạnh (bảng 8.3)
diff = |levelPlayer − mob.level|
tn = dame + hpFullQuái × 0,0005
nếu tempId == 0 (Mộc nhân): tn = 1
nếu diff > 5 và levelPlayer > levelQuái: tn = 1
ngược lại: tn = tn / ((int)(diff × 0,5) + 1,25)
tn = max(tn, 1)
tn = pl.nPoint.calSucManhTiemNang(tn)
```

Ví dụ hệ số chia theo chênh lệch cấp: diff 0–1 → ÷1,25; diff 2–3 → ÷2,25; diff 4–5 → ÷3,25; diff ≥ 6 (quái cấp cao hơn) → ÷4,25 …

### 8.2 `NPoint.calSucManhTiemNang(tn)` (`player/NPoint.java` dòng 1576–1658)

Nếu `power < getPowerLimit()` (chưa chạm giới hạn sức mạnh), với `tn0` = giá trị sau bước % trang bị:

| Thứ tự | Nguồn | Cộng thêm |
|---|---|---|
| 1 | `tlTNSM` (các % tiềm năng từ đồ, sao pha lê lục…) | lần lượt `tn += tn × tl/100` |
| 2 | Bùa trí tuệ (`tdTriTue`) | + tn0 (×2) |
| 3 | Bùa trí tuệ x3 (`tdTriTue3`) | + tn0 × 3 |
| 4 | Bùa trí tuệ x4 (`tdTriTue4`) | + tn0 × 4 **(bị cộng 2 lần trong code → +8 × tn0)** |
| 5 | VIP theo thời gian (`player.timevip`) | + tn0 × 3 |
| 6 | Chibi loại 2 | + tn0 × 2 |
| 7 | Tài khoản `session.vip > 0` (hoặc sư phụ của đệ tử) | + tn0 × 3 |
| 8 | Đuôi khỉ (item 1045, `isUseDK`) | + tn0 × 2 |
| 9 | Ở Bản đồ kho báu (135–138) + `isUseKhoBauX2` | + tn0 × 2 |
| 10 | Vệ tinh trí tuệ (`satellite.isIntelligent`) | + tn0 / 5 |
| 11 | Nội tại id 24 | + tn × param1 % |
| 12 | Sức mạnh ≥ 60 tỷ | − 80% |
| 13 | Đệ tử: sư phụ dùng Bùa Santa | + tn0 × 2 |
| 14 | Đệ tử: sư phụ có `tlTNSMPet` | + tn0/100 × (tlTNSMPet + 100) |
| 15 | Bản đồ kho báu (135–138) | × 1,5 |
| 16 | Đang cắm cờ (`cFlag != 0`) | cờ 8: +10%, cờ khác: +5% |
| 17 | `Manager.RATE_EXP_SERVER` | × hệ số server (đọc từ `Config.properties` `server.expserver`, hiện **= 3**) |
| 18 | `calSubTNSM` | chia theo sức mạnh: ≥ 40 tỷ ÷30, ≥ 50 tỷ ÷40, ≥ 60 tỷ ÷50, ≥ 80 tỷ ÷90, ≥ 90 tỷ ÷100 |
| 19 | | tối thiểu 1 |

Nếu `power >= getPowerLimit()` → trả **10**.

### 8.3 Cấp người chơi theo sức mạnh (`Service.getCurrLevel`)

| Sức mạnh < | Cấp | Sức mạnh < | Cấp |
|---|---|---|---|
| 3.000 | 0 | 1.500.000.000 | 10 |
| 15.000 | 1 | 5.000.000.000 | 11 |
| 40.000 | 2 | 10.000.000.000 | 12 |
| 90.000 | 3 | 40.000.000.000 | 13 |
| 170.000 | 4 | 50.010.000.000 | 14 |
| 340.000 | 5 | 60.010.000.000 | 15 |
| 700.000 | 6 | 70.010.000.000 | 16 |
| 1.500.000 | 7 | 80.010.000.000 | 17 |
| 15.000.000 | 8 | 90.010.000.000 | 18 |
| 150.000.000 | 9 | còn lại | 19 |

### 8.4 `Service.addSMTN` (dòng 946–1009)

- **Đệ tử đánh:** đệ tử nhận đủ; sư phụ nhận `param × 0,5`, qua `calSubTNSM` của sư phụ, bị cắt theo giới hạn sức mạnh.
- **Người chơi:** cắt theo `getPowerLimit()`; type 2 → `powerUp` + `tiemNangUp`; nếu có bang → `clan.addSMTNClan`.
- Song song: `TrainingService.tangTnsmLuyenTap` cộng `max(100, tn / (100 × (cấp+1)))` vào `tnsmLuyenTap` (tối đa 10.000.000) – dùng cho luyện tập offline.

---

## 9. Bảng rơi đồ khi giết quái

Nguồn: `Mob.sendMobDieAffterAttacked` → `mobReward` → `getItemMobReward(player, x, y)` (dòng 603–1071) + `dropItemTask` (dòng 1073–1098). Tất cả vật phẩm rơi **thuộc về người giết** (`playerId = player.id`). Nếu người giết có Bùa thu hút (`charms.tdThuHut`) (hoặc sư phụ của đệ tử có) → tự nhặt toàn bộ.

**Không rơi gì khi:** người giết là boss (`player.isBoss`), hoặc quái là Mộc nhân (tempId 0).

**Cờ Bôn La** (`itemTime.isUseCoBonLa`) – nhiều mục có hệ số ×1,15; vì kết quả ép kiểu `int`, với tỉ lệ gốc 1 thì ×1,15 = 1 (không đổi).

**Các nhóm map** (`map/service/MapService.java`):

| Hàm | Map id | Tên map (DB) |
|---|---|---|
| `isMap3Planets` | 0–38, **trừ** 0, 7, 14, 21, 22, 23, 24, 25, 26 | Các map 3 hành tinh thường (Đồi hoa cúc, Thung lũng tre, …) |
| `isMapUpSKH` | 1, 2, 3, 8, 9, 11, 15, 16, 17 | Đồi hoa cúc, Thung lũng tre, Rừng nấm, Đồi nấm tím, Thị trấn Moori, Thung lũng Maima, Đồi hoang, Làng Plant, Rừng nguyên sinh |
| `isMapDoanhTrai` | 53–62 | Tường thành 1 … Trại độc nhãn 3 |
| `isMapNappa` | 63–83 | Trại lính Fide … Hang khỉ đen |
| `isMapTuongLai` (int) | 92–94, 96–100, 102, 103 | Thành phố phía đông, …, Nhà Bunma, Võ đài Xên bọ hung |
| `isMapCold` (int) | 105–110 | Cánh đồng tuyết, Rừng tuyết, Núi tuyết, Dòng sông băng, Rừng băng, Hang băng |
| `isMapNguHanhSon` | 122–124 | Ngũ Hành Sơn |
| `isMapPhoBan` | 135–138 (Kho báu), 53–62 (Doanh trại), Con đường rắn độc, 147–152 trừ 150 (Khí gas) | |
| `isMapNgucTu` | 155 | Hành tinh ngục tù |
| `isMapUpPorata` | 156–159 | Tây / Đông / Bắc / Nam thánh địa |
| `isMapRiengTu` | 164 | Map riêng tư |
| `AllMap` | 0–163 | |
| `isMapCadic` | ≥ 165 | Sa mạc hoang vu, … |

### 9.1 Rơi theo nhiệm vụ

| Điều kiện | Quái | Vật phẩm | Tỉ lệ | Nguồn |
|---|---|---|---|---|
| Nhiệm vụ `TASK_2_0` | Khủng long (1), Lợn lòi (2), Quỷ đất (3) | Đùi gà (73) | 100% | `dropItemTask` |
| Nhiệm vụ `TASK_8_1` | Thằn lằn mẹ (10), Quỷ bay mẹ (12), Phi long mẹ (11) | Ngọc Rồng 7 sao (20) | 100% (`isTrue(10,10)`) | `dropItemTask` |
| Nhiệm vụ `TASK_8_1`, người chơi thật | Trái Đất: tempId 11 · Namếc: 12 · Xayda: 10 | Ngọc Rồng 7 sao (20) + `checkDoneTaskFind7Stars` | 100% | `getItemMobReward` dòng 621–626 |

### 9.2 Bảng rơi tổng hợp theo map

Mỗi dòng là 1 lần roll độc lập (1 con quái có thể rơi nhiều món).

| # | Điều kiện map / người chơi | Vật phẩm (id – tên) | Số lượng | Tỉ lệ gốc | Cờ Bôn La / điều kiện thêm | Dòng code |
|---|---|---|---|---|---|---|
| 1 | Dùng Máy dò (`isUseMayDo`), quái tempId 58–65 (Xên con cấp 1–8) | 380 – Viên Capsule kì bí | 1 | 20% | — | 614–618 |
| 2 | Map 156–159 (thánh địa) | 933 – Mảnh vỡ bông tai (option 31 = 1) | 1 | 10% | 15% | 640–643 |
| 3 | Map 156–159, nếu #2 trượt | 934 – Mảnh hồn bông tai (option 31 = 1) | 1 | 5% | 8%; giới hạn **150 lần/ngày** (`itemEvent.canDropManhVo(150)`) | 644–647 |
| 4 | Map 156–159, nếu #2 và #3 trượt | 935 – Đá xanh lam (option 31 = 1) | 1 | 1/500 (0,2%) | 2/500 | 648–651 |
| 5 | `isMap3Planets` | Vàng 500–2.999 (icon 76 nếu < 1.000; 188 nếu < 2.000; 189 còn lại) | 500–3.000 | 1/20 (5%) | — | 656–667 |
| 6 | `isMapNappa` (63–83) | Vàng 2.000–6.000 (188 < 3.000; 189 < 5.000; 190) | 2.000–6.000 | 1% | — | 668–679 |
| 7 | `AllMap` (0–163), lượt ngọc miễn phí trong ngày (`event.luotNhanNgocMienPhi == 1`) | 77 – Ngọc | 2 cục × 1 ngọc | 100% lần giết đầu tiên trong ngày, sau đó tắt lượt | reset mỗi ngày ở `PlayerService.dailyLogin` | 685–691 |
| 8 | `AllMap` | 1798 – Tayaki | 1 | 5% | — | 694–697 |
| 9 | `AllMap` | 1799 – Kẹo táo | 1 | 5% | — | 698–701 |
| 10 | `AllMap` | 1800 – Kem que đôi | 1 | 5% | — | 702–705 |
| 11 | `AllMap` | 1612 – Khúc mía | 1 | 5% | — | 706–709 |
| 12 | `AllMap` | 1801 – Mochi | 1 | 1% | — | 710–713 |
| 13 | `AllMap` | 1802 – Ramen | 1 | 1/130 (≈0,77%) | — | 714–717 |
| 14 | `isMapCold` (105–110) | Vàng (icon 190) | 150.000–250.000 | 30% | — | 720–731 |
| 15 | `isMapTuongLai` | Vàng (icon 190) | 80.000–150.000 | 15% | — | 732–743 |
| 16 | `isMapPhoBan` | Vàng (icon 190) | 80.000–200.000 | 1% | số vàng × 1,15 | 744–758 |
| 17 | Mọi map | 77 – Ngọc | 1 | 1/1.000.000 | — | 759–762 |
| 18 | `isMapUpSKH` | 1634 – Cápsule Vỡ | 1 | 1/9.999 | ×1,15 → vẫn 1 | 763–774 |
| 19 | `isMapDoanhTrai` | 1778 – Cuốn chả giò | 1 | 20% | 23% | 775–786 |
| 20 | Map 164 (riêng tư) | 1634 – Cápsule Vỡ | 1 | 1/19.999 | ×1,15 → vẫn 1 | 787–798 |
| 21 | Map 164 (riêng tư) | **Đồ kích hoạt** (xem 9.3) | 1 | `(int)tileDrop / 9.999` | xem 9.3 | 799–845 |
| 22 | `isMapUpSKH` | **Đồ kích hoạt** (xem 9.3) | 1 | `(int)tileDrop / 9.999` | xem 9.3 | 846–892 |
| 23 | `isMapUpSKH` | **Đồ sao "khác vải thô"** (xem 9.4) | 1 | 1/8.000 × 1/19.999 × 50% | — | 895–932 |
| 24 | `isMapUpSKH` | **Đồ sao 3 map đầu** (xem 9.5) | 1 | 50/50.000 × finalRate% × 49% | — | 936–973 |
| 25 | `isMapCold` (105–110) | **Đồ Thần Linh** ngẫu nhiên (`ItemService.randDoTL`) | 1 | 1/50.000 | (biến `rate` ×0,85 không được dùng) | 976–989 |
| 26 | Mọi map, người giết **mặc đủ 5 món Thần Linh** (`checkSetGod`: 5 ô body id 555–567) | 663–667 – Bánh Pudding / Xúc xích / Kem dâu / Mì ly / Sushi (ngẫu nhiên) | 1 | 3/333 (≈0,9%) | — | 994–999 |
| 27 | `isMapCold` (105–110) | Đá nâng cấp 220–224 ngẫu nhiên (kèm option 71−rand: 220 → 71 "nâng cấp rađa", 221 → 70 "giày", 222 → 69 "quần", 223 → 68 "áo", 224 → 67 "găng") | 1 | 20% | — | 1001–1008 |
| 28 | `isMapDoanhTrai` | 225 – Mảnh đá vụn (option 74) | 1 | 10% | — | 1010–1014 |
| 29 | `isMap3Planets` | 225 – Mảnh đá vụn (option 74) | 1 | 10% | — | 1016–1020 |
| 30 | `isMap3Planets` / `isMapNappa` / `isMapTuongLai` / `isMapCold` | Ngọc Rồng 6 sao (19) hoặc 7 sao (20), 50/50 | 1 | 10/70 (≈14,3%) **hoặc** (tài khoản đã kích hoạt `isActive()` và 1%) | 11/70 | 1022–1033 |
| 31 | Map 155 (Hành tinh ngục tù) + người giết **mặc đủ 5 món Hủy Diệt** | 1066–1070 – Mảnh áo/quần/giầy/nhẫn/găng tay (ngẫu nhiên) | 1 | 10% **hoặc** (kích hoạt và 2/555) | — | 1034–1038 |
| 32 | Map 155 | 1229 – Bí kíp tuyệt kỹ | 1 | 10% **hoặc** (kích hoạt + set Hủy Diệt + 20%) | — | 1039–1041 |
| 33 | Map 122–124 (Ngũ Hành Sơn) | 541 Quả Hồng Đào / 542 Quả Hồng Đào Chín | 1 | 10% | — | 1043–1045 |
| 34 | **Mọi map** (`mapId >= 0`) | Ngọc Rồng 4–7 sao (17–20) ngẫu nhiên | 1 | 1% | ×1,15 → vẫn 1% | 1047–1056 |
| 35 | **Mọi map** | Sao pha lê 441–447 ngẫu nhiên (option theo bảng 9.6) | 1 | 10% | 11% | 1058–1067 |

Ghi chú về vàng: ItemMap "vàng" có `quantity` = số vàng, icon 76/188/189/190 chỉ khác hình.

### 9.3 Đồ kích hoạt (SKH) – map 1,2,3,8,9,11,15,16,17 và map 164

```
tileDrop = 2
op236 = min(100, tổng option 236 "+#% May mắn" trên đồ đang mặc)
tileDrop *= 1 + ((op236/100)^1,5 × 20)/100          // tối đa ×1,2
nếu Cờ Bôn La: tileDrop *= 1,5
rơi nếu Util.isTrue((int)tileDrop, 9999)
```

| Trường hợp | `(int)tileDrop` | Tỉ lệ |
|---|---|---|
| Không Cờ Bôn La (mọi mức may mắn) | 2 | 2/9.999 ≈ 0,02% |
| Có Cờ Bôn La | 3 | 3/9.999 ≈ 0,03% |

Mỗi nhóm map (UpSKH và 164) roll riêng.

**Loại đồ** (`ItemService.randTempItemKichHoat(gender)`, dòng 703–720) – roll tuần tự:

| Bước | Điều kiện | Loại | Template (Trái Đất / Namếc / Xayda) |
|---|---|---|---|
| 1 | 10% | Rada | 12 Rada cấp 1 hoặc 57 Rada cấp 2 (chung) |
| 2 | 23% | Găng | TĐ {21, 24} · NM {22, 46} · XD {23, 53} |
| 3 | 23% | Quần | TĐ {6, 35} · NM {7, 43} · XD {8, 51} |
| 4 | 23% | Áo | TĐ {0, 33} · NM {1, 41} · XD {2, 49} |
| 5 | còn lại | Giày | TĐ {27, 30} · NM {28, 47} · XD {29, 55} |

Mỗi loại chọn 1 trong 2 template 50/50 (`Util.nextInt(2)`). Tỉ lệ thực: rada 10%; găng 90%×23% = 20,7%; quần ≈15,9%; áo ≈12,3%; giày ≈41,1%.

**Option** = option cơ bản theo shop (`getListOptionItemShop`) + set kích hoạt (`randOptionItemKichHoat`, dòng 749–826) + option 30 (không giao dịch):

| Hành tinh | 30%: set "Thần" 4 dòng | 70% còn lại: roll tuần tự 50% / 50% / 50% / phần còn lại |
|---|---|---|
| Trái Đất | 245, 246, 247, 248 (Thần Vũ Trụ Kaio) | 128+140 (Kirin) · 127+139 (Thên Xin Hăng) · 233+234 (Gohan) · 129+141 (Sôngôku) |
| Namếc | 237, 238, 239, 240 (Nail) | 130+142 (Picolo) · 131+143 (Ốc tiêu) · 233+234 (Gohan) · 132+144 (Pikkoro Daimao) |
| Xayda | 241, 242, 243, 244 (Cađic M) | 134+137 (Ca Đíc) · 135+138 (Nappa) · 233+234 (Gohan) · 133+136 (Kakarot) |

Tỉ lệ thực trong nhánh 70%: set 1 = 35%, set 2 = 17,5%, Gohan = 8,75%, set 4 = 8,75% (tính trên tổng).

### 9.4 Đồ sao "khác vải thô" (map UpSKH)

- Điều kiện kép: `Util.isTrue(1, 8000)` **và** `Util.isTrue(baseDropRate, 19999)` (Cờ Bôn La: `(int)(1×1,15)` = 1).
- Template: `ItemService.randTempItemDoSao(gender)` (dòng 670–701): 10% rada {58, 59, 184, 185, 186, 187}; 23% giày; 23% găng; 23% áo; còn lại quần (mảng theo hành tinh, 6 template/loại, chọn ngẫu nhiên; ví dụ áo Trái Đất {3, 34, 136, 137, 138, 139}). Lưu ý: comment trong code ghi nhầm "ao"/"gang" nhưng index thực tế trỏ tới mảng `gang`/`ao` như trên.
- Option: option shop; 50% được thêm lỗ sao pha lê (107): 50% → 1 lỗ, 40% → 2 lỗ, 10% → 3 lỗ. **Chỉ rơi nếu có lỗ** (50%).

### 9.5 Đồ sao "3 map đầu" (map UpSKH)

- Bước 1: `Util.isTrue(50, 50000)` = 0,1%.
- Bước 2: `finalRate = max(50 − (int)min(power/100.000, 5) × 20, 0)`; Cờ Bôn La: base 57.

| Sức mạnh người giết | finalRate (thường) | finalRate (Cờ Bôn La) |
|---|---|---|
| < 100.000 | 50% | 57% |
| 100.000 – 199.999 | 30% | 37% |
| 200.000 – 299.999 | 10% | 17% |
| ≥ 300.000 | 0% (không rơi) | 0% |

- Bước 3: `randOption = nextInt(1, 100) < 50` → 49% được thêm lỗ: 60% 1 lỗ, 30% 2 lỗ, 10% 3 lỗ. Chỉ rơi nếu có lỗ.
- Template: `ItemService.randDoSao(gender)` (dòng 722–747): rada 10% {12, 57}; "áo" 22% {0/33, 1/41, 2/49}; "quần" 23% {6/35, 7/43, 8/51}; "giày" 22% {27/30, 28/47, 29/55}; "găng" 23% {21/24, 22/46, 23/53}.

### 9.6 Sao pha lê rơi từ quái (dòng 1058–1067, `Util.spl`)

Mỗi viên 441–447 xác suất như nhau (1/7 của 10%):

| id | Tên | Option | Param |
|---|---|---|---|
| 441 | Sao pha lê đỏ | 95 Biến #% tấn công thành HP | 5 |
| 442 | Sao pha lê lam | 96 Biến #% tấn công thành KI | 5 |
| 443 | Sao pha lê hồng | 97 Phản #% sát thương | 5 |
| 444 | Sao pha lê tím | 99 Xuyên giáp #% cận chiến | 3 |
| 445 | Sao pha lê cam | 98 Xuyên giáp #% chưởng | 3 |
| 446 | Sao pha lê vàng | 100 +#% vàng từ quái | 5 |
| 447 | Sao pha lê lục | 101 +#% tiềm năng, sức mạnh | 5 |

### 9.7 Đồ Thần Linh rơi từ quái (`ItemService.randDoTL`, dòng 827–954)

Chọn loại (roll tuần tự): 10% Nhẫn (561); 25% Găng (562/564/566); 45% Quần (556/558/560); 75% Áo (555/557/559); còn lại Giầy (563/565/567). `tiLe = nextInt(100, 115)`.

| id | Món | Option chính |
|---|---|---|
| 555 / 557 / 559 | Áo TĐ / NM / XD | 47 Giáp = 800 / 850 / 900 × tiLe/100 |
| 556 / 558 / 560 | Quần TĐ / NM / XD | chiso = 52.000 / 50.000 / 48.000 × tiLe/100 → 22 HP K = chiso/1000, 27 HP/30s = chiso/20 |
| 562 / 564 / 566 | Găng TĐ / NM / XD | 0 Tấn công = 4.400 / 4.300 / 4.500 × tiLe/100 |
| 563 / 565 | Giầy TĐ / NM | chiso = 48.000 / 50.000 × tiLe/100 → 23 KI K = chiso/1000, 28 KI/30s = chiso/20 |
| 567 | Giầy XD | chiso = 46.000 × tiLe/100 → 23 = chiso/1000, 28 = chiso × 150/1000 |
| 561 | Nhẫn | 14 Chí mạng = 14 × tiLe/100 |

- Nếu `tiLe > 100` (trừ nhẫn): thêm option 206 "Vật phẩm hiếm rơi từ quái (+#%)" = tiLe − 100.
- 30% × 70% = 21%: thêm 1 trong option 86 (Ký gửi vàng) / 87 (Ký gửi ngọc).
- Option 21 (yêu cầu sức mạnh) = 15–17 tỉ.

---

## 10. Quái đệ tử (MobMe)

`mob/MobMe.java` – quái sinh ra từ chiêu Đẻ trứng (skill id 12):

| Thuộc tính | Giá trị |
|---|---|
| tempId | `SkillUtil.getTempMobMe(level chiêu)` |
| HP | `SkillUtil.getHPMobMe(hpMax người chơi, level)` (tối đa 2^31−1) |
| Sát thương | `SkillUtil.getHPMobMe(dame người chơi, level)` |
| Thời gian tồn tại | `SkillUtil.getTimeSurviveMobMe(level)`; **vĩnh viễn** nếu mặc đủ 5 món set Pikkoro Daimao (`setClothes.pikkoroDaimao == 5`) |
| Đánh người | chỉ khi mục tiêu còn > sát thương và > 5% HP (set Pikkoro Daimao 5 món bỏ qua điều kiện này) |
| Đánh quái | nếu HP quái > sát thương: trừ HP, cộng TNSM cho chủ theo `getTiemNangForPlayer` |

---

## 11. RewardService

`services/RewardService.java` **không** xử lý rơi đồ của quái; nó chứa các bảng phần thưởng dùng cho vòng quay, hộp quà và khởi tạo chỉ số đồ. Được gọi từ `services_func/LuckyRound.java` (3 chỗ), `services_func/UseItem.java` (17 chỗ), `npc_list/QuyLaoKame.java` (1 chỗ).

### 11.1 `getListItemLuckyRound(player, num, vip)` – Vòng quay may mắn

Tham số `vip` **không được sử dụng**. Với mỗi lượt (`num` lượt):

1. Mặc định: Vàng (189) số lượng `nextInt(5, 50) × 1000`; `success = isTrue(1, 2)`.
2. **50%** vào nhánh "VIP" – roll tuần tự:

| Bước | Tỉ lệ | Vật phẩm | Option |
|---|---|---|---|
| a | 5% | 1208 Bunma tóc xanh neon | 50, 77, 103 mỗi dòng 20–25; 154 (không bán lại); 99%: 93 HSD 3–15 ngày |
| b | 5% | 1209 Bunma tóc nâu băng đô | như trên |
| c | 5% | 1210 Bunma tóc tím thắt bím | như trên |
| d | 5% | 884 Cải trang Hit | 50, 77, 103 mỗi dòng 0–19; 5 (SĐCM) 0–29; 154; 99%: HSD 3–15 |
| e | 5% | 860 Cải trang Mị Nương | 50, 77 mỗi dòng 0–23; 117 (Đẹp +#% SĐ cho mình và người xung quanh) = 15; 154; 99%: HSD 3–15 |
| f | 1% | 955 Bó Hoa Vàng | 50, 77, 103 mỗi dòng 1–10; 99%: HSD 3–15 |
| g | 5% | 956 Mảnh Đội trưởng Vàng | (không option) |
| — | còn lại | giữ Vàng 5.000–50.000 | |

   (Trong nhánh này `itemRand` không bao giờ được gọi vì `quantity` luôn > 0.)

3. **50%** nhánh thường:

| Bước | Tỉ lệ | Vật phẩm |
|---|---|---|
| a | 50% | Đồ đeo ngẫu nhiên từ {467, 468, 469, 470, 471 (Lồng đèn…), 741 Cánh dơi Dracula, 745 Bông tuyết, 800, 801, 803, 804 (Lồng đèn), 1000 Xiên cá}; 20% đổi sang danh sách có thêm 999 Mèo mun, 1001 Phóng lợn. Option: 1 dòng ngẫu nhiên {77, 80, 81, 103, 50, 94, 5} param 5–10; 30; 93 HSD 1–30 ngày |
| b | 1% (của phần còn lại) | Ngọc Rồng 5–7 sao (18–20) × 1–5 |
| c | 1/30 | Đá nâng cấp 220–224 × 1–5 |
| d | 1% | Mảnh id 828–842 × 1–5 |

   Sau đó **luôn** gọi `itemRand(it, success)`: nếu `success == false` (50%) → thay bằng Vàng (189) `nextInt(5,12) × 1000`.

### 11.2 `rewardLancon(player)`

Cần 1 ô trống. Ngẫu nhiên 1 trong {734 Ngọc Thố, 920 Gậy như ý, 849 Pháo Thăng Thiên, 743 Chổi bay Phù Thủy, 733 Cân đẩu vân ngũ sắc}:
- 5%: 1 option {77,80,81,103,50,94,5} param 1–5 + 1 option {14,16,17,19,27,28,47,87} param 1–2 (vĩnh viễn).
- 95%: 1 option {77,…,5} param 1–10; 10%: thêm 1 option nhóm 2 param 1–10; HSD 1–30 ngày.
- Luôn thêm option 89 (Dùng để bay và phục hồi HP, KI), 30.

### 11.3 `rewardCapsuleTet(player)`

| Nhánh | Tỉ lệ | Vật phẩm & option |
|---|---|---|
| 1 | 40% | Như `rewardLancon` (734/920/849/743/733) |
| 2 | 50% × 60% = 30% | 942/943/944 (Hổ mặp vàng/trắng/xanh); option như nhánh 1 nhưng không có 89 |
| 3 | 30% | Item 2148–2152 (ngẫu nhiên; **các id này không có trong `item_template` của dump DB**, DB chỉ có id 0–1999): 5%: 77 10–20, 103 20, 50 10–20, 94 20, 14 2–20, 108 2–10, 50%: 5 1–14, 154 (vĩnh viễn); 95%: 77 10–20, 103 20, 5/30: 5 1–5, 50 10–20, 94 `nextInt(20,10)`, 14 2–10, 93 HSD 1–15. Sau đó mỗi dòng 20%: thêm 1 option {80,81,103,50,94,5} 1–5; option 108 1–10; option {14,16,17,19,27,28,47,87} 1–2 |

### 11.4 Khác

- `initChiSoItem` / `initBaseOptionClothes` (dòng 215–1670): bảng chỉ số cơ bản (giáp, HP, KI, hồi phục, sức đánh, chí mạng) cho từng template quần áo – dùng khi tạo đồ.
- `initActivationOption(gender, type, list)`: gắn 1 set kích hoạt ngẫu nhiên trong 3 set/hành tinh (`ACTIVATION_SET`): TĐ Sôngôku/Thên Xin Hăng/Kirin, NM Ốc tiêu/Pikkoro Daimao/Picolo, XD Kakarot/Ca Đíc/Nappa; kèm option 30 = 7.

---

## 12. Danh sách `mob_template`

Cột: id · tên · TYPE · HP mẫu · range_move · speed · percent_dame · percent_tiem_nang.

| id | Tên | TYPE | HP (mẫu) | range_move | speed | % dame | % tiềm năng |
|---|---|---|---|---|---|---|---|
| 0 | Mộc nhân | 0 | 20 | 0 | 1 | 5 | 10 |
| 1 | Khủng long | 1 | 200 | 33 | 1 | 5 | 50 |
| 2 | Lợn lòi | 1 | 200 | 33 | 1 | 5 | 50 |
| 3 | Quỷ đất | 1 | 200 | 33 | 1 | 5 | 50 |
| 4 | Khủng long mẹ | 1 | 500 | 33 | 2 | 5 | 50 |
| 5 | Lợn lòi mẹ | 1 | 500 | 33 | 1 | 5 | 50 |
| 6 | Quỷ đất mẹ | 1 | 500 | 33 | 2 | 5 | 50 |
| 7 | Thằn lằn bay | 4 | 600 | 33 | 1 | 5 | 50 |
| 8 | Phi long | 4 | 600 | 33 | 2 | 5 | 50 |
| 9 | Quỷ bay | 4 | 600 | 33 | 1 | 5 | 50 |
| 10 | Thằn lằn mẹ | 4 | 1000 | 33 | 2 | 5 | 50 |
| 11 | Phi long mẹ | 4 | 1000 | 33 | 1 | 5 | 50 |
| 12 | Quỷ bay mẹ | 4 | 1000 | 33 | 2 | 5 | 50 |
| 13 | Ốc mượn hồn | 1 | 3000 | 33 | 2 | 5 | 25 |
| 14 | Ốc sên | 1 | 3000 | 33 | 2 | 5 | 25 |
| 15 | Heo Xayda mẹ | 1 | 3000 | 33 | 2 | 5 | 25 |
| 16 | Heo rừng | 1 | 1500 | 33 | 1 | 5 | 25 |
| 17 | Heo da xanh | 1 | 1500 | 33 | 1 | 5 | 25 |
| 18 | Heo Xayda | 1 | 1500 | 33 | 1 | 5 | 25 |
| 19 | Heo rừng mẹ | 1 | 12000 | 33 | 2 | 5 | 25 |
| 20 | Heo xanh mẹ | 1 | 12000 | 33 | 2 | 5 | 25 |
| 21 | Alien | 4 | 12000 | 33 | 2 | 5 | 25 |
| 22 | Bulon | 1 | 6000 | 33 | 2 | 5 | 25 |
| 23 | Ukulele | 1 | 6000 | 33 | 2 | 5 | 25 |
| 24 | Quỷ mập | 1 | 6000 | 33 | 2 | 5 | 25 |
| 25 | Tambourine | 4 | 20000 | 33 | 2 | 5 | 25 |
| 26 | Drum | 1 | 20000 | 33 | 2 | 5 | 25 |
| 27 | Akkuman | 1 | 20000 | 33 | 2 | 5 | 25 |
| 28 | Thằn lằn bay 2 | 4 | 1500 | 33 | 2 | 5 | 25 |
| 29 | Phi long 2 | 4 | 1500 | 33 | 2 | 5 | 25 |
| 30 | Quỷ bay 2 | 4 | 1500 | 33 | 2 | 5 | 25 |
| 31 | Không tặc | 4 | 3000 | 33 | 2 | 5 | 25 |
| 32 | Quỷ đầu to | 4 | 3000 | 33 | 2 | 5 | 25 |
| 33 | Quỷ địa ngục | 4 | 3000 | 33 | 2 | 5 | 25 |
| 34 | Lính độc nhãn | 1 | 30000 | 33 | 2 | 5 | 25 |
| 35 | Lính độc nhãn | 1 | 30000 | 33 | 2 | 5 | 25 |
| 36 | Sói xám | 1 | 30000 | 33 | 2 | 5 | 25 |
| 37 | Robot bay | 4 | 30000 | 33 | 2 | 5 | 25 |
| 38 | Robot thép | 1 | 30000 | 33 | 2 | 5 | 25 |
| 39 | Nappa | 1 | 40000 | 33 | 2 | 5 | 10 |
| 40 | Soldier | 1 | 50000 | 33 | 2 | 5 | 10 |
| 41 | Appule | 1 | 60000 | 33 | 2 | 5 | 10 |
| 42 | Raspberry | 1 | 70000 | 33 | 2 | 5 | 10 |
| 43 | Thằn lằn xanh | 4 | 80000 | 33 | 2 | 5 | 10 |
| 44 | Quỷ đầu nhọn | 1 | 90000 | 33 | 2 | 5 | 10 |
| 45 | Quỷ đầu vàng | 1 | 100000 | 33 | 2 | 5 | 10 |
| 46 | Quỷ da tím | 1 | 110000 | 33 | 2 | 5 | 10 |
| 47 | Quỷ già | 1 | 120000 | 33 | 2 | 5 | 10 |
| 48 | Cá sấu | 1 | 130000 | 33 | 2 | 5 | 10 |
| 49 | Dơi da xanh | 4 | 140000 | 33 | 2 | 5 | 10 |
| 50 | Quỷ chim | 4 | 180000 | 33 | 2 | 5 | 10 |
| 51 | Lính đầu trọc | 1 | 150000 | 33 | 1 | 5 | 10 |
| 52 | Lính tai dài | 1 | 160000 | 33 | 2 | 5 | 10 |
| 53 | Lính vũ trụ | 1 | 170000 | 33 | 2 | 5 | 10 |
| 54 | Khỉ lông đen | 1 | 300000 | 33 | 2 | 5 | 10 |
| 55 | Khỉ giáp sắt | 1 | 350000 | 33 | 2 | 5 | 10 |
| 56 | Khỉ lông đỏ | 1 | 400000 | 33 | 2 | 5 | 10 |
| 57 | Khỉ lông vàng | 1 | 450000 | 33 | 2 | 5 | 10 |
| 58 | Xên con cấp 1 | 1 | 200000 | 33 | 3 | 5 | 10 |
| 59 | Xên con cấp 2 | 1 | 250000 | 33 | 3 | 5 | 10 |
| 60 | Xên con cấp 3 | 1 | 300000 | 33 | 3 | 5 | 10 |
| 61 | Xên con cấp  4 | 1 | 350000 | 33 | 3 | 5 | 10 |
| 62 | Xên con cấp  5 | 1 | 400000 | 33 | 3 | 5 | 10 |
| 63 | Xên con cấp  6 | 1 | 450000 | 33 | 3 | 5 | 10 |
| 64 | Xên con cấp  7 | 1 | 500000 | 33 | 3 | 5 | 10 |
| 65 | Xên con cấp  8 | 1 | 550000 | 33 | 3 | 5 | 10 |
| 66 | Tai tím | 1 | 350000 | 33 | 2 | 5 | 10 |
| 67 | Abo | 1 | 400000 | 33 | 2 | 5 | 10 |
| 68 | Kado | 1 | 450000 | 33 | 2 | 5 | 10 |
| 69 | Da xanh | 4 | 500000 | 33 | 2 | 5 | 10 |
| 70 | Hirudegarn | 1 | 40000000 | 33 | 1 | 5 | 10 |
| 71 | Vua Bạch Tuộc | 1 | 1500000 | 33 | 1 | 5 | 10 |
| 72 | Rôbốt bảo vệ | 1 | 1000000 | 33 | 1 | 5 | 10 |
| 73 | Kawazu | 1 | 50000 | 33 | 2 | 5 | 10 |
| 74 | Kinkarn | 1 | 55000 | 33 | 2 | 5 | 10 |
| 75 | Arbee | 4 | 60000 | 33 | 2 | 5 | 10 |
| 76 | Cỗ máy hủy diệt | 0 | 80000000 | 0 | 1 | 5 | 10 |
| 77 | Gấu tướng cướp | 1 | 2000000000 | 33 | 2 | 5 | 10 |
| 78 | Khỉ lông xanh | 1 | 2000000 | 33 | 2 | 5 | 10 |
| 79 | Taburine Đỏ | 4 | 3000000 | 33 | 3 | 5 | 10 |
| 80 | Cabira | 1 | 4000000 | 33 | 2 | 5 | 10 |
| 81 | Tobi | 1 | 5000000 | 33 | 2 | 5 | 10 |
| 82 | Voi Chín Ngà | 1 | 20000000 | 33 | 2 | 5 | 10 |
| 83 | Gà Chín Cựa | 1 | 12000000 | 33 | 2 | 5 | 10 |
| 84 | Ngựa Chín Lmao | 1 | 15000000 | 33 | 2 | 5 | 10 |
| 85 | Piano | 1 | 2000000000 | 33 | 1 | 5 | 10 |
| 86 | Ếch mặt đỏ | 1 | 4000000 | 33 | 2 | 5 | 10 |
| 87 | Jinai | 4 | 6000000 | 33 | 2 | 5 | 10 |
| 88 | Quỷ đỏ | 1 | 1000000 | 33 | 2 | 5 | 10 |
| 89 | Quỷ xanh | 1 | 1500000 | 33 | 2 | 5 | 10 |
| 90 | Quỷ xanh lá | 1 | 1000000 | 33 | 2 | 5 | 10 |
| 91 | Quỷ vàng | 1 | 1500000 | 33 | 2 | 5 | 10 |
| 92 | Godzila | 1 | 100000000 | 33 | 2 | 5 | 10 |
| 93 |  | 1 | 1 | 33 | 2 | 5 | 10 |
| 94 | Toppo | 1 | 4000000 | 33 | 1 | 5 | 10 |
| 95 | Cadic M | 1 | 6000000 | 0 | 2 | 5 | 10 |
| 96 | Janemba | 1 | 4000000 | 33 | 1 | 5 | 10 |
| 97 | MEZ | 1 | 4000000 | 33 | 1 | 5 | 10 |
| 98 | GOZ | 1 | 4000000 | 33 | 1 | 5 | 10 |
| 99 | Đá đỏ | 1 | 4000000 | 33 | 1 | 5 | 10 |
| 100 | Đá vàng | 1 | 4000000 | 33 | 1 | 5 | 10 |
| 101 | Đá xanh | 1 | 4000000 | 33 | 1 | 5 | 10 |
| 102 | Thây ma | 1 | 4000000 | 33 | 1 | 5 | 10 |
| 103 | Bù nhìn ma quái | 0 | 20 | 0 | 1 | 5 | 10 |
| 104 | Phù thủy | 4 | 4000000 | 33 | 1 | 5 | 10 |
| 105 | Frostbite | 1 | 500 | 33 | 2 | 5 | 10 |
| 106 | Snowy Tangerine | 1 | 550 | 33 | 2 | 5 | 10 |
| 107 | Deinonychus | 1 | 550 | 33 | 2 | 5 | 10 |
| 108 | Snake | 1 | 550 | 33 | 2 | 5 | 10 |
| 109 | Blizzard bird | 1 | 550 | 33 | 2 | 5 | 10 |
| 110 | Snowman | 1 | 55 | 33 | 2 | 5 | 10 |
| 111 | Yeti | 1 | 11 | 33 | 2 | 5 | 10 |
| 112 | Grim Reaper | 1 | 11 | 33 | 2 | 5 | 10 |
| 113 | Demon 3 | 1 | 11 | 33 | 2 | 5 | 10 |
| 114 | Golem | 1 | 11 | 50 | 2 | 5 | 10 |
| 115 | Dazing Stone | 1 | 11 | 50 | 2 | 5 | 10 |
| 116 | Demon 2 | 1 | 11 | 50 | 2 | 5 | 10 |
| 117 | Máy đo sức mạnh | 1 | 2000000000 | 1 | 1 | 5 | 0 |
| 118 | Cadic M | 1 | 6000000 | 33 | 2 | 5 | 10 |

Ghi chú: cột HP ở đây là HP mẫu trong `mob_template`; HP thật của quái trên map lấy theo mục 13. DB có đủ 119 bản ghi, id 0–118 (tên trong DB có thể khác tên hằng trong `ConstMob`, ví dụ id 95 trong DB là "Cadic M" còn `ConstMob.THO_CON = 95`).

---

## 13. Bố trí quái theo map

Trích từ cột `mobs` của `map_template` (mỗi phần tử `[tempId, level, hp, x, y]`, gộp các con giống hệt nhau; "x N" = số con trong **mỗi khu**). Cột "planet_id" là giá trị thô trong DB. Chỉ liệt kê map có quái.

| Map id | Tên map | planet_id | Quái (tên – tempId – level – HP) × số lượng |
|---|---|---|---|
| 0 | Làng Aru | 0 | Mộc nhân (id 0, lv 1, HP 100) x4 |
| 1 | Đồi hoa cúc | 0 | Khủng long (id 1, lv 2, HP 200) x4 |
| 2 | Thung lũng tre | 0 | Khủng long mẹ (id 4, lv 3, HP 500) x2; Khủng long (id 1, lv 2, HP 200) x2 |
| 3 | Rừng nấm | 0 | Khủng long (id 1, lv 2, HP 200) x3; Khủng long mẹ (id 4, lv 3, HP 500) x1; Thằn lằn bay (id 7, lv 4, HP 600) x4 |
| 4 | Rừng xương | 0 | Khủng long mẹ (id 4, lv 3, HP 500) x2; Thằn lằn bay (id 7, lv 4, HP 600) x3; Thằn lằn mẹ (id 10, lv 5, HP 1,000) x2 |
| 5 | Đảo Kamê | 0 | Ốc mượn hồn (id 13, lv 1, HP 2,000) x3 |
| 6 | Đông Karin | 0 | Heo rừng mẹ (id 19, lv 8, HP 12,000) x3; Tambourine (id 25, lv 8, HP 20,000) x1 |
| 7 | Làng Mori | 1 | Mộc nhân (id 0, lv 1, HP 100) x4 |
| 8 | Đồi nấm tím | 1 | Lợn lòi (id 2, lv 2, HP 200) x4 |
| 9 | Thị trấn Moori | 1 | Lợn lòi (id 2, lv 2, HP 200) x3; Lợn lòi mẹ (id 5, lv 3, HP 500) x2 |
| 10 | Thung lũng Namếc | 1 | Heo xanh mẹ (id 20, lv 8, HP 12,000) x3; Drum (id 26, lv 8, HP 20,000) x1 |
| 11 | Thung lũng Maima | 1 | Lợn lòi (id 2, lv 2, HP 200) x3; Lợn lòi mẹ (id 5, lv 3, HP 500) x1; Phi long (id 8, lv 4, HP 600) x4 |
| 12 | Vực maima | 1 | Lợn lòi mẹ (id 5, lv 3, HP 500) x2; Phi long (id 8, lv 4, HP 600) x3; Phi long mẹ (id 11, lv 5, HP 1,000) x2 |
| 13 | Đảo Guru | 1 | Ốc sên (id 14, lv 7, HP 3,000) x3 |
| 14 | Làng Kakarot | 2 | Mộc nhân (id 0, lv 1, HP 100) x4 |
| 15 | Đồi hoang | 2 | Quỷ đất (id 3, lv 2, HP 200) x5 |
| 16 | Làng Plant | 2 | Quỷ đất mẹ (id 6, lv 3, HP 500) x2; Quỷ đất (id 3, lv 2, HP 200) x2 |
| 17 | Rừng nguyên sinh | 2 | Quỷ đất (id 3, lv 2, HP 200) x3; Quỷ đất mẹ (id 6, lv 3, HP 500) x1; Quỷ bay (id 9, lv 4, HP 600) x4 |
| 18 | Rừng thông Xayda | 2 | Quỷ đất mẹ (id 6, lv 3, HP 500) x2; Quỷ bay (id 9, lv 4, HP 600) x3; Quỷ bay mẹ (id 12, lv 5, HP 1,000) x2 |
| 19 | Thành phố Vegeta | 2 | Akkuman (id 27, lv 8, HP 20,000) x1; Alien (id 21, lv 8, HP 12,000) x3 |
| 20 | Vách núi đen | 2 | Heo Xayda mẹ (id 15, lv 7, HP 3,000) x3 |
| 27 | Rừng Bamboo | 0 | Heo rừng (id 16, lv 6, HP 1,500) x4; Thằn lằn mẹ (id 10, lv 5, HP 1,000) x2 |
| 28 | Rừng dương xỉ | 0 | Heo rừng (id 16, lv 6, HP 1,500) x6; Thằn lằn mẹ (id 10, lv 5, HP 1,000) x4 |
| 29 | Nam Kamê | 0 | Ốc mượn hồn (id 13, lv 7, HP 3,000) x5; Không tặc (id 31, lv 7, HP 3,000) x2 |
| 30 | Đảo Bulông | 0 | Bulon (id 22, lv 8, HP 6,000) x4; Không tặc (id 31, lv 7, HP 3,000) x2 |
| 31 | Núi hoa vàng | 1 | Heo da xanh (id 17, lv 6, HP 1,500) x4; Phi long mẹ (id 11, lv 5, HP 1,000) x2 |
| 32 | Núi hoa tím | 1 | Heo da xanh (id 17, lv 6, HP 1,500) x5; Phi long mẹ (id 11, lv 5, HP 1,000) x4 |
| 33 | Nam Guru | 1 | Ốc sên (id 14, lv 7, HP 3,000) x5; Quỷ đầu to (id 32, lv 7, HP 3,000) x2 |
| 34 | Đông Nam Guru | 1 | Ukulele (id 23, lv 8, HP 6,000) x4; Quỷ đầu to (id 32, lv 7, HP 3,000) x2 |
| 35 | Rừng cọ | 2 | Heo Xayda (id 18, lv 6, HP 1,500) x4; Quỷ bay mẹ (id 12, lv 5, HP 1,000) x2 |
| 36 | Rừng đá | 2 | Heo Xayda (id 18, lv 6, HP 1,500) x5; Quỷ bay mẹ (id 12, lv 5, HP 1,000) x4 |
| 37 | Thung lũng đen | 2 | Heo Xayda mẹ (id 15, lv 7, HP 3,000) x5; Quỷ địa ngục (id 33, lv 7, HP 3,000) x2 |
| 38 | Bờ vực đen | 2 | Quỷ mập (id 24, lv 8, HP 6,000) x4; Quỷ địa ngục (id 33, lv 7, HP 3,000) x2 |
| 42 | Vách núi Aru | 0 | Máy đo sức mạnh (id 117, lv 1, HP 2,000,000,000) x1 |
| 43 | Vách núi Moori | 1 | Máy đo sức mạnh (id 117, lv 1, HP 2,000,000,000) x1 |
| 44 | Vách núi Kakarot | 2 | Máy đo sức mạnh (id 117, lv 1, HP 2,000,000,000) x1 |
| 52 | Đại hội võ thuật | 2 | Mộc nhân (id 0, lv 1, HP 100) x2 |
| 53 | Tường thành 1 | 2 | Lính độc nhãn (id 34, lv 8, HP 6,640) x7 |
| 55 | Tầng 1 | 0 | Lính độc nhãn (id 35, lv 8, HP 30,000) x4 |
| 56 | Tầng 2 | 0 | Lính độc nhãn (id 34, lv 8, HP 30,000) x2; Lính độc nhãn (id 35, lv 8, HP 30,000) x2; Robot thép (id 38, lv 8, HP 30,000) x1 |
| 57 | Tầng 4 | 0 | Robot thép (id 38, lv 8, HP 30,000) x5 |
| 58 | Tường thành 2 | 0 | Lính độc nhãn (id 34, lv 8, HP 30,000) x4; Lính độc nhãn (id 35, lv 8, HP 30,000) x3; Không tặc (id 31, lv 7, HP 3,000) x5 |
| 59 | Tường thành 3 | 0 | Lính độc nhãn (id 35, lv 8, HP 30,000) x6; Không tặc (id 31, lv 7, HP 3,000) x7; Bulon (id 22, lv 8, HP 6,000) x2 |
| 60 | Trại độc nhãn 1 | 0 | Lính độc nhãn (id 35, lv 8, HP 30,000) x3; Sói xám (id 36, lv 8, HP 30,000) x5 |
| 61 | Trại độc nhãn 2 | 0 | Sói xám (id 36, lv 8, HP 30,000) x10; Robot bay (id 37, lv 8, HP 30,000) x5 |
| 62 | Trại độc nhãn 3 | 0 | Lính độc nhãn (id 34, lv 8, HP 30,000) x2; Sói xám (id 36, lv 8, HP 30,000) x6; Lính độc nhãn (id 35, lv 8, HP 30,000) x3; Robot bay (id 37, lv 8, HP 30,000) x13 |
| 63 | Trại lính Fide | 2 | Quỷ đầu vàng (id 45, lv 10, HP 100,000) x8; Quỷ da tím (id 46, lv 10, HP 110,000) x7; Dơi da xanh (id 49, lv 11, HP 140,000) x1 |
| 64 | Núi dây leo | 2 | Quỷ đầu nhọn (id 44, lv 10, HP 90,000) x6; Thằn lằn xanh (id 43, lv 10, HP 80,000) x5 |
| 65 | Núi cây quỷ | 2 | Quỷ đầu nhọn (id 44, lv 10, HP 90,000) x3; Quỷ đầu vàng (id 45, lv 10, HP 100,000) x3; Thằn lằn xanh (id 43, lv 10, HP 80,000) x3 |
| 66 | Trại qủy già | 2 | Quỷ da tím (id 46, lv 10, HP 110,000) x5; Quỷ già (id 47, lv 11, HP 120,000) x3 |
| 67 | Vực chết | 2 | Quỷ già (id 47, lv 11, HP 120,000) x4; Dơi da xanh (id 49, lv 11, HP 140,000) x8 |
| 68 | Thung lũng Nappa | 2 | Nappa (id 39, lv 9, HP 40,000) x7; Tambourine (id 25, lv 8, HP 20,000) x2 |
| 69 | Vực cấm | 2 | Nappa (id 39, lv 9, HP 40,000) x4; Soldier (id 40, lv 9, HP 50,000) x2; Tambourine (id 25, lv 8, HP 20,000) x2 |
| 70 | Núi Appule | 2 | Nappa (id 39, lv 9, HP 40,000) x3; Soldier (id 40, lv 9, HP 50,000) x6; Appule (id 41, lv 9, HP 60,000) x3 |
| 71 | Căn cứ Raspberry | 2 | Appule (id 41, lv 9, HP 60,000) x8; Raspberry (id 42, lv 9, HP 70,000) x3 |
| 72 | Thung lũng Raspberry | 2 | Appule (id 41, lv 9, HP 60,000) x6; Raspberry (id 42, lv 9, HP 70,000) x1; Thằn lằn xanh (id 43, lv 10, HP 80,000) x3 |
| 73 | Thung lũng chết | 2 | Cá sấu (id 48, lv 11, HP 130,000) x8; Dơi da xanh (id 49, lv 11, HP 140,000) x1 |
| 74 | Đồi cây Fide | 2 | Lính đầu trọc (id 51, lv 11, HP 150,000) x4; Lính tai dài (id 52, lv 12, HP 160,000) x2; Dơi da xanh (id 49, lv 11, HP 140,000) x2 |
| 75 | Khe núi tử thần | 2 | Lính tai dài (id 52, lv 12, HP 160,000) x6; Lính vũ trụ (id 53, lv 12, HP 170,000) x1; Lính đầu trọc (id 51, lv 11, HP 150,000) x2 |
| 76 | Núi đá | 2 | Lính tai dài (id 52, lv 12, HP 160,000) x4; Lính vũ trụ (id 53, lv 12, HP 170,000) x2; Quỷ chim (id 50, lv 12, HP 180,000) x2 |
| 77 | Rừng đá | 2 | Lính vũ trụ (id 53, lv 12, HP 170,000) x6; Quỷ chim (id 50, lv 12, HP 180,000) x4 |
| 78 | Lãnh địa Fize | 0 | Mộc nhân (id 0, lv 1, HP 100) x4 |
| 79 | Núi khỉ đỏ | 2 | Khỉ lông đỏ (id 56, lv 15, HP 400,000) x8 |
| 80 | Núi khỉ vàng | 2 | Khỉ lông vàng (id 57, lv 16, HP 450,000) x7 |
| 81 | Hang quỷ chim | 2 | Quỷ chim (id 50, lv 12, HP 180,000) x4; Lính vũ trụ (id 53, lv 12, HP 170,000) x3; Khỉ lông đen (id 54, lv 13, HP 300,000) x2 |
| 82 | Núi khỉ đen | 2 | Khỉ lông đen (id 54, lv 13, HP 300,000) x9; Khỉ giáp sắt (id 55, lv 14, HP 350,000) x1; Quỷ chim (id 50, lv 12, HP 180,000) x2 |
| 83 | Hang khỉ đen | 2 | Khỉ giáp sắt (id 55, lv 14, HP 350,000) x9; Khỉ lông đỏ (id 56, lv 15, HP 400,000) x1 |
| 85 | Hành tinh M-2 | 2 | Khỉ lông vàng (id 57, lv 16, HP 450,000) x3; Quỷ chim (id 50, lv 12, HP 180,000) x1 |
| 86 | Hành tinh Polaris | 2 | Quỷ chim (id 50, lv 12, HP 180,000) x4 |
| 87 | Hành tinh Cretaceous | 2 | Khỉ lông đỏ (id 56, lv 15, HP 400,000) x1; Khỉ lông vàng (id 57, lv 16, HP 450,000) x1; Quỷ chim (id 50, lv 12, HP 180,000) x2 |
| 88 | Hành tinh Monmaasu | 2 | Quỷ chim (id 50, lv 12, HP 180,000) x4; Khỉ lông đỏ (id 56, lv 15, HP 400,000) x2 |
| 89 | Hành tinh Rudeeze | 2 | Khỉ lông vàng (id 57, lv 16, HP 450,000) x2 |
| 90 | Hành tinh Gelbo | 2 | Khỉ lông vàng (id 57, lv 16, HP 450,000) x2; Quỷ chim (id 50, lv 12, HP 180,000) x2 |
| 91 | Hành tinh Tigere | 2 | Khỉ lông đỏ (id 56, lv 15, HP 400,000) x2; Khỉ lông vàng (id 57, lv 16, HP 450,000) x2 |
| 92 | Thành phố phía đông | 2 | Xên con cấp 1 (id 58, lv 11, HP 200,000) x6 |
| 93 | Thành phố phía nam | 2 | Xên con cấp 2 (id 59, lv 12, HP 250,000) x7 |
| 94 | Đảo Balê | 2 | Xên con cấp 3 (id 60, lv 13, HP 300,000) x7 |
| 96 | Cao nguyên | 2 | Xên con cấp  4 (id 61, lv 14, HP 350,000) x4 |
| 97 | Thành phố phía bắc | 2 | Xên con cấp  5 (id 62, lv 15, HP 400,000) x6 |
| 98 | Ngọn núi phía bắc | 2 | Xên con cấp  6 (id 63, lv 16, HP 450,000) x6 |
| 99 | Thung lũng phía bắc | 2 | Xên con cấp  7 (id 64, lv 17, HP 500,000) x8 |
| 100 | Thị trấn Ginder | 2 | Xên con cấp  8 (id 65, lv 18, HP 550,000) x9 |
| 105 | Cánh đồng tuyết | 2 | Tai tím (id 66, lv 19, HP 1,350,000) x8 |
| 106 | Rừng tuyết | 2 | Tai tím (id 66, lv 19, HP 350,000) x5; Abo (id 67, lv 20, HP 1,400,000) x2 |
| 107 | Núi tuyết | 2 | Abo (id 67, lv 20, HP 1,400,000) x10 |
| 108 | Dòng sông băng | 2 | Abo (id 67, lv 20, HP 1,400,000) x8; Kado (id 68, lv 21, HP 1,450,000) x1 |
| 109 | Rừng băng | 2 | Kado (id 68, lv 21, HP 1,450,000) x10; Da xanh (id 69, lv 22, HP 1,500,000) x1 |
| 110 | Hang băng | 2 | Kado (id 68, lv 21, HP 1,450,000) x4; Da xanh (id 69, lv 22, HP 1,500,000) x7 |
| 122 | Ngũ Hành Sơn | 2 | Quỷ chim (id 50, lv 20, HP 500,000) x3; Khỉ lông vàng (id 57, lv 20, HP 500,000) x3 |
| 123 | Ngũ Hành Sơn | 2 | Khỉ lông đỏ (id 56, lv 20, HP 400,000) x4 |
| 124 | Ngũ Hành Sơn | 2 | Khỉ lông vàng (id 57, lv 20, HP 450,000) x4 |
| 126 | Thành phố Santa | 2 | Hirudegarn (id 70, lv 0, HP 40,000,000) x1; Quỷ chim (id 50, lv 12, HP 180,000) x6 |
| 135 | Động hải tặc | 0 | Lính độc nhãn (id 34, lv 8, HP 34,200) x4; Lính độc nhãn (id 35, lv 8, HP 34,200) x5; Lính độc nhãn (id 35, lv 8, HP 410,400) x1; Sói xám (id 36, lv 8, HP 410,400) x1; Sói xám (id 36, lv 8, HP 34,200) x2 |
| 136 | Hang Bạch Tuộc | 0 | Sói xám (id 36, lv 8, HP 36,252) x4; Robot bay (id 37, lv 8, HP 36,252) x2; Robot bay (id 37, lv 8, HP 435,024) x1; Vua Bạch Tuộc (id 71, lv 0, HP 40,732,000) x1 |
| 137 | Động kho báu | 0 | Robot bay (id 37, lv 8, HP 38,427) x1; Robot thép (id 38, lv 8, HP 38,427) x6; Robot thép (id 38, lv 8, HP 461,124) x1 |
| 138 | Cảng hải tặc | 0 | Robot bay (id 37, lv 8, HP 40,732) x4; Rôbốt bảo vệ (id 72, lv 0, HP 40,732,000) x1 |
| 141 | Con đường rắn độc | 0 | Quỷ mập (id 24, lv 8, HP 1,300) x4; Quỷ địa ngục (id 33, lv 8, HP 1,300) x3 |
| 142 | Con đường rắn độc | 0 | Drum (id 26, lv 8, HP 1,150) x4; Tambourine (id 25, lv 8, HP 1,150) x2; Tambourine (id 25, lv 8, HP 10,350) x1 |
| 143 | Con đường rắn độc | 0 | Dơi da xanh (id 49, lv 11, HP 1,000) x4; Quỷ chim (id 50, lv 12, HP 1,000) x2; Quỷ chim (id 50, lv 12, HP 9,000) x1 |
| 146 | Tây Karin | 0 | Quỷ đất mẹ (id 6, lv 3, HP 600,730) x1; Tambourine (id 25, lv 8, HP 1,501,827) x1; Drum (id 26, lv 8, HP 901,096) x1 |
| 147 | Sa mạc | 0 | Cỗ máy hủy diệt (id 76, lv 10, HP 84,800) x2; Kawazu (id 73, lv 9, HP 53,000) x5; Kawazu (id 73, lv 9, HP 636,000) x2; Arbee (id 75, lv 9, HP 63,600) x7; Arbee (id 75, lv 9, HP 763,200) x1 |
| 148 | Lâu đài Lychee | 2 | Kawazu (id 73, lv 9, HP 63,123) x3; Kinkarn (id 74, lv 9, HP 69,435) x2; Kinkarn (id 74, lv 9, HP 833,220) x1; Arbee (id 75, lv 9, HP 75,747) x4; Arbee (id 75, lv 9, HP 908,964) x1 |
| 149 | Thành phố Santa | 2 | Kinkarn (id 74, lv 9, HP 55,000) x6; Kinkarn (id 74, lv 9, HP 5,500) x1; Cỗ máy hủy diệt (id 76, lv 10, HP 80,000,000) x1; Arbee (id 75, lv 9, HP 60,000) x5; Arbee (id 75, lv 9, HP 720,000) x1 |
| 151 | Hành tinh bóng tối | 2 | Cỗ máy hủy diệt (id 76, lv 10, HP 95,281) x1; Kawazu (id 73, lv 9, HP 59,550) x2; Kinkarn (id 74, lv 9, HP 65,505) x2; Arbee (id 75, lv 9, HP 857,520) x1; Arbee (id 75, lv 9, HP 71,460) x3 |
| 152 | Vùng đất băng giá | 2 | Cỗ máy hủy diệt (id 76, lv 10, HP 89,888) x2; Kawazu (id 73, lv 9, HP 56,180) x7; Kawazu (id 73, lv 9, HP 674,160) x1; Kinkarn (id 74, lv 9, HP 61,798) x5; Kinkarn (id 74, lv 9, HP 741,576) x2; Arbee (id 75, lv 9, HP 67,416) x11; Arbee (id 75, lv 9, HP 808,992) x2 |
| 155 | Hành tinh ngục tù | 0 | Taburine Đỏ (id 79, lv 1, HP 3,000,000) x5; Khỉ lông xanh (id 78, lv 1, HP 2,000,000) x7 |
| 156 | Tây thánh địa | 2 | Nappa (id 39, lv 20, HP 7,250,000) x7; Dơi da xanh (id 49, lv 20, HP 5,750,000) x5 |
| 157 | Đông thánh Địa | 2 | Soldier (id 40, lv 21, HP 7,000,000) x6; Thằn lằn xanh (id 43, lv 21, HP 10,150,000) x2; Quỷ chim (id 50, lv 21, HP 9,100,000) x2 |
| 158 | Bắc thánh địa | 2 | Abo (id 67, lv 21, HP 13,050,000) x3; Tai tím (id 66, lv 21, HP 11,700,000) x4; Da xanh (id 69, lv 21, HP 10,350,000) x4 |
| 159 | Nam thánh Địa | 2 | Kado (id 68, lv 22, HP 12,000,000) x9; Da xanh (id 69, lv 22, HP 13,800,000) x5 |
| 160 | Khu hang động | 2 | Cabira (id 80, lv 21, HP 4,000,000) x9; Tobi (id 81, lv 21, HP 5,000,000) x9 |
| 161 | Bìa rừng nguyên thủy | 2 | Cabira (id 80, lv 21, HP 4,000,000) x9; Tobi (id 81, lv 21, HP 5,000,000) x8 |
| 162 | Rừng nguyên thủy | 2 | Cabira (id 80, lv 21, HP 4,000,000) x8; Tobi (id 81, lv 21, HP 5,000,000) x11 |
| 163 | Làng Plant nguyên thủy | 2 | Cabira (id 80, lv 21, HP 4,000,000) x6; Tobi (id 81, lv 21, HP 5,000,000) x4 |
| 164 | Map riêng tư | 0 | Khủng long (id 1, lv 2, HP 200) x3; Khủng long mẹ (id 4, lv 3, HP 500) x1; Thằn lằn bay (id 7, lv 4, HP 600) x4 |
| 165 | Sa mạc hoang vu | 2 | Cadic M (id 95, lv 1, HP 6,000,000) x5 |
| 183 | Thành cổ 1 | 0 | Heo Xayda mẹ (id 15, lv 21, HP 400,000) x1; Khỉ lông đen (id 54, lv 21, HP 500,000) x1; Heo Xayda mẹ (id 15, lv 21, HP 500,000) x1; Khỉ lông đen (id 54, lv 21, HP 400,000) x1 |
| 184 | Thành cổ 2 | 0 | Khỉ lông đỏ (id 56, lv 21, HP 4,000,000) x1; Khỉ lông vàng (id 57, lv 21, HP 5,000,000) x1; Khỉ lông đỏ (id 56, lv 21, HP 5,000,000) x1; Khỉ lông vàng (id 57, lv 21, HP 4,000,000) x1; Quỷ chim (id 50, lv 21, HP 4,000,000) x2 |

---

## 14. Ghi chú / điểm cần lưu ý

1. **Siêu quái bị tắt:** `Mob.lvMob()` dùng `Util.isTrue(0, 10000)` = 0% → không bao giờ sinh quái tinh anh. Nếu bật lại, nhánh sát thương cho quái maxHp > 20 triệu tính ra `(int)(2 × 0,1) = 0` (quái bất tử với người không có Bùa oai hùng).
2. **`maxTiemNang` không được dùng:** tiềm năng tính theo **sát thương gây ra** + 0,05% HP quái mỗi đòn, không bị chặn theo tổng HP quái → đánh quái HP lớn bằng nhiều đòn nhỏ vẫn nhận TN theo từng đòn.
3. **Bùa trí tuệ x4 bị cộng 2 lần** (`NPoint.calSucManhTiemNang` dòng 1592–1597) → thực tế +800% thay vì +400%.
4. **Hệ số EXP server** = 3 (`Config.properties` → `server.expserver=3`), nhân sau toàn bộ bonus.
5. **Cờ Bôn La không có tác dụng** ở các tỉ lệ gốc = 1 (Cápsule vỡ, NR 4–7 sao 1%, đồ sao khác vải thô) do ép kiểu `int` (1 × 1,15 → 1). Biến `rate`/`rate1` ở mục đồ Thần Linh/đồ ăn set Thần được tính nhưng không dùng.
6. **Đồ sao "khác vải thô"** gần như không thể rơi: 1/8.000 × 1/19.999 × 50% ≈ 3,1×10⁻⁹ mỗi con.
7. **Đồ sao 3 map đầu** chỉ rơi cho người có sức mạnh < 300.000.
8. **Nhiệm vụ 8_1:** giết đúng quái mẹ theo hành tinh có thể rơi **2 viên Ngọc Rồng 7 sao** cùng lúc (1 từ `dropItemTask`, 1 từ `getItemMobReward`).
9. **Mảnh hồn bông tai – giới hạn 150/ngày:** `ItemEvent.canDropManhVo` lần đầu sau nửa đêm trả `true` mà không trừ bộ đếm → thực tế tối đa 151 lần/ngày. Chỉ được roll khi mảnh vỡ (10%) trượt.
10. **Ngọc miễn phí mỗi ngày:** lần giết quái đầu tiên trong ngày ở map 0–163 rơi 2 viên ngọc (thông báo "1 đến 2 viên" trong `Service.sendDanhQuaiNhanNgoc` không khớp: code luôn rơi 2 cục, mỗi cục 1).
11. **Rơi vàng map lạnh / tương lai / phó bản** luôn dùng icon 190 vì số vàng luôn ≥ 10.000 (các nhánh `< 6000`, `< 10000`, `< 14000` là code chết).
12. **Big boss:** Voi Chín Ngà, Gà Chín Cựa, Ngựa Chín Lmao, Piano không có trong `map_template` và không tìm thấy chỗ spawn trong code → hiện không xuất hiện. Gấu tướng cướp, Vua Bạch Tuộc, Robot bảo vệ không có logic hồi sinh trong class (Vua Bạch Tuộc/Robot phụ thuộc phó bản Kho báu).
13. **Hirudegarn:** nhánh hồi sinh sau 600.000 ms (10 phút) không bao giờ chạy vì nhánh 15 giây luôn khớp trước → hồi sinh sau 15 giây. Mỗi chu kỳ 3 giai đoạn rơi tổng 90 cục vàng × 32.000 = 2.880.000 vàng (rơi tự do, ai cũng nhặt được).
14. **Mộc nhân không rơi đồ và chỉ cho 1 TN/đòn.**
15. **Map Cadic (≥ 165)** không thuộc `AllMap` → không rơi đồ ăn sự kiện, không nhận ngọc miễn phí; vẫn rơi NR 4–7 sao 1% và sao pha lê 10% vì 2 mục này áp dụng mọi map.
16. **RewardService.getListItemLuckyRound:** tham số `vip` không dùng; nhánh "VIP" không bao giờ gọi `itemRand` (điều kiện `quantity == 0` luôn sai).
17. Tick zone là 1 giây nên thời gian hồi sinh thực tế 3–4 giây; `Map.run()` (tick 5 giây) không được gọi.
