# 11b — Spec Boss phó bản, đấu trường, luyện tập, sự kiện

> Phần tiếp theo của [11-boss.md](./11-boss.md). Cơ chế chung (vòng đời `BossStatus`, `AppearType`, manager, các mẫu phần thưởng A/B/C, bảng tên skill) xem ở file 11. Toàn bộ số liệu lấy từ mã nguồn; tên map tra `map_template`, tên item tra `item_template`, tên option tra `item_option_template` trong `database team2026.sql`.

## Mục lục

- [1. Doanh trại Độc Nhãn (RedRibbonHQ)](#1-doanh-trại-độc-nhãn-redribbonhq)
- [2. Bản đồ kho báu (BanDoKhoBau)](#2-bản-đồ-kho-báu-bandokhobau)
- [3. Khí gas hủy diệt (DestronGas)](#3-khí-gas-hủy-diệt-destrongas)
- [4. Con đường rắn độc (SnakeWay)](#4-con-đường-rắn-độc-snakeway)
- [5. Siêu thần thủy — Ma vương Pôcôlô](#5-siêu-thần-thủy--ma-vương-pôcôlô)
- [6. Đại hội võ thuật lần 23](#6-đại-hội-võ-thuật-lần-23)
- [7. Võ đài Hạt Mít (Sinh tử)](#7-võ-đài-hạt-mít-sinh-tử)
- [8. Giải siêu hạng — bản sao đối thủ (Rival)](#8-giải-siêu-hạng--bản-sao-đối-thủ-rival)
- [9. Nhân bản Commeson](#9-nhân-bản-commeson)
- [10. Luyện tập tự động / thách đấu sư phụ](#10-luyện-tập-tự-động--thách-đấu-sư-phụ)
- [11. Boss sự kiện](#11-boss-sự-kiện)
  - [11.1 Halloween: Bí ma, Dơi, Ma trơi](#111-halloween-bí-ma-dơi-ma-trơi)
  - [11.2 Trung thu: Khỉ đột, Nguyệt thần, Nhật thần](#112-trung-thu-khỉ-đột-nguyệt-thần-nhật-thần)
  - [11.3 Hùng Vương: Thủy Tinh, Sơn Tinh](#113-hùng-vương-thủy-tinh-sơn-tinh)
  - [11.4 Giáng sinh: Ông già Noel](#114-giáng-sinh-ông-già-noel)
  - [11.5 Tết: Lân con](#115-tết-lân-con)
- [12. Ghi chú / điểm cần lưu ý](#12-ghi-chú--điểm-cần-lưu-ý)

---

## Bảng tổng hợp nhanh

| Nhóm | Boss | HP | Dame | Map (id – tên) | Xuất hiện / hồi sinh | Manager |
|---|---|---|---|---|---|---|
| Doanh trại | Trung uý Trắng | `totalDame × 50` | `totalHp / 20` | 59 – Tường thành 3 | 1 lần khi mở doanh trại | RedRibbonHQManager |
| Doanh trại | Trung uý Xanh Lơ | ×1,1 | ×1,1 | 62 – Trại độc nhãn 3 | như trên | như trên |
| Doanh trại | Trung uý Thép | ×1,15 | ×1,15 | 55 – Tầng 1 | như trên | như trên |
| Doanh trại | Ninja Áo Tím (+ phân thân) | ×1,2 (phân thân /10) | ×1,2 (phân thân /10) | 54 – Tầng 3 | như trên | như trên |
| Doanh trại | Rôbốt Vệ Sĩ 00–03 | ×1,3 | ×1,3 | 57 – Tầng 4 | như trên (4 con) | như trên |
| BĐKB | Trung úy Xanh Lơ | 20.000.000 × level | 200.000 × level | 137 – Động kho báu | 1 lần khi mở BĐKB | TreasureUnderSeaManager |
| Khí gas | Dr Lychee | 1.000.000 + 15.000.000 × level | 10.000 + 1.000 × level | 148 – Lâu đài Lychee | khi toàn bộ quái chết | GasDestroyManager |
| Khí gas | Hatchiyack | Dr Lychee × 1,5 | Dr Lychee × 1,5 | 148 | khi Dr Lychee rời map | GasDestroyManager |
| CĐRĐ | Số 1…6 (Saibamen) | 500.000 + 2.000.000 × level | 10.000 + 200.000 × level | 144 – Hoang mạc | khi mở CĐRĐ | SnakeWayManager |
| CĐRĐ | Nađíc | 500.000 + 10.000.000 × level | 10.000 + 1.000.000 × level | 144 | như trên | như trên |
| CĐRĐ | Cađích | 500.000 + 100.000.000 × level | 10.000 + 10.000.000 × level | 144 | như trên | như trên |
| Siêu thần thủy | Ma vương Pôcôlô | HP người chơi × 5 | dame người chơi | khu siêu thần thủy | khi hết quái | OtherBossManager |
| ĐHVT 23 | 12 đối thủ | 10.000 → 150.000.000 | 1.000 → 50.000 | khu thi đấu của người chơi | theo vòng | OtherBossManager |
| Võ đài Hạt Mít | 5 đối thủ | 111%–115% HP người chơi | 1.000–3.000 | khu thi đấu | theo vòng | OtherBossManager |
| Siêu hạng | Rival | chỉ số đối thủ | chỉ số đối thủ | khu thi đấu | khi thách đấu | OtherBossManager |
| Nhân bản | Bản sao Commeson | HP người chơi × 10 | dame × 10 | khu người chơi | NPC Potage | BossManager |
| Luyện tập | Karin / Yajirô / Mr.PôPô | 500 / 1.100 / 5.100 | 500 / 1.100 / 1.100 | 46 – Tháp Karin | khi người chơi gọi | OtherBossManager |
| Luyện tập | Thượng đế | 1.000 | 10.000 | 49 – Phòng tập thời gian | như trên | như trên |
| Luyện tập | Khỉ Bubbles / Thần Vũ Trụ | 30.000 / 45.000 | 30.000 / 45.000 | 48 – Hành tinh Kaio | như trên | như trên |
| Luyện tập | Tổ sư Kaio | 45.000 (bất tử) | 45.000 | 50 – Thánh địa Kaio | như trên | như trên |
| Luyện tập | Whis | 550.000 × lv | 10.000 × lv | 154 – Hành tinh Bill | như trên | như trên |
| Halloween | Bí ma / Dơi / Ma trơi | 500.000 | theo HP người chơi | map thường lớn | nghỉ 10 phút (sự kiện tắt) | HalloweenEventManager |
| Trung thu | Khỉ đột | 100.000.000 | 100.000 | 0–20 | nghỉ 15 phút (tắt) | TrungThuEventManager |
| Trung thu | Nguyệt thần + Nhật thần | 50.000.000 | theo HP người chơi | 0–20 | nghỉ 15 phút (tắt) | TrungThuEventManager |
| Hùng Vương | Thủy Tinh + Sơn Tinh | 50.000.000 | theo HP người chơi | map thường lớn | nghỉ 15 phút (bật nhưng không chạy) | HungVuongEventManager |
| Noel | Ông già Noel | 500 (bất tử) | 5.000.000 | map thường lớn | nghỉ 1 phút (tắt) | ChristmasEventManager |
| Tết | Lân con | 5.000.000 | 5.000 | map thường lớn | nghỉ 1 phút (tắt) | LunarNewYearEventManager |

"Map thường lớn" = 0–20, 24–37, 63–77, 79–84, 92–94, 96–100, 102–110 (xem file 11).

---

## 1. Doanh trại Độc Nhãn (RedRibbonHQ)

Nguồn: `map/phoban/RedRibbonHQ.java` (`init()`), `boss/doanh_trai/*.java`. Tất cả `BossType.PHOBANDT`, manager `RedRibbonHQManager` (tick 150ms). Thời gian phó bản `TIME_DOANH_TRAI = 1.800.000ms` (30 phút).

### 1.1 Công thức chỉ số

Khi khởi tạo doanh trại cho clan:

- `totalDamage = Σ nPoint.dame`, `totalHp = Σ nPoint.hpMax` của các thành viên clan đang trong phó bản (`clan.membersInGame`).
- `dame = totalHp / 20`, `hp = totalDamage × 50`.
- Mỗi boss nhân hệ số, dame tối đa 200.000.000, HP tối đa 2.147.483.647.

| Boss | ID | Map | Hệ số | Tọa độ vào |
|---|---|---|---|---|
| Trung uý Trắng | -4 | 59 Tường thành 3 | ×1,0 | (198, 456) |
| Trung uý Xanh Lơ | -6 | 62 Trại độc nhãn 3 | ×1,1 | (1210, 384) |
| Trung uý Thép | -5 | 55 Tầng 1 | ×1,15 | (884, 312) |
| Ninja Áo Tím | -7 | 54 Tầng 3 | ×1,2 | (190, 312) |
| Rôbốt Vệ Sĩ 00, 01, 02, 03 | -8, -9, -10, -11 | 57 Tầng 4 | ×1,3 (mỗi con) | (300, 312) |

Skill chung (Trái Đất): Demon 3, Demon 6, Dragon 7, Dragon 1, Galick 5, Kamejoko 7…1, Antomic 1…7, Masenko 1, 5, 6, Kamejoko 7 (hồi 1s; các skill khác hồi 1–23ms). Trung uý Thép không có dãy Kamejoko 1–7 (bị comment) — chỉ Kamejoko 7 (1s). Trung uý Xanh Lơ thêm Thái Dương Hạ San 7 (60s). BossData `secondsRest = 60` nhưng boss bị hủy khi rời map (`removeBoss` + `dispose`) nên không hồi sinh.

Tất cả: chết → thưởng (nếu có người kết liễu) → `DIE` → rời map, không thông báo xuất hiện (map phó bản).

### 1.2 Trung uý Trắng

- textM: "Xem mi dùng cách nào hạ được ta", "Ha ha ha", "Bulon đâu tiêu diệt hết bọn chúng cho ta".
- `doneChatS`: đặt `zone.isTUTAlive = true`; `leaveMap`: `isTUTAlive = false`.
- **Chỉ nhận sát thương khi cả 2 Bulon đã chết** (`!zone.isbulon1Alive && !zone.isbulon2Alive`), ngược lại nhận 0.
- Né 20%; `(dame/2) − def`; khiên chia 2.
- Chỉ đánh người chơi đứng trong x ∈ [755, 1060]; nếu boss ở x < 775 thì dịch chuyển tới mục tiêu.
- Phần thưởng:

| Nội dung | Tỉ lệ | SL | Người nhận |
|---|---|---|---|
| +5 Point | 100% | — | người kết liễu |
| Ngọc Rồng 4 sao (17) | 50% | 1 | rơi đất, chủ người kết liễu |
| Kiểm tra nhiệm vụ giết boss | 100% | — | người kết liễu |
| Cậu Vàng (1824) | 100% | 1 | rơi đất, chủ người kết liễu |

### 1.3 Trung uý Xanh Lơ (doanh trại)

- textM: "Xem các ngươi mạnh đến đâu", "He he he".
- `doneChatS → AFK`; `afk()` chuyển `ACTIVE` khi có người chơi trong **500px**.
- Né 20%; `(dame/2) − def`; khiên chia 2.
- Phần thưởng: +5 Point; 50% Ngọc Rồng 4 sao (17); **100% Bản đồ kho báu (611) ×1–3**; 100% Cậu Vàng (1824).

### 1.4 Trung uý Thép

- textM: "Nếu bọn mi muốn lên tiếp tầng lầu trên", "Phải bước qua xác chết của ta đã".
- Chỉ tuần tra/đánh trong x ∈ [640, 980] (mục tiêu ngoài vùng → quay về (884, 312) mỗi 1,5s); không đánh mục tiêu có y < 220.
- Né 20%; `(dame/2) − def`; khiên chia 2.
- Phần thưởng: +5 Point; 50% Ngọc Rồng 4 sao; **30% Bản đồ kho báu ×1–2**; 100% Cậu Vàng.

### 1.5 Ninja Áo Tím + phân thân

- textM: "Ta sẽ xé xác ngươi ra thành trăm mảnh", "Ha ha ha".
- Né **30%**; `(dame/2) − def`; khiên chia 2.
- **Phân thân:** lần đầu HP ≤ 50% (`calledNinja = false`): đòn đó nhận 0; 80% tạo 4 phân thân `NinjaClone` (ID -9…-12), và thêm 50% tạo tiếp 2 (ID -13, -14). Phân thân: dame = dame Ninja / 10, HP = hpMax / 10, xuất hiện ±200px quanh Ninja, né 20%, cùng skill (không có Kamejoko 7 1s cuối).
- Phần thưởng Ninja Áo Tím: +5 Point; 50% Ngọc Rồng 4 sao; 30% Bản đồ kho báu ×1–2; 100% Cậu Vàng.
- Phần thưởng mỗi phân thân: +5 Point; 10% Ngọc Rồng 4 sao.

### 1.6 Rôbốt Vệ Sĩ

- 4 con tên "Rôbốt Vệ Sĩ 00" … "03", ID `ROBOT_VE_SI − i`.
- `doneChatS → AFK`; `afk()`: khi có mục tiêu thì dịch chuyển tới (x ± 100, y = 0) và `ACTIVE`.
- Né theo `tlNeDon/1000`; `(dame/2) − def`; khiên chia 2.
- Phần thưởng: +5 Point; 30% Ngọc Rồng 4 sao; 100% Cậu Vàng.

---

## 2. Bản đồ kho báu (BanDoKhoBau)

Nguồn: `map/phoban/BanDoKhoBau.java`, `boss/ban_do_kho_bau/TrungUyXanhLo.java`. `BossType.PHOBANBDKB`. Thời gian phó bản 30 phút.

| Thuộc tính | Giá trị |
|---|---|
| Tên / ID | Trung úy Xanh Lơ / -6 |
| Hành tinh | Trái Đất |
| HP | `min(20.000.000 × level, 2.000.000.000)` |
| Dame | `min(200.000 × level, 200.000.000)` |
| Skill | Demon cấp 1–7 |
| Map | tạo ở khu map **137 Động kho báu** (dữ liệu khai báo 103 nhưng không dùng); vào tọa độ (198, 456) |
| Thoại | textM: "Các ngươi tới số rồi mới gặp phải ta", "He he he", "Xem các ngươi mạnh đến đâu" |

- `doneChatS → AFK`; `afk()` chuyển `ACTIVE` khi người chơi trong **200px**.
- Không override `injured` → nhận sát thương thô (né theo `tlNeDon`).
- Phần thưởng: +5 Point; **100% Bí ngô 4 sao (705)** ("Thu thập để ước Rồng Xương"), rơi cho người kết liễu.
- Rời map → xóa khỏi manager.

---

## 3. Khí gas hủy diệt (DestronGas)

Nguồn: `map/phoban/DestronGas.java`, `boss/khi_gas/DrLychee.java`, `Hatchiyack.java`. `BossType.PHOBANKGHD`, thời gian 30 phút.

| Thuộc tính | Dr Lychee | Hatchiyack |
|---|---|---|
| ID | -208 | -207 |
| Tên | "Dr Lychee" | " " (khoảng trắng) |
| Hành tinh | Trái Đất | Trái Đất |
| HP | `1.000.000 + min(15.000.000 × level, 2.000.000.000)` | `min(hpMax Dr Lychee × 1,5, 2.000.000.000)` |
| Dame | `10.000 + min(1.000 × level, 200.000.000)` | `min(dame Dr Lychee × 1,5, 200.000.000)` |
| Skill | Demon 1–7 | Demon 1–7 |
| Map | 148 Lâu đài Lychee (480, 295 → 480) | 148 |
| Xuất hiện | khi **toàn bộ quái** trong phó bản chết (1 lần) | khi Dr Lychee rời map (sau chết) |
| textS | "Ta đợi các ngươi mãi", "Bọn xayda các ngươi mau đền tội đi" | "Các ngươi dám hạ sư phụ ta", "Ta sẽ tiêu diệt hết các ngươi" |
| textM | "Đại bác báo thù...", "Heyyyyyyyy Yaaaaa" | như Dr Lychee |
| textE | "Các ngươi khá lắm", "Hatchiyack sẽ báo thù cho ta" | "Các ngươi khó mà rời khỏi nơi đây" |

**Nhận sát thương:**

| | Dr Lychee | Hatchiyack |
|---|---|---|
| Né | `level / 1000` | `(level + 10) / 1000` |
| Cầm NRNM | nhận 1 | nhận 1 |
| Trừ | `(dame + rand(−100×level..0)) − def`, rồi giảm `level/10` % | `(dame + rand(−200×level..0)) − def`, rồi giảm `level/5` % |
| Khiên | → 1 | → 1 |

**Phần thưởng** — rơi tự do (chủ `-1`, ai cũng nhặt được), số lượng = `1 + 2 × số người chơi trong khu` (1 ở giữa, mỗi người thêm 2 cái cách ±50px):

| | Dr Lychee | Hatchiyack |
|---|---|---|
| Item | Cải trang Dr Lychee (738) | Cải trang Hatchiyack (729) |
| Sức đánh +% (50) | ParamMax + 8…11 | ParamMax + 8…11 |
| HP +% (77) | ParamMax + 8…11 | ParamMax + 8…11 |
| KI +% (103) | ParamMax + 8…11 | ParamMax + 8…11 |
| Giảm % sát thương (94) | ParamMax + 0…3 | — |
| +% sức đánh chí mạng (5) | — | ParamMax + 0…3 |
| Hạn sử dụng (93) | `min(rand(3..ParamMax), 21)` ngày | như trên |
| Không thể giao dịch (30) | có | có |

`ParamMax`: level 0–9 → 14; level ≤ 110 → `14 + level/10`; level > 110 → 26.

- Hatchiyack rời map → `clan.KhiGasHuyDiet.hatchiyatchDead = true` → phó bản thông báo "Nơi này sắp nổ tung mau chạy đi" và kick người chơi sau 60 giây.

---

## 4. Con đường rắn độc (SnakeWay)

Nguồn: `map/phoban/SnakeWay.java`, `boss_con_duong_ran_doc/SAIBAMEN.java`, `NADIC.java`, `CADICH.java`. `BossType.PHOBANCDRD`, thời gian 30 phút, tất cả ở map **144 Hoang mạc**.

### 4.1 Công thức chỉ số (khởi tạo)

```
bossDamage = 200000 * level;  bossMaxHealth = 2000000 * level;   // cap 200M / 2B
6 Saibamen (i = 6..1):   dame = 10000 + bossDamage,  hp = 500000 + bossMaxHealth
bossDamage *= 5; bossMaxHealth *= 5 (cap) → Nađíc: dame = 10000 + ..., hp = 500000 + ...
bossDamage *= 10; bossMaxHealth *= 10 (cap) → Cađích: dame = 10000 + ..., hp = 500000 + ...
```

| Boss | ID | Hành tinh | Skill | Tọa độ |
|---|---|---|---|---|
| Số 1 … Số 6 (Saibamen) | `SAIBAMEN − i` = -18 … -23 | Xayda | Galick 1–7 | (420 + i×15, 342) |
| Nađíc | -16 | Xayda | Galick 7 (1s); Tái tạo năng lượng 5 (10s) | (470, 312) |
| Cađích | -15 | Xayda | Galick 7 (1s); Masenko 1 (1s); Antomic 1 (1s); Kamejoko 4 (1s); Biến hình 1 (1s) | (490, 312) |

Tất cả vào map ở trạng thái `AFK`. Nếu clan/phó bản mất → `leaveMap`.

### 4.2 Thứ tự kích hoạt

- **Số 1:** khi có người chơi trong khu → `PK_ALL`, ngủ 1,5 giây (`Functions.sleep` trong thread manager), `ACTIVE`.
- **Số i (2…6):** `ACTIVE` khi `9 − số boss còn sống == i` (tức lần lượt sau khi số trước chết).
- **Nađíc:** `ACTIVE` khi số boss còn sống < 3 (6 Saibamen đã chết). Mỗi đòn 5% (cách ≥ 10s) dùng Tái tạo năng lượng, chat "Ốp la...Xay da da!". Chết → Cađích di chuyển tới gần; textE "Sếp hãy giết nó, trả thù cho em!".
- **Cađích:** chuyển `CHAT_S` ("Vĩnh biệt chú mày nhé, Na đíc") khi số boss còn sống < 2 (Nađíc đã chết).

### 4.3 Saibamen (Số 1…6)

- Né `tlNeDon/1000`; `(dame/7) − def`; khiên chia 4.
- **Tự phát nổ khi chết:** thông báo "<tên> coi chừng đấy!" tới mọi người, kẻ kết liễu chat "Trời ơi muộn mất rồi" và **bị choáng 3,5 giây**, boss chat "He he he"; sau 2,5 giây gây sát thương `hpMax × 100` (không xuyên giáp) lên **mọi người chơi** trong khu.
- Phần thưởng: **100% Ngọc Rồng 6 sao (19)** cho người kết liễu.

### 4.4 Nađíc

- Không override `injured` (sát thương thô).
- Phần thưởng: **100% Ngọc Rồng 6 sao (19)**.

### 4.5 Cađích

- Không bị chặn giáp; né `tlNeDon/1000`; khiên chia 4.
- **Biến khỉ:** khi HP < 50% (1 lần): chat "Ha ha ha, ha ha ha", **bất tử 2 giây** (gồng), chat "Thế nào <tên>? Mi đã thấy phép biến hình của người Xayda rồi chứ?", trở thành khỉ cấp 1 trong 100 giây, `hpMax × 2` (tối đa 2 tỉ) và **hồi đầy HP**.
- Mỗi đòn 5% (cách ≥ 10s): **choáng mục tiêu 5 giây**, chat "Tuyệt chiêu hủy diệt của môn phái Xayda". Đòn thường chọn ngẫu nhiên Galick/Masenko/Antomic; Kamejoko (skill index 3) được ép dùng khi đã qua 3 giây kể từ mốc `lastTimeSkillHD` — sau khi dùng mốc bị đẩy rất xa, chỉ được đặt lại mỗi lần tung đòn choáng.
- Đòn chí mạng: xóa hiệu ứng, `NON_PK`, `die()` (HP không bị trừ) → textE "Tốt lắm phi thuyền đã đến đón ta" → rời map bằng phi thuyền, đặt `endCDRD = true` (phó bản kick người chơi sau 60 giây).
- Phần thưởng (50%, người kết liễu):

| Item | Option |
|---|---|
| Phiếu giảm giá (459) ×1 | Giảm 80% khi mua Avatar hoặc Cải trang; Hạn sử dụng 90 ngày; PIN random 0–9999 |
| Bí ngô 5 sao (706) ×1 | — |

---

## 5. Siêu thần thủy — Ma vương Pôcôlô

Nguồn: `boss/ma_vuong_picolo/Pocolo.java`, `services_dungeon/SuperDivineWaterService.java`. `BossType.PHOBAN` (OtherBossManager).

| Thuộc tính | Giá trị |
|---|---|
| Tên / ID | Ma vương Pôcôlô / -340 |
| Hành tinh | Namếc |
| HP | `min(hpMax người chơi × 5, 2.147.483.647)` |
| Dame | `min(dame người chơi, 200.000.000)` |
| Skill | Demon 7; Masenko 1–7; Kamejoko 1–7; Antomic 1–7 (1s); Tái tạo năng lượng 7 (15s) |
| Map | khu Siêu thần thủy của người chơi (dữ liệu: 146 Tây Karin) |
| Thoại | textS "Được! Mi muốn chết thì ta cho chết!"; textM "Khí công pháo"; textE "Hâyaaaa" |

- **Điều kiện xuất hiện:** người chơi đang ở map siêu thần thủy đúng khu của mình, chưa gọi Pôcôlô, và toàn bộ quái trong khu chết.
- Vào map (820, 36 → 336), KI tối đa, `AFK`:
  - Sau 3s: dùng Tái tạo năng lượng, chat "Hồi sinh đi các con của ta."
  - Sau 5s: `SuperDivineWaterService.init` **hồi sinh quái**, chat "Các con của ta hãy tiêu diệt nó."
  - Khi quái chết hết: dịch chuyển tới người chơi, `CHAT_S` → đánh.
- NPC MC chat "Không xong rồi, không xong rồi", "Nguy to cho thằng nhóc rồi" khi boss `ACTIVE`.
- **Nhận sát thương:** né 1%; cầm NRNM → 1; khiên → 1; **đòn chí mạng → nhận 0 và kích hoạt Laze**.
- **Laze:** khi bị đánh chí mạng hoặc khi đòn của boss đủ giết người chơi: chat "Xem đây", hiệu ứng skill 83 trong 3 giây; sau 3 giây: người chơi nhận hiệu ứng "chết" (1.000.000.000 sát thương hiển thị), `winSTT = true`, `lastTimeWinSTT = now`, hiệu ứng PK siêu thần thủy 60 giây, boss `DIE`.
- `reward()` rỗng; phần thưởng thực tế nhận qua menu Mèo Karin sau khi `winSTT` (service hồi sinh người chơi và mở menu "Để tôi đưa cậu về").
- Rời map sau 15 phút; rời bằng phi thuyền, xóa khỏi manager.

---

## 6. Đại hội võ thuật lần 23

Nguồn: `boss/dai_hoi_vo_thuat/*.java`, `matches/dai_hoi_vo_thuat/The23rdMartialArtCongress.java`. Boss `BossType.PHOBAN` (OtherBossManager), khởi tạo ở trạng thái `RESPAWN`.

### 6.1 Danh sách vòng đấu

Tất cả hành tinh Trái Đất, skill Kamejoko 7 (5s) + Galick 7 (1s), `secondsRest` 5 (không dùng).

| Vòng (`round`) | Boss | ID | HP | Dame | Đặc biệt |
|---|---|---|---|---|---|
| 0 | Sói hẹc quyn | -77 | 10.000 | 1.000 | — |
| 1 | Ở dơ | -78 | 25.000 | 3.000 | — |
| 2 | Xinbatô | -79 | 50.000 | 6.000 | — |
| 3 | Cha pa | -80 | 100.000 | 9.000 | — |
| 4 | Pon put | -81 | 250.000 | 10.000 | — |
| 5 | Chan xư | -82 | 500.000 | 10.000 | Choáng người chơi |
| 6 | Tàu Pảy Pảy | -83 | 2.000.000 | 15.000 | — |
| 7 | Yamcha | -84 | 5.000.000 | 15.000 | — |
| 8 | Jacky Chun | -85 | 25.000.000 | 20.000 | — |
| 9 | Thiên xin hăng | -86 | 75.000.000 | 22.000 | Phân thân |
| 10 | Liu Liu | -87 | 150.000.000 | 30.000 | — |
| 11 | PôCôLô | -92 | 100.000.000 | 50.000 | — |
| 12 | — | — | — | — | Vô địch (`champion`) |

### 6.2 Diễn biến trận

- Mỗi vòng: người chơi đặt tại (335, 264), boss vào khu của người chơi tại (435, 264), đếm ngược 13 tick (1 tick/giây):
  - 13: vòng 4/6/8/10 hồi chiêu toàn bộ skill; cả hai choáng 14s; người chơi hồi đầy HP/KI.
  - 11 → 3: NPC giới thiệu ("Trận đấu giữa … sắp diễn ra", "Xin quý vị khán giả…", "Trận đấu bắt đầu").
  - 1: bật PK PVP, boss `ACTIVE`, thời gian **180 giây**.
- Boss chỉ đánh `playerAtt`; nếu người chơi rời khu → boss rời map. KI boss bất tận.
- Thắng vòng khi boss chết → `round++`, boss rời map, vòng mới, cập nhật thưởng.
- Thua khi: người chơi chết, hết giờ ("Hết thời gian thi đấu"), rơi khỏi võ đài (y > 264 và x ngoài 150–630).
- **Chan xư:** sau 10 giây đầu, mỗi đòn 20% (cách ≥ 10s) choáng người chơi 1–10 giây ("Đứng hình" / "Nhất dương chỉ"); khi người chơi đang choáng boss chí mạng 100%.
- **Thiên xin hăng:** tự gỡ choáng mỗi tick; mỗi 30 giây tạo **4 phân thân** (ID -88…-91, HP 20.000.000, dame 5.000) sống 10 giây, vào trận ngay ở trạng thái `ACTIVE`.
- Boss `die()` chỉ `DIE` — **không có phần thưởng từ boss**.
- **Phần thưởng trận:** `player.levelWoodChest = max(levelWoodChest, round)` (cấp Rương gỗ theo vòng cao nhất đạt được), nhận ở NPC khác.

---

## 7. Võ đài Hạt Mít (Sinh tử)

Nguồn: `boss/vo_dai_hat_mit/*.java`, `matches/dai_hoi_vo_thuat/DeathOrAliveArena.java`. `BossType.PHOBAN`.

| Vòng | Boss | ID | HP (vào map) | Dame | Skill |
|---|---|---|---|---|---|
| 0 | Đracula | -93 | 111% hpMax người chơi | 1.000 | Tái tạo năng lượng 1 (60s) |
| 1 | Người vô hình | -94 | 112% | 1.000 | như trên |
| 2 | Bông băng | -95 | 113% | 2.000 | như trên |
| 3 | Vua Quỷ Sa tăng | -96 | 114% | 2.000 | như trên |
| 4 | Thỏ Đầu Bạc | -97 | 115% | 3.000 | như trên |
| 5 | — | — | — | — | Vô địch |

- HP dữ liệu gốc 5.000–30.000, map 112 Võ Đài Hạt Mít; khi vào map ghi đè `hpMax = hpMax người chơi / 100 × (100 + |id| − 82)`. Hành tinh Xayda. textM: "He he he", "Ta sẽ xé xác ngươi ra thành trăm mảnh", "Xem các ngươi mạnh đến đâu".
- Vào (523, 336); người chơi đặt (401, 336); đếm 5 giây, NPC "Khá lắm, chuẩn bị đánh tiếp nào", boss "Sẵn sàng chưa?"; mỗi vòng 180 giây.
- **Nhận sát thương (tất cả):** né 10%; cầm NRNM → 1; **tối đa hpMax/10 mỗi đòn** (tối thiểu 10 đòn).
- **Đracula:** mỗi 15 giây (khi HP boss > hpMax/30) hút 10% hpMax người chơi cộng vào HP boss, chat "Máu ngon quá hehe".
- **Người vô hình:** mỗi 15 giây tàng hình 5 giây (đứng ở y = 10.000 gần người chơi), sau đó lao tới người chơi.
- Thua: chết, hết giờ, rơi khỏi võ đài (y > 336 và x ngoài 322–614).
- **Phần thưởng:** hạ đủ 5 boss → lưu thời gian nhanh nhất `timePKVDST`, `haveRewardVDST = true` (nhận thưởng ở NPC Bà Hạt Mít). Mỗi vòng thắng/thua chia vàng cược cho người bình chọn: `(cược Hạt Mít + cược người chơi) × 900.000 / tổng cược bên thắng × số phiếu của mỗi người`.

---

## 8. Giải siêu hạng — bản sao đối thủ (Rival)

Nguồn: `boss/sieu_hang/Rival.java`, `SuperRank.java`; `matches/dai_hoi_vo_thuat/SuperRank.java`. `BossType.PHOBAN`.

| Thuộc tính | Giá trị |
|---|---|
| ID | `-rival.id` |
| Tên, giới tính, ngoại hình | copy người chơi đối thủ |
| HP / Dame | `hpg` / `dameg` đối thủ; khi vào map **dùng chung `nPoint`, đồ, đệ tử, hiệu ứng, set đồ, nội tại, cải trang… của đối thủ** (load từ DB) và hồi đầy HP/KI |
| Skill | toàn bộ skill đối thủ có điểm > 0, trừ Tự phát nổ, Quả cầu kênh khi, Makankosappo, Trị thương |
| Map | khu thi đấu (dữ liệu 113) |

- Trận: 5 giây chuẩn bị (người chơi (334, 264), Rival (434, 264), "sẵn sàng"), bật PK PVP, Rival `ACTIVE`; thời gian 180 giây.
- AI: 5% di chuyển tới người chơi; KI luôn đầy; mỗi 5 giây ăn đậu nếu thiếu máu; tránh dùng Khiên/Thái Dương Hạ San/Thôi miên/Trói khi người chơi đang dính hiệu ứng; tránh khống chế khi đang có khiên và HP > 50%.
- **Nhận sát thương:** công thức PvP đầy đủ — miễn nhiễm 3s sau Ma phong ba; nếu `islinhthuydanhbac` (đã nạp lần đầu) thì không thể tấn công; vô hiệu chưởng (`voHieuChuong`); né đòn trừ chính xác, tối đa 90%; giáp trừ xuyên giáp chưởng/cận chiến, tối đa 86%; trừ `def`; giáp Xên (÷2 hoặc 40%); khiên → 1.
- Thắng khi Rival chết/rời khu; thua khi người chơi chết/rời khu/hết giờ. Kết quả cập nhật hạng qua `updateResults`. Rival rời map chat "Mạnh quá, ta chịu thua" (người chơi thắng) hoặc "Đầu hàng chưa?".
- Rival `die()` chỉ `DIE`, không có phần thưởng item.

---

## 9. Nhân bản Commeson

Nguồn: `boss/nhan_ban/NhanBan.java`, `services/Service.java` (`callNhanBan`), `npc_list/Potage.java`. Boss không có type → `BossManager`.

| Thuộc tính | Giá trị |
|---|---|
| ID | `-player.id − 1.000.000.000` |
| Tên, giới tính, ngoại hình | copy người chơi gọi |
| HP | `hpMax người chơi × 10` (tối đa int) |
| Dame | `dame người chơi × 10` |
| Skill | toàn bộ skill có điểm > 0 của người chơi, trừ Tự phát nổ và Trói |
| Map | khu hiện tại của người chơi (dữ liệu 140 Hang động Potaufeu), ±200px |
| Thoại | textS (người chơi) "Boss nhân bản đã xuất hiện rồi"; textM "Ta sẽ thay thế ngươi, haha"; textE "Lần khác ta sẽ xử đẹp ngươi" |

- Gọi qua NPC Potage → `callNhanBan`; người chơi nhận hiệu ứng `PKCommeson` 300 giây.
- Cả hai chuyển PK PVP; boss chỉ đánh người chơi gọi.
- Hiệu ứng `isPKCommeson` hết → "Bạn đã thất bại, ngày mai hãy thử sức tiếp", boss rời map.
- Phần thưởng: **Bình chứa Commeson (638)** ×1 cho người kết liễu, options Hạn sử dụng 30 ngày, Không thể giao dịch; thông báo server "<tên> đã đánh bại bản sao Commeson, mọi người đều ngưỡng mộ".

---

## 10. Luyện tập tự động / thách đấu sư phụ

Nguồn: `boss/luyen_tap_tu_dong/*.java`, `services_dungeon/TrainingService.java` (`callBoss`). `BossType.PHOBAN`, khởi tạo trạng thái `RESPAWN`, chỉ đánh người chơi gọi. Khi gọi, NPC tương ứng bị ẩn với người chơi; khi boss rời map NPC hiện lại (`luyenTapEnd`).

### 10.1 Bảng boss

| Boss | ID | Hành tinh | HP | Dame | Skill | Map | Tọa độ vào |
|---|---|---|---|---|---|---|---|
| Karin | -357 | Xayda | 500 | 500 | Galick 1 (1s); Tái tạo NL 1 (60s) | 46 Tháp Karin | (420, 408) |
| Tàu Pảy Pảy | -309 | Xayda | 1.000 | 500 | Galick 1 (1s); Tái tạo NL 1 (60s) | 46 | phi thuyền x = 775 |
| Yajirô | -358 | Xayda | 1.100 | 1.100 | Galick 1 (1s); Tái tạo NL 1 (60s) | 46 | (320, 408) |
| Mr.PôPô | -359 | Xayda | 5.100 | 1.100 | Galick 1 (30s); Kamejoko 1 (30s); Thái Dương Hạ San 3 (30s) | 46 | (295, 408) |
| Thượng đế | -360 | Xayda | 1.000 | 10.000 | Dragon 1 (1s) | 49 Phòng tập thời gian | (408, 436) |
| Khỉ Bubbles | -361 | Trái Đất | 30.000 | 30.000 | Galick 7 (1s); Biến hình 7 (1800s) | 48 Hành tinh Kaio | (420, 408) |
| Thần Vũ Trụ | -362 | Xayda | 45.000 | 45.000 | Galick 7 (1s); Thái Dương Hạ San 7 (30s) | 48 | (420, 240) |
| Tổ sư Kaio | -363 | Xayda | 45.000 | 45.000 | Galick 1 (60s) | 50 Thánh địa Kaio | vị trí người chơi |
| Whis | -364 | Trái Đất | 550.000 × lv | 10.000 × lv | Dragon 7 (0,5s) | 154 Hành tinh Bill | (725, 312) |

Thoại textS chọn theo chế độ (`isThachDau ? 1 : 0`):

| Boss | Luyện tập (0) | Thách đấu (1) | textE |
|---|---|---|---|
| Karin / Tàu Pảy Pảy | "Ta sẽ dạy ngươi vài chiêu" | "Ta sẽ đánh hết sức, ngươi cẩn thận nhé" | "OK ta chịu thua" |
| Yajirô | "Ngon nhào vô" | "Cho mi biết sự lợi hại của ta" | "Ngươi thật lợi hại" |
| Mr.PôPô | "Đánh trúng ta 1 cái coi như ngươi thắng" | "Đánh trúng ta 3 cái coi như ngươi thắng" | "Thua thì thua" |
| Thượng đế | "Ta sẽ dạy võ cho con trong phòng tập thời gian này" | "Con hãy đánh hết sức nhé, ta sẽ không nương tay đâu" | "Ta rất tự hào về con" |
| Khỉ Bubbles | "Ù ù khẹt khẹt" | "Ù ù khẹt khẹt" | "Éc Éc Éc Éc!" |
| Thần Vũ Trụ | "Ta sẽ dạy ngươi chiêu kaio-ken" | "Ngươi cũng to gan lắm" | "Tại hôm nay ta...ta hơi bị đau bụng" |
| Whis | "Ta sẽ dạy ngươi vài chiêu" | — | random: "OK ta chịu thua" / "Ta rất tự hào về con" / "Tại hôm nay ta...ta hơi bị đau bụng" / "Thua thì thua" |

textM chung: "Haizzzzz", "Hahaha", "Xem đây".

### 10.2 Cơ chế chung (TrainingBoss)

- `active`: lần đầu bật PK và gửi trạng thái đối kháng (`sendPVB`).
- Mỗi 30 giây **ăn đậu**: +20% hpMax, đầy KI (Tàu Pảy Pảy tắt).
- **Nhận sát thương:** né **40%**; cầm NRNM → 1; không trừ giáp; HP < 20% chat "AAAAAAAAA"/"ai da".
- Người chơi chết → boss chat "Luyện tập tiếp đi", `AFK`, `NON_PK`, tắt đối kháng.
- Boss chết → `AFK`, đọc textE, tắt đối kháng; nếu **thách đấu** → `player.levelLuyenTap++`.
- Trong `AFK` sau một khoảng thì `LEAVE_MAP`: Karin 5s (vừa đi theo người chơi), Tàu Pảy Pảy 5s, Thượng đế 5s (nếu người chơi không chết thì đưa về map 45 Thần điện), Thần Vũ Trụ 7s (đi theo), Yajirô / Mr.PôPô / Khỉ Bubbles 15s.
- Rời map: nếu có NPC không tương tác tương ứng trong khu thì boss hồi máu và đi về NPC; ngược lại gửi xóa boss cho client và `luyenTapEnd`.
- Karin, Mr.PôPô có `bayLungTung` (nhảy quanh người chơi mỗi 3–5,5 giây).

### 10.3 Riêng từng boss

- **Tàu Pảy Pảy (luyện tập):** né 40%; **chỉ nhận sát thương thật khi nhiệm vụ hiện tại của người đánh là `TASK_10_1`**, ngược lại trả về 100 (không trừ HP); `dame − def`. Vào/ra bằng phi thuyền. Lưu ý: `services_dungeon.TrainingService.callBoss` **không** có case Tàu Pảy Pảy (chỉ bản cũ `services/TrainingService` có).
- **Tổ sư Kaio:** **bất tử**; mỗi 10 giây cộng TN/SM cho người chơi = `TrainingService.getTnsmMoiPhut(player) / 6`; người chơi rời khu → boss rời map.
- **Whis:** `level = top Whis của người chơi + 1`; HP ×level, dame ×level, tên "Whis [LV:n]". Vào map đặt người chơi (488, 360), boss dịch chuyển (341, 320) và chat. Né 40%; **sát thương nhận chia `level`**; HP < 1/3 chat "ai da". Khi bị hạ: `thachdauwhis++` cho người kết liễu, đánh dấu cập nhật top Whis, lưu người chơi; `traning.setTop(level)`, thời gian hoàn thành, lưu `TraningDAO`.

---

## 11. Boss sự kiện

Nguồn: `boss/event/Halloween/*.java`, `boss/event_trung_thu/*.java`, `boss/event_hung_vuong/*.java`, `boss/event_noel/OngGiaNoel.java`, `boss/event_tet/LanCon.java`, `event/EventManager.java`, `event_list/*.java`.

> Trạng thái hiện tại: chỉ `HungVuong` được `init()`; Halloween, Trung thu, Giáng sinh, Tết bị comment. Ngoài ra 5 manager sự kiện **không có thread chạy**, nên kể cả khi bật sự kiện, các boss này không cập nhật/không xuất hiện (xem Ghi chú).

### 11.1 Halloween: Bí ma, Dơi, Ma trơi

| Thuộc tính | Bí ma | Dơi | Ma trơi |
|---|---|---|---|
| ID | -351 | -350 | -349 |
| Tên khi vào | "Bí ma <10..99>" | "Dơi <10..99>" | "Ma trơi <10..99>" |
| Hành tinh | Xayda | Xayda | Xayda |
| HP | 500.000 | 500.000 | 500.000 |
| Dame | 100 (ghi đè: `hpMax người chơi / rand(30..50)`) | như trên | như trên |
| Skill | Galick 7 (hồi random 5–10s, cố định lúc nạp class) | như trên | như trên |
| Map | map thường lớn | như trên | như trên |
| Hồi sinh | 10 phút | 10 phút | 10 phút |
| Số lượng | 10 | 10 | 10 |
| Nhịp đánh | 0,5s | 0,5–1s | 0,5–1s |
| Hiệu ứng Halloween lên mục tiêu | loại 2, 30 phút | loại 3, 30 phút | loại 4, 30 phút |
| textM | "Khà khà" | "Khà khà" | "Khà khà" |

- Tắt thông báo, khu ≥ 2.
- **Nhận sát thương:** né 1%; `(dame/7) − def`; **tối đa hpMax/50 mỗi đòn** (≥ 50 đòn).
- Phần thưởng: **Bí ngô (585)** ×1 cho người kết liễu.
- `autoLeaveMap` 15 phút không người.

### 11.2 Trung thu: Khỉ đột, Nguyệt thần, Nhật thần

| Thuộc tính | Khỉ đột | Nguyệt thần | Nhật thần |
|---|---|---|---|
| ID | -344 | -345 | -346 |
| Hành tinh | Xayda | Xayda | Xayda |
| HP | 100.000.000 | 50.000.000 | 50.000.000 |
| Dame | 100.000 | 1.000 (ghi đè) | 1.000 (ghi đè) |
| Skill | Galick 7 (1s); Antomic 7 (3s); Biến hình 7 (60s) | Galick 1 (2s); Antomic 1 (6s) | Galick 1 (2s); Antomic 1 (6s) |
| Map | 0–20 | 0–20 | theo Nguyệt thần |
| Hồi sinh | 15 phút | 15 phút | APPEAR_WITH_ANOTHER |
| Số lượng | 10 | 10 (mỗi con kèm 1 Nhật thần) | — |
| Cờ | — | 1 | 2 |

- **Khỉ đột:** né `tlNeDon/1000`; `(dame/7) − def`. Thưởng **Đuôi khỉ (1045)** ×1. `autoLeaveMap` 15 phút không người.
- **Nguyệt thần / Nhật thần:** đánh người chơi khác cờ và boss của cờ đối lập; dame mỗi đòn = `hpMax người chơi / 30` (đánh boss: 10.000). Né `tlNeDon/1000`; đòn không xuyên giáp > 1.000.000 bị thay bằng 900.000–1.000.000.
- **Thưởng chéo:** giết Nguyệt thần → Nhật thần chuyển `AFK` và thả item **2124** cho người kết liễu, chat "Được! hảo hán!", 3 giây sau rời map; giết Nhật thần → Nguyệt thần thả item **2123**. Options: HP +10–20%, KI +10–20%, Sức đánh +10–20%, Giảm 1–10% sát thương, Chí mạng +1–10%, Không thể bán lại, Hạn sử dụng 1–15 ngày. (Item 2123/2124 không có trong `item_template`.)

### 11.3 Hùng Vương: Thủy Tinh, Sơn Tinh

| Thuộc tính | Thủy Tinh | Sơn Tinh |
|---|---|---|
| ID | -355 | -354 |
| Hành tinh | Xayda | Xayda |
| HP / Dame | 50.000.000 / 1.000 (ghi đè `hpMax người chơi / 30`, đánh boss 10.000) | như Thủy Tinh |
| Skill | Galick 1 (2s); Antomic 1 (6s) | như trên |
| Map | map thường lớn | theo Thủy Tinh |
| Hồi sinh | 15 phút, kèm `[SON_TINH]` | APPEAR_WITH_ANOTHER |
| Cờ | 1 | 2 |
| textM | "Trả Mị Nương lại cho ta", "Ta cho nước dâng chìm cả lũ bây giờ" | "Còn lâu á, chậm chân ráng chịu đi cưng", "Ta thách, chiêu này quá quen rồi" |

- Tắt thông báo, spawn khu mặc định. 10 Thủy Tinh (sự kiện `HungVuong`).
- Né `tlNeDon/1000`; đòn > 1.000.000 → 900.000–1.000.000.
- **Thưởng chéo** (+5 Point cho người kết liễu):
  - Giết Thủy Tinh → Sơn Tinh thả **Cải trang (421) — "Cải trang thành Sơn Tinh"**: HP +15–20%, KI +15–20%, Sức đánh +15–20%, Giảm 1–10% sát thương, Biến 1–15% tấn công thành HP, 1/15 thêm Kháng TDHS, Không thể bán lại, Hạn sử dụng 1–15 ngày.
  - Giết Sơn Tinh → Thủy Tinh thả **Cải trang (422) — "Cải trang thành Thủy Tinh"**: HP/KI/Sức đánh +15–20%, Giảm 1–10% sát thương, Chí mạng +2–5%, Không thể bán lại, Hạn sử dụng 1–15 ngày.
  - Boss thả thưởng chat "Được! hảo hán!" rồi rời map sau 3 giây.
- `autoLeaveMap` 15 phút không người.

### 11.4 Giáng sinh: Ông già Noel

| Thuộc tính | Giá trị |
|---|---|
| ID | -353 |
| Hành tinh | Xayda |
| HP / Dame | 500 / 5.000.000 |
| Skill | Tái tạo năng lượng 7 |
| Map | map thường lớn — khu đầu tiên có ≤ 10 người và chưa có Ông già Noel; hết khu → `leaveMapNew` |
| Hồi sinh | 1 phút |
| Số lượng | 30 |
| Thoại | textM "Mé ri chịch mệt", "Hô hô hô", "Giáng sinh vui vẻ!"; textE "Giáng sinh vui vẻ!" |

- **Bất tử** (`injured` luôn 0); không dùng skill tấn công, chỉ đi theo người chơi.
- Khi đứng ≤ 50px cạnh người chơi, mỗi 60 giây chat "Hô hô hô" và rơi **Hộp quà giáng sinh (648)** tự do (chủ -1): cái 1 tỉ lệ 1/3, cái 2 tỉ lệ 1/5, cái 3 tỉ lệ 1/7.
- Rời map sau 100–300 giây kể từ khi vào.

### 11.5 Tết: Lân con

| Thuộc tính | Giá trị |
|---|---|
| ID | `LAN_CON − rand(0..999.999)` (LAN_CON = -371) |
| Hành tinh | Xayda |
| HP / Dame | 5.000.000 / 5.000 |
| Skill | Tái tạo năng lượng 7 |
| Map | map thường lớn — khu đầu tiên ≤ 10 người (kiểm tra trùng theo ID Ông già Noel) |
| Hồi sinh | 1 phút |
| Số lượng | 10 |
| Thoại | textM "Tùng tùng xèng xèng" |

- Rời map sau 100–300 giây kể từ khi vào.
- Di chuyển theo người chơi; ≤ 100px: mỗi 30 giây (khi HP < hpMax) 10% **húc chết** người chơi ("Bạn đã bị Lân con húc chết!").
- **Nhận sát thương:** né 10%; `dame − def`; khiên → 1; tối đa 500.000/đòn.
- **Thu phục:** khi đòn đủ hạ Lân con **hoặc** câu chat gần nhất của người đánh chứa "thang": người đánh chat "Đi thôi lân con!", Lân con `NON_PK`, hồi đầy HP, `AFK` và **đi theo người đó** (đổi map theo; nếu người chơi chuyển map VIP thì dừng theo).
  - Khi người chơi trong 300px: `canReward = true`; xa hơn: `false`.
  - Người chơi mở NPC **Quy Lão Kame** khi `canReward` → menu "Giao Lân con" → `RewardService.rewardLancon`; `haveReward = true` → Lân con rời map.
- Phần thưởng `rewardLancon` (cần 1 ô trống):

| Nội dung | Chi tiết |
|---|---|
| 1 item ngẫu nhiên | Ngọc Thố (734), Gậy như ý (920), Pháo Thăng Thiên (849), Chổi bay Phù Thủy (743), Cân đẩu vân ngũ sắc (733) |
| 5% | 1 option trong {HP+%, HP+%/30s, KI+%/30s, KI+%, Sức đánh+%, Giảm % sát thương, +% sức đánh chí mạng} giá trị 1–5 **và** 1 option trong {Chí mạng+%, Tốc độ di chuyển+%, Né đòn, Tấn công+% khi đánh quái, +HP/30s, +KI/30s, Giáp, Ký gửi ngọc} giá trị 1–2; không hạn sử dụng |
| 95% | 1 option nhóm 1 giá trị 1–10; 10% thêm 1 option nhóm 2 giá trị 1–10; Hạn sử dụng 1–30 ngày |
| Luôn có | "Dùng để bay và phục hồi HP, KI" (89), Không thể giao dịch (30) |

---

## 12. Ghi chú / điểm cần lưu ý

1. **Boss sự kiện không chạy** — `TrungThuEventManager`, `HalloweenEventManager`, `ChristmasEventManager`, `HungVuongEventManager`, `LunarNewYearEventManager` không được start thread trong `ServerManager.run()`. Sự kiện Hùng Vương đang bật tạo 10 Thủy Tinh + 10 Sơn Tinh nhưng chúng đứng ở `REST` mãi.
2. **Item 2123 / 2124** (thưởng Nguyệt thần / Nhật thần) không tồn tại trong `item_template` (bảng dừng ở id 1999) → rơi item lỗi nếu sự kiện Trung thu được bật.
3. **Lân con kiểm tra trùng khu bằng `BossID.ONG_GIA_NOEL`** thay vì ID Lân con → nhiều Lân con có thể vào cùng khu.
4. **Lân con có thể thu phục không cần đánh chết** — chỉ cần chat có chữ "thang" rồi đánh trúng 1 đòn (hàm `isPlayerChatThang`).
5. **Saibamen Số 1 gọi `Functions.sleep(1500)`** và **Cađích gọi `Functions.sleep(2000)`** trong `afk()/attack()` → chặn toàn bộ thread `SnakeWayManager` (mọi CĐRĐ của mọi clan) trong thời gian đó.
6. **Trùng ID** trong doanh trại: Rôbốt Vệ Sĩ `-8 - i` (-8…-11) trùng với `NINJA_AO_TIM1…3` (-9…-11) của phân thân Ninja; ĐHVT Sói hẹc quyn/Ở dơ/Xinbatô dùng -77/-78/-79 trùng boss mini thế giới.
7. **Doanh trại tính chỉ số theo thành viên đang trong phó bản lúc mở** (`membersInGame`) — clan vào ít người/đồ yếu sẽ làm boss yếu; HP dùng tổng dame, dame dùng tổng HP.
8. **Trung uý Trắng phụ thuộc cờ `zone.isbulon1Alive/isbulon2Alive`** — nếu các cờ này không được cập nhật đúng (do quái Bulon), boss sẽ bất tử.
9. **ĐHVT/Võ đài Hạt Mít/Siêu hạng: boss không trao item** — thưởng xử lý ở lớp trận đấu (`levelWoodChest`, `haveRewardVDST`, hạng siêu hạng).
10. **Tàu Pảy Pảy luyện tập** không được gọi bởi `services_dungeon.TrainingService` (bản đang import ở `Controller`), chỉ còn trong `services/TrainingService` cũ.
11. **Whis `injured`** không kiểm tra `isDie()` và điều kiện chết là `damage >= hp` **sau khi** đã trừ HP → có thể gọi `die` lặp hoặc bỏ lỡ khi HP về 0 bằng đòn nhỏ.
12. **Rival (siêu hạng) dùng chung object `nPoint`, `effectSkill`, `inventory`… của người chơi đối thủ** (được load từ DB) và `dispose` đối tượng người chơi khi rời map — cần cẩn thận nếu đối thủ đang online.
13. Pôcôlô (siêu thần thủy) không thể bị hạ bằng sát thương: mọi đòn chí mạng đều kích hoạt Laze và kết thúc trận với `winSTT = true` cho người gọi.
14. Dr Lychee/Hatchiyack là boss duy nhất thả thưởng **tự do cho mọi người** và số lượng phụ thuộc số người chơi trong khu (1 + 2×N).
15. `secondsRest = 60` của boss phó bản/đấu trường không có tác dụng vì boss bị `removeBoss` + `dispose` khi rời map.
