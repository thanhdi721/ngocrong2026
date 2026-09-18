# 21a — Kiểm toán vật phẩm phần thưởng của tuyến nhiệm vụ mới

> **Phạm vi:** toàn bộ vật phẩm được trao trong [20a](../2-thiet-ke-nhiem-vu-moi/20a-chi-tiet-nhiem-vu-00-15.md) / [20b](../2-thiet-ke-nhiem-vu-moi/20b-chi-tiet-nhiem-vu-16-31.md) / [20c](../2-thiet-ke-nhiem-vu-moi/20c-chi-tiet-nhiem-vu-32-47.md) — thưởng bước, thưởng hoàn thành nhiệm vụ, đồ trao miễn phí ở bước 0, và vật phẩm nhiệm vụ.
> **Mục đích:** trả lời câu hỏi của chủ dự án — *"phát vật phẩm qua nhiệm vụ có phá vỡ logic kinh tế vật phẩm sẵn có không?"*
> **Tài liệu này chỉ đọc và đánh giá.** Không sửa code, không sửa 20a/20b/20c. Đặc tả item mới xem [21b](21b-item-moi-dac-ta.md); cân bằng rơi đồ boss xem [21c](21c-boss-roi-do-can-bang.md).
> **Nguồn số liệu:** `database team2026.sql` (bảng `item_template`, `item_shop`, `tab_shop`, `shop`), source Java trong `SRC/src/nro/**`, và các tài liệu [02b](../1-he-thong-hien-tai/02b-database-du-lieu-template.md), [06](../1-he-thong-hien-tai/06-vat-pham.md), [07](../1-he-thong-hien-tai/07-shop-giao-dich-ky-gui.md), [08](../1-he-thong-hien-tai/08-nang-cap-do-dap-do.md), [09](../1-he-thong-hien-tai/09-quai-roi-do-exp.md), [10](../1-he-thong-hien-tai/10-rong-than.md), [11](../1-he-thong-hien-tai/11-boss.md), [11b](../1-he-thong-hien-tai/11b-boss-su-kien-pho-ban.md), [14](../1-he-thong-hien-tai/14-ban-do-pho-ban.md), [16](../1-he-thong-hien-tai/16-su-kien-minigame-giai-dau.md), [17](../1-he-thong-hien-tai/17-bang-hoi.md), [19](../1-he-thong-hien-tai/19-vip-nap-tien-tien-te.md).

---

## Mục lục

1. [Kết luận nhanh](#1-kết-luận-nhanh)
2. [Bảng tổng vật phẩm toàn tuyến](#2-bảng-tổng-vật-phẩm-toàn-tuyến)
3. [Hồ sơ từng vật phẩm đã có trong DB — chỉ số + nguồn hiện tại](#3-hồ-sơ-từng-vật-phẩm-đã-có-trong-db)
4. [Đánh giá ảnh hưởng](#4-đánh-giá-ảnh-hưởng)
   - 4.1 [Cơ chế bán lại và tổng vàng quy đổi](#41-cơ-chế-bán-lại-và-tổng-vàng-quy-đổi)
   - 4.2 [Ngọc Rồng → Rồng Thần: đường phát ngọc lớn nhất](#42-ngọc-rồng--rồng-thần)
   - 4.3 [Đồ Thần Linh và chuỗi khoá endgame](#43-đồ-thần-linh-và-chuỗi-khoá-endgame)
   - 4.4 [Bông tai Porata](#44-bông-tai-porata)
   - 4.5 [Đá nâng cấp 1074–1078 — nhầm hệ thống](#45-đá-nâng-cấp-10741078--nhầm-hệ-thống)
   - 4.6 [Đá ngũ sắc, Capsule Vàng, Sao pha lê đen — ba vật phẩm chết](#46-ba-vật-phẩm-chết)
   - 4.7 [Đường cong nâng cấp +1 → +8](#47-đường-cong-nâng-cấp-1--8)
   - 4.8 [Vật phẩm mở khoá tính năng sớm hơn dự kiến](#48-vật-phẩm-mở-khoá-tính-năng-sớm-hơn-dự-kiến)
   - 4.9 [Bảng xếp hạng mức độ ảnh hưởng](#49-bảng-xếp-hạng-mức-độ-ảnh-hưởng)
5. [Xung đột / đề xuất sửa](#5-xung-đột--đề-xuất-sửa)
6. [Vật phẩm chưa có trong DB (id ≥ 2000)](#6-vật-phẩm-chưa-có-trong-db-id--2000)
7. [Nguyên tắc phát thưởng đề xuất](#7-nguyên-tắc-phát-thưởng-đề-xuất)
8. [Ghi chú / điểm cần chủ dự án quyết](#8-ghi-chú--điểm-cần-chủ-dự-án-quyết)

---

## 1. Kết luận nhanh

| Chỉ số | Giá trị |
|---|---|
| Vật phẩm **đã có trong DB** được tuyến mới trao | **68 id** (tính cả 12 id trang bị cấp 1 và 6 id sách theo hành tinh) |
| Vật phẩm **mới** đề xuất tạo (id ≥ 2000) | **32 id** |
| Chỗ xung đột mức **Cao** | **6** |
| Chỗ xung đột mức **Trung bình** | **8** |
| **Tổng vàng thu được nếu bán sạch đồ thưởng** (đi theo form bán Thỏi vàng) | **≈ 553.006.000 vàng** |
| Trường hợp xấu nhất (bán Thỏi vàng qua gói lệnh thô, `ShopService.sellItem`) | **≈ 2.405.006.000 vàng** |
| **Ngọc xanh** quy đổi gián tiếp qua Rồng Thần | **tối đa 30.000 ngọc** |

**Ba kết luận lớn nhất:**

1. Quyết định *"nhiệm vụ không thưởng vàng/ngọc"* **đang bị phá bằng vật phẩm**. Ba đường rò lớn: **4 Thỏi vàng** (NV 13, 14), **trọn bộ 5 món Thần Linh** (NV 40–44) và **3 bộ Ngọc Rồng đầy đủ** (NV 45, 47/50). Riêng bộ Ngọc Rồng cho phép gọi Rồng Thần 3 lần và ước "+10K Ngọc" mỗi lần — tức **30.000 ngọc xanh miễn phí**, gấp 30 lần giá một Bông tai Porata ở shop Uron.
2. Ba dòng vật phẩm được thiết kế mô tả sai chức năng: **Đá nâng cấp 1074–1078** không dùng để đập đồ +1→+8 (đó là 220–224), **Đá ngũ sắc 674** không phải nguyên liệu pha lê hoá (pha lê hoá chỉ ăn vàng + ngọc), **Capsule Vàng 574** không có nhánh xử lý nào trong `UseItem` (dùng không ra gì).
3. Ba vật phẩm hiếm nhất server bị phát thẳng: **Bông tai Porata cấp 2 (921)** và **cấp 3 (1819)** — cấp 3 hiện **không có nguồn nào trong code** vì mảnh 1820 không rơi ở đâu; và **trọn bộ Thần Linh** — hiện chỉ rơi 1–10% từ 5 nhóm boss, mở khoá shop Đồ Hủy Diệt của Bill.

---

## 2. Bảng tổng vật phẩm toàn tuyến

Cột "Tổng" tính theo **nhánh A** (task 20 → 31 → 47). Khác biệt của nhánh B (task 48 → 49 → 50) ghi trong ngoặc.
Nguồn: các dòng "Thưởng khi hoàn thành" và cột "Thưởng bước"/"trao miễn phí" của 20a §NV0–15, 20b §NV16–31/48/49, 20c §NV32–47/50.

### 2.1 Vật phẩm đã có trong DB

| id | Tên | Trao ở nhiệm vụ nào (số lượng) | Tổng toàn tuyến |
|---|---|---|---|
| 13 | Đậu thần cấp 1 | NV 0 ×5, NV 2 ×10, NV 5 ×10 | **25** |
| 193 | Gói 10 viên Capsule | NV 0 ×1, NV 4 ×2, NV 7 ×75 | **78** |
| 12 | Rada cấp 1 | NV 1 ×1 | **1** |
| 0, 6, 21, 27 (TĐ)<br>1, 7, 22, 28 (NM)<br>2, 8, 23, 29 (XD) | Bộ trang bị cấp 1 theo hành tinh | NV 3 ×1 bộ | **4 món** |
| 57 | Rada cấp 2 | NV 5 ×1 | **1** |
| 295 | Gói 30 đậu thần cấp 3 | NV 6, 8, 11, 13, 15 — mỗi NV ×1 | **5** |
| 58 | Rada cấp 3 | NV 9 ×1 | **1** |
| 94 / 101 / 108 | Sách Kamejoko / Masenko / Antomic lv1 | NV 10 bước 0 ×1 | **1** |
| 66 / 79 / 87 | Sách đấm Dragon / Demon / Galick lv1 | NV 10 hoàn thành ×1 | **1** |
| 401 | Đổi đệ tử | NV 12 ×1 | **1** |
| 402 | Nâng kỹ năng 1 đệ tử | NV 12 ×1 | **1** |
| **457** | **Thỏi vàng** | NV 13 ×1, NV 14 ×3 | **4** |
| 223 | Đá Titan | NV 17 bước 0 ×10 (trao miễn phí) | **10** |
| 222 | Đá Ruby | NV 17 bước 0 ×10 (trao miễn phí) | **10** |
| 1074 | Đá nâng cấp cấp 1 | NV 15 ×1, NV 16 ×20, NV 17 ×30 *(nhánh B thêm NV 48 ×20)* | **51** *(B: 71)* |
| 1075 | Đá nâng cấp cấp 2 | NV 18 ×20, NV 19 ×30, NV 20 ×40 | **90** *(B: 50)* |
| 1076 | Đá nâng cấp cấp 3 | NV 21 ×20, NV 22 ×30, NV 23 ×50 | **100** |
| 1077 | Đá nâng cấp cấp 4 | NV 24 ×20, NV 25 ×30, NV 27 ×40 | **90** |
| 1078 | Đá nâng cấp cấp 5 | NV 28 ×20, NV 29 ×25, NV 30 ×30, NV 31 ×50, NV 35 ×5 | **130** *(B: 80)* |
| 987 | Đá bảo vệ | NV 17 ×3, NV 29 ×5, NV 33 ×5 *(B: thêm NV 48 ×8, NV 49 ×10)* | **13** *(B: 31)* |
| 611 | Bản đồ kho báu | NV 21 ×2 | **2** |
| **574** | **Capsule Vàng** | NV 23 ×5 | **5** |
| **674** | **Đá ngũ sắc** | NV 26 bước 0 ×6 (miễn phí), NV 26 ×3, NV 46 ×1 *(B: thêm NV 49 ×5)* | **10** *(B: 15)* |
| 447 | Sao pha lê lục | NV 26 bước 0 ×2 (miễn phí), NV 26 ×2 *(B: thêm NV 49 ×3)* | **4** *(B: 7)* |
| 446 | Sao pha lê vàng | NV 30 ×2 | **2** |
| 445 | Sao pha lê cam | NV 31 ×2 *(chỉ nhánh A)* | **2** *(B: 0)* |
| 352 | Đậu thần cấp 8 | NV 32 ×30, NV 33 ×50, NV 36 ×50, NV 39 ×50 | **180** |
| **454** | **Bông tai Porata cấp 1** | NV 34 ×1 | **1** |
| 1795 | Bình hút năng lượng | NV 36 bước 0 ×1 (trao miễn phí) | **1** |
| **921** | **Bông tai Porata cấp 2** | NV 37 ×1 | **1** |
| **964** | **Sao pha lê đen cấp 2** | NV 38 ×1 | **1** |
| **1819** | **Bông tai Porata cấp 3** | NV 39 ×1 | **1** |
| **555 / 557 / 559** | **Áo Thần Linh / Thần Namếc / Thần Xayda** | NV 40 ×1 | **1** |
| **556 / 558 / 560** | **Quần Thần Linh / Thần namếc / Thần Xayda** | NV 41 ×1 | **1** |
| **562 / 564 / 566** | **Găng Thần Linh / Namếc / Xayda** | NV 42 ×1 | **1** |
| **563 / 565 / 567** | **Giầy Thần Linh / Namếc / Xayda** | NV 43 ×1 | **1** |
| **561** | **Nhẫn Thần Linh** | NV 44 ×1 | **1** |
| **14, 15, 16, 17, 18, 19** | **Ngọc Rồng 1–6 sao** | NV 45 ×1 mỗi loại, NV 47/50 ×2 mỗi loại | **3 viên mỗi loại (18 viên)** |
| **20** | **Ngọc Rồng 7 sao** | NV 45 ×1, NV 46 ×3, NV 47/50 ×2 | **6** |
| 638 | Bình chứa Commeson | NV 49 bước 1 ×1 *(chỉ nhánh B)* | **0** *(B: 1)* |
| *(chưa chọn id)* | Bùa / cải trang có option 106 "Không ảnh hưởng bởi cái lạnh" | NV 34 bước 0 — 20c §NV34 chỉ ghi "nên trao", **chưa chốt item** | **?** |

**68 id** đã có trong DB (đếm cả 12 id trang bị cấp 1 và 6 id sách theo hành tinh, vì mỗi hành tinh nhận id khác nhau).

### 2.2 Vật phẩm mới (id ≥ 2000) — xem §6

32 id: 2001, 2002, 2003, 2004, 2006, 2010, 2011 (20a) · 2012, 2013, 2040, 2041, 2042, 2043, 2044, 2070, 2071, 2072, 2073, 2074 (20b) · 2014, 2015, 2016, 2100, 2101, 2102, 2103, 2104, 2105, 2106, 2107, 2120, 2121 (20c).

---

## 3. Hồ sơ từng vật phẩm đã có trong DB

Cột `TYPE`, `level`, `power_require`, `gold`, `gem` lấy trực tiếp từ `database team2026.sql` bảng `item_template`.
Cột **Nguồn hiện tại** ghi rõ shop nào / quái-boss nào / kết hợp nào.

| id | Tên | TYPE | lv | power_require | gold | gem | NGUỒN HIỆN TẠI |
|---|---|---|---|---|---|---|---|
| 13 | Đậu thần cấp 1 | 6 | 1 | 1 | 0 | 0 | Cây đậu thần cấp 1 (`MagicTree.PEA_TEMP[0]`); Gói 293 ở shop URON 10 ngọc |
| 193 | Gói 10 viên Capsule | 27 | 0 | 0 | 0 | 3 | Không có trong `item_shop`; nguồn chính là thưởng nhiệm vụ cũ `TASK_7_2` (12 §rewardDoneTask) |
| 12 | Rada cấp 1 | 4 | 1 | 1.300 | 600 | 0 | **Shop BUNMA/DENDE/APPULE (npc 7/8/9) tab Phụ kiện — 600 vàng** |
| 0 / 1 / 2 | Áo vải 3 lỗ / sợi len / vải thô | 0 | 1 | 1.200 | 500 | 0 | **Shop BUNMA/DENDE/APPULE — 500 vàng** |
| 6 / 7 / 8 | Quần vải đen / sợi len / vải thô | 1 | 1 | 1.000 | 400 | 0 | **Shop 3 hành tinh — 400 vàng** |
| 21 / 22 / 23 | Găng vải đen / sợi len / vải thô | 2 | 1 | 1.500 | 700 | 0 | **Shop 3 hành tinh — 700 vàng** |
| 27 / 28 / 29 | Giầy nhựa / sợi len / vải thô | 3 | 1 | 800 | 300 | 0 | **Shop 3 hành tinh — 300 vàng** |
| 57 | Rada cấp 2 | 4 | 2 | 13.000 | 6.000 | 0 | **Shop 3 hành tinh — 6.000 vàng** |
| 58 | Rada cấp 3 | 4 | 3 | 28.000 | 12.000 | 0 | **Shop 3 hành tinh — 12.000 vàng** |
| 295 | Gói 30 đậu thần cấp 3 | 27 | 1 | 0 | 0 | 0 | **Shop URON (npc 16) tab 19 — 10 ngọc xanh** |
| 94 / 101 / 108 | Sách chưởng lv1 | 7 | 1 | 0 | 0 | 10 | **Shop URON tab Sách Chưởng — 10 ngọc xanh** |
| 66 / 79 / 87 | Sách đấm lv1 | 7 | 1 | 1 | 0 | 5 | **Shop URON tab Sách Võ — 5 ngọc xanh** |
| 401 | Đổi đệ tử | 27 | 1 | 0 | 0 | 100 | **Shop SANTA (npc 39) tab Cửa Hàng — 100 ngọc xanh** |
| 402 | Nâng kỹ năng 1 đệ tử | 27 | 1 | 0 | 0 | 100 | **Shop SANTA — 100 ngọc xanh** |
| **457** | **Thỏi vàng** | 27 | 1 | 1.500.000 | **500.000.000** | 0 | **Gói VIP nạp tiền thật** (19 §VIP: VIP1 50.000đ → ×200, VIP2 100.000đ → ×500); điểm danh NPC Bò Mộng 1 lần/ngày ×100 (16); vé xổ số (16 §Vé mỗi số). **Không rơi từ quái/boss, không bán ở shop thường.** Bán lại cho shop: **37.000.000 vàng/thỏi** qua form `BANSLL` (07 §6.2) |
| 222 | Đá Ruby (đá nâng cấp quần) | 14 | 0 | 0 | 0 | 0 | Rơi 20% ở map lạnh 105–110 (09 §mục 27); Rương Gỗ 570; giải Vô địch ĐHVT (16) |
| 223 | Đá Titan (đá nâng cấp áo) | 14 | 0 | 0 | 0 | 0 | Như trên |
| 1074–1078 | Đá nâng cấp cấp 1–5 | 27 | 1–5 | 0 | 0 | 0 | **Shop THIEN_SU (Whis, npc 56) map 154 — 1/2/3/4/5 ngọc xanh**. Dùng làm 1 trong 4 nguyên liệu **Chế tạo trang bị Thiên Sứ** (08 §12.1), **không** dùng cho đập đồ +1→+8 |
| 987 | Đá bảo vệ | 27 | 1 | 0 | 0 | 0 | **Shop bang hội SHOP_CLAN (Giu-ma Đầu Bò npc 47, map 153) — 1 điểm capsule bang** (17 §11); Hộp quà 5 sao 736 (10%); gói VIP1 ×5 / VIP2 ×10 |
| 611 | Bản đồ kho báu | 27 | 1 | 0 | 0 | 0 | **Rơi 100% ×1–3 từ Trung uý Xanh Lơ (−6) trong phó bản Doanh trại Độc Nhãn map 62** (11b §1, 14 §481); Ninja Áo Tím 30% ×1–2 |
| **574** | **Capsule Vàng** | 27 | 1 | 15.000 | 0 | 0 | **Không có trong `item_shop`, không rơi ở đâu, và `UseItem` không có `case 574`** → item chết (xem §4.6) |
| **674** | **Đá ngũ sắc** | 27 | 1 | 0 | 0 | 0 | **Không có nguồn nào trong code** (chỉ tồn tại hằng `ConstItem.DA_NGU_SAC` không được dùng). Mô tả DB ghi "dùng để đục lỗ" nhưng `PhaLeHoaTrangBi` không nhận nguyên liệu (xem §4.6) |
| 445 / 446 / 447 | Sao pha lê cam / vàng / lục | 30 | 5/6/7 | 1 | 0 | 0 | **Rơi 10% ở mọi map, chọn đều 1 trong 7 màu 441–447** (09 §mục 35) → ~1,43%/màu; Rương Gỗ 570; Cục xương 460 (75%); Hộp quà Noel 648 |
| **964** | **Sao pha lê đen cấp 2** | 30 | 8 | 1 | 0 | 0 | **Không có nguồn nào**; không nằm trong dãy nâng cấp 1416–1422 của `NangCapSaoPhaLe`, không có option gốc trong code (xem §4.6) |
| 352 | Đậu thần cấp 8 | 6 | 8 | 1 | 0 | 0 | Cây đậu thần **cấp 8** (`MagicTree.PEA_TEMP[7]`, hồi 64.000 HP/KI); **Gói 30 đậu thần cấp 8 (596) ở shop URON tab 19 — 10 ngọc xanh** |
| **454** | **Bông tai Porata cấp 1** | 27 | 1 | 1.500.000 | 0 | **1.000** | **Shop URON tab 19 — 1.000 ngọc xanh** |
| 1795 | Bình hút năng lượng | 75 | 0 | 0 | 0 | 0 | Rơi 20% mỗi lần Hirudegarn (mob 70, map 126) chuyển giai đoạn (09 §7.1) |
| **921** | **Bông tai Porata cấp 2** | 27 | 1 | 1.500.000 | 0 | 0 | **Chỉ từ combine**: 454 + 9.999 Mảnh vỡ bông tai (933) + 200.000.000 vàng + 1.000 ngọc, tỉ lệ **50%** (08 §8.1). Mảnh 933 rơi 10% ở map 156–159 |
| **1819** | **Bông tai Porata cấp 3** | 27 | 1 | 1.500.000 | 0 | 0 | **Chỉ từ combine**: 921 + **20.000** Mảnh vỡ bông tai cấp 3 (1820) + 200.000.000 vàng + 1.000 ngọc, tỉ lệ 50% (08 §8.3). **Item 1820 không rơi ở bất kỳ đâu trong code** → cấp 3 hiện **bất khả đắc** |
| 555 / 557 / 559 | Áo Thần Linh / Namếc / Xayda | 0 | 13 | 0 | **200.000.000** | 0 | `ItemService.randDoTL` (quái) / `randDoTLBoss` (boss). Xác suất ra Áo = **27,84%** của lần trúng đồ TL, hành tinh random 1/3 (06 §9.2). Nguồn: Hirudegarn 2,5%/lần chuyển giai đoạn; map lạnh 105–110 **1/50.000**; Cooler 10%; Baby 10%; Siêu Bọ Hung / Black Goku / Cumber 5%; nhóm Mabư 12h 1% |
| 556 / 558 / 560 | Quần Thần | 1 | 13 | 0 | **250.000.000** | 0 | Như trên; xác suất ra Quần = 30,375% |
| 562 / 564 / 566 | Găng Thần | 2 | 13 | 0 | **500.000.000** | 0 | Như trên; Găng = 22,5% |
| 563 / 565 / 567 | Giầy Thần | 3 | 13 | 0 | **170.000.000** | 0 | Như trên; Giày = 9,28% |
| 561 | Nhẫn Thần Linh | 4 | 13 | 0 | **500.000.000** | 0 | Như trên; Nhẫn = 10% (không phân hành tinh); Hộp thần linh 1775 (20%) |
| 14–20 | Ngọc Rồng 1–7 sao | 12 | 0 | 0 | 0 | 0 | Rơi **1% ở mọi map** cho 17–20 (09 §mục 34); **10/70 ≈ 14,3%** cho 19/20 ở map 3 hành tinh / Nappa / tương lai / lạnh (09 §mục 30); boss Mẫu B **10% ×1–3 viên** (2–7 sao); Rương Gỗ; Hộp quà Noel |
| 638 | Bình chứa Commeson | 27 | 1 | 0 | 0 | 0 | **Nhân bản Commeson** ở NPC Potage map 140, **1 lần/ngày**, thắng bản sao dame ×10 HP ×10 trong 5 phút (14 §763). Bản gốc có option Hạn sử dụng 30 ngày + Không thể giao dịch. Hiệu lực: **60 phút giảm 90% sát thương từ quái** (06 §640) |

> **Lưu ý về 5 dòng `item_shop` "ma"**: trong DB có các dòng bán 555, 556, 559, 561, 563 ở **tab 24 và tab 26** với giá 100–555 vàng. Hai tab này **không có dòng nào trong `tab_shop`** → không thuộc shop nào → **không mở được trong game**. Đây là dữ liệu test chết, **không** phải nguồn mua đồ Thần Linh.

---

## 4. Đánh giá ảnh hưởng

### 4.1 Cơ chế bán lại và tổng vàng quy đổi

Cơ chế thật, đọc từ `SRC/src/nro/models/shop/ShopService.java` dòng 1065–1130 (khớp 07 §6.1):

```
cost = template.gold
nếu id == 457 (Thỏi vàng): quantity = 1, cost = template.gold (500.000.000)
ngược lại:                  cost = cost / 4
nếu cost == 0: cost = 1
cost = cost × quantity
```

**Chặn bán chỉ có hai trường hợp:** item id 570 (Rương Gỗ), và item có **option 93 "Hạn sử dụng"** với param > 0.
→ **Option 30 "Không thể giao dịch" và option 154 "Không thể bán lại" KHÔNG chặn bán cho shop.** Đây là chi tiết then chốt cho mọi đề xuất ở §5: muốn một món thưởng không đổi ra vàng được, phải gắn **option 93**, hoặc dùng item có `gold = 0`, hoặc tạo item nhiệm vụ riêng.

**Tổng vàng nếu người chơi bán sạch đồ thưởng toàn tuyến (nhánh A):**

| Nhóm | Phép tính | Vàng |
|---|---|---|
| Áo Thần Linh | 200.000.000 / 4 | 50.000.000 |
| Quần Thần | 250.000.000 / 4 | 62.500.000 |
| Găng Thần | 500.000.000 / 4 | 125.000.000 |
| Giầy Thần | 170.000.000 / 4 | 42.500.000 |
| Nhẫn Thần Linh | 500.000.000 / 4 | 125.000.000 |
| **Cộng bộ Thần Linh** | | **405.000.000** |
| Thỏi vàng ×4 | 4 × 37.000.000 (form `BANSLL`) | **148.000.000** |
| Rada 12 + 57 + 58 | 150 + 1.500 + 3.000 | 4.650 |
| Bộ trang bị cấp 1 | (500+400+700+300)/4 | 475 |
| Mọi item còn lại (`gold = 0` → 1 vàng/cái) | 840 món | 840 |
| **TỔNG** | | **≈ 553.006.000 vàng** |

**Trường hợp xấu nhất:** 07 §6.2 ghi rõ nếu client gửi thẳng lệnh bán không qua form `BANSLL` thì `sellItem` trả **500.000.000 vàng/thỏi**. Khi đó 4 Thỏi vàng = **2.000.000.000 vàng**, tổng lên **≈ 2,405 tỷ vàng**.

**Đối chiếu:** mốc "mở giới hạn sức mạnh nhanh" tốn 50.000.000 vàng; pha lê hoá 0→1 lỗ tốn 5.000.000 vàng; nâng cấp +6→+7 tốn 100.000.000 vàng. **553 triệu vàng đủ trả 11 lần mở giới hạn nhanh, hoặc 110 lần thử pha lê hoá.** Đây chính xác là thứ mà quyết định "không thưởng vàng" muốn tránh — chỉ là đi vòng qua ba lô.

> **Kết luận 4.1:** *không thưởng vàng* hiện chỉ đúng trên giấy. **Mức: Cao.**

### 4.2 Ngọc Rồng → Rồng Thần

Đây là đường rò **lớn nhất**, lớn hơn cả tiền bán đồ, và nó không hiện ra trong bảng phần thưởng.

Theo 10 §2.3 (`SummonDragon.summonShenron`), gọi **Rồng Thần 1 sao** tiêu **đúng 1 viên mỗi loại 14, 15, 16, 17, 18, 19, 20**. Tuyến mới trao:

| Nhiệm vụ | Ngọc Rồng trao | Số bộ đầy đủ |
|---|---|---|
| NV 45 | 14–20, mỗi loại ×1 | 1 bộ |
| NV 46 | 20 ×3 | (lẻ) |
| NV 47 / NV 50 | 14–20, mỗi loại ×2 | 2 bộ |
| **Cộng** | **21 viên 1–6 sao + 6 viên 7 sao** | **3 bộ gọi rồng** |

Điều ước khả dụng (10 §2.5), rồng 1 sao:

| Điều ước | Hiệu ứng thật trong `confirmWish` |
|---|---|
| Giàu có +2 Tỏi Vàng | `inventory.gold = 2.000.000.000` (**gán bằng**, chỉ có tác dụng 1 lần) |
| Giàu có +10K Ngọc | `gem += 10.000` — **cộng dồn, lặp được** |
| +200 Tr Sức mạnh và tiềm năng | `addSMTN(2, 200.000.000)` (đi qua giới hạn) |
| Chí mạng Gốc +2% | `critdragon += 2`, trần 10% |

**3 bộ Ngọc Rồng = 3 lần ước "+10K Ngọc" = 30.000 ngọc xanh miễn phí.**

Quy đổi bằng giá shop thật:
- 30 × Bông tai Porata 454 (1.000 ngọc/cái, shop URON), hoặc
- 300 × Đổi đệ tử 401 (100 ngọc), hoặc
- 6.000 × Đá nâng cấp cấp 5 1078 (5 ngọc, shop Whis), hoặc
- 3.000 lần ép sao trang bị (10 ngọc/lần, 08 §4.1).

Ngoài ra Ngọc Rồng 14–20 còn là **nguyên liệu ép sao trực tiếp** (`CombineSystem.isDaPhaLe` nhận `type == 30` **hoặc id 14–20**, 08 §4.1): NR 7 sao cho HP +5%, NR 3 sao cho Sức đánh +3%… Tức 27 viên Ngọc Rồng cũng chính là **27 lần ép sao miễn phí nguyên liệu**.

> **Kết luận 4.2:** tuyến nhiệm vụ "không thưởng ngọc" đang phát **30.000 ngọc** qua cửa sau. **Mức: Cao — nghiêm trọng nhất toàn tuyến.**

### 4.3 Đồ Thần Linh và chuỗi khoá endgame

NV 40 → 44 trao **trọn 5 món Thần Linh** (Áo, Quần, Găng, Giầy, Nhẫn), mỗi nhiệm vụ 1 món.

**Nguồn hiện tại và độ hiếm thật.** Đồ Thần Linh chỉ sinh ra từ `ItemService.randDoTL` / `randDoTLBoss` (06 §9.2), với xác suất chọn món lồng nhau: Nhẫn 10% · Găng 22,5% · Quần 30,375% · Áo 27,84% · Giày 9,28%; **hành tinh của món là random 1/3**, không theo hành tinh người nhặt.

Tính số lần giết boss cần thiết để tự kiếm được **đúng món, đúng hành tinh** ở boss có tỉ lệ rơi đồ TL 5% (Siêu Bọ Hung, Black Goku, Cumber):

| Món | Xác suất mỗi lần giết | Số lần giết trung bình |
|---|---|---|
| Nhẫn 561 (không phân hành tinh) | 0,05 × 0,10 = 0,5% | **200 lần** |
| Găng đúng hành tinh | 0,05 × 0,225 / 3 = 0,375% | **267 lần** |
| Quần đúng hành tinh | 0,05 × 0,30375 / 3 = 0,506% | **198 lần** |
| Áo đúng hành tinh | 0,05 × 0,2784 / 3 = 0,464% | **216 lần** |
| Giầy đúng hành tinh | 0,05 × 0,0928 / 3 = 0,155% | **646 lần** |

→ Tự kiếm đủ bộ 5 món cần **cỡ 1.500 lượt giết boss 5%**, hoặc khoảng **250.000 con quái** nếu cày ở map lạnh (tỉ lệ 1/50.000, 09 §mục 25). Tuyến mới thay toàn bộ con số đó bằng **5 nhiệm vụ**.

**Hệ quả dây chuyền — đây mới là phần nặng.** Theo 06 §9.3, shop Đồ Hủy Diệt của Bill **chỉ mở khi người chơi đang mặc đủ 5 món level 13 (Thần Linh) ở ô 0–4** và có ≥ 99 thức ăn 663–667. Còn thức ăn 663–667 chỉ rơi khi người giết **đang mặc đủ set Thần Linh** (09 §mục 26, tỉ lệ 3/333). Nghĩa là:

```
5 món Thần Linh  →  mở shop Bill  →  Đồ Hủy Diệt (650–662, 720M–2 tỷ vàng/món)
                 →  set Hủy Diệt đủ 5 món  →  Whis mở Chế tạo trang bị Thiên Sứ
                                           →  Whis mở Học tuyệt kỹ
```

Tuyến mới **phát miễn phí cái chìa đầu tiên của cả chuỗi endgame**, ở mốc SM 10–16 tỷ, trong khi bản thân đồ Thần Linh rơi ra luôn mang **option 21 "Yêu cầu sức mạnh 15–17 tỉ"** (06 §9.2) — tức người chơi ở NV 40 (mốc 10 tỷ SM) **có thể không mặc nổi món vừa nhận**.

Ngoài ra, **20c không nói món Thần Linh thưởng được sinh chỉ số bằng hàm nào**: `randDoTL` (random 100–115%, có option 206/207, có option 86/87 ký gửi, có option 21) hay `initChiSoItem` như Hộp thần linh 1775 (chỉ số cố định)? Chưa chốt thì không viết được SQL/Java.

> **Kết luận 4.3:** phát miễn phí quá tay; vừa mất giá trị của 5 nhóm boss, vừa mở sớm hai tầng trang bị phía sau. **Mức: Cao.**

### 4.4 Bông tai Porata

| Nhiệm vụ | Trao | Chi phí thật để tự làm (08 §8) |
|---|---|---|
| NV 34 | 454 cấp 1 ×1 | **1.000 ngọc xanh** ở shop URON |
| NV 37 | 921 cấp 2 ×1 | 454 + **9.999 Mảnh vỡ bông tai (933)** + 200.000.000 vàng + 1.000 ngọc, tỉ lệ **50%** |
| NV 39 | 1819 cấp 3 ×1 | 921 + **20.000 Mảnh vỡ bông tai cấp 3 (1820)** + 200.000.000 vàng + 1.000 ngọc, tỉ lệ **50%** |

Mảnh 933 rơi **10%** từ quái map 156–159 (09 §mục 2) → 9.999 mảnh ≈ **100.000 con quái**. Kỳ vọng số lần thử ở tỉ lệ 50% là 2 lần → **400.000.000 vàng + 2.000 ngọc** cho riêng bước lên cấp 2.

Nặng hơn: **item 1820 "Mảnh vỡ bông tai cấp 3" không rơi ở bất kỳ đâu trong source** — grep toàn bộ `SRC/src` chỉ thấy nó ở hằng `NangCapBongTai3.ITEM_ID_MANH_VO_BT3`. Nghĩa là **Bông tai cấp 3 hiện là vật phẩm bất khả đắc trên server**, và tuyến mới biến nó thành phần thưởng của một nhiệm vụ chính.

Lưu ý thêm: cả ba lần combine đều **xoá sạch option** khi lên cấp, rồi mới mở chỉ số bằng chức năng riêng (tốn thêm 200 Mảnh hồn + 1.000 ngọc + Đá xanh lam, tỉ lệ 45%/30%). Nên bản thưởng nhiệm vụ cần ghi rõ là **bản trơn (chỉ có option 72)** hay **bản đã mở chỉ số** — 20c không nói.

> **Kết luận 4.4:** 1 bông tai cấp 1 thì chấp nhận được; cấp 2 và cấp 3 là quá tay. **Mức: Cao.**

### 4.5 Đá nâng cấp 1074–1078 — nhầm hệ thống

20b ghi các viên 1074–1078 là **"nguyên liệu đập đồ"** và trao **461 viên** (nhánh A). Đối chiếu code:

- **Đập đồ +1 → +8** (`combine/NangCapVatPham.java`, 08 §7) nhận đá `type == 14` — tức **220 Đá lục bảo, 221 Saphia, 222 Ruby, 223 Titan, 224 Thạch anh tím**, và phải **đúng loại với trang bị**. `template.TYPE` của 1074–1078 là **27**, không phải 14 → **không đặt vào ô nâng cấp được.**
- 1074–1078 (`isDaNangCap1`) chỉ có **một** công dụng: nguyên liệu **Chế tạo trang bị Thiên Sứ** ở Whis, **1 viên mỗi lần chế tạo**, và cấp đá chỉ đổi tỉ lệ thành công 90,1% → 94,1% (08 §12.1). Nút thắt thật của chế tạo Thiên Sứ là **999 Mảnh Thiên Sứ**, không phải viên đá này.

Hệ quả:
1. **Mô tả nhiệm vụ sai** — đúng cái lỗi mà file 20 §2 hứa sẽ không lặp lại ("chữ mô tả trong DB không khớp thứ server thưởng").
2. **461 viên cho một hệ thống tiêu 1 viên/lần** = kho dùng 461 lần chế tạo, trong khi người chơi cả đời may ra chế tạo được vài món. Quy đổi giá shop Whis: 51×1 + 90×2 + 100×3 + 90×4 + 130×5 = **1.541 ngọc xanh**.
3. Người chơi cầm 461 viên vẫn **không đập được đồ +1**, vì thứ họ cần là 220–224.

Điểm sáng: **NV 17 bước 0 trao đúng thứ cần** — Đá Titan (223) ×10 + Đá Ruby (222) ×10. Liều lượng cũng chuẩn: bảng 08 §7 cho thấy +0→+1 tốn 3 viên, +1→+2 tốn 7 viên → **đúng 10 viên đưa một món áo lên +2**. Phần này nên giữ nguyên.

> **Kết luận 4.5:** sai hệ thống + thừa số lượng. **Mức: Cao** (vì phải sửa cả mô tả DB lẫn bảng thưởng ở 10 nhiệm vụ).

### 4.6 Ba vật phẩm chết

| id | Vấn đề đo được |
|---|---|
| **674 Đá ngũ sắc** | Grep toàn `SRC/src`: chỉ xuất hiện ở `ConstItem.DA_NGU_SAC = 674`, **không có chỗ nào dùng hằng đó**. `PhaLeHoaTrangBi.showInfoCombine` nhận **đúng 1 món trang bị**, chi phí là **vàng + ngọc theo số lỗ** (5M vàng + 1 ngọc cho lỗ đầu), **không đọc nguyên liệu nào**. Vậy nên đề xuất ở 20b §NV26 và §5.6 mục 6 — *"chống kẹt bằng cách nâng Đá ngũ sắc lên ×6"* — **không chống được kẹt gì cả**: người chơi hết vàng vẫn kẹt nguyên. |
| **574 Capsule Vàng** | Không có trong `item_shop`, không rơi ở đâu, và `services_func/UseItem.java` **không có nhánh `case 574`**. Dùng vào không ra gì. 20b §NV23 mô tả là "nguồn trang bị bộ" — sai. |
| **964 Sao pha lê đen cấp 2** | `type = 30`, `level = 8`, nhưng **không nằm trong dãy 1416–1422** mà `NangCapSaoPhaLe` sinh ra, **không rơi ở đâu**, và **không có option mặc định** nào được gán trong code. Ép sao lấy `itemOptions.get(0)` của viên đá (08 §4.1 bảng chỉ số) → viên 964 không option sẽ **cộng vào trang bị đúng con số 0**, hoặc ném lỗi tuỳ cách `startCombine` xử lý danh sách rỗng. |

> **Kết luận 4.6:** ba phần thưởng này **không có tác dụng gì với người chơi** nhưng vẫn chiếm chỗ của một phần thưởng thật, và hai trong ba chỗ còn được mô tả sai trong lời nhiệm vụ. **Mức: Trung bình** (không phá kinh tế, nhưng phá niềm tin vào bảng thưởng).

### 4.7 Đường cong nâng cấp +1 → +8

Bảng thật (08 §7):

| Cấp | Số đá (220–224) | Vàng | Tỉ lệ | Thất bại |
|---|---|---|---|---|
| +0→+1 | 3 | 10.000 | 80% | giữ |
| +1→+2 | 7 | 70.000 | 50% | giữ |
| +2→+3 | 11 | 300.000 | 20% | **rớt về +1** |
| +3→+4 | 17 | 1.500.000 | 10% | giữ |
| +4→+5 | 23 | 7.000.000 | 7% | **rớt về +3** |
| +5→+6 | 35 | 23.000.000 | 5% | giữ |
| +6→+7 | 50 | 100.000.000 | 1% | **rớt về +5** |
| +7→+8 | 70 | 250.000.000 | 0,3% | giữ |

**Tuyến mới không đụng vào đường cong này** — vì 20 viên 222/223 ở NV 17 chỉ đủ tới +2, và 461 viên 1074–1078 không dùng được cho hệ thống này. Ảnh hưởng thật: **Thấp**.

Điểm cần chú ý là **Đá bảo vệ (987)**: nhánh A phát 13 viên, nhánh B phát **31 viên**. Đá bảo vệ chặn rớt cấp ở các mốc 2/4/6. 31 viên là đủ để một người chơi thử ~31 lần ở mốc +4→+5 hoặc +6→+7 mà không sợ tụt. Giá gốc chỉ 1 điểm capsule bang, nhưng điểm capsule bang lại là tài nguyên bang hội có giới hạn. Ảnh hưởng: **Thấp–Trung bình**, và **lệch nhánh** (nhánh B gấp 2,4 lần nhánh A), trái với nguyên tắc "không nhánh nào mạnh hơn nhánh nào" ở file 20 §7.

### 4.8 Vật phẩm mở khoá tính năng sớm hơn dự kiến

| Vật phẩm | Nhiệm vụ | Mốc SM khi nhận | Điều kiện dùng thật | Kết quả |
|---|---|---|---|---|
| 457 Thỏi vàng | NV 13 | ~800.000 | `power_require = 1.500.000` | Nhận trước khi đủ sức mạnh yêu cầu của chính item |
| 611 Bản đồ kho báu ×2 | NV 21 | ~18.000.000 | Mở hang cần **SM ≥ 2 tỷ + có bang + bang chưa có hang** (14 §514) | Nằm trong túi ~20 nhiệm vụ mới dùng được; dễ bị bán nhầm |
| 401 / 402 (đệ tử) | NV 12 | ~800.000 | — | 200 ngọc quy đổi ngay chương 2 |
| 454 Bông tai Porata | NV 34 | ~2,5 tỷ | `power_require = 1.500.000` — OK | Hợp thể đệ tử mở sớm, chấp nhận được |
| Bộ Thần Linh | NV 40–44 | 10–16 tỷ | Option 21 của đồ rơi ra là **15–17 tỷ SM** | Có thể **không mặc nổi** món vừa được thưởng |
| 638 Bình chứa Commeson (bản không HSD) | NV 49 | ~2 tỷ | — | Buff **giảm 90% sát thương từ quái, 60 phút**, cất kho vĩnh viễn thay vì bản 30 ngày của Potage |
| 3 bộ Ngọc Rồng | NV 45–47/50 | 16–18 tỷ | Phải đứng ở map 0/7/14 | Rồng Thần là tính năng đầu game, nay thành phần thưởng cuối game — nghịch chiều |

### 4.9 Bảng xếp hạng mức độ ảnh hưởng

| # | Vật phẩm / chỗ | Nhiệm vụ | Mức | Con số biện minh |
|---|---|---|---|---|
| 1 | **3 bộ Ngọc Rồng 14–20** | NV 45, 46, 47/50 | **Cao** | 3 lần gọi Rồng Thần → **30.000 ngọc xanh** (= 30 Bông tai 454 ở shop URON) |
| 2 | **Trọn bộ 5 món Thần Linh** | NV 40–44 | **Cao** | Thay thế ~1.500 lượt giết boss 5%; **405.000.000 vàng** nếu bán; mở khoá shop Bill → Hủy Diệt → Thiên Sứ |
| 3 | **4 Thỏi vàng (457)** | NV 13, 14 | **Cao** | **148.000.000 vàng** (tối đa 2 tỷ nếu bán qua gói thô); là vật phẩm của **gói VIP nạp tiền thật** |
| 4 | **Bông tai Porata 921 + 1819** | NV 37, 39 | **Cao** | Bỏ qua 9.999 + 20.000 mảnh, ~400M–800M vàng, 2.000–4.000 ngọc; **1820 không có nguồn** → 1819 hiện bất khả đắc |
| 5 | **461 viên Đá nâng cấp 1074–1078** | NV 15–35 (10 nhiệm vụ) | **Cao** | Sai hệ thống (đập đồ dùng 220–224); **1.541 ngọc** giá shop Whis; mô tả DB sai |
| 6 | **Đá ngũ sắc ×6 làm "chống kẹt" NV 26** | NV 26 | **Cao** | Pha lê hoá **không ăn nguyên liệu**; chi phí thật 5.000.000 vàng + 1 ngọc/lần ở tỉ lệ **50%** → bước 1 vẫn kẹt cứng với người hết vàng |
| 7 | 31 Đá bảo vệ ở nhánh B (vs 13 ở nhánh A) | NV 17, 29, 33, 48, 49 | Trung bình | Lệch **2,4 lần** giữa hai nhánh, trái nguyên tắc file 20 §7 |
| 8 | 964 Sao pha lê đen cấp 2 | NV 38 | Trung bình | Không nguồn, không option gốc → ép sao cộng 0 hoặc lỗi |
| 9 | 574 Capsule Vàng ×5 | NV 23 | Trung bình | Không có `case 574` trong `UseItem` → item chết, mô tả "nguồn trang bị bộ" sai |
| 10 | 638 Bình chứa Commeson bản không hạn dùng | NV 49 | Trung bình | Buff 60 phút giảm 90% sát thương quái, cất vĩnh viễn; bản gốc chỉ 1 lần/ngày + HSD 30 ngày |
| 11 | 401 + 402 | NV 12 | Trung bình | 200 ngọc quy đổi ở mốc SM 800.000 |
| 12 | 611 Bản đồ kho báu ×2 | NV 21 | Trung bình | Dùng được ở SM 2 tỷ, trao ở SM 18 triệu |
| 13 | Bùa option 106 chưa chọn id | NV 34 bước 0 | Trung bình | Thiếu item → NV 34 giảm 50% HP/sức đánh ở map 105–110 |
| 14 | Chỉ số đồ Thần Linh chưa chốt cách sinh | NV 40–44 | Trung bình | `randDoTL` (có option 21 = 15–17 tỷ) hay `initChiSoItem`? Chưa quyết thì không viết được code |
| 15 | 75 Gói 10 viên Capsule (193) | NV 7 | Thấp | 225 ngọc quy đổi theo `gem = 3`; giữ truyền thống `TASK_7_2` cũ |
| 16 | 180 Đậu thần cấp 8 (352) | NV 32, 33, 36, 39 | Thấp | Gói 596 bán **10 ngọc / 30 hạt** ở URON → tổng chỉ ≈ 60 ngọc |
| 17 | Sao pha lê 445/446/447 ×2–7 | NV 26, 30, 31, 49 | Thấp | Rơi 10% mọi map, ~1,43%/màu → vài chục phút cày |
| 18 | 20 Đá Titan/Ruby (222/223) | NV 17 bước 0 | Thấp | Đúng liều dạy +0→+2 (3+7 viên); **giữ nguyên** |
| 19 | Trang bị cấp 1, Rada 12/57/58, đậu thần cấp 1, gói đậu cấp 3, sách lv1 | NV 0–11 | Thấp | Tất cả bán ở shop 3 hành tinh / URON giá 300–12.000 vàng hoặc 5–10 ngọc |
| 20 | 1795 Bình hút năng lượng ×1 | NV 36 bước 0 | Thấp | Rơi 20% từ Hirudegarn; là item mở đường vòng bắt buộc |

---

## 5. Xung đột / đề xuất sửa

Ưu tiên thay bằng **vật phẩm không bán lại được** (`gold = 0`) hoặc **vật phẩm nhiệm vụ riêng** (id ≥ 2000, gắn option 93 nếu muốn chặn bán tuyệt đối).

| # | Nhiệm vụ | Đang trao | Vấn đề | **Nên đổi thành** | Vì sao |
|---|---|---|---|---|---|
| 1 | **NV 45** (20c L677) | Bộ 7 Ngọc Rồng 14–20 mỗi loại ×1 | = 1 lần gọi Rồng Thần → 10.000 ngọc | **Bỏ hẳn Ngọc Rồng.** Thay bằng **1× ITEM MỚI "Ấn Ký Ức Hợp Nhất"** (TYPE 11, `gold = 0`, không giao dịch) + **5× Đá bảo vệ (987)** | Giữ đúng cam kết không thưởng ngọc; NV 45 vốn đã có phần thưởng cốt truyện là Lõi Hư Không (2101) |
| 2 | **NV 46** (20c L717) | 3× Ngọc Rồng 7 sao (20) + 1× Đá ngũ sắc (674) | NR 7 sao vừa là nguyên liệu gọi rồng vừa là nguyên liệu ép sao; 674 là item chết | **3× Sao pha lê lục (447)** + **1× Đá bảo vệ (987)** | Cùng "cảm giác" phần thưởng (đá quý), `gold = 0`, không mở được điều ước |
| 3 | **NV 47 / NV 50** (20c L757, L803) | 2 bộ Ngọc Rồng (14–20 mỗi loại ×2) | = 2 lần gọi rồng → 20.000 ngọc | **Bỏ Ngọc Rồng.** Danh hiệu 2120/2121 + **1× ITEM MỚI "Vỏ Lõi rỗng" (2102)** đã đủ sức nặng kết tuyến; nếu muốn thêm vật chất thì **10× Đá bảo vệ (987)** | Phần thưởng kết tuyến nên là **danh hiệu + câu chuyện**, không phải tiền tệ trá hình |
| 4 | **NV 40, 41, 42, 43, 44** (20c L476, 513, 552, 592, 633) | Áo / Quần / Găng / Giầy / Nhẫn Thần Linh | Bán lại được **405.000.000 vàng**; mở khoá chuỗi Bill → Hủy Diệt → Thiên Sứ; option 21 có thể cao hơn SM người chơi | **Hai phương án, chủ dự án chọn:**<br>**(a) Giữ bộ Thần Linh nhưng khoá cứng:** sinh bằng `initChiSoItem` (chỉ số cố định, như Hộp thần linh 1775), **bỏ option 21**, **thêm option 93 "Hạn sử dụng 30 ngày"** → không bán được, không ký gửi được, và người chơi vẫn phải tự săn bộ thật để giữ lâu dài.<br>**(b) Đổi hẳn sang 5 "Mảnh Thần Linh" (ITEM MỚI, `gold = 0`)**, gom đủ 5 mảnh đến Bà Hạt Mít đổi lấy **1 món** Thần Linh tự chọn | (a) giữ nguyên nhịp truyện "mỗi chương một món thần"; (b) giữ nguyên giá trị của 5 nhóm boss đang rơi đồ TL — **21c nên quyết cùng** |
| 5 | **NV 13** (20a L502) | 1× Thỏi vàng (457) | `gold = 500.000.000`; bán 37 triệu; là vật phẩm gói VIP tiền thật | **1× 295 Gói 30 đậu thần cấp 3 (đã có) + 2× Đá bảo vệ (987)** | `gold = 0` cả hai; hợp cảnh "vào bang hội" (Đá bảo vệ vốn là hàng shop bang) |
| 6 | **NV 14** (20a L533) | 3× Thỏi vàng (457) | 111 triệu vàng; và lời thoại NV 14 nói "ký ức đổi được **ba thỏi vàng**" → thỏi vàng là **đạo cụ cốt truyện**, không nên là phần thưởng | **3× ITEM MỚI "Phiếu ký ức Uron"** (TYPE 11, `gold = 0`, không giao dịch), đúng vật chứng chợ đen; phần thưởng vật chất giữ ở **1× 2004 Hộp Ký Ức Bị Đánh Cắp** đang có | Giữ trọn ý đồ kịch bản mà không phát tiền |
| 7 | **NV 37** (20c L352) | 1× Bông tai Porata cấp 2 (921) | Bỏ qua 9.999 mảnh + 400M vàng + 2.000 ngọc | **200× Mảnh vỡ bông tai (933)** *(hoặc 2.000 nếu muốn hào phóng)* + **1× Đá xanh lam (935)** | Đưa người chơi **vào** hệ thống bông tai thay vì nhảy qua nó; 933 `gold = 0`, không bán được ra tiền |
| 8 | **NV 39** (20c L432) | 1× Bông tai Porata cấp 3 (1819) | Cấp 3 hiện **bất khả đắc** (1820 không rơi ở đâu) | **500× Mảnh vỡ bông tai cấp 3 (1820)** — và nhân dịp này **mở nguồn rơi cho 1820** ở boss chương 5 (Baby / Black Goku), việc mà 21c đang làm | Biến một item chết thành nội dung thật, không tặng thẳng kết quả |
| 9 | **NV 15, 16, 17, 18, 19, 20, 21, 22, 23, 24, 25, 27, 28, 29, 30, 31, 35** | Đá nâng cấp 1074–1078, tổng 461 viên, mô tả "nguyên liệu đập đồ" | **Sai hệ thống** — đập đồ dùng 220–224 (TYPE 14) | **Đổi toàn bộ sang đá nâng cấp thật 220–224**, số lượng bám bảng 08 §7:<br>NV 16–19: ×10 mỗi nhiệm vụ (đủ +0→+2 cho 1 món)<br>NV 21–25: ×15<br>NV 27–31: ×20<br>NV 35: ×20<br>**Giữ lại 1074–1078 với số lượng nhỏ (1–3 viên) chỉ ở NV 44** (nhiệm vụ gặp Whis), đúng nơi chúng được dùng | Mô tả khớp thực tế; người chơi thật sự đập được đồ; tổng chỉ ~150 viên thay vì 461, và 220–224 vốn rơi 20% ở map lạnh nên không phá giá |
| 10 | **NV 26 bước 0** (20b L477) | Đá ngũ sắc (674) ×6 + Sao pha lê lục (447) ×2, gọi là "nguyên liệu pha lê hoá" | Pha lê hoá **không nhận nguyên liệu**; chi phí là 5.000.000 vàng + 1 ngọc/lần, tỉ lệ thật 50% | **Bỏ Đá ngũ sắc.** Thay bằng: (a) **giữ Sao pha lê lục ×2** (để có cái mà ép ở bước 2); (b) áp dụng đúng đề xuất (b) của chính 20b — **bước 1 tính hoàn thành ngay nếu trang bị đã có option 107 ≥ 1**; (c) nếu vẫn muốn chống kẹt tuyệt đối thì cho Bà Hạt Mít **miễn phí 1 lượt pha lê hoá 100%** khi `getIdTask == TASK_26_1` (sửa `PhaLeHoaTrangBi`, không phát vàng) | Cách duy nhất chống kẹt mà **không** phát vàng/ngọc là sửa cơ chế, không phải phát thêm item |
| 11 | **NV 26 hoàn thành** (20b L490) | Sao pha lê lục ×2 + Đá ngũ sắc ×3 | 674 là item chết | **Sao pha lê lục (447) ×2 + Hematite (1423) ×1** | Hematite là nguyên liệu thật của "Nâng cấp Sao pha lê cấp 1 → cấp 2" (08 §4.4), nối tiếp đúng bài học của NV 26 |
| 12 | **NV 23** (20b L394) | Capsule Vàng (574) ×5, ghi "nguồn trang bị bộ" | `UseItem` không có `case 574` → dùng không ra gì | **Capsule kì bí (380) ×5** *(có nhánh xử lý, xuất hiện trong Rương Gỗ và Hộp quà Noel)* hoặc **Rương ngọc rồng (1560) ×1** | Người chơi mở ra thật sự nhận được thứ gì đó |
| 13 | **NV 38** (20c L391) | Sao pha lê đen cấp 2 (964) ×1 | Không nguồn, không option gốc → ép sao cộng 0 | **1× Sao pha lê lục cấp 2 (1422)** *(dãy hợp lệ của `NangCapSaoPhaLe`)*, hoặc giữ 964 **kèm yêu cầu 21b đặc tả option gốc cho nó** | Boss Black Goku xứng đáng phần thưởng "đen tối", nhưng phải là viên đá hoạt động được |
| 14 | **NV 48** (20b L287) vs **NV 20** (20b L252) | Nhánh B: 1074 ×20 + **987 ×8**; nhánh A: 1075 ×40 | Đá bảo vệ nhánh B nhiều gấp bội; sau khi áp dụng #9 thì hai nhánh phải tính lại từ đầu | **Nhánh A (Granola): 220–224 ×15** *(nguyên liệu thô)*; **Nhánh B (Jaco): 987 Đá bảo vệ ×4 + 220–224 ×5** *(quân trang cấp phát)* | Giữ đúng "khác loại, ngang giá trị" của file 20 §7, với con số kiểm được |
| 15 | **NV 49 bước 1** (20b L642) | Bình chứa Commeson (638) bản **không hạn sử dụng** | Buff giảm 90% sát thương quái 60 phút, cất kho vĩnh viễn; nguồn gốc là phần thưởng 1 lần/ngày có HSD 30 ngày | Giữ item nhưng **gắn option 93 "Hạn sử dụng 7 ngày"** thay vì bỏ hạn | Đủ dài để không mất giữa chừng nhiệm vụ, không thành vật phẩm vĩnh viễn |
| 16 | **NV 33** (20c L188) | 5× Đá bảo vệ (987) + 50× Đậu thần cấp 8 | Ổn, nhưng NV 33 là nhiệm vụ **mở giới hạn sức mạnh** — phần thưởng không liên quan nội dung | **5× Đá bảo vệ** giữ nguyên; đổi 50 đậu thần thành **1× ITEM MỚI "Giấy chứng phá giới"** (TYPE 11, kỷ vật) | Nhiệm vụ mốc nên để lại kỷ vật, không phải hàng tiêu hao |
| 17 | **NV 21** (20b L330) | Bản đồ kho báu (611) ×2 | Dùng được ở SM ≥ 2 tỷ, trao ở SM 18 triệu | Dời **611 ×2 xuống NV 35** (nhiệm vụ phó bản bang, SM ~4 tỷ); NV 21 thay bằng **220–224 ×15** | Người chơi nhận là dùng được ngay; tránh bán nhầm |
| 18 | **NV 12** (20a L462) | 401 Đổi đệ tử + 402 Nâng kỹ năng 1 đệ tử | 200 ngọc quy đổi ở SM 800.000 | Giữ **401 ×1** (thật sự cần, vì đệ tử ra chỉ số xấu là kẹt), **bỏ 402** | 402 là hàng nâng cấp, không phải hàng chống kẹt |
| 19 | **NV 34 bước 0** (20c L239) | "nên trao 1 bùa/cải trang có option 106" — **chưa có id** | Không chốt thì NV 34 giảm 50% HP/sức đánh | **Nón Noel Xanh theo hành tinh (665 TĐ / 666 NM / 667 XD)** — đã có sẵn option 106 + option 93 HSD 30 ngày (07 §item_shop_option) | Item có sẵn, đã mang option 93 nên **không bán lại được** — đúng loại phần thưởng ta muốn |
| 20 | **NV 7** (20a L307) | 75× Gói 10 viên Capsule (193) | 225 ngọc quy đổi | Hạ xuống **20×**, bù bằng **1× 2010 Mảnh Ký Ức #1** đang có | 20 gói = 200 lượt bay, quá đủ cho chương 1–2; tuyến cũ phát 75 là do không có phần thưởng nào khác |

---

## 6. Vật phẩm chưa có trong DB (id ≥ 2000)

> Bảng này **chỉ liệt kê**. **Đặc tả chi tiết (TYPE, gender, level, icon, option, quy tắc rơi, phương án thay bằng item sẵn có) xem [21b-item-moi-dac-ta.md](21b-item-moi-dac-ta.md).**

| id | Tên | Dùng ở nhiệm vụ | File nguồn |
|---|---|---|---|
| 2001 | Mảnh Vỡ Hư Không | NV 2 bước 2 | 20a |
| 2002 | Kỷ Vật Của Ông | NV 5 bước 0–2 | 20a |
| 2003 | Máy Dò Ký Ức | Thưởng NV 8 | 20a |
| 2004 | Hộp Ký Ức Bị Đánh Cắp | Thưởng NV 14 | 20a |
| 2006 | Hạt Giống Hy Vọng | NV 11 bước 0 (trao) → bước 1 (dùng) | 20a |
| 2010 | Mảnh Ký Ức #1 | Thưởng NV 7; dùng lại ở NV 40 bước 3 | 20a / 20c |
| 2011 | Mảnh Ký Ức #2 | Thưởng NV 15 | 20a |
| 2012 | Mảnh Ký Ức #3 | Thưởng NV 23 | 20b |
| 2013 | Mảnh Ký Ức #4 | Thưởng NV 31 / NV 49 | 20b |
| 2014 | Mảnh Ký Ức #5 | NV 39 bước 3 | 20c |
| 2015 | Mảnh Ký Ức #6 | NV 39 bước 5 | 20c |
| 2016 | Mảnh Ký Ức #7 | NV 45 bước 1 | 20c |
| 2040 | Vỏ đạn khắc dấu | NV 16 bước 3 | 20b |
| 2041 | Búa rèn cũ | NV 17 bước 0 → 2 | 20b |
| 2042 | Thẻ tiền thưởng Granola | NV 20 bước 4 (nhánh A) | 20b |
| 2043 | Biên bản truy nã Ngân Hà | NV 48 bước 4 (nhánh B) | 20b |
| 2044 | Máy đo ký ức | NV 22 bước 3 | 20b |
| 2070 | Lõi năng lượng Android | NV 25 bước 2 | 20b |
| 2071 | Mẫu kim loại có ký ức | NV 26 bước 0 → 3 | 20b |
| 2072 | Mảnh giáp khắc tên | NV 28 bước 3 | 20b |
| 2073 | Thẻ từ phòng thí nghiệm | NV 29 bước 0 → 1 | 20b |
| 2074 | Bản thiết kế bản sao | NV 29 bước 2 | 20b |
| 2100 | Lõi Ký Ức chưa hoàn chỉnh | NV 45 bước 3 | 20c |
| 2101 | Lõi Hư Không | NV 45 → NV 47 / NV 50 | 20c |
| 2102 | Vỏ Lõi rỗng | NV 47 bước 1 (nhánh A) | 20c |
| 2103 | Mảnh Ký Ức Vỡ | NV 32 bước 4–5 | 20c |
| 2104 | Mảnh Ký Ức Đóng Băng | NV 34 bước 3–5 | 20c |
| 2105 | Mảnh Bùa Babiđây | NV 36 bước 3 (đường vòng) | 20c |
| 2106 | Lõi Phép Babiđây | NV 37 bước 3–4 | 20c |
| 2107 | Ống nghiệm Myuu | NV 46 bước 4 | 20c |
| 2120 | Người Trả Ký Ức *(danh hiệu)* | NV 47 | 20c |
| 2121 | Kẻ Giữ Hư Không *(danh hiệu)* | NV 50 | 20c |

**Cộng: 32 item mới.**

**Hai mâu thuẫn về id mà 21b cần xử lý** (ghi ở đây vì phát hiện trong lúc kiểm toán, không sửa file 20\*):
1. Bảng chốt ở [20 §9.2b](../2-thiet-ke-nhiem-vu-moi/20-thiet-ke-nhiem-vu-moi.md) quy định **Mảnh Ký Ức #1–#7 = 2010–2016**, và 20a dùng đúng vậy. Nhưng **20c §6 lại ghi "2000–2003 = Mảnh Ký Ức #1…#4"**, và **20c §NV40 bước 3 gọi `ITEM MỚI 2000 "Mảnh Ký Ức #1"`**. Phải thống nhất về **2010**.
2. 20a §B đặt tiêu đề "Vật phẩm mới (**6 item**, id ≥ 2000)" nhưng bảng bên dưới liệt kê **7 dòng** (2001, 2002, 2003, 2004, 2006, 2010, 2011). Id **2005** và **2007–2009** bị bỏ trống không lý do.

---

## 7. Nguyên tắc phát thưởng đề xuất

Rút ra từ toàn bộ kiểm toán trên. Sáu nguyên tắc, mỗi nguyên tắc kèm cách kiểm tra được bằng số.

### 7.1 Mỗi loại vật phẩm chỉ nên có một nguồn "chính"

| Loại vật phẩm | Nguồn chính nên giữ | Nhiệm vụ chính được phép làm gì |
|---|---|---|
| **Tiền tệ (vàng, ngọc, hồng ngọc)** | Cày quái, bán đồ, phó bản, nạp | **Không gì cả** — kể cả gián tiếp qua Thỏi vàng, đồ có `gold` cao, hay Ngọc Rồng |
| **Ngọc Rồng 14–20** | Rơi 1% mọi map + 10% từ boss Mẫu B | **Không trao** — vì là chìa của Rồng Thần (10.000 ngọc/lần ước) |
| **Đồ Thần Linh 555–567** | Boss: Cooler / Baby 10%, Siêu Bọ Hung / Black Goku / Cumber 5%, Mabư 12h 1% | Trao **tối đa 1 món**, bản `initChiSoItem` + option 93, như "đồ mượn" của cốt truyện |
| **Đồ Hủy Diệt / Thiên Sứ** | Shop Bill (vàng) / Chế tạo Whis (999 mảnh) | **Không đụng tới** |
| **Bông tai Porata 454/921/1819** | Shop URON (cấp 1) + combine mảnh (cấp 2, 3) | Trao **mảnh** (933 / 1820), không trao **bông tai thành phẩm** từ cấp 2 trở lên |
| **Đá nâng cấp đập đồ 220–224** | Rơi 20% map lạnh, Rương Gỗ, giải ĐHVT | Trao được, liều lượng bám bảng 08 §7 (10 viên = +0→+2) |
| **Đá nâng cấp Thiên Sứ 1074–1078** | Shop Whis 1–5 ngọc | Trao 1–3 viên, **chỉ ở nhiệm vụ có Whis** |
| **Đá bảo vệ 987** | Shop bang hội, 1 điểm capsule bang | Trao được, **≤ 15 viên toàn tuyến**, và **bằng nhau giữa hai nhánh** |
| **Sao pha lê 441–447** | Rơi 10% mọi map | Trao được thoải mái (≤ 10 viên) |
| **Đậu thần, capsule, rada, trang bị cấp 1, sách lv1** | Shop 3 hành tinh / URON | Trao thoải mái — đều là hàng shop giá vài trăm vàng |
| **Vật phẩm cốt truyện** | **Chỉ nhiệm vụ** | Toàn quyền — đây là chỗ nhiệm vụ nên đầu tư |

### 7.2 Quy tắc "không đổi ra tiền"

Trước khi đưa một item vào bảng thưởng, kiểm hai cột trong `item_template`:

```
nếu gold > 0            → bán được gold/4  → CHỈ dùng khi đã gắn option 93
nếu id == 457           → bán được 37.000.000 → CẤM
nếu type == 12 (Ngọc Rồng) → gọi rồng được   → CẤM
còn lại (gold = 0)      → bán được 1 vàng/cái → AN TOÀN
```

Nhắc lại: **option 30 "Không thể giao dịch" và option 154 "Không thể bán lại" không chặn bán cho shop** — chỉ option 93 chặn.

### 7.3 Quy tắc "phần thưởng phải dùng được ngay"

Đối chiếu `power_require` của item với mốc SM ở [20 §6.1](../2-thiet-ke-nhiem-vu-moi/20-thiet-ke-nhiem-vu-moi.md), và đối chiếu điều kiện mở tính năng:

| Kiểm tra | Chỗ đang sai |
|---|---|
| `item.power_require ≤ SM mục tiêu của nhiệm vụ` | 457 ở NV 13 (1.500.000 vs 800.000) |
| Tính năng dùng item đã mở chưa | 611 ở NV 21 (cần SM 2 tỷ, trao ở 18 triệu) |
| Option 21 của đồ TL ≤ SM người chơi | Bộ Thần Linh NV 40–44 (15–17 tỷ vs 10–16 tỷ) |

### 7.4 Quy tắc "nhiệm vụ dạy tính năng phát đúng nguyên liệu của tính năng đó"

Mẫu đúng: **NV 17** phát Đá Titan 223 + Đá Ruby 222 — chính xác thứ `NangCapVatPham` đòi.
Mẫu sai: **NV 26** phát Đá ngũ sắc 674 cho pha lê hoá (không ăn nguyên liệu); **NV 16–35** phát 1074–1078 cho đập đồ (sai TYPE).

Cách kiểm: mở file combine tương ứng, đọc hàm `showInfoCombine`, liệt kê đúng các `if` kiểm tra item.

### 7.5 Quy tắc "hai nhánh ngang giá trị, khác loại"

Bảng đối chiếu bắt buộc cho mọi điểm rẽ (file 20 §7):

| Điểm rẽ | Nhánh A | Nhánh B | Cân chưa? |
|---|---|---|---|
| NV 20 / 48 | 1075 ×40 | 1074 ×20 + 987 ×8 | **Chưa** — đá bảo vệ khó kiếm hơn nhiều |
| NV 31 / 49 | 1078 ×50 + 445 ×2 | 447 ×3 + 674 ×5 + 987 ×10 + 638 ×1 | **Chưa** — nhánh B hơn hẳn (638 + 10 đá bảo vệ) |
| NV 47 / 50 | 2 bộ NR + danh hiệu 2120 | 2 bộ NR + danh hiệu 2121 | Cân, nhưng cả hai đều sai (§5 #3) |

Cách kiểm: quy mọi phần thưởng về **giá shop hiện hành** (ngọc xanh), chênh lệch không quá 10%.

### 7.6 Quy tắc "phần thưởng cuối chương là danh hiệu, không phải tài sản"

NV 7, 15, 23, 31, 39, 47 là mốc cảm xúc. Thứ nên trao là **Mảnh Ký Ức / danh hiệu / kỷ vật** — vật phẩm `gold = 0`, không giao dịch, không bán được. Tài sản (đá, sao, mảnh bông tai) nên rải đều ở các nhiệm vụ giữa chương, nơi người chơi cần chúng để đi tiếp.

---

## 8. Ghi chú / điểm cần chủ dự án quyết

| # | Câu hỏi | Vì sao phải quyết trước khi viết SQL |
|---|---|---|
| 1 | **Bỏ hẳn Ngọc Rồng khỏi bảng thưởng?** (§5 #1–#3) | Đây là quyết định lớn nhất của tài liệu này. Giữ 3 bộ = chấp nhận phát **30.000 ngọc**. Nếu muốn giữ vì lý do cốt truyện ("7 Mảnh Ký Ức hoá thành 7 Ngọc Rồng"), phương án trung dung là trao **6 loại (14–19), cố ý thiếu Ngọc Rồng 7 sao** → không gọi rồng được, vẫn giữ trọn hình ảnh |
| 2 | **Bộ Thần Linh: phương án (a) bản HSD 30 ngày hay (b) 5 mảnh đổi 1 món?** (§5 #4) | Ảnh hưởng trực tiếp tới 21c (rơi đồ boss) và tới việc shop Bill có nên tiếp tục khoá bằng set Thần Linh không |
| 3 | **Thỏi vàng: bỏ hẳn hay giữ làm đạo cụ cốt truyện không nhận vào túi?** (§5 #5, #6) | Thoại NV 14 đã viết "ba thỏi vàng"; nếu bỏ item thì thoại vẫn giữ được, chỉ cần vật chứng là item mới |
| 4 | **Có chấp nhận sửa `PhaLeHoaTrangBi` để cho 1 lượt miễn phí 100% khi đang ở NV 26?** (§5 #10) | Đây là cách **duy nhất** chống kẹt NV 26 mà không phát vàng/ngọc. Không sửa thì phải chấp nhận NV 26 có thể kẹt cứng với người chơi hết vàng |
| 5 | **Mở nguồn rơi cho item 1820 (Mảnh vỡ bông tai cấp 3)?** (§5 #8) | Nếu không, Bông tai cấp 3 vẫn là item chết và NV 39 không có phần thưởng thay thế hợp lý. 21c là chỗ hợp lý để đặt nguồn rơi này |
| 6 | **Đá nâng cấp: đổi toàn bộ 461 viên 1074–1078 sang 220–224?** (§5 #9) | Đụng bảng thưởng của **17 nhiệm vụ** và mô tả `detail` của từng nhiệm vụ. Càng quyết sớm càng ít phải sửa |
| 7 | **Chỉ số đồ Thần Linh thưởng sinh bằng hàm nào?** (`randDoTL` hay `initChiSoItem`) | Không quyết thì không viết được `rewardDoneTask`. Liên quan trực tiếp tới option 21 (yêu cầu 15–17 tỷ SM) — người chơi NV 40 chỉ có 10 tỷ |
| 8 | **Bông tai thưởng là bản trơn hay bản đã mở chỉ số?** | Combine lên cấp **xoá sạch option**; nếu trao bản trơn thì người chơi vẫn phải tốn 1.000 ngọc + 200 mảnh hồn + Đá xanh lam để mở chỉ số |
| 9 | **Ba item chết (674, 574, 964): xoá khỏi bảng thưởng hay bổ sung cơ chế cho chúng?** (§4.6) | Nếu muốn giữ, 21b phải đặc tả option gốc cho 964 và nhánh `UseItem` cho 574 — tức thêm việc code ngoài phạm vi tuyến nhiệm vụ |
| 10 | **Đá bảo vệ: trần bao nhiêu viên toàn tuyến?** | Kiểm toán đề xuất ≤ 15 và bằng nhau hai nhánh; hiện là 13 (A) vs 31 (B) |
| 11 | **Item nào cho option 106 ở NV 34?** (§5 #19) | 20c mới ghi "nên trao", chưa có id. Đề xuất Nón Noel Xanh 665/666/667 vì đã có sẵn option 106 + 93 |
| 12 | **Thống nhất id Mảnh Ký Ức về 2010–2016** (§6) | 20c §6 và 20c §NV40 đang dùng 2000–2003, lệch với bảng chốt ở 20 §9.2b và với 20a |
| 13 | **Bình chứa Commeson (638) ở NV 49: giữ không hạn sử dụng hay đổi 7 ngày?** (§5 #15) | Bản không hạn dùng là buff vĩnh viễn giảm 90% sát thương quái — mạnh hơn cả bản gốc của Potage |
| 14 | **Có cần một bảng `task_main_reward` kiểm được không?** | Đề xuất: thêm cột `gold_value` (vàng bán lại quy đổi) và `gem_value` (ngọc shop quy đổi) vào bảng, để mỗi lần sửa thưởng là chạy được một câu SQL kiểm tổng — đúng tinh thần "không bao giờ lặp lại chuyện chữ trong DB khác thứ server thưởng" của file 20 §9.2 |

---

*Tài liệu kiểm toán — soạn từ `database team2026.sql`, source `SRC/src/nro/**` và bộ tài liệu 02b/06/07/08/09/10/11/11b/14/16/17/19. Mọi con số trong tài liệu đều truy được về file + bảng/hàm cụ thể đã dẫn tại chỗ.*
