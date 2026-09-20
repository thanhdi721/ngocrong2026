# 26 — Cập nhật mốc khóa map & khóa tính năng sang tuyến nhiệm vụ mới

> **Nguồn mốc**: §8 của [20-thiet-ke-nhiem-vu-moi.md](../2-thiet-ke-nhiem-vu-moi/20-thiet-ke-nhiem-vu-moi.md),
> đối chiếu thêm với bảng chi tiết §E của [20a](../2-thiet-ke-nhiem-vu-moi/20a-chi-tiet-nhiem-vu-00-15.md),
> §5.4 của [20b](../2-thiet-ke-nhiem-vu-moi/20b-chi-tiet-nhiem-vu-16-31.md) và §7.4 của [20c](../2-thiet-ke-nhiem-vu-moi/20c-chi-tiet-nhiem-vu-32-47.md).
>
> **Trạng thái**: đã sửa code, biên dịch sạch 548 file bằng JDK 17.
>
> **Phạm vi KHÔNG đụng tới** (nhóm khác đang làm): `services/TaskService.java`, `consts/ConstTask.java`,
> `shop/ShopService.java`, `boss/**` (trừ `boss/luyen_tap_tu_dong/TauPayPay.java` được giao riêng), `database/**`.

---

## ⚠️ 1. CẢNH BÁO TRIỂN KHAI — ĐỌC TRƯỚC KHI ĐƯA LÊN SERVER

**TUYỆT ĐỐI KHÔNG đưa riêng đợt thay đổi này lên server production.**

Toàn bộ mốc trong tài liệu này chỉ đúng **sau khi dữ liệu nhiệm vụ mới đã được nạp**
(`task_main_template`, `task_sub_template`, `ConstTask.java` tuyến mới, `TaskService.java` viết lại,
và SQL reset `data_task` của người chơi cũ — xem §9 của file 20).

Nếu đưa lên một mình, hậu quả cụ thể:

| Hậu quả | Vì sao |
|---|---|
| **Người chơi đang chơi dở tuyến cũ bị khóa ngược** | Ví dụ người đang ở nhiệm vụ cũ 5 sẽ mất quyền vào map 3/11/17 (mốc mới `TASK_5_0` > mốc họ đang có), người ở nhiệm vụ cũ 20 mất quyền vào map 80. |
| **Map trước đây không khóa nay khóa cứng** | 84 Siêu Thị, 5/13/20 Map sư phụ, 153 Lãnh địa Bang Hội, 104 Sân sau siêu thị, 166 Phòng thí nghiệm Myuu, 155 Hành tinh ngục tù. Người chơi cũ mọi cấp độ sẽ mất truy cập cho tới khi tuyến mới nạp xong. |
| **Người chơi đang đứng trong map bị khóa sẽ bị đá về nhà khi đăng nhập** | `PlayerDAO` dòng ~424 gọi `checkMapCanJoin` lúc nạp nhân vật; không vào được thì đặt lại về `gender + 21`. |
| **Không ai vào được bang hội** | Điều kiện mời/vào bang nâng từ `TASK_10_0` lên `TASK_13_0`; theo đánh số tuyến cũ, đó là mốc người chơi tuyến cũ chưa chắc đạt. |
| **Rơi vật phẩm nhiệm vụ trống** | `Mob.dropItemTask` nay rơi item **2001 / 2002**, hai id này **chưa tồn tại trong DB**. Đã có chốt chặn (xem §4.2) nên server không sập, nhưng bước nhặt đồ sẽ không bao giờ xong. |

**Thứ tự triển khai bắt buộc** (khớp §9.1 của file 20): SQL nhiệm vụ + item mới → `ConstTask` →
`TaskService` → **tài liệu này** → migration reset `data_task`. Tất cả **trong cùng một lần bảo trì**.
**Sao lưu DB trước khi chạy.**

---

## 2. Bảng từng chỗ đã sửa

Số dòng lấy sau khi sửa. Mọi chỗ đều có comment `// TUYẾN MỚI:` hoặc `// FIX:` ngay tại dòng để tra ngược.

### 2.1 `map/service/ChangeMapService.java` — hàm `checkMapCanJoin`

| # | Dòng | Map | Mốc cũ | Mốc mới | Lý do |
|---|---|---|---|---|---|
| 1 | 891 | 1, 8, 15 Đồi | `TASK_1_0` | `TASK_2_0` | NV 2 "Vết nứt đầu tiên" diễn ra ở đồi |
| 2 | 899 | 42, 43, 44 Vách núi | `TASK_2_0` | `TASK_3_0` | NV 3 "Cảnh sát vũ trụ Jaco" |
| 3 | 907 | 2, 9, 16 | `TASK_3_0` | `TASK_4_0` | NV 4 "Thứ bò ra từ vết nứt" |
| 4 | 915 | 3, 11, 17 Rừng | `TASK_7_0` | `TASK_5_0` | NV 5 "Ký ức của ông" tìm item 2002 trong rừng |
| 5 | 923 | 24, 25, 26 Trạm tàu vũ trụ | `TASK_4_0` | `TASK_7_1` | NV 7 "Chạy khỏi vết nứt" kết thúc ở trạm tàu |
| 6 | 929 | **84 Siêu Thị** | *(không khóa)* | `TASK_8_0` | NV 8 "Máy dò ký ức" gặp Bunma ở siêu thị |
| 7 | 937 | **5, 13, 20 Map sư phụ** | *(không khóa)* | `TASK_9_0` | NV 9 "Chuyến bay đầu tiên" |
| 8 | 943 | **153 Lãnh địa Bang Hội** | *(không khóa)* | `TASK_13_0` | NV 13 "Không ai đi một mình" |
| 9 | 949 | 102 Nhà Bunma | `TASK_21_0` | `TASK_14_0` | NV 14 "Chợ đen ký ức" diễn ra ở map 102 — **tách khỏi nhóm 92–96** |
| 10 | 958 | 27, 31, 35 | `TASK_13_0` | `TASK_14_2` | NV 15 "Người bạn đã quên" — **tách khỏi nhóm 28–38** |
| 11 | 973 | 28, 29, 30, 32, 33, 34, 36, 37, 38 | `TASK_13_0` / `TASK_15_0` / *(29, 33, 37 không khóa)* | `TASK_16_0` | NV 16 "Dấu vết dẫn về phía Nam" |
| 12 | 982 | 6, 10, 19 | `TASK_16_0` | `TASK_18_0` | NV 18 "Tapion" (map 19 Thành phố Vegeta) |
| 13 | 1002 | 63–77 (gộp 2 nhóm cũ) | `TASK_18_0` (64, 65, 68–72) / `TASK_19_0` | `TASK_19_0` | NV 19 "Trại lính hoang" mở cả cụm một lượt |
| 14 | 1011 | 79, 81, 82, 83 | `TASK_19_0` | `TASK_22_0` | NV 22 "Tiểu đội sát thủ" |
| 15 | 1017 | 80 Núi khỉ vàng | `TASK_20_0` | `TASK_23_0` | NV 23 "Fide đại ca" — **kèm sửa lỗi thiếu `break`, xem §3.1** |
| 16 | 1026 | 92, 93, 94, 96 | `TASK_21_0` | `TASK_24_0` | NV 24 "Tín hiệu lạ từ phương Bắc" |
| 17 | 1033 | **104 Sân sau siêu thị** | *(không khóa)* | `TASK_27_0` | NV 27 "Ba cỗ máy" |
| 18 | 1042 | 97, 98, 99, 100 | `TASK_24_0` | `TASK_28_0` | NV 28 "King Kong" |
| 19 | 1048 | **166 Phòng thí nghiệm Myuu** | *(không khóa)* | `TASK_29_0` | NV 29 "Phòng thí nghiệm Myuu" |
| 20 | 1054 | 103 Võ đài Xên | `TASK_27_0` | `TASK_30_0` | NV 30 "Xên bọ hung" mở map 103 |
| 21 | 1065 | 105–110 Hành tinh Cold | `TASK_27_0` | `TASK_34_0` | NV 34 "Vùng đất băng giá" |
| 22 | 1077 | 154 Hành tinh Bill + **155 Hành tinh ngục tù** | `TASK_27_0` / *(155 không khóa)* | `TASK_42_0` | NV 42 — lối vào 155 đi qua 154 nên cả hai cùng mốc (§8 ghi rõ) |

> Map **111** (chặn theo sức mạnh > 1.500.000) và quy tắc "chỉ vào được nhà đúng hành tinh" **giữ nguyên**, không phụ thuộc nhiệm vụ.
> Map **95** và **101** là map offline/placeholder ([14-ban-do-pho-ban.md](../1-he-thong-hien-tai/14-ban-do-pho-ban.md)) nên **không** thêm vào nhóm 92–96 dù §8 ghi "92–96".

### 2.2 Các file còn lại

| # | File | Hàm | Dòng | Mốc cũ | Mốc mới | Lý do |
|---|---|---|---|---|---|---|
| 23 | `services/ClanService.java` | `sendInviteClan` | 283–284 | `TASK_10_0` | `TASK_13_0` | NV 13 mới là bước vào bang của tuyến mới |
| 24 | `services/ClanService.java` | `acceptJoinClan` | 318–319 | `TASK_10_0` | `TASK_13_0` | như trên |
| 25 | `map/service/NpcManager.java` | `getNpcsByMapPlayer` | 49–50 | `TASK_21_0` | `TASK_24_0` | NPC 38 Ca Lích chỉ hiện từ NV 24 |
| 26 | `npc_list/Calick.java` | `openBaseMenu` | 52–53 | `TASK_20_0` | `TASK_24_0` | đồng bộ với NpcManager |
| 27 | `npc_list/Calick.java` | `confirmMenu` | 93–94 | `TASK_20_0` | `TASK_24_0` | chặn "Đi đến Tương lai" cho đúng mốc |
| 28 | `npc_list/Cargo.java` | `openBaseMenu` | 25–31 | `taskMain.id == 7` → thoại "cứu đứa bé" | **bỏ nhánh** | NV 7 tuyến mới KẾT THÚC ở trạm tàu, chặn menu tàu tại đây sẽ làm kẹt NV 7 (20a §NV 7) |
| 29 | `npc_list/DrDrief.java` | `openBaseMenu` | 55–59 | như trên | **bỏ nhánh** | như trên |
| 30 | `npc_list/Cui.java` | `openBaseMenu` | 36 | như trên | **bỏ nhánh** | như trên |
| 31 | `npc_list/Cui.java` | `openBaseMenu` (map 19) | 41–56 | `TASK_19_0` / `TASK_19_1` / `TASK_19_2`, mỗi mốc một boss | `TASK_20_3` **hoặc** `TASK_48_3`, một menu cho cả 3 boss | Tuyến mới NV 20/48 đếm chung 3 boss Kuku / Mập đầu đinh / Rambo, **không bắt thứ tự** (20b §NV 20) |
| 32 | `npc_list/Cui.java` | `confirmMenu` + `goToBoss` (mới) | 72–98, 120–133 | 3 khối `MENU_FIND_*` trùng lặp | 1 khối + hàm dùng chung `goToBoss(player, bossId)` | Gộp theo thay đổi #31; `MENU_FIND_MAP_DAU_DINH` / `MENU_FIND_RAMBO` không còn dùng |
| 33 | `map/Zone.java` | `getItemMapsForPlayer` | 289–297 | item 78 hiện khi `== TASK_3_1`; item 74 hiện khi `>= TASK_3_0` | **bỏ cả hai**, thêm 2001/2002 chỉ chủ nhân thấy | §8: item 78 "bỏ"; 20a §NV 2 yêu cầu bỏ luôn item 74 và cho 2001/2002 hiển thị như item 726 |
| 34 | `mob/Mob.java` | `dropItemMob` (khối `//====TASK====`) | 622 | rơi Ngọc 7 sao (item 20) ở `TASK_8_1` | **bỏ nhánh** | `TASK_8_1` tuyến mới là bước **mua Rada cấp 1**, giữ lại sẽ phát nhầm vật phẩm |
| 35 | `mob/Mob.java` | `dropItemTask` | 1089–1091 | `TASK_2_0` → item 73 Đùi gà | `TASK_2_2` → item **2001** Mảnh Vỡ Hư Không | §8 + 20a §NV 2 |
| 36 | `mob/Mob.java` | `dropItemTask` | 1097–1099 | *(không có)* | `TASK_5_0` → item **2002** Kỷ Vật Của Ông, từ mob 7/8/9 | Nhánh mới của NV 5 (20a §NV 5) |
| 37 | `mob/Mob.java` | `dropItemTask` | 1105 | `TASK_8_1` → Ngọc 7 sao từ quái mẹ | **bỏ nhánh** | NV 8 tuyến mới chỉ yêu cầu HẠ 25 quái mẹ, không rơi đồ |
| 38 | `mob/Mob.java` | `dropQuestItem` (mới) | 1071–1081 | *(không có)* | chốt chặn `itemTemplateId >= Manager.ITEM_TEMPLATES.size()` | Xem §4.2 |
| 39 | `player/Player.java` | `getFlagBag` | 1007–1010 | `getIdTask == TASK_3_2` | `getIdTask >= TASK_5_2` | NV 5 mới là bước mở túi lưng; đổi `==` → `>=` để cờ túi không biến mất khi qua bước sau (20a §E) |
| 40 | `player/Player.java` | `update` | 448 | ép `taskMain.index = 2` khi đứng map nhà ở `TASK_0_0` / `TASK_0_1` | **bỏ đoạn** | Tuyến mới NV 0 bước 0 = "Đi về nhà" (`TASK_0_0`), bước 1 = "Lấy đồ trong rương" (`TASK_0_1`) là bước thật; giữ lại sẽ nhảy cóc mất bước 1 và mất thưởng bước đó |
| 41 | `boss/luyen_tap_tu_dong/TauPayPay.java` | `injured` | 100–103 | `!= TASK_10_1` → trả 100 | `< TASK_10_1` → trả 100 | §8 giữ mốc `TASK_10_1`, đổi `!=` thành `<` để người chơi đã qua bước vẫn đánh được, không kẹt khi quay lại |

**Tổng: 41 chỗ sửa** trên **11 file**.

### 2.3 Chỗ đã kiểm tra và cố ý GIỮ NGUYÊN

| File | Dòng | Nội dung | Vì sao giữ |
|---|---|---|---|
| `server/Controller.java` | 782 | Tutorial đăng nhập khi `getIdTask == TASK_0_0` | 20a §NV 0 ghi rõ "Giữ nguyên tutorial đăng nhập trong `Controller` cho `getIdTask == TASK_0_0`" |
| `map/service/ChangeMapService.java` | 1069 | Map 111 chặn theo sức mạnh > 1.500.000 | Không phụ thuộc nhiệm vụ |
| `map/service/ChangeMapService.java` | cuối hàm | Chặn vào nhà sai hành tinh | Không phụ thuộc nhiệm vụ |

Đã grep toàn bộ `getIdTask`, `isCurrentTask`, `taskMain.id` trên `SRC/src` — **ngoài `TaskService.java` (ngoài phạm vi) không còn chỗ nào dùng mốc nhiệm vụ mà chưa được xử lý**.

---

## 3. Ba lỗi cũ (ghi ở [12-nhiem-vu-chinh.md](../1-he-thong-hien-tai/12-nhiem-vu-chinh.md))

### 3.1 ✅ ĐÃ SỬA — Map 80 thiếu `break`

- **Chỗ**: `ChangeMapService.checkMapCanJoin`, `case 80` (nay là dòng 1017–1021).
- **Triệu chứng cũ**: `case 80` không có `break` nên sau khi qua được kiểm tra `TASK_20_0`, luồng **rơi xuống** nhánh `TASK_21_0` của nhóm 102/92/93/94/96. Kết quả: map 80 Núi khỉ vàng thực tế yêu cầu `TASK_21_0` chứ không phải `TASK_20_0` như ý định.
- **Đã sửa**: thêm `break;` kèm comment `// FIX: mốc cũ thiếu break nên map 80 rơi xuống nhánh TASK_21_0 kế tiếp`. Mốc mới của map 80 là `TASK_23_0` và nay đứng độc lập.

### 3.2 ⛔ CHƯA SỬA ĐƯỢC TRONG PHẠM VI NÀY — Bước "vào bang hội" xong chỉ nhờ nói chuyện sư phụ

- **Chỗ thật của lỗi**: `services/TaskService.java`, hàm `checkDoneTaskTalkNpc`, các dòng **168, 185, 202** —
  `|| doneTask(player, ConstTask.TASK_13_0)` bị đặt **ngoài** dấu ngoặc kiểm tra `gender`, nên chỉ cần nói chuyện
  Quy Lão Kame / Trưởng lão Guru / Vua Vegeta là bước gia nhập bang tự hoàn thành, không cần bang nào cả.
  (Cùng kiểu với `TASK_22_2`, `17_1`, `18_5`, `19_3`, `20_6`, `21_4`.)
- **Vì sao chưa sửa**: `services/TaskService.java` nằm trong danh sách **KHÔNG được đụng** của đợt này (nhóm khác đang viết lại).
- **Đã kiểm chứng phần thuộc phạm vi này là đúng**: `ClanService.checkDoneTaskJoinClan(Clan)` (dòng 977) đã kiểm tra
  `clan.getMembers().size() >= 2` trước khi gọi `TaskService.checkDoneTaskJoinClan(player)`, và được gọi từ cả
  `acceptJoinClan` (dòng 339) lẫn `acceptAskJoinClan` (dòng 397). **Phía bang hội không có lỗi.**
- **Yêu cầu chuyển cho nhóm `TaskService`**: khi viết lại, `TASK_13_0` **chỉ được** đến từ `checkDoneTaskJoinClan`,
  không nằm trong bảng NPC sư phụ. Đúng như 20a §F.2 đã ghi.

### 3.3 ⛔ CHƯA SỬA ĐƯỢC TRONG PHẠM VI NÀY — Mốc 40.000 dùng cho bước lẽ ra cần 500.000

- **Chỗ thật của lỗi**: `services/TaskService.java`, hàm `checkDoneTaskPower`, dòng **285–290**:

  ```java
  if (power >= 40000)  { doneTask(player, ConstTask.TASK_11_0); }   // <-- thừa, sai
  if (power >= 500000) { doneTask(player, ConstTask.TASK_11_0); }   // mốc đúng theo mô tả
  ```

  Dòng 40.000 chạy trước nên bước "Đạt 500k sức mạnh" xong ngay ở 40.000; dòng 500.000 trở thành vô nghĩa.
  (Lỗi anh em: dòng 273 cho `TASK_8_0` "Đạt 40.000 sức mạnh" cũng bị `checkDoneTaskGoToMap` map 0/7/14 hoàn thành hộ.)
- **Vì sao chưa sửa**: cùng lý do §3.2 — file nằm ngoài phạm vi.
- **Yêu cầu chuyển cho nhóm `TaskService`**: xóa dòng `power >= 40000 → TASK_11_0`. Theo 20a §F.3, bảng mốc sức mạnh
  của tuyến mới viết lại từ đầu, chương 2 chỉ còn một dòng `250.000 → TASK_10_2`.

> **Tóm lại: 1/3 lỗi đã sửa trong đợt này. 2 lỗi còn lại nằm trọn trong `TaskService.java`
> — không có cách sửa nào từ ngoài file đó. Cần bàn giao cho nhóm đang viết lại `TaskService`.**

---

## 4. Ghi chú kỹ thuật

### 4.1 Vì sao tách map 102 và nhóm 27/31/35 ra riêng

§8 liệt kê "92–96, 102 → `TASK_24_0`" và "28–30, 32–34, 36–38 → `TASK_16_0`", nhưng §8 cũng ghi riêng
"102 Nhà Bunma → **`TASK_14_0`**" và "27, 31, 35 → **`TASK_14_2`**". Hai dòng in đậm mới là đúng, vì:

- **NV 14 "Chợ đen ký ức" diễn ra ở map 84 và 102.** Nếu map 102 khóa tới `TASK_24_0`, người chơi **kẹt cứng ở NV 14**.
- **NV 15 "Người bạn đã quên" diễn ra ở map 27/31/35.** Nếu khóa tới `TASK_16_0`, người chơi **kẹt cứng ở NV 15**.

Code đã tách thành `case 102` riêng (`TASK_14_0`) và `case 27/31/35` riêng (`TASK_14_2`), đặt **trước** nhóm chung.
Đây là rủi ro nghiêm trọng nhất mà 20a §G.2 đã cảnh báo.

### 4.2 Chốt chặn item 2001 / 2002 chưa tồn tại

`ItemService.getTemplate(id)` = `Manager.ITEM_TEMPLATES.get(id)` — tra theo **vị trí** trong `ArrayList`, **không có
kiểm tra biên**. Gọi với id 2001 khi DB chưa có item đó sẽ ném `IndexOutOfBoundsException` ngay trong luồng xử lý
quái chết. Vì vậy `Mob.dropItemTask` không tạo `ItemMap` trực tiếp nữa mà đi qua hàm mới:

```java
private ItemMap dropQuestItem(Player player, int itemTemplateId) {
    if (itemTemplateId >= Manager.ITEM_TEMPLATES.size()) {
        return null;
    }
    return new ItemMap(zone, itemTemplateId, 1, location.x, location.y, player.id);
}
```

Nghĩa là nếu lỡ triển khai lệch nhịp, **server không sập** — chỉ là không rơi đồ. Đây là tấm lưới an toàn, **không phải
lý do để bỏ qua §1**.

### 4.3 Thay đổi có tính "sửa hành vi" chứ không chỉ đổi số

Bốn chỗ dưới đây không phải đổi hằng số mà đổi logic. Ghi riêng để người review chú ý:

- **#28–30** (Cargo / DrDrief / Cui): bỏ nhánh thoại "Hãy lên đường cứu đứa bé nhà tôi". Nhánh này **chặn menu tàu vũ trụ**
  khi `taskMain.id == 7`. Tuyến mới NV 7 lại **kết thúc tại trạm tàu vũ trụ** và cần dùng tàu ngay, nên giữ lại là kẹt.
- **#31–32** (Cui): gộp 3 menu tìm boss thành 1. Kèm theo đó, 3 khối code trùng lặp gần như nguyên văn được rút thành
  hàm `goToBoss(player, bossId)`.
- **#40** (Player.update): bỏ đoạn ép `taskMain.index = 2`.
- **#41** (TauPayPay): đổi `!=` thành `<`.

---

## 5. Chỗ nghi ngờ / cần chủ dự án xác nhận

| # | Vấn đề | Hiện đang làm gì | Cần quyết |
|---|---|---|---|
| 1 | **Map 154 Hành tinh Bill: `TASK_42_0` hay `TASK_44_0`?** §8 của file 20 ghi `TASK_42_0` (kèm giải thích: lối vào map 155 đi qua 154, để `TASK_44_0` thì NV 42 không khởi động được). §7.4 của 20c lại ghi `TASK_44_0`. | Theo **§8 → `TASK_42_0`**, vì §8 là bảng được giao áp dụng và có lý do kỹ thuật rõ. | Xác nhận §8 thắng 20c. Nếu chọn `TASK_44_0` thì phải mở lối vào map 155 không qua map 154. |
| 2 | **Menu thử thách của Bill / Whis vẫn khóa tới `TASK_44_0`** — nằm ở `services_dungeon/TrainingService.java`, không phải file map. | **Chưa sửa** (§8 chỉ nói "vẫn khóa tới TASK_44_0", và file này không nằm trong danh sách được giao). | Ai làm phần này? Đề xuất gộp vào đợt viết `TaskService`. |
| 3 | **`Cui` map 19 dùng `TASK_20_3` và `TASK_48_3`.** `TASK_48_3` là nhánh Jaco của điểm rẽ nhánh 1 (§7 file 20). Hằng số đã có sẵn trong `ConstTask`, biên dịch sạch, **nhưng nhánh 48 chưa được `TaskService` sinh ra**. | Đã viết sẵn cả hai mốc. | Xác nhận số hiệu nhánh là **48** (20b §NV 48 dùng số này). Nếu nhóm `TaskService` đổi số, phải sửa lại `Cui.java`. |
| 4 | **Ba boss Kuku / Mập đầu đinh / Rambo vẫn gọi `TASK_19_0/19_1/19_2`** trong `boss/**` (ngoài phạm vi). 20b yêu cầu đổi sang `TASK_20_3` / `TASK_48_3` với `maxCount = 3`. | **Chưa sửa** — `boss/**` bị cấm đụng. | Bàn giao cho nhóm boss. Nếu quên, menu Cui đưa tới boss đúng nhưng giết boss không tính bước. |
| 5 | **§8 ghi "92–96" nhưng map 95 là placeholder offline** (14-ban-do-pho-ban.md dòng 202). | Không thêm `case 95`. | Xác nhận map 95 không dùng tới. |
| 6 | **§8 ghi "29, 33, 37 trước đây không khóa" nhưng nay bị khóa `TASK_16_0`.** Đây là ba map trước giờ mở cho mọi người chơi cày. | Đã khóa theo §8 ("28–30, 32–34, 36–38"). | Xác nhận có thực sự muốn khóa 3 map cày này không — đây là thay đổi ảnh hưởng người chơi cũ nhiều nhất trong nhóm map thường. |
| 7 | **Khóa mới ở map 84 Siêu Thị** cũng khóa luôn **cửa hàng ký gửi** và các shop ở đó cho người chơi dưới `TASK_8_0`. | Đã khóa theo §8. | Chấp nhận, hay cho ký gửi một lối vào khác? |
| 8 | **Khóa mới ở map 5/13/20** cũng khóa **Bà Hạt Mít (nâng cấp/pha lê)** và **học kỹ năng ở sư phụ** cho người dưới `TASK_9_0`. | Đã khóa theo §8. | Chấp nhận (thiết kế muốn NV 10/17/26 dạy các tính năng này). |
| 9 | **Bỏ đoạn ép `taskMain.index = 2` trong `Player.update`** (#40). Không có trong §8 — là suy luận từ 20a §NV 0. | Đã bỏ. | Xác nhận. Nếu nhóm `TaskService` vẫn cần một cơ chế "gỡ kẹt" ở map nhà thì phải làm lại cho đúng bước mới. |
| 10 | **Chưa làm: các khóa tính năng ở file khác** mà 20b §5.4 / 20c §7.4 liệt kê nhưng §8 không có. Xem §6. | Chưa làm. | Chốt ai làm, đợt nào. |

---

## 6. Việc còn lại (khóa tính năng ngoài §8, chưa làm trong đợt này)

Các mốc dưới đây do 20b §5.4 và 20c §7.4 đặt ra, **không có trong §8** nên không thuộc phạm vi được giao.
Liệt kê để không thất lạc:

| Tính năng | Mốc mới đề xuất | File |
|---|---|---|
| Nâng cấp trang bị (Bà Hạt Mít) | `TASK_17_0` | `npc_list/BaHatMit.java` |
| Pha lê hóa / Ép sao (Bà Hạt Mít) | `TASK_26_0` | `npc_list/BaHatMit.java` |
| Phó bản Doanh Trại (Lính canh) | `TASK_21_0` | `npc_list/LinhCanh.java` |
| 156–159 Thánh địa (đang khóa theo SM ≥ 40 tỷ) | `TASK_32_1` | `npc_list/GiuMaDauBo.java` |
| 160–163 Hành tinh thực vật | `TASK_32_1` + item 992 | `services_func/UseItem.java` |
| NPC 42 Quốc Vương hiện ra | SM ≥ 17 tỷ **hoặc** `TASK_33_1` | `npc_list/QuocVuong.java` |
| Nhánh "Phá giới hạn (nhiệm vụ)" miễn phí | `TASK_33_3` / `TASK_44_4` / `TASK_47_3` / `TASK_50_3` | `services/OpenPowerService.java`, `QuocVuong.java`, `ToSuKaio.java` |
| Phó bản CĐRĐ | `TASK_35_0` | `npc_list/ThanVuTru.java`, `SnakeWayService` |
| Phó bản Mabư 12h / 14h | `TASK_36_0` / `TASK_37_0` | `npc_list/Osin.java` |
| Ngọc Rồng Sao Đen | `TASK_39_3` | `npc_list/RongOmega.java` |
| 50, 116 Thánh địa Kaio | `TASK_40_1` | `ChangeMapService.checkMapCanJoin` |
| Phó bản Khí gas | `TASK_43_0` | `npc_list/MrPoPo.java`, `DestronGasService` |
| Thách đấu Whis | `TASK_44_3` | `services_dungeon/TrainingService.java` |
| 145 Võ Đài Siêu Cấp | `TASK_46_2` | `ChangeMapService.checkMapCanJoin` |

> Hai dòng cuối cùng thuộc `ChangeMapService` — **cố ý chưa thêm** vì §8 không liệt kê, và khóa map 145 Võ Đài Siêu Cấp
> có thể ảnh hưởng các tính năng PvP/sự kiện đang chạy. Cần chủ dự án chốt trước khi thêm.

---

## 7. Kiểm chứng

```
cd /Users/phanthanhdi/Downloads/Teamobi2026/SRC
export PATH="/opt/homebrew/opt/openjdk@17/bin:$PATH"
find src -name '*.java' > /tmp/src2.txt && javac -nowarn -encoding UTF-8 -cp "lib/*" -d /tmp/out2 @/tmp/src2.txt
```

Kết quả: **biên dịch sạch 548 file**, không lỗi, không cảnh báo ngoài hai dòng `unchecked` vốn có của bản gốc.

Chưa chạy thử trong game — **không thể chạy thử cho tới khi dữ liệu nhiệm vụ mới được nạp** (xem §1).
