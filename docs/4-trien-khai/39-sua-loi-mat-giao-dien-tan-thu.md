# 39 — Sửa lỗi nhân vật mới mất hết giao diện (hướng dẫn tân thủ)

> **Lỗi chặn phát hành:** nhân vật tạo mới vào game không có thanh máu, thanh năng lượng, thanh kỹ năng hay nút menu (hành trang…). Màn hình chỉ còn bản đồ, NPC, vàng/ngọc và một ngón tay chỉ đường.

---

## 1. Nguyên nhân

Client game (Unity, đã biên dịch, **không sửa được**) có một **chế độ hướng dẫn tân thủ viết cứng** theo đúng cấu trúc các nhiệm vụ đầu của **tuyến gốc**. Khi nhân vật ở nhiệm vụ 0, client ẩn toàn bộ giao diện rồi mở dần theo từng bộ (id nhiệm vụ, chỉ số bước, npc/map của bước).

Tuyến "Vết Nứt Hư Không" đã đổi số bước, thứ tự và loại bước của NV 0–3. Client vì thế lệch nhịp:

- giao diện không bao giờ được mở lại, vì client chờ những bước không còn tồn tại;
- tệ hơn, bước 4 của NV 0 mới bắt **"Ăn một hạt đậu thần"**, tức phải mở túi đồ, mà túi đồ đang bị ẩn. **Người mới kẹt vĩnh viễn.**

Nhóm trước còn bỏ đoạn "giải cứu" trong `Player.update`. Đoạn này đẩy người đang đứng ở nhà mà còn kẹt ở bước đầu sang bước "Nói chuyện với ông".

**Cách sửa:** trả **nguyên cơ chế** NV 0–3 về tuyến gốc: cùng số bước, cùng thứ tự, cùng `max_count`, `npc_id`, `map` (kể cả các placeholder âm), cùng loại điều kiện, cùng vật phẩm nhiệm vụ và cùng thao tác trao/thu vật phẩm. **Chỉ đổi chữ hiển thị** (tên bước, `detail`, `notify`, lời thoại) để hợp với cốt truyện mới. NV 4 trở đi giữ nguyên.

---

## 2. Bảng NV 0–3 trước và sau

Cột "Sau" có cơ chế giống hệt dump gốc `database team2026.sql`, chỉ khác chữ.

### NV 0 — "Người duy nhất còn nhớ" (6 bước)

| # | Trước (tuyến mới, lỗi) | Sau: tên hiển thị | max, npc, map | Điều kiện (y nguyên gốc) |
|---|---|---|---|---|
| 0 | Đi về nhà %2 (vào map 21/22/23) | Đi tới mũi tên chỉ dẫn | 1, -1, -1 | `checkDoneTaskGoToMap`: map 39/40/41 và `x ≥ 635` |
| 1 | Lấy đồ trong rương | Về nhà %2 ở bên phải | 1, -2, -2 | vào map 21/22/23 |
| 2 | Nói chuyện với %2 | Nói chuyện với %2 | 1, -2, -2 | nói chuyện ông (NPC 0/2/1) |
| 3 | Xem cây đậu thần (bấm mục bất kỳ) | Mở rương đồ | 1, 3, -2 | `checkDoneTaskGetItemBox` |
| 4 | **Ăn một hạt Đậu thần** (gây kẹt) | Thu hoạch đậu thần | 1, 4, -2 | menu cây đậu `MAGIC_TREE_NON_UPGRADE_LEFT_PEA / FULL_PEA`, chọn mục 0 |
| 5 | — | Báo cáo với %2 | 1, -2, -2 | nói chuyện ông |

### NV 1 — "Bài học của ông" (2 bước)

| # | Trước | Sau | max, npc, map | Điều kiện |
|---|---|---|---|---|
| 0 | Đập vỡ 10 mộc nhân ở %1 (chỉ ở làng) | Đánh ngã 5 mộc nhân | 5, -1, -1 | hạ mob 0 ở bất kỳ map nào |
| 1 | Về khoe với %2 | Về khoe với %2 | 1, -2, -2 | nói chuyện ông |
| 2 | Cộng điểm tiềm năng lần đầu | *(bỏ)* | | |

### NV 2 — "Vết nứt đầu tiên" (2 bước)

| # | Trước | Sau | max, npc, map | Điều kiện |
|---|---|---|---|---|
| 0 | Lên %3 (vào map) | Nhặt 10 đùi gà | 10, -1, -3 | nhặt item **73**; `Mob.dropItemTask` rơi 73 khi `TASK_2_0` |
| 1 | Tiêu diệt 20 %4 | Mang đùi gà về cho %2 | 1, -2, -2 | nói chuyện ông → **trừ 10 item 73**, hiện **item 74** (đùi gà nướng) |
| 2 | Nhặt Mảnh Vỡ Hư Không (2009) | *(bỏ)* | | |

### NV 3 — "Cảnh sát vũ trụ Jaco" (3 bước)

| # | Trước | Sau | max, npc, map | Điều kiện |
|---|---|---|---|---|
| 0 | Gặp Jaco ở %5 (map 42–44) | Sử dụng tiềm năng | 1, -1, -1 | `checkDoneTaskUseTiemNang` |
| 1 | Đánh 20 %4 | Đi xem vật thể lạ vừa rơi | 1, -1, **-4** | nhặt item **78**, chỉ hiện khi `TASK_3_1` |
| 2 | Nâng sức đánh gốc lên 30 | Báo cáo với %2 | 1, -2, -2 | nói chuyện ông → **trừ item 78**, gửi lại cờ túi |

Cốt truyện mới được giữ bằng lời thoại. Ông gọi nhầm tên người chơi ("Kairo?"). Lũ thú trên đồi phát điên sau khi bầu trời rách một đường trắng. "Vật thể lạ" là con tàu của cảnh sát vũ trụ Jaco bị rơi, bên trong có một đứa bé cũng không nhớ mình là ai. Cuối NV 3, ông báo lũ thú mẹ đang bò ra từ vết nứt, nối vào NV 4 "Thứ bò ra từ vết nứt". Không đoạn chữ nào dùng ký tự `#`.

**Phần thưởng** vẫn đọc từ `task_main_reward` và vẫn theo nguyên tắc **chỉ sức mạnh, tiềm năng và vật phẩm**. Dòng thưởng hoàn thành (`sub_index = -1`) giữ nguyên. Dòng thưởng theo bước được nạp lại cho khớp số bước mới: NV 0 có 6 × 200, NV 1 có 2 × 300, NV 2 có 2 × 400, NV 3 có 3 × 500 (SM và TN).

---

## 3. Thay đổi code

| File | Hàm / chỗ sửa | Nội dung |
|---|---|---|
| `consts/ConstTask.java` | hằng mới | `MAP_VACH_NUI_LANG = -10` |
| `services/TaskService.java` | `checkDoneTaskTalkNpc` (ông) | thêm lại `TASK_0_5`, `TASK_2_1`, `TASK_3_2` (giữ `TASK_0_2`, `TASK_1_1` và các bước NV 4+) |
| | `checkDoneTaskTalkNpc` (Jaco) | bỏ `TASK_3_0` ở vách núi |
| | `checkDoneTaskTalkNpc` (Bà Hạt Mít, Quốc Vương) | `isMapVachNui` → `isMapVachNuiLang` (42/43/44) |
| | `checkDoneTaskGetItemBox` | `TASK_0_1` → `TASK_0_3` |
| | `checkDoneTaskUseTiemNang` | `TASK_1_2` → `TASK_3_0` |
| | `checkDoneTaskNangCS` | bỏ mốc `dameg ≥ 30 → TASK_3_2` (nay `TASK_3_2` là bước nói chuyện) |
| | `checkDoneTaskUseItem` | bỏ `case 13 → TASK_0_4` (bước ăn đậu gây kẹt) |
| | `checkDoneTaskGoToMap` | khôi phục `39/40/41 && x ≥ 635 → TASK_0_0` và `21/22/23 → TASK_0_1`; bỏ `1/8/15 → TASK_2_0`; nhánh 42/43/44 (`TASK_33_0`) dùng `isMapVachNuiLang` |
| | `checkDoneTaskPickItem` | khôi phục `73 → TASK_2_0` và `78 → TASK_3_1`; bỏ `2009 → TASK_2_2` |
| | `checkDoneTaskConfirmMenuNpc` (Đậu thần) | chỉ xong `TASK_0_4` khi menu còn đậu/đầy đậu và chọn mục 0 |
| | `checkDoneTaskKillMob` | mộc nhân tính ở mọi map (`TASK_1_0`); bỏ `TASK_2_1`, `TASK_3_1` ở quái 1/2/3 |
| | `doneTask` | khôi phục khối `TASK_0_0 … TASK_3_2`: tutorial, `npcSay`, thông báo đếm mộc nhân, trừ 10 × 73 + hiện 74, trừ 78 + `sendFlagBag` (lời thoại mới) |
| | `transformMapId` | `-4` → **39/40/41** (như gốc); `-10` → **42/43/44** |
| | `isMapVachNui` | đổi thành `isMapVachNuiLang` (placeholder -10) |
| `player/Player.java` | `update` | khôi phục đoạn giải cứu: đứng ở map `21 + gender` mà còn `TASK_0_0`/`TASK_0_1` thì `index = 2` và `sendTaskMain` (thêm kiểm tra null và số bước) |
| | `getFlagBag` | cờ túi 28 khi `== TASK_3_2` (gốc) **hoặc** `>= TASK_5_2` (tuyến mới) |
| `map/Zone.java` | `getItemMapsForPlayer` | khôi phục: item 78 chỉ hiện khi `TASK_3_1`, item 74 chỉ hiện từ `TASK_3_0` |
| `mob/Mob.java` | `dropItemTask` | khôi phục: quái 1/2/3 rơi đùi gà 73 khi `TASK_2_0`; bỏ nhánh 2009 ở `TASK_2_2` |
| `map/service/ChangeMapService.java` | `checkMapCanJoin` | khóa map 1/8/15 trả về `TASK_1_0`, map 42/43/44 trả về `TASK_2_0` (như gốc) |
| `npc_list/Jaco.java` | chú thích | ghi rõ `TASK_3_0` đã bỏ |

Đã kiểm tra, **không cần sửa**:

- `server/Controller.java`: lời chào tân thủ khi `getIdTask == TASK_0_0` vẫn còn.
- `database/PlayerDAO.createNewPlayer`: vẫn đặt nhân vật ở map `39 + gender`, `data_task = [0,0,0]`.
- `ChangeMapService.checkMapCanJoin`: **không** khóa 39/40/41, 21/22/23 hay 0/7/14.
- `PlayerService.playerMove` và `ChangeMapService` đều vẫn gọi `checkDoneTaskGoToMap`, nên điều kiện `x ≥ 635` được xét mỗi lần di chuyển.

---

## 4. Placeholder mới

| Placeholder | Hằng | Trả về (TĐ / NM / XD) | Dùng cho |
|---|---|---|---|
| **-4** | `MAP_VACH_NUI` | **39 / 40 / 41** (như gốc) | NV 3 bước 1 "vật thể lạ" |
| **-10** (mới) | `MAP_VACH_NUI_LANG` | **42 / 43 / 44** | 6 bước NV 4+ bên dưới |

Các bước NV 4+ đã chuyển từ `-4` sang `-10`. Chỉ đổi cột `map`, không đổi số bước:

| NV | Bước | ducvupro |
|---|---|---|
| 17 | 0 "Gặp Bà Hạt Mít ở %5", 1 "Nâng một trang bị lên +2" | 56, 57 |
| 33 | 0 "Tới vách núi của hành tinh bạn", 1 "Nói chuyện với Quốc Vương", 3 "Mở giới hạn sức mạnh", 5 "Báo cáo với Quốc Vương" | 135, 136, 138, 140 |

---

## 5. SQL

- `SRC/sql/patch/02-nhiem-vu-moi.sql` (**server cài mới**): NV 0–3 đã được thay bằng 13 bước gốc (ducvupro 1..13, số 14 bỏ trống). Thưởng theo bước đã được nạp lại, 6 bước NV 17/33 đổi sang `-10`, và câu kiểm tra 5.2 nay kỳ vọng **237** bước.
- `SRC/sql/patch/05-sua-huong-dan-tan-thu.sql` (**server đã import 02 bản cũ**): chỉ dùng cú pháp MariaDB 10.4 (`JSON_VALID`, `JSON_EXTRACT`, `CONCAT`; không có `JSON_TABLE`). File làm các việc sau:
  1. sao lưu `data_task` của người chơi NV 0–3 vào bảng `player_task_backup_05`;
  2. xóa rồi nạp lại `task_main_template` và `task_sub_template` của NV 0–3;
  3. xóa rồi nạp lại dòng `task_main_reward` theo bước (`sub_index >= 0`) của NV 0–3;
  4. `UPDATE … SET map = -10 WHERE task_main_id IN (17, 33) AND map = -4`;
  5. đưa **mọi người chơi ở NV 0–3** về `[taskId,0,0,0]`;
  6. chạy 8 câu kiểm tra K1–K8 (kỳ vọng ghi ngay trên từng câu).

  Chạy lại file này nhiều lần vẫn cho cùng một kết quả.

---

## 6. Triển khai lên server đang chạy

1. **Build lại jar** từ mã nguồn đã sửa. Lệnh biên dịch kiểm tra đã chạy sạch toàn bộ file `.java`.
2. **Tắt server** (bảo trì). Nếu server còn chạy, luồng tự lưu sẽ ghi đè `data_task` vừa sửa bằng chỉ số bước cũ trong bộ nhớ.
3. Sao lưu:
   `mysqldump -u root -p team2026 task_main_template task_sub_template task_main_reward player > backup_truoc_05_$(date +%F).sql`
4. Chạy `05-sua-huong-dan-tan-thu.sql`, rồi xem kết quả K1–K8. Chỉ đi tiếp khi tất cả đúng kỳ vọng.
5. Thay jar mới và **khởi động lại** server. `Manager` chỉ nạp lại nhiệm vụ lúc khởi động.
6. Thử với 3 nhân vật mới (TĐ, NM, XD). Giao diện phải mở dần: thanh máu và năng lượng sau khi lấy rađa ở rương, nút hành trang ở bước tương ứng. Sau đó đi hết NV 0–3.

---

## 7. Người chơi đang kẹt được giải cứu thế nào

- **Người ở NV 0 (đa số là người mới đang kẹt):** file 05 đặt họ về `[0,0,0,0]`. Khi đăng nhập lại:
  - Nếu họ đang đứng ở **nhà** (map `21 + gender`, rất thường gặp vì tuyến mới bắt về nhà), đoạn giải cứu trong `Player.update` thấy `TASK_0_0`, liền đặt `index = 2` ("Nói chuyện với %2") và gửi lại nhiệm vụ. Client hướng dẫn tân thủ bắt đúng nhịp từ đây.
  - Nếu họ đang ở vách núi 39/40/41, chỉ cần đi tới mũi tên (`x ≥ 635`) như tuyến gốc.
  - Nếu họ đang ở map khác: bước 0 dùng `map = -1`, nên client sẽ không chỉ đường. Xem mục 8.
- **Người ở NV 1–3:** về đầu nhiệm vụ đó. Họ mất tiến độ dở dang trong nhiệm vụ nhưng không mất nhiệm vụ đã xong. Họ có thể nhận lại thưởng các bước đầu của nhiệm vụ này (vài trăm SM/TN).
- **Người ở NV 4+:** không bị đụng tới. Người ở NV 17 / NV 33 chỉ thấy mũi tên chỉ đúng 42/43/44 như trước.

---

## 8. Chỗ nghi ngờ

1. **Item 78 nằm ở map 42/43/44, còn mũi tên của NV 3 bước 1 (`-4`) chỉ về 39/40/41.** `Map.initItem` rải item 78 ở 42/43/44, và `transformMapId` gốc trả `-4` về 39/40/41. Cả hai giữ **y nguyên như gốc** theo yêu cầu, vì client có thể dựa vào map này. Nếu người chơi phàn nàn mũi tên dẫn sai chỗ, đó là hành vi sẵn có của bản gốc. Không nên đổi khi chưa thử trên client thật.
2. **Người NV 0 đứng ở map lạ** (không phải nhà hay 39/40/41) sau khi reset: bước 0 cần đứng ở 39/40/41. Client gốc chỉ đưa người mới tới đó lúc tạo nhân vật. Trường hợp này hiếm, vì người mới bị ẩn giao diện chủ yếu đứng ở nhà hoặc vách núi. Nếu có, GM dịch chuyển họ về map `39 + gender` hoặc về nhà.
3. **Đoạn giải cứu đặt `index = 2` bỏ qua thưởng bước 0 và 1** (400 SM/TN), giống bản gốc.
4. **Map 1/8/15 và 42/43/44 mở sớm hơn thiết kế mới** vì khóa đã trả về mốc gốc `TASK_1_0` / `TASK_2_0`. Không ảnh hưởng NV 4+, vì các bước sau đều đến muộn hơn các mốc này.
5. **Item 2009 "Mảnh Vỡ Hư Không"** không còn bước nào dùng. Nó vẫn nằm trong `item_template` và bộ lọc `Zone` (vô hại).
6. **`checkDoneTaskNangCS`** hiện không còn bước nào dùng. Mốc cũ `TASK_27_0` của bản gốc không trả về, vì NV 27 mới là nhiệm vụ khác.
7. **Jaco vẫn đứng ở 42/43/44** (file `04-npc-tren-map.sql`) nhưng chỉ còn lời thoại, không gắn bước nào.
8. **Không thử được trên client thật trong phiên này.** Chỉ đảm bảo biên dịch sạch và dữ liệu khớp từng trường với dump gốc. Việc client mở lại giao diện dựa trên nguyên nhân đã xác định (hướng dẫn tân thủ viết cứng theo tuyến gốc).
