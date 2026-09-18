# 15 — Lệnh Admin / GM

> Tổng hợp **toàn bộ** lệnh và quyền quản trị của server NRO Teamobi2026: nội dung file `Lệnh admin.docx`, lệnh chat trong `Command.java`, menu admin (NPC Con Mèo), form nhập liệu admin, lệnh console, quyền đặc biệt của tài khoản admin trong gameplay, và các bảng DB liên quan.
> Nguồn chính: `SRC/src/nro/models/server/Command.java`, `SRC/src/nro/models/npc/NpcFactory.java`, `SRC/src/nro/models/services_func/Input.java`, `SRC/src/nro/models/server/ServerManager.java`.

## Mục lục

1. [Cách trở thành admin](#1-cách-trở-thành-admin)
2. [Nội dung `Lệnh admin.docx`](#2-nội-dung-lệnh-admindocx)
3. [Cơ chế xử lý lệnh chat](#3-cơ-chế-xử-lý-lệnh-chat)
4. [Lệnh chat admin – khớp chính xác](#4-lệnh-chat-admin--khớp-chính-xác)
5. [Lệnh chat admin – có tham số (khớp tiền tố)](#5-lệnh-chat-admin--có-tham-số-khớp-tiền-tố)
6. [Menu admin (lệnh `a`)](#6-menu-admin-lệnh-a)
7. [Menu bot (lệnh `1`)](#7-menu-bot-lệnh-1)
8. [Các form nhập liệu admin](#8-các-form-nhập-liệu-admin)
9. [Menu "Tìm kiếm người chơi": đi tới / gọi tới / đổi tên / ban / kick](#9-menu-tìm-kiếm-người-chơi)
10. [Danh sách boss & dịch chuyển tới boss](#10-danh-sách-boss--dịch-chuyển-tới-boss)
11. [Lệnh chat dành cho mọi người chơi](#11-lệnh-chat-dành-cho-mọi-người-chơi)
12. [Lệnh console server](#12-lệnh-console-server)
13. [Quyền đặc biệt của admin trong gameplay](#13-quyền-đặc-biệt-của-admin-trong-gameplay)
14. [Bảng DB liên quan (`account`, `adminpanel`)](#14-bảng-db-liên-quan)
15. [Bảng tổng hợp nhanh](#15-bảng-tổng-hợp-nhanh)
16. [Ghi chú / điểm cần lưu ý](#16-ghi-chú--điểm-cần-lưu-ý)

---

## 1. Cách trở thành admin

| Bước | Chi tiết | Nguồn |
|---|---|---|
| 1 | Đặt cột `account.is_admin = 1` cho tài khoản trong DB | `database team2026.sql` bảng `account` |
| 2 | Đăng nhập lại. Khi login, `session.isAdmin = rs.getBoolean("is_admin")` | `SRC/src/nro/models/database/MrBlue.java` dòng 68 |
| 3 | Mọi kiểm tra quyền dùng `Player.isAdmin()` = `session != null && session.isAdmin` | `SRC/src/nro/models/player/Player.java` dòng 1315-1316 |

- Chỉ có **1 cấp quyền** (admin / không admin); không có phân cấp GM/mod.
- Cột `account.admin` (int) **không** được server đọc – chỉ `is_admin` có tác dụng (có thể do web panel dùng).
- Thay đổi `is_admin` khi đang online **không** có hiệu lực tới khi đăng nhập lại.

---

## 2. Nội dung `Lệnh admin.docx`

File chỉ liệt kê tên lệnh (không mô tả). Nguyên văn:

```
item
getitem
brl
hs
d
a
m <mapId>
toado
1
2
b
n <taskId>
dm <so>
hp <so>
ki <so>
up <power>
upp <power>
i <itemId> <so luong> [optionId:value]
bien hinh
sach tuyet ky
```

Tất cả lệnh trên đều khớp với code trong `Command.java`; `bien hinh` và `sach tuyet ky` thực ra là **lệnh đệ tử dành cho mọi người chơi** (mục 11). Chi tiết từng lệnh ở các mục dưới.

---

## 3. Cơ chế xử lý lệnh chat

Luồng: client gửi cmd `44` (`CHAT_MAP`) → `Controller.onMessage` (chặn nếu đang giao dịch) → `Command.gI().chat(player, text)`.

`Command.chat` (`SRC/src/nro/models/server/Command.java` dòng 245-253):
1. `text.trim()`; rỗng → bỏ qua.
2. `check(player, text)`; nếu trả về `false` → `Service.gI().chat(player, text)` (phát chat ra map).

`Command.check` (dòng 255-313):
1. Nếu `player.isAdmin()`:
   - `adminCommands.containsKey(text)` → chạy, **return true** (không hiện chat).
   - Duyệt `parameterizedCommands` (HashMap): lệnh đầu tiên mà `text.startsWith(key)` → chạy, **return true**.
2. (Mọi người chơi) `text.startsWith("ten con la ")` → đổi tên đệ tử.
3. (Mọi người chơi có đệ tử) so khớp chính xác các lệnh đệ tử.
4. return `false` → câu chat vẫn được phát ra map (kể cả khi là lệnh đệ tử).

**Thứ tự duyệt thực tế** của `parameterizedCommands` (HashMap capacity 16, tính theo `String.hashCode`): `1` → `2` → `b` → `upp` → `toado` → `i ` → `hp` → `dm` → `up` → `m` → `n` → `ki`. Nhờ đó `upp` được xét trước `up` (đúng ý đồ).

> ⚠️ Vì khớp **tiền tố**, mọi câu chat của admin bắt đầu bằng `1`, `2`, `b`, `m`, `n`, `dm`, `hp`, `ki`, `up`, `i ` (i + dấu cách), `toado` đều bị hiểu là lệnh và **không hiện chat**. Xem mục 16.

---

## 4. Lệnh chat admin – khớp chính xác

Khai báo trong `Command.initAdminCommands()` (dòng 55-68). Quyền: **admin**.

| Lệnh | Cú pháp | Chức năng | Hàm xử lý |
|---|---|---|---|
| `item` | `item` | Mở form **"Tặng vật phẩm"** cho người chơi khác: Tên, Id Item, ID OPTION, PARAM, Số lượng (mục 8.1) | `Input.createFormGiveItem` |
| `getitem` | `getitem` | Mở form **"Get vật phẩm"** cho bản thân: Id Item, ID OPTION, PARAM, Số lượng (mục 8.2) | `Input.createFormGetItem` |
| `brl` | `brl` | Hiện danh sách boss của `BrolyManager` (UI TOP `-96`, tiêu đề "Boss") | `BrolyManager.gI().showListBoss` (kế thừa `BossManager.showListBoss`) |
| `hs` | `hs` | Hồi toàn bộ cooldown skill (gửi `-94` với thời gian còn lại 0) **và hồi đầy KI** | `Service.releaseCooldownSkill(player)` |
| `d` | `d` | Dịch nhân vật xuống **10 px** theo trục Y (`setPos(x, y + 10)`, cmd `123`) – dùng khi bị kẹt | `Service.setPos` |
| `a` | `a` | Mở **menu admin** (NPC Con Mèo, `ConstNpc.MENU_ADMIN = 512`) kèm thông tin server (mục 6) | `NpcService.createMenuConMeo` |

---

## 5. Lệnh chat admin – có tham số (khớp tiền tố)

Khai báo trong `Command.initParameterizedCommands()` (dòng 70-243). Quyền: **admin**.

| Lệnh | Cú pháp | Chức năng chi tiết | Lỗi / thông báo | Dòng |
|---|---|---|---|---|
| `m` | `m <mapId>` (VD `m 5`) | `mapId = int(text.replace("m","").trim())` → `ChangeMapService.changeMapInYard(player, mapId, -1, -1)`: vào khu có thể vào của map, x ngẫu nhiên `100..mapWidth-100` (map hẹp: `100..700`), y = mặt đất | "Sai định dạng map ID!" | 71-78 |
| `toado` | `toado` | Hiện hộp thoại OK: `x: <x> - y: <y>` (toạ độ hiện tại) | – | 80-82 |
| `1` | `1` | Mở **menu bot** (index menu `206783`) với thông tin: Player online, Thread, Sessions, Bot online (mục 7) | – | 84-92 |
| `2` | `2` | Gọi **1 bot tấn công** ngay cạnh admin: đặt admin sang PK `PK_ALL (5)`; tạo `BotAttackplayer` ngoại hình head `1624`, body `1628`, leg `1629`, tên `"đánh nhau không?"`, cờ `0`; thêm vào zone & `BotManager`; gửi thông báo chạy chữ toàn server "Đã gọi bot tấn công người chơi!" | – | 94-122 |
| `b` | `b` | Mở form **"SEND Vật Phẩm Option"** (buff tiền/item cho người chơi online, mục 8.3) | – | 124-126 |
| `n` | `n <taskId>` (VD `n 10`) | `taskMain.id = taskId - 1`, `index = 0`, rồi `TaskService.sendNextTaskMain` → nhảy sang nhiệm vụ `taskId` (có xử lý đặc biệt: id 3 → `gender + 4`; id 4/5/6 → 7). **Có gọi `rewardDoneTask`** nên nhận thưởng nhiệm vụ trước đó | "Sai định dạng task ID!" | 128-137 |
| `dm` | `dm <số>` | Đặt **sức đánh gốc** `nPoint.dameg = số` (int), cập nhật chỉ số | "SET DAMAGE = N" / "Sai cú pháp: dmg <số>" | 139-148 |
| `hp` | `hp <số>` | Đặt **HP gốc** `nPoint.hpg = số` | "SET HP GỐC = N" / "Sai cú pháp: hpg <số>" | 151-160 |
| `ki` | `ki <số>` | Đặt **KI gốc** `nPoint.mpg = số` | "SET KI GỐC = N" / "Sai cú pháp: ki <số>" | 163-172 |
| `up` | `up <power>` | `Service.addSMTN(player, 2, power, false)` → cộng **sức mạnh + tiềm năng** (bị chặn bởi giới hạn sức mạnh hiện tại `getPowerLimit()`) | "UP SMTN = N" / "Sai cú pháp: up <số>" | 174-182 |
| `upp` | `upp <power>` | Cộng SM + TN cho **đệ tử** (`addSMTN(pet, 2, power)`); đệ tử cộng thêm cho sư phụ **50%** (qua `calSubTNSM`, chặn giới hạn SM) | "Bạn chưa có đệ tử" / "UP TNSM cho đệ tử = N" / "Sai cú pháp: upp <số>" | 183-199 |
| `i ` | `i <itemId> [số lượng] [optId:value ...]` (VD `i 457 10`, `i 0 1 47:500 6:1000`) | Tạo `số lượng` (mặc định 1) item **riêng lẻ** (lặp `createNewItem` từng cái) vào túi. Có option tuỳ chỉnh → thay toàn bộ option; không có → dùng option shop mặc định (`getListOptionItemShop`) nếu có | "Cú pháp: i <itemId> <số lượng> [option:value...]" / "GET N x <tên> [id] SUCCESS!" / "Lỗi cú pháp! Dùng: i <itemId> <số lượng> [optionId:value]" | 202-242 |

---

## 6. Menu admin (lệnh `a`)

Tạo ở `Command.java` dòng 61-67; xử lý chọn ở `NpcFactory.java` (`case ConstNpc.MENU_ADMIN`, dòng 490-513).

Nội dung hiển thị:

```
|0|Time start: <dd/MM/yyyy HH:mm:ss lúc khởi động>
Clients: <số player online>
 Sessions: <số session>
Threads: <Thread.activeCount()> luồng
Memory: (used/total) GB
RAM: xx%
CPU: xx%
```

| Lựa chọn | Nhãn | Hành vi | Kiểm tra quyền trong handler |
|---|---|---|---|
| 0 | Ngọc rồng | Thêm vào túi item id **14 → 20** (Ngọc rồng 1–7 sao), mỗi loại 1 | Không (menu chỉ mở được bằng lệnh admin) |
| 1 | Đệ tử | `PetService.createNormalPet(player, null)` – tạo đệ tử thường cho bản thân | Không |
| 2 | Bảo trì | In console "`<tên>` Đang bảo trì game!" → `Maintenance.gI().startSeconds(5)` (bảo trì sau **5 giây**, xem `00-tong-quan.md` mục 9) | **Có** `isAdmin()` |
| 3 | Tìm kiếm người chơi | Mở form "Tìm kiếm người chơi" (Tên người chơi) → menu mục 9 | Không |
| 4 | Boss | `BossManager.gI().showListBoss(player)` (mục 10) | Có (trong `showListBoss`) |
| 5 | Đóng | – | – |

---

## 7. Menu bot (lệnh `1`)

Tạo ở `Command.java` dòng 84-92; xử lý ở `NpcFactory.java` `case 206783` (dòng 347-360); form ở `Input.java` dòng 104-143, 725-747; logic bot ở `SRC/src/nro/models/Bot/NewBot.java`.

| Lựa chọn | Form | Trường nhập | Hành vi |
|---|---|---|---|
| 0 – Bot Pem Quái | "Buff Bot Quái" (`BOTQUAI = 30`) | số lượng bot | `NewBot.runBot(0, null, slot)` trong thread mới – bot đánh quái, nhặt đồ, đổi map, tự chat |
| 1 – Bot Bán Item | "Buff Bot Item" (`BOTITEM = 31`) | số lượng bot; id item cần bán; id item trao đổi; số lượng yêu cầu trao đổi | `new BotGiaoDich(idBan, idTraoDoi, slTraoDoi)` → `runBot(1, bs, slot)` – bot đứng map 84 rao bán, giao dịch tự động |
| 2 – Bot Săn Boss | "Buff Bot Boss" (`BOTBOSS = 32`) | số lượng bot | `runBot(2, null, slot)` – bot bay tới boss ngẫu nhiên để đánh |
| 3 – Bot Attack Player | "Buff Bot Tấn Công Người" (`BOTATTACKPLAYER = 33`) | số lượng bot (thực tế được hiểu là **index** bot) | Chỉ kiểm tra `BotManager.bot[slot]` có phải `BotAttackplayer` rồi tạo thread **rỗng** → **không làm gì** |

Chỉ số và hành vi chi tiết của từng loại bot: xem `01-kien-truc-server-network.md` mục 15.

---

## 8. Các form nhập liệu admin

Form gửi cho client bằng cmd `-125` (`Input.createForm`, lưu `idMark.typeInput`); client trả về cmd `-125` → `Input.doInput` switch theo `typeInput`. Kiểu ô nhập: `NUMERIC = 0`, `ANY = 1`, `PASSWORD = 2`.

### 8.1. "Tặng vật phẩm" – `GIVE_IT = 507` (lệnh `item`)

| Ô | Kiểu |
|---|---|
| Tên | ANY |
| Id Item | ANY |
| ID OPTION | ANY |
| PARAM | ANY |
| Số lượng | ANY |

Xử lý (`Input.java` dòng 284-306): nếu người chơi tên đó **online** → tạo item (`createNewItem`), gán option shop mặc định (nếu có), `quantity = Số lượng`, **thêm** option `(ID OPTION, PARAM)` → vào túi người nhận, người nhận thấy "Nhận <tên item> từ <tên admin>". Không online → "Không online".
Không kiểm tra `isAdmin()` trong handler.

### 8.2. "Get vật phẩm" – `GET_IT = 508` (lệnh `getitem`)

Ô: Id Item, ID OPTION, PARAM, Số lượng (đều ANY). Xử lý (dòng 307-327): **có** kiểm tra `isAdmin()` ("Không đủ quyền hạn!"); tạo item như 8.1 vào túi chính mình → "Nhận <tên item> !".

### 8.3. "SEND Vật Phẩm Option" – `SEND_ITEM_OP = 29` (lệnh `b`)

| Ô | Kiểu |
|---|---|
| Tên người chơi | ANY |
| ID Trang Bị | NUMERIC |
| ID Option | NUMERIC |
| Param | NUMERIC |
| Số lượng | NUMERIC |

Xử lý (`Input.java` dòng 212-266), **có** kiểm tra `isAdmin()`; người nhận phải online (`Client.getPlayer(tên)`), ngược lại "Player không online":

| ID Trang Bị | Kết quả | Giới hạn |
|---|---|---|
| `-1` | Cộng **vàng** = Số lượng | tối đa `Inventory.LIMIT_GOLD = 200.000.000.000` |
| `-2` | Cộng **ngọc xanh** | tối đa `2.000.000.000` |
| `-3` | Cộng **hồng ngọc** (thông báo ghi "ngọc khóa") | tối đa `2.000.000.000` |
| khác | Tạo item id đó, thêm **1 option** `(ID Option, Param)`, `quantity = Số lượng`, thêm vào túi | Túi đầy → "Không thể thêm vật phẩm vào hành trang của người chơi" |

Kết thúc: hiện tutorial (avatar 24) cho admin: "Buff to player: <tên>" + nội dung đã buff.

### 8.4. "Tìm kiếm người chơi" – `FIND_PLAYER = 502` (menu admin → 3)

Ô: Tên người chơi (ANY). Người chơi online → mở menu mục 9; không → "Người chơi không tồn tại hoặc đang offline".

### 8.5. "Đổi tên <tên>" – `CHANGE_NAME = 503` (menu tìm người chơi → Đổi tên)

Ô: Tên mới (ANY). Xử lý (dòng 396-413): nếu tên đã tồn tại trong bảng `player` → "Tên nhân vật đã tồn tại"; ngược lại `update player set name = ? where id = ?`, gửi lại info/cải trang/cờ, `changeMap` tại chỗ để refresh; người bị đổi nhận "Chúc mừng bạn đã có cái tên mới đẹp đẽ hơn tên ban đầu", admin nhận "Đổi tên người chơi thành công". **Không** kiểm tra ký tự đặc biệt/độ dài.

### 8.6. Form bot (`BOTQUAI 30`, `BOTITEM 31`, `BOTBOSS 32`, `BOTATTACKPLAYER 33`)

Xem mục 7.

---

## 9. Menu "Tìm kiếm người chơi"

Tạo ở `Input.java` (`case FIND_PLAYER`, dòng 332-341) với tiêu đề "Ngài muốn..?"; xử lý ở `NpcFactory.java` `case ConstNpc.MENU_FIND_PLAYER (516)` dòng 630-657.

| Lựa chọn | Nhãn | Hành vi |
|---|---|---|
| 0 | Đi tới `<tên>` | `ChangeMapService.changeMapYardrat(admin, p.zone, p.x, p.y)` – dịch chuyển tới vị trí người chơi |
| 1 | Gọi `<tên>` tới đây | `ChangeMapService.changeMap(p, admin.zone, admin.x, admin.y)` – kéo người chơi tới chỗ admin |
| 2 | Đổi tên | Form mục 8.5 |
| 3 | Ban | Menu xác nhận `BAN_PLAYER (513)`: "Bạn có chắc chắn muốn ban <tên>" – [Đồng ý, Hủy] |
| 4 | Kick | "Kik người chơi <tên> thành công"; `Client.getPlayers().remove(p)`; `Client.kickSession(p.session)` (người chơi được lưu dữ liệu như thoát bình thường) |

### 9.1. Ban (`ConstNpc.BAN_PLAYER`)

`NpcFactory.java` dòng 458-463 → `PlayerService.banPlayer(p)` (`SRC/src/nro/models/services/PlayerService.java` dòng 252-262):
1. `update account set ban = 0 where id = ? and username = ?` (**xem mục 16 – đặt 0 chứ không phải 1**).
2. Gửi người bị ban: "Tài khoản của bạn đã bị khóa\nGame sẽ mất kết nối sau 5 giây...".
3. `idMark.lastTimeBan = now`, `idMark.ban = true`.
4. Trong `Player.update()` (`Player.java` dòng 451-454): nếu người chơi **không ở nhà** (map 21–23) và đã qua **5.000 ms** → `Client.kickSession`.
5. Admin nhận "Ban người chơi <tên> thành công".

Để ban thật sự: đặt `account.ban = 1` trực tiếp trong DB → lần login sau nhận "Tài khoản này đang bị khóa. Liên hệ Admin để biết thêm thông tin" (`MrBlue.login`).

### 9.2. Phát đệ tử (`ConstNpc.BUFF_PET = 514`)

`NpcFactory.java` dòng 464-472: nếu người chơi chưa có đệ tử → `PetService.createNormalPet(pl)` + "Phát đệ tử cho <tên> thành công". Menu này được mở từ `SubMenuService.controller` với `menuId = 501` (cmd `-30` sub `64`) – **không có** đường mở từ menu admin hiện tại (hàm `showMenu` bị comment).

---

## 10. Danh sách boss & dịch chuyển tới boss

`BossManager.showListBoss(player)` (`SRC/src/nro/models/boss/Boss_Manager/BossManager.java` dòng 384-419):
- Chỉ admin (không phải admin → return).
- `idMark.menuType = 3`.
- Gửi cmd `-96` (UI TOP) tiêu đề "Boss"; bỏ qua boss có map xuất hiện đầu tiên (`data[0].mapJoin[0]`) thuộc: map boss cuối, Hủy Diệt, Cadic, Yardart (131–133), Mabu, NR Sao Đen.
- Mỗi dòng: `int index`, ngoại hình, tên, `bossStatus`, "`<tên map>(<mapId>) khu <zoneId>`" hoặc "=))" nếu chưa có zone.

Khi admin chọn 1 dòng, client gửi cmd `-118` (`THACHDAU`) với `int id`; `Controller` (dòng 656-676): `menuType` không phải 0/1/2 → nếu admin → `BossManager.gI().getBoss(id)` (lấy **theo index danh sách**) → `ChangeMapService.changeMapYardrat` tới vị trí boss; không phải admin → "Không thể thực hiện".

Dùng từ: menu admin → "Boss" (danh sách `BossManager`), lệnh `brl` (danh sách `BrolyManager`).

---

## 11. Lệnh chat dành cho mọi người chơi

Xử lý trong `Command.check` (dòng 270-311) và `Service.chat` (dòng 686-706). Câu chat **vẫn được phát** ra map sau khi thực hiện (trừ `part` và câu chứa "thang").

| Lệnh | Điều kiện | Chức năng | Nguồn |
|---|---|---|---|
| `ten con la <tên>` | Mọi người chơi | `PetService.changeNamePet(player, <tên>)` – đổi tên đệ tử | `Command.java` dòng 270-272 |
| `di theo` / `follow` | Có đệ tử | Đệ tử **đi theo** (`Pet.FOLLOW`) | dòng 276-277 |
| `bao ve` / `protect` | Có đệ tử | Đệ tử **bảo vệ** (`Pet.PROTECT`) | dòng 278-279 |
| `tan cong` / `attack` | Có đệ tử | Đệ tử **tấn công** (`Pet.ATTACK`) | dòng 280-281 |
| `ve nha` / `go home` | Có đệ tử | Đệ tử **về nhà** (`Pet.GOHOME`) | dòng 282-283 |
| `bien hinh` | Có đệ tử | `pet.transform()` – biến hình đệ tử | dòng 284-285 |
| `sach tuyet ky` | Đệ tử `typePet` ∈ {2, 3, 4} (Goku vô cực, Kid Beerus, Jiren) | Tìm item **type 25** (sách tuyệt kỹ) đầu tiên trong túi; nếu đệ tử có sức mạnh **≥ 1.500.000** → mặc cho đệ tử (đồ cũ trả về túi), "Đã dùng <tên> cho đệ tử"; thiếu SM → "Đệ tử cần đạt 1tr5 sức mạnh để trang bị."; sai loại đệ tử → "Chỉ đệ tử (Goku vô cực, Kid Beerus, Jiren) mới có thể dùng sách tuyệt kỹ." | dòng 286-309 |
| `part` | **Mọi người chơi** (không kiểm tra admin) | `Manager.loadPart()` – đọc lại bảng `part` từ DB và ghi đè `data/update_data/part`; sau đó `DataGame.updateData` gửi lại 6 file data cho người chat | `Service.java` dòng 687-691 |
| câu chứa `thang` (không phân biệt hoa thường) | Mọi người chơi | Không phát ra map; chỉ lưu `player.lastChatMessage` (được boss sự kiện `LanCon` đọc) | `Service.java` dòng 682-694; `boss/event_tet/LanCon.java` dòng 215 |

---

## 12. Lệnh console server

Nhập trực tiếp vào cửa sổ console của tiến trình server (`ServerManager.activeCommandLine`, `SRC/src/nro/models/server/ServerManager.java` dòng 300-331). Quyền: người có quyền truy cập máy chủ.

| Lệnh | Chức năng |
|---|---|
| `bt` | Bảo trì sau **5 giây** (`Maintenance.gI().startSeconds(5)`) → lưu dữ liệu, kick tất cả, chạy `restart_server.bat` (khởi động lại sau 300 s), thoát |
| `bat` | Bật bảo trì tự động hằng ngày lúc **04:30** – "Đã bật chế độ bảo trì tự động." |
| `tat` | Tắt bảo trì tự động – "Đã tắt chế độ bảo trì tự động." |
| `run` | `cmd /c run.bat` – chạy thêm một tiến trình server – "Đã chạy run.bat" |
| khác | "Lệnh không hợp lệ." |

---

## 13. Quyền đặc biệt của admin trong gameplay

Tổng hợp mọi chỗ gọi `isAdmin()` trong code (ngoài `Command.java`):

| Quyền | Chi tiết | Nguồn |
|---|---|---|
| Đổi khu mọi map | `openZoneUI`: bỏ qua chặn map offline/phó bản… | `map/service/ChangeMapService.java` dòng 120 |
| Đổi khu không giới hạn | Bỏ chặn map offline, phó bản, Mabu, Mabu 2H; **không cooldown 5 s**; vào được khu **đã đầy** | `ChangeMapService.changeZone` dòng 170-193 |
| Vào mọi map | `checkMapCanJoin` trả về zone ngay cho admin (bỏ điều kiện nhiệm vụ/sức mạnh) | `ChangeMapService.java` dòng 882 |
| Yardrat vào map 122–124 | Được phép (người thường bị chặn) | `ChangeMapService.checkMapCanJoinByYardart` dòng 1049 |
| Dịch chuyển tới người chơi (cmd `18`) | Không cần trang bị dịch chuyển; tới được cả người đang **ẩn danh**; bỏ qua khu đầy (vẫn chặn map offline, NRSĐ, phó bản, Mabu) | `services/FriendAndEnemyService.java` dòng 278-280 |
| Chat thế giới | Không cooldown 30 s, không yêu cầu sức mạnh 1,5 triệu (vẫn cần ≥ 5 ngọc, trừ 1); tên hiển thị thêm hậu tố **" - Quản Trị Viên"**, hoặc **" - Founder"** nếu tên nhân vật là `Ngọc Rồng Online` | `services/ChatGlobalService.java` dòng 80-81, 162-163, 189-195 |
| Ở lại map Mabu khi hết giờ | Không bị đưa về nhà khi Mabu đóng | `services_dungeon/MajinBuuService.java` dòng 105 |
| Ở lại map Mabu 2H | Không bị kick | `map/phoban/MajinBuu14H.java` dòng 95 |
| Bản đồ kho báu | Vào không cần sức mạnh `POWER_CAN_GO_TO_DBKB` | `npc_list/QuyLaoKame.java` dòng 329, 351 |
| Commeson (Potage) | Gọi bản sao không giới hạn 1 lần/ngày | `npc_list/Potage.java` dòng 44 |
| Hiển thị skill đặc biệt | `NewSkill.sonPhiPhai()`: admin đặt `typePaint = -1` | `player/NewSkill.java` dòng 128-132 |
| Không bị chặn khi server đầy | Có điều kiện `!isAdmin` nhưng kiểm tra trước khi nạp tài khoản nên **không có tác dụng** | `network/MySession.java` dòng 113 |
| Cừu sát | Bỏ qua điều kiện VIP/số lượt | `services/SubMenuService.java` dòng 103 |
| Tới boss từ danh sách | cmd `-118` khi `menuType` = 3 | `server/Controller.java` dòng 665 |
| Danh sách boss | `BossManager.showListBoss` | `boss/Boss_Manager/BossManager.java` dòng 385 |

---

## 14. Bảng DB liên quan

### 14.1. `account` (cột liên quan quyền)

| Cột | Kiểu | Mặc định | Server dùng? | Ý nghĩa |
|---|---|---|---|---|
| `is_admin` | `tinyint(1)` | 0 | **Có** | 1 = admin |
| `ban` | `tinyint(1)` | 0 | **Có** | 1 = khoá, không login được |
| `active` | `int(11)` | 1 | Có (`session.actived`) | Kích hoạt tài khoản (điều kiện một số tính năng như oẳn tù tì, giao dịch) |
| `vip` | `int(11)` | 0 | Có | VIP 1: 15 lượt cừu sát/ngày, VIP 2: 30 |
| `admin` | `int(11)` | 0 | **Không** | Có thể do web panel dùng |

Dữ liệu mẫu trong dump: 3 tài khoản (`id` 3728–3730) đều `is_admin = 1`, mật khẩu yếu (tài khoản test) – nên đổi/xoá trước khi public.

### 14.2. `adminpanel`

Cấu trúc (dump dòng 123-143): `domain`, `title`, `tenmaychu`, `logo`, `trangthai`, `android`, `iphone`, `windows`, `java` (link tải), `apikey`, `giatri`, `userlogin`, `stk`, `name`, `password`, `sessionId`, `deviceId`, `token`, `time`.

- Có 1 dòng dữ liệu (domain web, tiêu đề "Ngoc Rong Online", `trangthai = 'hoatdong'`, thông tin đăng nhập/ngân hàng/API key).
- **Server Java không đọc/ghi bảng này** (grep `adminpanel` trong `SRC/src` không có kết quả) – đây là cấu hình của **web admin panel / nạp tiền** bên ngoài.
- Chứa mật khẩu, API key, token dạng **plaintext** → cần bảo mật.

Không có lệnh admin nào ghi vào DB bảng riêng; nhật ký admin `ServerLog.logAdmin` (gọi khi buff bằng lệnh `b`) **không ghi gì** (chỉ tạo tên file `admin/AD_dd_MM_yyyy.txt`).

---

## 15. Bảng tổng hợp nhanh

| Lệnh | Cú pháp | Quyền | Chức năng | File nguồn |
|---|---|---|---|---|
| `item` | `item` | Admin | Form tặng item (kèm 1 option) cho người chơi online | `server/Command.java`, `services_func/Input.java` (`GIVE_IT`) |
| `getitem` | `getitem` | Admin | Form lấy item (kèm 1 option) cho bản thân | `Command.java`, `Input.java` (`GET_IT`) |
| `brl` | `brl` | Admin | Danh sách boss `BrolyManager` | `Command.java`, `boss/Boss_Manager/BossManager.java` |
| `hs` | `hs` | Admin | Hồi cooldown skill + đầy KI | `Command.java`, `services/Service.java` |
| `d` | `d` | Admin | Dịch xuống 10 px | `Command.java`, `Service.setPos` |
| `a` | `a` | Admin | Menu admin: Ngọc rồng 1–7 sao, Đệ tử, Bảo trì 5 s, Tìm người chơi, Boss | `Command.java`, `npc/NpcFactory.java` |
| `m` | `m <mapId>` | Admin | Dịch chuyển tới map | `Command.java`, `map/service/ChangeMapService.java` |
| `toado` | `toado` | Admin | Xem toạ độ | `Command.java` |
| `1` | `1` | Admin | Menu tạo bot (quái / bán item / săn boss / tấn công) | `Command.java`, `NpcFactory.java`, `Input.java`, `Bot/NewBot.java` |
| `2` | `2` | Admin | Gọi 1 bot tấn công người chơi tại chỗ, bật PK | `Command.java`, `Bot/BotAttackplayer.java` |
| `b` | `b` | Admin | Form buff vàng (-1) / ngọc (-2) / hồng ngọc (-3) / item + option | `Command.java`, `Input.java` (`SEND_ITEM_OP`) |
| `n` | `n <taskId>` | Admin | Nhảy tới nhiệm vụ chính | `Command.java`, `services/TaskService.java` |
| `dm` | `dm <số>` | Admin | Đặt sức đánh gốc | `Command.java` |
| `hp` | `hp <số>` | Admin | Đặt HP gốc | `Command.java` |
| `ki` | `ki <số>` | Admin | Đặt KI gốc | `Command.java` |
| `up` | `up <power>` | Admin | Cộng SM + TN | `Command.java`, `Service.addSMTN` |
| `upp` | `upp <power>` | Admin | Cộng SM + TN cho đệ tử | `Command.java`, `Service.addSMTN` |
| `i` | `i <itemId> [sl] [opt:val ...]` | Admin | Tạo item vào túi | `Command.java`, `services/ItemService.java`, `services/InventoryService.java` |
| (menu) Đi tới / Gọi tới / Đổi tên / Ban / Kick | qua `a` → Tìm kiếm người chơi | Admin | Quản lý người chơi online | `NpcFactory.java` (`MENU_FIND_PLAYER`, `BAN_PLAYER`), `services/PlayerService.java` |
| (UI) Tới boss | chọn dòng trong danh sách boss (cmd `-118`) | Admin | Dịch chuyển tới boss | `server/Controller.java` |
| `ten con la <tên>` | | Mọi người | Đổi tên đệ tử | `Command.java`, `services/PetService.java` |
| `di theo`/`follow`, `bao ve`/`protect`, `tan cong`/`attack`, `ve nha`/`go home` | | Có đệ tử | Đổi trạng thái đệ tử | `Command.java` |
| `bien hinh` | | Có đệ tử | Biến hình đệ tử | `Command.java` |
| `sach tuyet ky` | | Đệ tử loại 2/3/4, SM ≥ 1,5 triệu | Trang bị sách tuyệt kỹ cho đệ tử | `Command.java` |
| `part` | | Mọi người (!) | Nạp lại part từ DB & gửi lại data | `services/Service.java` |
| `bt` / `bat` / `tat` / `run` | console | Quản trị máy chủ | Bảo trì 5 s / bật / tắt bảo trì tự động / chạy run.bat | `server/ServerManager.java` |

---

## 16. Ghi chú / điểm cần lưu ý

1. **Ban không khoá tài khoản**: `PlayerService.banPlayer` chạy `update account set ban = 0` (đúng ra phải là `1`). Kết quả: người chơi chỉ bị kick sau 5 giây rồi có thể đăng nhập lại ngay. Ngoài ra kick chỉ xảy ra khi người chơi **không đứng ở map nhà (21–23)** vì kiểm tra nằm trong nhánh `!MapService.isHome(...)` của `Player.update`.
2. **Lỗ hổng quyền ở `SubMenuService`**: cmd `-30` sub `64` với `menuId = 500` (BAN) hoặc `501` (BUFF_PET) mở menu xác nhận ban/phát đệ tử cho bất kỳ `playerId` nào mà **không kiểm tra `isAdmin()`**; handler `BAN_PLAYER`/`BUFF_PET` trong `NpcFactory` cũng không kiểm tra. Client bị sửa có thể kick (qua cơ chế ban) người khác hoặc phát đệ tử.
3. **Lệnh chat khớp tiền tố "nuốt" chat của admin**: mọi câu bắt đầu bằng `m`, `n`, `b`, `1`, `2`, `dm`, `hp`, `ki`, `up`, `i `, `toado` đều bị coi là lệnh. VD admin gõ "bao ve" / "bien hinh" sẽ mở form SEND item (lệnh `b`) thay vì ra lệnh đệ tử; "mình đây" → báo "Sai định dạng map ID!"; "2 giờ họp" → gọi bot tấn công.
4. **Chat `part` không yêu cầu quyền admin**: bất kỳ người chơi nào cũng có thể khiến server đọc lại bảng `part` và **ghi đè file** `data/update_data/part`, đồng thời nhận lại toàn bộ data (tốn tài nguyên, có thể bị lạm dụng).
5. Lệnh `brl` hiện danh sách của `BrolyManager`, nhưng khi chọn boss thì `Controller` (cmd `-118`) lại tra `BossManager.gI().getBoss(index)` → **dịch chuyển nhầm boss** (index của danh sách khác).
6. Menu bot "Bot Attack Player" (form `BOTATTACKPLAYER`) không tạo bot nào (thread rỗng; ô nhập được dùng như index). Muốn tạo bot tấn công dùng lệnh `2`.
7. Lệnh `2` tạo bot nhắm **người chơi gần nhất cùng map** – thường chính là admin vừa gọi; admin cũng bị chuyển sang PK `PK_ALL`.
8. Thông báo lỗi của `dm`/`hp` ghi sai tên lệnh ("dmg <số>", "hpg <số>").
9. Form `GIVE_IT` (lệnh `item`), form bot và các lựa chọn 0/1/3 của menu admin **không tự kiểm tra `isAdmin()`**; hiện an toàn vì `typeInput`/`indexMenu` chỉ được đặt khi server mở form/menu cho admin, nhưng nên thêm kiểm tra phòng thủ.
10. Form `SEND_ITEM_OP` khai báo ô "ID Trang Bị" là `NUMERIC`; tuỳ client, ô NUMERIC có thể không cho nhập dấu `-` → khó dùng mã `-1/-2/-3` (chưa xác minh trên client).
11. Trong `SEND_ITEM_OP`, dòng `if (player.id != pBuffItem.id) NpcService.createTutorial(player, ...)` gửi thông báo **lần 2 cho admin** thay vì cho người nhận (có thể định gửi cho `pBuffItem`).
12. `ServerLog.logAdmin` không ghi file (thân hàm chỉ tạo tên file) → **không có nhật ký thao tác admin**.
13. Lệnh `n <taskId>` gọi `sendNextTaskMain` → cũng trao thưởng nhiệm vụ hiện tại (`rewardDoneTask`) – có thể nhân thưởng nếu dùng nhiều lần.
14. Lệnh `i` với số lượng lớn tạo từng item riêng lẻ (không gộp), túi đầy thì các item thừa bị bỏ qua nhưng thông báo vẫn báo "SUCCESS".
15. Đổi tên qua menu admin (`CHANGE_NAME`) không kiểm tra độ dài 5–10 ký tự / ký tự đặc biệt như khi tạo nhân vật, và không cập nhật map tên trong `Client.players_name` (tra cứu theo tên mới có thể không thấy tới khi người đó đăng nhập lại).
16. Menu admin "Bảo trì" và lệnh console `bt` dùng 5 giây – quá ngắn để người chơi kịp thoát; nhưng dữ liệu vẫn được lưu do `Client.close()` kick và lưu từng người.
17. Nội dung chat tự động của bot (tạo từ menu `1`) chứa lời lẽ tục tĩu – xem `01-kien-truc-server-network.md` mục 17.
