# 03 — Nhân vật & chỉ số

> Đặc tả nhân vật (Player) của server Ngọc Rồng Online (Teamobi2026): hành tinh, tạo nhân vật, chỉ số gốc, cộng tiềm năng, công thức `calPoint`, sát thương nhận vào, hồi phục, TNSM, cấp bậc, chết/hồi sinh, PK/cờ, bùa, EffectSkin, danh hiệu, nội tại.
> Nguồn chính: `SRC/src/nro/models/player/NPoint.java`, `player/Player.java`, `player/SetClothes.java`, `player/Charms.java`, `player/EffectSkin.java`, `player/EffectSkill.java`, `player/RewardBlackBall.java`, `player/Fusion.java`, `player_badges/*`, `player_system/*`, `intrinsic/*`, `services/IntrinsicService.java`, `services/Service.java`, `services/PlayerService.java`, `database/PlayerDAO.java`, `mob/Mob.java`, `item/ItemTime.java`, `services_func/UseItem.java`; DB `database team2026.sql` (bảng `intrinsic`, `data_badges`, `task_badges_template`, `flag_bag`, `item_option_template`).

## Mục lục

1. [Hành tinh (gender)](#1-hành-tinh-gender)
2. [Tạo nhân vật & chỉ số khởi tạo](#2-tạo-nhân-vật--chỉ-số-khởi-tạo)
3. [Các trường chỉ số trong NPoint](#3-các-trường-chỉ-số-trong-npoint)
4. [Giới hạn sức mạnh & cộng tiềm năng](#4-giới-hạn-sức-mạnh--cộng-tiềm-năng)
5. [calPoint — tính lại toàn bộ chỉ số](#5-calpoint--tính-lại-toàn-bộ-chỉ-số)
6. [Bảng option item → chỉ số (addOption)](#6-bảng-option-item--chỉ-số-addoption)
7. [Công thức HP tối đa / KI tối đa / Sức đánh / Giáp / Chí mạng](#7-công-thức-hp-tối-đa--ki-tối-đa--sức-đánh--giáp--chí-mạng)
8. [Set kích hoạt, cải trang, hợp thể, biến hình, buff item](#8-set-kích-hoạt-cải-trang-hợp-thể-biến-hình-buff-item)
9. [Sát thương gây ra (tóm tắt) & sát thương nhận vào (injured)](#9-sát-thương-gây-ra-tóm-tắt--sát-thương-nhận-vào-injured)
10. [Hồi HP/KI, thể lực, bay](#10-hồi-hpki-thể-lực-bay)
11. [Tiềm năng / sức mạnh nhận được (TNSM)](#11-tiềm-năng--sức-mạnh-nhận-được-tnsm)
12. [Cấp bậc sức mạnh (tên cấp)](#12-cấp-bậc-sức-mạnh-tên-cấp)
13. [Chết & hồi sinh](#13-chết--hồi-sinh)
14. [PK, cờ (cFlag) và túi/flag_bag](#14-pk-cờ-cflag-và-túiflag_bag)
15. [Bùa (Charms)](#15-bùa-charms)
16. [EffectSkin (hiệu ứng từ trang bị)](#16-effectskin-hiệu-ứng-từ-trang-bị)
17. [Chibi (hiệu ứng ngẫu nhiên)](#17-chibi-hiệu-ứng-ngẫu-nhiên)
18. [Ngọc rồng sao đen (RewardBlackBall)](#18-ngọc-rồng-sao-đen-rewardblackball)
19. [Danh hiệu (player_badges)](#19-danh-hiệu-player_badges)
20. [player_system](#20-player_system)
21. [Nội tại (intrinsic)](#21-nội-tại-intrinsic)
22. [Ghi chú / điểm cần lưu ý](#22-ghi-chú--điểm-cần-lưu-ý)

---

## 1. Hành tinh (gender)

`consts/ConstPlayer.java`:

| gender | Hằng | Tên (`Service.get_HanhTinh`) | Map nhà (`21 + gender`) | Map tạo NV (`39 + gender`) |
|---|---|---|---|---|
| 0 | `TRAI_DAT` | Trái Đất | 21 | 39 |
| 1 | `NAMEC` | Namếc | 22 | 40 |
| 2 | `XAYDA` | Xayda | 23 | 41 |

Khác biệt theo hành tinh trong code:
- Chỉ số gốc khởi tạo (mục 2).
- Bộ kỹ năng (doc 04).
- Danh sách nội tại (mục 21).
- Huýt sáo: người Namếc cùng cờ **bị trừ 10% HP** thay vì được buff.
- Ngoại hình mặc định khi không mặc áo/quần: body `59` (Namếc) / `57`; leg `60` (Namếc, check `gender == 1`) / `58` (`Player.getBody/getLeg`).

---

## 2. Tạo nhân vật & chỉ số khởi tạo

`server/Controller.createChar` → `database/PlayerDAO.createNewPlayer(userId, name, gender, hair)`.

**Điều kiện tên**: độ dài 5–10, không ký tự đặc biệt (`Util.haveSpecialCharacter`), không trùng DB, không nằm trong `ConstIgnoreName.IGNORE_NAME`; tên lưu `toLowerCase()`.

| Trường | Trái Đất | Namếc | Xayda |
|---|---|---|---|
| Vàng | 2 000 | 2 000 | 2 000 |
| Ngọc xanh | 10 000 (Manager.TEST: 1 000 000 000) | = | = |
| Hồng ngọc | 0 | 0 | 0 |
| Giới hạn SM (`limitPower`) | 0 | 0 | 0 |
| Sức mạnh | 2 000 | 2 000 | 2 000 |
| Tiềm năng | 2 000 | 2 000 | 2 000 |
| Thể lực / tối đa | 1 000 / 1 000 | = | = |
| HP gốc (`hpg`) | **200** | 100 | 100 |
| KI gốc (`mpg`) | 100 | **200** | 100 |
| Sức đánh gốc (`dameg`) | 10 | 10 | **15** |
| Giáp gốc / Chí mạng gốc | 0 / 0 | = | = |
| Áo (slot 0) | item 0, option 47 (Giáp +2) | item 1, Giáp +2 | item 2, Giáp **+3** |
| Quần (slot 1) | item 6, option 6 (HP **+30**) | item 7, HP +20 | item 8, HP +20 |
| Túi đồ ô 0 | item 63 ×10, option 2 param 8 | = | = |
| Rương ô 0 | item 12 (rada), option 14 (Chí mạng +1%) | = | = |
| Vị trí | map 39+gender, x=100, y=384 | | |
| Kỹ năng | xem doc 04 (cấp 1 skill đấm) | | |
| Cây đậu | cấp 1, 5 hạt | | |
| Nhiệm vụ | id 0 (TEST: 28) | | |
| Nội tại | id 0 (chưa kích hoạt) | | |

`itemsBody` được tạo 11 ô. Ý nghĩa slot (theo `InventoryService.putItemBody` / `Player`):

| Slot | Nội dung |
|---|---|
| 0–4 | Áo, quần, găng, giày, rada (type 0–4) |
| 5 | Cải trang (type 5) |
| 6 | Giáp tập luyện (type 32) |
| 7 | Pet đi theo/linh thú (type 27) |
| 8 | Túi/cờ (flag bag, type 11) |
| 9 | Thú cưỡi (type 23/24) |
| 10 | Vật phẩm tuyệt kỹ (type 25) — đệ tử dùng slot 8 |

---

## 3. Các trường chỉ số trong NPoint

| Nhóm | Trường | Ý nghĩa |
|---|---|---|
| Gốc (cộng tiềm năng) | `hpg, mpg, dameg, defg, critg` | HP/KI/SĐ/giáp/chí mạng gốc |
| | `critdragon` | chí mạng từ rồng (cộng thẳng) |
| Hiện hành | `hp, hpMax, mp, mpMax, dame, def, crit, speed` | |
| Tiến trình | `power, tiemNang, limitPower, stamina, maxStamina` | |
| Cộng phẳng | `hpAdd, mpAdd, dameAdd, defAdd, critAdd, hpHoiAdd, mpHoiAdd` | |
| Cộng % (list, nhân dồn) | `tlHp, tlMp, tlDame, tlDameAttMob, tlTNSM, tlDameCrit` | |
| Cộng % (số) | `tlSDCM, tlHpHoi, tlMpHoi, tlHpHoiBanThanVaDongDoi, tlMpHoiBanThanVaDongDoi, tlHutHp, tlHutMp, tlHutHpMob, tlHutHpMpXQ, tlPST, tlGold, tlNeDon, tlBom, tlGiap, tlxgc, tlxgcc, tlchinhxac, tlTNSMPet, tlSexyDame, tlSubSD, tlHpGiamODo, tlSpeed` | |
| Cờ trạng thái | `teleport, khangTDHS, wearingVoHinh, isKhongLanh, isTanHinh, isHoaDa, isLamCham, isDoSPL, isThoBulma, isGogeta, islinhthuydanhbac, isTinhAn/NhatAn/NguyetAn` | |
| Khác | `voHieuChuong, xChuong, levelBT, dameAfter, isCrit, isCrit100, isCritTele` | |

---

## 4. Giới hạn sức mạnh & cộng tiềm năng

### 4.1 Bảng giới hạn theo `limitPower` (`NPoint.getPowerLimit`, `getHpMpLimit`, `getDameLimit`, `getDefLimit`, `getCritLimit`)

`MAX_LIMIT = 9`.

| limitPower | SM tối đa | HP/KI gốc tối đa | SĐ gốc tối đa | Giáp gốc tối đa | CM gốc tối đa |
|---|---|---|---|---|---|
| 0 | 17 999 999 999 | 220 000 | 11 000 | 550 | 1 |
| 1 | 19 999 999 999 | 240 000 | 12 000 | 600 | 2 |
| 2 | 24 999 999 999 | 300 000 | 15 000 | 700 | 3 |
| 3 | 29 999 999 999 | 350 000 | 18 000 | 800 | 4 |
| 4 | 39 999 999 999 | 400 000 | 20 000 | 1 000 | 5 |
| 5 | 50 010 000 000 | 450 000 | 22 000 | 1 200 | 6 |
| 6 | 60 010 000 000 | 500 000 | 24 000 | 1 400 | 7 |
| 7 | 70 010 000 000 | 525 000 | 24 500 | 1 500 | 8 |
| 8 | 80 010 000 000 | 550 000 | 25 000 | 1 600 | 9 |
| 9 | 90 010 000 000 | 575 000 | 26 000 | 1 800 | 10 |

Khi `power >= getPowerLimit()` → không nhận thêm SM (`Service.addSMTN` return) và `calSucManhTiemNang` trả về cố định 10.

### 4.2 Mở giới hạn

| Cách | Nguồn | Điều kiện | Chi phí | Kết quả |
|---|---|---|---|---|
| Thường | `OpenPowerService.openPowerBasic` | `limitPower < 9`, `power >= getPowerLimit()`, chưa đang mở | — | Bật `itemTime.isOpenPower`; sau `TIME_OPEN_POWER = 8 640 000 ms` (2,4 giờ) → `limitPower++` |
| Nhanh | `openPowerSpeed` | `limitPower < 9` | `COST_SPEED_OPEN_LIMIT_POWER = 50 000 000` vàng | `limitPower++` ngay (**không kiểm tra SM**) |
| NPC Quốc Vương (`npc_list/QuocVuong`) | | `limitPower < 5` (`MAX_LIMIT_CUSTOM`) | như trên | cho bản thân / đệ tử |
| NPC Tổ sư Kaio (`npc_list/ToSuKaio`) | | `5 <= limitPower < 9` | như trên | cho bản thân / đệ tử |

### 4.3 Cộng tiềm năng — `NPoint.increasePoint(type, point)`

Gói client cmd 16 (`Controller`): `type` (byte), `point` (short). Chặn nếu `point <= 0 || point > 1000`.

| type | Chỉ số | Tăng | Chi phí tiềm năng (code) | Dạng tổng | Giới hạn |
|---|---|---|---|---|---|
| 0 | HP gốc | `+20 × point` | `point × (2×(hpg+1000) + 20×point − 20) / 2` | `Σ_{i=0}^{point−1} (hpg + 1000 + 20i)` | `hpg + 20×point <= getHpMpLimit()` |
| 1 | KI gốc | `+20 × point` | `point × (2×(mpg+1000) + 20×point − 20) / 2` | tương tự | `mpg + 20×point <= getHpMpLimit()` |
| 2 | Sức đánh gốc | `+point` | `point × (2×dameg + point − 1) / 2 × 100` | `100 × Σ_{i=0}^{point−1}(dameg + i)` | `dameg + point <= getDameLimit()` |
| 3 | Giáp gốc | `+point` | `2 × (defg + 5) / 2 × 100 000` = `(defg + 5) × 100 000` | **không nhân point** | `defg + point <= getDefLimit()` |
| 4 | Chí mạng gốc | `+point` | `50 000 000 × 5^critg` | không nhân point | `critg + point <= getCritLimit()` |

Ví dụ: `hpg = 200`, cộng 1 lần (point 1) → tốn `1200` TN; `dameg = 10` +1 → `1000` TN; `defg = 0` → `500 000` TN; `critg = 0` → `50 000 000` TN, `critg = 3` → `6 250 000 000` TN.

Vượt giới hạn → "Vui lòng mở giới hạn sức mạnh". Thiếu TN → "Bạn không đủ tiềm năng" (`doUseTiemNang`).

### 4.4 `getFullTN()` (hiển thị ở thách đấu `PVPService`)

```
tnhp = (hpg/20) × (50 + 50 + hpg/20 − 1) / 2 × 20
tnki = (mpg/20) × (100 + mpg/20 − 1) / 2 × 20
tnsd = dameg × (dameg − 1) × 100 / 2
tng  = defg × (500000 + 500000 + (defg − 1) × 100000) / 2
tncm = 50 × (5^critg − 1) / 4 × 1 000 000
FullTN = tnhp + tnki + tnsd + tng + tncm
```
(Công thức này **không khớp** chi phí thực tế ở 4.3 — chỉ là ước lượng.)

---

## 5. calPoint — tính lại toàn bộ chỉ số

`NPoint.calPoint()`:
```
if player.pet != null: pet.nPoint.setPointWhenWearClothes()
this.setPointWhenWearClothes()
```
Được gọi qua `Service.point(player)` (gọi rất nhiều nơi: mặc/tháo đồ, dùng item, hết buff...). `SetClothes.setup()` (đếm set) chỉ gọi khi đổi trang bị (`UseItem` sau thao tác body/bag, SuperRankService).

`setPointWhenWearClothes()` — thứ tự:

1. `resetPoint()` — xoá mọi cộng thêm/cờ (không reset `critdragon`, `levelBT`, `tlNeDonBuffXinbato`).
2. **Ngọc rồng sao đen** còn hạn: 3 sao `tlHutHp += 35`; 4 sao `tlPST += 35`; 5 sao `tlDameCrit.add(35)`, `tlSDCM += 35`; 7 sao `tlNeDon += 14` (các sao 1, 2, 6 xử lý trong setDame/setHpMax/setMpMax).
3. **Danh hiệu** option 108: `tlNeDon += tlNeDon × param / 100`.
4. **Thẻ rada** (`player.Cards` có `Used == 1`): với mỗi `OptionCard` có `active == card.Level` (hoặc `Level == −1 && active == 0`) → áp giống bảng addOption (mục 6, thiếu các option 24/25/26/110/159/160).
5. **Bông tai Porata cấp 2** (`typeFusion == 8`): lấy item **921 trong túi**, `addOption` mọi option; option 72 → `levelBT`.
6. **Bông tai Porata cấp 3** (`typeFusion == 9`): item **1819 trong túi**, tương tự.
7. Đếm `setClothes.worldcup` = số item body thuộc {966, 982, 983, 883, 904}; item body 592–594 → `teleport = true`; `addOption` cho **mọi item trong `itemsBody`** (gồm cải trang, giáp tập luyện, pet item, cờ, thú cưỡi…).
8. `setDameTrainArmor()` (mục 8.6).
9. `setBasePoint()`: `setHpMax → setHp → setMpMax → setMp → setDame → setDef → setCrit → setHpHoi → setMpHoi → setLtdb → setThoBulma → setTinhNhatNguyetAn`.
10. `setOutfitFusion()` (Gogeta), `setSpeed()`.

> Lưu ý thứ tự: `setTinhNhatNguyetAn()` chạy **sau** setHpMax/setMpMax/setDame, nên set Tinh/Nhật/Nguyệt Ấn dùng giá trị cờ của lần `calPoint` **trước** (trễ 1 lần tính).

---

## 6. Bảng option item → chỉ số (addOption)

Tên option lấy từ bảng `item_option_template`.

| Option | Tên (DB) | Tác dụng trong `NPoint.addOption` |
|---|---|---|
| 0 | Tấn công+# | `dameAdd += p` |
| 2 | HP, KI+#000 | `hpAdd += p×1000; mpAdd += p×1000` |
| 3 | Vô hiệu và biến #% sát thương chưởng thành KI | `voHieuChuong += p` |
| 5 | +#% sức đánh chí mạng | `tlDameCrit.add(p); tlSDCM += p` |
| 6 | HP+# | `hpAdd += p` |
| 7 | KI+# | `mpAdd += p` |
| 8 | Hút #% HP, KI xung quanh mỗi 5 giây | `tlHutHpMpXQ += p` |
| 14 | Chí mạng+#% | `critAdd += p` |
| 16, 114, 148 | Tốc độ | `tlSpeed += p` |
| 18 | Chính xác +#% | `tlchinhxac += p` |
| 19 | Tấn công+#% khi đánh quái | `tlDameAttMob.add(p)` |
| 22 | HP+#K | `hpAdd += p×1000` |
| 23 | KI+#K | `mpAdd += p×1000` |
| 24 | Làm chậm xung quanh | `isLamCham = true` |
| 25 | Tàng hình mỗi 5 giây | `isTanHinh = true` |
| 26 | Hoá đá xung quanh mỗi 30 giây | `isHoaDa = true` |
| 27 | +# HP/30s | `hpHoiAdd += p` |
| 28 | +# KI/30s | `mpHoiAdd += p` |
| 33 | Dịch chuyển tức thời | `teleport = true` |
| 34 / 35 / 36 | Tinh ấn / Nguyệt ấn / Nhật ấn | `setTinhAn++ / setNguyetAn++ / setNhatAn++` |
| 47 | Giáp+# | `defAdd += p` |
| 48 | HP, KI+# | `hpAdd += p; mpAdd += p` |
| 49, 50 | Tấn công+#% / Sức đánh+#% | `tlDame.add(p)` |
| 77 | HP+#% | `tlHp.add(p)` |
| 80 | HP+#%/30s | `tlHpHoi += p` |
| 81 | KI+#%/30s | `tlMpHoi += p` |
| 88 | Cộng #% TN, SM khi đánh quái | `tlTNSM.add(p)` |
| 94 | Giảm #% sát thương | `tlGiap += p` |
| 95 | Biến #% tấn công thành HP | `tlHutHp += p` |
| 96 | Biến #% tấn công thành KI | `tlHutMp += p` |
| 97 | Phản #% sát thương | `tlPST += p` |
| 98 | Xuyên giáp #% chưởng | `tlxgc += p` |
| 99 | Xuyên giáp #% cận chiến | `tlxgcc += p` |
| 100 | +#% vàng từ quái | `tlGold += p` |
| 101 | +#% tiềm năng, sức mạnh | `tlTNSM.add(p)` |
| 103 | KI +#% | `tlMp.add(p)` |
| 104 | Biến #% tấn công quái thành HP | `tlHutHpMob += p` |
| 105 | Vô hình khi không đánh quái và boss | `wearingVoHinh = true` |
| 106 | Không ảnh hưởng bởi cái lạnh | `isKhongLanh = true` |
| 108, 111 | #% Né đòn / Phân tâm | `tlNeDon += p` |
| 109 | Hôi, giảm #% HP | `tlHpGiamODo += p` |
| 110 | Dò pha lê | `isDoSPL = true` |
| 116 | Kháng TDHS | `khangTDHS = true` |
| 117, 226 | Đẹp/Cute +#% SĐ cho mình và xung quanh | `tlSexyDame = max(tlSexyDame, p)` |
| 147 | +#% sức đánh | `tlDame.add(p)` |
| 153 | #% tỉ lệ phát nổ sau khi chết | `tlBom += p` |
| 156 | (DB: "Cộng dồn 1% sức đánh khi chỉ dùng chiêu đấm, tối đa #%") | code: `tlSubSD += 50; tlTNSM.add(p); tlGold += p` |
| 159 | x# sức đánh đòn chưởng cơ bản mỗi phút | `xChuong = p` (gán, không cộng) |
| 160 | +#% TN, SM cho đệ tử khi sư phụ mặc | `tlTNSMPet += p` |
| 162 | Cute hồi #% KI/s bản thân và xung quanh | `mpHoiCute += p` |
| 173 | Phục hồi #% HP và KI cho đồng đội | `tlHpHoiBanThanVaDongDoi += p; tlMpHoiBanThanVaDongDoi += p` |
| 211 | Giám định #/5 | `setltdb++` (≥ 5 món → `islinhthuydanhbac`) |

Option đếm set (129/141, 127/139, …, 237–248, 250, 252/253/255) do `SetClothes.setupSKT` xử lý (mục 8.1).

---

## 7. Công thức HP tối đa / KI tối đa / Sức đánh / Giáp / Chí mạng

Ký hiệu: `x += x × a%` nghĩa là `x = x + x × a / 100` (chia nguyên `long`). Các bước **nhân dồn** theo thứ tự dưới đây.

### 7.1 HP tối đa — `setHpMax()`

```
hpMax = hpg + hpAdd
for tl in tlHp:                         hpMax += hpMax × tl%
if set Nappa == 5:                      hpMax += 80%
if set Cađíc M >= 2:                    hpMax += 20%
if worldcup == 2:                       hpMax += 10%
if itemTime.isUseRX (rồng xương):       hpMax += 10%
if isNhatAn (5 món Nhật ấn):            hpMax += 15%
if NRSĐ 2 sao còn hạn:                  hpMax += 35%   (R2S_1)
if đang khỉ (và không phải đệ đang hợp thể): hpMax += (levelMonkey+3)×10 %
if là đệ tử typePet 1 (Mabư) & sư phụ Porata 1/2/3: += 0%
if là đệ tử typePet 2/3/4 & sư phụ Porata 1/2/3:    += 20%
if map NRSĐ (BlackBallWar):             hpMax ×= effectSkin.xHPKI
if thức ăn 2 icon 8062:                 hpMax += 5%
if isGogeta:                            hpMax += 10%
if isPhuHoMapMabu:                      hpMax += 1 000 000
if typeFusion != 0 (hợp thể):           hpMax += pet.nPoint.hpMax
if Bổ huyết (382) && !Bổ huyết 2:       hpMax ×= 2
if Nước mía 3 (1616):                   hpMax += hpMax/10
if Nước mía 2 (1615):                   hpMax += hpMax/10
if Nước mía 1 (1614):                   hpMax += hpMax/10
if Bổ huyết 2 (1152):                   hpMax ×= 2.2
if Huýt sáo (tiLeHPHuytSao != 0, không phải đệ đang hợp thể): hpMax += tiLeHPHuytSao%
if Chibi typeChibi == 3:                hpMax ×= 2
if map lạnh && !isKhongLanh:            hpMax /= 2
for option 77 của danh hiệu đang dùng:  hpMax += p%
hpMax = min(hpMax, 2 147 483 647)
```
`setHp()`: `hp = min(hp, hpMax)`.

### 7.2 KI tối đa — `setMpMax()`

```
mpMax = mpg + mpAdd
for tl in tlMp:                          mpMax += tl%
if isNguyetAn:                           mpMax += 15%
if NRSĐ 6 sao:                           mpMax += 40%   (R6S_1)
if worldcup == 2:                        mpMax += this.mpMax(cũ) × 10%     ← dùng giá trị lần tính trước
    (+ đệ Mabư Porata: += 0)
if đệ typePet 2/3/4 & sư phụ Porata:     mpMax += this.mpMax(cũ) × 20%     ← cũng dùng giá trị cũ
if map NRSĐ:                             mpMax ×= xHPKI
if isGogeta:                             mpMax += 10%
if isPhuHoMapMabu:                       mpMax += 1 000 000
if rồng xương:                           mpMax += 10%
if typeFusion != 0:                      mpMax += pet.nPoint.mpMax
if Bổ khí (383) && !Bổ khí 2:            mpMax ×= 2
if Bổ khí 2 (1151):                      mpMax ×= 2.2
for option 103 danh hiệu:                mpMax += p%
cap INT_MAX
```
(Không có khỉ, huýt sáo, map lạnh, nước mía cho KI.)

### 7.3 Sức đánh — `setDame()`

```
dame = dameg + dameAdd
for tl in tlDame:                        dame += tl%
if đệ typePet 3 & sư phụ Porata:         dame += 20%      ("pet pic")
if đệ typePet 1 & Porata:                dame += 0%
if đệ typePet 2 & Porata:                dame += 20%
if đệ typePet 3 & Porata:                dame += 20%      ("pet beer" — lần 2)
if đệ typePet 4 & Porata:                dame += 20%
if isTinhAn:                             dame += 15%
if thức ăn (663–667) của bản thân (đệ: của sư phụ): dame += 10%
if thức ăn 2 icon 8060:                  dame += 5%
if set Nail >= 2:                        tlDameCrit.add(10)        (không cộng tlSDCM)
if thức ăn 2 icon 8061:                  tlDameCrit.add(5); tlSDCM += 5
if Nước mía 3:                           tlDameCrit.add(10); tlSDCM += 10
if Cuồng nộ (381) && !Cuồng nộ 2:        dame ×= 2
if Nước mía 3:                           dame += dame/10
if Cuồng nộ 2 (1150):                    dame ×= 2.2
if NRSĐ 1 sao:                           dame += 21%    (R1S_2)
if worldcup == 2:                        dame += 10%
if isGogeta:                             dame += 10%
if isPhuHoMapMabu:                       dame += 10 000
if rồng xương:                           dame += 10%
if map NRSĐ:                             dame ×= effectSkin.xDame
if typeFusion != 0:                      dame += pet.nPoint.dame
for option 50 danh hiệu:                 dame += p%
if đang khỉ (không phải đệ hợp thể):     dame += (levelMonkey + 3)%
for option 117 danh hiệu:                tlSexyDame += p
dame += tlSexyDame%
dame += tlSexyDame%                      ← áp 2 lần
dame −= dame × tlSubSD%
if map lạnh && !isKhongLanh:             dame /= 2
cap INT_MAX
```

### 7.4 Giáp — `setDef()`

```
def = defg × 4 + defAdd
if Nước mía 3: def += def × 10 / 100
```
(`tlDef` list tồn tại nhưng không bao giờ được thêm/đọc.)

### 7.5 Chí mạng — `setCrit()`

```
crit = critg + critAdd + critdragon
if đang khỉ:                    crit = 110           (ghi đè)
if Nước mía 2:                  crit += 10
if set Thần Vũ Trụ Kaio >= 1:   crit += 10/100 (= 0)
```
Xác suất crit mỗi đòn: `Util.isTrue(crit, 100)` → `random[0,99] < crit`.

### 7.6 Tốc độ — `setSpeed()` (chỉ người chơi thật)

`speed = 8 + 8 × (tlSpeed / 100)` (chia nguyên → cần ≥ 100% mới tăng). Làm chậm: `speed = 1` trong 5 s.

### 7.7 Hồi HP/KI mỗi 30 giây — `setHpHoi()/setMpHoi()`

```
hpHoi = hpMax/100 + hpHoiAdd + hpMax × clamp(tlHpHoi,0,100)% + hpMax × clamp(tlHpHoiBanThanVaDongDoi,0,100)%
mpHoi = mpMax/100 + mpHoiAdd + mpMax × clamp(tlMpHoi,0,100)% + mpMax × clamp(tlMpHoiBanThanVaDongDoi,0,100)%
```

### 7.8 Ví dụ nhanh

Trái Đất mới tạo: `hpg = 200`, quần +30 HP → `hpMax = 230`; `mpg = 100`; `dameg = 10`; áo Giáp +2 → `def = 0×4 + 2 = 2`; `crit = 0`.

---

## 8. Set kích hoạt, cải trang, hợp thể, biến hình, buff item

### 8.1 SetClothes (`player/SetClothes.java`)

`setupSKT()` duyệt 5 món body (slot 0–4); **mỗi món chỉ tính option set đầu tiên** gặp được (`break` khi `isActSet`).

| Biến | Option | Tác dụng (nơi dùng) |
|---|---|---|
| `songoku` | 129, 141 | 5: Kamejoko ×2 (`getDameAttack`) |
| `thienXinHang` | 127, 139 | 5: thời gian TDHS ×2 |
| `kirin` | 128, 140 | 5: QCKK ×2 |
| `ocTieu` | 131, 143 | 5: Liên hoàn ×2 |
| `pikkoroDaimao` | 132, 144 | 5: Đẻ trứng ×4, trứng bất tử |
| `picolo` | 130, 142 | 5: Makankosappo `×3/2` (=×1, bug) |
| `nappa` | 135, 138 | 5: HP +80% |
| `kakarot` | 133, 136 | 5: Galick ×2 |
| `cadic` | 134, 137 | 5: thời gian khỉ ×5 |
| `kaioken` | 253 | chỉ dùng cho hiệu ứng hào quang |
| `lienHoan` | 250 | không có tác dụng chỉ số (option 250 là "Kilis") |
| `giamSatThuong` | 252, 255 | không có code sử dụng |
| `setDHD` | 21 với param 80 | không có code sử dụng |
| `thanVuTruKaio` | 245–248 | ≥1: crit +0 (bug); 4: Kaioken tốn 5% HP; 5: 3% HP + Kaioken ×1.3 |
| `nail` | 237–240 | ≥2: `tlDameCrit.add(10)`; 4: Masenko −20% hồi; 5: Masenko +80% & −50% hồi |
| `cadicM` | 241–244 | ≥2: HP +20%; ==2: tầm nổ +200; 4: nổ +20% hpMax & ×1.2; 5: +50% hpMax & ×1.5 |
| `worldcup` | item body 966/982/983/883/904 (đếm trong NPoint) | == 2: HP +10%, KI +10% (giá trị cũ), SĐ +10% |
| `godClothes` | 5 món id 555–567 | chỉ cờ |
| `ctHaiTac` | cải trang 618–624, 626, 627 | Hải tặc (mục 16) |

Set Tinh/Nhật/Nguyệt Ấn (option 34/36/35, đếm trên **mọi item body**): ≥ 5 → Tinh ấn SĐ +15%, Nhật ấn HP +15%, Nguyệt ấn KI +15%.
Option 211 ≥ 5 món → `islinhthuydanhbac` (miễn nhiễm "HAKAI" của đệ tử, doc 05).

Hào quang set (`Service.sendEffPlayer`): songoku/kaioken/kirin/thienXinHang ≥5 → eff 1200; ocTieu/pikkoroDaimao/picolo ≥5 → 10277; thanVuTruKaio ≥5 → 10277 + 5017; kakarot/nappa/cadic ≥5 → 1202.

### 8.2 Cải trang (slot 5)

- Toàn bộ option của cải trang được cộng qua `addOption`.
- Ngoại hình: `template.head/body/leg` nếu khác −1.
- Item 584 (Thỏ Bulma) → `isThoBulma` (chỉ đổi câu chat).
- **Gogeta**: master slot 5 = 2133 và đệ slot 5 = 2134 (hoặc ngược lại) → `isGogeta`: HP/KI/SĐ +10%, head 2100/body 2101/leg 2102 khi hợp thể.
- Cải trang 1693/1553 (khác nhau giữa master và đệ) khi hợp thể → head 1578/body 1581/leg 1582; `fusionGogeta` còn gửi hiệu ứng nổ.

### 8.3 Hợp thể (Fusion)

`ConstPlayer`: `NON_FUSION = 0`, `LUONG_LONG_NHAT_THE = 4`, `HOP_THE_PORATA = 6`, `HOP_THE_PORATA2 = 8`, `HOP_THE_PORATA3 = 9`, `HOP_THE_GOGETA = 10`.

| Kiểu | Kích hoạt | Thời gian | Cộng chỉ số cho sư phụ |
|---|---|---|---|
| Lưỡng Long Nhất Thể (4) | đệ `changeStatus(FUSION)` → `fusion(false)` | `Fusion.TIME_FUSION = 600 000` ms rồi tự tách | `hpMax += pet.hpMax`, `mpMax += pet.mpMax`, `dame += pet.dame` |
| Porata (6) | dùng item 454 (bông tai) | vô hạn, dùng lại để tách | như trên |
| Porata 2 (8) | item 921 | vô hạn | như trên + **toàn bộ option của item 921 trong túi** |
| Porata 3 (9) | item 1819 | vô hạn | như trên + option item 1819 trong túi |
| Gogeta (10) | `fusionGogeta(true)` — **không có nơi gọi** | | |

Sau tách (`unFusion`) phải chờ `TIME_WAIT_AFTER_UNFUSION = 5000` ms. Khi hợp thể `setFullHpMp()`. Chi tiết ở doc 05.

### 8.4 Biến hình (khỉ)

Xem doc 04 mục 7.4: HP +(lv+3)×10%, SĐ +(lv+3)%, crit = 110.

### 8.5 Buff từ vật phẩm (ItemTime) — `item/ItemTime.java`, `UseItem.useItemTime`

| Item | Cờ | Thời gian | Tác dụng |
|---|---|---|---|
| 382 Bổ huyết | `isUseBoHuyet` | 600 000 ms | HP max ×2 |
| 1152 Bổ huyết 2 | `isUseBoHuyet2` | 600 000 | HP max ×2.2 |
| 383 Bổ khí | `isUseBoKhi` | 600 000 | KI max ×2 |
| 1151 Bổ khí 2 | `isUseBoKhi2` | 600 000 | KI max ×2.2 |
| 381 Cuồng nộ | `isUseCuongNo` | 600 000 | SĐ ×2 |
| 1150 Cuồng nộ 2 | `isUseCuongNo2` | 600 000 | SĐ ×2.2 |
| 384 Giáp xên | `isUseGiapXen` | 600 000 | sát thương nhận (skill/quái) ÷2 |
| 1153 Giáp xên 2 | `isUseGiapXen2` | 600 000 | sát thương nhận ×40% |
| 385 / 1154 Ẩn danh | `isUseAnDanh(2)` | 600 000 | không bị thêm vào danh sách kẻ thù |
| 663–667 Thức ăn | `isEatMeal` | 600 000 | SĐ +10% |
| 880–882 Thức ăn 2 | `isEatMeal2` | 600 000 | theo icon: 8060 SĐ +5%, 8061 +5% SĐ chí mạng, 8062 HP +5% |
| 1614 / 1615 / 1616 Nước mía 1/2/3 | `isUseNuocMia1/2/3` | 600 000 | 1: HP +10%; 2: HP +10%, crit +10; 3: HP +10%, SĐ +10%, def +10%, SĐCM +10% |
| 638 Commeson | `isUseCMS` | 3 600 000 | sát thương quái đánh ×0.1 |
| 579, 1045 Đuôi khỉ | `isUseDK` | 1 800 000 | TNSM +2×tn |
| 1233 Nồi cơm điện | `isUseNCD` | 1 800 000 | tuyệt kỹ `typeItem = 2` (Ma phong ba ×2) |
| 1532 | `isUseKhoBauX2` | 1 800 000 | TNSM +2×tn trong map 135–138 |
| 1628 Bùa Santa | `isUseBuaSanta` | 1 800 000 (cộng dồn 30 phút) | TNSM đệ tử +2×tn |
| 379 Máy dò | `isUseMayDo` | 1 800 000 | (dò capsule) |
| Rồng xương (Shenron_Event) | `isUseRX` | — | HP/KI/SĐ +10% |
| Không cho dùng cùng lúc bản 1 và bản 2 (và 3 loại nước mía) | | | |

### 8.6 Giáp tập luyện (slot 6) — `setDameTrainArmor`

`getPercentTrainArmor`: 529/534 → 10; 530/535 → 20; 531/536 → 30; 1716 → 40.

- **Đang mặc** (slot 6 có item): `wearingTrainArmor = true`, `tlSubSD += %` (giảm sức đánh).
- **Không mặc**, có giáp tập (type 32) trong túi với option 9 (`Hiệu lực trong # phút`) > 0: `tlDame.add(%)` (tăng sức đánh).
- `EffectSkin.updateTrainArmor`: mỗi 60 s, nếu đang mặc **và** có đánh trong 30 s gần nhất → option 9 `+1` (tối đa 1000); mỗi 60 s mọi giáp tập trong túi/rương option 9 `−1`.

---

## 9. Sát thương gây ra (tóm tắt) & sát thương nhận vào (injured)

### 9.1 Sát thương gây ra

Công thức đầy đủ `NPoint.getDameAttack` ở `docs/04-ky-nang.md` mục 4. Tóm tắt:

```
dmg = dame × skillDamage% × (1 + nộiTại%) × (1 + dameAfter%) × (1 + buffSexy%) × Π(1 + tlDameAttMob%) [đánh quái]
if crit: dmg ×= 2; dmg += dmg × tlSDCM%
dmg += dmg × percentXDame%       (set)
dmg ± ~5%
```

### 9.2 Sát thương nhận vào — `Player.injured(plAtt, damage, piercing, isMobAttack)`

```
if chết: return 0
ghi plAtt vào temporaryEnemies
if map Mabư && plAtt không phải boss && skill ∈ {Kame, Masenko, Antomic, Dragon, Demon, Galick, Liên hoàn, Kaioken}:
    damage = min(damage, hpMax/20)
if plAtt là boss: effectSkin.isVoHinh = false
if plAtt đang bị nhốt bình && < 3000 ms: return 0
if plAtt là người && this có maBuHold && map 128: precentMabuHold++, damage = 1
if plAtt cầm NRNM && this là boss/newPet: return 1
if (một bên cầm NRNM) && cùng bang: return 0
if this vừa hồi sinh < 1500 ms: return 0

if skill ∈ {Kame, Masenko, Antomic} && voHieuChuong > 0:
    hồi KI = damage × voHieuChuong/100 ; return 0

tlGiap = nPoint.tlGiap ; tlNeDon = nPoint.tlNeDon
if plAtt != null && !isMobAttack:
    if skill ∈ {Kame, Masenko, Antomic, Dragon, Demon, Galick, Liên hoàn, Kaioken, QCKK, Makanko, DCTT}:
        tlNeDon −= plAtt.tlchinhxac
    else: tlNeDon = 0                       (skill khác không né được)
    chưởng (Kame/Masenko/Antomic): tlGiap = max(0, tlGiap − plAtt.tlxgc)
    đấm (Dragon/Demon/Galick/Liên hoàn/Kaioken): tlGiap = max(0, tlGiap − plAtt.tlxgcc)
if piercing: tlGiap = 0
tlNeDon = min(tlNeDon, 90) ; tlGiap = min(tlGiap, 86)
if random < tlNeDon%: return 0              (né)
if effectSkill.isXinbato && random < tlNeDonXinbato%: return 0

damage −= (damage / 100) × tlGiap
if !piercing: damage = max(1, damage − def)          (subDameInjureWithDeff; <0 → 1)

if (skill đánh thuộc nhóm trên && !piercing) || isMobAttack:
    Giáp xên 1 (không có GX2): damage /= 2
    Giáp xên 2: damage = damage/100 × 40
if !piercing && đang khiên && !isMobAttack:
    idMark.damePST = damage
    if damage > hpMax: vỡ khiên
    damage = 1 (map phó bản: 10)
damage = min(damage, INT_MAX)
if isMobAttack && bùa Bất tử && damage >= hp: damage = hp − 1
if map 129 (ĐHVT 23) && damage >= hp: thua trận, return 0
if map 51: totalDamageTaken += damage
subHP(damage)
if chết (do người hoặc quái) và không phải boss/newPet:
    if random < tlBom%: setBom (= setDie)
    else setDie(plAtt)
return damage
```

### 9.3 Quái đánh người — `Mob.mobAttackPlayer`

```
dameMob = mob.point.getDameAttack()
if bùa Da trâu: dameMob /= 2
if là đệ tử & sư phụ có bùa Đệ tử: dameMob /= 2
if mob.lvMob > 0 && không phải phó bản: dameMob = hpMax × 10%
if vệ tinh phòng thủ (satellite.isDefend): dameMob −= dameMob/5
if Commeson: dameMob = round(dameMob × 0.1)
dame = player.injured(null, dameMob, piercing=false, isMobAttack=true)
mob nhận phản sát thương: dame × tlPST% (tối đa hp quái − 1)
```

### 9.4 Hút HP/KI, phản sát thương (người ↔ người)

Xem doc 04 mục 3.6 – 3.7. Hút HP khi đánh quái = `tlHutHp + tlHutHpMob`.

---

## 10. Hồi HP/KI, thể lực, bay

| Cơ chế | Chu kỳ | Công thức | Nguồn |
|---|---|---|---|
| Hồi tự nhiên | 30 000 ms | `hoiPhuc(hpHoi, mpHoi)` (mục 7.7) | `NPoint.update` |
| Tái tạo năng lượng | ~1 s × 10 | `hpMax/100 × (lv+3)` | `NPoint.update` |
| Thể lực | 60 000 ms | `stamina++` tới `maxStamina` | `NPoint.update` |
| Tiêu thể lực khi đánh | mỗi 500 đòn | `stamina−−` (bùa Dẻo dai: không tốn) | `SkillService.useSkillAttack` |
| Tiêu KI khi bay | mỗi gói di chuyển trên không, không có thú cưỡi | `mp −= mpg / (100 × (khỉ ? 2 : 1))` | `PlayerService.playerMove` |
| Đậu thần | CD 1000 ms | option 2: `param×1000`, option 48: `param` HP & KI; đệ cùng map: +`100 × cấp đậu` thể lực và cùng lượng HP/KI | `UseItem.eatPea` |
| Hút HP/KI xung quanh (option 8) | 5 000 ms, 200 px | lấy `param%` hpMax/mpMax mỗi người/quái (không giết) | `EffectSkin.updateXenHutXungQuanh` |
| Chibi type 1 / 3 | 1 000 ms | +10% mpMax / +10% hpMax | `Player.update` |

`hoiPhuc` → `addHp/addMp` (cap `hpMax/mpMax`), không áp khi đã chết.

---

## 11. Tiềm năng / sức mạnh nhận được (TNSM)

### 11.1 Từ quái — `Mob.getTiemNangForPlayer(pl, dame)`

```
levelPlayer = Service.getCurrLevel(pl)       (mục 12)
checkLevel  = |levelPlayer − mob.level|
tn = (long)(dame + hpFull × 0.0005)
if mob.tempId == 0: tn = 1
if checkLevel > 5 && levelPlayer > mob.level: tn = 1
else: tn = (long)(tn / ((int)(checkLevel × 0.5) + 1.25))
tn = max(tn, 1)
tn = pl.nPoint.calSucManhTiemNang(tn)
```
Gọi mỗi đòn trúng trong `Mob.injured` → `Service.addSMTN(plAtt, 2, tn, true)` và `TrainingService.tangTnsmLuyenTap` (luyện tập: `+max(100, tn / (100 × (level+1)))`, tối đa 10 000 000).

### 11.2 Hệ số — `NPoint.calSucManhTiemNang(tiemNang)`

```
if power >= getPowerLimit(): return 10
for tl in tlTNSM: tn += tn × tl%                        (nhân dồn)
base = tn
+ base        nếu bùa Trí tuệ (213)
+ base × 3    nếu bùa Trí tuệ x3 (671)
+ base × 4    nếu bùa Trí tuệ x4 (672)
+ base × 4    nếu bùa Trí tuệ x4 (lặp lại — bug, thực tế +8)
+ base × 3    nếu timevip > now
+ base × 2    nếu Chibi type 2
+ base × 3    nếu session.vip > 0 (đệ tử: của sư phụ)
+ base × 2    nếu Đuôi khỉ
+ base × 2    nếu map 135–138 và item 1532
+ base / 5    nếu vệ tinh trí tuệ
tn += tn × param1%    nếu nội tại 24
if power >= 60 000 000 000: tn −= tn × 80%
if là đệ tử:
    + base × 2                       nếu sư phụ dùng Bùa Santa
    + base/100 × (tlTNSMPet + 100)   nếu sư phụ có option 160
map Ngũ Hành Sơn: ×1 ; map Bản đồ kho báu: ×1.5
if cFlag != 0: +10% (cờ 8) / +5% (cờ khác)
tn ×= Manager.RATE_EXP_SERVER            (Config.properties: server.expserver=3)
tn = calSubTNSM(tn) ; if tn <= 0: tn = 1
```

`calSubTNSM(tn)` (theo `power` hiện tại):

| Sức mạnh | Chia |
|---|---|
| ≥ 90 tỷ | /100 |
| ≥ 80 tỷ | /90 |
| ≥ 60 tỷ | /50 |
| ≥ 50 tỷ | /40 |
| ≥ 40 tỷ | /30 |
| < 40 tỷ | không chia |

### 11.3 Cộng — `Service.addSMTN(player, type, param, isOri)`

| Đối tượng | Xử lý |
|---|---|
| Đệ tử | đệ `power += param; tiemNang += param` (không kiểm tra giới hạn của đệ); sư phụ: `masterParam = calSubTNSM(param × 0.5)`, cắt theo giới hạn SM sư phụ, `powerUp + tiemNangUp`, **rồi gọi thêm `addSMTN(master, type, masterParam)`** → sư phụ nhận 2 lần |
| Bot | cộng thẳng |
| Người | nếu `power >= limit` → bỏ; nếu vượt → cắt; type 1: chỉ TN; type 2: SM + TN; khác: chỉ SM. `isOri && clan` → `clan.addSMTNClan` |

---

## 12. Cấp bậc sức mạnh (tên cấp)

`Service.getCurrLevel(pl)` & `ListCaption(gender)`:

| Level | Sức mạnh | Tên |
|---|---|---|
| 0 | < 3 000 | Tân thủ |
| 1 | < 15 000 | Tập sự sơ cấp |
| 2 | < 40 000 | Tập sự trung cấp |
| 3 | < 90 000 | Tập sự cao cấp |
| 4 | < 170 000 | Tân binh |
| 5 | < 340 000 | Chiến binh |
| 6 | < 700 000 | Chiến binh cao cấp |
| 7 | < 1 500 000 | Vệ binh |
| 8 | < 15 000 000 | Vệ binh hoàng gia |
| 9 | < 150 000 000 | Siêu nhân cấp 1 (TĐ) / Siêu Namếc cấp 1 / Siêu Xayda cấp 1 |
| 10 | < 1 500 000 000 | Siêu … cấp 2 |
| 11 | < 5 000 000 000 | Siêu … cấp 3 |
| 12 | < 10 000 000 000 | Siêu … cấp 4 |
| 13 | < 40 000 000 000 | Thần {hành tinh} cấp 1 |
| 14 | < 50 010 000 000 | Thần {hành tinh} cấp 2 |
| 15 | < 60 010 000 000 | Thần {hành tinh} cấp 3 |
| 16 | < 70 010 000 000 | Giới Vương Thần cấp 1 |
| 17 | < 80 010 000 000 | Giới Vương Thần cấp 2 |
| 18 | < 90 010 000 000 | Giới Vương Thần cấp 3 |
| 19 | ≥ 90 010 000 000 | Thiên đạo |

List còn 2 tên "kẻ hũy diệt vũ trụ", "vô địch đa giới" (index 20, 21) nhưng `getCurrLevel` tối đa 19.

---

## 13. Chết & hồi sinh

### 13.1 `Player.setDie(plAtt)`

```
if người chơi thật:
    vangtru = min(power / 1 000 000, 32 000)
    vang = vangtru − rand(10..100)
    if gold >= vang && vang >= 1:
        gold −= vang
        rơi ra map: vang × 95%  (item 189 nếu < 10 000; 188 nếu < 20 000; 190 còn lại)
if map Tương lai hoặc map lạnh: power −= max(1, (int)(power × 0.1%))
reset xHPKI/xDame = 1 ; huỷ gồng QCKK/Laze/Tự sát ; gỡ hiệu ứng skill
hp = 0 ; mp = 0 ; huỷ MobMe
nếu cả 2 là người thật (không pet/bot/boss) && kẻ giết không Ẩn danh: thêm vào danh sách kẻ thù
typePk = 0 ; nếu đang PVP (trừ map 140) → thua
rơi Ngọc rồng sao đen / Ngọc rồng Namếc đang cầm
```

Ngoài ra `Player.isDie()`: nếu hp <= 0 trong **map Tương lai** và `!hasReducedPower` → `power = originalPower × 0.99` (1 lần, cờ không bao giờ reset trong phiên).

### 13.2 Hồi sinh — `PlayerService.hoiSinh` (client yêu cầu)

| Điều kiện | Chi phí |
|---|---|
| map NRSĐ | 50 000 vàng (`COST_GOLD_HOI_SINH_NRSD`) |
| map khác (trừ map 51) | 1 ngọc (`COST_GEM_HOI_SINH`) |
| Chặn spam | `now − lastTimeRevived > 1500` |

`hoiSinhMaBu`: map Mabư 50 000 vàng, map khác 20 000 vàng. Kết quả `Service.hsChar(pl, hpMax, mpMax)` → `setJustRevivaled()` (miễn sát thương 1 500 ms).

Đệ tử: tự hồi sinh sau `120 000` ms (`Pet.update`).

---

## 14. PK, cờ (cFlag) và túi/flag_bag

### 14.1 typePk (`ConstPlayer`)

| Giá trị | Hằng | Ghi chú |
|---|---|---|
| 0 | `NON_PK` | |
| 3 | `PK_PVP` | không được Trị thương |
| 4 | `PK_PVP_2` | hai người cùng 4 → nhận TNSM khi đánh nhau |
| 5 | `PK_ALL` | đánh được tất cả; Mabư giữ đặt 5 |

### 14.2 `SkillService.canAttackPlayer2(p1, p2)`

```
nếu một bên là newPet hoặc NonInteractiveNPC → false
nếu một bên PK_ALL → true
nếu 2 người thật và một bên có killCharId = id bên kia (cừu sát) → true
nếu cả hai cFlag != 0 và (một bên cờ 8 hoặc cờ khác nhau) → true
nếu một bên không có pvp → false
return p1.pvp.isInPVP(p2) || p2.pvp.isInPVP(p1)
```

### 14.3 Đổi cờ — `Service.chooseFlag / changeFlag`

- Không được đổi trong map NRSĐ và map Mabư. Hồi 60 000 ms giữa 2 lần.
- `flagIconId = {2761, 2330, 2323, 2327, 2326, 2324, 2329, 2328, 2331, 4386, 4385, 2325}` (cờ 0–11). Đệ tử đổi theo.
- Map Mabư: tự gán cờ ngẫu nhiên 9 hoặc 10; rời map Mabư → cờ 0 (`Player.update`).
- Cờ ảnh hưởng TNSM (+5% / +10% với cờ 8) và Huýt sáo/Trị thương (cùng cờ).

### 14.4 Flag bag (túi đeo lưng, slot 8) — `Player.getFlagBag`, bảng `flag_bag`

Ưu tiên: đang cầm NRSĐ → 31; cầm NRNM (353–359) → 30; nhiệm vụ `TASK_3_2` → 28; item body slot 8 → `template.part`; (đệ tử: slot 7); có bang → `clan.imgId`; không → −1.
Bảng `flag_bag` (trích): 0 Cờ xám … 8 Cờ xanh dạ (10 000 vàng); 9 Cờ đỏ, 10–18 Khăn (50 000 vàng); 19 Ba lô (200 ngọc), 20 Đao (500 ngọc), 21 Gậy (400), 22 Mai rùa (300); 23–27 Giỏ (100 000 vàng); 29 Gậy phép (600 ngọc); 30 Viên ngọc Namếc; 31 Viên ngọc sao đen; 32–36 Lồng đèn (50 ngọc); 37 Gậy thần chết, 38 Cánh dơi (1 000 000 000 vàng)…

---

## 15. Bùa (Charms)

`player/Charms.java` — `addTimeCharms(itemId, min)`: nếu hết hạn thì đặt lại từ `now`, sau đó `+min × 60 000`.

| Item | Trường | Tên | Tác dụng thực tế (code) |
|---|---|---|---|
| 213 | `tdTriTue` | Trí tuệ | TNSM `+1 × base` |
| 214 | `tdManhMe` | Mạnh mẽ | đánh quái `+150%` |
| 215 | `tdDaTrau` | Da trâu | quái đánh `÷2` |
| 216 | `tdOaiHung` | Oai hùng | không có: quái "tinh anh" (`lvMob > 0`) bị cố định sát thương & phản công (`Mob.injured`) |
| 217 | `tdBatTu` | Bất tử | quái không giết được (còn 1 HP); khi HP ≤ 1 người chơi không gây sát thương cho quái |
| 218 | `tdDeoDai` | Dẻo dai | không tốn thể lực |
| 219 | `tdThuHut` | Thu hút | tự nhặt vật phẩm quái rơi (đệ tử giết → sư phụ nhặt) |
| 522 | `tdDeTu` | Đệ tử | đệ đánh quái ×2 (thực tế ×4), đệ nhận sát thương quái ÷2, đệ tốn thể lực mỗi 5 đòn, đệ đánh khi sư phụ không đánh |
| 671 | `tdTriTue3` | Trí tuệ x3 | TNSM `+3 × base` |
| 672 | `tdTriTue4` | Trí tuệ x4 | TNSM `+4 × base` hai lần (= +8) |
| 2025 / 2076 / 1387 | `tdDeTuMabu/2/3` | | không có code đọc |
| 3000–3003 | `tdPhuHP/KI/SD/TNSM` | | không có code đọc |

Shop bùa `BUA_1H`, `BUA_8H`, `BUA_1M` hiển thị thời gian còn lại bằng option 63 (ngày) / 64 (giờ) / 65 (phút) (`ShopService.resolveShopBua`). Mỗi ngày nhận 1 bùa 1h miễn phí ở Bà Hạt Mít (`sendTextTimeDaiLyGift`).

---

## 16. EffectSkin (hiệu ứng từ trang bị)

`player/EffectSkin.java`, `update()` mỗi giây (không chạy khi chết/map offline trừ mục đánh dấu):

| Hiệu ứng | Điều kiện | Chu kỳ | Tầm | Tác dụng |
|---|---|---|---|---|
| Vô hình | option 105 | liên tục | — | `isVoHinh = true` khi không đánh 5 s và không bị boss đánh 5 s (boss bỏ qua mục tiêu vô hình) |
| Ở dơ (Hôi) | option 109 | 10 s | 200 px | người không phải boss mất `hpMax × p%` (không chết), piercing |
| Xinbato (Phân tâm) | option 111 trên body | 10 s | 200 px | đặt `tlNeDonBuffXinbato = min(50, 90 − tlNeDon)` cho mọi người (kể cả bản thân), hết sau 10 s |
| Thỏ Bulma / Đẹp | `tlSexyDame > 0` | 10 s | 120 px | mỗi người xung quanh làm **bản thân người mặc** nhận `setDameBuff(11 000 ms, tlSexyDame)` |
| Ma phong ba | đang bị nhốt bình | 500 ms | — | xem doc 04 mục 9.3 |
| Hút xung quanh | option 8 | 5 s | 200 px | mục 10 |
| Tàng hình | option 25 | 5 s | — | tàng hình 1 500 ms |
| Hoá đá | option 26 | 30 s | 200 px | hoá đá người khác 6 000 ms |
| Làm chậm | option 24 | 10 s | 200 px | người khác speed = 1 trong 5 000 ms |
| X chưởng | option 159 | 60 s | — | bật `isXChuong` |
| Giáp tập luyện | slot 6 | 60 s | — | mục 8.6 |
| Phù NRSĐ | `xHPKI`, `xDame` | reset sau 1 800 000 ms hoặc khi rời map NRSĐ | — | nhân HP/KI/SĐ |
| Cải trang hải tặc | `ctHaiTac` (618–626) | 5 s | 300 px | đếm số loại cải trang hải tặc khác nhau gần nhau `count` → option 147/77/103 của mọi cải trang = `count × 3` |

---

## 17. Chibi (hiệu ứng ngẫu nhiên)

`Player.update()` mỗi giây, với người chơi thật, còn sống, **chưa** chibi, không ở map NRSĐ: `isTrue(20, 100)` → `EffectSkillService.setChibi(player, 600 000)`, `typeChibi = nextInt(0, 3)`:

| typeChibi | Tác dụng |
|---|---|
| 0 | chỉ ngoại hình |
| 1 | mỗi giây +10% mpMax KI |
| 2 | TNSM `+2 × base` |
| 3 | HP tối đa ×2, hồi đầy HP khi bắt đầu, mỗi giây +10% hpMax |

Hết 10 phút → `removeChibi`, giây tiếp theo có 20% bị gán lại → trên thực tế người chơi gần như luôn ở trạng thái chibi.

---

## 18. Ngọc rồng sao đen (RewardBlackBall)

`player/RewardBlackBall.java`: nhặt ngọc sao `n` → `timeOutOfDateReward[n−1] = now + 79 200 000` ms (22 giờ).

| Sao | Hằng dùng | Tác dụng |
|---|---|---|
| 1 | `R1S_2 = 21` | SĐ +21% |
| 2 | `R2S_1 = 35` | HP +35% |
| 3 | `R3S_1 = 35` | Hút HP +35% |
| 4 | `R4S_2 = 35` | Phản sát thương +35% |
| 5 | `R5S_1 = 35` | SĐ chí mạng +35% |
| 6 | `R6S_1 = 40` | KI +40% |
| 7 | `R7S_1 = 14` | Né đòn +14% |

Các hằng `R1S_1, R2S_2, R3S_2, R4S_1, R5S_2, R5S_3, R6S_2, R7S_2` không được dùng. `getReward` chỉ báo "Chỉ Số Tự Cộng Khi Nhặt xong".

---

## 19. Danh hiệu (player_badges)

### 19.1 Cấu trúc

- `BadgesData { idBadGes (= idEffect), timeofUseBadges, isUse }` — danh sách `player.dataBadges`.
- `BagesTemplate { id, idEffect, idItem, NAME, options }` — load từ bảng `data_badges`.
- Tạo mới `new BadgesData(player, id, days)`: hạn `now + days × 86 400 000`, tắt mọi danh hiệu khác, bật cái mới.
- `BadgesService.turnOnBadges(player, id)`: chỉ bật đúng id.
- `Player.autoSendBadges()` (mỗi giây): xoá danh hiệu hết hạn; `badges.idBadges` = cái đang bật; mỗi 10 s gửi hiệu ứng + `Service.point`.
- `BagesTemplate.sendListItemOption(player)`: option của mọi danh hiệu `isUse`.

### 19.2 Option danh hiệu được đọc ở đâu

Danh hiệu **không** đi qua `addOption`. Chỉ các option sau có tác dụng:

| Option | Nơi đọc | Công thức |
|---|---|---|
| 77 | `setHpMax` (cuối) | `hpMax += p%` |
| 103 | `setMpMax` (cuối) | `mpMax += p%` |
| 50 | `setDame` | `dame += p%` |
| 117 | `setDame` | `tlSexyDame += p` |
| 108 | `setPointWhenWearClothes` | `tlNeDon += tlNeDon × p / 100` (nhân với né đang có) |

Option 5 (SĐ chí mạng), 93 (hạn dùng) không có tác dụng.

### 19.3 Bảng `data_badges`

| id | idEffect | idItem | Tên | Option |
|---|---|---|---|---|
| 1 | 218 | 1289 | Đại gia mới nhú | SĐ +15% |
| 2 | 219 | 1290 | Trùm ước rồng | HP +6% |
| 3 | 220 | 1291 | Trùm săn boss | SĐ +5% |
| 4 | 221 | 1292 | Thánh đập đồ +7 | HP +10%, KI +10% |
| 5 | 222 | 1293 | Cao thủ siêu hạng | HP +8% |
| 6 | 223 | 1294 | Nông dân chăm chỉ | HP +5%, KI +5% |
| 7 | 224 | 1295 | Ông thần ve chai | Né +3% (nhân) |
| 8 | 225 | 1296 | Bị móc sạch túi | Né +5% (nhân), HP +5%, KI +5% |
| 9 | 228 | 1299 | Fan cứng | SĐ/HP/KI +3% |
| 12 | 242 | 1392 | Gõ đầu trẻ | HP +10%, KI +10%, (SĐCM 10% – không tác dụng) |
| 13 | 243 | 1393 | Gõ đầu trẻ | như trên |
| 14 | 240 | 1394 | Gõ đầu trẻ | như trên |
| 15 | 247 | 1457 | X-mas | SĐ/HP/KI +12% |
| 16 | 253 | 1514 | Em xinh, em đẹp | SĐ/HP/KI +11%, Đẹp +5% |
| 17 | 256 | 1790 | Mẹ Rồng | SĐ +13%, HP +13%, (SĐCM 7%) |
| 18 | 226 | 1297 | KOL | SĐ +10%, HP +10%, `{"param":103,"id":10}` (đảo id/param) |

Mọi danh hiệu có option 93 = 30 (ngày).

### 19.4 Nhiệm vụ danh hiệu — `task_badges_template`

| id | Nhiệm vụ | maxCount | Thưởng (idEffect) |
|---|---|---|---|
| 1 | Nạp tích luỹ 1 triệu trong ngày | 1 000 000 | 218 |
| 2 | Ước rồng thần 1 sao x100 | 100 | 219 |
| 3 | Hạ Cumber/Black Goku/Cooler/Xên 300 lần | 300 | 220 |
| 4 | Đập 5 trang bị +7 trong ngày | 5 | 221 |
| 5 | Top 1 Đại hội võ đài siêu hạng | 1 | 222 |
| 6 | 10 nhiệm vụ siêu khó tại Bò Mộng | 10 | 223 |
| 7 | Đánh bại/cho xương Sói 20 lần | 20 | 1286 |
| 8 | 5 nhiệm vụ Xinbato nước | 5 | 1287 |
| 9 | Nhặt đồ 500 lần trong ngày | 500 | 224 |
| 10 | Tiêu diệt 30 boss Ăn Trộm | 30 | 225 |
| 11 | Tiêu diệt 30 boss Ở Dơ | 30 | 1300 |
| 12/13/14 | Mở rương gỗ cấp 12 (10/20/30) | 10/20/30 | 240/242/243 |
| 15 | Đạt 500 điểm sự kiện | 500 | 247 |
| 16 | Nạp tích luỹ 2 triệu trong ngày | 2 000 000 | 253 |
| 17 | Sở hữu 7 rồng nhí vĩnh viễn | 7 | 256 |
| 18 | Fan cứng KOL | 1 | 226 |

Nhiệm vụ reset hằng ngày trong `PlayerService.dailyLogin` (`BadgesTaskService.createAndResetTask`).

---

## 20. player_system

| File | Nội dung |
|---|---|
| `AntiLogin.java` | Sai mật khẩu ≥ `MAX_WRONG = 5` lần → khoá đăng nhập `TIME_ANTI = 60 000` ms; hết thời gian → reset. |
| `GiftCode.java` | Model giftcode. |
| `Template.java` | Các template: `ItemOptionTemplate`, `ItemTemplate` (id, type, gender, name, strRequire, head/body/leg, gold/gem/ruby…), `MobTemplate` (hp, percentDame, percentTiemNang…), `NpcTemplate`, `SkillTemplate`… |

Các lớp phụ trong `player/`: `Satellite` (vệ tinh HP/MP/Trí tuệ/Phòng thủ, mỗi cờ tự tắt sau 3 000 ms nếu không được làm mới; Trí tuệ: TNSM +20%, Phòng thủ: sát thương quái −20%), `SuperRank` (Siêu hạng: rank/win/lose/ticket = 3, thưởng ngọc sau nửa đêm theo `SuperRankService.reward(rank)`), `Traning` (top, topWhis, time…), `Achievement`, `IDMark`, `Inventory`, `ItemEvent`, `DropItem`, `FightMabu`, `KOLProgressData`, `Location`, `Friend/Enemy`.

---

## 21. Nội tại (intrinsic)

### 21.1 Dữ liệu (`intrinsic` table)

`name` chứa placeholder `p0..p3` = `paramFrom1, paramTo1, paramFrom2, paramTo2`. Khi mở: `param1 = rand[paramFrom1, paramTo1]`, `param2 = rand[paramFrom2, paramTo2]`.

| id | Tên | param1 | param2 | gender | Cài đặt trong code |
|---|---|---|---|---|---|
| 0 | Chưa kích hoạt nội tại | 0 | 0 | 3 | — |
| 1 | Chiêu đấm Dragon +% sát thương | 5–25 | | 0 | `getDameAttack` |
| 2 | Chiêu Kamejoko +% sát thương | 5–25 | | 0 | `getDameAttack` |
| 3 | Thái Dương Hạ San +% tốc độ, −% KI | 10–35 | 10–35 | 0 | giảm hồi chiêu (param2 không dùng) |
| 4 | Quả cầu kênh khi +% tốc độ hồi phục | 15–55 | | 0 | giảm hồi chiêu |
| 5 | Khiên năng lượng +% tốc độ hồi phục | 15–55 | | 0 | giảm hồi chiêu |
| 6 | Dịch chuyển tức thời +% sát thương đòn kế | 50–150 | | 0 | `dameAfter` |
| 7 | Thôi miên +% sát thương đòn kế | 50–150 | | 0 | `dameAfter` |
| 8 | Chiêu đấm Demon +% sát thương | 5–25 | | 1 | `getDameAttack` |
| 9 | Chiêu Masenko +% sát thương | 2–25 | | 1 | `pDS` + `pXD` + giảm hồi chiêu |
| 10 | Trị thương +% tốc độ hồi phục | 15–65 | | 1 | giảm hồi chiêu |
| 11 | Makankosappo +% tốc độ hồi phục | 15–55 | | 1 | giảm hồi chiêu |
| 12 | Đẻ trứng +% tốc độ hồi phục | 15–65 | | 1 | giảm hồi chiêu |
| 13 | Liên hoàn +% sát thương | 5–25 | | 1 | `getDameAttack` |
| 14 | Biến Sôcôla +% sát thương đòn kế | 50–150 | | 1 | `dameAfter` |
| 15 | Khiên năng lượng +% tốc độ hồi phục | 15–55 | | 1 | giảm hồi chiêu |
| 16 | Chiêu đấm Galick +% sát thương | 5–25 | | 2 | `getDameAttack` |
| 17 | Chiêu Antomic +% sát thương | 5–25 | | 2 | `getDameAttack` |
| 18 | Biến hình +% sát thương | 5–25 | | 2 | mọi đòn khi đang khỉ |
| 19 | Tự phát nổ +% tốc độ hồi phục | 15–65 | | 2 | giảm hồi chiêu |
| 20 | Khiên năng lượng +% tốc độ hồi phục | 15–55 | | 2 | giảm hồi chiêu |
| 21 | Huýt sáo +% tốc độ hồi phục | 15–65 | | 2 | giảm hồi chiêu |
| 22 | Trói +% sát thương đòn kế | 50–150 | | 2 | `dameAfter` |
| 23 | Vàng rơi từ quái +% | 25–300 | | 3 | **không có code sử dụng** |
| 24 | Sức mạnh và tiềm năng khi đánh quái +% | 5–35 | | 3 | `calSucManhTiemNang` |
| 25 | Chí mạng liên tục khi HP dưới % | 20–50 | | 3 | `setIsCrit` |

`Manager` phân loại: gender 0 → `INTRINSIC_TD`, 1 → `INTRINSIC_NM`, 2 → `INTRINSIC_XD`, 3 → cả ba. Mỗi hành tinh: 7–8 nội tại riêng + 23, 24, 25.

"Giảm hồi chiêu" = cooldown thực tế `coolDown × (1 − param1/100)` (doc 04 mục 3.3).
"Đòn kế" = `dameAfter`: đòn đánh tiếp theo (bất kỳ skill nào đi qua `getDameAttack`) `+param1%`, sau đó reset 0.

### 21.2 Mở / quay nội tại — `IntrinsicService`

Menu: `Controller` cmd → `IntrinsicService.showMenu` ("Xem tất cả Nội Tại", "Mở Nội Tại", "Mở VIP"). Xử lý ở `NpcFactory` (`ConstNpc.INTRINSIC`, `CONFIRM_OPEN_INTRINSIC`, `CONFIRM_OPEN_INTRINSIC_VIP`).

| Loại | Điều kiện | Giá | Sau khi mở |
|---|---|---|---|
| Thường (`open`) | SM ≥ 10 000 000 000 | `COST_OPEN[countOpen] × 1 000 000` vàng | `countOpen++` (tối đa index 7) |
| VIP (`openVip`) | SM ≥ 10 000 000 000 | 100 ngọc | `countOpen = 0` |

`COST_OPEN = {10, 20, 40, 80, 160, 320, 640, 1280}` (triệu vàng) → lần 1: 10tr, lần 2: 20tr, …, từ lần 8 trở đi luôn 1 280tr.

`changeIntrinsic`: chọn `list.get(nextInt(1, size − 1))` (bỏ phần tử index 0), random param, thông báo.

---

## 22. Ghi chú / điểm cần lưu ý

1. **Overflow cộng tiềm năng SĐ**: `point × (2×dameg + point − 1) / 2 × 100` tính bằng `int` rồi mới gán `long` → với `point` lớn (vd 1000) và `dameg` > ~21 000 sẽ tràn số (âm) → trừ tiềm năng âm (= cộng). Giới hạn `point <= 1000` do server kiểm tra.
2. **Giáp & chí mạng** không nhân `point` trong chi phí: gửi `point = 100` chỉ tốn phí 1 điểm nhưng cộng 100 (vẫn bị chặn bởi `getDefLimit/getCritLimit`).
3. `setMpMax`: worldcup và đệ typePet 2/3/4 dùng `this.mpMax` (giá trị **cũ**) thay cho biến cục bộ `mpMax` → cộng dồn/chênh lệch sau mỗi lần `calPoint`.
4. `setDame`: `tlSexyDame` được cộng **2 lần**; đệ typePet 3 được +20% SĐ **2 lần** (comment "pet pic" và "pet beer").
5. `setCrit`: `crit += 10 / 100` = 0 (set Thần Vũ Trụ Kaio); khỉ ghi đè `crit = 110` rồi mới cộng nước mía.
6. `calSucManhTiemNang`: bùa Trí tuệ x4 bị cộng 2 lần (+8×). Nhiều hệ số VIP/bùa cộng dồn tuyến tính trên `base`.
7. `Service.addSMTN` (đệ tử): sư phụ nhận `masterParam` **2 lần** (cộng trực tiếp + gọi đệ quy). Đệ tử không bị kiểm tra giới hạn sức mạnh khi cộng.
8. `setTinhNhatNguyetAn()` chạy sau khi đã tính HP/KI/SĐ → Tinh/Nhật/Nguyệt ấn có hiệu lực trễ 1 lần `calPoint`.
9. **Xinbato**: `EffectSkill.isXinbato` không bao giờ được set `true` và `injured` dùng `nPoint.tlNeDon` (không cộng `tlNeDonBuffXinbato`) → buff né của Xinbato **không có tác dụng** (`getRealTlNeDon()` không được gọi).
10. `tlGold` (option 100/156) và nội tại 23 (vàng rơi) **không có code sử dụng**; `mpHoiCute` (option 162), `isDoSPL` (110), `tlDef` cũng không.
11. Option 156 trong code ("Giảm 50% SĐ…") khác mô tả DB ("Cộng dồn 1% sức đánh khi chỉ dùng chiêu đấm").
12. Danh hiệu option 108: `tlNeDon += tlNeDon × p / 100` — tại thời điểm đó `tlNeDon` chỉ có giá trị từ NRSĐ 7 sao (tối đa 14) → gần như không có tác dụng. Danh hiệu option 5 không được đọc. Danh hiệu KOL lưu sai `{"param":103,"id":10}`.
13. **Nội tại**: bảng `intrinsic` trong dump bị **nhân bản 8 lần** (208 dòng, không có PRIMARY KEY) → mỗi list hành tinh chứa 8 bản sao id 0 "Chưa kích hoạt". `changeIntrinsic` chỉ bỏ index 0 → xác suất quay trúng "Chưa kích hoạt" = 7/87 ≈ 8% (Trái Đất: list 88 phần tử).
14. `IntrinsicService.doinoitai`: `getName().substring(0, indexOf(" ["))` sẽ ném `StringIndexOutOfBoundsException` nếu tên không có " [" (id 0).
15. `TIME_OPEN_POWER = 8 640 000` ms = 2,4 giờ (có thể định ý là 24 h = 86 400 000).
16. `openPowerSpeed` không kiểm tra `canOpenPower()` → mở nhanh bằng vàng không cần đủ sức mạnh.
17. `ItemTime`: hết hạn Nước mía 2 và 3 đều so với `lastTimeUseNuocMia1` → thời hạn sai.
18. `NPoint.setHp(long)` / `setMp(long)` không cap theo `hpMax/mpMax` (chỉ cap INT_MAX) → đậu thần/huýt sáo/biến khỉ có thể đẩy HP vượt `hpMax` tới lần `calPoint` kế tiếp.
19. **Chibi**: 20%/giây tự gán → gần như luôn bật; type 3 ×2 HP, type 2 +2× TNSM → ảnh hưởng lớn tới cân bằng.
20. `Player.isDie()` có side-effect giảm 1% sức mạnh ở map Tương lai; `hasReducedPower` không reset → chỉ áp 1 lần mỗi phiên (và `originalPower` giữ giá trị cũ).
21. `setDie`: vàng mất `min(power/1e6, 32000) − rand(10..100)` — người có SM < ~10 triệu có `vang < 1` → không mất vàng.
22. Charms `tdDeTuMabu*`, `tdPhuHP/KI/SD/TNSM`: có lưu thời gian nhưng không có tác dụng.
23. Bùa Đệ tử: sát thương đệ đánh quái nhân 4 (xem doc 04 ghi chú 14).
24. `Mob.injured` với `lvMob > 0` và không có bùa Oai hùng: `damage = (maxHp <= 20tr ? maxHp × 1 : 2) × 0.1` — nhánh `: 2` cho sát thương 0 (int) với quái > 20 triệu HP. `lvMob` hiện luôn 0 (`Util.isTrue(0, 10000)`).
25. Khiên không chặn đòn quái; phản sát thương khi đang khiên tính theo sát thương **trước khiên**.
26. `hoiSinh` bị chặn ở map 51 (không hồi sinh bằng ngọc).
27. **Map nhà (21–23) không chạy phần lớn cập nhật**: trong `Player.update`, khối gọi `nPoint.update` (hồi HP/KI 30 s, Tái tạo NL, thể lực), `fusion.update`, `effectSkill.update` (hết hạn khỉ/choáng/khiên…), `effectSkin.update`, `pet.update`, `satellite.update`, chibi… chỉ chạy khi `!MapService.isHome(mapId)` → ở nhà không hồi phục tự nhiên, hiệu ứng không hết hạn, đệ tử đứng yên. Chu kỳ `Player.update` do `Manager` gọi `Zone.update()` mỗi 1 s (`Player.run/start` và `Map.run` (5 s) không được gọi).
28. **Thỏ Bulma / "Đẹp +#% SĐ cho mình và người xung quanh"** (option 117/226): `EffectSkin.updateThoBulma` gọi `setDameBuff(player, …)` lên **chính người mặc** (không phải người xung quanh), mà `getDameAttack` chỉ cộng buff khi `tlSexyDame == 0` → buff này không bao giờ có tác dụng; phần "cho mình" đã được cộng (2 lần) trong `setDame`.
