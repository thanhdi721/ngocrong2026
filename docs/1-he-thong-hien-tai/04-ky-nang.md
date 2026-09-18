# 04 — Kỹ năng (Skill)

> Tài liệu đặc tả hệ thống kỹ năng của server Ngọc Rồng Online (Teamobi2026).
> Nguồn: `SRC/src/nro/models/skill/**`, `SRC/src/nro/models/services/SkillService.java`, `SRC/src/nro/models/utils/SkillUtil.java`, `SRC/src/nro/models/services/EffectSkillService.java`, `SRC/src/nro/models/player/NPoint.java`, `SRC/src/nro/models/player/EffectSkill.java`, `SRC/src/nro/models/player/NewSkill.java`, `SRC/src/nro/models/mob/MobMe.java`, bảng `skill_template` trong `database team2026.sql`.
> Mọi con số dưới đây lấy trực tiếp từ code/DB. Chỗ nào code có hành vi lạ được ghi rõ ở mục cuối.

## Mục lục

1. [Mô hình dữ liệu kỹ năng](#1-mô-hình-dữ-liệu-kỹ-năng)
2. [Danh sách kỹ năng theo hành tinh](#2-danh-sách-kỹ-năng-theo-hành-tinh)
3. [Luồng dùng kỹ năng chung](#3-luồng-dùng-kỹ-năng-chung)
4. [Công thức sát thương chung (getDameAttack)](#4-công-thức-sát-thương-chung-getdameattack)
5. [Chi tiết từng kỹ năng — Trái Đất](#5-chi-tiết-từng-kỹ-năng--trái-đất)
6. [Chi tiết từng kỹ năng — Namếc](#6-chi-tiết-từng-kỹ-năng--namếc)
7. [Chi tiết từng kỹ năng — Xayda](#7-chi-tiết-từng-kỹ-năng--xayda)
8. [Kỹ năng dùng chung: Khiên năng lượng](#8-kỹ-năng-dùng-chung-khiên-năng-lượng)
9. [Tuyệt kỹ (skill 9): Super Kamejoko / Ma phong ba / Cađíc liên hoàn chưởng](#9-tuyệt-kỹ-skill-9)
10. [Bảng hàm thời gian / phạm vi trong SkillUtil](#10-bảng-hàm-thời-gian--phạm-vi-trong-skillutil)
11. [Hiệu ứng khống chế (EffectSkill) và cách gỡ](#11-hiệu-ứng-khống-chế-effectskill-và-cách-gỡ)
12. [Nội tại & set đồ ảnh hưởng tới kỹ năng](#12-nội-tại--set-đồ-ảnh-hưởng-tới-kỹ-năng)
13. [Học kỹ năng / nâng cấp kỹ năng](#13-học-kỹ-năng--nâng-cấp-kỹ-năng)
14. [Kỹ năng đệ tử](#14-kỹ-năng-đệ-tử)
15. [Ghi chú / điểm cần lưu ý](#15-ghi-chú--điểm-cần-lưu-ý)

---

## 1. Mô hình dữ liệu kỹ năng

### 1.1 Lớp `Skill` (`skill/Skill.java`)

| Trường | Ý nghĩa |
|---|---|
| `template` | `SkillTemplate` (id template, tên, `maxPoint`, `manaUseType`, `type`, `iconId`, `damInfo`) |
| `skillId` | id của **một cấp** kỹ năng (unique toàn game, ví dụ Dragon cấp 1 = 0, cấp 7 = 6) |
| `point` | cấp hiện tại (0 = chưa học) |
| `powRequire` | sức mạnh yêu cầu (`power_require`) |
| `coolDown` | thời gian hồi (ms) |
| `manaUse` | lượng KI tiêu hao (ý nghĩa tuỳ `manaUseType`) |
| `damage` | % sát thương / tham số kỹ năng (tuỳ skill) |
| `dx`, `dy`, `maxFight`, `price`, `moreInfo` | dữ liệu gửi client |
| `currLevel` | độ thành thạo của tuyệt kỹ (0..1000) |
| `lastTimeUseThisSkill` | mốc dùng lần cuối |

Hằng số: `RANGE_ATTACK_CHIEU_DAM = 100`, `RANGE_ATTACK_CHIEU_CHUONG = 300` (chỉ hằng đấm được dùng để tính trượt).

### 1.2 ID template kỹ năng (hằng số trong `Skill.java`)

| ID | Hằng | Tên (DB) | Hành tinh |
|---|---|---|---|
| 0 | `DRAGON` | Chiêu đấm Dragon | Trái Đất |
| 1 | `KAMEJOKO` | Chiêu Kamejoko | Trái Đất |
| 2 | `DEMON` | Chiêu đấm Demon | Namếc |
| 3 | `MASENKO` | Chiêu Masenko | Namếc |
| 4 | `GALICK` | Chiêu đấm Galick | Xayda |
| 5 | `ANTOMIC` | Chiêu Antomic | Xayda |
| 6 | `THAI_DUONG_HA_SAN` | Thái Dương Hạ San | Trái Đất |
| 7 | `TRI_THUONG` | Trị thương | Namếc |
| 8 | `TAI_TAO_NANG_LUONG` | Tái tạo năng lượng | Xayda |
| 9 | `KAIOKEN` | Kaioken | Trái Đất |
| 10 | `QUA_CAU_KENH_KHI` | Quả cầu kênh khi | Trái Đất |
| 11 | `MAKANKOSAPPO` | Makankosappo | Namếc |
| 12 | `DE_TRUNG` | Đẻ trứng | Namếc |
| 13 | `BIEN_KHI` | Biến hình (biến khỉ) | Xayda |
| 14 | `TU_SAT` | Tự phát nổ | Xayda |
| 17 | `LIEN_HOAN` | Liên hoàn | Namếc |
| 18 | `SOCOLA` | Biến Sôcôla | Namếc |
| 19 | `KHIEN_NANG_LUONG` | Khiên năng lượng | Cả 3 |
| 20 | `DICH_CHUYEN_TUC_THOI` | Dịch chuyển tức thời | Trái Đất |
| 21 | `HUYT_SAO` | Huýt sáo | Xayda |
| 22 | `THOI_MIEN` | Thôi miên | Trái Đất |
| 23 | `TROI` | Trói | Xayda |
| 24 | `SUPER_KAME` | Super Kamejoko | Trái Đất |
| 25 | `LIEN_HOAN_CHUONG` | Cađíc liên hoàn chưởng | Xayda |
| 26 | `MA_PHONG_BA` | Ma phong ba | Namếc |

### 1.3 `TYPE` của template (quyết định nhánh xử lý trong `SkillService.useSkill`)

| TYPE | Nhánh | Kỹ năng |
|---|---|---|
| 1 | `useSkillAttack` (cần mục tiêu) | Dragon, Kamejoko, Kaioken, QCKK, DCTT, Thôi miên, Demon, Masenko, Makankosappo, Liên hoàn, Sôcôla, Galick, Antomic, Trói |
| 2 | `useSkillBuffToPlayer` | Trị thương |
| 3 | `useSkillAlone` (không cần mục tiêu) | TDHS, Khiên, Đẻ trứng, Tái tạo NL, Biến khỉ, Tự phát nổ, Huýt sáo |
| 4 | `useNewSkillNotFocus` (tuyệt kỹ) | Super Kamejoko, Ma phong ba, Cađíc liên hoàn chưởng |

### 1.4 `mana_use_type` (`SkillService.canUseSkillWithMana` / `setMpAffterUseSkill`)

| Giá trị | Điều kiện dùng | KI bị trừ |
|---|---|---|
| 0 | `mp >= manaUse` | `manaUse` (số tuyệt đối) |
| 1 | `mp >= mpMax * manaUse / 100` | `mpMax * manaUse / 100` (% KI tối đa) |
| 2 | `mp > 0` | toàn bộ KI (`setMp(0)`) |

Ngoài ra với **Kaioken**: nếu `hp <= hpMax/100*10` thì không dùng được (trừ boss `Rival`: `hpUse = 0`).

### 1.5 Cooldown

```
canUseSkillWithCooldown = now - skill.lastTimeUseThisSkill > skill.coolDown - 50
```
(`SkillService.canUseSkillWithCooldown`) — có sai số cho phép 50 ms.

---

## 2. Danh sách kỹ năng theo hành tinh

Kỹ năng khởi tạo khi tạo nhân vật (`database/PlayerDAO.createNewPlayer`), thứ tự slot:

| Hành tinh | Danh sách template id (theo thứ tự) | Học sẵn |
|---|---|---|
| Trái Đất (gender 0) | 0, 1, 6, 9, 10, 20, 22, 19 | Dragon cấp 1 |
| Namếc (gender 1) | 2, 3, 7, 11, 12, 17, 18, 19 | Demon cấp 1 |
| Xayda (gender 2) | 4, 5, 8, 13, 14, 21, 23, 19 | Galick cấp 1 |

Tuyệt kỹ (24/25/26) không có sẵn, được thêm vào list khi học ở NPC Whis (xem mục 9, 13).

Các kỹ năng có `point = 0` được load bằng `SkillUtil.createSkillLevel0` (chỉ có template, `skillId = -1`).

---

## 3. Luồng dùng kỹ năng chung

`SkillService.useSkill(player, plTarget, mobTarget, status, msg)`:

1. Chặn nếu: cùng bang trong map Ngọc Rồng Sao Đen (NRSD) hoặc đang cầm Ngọc Rồng Namếc cùng bang → chat "Ê cùng bang mà".
2. Chặn nếu mục tiêu vừa hồi sinh < 1500 ms.
3. Nếu `status == 20` → đọc gói tuyệt kỹ (`skillId, dx, dy, dir, x, y`).
4. Chặn nếu người dùng đang dính hiệu ứng khống chế (`effectSkill.isHaveEffectSkill()`).
5. Nếu `template.type == 2` và đủ mana + hết cooldown → `useSkillBuffToPlayer` (Trị thương).
6. Chặn nếu: không `canAttackPlayer`, mob đã chết, thiếu mana, chưa hết cooldown.
7. Nếu đang dùng Trói → gỡ trói; nếu đang Tái tạo NL → dừng sạc.
8. Rẽ nhánh theo `type`: 1 → `useSkillAttack`, 3 → `useSkillAlone`, 4 → `useNewSkillNotFocus`.

### 3.1 Thể lực (stamina) — trong `useSkillAttack`

| Đối tượng | Quy tắc |
|---|---|
| Người chơi | Mỗi lần đánh `numAttack++`; khi `numAttack == 500` → `stamina--`. Nếu đang có **bùa Dẻo dai** (`charms.tdDeoDai`) thì không tăng `numAttack`. `stamina <= 0` → không đánh được ("Thể lực đã cạn kiệt"). |
| Đệ tử | Mỗi 2 lần đánh (5 lần nếu sư phụ có **bùa Đệ tử** `tdDeTu`) → `stamina--`. `stamina <= 0` → `askPea()` (xin đậu). |
| Boss | Không tốn thể lực. |

Hồi thể lực: `NPoint.update` — mỗi 60 000 ms `stamina++` nếu `< maxStamina`.

### 3.2 Trượt (miss) đòn đấm

Trong `useSkillAttack` với Dragon/Demon/Galick/Liên hoàn/Kaioken:
- Nếu mục tiêu là người và `distance > 100` (trừ map 113) → `miss = true` (sát thương 0).
- Nếu mục tiêu là quái và `distance > 100` → `miss = true`.

Chưởng (Kamejoko/Masenko/Antomic) không có kiểm tra khoảng cách phía server.

### 3.3 Sau khi dùng skill (`affterUseSkill`)

1. Nội tại "sát thương đòn kế" gán `nPoint.dameAfter = param1` (DCTT id 6, Thôi miên id 7, Sôcôla id 14, Trói id 22).
2. Trừ KI theo `manaUseType`.
3. `lastTimeUseThisSkill = now - 1`; nếu có nội tại giảm hồi chiêu (`subTimeParam != 0`) → đặt hẹn giờ `EffectSkillService.setIntrinsic(player, skillId, coolDown, now - coolDown*subTimeParam/100)`; khi `now - lastTimeUseSkill > coolDown` thì `releaseCooldownSkill` → kết quả thực tế: **cooldown hiệu dụng = coolDown × (1 − subTimeParam/100)**.

### 3.4 Đánh người (`playerAttackPlayer`)

```
if target.anTroi: attacker.isCrit100 = true          // đánh mục tiêu đang bị trói → chí mạng chắc chắn
dame = attacker.nPoint.getDameAttack(false)
if attacker.effectSkin.isXDame: (tắt cờ) ; nếu target là boss → dame /= 3
dameHit = target.injured(attacker, miss ? 0 : dame, piercing=false, isMobAttack=false)
damePST = (target đang khiên) ? target.idMark.damePST : dameHit
phanSatThuong(attacker, target, damePST)
hutHPMP(attacker, dameHit, target, null)
nếu target là Yardart: không cho hạ dưới 1 HP / nếu hp <= 10% thì bỏ qua gói tin
nếu cả 2 đều typePk == PK_PVP_2 (4): tnsm = attacker.calSucManhTiemNang(dameHit/10) / (|lvA − lvB| + 1); addSMTN(target, 2, tnsm)
nếu target chết trong map Mabư → attacker.fightMabu.changePoint(5)
```

Chi tiết `injured` (giáp, né, xuyên giáp…) được mô tả ở `docs/03-nhan-vat-chi-so.md`.

### 3.5 Đánh quái (`playerAttackMob`)

```
dameHit = attacker.nPoint.getDameAttack(true)
nếu có bùa Bất tử và hp <= 1 → (người) dameHit = 0
nếu có bùa Mạnh mẽ (tdManhMe) → dameHit += dameHit * 150 / 100      // ×2.5
nếu attacker là đệ tử và sư phụ có bùa Đệ tử → dameHit *= 2
miss → 0 ; cap 2 147 483 647
hutHPMP(attacker, dameHit, null, mob) ; mob.injured(attacker, dameHit, dieWhenHpFull)
```

### 3.6 Hút HP/KI (`hutHPMP`)

```
tlHutHp = nPoint.tlHutHp + (đánh quái ? nPoint.tlHutHpMob : 0)
hpHoi = dame * tlHutHp / 100 ; mpHoi = dame * tlHutMp / 100
PlayerService.hoiPhuc(player, hpHoi, mpHoi)
```

### 3.7 Phản sát thương (`phanSatThuong`)

```
damePST = dame * target.nPoint.tlPST / 100
if damePST >= attacker.hp: damePST = attacker.hp − 1         // phản không giết được
if attacker là boss (trừ Broly/SuperBroly) và damePST > attacker.hpMax/100:
    damePST = attacker.hpMax/100 − rand(0 .. hpMax/200 − 1)   // (chỉ random khi hpMax/200 > 1)
attacker.injured(attacker, damePST, piercing=true, false)
```

---

## 4. Công thức sát thương chung (getDameAttack)

`NPoint.getDameAttack(boolean isAttackMob)` — áp dụng cho mọi skill đánh (trừ các case `return` sớm).

```
setIsCrit():
    if intrinsic.id == 25 && %HP hiện tại <= intrinsic.param1 → isCrit = true
    elif isCrit100 → isCrit = true (reset cờ)
    else isCrit = random(0..99) < crit

dameAttack = nPoint.dame
percentDameIntrinsic = 0 ; percentDameSkill = 0 ; percentXDame = 0
if skill != DCTT && isCritTele: isCrit = true, isCritTele = false     // đòn sau DCTT luôn chí mạng

switch skill:
  DRAGON:   pDI = (intrinsic 1) ; pDS = damage
  KAMEJOKO: pDI = (intrinsic 2) ; pDS = damage ; set songoku=5 → pXD = 100
  GALICK:   pDI = (intrinsic 16); pDS = damage ; set kakarot=5 → pXD = 100
  ANTOMIC:  pDI = (intrinsic 17); pDS = damage
  TU_SAT:   pDS = damage ; cadicM==4 → pXD = 20 ; cadicM==5 → pXD = 50
  DEMON:    pDI = (intrinsic 8) ; pDS = damage
  MASENKO:  (intrinsic 9) pXD += param1 ; nail==5 → pXD += 80 ; pDS = damage + pXD
  LIEN_HOAN:pDI = (intrinsic 13); pDS = damage ; ocTieu=5 → pXD = 100
  KAIOKEN:  pDS = damage ; thanVuTruKaio==5 → pXD = 30
  DCTT:     isCrit = true ; isCritTele = true ; dameAttack = rand[dame − 5%dame, dame + 5%dame]
  MAKANKOSAPPO: return min(INT_MAX, mpMax * damage / 100)   (picolo=5: dameSkill *= 3/2 → xem ghi chú)
  QUA_CAU_KENH_KHI: return công thức riêng (mục 5.5)
  DE_TRUNG: return dame * (pikkoroDaimao==5 ? 4 : 1)   (cap INT_MAX)

if intrinsic.id == 18 && đang biến khỉ: pDI = param1          // áp cho MỌI skill khi đang khỉ
if pDS != 0: dameAttack = dameAttack * pDS / 100
dameAttack += dameAttack * pDI / 100
dameAttack += dameAttack * dameAfter / 100                     // nội tại đòn kế
if effectSkill.isDameBuff && tlSexyDame == 0: dameAttack += dameAttack * tileDameBuff / 100
if isAttackMob:
    for tl in tlDameAttMob: dameAttack += dameAttack * tl / 100
    if là đệ tử && sư phụ có bùa Đệ tử: dameAttack *= 2
dameAfter = 0
if isCrit: dameAttack *= 2 ; dameAttack += dameAttack * tlSDCM / 100
dameAttack += dameAttack * pXD / 100
temp = dameAttack/100*5 (tối thiểu 1)
dameAttack += getOne(−1, 1) * rand(0..temp−1) + 1                // dao động ~±5%
if effectSkin.isXChuong && skill ∈ {KAMEJOKO, ANTOMIC, MASENKO}:
    dameAttack *= nPoint.xChuong ; isXDame = true ; isXChuong = false
cap INT_MAX
```

Chú thích:
- `pDI` = `percentDameIntrinsic`, `pDS` = `percentDameSkill`, `pXD` = `percentXDame`.
- Skill **không có trong switch** (Thôi miên, Trói, Sôcôla, Super Kame, Liên hoàn chưởng, Ma phong ba…) có `pDS = 0` → **không nhân `damage%`**, sát thương = `dame` gốc (+crit…).
- `xChuong` lấy từ option 159 "x# sức đánh đòn chưởng cơ bản mỗi phút"; `EffectSkin.updateXChuong` bật `isXChuong` mỗi 60 000 ms.

---

## 5. Chi tiết từng kỹ năng — Trái Đất

### 5.1 Chiêu đấm Dragon (id 0)

<!-- nclass=0 id=0 -->
Template: `nclass_id=0`, `id=0`, `max_point=7`, `mana_use_type=0`, `TYPE=1`, `icon_id=539`, `slot=0`, `dam_info='Tăng sức đánh: #%'`

| Cấp | skillId | SM yêu cầu | damage | mana_use | cooldown (ms) | dx | dy | price |
|---|---|---|---|---|---|---|---|---|
| 1 | 0 | 1.000 | 100 | 1 | 500 | 32 | 18 | 0 |
| 2 | 1 | 10.000 | 110 | 2 | 500 | 34 | 18 | 10 |
| 3 | 2 | 22.000 | 120 | 4 | 500 | 36 | 18 | 50 |
| 4 | 3 | 66.000 | 130 | 8 | 500 | 38 | 18 | 100 |
| 5 | 4 | 200.000 | 140 | 16 | 500 | 40 | 18 | 500 |
| 6 | 5 | 600.000 | 150 | 32 | 500 | 42 | 18 | 1000 |
| 7 | 6 | 1.800.000 | 160 | 70 | 500 | 44 | 18 | 2000 |

- Nhánh: `useSkillAttack` → cận chiến (trượt nếu > 100px).
- Sát thương: `dame × damage% (100→160%)` × (1 + nội tại 1 [5–25%]).
- Sách: item 66–72.

### 5.2 Chiêu Kamejoko (id 1)

<!-- nclass=0 id=1 -->
Template: `nclass_id=0`, `id=1`, `max_point=7`, `mana_use_type=0`, `TYPE=1`, `icon_id=540`, `slot=1`, `dam_info='Tăng sức đánh: #%'`

| Cấp | skillId | SM yêu cầu | damage | mana_use | cooldown (ms) | dx | dy | price |
|---|---|---|---|---|---|---|---|---|
| 1 | 7 | 10.000 | 150 | 30 | 2.000 | 160 | 160 | 500 |
| 2 | 8 | 20.000 | 200 | 60 | 2.500 | 170 | 170 | 1000 |
| 3 | 9 | 60.000 | 250 | 120 | 3.000 | 180 | 180 | 2000 |
| 4 | 10 | 180.000 | 300 | 240 | 3.500 | 190 | 190 | 4000 |
| 5 | 11 | 540.000 | 350 | 480 | 4.000 | 200 | 200 | 8000 |
| 6 | 12 | 1.600.000 | 400 | 960 | 4.500 | 210 | 210 | 9999 |
| 7 | 13 | 4.800.000 | 450 | 1280 | 5.000 | 220 | 220 | 9999 |

- Chưởng; sát thương `dame × damage% (150→450%)` × (1 + nội tại 2 [5–25%]).
- **Set Sôngôku 5 món** (option 129/141): `percentXDame = 100` → ×2 sau crit.
- Bị **vô hiệu chưởng** (option 3) chặn hoàn toàn; xuyên giáp chưởng (option 98) áp dụng.
- Đệ tử: cooldown ép = 1000 ms.
- Sách: item 94–100.

### 5.3 Thái Dương Hạ San (id 6)

<!-- nclass=0 id=6 -->
Template: `nclass_id=0`, `id=6`, `max_point=7`, `mana_use_type=1`, `TYPE=3`, `icon_id=717`, `slot=2`, `dam_info='Thời gian tác dụng: # mili giây'`

| Cấp | skillId | SM yêu cầu | damage | mana_use | cooldown (ms) | dx | dy | price |
|---|---|---|---|---|---|---|---|---|
| 1 | 42 | 60.000 | 3000 | 45 | 60.000 | 150 | 150 | 500 |
| 2 | 43 | 120.000 | 4000 | 40 | 55.000 | 180 | 180 | 1000 |
| 3 | 44 | 360.000 | 5000 | 35 | 50.000 | 210 | 210 | 2000 |
| 4 | 45 | 1.000.000 | 6000 | 30 | 45.000 | 240 | 240 | 4000 |
| 5 | 46 | 3.200.000 | 7000 | 25 | 40.000 | 270 | 270 | 8000 |
| 6 | 47 | 10.000.000 | 8000 | 20 | 35.000 | 300 | 300 | 9999 |
| 7 | 48 | 30.000.000 | 9000 | 15 | 30.000 | 330 | 330 | 9999 |

- `useSkillAlone`. `timeStun = (point + 2) × 1000` ms (cấp 1 = 3 s, cấp 7 = 9 s; khớp cột damage). **Set Thiên Xin Hăng 5 món** (option 127/139): `timeStun × 2`.
- Phạm vi `120 + point × 30` px (150 → 330, khớp dx).
- Người: mọi `humanoid` trong phạm vi, `canAttackPlayer`, không có **Kháng TDHS** (option 116), bỏ qua sư phụ nếu người dùng là đệ tử. Không áp người ở map offline.
- Quái: mọi quái trong phạm vi (không áp nếu người dùng là boss).
- Nội tại 3: giảm hồi chiêu `param1` (10–35%). `param2` (−KI) **không được cài đặt**.
- Sách: item 115–121.

### 5.4 Kaioken (id 9)

<!-- nclass=0 id=9 -->
Template: `nclass_id=0`, `id=9`, `max_point=7`, `mana_use_type=0`, `TYPE=1`, `icon_id=716`, `slot=3`, `dam_info='Tăng sức đánh: #%'`

| Cấp | skillId | SM yêu cầu | damage | mana_use | cooldown (ms) | dx | dy | price |
|---|---|---|---|---|---|---|---|---|
| 1 | 63 | 150.000.000 | 160 | 9000 | 500 | 32 | 32 | 9999 |
| 2 | 64 | 200.000.000 | 170 | 13000 | 500 | 32 | 32 | 9999 |
| 3 | 65 | 250.000.000 | 180 | 15000 | 500 | 32 | 32 | 9999 |
| 4 | 66 | 300.000.000 | 190 | 18000 | 500 | 32 | 32 | 9999 |
| 5 | 67 | 350.000.000 | 200 | 21000 | 500 | 32 | 32 | 9999 |
| 6 | 68 | 400.000.000 | 210 | 24000 | 500 | 32 | 32 | 9999 |
| 7 | 69 | 450.000.000 | 220 | 27000 | 500 | 32 | 32 | 9999 |

- Trước khi đánh: `hpUse = hpMax/100 × 10` (set Thần Vũ Trụ Kaio 4 món: ×5; 5 món: ×3). Nếu `hp <= hpUse` → không đánh (`break`). Ngược lại trừ HP rồi **rơi xuống nhánh đấm** (fall-through) như Dragon.
- Sát thương `dame × damage% (160→220%)`; set Thần Vũ Trụ Kaio 5 món: `pXD = 30`.
- Sách: item 300–306.

### 5.5 Quả cầu kênh khí (id 10)

<!-- nclass=0 id=10 -->
Template: `nclass_id=0`, `id=10`, `max_point=7`, `mana_use_type=1`, `TYPE=1`, `icon_id=711`, `slot=4`, `dam_info='Gây sát thương #%'`

| Cấp | skillId | SM yêu cầu | damage | mana_use | cooldown (ms) | dx | dy | price |
|---|---|---|---|---|---|---|---|---|
| 1 | 70 | 500.000.000 | 500 | 50 | 360.000 | 300 | 300 | 9999 |
| 2 | 71 | 600.000.000 | 600 | 55 | 350.000 | 400 | 400 | 9999 |
| 3 | 72 | 700.000.000 | 700 | 60 | 340.000 | 500 | 500 | 9999 |
| 4 | 73 | 800.000.000 | 800 | 65 | 330.000 | 600 | 600 | 9999 |
| 5 | 74 | 900.000.000 | 900 | 70 | 320.000 | 700 | 700 | 9999 |
| 6 | 75 | 1.000.000.000 | 1000 | 75 | 310.000 | 800 | 800 | 9999 |
| 7 | 76 | 1.100.000.000 | 1100 | 80 | 300.000 | 900 | 900 | 9999 |

- Lần dùng 1: `prepareQCKK = true`, gửi gồng 4000 ms. Lần dùng 2: ném.
- Công thức (`getDameAttack` case QCKK), R = `getRangeQCKK(point) = 350 + point × 30`:

```
hpmob = Σ hp các quái còn sống trong R quanh người ném
hppl  = Σ hp các humanoid còn sống (khác bản thân) trong R
dame  = hpmob × 10% + hppl × 10% + nPoint.dame × 10
if set Kirin 5 món: dame × 2
dame += rand(−5..5) × dame / 100 ; cap INT_MAX
```

- **Cột `damage` (500–1100%) không được dùng.** Không crit, không nội tại sát thương.
- Nếu mục tiêu là người: đánh người đó + mọi quái trong R quanh người đó (mỗi quái nhận `getDameAttack(true)`, `dieWhenHpFull = true`). Nếu mục tiêu là quái: đánh quái đó + quái trong R.
- Nội tại 4: giảm hồi chiêu 15–55%.
- Sách: item 307–313.

### 5.6 Dịch chuyển tức thời (id 20)

<!-- nclass=0 id=20 -->
Template: `nclass_id=0`, `id=20`, `max_point=7`, `mana_use_type=0`, `TYPE=1`, `icon_id=3783`, `slot=5`, `dam_info='Dịch chuyển tức thời và gây choáng kẻ thù'`

| Cấp | skillId | SM yêu cầu | damage | mana_use | cooldown (ms) | dx | dy | price |
|---|---|---|---|---|---|---|---|---|
| 1 | 128 | 10.000.000 | 1000 | 5000 | 20.000 | 5000 | 5000 | 9999 |
| 2 | 129 | 25.000.000 | 1500 | 7000 | 19.000 | 5000 | 5000 | 9999 |
| 3 | 130 | 50.000.000 | 2000 | 10000 | 18.000 | 5000 | 5000 | 9999 |
| 4 | 131 | 125.000.000 | 2500 | 15000 | 17.000 | 5000 | 5000 | 9999 |
| 5 | 132 | 625.000.000 | 3000 | 20000 | 16.000 | 5000 | 5000 | 9999 |
| 6 | 133 | 3.125.000.000 | 3500 | 25000 | 15.000 | 5000 | 5000 | 9999 |
| 7 | 134 | 15.625.000.000 | 4000 | 30000 | 14.000 | 5000 | 5000 | 9999 |

- Dịch chuyển tới vị trí mục tiêu (`setPos`), đánh 1 đòn: `dame ± 5%`, **luôn chí mạng** (×2 + tlSDCM), không nhân `damage%`.
- Choáng mục tiêu `getTimeDCTT(point) = (point + 1) × 500` ms (1 s → 4 s).
- Đặt `isCrit100 = true` và `isCritTele = true` → đòn kế tiếp cũng chí mạng.
- Nội tại 6: đòn kế +50–150% (`dameAfter`).
- Sách: item 488–494.

### 5.7 Thôi miên (id 22)

<!-- nclass=0 id=22 -->
Template: `nclass_id=0`, `id=22`, `max_point=7`, `mana_use_type=0`, `TYPE=1`, `icon_id=3782`, `slot=6`, `dam_info='Ru ngủ kẻ thù # giây'`

| Cấp | skillId | SM yêu cầu | damage | mana_use | cooldown (ms) | dx | dy | price |
|---|---|---|---|---|---|---|---|---|
| 1 | 142 | 10.000.000 | 5 | 10000 | 30.000 | 200 | 200 | 9999 |
| 2 | 143 | 25.000.000 | 6 | 10000 | 32.000 | 200 | 200 | 9999 |
| 3 | 144 | 50.000.000 | 7 | 10000 | 34.000 | 200 | 200 | 9999 |
| 4 | 145 | 125.000.000 | 8 | 10000 | 36.000 | 200 | 200 | 9999 |
| 5 | 146 | 625.000.000 | 9 | 10000 | 38.000 | 200 | 200 | 9999 |
| 6 | 147 | 3.125.000.000 | 10 | 10000 | 40.000 | 200 | 200 | 9999 |
| 7 | 148 | 15.625.000.000 | 11 | 10000 | 42.000 | 200 | 200 | 9999 |

- Ngủ `getTimeThoiMien(point) = (point + 4) × 1000` ms (5 s → 11 s, khớp cột damage). Không gây sát thương.
- Nội tại 7: đòn kế +50–150%.
- Sách: item 495–501.

### 5.8 Super Kamejoko (id 24) — xem mục 9

---

## 6. Chi tiết từng kỹ năng — Namếc

### 6.1 Chiêu đấm Demon (id 2)

<!-- nclass=1 id=2 -->
Template: `nclass_id=1`, `id=2`, `max_point=7`, `mana_use_type=0`, `TYPE=1`, `icon_id=539`, `slot=0`, `dam_info='Tăng sức đánh: #%'`

| Cấp | skillId | SM yêu cầu | damage | mana_use | cooldown (ms) | dx | dy | price |
|---|---|---|---|---|---|---|---|---|
| 1 | 14 | 1.000 | 95 | 1 | 400 | 24 | 18 | 0 |
| 2 | 15 | 10.000 | 105 | 2 | 400 | 26 | 18 | 10 |
| 3 | 16 | 22.000 | 115 | 4 | 400 | 28 | 18 | 50 |
| 4 | 17 | 66.000 | 125 | 8 | 400 | 30 | 18 | 100 |
| 5 | 18 | 200.000 | 135 | 16 | 400 | 32 | 18 | 1000 |
| 6 | 19 | 600.000 | 145 | 32 | 400 | 34 | 18 | 2000 |
| 7 | 20 | 1.800.000 | 155 | 70 | 400 | 36 | 18 | 4000 |

- Cận chiến; `dame × damage% (95→155%)` × (1 + nội tại 8).
- Sách: item 79–84 và 86 (item 85 không map).

### 6.2 Chiêu Masenko (id 3)

<!-- nclass=1 id=3 -->
Template: `nclass_id=1`, `id=3`, `max_point=7`, `mana_use_type=0`, `TYPE=1`, `icon_id=540`, `slot=1`, `dam_info='Tăng sức đánh: #%'`

| Cấp | skillId | SM yêu cầu | damage | mana_use | cooldown (ms) | dx | dy | price |
|---|---|---|---|---|---|---|---|---|
| 1 | 21 | 10.000 | 100 | 8 | 800 | 140 | 140 | 500 |
| 2 | 22 | 20.000 | 110 | 16 | 790 | 150 | 150 | 1000 |
| 3 | 23 | 60.000 | 120 | 32 | 780 | 160 | 160 | 2000 |
| 4 | 24 | 180.000 | 130 | 64 | 760 | 170 | 170 | 4000 |
| 5 | 25 | 540.000 | 140 | 128 | 740 | 180 | 180 | 8000 |
| 6 | 26 | 1.600.000 | 150 | 256 | 720 | 190 | 190 | 9999 |
| 7 | 27 | 4.800.000 | 160 | 512 | 700 | 200 | 200 | 9999 |

- Chưởng. Công thức riêng:
```
pXD = (intrinsic 9 ? param1 : 0) + (set Nail 5 món ? 80 : 0)
pDS = damage + pXD
dame = dame × pDS/100 ; ... crit ... ; dame += dame × pXD/100
```
- Hồi chiêu giảm: nội tại 9 `param1`% + set Nail 4 món +20% / 5 món +50% (cộng dồn vào `subTimeParam`).
- Đệ tử: cooldown ép = 1000 ms. Sách: item 101–107.

### 6.3 Trị thương (id 7)

<!-- nclass=1 id=7 -->
Template: `nclass_id=1`, `id=7`, `max_point=7`, `mana_use_type=1`, `TYPE=2`, `icon_id=724`, `slot=2`, `dam_info='Phục hồi #% HP và KI cho đồng đội'`

| Cấp | skillId | SM yêu cầu | damage | mana_use | cooldown (ms) | dx | dy | price |
|---|---|---|---|---|---|---|---|---|
| 1 | 49 | 60.000 | 50 | 40 | 30.000 | 100 | 100 | 500 |
| 2 | 50 | 120.000 | 55 | 35 | 32.000 | 105 | 105 | 1000 |
| 3 | 51 | 360.000 | 60 | 30 | 34.000 | 110 | 110 | 2000 |
| 4 | 52 | 1.000.000 | 65 | 25 | 38.000 | 115 | 115 | 4000 |
| 5 | 53 | 3.200.000 | 70 | 20 | 40.000 | 120 | 120 | 8000 |
| 6 | 54 | 10.000.000 | 75 | 15 | 42.000 | 125 | 125 | 9999 |
| 7 | 55 | 30.000.000 | 80 | 10 | 44.000 | 130 | 130 | 9999 |

- `useSkillBuffToPlayer`, `percent = getPercentTriThuong(point) = (point + 9) × 5` (50% → 80%).
- Điều kiện `canHsPlayer(player, target)`: target không phải boss, không `PK_ALL`/`PK_PVP`; nếu người dùng có cờ thì target phải cùng cờ hoặc không cờ; nếu người dùng không cờ thì target cũng phải không cờ.
- Cấp > 1: thêm mọi người không phải boss trong 300px (điều kiện kiểm tra lại `canHsPlayer(player, plTarget)` — tức là kiểm tra mục tiêu chính, không phải từng người).
- Với mỗi người được hồi: **người dùng** +`hpMax×percent%` HP; **target** +`hpMax×percent%` HP và +`mpMax×percent%` KI; nếu target đang chết → hồi sinh.
- Nội tại 10: giảm hồi chiêu 15–65%. Sách: item 122–128.

### 6.4 Makankosappo (id 11)

<!-- nclass=1 id=11 -->
Template: `nclass_id=1`, `id=11`, `max_point=7`, `mana_use_type=2`, `TYPE=1`, `icon_id=723`, `slot=3`, `dam_info='Gây sát thương #%'`

| Cấp | skillId | SM yêu cầu | damage | mana_use | cooldown (ms) | dx | dy | price |
|---|---|---|---|---|---|---|---|---|
| 1 | 77 | 150.000.000 | 70 | 0 | 360.000 | 20000 | 20000 | 9999 |
| 2 | 78 | 200.000.000 | 80 | 0 | 350.000 | 20000 | 20000 | 9999 |
| 3 | 79 | 250.000.000 | 90 | 0 | 340.000 | 20000 | 20000 | 9999 |
| 4 | 80 | 300.000.000 | 100 | 0 | 330.000 | 20000 | 20000 | 9999 |
| 5 | 81 | 350.000.000 | 110 | 0 | 320.000 | 20000 | 20000 | 9999 |
| 6 | 82 | 400.000.000 | 120 | 0 | 310.000 | 20000 | 20000 | 9999 |
| 7 | 83 | 450.000.000 | 130 | 0 | 300.000 | 20000 | 20000 | 9999 |

- Lần 1: gồng 3000 ms; lần 2: bắn.
- Sát thương: `min(INT_MAX, mpMax × damage / 100)` (70% → 130% **KI tối đa**), `return` ngay: không crit, không nội tại, không dao động.
- `mana_use_type = 2`: yêu cầu `mp > 0`, dùng xong KI = 0.
- Nội tại 11: giảm hồi chiêu. Sách: item 328–334.

### 6.5 Đẻ trứng (id 12)

<!-- nclass=1 id=12 -->
Template: `nclass_id=1`, `id=12`, `max_point=7`, `mana_use_type=1`, `TYPE=3`, `icon_id=722`, `slot=4`, `dam_info='Tạo quái đi theo hỗ trợ'`

| Cấp | skillId | SM yêu cầu | damage | mana_use | cooldown (ms) | dx | dy | price |
|---|---|---|---|---|---|---|---|---|
| 1 | 84 | 500.000.000 | 50 | 20 | 360.000 | 200 | 200 | 9999 |
| 2 | 85 | 600.000.000 | 55 | 30 | 390.000 | 200 | 200 | 9999 |
| 3 | 86 | 700.000.000 | 60 | 40 | 420.000 | 200 | 200 | 9999 |
| 4 | 87 | 800.000.000 | 65 | 50 | 450.000 | 200 | 200 | 9999 |
| 5 | 88 | 900.000.000 | 70 | 60 | 480.000 | 200 | 200 | 9999 |
| 6 | 89 | 1.000.000.000 | 75 | 70 | 510.000 | 200 | 200 | 9999 |
| 7 | 90 | 1.100.000.000 | 80 | 80 | 540.000 | 200 | 200 | 9999 |

- Huỷ MobMe cũ (nếu có) và tạo `new MobMe(player)` (`mob/MobMe.java`):

| Thuộc tính | Công thức |
|---|---|
| Template quái | `{8, 11, 32, 25, 43, 49, 50}[point−1]` |
| HP | `hpMax × {30,40,50,60,70,80,90}[point−1] / 100` |
| Sát thương | `getDameAttack(false) × {30..90}% ` (cùng bảng %) |
| Thời gian tồn tại | `getTimeMonkey(point) × 2 = (point + 5) × 20 000` ms |
| Nhận sát thương | mỗi đòn bị cap `maxHp / 20` |

- MobMe đánh cùng lúc chủ đánh (`mobMe.attack` trong `useSkillAttack`): với người → chỉ đánh khi `hp mục tiêu > dame` và `hp > 5% hpMax` (piercing), với quái → chỉ khi `hp quái > dame`, chủ nhận TNSM.
- **Set Pikkoro Daimao 5 món**: trứng bất tử theo thời gian, bỏ điều kiện 5% HP, sát thương chủ ×4 (qua `getDameAttack` case DE_TRUNG).
- Nội tại 12: giảm hồi chiêu. Sách: item 335–341.

### 6.6 Liên hoàn (id 17)

<!-- nclass=1 id=17 -->
Template: `nclass_id=1`, `id=17`, `max_point=7`, `mana_use_type=0`, `TYPE=1`, `icon_id=3778`, `slot=5`, `dam_info='Tăng sức đánh: #%'`

| Cấp | skillId | SM yêu cầu | damage | mana_use | cooldown (ms) | dx | dy | price |
|---|---|---|---|---|---|---|---|---|
| 1 | 107 | 10.000.000 | 160 | 100 | 350 | 30 | 30 | 9999 |
| 2 | 108 | 25.000.000 | 165 | 200 | 345 | 35 | 35 | 9999 |
| 3 | 109 | 50.000.000 | 170 | 300 | 340 | 40 | 40 | 9999 |
| 4 | 110 | 125.000.000 | 175 | 400 | 335 | 45 | 45 | 9999 |
| 5 | 111 | 625.000.000 | 180 | 500 | 330 | 50 | 50 | 9999 |
| 6 | 112 | 3.125.000.000 | 185 | 600 | 335 | 55 | 55 | 9999 |
| 7 | 113 | 15.625.000.000 | 190 | 700 | 330 | 60 | 60 | 9999 |

- Cận chiến; `dame × damage% (160→190%)` × (1 + nội tại 13); set Ốc Tiêu 5 món: `pXD = 100`.
- Sách: item 481–487.

### 6.7 Biến Sôcôla (id 18)

<!-- nclass=1 id=18 -->
Template: `nclass_id=1`, `id=18`, `max_point=7`, `mana_use_type=1`, `TYPE=1`, `icon_id=3780`, `slot=6`, `dam_info='Biến quái thành Sôcôla'`

| Cấp | skillId | SM yêu cầu | damage | mana_use | cooldown (ms) | dx | dy | price |
|---|---|---|---|---|---|---|---|---|
| 1 | 114 | 10.000.000 | 15 | 22 | 30.000 | 500 | 500 | 9999 |
| 2 | 115 | 25.000.000 | 17 | 20 | 29.000 | 500 | 500 | 9999 |
| 3 | 116 | 50.000.000 | 19 | 18 | 28.000 | 500 | 500 | 9999 |
| 4 | 117 | 125.000.000 | 21 | 16 | 27.000 | 500 | 500 | 9999 |
| 5 | 118 | 625.000.000 | 23 | 14 | 26.000 | 500 | 500 | 9999 |
| 6 | 119 | 3.125.000.000 | 25 | 12 | 25.000 | 500 | 500 | 9999 |
| 7 | 120 | 15.625.000.000 | 27 | 10 | 24.000 | 500 | 500 | 9999 |

- Thời gian `getTimeSocola() = 30 000` ms **cho mọi cấp** (cột damage 15–27 không dùng). Người: `setSocola` + đổi ngoại hình (head 412/body 413/leg 414). Quái: `sendMobToSocola`.
- Nội tại 14: đòn kế +50–150%. Sách: item 474–480.

### 6.8 Ma phong ba (id 26) — xem mục 9

---

## 7. Chi tiết từng kỹ năng — Xayda

### 7.1 Chiêu đấm Galick (id 4)

<!-- nclass=2 id=4 -->
Template: `nclass_id=2`, `id=4`, `max_point=7`, `mana_use_type=0`, `TYPE=1`, `icon_id=539`, `slot=0`, `dam_info='Tăng sức đánh: #%'`

| Cấp | skillId | SM yêu cầu | damage | mana_use | cooldown (ms) | dx | dy | price |
|---|---|---|---|---|---|---|---|---|
| 1 | 28 | 1.000 | 110 | 1 | 500 | 36 | 18 | 0 |
| 2 | 29 | 10.000 | 120 | 2 | 500 | 37 | 18 | 10 |
| 3 | 30 | 22.000 | 130 | 4 | 500 | 38 | 18 | 50 |
| 4 | 31 | 66.000 | 140 | 8 | 500 | 39 | 18 | 100 |
| 5 | 32 | 200.000 | 150 | 16 | 500 | 40 | 18 | 1000 |
| 6 | 33 | 600.000 | 160 | 32 | 500 | 41 | 18 | 2000 |
| 7 | 34 | 1.800.000 | 170 | 70 | 500 | 42 | 18 | 4000 |

- Cận chiến; `dame × damage% (110→170%)` × (1 + nội tại 16); set Kakarot 5 món: `pXD = 100`.
- Sách: item 87–93.

### 7.2 Chiêu Antomic (id 5)

<!-- nclass=2 id=5 -->
Template: `nclass_id=2`, `id=5`, `max_point=7`, `mana_use_type=0`, `TYPE=1`, `icon_id=540`, `slot=1`, `dam_info='Tăng sức đánh: #%'`

| Cấp | skillId | SM yêu cầu | damage | mana_use | cooldown (ms) | dx | dy | price |
|---|---|---|---|---|---|---|---|---|
| 1 | 35 | 10.000 | 110 | 18 | 1.000 | 150 | 150 | 500 |
| 2 | 36 | 20.000 | 140 | 34 | 1.200 | 160 | 160 | 1000 |
| 3 | 37 | 60.000 | 170 | 68 | 1.400 | 170 | 170 | 2000 |
| 4 | 38 | 180.000 | 200 | 136 | 1.600 | 180 | 180 | 4000 |
| 5 | 39 | 540.000 | 230 | 258 | 1.800 | 190 | 190 | 8000 |
| 6 | 40 | 1.600.000 | 260 | 514 | 2.000 | 200 | 200 | 9999 |
| 7 | 41 | 4.800.000 | 290 | 1026 | 2.200 | 210 | 210 | 9999 |

- Chưởng; `dame × damage% (110→290%)` × (1 + nội tại 17). Đệ tử: cooldown 1000 ms. Sách: item 108–114.

### 7.3 Tái tạo năng lượng (id 8)

<!-- nclass=2 id=8 -->
Template: `nclass_id=2`, `id=8`, `max_point=7`, `mana_use_type=1`, `TYPE=3`, `icon_id=720`, `slot=2`, `dam_info='Tự tái tạo HP MP #%/s'`

| Cấp | skillId | SM yêu cầu | damage | mana_use | cooldown (ms) | dx | dy | price |
|---|---|---|---|---|---|---|---|---|
| 1 | 56 | 60.000 | 4 | 0 | 55.000 | 0 | 0 | 500 |
| 2 | 57 | 120.000 | 5 | 0 | 50.000 | 0 | 0 | 1000 |
| 3 | 58 | 360.000 | 6 | 0 | 45.000 | 0 | 0 | 2000 |
| 4 | 59 | 1.000.000 | 7 | 0 | 40.000 | 0 | 0 | 4000 |
| 5 | 60 | 3.200.000 | 8 | 0 | 35.000 | 0 | 0 | 8000 |
| 6 | 61 | 10.000.000 | 9 | 0 | 30.000 | 0 | 0 | 9999 |
| 7 | 62 | 30.000.000 | 10 | 0 | 25.000 | 0 | 0 | 9999 |

- `EffectSkillService.startCharge`: `isCharging = true`.
- Mỗi lần `NPoint.update()` (≈ 1 lần/giây: `Manager` lên lịch `Zone.update()` mỗi 1 s → `Zone.udPlayer` → `Player.update`), tối đa 10 lần:
```
tiLe = getPercentCharge(point) = point + 3        // 4%..10%, khớp cột damage
nếu không chết, không bị khống chế, (hp < hpMax || mp < mpMax):
    hoiPhuc(hpMax/100 × tiLe, mpMax/100 × tiLe)
ngược lại → stopCharge
countCharging >= 10 → stopCharge
```
- Bị ngắt khi dùng skill khác hoặc di chuyển (`PlayerService.playerMove`).
- Sách: item 129–135.

### 7.4 Biến hình / Biến khỉ (id 13)

<!-- nclass=2 id=13 -->
Template: `nclass_id=2`, `id=13`, `max_point=7`, `mana_use_type=1`, `TYPE=3`, `icon_id=718`, `slot=3`, `dam_info='Tăng sức đánh, HP và tốc độ'`

| Cấp | skillId | SM yêu cầu | damage | mana_use | cooldown (ms) | dx | dy | price |
|---|---|---|---|---|---|---|---|---|
| 1 | 91 | 250.000.000 | 100 | 10 | 300.000 | 200 | 200 | 9999 |
| 2 | 92 | 350.000.000 | 100 | 10 | 310.000 | 200 | 200 | 9999 |
| 3 | 93 | 450.000.000 | 100 | 10 | 320.000 | 200 | 200 | 9999 |
| 4 | 94 | 550.000.000 | 100 | 10 | 330.000 | 200 | 200 | 9999 |
| 5 | 95 | 650.000.000 | 100 | 10 | 340.000 | 200 | 200 | 9999 |
| 6 | 96 | 750.000.000 | 100 | 10 | 350.000 | 200 | 200 | 9999 |
| 7 | 97 | 850.000.000 | 100 | 10 | 360.000 | 200 | 200 | 9999 |

- `startUseSkillMonkey`: 1500 ms hoá khỉ (tốc độ = 0, tính là bị khống chế `isUseSkillMonkey`), sau đó `setIsMonkey`:

| Thuộc tính | Công thức |
|---|---|
| Thời gian | `(point + 5) × 10 000` ms (60 s → 120 s); **set Cađíc 5 món** ×5 |
| HP tối đa | `+ (point + 3) × 10 %` (40% → 100%) |
| Sức đánh | `+ (point + 3) %` (4% → 10%) |
| Chí mạng | `crit = 110` (luôn chí mạng) |
| HP hiện tại | `hp × 2` khi biến |
| Tiêu hao KI bay | chia đôi (`mpg / 200`) |
| Ngoại hình | head `HEADMONKEY[point−1]` = {192,195,196,199,197,200,198}, body 193, leg 194 |

- Nội tại 18: +5–25% sát thương **mọi đòn** khi đang khỉ. Khi hết giờ: `monkeyDown` (hp cắt về hpMax).
- Sách: item 314–320.

### 7.5 Tự phát nổ (id 14)

<!-- nclass=2 id=14 -->
Template: `nclass_id=2`, `id=14`, `max_point=7`, `mana_use_type=1`, `TYPE=3`, `icon_id=2248`, `slot=4`, `dam_info='Hy sinh, gây sát thương lớn cho kẻ thù'`

| Cấp | skillId | SM yêu cầu | damage | mana_use | cooldown (ms) | dx | dy | price |
|---|---|---|---|---|---|---|---|---|
| 1 | 98 | 250.000.000 | 100 | 50 | 120.000 | 200 | 200 | 9999 |
| 2 | 99 | 300.000.000 | 105 | 50 | 120.000 | 300 | 300 | 9999 |
| 3 | 100 | 350.000.000 | 110 | 50 | 120.000 | 400 | 400 | 9999 |
| 4 | 101 | 400.000.000 | 115 | 50 | 120.000 | 500 | 500 | 9999 |
| 5 | 102 | 450.000.000 | 120 | 50 | 120.000 | 600 | 600 | 9999 |
| 6 | 103 | 500.000.000 | 125 | 50 | 120.000 | 700 | 700 | 9999 |
| 7 | 104 | 550.000.000 | 130 | 50 | 120.000 | 900 | 900 | 9999 |

- Lần 1: gồng (`sendPlayerPrepareBom` 2000 ms). Lần 2: nếu người chơi bấm lại **< 1500 ms** → huỷ, vẫn tính cooldown. Boss/đệ tử `sleep(1500)`.
- Công thức:
```
range = 400 + point × 30         (set Cađíc M đúng 2 món: +200)
dame  = hp hiện tại
      + hpMax × 20% (Cađíc M 4 món) | + hpMax × 50% (Cađíc M 5 món)
quái trong range: mob.injured(player, dame, dieWhenHpFull=true)
người trong range (canAttackPlayer, không phải map offline):
    dame = target là boss ? (đang khỉ ? dame/3 : dame/2) : dame     // gán đè biến dame
    target.injured(player, dame, piercing = isMapYardart, false)
người dùng chết (trừ boss/đệ tử); gỡ Huýt sáo nếu có
```
- Cột `damage` (100–130) chỉ dùng trong `getDameAttack` case TU_SAT — nhưng nhánh nổ **không gọi** `getDameAttack`.
- Nội tại 19: giảm hồi chiêu. Sách: item 321–327.

### 7.6 Huýt sáo (id 21)

<!-- nclass=2 id=21 -->
Template: `nclass_id=2`, `id=21`, `max_point=7`, `mana_use_type=0`, `TYPE=3`, `icon_id=3781`, `slot=5`, `dam_info='Tăng tạm thời +#%HP cho mọi người xung quanh và +1 đòn chí mạng'`

| Cấp | skillId | SM yêu cầu | damage | mana_use | cooldown (ms) | dx | dy | price |
|---|---|---|---|---|---|---|---|---|
| 1 | 135 | 10.000.000 | 40 | 50 | 210.000 | 500 | 500 | 9999 |
| 2 | 136 | 25.000.000 | 50 | 45 | 205.000 | 500 | 500 | 9999 |
| 3 | 137 | 50.000.000 | 60 | 40 | 200.000 | 500 | 500 | 9999 |
| 4 | 138 | 125.000.000 | 70 | 35 | 195.000 | 500 | 500 | 9999 |
| 5 | 139 | 625.000.000 | 80 | 30 | 190.000 | 500 | 500 | 9999 |
| 6 | 140 | 3.125.000.000 | 90 | 25 | 185.000 | 500 | 500 | 9999 |
| 7 | 141 | 15.625.000.000 | 100 | 20 | 180.000 | 500 | 500 | 9999 |

- `tileHP = getPercentHPHuytSao(point) = (point + 3) × 10` (40% → 100%).
- Không phải boss, map không offline: duyệt **toàn bộ humanoid trong zone** (không giới hạn khoảng cách):
  - Mọi người đang dùng Trói → bị gỡ trói.
  - Người **không phải Namếc** và **cùng cờ** (`cFlag`) với người dùng: `setStartHuytSao(tileHP)` (HP tối đa +tileHP% trong `setHpMax`), `hp += hp × tileHP/100`, icon 3781 30 s.
  - Người **Namếc** cùng cờ: `hp −= hpMax × 10%` (chỉ khi đủ máu).
- Boss dùng: buff cho tất cả boss trong zone. Map offline: chỉ buff bản thân.
- Hiệu lực `30 000` ms (`EffectSkill.update`).
- "+1 đòn chí mạng" trong `dam_info` **không được cài đặt**.
- Nội tại 21: giảm hồi chiêu. Sách: item 509–515.

### 7.7 Trói (id 23)

<!-- nclass=2 id=23 -->
Template: `nclass_id=2`, `id=23`, `max_point=7`, `mana_use_type=0`, `TYPE=1`, `icon_id=3779`, `slot=6`, `dam_info='Trói kẻ thù'`

| Cấp | skillId | SM yêu cầu | damage | mana_use | cooldown (ms) | dx | dy | price |
|---|---|---|---|---|---|---|---|---|
| 1 | 149 | 10.000.000 | 5 | 5000 | 15.000 | 150 | 150 | 9999 |
| 2 | 150 | 25.000.000 | 10 | 10000 | 20.000 | 150 | 150 | 9999 |
| 3 | 151 | 50.000.000 | 15 | 15000 | 25.000 | 150 | 150 | 9999 |
| 4 | 152 | 125.000.000 | 20 | 20000 | 30.000 | 150 | 150 | 9999 |
| 5 | 153 | 625.000.000 | 25 | 25000 | 35.000 | 150 | 150 | 9999 |
| 6 | 154 | 3.125.000.000 | 30 | 30000 | 40.000 | 150 | 150 | 9999 |
| 7 | 155 | 15.625.000.000 | 35 | 32000 | 45.000 | 150 | 150 | 9999 |

- `timeHold = getTimeTroi(point) = point × 5000` ms (5 s → 35 s). Nếu mục tiêu là boss `BABY` hoặc quái `GauTuongCuop` → 5000 ms.
- Người dùng: `useTroi` (bị tính là đang dùng, di chuyển/dùng skill sẽ gỡ). Mục tiêu người: chỉ bị trói nếu không đang gồng QCKK/Laze/Tự sát → `anTroi` (khống chế).
- Đánh mục tiêu đang bị trói: chí mạng chắc chắn (`isCrit100`).
- Gỡ trói khi: hết giờ, người trói chết, người trói bị khống chế.
- Nội tại 22: đòn kế +50–150%. Sách: item 502–508.

### 7.8 Cađíc liên hoàn chưởng (id 25) — xem mục 9

---

## 8. Kỹ năng dùng chung: Khiên năng lượng

<!-- nclass=0 id=19 -->
Template: `nclass_id=0`, `id=19`, `max_point=7`, `mana_use_type=1`, `TYPE=3`, `icon_id=3784`, `slot=7`, `dam_info='Vô hiệu các đòn tấn công'`

| Cấp | skillId | SM yêu cầu | damage | mana_use | cooldown (ms) | dx | dy | price |
|---|---|---|---|---|---|---|---|---|
| 1 | 121 | 10.000.000 | 15 | 51 | 75.000 | 0 | 0 | 9999 |
| 2 | 122 | 25.000.000 | 20 | 48 | 80.000 | 0 | 0 | 9999 |
| 3 | 123 | 50.000.000 | 25 | 45 | 85.000 | 0 | 0 | 9999 |
| 4 | 124 | 125.000.000 | 30 | 42 | 90.000 | 0 | 0 | 9999 |
| 5 | 125 | 625.000.000 | 35 | 39 | 95.000 | 0 | 0 | 9999 |
| 6 | 126 | 3.125.000.000 | 40 | 36 | 100.000 | 0 | 0 | 9999 |
| 7 | 127 | 15.625.000.000 | 45 | 33 | 105.000 | 0 | 0 | 9999 |

(Dữ liệu giống hệt cho nclass 1 và 2.)

- `setStartShield`: `timeShield = getTimeShield(point) = (point + 2) × 5000` ms (15 s → 45 s, khớp cột damage).
- Trong `Player.injured` (đòn **không** piercing, **không** phải quái đánh):
```
idMark.damePST = damage (trước khiên)       // phản sát thương tính theo con số này
if damage > hpMax: breakShield()             // vỡ khiên
damage = 1 (map phó bản: 10)
```
- Quái đánh (`isMobAttack = true`) và đòn piercing **xuyên qua khiên**.
- Nội tại 5 (TĐ) / 15 (NM) / 20 (XD): giảm hồi chiêu 15–55%. Sách: item 434–440.

---

## 9. Tuyệt kỹ (skill 9)

<!-- nclass=0 id=24 -->
Template: `nclass_id=0`, `id=24`, `max_point=9`, `mana_use_type=1`, `TYPE=4`, `icon_id=11162`, `slot=8`, `dam_info='Tăng sức đánh: #%'`

| Cấp | skillId | SM yêu cầu | damage | mana_use | cooldown (ms) | dx | dy | price |
|---|---|---|---|---|---|---|---|---|
| 1 | 156 | 60.000.000.000 | 550 | 80 | 170.000 | 190 | 25 | 9999 |
| 2 | 157 | 60.000.000.000 | 600 | 75 | 160.000 | 200 | 30 | 9999 |
| 3 | 158 | 60.000.000.000 | 650 | 70 | 150.000 | 210 | 35 | 9999 |
| 4 | 159 | 60.000.000.000 | 700 | 65 | 140.000 | 230 | 40 | 9999 |
| 5 | 160 | 60.000.000.000 | 750 | 60 | 130.000 | 250 | 45 | 9999 |
| 6 | 161 | 60.000.000.000 | 800 | 55 | 120.000 | 270 | 50 | 9999 |
| 7 | 162 | 60.000.000.000 | 850 | 50 | 110.000 | 290 | 55 | 9999 |
| 8 | 163 | 60.000.000.000 | 900 | 45 | 100.000 | 310 | 60 | 9999 |
| 9 | 164 | 60.000.000.000 | 950 | 40 | 90.000 | 330 | 65 | 9999 |
| 10 | 165 | 60.000.000.000 | 1000 | 35 | 80.000 | 350 | 70 | 9999 |

<!-- nclass=1 id=26 -->
Template: `nclass_id=1`, `id=26`, `max_point=9`, `mana_use_type=1`, `TYPE=4`, `icon_id=11194`, `slot=8`, `dam_info='Nhốt đối thủ vào bình chứa'`

| Cấp | skillId | SM yêu cầu | damage | mana_use | cooldown (ms) | dx | dy | price |
|---|---|---|---|---|---|---|---|---|
| 1 | 166 | 60.000.000.000 | 550 | 80 | 170.000 | 83 | 83 | 9999 |
| 2 | 167 | 60.000.000.000 | 600 | 75 | 160.000 | 95 | 95 | 9999 |
| 3 | 168 | 60.000.000.000 | 650 | 70 | 150.000 | 107 | 107 | 9999 |
| 4 | 169 | 60.000.000.000 | 700 | 65 | 140.000 | 119 | 119 | 9999 |
| 5 | 170 | 60.000.000.000 | 750 | 60 | 130.000 | 130 | 130 | 9999 |
| 6 | 171 | 60.000.000.000 | 800 | 55 | 120.000 | 142 | 142 | 9999 |
| 7 | 172 | 60.000.000.000 | 850 | 50 | 110.000 | 154 | 154 | 9999 |
| 8 | 173 | 60.000.000.000 | 900 | 45 | 100.000 | 165 | 165 | 9999 |
| 9 | 174 | 60.000.000.000 | 950 | 40 | 90.000 | 177 | 177 | 9999 |
| 10 | 175 | 60.000.000.000 | 1000 | 35 | 80.000 | 188 | 188 | 9999 |

<!-- nclass=2 id=25 -->
Template: `nclass_id=2`, `id=25`, `max_point=9`, `mana_use_type=1`, `TYPE=4`, `icon_id=11193`, `slot=8`, `dam_info='Tăng sức đánh: #%'`

| Cấp | skillId | SM yêu cầu | damage | mana_use | cooldown (ms) | dx | dy | price |
|---|---|---|---|---|---|---|---|---|
| 1 | 176 | 60.000.000.000 | 550 | 80 | 170.000 | 120 | 120 | 9999 |
| 2 | 177 | 60.000.000.000 | 600 | 75 | 160.000 | 130 | 130 | 9999 |
| 3 | 178 | 60.000.000.000 | 650 | 70 | 150.000 | 140 | 140 | 9999 |
| 4 | 179 | 60.000.000.000 | 700 | 65 | 140.000 | 150 | 150 | 9999 |
| 5 | 180 | 60.000.000.000 | 750 | 60 | 130.000 | 160 | 160 | 9999 |
| 6 | 181 | 60.000.000.000 | 800 | 55 | 120.000 | 170 | 170 | 9999 |
| 7 | 182 | 60.000.000.000 | 850 | 50 | 110.000 | 180 | 180 | 9999 |
| 8 | 183 | 60.000.000.000 | 900 | 45 | 100.000 | 190 | 190 | 9999 |
| 9 | 184 | 60.000.000.000 | 950 | 40 | 90.000 | 200 | 200 | 9999 |
| 10 | 185 | 60.000.000.000 | 1000 | 35 | 80.000 | 210 | 210 | 9999 |

> DB có 10 cấp nhưng `max_point = 9`; Whis chỉ cho nâng tới `point >= 9` là dừng.

### 9.1 Luồng chung (`useNewSkillNotFocus` → `NewSkill.setSkillSpecial` → `SkillService.updateSkillSpecial`)

1. `typeItem = 2` nếu đang dùng **Nồi cơm điện** (`itemTime.isUseNCD`, item 1233), ngược lại 0.
2. Người chơi: `currLevel++` (độ thành thạo, tối đa 1000).
3. Tầm ngang: `dx = dir × (point + 400)`; `_xObjTaget = |dx|` nếu `|dx| < |x_mục_tiêu − x_người|` hoặc `|length| < 100`, ngược lại `= |length|`. Ma phong ba: `_xObjTaget = 75`. `_yObjTaget = |y_mục_tiêu|`.
4. Timer 250 ms gọi `updateSkillSpecial`. Bị huỷ nếu người dùng chết/bị khống chế.

### 9.2 Super Kamejoko (24) & Cađíc liên hoàn chưởng (25)

```
bước 0: sau TIME_GONG = 2000 ms → stepSkillSpecial = 1, gửi hiệu ứng bắn
bước 1: trong 2000 ms tiếp theo, MỖI tick 250 ms:
    với mỗi humanoid (boss dùng → non-boss) ở đúng phía `dir`,
        |x − xPlayer| <= _xObjTaget && |y − yPlayer| <= _yObjTaget && canAttackPlayer
        → playerAttackPlayer (getDameAttack thường)
    với mỗi quái cùng điều kiện (không áp khi người dùng là boss) → playerAttackMob
hết 2000 ms → closeSkillSpecial
```
- Sát thương mỗi hit = `getDameAttack` **không nhân `damage%`** (case không tồn tại trong switch) → ≈ `dame` (+crit, ±5%). Số hit tối đa ≈ 8 (2000/250).

### 9.3 Ma phong ba (26)

```
sau 2000 ms gồng:
    người chơi: 1/50 (2%) chết ngay "kiệt sức vì dùng ma phong ba quá nhiều"
    chọn mọi humanoid trong 500 px (canAttackPlayer) và mọi quái trong 500 px (không áp cho boss)
    startUseMafuba(4000 ms)
khi hết 4000 ms (finishUseMafuba):
    người bị nhốt: setIsBinh(time = 11 000 × (typeItem==0 ? 1 : 2) ms), kéo về cạnh người dùng (±75 px, trừ map NRSD)
    quái: setBinh(11 000 × (typeBinh==0 ? 1 : 2))
trong khi bị nhốt (EffectSkin.updateMaPhongBa, mỗi 500 ms):
    param = point_MaPhongBa × (typeBinh==0 ? 1 : 2)
    subHp = hpMax_người_dùng × param / 100
    nếu subHp >= hp mục tiêu → subHp = |hp − 100|
    target.injured(người dùng, subHp, piercing=true)
```
- Người bị nhốt trong 3000 ms đầu không gây được sát thương (`Player.injured`: `plAtt.isBinh && now − lastTimeUpBinh < 3000 → return 0`).
- Ngoại hình bình: `idOutfitMafuba = {1218,1219,1220}`.

### 9.4 Học/nâng tuyệt kỹ — NPC Whis (map 154) — `npc_list/Whis.java`

| Hành động | Yêu cầu | Tỉ lệ | Tiêu hao |
|---|---|---|---|
| Học mới (point 0/chưa có) | ≥ 9 999 Bí kiếp tuyệt kỹ (item 1229), 10 000 000 vàng, 99 ngọc, SM ≥ 60 tỷ | `isTrue(15,15)` = 100% | 9 999 bí kiếp + 10tr vàng + 99 ngọc |
| Nâng cấp | ≥ 999 bí kiếp, `currLevel >= 1000`, `point < 9` | `isTrue(1,30)` ≈ 3,33% | thành công: 999 bí kiếp; thất bại: 99 bí kiếp; luôn 10tr vàng + 99 ngọc |

---

## 10. Bảng hàm thời gian / phạm vi trong SkillUtil

| Hàm | Công thức | Cấp 1 | Cấp 7 |
|---|---|---|---|
| `getTimeMonkey(l)` | `(l + 5) × 10 000` ms | 60 s | 120 s |
| `getPercentHpMonkey(l)` | `(l + 3) × 10` % | 40% | 100% |
| `getPercentDameMonkey(l)` | `l + 3` % | 4% | 10% |
| `getTimeStun(l)` | `(l + 2) × 1000` ms | 3 s | 9 s |
| `getTimeSocola()` | `30 000` ms | 30 s | 30 s |
| `getTimeShield(l)` | `(l + 2) × 5000` ms | 15 s | 45 s |
| `getTimeTroi(l)` | `l × 5000` ms | 5 s | 35 s |
| `getTimeDCTT(l)` | `(l + 1) × 500` ms | 1 s | 4 s |
| `getTimeThoiMien(l)` | `(l + 4) × 1000` ms | 5 s | 11 s |
| `getRangeStun(l)` | `120 + l × 30` px | 150 | 330 |
| `getRangeBom(l)` | `400 + l × 30` px | 430 | 610 |
| `getRangeQCKK(l)` | `350 + l × 30` px | 380 | 560 |
| `getPercentHPHuytSao(l)` | `(l + 3) × 10` % | 40% | 100% |
| `getPercentTriThuong(l)` | `(l + 9) × 5` % | 50% | 80% |
| `getPercentCharge(l)` | `l + 3` %/tick | 4% | 10% |
| `getTempMobMe(l)` | `{8,11,32,25,43,49,50}[l−1]` | 8 | 50 |
| `getTimeSurviveMobMe(l)` | `getTimeMonkey(l) × 2` | 120 s | 240 s |
| `getHPMobMe(hp, l)` | `hp × {30..90}[l−1] / 100` | 30% | 90% |

`SkillUtil.getTyleSkillAttack`: Trị thương → 2; Kamejoko/Masenko/Antomic → 1; còn lại → 0.
`isUseSkillDam`: Dragon, Demon, Galick, Kaioken, Liên hoàn. `isUseSkillChuong`: Kamejoko, Masenko, Antomic.

`PlayerSkill.getIndexSkillSelect`: đấm (0,2,4,9,17) → 1; chưởng (1,3,5) → 2; tuyệt kỹ (24,25,26) → 4; còn lại → 3.

---

## 11. Hiệu ứng khống chế (EffectSkill) và cách gỡ

`EffectSkill.isHaveEffectSkill() = (isStun || isBlindDCTT || anTroi || isThoiMien || isStone || isMabuHold || isUseSkillMonkey) && !isDie`

| Hiệu ứng | Cờ | Nguồn | Thời gian | Gỡ bởi |
|---|---|---|---|---|
| Choáng (TDHS) | `isStun` | TDHS | `timeStun` | hết giờ / chết |
| Mù DCTT | `isBlindDCTT` | DCTT | `timeBlindDCTT` | hết giờ / chết |
| Bị trói | `anTroi` | Trói | theo người trói | người trói hết giờ/chết/bị khống chế |
| Ngủ | `isThoiMien` | Thôi miên | `timeThoiMien` | hết giờ / chết |
| Hoá đá | `isStone` | option 26 (6 s, mỗi 30 s) | `timeStone` | hết giờ; trong map Mabư khi bị hoá đá mất 50% HP |
| Mabư giữ | `isMabuHold` | Mabư 12h | tới khi `precentMabuHold > 15` hoặc chết | mất 1% hpMax/giây |
| Đang hoá khỉ | `isUseSkillMonkey` | Biến khỉ | 1500 ms | tự hết |
| Sôcôla | `isSocola` | Sôcôla | 30 s | (không tính là khống chế) |
| Làm chậm | `isLamCham` | option 24 | 5 s mỗi 10 s | speed = 1 |
| Tàng hình | `isTanHinh` | option 25 | 1.5 s mỗi 5 s | — |
| Bình Mafuba | `isBinh` | Ma phong ba | 11 s / 22 s | tự hết |
| Chibi | `isChibi` | ngẫu nhiên (xem doc 03) | 600 s | — |

Khi chết `removeSkillEffectWhenDie` gỡ: khỉ, hoá khỉ, bình, khiên, trói, choáng, ngủ, mù, đá, chậm, tàng hình, Mabư giữ, buff SĐ.

---

## 12. Nội tại & set đồ ảnh hưởng tới kỹ năng

### 12.1 Nội tại (xem đầy đủ tại doc 03)

| Loại tác dụng | ID nội tại → skill | Cài đặt |
|---|---|---|
| +% sát thương skill | 1 Dragon, 2 Kamejoko, 8 Demon, 13 Liên hoàn, 16 Galick, 17 Antomic, 18 mọi đòn khi khỉ | `percentDameIntrinsic` |
| Masenko đặc biệt | 9 | cộng vào `pDS` **và** `pXD` **và** giảm hồi chiêu |
| Giảm hồi chiêu | 3 TDHS, 4 QCKK, 5/15/20 Khiên, 10 Trị thương, 11 Makanko, 12 Đẻ trứng, 19 Tự nổ, 21 Huýt sáo | `setLastTimeUseSkill` |
| +% đòn kế | 6 DCTT, 7 Thôi miên, 14 Sôcôla, 22 Trói | `dameAfter` |
| Chí mạng khi HP thấp | 25 | `setIsCrit` |

### 12.2 Set kích hoạt (SetClothes) liên quan skill

| Set (option) | Điều kiện | Hiệu ứng trong code |
|---|---|---|
| Sôngôku (129/141) | 5 món | Kamejoko `pXD=100` |
| Thiên Xin Hăng (127/139) | 5 món | TDHS thời gian ×2 |
| Kirin (128/140) | 5 món | QCKK ×2 |
| Ốc Tiêu (131/143) | 5 món | Liên hoàn `pXD=100` |
| Pikkoro Daimao (132/144) | 5 món | Đẻ trứng: trứng không hết hạn, sát thương ×4 |
| Picolo (130/142) | 5 món | Makankosappo `dameSkill *= 3/2` (thực tế ×1, xem ghi chú) |
| Kakarot (133/136) | 5 món | Galick `pXD=100` |
| Cađíc (134/137) | 5 món | Thời gian khỉ ×5 |
| Nappa (135/138) | 5 món | HP +80% |
| Nail (237–240) | ≥2 món: thêm 10 vào list `tlDameCrit` (không cộng `tlSDCM` → không ảnh hưởng crit); 4 món: Masenko −20% hồi chiêu; 5 món: Masenko `pXD += 80`, −50% hồi chiêu |
| Cađíc M (241–244) | đúng 2: tầm nổ +200, HP +20% (≥2); 4: `pXD=20` + dame nổ +20% hpMax; 5: `pXD=50` + dame nổ +50% hpMax |
| Thần Vũ Trụ Kaio (245–248) | ≥1: crit `+10/100` (=0); 4: Kaioken tốn 5% HP; 5: tốn 3% HP, `pXD=30` |

---

## 13. Học kỹ năng / nâng cấp kỹ năng

### 13.1 Sách kỹ năng (item `type = 7`) — `UseItem.learnSkill`

- Map item → skill: `SkillUtil.getTempSkillSkillByItemID` / `getSkillByItemID`:

| Item id | Skill | Item id | Skill |
|---|---|---|---|
| 66–72 | Dragon | 300–306 | Kaioken |
| 79–84, 86 | Demon | 307–313 | QCKK |
| 87–93 | Galick | 314–320 | Biến khỉ |
| 94–100 | Kamejoko | 321–327 | Tự phát nổ |
| 101–107 | Masenko | 328–334 | Makankosappo |
| 108–114 | Antomic | 335–341 | Đẻ trứng |
| 115–121 | TDHS | 434–440 | Khiên năng lượng |
| 122–128 | Trị thương | 474–480 | Sôcôla |
| 129–135 | Tái tạo NL | 481–487 | Liên hoàn |
| | | 488–494 | DCTT |
| | | 495–501 | Thôi miên |
| | | 502–508 | Trói |
| | | 509–515 | Huýt sáo |

- Điều kiện chung (`UseItem.useItem`): `item.template.strRequire <= power`.
- `item.template.gender == gender` hoặc `== 3`.
- **Cấp sách = ký tự cuối cùng của tên item** (`name.split("")`, `Byte.parseByte(last)`).
- `curSkill.point == 7` → "Kỹ năng đã đạt tối đa".
- `point == 0` và sách cấp 1 → học mới (sub cmd 23). Sách cấp > 1 khi chưa học → báo học cấp trước.
- `point + 1 == level` → nâng (sub cmd 62), thêm id sách vào `BoughtSkill`.

### 13.2 Học qua NPC (Quy Lão Kame / Trưởng lão Guru / Vua Vegeta) — `ShopService.learnKyNang`

1. Chọn sách trong shop `QUY_LAO` (tương tự ở Guru/Vegeta). Kiểm tra `power >= strRequire`, `tiemNang >= is.cost`, cấp đúng `point + 1`, chưa có trong `BoughtSkill`.
2. `potential = (int) powRequire` của cấp đó; thời gian học theo cấp:

| Cấp | 1 | 2 | 3 | 4 | 5 | 6 | 7 |
|---|---|---|---|---|---|---|---|
| Thời gian | 15 phút | 30 phút | 1 giờ | 1 ngày | 3 ngày | 7 ngày | 15 ngày |

3. Xác nhận (menu 671, `NpcFactory`): `LearnSkill.Time = now + time[level−1]`, `tiemNang −= potential`.
4. Khi hết giờ và nói chuyện với NPC → nhận skill. Có thể **học cấp tốc**: `ngoc = 5 + (thời gian còn lại / 600 000)` nếu `≥ 2` khối 10 phút (ví dụ còn 1 giờ → 5 + 6 = 11 ngọc).

### 13.3 Tuyệt kỹ — xem mục 9.4.

---

## 14. Kỹ năng đệ tử

(Chi tiết đầy đủ ở `docs/05-de-tu-pet.md`.)

| Slot | Mở khi | Pool / tỉ lệ (`Pet.java`) |
|---|---|---|
| 1 | Tạo đệ | `createSkill(nextInt(0,2) × 2, 1)` → Dragon / Demon / Galick (1/3 mỗi loại) |
| 2 | SM ≥ 150 000 000 | Kamejoko 33 / Masenko 33 / Antomic 34 (%) — cooldown ép 1000 ms |
| 3 | SM ≥ 1 500 000 000 | TDHS 30 / Tái tạo NL 40 / Kaioken 30 |
| 4 | SM ≥ 20 000 000 000 | Biến khỉ 10 / Đẻ trứng 70 / Khiên 20 |
| 5 | SM ≥ 40 000 000 000 và `typePet ∈ {2,3,4}` | theo **gender đệ**: 0 → Super Kame, 1 → Ma phong ba, 2 → Liên hoàn chưởng |

- Sách nâng chiêu đệ tử: item 402 (chiêu 1), 403 (chiêu 2), 404 (chiêu 3), 759 (chiêu 4) → `SkillUtil.upSkillPet` (+1 cấp, tối đa 7).
- Đổi chiêu ngẫu nhiên: item 1758 (chiêu 2), 1759 (chiêu 3), 1760 (chiêu 4).

---

## 15. Ghi chú / điểm cần lưu ý

1. **Tuyệt kỹ không dùng `damage%`**: `getDameAttack` không có case cho SUPER_KAME/LIEN_HOAN_CHUONG/MA_PHONG_BA → `percentDameSkill = 0` → sát thương ≈ `dame`. Cột damage 550–1000 vô tác dụng (Ma phong ba dùng `point` làm % HP).
2. **Nâng tuyệt kỹ ở Whis chỉ tăng `point++`** (`Whis.upgradeSkill`) mà không thay object `Skill` bằng dữ liệu cấp mới → cooldown/mana/dx/dy/skillId vẫn là cấp cũ tới khi relog (khi load lại `createSkill(tempId, point)` mới cập nhật). `currLevel` cũng không reset.
3. **Picolo set 5**: `dameSkill *= 3 / 2;` — phép chia số nguyên `3/2 = 1` → không tăng.
4. **Thần Vũ Trụ Kaio**: `crit += 10 / 100` = 0.
5. **QCKK** bỏ qua cột `damage`; sát thương phụ thuộc HP quái/người xung quanh → có thể rất lớn ở map đông quái.
6. **Makankosappo** dùng `mpMax` chứ không dùng KI hiện tại, nhưng tiêu hết KI hiện tại.
7. **Tự phát nổ**: biến `dame` bị gán đè khi gặp boss (`dame = dame/2`) → các mục tiêu sau trong vòng lặp nhận sát thương đã bị chia (cộng dồn nếu gặp nhiều boss).
8. **Huýt sáo** tác dụng lên **toàn zone** không giới hạn khoảng cách, và gỡ trói của **mọi người** trong zone; "+1 đòn chí mạng" không có.
9. **Trị thương** cấp > 1: điều kiện thêm người xung quanh kiểm tra `canHsPlayer(player, plTarget)` (mục tiêu chính) thay vì từng `pl` → người khác cờ/PK trong 300px vẫn được hồi. Người dùng cũng tự hồi mỗi lần lặp.
10. **Sôcôla**: thời gian cố định 30 s, không theo cấp.
11. **Nội tại 9 (Masenko)** được tính 3 lần: cộng vào `percentDameSkill`, nhân `percentXDame` sau crit, và giảm hồi chiêu.
12. **Nội tại 3 (TDHS)**: `param2` "-KI" không có code sử dụng.
13. **Khiên** không chặn sát thương quái (`isMobAttack`) và đòn piercing.
14. **Đệ tử + bùa Đệ tử đánh quái**: ×2 trong `getDameAttack(true)` và ×2 lần nữa trong `playerAttackMob` → thực tế ×4.
15. **PK_PVP_2** (`typePk == 4`): TNSM tính từ người đánh nhưng cộng cho **người bị đánh** (`addSMTN(plInjure, ...)`).
16. `learnSkill` lấy cấp sách bằng **ký tự cuối tên item** → tên item phải kết thúc bằng chữ số 1–7; tên sai sẽ ném `NumberFormatException` (bị catch, log).
17. `QuyLaoKame.handleTalk` khi hết thời gian học: `createSkill(..., getSkillByItemID(...).point)` dùng **cấp hiện tại** thay vì cấp mới → học xong không tăng cấp (chỉ nhánh "học cấp tốc" `learnSkill` dùng đúng `level`). Menu 13 "Huỷ và nhận lại 50% tiềm năng" không có handler.
18. `ShopService.learnKyNang`: `potential = (int) powRequire` → skill có `power_require` > 2 147 483 647 (Khiên/Liên hoàn/DCTT/Thôi miên/Sôcôla/Trói/Huýt sáo cấp 6–7: 3,125 tỷ và 15,625 tỷ) bị **tràn int âm** → `tiemNang −= số âm` = **cộng tiềm năng**. Đồng thời kiểm tra đủ tiềm năng dùng `is.cost` khác với số bị trừ.
19. Ma phong ba: 2% chết khi dùng; `subHp = |hp − 100|` khi hp < subHp có thể lớn hơn hp nếu hp < 50 (vd hp = 10 → subHp = 90).
20. Tuyệt kỹ: `_yObjTaget = |y mục tiêu|` là **toạ độ tuyệt đối** chứ không phải khoảng cách → vùng dọc rất lớn.
21. `NewSkill.update()`: `if (this.isStartSkillSpecial = true)` là phép **gán**, luôn true → timer luôn gọi `updateSkillSpecial` (được `closeSkillSpecial` huỷ timer nên ít ảnh hưởng).
22. Vô hiệu chưởng (option 3): chỉ cần `voHieuChuong > 0` là **miễn 100%** sát thương chưởng, param chỉ quyết định % hồi KI.
