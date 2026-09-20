# 28 — Boss bản nhiệm vụ (−2100 … −2199) và boss mới Heart

> Sản phẩm của bước "dựng boss cho tuyến nhiệm vụ mới".
> Quyết định gốc: [22 §0](22-san-sang-code.md) mục **2** (dựng bản nhiệm vụ riêng) và mục **11** (boss nhiệm vụ **không** rơi vàng).
> Cân bằng chỉ số: [21c §6.2.a](../3-kiem-tra-can-bang/21c-boss-roi-do-can-bang.md) · Đặc tả Heart: [20c §5](../2-thiet-ke-nhiem-vu-moi/20c-chi-tiet-nhiem-vu-32-47.md) · Id vật phẩm: [25](25-bang-id-vat-pham-moi.md).

## Mục lục

1. [Tóm tắt](#1-tóm-tắt)
2. [Hai nguyên tắc chủ dự án đã chốt và cách code tôn trọng chúng](#2-hai-nguyên-tắc-chủ-dự-án-đã-chốt-và-cách-code-tôn-trọng-chúng)
3. [Bảng 6 boss bản nhiệm vụ](#3-bảng-6-boss-bản-nhiệm-vụ)
4. [Boss mới: Heart (−108108)](#4-boss-mới-heart-108108)
5. [So sánh với boss thế giới bản gốc](#5-so-sánh-với-boss-thế-giới-bản-gốc)
6. [Cách đăng ký và khởi tạo](#6-cách-đăng-ký-và-khởi-tạo)
7. [Chống đè lên boss thế giới và chống farm](#7-chống-đè-lên-boss-thế-giới-và-chống-farm)
8. [Danh sách file đã thêm / đã sửa](#8-danh-sách-file-đã-thêm--đã-sửa)
9. [Chỗ nghi ngờ / cần chủ dự án quyết](#9-chỗ-nghi-ngờ--cần-chủ-dự-án-quyết)

---

## 1. Tóm tắt

| Hạng mục | Kết quả |
|---|---|
| Boss bản nhiệm vụ đã dựng | **6** — Xên bọ hung, Cooler, Mabư 14h, Black Goku, Baby, Cumber |
| Dải id đã dùng | **−2100 … −2105** (còn trống −2106 … −2199) |
| Boss mới | **Heart −108108**, 4 hình dạng |
| Tổng số hình dạng mới | 6 boss = 18 hình dạng + Heart 4 hình dạng = **22** `BossData` mới |
| Boss thế giới bản gốc | **Không đụng một dòng nào** — HP, dame, `mapJoin`, bảng rơi đồ giữ nguyên |
| Biên dịch | **Sạch, 556 file** (trước đó 548 + 8 file mới) |

**Kiểm tra trùng id:** đã quét toàn bộ `BossID.java` (150 hằng số). Id âm gần dải mới nhất về hai phía là `BROLY = -1822` và `SUPER_BROLY = -82282`; **không có id nào nằm trong −2100 … −2199**, và **−108108 cũng chưa được dùng**.

## 2. Hai nguyên tắc chủ dự án đã chốt và cách code tôn trọng chúng

> **NT1 — Boss nằm trong bước nhiệm vụ bắt buộc chỉ rơi ĐỒ NHIỆM VỤ.** Không trang bị, không nguyên liệu giá trị, **không vàng**.
>
> **NT2 — Đồ xịn vẫn chỉ đến từ boss thế giới bản gốc.** Giữ nguyên độ khó và bảng rơi của chúng.

Cả hai nằm gọn trong **một** hàm duy nhất, `QuestBoss.reward(Player)`:

```java
@Override
public void reward(Player plKill) {
    // (1) Luôn báo cho hệ thống nhiệm vụ.
    TaskService.gI().checkDoneTaskKillBoss(plKill, this);
    // (2) Chỉ rơi đồ nhiệm vụ. KHÔNG vàng, KHÔNG trang bị, KHÔNG Ngọc Rồng,
    //     KHÔNG đồ Thần Linh — đồ xịn vẫn chỉ đến từ boss thế giới bản gốc.
    dropQuestItem(plKill);
}
```

`dropQuestItem` chỉ rơi đúng **một** vật phẩm, lấy từ `getQuestItemId(currentLevel)` (mặc định `-1` = không rơi gì), kèm option `30` "Không thể giao dịch" theo quy ước ở [25 §3](25-bang-id-vat-pham-moi.md). Hiện chỉ Heart hình dạng 3 dùng tới (item **2029 Ống nghiệm Myuu**); 6 boss còn lại **không rơi gì cả** vì vật phẩm của những nhiệm vụ đó đến từ NPC hoặc từ mob, không phải từ boss ([25 §3.5](25-bang-id-vat-pham-moi.md)).

Hàm còn chặn sẵn lỗi tràn mảng: nếu id vật phẩm chưa có trong `item_template` (file SQL chưa import) thì bỏ qua, không ném `IndexOutOfBoundsException` — đúng cảnh báo ở [25 §4](25-bang-id-vat-pham-moi.md).

**NT2 được bảo đảm bằng cách không chạm vào chúng**: `Cooler.java`, `BlackGoku.java`, `Baby.java`, `Cumber.java`, `XenBoHung.java`, `Mabu2H.java` và các `BossData` gốc (`COOLER`, `BLACK_GOKU`, `BABY`, `CUMBER`, `XEN_BO_HUNG_*`, `MABU`…) **không bị sửa** trong lần này.

## 3. Bảng 6 boss bản nhiệm vụ

Chỉ số lấy nguyên theo [21c §6.2.a](../3-kiem-tra-can-bang/21c-boss-roi-do-can-bang.md). Mọi giá trị HP đều nằm rất xa trần `int` (2.147.483.647) nên **không có nguy cơ tràn số âm**.

### 3.1 −2100 Xên bọ hung (NV 30)

| # | `currentLevel` | Tên hiển thị | Tạo hình (head/body/leg) | HP | Dame | Skill |
|---|---|---|---|---:|---:|---|
| 1 | 0 | Xên bọ hung | 228 / 229 / 230 | 800.000 | 12.000 | Kamejoko 7, Liên hoàn 7, DCTT 3 |
| 2 | 1 | Xên hoàn thiện | 231 / 232 / 233 | 930.000 | 12.500 | Kamejoko 7, Liên hoàn 7 |
| 3 | 2 | Xên hoàn thiện | 234 / 235 / 236 | 1.060.000 | 13.000 | Kamejoko 7, DCTT 7, Liên hoàn 7 |

- **Map**: 100 Thị trấn Ginder · **Hồi sinh**: 1 phút · **Rơi**: không rơi gì · **Dùng ở**: NV 30 bước 2 và 3.
- Bỏ hẳn cơ chế **hấp thụ (giết ngẫu nhiên 1 người trong khu)** và hệ số `dame/2` của bản gốc.

### 3.2 −2101 Cooler (NV 34)

| # | `currentLevel` | Tên hiển thị | Tạo hình | HP | Dame | Skill |
|---|---|---|---|---:|---:|---|
| 1 | 0 | Cooler | 317 / 318 / 319 | 3.000.000 | 18.700 | Galick 1, Antomic 1 |
| 2 | 1 | Cooler 2 | 320 / 321 / 322 | 3.300.000 | 18.700 | Galick 1, Antomic 1 |

- **Map**: 110 Hang băng · **Hồi sinh**: 1 phút · **Rơi**: không rơi gì · **Dùng ở**: NV 34 bước 4.
- Bỏ né đòn 1% của bản gốc (bản nhiệm vụ dùng né mặc định = 0).

### 3.3 −2102 Mabư 14h (NV 37)

| # | `currentLevel` | Tên hiển thị | Tạo hình | HP | Dame |
|---|---|---|---|---:|---:|
| 1 | 0 | Mabư mập | 297 / 298 / 299 | 2.000.000 | 24.000 |
| 2 | 1 | Super Bư | 421 / 422 / 423 | 2.000.000 | 24.000 |
| 3 | 2 | Bư Tênk | 424 / 425 / 426 | 2.000.000 | 24.000 |
| 4 | 3 | Bư Han | 427 / 428 / 429 | 2.000.000 | 24.000 |
| 5 | 4 | Kid Bư | 439 / 440 / 441 | 2.000.000 | 24.000 |

- **Skill mỗi hình dạng**: Kamejoko 3, Dragon 7 · **Map**: 127 Cổng phi thuyền · **Hồi sinh**: 1 phút · **Rơi**: không rơi gì · **Dùng ở**: NV 37 bước 1.
- **Ba cơ chế của bản gốc đã bỏ**, vì cả ba đều biến bước nhiệm vụ thành ngõ cụt:
  1. "Ăn người chơi" 20% mỗi 10 giây (kéo người chơi vào map 128 Bụng Mabư);
  2. Né 10% và trần 30 triệu sát thương mỗi đòn;
  3. **Hình dạng 5 chỉ chết bằng Quả cầu kênh khí** — kỹ năng mà phần lớn người chơi ở NV 37 chưa có.
- Bản nhiệm vụ cũng **không phụ thuộc khung giờ 14h** như phó bản gốc.

### 3.4 −2103 Black Goku (NV 38)

| # | `currentLevel` | Tên hiển thị | Tạo hình | HP | Dame | Skill |
|---|---|---|---|---:|---:|---|
| 1 | 0 | Black Goku | 550 / 551 / 552 | 3.100.000 | 28.500 | Kamejoko 7, Khiên NL 7, Galick 7 |
| 2 | 1 | Super Black Goku | 553 / 551 / 552 | 3.600.000 | 28.500 | TDHS 7, Khiên NL 7, Kamejoko 7, Galick 7 |

- **Map**: 102, 92, 93, 94, 96, 97, 98, 99, 100 · **Hồi sinh**: 1 phút · **Rơi**: không rơi gì · **Dùng ở**: NV 38 bước 2 và 3.
- **Bỏ Tái tạo năng lượng** (bản gốc hồi máu mỗi 300 giây) và bỏ hệ số `dame/2` của hình dạng 2 — hai thứ này khiến trận đánh kéo dài vô tận ở mốc 7 tỉ SM.

### 3.5 −2104 Baby (NV 39)

| # | `currentLevel` | Tên hiển thị | Tạo hình | HP | Dame | Skill |
|---|---|---|---|---:|---:|---|
| 1 | 0 | Baby | 1715 / 1716 / 1717 | 1.900.000 | 30.000 | Galick 7, Masenko 7, Kamejoko 7, Liên hoàn 7 |
| 2 | 1 | Baby | 1718 / 1719 / 1720 | 1.900.000 | 30.000 | Galick 7, Antomic 7, Kamejoko 7, Liên hoàn 7 |
| 3 | 2 | Baby | 1721 / 1722 / 1723 | 1.900.000 | 30.000 | Galick 7, Demon 7, Kamejoko 7, Super Kame 7 |

- **Map**: 14 Làng Kakarot · **Hồi sinh**: 1 phút · **Rơi**: không rơi gì · **Dùng ở**: NV 39 bước 6.
- Bỏ hệ số `dame × 0,7 / 2` (tức ×2,857 HP hiệu dụng) và bỏ chuỗi 6 chưởng hồi 0,1 giây của bản gốc.

### 3.6 −2105 Cumber (NV 42)

| # | `currentLevel` | Tên hiển thị | Tạo hình | HP | Dame | Skill |
|---|---|---|---|---:|---:|---|
| 1 | 0 | Cumber | 1254 / 1255 / 1256 | 3.700.000 | 37.200 | Kamejoko 7, Khiên NL 7, Galick 7 |
| 2 | 1 | Super Cumber | 1257 / 1255 / 1256 | 4.400.000 | 37.200 | TDHS 7, Khiên NL 7, Kamejoko 7, Galick 7 |

- **Map**: 155 Hành tinh ngục tù · **Hồi sinh**: 1 phút · **Rơi**: không rơi gì · **Dùng ở**: NV 42 bước 3 và 4.
- Bỏ Tái tạo năng lượng và hệ số `dame/2` như Black Goku.

## 4. Boss mới: Heart (−108108)

Tạo hình mượn nguyên `npc_template` **id 108** (`head 2109`, `body 2110`, `leg 2111`) — **không cần thêm tài nguyên client**, đúng như 20c §5 đã khảo sát.

| # | `currentLevel` | Tên | HP | Dame | Map | Skill | Dùng ở |
|---|---|---|---:|---:|---|---|---|
| 1 | 0 | Heart | 7.000.000 | 42.500 | 166 Phòng thí nghiệm Myuu | Thôi miên 7, Trói 7, Khiên NL 7, Masenko 7, Galick 7 | NV 46 bước 1 |
| 2 | 1 | Heart Hư Không | 7.600.000 | 42.500 | 145 Võ Đài Siêu Cấp | + TDHS 7, Tái tạo NL 7, DCTT 7, Kamejoko 7 | NV 46 bước 3 |
| 3 | 2 | Heart Toàn Ký | 8.200.000 | 42.500 | 145 Võ Đài Siêu Cấp | + Liên hoàn 7, Super Kame 7, Antomic 7 | NV 46 bước 4 |
| 4 | 3 | Hư Không Vô Danh | 9.000.000 | 43.000 | 145 **hoặc** 155 | toàn bộ, hồi chiêu ngắn nhất | NV 47 bước 5 / NV 50 bước 5 |

- **Hồi sinh**: `REST_10_M` = 600 giây (theo 20c §5.2; `Boss` chỉ đọc `secondsRest` của hình dạng đầu).
- **Rơi**: hình dạng 3 rơi **1 × item 2029 "Ống nghiệm Myuu"** cho người kết liễu (option 30 "Không thể giao dịch"). Ba hình dạng còn lại **không rơi gì**.
- **Lời thoại** (xuất hiện / trong lúc đánh / lúc chết) chép nguyên từ 20c §5.2, kể cả các câu prefix `|-2|` (Heart nói qua miệng một người chơi ngẫu nhiên trong bán kính 600).
- `isNotifyDisabled = true` — không bắn thông báo toàn server 4 lần liên tiếp mỗi khi đổi hình dạng.

### 4.1 Cách Heart đổi map giữa các hình dạng

Đây là chỗ khác biệt lớn nhất so với mọi boss nhiều hình dạng đang có: hình dạng 1 ở **map 166**, hình dạng 2–3 ở **map 145**.

`QuestBoss.joinMap()` kiểm tra map của khu đang giữ có nằm trong `mapJoin` của hình dạng hiện tại hay không.
- Nếu **không** (Heart vừa đổi từ hình dạng 1 sang 2, khu cũ vẫn ở map 166): boss **rời map ngay** rồi **giữ nguyên trạng thái `JOIN_MAP`** và chờ. Đúng bằng thời gian người chơi đi từ 166 sang 145 (NV 46 bước 2). Khi người chơi tới nơi, Heart hình dạng 2 hiện ra ở đúng khu của họ.
- Nếu chờ quá **10 phút** vẫn không thấy ai, boss bỏ lượt, về `REST` và chuỗi bắt đầu lại từ hình dạng 1.

Hình dạng 4 có `mapJoin = {145, 155}`. **Không cần code chọn map riêng**: boss đi vào khu của người chơi hợp lệ, nên ai đang ở NV 47 (đứng map 145) thì gặp ở 145, ai chọn nhánh NV 50 (đứng map 155) thì gặp ở 155.

## 5. So sánh với boss thế giới bản gốc

"HP hiệu dụng" = HP khai báo × hệ số giảm sát thương trong `injured` (theo [21c §1.1 (F)](../3-kiem-tra-can-bang/21c-boss-roi-do-can-bang.md)).

| Boss | HP khai báo (gốc → NV) | Hệ số giảm ST (gốc → NV) | **HP hiệu dụng (gốc → NV)** | Nhẹ hơn | Dame (gốc → NV) | Hồi sinh (gốc → NV) |
|---|---|---|---:|---:|---|---|
| Xên bọ hung | 300.000.000 → 2.790.000 | ×2 → ×1 | 600.000.000 → 2.790.000 | **215×** | 20–30k → 12–13k | 30 phút → 1 phút |
| Cooler | 700.000.000 → 6.300.000 | ×1,01 → ×1 | 707.000.000 → 6.300.000 | **112×** | 32–50k → 18,7k | 30 phút → 1 phút |
| Mabư 14h | 440.000.000 → 10.000.000 | ×1,11 + trần đòn → ×1 | 488.400.000 → 10.000.000 | **49×** | 500k → 24k | 10 phút → 1 phút |
| Black Goku | 2.500.000.000 → 6.700.000 | form 2 ×2 → ×1 | 4.000.000.000 → 6.700.000 | **597×** | 50–100k → 28,5k | 5 phút → 1 phút |
| Baby | 6.000.000.000 → 5.700.000 | ×2,857 → ×1 | 17.142.000.000 → 5.700.000 | **3.007×** | 200–250k → 30k | 15 phút → 1 phút |
| Cumber | 2.500.000.000 → 8.100.000 | form 2 ×2 → ×1 | 4.000.000.000 → 8.100.000 | **494×** | 50–100k → 37,2k | 5 phút → 1 phút |
| Heart | *(chưa có bản gốc)* → 31.800.000 | — → ×1 | — → 31.800.000 | — | *(20c đề xuất 150–500k)* → 42,5k | 10 phút |

> Mức giảm rất chênh nhau giữa các con **là cố ý**: bản nhiệm vụ được tính lại từ đầu theo mốc sức mạnh của chính nhiệm vụ đó ([21c §1.2](../3-kiem-tra-can-bang/21c-boss-roi-do-can-bang.md), P4 "60–120 giây solo cho boss thường, 240–420 giây cho boss cuối chương"), chứ không phải chia đều cho một hệ số. Baby giảm nhiều nhất vì bản gốc là con khó nhất server (5.270 người-phút).

**Đối chiếu dame với P5** (`dame = HP người chơi ÷ 12`, tức boss giết người chơi trong ~12 đòn):

| NV | Mốc SM | HP người chơi | Dame boss NV | Số đòn để chết |
|---|---:|---:|---:|---:|
| 30 | 1,4 tỉ | 149.615 | 13.000 | 11,5 |
| 34 | 3,5 tỉ | 224.939 | 18.700 | 12,0 |
| 37 | 6 tỉ | 288.328 | 24.000 | 12,0 |
| 38 | 7 tỉ | ~325.000 | 28.500 | 11,4 |
| 39 | 8 tỉ | 361.839 | 30.000 | 12,1 |
| 42 | 13 tỉ | 446.968 | 37.200 | 12,0 |
| 46–47 | 17,5 tỉ | 510.258 | 42.500 | 12,0 |

## 6. Cách đăng ký và khởi tạo

### 6.1 Hằng số id — `models/boss/BossID.java`

```java
//========================BOSS BẢN NHIỆM VỤ (tuyến nhiệm vụ mới)========================
public static final int XEN_BO_HUNG_NV = -2100;   // NV 30 — bản nhiệm vụ của -100
public static final int COOLER_NV      = -2101;   // NV 34 — bản nhiệm vụ của -29
public static final int MABU_14H_NV    = -2102;   // NV 37 — bản nhiệm vụ của -214
public static final int BLACK_GOKU_NV  = -2103;   // NV 38 — bản nhiệm vụ của -203
public static final int BABY_NV        = -2104;   // NV 39 — bản nhiệm vụ của -925
public static final int CUMBER_NV      = -2105;   // NV 42 — bản nhiệm vụ của -203999

//========================BOSS MỚI: HEART========================
public static final int HEART = -108108;          // NV 46 / 47 / 50 — 4 hình dạng
```

### 6.2 Dữ liệu — `models/boss/BossesData.java`

22 khai báo `BossData` mới, đặt ở cuối file dưới hai khối có tiêu đề rõ ràng: `XEN_BO_HUNG_NV_1..3`, `COOLER_NV_1..2`, `MABU_NV_1..5`, `BLACK_GOKU_NV_1..2`, `BABY_NV_1..3`, `CUMBER_NV_1..2`, `HEART`, `HEART_2`, `HEART_3`, `HEART_4`.

Hình dạng đầu dùng `secondsRest`; các hình dạng sau dùng `AppearType.ANOTHER_LEVEL` để `rest()` **không** bao giờ gọi thẳng chúng ra.

### 6.3 Nơi tạo boss — `models/boss/Boss_Manager/BossManager.java`

Thêm 7 `case` vào `createBoss(int)` và 7 dòng vào `loadBoss()`:

```java
this.createBoss(BossID.XEN_BO_HUNG_NV, 3);
this.createBoss(BossID.COOLER_NV, 3);
this.createBoss(BossID.MABU_14H_NV, 3);
this.createBoss(BossID.BLACK_GOKU_NV, 3);
this.createBoss(BossID.BABY_NV, 3);
this.createBoss(BossID.CUMBER_NV, 3);
this.createBoss(BossID.HEART, 3);
```

**Vì sao đặt trong `loadBoss()` mà không gọi theo nhiệm vụ** — đây là điểm tôi làm khác đề xuất ở 20c §5.5:

| Cách | Vấn đề |
|---|---|
| Gọi `createBoss` khi người chơi nhận nhiệm vụ | Phải móc vào `TaskService` — **file đang bị nhóm khác viết lại, không được sửa**. Ngoài ra mỗi lần gọi là một `Boss` mới được `addBoss` vào `BossManager.bosses` và **không bao giờ được gỡ ra** (`removeBoss` không được gọi ở đâu trong luồng này) → rò bộ nhớ, danh sách boss phình vô hạn theo số lượt nhận nhiệm vụ. |
| Đặt trong `loadBoss()` **+ chặn ở `joinMap()`** | Số lượng boss cố định, không rò. Lo ngại của 20c ("boss tự hiện ra ngay tick đầu vì `lastTimeRest = 0`") đã được xử lý triệt để: `QuestBoss.rest()` **không nhìn đồng hồ là chính**, nó chỉ cho `RESPAWN` khi quét thấy người chơi đúng bước nhiệm vụ. Không ai ở NV 30 thì boss −2100 nằm im vĩnh viễn. Chính 20c §5.5 cũng ghi cách này là hợp lệ. |

**Vì sao mỗi loại 3 bản**: [21c §7.3](../3-kiem-tra-can-bang/21c-boss-roi-do-can-bang.md) cảnh báo "200 người cùng làm NV 30, mỗi 30 phút 1 người qua bước → xếp hàng 100 giờ". Ba bản + hồi sinh 1 phút + mỗi bản đi vào một khu khác nhau làm cho hàng đợi gần như biến mất. Con số 3 chỉnh tự do ở `loadBoss()`.

### 6.4 Gọi `checkDoneTaskKillBoss`

Đã gọi **một chỗ duy nhất** cho cả 7 boss: `QuestBoss.reward(Player)`, chạy trong `die()` → `reward()`. Bảng đối chiếu `bossId + currentLevel → bước nhiệm vụ` nằm trong `TaskService.checkDoneTaskKillBoss` do nhóm khác cập nhật; hiện các id mới rơi vào nhánh không khớp `case` nào nên **không gây lỗi**, chỉ chưa có tác dụng.

Các id cần nhóm TaskService thêm `case`:

| Boss id | `currentLevel` | Bước nhiệm vụ dự kiến |
|---|---|---|
| −2100 | 0, 1 | `TASK_30_2` (cần 2 lượt) |
| −2100 | 2 | `TASK_30_3` |
| −2101 | 0, 1 | `TASK_34_4` (cần 2 lượt) |
| −2102 | 4 | `TASK_37_1` |
| −2103 | 0 / 1 | `TASK_38_2` / `TASK_38_3` |
| −2104 | 0, 1, 2 | `TASK_39_6` (cần 3 lượt) |
| −2105 | 0 / 1 | `TASK_42_3` / `TASK_42_4` |
| −108108 | 0 / 1 / 2 | `TASK_46_1` / `TASK_46_3` / `TASK_46_4` |
| −108108 | 3 | `TASK_47_5` **hoặc** `TASK_50_5` |

## 7. Chống đè lên boss thế giới và chống farm

### 7.1 Bảng `mapJoin` — đã đối chiếu với toàn bộ boss hiện có

| Boss NV | `mapJoin` | Boss thế giới dùng chung map | Cách xử lý |
|---|---|---|---|
| −2100 Xên bọ hung | 100 | −100 Xên bọ hung, −203 Black Goku, Bojack, boss vặt | Khóa theo **khu** |
| −2101 Cooler | 110 | −29 Cooler, boss vặt | Khóa theo **khu** |
| −2102 Mabư 14h | 127 | −214 Mabư 14h (phó bản 14h) | Khóa theo **khu** |
| −2103 Black Goku | 92, 93, 94, 96–100, 102 | −203 Black Goku, Dr.Kôrê, A19, Pic/Poc/KK, Bojack | Khóa theo **khu** |
| −2104 Baby | 14 | −925 Baby, boss sự kiện, −365 Ăn Trộm | Khóa theo **khu** |
| −2105 Cumber | 155 | −203999 Cumber | Khóa theo **khu** |
| −108108 Heart | 166, 145, 155 | **166 và 145 hiện không có boss nào**; 155 có −203999 Cumber | 166/145 trống hẳn; 155 khóa theo **khu** |

**Vì sao vẫn dùng đúng map của bản gốc**: map trong bảng trên là map mà **đặc tả nhiệm vụ** chỉ định (20b NV 30, 20c NV 34/37/38/39/42). Đổi sang map khác thì lời thoại, mốc `checkDoneTaskGoToMap` và toàn bộ dẫn dắt của nhiệm vụ sai hết. Chỉ có Heart là được đặt vào hai map **hoàn toàn trống boss** (166 Phòng thí nghiệm Myuu, 145 Võ Đài Siêu Cấp).

**Khóa theo khu** (`QuestBoss.isZoneOccupied`) — boss nhiệm vụ **từ chối vào** một khu nếu khu đó đang có:
- boss thế giới bản gốc tương ứng (so id: −100, −29, −214/−348, −203, −925, −203999), **hoặc**
- một bản nhiệm vụ khác cùng id (tránh 3 bản chồng lên nhau).

Boss lang thang (Ăn Trộm, Ở Dơ, boss sự kiện) **không** bị tính, vì nếu tính thì boss nhiệm vụ gần như không bao giờ vào nổi các map Trái Đất đông boss vặt.

Chiều ngược lại cũng an toàn: `Boss.joinMap()` của boss thế giới vốn đã bỏ qua mọi khu `!getBosses().isEmpty()`, nên boss nhiệm vụ đứng ở khu nào thì boss thế giới tự tránh khu đó.

### 7.2 Không farm được — bốn lớp chặn

1. **Không có người đúng bước thì boss không tồn tại.** `QuestBoss.rest()` chỉ đổi sang `RESPAWN` khi quét `Client.getPlayersSnapshot()` thấy một người chơi có `playerTask.taskMain.id` nằm trong danh sách nhiệm vụ của boss, **và** người đó đang đứng trong map hợp lệ, **và** khu đó chưa có boss gốc. Người chơi 90 tỉ SM đi ngang map 100 sẽ **không** thấy con −2100 nào.
2. **Không có gì để farm.** Bảng rơi đồ rỗng (mục 2).
3. **Không có thông báo toàn server.** `isNotifyDisabled = true` và `die()` được ghi đè để **bỏ** câu `ServerNotify "Đã tiêu diệt được…"`. Không ai biết boss ở đâu ngoài người đang làm nhiệm vụ.
4. **Tự dọn dẹp.** Khu trống người 5 phút → boss rời map (`autoLeaveMap`). Chờ khu hợp lệ quá 10 phút → bỏ lượt.

### 7.3 Tóm tắt vòng đời

```
REST ──(quét thấy người đúng bước + khu hợp lệ)──► RESPAWN ──► JOIN_MAP ──► CHAT_S ──► ACTIVE
  ▲                                                                │              │
  │                                                     (khu không hợp lệ:         │ chết
  │                                                      rời map, chờ tối đa       ▼
  │                                                      10 phút rồi bỏ lượt)    DIE ──► CHAT_E ──► LEAVE_MAP
  │                                                                                                    │
  └────────────── hình dạng cuối: hồi sinh sau `secondsRest` ─────────────────────────────────────────┘
                  (chưa phải hình dạng cuối: quay thẳng về RESPAWN, giữ khu)
```

## 8. Danh sách file đã thêm / đã sửa

### File mới (8)

| File | Nội dung |
|---|---|
| `SRC/src/nro/models/boss/quest/QuestBoss.java` | Lớp nền: gác cửa theo nhiệm vụ, khóa theo khu, `injured`, `reward` chỉ rơi đồ nhiệm vụ, `autoLeaveMap`, `die` không thông báo |
| `SRC/src/nro/models/boss/quest/XenBoHungNhiemVu.java` | −2100, 3 hình dạng |
| `SRC/src/nro/models/boss/quest/CoolerNhiemVu.java` | −2101, 2 hình dạng |
| `SRC/src/nro/models/boss/quest/MabuNhiemVu.java` | −2102, 5 hình dạng |
| `SRC/src/nro/models/boss/quest/BlackGokuNhiemVu.java` | −2103, 2 hình dạng |
| `SRC/src/nro/models/boss/quest/BabyNhiemVu.java` | −2104, 3 hình dạng |
| `SRC/src/nro/models/boss/quest/CumberNhiemVu.java` | −2105, 2 hình dạng |
| `SRC/src/nro/models/boss/heart/Heart.java` | −108108, 4 hình dạng, rơi item 2029 ở hình dạng 3 |

### File đã sửa (3)

| File | Sửa gì |
|---|---|
| `SRC/src/nro/models/boss/BossID.java` | Thêm 7 hằng số id (chỉ thêm, không đụng id cũ) |
| `SRC/src/nro/models/boss/BossesData.java` | Thêm 22 `BossData` ở cuối file (chỉ thêm) |
| `SRC/src/nro/models/boss/Boss_Manager/BossManager.java` | Thêm 7 `import`, 7 `case` trong `createBoss`, 7 dòng trong `loadBoss` |

**Không đụng tới**: `services/TaskService.java`, `consts/ConstTask.java`, `SRC/sql/**`, `map/service/ChangeMapService.java`, `shop/**`, `database/**`, và toàn bộ class boss thế giới bản gốc.

**Biên dịch**: `javac -nowarn -encoding UTF-8 -cp "lib/*"` trên 556 file — **sạch**, chỉ còn 2 dòng `Note:` về unchecked có sẵn từ trước.

## 9. Chỗ nghi ngờ / cần chủ dự án quyết

| # | Vấn đề | Tôi đã chọn gì | Vì sao cần bạn xem lại |
|---|---|---|---|
| 1 | **Heart hình dạng 4 rơi 1 đồ Thần Linh 100%** — [21c §6.3.a](../3-kiem-tra-can-bang/21c-boss-roi-do-can-bang.md) đề xuất vậy làm "phần thưởng kết tuyến" | **Không cho rơi.** Heart nằm trong bước nhiệm vụ bắt buộc (NV 47/50 bước 5) nên theo NT1 thì không được rơi trang bị | Nếu bạn vẫn muốn tặng đồ Thần Linh khi xong tuyến thì nên trao qua **`rewardDoneTask` của NV 47/50**, không phải rơi từ boss — như vậy mới chắc chắn 1 lần/tài khoản và không ai cướp được đòn cuối. **Cần bạn xác nhận.** |
| 2 | **Chỉ số Heart**: 20c ghi HP 1,5–2 tỉ / dame 150k–500k; 21c §6.2.a ghi 7–9 triệu / dame 42,5k | Lấy theo **21c** | Chênh nhau ~250 lần. 21c tính theo công thức sát thương thật, 20c ước lượng bằng cảm tính (21c §1.3 đã chỉ ra 20a/20b/20c sai lệch ~25 lần ở chỗ này). Nhưng đây là **boss cuối cả tuyến**, bạn có thể muốn nó "hoành tráng" hơn — nói một tiếng là tôi nâng. |
| 3 | **Boss nhiệm vụ dùng chung map với bản gốc** (6/7 con) | Giữ đúng map mà đặc tả nhiệm vụ chỉ định, chặn trùng ở mức **khu** | Nếu bạn muốn tách hẳn **map** thì phải sửa đặc tả nhiệm vụ tương ứng (20b/20c) — ví dụ chuyển bước hạ Cooler từ 110 Hang băng sang 108/109, chuyển Black Goku về một map riêng. Việc đó ảnh hưởng lời thoại và mốc `checkDoneTaskGoToMap`, nên tôi chưa tự làm. |
| 4 | **Tên hiển thị trùng hệt bản gốc** ("Cooler", "Baby", "Black Goku"…) | Giữ trùng, cho khớp lời kể nhiệm vụ | Đổi lại thì GM dễ phân biệt hơn khi xem `showListBoss`, nhưng người chơi sẽ thấy tên lạ giữa mạch truyện. Nếu bạn muốn, tôi thêm hậu tố ẩn chỉ hiện với admin. |
| 5 | **Mỗi loại 3 bản boss** | 3 | Đây là ước lượng theo số người chơi đồng thời. Bạn biết lưu lượng thật của server thì cho con số chính xác hơn. |
| 6 | **Mabư 14h bản nhiệm vụ bỏ ràng buộc "chỉ chết bằng Quả cầu kênh khí"** | Bỏ | Đúng tinh thần "boss cốt truyện phải qua được", nhưng làm mất nét đặc trưng của Mabư. Nếu bạn muốn giữ, cần bảo đảm NV 37 nằm sau bước học Quả cầu kênh khí. |
| 7 | **`checkDoneTaskKillBoss` vẫn chỉ tính cho người kết liễu** | Giữ nguyên cơ chế hiện có | [21c §7.1](../3-kiem-tra-can-bang/21c-boss-roi-do-can-bang.md) đề nghị đổi sang "mọi người đóng góp ≥ 5% sát thương". Việc đó phải sửa `Boss.java` (bảng đóng góp sát thương) — **ảnh hưởng toàn bộ boss thế giới**, nằm ngoài phạm vi lần này. Với boss nhiệm vụ thì rủi ro thấp hơn nhiều (3 bản, hồi sinh 1 phút, chỉ người đúng bước mới thấy), nhưng vẫn nên làm. |
| 8 | **Vật phẩm 2029 chưa có trong `item_template`** | Đã chặn bằng kiểm tra kích thước mảng — chưa import SQL thì Heart không rơi gì, không crash | Nhớ import `SRC/sql/patch/01-vat-pham-moi.sql` **và tăng phiên bản bảng item** trước khi mở tuyến, nếu không NV 46 bước 4 kẹt cứng. |
| 9 | **Dải id còn trống −2106 … −2199** | Chưa dùng | 21c §6.4 khuyến nghị phương án C cho 6 con "quan trọng nhất" và phương án A (phân biệt theo bước nhiệm vụ) cho phần còn lại. Nếu sau này bạn muốn dựng thêm bản nhiệm vụ cho Fide, Tiểu đội sát thủ, Android… thì dải này đủ chỗ và chỉ cần kế thừa `QuestBoss` là xong. |
