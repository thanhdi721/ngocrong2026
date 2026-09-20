# 24c — Sửa lỗi bảo mật và mất dữ liệu (đã thi công)

> Nhật ký thi công nhóm **lỗ hổng quyền admin + mất/lệch dữ liệu người chơi**. Chủ dự án đã duyệt sửa luôn.
> Nguồn mô tả lỗi: [15 — Lệnh admin / GM](../1-he-thong-hien-tai/15-lenh-admin-gm.md) · [01 — Kiến trúc server & network](../1-he-thong-hien-tai/01-kien-truc-server-network.md) · [02 — Database](../1-he-thong-hien-tai/02-database.md)
> Đường dẫn Java viết tắt `models/` = `SRC/src/nro/models/`.
> **Chưa biên dịch kiểm tra** (máy thi công chưa có JDK — `javac` chỉ là stub của macOS) — cần `ant`/`javac` chạy lại trước khi lên server.
> Mọi chỗ sửa đều có comment `// FIX: …` ngay tại dòng để tra ngược.

## Mục lục

1. [Lỗ hổng quyền admin ở gói tin −30 sub 64 (menu Ban / Phát đệ tử)](#1-lỗ-hổng-quyền-admin-ở-gói-tin-30-sub-64-menu-ban--phát-đệ-tử)
2. [Lệnh chat `part` ai cũng gọi được, ghi đè file dữ liệu](#2-lệnh-chat-part-ai-cũng-gọi-được-ghi-đè-file-dữ-liệu)
3. [Ban người chơi ghi `ban = 0` nên không có tác dụng](#3-ban-người-chơi-ghi-ban--0-nên-không-có-tác-dụng)
4. [Không có tự lưu định kỳ — server sập là mất tiến trình](#4-không-có-tự-lưu-định-kỳ--server-sập-là-mất-tiến-trình)
5. [`data_item_time` ghi và đọc lệch thứ tự từ phần tử 12](#5-data_item_time-ghi-và-đọc-lệch-thứ-tự-từ-phần-tử-12)
6. [`data_item_event` ghi 6 phần tử nhưng đọc 16 → reset mỗi lần load](#6-data_item_event-ghi-6-phần-tử-nhưng-đọc-16--reset-mỗi-lần-load)
7. [`masterDoesNotAttack` đọc nhầm tên cột](#7-masterdoesnotattack-đọc-nhầm-tên-cột)
8. [Điểm top bị ghi đè về 0 khi nạp nhân vật offline](#8-điểm-top-bị-ghi-đè-về-0-khi-nạp-nhân-vật-offline)
9. [`resetNhanQuaHangNgay` hard-code database `ngocrong`](#9-resetnhanquahangngay-hard-code-database-ngocrong)
10. [Dữ liệu cũ của người chơi sẽ được hiểu lại thế nào](#10-dữ-liệu-cũ-của-người-chơi-sẽ-được-hiểu-lại-thế-nào)
11. [Tổng hợp file đã đụng](#11-tổng-hợp-file-đã-đụng)
12. [Điểm cần chủ dự án xác nhận](#12-điểm-cần-chủ-dự-án-xác-nhận)

---

## 1. Lỗ hổng quyền admin ở gói tin −30 sub 64 (menu Ban / Phát đệ tử)

| | |
|---|---|
| **File** | `models/services/SubMenuService.java`, `models/npc/NpcFactory.java` |
| **Hàm** | `SubMenuService.controller(Player, int, int)` · `NpcFactory.createNpcConMeo()` → `confirmMenu(Player, int)` nhánh `ConstNpc.BAN_PLAYER` / `ConstNpc.BUFF_PET` |
| **Dòng** | `SubMenuService` 41–44, 55–59, 67–71 · `NpcFactory` 458–482 (sau sửa) |
| **Mức độ** | Nghiêm trọng — leo thang đặc quyền |

Đường đi của lỗi: client gửi `cmd -30`, `sub 64`, kèm `int playerId` + `short menuId`.
`Controller.messageSubCommand` (dòng 835–839) chuyển thẳng vào `SubMenuService.controller`
mà **không hề kiểm tra `isAdmin`**; `menuId = 500` mở menu xác nhận ban, `menuId = 501` mở menu
phát đệ tử. Bấm "Đồng ý" thì `NpcFactory` gọi `PlayerService.banPlayer` / `PetService.createNormalPet`
cho **bất kỳ người chơi nào**. Client bị sửa có thể kick (qua cơ chế ban) người khác.

### Code trước

```java
    public void controller(Player player, int playerTarget, int menuId) {
        Player plTarget = Client.gI().getPlayer(playerTarget);
        switch (menuId) {
            case BAN:
                if (plTarget != null) {
                    String[] selects = new String[]{"Đồng ý", "Hủy"};
                    NpcService.gI().createMenuConMeo(player, ConstNpc.BAN_PLAYER, -1,
                            "Bạn có chắc chắn muốn ban " + plTarget.name, selects, plTarget);
                }
                break;
            case BUFF_PET:
                if (plTarget != null) {
                    ...
                }
                break;
```

```java
                    case ConstNpc.BAN_PLAYER -> {
                        if (select == 0) {
                            PlayerService.gI().banPlayer((Player) PLAYERID_OBJECT.get(player.id));
                            Service.gI().sendThongBao(player, "Ban người chơi " + ... + " thành công");
                        }
                    }
```

### Code sau

```java
    public void controller(Player player, int playerTarget, int menuId) {
        // FIX: chống gói tin giả mạo - bỏ qua nếu chưa có nhân vật trong phiên
        if (player == null) {
            return;
        }
        Player plTarget = Client.gI().getPlayer(playerTarget);
        switch (menuId) {
            case BAN:
                // FIX: chỉ admin mới được mở menu ban người chơi (cmd -30 sub 64, menuId 500)
                if (!player.isAdmin()) {
                    Service.gI().sendThongBao(player, "Không đủ quyền hạn!");
                    break;
                }
                if (plTarget != null) {
                    ...
                }
                break;
            case BUFF_PET:
                // FIX: chỉ admin mới được mở menu phát đệ tử (cmd -30 sub 64, menuId 501)
                if (!player.isAdmin()) {
                    Service.gI().sendThongBao(player, "Không đủ quyền hạn!");
                    break;
                }
                ...
```

```java
                    case ConstNpc.BAN_PLAYER -> {
                        if (select == 0) {
                            // FIX: kiểm tra quyền admin ngay tại handler (phòng thủ nhiều lớp)
                            if (!player.isAdmin()) {
                                Service.gI().sendThongBao(player, "Không đủ quyền hạn!");
                            } else {
                                PlayerService.gI().banPlayer((Player) PLAYERID_OBJECT.get(player.id));
                                Service.gI().sendThongBao(player, "Ban người chơi " + ... + " thành công");
                            }
                        }
                    }
```

(nhánh `ConstNpc.BUFF_PET` sửa y hệt, dùng `if/else` chứ không dùng `break` vì đây là
`switch` mũi tên `->`, Java cấm `break` trong khối của switch rule.)

- **Lý do:** chặn tại cả 2 lớp — lớp mở menu (`SubMenuService`) và lớp xác nhận (`NpcFactory`).
  Sửa một lớp là đủ để bịt lỗ hổng hiện tại, nhưng lớp thứ hai giữ an toàn nếu sau này có
  đường khác mở `ConstNpc.BAN_PLAYER`.
- **Rủi ro:** các `menuId` khác (`OTT` 502, `CUU_SAT` 503) **không** đụng đến, người chơi thường
  vẫn dùng bình thường. Nếu trước đây có tính năng cho người chơi thường phát đệ tử qua menu 501
  thì tính năng đó sẽ ngừng — theo tài liệu 15 thì không có đường mở hợp lệ nào (hàm `showMenu`
  đang bị comment) nên coi như không ảnh hưởng.
- **Cách test:** (a) đăng nhập tài khoản thường, dùng client sửa gửi `-30/64` với `menuId = 500`
  → phải nhận "Không đủ quyền hạn!" và **không** thấy menu xác nhận. (b) đăng nhập admin
  (`account.is_admin = 1`) → vẫn mở được menu ban và ban thành công.

---

## 2. Lệnh chat `part` ai cũng gọi được, ghi đè file dữ liệu

| | |
|---|---|
| **File** | `models/services/Service.java`, `models/server/Manager.java` |
| **Hàm** | `Service.chat(Player, String)` · `Manager.loadPart()` |
| **Dòng** | `Service` 686–693 · `Manager` 244–296 (sau sửa) |
| **Mức độ** | Cao — DoS / phá dữ liệu |

Bất kỳ ai gõ `part` trong khung chat đều khiến server đọc lại toàn bộ bảng `part` (2099 dòng),
**ghi đè file nhị phân `data/update_data/part`**, rồi gửi lại 6 khối data cho chính người đó.
Spam lệnh này = spam truy vấn DB + ghi file + băng thông.

### Code trước

```java
    public void chat(Player player, String text) {
        if (text.equals("part")) {
            Manager.loadPart();
            DataGame.updateData(player.getSession());
            return;
        }
```

```java
            DataOutputStream dos = new DataOutputStream(new FileOutputStream("data/update_data/part"));
            dos.writeShort(parts.size());
            for (Part part : parts) { ... }
            dos.flush();
            dos.close();
```

### Code sau

```java
    public void chat(Player player, String text) {
        // FIX: chỉ admin mới được nạp lại part (ghi đè file data/update_data/part)
        if (text.equals("part") && player != null && player.isAdmin() && player.getSession() != null) {
            Manager.loadPart();
            DataGame.updateData(player.getSession());
            return;
        }
```

```java
            // FIX: kiểm tra dữ liệu đầu vào - không ghi đè file part khi bảng part rỗng/lỗi
            if (parts.isEmpty()) {
                Logger.error("loadPart: bang 'part' khong co du lieu, giu nguyen file data/update_data/part\n");
                return;
            }
            // FIX: ghi ra file tạm rồi mới thay thế để không làm hỏng file part khi ghi dở
            java.io.File fileTmp = new java.io.File("data/update_data/part.tmp");
            java.io.File filePart = new java.io.File("data/update_data/part");
            try (DataOutputStream dos = new DataOutputStream(new FileOutputStream(fileTmp))) {
                dos.writeShort(parts.size());
                for (Part part : parts) { ... }
                dos.flush();
            }
            java.nio.file.Files.move(fileTmp.toPath(), filePart.toPath(),
                    java.nio.file.StandardCopyOption.REPLACE_EXISTING);
```

- **Lý do:** `Service.chat` cũng được NPC/boss/pet gọi (`Service.gI().chat(npc, "...")`), các đối
  tượng đó không có `session` nên `isAdmin()` trả `false` — an toàn. Người chơi thường gõ `part`
  giờ chỉ là câu chat bình thường (rơi xuống nhánh phát ra map), không còn tác dụng phụ.
  `Manager.loadPart()` thêm 2 lớp bảo vệ dữ liệu: không ghi khi bảng rỗng, và ghi file tạm rồi
  `move` đè — nếu quá trình ghi lỗi giữa chừng thì file `part` cũ vẫn nguyên vẹn.
- **Rủi ro:** `Files.move` với `REPLACE_EXISTING` là thao tác cùng thư mục nên nguyên tử trên
  cùng ổ đĩa. Thư mục `data/update_data/` phải có quyền ghi (đã có sẵn vì bản cũ ghi thẳng vào đó).
  Sẽ xuất hiện thêm file tạm `part.tmp` trong chốc lát.
- **Cách test:** (a) tài khoản thường gõ `part` → chỉ thấy chữ "part" hiện trên đầu nhân vật,
  `data/update_data/part` **không đổi** thời gian sửa. (b) admin gõ `part` → file được cập nhật,
  client nhận lại data. (c) đổi tên bảng `part` trong DB rồi gõ `part` bằng admin → log báo lỗi,
  file cũ vẫn còn.

---

## 3. Ban người chơi ghi `ban = 0` nên không có tác dụng

| | |
|---|---|
| **File** | `models/services/PlayerService.java`, `models/player/Player.java` |
| **Hàm** | `PlayerService.banPlayer(Player)` · `Player.update()` |
| **Dòng** | `PlayerService` 252–270 · `Player` 453–457 (sau sửa) |
| **Mức độ** | Cao — chức năng quản trị vô hiệu |

### Code trước

```java
    public void banPlayer(Player playerBaned) {
        try {
            LocalManager.executeUpdate("update account set ban = 0 where id = ? and username = ?",
                    playerBaned.getSession().userId, playerBaned.getSession().uu);
        } catch (Exception e) {
        }
        ...
    }
```

```java
                if ((this.zone != null && !MapService.gI().isHome(this.zone.map.mapId)) || (!this.isPl() && this.zone == null)) {
                    if (isPl() && idMark != null && idMark.isBan() && Util.canDoWithTime(idMark.getLastTimeBan(), 5000)) {
                        Client.gI().kickSession(session);
                        return;
                    }
```

### Code sau

```java
    public void banPlayer(Player playerBaned) {
        if (playerBaned == null || playerBaned.getSession() == null) {
            return;
        }
        try {
            // FIX: ghi ban = 1 (trước đây ghi 0 nên tài khoản không hề bị khóa)
            LocalManager.executeUpdate("update account set ban = 1 where id = ? and username = ?",
                    playerBaned.getSession().userId, playerBaned.getSession().uu);
        } catch (Exception e) {
            // FIX: không nuốt lỗi - nếu không ghi được DB thì admin phải biết
            Logger.logException(PlayerService.class, e);
        }
        ...
    }
```

```java
                // FIX: kick người bị ban ở mọi bản đồ (trước đây chỉ kick khi không đứng ở map nhà)
                if (isPl() && idMark != null && idMark.isBan() && Util.canDoWithTime(idMark.getLastTimeBan(), 5000)) {
                    Client.gI().kickSession(session);
                    return;
                }
                if ((this.zone != null && !MapService.gI().isHome(this.zone.map.mapId)) || (!this.isPl() && this.zone == null)) {
```

- **Lý do:**
  1. `ban = 1` mới là giá trị `MrBlue.login()` kiểm tra (`if (rs.getBoolean("ban"))` → báo
     "Tài khoản này đang bị khóa..." và không cho vào game). Câu `UPDATE` chạy ngay lúc ban nên
     trạng thái đã nằm trong DB trước khi người chơi bị ngắt kết nối 5 giây sau — **đăng nhập lại
     là bị chặn ngay**, không cần khởi động lại server.
  2. Dời khối kick ra khỏi nhánh `!isHome(...)`: trước đây người bị ban đứng ở map nhà
     (21/22/23) sẽ **không bao giờ bị kick**, cứ đứng đó chơi tiếp đến khi tự thoát.
  3. Không nuốt `Exception` nữa để admin biết khi câu `UPDATE` thất bại.
- **Rủi ro:** khối kick giờ chạy mỗi tick cho mọi người chơi (trước chỉ chạy ngoài map nhà);
  chi phí là 3 phép so sánh, không đáng kể. Điều kiện `isPl()` giữ nguyên nên boss/pet/bot
  không bị ảnh hưởng.
- **Cách test:** admin ban một tài khoản đang đứng ở map nhà → sau ~5 giây mất kết nối;
  kiểm tra `select ban from account where username = '...'` phải bằng `1`; đăng nhập lại
  phải nhận thông báo "Tài khoản này đang bị khóa...". Mở khóa bằng `update account set ban = 0`.

---

## 4. Không có tự lưu định kỳ — server sập là mất tiến trình

| | |
|---|---|
| **File** | `models/server/ServerManager.java`, `models/database/PlayerDAO.java`, `models/server/Client.java`, `models/player/Player.java` |
| **Hàm** | `ServerManager.startAutoSaver()` / `autoSavePlayers()` / `stopAutoSaver()` · `PlayerDAO.updatePlayer()` / `autoSavePlayer()` / `doUpdatePlayer()` · `Client.getPlayersSnapshot()` · `Player.lastTimeAutoSave` |
| **Dòng** | `ServerManager` 69–76, 153, 169–232, 354 · `PlayerDAO` 352–396 · `Client` 176–194 · `Player` 175–176 |
| **Mức độ** | Cao — mất dữ liệu | **Đây là mục rủi ro nhất của đợt sửa này** |

Trước đây `PlayerDAO.updatePlayer` **chỉ** được gọi khi: người chơi thoát/rớt mạng
(`Client.remove`), bị kick, hoặc bảo trì (`Client.close`). Server sập/cúp điện ⇒ toàn bộ
tiến trình từ lúc đăng nhập bị mất.

### Code sau (phần chính)

`ServerManager` — luồng riêng, daemon, 1 thread:

```java
    // FIX: tự lưu định kỳ dữ liệu người chơi để server sập không mất tiến trình
    /** Mỗi nhân vật được lưu lại sau khoảng thời gian này (5 phút). */
    public static final long AUTO_SAVE_INTERVAL = 5 * 60 * 1000L;
    /** Nhịp quét, 5 giây một lần -> tải được rải đều thay vì dồn 1 lúc. */
    private static final long AUTO_SAVE_TICK = 5000L;
    /** Số nhân vật tối đa được lưu trong 1 nhịp quét. */
    private static final int MAX_SAVE_PER_TICK = 10;
    private ScheduledExecutorService autoSaver;
```

```java
    private void startAutoSaver() {
        autoSaver = Executors.newSingleThreadScheduledExecutor(r -> {
            Thread t = new Thread(r, "Auto Save Player");
            t.setDaemon(true);
            return t;
        });
        autoSaver.scheduleWithFixedDelay(this::autoSavePlayers,
                AUTO_SAVE_INTERVAL, AUTO_SAVE_TICK, TimeUnit.MILLISECONDS);
    }

    private void autoSavePlayers() {
        // Đang bảo trì thì Client.close() đang kick & lưu từng người, không lưu chồng
        if (!isRunning || Maintenance.isRunning) {
            return;
        }
        try {
            long now = System.currentTimeMillis();
            int saved = 0;
            List<Player> players = Client.gI().getPlayersSnapshot();
            for (Player pl : players) {
                if (saved >= MAX_SAVE_PER_TICK) break;
                if (pl == null || pl.isOffline || pl.beforeDispose || !pl.isPl()) continue;
                if (pl.zone == null || pl.zone.map == null) continue;   // đang đổi map
                if (pl.getSession() == null || !pl.getSession().joinedGame) continue;
                if (pl.idMark == null || !pl.idMark.isLoadedAllDataPlayer()) continue;
                if (now - pl.lastTimeAutoSave < AUTO_SAVE_INTERVAL) continue;
                pl.lastTimeAutoSave = now;
                try {
                    PlayerDAO.autoSavePlayer(pl);
                    saved++;
                } catch (Exception e) {
                    Logger.error("Loi tu luu nhan vat " + pl.name + ": " + e.getMessage() + "\n");
                }
            }
        } catch (Exception e) {
            Logger.logException(ServerManager.class, e);
        }
    }
```

`PlayerDAO` — tách thân hàm cũ thành `doUpdatePlayer`, thêm khoá theo từng nhân vật:

```java
    public static void updatePlayer(Player player) {
        if (player == null) {
            return;
        }
        synchronized (player) {
            doUpdatePlayer(player);
        }
    }

    public static void autoSavePlayer(Player player) {
        if (player == null || player.isOffline || player.beforeDispose) {
            return;
        }
        synchronized (player) {
            if (player.beforeDispose) {
                return;
            }
            if (player.zone == null || player.zone.map == null) {
                return;
            }
            player.mapIdBeforeLogout = player.zone.map.mapId;
            // MapService.getMapCanJoin() (được gọi bên trong doUpdatePlayer) có thể ghi đè
            // player.location khi nhân vật đang ở map phụ bản -> giữ lại toạ độ thật
            int x = player.location.x;
            int y = player.location.y;
            try {
                doUpdatePlayer(player);
            } finally {
                player.location.x = x;
                player.location.y = y;
            }
        }
    }

    private static void doUpdatePlayer(Player player) { /* thân hàm cũ, không đổi */ }
```

`Client` — bản sao danh sách an toàn:

```java
    public List<Player> getPlayersSnapshot() {
        List<Player> snapshot = new ArrayList<>();
        for (int i = players.size() - 1; i >= 0; i--) {
            try {
                Player pl = players.get(i);
                if (pl != null) {
                    snapshot.add(pl);
                }
            } catch (IndexOutOfBoundsException e) {
            }
        }
        return snapshot;
    }
```

### Cách tránh lưu trùng

| Tình huống | Cách xử lý |
|---|---|
| Người chơi đang thoát game | `Client.remove` đặt `player.beforeDispose = true` **trước khi** gọi `updatePlayer`. Luồng tự lưu bỏ qua mọi nhân vật có `beforeDispose == true` — kiểm tra **2 lần**: một lần trước khi lấy khoá (rẻ) và một lần **bên trong** `synchronized (player)` (chống đua). |
| Hai luồng cùng lưu 1 nhân vật | `synchronized (player)` bọc cả `updatePlayer` lẫn `autoSavePlayer`. Hai luồng không bao giờ chạy `doUpdatePlayer` đồng thời trên cùng một `Player`, và thứ tự ghi luôn nối tiếp nhau. Nếu tự lưu giành khoá trước, luồng thoát game chờ vài chục ms rồi ghi **bản cuối cùng đè lên** — kết quả DB luôn là bản mới nhất. |
| Bảo trì | `ServerManager.close()` gọi `stopAutoSaver()` **trước** `Client.gI().close()`; ngoài ra `autoSavePlayers()` tự thoát ngay khi `Maintenance.isRunning == true`. |
| Nhân vật offline nạp bằng `loadById` | `autoSavePlayer` bỏ qua `isOffline == true` (những nhân vật này không nằm trong `Client.players` nên thực tế không bao giờ gặp). |
| Bot / pet / boss | lọc bằng `pl.isPl()`. |

### Cách tránh chặn luồng game

- Chạy trên `ScheduledExecutorService` **một luồng riêng** tên `"Auto Save Player"`, đặt daemon.
  Không nằm trong `Player.update()`, `Zone` hay bất kỳ vòng lặp map/boss nào.
- **Rải tải:** quét mỗi 5 giây, mỗi nhịp chỉ lưu tối đa **10** nhân vật, và một nhân vật chỉ
  được lưu lại sau `AUTO_SAVE_INTERVAL` (5 phút) kể từ lần lưu trước. `lastTimeAutoSave` được
  khởi tạo bằng thời điểm tạo `Player` nên mốc lưu của mỗi người tự lệch nhau theo giờ đăng nhập
  — không có "cơn bão" ghi DB. Với 10 nhân vật/5 giây, hệ thống theo kịp tới 600 người online.
- Luồng bắt đầu chạy sau `AUTO_SAVE_INTERVAL` (không quét ngay lúc server vừa mở).
- Mọi lỗi được bắt ở 2 tầng (`try` quanh từng nhân vật và `try` quanh cả vòng lặp) nên một
  nhân vật lỗi không làm chết luồng tự lưu.
- Luồng thoát game/kick có thể **chờ** tối đa một lần ghi DB (`synchronized`) — thực tế vài chục ms,
  và bản thân hàm đó vốn đã ghi DB nên không phải chi phí mới.

### Rủi ro còn lại (cần theo dõi khi chạy thật)

1. **Đọc trạng thái trong lúc luồng game đang sửa.** `doUpdatePlayer` duyệt `inventory.itemsBag`,
   `itemsBody`… trong khi luồng game có thể thêm/bớt item ⇒ có thể ném
   `ConcurrentModificationException`. Hậu quả **không** phải hỏng dữ liệu: toàn bộ chuỗi JSON được
   dựng **trước** khi chạy câu `UPDATE`, nên hoặc ghi trọn vẹn, hoặc không ghi gì và chỉ log lỗi;
   5 phút sau tự thử lại. Nếu log xuất hiện nhiều, có thể hạ `MAX_SAVE_PER_TICK` hoặc bọc thêm khoá
   quanh `inventory` — nhưng đó là thay đổi lớn hơn, xin ý kiến chủ dự án trước.
2. **`mapIdBeforeLogout`.** Trường này trước đây **chỉ** được gán lúc thoát game; nếu tự lưu mà
   không gán thì mọi người sẽ được lưu ở map `0`. Vì vậy `autoSavePlayer` gán nó theo map hiện tại
   trước khi lưu. Logic sẵn có trong `doUpdatePlayer` vẫn đưa về map nhà nếu đang ở doanh trại /
   ngọc rồng đen / map không vào lại được.
3. **`MapService.getMapCanJoin()` có tác dụng phụ** — nó ghi đè `player.location.x/y` ở vài nhánh
   (bản đồ kho báu / khí gas / con đường rắn độc khi bang không còn phụ bản). Với người chơi đang
   online điều đó tương đương "dịch chuyển" nhân vật. Đã xử lý bằng cách lưu lại `x/y` trước và
   khôi phục trong khối `finally`. Toạ độ **ghi xuống DB** vẫn là toạ độ đọc trước khi gọi hàm đó,
   nên không đổi hành vi lưu.
4. Log `"... save successfully!"` sẽ xuất hiện đều đặn hơn (mỗi người 5 phút một dòng).

### Cách test

1. Đăng nhập, cày cho đổi vàng/EXP, chờ hơn 5 phút, rồi **kill −9** tiến trình server
   (mô phỏng server sập). Mở lại server, đăng nhập → tiến trình phải còn tới mốc tự lưu gần nhất.
2. Trong lúc chờ, xem log console phải có dòng `Auto save player thread started ...` và các dòng
   `Player <tên> save successfully!` rải đều, không dồn cục.
3. Đăng nhập 2 tài khoản, thoát 1 tài khoản đúng lúc gần mốc tự lưu → dữ liệu người thoát phải
   là dữ liệu cuối cùng (kiểm tra vàng/vị trí trong bảng `player`).
4. Kiểm tra vị trí: đứng ở một map thường (ví dụ map 5), chờ tự lưu, sập server, đăng nhập lại →
   phải vào lại đúng map đó chứ không phải map 0 hay map nhà.
5. Bảo trì bằng lệnh `bt` trong console → không được xuất hiện log tự lưu sau khi bắt đầu bảo trì.

---

## 5. `data_item_time` ghi và đọc lệch thứ tự từ phần tử 12

| | |
|---|---|
| **File** | `models/database/MrBlue.java` (đọc) |
| **Hàm** | `MrBlue.loadPlayer(LocalResultSet, boolean)` — khối `//data item time` |
| **Dòng** | 596–662, 682–689 (sau sửa) |
| **Mức độ** | Trung bình — sai dữ liệu buff |

`PlayerDAO.doUpdatePlayer` (dòng ~594–631) ghi mảng 32 phần tử theo thứ tự:

```
0 Bổ huyết  1 Bổ huyết 2  2 Bổ khí  3 Bổ khí 2  4 Giáp Xên  5 Giáp Xên 2
6 Cuồng nộ  7 Cuồng nộ 2  8 Ẩn danh  9 Ẩn danh 2  10 Mở sức mạnh  11 Máy dò
12 Cỏ bốn lá  13 Kho báu x2  14 Bùa Santa  15 Bữa ăn  16 icon bữa ăn
17 TDLT  18 CMS  19 GTPT  20 ĐK  21 RX  22 Bữa ăn 2  23 icon bữa ăn 2
24 dự phòng  25 NCD  26 Nước mía 1  27 Nước mía 2  28 Nước mía 3  29 Kilis  30-31 dự phòng
```

Hàm đọc lại lệch **1 ô** từ index 12 trở đi (đọc Kho báu x2 ở 12, bỏ qua 13, Bữa ăn ở 14…),
lại còn đọc Nước mía 3 ở `get(28)` — tức trùng với Nước mía 2. Kết quả: thời gian buff bị gán
nhầm sang item khác sau mỗi lần đăng nhập lại.

### Code trước

```java
            if (dataArray.size() > 12) {
                timeKhoBauX2 = Integer.parseInt(String.valueOf(dataArray.get(12)));
            }
            if (dataArray.size() > 13) {
            }
            if (dataArray.size() > 14) {
                timeMeal = Integer.parseInt(String.valueOf(dataArray.get(14)));
            }
            ...
            if (dataArray.size() > 25) {
                timeBuaSanta = Integer.parseInt(String.valueOf(dataArray.get(25)));
            }
            if (dataArray.size() > 26) {
                timeKilis = (int) Long.parseLong(String.valueOf(dataArray.get(26)));
            }
            ...
            if (dataArray.size() > 29) {
                timeNuocMia3 = (int) Long.parseLong(String.valueOf(dataArray.get(28)));
            }
```

### Code sau

```java
            // FIX: đọc đúng thứ tự mà PlayerDAO.updatePlayer đã ghi (trước đây lệch 1 ô từ index 12
            // nên thời gian buff bị gán nhầm sang item khác sau khi đăng nhập lại)
            if (dataArray.size() > 12) {
                timeCoBonLa = Long.parseLong(String.valueOf(dataArray.get(12)));
            }
            if (dataArray.size() > 13) {
                timeKhoBauX2 = Integer.parseInt(String.valueOf(dataArray.get(13)));
            }
            if (dataArray.size() > 14) {
                timeBuaSanta = Integer.parseInt(String.valueOf(dataArray.get(14)));
            }
            if (dataArray.size() > 15) {
                timeMeal = Integer.parseInt(String.valueOf(dataArray.get(15)));
            }
            ... (16 icon bữa ăn, 17 TDLT, 18 CMS, 19 GTPT, 20 ĐK, 21 RX, 22 bữa ăn 2, 23 icon) ...
            if (dataArray.size() > 25) {
                timeUseNCD = Integer.parseInt(String.valueOf(dataArray.get(25)));
            }
            if (dataArray.size() > 26) {
                timeNuocMia1 = Long.parseLong(String.valueOf(dataArray.get(26)));
            }
            if (dataArray.size() > 27) {
                timeNuocMia2 = Long.parseLong(String.valueOf(dataArray.get(27)));
            }
            if (dataArray.size() > 28) {
                timeNuocMia3 = Long.parseLong(String.valueOf(dataArray.get(28)));
            }
            if (dataArray.size() > 29) {
                timeKilis = Long.parseLong(String.valueOf(dataArray.get(29)));
            }
```

Thêm phần khôi phục mốc thời gian cho Kilis / Nước mía (trước chỉ bật cờ `isUse...` mà không
đặt `lastTimeUse...`, nên buff bị tắt ngay ở tick kế tiếp):

```java
            // FIX: khôi phục cả mốc thời gian của Kilis / Nước mía
            player.itemTime.lastTimeUseKilis = System.currentTimeMillis() - (ItemTime.TIME_KILIS - timeKilis);
            player.itemTime.lastTimeUseNuocMia1 = System.currentTimeMillis() - (ItemTime.TIME_NUOC_MIA1 - timeNuocMia1);
            player.itemTime.lastTimeUseNuocMia2 = System.currentTimeMillis() - (ItemTime.TIME_NUOC_MIA2 - timeNuocMia2);
            player.itemTime.lastTimeUseNuocMia3 = System.currentTimeMillis() - (ItemTime.TIME_NUOC_MIA3 - timeNuocMia3);
```

- **Lý do:** chọn sửa **bên đọc** chứ không sửa bên ghi, vì toàn bộ dữ liệu đang có trong DB được
  ghi theo thứ tự của `PlayerDAO`. Sửa bên đọc là cách duy nhất khiến dữ liệu cũ được hiểu đúng
  mà **không cần migrate SQL**.
- **Rủi ro:** các mốc `12–15` vốn chưa từng được đọc đúng, nên sau khi sửa người chơi có thể
  "bỗng dưng" còn Bùa Santa / Kho báu x2 / Cỏ bốn lá đang chạy dở (đúng như họ đã mua). Đây là
  hành vi **đúng**, nhưng cần báo trước nếu chủ dự án thấy lạ. Cờ `isUseCoBonLa` vẫn không được
  bật khi load — xem mục 12.
- **Cách test:** dùng Bùa Santa (hoặc Kho báu x2), thoát game, xem cột `data_item_time` phần tử
  thứ 14 (bắt đầu từ 0) phải > 0, đăng nhập lại → icon buff Santa còn nguyên với thời gian còn lại
  gần đúng, và **không** biến thành buff khác.

---

## 6. `data_item_event` ghi 6 phần tử nhưng đọc 16 → reset mỗi lần load

| | |
|---|---|
| **File** | `models/database/PlayerDAO.java` (ghi), `models/database/MrBlue.java` (đọc) |
| **Hàm** | `PlayerDAO.doUpdatePlayer` khối `//Data item event` · `MrBlue.loadPlayer` khối `//data item event` |
| **Dòng** | `PlayerDAO` 906–924 · `MrBlue` 1061–1097 (sau sửa) |
| **Mức độ** | Trung bình — giới hạn item sự kiện bị reset |

Bên ghi chỉ ghi 6 phần tử, bên đọc đọc tới `get(15)` ⇒ `IndexOutOfBoundsException` ⇒ khối
`catch` reset **toàn bộ 16 giá trị về 0** ⇒ mỗi lần đăng nhập, giới hạn rơi item sự kiện
(Tất võ giáng sinh, Hột hư, Bánh nướng, Bánh quy, Kẹo người tuyết, Cá tuyết, Chuông đồng,
Kẹo đường) lại được làm mới.

### Code sau — bên ghi (`PlayerDAO`)

```java
                //Data item event
                // FIX: ghi đủ 16 phần tử đúng như MrBlue.loadPlayer đang đọc
                // (trước đây chỉ ghi 6 -> load ném exception -> reset toàn bộ giới hạn item sự kiện về 0)
                dataArray.add(player.itemEvent.remainingTVGSCount);
                dataArray.add(player.itemEvent.lastTVGSTime);
                dataArray.add(player.itemEvent.remainingHHCount);
                dataArray.add(player.itemEvent.lastHHTime);
                dataArray.add(player.itemEvent.remainingBNCount);
                dataArray.add(player.itemEvent.lastBNTime);
                dataArray.add(player.itemEvent.remainingBanhQuyCount);
                dataArray.add(player.itemEvent.lastItemBanhQuy);
                dataArray.add(player.itemEvent.remainingKeoNguoiTuyetCount);
                dataArray.add(player.itemEvent.lastItemKeoNguoiTuyet);
                dataArray.add(player.itemEvent.remainingCaTuyetCount);
                dataArray.add(player.itemEvent.lastItemCaTuyet);
                dataArray.add(player.itemEvent.remainingChuongDongCount);
                dataArray.add(player.itemEvent.lastItemChuongDong);
                dataArray.add(player.itemEvent.remainingKeoDuongCount);
                dataArray.add(player.itemEvent.lastItemKeoDuong);
```

### Code sau — bên đọc (`MrBlue`)

```java
                // FIX: đọc theo số phần tử thực tế - dữ liệu cũ chỉ có 6 phần tử,
                // trước đây get(6) ném exception làm reset sạch cả 6 giá trị đầu mỗi lần load
                if (dataArray.size() > 1) {
                    player.itemEvent.remainingTVGSCount = Integer.parseInt(dataArray.get(0).toString());
                    player.itemEvent.lastTVGSTime = Long.parseLong(dataArray.get(1).toString());
                }
                if (dataArray.size() > 3) { ... HH ... }
                if (dataArray.size() > 5) { ... BN ... }
                if (dataArray.size() > 7) { ... Bánh quy ... }
                if (dataArray.size() > 9) { ... Kẹo người tuyết ... }
                if (dataArray.size() > 11) { ... Cá tuyết ... }
                if (dataArray.size() > 13) { ... Chuông đồng ... }
                if (dataArray.size() > 15) { ... Kẹo đường ... }
```

- **Lý do:** sửa **cả hai đầu**. Bên ghi ghi đủ 16 để từ nay không mất dữ liệu; bên đọc đọc theo
  `size()` thực tế để dữ liệu **cũ chỉ có 6 phần tử vẫn nạp được 6 giá trị đầu**, phần còn lại
  giữ mặc định 0 thay vì reset sạch. Cột `data_item_event` là `varchar(1000)` — 16 phần tử
  (số đếm + timestamp 13 chữ số) khoảng 120–140 ký tự, còn rất dư.
- **Rủi ro:** khối `catch` phía dưới vẫn giữ nguyên (reset về 0) cho trường hợp JSON hỏng hẳn.
  Nếu `data_item_event` là `'[]'` (mặc định cột) thì `size() == 0`, tất cả giữ 0 — đúng như cũ.
- **Cách test:** rơi item sự kiện đến khi hết lượt trong ngày, thoát game, kiểm tra
  `data_item_event` phải là mảng 16 phần tử, đăng nhập lại → **không** được rơi thêm.

---

## 7. `masterDoesNotAttack` đọc nhầm tên cột

| | |
|---|---|
| **File** | `models/database/MrBlue.java` |
| **Hàm** | `MrBlue.loadPlayer` — khối `// Sư phụ không tấn công` |
| **Dòng** | 999–1008 (sau sửa) |
| **Mức độ** | Thấp — cờ luôn `false` |

### Code trước

```java
                player.doesNotAttack = rs.getBoolean("masterDoesAttack");
```

### Code sau

```java
                // FIX: đọc đúng tên cột `masterDoesNotAttack` (trước đây đọc `masterDoesAttack` nên luôn lỗi -> false)
                player.doesNotAttack = rs.getBoolean("masterDoesNotAttack");
```

- **Lý do:** cột trong bảng `player` tên là `masterDoesNotAttack` (xem `PlayerDAO` dòng 340 và
  983 đều ghi đúng tên này). Tên sai ⇒ `SQLException` ⇒ rơi vào `catch` ⇒ luôn `false`.
- **Rủi ro:** từ nay cờ "sư phụ không tấn công" **thật sự** được khôi phục sau khi đăng nhập lại.
  Người chơi đã bật cờ này trước đây sẽ thấy đệ tử hành xử khác trước (đúng với cài đặt của họ).
- **Cách test:** bật "sư phụ không tấn công" cho đệ tử, thoát game, kiểm tra cột
  `masterDoesNotAttack = 1`/`'true'`, đăng nhập lại → trạng thái phải giữ nguyên.

---

## 8. Điểm top bị ghi đè về 0 khi nạp nhân vật offline

| | |
|---|---|
| **File** | `models/database/MrBlue.java` |
| **Hàm** | `MrBlue.loadPlayer(LocalResultSet, boolean)` — ngay sau khối thông tin cơ bản |
| **Dòng** | 199–212 (sau sửa) |
| **Mức độ** | Cao — mất điểm đua top |

`point_sukien`, `point_sukien1`, `point_sukien2`, `thachdauwhis`, `point_maydam`,
`total_damage_maydam` trước đây **chỉ** được đọc trong `MrBlue.login()` (dòng 119–124), trong khi
`PlayerDAO` **luôn** ghi chúng. Bất cứ chỗ nào nạp nhân vật offline bằng `MrBlue.loadById()` rồi
`PlayerDAO.updatePlayer()` sẽ ghi giá trị mặc định `0` đè lên điểm thật:
`BlackBallWar.java` (dòng 119–122, thưởng ngọc cho thành viên bang offline), `ClanService.java`
(203, 401), `Whis.java` (104), `SummonDragonNamek.java` (164, 218, 258).

### Code sau

```java
            player.haveTennisSpaceShip = rs.getBoolean("have_tennis_space_ship");

            // FIX: đọc các cột điểm top ngay trong loadPlayer.
            // Trước đây chỉ login() mới đọc, nên nhân vật offline nạp bằng loadById() rồi
            // updatePlayer() (BlackBallWar, ClanService, Whis, SummonDragonNamek...) ghi đè điểm về 0.
            try {
                player.point_sukien = rs.getInt("point_sukien");
                player.point_sukien1 = rs.getInt("point_sukien1");
                player.point_sukien2 = rs.getInt("point_sukien2");
                player.thachdauwhis = rs.getInt("thachdauwhis");
                player.point_maydam = rs.getInt("point_maydam");
                player.total_damage_maydam = rs.getLong("total_damage_maydam");
            } catch (Exception e) {
            }
```

- **Lý do:** đặt trong `loadPlayer()` thì **cả** `login()` **và** `loadById()` đều nạp đúng.
  Sáu dòng đọc trùng trong `login()` được giữ nguyên (vô hại, đọc lại cùng giá trị) để hạn chế
  phạm vi sửa.
- **Rủi ro:** không. `loadPlayer` được gọi với `select * from player ...` nên các cột này luôn có
  trong `ResultSet`; vẫn bọc `try/catch` theo đúng phong cách các khối xung quanh phòng khi DB
  cũ thiếu cột.
- **Liên quan mục 4:** nếu **không** sửa chỗ này thì luồng tự lưu định kỳ (mục 4) cũng sẽ ghi đè
  điểm top — vì vậy hai mục phải đi cùng nhau.
- **Cách test:** đặt `point_sukien = 12345` cho một nhân vật, để nhân vật đó **offline**, cho bang
  của nhân vật thắng ngọc rồng đen (hoặc gọi thủ công `MrBlue.loadById` + `PlayerDAO.updatePlayer`)
  → `point_sukien` trong DB phải vẫn là `12345`. Kiểm tra lại bảng xếp hạng sự kiện trong game.

---

## 9. `resetNhanQuaHangNgay` hard-code database `ngocrong`

| | |
|---|---|
| **File** | `models/server/ServerManager.java` |
| **Hàm** | `ServerManager.resetNhanQuaHangNgay()` |
| **Dòng** | 333–350 (sau sửa) |
| **Mức độ** | Cao (lỗi chức năng) — reset nhận quà hằng ngày không chạy |

### Code trước

```java
    public void resetNhanQuaHangNgay() {
        String url = "jdbc:mysql://localhost:3306/ngocrong";
        String username = "root";
        String password = "";
        String resetJson = "[1,1,\"1970-01-01T00:00:00\"]";

        try (Connection conn = DriverManager.getConnection(url, username, password)) {
            String sql = "UPDATE player SET checkNhanQua = ? WHERE checkNhanQua != ?";
            PreparedStatement statement = conn.prepareStatement(sql);

            statement.setString(1, resetJson);
            statement.setString(2, resetJson);

            int rowsUpdated = statement.executeUpdate();
            Logger.success("Đã reset nhận quà hằng ngày cho " + rowsUpdated + " người chơi với dữ liệu: " + resetJson);
        } catch (SQLException e) {
            System.err.println("Lỗi reset nhận quà hằng ngày: " + e.getMessage());
        }
    }
```

### Code sau

```java
    public void resetNhanQuaHangNgay() {
        String resetJson = "[1,1,\"1970-01-01T00:00:00\"]";

        // FIX: dùng đúng database đang cấu hình trong Config.properties
        // (trước đây hard-code jdbc:mysql://localhost:3306/ngocrong nên chạy sai/không chạy được DB thật)
        try (Connection conn = LocalManager.getConnection()) {
            String sql = "UPDATE player SET checkNhanQua = ? WHERE checkNhanQua != ?";
            try (PreparedStatement statement = conn.prepareStatement(sql)) {
                statement.setString(1, resetJson);
                statement.setString(2, resetJson);

                int rowsUpdated = statement.executeUpdate();
                Logger.success("Đã reset nhận quà hằng ngày cho " + rowsUpdated + " người chơi với dữ liệu: " + resetJson);
            }
        } catch (SQLException e) {
            System.err.println("Lỗi reset nhận quà hằng ngày: " + e.getMessage());
        }
    }
```

Đồng thời xoá `import java.sql.DriverManager;` (không còn dùng).

- **Lý do:** `LocalManager.getConnection()` lấy kết nối từ pool HikariCP đã đọc
  `Config.properties` (`database.host/port/name/user/pass`) — chạy đúng database thật (`team2026`),
  đúng tài khoản, và không mở kết nối rời khỏi pool. `PreparedStatement` cũng được đưa vào
  try-with-resources để không rò rỉ khi có lỗi.
- **Rủi ro:** đây là câu `UPDATE` quét toàn bảng `player`, giờ **thật sự chạy** (trước đây gần như
  luôn thất bại). Nên chạy vào giờ thấp điểm. `Config.properties` phải nằm đúng thư mục chạy server.
- **Cách test:** sửa `checkNhanQua` của vài nhân vật thành giá trị khác mặc định, gọi
  `AutoUpdate_NhanQuaFree` (hoặc `ServerManager.gI().resetNhanQuaHangNgay()`), xem log báo số dòng
  cập nhật > 0 và cột `checkNhanQua` trở về `[1,1,"1970-01-01T00:00:00"]`.

---

## 10. Dữ liệu cũ của người chơi sẽ được hiểu lại thế nào

Đây là phần quan trọng nhất cần chủ dự án đọc kỹ. **Không có migrate SQL nào** — tất cả chỉ là
đổi cách đọc.

### `data_item_time` (mục 5)

Toàn bộ dữ liệu đang có trong DB được ghi bởi `PlayerDAO` theo thứ tự "đúng", nên sau khi sửa
bên đọc, **mọi giá trị sẽ được hiểu đúng vị trí ngay từ lần đăng nhập đầu tiên**. Cụ thể:

| Phần tử | Trước khi sửa (đọc sai) | Sau khi sửa (đọc đúng) |
|---|---|---|
| 12 (Cỏ bốn lá) | bị đọc thành **Kho báu x2** | Cỏ bốn lá (`lastTimeUseCoBonLa`) |
| 13 (Kho báu x2) | bị **bỏ qua** hoàn toàn | Kho báu x2 |
| 14 (Bùa Santa) | bị đọc thành **Bữa ăn** | Bùa Santa |
| 15 (Bữa ăn) | bị đọc thành **icon bữa ăn** | Bữa ăn |
| 16 (icon bữa ăn) | bị đọc thành **TDLT** | icon bữa ăn |
| 17–23 | lệch 1 ô (TDLT↔CMS↔GTPT↔ĐK↔RX↔bữa ăn 2↔icon) | đúng vị trí |
| 24 (dự phòng) | bị đọc thành **NCD** | bỏ qua |
| 25 (NCD) | bị đọc thành **Bùa Santa** | NCD |
| 26–29 | Nước mía lệch 1 ô, Nước mía 3 trùng Nước mía 2, Kilis đọc nhầm | Nước mía 1/2/3 + Kilis đúng |

**Ảnh hưởng thấy được:** người chơi đang có buff dở sẽ thấy **đúng** buff mình đã dùng thay vì
buff khác. Không ai bị mất buff; một vài người sẽ "được thêm" buff mà trước đây bị hệ thống bỏ
quên (Kho báu x2, Cỏ bốn lá) — đó là buff họ đã mua thật.

### `data_item_event` (mục 6)

- Dòng **cũ (6 phần tử)**: nạp đúng 6 giá trị đầu (Tất võ giáng sinh, Hột hư, Bánh nướng),
  10 giá trị còn lại giữ `0` = "chưa dùng lần nào hôm nay". Trước khi sửa thì **cả 6 giá trị đầu
  cũng bị xoá về 0**, nên đây là cải thiện thuần tuý, không ai bị thiệt.
- Dòng mặc định `'[]'`: tất cả = 0, giống hệt trước.
- Từ lần lưu đầu tiên sau khi lên bản mới, mọi dòng trở thành **16 phần tử** và từ đó giữ được
  đầy đủ giới hạn item sự kiện.
- **Lưu ý:** những người chơi đang lợi dụng lỗi này để rơi item sự kiện không giới hạn (đăng nhập
  lại là được reset) sẽ **mất** khả năng đó ngay khi lên bản mới.

### `masterDoesNotAttack` (mục 7)

Cột vẫn được ghi đúng từ trước, chỉ là chưa bao giờ đọc được. Sau khi sửa, giá trị đã lưu bắt đầu
có hiệu lực. Ai từng bật cờ này sẽ thấy đệ tử ngừng tự đánh sau khi đăng nhập lại — **đúng ý họ**,
nhưng có thể gây ngạc nhiên nếu họ đã quen cờ bị "quên".

### Điểm top (mục 8)

Không đổi cách lưu. Chỉ là từ nay nhân vật offline được nạp đầy đủ điểm nên **không còn bị ghi
về 0**. Điểm đã mất trước đây (nếu có) **không tự khôi phục được** — nếu chủ dự án còn bản sao lưu
DB cũ thì có thể so sánh và bù thủ công.

---

## 11. Tổng hợp file đã đụng

| File | Mục | Nội dung |
|---|---|---|
| `models/services/SubMenuService.java` | 1 | Kiểm tra `isAdmin` cho `menuId` 500/501, chống `player == null` |
| `models/npc/NpcFactory.java` | 1 | Kiểm tra `isAdmin` trong handler `BAN_PLAYER` / `BUFF_PET` |
| `models/services/Service.java` | 2 | Lệnh chat `part` chỉ dành cho admin |
| `models/server/Manager.java` | 2 | `loadPart()`: không ghi khi bảng rỗng, ghi file tạm rồi thay thế |
| `models/services/PlayerService.java` | 3 | `banPlayer` ghi `ban = 1`, không nuốt lỗi, chống null |
| `models/player/Player.java` | 3, 4 | Kick người bị ban ở mọi map; thêm trường `lastTimeAutoSave` |
| `models/server/ServerManager.java` | 4, 9 | Luồng tự lưu định kỳ; `resetNhanQuaHangNgay` dùng DB cấu hình |
| `models/server/Client.java` | 4 | `getPlayersSnapshot()` cho luồng tự lưu |
| `models/database/PlayerDAO.java` | 4, 6 | `synchronized (player)`, `autoSavePlayer()`; ghi đủ 16 phần tử `data_item_event` |
| `models/database/MrBlue.java` | 5, 6, 7, 8 | Đọc đúng `data_item_time`, `data_item_event`, `masterDoesNotAttack`, điểm top |

**Không đụng** các file nhóm khác đang sửa: `boss/**`, `npc_list/Osin.java`,
`npc_list/DaiThienSu.java`, `npc_list/ToriBot.java`, `shop_ky_gui/**`, `shop/ShopService.java`,
`services/shenron/**`, `minigame/**`, `matches/**`, `boss_con_duong_ran_doc/**`.

**Không cần patch SQL** cho đợt sửa này.

---

## 12. Điểm cần chủ dự án xác nhận

| # | Vấn đề | Đề xuất |
|---|---|---|
| 1 | **Chưa biên dịch.** Máy thi công không có JDK (`javac` là stub macOS). Đã kiểm tra thủ công cân bằng ngoặc và tra lại từng API dùng tới, nhưng vẫn cần `ant`/`javac` chạy thật. | Build trước khi lên server |
| 2 | Chu kỳ tự lưu **5 phút**, tối đa **10 nhân vật / 5 giây** (`ServerManager.AUTO_SAVE_INTERVAL`, `MAX_SAVE_PER_TICK`). Nếu MySQL yếu hoặc online cao hơn 600, cần chỉnh lại. | Theo dõi log `save successfully` và tải MySQL trong ngày đầu |
| 3 | `ConcurrentModificationException` khi tự lưu (rủi ro mục 4.1). Hiện chỉ log và bỏ qua nhịp đó. | Nếu log xuất hiện dày, báo lại để thêm khoá quanh `inventory` |
| 4 | Cờ `isUseCoBonLa` vẫn **không** được bật khi load, vì thời lượng hiệu lực (`timeLengthCoBonLa`) **không được lưu xuống DB** — sửa đúng cần thêm 1 phần tử vào `data_item_time` (đổi định dạng). Đã giữ nguyên hành vi cũ. | Xin quyết định: có mở rộng `data_item_time` không? |
| 5 | `ItemTime.update()` kiểm tra hạn Nước mía 2 và Nước mía 3 bằng `lastTimeUseNuocMia1` (dòng 242, 248) — **lỗi sẵn có**, không thuộc phạm vi đợt này. | Xin quyết định sửa hay để lại |
| 6 | Kiểm tra `MAX_PLAYER` trong `MySession.login` dùng `this.isAdmin` **trước** khi nạp tài khoản (luôn `false`) nên admin cũng bị chặn khi server đầy — đã ghi trong tài liệu 15, **không nằm trong danh sách giao** nên chưa sửa. | Xin quyết định |
| 7 | Form `GIVE_IT` (lệnh `item`), form bot và các lựa chọn 0/1/3 của menu admin không tự kiểm tra `isAdmin` (tài liệu 15, phần "Lỗi và rủi ro"). Hiện an toàn vì `typeInput`/`indexMenu` chỉ được đặt khi server mở form cho admin, **không nằm trong danh sách giao** nên chưa sửa. | Xin quyết định thêm kiểm tra phòng thủ |
