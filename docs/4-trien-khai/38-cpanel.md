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
| `SRC/src/nro/models/server/ServerManager.java` | **Sửa đúng 1 chỗ**: trong `main()`, ngay trước `activeCommandLine()` gọi `nro.models.cpanel.CPanel.startIfEnabled();` |
| `SRC/Config.properties` | Thêm khóa `server.cpanel=true` |

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
│ [Tài khoản] [Người chơi online] [Nhân vật] [Server]                               │
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
- Cấp quyền admin, ban, trừ VND/tiền, đặt lại nhiệm vụ, kick, thông báo toàn server, bảo trì đều **hỏi xác nhận**.
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
