# 10 — Rồng Thần & các hệ thống Ngọc Rồng

> Spec đối chiếu từ source: `SRC/src/nro/models/services/shenron/` (`SummonDragon.java`, `SummonDragonNamek.java`, `Shenron_Service.java`, `Shenron_Event.java`, `Shenron_Manager.java`), `services_dungeon/NgocRongNamecService.java`, `services_dungeon/BlackBallWarService.java`, `map/phoban/BlackBallWar.java`, `player/RewardBlackBall.java`, cùng các điểm gọi: `services_func/UseItem.java`, `npc/NpcFactory.java`, `npc_list/Dende.java`, `npc_list/RongOmega.java`, `npc_list/Rong1Sao.java`, `server/Client.java`, `map/service/ChangeMapService.java`, `player/NPoint.java`. Tên item/option tra từ DB `database team2026.sql`.

## Mục lục

1. [Tổng quan các loại rồng / ngọc](#1-tổng-quan)
2. [Rồng Thần Shenron (Ngọc Rồng 1–7 sao thường)](#2-rồng-thần-shenron)
3. [Rồng Băng (Ngọc rồng băng 925–931)](#3-rồng-băng)
4. [Rồng Thần Namếc & sự kiện đoạt Ngọc Rồng Namếc](#4-rồng-thần-namếc--ngọc-rồng-namếc)
5. [Ngọc Rồng Sao Đen (Black Ball War)](#5-ngọc-rồng-sao-đen)
6. [Code chết: `models/ShenronEvent` & `ShenronEventManager`](#6-code-chết)
7. [Bảng tra item / option](#7-bảng-tra-item--option)
8. [Ghi chú / điểm cần lưu ý](#8-ghi-chú--điểm-cần-lưu-ý)

---

## 1. Tổng quan

| Hệ thống | Ngọc cần | Nơi gọi | Class chính | Phạm vi | Thời gian chờ gọi lại | Thời gian ước |
|---|---|---|---|---|---|---|
| Rồng Thần Shenron | Ngọc Rồng 1–7 sao (id 14–20) | Map 0 Làng Aru, 7 Làng Mori, 14 Làng Kakarot | `SummonDragon` (singleton) | **Toàn server chỉ 1 rồng** | 10 phút (toàn server) | 5 phút |
| Rồng Băng | Ngọc rồng băng 1–7 sao (925–931) | Mọi map **trừ** 0, 7, 14 | `Shenron_Service` + `Shenron_Event` (mỗi người 1 instance) | Theo người chơi | 60 giây (theo người) | 60 giây (xem ghi chú: không có timeout thực tế) |
| Rồng Thần Namếc (Porunga) | 7 Ngọc Rồng Namếc (353–359) gom tại map 7 | NPC Dende, map 7 Làng Mori | `SummonDragonNamek` + `NgocRongNamecService` | Cả bang hội | Ngọc hóa đá 24 giờ | 5 phút |
| Ngọc Rồng Sao Đen | Giữ ngọc 372–378 | Map 85–91 (Hành tinh M-2 … Tigere) | `BlackBallWar`, `BlackBallWarService`, `RewardBlackBall` | Cả bang hội | Mỗi ngày 20h–21h | — (buff 22 giờ) |

Tất cả Ngọc Rồng thường & băng đều là `template.type == 12`; dùng item → `UseItem` case 12 → `controllerCallRongThan()` (`services_func/UseItem.java` dòng 1706–1723).

---

## 2. Rồng Thần Shenron

### 2.1 Mở menu gọi rồng

`UseItem.controllerCallRongThan`:

| Item dùng | Kết quả |
|---|---|
| 14 Ngọc Rồng 1 sao | `SummonDragon.openMenuSummonShenron(pl, 1)` |
| 15 Ngọc Rồng 2 sao | `openMenuSummonShenron(pl, 2)` |
| 16 Ngọc Rồng 3 sao | `openMenuSummonShenron(pl, 3)` |
| 17–20 (4–7 sao) | Menu "Bạn chỉ có thể gọi rồng từ ngọc 3 sao, 2 sao, 1 sao" (`TUTORIAL_SUMMON_DRAGON`) |

Menu (NPC Con mèo, `ConstNpc.SUMMON_SHENRON`): "Hướng dẫn thêm (mới)" · "Gọi Rồng Thần N Sao" (`NpcFactory.createNpcConMeo`, case `SUMMON_SHENRON`).

### 2.2 Điều kiện gọi (`SummonDragon.summonShenron`, dòng 145–186)

| # | Điều kiện | Thông báo khi sai |
|---|---|---|
| 1 | Đang ở map **0, 7 hoặc 14** (làng trước nhà 3 hành tinh) | "Chỉ được gọi rồng thần ở ngôi làng trước nhà" |
| 2 | Đủ ngọc (bảng 2.3) – `checkShenronBall` | "Bạn còn thiếu 1 viên ngọc rồng X sao" |
| 3 | Không có rồng nào đang xuất hiện trên **toàn server** (`isShenronAppear`) | "Không thể thực hiện" |
| 4 | Đã qua **600.000 ms (10 phút)** kể từ lần rồng rời đi gần nhất (`lastTimeShenronAppeared`, toàn server) | "Vui lòng đợi X giây nữa" |

### 2.3 Ngọc tiêu hao

| Loại rồng | Kiểm tra có trong túi | Trừ khỏi túi (mỗi loại 1 viên) |
|---|---|---|
| Rồng 1 sao | 2, 3, 4, 5, 6, 7 sao (**không** kiểm tra 1 sao – viên 1 sao là item đang được dùng) | 1, 2, 3, 4, 5, 6, 7 sao |
| Rồng 2 sao | 3, 4, 5, 6, 7 sao | 2, 3, 4, 5, 6, 7 sao |
| Rồng 3 sao | 4, 5, 6, 7 sao | 3, 4, 5, 6, 7 sao |

Ngọc bị trừ **ngay khi gọi**, dù có ước hay không.

### 2.4 Khi rồng xuất hiện

- Thông báo tới mọi người chơi khác (msg `-25`): "`<tên>` vừa gọi rồng thần tại `<map>` khu vực `<khu>`".
- Gửi msg `-83` (hiệu ứng rồng, `DRAGON_SHENRON = 0`) tới **toàn server**.
- Cập nhật nhiệm vụ danh hiệu `TRUM_UOC_RONG` +1.
- Thời gian ước: **300.000 ms (5 phút)** (`timeShenronWait`). Luồng `update` kiểm tra mỗi 1 giây; quá hạn → rồng bay đi ("Ta buồn ngủ quá rồi…").
- Người gọi mất kết nối: `Client.remove` bật `isPlayerDisconnect`; khi người đó vào lại **đúng khu có rồng**, rồng được gọi lại và hiện lại menu (thời gian 5 phút **không** được reset).

### 2.5 Danh sách điều ước

Lời rồng: "Ta sẽ ban cho người 1 điều ước, ngươi có 5 phút, hãy suy nghĩ thật kỹ trước khi quyết định". Mỗi điều ước có bước xác nhận "Ngươi có chắc muốn ước?" → [điều ước] / "Từ chối" (Từ chối → mở lại menu điều ước).

#### Rồng 1 sao – trang 1 (`SHENRON_1_1`)

| Nút | Hiển thị | Hiệu ứng thực tế (`confirmWish`) | Điều kiện / từ chối |
|---|---|---|---|
| 0 | Giàu có +2 Tỏi Vàng | `inventory.gold = 2.000.000.000` (**gán bằng**, không cộng) | — |
| 1 | Găng tay đang mang lên 1 cấp | Găng (ô body 2): option 72 +1 (hoặc thêm `72 = 1`); option 0 (Tấn công) +10% | Không đeo găng → chọn lại; cấp ≥ 7 → "đã đạt cấp tối đa", chọn lại |
| 2 | Chí mạng Gốc +2% | `nPoint.critdragon += 2` (cộng vào `crit` khi tính chỉ số) | Nếu `critdragon >= 9` → "Điều ước này đã quá sức với ta", chọn lại. Tối đa đạt 10 (0→2→4→6→8→10) |
| 3 | Thay Chiêu 2-3 Đệ tử | `pet.openSkill2()`; nếu đệ có chiêu 3 thì thêm `pet.openSkill3()` | Không có đệ tử / đệ chưa có chiêu 2 → chọn lại |
| 4 | Điều ước khác | Chuyển sang trang 2 | — |

#### Rồng 1 sao – trang 2 (`SHENRON_1_2`)

| Nút | Hiển thị | Hiệu ứng thực tế | Điều kiện |
|---|---|---|---|
| 0 | Đẹp trai nhất Vũ trụ | Nhận **Avatar VIP** theo hành tinh: TĐ 227, NM 228, XD 229; option 97 (Phản #% sát thương) 5–10, option 77 (HP+#%) 10–20 | Hành trang đầy → chọn lại |
| 1 | Giàu có +10K Ngọc | `gem += 10.000` | — |
| 2 | +200 Tr Sức mạnh và tiềm năng | `addSMTN(type 2, 200.000.000)` (qua giới hạn sức mạnh) | — |
| 3 | Găng tay đệ đang mang lên 1 cấp | Găng của đệ tử: 72 +1, option 0 +10% | Không có đệ / đệ không đeo găng / cấp ≥ 7 → chọn lại |
| 4 | Điều ước khác | Quay lại trang 1 | — |

#### Rồng 1 sao – trang 3 (`SHENRON_1_3`) – không truy cập được

Mảng `SHENRON_1_STAR_WISHES_3` = "Quần đang mang lên 1 cấp", "Quần đệ đang mang lên 1 cấp", "Điều ước khác", nhưng không nút nào mở trang này và `confirmWish` không có case `SHENRON_1_3`.

#### Rồng 2 sao (`SHENRON_2`)

| Nút | Hiển thị | Hiệu ứng thực tế |
|---|---|---|
| 0 | Giàu có +2K Ngọc | `gem += 2.000` |
| 1 | +20 Tr Sức mạnh và tiềm năng | `addSMTN(2, 20.000.000)` |
| 2 | Giàu có +200 Tr Vàng | Nếu vàng hiện có > 1.800.000.000 → vàng = `LIMIT_GOLD` (200.000.000.000); ngược lại `gold += 200.000.000` |

#### Rồng 3 sao (`SHENRON_3`)

| Nút | Hiển thị | Hiệu ứng thực tế |
|---|---|---|
| 0 | Giàu có +200 Ngọc | `gem += 200` |
| 1 | +2 Tr Sức mạnh và tiềm năng | `addSMTN(2, 2.000.000)` |
| 2 | Giàu có +20 Tr Vàng | Nếu vàng > 1.980.000.000 → vàng = 200.000.000.000; ngược lại `gold += 20.000.000` |

Sau khi ước thành công: "Điều ước của ngươi đã trở thành sự thật…", rồng biến mất (msg `-83` appear = 1), `lastTimeShenronAppeared = now` → bắt đầu 10 phút chờ toàn server.

### 2.6 Kỹ năng đệ tử khi đổi chiêu (`player/Pet.java`)

| Hàm | Ô chiêu | Tỉ lệ |
|---|---|---|
| `openSkill2` | chiêu 2 | Kamejoko 33% · Masenko 33% · Antomic 34%; roll lại cho tới khi **khác** chiêu hiện tại |
| `openSkill3` | chiêu 3 | Thái dương hạ san 30% · Tái tạo năng lượng 40% · Kaioken 30% (có thể trùng chiêu cũ) |
| `openSkill4` | chiêu 4 | Biến khỉ 10% · Đẻ trứng 70% · Khiên năng lượng 20% (có thể trùng) |

### 2.7 Hướng dẫn trong game (`SUMMON_SHENRON_TUTORIAL`)

Văn bản hướng dẫn ghi: rồng 3 sao "Capsule 3 sao, hoặc 2 triệu sức mạnh, hoặc 200k vàng"; rồng 2 sao "Capsule 2 sao, hoặc 20 triệu sức mạnh, hoặc 2 triệu vàng"; rồng 1 sao "Capsule 1 sao, hoặc 200 triệu sức mạnh, hoặc 20 triệu vàng, hoặc đẹp trai…"; "Quá 5 phút nếu không ước rồng thần sẽ bay mất". Các phần thưởng thực tế theo bảng 2.5 (khác hướng dẫn – xem mục 8).

---

## 3. Rồng Băng

### 3.1 Nguồn ngọc

Ngọc rồng băng 1–7 sao = item **925–931**. Nguồn tìm thấy trong code: hộp quà Noel `UseItem.NoelItemBox` (dòng ~2352) – 5/90 ra 10–20 ngọc, còn lại chọn ngẫu nhiên 1 trong 7 loại quà, trong đó có 1 viên ngọc rồng băng ngẫu nhiên 925–931.

### 3.2 Gọi rồng (`Shenron_Service.summonShenron`)

- Dùng bất kỳ ngọc 925–931 → `Shenron_Service.openMenuSummonShenron(pl, 0)` → menu "Bạn có muốn gọi Rồng Băng không?" (Đồng ý / Từ chối), `shenronType = 0`.

| # | Điều kiện | Thông báo khi sai |
|---|---|---|
| 1 | Map **khác** 0, 7, 14 | "Không thể gọi rồng ở đây" |
| 2 | Có đủ 7 viên 925–931 | "Bạn còn thiếu 1 viên `<tên>`" |
| 3 | Người chơi chưa có rồng đang xuất hiện (`player.isShenronAppear`, `player.shenronEvent`) | "Không thể thực hiện" |
| 4 | Đã qua `Shenron_Event.timeResummonShenron` = **60.000 ms** kể từ lần rồng băng trước của **chính người đó** | "Vui lòng đợi X giây nữa" |

- Trừ 1 viên mỗi loại 925–931, tạo `Shenron_Event`, thêm vào `Shenron_Manager`, gửi msg `-83` (type `DRAGON_EVENT = 1`) cho **người trong map** (không phải toàn server).

### 3.3 Điều ước (`Shenron_Event.confirmWish`)

Lời rồng: "Ta sẽ ban cho người 1 điều ước, ngươi có 5 phút, hãy chọn đi: 1) Đổi skill 3, 4 đệ tử… 2) Thay đổi nội tại. 3) Cải trang siêu thần HSD 90 ngày. 4) Cải trang Black Gohan Rose HSD 90 ngày." Nút: "Điều ước 1" … "Điều ước 4".

| Nút | Điều ước | Hiệu ứng thực tế | Điều kiện / từ chối |
|---|---|---|---|
| 0 | Điều ước 1 – Đổi chiêu 3, 4 đệ tử | `pet.openSkill3()`; nếu đệ đã có chiêu 4 → `pet.openSkill4()` (tỉ lệ ở 2.6) | Không có đệ / đệ chưa có chiêu 3 → chọn lại |
| 1 | Điều ước 2 – Thay đổi nội tại | `IntrinsicService.doinoitai`: nội tại ngẫu nhiên (bỏ phần tử index 0) của hành tinh, param1/param2 ngẫu nhiên trong khoảng của nội tại | Sức mạnh < 10 tỷ → "10Tỷ Sức Mạnh?", chọn lại |
| 2 | Điều ước 3 – Cải trang siêu thần | TĐ: 905 Cải trang Siêu Thần Trái Đất · NM: 907 Cải trang Siêu thần Namếc · XD: 911 Cải trang Siêu Thần Xayda. Option: 50 (Sức đánh+%) = 22, 47 (Giáp) = 400, 108 (Né đòn %) = 30, 33 (Dịch chuyển tức thời) = 1, 93 (HSD) = 90 ngày | Hành trang đầy → gọi lại menu |
| 3 | Điều ước 4 – Cải trang Black Gohan Rose | 883 Cải trang Black Gohan Rose (mọi hành tinh). Option: 50 = 24, 14 (Chí mạng %) = 3, 103 (KI %) = 19, 80 (HP %/30s) = 10, 93 = 90 ngày | Hành trang đầy → gọi lại menu |

Code còn 2 case không thể chọn từ menu: `case 4` (bùa RX +30 phút, tăng HP/KI/SĐ) và `case 99` (quần đang mặc +1 cấp, +10% các option nâng cấp được).

Sau khi ước: "Điều ước của ngươi đã được thực hiện...tạm biệt", `player.lastTimeShenronAppeared = now`, xóa khỏi manager.

### 3.4 Timeout / mất kết nối

`Shenron_Event.update()` được thiết kế: quá `timeShenronWait` = **60.000 ms** → "Còn cái nịt =)) Có không ước mất đừng tìm." và rồng rời đi; người chơi vào lại (không phải map 0, 7, 14, 21, 22, 23) → gọi lại rồng.

**Tuy nhiên `Shenron_Manager` (implements `Runnable`) không được khởi chạy ở đâu cả** (chỉ có `gI().add()` và `remove()`), nên `update()` không bao giờ chạy: rồng băng **không tự rời đi** và cơ chế gọi lại khi mất kết nối không hoạt động.

---

## 4. Rồng Thần Namếc & Ngọc Rồng Namếc

### 4.1 Khởi tạo & vị trí ngọc (`NgocRongNamecService`)

- Service chạy thread "Update NRNM" (`server/ServerManager.java` dòng 111). Lần đầu `gI()` → `initNgocRongNamec(0)`: rải **7 viên** Ngọc Rồng Namếc 1–7 sao (353–359) ở **7 map khác nhau** chọn ngẫu nhiên trong danh sách, khu ngẫu nhiên, `x` ngẫu nhiên (100 … mapWidth−100).
- **Danh sách map có ngọc (`isMapNRNM`):**

| id | Tên map |
|---|---|
| 7 | Làng Mori |
| 8 | Đồi nấm tím |
| 9 | Thị trấn Moori |
| 10 | Thung lũng Namếc |
| 11 | Thung lũng Maima |
| 12 | Vực maima |
| 13 | Đảo Guru |
| 25 | Trạm tàu vũ trụ |
| 31 | Núi hoa vàng |
| 32 | Núi hoa tím |
| 33 | Nam Guru |
| 34 | Đông Nam Guru |
| 43 | Vách núi Moori |

- **Mỗi 600.000 ms (10 phút)** (`run()`): xóa ngọc/đá đang nằm trên đất tại vị trí đã ghi, rồi rải lại (vị trí mới ngẫu nhiên) **những viên không có người cầm** (`reInitNgocRongNamec`). Trong thời gian ngọc hóa đá, viên rải lại là Hóa thạch Ngọc Rồng (362).

### 4.2 Nhặt & giữ ngọc (`pickNamekBall`, `dropNamekBall`)

| Quy tắc | Chi tiết |
|---|---|
| Nhặt Hóa thạch (362) | Không được: "Chỉ là cục đá, vác chi cho nặng" |
| Nhặt ngọc khi đang trong 24 giờ hóa đá (`now < tOpenNrNamec`) | Không được (thông báo như trên) |
| Mỗi người chỉ cầm **1 viên** | Nhặt viên thứ 2: "Ngọc quá bự, bạn chỉ có thể mang theo 1 viên" |
| Khi nhặt | Người chơi (và đệ tử) chuyển **PK_ALL** (ai cũng đánh được); ghi `idNRNM`, map/khu/tên/id người cầm; `lastTimePickNRNM = now`; hiện cờ trên lưng |
| Chuyển sang map **không** thuộc danh sách 4.1 | Rớt ngọc tại chỗ (`ChangeMapService` dòng ~335) |
| Chuyển sang map thuộc danh sách | Cập nhật vị trí ngọc theo người cầm |
| Bị hạ gục | Rớt ngọc (`Player.java` dòng ~1303) |
| Thoát game | Rớt ngọc tại chỗ (`Client.remove`) |
| Khi rớt ngọc | Trở về NON_PK |

### 4.3 Dò ngọc – "Gói 10 Rađa dò ngọc" (item 361)

`UseItem` (dòng ~298–302): mỗi lần dùng **trừ 1 item 361**, chọn ngẫu nhiên 1 viên mục tiêu (`idGo` = 0–6), hiển thị danh sách khoảng cách/vị trí của 7 viên (hoặc tên người đang giữ, hoặc "Hóa thạch Ngọc Rồng").

- Nếu người chơi **không** cầm ngọc: nút "Đến ngay Viên X Sao 50 ngọc" → `teleportToNrNamec`: dịch chuyển tới khu có viên đó (nếu còn trên đất) hoặc tới khu của người đang giữ; sau đó **trừ 10 ngọc** (`NpcFactory` dòng 659–664; hiển thị 50 ngọc).
- Nếu đang cầm ngọc: chỉ có nút "Kết thúc".

### 4.4 Gọi Rồng Thần Namếc (NPC Dende, map 7)

Dende (`npc_list/Dende.java`) chỉ hiện menu gọi rồng khi người chơi **đang cầm 1 viên ngọc Namếc** và đứng ở map 7: "Hướng dẫn Gọi Rồng" · "Gọi rồng" · "Từ chối".

| # | Điều kiện (theo thứ tự) | Thông báo khi sai |
|---|---|---|
| 1 | Người gọi cầm đúng viên **1 sao** (353) | "Anh phải có viên Ngọc Rồng Namek 1 sao" |
| 2 | Giờ hiện tại (múi giờ Asia/Ho_Chi_Minh) từ **8 đến 22** (`getCurrHour() > 22 || < 8` bị từ chối → được gọi 08:00–22:59) | "…chỉ rảnh gọi Rồng vào khoảng 8h đến 22h" |
| 3 | Người gọi đã cầm ngọc ≥ **10 phút** (`lastTimePickNRNM`) | "Ngọc bẩn quá, xin chờ em X nữa…" |
| 4 | `canCallDragonNamec`: cả 7 viên đều ở **map 7, cùng 1 khu**, và **cả 7 người cầm** là thành viên bang của người gọi (người gọi phải có bang) | "Hãy gom đủ 7 viên Ngọc Rồng tại đây" |

Khi gọi thành công:
1. `tOpenNrNamec = now + 86.400.000` (24 giờ không nhặt được ngọc).
2. `doneDragonNamec()`: 7 người cầm mất ngọc, về NON_PK.
3. `initNgocRongNamec(1)`: rải 7 **Hóa thạch Ngọc Rồng** (362) ở 7 map ngẫu nhiên.
4. `reInitNrNamec(86.399.000)`: sau ~24 giờ, xóa đá và rải lại 7 ngọc thật.
5. `SummonDragonNamek.summonNamec(player)`: thông báo toàn server "`<tên>` vừa gọi rồng thần namek tại …", hiệu ứng Porunga (`DRAGON_PORUNGA = 1`) gửi toàn server, mở menu điều ước. Thời gian ước **300.000 ms (5 phút)**.

### 4.5 Điều ước Rồng Namếc (`SummonDragonNamek.confirmWish`)

Lời rồng: "Ta sẽ ban cho cả bang hội ngươi 1 điều ước, ngươi có 5 phút…". Có bước xác nhận.

Phần thưởng trao cho **mọi thành viên trong `clan.members`** của người gọi – thành viên online nhận trực tiếp; thành viên offline được load từ DB (`MrBlue.loadById`), thêm item rồi `PlayerDAO.updatePlayer`. Nếu người gọi không có bang (không thể xảy ra do điều kiện 4) thì chỉ người gọi nhận.

| Nút | Hiển thị | Phần thưởng (mỗi thành viên) |
|---|---|---|
| 0 | "1-20 viên ngọc rồng 3 sao" | Ngọc Rồng 3 sao (16) × **ngẫu nhiên 1–20** (random riêng từng người) |
| 1 | "pet hổ sẽ béo" | 1 × **Hổ mặp vàng** (942): 1 option ngẫu nhiên trong {77, 80, 81, 103, 50, 94, 5} param 5–10; 20% thêm 1 option trong {14, 16, 17, 19, 27, 28, 5, 47, 87} **cùng param**; option 30 (không giao dịch) |
| 2 | "x99 bột mỳ" | Item **2053** × 99 (id 2053 **không có** trong `item_template` của dump DB) |

Không kiểm tra ô trống hành trang khi thêm item. Sau khi ước → "Điều ước của ngươi đã được thực hiện...tạm biệt".

Mất kết nối: `Client.remove` bật `isPlayerDisconnect`; người gọi vào lại đúng khu → gọi lại rồng. Hết 5 phút → rồng bay đi.

### 4.6 Hướng dẫn trong game (`ConstNpc.HUONG_DAN_NRNM`)

"1) Đang có bang hội 2) Tập họp đủ 7 viên ngọc rồng Namếc tại đây 3) Em sẽ gọi rồng và anh nào giữ ngọc 1 sao sẽ được chọn điều ước 4) Thời gian gọi Rồng Thần là 8h-22h. (Lưu ý) Điều ước sẽ có tác dụng với tất cả thành viên trong bang có mặt tại đây. Sau khi điều ước được thực hiện, tất cả ngọc sẽ biến thành đá trong 1 ngày. Những ai vừa nhận điều ước, hoặc bang hội nhận điều ước phải chờ 7 ngày sau mới có thể nhận điều ước khác."

---

## 5. Ngọc Rồng Sao Đen

### 5.1 Lịch & map

Hằng số `map/phoban/BlackBallWar.java`, giờ Việt Nam (`TimeUtil.VIETNAM_ZONE`):

| Mốc | Giờ |
|---|---|
| Mở cửa (`isBlackBallWarOpen`) | Sau **20:00:00** và trước **21:00:00** |
| Được nhặt ngọc (`isBlackBallWarCanPick`) | Sau **20:30:00** (và đang mở) |
| Đóng cửa | 21:00:00 – mọi người trong map bị đưa về trạm tàu vũ trụ (map `24 + gender`) |

| Map | Tên map | Ngọc sao đen đặt sẵn (`Map.initItem`) |
|---|---|---|
| 85 | Hành tinh M-2 | 372 – Ngọc rồng 1 sao đen |
| 86 | Hành tinh Polaris | 373 – 2 sao đen |
| 87 | Hành tinh Cretaceous | 374 – 3 sao đen |
| 88 | Hành tinh Monmaasu | 375 – 4 sao đen |
| 89 | Hành tinh Rudeeze | 376 – 5 sao đen |
| 90 | Hành tinh Gelbo | 377 – 6 sao đen |
| 91 | Hành tinh Tigere | 378 – 7 sao đen |

### 5.2 NPC

- **Rồng Omega** (`npc_list/RongOmega.java`) tại map 24, 25, 26 (Trạm tàu vũ trụ): "Hướng dẫn thêm" · "Tham gia" (mở tab chọn map `CHANGE_BLACK_BALL`, chỉ khi đang mở) · "Nhận thưởng" (khi có buff còn hạn).
- **Rồng 1 Sao** (`npc_list/Rong1Sao.java`) trong map sao đen: menu phù hộ (`MENU_OPTION_PHU_HP`) và về nhà (`MENU_OPTION_GO_HOME` → map `21 + gender`).

### 5.3 Phù hộ (Rồng 1 Sao → `BlackBallWarService.xHPKI / xDame`)

| Lựa chọn | Hiệu ứng | Giá vàng | Thời gian |
|---|---|---|---|
| x3 HP/KI | `effectSkin.xHPKI = 3`, HP/KI hiện tại ×3 | 10.000.000 | 30 phút (`EffectSkin` dòng 100) |
| x5 HP/KI | `xHPKI = 5` | 30.000.000 | 30 phút |
| x7 HP/KI | `xHPKI = 7` | 50.000.000 | 30 phút |
| x3 Sức đánh | `effectSkin.xDame = 3` (hàm cũng nhân HP/KI hiện tại ×3) | 10.000.000 | 30 phút |
| x5 Sức đánh | `xDame = 5` | 30.000.000 | 30 phút |

Chỉ mua được 1 loại phù hộ tại một thời điểm ("Bạn đã được phù hộ rồi!").

### 5.4 Luật nhặt & thắng

| Quy tắc | Chi tiết |
|---|---|
| Vào map | Nhận cờ theo đồng đội cùng bang đang ở trong khu, nếu không có → cờ ngẫu nhiên 1–7 |
| Nhặt ngọc (`pickBlackBall`) | Chỉ sau 20:30, khu chưa kết thúc (`finishBlackBallWar`), và ≥ **5 giây** kể từ lần ngọc bị rớt gần nhất trong khu |
| Khi nhặt | Người cầm (và mọi thành viên cùng bang trong khu) đổi sang **cờ 8** |
| Rớt ngọc (`dropBlackBall`) | Ngọc rơi tại chỗ; người cầm & đồng đội cùng bang trong khu đổi về cờ ngẫu nhiên 1–7 |
| Thắng (`BlackBallWar.updatePlayer`, tick 150 ms) | Giữ ngọc **liên tục 300.000 ms (5 phút)**; mỗi 10 giây thông báo "Cố giữ ngọc thêm X giây nữa sẽ thắng" |
| Khi thắng | Khu đánh dấu kết thúc; người thắng và **mọi thành viên bang** (online hoặc load từ DB) nhận buff sao tương ứng; toàn bộ người trong khu bị đưa về trạm tàu |

(Hướng dẫn in-game còn ghi cách thắng thứ 2 "Sau 30 phút tham gia tàu sẽ đón về và đang giữ ngọc" – không có code tương ứng.)

### 5.5 Phần thưởng (buff) – `RewardBlackBall` + `NPoint`

- `reward(star)`: `timeOutOfDateReward[star−1] = now + 79.200.000 ms` (**22 giờ**). Nếu buff cũ còn hạn sau mốc 20h hôm nay → `quantilyBlackBall++`.
- Buff có tác dụng **tự động** trong thời hạn (tính trong `NPoint`); nút "Nhận thưởng" ở Rồng Omega chỉ báo "Chỉ Số Tự Cộng Khi Nhặt xong" (hoặc "Chờ Đi...." nếu gọi lại trong 1 giờ).

| Ngọc | Hằng | Hiệu ứng thực tế (`NPoint`) | Dòng |
|---|---|---|---|
| 1 sao đen | `R1S_2 = 21` | Sức đánh +21% | 1131–1133 |
| 2 sao đen | `R2S_1 = 35` | HP tối đa +35% | 814–816 |
| 3 sao đen | `R3S_1 = 35` | Hút HP (`tlHutHp`) +35% | 230–232 |
| 4 sao đen | `R4S_2 = 35` | Phản sát thương (`tlPST`) +35% | 233–235 |
| 5 sao đen | `R5S_1 = 35` | Sát thương chí mạng (`tlDameCrit` và `tlSDCM`) +35% | 236–239 |
| 6 sao đen | `R6S_1 = 40` | KI tối đa +40% | 943–945 |
| 7 sao đen | `R7S_1 = 14` | Né đòn (`tlNeDon`) +14% | 240–242 |

Các hằng `R1S_1, R2S_2, R3S_2, R4S_1, R5S_2, R5S_3, R6S_2, R7S_2` không được dùng.

---

## 6. Code chết

| File | Tình trạng |
|---|---|
| `SRC/src/models/ShenronEvent.java` | Bản cũ của Rồng Băng: chờ/gọi lại 300.000 ms; điều ước 1) đổi skill 3-4 đệ, 2) skill 5 đệ +1 cấp, 3) +10% HP/KI/SĐ 30 phút, 4) quần +1 cấp. Không nơi nào tạo instance. |
| `SRC/src/nro/models/managers/ShenronEventManager.java` | Được start ở `ServerManager` dòng 119 nhưng danh sách luôn rỗng (không ai gọi `add`). |
| `Shenron_Manager.java` | Có `add/remove` nhưng thread `run()` không được start (mục 3.4). |
| `SummonDragon.SHENRON_1_STAR_WISHES_3` | Không truy cập được (mục 2.5). |
| `RewardBlackBall.getReward` | Không trao gì, chỉ thông báo. |

---

## 7. Bảng tra item / option

| id | Tên (DB) |
|---|---|
| 14–20 | Ngọc Rồng 1 sao … 7 sao |
| 16 | Ngọc Rồng 3 sao |
| 227 / 228 / 229 | Avatar VIP (TĐ / NM / XD) |
| 353–359 | Ngọc Rồng Namek 1 Sao … 7 Sao |
| 360 | Ngọc Rồng Namek |
| 361 | Gói 10 Rađa dò ngọc |
| 362 | Hóa thạch Ngọc Rồng |
| 372–378 | Ngọc rồng 1 sao đen … 7 sao đen |
| 883 | Cải trang Black Gohan Rose |
| 905 | Cải trang Siêu Thần Trái Đất |
| 907 | Cải trang Siêu thần Namếc |
| 911 | Cải trang Siêu Thần Xayda |
| 925–931 | Ngọc rồng băng 1 sao … 7 sao |
| 942 | Hổ mặp vàng |
| 2053 | (không có trong `item_template`) |

| Option id | Tên |
|---|---|
| 14 | Chí mạng+#% |
| 30 | Không thể giao dịch |
| 33 | Dịch chuyển tức thời |
| 47 | Giáp+# |
| 50 | Sức đánh+#% |
| 72 | Cấp # |
| 77 | HP+#% |
| 80 | HP+#%/30s |
| 93 | Hạn sử dụng # ngày |
| 97 | Phản #% sát thương |
| 103 | KI +#% |
| 108 | #% Né đòn |

---

## 8. Ghi chú / điểm cần lưu ý

1. **Rồng 1 sao "Giàu có +2 Tỏi Vàng" gán thẳng `gold = 2.000.000.000`** – người đang có nhiều hơn 2 tỷ sẽ **bị giảm vàng** (giới hạn vàng là 200 tỷ).
2. **Lời hướng dẫn và nhãn khác thực tế:** tutorial ghi rồng 3 sao "200k vàng", rồng 2 sao "2 triệu vàng", rồng 1 sao "20 triệu vàng", có "Capsule N sao" – code không có Capsule; rồng 3 sao cho 20 triệu vàng/200 ngọc, rồng 2 sao 200 triệu vàng/2.000 ngọc. Các comment trong code (`+15 ngọc`, `+150 ngọc`, `2 tr vàng`) cũng lệch với giá trị thật.
3. **Ngưỡng chặn vàng của rồng 2/3 sao** so với 1,8 tỷ / 1,98 tỷ rồi đặt vàng = 200 tỷ (`LIMIT_GOLD`) → người có trên 1,8 tỷ (rồng 2 sao) hoặc 1,98 tỷ (rồng 3 sao) sẽ được **đẩy lên 200 tỷ vàng**. Đây là lỗi nghiêm trọng về kinh tế (ngưỡng cũ theo giới hạn 2 tỷ).
4. **Rồng Shenron là toàn server:** chỉ 1 rồng cùng lúc và 10 phút chờ chung cho mọi người.
5. **Rồng 1 sao không kiểm tra viên 1 sao** trong `checkShenronBall` (dựa vào việc người chơi dùng chính viên đó để mở menu).
6. **Rồng Băng:** `Shenron_Manager` không chạy → rồng không tự rời đi, không xử lý mất kết nối; nếu người chơi không ước, `player.shenronEvent` vẫn còn và **không thể gọi rồng băng lần nữa** cho tới khi ước (sau khi đăng nhập lại `shenronEvent` là `null` vì là trường runtime, nhưng instance cũ vẫn trong danh sách). Lời rồng ghi "5 phút" nhưng hằng là 60 giây. Menu chỉ hiện nhãn "Điều ước 1–4"; `case 4` và `case 99` không chọn được.
7. **Rồng Băng không gọi được ở làng 0/7/14**, ngược với Shenron (chỉ gọi ở làng).
8. **Rồng Namếc:** hướng dẫn nói "7 ngày mới nhận điều ước khác" và "tác dụng với thành viên có mặt tại đây" – code không có giới hạn 7 ngày (chỉ có 24 giờ ngọc hóa đá) và trao thưởng cho **toàn bộ** thành viên bang kể cả offline. Không kiểm tra ô trống hành trang. Item 2053 ("bột mỳ") không có trong DB.
9. **Dò ngọc Namếc** hiển thị "50 ngọc" nhưng trừ 10 ngọc và không kiểm tra đủ ngọc (có thể âm). Item "Gói 10 Rađa dò ngọc" bị trừ 1 cái mỗi lần mở.
10. **Giờ gọi Rồng Namếc** thực tế 08:00–22:59 (điều kiện `hour > 22`).
11. **Ngọc Namếc tự đổi chỗ mỗi 10 phút** nếu không ai cầm; `removeStoneNrNamec` xóa item trong vòng `for` với chỉ số `j` tăng trong khi `remove(j)` → có thể bỏ sót item liền kề.
12. **Sao đen:** phù hộ "x Sức đánh" cũng nhân HP/KI hiện tại; buff 22 giờ nên nếu thắng lúc ~20h30 thì hết hạn ~18h30 hôm sau (trước trận kế tiếp). Hằng `AVAILABLE = 5` và `TIME_WAIT` (1 giờ) chỉ dùng cho thông báo. Cách thắng "sau 30 phút tàu đón về" trong hướng dẫn không được cài đặt.
13. **Tỉ lệ đổi chiêu đệ tử:** chiêu 3 và 4 có thể ra trùng chiêu cũ; chỉ chiêu 2 (rồng Shenron) đảm bảo khác chiêu hiện tại.
