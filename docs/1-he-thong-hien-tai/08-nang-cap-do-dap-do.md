# 08 — Nâng cấp đồ / Đập đồ / Kết hợp vật phẩm (package `combine`)

> Tài liệu spec chi tiết được đối chiếu trực tiếp từ source `SRC/src/nro/models/combine/**` (32 file), các NPC gọi vào (`npc_list/BaHatMit.java`, `npc_list/Whis.java`) và dump DB `database team2026.sql` (bảng `item_template`, `item_option_template`, `map_template`).
> Mọi tỉ lệ, giá, nguyên liệu đều lấy từ code. Chỗ nào code khác với lời thoại NPC, tài liệu ghi **theo code** và nêu rõ khác biệt.

## Mục lục

1. [Tổng quan kiến trúc](#1-tổng-quan-kiến-trúc)
2. [Cách tính tỉ lệ (`Util.isTrue`)](#2-cách-tính-tỉ-lệ-utilistrue)
3. [NPC & menu mở từng chức năng](#3-npc--menu-mở-từng-chức-năng)
4. [Nhóm Sao pha lê (Bà Hạt Mít – Đảo Kamê)](#4-nhóm-sao-pha-lê-bà-hạt-mít--đảo-kamê)
   - 4.1 Ép sao trang bị
   - 4.2 Pha lê hóa trang bị (đục lỗ)
   - 4.3 Tạo đá Hematite
   - 4.4 Nâng cấp Sao pha lê (cấp 1 → cấp 2)
   - 4.5 Đánh bóng Sao pha lê (cấp 2 → lấp lánh)
   - 4.6 Cường hóa lỗ sao pha lê (ô thứ 8, 9)
   - 4.7 Tạo Dùi đục / Tạo Đá mài (không có menu)
5. [Chuyển hóa trang bị (Vàng / Ngọc)](#5-chuyển-hóa-trang-bị-vàng--ngọc)
6. [Đồ kích hoạt: Phân rã & Tái tạo Capsule](#6-đồ-kích-hoạt-phân-rã--tái-tạo-capsule)
7. [Nâng cấp vật phẩm +1 → +8 (đá nâng cấp, đá bảo vệ)](#7-nâng-cấp-vật-phẩm-1--8)
8. [Bông tai Porata (cấp 2, cấp 3, mở chỉ số)](#8-bông-tai-porata)
9. [Nhập Ngọc Rồng](#9-nhập-ngọc-rồng)
10. [Làm phép nhập đá](#10-làm-phép-nhập-đá)
11. [Sách Tuyệt Kỹ (7 chức năng)](#11-sách-tuyệt-kỹ)
12. [Whis: Chế tạo trang bị Thiên Sứ & Học tuyệt kỹ](#12-whis-chế-tạo-trang-bị-thiên-sứ--học-tuyệt-kỹ)
13. [Các nâng cấp KHÔNG có trong code](#13-các-nâng-cấp-không-có-trong-code)
14. [Phụ lục: bảng tra item / option dùng trong combine](#14-phụ-lục-bảng-tra-item--option)
15. [Ghi chú / điểm cần lưu ý](#15-ghi-chú--điểm-cần-lưu-ý)

---

## 1. Tổng quan kiến trúc

| Thành phần | File | Vai trò |
|---|---|---|
| `CombineService` | `combine/CombineService.java` | Singleton. Khai báo hằng số loại combine, điều hướng `showInfoCombine()` (khi người chơi đặt đồ vào ô) và `startCombine()` (khi bấm nút xác nhận), `startCombineVip(n)` (pha lê hóa x10/x100), gửi hiệu ứng (msg `-81`). Giữ tham chiếu NPC `baHatMit` và `whis`. |
| `Combine` | `combine/Combine.java` | Trạng thái combine của 1 người chơi (`player.combineNew`): `itemsCombine`, `typeCombine`, `goldCombine`, `gemCombine`, `ratioCombine`, `countDaNangCap`, `countDaBaoVe`. `clearParamCombine()` reset về 0 sau mỗi lần `startCombine`. |
| `CombineSystem` | `combine/CombineSystem.java` | Bảng giá/tỉ lệ dùng chung: ép sao, pha lê hóa, nâng cấp đồ, mapping đá pha lê → option. |
| `RandomCollection` | `combine/RandomCollection.java` | Tiện ích random theo trọng số (không được dùng trong các combine hiện tại). |
| Từng chức năng | các file còn lại | Mỗi file có `showInfoCombine(player)` (tính giá, hiện menu) và hàm thực hiện. |

**Luồng chung**

1. Người chơi chọn menu NPC → `CombineService.openTabCombine(player, type)` gửi msg `-81` mở tab kết hợp.
2. Client gửi danh sách index đồ trong túi (msg `-81`) → `Controller` (`server/Controller.java` ~dòng 404–412) gọi `CombineService.showInfoCombine(player, index[])` → gọi `XXX.showInfoCombine(player)` theo `typeCombine`.
3. NPC hiện menu `ConstNpc.MENU_START_COMBINE` → người chơi bấm → `BaHatMit.confirmMenu` / `Whis.confirmMenu` → `CombineService.startCombine(player)`.
4. Sau khi xong: `idMark.setIndexMenu(IGNORE_MENU)`, `combineNew.clearParamCombine()`, cập nhật `lastTimeCombine`.

**Hằng số loại combine** (`CombineService.java` dòng 32–58)

| Hằng | Giá trị | Có được xử lý trong `showInfoCombine`/`startCombine`? |
|---|---|---|
| `EP_SAO_TRANG_BI` | 500 | Có |
| `PHA_LE_HOA_TRANG_BI` | 501 | Có (+ `startCombineVip`) |
| `NANG_CAP_VAT_PHAM` | 510 | Có |
| `NANG_CAP_BONG_TAI` | 511 | Có |
| `LAM_PHEP_NHAP_DA` | 512 | **Không** (tab mở được nhưng không xử lý) |
| `NHAP_NGOC_RONG` | 513 | Có |
| `PHAN_RA_DO_THAN_LINH` | 514 | **Không** (chỉ có text tab) |
| `CHE_TAO_TRANG_BI_THIEN_SU` | 515 | Có |
| `NANG_CHI_SO_BONG_TAI` | 517 | Có |
| `NANG_CAP_BONG_TAI3` | 455 | Có |
| `NANG_CHI_SO_BONG_TAI3` | 457 | Có |
| `NANG_CAP_SAO_PHA_LE` | 100 | Có |
| `DANH_BONG_SAO_PHA_LE` | 101 | Có |
| `CUONG_HOA_LO_SAO_PHA_LE` | 102 | Có |
| `TAO_DA_HEMATITE` | 103 | Có |
| `GIAM_DINH_SACH` | 104 | Có |
| `TAY_SACH` | 105 | Có |
| `NANG_CAP_SACH_TUYET_KY` | 106 | Có |
| `HOI_PHUC_SACH` | 107 | Có |
| `PHAN_RA_SACH` | 108 | Có |
| `DUI_DUC` | 109 | Có trong switch nhưng **không NPC nào mở tab** |
| `DA_MAI` | 110 | Có trong switch nhưng **không NPC nào mở tab** |
| `CHUYEN_HOA_TRANG_BI_VANG` | 994 | Có |
| `CHUYEN_HOA_TRANG_BI_NGOC` | 995 | Có |
| `PHAN_RA_TRANG_BI_KH` | 996 | Có |
| `TAI_TAO_CAPSULE_KH` | 997 | Có |

Hằng khác: `MAX_STAR_ITEM = 9` (số lỗ sao pha lê tối đa), `MAX_LEVEL_ITEM = 8` (cấp nâng tối đa +8).

---

## 2. Cách tính tỉ lệ (`Util.isTrue`)

`utils/Util.java` dòng 204–215:

```java
isTrue(long ratio, long total)  -> nextLong(total) < ratio
isTrue(float ratio, long total) -> nếu ratio < 1 thì ratio*=100, total*=100, rồi gọi bản long
```

- `Util.isTrue(50, 100)` = 50%.
- `Util.isTrue(0.3f, 100)` → `isTrue(30, 10000)` = 0,3%.
- `Util.isTrue(0.7f, 100)` → `isTrue(70, 10000)` = 0,7%.
- `Util.isTrue(50, 200)` = **25%** (dùng trong cường hóa lỗ sao).
- `Util.nextInt(a, b)` trả về số nguyên trong đoạn **[a, b]** (bao gồm cả b); `Util.nextInt(n)` trả về [0, n).

---

## 3. NPC & menu mở từng chức năng

### 3.1 Bà Hạt Mít (`npc_list/BaHatMit.java`)

| Map | Tên map (DB) | Menu gốc | Chức năng con |
|---|---|---|---|
| 5 | Đảo Kamê | "Chức năng pha lê" | Ép sao trang bị · Pha lê hóa trang bị · Nâng cấp Sao pha lê · Đánh bóng Sao pha lê · Cường hóa lỗ sao pha lê · Tạo đá Hematite (dòng 139–146, 159–179) |
| 5 | Đảo Kamê | "Chuyển hóa Trang bị" | Chuyển hóa Vàng · Chuyển hóa Ngọc (dòng 148–151, 180–187) |
| 5 | Đảo Kamê | "Võ đài Sinh tử" | Dịch chuyển sang map 112 (không thuộc combine) |
| 5 | Đảo Kamê | "Phân rã Trang bị Kích hoạt" | `PHAN_RA_TRANG_BI_KH` |
| 5 | Đảo Kamê | "Tái tạo Capsule Kích hoạt" | `TAI_TAO_CAPSULE_KH` |
| 42, 43, 44, 84 | Vách núi Aru, Vách núi Moori, Vách núi Kakarot, Siêu Thị | "Thưởng Bùa 1h ngẫu nhiên" (nếu còn lượt ngày) | Nhận ngẫu nhiên bùa id 213–219 trong 60 phút |
| 42, 43, 44, 84 | – | "Sách Tuyệt Kỹ" | Đóng thành Sách cũ · Đổi Sách Tuyệt kỹ · Giám định Sách · Tẩy Sách · Nâng cấp Sách Tuyệt kỹ · Hồi phục Sách · Phân rã Sách |
| 42, 43, 44, 84 | – | "Cửa hàng Bùa" | Shop BUA_1H / BUA_8H / BUA_1M |
| 42, 43, 44, 84 | – | "Nâng cấp Vật phẩm" | `NANG_CAP_VAT_PHAM` |
| 42, 43, 44, 84 | – | "Nâng cấp Bông tai Porata" / "Mở chỉ số Bông tai Porata cấp 2" | Chỉ hiện nếu túi hoặc rương có item 454 hoặc 921. Nếu có 921 (tìm cả túi & rương) → mở chỉ số cấp 2; ngược lại → nâng cấp lên cấp 2 |
| 42, 43, 44, 84 | – | "Làm phép Nhập đá" | `LAM_PHEP_NHAP_DA` (không hoạt động – xem mục 10) |
| 42, 43, 44, 84 | – | "Nhập Ngọc Rồng" | `NHAP_NGOC_RONG` |
| 42, 43, 44, 84 | – | "Nâng cấp Bông tai Porata cấp 3" / "Mở chỉ số Bông tai Porata cấp 3" | Hiện nếu có bông tai cấp 2 hoặc 1819. Có 1819 → mở chỉ số cấp 3; chỉ có cấp 2 → nâng cấp lên cấp 3 |

Ghi chú về `openBaseMenu` (dòng 86–126): menu mặc định áp dụng cho mọi map khác 5/112/174/181, nhưng `confirmMenu` chỉ xử lý map **42, 43, 44, 84** (dòng 314).

### 3.2 Whis (`npc_list/Whis.java`)

| Map | Tên map | Menu | Chức năng |
|---|---|---|---|
| 154 | Hành tinh Bill | "Nói chuyện" → "Shop thiên sứ" / "Chế tạo" | Shop `THIEN_SU`; Chế tạo trang bị Thiên Sứ (yêu cầu đang **mặc đủ 5 món Hủy Diệt** – `SetClothes.checkSetDes()`: 5 ô body 0–4 đều có id 650–662) |
| 154 | Hành tinh Bill | "Học tuyệt kỹ" | Học / nâng tuyệt kỹ (mục 12.2) |
| 154 | Hành tinh Bill | "Top 100" / "[LV:x]" | Không thuộc combine |

---

## 4. Nhóm Sao pha lê (Bà Hạt Mít – Đảo Kamê)

### 4.1 Ép sao trang bị

- **File:** `combine/EpSaoTrangBi.java` (`showInfoCombine` dòng 18–78, `epSaoTrangBi` dòng 80–167); giá: `CombineSystem.getGemEpSao`.
- **NPC:** Bà Hạt Mít, map 5 → Chức năng pha lê → Ép sao trang bị.

**Đầu vào (đúng 2 món):**

| Ô | Điều kiện |
|---|---|
| Trang bị | `CombineSystem.isTrangBiPhaLeHoa`: `template.type < 5` (áo 0, quần 1, găng 2, giày 3, rada 4) hoặc `type == 32` |
| Đá pha lê | `CombineSystem.isDaPhaLe`: `template.type == 30` (các loại Sao pha lê) **hoặc** id 14–20 (Ngọc Rồng 1–7 sao) |

**Điều kiện:**
- Trang bị có option 107 (số lỗ) = `starEmpty`, option 102 (số sao đã ép) = `star`.
- `starEmpty <= 9` (nếu không → "lỗ sao tối đa là 9").
- `star < starEmpty` (nếu không → "Không thể ép sao cao hơn lỗ").
- Nếu `star >= 7` và `starEmpty >= 8` thì phải đã cường hóa: `CombineService.CheckSlot()` yêu cầu option 228 có `param >= starEmpty` (dòng 483–497). Nếu chưa → "Cần cường hóa lỗ sao pha lê này trước".

**Chi phí & tỉ lệ:**

| Số sao hiện tại (`star`) | 0 | 1 | 2 | 3 | 4 | 5 | 6 | 7 | 8 |
|---|---|---|---|---|---|---|---|---|---|
| Ngọc | 10 | 10 | 10 | 10 | 10 | 10 | 10 | 10 | 10 |
| Tỉ lệ | 100% | 100% | 100% | 100% | 100% | 100% | 100% | 100% | 100% |

Không tốn vàng. Không có random – luôn thành công.

**Kết quả:**
- Trừ ngọc, trừ 1 đá.
- Option của đá được cộng vào trang bị:
  - Nếu là sao thứ 8 hoặc 9 (`star + 1` = 8 hoặc 9): **thêm dòng option mới riêng** (không gộp).
  - Các sao 1–7: nếu trang bị đã có option cùng id → cộng dồn `param`; chưa có → thêm dòng mới.
- Option 102 tăng 1 (hoặc thêm `102 = 1`).

**Chỉ số của từng loại đá** (`CombineSystem.getOptionDaPhaLe` / `getParamDaPhaLe`):

| Đá | Option | Param |
|---|---|---|
| Đá `type == 30` (Sao pha lê 441–447, cấp 2 1416–1422, lấp lánh 1426–1432…) | lấy option **đầu tiên** (`itemOptions.get(0)`) của viên đá | `param` của option đó |
| 20 – Ngọc Rồng 7 sao | 77 – HP+#% | 5 |
| 19 – Ngọc Rồng 6 sao | 103 – KI +#% | 5 |
| 18 – Ngọc Rồng 5 sao | 80 – HP+#%/30s | 5 |
| 17 – Ngọc Rồng 4 sao | 81 – KI+#%/30s | 5 |
| 16 – Ngọc Rồng 3 sao | 50 – Sức đánh+#% | 3 |
| 15 – Ngọc Rồng 2 sao | 94 – Giảm #% sát thương | 2 |
| 14 – Ngọc Rồng 1 sao | 108 – #% Né đòn | 2 |

Option mặc định của Sao pha lê cấp 1 khi rơi từ quái (`Util.spl`, `utils/Util.java` dòng 555–585):

| Item | Tên | Option | Param |
|---|---|---|---|
| 441 | Sao pha lê đỏ | 95 – Biến #% tấn công thành HP | 5 |
| 442 | Sao pha lê lam | 96 – Biến #% tấn công thành KI | 5 |
| 443 | Sao pha lê hồng | 97 – Phản #% sát thương | 5 |
| 444 | Sao pha lê tím | 99 – Xuyên giáp #% cận chiến | 3 |
| 445 | Sao pha lê cam | 98 – Xuyên giáp #% chưởng | 3 |
| 446 | Sao pha lê vàng | 100 – +#% vàng từ quái | 5 |
| 447 | Sao pha lê lục | 101 – +#% tiềm năng, sức mạnh | 5 |

(Các case 441–447 trong `getOptionDaPhaLe` không bao giờ chạy tới vì đá type 30 đã return sớm.)

### 4.2 Pha lê hóa trang bị (đục lỗ)

- **File:** `combine/PhaLeHoaTrangBi.java`; giá ở `CombineSystem.getGoldPhaLeHoa`, `getGemPhaLeHoa`; tỉ lệ thật ở `PhaLeHoaTrangBi.getRatio` (dòng 65–88); tỉ lệ hiển thị ở `getFakeRatio` (dòng 90–113).
- **NPC:** Bà Hạt Mít, map 5 → Pha lê hóa trang bị.

**Đầu vào:** đúng 1 trang bị `isTrangBiPhaLeHoa` (type < 5 hoặc type 32). Số lỗ hiện tại (option 107) phải `< 9`.

**Menu:** "Nâng cấp (cần X ngọc)" · "Nâng cấp 10 lần" · "Nâng cấp 100 lần". Chỉ hiện nếu đủ vàng cho 1 lần.

**Bảng giá & tỉ lệ theo số lỗ hiện tại:**

| Lỗ hiện tại → sau | Vàng / lần | Ngọc / lần | Tỉ lệ **thật** (code) | Tỉ lệ NPC hiển thị |
|---|---|---|---|---|
| 0 → 1 | 5.000.000 | 1 | 50% | 80% |
| 1 → 2 | 10.000.000 | 2 | 20% | 40% |
| 2 → 3 | 20.000.000 | 3 | 10% | 30% |
| 3 → 4 | 40.000.000 | 4 | 5% | 20% |
| 4 → 5 | 60.000.000 | 5 | 1% | 10% |
| 5 → 6 | 90.000.000 | 6 | 0,7% | 5% |
| 6 → 7 | 120.000.000 | 7 | 0,5% | 3% |
| 7 → 8 | 200.000.000 | 8 | 0,1% | 2% |
| 8 → 9 | 300.000.000 | 9 | 0,1% | 1% |

**Cơ chế (`phaLeHoa(player, n)` dòng 115–214):**
- Chống spam: 2 lần gọi cách nhau tối thiểu 500 ms.
- `n = 1` (nút thường), `10` hoặc `100` (`startCombineVip`).
- Mỗi lần thử: kiểm tra đủ vàng & ngọc, **trừ vàng + ngọc**, roll `Util.isTrue(getRatio(star), 100)`.
- Dừng ngay khi thành công (không đập tiếp). Hết vàng/ngọc giữa chừng → dừng.
- **Thành công:** lỗ +1 (option 107). **Thất bại:** chỉ mất vàng/ngọc, trang bị giữ nguyên (không giảm lỗ, không mất đồ).
- NPC chat ngẫu nhiên 1 trong 8 câu khi thất bại.

> `CombineSystem.getRatioPhaLeHoa()` (50/20/10/5/2/1/1/0,5/0,3/…) **không được dùng** ở đâu cả.

### 4.3 Tạo đá Hematite

- **File:** `combine/TaoDaHematite.java`.
- **NPC:** Bà Hạt Mít, map 5 → Tạo đá Hematite.

| Mục | Giá trị |
|---|---|
| Đầu vào | Đúng 1 ô: Sao pha lê **cấp 1** (id 441–447), số lượng ≥ 5 (cùng 1 chồng) |
| Vàng | 50.000.000 |
| Tỉ lệ | 100% (`RATIO_TAO_DA = 100`) |
| Tiêu hao | 5 sao pha lê + vàng (trừ trước khi roll) |
| Kết quả | +1 **Hematite** (id 1423) |

Lời NPC ghi "Cần 5 sao pha lê Cấp 2" nhưng code kiểm tra id 441–447 (cấp 1).

### 4.4 Nâng cấp Sao pha lê (cấp 1 → cấp 2)

- **File:** `combine/NangCapSaoPhaLe.java`.

| Mục | Giá trị |
|---|---|
| Đầu vào (2 món) | 1 Sao pha lê cấp 1 (441–447) + 1 Hematite (1423) |
| Vàng | 200.000.000 |
| Ngọc | 10 |
| Tỉ lệ | 50% |
| Thành công | Tạo 1 viên cấp 2 tương ứng (sao chép **y nguyên** option của viên cấp 1), trừ 1 viên cấp 1, trừ 1 Hematite |
| Thất bại | Mất 1 Hematite + vàng + ngọc; viên sao pha lê cấp 1 giữ nguyên |

Mapping: 441→1416 (đỏ cấp 2), 442→1417 (lam), 443→1418 (hồng), 444→1419 (tím), 445→1420 (cam), 446→1421 (vàng), 447→1422 (lục).

### 4.5 Đánh bóng Sao pha lê (cấp 2 → lấp lánh)

- **File:** `combine/DanhBongSaoPhaLe.java`.

| Mục | Giá trị |
|---|---|
| Đầu vào (2 món) | Sao pha lê cấp 2 (1416–1422) số lượng ≥ 2 + 1 Đá mài (1439) |
| Vàng | 100.000.000 |
| Ngọc | 0 (`gemCombine` không được gán) |
| Tỉ lệ | 100% |
| Kết quả | 1 Sao pha lê lấp lánh (id = 1426 + (id cấp 2 − 1416)), **mỗi option param +1** so với viên cấp 2; trừ 2 viên cấp 2 + 1 đá mài |

Mapping: 1416→1426 (đỏ lấp lánh) … 1422→1432 (lục lấp lánh).

### 4.6 Cường hóa lỗ sao pha lê (ô thứ 8, 9)

- **File:** `combine/CuongHoaLoSaoPhaLe.java` (`cuongHoaLoSaoPhaLe` dòng 75–199).

| Mục | Giá trị |
|---|---|
| Đầu vào (3 món) | Trang bị `isTrangBiPhaLeHoa` + 1 Hematite (1423) + 1 Dùi đục (1438) |
| Vàng | 500.000.000 (`COST`) |
| Yêu cầu khác | Hành trang còn ≥ 1 ô trống |

**Điều kiện kiểm tra theo thứ tự:**
1. Trang bị phải có option 228 ("Cường hóa tới ô sao pha lê #") ≥ 8 **hoặc** option 102 (sao đã ép) **đúng bằng 7**. Nếu không → "Trang bị cần có đủ 7 lỗ để cường hóa."
2. Số lỗ (option 107) ≥ 8. Nếu không → yêu cầu nâng lên 8/9 lỗ trước.
3. Sau đó **trừ 500.000.000 vàng** rồi rẽ nhánh:

| Số lỗ (107) | Trạng thái option 228 | Kết quả | Tỉ lệ |
|---|---|---|---|
| 8 | chưa có | Thêm option 218 (dòng kẻ) nếu chưa có + `228 = 8` | 100% (không roll) |
| 8 | đã ≥ 8 | Báo "Trang bị đã có lỗ thứ 8", `return` | — (vàng **đã bị trừ**, không trừ nguyên liệu) |
| 9 | chưa có (và 102 == 7) | Thêm 218 + `228 = 8` | 100% |
| 9 | `228 == 8` | Thành công: `228 = 9`. Thất bại: không đổi | **25%** (`Util.isTrue(50, 200)`) — NPC hiển thị 50% |
| 9 | `228 == 9` | "Không thể cường hóa thêm", `return` | — (vàng đã bị trừ) |

Khi chạy tới cuối (các nhánh không `return`): trừ 1 Hematite + 1 Dùi đục (cả thành công lẫn thất bại).

### 4.7 Tạo Dùi đục / Tạo Đá mài (không có menu)

Code tồn tại và được map trong `CombineService` nhưng **không NPC nào gọi `openTabCombine(DUI_DUC | DA_MAI)`**, nên người chơi không thể dùng.

| Chức năng | File | Đầu vào | Vàng | Tỉ lệ | Kết quả |
|---|---|---|---|---|---|
| Tạo Dùi đục | `CheTaoDuiDuc.java` | 1 ô: Hematite (1423) ×5 | 50.000.000 | 100% | +1 Dùi đục (1438) |
| Tạo Đá mài | `TaoDaMai.java` (hàm tên `CheTaoDuiDuc`) | 1 ô: Dùi đục (1438) ×5 | 50.000.000 | 100% | +1 Đá mài (1439) |

---

## 5. Chuyển hóa trang bị (Vàng / Ngọc)

- **File:** `combine/ChuyenHoaTrangBi_Vang.java`, `combine/ChuyenHoaTrangBi_Ngoc.java`.
- **NPC:** Bà Hạt Mít, map 5 → Chuyển hóa Trang bị → Chuyển hóa Vàng / Chuyển hóa Ngọc.
- **Ý nghĩa:** chuyển cấp nâng (+N), sao pha lê và các option phụ từ **trang bị gốc** sang **đồ Thần Linh chưa nâng cấp**.

**Đầu vào:** đúng 2 món, **thứ tự quan trọng**: ô 1 = trang bị gốc, ô 2 = đồ cần chuyển hóa.

| Điều kiện | Bản Vàng | Bản Ngọc |
|---|---|---|
| Trang bị gốc hợp lệ | "Lưỡng long" (241, 253, 265, 277, 281), "Jean" (237, 249, 261, 273), "Zelot" (233, 245, 257, 269) **hoặc đồ Thần Linh 555–567** | Chỉ "Lưỡng long", "Jean", "Zelot" (không gồm Thần Linh) |
| Cấp trang bị gốc (option 72) | ≥ +4 | ≥ +4 |
| Đồ cần chuyển hóa | id 555–567 (toàn bộ đồ Thần Linh) | Ba dải 555–557, 558–560, 561–563 (tức id 555–563: áo/quần Thần Linh 3 hành tinh, Nhẫn Thần Linh 561, Găng 562 & Giầy 563 Thần Linh Trái Đất; **không** gồm 564–567) |
| Đồ chuyển hóa chưa nâng cấp & chưa ép sao | Không có option 72 và 102 | Như bản Vàng |
| Cùng loại & cùng hành tinh | `template.type` và `template.gender` bằng nhau | Như bản Vàng |
| Chi phí hiển thị | "Cần 2 tỷ vàng" | "Cần 5000 ngọc" |
| Chi phí **thực trừ** | 2.000.000.000 vàng | **2.000.000.000 vàng** (không trừ ngọc) |
| Tỉ lệ | 100% | 100% |

Tên gọi trong code không khớp tên DB: nhóm `isDoJean` (237, 249, 261, 273) trong DB là **"… vàng Zealot"** (Namếc); nhóm `isDoZelot` (233, 245, 257, 269) là **"… jean Calic"** (Trái Đất); nhóm `isDoLuongLong` (241, 253, 265, 277) là **"… lưỡng long"** (Xayda) + 281 **"Rada cấp 12"**.

**Công thức chỉ số gốc (`thucHienChuyenHoa`):**

```
chiSoGoc = param option đầu tiên của đồ Thần Linh
chiSoMoi = (int)(chiSoGoc × 1,1^cấp × 0,9^soLanRotCap)
```
- `cấp` = option 72 của trang bị gốc; `soLanRotCap` = tổng option 209 ("Bị rớt cấp # lần").
- Option "chỉ số gốc" = một trong {0, 6, 7, 14, 22, 23, 47}.

**Đồ mới tạo ra:**
1. Template = template đồ Thần Linh.
2. Chép các option của đồ Thần Linh (trừ option bị bỏ qua), option chỉ số gốc được thay bằng `chiSoMoi`.
3. Chép từ trang bị gốc mọi option **không** phải chỉ số gốc và **không** bị bỏ qua (gồm option 72 – cấp, 102/107 – sao pha lê, option từ đá pha lê, option 209…).
4. Option bị bỏ qua (`isIgnoredOption`): 228, 236, 127–144 (set kích hoạt), 210–218, 224–227, 233–248.
5. Mất cả 2 món gốc, trừ 2 tỷ vàng.

---

## 6. Đồ kích hoạt: Phân rã & Tái tạo Capsule

### 6.1 Phân rã trang bị kích hoạt

- **File:** `combine/PhanRaTrangBiKichHoat.java` (inner class `PhanRaTrangBi`).
- **NPC:** Bà Hạt Mít, map 5 → "Phân rã Trang bị Kích hoạt".

| Mục | Giá trị |
|---|---|
| Đầu vào | Đúng 1 món có ít nhất 1 option thuộc {127, 128, 129, 130, 131, 132, 133, 134, 135, 233, 237, 241, 245} (tên set kích hoạt) |
| Vàng | 2.000.000.000 |
| Tỉ lệ | 100% |
| Tiêu hao | 1 món + vàng |
| Kết quả | +1 **Khoáng tái chế** (1656) (cộng dồn nếu đã có) |

### 6.2 Tái tạo Capsule kích hoạt

- **File:** `combine/TaiTaoCapsuleKichHoat.java`.
- **NPC:** Bà Hạt Mít, map 5 → "Tái tạo Capsule Kích hoạt".

| Mục | Giá trị |
|---|---|
| Đầu vào | Tổng ≥ 3 **Khoáng tái chế** (1656) + ≥ 1 **Cápsule Vỡ** (1634) trong các ô đã chọn |
| Vàng | 2.000.000.000 |
| Tỉ lệ | 100% |
| Kết quả | +1 **Cápsule Kích hoạt 1 món tự chọn** (1655) |

Khi dùng item 1655 → menu Áo/Quần/Găng/Giày/Rada (`NpcFactory` case `CAPSULE_KICH_HOAT`) → `ItemService.OpenSKH()` (`services/ItemService.java` dòng 130–191): chọn ngẫu nhiên 1 template trong danh sách của loại đồ & hành tinh, gắn 1 set kích hoạt ngẫu nhiên:

| Hành tinh | Set có thể ra (option id) |
|---|---|
| Trái Đất | 128 Kirin, 129 Sôngôku, 127 Thên Xin Hăng, 233 Gohan, 245 Thần Vũ Trụ Kaio |
| Namếc | 130 Picolo, 131 Ốc tiêu, 132 Pikkoro Daimao, 233 Gohan, 237 Nail chiến binh Namếc |
| Xayda | 133 Kakarot, 135 Nappa, 134 Ca Đíc, 233 Gohan, 241 Cađic M |

---

## 7. Nâng cấp vật phẩm +1 → +8

- **File:** `combine/NangCapVatPham.java`; bảng số liệu `CombineSystem.getGoldNangCapDo`, `getTileNangCapDo`, `getCountDaNangCapDo`, `getCountDaBaoVe`, `isCoupleItemNangCapCheck`.
- **NPC:** Bà Hạt Mít, map 42/43/44/84 → "Nâng cấp Vật phẩm".

**Đầu vào:** 2 hoặc 3 món:
- 1 trang bị `type < 5`;
- 1 đá nâng cấp `type == 14`, **đúng loại** với trang bị;
- (tùy chọn) 1 **Đá bảo vệ** (id 987).

**Đá theo loại đồ** (`isCoupleItemNangCapCheck`):

| Loại đồ | type | Đá | id |
|---|---|---|---|
| Áo | 0 | Đá Titan | 223 |
| Quần | 1 | Đá Ruby | 222 |
| Găng | 2 | Đá thạch anh tím | 224 |
| Giày | 3 | Đá Saphia | 221 |
| Rada | 4 | Đá lục bảo | 220 |

**Bảng cấp:**

| Cấp hiện tại → sau | Số đá | Vàng | Tỉ lệ | Thất bại (không đá bảo vệ) |
|---|---|---|---|---|
| +0 → +1 | 3 | 10.000 | 80% | Giữ cấp |
| +1 → +2 | 7 | 70.000 | 50% | Giữ cấp |
| +2 → +3 | 11 | 300.000 | 20% | **Rớt về +1** |
| +3 → +4 | 17 | 1.500.000 | 10% | Giữ cấp |
| +4 → +5 | 23 | 7.000.000 | 7% | **Rớt về +3** |
| +5 → +6 | 35 | 23.000.000 | 5% | Giữ cấp |
| +6 → +7 | 50 | 100.000.000 | 1% | **Rớt về +5** |
| +7 → +8 | 70 | 250.000.000 | 0,3% | Giữ cấp |

Đá bảo vệ: **1 viên / lần** cho mọi cấp (`getCountDaBaoVe` luôn trả 1).

**Xử lý (`nangCapVatPham` dòng 126–262):**
- Kiểm tra: đủ vàng, đủ số đá, (nếu 3 món) đủ đá bảo vệ; cấp < 8.
- Trừ vàng **trước khi roll**.
- **Thành công** (`Util.isTrue(ratio, 100)`):
  - Option chỉ số chính (một trong 47 Giáp, 6 HP, 0 Tấn công, 7 KI, 14 Chí mạng, 22 HP K, 23 KI K) `+= 10%` (tối thiểu +1).
  - Nếu có option 27 (+# HP/30s) hoặc 28 (+# KI/30s): `+= 10%` (tối thiểu +1).
  - Option 72 (cấp) +1 (hoặc thêm `72 = 1`).
  - Lên +8 (từ cấp 7): cập nhật nhiệm vụ danh hiệu `THANH_DAP_DO_7`.
- **Thất bại:**
  - Nếu cấp hiện tại là 2, 4 hoặc 6 **và không đặt đá bảo vệ**: chỉ số chính `-= 11%` (tối thiểu 1), option 27/28 `-= 11%`, cấp −1, option 209 ("Bị rớt cấp # lần") +1.
  - Các trường hợp khác: không đổi.
- Luôn trừ: số đá nâng cấp theo bảng + (nếu có) 1 đá bảo vệ.

---

## 8. Bông tai Porata

Số lượng mảnh (933, 934, 1820) được lưu trong **option 31 ("Số lượng #")** của item, đọc bằng `InventoryService.getParam(player, 31, itemId)` và trừ bằng `subParamItemsBag` (item bị xóa khi param ≤ 0).

### 8.1 Nâng cấp Bông tai Porata cấp 1 → cấp 2

- **File:** `combine/NangCapBongTai.java`.

| Mục | Giá trị |
|---|---|
| Đầu vào (2 món) | Bông tai Porata (454) + Mảnh vỡ bông tai (933) |
| Điều kiện | Số mảnh (option 31) ≥ 9.999 (kiểm tra ở `showInfoCombine`); trong túi **chưa có** item 921 |
| Vàng | 200.000.000 / lần |
| Ngọc | 1.000 / lần |
| Tỉ lệ | 50% |
| Thành công | Đổi template thành 921, **xóa hết option**, chỉ còn `72 = 2`; trừ 9.999 mảnh |
| Thất bại | Trừ 99 mảnh |

### 8.2 Mở chỉ số Bông tai Porata cấp 2

- **File:** `combine/NangChiSoBongTai.java`.

| Mục | Giá trị |
|---|---|
| Đầu vào (3 món) | Bông tai Porata (921) + Mảnh hồn bông tai (934) + Đá xanh lam (935) |
| Mảnh hồn cần | **200** (`REQUIRED_HON_BONG_TAI`; lời tab ghi 99) |
| Ngọc | 1.000 |
| Vàng | 0 |
| Tỉ lệ | 45% |
| Thành công | Xóa option cũ; thêm **1** option ngẫu nhiên trong {77 HP%, 80 HP%/30s, 81 KI%/30s, 103 KI%, 50 Sức đánh%, 94 Giảm sát thương%, 5 Sức đánh chí mạng%} với param ngẫu nhiên 5–15; thêm `72 = 2` |
| Thất bại | Giữ nguyên chỉ số cũ |
| Tiêu hao (cả 2 trường hợp) | 1.000 ngọc + 200 mảnh hồn + 1 Đá xanh lam |

### 8.3 Nâng cấp Bông tai Porata cấp 2 → cấp 3

- **File:** `combine/NangCapBongTai3.java`.

| Mục | Giá trị |
|---|---|
| Đầu vào (2 món) | Bông tai Porata (921) + Mảnh vỡ bông tai cấp 3 (1820) |
| Điều kiện | Số mảnh ≥ **20.000** (lời tab ghi 19999); trong túi chưa có 1819 |
| Vàng | 200.000.000 |
| Ngọc | 1.000 |
| Tỉ lệ | 50% |
| Thành công | Template → 1819, **xóa hết option** (mất chỉ số cấp 2), chỉ còn `72 = 3`; trừ 20.000 mảnh |
| Thất bại | Trừ 200 mảnh |

### 8.4 Mở chỉ số Bông tai Porata cấp 3

- **File:** `combine/NangChiSoBongTai3.java`.

| Mục | Giá trị |
|---|---|
| Đầu vào (3 món) | Bông tai Porata (1819) + Mảnh hồn bông tai (934) + Đá xanh lam (935) |
| Mảnh hồn cần | **99** (lời tab ghi 199) |
| Ngọc | 1.000 |
| Tỉ lệ | 30% |
| Thành công | Xóa option cũ; thêm **2** option ngẫu nhiên (có thể trùng id) từ cùng danh sách {77, 80, 81, 103, 50, 94, 5}, mỗi dòng param 5–15; thêm `72 = 3` |
| Thất bại | Giữ nguyên |
| Tiêu hao (cả 2 trường hợp) | 1.000 ngọc + 99 mảnh hồn + 1 Đá xanh lam |

---

## 9. Nhập Ngọc Rồng

- **File:** `combine/NhapNgocRong.java`.
- **NPC:** Bà Hạt Mít, map 42/43/44/84 → "Nhập Ngọc Rồng".

| Mục | Giá trị |
|---|---|
| Đầu vào | Đúng 1 ô: Ngọc Rồng id **15–20** (2 sao → 7 sao), số lượng ≥ 7 |
| Điều kiện | Hành trang ≥ 1 ô trống |
| Chi phí | Không |
| Tỉ lệ | 100% |
| Kết quả | −7 viên, +1 viên sao cao hơn 1 bậc (id − 1) |

| Nguyên liệu | Kết quả |
|---|---|
| 7 × Ngọc Rồng 7 sao (20) | 1 × Ngọc Rồng 6 sao (19) |
| 7 × Ngọc Rồng 6 sao (19) | 1 × Ngọc Rồng 5 sao (18) |
| 7 × Ngọc Rồng 5 sao (18) | 1 × Ngọc Rồng 4 sao (17) |
| 7 × Ngọc Rồng 4 sao (17) | 1 × Ngọc Rồng 3 sao (16) |
| 7 × Ngọc Rồng 3 sao (16) | 1 × Ngọc Rồng 2 sao (15) |
| 7 × Ngọc Rồng 2 sao (15) | 1 × Ngọc Rồng 1 sao (14) |

---

## 10. Làm phép nhập đá

- **File:** `combine/LamPhepNhapDa.java` (hàm `showInfoCombine`, `lamphepnhapda`).
- **NPC:** Bà Hạt Mít, map 42/43/44/84 → "Làm phép Nhập đá" → mở tab `LAM_PHEP_NHAP_DA`.
- **Trạng thái:** `CombineService.showInfoCombine` và `startCombine` **không có case `LAM_PHEP_NHAP_DA`** → đặt đồ vào không có phản hồi, chức năng **không hoạt động**.

Spec trong class (nếu được nối lại):

| Mục | Giá trị |
|---|---|
| Đầu vào (2 món, thứ tự bất kỳ) | Mảnh đá vụn (225) ≥ 10 + Bình nước phép (226) ≥ 1 |
| Vàng | 10.000.000 |
| Tỉ lệ | 80% |
| Tiêu hao | 10 mảnh đá vụn + 1 bình nước phép + vàng (trước khi roll) |
| Thành công | +1 đá ngẫu nhiên id 220–224 (Đá lục bảo / Saphia / Ruby / Titan / Thạch anh tím) |
| Thất bại | Mất nguyên liệu |

---

## 11. Sách Tuyệt Kỹ

NPC: Bà Hạt Mít, map 42/43/44/84 → "Sách Tuyệt Kỹ" (menu `MENU_SACH_TUYET_KY`).

Sách Tuyệt Kỹ 1: 1044 (Trái Đất), 1211 (Namếc), 1212 (Xayda). Sách Tuyệt Kỹ 2: 1278, 1279, 1280.

### 11.1 Đóng thành Sách cũ

- **File:** `combine/CheTaoCuonSachCu.java` (không qua tab combine, dùng item trong túi).

| Mục | Giá trị |
|---|---|
| Nguyên liệu | 9.999 Trang sách cũ (1281) + 1 Bìa sách (1282) |
| Chi phí | Không |
| Điều kiện | Còn ô trống **hoặc** đã có Cuốn sách cũ (1283) trong túi |
| Tỉ lệ | 20% |
| Thành công | −9.999 trang, −1 bìa → +1 Cuốn sách cũ (1283) kèm option 30 (không thể giao dịch) |
| Thất bại | −99 trang sách cũ, −1 bìa sách |

### 11.2 Đổi Sách Tuyệt Kỹ 1

- **File:** `combine/DoiSachTuyetKy.java`.

| Mục | Không dùng Con dấu | Dùng Con dấu (1794) |
|---|---|---|
| Nguyên liệu | 10 Cuốn sách cũ (1283) + 1 Kìm bấm giấy (1285) | + 1 Con dấu |
| Tỉ lệ | 20% | 100% |
| Thất bại | −5 cuốn sách cũ, −1 kìm | — |
| Số dòng "Chưa giám định" (option 217) | 1 | 20%: ngẫu nhiên 1–3 dòng; 80%: 1 dòng |

Sách tạo ra (`createSach`): id ngẫu nhiên trong **{1044, 1211, 1212}** (không lọc theo hành tinh người chơi), kèm option: 217 (chưa giám định) × N, 21 = 40 (yêu cầu sức mạnh 40 tỉ), 30 (không giao dịch), 87 (ký gửi ngọc), 219 = 5 (số lần tẩy), 212 = 1000 (độ bền 1000/1000).

### 11.3 Giám định sách

- **File:** `combine/GiamDinhSach.java`.

| Mục | Giá trị |
|---|---|
| Đầu vào (2 món) | Sách Tuyệt Kỹ 1 hoặc 2 + Bùa giám định (1284) |
| Điều kiện | Sách còn option 217 |
| Chi phí | 1 Bùa giám định |
| Tỉ lệ | 100% |
| Kết quả | Mỗi dòng 217 được thay bằng 1 option ngẫu nhiên trong {77, 103, 50, 108, 94, 14, 80, 81, 175, 5, 214, 216}, param = `nextInt(1, 10 / nextInt(1,3))` → tối đa 10, 5 hoặc 3 (mỗi mức xác suất 1/3) |

### 11.4 Tẩy sách

- **File:** `combine/TaySach.java`.

| Mục | Giá trị |
|---|---|
| Đầu vào | 1 Sách Tuyệt Kỹ 1/2 |
| Điều kiện | Option 219 (số lần tẩy) > 0 và **không** còn dòng 217 |
| Chi phí | Không |
| Kết quả | Tất cả option đứng trước option 21 bị đổi thành 217 (chưa giám định); 219 −1 |

### 11.5 Nâng cấp Sách Tuyệt Kỹ (1 → 2)

- **File:** `combine/NangCapSachTuyetKy.java`.

| Mục | Giá trị |
|---|---|
| Đầu vào (2 món) | Sách Tuyệt Kỹ **1** + Kìm bấm giấy (1285) ≥ 10 |
| Chi phí | 10 Kìm bấm giấy (mất cả khi thất bại) |
| Tỉ lệ | 10% |
| Thành công | 1044→1278, 1211→1279, 1212→1280 (giữ nguyên option) |

### 11.6 Hồi phục sách

- **File:** `combine/HoiPhucSach.java`.

| Mục | Giá trị |
|---|---|
| Đầu vào | 1 sách có độ bền (option 212) < 1000 |
| Ngọc | `max(5, (1000 − độBền) × 50 / 1000)` → từ 5 đến 50 ngọc |
| Kết quả | Độ bền = 1000 |

### 11.7 Phân rã sách

- **File:** `combine/PhanRaSach.java`.

| Mục | Giá trị |
|---|---|
| Đầu vào | 1 Sách Tuyệt Kỹ 1/2 |
| Phí hiển thị | 10.000.000 vàng (chỉ kiểm tra có đủ) |
| Phí **thực trừ** | **Không trừ vàng** |
| Kết quả | −1 sách, +5 Cuốn sách cũ (1283, option 30) |

---

## 12. Whis: Chế tạo trang bị Thiên Sứ & Học tuyệt kỹ

### 12.1 Chế tạo trang bị Thiên Sứ

- **File:** `combine/CheTaoTrangBiThienSu.java`; mở từ `npc_list/Whis.java` (map 154) khi đang mặc đủ 5 món Hủy Diệt.
- **Quan trọng:** toàn bộ logic chế tạo nằm trong **`showInfoCombine`** → chế tạo được thực hiện **ngay khi đặt đủ 4 món vào ô**, không có bước xác nhận.

**Nguyên liệu (đúng 4 món):**

| Nguyên liệu | id hợp lệ | Số lượng tiêu hao |
|---|---|---|
| Công thức (`isCongThucVip`) | 1071–1073 (Công thức) hoặc 1084–1086 (Công thức VIP) | 1 |
| Mảnh Thiên Sứ (`isManhTS`) | 1066 Mảnh áo, 1067 Mảnh quần, 1068 Mảnh giầy, 1069 Mảnh nhẫn, 1070 Mảnh găng tay | 999 |
| Đá nâng cấp (`isDaNangCap1`) | 1074–1078 (cấp 1–5) | 1 |
| Đá may mắn (`isDaMayMan`) | 1079–1083 (cấp 1–5) | 1 |
| Vàng | — | 10.000.000 |
| Khác | Hành trang ≥ 1 ô trống | — |

**Tỉ lệ thành công:** `Util.nextInt(0, 100) < 90 + (idĐáNâng − 1073)`

| Đá nâng cấp | Ngưỡng | Xác suất thực (101 giá trị 0–100) |
|---|---|---|
| Cấp 1 (1074) | 91 | 91/101 ≈ 90,1% |
| Cấp 2 (1075) | 92 | ≈ 91,1% |
| Cấp 3 (1076) | 93 | ≈ 92,1% |
| Cấp 4 (1077) | 94 | ≈ 93,1% |
| Cấp 5 (1078) | 95 | ≈ 94,1% |

Nguyên liệu (1 công thức, 1 đá nâng, 999 mảnh, 1 đá may mắn) và 10 triệu vàng **mất cả khi thất bại**.

**Loại đồ ra** = hành tinh của Công thức (gender 0/1/2; nếu gender > 2 dùng hành tinh người chơi) × loại mảnh:

| Hành tinh \ Mảnh | 1066 áo | 1067 quần | 1070 găng | 1068 giầy | 1069 nhẫn |
|---|---|---|---|---|---|
| Trái Đất (0) | 1048 | 1051 | 1054 | 1057 | 1060 |
| Namếc (1) | 1049 | 1052 | 1055 | 1058 | 1061 |
| Xayda (2) | 1050 | 1053 | 1056 | 1059 | 1062 |

**Chỉ số gốc (`ItemService.DoThienSu`, `services/ItemService.java` dòng 1088–1134):**

| Món | Option | Giá trị | Bonus ×1,1 cho |
|---|---|---|---|
| Áo | 47 Giáp | 2.800–4.000 | Xayda |
| Quần | 22 HP K | 80%: 120–130K · 20%: 130–150K | Trái Đất |
| Găng | 0 Tấn công | 80%: 10.350–11.000 · 20%: 10.500–11.500 | Xayda |
| Giầy | 23 KI K | 80%: 90–110K · 20%: 110–130K | Namếc |
| Nhẫn | 14 Chí mạng % | 18–20 | Namếc |
| Chung | 21 = 30 (yêu cầu 30 tỉ SM), 30 = 1 | | |

(Bonus ×1,1 dựa trên `gender` của Công thức.)

**Sau khi tạo**, code gán `tilemacdinh = 100` rồi **cộng thêm 100% param cho mọi option có id khác 0 và 20** → giáp/HP/KI/chí mạng **nhân đôi**, và option 21 (yêu cầu sức mạnh) cũng tăng từ 30 → **60 tỉ**. Chỉ option 0 (tấn công của găng) giữ nguyên.

**Dòng may mắn:**
- `tileLucky = 5 + 5 × (idĐáMayMắn − 1078)` → cấp 1: 10, cấp 2: 15, cấp 3: 20, cấp 4: 25, cấp 5: 30.
- Roll `r = nextInt(0, 50)` (51 giá trị). Nếu `r <= tileLucky`:
  - `r >= tileLucky − 3` → 3 dòng;
  - `tileLucky − 10 <= r <= tileLucky − 4` → 2 dòng;
  - còn lại → 1 dòng.
- Thêm option 15 với param = số dòng, sau đó thêm N option **khác nhau** trong {50, 77, 103, 94, 5}, mỗi dòng param 1–3.

| Đá may mắn | 3 dòng | 2 dòng | 1 dòng | Không có dòng |
|---|---|---|---|---|
| Cấp 1 (T=10) | 4/51 (7,8%) | 7/51 (13,7%) | 0 | 40/51 |
| Cấp 2 (T=15) | 4/51 | 7/51 | 5/51 (9,8%) | 35/51 |
| Cấp 3 (T=20) | 4/51 | 7/51 | 10/51 (19,6%) | 30/51 |
| Cấp 4 (T=25) | 4/51 | 7/51 | 15/51 (29,4%) | 25/51 |
| Cấp 5 (T=30) | 4/51 | 7/51 | 20/51 (39,2%) | 20/51 |

**Nhánh `CheTaoTS` (nút "Nâng cấp")** — `CheTaoTrangBiThienSu.CheTaoTS` dòng 140–170, gọi qua `startCombine` khi Whis ở menu index 515. Không có chỗ nào tạo menu index 515 nên thực tế **không kích hoạt được**. Spec: 4 món, cần 1 đồ Hủy Diệt (650–662) + mảnh TS ≥ 5, trừ 500.000.000 vàng, 100% ra đồ Thiên Sứ theo hành tinh đồ Hủy Diệt × loại mảnh, trừ 1 đồ Hủy Diệt và **99** mảnh.

### 12.2 Học tuyệt kỹ (Whis)

- **File thực thi:** `npc_list/Whis.java` (`handleHocTuyetKy` dòng 146–188). Class `combine/HocTuyetKy.java` **không được gọi**.
- Tuyệt kỹ theo hành tinh: Trái Đất – Super Kamejoko, Namếc – Ma phong ba, Xayda – Liên hoàn chưởng (Cađíc).

| Điều kiện chung | Giá trị |
|---|---|
| Vật phẩm | Bí kíp tuyệt kỹ (1229) |
| Vàng | ≥ 10.000.000 |
| Ngọc | ≥ 99 |
| Sức mạnh | ≥ 60.000.000.000 |

| Trường hợp | Bí kíp cần | Tỉ lệ | Thành công | Thất bại |
|---|---|---|---|---|
| Học mới (chưa có skill hoặc point 0) | 9.999 | 100% (`isTrue(15, 15)`) | Học skill, −9.999 bí kíp | — |
| Nâng cấp (điều kiện: skill `currLevel ≥ 1000`, `point < 9`) | 999 | ≈3,33% (`isTrue(1, 30)`) | point +1, −999 bí kíp | −99 bí kíp |
| Chi phí mỗi lần | | | 10.000.000 vàng + 99 ngọc | 10.000.000 vàng + 99 ngọc |

Spec của class `HocTuyetKy.java` (không dùng): lv1 cần 9.999 bí kíp + 99 ngọc + 10 triệu vàng, cấp sau 999 bí kíp + 10 triệu vàng, tối đa cấp 7, không roll.

---

## 13. Các nâng cấp KHÔNG có trong code

Đã tìm trong toàn bộ `SRC/src/nro/models/**`, không có logic cho:

| Chức năng | Kết quả tìm kiếm |
|---|---|
| Đồ Thần Linh → Hủy Diệt (đập/đổi) | Không có combine. `PHAN_RA_DO_THAN_LINH = 514` chỉ có text tab, không xử lý; `ConstNpc.MENU_PHAN_RA_DO_THAN_LINH` không được dùng |
| Hủy Diệt → Thiên Sứ | Chỉ có chế tạo từ Công thức + Mảnh (mục 12.1) |
| Nâng cấp cải trang / cánh / ván bay | Không có |
| Đập đồ kích hoạt (SKH) trực tiếp | Không có; chỉ có Phân rã → Khoáng tái chế → Capsule (mục 6) |

---

## 14. Phụ lục: bảng tra item / option

### 14.1 Item (tra từ `item_template`)

| id | Tên | type |
|---|---|---|
| 14–20 | Ngọc Rồng 1 sao … 7 sao | 12 |
| 220 | Đá lục bảo | 14 |
| 221 | Đá Saphia | 14 |
| 222 | Đá Ruby | 14 |
| 223 | Đá Titan | 14 |
| 224 | Đá thạch anh tím | 14 |
| 225 | Mảnh đá vụn | 15 |
| 226 | Bình nước phép | 16 |
| 441–447 | Sao pha lê đỏ / lam / hồng / tím / cam / vàng / lục | 30 |
| 454 | Bông tai Porata (cấp 1) | 27 |
| 555–567 | Đồ Thần Linh (Áo/Quần/Găng/Giầy Thần Linh/Namếc/Xayda, 561 Nhẫn Thần Linh) | 0–4 |
| 650–662 | Đồ Hủy Diệt | 0–4 |
| 921 | Bông tai Porata (cấp 2) | 27 |
| 933 | Mảnh vỡ bông tai | 27 |
| 934 | Mảnh hồn bông tai | 27 |
| 935 | Đá xanh lam | 27 |
| 987 | Đá bảo vệ | 27 |
| 1044 / 1211 / 1212 | Sách tuyệt kỹ 1 (TĐ / NM / XD) | 25 |
| 1048–1062 | Áo/Quần/Găng/Giầy/Nhẫn Thiên Sứ | 0–4 |
| 1066 / 1067 / 1068 / 1069 / 1070 | Mảnh áo / quần / giầy / nhẫn / găng tay | 27 |
| 1071–1073 | Công thức | 27 |
| 1074–1078 | Đá nâng cấp cấp 1–5 | 27 |
| 1079–1083 | Đá may mắn cấp 1–5 | 27 |
| 1084–1086 | Công thức VIP | 27 |
| 1229 | Bí kíp tuyệt kỹ | 27 |
| 1278–1280 | Sách tuyệt kỹ 2 | 25 |
| 1281 | Trang sách cũ | 27 |
| 1282 | Bìa sách | 27 |
| 1283 | Cuốn sách cũ | 27 |
| 1284 | Bùa giám định | 27 |
| 1285 | Kìm bấm giấy | 27 |
| 1416–1422 | Sao pha lê đỏ … lục cấp 2 | 30 |
| 1423 | Hematite | 27 |
| 1426–1432 | Sao pha lê đỏ … lục lấp lánh | 30 |
| 1438 | Dùi đục | 27 |
| 1439 | Đá mài | 27 |
| 1634 | Cápsule Vỡ | 27 |
| 1655 | Cápsule Kích hoạt 1 món tự chọn | 27 |
| 1656 | Khoáng tái chế | 27 |
| 1794 | Con dấu | 75 |
| 1819 | Bông tai Porata (cấp 3) | 27 |
| 1820 | Mảnh vỡ bông tai cấp 3 | 27 |

### 14.2 Option (tra từ `item_option_template`)

| id | Tên |
|---|---|
| 0 | Tấn công+# |
| 5 | +#% sức đánh chí mạng |
| 6 / 7 | HP+# / KI+# |
| 14 | Chí mạng+#% |
| 15 | Phản đòn cận chiến+# (được dùng làm "số dòng may mắn" ở đồ Thiên Sứ) |
| 21 | Yêu cầu sức mạnh # tỉ |
| 22 / 23 | HP+#K / KI+#K |
| 27 / 28 | +# HP/30s / +# KI/30s |
| 30 | Không thể giao dịch |
| 31 | Số lượng # |
| 47 | Giáp+# |
| 50 | Sức đánh+#% |
| 72 | Cấp # |
| 77 | HP+#% |
| 80 / 81 | HP+#%/30s / KI+#%/30s |
| 87 | Ký gửi ngọc |
| 94 | Giảm #% sát thương |
| 95–101 | Option sao pha lê cấp 1 (xem 4.1) |
| 102 | # Sao Pha Lê (số sao đã ép) |
| 103 | KI +#% |
| 107 | # Sao Pha Lê (số lỗ) |
| 108 | #% Né đòn |
| 127–135 | Tên set kích hoạt |
| 175 | Giảm #% thời gian bị mù |
| 209 | Bị rớt cấp # lần |
| 212 | Độ bền #/1000 |
| 214 | Phạm vi tuyệt kỹ +#% |
| 216 | Tuyệt kỹ +#% sát thương |
| 217 | - Chưa giám định |
| 218 | (dòng kẻ "---------") |
| 219 | Số lần tẩy # |
| 228 | Cường hóa tới ô sao pha lê # |
| 233, 237, 241, 245 | Set Gohan / Nail chiến binh Namếc / Cađic M / Thần Vũ Trụ Kaio |

---

## 15. Ghi chú / điểm cần lưu ý

1. **Tỉ lệ pha lê hóa hiển thị sai:** NPC hiển thị `getFakeRatio` (80/40/30/20/10/5/3/2/1%) trong khi tỉ lệ thật là 50/20/10/5/1/0,7/0,5/0,1/0,1% (`PhaLeHoaTrangBi.java` dòng 45, 163).
2. **Cường hóa lỗ sao:** hiển thị 50% nhưng thực tế `Util.isTrue(50, 200)` = 25% (dòng 137). Vàng 500 triệu bị trừ **trước** các nhánh `return` ("đã có lỗ thứ 8", "không thể cường hóa thêm") → người chơi mất vàng mà không có gì; ở các nhánh đó cũng không gọi `sendMoney`.
3. **Chuyển hóa Ngọc:** NPC báo "Cần 5000 ngọc" nhưng code trừ **2 tỷ vàng** và không trừ ngọc. Cả 2 bản chuyển hóa **không kiểm tra đủ vàng** trước khi trừ → vàng có thể âm. `thucHienChuyenHoa` cũng không kiểm tra lại điều kiện.
4. **Nâng cấp vật phẩm – nút "Từ chối":** ở map 42/43/44/84, menu có 2 nút "Nâng cấp" (select 0) và "Từ chối" (select 1); `BaHatMit.confirmMenu` dòng 444–449 gọi `NangCapVatPham.nangCapVatPham()` cho **cả select 1** → bấm "Từ chối" vẫn đập đồ.
5. **Đóng thành Sách cũ:** handler `DONG_THANH_SACH_CU` (dòng 420–421) không kiểm tra `select` → bấm "Từ chối" cũng thực hiện chế tạo.
6. **Chế tạo đồ Thiên Sứ:** thực hiện ngay trong `showInfoCombine` (đặt đồ là đập, trừ 10 triệu vàng, không xác nhận). Mọi option (trừ 0, 20) bị nhân đôi, kể cả yêu cầu sức mạnh 30 → 60 tỉ. Lời tab ghi "0–15% chỉ số" không đúng thực tế. Nhánh `CheTaoTS` (500 triệu vàng, đồ Hủy Diệt) không có menu gọi.
7. **Phân rã sách:** kiểm tra đủ 10 triệu vàng nhưng không trừ.
8. **Bông tai:** số mảnh hồn cần khác lời tab: cấp 2 code = 200 (tab 99); cấp 3 code = 99 (tab 199); nâng lên cấp 3 cần 20.000 (tab 19999). Nâng cấp bông tai **xóa sạch chỉ số cũ** (cả lên cấp 2 lẫn cấp 3). `nangCapBongTai()` không kiểm tra lại số mảnh (chỉ kiểm ở bước hiển thị).
9. **Đổi sách tuyệt kỹ** tạo sách ngẫu nhiên giữa 3 hành tinh (1044/1211/1212), không theo hành tinh người chơi.
10. **Chức năng chết:** `LAM_PHEP_NHAP_DA` (menu có nhưng không xử lý), `PHAN_RA_DO_THAN_LINH`, `DUI_DUC`, `DA_MAI` (không có menu), `HocTuyetKy.java`, `CombineSystem.getRatioPhaLeHoa`, `CombineSystem.rollSuccess`, `RandomCollection`.
11. **Ép sao 100% và rẻ:** chỉ 10 ngọc/lần, không roll; Ngọc Rồng 1–7 sao cũng dùng được làm đá ép.
12. **Tạo đá Hematite:** lời NPC ghi "5 sao pha lê cấp 2" nhưng code dùng sao pha lê cấp 1 (441–447).
13. **NPE tiềm ẩn:** `NangCapVatPham` giả định trang bị luôn có 1 option chỉ số chính (47/6/0/7/14/22/23); nếu không có sẽ `NullPointerException` (dòng 87 & 202). `CheTaoTS` dùng `findFirst().get()` sẽ ném exception nếu thiếu đồ.
14. **Hành tinh gốc đồ LL/Jean/Zelot:** tên hàm trong code (`isDoJean`, `isDoZelot`) bị đảo so với tên item trong DB.
15. **Học tuyệt kỹ:** text menu luôn hiện "Giá ngọc: 99", `learnNewSkill`/`upgradeSkill` trừ vàng/ngọc bằng phép trừ trực tiếp sau khi đã kiểm tra đủ; nâng cấp thất bại vẫn mất 99 bí kíp.
