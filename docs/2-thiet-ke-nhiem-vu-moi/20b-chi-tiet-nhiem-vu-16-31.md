# 20b — Chi tiết nhiệm vụ 16 → 31 (Chương 3 & Chương 4)

> Phụ lục của [20-thiet-ke-nhiem-vu-moi.md](20-thiet-ke-nhiem-vu-moi.md). Bám đúng bảng tổng §5, đường cong thưởng §6, nhánh §7, khóa map §8.
> File anh em: [20a](20a-chi-tiet-nhiem-vu-00-15.md) (NV 0–15) · [20c](20c-chi-tiet-nhiem-vu-32-47.md) (NV 32–47).
>
> **Trạng thái: BẢN THIẾT KẾ — chưa có dòng code nào được sửa.**
> Mọi id map / mob / npc / boss / item trong file này đều tra từ [02b](../1-he-thong-hien-tai/02b-database-du-lieu-template.md), [11](../1-he-thong-hien-tai/11-boss.md), [11b](../1-he-thong-hien-tai/11b-boss-su-kien-pho-ban.md), [14](../1-he-thong-hien-tai/14-ban-do-pho-ban.md), [18](../1-he-thong-hien-tai/18-npc.md), [08](../1-he-thong-hien-tai/08-nang-cap-do-dap-do.md). Item chưa tồn tại được đánh dấu **ITEM MỚI**.

## Mục lục

1. [Bảng tóm tắt 16 nhiệm vụ + 2 nhánh](#1-bảng-tóm-tắt-16-nhiệm-vụ--2-nhánh)
2. [Quy ước đọc bảng bước](#2-quy-ước-đọc-bảng-bước)
3. [Chương 3 — Liên minh ba hành tinh (NV 16–23)](#3-chương-3--liên-minh-ba-hành-tinh-nv-1623)
   - [NV 16 — Dấu vết dẫn về phía Nam](#nv-16--dấu-vết-dẫn-về-phía-nam)
   - [NV 17 — Rèn lại vũ khí](#nv-17--rèn-lại-vũ-khí)
   - [NV 18 — Tapion](#nv-18--tapion)
   - [NV 19 — Trại lính hoang](#nv-19--trại-lính-hoang)
   - [NV 20 — Kẻ săn tiền thưởng (nhánh Granola)](#nv-20--kẻ-săn-tiền-thưởng-nhánh-granola)
   - [NV 48 — Kẻ săn tiền thưởng (nhánh Jaco)](#nv-48--kẻ-săn-tiền-thưởng-nhánh-jaco)
   - [NV 21 — Doanh trại Độc Nhãn](#nv-21--doanh-trại-độc-nhãn)
   - [NV 22 — Tiểu đội sát thủ](#nv-22--tiểu-đội-sát-thủ)
   - [NV 23 — Fide đại ca](#nv-23--fide-đại-ca)
4. [Chương 4 — Cỗ máy và những bản sao (NV 24–31)](#4-chương-4--cỗ-máy-và-những-bản-sao-nv-2431)
   - [NV 24 — Tín hiệu lạ từ phương Bắc](#nv-24--tín-hiệu-lạ-từ-phương-bắc)
   - [NV 25 — Android đầu tiên](#nv-25--android-đầu-tiên)
   - [NV 26 — Kim loại và ký ức](#nv-26--kim-loại-và-ký-ức)
   - [NV 27 — Ba cỗ máy](#nv-27--ba-cỗ-máy)
   - [NV 28 — King Kong](#nv-28--king-kong)
   - [NV 29 — Phòng thí nghiệm Myuu](#nv-29--phòng-thí-nghiệm-myuu)
   - [NV 30 — Xên bọ hung](#nv-30--xên-bọ-hung)
   - [NV 31 — Bản sao của chính ngươi (nhánh Tiêu diệt)](#nv-31--bản-sao-của-chính-ngươi-nhánh-tiêu-diệt)
   - [NV 49 — Bản sao của chính ngươi (nhánh Thu nhận)](#nv-49--bản-sao-của-chính-ngươi-nhánh-thu-nhận)
5. [Ghi chú / việc cần làm khi code](#5-ghi-chú--việc-cần-làm-khi-code)

---

## 1. Bảng tóm tắt 16 nhiệm vụ + 2 nhánh

| NV | Tên | Chương | Số bước | Kiểu bước | Map chính | Boss / mốc chính | SM khi xong | NV kế |
|---|---|---|---|---|---|---|---|---|
| 16 | Dấu vết dẫn về phía Nam | 3 | 5 | A6, A1, A1, A4, A3 | 29/33/37, 30/34/38 | — | 5.000.000 | 17 |
| 17 | Rèn lại vũ khí | 3 | 4 | A3, **B2**, A12, A3 | 42/43/44, 5/13/20 | Nâng trang bị lên +2 | 8.000.000 | 18 |
| 18 | Tapion | 3 | 5 | A6, A3, A1, A6, A3 | 19, 6/10/19, 126 | — | 12.000.000 | 19 |
| 19 | Trại lính hoang | 3 | 5 | A3, A1, A1, **B13**, A3 | 68–72 | — | 20.000.000 | 20 / 48 |
| **20** | Kẻ săn tiền thưởng *(Granola)* | 3 | 6 | A3, **B14**, A3, A2, A4, A3 | 160, 63–77 | Kuku −20, Mập Đầu Đinh −21, Rambo −22 | 30.000.000 | 21 |
| **48** | Kẻ săn tiền thưởng *(Jaco)* | 3 | 6 | A3, **B14**, A3, A2, A4, A3 | 160, 24, 63–77 | Kuku −20, Mập Đầu Đinh −21, Rambo −22 | 30.000.000 | 21 |
| 21 | Doanh trại Độc Nhãn | 3 | 4 | A3, **B1**, A3, A3 | 27 → 53–62 | Phó bản Doanh Trại Độc Nhãn | 50.000.000 | 22 |
| 22 | Tiểu đội sát thủ | 3 | 5 | A3, A1, A2, A4, A3 | 79, 81–83, 126 | TĐST −23…−27 | 70.000.000 | 23 |
| **23** | **Fide đại ca** | 3 | 6 | A5, A6, **B12**, A2, A2, A3 | 80 Núi khỉ vàng | Fide đại ca −28 (3 form) | 100.000.000 | 24 |
| 24 | Tín hiệu lạ từ phương Bắc | 4 | 5 | A3, A6, A1, A1, A3 | 92–96, 102 | — | 200.000.000 | 25 |
| 25 | Android đầu tiên | 4 | 4 | A6, A2, A4, A3 | 93/94/96 | Android 19 −30, Dr.Kôrê −31 | 300.000.000 | 26 |
| 26 | Kim loại và ký ức | 4 | 5 | A3, **B3**, **B3**, A12, A3 | 5 Đảo Kamê, 5/13/20 | Pha lê hóa + ép sao | 400.000.000 | 27 |
| 27 | Ba cỗ máy | 4 | 4 | A3, A6, A2, A3 | 104 Sân sau siêu thị | Android 15 −34, 13 −32, 14 −33 | 500.000.000 | 28 |
| 28 | King Kong | 4 | 5 | A6, A1, A2, A4, A3 | 97–99 | Poc −36, Pic −35, King Kong −37 | 800.000.000 | 29 |
| 29 | Phòng thí nghiệm Myuu | 4 | 4 | A3, A6, **B12**, A3 | 166 | Đồng hồ 6 phút | 1.000.000.000 | 30 |
| 30 | Xên bọ hung | 4 | 5 | A6, A1, A2, A2, A3 | 100 Thị trấn Ginder | Xên bọ hung −100 (3 form) | 1.400.000.000 | 31 |
| **31** | **Bản sao của chính ngươi** *(Tiêu diệt)* | 4 | 6 | A3, **B14**, A6, **B13**, A2, A5 | 140, 103 | Bản sao người chơi (boss mới) | 2.000.000.000 | 32 |
| **49** | **Bản sao của chính ngươi** *(Thu nhận)* | 4 | 7 | A3, **B14**, A6, **B13**, A2, A12, A5 | 140, 103 | Bản sao người chơi (boss mới) | 2.000.000.000 | 32 |

**Kiểm tra nhịp theo §4.3**

| Ràng buộc | Chương 3 | Chương 4 |
|---|---|---|
| Đúng 1 bước B12 | NV 23 bước 2 | NV 29 bước 2 |
| Đúng 1 bước B13 | NV 19 bước 3 | NV 31/49 bước 3 |
| Không quá 2 bước giết liên tiếp | ✔ (NV 16: A1+A1; NV 19: A1+A1 rồi B13) | ✔ (NV 24: A1+A1; NV 30: A2+A2) |
| 3–6 bước / nhiệm vụ, cuối chương 5–7 | ✔ (NV 23: 6) | ✔ (NV 31: 6, NV 49: 7) |
| Bước đầu ngắn | ✔ mọi NV mở bằng A3 / A6 / A5 | ✔ |

---

## 2. Quy ước đọc bảng bước

| Cột | Ý nghĩa |
|---|---|
| **#** | `task_sub_template` index, bắt đầu từ 0 |
| **Kiểu** | Mã trigger theo §4 của file 20 (A1–A12 đã có, B1–B14 cần thêm code) |
| **Điều kiện server kiểm tra** | Hàm/điều kiện thật trong `TaskService.java` + id thật |
| **Map / NPC** | `X/Y/Z` = theo hành tinh Trái Đất / Namếc / Xayda (dùng placeholder `map`/`npc_id` âm của `ConstTask`) |
| **Hằng số** | `ConstTask.TASK_<id>_<index>` — giá trị `((id<<10)+index)<<1` |
| **Thưởng bước** | Theo §6.3: mỗi bước trả **10%** mức thưởng của cả nhiệm vụ (đi qua `addDoneSubTask`) |

Ba sư phụ theo hành tinh, dùng chung placeholder `NPC_QUY_LAO = -5` / `MAP_QUY_LAO = -9`:

| Hành tinh | Sư phụ (npc id) | Map |
|---|---|---|
| Trái Đất | Quy Lão Kame (13) | 5 Đảo Kamê |
| Namếc | Trưởng lão Guru (14) | 13 Đảo Guru |
| Xayda | Vua Vegeta (15) | 20 Vách núi đen |

Khối id vật phẩm mới được **chia trước** cho 3 file chi tiết để không đụng nhau (id lớn nhất trong `item_template` hiện tại là **1999**):

| Khối | Dùng cho |
|---|---|
| 2001–2007 | 7 Mảnh Ký Ức (#1 → #7), dùng chung cả 3 file |
| 2020–2039 | Vật phẩm riêng của 20a (NV 0–15) |
| **2040–2069** | **Vật phẩm riêng của 20b — chương 3** |
| **2070–2099** | **Vật phẩm riêng của 20b — chương 4** |
| 2100+ | Vật phẩm riêng của 20c (NV 32–47) |

---

## 3. Chương 3 — Liên minh ba hành tinh (NV 16–23)

> **Mạch chương:** Máy dò của Bunma chỉ về phía Nam. Người chơi lần theo dấu, gặp **Tapion** (NPC 53) — người đến từ một quá khứ đã bị xóa sạch, không ai còn nhớ tên. Ba hành tinh vốn không ưa nhau buộc phải ngồi lại khi phát hiện quân Fide đang **thu gom ký ức thay cho Heart**. Chương kết bằng chiến thắng lớn đầu tiên: Fide đại ca ngã xuống, và lần đầu tiên cả ba hành tinh cùng đứng dưới một lá cờ.

### NV 16 — Dấu vết dẫn về phía Nam

**Chương** 3 — Liên minh ba hành tinh | **Mốc SM khi xong** 5.000.000 | **Nhiệm vụ kế** 17

*Máy dò ký ức của Bunma rung lên lần đầu tiên sau nhiều ngày im lặng: một nguồn hút ký ức đang di chuyển về phía Nam. Người chơi bám theo tín hiệu, và thứ tìm thấy không phải kẻ trộm — mà là những vỏ đạn có khắc dấu của quân Fide.*

| # | Tên bước (hiển thị cho người chơi) | Kiểu | Điều kiện server kiểm tra | Số lượng | Map (id + tên) | NPC (id + tên) | Hằng số | Thưởng bước |
|---|---|---|---|---|---|---|---|---|
| 0 | Lần theo tín hiệu về phía Nam | A6 | `checkDoneTaskGoToMap` — vào map 29 (TĐ) / 33 (NM) / 37 (XD) | 1 | 29 Nam Kamê / 33 Nam Guru / 37 Thung lũng đen | — | `TASK_16_0` | 200.000 SM + 200.000 TN |
| 1 | Dọn sạch 40 %4 chắn đường | A1 | `checkDoneTaskKillMob` — mob tempId 31 Không tặc (TĐ) / 32 Quỷ đầu to (NM) / 33 Quỷ địa ngục (XD), lv7 | 40 | 29 / 33 / 37 | — | `TASK_16_1` | 200.000 SM + 200.000 TN |
| 2 | Tiêu diệt 30 %4 canh bờ biển | A1 | `checkDoneTaskKillMob` — mob tempId 22 Bulon (TĐ) / 23 Ukulele (NM) / 24 Quỷ mập (XD), lv8 | 30 | 30 Đảo Bulông / 34 Đông Nam Guru / 38 Bờ vực đen | — | `TASK_16_2` | 200.000 SM + 200.000 TN |
| 3 | Nhặt 5 Vỏ đạn khắc dấu | A4 | `checkDoneTaskPickItem` — item **ITEM MỚI 2040 "Vỏ đạn khắc dấu"** (icon mượn 1421 của Mảnh đá vụn 225); rơi 25% từ mob 22/23/24 khi đang ở `TASK_16_3` | 5 | 30 / 34 / 38 | — | `TASK_16_3` | 200.000 SM + 200.000 TN |
| 4 | Mang vỏ đạn về cho %10 | A3 | `checkDoneTaskTalkNpc` — npc 13 Quy Lão Kame / 14 Trưởng lão Guru / 15 Vua Vegeta | 1 | 5 / 13 / 20 | 13 / 14 / 15 (sư phụ) | `TASK_16_4` | 200.000 SM + 200.000 TN |

- **Lời thoại**
  - *(bước 0, hệ thống — máy dò của Bunma npc 7)* "Máy dò kêu rồi! Tín hiệu đi về phía Nam, nhanh lên trước khi nó tắt!"
  - *(bước 2, sư phụ 13/14/15 nhắn)* "Bọn này không cướp vàng. Chúng cướp thứ khác — và ta không nhớ nổi là thứ gì."
  - *(bước 3, hệ thống)* "Trên vỏ đạn có một con dấu. Con dấu ấy quen đến mức làm ngươi lạnh sống lưng."
  - *(bước 4, sư phụ 13/14/15)* "Dấu của quân Fide. Ta tưởng chúng tan rã từ lâu... sao ta lại 'tưởng' nhỉ?"
  - *(bước 4, sư phụ 13/14/15)* "Đi tìm bà Hạt Mít. Vũ khí của ngươi chưa đủ để chạm vào bọn đó."
- **Mô tả nhiệm vụ (cột `detail` DB)**: "Máy dò ký ức chỉ về phía Nam. Lần theo dấu vết và mang bằng chứng về cho %10. Thưởng: 2.000.000 sức mạnh, 2.000.000 tiềm năng, 20 Đá nâng cấp cấp 1."
- **Thưởng khi hoàn thành**: 2.000.000 SM + 2.000.000 TN · Đá nâng cấp cấp 1 (1074) ×20.
- **Mở khóa**: map **27–38** (§8: mốc cũ `TASK_13_0`/`TASK_15_0` → mốc mới **`TASK_16_0`**).
- **Ghi chú triển khai**:
  - Thêm **ITEM MỚI 2040** vào `item_template` + tăng version data client.
  - Thêm luật rơi trong `Mob.getItemMobReward`: gated bằng `TaskService.isCurrentTask(player, ConstTask.TASK_16_3)` (theo tiền lệ `TASK_8_1` rơi Ngọc Rồng 7 sao).
  - `ChangeMapService.checkMapCanJoin`: đổi mốc khóa map 27–38 sang `TASK_16_0`.

---

### NV 17 — Rèn lại vũ khí

**Chương** 3 — Liên minh ba hành tinh | **Mốc SM khi xong** 8.000.000 | **Nhiệm vụ kế** 18

*Bà Hạt Mít nhìn vỏ đạn rồi im lặng rất lâu. Bà bảo: muốn đánh lại quân Fide thì phải biết nâng cấp đồ — và bà đưa cho người chơi cây búa rèn cũ của chồng mình, người mà bà không còn nhớ mặt. Đây là nhiệm vụ dạy tính năng nâng cấp trang bị, kèm nguyên liệu miễn phí.*

| # | Tên bước (hiển thị cho người chơi) | Kiểu | Điều kiện server kiểm tra | Số lượng | Map (id + tên) | NPC (id + tên) | Hằng số | Thưởng bước |
|---|---|---|---|---|---|---|---|---|
| 0 | Gặp Bà Hạt Mít ở %5 | A3 | `checkDoneTaskTalkNpc` — npc 21 Bà Hạt Mít; trao miễn phí Đá Titan (223) ×10, Đá Ruby (222) ×10, Đá bảo vệ (987) ×2 | 1 | 42 Vách núi Aru / 43 Vách núi Moori / 44 Vách núi Kakarot | 21 Bà Hạt Mít | `TASK_17_0` | 400.000 SM + 400.000 TN |
| 1 | Nâng một trang bị lên +2 | **B2** | **Trigger mới** — `combine/NangCapVatPham.nangCapVatPham` thành công và option 72 (cấp) của trang bị đạt `>= 2` | 1 | 42 / 43 / 44 (hoặc 84 Siêu Thị) | 21 Bà Hạt Mít | `TASK_17_1` | 400.000 SM + 400.000 TN |
| 2 | Dùng Búa rèn cũ | A12 | `checkDoneTaskUseItem` — item **ITEM MỚI 2041 "Búa rèn cũ"** (icon mượn 1417 của Đá Titan 223); item được trao ở bước 0 | 1 | bất kỳ | — | `TASK_17_2` | 400.000 SM + 400.000 TN |
| 3 | Khoe vũ khí mới với %10 | A3 | `checkDoneTaskTalkNpc` — npc 13 / 14 / 15 | 1 | 5 / 13 / 20 | 13 / 14 / 15 (sư phụ) | `TASK_17_3` | 400.000 SM + 400.000 TN |

- **Lời thoại**
  - *(bước 0, npc 21 Bà Hạt Mít)* "Con dấu này... ta từng rèn giáp cho người mang nó. Hay là ta rèn cho người chống lại nó?"
  - *(bước 0, npc 21)* "Cầm lấy đá. Miễn phí. Chỉ cần ngươi nhớ giùm ta cái tên ta đã quên."
  - *(bước 1, npc 21 khi nâng cấp thành công)* "Đó! Nghe tiếng kim loại kêu chưa? Kim loại nhớ dai hơn người đấy."
  - *(bước 2, hệ thống)* "Cây búa còn ấm. Trong đầu ngươi hiện lên một gương mặt đàn ông — rồi tắt ngay."
  - *(bước 3, sư phụ 13/14/15)* "Vũ khí sắc rồi. Nhưng ngươi sắp gặp một người còn bị quên lãng hơn cả bà ấy."
- **Mô tả nhiệm vụ (cột `detail` DB)**: "Bà Hạt Mít dạy ngươi cách nâng cấp trang bị và tặng nguyên liệu để thử. Thưởng: 4.000.000 sức mạnh, 4.000.000 tiềm năng, 30 Đá nâng cấp cấp 1, 3 Đá bảo vệ."
- **Thưởng khi hoàn thành**: 4.000.000 SM + 4.000.000 TN · Đá nâng cấp cấp 1 (1074) ×30 · Đá bảo vệ (987) ×3.
- **Mở khóa**: tính năng **Nâng cấp trang bị** (menu "Nâng cấp Vật phẩm" ở Bà Hạt Mít map 42/43/44/84) hiện ra từ `TASK_17_0`.
- **Ghi chú triển khai**:
  - **B2**: móc vào `combine/NangCapVatPham.java`, nhánh thành công (sau khi `option 72` +1) → gọi `TaskService.checkDoneTaskUpgradeItem(player, item)`; điều kiện `item.getOptionParam(72) >= 2`.
  - **A12**: `checkDoneTaskUseItem` hiện có `switch` rỗng — thêm `case 2041 -> doneTask(player, ConstTask.TASK_17_2)` trong `services_func/UseItem.java`.
  - Thêm **ITEM MỚI 2041** (type 27, gender 3, không giao dịch).
  - Bảng nâng cấp +0→+1 cần 3 đá / 10.000 vàng (80%), +1→+2 cần 7 đá / 70.000 vàng (50%) → 20 viên mỗi loại là dư dả, người chơi không bị kẹt.

---

### NV 18 — Tapion

**Chương** 3 — Liên minh ba hành tinh | **Mốc SM khi xong** 12.000.000 | **Nhiệm vụ kế** 19

*Ở Thành phố Vegeta có một chàng trai ngồi thổi hộp nhạc suốt ngày, không ai nhớ nổi anh ta đến từ đâu. Tapion là người duy nhất ngoài người chơi còn giữ được ký ức — vì hộp nhạc của anh giữ hộ. Anh dẫn người chơi tới Thành phố Santa, nơi quá khứ bị xóa để lại một vết sẹo.*

| # | Tên bước (hiển thị cho người chơi) | Kiểu | Điều kiện server kiểm tra | Số lượng | Map (id + tên) | NPC (id + tên) | Hằng số | Thưởng bước |
|---|---|---|---|---|---|---|---|---|
| 0 | Tới Thành phố Vegeta | A6 | `checkDoneTaskGoToMap` — vào map 19 | 1 | 19 Thành phố Vegeta | — | `TASK_18_0` | 700.000 SM + 700.000 TN |
| 1 | Nói chuyện với người lạ thổi nhạc | A3 | `checkDoneTaskTalkNpc` — npc 53 Tapion, yêu cầu `player.zone.map.mapId == 19` | 1 | 19 Thành phố Vegeta | 53 Tapion | `TASK_18_1` | 700.000 SM + 700.000 TN |
| 2 | Diệt 30 %14 đang vây thành | A1 | `checkDoneTaskKillMob` — mob tempId 25 Tambourine (TĐ) / 26 Drum (NM) / 27 Akkuman (XD), lv8 | 30 | 6 Đông Karin / 10 Thung lũng Namếc / 19 Thành phố Vegeta | — | `TASK_18_2` | 700.000 SM + 700.000 TN |
| 3 | Đi cùng Tapion tới Thành phố Santa | A6 | `checkDoneTaskGoToMap` — vào map 126 | 1 | 126 Thành phố Santa | — | `TASK_18_3` | 700.000 SM + 700.000 TN |
| 4 | Nghe Tapion kể chuyện | A3 | `checkDoneTaskTalkNpc` — npc 53 Tapion, yêu cầu `mapId == 126` | 1 | 126 Thành phố Santa | 53 Tapion | `TASK_18_4` | 700.000 SM + 700.000 TN |

- **Lời thoại**
  - *(bước 1, npc 53 Tapion)* "Ngươi nhìn thấy ta à? Thật sự nhìn thấy? Ba tháng nay không ai nhìn thấy ta cả."
  - *(bước 1, npc 53)* "Hộp nhạc này nhớ hộ ta. Ta thổi để khỏi quên tên em trai mình."
  - *(bước 3, npc 53)* "Thành phố Santa từng có bốn vạn người. Bây giờ chỉ còn tên gọi trên bản đồ."
  - *(bước 4, npc 53)* "Chúng không giết ai. Chúng chỉ lấy đi phần khiến ta là ta. Như vậy tệ hơn."
  - *(bước 4, npc 53)* "Phía Bắc có một trại lính bỏ hoang. Lính ở đó đã quên cả việc mình đang canh gì."
- **Mô tả nhiệm vụ (cột `detail` DB)**: "Tìm Tapion — người đến từ một quá khứ đã bị xóa — và đi cùng anh tới Thành phố Santa. Thưởng: 7.000.000 sức mạnh, 7.000.000 tiềm năng, 20 Đá nâng cấp cấp 2."
- **Thưởng khi hoàn thành**: 7.000.000 SM + 7.000.000 TN · Đá nâng cấp cấp 2 (1075) ×20.
- **Mở khóa**: map **6, 10, 19** (§8: mốc cũ `TASK_16_0` → mốc mới **`TASK_18_0`**).
- **Ghi chú triển khai**:
  - `checkDoneTaskTalkNpc` cần thêm `TAPION` (npc 53) — hiện chưa có nhánh nào cho NPC này. Phân biệt bước 1 và 4 bằng `mapId` (19 vs 126).
  - Map 126 Thành phố Santa đang có mob **Hirudegarn (tempId 70, lv0, HP 40.000.000)** — big boss, sát thương phản 10% maxHp. Không đưa vào điều kiện nhiệm vụ, nhưng cần cảnh báo ở lời thoại để người chơi 12M SM không đứng cạnh nó.
  - `ChangeMapService.checkMapCanJoin`: đổi mốc khóa map 6/10/19 sang `TASK_18_0`.

---

### NV 19 — Trại lính hoang

**Chương** 3 — Liên minh ba hành tinh | **Mốc SM khi xong** 20.000.000 | **Nhiệm vụ kế** 20 *(hoặc 48 sau khi rẽ nhánh ở NV 20)*

*Trại lính của Nappa vẫn còn đèn, còn cờ, còn lính đứng gác — nhưng không ai nhớ mình đang gác cái gì. Chúng tấn công bất cứ thứ gì động đậy. Cui (NPC 12) nhận ra đây không phải quân kỷ luật, mà là những cái vỏ rỗng, và nó đông đến mức một mình không dọn nổi.*

| # | Tên bước (hiển thị cho người chơi) | Kiểu | Điều kiện server kiểm tra | Số lượng | Map (id + tên) | NPC (id + tên) | Hằng số | Thưởng bước |
|---|---|---|---|---|---|---|---|---|
| 0 | Gặp Cui ở Thung lũng Nappa | A3 | `checkDoneTaskTalkNpc` — npc 12 Cui, yêu cầu `mapId == 68` | 1 | 68 Thung lũng Nappa | 12 Cui | `TASK_19_0` | 1.000.000 SM + 1.000.000 TN |
| 1 | Hạ 60 Nappa mất trí | A1 | `checkDoneTaskKillMob` — mob tempId 39 Nappa, lv9 | 60 | 68 Thung lũng Nappa, 69 Vực cấm, 70 Núi Appule | — | `TASK_19_1` | 1.000.000 SM + 1.000.000 TN |
| 2 | Hạ 40 Soldier gác kho | A1 | `checkDoneTaskKillMob` — mob tempId 40 Soldier, lv9 | 40 | 69 Vực cấm, 70 Núi Appule | — | `TASK_19_2` | 1.000.000 SM + 1.000.000 TN |
| 3 | Cùng một người khác hạ 30 Appule | **B13** | **Trigger mới** — `checkDoneTaskKillMob` mob tempId 41 Appule, lv9, **chỉ cộng tiến độ khi trong cùng `zone` có ≥ 2 người chơi thật** (`NMEMBER_DO_TASK_TOGETHER`, theo tiền lệ nhiệm vụ bang 14/15); ×2 tiến độ mỗi mạng | 30 | 71 Căn cứ Raspberry, 72 Thung lũng Raspberry | — | `TASK_19_3` | 1.000.000 SM + 1.000.000 TN |
| 4 | Báo cáo với Cui | A3 | `checkDoneTaskTalkNpc` — npc 12 Cui, yêu cầu `mapId == 68` | 1 | 68 Thung lũng Nappa | 12 Cui | `TASK_19_4` | 1.000.000 SM + 1.000.000 TN |

- **Lời thoại**
  - *(bước 0, npc 12 Cui)* "Ta hỏi tên chỉ huy của chúng. Nó đáp: 'Tôi... đang gác. Gác cái gì thì tôi quên rồi.'"
  - *(bước 0, npc 12)* "Đừng vào một mình. Chỗ Raspberry đông lắm, hai người mới dọn nổi."
  - *(bước 3, hệ thống)* "Có đồng đội bên cạnh, tiến độ đi nhanh gấp đôi. Rủ thêm người đi!"
  - *(bước 4, npc 12 Cui)* "Ngươi dọn sạch rồi à? Vậy mà ta vẫn thấy có kẻ đang đứng nhìn từ hang động."
  - *(bước 4, npc 12)* "Hắn tên Granola. Thợ săn tiền thưởng. Hắn biết kẻ nào đứng sau trại lính này."
- **Mô tả nhiệm vụ (cột `detail` DB)**: "Trại lính Nappa đã mất trí và tấn công tất cả. Dọn sạch trại — và nhớ rủ thêm một người. Thưởng: 10.000.000 sức mạnh, 10.000.000 tiềm năng, 30 Đá nâng cấp cấp 2."
- **Thưởng khi hoàn thành**: 10.000.000 SM + 10.000.000 TN · Đá nâng cấp cấp 2 (1075) ×30.
- **Mở khóa**: map **63–77** (§8: mốc cũ `TASK_18_0`/`TASK_19_0` → mốc mới **`TASK_19_0`**).
- **Ghi chú triển khai**:
  - **B13**: mở rộng `checkDoneTaskKillMob` — đếm số `Player` thật (không bot/pet/boss) trong `player.zone`; nếu `< 2` thì **không cộng count** và bắn thông báo "Bước này cần làm cùng người khác". Nếu `>= 2` thì `addDoneSubTask(player, 2)`.
  - Đây là bước B13 **duy nhất của chương 3** (§4.3).
  - `checkDoneTaskTalkNpc` cho `CUI` (12) hiện chỉ xử lý bước 17_1 với `mapId == 19` — cần thêm nhánh `mapId == 68`.

---

### NV 20 — Kẻ săn tiền thưởng *(nhánh Granola)*

**Chương** 3 — Liên minh ba hành tinh | **Mốc SM khi xong** 30.000.000 | **Nhiệm vụ kế** 21

*Granola (NPC 76) không quan tâm tới ký ức của ai. Hắn chỉ muốn Heart chết, vì Heart đã xóa sạch cả hành tinh của hắn — và đau nhất là hắn không còn nhớ hành tinh đó trông thế nào. Hắn đề nghị hợp tác: săn ba tay chân của Fide, lấy tiền thưởng, không hỏi han pháp luật.*

> **Đây là điểm rẽ nhánh 1 (§7).** Bước 1 là một bước `B14`: chọn **đi theo Granola** (giữ task id **20**) hoặc **báo cho Jaco** (chuyển sang task id **48**). Hai nhánh có **cùng giá trị thưởng, khác loại thưởng**, cùng hội tụ về NV 21.

| # | Tên bước (hiển thị cho người chơi) | Kiểu | Điều kiện server kiểm tra | Số lượng | Map (id + tên) | NPC (id + tên) | Hằng số | Thưởng bước |
|---|---|---|---|---|---|---|---|---|
| 0 | Tìm kẻ lạ ở Khu hang động | A3 | `checkDoneTaskTalkNpc` — npc 71 Berry, yêu cầu `mapId == 160` | 1 | 160 Khu hang động | 71 Berry | `TASK_20_0` | 1.400.000 SM + 1.400.000 TN |
| 1 | Chọn cách xử lý: Granola hay Jaco | **B14** | **Trigger mới** — menu NPC 71 Berry: chọn [0] "Đi theo Granola" → giữ task 20, `index = 2`; chọn [1] "Báo cảnh sát vũ trụ Jaco" → `switchTaskBranch(player, 48)` đặt task id 48, `index = 2` | 1 | 160 Khu hang động | 71 Berry | `TASK_20_1` | 1.400.000 SM + 1.400.000 TN |
| 2 | Bắt tay với Granola | A3 | `checkDoneTaskTalkNpc` — npc 76 Granola, yêu cầu `mapId == 160` | 1 | 160 Khu hang động | 76 Granola | `TASK_20_2` | 1.400.000 SM + 1.400.000 TN |
| 3 | Thanh toán 3 tay chân của Fide | A2 | `checkDoneTaskKillBoss` — đếm chung 3 boss: **Kuku (−20)**, **Mập Đầu Đinh (−21)**, **Rambo (−22)**, mỗi con 1 form | 3 | 68–72 (Kuku), 63–67 (Mập Đầu Đinh), 74–77 (Rambo) | — | `TASK_20_3` | 1.400.000 SM + 1.400.000 TN |
| 4 | Nhặt 3 Thẻ tiền thưởng | A4 | `checkDoneTaskPickItem` — item **ITEM MỚI 2042 "Thẻ tiền thưởng Granola"** (icon mượn 5428 của Bản đồ kho báu 611); rơi 100% từ boss −20/−21/−22 khi đang ở `TASK_20_4` | 3 | 63–77 | — | `TASK_20_4` | 1.400.000 SM + 1.400.000 TN |
| 5 | Nhận tiền thưởng từ Granola | A3 | `checkDoneTaskTalkNpc` — npc 76 Granola, yêu cầu `mapId == 160` | 1 | 160 Khu hang động | 76 Granola | `TASK_20_5` | 1.400.000 SM + 1.400.000 TN |

- **Lời thoại**
  - *(bước 0, npc 71 Berry)* "Đừng lại gần! Anh ấy bắn trước, hỏi sau. Mà dạo này anh ấy cũng chẳng hỏi nữa."
  - *(bước 1, npc 71 Berry)* "Ngươi chọn đi: đi với thợ săn, hay báo cảnh sát? Cả hai đều đúng, và đều mất một thứ."
  - *(bước 2, npc 76 Granola)* "Ta không cần ngươi tin ta. Ta cần ngươi bắn trúng. Ba cái đầu, chia đôi tiền."
  - *(bước 2, npc 76)* "Heart xóa hành tinh ta. Giờ ta không nhớ nổi mẹ ta trông ra sao. Ngươi hiểu chưa?"
  - *(bước 5, npc 76)* "Cầm lấy. Đá này ta cạy từ giáp của chúng — dùng được, mà bẩn tay."
  - *(bước 5, npc 76)* "Fide có một doanh trại ở phía Tây. Bản đồ nằm trong đó. Ngươi tự lo đi."
- **Mô tả nhiệm vụ (cột `detail` DB)**: "Granola đề nghị hợp tác săn ba tay chân của Fide, không cần giấy phép. Thưởng: 14.000.000 sức mạnh, 14.000.000 tiềm năng, 40 Đá nâng cấp cấp 2."
- **Thưởng khi hoàn thành**: 14.000.000 SM + 14.000.000 TN · **Đá nâng cấp cấp 2 (1075) ×40** *(nhánh thợ săn nghiêng về nguyên liệu đập đồ)*.
- **Mở khóa**: boss nhóm Nappa (**Kuku −20, Mập Đầu Đinh −21, Rambo −22**) vào bảng nhiệm vụ; NPC **Granola (76)** được đặt vào map 160.
- **Ghi chú triển khai**:
  - **B14**: thêm `TaskService.switchTaskBranch(Player, int newTaskId)` — gán `playerTask.taskMain = getTaskMainById(player, newTaskId)`, đặt `index`, `count = 0`, gửi `sendTaskMain`. Móc vào menu của `npc_list/Berry.java`.
  - Bảng chuyển tiếp trong `sendNextTaskMain`: `20 → 21`, **`48 → 21`**.
  - **Granola (npc 76) chưa có class và chưa có trong `map_template`** ([18-npc.md](../1-he-thong-hien-tai/18-npc.md)) → cần viết `npc_list/Granola.java`, thêm hằng `ConstNpc.GRANOLA = 76`, và thêm `[76, x, y]` vào cột `npcs` của map 160.
  - Boss `Kuku`, `MapDauDinh`, `Rambo` hiện gọi `TASK_19_0/19_1/19_2` — đổi sang `TASK_20_3` (đếm chung, `maxCount = 3`) và `TASK_48_3`.
  - **Lưu ý cày**: 3 boss này hồi sinh 10 phút, `autoLeaveMap` **rời map sau 15 phút kể từ khi vào map bất kể có người hay không** ([11-boss.md](../1-he-thong-hien-tai/11-boss.md) §5.1) — nên để `maxCount = 3` đếm chung thay vì bắt giết đúng thứ tự, tránh kẹt.

---

### NV 48 — Kẻ săn tiền thưởng *(nhánh Jaco)*

**Chương** 3 — Liên minh ba hành tinh | **Mốc SM khi xong** 30.000.000 | **Nhiệm vụ kế** 21

*Người chơi chọn con đường có giấy tờ: báo cho cảnh sát vũ trụ Jaco (NPC 63). Jaco phát lệnh truy nã hợp lệ, và ba tay chân của Fide bị hạ dưới danh nghĩa pháp luật — chậm hơn, ồn hơn, nhưng để lại một biên bản mà cả Ngân Hà phải công nhận.*

> Nhánh song sinh của NV 20. Bước 0 và 1 **giống hệt** task 20 (để cú chuyển task không bị giật màn hình), khác nhau từ bước 2.

| # | Tên bước (hiển thị cho người chơi) | Kiểu | Điều kiện server kiểm tra | Số lượng | Map (id + tên) | NPC (id + tên) | Hằng số | Thưởng bước |
|---|---|---|---|---|---|---|---|---|
| 0 | Tìm kẻ lạ ở Khu hang động | A3 | `checkDoneTaskTalkNpc` — npc 71 Berry, `mapId == 160` | 1 | 160 Khu hang động | 71 Berry | `TASK_48_0` | 1.400.000 SM + 1.400.000 TN |
| 1 | Chọn cách xử lý: Granola hay Jaco | **B14** | **Trigger mới** — bước này chỉ tồn tại để khớp index; người chơi tới đây là đã chọn Jaco (xem NV 20 bước 1) | 1 | 160 Khu hang động | 71 Berry | `TASK_48_1` | 1.400.000 SM + 1.400.000 TN |
| 2 | Trình báo cảnh sát vũ trụ Jaco | A3 | `checkDoneTaskTalkNpc` — npc 63 Jaco, yêu cầu `mapId == 24` | 1 | 24 Trạm tàu vũ trụ (Trái Đất) | 63 Jaco | `TASK_48_2` | 1.400.000 SM + 1.400.000 TN |
| 3 | Thi hành lệnh truy nã 3 mục tiêu | A2 | `checkDoneTaskKillBoss` — đếm chung **Kuku (−20)**, **Mập Đầu Đinh (−21)**, **Rambo (−22)** | 3 | 68–72, 63–67, 74–77 | — | `TASK_48_3` | 1.400.000 SM + 1.400.000 TN |
| 4 | Nhặt 3 Biên bản truy nã | A4 | `checkDoneTaskPickItem` — item **ITEM MỚI 2043 "Biên bản truy nã Ngân Hà"** (icon mượn 5428); rơi 100% từ boss −20/−21/−22 khi ở `TASK_48_4` | 3 | 63–77 | — | `TASK_48_4` | 1.400.000 SM + 1.400.000 TN |
| 5 | Nộp biên bản cho Jaco | A3 | `checkDoneTaskTalkNpc` — npc 63 Jaco, `mapId == 24` | 1 | 24 Trạm tàu vũ trụ | 63 Jaco | `TASK_48_5` | 1.400.000 SM + 1.400.000 TN |

- **Lời thoại**
  - *(bước 2, npc 63 Jaco)* "Jaco đây. Ngươi làm đúng. Thợ săn kia sẽ bắn cả nhân chứng đấy."
  - *(bước 2, npc 63)* "Ta cấp lệnh truy nã. Có lệnh thì cái chết của chúng được ghi vào hồ sơ, không phải vô danh."
  - *(bước 4, hệ thống)* "Mỗi biên bản có tên thật của kẻ bị hạ. Ít nhất trong giấy tờ, chúng vẫn còn là ai đó."
  - *(bước 5, npc 63 Jaco)* "Nhận hàng cấp phát của Ngân Hà đi. Đồ chuẩn, không phải thứ cạy ra từ xác quái."
  - *(bước 5, npc 63)* "Hồ sơ chỉ về một doanh trại phía Tây. Granola cũng sẽ tới đó."
- **Mô tả nhiệm vụ (cột `detail` DB)**: "Báo cho cảnh sát vũ trụ Jaco và thi hành lệnh truy nã ba tay chân của Fide. Thưởng: 14.000.000 sức mạnh, 14.000.000 tiềm năng, 20 Đá nâng cấp cấp 1, 8 Đá bảo vệ."
- **Thưởng khi hoàn thành**: 14.000.000 SM + 14.000.000 TN · **Đá nâng cấp cấp 1 (1074) ×20** · **Đá bảo vệ (987) ×8** *(nhánh pháp luật nhận quân trang cấp phát — tổng giá trị tương đương 40 Đá nâng cấp cấp 2 của nhánh Granola; xem ghi chú cân bằng nhánh)*.
- **Mở khóa**: giống NV 20 (boss nhóm Nappa vào bảng nhiệm vụ).
- **Ghi chú triển khai**:
  - **Jaco (npc 63)** hiện đứng ở map **24 Trạm tàu vũ trụ (Trái Đất)** và **139 Hành tinh Potaufeu**. Người Namếc/Xayda phải bay về map 24 (tàu vũ trụ đã mở từ NV 7). Nếu muốn ba hành tinh đối xứng thì thêm `[63, x, y]` vào `npcs` của map 25 và 26 rồi dùng placeholder `MAP_TTVT = -6` / `NPC_TTVT = -3`.
  - Task 48 phải được **sinh đủ trong `task_main_template` / `task_sub_template`** giống mọi task khác; `ConstTask` đã có sẵn `TASK_48_*`.
  - `sendNextTaskMain`: `48 → 21`.
  - **Cân bằng nhánh (sau khi bỏ thưởng vàng/ngọc):** nhánh 48 trước đây nhận **50 Ngọc + 20 Đá nâng cấp cấp 1**, nhánh 20 nhận **20 Ngọc + 40 Đá nâng cấp cấp 2**. Bỏ ngọc thì phần ngọc của nhánh 48 được quy đổi thành **vật phẩm tương đương: Đá bảo vệ (987) ×8** — vẫn đúng chất "Ngân Hà trả lương đàng hoàng" (đồ cấp phát, giữ đồ không tụt cấp) và **khác loại** với nguyên liệu thô của Granola, trong khi tổng giá trị hai nhánh vẫn ngang nhau (§7). Số lượng 8 là ước lượng theo giá quy đổi hiện hành — chủ dự án chốt lại con số cuối. Nếu chỉnh số lượng của một nhánh thì phải chỉnh cả hai.

---

### NV 21 — Doanh trại Độc Nhãn

**Chương** 3 — Liên minh ba hành tinh | **Mốc SM khi xong** 50.000.000 | **Nhiệm vụ kế** 22

*Bản đồ hành quân của Fide nằm trong két của Độc Nhãn, sau mười tầng tường thành. Đây là nhiệm vụ dạy phó bản bang hội: một mình không vào được, không phá nổi, và cũng không mang được gì ra.*

| # | Tên bước (hiển thị cho người chơi) | Kiểu | Điều kiện server kiểm tra | Số lượng | Map (id + tên) | NPC (id + tên) | Hằng số | Thưởng bước |
|---|---|---|---|---|---|---|---|---|
| 0 | Gặp Lính canh ở Rừng Bamboo | A3 | `checkDoneTaskTalkNpc` — npc 25 Lính canh, yêu cầu `mapId == 27` | 1 | 27 Rừng Bamboo | 25 Lính canh | `TASK_21_0` | 1.800.000 SM + 1.800.000 TN |
| 1 | Phá xong Doanh trại Độc Nhãn | **B1** | **Trigger mới** — trong `map/phoban/RedRibbonHQ.java`, tại chỗ đặt `winDT = true` (đã hạ hết quái + 5 boss ở **10 map 53, 58, 59, 60, 61, 62, 55, 56, 54, 57**) → duyệt mọi `Player` trong instance, gọi `TaskService.checkDoneTaskFinishDungeon(player, ConstMap.MAP_DOANH_TRAI)` | 1 | 53 Tường thành 1 → 58 → 59 → 60 → 61 → 62 → 55 → 56 → 54 → 57 Tầng 4 | — | `TASK_21_1` | 1.800.000 SM + 1.800.000 TN |
| 2 | Lấy bản đồ hành quân từ Độc Nhãn | A3 | `checkDoneTaskTalkNpc` — npc 26 Độc Nhãn, yêu cầu `mapId == 57` và `zone.winDT == true` | 1 | 57 Tầng 4 | 26 Độc Nhãn | `TASK_21_2` | 1.800.000 SM + 1.800.000 TN |
| 3 | Mang bản đồ về cho %10 | A3 | `checkDoneTaskTalkNpc` — npc 13 / 14 / 15 | 1 | 5 / 13 / 20 | 13 / 14 / 15 (sư phụ) | `TASK_21_3` | 1.800.000 SM + 1.800.000 TN |

**Điều kiện vào phó bản thật** (theo [14-ban-do-pho-ban.md](../1-he-thong-hien-tai/14-ban-do-pho-ban.md) §6.2, `LinhCanh.openBaseMenu` + `RedRibbonHQService.joinDoanhTrai`) — nhiệm vụ **không nới lỏng điều kiện nào**:

| # | Điều kiện | Ghi chú |
|---|---|---|
| 1 | Có bang + tài khoản đã kích hoạt (`session.actived`) | Bang đã mở ở NV 13 |
| 2 | Bang có **≥ 5 thành viên** (`N_PLAYER_CLAN = 5`) | |
| 3 | Đã vào bang **≥ 1 ngày** | `getNumDateFromJoinTimeToToday() >= 1` |
| 4 | Có **≥ 1 đồng đội cùng bang** đứng trong vùng **x 1285–1645** của map 27 (`N_PLAYER_MAP = 1`) | Người trong vùng được kéo vào cùng |
| 5 | Mỗi bang **1 lần/ngày** (`clan.haveGoneDoanhTrai`) | |
| 6 | Còn slot trong **50 instance** (`AVAILABLE`) | |

**Cách server nhận biết "đã phá xong"**: `RedRibbonHQ` chỉ đặt `winDT = true` khi **toàn bộ quái và 5 boss ở cả 10 map đều chết** (Trung uý Trắng −4 map 59, Trung uý Xanh Lơ −6 map 62, Trung uý Thép −5 map 55, Ninja Áo Tím −7 + phân thân map 54, 4 × Rôbốt Vệ Sĩ −8…−11 map 57), rồi phát thông báo "Mau đi tìm Độc Nhãn" và mở **5 phút nhặt ngọc** (`isTimePicking`, `TIME_PICK_DOANH_TRAI = 300.000ms`). Bước 1 chấm ở đúng thời điểm `winDT = true`, **không** chờ hết 5 phút nhặt — để người chơi bị rớt mạng giữa chừng vẫn có tiến độ.

- **Lời thoại**
  - *(bước 0, npc 25 Lính canh)* "Doanh trại kia à? Bang ngươi phải đủ 5 người, và phải có người đứng chờ ở đây."
  - *(bước 0, npc 25)* "Một bang một ngày một lần thôi. Vào rồi thì đừng chết vô ích."
  - *(bước 1, hệ thống khi `winDT`)* "Tường thành im rồi. Mau đi tìm Độc Nhãn ở Tầng 4!"
  - *(bước 2, npc 26 Độc Nhãn)* "Bản đồ? Cầm đi. Ta giữ nó mười năm mà chẳng nhớ mình giữ để làm gì."
  - *(bước 3, sư phụ 13/14/15)* "Đường này dẫn tới Tiểu đội sát thủ. Bọn đó không mất trí — bọn đó bị mua."
- **Mô tả nhiệm vụ (cột `detail` DB)**: "Bản đồ hành quân của Fide nằm trong Doanh trại Độc Nhãn — phá phó bản cùng bang hội để lấy nó. Thưởng: 18.000.000 sức mạnh, 18.000.000 tiềm năng, 20 Đá nâng cấp cấp 3, 2 Bản đồ kho báu."
- **Thưởng khi hoàn thành**: 18.000.000 SM + 18.000.000 TN · Đá nâng cấp cấp 3 (1076) ×20 · Bản đồ kho báu (611) ×2.
- **Mở khóa**: **phó bản Doanh Trại Độc Nhãn** chính thức vào tuyến chính (menu Lính canh hiện từ `TASK_21_0`); Bản đồ kho báu mở đường sang phó bản BĐKB.
- **Ghi chú triển khai**:
  - **B1**: thêm `TaskService.checkDoneTaskFinishDungeon(Player, int mapType)` và móc vào **nơi trao thưởng / kết thúc phó bản trong `services_dungeon/`** — cụ thể `map/phoban/RedRibbonHQ.java` (chỗ set `winDT`). Dùng chung hàm này cho NV 35 (Con đường rắn độc) và NV 43 (Khí gas) ở file 20c.
  - `npc_list/DocNhan.java` cần thêm nhánh `checkDoneTaskTalkNpc` cho `TASK_21_2`.
  - **Rủi ro kẹt tuyến**: bang < 5 người thì cả tuyến chính đứng lại. Đề xuất **đường vòng**: nếu người chơi ở `TASK_21_1` quá 7 ngày, Lính canh mở menu "Nhờ người khác dẫn" — tính hoàn thành khi người chơi đứng trong instance của bang **khác** lúc `winDT`. Cần bạn duyệt.
  - Trong doanh trại, quái rơi 20% **Cuốn chả giò (1778)** và 10% **Mảnh đá vụn (225)** — không đụng tới nhiệm vụ.

---

### NV 22 — Tiểu đội sát thủ

**Chương** 3 — Liên minh ba hành tinh | **Mốc SM khi xong** 70.000.000 | **Nhiệm vụ kế** 23

*Tiểu đội sát thủ không quên gì cả — chúng nhớ rất rõ, và chúng bán cái nhớ đó cho Heart lấy tiền. Tapion nhận ra máy đo của chúng không đo sức mạnh: nó đo **lượng ký ức** còn lại trong một người. Người chơi hiện lên vạch đỏ, và cả tiểu đội bắt đầu săn người chơi.*

| # | Tên bước (hiển thị cho người chơi) | Kiểu | Điều kiện server kiểm tra | Số lượng | Map (id + tên) | NPC (id + tên) | Hằng số | Thưởng bước |
|---|---|---|---|---|---|---|---|---|
| 0 | Hỏi Tapion về máy đo lạ | A3 | `checkDoneTaskTalkNpc` — npc 53 Tapion, `mapId == 126` | 1 | 126 Thành phố Santa | 53 Tapion | `TASK_22_0` | 2.400.000 SM + 2.400.000 TN |
| 1 | Hạ 40 lính khỉ canh đường | A1 | `checkDoneTaskKillMob` — mob tempId 54 Khỉ lông đen (lv13) hoặc 55 Khỉ giáp sắt (lv14), đếm chung | 40 | 81 Hang quỷ chim, 82 Núi khỉ đen, 83 Hang khỉ đen | — | `TASK_22_1` | 2.400.000 SM + 2.400.000 TN |
| 2 | Hạ trọn Tiểu đội sát thủ | A2 | `checkDoneTaskKillBoss` — đếm chung 5 boss: **Số 4 (−23)**, **Số 3 (−24)**, **Số 2 (−25)**, **Số 1 (−26)**, **Tiểu đội trưởng (−27)**; thứ tự bị ép bởi chuỗi AFK sẵn có (4 → 3 → 1&2 → TĐT) | 5 | 79 Núi khỉ đỏ, 81 Hang quỷ chim, 82 Núi khỉ đen, 83 Hang khỉ đen | — | `TASK_22_2` | 2.400.000 SM + 2.400.000 TN |
| 3 | Nhặt Máy đo ký ức | A4 | `checkDoneTaskPickItem` — item **ITEM MỚI 2044 "Máy đo ký ức"** (icon mượn 6467 của Đá ngũ sắc 674); rơi 100% từ **Tiểu đội trưởng (−27)** khi ở `TASK_22_3` | 1 | 79, 81, 82, 83 | — | `TASK_22_3` | 2.400.000 SM + 2.400.000 TN |
| 4 | Đưa máy đo cho Tapion | A3 | `checkDoneTaskTalkNpc` — npc 53 Tapion, `mapId == 126` | 1 | 126 Thành phố Santa | 53 Tapion | `TASK_22_4` | 2.400.000 SM + 2.400.000 TN |

- **Lời thoại**
  - *(bước 0, npc 53 Tapion)* "Cái máy đó không đo sức mạnh. Ta đã thấy chúng chĩa nó vào một đứa bé rồi cười."
  - *(bước 2, boss −27 Tiểu đội trưởng, textS)* "Vạch đỏ! Thằng này còn nguyên ký ức — Heart trả gấp mười cho hàng nguyên!"
  - *(bước 2, boss −27, textE)* "Đừng tưởng... ngươi giữ được nó... lâu đâu..."
  - *(bước 4, npc 53 Tapion)* "Kim chỉ vào ngươi thì vọt lên đỉnh. Ngươi là kho ký ức cuối cùng của cả vùng này."
  - *(bước 4, npc 53)* "Fide đang đợi ở Núi khỉ vàng. Lần này ta không đi cùng được. Đừng chết."
- **Mô tả nhiệm vụ (cột `detail` DB)**: "Tiểu đội sát thủ bị Heart mua chuộc để săn người còn ký ức — và ngươi đang ở đầu danh sách. Thưởng: 24.000.000 sức mạnh, 24.000.000 tiềm năng, 30 Đá nâng cấp cấp 3."
- **Thưởng khi hoàn thành**: 24.000.000 SM + 24.000.000 TN · Đá nâng cấp cấp 3 (1076) ×30.
- **Mở khóa**: map **79, 81, 82, 83** (§8: mốc cũ `TASK_19_0` → mốc mới **`TASK_22_0`**).
- **Ghi chú triển khai**:
  - 5 boss TĐST hiện gọi `TASK_20_1…20_5` — đổi hết sang `TASK_22_2` với `maxCount = 5` (đếm chung).
  - Tiểu đội trưởng −27 có đòn **đổi thân xác** (50% mỗi 10 giây, `EffectSkillService.setIsBodyChangeTechnique`) — giữ nguyên, đây là điểm nhấn của trận.
  - `autoLeaveMap` của cả nhóm là "15 phút không có người" → nhóm sẽ đứng chờ, không bị mất như nhóm Nappa.
  - Cần thêm **ITEM MỚI 2044** và luật rơi trong `Boss.reward` của `TDT.java`.

---

### NV 23 — Fide đại ca

**Chương** 3 — Liên minh ba hành tinh | **Mốc SM khi xong** 100.000.000 | **Nhiệm vụ kế** 24

*Cao trào chương 3. Fide không phải kẻ chủ mưu — hắn chỉ là kẻ giao hàng: gom ký ức của cả một vùng rồi nộp cho Heart để đổi lấy sự bất tử. Khi Fide ngã xuống ở Núi khỉ vàng, ba hành tinh lần đầu tiên đứng chung một trận tuyến, và người chơi cầm trong tay **Mảnh Ký Ức #3**. Đây là **chiến thắng lớn đầu tiên**.*

| # | Tên bước (hiển thị cho người chơi) | Kiểu | Điều kiện server kiểm tra | Số lượng | Map (id + tên) | NPC (id + tên) | Hằng số | Thưởng bước |
|---|---|---|---|---|---|---|---|---|
| 0 | Đạt 80.000.000 sức mạnh | A5 | `checkDoneTaskPower` — `power >= 80_000_000`; **kiểm tra lại ngay khi vừa sang bước** (xem ghi chú) | 1 | — | — | `TASK_23_0` | 3.000.000 SM + 3.000.000 TN |
| 1 | Tới Núi khỉ vàng | A6 | `checkDoneTaskGoToMap` — vào map 80 | 1 | 80 Núi khỉ vàng | — | `TASK_23_1` | 3.000.000 SM + 3.000.000 TN |
| 2 | Đốt kho tiếp tế: 25 Khỉ lông vàng trong 5 phút | **B12** | **Trigger mới** — `checkDoneTaskKillMob` mob tempId 57 Khỉ lông vàng (lv16, HP 450.000) ở map 80, **kèm đồng hồ**: mốc thời gian ghi vào `TaskMain.lastTime` khi vào bước; `Player.update` kiểm tra `System.currentTimeMillis() - lastTime > 300_000` → **`count = 0`**, `lastTime = now`, báo "Hết giờ! Kho tiếp tế được bổ sung lại." | 25 | 80 Núi khỉ vàng | — | `TASK_23_2` | 3.000.000 SM + 3.000.000 TN |
| 3 | Hạ Fide đại ca — hai hình dạng đầu | A2 | `checkDoneTaskKillBoss` — boss **Fide đại ca (−28)**, `currentLevel` 1 (Fide đại ca 1, HP 10M, dame 22.000) rồi 2 (Fide đại ca 2, HP 20M, dame 25.000) | 2 | 80 Núi khỉ vàng | — | `TASK_23_3` | 3.000.000 SM + 3.000.000 TN |
| 4 | Hạ Fide đại ca — hình dạng cuối | A2 | `checkDoneTaskKillBoss` — boss **Fide đại ca (−28)**, `currentLevel` 3 (Fide đại ca 3, HP 30M, dame 30.000) | 1 | 80 Núi khỉ vàng | — | `TASK_23_4` | 3.000.000 SM + 3.000.000 TN |
| 5 | Tuyên bố liên minh với %10 | A3 | `checkDoneTaskTalkNpc` — npc 13 / 14 / 15 | 1 | 5 / 13 / 20 | 13 / 14 / 15 (sư phụ) | `TASK_23_5` | 3.000.000 SM + 3.000.000 TN |

- **Lời thoại**
  - *(bước 2, hệ thống)* "Năm phút. Sau đó tàu tiếp tế của Fide hạ cánh và mọi thứ ngươi đốt đều mọc lại."
  - *(bước 3, boss −28 Fide đại ca, textS)* "Ngươi là cái kho ký ức biết đi mà Heart đặt hàng. Ta chỉ cần giao nguyên vẹn."
  - *(bước 3, boss −28, textE form 2)* "Ác quỷ biến hình... Graaaaa...."
  - *(bước 4, boss −28, textE form 3)* "Heart không cần ta nữa... nhưng hắn sẽ cần ngươi... nhiều hơn ngươi tưởng..."
  - *(bước 5, sư phụ 13/14/15)* "Ba hành tinh vừa đứng chung một hàng lần đầu. Ta không nhớ nổi lần trước là khi nào."
  - *(bước 5, sư phụ 13/14/15)* "Mảnh Ký Ức thứ ba là của ngươi. Nhưng phía Bắc có tiếng kim loại đang gõ."
- **Mô tả nhiệm vụ (cột `detail` DB)**: "Fide đại ca gom ký ức cả một vùng để bán cho Heart. Hạ cả ba hình dạng của hắn ở Núi khỉ vàng. Thưởng: 30.000.000 sức mạnh, 30.000.000 tiềm năng, 50 Đá nâng cấp cấp 3, 5 Capsule Vàng, Mảnh Ký Ức #3."
- **Thưởng khi hoàn thành**: 30.000.000 SM + 30.000.000 TN · Đá nâng cấp cấp 3 (1076) ×50 · Capsule Vàng (574) ×5 *(nguồn trang bị bộ)* · **Mảnh Ký Ức #3 — ITEM MỚI 2012** (icon mượn 421 của Ngọc Rồng 3 sao).
- **Mở khóa**: map **80 Núi khỉ vàng** (§8: mốc cũ `TASK_20_0` → mốc mới **`TASK_23_0`**); **Mảnh Ký Ức #3**; kết thúc chương 3.
- **Ghi chú triển khai**:
  - **B12** — đây là bước đếm giờ **duy nhất của chương 3** (§4.3). Dùng sẵn trường `TaskMain.lastTime` (đã được `PlayerDAO` ghi vào `data_task`) + kiểm tra trong `Player.update`. Luật: hết giờ **reset count về 0, không tụt bước, không mất đồ**.
  - `boss/Frieza/Fide.java` hiện gọi `TASK_21_1/2/3` theo form — đổi thành `TASK_23_3` (form 1 và 2, `maxCount = 2`) và `TASK_23_4` (form 3).
  - **Lỗi cũ cần sửa** ([12-nhiem-vu-chinh.md](../1-he-thong-hien-tai/12-nhiem-vu-chinh.md)): thiếu `break` ở nhánh khóa map 80 trong `ChangeMapService`.
  - `checkDoneTaskPower` chỉ chạy khi `NPoint.powerUp` được gọi → **thêm kiểm tra lại mốc SM ngay trong `sendNextSubTask`**, nếu không người đã đủ 80M sẽ đứng ở bước 0 tới khi giết con quái tiếp theo.
  - Map 80 có NPC **Goku SSJ (60)** — có thể dùng làm người báo tin thay sư phụ nếu muốn giữ người chơi ở lại map.

---

## 4. Chương 4 — Cỗ máy và những bản sao (NV 24–31)

> **Mạch chương:** Fide chỉ là kẻ giao hàng; kẻ chế tạo là **Dr. Myuu** (NPC 83). Myuu không xóa ký ức — hắn **sao chép** nó, rồi đúc vào kim loại. Android, King Kong, Xên bọ hung đều là những thí nghiệm dở dang trên đường tới sản phẩm thật. Chương kết ở Võ đài Xên, khi người chơi nhìn thấy sản phẩm hoàn thiện của Myuu: **chính mình**.

### NV 24 — Tín hiệu lạ từ phương Bắc

**Chương** 4 — Cỗ máy và những bản sao | **Mốc SM khi xong** 200.000.000 | **Nhiệm vụ kế** 25

*Máy dò của Bunma bắt được một sóng mới: không phải sóng sinh học, mà sóng cơ khí — đều đặn, lạnh, và lặp lại đúng một đoạn ký ức của người chơi. Ở phía Bắc, những con Xên con đang bò ra từ một đường ống mà lẽ ra không tồn tại.*

| # | Tên bước (hiển thị cho người chơi) | Kiểu | Điều kiện server kiểm tra | Số lượng | Map (id + tên) | NPC (id + tên) | Hằng số | Thưởng bước |
|---|---|---|---|---|---|---|---|---|
| 0 | Nghe Bunma nói về sóng cơ khí | A3 | `checkDoneTaskTalkNpc` — npc 37 Bunma (tương lai), `mapId == 102` | 1 | 102 Nhà Bunma | 37 Bunma | `TASK_24_0` | 6.000.000 SM + 6.000.000 TN |
| 1 | Tới Thành phố phía đông | A6 | `checkDoneTaskGoToMap` — vào map 92 | 1 | 92 Thành phố phía đông | — | `TASK_24_1` | 6.000.000 SM + 6.000.000 TN |
| 2 | Diệt 50 Xên con cấp 1–2 | A1 | `checkDoneTaskKillMob` — mob tempId 58 Xên con cấp 1 (lv11) và 59 Xên con cấp 2 (lv12), đếm chung | 50 | 92 Thành phố phía đông, 93 Thành phố phía nam | — | `TASK_24_2` | 6.000.000 SM + 6.000.000 TN |
| 3 | Diệt 40 Xên con cấp 3–4 | A1 | `checkDoneTaskKillMob` — mob tempId 60 Xên con cấp 3 (lv13) và 61 Xên con cấp 4 (lv14), đếm chung | 40 | 94 Đảo Balê, 96 Cao nguyên | — | `TASK_24_3` | 6.000.000 SM + 6.000.000 TN |
| 4 | Báo lại cho Bunma | A3 | `checkDoneTaskTalkNpc` — npc 37 Bunma, `mapId == 102` | 1 | 102 Nhà Bunma | 37 Bunma | `TASK_24_4` | 6.000.000 SM + 6.000.000 TN |

- **Lời thoại**
  - *(bước 0, npc 37 Bunma)* "Sóng này lặp đúng 12 giây một lần. Và đoạn lặp là... giọng của chính ngươi."
  - *(bước 0, npc 37)* "Ai đó đã ghi âm ký ức ngươi và đem phát lại cho máy móc nghe. Ta thấy lạnh gáy."
  - *(bước 3, hệ thống)* "Xên con bò ra từ một đường ống chôn dưới đất. Đường ống này không có trên bản đồ nào."
  - *(bước 4, npc 37 Bunma)* "Ống dẫn về phía Bắc. Trên đó có hai gã tự xưng 'bác sĩ'. Cẩn thận."
- **Mô tả nhiệm vụ (cột `detail` DB)**: "Máy dò bắt được sóng cơ khí phát lại chính ký ức của ngươi. Lần theo nó lên phía Bắc. Thưởng: 60.000.000 sức mạnh, 60.000.000 tiềm năng, 20 Đá nâng cấp cấp 4."
- **Thưởng khi hoàn thành**: 60.000.000 SM + 60.000.000 TN · Đá nâng cấp cấp 4 (1077) ×20.
- **Mở khóa**: map **92–96 và 102 Nhà Bunma** (§8: mốc cũ `TASK_21_0` → mốc mới **`TASK_24_0`**); **NPC Ca Lích (38)** (§8: mốc cũ `TASK_20_0`/`TASK_21_0` → **`TASK_24_0`**).
- **Ghi chú triển khai**:
  - Map 95 là `MAP_OFFLINE` — **không** đưa vào điều kiện bước nào.
  - `checkDoneTaskTalkNpc` cho `BUNMA_TL` (37) hiện xử lý 22_3, 22_5, 23_4, 24_4, 25_5, 26_5, 27_5, 28_5 → viết lại hoàn toàn theo tuyến mới.
  - Xên con cấp 1–8 (tempId 58–65) là nguồn rơi **Viên Capsule kì bí (380)** khi dùng Máy dò — không đụng tới nhiệm vụ mới.

---

### NV 25 — Android đầu tiên

**Chương** 4 — Cỗ máy và những bản sao | **Mốc SM khi xong** 300.000.000 | **Nhiệm vụ kế** 26

*Dr.Kôrê và Android 19 là bản mẫu số một: hai cỗ máy biết hút năng lượng nhưng chưa biết hút ký ức. Trong lõi của chúng có một đoạn băng ghi tiếng nói của Dr. Myuu — và một dòng chữ khắc: "Mẫu 01. Thất bại. Ký ức không bám vào kim loại."*

| # | Tên bước (hiển thị cho người chơi) | Kiểu | Điều kiện server kiểm tra | Số lượng | Map (id + tên) | NPC (id + tên) | Hằng số | Thưởng bước |
|---|---|---|---|---|---|---|---|---|
| 0 | Tới Cao nguyên tìm hai "bác sĩ" | A6 | `checkDoneTaskGoToMap` — vào map 96 | 1 | 96 Cao nguyên | — | `TASK_25_0` | 9.000.000 SM + 9.000.000 TN |
| 1 | Hạ Android 19 rồi Dr.Kôrê | A2 | `checkDoneTaskKillBoss` — đếm chung **Android 19 (−30)** (HP 1M, dame 12.200) và **Dr.Kôrê (−31)** (HP 2M, dame 12.000, xuất hiện DEFAULT kèm Android 19) | 2 | 96 Cao nguyên, 94 Đảo Balê, 93 Thành phố phía nam | — | `TASK_25_1` | 9.000.000 SM + 9.000.000 TN |
| 2 | Nhặt 3 Lõi năng lượng Android | A4 | `checkDoneTaskPickItem` — item **ITEM MỚI 2070 "Lõi năng lượng Android"** (icon mượn 8620 của Đá xanh lam 935); rơi 100% từ −30 và −31 khi ở `TASK_25_2` | 3 | 93, 94, 96 | — | `TASK_25_2` | 9.000.000 SM + 9.000.000 TN |
| 3 | Đưa lõi cho Bunma mổ xẻ | A3 | `checkDoneTaskTalkNpc` — npc 37 Bunma, `mapId == 102` | 1 | 102 Nhà Bunma | 37 Bunma | `TASK_25_3` | 9.000.000 SM + 9.000.000 TN |

- **Lời thoại**
  - *(bước 1, boss −31 Dr.Kôrê)* "Hút năng lượng của nó, mau lên! Mẫu này còn nguyên dữ liệu!"
  - *(bước 1, boss −30 Android 19)* "Hấp thụ.. các ngươi nghĩ sao vậy?"
  - *(bước 1, boss −31, textE)* "Mau đền mạng cho thằng em trai ta..."
  - *(bước 2, hệ thống)* "Trong lõi có một dòng khắc: 'Mẫu 01. Thất bại. Ký ức không bám vào kim loại.'"
  - *(bước 3, npc 37 Bunma)* "Chữ ký trên lõi là Dr. Myuu. Hắn đã thử 01, vậy bây giờ hắn đang ở mẫu số mấy?"
  - *(bước 3, npc 37)* "Kim loại thường không giữ ký ức. Phải pha lê hóa. Tới gặp bà Hạt Mít."
- **Mô tả nhiệm vụ (cột `detail` DB)**: "Hạ hai cỗ máy đầu tiên của Dr. Myuu và mang lõi năng lượng của chúng về cho Bunma. Thưởng: 90.000.000 sức mạnh, 90.000.000 tiềm năng, 30 Đá nâng cấp cấp 4."
- **Thưởng khi hoàn thành**: 90.000.000 SM + 90.000.000 TN · Đá nâng cấp cấp 4 (1077) ×30.
- **Mở khóa**: boss **Dr.Kôrê (−31)** và **Android 19 (−30)** vào bảng nhiệm vụ.
- **Ghi chú triển khai**:
  - Boss hiện gọi `TASK_23_1` (Android 19) và `TASK_23_2` (Dr.Kôrê) — đổi cả hai sang `TASK_25_1`, `maxCount = 2`.
  - **Android 19 hấp thụ chưởng**: Kamejoko/Masenko/Antomic bị hồi 80% sát thương cho boss → lời thoại bước 1 phải nhắc người chơi dùng **đánh thường**, nếu không sẽ tưởng boss bất tử.
  - `Dr.Kôrê.injured(Player, int, …)` **sai chữ ký** (int thay vì long) nên không override được — hắn không hấp thụ chưởng. Không cần sửa cho nhiệm vụ, nhưng nên ghi vào phiếu lỗi.
  - Thêm **ITEM MỚI 2070** + luật rơi trong `reward` của `DrKore.java` / `Android19.java`.

---

### NV 26 — Kim loại và ký ức

**Chương** 4 — Cỗ máy và những bản sao | **Mốc SM khi xong** 400.000.000 | **Nhiệm vụ kế** 27

*Bà Hạt Mít giải thích: kim loại thường quên, nhưng **pha lê** thì nhớ. Đó là lý do Myuu phải đục lỗ và ép sao vào sản phẩm của hắn. Nhiệm vụ dạy tính năng pha lê hóa + ép sao, và kết bằng một mẫu kim loại phát lại đúng giọng nói của người chơi.*

| # | Tên bước (hiển thị cho người chơi) | Kiểu | Điều kiện server kiểm tra | Số lượng | Map (id + tên) | NPC (id + tên) | Hằng số | Thưởng bước |
|---|---|---|---|---|---|---|---|---|
| 0 | Gặp Bà Hạt Mít ở Đảo Kamê | A3 | `checkDoneTaskTalkNpc` — npc 21 Bà Hạt Mít, yêu cầu `mapId == 5`; trao miễn phí **Đá ngũ sắc (674) ×6**, **Sao pha lê lục (447) ×2**, **ITEM MỚI 2071 "Mẫu kim loại có ký ức"** ×1 | 1 | 5 Đảo Kamê | 21 Bà Hạt Mít | `TASK_26_0` | 13.000.000 SM + 13.000.000 TN |
| 1 | Pha lê hóa một trang bị (đục 1 lỗ) | **B3** | **Trigger mới** — `combine/PhaLeHoaTrangBi.phaLeHoa` roll thành công → option 107 (số lỗ) tăng lên `>= 1`; **hoặc** người chơi đã sẵn có một trang bị với option 107 `>= 1` → tính hoàn thành ngay khi vào bước (chống kẹt) | 1 | 5 Đảo Kamê | 21 Bà Hạt Mít | `TASK_26_1` | 13.000.000 SM + 13.000.000 TN |
| 2 | Ép 1 Sao pha lê vào trang bị đó | **B3** | **Trigger mới** — `combine/EpSaoTrangBi.epSaoTrangBi` thành công → option 102 (số sao) tăng lên `>= 1` | 1 | 5 Đảo Kamê | 21 Bà Hạt Mít | `TASK_26_2` | 13.000.000 SM + 13.000.000 TN |
| 3 | Dùng Mẫu kim loại có ký ức | A12 | `checkDoneTaskUseItem` — item **ITEM MỚI 2071** (icon mượn 2288 của Hóa thạch Ngọc Rồng 362); phát 3 dòng thoại rồi tự xóa | 1 | bất kỳ | — | `TASK_26_3` | 13.000.000 SM + 13.000.000 TN |
| 4 | Kể lại cho %10 những gì ngươi nghe được | A3 | `checkDoneTaskTalkNpc` — npc 13 / 14 / 15 | 1 | 5 / 13 / 20 | 13 / 14 / 15 (sư phụ) | `TASK_26_4` | 13.000.000 SM + 13.000.000 TN |

- **Lời thoại**
  - *(bước 0, npc 21 Bà Hạt Mít)* "Sắt thì quên. Pha lê thì nhớ. Ai muốn nhốt ký ức đều phải qua tay nghề của ta."
  - *(bước 0, npc 21)* "Cầm đá ngũ sắc mà đục lỗ. Trượt cũng không sao, chỉ mất tiền thôi — đồ không hỏng."
  - *(bước 2, npc 21)* "Thấy chưa? Ngôi sao vừa dính vào là món đồ có 'tiểu sử'. Từ giờ nó nhớ chủ nó."
  - *(bước 3, hệ thống)* "Mẫu kim loại rung lên. Rồi nó cất tiếng — bằng đúng giọng của ngươi."
  - *(bước 4, sư phụ 13/14/15)* "Hắn không xóa ký ức ngươi. Hắn sao chép nó. Nghĩa là ở đâu đó có một 'ngươi' khác."
- **Mô tả nhiệm vụ (cột `detail` DB)**: "Bà Hạt Mít dạy pha lê hóa và ép sao — cách duy nhất khiến kim loại giữ được ký ức. Thưởng: 130.000.000 sức mạnh, 130.000.000 tiềm năng, 2 Sao pha lê lục, 3 Đá ngũ sắc."
- **Thưởng khi hoàn thành**: 130.000.000 SM + 130.000.000 TN · Sao pha lê lục (447) ×2 · Đá ngũ sắc (674) ×3.
- **Mở khóa**: tính năng **Pha lê hóa trang bị** và **Ép sao trang bị** (menu "Chức năng pha lê" của Bà Hạt Mít, map 5) hiện từ `TASK_26_0`.
- **Ghi chú triển khai**:
  - **B3**: móc vào `combine/PhaLeHoaTrangBi.java` (nhánh roll thành công) và `combine/EpSaoTrangBi.java` (cuối `epSaoTrangBi`) → `TaskService.checkDoneTaskCombine(player, type)`.
  - **Cảnh báo cân bằng**: pha lê hóa 0 → 1 lỗ tốn **5.000.000 vàng + 1 ngọc**, tỉ lệ thật **50%** (NPC hiển thị 80%). Ép sao thì luôn 100%, chỉ tốn 10 ngọc. Đây là **chi phí cơ chế game**, người chơi tự trả — **nhiệm vụ không phát vàng/ngọc**. Chống kẹt bằng hai cách: (a) trao đủ nguyên liệu **bằng vật phẩm** ngay ở bước 0 — nâng **Đá ngũ sắc (674) lên ×6** (thay cho đề xuất cũ là phát thêm vàng/ngọc) và giữ **Sao pha lê lục (447) ×2**; (b) bước 1 tính là **hoàn thành ngay** nếu trang bị của người chơi **đã sẵn có ≥ 1 lỗ pha lê** (option 107 `>= 1`), khỏi phải roll.
  - **Bà Hạt Mít chỉ có chức năng pha lê ở map 5** (Đảo Kamê). Người Namếc/Xayda buộc phải bay về map 5. Nếu muốn giữ đúng "5/13/20" của bảng tổng thì phải thêm `[21, x, y]` vào `npcs` của map 13 và 20 **và** mở nhánh pha lê trong `BaHatMit.openBaseMenu` cho hai map đó. Cần bạn chốt.
  - **A12**: thêm `case 2071` vào `switch` của `checkDoneTaskUseItem`.

---

### NV 27 — Ba cỗ máy

**Chương** 4 — Cỗ máy và những bản sao | **Mốc SM khi xong** 500.000.000 | **Nhiệm vụ kế** 28

*Ca Lích (NPC 38) chỉ đường tới sân sau siêu thị: ba cỗ máy mẫu 13, 14, 15 đang nằm trong container, được đánh số bằng sơn trắng. Chúng không được lập trình để giết — chúng được lập trình để **ghi hình** người chơi chiến đấu. Myuu đang thu thập dữ liệu vận động.*

| # | Tên bước (hiển thị cho người chơi) | Kiểu | Điều kiện server kiểm tra | Số lượng | Map (id + tên) | NPC (id + tên) | Hằng số | Thưởng bước |
|---|---|---|---|---|---|---|---|---|
| 0 | Hỏi Ca Lích về container lạ | A3 | `checkDoneTaskTalkNpc` — npc 38 Ca Lích, `mapId == 102` | 1 | 102 Nhà Bunma | 38 Ca Lích | `TASK_27_0` | 18.000.000 SM + 18.000.000 TN |
| 1 | Tới sân sau siêu thị | A6 | `checkDoneTaskGoToMap` — vào map 104 | 1 | 104 Sân sau siêu thị | — | `TASK_27_1` | 18.000.000 SM + 18.000.000 TN |
| 2 | Hạ ba cỗ máy mẫu | A2 | `checkDoneTaskKillBoss` — đếm chung **Android 15 (−34)** (HP 5M), **Android 13 (−32)** (HP 3M), **Android 14 (−33)** (HP 4M); thứ tự bị ép bởi cơ chế `callApk13` sẵn có | 3 | 104 Sân sau siêu thị | — | `TASK_27_2` | 18.000.000 SM + 18.000.000 TN |
| 3 | Báo cáo với Ca Lích | A3 | `checkDoneTaskTalkNpc` — npc 38 Ca Lích, `mapId == 102` | 1 | 102 Nhà Bunma | 38 Ca Lích | `TASK_27_3` | 18.000.000 SM + 18.000.000 TN |

- **Lời thoại**
  - *(bước 0, npc 38 Ca Lích)* "Ba cái thùng. Không nhãn, không hải quan. Nhưng có lỗ ống kính ở mặt trước."
  - *(bước 2, boss −33 Android 14, textS)* "Các ngươi là ai? ... Ghi nhận. Đang quay. Đang gửi dữ liệu."
  - *(bước 2, boss −32 Android 13, textE)* "Sô..Sông...gôku....."
  - *(bước 3, npc 38 Ca Lích)* "Chúng không đánh để thắng. Chúng đánh để đo ngươi. Ai đó đang dựng hồ sơ về ngươi."
  - *(bước 3, npc 38)* "Phía Bắc còn một con to hơn cả ba cái này cộng lại. Nó canh cửa cho phòng thí nghiệm."
- **Mô tả nhiệm vụ (cột `detail` DB)**: "Ba cỗ máy mẫu 13, 14, 15 ở sân sau siêu thị không đánh để thắng — chúng đánh để ghi hình ngươi. Thưởng: 180.000.000 sức mạnh, 180.000.000 tiềm năng, 40 Đá nâng cấp cấp 4."
- **Thưởng khi hoàn thành**: 180.000.000 SM + 180.000.000 TN · Đá nâng cấp cấp 4 (1077) ×40.
- **Mở khóa**: map **104 Sân sau siêu thị** (§8: trước đây không khóa → mốc mới **`TASK_27_0`**).
- **Ghi chú triển khai**:
  - Boss hiện gọi `TASK_24_1` (A15), `TASK_24_2` (A14), `TASK_24_3` (A13) — đổi cả ba sang `TASK_27_2`, `maxCount = 3`.
  - **Cơ chế bắt buộc phải tôn trọng**: A13 chỉ xuất hiện khi A14/A15 ăn đòn chí mạng lần đầu (`callApk13`), và **A13 không thể chết khi A14 hoặc A15 còn sống**. Vì vậy `maxCount = 3` đếm chung là cách duy nhất không gây kẹt — **không** tách thành 3 bước theo thứ tự.
  - Nhóm này **không có `autoLeaveMap`** → đứng mãi trong map 104, an toàn cho người chơi cày chậm.

---

### NV 28 — King Kong

**Chương** 4 — Cỗ máy và những bản sao | **Mốc SM khi xong** 800.000.000 | **Nhiệm vụ kế** 29

*Cửa vào phòng thí nghiệm nằm dưới chân ngọn núi phía Bắc, và thứ canh nó cao bằng một tòa nhà. King Kong không phải máy: nó là một sinh vật bị Myuu nhồi kim loại vào đầu. Trong mảnh giáp của nó có khắc tên một đứa trẻ.*

| # | Tên bước (hiển thị cho người chơi) | Kiểu | Điều kiện server kiểm tra | Số lượng | Map (id + tên) | NPC (id + tên) | Hằng số | Thưởng bước |
|---|---|---|---|---|---|---|---|---|
| 0 | Tới Thành phố phía bắc | A6 | `checkDoneTaskGoToMap` — vào map 97 | 1 | 97 Thành phố phía bắc | — | `TASK_28_0` | 24.000.000 SM + 24.000.000 TN |
| 1 | Dọn 60 Xên con cấp 5–7 quanh cửa hầm | A1 | `checkDoneTaskKillMob` — mob tempId 62 Xên con cấp 5 (lv15), 63 cấp 6 (lv16), 64 cấp 7 (lv17), đếm chung | 60 | 97 Thành phố phía bắc, 98 Ngọn núi phía bắc, 99 Thung lũng phía bắc | — | `TASK_28_1` | 24.000.000 SM + 24.000.000 TN |
| 2 | Hạ Poc, Pic rồi King Kong | A2 | `checkDoneTaskKillBoss` — đếm chung **Poc (−36)** (HP 15M, dame 18.000), **Pic (−35)** (HP 10M, dame 17.022), **King Kong (−37)** (HP 20M, dame 12.000); thứ tự bị ép bởi chuỗi AFK sẵn có (Poc → Pic → King Kong) | 3 | 97, 98, 99 | — | `TASK_28_2` | 24.000.000 SM + 24.000.000 TN |
| 3 | Nhặt Mảnh giáp có khắc tên | A4 | `checkDoneTaskPickItem` — item **ITEM MỚI 2072 "Mảnh giáp khắc tên"** (icon mượn 10197 của Mảnh áo 1066); rơi 100% từ **King Kong (−37)** khi ở `TASK_28_3` | 1 | 97, 98, 99 | — | `TASK_28_3` | 24.000.000 SM + 24.000.000 TN |
| 4 | Đưa mảnh giáp cho Bunma | A3 | `checkDoneTaskTalkNpc` — npc 37 Bunma, `mapId == 102` | 1 | 102 Nhà Bunma | 37 Bunma | `TASK_28_4` | 24.000.000 SM + 24.000.000 TN |

- **Lời thoại**
  - *(bước 2, boss −35 Pic, textS)* "Chào! Có Gôku ở đây không? Bọn ta được lệnh không cho ai xuống hầm."
  - *(bước 2, boss −37 King Kong)* "Mau đền mạng cho những người bạn của ta!"
  - *(bước 3, hệ thống)* "Mặt trong mảnh giáp có một cái tên nguệch ngoạc, kiểu chữ trẻ con tập viết."
  - *(bước 4, npc 37 Bunma)* "Nó từng là một đứa bé. Myuu không chế tạo quái vật — hắn nhồi kim loại vào người sống."
  - *(bước 4, npc 37)* "Cửa hầm mở rồi. Nhưng bên trong có hệ thống thanh lọc — đứng lâu là ngươi bị đẩy ra."
- **Mô tả nhiệm vụ (cột `detail` DB)**: "Hạ King Kong cùng Pic và Poc để mở cửa hầm dẫn vào phòng thí nghiệm của Dr. Myuu. Thưởng: 240.000.000 sức mạnh, 240.000.000 tiềm năng, 20 Đá nâng cấp cấp 5."
- **Thưởng khi hoàn thành**: 240.000.000 SM + 240.000.000 TN · Đá nâng cấp cấp 5 (1078) ×20.
- **Mở khóa**: map **97–100** (§8: mốc cũ `TASK_24_0` → mốc mới **`TASK_28_0`**).
- **Ghi chú triển khai**:
  - Boss hiện gọi `TASK_25_1` (Poc), `TASK_25_2` (Pic), `TASK_25_3` (King Kong) — đổi cả ba sang `TASK_28_2`, `maxCount = 3`.
  - `autoLeaveMap` 15 phút không người → nhóm ở lại nếu có người, an toàn.
  - Thêm **ITEM MỚI 2072** + luật rơi trong `KingKong.reward`.

---

### NV 29 — Phòng thí nghiệm Myuu

**Chương** 4 — Cỗ máy và những bản sao | **Mốc SM khi xong** 1.000.000.000 | **Nhiệm vụ kế** 30

*Bên trong phòng thí nghiệm, không khí được lọc mỗi sáu phút — và mẻ lọc cuốn theo mọi sinh vật không có thẻ từ. Người chơi có đúng một cửa sổ thời gian để giật lấy các bản thiết kế treo trên tường. Trên bản thiết kế là hình vẽ một người, và người đó có khuôn mặt của người chơi.*

| # | Tên bước (hiển thị cho người chơi) | Kiểu | Điều kiện server kiểm tra | Số lượng | Map (id + tên) | NPC (id + tên) | Hằng số | Thưởng bước |
|---|---|---|---|---|---|---|---|---|
| 0 | Lấy thẻ từ giả của Bunma | A3 | `checkDoneTaskTalkNpc` — npc 37 Bunma, `mapId == 102`; trao **ITEM MỚI 2073 "Thẻ từ phòng thí nghiệm"** ×1 | 1 | 102 Nhà Bunma | 37 Bunma | `TASK_29_0` | 30.000.000 SM + 30.000.000 TN |
| 1 | Đột nhập Phòng thí nghiệm Myuu | A6 | `checkDoneTaskGoToMap` — vào map 166; yêu cầu có item 2073 trong hành trang | 1 | 166 Phòng thí nghiệm Myuu | — | `TASK_29_1` | 30.000.000 SM + 30.000.000 TN |
| 2 | Giật 5 Bản thiết kế trong 6 phút | **B12** | **Trigger mới** — `checkDoneTaskPickItem` item **ITEM MỚI 2074 "Bản thiết kế bản sao"** (icon mượn 12846 của Rương ngọc rồng 1560), rơi sẵn trên map 166, **kèm đồng hồ 6 phút** (xem luật hết giờ bên dưới) | 5 | 166 Phòng thí nghiệm Myuu | — | `TASK_29_2` | 30.000.000 SM + 30.000.000 TN |
| 3 | Đối mặt Dr. Myuu | A3 | `checkDoneTaskTalkNpc` — npc 83 Dr. Myuu, yêu cầu `mapId == 166` | 1 | 166 Phòng thí nghiệm Myuu | 83 Dr. Myuu | `TASK_29_3` | 30.000.000 SM + 30.000.000 TN |

**Luật hết giờ của bước 2 (B12) — ghi rõ để khỏi hiểu nhầm:**

| Mốc | Xử lý |
|---|---|
| Vào map 166 lần đầu ở bước 2 | Ghi `TaskMain.lastTime = System.currentTimeMillis()`, gửi thông báo "Hệ thống lọc khí sẽ chạy sau 6 phút." |
| Mỗi 30 giây còn lại | Nhắc "Còn X phút." (60s cuối nhắc mỗi 10 giây) |
| `now - lastTime > 360_000` (6 phút) mà chưa đủ 5 bản | **`count = 0`**; item 2074 đang cầm bị xóa; người chơi bị **đưa về map 97 Thành phố phía bắc**; thông báo "Hệ thống lọc khí đã chạy. Ngươi bị đẩy ra ngoài." |
| Làm lại | Quay lại map 166 → `lastTime` được đặt lại → đồng hồ chạy từ đầu. **Không giới hạn số lần thử, không mất đồ, không tụt bước.** |
| Rời map 166 giữa chừng (tự thoát / mất mạng / đứt kết nối) | Đồng hồ **dừng và reset**: `count = 0`, `lastTime = 0`. Vào lại là bắt đầu lượt mới. |
| Đủ 5 bản trước khi hết giờ | Sang bước 3 ngay, đồng hồ bị hủy (`lastTime = 0`) |

- **Lời thoại**
  - *(bước 0, npc 37 Bunma)* "Thẻ này giả, chỉ qua được cửa chứ không qua được hệ thống lọc khí. Sáu phút thôi."
  - *(bước 1, hệ thống khi vào map 166)* "Hệ thống lọc khí sẽ chạy sau 6 phút. Lấy đủ 5 bản thiết kế rồi biến."
  - *(bước 2, hệ thống khi hết giờ)* "Hệ thống lọc khí đã chạy. Ngươi bị đẩy ra ngoài — thử lại đi."
  - *(bước 3, npc 83 Dr. Myuu)* "Ngươi cầm bản vẽ của chính ngươi mà tay run à? Nó hoàn thiện hơn ngươi nhiều."
  - *(bước 3, npc 83)* "Heart đặt hàng một vũ trụ không đau. Ta chỉ giao đúng thứ ngài ấy đặt: những cái vỏ ngoan."
  - *(bước 3, npc 83)* "Muốn xem bản thật à? Nó đang đợi ngươi ở võ đài. Nó cũng muốn xem ngươi lắm."
- **Mô tả nhiệm vụ (cột `detail` DB)**: "Đột nhập phòng thí nghiệm của Dr. Myuu và lấy 5 bản thiết kế trước khi hệ thống lọc khí chạy (6 phút). Thưởng: 300.000.000 sức mạnh, 300.000.000 tiềm năng, 25 Đá nâng cấp cấp 5, 5 Đá bảo vệ."
- **Thưởng khi hoàn thành**: 300.000.000 SM + 300.000.000 TN · Đá nâng cấp cấp 5 (1078) ×25 · Đá bảo vệ (987) ×5.
- **Mở khóa**: map **166 Phòng thí nghiệm Myuu** (§8: trước đây không khóa → mốc mới **`TASK_29_0`**).
- **Ghi chú triển khai**:
  - **B12** — bước đếm giờ **duy nhất của chương 4** (§4.3). Cùng cơ chế với NV 23 bước 2: `TaskMain.lastTime` + kiểm tra trong `Player.update`, thêm nhánh "đẩy khỏi map" dùng `ChangeMapService.changeMap(player, 97, …)`.
  - **Map 166 hiện không có mob, không có NPC, không có waypoint nào trong `map_template`** — cần: (a) thêm `[83, x, y]` vào cột `npcs`; (b) thêm điểm rơi item 2074 (sinh bằng `ItemMap` khi người chơi vào map, 5–8 viên rải quanh, **không** tự biến mất sau 50 giây); (c) thêm waypoint ra map 97.
  - `npc_list/DrMyuu.java` đã tồn tại nhưng **chưa bao giờ được spawn** ([18-npc.md](../1-he-thong-hien-tai/18-npc.md)) — cần thêm nhánh `checkDoneTaskTalkNpc` cho `TASK_29_3` và viết lại menu.
  - Thêm **ITEM MỚI 2073, 2074**.

---

### NV 30 — Xên bọ hung

**Chương** 4 — Cỗ máy và những bản sao | **Mốc SM khi xong** 1.400.000.000 | **Nhiệm vụ kế** 31

*Xên bọ hung là mẫu gần nhất: một cỗ máy biết hấp thụ người khác và giữ lại ký ức của họ. Myuu gọi nó là "mẫu 07 — thành công một phần". Nó thành công ở chỗ nó nhớ; nó thất bại ở chỗ nó không biết mình là ai trong đống ký ức đã nuốt.*

| # | Tên bước (hiển thị cho người chơi) | Kiểu | Điều kiện server kiểm tra | Số lượng | Map (id + tên) | NPC (id + tên) | Hằng số | Thưởng bước |
|---|---|---|---|---|---|---|---|---|
| 0 | Tới Thị trấn Ginder | A6 | `checkDoneTaskGoToMap` — vào map 100 | 1 | 100 Thị trấn Ginder | — | `TASK_30_0` | 38.000.000 SM + 38.000.000 TN |
| 1 | Diệt 50 Xên con cấp 8 | A1 | `checkDoneTaskKillMob` — mob tempId 65 Xên con cấp 8 (lv18, HP 550.000) | 50 | 100 Thị trấn Ginder | — | `TASK_30_1` | 38.000.000 SM + 38.000.000 TN |
| 2 | Hạ Xên bọ hung — hai hình dạng đầu | A2 | `checkDoneTaskKillBoss` — boss **Xên bọ hung (−100)**, `currentLevel` 1 (Xên bọ hung, HP 50M, dame 20.000) rồi 2 (Xên hoàn thiện, HP 100M, dame 25.000) | 2 | 100 Thị trấn Ginder | — | `TASK_30_2` | 38.000.000 SM + 38.000.000 TN |
| 3 | Hạ Xên hoàn thiện | A2 | `checkDoneTaskKillBoss` — boss **Xên bọ hung (−100)**, `currentLevel` 3 (Xên hoàn thiện, HP 150M, dame 30.000) | 1 | 100 Thị trấn Ginder | — | `TASK_30_3` | 38.000.000 SM + 38.000.000 TN |
| 4 | Báo với Bunma về "mẫu 07" | A3 | `checkDoneTaskTalkNpc` — npc 37 Bunma, `mapId == 102` | 1 | 102 Nhà Bunma | 37 Bunma | `TASK_30_4` | 38.000.000 SM + 38.000.000 TN |

- **Lời thoại**
  - *(bước 2, boss −100 Xên bọ hung, textS)* "Ta nhớ bốn nghìn người. Ta nhớ hết. Nhưng ta không nhớ nổi ta là ai."
  - *(bước 2, boss −100 khi hấp thụ)* "Haha, ngọt lắm đấy — thêm một cái tên nữa vào bộ sưu tập."
  - *(bước 3, boss −100, textE)* "Oái.. không... cơ thể hoàn hảo của ta!!"
  - *(bước 4, npc 37 Bunma)* "Myuu ghi: 'mẫu 07, thành công một phần'. Thành công một phần, nghĩa là có mẫu 08."
  - *(bước 4, npc 37)* "Potage ở hang động Potaufeu biết về chuyện bản sao. Ông ta là người duy nhất dám nói."
- **Mô tả nhiệm vụ (cột `detail` DB)**: "Xên bọ hung là mẫu thử gần hoàn thiện nhất của Dr. Myuu — hạ cả ba hình dạng của nó ở Thị trấn Ginder. Thưởng: 380.000.000 sức mạnh, 380.000.000 tiềm năng, 30 Đá nâng cấp cấp 5, 2 Sao pha lê vàng."
- **Thưởng khi hoàn thành**: 380.000.000 SM + 380.000.000 TN · Đá nâng cấp cấp 5 (1078) ×30 · Sao pha lê vàng (446) ×2.
- **Mở khóa**: map **103 Võ đài Xên bọ hung** (§8: mốc cũ `TASK_27_0` → mốc mới **`TASK_30_0`**).
- **Ghi chú triển khai**:
  - `boss/Cell/XenBoHung.java` hiện gọi `TASK_26_1/2/3` theo form — đổi sang `TASK_30_2` (form 1 và 2, `maxCount = 2`) và `TASK_30_3` (form 3).
  - **Cơ chế hấp thụ người chơi** (giết người chơi, cộng 5% dame + 2% HP cho boss) giữ nguyên — nhưng lời thoại bước 0 nên cảnh báo: "Đừng đứng một mình trong tầm nó."
  - Boss nghỉ **30 phút**, chỉ có **1 con** toàn server, **không có `autoLeaveMap`** → đây là nút thắt đông người nhất của chương 4. Đề xuất: tăng `loadBoss` Xên bọ hung lên **2–3 con** trong thời gian tuyến mới chạy.

---

### NV 31 — Bản sao của chính ngươi *(nhánh Tiêu diệt)*

**Chương** 4 — Cỗ máy và những bản sao | **Mốc SM khi xong** 2.000.000.000 | **Nhiệm vụ kế** 32

*Potage (NPC 62) kể sự thật: Myuu không sao chép ký ức để lưu trữ — hắn sao chép để **thay thế**. Mẫu 08 đã hoàn thiện, và mẫu 08 chính là người chơi: cùng gương mặt, cùng chiêu thức, cùng từng kỷ niệm — chỉ thiếu Mảnh Ký Ức. Ở Võ đài Xên, người chơi đứng đối diện chính mình và phải quyết định xem bản nào được quyền tồn tại.*

> **Đây là điểm rẽ nhánh 2 (§7).** Bước 1 là một bước `B14`: **Tiêu diệt bản sao** (giữ task id **31**) hoặc **Thu nhận bản sao làm đồng minh** (chuyển sang task id **49**). Hai nhánh cùng giá trị thưởng, khác loại, cùng hội tụ về NV 32.

| # | Tên bước (hiển thị cho người chơi) | Kiểu | Điều kiện server kiểm tra | Số lượng | Map (id + tên) | NPC (id + tên) | Hằng số | Thưởng bước |
|---|---|---|---|---|---|---|---|---|
| 0 | Nghe Potage nói sự thật | A3 | `checkDoneTaskTalkNpc` — npc 62 Potage, yêu cầu `mapId == 140` | 1 | 140 Hang động Potaufeu | 62 Potage | `TASK_31_0` | 50.000.000 SM + 50.000.000 TN |
| 1 | Quyết định số phận bản sao | **B14** | **Trigger mới** — menu npc 62 Potage: [0] "Tiêu diệt nó" → giữ task 31, `index = 2`; [1] "Thu nhận nó" → `switchTaskBranch(player, 49)` đặt task 49, `index = 2` và trao **Bình chứa Commeson (638)** ×1 | 1 | 140 Hang động Potaufeu | 62 Potage | `TASK_31_1` | 50.000.000 SM + 50.000.000 TN |
| 2 | Tới Võ đài Xên bọ hung | A6 | `checkDoneTaskGoToMap` — vào map 103 | 1 | 103 Võ đài Xên bọ hung | — | `TASK_31_2` | 50.000.000 SM + 50.000.000 TN |
| 3 | Gọi một nhân chứng vào võ đài | **B13** | **Trigger mới** — trong `zone` của map 103 phải có **≥ 2 người chơi thật**; kiểm tra mỗi 2 giây trong `Player.update`, đủ điều kiện → `addDoneSubTask(player, 1)` | 1 | 103 Võ đài Xên bọ hung | — | `TASK_31_3` | 50.000.000 SM + 50.000.000 TN |
| 4 | Tiêu diệt Bản sao của ngươi | A2 | `checkDoneTaskKillBoss` — **BOSS MỚI "Bản sao <tên người chơi>"** (xem bảng chỉ số bên dưới); nhận diện bằng `boss instanceof BanSaoNguoiChoi && boss.ownerId == player.id`, **không** so id số | 1 | 103 Võ đài Xên bọ hung | — | `TASK_31_4` | 50.000.000 SM + 50.000.000 TN |
| 5 | Đạt 2.000.000.000 sức mạnh | A5 | `checkDoneTaskPower` — `power >= 2_000_000_000`; kiểm tra lại ngay khi vừa sang bước | 1 | — | — | `TASK_31_5` | 50.000.000 SM + 50.000.000 TN |

**Boss "Bản sao người chơi" — cách lấy chỉ số** (ghép từ **Nhân bản Commeson** và **bản sao đối thủ Rival** trong [11b](../1-he-thong-hien-tai/11b-boss-su-kien-pho-ban.md) §8–§9):

| Thuộc tính | Lấy như thế nào | Nguồn tham khảo |
|---|---|---|
| Class | `boss/nhan_ban/BanSaoNguoiChoi.java`, dựng theo `NhanBan.java`; không khai báo `BossType` → chạy trong `BossManager` | 11b §9 |
| id | `-1_500_000_000 - player.id` (khác dải của Commeson là `-player.id - 1_000_000_000`, tránh trùng). **Bắt buộc** kiểm tra `player.id < 600.000.000` để không tràn `int` | 11b §9 |
| Tên / giới tính / ngoại hình / cải trang | Copy nguyên từ người chơi gọi, tên hiển thị `"Bản sao " + player.name` | 11b §9 (Commeson), §8 (Rival) |
| HP | `player.nPoint.hpMax × 3` (Commeson dùng ×10 — quá nặng cho một bước tuyến chính bắt buộc; ×3 là mức hạ solo được ở 1,4 tỉ SM) | 11b §9 |
| Dame | `player.nPoint.dame × 3` | 11b §9 |
| Giáp / chí mạng / né | Dùng chung `nPoint` của người chơi như Rival (load lại từ DB), hồi đầy HP/KI khi vào map | 11b §8 |
| Skill | Toàn bộ skill có điểm > 0 của người chơi, **trừ** Tự phát nổ, Trói, Quả cầu kênh khi, Makankosappo, Trị thương | 11b §8, §9 |
| Map | Khu hiện tại của người chơi ở **map 103 Võ đài Xên bọ hung**, spawn cách ±200px | 11b §9 |
| Phạm vi đánh | PK PVP, **chỉ đánh đúng người chơi đã gọi** (`ownerId`) — không đụng vào nhân chứng | 11b §9 |
| Đồng hồ | Hiệu ứng kiểu `PKCommeson` **300 giây**; hết giờ → "Ngươi chưa đủ sức nhìn thẳng vào chính mình" + boss rời map, bước **không** tính, làm lại được ngay | 11b §9 |
| Thoại | textS "Ta cũng nhớ ngày đó. Ta nhớ rõ hơn ngươi."; textM "Ngươi mệt rồi. Để ta sống thay cho."; textE tuỳ nhánh | mới |

- **Lời thoại**
  - *(bước 0, npc 62 Potage)* "Myuu không xóa ngươi. Hắn chép ngươi. Xóa chỉ là để bản gốc khỏi cãi."
  - *(bước 0, npc 62)* "Mẫu 08 có đủ mọi thứ của ngươi. Thiếu đúng một thứ: Mảnh Ký Ức trong lồng ngực ngươi."
  - *(bước 1, npc 62)* "Giết nó, ngươi ngủ yên. Giữ nó, ngươi có đồng minh — và một ngày phải nhìn nó thay ngươi."
  - *(bước 3, hệ thống)* "Võ đài cần một nhân chứng. Không ai nhìn thấy thì không ai biết bản nào là thật."
  - *(bước 4, boss Bản sao, textS)* "Ta cũng nhớ ngày ông gọi nhầm tên. Ta nhớ rõ hơn ngươi đấy."
  - *(bước 4, boss Bản sao, textE)* "Ngươi giết ta... nhưng ngươi sẽ nhớ mãi rằng ta cũng từng là ngươi."
- **Mô tả nhiệm vụ (cột `detail` DB)**: "Potage tiết lộ Myuu đã chế ra một bản sao hoàn chỉnh của ngươi. Tới Võ đài Xên và tiêu diệt nó. Thưởng: 500.000.000 sức mạnh, 500.000.000 tiềm năng, 50 Đá nâng cấp cấp 5, 2 Sao pha lê cam, Mảnh Ký Ức #4."
- **Thưởng khi hoàn thành**: 500.000.000 SM + 500.000.000 TN · **Đá nâng cấp cấp 5 (1078) ×50** · **Sao pha lê cam (445) ×2** · **Mảnh Ký Ức #4 — ITEM MỚI 2013** (icon mượn 422 của Ngọc Rồng 4 sao).
- **Mở khóa**: **Mảnh Ký Ức #4**; tính năng **Nhân bản** ở NPC Potage (map 140) mở vĩnh viễn từ `TASK_31_4`; kết thúc chương 4.
- **Ghi chú triển khai**: xem [khối ghi chú chung của NV 31/49](#ghi-chú-triển-khai-chung-cho-nv-31-và-nv-49) bên dưới.

---

### NV 49 — Bản sao của chính ngươi *(nhánh Thu nhận)*

**Chương** 4 — Cỗ máy và những bản sao | **Mốc SM khi xong** 2.000.000.000 | **Nhiệm vụ kế** 32

*Người chơi chọn không giết. Đánh gục bản sao rồi nhốt nó vào Bình chứa Commeson — giữ lại một phiên bản của chính mình, ngoan hơn, lạnh hơn, và luôn nhớ rõ hơn mình. Potage cảnh báo: thứ gì biết nhớ thì cũng biết muốn.*

> Nhánh song sinh của NV 31. Bước 0–4 **giống hệt** task 31; khác từ bước 5.

| # | Tên bước (hiển thị cho người chơi) | Kiểu | Điều kiện server kiểm tra | Số lượng | Map (id + tên) | NPC (id + tên) | Hằng số | Thưởng bước |
|---|---|---|---|---|---|---|---|---|
| 0 | Nghe Potage nói sự thật | A3 | `checkDoneTaskTalkNpc` — npc 62 Potage, `mapId == 140` | 1 | 140 Hang động Potaufeu | 62 Potage | `TASK_49_0` | 50.000.000 SM + 50.000.000 TN |
| 1 | Quyết định số phận bản sao | **B14** | Bước khớp index; người chơi tới đây là đã chọn "Thu nhận" (xem NV 31 bước 1) và đã nhận **Bình chứa Commeson (638)** ×1 | 1 | 140 Hang động Potaufeu | 62 Potage | `TASK_49_1` | 50.000.000 SM + 50.000.000 TN |
| 2 | Tới Võ đài Xên bọ hung | A6 | `checkDoneTaskGoToMap` — vào map 103 | 1 | 103 Võ đài Xên bọ hung | — | `TASK_49_2` | 50.000.000 SM + 50.000.000 TN |
| 3 | Gọi một nhân chứng vào võ đài | **B13** | ≥ 2 người chơi thật trong `zone` của map 103 | 1 | 103 Võ đài Xên bọ hung | — | `TASK_49_3` | 50.000.000 SM + 50.000.000 TN |
| 4 | Đánh gục Bản sao của ngươi | A2 | `checkDoneTaskKillBoss` — cùng **BOSS MỚI "Bản sao <tên người chơi>"**; ở nhánh này boss **không `die()`** mà chuyển `NON_PK` + đứng yên, chờ bước 5 | 1 | 103 Võ đài Xên bọ hung | — | `TASK_49_4` | 50.000.000 SM + 50.000.000 TN |
| 5 | Thu nhận nó bằng Bình chứa Commeson | A12 | `checkDoneTaskUseItem` — item **638 Bình chứa Commeson**, yêu cầu `mapId == 103` và boss bản sao của chính mình đang ở trạng thái `NON_PK` trong khu | 1 | 103 Võ đài Xên bọ hung | — | `TASK_49_5` | 50.000.000 SM + 50.000.000 TN |
| 6 | Đạt 2.000.000.000 sức mạnh | A5 | `checkDoneTaskPower` — `power >= 2_000_000_000`; kiểm tra lại ngay khi vừa sang bước | 1 | — | — | `TASK_49_6` | 50.000.000 SM + 50.000.000 TN |

- **Lời thoại**
  - *(bước 4, boss Bản sao, textE nhánh này)* "Ngươi không giết ta? ... Vậy thì ta nợ ngươi một lần nhớ."
  - *(bước 5, hệ thống)* "Bình chứa khép lại. Bên trong, một giọng nói giống hệt ngươi vẫn đang kể chuyện."
  - *(bước 5, npc 62 Potage)* "Ngươi vừa giữ một thứ biết nhớ. Thứ gì biết nhớ thì sớm muộn cũng biết muốn."
  - *(bước 6, npc 62 Potage)* "Đừng mở bình khi ngươi yếu. Nó đang đếm từng ngày ngươi mệt đấy."
  - *(bước 6, npc 62)* "Mảnh Ký Ức thứ tư là của ngươi. Phía trước có kẻ chưa từng có ký ức."
- **Mô tả nhiệm vụ (cột `detail` DB)**: "Đánh gục bản sao rồi thu nhận nó làm đồng minh bằng Bình chứa Commeson. Thưởng: 500.000.000 sức mạnh, 500.000.000 tiềm năng, 3 Sao pha lê lục, 5 Đá ngũ sắc, 10 Đá bảo vệ, Mảnh Ký Ức #4."
- **Thưởng khi hoàn thành**: 500.000.000 SM + 500.000.000 TN · **Sao pha lê lục (447) ×3** · **Đá ngũ sắc (674) ×5** · **Đá bảo vệ (987) ×10** · **Mảnh Ký Ức #4 — ITEM MỚI 2013** *(cùng giá trị nhánh 31, nghiêng về pha lê thay vì đá nâng cấp)*.
- **Mở khóa**: **Mảnh Ký Ức #4**; tính năng **Nhân bản** ở Potage; kết thúc chương 4.

#### Ghi chú triển khai chung cho NV 31 và NV 49

- **Cân bằng nhánh (sau khi bỏ thưởng vàng/ngọc):** hai nhánh trước đây **cùng nhận 50 Ngọc**, nên việc bỏ ngọc là **đối xứng** — không nhánh nào bị hụt, không phải bù thêm vật phẩm. Phần còn lại vẫn giữ đúng thế cân của §7: nhánh **31** nghiêng về **đá nâng cấp cấp 5**, nhánh **49** nghiêng về **pha lê (sao pha lê + đá ngũ sắc + đá bảo vệ)**, tổng giá trị hai bên tương đương.
- **B14**: dùng lại `TaskService.switchTaskBranch` đã thêm ở NV 20; móc vào menu `npc_list/Potage.java`. Bảng chuyển tiếp trong `sendNextTaskMain`: `31 → 32`, **`49 → 32`**.
- **B13**: dùng lại hàm đếm người chơi trong `zone` đã thêm ở NV 19, nhưng kiểu kiểm tra là **trạng thái** (không phải "giết quái cùng nhau") → cần một biến thể `checkDoneTaskTogetherInZone(player)` gọi định kỳ trong `Player.update`.
- **Boss mới `BanSaoNguoiChoi`**:
  - Dựng từ `boss/nhan_ban/NhanBan.java` (copy người chơi, PK riêng, hiệu ứng đếm giờ) + phần load `nPoint`/đồ/nội tại/set của `boss/sieu_hang/Rival.java`.
  - Gọi boss: thêm hàm `Service.callBanSao(player)` theo mẫu `Service.callNhanBan`, kích hoạt khi người chơi vào map 103 ở bước 4 và đã xong bước 3.
  - `checkDoneTaskKillBoss` phải nhận diện bằng **class + `ownerId`**, **không** so id số — vì id được tính từ `player.id` nên dễ trùng dải với Commeson/Rival.
  - Nhánh 49: boss **không chết**. Khi `hp <= 0` lần đầu → `hp = 1`, `changeStatus(NON_PK)`, chat textE nhánh thu nhận, hoàn thành `TASK_49_4`, và **giữ boss trong khu 180 giây** để người chơi kịp dùng bình. Quá 180 giây → boss rời map, người chơi làm lại bước 4.
  - Boss **không rơi item**, **không cộng** `TRUM_SAN_BOSS` — tránh bị lạm dụng thành nguồn farm.
- **A12 với item 638**: hiện Bình chứa Commeson là phần thưởng của Nhân bản Commeson (có option Hạn sử dụng 30 ngày + Không thể giao dịch). Bản trao ở bước 1 của task 49 phải là bản **riêng, không hạn sử dụng, không giao dịch**, để người chơi không bị mất do hết hạn giữa chừng.
- **Map 103 Võ đài Xên bọ hung** đang là sân của **Siêu Bọ Hung (−101) + 7 Xên con (−102…−108)**, nghỉ 30 phút, và Siêu Bọ Hung **tự phát nổ gây `hpMax` sát thương toàn khu** khi chết. Phải đảm bảo bản sao và Siêu Bọ Hung **không ở cùng khu** — đề xuất: khi gọi bản sao, đưa người chơi sang khu trống (`MapService.getZoneWithMinPlayer`), hoặc chặn `SieuBoHung` vào khu đang có bản sao.
- **Rủi ro B13**: bước 3 bắt buộc có người thứ hai. Cần **đường vòng** giống NV 21: nếu đứng ở bước 3 quá 3 ngày, Potage mở menu "Tự làm chứng" → hoàn thành bước, đổi lại bản sao +20% HP. Cần bạn duyệt.

---

## 5. Ghi chú / việc cần làm khi code

> **Nhiệm vụ chính không thưởng vàng/ngọc/hồng ngọc** — chỉ SM, TN và vật phẩm (quyết định của chủ dự án).

### 5.1 Trigger mới phải thêm (phần thuộc file này)

| Mã | Dùng ở | Móc vào file | Nội dung |
|---|---|---|---|
| **B1** | NV 21 | `map/phoban/RedRibbonHQ.java` (chỗ đặt `winDT = true`) | `TaskService.checkDoneTaskFinishDungeon(player, mapType)`; duyệt mọi người chơi trong instance |
| **B2** | NV 17 | `combine/NangCapVatPham.java` (nhánh roll thành công) | `checkDoneTaskUpgradeItem(player, item)`; điều kiện option 72 `>= 2` |
| **B3** | NV 26 | `combine/PhaLeHoaTrangBi.java`, `combine/EpSaoTrangBi.java` | `checkDoneTaskCombine(player, type)`; option 107 (lỗ) và 102 (sao) |
| **B12** | NV 23 bước 2, NV 29 bước 2 | `TaskMain.lastTime` + `Player.update` | Đếm ngược; hết giờ **reset `count` về 0**, không tụt bước, không mất đồ; NV 29 còn đẩy người chơi về map 97 |
| **B13** | NV 19 bước 3, NV 31/49 bước 3 | `TaskService.checkDoneTaskKillMob` (NV 19) + `Player.update` (NV 31) | Đếm `Player` thật trong `zone`; NV 19 ×2 tiến độ khi ≥ 2 người |
| **B14** | NV 20 bước 1, NV 31 bước 1 | `npc_list/Berry.java`, `npc_list/Potage.java` + `sendNextTaskMain` | `switchTaskBranch(player, newTaskId)`; bảng chuyển tiếp `20→21`, `48→21`, `31→32`, `49→32` |
| **A12** | NV 17 bước 2, NV 26 bước 3, NV 49 bước 5 | `services_func/UseItem.java` | `checkDoneTaskUseItem` hiện có `switch` **rỗng** — thêm `case 2041`, `case 2071`, `case 638` |

### 5.2 Item mới (khối 2040–2099 + 2 Mảnh Ký Ức)

| id | Tên | TYPE | Icon mượn từ | Dùng ở | Ghi chú |
|---|---|---|---|---|---|
| **2012** | Mảnh Ký Ức #3 | 11 | 421 (Ngọc Rồng 3 sao 16) | NV 23 thưởng | Khối dùng chung 3 file |
| **2013** | Mảnh Ký Ức #4 | 11 | 422 (Ngọc Rồng 4 sao 17) | NV 31 / 49 thưởng | Khối dùng chung 3 file |
| **2040** | Vỏ đạn khắc dấu | 27 | 1421 (Mảnh đá vụn 225) | NV 16 bước 3 | Rơi từ mob 22/23/24 |
| **2041** | Búa rèn cũ | 27 | 1417 (Đá Titan 223) | NV 17 bước 0 → 2 | Dùng một lần |
| **2042** | Thẻ tiền thưởng Granola | 27 | 5428 (Bản đồ kho báu 611) | NV 20 bước 4 | Chỉ nhánh 20 |
| **2043** | Biên bản truy nã Ngân Hà | 27 | 5428 | NV 48 bước 4 | Chỉ nhánh 48 |
| **2044** | Máy đo ký ức | 27 | 6467 (Đá ngũ sắc 674) | NV 22 bước 3 | Rơi từ boss −27 |
| **2070** | Lõi năng lượng Android | 27 | 8620 (Đá xanh lam 935) | NV 25 bước 2 | Rơi từ boss −30/−31 |
| **2071** | Mẫu kim loại có ký ức | 27 | 2288 (Hóa thạch Ngọc Rồng 362) | NV 26 bước 0 → 3 | Dùng một lần |
| **2072** | Mảnh giáp khắc tên | 27 | 10197 (Mảnh áo 1066) | NV 28 bước 3 | Rơi từ boss −37 |
| **2073** | Thẻ từ phòng thí nghiệm | 27 | 9406 (Đá bảo vệ 987) | NV 29 bước 0 → 1 | Điều kiện vào map 166 |
| **2074** | Bản thiết kế bản sao | 27 | 12846 (Rương ngọc rồng 1560) | NV 29 bước 2 | Rơi sẵn trên map 166 |

Tất cả đặt `gender = 3`, `level = 1`, `power_require = 0`, option **Không thể giao dịch**. Phải **tăng phiên bản data client** sau khi thêm (§1 ràng buộc 5 của file 20). Nếu chủ dự án chọn phương án "không đụng data client" (§10 câu hỏi 1), thay thế: 2040/2044/2070/2072/2074 → **Mảnh đá vụn (225)**; 2041/2071/2073 → **Hộp Capsule (796)**; 2042/2043 → **Bản đồ kho báu (611)**; Mảnh Ký Ức #3/#4 → **Ngọc Rồng 3 sao (16) / 4 sao (17)**.

### 5.3 Boss mới / boss phải đổi hằng số nhiệm vụ

| Boss | id | File | Hằng số cũ | Hằng số mới |
|---|---|---|---|---|
| Kuku / Mập Đầu Đinh / Rambo | −20 / −21 / −22 | `boss/Nappa/*.java` | `TASK_19_0/1/2` | `TASK_20_3` và `TASK_48_3` (`maxCount = 3`) |
| Số 4 / 3 / 2 / 1 / Tiểu đội trưởng | −23 / −24 / −25 / −26 / −27 | `boss/tieu_doi_sat_thu/*.java` | `TASK_20_1…20_5` | `TASK_22_2` (`maxCount = 5`) |
| Fide đại ca | −28 (3 form) | `boss/Frieza/Fide.java` | `TASK_21_1/2/3` | `TASK_23_3` (form 1–2) + `TASK_23_4` (form 3) |
| Android 19 / Dr.Kôrê | −30 / −31 | `boss/Android/Android19.java`, `DrKore.java` | `TASK_23_1/23_2` | `TASK_25_1` (`maxCount = 2`) |
| Android 15 / 14 / 13 | −34 / −33 / −32 | `boss/Android/Android15.java`, `Android14.java`, `Android13.java` | `TASK_24_1/2/3` | `TASK_27_2` (`maxCount = 3`) |
| Poc / Pic / King Kong | −36 / −35 / −37 | `boss/Android/Poc.java`, `Pic.java`, `KingKong.java` | `TASK_25_1/2/3` | `TASK_28_2` (`maxCount = 3`) |
| Xên bọ hung | −100 (3 form) | `boss/Cell/XenBoHung.java` | `TASK_26_1/2/3` | `TASK_30_2` (form 1–2) + `TASK_30_3` (form 3) |
| **Bản sao người chơi** | `-1_500_000_000 - player.id` | **`boss/nhan_ban/BanSaoNguoiChoi.java` (MỚI)** | — | `TASK_31_4` / `TASK_49_4` |

### 5.4 Mốc khóa map / tính năng cần sửa (phần thuộc file này, khớp §8)

| Map / tính năng | Mốc cũ | Mốc mới | File |
|---|---|---|---|
| 27–38 | `TASK_13_0` / `TASK_15_0` | `TASK_16_0` | `ChangeMapService.checkMapCanJoin` |
| 6, 10, 19 | `TASK_16_0` | `TASK_18_0` | `ChangeMapService` |
| 63–77 | `TASK_18_0` / `TASK_19_0` | `TASK_19_0` | `ChangeMapService` |
| 79, 81, 82, 83 | `TASK_19_0` | `TASK_22_0` | `ChangeMapService` |
| 80 Núi khỉ vàng | `TASK_20_0` | `TASK_23_0` | `ChangeMapService` — **sửa luôn lỗi thiếu `break`** |
| 92–96, 102 | `TASK_21_0` | `TASK_24_0` | `ChangeMapService` |
| NPC Ca Lích | `TASK_20_0` / `TASK_21_0` | `TASK_24_0` | `npc_list/Calick.java` |
| 104 Sân sau siêu thị | (không khóa) | `TASK_27_0` | `ChangeMapService` |
| 97–100 | `TASK_24_0` | `TASK_28_0` | `ChangeMapService` |
| 166 Phòng thí nghiệm Myuu | (không khóa) | `TASK_29_0` | `ChangeMapService` |
| 103 Võ đài Xên | `TASK_27_0` | `TASK_30_0` | `ChangeMapService` |
| Nâng cấp trang bị (Bà Hạt Mít) | (không khóa) | `TASK_17_0` | `npc_list/BaHatMit.java` |
| Pha lê hóa / Ép sao (Bà Hạt Mít) | (không khóa) | `TASK_26_0` | `npc_list/BaHatMit.java` |
| Phó bản Doanh Trại (Lính canh) | (không khóa) | `TASK_21_0` | `npc_list/LinhCanh.java` |

### 5.5 Dữ liệu map / NPC phải bổ sung

| Việc | Map | Lý do |
|---|---|---|
| Thêm NPC **Granola (76)** vào cột `npcs` | 160 Khu hang động | NV 20 — Granola chưa có trong `map_template`, chưa có class, chưa có hằng `ConstNpc` |
| Viết `npc_list/Granola.java` + `ConstNpc.GRANOLA = 76` + nhánh trong `NpcFactory.createNPC` | — | NV 20 |
| Thêm NPC **Dr. Myuu (83)** vào cột `npcs` | 166 Phòng thí nghiệm Myuu | NV 29 — class `DrMyuu.java` đã có nhưng chưa từng được spawn |
| Thêm **waypoint** map 166 ↔ map 97 | 166 | NV 29 — map 166 hiện không có waypoint nào |
| Rải điểm rơi item 2074 (5–8 viên/khu, không tự biến mất sau 50 s) | 166 | NV 29 bước 2 |
| *(tuỳ chọn)* Thêm Jaco (63) vào map 25, 26 | 25, 26 Trạm tàu vũ trụ | NV 48 — để ba hành tinh đối xứng |
| *(tuỳ chọn)* Thêm Bà Hạt Mít (21) vào map 13, 20 + mở nhánh pha lê | 13, 20 | NV 26 — hiện chức năng pha lê chỉ có ở map 5 |

### 5.6 Rủi ro và điểm cần chủ dự án quyết

| # | Rủi ro | Mức | Đề xuất |
|---|---|---|---|
| 1 | **NV 21 kẹt cứng nếu bang < 5 người** — doanh trại là bước bắt buộc của tuyến chính | **Cao** | Mở đường vòng ở Lính canh sau 7 ngày đứng bước (đi nhờ instance bang khác) |
| 2 | **NV 31/49 bước B13 kẹt nếu server vắng người** | **Cao** | Đường vòng "Tự làm chứng" ở Potage sau 3 ngày, đổi lại bản sao +20% HP |
| 3 | **Xên bọ hung (−100) chỉ có 1 con toàn server, nghỉ 30 phút** — nút thắt của NV 30 | **Cao** | Tăng `loadBoss` lên 2–3 con trong thời gian tuyến mới chạy |
| 4 | **Nhóm Nappa `autoLeaveMap` sau 15 phút bất kể có người hay không** — NV 20/48 có thể bị hụt boss | Trung bình | Đếm chung `maxCount = 3` (đã áp dụng); hoặc sửa `autoLeaveMap` về mẫu "15 phút không người" như các nhóm khác |
| 5 | **Boss bản sao (NV 31) dùng id tính từ `player.id`** — nguy cơ trùng dải với Commeson / Rival và tràn `int` | Trung bình | Nhận diện bằng class + `ownerId`, chặn `player.id >= 600.000.000` |
| 6 | **NV 26 pha lê hóa tỉ lệ thật chỉ 50%** (NPC hiển thị 80%) — người xui sẽ hết nguyên liệu | Trung bình | Nhiệm vụ không phát vàng/ngọc → trao đủ nguyên liệu **bằng vật phẩm** ở bước 0 (Đá ngũ sắc ×6, Sao pha lê lục ×2) **và** cho bước 1 tính cả trường hợp trang bị **đã sẵn** có ≥ 1 lỗ pha lê |
| 7 | **`checkDoneTaskPower` chỉ chạy khi `NPoint.powerUp`** — NV 23 bước 0 và NV 31 bước 5 có thể đứng dù đã đủ SM | Trung bình | Kiểm tra lại mốc SM ngay trong `sendNextSubTask` |
| 8 | **Map 103 vừa là sân bản sao vừa là sân Siêu Bọ Hung** (tự phát nổ gây `hpMax` toàn khu) | Trung bình | Đưa người gọi bản sao sang khu trống, hoặc chặn Siêu Bọ Hung vào khu có bản sao |
| 9 | **12 item mới cần cập nhật data client** | Thấp | Đã có phương án thay thế bằng item sẵn có ở §5.2 |
| 10 | **Trùng id item giữa 3 file chi tiết (20a / 20b / 20c)** | Thấp | Tuân thủ bảng chia khối ở §2 (2020–2039 cho 20a, 2040–2099 cho 20b, 2100+ cho 20c) |
| 11 | **Map 126 có Hirudegarn (mob 70, HP 40M, big boss)** cạnh chỗ nói chuyện Tapion ở NV 18/22 | Thấp | Đặt Tapion xa Hirudegarn; thêm cảnh báo trong thoại |
