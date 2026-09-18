# 33 — Giảm sát thương nhận vào cho boss

> **Việc chủ dự án giao:**
> *"Trường máu của game là số nguyên 32-bit nên trần máu tuyệt đối là 2.147.483.647 cho mỗi hình dạng boss. Boss kết truyện Heart đã chạm trần. Thay vì tăng máu (không tăng được nữa), hãy thêm cơ chế GIẢM SÁT THƯƠNG NHẬN VÀO cho boss, và rà lại toàn bộ boss để đưa ra mức % hợp lý cho từng con."*

Đây là đúng khuyến nghị đã ghi sẵn trong [21c §6.2](../3-kiem-tra-can-bang/21c-boss-roi-do-can-bang.md#62-bảng-đề-xuất-chỉnh-hp--dame):

> ⚠️ *"`BossData.hp` là `int[]` → trần 2.147.483.647. Không khai báo HP 3 tỉ / 5 tỉ (tràn sang số âm → boss chết ngay đòn đầu). Muốn boss "dai" hơn thì **tăng hệ số giảm sát thương trong `injured`**, không tăng HP."*

---

## Mục lục

- [1. Vì sao cần cơ chế này](#1-vì-sao-cần-cơ-chế-này)
- [2. Cơ chế hoạt động](#2-cơ-chế-hoạt-động)
  - [2.1 Ba thứ được thêm vào `Boss.java`](#21-ba-thứ-được-thêm-vào-bossjava)
  - [2.2 Chỗ cắm trong `injured`](#22-chỗ-cắm-trong-injured)
  - [2.3 Bốn bảo đảm an toàn](#23-bốn-bảo-đảm-an-toàn)
  - [2.4 Chạy theo từng hình dạng](#24-chạy-theo-từng-hình-dạng)
  - [2.5 Câu chat báo cho người chơi](#25-câu-chat-báo-cho-người-chơi)
- [3. Mô hình tính thời gian hạ boss](#3-mô-hình-tính-thời-gian-hạ-boss)
- [4. Bảng đầy đủ từng boss](#4-bảng-đầy-đủ-từng-boss)
  - [4.1 Boss cốt truyện chương 1–2 — 0%](#41-boss-cốt-truyện-chương-12--0)
  - [4.2 Boss bản nhiệm vụ chương 4–6](#42-boss-bản-nhiệm-vụ-chương-46)
  - [4.3 Heart (-108108) — boss kết truyện](#43-heart--108108--boss-kết-truyện)
  - [4.4 Boss thế giới](#44-boss-thế-giới)
  - [4.5 Toàn bộ boss còn lại — 0%](#45-toàn-bộ-boss-còn-lại--0)
- [5. Boss có `injured` riêng và cách tránh phá chúng](#5-boss-có-injured-riêng-và-cách-tránh-phá-chúng)
- [6. Bật/tắt phần boss thế giới](#6-bậttắt-phần-boss-thế-giới)
- [7. Danh sách file đã sửa](#7-danh-sách-file-đã-sửa)
- [8. Chỗ nghi ngờ](#8-chỗ-nghi-ngờ)

---

## 1. Vì sao cần cơ chế này

Máu là số nguyên 32-bit ở **cả ba tầng**, nên không có chỗ nào nới ra được nếu không đổi kiểu dữ liệu toàn hệ thống:

| Tầng | Khai báo | Trần |
|---|---|---:|
| Dữ liệu boss | `BossData.hp` là `int[]` (`BossesData.java`, mọi `new int[]{…}`) | 2.147.483.647 |
| Chỉ số nhân vật | `NPoint.hp`, `NPoint.hpg` kiểu `int` | 2.147.483.647 |
| Dame boss | `BossData.dame` kiểu `int` | 2.147.483.647 |

Heart (-108108) đang dùng **1,5 tỉ + 2 tỉ + 2 tỉ + 2 tỉ**. Ba hình dạng cuối chỉ còn cách trần **7,1%**. Cộng thêm 1 máu nữa cho hình dạng cuối là hết đường. Boss thế giới Baby (-925), Super Black Goku (-203) và Cumber (-203999) cũng đã ở mức 2 tỉ/hình dạng.

**Giảm sát thương nhận vào** là cách duy nhất còn lại để kéo dài trận đánh mà không đụng tới kiểu dữ liệu: boss vẫn khai báo 2 tỉ máu, nhưng mỗi đòn chỉ ăn 35% giá trị thật, tức người chơi phải gõ ra tổng 5,7 tỉ sát thương mới hạ được.

> **Máu hiệu dụng = máu danh nghĩa ÷ (1 − %giảm / 100)**

---

## 2. Cơ chế hoạt động

### 2.1 Ba thứ được thêm vào `Boss.java`

**(a) Một trường dữ liệu** — bảng % chạy theo hình dạng:

```java
/** Bảng % sát thương bị chặn, chạy theo currentLevel (hình dạng). null = không giảm gì. */
protected int[] damageReducePercentByLevel;
```

**(b) Điểm mở rộng** — lớp con nào cần logic động thì ghi đè thẳng hàm này:

```java
public int getDamageReducePercent() {
    int[] table = this.damageReducePercentByLevel;
    if (table == null || table.length == 0) {
        return 0;                      // ← mặc định: KHÔNG giảm gì
    }
    int level = this.currentLevel;
    if (level < 0) {
        level = 0;                     // currentLevel khởi tạo bằng -1
    } else if (level >= table.length) {
        level = table.length - 1;      // die()/leaveMap() có nhánh đẩy vượt data.length
    }
    return table[level];
}
```

**(c) Hàm áp dụng** — `protected final`, không lớp con nào sửa được cách tính:

```java
protected final long applyDamageReduce(long damage) {
    if (damage <= 0) return damage;
    int percent = getDamageReducePercent();
    if (percent <= 0) return damage;
    if (percent > BossDamageReduce.MAX_PERCENT) percent = BossDamageReduce.MAX_PERCENT;
    int keep = 100 - percent;
    long reduced = (damage / 100L) * keep + (damage % 100L) * keep / 100L;
    if (reduced < 1L) reduced = 1L;
    notifyDamageReduceOnce(percent);
    return reduced;
}
```

Toàn bộ con số % nằm trong một file duy nhất: **`SRC/src/nro/models/boss/BossDamageReduce.java`**.

### 2.2 Chỗ cắm trong `injured`

Nguyên tắc đặt: **sau mọi nhánh đặc biệt, ngay trước `nPoint.subHP(damage)`**. Đặt ở đây thì mọi cơ chế cũ (né đòn, khiên năng lượng, trừ giáp, trần sát thương mỗi đòn, hấp thụ chưởng, trả về 0/1 cứng) đều chạy trước và không bị đụng tới.

Chỉ **5 trên 71** thân hàm `injured` trong cây mã được sửa:

| File | Lý do sửa |
|---|---|
| `boss/Boss.java` | Bản gốc — mọi boss không ghi đè `injured` đều đi qua đây |
| `boss/quest/QuestBoss.java` | Lớp nền của 8 boss nhiệm vụ + Heart |
| `boss/Black_Goku/BlackGoku.java` | Boss thế giới được đặt % |
| `boss/cumber/Cumber.java` | Boss thế giới được đặt % |
| `boss/Baby/Baby.java` | Nối dây sẵn (hiện để 0%, xem §4.4) |
| `boss/Cold/Cooler.java` | Nối dây sẵn (hiện để 0%, xem §4.4) |

**65 thân hàm `injured` còn lại không bị chạm một dòng nào.** Chúng vẫn chạy y như cũ vì `getDamageReducePercent()` mặc định trả 0 — xem §5.

### 2.3 Bốn bảo đảm an toàn

| Rủi ro | Cách chặn |
|---|---|
| **Chia cho 0** | Không có phép chia nào cho biến. Chỉ chia cho hằng `100L`. |
| **Tràn số** | Chia trước rồi mới nhân: `(damage / 100) * keep + (damage % 100) * keep / 100`. Vế trái tối đa `Long.MAX/100 × 100`, vế phải tối đa `99 × 100 / 100`. `damage` có bằng `Long.MAX_VALUE` cũng không tràn. |
| **Boss bất tử do làm tròn** | `if (reduced < 1L) reduced = 1L;` — mọi đòn có `damage ≥ 1` vẫn trừ được ít nhất 1 máu. Với `damage = 1` và `percent = 65`, kết quả là **1**, không phải 0. |
| **Gõ nhầm % (100, 250, âm)** | `percent <= 0` → thoát sớm; `percent > MAX_PERCENT (90)` → kẹp về 90. Không có cách nào tạo ra boss chặn 100% sát thương. |

Thêm một điểm: `damage <= 0` được **trả nguyên vẹn**, không kẹp lên 1. Đây là chủ ý — vài nhánh trong game cố tình đẩy `damage` về 0 và hàm không được phép biến 0 thành 1.

### 2.4 Chạy theo từng hình dạng

Bảng là `int[]` chạy theo `Boss.currentLevel`. Ví dụ Heart:

```java
public static final int[] HEART = {20, 35, 50, 65};
```

| `currentLevel` | Tên hình dạng | % chặn |
|---:|---|---:|
| 0 | Heart | 20% |
| 1 | Heart Hư Không | 35% |
| 2 | Heart Toàn Ký | 50% |
| 3 | Hư Không Vô Danh | 65% |

Mảng ngắn hơn số hình dạng thì phần tử cuối dùng cho phần còn lại — nên `{0}` là cách viết gọn của "0% ở mọi hình dạng".

### 2.5 Câu chat báo cho người chơi

Khi một hình dạng có `% > 0` ăn đòn **lần đầu**, boss chat:

> *"Đòn của ngươi yếu đi 65% trước ta"*

Chống spam bằng trường `damageReduceChatLevel`: chỉ nói **một lần cho mỗi hình dạng, mỗi lượt ra map**. Cờ này được reset ở đầu `Boss.joinMap()` để lượt hồi sinh sau vẫn nói lại. Boss 0% không bao giờ nói câu này (hàm thoát sớm trước khi gọi tới chat).

---

## 3. Mô hình tính thời gian hạ boss

Dùng nguyên mô hình "người-phút" của [21c §1](../3-kiem-tra-can-bang/21c-boss-roi-do-can-bang.md#1-mô-hình-tính-sức-mạnh-người-chơi-cần-có), không chế thêm giả định mới:

- `DPS 1 người = dame × 1,6 (Chiêu đấm cấp 7) × 2 đòn/giây × 1,1 (chí mạng ~10%) = dame × 3,52`
- `dame = dameg + tấn công trang bị`, `dameg = √(0,5 × SM ÷ 50)` (Giả định 2 của 21c: 50% tiềm năng vào sức đánh)
- Mốc sức mạnh mỗi nhiệm vụ lấy từ [20 §6.1](../2-thiet-ke-nhiem-vu-moi/20-thiet-ke-nhiem-vu-moi.md#61-mốc-sức-mạnh-mục-tiêu-khi-kết-thúc-từng-nhiệm-vụ)
- Cột "5 người + buff ×2" phản ánh nhóm 5 người có Cuồng nộ / Bổ huyết — điều kiện tốt mà nhóm bình thường đạt được

DPS dùng cho từng mốc:

| Mốc | NV | SM | DPS 1 người |
|---|---:|---:|---:|
| P1 | 6 | 46.000 | 149 |
| P2 | 15 | 2.000.000 | 920 |
| P7 | 30 | 1.400.000.000 | 18.627 |
| P9 | 34 | 3.500.000.000 | 26.281 |
| P10 | 37 | 6.000.000.000 | 32.722 |
| — | 38 | 7.000.000.000 | 34.900 |
| P11 | 39 | 8.000.000.000 | 46.972 |
| P13 | 42 | 13.000.000.000 | 54.208 |
| P14 | 46 | 17.500.000.000 | 54.208 *(chạm trần `dameg = 11.000`)* |
| P15 | — | 90.000.000.000 | 109.736 *(sau khi mở giới hạn lần 2)* |

Mục tiêu thời gian, theo nguyên tắc **P4** của 21c §6.1: **60–120 giây solo cho boss thường, 240–420 giây solo cho boss cuối chương**.

---

## 4. Bảng đầy đủ từng boss

### 4.1 Boss cốt truyện chương 1–2 — 0%

| Boss | ID | NV | Hình dạng | Máu danh nghĩa | % giảm | Máu hiệu dụng | TTK solo | TTK 5 người | Lý do chọn % |
|---|---:|---:|---:|---:|---:|---:|---:|---:|---|
| Kẻ Thu Gom | −2000 | 6 | 1 | 80.000 | **0%** | 80.000 | 8,9 phút | 1,8 phút | Người chơi mốc 46.000 SM, DPS 149. Boss này 21c §5.1 đã chấm 🟡 "hơi dai cho NV 6" ở nguyên máu hiện tại. Thêm bất kỳ % nào là đẩy sang 🔴. |
| Jaco Vô Thức | −2001 | 15 | 2 | 1.200.000 + 2.500.000 = **3.700.000** | **0%** | 3.700.000 | 67,0 phút | 13,4 phút | 21c §5.1 chấm 🔴 "quá dai + dame quá cao" — đang gấp ~26 lần mức hợp lý cho NV 15. Chỗ này cần **giảm máu**, không phải thêm phòng ngự. |

> Đúng chỉ đạo của chủ dự án: *"Boss nhiệm vụ chương 1–2: 0% — người chơi mới, đừng làm khó."* Số liệu cũng nói y vậy: cả hai con đã dai hơn mức nên có.

### 4.2 Boss bản nhiệm vụ chương 4–6

Máu các con này đã được hạ xuống "thang boss cốt truyện" theo 21c §6.2.a, nên % ở đây chỉ để kéo trận đánh về vùng mục tiêu **240–420 giây solo** của boss cuối chương, và để người chơi cảm nhận được boss chương sau cứng hơn boss chương trước.

| Boss | ID | NV | Hình dạng | Máu danh nghĩa (mỗi hình dạng) | % giảm (mỗi hình dạng) | Máu hiệu dụng | TTK solo | 5 người | 5 người + buff ×2 | Lý do chọn % |
|---|---:|---:|---:|---|---|---:|---:|---:|---:|---|
| Xên bọ hung NV | −2100 | 30 | 3 | 800.000 / 930.000 / 1.060.000 | **8 / 10 / 12** | 3.107.444 | 2,8 phút | 33 giây | 17 giây | Boss cuối chương 4, con đầu tiên có phòng ngự. Trung bình 10% = mép dưới dải 10–25% chủ dự án đặt. Nâng cao hơn sẽ vượt cửa sổ 240–420 giây. |
| Cooler NV | −2101 | 34 | 2 | 3.000.000 / 3.300.000 | **10 / 15** | 7.215.686 | **4,6 phút** | 55 giây | 27 giây | Mở đầu chương 5 → nhích lên một bậc so với −2100. Rơi đúng giữa cửa sổ mục tiêu. |
| Mabư NV | −2102 | 37 | 5 | 2.000.000 × 5 | **12 / 14 / 16 / 18 / 20** | 11.918.285 | **6,1 phút** | 73 giây | 36 giây | 5 hình dạng liên tiếp — % tăng đều để hình dạng cuối (Kid Bư) đáng nhớ hơn hình dạng đầu. Tổng vẫn nằm trong cửa sổ. |
| Black Goku NV | −2103 | 38 | 2 | 3.100.000 / 3.600.000 | **15 / 20** | 8.147.059 | **3,9 phút** | 47 giây | 23 giây | Áp chót chương 5. Hình dạng 2 (Super Saiyan Rose) 20% để khớp lời thoại "sức mạnh không giới hạn". |
| Baby NV | −2104 | 39 | 3 | 1.900.000 × 3 | **18 / 22 / 25** | 7.286.304 | 2,6 phút | 31 giây | 16 giây | Boss cuối chương 5 → chạm **trần 25%** của dải chủ dự án đặt cho chương 4–5. TTK hơi dưới cửa sổ vì máu con này thấp nhất nhóm; muốn kéo lên thì nên tăng máu chứ không tăng %. |
| Cumber NV | −2105 | 42 | 2 | 3.700.000 / 4.400.000 | **28 / 32** | 11.609.477 | **3,6 phút** | 43 giây | 21 giây | Chương 6, chủ dự án đặt 30% → dùng 28/32 để trung bình đúng 30% và vẫn tăng dần theo hình dạng. |

**Đường cong % theo chương (trung bình mỗi con):** 0 → 0 → 10 → 12,5 → 16 → 17,5 → 21,7 → 30. Tăng đơn điệu, không có bước nhảy nào quá 8 điểm.

### 4.3 Heart (-108108) — boss kết truyện

Mốc sức mạnh NV 46: **17,5 tỉ SM**, DPS 1 người = **54.208** (đã chạm trần `dameg = 11.000` nếu chưa mở giới hạn lần 2 ở NV 44).

| Hình dạng | `currentLevel` | Tên | Máu danh nghĩa | % giảm | **Máu hiệu dụng** | Solo | 5 người + buff ×2 | 20 người + buff ×2 |
|---:|---:|---|---:|---:|---:|---:|---:|---:|
| 1 | 0 | Heart | 1.500.000.000 | **20%** | 1.875.000.000 | 9,6 giờ | 57,6 phút | 14,4 phút |
| 2 | 1 | Heart Hư Không | 2.000.000.000 | **35%** | 3.076.923.077 | 15,8 giờ | 94,6 phút | 23,6 phút |
| 3 | 2 | Heart Toàn Ký | 2.000.000.000 | **50%** | 4.000.000.000 | 20,5 giờ | 123,0 phút | 30,7 phút |
| 4 | 3 | Hư Không Vô Danh | 2.000.000.000 | **65%** | 5.714.285.714 | 29,3 giờ | 175,7 phút | 43,9 phút |
| | | **TỔNG** | **7.500.000.000** | — | **14.666.208.791** | **75,2 giờ** | **7,5 giờ** | **1,9 giờ** |

**Máu hiệu dụng tổng cộng ≈ 14,67 tỉ** — gấp **1,96 lần** con số danh nghĩa 7,5 tỉ, đúng khoảng 14–15 tỉ chủ dự án đặt ra.

Lý do chọn đúng 20 → 35 → 50 → 65:

1. **Bước đều 15 điểm.** Mỗi hình dạng đắt hơn hình dạng trước một cách dễ đoán, người chơi cảm nhận được tiến độ.
2. **Hình dạng cuối 65% không chạm trần 90%.** Còn dư chỗ nếu sau này muốn nâng mà không phải sửa cơ chế.
3. **Máu hiệu dụng tăng gấp bội theo hình dạng** (1,88 → 3,08 → 4,00 → 5,71 tỉ) dù máu danh nghĩa 3 hình dạng cuối bằng nhau ở trần 2 tỉ. Đây chính là thứ mà tăng máu không làm được nữa.
4. **Thang "người-phút" = 4.509** — đặt Heart giữa Black Goku sau khi sửa (1.859) và Baby (5.270), tức Heart là boss khó nhì game, không phải khó nhất. Hợp vai "boss kết truyện có cả server đánh".

Thang so sánh đầy đủ (người-phút, tính ở DPS P14):

| Boss | Người-phút | Xếp hạng |
|---|---:|---:|
| Baby (−925) thế giới | 5.270 | 1 |
| **Heart (−108108)** | **4.509** | **2** |
| Black Goku (−203) *sau sửa* | 1.859 | 3 |
| Cumber (−203999) *sau sửa* | 1.859 | 3 |
| Siêu Bọ Hung (−101) | 323 | 5 |
| Cooler (−29) | 217 | 6 |

### 4.4 Boss thế giới

> ⚠️ **ĐÂY LÀ PHẦN DUY NHẤT ĐỤNG TỚI NGƯỜI CHƠI ĐANG CÀY.** Tách riêng, gom một chỗ, có công tắc — xem [§6](#6-bậttắt-phần-boss-thế-giới).

Ba con dưới đây **đã có sẵn** cơ chế chia sát thương trong `injured` của riêng chúng. % mới **cộng dồn lên trên** cơ chế cũ, không thay thế.

| Boss | ID | Hình dạng | Máu danh nghĩa | Cơ chế cũ trong `injured` | % mới | **Máu hiệu dụng** | Trước khi sửa | Lý do |
|---|---:|---:|---:|---|---:|---:|---:|---|
| Super Black Goku | −203 | 1 | 500.000.000 | không có | **30%** | 714.285.714 | 500.000.000 | Hình dạng 1 xưa nay không có phòng ngự gì → đây là chỗ đáng thêm nhất. |
| Super Black Goku | −203 | 2 | **2.000.000.000** | `damage /= 2` (≈50%) | **+25%** | 5.333.333.333 | 4.000.000.000 | Tổng phòng ngự 62,5%, nằm trong dải 25–40% chủ dự án đặt. Máu đã chạm trần, không nâng được nữa. |
| | | **tổng** | 2.500.000.000 | | | **6.047.619.048** | 4.500.000.000 | **+34%** |
| Cumber | −203999 | 1 | 500.000.000 | không có | **30%** | 714.285.714 | 500.000.000 | Chỉ số và `injured` y hệt Black Goku → giữ y hệt để hai con cùng hạng. |
| Cumber | −203999 | 2 | **2.000.000.000** | `damage /= 2` | **+25%** | 5.333.333.333 | 4.000.000.000 | như trên |
| | | **tổng** | 2.500.000.000 | | | **6.047.619.048** | 4.500.000.000 | **+34%** |
| **Baby** | −925 | 1–3 | 2.000.000.000 × 3 | `damage × 0,7 / 2` (**≈65%**) | **0%** *(cố ý)* | 17.142.000.000 | 17.142.000.000 | Xem giải thích bên dưới |
| **Cooler** | −29 | 1–2 | 200M + 500M | né 1% | **0%** *(cố ý)* | 707.000.000 | 707.000.000 | Xem giải thích bên dưới |

Thời gian hạ Black Goku / Cumber sau sửa (DPS P14 = 54.208):

| | Solo | 5 người | 5 người + buff ×2 | 20 người + buff ×2 |
|---|---:|---:|---:|---:|
| Trước | 23,1 giờ | 4,6 giờ | 2,3 giờ | 34,6 phút |
| **Sau** | **31,0 giờ** | **6,2 giờ** | **3,1 giờ** | **46,5 phút** |

#### Vì sao Baby (−925) để 0% — đi ngược đề xuất khởi điểm

Chủ dự án gợi ý 25–40% cho nhóm chạm trần 2 tỉ, trong đó có Baby. **Tôi đề nghị không áp dụng cho Baby, và đây là lý do bằng số:**

- `Baby.injured` hiện làm `damage = damage × 0,7` rồi `damage / 2` → hệ số **×2,857**, tức **đã giảm sẵn 65%** sát thương. Baby **đã có** đúng cơ chế mà yêu cầu này muốn thêm, và ở mức **cao hơn cả dải 25–40% được đề nghị**.
- Hệ quả: máu hiệu dụng hiện tại của Baby là **17,14 tỉ** — **5.270 người-phút**, gấp **4,3 lần** Black Goku và gấp **24 lần** Cooler. 21c §5.3 đã xếp Baby hạng 1 trong danh sách "boss quá khó so với mốc nhiệm vụ", chênh **×100** so với mục tiêu.
- Cộng thêm 25% nữa sẽ đẩy Baby lên **22,9 tỉ máu hiệu dụng / 7.026 người-phút** — vượt cả Heart 1,56 lần, tức boss thế giới farm-được lại khó hơn boss kết truyện. Sai vai.

Dây đã nối sẵn. Muốn bật thì sửa đúng một dòng trong `BossDamageReduce.java`:
```java
public static final int[] BABY_TG = world(new int[]{0, 0, 0});   // → {25, 30, 35}
```

#### Vì sao Cooler (−29) để 0%

Cooler **không** chạm trần máu (200M + 500M), nên không thuộc nhóm chủ dự án khoanh. Nó vẫn là con đáng bàn nhất vì 21c §5.2 ② xếp nó là boss **"thưởng quá hậu so với độ khó" nặng nhất** trong nhóm rơi đồ Thần Linh: máu hiệu dụng chỉ bằng **15,7%** của Black Goku mà bảng rơi đồ y hệt.

Nhưng cách sửa mà 21c **§6.3** đề xuất cho Cooler là **hạ phần thưởng**, không phải tăng độ khó — và tăng độ khó Cooler lúc này **không khép được khoảng cách**: nếu bật `{25, 35}` thì Cooler lên 1,05 tỉ máu hiệu dụng (322 người-phút) trong khi Black Goku sau sửa lên 1.859 → tỉ lệ vẫn **5,8 lần**, đúng bằng tỉ lệ cũ. Tức là làm Cooler khó hơn mà **không giải quyết được vấn đề gốc**, chỉ khiến người chơi đang farm khó chịu thêm.

Dây cũng đã nối sẵn:
```java
public static final int[] COOLER_TG = world(new int[]{0, 0});    // → {25, 35}
```

#### Boss thế giới còn lại — giữ 0%

| Boss | ID | Máu | Cơ chế phòng ngự sẵn có | Vì sao không thêm |
|---|---:|---:|---|---|
| Siêu Bọ Hung | −101 | 150M + 200M | `damage / 3` (≈67%) | Đã giảm sẵn nhiều hơn dải đề nghị. 21c §6.2.b còn muốn **tăng số lượng** con này lên 2 vì nó đang là nút cổ chai nguồn đồ cấp. |
| Xên bọ hung | −100 | 50M/100M/150M | `damage / 2` (50%) | Đã có sẵn 50%. |
| Fide Vàng | −502 | 1.000.000.000 | trần `50M`/đòn | 21c §6.2.b muốn **giảm** độ khó (bom 2,1 tỉ xuyên giáp), không tăng. |
| Mabư 14h | −214 | 5 hình dạng | né 10% + trần 30M/đòn + hình dạng cuối chỉ chết bằng Quả cầu kênh khí | Đã có ba lớp phòng ngự chồng nhau. |
| Mabư 12h | −236 | 100M | trần 50M/đòn | Boss hồi sinh 60 giây — thêm phòng ngự chỉ làm chỗ farm khó chịu hơn, không sửa được lỗi gốc (§5.2 ① của 21c). |
| Broly / Super Broly | −1822 / −82282 | rand | trần `hpMax/100`/đòn → luôn cần ≥100 đòn | Cơ chế trần đòn đã quyết định độ dai, % không đổi được gì. |
| Dr Lychee / Hatchiyack | phó bản | `1M + 15M×lv`, trần 2 tỉ | `damage −= damage/100 × (lv/10)` — **đã giảm theo level phó bản** | Đã có cơ chế giảm scale theo level, đúng tinh thần. |
| Nappa, TĐST, Fide (−28), Android 13/14/15, Pic/Poc/King Kong, Bojack, Siêu Bojack | | | không có | 21c §6.2.a muốn **hạ máu** các con này xuống thang cốt truyện, ngược hướng hoàn toàn. |

### 4.5 Toàn bộ boss còn lại — 0%

Mọi boss không xuất hiện ở §4.1–§4.4 (boss sự kiện Tết / Trung thu / Halloween / Noel / Hùng Vương, boss phó bản Doanh trại, Võ đài Hạt mít, Con đường rắn độc, Luyện tập tự động, boss mini, Yardrat, Golden Frieza…) **không được gán bảng %** → `damageReducePercentByLevel` là `null` → `getDamageReducePercent()` trả 0 → `applyDamageReduce()` thoát ngay ở dòng thứ ba và trả lại `damage` **nguyên vẹn, không đổi một bit**.

---

## 5. Boss có `injured` riêng và cách tránh phá chúng

Cây mã có **71 thân hàm `injured`** (1 bản gốc + 70 trong lớp con). Cách tôi bảo đảm không phá chúng:

**Nguyên tắc 1 — mặc định là 0, và 0 nghĩa là "không chạm tới".** `applyDamageReduce` thoát ở `if (percent <= 0) return damage;` **trước** khi làm bất cứ phép tính nào. Không làm tròn, không kẹp, không chat. Boss 0% chạy y hệt trước khi có tính năng này.

**Nguyên tắc 2 — chỉ sửa 5 file, không sửa 65 file còn lại.** Xem bảng ở §2.2.

**Nguyên tắc 3 — luôn cắm sau các nhánh đặc biệt.** Đây là các lớp tôi đã đọc kỹ và xác nhận không bị ảnh hưởng:

| Boss | File | Cơ chế đặc biệt | Vì sao an toàn |
|---|---|---|---|
| **Tàu Pảy Pảy** | `luyen_tap_tu_dong/TauPayPay.java:93` | Người chơi chưa tới `TASK_10_1` thì `return 100;` — chỉ trừ 100 máu mỗi đòn, bất kể sát thương thật | File **không bị sửa**, % = 0. Nhánh `return 100` nằm trước mọi thứ và vẫn nguyên. |
| **Dr.Kôrê** | `Android/DrKore.java:71` | Hấp thụ Kamejoko / Masenko / Antomic → hồi máu, `return 0` | File **không bị sửa**, % = 0. *(Xem §8 mục 3 — hàm này thực ra là một **overload**, không phải override.)* |
| **Android 19** | `Android/Android19.java:56` | Miễn nhiễm chưởng, hồi 80% | File không bị sửa, % = 0. |
| **Android 13** | `Android/Android13.java:56` | Bất tử khi A14/A15 còn sống | File không bị sửa, % = 0. |
| **Mabư 12h** | `MajinBuu_12h/Mabu.java:181` | Trần 50.000.000/đòn + tích `fightMabu` | File không bị sửa, % = 0. |
| **Mabư 14h / Super Bư** | `MajinBuu_14h/Mabu2H.java:127`, `SuperBu.java:85` | Né 10%, trần 30M/đòn, **hình dạng cuối chỉ chết bằng Quả cầu kênh khí** | File không bị sửa, % = 0. |
| **Bui Bui / Ya côn** | `MajinBuu_12h/BuiBui*.java`, `Yacon.java` | Miễn nhiễm Kamejoko / Masenko / Antomic / Liên hoàn | File không bị sửa, % = 0. |
| **Broly / Super Broly** | `Broly/Broly.java:111`, `SuperBroly.java:97` | Trần `hpMax/100` mỗi đòn + tăng chỉ số khi bị đánh | File không bị sửa, % = 0. |
| **Fide Vàng + 5 Death Beam** | `Golden_fireza/*.java` | Death Beam bất tử | File không bị sửa, % = 0. |
| **Xên bọ hung / Siêu Bọ Hung** | `Cell/XenBoHung.java:80`, `SieuBoHung.java:151` | `damage/2`, `damage/3`, tự phát nổ khi chết | File không bị sửa, % = 0. |
| **Dr Lychee / Hatchiyack** | `khi_gas/*.java` | Giảm sát thương theo level phó bản | File không bị sửa, % = 0. |
| **Black Goku / Cumber** | `Black_Goku/BlackGoku.java:93`, `cumber/Cumber.java:84` | `if (currentLevel != 0) damage /= 2`, trừ `rand(0..100.000)`, khiên → `damage = 1` | **Có sửa.** Dòng `applyDamageReduce` đặt **sau** cả ba nhánh, ngay trước `subHP`. Khiên vẫn ép `damage = 1`, và 1 sau khi giảm 25% vẫn là **1** nhờ kẹp sàn. |
| **Baby** | `Baby/Baby.java:111` | `damage × 0,7`, `/2`, khiên → `damage / 4` | **Có sửa** (hiện 0%). Cắm sau toàn bộ, thứ tự phép tính cũ không đổi. |
| **Cooler** | `Cold/Cooler.java:94` | Né 1% cứng (không phụ thuộc `piercing`) | **Có sửa** (hiện 0%). Cắm sau `subDameInjureWithDeff`. |
| **QuestBoss** (8 boss nhiệm vụ + Heart) | `quest/QuestBoss.java:254` | Né đòn, `idNRNM` → `return 1`, khiên → `damage = 1` | **Có sửa.** Cắm sau cả ba. Nhánh `return 1` thoát trước nên không bao giờ đi qua hàm giảm. |

**Nguyên tắc 4 — hàm tính là `final`.** `applyDamageReduce` khai báo `protected final`, lớp con không thể ghi đè cách tính và vô tình phá bảo đảm "tối thiểu 1 sát thương".

---

## 6. Bật/tắt phần boss thế giới

Toàn bộ số nằm trong **`SRC/src/nro/models/boss/BossDamageReduce.java`**.

**Tắt hẳn phần boss thế giới** (boss nhiệm vụ và Heart **không** bị ảnh hưởng):

```java
public static final boolean WORLD_BOSS_ENABLED = false;
```

Cách hoạt động: các hằng `*_TG` đi qua hàm lọc `world(...)`, hàm này trả `null` khi công tắc tắt; `getDamageReducePercent()` gặp `null` thì trả 0 và `applyDamageReduce()` thoát ngay. Black Goku, Cumber, Baby, Cooler quay về **đúng** hành vi trước khi có tính năng này, không cần sửa thêm file nào.

**Chỉnh một con riêng lẻ:** sửa mảng của con đó. Ví dụ hạ Black Goku về 20% cho cả hai hình dạng:

```java
public static final int[] BLACK_GOKU_TG = world(new int[]{20, 20});
```

**Tắt riêng một con:** đổi mảng thành toàn số 0.

**Bật boss thế giới nhưng giữ Heart / boss nhiệm vụ nguyên:** đúng là trạng thái mặc định hiện tại — hai nhóm hằng số tách rời, không dùng chung công tắc.

---

## 7. Danh sách file đã sửa

| File | Thay đổi |
|---|---|
| `SRC/src/nro/models/boss/BossDamageReduce.java` | **File mới.** Toàn bộ hằng số % + công tắc `WORLD_BOSS_ENABLED`. |
| `SRC/src/nro/models/boss/Boss.java` | Thêm trường `damageReducePercentByLevel`, `damageReduceChatLevel`; thêm `getDamageReducePercent()`, `applyDamageReduce(long)`, `notifyDamageReduceOnce(int)`; gọi `applyDamageReduce` trong `injured`; reset cờ chat trong `joinMap()`. |
| `SRC/src/nro/models/boss/quest/QuestBoss.java` | Gọi `applyDamageReduce` trong `injured`, trước `subHP`. |
| `SRC/src/nro/models/boss/quest/KeThuGom.java` | Gán bảng `KE_THU_GOM`. |
| `SRC/src/nro/models/boss/quest/JacoVoThuc.java` | Gán bảng `JACO_VO_THUC`. |
| `SRC/src/nro/models/boss/quest/XenBoHungNhiemVu.java` | Gán bảng `XEN_BO_HUNG_NV`. |
| `SRC/src/nro/models/boss/quest/CoolerNhiemVu.java` | Gán bảng `COOLER_NV`. |
| `SRC/src/nro/models/boss/quest/MabuNhiemVu.java` | Gán bảng `MABU_NV`. |
| `SRC/src/nro/models/boss/quest/BlackGokuNhiemVu.java` | Gán bảng `BLACK_GOKU_NV`. |
| `SRC/src/nro/models/boss/quest/BabyNhiemVu.java` | Gán bảng `BABY_NV`. |
| `SRC/src/nro/models/boss/quest/CumberNhiemVu.java` | Gán bảng `CUMBER_NV`. |
| `SRC/src/nro/models/boss/heart/Heart.java` | Gán bảng `HEART`. |
| `SRC/src/nro/models/boss/Black_Goku/BlackGoku.java` | Gán bảng `BLACK_GOKU_TG` + gọi `applyDamageReduce`. |
| `SRC/src/nro/models/boss/cumber/Cumber.java` | Gán bảng `CUMBER_TG` + gọi `applyDamageReduce`. |
| `SRC/src/nro/models/boss/Baby/Baby.java` | Gán bảng `BABY_TG` + gọi `applyDamageReduce`. |
| `SRC/src/nro/models/boss/Cold/Cooler.java` | Gán bảng `COOLER_TG` + gọi `applyDamageReduce`. |

**Không đụng tới:** `services/TaskService.java`, `consts/ConstTask.java`, `SRC/sql/**`, `map/service/ChangeMapService.java`, `shop/**`, `database/**`, `npc_list/**`, `BossesData.java` (không đổi một con số máu nào).

**Biên dịch:** sạch, 563 file (562 cũ + 1 file mới), 0 lỗi.

---

## 8. Chỗ nghi ngờ

**1. Heart 75,2 giờ solo — có thể vẫn quá dai, và điều này đến từ máu chứ không phải từ %.**
21c §5.3 đã xếp Heart hạng 3 trong danh sách "boss quá khó khiến người chơi kẹt" **ngay cả khi chưa có % nào**, và 21c §6.2.a đề nghị hạ máu Heart xuống **7–9 triệu mỗi hình dạng** (thang boss cốt truyện). Chủ dự án đã "chốt" giữ 1,5 tỉ / 2 tỉ (ghi rõ trong comment `BossesData.java`), nên tôi thực hiện theo và chỉ thêm %. Nhưng cần nói thẳng:

- Với nhóm **5 người + buff ×2**: **7,5 giờ**. Với **20 người + buff ×2**: **1,9 giờ**. Phải khoảng **50 người có buff** mới về mức 45 phút.
- Kể cả người chơi **P15** (90 tỉ SM, đã mở giới hạn lần 2, DPS 109.736): solo vẫn **37,1 giờ**; 20 người + buff vẫn **55,7 phút**.
- Heart là **bước bắt buộc của NV 46/47/50**. Nếu một người chơi lẻ không gom được nhóm 20 người thì tuyến chính **đứng hẳn** ở đó. Điều này va với tinh thần [32-duong-vong-nguoi-choi-le.md](32-duong-vong-nguoi-choi-le.md).
- **Đề nghị chủ dự án quyết:** hoặc chấp nhận Heart là nội dung raid (và mở đường vòng cho người chơi lẻ), hoặc hạ máu danh nghĩa xuống ~300–500 triệu/hình dạng và **giữ nguyên bảng % này** — lúc đó tổng máu hiệu dụng khoảng 2,9–3,7 tỉ, tức 5 người + buff ×2 hạ trong ~1,5–1,9 giờ.

**2. DPS 54.208 ở NV 46 là trần, không phải ước lượng.**
`NPoint.getDameLimit()` chặn `dameg ≤ 11.000` khi `limitPower = 0`. Người chơi ở 13 tỉ SM và ở 17,5 tỉ SM có **cùng DPS**. Nghĩa là mọi con số "TTK" của Heart **không cải thiện khi cày thêm sức mạnh** — chỉ cải thiện khi mở giới hạn (NV 33 và NV 44) hoặc thêm người. Nếu tuyến nhiệm vụ để lọt người chơi tới NV 46 mà chưa mở giới hạn lần 2, họ kẹt cứng.

**3. `DrKore.injured` là overload, không phải override — nghi là lỗi có sẵn.**
`Android/DrKore.java:71` khai báo `injured(Player, **int** damage, ...)` trong khi bản gốc ở `Boss.java` là `(Player, **long** damage, ...)`, và **không có `@Override`**. Java coi đây là hai hàm khác nhau; mọi lời gọi từ hệ thống sát thương (truyền `long`) sẽ đi thẳng vào `Boss.injured`, **bỏ qua nhánh hấp thụ chưởng của Dr.Kôrê**. Tức cơ chế hấp thụ nhiều khả năng **đang không chạy** — khớp với ghi chú *"(hấp thụ lỗi, xem §7)"* ở 21c §2.1. **Tôi không sửa** vì nằm ngoài phạm vi việc này, nhưng nó đáng được ghi vào danh sách lỗi.

**4. Mô hình DPS bỏ qua giáp boss và sao pha lê / danh hiệu người chơi.**
`QuestBoss.injured` và `Cooler.injured` có gọi `subDameInjureWithDeff` (trừ giáp boss) nhưng mô hình của 21c §1.1 không mô hình hóa giáp, còn bản gốc `Boss.injured` thì **không trừ giáp chút nào**. Ngược lại, người chơi có sao pha lê %, danh hiệu %, nội tại có thể cao hơn DPS mô hình khá nhiều. Hai sai số này ngược chiều nhau và tôi không định lượng được — **mọi TTK trong tài liệu này nên đọc là "cùng bậc độ lớn", không phải con số chính xác.**

**5. `Baby NV (−2104)` 2,6 phút và `Xên bọ hung NV (−2100)` 2,8 phút hơi thấp hơn cửa sổ 4–7 phút.**
Nguyên nhân là máu danh nghĩa của hai con này thấp nhất nhóm (1,9M×3 và 800k/930k/1,06M) trong khi mốc sức mạnh lại cao. Tôi **không** nâng % để bù vì làm vậy sẽ vượt dải 10–25% chủ dự án đặt cho chương 4–5. Nếu muốn kéo về đúng cửa sổ thì nên **tăng máu danh nghĩa** (còn rất xa trần) — sửa `BossesData.java`, không sửa file này.

**6. Con số % của boss thế giới chưa được thử với người chơi thật.**
`WORLD_BOSS_ENABLED = true` là trạng thái mặc định tôi để, theo đúng yêu cầu. Nhưng đây là thay đổi làm **Black Goku và Cumber dai thêm 34%** đối với người chơi đang cày. Nếu chủ dự án muốn ra mắt tuyến nhiệm vụ trước rồi mới đụng tới endgame thì đặt `WORLD_BOSS_ENABLED = false` — mọi thứ khác vẫn chạy.

**7. Câu chat "Đòn của ngươi yếu đi N% trước ta" là do tôi tự đặt.**
Yêu cầu ghi "tuỳ bạn, không bắt buộc". Câu này nói **một lần mỗi hình dạng mỗi lượt ra map**. Nếu thấy phá không khí lời thoại cốt truyện của Heart thì xóa dòng gọi `notifyDamageReduceOnce(percent);` trong `Boss.applyDamageReduce` là xong — không ảnh hưởng gì tới phần tính toán.
