# 38 — Bảng điều khiển (cpanel) dạng cửa sổ

Cửa sổ Java Swing tự bật lên khi chạy server, dùng để **đăng ký tài khoản** và **quản lý người chơi nhanh** mà không cần vào game hay mở phpMyAdmin. Không thêm thư viện ngoài (chỉ Swing có sẵn trong JDK 17).

---

## 1. Tệp và điểm nối

| Tệp | Vai trò |
|---|---|
| `SRC/src/nro/models/cpanel/CPanel.java` | Khởi động, kiểm tra cấu hình/headless, khung cửa sổ, luồng nền, hộp thoại, ghi log |
| `SRC/src/nro/models/cpanel/AccountDao.java` | Đọc/ghi bảng `account` (PreparedStatement) |
| `SRC/src/nro/models/cpanel/CharacterOps.java` | Đọc nhân vật; cộng tiền, đặt lại nhiệm vụ, tặng vật phẩm theo 2 đường online/offline |
| `SRC/src/nro/models/cpanel/AccountTab.java` | Tab **Tài khoản** |
| `SRC/src/nro/models/cpanel/OnlineTab.java` | Tab **Người chơi online** |
| `SRC/src/nro/models/cpanel/CharacterTab.java` | Tab **Nhân vật** |
| `SRC/src/nro/models/cpanel/ServerTab.java` | Tab **Server** |
| `SRC/src/nro/models/cpanel/BuffOps.java` | Tra vật phẩm/option, phát vật phẩm có option tùy chỉnh (online/offline/tất cả), đọc/ghi mẫu buff |
| `SRC/src/nro/models/cpanel/BuffTab.java` | Tab **Buff đồ** (+ hàm dùng chung `pickCharacter`, `runOp`) |
| `SRC/src/nro/models/cpanel/EventOps.java` | Giftcode (DB + bộ nhớ), tỉ lệ EXP server |
| `SRC/src/nro/models/cpanel/EventTab.java` | Tab **Sự kiện** |
| `SRC/src/nro/models/event/EventManager.java` | Viết lại: danh sách sự kiện, đọc cờ từ `Config.properties`, API bật/tắt lúc chạy, `startBossManagers()` |
| `SRC/src/nro/models/event/Event.java` | Ghi nhớ boss/NPC do sự kiện tạo; thêm `stop()` để gỡ lúc chạy |
| `SRC/src/nro/models/event/EventConfig.java` | Đọc / ghi 1 khóa trong `Config.properties`, giữ nguyên chú thích |
| `SRC/src/nro/models/server/ServerManager.java` | (1) trong `main()`, ngay trước `activeCommandLine()` gọi `nro.models.cpanel.CPanel.startIfEnabled();` (2) trong `run()`, sau luồng `GasDestroyManager`, gọi `EventManager.gI().startBossManagers();` (sửa lỗi boss sự kiện đứng im, §4.6.1) |
| `SRC/Config.properties` | Khóa `server.cpanel=true`; khóa `event.<khóa>=true/false` cho từng sự kiện |
| `cpanel_buff_presets.json` (thư mục chạy server, tự tạo) | Mẫu buff lưu nhanh của tab Buff đồ |

Không sửa `TaskService`, `ConstTask`, `Player`, `Controller`, `PlayerDAO` — cpanel chỉ **gọi** các hàm công khai có sẵn của chúng.

---

## 2. Bật / tắt

Trong `Config.properties` (cùng thư mục với `20.jar`):

```properties
server.cpanel=true    # mặc định: tự bật cửa sổ khi chạy server
server.cpanel=false   # tắt hẳn (nhận cả 0 / off / no)
```

Thiếu khóa hoặc không đọc được file ⇒ coi như **bật**.

**Máy không có màn hình (VPS Linux headless):** `CPanel.startIfEnabled()` kiểm tra `GraphicsEnvironment.isHeadless()`. Nếu headless thì in ra console

```
[CPANEL hh:mm:ss] Máy chủ không có màn hình (headless) -> bỏ qua cpanel, server vẫn chạy bình thường.
```

và thoát khỏi hàm, server chạy như cũ. Trên Linux, JDK tự coi là headless khi không có biến `DISPLAY`; có thể ép bằng `java -Djava.awt.headless=true -jar 20.jar`. Mọi lỗi khi dựng cửa sổ (VD `DISPLAY` đặt sai, thiếu thư viện đồ họa) đều bị bắt (`catch Throwable`), chỉ ghi log — **không làm sập server**. Đã chạy thử cả hai trường hợp headless và `server.cpanel=false`: server tiếp tục chạy.

**Đóng cửa sổ không tắt server.** Bấm nút X sẽ hỏi: *Thu nhỏ* / *Đóng bảng điều khiển* / *Hủy*. Đóng hẳn thì muốn mở lại phải khởi động lại server (lệnh console không bị đụng tới).

---

## 3. Bố cục (mô tả bằng chữ)

```
┌─ NRO CPanel - <tên server> - cổng 14445 ─────────────────────────────────────────┐
│ [Tài khoản] [Người chơi online] [Nhân vật] [Server] [Buff đồ] [Sự kiện]          │
│ ┌──────────────────────────────────────────────────────────┐ ┌─ Thao tác ───────┐ │
│ │ Tên đăng nhập: [__________] [Tìm / Làm mới]  12 tài khoản│ │ Tạo tài khoản mới│ │
│ │ ┌──────────────────────────────────────────────────────┐ │ │ Đổi mật khẩu     │ │
│ │ │ ID │ Tên đăng nhập │ Admin │ Bị ban │ VND │ Tổng nạp │ │ │ Bật / tắt admin  │ │
│ │ │    │ Kích hoạt │ Ngày tạo │ Đang online              │ │ │ Ban / gỡ ban     │ │
│ │ │ ...  (bảng, bấm tiêu đề cột để sắp xếp)              │ │ │ Cộng / trừ VND   │ │
│ │ └──────────────────────────────────────────────────────┘ │ │ Kích hoạt / hủy  │ │
│ └──────────────────────────────────────────────────────────┘ └──────────────────┘ │
│ ┌─ Nhật ký thao tác (cũng in ra console) ──────────────────────────────────────┐ │
│ │ [CPANEL 10:15:02] Tạo tài khoản "abc123" (id 3731, kích hoạt=true)           │ │
│ └──────────────────────────────────────────────────────────────────────────────┘ │
│  Sẵn sàng / Đang xử lý (1 việc)...                                   (thanh trạng thái) │
└───────────────────────────────────────────────────────────────────────────────────┘
```

- Mỗi tab: **trên** là ô tìm/làm mới, **giữa** là bảng, **phải** là cột nút thao tác. Nút cần chọn dòng sẽ mờ khi chưa chọn.
- **Dưới cùng** (chung cho mọi tab): khung nhật ký thao tác và thanh trạng thái "Đang xử lý (n việc)..." khi có việc chạy nền.
- Tab **Nhân vật**: bảng kết quả tìm ở nửa trên, khung "Chi tiết nhân vật" ở nửa dưới (kéo được thanh chia).
- Tab **Server**: khung "Thông số" (dạng nhãn : giá trị) ở trên, hàng nút thao tác ở dưới.
- Tab **Buff đồ**: trái = ô tìm + bảng vật phẩm; phải (cuộn được) = vật phẩm đang chọn + số lượng, bảng option, nút sửa option, nút nhanh, khung "Xem trước", dưới cùng là mẫu buff + người nhận + nút **PHÁT VẬT PHẨM**.
- Tab **Sự kiện**: nửa trên = bảng sự kiện + nút Bật/Tắt; nửa dưới = bảng giftcode + nút Tạo/Xóa; cột phải = Thông báo toàn server, Điểm sự kiện, Tỉ lệ EXP.
- Font: tự chọn font đầu tiên hiển thị đủ tiếng Việt trong `Segoe UI`, `Tahoma`, `Arial`, `Noto Sans`, `DejaVu Sans`, `Liberation Sans`; không có thì dùng font logic `Dialog`. Giao diện theo Look&Feel của hệ điều hành.

---

## 4. Từng chức năng

### 4.1. Tab Tài khoản

| Chức năng | Làm gì | Ghi chú |
|---|---|---|
| Danh sách / tìm | `SELECT id, username, is_admin, ban, vnd, tongnap, active, create_time FROM account WHERE username LIKE ? ORDER BY id DESC LIMIT 500` | Tìm theo một phần tên (ký tự `%` `_` được thoát). Cột "Đang online" = tên nhân vật nếu tài khoản đang trong game (`Client.getPlayerByUser`). |
| **Tạo tài khoản mới** | Hỏi tên, mật khẩu (2 lần), ô "Kích hoạt ngay". Kiểm tra trùng tên bằng `SELECT` trước, rồi `INSERT INTO account (username, password, email, token, xsrf_token, newpass, active) VALUES (?, ?, '', '', '', '', ?)` | Tên ≤ 20 ký tự, chỉ gồm `A-Z a-z 0-9 _ . @ -`; mật khẩu 1–100 ký tự. 4 cột `email/token/xsrf_token/newpass` là NOT NULL không có DEFAULT nên phải truyền chuỗi rỗng. Nếu 2 người tạo trùng cùng lúc, lỗi khóa UNIQUE (mã 1062) cũng được báo "đã tồn tại". |
| **Đổi mật khẩu** | `UPDATE account SET password = ? WHERE id = ?` | Nhập 2 lần. |
| **Bật/tắt admin** | `UPDATE account SET is_admin = ?` + nếu đang online gán luôn `session.isAdmin` | Có hỏi xác nhận. Có hiệu lực ngay, không cần đăng nhập lại. |
| **Ban / gỡ ban** | `UPDATE account SET ban = ?`. Ban mà người đó đang online: gọi `PlayerService.banPlayer()` có sẵn (báo "Tài khoản của bạn đã bị khóa...") rồi **tự kick sau 5 giây** bằng `Client.kickSession()` | Hỏi xác nhận. Tự kick vì `Player.update()` chỉ kick khi người chơi không đứng ở nhà (doc 15 §16.1). |
| **Cộng/trừ VND** | `UPDATE account SET vnd = vnd + ? WHERE id = ? AND vnd + ? >= 0`, đọc lại số dư, gán `session.vnd` nếu online | Số âm = trừ, **hỏi xác nhận**. Cập nhật *tương đối* giống `PlayerDAO.subvnd()` nên không đè lên giao dịch đang diễn ra; không cho số dư âm. |
| **Kích hoạt / hủy kích hoạt** | `UPDATE account SET active = ?` + gán `session.actived` nếu online | Hủy kích hoạt có hỏi xác nhận. |

Các cột `account` không bị luồng lưu nhân vật (`PlayerDAO.updatePlayer`) ghi, nên luôn sửa thẳng DB là an toàn.

### 4.2. Tab Người chơi online

- Bảng: ID, Nhân vật, Tài khoản, Hành tinh, Sức mạnh, Bản đồ (tên + id), Khu. **Tự làm mới mỗi 3 giây** trên luồng `CPanel Timer` (chỉ đọc bộ nhớ qua `Client.getPlayersSnapshot()`, không đụng DB), giữ nguyên dòng đang chọn.
- **Kick**: hỏi xác nhận → `Client.gI().kickSession(session)` (giống menu admin "Kick"; `Client.remove()` tự lưu dữ liệu như thoát bình thường).
- **Gửi thông báo tới người này**: `Service.sendThongBaoOK(player, text)` (hộp thoại OK trong game).
- **Thông báo toàn server**: hỏi xác nhận → `Service.sendThongBaoAllPlayer(text)` (dòng chữ thông báo cho mọi người).

### 4.3. Tab Nhân vật

- Tìm theo một phần tên (`player.name LIKE ?`, tối đa 100 dòng). Chọn một dòng ⇒ khung chi tiết: tài khoản, hành tinh, trạng thái, **sức mạnh, vàng, ngọc, hồng ngọc, nhiệm vụ hiện tại** (id + tên nhiệm vụ, bước + tên bước, số đã làm).
  - Offline: đọc từ JSON `data_point[1]`, `data_inventory[0..2]`, `data_task[0..2]` (doc 02 §4).
  - Online: lấy **từ bộ nhớ** (`nPoint.power`, `inventory.*`, `playerTask.taskMain`) vì DB có thể cũ tới 5 phút; hiện thêm map/khu.
- **Cộng / trừ vàng, ngọc, hồng ngọc**: số âm = trừ (hỏi xác nhận); cộng vàng ≥ 10 tỷ cũng hỏi. Kết quả bị kẹp trong `[0, Inventory.LIMIT_GOLD]` với vàng và `[0, 2.147.483.647]` với ngọc/hồng ngọc.
- **Đặt lại nhiệm vụ**: nhập id nhiệm vụ (mặc định = nhiệm vụ hiện tại ⇒ làm lại từ bước 0), hỏi xác nhận. **Không trao thưởng** (khác lệnh chat `n <id>` vốn gọi `sendNextTaskMain` và trao thưởng). Id phải có trong `task_main_template` (`Manager.TASKS`).
- **Tặng vật phẩm**: nhập id (hiện tên vật phẩm ngay khi gõ) + số lượng. Option mặc định lấy từ shop (`ItemService.getListOptionItemShop`) giống lệnh `i`; vật phẩm cộng dồn (`isUpToUp`) tặng 1 ô, không cộng dồn tặng từng cái (tối đa 80 cái/lần, phải đủ ô trống).

**Hai đường ghi — không đè lên nhau:**

| Trạng thái | Cách làm |
|---|---|
| **Đang online** (có trong `Client`, đã nạp xong dữ liệu) | Chỉ sửa đối tượng `Player` trong bộ nhớ bằng hàm có sẵn: tiền ⇒ sửa `inventory` + `Service.sendMoney()`; nhiệm vụ ⇒ `TaskService.switchTaskMain(player, id, 0)`; vật phẩm ⇒ `ItemService.createNewItem` + `InventoryService.addItemBag` + `sendItemBags`. Người chơi nhận thông báo. **Không ghi DB** — game tự lưu (5 phút/lần, khi thoát, khi bảo trì, hoặc nút "Lưu toàn bộ"). |
| **Đang vào game / đang thoát** (`beforeDispose`, chưa `loadedAllDataPlayer`) | Từ chối, báo "thử lại sau vài giây". |
| **Offline** | Một transaction: `SELECT <cột> ... FROM player p LEFT JOIN account a ... WHERE p.id = ? FOR UPDATE` → **kiểm tra lại online sau khi đã khóa dòng** (nếu vừa vào game thì `ROLLBACK` và chuyển sang đường online) → nếu `account.last_time_login > last_time_logout` (phiên trước chưa đóng xong: game đang lưu lúc thoát, hoặc server từng sập) thì **hỏi admin xác nhận** → sửa JSON → `UPDATE player SET <cột> = ? WHERE id = ?` → `COMMIT`. Sau khi ghi, nếu thấy nhân vật đã online thì cảnh báo "thay đổi có thể bị ghi đè". |

Cột được sửa offline chỉ là 3 hằng số trong code: `data_inventory`, `data_task` (`[id, 0, 0, now]`), `items_bag` (tìm ô `-1` trống, ghi `[id, số lượng, "[\"[opt,param]\"...]", createTime]` đúng định dạng `PlayerDAO.createNewPlayer`/`MrBlue.loadPlayer`; vật phẩm cộng dồn trùng id + option thì cộng vào ô cũ).
Vật phẩm đặc biệt (loại tiền tệ 9/10/34, ngọc rồng đen/namếc/đá, 517/518 mở rộng ô) **chỉ tặng khi online** vì game xử lý riêng trong `addItemBag`.

### 4.4. Tab Server

- Thông số tự làm mới 2 giây: người chơi online, số phiên kết nối (`SessionManager`), bộ nhớ (đang dùng / đã cấp / tối đa, MB), số luồng, giờ khởi động, trạng thái bảo trì.
- **Lưu toàn bộ dữ liệu** (hỏi xác nhận, chạy nền): với từng người online gọi `PlayerDAO.autoSavePlayer()` (hàm tự lưu có sẵn, có khóa `saveLock`, bỏ qua người đang thoát), sau đó `ClanService.close()` (tên là close nhưng chỉ ghi bang hội xuống DB) và `ConsignShopManager.save()`. Báo số nhân vật đã lưu và thời gian.
- **Bảo trì...**: nhập số giây (5–3600, mặc định 60), hỏi xác nhận → `Maintenance.gI().startSeconds(n)` — **đúng cơ chế đang có** (lệnh console `bt`, menu admin "Bảo trì"): đếm ngược báo người chơi mỗi giây → `ServerManager.close()` lưu dữ liệu, kick tất cả, chạy `restart_server.bat`, thoát. Không hủy được sau khi bắt đầu.
- Ô **Bảo trì tự động hằng ngày (04:30)**: bật/tắt `AutoMaintenance.AutoMaintenance` (giống lệnh console `bat` / `tat`).
- **Dọn bộ nhớ (GC)**: gọi `System.gc()` trên luồng nền.

### 4.5. Tab Buff đồ

**Tìm vật phẩm**: gõ id (khớp chính xác, ưu tiên lên đầu) hoặc một phần tên (bỏ dấu, không phân biệt hoa/thường) → tìm trong `Manager.ITEM_TEMPLATES` đã nạp (không đụng DB), tối đa 300 dòng. Cột: ID, Tên, Loại (TYPE + tên loại theo docs 06 §3), Hành tinh (3 = Chung), Icon, Cộng dồn.

**Dựng option** (danh sách chính xác sẽ gắn vào vật phẩm, đúng thứ tự):

| Nút | Làm gì |
|---|---|
| Thêm option... | Hộp thoại tìm option trong `item_option_template` (`Manager.ITEM_OPTION_TEMPLATES`) theo tên (bỏ dấu) hoặc id, nhập giá trị `#` |
| Sửa giá trị / Xóa option / Xóa hết | trên dòng đang chọn |
| Nạp option shop | Thêm option mặc định của shop (`ItemService.getListOptionItemShop`, giống lệnh admin `i` / form `GIVE_IT`) |
| Chỉ số Thiên Sứ | Chỉ cho id 1048–1062: gọi `ItemService.DoThienSu(id, gender)` rồi lấy option của nó (chỉ số ngẫu nhiên + 21=30 + 30) |
| Số lỗ sao pha lê (**107**) · Sao đã ép (**102**) · Cấp +N (**72**, 1–8) · Hạn sử dụng ngày (**93**, ≥1) · Không thể giao dịch (**30**, param 0 — game chỉ xét id) · Yêu cầu SM tỉ (**21**) · Sức đánh % (**50**) · HP % (**77**) · KI % (**103**) · Chí mạng % (**14**) · Giảm sát thương % (**94**) | Nút nhanh; id tra từ docs 06 §5 và đối chiếu tên trong `item_option_template`. Nếu đã có option cùng id thì **thay giá trị** thay vì thêm dòng thứ hai |
| Set kích hoạt... | Chọn 1 trong 13 set (docs 06 §9.5): thêm option set (param 1) + dòng mô tả `ItemService.getOptionIdsBySKH(skhId)` + 30, **y như `ItemService.createItemSKH`**; tự bỏ set cũ (127–144, 233–248) trước |

Không có nút "chỉ số Thần Linh" riêng vì `ItemService.randDoTL/randDoTLBoss` tạo `ItemMap` gắn với `Zone` (không dùng được ngoài map); mẫu "Đồ Thần Linh" dùng công thức docs 06 §9.2 ở tiLe = 100.

**Xem trước**: số lượng × tên [id] và từng dòng option đã thay `#` bằng giá trị (kèm `[id:param]`). Không có option ⇒ ghi chú "game tự thêm option 73".

**Phát**:
- **Một nhân vật**: nhập tên → tìm (`player.name LIKE`), khớp đúng tên thì chọn luôn, nhiều kết quả thì cho chọn → hỏi xác nhận → `BuffOps.giveToCharacter`:
  - Online: `ItemService.createNewItem(id, qty)` + `new Item.ItemOption(id, param)` + `InventoryService.addItemBag` + `sendItemBags` (như form `SEND_ITEM_OP`). Cộng dồn ⇒ 1 món số lượng N; không cộng dồn ⇒ N món (≤ 80, phải đủ ô trống).
  - Offline: **đúng đường của tab Nhân vật** (`CharacterOps.modifyOffline` "items_bag": `FOR UPDATE`, kiểm tra lại online, hỏi xác nhận nếu phiên trước chưa đóng), ghi `[id, qty, "[\"[opt,param]\"...]", now]` với **đúng danh sách option đã dựng**. Vật phẩm đặc biệt (tiền tệ, ngọc rồng, 517/518) chỉ phát khi online.
- **Tất cả người đang online**: hỏi xác nhận → duyệt `Client.getPlayersSnapshot()`, bỏ qua người đang vào/thoát, mỗi người nhận 1 phần. Báo số thành công / thất bại (hành trang đầy) / bỏ qua, log tên người thất bại.

**An toàn**: trước khi phát kiểm tra id vật phẩm **theo vị trí mảng** (`id < ITEM_TEMPLATES.size()` và `ITEM_TEMPLATES.get(id).id == id`) vì `ItemService.getTemplate` tra `get(id)` — id ngoài biên sẽ ném `IndexOutOfBoundsException`; option cũng kiểm tra y như vậy (`Item.ItemOption(id, param)` tra `ITEM_OPTION_TEMPLATES.get(id)`). **Id 2000–2031 (vật phẩm nhiệm vụ)**: nhãn đỏ trong ô chọn, cảnh báo trong xem trước và hỏi xác nhận riêng trước khi phát. Log mọi lần phát `[CPANEL] [BUFF]...`.

**Mẫu buff**: hộp chọn + "Nạp mẫu" (điền vật phẩm, số lượng, option) / "Lưu thành mẫu..." (lưu trạng thái hiện tại, trùng tên thì ghi đè) / "Xóa mẫu" (hỏi xác nhận). Lưu ở `cpanel_buff_presets.json` trong thư mục chạy server (UTF-8, ghi file tạm rồi thay thế), **không đụng DB**. Chưa có file ⇒ 3 mẫu mặc định:

| Mẫu | Vật phẩm | Option |
|---|---|---|
| Set kích hoạt - Áo vải 3 lỗ Sôngôku (TĐ) | 0 × 1 | 47:2 (giáp gốc, docs 06 §2.3), 129:1, 141:1, 30:1 |
| Đồ Thần Linh - Áo TL Trái Đất | 555 × 1 | 47:800, 21:15 (docs 06 §9.2, tiLe 100) |
| Hộp quà sự kiện x10 | 1776 × 10 | 30:0 |

Định dạng file: `[{"name":"...","item":[id,số lượng],"options":[[optId,param],...]}]`.

### 4.6. Tab Sự kiện

**Bảng sự kiện** (bấm "Làm mới" để cập nhật): khóa, tên, đang chạy, lần khởi động sau (đọc `Config.properties`), bật/tắt lúc chạy được không, số boss trên map / tổng, luồng boss đã start chưa, ghi chú.

| Khóa (`event.<khóa>`) | Sự kiện | Mặc định (thiếu khóa) | Bật/tắt lúc chạy |
|---|---|---|---|
| `default` | 30 Broly | bật | **Không** — Broly nằm trong `BrolyManager` chung với boss khác và có thể biến thành Super Broly (boss mới) ⇒ không gỡ sạch được |
| `hung_vuong` | 10 Thủy Tinh (+ Sơn Tinh đi kèm) | bật | Được |
| `halloween` | 10 Bí ma + 10 Ma trơi + 10 Dơi | tắt | Được |
| `trung_thu` | 10 Khỉ đột + 10 Nguyệt thần (+ Nhật thần) | tắt | Được — khi bật, cảnh báo nếu `item_template` thiếu 2123/2124 |
| `christmas` | 30 Ông già Noel | tắt | Được |
| `lunar_new_year` | 10 Lân con + NPC 49 ở map 0 | tắt | Được — người đang đứng ở map 0 phải đổi map mới thấy/mất NPC |
| `womens_day` | 8/3 | tắt | **Không** (chỉ đọc bảng `event` vốn không tồn tại, dữ liệu không được dùng) |
| `top_up` | TopUp | bật | **Không** (lớp rỗng) |

Mặc định giữ **đúng hành vi cũ** của `EventManager.init()` (chỉ Default, Hùng Vương, TopUp chạy; các cờ `true` cũ vô tác dụng vì lời gọi bị comment). `Config.properties` đã được thêm đủ 8 khóa với giá trị này.

**Bật / Tắt**: chọn dòng → nút BẬT/TẮT → hộp thoại "Áp dụng NGAY + lưu" / "Chỉ lưu (lần khởi động sau)" / "Hủy" (sự kiện không bật/tắt được lúc chạy thì chỉ có xác nhận "chỉ lưu"). Luôn ghi `event.<khóa>=true/false` vào `Config.properties` (thay đúng dòng, giữ chú thích — `EventConfig.set`).
- **Bật ngay**: tạo instance sự kiện, `init()` (tạo boss/NPC như lúc khởi động) và start luồng manager boss nếu chưa start. Đã chạy rồi thì không tạo thêm.
- **Tắt ngay** (`Event.stop`): bỏ mọi boss do sự kiện tạo (kể cả boss "xuất hiện cùng") khỏi manager của sự kiện → chờ 1,6 giây (quá 1 nhịp 1,5 giây của manager, để không còn `update()` dở) → `ChangeMapService.exitMap(boss)` cho boss đang trên map → gỡ NPC sự kiện khỏi `map.npcs`. Luồng manager vẫn chạy (danh sách rỗng, gần như không tốn gì) để lần bật sau dùng lại.

#### 4.6.1. Lỗi đã sửa: boss sự kiện đứng im

Boss có `BossType` sự kiện (`HUNGVUONG_EVENT`, `HALLOWEEN_EVENT`, `CHRISTMAS_EVENT`, `TRUNGTHU_EVENT`, `TET_EVENT`) được constructor `Boss` đưa vào 5 manager riêng (`HungVuongEventManager`…`LunarNewYearEventManager`, đều kế thừa `BossManager implements Runnable`), nhưng `ServerManager.run()` chỉ `new Thread(...)` cho các manager boss thường ⇒ không ai gọi `boss.update()` ⇒ boss sự kiện (kể cả Thủy Tinh/Sơn Tinh đang bật) nằm REST mãi, không bao giờ lên map.

Sửa: `EventManager.startBossManagers()` được gọi trong `ServerManager.run()` ngay sau luồng `GasDestroyManager` (cùng chỗ và cùng cách `new Thread(manager, "Update ...").start()` như các manager khác). Chỉ start manager của **sự kiện đang bật**; mỗi manager được ghi vào một `Set` định danh (identity) trong `EventManager` (các hàm đều `synchronized`) ⇒ gọi lại nhiều lần, hoặc bật sự kiện lúc chạy sau đó, **không bao giờ start trùng**. Đã grep: không chỗ nào khác start 5 manager này. Tên luồng: `Update <khóa> event boss`.

⇒ **Hệ quả cần biết**: từ bản này, với cấu hình mặc định, **10 Thủy Tinh + 10 Sơn Tinh sẽ thật sự xuất hiện** trên các map 0–20, 24–37… (trước đây tuy "bật" nhưng không bao giờ ra). Không muốn thì đặt `event.hung_vuong=false` hoặc tắt ở tab Sự kiện.

**Giftcode** (bảng `giftcode`):
- Danh sách: `SELECT id, code, count_left, detail, datecreate, expired FROM giftcode ORDER BY id DESC LIMIT 1000`; thêm cột "Lượt còn (bộ nhớ)" lấy từ `GiftCodeManager.listGiftCode` (`CHƯA NẠP` = có trong DB nhưng server chưa nạp — VD thêm tay vào DB sau khi khởi động). Cột "Quà" tóm tắt JSON `detail`.
- **Tạo code mới**: mã `[A-Za-z0-9_]{3,40}` (cột `code` là latin1, game so khớp phân biệt hoa/thường), số lượt (-1 = không giới hạn, game đổi thành 999.999.999), hạn dùng `yyyy-MM-dd HH:mm:ss` (sau hiện tại, trước 2038-01-19 vì cột `TIMESTAMP`), nội dung quà mỗi dòng `<id> <số lượng> [optId:param ...]` (id -1 vàng, -2 ngọc, -3 hồng ngọc; dòng `#` là chú thích). Kiểm tra: id vật phẩm/option tồn tại, **không trùng id** (game lưu quà trong `HashMap<id, số lượng>` nên 2 dòng cùng id sẽ đè nhau), ≤ 20 dòng, mã chưa có trong DB lẫn bộ nhớ. Ghi `INSERT INTO giftcode (code, count_left, detail, datecreate, expired) VALUES (?, ?, ?, ?, ?)` với `detail` đúng định dạng `Manager.java` đang đọc: `[{"id":457,"quantity":10,"options":[{"id":30,"param":0}]}]`, rồi **nạp luôn vào `GiftCodeManager.listGiftCode`** giống hệt `Manager.java` ⇒ người chơi nhập được ngay, không cần restart.
- **Xóa code**: hỏi xác nhận → `DELETE FROM giftcode WHERE id = ?` + gỡ khỏi bộ nhớ.

**Thông báo sự kiện toàn server**: hỏi xác nhận → `Service.sendThongBaoAllPlayer(text)`.

**Cộng / trừ điểm sự kiện** (`player.event_point` ↔ `PlayerEvent.eventPoint`, dùng ở shop đổi điểm Quy Lão Kame): nhập tên → chọn nhân vật → số (âm = trừ, không xuống dưới 0) → xác nhận. Online: `player.event.setEventPoint` (game tự lưu); offline: `modifyOffline` cột `event_point` (đã thêm vào danh sách trắng cột), cùng cơ chế khóa dòng / hỏi xác nhận như tab Nhân vật.

**Tỉ lệ EXP server** (`server.expserver` ↔ `Manager.RATE_EXP_SERVER`, `NPoint` nhân tiềm năng với biến này **mỗi lần tính**): nhập 1–100 → xác nhận → gán biến tĩnh (**có hiệu lực ngay**) và ghi `server.expserver` vào `Config.properties` (giữ khi khởi động lại).

---

## 5. Không làm chậm / treo server

- Mọi truy vấn DB và thao tác game chạy trên **một luồng nền duy nhất** `CPanel Worker` (daemon). Luồng giao diện Swing chỉ vẽ và hỏi xác nhận; kết quả trả về qua `SwingUtilities.invokeLater`.
- Vì chỉ 1 luồng, cpanel **chiếm tối đa 1 kết nối** HikariCP tại một thời điểm, transaction offline chỉ vài mili-giây.
- Làm mới danh sách online / thông số server chạy trên luồng `CPanel Timer` (daemon), chỉ đọc bộ nhớ qua bản sao `getPlayersSnapshot()`.
- Luồng daemon ⇒ không giữ tiến trình sống khi server `System.exit`.

---

## 6. Lưu ý bảo mật

> ⚠️ **MẬT KHẨU LƯU DẠNG CHỮ THƯỜNG (plain text).** Code đăng nhập hiện tại (`MrBlue.login()`: `select * from account where username = ? and password = ?`) so sánh thẳng chuỗi, nên cpanel **buộc phải** ghi mật khẩu dạng chữ thường để người chơi đăng nhập được. Ai đọc được DB / file dump / bản sao lưu là thấy toàn bộ mật khẩu. Nên chuyển sang băm (BCrypt/Argon2) ở cả web nạp, client đăng nhập lẫn cpanel **trong cùng một đợt**; khi đó chỉ cần sửa `AccountDao.create()` và `AccountDao.changePassword()`.

- Cửa sổ cpanel **không có đăng nhập riêng**: ai ngồi trước máy chủ (hoặc vào được Remote Desktop) là có toàn quyền. Khóa màn hình / giới hạn RDP; trên máy dùng chung thì đặt `server.cpanel=false`.
- Mật khẩu nhập qua ô `JPasswordField`, **không** in ra log. Log chỉ ghi tên tài khoản và loại thao tác.
- Mọi câu SQL dùng `PreparedStatement` có tham số. Chỗ duy nhất ghép tên cột (`data_inventory`/`data_task`/`items_bag`) là hằng số trong code, có kiểm tra danh sách trắng.
- Cấp quyền admin, ban, trừ VND/tiền, đặt lại nhiệm vụ, kick, thông báo toàn server, bảo trì, phát đồ (1 người / tất cả), phát vật phẩm nhiệm vụ, bật/tắt sự kiện, tạo/xóa giftcode, điểm sự kiện, tỉ lệ EXP đều **hỏi xác nhận**.
- Mọi thao tác ghi dòng `[CPANEL hh:mm:ss] ...` ra console và khung nhật ký. Nhật ký **không** ghi ra file (console của `run.bat`); muốn lưu lâu dài thì chuyển hướng output khi chạy.

---

## 7. Chỗ nghi ngờ / giới hạn

1. **Chưa chạy thử với DB thật.** Đã biên dịch sạch toàn dự án, chạy thử được đường headless / tắt bằng cấu hình và dựng được cửa sổ; nhưng máy thử dùng MySQL 8 (driver 5.1.23 không hỗ trợ `caching_sha2_password`) nên chưa kiểm tra các thao tác DB trên MariaDB 10.4. Cần thử: tạo tài khoản → đăng nhập game; tặng vật phẩm offline → đăng nhập xem hành trang.
2. **Kẽ hở rất nhỏ khi sửa offline**: `MrBlue.login()` đọc bảng `player` bằng `SELECT` thường (không chờ khóa). Nếu người chơi đăng nhập **đúng trong vài mili-giây** transaction của cpanel đang chạy, game có thể nạp bản cũ rồi lần lưu sau đè mất thay đổi. Cpanel kiểm tra lại sau khi ghi và cảnh báo nếu thấy nhân vật đã online. Muốn chặn tuyệt đối phải sửa `MrBlue.login()` (ngoài phạm vi).
3. **Sửa tiền online không có khóa**: `inventory.gold += delta` từ luồng cpanel trong khi luồng game cũng có thể đang cộng/trừ vàng của đúng người đó ⇒ khả năng mất một lần cập nhật (rất hiếm; game cũng không khóa khi các luồng mạng sửa tiền).
4. **Đặt lại nhiệm vụ** dùng `TaskService.switchTaskMain()`; nhóm khác đang sửa `TaskService` — nếu hàm này đổi tên/chữ ký thì `CharacterOps.resetTask()` sẽ không biên dịch được, cần sửa theo. `switchTaskMain` gọi `recheckPassiveSubTask` nên bước 0 có thể được tính hoàn thành ngay (VD bước kiểm tra sức mạnh).
5. **Nhân vật offline có `last_time_login > last_time_logout`** thường là do server từng sập (không ghi được giờ thoát) — khi đó cpanel luôn hỏi xác nhận trước khi ghi, bấm Đồng ý là được.
6. **Lưu shop ký gửi** (`ConsignShopManager.save()`) làm `TRUNCATE` rồi ghi lại toàn bộ; nếu có người đang ký gửi/mua đúng lúc, vòng lặp có thể lỗi giữa chừng ⇒ bảng tạm thiếu dữ liệu cho tới lần lưu kế tiếp (dữ liệu trong bộ nhớ vẫn đủ, bảo trì sẽ lưu lại). Đây là hàm có sẵn, cpanel chỉ gọi.
7. `database.max=1` trong `Config.properties` ⇒ pool HikariCP chỉ **1 kết nối** dùng chung cho cả server. Mọi truy vấn của cpanel (dù ngắn) vẫn phải xếp hàng với game. Nên tăng lên 5–10.
8. Nút **Bảo trì** gọi `ServerManager.close()` vốn chạy `cmd /c start restart_server.bat` — chỉ đúng trên Windows (giống lệnh `bt` hiện có).
9. Tài khoản mới tạo chưa có nhân vật; người chơi đăng nhập game lần đầu sẽ được tạo nhân vật như bình thường. Danh sách tài khoản chỉ hiện 500 dòng mới nhất — lọc theo tên để tìm tài khoản cũ.
10. Đổi quyền admin khi đang online có hiệu lực ngay (gán `session.isAdmin`), khác ghi chú ở doc 15 §1 ("phải đăng nhập lại") vốn nói về việc sửa tay trong DB.
11. **Buff đồ / sự kiện chưa chạy thử với DB và client thật** (cùng lý do mục 1). Đã biên dịch sạch và chạy thử headless phần không cần DB: ghi `Config.properties` giữ chú thích, JSON `items_bag` offline, JSON `detail` giftcode, đọc/ghi mẫu buff, dựng tab Buff đồ.
12. **Tắt sự kiện lúc chạy** gọi `ChangeMapService.exitMap(boss)` từ luồng cpanel trong khi luồng map có thể đang duyệt danh sách người chơi của khu — cùng mức rủi ro với các lệnh admin sẵn có (game không khóa danh sách này). Danh sách boss của manager là `ArrayList` không khóa: thêm/bớt lúc manager đang duyệt có thể gây `ConcurrentModificationException`, bị `BossManager.run()` nuốt và chạy lại ở nhịp sau (không sập). Lân con đang "đi theo" người chơi khi bị tắt thì người đó mất trạng thái nhận thưởng.
13. **Bật Hùng Vương mặc định giờ có boss thật** (§4.6.1). Chưa kiểm chứng phần thưởng/AI của Thủy Tinh/Sơn Tinh khi chạy thật vì trước giờ chúng chưa từng xuất hiện. Trung thu: 2123/2124 không có trong `item_template` gốc (cpanel chỉ cảnh báo, không chặn).
14. **Giftcode trong bộ nhớ**: `GiftCodeManager.listGiftCode` là `ArrayList` không khóa phía game; tạo/xóa code đúng lúc có người đang nhập code có thể làm lần nhập đó lỗi (`ConcurrentModificationException`, chỉ ảnh hưởng 1 lần nhập). Hạn dùng: game so `datecreate > expired` chứ **không so với giờ hiện tại** (doc 16 §13) ⇒ code do cpanel tạo **không tự hết hạn** cho tới lần restart đầu tiên sau ngày hết hạn (cần có người dùng code sau hạn để `datecreate` bị `ON UPDATE` đổi). Sửa triệt để phải sửa `GiftCode.timeCode()` (ngoài phạm vi). Vàng từ giftcode bị game kẹp 2 tỷ, ngọc/hồng ngọc 200 triệu.
15. **Mẫu "Set kích hoạt" mặc định**: giá trị giáp 47:2 lấy theo áo mặc định của nhân vật mới (docs 06 §2.3), không phải option shop; nếu muốn giống `createItemSKH` hoàn toàn thì bấm "Nạp option shop" rồi lưu lại mẫu. `createItemSKH` gắn option 30 với param 1 (không phải 0) — cpanel làm theo.
16. **Id 2000–2031 = vật phẩm nhiệm vụ** là theo yêu cầu của đợt nhiệm vụ mới (`SRC/sql/patch/01-vat-pham-moi.sql`); nếu nhóm nhiệm vụ đổi dải id thì sửa `BuffOps.QUEST_ITEM_MIN/MAX`.
17. Tắt sự kiện không đụng tới vật phẩm/điểm người chơi đã nhận, top sự kiện, NPC có sẵn trong `map_template` (VD NPC Hùng Vương map 183–185 vẫn còn khi tắt `hung_vuong`).
18. `server.expserver` là `byte` trong code ⇒ cpanel giới hạn 1–100. `Manager` chỉ đọc khóa này lúc khởi động; cpanel vừa gán biến vừa ghi file nên hai bên luôn khớp.
