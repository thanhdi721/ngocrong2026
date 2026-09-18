# 20 — Thiết kế tuyến nhiệm vụ chính MỚI: "Vết Nứt Hư Không"

> **Trạng thái: BẢN THIẾT KẾ ĐỂ DUYỆT — chưa có dòng code nào được sửa.**
> Sau khi bạn duyệt, phần triển khai (SQL + Java) làm theo §9.
>
> Tài liệu nền: [12-nhiem-vu-chinh.md](../1-he-thong-hien-tai/12-nhiem-vu-chinh.md) (tuyến cũ), [02b-database-du-lieu-template.md](../1-he-thong-hien-tai/02b-database-du-lieu-template.md) (id map/mob/npc/item), [11-boss.md](../1-he-thong-hien-tai/11-boss.md) (boss), [14-ban-do-pho-ban.md](../1-he-thong-hien-tai/14-ban-do-pho-ban.md) (phó bản).
> Chi tiết từng bước nằm ở 3 file kèm theo: [20a](20a-chi-tiet-nhiem-vu-00-15.md), [20b](20b-chi-tiet-nhiem-vu-16-31.md), [20c](20c-chi-tiet-nhiem-vu-32-47.md).

## Mục lục

1. [Quyết định đã chốt & phạm vi](#1-quyết-định-đã-chốt--phạm-vi)
2. [Vì sao tuyến cũ chán — và sửa bằng cách nào](#2-vì-sao-tuyến-cũ-chán--và-sửa-bằng-cách-nào)
3. [Cốt truyện mới](#3-cốt-truyện-mới)
4. [Bộ công cụ bước nhiệm vụ (trigger)](#4-bộ-công-cụ-bước-nhiệm-vụ-trigger)
5. [Bảng tổng 48 nhiệm vụ](#5-bảng-tổng-48-nhiệm-vụ)
6. [Đường cong sức mạnh & phần thưởng](#6-đường-cong-sức-mạnh--phần-thưởng)
7. [Nhánh lựa chọn](#7-nhánh-lựa-chọn)
8. [Khóa map / tính năng theo tuyến mới](#8-khóa-map--tính-năng-theo-tuyến-mới)
9. [Kế hoạch triển khai](#9-kế-hoạch-triển-khai)
10. [Rủi ro & điểm cần bạn quyết](#10-rủi-ro--điểm-cần-bạn-quyết)

---

## 1. Quyết định đã chốt & phạm vi

| Hạng mục | Chốt |
|---|---|
| Nội dung | Cốt truyện mới hoàn toàn, không bám nguyên tác Ngọc Rồng |
| Quy mô | **48 nhiệm vụ** (id 0 → 47), khoảng **210 bước con** |
| Người chơi cũ | **Reset toàn bộ về nhiệm vụ 0**, có SQL bù thưởng theo tiến độ cũ (§9.4) |
| Cách làm | Duyệt thiết kế này trước, sau đó mới viết SQL + sửa Java |

**Ràng buộc kỹ thuật bắt buộc tôn trọng** (lấy từ code thật):

1. Tiến độ người chơi chỉ lưu `[id, index, count, lastTime]` trong cột `player.data_task` → không lưu được "cờ" phụ. Mọi nhánh rẽ phải thể hiện bằng **task id khác nhau** (§7).
2. Điều kiện hoàn thành bước nằm trong `TaskService.java` (Java), không nằm trong DB → thêm kiểu bước mới = sửa Java.
3. Mã bước `((taskId<<10)+index)<<1` tăng dần → mọi chỗ khóa map/tính năng dạng `getIdTask(player) < TASK_x_y` phải đánh số lại (§8).
4. `Service.addSMTN` bị chặn bởi giới hạn sức mạnh (18 tỷ ở `limitPower = 0`) → thưởng sức mạnh của chương 5–6 chỉ có tác dụng nếu người chơi đã mở giới hạn. Tuyến mới vì vậy có nhiệm vụ dạy mở giới hạn (NV 33).
5. Item mới (mảnh ký ức, tín vật…) phải dùng **icon đã có sẵn** trong `SRC/data/icon`, và phải tăng phiên bản data để client tải lại. Nếu bạn không muốn đụng vào data client, tuyến này vẫn chạy được bằng cách thay hết item mới bằng item sẵn có — xem §10.

---

## 2. Vì sao tuyến cũ chán — và sửa bằng cách nào

| Vấn đề của tuyến cũ | Cách tuyến mới xử lý |
|---|---|
| 125 bước nhưng chỉ có ~6 kiểu bước, đa số là "giết N con quái" và "nói chuyện NPC" | 18 kiểu bước (§4). Không chương nào lặp cùng một kiểu quá 2 lần liên tiếp |
| Không có lựa chọn — ai chơi cũng giống nhau | 3 điểm rẽ nhánh, mỗi nhánh có thưởng và danh hiệu riêng (§7) |
| Người chơi không biết mình đang ở đâu trong câu chuyện | Chia 6 chương, mỗi chương có mở đầu, cao trào (đánh boss) và kết |
| Thưởng quá nhỏ (nhiệm vụ 24 chỉ 12.500 SM và 500.000 vàng) trong khi trang bị bán hàng tỷ | Thưởng bám theo đường cong sức mạnh thật (§6), có cả vật phẩm chứ không chỉ SM |
| Chữ mô tả trong DB không khớp thứ server thưởng | Mỗi nhiệm vụ có một dòng thưởng duy nhất, sinh cả DB lẫn code từ cùng một bảng |
| Nhiệm vụ 25–29 không thưởng gì (code thiếu) | Bảng thưởng phủ hết 48 nhiệm vụ |
| Nội dung dừng ở Xên, trong khi server có Mabư, Black Goku, Cumber, Baby, Broly, Bill | Chương 4–6 dẫn người chơi qua toàn bộ nội dung endgame đã có sẵn |
| Nhiệm vụ không dạy người chơi dùng tính năng (đập đồ, phó bản, đệ tử, rồng thần) | Mỗi tính năng lớn có đúng 1 nhiệm vụ dạy dùng, kèm nguyên liệu miễn phí để thử |

---

## 3. Cốt truyện mới

### 3.1 Tiền đề

Một **vết nứt** mở ra giữa ba hành tinh. Thứ tràn qua vết nứt không giết người — nó **ăn ký ức**. Cả ba hành tinh dần quên: quên người thân, quên cách chiến đấu, quên cả tên hành tinh mình.

Nhân vật chính là người duy nhất còn nhớ, vì trong người có một **Mảnh Ký Ức** — mảnh vỡ của thứ gọi là **Lõi Hư Không**.

Kẻ đứng sau: **Heart** (NPC 108) — kẻ tin rằng đau khổ đến từ ký ức, nên xóa sạch ký ức là cứu rỗi. Hắn dùng **Dr. Myuu** (NPC 83) để chế tạo cỗ máy hút ký ức, và nuôi **Cumber** (boss -203999) trong Hành tinh ngục tù như một vũ khí sống.

Người chơi đi tìm 7 Mảnh Ký Ức rải khắp vũ trụ, mỗi mảnh trả lại cho thế giới một phần quá khứ — và mỗi lần trả lại, thế giới thay đổi: map mới mở, NPC nhớ ra người chơi, boss mới thức giấc.

### 3.2 Tuyến nhân vật (đều là NPC đã có trong DB)

| Nhân vật | NPC id | Vai trò mới |
|---|---|---|
| Ông Gôhan / Moori / Paragus | 0 / 2 / 1 | Người nuôi. Là người **đầu tiên quên** người chơi — cú đấm cảm xúc mở màn |
| Jaco | 63 | Cảnh sát vũ trụ, người phát hiện vết nứt, dẫn chuyện chương 1–2 |
| Bunma | 7 | Chế "Máy dò ký ức", tuyến công nghệ |
| Quy Lão Kame / Guru / Vua Vegeta | 13 / 14 / 15 | Sư phụ; dạy chiến đấu, giữ Mảnh Ký Ức thứ 2 |
| Tapion | 53 | Người đến từ quá khứ đã bị xóa, bạn đồng hành chương 3 |
| Bardock | 70 | Kẻ nhìn thấy tương lai; cảnh báo về Heart |
| Granola | 76 | Thợ săn, muốn giết Heart vì thù riêng — nhánh "Báo thù" |
| Potage | 62 | Giữ bí mật bản sao, tuyến chương 4 |
| Dr. Myuu | 83 | Phản diện phụ, chương 4 |
| Heart | 108 | Phản diện chính, chương 5–6 |
| Bill & Whis | 55 / 56 | Thần Hủy Diệt: không cứu, chỉ thử thách — chương 6 |
| Tổ Sư Kaio | 43 | Người duy nhất biết Lõi Hư Không là gì |

### 3.3 Sáu chương

| Chương | Tên | NV | Mốc SM khi xong | Nội dung |
|---|---|---|---|---|
| 1 | Ngày ký ức vỡ | 0–7 | ~50.000 | Ông quên tên mình. Vết nứt đầu tiên. Mảnh Ký Ức #1 |
| 2 | Kẻ trộm ký ức | 8–15 | ~2.000.000 | Máy dò của Bunma, bái sư, bang hội, Mảnh #2 |
| 3 | Liên minh ba hành tinh | 16–23 | ~100.000.000 | Trại lính Fide, Tiểu đội sát thủ, doanh trại, Mảnh #3 |
| 4 | Cỗ máy và những bản sao | 24–31 | ~2.000.000.000 | Android, Xên, phòng thí nghiệm Myuu, Mảnh #4 |
| 5 | Vương triều bóng tối | 32–39 | ~8.000.000.000 | Mabư, Black Goku, Baby, Mảnh #5 và #6 |
| 6 | Lõi Hư Không | 40–47 | 18 tỷ + mở giới hạn | Broly, Cumber, Bill thử thách, Heart, Mảnh #7 |

### 3.4 Mạch cảm xúc

- **NV 0–2:** ấm áp rồi hụt hẫng. Ông gọi người chơi bằng tên người khác.
- **NV 7:** cao trào nhỏ đầu tiên — vết nứt nuốt cả map, người chơi phải chạy thoát trong thời gian giới hạn.
- **NV 15:** phản bội — người đồng hành chương 2 hóa ra đã bị xóa ký ức và tấn công người chơi.
- **NV 23:** chiến thắng lớn đầu tiên, cả ba hành tinh liên minh.
- **NV 31:** phát hiện bản sao của chính mình.
- **NV 39:** mất mát — một đồng minh chọn quên để cứu người chơi.
- **NV 47:** người chơi phải chọn: trả ký ức cho vũ trụ (mất toàn bộ Mảnh) hay giữ sức mạnh (§7).

---

## 4. Bộ công cụ bước nhiệm vụ (trigger)

### 4.1 Kiểu bước dùng lại được ngay (đã có trong `TaskService.java`)

| Mã | Kiểu bước | Hàm hiện có |
|---|---|---|
| A1 | Giết N con quái loại X | `checkDoneTaskKillMob` |
| A2 | Hạ boss X | `checkDoneTaskKillBoss` |
| A3 | Nói chuyện NPC X | `checkDoneTaskTalkNpc` |
| A4 | Nhặt item X trên đất | `checkDoneTaskPickItem` |
| A5 | Đạt mốc sức mạnh | `checkDoneTaskPower` |
| A6 | Đi tới map X | `checkDoneTaskGoToMap` |
| A7 | Dùng tiềm năng | `checkDoneTaskUseTiemNang` |
| A8 | Lấy đồ từ rương | `checkDoneTaskGetItemBox` |
| A9 | Chọn menu NPC | `checkDoneTaskConfirmMenuNpc` |
| A10 | Vào bang hội | `checkDoneTaskJoinClan` |
| A11 | Đạt sức đánh gốc X | `checkDoneTaskNangCS` |
| A12 | **Dùng item X** | `checkDoneTaskUseItem` — **hàm đã có, `switch` đang rỗng, dùng được ngay** |

### 4.2 Kiểu bước cần thêm code (điểm móc đều đã tồn tại)

| Mã | Kiểu bước mới | Móc vào | Công sức |
|---|---|---|---|
| B1 | Hoàn thành phó bản (Doanh trại / BĐKB / Khí gas / Con đường rắn độc) | nơi trao thưởng phó bản trong `services_dungeon/` | Nhỏ |
| B2 | Nâng cấp trang bị lên +N thành công | `combine/CombineService` | Nhỏ |
| B3 | Ép sao pha lê / pha lê hóa thành công | `combine/` | Nhỏ |
| B4 | Mua một item ở shop | `shop/ShopService` | Nhỏ |
| B5 | Thắng 1 trận PVP / Võ đài Hạt Mít / ĐHVT | `matches/` | Nhỏ |
| B6 | Gọi rồng thần và ước | `services/shenron/` | Nhỏ |
| B7 | Thu hoạch đậu thần từ cây | `npc/MagicTree` | Nhỏ |
| B8 | Có đệ tử / đệ tử đạt SM X | `services/PetService` | Nhỏ |
| B9 | Học skill đạt cấp X | `services/SkillService` | Nhỏ |
| B10 | Mở giới hạn sức mạnh | `OpenPowerService` | Nhỏ |
| B11 | Đạt HP / KI / giáp / chí mạng gốc X | `NPoint.increasePoint` (mở rộng A11) | Nhỏ |
| B12 | **Bước có giới hạn thời gian** (làm xong trong T phút, hết giờ phải làm lại) | dùng sẵn trường `TaskMain.lastTime` + kiểm tra trong `Player.update` | Trung bình |
| B13 | **Bước làm cùng người khác** (2+ người trong cùng khu) | đã có tiền lệ `NMEMBER_DO_TASK_TOGETHER` ở nhiệm vụ bang | Trung bình |
| B14 | **Bước rẽ nhánh theo lựa chọn** | bảng chuyển tiếp task id trong `sendNextTaskMain` | Trung bình |

> B12 và B13 là hai thứ tuyến cũ hoàn toàn không có, và là nguồn "cuốn" chính: một bước có đồng hồ đếm ngược, và một bước bắt buộc rủ bạn.

### 4.3 Nguyên tắc nhịp

- Bước đầu của mỗi nhiệm vụ luôn là bước **ngắn** (dưới 1 phút) để người chơi thấy tiến độ ngay.
- Không quá 2 bước "giết quái" liên tiếp.
- Mỗi nhiệm vụ 3–6 bước; nhiệm vụ cuối chương 5–7 bước và luôn kết bằng boss.
- Mỗi chương có đúng 1 bước B12 (đếm giờ) và 1 bước B13 (rủ bạn) — không nhiều hơn, tránh gây ức chế.

---

## 5. Bảng tổng 48 nhiệm vụ

Cột "Bước" ghi kiểu bước theo §4. Cột "Mở khóa" là thứ nhiệm vụ đó mở ra.
Chi tiết từng bước, lời thoại và số lượng nằm ở [20a](20a-chi-tiet-nhiem-vu-00-15.md) / [20b](20b-chi-tiet-nhiem-vu-16-31.md) / [20c](20c-chi-tiet-nhiem-vu-32-47.md).

### Chương 1 — Ngày ký ức vỡ (NV 0–7)

| NV | Tên | Tóm tắt | Map chính | Bước | Mở khóa |
|---|---|---|---|---|---|
| 0 | Người duy nhất còn nhớ | Tỉnh dậy trong nhà, ông gọi nhầm tên người chơi | 21/22/23 Nhà | A6, A8, A3, A9, A12 | Hành trang, rương, đậu thần |
| 1 | Bài học của ông | Tập đánh Mộc nhân, học bay | Làng 0/7/14 | A1, A3, A7 | Map đồi (1/8/15) |
| 2 | Vết nứt đầu tiên | Vết nứt hiện trên đồi, nhặt Mảnh vỡ lạ | 1/8/15 Đồi | A6, A1, A4 | Vách núi (42/43/44) |
| 3 | Cảnh sát vũ trụ Jaco | Jaco đáp xuống, đo sức mạnh người chơi | 42/43/44 Vách núi | A3, A1, A11 | Máy đo sức mạnh, shop làng |
| 4 | Thứ bò ra từ vết nứt | Quái mẹ biến dạng tràn ra | 2/9/16 | A1, A1, A3 | Map 2/9/16 |
| 5 | Ký ức của ông | Tìm lại vật kỷ niệm để ông nhớ ra | 3/11/17 Rừng | A4, A12, A3 | Map rừng, túi lưng |
| 6 | Người thu gom | Gặp kẻ thu gom ký ức đầu tiên | 4/12/18 | A1, A2 (boss mini), A3 | Trạm tàu vũ trụ 24/25/26 |
| 7 | **Chạy khỏi vết nứt** | Vết nứt nuốt map — thoát trong 3 phút | 4/12/18 → 24/25/26 | **B12**, A6, A3 | Tàu vũ trụ, **Mảnh Ký Ức #1** |

### Chương 2 — Kẻ trộm ký ức (NV 8–15)

| NV | Tên | Tóm tắt | Map chính | Bước | Mở khóa |
|---|---|---|---|---|---|
| 8 | Máy dò ký ức | Bunma chế máy dò, cần vật liệu | 84 Siêu Thị | A3, **B4**, A1 | Shop siêu thị |
| 9 | Chuyến bay đầu tiên | Bay tới Đảo Kamê / Guru / Vách núi đen | 5/13/20 | A6, A1, A3 | Map sư phụ |
| 10 | Bái sư | Sư phụ nhận làm đệ tử, dạy kỹ năng | 5/13/20 | A3, **B9**, A5 (250.000) | Học skill, sách kỹ năng |
| 11 | Hạt giống hy vọng | Trồng và thu hoạch đậu thần | 21/22/23 Nhà | **B7**, A12, A3 | Cây đậu thần |
| 12 | Bạn đồng hành | Nhận đệ tử đầu tiên | 21/22/23 | **B8**, A1, A3 | Đệ tử |
| 13 | Không ai đi một mình | Vào bang hội, làm nhiệm vụ cùng bang | 153 Lãnh địa Bang Hội | A10, **B13**, A3 | Bang hội, phó bản bang |
| 14 | Chợ đen ký ức | Ký ức bị đem bán ở chợ | 84, 102 | A3, **B4**, A1 | Ký gửi |
| 15 | **Người bạn đã quên** | Đồng hành chương 2 bị xóa ký ức, quay lại đánh người chơi | 27/31/35 | A1, **A2 (boss)**, A3 | **Mảnh Ký Ức #2**, PVP |

### Chương 3 — Liên minh ba hành tinh (NV 16–23)

| NV | Tên | Tóm tắt | Map chính | Bước | Mở khóa |
|---|---|---|---|---|---|
| 16 | Dấu vết dẫn về phía Nam | Lần theo dấu máy dò | 29/33/37, 30/34/38 | A6, A1, A4 | Map 27–38 |
| 17 | Rèn lại vũ khí | Bà Hạt Mít dạy nâng cấp đồ | 5/13/20, 42/43/44 | A3, **B2**, A12 | Nâng cấp trang bị |
| 18 | Tapion | Gặp Tapion — người đến từ quá khứ đã bị xóa | 19 Thành phố Vegeta, 126 | A6, A3, A1 | Map 6/10/19 |
| 19 | Trại lính hoang | Trại Nappa bị chiếm bởi quái mất trí | 68–72 | A1, A1, **B13** | Map 63–77 |
| 20 | Kẻ săn tiền thưởng | Granola đề nghị hợp tác | 160 Khu hang động | A3, A2 (Kuku/Mập Đầu Đinh/Rambo), A3 | Boss Nappa |
| 21 | Doanh trại Độc Nhãn | Phá doanh trại để lấy bản đồ | 53–62 | A3, **B1**, A3 | Phó bản doanh trại |
| 22 | Tiểu đội sát thủ | Tiểu đội bị Heart mua chuộc | 79, 81–83 | A1, A2 (Tiểu đội trưởng), A4 | Map 79–83 |
| 23 | **Fide đại ca** | Cao trào chương 3 — 3 form Fide | 80 Núi khỉ vàng | A5, **A2 (Fide -28)**, A3 | **Mảnh Ký Ức #3**, map 80 |

### Chương 4 — Cỗ máy và những bản sao (NV 24–31)

| NV | Tên | Tóm tắt | Map chính | Bước | Mở khóa |
|---|---|---|---|---|---|
| 24 | Tín hiệu lạ từ phương Bắc | Máy dò bắt sóng cơ khí | 92–96 | A6, A1, A3 | Map 92–96, 102 |
| 25 | Android đầu tiên | Dr.Kôrê và Android 19 | 93/94/96 | A2 (-31), A4, A3 | Boss Android |
| 26 | Kim loại và ký ức | Nâng sao pha lê cho trang bị | 5/13/20 | **B3**, A12, A3 | Pha lê hóa, ép sao |
| 27 | Ba cỗ máy | Android 13/14/15 ở sân sau siêu thị | 104 | A6, A2 (-33), A3 | Map 104 |
| 28 | King Kong | Cỗ máy khổng lồ canh phòng thí nghiệm | 97–99 | A1, A2 (-37), A4 | Map 97–100 |
| 29 | Phòng thí nghiệm Myuu | Đột nhập, thấy bản thiết kế bản sao | 166 Phòng thí nghiệm Myuu | A6, **B12**, A3 | Map 166 |
| 30 | Xên bọ hung | Sản phẩm hoàn thiện của Myuu | 100 Thị trấn Ginder | A1 (xên con), A2 (-100), A3 | Map 103 |
| 31 | **Bản sao của chính ngươi** | Potage tiết lộ bản sao; đánh bản sao chính mình | 140, 103 Võ đài Xên | A3, **A2 (bản sao)**, A5 | **Mảnh Ký Ức #4**, nhân bản |

### Chương 5 — Vương triều bóng tối (NV 32–39)

| NV | Tên | Tóm tắt | Map chính | Bước | Mở khóa |
|---|---|---|---|---|---|
| 32 | Lời cảnh báo của Bardock | Bardock thấy trước kết cục | 160 | A3, A1, A4 | Map 156–163 |
| 33 | Phá vỡ giới hạn | Học mở giới hạn sức mạnh | 43 / 50 | A3, **B10**, A5 | Mở giới hạn, NPC Quốc Vương / Tổ Sư Kaio |
| 34 | Vùng đất băng giá | Cooler giữ mảnh ký ức đóng băng | 105–110 | A6, A1, A2 (-29) | Map 105–110 |
| 35 | Con đường rắn độc | Phó bản bang để tới thế giới bên kia | 141–144 | **B1**, A1, A3 | Phó bản CĐRĐ |
| 36 | Cổng phi thuyền | Babiđây mở cổng, Mabư thức giấc | 114–120 | A6, A2 (Drabura), A3 | Phó bản Mabư 12h |
| 37 | Mabư | Hạ Mabư ở tầng cuối | 120, 127 | A2 (Mabư), A4, A3 | Phó bản Mabư 14h |
| 38 | Black Goku | Kẻ mang khuôn mặt quen thuộc | 92–100, 102 | A1, A2 (-203), A3 | Boss Black Goku |
| 39 | **Cái giá của ký ức** | Đồng minh chọn quên để cứu người chơi | 85–91 | **B6** (ước rồng), A2 (Baby -925), A3 | **Mảnh #5 & #6**, ngọc rồng sao đen |

### Chương 6 — Lõi Hư Không (NV 40–47)

| NV | Tên | Tóm tắt | Map chính | Bước | Mở khóa |
|---|---|---|---|---|---|
| 40 | Tổ Sư Kaio | Sự thật về Lõi Hư Không | 50/116 Thánh địa Kaio | A6, A3, A12 | Map thánh địa |
| 41 | Cơn thịnh nộ Broly | Broly phát điên vì ký ức bị xóa | 27–38 | A1, A2 (Broly -1822), A2 (Super Broly) | Boss Broly |
| 42 | Hành tinh ngục tù | Cumber bị Heart nuôi làm vũ khí | 155 | A6, **B12**, A2 (-203999) | Map 155 |
| 43 | Khí gas hủy diệt | Dr Lychee thức giấc theo Heart | 147–152 | **B1**, A2 (Hatchiyack), A3 | Phó bản khí gas |
| 44 | Thử thách của Thần Hủy Diệt | Bill không cứu, chỉ thử | 154 Hành tinh Bill | A3, **B5**, A3 | Map 154, Whis |
| 45 | Bảy mảnh hợp nhất | Ghép 7 Mảnh Ký Ức | 78 Lãnh địa Fize | A4, A12, **B13** | Mảnh Ký Ức #7 |
| 46 | Heart | Đối đầu phản diện chính | 166 → 145 Võ Đài Siêu Cấp | A2, A2, A3 | Boss Heart |
| 47 | **Trả lại hay giữ lấy** | Lựa chọn cuối: trả ký ức cho vũ trụ hay giữ Lõi | 145 | **B14** (nhánh), A2, A3 | Danh hiệu kết thúc, tuyến hậu truyện |

> NV 46 cần **một boss mới "Heart"** dựng từ `BossesData` (NPC 108 đã có sẵn tạo hình). Không cần tài nguyên client mới.

---

## 6. Đường cong sức mạnh & phần thưởng

### 6.1 Mốc sức mạnh mục tiêu khi kết thúc từng nhiệm vụ

| NV | SM mục tiêu | NV | SM mục tiêu | NV | SM mục tiêu |
|---|---|---|---|---|---|
| 0–3 | 1.000 → 18.000 | 16–19 | 5M → 20M | 32–35 | 2,5 tỷ → 4 tỷ |
| 4–7 | 25.000 → 65.000 | 20–23 | 30M → 100M | 36–39 | 5 tỷ → 8 tỷ |
| 8–11 | 100.000 → 500.000 | 24–27 | 200M → 500M | 40–43 | 10 tỷ → 14 tỷ |
| 12–15 | 800.000 → 2M | 28–31 | 800M → 2 tỷ | 44–47 | 16 tỷ → 18 tỷ + mở giới hạn |

### 6.2 Công thức thưởng đề xuất

Thay `rewardDoneTask` hiện tại (hardcode `switch`) bằng **bảng dữ liệu** `task_main_reward` (§9.2):

> **Nhiệm vụ chính KHÔNG thưởng vàng, ngọc hay hồng ngọc.** Chỉ thưởng sức mạnh, tiềm năng và vật phẩm. Tiền để người chơi tự kiếm qua đánh quái, phó bản, bán đồ.

| Chương | SM & TN mỗi nhiệm vụ | Vật phẩm tiêu biểu |
|---|---|---|
| 1 (0–7) | 2.000 → 20.000 | Đậu thần cấp 1 (13), Gói 10 viên Capsule (193), trang bị cấp 1, rada |
| 2 (8–15) | 50.000 → 600.000 | Sách kỹ năng lv1 (94/101/108), Gói 30 đậu thần cấp 3 (295), Thỏi vàng (457) |
| 3 (16–23) | 2M → 30M | Đá nâng cấp cấp 1–3 (1074–1076), Capsule Vàng (574), trang bị bộ |
| 4 (24–31) | 60M → 500M | Đá nâng cấp cấp 4–5 (1077–1078), sao pha lê, đá bảo vệ |
| 5 (32–39) | 800M → 3 tỷ | Đậu thần cấp 8 (352), bông tai, sao pha lê cao cấp |
| 6 (40–47) | 4 tỷ → 8 tỷ | Đồ Thần Linh, Ngọc Rồng (14–20), danh hiệu |

> Con số cụ thể cho từng nhiệm vụ nằm trong 3 file chi tiết. Mọi thưởng đều đi qua `Service.addSMTN` nên vẫn bị giới hạn sức mạnh chặn — đó là lý do NV 33 dạy mở giới hạn trước khi chương 5 trả thưởng lớn.
>
> **Hệ quả cần xử lý:** vài chỗ trong thiết kế trước đây dựa vào việc nhiệm vụ phát tiền — mở giới hạn nhanh ở NV 33 tốn 50 triệu vàng, và pha lê hóa ở NV 26 tốn vàng + ngọc mỗi lần thử. Vì nhiệm vụ không phát tiền nữa, hai chỗ này phải chuyển sang **trao thẳng vật phẩm/hiệu quả** (xem §10 mục 10–11).

### 6.3 Thưởng theo bước (không chỉ theo nhiệm vụ)

Tuyến cũ chỉ thưởng khi xong cả nhiệm vụ. Tuyến mới thưởng **mỗi bước** một khoản nhỏ (10% mức nhiệm vụ) để người chơi thấy phản hồi liên tục — dùng lại đúng `addDoneSubTask`.

---

## 7. Nhánh lựa chọn

Vì tiến độ chỉ lưu `[id, index, count]`, nhánh được làm bằng **task id riêng**, giống cách tuyến cũ tách nhiệm vụ 4/5/6 theo hành tinh.

| Điểm rẽ | Ở NV | Lựa chọn | Task id nhánh | Hội tụ tại |
|---|---|---|---|---|
| 1 | 20 | Hợp tác với **Granola** (săn tiền thưởng) / Báo cáo **Jaco** (luật pháp) | 20 / 48 | NV 21 |
| 2 | 31 | Tiêu diệt bản sao / Thu nhận bản sao làm đồng minh | 31 / 49 | NV 32 |
| 3 | 47 | **Trả ký ức** cho vũ trụ / **Giữ Lõi Hư Không** | 47 / 50 | Kết thúc (2 danh hiệu khác nhau) |

Khác biệt giữa hai nhánh: lời thoại, 1 bước khác nhau, và phần thưởng cùng giá trị nhưng khác loại (ví dụ nhánh Granola thưởng đá nâng cấp, nhánh Jaco thưởng ngọc). **Không nhánh nào mạnh hơn nhánh nào** — tránh chuyện ai cũng chọn một đường.

Bảng chuyển tiếp thay cho `sendNextTaskMain` hiện tại:

```
id 20 → 21      id 48 → 21
id 31 → 32      id 49 → 32
id 47 → hết     id 50 → hết
còn lại → id + 1
```

---

## 8. Khóa map / tính năng theo tuyến mới

Mọi mốc `TASK_x_y` trong bảng dưới phải được sửa trong code (§9.3). Đây là phần **dễ gây kẹt người chơi nhất** nếu bỏ sót.

| Map / tính năng | Mốc cũ | Mốc mới |
|---|---|---|
| 1, 8, 15 Đồi | TASK_1_0 | TASK_2_0 |
| 42, 43, 44 Vách núi | TASK_2_0 | TASK_3_0 |
| 2, 9, 16 | TASK_3_0 | TASK_4_0 |
| 3, 11, 17 Rừng | TASK_7_0 | TASK_5_0 |
| 24, 25, 26 Trạm tàu vũ trụ | TASK_4_0 | TASK_7_1 |
| 84 Siêu Thị | (không khóa) | TASK_8_0 |
| 5, 13, 20 Map sư phụ | (không khóa) | TASK_9_0 |
| 153 Lãnh địa Bang Hội | (không khóa) | TASK_13_0 |
| Mời / vào bang hội | TASK_10_0 | TASK_13_0 |
| 102 Nhà Bunma | TASK_21_0 | **TASK_14_0** (NV 14 diễn ra ở đây) |
| 27, 31, 35 | TASK_13_0 | **TASK_14_2** (NV 15 diễn ra ở đây) |
| 28–30, 32–34, 36–38 | TASK_13_0 / TASK_15_0 | TASK_16_0 |
| 6, 10, 19 | TASK_16_0 | TASK_18_0 |
| 63–77 | TASK_18_0 / TASK_19_0 | TASK_19_0 |
| 79, 81, 82, 83 | TASK_19_0 | TASK_22_0 |
| 80 Núi khỉ vàng | TASK_20_0 | TASK_23_0 |
| 92–96, 102 | TASK_21_0 | TASK_24_0 |
| 104 Sân sau siêu thị | (không khóa) | TASK_27_0 |
| 97–100 | TASK_24_0 | TASK_28_0 |
| 166 Phòng thí nghiệm Myuu | (không khóa) | TASK_29_0 |
| 103 Võ đài Xên | TASK_27_0 | TASK_30_0 |
| 105–110 | TASK_27_0 | TASK_34_0 |
| 155 Hành tinh ngục tù | (không khóa) | TASK_42_0 |
| 154 Hành tinh Bill | TASK_27_0 | **TASK_42_0** — lối vào map 155 đi qua map 154, nếu để TASK_44_0 thì NV 42 không khởi động được. Menu thử thách của Bill vẫn khóa tới TASK_44_0 |
| NPC Ca Lích | TASK_20_0 / TASK_21_0 | TASK_24_0 |
| Item 78 "Đứa bé" hiện trên map | TASK_3_1 | bỏ (tuyến mới không dùng) |
| Rơi Đùi gà (73) | TASK_2_0 | thay bằng Mảnh vỡ ở TASK_2_2 |
| Tàu Pảy Pảy nhận sát thương thật | TASK_10_1 | TASK_10_1 (giữ, đổi nội dung) |

Đồng thời sửa luôn 3 lỗi đã ghi ở [12-nhiem-vu-chinh.md](../1-he-thong-hien-tai/12-nhiem-vu-chinh.md): thiếu `break` ở map 80, bước "vào bang" hoàn thành khi chỉ nói chuyện sư phụ, và mốc 40.000 dùng nhầm cho bước cần 500.000.

---

## 9. Kế hoạch triển khai

### 9.1 Thứ tự làm

| Bước | Việc | Sản phẩm |
|---|---|---|
| 1 | Bạn duyệt thiết kế này + 3 file chi tiết | — |
| 2 | Sinh dữ liệu nhiệm vụ | `sql/quest_v2_data.sql`: xóa và nạp lại `task_main_template`, `task_sub_template`, thêm `task_main_reward` |
| 3 | Thêm item mới (Mảnh Ký Ức, tín vật) | `sql/quest_v2_items.sql` + tăng version data |
| 4 | Sinh lại `ConstTask.java` | Hằng số bước cho 51 task id (48 + 3 nhánh) |
| 5 | Viết lại `TaskService.java` | Trigger A1–A12 theo bảng mới, thêm B1–B14 |
| 6 | Bảng thưởng đọc từ DB | Thay `rewardDoneTask` hardcode |
| 7 | Cập nhật các mốc khóa map/tính năng | `ChangeMapService`, `ClanService`, `NpcManager`, `Zone`, `Mob`, `Calick`, `Cui` |
| 8 | Boss mới "Heart" + bản sao NV 31 | `BossesData` + class boss |
| 9 | Reset & bù thưởng người chơi | `sql/quest_v2_migration.sql` |
| 10 | Chạy thử 3 hành tinh, cả 3 nhánh | Nhật ký test |

### 9.2 Bảng mới đề xuất: `task_main_reward`

Để không bao giờ lặp lại chuyện "chữ trong DB khác thứ server thưởng":

| Cột | Kiểu | Ý nghĩa |
|---|---|---|
| `task_id` | int | id nhiệm vụ (hoặc bước, nếu `sub_index >= 0`) |
| `sub_index` | int | -1 = thưởng khi xong cả nhiệm vụ |
| `sm` / `tn` | bigint | Sức mạnh / tiềm năng |
| `gold` / `gem` / `ruby` | bigint | Tiền |
| `items` | text | JSON `[[itemId, số lượng, [option...]], ...]` |
| `text` | varchar | Dòng mô tả hiển thị, sinh tự động từ các cột trên |

### 9.2a ⚠️ RÀNG BUỘC CỨNG: id vật phẩm phải liên tục, không được để trống

Đã kiểm chứng trong code:

- `ItemService.getTemplate(id)` = `Manager.ITEM_TEMPLATES.get(id)` — lấy theo **vị trí trong danh sách**, không tra theo id (`services/ItemService.java:360`).
- `ItemData.java` gửi bảng item xuống client **theo thứ tự, không gửi kèm id** (`data/ItemData.java:48–86`).

⇒ Chỉ cần thiếu một id (ví dụ có 2001 mà không có 2000) là **toàn bộ bảng item từ đó trở đi lệch một bậc**: server trả nhầm vật phẩm, client hiện sai tên và icon, món cuối gây lỗi tràn mảng.

⇒ **Dải id rải rác ở §9.2b bên dưới KHÔNG dùng được như hiện tại.** Trước khi viết SQL phải đánh số lại toàn bộ vật phẩm mới thành một khối **liên tục từ 2000**, hoặc chèn đủ dòng trống để lấp kín mọi khoảng hở. Chi tiết và phương án ở [21b-item-moi-dac-ta.md](../3-kiem-tra-can-bang/21b-item-moi-dac-ta.md).

Kèm theo: mã nguồn hiện đã hardcode 23 id ≥ 2000 **không tồn tại trong DB** (di sản từ bản server khác). Chúng đang nằm im, nhưng sẽ **sống lại ngay khi thêm item ≥ 2000** và có thể phát nhầm vật phẩm nhiệm vụ. Phải rà và vá trước — danh sách đầy đủ ở 21b.

### 9.2b Dải id vật phẩm mới — bảng chốt (CẦN ĐÁNH SỐ LẠI, xem 9.2a)

Ba file chi tiết ban đầu đặt id chồng nhau; dưới đây là dải đã chuẩn hóa, **các file 20a/20b/20c đã được sửa theo bảng này**. Khi viết SQL phải theo đúng đây.

| Dải id | Dùng cho | File |
|---|---|---|
| 2001–2006 | Vật phẩm nhiệm vụ chương 1–2 (Mảnh Vỡ Hư Không, Kỷ Vật Của Ông, Máy Dò Ký Ức, Hộp Ký Ức, Hạt Giống Hy Vọng) | 20a |
| **2010–2016** | **7 Mảnh Ký Ức #1 → #7** (xuyên suốt cả tuyến) | 20a (#1–2), 20b (#3–4), 20c (#5–7) |
| 2040–2099 | Vật phẩm nhiệm vụ chương 3–4 | 20b |
| 2100–2119 | Vật phẩm nhiệm vụ chương 5–6 (Lõi Ký Ức, Lõi Hư Không, Vỏ Lõi rỗng…) | 20c |
| 2120–2121 | 2 danh hiệu kết thúc | 20c |

Mọi item mới đều mượn icon của item đã có (ghi rõ trong từng file), và đều có phương án thay bằng item sẵn có nếu bạn không muốn cập nhật data client.

### 9.3 Ước lượng khối lượng code

| Việc | Ước lượng |
|---|---|
| `ConstTask.java` sinh lại | tự sinh, không tính |
| `TaskService.java` | viết lại khoảng 1.200 dòng |
| Trigger mới B1–B14 | khoảng 25 điểm móc, mỗi chỗ 2–5 dòng |
| Sửa mốc khóa map/tính năng | khoảng 30 chỗ |
| Bảng thưởng + DAO | khoảng 200 dòng |
| Boss Heart + bản sao | khoảng 250 dòng |
| SQL | khoảng 400 câu INSERT, sinh bằng script |

### 9.4 Reset và bù thưởng người chơi cũ

```sql
-- Bù theo tiến độ cũ rồi đưa tất cả về nhiệm vụ 0
-- (chi tiết trong quest_v2_migration.sql; bảng bù theo task id cũ 0..29)
UPDATE player SET data_task = '[0,0,0,0]';
```

Kèm thư trong game: giải thích tuyến nhiệm vụ mới, và quà bù = tổng thưởng tuyến cũ của phần đã chơi + 1 gói cảm ơn.

**Bắt buộc sao lưu DB trước khi chạy.**

---

## 10. Rủi ro & điểm cần bạn quyết

| # | Vấn đề | Đề xuất |
|---|---|---|
| 1 | **Item mới cần client tải lại data.** Mảnh Ký Ức, tín vật… là item chưa có | Nếu bạn ngại đụng data client: dùng item sẵn có làm vật phẩm nhiệm vụ (ví dụ Ngọc Rồng 1–7 sao làm 7 Mảnh Ký Ức). Tôi cần bạn chốt |
| 2 | **Boss Heart là boss mới** | Dựng từ tạo hình NPC 108 đã có, không cần tài nguyên mới |
| 3 | **Lời thoại dài** hiển thị trên client cũ có thể bị cắt | Giới hạn mỗi câu ≤ 120 ký tự, đã áp dụng trong file chi tiết |
| 4 | Nhiều nhiệm vụ chương 5–6 phụ thuộc **phó bản theo giờ** (Mabư 12h/14h) → người chơi giờ khác bị kẹt | NV 36, 37 có đường vòng: nếu ngoài giờ, nhận nhiệm vụ thay thế từ NPC Ôsin |
| 5 | **13 lỗi code cũ chặn cứng tuyến mới** (bảng đầy đủ ở [20c §7.1](20c-chi-tiet-nhiem-vu-32-47.md)). Nặng nhất: Cooler, Baby, Broly, Dr Lychee không gọi `checkDoneTaskKillBoss` khi chết → NV 34/39/41/43 không bao giờ xong; menu Ôsin không xuống được tầng Mabư → NV 36 kẹt; NPC Đại Thiên Sứ (64) menu rỗng → NV 45, 46 và **điểm rẽ nhánh cuối** đều kẹt | Sửa nhóm lỗi này **trước** khi bật tuyến mới. Đây là việc bắt buộc, không phải tùy chọn |
| 5b | `BossData.hp` là `int[]` nên **HP boss trần 2,147 tỷ** | Boss Heart dừng ở 2 tỷ mỗi form; muốn dai hơn thì giới hạn sát thương nhận, đừng tăng HP |
| 6 | Reset gây phản ứng từ người chơi đang chơi | Thông báo trước 3–7 ngày + quà bù |
| 7 | **NV 12 bắt người chơi có đệ tử, nhưng server không có nguồn đệ tử miễn phí** (chỉ rơi từ Super Broly, gói VIP tiền thật, hoặc admin phát) | Cho NPC sư phụ tặng 1 đệ tử ở NV 12, hoặc đổi NV 12 thành nhiệm vụ khác. **Cần bạn chốt** |
| 8 | **Quái chương 2 quá yếu so với mốc 2 triệu sức mạnh** (quái mẹ chỉ 1.000 HP) | Tăng level quái ở map chương 2 trong `map_template`, hoặc hạ mốc SM chương 2 xuống ~800.000 |
| 10 | **NV 33 mở giới hạn sức mạnh nhanh tốn 50 triệu vàng**, mà nhiệm vụ không phát vàng nữa | Cho NPC Quốc Vương mở giới hạn **miễn phí một lần** khi người chơi đang ở bước NV 33, hoặc trao 1 vật phẩm "Giấy phép phá giới hạn" dùng thay tiền |
| 11 | **NV 26 pha lê hóa tỉ lệ thật chỉ 50%**, mỗi lần thử tốn vàng + ngọc | Trao đủ nguyên liệu bằng vật phẩm và cho bước tính là xong nếu trang bị đã có sẵn lỗ pha lê, để không ai kẹt vì hết tiền |
| 11b | **NV 8 và NV 14 bắt người chơi mua đồ ở shop**, mà chương 1–2 không còn nguồn vàng nào từ nhiệm vụ | Người chơi vẫn nhặt được vàng rơi từ quái, nhưng nên kiểm tra thực tế: nếu tân thủ không đủ tiền mua Rada cấp 1 thì đổi bước "mua" thành "NPC tặng", hoặc tăng vàng rơi ở map chương 1 |
| 12 | Tuyến 48 nhiệm vụ dài — người chơi lười có thể bỏ giữa chừng | Mỗi chương xong có 1 phần thưởng lớn thấy rõ; cân nhắc thêm "bỏ qua nhiệm vụ" bằng ngọc ở chương 1–2 |

### Ba câu cần bạn trả lời trước khi tôi viết code

1. **Vật phẩm nhiệm vụ**: thêm item mới (đẹp hơn, phải cập nhật data client) hay dùng item có sẵn (an toàn, không cần đụng client)?
2. **Độ dài cày**: mốc sức mạnh ở §6.1 là dựa trên server tỉ lệ EXP = 3 hiện tại. Bạn muốn người chơi mất bao lâu để xong tuyến — 2 tuần, 1 tháng, hay 3 tháng?
3. **Nhánh lựa chọn**: giữ 3 điểm rẽ như §7, hay bạn muốn ít hơn cho đỡ rắc rối khi bảo trì?
