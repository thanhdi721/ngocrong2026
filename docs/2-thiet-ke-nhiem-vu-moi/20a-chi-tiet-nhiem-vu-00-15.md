# 20a — Chi tiết nhiệm vụ 00 → 15 (Chương 1 & Chương 2)

> **File con của [20-thiet-ke-nhiem-vu-moi.md](20-thiet-ke-nhiem-vu-moi.md).** Tên nhiệm vụ, map chính và kiểu bước bám đúng bảng tổng §5 của file gốc.
> Hai file anh em: [20b](20b-chi-tiet-nhiem-vu-16-31.md) (NV 16–31), [20c](20c-chi-tiet-nhiem-vu-32-47.md) (NV 32–47).
> Mọi id map / quái / NPC / vật phẩm tra từ [02b-database-du-lieu-template.md](../1-he-thong-hien-tai/02b-database-du-lieu-template.md). Cơ chế trigger tra từ [12-nhiem-vu-chinh.md](../1-he-thong-hien-tai/12-nhiem-vu-chinh.md). Boss tra từ [11-boss.md](../1-he-thong-hien-tai/11-boss.md).
> **Quy ước placeholder** (đã có sẵn trong `ConstTask`): `MAP_NHA(-2)` = 21/22/23 · `MAP_200(-3)` = 1/8/15 · `MAP_TTVT(-6)` = 24/25/26 · `MAP_QUAI_BAY_600(-7)` = 3/11/17 · `MAP_LANG(-8)` = 0/7/14 · `MAP_QUY_LAO(-9)` = 5/13/20 · `NPC_NHA(-2)` = 0/2/1 · `NPC_QUY_LAO(-5)` = 13/14/15. Thứ tự ghi luôn là **Trái Đất / Namếc / Xayda**.

---

## Mục lục

- [Bảng tóm tắt 16 nhiệm vụ](#bảng-tóm-tắt-16-nhiệm-vụ)
- [Chương 1 — Ngày ký ức vỡ (NV 0 → 7)](#chương-1--ngày-ký-ức-vỡ-nv-0--7)
  - [NV 0 — Người duy nhất còn nhớ](#nv-0--người-duy-nhất-còn-nhớ)
  - [NV 1 — Bài học của ông](#nv-1--bài-học-của-ông)
  - [NV 2 — Vết nứt đầu tiên](#nv-2--vết-nứt-đầu-tiên)
  - [NV 3 — Cảnh sát vũ trụ Jaco](#nv-3--cảnh-sát-vũ-trụ-jaco)
  - [NV 4 — Thứ bò ra từ vết nứt](#nv-4--thứ-bò-ra-từ-vết-nứt)
  - [NV 5 — Ký ức của ông](#nv-5--ký-ức-của-ông)
  - [NV 6 — Người thu gom](#nv-6--người-thu-gom)
  - [NV 7 — Chạy khỏi vết nứt](#nv-7--chạy-khỏi-vết-nứt)
- [Chương 2 — Kẻ trộm ký ức (NV 8 → 15)](#chương-2--kẻ-trộm-ký-ức-nv-8--15)
  - [NV 8 — Máy dò ký ức](#nv-8--máy-dò-ký-ức)
  - [NV 9 — Chuyến bay đầu tiên](#nv-9--chuyến-bay-đầu-tiên)
  - [NV 10 — Bái sư](#nv-10--bái-sư)
  - [NV 11 — Hạt giống hy vọng](#nv-11--hạt-giống-hy-vọng)
  - [NV 12 — Bạn đồng hành](#nv-12--bạn-đồng-hành)
  - [NV 13 — Không ai đi một mình](#nv-13--không-ai-đi-một-mình)
  - [NV 14 — Chợ đen ký ức](#nv-14--chợ-đen-ký-ức)
  - [NV 15 — Người bạn đã quên](#nv-15--người-bạn-đã-quên)
- [Ghi chú / việc cần làm khi code](#ghi-chú--việc-cần-làm-khi-code)

---

## Bảng tóm tắt 16 nhiệm vụ

| NV | Tên | Map chính | Bước | SM & TN thưởng | Mốc SM khi xong | Mở khóa |
|---|---|---|---|---|---|---|
| 0 | Người duy nhất còn nhớ | 21/22/23 Nhà | A6, A8, A3, A9, A12 | 2.000 | ~2.000 | Hành trang, rương đồ, cây đậu thần |
| 1 | Bài học của ông | 0/7/14 Làng | A1, A3, A7 | 3.000 | ~5.500 | Tiềm năng, map đồi 1/8/15 |
| 2 | Vết nứt đầu tiên | 1/8/15 Đồi | A6, A1, A4 | 4.000 | ~11.000 | Vách núi 42/43/44 |
| 3 | Cảnh sát vũ trụ Jaco | 42/43/44 Vách núi | A3, A1, A11 | 5.000 | ~17.000 | Máy đo sức mạnh, shop làng, map 2/9/16 |
| 4 | Thứ bò ra từ vết nứt | 2/9/16 | A1, A1, A3 | 6.000 | ~25.000 | Map rừng 3/11/17 |
| 5 | Ký ức của ông | 3/11/17 Rừng | A4, A12, A3 | 8.000 | ~34.000 | Túi lưng, map 4/12/18 |
| 6 | Người thu gom | 4/12/18 | A1, **A2** (boss mini), A3 | 12.000 | ~46.000 | Đường tới Trạm tàu vũ trụ |
| 7 | **Chạy khỏi vết nứt** | 4/12/18 → 24/25/26 | **B12**, A6, A3 | 20.000 | ~66.000 | Tàu vũ trụ, **Mảnh Ký Ức #1** |
| 8 | Máy dò ký ức | 84 Siêu Thị | A3, **B4**, A1 | 50.000 | ~120.000 | Map 84, shop siêu thị, Máy Dò Ký Ức |
| 9 | Chuyến bay đầu tiên | 5/13/20 | A6, A1, A3 | 70.000 | ~190.000 | Map sư phụ 5/13/20 |
| 10 | Bái sư | 5/13/20 | A3, **B9**, A5 | 100.000 | ~300.000 | Học kỹ năng, shop sách sư phụ |
| 11 | Hạt giống hy vọng | 21/22/23 Nhà | **B7**, A12, A3 | 140.000 | ~450.000 | Cây đậu thần cấp 2 |
| 12 | Bạn đồng hành | 21/22/23 Nhà | **B8**, A1, A3 | 200.000 | ~680.000 | Đệ tử |
| 13 | Không ai đi một mình | 153 Lãnh địa Bang Hội | A10, **B13**, A3 | 280.000 | ~980.000 | Bang hội, map 153, phó bản bang |
| 14 | Chợ đen ký ức | 84 Siêu Thị, 102 Nhà Bunma | A3, **B4**, A1 | 400.000 | ~1.400.000 | Ký gửi, map 102, map 27/31/35 |
| 15 | **Người bạn đã quên** | 27/31/35 | A1, **A2** (boss mới), A3 | 600.000 | ~2.000.000 | **Mảnh Ký Ức #2**, PVP / Võ đài Hạt Mít |

Tổng chương 1: **60.000 SM & TN**. Tổng chương 2: **1.840.000 SM & TN**.
Cộng thêm thưởng theo bước (§6.3 file gốc: mỗi bước = 10% mức nhiệm vụ) và sức mạnh cày quái, mốc cuối chương 1 ≈ **50–70k**, cuối chương 2 ≈ **2 triệu** — khớp §6.1.

---

## Chương 1 — Ngày ký ức vỡ (NV 0 → 7)

### NV 0 — Người duy nhất còn nhớ
**Chương** 1 — Ngày ký ức vỡ | **Mốc SM khi xong** ~2.000 | **Nhiệm vụ kế** NV 1 — Bài học của ông

*Người chơi tỉnh dậy ngoài vách núi, đầu đau như búa bổ, trong ngực có thứ gì đó ấm và sáng. Về đến nhà, ông đang ngồi đó — nhưng ông gọi người chơi bằng một cái tên khác. Và ông nói cái tên đó bằng giọng rất thương.*

| # | Tên bước (hiển thị cho người chơi) | Kiểu | Điều kiện server kiểm tra | Số lượng | Map (id + tên) | NPC (id + tên) | Hằng số | Thưởng bước |
|---|---|---|---|---|---|---|---|---|
| 0 | Đi về nhà | A6 | `checkDoneTaskGoToMap`: `zone.map.mapId ∈ {21, 22, 23}` (= `MAP_NHA`, tức `21 + gender`) | 1 | 21 Nhà Gôhan / 22 Nhà Moori / 23 Nhà Broly | — | `TASK_0_0` | 200 SM & TN |
| 1 | Lấy đồ trong rương | A8 | `checkDoneTaskGetItemBox`: `UseItem` nhánh `ITEM_BOX_TO_BODY_OR_BAG` chuyển ≥ 1 item từ rương ra hành trang/cơ thể | 1 | 21/22/23 Nhà | 3 Rương đồ | `TASK_0_1` | 200 SM & TN |
| 2 | Nói chuyện với %2 | A3 | `checkDoneTaskTalkNpc`: `npc.tempId == 0 Ông Gôhan / 2 Ông Moori / 1 Ông Paragus` (`NPC_NHA`) | 1 | 21/22/23 Nhà | -2 → 0 / 2 / 1 | `TASK_0_2` | 200 SM & TN |
| 3 | Xem cây đậu thần | A9 | `checkDoneTaskConfirmMenuNpc`: `npc.tempId == 4` (Đậu thần) và người chơi bấm chọn 1 mục trong menu cây | 1 | 21/22/23 Nhà | 4 Đậu thần | `TASK_0_3` | 200 SM & TN |
| 4 | Ăn một hạt Đậu thần cấp 1 | A12 | `checkDoneTaskUseItem`: `item.template.id == 13` (Đậu thần cấp 1) | 1 | 21/22/23 Nhà | — | `TASK_0_4` | 200 SM & TN |

- **Lời thoại**
  - *Bước 0 — tutorial hệ thống (không NPC):* "Đầu ngươi đau như vỡ ra. Có thứ gì đó ấm trong lồng ngực. Về nhà đi."
  - *Bước 2 — NPC 0 / 2 / 1 (ông Gôhan / ông Moori / ông Paragus):* "Con về rồi à... Kairo? Ăn cơm chưa, Kairo?"
  - *Bước 2 — NPC 0 / 2 / 1:* "Sao con nhìn ta lạ vậy? Ta gọi đúng tên con mà, đúng không?"
  - *Bước 2 — NPC 0 / 2 / 1:* "Thôi, chắc ta già rồi. Ra ăn hạt đậu cho khỏe đi con."
  - *Bước 4 — NPC 0 / 2 / 1:* "Ngoan. Đừng để ai lấy mất cái gì của con, nghe chưa."

- **Mô tả nhiệm vụ (cột `detail`)**: "Về nhà ở %5, mở rương lấy đồ, gặp %2 và ăn một hạt đậu thần. Thưởng 2.000 sức mạnh, 2.000 tiềm năng."

- **Thưởng khi hoàn thành**: 2.000 SM · 2.000 TN · **5 × 13 Đậu thần cấp 1**, **1 × 193 Gói 10 viên Capsule**.
- **Mở khóa**: hành trang, rương đồ (NPC 3), cây đậu thần (NPC 4). Giữ nguyên tutorial đăng nhập trong `Controller` cho `getIdTask == TASK_0_0`.
- **Ghi chú triển khai**:
  - `TaskService.checkDoneTaskGoToMap`: đổi nhánh `21/22/23` từ `TASK_0_1` cũ sang **`TASK_0_0`**; **xóa** nhánh `39/40/41 (x ≥ 635)` (tuyến mới không dùng bước đi tới mép vách núi).
  - `TaskService.checkDoneTaskUseItem`: `switch` đang **rỗng** — thêm `case 13: doneTask(player, ConstTask.TASK_0_4)`.
  - Không cần sửa dữ liệu map: rương (3), đậu thần (4) và ông đã có sẵn trong `map_template` của 21/22/23.

---

### NV 1 — Bài học của ông
**Chương** 1 | **Mốc SM khi xong** ~5.500 | **Nhiệm vụ kế** NV 2 — Vết nứt đầu tiên

*Ông không nhớ tên người chơi, nhưng tay ông vẫn nhớ cách dạy đánh. Ông lôi ra mấy con mộc nhân cũ ở đầu làng và bảo: "Đánh đi. Hồi nhỏ con thích lắm." Người chơi học lại từ đầu thứ mà cả thế giới đang quên dần.*

| # | Tên bước | Kiểu | Điều kiện server kiểm tra | Số lượng | Map (id + tên) | NPC (id + tên) | Hằng số | Thưởng bước |
|---|---|---|---|---|---|---|---|---|
| 0 | Đập vỡ mộc nhân tập luyện | A1 | `checkDoneTaskKillMob`: `mob.tempId == 0` (Mộc nhân, HP 20) | 10 | 0 Làng Aru / 7 Làng Mori / 14 Làng Kakarot (`MAP_LANG`) | — | `TASK_1_0` | 300 SM & TN |
| 1 | Về khoe với %2 | A3 | `checkDoneTaskTalkNpc`: `npc.tempId ∈ {0, 2, 1}` (`NPC_NHA`) | 1 | 21/22/23 Nhà | -2 → 0 / 2 / 1 | `TASK_1_1` | 300 SM & TN |
| 2 | Cộng điểm tiềm năng lần đầu | A7 | `checkDoneTaskUseTiemNang`: gọi từ `NPoint.doUseTiemNang` (cộng bất kỳ chỉ số nào) | 1 | mọi map | — | `TASK_1_2` | 300 SM & TN |

- **Lời thoại**
  - *Bước 0 — NPC 0 / 2 / 1:* "Mấy con mộc nhân ngoài làng còn đó. Đánh vài cái cho ấm người đi con."
  - *Bước 1 — NPC 0 / 2 / 1:* "Giỏi lắm. Hồi nhỏ con cũng đấm y hệt vậy... mà khoan, hồi nhỏ nào nhỉ?"
  - *Bước 1 — NPC 0 / 2 / 1:* "Ta quên mất rồi. Dạo này ta quên nhiều thứ lắm, con đừng cười ta."
  - *Bước 2 — tutorial hệ thống:* "Mở bảng chỉ số, dùng tiềm năng để mạnh lên. Ông không dạy con mãi được."

- **Mô tả nhiệm vụ (cột `detail`)**: "Đánh 10 mộc nhân ở %1, về gặp %2 rồi cộng điểm tiềm năng. Thưởng 3.000 sức mạnh, 3.000 tiềm năng."

- **Thưởng khi hoàn thành**: 3.000 SM · 3.000 TN · **1 × 12 Rada cấp 1**.
- **Mở khóa**: bảng tiềm năng; map **1 Đồi hoa cúc / 8 Đồi nấm tím / 15 Đồi hoang** mở tại mốc **`TASK_2_0`** (§8 file gốc).
- **Ghi chú triển khai**:
  - `ChangeMapService.checkMapCanJoin`: map 1/8/15 đổi ngưỡng `TASK_1_0` → **`TASK_2_0`**.
  - `checkDoneTaskKillMob`: thêm nhánh `tempId == 0 → TASK_1_0`.
  - `checkDoneTaskTalkNpc` (ông): bảng bước mới của NPC 0/2/1 là `{0_2, 1_1, 4_2, 5_2, 6_2, 11_2, 12_2}` — viết lại, **có kiểm tra đúng ông của hành tinh mình** (lỗi cũ: không kiểm tra `gender`).

---

### NV 2 — Vết nứt đầu tiên
**Chương** 1 | **Mốc SM khi xong** ~11.000 | **Nhiệm vụ kế** NV 3 — Cảnh sát vũ trụ Jaco

*Trên đồi, bầu trời rách một đường dài màu trắng đục. Lũ thú quanh đó không hung dữ hơn — chúng chỉ đứng ngơ ngác, như vừa quên mất mình là con gì. Trong đám cỏ cháy có một mảnh vỡ lạnh ngắt, và khi chạm vào nó người chơi nghe thấy giọng nói của chính mình.*

| # | Tên bước | Kiểu | Điều kiện server kiểm tra | Số lượng | Map (id + tên) | NPC | Hằng số | Thưởng bước |
|---|---|---|---|---|---|---|---|---|
| 0 | Lên %3 xem chuyện gì xảy ra | A6 | `checkDoneTaskGoToMap`: `zone.map.mapId ∈ {1, 8, 15}` (`MAP_200`) | 1 | 1 Đồi hoa cúc / 8 Đồi nấm tím / 15 Đồi hoang | — | `TASK_2_0` | 400 SM & TN |
| 1 | Tiêu diệt %4 đang phát điên | A1 | `checkDoneTaskKillMob`: `mob.tempId == 1 Khủng long / 2 Lợn lòi / 3 Quỷ đất` (HP 200, lv2) | 20 | 1/8/15 Đồi | — | `TASK_2_1` | 400 SM & TN |
| 2 | Nhặt Mảnh Vỡ Hư Không | A4 | `checkDoneTaskPickItem`: `itemMap.itemTemplate.id == 2001` (**ITEM MỚI**) | 1 | 1/8/15 Đồi | — | `TASK_2_2` | 400 SM & TN |

- **Lời thoại**
  - *Bước 0 — tutorial hệ thống:* "Trên trời có một vệt trắng. Nó không phải mây. Nó là một vết rách."
  - *Bước 1 — tutorial hệ thống:* "Lũ thú không tấn công vì đói. Chúng tấn công vì không còn nhớ ngươi là ai."
  - *Bước 2 — tutorial hệ thống:* "Mảnh vỡ lạnh như băng. Trong đó có tiếng ngươi gọi tên một người ngươi không quen."

- **Mô tả nhiệm vụ (cột `detail`)**: "Lên %3, diệt 20 %4 và nhặt Mảnh Vỡ Hư Không rơi ra từ chúng. Thưởng 4.000 sức mạnh, 4.000 tiềm năng."

- **Thưởng khi hoàn thành**: 4.000 SM · 4.000 TN · **10 × 13 Đậu thần cấp 1**.
- **Mở khóa**: map **42 Vách núi Aru / 43 Vách núi Moori / 44 Vách núi Kakarot** tại mốc **`TASK_3_0`**.
- **Ghi chú triển khai**:
  - **ITEM MỚI `2001` "Mảnh Vỡ Hư Không"** — TYPE 11 (vật phẩm nhiệm vụ), gender 3, level 1, `power_require` 0, part -1, **icon mượn `1421`** (của item 225 Mảnh đá vụn).
    *Phương án không đụng data client:* dùng luôn item **225 Mảnh đá vụn** thay cho 2001.
  - `Mob.dropItemTask`: bỏ nhánh rơi **73 Đùi gà**; thêm: nếu `getIdTask(player) == ConstTask.TASK_2_2` và `mob.tempId ∈ {1,2,3}` → `dropItemMapForMe(item 2001)` (tỉ lệ 100%, tối đa 1 cái trên đất).
  - `Zone.getItemMapsForPlayer`: item 2001 chỉ hiển thị cho chủ nhân; **bỏ** nhánh cũ của item 74 Đùi gà nướng và 78 Đứa bé.
  - `checkDoneTaskPickItem`: `case 2001 → TASK_2_2`; **xóa** `case 73`, `case 78`.
  - `ChangeMapService`: map 42/43/44 đổi `TASK_2_0` → **`TASK_3_0`**.

---

### NV 3 — Cảnh sát vũ trụ Jaco
**Chương** 1 | **Mốc SM khi xong** ~17.000 | **Nhiệm vụ kế** NV 4 — Thứ bò ra từ vết nứt

*Một con tàu nhỏ xíu rơi xuống vách núi, và bước ra là Jaco — cảnh sát vũ trụ, người đang đi đếm những hành tinh bị mất trí nhớ. Hắn quét máy đo qua người chơi rồi sững lại: chỉ số sức mạnh thì thấp, nhưng có một tín hiệu mà máy của hắn chưa từng đọc được. Jaco quyết định ở lại.*

| # | Tên bước | Kiểu | Điều kiện server kiểm tra | Số lượng | Map (id + tên) | NPC (id + tên) | Hằng số | Thưởng bước |
|---|---|---|---|---|---|---|---|---|
| 0 | Gặp Jaco ở %5 | A3 | `checkDoneTaskTalkNpc`: `npc.tempId == 63` (Jaco) và `player.zone.map.mapId ∈ {42, 43, 44}` | 1 | 42 Vách núi Aru / 43 Vách núi Moori / 44 Vách núi Kakarot | 63 Jaco | `TASK_3_0` | 500 SM & TN |
| 1 | Cho Jaco xem ngươi đánh nhau | A1 | `checkDoneTaskKillMob`: `mob.tempId ∈ {1, 2, 3}` (Khủng long / Lợn lòi / Quỷ đất) | 20 | 1/8/15 Đồi | — | `TASK_3_1` | 500 SM & TN |
| 2 | Nâng sức đánh gốc lên 30 | A11 | `checkDoneTaskNangCS`: gọi từ `NPoint.increasePoint(type 2)`, điều kiện `nPoint.dameg >= 30` | 1 | mọi map | — | `TASK_3_2` | 500 SM & TN |

> Sức đánh gốc khởi điểm: Trái Đất/Namếc **10**, Xayda **15** → mốc 30 tốn khoảng 25.000 TN (Xayda rẻ hơn). Con số này **chỉnh được**; nếu thấy chặn quá nặng, hạ xuống 25.

- **Lời thoại**
  - *Bước 0 — NPC 63 Jaco:* "Đứng im! Cảnh sát vũ trụ đây. Ngươi là người thứ tư hôm nay còn nhớ tên mình."
  - *Bước 0 — NPC 63 Jaco:* "Máy của ta đọc ngươi ra ba con số. Hai con số bình thường. Con số thứ ba thì... không nên tồn tại."
  - *Bước 1 — NPC 63 Jaco:* "Đánh đi, ta đo. Đừng làm bộ khiêm tốn, ta ghét mấy đứa khiêm tốn."
  - *Bước 2 — NPC 63 Jaco:* "Yếu quá. Cộng tiềm năng vào sức đánh đi, không thì ngươi chết trước khi ta viết xong báo cáo."
  - *Bước 2 — NPC 63 Jaco:* "Nghe đây: thứ đang ăn hành tinh này không giết ai cả. Nó chỉ lấy đi. Thế mới sợ."

- **Mô tả nhiệm vụ (cột `detail`)**: "Gặp Jaco ở %5, diệt 20 %4 cho hắn đo sức mạnh rồi nâng sức đánh gốc lên 30. Thưởng 5.000 sức mạnh, 5.000 tiềm năng."

- **Thưởng khi hoàn thành**: 5.000 SM · 5.000 TN · **1 bộ trang bị cấp 1 theo hành tinh**: TĐ `0 Áo vải 3 lỗ + 6 Quần vải đen + 21 Găng vải đen + 27 Giầy nhựa`; NM `1 Áo sợi len + 7 Quần sợi len + 22 Găng sợi len + 28 Giầy sợi len`; XD `2 Áo vải thô + 8 Quần vải thô + 23 Găng vải thô + 29 Giầy vải thô`.
- **Mở khóa**: Máy đo sức mạnh (mob 117 tại 42/43/44), shop làng (NPC 7 Bunma / 8 Dende / 9 Appule), map **2 Thung lũng tre / 9 Thị trấn Moori / 16 Làng Plant** tại mốc **`TASK_4_0`**.
- **Ghi chú triển khai**:
  - **Dữ liệu**: thêm **NPC 63 Jaco** vào `map_template.npcs` của map **42, 43, 44** (Jaco hiện chỉ có ở map 24 và 139).
  - Cần **class `npc_list/Jaco.java`** (hiện chưa có; NPC 63 đang rơi vào nhánh `default` của `NpcFactory.createNPC`). Class này phải gọi `TaskService.gI().checkDoneTaskTalkNpc(player, this)` trong `openBaseMenu` và trả về sớm nếu trả `true`.
  - `checkDoneTaskTalkNpc`: bảng của NPC 63 = `{3_0, 7_2, 15_2}` (kèm kiểm tra map cho bước 3_0).
  - `checkDoneTaskNangCS`: đổi điều kiện cũ (`dameg >= 35.000 → TASK_27_0`) thành bảng nhiều mốc; mốc đầu `dameg >= 30 → TASK_3_2`.
  - `ChangeMapService`: map 2/9/16 đổi `TASK_3_0` → **`TASK_4_0`**.

---

### NV 4 — Thứ bò ra từ vết nứt
**Chương** 1 | **Mốc SM khi xong** ~25.000 | **Nhiệm vụ kế** NV 5 — Ký ức của ông

*Vết nứt bắt đầu nhả ra thứ gì đó. Lũ thú mẹ trong thung lũng biến dạng — mắt trắng dã, không còn bảo vệ con của chúng nữa. Jaco gọi đó là "giai đoạn hai": khi một sinh vật quên hết, nó chỉ còn lại bản năng tấn công.*

| # | Tên bước | Kiểu | Điều kiện server kiểm tra | Số lượng | Map (id + tên) | NPC | Hằng số | Thưởng bước |
|---|---|---|---|---|---|---|---|---|
| 0 | Dọn đường vào %6 | A1 | `checkDoneTaskKillMob`: `mob.tempId ∈ {1, 2, 3}` (Khủng long / Lợn lòi / Quỷ đất, lv2) | 12 | 2 Thung lũng tre / 9 Thị trấn Moori / 16 Làng Plant | — | `TASK_4_0` | 600 SM & TN |
| 1 | Hạ lũ quái mẹ biến dạng | A1 | `checkDoneTaskKillMob`: `mob.tempId == 4 Khủng long mẹ / 5 Lợn lòi mẹ / 6 Quỷ đất mẹ` (HP 500, lv3) | 15 | 2/9/16 | — | `TASK_4_1` | 600 SM & TN |
| 2 | Kể lại cho %2 | A3 | `checkDoneTaskTalkNpc`: `npc.tempId ∈ {0, 2, 1}` đúng theo `gender` | 1 | 21/22/23 Nhà | -2 → 0 / 2 / 1 | `TASK_4_2` | 600 SM & TN |

- **Lời thoại**
  - *Bước 0 — NPC 63 Jaco:* "Chúng không đói. Chúng không sợ. Chúng chỉ còn mỗi việc đánh. Đó mới là thứ đáng sợ."
  - *Bước 1 — tutorial hệ thống:* "Con mẹ đi ngang qua ổ của chính nó mà không dừng lại. Nó quên mất bầy con."
  - *Bước 2 — NPC 0 / 2 / 1:* "Con đi đâu về mà mặt mũi thế kia... à, con là ai nhỉ? Cháu nhà ai bên xóm à?"
  - *Bước 2 — NPC 0 / 2 / 1:* "Thôi vào nhà đi, trời sắp tối. Nhà ta còn thừa một chỗ ngồi mà ta không nhớ của ai."

- **Mô tả nhiệm vụ (cột `detail`)**: "Diệt 12 %4 và 15 quái mẹ biến dạng ở %6, rồi về kể cho %2. Thưởng 6.000 sức mạnh, 6.000 tiềm năng."

- **Thưởng khi hoàn thành**: 6.000 SM · 6.000 TN · **2 × 193 Gói 10 viên Capsule**.
- **Mở khóa**: map **3 Rừng nấm / 11 Thung lũng Maima / 17 Rừng nguyên sinh** tại mốc **`TASK_5_0`** (§8: mốc cũ `TASK_7_0`).
- **Ghi chú triển khai**:
  - `ChangeMapService`: map 3/11/17 đổi `TASK_7_0` → **`TASK_5_0`**.
  - Tuyến cũ tách nhiệm vụ 4/5/6 theo hành tinh trong `sendNextTaskMain` (`id == 3 → gender + 4`). Tuyến mới **bỏ hoàn toàn nhánh theo hành tinh ở chương 1**: `id 3 → 4`, `id 4 → 5`. Khác biệt hành tinh giờ chỉ nằm ở `transformMapId` / `transformNpcId` / `transformName`.
  - Hai bước A1 liên tiếp là trường hợp duy nhất được phép trong chương 1 (§4.3: "không quá 2 bước giết quái liên tiếp").

---

### NV 5 — Ký ức của ông
**Chương** 1 | **Mốc SM khi xong** ~34.000 | **Nhiệm vụ kế** NV 6 — Người thu gom

*Jaco nói ký ức bị hút đi vẫn để lại "mỏ neo": một đồ vật mà người ta từng yêu. Trong rừng, dưới bụng một con thằn lằn bay, người chơi tìm thấy chiếc nhẫn cũ ông vẫn đeo — thứ ông đã đánh rơi từ hôm vết nứt mở ra. Ông cầm nó, và trong đúng mười giây, ông nhớ.*

| # | Tên bước | Kiểu | Điều kiện server kiểm tra | Số lượng | Map (id + tên) | NPC (id + tên) | Hằng số | Thưởng bước |
|---|---|---|---|---|---|---|---|---|
| 0 | Tìm Kỷ Vật Của Ông trong %13 | A4 | `checkDoneTaskPickItem`: `itemMap.itemTemplate.id == 2002` (**ITEM MỚI**), rơi từ `mob.tempId == 7 Thằn lằn bay / 8 Phi long / 9 Quỷ bay` | 1 | 3 Rừng nấm / 11 Thung lũng Maima / 17 Rừng nguyên sinh | — | `TASK_5_0` | 800 SM & TN |
| 1 | Lau sạch kỷ vật | A12 | `checkDoneTaskUseItem`: `item.template.id == 2002` | 1 | mọi map | — | `TASK_5_1` | 800 SM & TN |
| 2 | Đưa kỷ vật cho %2 | A3 | `checkDoneTaskTalkNpc`: `npc.tempId ∈ {0, 2, 1}` đúng `gender`, và túi có item 2002 → trừ 1 | 1 | 21/22/23 Nhà | -2 → 0 / 2 / 1 | `TASK_5_2` | 800 SM & TN |

- **Lời thoại**
  - *Bước 0 — NPC 63 Jaco:* "Ký ức bị rút đi luôn để lại một cái mỏ neo. Tìm món đồ ông ngươi thương nhất."
  - *Bước 1 — tutorial hệ thống:* "Chiếc nhẫn mòn vẹt. Bên trong khắc hai chữ đã mờ, một trong hai chữ là tên ngươi."
  - *Bước 2 — NPC 0 / 2 / 1:* "Cái này... cái này của ta. Ta đánh rơi hôm trời rách ra làm đôi."
  - *Bước 2 — NPC 0 / 2 / 1:* "Con ơi. Ta nhớ ra rồi. Ta nhớ ra con rồi. Đừng đi đâu hết, ở đây với ta."
  - *Bước 2 — NPC 0 / 2 / 1:* "...Xin lỗi, nãy giờ ta nói gì thế? Cháu là ai mà đứng trong nhà ta?"

- **Mô tả nhiệm vụ (cột `detail`)**: "Tìm Kỷ Vật Của Ông rơi từ %9 trong %13, lau sạch rồi đưa cho %2. Thưởng 8.000 sức mạnh, 8.000 tiềm năng."

- **Thưởng khi hoàn thành**: 8.000 SM · 8.000 TN · **1 × 57 Rada cấp 2**, **10 × 13 Đậu thần cấp 1**.
- **Mở khóa**: **túi lưng** (`Player.getFlagBag` — mốc cũ `TASK_3_2`, mốc mới **`TASK_5_2`**); map 4 Rừng xương / 12 Vực maima / 18 Rừng thông Xayda (vốn không khóa, giữ nguyên).
- **Ghi chú triển khai**:
  - **ITEM MỚI `2002` "Kỷ Vật Của Ông"** — TYPE 11, gender 3, level 1, part -1, **icon mượn `9067`** (của item 992 Nhẫn thời không sai lệch). *Phương án thay thế:* dùng item **992** sẵn có.
  - `Mob.dropItemTask`: nếu `getIdTask(player) == TASK_5_0` và `mob.tempId ∈ {7,8,9}` → `dropItemMapForMe(item 2002)`.
  - `checkDoneTaskPickItem`: `case 2002 → TASK_5_0`. `checkDoneTaskUseItem`: `case 2002 → TASK_5_1` (**không xóa item** ở bước này, item bị trừ ở bước 2).
  - Trong `switch` của `doneTask`, `case TASK_5_2`: `InventoryService.subQuantityItemsBag(player, item 2002, 1)`.
  - `Player.getFlagBag`: đổi điều kiện `getIdTask == TASK_3_2` → `getIdTask >= TASK_5_2`.

---

### NV 6 — Người thu gom
**Chương** 1 | **Mốc SM khi xong** ~46.000 | **Nhiệm vụ kế** NV 7 — Chạy khỏi vết nứt

*Trong rừng xương có một kẻ mặc áo choàng đi từ xác này sang xác khác, cúi xuống và "hút" thứ gì đó trong suốt ra khỏi chúng. Hắn không nói tên. Hắn chỉ nói: "Tôi làm việc. Ông chủ tôi trả công bằng sự yên tĩnh." Đây là lần đầu người chơi thấy bàn tay của Heart.*

| # | Tên bước | Kiểu | Điều kiện server kiểm tra | Số lượng | Map (id + tên) | NPC (id + tên) | Hằng số | Thưởng bước |
|---|---|---|---|---|---|---|---|---|
| 0 | Lần theo tiếng rít trong rừng | A1 | `checkDoneTaskKillMob`: `mob.tempId == 7 Thằn lằn bay / 8 Phi long / 9 Quỷ bay` (HP 600, lv4) | 15 | 4 Rừng xương / 12 Vực maima / 18 Rừng thông Xayda | — | `TASK_6_0` | 1.200 SM & TN |
| 1 | Hạ Kẻ Thu Gom | A2 | `checkDoneTaskKillBoss`: `boss.id == -2000` (**BOSS MỚI** "Kẻ Thu Gom") | 1 | 4 / 12 / 18 | — | `TASK_6_1` | 1.200 SM & TN |
| 2 | Báo lại cho %2 | A3 | `checkDoneTaskTalkNpc`: `npc.tempId ∈ {0, 2, 1}` đúng `gender` | 1 | 21/22/23 Nhà | -2 → 0 / 2 / 1 | `TASK_6_2` | 1.200 SM & TN |

- **Lời thoại**
  - *Bước 1 — Boss -2000 (textS):* "Đừng phiền. Tôi chỉ đang dọn dẹp. Mấy thứ này chúng nó có giữ cũng chẳng để làm gì."
  - *Bước 1 — Boss -2000 (textM):* "Ngươi còn nhớ mẹ ngươi tên gì không? Thấy chưa, tôi giúp ngươi nhẹ hơn thôi mà."
  - *Bước 1 — Boss -2000 (textE):* "Ông chủ... sẽ tự đến lấy... phần của ngài..."
  - *Bước 2 — NPC 0 / 2 / 1:* "Áo choàng xám hả? Hôm qua nó vào nhà mình. Ta mời nó uống nước. Rồi ta quên tên ta."

- **Mô tả nhiệm vụ (cột `detail`)**: "Diệt 15 %9 ở rừng xương, hạ Kẻ Thu Gom rồi về báo cho %2. Thưởng 12.000 sức mạnh, 12.000 tiềm năng."

- **Thưởng khi hoàn thành**: 12.000 SM · 12.000 TN · **1 × 295 Gói 30 đậu thần cấp 3**.
- **Mở khóa**: đường tới **24 / 25 / 26 Trạm tàu vũ trụ** (mốc thực thi là **`TASK_7_1`** theo §8).
- **Ghi chú triển khai**:
  - **BOSS MỚI `-2000` "Kẻ Thu Gom"** khai báo trong `BossesData` theo mẫu `BossData`:
    `name = "Kẻ Thu Gom"`, `gender = 0`, `outfit = {144, 145, 146, -1, -1, -1}` (mượn tạo hình NPC 26 Độc Nhãn), `dame = 400`, `hp = {80_000}`, `mapJoin = {4, 12, 18}`, `skillTemp = {{0, 3}}` (Chiêu đấm Dragon cấp 3), `secondsRest = REST_2_M`, `typeAppear = DEFAULT_APPEAR`, textS/textM/textE như trên.
    Tạo trong `loadBoss()` ×1 (mỗi hành tinh 1 con vì `mapJoin` phủ cả 3 map).
  - `checkDoneTaskKillBoss`: `case -2000 → TASK_6_1`.
  - Boss này chỉ tồn tại để phục vụ nhiệm vụ nên đặt phần thưởng rơi đồ **thấp** (mẫu A rút gọn: 50.000 vàng), tránh thành chỗ cày vàng.

---

### NV 7 — Chạy khỏi vết nứt
**Chương** 1 — *cao trào nhỏ đầu tiên* | **Mốc SM khi xong** ~66.000 | **Nhiệm vụ kế** NV 8 — Máy dò ký ức

*Vết nứt không nhả quái nữa — nó bắt đầu nuốt. Cả khu rừng bị kéo về phía đường rách trắng, và những cái vòi trong suốt thò ra bám vào mặt đất. Người chơi có **3 phút** để chặt đứt chúng và chạy ra trạm tàu vũ trụ trước khi map đóng lại.*

| # | Tên bước | Kiểu | Điều kiện server kiểm tra | Số lượng | Map (id + tên) | NPC (id + tên) | Hằng số | Thưởng bước |
|---|---|---|---|---|---|---|---|---|
| 0 | **Chặt đứt 10 Vòi Hư Không trong 3 phút** | **B12** | `checkDoneTaskKillMob` + đồng hồ: `mob.tempId == 10 Thằn lằn mẹ / 11 Phi long mẹ / 12 Quỷ bay mẹ`, tính từ `taskMain.lastTime`, giới hạn **180.000 ms** | 10 | 4 / 12 / 18 | — | `TASK_7_0` | 2.000 SM & TN |
| 1 | Chạy tới %7 ở Trạm tàu vũ trụ | A6 | `checkDoneTaskGoToMap`: `zone.map.mapId ∈ {24, 25, 26}` (`MAP_TTVT`) | 1 | 24 / 25 / 26 Trạm tàu vũ trụ | — | `TASK_7_1` | 2.000 SM & TN |
| 2 | Nói chuyện với Jaco | A3 | `checkDoneTaskTalkNpc`: `npc.tempId == 63` và `player.zone.map.mapId ∈ {24, 25, 26}` | 1 | 24 / 25 / 26 | 63 Jaco | `TASK_7_2` | 2.000 SM & TN |

**Luật của bước B12 (bước đếm giờ duy nhất của chương 1):**

| Tình huống | Server xử lý |
|---|---|
| Vừa sang bước `TASK_7_0` | `taskMain.lastTime = System.currentTimeMillis()`; gửi message 43 + tutorial "Còn 3:00" |
| Mỗi 30 giây | `Player.update` bắn thông báo còn lại ("Còn 2:30", "Còn 2:00", …) |
| Giết đủ 10 con trước khi hết giờ | Bước xong bình thường qua `addDoneSubTask`; `lastTime = 0` |
| **Hết 180 giây mà chưa đủ** | `subTasks[0].count = 0`; `lastTime = now` (đếm lại từ đầu **ngay lập tức**); thông báo "Vết nứt khép lại rồi lại mở ra. Làm lại từ đầu!" — **không** trừ đồ, **không** đẩy người chơi ra khỏi map |
| Người chơi chết / thoát game / đổi map giữa chừng | Đồng hồ **vẫn chạy**; khi vào lại, nếu đã quá giờ thì áp dụng đúng luật "hết giờ" ở trên (reset count, đếm lại) |
| Người chơi muốn chủ động làm lại | Rời map 4/12/18 rồi vào lại → `checkDoneTaskGoToMap` gọi `resetTimedSubTask` đặt `count = 0`, `lastTime = now` |

> Người chơi **không thể kẹt vĩnh viễn**: bước luôn tự reset và cho thử lại, không giới hạn số lần.

- **Lời thoại**
  - *Bước 0 — NPC 63 Jaco:* "Nó không nhả quái nữa — nó đang hút! Chặt hết vòi đi, ta giữ tàu. Ba phút thôi!"
  - *Bước 0 (hết giờ) — NPC 63 Jaco:* "Trễ rồi! Nó mọc lại hết rồi. Hít thở đi, mình làm lại lần nữa."
  - *Bước 1 — NPC 63 Jaco:* "Chạy! Đừng quay đầu nhìn, nhìn là ngươi quên mất mình đang chạy vì cái gì."
  - *Bước 2 — NPC 63 Jaco:* "Cái mảnh ngươi nhặt được... nó đang phát sáng. Đó là một Mảnh Ký Ức. Có bảy cái."
  - *Bước 2 — NPC 63 Jaco:* "Giữ nó cho chặt. Chừng nào ngươi còn giữ nó, ngươi còn là ngươi."

- **Mô tả nhiệm vụ (cột `detail`)**: "Chặt 10 Vòi Hư Không trong 3 phút rồi chạy tới Trạm tàu vũ trụ gặp Jaco. Thưởng 20.000 sức mạnh, 20.000 tiềm năng và Mảnh Ký Ức #1."

- **Thưởng khi hoàn thành**: 20.000 SM · 20.000 TN · **1 × 2010 Mảnh Ký Ức #1 (ITEM MỚI)** · **75 × 193 Gói 10 viên Capsule** (giữ lại truyền thống tuyến cũ TASK_7_2 để người chơi có phương tiện đi lại).
- **Mở khóa**: tàu vũ trụ (menu của NPC 10 Dr. Brief / 11 Cargo / 12 Cui), map **24/25/26** tại **`TASK_7_1`**, map **84 Siêu Thị** tại **`TASK_8_0`**.
- **Ghi chú triển khai**:
  - **ITEM MỚI `2010` "Mảnh Ký Ức #1"** — TYPE 11, gender 3, level 1, part -1, **icon mượn `419`** (của item 14 Ngọc Rồng 1 sao). *Phương án thay thế (§10 file gốc):* dùng thẳng item **14 Ngọc Rồng 1 sao**.
  - **Trigger B12 (mới)** — thêm vào `TaskService`:
    - `startTimedSubTask(Player, long millis)`: gán `taskMain.lastTime = now`, gửi message 43.
    - `updateTimedSubTask(Player)`: gọi trong `Player.update()` (đã có sẵn nhịp gọi `sendUpdateCountSubTask`), so `now - lastTime > limit` → reset count + `lastTime`, `Service.sendThongBao`.
    - Bảng cấu hình `Map<Integer /*idTaskCustom*/, Long /*millis*/>`; chương 1 chỉ có 1 dòng: `TASK_7_0 → 180_000`.
    - `lastTime` đã có sẵn trong `TaskMain` và đã được `PlayerDAO` lưu vào `data_task` phần tử thứ 4 → **không cần đổi schema**.
  - `checkDoneTaskGoToMap`: `24/25/26 → TASK_7_1`; đồng thời khi vào 4/12/18 mà đang ở `TASK_7_0` → `startTimedSubTask`.
  - `ChangeMapService`: map 24/25/26 đổi `TASK_4_0` → **`TASK_7_1`**; thêm map **84** ngưỡng **`TASK_8_0`**.
  - `npc_list/DrDrief`, `Cargo`, `Cui`: bỏ nhánh thoại "Hãy lên đường cứu đứa bé nhà tôi" (nội dung tuyến cũ), mở menu tàu vũ trụ từ `TASK_7_2` trở đi.

---

## Chương 2 — Kẻ trộm ký ức (NV 8 → 15)

### NV 8 — Máy dò ký ức
**Chương** 2 — Kẻ trộm ký ức | **Mốc SM khi xong** ~120.000 | **Nhiệm vụ kế** NV 9 — Chuyến bay đầu tiên

*Jaco đưa người chơi tới Siêu Thị tìm Bunma — người duy nhất trong vũ trụ này có thể chế ra thứ đo được cái không đo được. Bunma nhìn Mảnh Ký Ức đúng ba giây rồi tuyên bố: "Được, tôi làm được. Nhưng tôi cần một cái rada và ba cái lõi cảm biến mà lũ quái vừa nuốt mất."*

| # | Tên bước | Kiểu | Điều kiện server kiểm tra | Số lượng | Map (id + tên) | NPC (id + tên) | Hằng số | Thưởng bước |
|---|---|---|---|---|---|---|---|---|
| 0 | Gặp Bunma ở Siêu Thị | A3 | `checkDoneTaskTalkNpc`: `npc.tempId == 7` (Bunma) và `player.zone.map.mapId == 84` | 1 | 84 Siêu Thị | 7 Bunma | `TASK_8_0` | 5.000 SM & TN |
| 1 | Mua 1 Rada cấp 1 ở shop | **B4** | `checkDoneTaskBuyItem` (mới): mua thành công `itemTemplate.id == 12` (Rada cấp 1) ở shop `1 BUNMA / 2 DENDE / 3 APPULE` | 1 | 84 Siêu Thị | 7 Bunma / 8 Dende / 9 Appule | `TASK_8_1` | 5.000 SM & TN |
| 2 | Moi lõi cảm biến từ lũ quái mẹ | A1 | `checkDoneTaskKillMob`: `mob.tempId == 10 Thằn lằn mẹ / 11 Phi long mẹ / 12 Quỷ bay mẹ` | 25 | 4 Rừng xương / 12 Vực maima / 18 Rừng thông Xayda | — | `TASK_8_2` | 5.000 SM & TN |

- **Lời thoại**
  - *Bước 0 — NPC 7 Bunma:* "Đưa đây xem nào. Ối! Nó lạnh mà lại ấm. Tôi ghét những thứ vừa lạnh vừa ấm."
  - *Bước 0 — NPC 7 Bunma:* "Được rồi, tôi chế cho cậu cái máy dò. Nhưng tôi không làm không công đâu nhé."
  - *Bước 1 — NPC 7 Bunma:* "Ra quầy mua cho tôi một cái rada cấp 1. Rẻ thôi. Tôi cần cái ăng-ten của nó."
  - *Bước 2 — NPC 7 Bunma:* "Lõi cảm biến thì lũ quái mẹ nuốt hết rồi. Mổ bụng chúng ra. Đừng kể ai là tôi bảo."
  - *Bước 2 — NPC 63 Jaco:* "Bà ta nói nhiều nhưng tay thì thật. Ở hành tinh này bà ta là thứ đáng tin nhất."

- **Mô tả nhiệm vụ (cột `detail`)**: "Gặp Bunma ở Siêu Thị, mua 1 Rada cấp 1 và moi 25 lõi cảm biến từ quái mẹ. Thưởng 50.000 sức mạnh, 50.000 tiềm năng và Máy Dò Ký Ức."

- **Thưởng khi hoàn thành**: 50.000 SM · 50.000 TN · **1 × 2003 Máy Dò Ký Ức (ITEM MỚI)** · **1 × 295 Gói 30 đậu thần cấp 3**.
- **Mở khóa**: map **84 Siêu Thị** (`TASK_8_0`), shop siêu thị (shop 1/2/3 Bunma-Dende-Appule, 4 Uron, 6/7/8 Bà Hạt Mít), map **5/13/20** tại **`TASK_9_0`**.
- **Ghi chú triển khai**:
  - **ITEM MỚI `2003` "Máy Dò Ký Ức"** — TYPE 11 (vật phẩm nhiệm vụ, không dùng được), gender 3, level 1, part -1, **icon mượn `1089`** (của item 12 Rada cấp 1). *Phương án thay thế:* item **1822 Rada ngọc rồng**.
  - **Trigger B4 (mới)**: trong `shop/ShopService` (hoặc `services_func/ShopService.buyItem`), sau khi trừ tiền và `addItemBag` thành công → gọi `TaskService.gI().checkDoneTaskBuyItem(player, itemTemplateId, shopId)`. Bảng: `(12, shop ∈ {1,2,3}) → TASK_8_1`; `(bất kỳ, shop == 4 URON) → TASK_14_1`.
  - `ChangeMapService`: thêm map **84** ngưỡng `TASK_8_0`, map **5/13/20** ngưỡng `TASK_9_0` (trước đây hai nhóm này **không khóa**).
  - `npc_list/Bunma.java` phải gọi `checkDoneTaskTalkNpc` trước khi mở shop.

---

### NV 9 — Chuyến bay đầu tiên
**Chương** 2 | **Mốc SM khi xong** ~190.000 | **Nhiệm vụ kế** NV 10 — Bái sư

*Máy dò của Bunma quay tít về một hướng: nơi có người già nhất hành tinh còn tỉnh táo. Người chơi lần đầu rời khỏi khu vực mình lớn lên, bay qua biển, và nhận ra thế giới rộng hơn cái làng rất nhiều — và cũng hỏng nhiều hơn rất nhiều.*

| # | Tên bước | Kiểu | Điều kiện server kiểm tra | Số lượng | Map (id + tên) | NPC (id + tên) | Hằng số | Thưởng bước |
|---|---|---|---|---|---|---|---|---|
| 0 | Bay tới %11 | A6 | `checkDoneTaskGoToMap`: `zone.map.mapId ∈ {5, 13, 20}` (`MAP_QUY_LAO`) | 1 | 5 Đảo Kamê / 13 Đảo Guru / 20 Vách núi đen | — | `TASK_9_0` | 7.000 SM & TN |
| 1 | Dọn sạch %12 quanh nhà sư phụ | A1 | `checkDoneTaskKillMob`: `mob.tempId == 13 Ốc mượn hồn / 14 Ốc sên / 15 Heo Xayda mẹ` (HP 3.000) | 20 | 5 / 13 / 20 | — | `TASK_9_1` | 7.000 SM & TN |
| 2 | Nói chuyện với %10 | A3 | `checkDoneTaskTalkNpc`: `npc.tempId == 13 Quy Lão Kame / 14 Trưởng lão Guru / 15 Vua Vegeta` (`NPC_QUY_LAO`), **đúng theo `gender`** | 1 | 5 / 13 / 20 | -5 → 13 / 14 / 15 | `TASK_9_2` | 7.000 SM & TN |

- **Lời thoại**
  - *Bước 0 — NPC 7 Bunma:* "Máy dò chỉ về hướng đó. Bay đi. Và nhớ là tôi cho mượn, không cho luôn."
  - *Bước 1 — NPC 13 / 14 / 15:* "Lũ này canh cửa nhà ta suốt ba ngày. Chúng quên cả đường về biển rồi."
  - *Bước 2 — NPC 13 / 14 / 15:* "Ngươi mang theo một mảnh của Lõi. Ta đã đợi cái mảnh đó sáu mươi năm."
  - *Bước 2 — NPC 13 / 14 / 15:* "Ta chưa quên gì cả. Đó mới là điều đáng sợ — vì ta biết rõ mình sắp quên."

- **Mô tả nhiệm vụ (cột `detail`)**: "Bay tới %11, diệt 20 %12 và gặp %10. Thưởng 70.000 sức mạnh, 70.000 tiềm năng."

- **Thưởng khi hoàn thành**: 70.000 SM · 70.000 TN · **1 × 58 Rada cấp 3**.
- **Mở khóa**: map **5 Đảo Kamê / 13 Đảo Guru / 20 Vách núi đen** (`TASK_9_0`), menu "Nhiệm vụ" ở NPC sư phụ.
- **Ghi chú triển khai**:
  - `checkDoneTaskGoToMap`: nhánh `5/13/20 → TASK_9_0` (trùng mốc cũ, giữ nguyên số nhưng đổi ý nghĩa nội dung).
  - `checkDoneTaskTalkNpc` cho NPC 13/14/15: **viết lại hoàn toàn**. Lỗi cũ (§8 file gốc) là danh sách `13_0, 22_2, 17_1, …` nằm **ngoài** điều kiện `gender` do đặt ngoặc sai → mọi hành tinh đều hoàn thành ở Quy Lão Kame. Tuyến mới: bảng `{9_2, 10_0}` và **bắt buộc** `npc.tempId == 13 + player.gender`.
  - `npc_list/QuyLaoKame`, `TruongLaoGuru`, `VuaVegeta`: cập nhật menu "Nhiệm vụ".

---

### NV 10 — Bái sư
**Chương** 2 | **Mốc SM khi xong** ~300.000 | **Nhiệm vụ kế** NV 11 — Hạt giống hy vọng

*Sư phụ nhận người chơi làm đệ tử — không phải vì thương, mà vì ông cần một người còn nhớ để truyền lại trước khi chính ông quên. Ông đưa cuốn sách chưởng đầu tiên và nói: "Học đi. Kỹ năng nằm trong tay, tay thì khó quên hơn đầu."*

| # | Tên bước | Kiểu | Điều kiện server kiểm tra | Số lượng | Map (id + tên) | NPC (id + tên) | Hằng số | Thưởng bước |
|---|---|---|---|---|---|---|---|---|
| 0 | Xin %10 nhận làm đệ tử | A3 | `checkDoneTaskTalkNpc`: `npc.tempId == 13 + player.gender` | 1 | 5 Đảo Kamê / 13 Đảo Guru / 20 Vách núi đen | -5 → 13 / 14 / 15 | `TASK_10_0` | 10.000 SM & TN |
| 1 | Học chưởng cấp 1 | **B9** | `checkDoneTaskLearnSkill` (mới): `skillId == 1 Kamejoko / 3 Masenko / 5 Antomic` và `skill.point >= 1` | 1 | 5 / 13 / 20 | 13 / 14 / 15 | `TASK_10_1` | 10.000 SM & TN |
| 2 | Đạt 250.000 sức mạnh | A5 | `checkDoneTaskPower`: gọi từ `NPoint.powerUp`, `power >= 250_000` | 1 | mọi map | — | `TASK_10_2` | 10.000 SM & TN |

> **Lưu ý về con số**: bảng tổng §5 ghi mốc `A5 (40.000)`, nhưng theo §6.1 người chơi đã có ~66.000 SM từ cuối chương 1 → bước sẽ tự hoàn thành ngay lần cày quái đầu tiên. Đề xuất dùng **250.000** để khớp §6.1 (NV 8–11: 100.000 → 500.000). Đây là điểm cần chủ dự án chốt.

- **Lời thoại**
  - *Bước 0 — NPC 13 / 14 / 15:* "Ta không nhận đệ tử vì thương ngươi. Ta nhận vì ta sắp quên, còn ngươi thì chưa."
  - *Bước 0 — NPC 13 / 14 / 15:* "Cầm lấy cuốn này. Đừng để nó trong rương, hãy để nó trong tay."
  - *Bước 1 — NPC 13 / 14 / 15:* "Kỹ năng nằm ở tay. Tay khó quên hơn đầu. Đó là lý do ta bắt ngươi tập chứ không bắt ngươi đọc."
  - *Bước 2 — NPC 13 / 14 / 15:* "Hai trăm năm mươi ngàn. Dưới mức đó thì ngươi chưa đủ sức nghe phần còn lại của câu chuyện."

- **Mô tả nhiệm vụ (cột `detail`)**: "Bái %10 làm sư phụ, học chưởng cấp 1 và đạt 250.000 sức mạnh. Thưởng 100.000 sức mạnh, 100.000 tiềm năng."

- **Thưởng khi hoàn thành**: 100.000 SM · 100.000 TN · **1 × 94 Sách Kamejoko lv1 (TĐ) / 101 Sách Masenko lv1 (NM) / 108 Sách Antomic lv1 (XD)** trao ở **bước 0** (để người chơi có sách mà học ở bước 1), thưởng cuối nhiệm vụ thêm **1 × 66 Sách đấm Dragon lv1 (TĐ) / 79 Sách đấm Demon lv1 (NM) / 87 Sách đấm Galick lv1 (XD)**.
- **Mở khóa**: học kỹ năng, shop sách của sư phụ (shop 25 `QUY_LAO`), NPC 16 Uron ở map 84.
- **Ghi chú triển khai**:
  - Trong `switch` của `doneTask`, `case TASK_10_0`: trao sách theo `gender` (đúng như tuyến cũ làm ở `TASK_10_2`, chỉ dời chỗ).
  - **Trigger B9 (mới)**: trong `services/SkillService` (hàm học/nâng skill) sau khi `skill.point` tăng → `TaskService.gI().checkDoneTaskLearnSkill(player, skill)`. Bảng chương 2: `(skillId ∈ {1,3,5}, point >= 1) → TASK_10_1`.
  - `checkDoneTaskPower`: **xóa sạch** bảng mốc cũ (16.000/40.000/200.000/500.000/550.000/600.000/600M/2 tỷ) và dựng lại; mốc chương 2 duy nhất là `250_000 → TASK_10_2`. Tuyến cũ dùng nhầm mốc 40.000 cho bước cần 500.000 — lỗi này biến mất khi viết lại.
  - `boss/luyen_tap_tu_dong/TauPayPay.injured`: giữ điều kiện `getIdTask == TASK_10_1` (§8) nhưng nội dung nhiệm vụ đã đổi → chỉnh thành `getIdTask >= TASK_10_1` để Tàu Pảy Pảy luôn nhận sát thương thật sau khi bái sư.

---

### NV 11 — Hạt giống hy vọng
**Chương** 2 | **Mốc SM khi xong** ~450.000 | **Nhiệm vụ kế** NV 12 — Bạn đồng hành

*Sư phụ đưa một hạt giống nhỏ xíu: "Cây đậu nhà ngươi sắp chết vì đất quên cách nuôi nó." Người chơi trồng hạt giống ấy vào gốc cây đậu thần cũ trong sân nhà. Lần đầu tiên từ khi vết nứt mở ra, có một thứ trong nhà này lớn lên thay vì mất đi.*

| # | Tên bước | Kiểu | Điều kiện server kiểm tra | Số lượng | Map (id + tên) | NPC (id + tên) | Hằng số | Thưởng bước |
|---|---|---|---|---|---|---|---|---|
| 0 | Thu hoạch 5 hạt đậu thần | **B7** | `checkDoneTaskHarvestPea` (mới): gọi từ `MagicTree.harvestPea` → cộng `n` = số hạt vừa thu | 5 | 21/22/23 Nhà | 4 Đậu thần | `TASK_11_0` | 14.000 SM & TN |
| 1 | Gieo Hạt Giống Hy Vọng | A12 | `checkDoneTaskUseItem`: `item.template.id == 2006` (**ITEM MỚI**) và người chơi đang ở map 21/22/23 | 1 | 21/22/23 Nhà | 4 Đậu thần | `TASK_11_1` | 14.000 SM & TN |
| 2 | Khoe cây mới với %2 | A3 | `checkDoneTaskTalkNpc`: `npc.tempId ∈ {0, 2, 1}` đúng `gender` | 1 | 21/22/23 Nhà | -2 → 0 / 2 / 1 | `TASK_11_2` | 14.000 SM & TN |

- **Lời thoại**
  - *Bước 0 — NPC 13 / 14 / 15:* "Về hái đậu đi. Cây nhà ngươi đang héo vì đất cũng bắt đầu quên cách nuôi nó."
  - *Bước 1 — NPC 13 / 14 / 15:* "Hạt này ta giữ từ thời chưa có vết nứt. Gieo vào gốc cũ, đừng gieo chỗ đất mới."
  - *Bước 2 — NPC 0 / 2 / 1:* "Cây cao hơn hôm qua kìa. Ta không nhớ ai trồng, nhưng ta thấy vui lạ."
  - *Bước 2 — NPC 0 / 2 / 1:* "Cháu ở lại ăn cơm nhé. Nhà ta lúc nào cũng dư một cái bát."

- **Mô tả nhiệm vụ (cột `detail`)**: "Thu hoạch 5 hạt đậu thần, gieo Hạt Giống Hy Vọng vào cây rồi khoe với %2. Thưởng 140.000 sức mạnh, 140.000 tiềm năng."

- **Thưởng khi hoàn thành**: 140.000 SM · 140.000 TN · **1 × 295 Gói 30 đậu thần cấp 3**.
- **Mở khóa**: **cây đậu thần lên cấp 2 miễn phí** (max 7 hạt, đậu cấp 2 hồi +500 HP/KI).
- **Ghi chú triển khai**:
  - **ITEM MỚI `2006` "Hạt Giống Hy Vọng"** — TYPE 27 (dùng được), gender 3, level 1, part -1, **icon mượn `241`** (icon chung của đậu thần). *Phương án thay thế:* item **568 Quả Trứng** (đổi tên hiển thị).
  - Trao item 2006 ở **thưởng bước 0** (`case TASK_11_0` trong `switch` của `doneTask`).
  - **Trigger B7 (mới)**: `npc/MagicTree.harvestPea()` sau `addPeaHarvest` → `TaskService.gI().checkDoneTaskHarvestPea(player, soHat)` (cộng dồn `addDoneSubTask(player, soHat)` chứ không phải 1).
  - `checkDoneTaskUseItem`: `case 2006` → nếu `player.magicTree.level < 2` thì `level = 2`, `isUpgrade = false`, reset `lastTimeHarvest`; rồi `doneTask(TASK_11_1)`; trừ item.
  - Cây đậu cấp 1 ra 1 hạt/60 giây, tối đa 5 hạt → bước 0 mất tối đa ~5 phút nếu cây trống. Nếu muốn bước đầu **ngắn** đúng §4.3, cho `MagicTree` đầy hạt sẵn khi người chơi sang bước `TASK_11_0`.

---

### NV 12 — Bạn đồng hành
**Chương** 2 | **Mốc SM khi xong** ~680.000 | **Nhiệm vụ kế** NV 13 — Không ai đi một mình

*Quả trứng trong sân nhà — thứ mà ông vẫn nói "để dành cho con" mà chẳng ai nhớ là để dành cho ai — nứt ra. Bên trong là một đứa nhỏ nhìn người chơi và nói câu đầu tiên trong đời nó: "Xin hãy thu nhận tao làm đệ tử." Từ hôm nay người chơi có một người để không quên.*

| # | Tên bước | Kiểu | Điều kiện server kiểm tra | Số lượng | Map (id + tên) | NPC (id + tên) | Hằng số | Thưởng bước |
|---|---|---|---|---|---|---|---|---|
| 0 | Nhận đệ tử đầu tiên | **B8** | `checkDoneTaskHavePet` (mới): `player.pet != null` | 1 | 21/22/23 Nhà | 50 Quả trứng | `TASK_12_0` | 20.000 SM & TN |
| 1 | Cùng đệ tử luyện tập | A1 | `checkDoneTaskKillMob`: `mob.tempId == 10 / 11 / 12` (Thằn lằn mẹ / Phi long mẹ / Quỷ bay mẹ) | 25 | 4 Rừng xương / 12 Vực maima / 18 Rừng thông Xayda | — | `TASK_12_1` | 20.000 SM & TN |
| 2 | Giới thiệu đệ tử với %2 | A3 | `checkDoneTaskTalkNpc`: `npc.tempId ∈ {0, 2, 1}` đúng `gender` | 1 | 21/22/23 Nhà | -2 → 0 / 2 / 1 | `TASK_12_2` | 20.000 SM & TN |

- **Lời thoại**
  - *Bước 0 — NPC 50 Quả trứng:* "Quả trứng này ông ngươi để dành cho ai đó. Không ai còn nhớ là cho ai."
  - *Bước 0 — đệ tử (chat tự động):* "Xin hãy thu nhận tao làm đệ tử!"
  - *Bước 1 — NPC 63 Jaco:* "Dạy nó đánh đi. Ngày nào đó ngươi quên, nó sẽ là đứa nhắc ngươi nhớ."
  - *Bước 2 — NPC 0 / 2 / 1:* "Thằng bé giống con hồi nhỏ ghê. À, "con" là ai nhỉ... thôi kệ, ta thích thằng bé."

- **Mô tả nhiệm vụ (cột `detail`)**: "Nhận đệ tử ở quả trứng trong sân, cùng nó diệt 25 quái mẹ rồi về gặp %2. Thưởng 200.000 sức mạnh, 200.000 tiềm năng."

- **Thưởng khi hoàn thành**: 200.000 SM · 200.000 TN · **1 × 401 Đổi đệ tử** (cho người chơi đổi nếu đệ ra chỉ số xấu) · **1 × 402 Nâng kỹ năng 1 đệ tử**.
- **Mở khóa**: hệ thống đệ tử (`PetService`), giao diện đệ tử, item 401–404.
- **Ghi chú triển khai**:
  - Hiện **không có nguồn đệ tử miễn phí**: `createNormalPet` chỉ gọi từ Super Broly, gói VIP của ToriBot (nạp tiền thật) và lệnh admin. **Phải thêm một nguồn miễn phí cho nhiệm vụ này**, nếu không người chơi kẹt cứng.
  - Đề xuất: `npc_list/QuaTrung.java` thêm mục menu **"Nở trứng"** hiện ra khi `getIdTask(player) == ConstTask.TASK_12_0` và `player.pet == null` → `PetService.gI().createNormalPet(player)`.
  - **Trigger B8 (mới)**: `checkDoneTaskHavePet(player)` gọi ngay sau `createNewPet` và một lần nữa trong `Player.update` (để người chơi đã có đệ từ trước cũng qua bước ngay).
  - `checkDoneTaskKillMob`: nhánh `10/11/12` nay phục vụ 4 bước khác nhau (`7_0` có đếm giờ, `8_2`, `12_1`, `15_0`) → dùng `isCurrentTask` để phân biệt, đừng viết `if/else` lồng nhau.

---

### NV 13 — Không ai đi một mình
**Chương** 2 | **Mốc SM khi xong** ~980.000 | **Nhiệm vụ kế** NV 14 — Chợ đen ký ức

*Giu-ma Đầu Bò mở cửa Lãnh địa Bang Hội cho người chơi và nói thẳng: "Ký ức một người thì dễ lấy. Ký ức của cả một bang thì khó hơn nhiều — vì tụi nó nhắc nhau." Bài học của chương này không phải một chiêu thức: nó là việc đi cùng người khác.*

| # | Tên bước | Kiểu | Điều kiện server kiểm tra | Số lượng | Map (id + tên) | NPC (id + tên) | Hằng số | Thưởng bước |
|---|---|---|---|---|---|---|---|---|
| 0 | Gia nhập một bang hội | A10 | `checkDoneTaskJoinClan`: gọi từ `ClanService` khi bang có ≥ 2 thành viên và `player.clan != null` | 1 | mọi map | — | `TASK_13_0` | 28.000 SM & TN |
| 1 | **Cùng bạn cùng bang diệt 30 quái mẹ** | **B13** | `checkDoneTaskKillMobTogether` (mới): `mob.tempId == 10 / 11 / 12`, **và** `player.clan != null`, **và** trong `player.zone` có ≥ **2** người chơi (kể cả bản thân) thuộc **cùng `clan.id`** | 30 | 4 Rừng xương / 12 Vực maima / 18 Rừng thông Xayda | — | `TASK_13_1` | 28.000 SM & TN |
| 2 | Gặp Giu-ma Đầu Bò | A3 | `checkDoneTaskTalkNpc`: `npc.tempId == 47` (Giu-ma Đầu Bò), map 153 | 1 | 153 Lãnh địa Bang Hội | 47 Giu-ma Đầu Bò | `TASK_13_2` | 28.000 SM & TN |

**Luật của bước B13 (bước "rủ bạn" duy nhất của chương 2):**

| Câu hỏi | Trả lời |
|---|---|
| Mấy người? | **2 người trở lên** (`ConstTask.NMEMBER_DO_TASK_TOGETHER = 2`), tính cả bản thân người chơi |
| Có bắt buộc cùng bang không? | **Có.** Cả hai phải `clan != null` và **cùng `clan.id`**. Đi cùng người lạ **không** tính |
| Có bắt buộc cùng khu không? | **Có.** Phải cùng `zone` (cùng map **và** cùng khu). Khác khu → không tính |
| Ai giết thì được tính? | Người ra đòn cuối (`mob.plAtt`). Cả hai người cùng làm nhiệm vụ đều được cộng nếu mỗi người tự kết liễu quái của mình |
| Có thưởng cộng dồn không? | **Có**: nếu trong zone có ≥ 3 người cùng bang, mỗi con tính **×2** (giữ tinh thần `NMEMBER_DO_TASK_TOGETHER` của tuyến cũ) |
| Không có ai cùng bang online thì sao? | Tiến độ **không tăng** và client hiện thông báo "Cần thêm 1 thành viên cùng bang ở cùng khu". Không có đường vòng — đây là bước cố ý bắt người chơi tương tác |

- **Lời thoại**
  - *Bước 0 — NPC 47 Giu-ma Đầu Bò:* "Vào bang đi nhóc. Ký ức một người thì dễ lấy, ký ức cả bang thì khó nuốt lắm."
  - *Bước 1 — NPC 47 Giu-ma Đầu Bò:* "Rủ một đứa cùng bang đi chung. Hai cái đầu quên chậm hơn một cái đầu."
  - *Bước 2 — NPC 47 Giu-ma Đầu Bò:* "Thấy chưa? Lúc nó gọi tên ngươi, ngươi nhớ ngươi là ai ngay lập tức."
  - *Bước 2 — NPC 63 Jaco:* "Đây là thứ duy nhất Heart không tính tới: người ta nhắc nhau."

- **Mô tả nhiệm vụ (cột `detail`)**: "Gia nhập bang hội, cùng một thành viên cùng bang diệt 30 quái mẹ rồi gặp Giu-ma Đầu Bò. Thưởng 280.000 sức mạnh, 280.000 tiềm năng."

- **Thưởng khi hoàn thành**: 280.000 SM · 280.000 TN · **1 × 457 Thỏi vàng** · **1 × 295 Gói 30 đậu thần cấp 3**.
- **Mở khóa**: bang hội (mời / vào bang), map **153 Lãnh địa Bang Hội**, nhiệm vụ bang (`clan_task_template`), phó bản bang.
- **Ghi chú triển khai**:
  - `ClanService.sendInviteClan` / `acceptJoinClan`: đổi ngưỡng `TASK_10_0` → **`TASK_13_0`**.
  - **Sửa lỗi cũ** (§8 file gốc): bước "vào bang" của tuyến cũ hoàn thành nhầm khi chỉ nói chuyện sư phụ, vì `13_0` nằm trong bảng NPC của Quy Lão Kame. Tuyến mới **không** để `TASK_13_0` trong bất kỳ bảng `checkDoneTaskTalkNpc` nào.
  - **Trigger B13 (mới)**: viết `countClanMemberInZone(Player)` trong `TaskService`; dùng trong `checkDoneTaskKillMob` khi `isCurrentTask(player, TASK_13_1)`.
  - `ChangeMapService`: thêm map **153** ngưỡng `TASK_13_0` (trước đây không khóa).
  - `ChangeMapService`: **bỏ** hai ngưỡng cũ `TASK_13_0` (map 27/28/31/32/35/36) và `TASK_15_0` (map 30/34/38) — xem NV 14.

---

### NV 14 — Chợ đen ký ức
**Chương** 2 | **Mốc SM khi xong** ~1.400.000 | **Nhiệm vụ kế** NV 15 — Người bạn đã quên

*Máy dò dẫn về Nhà Bunma, nơi cô kỹ sư phát hiện một chuyện tởm hơn cả vết nứt: có người đang **bán** ký ức. Đóng hộp, dán nhãn, đề giá. Một ký ức tuổi thơ đổi được ba thỏi vàng. Kẻ thu gom hôm trước chỉ là nhân viên giao hàng.*

| # | Tên bước | Kiểu | Điều kiện server kiểm tra | Số lượng | Map (id + tên) | NPC (id + tên) | Hằng số | Thưởng bước |
|---|---|---|---|---|---|---|---|---|
| 0 | Nghe Bunma báo tin ở Nhà Bunma | A3 | `checkDoneTaskTalkNpc`: `npc.tempId == 37` (Bunma, bản tương lai) và `player.zone.map.mapId == 102` | 1 | 102 Nhà Bunma | 37 Bunma | `TASK_14_0` | 40.000 SM & TN |
| 1 | Mua lại một món hàng ở quầy Uron | **B4** | `checkDoneTaskBuyItem`: mua thành công **bất kỳ item nào** ở shop `4 URON` (NPC 16 Uron) | 1 | 84 Siêu Thị | 16 Uron | `TASK_14_1` | 40.000 SM & TN |
| 2 | Chặn đoàn thú tải hàng cho chợ đen | A1 | `checkDoneTaskKillMob`: `mob.tempId == 16 Heo rừng / 17 Heo da xanh / 18 Heo Xayda` (HP 1.500, lv6) | 20 | 27 Rừng Bamboo / 31 Núi hoa vàng / 35 Rừng cọ | — | `TASK_14_2` | 40.000 SM & TN |

- **Lời thoại**
  - *Bước 0 — NPC 37 Bunma:* "Cậu ngồi xuống đi. Chuyện này tôi nói đứng không nổi. Có người đang bán ký ức."
  - *Bước 0 — NPC 37 Bunma:* "Đóng hộp đàng hoàng, có nhãn, có giá. Một tuổi thơ đổi ba thỏi vàng. Ba thỏi thôi."
  - *Bước 1 — NPC 16 Uron:* "Hàng này tôi nhập lại thôi, thề! Tôi có biết trong hộp là ký ức của ai đâu!"
  - *Bước 2 — NPC 37 Bunma:* "Lũ thú đó tải hàng cho chúng. Chặn đường đi, tôi cần cái vận đơn."
  - *Bước 2 — NPC 63 Jaco:* "Chữ trên vận đơn là chữ của một người ta biết. Và ta ước gì ta không biết."

- **Mô tả nhiệm vụ (cột `detail`)**: "Gặp Bunma ở Nhà Bunma, mua lại một món hàng ở quầy Uron và chặn 20 con thú tải hàng. Thưởng 400.000 sức mạnh, 400.000 tiềm năng."

- **Thưởng khi hoàn thành**: 400.000 SM · 400.000 TN · **1 × 2004 Hộp Ký Ức Bị Đánh Cắp (ITEM MỚI)** · **3 × 457 Thỏi vàng**.
- **Mở khóa**: **Cửa hàng ký gửi** (NPC 28 ở map 84), map **102 Nhà Bunma**, map **27 / 31 / 35**.
- **Ghi chú triển khai**:
  - **ITEM MỚI `2004` "Hộp Ký Ức Bị Đánh Cắp"** — TYPE 11, gender 3, level 1, part -1, **icon mượn `7222`** (của item 796 Hộp Capsule). Là vật chứng dẫn sang NV 15. *Phương án thay thế:* item **796**.
  - **Xung đột với §8 cần chốt**: §8 xếp map **102** vào nhóm mở tại `TASK_24_0`, nhưng NV 14 lấy 102 làm map chính. Đề xuất **tách nhóm**: `102 Nhà Bunma → TASK_14_0`; `92, 93, 94, 96 → TASK_24_0`.
  - **Xung đột thứ hai**: §8 xếp cả nhóm `27–38` vào `TASK_16_0`, nhưng NV 14 (bước 2) và NV 15 (map chính) cần **27 / 31 / 35** sớm hơn. Đề xuất **tách nhóm**: `27, 31, 35 → TASK_14_2`; `28, 29, 30, 32, 33, 34, 36, 37, 38 → TASK_16_0`.
  - `NpcManager.getNpcsByMapPlayer`: NPC **38 Ca Lích** ở map 102 vẫn giữ ngưỡng `TASK_24_0` → người chơi vào được map 102 ở NV 14 nhưng **chưa thấy** Ca Lích. Cần kiểm tra map 102 không bị trống trơ (còn NPC 37 Bunma và 82 Rương Sưu Tầm).
  - `npc_list/Uron.java`: gọi `checkDoneTaskTalkNpc` trước khi mở shop; trigger B4 nằm ở `ShopService` chứ không ở NPC.

---

### NV 15 — Người bạn đã quên
**Chương** 2 — *cao trào chương 2* | **Mốc SM khi xong** ~2.000.000 | **Nhiệm vụ kế** NV 16 — Dấu vết dẫn về phía Nam

*Chữ trên vận đơn là chữ của Jaco. Người chơi tìm tới nơi hẹn và thấy hắn đứng đó, mắt trắng dã, súng chĩa thẳng: cái máy đã lấy hết của hắn — kể cả khuôn mặt người bạn hắn vừa đi cùng suốt hai chương. Đây là lần đầu người chơi phải đánh một người mình thương để cứu người đó.*

| # | Tên bước | Kiểu | Điều kiện server kiểm tra | Số lượng | Map (id + tên) | NPC (id + tên) | Hằng số | Thưởng bước |
|---|---|---|---|---|---|---|---|---|
| 0 | Tới điểm hẹn trên vận đơn | A1 | `checkDoneTaskKillMob`: `mob.tempId == 10 Thằn lằn mẹ / 11 Phi long mẹ / 12 Quỷ bay mẹ` | 20 | 27 Rừng Bamboo / 31 Núi hoa vàng / 35 Rừng cọ | — | `TASK_15_0` | 60.000 SM & TN |
| 1 | **Đánh thức Jaco** | A2 | `checkDoneTaskKillBoss`: `boss.id == -2001` (**BOSS MỚI**) **và** `boss.currentLevel == 1` (form cuối "Jaco Vô Thức") | 1 | 27 / 31 / 35 | — | `TASK_15_1` | 60.000 SM & TN |
| 2 | Gặp lại Jaco ở Trạm tàu vũ trụ | A3 | `checkDoneTaskTalkNpc`: `npc.tempId == 63` và `player.zone.map.mapId ∈ {24, 25, 26}` | 1 | 24 / 25 / 26 Trạm tàu vũ trụ | 63 Jaco | `TASK_15_2` | 60.000 SM & TN |

**Boss mới `-2001` — đề xuất chỉ số (mốc SM người chơi ~2.000.000), khai báo theo `BossesData`:**

| Trường | Form 0 — "Jaco Mất Ký Ức" | Form 1 — "Jaco Vô Thức" |
|---|---|---|
| `name` | `"Jaco Mất Ký Ức"` | `"Jaco Vô Thức"` |
| `gender` | `0` | `0` |
| `outfit` | `{624, 625, 626, -1, -1, -1}` (mượn tạo hình NPC 63 Jaco) | `{624, 625, 626, -1, -1, -1}` (nếu muốn khác form 0, gán thêm `aura`/`eff` bằng một id hào quang **đã có sẵn** trong data client) |
| `hp` | `{1_200_000}` | `{2_500_000}` |
| `dame` | `3_000` | `5_000` |
| `mapJoin` | `{27, 31, 35}` | *(theo form 0)* |
| `skillTemp` | `{{0, 5}, {10, 3}}` — Chiêu đấm cấp 5 + Quả cầu kênh khí cấp 3 | `{{0, 7}, {10, 5}, {20, 3}}` — thêm Dịch chuyển tức thời |
| `secondsRest` | `REST_5_M` (300s) | — |
| `typeAppear` | `DEFAULT_APPEAR` | `ANOTHER_LEVEL` |
| Nguồn tạo | `loadBoss()` ×1 (phủ cả 3 map) | tự lên form khi form 0 chết |

> Đối chiếu để cân bằng: Dr.Kôrê (-31) có HP 2.000.000 / dame 12.000 và ở tuyến cũ nằm tận nhiệm vụ 23. Ở đây dame để **thấp hơn nhiều** (3.000 → 5.000) vì người chơi mốc 2 triệu SM còn mặc đồ cấp 4–5, HP thực tế chỉ khoảng 20.000–40.000. Tổng HP 3,7 triệu ≈ 1,5–3 phút đánh solo.

- **Lời thoại**
  - *Bước 1 — Boss -2001 (textS):* "Đứng im. Cảnh sát vũ trụ đây. Ngươi là... ngươi là... ngươi là ai?"
  - *Bước 1 — Boss -2001 (textM, form 0):* "Ta có một người bạn. Ta nhớ là có. Nhưng ta không nhớ mặt nó."
  - *Bước 1 — Boss -2001 (textM, form 1):* "Đừng gọi tên ta nữa! Mỗi lần ngươi gọi là ta lại đau!"
  - *Bước 1 — Boss -2001 (textE):* "...Ngươi vẫn gọi đúng tên ta. Cảm ơn. Cảm ơn vì đã không quên ta."
  - *Bước 2 — NPC 63 Jaco:* "Ta bán ký ức của chính mình để mua thông tin về Heart. Ta tưởng ta chịu nổi."
  - *Bước 2 — NPC 63 Jaco:* "Giờ ta nợ ngươi một cái tên. Đi tiếp đi — hắn đang đợi ngươi ở phía Nam."

- **Mô tả nhiệm vụ (cột `detail`)**: "Tới điểm hẹn, đánh thức Jaco đang bị xóa ký ức rồi gặp lại hắn ở Trạm tàu vũ trụ. Thưởng 600.000 sức mạnh, 600.000 tiềm năng và Mảnh Ký Ức #2."

- **Thưởng khi hoàn thành**: 600.000 SM · 600.000 TN · **1 × 2011 Mảnh Ký Ức #2 (ITEM MỚI)** · **1 × 295 Gói 30 đậu thần cấp 3** · **1 × 1074 Đá nâng cấp cấp 1** (mồi cho NV 17 dạy nâng cấp đồ).
- **Mở khóa**: PVP / **112 Võ đài Hạt Mít**, map **27 → 38** tại **`TASK_16_0`** (trừ 27/31/35 đã mở từ `TASK_14_2`).
- **Ghi chú triển khai**:
  - **ITEM MỚI `2011` "Mảnh Ký Ức #2"** — TYPE 11, gender 3, level 1, part -1, **icon mượn `420`** (của item 15 Ngọc Rồng 2 sao). *Phương án thay thế:* item **15**.
  - `BossesData`: thêm `-2001` theo bảng trên; boss nhiều form dùng `AppearType.ANOTHER_LEVEL` giống mẫu Fide (-28) và Cooler (-29).
  - `checkDoneTaskKillBoss`: `case -2001:` chỉ `doneTask(TASK_15_1)` **khi `boss.currentLevel == 1`** (giống cách code hiện tại xét `currentLevel` cho Fide/Xên).
  - Phần thưởng rơi đồ của boss này nên **thấp** (Mẫu A rút gọn) — Mảnh Ký Ức #2 trao qua `rewardDoneTask`, không trao qua `Boss.reward`, tránh người chơi nhặt hụt.
  - `npc_list/Jaco.java`: thêm nhánh thoại sau NV 15 (bước `15_2`) và giữ NPC ở map 24/25/26 vĩnh viễn.

---

## Ghi chú / việc cần làm khi code

> **Nhiệm vụ chính không thưởng vàng/ngọc/hồng ngọc** — chỉ SM, TN và vật phẩm (quyết định của chủ dự án).

### A. Trigger mới cần thêm (NV 0–15 đòi hỏi 5 loại)

| Mã | Hàm mới trong `TaskService` | Điểm móc (file) | Bước dùng | Công sức |
|---|---|---|---|---|
| **B4** | `checkDoneTaskBuyItem(Player, int itemTemplateId, int shopId)` | `shop/ShopService` — sau khi trừ tiền + `addItemBag` thành công | `TASK_8_1` (item 12, shop 1/2/3), `TASK_14_1` (mọi item, shop 4 URON) | Nhỏ |
| **B7** | `checkDoneTaskHarvestPea(Player, int soHat)` | `npc/MagicTree.harvestPea` — sau `addPeaHarvest` | `TASK_11_0` (5 hạt) | Nhỏ |
| **B8** | `checkDoneTaskHavePet(Player)` | `services/PetService.createNewPet` **và** `player/Player.update` | `TASK_12_0` | Nhỏ |
| **B9** | `checkDoneTaskLearnSkill(Player, Skill)` | `services/SkillService` — sau khi `skill.point` tăng | `TASK_10_1` (skill 1/3/5 cấp ≥ 1) | Nhỏ |
| **B12** | `startTimedSubTask` + `updateTimedSubTask` + `resetTimedSubTask` | `player/Player.update` (đọc `TaskMain.lastTime`), `ChangeMapService` | `TASK_7_0` (180.000 ms, 10 con) | Trung bình |
| **B13** | `countClanMemberInZone(Player)` dùng trong `checkDoneTaskKillMob` | `mob/Mob.java` (đường cũ), `map/Zone` để đếm | `TASK_13_1` (≥2 người cùng bang, cùng zone) | Trung bình |

`checkDoneTaskUseItem` (**A12**) đã có hàm nhưng `switch` rỗng — chương này nạp 3 `case`: **13** (`TASK_0_4`), **2002** (`TASK_5_1`), **2006** (`TASK_11_1`).

### B. Vật phẩm mới (6 item, id ≥ 2000)

| id | Tên | TYPE | Icon mượn của | Dùng ở | Phương án không đụng data client |
|---|---|---|---|---|---|
| 2001 | Mảnh Vỡ Hư Không | 11 | `1421` (225 Mảnh đá vụn) | NV 2 bước 2 | dùng thẳng item **225** |
| 2002 | Kỷ Vật Của Ông | 11 | `9067` (992 Nhẫn thời không sai lệch) | NV 5 bước 0–2 | dùng thẳng item **992** |
| 2003 | Máy Dò Ký Ức | 11 | `1089` (12 Rada cấp 1) | Thưởng NV 8 | dùng thẳng item **1822 Rada ngọc rồng** |
| 2004 | Hộp Ký Ức Bị Đánh Cắp | 11 | `7222` (796 Hộp Capsule) | Thưởng NV 14 | dùng thẳng item **796** |
| 2006 | Hạt Giống Hy Vọng | 27 | `241` (13 Đậu thần cấp 1) | NV 11 bước 1 | dùng thẳng item **568 Quả Trứng** |
| 2010 | Mảnh Ký Ức #1 | 11 | `419` (14 Ngọc Rồng 1 sao) | Thưởng NV 7 | dùng thẳng item **14** |
| 2011 | Mảnh Ký Ức #2 | 11 | `420` (15 Ngọc Rồng 2 sao) | Thưởng NV 15 | dùng thẳng item **15** |

> id 2005, 2007–2009 để trống, dành cho 20b/20c (Mảnh Ký Ức #3 → #7 nên đi tiếp 2012 → 2016 để dãy 2010–2016 liền mạch).
> Mọi item mới đều **phải tăng phiên bản data client** (§1.5 file gốc). Nếu chốt phương án "không đụng client", chỉ cần sửa bảng ánh xạ trong SQL, code không đổi.

### C. Boss mới (2 con)

| id | Tên | Form | HP | Dame | Map | Tạo hình mượn | Nghỉ | Dùng ở |
|---|---|---|---|---|---|---|---|---|
| -2000 | Kẻ Thu Gom | 1 | 80.000 | 400 | 4, 12, 18 | NPC 26 Độc Nhãn `{144,145,146}` | `REST_2_M` | NV 6 bước 1 |
| -2001 | Jaco Mất Ký Ức → Jaco Vô Thức | 2 | 1.200.000 → 2.500.000 | 3.000 → 5.000 | 27, 31, 35 | NPC 63 Jaco `{624,625,626}` | `REST_5_M` | NV 15 bước 1 |

Cả hai đăng ký ở `BossesData` + `loadBoss()` ×1, dùng lại lớp `Boss` chuẩn (không cần class riêng trừ khi muốn thoại đặc biệt giữa trận). Dải id `-2000 → -2099` hiện **trống** trong toàn bộ danh sách boss (§1.1 của 11-boss.md) → an toàn.

### D. NPC / dữ liệu map cần sửa (`map_template`)

| Việc | Chi tiết |
|---|---|
| Thêm **NPC 63 Jaco** vào map **42, 43, 44** | NV 3 bước 0 |
| Thêm **NPC 63 Jaco** vào map **25, 26** | NV 7 & NV 15 bước cuối; Jaco hiện chỉ có ở map 24 (Trái Đất) và 139 |
| Viết **class `npc_list/Jaco.java`** | NPC 63 hiện rơi vào `default` của `NpcFactory.createNPC` → không có `openBaseMenu` riêng, không gọi được `checkDoneTaskTalkNpc` |
| Thêm mục menu **"Nở trứng"** cho `npc_list/QuaTrung` | Nguồn đệ tử miễn phí cho NV 12 (hiện chỉ có Super Broly / gói VIP tiền thật / admin) |
| `npc_list/Bunma`, `Uron`, `GiuMaDauBo`, `DauThan` | Gọi `checkDoneTaskTalkNpc` / `checkDoneTaskConfirmMenuNpc` đầu `openBaseMenu` và thoát sớm nếu trả `true` |

### E. Mốc khóa map / tính năng phải sửa (phần thuộc NV 0–15)

| Map / tính năng | Mốc cũ | Mốc mới | File |
|---|---|---|---|
| 1, 8, 15 Đồi | `TASK_1_0` | **`TASK_2_0`** | `ChangeMapService` |
| 42, 43, 44 Vách núi | `TASK_2_0` | **`TASK_3_0`** | `ChangeMapService` |
| 2, 9, 16 | `TASK_3_0` | **`TASK_4_0`** | `ChangeMapService` |
| 3, 11, 17 Rừng | `TASK_7_0` | **`TASK_5_0`** | `ChangeMapService` |
| 24, 25, 26 Trạm tàu vũ trụ | `TASK_4_0` | **`TASK_7_1`** | `ChangeMapService` |
| 84 Siêu Thị | (không khóa) | **`TASK_8_0`** | `ChangeMapService` |
| 5, 13, 20 Map sư phụ | (không khóa) | **`TASK_9_0`** | `ChangeMapService` |
| 153 Lãnh địa Bang Hội | (không khóa) | **`TASK_13_0`** | `ChangeMapService` |
| Mời / vào bang hội | `TASK_10_0` | **`TASK_13_0`** | `ClanService` |
| **102 Nhà Bunma** | `TASK_21_0` | **`TASK_14_0`** *(tách khỏi nhóm 92–96 — xem rủi ro #2)* | `ChangeMapService` |
| **27, 31, 35** | `TASK_13_0` | **`TASK_14_2`** *(tách khỏi nhóm 27–38 — xem rủi ro #2)* | `ChangeMapService` |
| 28, 29, 30, 32, 33, 34, 36, 37, 38 | `TASK_13_0` / `TASK_15_0` | **`TASK_16_0`** | `ChangeMapService` |
| Túi lưng (flagBag 28) | `TASK_3_2` | **`TASK_5_2`** (đổi `==` thành `>=`) | `Player.getFlagBag` |
| Rơi item nhiệm vụ từ quái | Đùi gà 73 ở `TASK_2_0`; Ngọc 7 sao 20 ở `TASK_8_1` | **2001** ở `TASK_2_2`; **2002** ở `TASK_5_0`; bỏ nhánh Ngọc 7 sao | `Mob.dropItemTask` |
| Item hiện trên đất | 74 Đùi gà nướng (`>= TASK_3_0`), 78 Đứa bé (`== TASK_3_1`) | **bỏ cả hai**, thay bằng 2001 / 2002 (chỉ chủ nhân thấy) | `Zone.getItemMapsForPlayer` |
| Tàu Pảy Pảy nhận sát thương thật | `== TASK_10_1` | **`>= TASK_10_1`** | `TauPayPay.injured` |

### F. Lỗi cũ được sửa luôn trong khối này

1. **Ông Gôhan / Moori / Paragus không kiểm tra `gender`** → người chơi Namếc có thể hoàn thành bước bằng ông Gôhan. Tuyến mới bắt buộc `npc.tempId == (gender == 0 ? 0 : gender == 1 ? 2 : 1)`.
2. **Bước "vào bang" hoàn thành khi chỉ nói chuyện sư phụ** (`13_0` lọt vào bảng NPC của Quy Lão Kame do đặt ngoặc sai) → `TASK_13_0` nay chỉ đến từ `checkDoneTaskJoinClan`.
3. **Mốc 40.000 dùng nhầm cho bước cần 500.000** trong `checkDoneTaskPower` → bảng mốc viết lại từ đầu, chương 2 chỉ còn một dòng `250.000 → TASK_10_2`.
4. **`sendNextTaskMain` rẽ theo hành tinh ở id 3 → gender+4** → bỏ; chương 1–2 chạy thẳng `id + 1`.

### G. Rủi ro / điểm cần chủ dự án chốt

| # | Vấn đề | Đề xuất |
|---|---|---|
| 1 | **Mốc `A5` của NV 10**: bảng tổng ghi 40.000, nhưng cuối chương 1 người chơi đã ~66.000 → bước tự xong. | Nâng lên **250.000** (đang ghi trong bảng bước). Cần chốt. |
| 2 | **Map 102 và 27/31/35 bị §8 khóa tới `TASK_24_0` / `TASK_16_0`, nhưng NV 14–15 cần chúng ở chương 2.** Nếu bỏ sót, người chơi **kẹt cứng ở NV 14**. | Tách nhóm map như bảng E. Đây là rủi ro nghiêm trọng nhất của khối này. |
| 3 | **Không có nguồn đệ tử miễn phí** cho NV 12 (chỉ Super Broly / gói VIP tiền thật / admin). | Thêm menu "Nở trứng" ở NPC 50 Quả trứng, giới hạn 1 lần khi `getIdTask == TASK_12_0`. |
| 4 | **B13 (NV 13) phụ thuộc người chơi khác online cùng bang cùng khu.** Server vắng → kẹt. | Giữ nguyên yêu cầu (đây là điểm "cuốn"), nhưng bổ sung nút "Tìm đồng đội" ở NPC 47 hoặc cho phép **đệ tử** tính là 1 người nếu server vắng — cần chốt. |
| 5 | **Quái chương 2 quá yếu so với mốc SM.** Mob 10/11/12 chỉ có 1.000 HP trong khi người chơi lên tới 2 triệu SM. | Nâng `level` của mob trong `map_template.mobs` ở map 4/12/18 và 27/31/35, hoặc chấp nhận chương 2 là giai đoạn "cày nhanh cho sướng tay". |
| 6 | **NPC 63 Jaco chưa có class**, và NPC 38 Ca Lích vẫn ẩn tới `TASK_24_0` dù map 102 mở từ NV 14. | Viết `Jaco.java`; kiểm tra map 102 không trống trơ. |
| 7 | **Thưởng chương 1 cộng dồn ~66.000 SM**, hơi vượt mốc "~50.000" ở §6.1. | Chấp nhận (§6.2 quy định 2.000 → 20.000/nhiệm vụ, cộng 8 nhiệm vụ tất yếu ra con số này), hoặc hạ NV 6–7 xuống 10.000 / 16.000. |
