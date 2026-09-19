# 41 — Ghi rõ map và boss cho từng bước nhiệm vụ, sửa mũi tên chỉ đường

> Code: [`TaskService.java`](../../SRC/src/nro/models/services/TaskService.java) ·
> [`ConstTask.java`](../../SRC/src/nro/models/consts/ConstTask.java) ·
> [`BossManager.java`](../../SRC/src/nro/models/boss/Boss_Manager/BossManager.java)
> SQL: [`02-nhiem-vu-moi.sql`](../../SRC/sql/patch/02-nhiem-vu-moi.sql) (cài mới) ·
> [`07-ro-map-va-boss.sql`](../../SRC/sql/patch/07-ro-map-va-boss.sql) (server đang chạy) ·
> [`05-sua-huong-dan-tan-thu.sql`](../../SRC/sql/patch/05-sua-huong-dan-tan-thu.sql) (một tên bước NV 3)
>
> Bảng đối chiếu dưới đây sinh bằng script: đọc `task_sub_template` từ DB tạm (`database team2026.sql`
> → 01 → 02 → 04 → 05 → 06), cột `mobs` / `npcs` của `map_template`, `mapJoin` trong
> `boss/BossesData.java` + `Broly.java`; điều kiện server chép từ `TaskService` (các hàm
> `checkDoneTask*`), `Mob.dropItemTask`, `QuestBoss.reward`, `Zone.getItemMapsForPlayer`.

## 1. Phản hồi của người chơi và kết quả xác minh

Ảnh chụp: NV 6 "Người thu gom", bước 0 "Hạ 15 thằn lằn bay". Đánh thằn lằn bay ở **Rừng nấm**
không được tính, sang **Rừng xương** mới tính.

**Xác minh — đúng như người chơi báo:**

| Chỗ | Trước khi sửa |
|---|---|
| `TaskService.checkDoneTaskKillMob`, mob 7/8/9 | `if (mapId == 4 \|\| mapId == 12 \|\| mapId == 18) doneTask(TASK_6_0)` → **chỉ tính ở Rừng xương / Vực maima / Rừng thông Xayda** |
| Quái có thật ở đâu (`map_template.mobs`) | Thằn lằn bay (7): **3 Rừng nấm**, 4 Rừng xương · Phi long (8): **11 Thung lũng Maima**, 12 Vực maima · Quỷ bay (9): **17 Rừng nguyên sinh**, 18 Rừng thông Xayda |
| Mũi tên bước 6.0 (cột `map`) | **-1 = không có mũi tên** |
| Mũi tên bước liền trước 5.0 (nhặt kỷ vật) | -7 → **3 Rừng nấm / 11 Thung lũng Maima / 17 Rừng nguyên sinh** (đúng cho 5.0 vì kỷ vật rơi từ quái bay ở mọi map) |
| Chữ | tên "Hạ 15 %9", nhắc "Lần theo tiếng rít vào rừng sâu" — không nêu map nào |

Nghĩa là người chơi đi theo mũi tên của bước 5.0 tới Rừng nấm, sang bước 6.0 thì mũi tên biến mất,
cứ đứng Rừng nấm đánh cùng loại quái mà không được tính. Bước 6.1 "Hạ Kẻ Thu Gom" cũng không có
mũi tên, không nói là boss, không nói ở đâu (boss chỉ đứng ở 4 / 12 / 18).

Đây không phải lỗi riêng NV 6: rà cả 237 bước thấy **27 bước đánh quái bị khóa vào một vài map** (20 bước trong đó bỏ sót map thật sự có quái),
**18 bước không có mũi tên** dù bước có chỗ cụ thể, **2 bước mũi tên trỏ sang map mà bước không
tính**, **8 bước mũi tên trỏ vào phó bản** (không đi bộ tới được), và nhiều bước đánh boss
không nói là boss, không nói ở đâu.

## 2. Đã sửa gì

### 2.1 Java

| File | Sửa |
|---|---|
| `services/TaskService.java` · `checkDoneTaskKillMob` | Bỏ ràng buộc map ở **27 bước đánh quái** (kể cả đường vòng Quỷ chim của NV 37 bước 2): tính theo loại quái ở **mọi map**. Giữ ràng buộc ở 9 bước ngoại lệ (mục 4). NV 41 bước 1 "vành đai rừng" nay tính **mọi quái** trong vùng map 27–38 (trước chỉ 5 loài). |
| `services/TaskService.java` · `transformMapId` | Thêm 4 placeholder mũi tên theo hành tinh: **-11** → 4/12/18, **-12** → 27/31/35, **-13** → 29/33/37, **-14** → 30/34/38. |
| `services/TaskService.java` · `transformName` | Thêm chữ **%15–%20** (thay TRƯỚC `%1`/`%2` để `"%15"` không bị ăn thành `"Làng Aru5"`). **Sửa %13** Namếc: "Thung lũng Namếc" (map 10, không có phi long) → "Thung lũng Maima" (map 11, đúng placeholder -7). **Sửa %14** xếp nhầm hành tinh: TĐ ra "phi long mẹ", XD ra "thằn lằn mẹ" → nay TĐ "thằn lằn mẹ", NM "phi long mẹ", XD "quỷ bay mẹ" (đúng quái mẹ ở 4 / 12 / 18). |
| `consts/ConstTask.java` | Hằng `MAP_RUNG_XUONG`, `MAP_RUNG_BAMBOO`, `MAP_PHIA_NAM`, `MAP_BO_BIEN`, `TEN_MAP_RUNG_XUONG`… `TEN_QUAI_BO_BIEN`. |
| `boss/Boss_Manager/BossManager.java` | Kẻ Thu Gom và Jaco: **3 → 6 bản** (mục 5). |

Không sửa: `cpanel`, `event/**`, `mob/Mob.java`, `boss/quest/QuestBoss.java` (chỉ đọc).

### 2.2 Placeholder

| Mũi tên | TĐ | NM | XD | Chữ đi kèm | TĐ | NM | XD |
|---|---|---|---|---|---|---|---|
| -7 (có sẵn) | 3 Rừng nấm | 11 Thung lũng Maima | 17 Rừng nguyên sinh | %13 (sửa NM) | Rừng nấm | **Thung lũng Maima** | Rừng nguyên sinh |
| **-11** | 4 Rừng xương | 12 Vực maima | 18 Rừng thông Xayda | **%15** | Rừng xương | Vực maima | Rừng thông Xayda |
| **-12** | 27 Rừng Bamboo | 31 Núi hoa vàng | 35 Rừng cọ | **%16** | Rừng Bamboo | Núi hoa vàng | Rừng cọ |
| **-13** | 29 Nam Kamê | 33 Nam Guru | 37 Thung lũng đen | **%17** | Nam Kamê | Nam Guru | Thung lũng đen |
| **-14** | 30 Đảo Bulông | 34 Đông Nam Guru | 38 Bờ vực đen | **%18** | Đảo Bulông | Đông Nam Guru | Bờ vực đen |
| | | | | **%19** | không tặc | quỷ đầu to | quỷ địa ngục |
| | | | | **%20** | bulon | ukulele | quỷ mập |
| | | | | %14 (sửa) | **thằn lằn mẹ** | phi long mẹ | **quỷ bay mẹ** |

### 2.3 Chữ (quy tắc)

* Tên bước **≤ 28 ký tự** tính theo placeholder dài nhất (dài nhất sau sửa: 28 — "Hạ boss Xên bọ hung dạng 1-2",
  "Tìm đồ lạ ở %5" → "Tìm đồ lạ ở Vách núi Kakarot"). `max_count > 1` thì tên vẫn có đúng con số.
* Bước đánh boss: tên có chữ **"boss"** ("Hạ boss Kẻ Thu Gom"), `notify` nêu **map boss xuất hiện** và **thời gian
  hồi sinh** (boss bản nhiệm vụ `QuestBoss`: 15–30 phút; boss thế giới: theo `secondsRest` của `BossesData`).
* Bước đánh quái: `notify` nêu **quái có ở map nào**; bước còn ràng buộc thì ghi **"chỉ tính ở …"**.
* `notify` ≤ 100 ký tự (placeholder dài nhất), `detail`: dòng mục tiêu ≤ 70, cả mô tả ≤ 200.
* NV 0–3: không đổi điều kiện, `map`, `npc_id`, `max_count`, notify trống giữ trống. Chỉ đổi **một tên bước**
  (3.1 "Tìm vật thể lạ rơi xuống" → "Tìm đồ lạ ở %5", vì vật thể lạ — item 78 — thật ra nằm ở 42/43/44).

Tổng cộng: **65 bước** đổi (33 tên, 48 câu nhắc, 33 mũi tên, 3 NPC) và **18 dòng mục tiêu** trong `detail`.

## 3. NV 6 — trước / sau

| | Trước | Sau |
|---|---|---|
| Mô tả (dòng mục tiêu) | Hạ %9 trong rừng, diệt Kẻ Thu Gom rồi báo cho %2. | Hạ %9, diệt boss Kẻ Thu Gom ở %15, báo ông. |
| 6.0 tên | Hạ 15 %9 | Hạ 15 %9 *(không đổi — client tự nối "(x/15)")* |
| 6.0 server tính | Thằn lằn bay / phi long / quỷ bay **chỉ ở 4, 12, 18** | **Mọi map**: 3, 4 · 11, 12 · 17, 18 (và 164 map riêng tư) |
| 6.0 mũi tên | không có | -7 → **Rừng nấm / Thung lũng Maima / Rừng nguyên sinh** (gần nhất) |
| 6.0 nhắc | Lần theo tiếng rít vào rừng sâu, hạ 15 %9 | **%9 có ở %13 và %15. Hạ ở map nào cũng tính** → TĐ: "thằn lằn bay có ở Rừng nấm và Rừng xương. Hạ ở map nào cũng tính" |
| 6.1 tên | Hạ Kẻ Thu Gom | **Hạ boss Kẻ Thu Gom** |
| 6.1 mũi tên | không có | -11 → **Rừng xương / Vực maima / Rừng thông Xayda** |
| 6.1 nhắc | Kẻ Thu Gom đang lảng vảng trong rừng sâu | **Boss Kẻ Thu Gom xuất hiện ở %15, hồi sinh 15–30 phút** |
| Số bản boss | 3 (mỗi map đúng 1) | 6 (mỗi map 2) |

Người chơi TĐ thấy: `- Hạ 15 thằn lằn bay (3/15)`, bấm vào: "thằn lằn bay có ở Rừng nấm và Rừng xương. Hạ ở map nào
cũng tính"; bước sau `- Hạ boss Kẻ Thu Gom`, mũi tên chỉ Rừng xương.

## 4. Bước đánh quái đã bỏ ràng buộc map (27 bước)

Tính theo **loại quái ở mọi map** quái đó xuất hiện. Cột "Map được thêm" là những map có quái đó mà trước đây đánh không tính (trống = không đổi gì trong thực tế vì quái chỉ có ở đúng các map cũ — sửa cho đồng bộ).

| Bước | Tên (placeholder) | Quái | Trước: chỉ tính ở | Map được thêm |
|---|---|---|---|---|
| 4.0 | Hạ 12 %4 | 1 Khủng long, 2 Lợn lòi, 3 Quỷ đất | 2 / 9 / 16 (đúng hành tinh mình) | 1 Đồi hoa cúc, 3 Rừng nấm, 8 Đồi nấm tím, 11 Thung lũng Maima, 15 Đồi hoang, 17 Rừng nguyên sinh, 164 Map riêng tư + map của 2 hành tinh kia |
| 4.1 | Hạ 15 %4 mẹ | 4 Khủng long mẹ, 5 Lợn lòi mẹ, 6 Quỷ đất mẹ | 2 / 9 / 16 (đúng hành tinh mình) | 3 Rừng nấm, 4 Rừng xương, 11 Thung lũng Maima, 12 Vực maima, 17 Rừng nguyên sinh, 18 Rừng thông Xayda, 146 Tây Karin, 164 Map riêng tư + map của 2 hành tinh kia |
| 6.0 | Hạ 15 %9 | 7 Thằn lằn bay, 8 Phi long, 9 Quỷ bay | 4, 12, 18 | 3 Rừng nấm, 11 Thung lũng Maima, 17 Rừng nguyên sinh, 164 Map riêng tư |
| 7.0 | Hạ 10 quái mẹ trong 3 phút | 10 Thằn lằn mẹ, 11 Phi long mẹ, 12 Quỷ bay mẹ | 4, 12, 18 | 27 Rừng Bamboo, 28 Rừng dương xỉ, 31 Núi hoa vàng, 32 Núi hoa tím, 35 Rừng cọ, 36 Rừng đá |
| 8.2 | Hạ 25 quái mẹ lấy lõi | 10 Thằn lằn mẹ, 11 Phi long mẹ, 12 Quỷ bay mẹ | 4, 12, 18 | 27 Rừng Bamboo, 28 Rừng dương xỉ, 31 Núi hoa vàng, 32 Núi hoa tím, 35 Rừng cọ, 36 Rừng đá |
| 9.1 | Hạ 20 %12 | 13 Ốc mượn hồn, 14 Ốc sên, 15 Heo Xayda mẹ | 5 / 13 / 20 (đúng hành tinh mình) | 29 Nam Kamê, 33 Nam Guru, 37 Thung lũng đen, 183 Thành cổ 1 + map của 2 hành tinh kia |
| 14.2 | Hạ 20 heo chở hàng | 16 Heo rừng, 17 Heo da xanh, 18 Heo Xayda | 27, 31, 35 | 28 Rừng dương xỉ, 32 Núi hoa tím, 36 Rừng đá |
| 15.0 | Hạ 20 quái mẹ | 10 Thằn lằn mẹ, 11 Phi long mẹ, 12 Quỷ bay mẹ | 27, 31, 35 | 4 Rừng xương, 12 Vực maima, 18 Rừng thông Xayda, 28 Rừng dương xỉ, 32 Núi hoa tím, 36 Rừng đá |
| 16.1 | Hạ 40 %19 | 31 Không tặc, 32 Quỷ đầu to, 33 Quỷ địa ngục | 29, 33, 37 | 30 Đảo Bulông, 34 Đông Nam Guru, 38 Bờ vực đen, 58 Tường thành 2, 59 Tường thành 3, 141 Con đường rắn độc |
| 16.2 | Hạ 30 %20 | 22 Bulon, 23 Ukulele, 24 Quỷ mập | 30, 34, 38 | 59 Tường thành 3, 141 Con đường rắn độc |
| 18.2 | Hạ 30 quái vây thành | 25 Tambourine, 26 Drum, 27 Akkuman | 6, 10, 19 | 68 Thung lũng Nappa, 69 Vực cấm, 142 Con đường rắn độc, 146 Tây Karin |
| 19.1 | Hạ 60 Nappa mất trí | 39 Nappa | 68, 69, 70 | 156 Tây thánh địa |
| 19.2 | Hạ 40 Soldier gác kho | 40 Soldier | 69, 70 | 157 Đông thánh Địa |
| 19.3 | Cùng bạn hạ 30 Appule | 41 Appule | 71, 72 | 70 Núi Appule |
| 22.1 | Hạ 40 lính khỉ canh đường | 54 Khỉ lông đen, 55 Khỉ giáp sắt | 81, 82, 83 | 183 Thành cổ 1 |
| 23.2 | Hạ 25 Khỉ lông vàng, 5 phút | 57 Khỉ lông vàng | 80 | 85 Hành tinh M-2, 87 Hành tinh Cretaceous, 89 Hành tinh Rudeeze, 90 Hành tinh Gelbo, 91 Hành tinh Tigere, 122 Ngũ Hành Sơn, 124 Ngũ Hành Sơn, 184 Thành cổ 2 |
| 24.2 | Hạ 50 Xên con cấp 1-2 | 58 Xên con cấp 1, 59 Xên con cấp 2 | 92, 93 |  |
| 24.3 | Hạ 40 Xên con cấp 3-4 | 60 Xên con cấp 3, 61 Xên con cấp  4 | 94, 96 |  |
| 28.1 | Hạ 60 Xên con cấp 5-7 | 62 Xên con cấp  5, 63 Xên con cấp  6, 64 Xên con cấp  7 | 97, 98, 99 |  |
| 30.1 | Hạ 50 Xên con cấp 8 | 65 Xên con cấp  8 | 100 |  |
| 32.3 | Hạ 40 Cabira hoặc Tobi | 80 Cabira, 81 Tobi | 160, 161, 162 | 163 Làng Plant nguyên thủy |
| 34.1 | Hạ 50 Tai tím hoặc Abo | 66 Tai tím, 67 Abo | 105, 106, 107 | 108 Dòng sông băng, 158 Bắc thánh địa |
| 34.2 | Hạ 20 Kado trong 5 phút | 68 Kado | 108, 109 | 110 Hang băng, 159 Nam thánh Địa |
| 37.2 | Hạ Drabura 3 / 30 Quỷ chim | 50 Quỷ chim | 126 | 76 Núi đá, 77 Rừng đá, 81 Hang quỷ chim, 82 Núi khỉ đen, 85 Hành tinh M-2, 86 Hành tinh Polaris, 87 Hành tinh Cretaceous, 88 Hành tinh Monmaasu, 90 Hành tinh Gelbo, 122 Ngũ Hành Sơn … (+3) |
| 38.1 | Hạ 60 Xên con phía bắc | 62 Xên con cấp  5, 63 Xên con cấp  6, 64 Xên con cấp  7, 65 Xên con cấp  8 | 97, 98, 99, 100 |  |
| 39.4 | Hạ 40 Khỉ lông vàng | 57 Khỉ lông vàng | 80 | 85 Hành tinh M-2, 87 Hành tinh Cretaceous, 89 Hành tinh Rudeeze, 90 Hành tinh Gelbo, 91 Hành tinh Tigere, 122 Ngũ Hành Sơn, 124 Ngũ Hành Sơn, 184 Thành cổ 2 |
| 42.2 | Phá 60 lồng giam, 10 phút | 78 Khỉ lông xanh, 79 Taburine Đỏ | 155 |  |

Thực sự mở rộng: **21 bước** (kể cả 37.2); đồng bộ hình thức (quái chỉ có ở đúng map cũ): 24.2, 24.3, 28.1, 30.1, 38.1, 42.2.

## 5. Ngoại lệ vẫn giữ ràng buộc map (9 bước)

| Bước | Tên (sau sửa) | Chỉ tính ở | Lý do giữ | Chữ báo cho người chơi |
|---|---|---|---|---|
| 21.1 | Phá trại hoặc hạ 300 quái | 63–67 (Trại lính Fide, Núi dây leo, Núi cây quỷ, Trại quỷ già, Vực chết) + phá Doanh trại | Đường vòng cho người chơi lẻ thay phó bản Doanh trại (doc 32): phải cày đúng cụm map tương đương độ khó; quái 43–49 còn ở map khác (72, 73, 157…) dễ hơn. | Không có bang? Chỉ tính quái ở Trại lính Fide, Núi dây leo, Núi cây quỷ, Trại quỷ già, Vực chết |
| 35.2 | Tích 120 điểm diệt quái | trong CĐRĐ 141–143 (2 điểm) hoặc Dơi da xanh / Quỷ chim ở 73/74/76/77/81/82 (1 điểm) | Đường vòng thay phó bản Con đường rắn độc (doc 32), điểm quy đổi. | Phó bản: mỗi quái 2 điểm. Ngoài: chỉ Dơi da xanh, Quỷ chim ở map 73/74/76/77/81/82, 1 điểm |
| 35.3 | Qua rắn độc hoặc hạ 200 quái | phá CĐRĐ, hoặc Dơi da xanh / Quỷ chim ở 73/74/76/77/81/82 | Như trên. | Không có bang? Hạ 200 Dơi da xanh hoặc Quỷ chim ở map 73/74/76/77/81/82 |
| 36.2 | Hạ boss Drabura/20 Cadic M | Drabura (phi thuyền) hoặc mob 95 chỉ ở 165 Sa mạc hoang vu | Mob id 95 dùng chung với "Thỏ con" của sự kiện — bỏ ràng buộc thì thỏ sự kiện cũng được tính. | Boss Drabura trong phi thuyền (12h-12h59), hoặc 20 Cadic M ở Sa mạc hoang vu |
| 41.1 | Hạ 60 quái vành đai rừng | mọi quái ở map 27–38 | Bước theo VÙNG ("vành đai rừng"), không theo loài. Trước chỉ 5 loài (heo rừng, bulon, quỷ mập, quỷ đầu to, quỷ địa ngục) — người Namếc đánh heo da xanh / ukulele ở chính vùng đó không được tính. Nay mọi quái trong vùng đều tính. | Mọi quái ở vùng rừng quanh %16 (map 27-38, cả 3 hành tinh) đều tính |
| 43.1 | Tích 160 điểm diệt quái | quái phó bản Khí gas 147/149/151/152 (2 điểm) hoặc mọi quái ở 155/160/161 (1 điểm) | Đường vòng thay phó bản Khí gas hủy diệt (doc 32). | Phó bản: mỗi quái 2 điểm. Ngoài: chỉ quái ở Hành tinh ngục tù, Khu hang động, Bìa rừng |
| 43.2 | Hạ boss Lychee hoặc 50 quái | boss Dr Lychee (phó bản) hoặc quái 155/160/161 | Như trên. | Không có bang? Hạ 50 quái ở Hành tinh ngục tù hoặc Khu hang động, Bìa rừng |
| 43.3 | Hạ boss Hatchiyack/70 quái | boss Hatchiyack (phó bản) hoặc quái 155/160/161 | Như trên. | Không có bang? Hạ 70 quái ở Hành tinh ngục tù hoặc Khu hang động, Bìa rừng |
| 43.4 | Xong khí gas hoặc 100 quái | phá Khí gas, hoặc quái 155/160/161 | Như trên. | Không có bang? Hạ 100 quái ở Hành tinh ngục tù hoặc Khu hang động, Bìa rừng |

Ghi chú: 42.2 "Phá 60 lồng giam, 10 phút" (Khỉ lông xanh, Taburine Đỏ) đã bỏ điều kiện `mapId == 155` cho đồng bộ — hai loài này chỉ có ở 155 nên không đổi gì.

## 6. Mũi tên chỉ đường

### 6.1 Thống kê trước khi sửa

| Loại lệch | Số bước | Bước |
|---|---|---|
| Mũi tên trỏ tới map mà bước **không tính** | 2 | 3.1, 37.2 |
| Bước có chỗ cụ thể nhưng **không có mũi tên** (-1) | 18 | 0.0, 1.0, 6.0, 6.1, 7.0, 8.2, 12.1, 13.1, 14.2, 15.0, 15.1, 16.0, 16.1, 16.2, 16.3, 18.2, 20.3, 48.3 |
| Mũi tên trỏ **vào phó bản** (không đi bộ tới được; người không bang không vào được) | 8 | 21.1 (53), 35.1 (143), 35.2 (141), 35.3 (144), 43.1 (147), 43.2 (148), 43.3 (148), 43.4 (147) |
| Mũi tên trỏ vào map **chỉ mở theo giờ** trong khi có đường vòng 24/7 | 3 | 37.1, 37.2, 37.3 (120 Phòng chỉ huy — phi thuyền Babiđây) |
| NPC của bước không đứng ở map mũi tên | 2 | 35.1 (NPC 20 Thần Vũ Trụ ở map 143), 39.2 (NPC 5 "Con mèo" ở làng) |
| Có mũi tên nhưng dẫn tới NPC phụ, bỏ qua đường chính mọi người đều đi được | 1 | 21.2 (Độc Nhãn ở 57, chỉ vào được khi phá xong doanh trại; Lính canh ở 27 cũng hoàn thành bước) |

Tính "lệch" theo từng hành tinh (mũi tên sau `transformMapId` so với tập map mà code tính được). 37.2 là lệch thật: mũi tên 120, còn Drabura 3 ở 114 và Quỷ chim (cũ) chỉ tính ở 126.

### 6.2 Sau khi sửa

* Lệch còn lại: **1** — 3.1 (NV 3, cấm đổi cột `map`, xem mục 9).
* Không mũi tên còn lại: **2** — 0.0 và 1.0 (NV 0–3, giữ nguyên tuyến gốc).
* 33 bước đổi mũi tên, 3 bước đổi NPC. Mọi mũi tên mới trỏ tới map **gần nhất có quái/boss đó, đúng hành tinh** của người chơi và **nằm trong tập map bước tính được**.

| Bước | Mũi tên cũ | Mũi tên mới | Vì sao chọn map này |
|---|---|---|---|
| 6.0 | — | TĐ: 3 Rừng nấm / NM: 11 Thung lũng Maima / XD: 17 Rừng nguyên sinh | map quái bay đầu tiên (liền sau bước 5.0) |
| 6.1 | — | TĐ: 4 Rừng xương / NM: 12 Vực maima / XD: 18 Rừng thông Xayda | boss chỉ đứng ở 4/12/18 |
| 7.0 | — | TĐ: 4 Rừng xương / NM: 12 Vực maima / XD: 18 Rừng thông Xayda | map quái mẹ duy nhất đã mở ở NV 7 |
| 8.2 | — | TĐ: 4 Rừng xương / NM: 12 Vực maima / XD: 18 Rừng thông Xayda | như 7.0 |
| 12.1 | — | TĐ: 4 Rừng xương / NM: 12 Vực maima / XD: 18 Rừng thông Xayda | như 7.0 |
| 13.1 | — | TĐ: 4 Rừng xương / NM: 12 Vực maima / XD: 18 Rừng thông Xayda | như 7.0 |
| 14.2 | — | TĐ: 27 Rừng Bamboo / NM: 31 Núi hoa vàng / XD: 35 Rừng cọ | map heo đã mở ở NV 14 (28/32/36 mở từ NV 16) |
| 15.0 | — | TĐ: 27 Rừng Bamboo / NM: 31 Núi hoa vàng / XD: 35 Rừng cọ | điểm hẹn, cùng map với boss Jaco |
| 15.1 | — | TĐ: 27 Rừng Bamboo / NM: 31 Núi hoa vàng / XD: 35 Rừng cọ | boss chỉ đứng ở 27/31/35 |
| 16.0 | — | TĐ: 29 Nam Kamê / NM: 33 Nam Guru / XD: 37 Thung lũng đen | đúng map code kiểm (theo hành tinh) |
| 16.1 | — | TĐ: 29 Nam Kamê / NM: 33 Nam Guru / XD: 37 Thung lũng đen | map đầu tiên có quái này |
| 16.2 | — | TĐ: 30 Đảo Bulông / NM: 34 Đông Nam Guru / XD: 38 Bờ vực đen | map có quái này |
| 16.3 | — | TĐ: 30 Đảo Bulông / NM: 34 Đông Nam Guru / XD: 38 Bờ vực đen | như 16.2 |
| 18.2 | — | 19 Thành phố Vegeta | Thành phố Vegeta — cùng map với bước 18.1, có Akkuman |
| 20.3 | — | 68 Thung lũng Nappa | Thung lũng Nappa: có Kuku, có Cui chỉ đường tới boss, cạnh 63–67 / 74–77 |
| 20.4 | — | 68 Thung lũng Nappa | như 20.3 |
| 21.1 | 53 Tường thành 1 | 63 Trại lính Fide | đường vòng mọi người đi được |
| 21.2 | 57 Tầng 4 | 27 Rừng Bamboo (npc 26 → 25) | Lính canh ở 27 hoàn thành bước cho mọi người |
| 35.1 | 143 Con đường rắn độc | 81 Hang quỷ chim (npc 20 → -1) | cụm đường vòng, Hang quỷ chim có Quỷ chim |
| 35.2 | 141 Con đường rắn độc | 81 Hang quỷ chim | như 35.1 |
| 35.3 | 144 Hoang mạc | 81 Hang quỷ chim | như 35.1 |
| 37.1 | 120 Phòng chỉ huy | 126 Thành phố Santa | Hirudegarn ở 126, đánh 24/7 |
| 37.2 | 120 Phòng chỉ huy | 126 Thành phố Santa | Quỷ chim có ở 126 |
| 37.3 | 120 Phòng chỉ huy | 126 Thành phố Santa | như 37.1 |
| 39.2 | TĐ: 0 Làng Aru / NM: 7 Làng Mori / XD: 14 Làng Kakarot | TĐ: 0 Làng Aru / NM: 7 Làng Mori / XD: 14 Làng Kakarot (npc 5 → -1) | NPC không đứng ở map đó |
| 41.1 | 27 Rừng Bamboo | TĐ: 27 Rừng Bamboo / NM: 31 Núi hoa vàng / XD: 35 Rừng cọ | vành đai rừng của hành tinh mình |
| 41.2 | 27 Rừng Bamboo | TĐ: 27 Rừng Bamboo / NM: 31 Núi hoa vàng / XD: 35 Rừng cọ | Broly lang thang 27–38 |
| 41.3 | 27 Rừng Bamboo | TĐ: 27 Rừng Bamboo / NM: 31 Núi hoa vàng / XD: 35 Rừng cọ | như 41.2 |
| 43.1 | 147 Sa mạc | 155 Hành tinh ngục tù | đường vòng mọi người đi được |
| 43.2 | 148 Lâu đài Lychee | 155 Hành tinh ngục tù | như 43.1 |
| 43.3 | 148 Lâu đài Lychee | 155 Hành tinh ngục tù | như 43.1 |
| 43.4 | 147 Sa mạc | 155 Hành tinh ngục tù | như 43.1 |
| 48.3 | — | 68 Thung lũng Nappa | như 20.3 |
| 48.4 | — | 68 Thung lũng Nappa | như 20.3 |

## 7. Boss của tuyến: map xuất hiện theo hành tinh và số bản

| Bước | Boss | `mapJoin` | TĐ / NM / XD | Loại | Hồi sinh | Số bản (`BossManager`) |
|---|---|---|---|---|---|---|
| 6.1 | Kẻ Thu Gom | 4, 12, 18 | 4 / 12 / 18 — mỗi hành tinh 1 map | QuestBoss | 15–30 phút | 3 → **6** |
| 15.1 | Jaco (dạng 2 mới tính) | 27, 31, 35 | 27 / 31 / 35 — mỗi hành tinh 1 map | QuestBoss | 15–30 phút | 3 → **6** |
| 20.3 | Kuku / Mập Đầu Đinh / Rambo | 68–72 / 63–67 / 74–77 | chung (cụm Xayda, ai cũng tới được từ NV 19) | thế giới | 10 phút | 5 mỗi loại |
| 22.2 | Số 1–4 + Tiểu đội trưởng | 79, 81, 82, 83 | chung | thế giới | 5 phút | 1 nhóm (bản "Namek" 73–77 KHÔNG tính) |
| 23.3 | Fide đại ca | 80 | chung | thế giới | 10 phút | 1 |
| 25.1 | Android 19 + Dr.Kôrê | 96, 94, 93 | chung | thế giới | 10 phút | 1 |
| 27.2 | Android 13/14/15 | 104 | chung | thế giới | 10 phút | 1 |
| 28.2 | Poc / Pic / King Kong | 97, 98, 99 | chung | thế giới | 10 phút | 1 |
| 30.2 | Xên bọ hung | 100 | chung | thế giới + QuestBoss | 30 phút / 15–30 phút | 1 + 3 |
| 34.4 | Cooler | 110 | chung | thế giới + QuestBoss | 30 phút / 15–30 phút | 1 + 3 |
| 36.2 | Drabura | 114 (phi thuyền 12h) | chung | sự kiện giờ | — | — |
| 37.1 | Mabư | 120 (12h) / 127 (bản NV) | chung | sự kiện + QuestBoss | — | 3 (bản NV) |
| 38.2 | Black Goku | 102, 92–100 | chung | thế giới + QuestBoss | 5 phút / 15–30 phút | 2 + 3 |
| 39.6 | Baby | 14 Làng Kakarot | chung | thế giới + QuestBoss | 15 phút / 15–30 phút | 2 + 3 |
| 41.2 | Broly / Super Broly | 5, 13, 20, 27–38 (khu ≥ 2) | cả 3 hành tinh | sự kiện Default | — | 30 |
| 42.3 | Cumber | 155 | chung | thế giới + QuestBoss | 5 phút / 15–30 phút | 1 + 3 |
| 46.1 | Heart (4 dạng) | 166 / 145 / 145 / 145, 155 | chung | QuestBoss | 15–30 phút | 3 |

Chỉ **Kẻ Thu Gom** và **Jaco** có `mapJoin` trải 3 hành tinh, mỗi hành tinh đúng 1 map. `QuestBoss.findRandomZone` (bản vừa sửa) đặt bản mới vào map đang ít bản cùng loại nhất ⇒ với N bản, mỗi map có ⌊N/3⌋–⌈N/3⌉ bản. 3 bản đủ điều kiện "≥ số map", nhưng khi bản duy nhất của một map vừa chết thì cả hành tinh đó chờ 15–30 phút, mà ở NV 6 người chơi **chưa đi tàu sang hành tinh khác được** (trạm tàu mở ở `TASK_7_1`). Vì vậy tăng lên **6** (2 bản mỗi map). Boss bản nhiệm vụ chỉ rơi đồ nhiệm vụ (Kẻ Thu Gom không rơi gì) nên không mở thêm đường farm.

## 8. Bảng đối chiếu đầy đủ 237 bước

Cột:
* **Điều kiện server** — sau sửa; phần in đậm "chỉ map … → mọi map" là ràng buộc đã bỏ.
* **Mũi tên** — cột `map` sau `transformMapId`, dạng `TĐ / NM / XD` khi khác nhau; "—" = -1 không chỉ đường;
  in đậm = giá trị mới.
* **Map tính được** — sau sửa: map có quái (theo `map_template.mobs`), `mapJoin` của boss, map NPC mà code kiểm, v.v.
  Có cả map phó bản / sự kiện / 164 "Map riêng tư" nếu quái có mặt ở đó.
* **Tên bước / Nhắc** — chữ còn placeholder; "cũ → **mới**" khi đổi, ~~gạch~~ = chữ cũ.
* **Cờ (trước sửa)** — LỆCH = mũi tên trỏ map bước không tính; "không mũi tên" = bước có chỗ cụ thể mà `map = -1`;
  THIẾU NGUỒN ĐỒ = vật phẩm cần nhặt không được rơi/đặt ở đâu trong code (mục 9).

| Bước | Loại | Điều kiện server | Mũi tên | Map tính được | Tên bước | Nhắc (`notify`) | Cờ (trước sửa) |
|---|---|---|---|---|---|---|---|
| 0.0 | Tới map | vào map | — | 39 Vách núi Aru, 40 Vách núi Moori, 41 Vực Plant | Đi theo mũi tên chỉ dẫn | *(trống)* | không mũi tên |
| 0.1 | Tới map | vào map | TĐ: 21 Nhà Gôhan / NM: 22 Nhà Moori / XD: 23 Nhà Broly | TĐ: 21 Nhà Gôhan / NM: 22 Nhà Moori / XD: 23 Nhà Broly | Về nhà %2 | *(trống)* |  |
| 0.2 | Gặp NPC | NPC ông (NPC 0/2/1) | TĐ: 21 Nhà Gôhan / NM: 22 Nhà Moori / XD: 23 Nhà Broly | TĐ: 21 Nhà Gôhan / NM: 22 Nhà Moori / XD: 23 Nhà Broly | Gặp %2 | *(trống)* |  |
| 0.3 | Khác | mở rương (NPC 3) ở nhà | TĐ: 21 Nhà Gôhan / NM: 22 Nhà Moori / XD: 23 Nhà Broly | TĐ: 21 Nhà Gôhan / NM: 22 Nhà Moori / XD: 23 Nhà Broly | Mở rương đồ lấy rađa | *(trống)* |  |
| 0.4 | Khác | menu cây đậu (NPC 4), mục 0 | TĐ: 21 Nhà Gôhan / NM: 22 Nhà Moori / XD: 23 Nhà Broly | TĐ: 21 Nhà Gôhan / NM: 22 Nhà Moori / XD: 23 Nhà Broly | Thu hoạch đậu thần | *(trống)* |  |
| 0.5 | Gặp NPC | NPC ông (NPC 0/2/1) | TĐ: 21 Nhà Gôhan / NM: 22 Nhà Moori / XD: 23 Nhà Broly | TĐ: 21 Nhà Gôhan / NM: 22 Nhà Moori / XD: 23 Nhà Broly | Về báo %2 | *(trống)* |  |
| 1.0 | Đánh quái | mob 0 Mộc nhân; mọi map | — | 0 Làng Aru, 7 Làng Mori, 14 Làng Kakarot, 52 Đại hội võ thuật, 78 Lãnh địa Fize | Đánh ngã 5 mộc nhân | Mộc nhân cũ vẫn đứng ở %1. Đánh ngã 5 con cho ông xem | không mũi tên |
| 1.1 | Gặp NPC | NPC ông (NPC 0/2/1) | TĐ: 21 Nhà Gôhan / NM: 22 Nhà Moori / XD: 23 Nhà Broly | TĐ: 21 Nhà Gôhan / NM: 22 Nhà Moori / XD: 23 Nhà Broly | Về khoe với %2 | Mộc nhân đổ cả rồi. Về khoe với %2 thôi |  |
| 2.0 | Nhặt đồ | item 73: mob 1/2/3 (Mob.dropItemTask, mọi map) | TĐ: 1 Đồi hoa cúc / NM: 8 Đồi nấm tím / XD: 15 Đồi hoang | 1 Đồi hoa cúc, 2 Thung lũng tre, 3 Rừng nấm, 8 Đồi nấm tím, 9 Thị trấn Moori, 11 Thung lũng Maima, 15 Đồi hoang, 16 Làng Plant … (+2) | Hạ %4 lấy 10 đùi gà | Lên %3 hạ lũ %4, nhặt đủ 10 đùi gà |  |
| 2.1 | Gặp NPC | NPC ông (NPC 0/2/1) | TĐ: 21 Nhà Gôhan / NM: 22 Nhà Moori / XD: 23 Nhà Broly | TĐ: 21 Nhà Gôhan / NM: 22 Nhà Moori / XD: 23 Nhà Broly | Đưa đùi gà cho %2 | Đủ 10 đùi gà rồi. Mang về cho %2 kẻo ông đói |  |
| 3.0 | Khác | cộng điểm tiềm năng | — | mọi map | Cộng điểm tiềm năng | *(trống)* |  |
| 3.1 | Nhặt đồ | item 78: rải sẵn (Map.initItem) ở 42/43/44 | TĐ: 39 Vách núi Aru / NM: 40 Vách núi Moori / XD: 41 Vực Plant | 42 Vách núi Aru, 43 Vách núi Moori, 44 Vách núi Kakarot | Tìm vật thể lạ rơi xuống → **Tìm đồ lạ ở %5** | *(trống)* | LỆCH |
| 3.2 | Gặp NPC | NPC ông (NPC 0/2/1) | TĐ: 21 Nhà Gôhan / NM: 22 Nhà Moori / XD: 23 Nhà Broly | TĐ: 21 Nhà Gôhan / NM: 22 Nhà Moori / XD: 23 Nhà Broly | Đưa vật lạ cho %2 | Mang thứ vừa tìm được về cho %2 xem |  |
| 4.0 | Đánh quái | mob 1 Khủng long, 2 Lợn lòi, 3 Quỷ đất; **chỉ map 2/9/16 (theo hành tinh) → mọi map** | TĐ: 2 Thung lũng tre / NM: 9 Thị trấn Moori / XD: 16 Làng Plant | 1 Đồi hoa cúc, 2 Thung lũng tre, 3 Rừng nấm, 8 Đồi nấm tím, 9 Thị trấn Moori, 11 Thung lũng Maima, 15 Đồi hoang, 16 Làng Plant … (+2) | Hạ 12 %4 | ~~Tới %6, hạ 12 %4 đang phát điên~~ → **%4 có ở %3 và %6. Hạ ở map nào cũng tính** |  |
| 4.1 | Đánh quái | mob 4 Khủng long mẹ, 5 Lợn lòi mẹ, 6 Quỷ đất mẹ; **chỉ map 2/9/16 (theo hành tinh) → mọi map** | TĐ: 2 Thung lũng tre / NM: 9 Thị trấn Moori / XD: 16 Làng Plant | 2 Thung lũng tre, 3 Rừng nấm, 4 Rừng xương, 9 Thị trấn Moori, 11 Thung lũng Maima, 12 Vực maima, 16 Làng Plant, 17 Rừng nguyên sinh … (+3) | Hạ 15 %4 mẹ | ~~Hạ 15 %4 mẹ ở %6, chúng cũng biến dạng rồi~~ → **%4 mẹ có ở %6 (và các khu rừng phía sau). Hạ ở map nào cũng tính** |  |
| 4.2 | Gặp NPC | NPC ông (NPC 0/2/1) | TĐ: 21 Nhà Gôhan / NM: 22 Nhà Moori / XD: 23 Nhà Broly | TĐ: 21 Nhà Gôhan / NM: 22 Nhà Moori / XD: 23 Nhà Broly | Về kể cho %2 | Về nhà kể cho %2 chuyện ở %6 |  |
| 5.0 | Nhặt đồ | item 2010: mob 7/8/9 (mọi map) | TĐ: 3 Rừng nấm / NM: 11 Thung lũng Maima / XD: 17 Rừng nguyên sinh | 3 Rừng nấm, 4 Rừng xương, 11 Thung lũng Maima, 12 Vực maima, 17 Rừng nguyên sinh, 18 Rừng thông Xayda, 164 Map riêng tư | Tìm Kỷ Vật Của Ông | ~~Hạ %9 trong rừng, kỷ vật sẽ rơi ra~~ → **Hạ %9 ở %13 hoặc %15, kỷ vật sẽ rơi ra** |  |
| 5.1 | Khác | dùng item 2010 | — | mọi map | Lau sạch Kỷ Vật | Mở hành trang, dùng Kỷ Vật Của Ông để lau sạch |  |
| 5.2 | Gặp NPC | NPC ông (NPC 0/2/1) | TĐ: 21 Nhà Gôhan / NM: 22 Nhà Moori / XD: 23 Nhà Broly | TĐ: 21 Nhà Gôhan / NM: 22 Nhà Moori / XD: 23 Nhà Broly | Đưa kỷ vật cho %2 | Mang kỷ vật về nhà đưa cho %2 |  |
| 6.0 | Đánh quái | mob 7 Thằn lằn bay, 8 Phi long, 9 Quỷ bay; **chỉ map 4/12/18 → mọi map** | — → **TĐ: 3 Rừng nấm / NM: 11 Thung lũng Maima / XD: 17 Rừng nguyên sinh** | 3 Rừng nấm, 4 Rừng xương, 11 Thung lũng Maima, 12 Vực maima, 17 Rừng nguyên sinh, 18 Rừng thông Xayda, 164 Map riêng tư | Hạ 15 %9 | ~~Lần theo tiếng rít vào rừng sâu, hạ 15 %9~~ → **%9 có ở %13 và %15. Hạ ở map nào cũng tính** | không mũi tên |
| 6.1 | Boss | boss Kẻ Thu Gom (-2000, QuestBoss ×6) | — → **TĐ: 4 Rừng xương / NM: 12 Vực maima / XD: 18 Rừng thông Xayda** | 4 Rừng xương, 12 Vực maima, 18 Rừng thông Xayda | Hạ Kẻ Thu Gom → **Hạ boss Kẻ Thu Gom** | ~~Kẻ Thu Gom đang lảng vảng trong rừng sâu~~ → **Boss Kẻ Thu Gom xuất hiện ở %15, hồi sinh 15–30 phút** | không mũi tên |
| 6.2 | Gặp NPC | NPC ông (NPC 0/2/1) | TĐ: 21 Nhà Gôhan / NM: 22 Nhà Moori / XD: 23 Nhà Broly | TĐ: 21 Nhà Gôhan / NM: 22 Nhà Moori / XD: 23 Nhà Broly | Về báo %2 | Về nhà báo cho %2 biết chuyện |  |
| 7.0 | Đánh quái | mob 10 Thằn lằn mẹ, 11 Phi long mẹ, 12 Quỷ bay mẹ; **chỉ map 4/12/18 → mọi map** (3 phút) | — → **TĐ: 4 Rừng xương / NM: 12 Vực maima / XD: 18 Rừng thông Xayda** | 4 Rừng xương, 12 Vực maima, 18 Rừng thông Xayda, 27 Rừng Bamboo, 28 Rừng dương xỉ, 31 Núi hoa vàng, 32 Núi hoa tím, 35 Rừng cọ … (+1) | Hạ 10 quái mẹ trong 3 phút | ~~Còn 3 phút! Hạ quái mẹ ở rừng sâu, hết giờ phải đếm lại từ đầu~~ → **Còn 3 phút! %14 có ở %15; hết giờ phải đếm lại từ đầu** | không mũi tên |
| 7.1 | Tới map | vào map | TĐ: 24 Trạm tàu vũ trụ / NM: 25 Trạm tàu vũ trụ / XD: 26 Trạm tàu vũ trụ | TĐ: 24 Trạm tàu vũ trụ / NM: 25 Trạm tàu vũ trụ / XD: 26 Trạm tàu vũ trụ | Chạy tới Trạm tàu vũ trụ | Chạy ngay tới Trạm tàu vũ trụ |  |
| 7.2 | Gặp NPC | NPC Jaco 63 | TĐ: 24 Trạm tàu vũ trụ / NM: 25 Trạm tàu vũ trụ / XD: 26 Trạm tàu vũ trụ | TĐ: 24 Trạm tàu vũ trụ / NM: 25 Trạm tàu vũ trụ / XD: 26 Trạm tàu vũ trụ | Gặp Jaco | Jaco đang đợi ở Trạm tàu vũ trụ |  |
| 8.0 | Gặp NPC | NPC Bunma 7 | 84 Siêu Thị | 84 Siêu Thị | Gặp Bunma ở Siêu Thị | Bunma đang đợi ở Siêu Thị |  |
| 8.1 | Khác | mua item 12 ở shop 1/2/3 (Bunma/Dende/Appule) | 84 Siêu Thị | 0 Làng Aru, 7 Làng Mori, 14 Làng Kakarot, 84 Siêu Thị | Mua 1 Rada cấp 1 | Mua 1 Rada cấp 1 ở cửa hàng trong Siêu Thị |  |
| 8.2 | Đánh quái | mob 10 Thằn lằn mẹ, 11 Phi long mẹ, 12 Quỷ bay mẹ; **chỉ map 4/12/18 → mọi map** | — → **TĐ: 4 Rừng xương / NM: 12 Vực maima / XD: 18 Rừng thông Xayda** | 4 Rừng xương, 12 Vực maima, 18 Rừng thông Xayda, 27 Rừng Bamboo, 28 Rừng dương xỉ, 31 Núi hoa vàng, 32 Núi hoa tím, 35 Rừng cọ … (+1) | Hạ 25 quái mẹ lấy lõi | ~~Hạ thằn lằn mẹ, phi long mẹ hoặc quỷ bay mẹ ở rừng sâu~~ → **Thằn lằn mẹ, phi long mẹ, quỷ bay mẹ đều tính; gần nhất ở %15** | không mũi tên |
| 9.0 | Tới map | vào map | TĐ: 5 Đảo Kamê / NM: 13 Đảo Guru / XD: 20 Vách núi đen | TĐ: 5 Đảo Kamê / NM: 13 Đảo Guru / XD: 20 Vách núi đen | Bay tới %11 | Bay tới %11 tìm sư phụ |  |
| 9.1 | Đánh quái | mob 13 Ốc mượn hồn, 14 Ốc sên, 15 Heo Xayda mẹ; **chỉ map 5/13/20 (theo hành tinh) → mọi map** | TĐ: 5 Đảo Kamê / NM: 13 Đảo Guru / XD: 20 Vách núi đen | 5 Đảo Kamê, 13 Đảo Guru, 20 Vách núi đen, 29 Nam Kamê, 33 Nam Guru, 37 Thung lũng đen, 183 Thành cổ 1 | Hạ 20 %12 | ~~%12 bám đầy quanh nhà sư phụ. Hạ 20 con~~ → **%12 có ở %11, quanh nhà sư phụ. Hạ 20 con** |  |
| 9.2 | Gặp NPC | NPC sư phụ 13/14/15 | TĐ: 5 Đảo Kamê / NM: 13 Đảo Guru / XD: 20 Vách núi đen | TĐ: 5 Đảo Kamê / NM: 13 Đảo Guru / XD: 20 Vách núi đen | Gặp %10 | %10 đang đợi ngươi |  |
| 10.0 | Gặp NPC | NPC sư phụ | TĐ: 5 Đảo Kamê / NM: 13 Đảo Guru / XD: 20 Vách núi đen | TĐ: 5 Đảo Kamê / NM: 13 Đảo Guru / XD: 20 Vách núi đen | Bái %10 làm thầy | Nói chuyện với %10 để xin làm đệ tử |  |
| 10.1 | Khác | học chiêu 1/3/5 (ở sư phụ) | TĐ: 5 Đảo Kamê / NM: 13 Đảo Guru / XD: 20 Vách núi đen | TĐ: 5 Đảo Kamê / NM: 13 Đảo Guru / XD: 20 Vách núi đen | Học chưởng cấp 1 | Học chưởng cấp 1 từ %10 |  |
| 10.2 | Khác | sức mạnh ≥ 250.000 | — | mọi map | Đạt 250.000 sức mạnh | Luyện tập đến khi đạt 250.000 sức mạnh |  |
| 11.0 | Khác | hái đậu (cây ở nhà) | TĐ: 21 Nhà Gôhan / NM: 22 Nhà Moori / XD: 23 Nhà Broly | TĐ: 21 Nhà Gôhan / NM: 22 Nhà Moori / XD: 23 Nhà Broly | Thu hoạch 5 hạt đậu | Về nhà hái đậu thần trên cây |  |
| 11.1 | Khác | dùng item 2013 ở nhà | TĐ: 21 Nhà Gôhan / NM: 22 Nhà Moori / XD: 23 Nhà Broly | TĐ: 21 Nhà Gôhan / NM: 22 Nhà Moori / XD: 23 Nhà Broly | Gieo Hạt Giống Hy Vọng | Dùng Hạt Giống Hy Vọng khi đang ở nhà |  |
| 11.2 | Gặp NPC | NPC ông (NPC 0/2/1) | TĐ: 21 Nhà Gôhan / NM: 22 Nhà Moori / XD: 23 Nhà Broly | TĐ: 21 Nhà Gôhan / NM: 22 Nhà Moori / XD: 23 Nhà Broly | Khoe cây mới với %2 | Cây đã khỏe lại. Khoe với %2 thôi |  |
| 12.0 | Khác | có đệ tử (trứng ở nhà) | TĐ: 21 Nhà Gôhan / NM: 22 Nhà Moori / XD: 23 Nhà Broly | TĐ: 21 Nhà Gôhan / NM: 22 Nhà Moori / XD: 23 Nhà Broly | Nở trứng nhận đệ tử | Chạm vào Quả trứng ở nhà, chọn Nở trứng |  |
| 12.1 | Đánh quái | mob 10 Thằn lằn mẹ, 11 Phi long mẹ, 12 Quỷ bay mẹ; mọi map | — → **TĐ: 4 Rừng xương / NM: 12 Vực maima / XD: 18 Rừng thông Xayda** | 4 Rừng xương, 12 Vực maima, 18 Rừng thông Xayda, 27 Rừng Bamboo, 28 Rừng dương xỉ, 31 Núi hoa vàng, 32 Núi hoa tím, 35 Rừng cọ … (+1) | Cùng đệ tử hạ 25 quái mẹ | ~~Dẫn đệ tử đi hạ quái mẹ~~ → **Dẫn đệ tử đi hạ %14 ở %15; quái mẹ loại nào cũng tính** | không mũi tên |
| 12.2 | Gặp NPC | NPC ông (NPC 0/2/1) | TĐ: 21 Nhà Gôhan / NM: 22 Nhà Moori / XD: 23 Nhà Broly | TĐ: 21 Nhà Gôhan / NM: 22 Nhà Moori / XD: 23 Nhà Broly | Dẫn đệ tử gặp %2 | Về nhà giới thiệu đệ tử với %2 |  |
| 13.0 | Khác | vào bang | — | mọi map | Gia nhập 1 bang hội | Tạo bang hoặc xin vào một bang hội |  |
| 13.1 | Đánh quái | mob 10 Thằn lằn mẹ, 11 Phi long mẹ, 12 Quỷ bay mẹ; mọi map (≥2 người cùng bang cùng khu) | — → **TĐ: 4 Rừng xương / NM: 12 Vực maima / XD: 18 Rừng thông Xayda** | 4 Rừng xương, 12 Vực maima, 18 Rừng thông Xayda, 27 Rừng Bamboo, 28 Rừng dương xỉ, 31 Núi hoa vàng, 32 Núi hoa tím, 35 Rừng cọ … (+1) | Cùng bạn bang hạ 30 quái mẹ | ~~Cần ít nhất 1 bạn cùng bang ở cùng khu; từ 3 người mỗi con tính 2~~ → **Quái mẹ ở %15. Cần 1 bạn cùng bang cùng khu; từ 3 người mỗi con tính 2** | không mũi tên |
| 13.2 | Gặp NPC | NPC Giu-ma 47 | 153 Lãnh địa Bang Hội | 153 Lãnh địa Bang Hội | Gặp Giu-ma Đầu Bò | Giu-ma Đầu Bò ở Lãnh địa Bang Hội |  |
| 14.0 | Gặp NPC | NPC Bunma TL 37 | 102 Nhà Bunma | 102 Nhà Bunma | Gặp Bunma ở Nhà Bunma | Bunma có tin mới, gặp cô ở Nhà Bunma |  |
| 14.1 | Khác | mua bất kỳ ở shop Uron (4) | 84 Siêu Thị | 84 Siêu Thị | Mua 1 món ở quầy Uron | Uron bán hàng ở Siêu Thị. Mua món gì cũng được |  |
| 14.2 | Đánh quái | mob 16 Heo rừng, 17 Heo da xanh, 18 Heo Xayda; **chỉ map 27/31/35 → mọi map** | — → **TĐ: 27 Rừng Bamboo / NM: 31 Núi hoa vàng / XD: 35 Rừng cọ** | 27 Rừng Bamboo, 28 Rừng dương xỉ, 31 Núi hoa vàng, 32 Núi hoa tím, 35 Rừng cọ, 36 Rừng đá | Hạ 20 heo chở hàng | ~~Heo chở hàng đi qua Rừng Bamboo, Núi hoa vàng, Rừng cọ~~ → **Heo rừng, heo da xanh, heo Xayda đều tính; gần nhất ở %16** | không mũi tên |
| 15.0 | Đánh quái | mob 10 Thằn lằn mẹ, 11 Phi long mẹ, 12 Quỷ bay mẹ; **chỉ map 27/31/35 → mọi map** | — → **TĐ: 27 Rừng Bamboo / NM: 31 Núi hoa vàng / XD: 35 Rừng cọ** | 4 Rừng xương, 12 Vực maima, 18 Rừng thông Xayda, 27 Rừng Bamboo, 28 Rừng dương xỉ, 31 Núi hoa vàng, 32 Núi hoa tím, 35 Rừng cọ … (+1) | Hạ 20 quái mẹ ở điểm hẹn → **Hạ 20 quái mẹ** | ~~Điểm hẹn ở Rừng Bamboo, Núi hoa vàng hoặc Rừng cọ~~ → **Điểm hẹn là %16; quái mẹ ở %15 cũng tính** | không mũi tên |
| 15.1 | Boss | boss Jaco (-2001) dạng 2 (QuestBoss ×6) | — → **TĐ: 27 Rừng Bamboo / NM: 31 Núi hoa vàng / XD: 35 Rừng cọ** | 27 Rừng Bamboo, 31 Núi hoa vàng, 35 Rừng cọ | Đánh bại Jaco mất ký ức → **Hạ boss Jaco mất ký ức** | ~~Jaco đã bị xóa ký ức. Đánh gục hắn để hắn tỉnh lại~~ → **Boss Jaco ở %16, hạ cả 2 dạng; hồi sinh 15–30 phút** | không mũi tên |
| 15.2 | Gặp NPC | NPC Jaco 63 | TĐ: 24 Trạm tàu vũ trụ / NM: 25 Trạm tàu vũ trụ / XD: 26 Trạm tàu vũ trụ | TĐ: 24 Trạm tàu vũ trụ / NM: 25 Trạm tàu vũ trụ / XD: 26 Trạm tàu vũ trụ | Gặp Jaco ở Trạm tàu vũ trụ | Jaco đã tỉnh, hắn đợi ngươi ở Trạm tàu vũ trụ |  |
| 16.0 | Tới map | vào map | — → **TĐ: 29 Nam Kamê / NM: 33 Nam Guru / XD: 37 Thung lũng đen** | TĐ: 29 Nam Kamê / NM: 33 Nam Guru / XD: 37 Thung lũng đen | Đi về vùng đất phía Nam → **Tới %17** | ~~Tới Nam Kamê, Nam Guru hoặc Thung lũng đen (theo hành tinh)~~ → **Vùng phía Nam của hành tinh ngươi là %17** | không mũi tên |
| 16.1 | Đánh quái | mob 31 Không tặc, 32 Quỷ đầu to, 33 Quỷ địa ngục; **chỉ map 29/33/37 → mọi map** | — → **TĐ: 29 Nam Kamê / NM: 33 Nam Guru / XD: 37 Thung lũng đen** | 29 Nam Kamê, 30 Đảo Bulông, 33 Nam Guru, 34 Đông Nam Guru, 37 Thung lũng đen, 38 Bờ vực đen, 58 Tường thành 2, 59 Tường thành 3 … (+1) | Hạ 40 quái chắn đường → **Hạ 40 %19** | ~~Hạ Không tặc, Quỷ đầu to hoặc Quỷ địa ngục ở vùng phía Nam~~ → **%19 có ở %17 và %18; không tặc, quỷ đầu to, quỷ địa ngục đều tính** | không mũi tên |
| 16.2 | Đánh quái | mob 22 Bulon, 23 Ukulele, 24 Quỷ mập; **chỉ map 30/34/38 → mọi map** | — → **TĐ: 30 Đảo Bulông / NM: 34 Đông Nam Guru / XD: 38 Bờ vực đen** | 30 Đảo Bulông, 34 Đông Nam Guru, 38 Bờ vực đen, 59 Tường thành 3, 141 Con đường rắn độc | Hạ 30 quái canh bờ biển → **Hạ 30 %20** | ~~Hạ Bulon, Ukulele hoặc Quỷ mập ở vùng ven biển~~ → **%20 có ở %18; bulon, ukulele, quỷ mập đều tính** | không mũi tên |
| 16.3 | Nhặt đồ | item 2014: mob 22/23/24, 25% (mọi map) | — → **TĐ: 30 Đảo Bulông / NM: 34 Đông Nam Guru / XD: 38 Bờ vực đen** | 30 Đảo Bulông, 34 Đông Nam Guru, 38 Bờ vực đen, 59 Tường thành 3, 141 Con đường rắn độc | Nhặt 5 Vỏ đạn khắc dấu | ~~Vỏ đạn rơi từ Bulon, Ukulele, Quỷ mập~~ → **Vỏ đạn rơi khi hạ %20 ở %18 (bulon, ukulele, quỷ mập đều rơi)** | không mũi tên |
| 16.4 | Gặp NPC | NPC sư phụ | TĐ: 5 Đảo Kamê / NM: 13 Đảo Guru / XD: 20 Vách núi đen | TĐ: 5 Đảo Kamê / NM: 13 Đảo Guru / XD: 20 Vách núi đen | Về gặp %10 | Mang vỏ đạn về cho %10 xem |  |
| 17.0 | Gặp NPC | NPC Bà Hạt Mít 21 | TĐ: 42 Vách núi Aru / NM: 43 Vách núi Moori / XD: 44 Vách núi Kakarot | TĐ: 42 Vách núi Aru / NM: 43 Vách núi Moori / XD: 44 Vách núi Kakarot | Gặp Bà Hạt Mít | Bà Hạt Mít đang đợi ở %5 |  |
| 17.1 | Khác | nâng trang bị +2 (menu Bà Hạt Mít) | TĐ: 42 Vách núi Aru / NM: 43 Vách núi Moori / XD: 44 Vách núi Kakarot | mọi map | Nâng 1 trang bị lên +2 | Nhờ Bà Hạt Mít nâng cấp, nguyên liệu bà đã cho |  |
| 17.2 | Khác | dùng item 2015 | — | mọi map | Dùng Búa rèn cũ | Mở hành trang, dùng Búa rèn cũ |  |
| 17.3 | Gặp NPC | NPC sư phụ | TĐ: 5 Đảo Kamê / NM: 13 Đảo Guru / XD: 20 Vách núi đen | TĐ: 5 Đảo Kamê / NM: 13 Đảo Guru / XD: 20 Vách núi đen | Về khoe với %10 | Mang vũ khí mới về khoe với %10 |  |
| 18.0 | Tới map | vào map | 19 Thành phố Vegeta | 19 Thành phố Vegeta | Tới Thành phố Vegeta | Tới Thành phố Vegeta |  |
| 18.1 | Gặp NPC | NPC Tapion 53 | 19 Thành phố Vegeta | 19 Thành phố Vegeta | Gặp người lạ thổi nhạc | Người thổi nhạc đứng trong Thành phố Vegeta |  |
| 18.2 | Đánh quái | mob 25 Tambourine, 26 Drum, 27 Akkuman; **chỉ map 6/10/19 → mọi map** | — → **19 Thành phố Vegeta** | 6 Đông Karin, 10 Thung lũng Namếc, 19 Thành phố Vegeta, 68 Thung lũng Nappa, 69 Vực cấm, 142 Con đường rắn độc, 146 Tây Karin | Hạ 30 quái vây thành | ~~Hạ Tambourine, Drum hoặc Akkuman đang vây thành~~ → **Akkuman ở Thành phố Vegeta; Tambourine ở Đông Karin, Drum ở Thung lũng Namếc** | không mũi tên |
| 18.3 | Tới map | vào map | 126 Thành phố Santa | 126 Thành phố Santa | Tới Thành phố Santa | Tapion đi trước rồi. Theo anh tới Thành phố Santa |  |
| 18.4 | Gặp NPC | NPC Tapion 53 | 126 Thành phố Santa | 126 Thành phố Santa | Nghe Tapion kể chuyện | Tapion đợi ngươi ở Thành phố Santa |  |
| 19.0 | Gặp NPC | NPC Cui 12 | 68 Thung lũng Nappa | 68 Thung lũng Nappa | Gặp Cui ở Thung lũng Nappa | Cui đang đợi ở Thung lũng Nappa |  |
| 19.1 | Đánh quái | mob 39 Nappa; **chỉ map 68/69/70 → mọi map** | 68 Thung lũng Nappa | 68 Thung lũng Nappa, 69 Vực cấm, 70 Núi Appule, 156 Tây thánh địa | Hạ 60 Nappa mất trí | Nappa ở Thung lũng Nappa, Vực cấm, Núi Appule |  |
| 19.2 | Đánh quái | mob 40 Soldier; **chỉ map 69/70 → mọi map** | 69 Vực cấm | 69 Vực cấm, 70 Núi Appule, 157 Đông thánh Địa | Hạ 40 Soldier gác kho | Soldier ở Vực cấm và Núi Appule |  |
| 19.3 | Đánh quái | mob 41 Appule; **chỉ map 71/72 → mọi map** (cần 1 người khác cùng khu, ×2) | 71 Căn cứ Raspberry | 70 Núi Appule, 71 Căn cứ Raspberry, 72 Thung lũng Raspberry | Cùng bạn hạ 30 Appule | ~~Appule ở vùng Raspberry. Cần 1 người chơi khác cùng khu, mỗi con tính 2~~ → **Appule ở Núi Appule và vùng Raspberry. Cần 1 người khác cùng khu, mỗi con tính 2** |  |
| 19.4 | Gặp NPC | NPC Cui 12 | 68 Thung lũng Nappa | 68 Thung lũng Nappa | Báo cáo với Cui | Về Thung lũng Nappa báo cáo với Cui |  |
| 20.0 | Gặp NPC | NPC Berry 71 | 160 Khu hang động | 160 Khu hang động | Gặp Berry ở Khu hang động | Berry đứng ở Khu hang động |  |
| 20.1 | Khác | chọn menu Berry | 160 Khu hang động | 160 Khu hang động | Chọn: Granola hay Jaco | Điểm rẽ nhánh: chọn đi theo Granola hoặc báo Jaco |  |
| 20.2 | Gặp NPC | NPC Granola 76 | 160 Khu hang động | 160 Khu hang động | Bắt tay với Granola | Granola ở ngay Khu hang động |  |
| 20.3 | Boss | boss Kuku, Mập Đầu Đinh, Rambo | — → **68 Thung lũng Nappa** | 63 Trại lính Fide, 64 Núi dây leo, 65 Núi cây quỷ, 66 Trại qủy già, 67 Vực chết, 68 Thung lũng Nappa, 69 Vực cấm, 70 Núi Appule … (+6) | Hạ 3 tay chân của Fide → **Hạ 3 boss tay chân Fide** | ~~Săn Kuku, Mập Đầu Đinh và Rambo~~ → **Boss Kuku ở Thung lũng Nappa, Mập Đầu Đinh ở Trại lính Fide, Rambo ở Đồi cây Fide** | không mũi tên |
| 20.4 | Nhặt đồ | item 2016: KHÔNG có nguồn rơi trong code | — → **68 Thung lũng Nappa** | — | Nhặt 3 Thẻ tiền thưởng | Thẻ rơi khi hạ Kuku, Mập Đầu Đinh, Rambo | THIẾU NGUỒN ĐỒ |
| 20.5 | Gặp NPC | NPC Granola 76 | 160 Khu hang động | 160 Khu hang động | Nhận thưởng từ Granola | Về Khu hang động nhận tiền từ Granola |  |
| 21.0 | Gặp NPC | NPC Lính canh 25 | 27 Rừng Bamboo | 27 Rừng Bamboo | Gặp Lính canh ở Rừng Bamboo | Lính canh đứng ở Rừng Bamboo |  |
| 21.1 | Đánh quái | mob 43 Thằn lằn xanh, 44 Quỷ đầu nhọn, 45 Quỷ đầu vàng, 46 Quỷ da tím, 47 Quỷ già, 49 Dơi da xanh — NGOẠI LỆ: hoặc phá Doanh trại (map 53–62) | 53 Tường thành 1 → **63 Trại lính Fide** | 53 Tường thành 1, 54 Tầng 3, 55 Tầng 1, 56 Tầng 2, 57 Tầng 4, 58 Tường thành 2, 59 Tường thành 3, 60 Trại độc nhãn 1 … (+7) | Phá trại hoặc hạ 300 quái | ~~Không có bang? Hạ 300 quái ở Trại lính Fide, Núi dây leo, Núi cây quỷ, Trại quỷ già, Vực chết~~ → **Không có bang? Chỉ tính quái ở Trại lính Fide, Núi dây leo, Núi cây quỷ, Trại quỷ già, Vực chết** |  |
| 21.2 | Gặp NPC | NPC Lính canh 25 ở 27 / Độc Nhãn 26 ở 57 | 57 Tầng 4 → **27 Rừng Bamboo** (npc 26 → 25) | 27 Rừng Bamboo, 57 Tầng 4 | Lấy bản đồ hành quân | ~~Lấy từ Độc Nhãn trong doanh trại, hoặc hỏi Lính canh ở Rừng Bamboo~~ → **Hỏi Lính canh ở Rừng Bamboo, hoặc Độc Nhãn khi đã phá xong doanh trại** |  |
| 21.3 | Gặp NPC | NPC sư phụ | TĐ: 5 Đảo Kamê / NM: 13 Đảo Guru / XD: 20 Vách núi đen | TĐ: 5 Đảo Kamê / NM: 13 Đảo Guru / XD: 20 Vách núi đen | Về gặp %10 | Mang bản đồ hành quân về cho %10 |  |
| 22.0 | Gặp NPC | NPC Tapion 53 | 126 Thành phố Santa | 126 Thành phố Santa | Hỏi Tapion về máy đo lạ | Tapion ở Thành phố Santa |  |
| 22.1 | Đánh quái | mob 54 Khỉ lông đen, 55 Khỉ giáp sắt; **chỉ map 81/82/83 → mọi map** | 81 Hang quỷ chim | 81 Hang quỷ chim, 82 Núi khỉ đen, 83 Hang khỉ đen, 183 Thành cổ 1 | Hạ 40 lính khỉ canh đường | ~~Hạ Khỉ lông đen, Khỉ giáp sắt ở Hang quỷ chim, Núi khỉ đen, Hang khỉ đen~~ → **Khỉ lông đen, Khỉ giáp sắt ở Hang quỷ chim, Núi khỉ đen, Hang khỉ đen** |  |
| 22.2 | Boss | boss Số 1–4 + Tiểu đội trưởng | 79 Núi khỉ đỏ | 79 Núi khỉ đỏ, 81 Hang quỷ chim, 82 Núi khỉ đen, 83 Hang khỉ đen | Hạ 5 tên Tiểu đội sát thủ → **Hạ 5 boss Tiểu đội sát thủ** | ~~Tiểu đội sát thủ xuất hiện ở Núi khỉ đỏ~~ → **Boss ở Núi khỉ đỏ, Hang quỷ chim, Núi khỉ đen, Hang khỉ đen; hồi sinh 5 phút** |  |
| 22.3 | Nhặt đồ | item 2018: KHÔNG có nguồn rơi | 79 Núi khỉ đỏ | — | Nhặt Máy đo ký ức | Máy đo rơi khi hạ tên cuối của tiểu đội | THIẾU NGUỒN ĐỒ |
| 22.4 | Gặp NPC | NPC Tapion 53 | 126 Thành phố Santa | 126 Thành phố Santa | Đưa máy đo cho Tapion | Mang máy đo về Thành phố Santa cho Tapion |  |
| 23.0 | Khác | sức mạnh ≥ 80 triệu | — | mọi map | Đạt 80.000.000 sức mạnh | Cần 80.000.000 sức mạnh mới đủ sức đấu Fide |  |
| 23.1 | Tới map | vào map | 80 Núi khỉ vàng | 80 Núi khỉ vàng | Tới Núi khỉ vàng | Tới Núi khỉ vàng |  |
| 23.2 | Đánh quái | mob 57 Khỉ lông vàng; **chỉ map 80 → mọi map** (5 phút) | 80 Núi khỉ vàng | 80 Núi khỉ vàng, 85 Hành tinh M-2, 87 Hành tinh Cretaceous, 89 Hành tinh Rudeeze, 90 Hành tinh Gelbo, 91 Hành tinh Tigere, 122 Ngũ Hành Sơn, 124 Ngũ Hành Sơn … (+1) | Hạ 25 Khỉ lông vàng, 5 phút | Còn 5 phút! Hết giờ phải đếm lại |  |
| 23.3 | Boss | boss Fide | 80 Núi khỉ vàng | 80 Núi khỉ vàng | Hạ 2 dạng đầu của Fide → **Hạ boss Fide dạng 1 và 2** | ~~Fide đại ca xuất hiện ở Núi khỉ vàng~~ → **Boss Fide đại ca ở Núi khỉ vàng, hồi sinh 10 phút** |  |
| 23.4 | Boss | boss Fide | 80 Núi khỉ vàng | 80 Núi khỉ vàng | Hạ Fide dạng cuối → **Hạ boss Fide dạng cuối** | ~~Fide biến hình lần cuối, hạ hắn đi~~ → **Fide biến hình lần cuối ở Núi khỉ vàng, hạ hắn đi** |  |
| 23.5 | Gặp NPC | NPC sư phụ | TĐ: 5 Đảo Kamê / NM: 13 Đảo Guru / XD: 20 Vách núi đen | TĐ: 5 Đảo Kamê / NM: 13 Đảo Guru / XD: 20 Vách núi đen | Về gặp %10 | Ba hành tinh đã liên minh. Về báo tin cho %10 |  |
| 24.0 | Gặp NPC | NPC Bunma TL 37 | 102 Nhà Bunma | 102 Nhà Bunma | Gặp Bunma ở Nhà Bunma | Bunma đợi ở Nhà Bunma |  |
| 24.1 | Tới map | vào map | 92 Thành phố phía đông | 92 Thành phố phía đông | Tới Thành phố phía đông | Tới Thành phố phía đông |  |
| 24.2 | Đánh quái | mob 58 Xên con cấp 1, 59 Xên con cấp 2; **chỉ map 92/93 → mọi map** | 92 Thành phố phía đông | 92 Thành phố phía đông, 93 Thành phố phía nam | Hạ 50 Xên con cấp 1-2 | Xên con cấp 1-2 ở Thành phố phía đông và phía nam |  |
| 24.3 | Đánh quái | mob 60 Xên con cấp 3, 61 Xên con cấp  4; **chỉ map 94/96 → mọi map** | 94 Đảo Balê | 94 Đảo Balê, 96 Cao nguyên | Hạ 40 Xên con cấp 3-4 | Xên con cấp 3-4 ở Đảo Balê và Cao nguyên |  |
| 24.4 | Gặp NPC | NPC Bunma TL 37 | 102 Nhà Bunma | 102 Nhà Bunma | Báo lại cho Bunma | Về Nhà Bunma báo lại |  |
| 25.0 | Tới map | vào map | 96 Cao nguyên | 96 Cao nguyên | Tới Cao nguyên | Hai bác sĩ máy đang ở Cao nguyên |  |
| 25.1 | Boss | boss Android 19, Dr.Kôrê | 96 Cao nguyên | 93 Thành phố phía nam, 94 Đảo Balê, 96 Cao nguyên | Hạ 2 boss: Android 19, Kôrê | ~~Hạ Android 19 rồi Dr.Kôrê ở Cao nguyên~~ → **Boss ở Cao nguyên, Đảo Balê, Thành phố phía nam; hồi sinh 10 phút** |  |
| 25.2 | Nhặt đồ | item 2019: KHÔNG có nguồn rơi | 96 Cao nguyên | — | Nhặt 3 Lõi năng lượng | Lõi rơi khi hạ Android 19 và Dr.Kôrê | THIẾU NGUỒN ĐỒ |
| 25.3 | Gặp NPC | NPC Bunma TL 37 | 102 Nhà Bunma | 102 Nhà Bunma | Đưa lõi cho Bunma | Mang lõi về Nhà Bunma |  |
| 26.0 | Gặp NPC | NPC Bà Hạt Mít 21 | 5 Đảo Kamê | 5 Đảo Kamê | Gặp Bà Hạt Mít ở Đảo Kamê | Bà Hạt Mít đợi ở Đảo Kamê, nguyên liệu bà cho không |  |
| 26.1 | Khác | pha lê hóa | 5 Đảo Kamê | mọi map | Pha lê hóa 1 trang bị | Nhờ Bà Hạt Mít pha lê hóa một trang bị |  |
| 26.2 | Khác | ép sao | 5 Đảo Kamê | mọi map | Ép 1 Sao pha lê vào đồ | Nhờ Bà Hạt Mít ép sao pha lê vào trang bị vừa pha lê hóa |  |
| 26.3 | Khác | dùng item 2020 | — | mọi map | Dùng Mẫu kim loại có ký ức | Mở hành trang, dùng Mẫu kim loại có ký ức |  |
| 26.4 | Gặp NPC | NPC sư phụ | TĐ: 5 Đảo Kamê / NM: 13 Đảo Guru / XD: 20 Vách núi đen | TĐ: 5 Đảo Kamê / NM: 13 Đảo Guru / XD: 20 Vách núi đen | Về kể cho %10 | Kể cho %10 những gì ngươi nghe được |  |
| 27.0 | Gặp NPC | NPC Ca Lích 38 | 102 Nhà Bunma | 102 Nhà Bunma | Hỏi Ca Lích về container | Ca Lích ở Nhà Bunma |  |
| 27.1 | Tới map | vào map | 104 Sân sau siêu thị | 104 Sân sau siêu thị | Tới Sân sau siêu thị | Tới Sân sau siêu thị |  |
| 27.2 | Boss | boss Android 13/14/15 | 104 Sân sau siêu thị | 104 Sân sau siêu thị | Hạ 3 Android 13, 14, 15 → **Hạ 3 boss Android 13-14-15** | ~~Ba cỗ máy mẫu đang ở Sân sau siêu thị~~ → **Ba boss Android ở Sân sau siêu thị, hồi sinh 10 phút** |  |
| 27.3 | Gặp NPC | NPC Ca Lích 38 | 102 Nhà Bunma | 102 Nhà Bunma | Báo cáo với Ca Lích | Về Nhà Bunma báo cáo với Ca Lích |  |
| 28.0 | Tới map | vào map | 97 Thành phố phía bắc | 97 Thành phố phía bắc | Tới Thành phố phía bắc | Tới Thành phố phía bắc |  |
| 28.1 | Đánh quái | mob 62 Xên con cấp  5, 63 Xên con cấp  6, 64 Xên con cấp  7; **chỉ map 97/98/99 → mọi map** | 97 Thành phố phía bắc | 97 Thành phố phía bắc, 98 Ngọn núi phía bắc, 99 Thung lũng phía bắc | Hạ 60 Xên con cấp 5-7 | Xên con cấp 5-7 ở Thành phố, Ngọn núi, Thung lũng phía bắc |  |
| 28.2 | Boss | boss Poc, Pic, King Kong | 97 Thành phố phía bắc | 97 Thành phố phía bắc, 98 Ngọn núi phía bắc, 99 Thung lũng phía bắc | Hạ 3 tên Poc, Pic, King Kong → **Hạ 3 boss Poc/Pic/King Kong** | ~~Hạ Poc, Pic rồi King Kong ở Thành phố phía bắc~~ → **Boss ở Thành phố, Ngọn núi, Thung lũng phía bắc; hồi sinh 10 phút** |  |
| 28.3 | Nhặt đồ | item 2021: KHÔNG có nguồn rơi | 97 Thành phố phía bắc | — | Nhặt Mảnh giáp khắc tên | Mảnh giáp rơi khi hạ King Kong | THIẾU NGUỒN ĐỒ |
| 28.4 | Gặp NPC | NPC Bunma TL 37 | 102 Nhà Bunma | 102 Nhà Bunma | Đưa mảnh giáp cho Bunma | Mang mảnh giáp về Nhà Bunma |  |
| 29.0 | Gặp NPC | NPC Bunma TL 37 | 102 Nhà Bunma | 102 Nhà Bunma | Lấy thẻ từ giả của Bunma | Bunma đợi ở Nhà Bunma |  |
| 29.1 | Tới map | vào map | 166 Phòng thí nghiệm Myuu | 166 Phòng thí nghiệm Myuu | Vào Phòng thí nghiệm Myuu | Mang theo thẻ từ giả để vào Phòng thí nghiệm Myuu |  |
| 29.2 | Nhặt đồ | item 2023: thiết kế: rải sẵn ở 166 — code KHÔNG rải | 166 Phòng thí nghiệm Myuu | 166 Phòng thí nghiệm Myuu | Nhặt 5 Bản thiết kế, 6 phút | Còn 6 phút trước khi lọc khí chạy! Rời phòng là phải lấy lại |  |
| 29.3 | Gặp NPC | NPC Dr. Myuu 83 | 166 Phòng thí nghiệm Myuu | 166 Phòng thí nghiệm Myuu | Đối mặt Dr. Myuu | Dr. Myuu đang ở trong phòng thí nghiệm |  |
| 30.0 | Tới map | vào map | 100 Thị trấn Ginder | 100 Thị trấn Ginder | Tới Thị trấn Ginder | Tới Thị trấn Ginder |  |
| 30.1 | Đánh quái | mob 65 Xên con cấp  8; **chỉ map 100 → mọi map** | 100 Thị trấn Ginder | 100 Thị trấn Ginder | Hạ 50 Xên con cấp 8 | Xên con cấp 8 ở Thị trấn Ginder |  |
| 30.2 | Boss | boss Xên bọ hung (+ bản NV) | 100 Thị trấn Ginder | 100 Thị trấn Ginder | Hạ 2 dạng đầu Xên bọ hung → **Hạ boss Xên bọ hung dạng 1-2** | ~~Xên bọ hung xuất hiện ở Thị trấn Ginder~~ → **Boss Xên bọ hung ở Thị trấn Ginder, hồi sinh 15–30 phút** |  |
| 30.3 | Boss | boss Xên bọ hung (+ bản NV) | 100 Thị trấn Ginder | 100 Thị trấn Ginder | Hạ Xên hoàn thiện → **Hạ boss Xên hoàn thiện** | Xên đã hoàn thiện, hạ nó đi |  |
| 30.4 | Gặp NPC | NPC Bunma TL 37 | 102 Nhà Bunma | 102 Nhà Bunma | Báo cho Bunma về mẫu 07 | Về Nhà Bunma báo tin |  |
| 31.0 | Gặp NPC | NPC Potage 62 | 140 Hang động Potaufeu | 140 Hang động Potaufeu | Nghe Potage kể sự thật | Potage ở Hang động Potaufeu |  |
| 31.1 | Khác | chọn menu Potage | 140 Hang động Potaufeu | 140 Hang động Potaufeu | Chọn số phận bản sao | Điểm rẽ nhánh: tiêu diệt hay thu nhận bản sao |  |
| 31.2 | Tới map | vào map | 103 Võ đài Xên bọ hung | 103 Võ đài Xên bọ hung | Tới Võ đài Xên bọ hung | Tới Võ đài Xên bọ hung |  |
| 31.3 | Khác | 2 người thật ở 103 | 103 Võ đài Xên bọ hung | 103 Võ đài Xên bọ hung | Rủ 1 người vào võ đài | Võ đài cần một nhân chứng: 1 người chơi khác cùng khu |  |
| 31.4 | Boss | boss bản sao người chơi | 103 Võ đài Xên bọ hung | 103 Võ đài Xên bọ hung | Hạ bản sao của ngươi | Bản sao đang đợi ngươi trên võ đài |  |
| 31.5 | Khác | sức mạnh ≥ 2 tỷ | — | mọi map | Đạt 2 tỷ sức mạnh | Luyện tập đến khi đạt 2 tỷ sức mạnh |  |
| 32.0 | Gặp NPC | NPC Bunma TL 37 | 102 Nhà Bunma | 102 Nhà Bunma | Gặp Bunma ở Nhà Bunma | Bunma có món đồ đưa ngươi, gặp cô ở Nhà Bunma |  |
| 32.1 | Khác | dùng item 992 | — | mọi map | Dùng Nhẫn thời không | Mở hành trang, dùng Nhẫn thời không sai lệch |  |
| 32.2 | Gặp NPC | NPC Bardock 70 | 160 Khu hang động | 160 Khu hang động | Gặp Bardock | Bardock ở Khu hang động |  |
| 32.3 | Đánh quái | mob 80 Cabira, 81 Tobi; **chỉ map 160/161/162 → mọi map** | 160 Khu hang động | 160 Khu hang động, 161 Bìa rừng nguyên thủy, 162 Rừng nguyên thủy, 163 Làng Plant nguyên thủy | Hạ 40 Cabira hoặc Tobi | ~~Cabira, Tobi ở Khu hang động, Bìa rừng và Rừng nguyên thủy~~ → **Cabira, Tobi ở Khu hang động, Bìa rừng, Rừng nguyên thủy, Làng Plant nguyên thủy** |  |
| 32.4 | Nhặt đồ | item 2025: mob 81 Tobi (mọi map) | 161 Bìa rừng nguyên thủy | 160 Khu hang động, 161 Bìa rừng nguyên thủy, 162 Rừng nguyên thủy, 163 Làng Plant nguyên thủy | Nhặt 3 Mảnh Ký Ức Vỡ | Mảnh vỡ chỉ rơi khi hạ Tobi |  |
| 32.5 | Gặp NPC | NPC Bardock 70 | 160 Khu hang động | 160 Khu hang động | Báo cáo với Bardock | Về Khu hang động gặp Bardock |  |
| 33.0 | Tới map | vào map | TĐ: 42 Vách núi Aru / NM: 43 Vách núi Moori / XD: 44 Vách núi Kakarot | TĐ: 42 Vách núi Aru / NM: 43 Vách núi Moori / XD: 44 Vách núi Kakarot | Tới %5 | Quốc Vương đợi ở %5 |  |
| 33.1 | Gặp NPC | NPC Quốc Vương 42 | TĐ: 42 Vách núi Aru / NM: 43 Vách núi Moori / XD: 44 Vách núi Kakarot | TĐ: 42 Vách núi Aru / NM: 43 Vách núi Moori / XD: 44 Vách núi Kakarot | Gặp Quốc Vương | Quốc Vương đứng ở %5 |  |
| 33.2 | Khác | HP gốc chạm trần | — | mọi map | Nâng HP gốc lên 220.000 | Cộng tiềm năng vào HP đến khi HP gốc đạt 220.000 |  |
| 33.3 | Khác | mở giới hạn (menu Quốc Vương) | TĐ: 42 Vách núi Aru / NM: 43 Vách núi Moori / XD: 44 Vách núi Kakarot | TĐ: 42 Vách núi Aru / NM: 43 Vách núi Moori / XD: 44 Vách núi Kakarot | Mở giới hạn sức mạnh | Nhờ Quốc Vương mở giới hạn, lần này miễn phí |  |
| 33.4 | Khác | sức mạnh ≥ 3 tỷ | — | mọi map | Đạt 3 tỷ sức mạnh | Luyện tập đến khi đạt 3 tỷ sức mạnh |  |
| 33.5 | Gặp NPC | NPC Quốc Vương 42 | TĐ: 42 Vách núi Aru / NM: 43 Vách núi Moori / XD: 44 Vách núi Kakarot | TĐ: 42 Vách núi Aru / NM: 43 Vách núi Moori / XD: 44 Vách núi Kakarot | Báo cáo với Quốc Vương | Về %5 báo cáo với Quốc Vương |  |
| 34.0 | Tới map | vào map | 105 Cánh đồng tuyết | 105 Cánh đồng tuyết | Tới Cánh đồng tuyết | Map lạnh trừ 50% HP nếu không có đồ chống lạnh |  |
| 34.1 | Đánh quái | mob 66 Tai tím, 67 Abo; **chỉ map 105/106/107 → mọi map** | 105 Cánh đồng tuyết | 105 Cánh đồng tuyết, 106 Rừng tuyết, 107 Núi tuyết, 108 Dòng sông băng, 158 Bắc thánh địa | Hạ 50 Tai tím hoặc Abo | ~~Tai tím, Abo ở Cánh đồng tuyết, Rừng tuyết, Núi tuyết~~ → **Tai tím, Abo ở Cánh đồng tuyết, Rừng tuyết, Núi tuyết, Dòng sông băng** |  |
| 34.2 | Đánh quái | mob 68 Kado; **chỉ map 108/109 → mọi map** (5 phút) | 108 Dòng sông băng | 108 Dòng sông băng, 109 Rừng băng, 110 Hang băng, 159 Nam thánh Địa | Hạ 20 Kado trong 5 phút | ~~Còn 5 phút! Kado ở Dòng sông băng, hết giờ phải đếm lại~~ → **Còn 5 phút! Kado ở Dòng sông băng, Rừng băng, Hang băng; hết giờ phải đếm lại** |  |
| 34.3 | Nhặt đồ | item 2026: thiết kế: rải sẵn ở 110 — code KHÔNG rải | 110 Hang băng | 110 Hang băng | Nhặt Mảnh Ký Ức Đóng Băng | Mảnh ký ức nằm trong Hang băng |  |
| 34.4 | Boss | boss Cooler (+ bản NV) | 110 Hang băng | 110 Hang băng | Hạ Cooler cả 2 dạng → **Hạ boss Cooler cả 2 dạng** | ~~Cooler canh giữ Hang băng~~ → **Boss Cooler canh giữ Hang băng, hồi sinh 15–30 phút** |  |
| 34.5 | Gặp NPC | NPC Bardock 70 | 160 Khu hang động | 160 Khu hang động | Báo cáo với Bardock | Về Khu hang động gặp Bardock |  |
| 35.0 | Gặp NPC | NPC Thần Vũ Trụ 20 | 48 Hành tinh Kaio | 48 Hành tinh Kaio | Gặp Thần Vũ Trụ | Thần Vũ Trụ ở Hành tinh Kaio |  |
| 35.1 | Khác | 2 người: cùng bang ở 143, hoặc bất kỳ ở 73/74/76/77/81/82 | 143 Con đường rắn độc → **81 Hang quỷ chim** (npc 20 → -1) | 73 Thung lũng chết, 74 Đồi cây Fide, 76 Núi đá, 77 Rừng đá, 81 Hang quỷ chim, 82 Núi khỉ đen, 143 Con đường rắn độc | Rủ 1 người đi cùng | Vào Con đường rắn độc cùng bạn bang, hoặc đứng cùng 1 người ở Hang quỷ chim |  |
| 35.2 | Đánh quái | mob 24 Quỷ mập, 25 Tambourine, 26 Drum, 33 Quỷ địa ngục, 49 Dơi da xanh, 50 Quỷ chim — NGOẠI LỆ: trong CĐRĐ 141–143 ×2; ngoài chỉ mob 49/50 ở 73/74/76/77/81/82 | 141 Con đường rắn độc → **81 Hang quỷ chim** | 73 Thung lũng chết, 74 Đồi cây Fide, 76 Núi đá, 77 Rừng đá, 81 Hang quỷ chim, 82 Núi khỉ đen, 141 Con đường rắn độc, 142 Con đường rắn độc … (+2) | Tích 120 điểm diệt quái | ~~Trong phó bản mỗi quái 2 điểm; ngoài phó bản (map 73-82) mỗi quái 1 điểm~~ → **Phó bản: mỗi quái 2 điểm. Ngoài: chỉ Dơi da xanh, Quỷ chim ở map 73/74/76/77/81/82, 1 điểm** |  |
| 35.3 | Đánh quái | mob 49 Dơi da xanh, 50 Quỷ chim — NGOẠI LỆ: hoặc qua phó bản CĐRĐ | 144 Hoang mạc → **81 Hang quỷ chim** | 73 Thung lũng chết, 74 Đồi cây Fide, 76 Núi đá, 77 Rừng đá, 81 Hang quỷ chim, 82 Núi khỉ đen, 141 Con đường rắn độc, 142 Con đường rắn độc … (+2) | Qua rắn độc hoặc hạ 200 quái | Không có bang? Hạ 200 Dơi da xanh hoặc Quỷ chim ở map 73/74/76/77/81/82 |  |
| 35.4 | Gặp NPC | NPC Thượng Đế 19 | 45 Thần điện | 45 Thần điện | Gặp Thượng Đế ở Thần điện | Thượng Đế đợi ở Thần điện |  |
| 36.0 | Gặp NPC | NPC Ôsin 44 | 52 Đại hội võ thuật | 52 Đại hội võ thuật | Gặp Ôsin ở Đại hội võ thuật | Ôsin ở Đại hội võ thuật |  |
| 36.1 | Tới map | vào map | 114 Cổng phi thuyền | 114 Cổng phi thuyền, 165 Sa mạc hoang vu | Vào Cổng phi thuyền | Cổng mở 12h-12h59. Sai giờ thì nhờ Ôsin đưa qua Sa mạc hoang vu |  |
| 36.2 | Boss | boss Drabura hoặc mob 95 (Cadic M…) ở 165 — NGOẠI LỆ: mob 95 chỉ tính ở 165 | 114 Cổng phi thuyền | 114 Cổng phi thuyền, 165 Sa mạc hoang vu | Hạ Drabura hoặc 20 Cadic M → **Hạ boss Drabura/20 Cadic M** | ~~Hạ Drabura trong phi thuyền, hoặc 20 Cadic M ở Sa mạc hoang vu~~ → **Boss Drabura trong phi thuyền (12h-12h59), hoặc 20 Cadic M ở Sa mạc hoang vu** |  |
| 36.3 | Khác | vào map 117 hoặc nhặt item 2027 (không có nguồn) | 117 Cửa Ải 1 | 117 Cửa Ải 1 | Xuống Cửa Ải 1 | Xuống Cửa Ải 1, hoặc nhặt đồ rơi từ Cadic M ở Sa mạc hoang vu |  |
| 36.4 | Gặp NPC | NPC Babiđây 46 ở 117 / Ôsin 44 ở 165 | 117 Cửa Ải 1 | 117 Cửa Ải 1, 165 Sa mạc hoang vu | Nói chuyện với Babiđây | Babiđây ở Cửa Ải 1; đi đường vòng thì gặp Ôsin ở Sa mạc hoang vu |  |
| 37.0 | Gặp NPC | NPC Ôsin 44 | 52 Đại hội võ thuật | 52 Đại hội võ thuật | Gặp Ôsin ở Đại hội võ thuật | Ôsin ở Đại hội võ thuật |  |
| 37.1 | Boss | boss Mabư 12h / Mabư NV dạng 5 hoặc mob 70 (Hirudegarn…) ở 126 | 120 Phòng chỉ huy → **126 Thành phố Santa** | 120 Phòng chỉ huy, 126 Thành phố Santa, 127 Cổng phi thuyền | Hạ Mabư → **Hạ boss Mabư** | ~~Hạ Mabư trong phi thuyền, hoặc Hirudegarn ở Thành phố Santa~~ → **Boss Mabư trong phi thuyền, hoặc hạ quái Hirudegarn ở Thành phố Santa (24/7)** |  |
| 37.2 | Boss | boss Drabura 3 / Super Bư hoặc mob 50 (Quỷ chim…) **chỉ map 126 → mọi map** | 120 Phòng chỉ huy → **126 Thành phố Santa** | 76 Núi đá, 77 Rừng đá, 81 Hang quỷ chim, 82 Núi khỉ đen, 85 Hành tinh M-2, 86 Hành tinh Polaris, 87 Hành tinh Cretaceous, 88 Hành tinh Monmaasu … (+7) | Hạ Drabura 3 / 30 Quỷ chim | ~~Hạ Drabura 3, hoặc 30 Quỷ chim ở Thành phố Santa~~ → **Hạ boss Drabura 3, hoặc 30 Quỷ chim (Thành phố Santa, Hang quỷ chim, Núi đá...)** | LỆCH |
| 37.3 | Nhặt đồ | item 2028: KHÔNG có nguồn rơi | 120 Phòng chỉ huy → **126 Thành phố Santa** | — | Nhặt Lõi Phép Babiđây | Lõi Phép rơi cho người kết liễu ở bước trước | THIẾU NGUỒN ĐỒ |
| 37.4 | Gặp NPC | NPC Kibit 45 | 50 Thánh địa Kaio | 50 Thánh địa Kaio | Mang Lõi Phép cho Kibit | Kibit ở Thánh địa Kaio |  |
| 38.0 | Gặp NPC | NPC Bunma TL 37 | 102 Nhà Bunma | 102 Nhà Bunma | Gặp Bunma ở Nhà Bunma | Bunma đợi ở Nhà Bunma |  |
| 38.1 | Đánh quái | mob 62 Xên con cấp  5, 63 Xên con cấp  6, 64 Xên con cấp  7, 65 Xên con cấp  8; **chỉ map 97/98/99/100 → mọi map** | 97 Thành phố phía bắc | 97 Thành phố phía bắc, 98 Ngọn núi phía bắc, 99 Thung lũng phía bắc, 100 Thị trấn Ginder | Hạ 60 Xên con phía bắc | Xên con cấp 5-8 ở vùng phía bắc và Thị trấn Ginder |  |
| 38.2 | Boss | boss Black Goku (+ bản NV) | 92 Thành phố phía đông | 92 Thành phố phía đông, 93 Thành phố phía nam, 94 Đảo Balê, 96 Cao nguyên, 97 Thành phố phía bắc, 98 Ngọn núi phía bắc, 99 Thung lũng phía bắc, 100 Thị trấn Ginder … (+1) | Hạ Black Goku → **Hạ boss Black Goku** | ~~Black Goku xuất hiện ở Thành phố phía đông~~ → **Boss Black Goku ở Nhà Bunma và các map Tương lai 92-100; hồi sinh 15–30 phút** |  |
| 38.3 | Boss | boss Black Goku (+ bản NV) | 92 Thành phố phía đông | 92 Thành phố phía đông, 93 Thành phố phía nam, 94 Đảo Balê, 96 Cao nguyên, 97 Thành phố phía bắc, 98 Ngọn núi phía bắc, 99 Thung lũng phía bắc, 100 Thị trấn Ginder … (+1) | Hạ Super Black Goku → **Hạ boss Super Black Goku** | Black Goku đã biến hình, hạ hắn đi |  |
| 38.4 | Nhặt đồ | item 992: Black Goku bản thế giới (ngẫu nhiên 1/7 món); bản NV không rơi | 92 Thành phố phía đông | 92 Thành phố phía đông, 93 Thành phố phía nam, 94 Đảo Balê, 96 Cao nguyên, 97 Thành phố phía bắc, 98 Ngọn núi phía bắc, 99 Thung lũng phía bắc, 100 Thị trấn Ginder … (+1) | Nhặt Nhẫn thời không | ~~Nhẫn thời không sai lệch rơi khi hạ Black Goku~~ → **Nhẫn thời không sai lệch rơi ngẫu nhiên khi hạ boss Black Goku** |  |
| 38.5 | Gặp NPC | NPC Bunma TL 37 | 102 Nhà Bunma | 102 Nhà Bunma | Báo cáo với Bunma | Về Nhà Bunma báo cáo |  |
| 39.0 | Gặp NPC | NPC Bardock 70 | 160 Khu hang động | 160 Khu hang động | Gặp Bardock | Bardock ở Khu hang động |  |
| 39.1 | Khác | đủ 7 viên ngọc 14–20 | — | mọi map | Gom đủ 7 viên Ngọc Rồng | Đủ 7 viên từ 1 đến 7 sao trong hành trang |  |
| 39.2 | Khác | ước Rồng Thần 1 sao ở làng | TĐ: 0 Làng Aru / NM: 7 Làng Mori / XD: 14 Làng Kakarot (npc 5 → -1) | TĐ: 0 Làng Aru / NM: 7 Làng Mori / XD: 14 Làng Kakarot | Gọi Rồng Thần và ước | Gọi Rồng Thần ở làng quê nhà rồi ước một điều |  |
| 39.3 | Gặp NPC | NPC Rồng Omega 29 | TĐ: 24 Trạm tàu vũ trụ / NM: 25 Trạm tàu vũ trụ / XD: 26 Trạm tàu vũ trụ | TĐ: 24 Trạm tàu vũ trụ / NM: 25 Trạm tàu vũ trụ / XD: 26 Trạm tàu vũ trụ | Hỏi Rồng Omega về sao đen | Rồng Omega ở Trạm tàu vũ trụ |  |
| 39.4 | Đánh quái | mob 57 Khỉ lông vàng; **chỉ map 80 → mọi map** | 80 Núi khỉ vàng | 80 Núi khỉ vàng, 85 Hành tinh M-2, 87 Hành tinh Cretaceous, 89 Hành tinh Rudeeze, 90 Hành tinh Gelbo, 91 Hành tinh Tigere, 122 Ngũ Hành Sơn, 124 Ngũ Hành Sơn … (+1) | Hạ 40 Khỉ lông vàng | Khỉ lông vàng ở Núi khỉ vàng |  |
| 39.5 | Gặp NPC | NPC Bardock 70 | 14 Làng Kakarot | 14 Làng Kakarot | Gặp Bardock ở Làng Kakarot | Bardock đợi ngươi ở Làng Kakarot |  |
| 39.6 | Boss | boss Baby (+ bản NV) | 14 Làng Kakarot | 14 Làng Kakarot | Hạ Baby cả 3 dạng → **Hạ boss Baby cả 3 dạng** | ~~Baby xuất hiện ở Làng Kakarot~~ → **Boss Baby xuất hiện ở Làng Kakarot, hồi sinh 15–30 phút** |  |
| 40.0 | Gặp NPC | NPC Thần Vũ Trụ 20 | 48 Hành tinh Kaio | 48 Hành tinh Kaio | Gặp Thần Vũ Trụ | Thần Vũ Trụ ở Hành tinh Kaio |  |
| 40.1 | Tới map | vào map | 50 Thánh địa Kaio | 50 Thánh địa Kaio | Lên Thánh địa Kaio | Lên Thánh địa Kaio |  |
| 40.2 | Gặp NPC | NPC Tổ Sư Kaio 43 | 50 Thánh địa Kaio | 50 Thánh địa Kaio | Gặp Tổ Sư Kaio | Tổ Sư Kaio ở Thánh địa Kaio |  |
| 40.3 | Khác | dùng item 2002 ở 50 | 50 Thánh địa Kaio | 50 Thánh địa Kaio | Dùng Mảnh Ký Ức 1 | Dùng Mảnh Ký Ức 1 ở Thánh địa Kaio. Mảnh vẫn giữ lại |  |
| 40.4 | Gặp NPC | NPC Kibit 45 | 50 Thánh địa Kaio | 50 Thánh địa Kaio | Nghe Kibit kể tiếp | Kibit ở Thánh địa Kaio |  |
| 41.0 | Gặp NPC | NPC Tổ Sư Kaio 43 | 50 Thánh địa Kaio | 50 Thánh địa Kaio | Nghe Tổ Sư Kaio dặn | Tổ Sư Kaio ở Thánh địa Kaio |  |
| 41.1 | Đánh quái | mob 16 Heo rừng, 22 Bulon, 24 Quỷ mập, 32 Quỷ đầu to, 33 Quỷ địa ngục — NGOẠI LỆ VÙNG: cũ chỉ 5 loài (16/22/24/32/33) → mới MỌI quái ở map 27–38 | 27 Rừng Bamboo → **TĐ: 27 Rừng Bamboo / NM: 31 Núi hoa vàng / XD: 35 Rừng cọ** | 27 Rừng Bamboo, 28 Rừng dương xỉ, 29 Nam Kamê, 30 Đảo Bulông, 31 Núi hoa vàng, 32 Núi hoa tím, 33 Nam Guru, 34 Đông Nam Guru … (+4) | Hạ 60 quái vành đai rừng | ~~Hạ quái ở vùng map 27-38, bắt đầu từ Rừng Bamboo~~ → **Mọi quái ở vùng rừng quanh %16 (map 27-38, cả 3 hành tinh) đều tính** |  |
| 41.2 | Boss | boss Broly | 27 Rừng Bamboo → **TĐ: 27 Rừng Bamboo / NM: 31 Núi hoa vàng / XD: 35 Rừng cọ** | 5 Đảo Kamê, 13 Đảo Guru, 20 Vách núi đen, 27 Rừng Bamboo, 28 Rừng dương xỉ, 29 Nam Kamê, 30 Đảo Bulông, 31 Núi hoa vàng … (+7) | Hạ Broly → **Hạ boss Broly** | ~~Broly đang nổi điên ở Rừng Bamboo~~ → **Boss Broly lang thang ở %11 và vùng rừng map 27-38 (khu 2 trở lên)** |  |
| 41.3 | Boss | boss Super Broly | 27 Rừng Bamboo → **TĐ: 27 Rừng Bamboo / NM: 31 Núi hoa vàng / XD: 35 Rừng cọ** | 27 Rừng Bamboo, 28 Rừng dương xỉ, 29 Nam Kamê, 30 Đảo Bulông, 31 Núi hoa vàng, 32 Núi hoa tím, 33 Nam Guru, 34 Đông Nam Guru … (+4) | Hạ Super Broly → **Hạ boss Super Broly** | ~~Super Broly xuất hiện ngay tại chỗ~~ → **Super Broly hiện ra ngay tại chỗ Broly biến mất (vùng rừng map 27-38)** |  |
| 41.4 | Gặp NPC | NPC Tổ Sư Kaio 43 | 50 Thánh địa Kaio | 50 Thánh địa Kaio | Báo lại với Tổ Sư Kaio | Về Thánh địa Kaio báo lại |  |
| 42.0 | Gặp NPC | NPC Ôsin 44 | 50 Thánh địa Kaio | 50 Thánh địa Kaio | Hỏi Ôsin đường tới ngục tù | Ôsin ở Thánh địa Kaio |  |
| 42.1 | Tới map | vào map | 155 Hành tinh ngục tù | 155 Hành tinh ngục tù | Tới Hành tinh ngục tù | Nhờ Ôsin đưa tới Hành tinh ngục tù |  |
| 42.2 | Đánh quái | mob 78 Khỉ lông xanh, 79 Taburine Đỏ; **chỉ map 155 → mọi map** (10 phút) | 155 Hành tinh ngục tù | 155 Hành tinh ngục tù | Phá 60 lồng giam, 10 phút | Còn 10 phút! Hạ Khỉ lông xanh, Taburine Đỏ; hết giờ phải đếm lại |  |
| 42.3 | Boss | boss Cumber (+ bản NV) | 155 Hành tinh ngục tù | 155 Hành tinh ngục tù | Hạ Cumber → **Hạ boss Cumber** | ~~Cumber ở Hành tinh ngục tù~~ → **Boss Cumber ở Hành tinh ngục tù, hồi sinh 15–30 phút** |  |
| 42.4 | Boss | boss Cumber (+ bản NV) | 155 Hành tinh ngục tù | 155 Hành tinh ngục tù | Hạ Super Cumber → **Hạ boss Super Cumber** | Cumber đã biến hình, hạ hắn đi |  |
| 42.5 | Gặp NPC | NPC Ôsin 44 | 155 Hành tinh ngục tù | 155 Hành tinh ngục tù | Báo cáo với Ôsin | Ôsin đợi ở Hành tinh ngục tù |  |
| 43.0 | Gặp NPC | NPC Mr Popo 67 | 0 Làng Aru | 0 Làng Aru | Gặp Mr Popo ở Làng Aru | Mr Popo ở Làng Aru |  |
| 43.1 | Đánh quái | mob 73 Kawazu, 74 Kinkarn, 75 Arbee, 76 Cỗ máy hủy diệt, 78 Khỉ lông xanh, 79 Taburine Đỏ, 80 Cabira, 81 Tobi — NGOẠI LỆ: phó bản Khí gas ×2; ngoài chỉ 155/160/161 | 147 Sa mạc → **155 Hành tinh ngục tù** | 147 Sa mạc, 148 Lâu đài Lychee, 149 Thành phố Santa, 151 Hành tinh bóng tối, 152 Vùng đất băng giá, 155 Hành tinh ngục tù, 160 Khu hang động, 161 Bìa rừng nguyên thủy | Tích 160 điểm diệt quái | ~~Trong phó bản mỗi quái 2 điểm; ngoài phó bản (map 155/160/161) mỗi quái 1~~ → **Phó bản: mỗi quái 2 điểm. Ngoài: chỉ quái ở Hành tinh ngục tù, Khu hang động, Bìa rừng** |  |
| 43.2 | Boss | boss Dr Lychee hoặc mob 78/79/80/81 (Khỉ lông xanh…) ở 155/160/161 — NGOẠI LỆ đường vòng | 148 Lâu đài Lychee → **155 Hành tinh ngục tù** | 148 Lâu đài Lychee, 155 Hành tinh ngục tù, 160 Khu hang động, 161 Bìa rừng nguyên thủy | Hạ Dr Lychee hoặc 50 quái → **Hạ boss Lychee hoặc 50 quái** | Không có bang? Hạ 50 quái ở Hành tinh ngục tù hoặc Khu hang động, Bìa rừng |  |
| 43.3 | Boss | boss Hatchiyack hoặc mob 78/79/80/81 (Khỉ lông xanh…) ở 155/160/161 — NGOẠI LỆ đường vòng | 148 Lâu đài Lychee → **155 Hành tinh ngục tù** | 148 Lâu đài Lychee, 155 Hành tinh ngục tù, 160 Khu hang động, 161 Bìa rừng nguyên thủy | Hạ Hatchiyack hoặc 70 quái → **Hạ boss Hatchiyack/70 quái** | Không có bang? Hạ 70 quái ở Hành tinh ngục tù hoặc Khu hang động, Bìa rừng |  |
| 43.4 | Đánh quái | mob 78 Khỉ lông xanh, 79 Taburine Đỏ, 80 Cabira, 81 Tobi — NGOẠI LỆ: hoặc qua phó bản Khí gas | 147 Sa mạc → **155 Hành tinh ngục tù** | 147 Sa mạc, 148 Lâu đài Lychee, 149 Thành phố Santa, 150 ?, 151 Hành tinh bóng tối, 152 Vùng đất băng giá, 155 Hành tinh ngục tù, 160 Khu hang động … (+1) | Xong khí gas hoặc 100 quái | Không có bang? Hạ 100 quái ở Hành tinh ngục tù hoặc Khu hang động, Bìa rừng |  |
| 43.5 | Gặp NPC | NPC Thượng Đế 19 | 45 Thần điện | 45 Thần điện | Báo cáo với Thượng Đế | Thượng Đế đợi ở Thần điện |  |
| 44.0 | Gặp NPC | NPC Ôsin 44 | 50 Thánh địa Kaio | 50 Thánh địa Kaio | Nhờ Ôsin tới hành tinh Bill | Ôsin ở Thánh địa Kaio |  |
| 44.1 | Gặp NPC | NPC Bill 55 | 154 Hành tinh Bill | 154 Hành tinh Bill | Nói chuyện với Bill | Bill ở Hành tinh Bill |  |
| 44.2 | Khác | thắng 1 trận (thách đấu / võ đài / ĐHVT) | 112 Võ đài Hạt Mít | mọi map | Thắng 1 trận đấu | Thắng một trận thách đấu, võ đài hoặc Đại hội võ thuật |  |
| 44.3 | Boss | boss Whis (thách đấu NPC 56) | 154 Hành tinh Bill | 154 Hành tinh Bill | Đánh bại Whis | Thách đấu Whis ở Hành tinh Bill |  |
| 44.4 | Khác | mở giới hạn (Tổ Sư Kaio) | 50 Thánh địa Kaio | 50 Thánh địa Kaio | Mở giới hạn lần hai | Nhờ Tổ Sư Kaio mở giới hạn, lần này miễn phí |  |
| 44.5 | Gặp NPC | NPC Whis 56 | 154 Hành tinh Bill | 154 Hành tinh Bill | Nghe Whis dặn dò | Whis ở Hành tinh Bill |  |
| 45.0 | Tới map | vào map | 78 Lãnh địa Fize | 78 Lãnh địa Fize | Tới Lãnh địa Fize | Tới Lãnh địa Fize |  |
| 45.1 | Nhặt đồ | item 2008: thiết kế: rải sẵn ở 78 — code KHÔNG rải | 78 Lãnh địa Fize | 78 Lãnh địa Fize | Nhặt Mảnh Ký Ức thứ bảy | Mảnh ký ức nằm ở Lãnh địa Fize |  |
| 45.2 | Khác | 2 người thật ở 78 | 78 Lãnh địa Fize | 78 Lãnh địa Fize | Rủ 1 người làm lễ hợp nhất | Cần thêm 1 người chơi khác cùng khu |  |
| 45.3 | Khác | dùng item 2024 | 78 Lãnh địa Fize | mọi map | Hợp nhất 7 mảnh | Dùng Lõi Ký Ức chưa hoàn chỉnh khi đủ 7 Mảnh Ký Ức |  |
| 45.4 | Gặp NPC | NPC Thiên Sứ Whis 64 | 78 Lãnh địa Fize | 78 Lãnh địa Fize | Nghe Thiên Sứ Whis | Thiên Sứ Whis ở Lãnh địa Fize |  |
| 46.0 | Gặp NPC | NPC Dr. Myuu 83 | 166 Phòng thí nghiệm Myuu | 166 Phòng thí nghiệm Myuu | Gặp Dr. Myuu | Dr. Myuu ở Phòng thí nghiệm Myuu |  |
| 46.1 | Boss | boss Heart dạng 1 | 166 Phòng thí nghiệm Myuu | 166 Phòng thí nghiệm Myuu | Hạ Heart → **Hạ boss Heart** | ~~Heart ở Phòng thí nghiệm Myuu~~ → **Boss Heart ở Phòng thí nghiệm Myuu, hồi sinh 15–30 phút** |  |
| 46.2 | Tới map | vào map | 145 Võ Đài Siêu Cấp | 145 Võ Đài Siêu Cấp | Đuổi tới Võ Đài Siêu Cấp | Heart bỏ chạy tới Võ Đài Siêu Cấp |  |
| 46.3 | Boss | boss Heart dạng 2/3 | 145 Võ Đài Siêu Cấp | 145 Võ Đài Siêu Cấp | Hạ Heart Hư Không → **Hạ boss Heart Hư Không** | Heart biến hình ở Võ Đài Siêu Cấp |  |
| 46.4 | Boss | boss Heart dạng 2/3 | 145 Võ Đài Siêu Cấp | 145 Võ Đài Siêu Cấp | Hạ Heart Toàn Ký → **Hạ boss Heart Toàn Ký** | Dạng cuối của Heart, hạ hắn đi |  |
| 46.5 | Gặp NPC | NPC Thiên Sứ Whis 64 | 145 Võ Đài Siêu Cấp | 145 Võ Đài Siêu Cấp | Gặp Thiên Sứ Whis | Thiên Sứ Whis ở Võ Đài Siêu Cấp |  |
| 47.0 | Khác | chọn menu Whis | 145 Võ Đài Siêu Cấp | 145 Võ Đài Siêu Cấp | Chọn số phận của Lõi | Điểm rẽ nhánh: trả ký ức cho vũ trụ hay giữ Lõi Hư Không |  |
| 47.1 | Khác | dùng item 2000 ở 145 | 145 Võ Đài Siêu Cấp | 145 Võ Đài Siêu Cấp | Trả Lõi Hư Không | Dùng Lõi Hư Không ở Võ Đài Siêu Cấp |  |
| 47.2 | Tới map | vào map | 50 Thánh địa Kaio | 50 Thánh địa Kaio | Lên Thánh địa Kaio | Lên Thánh địa Kaio |  |
| 47.3 | Khác | mở giới hạn (Tổ Sư Kaio) | 50 Thánh địa Kaio | 50 Thánh địa Kaio | Mở giới hạn lần cuối | Nhờ Tổ Sư Kaio mở giới hạn, lần này miễn phí |  |
| 47.4 | Gặp NPC | NPC Tổ Sư Kaio 43 | 50 Thánh địa Kaio | 50 Thánh địa Kaio | Nghe lời cuối Tổ Sư Kaio | Tổ Sư Kaio ở Thánh địa Kaio |  |
| 47.5 | Boss | boss Hư Không Vô Danh (ngoài map 155) | 145 Võ Đài Siêu Cấp | 145 Võ Đài Siêu Cấp | Hạ Hư Không Vô Danh → **Hạ boss Hư Không Vô Danh** | Hư Không Vô Danh ở Võ Đài Siêu Cấp |  |
| 48.0 | Gặp NPC | NPC Berry 71 | 160 Khu hang động | 160 Khu hang động | Gặp Berry ở Khu hang động | Berry đứng ở Khu hang động |  |
| 48.1 | Khác | chọn menu Berry | 160 Khu hang động | 160 Khu hang động | Chọn: Granola hay Jaco | Điểm rẽ nhánh: chọn đi theo Granola hoặc báo Jaco |  |
| 48.2 | Gặp NPC | NPC Jaco 63 | 24 Trạm tàu vũ trụ | 24 Trạm tàu vũ trụ | Gặp Jaco ở Trạm tàu vũ trụ | Jaco ở Trạm tàu vũ trụ Trái Đất |  |
| 48.3 | Boss | boss Kuku, Mập Đầu Đinh, Rambo | — → **68 Thung lũng Nappa** | 63 Trại lính Fide, 64 Núi dây leo, 65 Núi cây quỷ, 66 Trại qủy già, 67 Vực chết, 68 Thung lũng Nappa, 69 Vực cấm, 70 Núi Appule … (+6) | Hạ 3 tên bị truy nã → **Hạ 3 boss bị truy nã** | ~~Truy nã Kuku, Mập Đầu Đinh và Rambo~~ → **Boss Kuku ở Thung lũng Nappa, Mập Đầu Đinh ở Trại lính Fide, Rambo ở Đồi cây Fide** | không mũi tên |
| 48.4 | Nhặt đồ | item 2017: KHÔNG có nguồn rơi | — → **68 Thung lũng Nappa** | — | Nhặt 3 Biên bản truy nã | Biên bản rơi khi hạ Kuku, Mập Đầu Đinh, Rambo | THIẾU NGUỒN ĐỒ |
| 48.5 | Gặp NPC | NPC Jaco 63 | 24 Trạm tàu vũ trụ | 24 Trạm tàu vũ trụ | Nộp biên bản cho Jaco | Về Trạm tàu vũ trụ Trái Đất gặp Jaco |  |
| 49.0 | Gặp NPC | NPC Potage 62 | 140 Hang động Potaufeu | 140 Hang động Potaufeu | Nghe Potage kể sự thật | Potage ở Hang động Potaufeu |  |
| 49.1 | Khác | chọn menu Potage | 140 Hang động Potaufeu | 140 Hang động Potaufeu | Chọn số phận bản sao | Điểm rẽ nhánh: tiêu diệt hay thu nhận bản sao |  |
| 49.2 | Tới map | vào map | 103 Võ đài Xên bọ hung | 103 Võ đài Xên bọ hung | Tới Võ đài Xên bọ hung | Tới Võ đài Xên bọ hung |  |
| 49.3 | Khác | 2 người thật ở 103 | 103 Võ đài Xên bọ hung | 103 Võ đài Xên bọ hung | Rủ 1 người vào võ đài | Võ đài cần một nhân chứng: 1 người chơi khác cùng khu |  |
| 49.4 | Boss | boss bản sao người chơi | 103 Võ đài Xên bọ hung | 103 Võ đài Xên bọ hung | Đánh gục bản sao | Đánh gục bản sao, đừng để nó chết |  |
| 49.5 | Khác | dùng item 638 ở 103 | 103 Võ đài Xên bọ hung | 103 Võ đài Xên bọ hung | Dùng Bình chứa Commeson | Dùng Bình chứa Commeson khi bản sao đã gục trên võ đài |  |
| 49.6 | Khác | sức mạnh ≥ 2 tỷ | — | mọi map | Đạt 2 tỷ sức mạnh | Luyện tập đến khi đạt 2 tỷ sức mạnh |  |
| 50.0 | Khác | chọn menu Whis | 145 Võ Đài Siêu Cấp | 145 Võ Đài Siêu Cấp | Chọn số phận của Lõi | Điểm rẽ nhánh: trả ký ức cho vũ trụ hay giữ Lõi Hư Không |  |
| 50.1 | Khác | dùng item 2000 ở 145 | 145 Võ Đài Siêu Cấp | 145 Võ Đài Siêu Cấp | Hấp thụ Lõi Hư Không | Dùng Lõi Hư Không ở Võ Đài Siêu Cấp, Lõi vẫn ở lại với ngươi |  |
| 50.2 | Tới map | vào map | 155 Hành tinh ngục tù | 155 Hành tinh ngục tù | Về Hành tinh ngục tù | Lõi kéo ngươi về Hành tinh ngục tù |  |
| 50.3 | Khác | mở giới hạn (Tổ Sư Kaio) | 50 Thánh địa Kaio | 50 Thánh địa Kaio | Mở giới hạn lần cuối | Nhờ Tổ Sư Kaio ở Thánh địa Kaio mở giới hạn, lần này miễn phí |  |
| 50.4 | Gặp NPC | NPC Ôsin 44 | 155 Hành tinh ngục tù | 155 Hành tinh ngục tù | Nghe lời cuối của Ôsin | Ôsin ở Hành tinh ngục tù |  |
| 50.5 | Boss | boss Hư Không Vô Danh ở map 155 | 155 Hành tinh ngục tù | 155 Hành tinh ngục tù | Hạ Hư Không Vô Danh → **Hạ boss Hư Không Vô Danh** | Hư Không Vô Danh ở Hành tinh ngục tù |  |

### 8.1 Dòng mục tiêu trong `detail` (18 nhiệm vụ)

| NV | Cũ | Mới |
|---|---|---|
| 5 | Tìm kỷ vật rơi từ %9, lau sạch rồi đưa cho %2. | Hạ %9 ở %13 lấy kỷ vật, lau sạch, đưa cho ông. |
| 6 | Hạ %9 trong rừng, diệt Kẻ Thu Gom rồi báo cho %2. | Hạ %9, diệt boss Kẻ Thu Gom ở %15, báo ông. |
| 7 | Hạ 10 quái mẹ trong 3 phút, rồi chạy tới Trạm tàu vũ trụ gặp Jaco. | Hạ 10 quái mẹ ở %15 trong 3 phút, rồi tới trạm tàu. |
| 8 | Gặp Bunma ở Siêu Thị, mua Rada cấp 1, hạ quái mẹ lấy lõi. | Gặp Bunma ở Siêu Thị, mua Rada cấp 1, hạ quái mẹ ở %15. |
| 12 | Nở trứng nhận đệ tử, cùng nó hạ quái mẹ rồi về gặp %2. | Nở trứng nhận đệ tử, cùng nó hạ quái mẹ ở %15 rồi về nhà. |
| 13 | Vào bang, cùng bạn bang hạ quái mẹ rồi gặp Giu-ma Đầu Bò. | Vào bang, cùng bang hạ quái mẹ ở %15, gặp Giu-ma Đầu Bò. |
| 14 | Gặp Bunma, mua một món ở quầy Uron, chặn đoàn heo chở hàng. | Gặp Bunma, mua ở quầy Uron, chặn heo chở hàng ở %16. |
| 15 | Tới điểm hẹn, đánh bại Jaco mất ký ức rồi gặp lại hắn. | Tới %16, hạ boss Jaco mất ký ức rồi gặp lại hắn. |
| 16 | Dọn quái phía Nam, nhặt 5 Vỏ đạn khắc dấu rồi về gặp %10. | Dọn quái %17, %18, nhặt 5 Vỏ đạn, về gặp sư phụ. |
| 19 | Gặp Cui, dọn sạch trại lính, rủ thêm một người hạ Appule. | Gặp Cui ở Thung lũng Nappa, dọn trại lính, rủ bạn hạ Appule. |
| 22 | Hạ trọn Tiểu đội sát thủ, lấy Máy đo ký ức đưa cho Tapion. | Hạ trọn Tiểu đội sát thủ ở Núi khỉ đỏ, lấy máy đo cho Tapion. |
| 24 | Lần theo sóng, dọn Xên con ở phía đông rồi báo Bunma. | Dọn Xên con ở Thành phố phía đông rồi báo Bunma. |
| 25 | Hạ Android 19 và Dr.Kôrê, mang 3 lõi năng lượng về cho Bunma. | Hạ Android 19, Dr.Kôrê ở Cao nguyên, mang 3 lõi về cho Bunma. |
| 28 | Dọn Xên con phía bắc, hạ Poc, Pic, King Kong rồi lấy mảnh giáp. | Dọn Xên con ở Thành phố phía bắc, hạ Poc, Pic, King Kong. |
| 32 | Tìm Bardock, dọn hang động, nhặt 3 Mảnh Ký Ức Vỡ cho ông. | Tìm Bardock ở Khu hang động, dọn quái, nhặt 3 Mảnh Ký Ức Vỡ. |
| 38 | Hạ Black Goku cả hai dạng, nhặt nhẫn về cho Bunma. | Hạ boss Black Goku ở Tương lai, nhặt nhẫn về cho Bunma. |
| 41 | Dọn vành đai rừng, hạ Broly rồi hạ tiếp Super Broly. | Dọn vành đai rừng quanh %16, hạ boss Broly và Super Broly. |
| 42 | Phá 60 lồng giam trong 10 phút, rồi hạ Cumber cả hai dạng. | Phá 60 lồng giam ở Hành tinh ngục tù, rồi hạ boss Cumber. |

## 9. Chỗ nghi ngờ

| # | Mức | Vấn đề | Đề xuất |
|---|---|---|---|
| 1 | **Chặn tuyến** | **6 vật phẩm nhiệm vụ không được rơi ở đâu cả.** `checkDoneTaskPickItem` chờ nhặt 2016 (20.4 Thẻ tiền thưởng), 2017 (48.4 Biên bản truy nã), 2018 (22.3 Máy đo ký ức), 2019 (25.2 Lõi năng lượng), 2021 (28.3 Mảnh giáp khắc tên), 2028 (37.3 Lõi Phép Babiđây). `grep` toàn bộ `SRC/src`: không `Boss.reward`, `QuestBoss.getQuestItemId`, `Mob.dropItemTask` nào tạo các id này (chỉ Heart rơi Ống nghiệm Myuu). Người chơi hạ đủ boss rồi **kẹt vĩnh viễn** ở bước nhặt đồ. | Thêm rơi đồ nhiệm vụ vào `reward()` của Kuku/Mập Đầu Đinh/Rambo, Tiểu đội trưởng, Android 19/Kôrê, King Kong, Drabura 3/Mabư (hoặc đổi bước thành "hạ boss" rồi trao thẳng). Ngoài phạm vi lượt này (chỉ chữ + map), **cần làm gấp**. |
| 2 | **Chặn tuyến** | **3 vật phẩm "rải sẵn trên map" không được rải.** `Zone.getItemMapsForPlayer` có bộ lọc cho 2023 (29.2, map 166), 2026 (34.3, map 110), 2008 (45.1, map 78) nhưng `Map.initItem` chỉ rải item 74/78 và ngọc sao đen — không có 3 món này. | Thêm vào `Map.initItem` (hoặc rơi từ quái map đó). |
| 3 | Cao | 38.4 "Nhặt Nhẫn thời không": 992 chỉ rơi từ **Black Goku bản thế giới** (2 bản, rơi ngẫu nhiên 1 trong 7 món 15–20 / 992). Bản nhiệm vụ `BlackGokuNhiemVu` không rơi gì. Người chơi có thể hạ bản NV nhiều lần mà không bao giờ có nhẫn. | Cho `BlackGokuNhiemVu.getQuestItemId` trả 992 ở dạng 2. |
| 4 | Trung bình | 3.1 (NV 3): item 78 rải ở **42/43/44**, mũi tên `-4` trỏ **39/40/41**. Luật NV 0–3 cấm đổi cột `map`, nên chỉ đổi tên thành "Tìm đồ lạ ở %5" (= Vách núi Aru / Moori / Kakarot, đúng tên map 42/43/44). Với Trái Đất và Namếc, map 39 và 42 **trùng tên** nên người chơi vẫn có thể đứng nhầm ở 39. | Thử client thật: nếu tutorial không phụ thuộc cột `map` của bước này thì đổi `-4` → `-10`. |
| 5 | Trung bình | 22.2: bản "Số 1–4 Namek / Tiểu đội trưởng Namek" (map 73–77, id `*_NM`) **không** được tính — chỉ bản ở 79/81/82/83. Tên hiển thị khác ("Số 4 Namek") nên chữ nhắc đã chỉ đúng 4 map. | Giữ (bản Namek máu thấp hơn 10 lần). |
| 6 | Trung bình | Placeholder mới -11..-14 và %15..%20 cần **jar mới + 07 cùng lúc**. Jar cũ + SQL mới: mũi tên nhận -11 (không có map), chữ hiện nguyên "%15". Jar mới + SQL cũ: không lỗi (chỉ thiếu chữ mới). | Triển khai jar trước hoặc cùng lúc. |
| 7 | Thấp | Bỏ ràng buộc map khiến vài quái ở map phó bản cũng tính: 16.1 (Không tặc ở Tường thành 2/3), 16.2 (Bulon ở Tường thành 3, Quỷ mập ở CĐRĐ 141), 18.2 (Tambourine/Drum ở CĐRĐ 142, Tây Karin 146, Thung lũng Nappa). Người chơi vào được các map đó thì đã qua bước này từ lâu. | Không cần làm gì. |
| 8 | Thấp | 36.x: mũi tên vẫn trỏ 114 / 117 (phi thuyền, chỉ mở 12h–12h59). Đường vòng 24/7 là Sa mạc hoang vu 165, vào **qua NPC Ôsin ở 52**, không đi bộ tới được nên mũi tên trỏ 165 cũng vô ích. Câu nhắc đã nói cả hai đường. | Giữ. |
| 9 | Thấp | 41.2/41.3: Broly là boss của **sự kiện "default"** (`event_list/Default`, 30 bản, map 5/13/20/27–38, chỉ khu ≥ 2). Tắt sự kiện này trong cpanel là NV 41 kẹt. Super Broly sinh ra khi Broly rời map (`Broly.leaveMap`). | Không tắt sự kiện "default". |
| 10 | Thấp | Bước "tới map" 16.0 chỉ tính đúng map phía Nam của **hành tinh mình** (29/33/37 theo gender) — nay mũi tên -13 và tên "Tới %17" đã khớp. Các bước đánh quái thì tính mọi hành tinh, nên người Namếc đánh không tặc ở Nam Kamê vẫn được. | Đúng ý. |

## 10. SQL, kiểm tra, cách chạy

### 10.1 File

* `02-nhiem-vu-moi.sql` — sửa 65 dòng bước + 18 mô tả, thêm chú thích placeholder mới. Cài mới chỉ cần 02 là đủ chữ mới;
  nhưng nếu chạy tiếp 05 → 06 (như thứ tự cũ) thì 06 ghi đè chữ cũ ⇒ **luôn chạy 07 sau cùng**.
* `05-sua-huong-dan-tan-thu.sql` — chỉ đổi tên bước 3.1 cho khớp 02 (server đã chạy 05 không cần chạy lại; 07 lo phần này).
* `07-ro-map-va-boss.sql` — **mới**, cho server đang chạy: 65 `UPDATE task_sub_template … WHERE task_main_id = … AND ducvupro = …`
  (chỉ cột thật sự đổi) + 18 `UPDATE task_main_template … WHERE id = …`, trong `START TRANSACTION … COMMIT`. Không DELETE/INSERT,
  không đụng `max_count`, `ducvupro`, thưởng, `data_task`. Chạy lại bao nhiêu lần cũng vậy. Chỉ dùng `UPDATE`, `CRC32`,
  `CONCAT_WS`, `CHAR_LENGTH`, `IN` với bộ (row constructor) — đều có trong MariaDB 10.4.

Cách chạy trên server:

```
mysqldump -u root -p team2026 task_main_template task_sub_template > backup_truoc_07_$(date +%F).sql
mysql -u root -p team2026 < SRC/sql/patch/07-ro-map-va-boss.sql
# build lại jar (TaskService, ConstTask, BossManager) rồi khởi động lại server
```

Kiểm tra cuối file 07: K1 = 51 / 237 · K2 `dung_sub = 1`, `dung_main = 1` (dấu vân tay CRC32 của 65 bước + 18 mô tả) ·
K3 số bước dùng -11 / -12 / -13 / -14 = 5 / 6 / 2 / 2 · K4 bước đánh boss thiếu chữ "boss" = 0 dòng · K5 tên dài nhất ≤ 28.
Sau 07, câu K2 của file 06 sẽ ra 0 — đúng kỳ vọng vì chữ đã đổi.

### 10.2 Kết quả chạy thử

Máy có MySQL 8.4 (không có MariaDB); file 07 chỉ dùng cú pháp có trong MariaDB 10.4.

| Lần | Chuỗi | Kết quả |
|---|---|---|
| A | `database team2026.sql` → 01 → 02 (mới) → 05 → 06 → 07 → 07 lần 2 | Không lỗi. 07: K1 51/237, K2 1/1, K3 5/6/2/2, K4 0 dòng, K5 28. Chạy lần 2 cùng kết quả. |
| B | `database team2026.sql` → 01 → **02 cũ (trước sửa)** → 05 → 06 → 07 — mô phỏng server đang chạy | Không lỗi. Bảng `task_sub_template` / `task_main_template` **giống hệt** lần A (so MD5 toàn bảng). |
| C | `database team2026.sql` → 01 → 02 (mới) (→ 05) | Giống hệt A — 02 mới tự nó đã đủ chữ mới. |

DB tạm đã xóa. Biên dịch: `javac` JDK 17, **577 file, không lỗi**.

### 10.3 Tự kiểm tra chữ (script)

| Kiểm tra | Kết quả |
|---|---|
| Tên bước ≤ 28 (placeholder dài nhất) | Đạt — dài nhất 28 |
| `max_count > 1` thì tên có đúng con số | Đạt 237/237 |
| `notify` ≤ 100 (placeholder dài nhất) | Đạt — dài nhất 95 |
| Dòng mục tiêu ≤ 70, `detail` ≤ 200 | Đạt |
| Sau khi thay placeholder theo đúng thứ tự Java, không còn `%` sót (trừ "50% HP" có sẵn ở 34.0) | Đạt, 3 hành tinh |
| NV 0–3: `map`, `npc_id`, `max_count`, điều kiện, notify trống | Không đổi (chỉ tên 3.1) |
