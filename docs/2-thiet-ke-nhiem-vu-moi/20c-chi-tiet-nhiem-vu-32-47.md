# 20c — Chi tiết nhiệm vụ 32–47: Chương 5 "Vương triều bóng tối" & Chương 6 "Lõi Hư Không"

> **Trạng thái: BẢN THIẾT KẾ ĐỂ DUYỆT — chưa sửa một dòng code nào.**
> File này là phần 3/3 của bộ chi tiết, bám theo [20-thiet-ke-nhiem-vu-moi.md](20-thiet-ke-nhiem-vu-moi.md).
> Hai file kia: [20a](20a-chi-tiet-nhiem-vu-00-15.md) (NV 0–15), [20b](20b-chi-tiet-nhiem-vu-16-31.md) (NV 16–31).
>
> Mọi id map / mob / npc / boss / item trong file này đều **tra từ dữ liệu thật** của server
> ([02b](../1-he-thong-hien-tai/02b-database-du-lieu-template.md), [11](../1-he-thong-hien-tai/11-boss.md), [11b](../1-he-thong-hien-tai/11b-boss-su-kien-pho-ban.md), [14](../1-he-thong-hien-tai/14-ban-do-pho-ban.md), [18](../1-he-thong-hien-tai/18-npc.md)).
> Vật phẩm chưa tồn tại được ghi rõ **ITEM MỚI** kèm id đề xuất ≥ 2000 và icon mượn từ item đã có
> (bảng `item_template` hiện dừng ở id 1999).

## Mục lục

1. [Bảng tóm tắt 16 nhiệm vụ + nhánh kết](#1-bảng-tóm-tắt-16-nhiệm-vụ--nhánh-kết)
2. [Quy ước đọc bảng bước](#2-quy-ước-đọc-bảng-bước)
3. [Chương 5 — Vương triều bóng tối (NV 32–39)](#3-chương-5--vương-triều-bóng-tối-nv-3239)
   - [NV 32 — Lời cảnh báo của Bardock](#nv-32--lời-cảnh-báo-của-bardock)
   - [NV 33 — Phá vỡ giới hạn](#nv-33--phá-vỡ-giới-hạn)
   - [NV 34 — Vùng đất băng giá](#nv-34--vùng-đất-băng-giá)
   - [NV 35 — Con đường rắn độc](#nv-35--con-đường-rắn-độc)
   - [NV 36 — Cổng phi thuyền](#nv-36--cổng-phi-thuyền)
   - [NV 37 — Mabư](#nv-37--mabư)
   - [NV 38 — Black Goku](#nv-38--black-goku)
   - [NV 39 — Cái giá của ký ức](#nv-39--cái-giá-của-ký-ức)
4. [Chương 6 — Lõi Hư Không (NV 40–47)](#4-chương-6--lõi-hư-không-nv-4047)
   - [NV 40 — Tổ Sư Kaio](#nv-40--tổ-sư-kaio)
   - [NV 41 — Cơn thịnh nộ Broly](#nv-41--cơn-thịnh-nộ-broly)
   - [NV 42 — Hành tinh ngục tù](#nv-42--hành-tinh-ngục-tù)
   - [NV 43 — Khí gas hủy diệt](#nv-43--khí-gas-hủy-diệt)
   - [NV 44 — Thử thách của Thần Hủy Diệt](#nv-44--thử-thách-của-thần-hủy-diệt)
   - [NV 45 — Bảy mảnh hợp nhất](#nv-45--bảy-mảnh-hợp-nhất)
   - [NV 46 — Heart](#nv-46--heart)
   - [NV 47 — Trả lại hay giữ lấy (nhánh A, id 47)](#nv-47--trả-lại-hay-giữ-lấy-nhánh-a-id-47)
   - [NV 50 — Trả lại hay giữ lấy (nhánh B, id 50)](#nv-50--trả-lại-hay-giữ-lấy-nhánh-b-id-50)
5. [Boss mới: Heart](#5-boss-mới-heart)
6. [Vật phẩm mới dùng ở 20c](#6-vật-phẩm-mới-dùng-ở-20c)
7. [Ghi chú / việc cần làm khi code](#7-ghi-chú--việc-cần-làm-khi-code)

---

## 1. Bảng tóm tắt 16 nhiệm vụ + nhánh kết

| NV | Tên | Chương | Map chính | Số bước | Kiểu bước | Boss / mốc chính | SM khi xong | Mở khóa |
|---|---|---|---|---|---|---|---|---|
| 32 | Lời cảnh báo của Bardock | 5 | 160 Khu hang động | 6 | A3, A12, A3, A1, A4, A3 | — | 2,5 tỷ | Map 156–163 |
| 33 | Phá vỡ giới hạn | 5 | 43 Vách núi Moori | 6 | A6, A3, B11, **B10**, A5, A3 | Mở giới hạn lần 1 | 3 tỷ | Mở giới hạn, NPC Quốc Vương / Tổ Sư Kaio |
| 34 | Vùng đất băng giá | 5 | 105–110 | 6 | A6, A1, **B12**, A4, A2, A3 | Cooler (-29) 2 form | 3,5 tỷ | Map 105–110 |
| 35 | Con đường rắn độc | 5 | 141–144 | 5 | A3, **B13**, A1, **B1**, A3 | Cađích (-15) | 4 tỷ | Phó bản CĐRĐ |
| 36 | Cổng phi thuyền | 5 | 114–117 | 5 | A3, A6, A2, A6, A3 | Drabura (-233) | 5 tỷ | Phó bản Mabư 12h |
| 37 | Mabư | 5 | 120, 127 | 5 | A3, A2, A2, A4, A3 | Mabư (-236) / Mabư 14h (-214) | 6 tỷ | Phó bản Mabư 14h |
| 38 | Black Goku | 5 | 92–100, 102 | 6 | A3, A1, A2, A2, A4, A3 | Black Goku (-203) 2 form | 7 tỷ | Boss Black Goku |
| 39 | **Cái giá của ký ức** | 5 | 85–91, 14 | 7 | A3, A4, **B6**, A3, A1, A3, A2 | Baby (-925) 3 form | 8 tỷ | Mảnh #5 & #6, Ngọc Rồng Sao Đen |
| 40 | Tổ Sư Kaio | 6 | 50 Thánh địa Kaio | 5 | A3, A6, A3, A12, A3 | — | 10 tỷ | Map thánh địa 50/116 |
| 41 | Cơn thịnh nộ Broly | 6 | 27–38 | 5 | A3, A1, A2, A2, A3 | Broly (-1822), Super Broly (-82282) | 11,5 tỷ | Boss Broly |
| 42 | Hành tinh ngục tù | 6 | 155 | 6 | A3, A6, **B12**, A2, A2, A3 | Cumber (-203999) 2 form | 13 tỷ | Map 155 |
| 43 | Khí gas hủy diệt | 6 | 147–152 | 6 | A3, A1, A2, A2, **B1**, A3 | Dr Lychee (-208), Hatchiyack (-207) | 14 tỷ | Phó bản khí gas |
| 44 | Thử thách của Thần Hủy Diệt | 6 | 154 Hành tinh Bill | 6 | A3, A3, **B5**, A2, **B10**, A3 | Whis (-364); mở giới hạn lần 2 | 16 tỷ | Map 154, Whis |
| 45 | Bảy mảnh hợp nhất | 6 | 78 Lãnh địa Fize | 5 | A6, A4, **B13**, A12, A3 | — | 17 tỷ | Mảnh Ký Ức #7 |
| 46 | Heart | 6 | 166 → 145 | 6 | A3, A2, A6, A2, A2, A3 | **Heart (-108108) 3 form** | 17,5 tỷ | Boss Heart, map 145 |
| 47 | **Trả lại hay giữ lấy** (nhánh A) | 6 | 145 Võ Đài Siêu Cấp | 6 | **B14**, A12, A6, **B10**, A3, A2 | Hư Không Vô Danh (-108108 form 4) | 18 tỷ + mở giới hạn | Danh hiệu "Người Trả Ký Ức", hậu truyện |
| 50 | **Trả lại hay giữ lấy** (nhánh B) | 6 | 155 Hành tinh ngục tù | 6 | **B14**, A12, A6, **B10**, A3, A2 | Hư Không Vô Danh (-108108 form 4) | 18 tỷ + mở giới hạn | Danh hiệu "Kẻ Giữ Hư Không", hậu truyện |

**Nhánh kết (§7 của file 20):**

| Lựa chọn tại NV 47 bước 0 | Task id | Nội dung khác biệt | Danh hiệu |
|---|---|---|---|
| "Trả ký ức cho vũ trụ" | **47** | Trả Lõi tại 145 Võ Đài Siêu Cấp trước mặt Thiên Sứ Whis; mở giới hạn nhờ Tổ Sư Kaio | ITEM MỚI 2120 "Người Trả Ký Ức" |
| "Giữ Lõi Hư Không" | **50** | Hấp thụ Lõi tại 155 Hành tinh ngục tù, nơi Heart nuôi Cumber; mở giới hạn bằng chính sức mạnh Lõi | ITEM MỚI 2121 "Kẻ Giữ Hư Không" |

Hai nhánh **cùng giá trị thưởng** (8 tỷ SM & TN, 2 bộ Ngọc Rồng 14–20, 1 danh hiệu),
chỉ khác lời thoại, map của 3 bước cuối và tên danh hiệu. Cả hai đều kết thúc tuyến (`sendNextTaskMain` → hết).

**Phân bố trigger mới trong 20c** (đúng luật §4.3: mỗi chương đúng 1 B12 và 1 B13):

| Chương | B12 (đếm giờ) | B13 (làm cùng người) | B1 | B5 | B6 | B10 | B11 | B14 |
|---|---|---|---|---|---|---|---|---|
| 5 | NV 34 bước 2 | NV 35 bước 1 | NV 35 bước 3 | — | NV 39 bước 2 | NV 33 bước 3 | NV 33 bước 2 | — |
| 6 | NV 42 bước 2 | NV 45 bước 2 | NV 43 bước 4 | NV 44 bước 2 | — | NV 44 bước 4, NV 47/50 bước 3 | — | NV 47 bước 0 |

---

## 2. Quy ước đọc bảng bước

| Cột | Ý nghĩa |
|---|---|
| **#** | `taskMain.index` — chỉ số bước con trong `task_sub_template` |
| **Tên bước** | cột `task_sub_template.NAME`, hiển thị nguyên văn cho người chơi |
| **Kiểu** | mã trigger theo §4 của file 20 (A1–A12 đã có hàm, B1–B14 phải thêm code) |
| **Điều kiện server kiểm tra** | điều kiện thật trong `TaskService`: mob `tempId`, boss id âm + `currentLevel` (form), npc id, map id, mốc SM, item id |
| **Số lượng** | `task_sub_template.max_count` |
| **Hằng số** | `ConstTask.TASK_<taskId>_<index>`, giá trị `((taskId<<10)+index)<<1` |
| **Thưởng bước** | trao qua `addDoneSubTask` (§6.3 file 20: 10% mức thưởng của cả nhiệm vụ) |

Giá trị hằng số để đối chiếu: `TASK_32_0 = 65536`, `TASK_39_0 = 79872`, `TASK_40_0 = 81920`, `TASK_47_0 = 96256`, `TASK_50_0 = 102400`.
Công thức: `TASK_x_y = (x*1024 + y) * 2`.

**Mức thưởng bước theo nhiệm vụ** (dùng lại cho mọi bước của nhiệm vụ đó):

| NV | Thưởng mỗi bước | NV | Thưởng mỗi bước |
|---|---|---|---|
| 32 | +80.000.000 SM & TN | 40 | +400.000.000 SM & TN |
| 33 | +100.000.000 SM & TN | 41 | +450.000.000 SM & TN |
| 34 | +130.000.000 SM & TN | 42 | +500.000.000 SM & TN |
| 35 | +160.000.000 SM & TN | 43 | +550.000.000 SM & TN |
| 36 | +200.000.000 SM & TN | 44 | +600.000.000 SM & TN |
| 37 | +230.000.000 SM & TN | 45 | +650.000.000 SM & TN |
| 38 | +260.000.000 SM & TN | 46 | +700.000.000 SM & TN |
| 39 | +300.000.000 SM & TN | 47 / 50 | +800.000.000 SM & TN |

> Mọi khoản SM đều đi qua `Service.addSMTN` nên **bị cắt bởi giới hạn sức mạnh**. Đó là lý do NV 33 dạy mở giới hạn
> **trước** khi chương 5 bắt đầu trả thưởng tỷ, và NV 44 bắt mở giới hạn lần hai trước khi chương 6 trả 6–8 tỷ/nhiệm vụ.

---

## 3. Chương 5 — Vương triều bóng tối (NV 32–39)

> Mở đầu chương: người chơi vừa đối mặt bản sao của chính mình (NV 31). Bardock — kẻ nhìn thấy tương lai —
> gọi người chơi tới Hành tinh thực vật để báo một tin: Heart không đi tìm người chơi, hắn **đợi** người chơi gom đủ mảnh.
> Cao trào chương: Bardock tự xóa ký ức của mình để Baby không lần ra Mảnh Ký Ức trong người chơi (NV 39).

### NV 32 — Lời cảnh báo của Bardock

**Chương** 5 — Vương triều bóng tối | **Mốc SM khi xong** 2.500.000.000 | **Nhiệm vụ kế** 33

*Máy dò của Bunma tương lai bắt được một tín hiệu cũ hơn cả vết nứt, phát ra từ một hành tinh chưa từng có trên bản đồ.
Bardock đã đợi người chơi ở đó từ lâu — ông nhìn thấy trước cảnh người chơi đứng trước Lõi Hư Không.
Điều ông không nhìn thấy là người chơi sẽ chọn gì.*

| # | Tên bước (hiển thị cho người chơi) | Kiểu | Điều kiện server kiểm tra | Số lượng | Map (id + tên) | NPC (id + tên) | Hằng số | Thưởng bước |
|---|---|---|---|---|---|---|---|---|
| 0 | Gặp Bunma ở nhà Bunma | A3 | `checkDoneTaskTalkNpc` — npc **37 Bunma (tương lai)**, map 102 | 1 | 102 Nhà Bunma | 37 Bunma (tương lai) | `TASK_32_0` | +80M SM & TN, **nhận 1 Nhẫn thời không sai lệch (992)** |
| 1 | Dùng Nhẫn thời không sai lệch | A12 | `checkDoneTaskUseItem` — item **992 Nhẫn thời không sai lệch** (`UseItem` → `Controller` type 2 → map 160) | 1 | — | — | `TASK_32_1` | +80M SM & TN |
| 2 | Nói chuyện với Bardock | A3 | `checkDoneTaskTalkNpc` — npc **70 Bardock**, map 160 | 1 | 160 Khu hang động | 70 Bardock | `TASK_32_2` | +80M SM & TN |
| 3 | Dọn sạch hang động nguyên thủy | A1 | `checkDoneTaskKillMob` — mob tempId **80 Cabira** hoặc **81 Tobi** | 40 | 160 Khu hang động, 161 Bìa rừng nguyên thủy, 162 Rừng nguyên thủy | — | `TASK_32_3` | +80M SM & TN |
| 4 | Nhặt Mảnh Ký Ức Vỡ | A4 | `checkDoneTaskPickItem` — item **ITEM MỚI 2103 "Mảnh Ký Ức Vỡ"** (icon mượn 6467 của 674 Đá ngũ sắc); chỉ rơi từ mob 81 Tobi khi `getIdTask == TASK_32_4` (cơ chế `Mob.dropItemTask`) | 3 | 161, 162, 163 Làng Plant nguyên thủy | — | `TASK_32_4` | +80M SM & TN |
| 5 | Báo cáo với Bardock | A3 | `checkDoneTaskTalkNpc` — npc **70 Bardock**, map 160 → trừ 3 item 2103 | 1 | 160 Khu hang động | 70 Bardock | `TASK_32_5` | +80M SM & TN |

- **Lời thoại**
  - NPC 37 Bunma (tương lai): "Máy dò bắt được sóng cũ hơn cả vết nứt. Nó phát ra từ một nơi không có trên bản đồ."
  - NPC 37 Bunma (tương lai): "Cầm chiếc nhẫn này. Nó lệch thời gian, nên nó tới được chỗ đó."
  - NPC 70 Bardock: "Ta đợi ngươi mười hai năm rồi. Trong đầu ta, ngươi đã tới đây một trăm lần."
  - NPC 70 Bardock: "Heart không đi tìm ngươi. Hắn ngồi yên, đợi ngươi gom đủ bảy mảnh giùm hắn."
  - NPC 70 Bardock: "Nhặt mấy mảnh vỡ ngoài kia về đây. Ta sẽ cho ngươi xem thứ ta thấy."
- **Mô tả nhiệm vụ (cột `detail` DB)**
  > Bunma đưa bạn Nhẫn thời không sai lệch để tới Hành tinh thực vật.
  > Hãy tìm Bardock, dọn sạch bọn Cabira và nhặt 3 Mảnh Ký Ức Vỡ mang về cho ông.
  > Thưởng 800.000.000 sức mạnh — Thưởng 800.000.000 tiềm năng
  > Thưởng 30 Đậu thần cấp 8
- **Thưởng khi hoàn thành**: +800.000.000 SM, +800.000.000 TN, **30× Đậu thần cấp 8 (352)**.
- **Mở khóa**: map **156 Tây thánh địa, 157 Đông thánh Địa, 158 Bắc thánh địa, 159 Nam thánh Địa, 160–163 Hành tinh thực vật** (mốc `TASK_32_1`).
- **Ghi chú triển khai**
  - `A12` dùng `checkDoneTaskUseItem` — hàm đã có, `switch` đang rỗng: thêm `case 992 → doneTask(TASK_32_1)` trong `TaskService`, móc tại `services_func/UseItem.java`.
  - Map 156–159 hiện chỉ vào được qua **Giu-ma Đầu Bò (47)** ở map 153 với điều kiện **SM ≥ 40.000.000.000** (`npc_list/GiuMaDauBo.java`). Phải đổi điều kiện đó sang `TaskService.getIdTask(player) >= ConstTask.TASK_32_1`, nếu không người chơi 2,5 tỷ SM không bao giờ vào được.
  - Map 160–163 vào bằng item 992 (`services_func/UseItem.java` + `Controller` type 2) — giữ nguyên, chỉ cần bảo đảm bước 0 trao item.
  - Thêm `case ConstNpc.BARDOCK` vào `checkDoneTaskTalkNpc`: Bardock hiện chỉ có menu "ok" không làm gì (`npc_list/Bardock.java`), rất hợp để gắn nhiệm vụ.
  - Thêm rơi ITEM MỚI 2103 trong `mob/Mob.dropItemTask` theo mẫu Đùi gà (73) của tuyến cũ.
  - **NV 32 không trao vàng/ngọc.** Bản thiết kế cũ để nhiệm vụ này phát 100 triệu vàng làm vốn cho người chơi trả 50 triệu mở giới hạn nhanh ở NV 33; chuỗi đó **đã bỏ** — NV 33 nay mở giới hạn **miễn phí** qua `OpenPowerService.openPowerByTask` (xem ghi chú NV 33).

---

### NV 33 — Phá vỡ giới hạn

**Chương** 5 — Vương triều bóng tối | **Mốc SM khi xong** 3.000.000.000 | **Nhiệm vụ kế** 34

*Bardock chỉ ra thứ Heart thật sự sợ: không phải sức mạnh, mà là một người còn nhớ và còn lớn lên được.
Cơ thể người chơi đã chạm trần — HP gốc không lên nữa, tiềm năng đổ vào đâu cũng rơi ra ngoài.
Quốc Vương của Namếc là người duy nhất còn nhớ cách bẻ cái trần đó.*

| # | Tên bước (hiển thị cho người chơi) | Kiểu | Điều kiện server kiểm tra | Số lượng | Map (id + tên) | NPC (id + tên) | Hằng số | Thưởng bước |
|---|---|---|---|---|---|---|---|---|
| 0 | Tới vách núi của hành tinh bạn | A6 | `checkDoneTaskGoToMap` — map **42 Vách núi Aru / 43 Vách núi Moori / 44 Vách núi Kakarot** (placeholder `MAP_VACH_NUI`) | 1 | 42 / 43 / 44 | — | `TASK_33_0` | +100M SM & TN |
| 1 | Nói chuyện với Quốc Vương | A3 | `checkDoneTaskTalkNpc` — npc **42 Quốc Vương** | 1 | 43 Vách núi Moori (xem ghi chú) | 42 Quốc Vương | `TASK_33_1` | +100M SM & TN |
| 2 | Nâng HP gốc chạm trần 220.000 | B11 | `NPoint.increasePoint(type 0)` — `hpg >= 220.000` (= `getHpMpLimit()` khi `limitPower = 0`) | 1 | — | — | `TASK_33_2` | +100M SM & TN |
| 3 | Mở giới hạn sức mạnh | **B10** | `OpenPowerService` — `limitPower` tăng từ **0 → 1** qua nhánh MỚI `openPowerByTask` của npc **42 Quốc Vương**: **miễn phí, không tốn vàng, không kiểm tra SM**, chỉ mở khi `TaskService.getIdTask(player) == ConstTask.TASK_33_3` và **đúng một lần** cho mỗi bậc | 1 | 43 Vách núi Moori | 42 Quốc Vương | `TASK_33_3` | +100M SM & TN |
| 4 | Đạt 3 tỷ sức mạnh | A5 | `checkDoneTaskPower` — `power >= 3.000.000.000` (gọi từ `NPoint.powerUp`) | 1 | — | — | `TASK_33_4` | +100M SM & TN |
| 5 | Báo cáo với Quốc Vương | A3 | `checkDoneTaskTalkNpc` — npc **42 Quốc Vương** | 1 | 43 Vách núi Moori | 42 Quốc Vương | `TASK_33_5` | +100M SM & TN |

- **Lời thoại**
  - NPC 42 Quốc Vương: "Cơ thể ngươi đầy rồi. Đổ thêm tiềm năng vào cũng chỉ chảy ra ngoài thôi."
  - NPC 42 Quốc Vương: "Giới hạn không phải do trời đặt. Nó là chỗ tổ tiên ngươi dừng lại vì quên mất cách đi tiếp."
  - NPC 42 Quốc Vương: "Ta không lấy của ngươi đồng nào. Cái giá là thứ ta phải nhớ lại."
  - NPC 42 Quốc Vương: "Xong rồi. Từ giờ ngươi lớn tới đâu, ta mở tới đó. Đừng chết trước khi ta kịp mở."
  - NPC 70 Bardock: "Heart không sợ kẻ mạnh. Hắn sợ kẻ còn nhớ và còn lớn lên được. Ngươi vừa thành cả hai."
- **Mô tả nhiệm vụ (cột `detail` DB)**
  > HP gốc của bạn đã chạm trần. Hãy tìm Quốc Vương ở vách núi để mở giới hạn sức mạnh lần đầu, rồi đạt 3 tỷ sức mạnh.
  > Thưởng 1.000.000.000 sức mạnh — Thưởng 1.000.000.000 tiềm năng
  > Thưởng 50 Đậu thần cấp 8 — Thưởng 5 Đá bảo vệ
- **Thưởng khi hoàn thành**: +1.000.000.000 SM, +1.000.000.000 TN, **50× Đậu thần cấp 8 (352)**, **5× Đá bảo vệ (987)**.
- **Mở khóa**: **tính năng mở giới hạn sức mạnh**; NPC **42 Quốc Vương** (limitPower 0–4) và **43 Tổ Sư Kaio** (limitPower 5–8) hiện ra với người chơi.
- **Ghi chú triển khai** — **đây là nhiệm vụ then chốt của cả tuyến, sai là hỏng toàn bộ thưởng chương 5–6:**
  - `Service.addSMTN` **bỏ qua** mọi khoản SM khi `power >= getPowerLimit()`. Với `limitPower = 0` trần là **17.999.999.999**. Chương 5 thưởng tổng ~12,6 tỷ SM và chương 6 ~48 tỷ SM, nên **phải mở giới hạn ở NV 33** rồi mới trả các khoản đó, đúng như §6.2 file 20 đã cảnh báo.
  - **Chặn cứng hiện tại:** `npc_list/QuocVuong.java` chỉ hiển thị NPC khi **SM ≥ 17.000.000.000**, còn `OpenPowerService.openPowerBasic` yêu cầu `power >= getPowerLimit()` (≈18 tỷ). Ở mốc 3 tỷ cả hai đều không thỏa. Vì **nhiệm vụ không còn phát vàng**, người chơi cũng không thể dùng nhánh trả tiền `openPowerSpeed` để đi tiếp. Phải sửa:
    1. Điều kiện hiện NPC Quốc Vương: `SM ≥ 17 tỷ` **hoặc** `TaskService.getIdTask(player) >= ConstTask.TASK_33_1` (`npc_list/QuocVuong.java`).
    2. **Thêm nhánh menu mới "Phá giới hạn (nhiệm vụ)"** ở `npc_list/QuocVuong.java` và `npc_list/ToSuKaio.java`. Nhánh này **chỉ hiện** khi `TaskService.getIdTask(player)` đang đúng bước B10 của tuyến (`TASK_33_3`, `TASK_44_4`, `TASK_47_3`, `TASK_50_3`), và gọi hàm MỚI `OpenPowerService.openPowerByTask(player)`.
    3. `openPowerByTask` = **mở giới hạn miễn phí một lần**: `limitPower++` ngay lập tức, **không trừ vàng, không trừ ngọc, không chờ 2,4 giờ, không kiểm tra `power >= getPowerLimit()`**, rồi gọi `TaskService.gI().checkDoneTaskOpenPower(player)`. Chống lạm dụng bằng cách so `limitPower` hiện tại với bậc mà bước nhiệm vụ cho phép (`TASK_33_3` → chỉ chạy khi `limitPower == 0`; `TASK_44_4` → `limitPower == 1`; `TASK_47_3`/`TASK_50_3` → `limitPower == 2`), nên mỗi bậc chỉ mở được đúng một lần dù người chơi bấm lại.
    4. **Giữ nguyên hai nhánh cũ** cho nội dung ngoài tuyến: `openPowerBasic` (chờ 2,4 giờ, đòi đã chạm trần) và `openPowerSpeed` (**tốn 50.000.000 vàng**, không kiểm tra SM). Tuyến nhiệm vụ chỉ **không phụ thuộc** vào chúng nữa, chứ luật giá của chúng không đổi.
    5. Hệ quả: **NV 32 không cần thưởng vàng nữa**; chuỗi cũ "NV 32 phát 100 triệu vàng → NV 33 trả 50 triệu" đã bị bỏ hẳn.
  - NPC **42 Quốc Vương chỉ tồn tại ở map 43 Vách núi Moori** trong `map_template`. Hai hành tinh còn lại không có. Chọn 1 trong 2 cách: (a) thêm dòng NPC 42 vào `map_template.npcs` của map **42** và **44**; (b) giữ nguyên và để bước 0 là placeholder `MAP_VACH_NUI` còn bước 1–5 ép về map 43 (người Trái Đất/Xayda phải bay sang Namếc). **Khuyến nghị (a)** — tránh kẹt người chơi.
  - **B10** — móc mới trong `services/OpenPowerService.java`: sau khi `limitPower++` thành công (cả `openPowerBasic` hết 2,4 giờ lẫn `openPowerSpeed`) gọi `TaskService.gI().checkDoneTaskOpenPower(player)`.
  - **B11** — móc mới trong `player/NPoint.increasePoint(type, point)`: sau khi cộng thành công `type == 0` (HP gốc), kiểm tra `hpg >= getHpMpLimit()` → `doneTask(TASK_33_2)`. Đây là mở rộng của `checkDoneTaskNangCS` (vốn chỉ xét `type 2` sức đánh gốc).
  - Chi phí bước 2 để tham khảo: nâng HP gốc 0 → 220.000 tốn khoảng **1,22 tỷ tiềm năng** — vừa tầm người chơi 2,5–3 tỷ SM, không gây kẹt.
  - ⚠️ **Lời thoại cần chủ dự án duyệt lại:** câu của NPC 42 Quốc Vương *"Ta lấy năm mươi triệu vàng…"* ở trên được **giữ nguyên văn** theo yêu cầu không sửa thoại, nhưng nó **không còn khớp** với cơ chế mới (mở giới hạn nhiệm vụ là miễn phí). Đề nghị chủ dự án chốt câu thay thế — ví dụ: *"Ta không lấy của ngươi đồng nào. Cái giá là thứ ta phải nhớ lại."*

---

### NV 34 — Vùng đất băng giá

**Chương** 5 — Vương triều bóng tối | **Mốc SM khi xong** 3.500.000.000 | **Nhiệm vụ kế** 35

*Một trong bảy mảnh nằm dưới lớp băng của hành tinh Cold, và Cooler đã ngồi trên nó nhiều năm mà không biết đó là gì.
Cái lạnh ở đây không chỉ rút máu — nó làm người ta quên cả lý do mình tới.
Người chơi chỉ có vài phút trước khi chính mình cũng quên.*

| # | Tên bước (hiển thị cho người chơi) | Kiểu | Điều kiện server kiểm tra | Số lượng | Map (id + tên) | NPC (id + tên) | Hằng số | Thưởng bước |
|---|---|---|---|---|---|---|---|---|
| 0 | Tới Cánh đồng tuyết | A6 | `checkDoneTaskGoToMap` — map **105 Cánh đồng tuyết** | 1 | 105 Cánh đồng tuyết | — | `TASK_34_0` | +130M SM & TN |
| 1 | Diệt bọn canh băng | A1 | `checkDoneTaskKillMob` — mob tempId **66 Tai tím** hoặc **67 Abo** | 50 | 105 Cánh đồng tuyết, 106 Rừng tuyết, 107 Núi tuyết | — | `TASK_34_1` | +130M SM & TN |
| 2 | Hạ 20 Kado trong 5 phút | **B12** | `doneTask` + `TaskMain.lastTime`: đếm ngược **300.000 ms** kể từ khi vào bước; hạ đủ **20** mob tempId **68 Kado** trước khi hết giờ, quá hạn → `count = 0`, `lastTime = now` (kiểm tra trong `Player.update`) | 20 | 108 Dòng sông băng, 109 Rừng băng | — | `TASK_34_2` | +130M SM & TN |
| 3 | Nhặt Mảnh Ký Ức Đóng Băng | A4 | `checkDoneTaskPickItem` — item **ITEM MỚI 2104 "Mảnh Ký Ức Đóng Băng"** (icon mượn 8620 của 935 Đá xanh lam); chỉ hiện với người đang ở `TASK_34_3` (cơ chế `Zone.getItemMapsForPlayer`) | 1 | 110 Hang băng | — | `TASK_34_3` | +130M SM & TN |
| 4 | Hạ Cooler | A2 | `checkDoneTaskKillBoss` — boss **-29 Cooler**, `currentLevel = 0` (Cooler, HP 200M) **và** `currentLevel = 1` (Cooler 2, HP 500M) | 2 | 110 Hang băng | — | `TASK_34_4` | +130M SM & TN |
| 5 | Báo cáo với Bardock | A3 | `checkDoneTaskTalkNpc` — npc **70 Bardock**, map 160 → trừ 1 item 2104 | 1 | 160 Khu hang động | 70 Bardock | `TASK_34_5` | +130M SM & TN |

- **Lời thoại**
  - NPC 70 Bardock: "Dưới lớp băng đó có một mảnh. Cooler ngồi lên nó cả chục năm mà không biết đó là gì."
  - NPC 70 Bardock: "Cái lạnh chỗ đó ăn trí nhớ. Đứng lâu quá, ngươi sẽ quên mình xuống đó làm gì."
  - Boss -29 Cooler (textS sẵn có): "Ta sẽ cho chúng bây biết sức mạnh thực sự của dân tộc Frost Demons."
  - Boss -29 Cooler (textE form 2 sẵn có): "Mọi chuyện chưa kết thúc đâu."
  - NPC 70 Bardock: "Mảnh này lạnh hơn băng. Nó là ký ức của người đã chết cóng mà vẫn cố nhớ tên ai đó."
- **Mô tả nhiệm vụ (cột `detail` DB)**
  > Một Mảnh Ký Ức bị đóng băng trong Hang băng, do Cooler canh giữ.
  > Hãy diệt bọn canh băng, sống sót 5 phút giữa bầy Kado, lấy mảnh và hạ Cooler cả hai dạng.
  > Thưởng 1.300.000.000 sức mạnh — Thưởng 1.300.000.000 tiềm năng
  > Thưởng 1 Bông tai Porata
- **Thưởng khi hoàn thành**: +1.300.000.000 SM, +1.300.000.000 TN, **1× Bông tai Porata (454)**.
- **Mở khóa**: map **105 Cánh đồng tuyết, 106 Rừng tuyết, 107 Núi tuyết, 108 Dòng sông băng, 109 Rừng băng, 110 Hang băng** (mốc cũ `TASK_27_0` → mốc mới `TASK_34_0`, `ChangeMapService.checkMapCanJoin`).
- **Ghi chú triển khai**
  - **B12** (trigger mới): dùng sẵn `TaskMain.lastTime` (đã có trong `data_task` JSON `[id, index, count, lastTime]`, đã được `PlayerDAO` ghi/đọc). Đặt `lastTime = System.currentTimeMillis()` khi `index` chuyển sang 2; `Player.update` mỗi tick kiểm tra `now - lastTime > 300_000` → reset `subTasks[2].count = 0`, đặt lại `lastTime`, `sendUpdateCountSubTask` + thông báo "Hết giờ, làm lại từ đầu". Cần gửi thời gian còn lại xuống client qua message 43 để người chơi thấy đồng hồ.
  - **Cooler không gọi nhiệm vụ**: theo `11-boss.md` mục 9.17, `boss/Cold/Cooler.java` override `reward()` mà **không** gọi `TaskService.checkDoneTaskKillBoss`. Phải thêm dòng đó vào `Cooler.reward()`, nếu không bước 4 không bao giờ xong.
  - Map 105–110 là **map lạnh** (`MapService.isMapCold`): người chơi không có option **106 "Không ảnh hưởng bởi cái lạnh"** bị **giảm 50% HP và sức đánh**. Bước 0 nên trao kèm 1 bùa/cải trang có option 106 hạn 1 ngày, hoặc ghi rõ cảnh báo trong `detail`.
  - Cooler nghỉ **30 phút** giữa các lần xuất hiện, 1 con toàn server (`loadBoss` ×1) → bước 4 là điểm nghẽn tranh boss. Cân nhắc nâng số lượng Cooler trong `BossManager.loadBoss()` lên 2–3 con khi triển khai tuyến mới.

---

### NV 35 — Con đường rắn độc

**Chương** 5 — Vương triều bóng tối | **Mốc SM khi xong** 4.000.000.000 | **Nhiệm vụ kế** 36

*Thần Vũ Trụ nói thẳng: muốn biết Heart lấy ký ức đem đi đâu, phải hỏi người đã chết.
Con đường rắn độc là lối duy nhất sang thế giới bên kia, và nó dài đến mức không ai đi một mình mà tới nơi.
Lần đầu tiên tuyến nhiệm vụ bắt người chơi phải rủ đúng một người bạn.*

| # | Tên bước (hiển thị cho người chơi) | Kiểu | Điều kiện server kiểm tra | Số lượng | Map (id + tên) | NPC (id + tên) | Hằng số | Thưởng bước |
|---|---|---|---|---|---|---|---|---|
| 0 | Nói chuyện với Thần Vũ Trụ | A3 | `checkDoneTaskTalkNpc` — npc **20 Thần Vũ Trụ**, map 48 | 1 | 48 Hành tinh Kaio | 20 Thần Vũ Trụ | `TASK_35_0` | +160M SM & TN |
| 1 | Vào Con đường rắn độc cùng bạn | **B13** | `SnakeWayService.openConDuongRanDoc` thành công **và** có **≥ 2 người chơi thật cùng bang** trong cùng zone của map **143** (mẫu `NMEMBER_DO_TASK_TOGETHER` của nhiệm vụ bang) | 1 | 143 Con đường rắn độc | 20 Thần Vũ Trụ | `TASK_35_1` | +160M SM & TN |
| 2 | Dọn đường qua ba chặng | A1 | `checkDoneTaskKillMob` — mob tempId **24 Quỷ mập**, **33 Quỷ địa ngục**, **25 Tambourine**, **26 Drum**, **49 Dơi da xanh**, **50 Quỷ chim** trong map 141–143 | 60 | 141, 142, 143 Con đường rắn độc | — | `TASK_35_2` | +160M SM & TN |
| 3 | Hoàn thành Con đường rắn độc | **B1** | `SnakeWay.finish()` / cờ `endCDRD` — tức **Cađích (-15)** đã rời map 144 Hoang mạc | 1 | 144 Hoang mạc | — | `TASK_35_3` | +160M SM & TN |
| 4 | Gặp Thượng Đế ở Thần điện | A3 | `checkDoneTaskTalkNpc` — npc **19 Thượng Đế**, map **45 Thần điện** | 1 | 45 Thần điện | 19 Thượng Đế | `TASK_35_4` | +160M SM & TN |

- **Lời thoại**
  - NPC 20 Thần Vũ Trụ: "Ngươi hỏi ký ức bị lấy đi đâu à? Hỏi người sống thì vô ích. Hỏi người chết đi."
  - NPC 20 Thần Vũ Trụ: "Con đường rắn độc dài lắm. Đi một mình thì tới giữa đường là quên mất đích."
  - NPC 19 Thượng Đế: "Bên kia không có ai quên cả. Người chết giữ nguyên mọi thứ họ mang theo."
  - NPC 19 Thượng Đế: "Và họ nói với ta: mấy năm nay có kẻ đem ký ức người sống xuống đây gửi. Kẻ đó tên Heart."
- **Mô tả nhiệm vụ (cột `detail` DB)**
  > Thần Vũ Trụ mở Con đường rắn độc cho bạn, nhưng bạn phải đi cùng ít nhất một thành viên bang.
  > Dọn sạch ba chặng, hạ Cađích rồi về Thần điện gặp Thượng Đế.
  > Thưởng 1.600.000.000 sức mạnh — Thưởng 1.600.000.000 tiềm năng
  > Thưởng 5 Đá nâng cấp cấp 5
- **Thưởng khi hoàn thành**: +1.600.000.000 SM, +1.600.000.000 TN, **5× Đá nâng cấp cấp 5 (1078)**.
- **Mở khóa**: **phó bản Con đường rắn độc** (menu "Di chuyển → Con đường rắn độc" của NPC 20 Thần Vũ Trụ tại map 48).
- **Ghi chú triển khai**
  - **B13** (trigger mới): mẫu đã có sẵn ở nhiệm vụ bang (`NMEMBER_DO_TASK_TOGETHER`, xem `checkDoneTaskKillMob` của task 14/15). Viết `TaskService.checkDoneTaskTogether(player, zone, nMember)`: đếm `zone.getPlayers()` là người thật (`!isBot && !isPet && !isBoss`), cùng `player.clan`, ≥ 2 → `doneTask`.
  - **B1** (trigger mới): móc vào `map/phoban/SnakeWay.finish()` và `boss_con_duong_ran_doc/CADICH.leaveMap()` (chỗ đặt `endCDRD = true`) — duyệt mọi người chơi trong instance gọi `TaskService.gI().checkDoneTaskDungeon(pl, ConstMap.MAP_CON_DUONG_RAN_DOC)`. Làm y hệt cho Doanh trại (`RedRibbonHQ`), BĐKB (`BanDoKhoBau`), Khí gas (`DestronGas`) để dùng lại ở NV 43.
  - **Rào cản lớn nhất của NV 35** (phải quyết trước khi code): `SnakeWayService.openConDuongRanDoc` giới hạn **1 lần / 7 ngày / người chơi** và yêu cầu **vào bang ≥ 2 ngày**. Người chơi mới vào bang, hoặc vừa đi CĐRĐ hôm trước, sẽ **kẹt cứng ở NV 35 tới 7 ngày**. Ba cách xử lý, chọn 1:
    1. Bỏ kiểm tra 7 ngày khi `getIdTask(player)` đang nằm trong `TASK_35_1..TASK_35_3` (khuyến nghị).
    2. Cho bước 3 cũng hoàn thành khi người chơi hoàn thành **Doanh Trại Độc Nhãn** hoặc **Bản đồ kho báu** (B1 chấp nhận 3 loại phó bản).
    3. Hạ điều kiện "vào bang ≥ 2 ngày" xuống 0 ngày cho người đang ở NV 35.
  - Lỗi cũ nên sửa luôn: NPC Thần Vũ Trụ cho chọn cấp khi vào bang ≥ 1 ngày nhưng service đòi ≥ 2 ngày rồi `return` **im lặng** — người chơi bấm mà không hiểu vì sao không có gì xảy ra (`14-ban-do-pho-ban.md` mục 16.13).
  - `SAIBAMEN.afk()` gọi `Functions.sleep(1500)` và `CADICH.attack()` gọi `sleep(2000)` **chặn cả thread `SnakeWayManager`** (mọi CĐRĐ của mọi bang). Nên sửa trước khi CĐRĐ thành nhiệm vụ bắt buộc.

---

### NV 36 — Cổng phi thuyền

**Chương** 5 — Vương triều bóng tối | **Mốc SM khi xong** 5.000.000.000 | **Nhiệm vụ kế** 37

*Người chết nói Heart gửi ký ức xuống một cỗ phi thuyền cổ do phù thủy Babiđây trông coi.
Cổng phi thuyền chỉ mở đúng một giờ mỗi ngày, và Drabura đứng ngay cửa.
Ai tới sai giờ thì phải đi đường vòng qua sa mạc — Ôsin biết đường đó.*

| # | Tên bước (hiển thị cho người chơi) | Kiểu | Điều kiện server kiểm tra | Số lượng | Map (id + tên) | NPC (id + tên) | Hằng số | Thưởng bước |
|---|---|---|---|---|---|---|---|---|
| 0 | Gặp Ôsin ở Đại hội võ thuật | A3 | `checkDoneTaskTalkNpc` — npc **44 Ôsin**, map **52 Đại hội võ thuật** | 1 | 52 Đại hội võ thuật | 44 Ôsin | `TASK_36_0` | +200M SM & TN, **nhận 1 Bình hút năng lượng (1795)** |
| 1 | Vào Cổng phi thuyền | A6 | `checkDoneTaskGoToMap` — map **114 Cổng phi thuyền** (`TimeUtil.isMabuOpen()` 12:00–12:59)<br>**HOẶC đường vòng:** map **165 Sa mạc hoang vu** (mở 24/7, vào bằng item 1795 qua Ôsin map 52) | 1 | 114 Cổng phi thuyền / 165 Sa mạc hoang vu | 44 Ôsin | `TASK_36_1` | +200M SM & TN |
| 2 | Hạ Drabura | A2 | `checkDoneTaskKillBoss` — boss **-233 Drabura** (map 114, HP 20M, tối đa 20M sát thương/đòn)<br>**HOẶC đường vòng:** `checkDoneTaskKillMob` — 50 mob tempId **95 Cadic M** / **118 Cadic M** ở map 165 | 1 (hoặc 50) | 114 Cổng phi thuyền / 165 Sa mạc hoang vu | — | `TASK_36_2` | +200M SM & TN |
| 3 | Xuống tới Cửa Ải 1 | A6 | `checkDoneTaskGoToMap` — map **117 Cửa Ải 1** (cần đủ `POINT_MAX = 10` điểm Mabư để Ôsin/Babiđây mở đường 114→115→117)<br>**HOẶC đường vòng:** `checkDoneTaskPickItem` — item **ITEM MỚI 2105 "Mảnh Bùa Babiđây"** (icon mượn 7743 của 861 Hồng ngọc) rơi từ Cadic M ở map 165 | 1 | 117 Cửa Ải 1 / 165 Sa mạc hoang vu | — | `TASK_36_3` | +200M SM & TN |
| 4 | Nói chuyện với Babiđây | A3 | `checkDoneTaskTalkNpc` — npc **46 Babiđây**, map 117 (chỉ phe `cFlag == 10`)<br>**HOẶC đường vòng:** npc **44 Ôsin** ở map 165 | 1 | 117 Cửa Ải 1 / 165 Sa mạc hoang vu | 46 Babiđây / 44 Ôsin | `TASK_36_4` | +200M SM & TN |

- **Lời thoại**
  - NPC 44 Ôsin: "Cổng phi thuyền chỉ mở lúc mười hai giờ. Trễ một phút là đứng ngoài tới mai."
  - NPC 44 Ôsin: "Nếu cậu không chờ được, cầm cái bình này. Sa mạc hoang vu cũng dẫn tới cùng chỗ, chỉ là bẩn hơn."
  - Boss -233 Drabura: "Phẹt." *(hóa đá 22 giây — thoại sẵn có)*
  - NPC 46 Babiđây: "Ngươi ngửi thấy chưa? Cả tầng dưới toàn mùi ký ức người sống. Chủ ta chất chúng ở đây."
  - NPC 46 Babiđây: "Ta chỉ là kẻ giữ kho. Kẻ gửi hàng thì ngươi biết rồi đấy."
- **Mô tả nhiệm vụ (cột `detail` DB)**
  > Ký ức bị Heart gửi vào cỗ phi thuyền của Babiđây. Cổng chỉ mở từ 12h đến 12h59.
  > Nếu tới sai giờ, hãy hỏi Ôsin đường vòng qua Sa mạc hoang vu.
  > Thưởng 2.000.000.000 sức mạnh — Thưởng 2.000.000.000 tiềm năng
  > Thưởng 50 Đậu thần cấp 8
- **Thưởng khi hoàn thành**: +2.000.000.000 SM, +2.000.000.000 TN, **50× Đậu thần cấp 8 (352)**.
- **Mở khóa**: **phó bản Mabư 12h** (map 114, 115, 117, 118, 119, 120) qua NPC 44 Ôsin ở map 52.
- **Ghi chú triển khai** — **đây là chỗ dễ kẹt người chơi nhất của chương 5 (§10 mục 4 file 20):**
  - **Đường vòng theo giờ thật** — thiết kế cụ thể: mỗi bước 1–4 có **hai điều kiện OR** trong cùng một hằng `TASK_36_x`, nên `data_task` vẫn chỉ lưu `[id, index, count, lastTime]`, không cần cờ phụ. Cách chọn nhánh do **chính người chơi** quyết định bằng việc bấm Ôsin lúc nào:
    - Trong khung **12:00–12:59** (`TimeUtil.isMabuOpen()`): Ôsin map 52 mở menu "OK" → map 114 như hiện tại.
    - Ngoài khung giờ: Ôsin map 52 hiện thêm nút **"Đường vòng Sa mạc"** (chỉ hiện khi `getIdTask` nằm trong `TASK_36_0..TASK_36_4`) → dùng item **1795 Bình hút năng lượng** → map **165 Sa mạc hoang vu** (mở 24/7, `Osin.java` đã có sẵn nhánh này).
    - Map 165 có mob **95 / 118 Cadic M** (HP 6.000.000) — thừa sức làm nguồn hoàn thành cho bước 2 và 3.
  - Vì đường vòng cần item 1795, **bước 0 phải trao item 1795** (không mất tiền). Nếu người chơi lỡ bán, cho Ôsin trao lại miễn phí khi đang ở NV 36.
  - Dùng **Drabura -233 (map 114)**, **tuyệt đối không dùng Drabura 2 (-237, map 119)**: theo `11-boss.md` mục 9.6, Drabura 2 chuyển thẳng `AFK → DIE` mà không qua `die()/reward()` nên **không bao giờ gọi `checkDoneTaskKillBoss`**.
  - Lỗi cũ chặn đường: `Osin.confirmMenu` để các `case 114..120` **bên trong** `switch(indexMenu)` của map 52 (so với `indexMenu` thay vì `mapId`) → menu "Xuống tầng dưới" của Ôsin trong tầng Mabư **không hoạt động** (`18-npc.md` mục 9.8). Người chơi phe Ôsin (cFlag 9) sẽ không xuống được tầng → bước 3 kẹt. **Phải sửa trước.** (Phe Babiđây cFlag 10 vẫn xuống được vì `Babiday.java` xử lý riêng.)
  - Map 116 Thánh địa Kaio nằm lọt trong dải 114–120 của `MapService.getMapMaBu()` — đừng đưa 116 vào điều kiện bước 1/3.

---

### NV 37 — Mabư

**Chương** 5 — Vương triều bóng tối | **Mốc SM khi xong** 6.000.000.000 | **Nhiệm vụ kế** 38

*Dưới tầng cuối của phi thuyền không có kho ký ức nào cả — chỉ có một sinh vật hồng đang ăn chúng.
Mabư không phải tay sai của Heart; nó là cái dạ dày mà Heart thuê.
Giết nó xong, người chơi nhặt được thứ Babiđây dùng để ra lệnh: Lõi Phép.*

| # | Tên bước (hiển thị cho người chơi) | Kiểu | Điều kiện server kiểm tra | Số lượng | Map (id + tên) | NPC (id + tên) | Hằng số | Thưởng bước |
|---|---|---|---|---|---|---|---|---|
| 0 | Gặp Ôsin ở Đại hội võ thuật | A3 | `checkDoneTaskTalkNpc` — npc **44 Ôsin**, map 52 | 1 | 52 Đại hội võ thuật | 44 Ôsin | `TASK_37_0` | +230M SM & TN |
| 1 | Hạ Mabư | A2 | `checkDoneTaskKillBoss` — boss **-236 Mabư** (map 120 Phòng chỉ huy, HP 100M, khung 12h)<br>**HOẶC** boss **-214 Mabư 14h** `currentLevel = 4` (**Kid Bư**, HP 150M, map 127, khung 14h — chỉ chết bởi **Quả cầu kênh khi**)<br>**HOẶC đường vòng 24/7:** mob tempId **70 Hirudegarn** (HP 40.000.000) ở map **126 Thành phố Santa** | 1 | 120 Phòng chỉ huy / 127 Cổng phi thuyền / 126 Thành phố Santa | — | `TASK_37_1` | +230M SM & TN |
| 2 | Hạ Drabura 3 | A2 | `checkDoneTaskKillBoss` — boss **-343 Drabura (3)** (được Mabư gọi khi chết, map 120, rời map sau 60 giây)<br>**HOẶC đường vòng:** boss **-348 Super Bư** (map 128 Bụng Mabư) **HOẶC** 30 mob tempId **50 Quỷ chim** ở map 126 | 1 (hoặc 30) | 120 / 128 / 126 | — | `TASK_37_2` | +230M SM & TN |
| 3 | Nhặt Lõi Phép Babiđây | A4 | `checkDoneTaskPickItem` — item **ITEM MỚI 2106 "Lõi Phép Babiđây"** (icon mượn 5829 của 638 Bình chứa Commeson); rơi 100% cho người kết liễu ở bước 2 | 1 | 120 / 128 / 126 | — | `TASK_37_3` | +230M SM & TN |
| 4 | Mang Lõi Phép cho Kibit | A3 | `checkDoneTaskTalkNpc` — npc **45 Kibit**, map **50 Thánh địa Kaio** → trừ 1 item 2106 | 1 | 50 Thánh địa Kaio | 45 Kibit | `TASK_37_4` | +230M SM & TN |

- **Lời thoại**
  - NPC 44 Ôsin: "Tầng cuối không phải kho. Nó là cái bụng. Thứ trong đó ăn ký ức thật, nhai thật."
  - Boss -236 Mabư: "Úm ba la xì bùa." *(thoại sẵn có, biến Sôcôla 30 giây)*
  - NPC 46 Babiđây: "Mabư không nghe Heart. Nó nghe cái lõi trong tay ta. Mà giờ cái lõi trong tay ngươi rồi."
  - NPC 45 Kibit: "Lõi này khắc tên chủ nhân thật. Không phải Babiđây. Là Heart."
  - NPC 45 Kibit: "Tổ Sư Kaio phải xem thứ này. Nhưng ngài chưa muốn gặp ngươi — chưa phải lúc."
- **Mô tả nhiệm vụ (cột `detail` DB)**
  > Hạ Mabư ở tầng cuối phi thuyền, nhặt Lõi Phép Babiđây và mang tới Kibit ở Thánh địa Kaio.
  > Ngoài giờ mở phó bản, hãy tìm Hirudegarn ở Thành phố Santa.
  > Thưởng 2.300.000.000 sức mạnh — Thưởng 2.300.000.000 tiềm năng
  > Thưởng 1 Bông tai Porata
- **Thưởng khi hoàn thành**: +2.300.000.000 SM, +2.300.000.000 TN, **1× Bông tai Porata (921)**.
- **Mở khóa**: **phó bản Mabư 14h** (map 127 Cổng phi thuyền, 128 Bụng Mabư) qua NPC 44 Ôsin ở map 52.
- **Ghi chú triển khai**
  - **Đường vòng theo giờ thật — ba cửa cho cùng một bước**: khung **12h** (Mabư 12h, boss -236), khung **14h** (Mabư 14h, boss -214 form 5), và **24/7** (Hirudegarn, mob 70 ở map 126). Cả ba cùng ghi vào `TASK_37_1`. Map 126 vào qua NPC **53 Tapion** ở map 19 Thành phố Vegeta (đã mở từ chương 3) nên luôn khả dụng.
  - Boss nhóm Mabư 12h/14h **đã gọi** `checkDoneTaskKillBoss` trong `reward()` (xem `11-boss.md` mục 2.7) — chỉ cần thêm case id vào bảng `TaskService`, không phải sửa lớp boss.
  - Hirudegarn là **mob** (tempId 70) chứ không phải boss → dùng `checkDoneTaskKillMob`, không dùng `checkDoneTaskKillBoss`.
  - **Kid Bư (form 5 của -214) chỉ nhận đòn kết liễu bằng "Quả cầu kênh khi"** — nếu người chơi không có chiêu đó sẽ không giết được. Vì thế nhánh 14h phải là **lựa chọn**, không bao giờ là bắt buộc; và `detail` nên nhắc rõ.
  - Drabura 3 (-343) **chỉ xuất hiện sau khi Mabư chết** và **tự rời map sau 60 giây** → bước 2 phải làm liền tay. Nếu thấy quá gắt khi test, cho phép bước 2 cũng hoàn thành bằng Super Bư (-348) như đã ghi trong bảng.
  - Rơi ITEM MỚI 2106: thêm vào `reward()` của `Mabu.java` / `Mabu2H.java` / `SuperBu.java` và vào `Mob.getItemMobReward` cho mob 70, tất cả đều gắn chủ `plKill.id`.

---

### NV 38 — Black Goku

**Chương** 5 — Vương triều bóng tối | **Mốc SM khi xong** 7.000.000.000 | **Nhiệm vụ kế** 39

*Bunma tương lai báo có một người mang khuôn mặt quen thuộc đang đi khắp Tương lai, xóa sạch từng thành phố một.
Hắn không nhớ mình là ai — hắn chỉ nhớ mình từng là ai đó quan trọng.
Đó là bản nháp đầu tiên của Heart: một người bị lấy hết ký ức rồi được lắp lại bằng ký ức vay mượn.*

| # | Tên bước (hiển thị cho người chơi) | Kiểu | Điều kiện server kiểm tra | Số lượng | Map (id + tên) | NPC (id + tên) | Hằng số | Thưởng bước |
|---|---|---|---|---|---|---|---|---|
| 0 | Gặp Bunma ở Tương lai | A3 | `checkDoneTaskTalkNpc` — npc **37 Bunma (tương lai)**, map 102 | 1 | 102 Nhà Bunma | 37 Bunma (tương lai) | `TASK_38_0` | +260M SM & TN |
| 1 | Dọn sạch bọn Xên con phía bắc | A1 | `checkDoneTaskKillMob` — mob tempId **62 Xên con cấp 5**, **63 Xên con cấp 6**, **64 Xên con cấp 7**, **65 Xên con cấp 8** | 60 | 97 Thành phố phía bắc, 98 Ngọn núi phía bắc, 99 Thung lũng phía bắc, 100 Thị trấn Ginder | — | `TASK_38_1` | +260M SM & TN |
| 2 | Hạ Black Goku | A2 | `checkDoneTaskKillBoss` — boss **-203 Black Goku**, `currentLevel = 0` (HP 500M, dame 50.000) | 1 | 92, 93, 94, 96, 97, 98, 99, 100, 102 | — | `TASK_38_2` | +260M SM & TN |
| 3 | Hạ Super Black Goku | A2 | `checkDoneTaskKillBoss` — boss **-203 Super Black Goku**, `currentLevel = 1` (HP 2 tỷ, dame 100.000) | 1 | 92, 93, 94, 96, 97, 98, 99, 100, 102 | — | `TASK_38_3` | +260M SM & TN |
| 4 | Nhặt Nhẫn thời không sai lệch | A4 | `checkDoneTaskPickItem` — item **992 Nhẫn thời không sai lệch** (Black Goku đã rơi sẵn item này ở khối 10% Mẫu B) | 1 | 92–100, 102 | — | `TASK_38_4` | +260M SM & TN |
| 5 | Báo cáo với Bunma | A3 | `checkDoneTaskTalkNpc` — npc **37 Bunma (tương lai)**, map 102 | 1 | 102 Nhà Bunma | 37 Bunma (tương lai) | `TASK_38_5` | +260M SM & TN |

- **Lời thoại**
  - NPC 37 Bunma (tương lai): "Có kẻ mang mặt người quen đang đi xóa từng thành phố. Xóa xong hắn đứng đó, như đợi ai khen."
  - Boss -203 Black Goku (textS sẵn có): "Ta là Sôn Gô Ku. Cơ thể này, sức mạnh này."
  - Boss -203 Black Goku (textE form 1 sẵn có): "Biến hình! Super Saiyan Rose."
  - NPC 37 Bunma (tương lai): "Hắn không phải bản sao. Hắn là người bị moi rỗng rồi nhét ký ức của người khác vào."
  - NPC 37 Bunma (tương lai): "Nghĩa là Heart đã thử làm điều đó với một người — trước khi định làm với cả vũ trụ."
- **Mô tả nhiệm vụ (cột `detail` DB)**
  > Một kẻ mang khuôn mặt quen thuộc đang xóa sạch Tương lai. Hãy hạ Black Goku cả hai dạng và mang bằng chứng về cho Bunma.
  > Thưởng 2.600.000.000 sức mạnh — Thưởng 2.600.000.000 tiềm năng
  > Thưởng 1 Sao pha lê đen cấp 2
- **Thưởng khi hoàn thành**: +2.600.000.000 SM, +2.600.000.000 TN, **1× Sao pha lê đen cấp 2 (964)**.
- **Mở khóa**: **boss Black Goku** vào danh sách săn (không còn khóa theo nhiệm vụ), bảng thành tích "Hạ Gục Cumber, Black Goku, Cooler, Xên" bắt đầu đếm.
- **Ghi chú triển khai**
  - Black Goku **đã gọi nhiệm vụ** trong `reward()` (Mẫu B có dòng "nhiệm vụ") → chỉ cần thêm case `-203` theo `currentLevel` vào `checkDoneTaskKillBoss`.
  - **Bẫy cần biết** (`11-boss.md` mục 9.8): khi không có người trong khu, `autoLeaveMap` của Black Goku có **50% biến thẳng thành Super Black Goku tại chỗ** thay vì rời map. Nghĩa là người chơi có thể gặp form 2 mà chưa từng giết form 1 → **bước 2 và 3 phải độc lập**, không ép thứ tự bằng cách "form 2 chỉ tính nếu đã xong form 1". Bảng trên đã tách 2 bước riêng, đúng ý này.
  - `loadBoss` tạo **2 con** Black Goku, nghỉ 5 phút → bước 2/3 không nghẽn như Cooler.
  - Item 992 nằm trong khối **10%** rơi chung với Ngọc Rồng 2–7 sao. Để tránh người chơi cày 20 lần không ra, thêm luật: khi người kết liễu đang ở `TASK_38_4`, **ép rơi 100%** item 992 (giống cách `Mob.dropItemTask` ép rơi Đùi gà ở tuyến cũ).
  - Nhắc trong `detail`: chết ở map Tương lai bị **trừ 0,1% sức mạnh** mỗi lần (`Player` ~dòng 1260).

---

### NV 39 — Cái giá của ký ức

**Chương** 5 — Vương triều bóng tối *(nhiệm vụ cuối chương — 7 bước, kết bằng boss)* | **Mốc SM khi xong** 8.000.000.000 | **Nhiệm vụ kế** 40

*Bardock đã thấy trước cảnh này: Baby tìm được người chơi, ký sinh, và moi Mảnh Ký Ức ra khỏi lồng ngực.
Nên ông làm thứ duy nhất Baby không lường được — ông đứng ra trước, tự xóa sạch ký ức của chính mình
và để Baby ký sinh vào một cái đầu trống rỗng. Ông không còn nhớ tên người chơi khi nói câu cuối.*

| # | Tên bước (hiển thị cho người chơi) | Kiểu | Điều kiện server kiểm tra | Số lượng | Map (id + tên) | NPC (id + tên) | Hằng số | Thưởng bước |
|---|---|---|---|---|---|---|---|---|
| 0 | Nói chuyện với Bardock | A3 | `checkDoneTaskTalkNpc` — npc **70 Bardock**, map 160 | 1 | 160 Khu hang động | 70 Bardock | `TASK_39_0` | +300M SM & TN |
| 1 | Gom đủ 7 viên Ngọc Rồng | A4 | `checkDoneTaskPickItem` — item **14 Ngọc Rồng 1 sao … 20 Ngọc Rồng 7 sao** (mỗi loại 1 viên trong hành trang) | 7 | mọi map | — | `TASK_39_1` | +300M SM & TN |
| 2 | Gọi Rồng Thần và ước | **B6** | `services/shenron/SummonDragon.confirmWish` — ước thành công với **Rồng Thần 1 Sao** (dùng item 14) tại map **0 Làng Aru / 7 Làng Mori / 14 Làng Kakarot** | 1 | 0 / 7 / 14 | 5 Con mèo (menu `SUMMON_SHENRON`), 24 Rồng Thiêng | `TASK_39_2` | +300M SM & TN |
| 3 | Hỏi Rồng Omega về sao đen | A3 | `checkDoneTaskTalkNpc` — npc **29 Rồng Omega**, map **24 / 25 / 26 Trạm tàu vũ trụ** → nhận **ITEM MỚI 2014 "Mảnh Ký Ức #5"** | 1 | 24 / 25 / 26 Trạm tàu vũ trụ | 29 Rồng Omega | `TASK_39_3` | +300M SM & TN |
| 4 | Diệt bầy khỉ ở Núi khỉ vàng | A1 | `checkDoneTaskKillMob` — mob tempId **57 Khỉ lông vàng** | 40 | 80 Núi khỉ vàng | — | `TASK_39_4` | +300M SM & TN |
| 5 | Gặp Bardock ở Làng Kakarot | A3 | `checkDoneTaskTalkNpc` — npc **70 Bardock** tại map **14 Làng Kakarot** (xem ghi chú: phải thêm Bardock vào map 14) → nhận **ITEM MỚI 2015 "Mảnh Ký Ức #6"** | 1 | 14 Làng Kakarot | 70 Bardock | `TASK_39_5` | +300M SM & TN |
| 6 | Hạ Baby | A2 | `checkDoneTaskKillBoss` — boss **-925 Baby**, đủ cả 3 form: `currentLevel = 0` (dame 200.000), `= 1` (dame 250.000), `= 2` (dame 30.000); HP mỗi form 2 tỷ | 3 | 14 Làng Kakarot | — | `TASK_39_6` | +300M SM & TN |

- **Lời thoại**
  - NPC 70 Bardock: "Ta thấy rồi. Baby tìm ra ngươi ở Làng Kakarot, và nó moi mảnh trong ngực ngươi ra."
  - NPC 70 Bardock: "Ta thấy cảnh đó một trăm lần. Lần nào ngươi cũng chết. Nên ta đổi một chi tiết."
  - NPC 70 Bardock: "Nó ký sinh vào cái đầu nào nhớ nhiều nhất. Ta sẽ làm cái đầu trống rỗng nhất ở đây."
  - NPC 70 Bardock: "Đừng gọi tên ta nữa. Ta không còn nhận ra nó đâu. Cầm hai mảnh này. Đi đi."
  - Boss -925 Baby (textS sẵn có): "Ta sẽ kí sinh vào người của vegeta để đạt được dạng hoàn hảo!"
  - Boss -925 Baby (textE form 3 sẵn có): "Cơ thể hoàn hảo của ta!!"
- **Mô tả nhiệm vụ (cột `detail` DB)**
  > Bardock nhìn thấy trước cái chết của bạn dưới tay Baby, và ông chọn cách đổi chỗ cho bạn.
  > Gom 7 viên ngọc, ước một điều, rồi tới Làng Kakarot lần cuối.
  > Thưởng 3.000.000.000 sức mạnh — Thưởng 3.000.000.000 tiềm năng
  > Thưởng 1 Bông tai Porata — Thưởng 50 Đậu thần cấp 8
- **Thưởng khi hoàn thành**: +3.000.000.000 SM, +3.000.000.000 TN, **1× Bông tai Porata (1819)**, **50× Đậu thần cấp 8 (352)**. Nhận **Mảnh Ký Ức #5 (2014)** và **#6 (2015)**.
- **Mở khóa**: **Mảnh Ký Ức #5 và #6**; tính năng **Ngọc Rồng Sao Đen** (menu "Tham gia" của NPC 29 Rồng Omega ở map 24/25/26, khung 20:00–20:59, map 85–91).
- **Ghi chú triển khai**
  - **B6** (trigger mới): móc vào `services/shenron/SummonDragon.confirmWish` — ngay sau khi điều ước được thực hiện thành công (trước dòng `lastTimeShenronAppeared = now`) gọi `TaskService.gI().checkDoneTaskWishDragon(player, starType)`. Chỉ tính **Rồng Thần 1 Sao** để bước này thật sự dạy người chơi gom đủ 7 viên.
  - **Baby không gọi nhiệm vụ** (`11-boss.md` mục 9.17): `boss/Baby/Baby.java` override `reward()` mà không gọi `checkDoneTaskKillBoss` (và cũng không cộng Point). **Bắt buộc thêm dòng đó**, nếu không bước 6 — bước cuối chương 5 — không bao giờ xong.
  - Baby **không có `autoLeaveMap`**: nó ở lại map 14 tới khi chết, `loadBoss` tạo **2 con**, nghỉ 15 phút. Đây là boss dễ tiếp cận nhất chương 5 → hợp làm cao trào.
  - NPC **70 Bardock chỉ có ở map 160** trong `map_template`. Bước 5 cần ông ở map **14 Làng Kakarot**. Hai cách: (a) thêm dòng `[70, x, y]` vào `map_template.npcs` của map 14 và ẩn ông đi khi `getIdTask < TASK_39_5` bằng `NpcManager.getNpcsByMapPlayer` (mẫu đã có với NPC Ca Lích); (b) dựng NPC tạm bằng `NpcFactory` chỉ cho người chơi đang ở bước này. **Khuyến nghị (a)**.
  - Sau bước 5, Bardock ở map 160 phải đổi toàn bộ thoại sang dạng "không nhận ra người chơi" (kiểm tra `getIdTask >= TASK_39_5`). Đây là cú đấm cảm xúc của chương — nếu ông vẫn chào như cũ thì hỏng hết.
  - Mốc mở tính năng Ngọc Rồng Sao Đen: đổi điều kiện trong `npc_list/RongOmega.java` thành `getIdTask(player) >= ConstTask.TASK_39_3`.

---

## 4. Chương 6 — Lõi Hư Không (NV 40–47)

> Chương cuối. Người chơi đã có 6 mảnh và mất một đồng minh. Tổ Sư Kaio là người duy nhất còn biết
> Lõi Hư Không thật sự là gì. Chương đi qua Broly (kẻ phát điên vì bị xóa), Cumber (vũ khí Heart nuôi),
> Dr Lychee (kẻ được Heart đánh thức), Bill (thử thách chứ không cứu), rồi tới Heart và lựa chọn cuối.

### NV 40 — Tổ Sư Kaio

**Chương** 6 — Lõi Hư Không | **Mốc SM khi xong** 10.000.000.000 | **Nhiệm vụ kế** 41

*Kibit dẫn người chơi lên Thánh địa. Tổ Sư Kaio nhìn Mảnh Ký Ức đầu tiên rồi nói một câu ngắn:
Lõi Hư Không không phải vũ khí, nó là nơi chứa. Ai cầm nó thì giữ toàn bộ quá khứ của vũ trụ trong ngực mình.
Heart không muốn hủy diệt — hắn muốn làm thủ thư.*

| # | Tên bước (hiển thị cho người chơi) | Kiểu | Điều kiện server kiểm tra | Số lượng | Map (id + tên) | NPC (id + tên) | Hằng số | Thưởng bước |
|---|---|---|---|---|---|---|---|---|
| 0 | Nói chuyện với Thần Vũ Trụ | A3 | `checkDoneTaskTalkNpc` — npc **20 Thần Vũ Trụ**, map 48 | 1 | 48 Hành tinh Kaio | 20 Thần Vũ Trụ | `TASK_40_0` | +400M SM & TN |
| 1 | Lên Thánh địa Kaio | A6 | `checkDoneTaskGoToMap` — map **50 Thánh địa Kaio** (vào từ map 48 qua menu "Di chuyển → Thánh địa Kaio" của npc 20) | 1 | 50 Thánh địa Kaio | — | `TASK_40_1` | +400M SM & TN |
| 2 | Nói chuyện với Tổ Sư Kaio | A3 | `checkDoneTaskTalkNpc` — npc **43 Tổ Sư Kaio**, map 50 | 1 | 50 Thánh địa Kaio | 43 Tổ Sư Kaio | `TASK_40_2` | +400M SM & TN |
| 3 | Đưa Mảnh Ký Ức cho Tổ Sư Kaio | A12 | `checkDoneTaskUseItem` — dùng item **ITEM MỚI 2010 "Mảnh Ký Ức #1"** khi đang đứng ở map 50 | 1 | 50 Thánh địa Kaio | 43 Tổ Sư Kaio | `TASK_40_3` | +400M SM & TN |
| 4 | Nghe Kibit kể phần còn lại | A3 | `checkDoneTaskTalkNpc` — npc **45 Kibit**, map 50 | 1 | 50 Thánh địa Kaio | 45 Kibit | `TASK_40_4` | +400M SM & TN |

- **Lời thoại**
  - NPC 20 Thần Vũ Trụ: "Ngài đã chịu gặp ngươi. Đừng hỏi nhiều. Đưa mảnh ra rồi im lặng mà nghe."
  - NPC 43 Tổ Sư Kaio: "Lõi Hư Không không giết ai cả. Nó chỉ là cái hũ. Toàn bộ quá khứ vũ trụ nằm trong đó."
  - NPC 43 Tổ Sư Kaio: "Heart không muốn hủy diệt. Hắn muốn làm người duy nhất giữ chìa khóa cái hũ ấy."
  - NPC 43 Tổ Sư Kaio: "Bảy mảnh là bảy cái khóa. Ngươi đang cầm sáu. Mảnh cuối nằm trong chính hắn."
  - NPC 45 Kibit: "Còn một điều ngài không nói: kẻ ghép đủ bảy mảnh cũng phải chọn — trả lại, hay giữ."
- **Mô tả nhiệm vụ (cột `detail` DB)**
  > Lên Thánh địa Kaio, đưa Mảnh Ký Ức đầu tiên cho Tổ Sư Kaio và nghe sự thật về Lõi Hư Không.
  > Thưởng 4.000.000.000 sức mạnh — Thưởng 4.000.000.000 tiềm năng
  > Thưởng 1 Áo Thần Linh
- **Thưởng khi hoàn thành**: +4.000.000.000 SM, +4.000.000.000 TN, **1× Áo Thần Linh theo hành tinh — 555 Áo Thần Linh (Trái Đất) / 557 Áo Thần Namếc / 559 Áo Thần Xayda**.
- **Mở khóa**: map **50 Thánh địa Kaio** và **116 Thánh địa Kaio** (mốc mới `TASK_40_1`); NPC **43 Tổ Sư Kaio** mở nhánh "Nâng Giới hạn Sức mạnh" cho `limitPower ∈ [5, 9)`.
- **Ghi chú triển khai**
  - Map 50 và 116 đều là **type 1 `MAP_OFFLINE` → chỉ 1 khu, 15 người**. Nếu đông người cùng ở NV 40 sẽ chen chúc; cân nhắc đổi map 50 sang type 0 (10 khu) trước khi mở tuyến, hoặc chấp nhận hàng đợi.
  - `A12` bước 3: thêm `case 2010` vào `checkDoneTaskUseItem` kèm kiểm tra `player.zone.map.mapId == 50`. Không trừ item (Mảnh Ký Ức phải giữ tới NV 45).
  - Nếu 20a đặt id khác cho Mảnh Ký Ức #1, sửa lại cho khớp — **bộ 7 mảnh phải liên tiếp 2010–2016** để `InventoryService` đếm dễ ở NV 45.
  - `npc_list/ToSuKaio.java` hiện chỉ có nhánh luyện tập + nâng giới hạn. Thêm `checkDoneTaskTalkNpc` ở đầu `openBaseMenu` như các NPC khác.

---

### NV 41 — Cơn thịnh nộ Broly

**Chương** 6 — Lõi Hư Không | **Mốc SM khi xong** 11.500.000.000 | **Nhiệm vụ kế** 42

*Broly không bị Heart mua chuộc — hắn bị Heart xóa. Và một Xayda bị lấy mất ký ức thì không hóa hiền, hắn hóa điên.
Hắn đi khắp vành đai rừng phía nam, đánh mọi thứ động đậy, gào tên một người mà chính hắn không nhớ là ai.
Hạ hắn xuống rồi hắn vẫn đứng dậy, to hơn, giận hơn — vì cái trống rỗng trong đầu không bao giờ đầy.*

| # | Tên bước (hiển thị cho người chơi) | Kiểu | Điều kiện server kiểm tra | Số lượng | Map (id + tên) | NPC (id + tên) | Hằng số | Thưởng bước |
|---|---|---|---|---|---|---|---|---|
| 0 | Nhận lời dặn của Tổ Sư Kaio | A3 | `checkDoneTaskTalkNpc` — npc **43 Tổ Sư Kaio**, map 50 | 1 | 50 Thánh địa Kaio | 43 Tổ Sư Kaio | `TASK_41_0` | +450M SM & TN |
| 1 | Dọn dẹp vành đai rừng | A1 | `checkDoneTaskKillMob` — mob tempId **16 Heo rừng**, **22 Bulon**, **32 Quỷ đầu to**, **33 Quỷ địa ngục**, **24 Quỷ mập** | 60 | 27 Rừng Bamboo, 28 Rừng dương xỉ, 29 Nam Kamê, 30 Đảo Bulông, 31–38 | — | `TASK_41_1` | +450M SM & TN |
| 2 | Hạ Broly | A2 | `checkDoneTaskKillBoss` — boss **-1822 Broly** (HP thực khi vào map: random 500–100.000; dame = HP/100; nhận tối đa `hpMax/100` sát thương mỗi đòn) | 1 | 5 Đảo Kamê, 13 Đảo Guru, 20 Vách núi đen, 27–38 | — | `TASK_41_2` | +450M SM & TN |
| 3 | Hạ Super Broly | A2 | `checkDoneTaskKillBoss` — boss **-82282 Super Broly** (sinh ra tại đúng vị trí Broly khi Broly rời map; HP random 1.500.000–16.070.777) | 1 | tại chỗ Broly (5, 13, 20, 27–38) | — | `TASK_41_3` | +450M SM & TN |
| 4 | Báo lại với Tổ Sư Kaio | A3 | `checkDoneTaskTalkNpc` — npc **43 Tổ Sư Kaio**, map 50 | 1 | 50 Thánh địa Kaio | 43 Tổ Sư Kaio | `TASK_41_4` | +450M SM & TN |

- **Lời thoại**
  - NPC 43 Tổ Sư Kaio: "Có kẻ đang phá nát vành đai rừng phía nam. Hắn không theo Heart. Hắn bị Heart xóa."
  - Boss -1822 Broly (textM sẵn có): "Haha! ta sẽ giết hết các ngươi. Sức mạnh của ta là tuyệt đối."
  - Boss -1822 Broly (textE sẵn có): "Các ngươi giỏi lắm. Ta sẽ quay lại."
  - NPC 43 Tổ Sư Kaio: "Đánh gục hắn thì hắn to hơn. Cái trống rỗng trong đầu hắn không bao giờ đầy được."
  - NPC 43 Tổ Sư Kaio: "Đó là thứ Heart hứa cho cả vũ trụ. Bình yên, theo kiểu của Broly."
- **Mô tả nhiệm vụ (cột `detail` DB)**
  > Broly phát điên vì bị xóa ký ức và đang tàn phá vành đai rừng phía nam.
  > Hạ Broly, rồi hạ tiếp Super Broly xuất hiện ngay tại chỗ, sau đó báo lại Tổ Sư Kaio.
  > Thưởng 4.500.000.000 sức mạnh — Thưởng 4.500.000.000 tiềm năng
  > Thưởng 1 Quần Thần Linh
- **Thưởng khi hoàn thành**: +4.500.000.000 SM, +4.500.000.000 TN, **1× Quần Thần theo hành tinh — 556 Quần Thần Linh / 558 Quần Thần namếc / 560 Quần Thần Xayda**.
- **Mở khóa**: **boss Broly / Super Broly** (`BrolyManager`) được tính vào nhiệm vụ và bảng thành tích.
- **Ghi chú triển khai** — **NV 41 phụ thuộc luồng boss đang lỗi, phải sửa trước (§10 mục 5 file 20):**
  1. **Broly không trao thưởng và không gọi nhiệm vụ.** Theo `11-boss.md` mục 5.17: `Broly.die()` chỉ đặt trạng thái `DIE`, không `reward()`, không thông báo → `checkDoneTaskKillBoss` **không bao giờ chạy**. Phải thêm `reward(plKill)` (hoặc ít nhất `TaskService.gI().checkDoneTaskKillBoss(plKill, this)`) vào `boss/Broly/Broly.java`.
  2. **Super Broly cũng không gọi nhiệm vụ**: `SuperBroly.reward()` chỉ tạo đệ tử thường cho người chưa có đệ. Thêm `checkDoneTaskKillBoss` vào đó.
  3. **Broly chết vẫn sinh Super Broly** (mục 9.7): `die → DIE → CHAT_E → LEAVE_MAP → leaveMap()` và `leaveMap()` **luôn** `new SuperBroly(...)`. Với NV 41 điều này lại **có lợi** (giết Broly xong Super Broly hiện ngay tại chỗ, đúng nhịp 2 bước liên tiếp) — nhưng phải ghi rõ trong tài liệu để người sửa bug sau này không "sửa" mất nhịp nhiệm vụ. Nếu vẫn muốn sửa, giữ nhánh sinh Super Broly khi `plKill != null`.
  4. **Chỉ số Broly quá thấp so với chương 6**: HP random **500–100.000**, dame HP/100. Người chơi 11 tỷ SM one-shot ngay. Hai lựa chọn: (a) chấp nhận — coi đây là bước "thở" giữa hai boss nặng; (b) thêm `BossID.BROLY_HU_KHONG` riêng cho tuyến nhiệm vụ với HP 2.000.000.000 / dame 300.000 (đụng trần `int` của `hp[]`, xem mục 7). **Khuyến nghị (a)** để không phải dựng thêm boss.
  5. Broly **không gửi thông báo xuất hiện** và chọn khu random từ 2 trở đi; `Default` event tạo **30 con**, `BrolyManager` **có** thread (khác các manager sự kiện mùa vụ đang chết) → bước 2 không nghẽn.
  6. Điều kiện `hpMax == 1_500_000` trong `Broly.active()` gần như không bao giờ đúng (so sánh bằng tuyệt đối) — không ảnh hưởng nhiệm vụ, nhưng nên ghi vào phiếu sửa lỗi chung.

---

### NV 42 — Hành tinh ngục tù

**Chương** 6 — Lõi Hư Không | **Mốc SM khi xong** 13.000.000.000 | **Nhiệm vụ kế** 43

*Ôsin dẫn người chơi tới một hành tinh không có tên trên bản đồ Kaio: nơi Heart nhốt thứ hắn nuôi từ nhỏ.
Cumber được cho ăn ký ức của tù nhân suốt nhiều năm, và cái lồng đang rão dần.
Cửa lồng chỉ giữ được mười phút sau khi người lạ bước vào.*

| # | Tên bước (hiển thị cho người chơi) | Kiểu | Điều kiện server kiểm tra | Số lượng | Map (id + tên) | NPC (id + tên) | Hằng số | Thưởng bước |
|---|---|---|---|---|---|---|---|---|
| 0 | Hỏi Ôsin đường tới ngục tù | A3 | `checkDoneTaskTalkNpc` — npc **44 Ôsin**, map **50 Thánh địa Kaio** (thêm nút "Đến hành tinh ngục tù", xem ghi chú) | 1 | 50 Thánh địa Kaio | 44 Ôsin | `TASK_42_0` | +500M SM & TN |
| 1 | Tới Hành tinh ngục tù | A6 | `checkDoneTaskGoToMap` — map **155 Hành tinh ngục tù** | 1 | 155 Hành tinh ngục tù | — | `TASK_42_1` | +500M SM & TN |
| 2 | Phá 60 lồng giam trong 10 phút | **B12** | `TaskMain.lastTime`: đếm ngược **600.000 ms** từ lúc vào bước; hạ **60** mob tempId **78 Khỉ lông xanh** (HP 2.000.000) hoặc **79 Taburine Đỏ** (HP 3.000.000) trong map 155; quá giờ → `count = 0`, đặt lại `lastTime` | 60 | 155 Hành tinh ngục tù | — | `TASK_42_2` | +500M SM & TN |
| 3 | Hạ Cumber | A2 | `checkDoneTaskKillBoss` — boss **-203999 Cumber**, `currentLevel = 0` (HP 500M, dame 50.000) | 1 | 155 Hành tinh ngục tù | — | `TASK_42_3` | +500M SM & TN |
| 4 | Hạ Super Cumber | A2 | `checkDoneTaskKillBoss` — boss **-203999 Super Cumber**, `currentLevel = 1` (HP 2 tỷ, dame 100.000) | 1 | 155 Hành tinh ngục tù | — | `TASK_42_4` | +500M SM & TN |
| 5 | Báo cáo với Ôsin | A3 | `checkDoneTaskTalkNpc` — npc **44 Ôsin**, map 155 | 1 | 155 Hành tinh ngục tù | 44 Ôsin | `TASK_42_5` | +500M SM & TN |

- **Lời thoại**
  - NPC 44 Ôsin: "Chỗ đó không có trên bản đồ của ngài Kaio. Heart tự vẽ nó ra và tự quên đi."
  - NPC 44 Ôsin: "Trong đó nuôi một đứa. Nó ăn ký ức tù nhân từ bé. Giờ chả còn tù nhân nào nhớ nổi tên mình."
  - Boss -203999 Cumber (textE form 1 sẵn có): "Biến hình! Super Saiyan SSJ."
  - NPC 44 Ôsin: "Cậu vừa phá cái chuồng của Heart. Hắn sẽ thấy phiền — mà hắn phiền thì hắn mới ló mặt."
- **Mô tả nhiệm vụ (cột `detail` DB)**
  > Heart nuôi Cumber trong Hành tinh ngục tù bằng ký ức của tù nhân.
  > Phá 60 lồng giam trong 10 phút, rồi hạ Cumber cả hai dạng.
  > Thưởng 5.000.000.000 sức mạnh — Thưởng 5.000.000.000 tiềm năng
  > Thưởng 1 Găng Thần Linh
- **Thưởng khi hoàn thành**: +5.000.000.000 SM, +5.000.000.000 TN, **1× Găng Thần theo hành tinh — 562 Găng Thần Linh / 564 Găng Thần Namếc / 566 Găng Thần Xayda**.
- **Mở khóa**: map **155 Hành tinh ngục tù** (mốc mới `TASK_42_0`).
- **Ghi chú triển khai**
  - **Mâu thuẫn khóa map phải xử lý ngay** (§8 file 20): map **155 mở ở `TASK_42_0`** nhưng đường vào 155 hiện tại **duy nhất** là NPC 44 Ôsin đứng ở map **154 Hành tinh Bill**, mà map 154 lại mở ở `TASK_44_0`. Nếu giữ nguyên, NV 42 **không thể bắt đầu**. Cách sửa khuyến nghị: thêm nhánh **"Đến hành tinh ngục tù"** vào menu Ôsin ở **map 50 Thánh địa Kaio** (`npc_list/Osin.java`, `case 50`), chỉ hiện khi `getIdTask(player) >= ConstTask.TASK_42_0` → `changeMap(155, 111, 792)`. Giữ nguyên mốc `TASK_44_0` cho map 154. (Cách thay thế: hạ mốc map 154 xuống `TASK_42_0` — đơn giản hơn nhưng làm lộ nội dung NV 44 sớm.)
  - **B12** lần thứ hai của tuyến: dùng lại đúng cơ chế đã viết cho NV 34, chỉ đổi hằng số thời gian. Tránh đặt thêm B12 nào khác ở chương 6 (§4.3).
  - Cumber **đã gọi nhiệm vụ** trong `reward()` (code giống hệt Black Goku) → chỉ thêm case `-203999` theo `currentLevel`.
  - Cảnh báo giống Black Goku (`11-boss.md` mục 9.8): `autoLeaveMap` của Cumber có **50% biến thành Super Cumber tại chỗ** khi khu vắng người → bước 3 và 4 phải độc lập, không ép thứ tự.
  - `loadBoss` chỉ tạo **1 Cumber** toàn server, nghỉ 5 phút, khu ≥ 2 → điểm nghẽn. Cân nhắc nâng lên 2 con cùng lúc mở tuyến.
  - Map 155 có `MapService.isMapNgucTu` → kiểm tra xem có luật riêng nào (rơi đồ/giảm SM khi chết) cần nhắc trong `detail` không.

---

### NV 43 — Khí gas hủy diệt

**Chương** 6 — Lõi Hư Không | **Mốc SM khi xong** 14.000.000.000 | **Nhiệm vụ kế** 44

*Heart không tạo ra kẻ thù mới — hắn đánh thức những kẻ cũ đã bị lãng quên, vì kẻ bị quên thì dễ sai khiến.
Dr Lychee tỉnh dậy sau nhiều năm trong lâu đài của mình, thả lại thứ khí gas từng suýt giết cả một hành tinh.
Và Hatchiyack, cỗ máy chạy bằng lòng thù hận, vẫn còn nguyên thù hận để chạy.*

| # | Tên bước (hiển thị cho người chơi) | Kiểu | Điều kiện server kiểm tra | Số lượng | Map (id + tên) | NPC (id + tên) | Hằng số | Thưởng bước |
|---|---|---|---|---|---|---|---|---|
| 0 | Gặp Mr Popo ở Làng Aru | A3 | `checkDoneTaskTalkNpc` — npc **67 Mr Popo**, map **0 Làng Aru** | 1 | 0 Làng Aru | 67 Mr Popo | `TASK_43_0` | +550M SM & TN |
| 1 | Dọn sạch khí gas | A1 | `checkDoneTaskKillMob` — mob tempId **73 Kawazu**, **74 Kinkarn**, **75 Arbee**, **76 Cỗ máy hủy diệt** trong phó bản | 80 | 147 Sa mạc, 149 Thành phố Santa, 151 Hành tinh bóng tối, 152 Vùng đất băng giá | — | `TASK_43_1` | +550M SM & TN |
| 2 | Hạ Dr Lychee | A2 | `checkDoneTaskKillBoss` — boss **-208 Dr Lychee** (chỉ xuất hiện khi **toàn bộ quái** của instance đã chết; HP `1.000.000 + min(15.000.000 × level, 2 tỷ)`) | 1 | 148 Lâu đài Lychee | — | `TASK_43_2` | +550M SM & TN |
| 3 | Hạ Hatchiyack | A2 | `checkDoneTaskKillBoss` — boss **-207 Hatchiyack** (tên hiển thị là khoảng trắng `" "`; sinh ra khi Dr Lychee rời map; HP/dame = ×1,5 Dr Lychee, trần 2 tỷ / 200 triệu) | 1 | 148 Lâu đài Lychee | — | `TASK_43_3` | +550M SM & TN |
| 4 | Hoàn thành Khí gas hủy diệt | **B1** | `DestronGas.finish()` / cờ `clan.KhiGasHuyDiet.hatchiyatchDead == true` | 1 | 147–152 | — | `TASK_43_4` | +550M SM & TN |
| 5 | Báo cáo với Thượng Đế | A3 | `checkDoneTaskTalkNpc` — npc **19 Thượng Đế**, map **45 Thần điện** | 1 | 45 Thần điện | 19 Thượng Đế | `TASK_43_5` | +550M SM & TN |

- **Lời thoại**
  - NPC 67 Mr Popo: "Thượng Đế phát hiện lại thứ khí gas cũ. Cũ tới mức chẳng ai còn nhớ nó từng giết bao nhiêu người."
  - Boss -208 Dr Lychee (textS sẵn có): "Ta đợi các ngươi mãi. Bọn xayda các ngươi mau đền tội đi."
  - Boss -208 Dr Lychee (textE sẵn có): "Các ngươi khá lắm. Hatchiyack sẽ báo thù cho ta."
  - Boss -207 Hatchiyack (textS sẵn có): "Các ngươi dám hạ sư phụ ta."
  - NPC 19 Thượng Đế: "Heart không tạo kẻ thù mới. Hắn đánh thức kẻ bị quên — vì kẻ bị quên dễ sai khiến nhất."
- **Mô tả nhiệm vụ (cột `detail` DB)**
  > Heart đánh thức Dr Lychee và thả lại khí gas hủy diệt.
  > Cùng bang hội dọn sạch phó bản, hạ Dr Lychee và Hatchiyack rồi báo lại Thượng Đế.
  > Thưởng 5.500.000.000 sức mạnh — Thưởng 5.500.000.000 tiềm năng
  > Thưởng 1 Giầy Thần Linh
- **Thưởng khi hoàn thành**: +5.500.000.000 SM, +5.500.000.000 TN, **1× Giầy Thần theo hành tinh — 563 Giầy Thần Linh / 565 Giầy Thần Namếc / 567 Giầy Thần Xayda**.
- **Mở khóa**: **phó bản Khí gas hủy diệt** (NPC 67 Mr Popo, map 0 Làng Aru).
- **Ghi chú triển khai** — **NV 43 phụ thuộc luồng phó bản đang lỗi, phải sửa trước:**
  1. **Dr Lychee và Hatchiyack không gọi nhiệm vụ**: theo `11b-boss-su-kien-pho-ban.md`, trong toàn bộ file chỉ có boss doanh trại có dòng "Kiểm tra nhiệm vụ giết boss". `boss/khi_gas/DrLychee.java` và `Hatchiyack.java` override `reward()` chỉ để thả cải trang. **Phải thêm `TaskService.gI().checkDoneTaskKillBoss(plKill, this)`** vào cả hai, nếu không bước 2 và 3 kẹt vĩnh viễn.
  2. **Thưởng rơi tự do** (chủ `-1`, `1 + 2 × số người trong khu`) — người khác nhặt được, nhưng **việc hoàn thành nhiệm vụ vẫn theo `plKill`**, nên chỉ **người kết liễu** xong bước. Phải ghi rõ trong `detail`, hoặc mở rộng thành "mọi người trong khu đều xong bước" cho công bằng — **khuyến nghị mở rộng**, vì phó bản là hoạt động bang.
  3. **Điều kiện vào rất chặt**: có bang, **vào bang ≥ 2 ngày**, **chỉ bang chủ** mới mở được, **3 lần/ngày/bang**. Người chơi lẻ sẽ kẹt. Xử lý như NV 35: bỏ ràng buộc "≥ 2 ngày" khi `getIdTask` nằm trong `TASK_43_1..TASK_43_4`, và cho bước 4 (B1) cũng hoàn thành bằng Doanh Trại Độc Nhãn / BĐKB.
  4. **Bước 1 là chỗ tốn thời gian nhất tuyến**: Dr Lychee chỉ xuất hiện khi **toàn bộ quái của cả instance** chết (147 có 17 con, 148 có 11, 149 có 14, 151 có 9, 152 có **30** — tổng ~81 con). Đó là lý do `max_count` bước 1 đặt đúng **80**: người chơi làm bước 1 thì gần như đồng thời mở luôn boss.
  5. Khi còn **Cỗ máy hủy diệt (mob 76)** sống trong khu, các chiêu **Liên hoàn / Antomic / Masenko / Kamejoko chỉ gây 1 sát thương** lên quái (`Mob.injured`) → nhắc trong `detail` để người chơi không tưởng là lỗi.
  6. Map **152 bị tính là map lạnh** trong `isMapCold(Map)` → giảm 50% HP. Map **150 không tồn tại** trong `map_template` — đừng đưa vào danh sách bước 1.
  7. `GasDestroyManager` **có** thread (nằm trong 10 manager được start) → luồng boss chạy được, chỉ thiếu móc nhiệm vụ.
  8. Tên hiển thị của Hatchiyack là **khoảng trắng** `" "` → thông báo "Đã tiêu diệt được  " trông như lỗi. Nên đổi `name` thành "Hatchiyack" trong `BossesData` khi làm tuyến này.

---

### NV 44 — Thử thách của Thần Hủy Diệt

**Chương** 6 — Lõi Hư Không | **Mốc SM khi xong** 16.000.000.000 | **Nhiệm vụ kế** 45

*Ôsin đưa người chơi tới hành tinh của Bill. Bill nghe hết chuyện, ngáp, rồi nói: ta không cứu ai cả.
Thần Hủy Diệt chỉ hủy diệt — nhưng ngài chịu thử xem cái ngực đang giữ sáu mảnh ký ức kia có chịu nổi mảnh thứ bảy không.
Whis là bài kiểm tra, còn cái trần sức mạnh của người chơi là câu trả lời.*

| # | Tên bước (hiển thị cho người chơi) | Kiểu | Điều kiện server kiểm tra | Số lượng | Map (id + tên) | NPC (id + tên) | Hằng số | Thưởng bước |
|---|---|---|---|---|---|---|---|---|
| 0 | Nhờ Ôsin đưa tới hành tinh Bill | A3 | `checkDoneTaskTalkNpc` — npc **44 Ôsin**, map **50 Thánh địa Kaio** (nút "Đến hành tinh Bill" → map 154, 200, 312) | 1 | 50 Thánh địa Kaio | 44 Ôsin | `TASK_44_0` | +600M SM & TN |
| 1 | Nói chuyện với Bill | A3 | `checkDoneTaskTalkNpc` — npc **55 Bill**, map **154 Hành tinh Bill** | 1 | 154 Hành tinh Bill | 55 Bill | `TASK_44_1` | +600M SM & TN |
| 2 | Thắng một trận đấu | **B5** | `matches/` — thắng 1 trận thuộc **một trong ba loại**: PVP Thách đấu (`ThachDau.lose()` bên đối thủ), Võ đài Hạt Mít (`DeathOrAliveArena` vô địch 5 vòng, `haveRewardVDST = true`), Đại hội võ thuật (`WorldMartialArtsTournament` thắng ≥ 1 vòng, `martialArtsTournamentWins++`) | 1 | 51 Đấu trường / 112 Võ đài Hạt Mít / 52 Đại hội võ thuật | 21 Bà Hạt Mít, 23 Ghi danh | `TASK_44_2` | +600M SM & TN |
| 3 | Thách đấu Whis | A2 | `checkDoneTaskKillBoss` — boss **-364 Whis** (`TrainingService.callBoss`; HP `550.000 × level`, dame `10.000 × level`, né 40%, sát thương nhận chia `level`) | 1 | 154 Hành tinh Bill | 56 Whis | `TASK_44_3` | +600M SM & TN |
| 4 | Mở giới hạn sức mạnh lần hai | **B10** | `OpenPowerService.openPowerByTask` — `limitPower` tăng **1 → 2** (trần SM 19.999.999.999 → 24.999.999.999), **miễn phí, không tốn vàng**, qua nhánh "Phá giới hạn (nhiệm vụ)" của npc **42 Quốc Vương** (`limitPower < 5`) hoặc **43 Tổ Sư Kaio** (`limitPower ≥ 5`), chỉ mở khi `getIdTask == TASK_44_4` | 1 | 43 Vách núi Moori / 50 Thánh địa Kaio | 42 Quốc Vương / 43 Tổ Sư Kaio | `TASK_44_4` | +600M SM & TN |
| 5 | Nghe Whis dặn dò | A3 | `checkDoneTaskTalkNpc` — npc **56 Whis**, map 154 | 1 | 154 Hành tinh Bill | 56 Whis | `TASK_44_5` | +600M SM & TN |

- **Lời thoại**
  - NPC 55 Bill: "Ký ức của vũ trụ à? Ta ngủ ba mươi năm một giấc, ta quên nhiều hơn Heart xóa được."
  - NPC 55 Bill: "Ta không cứu ai. Ta hủy diệt. Nhưng ta tò mò: cái ngực giữ sáu mảnh của ngươi chịu nổi mảnh thứ bảy chứ?"
  - NPC 56 Whis: "Ngài ấy nói vậy nghĩa là đồng ý thử. Mời cậu đánh với tôi một hiệp."
  - NPC 56 Whis: "Cậu chạm trần rồi đấy. Đi phá nó thêm một lần nữa, rồi quay lại đây."
  - NPC 56 Whis: "Mảnh thứ bảy không nằm ở đâu xa. Nó nằm trong ngực Heart. Muốn lấy thì phải lấy cả hắn."
- **Mô tả nhiệm vụ (cột `detail` DB)**
  > Bill không cứu ai, ngài chỉ thử. Hãy thắng một trận đấu, hạ Whis, rồi mở giới hạn sức mạnh lần thứ hai.
  > Thưởng 6.000.000.000 sức mạnh — Thưởng 6.000.000.000 tiềm năng
  > Thưởng 1 Nhẫn Thần Linh
- **Thưởng khi hoàn thành**: +6.000.000.000 SM, +6.000.000.000 TN, **1× Nhẫn Thần Linh (561)**.
- **Mở khóa**: map **154 Hành tinh Bill** (mốc cũ `TASK_27_0` → mốc mới `TASK_44_0`); thách đấu **Whis** (`TrainingService`), bảng top Whis.
- **Ghi chú triển khai**
  - **Bước 4 là bước B10 thứ hai — bắt buộc phải có**, nếu không toàn bộ thưởng chương 6 bị `Service.addSMTN` cắt. Sau NV 33 người chơi ở `limitPower = 1` (trần **19.999.999.999**); thưởng NV 44–47 cộng dồn ~26 tỷ SM. Mở lên `limitPower = 2` nâng trần lên **24.999.999.999**, vừa đủ cho mốc "18 tỷ + mở giới hạn" của §6.1 mà không lạm phát.
  - Bước 4 dùng nhánh **miễn phí** `OpenPowerService.openPowerByTask(player)` (chỉ chạy khi `getIdTask == TASK_44_4` và `limitPower == 1`), **không tốn vàng** — vì nhiệm vụ không phát tiền tệ. Menu "Phá giới hạn (nhiệm vụ)" phải có ở **cả** `npc_list/QuocVuong.java` và `npc_list/ToSuKaio.java`.
  - Bước này **không có trong bảng tổng §5** (bảng đó chỉ ghi khung A3, B5, A3) — đây là **bước bổ sung**, không đổi kiểu bước nào đã liệt kê, và §4.3 cho phép 3–6 bước mỗi nhiệm vụ.
  - **B5** (trigger mới): ba điểm móc, tất cả đều trong `matches/`:
    - `matches/ThachDau.java` → trong `reward(winner)`;
    - `matches/dai_hoi_vo_thuat/DeathOrAliveArena.java` → chỗ đặt `haveRewardVDST = true`;
    - `matches/dai_hoi_vo_thuat/WorldMartialArtsTournament.java` → chỗ tăng `martialArtsTournamentWins`.
    Gọi chung `TaskService.gI().checkDoneTaskWinMatch(player, typePvp)`.
  - **Lỗi cũ chặn NV 44**: `npc_list/Bill.java` có `confirmMenu` dùng `switch` theo `mapId` với `case 2` (map 2) thay vì `indexMenu 2`, và nút "Nói chuyện" đặt `indexMenu` 100/101 **không ai xử lý** (`18-npc.md` mục 9.2). Phải sửa `Bill.confirmMenu` trước, nếu không bước 1 không nhận được tương tác nào.
  - `npc_list/Whis.java` xử lý map 48, 154 (và 164 thừa). Thách đấu Whis đi qua `services_dungeon/TrainingService.callBoss(BossID.WHIS, true)`. Whis có lỗi `injured` không kiểm tra `isDie()` và so `damage >= hp` **sau khi** đã trừ HP (`11b` mục 12.11) → có thể gọi `die` lặp hoặc bỏ lỡ đòn kết liễu. Sửa trước khi gắn nhiệm vụ.
  - Whis `level = top Whis của người chơi + 1`, HP/dame nhân `level`, **sát thương nhận chia `level`** → càng đánh nhiều lần càng khó. Bước 3 chỉ cần **1 lần**, nên người chơi lần đầu gặp Whis level 1 — vừa sức ở 16 tỷ SM.
  - Cân nhắc đặt `max_count` bước 2 là 1 (không phải 3) để người chơi không bị ép cày PVP.

---

### NV 45 — Bảy mảnh hợp nhất

**Chương** 6 — Lõi Hư Không | **Mốc SM khi xong** 17.000.000.000 | **Nhiệm vụ kế** 46

*Thiên Sứ Whis mở Lãnh địa Fize — mảnh đất trung lập duy nhất còn nhớ cách làm lễ.
Sáu mảnh trong ngực người chơi bắt đầu tự tìm nhau, nhưng chúng không chịu ghép khi chỉ có một người đứng đó:
ký ức cần ít nhất hai người mới thành ký ức, một người nhớ thì chỉ là giấc mơ.*

| # | Tên bước (hiển thị cho người chơi) | Kiểu | Điều kiện server kiểm tra | Số lượng | Map (id + tên) | NPC (id + tên) | Hằng số | Thưởng bước |
|---|---|---|---|---|---|---|---|---|
| 0 | Tới Lãnh địa Fize | A6 | `checkDoneTaskGoToMap` — map **78 Lãnh địa Fize** | 1 | 78 Lãnh địa Fize | — | `TASK_45_0` | +650M SM & TN |
| 1 | Nhặt Mảnh Ký Ức thứ bảy | A4 | `checkDoneTaskPickItem` — item **ITEM MỚI 2016 "Mảnh Ký Ức #7"** (icon mượn 425 của 20 Ngọc Rồng 7 sao); item chỉ hiển thị với người đang ở `TASK_45_1` (`Zone.getItemMapsForPlayer`, mẫu item 78 Đứa bé) | 1 | 78 Lãnh địa Fize | — | `TASK_45_1` | +650M SM & TN |
| 2 | Làm lễ hợp nhất cùng một người khác | **B13** | `checkDoneTaskTogether` — có **≥ 2 người chơi thật** cùng đứng trong một zone của map **78** (không bắt buộc cùng bang, khác với NV 35) | 1 | 78 Lãnh địa Fize | 64 Thiên Sứ Whis | `TASK_45_2` | +650M SM & TN |
| 3 | Hợp nhất bảy mảnh | A12 | `checkDoneTaskUseItem` — dùng item **ITEM MỚI 2100 "Lõi Ký Ức chưa hoàn chỉnh"** (icon mượn 9650 của 1015 Ngọc rồng Siêu Cấp); yêu cầu **đủ 7 item 2010–2016** trong hành trang, trừ cả 7 và trao **ITEM MỚI 2101 "Lõi Hư Không"** (icon mượn 2321 của 378 Ngọc rồng 7 sao đen) | 1 | 78 Lãnh địa Fize | 64 Thiên Sứ Whis | `TASK_45_3` | +650M SM & TN |
| 4 | Nghe Thiên Sứ Whis giải thích | A3 | `checkDoneTaskTalkNpc` — npc **64 Thiên Sứ Whis**, map 78 | 1 | 78 Lãnh địa Fize | 64 Thiên Sứ Whis | `TASK_45_4` | +650M SM & TN |

- **Lời thoại**
  - NPC 64 Thiên Sứ Whis: "Fize là chỗ duy nhất còn nhớ cách làm lễ. Mọi nơi khác quên cả nghi thức lẫn lý do."
  - NPC 64 Thiên Sứ Whis: "Sáu mảnh không chịu ghép khi chỉ có cậu. Ký ức cần hai người mới thành ký ức."
  - NPC 64 Thiên Sứ Whis: "Một người nhớ thì đó chỉ là giấc mơ. Hai người cùng nhớ, nó mới có thật."
  - NPC 64 Thiên Sứ Whis: "Lõi đã liền. Từ giờ cậu không mang bảy mảnh nữa — cậu mang cả quá khứ của vũ trụ."
  - NPC 64 Thiên Sứ Whis: "Heart sẽ tới tìm cậu. Đừng đi tìm hắn ở phòng thí nghiệm. Hắn muốn cậu tới đó."
- **Mô tả nhiệm vụ (cột `detail` DB)**
  > Tới Lãnh địa Fize, nhặt Mảnh Ký Ức thứ bảy và làm lễ hợp nhất cùng ít nhất một người chơi khác.
  > Thưởng 6.500.000.000 sức mạnh — Thưởng 6.500.000.000 tiềm năng
  > Thưởng 7 viên Ngọc Rồng
- **Thưởng khi hoàn thành**: +6.500.000.000 SM, +6.500.000.000 TN, **bộ 7 Ngọc Rồng: 14 Ngọc Rồng 1 sao, 15 (2 sao), 16 (3 sao), 17 (4 sao), 18 (5 sao), 19 (6 sao), 20 (7 sao)** mỗi loại 1 viên. Nhận **Lõi Hư Không (ITEM MỚI 2101)**.
- **Mở khóa**: **Mảnh Ký Ức #7**; item **Lõi Hư Không (2101)** — điều kiện tiên quyết của NV 47/50.
- **Ghi chú triển khai**
  - **B13** lần thứ hai (chương 6). Khác NV 35 ở chỗ **không yêu cầu cùng bang** — chỉ cần 2 người chơi thật trong zone map 78, để người chơi không có bang vẫn đi tiếp được. Cùng một hàm `checkDoneTaskTogether(player, zone, nMember, requireSameClan)`.
  - Map 78 Lãnh địa Fize là map thường (type 0, 10 khu, 15 người/khu) và có sẵn NPC 7 Bunma, 64 Thiên Sứ Whis, 19 Thượng Đế, 20 Thần Vũ Trụ, 49 Đường Tăng → đông người qua lại, thuận cho bước B13.
  - **Lỗi cũ chặn NV 45**: `npc_list/DaiThienSu.java` (NPC 64) có `openBaseMenu` và `confirmMenu` **rỗng** — bấm vào không phản hồi (`18-npc.md` mục 6.43). Phải viết menu cho NPC này trước. Đồng thời NPC 19 và 20 đặt ở map 78 nhưng code chỉ xử lý map 45/141 và 48 → cũng không có menu ở 78 (mục 9.5).
  - Item 2100 "Lõi Ký Ức chưa hoàn chỉnh" nên được trao tự động ở bước 1 (cùng lúc nhặt mảnh #7) để người chơi có thứ để "dùng" ở bước 3.
  - Việc trừ 7 item và trao item 2101 làm trong `switch(idTaskCustom)` của `doneTask` (mẫu TASK_2_1 trừ 10 Đùi gà đã có sẵn), rồi `InventoryService.sendItemBags`.
  - Nếu chủ dự án chọn phương án "**không thêm item mới**" (§10 mục 1 file 20): thay 2010–2016 bằng **Ngọc Rồng 1–7 sao (14–20)** và thay 2101 bằng **1015 Ngọc rồng Siêu Cấp**. Khi đó bước 3 phải kiểm tra người chơi có đủ 7 viên 14–20, và bộ Ngọc Rồng ở phần thưởng phải đổi sang thứ khác (đề xuất: 3× Đá ngũ sắc 674).

---

### NV 46 — Heart

**Chương** 6 — Lõi Hư Không | **Mốc SM khi xong** 17.500.000.000 | **Nhiệm vụ kế** 47

*Dr. Myuu đợi sẵn trong phòng thí nghiệm, không để chặn đường mà để nói lời cuối: hắn cũng bị xóa, chỉ là xóa từ từ.
Heart hiện ra ngay sau đó, bình thản, gần như dịu dàng — hắn thật lòng tin rằng xóa ký ức là lòng thương.
Ba dạng, ba lần hắn giải thích, và lần nào cũng thuyết phục hơn lần trước.*

| # | Tên bước (hiển thị cho người chơi) | Kiểu | Điều kiện server kiểm tra | Số lượng | Map (id + tên) | NPC (id + tên) | Hằng số | Thưởng bước |
|---|---|---|---|---|---|---|---|---|
| 0 | Nói chuyện với Dr. Myuu | A3 | `checkDoneTaskTalkNpc` — npc **83 Dr. Myuu**, map **166 Phòng thí nghiệm Myuu** | 1 | 166 Phòng thí nghiệm Myuu | 83 Dr. Myuu | `TASK_46_0` | +700M SM & TN |
| 1 | Hạ Heart | A2 | `checkDoneTaskKillBoss` — boss **ITEM/BOSS MỚI -108108 Heart**, `currentLevel = 0` (HP 1.500.000.000, dame 150.000) | 1 | 166 Phòng thí nghiệm Myuu | — | `TASK_46_1` | +700M SM & TN |
| 2 | Đuổi theo Heart tới Võ Đài Siêu Cấp | A6 | `checkDoneTaskGoToMap` — map **145 Võ Đài Siêu Cấp** | 1 | 145 Võ Đài Siêu Cấp | 64 Thiên Sứ Whis | `TASK_46_2` | +700M SM & TN |
| 3 | Hạ Heart Hư Không | A2 | `checkDoneTaskKillBoss` — boss **-108108**, `currentLevel = 1` ("Heart Hư Không", HP 2.000.000.000, dame 250.000) | 1 | 145 Võ Đài Siêu Cấp | — | `TASK_46_3` | +700M SM & TN |
| 4 | Hạ Heart Toàn Ký | A2 | `checkDoneTaskKillBoss` — boss **-108108**, `currentLevel = 2` ("Heart Toàn Ký", HP 2.000.000.000, dame 400.000) → rơi **ITEM MỚI 2107 "Ống nghiệm Myuu"** | 1 | 145 Võ Đài Siêu Cấp | — | `TASK_46_4` | +700M SM & TN |
| 5 | Nói chuyện với Thiên Sứ Whis | A3 | `checkDoneTaskTalkNpc` — npc **64 Thiên Sứ Whis**, map 145 | 1 | 145 Võ Đài Siêu Cấp | 64 Thiên Sứ Whis | `TASK_46_5` | +700M SM & TN |

- **Lời thoại**
  - NPC 83 Dr. Myuu: "Tôi chế cái máy đó. Rồi ông ta dùng nó lên tôi, mỗi ngày một ít, để tôi ngoan."
  - NPC 83 Dr. Myuu: "Hôm nay tôi còn nhớ tên mình. Ngày mai thì chưa chắc. Đi đi, đừng nghe ông ta nói."
  - Boss -108108 Heart: "Ta không ghét ai cả. Ta chỉ thấy các ngươi đau, và ta biết chỗ cơn đau nằm."
  - Boss -108108 Heart: "Ngươi nhớ ông ngươi gọi nhầm tên ngươi chứ? Ta có thể lấy luôn cả cái đó đi."
  - Boss -108108 Heart (form 3): "Sáu mảnh trong ngực ngươi vừa gọi mảnh thứ bảy trong ngực ta. Chúng nhớ nhau đấy."
  - NPC 64 Thiên Sứ Whis: "Hắn chưa chết hẳn. Hắn chỉ tan vào Lõi. Giờ cậu mới là người phải quyết."
- **Mô tả nhiệm vụ (cột `detail` DB)**
  > Đối đầu Heart tại phòng thí nghiệm Myuu, rồi đuổi theo hắn tới Võ Đài Siêu Cấp và hạ cả ba dạng.
  > Thưởng 7.000.000.000 sức mạnh — Thưởng 7.000.000.000 tiềm năng
  > Thưởng 3 Ngọc Rồng 7 sao — Thưởng 1 Đá ngũ sắc
- **Thưởng khi hoàn thành**: +7.000.000.000 SM, +7.000.000.000 TN, **3× Ngọc Rồng 7 sao (20)**, **1× Đá ngũ sắc (674)**.
- **Mở khóa**: **boss Heart** (-108108) vào `BossManager`; map **145 Võ Đài Siêu Cấp** (mốc mới `TASK_46_2` — hiện `checkMapCanJoin` chưa khóa map này).
- **Ghi chú triển khai**
  - Boss Heart là **boss mới hoàn toàn** — khai báo đầy đủ ở [mục 5](#5-boss-mới-heart). Không cần tài nguyên client mới vì mượn tạo hình NPC 108.
  - NPC **83 Dr. Myuu** có class `DrMyuu.java` nhưng **không xuất hiện trong `map_template`** → phải thêm dòng `[83, x, y]` vào `map_template.npcs` của map **166**. Map 166 hiện **không có NPC nào**.
  - NPC **64 Thiên Sứ Whis** ở map 145 có menu rỗng (xem NV 45) → sửa chung một lần.
  - Map 145 hiện không được `matches/` dùng (Giải Siêu Hạng chạy ở map **113**), nên dùng làm đấu trường cốt truyện là an toàn.
  - Bước 1 ở map 166 và bước 3–4 ở map 145: Heart phải là boss **nhiều form đổi map** — `mapJoin` của form 0 là `{166}`, của form 1–2 là `{145}`. Cơ chế `leaveMap → RESPAWN` form kế tiếp của `Boss` hỗ trợ sẵn việc này (`joinMap` đọc `mapJoin` của form hiện tại).
  - Đặt `isNotifyDisabled` cho Heart để không spam thông báo toàn server mỗi lần đổi form.

---

### NV 47 — Trả lại hay giữ lấy (nhánh A, id 47)

**Chương** 6 — Lõi Hư Không *(nhiệm vụ cuối tuyến — 6 bước, kết bằng boss)* | **Mốc SM khi xong** 18.000.000.000 + mở giới hạn | **Nhiệm vụ kế** — (hết tuyến)

*Heart tan rồi, nhưng Lõi vẫn nằm trong ngực người chơi, và toàn bộ quá khứ của vũ trụ đang chờ một câu trả lời.
Trả lại: mọi hành tinh nhớ ra mọi thứ — kể cả những thứ họ đã sống yên ổn nhờ quên đi. Người chơi mất hết sức mạnh của Lõi.
Đây là nhánh A: **trả ký ức cho vũ trụ**.*

| # | Tên bước (hiển thị cho người chơi) | Kiểu | Điều kiện server kiểm tra | Số lượng | Map (id + tên) | NPC (id + tên) | Hằng số | Thưởng bước |
|---|---|---|---|---|---|---|---|---|
| 0 | Quyết định số phận của Lõi | **B14** | `checkDoneTaskConfirmMenuNpc` — npc **64 Thiên Sứ Whis** ở map **145**, menu 2 nút: `select 0` = "Trả ký ức<br>cho vũ trụ" → ở lại task **47**, index → 1; `select 1` = "Giữ<br>Lõi Hư Không" → `TaskService.switchTaskMain(player, 50, index 1)` | 1 | 145 Võ Đài Siêu Cấp | 64 Thiên Sứ Whis | `TASK_47_0` | +800M SM & TN |
| 1 | Trả Lõi Hư Không | A12 | `checkDoneTaskUseItem` — dùng item **ITEM MỚI 2101 "Lõi Hư Không"** khi đứng ở map **145** → **trừ item 2101**, trao **ITEM MỚI 2102 "Vỏ Lõi rỗng"** (icon mượn 12846 của 1560 Rương ngọc rồng) | 1 | 145 Võ Đài Siêu Cấp | 64 Thiên Sứ Whis | `TASK_47_1` | +800M SM & TN |
| 2 | Lên Thánh địa Kaio | A6 | `checkDoneTaskGoToMap` — map **50 Thánh địa Kaio** | 1 | 50 Thánh địa Kaio | — | `TASK_47_2` | +800M SM & TN |
| 3 | Mở giới hạn sức mạnh lần cuối | **B10** | `OpenPowerService.openPowerByTask` — `limitPower` tăng **2 → 3** (trần SM 29.999.999.999), **miễn phí, không tốn vàng**, qua nhánh "Phá giới hạn (nhiệm vụ)" của npc **42 Quốc Vương** hoặc **43 Tổ Sư Kaio**, chỉ mở khi `getIdTask == TASK_47_3` | 1 | 50 Thánh địa Kaio / 43 Vách núi Moori | 43 Tổ Sư Kaio / 42 Quốc Vương | `TASK_47_3` | +800M SM & TN |
| 4 | Nghe Tổ Sư Kaio nói lời cuối | A3 | `checkDoneTaskTalkNpc` — npc **43 Tổ Sư Kaio**, map 50 | 1 | 50 Thánh địa Kaio | 43 Tổ Sư Kaio | `TASK_47_4` | +800M SM & TN |
| 5 | Hạ Hư Không Vô Danh | A2 | `checkDoneTaskKillBoss` — boss **-108108**, `currentLevel = 3` ("Hư Không Vô Danh", HP 2.000.000.000, dame 500.000, `mapJoin = {145}`) | 1 | 145 Võ Đài Siêu Cấp | — | `TASK_47_5` | +800M SM & TN |

- **Lời thoại**
  - NPC 64 Thiên Sứ Whis: "Lõi trong ngực cậu. Trả lại thì vũ trụ nhớ hết — cả những thứ họ sống yên nhờ quên."
  - NPC 64 Thiên Sứ Whis: "Giữ lấy thì cậu là người duy nhất nhớ. Mạnh nhất, và một mình. Cậu chọn đi."
  - NPC 43 Tổ Sư Kaio: "Ngươi trả rồi. Cả vũ trụ vừa nhớ ra mọi thứ cùng một lúc. Có nơi đang khóc đấy."
  - NPC 43 Tổ Sư Kaio: "Ông ngươi vừa gọi đúng tên ngươi. Ngươi đổi sức mạnh lấy một câu gọi tên. Đáng."
  - Boss -108108 Hư Không Vô Danh: "Ta là phần bị bỏ lại. Không tên, không chủ. Ngươi trả ta cho ai đây?"
- **Mô tả nhiệm vụ (cột `detail` DB)**
  > Bạn chọn trả toàn bộ ký ức về cho vũ trụ. Hãy dùng Lõi Hư Không ở Võ Đài Siêu Cấp,
  > mở giới hạn sức mạnh lần cuối rồi hạ phần Hư Không còn sót lại.
  > Thưởng 8.000.000.000 sức mạnh — Thưởng 8.000.000.000 tiềm năng
  > Thưởng 2 bộ Ngọc Rồng — Thưởng danh hiệu Người Trả Ký Ức
- **Thưởng khi hoàn thành**: +8.000.000.000 SM, +8.000.000.000 TN, **2 bộ Ngọc Rồng (14, 15, 16, 17, 18, 19, 20 — mỗi loại 2 viên)**, **danh hiệu ITEM MỚI 2120 "Người Trả Ký Ức"** (TYPE 36, `idEffect` mới **260**; option đề xuất: Sức đánh +12%, HP +12%, KI +12%, Hạn sử dụng 30 ngày).
- **Mở khóa**: danh hiệu kết thúc nhánh A; cờ hậu truyện `"đã trả ký ức"` (suy ra từ `taskMain.id == 47 && index == 5` đã hoàn thành).
- **Ghi chú triển khai**
  - **B14** (trigger mới) — điểm rẽ nhánh. Vì `data_task` chỉ lưu `[id, index, count, lastTime]`, nhánh **phải** là task id riêng (§7 file 20). Viết `TaskService.switchTaskMain(player, newTaskId, newIndex)`: nạp `getTaskMainById(player, 50)`, đặt `index = 1`, `count = 0`, `sendTaskMain` (message 40) + thông báo. Móc vào `checkDoneTaskConfirmMenuNpc` (hàm đã có, hiện chỉ phục vụ NPC Đậu thần).
  - Sửa bảng chuyển tiếp trong `sendNextTaskMain` đúng §7:
    ```
    id 20 → 21      id 48 → 21
    id 31 → 32      id 49 → 32
    id 47 → hết     id 50 → hết
    còn lại → id + 1
    ```
    Với `id 47` và `id 50`: **không** gọi `getTaskMainById(id + 1)`; thay vào đó đặt cờ hoàn thành tuyến, trao thưởng kết, gửi thông báo toàn server và **không** đổi `taskMain`.
  - **Danh hiệu**: thêm dòng vào `data_badges` (`idEffect = 260`, `idItem = 2120`) và item TYPE 36 id 2120 vào `item_template`. `BadgesTaskService` gán thời hạn **30 ngày** — với danh hiệu kết thúc tuyến nên đổi thành **vĩnh viễn** (bỏ option 93), cần sửa `BadgesTaskService` hoặc trao thẳng item.
  - Heart form 4 ("Hư Không Vô Danh") dùng **cùng id -108108**, `currentLevel = 3`. Sau khi form 3 chết ở NV 46, boss về `REST`; form 4 chỉ được phép `RESPAWN` khi trong khu có người đang ở `TASK_47_5` hoặc `TASK_50_5` — kiểm tra trong `joinMap()` giống cách `GoldenFrieza` kiểm tra khung giờ 21h.
  - Bước cuối là boss, đúng luật §4.3 "nhiệm vụ cuối chương luôn kết bằng boss".

---

### NV 50 — Trả lại hay giữ lấy (nhánh B, id 50)

**Chương** 6 — Lõi Hư Không *(nhánh kết thay thế của NV 47 — 6 bước, kết bằng boss)* | **Mốc SM khi xong** 18.000.000.000 + mở giới hạn | **Nhiệm vụ kế** — (hết tuyến)

*Giữ lấy: người chơi trở thành người duy nhất trong vũ trụ còn nhớ.
Không ai cảm ơn, vì không ai biết mình đã mất gì. Sức mạnh của Lõi ở lại trong ngực,
và nó kéo người chơi về đúng nơi Heart từng nuôi Cumber — vì Lõi nhớ đường về nhà.*

| # | Tên bước (hiển thị cho người chơi) | Kiểu | Điều kiện server kiểm tra | Số lượng | Map (id + tên) | NPC (id + tên) | Hằng số | Thưởng bước |
|---|---|---|---|---|---|---|---|---|
| 0 | Quyết định số phận của Lõi | **B14** | Bước này **được đánh dấu xong ngay khi chuyển nhánh** (`switchTaskMain(player, 50, 1)` đặt `index = 1`); giữ trong `task_sub_template` để `subTasks.size()` khớp và người chơi xem lại thấy lựa chọn của mình | 1 | 145 Võ Đài Siêu Cấp | 64 Thiên Sứ Whis | `TASK_50_0` | — (đã trao ở `TASK_47_0`) |
| 1 | Hấp thụ Lõi Hư Không | A12 | `checkDoneTaskUseItem` — dùng item **ITEM MỚI 2101 "Lõi Hư Không"** khi đứng ở map **145** → **giữ nguyên item 2101** (không trừ), gắn cờ hiển thị hiệu ứng | 1 | 145 Võ Đài Siêu Cấp | 64 Thiên Sứ Whis | `TASK_50_1` | +800M SM & TN |
| 2 | Về Hành tinh ngục tù | A6 | `checkDoneTaskGoToMap` — map **155 Hành tinh ngục tù** | 1 | 155 Hành tinh ngục tù | — | `TASK_50_2` | +800M SM & TN |
| 3 | Mở giới hạn sức mạnh lần cuối | **B10** | `OpenPowerService.openPowerByTask` — `limitPower` tăng **2 → 3** (trần SM 29.999.999.999), **miễn phí, không tốn vàng**, qua nhánh "Phá giới hạn (nhiệm vụ)" của npc **42 Quốc Vương** hoặc **43 Tổ Sư Kaio**, chỉ mở khi `getIdTask == TASK_50_3` | 1 | 43 Vách núi Moori / 50 Thánh địa Kaio | 42 Quốc Vương / 43 Tổ Sư Kaio | `TASK_50_3` | +800M SM & TN |
| 4 | Nghe Ôsin nói lời cuối | A3 | `checkDoneTaskTalkNpc` — npc **44 Ôsin**, map **155** | 1 | 155 Hành tinh ngục tù | 44 Ôsin | `TASK_50_4` | +800M SM & TN |
| 5 | Hạ Hư Không Vô Danh | A2 | `checkDoneTaskKillBoss` — boss **-108108**, `currentLevel = 3` ("Hư Không Vô Danh", HP 2.000.000.000, dame 500.000; với nhánh này `mapJoin` gồm cả **155**) | 1 | 155 Hành tinh ngục tù | — | `TASK_50_5` | +800M SM & TN |

- **Lời thoại**
  - NPC 64 Thiên Sứ Whis: "Cậu giữ nó. Vậy thì từ giờ cả vũ trụ quên, và chỉ mình cậu nhớ giùm họ."
  - NPC 64 Thiên Sứ Whis: "Sẽ không ai cảm ơn cậu đâu. Người ta không cảm ơn thứ họ không biết là mình đã mất."
  - NPC 44 Ôsin: "Cậu về đây làm gì? À... không phải cậu về. Là cái Lõi trong ngực cậu nhớ đường về nhà."
  - NPC 44 Ôsin: "Heart cũng từng đứng đúng chỗ cậu đang đứng. Khác mỗi chuyện hắn quyết định một mình."
  - Boss -108108 Hư Không Vô Danh: "Ngươi giữ ta lại. Vậy thì ngươi với ta, ai là chủ, ai là hũ đựng?"
- **Mô tả nhiệm vụ (cột `detail` DB)**
  > Bạn chọn giữ Lõi Hư Không và trở thành người duy nhất còn nhớ.
  > Lõi kéo bạn về Hành tinh ngục tù — nơi mọi chuyện bắt đầu — để đối mặt phần còn sót lại của nó.
  > Thưởng 8.000.000.000 sức mạnh — Thưởng 8.000.000.000 tiềm năng
  > Thưởng 2 bộ Ngọc Rồng — Thưởng danh hiệu Kẻ Giữ Hư Không
- **Thưởng khi hoàn thành**: **giá trị bằng đúng nhánh A** — +8.000.000.000 SM, +8.000.000.000 TN, **2 bộ Ngọc Rồng (14–20, mỗi loại 2 viên)**, **danh hiệu ITEM MỚI 2121 "Kẻ Giữ Hư Không"** (TYPE 36, `idEffect` mới **261**; option đề xuất: Sức đánh +12%, HP +12%, KI +12%, Hạn sử dụng 30 ngày — **cùng chỉ số với 2120**, chỉ khác tên và icon).
- **Mở khóa**: danh hiệu kết thúc nhánh B; giữ lại item **2101 Lõi Hư Không** trong hành trang làm cờ hậu truyện.
- **Ghi chú triển khai**
  - **Không nhánh nào mạnh hơn nhánh nào** (§7 file 20): hai danh hiệu **cùng option, cùng số**, chỉ khác tên/icon/màu. Đừng cho nhánh B thêm chỉ số chỉ vì "giữ sức mạnh" — đó là chuyện kể, không phải chuyện chỉ số.
  - Khác biệt thật giữa hai nhánh, đúng như §7 mô tả ("lời thoại, 1 bước khác nhau, thưởng cùng giá trị khác loại"):
    | | Nhánh A (47) | Nhánh B (50) |
    |---|---|---|
    | Bước 1 | Trừ item 2101, nhận 2102 Vỏ Lõi rỗng | Giữ item 2101 |
    | Bước 2 | map 50 Thánh địa Kaio | map 155 Hành tinh ngục tù |
    | Bước 4 | npc 43 Tổ Sư Kaio | npc 44 Ôsin |
    | Bước 5 | boss -108108 form 4 tại map 145 | boss -108108 form 4 tại map 155 |
    | Danh hiệu | 2120 Người Trả Ký Ức | 2121 Kẻ Giữ Hư Không |
  - `mapJoin` của Heart form 4 phải là `{145, 155}`; trong `joinMap()` chọn map theo `taskMain.id` của người chơi đang có mặt (47 → 145, 50 → 155), giống cách boss phó bản chọn `zoneFinal`.
  - Task id 50 phải có đủ **6 dòng** trong `task_sub_template` dù bước 0 không bao giờ được người chơi làm — `addDoneSubTask` dựa vào `subTasks.size()` để biết khi nào hết nhiệm vụ.
  - **Gợi ý nội dung hậu truyện** (không nằm trong 48 nhiệm vụ, để dành cho bản cập nhật sau):
    - *Nhánh A — "Vũ trụ nhớ ra":* mọi NPC lớn đổi thoại sang dạng "nhớ ra người chơi"; mở chuỗi nhiệm vụ phụ hằng ngày **"Người ta nhờ nhớ giùm"** tại NPC 0/1/2 (ba ông), thưởng vật phẩm ký ức; boss thế giới mới **"Ký Ức Cũ"** dựng từ tạo hình các NPC đã chết trong tuyến (Bardock), chạy theo lịch giờ như Fide Vàng.
    - *Nhánh B — "Người duy nhất còn nhớ":* item 2101 trong hành trang cho phép mở map riêng **"Kho Ký Ức"** (tái sử dụng map 164 Map riêng tư hoặc 49 Phòng tập thời gian) — mỗi ngày vào 1 lần, đánh lại lần lượt các boss của tuyến 32–46 dưới dạng "ký ức", thưởng theo chuỗi; NPC 108 Heart xuất hiện lại ở đó như một cái bóng không nói được, dần dần nhớ lại.
    - Cả hai nhánh dùng chung điểm hội tụ: một map **Đài Tưởng Niệm** đặt tại 78 Lãnh địa Fize, ghi tên người chơi kèm nhánh đã chọn (đọc từ `taskMain.id` 47/50) — rẻ để làm, và làm cho lựa chọn cuối "thấy được".

---

## 5. Boss mới: Heart

Boss duy nhất phải dựng mới cho toàn tuyến. **Không cần tài nguyên client mới**: tạo hình mượn nguyên
`npc_template` id **108 Heart** (`head = 2109`, `body = 2110`, `leg = 2111`, `avatar = 16165`).
NPC 108 hiện **không có hằng `ConstNpc`, không có class, không xuất hiện trong `map_template`** —
tức là tạo hình đã nằm sẵn trong data client mà server chưa dùng tới. Đúng thứ ta cần.

### 5.1 Hằng số và file phải thêm

| Việc | File | Nội dung |
|---|---|---|
| Id boss | `models/boss/BossID.java` | `public static final int HEART = -108108;` (đã kiểm tra: **chưa trùng** id nào trong `BossID.java`) |
| Dữ liệu 4 form | `models/boss/BossesData.java` | `HEART`, `HEART_2`, `HEART_3`, `HEART_4` (bảng 5.2) |
| Lớp boss | `models/boss/heart/Heart.java` | kế thừa `Boss`, override `injured`, `joinMap`, `reward`, `autoLeaveMap` |
| Tạo boss | `models/boss/Boss_Manager/BossManager.java` | thêm `case BossID.HEART` trong `createBoss`; **không** thêm vào `loadBoss` (xem 5.4) |
| Hằng NPC | `consts/ConstNpc.java` | `public static final int HEART = 108;` (để dùng NPC 108 cho các cảnh hội thoại ngoài trận) |

### 5.2 Khai báo `BossesData` (theo đúng thứ tự tham số của các boss hiện có)

```java
// ----- Form 1: xuất hiện ở Phòng thí nghiệm Myuu (NV 46 bước 1) -----
public static final BossData HEART = new BossData(
        "Heart",                                        // name
        ConstPlayer.TRAI_DAT,                           // gender
        new short[]{2109, 2110, 2111, -1, -1, -1},      // outfit {head, body, leg, bag, aura, eff} — NPC 108
        150_000,                                        // dame
        new int[]{1_500_000_000},                       // hp
        new int[]{166},                                 // mapJoin — 166 Phòng thí nghiệm Myuu
        new int[][]{
            {Skill.THOI_MIEN, 7, 60_000},
            {Skill.TROI, 7, 90_000},
            {Skill.KHIEN_NANG_LUONG, 7, 300_000},
            {Skill.MASENKO, 7, 100},
            {Skill.GALICK, 7, 100}},                    // skillTemp {skillId, level, cooldown(ms)}
        new String[]{                                   // textS — lúc xuất hiện
            "|-1|Ta không ghét ai cả",
            "|-1|Ta chỉ thấy các ngươi đau, và ta biết chỗ cơn đau nằm",
            "|-1|Đưa sáu mảnh đó cho ta, rồi ngươi sẽ ngủ ngon"},
        new String[]{                                   // textM — trong lúc đánh
            "|-1|Ngươi còn nhớ ông ngươi gọi nhầm tên ngươi chứ?",
            "|-1|Ta có thể lấy luôn cả cái đó đi",
            "|-2|Đừng nghe hắn! Hắn nói với ta y hệt vậy!",
            "|-1|Tiến sĩ Myuu, ngươi lại nhớ ra rồi à? Phiền thật",
            "|-1|Quên đi. Quên là món quà, không phải hình phạt"},
        new String[]{                                   // textE — lúc chết
            "|-1|Thân xác này chỉ là chỗ ta tạm ngồi",
            "|-1|Lên Võ Đài Siêu Cấp đi. Ta đợi ở đó"},
        REST_10_M                                       // secondsRest (chỉ form đầu được Boss đọc)
);

// ----- Form 2: Võ Đài Siêu Cấp (NV 46 bước 3) -----
public static final BossData HEART_2 = new BossData(
        "Heart Hư Không",
        ConstPlayer.TRAI_DAT,
        new short[]{2109, 2110, 2111, -1, -1, -1},
        250_000,
        new int[]{2_000_000_000},
        new int[]{145},                                 // 145 Võ Đài Siêu Cấp
        new int[][]{
            {Skill.THAI_DUONG_HA_SAN, 7, 30_000},
            {Skill.TAI_TAO_NANG_LUONG, 7, 300_000},
            {Skill.KHIEN_NANG_LUONG, 7, 300_000},
            {Skill.DICH_CHUYEN_TUC_THOI, 7, 20_000},
            {Skill.KAMEJOKO, 7, 100},
            {Skill.GALICK, 7, 100}},
        new String[]{"|-1|Đây mới là ta. Phần còn lại chỉ là bộ đồ"},
        new String[]{
            "|-1|Ngươi đánh ta để giữ ký ức. Ta lấy ký ức để bớt người phải đánh nhau",
            "|-1|Ai trong hai ta đang hủy diệt ít hơn?",
            "|-2|Cậu không cần trả lời hắn. Cứ đánh đi",
            "|-1|Thiên sứ thì biết gì về quên"},
        new String[]{"|-1|Chưa xong đâu. Mảnh thứ bảy vẫn trong ngực ta"},
        REST_10_M
);

// ----- Form 3: Võ Đài Siêu Cấp (NV 46 bước 4) -----
public static final BossData HEART_3 = new BossData(
        "Heart Toàn Ký",
        ConstPlayer.TRAI_DAT,
        new short[]{2109, 2110, 2111, -1, -1, -1},
        400_000,
        new int[]{2_000_000_000},
        new int[]{145},
        new int[][]{
            {Skill.THAI_DUONG_HA_SAN, 7, 30_000},
            {Skill.TAI_TAO_NANG_LUONG, 7, 200_000},
            {Skill.KHIEN_NANG_LUONG, 7, 200_000},
            {Skill.LIEN_HOAN, 7, 10_000},
            {Skill.SUPER_KAME, 7, 10_000},
            {Skill.ANTOMIC, 7, 100},
            {Skill.GALICK, 7, 100}},
        new String[]{
            "|-1|Sáu mảnh trong ngực ngươi vừa gọi mảnh thứ bảy trong ngực ta",
            "|-1|Chúng nhớ nhau đấy. Ngươi nghe thấy không?"},
        new String[]{
            "|-1|Ta đã xóa hai vạn hành tinh. Không hành tinh nào khóc cả",
            "|-1|Vì không ai còn nhớ để mà khóc",
            "|-2|Tôi nhớ. Tôi nhớ hết",
            "|-1|Ngươi là lỗi duy nhất của ta"},
        new String[]{
            "|-1|Ngươi không giết ta. Ngươi chỉ mở cái hũ ra",
            "|-1|Giờ thì tới lượt ngươi chọn..."},
        REST_10_M
);

// ----- Form 4: "Hư Không Vô Danh" — chỉ dùng ở NV 47 / NV 50 -----
public static final BossData HEART_4 = new BossData(
        "Hư Không Vô Danh",
        ConstPlayer.TRAI_DAT,
        new short[]{2109, 2110, 2111, -1, -1, -1},
        500_000,
        new int[]{2_000_000_000},
        new int[]{145, 155},                            // 145 (nhánh 47) hoặc 155 (nhánh 50)
        new int[][]{
            {Skill.THAI_DUONG_HA_SAN, 7, 25_000},
            {Skill.TAI_TAO_NANG_LUONG, 7, 200_000},
            {Skill.KHIEN_NANG_LUONG, 7, 200_000},
            {Skill.THOI_MIEN, 7, 60_000},
            {Skill.LIEN_HOAN, 7, 10_000},
            {Skill.SUPER_KAME, 7, 8_000},
            {Skill.GALICK, 7, 100}},
        new String[]{
            "|-1|Ta là phần bị bỏ lại",
            "|-1|Không tên, không chủ. Ngươi trả ta cho ai đây?"},
        new String[]{
            "|-1|Ngươi giữ ta lại. Vậy ai là chủ, ai là hũ đựng?",
            "|-1|Mỗi lần ngươi nhớ một người, ta lại dày thêm một chút"},
        new String[]{"|-1|Được rồi... ta đi đây. Nhớ giùm ta nhé"},
        REST_10_M
);
```

### 5.3 Bảng tóm tắt chỉ số

| Form | `currentLevel` | Tên | HP | Dame | Map | Skill chính | Dùng ở |
|---|---|---|---|---|---|---|---|
| 1 | 0 | Heart | 1.500.000.000 | 150.000 | 166 Phòng thí nghiệm Myuu | Thôi miên 7, Trói 7, Khiên NL 7, Masenko 7, Galick 7 | NV 46 bước 1 |
| 2 | 1 | Heart Hư Không | 2.000.000.000 | 250.000 | 145 Võ Đài Siêu Cấp | + Thái Dương Hạ San 7, Tái tạo NL 7, Dịch chuyển tức thời 7, Kamejoko 7 | NV 46 bước 3 |
| 3 | 2 | Heart Toàn Ký | 2.000.000.000 | 400.000 | 145 Võ Đài Siêu Cấp | + Liên hoàn 7, Super Kamejoko 7, Antomic 7 | NV 46 bước 4 |
| 4 | 3 | Hư Không Vô Danh | 2.000.000.000 | 500.000 | 145 hoặc 155 | toàn bộ, hồi chiêu ngắn nhất | NV 47 bước 5 / NV 50 bước 5 |

`secondsRest` = `REST_10_M` (600 giây). Lưu ý `Boss` constructor **chỉ đọc `data[0].getSecondsRest()`**, nên chỉ form 1 có tác dụng.

### 5.4 Hành vi riêng cần viết trong `Heart.java`

| Hàm | Nội dung |
|---|---|
| `joinMap()` | Chỉ cho vào map khi trong khu có **ít nhất 1 người chơi ở đúng bước tương ứng** (`TASK_46_1` cho form 1, `TASK_46_3`/`TASK_46_4` cho form 2–3, `TASK_47_5`/`TASK_50_5` cho form 4); nếu không → quay lại `REST`. Mẫu code: `GoldenFrieza.joinMap()` (kiểm tra khung 21h). Form 4 chọn map theo `taskMain.id` của người chơi trong khu: 47 → 145, 50 → 155. |
| `injured(Player, long, boolean, boolean)` | Né **5%** (`Util.isTrue(50, 1000)`); `(dame − rand(0..200_000)) − def`; khiên năng lượng → 1; **giới hạn tối đa `hpMax / 50` sát thương mỗi đòn** (tránh người chơi 18 tỷ SM one-shot). Nhớ override đúng chữ ký `long` (lỗi Dr.Kôrê ở `11-boss.md` mục 9.3 là do override nhầm chữ ký `int`). |
| `reward(Player plKill)` | **Bắt buộc gọi `TaskService.gI().checkDoneTaskKillBoss(plKill, this)`** (đây là lỗi mà Cooler / Baby / Broly / Dr Lychee đang mắc). Ngoài ra: +5 Point sự kiện; huy hiệu `TRUM_SAN_BOSS`; form 3 rơi thêm **ITEM MỚI 2107 "Ống nghiệm Myuu"**; form 4 rơi **1 đồ Thần Linh** (`ItemService.randDoTLBoss`). |
| `autoLeaveMap()` | 10 phút không có người trong khu → `leaveMapNew()` (về `REST`, giữ `currentLevel`) để người chơi quay lại vẫn gặp đúng form. |
| `notifyJoinMap()` | Đặt `isNotifyDisabled = true` — tránh spam thông báo toàn server 4 lần liên tiếp khi đổi form. |

### 5.5 Rào chắn kỹ thuật phải nhớ

- **`BossData.hp` là `int[]`** → trần **2.147.483.647**. Không khai báo HP 3 tỷ hay 5 tỷ (sẽ tràn số âm). Mọi form của Heart vì vậy dừng ở **2.000.000.000**, đúng như Super Black Goku / Super Cumber đang làm. Muốn boss "dai" hơn thì tăng giới hạn sát thương nhận mỗi đòn trong `injured`, đừng tăng HP.
- **Không đưa Heart vào `BossManager.loadBoss()`** — nếu đưa vào, boss sẽ tự xuất hiện ngay tick đầu sau khi server chạy (trạng thái khởi tạo là `REST` với `lastTimeRest = 0`) và người chơi chưa tới NV 46 cũng gặp. Tạo bằng `createBoss(BossID.HEART)` khi cần, hoặc để trong `loadBoss` nhưng chặn ở `joinMap()` như mô tả ở 5.4.
- **Nhịp tick `BossManager` là 1.500 ms** → mọi ngưỡng "mỗi 100 ms" trong `attack()` thực tế là mỗi ~1,5 giây. Đặt cooldown skill theo con số thật, đừng kỳ vọng phản ứng nhanh.
- `Boss.injured` mặc định **không trừ giáp** — phải tự trừ `def` trong bản override, nếu không boss ăn trọn sát thương.
- Heart dùng `gender = ConstPlayer.TRAI_DAT` để khớp hiệu ứng skill mặc định; tạo hình lấy từ `outfit` nên giới tính không ảnh hưởng hình ảnh.

---

## 6. Vật phẩm mới dùng ở 20c

Bảng `item_template` hiện dừng ở **id 1999**, nên mọi item mới dùng id **≥ 2000**.
Cột "Icon mượn" là `icon_id` của một item đã có — dùng lại để **không phải thêm tài nguyên client**
(vẫn phải tăng phiên bản data để client tải lại bảng `item_template`).

| id | Tên | TYPE | Icon mượn (từ item) | Dùng ở | Ghi chú |
|---|---|---|---|---|---|
| 2010–2013 | Mảnh Ký Ức #1 … #4 | 27 | 419, 420, 421, 422 (Ngọc Rồng 1–4 sao) | 20a / 20b; **20c dùng lại #1 ở NV 40** | Do file 20a/20b định nghĩa — **phải thống nhất id** |
| **2014** | Mảnh Ký Ức #5 | 27 | **423** (18 Ngọc Rồng 5 sao) | NV 39 bước 3 | Vật phẩm nhiệm vụ, không giao dịch |
| **2015** | Mảnh Ký Ức #6 | 27 | **424** (19 Ngọc Rồng 6 sao) | NV 39 bước 5 | Vật phẩm nhiệm vụ, không giao dịch |
| **2016** | Mảnh Ký Ức #7 | 27 | **425** (20 Ngọc Rồng 7 sao) | NV 45 bước 1 | Chỉ hiện trên đất với người ở `TASK_45_1` |
| **2100** | Lõi Ký Ức chưa hoàn chỉnh | 27 | **9650** (1015 Ngọc rồng Siêu Cấp) | NV 45 bước 3 | Dùng để kích hoạt lễ hợp nhất |
| **2101** | Lõi Hư Không | 27 | **2321** (378 Ngọc rồng 7 sao đen) | NV 45 → NV 47/50 | Nhánh A trừ đi; nhánh B giữ lại làm cờ hậu truyện |
| **2102** | Vỏ Lõi rỗng | 27 | **12846** (1560 Rương ngọc rồng) | NV 47 bước 1 | Kỷ vật nhánh A |
| **2103** | Mảnh Ký Ức Vỡ | 27 | **6467** (674 Đá ngũ sắc) | NV 32 bước 4–5 | Rơi từ mob 81 Tobi |
| **2104** | Mảnh Ký Ức Đóng Băng | 27 | **8620** (935 Đá xanh lam) | NV 34 bước 3–5 | Chỉ hiện ở map 110 với người ở `TASK_34_3` |
| **2105** | Mảnh Bùa Babiđây | 27 | **7743** (861 Hồng ngọc) | NV 36 bước 3 (đường vòng) | Rơi từ mob 95/118 Cadic M ở map 165 |
| **2106** | Lõi Phép Babiđây | 27 | **5829** (638 Bình chứa Commeson) | NV 37 bước 3–4 | Rơi 100% cho người kết liễu bước 2 |
| **2107** | Ống nghiệm Myuu | 27 | **6849** (727 Siêu thần thủy) | NV 46 bước 4 | Rơi từ Heart form 3 |
| **2120** | Người Trả Ký Ức | **36** | **11614** (1293 Cao thủ siêu hạng) | NV 47 | Danh hiệu; `data_badges.idEffect = 260` |
| **2121** | Kẻ Giữ Hư Không | **36** | **11617** (1291 Trùm săn Boss) | NV 50 | Danh hiệu; `data_badges.idEffect = 261`; **chỉ số bằng đúng 2120** |

> Id **2014** để trống có chủ ý, dành cho "Nhật ký Bardock" nếu muốn thêm một vật phẩm kỷ niệm ở NV 39
> (icon mượn **5207** của item 590 Bí kiếp).
>
> **Phương án không thêm item mới** (nếu chủ dự án chốt như vậy ở §10 mục 1 file 20):
> 2014/2015/2016 → **18, 19, 20 Ngọc Rồng 5/6/7 sao**; 2101 → **1015 Ngọc rồng Siêu Cấp**;
> 2103–2106, 2107 → **992 Nhẫn thời không sai lệch** hoặc **674 Đá ngũ sắc** (đổi số lượng để phân biệt bước);
> 2120/2121 → dùng lại 2 danh hiệu chưa gắn nhiệm vụ: **1288 Chiến thần khóa nick** và **1673 Tay nhanh hơn não** (đổi tên trong DB).
> Khi đó tuyến chạy được **mà không phải đụng data client**, chỉ mất phần hình ảnh riêng.

---

## 7. Ghi chú / việc cần làm khi code

- **Nhiệm vụ chính không thưởng vàng/ngọc/hồng ngọc** — chỉ SM, TN và vật phẩm (quyết định của chủ dự án).

### 7.1 Lỗi cũ **bắt buộc sửa trước**, nếu không nhiệm vụ 32–47 sẽ kẹt

| # | Lỗi | File | Chặn nhiệm vụ nào | Cách sửa |
|---|---|---|---|---|
| 1 | `Cooler.reward()` **không gọi** `checkDoneTaskKillBoss` (`11-boss.md` 9.17) | `boss/Cold/Cooler.java` | **NV 34** bước 4 | Thêm `TaskService.gI().checkDoneTaskKillBoss(plKill, this);` vào `reward()` |
| 2 | `Baby.reward()` **không gọi** nhiệm vụ và không cộng Point (9.17) | `boss/Baby/Baby.java` | **NV 39** bước 6 (cao trào chương 5) | như trên |
| 3 | `Broly.die()` chỉ `DIE`, **không `reward()`** (5.17) | `boss/Broly/Broly.java` | **NV 41** bước 2 | Gọi `reward(plKill)` hoặc ít nhất `checkDoneTaskKillBoss` |
| 4 | `SuperBroly.reward()` chỉ tạo đệ tử, **không gọi nhiệm vụ** | `boss/Broly/SuperBroly.java` | **NV 41** bước 3 | như trên |
| 5 | `DrLychee.reward()` / `Hatchiyack.reward()` **không gọi nhiệm vụ** (`11b` mục 3) | `boss/khi_gas/DrLychee.java`, `Hatchiyack.java` | **NV 43** bước 2–3 | như trên |
| 6 | `Osin.confirmMenu` để `case 114..120` và `case 127` **bên trong** `switch(indexMenu)` của map 52 → menu "Xuống tầng dưới" của phe Ôsin không chạy (`18-npc.md` 9.8) | `npc_list/Osin.java` | **NV 36** bước 3 | Tách thành `switch (mapId)` đúng như `Babiday.java` |
| 7 | `Bill.confirmMenu` dùng `case 2` theo `mapId` thay vì `indexMenu`; nút "Nói chuyện" đặt `indexMenu` 100/101 **không ai xử lý** (9.2) | `npc_list/Bill.java` | **NV 44** bước 1 | Viết lại `confirmMenu`, gọi `checkDoneTaskTalkNpc` ở `openBaseMenu` |
| 8 | `DaiThienSu` (NPC 64) `openBaseMenu` và `confirmMenu` **rỗng** (6.43) | `npc_list/DaiThienSu.java` | **NV 45** bước 4, **NV 46** bước 5, **NV 47/50** bước 0 (điểm rẽ nhánh!) | Viết menu; đây là NPC dẫn cả đoạn kết |
| 9 | `Whis.injured` không kiểm tra `isDie()`, so `damage >= hp` **sau khi** đã trừ HP (`11b` 12.11) | `boss/luyen_tap_tu_dong/Whis.java` | **NV 44** bước 3 | Kiểm tra `isDie()` trước, so sánh trước khi trừ |
| 10 | `Drabura 2 (-237)` không đi qua `die()/reward()` nên **không trao thưởng/nhiệm vụ** (`11-boss.md` 9.6) | `boss/MajinBuu_12h/Drabura2.java` | không chặn (20c né bằng cách dùng **-233**), nhưng nên sửa | Cho `afk()` gọi `die()` bình thường |
| 11 | `checkMapCanJoin` case **80** thiếu `break` (`14` 16.3) | `map/service/ChangeMapService.java` | tuyến cũ, sửa khi đánh số lại mốc | Thêm `break` |
| 12 | `SAIBAMEN.afk()` `Functions.sleep(1500)` và `CADICH.attack()` `sleep(2000)` **chặn thread `SnakeWayManager`** (`11b` 12.5) | `boss_con_duong_ran_doc/SAIBAMEN.java`, `CADICH.java` | **NV 35** (CĐRĐ thành nhiệm vụ bắt buộc → tải tăng mạnh) | Thay bằng mốc thời gian, không `sleep` trong thread manager |
| 13 | Tên hiển thị của Hatchiyack là khoảng trắng `" "` | `boss/BossesData.java` | NV 43 (thông báo trông như lỗi) | Đổi `name` thành `"Hatchiyack"` |

### 7.2 Chặn cứng về điều kiện (không phải bug, nhưng làm người chơi kẹt)

| # | Vấn đề | Chặn ở | Xử lý đề xuất |
|---|---|---|---|
| 1 | **NPC 42 Quốc Vương chỉ hiện khi SM ≥ 17 tỷ**, còn `openPowerBasic` đòi `power >= getPowerLimit()` (≈18 tỷ); nhiệm vụ **không phát vàng** nên cũng không dùng được nhánh trả tiền `openPowerSpeed` | **NV 33** (mốc 3 tỷ), NV 44, NV 47/50 | Hiện NPC khi `getIdTask >= TASK_33_1`; thêm hàm MỚI `OpenPowerService.openPowerByTask(player)` **mở giới hạn miễn phí một lần** khi người chơi đang đúng bước B10 (`TASK_33_3` / `TASK_44_4` / `TASK_47_3` / `TASK_50_3`), không trừ vàng/ngọc và không kiểm tra SM; giữ nguyên `openPowerBasic` và `openPowerSpeed` cho nội dung ngoài tuyến |
| 2 | **NPC 42 Quốc Vương chỉ có ở map 43** (Namếc) trong `map_template` | NV 33 với người Trái Đất / Xayda | Thêm NPC 42 vào `map_template.npcs` của map **42** và **44** |
| 3 | **Map 156–159 đòi SM ≥ 40 tỷ** ở NPC 47 Giu-ma Đầu Bò | **NV 32** (mốc 2,5 tỷ) | Đổi điều kiện thành `getIdTask >= TASK_32_1` |
| 4 | **Map 155 mở ở `TASK_42_0` nhưng đường vào duy nhất đi qua map 154 (mở ở `TASK_44_0`)** | **NV 42** — không thể bắt đầu | Thêm nút "Đến hành tinh ngục tù" cho Ôsin ở **map 50**, hiện khi `getIdTask >= TASK_42_0` |
| 5 | **NPC 70 Bardock chỉ có ở map 160**, NV 39 cần ông ở map 14 | **NV 39** bước 5 | Thêm NPC 70 vào `map_template.npcs` của map 14, ẩn/hiện qua `NpcManager.getNpcsByMapPlayer` (mẫu NPC Ca Lích) |
| 6 | **NPC 83 Dr. Myuu không có trong `map_template`**; map 166 không có NPC nào | **NV 46** bước 0 | Thêm `[83, x, y]` vào `map_template.npcs` map 166 |
| 7 | **CĐRĐ: 1 lần / 7 ngày / người + vào bang ≥ 2 ngày** | **NV 35** | Bỏ kiểm tra 7 ngày khi `getIdTask ∈ [TASK_35_1, TASK_35_3]`; hoặc cho B1 chấp nhận Doanh Trại / BĐKB |
| 8 | **Khí gas: chỉ bang chủ mở, 3 lần/ngày/bang, vào bang ≥ 2 ngày** | **NV 43** | Như trên; và cho **mọi người trong instance** hoàn thành bước, không chỉ `plKill` |
| 9 | **Map 105–110 giảm 50% HP/sức đánh** nếu không có option 106 "Không ảnh hưởng bởi cái lạnh" | **NV 34** | Trao bùa/cải trang có option 106 hạn 1 ngày ở bước 0, hoặc ghi rõ trong `detail` |
| 10 | **Kid Bư (form 5 của -214) chỉ chết bằng Quả cầu kênh khi** | **NV 37** nhánh 14h | Giữ nhánh 14h là **lựa chọn**, không bắt buộc; ghi rõ trong `detail` |
| 11 | **Map 50, 116 là `MAP_OFFLINE` → chỉ 1 khu, 15 người** | NV 40, 41, 42, 44, 47 đều dừng chân ở map 50 | Cân nhắc đổi map 50 sang `type 0` (10 khu) trong `map_template` |
| 12 | **Map 145 chưa bị khóa** trong `checkMapCanJoin` | NV 46 | Thêm mốc `TASK_46_2` |

### 7.3 Trigger mới phải viết cho riêng 20c (và điểm móc)

| Mã | Trigger | Hàm mới trong `TaskService` | Điểm móc | Dùng ở |
|---|---|---|---|---|
| **B1** | Hoàn thành phó bản | `checkDoneTaskDungeon(player, typeMap)` | `map/phoban/SnakeWay.finish()` + `CADICH.leaveMap()`; `map/phoban/DestronGas.finish()` + cờ `hatchiyatchDead`; (dùng lại cho `RedRibbonHQ`, `BanDoKhoBau`) | NV 35, NV 43 |
| **B5** | Thắng trận đấu | `checkDoneTaskWinMatch(player, typePvp)` | `matches/ThachDau.reward()`; `matches/dai_hoi_vo_thuat/DeathOrAliveArena` (chỗ `haveRewardVDST = true`); `WorldMartialArtsTournament` (chỗ `martialArtsTournamentWins++`) | NV 44 |
| **B6** | Gọi rồng thần và ước | `checkDoneTaskWishDragon(player, starType)` | `services/shenron/SummonDragon.confirmWish()`, ngay trước `lastTimeShenronAppeared = now` | NV 39 |
| **B10** | Mở giới hạn sức mạnh | `checkDoneTaskOpenPower(player)` + hàm MỚI `OpenPowerService.openPowerByTask(player)` (**miễn phí, một lần / bậc**) | `services/OpenPowerService` — sau `limitPower++` ở **cả ba** nhánh: `openPowerBasic` (hết 2,4 giờ), `openPowerSpeed` (50 triệu vàng, ngoài tuyến) và `openPowerByTask` (nhiệm vụ). Nhánh mới lộ ra qua menu "Phá giới hạn (nhiệm vụ)" ở `npc_list/QuocVuong.java` và `npc_list/ToSuKaio.java` | NV 33, NV 44, NV 47/50 |
| **B11** | Đạt HP/KI/giáp/CM gốc X | `checkDoneTaskBasePoint(player, type)` | `player/NPoint.increasePoint(type, point)` — mở rộng `checkDoneTaskNangCS` vốn chỉ xét `type 2` | NV 33 |
| **B12** | Bước có giới hạn thời gian | `checkTimeLimitSubTask(player)` | `TaskMain.lastTime` (đã có trong `data_task`); kiểm tra trong `Player.update`; gửi thời gian còn lại qua message **43** | NV 34, NV 42 |
| **B13** | Làm cùng người khác | `checkDoneTaskTogether(player, zone, nMember, requireSameClan)` | Mẫu `NMEMBER_DO_TASK_TOGETHER` đã có ở nhiệm vụ 14/15 của tuyến cũ | NV 35 (cùng bang), NV 45 (không cần cùng bang) |
| **B14** | Rẽ nhánh theo lựa chọn | `switchTaskMain(player, newTaskId, newIndex)` | `checkDoneTaskConfirmMenuNpc` (hàm đã có) + bảng chuyển tiếp trong `sendNextTaskMain` | NV 47 → NV 50 |
| A12 | Dùng item | `checkDoneTaskUseItem` — **hàm đã có, `switch` đang rỗng** | `services_func/UseItem.java` | NV 32 (item 992), NV 40 (2010), NV 45 (2100), NV 47/50 (2101) |

### 7.4 Mốc khóa map / tính năng do 20c đặt ra

| Map / tính năng | Mốc cũ | Mốc mới | File |
|---|---|---|---|
| 156–159 Thánh địa | SM ≥ 40 tỷ (NPC Giu-ma) | `TASK_32_1` | `npc_list/GiuMaDauBo.java` |
| 160–163 Hành tinh thực vật | item 992 | `TASK_32_1` + item 992 | `services_func/UseItem.java` |
| NPC 42 Quốc Vương hiện ra | SM ≥ 17 tỷ | SM ≥ 17 tỷ **hoặc** `TASK_33_1` | `npc_list/QuocVuong.java` |
| Nhánh "Phá giới hạn (nhiệm vụ)" — miễn phí | không có | `TASK_33_3` / `TASK_44_4` / `TASK_47_3` / `TASK_50_3` | `services/OpenPowerService.java`, `npc_list/QuocVuong.java`, `npc_list/ToSuKaio.java` |
| 105–110 Hành tinh Cold | `TASK_27_0` | **`TASK_34_0`** | `ChangeMapService.checkMapCanJoin` |
| Phó bản CĐRĐ (menu NPC 20) | bang ≥ 1–2 ngày | `TASK_35_0` (nới ràng buộc ngày) | `npc_list/ThanVuTru.java`, `SnakeWayService` |
| Phó bản Mabư 12h (Ôsin map 52) | không khóa | `TASK_36_0` | `npc_list/Osin.java` |
| Phó bản Mabư 14h (Ôsin map 52) | không khóa | `TASK_37_0` | `npc_list/Osin.java` |
| Ngọc Rồng Sao Đen (NPC 29) | không khóa | `TASK_39_3` | `npc_list/RongOmega.java` |
| 50, 116 Thánh địa Kaio | không khóa | **`TASK_40_1`** | `ChangeMapService.checkMapCanJoin` |
| 155 Hành tinh ngục tù | không khóa | **`TASK_42_0`** (+ lối vào mới từ map 50) | `ChangeMapService`, `npc_list/Osin.java` |
| Phó bản Khí gas (NPC 67) | bang ≥ 2 ngày | `TASK_43_0` (nới ràng buộc ngày) | `npc_list/MrPoPo.java`, `DestronGasService` |
| 154 Hành tinh Bill | `TASK_27_0` | **`TASK_44_0`** | `ChangeMapService.checkMapCanJoin` |
| Thách đấu Whis | không khóa | `TASK_44_3` | `services_dungeon/TrainingService.java` |
| 145 Võ Đài Siêu Cấp | không khóa | **`TASK_46_2`** | `ChangeMapService.checkMapCanJoin` |

### 7.5 Cân bằng thưởng — điểm cần chủ dự án quyết

- Tổng thưởng SM của chương 5 theo §6.2 là **≈ 12,6 tỷ**, của chương 6 là **≈ 48 tỷ**. Trong khi mốc §6.1 chỉ là
  8 tỷ (cuối chương 5) và 18 tỷ (cuối chương 6). Nghĩa là **phần thừa sẽ bị `Service.addSMTN` cắt** tại trần giới hạn.
- Thiết kế của 20c xử lý bằng **ba lần mở giới hạn**: NV 33 (0→1, trần 20 tỷ), NV 44 (1→2, trần 25 tỷ), NV 47/50 (2→3, trần 30 tỷ).
  Người chơi đi hết tuyến sẽ dừng quanh **18–25 tỷ SM** — đúng tinh thần "18 tỷ + mở giới hạn" của §6.1, và vẫn còn 6 bậc
  giới hạn (4→9) để dành cho nội dung cày sau tuyến.
- Nếu chủ dự án muốn tuyến **không** làm lạm phát sức mạnh: hạ mức thưởng chương 6 từ "4 tỷ → 8 tỷ" xuống "1 tỷ → 2 tỷ"
  mỗi nhiệm vụ và giữ nguyên số mở giới hạn. Con số trong file này khi đó phải sửa đồng loạt ở bảng §2 và ở mục
  "Thưởng khi hoàn thành" của từng nhiệm vụ.
- **Tiền tệ: nhiệm vụ chính không trao vàng, ngọc hay hồng ngọc** (quyết định của chủ dự án). Tổng thưởng của 16 nhiệm vụ
  32–47/50 giờ chỉ gồm **SM, TN và vật phẩm**; thưởng mỗi bước (§2) cũng chỉ còn SM & TN.
- Vì không còn nguồn vàng từ nhiệm vụ, **mở giới hạn sức mạnh trong tuyến phải miễn phí**: NV 33, 44 và 47/50 dùng nhánh
  mới `OpenPowerService.openPowerByTask` (xem [7.2](#72-chặn-cứng-về-điều-kiện-không-phải-bug-nhưng-làm-người-chơi-kẹt) mục 1).
  Nhánh trả tiền `openPowerSpeed` (**50 triệu vàng**) **giữ nguyên luật giá** nhưng chỉ dành cho người chơi mở giới hạn ngoài tuyến.
- Điểm cần chủ dự án cân nhắc: bỏ vàng/ngọc khỏi 16 nhiệm vụ này cắt mất khoảng **2,4 tỷ vàng** và **2.400 ngọc** so với bản
  thiết kế cũ. Nếu muốn bù, nên bù bằng **vật phẩm** (đậu thần, đá nâng cấp, trang bị Thần Linh) hoặc bằng nguồn ngoài tuyến
  (boss, phó bản, sự kiện), **không** bù bằng tiền trong `task_main_reward`.

### 7.6 Thứ tự làm việc đề xuất cho riêng phần 32–47

1. Sửa 13 lỗi ở [7.1](#71-lỗi-cũ-bắt-buộc-sửa-trước-nếu-không-nhiệm-vụ-3247-sẽ-kẹt) — làm trước hết, vì phần lớn chỉ là thêm 1 dòng gọi `checkDoneTaskKillBoss`.
2. Nới 12 chặn cứng ở [7.2](#72-chặn-cứng-về-điều-kiện-không-phải-bug-nhưng-làm-người-chơi-kẹt) và thêm 3 dòng NPC vào `map_template` (42→map 42/44, 70→map 14, 83→map 166).
3. Viết 8 trigger mới ở [7.3](#73-trigger-mới-phải-viết-cho-riêng-20c-và-điểm-móc) — B12 và B13 là hai cái tốn công nhất, làm trước.
4. Sinh SQL cho task id **32–47 và 50** (`task_main_template`, `task_sub_template`, `task_main_reward`) + 14 item mới + 2 dòng `data_badges`.
5. Dựng boss **Heart** theo [mục 5](#5-boss-mới-heart).
6. Cập nhật 14 mốc khóa ở [7.4](#74-mốc-khóa-map--tính-năng-do-20c-đặt-ra).
7. Chạy thử **cả ba hành tinh** và **cả hai nhánh kết** (47 và 50); kiểm riêng ba khung giờ 12h / 14h / 20h và đường vòng ngoài giờ của NV 36, 37, 39.

### 7.7 Danh sách kiểm tra khi test (32–47)

- [ ] NV 33: người chơi 3 tỷ SM có **0 vàng trong túi** vẫn **mở được giới hạn** bằng nhánh "Phá giới hạn (nhiệm vụ)" (miễn phí); sau đó thưởng NV 34 **không bị cắt**.
- [ ] Nhánh "Phá giới hạn (nhiệm vụ)" **không hiện** khi người chơi không ở bước B10, và bấm lại ở cùng một bậc **không** cộng thêm `limitPower` (kiểm cả NV 44 bước 4 và NV 47/50 bước 3).
- [ ] Không nhiệm vụ nào trong 32–47/50 cộng **vàng, ngọc hay hồng ngọc** — kiểm cả thưởng bước (`addDoneSubTask`) lẫn thưởng hoàn thành (`task_main_reward`).
- [ ] NV 34: đồng hồ 5 phút hiển thị đúng; hết giờ `count` về 0 và **có thông báo**; Cooler chết → bước 4 xong.
- [ ] NV 35: người **không có bang** và người **vừa đi CĐRĐ hôm qua** đều đi tiếp được.
- [ ] NV 36 & 37: đăng nhập lúc **03:00 sáng** vẫn hoàn thành được qua đường vòng map 165 / map 126.
- [ ] NV 39: ước rồng 1 sao ghi nhận bước; Bardock ở map 160 **đổi thoại** sau bước 5; Baby chết → bước 6 xong.
- [ ] NV 41: giết Broly → Super Broly hiện tại chỗ → giết tiếp → cả hai bước cùng xong.
- [ ] NV 42: vào map 155 **từ map 50** (không cần qua 154).
- [ ] NV 43: cả bang đều xong bước khi Hatchiyack chết, không chỉ người kết liễu.
- [ ] NV 44: thắng bằng **cả ba loại** trận (thách đấu / Hạt Mít / ĐHVT) đều tính.
- [ ] NV 45: một mình **không** qua được bước B13; rủ thêm 1 người thì qua.
- [ ] NV 46: Heart đổi map 166 → 145 đúng theo form; không spam thông báo toàn server.
- [ ] NV 47/50: chọn "Giữ Lõi" → `data_task` đổi sang `[50, 1, 0, ...]`; chọn "Trả ký ức" → ở lại `[47, 1, 0, ...]`; cả hai đều kết thúc tuyến và nhận đúng danh hiệu.
- [ ] Sau khi xong nhánh kết, `sendNextTaskMain` **không** nhảy sang task 48/51 (không tồn tại) và không gây lỗi null.
