# 71 — Luyện đan (patch 84)

Ngày làm: 2026-09-27. Trạng thái: **đã code, đã biên dịch sạch bằng JDK 21, CHƯA chạy thử
trong game.**

Nối tiếp [69](69-map-tu-tien-ban-thiet-ke.md) và [70](70-map-tu-tien-da-lam.md).

---

## 1. Vì sao làm, và vì sao làm kiểu này

Map Tu Tiên có một lỗ: **cày hết trần 500 Linh Thạch/ngày là map chết**, không còn lý do ở lại.
Luyện đan vá đúng lỗ đó bằng một vòng khép kín bắt người chơi đi qua cả hai tính năng đã có:

```
quái map Tu Tiên  ──► linh thảo  (KHÔNG có trần ngày, cho giao dịch)
        boss      ──► Địa Hỏa Tinh + đan phương  (xúc tác bắt buộc)
                          │
                          ▼
                  Lò Luyện Đan (NPC 90, đảo Kamê)
                          │
          ┌───────────────┴───────────────┐
      thành (45–75%)                 nổ lò
   đan thượng phẩm, 20 phút        Đan Phế + cả server cười
```

Ba quyết định đáng ghi lại:

| Quyết định | Lý do |
|---|---|
| Linh thảo rơi từ **quái map Tu Tiên**, không phải boss | Nếu boss rơi hết thì luyện đan thành phụ phẩm của săn boss, map Tu Tiên vẫn chết. |
| Đan thượng phẩm là **vật phẩm riêng**, không phải "phẩm chất" gắn vào đan cũ | `item_option_template` gửi bằng `writeByte` nên tối đa 255 dòng, hiện đã 253. Hai ô cuối để dành việc khẩn cấp. |
| Đan phương là **item tiêu hao**, không phải "học vĩnh viễn" | Học vĩnh viễn thì phải lưu theo từng người ⇒ phải thêm cột vào bảng `player`, mà bảng đó đang sát trần 65.535 byte/dòng của InnoDB (xem patch 78). |

---

## 2. Vật phẩm mới (2276–2289)

| id | Tên | icon | Nguồn |
|---|---|---|---|
| 2276 | Thanh Vân Thảo | 24872 | quái map 208, 8% |
| 2277 | Ngọc Diệp Thảo | 24874 | quái map 208, 8% |
| 2278 | Hàn Tinh Quả | 24891 | quái map 208, 5% |
| 2279 | Kim Nhung Quả | 24892 | quái map 208, 3% |
| 2280 | Địa Hỏa Tinh | 24881 | **chỉ boss** |
| 2281 | Đan Phương Sơ Cấp | 30192 | Bát Giới 12% · Ngộ Không Giả 20% |
| 2282 | Đan Phương Trung Cấp | 30194 | Ngộ Không Giả 8% · Siêu Thần God 15% |
| 2283 | Đan Phương Cao Cấp | 30196 | Siêu Thần God 5% |
| 2284 | Đan Phế | 24887 | nổ lò |
| 2285 | Luyện Khí Đan Thượng Phẩm | 24896 | lò — sức đánh **+30%**, 20 phút |
| 2286 | Hộ Thể Đan Thượng Phẩm | 24890 | lò — HP **+45%**, 20 phút |
| 2287 | Tụ Khí Đan Thượng Phẩm | 24889 | lò — KI **+45%**, 20 phút |
| 2288 | Kim Cương Đan Thượng Phẩm | 24886 | lò — chịu đòn **−60%**, 20 phút |
| 2289 | Phá Quân Đan Thượng Phẩm | 24882 | lò — chí mạng **+15%**, 20 phút |

Bốn linh thảo **cho giao dịch** (chủ ý — để tự mọc ra chợ giữa người chơi). Đan cũng cho giao
dịch, y như đan tiệm.

14 icon kéo từ SUMO về `SRC/data/icon/x1..x4`. Mức x1 của bộ 24xxx trong SUMO không có nên
được dựng lại bằng cách thu nhỏ x2 đúng một nửa — giống cách patch 83 đã làm.

**Ngân sách gói tin:** đo thật trên CSDL đã chạy hết patch: **127.932 byte / 130.000**
(trần 2 gói × 65.000, xem `data/ItemData.MAX_PACKET_BYTES`). 14 món này nặng 948 byte, còn trống
**2.068 byte** ≈ 20 món nữa. Mô tả vật phẩm cố ý viết ngắn vì lý do này. Khối (3) của patch 84
in ra con số đó mỗi lần chạy.

---

## 3. Năm công thức

Tỉ lệ càng cao thì nổ càng nhiều — đây là chỗ hồi hộp, và cũng là lý do đan phương có giá trị.

| Ra | Đan phương | Thành | Nguyên liệu |
|---|---|---|---|
| Luyện Khí Đan TP | Sơ Cấp | **75%** | 5 Thanh Vân Thảo + 3 Kim Nhung Quả + 1 Địa Hỏa Tinh |
| Hộ Thể Đan TP | Sơ Cấp | **75%** | 5 Ngọc Diệp Thảo + 3 Hàn Tinh Quả + 1 Địa Hỏa Tinh |
| Tụ Khí Đan TP | Trung Cấp | **60%** | 6 Hàn Tinh Quả + 4 Thanh Vân Thảo + 2 Địa Hỏa Tinh |
| Phá Quân Đan TP | Trung Cấp | **60%** | 6 Kim Nhung Quả + 4 Ngọc Diệp Thảo + 2 Địa Hỏa Tinh |
| Kim Cương Đan TP | Cao Cấp | **45%** | 5 mỗi loại linh thảo + 3 Địa Hỏa Tinh |

Thứ tự trong `LuyenDan.CONG_THUC` **chính là số hiệu nút menu**, nên thêm công thức mới thì
thêm vào cuối, đừng đảo.

### Nổ lò cố ý KHÔNG phạt gì ngoài nguyên liệu

Không trừ máu, không đá về nhà, không debuff. Nổ lò phải vui chứ không được cắt ngang việc
người chơi đang làm. Đổi lại: một cục Đan Phế (ăn vào chỉ ra một câu nhảm) và một dòng thông
báo toàn server, cộng bảng vàng **"Vua Nổ Lò"**.

Bảng vàng đó ghi ra `data/no_lo.json`, **không thêm cột nào vào bảng `player`** — đúng cách
`boss/drop/BangRoiBoss` đang làm. Ghi theo nhịp 1 phút, và chốt một bản lúc bảo trì
(`ServerManager.close`). Mất file thì chỉ mất bảng vàng, nhân vật không sao.

---

## 4. Hai boss mới — để mem yếu cũng có đường

Trước patch này chỉ bộ Siêu Thần God (500 triệu máu, 50.000 sát thương) rơi đồ quý ⇒ người chơi
yếu không có cửa lấy Địa Hỏa Tinh. Thêm hai hạng dưới:

| Boss | id | Máu | Sát thương | Map | Nhịp | Địa Hỏa Tinh |
|---|---|---|---|---|---|---|
| Trư Bát Giới Ăn Vụng | −2215 | 8.000.000 | 5.000 | 1 / 8 / 15 (map **đầu** mỗi hành tinh) | 5 phút | x1, 40% |
| Tôn Ngộ Không Giả | −2216 | 60.000.000 | 15.000 | 3 / 10 / 17 | 10 phút | x1–2, 60% |
| Siêu Thần God + Lão Dê | có sẵn | 500.000.000 | 50.000 | 4 / 12 / 20 (map **cuối**) | 10 phút | x2–3, 100% |

Ngoại hình lấy từ hai cải trang Tây Du đã có sẵn, chưa con boss nào dùng: "Cải trang Bát Giới"
(part 2268/2269/2270) và "Cải trang Ngộ Không" (part 2271/2272/2273).

Hai con này dùng hàm dựng 2 tham số nên **không** bật `isZone01SpawnDisabled` — cố ý, vì phải
ra ngay khu 0/1 là nơi người chơi mới đứng đông nhất.

### 4b. Luật rơi CHUNG — mọi boss đều rơi Địa Hỏa Tinh

Chỉ thêm hai con boss mới là chưa đủ: người cày chay hạ được con boss nào khác cũng **không
tiến thêm bước nào** trong tuyến luyện đan. Nên `BangRoiBoss.roi` nay cộng thêm một **luật
chung tính theo máu boss**, áp cho tất cả:

| Máu boss | Địa Hỏa Tinh | Ví dụ |
|---|---|---|
| < 300.000 | **không rơi** | Ăn Trộm (100), Mặt Trời (100), Sói hẹc quyn, Kẻ Thu Gom |
| 300.000 – 5 triệu | 15% × 1 | Kuku, Mập Đầu Đinh, Rambo, Dr.Kôrê, Android 13/14, bộ Ma Bư |
| 5 – 50 triệu | 25% × 1 | King Kong, Pic, Poc, Fide đại ca, Jacky Chun, Số 1–4 |
| 50 – 200 triệu | 40% × 1 | Tiểu đội trưởng, Bojack, Siêu Bojack, PôCôLô, Bujin/Kogu/Zangya |
| > 200 triệu | 60% × 1–2 | Cumber, Black Goku, Cooler, Heart, Fide Vàng, Fu |

Thêm **Đan Phương Sơ Cấp 10%** cho mọi boss trên 20 triệu máu.

Năm con số đó là **ô chỉnh trong cpanel** (tab *Rơi đồ boss*), key `ld_*` — để 0 là tắt.

Ba trường hợp KHÔNG áp luật chung:

1. Boss dưới ngưỡng máu (`ld_mau_min`) — chặn cày vô hạn Ăn Trộm / Mặt Trời (100 máu, 20 bản).
2. **Lâu la** đi kèm boss khác (`Boss.laLauLa()`) — không thì một boss kéo 5 lâu la là rơi gấp 6.
3. Con nào đã có dòng Địa Hỏa Tinh **viết tay** trong bảng riêng — dòng viết tay luôn thắng, để
   cpanel chỉnh được từng con. Đó là hai con Tây Du, bộ Siêu Thần God và Lão Dê.

Quan trọng: `roi()` **cộng** bảng riêng với luật chung chứ không phải "có bảng riêng thì thôi
luật chung" — gần như mọi boss cũ đều không có bảng riêng, thoát sớm là chúng chẳng bao giờ rơi.

NPC "Theo Dõi Boss" cũng hiện dòng này (`BangRoiBoss.xem(bossId, mauMax)`), nên người chơi
nhìn vào biết ngay con nào rơi bao nhiêu.

> **Cần để ý:** boss **Ở dơ** chỉ 500.000 máu nhưng có 5 bản và đi khắp ~73 map ⇒ lọt vào bậc
> 15% và rất dễ cày. Nếu thấy thừa Địa Hỏa Tinh thì nâng `ld_mau_min` lên 600.000 ở cpanel là
> con này rụng khỏi danh sách (Kuku 500.000 cũng rụng theo — cân nhắc).

> **Cảnh báo cho người vận hành:** nếu trước đây đã bấm "lưu" ở cpanel tab *Rơi đồ boss* thì
> file `data/bossdrop_table.json` đang tồn tại, và **file ghi đè hẳn bảng mặc định của con nào
> có trong file**. Tức là ba dòng luyện đan mới thêm cho bộ Siêu Thần God sẽ KHÔNG xuất hiện.
> Cách xử lý: hoặc xoá file rồi chỉnh lại từ đầu, hoặc vào cpanel thêm tay ba dòng đó.
> Hai boss Tây Du là id mới nên không bị ảnh hưởng. **Luật rơi chung ở mục 4b cũng không bị
> ảnh hưởng** — nó nằm trong code, không nằm trong file đó.

---

## 5. Đan tiệm và đan thượng phẩm dùng CHUNG một ô hiệu lực

Đây là chỗ dễ sinh lỗi nhất nên ghi rõ.

`ItemTime` trước đây mỗi loại đan có 2 ô (`isUseDanX`, `lastTimeDanX`) và thời hạn / mức cộng
viết cứng (10 phút, +30%…). Nay mỗi loại có **4 ô**: thêm `hanDanX` (thời hạn) và `mucDanX`
(mức cộng %). Ăn viên nào thì viên đó **ghi đè cả bốn ô**, nên:

* ăn viên thượng phẩm đè lên viên tiệm đang chạy ⇒ nâng cấp lên 20 phút / mức cao;
* ăn viên tiệm đè lên viên thượng phẩm ⇒ hạ xuống 10 phút / mức gốc;
* **không bao giờ cộng dồn hai hạng** — đúng như hai viên đan tiệm vẫn xử sự với nhau.

Khi hết hạn, `ItemTime.update` trả `hanDanX` / `mucDanX` về mức gốc, để viên tiệm ăn sau đó
không thừa hưởng nhầm mức của viên thượng phẩm vừa hết.

Đồng thời sửa luôn hai lỗi cũ trong `ItemTimeService.sendAllItemTime`:

* icon đồng hồ đếm ngược của 5 viên đan trước đây **viết cứng và sai** (27641 là cái đầu heo,
  24886 là bình ngọc) — nay tra thẳng `iconID` của vật phẩm tương ứng;
* thời gian đếm ngược trước đây luôn lấy `TIME_ITEM` — viên 20 phút sẽ đếm ra **số âm** sau
  10 phút. Nay lấy `hanDanX` và kẹp tại 0.

Và một lỗi tràn số trong `NPoint`: `hpMax` / `mpMax` / `dame` đều là `int`, mà ba chỗ cộng chỉ số
đan viết `hpMax += hpMax * 30 / 100`. Phép nhân làm bằng `int` nên **tràn thành số âm** khi máu
vượt 71 triệu (với mức 45% của đan thượng phẩm thì tràn ngay từ 47,7 triệu). Nay nhân bằng `long`
rồi kẹp tại `Integer.MAX_VALUE`. Lỗi này có sẵn từ patch 83, không phải do patch 84 sinh ra.

---

## 6. NPC "Lò Luyện Đan" (npc_template 90)

NPC dạng **vật thể**, không phải người — dựng y hệt "Cây thông Noel" (npc 79):

```
part 2658 (đầu):  [[2955,0,0],[33001,0,0],[2955,0,0]]
part 2659 (thân): 17 mảnh 2955 (trong suốt)
part 2660 (chân): 14 mảnh 2955
```

Icon **33001** là ảnh lò, tự dựng từ icon 24895 (bình luyện đan của SUMO) phóng lên đúng khung
36×50 / 73×100 / 109×150 / 146×200 — **trùng khít** khung của icon 15041 (cây thông), nên toạ độ
trong part lấy nguyên và chắc chắn không lệch chân.

Chỗ đứng: **map 5, x = 170, y = 288**. Thềm trên đảo Kamê là cột 0..19 của lưới ô 24 px
(x = 0..479); trên đó đã có NPC 81 (240), 86 (310), 87 (380), 89 (450) cách nhau đúng 70 px,
không còn khe ở giữa. x = 170 là ô trống kế bên trái NPC 81, vẫn nền đặc, cách NPC gần nhất
70 px > bán kính 60 px của `Map.getNpc`. Muốn dời thì sửa khối (2d) của patch 84, không đụng code.

Ba menu: `LO_LUYEN_DAN_MENU` (1110) → `LO_LUYEN_DAN_CHON` (1111) → `LO_LUYEN_DAN_XAC_NHAN` (1112).
Menu xác nhận in đủ nguyên liệu, **số đang có** và tỉ lệ thành trước khi bấm.

---

## 7. File đã đụng

**Mới**

* `src/nro/models/tu_tien/LuyenDan.java` — công thức, luyện, nổ lò, hiệu lực đan thượng phẩm
* `src/nro/models/tu_tien/BangVangNoLo.java` — bảng vàng "Vua Nổ Lò", lưu ra `data/no_lo.json`
* `src/nro/models/npc_list/LoLuyenDan.java` — NPC 90
* `src/nro/models/boss/tay_du/BossTayDu.java` — hai boss Tây Du
* `sql/patch/84-luyen-dan.sql`
* 14 icon + icon 33001 trong `data/icon/x1..x4`

**Mới (patch 85)**

* `src/nro/models/tu_tien/LinhDien.java` — 6 ô ruộng, đọc/ghi cột `tu_tien`
* `sql/patch/85-linh-dien.sql`

**Sửa**

| File | Sửa gì |
|---|---|
| `npc_list/TuTienNPC` | thêm mục "Linh điền" + hai nút gieo/hái |
| `player/Player` | hai mảng 6 ô của Linh Điền |
| `server/Manager` | `HAS_TU_TIEN` + tự thêm cột `tu_tien` |
| `database/MrBlue`, `database/PlayerDAO` | đọc / ghi cột `tu_tien` |
| `item/ItemTime` | thêm `hanDanX` / `mucDanX` cho 5 loại đan, trả về mức gốc khi hết hạn |
| `player/NPoint` | 4 chỗ cộng chỉ số đan đọc `mucDanX` thay vì số viết cứng |
| `player/Player` | Kim Cương Đan giảm theo `mucDanGiap`, kẹp trong [0, 90] |
| `services/ItemTimeService` | icon + thời gian đếm ngược đúng cho cả hai hạng đan |
| `services_func/UseItem` | 6 case mới; 5 case đan tiệm nay đặt lại cả thời hạn và mức |
| `tu_tien/TuTien` | `roiLinhThach` gọi thêm `LuyenDan.themLinhThao` |
| `boss/BossID`, `boss/BossesData` | hai boss mới |
| `boss/Boss_Manager/BossManager` | dựng hai boss lúc nạp |
| `boss/drop/BangRoiBoss` | bảng rơi 2 boss mới, 3 dòng cho bộ Siêu Thần God, **luật rơi chung theo máu** |
| `boss/BossDropConfig` | 6 ô `ld_*` chỉnh luật rơi chung từ cpanel |
| `boss/Boss` | mở `laLauLa()` để bảng rơi biết bỏ qua lâu la |
| `npc_list/TheoDoiBoss` | hiện thêm dòng rơi theo luật chung |
| `consts/ConstNpc`, `npc/NpcFactory` | NPC 90 và ba menu |
| `server/ServerManager` | chốt bảng vàng lúc bảo trì |
| `data/DataGame` | `vsItem` 31 → 32 |

---

## 8. Việc của người vận hành

1. **Tắt server.**
2. `mysqldump -u root -p team2026 item_template npc_template part map_template > backup_84.sql`
3. Chạy `sql/patch/84-luyen-dan.sql` (chạy lại nhiều lần vẫn an toàn). Xem khối (3): mọi cột
   `dat` phải = 1.
4. Kéo bản code mới, biên dịch **bằng JDK 21** (JDK 25 làm Lombok chết và javac báo "0 lỗi" giả).
5. Nếu `data/bossdrop_table.json` đang tồn tại — xem cảnh báo ở mục 4.
6. Bật server. Trong log phải thấy `Kiểm tra icon: mọi icon trong dữ liệu đều có file ảnh`.

> Patch 84 đã được dựng lại và chạy thử trên CSDL nháp `scratch84`: nạp
> `database team2026.sql` rồi chạy toàn bộ 82 patch theo thứ tự (bỏ qua 36 vì đó là file **gỡ**,
> không phải patch), rồi chạy 84 hai lần. **0 lỗi**, cả 8 dòng kiểm tra đều = 1, chạy lại vẫn an
> toàn. CSDL nháp đã xoá sau khi thử; `team2026` thật không bị đụng tới.

## 9. Linh Điền (patch 85)

### Vì sao vẫn cần, dù đã rải đồ rơi ra mọi boss

Sau mục 4b thì Địa Hỏa Tinh và đan phương có ở khắp nơi. Thứ **duy nhất** còn chặn người cày
chay là **linh thảo** — mà linh thảo chỉ rơi ở map Tu Tiên, nơi quái 20 triệu máu và đánh
**50.000 sát thương một đòn**. Người yếu vào đó là chết, tức là họ có xúc tác, có công thức,
nhưng vĩnh viễn không luyện được viên nào.

Linh Điền là **cái sàn** cho đúng nhóm đó: gieo bằng **vàng** — thứ ai cũng kiếm được, không
phải đánh nhau với cái gì cả.

### Luật

| | |
|---|---|
| Số ô | **6** |
| Gieo | **500.000 vàng/ô**, loại linh thảo bốc ngẫu nhiên |
| Chín | **4 giờ** |
| Hái | **3–5 hạt** của **một** loại mỗi ô |

Tối đa 30 hạt mỗi 4 giờ (~180/ngày nếu vào đúng giờ). So với **~670 lá/giờ** khi cày map Tu Tiên
(đo ở [docs 70](70-map-tu-tien-da-lam.md)) thì map Tu Tiên vẫn hơn vài chục lần — Linh Điền
**không cạnh tranh** với nó, chỉ là cái sàn, và là lý do để người ta đăng nhập lại lần thứ hai
trong ngày. Tiện thể cũng là một chỗ tiêu vàng.

### Đặt ở NPC Tu Tiên (đảo Kamê), KHÔNG đặt trong map Tu Tiên

Cố ý. Linh Điền sinh ra cho người yếu, mà map Tu Tiên thì cờ đen bật sẵn và quái đánh 50.000
một đòn — để ruộng trong đó thì đúng nhóm người cần nó lại không vào nổi. Nên nó là mục thứ ba
trên menu NPC Tu Tiên: *Vào map tu tiên / Shop tu tiên / **Linh điền** / Đóng*.

Menu Linh Điền liệt kê 6 ô kèm thời gian còn lại, và chỉ có hai nút: **Gieo hết ô trống** và
**Hái hết ô chín** — không bắt bấm 6 lần.

Nếu có ô đã chín, dòng chữ của NPC báo ngay từ menu gốc, khỏi phải vào xem.

### Lưu ở đâu — và vì sao chỉ thêm MỘT cột

Cột **`player`.`tu_tien`** TEXT. Bảng `player` đã sát trần 65.535 byte một dòng của InnoDB —
patch 78 phải đổi `data_card` từ VARCHAR(10000) sang TEXT mới có chỗ thêm `thong_dit`. Mỗi lần
thêm cột là một lần đánh cược với lỗi 1118, nên cột này cố ý là **cột gom**:

```
ld=2276,1759000000000;0,0;2277,1759000500000;0,0;0,0;0,0
   │    └ lúc gieo (ms)                       └ ô trống
   └ id linh thảo đang gieo
```

Định dạng `khoa=giatri#khoa=giatri`, hiện chỉ có khoá `ld`. **Khoá lạ thì bộ đọc bỏ qua**, nên
tính năng tu tiên sau này nhét thêm khoá vào đây chứ không thêm cột nữa.

Chuỗi hỏng / rỗng / `null` đều cho ra ruộng trống, không bao giờ ném lỗi ra ngoài.

`Manager.ensureSchema` **tự thêm cột** lúc server khởi động nếu thiếu (giống `vqtd`,
`aura_npc`, `thong_dit`), nên quên chạy patch 85 cũng không hỏng. Chạy tay vẫn nên, để thấy lỗi
ngay nếu bảng hết chỗ thay vì phát hiện lúc đang chạy.

> Đã thử trên CSDL nháp `scratch85`: nạp dump, chạy hết 83 patch, cột `tu_tien` thêm được
> **không dính lỗi 1118**, chạy lại patch 85 vẫn an toàn, và bảng vẫn còn chỗ cho ít nhất một
> cột TEXT nữa. Đã xoá CSDL nháp.

### Hai chỗ cố ý không để mất đồ

* **Gieo:** trừ vàng của từng ô ngay sau khi ô đó được gieo, không gom lại trừ một cục — hỏng
  giữa chừng thì cũng không lệch tiền.
* **Hái:** mỗi ô ra một loại duy nhất nên chỉ cần **một** lần nhét túi. Nhét không được (túi đầy)
  thì **để nguyên ô đó** và dừng — cây vẫn còn trên ruộng, người chơi không mất gì.

---

## 10. Còn thiếu

* **Chưa chạy thử trong game một lần nào.** Mọi thứ ở trên mới chỉ biên dịch sạch và kiểm trên
  CSDL nháp.
