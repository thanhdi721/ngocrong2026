# 27 — Dữ liệu tuyến nhiệm vụ chính mới: bảng tra nhanh + đặc tả cho `TaskService`

> **Đây là bản đặc tả mà nhóm viết `TaskService` code theo.** Mọi id map / mob / boss / NPC /
> vật phẩm trong file này là id THẬT, đã đối chiếu với `database team2026.sql` và mã nguồn.
>
> File SQL đi kèm: [`SRC/sql/patch/02-nhiem-vu-moi.sql`](../../SRC/sql/patch/02-nhiem-vu-moi.sql)
> · [`SRC/sql/patch/03-reset-tien-do.sql`](../../SRC/sql/patch/03-reset-tien-do.sql)
> Thiết kế gốc: [20](../2-thiet-ke-nhiem-vu-moi/20-thiet-ke-nhiem-vu-moi.md) ·
> [20a](../2-thiet-ke-nhiem-vu-moi/20a-chi-tiet-nhiem-vu-00-15.md) ·
> [20b](../2-thiet-ke-nhiem-vu-moi/20b-chi-tiet-nhiem-vu-16-31.md) ·
> [20c](../2-thiet-ke-nhiem-vu-moi/20c-chi-tiet-nhiem-vu-32-47.md)
> Id vật phẩm CHỐT: [25](25-bang-id-vat-pham-moi.md) · Quyết định chủ dự án: [22 §0](22-san-sang-code.md)

## Mục lục

1. [Tóm tắt](#1-tóm-tắt)
2. [Quy ước đọc bảng](#2-quy-ước-đọc-bảng)
3. [Bảng tra nhanh 51 nhiệm vụ](#3-bảng-tra-nhanh-51-nhiệm-vụ)
4. [Bước ↔ điều kiện server phải kiểm tra](#4-bước--điều-kiện-server-phải-kiểm-tra)
5. [Bảng chuyển tiếp task id](#5-bảng-chuyển-tiếp-task-id)
6. [Trigger mới phải viết + điểm móc](#6-trigger-mới-phải-viết--điểm-móc)
7. [Thay đổi Java bắt buộc đi kèm dữ liệu](#7-thay-đổi-java-bắt-buộc-đi-kèm-dữ-liệu)
8. [Đường cong sức mạnh — kiểm chứng lại §6.1](#8-đường-cong-sức-mạnh--kiểm-chứng-lại-61)
9. [Chỗ nghi ngờ](#9-chỗ-nghi-ngờ)

---

## 1. Tóm tắt

| Chỉ số | Giá trị |
|---|---|
| Nhiệm vụ | **51** — id 0–47 tuyến chính + **48** (nhánh Jaco), **49** (nhánh Thu nhận bản sao), **50** (nhánh Giữ Lõi) |
| Bước con (`task_sub_template`) | **238** |
| Dòng bảng thưởng (`task_main_reward`) | **297** = 51 (hoàn thành nhiệm vụ) + 6 (hoàn thành, theo hành tinh: NV 3 và NV 10) + 237 (theo bước — 238 bước trừ `TASK_50_0` đã trả ở `TASK_47_0`) + 3 (bước `TASK_10_0`, theo hành tinh) |
| Vật phẩm mới dùng trong tuyến | 2000–2031 theo [bảng 25](25-bang-id-vat-pham-moi.md) — **không dùng id cũ của 20a/20b/20c** |
| Boss mới phải dựng | `-2000` Kẻ Thu Gom · `-2001` Jaco Mất Ký Ức (2 form) · `BanSaoNguoiChoi` · `-108108` Heart (4 form) |
| Tiền tệ trong thưởng | **0 vàng, 0 ngọc, 0 hồng ngọc, 0 Ngọc Rồng, 0 Thỏi vàng** |
| Điểm rẽ nhánh | 3 — tại NV 20, NV 31, NV 47 |
| Bước đếm giờ (B12) | 5 — TASK_7_0, TASK_23_2, TASK_29_2, TASK_34_2, TASK_42_2 |
| Bước làm cùng người khác (B13) | 5 — TASK_13_1, TASK_19_3, TASK_31_3 / TASK_49_3, TASK_35_1, TASK_45_2 |

---

## 2. Quy ước đọc bảng

| Cột | Ý nghĩa |
|---|---|
| **#** | `taskMain.index` — chỉ số bước trong `task_sub_template`, **đếm từ 0**; `TaskService` tra theo đúng thứ tự này |
| **Hằng** | `ConstTask.TASK_<id>_<index>`, giá trị `((id << 10) + index) << 1` = `(id × 1024 + index) × 2` |
| **Kiểu** | mã trigger theo [20 §4](../2-thiet-ke-nhiem-vu-moi/20-thiet-ke-nhiem-vu-moi.md#4-bộ-công-cụ-bước-nhiệm-vụ-trigger): A1–A12 đã có hàm, B1–B14 phải viết mới |
| **SL** | `task_sub_template.max_count` |
| **NPC / Map** | giá trị thật trong DB. Số âm là **placeholder theo hành tinh**, `transformNpcId` / `transformMapId` đổi lúc nạp |
| **Điều kiện server kiểm tra** | điều kiện chính xác `TaskService` phải kiểm — id mob / boss / npc / map / item thật |

**Placeholder map** (`task_sub_template.map`):
`-2` MAP_NHA = 21/22/23 · `-3` MAP_200 = 1/8/15 · `-4` MAP_VACH_NUI = **42/43/44** ⚠️ ·
`-5` MAP_500 = **2/9/16** ⚠️ · `-6` MAP_TTVT = 24/25/26 · `-7` MAP_QUAI_BAY_600 = 3/11/17 ·
`-8` MAP_LANG = 0/7/14 · `-9` MAP_QUY_LAO = 5/13/20 · `-1` = không chỉ đường.

**Placeholder NPC** (`task_sub_template.npc_id`):
`-2` NPC_NHA = 0/2/1 · `-3` NPC_TTVT = 10/11/12 · `-4` NPC_SHOP_LANG = 7/8/9 ·
`-5` NPC_QUY_LAO = 13/14/15 · `-1` = không có NPC.

**Placeholder văn bản** trong `NAME` / `detail` / `notify`: `%1` TEN_LANG · `%2` TEN_NPC_NHA ·
`%3` TEN_MAP_200 · `%4` TEN_QUAI_200 · `%5` TEN_VACH_NUI · `%6` TEN_MAP_500 ·
`%7` TEN_NPC_TTVT · `%9` TEN_QUAI_BAY_600 · `%10` TEN_NPC_QUY_LAO · `%11` TEN_MAP_QUY_LAO ·
`%12` TEN_QUAI_3000 · `%13` TEN_MAP_600 · `%14` TEN_QUAI_1000.

⚠️ Hai placeholder map đánh dấu **cần sửa `transformMapId` trước khi bật tuyến** — xem [§7](#7-thay-đổi-java-bắt-buộc-đi-kèm-dữ-liệu).

---

## 3. Bảng tra nhanh 51 nhiệm vụ

| NV | Ch | Tên | Bước | Hằng đầu | Hằng cuối | SM & TN | Thưởng vật phẩm khi hoàn thành |
|---|---|---|---|---|---|---|---|
| 0 | 1 | Người duy nhất còn nhớ | 5 | `TASK_0_0` = 0 | `TASK_0_4` = 8 | 2.000 | 5×Đậu thần cấp 1, 1×Gói 10 viên Capsule |
| 1 | 1 | Bài học của ông | 3 | `TASK_1_0` = 2048 | `TASK_1_2` = 2052 | 3.000 | 1×Rada cấp 1 |
| 2 | 1 | Vết nứt đầu tiên | 3 | `TASK_2_0` = 4096 | `TASK_2_2` = 4100 | 4.000 | 10×Đậu thần cấp 1 |
| 3 | 1 | Cảnh sát vũ trụ Jaco | 3 | `TASK_3_0` = 6144 | `TASK_3_2` = 6148 | 5.000 | — + đồ theo hành tinh |
| 4 | 1 | Thứ bò ra từ vết nứt | 3 | `TASK_4_0` = 8192 | `TASK_4_2` = 8196 | 6.000 | 2×Gói 10 viên Capsule |
| 5 | 1 | Ký ức của ông | 3 | `TASK_5_0` = 10240 | `TASK_5_2` = 10244 | 8.000 | 1×Rada cấp 2, 10×Đậu thần cấp 1 |
| 6 | 1 | Người thu gom | 3 | `TASK_6_0` = 12288 | `TASK_6_2` = 12292 | 12.000 | 1×Gói 30 đậu thần cấp 3 |
| 7 | 1 | Chạy khỏi vết nứt | 3 | `TASK_7_0` = 14336 | `TASK_7_2` = 14340 | 20.000 | 1×Mảnh Ký Ức 1, 75×Gói 10 viên Capsule |
| 8 | 2 | Máy dò ký ức | 3 | `TASK_8_0` = 16384 | `TASK_8_2` = 16388 | 50.000 | 1×Máy Dò Ký Ức, 1×Gói 30 đậu thần cấp 3 |
| 9 | 2 | Chuyến bay đầu tiên | 3 | `TASK_9_0` = 18432 | `TASK_9_2` = 18436 | 70.000 | 1×Rada cấp 3 |
| 10 | 2 | Bái sư | 3 | `TASK_10_0` = 20480 | `TASK_10_2` = 20484 | 100.000 | — + đồ theo hành tinh |
| 11 | 2 | Hạt giống hy vọng | 3 | `TASK_11_0` = 22528 | `TASK_11_2` = 22532 | 140.000 | 1×Gói 30 đậu thần cấp 3 |
| 12 | 2 | Bạn đồng hành | 3 | `TASK_12_0` = 24576 | `TASK_12_2` = 24580 | 200.000 | 1×Đổi đệ tử, 1×Nâng kỹ năng 1 đệ tử |
| 13 | 2 | Không ai đi một mình | 3 | `TASK_13_0` = 26624 | `TASK_13_2` = 26628 | 280.000 | 2×Gói 30 đậu thần cấp 3, 2×Đá bảo vệ |
| 14 | 2 | Chợ đen ký ức | 3 | `TASK_14_0` = 28672 | `TASK_14_2` = 28676 | 400.000 | 1×Hộp Ký Ức Bị Đánh Cắp, 10×Đá nâng cấp cấp 1, 3×Đá bảo vệ |
| 15 | 2 | Người bạn đã quên | 3 | `TASK_15_0` = 30720 | `TASK_15_2` = 30724 | 600.000 | 1×Mảnh Ký Ức 2, 1×Gói 30 đậu thần cấp 3, 5×Đá nâng cấp cấp 1 |
| 16 | 3 | Dấu vết dẫn về phía Nam | 5 | `TASK_16_0` = 32768 | `TASK_16_4` = 32776 | 3.000.000 | 20×Đá nâng cấp cấp 1 |
| 17 | 3 | Rèn lại vũ khí | 4 | `TASK_17_0` = 34816 | `TASK_17_3` = 34822 | 5.000.000 | 30×Đá nâng cấp cấp 1, 3×Đá bảo vệ |
| 18 | 3 | Tapion | 5 | `TASK_18_0` = 36864 | `TASK_18_4` = 36872 | 6.000.000 | 20×Đá nâng cấp cấp 2 |
| 19 | 3 | Trại lính hoang | 5 | `TASK_19_0` = 38912 | `TASK_19_4` = 38920 | 7.000.000 | 30×Đá nâng cấp cấp 2 |
| 20 | 3 | Kẻ săn tiền thưởng | 6 | `TASK_20_0` = 40960 | `TASK_20_5` = 40970 | 8.000.000 | 40×Đá nâng cấp cấp 2 |
| 21 | 3 | Doanh trại Độc Nhãn | 4 | `TASK_21_0` = 43008 | `TASK_21_3` = 43014 | 9.000.000 | 20×Đá nâng cấp cấp 3, 2×Bản đồ kho báu |
| 22 | 3 | Tiểu đội sát thủ | 5 | `TASK_22_0` = 45056 | `TASK_22_4` = 45064 | 11.000.000 | 30×Đá nâng cấp cấp 3 |
| 23 | 3 | Fide đại ca | 6 | `TASK_23_0` = 47104 | `TASK_23_5` = 47114 | 16.000.000 | 50×Đá nâng cấp cấp 3, 5×Đá bảo vệ, 1×Mảnh Ký Ức 3 |
| 24 | 4 | Tín hiệu lạ từ phương Bắc | 5 | `TASK_24_0` = 49152 | `TASK_24_4` = 49160 | 80.000.000 | 20×Đá nâng cấp cấp 4 |
| 25 | 4 | Android đầu tiên | 4 | `TASK_25_0` = 51200 | `TASK_25_3` = 51206 | 100.000.000 | 30×Đá nâng cấp cấp 4 |
| 26 | 4 | Kim loại và ký ức | 5 | `TASK_26_0` = 53248 | `TASK_26_4` = 53256 | 130.000.000 | 2×Sao pha lê lục, 3×Đá ngũ sắc |
| 27 | 4 | Ba cỗ máy | 4 | `TASK_27_0` = 55296 | `TASK_27_3` = 55302 | 150.000.000 | 40×Đá nâng cấp cấp 4 |
| 28 | 4 | King Kong | 5 | `TASK_28_0` = 57344 | `TASK_28_4` = 57352 | 180.000.000 | 20×Đá nâng cấp cấp 5 |
| 29 | 4 | Phòng thí nghiệm Myuu | 4 | `TASK_29_0` = 59392 | `TASK_29_3` = 59398 | 200.000.000 | 25×Đá nâng cấp cấp 5, 5×Đá bảo vệ |
| 30 | 4 | Xên bọ hung | 5 | `TASK_30_0` = 61440 | `TASK_30_4` = 61448 | 220.000.000 | 30×Đá nâng cấp cấp 5, 2×Sao pha lê vàng |
| 31 | 4 | Bản sao của chính ngươi | 6 | `TASK_31_0` = 63488 | `TASK_31_5` = 63498 | 230.000.000 | 50×Đá nâng cấp cấp 5, 2×Sao pha lê cam, 1×Mảnh Ký Ức 4 |
| 32 | 5 | Lời cảnh báo của Bardock | 6 | `TASK_32_0` = 65536 | `TASK_32_5` = 65546 | 200.000.000 | 30×Đậu thần cấp 8 |
| 33 | 5 | Phá vỡ giới hạn | 6 | `TASK_33_0` = 67584 | `TASK_33_5` = 67594 | 250.000.000 | 50×Đậu thần cấp 8, 5×Đá bảo vệ |
| 34 | 5 | Vùng đất băng giá | 6 | `TASK_34_0` = 69632 | `TASK_34_5` = 69642 | 300.000.000 | 1×Bông tai Porata |
| 35 | 5 | Con đường rắn độc | 5 | `TASK_35_0` = 71680 | `TASK_35_4` = 71688 | 350.000.000 | 5×Đá nâng cấp cấp 5 |
| 36 | 5 | Cổng phi thuyền | 5 | `TASK_36_0` = 73728 | `TASK_36_4` = 73736 | 400.000.000 | 50×Đậu thần cấp 8 |
| 37 | 5 | Mabư | 5 | `TASK_37_0` = 75776 | `TASK_37_4` = 75784 | 450.000.000 | 1×Bông tai Porata |
| 38 | 5 | Black Goku | 6 | `TASK_38_0` = 77824 | `TASK_38_5` = 77834 | 550.000.000 | 2×Sao pha lê lục |
| 39 | 5 | Cái giá của ký ức | 7 | `TASK_39_0` = 79872 | `TASK_39_6` = 79884 | 1.000.000.000 | 1×Bông tai Porata, 50×Đậu thần cấp 8 |
| 40 | 6 | Tổ Sư Kaio | 5 | `TASK_40_0` = 81920 | `TASK_40_4` = 81928 | 550.000.000 | 150×Mảnh áo, 50×Đậu thần cấp 8 |
| 41 | 6 | Cơn thịnh nộ Broly | 5 | `TASK_41_0` = 83968 | `TASK_41_4` = 83976 | 650.000.000 | 150×Mảnh quần, 50×Đậu thần cấp 8 |
| 42 | 6 | Hành tinh ngục tù | 6 | `TASK_42_0` = 86016 | `TASK_42_5` = 86026 | 750.000.000 | 150×Mảnh găng tay, 50×Đậu thần cấp 8 |
| 43 | 6 | Khí gas hủy diệt | 6 | `TASK_43_0` = 88064 | `TASK_43_5` = 88074 | 850.000.000 | 150×Mảnh giầy, 50×Đậu thần cấp 8 |
| 44 | 6 | Thử thách của Thần Hủy Diệt | 6 | `TASK_44_0` = 90112 | `TASK_44_5` = 90122 | 900.000.000 | 150×Mảnh nhẫn, 50×Đậu thần cấp 8 |
| 45 | 6 | Bảy mảnh hợp nhất | 5 | `TASK_45_0` = 92160 | `TASK_45_4` = 92168 | 950.000.000 | 5×Đá ngũ sắc, 50×Đậu thần cấp 8 |
| 46 | 6 | Heart | 6 | `TASK_46_0` = 94208 | `TASK_46_5` = 94218 | 1.100.000.000 | 5×Đá ngũ sắc, 10×Đá bảo vệ, 100×Mảnh áo, 100×Mảnh quần, 100×Mảnh giầy, 100×Mảnh nhẫn, 100×Mảnh găng tay |
| 47 | 6 | Trả lại hay giữ lấy | 6 | `TASK_47_0` = 96256 | `TASK_47_5` = 96266 | 1.500.000.000 | 1×Người Trả Ký Ức, 150×Mảnh áo, 150×Mảnh quần, 150×Mảnh giầy, 150×Mảnh nhẫn, 150×Mảnh găng tay, 20×Đá bảo vệ, 99×Đậu thần cấp 8 |
| 48 | 3 | Kẻ săn tiền thưởng | 6 | `TASK_48_0` = 98304 | `TASK_48_5` = 98314 | 8.000.000 | 20×Đá nâng cấp cấp 1, 8×Đá bảo vệ |
| 49 | 4 | Bản sao của chính ngươi | 7 | `TASK_49_0` = 100352 | `TASK_49_6` = 100364 | 230.000.000 | 3×Sao pha lê lục, 5×Đá ngũ sắc, 10×Đá bảo vệ, 1×Mảnh Ký Ức 4 |
| 50 | 6 | Trả lại hay giữ lấy | 6 | `TASK_50_0` = 102400 | `TASK_50_5` = 102410 | 1.500.000.000 | 1×Kẻ Giữ Hư Không, 150×Mảnh áo, 150×Mảnh quần, 150×Mảnh giầy, 150×Mảnh nhẫn, 150×Mảnh găng tay, 20×Đá bảo vệ, 99×Đậu thần cấp 8 |

> Thưởng **mỗi bước** = 10% mức thưởng của cả nhiệm vụ (§6.3 của file 20), trả qua `addDoneSubTask`.
> Riêng `TASK_50_0` không có dòng thưởng — bước đó đã được trả ở `TASK_47_0` trước khi rẽ nhánh.

### 3.1 Vật phẩm trao giữa chừng (`task_main_reward` với `sub_index >= 0`)

| Bước | Vật phẩm trao | Vì sao trao sớm |
|---|---|---|
| `TASK_10_0` | 1 Sách chưởng cấp 1 theo hành tinh (94 / 101 / 108) | để có sách mà học ở bước 1 (B9) |
| `TASK_11_0` | 1 Hạt Giống Hy Vọng (2013) | để gieo ở bước 1 (A12) |
| `TASK_17_0` | 10 Đá Titan (223), 10 Đá Ruby (222), 2 Đá bảo vệ (987), 1 Búa rèn cũ (2015) | nguyên liệu miễn phí cho bước B2 và A12 |
| `TASK_26_0` | 6 Đá ngũ sắc (674), 2 Sao pha lê lục (447), 1 Mẫu kim loại có ký ức (2020) | nguyên liệu cho hai bước B3 và bước A12 |
| `TASK_29_0` | 1 Thẻ từ phòng thí nghiệm (2022) | điều kiện vào map 166 |
| `TASK_32_0` | 1 Nhẫn thời không sai lệch (992) | phương tiện tới map 160 (A12 bước 1) |
| `TASK_36_0` | 1 Bình hút năng lượng (1795) | đường vòng map 165 khi ngoài khung 12h |
| `TASK_39_3` | 1 Mảnh Ký Ức 5 (2006) | NPC 29 Rồng Omega trao |
| `TASK_39_5` | 1 Mảnh Ký Ức 6 (2007) | NPC 70 Bardock trao |
| `TASK_45_1` | 1 Lõi Ký Ức chưa hoàn chỉnh (2024) | để "dùng" ở bước 3 |
| `TASK_45_3` | 1 Lõi Hư Không (2000) | sản phẩm của lễ hợp nhất, sau khi trừ 7 mảnh 2002–2008 + 2024 |
| `TASK_47_1` | 1 Vỏ Lõi rỗng (2001) | kỷ vật nhánh A, sau khi trừ 2000 |
| `TASK_31_1` → nhánh 49 | 1 Bình chứa Commeson (638) **bản không hạn sử dụng** | do `switchTaskBranch` trao, **không** nằm trong bảng thưởng vì bước `TASK_49_1` không bao giờ chạy qua `addDoneSubTask` |

---

## 4. Bước ↔ điều kiện server phải kiểm tra

### Chương 1 — Ngày ký ức vỡ

#### NV 0 — Người duy nhất còn nhớ  *(chương 1, 5 bước, thưởng 2.000 SM & TN)*

| # | Hằng | Kiểu | Tên bước | SL | NPC | Map | Điều kiện server kiểm tra |
|---|---|---|---|---|---|---|---|
| 0 | `TASK_0_0` = 0 | **A6** | Đi về nhà %2 | 1 | — | -2 MAP_NHA | checkDoneTaskGoToMap: zone.map.mapId ∈ {21,22,23} (MAP_NHA = 21 + gender) |
| 1 | `TASK_0_1` = 2 | **A8** | Lấy đồ trong rương | 1 | 3 | -2 MAP_NHA | checkDoneTaskGetItemBox: UseItem nhánh ITEM_BOX_TO_BODY_OR_BAG chuyển >=1 item ra khỏi rương (NPC 3) |
| 2 | `TASK_0_2` = 4 | **A3** | Nói chuyện với %2 | 1 | -2 NPC_NHA | -2 MAP_NHA | checkDoneTaskTalkNpc: npc.tempId == 0 / 2 / 1 ĐÚNG theo gender (NPC_NHA) |
| 3 | `TASK_0_3` = 6 | **A9** | Xem cây đậu thần | 1 | 4 | -2 MAP_NHA | checkDoneTaskConfirmMenuNpc: npc.tempId == 4 (Đậu thần), người chơi chọn 1 mục menu |
| 4 | `TASK_0_4` = 8 | **A12** | Ăn một hạt Đậu thần cấp 1 | 1 | — | -2 MAP_NHA | checkDoneTaskUseItem: item.template.id == 13 |

#### NV 1 — Bài học của ông  *(chương 1, 3 bước, thưởng 3.000 SM & TN)*

| # | Hằng | Kiểu | Tên bước | SL | NPC | Map | Điều kiện server kiểm tra |
|---|---|---|---|---|---|---|---|
| 0 | `TASK_1_0` = 2048 | **A1** | Đập vỡ 10 mộc nhân ở %1 | 10 | — | -8 MAP_LANG | checkDoneTaskKillMob: mob.tempId == 0 (Mộc nhân) tại map 0/7/14 |
| 1 | `TASK_1_1` = 2050 | **A3** | Về khoe với %2 | 1 | -2 NPC_NHA | -2 MAP_NHA | checkDoneTaskTalkNpc: npc.tempId ∈ {0,2,1} đúng gender |
| 2 | `TASK_1_2` = 2052 | **A7** | Cộng điểm tiềm năng lần đầu | 1 | — | — | checkDoneTaskUseTiemNang: gọi từ NPoint.doUseTiemNang (cộng bất kỳ chỉ số nào) |

#### NV 2 — Vết nứt đầu tiên  *(chương 1, 3 bước, thưởng 4.000 SM & TN)*

| # | Hằng | Kiểu | Tên bước | SL | NPC | Map | Điều kiện server kiểm tra |
|---|---|---|---|---|---|---|---|
| 0 | `TASK_2_0` = 4096 | **A6** | Lên %3 xem chuyện gì xảy ra | 1 | — | -3 MAP_200 | checkDoneTaskGoToMap: mapId ∈ {1,8,15} (MAP_200) |
| 1 | `TASK_2_1` = 4098 | **A1** | Tiêu diệt 20 %4 đang phát điên | 20 | — | -3 MAP_200 | checkDoneTaskKillMob: mob.tempId == 1 / 2 / 3 |
| 2 | `TASK_2_2` = 4100 | **A4** | Nhặt Mảnh Vỡ Hư Không | 1 | — | -3 MAP_200 | checkDoneTaskPickItem: itemMap template 2009; Mob.dropItemTask rơi 100% từ mob 1/2/3 khi isCurrentTask(TASK_2_2) |

#### NV 3 — Cảnh sát vũ trụ Jaco  *(chương 1, 3 bước, thưởng 5.000 SM & TN)*

| # | Hằng | Kiểu | Tên bước | SL | NPC | Map | Điều kiện server kiểm tra |
|---|---|---|---|---|---|---|---|
| 0 | `TASK_3_0` = 6144 | **A3** | Gặp Jaco ở %5 | 1 | 63 | -4 MAP_VACH_NUI | checkDoneTaskTalkNpc: npc.tempId == 63 (Jaco) VÀ mapId ∈ {42,43,44} |
| 1 | `TASK_3_1` = 6146 | **A1** | Cho Jaco xem ngươi đánh 20 %4 | 20 | — | -3 MAP_200 | checkDoneTaskKillMob: mob.tempId == 1 / 2 / 3 |
| 2 | `TASK_3_2` = 6148 | **A11** | Nâng sức đánh gốc lên 30 | 1 | — | — | checkDoneTaskNangCS: gọi từ NPoint.increasePoint(type 2), điều kiện nPoint.dameg >= 30 |

#### NV 4 — Thứ bò ra từ vết nứt  *(chương 1, 3 bước, thưởng 6.000 SM & TN)*

| # | Hằng | Kiểu | Tên bước | SL | NPC | Map | Điều kiện server kiểm tra |
|---|---|---|---|---|---|---|---|
| 0 | `TASK_4_0` = 8192 | **A1** | Dọn đường vào %6: hạ 12 %4 | 12 | — | -5 MAP_500 | checkDoneTaskKillMob: mob.tempId == 1 / 2 / 3 tại map 2/9/16 |
| 1 | `TASK_4_1` = 8194 | **A1** | Hạ 15 quái mẹ biến dạng | 15 | — | -5 MAP_500 | checkDoneTaskKillMob: mob.tempId == 4 / 5 / 6 tại map 2/9/16 |
| 2 | `TASK_4_2` = 8196 | **A3** | Kể lại cho %2 | 1 | -2 NPC_NHA | -2 MAP_NHA | checkDoneTaskTalkNpc: npc.tempId ∈ {0,2,1} đúng gender |

#### NV 5 — Ký ức của ông  *(chương 1, 3 bước, thưởng 8.000 SM & TN)*

| # | Hằng | Kiểu | Tên bước | SL | NPC | Map | Điều kiện server kiểm tra |
|---|---|---|---|---|---|---|---|
| 0 | `TASK_5_0` = 10240 | **A4** | Tìm Kỷ Vật Của Ông trong %13 | 1 | — | -7 MAP_QUAI_BAY_600 | checkDoneTaskPickItem: template 2010; Mob.dropItemTask rơi từ mob 7/8/9 khi isCurrentTask(TASK_5_0) |
| 1 | `TASK_5_1` = 10242 | **A12** | Lau sạch kỷ vật | 1 | — | — | checkDoneTaskUseItem: template 2010 — KHÔNG trừ item ở bước này |
| 2 | `TASK_5_2` = 10244 | **A3** | Đưa kỷ vật cho %2 | 1 | -2 NPC_NHA | -2 MAP_NHA | checkDoneTaskTalkNpc: npc.tempId ∈ {0,2,1} đúng gender; switch(TASK_5_2) trừ 1 item 2010 |

#### NV 6 — Người thu gom  *(chương 1, 3 bước, thưởng 12.000 SM & TN)*

| # | Hằng | Kiểu | Tên bước | SL | NPC | Map | Điều kiện server kiểm tra |
|---|---|---|---|---|---|---|---|
| 0 | `TASK_6_0` = 12288 | **A1** | Lần theo tiếng rít: hạ 15 %9 | 15 | — | — | checkDoneTaskKillMob: mob.tempId == 7 / 8 / 9 tại map 4/12/18 |
| 1 | `TASK_6_1` = 12290 | **A2** | Hạ Kẻ Thu Gom | 1 | — | — | checkDoneTaskKillBoss: boss.id == -2000 (BOSS MỚI, mapJoin {4,12,18}) |
| 2 | `TASK_6_2` = 12292 | **A3** | Báo lại cho %2 | 1 | -2 NPC_NHA | -2 MAP_NHA | checkDoneTaskTalkNpc: npc.tempId ∈ {0,2,1} đúng gender |

#### NV 7 — Chạy khỏi vết nứt  *(chương 1, 3 bước, thưởng 20.000 SM & TN)*

| # | Hằng | Kiểu | Tên bước | SL | NPC | Map | Điều kiện server kiểm tra |
|---|---|---|---|---|---|---|---|
| 0 | `TASK_7_0` = 14336 | **B12** | Chặt 10 Vòi Hư Không trong 3 phút | 10 | — | — | checkDoneTaskKillMob mob.tempId == 10/11/12 tại map 4/12/18 + đồng hồ 180.000 ms tính từ TaskMain.lastTime; hết giờ -> count = 0, lastTime = now |
| 1 | `TASK_7_1` = 14338 | **A6** | Chạy tới Trạm tàu vũ trụ | 1 | -3 NPC_TTVT | -6 MAP_TTVT | checkDoneTaskGoToMap: mapId ∈ {24,25,26} (MAP_TTVT) |
| 2 | `TASK_7_2` = 14340 | **A3** | Nói chuyện với Jaco | 1 | 63 | -6 MAP_TTVT | checkDoneTaskTalkNpc: npc.tempId == 63 VÀ mapId ∈ {24,25,26} |

### Chương 2 — Kẻ trộm ký ức

#### NV 8 — Máy dò ký ức  *(chương 2, 3 bước, thưởng 50.000 SM & TN)*

| # | Hằng | Kiểu | Tên bước | SL | NPC | Map | Điều kiện server kiểm tra |
|---|---|---|---|---|---|---|---|
| 0 | `TASK_8_0` = 16384 | **A3** | Gặp Bunma ở Siêu Thị | 1 | 7 | 84 | checkDoneTaskTalkNpc: npc.tempId == 7 (Bunma) VÀ mapId == 84 |
| 1 | `TASK_8_1` = 16386 | **B4** | Mua 1 Rada cấp 1 ở shop | 1 | -4 NPC_SHOP_LANG | 84 | checkDoneTaskBuyItem(player, 12, shopId) — shopId ∈ {1,2,3} (BUNMA/DENDE/APPULE), gọi sau khi trừ tiền + addItemBag thành công |
| 2 | `TASK_8_2` = 16388 | **A1** | Moi 25 lõi cảm biến từ quái mẹ | 25 | — | — | checkDoneTaskKillMob: mob.tempId == 10 / 11 / 12 tại map 4/12/18 |

#### NV 9 — Chuyến bay đầu tiên  *(chương 2, 3 bước, thưởng 70.000 SM & TN)*

| # | Hằng | Kiểu | Tên bước | SL | NPC | Map | Điều kiện server kiểm tra |
|---|---|---|---|---|---|---|---|
| 0 | `TASK_9_0` = 18432 | **A6** | Bay tới %11 | 1 | — | -9 MAP_QUY_LAO | checkDoneTaskGoToMap: mapId ∈ {5,13,20} (MAP_QUY_LAO) |
| 1 | `TASK_9_1` = 18434 | **A1** | Dọn sạch 20 %12 quanh nhà sư phụ | 20 | — | -9 MAP_QUY_LAO | checkDoneTaskKillMob: mob.tempId == 13 / 14 / 15 |
| 2 | `TASK_9_2` = 18436 | **A3** | Nói chuyện với %10 | 1 | -5 NPC_QUY_LAO | -9 MAP_QUY_LAO | checkDoneTaskTalkNpc: npc.tempId == 13 + player.gender (BẮT BUỘC kiểm tra gender) |

#### NV 10 — Bái sư  *(chương 2, 3 bước, thưởng 100.000 SM & TN)*

| # | Hằng | Kiểu | Tên bước | SL | NPC | Map | Điều kiện server kiểm tra |
|---|---|---|---|---|---|---|---|
| 0 | `TASK_10_0` = 20480 | **A3** | Xin %10 nhận làm đệ tử | 1 | -5 NPC_QUY_LAO | -9 MAP_QUY_LAO | checkDoneTaskTalkNpc: npc.tempId == 13 + gender; switch(TASK_10_0) TRAO sách chưởng theo hành tinh |
| 1 | `TASK_10_1` = 20482 | **B9** | Học chưởng cấp 1 | 1 | -5 NPC_QUY_LAO | -9 MAP_QUY_LAO | checkDoneTaskLearnSkill: skillId == 1 Kamejoko / 3 Masenko / 5 Antomic VÀ skill.point >= 1 |
| 2 | `TASK_10_2` = 20484 | **A5** | Đạt 250.000 sức mạnh | 1 | — | — | checkDoneTaskPower: power >= 250.000 (gọi từ NPoint.powerUp + kiểm lại trong sendNextSubTask) |

#### NV 11 — Hạt giống hy vọng  *(chương 2, 3 bước, thưởng 140.000 SM & TN)*

| # | Hằng | Kiểu | Tên bước | SL | NPC | Map | Điều kiện server kiểm tra |
|---|---|---|---|---|---|---|---|
| 0 | `TASK_11_0` = 22528 | **B7** | Thu hoạch 5 hạt đậu thần | 5 | 4 | -2 MAP_NHA | checkDoneTaskHarvestPea(player, soHat): gọi từ MagicTree.harvestPea sau addPeaHarvest, cộng dồn addDoneSubTask(player, soHat) |
| 1 | `TASK_11_1` = 22530 | **A12** | Gieo Hạt Giống Hy Vọng | 1 | 4 | -2 MAP_NHA | checkDoneTaskUseItem: template 2013 VÀ mapId ∈ {21,22,23}; nâng magicTree.level lên 2 rồi trừ item |
| 2 | `TASK_11_2` = 22532 | **A3** | Khoe cây mới với %2 | 1 | -2 NPC_NHA | -2 MAP_NHA | checkDoneTaskTalkNpc: npc.tempId ∈ {0,2,1} đúng gender |

#### NV 12 — Bạn đồng hành  *(chương 2, 3 bước, thưởng 200.000 SM & TN)*

| # | Hằng | Kiểu | Tên bước | SL | NPC | Map | Điều kiện server kiểm tra |
|---|---|---|---|---|---|---|---|
| 0 | `TASK_12_0` = 24576 | **B8** | Nhận đệ tử đầu tiên | 1 | 50 | -2 MAP_NHA | checkDoneTaskHavePet: player.pet != null — gọi sau PetService.createNewPet VÀ trong Player.update; NPC 50 Quả trứng mở menu 'Nở trứng' khi getIdTask == TASK_12_0 và pet == null |
| 1 | `TASK_12_1` = 24578 | **A1** | Cùng đệ tử diệt 25 quái mẹ | 25 | — | — | checkDoneTaskKillMob: mob.tempId == 10 / 11 / 12 |
| 2 | `TASK_12_2` = 24580 | **A3** | Giới thiệu đệ tử với %2 | 1 | -2 NPC_NHA | -2 MAP_NHA | checkDoneTaskTalkNpc: npc.tempId ∈ {0,2,1} đúng gender |

#### NV 13 — Không ai đi một mình  *(chương 2, 3 bước, thưởng 280.000 SM & TN)*

| # | Hằng | Kiểu | Tên bước | SL | NPC | Map | Điều kiện server kiểm tra |
|---|---|---|---|---|---|---|---|
| 0 | `TASK_13_0` = 26624 | **A10** | Gia nhập một bang hội | 1 | — | — | checkDoneTaskJoinClan: gọi từ ClanService khi player.clan != null — TUYỆT ĐỐI không để TASK_13_0 trong bảng checkDoneTaskTalkNpc (lỗi cũ) |
| 1 | `TASK_13_1` = 26626 | **B13** | Cùng bạn cùng bang diệt 30 quái mẹ | 30 | — | — | checkDoneTaskKillMob mob.tempId == 10/11/12 VÀ player.clan != null VÀ zone có >= 2 người cùng clan.id; >= 3 người thì mỗi mạng tính x2 |
| 2 | `TASK_13_2` = 26628 | **A3** | Gặp Giu-ma Đầu Bò | 1 | 47 | 153 | checkDoneTaskTalkNpc: npc.tempId == 47 VÀ mapId == 153 |

#### NV 14 — Chợ đen ký ức  *(chương 2, 3 bước, thưởng 400.000 SM & TN)*

| # | Hằng | Kiểu | Tên bước | SL | NPC | Map | Điều kiện server kiểm tra |
|---|---|---|---|---|---|---|---|
| 0 | `TASK_14_0` = 28672 | **A3** | Nghe Bunma báo tin ở Nhà Bunma | 1 | 37 | 102 | checkDoneTaskTalkNpc: npc.tempId == 37 (Bunma tương lai) VÀ mapId == 102 |
| 1 | `TASK_14_1` = 28674 | **B4** | Mua một món hàng ở quầy Uron | 1 | 16 | 84 | checkDoneTaskBuyItem(player, bất kỳ itemId, shopId == 4 URON) tại map 84 |
| 2 | `TASK_14_2` = 28676 | **A1** | Chặn 20 con thú tải hàng | 20 | — | — | checkDoneTaskKillMob: mob.tempId == 16 / 17 / 18 tại map 27/31/35 |

#### NV 15 — Người bạn đã quên  *(chương 2, 3 bước, thưởng 600.000 SM & TN)*

| # | Hằng | Kiểu | Tên bước | SL | NPC | Map | Điều kiện server kiểm tra |
|---|---|---|---|---|---|---|---|
| 0 | `TASK_15_0` = 30720 | **A1** | Tới điểm hẹn: hạ 20 quái mẹ | 20 | — | — | checkDoneTaskKillMob: mob.tempId == 10 / 11 / 12 tại map 27/31/35 |
| 1 | `TASK_15_1` = 30722 | **A2** | Đánh thức Jaco | 1 | — | — | checkDoneTaskKillBoss: boss.id == -2001 VÀ boss.currentLevel == 1 (form cuối 'Jaco Vô Thức') |
| 2 | `TASK_15_2` = 30724 | **A3** | Gặp lại Jaco ở Trạm tàu vũ trụ | 1 | 63 | -6 MAP_TTVT | checkDoneTaskTalkNpc: npc.tempId == 63 VÀ mapId ∈ {24,25,26} |

### Chương 3 — Liên minh ba hành tinh

#### NV 16 — Dấu vết dẫn về phía Nam  *(chương 3, 5 bước, thưởng 3.000.000 SM & TN)*

| # | Hằng | Kiểu | Tên bước | SL | NPC | Map | Điều kiện server kiểm tra |
|---|---|---|---|---|---|---|---|
| 0 | `TASK_16_0` = 32768 | **A6** | Lần theo tín hiệu về phía Nam | 1 | — | — | checkDoneTaskGoToMap: mapId == 29 (TĐ) / 33 (NM) / 37 (XD) |
| 1 | `TASK_16_1` = 32770 | **A1** | Dọn sạch 40 quái chắn đường | 40 | — | — | checkDoneTaskKillMob: mob.tempId == 31 / 32 / 33 tại map 29/33/37 |
| 2 | `TASK_16_2` = 32772 | **A1** | Tiêu diệt 30 quái canh bờ biển | 30 | — | — | checkDoneTaskKillMob: mob.tempId == 22 / 23 / 24 tại map 30/34/38 |
| 3 | `TASK_16_3` = 32774 | **A4** | Nhặt 5 Vỏ đạn khắc dấu | 5 | — | — | checkDoneTaskPickItem: template 2014; rơi 25% từ mob 22/23/24 khi isCurrentTask(TASK_16_3) |
| 4 | `TASK_16_4` = 32776 | **A3** | Mang vỏ đạn về cho %10 | 1 | -5 NPC_QUY_LAO | -9 MAP_QUY_LAO | checkDoneTaskTalkNpc: npc.tempId == 13 + gender |

#### NV 17 — Rèn lại vũ khí  *(chương 3, 4 bước, thưởng 5.000.000 SM & TN)*

| # | Hằng | Kiểu | Tên bước | SL | NPC | Map | Điều kiện server kiểm tra |
|---|---|---|---|---|---|---|---|
| 0 | `TASK_17_0` = 34816 | **A3** | Gặp Bà Hạt Mít ở %5 | 1 | 21 | -4 MAP_VACH_NUI | checkDoneTaskTalkNpc: npc.tempId == 21 VÀ mapId ∈ {42,43,44}; switch TRAO nguyên liệu miễn phí |
| 1 | `TASK_17_1` = 34818 | **B2** | Nâng một trang bị lên +2 | 1 | 21 | -4 MAP_VACH_NUI | checkDoneTaskUpgradeItem: móc ở combine/NangCapVatPham nhánh thành công, điều kiện item.getOptionParam(72) >= 2 |
| 2 | `TASK_17_2` = 34820 | **A12** | Dùng Búa rèn cũ | 1 | — | — | checkDoneTaskUseItem: template 2015 (trừ item sau khi xong) |
| 3 | `TASK_17_3` = 34822 | **A3** | Khoe vũ khí mới với %10 | 1 | -5 NPC_QUY_LAO | -9 MAP_QUY_LAO | checkDoneTaskTalkNpc: npc.tempId == 13 + gender |

#### NV 18 — Tapion  *(chương 3, 5 bước, thưởng 6.000.000 SM & TN)*

| # | Hằng | Kiểu | Tên bước | SL | NPC | Map | Điều kiện server kiểm tra |
|---|---|---|---|---|---|---|---|
| 0 | `TASK_18_0` = 36864 | **A6** | Tới Thành phố Vegeta | 1 | — | 19 | checkDoneTaskGoToMap: mapId == 19 |
| 1 | `TASK_18_1` = 36866 | **A3** | Nói chuyện với người lạ thổi nhạc | 1 | 53 | 19 | checkDoneTaskTalkNpc: npc.tempId == 53 (Tapion) VÀ mapId == 19 |
| 2 | `TASK_18_2` = 36868 | **A1** | Diệt 30 quái đang vây thành | 30 | — | — | checkDoneTaskKillMob: mob.tempId == 25 / 26 / 27 tại map 6/10/19 |
| 3 | `TASK_18_3` = 36870 | **A6** | Đi cùng Tapion tới Thành phố Santa | 1 | — | 126 | checkDoneTaskGoToMap: mapId == 126 |
| 4 | `TASK_18_4` = 36872 | **A3** | Nghe Tapion kể chuyện | 1 | 53 | 126 | checkDoneTaskTalkNpc: npc.tempId == 53 VÀ mapId == 126 |

#### NV 19 — Trại lính hoang  *(chương 3, 5 bước, thưởng 7.000.000 SM & TN)*

| # | Hằng | Kiểu | Tên bước | SL | NPC | Map | Điều kiện server kiểm tra |
|---|---|---|---|---|---|---|---|
| 0 | `TASK_19_0` = 38912 | **A3** | Gặp Cui ở Thung lũng Nappa | 1 | 12 | 68 | checkDoneTaskTalkNpc: npc.tempId == 12 (Cui) VÀ mapId == 68 |
| 1 | `TASK_19_1` = 38914 | **A1** | Hạ 60 Nappa mất trí | 60 | — | 68 | checkDoneTaskKillMob: mob.tempId == 39 tại map 68/69/70 |
| 2 | `TASK_19_2` = 38916 | **A1** | Hạ 40 Soldier gác kho | 40 | — | 69 | checkDoneTaskKillMob: mob.tempId == 40 tại map 69/70 |
| 3 | `TASK_19_3` = 38918 | **B13** | Cùng người khác hạ 30 Appule | 30 | — | 71 | checkDoneTaskKillMob mob.tempId == 41 tại map 71/72 VÀ zone có >= 2 Player thật; đủ điều kiện thì addDoneSubTask(player, 2) |
| 4 | `TASK_19_4` = 38920 | **A3** | Báo cáo với Cui | 1 | 12 | 68 | checkDoneTaskTalkNpc: npc.tempId == 12 VÀ mapId == 68 |

#### NV 20 — Kẻ săn tiền thưởng  *(chương 3, 6 bước, thưởng 8.000.000 SM & TN)*

| # | Hằng | Kiểu | Tên bước | SL | NPC | Map | Điều kiện server kiểm tra |
|---|---|---|---|---|---|---|---|
| 0 | `TASK_20_0` = 40960 | **A3** | Tìm kẻ lạ ở Khu hang động | 1 | 71 | 160 | checkDoneTaskTalkNpc: npc.tempId == 71 (Berry) VÀ mapId == 160 |
| 1 | `TASK_20_1` = 40962 | **B14** | Chọn cách xử lý: Granola hay Jaco | 1 | 71 | 160 | Menu NPC 71 Berry: [0] 'Đi theo Granola' -> giữ task 20, index = 2; [1] 'Báo cảnh sát vũ trụ Jaco' -> switchTaskBranch(player, 48) đặt task 48 index = 2 |
| 2 | `TASK_20_2` = 40964 | **A3** | Bắt tay với Granola | 1 | 76 | 160 | checkDoneTaskTalkNpc: npc.tempId == 76 (Granola) VÀ mapId == 160 |
| 3 | `TASK_20_3` = 40966 | **A2** | Thanh toán 3 tay chân của Fide | 3 | — | — | checkDoneTaskKillBoss đếm CHUNG: Kuku (-20), Mập Đầu Đinh (-21), Rambo (-22) |
| 4 | `TASK_20_4` = 40968 | **A4** | Nhặt 3 Thẻ tiền thưởng | 3 | — | — | checkDoneTaskPickItem: template 2016; rơi 100% từ boss -20/-21/-22 khi isCurrentTask(TASK_20_4) |
| 5 | `TASK_20_5` = 40970 | **A3** | Nhận tiền thưởng từ Granola | 1 | 76 | 160 | checkDoneTaskTalkNpc: npc.tempId == 76 VÀ mapId == 160 |

#### NV 21 — Doanh trại Độc Nhãn  *(chương 3, 4 bước, thưởng 9.000.000 SM & TN)*

| # | Hằng | Kiểu | Tên bước | SL | NPC | Map | Điều kiện server kiểm tra |
|---|---|---|---|---|---|---|---|
| 0 | `TASK_21_0` = 43008 | **A3** | Gặp Lính canh ở Rừng Bamboo | 1 | 25 | 27 | checkDoneTaskTalkNpc: npc.tempId == 25 (Lính canh) VÀ mapId == 27 |
| 1 | `TASK_21_1` = 43010 | **B1** | Phá xong Doanh trại Độc Nhãn | 1 | — | 53 | checkDoneTaskFinishDungeon(player, ConstMap.MAP_DOANH_TRAI): móc tại chỗ đặt winDT = true trong map/phoban/RedRibbonHQ, duyệt mọi Player trong instance |
| 2 | `TASK_21_2` = 43012 | **A3** | Lấy bản đồ hành quân từ Độc Nhãn | 1 | 26 | 57 | checkDoneTaskTalkNpc: npc.tempId == 26 VÀ mapId == 57 VÀ zone.winDT == true |
| 3 | `TASK_21_3` = 43014 | **A3** | Mang bản đồ về cho %10 | 1 | -5 NPC_QUY_LAO | -9 MAP_QUY_LAO | checkDoneTaskTalkNpc: npc.tempId == 13 + gender |

#### NV 22 — Tiểu đội sát thủ  *(chương 3, 5 bước, thưởng 11.000.000 SM & TN)*

| # | Hằng | Kiểu | Tên bước | SL | NPC | Map | Điều kiện server kiểm tra |
|---|---|---|---|---|---|---|---|
| 0 | `TASK_22_0` = 45056 | **A3** | Hỏi Tapion về máy đo lạ | 1 | 53 | 126 | checkDoneTaskTalkNpc: npc.tempId == 53 VÀ mapId == 126 |
| 1 | `TASK_22_1` = 45058 | **A1** | Hạ 40 lính khỉ canh đường | 40 | — | 81 | checkDoneTaskKillMob: mob.tempId == 54 hoặc 55, đếm chung, tại map 81/82/83 |
| 2 | `TASK_22_2` = 45060 | **A2** | Hạ trọn Tiểu đội sát thủ | 5 | — | 79 | checkDoneTaskKillBoss đếm CHUNG 5 boss: -23, -24, -25, -26, -27 |
| 3 | `TASK_22_3` = 45062 | **A4** | Nhặt Máy đo ký ức | 1 | — | 79 | checkDoneTaskPickItem: template 2018; rơi 100% từ boss -27 khi isCurrentTask(TASK_22_3) |
| 4 | `TASK_22_4` = 45064 | **A3** | Đưa máy đo cho Tapion | 1 | 53 | 126 | checkDoneTaskTalkNpc: npc.tempId == 53 VÀ mapId == 126 |

#### NV 23 — Fide đại ca  *(chương 3, 6 bước, thưởng 16.000.000 SM & TN)*

| # | Hằng | Kiểu | Tên bước | SL | NPC | Map | Điều kiện server kiểm tra |
|---|---|---|---|---|---|---|---|
| 0 | `TASK_23_0` = 47104 | **A5** | Đạt 80.000.000 sức mạnh | 1 | — | — | checkDoneTaskPower: power >= 80.000.000 — PHẢI kiểm lại ngay trong sendNextSubTask |
| 1 | `TASK_23_1` = 47106 | **A6** | Tới Núi khỉ vàng | 1 | — | 80 | checkDoneTaskGoToMap: mapId == 80 |
| 2 | `TASK_23_2` = 47108 | **B12** | Đốt kho tiếp tế: 25 Khỉ lông vàng trong 5 phút | 25 | — | 80 | checkDoneTaskKillMob mob.tempId == 57 tại map 80 + đồng hồ 300.000 ms; hết giờ -> count = 0, lastTime = now |
| 3 | `TASK_23_3` = 47110 | **A2** | Hạ Fide đại ca — hai dạng đầu | 2 | — | 80 | checkDoneTaskKillBoss: boss -28, currentLevel 1 rồi 2 |
| 4 | `TASK_23_4` = 47112 | **A2** | Hạ Fide đại ca — dạng cuối | 1 | — | 80 | checkDoneTaskKillBoss: boss -28, currentLevel 3 |
| 5 | `TASK_23_5` = 47114 | **A3** | Tuyên bố liên minh với %10 | 1 | -5 NPC_QUY_LAO | -9 MAP_QUY_LAO | checkDoneTaskTalkNpc: npc.tempId == 13 + gender |

### Chương 4 — Cỗ máy và những bản sao

#### NV 24 — Tín hiệu lạ từ phương Bắc  *(chương 4, 5 bước, thưởng 80.000.000 SM & TN)*

| # | Hằng | Kiểu | Tên bước | SL | NPC | Map | Điều kiện server kiểm tra |
|---|---|---|---|---|---|---|---|
| 0 | `TASK_24_0` = 49152 | **A3** | Nghe Bunma nói về sóng cơ khí | 1 | 37 | 102 | checkDoneTaskTalkNpc: npc.tempId == 37 VÀ mapId == 102 |
| 1 | `TASK_24_1` = 49154 | **A6** | Tới Thành phố phía đông | 1 | — | 92 | checkDoneTaskGoToMap: mapId == 92 |
| 2 | `TASK_24_2` = 49156 | **A1** | Diệt 50 Xên con cấp 1-2 | 50 | — | 92 | checkDoneTaskKillMob: mob.tempId == 58 hoặc 59, đếm chung, map 92/93 |
| 3 | `TASK_24_3` = 49158 | **A1** | Diệt 40 Xên con cấp 3-4 | 40 | — | 94 | checkDoneTaskKillMob: mob.tempId == 60 hoặc 61, đếm chung, map 94/96 (KHÔNG dùng map 95 — MAP_OFFLINE) |
| 4 | `TASK_24_4` = 49160 | **A3** | Báo lại cho Bunma | 1 | 37 | 102 | checkDoneTaskTalkNpc: npc.tempId == 37 VÀ mapId == 102 |

#### NV 25 — Android đầu tiên  *(chương 4, 4 bước, thưởng 100.000.000 SM & TN)*

| # | Hằng | Kiểu | Tên bước | SL | NPC | Map | Điều kiện server kiểm tra |
|---|---|---|---|---|---|---|---|
| 0 | `TASK_25_0` = 51200 | **A6** | Tới Cao nguyên tìm hai bác sĩ | 1 | — | 96 | checkDoneTaskGoToMap: mapId == 96 |
| 1 | `TASK_25_1` = 51202 | **A2** | Hạ Android 19 rồi Dr.Kôrê | 2 | — | 96 | checkDoneTaskKillBoss đếm CHUNG: Android 19 (-30) và Dr.Kôrê (-31) |
| 2 | `TASK_25_2` = 51204 | **A4** | Nhặt 3 Lõi năng lượng Android | 3 | — | 96 | checkDoneTaskPickItem: template 2019; rơi 100% từ boss -30/-31 khi isCurrentTask(TASK_25_2) |
| 3 | `TASK_25_3` = 51206 | **A3** | Đưa lõi cho Bunma mổ xẻ | 1 | 37 | 102 | checkDoneTaskTalkNpc: npc.tempId == 37 VÀ mapId == 102 |

#### NV 26 — Kim loại và ký ức  *(chương 4, 5 bước, thưởng 130.000.000 SM & TN)*

| # | Hằng | Kiểu | Tên bước | SL | NPC | Map | Điều kiện server kiểm tra |
|---|---|---|---|---|---|---|---|
| 0 | `TASK_26_0` = 53248 | **A3** | Gặp Bà Hạt Mít ở Đảo Kamê | 1 | 21 | 5 | checkDoneTaskTalkNpc: npc.tempId == 21 VÀ mapId == 5; switch TRAO nguyên liệu miễn phí |
| 1 | `TASK_26_1` = 53250 | **B3** | Pha lê hóa một trang bị | 1 | 21 | 5 | checkDoneTaskCombine: combine/PhaLeHoaTrangBi roll thành công -> option 107 >= 1; HOẶC tính xong ngay nếu người chơi đã sẵn có trang bị option 107 >= 1 (chống kẹt) |
| 2 | `TASK_26_2` = 53252 | **B3** | Ép 1 Sao pha lê vào trang bị đó | 1 | 21 | 5 | checkDoneTaskCombine: combine/EpSaoTrangBi thành công -> option 102 >= 1 |
| 3 | `TASK_26_3` = 53254 | **A12** | Dùng Mẫu kim loại có ký ức | 1 | — | — | checkDoneTaskUseItem: template 2020 — phát 3 dòng thoại rồi trừ item |
| 4 | `TASK_26_4` = 53256 | **A3** | Kể lại cho %10 những gì ngươi nghe được | 1 | -5 NPC_QUY_LAO | -9 MAP_QUY_LAO | checkDoneTaskTalkNpc: npc.tempId == 13 + gender |

#### NV 27 — Ba cỗ máy  *(chương 4, 4 bước, thưởng 150.000.000 SM & TN)*

| # | Hằng | Kiểu | Tên bước | SL | NPC | Map | Điều kiện server kiểm tra |
|---|---|---|---|---|---|---|---|
| 0 | `TASK_27_0` = 55296 | **A3** | Hỏi Ca Lích về container lạ | 1 | 38 | 102 | checkDoneTaskTalkNpc: npc.tempId == 38 (Ca Lích) VÀ mapId == 102 |
| 1 | `TASK_27_1` = 55298 | **A6** | Tới sân sau siêu thị | 1 | — | 104 | checkDoneTaskGoToMap: mapId == 104 |
| 2 | `TASK_27_2` = 55300 | **A2** | Hạ ba cỗ máy mẫu | 3 | — | 104 | checkDoneTaskKillBoss đếm CHUNG: Android 15 (-34), Android 13 (-32), Android 14 (-33) — KHÔNG tách 3 bước vì cơ chế callApk13 ép thứ tự |
| 3 | `TASK_27_3` = 55302 | **A3** | Báo cáo với Ca Lích | 1 | 38 | 102 | checkDoneTaskTalkNpc: npc.tempId == 38 VÀ mapId == 102 |

#### NV 28 — King Kong  *(chương 4, 5 bước, thưởng 180.000.000 SM & TN)*

| # | Hằng | Kiểu | Tên bước | SL | NPC | Map | Điều kiện server kiểm tra |
|---|---|---|---|---|---|---|---|
| 0 | `TASK_28_0` = 57344 | **A6** | Tới Thành phố phía bắc | 1 | — | 97 | checkDoneTaskGoToMap: mapId == 97 |
| 1 | `TASK_28_1` = 57346 | **A1** | Dọn 60 Xên con cấp 5-7 quanh cửa hầm | 60 | — | 97 | checkDoneTaskKillMob: mob.tempId == 62, 63 hoặc 64, đếm chung, map 97/98/99 |
| 2 | `TASK_28_2` = 57348 | **A2** | Hạ Poc, Pic rồi King Kong | 3 | — | 97 | checkDoneTaskKillBoss đếm CHUNG: Poc (-36), Pic (-35), King Kong (-37) |
| 3 | `TASK_28_3` = 57350 | **A4** | Nhặt Mảnh giáp có khắc tên | 1 | — | 97 | checkDoneTaskPickItem: template 2021; rơi 100% từ King Kong (-37) khi isCurrentTask(TASK_28_3) |
| 4 | `TASK_28_4` = 57352 | **A3** | Đưa mảnh giáp cho Bunma | 1 | 37 | 102 | checkDoneTaskTalkNpc: npc.tempId == 37 VÀ mapId == 102 |

#### NV 29 — Phòng thí nghiệm Myuu  *(chương 4, 4 bước, thưởng 200.000.000 SM & TN)*

| # | Hằng | Kiểu | Tên bước | SL | NPC | Map | Điều kiện server kiểm tra |
|---|---|---|---|---|---|---|---|
| 0 | `TASK_29_0` = 59392 | **A3** | Lấy thẻ từ giả của Bunma | 1 | 37 | 102 | checkDoneTaskTalkNpc: npc.tempId == 37 VÀ mapId == 102; switch TRAO 1 item 2022 |
| 1 | `TASK_29_1` = 59394 | **A6** | Đột nhập Phòng thí nghiệm Myuu | 1 | — | 166 | checkDoneTaskGoToMap: mapId == 166; điều kiện vào map: có item 2022 trong hành trang |
| 2 | `TASK_29_2` = 59396 | **B12** | Giật 5 Bản thiết kế trong 6 phút | 5 | — | 166 | checkDoneTaskPickItem template 2023 (ItemMap sinh sẵn 5-8 viên trên map 166) + đồng hồ 360.000 ms; hết giờ -> count = 0, xóa item 2023 đang cầm, đẩy người chơi về map 97; rời map 166 -> count = 0, lastTime = 0 |
| 3 | `TASK_29_3` = 59398 | **A3** | Đối mặt Dr. Myuu | 1 | 83 | 166 | checkDoneTaskTalkNpc: npc.tempId == 83 VÀ mapId == 166 |

#### NV 30 — Xên bọ hung  *(chương 4, 5 bước, thưởng 220.000.000 SM & TN)*

| # | Hằng | Kiểu | Tên bước | SL | NPC | Map | Điều kiện server kiểm tra |
|---|---|---|---|---|---|---|---|
| 0 | `TASK_30_0` = 61440 | **A6** | Tới Thị trấn Ginder | 1 | — | 100 | checkDoneTaskGoToMap: mapId == 100 |
| 1 | `TASK_30_1` = 61442 | **A1** | Diệt 50 Xên con cấp 8 | 50 | — | 100 | checkDoneTaskKillMob: mob.tempId == 65 tại map 100 |
| 2 | `TASK_30_2` = 61444 | **A2** | Hạ Xên bọ hung — hai dạng đầu | 2 | — | 100 | checkDoneTaskKillBoss: boss -100, currentLevel 1 rồi 2 |
| 3 | `TASK_30_3` = 61446 | **A2** | Hạ Xên hoàn thiện | 1 | — | 100 | checkDoneTaskKillBoss: boss -100, currentLevel 3 |
| 4 | `TASK_30_4` = 61448 | **A3** | Báo với Bunma về mẫu 07 | 1 | 37 | 102 | checkDoneTaskTalkNpc: npc.tempId == 37 VÀ mapId == 102 |

#### NV 31 — Bản sao của chính ngươi  *(chương 4, 6 bước, thưởng 230.000.000 SM & TN)*

| # | Hằng | Kiểu | Tên bước | SL | NPC | Map | Điều kiện server kiểm tra |
|---|---|---|---|---|---|---|---|
| 0 | `TASK_31_0` = 63488 | **A3** | Nghe Potage nói sự thật | 1 | 62 | 140 | checkDoneTaskTalkNpc: npc.tempId == 62 (Potage) VÀ mapId == 140 |
| 1 | `TASK_31_1` = 63490 | **B14** | Quyết định số phận bản sao | 1 | 62 | 140 | Menu NPC 62 Potage: [0] 'Tiêu diệt nó' -> giữ task 31, index = 2; [1] 'Thu nhận nó' -> switchTaskBranch(player, 49) đặt task 49 index = 2 VÀ trao 1 Bình chứa Commeson (638) bản không hạn sử dụng |
| 2 | `TASK_31_2` = 63492 | **A6** | Tới Võ đài Xên bọ hung | 1 | — | 103 | checkDoneTaskGoToMap: mapId == 103 |
| 3 | `TASK_31_3` = 63494 | **B13** | Gọi một nhân chứng vào võ đài | 1 | — | 103 | checkDoneTaskTogetherInZone: zone của map 103 có >= 2 Player thật; kiểm tra định kỳ trong Player.update |
| 4 | `TASK_31_4` = 63496 | **A2** | Tiêu diệt Bản sao của ngươi | 1 | — | 103 | checkDoneTaskKillBoss: nhận diện bằng boss instanceof BanSaoNguoiChoi && boss.ownerId == player.id (KHÔNG so id số) |
| 5 | `TASK_31_5` = 63498 | **A5** | Đạt 2.000.000.000 sức mạnh | 1 | — | — | checkDoneTaskPower: power >= 2.000.000.000 — kiểm lại ngay trong sendNextSubTask |

### Chương 5 — Vương triều bóng tối

#### NV 32 — Lời cảnh báo của Bardock  *(chương 5, 6 bước, thưởng 200.000.000 SM & TN)*

| # | Hằng | Kiểu | Tên bước | SL | NPC | Map | Điều kiện server kiểm tra |
|---|---|---|---|---|---|---|---|
| 0 | `TASK_32_0` = 65536 | **A3** | Gặp Bunma ở Nhà Bunma | 1 | 37 | 102 | checkDoneTaskTalkNpc: npc.tempId == 37 VÀ mapId == 102; switch TRAO 1 item 992 |
| 1 | `TASK_32_1` = 65538 | **A12** | Dùng Nhẫn thời không sai lệch | 1 | — | — | checkDoneTaskUseItem: template 992 (UseItem -> Controller type 2 -> map 160) |
| 2 | `TASK_32_2` = 65540 | **A3** | Nói chuyện với Bardock | 1 | 70 | 160 | checkDoneTaskTalkNpc: npc.tempId == 70 (Bardock) VÀ mapId == 160 |
| 3 | `TASK_32_3` = 65542 | **A1** | Dọn sạch hang động nguyên thủy | 40 | — | 160 | checkDoneTaskKillMob: mob.tempId == 80 (Cabira) hoặc 81 (Tobi), map 160/161/162 |
| 4 | `TASK_32_4` = 65544 | **A4** | Nhặt 3 Mảnh Ký Ức Vỡ | 3 | — | 161 | checkDoneTaskPickItem: template 2025; Mob.dropItemTask chỉ rơi từ mob 81 Tobi khi isCurrentTask(TASK_32_4) |
| 5 | `TASK_32_5` = 65546 | **A3** | Báo cáo với Bardock | 1 | 70 | 160 | checkDoneTaskTalkNpc: npc.tempId == 70 VÀ mapId == 160; switch trừ 3 item 2025 |

#### NV 33 — Phá vỡ giới hạn  *(chương 5, 6 bước, thưởng 250.000.000 SM & TN)*

| # | Hằng | Kiểu | Tên bước | SL | NPC | Map | Điều kiện server kiểm tra |
|---|---|---|---|---|---|---|---|
| 0 | `TASK_33_0` = 67584 | **A6** | Tới vách núi của hành tinh bạn | 1 | — | -4 MAP_VACH_NUI | checkDoneTaskGoToMap: mapId ∈ {42,43,44} (MAP_VACH_NUI sau khi sửa) |
| 1 | `TASK_33_1` = 67586 | **A3** | Nói chuyện với Quốc Vương | 1 | 42 | -4 MAP_VACH_NUI | checkDoneTaskTalkNpc: npc.tempId == 42; NPC phải hiện khi getIdTask >= TASK_33_1 (hiện đang đòi SM >= 17 tỷ) |
| 2 | `TASK_33_2` = 67588 | **B11** | Nâng HP gốc chạm trần 220.000 | 1 | — | — | checkDoneTaskBasePoint: NPoint.increasePoint(type 0) xong, kiểm tra hpg >= getHpMpLimit() (220.000 khi limitPower == 0) |
| 3 | `TASK_33_3` = 67590 | **B10** | Mở giới hạn sức mạnh | 1 | 42 | -4 MAP_VACH_NUI | checkDoneTaskOpenPower: OpenPowerService.openPowerByTask MỚI — miễn phí, chỉ chạy khi getIdTask == TASK_33_3 VÀ limitPower == 0, limitPower 0 -> 1 |
| 4 | `TASK_33_4` = 67592 | **A5** | Đạt 3 tỷ sức mạnh | 1 | — | — | checkDoneTaskPower: power >= 3.000.000.000 |
| 5 | `TASK_33_5` = 67594 | **A3** | Báo cáo với Quốc Vương | 1 | 42 | -4 MAP_VACH_NUI | checkDoneTaskTalkNpc: npc.tempId == 42 |

#### NV 34 — Vùng đất băng giá  *(chương 5, 6 bước, thưởng 300.000.000 SM & TN)*

| # | Hằng | Kiểu | Tên bước | SL | NPC | Map | Điều kiện server kiểm tra |
|---|---|---|---|---|---|---|---|
| 0 | `TASK_34_0` = 69632 | **A6** | Tới Cánh đồng tuyết | 1 | — | 105 | checkDoneTaskGoToMap: mapId == 105 |
| 1 | `TASK_34_1` = 69634 | **A1** | Diệt bọn canh băng | 50 | — | 105 | checkDoneTaskKillMob: mob.tempId == 66 (Tai tím) hoặc 67 (Abo), map 105/106/107 |
| 2 | `TASK_34_2` = 69636 | **B12** | Hạ 20 Kado trong 5 phút | 20 | — | 108 | checkDoneTaskKillMob mob.tempId == 68 (Kado) tại map 108/109 + đồng hồ 300.000 ms; hết giờ -> count = 0, lastTime = now |
| 3 | `TASK_34_3` = 69638 | **A4** | Nhặt Mảnh Ký Ức Đóng Băng | 1 | — | 110 | checkDoneTaskPickItem: template 2026; Zone.getItemMapsForPlayer chỉ hiện với người ở TASK_34_3, map 110 |
| 4 | `TASK_34_4` = 69640 | **A2** | Hạ Cooler cả hai dạng | 2 | — | 110 | checkDoneTaskKillBoss: boss -29, currentLevel 0 rồi 1 — PHẢI thêm checkDoneTaskKillBoss vào Cooler.reward() |
| 5 | `TASK_34_5` = 69642 | **A3** | Báo cáo với Bardock | 1 | 70 | 160 | checkDoneTaskTalkNpc: npc.tempId == 70 VÀ mapId == 160; switch trừ 1 item 2026 |

#### NV 35 — Con đường rắn độc  *(chương 5, 5 bước, thưởng 350.000.000 SM & TN)*

| # | Hằng | Kiểu | Tên bước | SL | NPC | Map | Điều kiện server kiểm tra |
|---|---|---|---|---|---|---|---|
| 0 | `TASK_35_0` = 71680 | **A3** | Nói chuyện với Thần Vũ Trụ | 1 | 20 | 48 | checkDoneTaskTalkNpc: npc.tempId == 20 VÀ mapId == 48 |
| 1 | `TASK_35_1` = 71682 | **B13** | Vào Con đường rắn độc cùng bạn | 1 | 20 | 143 | checkDoneTaskTogether(player, zone, 2, requireSameClan = true) sau khi SnakeWayService.openConDuongRanDoc thành công, tại map 143 |
| 2 | `TASK_35_2` = 71684 | **A1** | Dọn đường qua ba chặng | 60 | — | 141 | checkDoneTaskKillMob: mob.tempId ∈ {24, 33, 25, 26, 49, 50} trong map 141/142/143 |
| 3 | `TASK_35_3` = 71686 | **B1** | Hoàn thành Con đường rắn độc | 1 | — | 144 | checkDoneTaskDungeon(player, ConstMap.MAP_CON_DUONG_RAN_DOC): móc ở SnakeWay.finish() / CADICH.leaveMap() (chỗ đặt endCDRD = true), duyệt mọi người trong instance |
| 4 | `TASK_35_4` = 71688 | **A3** | Gặp Thượng Đế ở Thần điện | 1 | 19 | 45 | checkDoneTaskTalkNpc: npc.tempId == 19 VÀ mapId == 45 |

#### NV 36 — Cổng phi thuyền  *(chương 5, 5 bước, thưởng 400.000.000 SM & TN)*

| # | Hằng | Kiểu | Tên bước | SL | NPC | Map | Điều kiện server kiểm tra |
|---|---|---|---|---|---|---|---|
| 0 | `TASK_36_0` = 73728 | **A3** | Gặp Ôsin ở Đại hội võ thuật | 1 | 44 | 52 | checkDoneTaskTalkNpc: npc.tempId == 44 VÀ mapId == 52; switch TRAO 1 item 1795 (miễn phí, trao lại nếu mất) |
| 1 | `TASK_36_1` = 73730 | **A6** | Vào Cổng phi thuyền | 1 | 44 | 114 | checkDoneTaskGoToMap: mapId == 114 (khung 12h) HOẶC mapId == 165 Sa mạc hoang vu (đường vòng 24/7, vào bằng item 1795 qua Ôsin map 52) |
| 2 | `TASK_36_2` = 73732 | **A2** | Hạ Drabura hoặc 20 Cadic M | 20 | — | 114 | checkDoneTaskKillBoss boss -233 Drabura -> addDoneSubTask(player, 20); HOẶC checkDoneTaskKillMob mob.tempId == 95/118 (Cadic M) ở map 165 -> +1 mỗi con. TUYỆT ĐỐI không dùng Drabura 2 (-237) |
| 3 | `TASK_36_3` = 73734 | **A6** | Xuống tới Cửa Ải 1 | 1 | — | 117 | checkDoneTaskGoToMap mapId == 117 HOẶC checkDoneTaskPickItem template 2027 rơi từ Cadic M ở map 165 |
| 4 | `TASK_36_4` = 73736 | **A3** | Nói chuyện với Babiđây | 1 | 46 | 117 | checkDoneTaskTalkNpc: npc.tempId == 46 VÀ mapId == 117 HOẶC npc.tempId == 44 (Ôsin) VÀ mapId == 165 |

#### NV 37 — Mabư  *(chương 5, 5 bước, thưởng 450.000.000 SM & TN)*

| # | Hằng | Kiểu | Tên bước | SL | NPC | Map | Điều kiện server kiểm tra |
|---|---|---|---|---|---|---|---|
| 0 | `TASK_37_0` = 75776 | **A3** | Gặp Ôsin ở Đại hội võ thuật | 1 | 44 | 52 | checkDoneTaskTalkNpc: npc.tempId == 44 VÀ mapId == 52 |
| 1 | `TASK_37_1` = 75778 | **A2** | Hạ Mabư | 1 | — | 120 | checkDoneTaskKillBoss boss -236 (map 120, khung 12h) HOẶC boss -214 currentLevel 4 Kid Bư (map 127, khung 14h, chỉ chết bởi Quả cầu kênh khi) HOẶC checkDoneTaskKillMob mob.tempId == 70 Hirudegarn (map 126, 24/7) |
| 2 | `TASK_37_2` = 75780 | **A2** | Hạ Drabura 3 hoặc 30 Quỷ chim | 30 | — | 120 | checkDoneTaskKillBoss boss -343 hoặc -348 Super Bư -> addDoneSubTask(player, 30); HOẶC checkDoneTaskKillMob mob.tempId == 50 (Quỷ chim) ở map 126 -> +1 mỗi con |
| 3 | `TASK_37_3` = 75782 | **A4** | Nhặt Lõi Phép Babiđây | 1 | — | 120 | checkDoneTaskPickItem: template 2028; rơi 100% cho người kết liễu ở bước 2 |
| 4 | `TASK_37_4` = 75784 | **A3** | Mang Lõi Phép cho Kibit | 1 | 45 | 50 | checkDoneTaskTalkNpc: npc.tempId == 45 (Kibit) VÀ mapId == 50; switch trừ 1 item 2028 |

#### NV 38 — Black Goku  *(chương 5, 6 bước, thưởng 550.000.000 SM & TN)*

| # | Hằng | Kiểu | Tên bước | SL | NPC | Map | Điều kiện server kiểm tra |
|---|---|---|---|---|---|---|---|
| 0 | `TASK_38_0` = 77824 | **A3** | Gặp Bunma ở Tương lai | 1 | 37 | 102 | checkDoneTaskTalkNpc: npc.tempId == 37 VÀ mapId == 102 |
| 1 | `TASK_38_1` = 77826 | **A1** | Dọn sạch bọn Xên con phía bắc | 60 | — | 97 | checkDoneTaskKillMob: mob.tempId ∈ {62,63,64,65}, map 97/98/99/100 |
| 2 | `TASK_38_2` = 77828 | **A2** | Hạ Black Goku | 1 | — | 92 | checkDoneTaskKillBoss: boss -203, currentLevel == 0 |
| 3 | `TASK_38_3` = 77830 | **A2** | Hạ Super Black Goku | 1 | — | 92 | checkDoneTaskKillBoss: boss -203, currentLevel == 1 — hai bước ĐỘC LẬP, không ép thứ tự (autoLeaveMap có 50% tự lên form 2) |
| 4 | `TASK_38_4` = 77832 | **A4** | Nhặt Nhẫn thời không sai lệch | 1 | — | 92 | checkDoneTaskPickItem: template 992 — ép rơi 100% khi người kết liễu đang ở TASK_38_4 |
| 5 | `TASK_38_5` = 77834 | **A3** | Báo cáo với Bunma | 1 | 37 | 102 | checkDoneTaskTalkNpc: npc.tempId == 37 VÀ mapId == 102 |

#### NV 39 — Cái giá của ký ức  *(chương 5, 7 bước, thưởng 1.000.000.000 SM & TN)*

| # | Hằng | Kiểu | Tên bước | SL | NPC | Map | Điều kiện server kiểm tra |
|---|---|---|---|---|---|---|---|
| 0 | `TASK_39_0` = 79872 | **A3** | Nói chuyện với Bardock | 1 | 70 | 160 | checkDoneTaskTalkNpc: npc.tempId == 70 VÀ mapId == 160 |
| 1 | `TASK_39_1` = 79874 | **A4** | Gom đủ 7 viên Ngọc Rồng | 7 | — | — | checkDoneTaskPickItem: item 14..20, mỗi loại tính 1 lần (kiểm tra hành trang) |
| 2 | `TASK_39_2` = 79876 | **B6** | Gọi Rồng Thần và ước | 1 | 5 | -8 MAP_LANG | checkDoneTaskWishDragon(player, starType): móc trong SummonDragon.confirmWish ngay trước lastTimeShenronAppeared = now; CHỈ tính Rồng Thần 1 Sao, map 0/7/14 |
| 3 | `TASK_39_3` = 79878 | **A3** | Hỏi Rồng Omega về sao đen | 1 | 29 | -6 MAP_TTVT | checkDoneTaskTalkNpc: npc.tempId == 29 VÀ mapId ∈ {24,25,26}; switch TRAO Mảnh Ký Ức 5 |
| 4 | `TASK_39_4` = 79880 | **A1** | Diệt bầy khỉ ở Núi khỉ vàng | 40 | — | 80 | checkDoneTaskKillMob: mob.tempId == 57 tại map 80 |
| 5 | `TASK_39_5` = 79882 | **A3** | Gặp Bardock ở Làng Kakarot | 1 | 70 | 14 | checkDoneTaskTalkNpc: npc.tempId == 70 VÀ mapId == 14 (phải thêm NPC 70 vào map_template của map 14); switch TRAO Mảnh Ký Ức 6 |
| 6 | `TASK_39_6` = 79884 | **A2** | Hạ Baby cả ba dạng | 3 | — | 14 | checkDoneTaskKillBoss: boss -925, currentLevel 0, 1, 2 — PHẢI thêm checkDoneTaskKillBoss vào Baby.reward() |

### Chương 6 — Lõi Hư Không

#### NV 40 — Tổ Sư Kaio  *(chương 6, 5 bước, thưởng 550.000.000 SM & TN)*

| # | Hằng | Kiểu | Tên bước | SL | NPC | Map | Điều kiện server kiểm tra |
|---|---|---|---|---|---|---|---|
| 0 | `TASK_40_0` = 81920 | **A3** | Nói chuyện với Thần Vũ Trụ | 1 | 20 | 48 | checkDoneTaskTalkNpc: npc.tempId == 20 VÀ mapId == 48 |
| 1 | `TASK_40_1` = 81922 | **A6** | Lên Thánh địa Kaio | 1 | — | 50 | checkDoneTaskGoToMap: mapId == 50 |
| 2 | `TASK_40_2` = 81924 | **A3** | Nói chuyện với Tổ Sư Kaio | 1 | 43 | 50 | checkDoneTaskTalkNpc: npc.tempId == 43 VÀ mapId == 50 |
| 3 | `TASK_40_3` = 81926 | **A12** | Đưa Mảnh Ký Ức cho Tổ Sư Kaio | 1 | 43 | 50 | checkDoneTaskUseItem: template 2002 (Mảnh Ký Ức 1) VÀ mapId == 50 — KHÔNG trừ item, phải giữ tới NV 45 |
| 4 | `TASK_40_4` = 81928 | **A3** | Nghe Kibit kể phần còn lại | 1 | 45 | 50 | checkDoneTaskTalkNpc: npc.tempId == 45 VÀ mapId == 50 |

#### NV 41 — Cơn thịnh nộ Broly  *(chương 6, 5 bước, thưởng 650.000.000 SM & TN)*

| # | Hằng | Kiểu | Tên bước | SL | NPC | Map | Điều kiện server kiểm tra |
|---|---|---|---|---|---|---|---|
| 0 | `TASK_41_0` = 83968 | **A3** | Nhận lời dặn của Tổ Sư Kaio | 1 | 43 | 50 | checkDoneTaskTalkNpc: npc.tempId == 43 VÀ mapId == 50 |
| 1 | `TASK_41_1` = 83970 | **A1** | Dọn dẹp vành đai rừng | 60 | — | 27 | checkDoneTaskKillMob: mob.tempId ∈ {16, 22, 32, 33, 24} tại map 27-38 |
| 2 | `TASK_41_2` = 83972 | **A2** | Hạ Broly | 1 | — | 27 | checkDoneTaskKillBoss: boss -1822 — PHẢI thêm reward()/checkDoneTaskKillBoss vào Broly.die() |
| 3 | `TASK_41_3` = 83974 | **A2** | Hạ Super Broly | 1 | — | 27 | checkDoneTaskKillBoss: boss -82282 — PHẢI thêm checkDoneTaskKillBoss vào SuperBroly.reward() |
| 4 | `TASK_41_4` = 83976 | **A3** | Báo lại với Tổ Sư Kaio | 1 | 43 | 50 | checkDoneTaskTalkNpc: npc.tempId == 43 VÀ mapId == 50 |

#### NV 42 — Hành tinh ngục tù  *(chương 6, 6 bước, thưởng 750.000.000 SM & TN)*

| # | Hằng | Kiểu | Tên bước | SL | NPC | Map | Điều kiện server kiểm tra |
|---|---|---|---|---|---|---|---|
| 0 | `TASK_42_0` = 86016 | **A3** | Hỏi Ôsin đường tới ngục tù | 1 | 44 | 50 | checkDoneTaskTalkNpc: npc.tempId == 44 VÀ mapId == 50 — phải thêm nút 'Đến hành tinh ngục tù' vào Osin case 50 (lối vào cũ đi qua map 154 chỉ mở ở TASK_44_0) |
| 1 | `TASK_42_1` = 86018 | **A6** | Tới Hành tinh ngục tù | 1 | — | 155 | checkDoneTaskGoToMap: mapId == 155 |
| 2 | `TASK_42_2` = 86020 | **B12** | Phá 60 lồng giam trong 10 phút | 60 | — | 155 | checkDoneTaskKillMob mob.tempId == 78 hoặc 79 tại map 155 + đồng hồ 600.000 ms; hết giờ -> count = 0, lastTime = now |
| 3 | `TASK_42_3` = 86022 | **A2** | Hạ Cumber | 1 | — | 155 | checkDoneTaskKillBoss: boss -203999, currentLevel == 0 |
| 4 | `TASK_42_4` = 86024 | **A2** | Hạ Super Cumber | 1 | — | 155 | checkDoneTaskKillBoss: boss -203999, currentLevel == 1 — hai bước ĐỘC LẬP |
| 5 | `TASK_42_5` = 86026 | **A3** | Báo cáo với Ôsin | 1 | 44 | 155 | checkDoneTaskTalkNpc: npc.tempId == 44 VÀ mapId == 155 |

#### NV 43 — Khí gas hủy diệt  *(chương 6, 6 bước, thưởng 850.000.000 SM & TN)*

| # | Hằng | Kiểu | Tên bước | SL | NPC | Map | Điều kiện server kiểm tra |
|---|---|---|---|---|---|---|---|
| 0 | `TASK_43_0` = 88064 | **A3** | Gặp Mr Popo ở Làng Aru | 1 | 67 | 0 | checkDoneTaskTalkNpc: npc.tempId == 67 VÀ mapId == 0 (Mr Popo chỉ có ở map 0) |
| 1 | `TASK_43_1` = 88066 | **A1** | Dọn sạch khí gas | 80 | — | 147 | checkDoneTaskKillMob: mob.tempId ∈ {73,74,75,76} trong instance phó bản, map 147/149/151/152 (map 150 KHÔNG tồn tại) |
| 2 | `TASK_43_2` = 88068 | **A2** | Hạ Dr Lychee | 1 | — | 148 | checkDoneTaskKillBoss: boss -208 — PHẢI thêm checkDoneTaskKillBoss vào DrLychee.reward() |
| 3 | `TASK_43_3` = 88070 | **A2** | Hạ Hatchiyack | 1 | — | 148 | checkDoneTaskKillBoss: boss -207 — PHẢI thêm checkDoneTaskKillBoss vào Hatchiyack.reward() |
| 4 | `TASK_43_4` = 88072 | **B1** | Hoàn thành Khí gas hủy diệt | 1 | — | 147 | checkDoneTaskDungeon(player, ConstMap.MAP_KHI_GAS): DestronGas.finish() / cờ clan.KhiGasHuyDiet.hatchiyatchDead == true; cho MỌI người trong instance xong bước |
| 5 | `TASK_43_5` = 88074 | **A3** | Báo cáo với Thượng Đế | 1 | 19 | 45 | checkDoneTaskTalkNpc: npc.tempId == 19 VÀ mapId == 45 |

#### NV 44 — Thử thách của Thần Hủy Diệt  *(chương 6, 6 bước, thưởng 900.000.000 SM & TN)*

| # | Hằng | Kiểu | Tên bước | SL | NPC | Map | Điều kiện server kiểm tra |
|---|---|---|---|---|---|---|---|
| 0 | `TASK_44_0` = 90112 | **A3** | Nhờ Ôsin đưa tới hành tinh Bill | 1 | 44 | 50 | checkDoneTaskTalkNpc: npc.tempId == 44 VÀ mapId == 50 (nút 'Đến hành tinh Bill' -> map 154) |
| 1 | `TASK_44_1` = 90114 | **A3** | Nói chuyện với Bill | 1 | 55 | 154 | checkDoneTaskTalkNpc: npc.tempId == 55 VÀ mapId == 154 — phải viết lại Bill.confirmMenu (lỗi cũ dùng case theo mapId) |
| 2 | `TASK_44_2` = 90116 | **B5** | Thắng một trận đấu | 1 | 21 | 112 | checkDoneTaskWinMatch(player, typePvp): ThachDau.reward(winner) HOẶC DeathOrAliveArena chỗ haveRewardVDST = true HOẶC WorldMartialArtsTournament chỗ martialArtsTournamentWins++ |
| 3 | `TASK_44_3` = 90118 | **A2** | Thách đấu Whis | 1 | 56 | 154 | checkDoneTaskKillBoss: boss -364 Whis (TrainingService.callBoss) |
| 4 | `TASK_44_4` = 90120 | **B10** | Mở giới hạn sức mạnh lần hai | 1 | 43 | 50 | checkDoneTaskOpenPower: OpenPowerService.openPowerByTask — miễn phí, chỉ chạy khi getIdTask == TASK_44_4 VÀ limitPower == 1, limitPower 1 -> 2 |
| 5 | `TASK_44_5` = 90122 | **A3** | Nghe Whis dặn dò | 1 | 56 | 154 | checkDoneTaskTalkNpc: npc.tempId == 56 VÀ mapId == 154 |

#### NV 45 — Bảy mảnh hợp nhất  *(chương 6, 5 bước, thưởng 950.000.000 SM & TN)*

| # | Hằng | Kiểu | Tên bước | SL | NPC | Map | Điều kiện server kiểm tra |
|---|---|---|---|---|---|---|---|
| 0 | `TASK_45_0` = 92160 | **A6** | Tới Lãnh địa Fize | 1 | — | 78 | checkDoneTaskGoToMap: mapId == 78 |
| 1 | `TASK_45_1` = 92162 | **A4** | Nhặt Mảnh Ký Ức thứ bảy | 1 | — | 78 | checkDoneTaskPickItem: template 2008; Zone.getItemMapsForPlayer chỉ hiện với người ở TASK_45_1; switch TRAO 1 item 2024 |
| 2 | `TASK_45_2` = 92164 | **B13** | Làm lễ hợp nhất cùng một người khác | 1 | 64 | 78 | checkDoneTaskTogether(player, zone, 2, requireSameClan = false) tại map 78 |
| 3 | `TASK_45_3` = 92166 | **A12** | Hợp nhất bảy mảnh | 1 | 64 | 78 | checkDoneTaskUseItem: template 2024 — kiểm tra đủ 7 item 2002..2008 trong hành trang, trừ cả 7 + trừ 2024, TRAO 1 item 2000 Lõi Hư Không |
| 4 | `TASK_45_4` = 92168 | **A3** | Nghe Thiên Sứ Whis giải thích | 1 | 64 | 78 | checkDoneTaskTalkNpc: npc.tempId == 64 VÀ mapId == 78 — NPC 64 hiện có openBaseMenu/confirmMenu RỖNG, phải viết |

#### NV 46 — Heart  *(chương 6, 6 bước, thưởng 1.100.000.000 SM & TN)*

| # | Hằng | Kiểu | Tên bước | SL | NPC | Map | Điều kiện server kiểm tra |
|---|---|---|---|---|---|---|---|
| 0 | `TASK_46_0` = 94208 | **A3** | Nói chuyện với Dr. Myuu | 1 | 83 | 166 | checkDoneTaskTalkNpc: npc.tempId == 83 VÀ mapId == 166 |
| 1 | `TASK_46_1` = 94210 | **A2** | Hạ Heart | 1 | — | 166 | checkDoneTaskKillBoss: boss -108108, currentLevel == 0 (BOSS MỚI, HP 1.500.000.000) |
| 2 | `TASK_46_2` = 94212 | **A6** | Đuổi theo Heart tới Võ Đài Siêu Cấp | 1 | 64 | 145 | checkDoneTaskGoToMap: mapId == 145 |
| 3 | `TASK_46_3` = 94214 | **A2** | Hạ Heart Hư Không | 1 | — | 145 | checkDoneTaskKillBoss: boss -108108, currentLevel == 1 |
| 4 | `TASK_46_4` = 94216 | **A2** | Hạ Heart Toàn Ký | 1 | — | 145 | checkDoneTaskKillBoss: boss -108108, currentLevel == 2 — form này rơi item 2029 |
| 5 | `TASK_46_5` = 94218 | **A3** | Nói chuyện với Thiên Sứ Whis | 1 | 64 | 145 | checkDoneTaskTalkNpc: npc.tempId == 64 VÀ mapId == 145 |

#### NV 47 — Trả lại hay giữ lấy  *(chương 6, 6 bước, thưởng 1.500.000.000 SM & TN)*

| # | Hằng | Kiểu | Tên bước | SL | NPC | Map | Điều kiện server kiểm tra |
|---|---|---|---|---|---|---|---|
| 0 | `TASK_47_0` = 96256 | **B14** | Quyết định số phận của Lõi | 1 | 64 | 145 | checkDoneTaskConfirmMenuNpc: npc 64 ở map 145, menu 2 nút — select 0 'Trả ký ức cho vũ trụ' -> ở lại task 47 index 1; select 1 'Giữ Lõi Hư Không' -> switchTaskMain(player, 50, 1) |
| 1 | `TASK_47_1` = 96258 | **A12** | Trả Lõi Hư Không | 1 | 64 | 145 | checkDoneTaskUseItem: template 2000 VÀ mapId == 145 -> TRỪ item 2000, TRAO item 2001 Vỏ Lõi rỗng |
| 2 | `TASK_47_2` = 96260 | **A6** | Lên Thánh địa Kaio | 1 | — | 50 | checkDoneTaskGoToMap: mapId == 50 |
| 3 | `TASK_47_3` = 96262 | **B10** | Mở giới hạn sức mạnh lần cuối | 1 | 43 | 50 | checkDoneTaskOpenPower: openPowerByTask — chỉ chạy khi getIdTask == TASK_47_3 VÀ limitPower == 2, limitPower 2 -> 3 |
| 4 | `TASK_47_4` = 96264 | **A3** | Nghe Tổ Sư Kaio nói lời cuối | 1 | 43 | 50 | checkDoneTaskTalkNpc: npc.tempId == 43 VÀ mapId == 50 |
| 5 | `TASK_47_5` = 96266 | **A2** | Hạ Hư Không Vô Danh | 1 | — | 145 | checkDoneTaskKillBoss: boss -108108, currentLevel == 3, map 145; Heart.joinMap() chỉ RESPAWN form 4 khi khu có người ở TASK_47_5 / TASK_50_5 |

### Nhánh (task id 48, 49, 50)

#### NV 48 — Kẻ săn tiền thưởng  *(chương 3, 6 bước, thưởng 8.000.000 SM & TN)*

| # | Hằng | Kiểu | Tên bước | SL | NPC | Map | Điều kiện server kiểm tra |
|---|---|---|---|---|---|---|---|
| 0 | `TASK_48_0` = 98304 | **A3** | Tìm kẻ lạ ở Khu hang động | 1 | 71 | 160 | checkDoneTaskTalkNpc: npc.tempId == 71 VÀ mapId == 160 (giống TASK_20_0, chỉ để khớp index) |
| 1 | `TASK_48_1` = 98306 | **B14** | Chọn cách xử lý: Granola hay Jaco | 1 | 71 | 160 | Bước chỉ tồn tại để khớp index — người chơi tới đây là đã chọn Jaco ở TASK_20_1; switchTaskBranch đặt index = 2 ngay |
| 2 | `TASK_48_2` = 98308 | **A3** | Trình báo cảnh sát vũ trụ Jaco | 1 | 63 | 24 | checkDoneTaskTalkNpc: npc.tempId == 63 VÀ mapId == 24 (Jaco chỉ có ở map 24 và 139) |
| 3 | `TASK_48_3` = 98310 | **A2** | Thi hành lệnh truy nã 3 mục tiêu | 3 | — | — | checkDoneTaskKillBoss đếm CHUNG: Kuku (-20), Mập Đầu Đinh (-21), Rambo (-22) |
| 4 | `TASK_48_4` = 98312 | **A4** | Nhặt 3 Biên bản truy nã | 3 | — | — | checkDoneTaskPickItem: template 2017; rơi 100% từ boss -20/-21/-22 khi isCurrentTask(TASK_48_4) |
| 5 | `TASK_48_5` = 98314 | **A3** | Nộp biên bản cho Jaco | 1 | 63 | 24 | checkDoneTaskTalkNpc: npc.tempId == 63 VÀ mapId == 24 |

#### NV 49 — Bản sao của chính ngươi  *(chương 4, 7 bước, thưởng 230.000.000 SM & TN)*

| # | Hằng | Kiểu | Tên bước | SL | NPC | Map | Điều kiện server kiểm tra |
|---|---|---|---|---|---|---|---|
| 0 | `TASK_49_0` = 100352 | **A3** | Nghe Potage nói sự thật | 1 | 62 | 140 | checkDoneTaskTalkNpc: npc.tempId == 62 VÀ mapId == 140 |
| 1 | `TASK_49_1` = 100354 | **B14** | Quyết định số phận bản sao | 1 | 62 | 140 | Bước khớp index — người chơi tới đây là đã chọn 'Thu nhận' ở TASK_31_1 và đã nhận 1 Bình chứa Commeson (638) bản KHÔNG hạn sử dụng |
| 2 | `TASK_49_2` = 100356 | **A6** | Tới Võ đài Xên bọ hung | 1 | — | 103 | checkDoneTaskGoToMap: mapId == 103 |
| 3 | `TASK_49_3` = 100358 | **B13** | Gọi một nhân chứng vào võ đài | 1 | — | 103 | checkDoneTaskTogetherInZone: zone của map 103 có >= 2 Player thật |
| 4 | `TASK_49_4` = 100360 | **A2** | Đánh gục Bản sao của ngươi | 1 | — | 103 | checkDoneTaskKillBoss: boss bản sao — ở nhánh này boss KHÔNG die(): hp <= 0 -> hp = 1, changeStatus(NON_PK), giữ trong khu 180 giây |
| 5 | `TASK_49_5` = 100362 | **A12** | Thu nhận nó bằng Bình chứa Commeson | 1 | — | 103 | checkDoneTaskUseItem: template 638 VÀ mapId == 103 VÀ bản sao của chính mình đang NON_PK trong khu |
| 6 | `TASK_49_6` = 100364 | **A5** | Đạt 2.000.000.000 sức mạnh | 1 | — | — | checkDoneTaskPower: power >= 2.000.000.000 — kiểm lại ngay trong sendNextSubTask |

#### NV 50 — Trả lại hay giữ lấy  *(chương 6, 6 bước, thưởng 1.500.000.000 SM & TN)*

| # | Hằng | Kiểu | Tên bước | SL | NPC | Map | Điều kiện server kiểm tra |
|---|---|---|---|---|---|---|---|
| 0 | `TASK_50_0` = 102400 | **B14** | Quyết định số phận của Lõi | 1 | 64 | 145 | Bước được đánh dấu xong NGAY khi switchTaskMain(player, 50, 1) chạy — giữ dòng này để subTasks.size() khớp |
| 1 | `TASK_50_1` = 102402 | **A12** | Hấp thụ Lõi Hư Không | 1 | 64 | 145 | checkDoneTaskUseItem: template 2000 VÀ mapId == 145 -> GIỮ NGUYÊN item 2000 (không trừ), gắn cờ hậu truyện |
| 2 | `TASK_50_2` = 102404 | **A6** | Về Hành tinh ngục tù | 1 | — | 155 | checkDoneTaskGoToMap: mapId == 155 |
| 3 | `TASK_50_3` = 102406 | **B10** | Mở giới hạn sức mạnh lần cuối | 1 | 43 | 50 | checkDoneTaskOpenPower: openPowerByTask — chỉ chạy khi getIdTask == TASK_50_3 VÀ limitPower == 2, limitPower 2 -> 3 |
| 4 | `TASK_50_4` = 102408 | **A3** | Nghe Ôsin nói lời cuối | 1 | 44 | 155 | checkDoneTaskTalkNpc: npc.tempId == 44 VÀ mapId == 155 |
| 5 | `TASK_50_5` = 102410 | **A2** | Hạ Hư Không Vô Danh | 1 | — | 155 | checkDoneTaskKillBoss: boss -108108, currentLevel == 3, map 155 (mapJoin form 4 = {145, 155}, chọn theo taskMain.id của người trong khu) |

---

## 5. Bảng chuyển tiếp task id

`TaskService.sendNextTaskMain` phải dùng đúng bảng này (thay cho nhánh `id == 3 → gender + 4` của tuyến cũ):

```
id 20 → 21        id 48 → 21
id 31 → 32        id 49 → 32
id 47 → HẾT       id 50 → HẾT
còn lại → id + 1
```

Với `id 47` và `id 50`: **không** gọi `getTaskMainById(id + 1)` (task 48 / 51 không phải nhiệm vụ kế tiếp
của tuyến — 48 là nhánh của NV 20, còn 51 không tồn tại). Thay vào đó: trao thưởng kết, trao danh hiệu,
gửi thông báo, **giữ nguyên `taskMain`** để người chơi vẫn xem lại được nhiệm vụ cuối.

**Điểm rẽ nhánh — cách chuyển:**

| Điểm rẽ | Ở bước | NPC mở menu | Chọn A (giữ task) | Chọn B (đổi task) | Hội tụ |
|---|---|---|---|---|---|
| 1 | `TASK_20_1` | 71 Berry, map 160 | giữ task **20**, `index = 2` | `switchTaskBranch(player, 48)` → task **48**, `index = 2` | NV 21 |
| 2 | `TASK_31_1` | 62 Potage, map 140 | giữ task **31**, `index = 2` | `switchTaskBranch(player, 49)` → task **49**, `index = 2`, trao 1 item 638 | NV 32 |
| 3 | `TASK_47_0` | 64 Thiên Sứ Whis, map 145 | giữ task **47**, `index = 1` | `switchTaskMain(player, 50, 1)` → task **50**, `index = 1` | kết tuyến |

`switchTaskBranch` / `switchTaskMain` đều phải: nạp `getTaskMainById(player, newId)`, đặt `index`,
`count = 0`, `lastTime = 0`, rồi `sendTaskMain` (message 40). Hai task nhánh có **cùng số bước ở phần
đầu** với task gốc nên cú nhảy không làm giật giao diện.

---

## 6. Trigger mới phải viết + điểm móc

| Mã | Hàm mới trong `TaskService` | Điểm móc | Bước dùng |
|---|---|---|---|
| **B1** | `checkDoneTaskDungeon(Player, int typeMap)` | `map/phoban/RedRibbonHQ` (chỗ `winDT = true`); `map/phoban/SnakeWay.finish()` + `CADICH.leaveMap()` (chỗ `endCDRD = true`); `map/phoban/DestronGas.finish()` + cờ `hatchiyatchDead`. Duyệt **mọi** `Player` trong instance | `TASK_21_1`, `TASK_35_3`, `TASK_43_4` |
| **B2** | `checkDoneTaskUpgradeItem(Player, Item)` | `combine/NangCapVatPham` nhánh roll thành công; điều kiện `item.getOptionParam(72) >= 2` | `TASK_17_1` |
| **B3** | `checkDoneTaskCombine(Player, int type)` | `combine/PhaLeHoaTrangBi` (option 107 ≥ 1) và `combine/EpSaoTrangBi` (option 102 ≥ 1) | `TASK_26_1`, `TASK_26_2` |
| **B4** | `checkDoneTaskBuyItem(Player, int itemTemplateId, int shopId)` | `shop/ShopService` — sau khi trừ tiền **và** `addItemBag` thành công | `TASK_8_1` (item 12, shop 1/2/3), `TASK_14_1` (mọi item, shop 4 URON) |
| **B5** | `checkDoneTaskWinMatch(Player, int typePvp)` | `matches/ThachDau.reward(winner)`; `matches/dai_hoi_vo_thuat/DeathOrAliveArena` (chỗ `haveRewardVDST = true`); `WorldMartialArtsTournament` (chỗ `martialArtsTournamentWins++`) | `TASK_44_2` |
| **B6** | `checkDoneTaskWishDragon(Player, int starType)` | `services/shenron/SummonDragon.confirmWish`, ngay **trước** `lastTimeShenronAppeared = now`; chỉ tính Rồng Thần 1 Sao | `TASK_39_2` |
| **B7** | `checkDoneTaskHarvestPea(Player, int soHat)` | `npc/MagicTree.harvestPea` sau `addPeaHarvest`; cộng `addDoneSubTask(player, soHat)` chứ không phải 1 | `TASK_11_0` |
| **B8** | `checkDoneTaskHavePet(Player)` | `services/PetService.createNewPet` **và** `player/Player.update` | `TASK_12_0` |
| **B9** | `checkDoneTaskLearnSkill(Player, Skill)` | `services/SkillService` sau khi `skill.point` tăng | `TASK_10_1` |
| **B10** | `checkDoneTaskOpenPower(Player)` + hàm MỚI `OpenPowerService.openPowerByTask(Player)` | `services/OpenPowerService` sau `limitPower++` ở **cả ba** nhánh; nhánh mới lộ qua menu "Phá giới hạn (nhiệm vụ)" ở `npc_list/QuocVuong` và `npc_list/ToSuKaio` | `TASK_33_3` (0→1), `TASK_44_4` (1→2), `TASK_47_3` / `TASK_50_3` (2→3) |
| **B11** | `checkDoneTaskBasePoint(Player, int type)` | `player/NPoint.increasePoint(type, point)` — mở rộng `checkDoneTaskNangCS` vốn chỉ xét `type 2` | `TASK_33_2` (HP gốc ≥ `getHpMpLimit()`) |
| **B12** | `startTimedSubTask` / `updateTimedSubTask` / `resetTimedSubTask` | `TaskMain.lastTime` (đã nằm trong `data_task` phần tử thứ 4, `PlayerDAO` đã lưu); kiểm trong `Player.update`; gửi thời gian còn lại qua message **43** | `TASK_7_0` (180.000 ms), `TASK_23_2` (300.000), `TASK_29_2` (360.000), `TASK_34_2` (300.000), `TASK_42_2` (600.000) |
| **B13** | `checkDoneTaskTogether(Player, Zone, int nMember, boolean requireSameClan)` và biến thể `checkDoneTaskTogetherInZone` gọi định kỳ trong `Player.update` | đếm `Player` thật trong `zone` (`!isBot && !isPet && !isBoss`) | `TASK_13_1` (cùng bang), `TASK_19_3` (không cần cùng bang, ×2 tiến độ), `TASK_31_3` / `TASK_49_3`, `TASK_35_1` (cùng bang), `TASK_45_2` (không cần cùng bang) |
| **B14** | `switchTaskBranch(Player, int newTaskId)` / `switchTaskMain(Player, int newTaskId, int newIndex)` | menu `npc_list/Berry`, `npc_list/Potage`, `npc_list/DaiThienSu` + bảng chuyển tiếp trong `sendNextTaskMain` | `TASK_20_1`, `TASK_31_1`, `TASK_47_0` |
| **A12** | `checkDoneTaskUseItem` — **hàm đã có, `switch` đang RỖNG** | `services_func/UseItem.java` | `case 13` → `TASK_0_4`; `2010` → `TASK_5_1`; `2013` → `TASK_11_1`; `2015` → `TASK_17_2`; `2020` → `TASK_26_3`; `638` → `TASK_49_5`; `992` → `TASK_32_1`; `2002` → `TASK_40_3`; `2024` → `TASK_45_3`; `2000` → `TASK_47_1` / `TASK_50_1` |

### 6.1 Bước có HAI đường hoàn thành (theo khung giờ phó bản)

Ba bước dưới đây cố ý có nhiều điều kiện OR ghi vào **cùng một hằng số**, nên `data_task` vẫn chỉ lưu
`[id, index, count, lastTime]`, không cần cờ phụ. Người chơi tự chọn đường bằng việc bấm NPC lúc nào.

| Bước | Đường chính (theo giờ) | Đường vòng 24/7 | Cách cộng tiến độ |
|---|---|---|---|
| `TASK_36_2` (SL 20) | boss **-233 Drabura**, map 114, khung 12:00–12:59 | mob **95 / 118 Cadic M**, map 165 | boss → `addDoneSubTask(player, 20)`; mỗi Cadic M → `+1` |
| `TASK_37_1` (SL 1) | boss **-236 Mabư** (map 120, 12h) hoặc **-214** `currentLevel 4` Kid Bư (map 127, 14h) | mob **70 Hirudegarn**, map 126 | đều `+1` |
| `TASK_37_2` (SL 30) | boss **-343 Drabura 3** (map 120) hoặc **-348 Super Bư** (map 128) | mob **50 Quỷ chim**, map 126 | boss → `addDoneSubTask(player, 30)`; mỗi Quỷ chim → `+1` |

`TASK_36_1`, `TASK_36_3`, `TASK_36_4` cũng có hai cửa (map 114/117 hoặc map 165), ghi rõ ở bảng §4.

---

## 7. Thay đổi Java bắt buộc đi kèm dữ liệu

### 7.1 Hai placeholder map phải sửa — nếu không, mũi tên chỉ đường sai

`SRC/src/nro/models/services/TaskService.java:1134` `transformMapId`:

| Placeholder | Hiện trả về | Phải trả về | Vì sao |
|---|---|---|---|
| `MAP_VACH_NUI` (-4) | 39 / 40 / 41 | **42 / 43 / 44** | Map 39/40/41 và 42/43/44 **trùng tên** trong `map_template` ("Vách núi Aru / Moori"), nhưng Jaco (NV 3), Bà Hạt Mít (NV 17) và Quốc Vương (NV 33) đứng ở **42/43/44** |
| `MAP_500` (-5) | *(không xử lý — trả nguyên -5 xuống client)* | **2 / 9 / 16** | NV 4 diễn ra ở 2 Thung lũng tre / 9 Thị trấn Moori / 16 Làng Plant; placeholder văn bản `%6` đã đúng, chỉ thiếu phần map |

Tuyến cũ bị xóa sạch nên đổi nghĩa hai hằng này **không ảnh hưởng dữ liệu nào khác**.

### 7.2 Bảng thưởng đọc từ DB — thay `rewardDoneTask` hardcode

`task_main_reward` có thêm cột `gender` so với đề xuất §9.2 của file 20:

| `gender` | Nghĩa |
|---|---|
| `-1` | áp dụng cho cả 3 hành tinh — **luôn là dòng chứa `sm` / `tn`** |
| `0` / `1` / `2` | chỉ Trái Đất / Namếc / Xayda — **chỉ chứa vật phẩm**, `sm = tn = 0` |

DAO phải cộng dồn mọi dòng `WHERE task_id = ? AND sub_index = ? AND gender IN (-1, player.gender)`.
Tách như vậy để trang bị cấp 1 (NV 3) và sách chưởng / sách đấm (NV 10) không phải nhân ba bản ghi
sức mạnh, tránh cộng đúp.

**Hai id đặc biệt trong cột `items`**: `2030` và `2031` là **danh hiệu TYPE 36**, không phải item
hành trang. Khi gặp hai id này, DAO **không** gọi `addItemBag` mà phải làm:

```java
new BadgesData(player, 257, 36500);          // 2030 -> idEffect 257 ; 2031 -> 258
BadgesService.turnOnBadges(player, 257);
// KHÔNG gọi thêm player.dataBadges.add(...) — constructor BadgesData đã tự add
```

### 7.3 Lỗi cũ chặn cứng — phải sửa trước khi bật tuyến

Rút gọn từ [20c §7.1](../2-thiet-ke-nhiem-vu-moi/20c-chi-tiet-nhiem-vu-32-47.md) và [22 §6.1]; đây là
những chỗ khiến **bước nhiệm vụ không bao giờ xong** dù dữ liệu đúng:

| Bước bị kẹt | Nguyên nhân | File |
|---|---|---|
| `TASK_34_4` | `Cooler.reward()` không gọi `checkDoneTaskKillBoss` | `boss/Cold/Cooler.java` |
| `TASK_39_6` | `Baby.reward()` không gọi nhiệm vụ | `boss/Baby/Baby.java` |
| `TASK_41_2` | `Broly.die()` chỉ đặt `DIE`, không `reward()` | `boss/Broly/Broly.java` |
| `TASK_41_3` | `SuperBroly.reward()` chỉ tạo đệ tử | `boss/Broly/SuperBroly.java` |
| `TASK_43_2`, `TASK_43_3` | `DrLychee.reward()` / `Hatchiyack.reward()` không gọi nhiệm vụ | `boss/khi_gas/*.java` |
| `TASK_36_3` | `Osin.confirmMenu` để `case 114..120` bên trong `switch(indexMenu)` của map 52 | `npc_list/Osin.java` |
| `TASK_44_1` | `Bill.confirmMenu` dùng `case` theo `mapId` thay vì `indexMenu` | `npc_list/Bill.java` |
| `TASK_45_2`, `TASK_45_4`, `TASK_46_5`, **`TASK_47_0`** | `DaiThienSu` (NPC 64) có `openBaseMenu` và `confirmMenu` **rỗng** — đây là NPC mở **điểm rẽ nhánh cuối** | `npc_list/DaiThienSu.java` |
| `TASK_42_0` | map 155 mở ở `TASK_42_0` nhưng lối vào duy nhất đi qua map 154 (mở ở `TASK_44_0`) — phải thêm nút "Đến hành tinh ngục tù" cho Ôsin ở map 50 | `npc_list/Osin.java` |
| `TASK_32_1` | map 156–159 đòi SM ≥ 40 tỷ ở NPC 47 Giu-ma, trong khi NV 32 ở mốc 2,5 tỷ | `npc_list/GiuMaDauBo.java` |
| `TASK_33_1`, `TASK_33_3` | NPC 42 Quốc Vương chỉ hiện khi SM ≥ 17 tỷ; `openPowerBasic` đòi đã chạm trần; nhiệm vụ không phát vàng nên không dùng được `openPowerSpeed` (50 triệu vàng) | `npc_list/QuocVuong.java`, `services/OpenPowerService.java` |
| `TASK_12_0` | không có nguồn đệ tử miễn phí — phải thêm menu "Nở trứng" cho NPC 50 | `npc_list/QuaTrung.java` |

### 7.4 Dữ liệu `map_template` phải bổ sung

| Việc | Map | Phục vụ bước |
|---|---|---|
| Thêm NPC **63 Jaco** | 42, 43, 44 và 25, 26 | `TASK_3_0`, `TASK_7_2`, `TASK_15_2`, `TASK_48_2` |
| Thêm NPC **42 Quốc Vương** | 42, 44 | `TASK_33_1`, `TASK_33_3`, `TASK_33_5` |
| Thêm NPC **76 Granola** (+ class + `ConstNpc.GRANOLA`) | 160 | `TASK_20_2`, `TASK_20_5` |
| Thêm NPC **83 Dr. Myuu** | 166 (map hiện **không có NPC nào**) | `TASK_29_3`, `TASK_46_0` |
| Thêm NPC **70 Bardock** (ẩn/hiện theo `getIdTask`) | 14 | `TASK_39_5` |
| Thêm **waypoint** 166 ↔ 97 | 166 | `TASK_29_1`, `TASK_29_2` |
| Rải `ItemMap` 2023 (5–8 viên/khu, **không** tự biến mất sau 50 s) | 166 | `TASK_29_2` |
| *(khuyến nghị)* đổi map **50** từ `MAP_OFFLINE` sang type 0 | 50 | NV 40, 41, 42, 44, 47 đều dừng chân ở đây; type 1 chỉ 1 khu / 15 người |

---

## 8. Đường cong sức mạnh — kiểm chứng lại §6.1

Con số SM trong file này **không lấy nguyên** từ 20a/20b/20c. Lý do: tổng thưởng của bản thiết kế gốc
là **≈ 99 tỷ SM**, trong khi trần sức mạnh sau ba lần mở giới hạn chỉ là **29.999.999.999**, nên
`Service.addSMTN` sẽ **cắt bỏ khoảng 70% phần thưởng của chương 5–6** — người chơi làm nhiệm vụ mà
không thấy sức mạnh tăng. Đã tính lại để tổng cộng dồn bám đúng mốc §6.1.

| Chương | Thưởng hoàn thành | Thưởng theo bước (10%) | Cộng dồn tới cuối chương | Mốc §6.1 |
|---|---|---|---|---|
| 1 — Ngày ký ức vỡ | 60.000 | 18.400 | **78.400** | ~65.000 |
| 2 — Kẻ trộm ký ức | 1.840.000 | 552.000 | **2.470.400** | ~2.000.000 |
| 3 — Liên minh ba hành tinh | 65.000.000 | 33.500.000 | **100.970.400** | ~100.000.000 |
| 4 — Cỗ máy và những bản sao | 1.290.000.000 | 623.000.000 | **2.013.970.400** | ~2 tỷ |
| 5 — Vương triều bóng tối | 3.500.000.000 | 2.080.000.000 | **7.593.970.400** | ~8 tỷ |
| 6 — Lõi Hư Không | 7.250.000.000 | 4.135.000.000 | **18.978.970.400** | ~18 tỷ |

Ba lần mở giới hạn vì thế vẫn **bắt buộc**, không phải trang trí:

| Mốc | Bước | `limitPower` | Trần SM sau khi mở |
|---|---|---|---|
| NV 33 | `TASK_33_3` | 0 → 1 | 17.999.999.999 → **19.999.999.999** — mốc cuối tuyến là ~19 tỷ nên **không mở là bị cắt thưởng** |
| NV 44 | `TASK_44_4` | 1 → 2 | → **24.999.999.999** — dư địa cho phần cày sau chương 6 |
| NV 47 / 50 | `TASK_47_3` / `TASK_50_3` | 2 → 3 | → **29.999.999.999** — phần thưởng kết tuyến, mở đường cho nội dung hậu truyện |

**Khối lượng cày** (không tính thời gian chờ boss hồi sinh và phó bản theo ngày):

| Chương | Quái phải giết | Boss phải hạ | Ghi chú |
|---|---|---|---|
| 1 | 102 | 2 (`-2000`, và các bước A2 khác) | tân thủ, quái 20–600 HP |
| 2 | 140 | 1 (`-2001`, 2 form) | |
| 3 | 240 | 12 (3 Nappa + 5 TĐST + 3 form Fide) | + 1 lượt phó bản Doanh Trại |
| 4 | 200 | 11 | Xên bọ hung 1 con/server nghỉ 30 phút là nút thắt |
| 5 | 270 | 12 | phụ thuộc khung giờ 12h / 14h / 20h |
| 6 | 200 | 13 (kể cả 4 form Heart) | + phó bản CĐRĐ và Khí gas |

Tổng ~1.150 lượt giết quái và ~51 lượt hạ boss. Thời gian thực tế bị chi phối bởi **mốc sức mạnh**
(18–19 tỷ) và **thời gian hồi sinh boss / giới hạn phó bản theo ngày**, không phải số quái — đúng
mốc "1–2 tháng" của [22 §0 mục 5](22-san-sang-code.md).

---

## 9. Chỗ nghi ngờ

Những chỗ tôi **phải tự quyết** vì tài liệu thiết kế mâu thuẫn với quyết định đã chốt hoặc với mã nguồn
thật. Chủ dự án nên đọc hết mục này trước khi cho lên server.

| # | Vấn đề | Tôi đã làm gì | Rủi ro / cần chốt lại |
|---|---|---|---|
| 1 | **"Bộ Thần Linh trao dưới dạng mảnh"** (22 §0 mục 4) nhưng **không tồn tại item "Mảnh Thần Linh"**, và bảng id [25](25-bang-id-vat-pham-moi.md) đã chốt cứng 2000–2031, không còn chỗ trống | Dùng bộ mảnh **Thiên Sứ** sẵn có: 1066 Mảnh áo, 1067 Mảnh quần, 1068 Mảnh giầy, 1069 Mảnh nhẫn, 1070 Mảnh găng tay. Mỗi món cần **999 mảnh** đổi ở Whis (map 154 — chính là map của NV 44). Tuyến trao **400/999 mỗi loại** (150 ở NV 40–44, 100 ở NV 46, 150 ở NV 47/50), người chơi tự cày nốt 599 | Bộ **Thiên Sứ mạnh hơn bộ Thần Linh**. Trao 40% một bộ Thiên Sứ có thể giá trị hơn phát thẳng 5 món Thần Linh. Nếu chủ dự án thấy lạm phát, phương án sạch hơn là **tạo 5 item mới 2032–2036 "Mảnh Thần Linh"** ở một file patch sau và đổi lại `ID_TASK_ITEM_MAX` |
| 2 | **Thưởng SM của 20a/20b/20c tổng ≈ 99 tỷ**, trần sau 3 lần mở giới hạn là 30 tỷ | Tính lại toàn bộ SM chương 3–6 để cộng dồn bám đúng §6.1 (xem [§8](#8-đường-cong-sức-mạnh--kiểm-chứng-lại-61)). Chương 1–2 giữ nguyên số của 20a | Con số từng nhiệm vụ giờ **khác 20b/20c**. Nếu chủ dự án muốn giữ đúng số cũ thì phải chấp nhận ~70% thưởng chương 5–6 bị `addSMTN` cắt |
| 3 | **`MAP_VACH_NUI` (-4) trỏ sai map** (39/40/41 thay vì 42/43/44) và **`MAP_500` (-5) không được xử lý** | Vẫn dùng hai placeholder đó trong dữ liệu để một bản ghi dùng chung 3 hành tinh, và ghi rõ hai chỗ phải sửa `transformMapId` ([§7.1](#71-hai-placeholder-map-phải-sửa--nếu-không-mũi-tên-chỉ-đường-sai)) | Nếu nhóm code **quên sửa**, nhiệm vụ vẫn chạy nhưng NV 3/4/17/33 chỉ sai chỗ. Phương án thay thế: đổi hai dòng đó về `map = -1` (mất mũi tên) |
| 4 | Thiết kế trao **Ngọc Rồng** ở NV 45 (1 bộ) và NV 47/50 (2 bộ) — vi phạm quyết định "không Ngọc Rồng" | Thay bằng vật phẩm: NV 45 → 5 Đá ngũ sắc + 50 Đậu thần cấp 8; NV 47/50 → 20 Đá bảo vệ + 99 Đậu thần cấp 8 + mảnh trang bị | Giá trị quy đổi là **ước lượng**, chủ dự án nên chốt lại số lượng |
| 5 | Thiết kế trao **Thỏi vàng (457)** ở NV 13 và NV 14 | Bỏ hẳn — 457 là tiền trá hình (`gold = 500.000.000`) và đang nằm trong lỗ hổng ký gửi ([22 §6.3](22-san-sang-code.md)). Thay bằng Gói 30 đậu thần cấp 3 và Đá bảo vệ / Đá nâng cấp cấp 1 | — |
| 6 | Thiết kế trao **Capsule Vàng (574)** ở NV 23 và **Sao pha lê đen cấp 2 (964)** ở NV 38 — cả hai là **thưởng chết**: 574 không có code xử lý khi dùng, 964 cộng 0 chỉ số ([22 §6.2](22-san-sang-code.md)) | Thay bằng 5 Đá bảo vệ (NV 23) và 2 Sao pha lê lục (NV 38) | Nếu nhóm 24a sửa được hai lỗi kia thì nên trả về thưởng gốc cho đúng mạch chuyện |
| 7 | **`TASK_39_1` bắt gom đủ 7 viên Ngọc Rồng** rồi `TASK_39_2` bắt ước rồng — có thể mất nhiều ngày cày | Giữ đúng thiết kế (đây là bước dạy tính năng rồng thần) | Đây là **bước chậm nhất tuyến**. Cần cân nhắc đường vòng, hoặc chấp nhận vì đúng mốc "1–2 tháng" |
| 8 | **Bước `TASK_36_2` và `TASK_37_2` có hai đường với khối lượng rất khác nhau** (1 boss so với 50 / 30 quái) | Đặt `max_count` = 20 và 30, boss cộng trọn gói, quái cộng từng con ([§6.1](#61-bước-có-hai-đường-hoàn-thành-theo-khung-giờ-phó-bản)) | Tỉ lệ quy đổi 1 boss = 20 (hoặc 30) quái là **tôi tự đặt**, cần test thực tế |
| 9 | **`TASK_21_1` (Doanh Trại), `TASK_35_3` (CĐRĐ), `TASK_43_4` (Khí gas)** đều đòi bang ≥ 5 người / vào bang ≥ 2 ngày / 1 lần trên 7 ngày — người chơi lẻ **kẹt cứng tuyến chính** | Giữ nguyên bước (phó bản là nội dung chính của chương), **chưa** viết đường vòng | **Rủi ro cao nhất của cả tuyến.** Cần chủ dự án chốt: nới ràng buộc khi `getIdTask` đang ở đúng bước, hay cho B1 chấp nhận bất kỳ phó bản nào trong 4 loại |
| 10 | **`TASK_13_1`, `TASK_19_3`, `TASK_31_3`, `TASK_35_1`, `TASK_45_2`** cần người chơi thứ hai online cùng khu | Giữ nguyên (đây là điểm "cuốn" của thiết kế) | Server vắng người là **kẹt**. 20b đề xuất đường vòng sau 3–7 ngày, chưa được duyệt |
| 11 | Bảng thưởng có thêm **cột `gender`** so với §9.2 của file 20 | Thêm, vì trang bị cấp 1 và sách kỹ năng khác nhau theo hành tinh mà cột `items` chỉ là một JSON duy nhất | DAO phải nhớ cộng dồn `gender IN (-1, player.gender)`; quên là người chơi **không nhận được trang bị NV 3 / sách NV 10** |
| 12 | **Danh hiệu 2030 / 2031 nằm trong cột `items`** nhưng không phải item hành trang | Ghi rõ ở [§7.2](#72-bảng-thưởng-đọc-từ-db--thay-rewarddonetask-hardcode) rằng DAO phải chuyển sang `BadgesData` + `turnOnBadges`, 36500 ngày | Nếu DAO gọi `addItemBag` cho 2030/2031, người chơi nhận một item TYPE 36 vô dụng trong túi và **không có danh hiệu** |
| 13 | **`idEffect` 257 / 258 chưa có tài nguyên** (`DataEffect_257/258`, `ImgEffect_257/258.png`) | Không xử lý được bằng SQL | Danh hiệu **cộng chỉ số nhưng tàng hình**. Cần vẽ 10 file trước khi phát hành ([25 §3.6](25-bang-id-vat-pham-moi.md)) |
| 14 | **`ducvupro` là khóa chính AUTO_INCREMENT** và `Manager` nạp task bằng JOIN **không có `ORDER BY`** | Ghi `ducvupro` tăng dần đúng thứ tự `(task_main_id, index)`; câu kiểm tra 5.5 của file SQL bắt lỗi nếu khoảng `ducvupro` của một nhiệm vụ bị chèn bước của nhiệm vụ khác | Nếu sau này ai đó **chèn / xóa bước** bằng tay, thứ tự có thể vỡ và `TaskService` tra nhầm `index`. Nên thêm luôn `ORDER BY task_main_id, ducvupro` vào `Manager` |
| 15 | **Chưa chạy trên DB `team2026` thật** | Đã import thử toàn bộ 02 và 03 trên MySQL 8.4 với bảng rỗng dựng lại đúng schema của dump, cả 8 + 4 câu kiểm tra đều đạt | Vẫn phải chạy trên **bản sao** của `team2026` trước khi làm trên bản chính |

---

*Hết file 27. File SQL: [02-nhiem-vu-moi.sql](../../SRC/sql/patch/02-nhiem-vu-moi.sql) ·
[03-reset-tien-do.sql](../../SRC/sql/patch/03-reset-tien-do.sql).*
