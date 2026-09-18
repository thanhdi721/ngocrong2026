# 05 — Đệ tử (Pet) & thú cưng

> Đặc tả hệ thống đệ tử của server Ngọc Rồng Online (Teamobi2026).
> Nguồn: `SRC/src/nro/models/player/Pet.java`, `player/NewPet.java`, `player/Fusion.java`, `player/NPoint.java`, `services/PetService.java`, `services/Service.java` (`addSMTN`, `showInfoPet`), `services/InventoryService.java`, `services_func/UseItem.java`, `npc/MabuEgg.java`, `npc_list/QuaTrung.java`, `npc_list/ToriBot.java`, `boss/Broly/SuperBroly.java`, `server/Command.java`, `server/Controller.java`, `database/MrBlue.java`, `mob/Mob.java`.

## Mục lục

1. [Tổng quan & các loại đệ tử](#1-tổng-quan--các-loại-đệ-tử)
2. [Cách nhận đệ tử](#2-cách-nhận-đệ-tử)
3. [Khởi tạo đệ tử (createNewPet) — chỉ số random](#3-khởi-tạo-đệ-tử-createnewpet--chỉ-số-random)
4. [Đổi đệ tử / đổi tên](#4-đổi-đệ-tử--đổi-tên)
5. [Trạng thái đệ tử & AI](#5-trạng-thái-đệ-tử--ai)
6. [Tự cộng tiềm năng & nhận TNSM](#6-tự-cộng-tiềm-năng--nhận-tnsm)
7. [Kỹ năng đệ tử](#7-kỹ-năng-đệ-tử)
8. [Trang bị của đệ tử](#8-trang-bị-của-đệ-tử)
9. [Hợp thể & bông tai Porata 1-2-3](#9-hợp-thể--bông-tai-porata-1-2-3)
10. [Ngoại hình, biến hình](#10-ngoại-hình-biến-hình)
11. [Chết, hồi sinh, thể lực, đậu thần](#11-chết-hồi-sinh-thể-lực-đậu-thần)
12. [Pet đi theo / linh thú (NewPet)](#12-pet-đi-theo--linh-thú-newpet)
13. [Liên quan mob_bigboss](#13-liên-quan-mob_bigboss)
14. [Lưu trữ DB](#14-lưu-trữ-db)
15. [Ghi chú / điểm cần lưu ý](#15-ghi-chú--điểm-cần-lưu-ý)

---

## 1. Tổng quan & các loại đệ tử

`Pet extends Player` với `isPet = true`, `master` = sư phụ. ID: `pet.id = -player.id` (người chơi thật) hoặc `-|player.id| − 100000` (khác).

| typePet | Tên mặc định (`name`) | Hàm tạo | SM khởi đầu | Ghi chú |
|---|---|---|---|---|
| 0 | `$Đệ tử` | `createNormalPet` | 2 000 | đệ thường |
| 1 | `$Mabư` | `createMabuPet` | 1 500 000 | từ trứng Mabư |
| 2 | `$Uub` | `createUubPet` | 40 000 000 000 | "đệ VIP" (Command gọi là "Goku vô cực") |
| 3 | `$Kid Beer` | `createKidBeerPet` | 40 000 000 000 | "Kid Beerus" / Berus |
| 4 | `$Kid Jiren` | `createJirenPet` | 40 000 000 000 | Jiren |
| 5 | — | **không có hàm tạo** | — | chỉ xuất hiện trong điều kiện AI (HAKAI, đánh người) |

Không tồn tại đệ "Xên con" trong code (các `XENCON1..7` là boss con của Xên Bọ Hung, không phải pet).

---

## 2. Cách nhận đệ tử

| Nguồn | Điều kiện | Kết quả | File |
|---|---|---|---|
| Giết **Super Broly** | người giết chưa có đệ | `createNormalPet(plKill)` | `boss/Broly/SuperBroly.reward` |
| NPC ToriBot — mua gói VIP1/VIP2/VIP3 | chưa có đệ | `createNormalPet` | `npc_list/ToriBot.VIP1/2/3` |
| Admin: menu "Phát đệ tử" (`ConstNpc.BUFF_PET`) / `MENU_ADMIN` case 1 | người nhận chưa có đệ | `createNormalPet` | `npc/NpcFactory` |
| **Trứng Mabư** (item 568) | chưa có trứng | tạo `MabuEgg`, nở sau `DEFAULT_TIME_DONE = 864 000 000 ms` (10 ngày) | `UseItem` case 568, `npc/MabuEgg` |
| — Ấp nhanh (NPC Quả trứng, map nhà `21+gender`) | 1 000 000 000 vàng (`COST_AP_TRUNG_NHANH`) | `timeDone = 0` | `npc_list/QuaTrung` |
| — Nở trứng | **phải đang có đệ tử**; chọn Mabư Trái Đất / Namếc / Xayda | `changeMabuPet(player, gender)` (thay đệ cũ, giữ `limitPower`), chuyển map `changeMapInYard(gender × 7)` | `MabuEgg.openEgg` |
| Item **1795** (đổi đệ ngẫu nhiên) | item có option 250 (Kilis) ≥ 3000; đang có đệ **Mabư** (typePet 1) SM ≥ 40 tỷ | random đều 1/3: Uub (2) / Kid Beer (3) / Kid Jiren (4); xoá item | `UseItem.changePetRamdom` |

Tích Kilis cho item 1795 (`Mob.injured`): đánh quái ở map `mapId >= 165` (`isMapCadic`), mỗi đòn có tỉ lệ `1/333` (hoặc `10/333` nếu có buff Kilis `itemTime.isUseKilis`) → option 250 của item 1795 trong túi `+1`. Buff Kilis mua ở NPC Osin (`BUA_HO_TRO`): 5 ngọc/10 phút, cộng dồn tối đa 60 phút.

---

## 3. Khởi tạo đệ tử (createNewPet) — chỉ số random

`PetService.createNewPet(player, isMabu, isUub, isKidBeer, isJiren, gender...)`:

| Chỉ số gốc | Thường (0) | Mabư (1) | Uub (2) / Kid Beer (3) / Kid Jiren (4) |
|---|---|---|---|
| `hpg` | `rand[40,105] × 20` (800–2 100) | `rand[40,105] × 20` | 400 000 |
| `mpg` | `rand[40,105] × 20` | `rand[40,105] × 20` | 400 000 |
| `dameg` | `rand[20,45]` | `rand[50,120]` | 20 000 |
| `defg` | `rand[9,50]` | `rand[9,50]` | `rand[9,50]` |
| `critg` | `rand[0,2]` | `rand[0,2]` | `rand[0,2]` |
| `stamina / maxStamina` | 1000 / 1000 | = | = |
| Số ô `itemsBody` | 7 | 7 | 9 |

Các trường khác:
- `gender`: tham số truyền vào, nếu không có → `rand[0,2]`. `createUubPet/KidBeer/Jiren` dùng `player.gender`.
- `limitPower`: mặc định 0; `createXxxPet(player, limitPower)` gán nếu truyền đúng 1 giá trị (dùng khi đổi đệ).
- Kỹ năng: `skills[0] = createSkill(rand[0,2] × 2, 1)` → Dragon (0) / Demon (2) / Galick (4) cấp 1 (không phụ thuộc gender đệ); thêm 6 ô rỗng (`skillId = −1`).
- `setFullHpMp()`, gán `player.pet = pet`.
- Sau 1 giây chat: thường/Uub/Jiren "Xin hãy thu nhận tao làm đệ tử", Mabư "Oa oa oa...", Kid Beer "Hãy hợp tác với ta, Kakarot!".

---

## 4. Đổi đệ tử / đổi tên

### 4.1 Đổi đệ

| Hàm | Gọi từ | Xử lý |
|---|---|---|
| `changeNormalPet(player, gender)` | item **401** (đổi đệ tử): `gender = (pet.gender + 1) % 3` (0→1→2→0) | lưu `limitPower`; nếu đang hợp thể → `unFusion`; `exitMap` + `dispose`; `createNormalPet(player, gender, limitPower)`; trừ 1 item |
| `changeMabuPet(player, gender)` | nở trứng Mabư | như trên, tạo Mabư |
| `changeUubPet` / `changeKidBeerPet` / `changeJirenPet` | không có nơi gọi | xem ghi chú (NPE) |

Đổi đệ **mất toàn bộ** sức mạnh, tiềm năng, chỉ số, kỹ năng, trang bị đang mặc trên đệ (object bị dispose; đồ trên người đệ không trả về túi), **giữ `limitPower`**.

### 4.2 Đổi tên — chat `ten con la <tên>` (`Command.check` → `PetService.changeNamePet`)

- Cần item **400** (thẻ đặt tên đệ tử, mua ở Santa), tên không ký tự đặc biệt, độ dài ≤ 10.
- Tên lưu `"$" + name.toLowerCase().trim()`; trừ 1 item 400.

---

## 5. Trạng thái đệ tử & AI

### 5.1 Hằng trạng thái (`Pet.java`)

| status | Hằng | Chat (đệ thường) | Chat (Jiren typePet 4) |
|---|---|---|---|
| 0 | `FOLLOW` | Ok con theo sư phụ | Lũ con người không đủ tư cách để nói chuyện với ta |
| 1 | `PROTECT` | Ok con sẽ bảo vệ sư phụ | Ta sẽ cho người biết sức mạnh của một vị thần là như thế nào ! |
| 2 | `ATTACK` | Ok sư phụ để con lo cho | Ta sẽ thống trị vũ trụ |
| 3 | `GOHOME` | OK con về, bibi sư phụ | Không lí nào ta lại run sợ bọn con người sao |
| 4 | `FUSION` | (default) Sư phụ ơi con lên cấp rồi | Sức mạnh của ta là không có giới hạn |
| 5 | `HTVV` | Dm sư phụ | Lũ các ngươi làm ta thấy đau rồi ấy haha |

Đổi trạng thái:
- Gói client `-108` (`Controller`): `pet.changeStatus(byte)` — nhận **bất kỳ** byte.
- Chat lệnh (`Command`): `di theo`/`follow` → 0, `bao ve`/`protect` → 1, `tan cong`/`attack` → 2, `ve nha`/`go home` → 3, `bien hinh` → `transform()`.

`changeStatus(status)`:
```
if goingHome || master.fusion.typeFusion != 0 || (đệ chết && status == FUSION): "Không thể thực hiện"
chat text ; if GOHOME → goHome() ; if FUSION → fusion(false)   (Lưỡng Long Nhất Thể)
this.status = status
```

### 5.2 Vòng cập nhật `Pet.update()`

Được gọi trong `Player.update()` của sư phụ (≈ 1 lần/giây, do `Manager` cập nhật zone mỗi 1 s) — **chỉ khi sư phụ không ở map nhà** (`!MapService.isHome`).

```
super.update()            (Player.update: hồi phục, hiệu ứng...)
increasePoint()           (mục 6.1)
updatePower()             (mở skill theo SM, mục 7)
if đệ chết: nếu now − lastTimeDie > 120 000 → hsChar(full HP/KI) ; else return
if đang tung tuyệt kỹ: return
if vừa hồi sinh và cùng map: chat "Sư phụ ơi con đây nè"
if khác zone sư phụ: joinMapMaster()
if sư phụ chết || đệ chết || đệ bị khống chế: return
masterDoesNotAttack() ; moveIdle()
switch status: ...
```

### 5.3 Hành vi theo trạng thái

| Trạng thái | Hành vi |
|---|---|
| FOLLOW | `followMaster(60)`: nếu cách > 60 px hoặc `x_đệ − x_sư_phụ < 50` → đứng cạnh sư phụ ±50 px. Không đánh. |
| PROTECT | 1) thử `useSkill3() → useSkill4() → useSkill5()`; 2) tìm **người** để đánh (`findPlayerAttack`): nếu có → (typePet 2/4/5: **HAKAI** 1/5) → đánh; 3) nếu không có người → tìm **quái** trong 300 px → đánh; không có → `idle`. |
| ATTACK | giống PROTECT nhưng **không HAKAI**; khi đánh người bằng chiêu 1 xong gọi thêm `useSkill5()`. |
| GOHOME | Sau 2 s chuyển về map nhà `21 + master.gender`. Nếu đang ở map 21/22/23: mỗi 5 s đi qua lại (map 21/23: x 250↔200; map 22: x 500↔452, y 336) và chat "Là do bạn không chơi đồ đấy bạn ạ!". |
| FUSION | đệ rời map (đang hợp thể). |
| HTVV | nếu **sư phụ là Namếc** (`master.gender == 1`): hiệu ứng hợp thể, đệ rời map, `addSMTN(master, type 1, pet.power)` (cộng **tiềm năng** bằng toàn bộ SM của đệ), `master.pet = null` (mất đệ). |

Chọn chiêu đánh (PROTECT/ATTACK):
```
if khoảng cách <= 50 (ARANGE_ATT_SKILL1): dùng chiêu 1 (đấm), di chuyển tới mục tiêu ±60 (quái ATTACK: ±20)
else: dùng chiêu 2 (chưởng) nếu có, không có → chiêu 1
mỗi lần: kiểm tra cooldown + canAttack(); thiếu KI → askPea()
```

`findPlayerAttack` (trong 300 px, gần nhất) — `cantAttack(pl)` loại bỏ nếu: null/chết/ > 500 px/là chính nó/**là sư phụ**/**typePet ∉ {2,4,5}**/không nằm trong `temporaryEnemies` của đệ **hoặc** sư phụ/`!canAttackPlayer`.
→ Chỉ đệ Uub (2), Jiren (4) (và typePet 5) mới đánh người; đệ thường/Mabư/Kid Beer chỉ đánh quái.

**HAKAI** (chỉ PROTECT, typePet 2/4/5), mỗi tick tìm được mục tiêu:
```
if random(1/5) && target.hp < 1 000 000 000 && !target.nPoint.islinhthuydanhbac && !target.isBoss:
    target.setDie(pet) ; pet chat "HAKAI <tên>!" ; target nhận "Bạn đã bị Hakai!"
else petSay(target)       (chuỗi chửi, 15 s cho câu đầu, 1.5 s các câu sau)
```
`islinhthuydanhbac` = mặc ≥ 5 món có option 211.

`findMobAttack`: quái còn sống gần nhất trong 300 px.

`masterDoesNotAttack()`: nếu `now − master.lastTimePlayerNotAttack > master.timeNotAttack` → `master.doesNotAttack = true` (trừ map offline), đặt lại `timeNotAttack = rand[1 800 000, 3 600 000]` ms. Khi sư phụ dùng skill đánh (`SkillService.useSkillAttack`) → `doesNotAttack = false`.
`canAttack()`: nếu sư phụ là người thật, `doesNotAttack` và **không** có bùa Đệ tử → đệ không đánh, 10 s chat "Sao sư phụ không đánh đi?".

`moveIdle()`: khi `idle` và hết `timeMoveIdle` (rand 5–8 s) → di chuyển cạnh sư phụ ±50.

`Pet.followMaster()` (gọi khi sư phụ di chuyển): ATTACK và đang có quái trong 1000 px thì không theo; FOLLOW/PROTECT/ATTACK khác → `followMaster(500)`.

`joinMapMaster()`: nếu sư phụ ở map offline hoặc map 113 → đệ về map nhà; ngược lại vào cùng zone, x = sư phụ ±10.

---

## 6. Tự cộng tiềm năng & nhận TNSM

### 6.1 Tự cộng điểm — `Pet.increasePoint()`

Mỗi lần `update` (điều kiện `canDoWithTime(last, 0)` luôn đúng): lặp **20 lần** `nPoint.increasePoint(rand[0,4], 1)` → chọn ngẫu nhiên HP/KI/SĐ/Giáp/Chí mạng, cộng 1 điểm nếu đủ tiềm năng và chưa chạm giới hạn (công thức chi phí & giới hạn giống người chơi — doc 03 mục 4).

| type | Tăng | Chi phí (point = 1) |
|---|---|---|
| 0 HP | `+20 hpg` | `hpg + 1000` |
| 1 KI | `+20 mpg` | `mpg + 1000` |
| 2 SĐ | `+1 dameg` | `dameg × 100` |
| 3 Giáp | `+1 defg` | `(defg + 5) × 100 000` |
| 4 Chí mạng | `+1 critg` | `50 000 000 × 5^critg` |

Giới hạn theo `pet.nPoint.limitPower` (limit 0: HP/KI ≤ 220 000, SĐ ≤ 11 000, giáp ≤ 550, CM ≤ 1).

### 6.2 Nhận TNSM

Đệ đánh quái → `Mob.injured` → `Service.addSMTN(pet, 2, tn, true)` với `tn = getTiemNangForPlayer(pet, dame)` (doc 03 mục 11; `calSucManhTiemNang` của đệ có thêm Bùa Santa `+2×base` và option 160 của sư phụ `+base/100 × (tlTNSMPet + 100)`; VIP của sư phụ `+3×base`).

`Service.addSMTN` nhánh đệ tử:
```
pet.power += param ; pet.tiemNang += param            (không kiểm tra giới hạn SM của đệ)
masterParam = calSubTNSM_master(param × 0.5)
if master.power >= master.limit: return
masterParam = min(masterParam, limit − master.power)
master.power += masterParam ; master.tiemNang += masterParam
addSMTN(master, type, masterParam, true)              ← sư phụ được cộng thêm lần nữa
```

Giới hạn SM của đệ thể hiện qua `calSucManhTiemNang`: `power >= getPowerLimit()` → `tn = 10`.

### 6.3 Nâng giới hạn sức mạnh đệ

NPC Quốc Vương (limitPower < 5) và Tổ sư Kaio (5 ≤ limitPower < 9): `OpenPowerService.openPowerSpeed(pet)`, giá **50 000 000 vàng** (thông báo thiếu ghi "ngọc" nhưng trừ vàng), không yêu cầu SM.

---

## 7. Kỹ năng đệ tử

### 7.1 Mở kỹ năng theo sức mạnh — `Pet.updatePower()`

Dựa trên `getSizeSkill()` (số skill có `skillId != −1`):

| Đang có | Điều kiện SM | Hàm | Pool & tỉ lệ | Ghi vào |
|---|---|---|---|---|
| 1 skill | ≥ 150 000 000 | `openSkill2` | Kamejoko 33% / Masenko 33% / Antomic 34% (random lại nếu trùng skill hiện tại); `coolDown = 1000` | `skills[1]` |
| 2 skill | ≥ 1 500 000 000 | `openSkill3` | Thái Dương Hạ San 30% / Tái tạo năng lượng 40% / Kaioken 30% | `skills[2]` |
| 3 skill | ≥ 20 000 000 000 | `openSkill4` | Biến khỉ 10% / Đẻ trứng 70% / Khiên năng lượng 20% | `skills[3]` |
| 4 skill | ≥ 40 000 000 000 **và** typePet ∈ {2, 3, 4} | `openSkill5` | theo `pet.gender`: 0 → Super Kamejoko, 1 → Ma phong ba, 2 → Cađíc liên hoàn chưởng | `skills[4]` |

Tất cả được tạo ở **cấp 1**. Đệ Uub/Beer/Jiren tạo mới có SM 40 tỷ nên mở dần chiêu 2→5 trong các tick đầu.

### 7.2 Nâng cấp / đổi chiêu bằng item

| Item | Tác dụng | Hàm |
|---|---|---|
| 402 | chiêu 1 +1 cấp | `SkillUtil.upSkillPet(skills, 0)` |
| 403 | chiêu 2 +1 cấp (giữ `coolDown = 1000`) | `upSkillPet(skills, 1)` |
| 404 | chiêu 3 +1 cấp | `upSkillPet(skills, 2)` |
| 759 | chiêu 4 +1 cấp | `upSkillPet(skills, 3)` |
| 1758 | random lại chiêu 2 (`openSkill2`) — cần đã có chiêu 2 | `UseItem` |
| 1759 | random lại chiêu 3 (`openSkill3`) | `UseItem` |
| 1760 | random lại chiêu 4 (`openSkill4`) | `UseItem` |

`upSkillPet`: `level = point + 1`; `level > 7` → thất bại; thay skill bằng dữ liệu cấp mới. Item bị trừ trước khi kiểm tra ở 1758–1760 (xem ghi chú).

### 7.3 Cách đệ dùng kỹ năng

| Hàm | Skill | Điều kiện dùng |
|---|---|---|
| `useSkill3` | TDHS | hết cooldown & đủ KI → dùng ngay, chat "Bất ngờ chưa ông già" |
| | Tái tạo NL | khi HP ≤ 20% hoặc KI ≤ 20%; đang sạc thì giữ thêm `rand[3,5]` tick |
| | Kaioken | tìm người (ưu tiên) hoặc quái; tiến tới mục tiêu rồi dùng; đặt cooldown chiêu 1 |
| `useSkill4` | Biến khỉ | chưa khỉ |
| | Khiên | chưa có khiên |
| | Đẻ trứng | chưa có MobMe |
| `useSkill5` | Super Kame / Liên hoàn chưởng / Ma phong ba | cần `newSkill`, có người gần nhất (ưu tiên) hoặc quái gần nhất trong zone (`zone.findNearestPlayer/Mob`), gọi `newSkill.setSkillSpecial` + `affterUseSkill` |

Sát thương và hiệu ứng từng skill giống người chơi (doc 04). Load từ DB (`MrBlue`): Kamejoko/Masenko/Antomic của đệ luôn `coolDown = 1000`.

Sách tuyệt kỹ (type 25) cho đệ: chat `sach tuyet ky` → chỉ typePet 2/3/4, đệ SM ≥ 1 500 000 → mặc item type 25 đầu tiên trong túi vào slot 8 của đệ.

---

## 8. Trang bị của đệ tử

`InventoryService.itemBagToPetBody`: đệ phải có **SM ≥ 1 500 000** ("Đệ tử phải đạt 1tr5 sức mạnh mới có thể mặc").

`putItemBody` (áp cho đệ):
- Loại hợp lệ: type 0–5, 32, 23, 24, 11, 27, 25; kiểm tra `gender` (trừ gender 3) và SM yêu cầu (`strRequire` hoặc option 21 × 1 tỷ).
- Đệ **không** được mặc type 23/24 (thú cưỡi), 27 (pet đi theo).
- Type 11 (túi/cờ) và 25 (tuyệt kỹ) chỉ đệ có `pet.type ∈ {2,3,4}` ("Chỉ đệ tử vip mới sử dụng được vật phẩm này!").

Slot đệ: 0–5 như người; 6 giáp tập luyện (type 32); 7 flag bag của đệ (`Player.getFlagBag` dùng slot 7 cho đệ); 8 tuyệt kỹ (type 25) — `putItemBody` map type 11 → index 8, type 25 → index 8 (trùng slot với nhau đối với đệ).

Chỉ số của đệ tính bằng cùng `NPoint.calPoint` (mọi option trên `itemsBody` của đệ, doc 03 mục 5–7), được tính **trước** sư phụ trong mỗi `calPoint` của sư phụ.

---

## 9. Hợp thể & bông tai Porata 1-2-3

### 9.1 Các kiểu (`ConstPlayer`)

| typeFusion | Tên | Kích hoạt | Hàm | Thời gian | Icon |
|---|---|---|---|---|---|
| 4 | Lưỡng Long Nhất Thể | `changeStatus(FUSION)` | `Pet.fusion(false)` | `TIME_FUSION = 600 000` ms → `Fusion.update` tự `unFusion` | 3901 (Namếc) / 3790 |
| 6 | Hợp thể Porata | dùng item **454** | `UseItem.usePorata` → `fusion(true)` | đến khi dùng lại item | — |
| 8 | Porata cấp 2 | item **921** | `usePorata2` → `fusion2(true)` | đến khi dùng lại | — |
| 9 | Porata cấp 3 | item **1819** | `usePorata3` → `fusion3(true)` | đến khi dùng lại | — |
| 10 | Gogeta | `fusionGogeta(true)` | **không có nơi gọi** | | |

Điều kiện (`usePorata*`): có đệ, `typeFusion != 4` (đang Lưỡng Long thì không dùng bông tai). Nếu `typeFusion == 0` → hợp thể; ngược lại → `unFusion()` (dùng item bất kỳ cấp nào cũng tách).

`fusion*`:
```
if đệ chết: "Yêu cầu phải có đệ tử và đệ tử còn sống"
if now − lastTimeUnfusion <= 5000: "Vui lòng đợi ..."
typeFusion = ... ; pet.status = FUSION ; đệ exitMap ; gửi hiệu ứng (msg 125)
master.nPoint.calPoint() ; master.nPoint.setFullHpMp() ; Service.point(master)
```
`unFusion`: `typeFusion = 0`, `pet.status = PROTECT`, đệ vào lại map, `lastTimeUnfusion = now`.
Bị Mabư (map 127/128) bắt giữ khi hợp thể → tự `unFusion` (`Service.sendMabuEat`).

### 9.2 Công thức cộng chỉ số khi hợp thể (cho sư phụ)

Trong `NPoint` của **sư phụ** (sau khi đã tính các buff % của chính sư phụ; trước Bổ huyết/Bổ khí/Nước mía/Huýt sáo/Chibi/map lạnh/danh hiệu):

```
if typeFusion != 0:
    hpMax_master += pet.nPoint.hpMax
    mpMax_master += pet.nPoint.mpMax
    dame_master  += pet.nPoint.dame
```
(giáp, chí mạng, né… của đệ **không** cộng.)

Các bước sau đó vẫn nhân lên tổng: ví dụ Bổ huyết ×2 áp cho `(hp_sư_phụ + hp_đệ)`.

Khi sư phụ ở trạng thái Porata (6/8/9), **chỉ số của đệ** được tăng thêm theo typePet (trong NPoint của đệ):

| typePet | HP max | KI max | Sức đánh |
|---|---|---|---|
| 0 Thường | — | — | — |
| 1 Mabư | +0% | +0% (chỉ khi worldcup 2) | +0% |
| 2 Uub | +20% | +20% × mpMax cũ | +20% |
| 3 Kid Beer | +20% | +20% × mpMax cũ | +20% rồi +20% (≈ +44%) |
| 4 Kid Jiren | +20% | +20% × mpMax cũ | +20% |

(Lưỡng Long Nhất Thể typeFusion 4 **không** được các khoản +20% này.)

Đệ đang hợp thể (`status == FUSION`): phần cộng từ **biến khỉ** và **huýt sáo** của đệ bị bỏ qua.

### 9.3 Bông tai Porata 2 / 3 — cộng option

Trong `setPointWhenWearClothes` của sư phụ:
```
if typeFusion == 8: item = item 921 đầu tiên trong itemsBag
if typeFusion == 9: item = item 1819 đầu tiên trong itemsBag
for option in item.itemOptions: addOption(option)     (toàn bộ bảng doc 03 mục 6)
    option 72 (Cấp #) → nPoint.levelBT = param
```
→ Chỉ số thực tế của bông tai cấp 2/3 phụ thuộc option trên item (ví dụ HP+#%, SĐ+#%… do hệ thống nâng cấp item tạo ra); bông tai phải **nằm trong túi**. Porata cấp 1 (454) không cộng option.

### 9.4 Ngoại hình khi hợp thể (`Player.getHead/getBody/getLeg`)

`idOutfitFusion`:

| index | head, body, leg | Dùng cho |
|---|---|---|
| 0 | 380, 381, 382 | Lưỡng Long (TĐ/Xayda) |
| 1 | 383, 384, 385 | Porata 1 (TĐ/Xayda) |
| 2 | 391, 392, 393 | Namếc (Lưỡng Long & Porata 1) |
| 3 / 4 / 5 | 870–872 / 873–875 / 867–869 | Porata 2 theo gender 0/1/2 |
| 6 / 7 / 8 | 1866,1859,1860 / 1861,1864,1865 / 1856,1859,1860 | Porata 3 theo gender 0/1/2 |

Ưu tiên cao hơn: cải trang 1693/1553 khác nhau giữa sư phụ & đệ → 1578/1581/1582; Gogeta (2133 + 2134) → 2100/2101/2102; khỉ, sôcôla, bình, hoá đá.

---

## 10. Ngoại hình, biến hình

`Pet.PET_ID = {{285,286,287}, {288,289,290}, {282,283,284}, {304,305,303}, {946,947,948}, {1743,1744,1745}, {876,877,878}}`

| Loại | head | body | leg | avatar |
|---|---|---|---|---|
| Thường, SM < 1 500 000 | `PET_ID[gender][0]` (TĐ 285, NM 288, XD 282) | `PET_ID[gender][1]` | `PET_ID[gender][2]` | `PET_ID[3][gender]` |
| Thường, SM ≥ 1 500 000 | `PET_ID[3][gender]` (304/305/303) | body áo (slot 0) hoặc 57/59 | leg quần hoặc 58/60 | = |
| Mabư (1) | 297 | 298 (khi `!isTransform`) | 299 | 297 |
| Uub (2) | 946 | 947 | 948 | 946 |
| Kid Beer (3) | 1422 | 1423 | 1424 | 1422 |
| Kid Jiren (4) | 876 | 877 | 878 | 876 |

Cải trang (slot 5) chỉ đổi head/body/leg cho đệ thường (các loại 1–4 ưu tiên ngoại hình riêng).

`transform()` (chat `bien hinh`):
- typePet 1: đảo `isTransform` → body/leg trở lại theo trang bị; chat "Bố Mày Là Bư Nè !! ...".
- typePet 2: đảo **2 lần** (2 khối `if typePet == 2`) → không đổi; chat 2 câu.
- typePet 4: đảo `isTransform` nhưng body/leg của Jiren không xét `isTransform` → không đổi hình.
- typePet 3: không xử lý.

---

## 11. Chết, hồi sinh, thể lực, đậu thần

| Cơ chế | Chi tiết |
|---|---|
| Chết | `Service.charDie` ghi `lastTimeDie`. Đệ không mất vàng. |
| Hồi sinh | tự động sau 120 000 ms, full HP/KI (`Pet.update`). |
| Thể lực | mỗi 2 đòn đánh −1 (5 đòn nếu sư phụ có bùa Đệ tử); hết thể lực → `askPea()`; hồi +1/60 s. |
| `askPea()` | cooldown 10 s; chat "Sư phụ ơi cho con đậu thần" và gọi `UseItem.eatPea(master)`: sư phụ ăn 1 đậu trong túi, nếu đệ cùng map & còn sống → đệ +`100 × cấp đậu` thể lực và cùng lượng HP/KI như sư phụ. |
| Bùa Đệ tử (522) | đệ đánh quái ×2 (thực tế ×4), quái đánh đệ ÷2, đệ vẫn đánh khi sư phụ AFK, thể lực bền hơn. |
| Bùa Thu hút | đồ quái do đệ giết được sư phụ tự nhặt. |

---

## 12. Pet đi theo / linh thú (NewPet)

`player/NewPet.java` — `isNewPet = isNewPet1 = true`, id bắt đầu `−310720020` giảm dần.

- Tạo bởi `Player.sendNewPet()` khi **đăng nhập** (`Controller`, sau `mapInfo`): nếu `itemsBody[7]` có item và chưa có `newPet` → `PetService.Pet2(player, head, body, leg)` theo bảng item:

| Item slot 7 | head, body, leg | Item | head, body, leg |
|---|---|---|---|
| 892 | 882, 883, 884 | 1243 | 1245, 1246, 1247 |
| 893 | 885, 886, 887 | 1244 | 1248, 1249, 1250 |
| 908 | 891, 892, 893 | 1256 | 1267, 1268, 1269 |
| 909 | 894, 895, 896 | 1318 | 1299, 1300, 1301 |
| 910 | 897, 898, 899 | 1347 | 1302, 1303, 1304 |
| 916 | 925, 926, 927 | 1414 | 1341, 1342, 1343 |
| 917 | 928, 929, 930 | 1435 | 1347, 1348, 1349 |
| 918 | 931, 932, 933 | 1452 | 1365, 1366, 1367 |
| 919 | 934, 935, 936 | 1458 | 1368, 1369, 1370 |
| 936 | 718, 719, 720 | 1482 | 1398, 1399, 1400 |
| 942 | 966, 967, 968 | 1497 | 1401, 1402, 1403 |
| 943 | 969, 970, 971 | 1550 | 1428, 1429, 1430 |
| 944 | 972, 973, 974 | 1551 | 1425, 1426, 1427 |
| 967 | 1050, 1051, 1052 | 1564 | 1437, 1438, 1439 |
| 1008 | 1074, 1075, 1076 | 1568 | 1443, 1444, 1445 |
| 1039 | 1089, 1090, 1091 | 1573 | 1446, 1447, 1448 |
| 1040 | 1092, 1093, 1094 | 1596, 1597 | 1473, 1474, 1475 |
| 1046 | −1, −1, −1 | 1611 | 1488, 1494, 1495 |
| 1107 | 1155, 1156, 1157 | 1620, 1621 | 1496, 1497, 1498 |
| 1114 | 1158, 1159, 1160 | 1622 | 1488, 1489, 1490 |
| 1188 | 1183, 1184, 1185 | 1629 | 1505, 1506, 1507 |
| 1202, 1203 | 1201, 1202, 1203 | 1630 | 1508, 1509, 1510 |
| 1207 | 1077, 1078, 1079 | 1631 | 1513, 1516, 1517 |
| 1224 | 1227, 1228, 1229 | 1633 | 1523, 1524, 1525 |
| 1225 | 1233, 1234, 1235 | 1654 | 1526, 1529, 1530 |
| 1226 | 1230, 1231, 1232 | 1668 | 1550, 1551, 1552 |
| 1682 | 1558, 1559, 1560 | 1683 | 1561, 1562, 1563 |
| 1686 | 1572, 1573, 1574 | 1750 | 1464, 1465, 1466 |
| 1765 | 1662, 1663, 1764 | 1729 | 1621, 1622, 1623 |
| 1727 | 1616, 1617, 1618 | 1789 | 1724, 1725, 1726 |
| 1766 | 1665, 1666, 1667 | 1767 | 1668, 1669, 1670 |
| 1768 | 1671, 1672, 1673 | 1769 | 1674, 1675, 1676 |
| 1770 | 1677, 1678, 1679 | 1771 | 1680, 1681, 1682 |

- Chỉ số NewPet (`Pet2`): `hpg = mpg = hp = mp = 500 000 000`, `dameg = defg = critg = 1`, `power = tiemNang = 1`, `limitPower = 1`, tên `"$"`, gender = gender sư phụ.
- Hành vi: không đánh, không bị đánh (`canAttackPlayer2` trả `false` với newPet), tự hồi sinh ngay nếu chết, theo sư phụ (`followMaster(50)`), vào cùng zone (trừ map offline), `moveIdle` 5–8 s.
- **Chỉ số cho sư phụ**: đến từ option của item pet ở slot 7 (đi qua `addOption`, vd option 50/77/103), không từ object NewPet.
- Tháo item slot 7 (`itemBodyToBag` index 7, không phải type 25) → huỷ NewPet.
- Pet đi theo slot 12 (`sendPetFollow`) là hiển thị khác (tháo slot 12 → gửi `sendPetFollow(0)`).

---

## 13. Liên quan mob_bigboss

Package `mob_bigboss/` (`GaChinCua`, `GauTuongCuop`, `Hirudegarn`, `MayDoSucManh`, `NguaChinLmao`, `Piano`, `RobotBaoVe`, `VoiChinNga`, `VuaBachTuoc`) là quái lớn (BigBoss), **không phải đệ tử/thú cưng**. Liên quan duy nhất tới kỹ năng: Trói lên `GauTuongCuop` cố định 5 000 ms (`SkillService`). Đệ tử đánh các quái này như quái thường qua `findMobAttack`.

---

## 14. Lưu trữ DB

Cột `pet` của bảng `player` là JSON array (đọc ở `database/MrBlue.java`, ghi ở `PlayerDAO`):

| Phần tử | Nội dung |
|---|---|
| `[0]` | `[typePet, gender, name, typeFusion(master), thời gian hợp thể còn lại, status]` — `typeFusion` & `lastTimeFusion` được gán cho **sư phụ** |
| `[1]` | `[limitPower, power, tiemNang, stamina, maxStamina, hpg, mpg, dameg, defg, critg, hp, mp]` |
| `[2]` | items body `[tempId, quantity, options, createTime]` (lọc item hết hạn; bổ sung đủ 7 ô, hoặc 9 ô cho typePet 2/3/4) |
| `[3]` | skills `[tempId, point, lastTimeUse, currLevel]` (point 0 → `createSkillLevel0`); bổ sung tới 4 skill (5 với typePet 3/4) |

Khi load: `pet.type = typePet` (trường `Player.type` dùng để kiểm tra "đệ VIP" khi mặc đồ).

---

## 15. Ghi chú / điểm cần lưu ý

1. **NPE ở `changeUubPet` / `changeKidBeerPet` / `changeJirenPet`**: gán `player.pet = null` rồi đọc `player.pet.gender` → NullPointerException. Ngoài ra `changeJirenPet` gọi nhầm `createKidBeerPet`, và các hàm `createXxxPet(player, gender, limitPower)` truyền 2 giá trị vào varargs `limitPower` → `limitPower.length != 1` nên không giữ được giới hạn. (Hiện không có nơi gọi.)
2. **Item 1795 (`changePetRamdom`)** gọi thẳng `createUubPet/…` mà **không** `unFusion`, `exitMap`, `dispose` đệ Mabư cũ → đệ cũ có thể còn "ma" trên map / trạng thái hợp thể bị treo; `limitPower` về 0.
3. **Đệ VIP mới tạo không mặc được type 11/25** tới khi relog: `putItemBody` kiểm tra `pet.type` (chỉ gán khi load DB), `createNewPet` chỉ gán `typePet`.
4. **Đệ Uub/Beer/Jiren khởi tạo vượt giới hạn**: `hpg/mpg = 400 000 > 220 000`, `dameg = 20 000 > 11 000`, `power = 40 tỷ > 17,99 tỷ` (limit 0) → không tự cộng HP/KI/SĐ và `calSucManhTiemNang` trả 10 cho tới khi nâng `limitPower`.
5. **HAKAI**: đệ Uub/Jiren ở chế độ Bảo vệ có 20% mỗi giây giết ngay người chơi có HP < 1 tỷ (chỉ cần là kẻ thù tạm thời của đệ hoặc sư phụ và `canAttackPlayer`) — rất mạnh trong PK. Miễn nhiễm bằng 5 món option 211.
6. **Trạng thái HTVV (5)** có thể đặt qua gói `-108` từ client: sư phụ Namếc sẽ nhận toàn bộ SM đệ thành **tiềm năng** (bị cắt theo giới hạn SM sư phụ) và mất đệ.
7. **Sư phụ nhận TNSM từ đệ 2 lần** (`Service.addSMTN`): cộng trực tiếp rồi gọi đệ quy `addSMTN(master, …)`.
8. **Bùa Đệ tử**: sát thương đệ đánh quái ×2 ở `getDameAttack(true)` và ×2 ở `playerAttackMob` → ×4.
9. `Pet.update` chỉ chạy khi **sư phụ không ở map nhà** → đệ đứng im (không hồi sinh, không tự cộng điểm, không mở skill) khi sư phụ ở nhà.
10. `increasePoint` gọi 20 lần `increasePoint` mỗi giây với chi phí/giới hạn đầy đủ — tiêu tiềm năng liên tục, chỉ số phân bổ ngẫu nhiên (không cho người chơi tự cộng cho đệ).
11. Item **1758/1759/1760** trừ item **trước** khi kiểm tra đệ đã có chiêu tương ứng → không có chiêu vẫn mất item.
12. `upSkillPet` không kiểm tra skill rỗng (`template == null` với slot trống tạo bằng `createEmptySkill`) → NPE khi dùng sách cho slot chưa mở (bị catch, báo "Không thể thực hiện" nhưng item chưa bị trừ vì trừ sau khi thành công).
13. `transform()` typePet 2 đảo trạng thái 2 lần (không có tác dụng); typePet 3 không có biến hình; typePet 4 không đổi ngoại hình.
14. `MabuEgg.openEgg`: nhánh `createMabuPet` (khi `pet == null`) không bao giờ chạy vì điều kiện ngoài yêu cầu `pet != null`.
15. `QuocVuong`: thông báo "... còn thiếu ... **ngọc**" và nút "Nâng ngay ... ngọc" cho đệ nhưng thực tế trừ **vàng**.
16. `putItemBody` cho đệ map cả type 11 và type 25 vào **slot 8** → hai loại đè nhau.
17. `Pet.askPea` có nhánh `master.isPet` (không thể xảy ra) cộng thẳng 1000 thể lực và 100 000 HP/KI.
18. NewPet chỉ được tạo khi **đăng nhập** (`sendNewPet` chỉ gọi trong `Controller`), mặc item pet khi đang online không hiện linh thú tới lần login sau. Item 1765 map leg `1764` (khả năng nhập sai, các dòng khác liên tiếp).
19. `fusionGogeta` (typeFusion 10) không có nơi gọi; `HOP_THE_PORATA2/3` khai báo `static` không `final`.
20. `Fusion.update` chỉ tự tách với Lưỡng Long (4); Porata 1/2/3 không giới hạn thời gian.
21. `setMpMax` phần +20% KI cho đệ VIP khi Porata dùng `this.mpMax` (giá trị lần tính trước) → giá trị dao động/cộng dồn theo số lần `calPoint`.
