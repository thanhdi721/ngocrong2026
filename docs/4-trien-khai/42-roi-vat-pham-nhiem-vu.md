# 42 — Rơi vật phẩm nhiệm vụ (sửa lỗi chặn tuyến "bước nhặt đồ")

> Nối tiếp [41 §9](41-ro-map-va-boss-nhiem-vu.md) mục 1–3 và 5. Id vật phẩm theo bảng chốt
> [25](25-bang-id-vat-pham-moi.md) (2000–2031), **không** theo id cũ trong 20b/20c.

## 1. Tóm tắt

* **Lỗi**: 10 bước "nhặt vật phẩm" (và thêm bước 36.3, rà ra khi chạy script) chờ một vật phẩm **không rơi ở đâu cả**,
  nên người chơi kẹt vĩnh viễn. Ba món ghi là "rải sẵn trên map" (2023, 2026, 2008) nhưng `Map.initItem` chưa từng rải.
* **Đã sửa 11 bước**: 20.4, 48.4, 22.3, 25.2, 28.3, 29.2, 34.3, 36.3, 37.3, 38.4, 45.1.
  Ba bước cũ đã có nguồn rơi (5.0, 16.3, 32.4) được **chuyển vào cùng bảng**, tỉ lệ giữ nguyên.
* **Cơ chế dùng chung**: lớp mới `SRC/src/nro/models/task/QuestDrop.java`, một bảng `RULES`, ba điểm móc.
* **Script kiểm tra** `docs/4-trien-khai/42-kiem-tra-roi-vat-pham.py`: **0 bước thiếu nguồn rơi** (trước khi sửa: 14).
* Biên dịch `javac` JDK 17: **578 file, không lỗi** (577 cũ + `QuestDrop.java`).

## 2. Cơ chế dùng chung — `task/QuestDrop.java`

Mỗi dòng `Rule` gồm: **bước** (`ConstTask.TASK_x_y`) · **loại nguồn** (`MOB` / `BOSS` / `MAP_SPAWN`) ·
**id nguồn** (tempId quái hoặc id boss) · **map** (rỗng = mọi map) · **id vật phẩm** · **tỉ lệ %** · **số lượng**.

| Điểm móc | Nơi gọi | Làm gì |
|---|---|---|
| Quái chết | `Mob.dropItemTask` → `QuestDrop.onMobKilled(player, mob)` | Người kết liễu đang ở đúng bước + đúng loại quái + đúng map → tạo `ItemMap` gắn chủ. `Mob` gộp vào gói rơi đồ như cũ (gói ghi `playerId` = người kết liễu, bùa thu hút vẫn hút được). |
| Boss chết | cuối `TaskService.checkDoneTaskKillBoss` → `QuestDrop.onBossKilled(player, boss)` | Gọi **sau** `switch` cộng tiến độ, nên đòn kết liễu vừa chuyển người chơi sang bước "nhặt" là rơi luôn. Mọi boss (thế giới lẫn `QuestBoss`) đều gọi `checkDoneTaskKillBoss` trong `reward()`, nên một chỗ móc phủ hết. Rơi bằng `Service.dropItemMapForMe` — chỉ người kết liễu thấy. |
| Sinh sẵn (map không có quái) | `Zone.update` → `QuestDrop.updateZone(zone)` | Chỉ chạy ở map có dòng `MAP_SPAWN` (166). Mỗi 3 giây, với từng người đang ở đúng bước, giữ **3 món của riêng người đó** trên đất quanh chỗ đứng (món hết hạn 50 giây sẽ được bù). |

**Chỉ người đó thấy và nhặt được**: `QuestDrop` lưu chủ nhân của từng `ItemMap` trong một `WeakHashMap` riêng
(không dựa vào `ItemMap.playerId` vì `ItemMap.update` xóa chủ sau 45 giây).
* `Zone.getItemMapsForPlayer`: bỏ qua vật phẩm `QuestDrop.isHiddenFor(item, player)` → người khác vào map không thấy.
* `Zone.pickItem`: `QuestDrop.canPick` sai → báo "Không thể nhặt vật phẩm nhiệm vụ của người khác", dừng.
* Ba bộ lọc cũ theo bước của 2008 / 2026 / 2023 trong `getItemMapsForPlayer` **giữ lại** (lớp chặn thứ hai, chủ nhân
  luôn đang ở đúng bước khi vật phẩm được tạo). Mục 6 của yêu cầu: vật phẩm **hiện đúng** cho người đang ở bước — đã kiểm tra.

**Option 30 "Không thể giao dịch"**: `QuestDrop.create` tự gắn cho mọi id trong dải 2000–2031
(`ItemService.isTaskItem`), đúng quy định [25 §3](25-bang-id-vat-pham-moi.md). 992 **không** gắn (không thuộc dải;
nhẫn trao ở NV 32 và nhẫn Black Goku thế giới rơi cũng không có option này — gắn thêm sẽ làm không gộp chồng được).

`QuestBoss.dropQuestItem` (Heart rơi 2029) cũng chuyển sang `QuestDrop.dropForPlayer` — cùng cách gắn chủ + option 30.

## 3. Bảng từng bước

| Bước | Vật phẩm | Nguồn rơi | Map | Tỉ lệ | SL | Đã sửa gì |
|---|---|---|---|---|---|---|
| 5.0 | 2010 Kỷ Vật Của Ông | Quái Thằn lằn bay / Phi long / Quỷ bay (7/8/9) | mọi map có quái đó | 100% | 1 | Chuyển từ `Mob.dropItemTask` vào bảng, thêm option 30 |
| 16.3 | 2014 Vỏ đạn khắc dấu | Quái Bulon / Ukulele / Quỷ mập (22/23/24) | mọi map | 25% | 1 | Như trên |
| **20.4** | **2016** Thẻ tiền thưởng Granola | Boss Kuku −20 / Mập Đầu Đinh −21 / Rambo −22 | 68 và các map của 3 boss | 100% | 1 | **Mới** — trước không có nguồn |
| **48.4** | **2017** Biên bản truy nã Ngân Hà | Cùng 3 boss trên | như trên | 100% | 1 | **Mới** |
| **22.3** | **2018** Máy đo ký ức | Tiểu đội trưởng −27 **và** Số 1–4 (−23…−26) | 79/81/82/83 | 100% | 1 | **Mới**. Thiết kế 20b ghi chỉ −27; chữ bước ghi "rơi khi hạ **tên cuối** của tiểu đội" → cho cả 5 tên, ai bị hạ khi đang ở 22.3 cũng rơi (đỡ phải chờ riêng Tiểu đội trưởng hồi sinh) |
| **25.2** | **2019** Lõi năng lượng Android | Android 19 −30 / Dr.Kôrê −31 | 93/94/96 | 100% | 1 | **Mới** |
| **28.3** | **2021** Mảnh giáp khắc tên | King Kong −37 | 97/98/99 | 100% | 1 | **Mới** |
| **29.2** | **2023** Bản thiết kế bản sao | **Sinh sẵn cho riêng người chơi** | 166 | luôn giữ 3 món/người | 1 | **Mới**. Map 166 **không có quái** (kiểm tra `map_template`) nên không thể "rơi từ quái trong map" → sinh bằng `Zone.update` khi người chơi đứng ở 166 và đang ở 29.2 (đồng hồ 6 phút giữ nguyên) |
| 32.4 | 2025 Mảnh Ký Ức Vỡ | Quái Tobi (81) | 160–163 | 100% | 1 | Chuyển vào bảng, thêm option 30 |
| **34.3** | **2026** Mảnh Ký Ức Đóng Băng | Quái Kado (68) / Đá xanh (69) | **chỉ 110** Hang băng | 25% | 1 | **Mới** — thay "rải sẵn" (không chạy) bằng rơi từ quái trong map |
| **36.3** | **2027** Mảnh Bùa Babiđây | Quái Cadic M (118; kèm 95 như `checkDoneTaskKillMob`) | **chỉ 165** Sa mạc hoang vu | 25% | 1 | **Mới** — ngoài danh sách 10 món nhưng cũng không có nguồn (script phát hiện). Đường vòng ngoài giờ phó bản của bước "Xuống Cửa Ải 1" |
| **37.3** | **2028** Lõi Phép Babiđây | Boss Drabura 3 −343 / Super Bư −348 / Mabư 12h −236 / Mabư 14h −214 / Mabư NV −2102; quái Quỷ chim (50) / Hirudegarn (70) | mọi map có nguồn | 100% | 1 | **Mới**. Bước 37.2 xong được bằng boss **hoặc** 30 Quỷ chim → cả hai loại nguồn đều rơi; thêm nguồn bước 1 (Mabư, Hirudegarn) để có đường 24/7 |
| **38.4** | **992** Nhẫn thời không sai lệch | Black Goku **bản nhiệm vụ −2103** (và bản thế giới −203) | 92–100, 102 | 100% | 1 | **Mới** — chỉ khi người kết liễu đang ở 38.4 (đúng nguyên tắc "boss nhiệm vụ chỉ rơi đồ nhiệm vụ"). Hạ Super Black Goku xong 38.3 → sang 38.4 → rơi luôn |
| **45.1** | **2008** Mảnh Ký Ức 7 | Quái Mộc nhân (0) | **chỉ 78** Lãnh địa Fize | 30% | 1 | **Mới** — thay "rải sẵn" bằng rơi từ quái trong map (map 78 chỉ có 4 mộc nhân) |

Hai bước tuyến gốc NV 2/3 không đổi: 2.0 (đùi gà 73 — `Mob.dropItemTask`), 3.1 (item 78 — `Map.initItem` map 42/43/44).
Bước 39.1 (gom 7 viên Ngọc Rồng) đếm hành trang, không thuộc cơ chế này.

**Vì sao bỏ cách "rải sẵn"**: `Map.initItem` không rải 3 món này; và kể cả có rải, `ItemMap.update` xóa mọi vật phẩm
nằm đất quá 50 giây (trừ 74/78/726) — file `ItemMap.java` không nằm trong phạm vi được sửa. Rơi từ quái / sinh lại
theo nhịp cho từng người không phụ thuộc thời hạn đó.

`TaskService.checkDoneTaskPickItem`: **đã có đủ `case`** cho cả 10 món + 2027, đúng hằng bước — không phải thêm.

## 4. Bước 22.2 và bản Namek (−311…−315)

**Đúng là không được tính**: 5 lớp `boss/tieu_doi_sat_thu_namek/*_NM.java` ghi đè `reward()` mà **không gọi**
`TaskService.checkDoneTaskKillBoss` (chỉ rơi 77 + đồ riêng). Thêm `case` vào `switch` cũng vô ích vì hàm không bao giờ được gọi.
**Chưa sửa**: gói `tieu_doi_sat_thu_namek` nằm ngoài phạm vi được sửa của lượt này, và [41 §9 mục 5](41-ro-map-va-boss-nhiem-vu.md)
đã chốt giữ (bản Namek máu thấp hơn 10 lần, map 73–77 khác với 4 map ghi trong chữ bước). Chữ bước 22.2 chỉ nêu
Núi khỉ đỏ / Hang quỷ chim / Núi khỉ đen / Hang khỉ đen nên người chơi không bị dẫn nhầm.
Nếu muốn tính: thêm `TaskService.gI().checkDoneTaskKillBoss(plKill, this);` vào `reward()` của 5 lớp `_NM`, thêm
`BossID.SO_x_NM` / `TIEU_DOI_TRUONG_NM` vào `case` của 22.2 và vào dòng 22.3 của `QuestDrop.RULES`.

## 5. Kết quả script kiểm tra

Chạy: `python3 docs/4-trien-khai/42-kiem-tra-roi-vat-pham.py` (thoát mã 1 nếu còn thiếu).

Script đọc: các bước trong `02-nhiem-vu-moi.sql` (237 bước) · cặp (vật phẩm → bước) trong `checkDoneTaskPickItem` ·
bảng `QuestDrop.RULES` + 2 nguồn gốc (73, 78) · map/quái trong `map_template` của `nro1.sql` · boss: có lớp tạo trong
`BossManager`, lớp đó gọi `checkDoneTaskKillBoss` (hoặc là `QuestBoss`) và được sinh ra ở đâu đó.

```
02: 237 bước
checkDoneTaskPickItem: 16 cặp (vật phẩm, bước)
nguồn rơi: 17 dòng
```

| Bước | Vật phẩm | Tên bước | Tỉ lệ | Nguồn tới được | Kết quả |
|---|---|---|---|---|---|
| TASK_2_0 | 73 | Hạ %4 lấy 10 đùi gà | 100% | quái KHUNG_LONG(1) @ [1, 2, 3, 164]; LON_LOI(2) @ [8, 9, 11]; QUY_DAT(3) @ [15, 16, 17] | OK |
| TASK_3_1 | 78 | Tìm đồ lạ ở %5 | 100% | map_init map 42 / 43 / 44 | OK |
| TASK_5_0 | 2010 | Tìm Kỷ Vật Của Ông | 100% | quái THAN_LAN_BAY(7) @ [3, 4, 164]; PHI_LONG(8) @ [11, 12]; QUY_BAY(9) @ [17, 18] | OK |
| TASK_16_3 | 2014 | Nhặt 5 Vỏ đạn khắc dấu | 25% | quái BULON(22) @ [30, 59]; UKULELE(23) @ [34]; QUY_MAP(24) @ [38, 141] | OK |
| TASK_20_4 | 2016 | Nhặt 3 Thẻ tiền thưởng | 100% | boss KUKU(-20); MAP_DAU_DINH(-21); RAMBO(-22) | OK |
| TASK_22_3 | 2018 | Nhặt Máy đo ký ức | 100% | boss TIEU_DOI_TRUONG(-27); SO_1..SO_4(-26..-23) | OK |
| TASK_25_2 | 2019 | Nhặt 3 Lõi năng lượng | 100% | boss ANDROID_19(-30); DR_KORE(-31) | OK |
| TASK_28_3 | 2021 | Nhặt Mảnh giáp khắc tên | 100% | boss KING_KONG(-37) | OK |
| TASK_29_2 | 2023 | Nhặt 5 Bản thiết kế, 6 phút | 100% | map_spawn map 166 | OK |
| TASK_32_4 | 2025 | Nhặt 3 Mảnh Ký Ức Vỡ | 100% | quái TOBI(81) @ [160, 161, 162, 163] | OK |
| TASK_34_3 | 2026 | Nhặt Mảnh Ký Ức Đóng Băng | 25% | quái KADO(68) @ [110]; DA_XANH(69) @ [110] | OK |
| TASK_36_3 | 2027 | Xuống Cửa Ải 1 | 25% | quái CADIC_M(118) @ [165] | OK |
| TASK_37_3 | 2028 | Nhặt Lõi Phép Babiđây | 100% | boss DRABURA_3, SUPERBU, MABU_12H, MABU, MABU_14H_NV; quái QUY_CHIM(50) @ [76, 77, 81, 82, 85, 86…]; HIRUDEGARN(70) @ [126] | OK |
| TASK_38_4 | 992 | Nhặt Nhẫn thời không | 100% | boss BLACK_GOKU_NV(-2103); BLACK_GOKU(-203) | OK |
| TASK_45_1 | 2008 | Nhặt Mảnh Ký Ức thứ bảy | 30% | quái MOC_NHAN(0) @ [78] | OK |
| TASK_48_4 | 2017 | Nhặt 3 Biên bản truy nã | 100% | boss KUKU(-20); MAP_DAU_DINH(-21); RAMBO(-22) | OK |

```
Bước tên "Nhặt…" trong 02 mà checkDoneTaskPickItem không xử lý: 0
SỐ BƯỚC NHẶT THIẾU NGUỒN RƠI: 0
```

Thử âm tính: bỏ bảng `QuestDrop` khỏi dữ liệu đầu vào thì script báo **14** bước thiếu (11 bước sửa ở đây + 5.0, 16.3, 32.4) — script bắt đúng lỗi.

## 6. File đã sửa

| File | Thay đổi |
|---|---|
| `task/QuestDrop.java` | **Mới** — bảng rơi + 3 điểm móc + sổ chủ nhân |
| `mob/Mob.java` | `dropItemTask`: bỏ 3 nhánh 2010/2014/2025 riêng lẻ, gọi `QuestDrop.onMobKilled`; xóa hàm `dropQuestItem` không còn dùng. **Không đụng** phần tỉ lệ Ngọc Rồng |
| `services/TaskService.java` | Cuối `checkDoneTaskKillBoss` gọi `QuestDrop.onBossKilled` |
| `boss/quest/QuestBoss.java` | `dropQuestItem` dùng `QuestDrop.dropForPlayer` (chỉ người kết liễu thấy) |
| `boss/quest/BlackGokuNhiemVu.java` | Chỉ thêm chú thích (992 rơi qua bảng, theo bước) |
| `map/Zone.java` | `update` gọi `QuestDrop.updateZone`; `getItemMapsForPlayer` ẩn đồ nhiệm vụ của người khác; `pickItem` chặn người không phải chủ |

Không sửa: `cpanel`, `event/**`, `SRC/sql/**`, `Map.java` (không cần nữa).

## 7. Chỗ nghi ngờ

| # | Mức | Vấn đề | Ghi chú / đề xuất |
|---|---|---|---|
| 1 | Cao | **Vật phẩm trên đất tự mất sau 50 giây** (`ItemMap.update`, ngoài phạm vi sửa). Không nhặt kịp thì phải hạ boss lại: King Kong (28.3) chỉ 1 con, hồi sinh ~10 phút; Black Goku NV nghỉ 15–30 phút. | Chấp nhận. Nếu muốn chắc: miễn xóa cho `QuestDrop.isQuestDrop(item)` trong `ItemMap.update`. |
| 2 | **Chặn tuyến (ngoài phạm vi)** | **Map 166 không có đường vào.** `map_template` 166 không có waypoint; [04-npc-tren-map.sql](../../SRC/sql/patch/04-npc-tren-map.sql) khối (5) cũng ghi "KHÔNG CÓ ĐƯỜNG VÀO"; `grep -w 166` toàn `SRC/src` không thấy NPC / dịch chuyển nào đưa tới 166 (chỉ có khóa ở `ChangeMapService`). Nếu đúng vậy thì 29.1 "Vào Phòng thí nghiệm" kẹt trước cả bước nhặt — cơ chế sinh sẵn ở 29.2 đúng nhưng không ai tới được. Script chỉ kiểm tra map tồn tại, không kiểm tra đường vào. | Thêm menu dịch chuyển tới 166 cho NPC 37 Bunma (map 102) khi đang ở NV 29 / NV 46, hoặc waypoint 97 ↔ 166. |
| 3 | Trung bình | Quái: gói rơi đồ tạo **trước** `checkDoneTaskKillMob`. Con Quỷ chim thứ 30 (xong 37.2) chưa rơi Lõi Phép; con kế tiếp mới rơi (100%). Boss thì rơi ngay đòn kết liễu. | Không đổi thứ tự trong `Mob` để khỏi ảnh hưởng mọi tuyến khác. |
| 4 | Trung bình | 22.3: mở rộng nguồn từ chỉ Tiểu đội trưởng (20b) sang cả 5 tên. | Khớp chữ bước "tên cuối của tiểu đội". Muốn đúng 20b thì bớt SO_1..SO_4 khỏi dòng 22.3. |
| 5 | Trung bình | 22.2 không tính bản Namek — xem §4. | Chưa sửa (ngoài phạm vi, 41 đã chốt giữ). |
| 6 | Thấp | Đồ rơi từ **quái** vẫn đi trong gói rơi đồ chung của `Mob` (gửi cả map, kèm id chủ) — người khác có thể thấy thoáng qua nhưng **không nhặt được** (chặn ở `Zone.pickItem`), vào map sau cũng không thấy. Đồ rơi từ boss / sinh sẵn thì chỉ gửi cho chủ. | Giữ để bùa thu hút (`hutItem`) vẫn hút được đồ nhiệm vụ. |
| 7 | Thấp | 20.4 / 48.4: thẻ chỉ rơi khi **đã** ở bước nhặt, nên sau 3 boss của 20.3 cần hạ thêm 2 boss (con thứ 3 xong 20.3 rơi luôn thẻ đầu). | Đúng thiết kế 20b ("rơi 100% … khi đang ở TASK_20_4"). |
| 8 | Thấp | 38.4 cũng rơi từ Black Goku **thế giới** (−203) khi đang ở bước — cộng thêm vào tỉ lệ rơi ngẫu nhiên sẵn có của bản đó. | Đúng ghi chú 20c NV 38 ("ép rơi 100%"). |
| 9 | Thấp | 2010 / 2014 / 2025 nay có option 30; bản đã nằm trong hành trang từ trước (không option) sẽ không gộp chồng với bản mới. | Chỉ tốn thêm ô, không kẹt nhiệm vụ. |
| 10 | Thấp | Mộc nhân map 78 chỉ nhận tối đa 10% máu mỗi đòn → ~10 đòn/con, 4 con, 30%. | Nếu thấy chậm, tăng tỉ lệ dòng 45.1. |
| 11 | Thấp | Heart (2029) không gắn theo bước — rơi mỗi lần hạ hình dạng 3 như trước, nay chỉ người kết liễu thấy. | Không có bước "nhặt 2029"; giữ. |
