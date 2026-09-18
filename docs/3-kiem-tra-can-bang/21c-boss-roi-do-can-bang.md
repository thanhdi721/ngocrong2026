# 21c — Kiểm toán cân bằng: Boss ↔ Rơi đồ ↔ Độ khó

> **Vấn đề chủ dự án nêu (trọng tâm của file này):**
> *"Logic cũ: boss Xên hoàn thiện rơi đồ rất tốt vì nó khó. Nhưng trong tuyến nhiệm vụ mới, người chơi được dẫn tới giết những boss đó sớm và dễ hơn, mà vẫn rơi đồ tốt — như vậy là phá vỡ cân bằng. Đồ tốt phải gắn với boss khó. Boss của nhiệm vụ thì chỉ nên rơi đồ nhiệm vụ. HP và chỉ số vật phẩm phải hợp lý."*
>
> File này **chỉ kiểm toán và đề xuất**, không sửa code, không sửa file 20*.
> Nguồn số liệu: `SRC/src/nro/models/boss/**`, `SRC/src/nro/models/services/{TaskService,ItemService,RewardService}.java`, `database team2026.sql` (`item_template`, `item_shop`, `item_shop_option`), và các tài liệu [11](../1-he-thong-hien-tai/11-boss.md), [11b](../1-he-thong-hien-tai/11b-boss-su-kien-pho-ban.md), [03](../1-he-thong-hien-tai/03-nhan-vat-chi-so.md), [04](../1-he-thong-hien-tai/04-ky-nang.md), [06](../1-he-thong-hien-tai/06-vat-pham.md), [08](../1-he-thong-hien-tai/08-nang-cap-do-dap-do.md), [09](../1-he-thong-hien-tai/09-quai-roi-do-exp.md), [14](../1-he-thong-hien-tai/14-ban-do-pho-ban.md), [20/20a/20b/20c](../2-thiet-ke-nhiem-vu-moi/20-thiet-ke-nhiem-vu-moi.md).

---

## Mục lục

- [0. Kết luận ngắn (đọc trước)](#0-kết-luận-ngắn-đọc-trước)
- [1. Mô hình tính "sức mạnh người chơi cần có"](#1-mô-hình-tính-sức-mạnh-người-chơi-cần-có)
  - [1.1 Giả định và công thức](#11-giả-định-và-công-thức)
  - [1.2 Bảng hồ sơ người chơi theo mốc sức mạnh](#12-bảng-hồ-sơ-người-chơi-theo-mốc-sức-mạnh)
  - [1.3 Kiểm chứng mô hình](#13-kiểm-chứng-mô-hình)
- [2. Độ khó thật của từng boss](#2-độ-khó-thật-của-từng-boss)
  - [2.1 Bảng chỉ số gốc + hệ số giảm sát thương](#21-bảng-chỉ-số-gốc--hệ-số-giảm-sát-thương)
  - [2.2 Xếp hạng độ khó thật (HP hiệu dụng / người-phút)](#22-xếp-hạng-độ-khó-thật-hp-hiệu-dụng--người-phút)
- [3. Phần thưởng hiện tại của từng boss](#3-phần-thưởng-hiện-tại-của-từng-boss)
  - [3.1 Ba mẫu thưởng và tỉ lệ thật trong code](#31-ba-mẫu-thưởng-và-tỉ-lệ-thật-trong-code)
  - [3.2 Quy giá trị phần thưởng ra vàng](#32-quy-giá-trị-phần-thưởng-ra-vàng)
  - [3.3 Bảng phần thưởng từng boss](#33-bảng-phần-thưởng-từng-boss)
- [4. Đối chiếu độ khó ↔ giá trị phần thưởng](#4-đối-chiếu-độ-khó--giá-trị-phần-thưởng)
  - [4.1 Bảng chấm điểm và xếp hạng](#41-bảng-chấm-điểm-và-xếp-hạng)
  - [4.2 Boss thưởng quá hậu so với độ khó](#42-boss-thưởng-quá-hậu-so-với-độ-khó)
  - [4.3 Boss thưởng quá bèo so với độ khó](#43-boss-thưởng-quá-bèo-so-với-độ-khó)
- [5. Đối chiếu với tuyến nhiệm vụ mới](#5-đối-chiếu-với-tuyến-nhiệm-vụ-mới)
  - [5.1 Bảng đối chiếu mốc SM ↔ độ khó boss](#51-bảng-đối-chiếu-mốc-sm--độ-khó-boss)
  - [5.2 Boss người chơi tới quá sớm / quá mạnh → giết dễ mà vẫn ăn đồ xịn](#52-boss-người-chơi-tới-quá-sớm--quá-mạnh--giết-dễ-mà-vẫn-ăn-đồ-xịn)
  - [5.3 Boss quá khó so với mốc nhiệm vụ → người chơi kẹt](#53-boss-quá-khó-so-với-mốc-nhiệm-vụ--người-chơi-kẹt)
  - [5.4 Boss thế giới nằm đè lên map tuyến chính (nguy hiểm chết người)](#54-boss-thế-giới-nằm-đè-lên-map-tuyến-chính-nguy-hiểm-chết-người)
- [6. Đề xuất sửa](#6-đề-xuất-sửa)
  - [6.1 Năm nguyên tắc cân bằng](#61-năm-nguyên-tắc-cân-bằng)
  - [6.2 Bảng đề xuất chỉnh HP / dame](#62-bảng-đề-xuất-chỉnh-hp--dame)
  - [6.3 Bảng đề xuất chỉnh phần thưởng](#63-bảng-đề-xuất-chỉnh-phần-thưởng)
  - [6.4 Cơ chế phân biệt "giết boss vì nhiệm vụ" và "farm boss"](#64-cơ-chế-phân-biệt-giết-boss-vì-nhiệm-vụ-và-farm-boss)
  - [6.5 Danh sách file / hàm phải sửa](#65-danh-sách-file--hàm-phải-sửa)
- [7. Cảnh báo về cơ chế thưởng boss hiện tại](#7-cảnh-báo-về-cơ-chế-thưởng-boss-hiện-tại)
  - [7.1 Chỉ người kết liễu nhận thưởng — không có top sát thương](#71-chỉ-người-kết-liễu-nhận-thưởng--không-có-top-sát-thương)
  - [7.2 Boss không gọi checkDoneTaskKillBoss](#72-boss-không-gọi-checkdonetaskkillboss)
  - [7.3 Ảnh hưởng tới tuyến nhiệm vụ mới](#73-ảnh-hưởng-tới-tuyến-nhiệm-vụ-mới)
- [8. Ghi chú / điểm cần chủ dự án quyết](#8-ghi-chú--điểm-cần-chủ-dự-án-quyết)

---

## 0. Kết luận ngắn (đọc trước)

Đã kiểm **38 boss / nhóm boss** (24 boss thế giới, 9 boss phó bản–sự kiện liên quan tuyến mới, 5 boss mới do file 20 đề xuất).

Bốn kết luận chính:

1. **"Xên hoàn thiện rơi đồ rất tốt" là đúng — nhưng đó là hai con khác nhau, và con rơi đồ tốt lại *không* khó nhất.**
   - `Xên bọ hung (-100)` 3 form chỉ rơi **Mẫu A** (25.000 vàng + 80% 1 Ngọc Rồng). Nó có **600 triệu HP hiệu dụng** — đắt nhất nhì game mà thưởng bèo nhất game.
   - `Siêu Bọ Hung (-101)` có **form 1 tên đúng là "Xên Hoàn Thiện"** (`BossesData.SIEU_BO_HUNG_1`, dòng 716–717) — đây mới là con rơi **đồ cấp 30%** (cao gấp **6 lần** Cooler / Black Goku / Cumber, gấp **30 lần** nhóm Mabư 12h) + đồ Thần Linh 5%.
2. **Boss "dễ mà thưởng hậu" nặng nhất hiện nay không phải Xên, mà là `Cooler (-29)`, nhóm `Mabư 12h`, `Tiểu đội sát thủ Namek` và `Bojack`.** Cooler có HP hiệu dụng thấp nhất trong nhóm rơi đồ Thần Linh (700 triệu, bằng **17,5%** của Black Goku) nhưng bảng thưởng y hệt Black Goku → hiệu quả farm **cao gấp 5,7 lần**.
3. **Tuyến nhiệm vụ mới dẫn người chơi vào đúng 4 nguồn đồ Thần Linh duy nhất của server**: NV 34 Cooler, NV 38 Black Goku, NV 39 Baby, NV 42 Cumber — cộng thêm NV 36/37 nhóm Mabư 12h. Sau khi tuyến chạy, mọi tài khoản đều đi qua các boss này với một đám đông có tổ chức → đồ Thần Linh từ "hiếm" thành "ai chơi tuyến cũng có".
4. **Ngược lại, phần lớn boss trong tuyến mới lại *quá khó* so với mốc sức mạnh mà chính file 20 đặt ra** — chênh 20–500 lần (xem §5.3). Nếu giữ nguyên HP, tuyến nhiệm vụ sẽ **kẹt cứng** từ NV 22 trở đi.

Nghĩa là bài toán **không phải** "nerf boss" hay "nerf loot" đơn thuần, mà là **tách hẳn hai vai trò**: boss cốt truyện (nhanh, chỉ rơi đồ nhiệm vụ) và boss thế giới (dai, rơi đồ xịn, không nằm trong bước bắt buộc). Chi tiết ở §6.

---

## 1. Mô hình tính "sức mạnh người chơi cần có"

### 1.1 Giả định và công thức

Toàn bộ con số dưới đây suy ra từ code, không phải ước lượng cảm tính. **Mọi giả định đều ghi rõ để chủ dự án có thể bác bỏ từng cái một.**

**(A) Quan hệ Sức mạnh (SM) ↔ Tiềm năng (TN).**
`Service.addSMTN(player, type, param)` (docs/03 §11.3) cộng **cùng một `param`** vào `power` và `tiemNang` khi `type = 2` (nguồn chính: đánh quái, thưởng nhiệm vụ). Vì vậy:

> **Giả định 1:** tổng TN người chơi từng nhận ≈ SM hiện tại. Người chơi đã tiêu gần hết TN vào chỉ số.

**(B) Chi phí tiềm năng của từng chỉ số** — `NPoint.increasePoint` (docs/03 §4.3):

| Chỉ số | Mỗi điểm cho | Chi phí TN | Tổng TN để đạt mức X |
|---|---|---|---|
| HP gốc (`hpg`) | +20 | `hpg + 1000` | `≈ 50·X + X²/40` → **`X ≈ √(40·TN)`** |
| Sức đánh gốc (`dameg`) | +1 | `100 × dameg` | `≈ 50·X²` → **`X = √(TN/50)`** |
| Giáp gốc | +1 | `(defg + 5) × 100.000` | 550 giáp ≈ **15,4 tỉ TN** |
| Chí mạng gốc | +1 | `50.000.000 × 5^critg` | 5 chí mạng ≈ **31,2 tỉ TN** |

> **Giả định 2:** người chơi bỏ **50% TN vào sức đánh, 30% vào HP, 20% vào KI**, và **bỏ qua giáp / chí mạng gốc** (vì 1 điểm giáp đầu tiên đã tốn 500.000 TN và 1 điểm chí mạng đầu tiên tốn 50 triệu TN — không ai mua sớm). Chí mạng lấy từ Rada / Nhẫn thay vì tiềm năng.

**(C) Trần chỉ số gốc** — `NPoint.getDameLimit()` (docs/03 §4.1): `limitPower = 0` → `dameg ≤ 11.000`. Tuyến mới mở giới hạn lần 1 ở **NV 33** và lần 2 ở **NV 44**, nên **toàn bộ NV 0–32 bị chặn ở `dameg ≤ 11.000`**.

**(D) Sát thương mỗi đòn** — `NPoint.getDameAttack` (docs/04 §4):

```
dmg = dame × skillDamage% × (1 + nộiTại%) ... ; nếu chí mạng: ×2, cộng tlSDCM%
```

> **Giả định 3:** đòn chuẩn dùng để tính DPS là **Chiêu đấm cấp 7** (`damage = 160%`, `cooldown = 500 ms` — docs/04 §5.1), tức **2 đòn/giây**. Chí mạng ~10% (từ Rada cấp 10/11 hoặc Nhẫn Thần Linh) → hệ số trung bình **×1,1**.
>
> **DPS 1 người = `dame × 1,6 × 2 × 1,1` = `dame × 3,52`.**
>
> Không tính buff (Cuồng nộ ×2, Bổ huyết ×2, Chibi ×2 HP, sao pha lê %, danh hiệu %). Người chơi buff đầy có thể **×2 → ×3 DPS**; các bảng dưới ghi thêm cột "5 người" và "5 người + buff ×2" để phản ánh điều này.

**(E) Trang bị.** Lấy đúng option shop trong `item_shop_option` và đúng code `ItemService.randDoTLBoss` (dòng 956–1086):

| Món | `powReq` | Option thật |
|---|---|---|
| Găng bạc Goku (254), cấp 9 | 68.000.000 | Tấn công **+680** |
| Găng siêu Xayda (263), cấp 10 | 200.000.000 | Tấn công **+1.050** |
| Găng Kaio (264), cấp 11 | 600.000.000 | Tấn công **+1.550** |
| Găng Thần Linh (562/564/566), cấp 13 | 0 | Tấn công **+4.300 … +5.175** (`4400 × tiLe/100`, `tiLe = 100…115`) |
| Quần Kaio (252), cấp 11 | 170.000.000 | HP **+20.000**, +3.100 HP/30s |
| Quần Thần Linh (556/558/560) | 0 | HP **+48.000 … +59.800** (option 22 = `chiso/1000` với `chiso = 48.000–52.000 × tiLe/100`) |
| Nhẫn Thần Linh (561) | 0 | Chí mạng **+14 … +16%** |
| Rada cấp 11 (280) | 1.000.000.000 | Chí mạng **+11%** |

> **Điểm đáng lưu ý ngay:** một **Găng Thần Linh** cho sức đánh gấp **~3 lần** món Găng tốt nhất mua được bằng vàng (Găng Kaio +1.550, giá 60 triệu vàng). Đây là lý do đồ Thần Linh phải gắn với boss khó nhất.

**(F) HP hiệu dụng của boss.** Nhiều boss override `injured` để chia sát thương nhận vào. HP hiệu dụng = `HP khai báo × hệ số`:

| Boss | Code | Hệ số |
|---|---|---|
| Xên bọ hung (-100) | `(dame / 2) − def` — `Cell/XenBoHung.java:80` | **×2** |
| Siêu Bọ Hung (-101) | `(dame / 3) − def` — `Cell/SieuBoHung.java:151` | **×3** |
| Baby (-925) | `(dame × 0,7 / 2) − def` — `Baby/Baby.java:108` | **×2,857** |
| Black Goku (-203) form 2 | chia đôi trước rồi `− rand(0..100.000) − def` — `Black_Goku/BlackGoku.java:93` | **×2** (form 2) |
| Cumber (-203999) form 2 | như Black Goku — `cumber/Cumber.java:84` | **×2** (form 2) |
| Cooler (-29) | né 1%, `dame − def`, không giới hạn — `Cold/Cooler.java:92` | **×1,01** |
| Broly / Super Broly | tối đa `hpMax/100` mỗi đòn — `Broly/Broly.java:111` | ≥ **100 đòn** |
| Mabư 14h (-214) | né 10%, tối đa 30M/đòn — `MajinBuu_14h/Mabu2H.java:127` | **×1,11** + trần đòn |
| Fide Vàng (-502) | tối đa 50M/đòn — `Golden_fireza/GoldenFrieza.java:60` | ≥ **20 đòn** |
| Nappa, TĐST, Fide, Android, King Kong, Bojack | **không override `injured`** → nhận trọn sát thương, **không trừ giáp** (docs/11 §9.18) | **×1** |

### 1.2 Bảng hồ sơ người chơi theo mốc sức mạnh

Tính theo §1.1. Cột "Trang bị" là mức hợp lý ở mốc đó.

| Mốc | SM | `dameg` | Trang bị (Tấn công / HP cộng thêm) | **dame cuối** | **DPS 1 người** | `hpg` | **HP cuối** |
|---|---:|---:|---|---:|---:|---:|---:|
| P1 | 46.000 (NV 6) | 21 | đồ cấp 1 (+20 / +2.000) | **42** | **149** | 743 | 2.775 |
| P2 | 2.000.000 (NV 15) | 141 | đồ cấp 4–5 (+120 / +8.000) | **261** | **920** | 4.899 | 12.899 |
| P3 | 30.000.000 (NV 20) | 548 | Găng cấp 9 (+680 / +16.000) | **1.228** | **4.322** | 18.974 | 34.974 |
| P4 | 100.000.000 (NV 23) | 1.000 | Găng cấp 9 (+680 / +16.000) | **1.680** | **5.914** | 34.641 | 50.641 |
| P5 | 300.000.000 (NV 25) | 1.732 | Găng cấp 10 (+1.050 / +16.000) | **2.782** | **9.793** | 60.000 | 76.000 |
| P6 | 800.000.000 (NV 28) | 2.828 | Găng cấp 10 (+1.050) | **3.878** | **13.652** | 97.980 | 113.980 |
| P7 | 1.400.000.000 (NV 30) | 3.742 | Găng Kaio (+1.550 / +20.000) | **5.292** | **18.627** | 129.615 | 149.615 |
| P8 | 2.000.000.000 (NV 31) | 4.472 | Găng Kaio (+1.550) | **6.022** | **21.198** | 154.919 | 174.919 |
| P9 | 3.500.000.000 (NV 34) | 5.916 | Găng Kaio (+1.550) | **7.466** | **26.281** | 204.939 | 224.939 |
| P10 | 6.000.000.000 (NV 37) | 7.746 | Găng Kaio (+1.550) | **9.296** | **32.722** | 268.328 | 288.328 |
| P11 | 8.000.000.000 (NV 39) | 8.944 | Găng Thần Linh (+4.400 / +52.000) | **13.344** | **46.972** | 309.839 | 361.839 |
| P12 | 11.500.000.000 (NV 41) | 10.724 | Găng Thần Linh (+4.400) | **15.124** | **53.236** | 371.484 | 423.484 |
| P13 | 13.000.000.000 (NV 42) | **11.000** (chạm trần) | Găng Thần Linh (+4.400) | **15.400** | **54.208** | 394.968 | 446.968 |
| P14 | 17.500.000.000 (NV 46) | **11.000** (chạm trần) | Găng Thần Linh (+4.400) | **15.400** | **54.208** | 458.258 | 510.258 |
| P15 | 90.000.000.000 (`limitPower = 9`) | 26.000 | Thần Linh full (+5.175) | **31.175** | **109.736** | 1.039.230 | 1.091.230 |

> **Chú ý trần chỉ số:** từ P13 trở đi `dameg` chạm trần 11.000 của `limitPower = 0`. DPS **không tăng nữa** dù SM lên từ 13 tỉ tới 18 tỉ. Nếu người chơi không mở giới hạn ở NV 33 / NV 44 thì **chương 6 đứng yên hoàn toàn về sức mạnh**. Đây là lý do NV 33 (mở giới hạn) là bước bắt buộc — thiết kế 20c làm đúng chỗ này.

### 1.3 Kiểm chứng mô hình

Thiết kế 20a ghi ở NV 15: *"HP thực tế chỉ khoảng 20.000–40.000"* ở mốc 2 triệu SM. Mô hình cho **12.899** (chỉ tiềm năng + trang bị cơ bản) → cùng bậc, chênh do 20a giả định trang bị tốt hơn. Mô hình dùng được.

Ngược lại, 20a ước lượng boss Jaco (-2001) tổng HP 3,7 triệu ≈ *"1,5–3 phút đánh solo"*. Theo mô hình: `3.700.000 / 920 = 4.022 giây ≈ 67 phút`. **Sai lệch ~25 lần.** Đây là lỗi ước lượng hệ thống của bộ 20a/20b/20c: các file này ước tính thời gian hạ boss mà không dùng công thức sát thương thật. Toàn bộ §5 dưới đây dựa trên công thức, không dựa trên ước lượng của file 20.

---

## 2. Độ khó thật của từng boss

### 2.1 Bảng chỉ số gốc + hệ số giảm sát thương

Nguồn: `SRC/src/nro/models/boss/BossesData.java` (số dòng ghi ở cột cuối) và docs/11 §5–§8, docs/11b.

| Boss (ID) | Form | HP mỗi form | Dame | Skill đáng ngại | Hồi sinh | Giảm ST nhận | `BossesData` dòng |
|---|---:|---:|---:|---|---|---|---:|
| Kẻ Thu Gom (-2000) *(mới, 20a)* | 1 | 80.000 | 400 | Dragon 3 | 2 phút | — | *chưa có* |
| Jaco Mất Ký Ức (-2001) *(mới, 20a)* | 2 | 1,2M → 2,5M | 3.000 → 5.000 | Quả cầu kênh khí, DCTT | 5 phút | — | *chưa có* |
| Kuku (-20) | 1 | 500.000 | 9.000 | Masenko 3, Liên hoàn 7 | 10 phút | — | 34 |
| Mập Đầu Đinh (-21) | 1 | 1.000.000 | 10.000 | Galick 7, Antomic 7 | 10 phút | — | 56 |
| Rambo (-22) | 1 | 1.500.000 | 12.400 | Galick 7, Antomic 7 | 10 phút | — | 77 |
| Số 4 (-23) | 1 | 25.000.000 | 10.000 | **Thôi miên 7** | cùng TĐT | — | 99 |
| Số 3 (-24) | 1 | 30.000.000 | 11.000 | Antomic 4 | cùng TĐT | — | 125 |
| Số 2 (-25) | 1 | 30.500.000 | 12.000 | Antomic 3 | cùng TĐT | — | 150 |
| Số 1 (-26) | 1 | 40.000.000 | 12.500 | Kamejoko 4 | cùng TĐT | — | 174 |
| Tiểu đội trưởng (-27) | 1 | 50.000.000 | 13.000 | **đổi thân xác 50%/10s** | 5 phút | — | 198 |
| TĐST Namek (-311…-315) | 5 con | 2,5M…5M | 10.000–15.000 | Thôi miên 7 | 5 phút | — | 222–316 |
| Fide đại ca (-28) | 3 | 10M → 20M → 30M | 22.000 → 30.000 | Masenko 7, Galick 7 | 10 phút | — | 340/362/390 |
| Android 19 (-30) | 1 | 1.000.000 | 12.200 | **hấp thụ chưởng, hồi 80%** | cùng Dr.Kôrê | chưởng = 0 ST | 448 |
| Dr.Kôrê (-31) | 1 | 2.000.000 | 12.000 | Thôi miên 3 | 10 phút | *(hấp thụ lỗi, xem §7)* | 415 |
| Android 13 (-32) | 1 | 3.000.000 | 12.055 | — | do A14 gọi | **bất tử khi A14/A15 còn sống** | 469 |
| Android 14 (-33) | 1 | 4.000.000 | 12.000 | — | 10 phút | hồi đầy 1 lần khi gọi A13 | 498 |
| Android 15 (-34) | 1 | 5.000.000 | 12.200 | — | cùng A14 | hồi đầy 1 lần | 518 |
| Pic (-35) | 1 | 10.000.000 | 17.022 | — | cùng KK | — | 535 |
| Poc (-36) | 1 | 15.000.000 | 18.000 | — | cùng KK | — | 570 |
| King Kong (-37) | 1 | 20.000.000 | 12.000 | — | 10 phút | — | 592 |
| **Xên bọ hung (-100)** | 3 | 50M → 100M → 150M | 20.000 → 30.000 | DCTT, Thôi miên, **hấp thụ giết người chơi** | **30 phút** | **`dame/2`** | 614/653/680 |
| Xên con (-102…-108) | 1 ×7 | 5.000.000 | 15.000 | Kamejoko 7 | do -101 gọi | — | 755–857 |
| **Siêu Bọ Hung (-101)** | 2 | 150M → 200M | 35.000 → 40.000 | TDHS 7, Thôi miên 7, **tự phát nổ `hpMax` toàn khu** | **30 phút** | **`dame/3`** | 716/736 |
| **Cooler (-29)** | 2 | 200M → 500M | 32.000 → 50.000 | Galick 1, Antomic 1 *(cấp 1!)* | 30 phút | né 1% | 2222/2250 |
| **Black Goku (-203)** | 2 | 500M → **2.000M** | 50.000 → 100.000 | Tái tạo NL, Khiên NL, TDHS | 5 phút | form 2 `dame/2` | 875/905 |
| **Cumber (-203999)** | 2 | 500M → **2.000M** | 50.000 → 100.000 | như Black Goku | 5 phút | form 2 `dame/2` | 933/963 |
| **Baby (-925)** | 3 | 2.000M ×3 | 200.000 → 250.000 → **30.000** | 6 chưởng hồi 0,1s + Super Kame | 15 phút | **`dame×0,7/2`** | 2599/2640/2672 |
| Bojack (-320) + 4 đệ | 2 + 4 | 100M→150M; 20M–80M | 170.000–300.000 | Trói 7, Tái tạo NL, Khiên NL | 15 phút | — | 1496–1602 |
| Siêu Bojack (-321) | 1 | 500.000.000 | 300.000 | Trói 3, Tái tạo NL | 30 phút | — | 1623 |
| **Fide Vàng (-502)** | 1 | 1.000.000.000 | 100.000 | **Bom 2,1 tỉ ST xuyên giáp**, 5 Death Beam bất tử mỗi con 2,1 tỉ ST | 5 phút, **chỉ 21:00–21:59** | tối đa 50M/đòn | 2496 |
| Broly (-1822) | 1 | rand 500–100.000 | `hp/100` | tăng chỉ số khi bị đánh | 600 giây | **tối đa `hpMax/100`/đòn** | — |
| Super Broly (-82282) | 1 | rand 1,5M–16.070.777 | `hp/100` | tăng chỉ số | — | **tối đa `hpMax/100`/đòn** | — |
| Drabura (-233) | 1 | 20.000.000 | 10.000 | hóa đá 22s | **60 giây (hồi sinh tại chỗ)** | tối đa 20M/đòn | 1164 |
| Bui Bui (-234/-238) | 1 ×2 | 40.000.000 | 200.000 | làm chậm | 60 giây | **miễn nhiễm Kamejoko/Masenko/Antomic/Liên hoàn** | 1213/1228 |
| Ya côn (-235) | 1 | 50.000.000 | 200.000 | tàng hình + chí mạng 100% | 60 giây | miễn chưởng | 1245 |
| Mabư 12h (-236) | 1 | 100.000.000 | 10.000 | hóa đá / Sôcôla | 60 giây | tối đa 50M/đòn | 1089 |
| Mabư 14h (-214) | 5 | 50M→60M→80M→100M→150M | 500.000 | **ăn người chơi 20%/10s**; form 5 chỉ chết bằng Quả cầu kênh khí | 10 phút | né 10%, tối đa 30M/đòn | 990–1056 |
| Dr Lychee / Hatchiyack | 2 | `1M + 15M×lv` → ×1,5 (trần 2 tỉ) | `10k + 1k×lv` → ×1,5 | — | theo phó bản | giảm `lv/10`% và `lv/5`% | *(11b §157)* |
| Bản sao người chơi *(mới, 20b)* | 1 | `hpMax × 3` | `dame × 3` | toàn bộ skill người chơi | — | theo `nPoint` người chơi | *(chưa có)* |
| Heart (-108108) *(mới, 20c)* | 4 | 1,5 tỉ → 2 tỉ ×3 | 150k → 500k | toàn bộ | 10 phút | đề xuất tối đa `hpMax/50`/đòn | *(chưa có)* |

### 2.2 Xếp hạng độ khó thật (HP hiệu dụng / người-phút)

Đơn vị chuẩn hóa: **"người-phút"** = số phút × số người chơi cần để hạ, tính theo DPS của người chơi **P14 (17,5 tỉ SM, full Thần Linh, DPS 54.208)** — tức người farm boss thật sự ở endgame.

`người-phút = HP hiệu dụng / (54.208 × 60)`

| Hạng | Boss (một lượt hoàn chỉnh) | HP hiệu dụng | **người-phút** | Ghi chú độ khó ngoài HP |
|---:|---|---:|---:|---|
| 1 | **Baby (-925)** 3 form | **17.142.000.000** | **5.270** | dame 200–250k, 6 chưởng hồi 0,1s |
| 2 | Black Goku (-203) 2 form | 4.000.000.000 | 1.230 | Tái tạo NL + Khiên NL, 2 con/server |
| 2 | Cumber (-203999) 2 form | 4.000.000.000 | 1.230 | như trên, 1 con |
| 4 | Siêu Bọ Hung (-101) 2 form | 1.050.000.000 | **323** | tự phát nổ `hpMax` toàn khu khi chết |
| 5 | Fide Vàng (-502) | 1.000.000.000 | 308 | **Bom 2,1 tỉ xuyên giáp** — gần như không thể solo |
| 6 | Cooler (-29) 2 form | 700.000.000 | **215** | **không có skill hồi máu / khiên**, skill cấp 1 |
| 7 | Xên bọ hung (-100) 3 form | 600.000.000 | **185** | hấp thụ (giết 1 người ngẫu nhiên) |
| 8 | Siêu Bojack (-321) | 500.000.000 | 154 | Tái tạo NL |
| 9 | Mabư 14h (-214) 5 form | 440.000.000 | 135 | form cuối chỉ chết bằng Quả cầu kênh khí |
| 10 | Bojack (-320) + 4 đệ | 350.000.000 | 108 | dame 170k–300k |
| 11 | Mabư 12h (chuỗi 6 boss) | 250.000.000 | 77 | **mỗi con hồi sinh sau 60 giây tại chỗ** |
| 12 | TĐST Xayda (5 con) | 175.500.000 | 54 | phải giết theo thứ tự 4→3→1&2→TĐT |
| 13 | Fide (-28) 3 form | 60.000.000 | 18,4 | — |
| 14 | King Kong + Pic + Poc | 45.000.000 | 13,8 | thứ tự Poc→Pic→KK |
| 15 | Xên con ×7 | 35.000.000 | 10,8 | — |
| 16 | TĐST Namek (5 con) | 18.000.000 | **5,5** | — |
| 17 | Super Broly (max roll) | 16.070.777 | 4,9 | trần `hpMax/100`/đòn → ≥ 100 đòn |
| 18 | A13 + A14 + A15 | 12.000.000 | 3,7 | A14/A15 hồi đầy 1 lần |
| 19 | Drabura (-233) đơn lẻ | 20.000.000 | 6,2 | hồi sinh 60 giây |
| 20 | Dr.Kôrê + Android 19 | 3.000.000 | 0,9 | A19 miễn chưởng |
| 21 | Rambo (-22) | 1.500.000 | 0,5 | — |
| 22 | Mập Đầu Đinh (-21) | 1.000.000 | 0,3 | — |
| 23 | Kuku (-20) | 500.000 | 0,2 | — |

> **Đọc bảng này ra sao:** Baby đắt gấp **4,3 lần** Black Goku, gấp **24 lần** Cooler, gấp **68 lần** Mabư 12h. Nếu phần thưởng không chênh theo đúng tỉ lệ đó thì cân bằng đã vỡ.

---

## 3. Phần thưởng hiện tại của từng boss

### 3.1 Ba mẫu thưởng và tỉ lệ thật trong code

**Mẫu A — "Vàng + Ngọc Rồng"** (`Nappa/Kuku.java:30`, `Frieza/Fide.java:25`, `Android/*.java`, `Cell/XenBoHung.java:31`, `Cell/XENCON*.java`, `tieu_doi_sat_thu/*.java`)

| # | Nội dung | Tỉ lệ | Số lượng | Ai nhận |
|---|---|---|---|---|
| 1 | +5 Point sự kiện | 100% | 5 | người kết liễu |
| 2 | `checkDoneTaskKillBoss` | 100% | — | người kết liễu |
| 3 | **Vàng (190)** | 100% | 20.000–30.001 | rơi đất, chủ = người kết liễu |
| 4 | 1 trong **Ngọc Rồng 5 sao (18) / 6 sao (19) / 7 sao (20)** | 80% | 1 | rơi đất, chủ = người kết liễu |

**Mẫu B — "Boss cao cấp"** (`Cold/Cooler.java:30`, `Black_Goku/BlackGoku.java:30`, `cumber/Cumber.java:31`, `Baby/Baby.java:24`, `Cell/SieuBoHung.java:79`, `MajinBuu_12h/*.java`)

| # | Nội dung | Tỉ lệ theo boss |
|---|---|---|
| 1 | **1 món đồ Thần Linh** (`ItemService.randDoTLBoss`, dòng 956) — Nhẫn (561), Găng (562/564/566), Quần (556/558/560), Áo (555/557/559), Giầy (563/565/567) | Cooler **5%**, Siêu Bọ Hung **5%**, Black Goku **5%**, Cumber **5%**, **Baby 10%**, nhóm Mabư 12h **1%** |
| 2 | Vàng (190) | 100%, 20.000–30.000 |
| 3 | **1 món đồ cấp 9–11** (danh sách 27 món Áo/Quần/Giày 70% + 12 món Găng/Rada 30%), chỉ số shop ×100–115%, kèm option 107 "# Sao Pha Lê" 1–3 (80%) / 4–5 (17%) / 6 (3%) | Cooler **5%**, **Siêu Bọ Hung 30%**, Black Goku **5%**, Cumber **5%**, Baby **5%**, Mabư 12h **1%** |
| 4 | 1 loại Ngọc Rồng 2–7 sao (15–20) [+ Nhẫn thời không sai lệch (992)] | Cooler **80%** (1–3 viên), Siêu Bọ Hung **80%**, Black Goku / Cumber **10%**, Baby **10%**, Mabư 12h **10%** |

> **Lỗi trong `randDoTLBoss` (ItemService.java:965–975):** chuỗi `if (Util.isTrue(10,100)) … else if (Util.isTrue(25,100)) …` khiến tỉ lệ thật **không khớp chú thích**. Tỉ lệ thật: Nhẫn **10%**, Găng **22,5%** *(chú thích ghi 15%)*, Quần **30,4%** *(ghi 20%)*, Áo **27,8%** *(ghi 30%)*, Giày **9,3%** *(ghi 25%)*. Hệ quả: **Nhẫn + Găng (hai món đắt nhất, 500 triệu vàng/món) chiếm 32,5%** thay vì 25% như thiết kế.

**Mẫu C — "Ngọc xanh + Cải trang"** (`trai_dat/BOJACK.java:25`, `trai_dat/SUPER_BOJACK.java`, `tieu_doi_sat_thu_namek/TDT_NM.java:32`)

| # | Nội dung | Tỉ lệ | Số lượng |
|---|---|---|---|
| 1 | **Ngọc xanh (77)** — 4 vòng lặp thả nhiều cụm | **100%** | Bojack & Siêu Bojack: **5–20 ngọc/cụm**; TĐST Namek: 1–5 ngọc/cụm |
| 2 | Cải trang tương ứng (423–428, 429–433), option lấy từ shop | **100%** | 1 |
| 3 | Ngọc Rồng 6 sao (19) | **100%** | 1 |
| 4 | Ngọc Rồng 7 sao (20) | **100%** | 1 |
| 5 | Point sự kiện | 100% | +1 / +5 |

Số cụm: `1 + nextInt(2) + nextInt(3,4) + nextInt(3,3)` → kỳ vọng **8 cụm/boss**.
→ **Bojack và mỗi đồng bọn: ~100 ngọc xanh/con; cả nhóm 5 con ≈ 500 ngọc xanh mỗi 15 phút.**
→ **TĐST Namek: ~20 ngọc/con; cả nhóm 5 con ≈ 100 ngọc xanh mỗi 5 phút.**

> Ngọc xanh (`inventory.gem`) là **tiền tệ nạp** của server (docs/19 §290). Đây là hai vòi ngọc lớn nhất trong game và **không** gắn với độ khó.

### 3.2 Quy giá trị phần thưởng ra vàng

Dùng `item_template.gold` (giá shop) làm thước đo giá trị — đây là con số duy nhất trong DB định giá được trang bị.

| Nhóm | Giá trị kỳ vọng 1 món |
|---|---:|
| Đồ cấp 9–11 (70% nhóm Áo/Quần/Giày TB **7.411.111** vàng + 30% nhóm Găng/Rada TB **32.050.000** vàng) | **14.802.778 vàng** |
| Đồ Thần Linh (theo tỉ lệ thật ở §3.1: Nhẫn 10%×500tr, Găng 22,5%×500tr, Quần 30,4%×250tr, Áo 27,8%×200tr, Giày 9,3%×170tr) | **309.903.125 vàng** |
| Vàng (190) mỗi lần rơi | 25.000 vàng |

Ngọc Rồng (14–20) và Ngọc xanh (77) **không quy ra vàng** (không bán được ở shop) — liệt kê riêng ở cột "Thưởng phụ".

### 3.3 Bảng phần thưởng từng boss

| Boss | Mẫu | Đồ Thần Linh | Đồ cấp 9–11 | Vàng | **EV vàng / lượt** | Thưởng phụ (không quy ra vàng) |
|---|---|---:|---:|---:|---:|---|
| Kuku / Mập Đầu Đinh / Rambo | A | — | — | 25.000 | **25.000** | 80% 1 Ngọc Rồng 5–7 sao |
| TĐST Xayda (5 con) | A ×5 | — | — | 125.000 | **125.000** | 5 × 80% NR |
| Fide (-28) 3 form | A ×3 | — | — | 75.000 | **75.000** | 3 × 80% NR |
| Dr.Kôrê + Android 19 | A ×2 | — | — | 50.000 | **50.000** | 2 × 80% NR |
| Android 13/14/15 | A ×3 | — | — | 75.000 | **75.000** | 3 × 80% NR |
| King Kong + Pic + Poc | A ×3 | — | — | 75.000 | **75.000** | 3 × 80% NR |
| **Xên bọ hung (-100)** 3 form | A ×3 | — | — | 75.000 | **75.000** | 3 × 80% NR + huy hiệu `TRUM_SAN_BOSS` |
| Xên con ×7 | A ×7 | — | — | 175.000 | **175.000** | 7 × 80% NR |
| **Siêu Bọ Hung (-101)** 2 form | B ×2 | **5%** ×2 | **30%** ×2 | 50.000 | **39.921.979** | 2 × 80% 1–3 NR 2–7 sao + huy hiệu |
| **Cooler (-29)** 2 form | B ×2 | **5%** ×2 | 5% ×2 | 50.000 | **32.520.590** | 2 × 80% 1–3 NR + huy hiệu |
| **Black Goku (-203)** 2 form | B ×2 | 5% ×2 | 5% ×2 | 50.000 | **32.520.590** | 2 × 10% 1–3 NR / Nhẫn thời không sai lệch (992) |
| **Cumber (-203999)** 2 form | B ×2 | 5% ×2 | 5% ×2 | 50.000 | **32.520.590** | 2 × 10% 1–3 NR / 992 |
| **Baby (-925)** 3 form | B ×3 | **10%** ×3 | 5% ×3 | 75.000 | **95.266.354** | 3 × 1% **Cải trang Baby** (SĐ +30–40%, HP +30–40%, KI +30–40%, giảm 10–20% ST, +10–20% SĐCM, +10–20% tấn công lên Boss, hạn 2–5 ngày) + 3 × 10% NR |
| Mabư 12h (chuỗi 6 boss) | B ×6 | **1%** ×6 | 1% ×6 | 150.000 | **19.632.354** | 6 × 10% 1–3 NR + điểm Mabư |
| Mabư 14h (-214) 5 form | — | — | — | — | **0** | chỉ +5 Point, **không rơi item nào** |
| Bojack + 4 đệ | C ×5 | — | — | — | **0** | **~500 Ngọc xanh** + 5 cải trang + 5 NR6 + 5 NR7 |
| Siêu Bojack (-321) | C | — | — | — | **0** | **~100 Ngọc xanh** + cải trang 428 + NR6 + NR7 |
| TĐST Namek (5 con) | C ×5 | — | — | — | **0** | **~100 Ngọc xanh** + 5 cải trang (429–433) + 5 NR6 + 5 NR7 |
| Fide Vàng (-502) | riêng | — | — | — | **0** | Cải trang Fide vàng (629): SĐ/HP/KI +20%, **hạn 20 ngày**, không giao dịch |
| Super Broly (-82282) | riêng | — | — | — | **0** | Đệ tử (chỉ nếu `plKill.pet == null`) |
| Broly (-1822) | **không có** | — | — | — | **0** | `die()` chỉ `changeStatus(DIE)` — `Broly.java:176` |
| Yardart (Tập sự…Đội trưởng) | riêng | — | — | — | — | **Bí kiếp (590)** với tỉ lệ 1/5 … 1/2 mỗi lần "giết" — boss **không bao giờ chết**, farm vô hạn |
| Dr Lychee / Hatchiyack | riêng | — | — | — | — | Cải trang 738/729 **rơi tự do** (`-1`), số lượng `1 + 2 × số người trong khu` |
| Bản sao Commeson (`NhanBan`) | riêng | — | — | — | — | Bình chứa Commeson (638) — `NhanBan.java:34` |

---

## 4. Đối chiếu độ khó ↔ giá trị phần thưởng

### 4.1 Bảng chấm điểm và xếp hạng

Chỉ số cân bằng: **`vàng / người-phút`** = EV vàng ÷ người-phút (§2.2).
**Mốc chuẩn (benchmark) = Black Goku / Cumber = 26.443 vàng/người-phút** — chọn hai con này làm chuẩn vì chúng là boss cao cấp có tương quan HP↔thưởng hợp lý nhất hiện nay.

| Hạng | Boss | người-phút | EV vàng | **vàng / người-phút** | **So với chuẩn** | Đánh giá |
|---:|---|---:|---:|---:|---:|---|
| 1 | **Mabư 12h (chuỗi 6 boss)** | 77 | 19.632.354 | **255.415** | **×9,66** | 🔴 Quá hậu |
| 2 | Kuku (-20) | 0,2 | 25.000 | **162.624** | ×6,15 | 🟡 Hậu nhưng giá trị tuyệt đối nhỏ |
| 3 | **Cooler (-29)** | 215 | 32.520.590 | **151.104** | **×5,71** | 🔴 Quá hậu |
| 4 | **Siêu Bọ Hung (-101)** | 323 | 39.921.979 | **123.662** | **×4,68** | 🔴 Quá hậu |
| 5 | Mập Đầu Đinh (-21) | 0,3 | 25.000 | 81.312 | ×3,07 | 🟡 |
| 6 | Rambo (-22) / Dr.Kôrê+A19 | 0,5 / 0,9 | 25.000 / 50.000 | 54.208 | ×2,05 | 🟢 |
| 7 | **Black Goku (-203)** | 1.230 | 32.520.590 | **26.443** | **×1,00** | 🟢 **Chuẩn** |
| 7 | **Cumber (-203999)** | 1.230 | 32.520.590 | **26.443** | **×1,00** | 🟢 **Chuẩn** |
| 9 | Android 13+14+15 | 3,7 | 75.000 | 20.328 | ×0,77 | 🟢 |
| 10 | **Baby (-925)** | 5.270 | 95.266.354 | **18.076** | ×0,68 | 🟡 Hơi bèo, nhưng vẫn là nguồn Thần Linh 10% |
| 11 | Xên con (×7) | 10,8 | 175.000 | 16.262 | ×0,61 | 🟢 |
| 12 | King Kong + Pic + Poc | 13,8 | 75.000 | 5.421 | ×0,21 | 🟠 Bèo |
| 13 | Fide (-28) 3 form | 18,4 | 75.000 | 4.066 | ×0,15 | 🟠 Bèo |
| 14 | TĐST Xayda (5 con) | 54 | 125.000 | 2.317 | ×0,09 | 🔴 Quá bèo |
| 15 | **Xên bọ hung (-100)** 3 form | 185 | 75.000 | **407** | **×0,015** | 🔴 **Bèo nhất game** |
| 16 | Mabư 14h (-214) 5 form | 135 | 0 | **0** | ×0 | 🔴 Không có phần thưởng vật phẩm |
| 16 | Fide Vàng (-502) | 308 | 0 | **0** | ×0 | 🔴 Boss khó nhất về cơ chế, chỉ 1 cải trang 20 ngày |
| — | **Bojack + 4 đệ** | 108 | 0 (nhưng **~500 ngọc xanh**) | — | — | 🔴 Ngoài thang đo — vòi tiền tệ nạp |
| — | **TĐST Namek (5 con)** | **5,5** | 0 (nhưng **~100 ngọc xanh**) | — | — | 🔴 **Vòi ngọc rẻ nhất game** |

### 4.2 Boss thưởng quá hậu so với độ khó

1. **`Tiểu đội sát thủ Namek` (-311…-315) — vấn đề nặng nhất về tiền tệ.**
   5,5 người-phút (rẻ hơn Fide 3,3 lần, rẻ hơn Xên bọ hung **34 lần**) mà mỗi lượt cho **~100 Ngọc xanh + 5 cải trang + 5 Ngọc Rồng 6 sao + 5 Ngọc Rồng 7 sao**, hồi sinh **5 phút**, và chỉ có **1 nhóm/server** (`BossManager.loadBoss():143`). Một nhóm endgame canh con này 1 giờ = **~1.200 ngọc xanh**.
   Nguồn: `tieu_doi_sat_thu_namek/TDT_NM.java:32–61`, `SO1_NM…SO4_NM.java`.

2. **`Bojack` (-320) + 4 đệ và `Siêu Bojack` (-321).**
   ~500 ngọc xanh / 15 phút cho 108 người-phút. Về vàng thì bằng 0, nhưng về **tiền tệ nạp** thì đây là vòi lớn nhất. `trai_dat/BOJACK.java:26–35` thả 8 cụm × `nextInt(5,20)` ngọc.

3. **`Cooler` (-29) — vấn đề nặng nhất về đồ Thần Linh.**
   - HP hiệu dụng **700 triệu** = **17,5%** của Black Goku, nhưng **bảng thưởng y hệt** (Thần Linh 5%, đồ cấp 5%) và còn **hơn** ở NR (80% 1–3 viên so với 10% của Black Goku).
   - Skill của Cooler là **Galick cấp 1 và Antomic cấp 1** (`BossesData.java:2222, 2250`) — cấp thấp nhất có thể, trong khi Black Goku có **Tái tạo năng lượng 7 + Khiên năng lượng 7**.
   - Cooler **không hồi máu, không khiên**, chỉ né 1%.
   → **Cooler là con đường rẻ nhất tới đồ Thần Linh trong game: hiệu quả gấp 5,71 lần chuẩn.**

4. **Nhóm `Mabư 12h` — vấn đề nặng nhất về tốc độ lặp.**
   Drabura / Bui Bui / Bui Bui 2 / Ya côn / Mabư đều override `die()` → thưởng xong **chuyển `AFK`, 60 giây sau hồi đầy HP và đánh tiếp** (docs/11 §6.0), **không giới hạn số lần**. Mỗi vòng cho 1% đồ Thần Linh + 1% đồ cấp + 10% NR. Hiệu quả **9,66 lần chuẩn**. Đây là "máy in" đồ Thần Linh của server.

5. **`Siêu Bọ Hung` (-101) — tỉ lệ đồ cấp cao bất thường.**
   **30%** đồ cấp 9–11 (`Cell/SieuBoHung.java:96`) so với **5%** của Cooler / Black Goku / Cumber / Baby và **1%** của Mabư 12h. Không có lý do thiết kế nào giải thích con số 30% này — nhiều khả năng là số còn sót lại khi copy code (dòng chú thích ngay trên nó ghi *"// 30% xác suất để rơi đồ"* và cùng đoạn code ở Cooler/Baby lại ghi `isTrue(5, 100)` với **chú thích vẫn là 30%** — tức là code được copy rồi sửa số nhưng quên sửa chú thích ở nơi khác).

### 4.3 Boss thưởng quá bèo so với độ khó

1. **`Xên bọ hung` (-100) — tệ nhất.** 600 triệu HP hiệu dụng (`dame/2`), 3 form, cơ chế hấp thụ **giết hẳn một người chơi** mỗi lần trúng, hồi sinh **30 phút**, chỉ **1 con toàn server**. Thưởng: **75.000 vàng + 3 lần roll Ngọc Rồng**. Bằng **1,5%** mức chuẩn.
2. **`Mabư 14h` (-214).** 440 triệu HP hiệu dụng, dame **500.000** (một đòn giết người chơi 6 tỉ SM), form cuối **chỉ có thể kết liễu bằng Quả cầu kênh khí**. Thưởng: **+5 Point, không item** (`MajinBuu_14h/Mabu2H.java:119`).
3. **`Fide Vàng` (-502).** Boss khó nhất game về cơ chế: bom **2.100.000.000 sát thương xuyên giáp** mỗi 5–10 giây + 5 Death Beam bất tử mỗi con cũng 2,1 tỉ. Chỉ sống 1 giờ/ngày. Thưởng: **1 cải trang hạn 20 ngày**.
4. **`Tiểu đội sát thủ Xayda`** (54 người-phút, 125.000 vàng) và **`Fide` (-28)** (18,4 người-phút, 75.000 vàng) — bằng 9% và 15% mức chuẩn.
5. **`Broly` (-1822)** — **không có phần thưởng nào cả**: `Broly.die()` chỉ gọi `changeStatus(BossStatus.DIE)` (`Broly/Broly.java:176–178`), và `leaveMap()` **luôn** sinh Super Broly kể cả khi bị giết.

---

## 5. Đối chiếu với tuyến nhiệm vụ mới

### 5.1 Bảng đối chiếu mốc SM ↔ độ khó boss

Cột "TTK" = thời gian hạ boss, tính bằng DPS của hồ sơ người chơi **tại đúng mốc SM mà file 20 đặt cho nhiệm vụ đó** (§1.2). Cột "đòn boss giết ta" = `HP người chơi ÷ dame boss`.

| NV | Boss tuyến mới bắt giết | Mốc SM (20/20a/20b/20c) | HP hiệu dụng | **TTK 1 người** | **TTK 5 người** | **TTK 5 người + buff ×2** | đòn boss giết ta | Kết luận |
|---|---|---:|---:|---:|---:|---:|---:|---|
| 6 | Kẻ Thu Gom (-2000) *(mới)* | 46.000 | 80.000 | 8,9 phút | 1,8 phút | 0,9 phút | 6,9 | 🟡 hơi dai cho NV 6 |
| 15 | Jaco (-2001) *(mới)* 2 form | 2.000.000 | 3.700.000 | **1,1 giờ** | 13,4 phút | 6,7 phút | 2,6 | 🔴 quá dai + dame quá cao |
| 20 | Kuku (-20) | 30.000.000 | 500.000 | 1,9 phút | 0,4 phút | 0,2 phút | 3,9 | 🟠 **quá dễ** |
| 20 | Mập Đầu Đinh (-21) | 30.000.000 | 1.000.000 | 3,9 phút | 0,8 phút | 0,4 phút | 3,5 | 🟢 |
| 20 | Rambo (-22) | 30.000.000 | 1.500.000 | 5,8 phút | 1,2 phút | 0,6 phút | 2,8 | 🟢 |
| 22 | TĐST -23…-27 (5 con) | 70.000.000 | 175.500.000 | **8,2 giờ** | 1,6 giờ | 49,5 phút | 3,9 | 🔴 kẹt |
| 23 | Fide (-28) 3 form | 100.000.000 | 60.000.000 | **2,8 giờ** | 33,8 phút | 16,9 phút | 1,7 | 🔴 kẹt |
| 25 | Dr.Kôrê (-31) + A19 (-30) | 300.000.000 | 3.000.000 | 5,1 phút | 1,0 phút | 0,5 phút | 6,2 | 🟠 **quá dễ** |
| 27 | A13 + A14 + A15 | 500.000.000 | 12.000.000 | 20,4 phút | 4,1 phút | 2,0 phút | 6,2 | 🟢 |
| 28 | King Kong + Pic + Poc | 800.000.000 | 45.000.000 | 54,9 phút | 11,0 phút | 5,5 phút | 6,3 | 🟡 |
| 30 | **Xên bọ hung (-100)** 3 form | 1.400.000.000 | 600.000.000 | **8,9 giờ** | 1,8 giờ | 53,7 phút | 5,0 | 🔴 kẹt nặng |
| 31 | Bản sao (HP ×3 người chơi) | 2.000.000.000 | 524.757 | 0,4 phút | — | — | 4,0 | 🟠 **quá dễ** *(20b chọn ×3 thay vì ×10 của Commeson — hợp lý nhưng hơi nhẹ)* |
| 34 | **Cooler (-29)** 2 form | 3.500.000.000 | 700.000.000 | **7,4 giờ** | 1,5 giờ | 44,4 phút | 4,5 | 🔴 kẹt + **rơi Thần Linh** |
| 36 | Drabura (-233) | 5.000.000.000 | 20.000.000 | 10,2 phút | 2,0 phút | 1,0 phút | 28,8 | 🟠 **quá dễ + rơi Thần Linh 1%** |
| 37 | Mabư 12h (-236) | 6.000.000.000 | 100.000.000 | 50,9 phút | 10,2 phút | 5,1 phút | 28,8 | 🟡 **rơi Thần Linh 1%** |
| 37 | Mabư 14h (-214) 5 form | 6.000.000.000 | 440.000.000 | **3,7 giờ** | 44,8 phút | 22,4 phút | **0,6** | 🔴 kẹt + **one-shot người chơi** |
| 38 | **Black Goku (-203)** 2 form | 7.000.000.000 | 4.000.000.000 | **34 giờ** | 6,8 giờ | 3,4 giờ | 2,9 | 🔴 kẹt cứng + **rơi Thần Linh** |
| 39 | **Baby (-925)** 3 form | 8.000.000.000 | 17.142.000.000 | **101 giờ** | 20,3 giờ | 10,1 giờ | 1,4 | 🔴 **không thể qua** + **Thần Linh 10%** |
| 41 | Broly (-1822) | 11.500.000.000 | ≤ 100.000 | vài giây | — | — | 423 | 🟠 quá dễ, **và không trao nhiệm vụ** |
| 41 | Super Broly (-82282) | 11.500.000.000 | ≤ 16.070.777 | 5,0 phút | 1,0 phút | 0,5 phút | 2,6 | 🟢 *(nhưng không trao nhiệm vụ)* |
| 42 | **Cumber (-203999)** 2 form | 13.000.000.000 | 4.000.000.000 | **20,5 giờ** | 4,1 giờ | 2,0 giờ | 4,5 | 🔴 kẹt cứng + **rơi Thần Linh** |
| 43 | Dr Lychee / Hatchiyack | 14.000.000.000 | `1M + 15M×lv` (trần 2 tỉ) | tuỳ level phó bản | — | — | tuỳ level | 🟡 scale theo level — an toàn |
| 46 | Heart (-108108) 3 form *(mới)* | 17.500.000.000 | 5.500.000.000 | **28,2 giờ** | 5,6 giờ | 2,8 giờ | 1,3 | 🔴 đề xuất của 20c cũng quá dai |

### 5.2 Boss người chơi tới quá sớm / quá mạnh → giết dễ mà vẫn ăn đồ xịn

> Đây chính là vấn đề chủ dự án nêu. Có **5 điểm rò rỉ**, xếp theo mức nghiêm trọng:

**① NV 36–37 — nhóm Mabư 12h (Drabura -233, Mabư -236).**
- Người chơi tới ở mốc **5–6 tỉ SM**; Drabura chỉ có **20 triệu HP** và **dame 10.000** (giết người chơi trong **28,8 đòn** — tức gần như vô hại).
- Nhưng mỗi lần giết, Drabura roll **1% đồ Thần Linh + 1% đồ cấp 9–11 + 10% Ngọc Rồng**.
- Boss **hồi sinh sau 60 giây tại chỗ, vô hạn lần**.
- ⇒ Một người chơi ở NV 36 có thể đứng tại map 114 farm **60 lượt/giờ** → kỳ vọng **0,6 món Thần Linh/giờ** (≈ **186 triệu vàng/giờ**) từ một con boss mà họ giết trong 10 phút lần đầu. **Đây là chỗ vỡ nặng nhất.**

**② NV 34 — Cooler (-29).**
- Tuyến mới đưa mọi người chơi tới Cooler ở mốc **3,5 tỉ SM**, và vì là bước bắt buộc nên **sẽ luôn có đám đông** ở map 110 → thời gian hạ rơi từ 7,4 giờ (solo) xuống **~10–20 phút** (10–20 người).
- Cooler rơi **đồ Thần Linh 5%** — cùng tỉ lệ với Black Goku (HP hiệu dụng gấp 5,7 lần) và Cumber.
- ⇒ Tuyến nhiệm vụ biến con boss có **tỉ lệ thưởng/độ khó tốt nhất game** thành **điểm dừng bắt buộc của mọi tài khoản**. Sau khi tuyến chạy, map 110 sẽ luôn đông → Cooler chết liên tục mỗi 30 phút → nguồn Thần Linh tăng đột biến.

**③ NV 30 — Xên bọ hung (-100) và map 103 (Siêu Bọ Hung -101).**
- NV 30 bắt giết cả 3 form Xên bọ hung; NV 31 mở map **103 Võ đài Xên bọ hung** — nơi **Siêu Bọ Hung (-101)** đóng đô với **đồ cấp 30%** + Thần Linh 5%.
- Người chơi ở NV 31 (2 tỉ SM) chưa hạ nổi Siêu Bọ Hung một mình, nhưng vì cả server đều bị tuyến đẩy vào map 103 cùng lúc, boss sẽ chết mỗi 30 phút với đám đông → **60 lượt roll đồ cấp/ngày ở tỉ lệ 30%**.
- 20b (dòng 718) đã nhận ra xung đột kỹ thuật (bản sao và Siêu Bọ Hung cùng khu, boss tự phát nổ `hpMax` toàn khu) nhưng **chưa nhận ra xung đột kinh tế**.

**④ NV 38 / 39 / 42 — Black Goku, Baby, Cumber.**
- Ba con này là **ba nguồn đồ Thần Linh còn lại**. Tuyến mới đưa cả ba vào bước bắt buộc.
- Riêng **Baby rơi Thần Linh 10%** (gấp đôi mọi boss khác) và **1% Cải trang Baby** — cải trang mạnh nhất rơi từ boss thế giới (SĐ +30–40%, HP +30–40%, KI +30–40%, giảm 10–20% sát thương, +10–20% SĐCM, +10–20% tấn công lên Boss).
- Sau NV 39, mọi tài khoản đều biết cách và có động lực farm Baby.

**⑤ NV 20 / 25 / 31 / 36 / 41 — boss quá dễ so với mốc (thừa sức mạnh).**

| NV | Boss | TTK ở mốc | Ý nghĩa |
|---|---|---|---|
| 20 | Kuku (-20) | **1,9 phút solo** | 500.000 HP ở mốc 30 triệu SM — người chơi mạnh hơn boss ~60 lần |
| 25 | Dr.Kôrê (-31) + A19 | **5,1 phút solo** | 3 triệu HP ở mốc 300 triệu SM |
| 31 | Bản sao (×3) | **0,4 phút** | 20b hạ từ ×10 (Commeson) xuống ×3 → bước cao trào chương 4 mất sức nặng |
| 36 | Drabura (-233) | **10,2 phút**, 28,8 đòn mới chết | xem ①|
| 41 | Broly (-1822) | **vài giây** | HP tối đa 100.000 ở mốc 11,5 tỉ SM |

### 5.3 Boss quá khó so với mốc nhiệm vụ → người chơi kẹt

Sắp theo mức kẹt (TTK 5 người + buff ×2, tức điều kiện tốt nhất mà một nhóm bình thường đạt được):

| Hạng kẹt | NV | Boss | TTK nhóm 5 + buff | Chênh so với mục tiêu 3–7 phút |
|---:|---|---|---:|---:|
| 1 | **39** | **Baby (-925)** | **10,1 giờ** | **×100** |
| 2 | **38** | **Black Goku (-203)** | **3,4 giờ** | ×34 |
| 3 | **46** | **Heart (-108108)** *(đề xuất 20c)* | **2,8 giờ** | ×28 |
| 4 | **42** | **Cumber (-203999)** | **2,0 giờ** | ×20 |
| 5 | **30** | **Xên bọ hung (-100)** | 53,7 phút | ×9 |
| 6 | **22** | **TĐST Xayda** | 49,5 phút | ×8 |
| 7 | **34** | **Cooler (-29)** | 44,4 phút | ×7 |
| 8 | **37** | **Mabư 14h (-214)** | 22,4 phút | ×4 + **dame 500.000 one-shot** |
| 9 | **23** | **Fide (-28)** | 16,9 phút | ×3 |
| 10 | **15** | **Jaco (-2001)** *(đề xuất 20a)* | 6,7 phút | ×1 nhưng solo mất 1,1 giờ |

Ba điểm chết người kèm theo:

- **NV 37 Mabư 14h**: dame **500.000** so với HP người chơi mốc 6 tỉ (**288.328**) → **chết trong 0,6 đòn**, tức **một đòn là chết, không kịp ăn đậu**. Bước này không thể hoàn thành bằng cách "đánh giỏi", chỉ bằng cách đứng ngoài tầm.
- **NV 39 Baby**: dame 200.000–250.000 so với HP 361.839 → **1,4 đòn**. Cộng với 6 skill chưởng hồi 0,1 giây (`BossesData.java:2599`), người chơi chết liên tục.
- **NV 46 Heart** (đề xuất trong 20c §5.2): 3 form × 2 tỉ HP với dame 400.000–500.000 so với HP người chơi 510.258 → **1,3 đòn**. Đề xuất `injured` giới hạn `hpMax/50` mỗi đòn (20c §5.4) làm boss **dai thêm** chứ không làm nó dễ hơn: `hpMax/50 = 40.000.000` — người chơi P14 chỉ gây 54.208/giây nên trần này **không bao giờ chạm tới**, tức nó vô tác dụng nhưng HP 2 tỉ thì vẫn nguyên.

### 5.4 Boss thế giới nằm đè lên map tuyến chính (nguy hiểm chết người)

Đây là vấn đề chưa được nêu ở đâu trong bộ 20, nhưng nó phá tuyến ngay từ chương 1–2:

| Boss thế giới | `mapJoin` | Dame | Map trùng với bước nào của tuyến mới | HP người chơi lúc đó | Hậu quả |
|---|---|---:|---|---:|---|
| **Bojack (-320) + Bujin/Kogu/Zangya/Bido** | **3, 4, 5, 6**, 27, 28, 29, 30 (`BossesData.java:1581`) | **170.000 – 300.000** | **NV 9 & NV 10** (map **5 Đảo Kamê** — sư phụ Trái Đất), **NV 15** (map **27**) | **~12.899** | **chết ngay 1 đòn**, lặp lại mỗi 15 phút |
| **Siêu Bojack (-321)** | 3, 4, 5, 6, 27–30 | 300.000 | như trên | ~12.899 | như trên |
| **TĐST Namek (-311…-315)** | **7, 8, 9, 10, 11, 12, 13, 25**, 33, 34, **43** (`BossesData.java:222`) | 10.000 – 15.000 | **NV 3** (map **43** Vách núi Moori), **NV 7** (map **25** Trạm tàu vũ trụ), **NV 9/10** (map **13** Đảo Guru), **NV 33** (map 43) | **~800 (NV 3) … ~12.899 (NV 10)** | **chết ngay 1 đòn**, hồi sinh mỗi 5 phút |
| **Broly / Super Broly** | 5, **13**, 20, 27–38 | Broly ≤ 1.000; **Super Broly tới 160.707** | **NV 9/10** (map 5/13/20), **NV 15/16** (27–38) | ~12.899 | Broly chấp nhận được; **Super Broly one-shot** |
| **Ăn Trộm (-365), Ở Dơ (-78), Sói hẹc quyn (-77)** | MAP_THUONG_LON (0–20, 24–37, …) | 1.000–3.000 | toàn bộ chương 1–2 | 2.775–12.899 | mất vàng / chết ở NV 0–7 |

> Tuyến cũ không có vấn đề này vì người chơi tới map 5/13/20 rất muộn. Tuyến mới đưa **NV 9–10 (bái sư)** vào đúng map mà Bojack và Broly spawn.

---

## 6. Đề xuất sửa

### 6.1 Năm nguyên tắc cân bằng

> **P1 — Một boss chỉ được giữ một vai.**
> Hoặc là **boss cốt truyện** (nằm trong bước bắt buộc của tuyến chính) — nhanh, dễ tìm, chỉ rơi đồ nhiệm vụ.
> Hoặc là **boss thế giới** (nguồn đồ xịn) — dai, hiếm, **không** nằm trong bước bắt buộc nào.
> **Không có boss nào vừa là cửa nhiệm vụ vừa là nguồn đồ Thần Linh.**

> **P2 — Đồ Thần Linh (555–567) và đồ cấp 9–11 (230–280) chỉ rơi từ tầng "khó".**
> Định nghĩa tầng khó, kiểm được bằng số: **HP hiệu dụng ≥ 1 tỉ** *và* **hồi sinh ≥ 15 phút** *và* **không xuất hiện trong bất kỳ `TASK_*` nào của tuyến chính**.
> Hiện chỉ có **Black Goku, Cumber, Baby, Siêu Bọ Hung, Fide Vàng** đạt tiêu chí HP; nhưng 4/5 con đang nằm trong tuyến mới → phải gỡ khỏi tuyến hoặc gỡ loot.

> **P3 — Boss nhiệm vụ rơi đúng 3 thứ:** vàng theo mốc (§6.3), **vật phẩm nhiệm vụ** (Mảnh Ký Ức trao qua `rewardDoneTask`, **không** rơi ra đất để tránh nhặt hụt — đúng như 20a đã ghi), và **nguyên liệu tiêu hao cấp thấp** (đậu thần, đá nâng cấp 1–3, capsule).
> **Tuyệt đối không**: trang bị mặc được, đồ Thần Linh, Ngọc Rồng, Ngọc xanh, cải trang vĩnh viễn.

> **P4 — HP boss phải khớp mốc SM.**
> Mục tiêu: **60–120 giây solo** cho boss thường; **240–420 giây** cho boss cuối chương. Công thức: `HP khai báo = DPS(mốc) × giây mục tiêu ÷ hệ số giảm sát thương`.

> **P5 — Dame boss phải giết người chơi trong 10–15 đòn ở mốc tương ứng.**
> `dame = HP người chơi (mốc) ÷ 12`. Hiện nhiều boss giết trong 0,6–1,4 đòn (Mabư 14h, Baby, Heart) hoặc 28 đòn (Drabura) — cả hai đều sai.

### 6.2 Bảng đề xuất chỉnh HP / dame

> ⚠️ **Giới hạn kỹ thuật bắt buộc nhớ:** `BossData.hp` là **`int[]`** (`BossesData.java`, mọi khai báo `new int[]{…}`) → trần **2.147.483.647**. Không khai báo HP 3 tỉ / 5 tỉ (tràn sang số âm → boss chết ngay đòn đầu). Muốn boss "dai" hơn thì **tăng hệ số giảm sát thương trong `injured`**, không tăng HP.
> `BossData.dame` cũng là `int` → trần như trên (không vấn đề, dame cao nhất đề xuất là 42.522).

#### 6.2.a Boss thuộc tuyến nhiệm vụ — chuyển sang thang "boss cốt truyện"

Tính theo P4/P5 với DPS và HP người chơi ở mốc SM của chính nhiệm vụ đó (§1.2).

| NV | Boss | HP hiện tại (mỗi form) | **HP đề xuất (mỗi form)** | Dame hiện tại | **Dame đề xuất** | Giây mục tiêu | Ghi chú |
|---|---|---:|---:|---:|---:|---:|---|
| 6 | Kẻ Thu Gom (-2000) | *(mới)* 80.000 | **13.000** | 400 | **230** | 90 | 20a đặt cao gấp 6 lần |
| 15 | Jaco (-2001) form 1 / 2 | *(mới)* 1.200.000 / 2.500.000 | **70.000 / 70.000** | 3.000 / 5.000 | **1.000 / 1.100** | 150 tổng | 20a đặt cao gấp **26 lần** |
| 20 | Kuku (-20) | 500.000 | **260.000** | 9.000 | **2.900** | 60 | giảm dame để không one-shot |
| 20 | Mập Đầu Đinh (-21) | 1.000.000 | **325.000** | 10.000 | **2.900** | 75 | |
| 20 | Rambo (-22) | 1.500.000 | **390.000** | 12.400 | **2.900** | 90 | |
| 22 | Số 4 (-23) | 25.000.000 | **320.000** | 10.000 | **3.700** | 60 | **giảm 78 lần** |
| 22 | Số 3 (-24) | 30.000.000 | **320.000** | 11.000 | **3.700** | 60 | |
| 22 | Số 2 (-25) | 30.500.000 | **320.000** | 12.000 | **3.700** | 60 | |
| 22 | Số 1 (-26) | 40.000.000 | **320.000** | 12.500 | **3.700** | 60 | |
| 22 | Tiểu đội trưởng (-27) | 50.000.000 | **320.000** | 13.000 | **3.700** | 60 | tổng nhóm 300 giây |
| 23 | Fide (-28) f1 / f2 / f3 | 10M / 20M / 30M | **400.000 / 470.000 / 550.000** | 22.000/25.000/30.000 | **4.000 / 4.200 / 4.400** | 240 tổng | boss cuối chương 3 |
| 25 | Android 19 (-30) | 1.000.000 | **590.000** | 12.200 | **6.300** | 60 | |
| 25 | Dr.Kôrê (-31) | 2.000.000 | **880.000** | 12.000 | **6.300** | 90 | |
| 27 | Android 15 (-34) | 5.000.000 | **694.000** | 12.200 | **7.800** | 60 | |
| 27 | Android 14 (-33) | 4.000.000 | **868.000** | 12.000 | **7.800** | 75 | |
| 27 | Android 13 (-32) | 3.000.000 | **1.041.000** | 12.055 | **7.800** | 90 | |
| 28 | Poc (-36) | 15.000.000 | **819.000** | 18.000 | **9.500** | 60 | |
| 28 | Pic (-35) | 10.000.000 | **1.024.000** | 17.022 | **9.500** | 75 | |
| 28 | King Kong (-37) | 20.000.000 | **1.638.000** | 12.000 | **9.500** | 120 | |
| 30 | Xên bọ hung (-100) f1/f2/f3 | 50M / 100M / 150M | **800.000 / 930.000 / 1.060.000** | 20.000/25.000/30.000 | **12.000 / 12.500 / 13.000** | 300 tổng | giữ `dame/2` → HP hiệu dụng 5,58M |
| 31 | Bản sao *(mới)* | `hpMax × 3` | **`hpMax × 8`** | `dame × 3` | **`dame × 1,2`** | ~180 | 20b đề xuất ×3 → hạ trong 24 giây, quá nhẹ cho cao trào chương 4 |
| 34 | Cooler (-29) f1 / f2 | 200M / 500M | **3.000.000 / 3.300.000** | 32.000 / 50.000 | **18.700 / 18.700** | 240 tổng | |
| 36 | Drabura (-233) | 20.000.000 | **2.730.000** | 10.000 | **22.000** | 90 | **tăng dame** ×2,2 |
| 37 | Mabư 12h (-236) | 100.000.000 | **5.890.000** | 10.000 | **24.000** | 180 | **tăng dame** ×2,4 |
| 37 | Mabư 14h (-214) 5 form | 50/60/80/100/150M | **2.000.000 mỗi form** | 500.000 | **24.000** | 360 tổng | **giảm dame 21 lần** (đang one-shot) |
| 38 | Black Goku (-203) f1 / f2 | 500M / **2.000M** | **3.100.000 / 3.600.000** | 50.000 / 100.000 | **28.500 / 28.500** | 300 tổng | giữ `dame/2` ở form 2 |
| 39 | Baby (-925) 3 form | 2.000M ×3 | **1.900.000 mỗi form** | 200k/250k/**30k** | **30.000 mỗi form** | 360 tổng | **form 3 dame = 30.000 nhiều khả năng là lỗi gõ của 300.000** (`BossesData.java:2672`) |
| 41 | Broly (-1822) | rand 500–100.000 | **rand 2.000.000–6.000.000** | `hp/100` | giữ `hp/100` | ~120 | |
| 41 | Super Broly (-82282) | rand 1,5M–16.070.777 | **rand 8.000.000–12.000.000** | `hp/100` | giữ | 180 | |
| 42 | Cumber (-203999) f1 / f2 | 500M / **2.000M** | **3.700.000 / 4.400.000** | 50.000 / 100.000 | **37.200 / 37.200** | 300 tổng | |
| 46 | Heart (-108108) f1/f2/f3 | *(đề xuất 20c)* 1,5 tỉ / 2 tỉ / 2 tỉ | **7.000.000 / 7.600.000 / 8.200.000** | 150k/250k/400k | **42.500 mỗi form** | 420 tổng | bỏ giới hạn `hpMax/50` (vô tác dụng, xem §5.3) |
| 47/50 | Hư Không Vô Danh (form 4) | *(đề xuất 20c)* 2 tỉ | **9.000.000** | 500.000 | **43.000** | 180 | |

> **Mức giảm trung bình: ~90 lần.** Đây là con số lớn và cần chủ dự án quyết dứt khoát (xem §8, câu hỏi 1): hoặc **hạ HP boss thế giới xuống thang cốt truyện** (người chơi cũ sẽ thấy boss "nát"), hoặc **giữ boss thế giới và dựng bản nhiệm vụ riêng** (tốn công code hơn nhưng không đụng tới trải nghiệm endgame).

#### 6.2.b Boss thế giới (ngoài tuyến) — giữ HP, chỉ sửa chỗ lệch

| Boss | Sửa gì | Lý do |
|---|---|---|
| **Siêu Bọ Hung (-101)** | Giữ HP 150M/200M và `dame/3`. **Tăng `loadBoss` từ 1 → 2 con** (`BossManager.java:148`) | Là nguồn đồ cấp chính; 1 con/server + 30 phút nghỉ là nút cổ chai |
| **Fide Vàng (-502)** | Giữ HP 1 tỉ. **Giảm bom từ 2.100.000.000 → 3× HP người chơi trung bình trong khu** | 2,1 tỉ ST xuyên giáp là chết chắc, không phải thử thách |
| **Bojack (-320) + 4 đệ** | Giữ HP. **Đổi `mapJoin` từ `{3,4,5,6,27,28,29,30}` sang `{31,32,36,37,38}`** (`BossesData.java:1581, 1602, 1623`) | Gỡ khỏi map 5 Đảo Kamê và map 27 — nơi NV 9/10/15 diễn ra (§5.4) |
| **TĐST Namek (-311…-315)** | Giữ HP. **Đổi `mapJoin` từ `{7,8,9,10,11,12,13,25,33,34,43}` sang `{33,34,45}`** (`BossesData.java:222, 247, 270, 293, 316`) | Gỡ khỏi map 13 (bái sư Namếc), 25 (NV 7), 43 (NV 3 & 33) |
| **Broly / Super Broly** | **Đổi `mapJoin` bỏ map 5, 13, 20** | Gỡ khỏi 3 map bái sư (NV 9/10) |
| **Ăn Trộm (-365), Ở Dơ (-78)** | Thêm điều kiện `mapId > 20` khi chọn map | Không quấy rối người chơi chương 1 |
| **Mabư 12h — Drabura (-233)** | Bỏ cơ chế hồi sinh `AFK` 60 giây, đổi sang `REST` như boss thường (**5 phút**) | §5.2 ① — đây là cơ chế cho phép farm vô hạn |

### 6.3 Bảng đề xuất chỉnh phần thưởng

#### 6.3.a Boss nhiệm vụ — bỏ toàn bộ loot có giá trị

| Boss | Bỏ | Giữ / Thêm |
|---|---|---|
| Kuku, Mập Đầu Đinh, Rambo (-20/-21/-22) | **bỏ Ngọc Rồng 5–7 sao (80%)** | Vàng **50.000** (thay cho 20.000–30.001) |
| TĐST Xayda (-23…-27) | **bỏ Ngọc Rồng** | Vàng **150.000**/con; thêm **1 Đá nâng cấp cấp 2 (1075)** ở Tiểu đội trưởng |
| Fide (-28) | **bỏ Ngọc Rồng** | Vàng **300.000**/form; form 3 thêm **1 Capsule Vàng (574)** |
| Dr.Kôrê, A19, A13/14/15, Pic/Poc/King Kong | **bỏ Ngọc Rồng** | Vàng **500.000** – **1.000.000** tuỳ con |
| Xên bọ hung (-100) | **bỏ Ngọc Rồng** | Vàng **2.000.000**/form; form 3 thêm **5 Đá nâng cấp cấp 4 (1077)** |
| **Cooler (-29)** | **BỎ đồ Thần Linh (5%)**, **BỎ đồ cấp (5%)**, **BỎ Ngọc Rồng (80%)** | Vàng **5.000.000**/form; form 2 thêm **2 Sao pha lê** |
| Nhóm **Mabư 12h** (6 boss) | **BỎ đồ Thần Linh (1%)**, **BỎ đồ cấp (1%)**, **BỎ Ngọc Rồng (10%)** | Giữ điểm Mabư; Vàng **3.000.000**/con — giá trị chuyển vào **phần thưởng phó bản Mabư** trao 1 lần/ngày |
| **Mabư 14h (-214)** | — | **Thêm**: Vàng **10.000.000**, và **1 Sao pha lê vàng (446)** ở form 5 |
| **Black Goku (-203)** | **BỎ đồ Thần Linh**, **BỎ đồ cấp** | Vàng **8.000.000**/form; giữ 10% Ngọc Rồng / Nhẫn thời không sai lệch (992) |
| **Baby (-925)** | **BỎ đồ Thần Linh (10%)**, **BỎ đồ cấp (5%)** | Vàng **10.000.000**/form; **giữ 1% Cải trang Baby** (đây là phần thưởng "đúng chất boss khó", và cải trang có hạn 2–5 ngày nên không lạm phát) |
| **Cumber (-203999)** | **BỎ đồ Thần Linh**, **BỎ đồ cấp** | Vàng **8.000.000**/form; giữ 10% NR/992 |
| Broly / Super Broly | — | **Thêm** Vàng **5.000.000**; giữ đệ tử |
| Heart (-108108) *(mới)* | — | Vàng **20.000.000**/form; form 3 rơi **Ống nghiệm Myuu (2107)**; **form 4 rơi 1 đồ Thần Linh 100%** — phần thưởng kết tuyến, 1 lần/tài khoản |

#### 6.3.b Boss thế giới — trở thành nguồn đồ xịn duy nhất

Sau khi gỡ Thần Linh khỏi 4 boss trên, cần **bù lại nguồn cung**, nếu không đồ Thần Linh sẽ tuyệt chủng. Đề xuất:

| Boss | HP hiệu dụng | người-phút | **Đồ Thần Linh** | **Đồ cấp 9–11** | Ngọc Rồng | EV vàng mới | vàng/người-phút |
|---|---:|---:|---:|---:|---:|---:|---:|
| **Siêu Bọ Hung (-101)** 2 form | 1.050.000.000 | 323 | **8%** (từ 5%) | **10%** (từ **30%**) | 80% giữ | 52.443.000 | **162.365** |
| **Fide Vàng (-502)** | 1.000.000.000 | 308 | **20%** (từ 0) | **30%** (từ 0) | 50% 1–3 viên | 66.421.000 | **215.652** |
| **Bojack (-320) + 4 đệ** | 350.000.000 | 108 | **3%** mỗi con (từ 0) | 5% mỗi con | giữ NR6+NR7 | 50.185.000 | 464.676 |
| **Siêu Bojack (-321)** | 500.000.000 | 154 | **10%** (từ 0) | 15% (từ 0) | giữ | 33.210.000 | 215.649 |
| **TĐST Namek (5 con)** | 18.000.000 | 5,5 | — | — | giữ NR6+NR7 | 0 | — |

> Ba điều chỉnh bắt buộc đi kèm bảng trên:
> - **Cắt vòi Ngọc xanh:** `trai_dat/BOJACK.java:26–35`, `SUPER_BOJACK.java`, `tieu_doi_sat_thu_namek/TDT_NM.java:33–42` đổi `Util.nextInt(5, 20)` → `Util.nextInt(1, 3)` và `Util.nextInt(1,5)` → `Util.nextInt(1,2)`. Bojack rơi từ ~500 xuống **~40 ngọc/lượt**; TĐST Namek từ ~100 xuống **~30 ngọc/lượt**.
> - **Sửa tỉ lệ `randDoTLBoss`** (`ItemService.java:965–975`) cho khớp chú thích, hoặc sửa chú thích cho khớp code — hiện Nhẫn + Găng (2 món 500 triệu vàng) chiếm 32,5% thay vì 25%.
> - **Fide Vàng phải hạ được**: nếu giữ bom 2,1 tỉ xuyên giáp thì 20% Thần Linh cũng vô nghĩa vì không ai kết liễu được nó.

#### 6.3.c Kiểm tra lại cân bằng sau khi sửa

| Boss | vàng / người-phút (cũ) | **(mới)** | So với chuẩn mới (Siêu Bọ Hung = 162.365) |
|---|---:|---:|---:|
| Mabư 12h chuỗi | 255.415 | **~234.000** (vàng thuần, không có Thần Linh) | ×1,44 — chấp nhận được, vì đã bỏ được nguồn Thần Linh vô hạn |
| Cooler | 151.104 | **46.470** | ×0,29 |
| Siêu Bọ Hung | 123.662 | **162.365** | ×1,00 (chuẩn mới) |
| Fide Vàng | 0 | **215.652** | ×1,33 — hợp lý, boss chỉ sống 1 giờ/ngày |
| Black Goku / Cumber | 26.443 | **13.010** | ×0,08 — **cần xem lại**, xem §8 câu hỏi 3 |
| Baby | 18.076 | **5.694** + cải trang | ×0,04 — **cần xem lại** |
| Xên bọ hung (-100) | 407 | **1.075.000** *(HP mới 5,58M, vàng 6M)* | boss nhiệm vụ, không so bằng thang này |

> ⚠️ Black Goku, Cumber và Baby sau khi bị gỡ Thần Linh sẽ trở thành boss "đắt mà bèo" (đúng vấn đề §4.3 nhưng đảo chiều). **Đây là mâu thuẫn không thể tránh nếu vừa giữ chúng trong tuyến vừa giữ HP tỉ.** Hai lối thoát ở §8 câu hỏi 1.

### 6.4 Cơ chế phân biệt "giết boss vì nhiệm vụ" và "farm boss"

Đây là cách rẻ nhất để đạt được yêu cầu *"boss nhiệm vụ chỉ rơi đồ nhiệm vụ"* mà **không phải nhân đôi số boss**.

**Ý tưởng:** cùng một con boss, nhưng **bảng rơi đồ phụ thuộc vào việc người kết liễu có đang ở đúng bước nhiệm vụ của boss đó hay không.**

```java
// Đề xuất thêm vào RewardService.java
public boolean isQuestKill(Player pl, Boss boss) {
    if (pl == null || pl.playerTask == null) return false;
    TaskMain t = pl.playerTask.taskMain;
    // tra bảng tĩnh: bossId + currentLevel -> (taskId, index)
    int[] step = QUEST_BOSS_STEP.get(key(boss.id, boss.currentLevel));
    return step != null && t.id == step[0] && t.index == step[1];
}
```

Áp vào `Boss.reward(Player plKill)` (`Boss.java:652`) và các bản override:

```java
@Override
public void reward(Player plKill) {
    TaskService.gI().checkDoneTaskKillBoss(plKill, this);          // luôn chạy
    if (RewardService.gI().isQuestKill(plKill, this)) {
        RewardService.gI().dropQuestPack(this, plKill);            // vàng + nguyên liệu, KHÔNG trang bị
    } else {
        RewardService.gI().dropFarmPack(this, plKill);             // bảng farm hiện tại
    }
}
```

| Trạng thái người kết liễu | Nhận gì |
|---|---|
| **Đang ở đúng bước nhiệm vụ** của boss này | Gói nhiệm vụ: vàng theo §6.3.a + nguyên liệu tiêu hao. **Không** trang bị, **không** Thần Linh, **không** Ngọc Rồng, **không** Ngọc xanh |
| **Đã qua bước đó** (hoặc chưa tới) | Gói farm: bảng thưởng thế giới bình thường |
| **Chưa tới bước đó** | Gói farm — nhưng nếu boss thuộc tầng "khó" (P2) thì người chơi chưa đủ sức kết liễu nên không đáng lo |

Ba biến thể tuỳ chủ dự án chọn:

| Phương án | Ưu | Nhược |
|---|---|---|
| **A — theo bước nhiệm vụ** (mô tả trên) | Không thêm bảng DB, không thêm boss, sửa ~15 file | Người chơi có thể **bỏ qua bước nhiệm vụ** rồi quay lại farm gói xịn → mất ý nghĩa nếu boss đó vẫn dễ |
| **B — giới hạn lần đầu/ngày** (`player_boss_kill(player_id, boss_id, count, last_reset)`) | Chặn được cày lặp (nhất là Mabư 12h) | Thêm 1 bảng DB + cache |
| **C — boss bản nhiệm vụ riêng** (id -2100…-2199, sao chép tạo hình, HP theo §6.2.a, chỉ rơi đồ nhiệm vụ) | **Sạch nhất**, boss thế giới không bị đụng gì, người chơi cũ không bị ảnh hưởng | ~14 class boss mới + 14 `BossData` + logic spawn theo bước nhiệm vụ (mẫu `GoldenFrieza.joinMap()`) |

> **Khuyến nghị: C cho 6 boss quan trọng nhất (Cooler, Black Goku, Baby, Cumber, Xên bọ hung, Mabư 14h), A cho phần còn lại, B riêng cho nhóm Mabư 12h.**

### 6.5 Danh sách file / hàm phải sửa

| # | File | Hàm / dòng | Sửa gì |
|---|---|---|---|
| 1 | `SRC/src/nro/models/boss/BossesData.java` | các khai báo `BossData` (dòng ghi ở §2.1) | HP / dame theo §6.2.a; `mapJoin` theo §6.2.b |
| 2 | `SRC/src/nro/models/boss/Boss.java` | `die(Player)` **632–649**, `reward(Player)` **652–654** | Thêm bảng đóng góp sát thương; gọi `checkDoneTaskKillBoss` cho **mọi người đóng góp ≥ 5%**; tách `dropQuestPack` / `dropFarmPack` |
| 3 | `SRC/src/nro/models/services/TaskService.java` | `checkDoneTaskKillBoss` **394–474** | Thêm `case` cho: **-29 Cooler, -925 Baby, -1822 Broly, -82282 Super Broly, -203999 Cumber, -203 Black Goku, -311…-315 TĐST Namek, -320/-321 Bojack, -208/-207 Dr Lychee/Hatchiyack, -502 Fide Vàng**, và boss mới **-2000, -2001, -108108, bản sao** |
| 4 | `SRC/src/nro/models/services/RewardService.java` | thêm `isQuestKill`, `dropQuestPack`, `dropFarmPack` | §6.4 |
| 5 | `SRC/src/nro/models/services/ItemService.java` | `randDoTLBoss` **956–1086** | Sửa tỉ lệ / chú thích (§3.1); thêm tham số `tier` để chỉ boss tầng khó gọi được |
| 6 | `SRC/src/nro/models/boss/Cold/Cooler.java` | `reward` **30**, `injured` **92** | Bỏ `randDoTLBoss` + đồ cấp + NR; HP/dame theo §6.2.a; **thêm `checkDoneTaskKillBoss`** |
| 7 | `SRC/src/nro/models/boss/Black_Goku/BlackGoku.java` | `reward` **30** | Bỏ Thần Linh + đồ cấp |
| 8 | `SRC/src/nro/models/boss/cumber/Cumber.java` | `reward` **31** | Bỏ Thần Linh + đồ cấp |
| 9 | `SRC/src/nro/models/boss/Baby/Baby.java` | `reward` **24**, `injured` **108** | Bỏ Thần Linh + đồ cấp; giữ cải trang 1%; **thêm `checkDoneTaskKillBoss`**; sửa `BABY_3.dame = 30000` → 300.000 hoặc theo §6.2.a |
| 10 | `SRC/src/nro/models/boss/Cell/SieuBoHung.java` | `reward` **79** | Đồ cấp **30% → 10%**, Thần Linh **5% → 8%** |
| 11 | `SRC/src/nro/models/boss/Cell/XenBoHung.java` | `reward` **31** | Bỏ NR, tăng vàng |
| 12 | `SRC/src/nro/models/boss/MajinBuu_12h/{Drabura,BuiBui,BuiBui2,Yacon,Mabu,Drabura2,Drabura3,Goku,Cadic}.java` | `reward` (≈ dòng 38) và `die()`/`afk()` | Bỏ Thần Linh + đồ cấp + NR; bỏ hồi sinh 60 giây vô hạn; **`Drabura2` hiện không chạy qua `die()` nên không trao thưởng và không trao nhiệm vụ** (docs/11 §9.6) — phải sửa |
| 13 | `SRC/src/nro/models/boss/MajinBuu_14h/Mabu2H.java` | `reward` **119**, `injured` **127** | Thêm phần thưởng; giảm dame 500.000 → 24.000 |
| 14 | `SRC/src/nro/models/boss/trai_dat/BOJACK.java` + `BUJIN/KOGU/ZANGYA/BIDO/SUPER_BOJACK.java` | `reward` **25** (BOJACK) | `nextInt(5,20)` → `nextInt(1,3)`; thêm Thần Linh 3%; **thêm `checkDoneTaskKillBoss`** |
| 15 | `SRC/src/nro/models/boss/tieu_doi_sat_thu_namek/{TDT_NM,SO1_NM…SO4_NM}.java` | `reward` **32** | `nextInt(1,5)` → `nextInt(1,2)`; **thêm `checkDoneTaskKillBoss`** |
| 16 | `SRC/src/nro/models/boss/Golden_fireza/GoldenFrieza.java` | `reward` **41**, `injured` **60**, chu kỳ bom | Thêm Thần Linh 20% + đồ cấp 30%; giảm bom; **thêm `checkDoneTaskKillBoss`** |
| 17 | `SRC/src/nro/models/boss/Broly/Broly.java` | `die` **176–178**, `joinMap` **55–62** | `die()` phải gọi `reward()`; HP random theo §6.2.a |
| 18 | `SRC/src/nro/models/boss/Broly/SuperBroly.java` | `reward` **52–56**, `joinMap` **64–67** | **Thêm `checkDoneTaskKillBoss`** (NV 41 hiện **không thể hoàn thành**) |
| 19 | `SRC/src/nro/models/boss/khi_gas/{DrLychee,Hatchiyack}.java` | `reward` | **Thêm `checkDoneTaskKillBoss`** (NV 43 hiện không thể hoàn thành) |
| 20 | `SRC/src/nro/models/boss/nhan_ban/NhanBan.java` | `reward` **34–39** | **Thêm `checkDoneTaskKillBoss`** (NV 31 hiện không thể hoàn thành) |
| 21 | `SRC/src/nro/models/boss/yardrat/Yardart.java` | `reward` **85** | Boss không bao giờ chết mà vẫn thưởng Bí kiếp 1/5–1/2 mỗi lượt — xem lại tỉ lệ |
| 22 | `SRC/src/nro/models/boss/Boss_Manager/BossManager.java` | `loadBoss()` **141–165** | Xên bọ hung 1 → 2–3 con; Siêu Bọ Hung 1 → 2 con; xem lại Kuku/MĐĐ/Rambo ×5 |
| 23 | `SRC/src/nro/models/boss/Android/DrKore.java` | `injured(Player,int,…)` | **Sai chữ ký** (`int` thay vì `long`) → hấp thụ chưởng không chạy (docs/11 §9.3). Sửa hoặc bỏ hẳn |

---

## 7. Cảnh báo về cơ chế thưởng boss hiện tại

### 7.1 Chỉ người kết liễu nhận thưởng — không có top sát thương

`Boss.die(plKill)` (`Boss.java:632–649`) trao **toàn bộ** phần thưởng cho **một người duy nhất** — người gây đòn cuối. Mọi `ItemMap` đều tạo với `plKill.id` làm chủ (docs/11 §0, §9.14). Không có cơ chế top sát thương ở bất kỳ boss thế giới nào.

**Hậu quả với farm boss:**

| Vấn đề | Mức độ |
|---|---|
| **"Cướp boss"**: một người chờ boss gần chết rồi vào đánh 1 đòn ăn trọn. Ở Black Goku (4 tỉ HP hiệu dụng) người này bỏ ra 0,000001% công sức và nhận 100% phần thưởng | 🔴 Nghiêm trọng |
| **Không ai muốn đánh boss dai**: 20 người đánh Baby 10 tiếng, 19 người ra về tay không | 🔴 Nghiêm trọng |
| **Bot / macro có lợi thế tuyệt đối** vì chỉ cần canh đòn cuối. `if (!plKill.isBot)` chỉ chặn bot **của server**, không chặn client tự động | 🔴 |
| **Ngoại lệ tốt:** Dr Lychee / Hatchiyack rơi tự do (`-1`) với số lượng `1 + 2 × số người trong khu` (11b §184) — đây là **mẫu đúng duy nhất trong codebase**, nên nhân rộng | 🟢 |
| Yardart trao thưởng cho **người đánh**, không phải người kết liễu (`Yardart.java:85`) — cũng là mẫu đúng | 🟢 |

**Đề xuất:** thêm `Map<Long, Long> damageContribution` vào `Boss`, cập nhật trong `injured()`, và trong `die()`:
- `checkDoneTaskKillBoss` gọi cho **mọi người đóng góp ≥ 5%**;
- item chính rơi cho **top 1 sát thương** (không phải người kết liễu);
- vàng rơi tự do (`-1`) với số lượng chia theo số người đóng góp — theo mẫu Dr Lychee.

### 7.2 Boss không gọi `checkDoneTaskKillBoss`

Quét toàn bộ `SRC/src/nro/models/boss/**` (`grep -rl "public void reward"` đối chiếu `grep -c "checkDoneTaskKillBoss"`), **45 file có `reward()` nhưng không gọi `checkDoneTaskKillBoss`**. Trong đó **ảnh hưởng trực tiếp tới tuyến nhiệm vụ mới**:

| NV | Boss | File | Hiện trạng |
|---|---|---|---|
| **31** | Bản sao người chơi | `nhan_ban/NhanBan.java:34` | ❌ không gọi |
| **34** | Cooler (-29) | `Cold/Cooler.java:30` | ❌ không gọi |
| **39** | Baby (-925) | `Baby/Baby.java:24` | ❌ không gọi |
| **41** | Broly (-1822) | `Broly/Broly.java:176` | ❌ **`die()` không gọi cả `reward()`** |
| **41** | Super Broly (-82282) | `Broly/SuperBroly.java:52` | ❌ không gọi |
| **43** | Dr Lychee / Hatchiyack | `khi_gas/DrLychee.java`, `Hatchiyack.java` | ❌ không gọi |
| 36 | Drabura 2 (-237) | `MajinBuu_12h/Drabura2.java` | ❌ **`die()` không bao giờ chạy** (đòn chí mạng → `AFK` → `DIE`, bỏ qua `die()`) — docs/11 §9.6 |

Ngoài ra không gọi: toàn bộ Bojack (`trai_dat/*`), TĐST Namek, Fide Vàng, Ma vương Pôcôlô, Trung Uý Xanh Lơ (BĐKB), Ninja Áo Tím / Rôbốt Vệ Sĩ / Trung Uý Thép / Trung Uý Xanh Lơ (doanh trại), Yardart, toàn bộ boss sự kiện và boss mini.

**Bảng `checkDoneTaskKillBoss` hiện có** (`TaskService.java:397–472`) chỉ phủ 27 boss id và gắn cứng vào **task 19–28 của tuyến cũ**. Tuyến mới đánh số lại hoàn toàn (NV 0–50) → **toàn bộ bảng này phải viết lại**, không chỉ thêm case.

### 7.3 Ảnh hưởng tới tuyến nhiệm vụ mới

| Hệ quả | Chi tiết |
|---|---|
| **7 bước nhiệm vụ không thể hoàn thành** ngay cả khi giết được boss | NV 31 (bản sao), NV 34 (Cooler), NV 39 (Baby), NV 41 (Broly, Super Broly), NV 43 (Hatchiyack), NV 36 (Drabura 2 nếu dùng) |
| **Tuyến đứt ở NV 31** → không ai qua được chương 4 | Đây là **lỗi chặn (blocker) số 1** phải sửa trước khi mở tuyến |
| **Bước "giết boss" trở thành cuộc đua cướp đòn cuối** | Ở NV 22, 23, 30, 34, 38, 39, 42 — mỗi con boss 1 con/server, nghỉ 5–30 phút. Với 200 người cùng làm NV 30, **mỗi 30 phút chỉ 1 người qua được bước** → xếp hàng **100 giờ**. Đây là lý do bắt buộc phải đổi sang "tính cho mọi người đóng góp ≥ 5%" (§7.1) |
| **Người chơi mạnh farm sạch boss nhiệm vụ** khiến người yếu không bao giờ thấy boss | Boss thế giới không phân khu theo cấp; người 90 tỉ SM một mình xóa boss trong vài giây, người ở NV 30 không kịp gây 1 đòn |
| **Bot ưu thế tuyệt đối** ở các bước A2 | Vì chỉ cần đòn cuối |

> **Khuyến nghị ưu tiên cao nhất:** trước khi làm bất kỳ chỉnh số nào ở §6, phải làm xong **hai việc**:
> 1. `Boss` ghi bảng đóng góp sát thương và `checkDoneTaskKillBoss` chạy cho mọi người đóng góp ≥ 5%.
> 2. Viết lại toàn bộ bảng `checkDoneTaskKillBoss` theo đánh số NV mới, phủ đủ 23 boss của tuyến.
>
> Không có hai thứ này thì mọi chỉnh số HP đều vô nghĩa.

---

## 8. Ghi chú / điểm cần chủ dự án quyết

### Sáu câu hỏi cần trả lời trước khi viết code

**1. Hạ HP boss thế giới, hay dựng bản nhiệm vụ riêng?**
Bảng §6.2.a đề xuất giảm HP trung bình **~90 lần** cho 23 boss. Nếu làm thẳng trên boss hiện có thì người chơi cũ (SM hàng chục tỉ) sẽ thấy Black Goku và Baby "nát" trong vài giây, và toàn bộ nội dung endgame biến mất. Ba lựa chọn:
- **(a)** Hạ HP thật — nhanh, rẻ, nhưng phá endgame.
- **(b)** Dựng boss bản nhiệm vụ id `-2100…-2199` (phương án C ở §6.4) — ~14 class mới, giữ nguyên endgame. **Khuyến nghị.**
- **(c)** Giữ boss nguyên, **nâng mốc SM của file 20** cho khớp (ví dụ NV 38 Black Goku phải ở mốc ~200 tỉ SM) — bất khả thi vì trần SM là 90 tỉ ở `limitPower = 9`.

**2. Đồ Thần Linh nên rơi từ đâu sau khi gỡ khỏi Cooler / Black Goku / Baby / Cumber / Mabư 12h?**
Đề xuất của tôi (§6.3.b): **Siêu Bọ Hung 8%, Fide Vàng 20%, Siêu Bojack 10%, Bojack 3%/con**. Cần chủ dự án xác nhận vì nó **đổi hẳn map farm chính của server** từ map 110/102/14/155 (Cooler, Black Goku, Baby, Cumber) sang map 103/6/3–6 (Siêu Bọ Hung, Fide Vàng, Bojack).

**3. Black Goku / Cumber / Baby sau khi mất Thần Linh có còn lý do tồn tại?**
Với 4 tỉ và 17 tỉ HP hiệu dụng mà chỉ rơi vàng, chúng sẽ bị bỏ hoang. Ba hướng: **(a)** giảm HP xuống thang cốt truyện và chấp nhận chúng là boss cốt truyện thuần; **(b)** giữ HP và cho rơi **thứ khác** (Ngọc Rồng Sao Đen, sao pha lê cao cấp, bông tai Porata); **(c)** giữ Thần Linh nhưng **gỡ chúng ra khỏi tuyến nhiệm vụ** (đổi NV 38/39/42 sang boss khác).

**4. Có chấp nhận thêm bảng DB `player_boss_kill` không?**
Cần cho phương án B (§6.4) — thứ duy nhất chặn được việc cày lặp nhóm Mabư 12h (hồi sinh 60 giây vô hạn). Nếu không muốn thêm bảng thì bắt buộc phải **bỏ cơ chế hồi sinh `AFK` 60 giây** của nhóm Mabư 12h.

**5. Ngọc xanh từ Bojack và TĐST Namek — cắt bao nhiêu?**
Hiện ~500 ngọc/15 phút (Bojack) và ~100 ngọc/5 phút (TĐST Namek). Đề xuất cắt còn ~40 và ~30. Đây là **tiền tệ nạp**, nên con số này ảnh hưởng trực tiếp tới doanh thu — chủ dự án phải quyết, tôi chỉ ghi nhận rằng hiện tại nó **không gắn với độ khó** chút nào (TĐST Namek chỉ 5,5 người-phút).

**6. Ba boss mới trong file 20 (Kẻ Thu Gom -2000, Jaco -2001, Heart -108108) có giữ chỉ số do 20a/20c đề xuất không?**
Theo mô hình §1, cả ba đều **quá dai**: Kẻ Thu Gom gấp 6 lần, Jaco gấp **26 lần**, Heart gấp **28 lần** mức hợp lý. Nguyên nhân: 20a/20c ước lượng thời gian đánh mà không dùng công thức `getDameAttack`. Bảng §6.2.a có số thay thế.

### Ghi nhận thêm (không cần quyết ngay, nhưng nên biết)

| # | Ghi nhận | Nguồn |
|---|---|---|
| 1 | `BossData.hp` và `dame` là `int` → **trần 2.147.483.647**. Super Black Goku / Super Cumber / Baby / Heart đều đã sát trần. Không thể làm boss "dai hơn" bằng HP nữa | `BossesData.java`, 20c §5.5 |
| 2 | `BABY_3.dame = 30000` trong khi `BABY = 200000` và `BABY_2 = 250000` — gần như chắc chắn là **lỗi gõ thiếu một số 0** | `BossesData.java:2672` |
| 3 | Chú thích tỉ lệ trong `randDoTLBoss` **sai so với code** (15%/20%/30%/25% ghi vs 22,5%/30,4%/27,8%/9,3% thật) | `ItemService.java:965–975` |
| 4 | Chú thích `// 30% xác suất để rơi đồ` xuất hiện ở **cả Cooler và Baby** nhưng code là `isTrue(5, 100)` — cho thấy 30% của Siêu Bọ Hung là **số gốc bị bỏ quên**, không phải thiết kế | `Cooler.java:50`, `Baby.java:57`, `SieuBoHung.java:96` |
| 5 | Nhịp tick `BossManager` là **1.500 ms** → các skill khai báo cooldown 100 ms (Baby, Black Goku, Cumber, Heart) thực tế chỉ bắn được mỗi ~1,5 giây. Đừng đặt cooldown dưới 1.500 | docs/11 §9.16 |
| 6 | `Boss.injured` mặc định **không trừ giáp** → Nappa, TĐST, Fide, Android, King Kong, Bojack nhận trọn sát thương. Giáp người chơi thành vô dụng khi đánh boss, nhưng đồng thời làm các boss này dễ hơn số liệu gợi ý | docs/11 §9.18 |
| 7 | Điều kiện `if (mapId != 140 \|\| !isMapMaBu \|\| !isMapDoanhTrai \|\| !isMapBanDoKhoBau)` trong `Boss.die()` **gần như luôn đúng** (biểu thức `\|\|`) → nhánh `else` không bao giờ chạy | `Boss.java:634–637`, docs/11 §9.1 |
| 8 | Trùng `BossID`: `SOI_HEC_QUYN1 = SOI_HEC_QUYN = -77`; `O_DO1 = O_DO = -78`; `Virut = XINBATO = -79` (và Mặt Trời cũng -79); `HAKAI = DEATH_BEAM_5 = -613`; Rôbốt Vệ Sĩ trùng Ninja Áo Tím (-9…-11). Nếu dùng `boss.id` làm khóa trong bảng nhiệm vụ mới thì **sẽ nhầm boss** | docs/11 §9.4 |
| 9 | Dải id `-2000…-2099` (20a) và `-2100…-2199` (đề xuất của tôi) đều **còn trống** — an toàn | docs/11 §1 |
| 10 | Xên bọ hung (-100) và Siêu Bọ Hung (-101) mỗi con **chỉ 1 toàn server** (`BossManager.java:147–148`), nghỉ 30 phút, **không có `autoLeaveMap`** (Xên bọ hung). Với tuyến mới đẩy cả server vào NV 30, đây là nút cổ chai nặng nhất — 20b (dòng ghi chú NV 30) đã đề xuất tăng lên 2–3 con | `BossManager.java:141–165` |
| 11 | Nhóm Mabư 12h, Yardart, Tàu Pảy Pảy được tạo bởi `Map.initBoss` **cho mỗi khu** → số lượng nhân theo số khu của map. Nhóm Mabư 12h vì thế có **nhiều bản song song**, làm hiệu quả farm ở §4.1 còn cao hơn con số 255.415 vàng/người-phút đã tính | docs/11 §3.4 |
| 12 | Boss có `bossesAppearTogether` (TĐST, Android, King Kong, Bojack) chỉ kích hoạt theo chuỗi AFK → **thời gian thật để hạ trọn nhóm dài hơn** con số người-phút ở §2.2 vì phải chờ chuyển trạng thái | docs/11 §5.2, §5.7 |

---

*Tài liệu này chỉ kiểm toán. Mọi thay đổi code phải qua xác nhận của chủ dự án theo §8.*
