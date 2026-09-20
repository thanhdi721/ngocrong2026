# 35 — Tỉ lệ rơi đồ theo sức mạnh boss

Tài liệu này ghi lại hai thay đổi cân bằng rơi đồ:

1. **Đồ Thần Linh** — bỏ toàn bộ số cứng, tính tỉ lệ theo sức mạnh thật của boss, luôn nằm trong 1–5%.
2. **Hai boss mini Ở Dơ và Sói hẹc quyn** — hạ "Hộp quà Goku Day" từ 100% xuống 5% mỗi món.

---

## 1. Hiện trạng trước khi sửa

Có đúng **14 chỗ** gọi `ItemService.gI().randDoTLBoss(...)`, mỗi chỗ bọc trong một
`Util.isTrue(X, 100)` với con số tự đặt, không liên quan gì tới độ khó:

| Tệp | Tỉ lệ cũ |
|---|---|
| `boss/Baby/Baby.java` | 10% |
| `boss/Black_Goku/BlackGoku.java` | 5% |
| `boss/cumber/Cumber.java` | 5% |
| `boss/Cold/Cooler.java` | 5% |
| `boss/Cell/SieuBoHung.java` | 5% |
| `boss/MajinBuu_12h/Mabu.java` | 1% |
| `boss/MajinBuu_12h/Goku.java` | 1% |
| `boss/MajinBuu_12h/Cadic.java` | 1% |
| `boss/MajinBuu_12h/Yacon.java` | 1% |
| `boss/MajinBuu_12h/BuiBui.java` | 1% |
| `boss/MajinBuu_12h/BuiBui2.java` | 1% |
| `boss/MajinBuu_12h/Drabura.java` | 1% |
| `boss/MajinBuu_12h/Drabura2.java` | 1% |
| `boss/MajinBuu_12h/Drabura3.java` | 1% |

Hai chỗ chướng mắt nhất:

- **Cooler** (200 triệu máu, không có lớp giảm sát thương nào) rơi đồ **ngang bằng**
  Super Black Goku hình dạng 2 (hơn 5 tỉ máu hiệu dụng) — chênh nhau gần 8 lần công sức
  mà phần thưởng như nhau.
- **Baby**, con khó nhất game, rơi **gấp đôi** mọi con khác chỉ vì ai đó gõ số 10.

---

## 2. Cách tính mới

Lớp mới: **`SRC/src/nro/models/boss/BossDropRate.java`**.

```
máu hiệu dụng = máu danh nghĩa của hình dạng đang đánh ÷ (1 − tổng % giảm sát thương)
```

### 2.1. Máu danh nghĩa lấy ở đâu

`BossDropRate.nominalHp(boss)` đọc `boss.nPoint.hpg` — con số máu đã được `Boss.initBase()`
bốc ra thật cho lượt này từ mảng `BossData.hp`. Nếu `nPoint` chưa khởi tạo (dữ liệu hỏng)
thì quay về đọc thẳng `BossData` của hình dạng hiện tại và lấy phần tử lớn nhất.

`reward(plKill)` được gọi từ `Boss.die()` **trước khi** boss đổi hình dạng, nên `currentLevel`
lúc đó vẫn trỏ đúng vào hình dạng vừa bị hạ. Đây chính là "hình dạng đang đánh".

### 2.2. Tổng % giảm sát thương — gộp hai lớp

Trong `injured()` của boss có **hai** lớp giảm khác nhau, và chúng **nhân tiếp nhau** chứ
không cộng:

| Lớp | Nguồn | Hàm đọc |
|---|---|---|
| Lớp cũ, viết cứng trong `injured()` của từng boss (`damage /= 2`, `×0,7 /2`, `damage / 3`) | chính lớp boss | `Boss.getLegacyDamageReducePercent()` — **mới thêm** |
| Lớp mới theo bảng cân bằng | `boss/BossDamageReduce.java` | `Boss.getDamageReducePercent()` — đã có |

```
còn lại = (1 − lớp_cũ) × (1 − lớp_bảng)
tổng % giảm = 1 − còn lại
```

`Boss.getLegacyDamageReducePercent()` mặc định trả `0`. Bốn lớp con ghi đè:

| Lớp | Phép chia trong `injured()` | % khai báo |
|---|---|---|
| `Baby` | `damage = damage × 0,7` rồi `damage / 2` → còn 35% | **65** (mọi hình dạng) |
| `BlackGoku` | `if (currentLevel != 0) damage /= 2` | **50** khi `currentLevel != 0`, ngược lại 0 |
| `Cumber` | y hệt Black Goku | **50** khi `currentLevel != 0`, ngược lại 0 |
| `SieuBoHung` | `subDameInjureWithDeff(damage / 3)` → còn ~33% | **66** (làm tròn xuống từ 66,67 cho an toàn) |

Chỉ khai báo phần **luôn luôn** áp dụng. Những nhánh có điều kiện — khiên đỡ
(`damage / 4`, `damage = 1`), né đòn (`tlNeDon`), trần sát thương mỗi đòn — **không** tính
vào đây vì không phải lần nào cũng xảy ra (xem §5).

### 2.3. Thang bậc

| Máu hiệu dụng | Tỉ lệ |
|---|---|
| dưới 50 triệu | 1% |
| 50 – 200 triệu | 2% |
| 200 – 700 triệu | 3% |
| 700 triệu – 2 tỉ | 4% |
| từ 2 tỉ trở lên | 5% |

**Giữ nguyên thang bậc chủ dự án đề xuất, không chỉnh.** Lý do: khi cắm chỉ số thật vào,
thang này đã chia đàn boss ra đúng 5 nhóm có ý nghĩa và không nhóm nào rỗng —

- 1% rơi vào lũ tay chân Mabư 12h (20–40 triệu, hạ trong vài chục giây);
- 2% rơi vào nhóm giữa của chuỗi 12h (50–100 triệu);
- 3% rơi vào Cooler và Siêu Bọ Hung (200–600 triệu);
- 4% rơi vào hình dạng đầu của Black Goku / Cumber (714 triệu);
- 5% rơi vào Baby và hình dạng cuối của Black Goku / Cumber (hơn 5 tỉ).

Các mốc cũng rơi đúng vào chỗ trống giữa các cụm chỉ số thật, nên chỉnh máu boss lên xuống
vài phần trăm sẽ không làm tỉ lệ nhảy bậc bất ngờ. Mốc duy nhất sát ranh là Black Goku /
Cumber hình dạng 1 (714 triệu so với mốc 700 triệu) — xem §5.

Kết quả **luôn** bị kẹp trong `MIN_PERCENT = 1` … `MAX_PERCENT = 5`, kể cả khi ai đó nhập
máu 0, máu âm, hay % giảm ≥ 100.

### 2.4. Điểm mấu chốt: không có bảng tra id

`BossDropRate` **không** chứa một `switch` hay `Map` nào theo `BossID`. Nó chỉ đọc
`nPoint.hpg`, `getDamageReducePercent()` và `getLegacyDamageReducePercent()` của chính con
boss được truyền vào. Hệ quả:

- Sửa `BossesData.BABY` từ 2 tỉ xuống 800 triệu → Baby tự tụt từ 5% xuống 4%, không phải
  sửa dòng nào trong `BossDropRate`.
- Bật `BossDamageReduce.COOLER_TG = {25, 35}` → Cooler hình dạng 2 lên 769 triệu máu hiệu
  dụng và tự nhảy lên 4%.
- Tắt `BossDamageReduce.WORLD_BOSS_ENABLED` → Black Goku hình dạng 1 tụt về 500 triệu và
  tự xuống 3%.

---

## 3. Bảng kiểm chứng từng boss

Số liệu dưới đây do chương trình kiểm chứng in ra, gọi thẳng
`BossDropRate.percentForEffectiveHp(...)` và đọc thẳng hằng số `BossDamageReduce`
(`WORLD_BOSS_ENABLED = true`).

| Boss (hình dạng) | id / hằng số | Máu danh nghĩa | % giảm (lớp cũ) | % giảm (bảng) | Tổng % giảm | Máu hiệu dụng | Tỉ lệ cũ | **Tỉ lệ mới** |
|---|---|---:|---:|---:|---:|---:|---:|---:|
| Baby hd1 | `-925` | 2.000.000.000 | 65 | 0 | 65% | 5.714.285.714 | 10% | **5%** |
| Baby hd2 | `-925` | 2.000.000.000 | 65 | 0 | 65% | 5.714.285.714 | 10% | **5%** |
| Baby hd3 | `-925` | 2.000.000.000 | 65 | 0 | 65% | 5.714.285.714 | 10% | **5%** |
| Black Goku hd1 | `-203` | 500.000.000 | 0 | 30 | 30% | 714.285.714 | 5% | **4%** |
| Super Black Goku hd2 | `-203` | 2.000.000.000 | 50 | 25 | 63% | 5.405.405.405 | 5% | **5%** |
| Cumber hd1 | `-203999` | 500.000.000 | 0 | 30 | 30% | 714.285.714 | 5% | **4%** |
| Super Cumber hd2 | `-203999` | 2.000.000.000 | 50 | 25 | 63% | 5.405.405.405 | 5% | **5%** |
| Cooler hd1 | `-29` | 200.000.000 | 0 | 0 | 0% | 200.000.000 | 5% | **3%** |
| Cooler hd2 | `-29` | 500.000.000 | 0 | 0 | 0% | 500.000.000 | 5% | **3%** |
| Xên Hoàn Thiện hd1 | `SIEU_BO_HUNG_1` | 150.000.000 | 66 | 0 | 66% | 441.176.470 | 5% | **3%** |
| Siêu Bọ Hung hd2 | `SIEU_BO_HUNG_2` | 200.000.000 | 66 | 0 | 66% | 588.235.294 | 5% | **3%** |
| Mabư 12h | `MABU_12H` | 100.000.000 | 0 | 0 | 0% | 100.000.000 | 1% | **2%** |
| Gôku (12h) | `GOKU` | 60.000.000 | 0 | 0 | 0% | 60.000.000 | 1% | **2%** |
| Ca Đít (12h) | `CADIC` | 60.000.000 | 0 | 0 | 0% | 60.000.000 | 1% | **2%** |
| Ya côn | `YACON` | 50.000.000 | 0 | 0 | 0% | 50.000.000 | 1% | **2%** |
| Bui Bui | `BUI_BUI` | 40.000.000 | 0 | 0 | 0% | 40.000.000 | 1% | **1%** |
| Bui Bui 2 | `BUI_BUI_2` | 40.000.000 | 0 | 0 | 0% | 40.000.000 | 1% | **1%** |
| Drabura | `DRABURA` | 20.000.000 | 0 | 0 | 0% | 20.000.000 | 1% | **1%** |
| Drabura 2 | `DRABURA_2` | 20.000.000 | 0 | 0 | 0% | 20.000.000 | 1% | **1%** |
| Drabura 3 | `DRABURA_3` | 20.000.000 | 0 | 0 | 0% | 20.000.000 | 1% | **1%** |

### Kiểm tra lại ba điều kiện chủ dự án đặt ra

1. **Không con nào ra ngoài 1–5%** — cột cuối chỉ có các giá trị 1, 2, 3, 4, 5. Chương
   trình kiểm chứng còn ném lỗi nếu gặp giá trị ngoài khoảng, và chạy sạch.
2. **Boss khó hơn thì tỉ lệ cao hơn** — sắp theo máu hiệu dụng giảm dần:
   Baby (5,71 tỉ → 5%) ≥ Super Black Goku / Super Cumber (5,41 tỉ → 5%) >
   Black Goku / Cumber hd1 (714 tr → 4%) > Siêu Bọ Hung (588 tr → 3%) >
   Cooler hd2 (500 tr → 3%) > Xên Hoàn Thiện (441 tr → 3%) > Cooler hd1 (200 tr → 3%) >
   Mabư 12h (100 tr → 2%) > Gôku / Ca Đít (60 tr → 2%) > Ya côn (50 tr → 2%) >
   Bui Bui (40 tr → 1%) > Drabura (20 tr → 1%). Không có chỗ nào bị đảo.
3. **Giá trị biên** — `percentForEffectiveHp(0)` trả 1, `percentForEffectiveHp(Long.MAX_VALUE)`
   trả 5.

### Thay đổi đáng chú ý

- **Baby 10% → 5%**: giảm một nửa. Đây là con duy nhất bị hạ mạnh, vì 10% vốn là con số
  không có căn cứ.
- **Cooler 5% → 3%** và **Siêu Bọ Hung 5% → 3%**: đúng theo nhận xét
  `docs/3-kiem-tra-can-bang/21c-boss-roi-do-can-bang.md` §5.2 ② rằng Cooler "thưởng quá hậu
  so với độ khó".
- **Mabư 12h / Gôku / Ca Đít / Ya côn 1% → 2%**: tăng nhẹ. Bốn con này có 50–100 triệu máu,
  đáng lẽ không nên bị xếp ngang Drabura 20 triệu máu. Tổng kỳ vọng cho một lượt chạy trọn
  chuỗi 12h (9 con) tăng từ 9% lên 13% — xem §5.

---

## 4. Hai boss mini rơi đồ 100%

### `boss/Boss_mini/Odo.java` — Ở Dơ

Mã cũ:

```java
int count1591 = Util.nextInt(1, 1);   // luôn = 1
int count1594 = Util.nextInt(1, 1);   // luôn = 1
for (int i = 0; i < count1591; i++) { ... drop 1591 ... }
for (int i = 0; i < count1594; i++) { ... drop 1594 ... }
```

`Util.nextInt(1, 1)` luôn trả về 1, nên mỗi lần giết là chắc chắn rơi **cả hai** hộp quà.
Nay mỗi món gieo xúc xắc riêng với hằng số `DROP_HOP_QUA_PERCENT = 5`.

### `boss/Boss_mini/SoiHecQuyn.java` — Sói hẹc quyn

Mã cũ lặp thẳng qua `int[] itemDropIds = {1591, 1594}` và thả cả hai không gieo xúc xắc.
Nay mỗi vòng lặp `continue` nếu không trúng 5%.

### Ghi chú

- **5% áp cho từng món, hai món độc lập.** Xác suất mỗi lần giết: rơi ít nhất một món
  9,75%, rơi cả hai 0,25%, không rơi gì 90,25%. Đây đúng là "mỗi món 5%" chứ **không phải**
  "5% rơi cả hai".
- **Phần cộng điểm sự kiện và cập nhật nhiệm vụ danh hiệu giữ nguyên hoàn toàn**:
  `BadgesTaskService.updateCountBagesTask(...)`, `plKill.event.addEventPoint(5)` và
  `sendThongBao(plKill, "+5 Point")` vẫn chạy mọi lần giết, không phụ thuộc kết quả gieo.
- Hai con này **không** dùng `BossDropRate` vì chúng không rơi đồ Thần Linh; hộp quà
  Goku Day là vật phẩm sự kiện, tỉ lệ do chủ dự án ấn định chứ không suy ra từ máu.

---

## 5. Chỗ nghi ngờ

1. **Chuỗi Mabư 12h chặn trần sát thương mỗi đòn, không phải giảm phần trăm.**
   `Drabura`, `Drabura2`, `Drabura3`, `Goku`, `Cadic` chặn ở 20 triệu / 10 triệu mỗi đòn,
   `Mabu` chặn ở 50 triệu. Đây là trần tuyệt đối chứ không phải tỉ lệ, nên **không** quy ra
   được "% giảm" và **không** được cộng vào máu hiệu dụng. Thực tế với người chơi sát thương
   cao thì mấy con này dai hơn con số trong bảng khá nhiều. Nếu chủ dự án thấy chuỗi 12h
   xứng đáng hơn, cách sạch nhất là đổi trần thành tỉ lệ phần trăm rồi khai báo qua
   `getLegacyDamageReducePercent()`, tỉ lệ sẽ tự lên theo.

2. **Tổng kỳ vọng của chuỗi 12h tăng từ 9% lên 13% mỗi lượt.** Chuỗi có 9 con và người chơi
   thường hạ hết cả chuỗi. Nếu thấy rộng tay quá thì hạ mốc bậc 2 (`TIER_2_HP`) lên trên
   100 triệu, khi đó Gôku / Ca Đít / Ya côn / Mabư 12h quay về 1% và tổng chuỗi còn 9%.

3. **`reward()` được gọi ở MỖI hình dạng chết, không phải chỉ hình dạng cuối.**
   `Boss.die()` → `reward()` chạy mỗi lần một hình dạng bị hạ, rồi boss mới `RESPAWN` sang
   hình dạng kế. Nghĩa là hạ trọn Baby cho **ba** lần gieo 5%, hạ trọn Black Goku cho một
   lần 4% cộng một lần 5%. Hành vi này **có từ trước**, không phải do thay đổi lần này gây
   ra, nhưng nên biết khi đọc bảng ở §3: tỉ lệ trong bảng là **cho mỗi hình dạng**, không
   phải cho mỗi trận.

4. **Black Goku / Cumber hình dạng 1 nằm sát ranh giới.** Máu hiệu dụng 714.285.714 chỉ hơn
   mốc 700 triệu đúng 2%. Chỉ cần hạ `BLACK_GOKU_TG[0]` từ 30 xuống 29 là máu hiệu dụng tụt
   còn 704 triệu (vẫn 4%), nhưng xuống 28 là còn 694 triệu → rơi về 3%. Đây là chỗ duy nhất
   trong đàn boss mà một thay đổi nhỏ làm nhảy bậc.

5. **Lớp giảm có điều kiện bị bỏ qua — cố ý.** Khiên đỡ (`damage / 4` của Baby và Siêu Bọ
   Hung, `damage = 1` của Black Goku / Cumber), né đòn `tlNeDon`, và
   `subDameInjureWithDeff` (trừ theo giáp của boss) đều **không** tính vào
   `getLegacyDamageReducePercent()`. Chúng chỉ xảy ra trong một phần số đòn nên không có
   một con số phần trăm cố định đúng. Hệ quả: máu hiệu dụng trong bảng là **cận dưới** —
   thực tế boss dai hơn con số này một chút. Vì mọi boss đều bị tính hụt theo cùng một
   kiểu nên thứ tự xếp hạng không bị ảnh hưởng.

6. **`nPoint.hpg` là `int`.** Trần tuyệt đối 2.147.483.647 cho mỗi hình dạng (đã ghi trong
   `33-giam-sat-thuong-boss.md`). Baby và Super Black Goku / Super Cumber đã cắm 2 tỉ, tức
   gần chạm trần. Muốn boss cứng hơn nữa thì phải tăng % giảm sát thương, không tăng máu —
   và `BossDropRate` xử lý cả hai đường như nhau nên không cần sửa gì thêm.

7. **Boss bản nhiệm vụ và Heart không đụng tới.** `boss/quest/**` và `boss/heart/**` không
   gọi `randDoTLBoss` và đã được giữ nguyên hoàn toàn, đúng yêu cầu.

---

## 6. Danh sách tệp đã sửa

**Mới:**

- `SRC/src/nro/models/boss/BossDropRate.java`

**Sửa:**

- `SRC/src/nro/models/boss/Boss.java` — thêm `getLegacyDamageReducePercent()` (mặc định 0)
- `SRC/src/nro/models/boss/Baby/Baby.java` — ghi đè 65, thay chỗ gọi
- `SRC/src/nro/models/boss/Black_Goku/BlackGoku.java` — ghi đè 50 / 0, thay chỗ gọi
- `SRC/src/nro/models/boss/cumber/Cumber.java` — ghi đè 50 / 0, thay chỗ gọi
- `SRC/src/nro/models/boss/Cell/SieuBoHung.java` — ghi đè 66, thay chỗ gọi
- `SRC/src/nro/models/boss/Cold/Cooler.java` — thay chỗ gọi
- `SRC/src/nro/models/boss/MajinBuu_12h/{Mabu,Goku,Cadic,Yacon,BuiBui,BuiBui2,Drabura,Drabura2,Drabura3}.java` — thay chỗ gọi
- `SRC/src/nro/models/boss/Boss_mini/Odo.java` — hộp quà 100% → 5% mỗi món
- `SRC/src/nro/models/boss/Boss_mini/SoiHecQuyn.java` — hộp quà 100% → 5% mỗi món

Mọi chỗ sửa đều có comment `// FIX:` giải thích ngắn. Toàn bộ cây mã (564 tệp `.java`)
biên dịch sạch bằng JDK 17.
