# 11 — Spec Boss (phần chung + boss thế giới)

> Tài liệu tổng hợp từ mã nguồn thật trong `SRC/src/nro/models/boss/**`, `SRC/src/nro/models/boss_con_duong_ran_doc/**` và các service liên quan. Tên map tra theo bảng `map_template`, tên item tra theo `item_template`, tên chỉ số (option) tra theo `item_option_template`, tên skill tra theo `skill_template` trong `database team2026.sql`.
>
> Phần boss trong phó bản, đấu trường, luyện tập và sự kiện nằm ở file tách riêng: [11b-boss-su-kien-pho-ban.md](./11b-boss-su-kien-pho-ban.md).

## Mục lục

- [0. Quy ước đọc tài liệu](#0-quy-ước-đọc-tài-liệu)
- [1. Bảng tổng hợp toàn bộ boss](#1-bảng-tổng-hợp-toàn-bộ-boss)
- [2. Cơ chế chung (Boss.java, BossData.java)](#2-cơ-chế-chung-bossjava-bossdatajava)
  - [2.1 Dữ liệu boss — BossData](#21-dữ-liệu-boss--bossdata)
  - [2.2 Vòng đời / trạng thái (BossStatus)](#22-vòng-đời--trạng-thái-bossstatus)
  - [2.3 Kiểu xuất hiện (AppearType), boss nhiều form, boss theo nhóm](#23-kiểu-xuất-hiện-appeartype-boss-nhiều-form-boss-theo-nhóm)
  - [2.4 Vào map, chọn khu](#24-vào-map-chọn-khu)
  - [2.5 Chọn mục tiêu và tấn công](#25-chọn-mục-tiêu-và-tấn-công)
  - [2.6 Nhận sát thương (injured) và các biến thể giới hạn dame](#26-nhận-sát-thương-injured-và-các-biến-thể-giới-hạn-dame)
  - [2.7 Chết, phần thưởng, thông báo](#27-chết-phần-thưởng-thông-báo)
  - [2.8 Tự rời map (autoLeaveMap)](#28-tự-rời-map-autoleavemap)
  - [2.9 Lời thoại (textS / textM / textE)](#29-lời-thoại-texts--textm--texte)
  - [2.10 Tự phát nổ (setBom)](#210-tự-phát-nổ-setbom)
- [3. BossManager và các manager, thread, lịch xuất hiện](#3-bossmanager-và-các-manager-thread-lịch-xuất-hiện)
- [4. Các mẫu phần thưởng dùng chung](#4-các-mẫu-phần-thưởng-dùng-chung)
- [5. Chi tiết boss thế giới theo nhóm cốt truyện](#5-chi-tiết-boss-thế-giới-theo-nhóm-cốt-truyện)
  - [5.1 Nhóm Nappa: Kuku, Mập Đầu Đinh, Rambo](#51-nhóm-nappa-kuku-mập-đầu-đinh-rambo)
  - [5.2 Tiểu đội sát thủ (Xayda)](#52-tiểu-đội-sát-thủ-xayda)
  - [5.3 Tiểu đội sát thủ Namek](#53-tiểu-đội-sát-thủ-namek)
  - [5.4 Fide (Frieza)](#54-fide-frieza)
  - [5.5 Android: Dr.Kôrê & Android 19](#55-android-drkôrê--android-19)
  - [5.6 Android 13 / 14 / 15](#56-android-13--14--15)
  - [5.7 King Kong, Pic, Poc](#57-king-kong-pic-poc)
  - [5.8 Xên bọ hung (Cell) 3 form](#58-xên-bọ-hung-cell-3-form)
  - [5.9 Siêu Bọ Hung + 7 Xên con](#59-siêu-bọ-hung--7-xên-con)
  - [5.10 Cooler](#510-cooler)
  - [5.11 Black Goku](#511-black-goku)
  - [5.12 Cumber](#512-cumber)
  - [5.13 Baby](#513-baby)
  - [5.14 Bojack và đồng bọn](#514-bojack-và-đồng-bọn)
  - [5.15 Siêu Bojack (boss độc lập)](#515-siêu-bojack-boss-độc-lập)
  - [5.16 Fide Vàng (Golden Frieza) 21h + Death Beam](#516-fide-vàng-golden-frieza-21h--death-beam)
  - [5.17 Broly / Super Broly](#517-broly--super-broly)
  - [5.18 Tàu Pảy Pảy (Đông Nam Karin)](#518-tàu-pảy-pảy-đông-nam-karin)
  - [5.19 Hành tinh Yardart (Tập sự / Tân binh / Chiến binh / Đội trưởng)](#519-hành-tinh-yardart-tập-sự--tân-binh--chiến-binh--đội-trưởng)
- [6. Mabư 12h (Cổng phi thuyền → Phòng chỉ huy)](#6-mabư-12h-cổng-phi-thuyền--phòng-chỉ-huy)
- [7. Mabư 14h (Cổng phi thuyền / Bụng Mabư)](#7-mabư-14h-cổng-phi-thuyền--bụng-mabư)
- [8. Boss mini (Boss_mini)](#8-boss-mini-boss_mini)
- [9. Ghi chú / điểm cần lưu ý](#9-ghi-chú--điểm-cần-lưu-ý)

---

## 0. Quy ước đọc tài liệu

| Ký hiệu | Ý nghĩa |
|---|---|
| **ID** | Hằng số trong `boss/BossID.java` (luôn âm). |
| **HP / Dame** | Giá trị gốc trong `BossData` (`hp[]` — nếu mảng nhiều phần tử sẽ random 1 phần tử; `dame` gán vào `nPoint.dameg`). Sau đó `nPoint.calPoint()` được gọi, một số boss ghi đè lại HP/dame lúc vào map (ghi rõ ở từng boss). |
| **Skill "Tên cấp N (hồi Xs)"** | Mỗi phần tử `skillTemp = {skillId, level, cooldown(ms)}`; cooldown là tham số thứ 3 (nếu có). |
| **Hồi sinh** | `secondsRest` (giây) của **form đầu tiên** (`data[0]`) — `Boss` constructor chỉ đọc `data[0].getSecondsRest()`. |
| **Map** | `mapJoin[]` — boss random 1 map trong danh sách mỗi lần xuất hiện. |
| **Rơi cho người kết liễu** | `ItemMap(..., plKill.id)` — item rơi ra đất gắn chủ là người kết liễu. `ItemMap(..., -1)` là item rơi tự do. |
| **Point** | `plKill.event.addEventPoint(n)` — điểm sự kiện, kèm thông báo "+n Point". |
| Giới tính | `ConstPlayer.TRAI_DAT` = Trái Đất, `NAMEC` = Namếc, `XAYDA` = Xayda. |

Tên skill (theo `skill_template`, id = hằng trong `skill/Skill.java`):

| Hằng | id | Tên trong DB |
|---|---|---|
| DRAGON | 0 | Chiêu đấm Dragon |
| KAMEJOKO | 1 | Chiêu Kamejoko |
| DEMON | 2 | Chiêu đấm Demon |
| MASENKO | 3 | Chiêu Masenko |
| GALICK | 4 | Chiêu đấm Galick |
| ANTOMIC | 5 | Chiêu Antomic |
| THAI_DUONG_HA_SAN | 6 | Thái Dương Hạ San |
| TAI_TAO_NANG_LUONG | 8 | Tái tạo năng lượng |
| BIEN_KHI | 13 | Biến hình (biến khỉ) |
| LIEN_HOAN | 17 | Liên hoàn |
| KHIEN_NANG_LUONG | 19 | Khiên năng lượng |
| DICH_CHUYEN_TUC_THOI | 20 | Dịch chuyển tức thời |
| THOI_MIEN | 22 | Thôi miên |
| TROI | 23 | Trói |
| SUPER_KAME | 24 | Super Kamejoko |

Nhóm map lớn dùng lặp lại nhiều lần trong tài liệu:

| Ký hiệu nhóm | Danh sách map id | Ghi chú tên map |
|---|---|---|
| **MAP_THUONG_LON** | 0–20, 24–37, 63–77, 79–84, 92–94, 96–100, 102–110 | Toàn bộ map thường 3 hành tinh (Làng Aru … Vách núi đen, Trạm tàu vũ trụ, Rừng Bamboo … Thung lũng đen, Trại lính Fide … Rừng đá, Núi khỉ đỏ … Siêu Thị, Thành phố phía đông … Thị trấn Ginder, Nhà Bunma … Hang băng) |
| **MAP_0_20** | 0–20 | Làng Aru, Đồi hoa cúc, Thung lũng tre, Rừng nấm, Rừng xương, Đảo Kamê, Đông Karin, Làng Mori, Đồi nấm tím, Thị trấn Moori, Thung lũng Namếc, Thung lũng Maima, Vực maima, Đảo Guru, Làng Kakarot, Đồi hoang, Làng Plant, Rừng nguyên sinh, Rừng thông Xayda, Thành phố Vegeta, Vách núi đen |

---

## 1. Bảng tổng hợp toàn bộ boss

Cột "Nguồn tạo" cho biết boss được sinh ra từ đâu (xem [mục 3](#3-bossmanager-và-các-manager-thread-lịch-xuất-hiện)). HP/Dame ghi theo từng form (`→`).

### 1.1 Boss thế giới (chi tiết ở file này)

| Nhóm | Boss (ID) | HP | Dame | Map | Hồi sinh / lịch | Nguồn tạo (số lượng) |
|---|---|---|---|---|---|---|
| Nappa | Kuku (-20) | 500.000 | 9.000 | 68–72 | 10 phút | `loadBoss` ×5 |
| Nappa | Mập Đầu Đinh (-21) | 1.000.000 | 10.000 | 63–67 | 10 phút | `loadBoss` ×5 |
| Nappa | Rambo (-22) | 1.500.000 | 12.400 | 74–77 | 10 phút | `loadBoss` ×5 |
| TDST | Tiểu đội trưởng (-27) | 50.000.000 | 13.000 | 79, 81, 82, 83 | 5 phút | `loadBoss` ×1 |
| TDST | Số 1 / 2 / 3 / 4 (-26/-25/-24/-23) | 40M / 30,5M / 30M / 25M | 12.500 / 12.000 / 11.000 / 10.000 | theo Tiểu đội trưởng | xuất hiện cùng TĐT | con của TĐT |
| TDST Namek | Tiểu đội trưởng Namek (-315) | 5.000.000 | 15.000 | 7–13, 25, 33, 34, 43 | 5 phút | `loadBoss` ×1 |
| TDST Namek | Số 1/2/3/4 Namek (-314/-313/-312/-311) | 4M / 3,5M / 3M / 2,5M | 13.200 / 12.200 / 10.000 / 10.000 | theo TĐT Namek | cùng TĐT | con |
| Fide | Fide đại ca 1 → 2 → 3 (-28) | 10M → 20M → 30M | 22.000 → 25.000 → 30.000 | 80 | 10 phút | `loadBoss` ×1 |
| Android | Dr.Kôrê (-31) | 2.000.000 | 12.000 | 96, 94, 93 | 10 phút | `loadBoss` ×1 |
| Android | Android 19 (-30) | 1.000.000 | 12.200 | theo Dr.Kôrê | cùng Dr.Kôrê | con |
| Android | Android 14 (-33) | 4.000.000 | 12.000 | 104 | 10 phút | `loadBoss` ×1 |
| Android | Android 15 (-34) | 5.000.000 | 12.200 | 104 | cùng A14 | con |
| Android | Android 13 (-32) | 3.000.000 | 12.055 | 104 | được A14 gọi | con |
| Android | King Kong (-37) | 20.000.000 | 12.000 | 97, 98, 99 | 10 phút | `loadBoss` ×1 |
| Android | Pic (-35) / Poc (-36) | 10M / 15M | 17.022 / 18.000 | theo King Kong | cùng King Kong | con |
| Cell | Xên bọ hung → Xên hoàn thiện → Xên hoàn thiện (-100) | 50M → 100M → 150M | 20.000 → 25.000 → 30.000 | 100 | 30 phút | `loadBoss` ×1 |
| Cell | Xên Hoàn Thiện → Siêu Bọ Hung (-101) | 150M → 200M | 35.000 → 40.000 | 103 | 30 phút | `loadBoss` ×1 |
| Cell | Xên con 1…7 (-102…-108) | 5.000.000 | 15.000 | 103 | được Siêu Bọ Hung gọi | con |
| Cooler | Cooler → Cooler 2 (-29) | 200M → 500M | 32.000 → 50.000 | 110 | 30 phút | `loadBoss` ×1 |
| Black Goku | Black Goku → Super Black Goku (-203) | 500M → 2.000M | 50.000 → 100.000 | 102, 92, 93, 94, 96–100 | 5 phút | `loadBoss` ×2 |
| Cumber | Cumber → Super Cumber (-203999) | 500M → 2.000M | 50.000 → 100.000 | 155 | 5 phút | `loadBoss` ×1 |
| Baby | Baby (3 form) (-925) | 2.000M ×3 | 200.000 → 250.000 → 30.000 | 14 | 15 phút | `loadBoss` ×2 |
| Bojack | Bojack → Siêu Bojack (-320) | 100M → 150M | 300.000 | 3–6, 27–30 | 15 phút | `loadBoss` ×1 |
| Bojack | Bujin / Kogu / Zangya / Bido (-316/-317/-318/-319) | 20M / 40M / 60M / 80M | 170.000 / 180.000 / 207.200 / 250.200 | theo Bojack | cùng Bojack | con |
| Bojack | Siêu Bojack độc lập (-321) | 500M | 300.000 | 3–6, 27–30 | 30 phút | `loadBoss` ×1 |
| 21h | Fide Vàng (-502) | 1.000M | 100.000 | 6 | chỉ 21:00–21:59, nghỉ 5 phút | `loadBoss` ×1 |
| 21h | Death Beam 1…5 (-609…-613) | 500 (bất tử) | giết 1 người | theo Fide Vàng | được Fide Vàng gọi | con |
| Broly | Broly (-1822) | random 500–100.000 | HP/100 | 5, 13, 20, 27–38 | 600 giây | sự kiện `Default` ×30 |
| Broly | Super Broly (-82282) | random 1,5M–16.070.777 | HP/100 | tại chỗ Broly | sinh ra khi Broly rời map | động |
| Karin | Tàu Pảy Pảy (id random) | 10.000 | 100 | 111 | 1 phút | `Map.initBoss` (mỗi khu map 111) |
| Yardart | Tân binh-5 + Tập sự-0…4 | 450k / 350k | 10.000 (động) | 131 | 1 giây (không chết) | `Map.initBoss` (mỗi khu) |
| Yardart | Chiến binh-5 + Tân binh-0…4 | 500k / 450k | 10.000 (động) | 132 | 1 giây | `Map.initBoss` |
| Yardart | Đội trưởng-5 + Chiến binh-0…4 | 1M / 500k | 10.000 (động) | 133 | 1 giây | `Map.initBoss` |
| Mabư 12h | Drabura (-233) | 20M | 10.000 | 114 | chết → hồi sinh sau 60s | `Map.initBoss` (mỗi khu) |
| Mabư 12h | Bui Bui (-234) | 40M | 200.000 | 115 | 60s | `Map.initBoss` |
| Mabư 12h | Bui Bui (-238) | 40M | 200.000 | 117 | 60s | `Map.initBoss` |
| Mabư 12h | Ya côn (-235) | 50M | 200.000 | 118 | 60s | `Map.initBoss` |
| Mabư 12h | Drabura 2 (-237) + Gôku (-341), Ca Đít (-342) | 20M; 60M (/4) | 200.000; 1.000 | 119 | 5 phút | `Map.initBoss` |
| Mabư 12h | Mabư (-236) + Drabura 3 (-343) | 100M; 20M | 10.000; 100.000 | 120 | 1 phút | `Map.initBoss` |
| Mabư 14h | Mabư mập → Super Bư → Bư Tênk → Bư Han → Kid Bư (-214) | 50M → 60M → 80M → 100M → 150M | 500.000 | 127 | 10 phút | `Map.initBoss` |
| Mabư 14h | Super Bư (bụng) (-348) | 50M | 500.000 | 128 | 10 giây | `Map.initBoss` |
| Mini | Sói hẹc quyn (-77) | 10.000 (bất tử) | 1.000 | MAP_THUONG_LON | 10 phút | `loadBoss` ×2 |
| Mini | Ăn Trộm (-365) | random 0–99 | HP/10 | MAP_THUONG_LON (khu 0) | 600 giây | `loadBoss` ×5 |
| Mini | Ở Dơ (-78) | 500.000 | 1.000 | MAP_THUONG_LON (khu 0) | 600.000 giây | `loadBoss` ×5 |
| Mini | Mặt Trời (-79) | 100 | 1 | 5, 7, 0, 14 (khu 0) | 600 giây | `loadBoss` ×20 |
| Mini | Virut (-79) | 100 | 1 | 5, 7, 0, 14 | 600 giây | không được tạo |
| Mini | Rồng Nhí (-386998) | 50M | 1 | MAP_0_20, 24–37 | 1 phút | không được tạo |

### 1.2 Boss phó bản / đấu trường / luyện tập / sự kiện (chi tiết ở file 11b)

| Nhóm | Boss | HP | Dame | Map | Xuất hiện |
|---|---|---|---|---|---|
| Doanh trại | Trung uý Trắng | clan totalDame×50 | clan totalHp/20 | 59 | khi mở doanh trại |
| Doanh trại | Trung uý Xanh Lơ | ×1,1 | ×1,1 | 62 | khi mở doanh trại |
| Doanh trại | Trung uý Thép | ×1,15 | ×1,15 | 55 | khi mở doanh trại |
| Doanh trại | Ninja Áo Tím (+4–6 phân thân) | ×1,2 (phân thân /10) | ×1,2 | 54 | khi mở doanh trại |
| Doanh trại | Rôbốt Vệ Sĩ 00–03 | ×1,3 | ×1,3 | 57 | khi mở doanh trại |
| Bản đồ kho báu | Trung úy Xanh Lơ | 20M × level | 200.000 × level | 137 | khi mở BĐKB |
| Khí gas | Dr Lychee → Hatchiyack | 1M + 15M×level → ×1,5 | 10.000 + 1.000×level → ×1,5 | 148 | khi hết quái |
| Con đường rắn độc | Số 1…6 (Saibamen), Nađíc, Cađích | 500k + 2M×level (×5, ×50) | 10k + 200k×level (×5, ×50) | 144 | khi mở CĐRĐ |
| Siêu thần thủy | Ma vương Pôcôlô | HP người chơi ×5 | dame người chơi | khu riêng người chơi | khi hết quái |
| ĐHVT 23 | 12 vòng: Sói hẹc quyn … Pôcôlô | 10k … 150M | 1.000 … 50.000 | map ĐHVT | theo lượt thi đấu |
| Võ đài Hạt Mít | Đracula, Người vô hình, Bông băng, Vua Quỷ Sa tăng, Thỏ Đầu Bạc | 111%–115% HP người chơi | 1.000–3.000 | 112 | theo lượt thi đấu |
| Siêu hạng | Bản sao đối thủ (Rival) | chỉ số đối thủ | chỉ số đối thủ | map giải | khi thách đấu |
| Nhân bản | Bản sao Commeson | HP người chơi ×10 | dame ×10 | khu người chơi | NPC Potage |
| Luyện tập | Karin, Yajirô, Mr.PôPô, Thượng đế, Khỉ Bubbles, Thần Vũ Trụ, Tổ sư Kaio, Whis (+Tàu Pảy Pảy) | 500 … 550.000×lv | 500 … 45.000 | 46, 48, 49, 50, 154 | khi người chơi gọi |
| Halloween | Bí ma, Dơi, Ma trơi | 500.000 | theo HP người chơi | MAP_THUONG_LON | sự kiện (đang tắt) |
| Trung thu | Khỉ đột; Nguyệt thần + Nhật thần | 100M; 50M | 100.000; 1.000 | MAP_0_20 | sự kiện (đang tắt) |
| Hùng Vương | Thủy Tinh + Sơn Tinh | 50M | 1.000 | MAP_THUONG_LON | sự kiện (bật, nhưng manager không chạy) |
| Noel | Ông già Noel | 500 (bất tử) | 5.000.000 | MAP_THUONG_LON | sự kiện (đang tắt) |
| Tết | Lân con | 5.000.000 | 5.000 | MAP_THUONG_LON | sự kiện (đang tắt) |

---

## 2. Cơ chế chung (Boss.java, BossData.java)

Nguồn: `SRC/src/nro/models/boss/Boss.java`, `BossData.java`, `consts/BossStatus.java`, `consts/AppearType.java`, `consts/BossType.java`.

`Boss extends Player implements IBoss` — boss là một `Player` đặc biệt (`isBoss = true`), dùng chung hệ thống skill, `nPoint`, `effectSkill` với người chơi.

### 2.1 Dữ liệu boss — BossData

`BossData` (Lombok `@Data`) gồm:

| Trường | Kiểu | Ý nghĩa |
|---|---|---|
| `name` | String | Tên; được `String.format(name, Util.nextInt(0,100))` khi `initBase` (tên chứa `%` sẽ nhận số ngẫu nhiên). |
| `gender` | byte | Hành tinh/giới tính. |
| `outfit` | short[6] | `{head, body, leg, bag, aura, eff}`. |
| `dame` | int | Gán `nPoint.dameg`. |
| `hp` | int[] | Random 1 phần tử gán `nPoint.hpg`, `nPoint.hp`. |
| `mapJoin` | int[] | Danh sách map có thể xuất hiện. |
| `skillTemp` | int[][] | `{skillId, level[, cooldown]}`. |
| `textS` / `textM` / `textE` | String[] | Lời thoại lúc xuất hiện / trong lúc đánh / lúc chết. |
| `secondsRest` | int | Thời gian nghỉ (giây) trước khi hồi sinh. Mặc định 0. |
| `typeAppear` | AppearType | Mặc định `DEFAULT_APPEAR`. |
| `bossesAppearTogether` | int[] | Danh sách BossID xuất hiện kèm theo form này. |

Các hằng thời gian trong `BossesData.java`: `REST_1_S=1`, `REST_2_S=2`, `REST_5_S=5`, `REST_10_S=10`, `REST_20_S=20`, `REST_30_S=30`, `REST_1_M=60`, `REST_2_M=120`, `REST_5_M=300`, `REST_10_M=600`, `REST_15_M=900`, `REST_30_M=1800`, `REST_24_H=86400000` (không được dùng).

`initBase()` (mỗi lần respawn một form):
- `name`, `gender`, `nPoint.mpg = 31_07_2002`, `nPoint.dameg = dame`, `nPoint.hpg = hp[random]`, `nPoint.hp = hpg`, gọi `nPoint.calPoint()`, `initSkill()` (xóa skill cũ, tạo skill theo `skillTemp`, gán `coolDown` nếu có phần tử thứ 3), `resetBase()` (reset chỉ số chat).
- `update()` luôn đặt `nPoint.mp = nPoint.mpg` → boss không bao giờ hết KI.

### 2.2 Vòng đời / trạng thái (BossStatus)

Enum: `REST, RESPAWN, JOIN_MAP, CHAT_S, ACTIVE, DIE, CHAT_E, LEAVE_MAP, AFK`.

`Boss.update()` mỗi tick của manager:

1. Nếu `prepareBom` (đang tự nổ) → bỏ qua.
2. `super.update()` (Player), `nPoint.mp = mpg`.
3. Nếu boss đang dính hiệu ứng khống chế (`effectSkill.isHaveEffectSkill()`) hoặc đang tung skill đặc biệt → bỏ qua tick.
4. Ở trạng thái `CHAT_S`, `AFK`, `ACTIVE` → gọi `autoLeaveMap()` (mặc định rỗng).
5. Xử lý theo trạng thái:

| Trạng thái | Hành vi mặc định | Chuyển sang |
|---|---|---|
| `REST` | `rest()`: tính `nextLevel = currentLevel+1` (vòng về 0); nếu form kế tiếp là `DEFAULT_APPEAR` và đã nghỉ đủ `secondsRest` giây kể từ `lastTimeRest` | `RESPAWN` |
| `RESPAWN` | `respawn()`: `currentLevel++` (vòng về 0), `initBase()`, chuyển `NON_PK` | `JOIN_MAP` |
| `JOIN_MAP` | `joinMap()` (xem 2.4), `notifyJoinMap()` | `CHAT_S` (lỗi → `REST`) |
| `CHAT_S` | `chatS()` đọc lần lượt `textS`; xong → `doneChatS()`, đặt `timeChatM=5s` | `ACTIVE` (trừ khi `doneChatS` đổi sang `AFK`) |
| `AFK` | `afk()` — mặc định không làm gì (đứng yên, không đánh) | tùy boss |
| `ACTIVE` | `chatM()`; nếu đang gồng (`isCharging`, 19/20 tick bỏ qua) hoặc đang trói (`useTroi`) thì dừng; `active()` → đổi `PK_ALL` → `attack()` | — |
| `DIE` | — | `CHAT_E` |
| `CHAT_E` | `chatE()` đọc `textE`; xong → `doneChatE()` | `LEAVE_MAP` |
| `LEAVE_MAP` | `leaveMap()` (xem 2.3) | `RESPAWN` (form kế) hoặc `REST` |

Trạng thái khởi tạo: `REST` với `lastTimeRest = 0` → boss `DEFAULT_APPEAR` sẽ xuất hiện **ngay** ở tick đầu tiên sau khi server chạy.

### 2.3 Kiểu xuất hiện (AppearType), boss nhiều form, boss theo nhóm

`AppearType`: `DEFAULT_APPEAR`, `APPEAR_WITH_ANOTHER`, `ANOTHER_LEVEL`, `CALL_BY_ANOTHER`.

| Kiểu | Ý nghĩa thực tế trong code |
|---|---|
| `DEFAULT_APPEAR` | Tự hồi sinh sau `secondsRest` (ở `rest()`). |
| `ANOTHER_LEVEL` | Form sau của cùng một boss. `rest()` không tự respawn form này; form này được kích hoạt ngay trong `leaveMap()`. |
| `APPEAR_WITH_ANOTHER` | Boss con xuất hiện **cùng lúc** với boss cha: khi cha vào map, `wakeupAnotherBossWhenAppear()` gọi `leaveMap()` (nếu con đang ở map) rồi đặt con `RESPAWN`. |
| `CALL_BY_ANOTHER` | Boss con **chỉ** xuất hiện khi có code gọi riêng (`changeStatus(RESPAWN)`). Khi cha vào map, nếu con còn ở map sẽ bị `leaveMap()`. |

**Boss nhiều form (đổi hình):** một `Boss` giữ mảng `data[]`. `leaveMap()`:

```java
if (this.currentLevel < this.data.length - 1) {
    this.lastZone = this.zone;
    this.changeStatus(BossStatus.RESPAWN);   // biến hình ngay tại khu cũ
} else {
    ChangeMapService.gI().exitMap(this);
    this.lastZone = null;
    this.lastTimeRest = System.currentTimeMillis();
    this.changeStatus(BossStatus.REST);      // nghỉ, lần sau quay lại form 0
}
this.wakeupAnotherBossWhenDisappear();
```

→ Chết ở form N (chưa phải form cuối) ⇒ qua `DIE → CHAT_E → LEAVE_MAP` rồi hồi sinh form N+1 ở **cùng khu, cùng tọa độ** (`joinMap` với `currentLevel != 0` dùng `location` cũ). Chết ở form cuối ⇒ rời map, nghỉ `data[0].secondsRest`.

`leaveMapNew()` đặt `currentLevel = data.length` rồi chuyển `LEAVE_MAP` ⇒ rời map hẳn, không biến hình (dùng khi hết giờ/không người chơi).

**Boss theo nhóm:** Trong constructor, với mỗi form `i` có `bossesAppearTogether`, boss cha gọi `BossManager.gI().createBoss(id)` tạo từng boss con, gán `parentBoss = this`, `lv = j` (vị trí). Con vào map tại `parentBoss.zone`, tọa độ `x = parent.x - (lv+1)*30`. Chuỗi "boss sau xuất hiện khi boss trước chết" được cài bằng override `doneChatE()` / `doneChatS()` / `afk()` ở từng class (ghi chi tiết ở mỗi boss).

Lời thoại nhóm dùng prefix trong chuỗi `"|prefix|nội dung"`:

| prefix | Người nói |
|---|---|
| `-1` | Chính boss. |
| `-2` | Một người chơi ngẫu nhiên trong map (≤ 600px, còn sống); nếu không có thì câu thoại bị giữ lại chờ. |
| `-3` | Boss cha (nếu còn sống). |
| `≥ 0` | Boss con thứ `prefix` trong `bossAppearTogether[currentLevel]` (hoặc anh em cùng cha). |

### 2.4 Vào map, chọn khu

`joinMap()` mặc định:

1. Nếu `zoneFinal != null` (boss gắn cố định vào một khu — Mabư, Yardart, Tàu Pảy Pảy…) → vào đúng khu đó, random `x` trong `[100, mapWidth-100]`, `CHAT_S`, đánh thức boss kèm.
2. Nếu `zone == null`: con → khu của cha; chưa có `lastZone` → `getMapJoin()` (random map trong `mapJoin`, random khu qua `MapService.getMapWithRandZone`); có `lastZone` → khu cũ.
3. Với form 0 và không có cha:
   - `isZone01SpawnDisabled = true` và map có > 2 khu: chọn khu random từ 2 → cuối; nếu khu đã có boss thì tăng dần; hết khu → quay lại `REST`.
   - Ngược lại: từ khu 0 bỏ qua khu có > 10 người chơi, rồi bỏ qua khu đã có boss; hết khu → khu 0.
4. `ChangeMapService.changeMap(...)`, gửi cờ, `notifyJoinMap()`, `CHAT_S`. Lỗi → `REST` (log tối đa 5 lần).

Constructor 4 tham số `Boss(id, isNotifyDisabled, isZone01SpawnDisabled, data...)` bật/tắt 2 cờ trên.

### 2.5 Chọn mục tiêu và tấn công

`getPlayerAttack()`:
- Bỏ mục tiêu nếu đã chết hoặc khác khu.
- Chọn lại `zone.getRandomPlayerInMap()` khi chưa có mục tiêu **hoặc** mỗi 5–7 giây (`timeTargetPlayer = Util.nextInt(5000,7000)`).
- Không đánh đệ tử của chính mình.

`attack()` (mỗi ≥ 100ms và đang `PK_ALL`):
- Chọn **ngẫu nhiên 1 skill** trong danh sách.
- Tầm đánh: Kamejoko/Masenko/Antomic = `RANGE_ATTACK_CHIEU_CHUONG` (300); Dragon/Demon/Galick/Liên hoàn/Kaioken = `RANGE_ATTACK_CHIEU_DAM` (100); còn lại 500.
- Trong tầm: 25% di chuyển lại gần (chưởng: lệch 20–200px, đấm: lệch 10–40px), rồi `SkillService.useSkill(this, pl, ...)`.
- Ngoài tầm: 50% `moveToPlayer` (mỗi bước 40–60px, 30% nhảy lên 50px).

### 2.6 Nhận sát thương (injured) và các biến thể giới hạn dame

`Boss.injured()` mặc định:
1. Không xuyên giáp (`!piercing`) và trúng tỉ lệ `nPoint.tlNeDon/1000` → chat "Xí hụt", nhận 0.
2. Kẻ tấn công đang cầm Ngọc Rồng Namếc (`idNRNM != -1`) → chỉ nhận **1** sát thương.
3. `nPoint.subHP(damage)` — **không** trừ giáp (`def`), không giới hạn.
4. HP ≤ 0 → `setDie(plAtt)` + `die(plAtt)`.

Hàm trừ giáp dùng ở nhiều boss con: `NPoint.subDameInjureWithDeff(dame) = max(dame - def, 1)`.

Bảng tổng hợp các kiểu override `injured` (chi tiết ở từng boss):

| Boss | Né | Giảm/Giới hạn dame | Đặc biệt |
|---|---|---|---|
| Xên bọ hung | tlNeDon/1000 | (dame/2) − def; khiên: /4 | — |
| Siêu Bọ Hung | tlNeDon/1000 | (dame/3) − def; khiên: /4 | Lần chết đầu gọi 7 Xên con, hồi đầy máu; chết thật → tự phát nổ |
| Cooler | cố định 1% | dame − def | — |
| Black Goku / Cumber | tlNeDon/1000 | form ≥1: /2; rồi (dame − rand(0..100.000)) − def; khiên → 1 | — |
| Baby | tlNeDon/1000 | (dame×0,7/2) − def; khiên: /4 | — |
| Fide Vàng | tlNeDon/1000 | dame − def; khiên → 1; **tối đa 50.000.000/đòn** | — |
| Death Beam | — | luôn 0 | bất tử |
| Broly / Super Broly | tlNeDon/1000 | dame − def; **tối đa hpMax/100/đòn** (trừ Tự phát nổ) | có tỉ lệ tăng chỉ số khi bị đánh |
| Tàu Pảy Pảy (Karin) | xem ghi chú | dame − def; khiên → 1; **tối đa 100/đòn** | cộng TN/SM cho người đánh |
| Android 19 | — | — | Bị Kamejoko/Masenko/Antomic: hồi 80% dame, nhận 0 |
| Android 13/14/15 | — | — | Không chết theo điều kiện nhóm |
| Yardart | 10% | tối đa hpMax/100/đòn | không bao giờ chết (hồi đầy máu + thưởng) |
| Drabura (114) | 10% | tối đa 20M | miễn Tự phát nổ |
| Bui Bui, Bui Bui 2, Ya côn, Drabura 2 | 20% | Drabura 2 tối đa 20M | miễn Kamejoko/Masenko/Antomic/Liên hoàn |
| Mabư 12h | 20% | tối đa 50M ± 10.000 | — |
| Gôku / Ca Đít | xem ghi chú | Gôku 20M, Ca Đít 10M | — |
| Mabư 14h / Super Bư | 10% | tối đa 30M ± 10.000 | Form cuối chỉ chết bởi Quả cầu kênh khi |
| Sói hẹc quyn, Ông già Noel | — | luôn 0 | bất tử |
| Ăn Trộm, Mặt Trời, Virut | — | luôn 1 | — |
| Ở Dơ | — | luôn 50.000 | — |

### 2.7 Chết, phần thưởng, thông báo

`Boss.die(plKill)` mặc định:

```java
if (plKill != null && (this.zone.map.mapId != 140 || !isMapMaBu(...) || !isMapDoanhTrai(...) || !isMapBanDoKhoBau(...))) {
    if (!plKill.isBot) reward(plKill);
    ServerNotify.gI().notify(plKill.name + ": Đã tiêu diệt được " + this.name + " mọi người đều ngưỡng mộ.");
    this.changeStatus(BossStatus.DIE);
} else { ... }
```

- Phần thưởng chỉ trao cho **người kết liễu** (`plKill`), không có cơ chế top dame trong code boss. Item rơi ra đất gắn chủ `plKill.id`.
- Bot (`isBot`) kết liễu thì không có thưởng.
- `Boss.reward()` mặc định chỉ gọi `TaskService.checkDoneTaskKillBoss(plKill, this)` (hoàn thành nhiệm vụ giết boss + thành tựu `TRUM_KET_LIEU_BOSS`).
- Thông báo toàn server khi boss xuất hiện (`notifyJoinMap`): `"BOSS <tên> vừa xuất hiện tại <tên map>"`, **không** gửi nếu `isNotifyDisabled` hoặc map 140, 111, map phó bản (BĐKB/Doanh trại/CĐRĐ/Khí gas), map Mabư (114–120), map Ngọc Rồng Sao Đen (85–91).

Bảng nhiệm vụ gắn với boss (`TaskService.checkDoneTaskKillBoss`):

| Boss | Nhiệm vụ |
|---|---|
| Kuku / Mập Đầu Đinh / Rambo | TASK_19_0 / 19_1 / 19_2 |
| Số 4 / Số 3 / Số 2 / Số 1 / Tiểu đội trưởng | TASK_20_1 / 20_2 / 20_3 / 20_4 / 20_5 |
| Fide form 1 / 2 / 3 | TASK_21_1 / 21_2 / 21_3 |
| Android 19 / Dr.Kôrê | TASK_23_1 / 23_2 |
| Android 15 / 14 / 13 | TASK_24_1 / 24_2 / 24_3 |
| Poc / Pic / King Kong | TASK_25_1 / 25_2 / 25_3 |
| Xên bọ hung form 1 / 2 / 3 | TASK_26_1 / 26_2 / 26_3 |
| Xên con 1–7 | TASK_27_3 |
| Siêu Bọ Hung form 2 | TASK_27_4 |
| Drabura (1, 2, 3) | TASK_28_1 và 28_5 |
| Bui Bui (1, 2) | TASK_28_2 và 28_3 |
| Ya côn | TASK_28_4 |
| Mabư 12h | TASK_28_6 |

### 2.8 Tự rời map (autoLeaveMap)

Mặc định rỗng. Phần lớn boss thế giới dùng mẫu:

```java
if (Util.canDoWithTime(st, 900000)) this.leaveMapNew();            // 15 phút
if (this.zone != null && this.zone.getNumOfPlayers() > 0) st = now; // có người thì reset
```

→ **Rời map sau 15 phút liên tục không có người chơi trong khu**. `st` được đặt lại mỗi lần `joinMap`. Các ngoại lệ ghi ở từng boss.

### 2.9 Lời thoại (textS / textM / textE)

- `chatS`/`chatE`: đọc tuần tự, mỗi câu cách nhau `min(độ dài × 100ms, 2000ms)`.
- `chatM`: chỉ khi boss `PK_ALL`; chọn ngẫu nhiên 1 câu, cách nhau 3–20 giây.

### 2.10 Tự phát nổ (setBom)

`Boss.setBom(plAtt)` (dùng bởi Siêu Bọ Hung): đặt HP = 1, chat "Rồi, rồi, mày xong rồi!", gửi hiệu ứng gồng (msg -45, skill 104), vòng lặp chờ 2,5 giây rồi `setDie` + `die(plAtt)`, gây sát thương = `hpMax` của boss lên **mọi quái** và **mọi người chơi** trong khu (người chơi bị đánh không xuyên giáp, trừ map offline).

---

## 3. BossManager và các manager, thread, lịch xuất hiện

Nguồn: `boss/Boss_Manager/*.java`, `server/ServerManager.java`, `map/Map.java` (`initBoss`), `event/EventManager.java`, `event_list/*.java`.

### 3.1 Phân bổ boss vào manager

Constructor `Boss(id, data...)` (không có `BossType`) → `BossManager.gI().addBoss(this)`.
Constructor `Boss(BossType, id, data...)` → manager theo loại:

| BossType | Manager | Thread | Nhịp tick | Điều kiện vòng lặp |
|---|---|---|---|---|
| (không type) | `BossManager` | "Update boss" | 1500ms − thời gian xử lý | `ServerManager.isRunning` |
| `YARDART` | `YardartManager` | "Update yardart boss" | 1500ms (kế thừa) | `isRunning` |
| `FINAL` | `FinalBossManager` | "Update final boss" | 1500ms | `isRunning` |
| `SKILLSUMMONED` | `SkillSummonedManager` | "Update skill-summoned boss" | 150ms (tối thiểu 10ms) | `!Maintenance.isRunning` |
| `BROLY` | `BrolyManager` | "Update broly boss" | 1500ms | `isRunning` |
| `PHOBAN` | `OtherBossManager` | "Update other boss" | 150ms | `!Maintenance.isRunning` |
| `PHOBANDT` | `RedRibbonHQManager` | "Update red ribbon hq boss" | 150ms | như trên |
| `PHOBANBDKB` | `TreasureUnderSeaManager` | "Update treasure under sea boss" | 150ms | như trên |
| `PHOBANCDRD` | `SnakeWayManager` | "Update snake way boss" | 150ms | như trên |
| `PHOBANKGHD` | `GasDestroyManager` | "Update gas destroy boss" | 150ms | như trên |
| `TRUNGTHU_EVENT` | `TrungThuEventManager` | **không có thread** | — | — |
| `HALLOWEEN_EVENT` | `HalloweenEventManager` | **không có thread** | — | — |
| `CHRISTMAS_EVENT` | `ChristmasEventManager` | **không có thread** | — | — |
| `HUNGVUONG_EVENT` | `HungVuongEventManager` | **không có thread** | — | — |
| `TET_EVENT` | `LunarNewYearEventManager` | **không có thread** | — | — |
| — | `AnTromManager` | không dùng | — | — |

Các manager 150ms duyệt danh sách từ cuối lên, bắt exception theo từng boss và **xóa boss lỗi** khỏi danh sách. Các manager 1500ms (`BossManager.run`) không xóa boss lỗi; `Thread.sleep` âm sẽ ném exception và bị nuốt.

Boss con được tạo bằng `BossManager.gI().createBoss(id)` trong constructor của boss cha nên cũng thuộc manager theo type của chính con.

### 3.2 Thứ tự khởi động (ServerManager.run)

1. `BossManager.gI().loadBoss()` — tạo boss thế giới.
2. `Manager.MAPS.forEach(Map::initBoss)` — tạo boss gắn khu.
3. `EventManager.gI().init()` — tạo boss sự kiện.
4. Start thread cho 10 manager (BossManager, Yardart, Final, SkillSummoned, Broly, Other, RedRibbonHQ, TreasureUnderSea, SnakeWay, GasDestroy).

### 3.3 `loadBoss()` — boss thế giới cố định

| BossID | Số lượng |
|---|---|
| TIEU_DOI_TRUONG, TIEU_DOI_TRUONG_NM, BOJACK, SUPER_BOJACK, KING_KONG, FIDE, ANDROID_14, DR_KORE, CUMBER | 1 |
| XEN_BO_HUNG, SIEU_BO_HUNG, COOLER, GOLDEN_FRIEZA | 1 |
| KUKU, MAP_DAU_DINH, RAMBO | 5 mỗi loại |
| BLACK_GOKU | 2 |
| SOI_HEC_QUYN1 (Sói hẹc quyn mini) | 2 |
| AN_TROM | 5 |
| O_DO1 (Ở Dơ mini) | 5 |
| BABY | 2 |
| MAT_TROI | 20 |

### 3.4 `Map.initBoss()` — boss gắn cố định mỗi khu

Với **mỗi khu** của các map sau, tạo 1 boss, gán `zoneFinal = zone` và `joinMapByZone(zone)`:

| Map id | Tên map | Boss |
|---|---|---|
| 111 | Đông Nam Karin | Tàu Pảy Pảy (TaoPaiPai) |
| 114 | Cổng phi thuyền | Drabura |
| 115 | Phòng chờ | Bui Bui |
| 117 | Cửa Ải 1 | Bui Bui 2 |
| 118 | Cửa Ải 2 | Ya côn |
| 119 | Cửa Ải 3 | Drabura 2 (+ Gôku, Ca Đít) |
| 120 | Phòng chỉ huy | Mabư (+ Drabura 3) |
| 127 | Cổng phi thuyền | Mabư 14h (5 form) |
| 128 | Bụng Mabư | Super Bư |
| 131 | Hành Tinh Yardart | Tân binh-5 (+ Tập sự-0…4) |
| 132 | Hành Tinh Yardart 2 | Chiến binh-5 (+ Tân binh-0…4) |
| 133 | Hành Tinh Yardart 3 | Đội trưởng-5 (+ Chiến binh-0…4) |

### 3.5 `EventManager.init()` — boss sự kiện

| Sự kiện (class) | Cờ | Trạng thái trong code | Boss tạo |
|---|---|---|---|
| `Default` | luôn chạy | bật | BROLY ×30 |
| `LunarNewYear` | `LUNNAR_NEW_YEAR=true` | dòng `init()` bị comment | LAN_CON ×10 |
| `InternationalWomensDay` | true | bị comment | — |
| `Halloween` | true | bị comment | BIMA ×10, MATROI ×10, DOI ×10 |
| `Christmas` | true | bị comment | ONG_GIA_NOEL ×30 |
| `HungVuong` | true | **bật** | THUY_TINH ×10 (mỗi con kèm Sơn Tinh) |
| `TrungThu` | true | bị comment | KHIDOT ×10, NGUYETTHAN ×10 |
| `TopUp` | true | bật | không có boss |

### 3.6 Lịch theo giờ

| Boss | Điều kiện giờ (giờ Việt Nam) | Nguồn |
|---|---|---|
| Fide Vàng | `TimeUtil.is21H()` — 21:00 → 21:59 | `GoldenFrieza.joinMap/autoLeaveMap` |
| Mabư 12h (map 114–120) | `TimeUtil.isMabuOpen()` 12:00 → 12:59 (giới hạn vào map ở service) | `utils/TimeUtil.java`, `MajinBuuService.HOUR_OPEN_MAP_MABU = 12` |
| Mabư 14h (map 127–128) | `TimeUtil.isMabu14HOpen()` 14:00 → 14:59 | `utils/TimeUtil.java` |

Các boss Mabư/Yardart/Tàu Pảy Pảy luôn tồn tại trong manager; giờ mở chỉ khống chế việc người chơi vào map.

### 3.7 Tiện ích admin

`BossManager.showListBoss(player)` (chỉ admin): gửi danh sách boss (bỏ qua boss có map đầu thuộc map boss final, hủy diệt, Cađíc, Yardart, Mabư, NRSĐ) kèm trạng thái và `tên map(id) khu N`.

---

## 4. Các mẫu phần thưởng dùng chung

Để tránh lặp, các boss dùng lại cùng khối code thưởng được đặt tên mẫu như sau.

### Mẫu A — "Vàng + Ngọc rồng 5–7 sao"

Dùng bởi: Kuku, Mập Đầu Đinh, Rambo, Số 1–4, Tiểu đội trưởng, Fide, Dr.Kôrê, Android 13/14/15/19, King Kong, Pic, Poc, Xên bọ hung, Xên con.

| Thứ tự | Nội dung | Tỉ lệ | Số lượng | Người nhận |
|---|---|---|---|---|
| 1 | +5 Point sự kiện | 100% | 5 | người kết liễu |
| 2 | Kiểm tra nhiệm vụ giết boss | 100% | — | người kết liễu |
| 3 | Vàng (item 190) | 100% | 20.000 – 30.001 | rơi đất, chủ = người kết liễu |
| 4 | 1 viên ngẫu nhiên: Ngọc Rồng 5 sao (18) / 6 sao (19) / 7 sao (20) | 80% | 1 | rơi đất, chủ = người kết liễu |

(Xên bọ hung, Xên con thêm cập nhật huy hiệu `TRUM_SAN_BOSS`. Ở Xên con 1 điểm +5 Point nằm trong khối 80%.)

### Mẫu B — "Boss cao cấp"

Dùng bởi: Cooler, Siêu Bọ Hung, Black Goku, Cumber, Baby, nhóm Mabư 12h. Các tỉ lệ X/Y/Z khác nhau theo boss (ghi ở từng boss).

| Thứ tự | Nội dung | Tỉ lệ | Số lượng |
|---|---|---|---|
| 1 | 1 món **đồ Thần Linh** (`ItemService.randDoTLBoss`): Nhẫn Thần Linh (561), Găng (562/564/566), Quần (556/558/560), Áo (555/557/559), Giầy (563/565/567) | X% | 1 |
| 2 | Vàng (190) | 100% | 20.000 – 30.000 |
| 3 | 1 món **đồ cấp**: 70% nhóm Áo/Quần/Giày, 30% nhóm Găng/Rada; chỉ số shop × 100–115%; thêm option 107 "# Sao Pha Lê" = 1–3 (80%), 4–5 (17%), 6 (3%) | Y% | 1 |
| 4 | 1 loại trong danh sách Ngọc Rồng 2–7 sao (15–20) [+ Nhẫn thời không sai lệch (992) nếu có] | Z% | 1–3 |

Danh sách đồ cấp nhóm Áo/Quần/Giày: Áo bạc Goku (230), Áo vàng Goku (231), Áo da Calic (232), Áo sắt Tron (234), Áo đồng Tron (235), Áo bạc Zealot (236), Áo lông đỏ (238), Áo siêu xayda (239), Áo Kaio (240), Quần bạc Goku (242), Quần vàng Goku (243), Quần da Calic (244), Quần sắt Tron (246), Quần đồng Tron (247), Quần bạc Zealot (248), Quần lông đỏ (250), Quần siêu Xayda (251), Quần Kaio (252), Giày bạc Goku (266), Giày vàng Goku (267), Giày da Calic (268), Giày sắt Tron (270), Giày đồng Tron (271), Giày bạc Zealot (272), Giày lông đỏ (274), Giày siêu Xayda (275), Giày Kaio (276).

Nhóm Găng/Rada: Găng bạc Goku (254), Găng vàng Goku (255), Găng da Calic (256), Găng sắt Tron (258), Găng đồng Tron (259), Găng bạc Zealot (260), Găng lông đỏ (262), Găng siêu Xayda (263), Găng Kaio (264), Rada cấp 9 (278), Rada cấp 10 (279), Rada cấp 11 (280).

Tất cả item của mẫu B rơi ra đất, chủ = người kết liễu.

### Mẫu C — "Ngọc + Cải trang + Ngọc rồng 6/7 sao"

Dùng bởi: nhóm Bojack, Siêu Bojack, Tiểu đội sát thủ Namek.

| Thứ tự | Nội dung | Tỉ lệ | Số lượng |
|---|---|---|---|
| 1 | Ngọc (item 77) — 4 vòng lặp rơi nhiều cụm: 1 cụm + `nextInt(2)` cụm + `nextInt(3,4)` (hoặc 3) cụm + 3 cụm | 100% | mỗi cụm random (xem boss) |
| 2 | Cải trang tương ứng boss (options lấy theo shop `getListOptionItemShop`) | 100% | 1 |
| 3 | Ngọc Rồng 6 sao (19) | 100% | 1 |
| 4 | Ngọc Rồng 7 sao (20) | 100% | 1 |
| 5 | Point sự kiện | 100% | +1 hoặc +5 |

---

## 5. Chi tiết boss thế giới theo nhóm cốt truyện

### 5.1 Nhóm Nappa: Kuku, Mập Đầu Đinh, Rambo

Nguồn: `boss/Nappa/Kuku.java`, `MapDauDinh.java`, `Rambo.java`; dữ liệu `BossesData.KUKU`, `MAP_DAU_DINH`, `RAMBO`.

| Thuộc tính | Kuku | Mập Đầu Đinh | Rambo |
|---|---|---|---|
| ID | -20 | -21 | -22 |
| Hành tinh | Xayda | Xayda | Xayda |
| Form | 1 | 1 | 1 |
| HP | 500.000 | 1.000.000 | 1.500.000 |
| Dame | 9.000 | 10.000 | 12.400 |
| Skill | Masenko cấp 3 (hồi 1s); Liên hoàn cấp 7 (hồi 1s) | Galick cấp 7 (hồi 1s); Antomic cấp 7 (hồi 10s) | Galick cấp 7 (hồi 1s); Antomic cấp 7 (hồi 10s) |
| Map | 68 Thung lũng Nappa, 69 Vực cấm, 70 Núi Appule, 71 Căn cứ Raspberry, 72 Thung lũng Raspberry | 63 Trại lính Fide, 64 Núi dây leo, 65 Núi cây quỷ, 66 Trại qủy già, 67 Vực chết | 74 Đồi cây Fide, 75 Khe núi tử thần, 76 Núi đá, 77 Rừng đá |
| Hồi sinh | 10 phút | 10 phút | 10 phút |
| Số lượng | 5 | 5 | 5 |
| Thông báo xuất hiện | tắt (`isNotifyDisabled = true`) | tắt | tắt |
| Khu | từ khu 2 trở lên | từ khu 2 | từ khu 2 |
| Thoại | textM: "Ta sẽ tàn sát khu này trong vòng 5 phút nữa", "Tao đã có lệnh của đại ca Fide rồi"… | textM: "Tao chỉ cần 10 giây để giết hết bọn mày"… | textM: "Thấy ta đẹp trai không"…; textE: "Ôi bạn ơi..." |
| Phần thưởng | Mẫu A | Mẫu A | Mẫu A |
| Nhiệm vụ | TASK_19_0 | TASK_19_1 | TASK_19_2 |

Điều kiện đặc biệt:
- `autoLeaveMap`: **rời map sau 15 phút kể từ khi vào map**, bất kể có người hay không (đoạn reset theo người chơi bị comment), dùng `changeStatus(LEAVE_MAP)`.
- Không override `injured` → nhận sát thương thô, không trừ giáp.

### 5.2 Tiểu đội sát thủ (Xayda)

Nguồn: `boss/tieu_doi_sat_thu/TDT.java`, `SO1.java`…`SO4.java`; dữ liệu `BossesData.TIEU_DOI_TRUONG`, `SO_1`…`SO_4`.

| Boss | ID | HP | Dame | Skill | Kiểu xuất hiện |
|---|---|---|---|---|---|
| Tiểu đội trưởng | -27 | 50.000.000 | 13.000 | Masenko cấp 7 (1s); Galick cấp 7 (1s) | DEFAULT, nghỉ 5 phút, gọi kèm `[SO_2, SO_1, SO_3, SO_4]` |
| Số 1 | -26 | 40.000.000 | 12.500 | Liên hoàn cấp 7 (1s); Kamejoko cấp 4 (10s) | APPEAR_WITH_ANOTHER |
| Số 2 | -25 | 30.500.000 | 12.000 | Galick cấp 7 (1s); Antomic cấp 3 (3s) | APPEAR_WITH_ANOTHER |
| Số 3 | -24 | 30.000.000 | 11.000 | Liên hoàn cấp 7 (1s); Antomic cấp 4 (1s) | APPEAR_WITH_ANOTHER |
| Số 4 | -23 | 25.000.000 | 10.000 | Liên hoàn cấp 7 (1s); Masenko cấp 7 (1s); Thôi miên cấp 7 (100s) | APPEAR_WITH_ANOTHER |

- Hành tinh: Xayda. Map: 79 Núi khỉ đỏ, 81 Hang quỷ chim, 82 Núi khỉ đen, 83 Hang khỉ đen. Khu ≥ 2, có thông báo xuất hiện.
- **Chuỗi kích hoạt:** tất cả đều `doneChatS → AFK` (đứng yên, không đánh). Chỉ người chơi đánh được chúng.
  - Số 4 chết (`doneChatE`) → Số 3 chuyển `ACTIVE`.
  - Số 3 chết → Số 1 và Số 2 (nếu còn sống) chuyển `ACTIVE`.
  - Số 1 chết → nếu Số 2 đã chết thì Tiểu đội trưởng `ACTIVE`; Số 2 chết → nếu Số 1 đã chết thì Tiểu đội trưởng `ACTIVE`.
- Tiểu đội trưởng: mỗi 10 giây khi tấn công, chat "Úm ba la xì bùa", 50% mỗi người chơi trong khu bị **đổi thân xác** (`EffectSkillService.setIsBodyChangeTechnique`).
- Thoại chung textM: "Oải rồi hả", "Một mình tao chấp hết tụi bây", "Đại ca Fide có nhầm không nhỉ"…; textE: "Fide gọi ta về, ngươi có ngon thì chờ ở đây" + câu nhờ đồng đội "Để tao xử nó cho".
- `autoLeaveMap`: mẫu 15 phút không người → `leaveMapNew`.
- `moveTo` bị chặn nếu `currentLevel == 1` (không xảy ra vì mỗi boss 1 form).
- Phần thưởng: Mẫu A cho cả 5 boss. Nhiệm vụ TASK_20_1…20_5.

### 5.3 Tiểu đội sát thủ Namek

Nguồn: `boss/tieu_doi_sat_thu_namek/TDT_NM.java`, `SO1_NM.java`…`SO4_NM.java`; dữ liệu `*_NM`.

| Boss | ID | HP | Dame | Skill | Cải trang rơi |
|---|---|---|---|---|---|
| Tiểu đội trưởng Namek | -315 | 5.000.000 | 15.000 | Masenko cấp 7 (1s); Galick cấp 7 (1s) | 433 Cải trang (thành Tiểu Đội Trưởng Ginyu) |
| Số 1 Namek | -314 | 4.000.000 | 13.200 | Liên hoàn 7 (1s); Kamejoko 4 (10s) | 432 Cải trang (thành Số 1) |
| Số 2 Namek | -313 | 3.500.000 | 12.200 | Galick 7 (1s); Antomic 3 (3s) | 431 Cải trang (thành Số 2) |
| Số 3 Namek | -312 | 3.000.000 | 10.000 | Liên hoàn 7 (1s); Antomic 4 (1s) | 430 Cải trang (thành Số 3) |
| Số 4 Namek | -311 | 2.500.000 | 10.000 | Liên hoàn 7 (1s); Masenko 7 (1s); Thôi miên 7 (100s) | 429 Cải trang (thành Số 4) |

- Hành tinh: Xayda. Map: 7 Làng Mori, 8 Đồi nấm tím, 9 Thị trấn Moori, 10 Thung lũng Namếc, 11 Thung lũng Maima, 12 Vực maima, 13 Đảo Guru, 25 Trạm tàu vũ trụ, 34 Đông Nam Guru, 33 Nam Guru, 43 Vách núi Moori.
- Tiểu đội trưởng Namek nghỉ 5 phút, kèm `[SO_4_NM, SO_3_NM, SO_2_NM, SO_1_NM]`.
- Chuỗi AFK/kích hoạt giống bản Xayda (Số 4 → Số 3 → Số 1 & 2 → Tiểu đội trưởng). Tiểu đội trưởng Namek **không** có đổi thân xác.
- Thoại textM tương tự; textE: "Cay quá!", "Ta mà lại thua được sao?", "Hãy trả thù cho ta!".
- Phần thưởng (Mẫu C, không gọi nhiệm vụ):

| Nội dung | Số lượng / cụm |
|---|---|
| Ngọc (77) cụm 1 | 1–2 |
| Ngọc (77) vòng `nextInt(2)` | 1–3 mỗi cụm |
| Ngọc (77) vòng 3 (Số 4/TĐT: `nextInt(3,4)`; Số 1–3: 3 cụm) | 1–4 mỗi cụm |
| Ngọc (77) vòng 4 (Số 2/3: `nextInt(3,4)`; còn lại 3 cụm) | 1–5 mỗi cụm |
| Cải trang tương ứng (bảng trên) | 1 |
| Ngọc Rồng 6 sao (19), 7 sao (20) | 1 mỗi loại |
| Point | Số 1–4: +1; Tiểu đội trưởng: +5 |

### 5.4 Fide (Frieza)

Nguồn: `boss/Frieza/Fide.java`; dữ liệu `FIDE_DAI_CA_1/2/3`.

| Form | Tên | HP | Dame | Skill | Kiểu |
|---|---|---|---|---|---|
| 1 | Fide đại ca 1 | 10.000.000 | 22.000 | Masenko 7 (1s); Galick 7 (1s) | DEFAULT, nghỉ 10 phút |
| 2 | Fide đại ca 2 | 20.000.000 | 25.000 | Masenko 7 (1s); Galick 7 (1s) | ANOTHER_LEVEL |
| 3 | Fide đại ca 3 | 30.000.000 | 30.000 | Masenko 7 (1s); Galick 7 (1s) | ANOTHER_LEVEL |

- ID -28, Xayda, map 80 Núi khỉ vàng. Có thông báo. Spawn theo quy tắc khu mặc định (không bật khu ≥2).
- Thoại: form 1 textS người chơi "Fide!!!, với những gì ngươi đã làm với người Xayda và Namek…", textE "Ác quỷ biến hình, hây aaaa..."; form 2 textE "Ác quỷ biến hình, Graaaaa...."; form 3 textE "Lũ khốn.. Một ngày nào đó ta sẽ quay lại và trả thù các ngươi".
- Mỗi form chết → Mẫu A + nhiệm vụ TASK_21_1/2/3 theo form.
- `autoLeaveMap`: 15 phút không người.

### 5.5 Android: Dr.Kôrê & Android 19

Nguồn: `boss/Android/DrKore.java`, `Android19.java`.

| Thuộc tính | Dr.Kôrê | Android 19 |
|---|---|---|
| ID | -31 | -30 |
| Hành tinh | Trái Đất | Trái Đất |
| HP / Dame | 2.000.000 / 12.000 | 1.000.000 / 12.200 |
| Skill | Thôi miên 3 (10s); Kamejoko 7 (10s); Liên hoàn 7 (1s) | Kamejoko 7 (1s); Liên hoàn 7 (10s) |
| Map | 96 Cao nguyên, 94 Đảo Balê, 93 Thành phố phía nam | theo Dr.Kôrê |
| Xuất hiện | DEFAULT, nghỉ 10 phút, kèm `[ANDROID_19]` | APPEAR_WITH_ANOTHER |
| Phần thưởng | Mẫu A (TASK_23_2) | Mẫu A (TASK_23_1) |

Cơ chế:
- Dr.Kôrê `doneChatS` → Android 19 chuyển `PK_ALL`. Khi Dr.Kôrê chuyển `PK_ALL` thì chat "Mau đền mạng cho thằng em trai ta".
- Android 19 rời map (`wakeupAnotherBossWhenDisappear`) → Dr.Kôrê chuyển `PK_ALL`.
- `chatM` Dr.Kôrê: 1/61 lần thay bằng cặp thoại "Hút năng lượng của nó, mau lên" / "Tuân lệnh đại ca, hê hê hê".
- **Android 19 hấp thụ chưởng:** bị đánh bằng Kamejoko/Masenko/Antomic → hồi HP 80% sát thương, nhận 0, 20% chat "Hấp thụ.. các ngươi nghĩ sao vậy?".
- Dr.Kôrê có hàm `injured(Player, int, ...)` tương tự (hồi 100% dame) nhưng **sai chữ ký** (int thay vì long) nên không override được → Dr.Kôrê thực tế không hấp thụ chưởng.
- Cả hai: `autoLeaveMap` 15 phút không người.

### 5.6 Android 13 / 14 / 15

Nguồn: `boss/Android/Android13.java`, `Android14.java`, `Android15.java`.

| Thuộc tính | Android 14 (cha) | Android 15 | Android 13 |
|---|---|---|---|
| ID | -33 | -34 | -32 |
| HP / Dame | 4.000.000 / 12.000 | 5.000.000 / 12.200 | 3.000.000 / 12.055 |
| Skill | Kamejoko 7 (10s); Liên hoàn 7 (1s) | như A14 | như A14 |
| Xuất hiện | DEFAULT, nghỉ 10 phút, kèm `[ANDROID_13, ANDROID_15]` | APPEAR_WITH_ANOTHER | CALL_BY_ANOTHER |
| Nhiệm vụ | TASK_24_2 | TASK_24_1 | TASK_24_3 |

- Trái Đất; map 104 Sân sau siêu thị.
- A14 `doneChatS` → A15 `PK_ALL`.
- **Gọi Android 13:** lần đầu A14 hoặc A15 nhận đòn chí mạng (`damage >= hp`) khi `callApk13 = false` → `Android14.callApk13()`:
  - Android 13 `RESPAWN` (xuất hiện).
  - A14, A15 chuyển `NON_PK`, hồi đầy HP, đặt `callApk13 = true` (đòn đó nhận 0).
- A13 `doneChatS` → A15 và A14 chuyển `PK_ALL`.
- **A13 không thể chết** khi A15 còn sống, hoặc khi A14 còn sống (đòn chí mạng nhận 0).
- Thoại A14 textS (người chơi): "Các ngươi là ai?"…; A13 textS: "Sôn..gôku", "Bọn ta là rôbốt sát thủ…"; A13 textE "Sô..Sông...gôku.....".
- Phần thưởng: Mẫu A cho cả 3. Không có `autoLeaveMap`.

### 5.7 King Kong, Pic, Poc

Nguồn: `boss/Android/KingKong.java`, `Pic.java`, `Poc.java`.

| Thuộc tính | King Kong (cha) | Pic | Poc |
|---|---|---|---|
| ID | -37 | -35 | -36 |
| HP / Dame | 20.000.000 / 12.000 | 10.000.000 / 17.022 | 15.000.000 / 18.000 |
| Skill | Masenko 7 (1s); Galick 7 (1s) | như KK | như KK |
| Xuất hiện | DEFAULT, nghỉ 10 phút, kèm `[PIC, POC]` | APPEAR_WITH_ANOTHER | APPEAR_WITH_ANOTHER |
| Nhiệm vụ | TASK_25_3 | TASK_25_2 | TASK_25_1 |

- Trái Đất; map 97 Thành phố phía bắc, 98 Ngọn núi phía bắc, 99 Thung lũng phía bắc.
- King Kong và Pic `doneChatS → AFK`; Poc đánh ngay.
- Poc chết → Pic `ACTIVE`; Pic chết → King Kong `ACTIVE`.
- Thoại Pic textS: "Chào! Có Gôku ở đây không?"…; textE "Pic tiêu rồi, tớ lên trước nhé!"; Poc: "Đừng tưởng ta đây là con gái mà dễ bắt nạt nhé"…; King Kong: "Mau đền mạng cho những người bạn của ta".
- Phần thưởng: Mẫu A. `autoLeaveMap` 15 phút không người.

### 5.8 Xên bọ hung (Cell) 3 form

Nguồn: `boss/Cell/XenBoHung.java`; dữ liệu `XEN_BO_HUNG_1/2/3`.

| Form | Tên | HP | Dame | Skill |
|---|---|---|---|---|
| 1 | Xên bọ hung | 50.000.000 | 20.000 | Kamejoko 7 (1s) ×2; Liên hoàn 7 (10s); Dịch chuyển tức thời 3 (10s) |
| 2 | Xên hoàn thiện | 100.000.000 | 25.000 | Kamejoko 7 (1s); Kamejoko 7 (5s); Liên hoàn 7 (10s) |
| 3 | Xên hoàn thiện | 150.000.000 | 30.000 | Kamejoko 7 (1s); Kamejoko 7 (5s); Dịch chuyển tức thời 7 (10s); Liên hoàn 7 (10s); Thôi miên 3 (100s) |

- ID -100, Xayda, map 100 Thị trấn Ginder, nghỉ 30 phút, 1 con.
- **Nhận sát thương:** `(dame / 2) − def`; khiên năng lượng: chia 4 (vỡ khiên nếu dame > hpMax).
- **Hấp thụ người chơi** (mỗi tick `active`): khi đã qua thời gian chờ 10–20 giây và trúng 1%: dịch chuyển tới 1 người chơi ngẫu nhiên, cộng `dameg += 5% dame người chơi`, `hpg += 2% HP người chơi`, `critg += 1`, hồi HP bằng HP người chơi, giết người chơi đó (sát thương xuyên giáp = hpMax), thông báo "Bạn vừa bị Xên bọ hung hấp thu!", thoại qua boss con thứ 2 và "Haha, ngọt lắm đấy <tên>..".
- Thoại form 1 textS: "Ta sẽ hấp thụ số 17 và 18 để đạt được dạng hoàn hảo!"; form 3 textE: "Oái.. không...", "Cơ thể hoàn hảo của ta!!".
- Phần thưởng mỗi form: Mẫu A + huy hiệu `TRUM_SAN_BOSS`; nhiệm vụ TASK_26_1/2/3.
- Không có `autoLeaveMap`.

### 5.9 Siêu Bọ Hung + 7 Xên con

Nguồn: `boss/Cell/SieuBoHung.java`, `XENCON1…7.java`; dữ liệu `SIEU_BO_HUNG_1/2`, `XEN_CON_1…7`.

| Form / Boss | Tên | ID | HP | Dame | Skill |
|---|---|---|---|---|---|
| Form 1 | Xên Hoàn Thiện | -101 | 150.000.000 | 35.000 | Kamejoko 7 (10s); Dịch chuyển tức thời 7 (20s); Galick 7 (1s); Thái Dương Hạ San 7 (50s) |
| Form 2 | Siêu Bọ Hung | -101 | 200.000.000 | 40.000 | Kamejoko 7 (5s); Dịch chuyển tức thời 3 (30s); Galick 7 (1s); Thôi miên 7 (30s) |
| Con | Xên con 1…7 | -102…-108 | 5.000.000 | 15.000 | Kamejoko 7 (5s); Galick 7 (1s) |

- Xayda, map 103 Võ đài Xên bọ hung, nghỉ 30 phút. Form 1 kèm 7 Xên con (`CALL_BY_ANOTHER`); form 2 **không** khai báo `bossesAppearTogether`. Không có textS/M/E.
- **Gọi Xên con:** đòn chí mạng đầu tiên (`callCellCon = false`) → nhận 0, chạy luồng riêng: `AFK`, `NON_PK`, hồi đầy HP, thoại "Hãy đấu với 7 đứa con của ta, chúng đều là siêu cao thủ" → (2s) "Cứ chưởng tiếp đi haha" → (2s) "Liệu mà giữ mạng đấy" → (2s) 7 Xên con `RESPAWN`.
- Xên con vào map cạnh cha (±100px). Khi 1 Xên con chết mà 6 con còn lại đều đã chết → Siêu Bọ Hung `ACTIVE`. Xên con `leaveMap` → rời map, `REST`.
- **Nhận sát thương:** `(dame / 3) − def`; khiên chia 4. Hết HP → **tự phát nổ** (`setBom`, xem 2.10) gây `hpMax` sát thương lên toàn khu, sau đó mới `die`.
- `autoLeaveMap`: NPC MC trong map đọc lần lượt "Thưa quý vị và các bạn, đây đúng là trận đấu trời long đất lở", "Vượt xa mọi dự đoán của chúng tôi", "Eo ơi toàn thân lão Xên bốc cháy kìa" (3s/câu, nghỉ 7s mỗi vòng), 2/3 cơ hội di chuyển mỗi 15s; nếu `currentLevel > 0` mà đang `AFK` thì chuyển `ACTIVE`; 15 phút không người → `leaveMapNew`.
- Phần thưởng Siêu Bọ Hung (Mẫu B): đồ Thần Linh **5%**; Vàng 100%; đồ cấp **30%**; Ngọc Rồng 2–7 sao (1–3 viên) **80%** — +5 Point nằm trong khối 80%; huy hiệu `TRUM_SAN_BOSS`; nhiệm vụ (form 2 → TASK_27_4).
- Phần thưởng Xên con: Mẫu A + huy hiệu; nhiệm vụ TASK_27_3.

### 5.10 Cooler

Nguồn: `boss/Cold/Cooler.java`; dữ liệu `COOLER`, `COOLER_2`.

| Form | Tên | HP | Dame | Skill |
|---|---|---|---|---|
| 1 | Cooler | 200.000.000 | 32.000 | Galick 1 (2s); Antomic 1 (6s) |
| 2 | Cooler 2 | 500.000.000 | 50.000 | Galick 1 (2s); Antomic 1 (6s) |

- ID -29, Xayda, map 110 Hang băng, nghỉ 30 phút, 1 con.
- Thoại textS: "Ta sẽ cho chúng bây biết sức mạnh thực sự của dân tộc Frost Demons"; textM 10 câu ("Tụi mày có giỏi thì xông vào cứu hắn đi", "Trận địa pháo mini"…); form 1 textE "Nãy giờ ta chưa thèm tung hết sức đâu", "Biến hình, hây aaaa..."; form 2 textE "Mọi chuyện chưa kết thúc đâu".
- **Nhận sát thương:** né cố định 1% (`isTrue(10,1000)`), `dame − def`, không giới hạn.
- Phần thưởng mỗi form (Mẫu B, không gọi nhiệm vụ): +5 Point; huy hiệu `TRUM_SAN_BOSS`; đồ Thần Linh 5%; Vàng 100%; đồ cấp 5%; Ngọc Rồng 2–7 sao (1–3) 80%.
- `autoLeaveMap` 15 phút không người.

### 5.11 Black Goku

Nguồn: `boss/Black_Goku/BlackGoku.java`; dữ liệu `BLACK_GOKU`, `SUPER_BLACK_GOKU`.

| Form | Tên | HP | Dame | Skill |
|---|---|---|---|---|
| 1 | Black Goku | 500.000.000 | 50.000 | Kamejoko 7 (0,1s); Tái tạo năng lượng 7 (1000s); Khiên năng lượng 7 (300s); Galick 7 (0,1s) |
| 2 | Super Black Goku | 2.000.000.000 | 100.000 | Thái Dương Hạ San 7 (30s); Tái tạo năng lượng 7 (300s); Khiên năng lượng 7 (300s); Kamejoko 7 (0,1s); Galick 7 (0,1s) |

- ID -203, Trái Đất; map 102 Nhà Bunma, 92 Thành phố phía đông, 93 Thành phố phía nam, 94 Đảo Balê, 96 Cao nguyên, 97 Thành phố phía bắc, 98 Ngọn núi phía bắc, 99 Thung lũng phía bắc, 100 Thị trấn Ginder. Nghỉ 5 phút, **2 con**, khu ≥ 2, có thông báo.
- Tên khi vào map: `"<tên form> <1..100>"`.
- Thoại form 1 textS: "Ta là Sôn Gô Ku", "Cơ thể này,sức mạnh này", "Ta khá thích việc loại bỏ các ngươi"; textE "Biến hình! Super Saiyan Rose"; form 2 textM "Ta sẽ thống trị vũ trụ"…
- **Nhận sát thương:** form 2 chia đôi trước; `(dame − rand(0..100.000)) − def`; khiên → 1.
- **Di chuyển riêng:** > 450px thì bay tới sát; 100–450px tiến 50–100px; ≤ 100px mới đánh (30% lùi/nhích ≤50px).
- `autoLeaveMap`: không có người trong `timeLeaveMap` (vào map: 10–15 phút; mỗi lần có người đặt lại 5–15 phút) → 50% `leaveMap()` (ở form 1 sẽ **biến thành Super Black Goku** tại chỗ), 50% `leaveMapNew()`.
- Phần thưởng mỗi form (Mẫu B): huy hiệu `TRUM_SAN_BOSS`; đồ Thần Linh 5%; Vàng 100%; đồ cấp 5%; **10%** 1–3 viên trong {Ngọc Rồng 2–7 sao, Nhẫn thời không sai lệch (992)}; nhiệm vụ; +5 Point.

### 5.12 Cumber

Nguồn: `boss/cumber/Cumber.java`; dữ liệu `CUMBER`, `SUPER_CUMBER`.

| Form | Tên | HP | Dame | Skill |
|---|---|---|---|---|
| 1 | Cumber | 500.000.000 | 50.000 | Kamejoko 7 (0,1s); Tái tạo năng lượng 7 (300s); Khiên năng lượng 7 (300s); Galick 7 (0,1s) |
| 2 | Super Cumber | 2.000.000.000 | 100.000 | Thái Dương Hạ San 7 (300s); Tái tạo năng lượng 7 (300s); Khiên năng lượng 7 (300s); Kamejoko 7 (0,1s); Galick 7 (0,1s) |

- ID -203999, Trái Đất, map 155 Hành tinh ngục tù, nghỉ 5 phút, 1 con, khu ≥ 2.
- Code giống hệt Black Goku (giảm sát thương, di chuyển, autoLeaveMap, phần thưởng Mẫu B với cùng tỉ lệ). Thoại giống Black Goku, textE form 1 "Biến hình! Super Saiyan SSJ".

### 5.13 Baby

Nguồn: `boss/Baby/Baby.java`; dữ liệu `BABY`, `BABY_2`, `BABY_3`.

| Form | HP | Dame | Skill |
|---|---|---|---|
| 1 | 2.000.000.000 | 200.000 | Galick, Dragon, Demon, Masenko, Antomic, Kamejoko cấp 7 (0,1s); Liên hoàn 7 (10s); Super Kamejoko 7 (10s) |
| 2 | 2.000.000.000 | 250.000 | như form 1, Super Kamejoko hồi 20s |
| 3 | 2.000.000.000 | 30.000 | như form 2 |

- ID -925, Xayda, map 14 Làng Kakarot, nghỉ 15 phút, **2 con**. Có thông báo, spawn khu theo quy tắc mặc định.
- Thoại form 1 textS: "Ta sẽ kí sinh vào người của vegeta để đạt được dạng hoàn hảo!"; textM 15 câu; textE "Khốn kiếp, vegeta.. hắn bị baby kí sinh rồi!!"; form 3 textE "Cơ thể hoàn hảo của ta!!"…
- **Nhận sát thương:** `(dame × 0,7 / 2) − def`; khiên chia 4.
- Không có `autoLeaveMap` (ở lại map tới khi chết).
- Phần thưởng mỗi form (Mẫu B, **không** cộng Point, **không** gọi nhiệm vụ):

| Nội dung | Tỉ lệ | SL |
|---|---|---|
| Đồ Thần Linh | 10% | 1 |
| `plKill.bossBabyDefeatParticipationCount++` | 100% | — |
| 1 trong: Cải trang baby (1785), Cải trang vegeta baby (1786), Cải trang baby khỉ (1788) — options: Sức đánh +30–40%, HP +30–40%, KI +30–40%, Giảm 10–20% sát thương, +10–20% sức đánh chí mạng, Tấn công +10–20% lên Boss, Không thể giao dịch, Hạn sử dụng 2–5 ngày | 1% | 1 |
| Vàng (190) | 100% | 20.000–30.000 |
| Đồ cấp | 5% | 1 |
| Ngọc Rồng 2–7 sao hoặc Nhẫn thời không sai lệch (992) | 10% | 1–3 |

### 5.14 Bojack và đồng bọn

Nguồn: `boss/trai_dat/BOJACK.java`, `BUJIN.java`, `KOGU.java`, `ZANGYA.java`, `BIDO.java`; dữ liệu `BOJACK`, `SUPER_BOJACK`, `BUJIN`, `KOGU`, `ZANGYA`, `BIDO`.

| Boss | ID | HP | Dame | Skill | Cải trang rơi |
|---|---|---|---|---|---|
| Bojack (form 1) | -320 | 100.000.000 | 300.000 | Tái tạo năng lượng 7 (100s); Trói 7 (120s); Masenko 7 (1s); Galick 7 (1s) | 427 (thành Bojack) |
| Siêu Bojack (form 2) | -320 | 150.000.000 | 300.000 | Tái tạo năng lượng 7 (100s); Thôi miên 7 (100s); Khiên năng lượng 7 (100s); Galick 7 (1s) | 427 |
| Bujin | -316 | 20.000.000 | 170.000 | Demon 7 (1s); Masenko 7 (1s) | 423 (thành Bujin) |
| Kogu | -317 | 40.000.000 | 180.000 | Tái tạo NL 7 (100s); Dragon 7 (1s); Trói 4 (50s); Antomic 4 (1s) | 424 (thành Kogu) |
| Zangya | -318 | 60.000.000 | 207.200 | Tái tạo NL 7 (100s); Galick 7 (1s); Trói 5 (50s); Antomic 3 (3s) | 425 (thành Zangya) |
| Bido | -319 | 80.000.000 | 250.200 | Tái tạo NL 7 (100s); Dragon 7 (1s); Kamejoko 4 (10s) | 426 (thành Bido) |

- Trái Đất; map 3 Rừng nấm, 4 Rừng xương, 5 Đảo Kamê, 6 Đông Karin, 27 Rừng Bamboo, 28 Rừng dương xỉ, 29 Nam Kamê, 30 Đảo Bulông. Bojack nghỉ **15 phút**, form 1 kèm `[BUJIN, KOGU, BIDO, ZANGYA]` (APPEAR_WITH_ANOTHER). Khu ≥ 2.
- Bojack form 1 `doneChatS → AFK`. Mỗi đồng bọn chết (`doneChatE`) kiểm tra 3 đồng bọn còn lại; nếu đều đã chết → Bojack `ACTIVE`. Form 2 không AFK.
- Thoại: Bojack "Hahaha"; Kogu/Zangya textM "Trói", textE "Cứu"; Bujin/Bido "Oải rồi hả?", "Cay quá!".
- Không override `injured`.
- Phần thưởng (Mẫu C, không gọi nhiệm vụ): Ngọc (77) mỗi cụm **5–20** (4 vòng lặp), cải trang (bảng trên), Ngọc Rồng 6 sao, 7 sao, +5 Point.
- `autoLeaveMap` 15 phút không người.

### 5.15 Siêu Bojack (boss độc lập)

Nguồn: `boss/trai_dat/SUPER_BOJACK.java`; dữ liệu `SUPER_BOJACK_2`.

| Thuộc tính | Giá trị |
|---|---|
| ID | -321 |
| Hành tinh | Trái Đất |
| HP / Dame | 500.000.000 / 300.000 |
| Skill | Tái tạo năng lượng 7 (100s); Trói 3 (60s); Kamejoko 7 (1s); Galick 7 (1s) |
| Map | như Bojack (3–6, 27–30) |
| Hồi sinh | 30 phút |
| Phần thưởng | Mẫu C: Ngọc cụm 1: 5–12, vòng 2: 5–13, vòng 3: 5–14, vòng 4: 5–15; Cải trang 428 (thành Super Bojack); Ngọc Rồng 6 & 7 sao; +5 Point |

### 5.16 Fide Vàng (Golden Frieza) 21h + Death Beam

Nguồn: `boss/Golden_fireza/GoldenFrieza.java`, `DeathBeam1…5.java`; dữ liệu `GOLDEN_FRIEZA`, `DEATH_BEAM`.

| Thuộc tính | Fide Vàng | Death Beam 1…5 |
|---|---|---|
| ID | -502 | -609 … -613 |
| Tên | "Fide Vàng <1..100>" | "$" (định dạng bằng số random) |
| Hành tinh | Xayda | Xayda |
| HP / Dame | 1.000.000.000 / 100.000 | 500 / 1.000 |
| Skill | Tái tạo năng lượng 1 (120s); Galick 7 (1s); Kamejoko cấp 1–7; Masenko cấp 1–7; Antomic cấp 1–7 (1s) | không |
| Map | 6 Đông Karin | theo Fide Vàng |
| Hồi sinh | 5 phút, **chỉ vào map trong 21:00–21:59** | CALL_BY_ANOTHER |
| Manager | BossManager | SkillSummonedManager |

Cơ chế Fide Vàng:
- `joinMap`: ngoài khung 21h → quay lại `REST`. Vào map: **giết toàn bộ quái** trong khu (99.999.999 sát thương), đặt `zone.isGoldenFriezaAlive = true`.
- `autoLeaveMap`: hết khung 21h → `leaveMap` (đặt `isGoldenFriezaAlive = false`, `REST`).
- **Nhận sát thương:** `dame − def`; khiên → 1; **tối đa 50.000.000/đòn**.
- **Chu kỳ tấn công:** mỗi 5–10 giây random 1 trạng thái:
  - `0` — **Bom:** gửi hiệu ứng gồng, sau 2,5 giây gây **2.100.000.000** sát thương xuyên giáp cho mọi người chơi trong khu (trừ map offline).
  - `1` — **Gọi Death Beam:** 5 Death Beam đang `REST` chuyển `RESPAWN`; khi cả 5 đã quay về `REST` thì chuyển sang trạng thái `2` trong 30 giây.
  - `2` — đánh thường như Boss mặc định.
- Phần thưởng: +5 Point; **Cải trang Fide vàng (629)** rơi cho người kết liễu, options: Không thể giao dịch (30,1), Sức đánh +20%, HP +20%, KI +20%, Hạn sử dụng 20 ngày. (Không gọi nhiệm vụ.)

Cơ chế Death Beam:
- Vào cạnh Fide Vàng (±100px, y = 300), khóa 1 mục tiêu, trạng thái `ACTIVE`.
- Mỗi tick bay theo mục tiêu 30px/lần; khi lệch < 5px → gây **2.100.000.000** sát thương xuyên giáp lên mục tiêu (1 lần), rồi bay lên.
- Sống ~14,6 / 14,7 / 14,8 / 14,9 / 15 giây (Beam 1→5), sau đó bay lên mỗi 0,5s và rời map khi `y < 0`.
- Mục tiêu chết/rời khu → `AFK`, mỗi 3s có 50% chọn mục tiêu mới.
- **Bất tử** (`injured` luôn 0). Có hàm `reward` giống Fide Vàng nhưng không thể kích hoạt.

### 5.17 Broly / Super Broly

Nguồn: `boss/Broly/Broly.java`, `SuperBroly.java`; `event_list/Default.java`.

| Thuộc tính | Broly | Super Broly |
|---|---|---|
| ID | -1822 | -82282 |
| Hành tinh | Xayda | Xayda |
| HP gốc (BossData) | 500 | 1.000 |
| HP thực khi vào map | random 500–100.000 | random 1.500.000–16.070.777 |
| Dame khi vào map | HP/100 | HP/100 |
| Chí mạng | random 0–49 | random 0–49 |
| Skill | Tái tạo năng lượng cấp 1–7; Dragon, Demon, Galick, Kamejoko, Masenko, Antomic cấp 1–7 | như Broly |
| Map | 5 Đảo Kamê, 13 Đảo Guru, 20 Vách núi đen, 27 Rừng Bamboo, 28 Rừng dương xỉ, 29 Nam Kamê, 30 Đảo Bulông, 31 Núi hoa vàng, 32 Núi hoa tím, 33 Nam Guru, 34 Đông Nam Guru, 35 Rừng cọ, 36 Rừng đá, 37 Thung lũng đen, 38 Bờ vực đen | xuất hiện tại vị trí Broly |
| Hồi sinh | 600 giây | 1 giây (nhưng bị hủy khi rời map) |
| Số lượng | 30 (sự kiện `Default`) | sinh động |
| Manager | BrolyManager | BrolyManager |
| Thoại | textM: "Haha! ta sẽ giết hết các ngươi", "Sức mạnh của ta là tuyệt đối", "Vào hết đây!!!"; textE "Các ngươi giỏi lắm. Ta sẽ quay lại." | như Broly |

Cơ chế:
- Tên "Broly <10..99>" / "Super Broly <10..99>".
- Broly chọn khu random từ 2 → cuối, bỏ qua khu có boss; hết khu → `DIE`. Không gửi thông báo xuất hiện (Super Broly có thông báo).
- Tấn công chỉ dùng skill đánh (index ≥ 7); 1% mỗi đòn dùng Tái tạo năng lượng + **tăng chỉ số**.
- Bị đánh: 10% (Broly) / 3,3% (Super Broly) dùng Tái tạo năng lượng + tăng chỉ số. **Tăng chỉ số:** `hpMax += hpMax / rand(10..100)` (Super Broly: `rand(80..100)`), tối đa 16.070.777; `dame = hpMax / 10`.
- **Giới hạn sát thương nhận:** `dame − def`, tối đa `hpMax/100` mỗi đòn (không áp dụng khi kẻ tấn công dùng Tự phát nổ hoặc đòn xuyên giáp).
- Broly `die()` chỉ `DIE` (không thưởng, không thông báo); `leaveMap()` **luôn tạo Super Broly** ở đúng khu/tọa độ. Ngoài ra `active()` gọi `leaveMap` nếu `hpMax == 1.500.000`.
- Super Broly vào map tự tạo đệ tử thường cho bản thân; `autoLeaveMap` 5 phút không người → rời map, xóa khỏi BrolyManager, `dispose`.
- **Phần thưởng Super Broly:** nếu người kết liễu **chưa có đệ tử** → `PetService.createNormalPet(plKill)` (nhận đệ tử). Không có item.

### 5.18 Tàu Pảy Pảy (Đông Nam Karin)

Nguồn: `boss/Tau_PayPay/TaoPaiPai.java`; dữ liệu `TAU_PAY_PAY_DONG_NAM_KARIN`.

| Thuộc tính | Giá trị |
|---|---|
| ID | `Util.randomBossId()` (random -1.000.000 … -100.000) |
| Hành tinh | Trái Đất |
| HP / Dame | 10.000 / 100 |
| Skill | Kamejoko 7 (5s); Galick 7 (1s) |
| Map | 111 Đông Nam Karin — mỗi khu 1 con (`Map.initBoss`) |
| Hồi sinh | 1 phút |
| Manager | FinalBossManager |
| Thông báo | không (map 111 bị loại trừ) |

- **Nhận sát thương:** `dame − def`; khiên → 1; **tối đa 100/đòn**; `nPoint.dame = dame / rand(500..1000)`.
- Mỗi đòn trúng cộng **TN/SM** cho người đánh: `dame × rand(20..50)`, tối đa ~10.000.000; nếu sức mạnh người đánh ≥ 1.500.000 thì chỉ cộng 0.
- Né: `Util.isTrue(tlNeDon, 1)` (xem ghi chú).
- Phần thưởng: mặc định (chỉ kiểm tra nhiệm vụ).

### 5.19 Hành tinh Yardart (Tập sự / Tân binh / Chiến binh / Đội trưởng)

Nguồn: `boss/yardrat/Yardart.java` (abstract) và 18 class con; dữ liệu `TAP_SU_*`, `TAN_BINH_*`, `CHIEN_BINH_*`, `DOI_TRUONG_5`.

Skill chung (tất cả): Dragon 1, Demon 1, Galick 1, Masenko 1, Antomic 1, Kamejoko 1, Liên hoàn 1 (hồi 0,5–1s), Dịch chuyển tức thời 7 (30s). Hành tinh Trái Đất. textM: "Khí công pháo".

| Map | Boss cha (DEFAULT, nghỉ 1s) | HP cha | Boss con (APPEAR_WITH_ANOTHER) | HP con | Hồi HP (chu kỳ) | Tỉ lệ thưởng |
|---|---|---|---|---|---|---|
| 131 Hành Tinh Yardart | Tân binh-5 (-327) | 450.000 | Tập sự-0…4 (-322…-326) | 350.000 | con 30s / cha 25s | con 1/5, cha 1/4 |
| 132 Hành Tinh Yardart 2 | Chiến binh-5 (-333) | 500.000 | Tân binh-0…4 (-328…-332) | 450.000 | con 25s / cha 20s | con 1/4, cha 1/3 |
| 133 Hành Tinh Yardart 3 | Đội trưởng-5 (-339) | 1.000.000 | Chiến binh-0…4 (-334…-338) | 500.000 | con 20s / cha 15s | con 1/3, cha 1/2 |

Dame gốc tất cả 10.000 nhưng bị ghi đè khi đánh (xem dưới). Mỗi boss có vùng tuần tra riêng `x → x2` (y = 456): vị trí 0: 170–240; 1: 376–446; 2: 582–652; 3: 787–857; 4: 993–1063; 5: 1199–1269.

Cơ chế:
- `Map.initBoss` tạo 1 nhóm cho **mỗi khu** của map 131/132/133, manager `YardartManager`. Không có thông báo.
- **Không bao giờ chết:** khi đòn đánh ≥ HP còn lại → hồi đầy HP/MP (`hsChar`), đặt HP = 1 (rồi được hồi), 10% gửi lại info, và **trao thưởng** cho người đánh.
- **Nhận sát thương:** né 10%; tối đa `hpMax/100` mỗi đòn (không xuyên giáp).
- **Hồi HP:** mỗi chu kỳ `timeHoiHP`, 50% hồi 1–5% hpMax. Khi không đánh ai: 10% mỗi tick hồi đầy HP.
- **Tấn công:** chỉ đánh người chơi trong phạm vi `range2 = 150px` (và gần vùng tuần tra hoặc 80% ngẫu nhiên). Dame mỗi đòn = `hpMax người chơi × rand(1..3)% / rand(10..30)`; nếu chọn Dịch chuyển tức thời: 75% đổi sang skill đầu, 25% dame = `hpMax người chơi × rand(5..10)%`.
- Không có mục tiêu: đi qua lại giữa `x` và `x2` mỗi 1,5 giây.
- **Phần thưởng** (`reward`): trúng `1/rewardRatio` → rơi **Bí kiếp (590)** tại chỗ người chơi, option "Số lượng 1" (31,1), chủ = người đánh.

---

## 6. Mabư 12h (Cổng phi thuyền → Phòng chỉ huy)

Nguồn: `boss/MajinBuu_12h/*.java`; dữ liệu `DRABURA`, `BUI_BUI`, `BUI_BUI_2`, `YACON`, `DRABURA_2`, `GOKU`, `CADIC`, `MABU_12H`, `DRABURA_3`. Tất cả `BossType.FINAL` (FinalBossManager), tạo bởi `Map.initBoss` cho **mỗi khu**, vào map theo `zoneFinal`. Không có thông báo xuất hiện (map Mabư bị loại trừ) nhưng có thông báo tiêu diệt.

### 6.0 Phần thưởng chung nhóm Mabư 12h (Mẫu B biến thể)

| Nội dung | Tỉ lệ | SL |
|---|---|---|
| +5 Point | 100% | 5 |
| Đồ Thần Linh | 1% | 1 |
| Vàng (190) | 100% | 20.000–30.000 |
| Đồ cấp (sao pha lê) | 1% | 1 |
| Ngọc Rồng 2–7 sao | 10% | 1–3 |
| Điểm Mabư `fightMabu.changePoint` | 100% | +10 (Drabura 3: +20; Mabư: +25) |
| Kiểm tra nhiệm vụ | 100% | — |

Ngoài ra khi người chơi đánh trúng (Drabura, Bui Bui, Bui Bui 2, Ya côn, Drabura 2, Mabư): 20% `fightMabu.changePercentPoint(+1)`; Gôku/Ca Đít: 20% `changePoint(+1)`.

**Hồi sinh kiểu AFK:** Drabura, Bui Bui, Bui Bui 2, Ya côn, Gôku, Ca Đít override `die()` → thưởng + thông báo rồi chuyển `AFK` (không `DIE`). Trong `AFK`: mỗi 10–15 giây chat "Đừng vội mừng, ta sẽ hồi sinh và thịt hết bọn mi"; sau **60 giây** hồi đầy HP/MP và quay lại `CHAT_S` → đánh tiếp.

### 6.1 Bảng boss

| Boss | ID | Map | HP | Dame | Skill | Né | Giới hạn / miễn nhiễm |
|---|---|---|---|---|---|---|---|
| Drabura | -233 | 114 Cổng phi thuyền | 20.000.000 | 10.000 | Galick 7 (1s) | 10% | tối đa 20M/đòn; miễn Tự phát nổ |
| Bui Bui | -234 | 115 Phòng chờ | 40.000.000 | 200.000 | Galick 7 (10s) | 20% | miễn Kamejoko/Masenko/Antomic/Liên hoàn |
| Bui Bui | -238 | 117 Cửa Ải 1 | 40.000.000 | 200.000 | Galick 7 (10s) | 20% | như trên |
| Ya côn | -235 | 118 Cửa Ải 2 | 50.000.000 | 200.000 | Galick 7 (0,1s) | 20% | như trên; `dame − def`; khiên → 1 |
| Drabura (2) | -237 | 119 Cửa Ải 3 | 20.000.000 | 200.000 | Galick 7 (1s) | 20% | miễn chưởng + Liên hoàn; tối đa 20M |
| Gôku | -341 | 119 (khu của Drabura 2) | 60.000.000 (vào map chia 4 = 15M) | 1.000 | Galick 7 (1s); Kamejoko 7 (1s); Tái tạo NL 7 (1000s); Thái Dương Hạ San 1 (60s) | xem ghi chú | tối đa 20M |
| Ca Đít | -342 | 119 | 60.000.000 (/4) | 1.000 | Galick 7 (1s); Antomic 7 (1s); Tái tạo NL 7 (1000s) | xem ghi chú | tối đa 10M |
| Mabư | -236 | 120 Phòng chỉ huy | 100.000.000 | 10.000 | Tái tạo NL 3 (1200s); Galick 7 (1s) | 20% | tối đa 50M ± 10.000 |
| Drabura (3) | -343 | 120 (khu Mabư) | 20.000.000 | 100.000 | Galick 7 (1s); Tái tạo NL 7 (10.000s) | tlNeDon | tối đa 20M |

Hồi sinh (secondsRest): Drabura, Bui Bui, Bui Bui 2, Ya côn, Mabư = 60 giây; Drabura 2 = 5 phút.

### 6.2 Cơ chế từng boss

- **Drabura (114):** vào map thẳng `ACTIVE`, cờ PK 10. Chỉ đánh người không tàng hình và khác cờ. Mỗi 10 giây: 10% mỗi người chơi bị **hóa đá 22 giây** (chat "phẹt"). Không có mục tiêu thì đi lang thang.
- **Bui Bui (115):** chỉ đánh khi `PK_ALL`, hồi sinh AFK 60s. textM "Hãy xem đây nhóc".
- **Bui Bui 2 (117):** mỗi 10 giây 50% mỗi người chơi bị **làm chậm 5 giây**; `chatM` đọc tuần tự 3 câu ("Trọng lực bây giờ đã tăng gấp 10 lần"…) cách 3s, nghỉ 10s.
- **Ya côn (118):** mỗi 30 giây 10% "tàng hình" (người chơi chat "Mi đâu rồi"/"Đồ ăn gian!"); trong 10 giây sau đó boss đứng ở y = 10.000 (không nhìn thấy) và **chí mạng 100%**, ngoài thời gian đó chí mạng 10%.
- **Drabura 2 (119):**
  - Vào map `CHAT_S` ("Ta đã trở lại, lợi hại gấp hai, hahaha").
  - Đòn chí mạng → nhận 0, `AFK` → `afk()` chuyển `NON_PK` + `DIE` → textE ("Hêhê..ta chẳng cần tốn sức đánh với các ngươi nữa", "Mà ta sẽ để cho các ngươi tự thanh toán lẫn nhau, xin chào") → rời map → `REST`.
  - Trong `REST`, sau 5 giây gọi **Gôku và Ca Đít** `RESPAWN` (1 lần/lượt). Drabura 2 xuất hiện lại sau 5 phút.
  - `autoLeaveMap`: 250 giây sau khi vào map thì rời map.
  - Không có phần thưởng thực tế vì không đi qua `die()` (xem ghi chú).
- **Gôku / Ca Đít (119):** vào tại khu `zoneFinal` của Drabura 2, HP chia 4, Gôku cờ 9, Ca Đít cờ 10 (đánh nhau và đánh người chơi khác cờ, không tàng hình). `doneChatS` dùng skill index 2 (Tái tạo năng lượng). Thoại tuần tự (Gôku: "Tỉnh lại đi Cađíc!", "Đừng lùa gà nữa"…; Ca Đít: "Không, còn điện thoại mới, xe mới, nhà mới thì sao", "Chúng ta sẽ 1 mất 1 còn!"…). 248,5 giây sau khi vào: dùng Tái tạo năng lượng (MP tạm 1 tỉ); 250 giây: rời map. Chết → AFK 60s rồi hồi đầy máu.
- **Mabư (120):**
  - Trong `REST` gửi thanh tiến trình `Service.SendMabu(zoneFinal, percent)` với `percent = thời gian đã nghỉ / (secondsRest − 3)`; sau 60 giây xuất hiện, NPC Babiday chat "Mabư ! Hãy theo lệnh ta, giết hết bọn chúng đi".
  - Mỗi 30 giây mỗi người chơi: 10% **hóa đá 22 giây**, hoặc (nếu không) 20% **biến Sôcôla 30 giây** (chat "Úm ba la xì bùa").
  - `autoLeaveMap` rỗng. Chết → `DIE` → `CHAT_E` → `leaveMap` → `REST` và gọi **Drabura 3** `RESPAWN`.
- **Drabura 3 (120):** vào khu của Mabư, cờ 10, mỗi 10 giây 10% hóa đá 22s. Rời map sau 60 giây kể từ lúc vào. Chết → thưởng (+20 điểm Mabư) → `AFK` (không tự hồi sinh; rời map khi hết 60 giây).

---

## 7. Mabư 14h (Cổng phi thuyền / Bụng Mabư)

Nguồn: `boss/MajinBuu_14h/Mabu2H.java`, `SuperBu.java`; dữ liệu `MABU`, `SUPER_BU`, `BU_TENK`, `BU_HAN`, `KID_BU`, `SUPER_BU_BUNG`. `BossType.FINAL`, tạo mỗi khu bởi `Map.initBoss`.

### 7.1 Mabư 14h — 5 form (map 127 Cổng phi thuyền)

| Form | Tên | HP | Dame | Skill |
|---|---|---|---|---|
| 1 | Mabư mập | 50.000.000 | 500.000 | Kamejoko 3 (5s); Dragon 7 (1s) |
| 2 | Super Bư | 60.000.000 | 500.000 | như trên |
| 3 | Bư Tênk | 80.000.000 | 500.000 | như trên |
| 4 | Bư Han | 100.000.000 | 500.000 | như trên |
| 5 | Kid Bư | 150.000.000 | 500.000 | như trên |

- ID -214, Xayda. Tất cả form khai báo `REST_10_M` và `DEFAULT_APPEAR`; do cơ chế `leaveMap`, chết ở form 1–4 sẽ chuyển ngay sang form kế tiếp tại chỗ; chết ở form 5 thì nghỉ 10 phút rồi quay về Mabư mập.
- Thoại: textM "Khí công pháo" (+ "Úm ba la xì bùa" form 1, "Ui da đau bụng quá" form 3); textE "Biến hình".
- Vào map thẳng `ACTIVE`.
- Mỗi 10 giây: 20% mỗi người chơi trong khu bị **ăn** (`isMabuHold = true`, `sendMabuEat`, thêm vào `maBuEat`, chat "Măm măm"). Riêng form 1 thêm 20% mỗi người **biến Sôcôla 30 giây**.
- Form 2–5: mỗi 5–10 giây dùng đòn đặc biệt `sendMabuAttackSkill` thay cho đòn thường.
- **Nhận sát thương:** né 10%; tối đa 30M ± 10.000/đòn; **form cuối (Kid Bư) chỉ có thể nhận đòn kết liễu bằng Quả cầu kênh khi** (skill khác nếu đủ giết thì nhận 0).
- Khi chết: giết luôn Super Bư ở map 128 cùng khu; đưa mọi người chơi đang ở map 128 trong danh sách bị ăn về map 127 (cùng khu, y = 312); thưởng + thông báo.
- Phần thưởng: +5 Point, kiểm tra nhiệm vụ (không có item).

### 7.2 Super Bư (map 128 Bụng Mabư)

| Thuộc tính | Giá trị |
|---|---|
| ID | -348 |
| HP / Dame | 50.000.000 / 500.000 |
| Skill | Kamejoko 3 (5s); Dragon 7 (1s) |
| Hồi sinh | 10 giây |
| Thoại | textM "Khí công pháo" |

- Vào map thẳng `ACTIVE`; mỗi 5–10 giây dùng `sendMabuAttackSkill`.
- **Mọi sát thương nhận vào đồng thời được chuyển sang Mabư 14h** ở map 127 cùng khu (gọi `injured` của Mabư).
- Né 10%; tối đa 30M ± 10.000/đòn.
- Chết: đưa người chơi bị ăn ở map 128 về map 127; +5 Point, kiểm tra nhiệm vụ, thông báo.

---

## 8. Boss mini (Boss_mini)

Nguồn: `boss/Boss_mini/*.java`.

### 8.1 Sói hẹc quyn (mini)

| Thuộc tính | Giá trị |
|---|---|
| Class / ID | `Boss_mini/SoiHecQuyn.java` / `SOI_HEC_QUYN1` = -77 |
| Dữ liệu | `BossesData.SOI_HEC_QUYN`: Trái Đất, HP 10.000, dame 1.000, Kamejoko 7 (5s), Galick 7 (1s) |
| Map | MAP_THUONG_LON — chọn ngẫu nhiên 1 khu có ≤ 10 người và chưa có Sói; không có khu → `leaveMapNew` |
| Hồi sinh | 10 phút |
| Số lượng | 2 (`loadBoss`) |
| Rời map | sau 100–300 giây kể từ lúc vào (`leaveMapNew`) |

- **Bất tử** (`injured` luôn 0), không dùng skill (chỉ đi theo người chơi).
- **Cách "hạ":** người chơi dùng item **Cục xương (460)** trong khu có Sói (`UseItem.XuongCho`):
  - Nếu Sói đang "no" (vừa ăn trong 5 giây) → "Sói đã no rồi".
  - Ngược lại Sói chat "Ê, Cục xương ngon quá", rơi Cục xương (đã nhặt), cập nhật huy hiệu `KE_THAO_TUNG_SOI`, trừ 1 Cục xương, rồi:
    - 75%: nhận 1 Sao pha lê ngẫu nhiên (441 đỏ, 442 lam, 443 hồng, 444 tím, 445 cam, 446 vàng, 447 lục) kèm option id `95 + rand` (rand = vị trí 0–6 của viên sao), giá trị 3 nếu là Sao pha lê tím/cam (rand 3–4), còn lại giá trị 5.
    - 25%: nhận Phiếu giảm giá (459): "Giảm 80% khi mua Avatar hoặc Cải trang", "Hạn sử dụng 90 ngày", "PIN" random.
  - Sau 5 giây (Thread.sleep trong xử lý dùng item) xóa cục xương và Sói `leaveMapNew`.
- Có `reward()` (Hộp quà Goku Day 1591 + 1594, +5 Point, huy hiệu) nhưng không thể được gọi vì Sói không chết.

### 8.2 Ăn Trộm

| Thuộc tính | Giá trị |
|---|---|
| Class / ID | `Boss_mini/AnTrom.java` / -365 |
| Hành tinh | Trái Đất |
| HP / Dame | vào map: HP = random 0–99, dameg = HP/10 |
| Skill | Thái Dương Hạ San 3 (50s) |
| Map | MAP_THUONG_LON, luôn khu 0 |
| Hồi sinh | 600 giây |
| Số lượng | 5 |

- Tên "Ăn Trộm <1..49>". Không thông báo.
- **Nhận sát thương:** luôn 1; mỗi lần bị đánh dùng ngay 1 skill lên kẻ tấn công (Thái Dương Hạ San).
- **Trộm vàng:** khi đứng ≤ 40px cạnh người chơi, mỗi 0,5 giây (tổng trộm < 10 tỉ): nếu vàng người chơi ≥ 2.000.000 → trộm 200.000–1.000.000; (nhánh ≥ 1 tỉ không bao giờ tới); ≥ 1.000.000 → 1.000–2.000. Chat "Haha đã trộm được <tổng> Vàng", hiệu ứng nhặt vàng.
- `active`: 15 phút sau khi vào → `LEAVE_MAP`.
- **Chết:** cập nhật huy hiệu `BI_MOC_SACH_TUI`, rồi thưởng **nếu đã trộm được vàng**:

| Nội dung | Tỉ lệ | Số lượng |
|---|---|---|
| Vàng (190) — 5 cụm | 100% | mỗi cụm = 80% tổng vàng đã trộm / 5 |
| Hộp quà Goku Day (1591) | 100% | 1 |
| Hộp quà Goku Day (1594) | 100% | 1 |
| Huy hiệu `BI_MOC_SACH_TUI` (lần 2), +5 Point | 100% | — |

### 8.3 Ở Dơ (mini)

| Thuộc tính | Giá trị |
|---|---|
| Class / ID | `Boss_mini/Odo.java` / `O_DO1` = -78 |
| Tên | "Ở Dơ <1..49>" (random 1 lần khi khởi tạo) |
| HP / Dame | 500.000 / 1.000 |
| Skill | Tái tạo năng lượng 1 |
| Map | MAP_THUONG_LON, khu 0 |
| Hồi sinh | 600.000 giây (~6,9 ngày) |
| Số lượng | 5 |

- **Nhận sát thương:** luôn **50.000** mỗi đòn (10 đòn là chết).
- **Hôi thối:** mỗi 3–5 giây khi tấn công, mọi người chơi trong 200px bị trừ 10% hpMax (không chết, tối thiểu còn 1 HP), boss chat "Bùm Bùm", người chơi chat câu ngẫu nhiên.
- Mỗi 30 giây hồi 10–20% hpMax, chat "Mùi Của Các Ngươi Thơm Quá!! HAHA".
- Rời map sau 15 phút kể từ khi vào (`active`).
- Phần thưởng: Hộp quà Goku Day (1591) ×1, (1594) ×1; huy hiệu `O_DO`; +5 Point.

### 8.4 Mặt Trời

| Thuộc tính | Giá trị |
|---|---|
| Class / ID | `Boss_mini/MatTroi.java` / dùng `BossID.Virut` = -79 |
| Hành tinh | Trái Đất |
| HP / Dame | 100 / 1 |
| Skill | Dragon 7 (1s) |
| Map | 5 Đảo Kamê, 7 Làng Mori, 0 Làng Aru, 14 Làng Kakarot — khu 0 |
| Hồi sinh | 600 giây |
| Số lượng | 20 |

- **Nhận sát thương:** luôn 1 (100 đòn).
- Tấn công mỗi 3 giây khi đứng ≤ 40px; 30% mỗi người chơi trong 200px bị **bỏng nhiệt**: hiện icon 12953 trong 60s, lưu hạn 30 giây, chat "…Đã bị bỏng nhiệt".
- Sau 15 phút: `LEAVE_MAP` và kiểm tra hiệu ứng — người chơi đã hết hạn bỏng có **80% bị giết**.
- Phần thưởng: huy hiệu `KOL`; **50%** Mặt trời tí hon (1562): Sức đánh +7–10%, HP +7–10%, KI +7–10%, Không thể giao dịch, Hạn sử dụng 2–5 ngày.

### 8.5 Virut (không được tạo)

`Boss_mini/Virut.java`, ID -79, HP 100, dame 10 (vào map dame 1), map 5/7/0/14 khu 0, hồi sinh 600s. Hiệu ứng "nhiễm" (icon 7143, hạn 300 giây; hết 15 phút → 80% giết người đã hết hạn). Nhận 1 sát thương/đòn. Thưởng Hộp quà Goku Day 1591 + 1594, +5 Point ("+5 Point sự kiện từ Virut"). **Không có chỗ nào gọi `createBoss(BossID.Virut)`** (id -79 trong `createBoss` tạo Virut, nhưng `loadBoss` chỉ tạo `MAT_TROI`).

### 8.6 Rồng Nhí (không được tạo)

`Boss_mini/RongNhi.java`, ID -386998, dữ liệu `RONG_NHI`: Trái Đất, HP 50.000.000, dame 1, Galick 5 (hồi 1.000.000.000ms), map MAP_0_20 và 24–37, nghỉ 1 phút, tắt thông báo, khu ≥ 2. Thoại "Tới giờ làm việc", "Ái chà chà". Thưởng: 20% → (20%: Trứng vàng rồng nhí 1821; 80%: Ngọc Rồng 5/6/7 sao) + 5 Point. `autoLeaveMap` 15 phút không người. **Không nằm trong `loadBoss` hay sự kiện nào.**

---

## 9. Ghi chú / điểm cần lưu ý

1. **Điều kiện thông báo tiêu diệt luôn đúng** — `Boss.die()` dùng `mapId != 140 || !isMapMaBu || !isMapDoanhTrai || !isMapBanDoKhoBau`; biểu thức `||` này gần như luôn `true`, nên nhánh `else` (không thông báo) không bao giờ chạy khi `plKill != null`.
2. **Manager sự kiện không có thread** — `TrungThuEventManager`, `HalloweenEventManager`, `ChristmasEventManager`, `HungVuongEventManager`, `LunarNewYearEventManager` không được `start` trong `ServerManager.run()`. Sự kiện Hùng Vương đang bật (`new HungVuong().init()`) tạo 10 Thủy Tinh (+10 Sơn Tinh) nhưng các boss này **không bao giờ được update** nên không xuất hiện.
3. **Dr.Kôrê hấp thụ chưởng không hoạt động** — hàm `injured(Player, int, boolean, boolean)` không override `injured(Player, long, ...)`.
4. **Trùng BossID:** `SOI_HEC_QUYN1 = SOI_HEC_QUYN = -77`; `O_DO1 = O_DO = -78`; `Virut = XINBATO = -79` và Mặt Trời cũng dùng -79; `HAKAI = DEATH_BEAM_5 = -613`; Rôbốt Vệ Sĩ dùng `ROBOT_VE_SI - id` = -8…-11 trùng `NINJA_AO_TIM1…3` (-9…-11). `BossManager.getBossById`, `checkBosses` tìm theo id nên có thể nhầm boss.
5. **Né đòn dùng mẫu số 1** — Tàu Pảy Pảy, Gôku, Ca Đít dùng `Util.isTrue(this.nPoint.tlNeDon, 1)`: nếu `tlNeDon ≥ 1` thì **luôn né**, nếu `tlNeDon = 0` thì không bao giờ né. Nhiều khả năng nhầm với `/1000`.
6. **Drabura 2 không trao thưởng** — đòn chí mạng chuyển `AFK` và đặt dame = 0, `afk()` chuyển thẳng `DIE`; `die()`/`reward()` không được gọi nên người chơi không nhận thưởng/nhiệm vụ TASK_28_1/28_5 từ Drabura 2.
7. **Broly luôn biến Super Broly khi rời map** — kể cả khi bị giết (`die → DIE → CHAT_E → LEAVE_MAP → leaveMap()` tạo `SuperBroly`). Điều kiện `hpMax == 1_500_000` trong `active()` hầu như không xảy ra (so sánh bằng tuyệt đối). Broly hết khu trống → `DIE` (cũng sinh Super Broly).
8. **Black Goku / Cumber rời map do vắng người có thể biến hình** — 50% nhánh `leaveMap()` khi đang form 1 sẽ respawn form 2 tại chỗ thay vì rời map.
9. **Siêu Bọ Hung form 2 có thể "hồi đầy máu" 1 lần** — `resetBase` đặt lại `callCellCon = false` mỗi form; ở form 2 đòn chí mạng đầu vẫn gọi `callCellCon()` (hồi máu, AFK) nhưng `bossAppearTogether[1]` null nên không gọi được Xên con; `autoLeaveMap` sẽ đưa boss về `ACTIVE`.
10. **Ở Dơ nghỉ 600.000 giây** — hằng truyền vào `BossData` là giây, tương đương ~6,9 ngày (các boss mini khác dùng 600).
11. **Ăn Trộm** — nhánh trộm khi vàng ≥ 1 tỉ nằm sau nhánh ≥ 2 triệu nên không thể tới; `hpMax = Util.nextInt(100)` có thể bằng 0; huy hiệu `BI_MOC_SACH_TUI` bị cộng 2 lần khi giết (trong `die` và `reward`).
12. **Sói hẹc quyn** — xử lý "Cục xương" gọi `Thread.sleep(5000)` trong luồng dùng item của người chơi (chặn luồng xử lý).
13. **Virut, Rồng Nhí** có code nhưng không được tạo ở đâu; `AnTromManager` không được dùng; `BossManager.ratioReward = 10` không được dùng; hằng `REST_24_H` không được dùng.
14. **Không có cơ chế top sát thương** — toàn bộ phần thưởng boss thế giới trao cho người kết liễu (item gắn chủ `plKill.id`). Riêng Dr Lychee/Hatchiyack/quà Noel rơi tự do (`-1`) — xem file 11b.
15. **HP/dame thực tế có thể lệch** so với `BossData` vì `nPoint.calPoint()` và một số boss ghi đè khi vào map (Broly, Ăn Trộm, Mặt Trời, Gôku/Ca Đít chia 4, Whis, Võ đài Hạt Mít...).
16. **Nhịp tick 1,5 giây** của `BossManager`/`FinalBossManager`/`YardartManager`/`BrolyManager` khiến các ngưỡng "mỗi 100ms" trong `attack()` thực tế là mỗi ~1,5 giây; boss ở manager 150ms (phó bản, đấu trường) phản ứng nhanh hơn ~10 lần.
17. **Baby không cộng Point và không gọi nhiệm vụ**; Cooler không gọi nhiệm vụ; nhóm Bojack/TDST Namek không gọi nhiệm vụ.
18. `Boss.injured` mặc định **không trừ giáp** (`def`) — các boss không override (Nappa, TDST, Fide, Android, Bojack…) nhận toàn bộ sát thương.
19. Map id 168 (dùng trong dữ liệu boss ĐHVT) không tồn tại trong `map_template`; boss ĐHVT thực tế vào khu của người chơi (xem 11b).
