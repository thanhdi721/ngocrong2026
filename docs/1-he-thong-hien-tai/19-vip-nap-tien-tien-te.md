# 19. VIP, nạp tiền và tiền tệ trong game

> Tài liệu mô tả hệ thống nạp tiền (VND), cấp VIP, kích hoạt tài khoản ("mở thành viên"), quy đổi VND sang tiền trong game và các loại tiền tệ của server Ngọc Rồng Online (Teamobi2026).
> Nguồn: mã Java trong `SRC/src/nro/models/**` và file dump `database team2026.sql`. Mọi đường dẫn Java bên dưới tính từ `SRC/src/nro/models/`.
> Tài liệu liên quan: vật phẩm/shop, nâng cấp đồ/rồng thần, `16-su-kien-minigame-giai-dau.md`, `17-bang-hoi.md`, `18-npc.md`.

## Mục lục

1. [Tổng quan nhanh](#1-tổng-quan-nhanh)
2. [Bảng `account` – các cột liên quan tiền/VIP](#2-bảng-account--các-cột-liên-quan-tiềnvip)
3. [Các bảng nạp tiền: `napthe`, `payments`, `bank_transfers`](#3-các-bảng-nạp-tiền-napthe-payments-bank_transfers)
4. [Bảng `history_transaction` (lịch sử giao dịch)](#4-bảng-history_transaction-lịch-sử-giao-dịch)
5. [Bảng `settings`](#5-bảng-settings)
6. [Hệ thống VIP](#6-hệ-thống-vip)
7. [Tổng nạp (`tongnap`) và mốc nạp](#7-tổng-nạp-tongnap-và-mốc-nạp)
8. [Kích hoạt tài khoản / mở thành viên (`active`)](#8-kích-hoạt-tài-khoản--mở-thành-viên-active)
9. [Quy đổi VND sang tiền trong game](#9-quy-đổi-vnd-sang-tiền-trong-game)
10. [Các loại tiền tệ trong game](#10-các-loại-tiền-tệ-trong-game)
11. [Giao dịch, tặng ngọc, ký gửi và phí](#11-giao-dịch-tặng-ngọc-ký-gửi-và-phí)
12. [Ghi chú / điểm cần lưu ý](#12-ghi-chú--điểm-cần-lưu-ý)

---

## 1. Tổng quan nhanh

| Khía cạnh | Thực tế trong code |
|---|---|
| Nạp thẻ / chuyển khoản | **Không xử lý trong server Java.** Các bảng `napthe`, `payments`, `bank_transfers` không được code Java đọc/ghi → do web nạp bên ngoài xử lý và cộng vào `account.vnd` / `account.tongnap`. |
| Số dư VND | `account.vnd` được nạp vào `MySession.vnd` khi đăng nhập; bị trừ khi đổi ngọc (Bò Mộng) và khi mua gói VIP (Tori-Bot). |
| VIP | Có **2 khái niệm VIP khác nhau**: (a) `account.vip` (chỉ đọc, do web/admin ghi) và (b) `player.vip` lưu trong `player.data_vip` (mua ở NPC Tori-Bot, 4 cấp, theo mùa). |
| Mốc nạp tích lũy | **Không có** hệ thống mốc nạp/quà tích lũy. `tongnap` chỉ dùng cho thành tích "Lần đầu nạp ngọc". |
| Kích hoạt tài khoản | `account.active` → `MySession.actived`. Chưa kích hoạt bị chặn giao dịch, ký gửi, thách đấu, oẳn tù tì, vài phó bản bang. Server **không có** chức năng mua kích hoạt đang được gọi (hàm `PlayerDAO.MuaThanhVien` tồn tại nhưng không được dùng). |
| Tiền tệ | Vàng (`gold`, tối đa 200 tỷ), Ngọc xanh (`gem`), Hồng ngọc (`ruby`), Thỏi vàng (vật phẩm id 457), điểm sự kiện, `coupon` (điểm). |

---

## 2. Bảng `account` – các cột liên quan tiền/VIP

Schema lấy từ `CREATE TABLE account` trong dump. Cột "Code Java" cho biết server có đọc/ghi hay không.

| Cột | Kiểu (mặc định) | Ý nghĩa | Code Java đọc | Code Java ghi |
|---|---|---|---|---|
| `id` | int | Khóa chính, = `MySession.userId` | `database/MrBlue.java` `login()` | – |
| `username`, `password` | varchar | Đăng nhập (mật khẩu so sánh **dạng thô** trong SQL) | `MrBlue.login()` | `services/Service.java` (đổi mật khẩu, dòng ~1801) |
| `ban` | tinyint (0) | 1 = khóa tài khoản, không cho đăng nhập | `MrBlue.login()` | `services/PlayerService.java` (~254, gỡ ban) |
| `is_admin` | tinyint (0) | Quyền admin trong game → `MySession.isAdmin` | `MrBlue.login()` | – |
| `admin` | int (0) | Cột phụ, không dùng trong Java (web) | – | – |
| `active` | int (**1**) | Đã kích hoạt/mở thành viên → `MySession.actived` | `MrBlue.login()` | `PlayerDAO.MuaThanhVien()` (không được gọi ở đâu) |
| `vnd` | int (0) | **Số dư VND** có thể tiêu trong game | `MrBlue.login()` | `PlayerDAO.subvnd()` (`vnd = vnd - ?`), `PlayerDAO.MuaThanhVien()` |
| `tongnap` | int (0) | **Tổng tiền đã nạp** (tích lũy) | `MrBlue.login()` | – (web ghi) |
| `vip` | int (0) | Cấp VIP cấp tài khoản → `MySession.vip` | `MrBlue.login()` | – (web/admin ghi) |
| `thoi_vang` | int (0) | Thỏi vàng ở cấp tài khoản → `MySession.goldBar` | `MrBlue.login()` | – (**nạp vào session nhưng không dùng ở đâu**) |
| `vang` | bigint (0) | Vàng cấp tài khoản → `MySession.gold` | `MrBlue.login()` | – (không dùng) |
| `event_point` | int (0) | Điểm sự kiện cấp tài khoản → `MySession.eventPoint` | `MrBlue.login()` | – (không dùng; điểm sự kiện thật lưu ở `player.event_point`) |
| `luotquay` | int (0) | Lượt quay → `MySession.luotquay` | `MrBlue.login()` | – (không dùng) |
| `bd_player` | double (1) | → `MySession.bdPlayer` | `MrBlue.login()` | – |
| `is_gift_box`, `gift_time`, `reward` | – | Quà/hộp quà cấp tài khoản | – | – |
| `last_time_login`, `last_time_logout`, `ip_address` | timestamp/varchar | Chống đăng nhập liên tục, ghi IP | `MrBlue.login()`, `PlayerDAO.checkLogout()` | `MrBlue.login()`, `server/Client.java` |
| `server_login` | int (-1) | – | – | – |
| `tichdiem`, `point_post`, `last_post`, `baiviet`, `gioithieu`, `xacnhan_gioitheu`, `xacminh` | int | Tích điểm / diễn đàn / giới thiệu / xác minh – **chức năng web** | – | – |
| `token`, `xsrf_token`, `newpass`, `email` | text | Phiên web, quên mật khẩu | – | – |

Dữ liệu mẫu trong dump: 3 tài khoản test (`is_admin = 1`, `active = 1`, `vnd = 9.890.000`, `tongnap = 0`, `vip = 0`).

Nạp vào session: `network/MySession.java` có các trường `actived`, `goldBar`, `gold`, `eventPoint`, `vnd`, `tongnap`, `vip`, `luotquay`. Các giá trị này **chỉ nạp một lần khi login**; nếu web cộng tiền trong lúc người chơi đang online thì trong game không thấy cho tới khi đăng nhập lại (hoặc có lệnh reload riêng – không tìm thấy trong code).

---

## 3. Các bảng nạp tiền: `napthe`, `payments`, `bank_transfers`

Tìm kiếm toàn bộ `SRC/src` không có câu SQL nào tham chiếu tới ba bảng này → chúng **do website nạp tiền bên ngoài** ghi và xử lý (callback API gạch thẻ / webhook ngân hàng), sau đó web cộng vào `account.vnd` và `account.tongnap`.

### 3.1 `napthe` (nạp thẻ cào – bản cũ)

| Cột | Kiểu | Ý nghĩa (suy ra từ tên cột) |
|---|---|---|
| `id` | int | Khóa |
| `user_nap` | varchar(100) | Username người nạp |
| `telco` | varchar | Nhà mạng |
| `serial`, `code` | varchar | Seri / mã thẻ |
| `amount` | int | Mệnh giá |
| `status` | int | Trạng thái (dữ liệu mẫu: `1`) |
| `request_id` | varchar | Mã yêu cầu gửi API |
| `created_at` | timestamp | Thời điểm |

Dump có 1 dòng thử nghiệm (`amount = 10.000.000`, `status = 1`).

### 3.2 `payments` (nạp thẻ cào qua API – bản mới)

| Cột | Kiểu | Ý nghĩa |
|---|---|---|
| `name` | varchar | Username |
| `refNo` | varchar | Mã tham chiếu (dạng `nrc_...`) |
| `date` | datetime | Thời điểm |
| `card_serial`, `card_pin` | varchar | Seri, mã thẻ (**lưu dạng thô**) |
| `card_telco` | varchar | Nhà mạng (VIETTEL…) |
| `declared_amount` | int | Mệnh giá người chơi khai |
| `api_declared_value`, `detected_value`, `received_amount_from_api` | int | Giá trị do API trả về |
| `final_credited_amount` | int (0) | Số tiền thực cộng vào tài khoản |
| `status_text` | varchar | "Thành công", "Thẻ lỗi"… |
| `api_status_code`, `api_message` | varchar/text | Mã/Thông điệp API (vd `99`/`PENDING`, `3`/`lang.invalid_card_code`) |
| `is_credited` | tinyint (0) | Đã cộng tiền hay chưa (chống cộng trùng) |

Dump có 4 dòng mẫu (2 dòng `PENDING`, 2 dòng "Thẻ lỗi"), tất cả `is_credited = 0`.

### 3.3 `bank_transfers` (chuyển khoản ngân hàng)

| Cột | Kiểu | Ý nghĩa |
|---|---|---|
| `transaction_id` | varchar | Mã giao dịch ngân hàng |
| `username` | varchar | Tài khoản được cộng (thường lấy từ nội dung CK) |
| `amount` | decimal(15,2) | Số tiền |
| `description` | text | Nội dung chuyển khoản |
| `status` | varchar | Trạng thái |
| `sender_bank_name` | varchar | Ngân hàng gửi |
| `created_at` | datetime | Thời điểm |
| `is_credited` | tinyint (0) | Đã cộng tiền chưa |

Dump không có dữ liệu.

### 3.4 Form nạp thẻ trong game

`services_func/Input.java` có hằng `NAP_THE = 505` và hàm `createFormNapThe(Player, byte loaiThe)` (form "Mã thẻ", "Seri"), nhưng **không có nhánh `case NAP_THE`** xử lý dữ liệu nhập và cũng không nơi nào gọi `createFormNapThe` → chức năng nạp thẻ trong game là **code chết**.

---

## 4. Bảng `history_transaction` (lịch sử giao dịch)

| Cột | Ý nghĩa |
|---|---|
| `player_1`, `player_2` | `"tên (id)"` của hai bên |
| `item_player_1`, `item_player_2` | `"Gold: <số>, Tên vật phẩm (xN),..."` mỗi bên đưa ra |
| `bag_1_before_tran`, `bag_2_before_tran` | Hành trang trước giao dịch (chuỗi tên + số lượng) |
| `bag_1_after_tran`, `bag_2_after_tran` | Hành trang sau giao dịch |
| `time_tran` | Thời điểm |

- **Ai ghi:** server Java – `database/HistoryTransactionDAO.insert(...)`, được gọi trong `services_func/Trade.java` `startTrade()` khi giao dịch giữa 2 người chơi thành công.
- Tham số `gold1Before/gold2Before/gold1After/gold2After` được truyền vào nhưng **không được lưu** (chỉ lưu số vàng giao dịch).
- **Tự xóa:** `HistoryTransactionDAO.deleteHistory()` (gọi khi khởi động trong `server/ServerManager.java`) xóa bản ghi cũ hơn **3 ngày**.

---

## 5. Bảng `settings`

Bảng cấu hình **website** (một dòng), không được code Java đọc: `Title`, `Description`, `Keywords`, `SiteKey`/`SecretKey` (reCAPTCHA), `ServerName`, `Fanpage`, `Group`, `Zalo`, `EmailSupport`, `AccountBank`, `PasswordBank`, `NumberBank`, `NameBank` (thông tin tài khoản ngân hàng nhận tiền – dùng cho `bank_transfers`), link tải `Android`/`Windows`/`IPhone`/`Java`.

Dump hiện chứa giá trị giữ chỗ (`'1'`) cho các cột ngân hàng, nhưng `SiteKey/SecretKey` có giá trị thật (không chép vào tài liệu này).

---

## 6. Hệ thống VIP

Server có VIP thực sự nhưng tồn tại **hai nguồn VIP độc lập**:

| | VIP tài khoản | VIP mùa (Tori-Bot) |
|---|---|---|
| Lưu ở | `account.vip` → `MySession.vip` | `player.data_vip` (JSON) → `Player.vip`, `Player.timevip`, `Player.vipPurchaseCount` |
| Ai ghi | Web/admin (Java không ghi) | NPC Tori-Bot (`npc_list/ToriBot.java`) |
| Lợi ích | +300% tiềm năng (`player/NPoint.java` `calSucManhTiemNang`), quyền "Cừu sát" (`services/SubMenuService.java`) | Vật phẩm theo gói + `timevip` +30 ngày → +300% tiềm năng |

### 6.1 VIP tài khoản (`account.vip`)

| Nơi dùng | Hiệu ứng |
|---|---|
| `player/NPoint.java` `calSucManhTiemNang()` | Nếu `session.vip > 0` (hoặc chủ của đệ tử có `vip > 0`) → `tiemNang += tn * 3` (thêm 300%). |
| `services/SubMenuService.java` case `CUU_SAT` | Reset lượt mỗi ngày: `vip = 1` → **15 lượt**, `vip = 2` → **30 lượt**; cần `vip > 0` và còn lượt (admin bỏ qua). Phần khởi tạo `new CuuSat(...)` đang bị comment → chỉ trừ lượt. |
| `server/Controller.java`, `services/AchievementService.java` | (dùng `tongnap`, xem mục 7) |

### 6.2 VIP mùa – NPC Tori-Bot

- NPC: **Tori-Bot** (`npc_template.id = 74`, `ConstNpc.TORIBOT = 74`), lớp `npc_list/ToriBot.java`.
- **Mùa VIP:** `VIP_SEASON_START_DATE` = 05/06 00:00:00, `VIP_SEASON_END_DATE` = 05/12 23:59:59 của năm hiện tại (hardcode). Sau ngày kết thúc chỉ hiện "Ngươi tìm ta có việc gì?".
- **Giới hạn:** tối đa **4 lượt mua/mùa** (`vipPurchaseCount`). Mua lại cấp ≤ cấp hiện có vẫn được (chỉ thông báo) và vẫn nhận quà.
- **Thanh toán:** trừ `session.vnd` (menu ghi "điểm mùa"). `Player.vip` chỉ tăng nếu cấp mua cao hơn. Mỗi lần mua: `timevip` = max(now, timevip) + **30 ngày**.
- Hàm: `openBaseMenu()`, `confirmMenu()`, `BuyVip(pl, vipLevel, cost)`, `VIP1()`…`VIP4()`.

| Cấp | Giá (VND) | Quà thực tế trong code (id → tên theo `item_template`) |
|---|---|---|
| VIP 1 | **50.000** | 457 Thỏi vàng x200; 459 Phiếu giảm giá x10; 1252 Ve Sầu Xên (SĐ/HP/KI +10%, HSD 30 ngày); 1248 Bọ Cánh Cứng (+10%/10%/10%, HSD 30 ngày); 1256 Pet heo bướm (+10%×3, HSD 30 ngày – menu ghi "Búa hắc hường"); 987 Đá bảo vệ x5; tạo đệ tử thường nếu chưa có |
| VIP 2 | **100.000** | 457 Thỏi vàng x500; 459 x10; 1252 Ve Sầu Xên (+12%×3, 30 ngày); 1248 Bọ Cánh Cứng (+12%×3, 30 ngày); 1254 Búa hắc hường (+12%×3, 30 ngày); 987 Đá bảo vệ x10; 584 Cải trang Thỏ Bunma (SĐ +24%, HP +24%, "Đẹp" +15%, 30 ngày); đệ tử nếu chưa có |
| VIP 3 | **150.000** | 457 x700; 459 x10; 1252, 1248, 1254 (+12%×3, **vĩnh viễn**); 987 x30; 584 Cải trang Thỏ Bunma (vĩnh viễn); 1655 Cápsule Kích hoạt 1 món tự chọn x2 (không giao dịch); 956 Mảnh Đội trưởng Vàng x10; đệ tử nếu chưa có |
| VIP 4 | **200.000** | 457 x1000; 459 x10; 568 Quả Trứng x1 (menu: "đệ tử mabu"); 987 x50; 1554 Tàu ngầm 19 Cam (+15% SĐ/HP/KI, option 14 Chí mạng +10); 1771 Bé Rồng Cute (+18% SĐ/HP, option 5 +18, option 14 +10, option 236 +15); 1772 búa sơn tinh (+15%×3, option 236 +15); 1557 Hắc Mị Nương (SĐ/HP +25%, option 117 +25, option 236 +25); 1655 Cápsule x5; 1204 Mảnh Rồng thần Namếc x20 |

Option id tra từ `item_option_template`: 50 = Sức đánh+#%, 77 = HP+#%, 103 = KI+#%, 93 = Hạn sử dụng # ngày, 117 = Đẹp +#% SĐ, 14 = Chí mạng+#%, 5 = +#% sức đánh chí mạng, 236 = "+#% May mắn" (comment trong code ghi "Kháng tất cả"), 30 = Không thể giao dịch.

### 6.3 Reset VIP mùa

`services/PlayerService.java` `dailyLogin()`: nếu ngày hiện tại **sau 05/07** năm nay và `vipPurchaseCount != 0` → đặt `vip = 0`, `timevip = 0`, `vipPurchaseCount = 0` (chạy lần đăng nhập đầu tiên mỗi ngày). Ngày này **không khớp** mùa VIP của Tori-Bot (05/06–05/12) – xem mục 12.

### 6.4 Lưu trữ `data_vip`

`PlayerDAO` (khối "data vip", ~dòng 915) ghi mảng JSON: `[timesPerDayCuuSat, lastTimeCuuSat, nhanDeTuNangVIP, nhanVangNangVIP, nhanSKHVIP, vip, timevip, vipPurchaseCount]`. `MrBlue.loadPlayer` đọc lại; nếu mảng ≤ 7 phần tử (định dạng cũ) thì `nhanSKHVIP`, `vipPurchaseCount` về mặc định. Các cờ `nhanDeTuNangVIP`, `nhanVangNangVIP`, `nhanSKHVIP` chỉ được lưu/đọc, không có logic dùng.

### 6.5 Những thứ mang chữ "VIP" nhưng không phải VIP nạp tiền

| Tính năng | File | Ghi chú |
|---|---|---|
| Nội tại VIP | `services/IntrinsicService.java` `openVip()` | Mở nội tại giá **100 ngọc xanh** |
| Chọn Ai Đây giải VIP | `npc_list/LyTieuNuong.java`, `minigame/ChonAiDay_Gold/Gem.java` | Mức cược 10 triệu vàng / 100 ngọc xanh |
| KOL VIP | `npc_list/QuyLaoKame.java` | Cần "Vé Nhiệm Vụ VIP" |
| Vòng quay VIP | `services/RewardService.java` `getListItemLuckyRound(..., vip)` | Bảng thưởng khác nhau |

---

## 7. Tổng nạp (`tongnap`) và mốc nạp

- `tongnap` chỉ được đọc (`MrBlue.login`). Java không cộng/trừ.
- Nơi dùng duy nhất: `server/Controller.java` (khi vào game) – nếu `tongnap > 0` gọi `AchievementService.checkDoneTask(player, ConstAchievement.LAN_DAU_NAP_NGOC)`; hàm này đặt tiến độ thành tích = `tongnap` (`achievement.doneNotAdd`).
- Thành tích tương ứng (`achievement_template` id 12, index 11): **"Lần đầu nạp ngọc – Nạp ít nhất 150 ngọc"**, `max_count = 150`, thưởng **50 ngọc xanh** (nhận qua `AchievementService.confirmAchievement`). Nghĩa là `tongnap ≥ 150` (VND) là đủ.
- **Không có** hệ thống mốc nạp tích lũy / quà theo mốc (`mocnap`, `danap` không tồn tại trong code).
- Huy hiệu/nhiệm vụ huy hiệu `ConstTaskBadges.DAI_GIA_MOI_NHU` và `EM_XINH_EM_DEP` được cộng tiến độ bằng số VND đã đổi (xem mục 9).

---

## 8. Kích hoạt tài khoản / mở thành viên (`active`)

### 8.1 Trạng thái

- Cột `account.active` mặc định **1** trong schema → tài khoản mới tạo đã được kích hoạt, trừ khi web tạo với giá trị 0.
- Nạp vào `MySession.actived`. `Player.isActive()` (`player/Player.java`) trả về `actived` của người chơi, hoặc của chủ nếu là đệ tử.
- `MrBlue.loadPlayer` (~dòng 1320): nếu `actived` và `vnd < 0` → đặt `actived = false`, `vnd = 0` (**chỉ trong bộ nhớ**, không ghi DB).

### 8.2 Giá mở thành viên

- `PlayerDAO.MuaThanhVien(player, num)`: trừ `num` VND và ghi `active` theo giá trị `session.actived` hiện tại. **Hàm không được gọi ở đâu** và không đặt `actived = true` trước khi ghi → không có giá mở thành viên nào trong server. Thông báo khi bị chặn giao dịch hướng người chơi "Truy Cập: `ServerManager.DOMAIN` Để Mở Thành Viên" (`DOMAIN = "Server 1"`, hardcode) → việc mở thành viên dự kiến thực hiện trên web.

### 8.3 Hạn chế khi chưa kích hoạt

| Chức năng bị chặn | File / hàm | Thông báo |
|---|---|---|
| Mời / chấp nhận giao dịch | `services_func/TransactionService.java` (SEND_INVITE_TRADE, ACCEPT_TRADE) | "Truy Cập: … Để Mở Thành Viên" |
| Thêm vật phẩm/vàng vào giao dịch | `services_func/Trade.java` `addItemTrade()` | – |
| Cửa hàng ký gửi | `npc_list/KyGui.java` | "Bạn chưa kích hoạt thành viên!!!" |
| Đổi VND → Thỏi vàng (`TRADE_GOLD`) | `services_func/Input.java` | "Vui lòng kích hoạt tài khoản!" |
| Thách đấu (đối thủ chưa kích hoạt) | `matches/PVPService.java` (2 chỗ) | "Đối thủ chưa kích hoạt tài khoản" |
| Oẳn tù tì cược 5 triệu vàng (cả hai bên) | `services/SubMenuService.java` case `OTT` | "… chưa kích hoạt tài khoản!" |
| Doanh trại Độc Nhãn (Lính canh) | `npc_list/LinhCanh.java` | "Vui lòng mở thành viên trước" |
| Khí gas hủy diệt (Mr. PoPo) | `npc_list/MrPoPo.java` | như trên |
| Con đường rắn độc (Thần Vũ Trụ) | `npc_list/ThanVuTru.java` | như trên |

### 8.4 Lợi ích khi đã kích hoạt (rơi đồ, `mob/Mob.java`)

| Điều kiện | Không kích hoạt | Có kích hoạt (thêm) |
|---|---|---|
| Ngọc rồng 6/7 sao (id 19–20) ở map 3 hành tinh / Nappa / Tương lai / Cold | 10/70 (≈14,3%, cờ bốn lá ×1,15) | thêm cơ hội 1/100 |
| Mảnh thiên sứ id 1066–1070 (Ngục tù, mặc set Hủy diệt) | 10/100 | thêm 2/555 |
| Bí kíp tuyệt kỹ id 1229 (Ngục tù) | 10/100 | thêm 20/100 nếu mặc set Hủy diệt |

---

## 9. Quy đổi VND sang tiền trong game

### 9.1 Bảng tổng hợp

| Hình thức | NPC / menu | Hàm | Tỉ lệ | Giới hạn mỗi lần | Cần kích hoạt | Trạng thái |
|---|---|---|---|---|---|---|
| VND → **Ngọc xanh** | **Bò Mộng** (map 47 Rừng Karin, 84 Siêu Thị) → "Nạp Ngọc" | `Input.createFormTradeGem()` → `case TRADE_GEM` | **1 VND = 1 ngọc xanh** (10.000đ = 10.000 ngọc) | 10.000 – 5.000.000 đ | Không | Đang dùng |
| VND → **Thỏi vàng** | (không có NPC gọi) | `Input.createFormTradeGold()` → `case TRADE_GOLD` | **1.000đ = 4 thỏi vàng** (10.000đ = 40) | 10.000 – 5.000.000 đ | Có | **Code chết** (form không được gọi) |
| VND → **Gói VIP** | **Tori-Bot** | `ToriBot.BuyVip()` | 50k / 100k / 150k / 200k | 4 lượt/mùa | Không | Đang dùng (xem lỗi mục 12) |
| VND → **Hồng ngọc** | – | – | Không có | – | – | Không tồn tại |

### 9.2 Thưởng kèm mỗi lần đổi (cả TRADE_GEM và TRADE_GOLD)

| Thưởng | Công thức | Ví dụ 100.000đ |
|---|---|---|
| 718 Vé tặng ngọc | `(VND / 10.000) × 10` | 100 vé |
| Điểm sự kiện (`player.event`) | `(VND / 10.000) × 50` | 500 điểm |
| Tiến độ huy hiệu `DAI_GIA_MOI_NHU`, `EM_XINH_EM_DEP` | + số VND | +100.000 |
| (TRADE_GOLD) Thỏi vàng 457 | `(VND / 1.000) × 4` | 400 thỏi |

Việc trừ tiền dùng `PlayerDAO.subvnd()` – `UPDATE account SET vnd = vnd - ? WHERE id = ?` rồi trừ trong session.

### 9.3 Quy đổi thỏi vàng ↔ vàng

| Hướng | Nơi | Tỉ lệ |
|---|---|---|
| Thỏi vàng → vàng | Bán ở shop (`ShopService.showConfirmSellItem` → form `BANSLL` trong `Input.java`) | **1 thỏi = 37.000.000 vàng** (hardcode), chặn nếu vượt 200 tỷ |
| Thỏi vàng → vàng (gói tin bán trực tiếp) | `ShopService.sellItem()` | `item_template.gold` của id 457 = **500.000.000 vàng**, mỗi lần bán 1 thỏi (xem mục 12) |
| Vàng → thỏi vàng | Không có | – |

---

## 10. Các loại tiền tệ trong game

### 10.1 Bảng tiền tệ

| Tiền tệ | Lưu ở đâu | Kiểu / giới hạn | Hiển thị client |
|---|---|---|---|
| **Vàng** (`inventory.gold`) | `player.data_inventory[0]` | `long`, `Inventory.LIMIT_GOLD = 200.000.000.000` (200 tỷ); khi lưu `PlayerDAO` cũng cắt về 200 tỷ | `Service.sendMoney()` (long nếu client version ≥ 214, ngược lại int) |
| **Ngọc xanh** (`inventory.gem`) | `data_inventory[1]` | `int`; nhặt ngọc cắt ở `Integer.MAX_VALUE`; giftcode cắt 200.000.000; admin buff cắt 2.000.000.000; các nguồn khác không cắt | `sendMoney()` |
| **Hồng ngọc** (`inventory.ruby`) | `data_inventory[2]` | `int`; nhặt item type 34 (vd id 861 "Hồng ngọc") cắt `Integer.MAX_VALUE`; giftcode cắt 200.000.000 | `sendMoney()` |
| **Coupon / điểm** (`inventory.coupon`) | `data_inventory[3]` | `int`; dùng cho shop `type_sell = 4` | – |
| `inventory.event` | `data_inventory[4]` | `int` | – |
| **Thỏi vàng** | Vật phẩm id **457** (type 27) trong hành trang | Số lượng item | – |
| **Điểm sự kiện** | `PlayerEvent.eventPoint` → `player.event_point` | `int` | – |
| **VND** | `account.vnd` | `int` | Hiện trong form đổi, menu Tori-Bot |

Loại tiền trong shop (`shop/ShopService.java`): `COST_GOLD = 0`, `COST_GEM = 1`, `COST_RUBY = 3`, `COST_COUPON = 4`. Dump `item_shop` hiện chỉ dùng `type_sell = 0` (262 dòng) và `1` (564 dòng).

### 10.2 Nguồn kiếm chính

| Tiền tệ | Nguồn | File |
|---|---|---|
| Vàng | Nhặt vàng rơi (item type 9; biến hình Chibi loại 0 được ×2) | `services/InventoryService.java` |
| Vàng | Bán đồ ở shop (giá `template.gold / 4`), bán thỏi vàng (37 triệu) | `shop/ShopService.java`, `Input.java` |
| Vàng | Rồng thần: điều ước +200 triệu (`SHENRON_2`), +20 triệu (`SHENRON_3`) | `services/shenron/SummonDragon.java` |
| Vàng | Pháo bông 5.000–20.000; Pháo bông VIP 500.000–2.000.000; Capsule kì bí 5.000–20.000 | `services_func/UseItem.java` |
| Vàng | Giao dịch (tối đa 10 triệu/lần), Chọn Ai Đây, oẳn tù tì | `Trade.java`, `minigame/` |
| Ngọc xanh | Đổi VND (Bò Mộng) | `Input.java` TRADE_GEM |
| Ngọc xanh | Điểm danh Bò Mộng: **10.000 ngọc + 100 thỏi vàng**/ngày | `npc_list/BoMong.java` |
| Ngọc xanh | Thành tích (`achievement_template.money`, vd 10.000 ngọc cho "Gia nhập Vệ Binh", 50.000 cho "Tuyệt kỹ thành thạo") | `AchievementService.confirmAchievement()` |
| Ngọc xanh | Rồng thần +10.000 / +2.000 / +200 | `SummonDragon.java` |
| Ngọc xanh | Ký gửi bán bằng ngọc (nhận 90%), Siêu hạng, Chọn Ai Đây (ngọc), Con số may mắn, sự kiện Vua Hùng, hộp quà Noel (10–20) | `shop_ky_gui/`, `player/SuperRank.java`, `minigame/`, `event/VuaHung.java`, `UseItem.NoelItemBox` |
| Hồng ngọc | Nhặt item type 34, giftcode, admin buff | `InventoryService`, `GiftCodeService`, `Input.SEND_ITEM_OP` |
| Thỏi vàng | Gói VIP (200–1000), điểm danh Bò Mộng (100, không giao dịch), đổi VND (code chết), nhận tiền ký gửi bán bằng vàng, giftcode | nhiều file |
| Điểm sự kiện | Đổi VND (50 điểm/10.000đ) | `Input.java` |

### 10.3 Nơi tiêu chính

| Tiền tệ | Nơi tiêu | Ghi chú |
|---|---|---|
| Vàng | Shop NPC (`type_sell = 0`), nội tại (`COST_OPEN[count] × 1.000.000`), vòng quay (**250.000.000/lượt**), mua lại đồ đã bán, ký gửi, Chọn Ai Đây (1 triệu/10 triệu), oẳn tù tì 5 triệu, thách đấu | `ShopService`, `IntrinsicService`, `services_func/LuckyRound.java` |
| Ngọc xanh | Shop (`type_sell = 1`), vòng quay (**4 ngọc/lượt**), nội tại VIP (100), tặng ngọc (kèm phí), mua đồ ký gửi bán bằng ngọc, Chọn Ai Đây (10/100). Lời NPC ký gửi ghi "phí 5 ngọc" nhưng code thực tế thu 1 thỏi vàng | nhiều file |
| Hồng ngọc | Shop `type_sell = 3` (hiện không có mặt hàng), thách đấu Ghi Danh | `ShopService`, `npc_list/GhiDanh.java` |
| Thỏi vàng | Phí đăng ký gửi (1 thỏi), bán lấy vàng | `ConsignShopService.KiGui()` |
| Điểm sự kiện | Shop tab 59 (xem dưới) | `ShopService.buyItem()` |
| Vé quay (821 "Vé quay ngọc vàng") | Vòng quay, 1 vé/lượt | `LuckyRound.java` |

Shop đổi điểm sự kiện (tab 59, hardcode trong `ShopService.buyItem`):

| Item | Giá (điểm) |
|---|---|
| 1567 CT Frieren | 999 |
| 1731 CT Black Goku Rose | 999 |
| 1711 Cân Đẩu Vân Thơ Mộng | 750 |
| 1713 Khủng Long Thơ Mộng | 499 |
| 1682 Pet Hải Ly | 499 |
| 1698 CT Urôn Trư Bát Giới | 499 |
| 1821 Trứng vàng rồng nhí | 199 |
| 1840 Hộp quà Goku Day VIP | 99 |
| 1757 Hộp quà Cađíc VIP | 99 |
| 1592 Hộp quà Goku Day VIP | 99 |
| 1608 Hộp quà thiếu nhi | 9 |
| Item khác trong tab 59 | 0 (miễn phí – `default` không đặt giá) |

---

## 11. Giao dịch, tặng ngọc, ký gửi và phí

### 11.1 Giao dịch người chơi (`services_func/Trade.java`)

| Quy tắc | Giá trị |
|---|---|
| Điều kiện | Cả hai đã kích hoạt tài khoản |
| Vàng tối đa mỗi lần | `MAX_GOLD_TRADE_PER_TIME = 10.000.000` |
| Vàng sau giao dịch | Không vượt 200 tỷ (`FAIL_MAX_GOLD_PLAYER1/2`) |
| Thuế | **Không có** |
| Không giao dịch được | Item có option 30; type 27 (trừ id 590 Bí kiếp) → **thỏi vàng không giao dịch được**; cải trang (5), đậu thần (6), sách skill (7), vật phẩm NV (8), flag bag (11), bùa (13), vệ tinh (22), ván bay (23, 24), cờ (28), bánh (31), giáp tập luyện (32), id 454, 921; Rương Gỗ 570 |
| Lưu vết | `history_transaction` (mục 4) |

### 11.2 Tặng ngọc xanh (`Input.java` `FIND_PLAYER_NAME` → `FIND_PLAYER_GIFT_RUBY`)

| Bước | Giá trị |
|---|---|
| Yêu cầu | 1 vé **718 Vé tặng ngọc** (mất 1 vé/lần) |
| Người gửi bị trừ | `soGem + 10%` |
| Người nhận được | `soGem × 90%` |
| Hiệu quả thực | Mất 20% (phí tính 2 lần) |

`TANG_NGOC_HONG` (tặng hồng ngọc) có trong `Input.java` nhưng form `createFormTangRuby` không được gọi.

### 11.3 Cửa hàng ký gửi (`shop_ky_gui/ConsignShopService.java`, NPC 28)

| Quy tắc | Giá trị |
|---|---|
| Điều kiện mở | Tài khoản đã kích hoạt |
| Phí đăng bán | **1 thỏi vàng** (`SubThoiVang`) |
| Không ký gửi | Item có option 30 |
| Điều kiện mua | Sức mạnh **> 17 tỷ** |
| Mua bằng vàng | Người mua bị trừ `goldSell` **vàng** |
| Người bán nhận | `goldSell − 10%` **thỏi vàng** (item 457) |
| Mua bằng ngọc | Người mua trừ `gemSell` ngọc; người bán nhận `gemSell − 10%` ngọc |
| Thuế | **10%** trên tiền nhận |

---

## 12. Ghi chú / điểm cần lưu ý

### Bảo mật / tiền tệ nghiêm trọng

1. **Mua VIP không trừ tiền trong DB.** `ToriBot.BuyVip()` chỉ làm `pl.getSession().vnd -= cost` (bộ nhớ), không gọi `PlayerDAO.subvnd()`. Vì `account.vnd` chỉ được đọc lúc login và không được lưu lại khi thoát, người chơi đăng nhập lại là có lại số dư → nhận quà VIP (tới 1000 thỏi vàng/lần) gần như miễn phí, chỉ bị chặn bởi 4 lượt/mùa. Ngoài ra số dư trong session sau khi mua VIP lệch với DB, ảnh hưởng tới kiểm tra `subvnd` lần sau.
2. **Điều ước rồng thần cho vàng tối đa.** `SummonDragon.java`: `SHENRON_2` – nếu vàng > 1.800.000.000 thì gán `gold = LIMIT_GOLD` (**200 tỷ**) thay vì +200 triệu; `SHENRON_3` – nếu vàng > 1.980.000.000 thì cũng gán 200 tỷ. Ngưỡng là của giới hạn cũ 2 tỷ, sau khi nâng `LIMIT_GOLD` lên 200 tỷ trở thành lỗi in vàng.
3. **Bán thỏi vàng trực tiếp giá 500 triệu.** Client bình thường đi qua form `BANSLL` (37 triệu/thỏi), nhưng `Controller` cho phép gửi thẳng action bán → `ShopService.sellItem()` dùng `item_template.gold = 500.000.000` cho id 457 (mỗi lần 1 thỏi) → chênh lệch ~13,5 lần.
4. **Ký gửi lệch đơn vị tiền:** người mua trả `goldSell` **vàng**, người bán nhận `goldSell × 90%` **thỏi vàng** (1 thỏi = 37 triệu vàng). Cần kiểm tra lại ý đồ thiết kế – có thể là lỗ hổng in vàng. Các kiểm tra `money > 100000 && money < 0` và `quantity > 99 && quantity < 0` luôn sai (dùng `&&`) nên giới hạn giá/số lượng không có tác dụng.
5. **`subMoneyByItemShop` kiểm tra sai hồng ngọc:** so `inventory.gem < ruby` thay vì `inventory.ruby < ruby` → hồng ngọc có thể âm nếu có shop `type_sell = 3` (hiện DB chưa có).
6. **`subIemByItemShop` case 77 (ngọc)** chỉ kiểm tra đủ ngọc nhưng **không trừ ngọc** → mua miễn phí với các mặt hàng dùng icon ngọc làm tiền.
7. **`BANSLL`** dùng `Math.abs(Integer.parseInt(...))`: với `-2147483648` kết quả vẫn âm → vượt qua kiểm tra số lượng (cộng vàng âm / số lượng âm) – nên kiểm tra `sltv > 0`.
8. **Ngọc xanh không có trần** ở đổi VND (`gem += quantity`, tối đa 5 triệu/lần), rồng thần, thành tích… → có thể tràn `int` (âm) khi vượt ~2,147 tỷ.
9. **`TANG_NGOC_HONG`** (code chết) kiểm tra hồng ngọc nhưng trừ **ngọc xanh** của người gửi và cộng **hồng ngọc** cho người nhận; nếu được bật lại sẽ thành lỗi đổi tiền.
10. **Mật khẩu lưu dạng thô** trong `account.password` và so sánh trực tiếp trong SQL (`MrBlue.login`); mã/seri thẻ lưu thô trong `payments`. Bảng `settings` trong dump chứa khóa reCAPTCHA thật và có cột `PasswordBank` – không nên đưa dump lên công khai.
11. `LocalManager.executeUpdate("update account set last_time_login = '" + ... + "', ip_address = '" + session.ipAddress + "'...")` ghép chuỗi SQL (giá trị do server tạo nên rủi ro thấp, nhưng nên dùng tham số).

### Logic / thiết kế

12. **Mùa VIP không thống nhất:** Tori-Bot bán từ 05/06 đến 05/12, nhưng `PlayerService.dailyLogin()` reset VIP khi ngày hiện tại **sau 05/07** → mua sau 05/07 sẽ bị xóa `vip`, `timevip`, `vipPurchaseCount` ở lần đăng nhập ngày kế tiếp (và mở lại được 4 lượt mua).
13. **Mô tả gói VIP khác quà thực tế:** VIP1 ghi "Búa hắc hường" nhưng tặng id 1256 "Pet heo bướm"; VIP4 ghi "đệ tử mabu" nhưng tặng id 568 "Quả Trứng"; "10 thẻ đội trưởng vàng"/"20 thẻ rồng thần" thực chất là **Mảnh** Đội trưởng Vàng (956) / **Mảnh** Rồng thần Namếc (1204); menu ghi "X3 Kinh nghiệm toàn mùa" nhưng thực tế chỉ +30 ngày `timevip`. Không kiểm tra ô trống hành trang trước khi phát quà.
14. **Hai hệ VIP chồng nhau:** `session.vip` (account) và `timevip` (Tori-Bot) đều cộng +300% tiềm năng, cộng dồn được. `account.vip` không được Java ghi, `Player.vip` (Tori-Bot) lại không được dùng cho quyền lợi nào ngoài hiển thị/kiểm tra mua.
15. **Nhiều cột `account` bị nạp nhưng bỏ không:** `thoi_vang`, `vang`, `event_point`, `luotquay` (vào `MySession` nhưng không dùng). `tongnap` chỉ dùng cho thành tích 150 VND.
16. **Không có mốc nạp tích lũy, không có nạp thẻ trong game** (`NAP_THE`/`createFormNapThe` là code chết), không có mua kích hoạt (`MuaThanhVien` không được gọi, và nếu gọi cũng không set `actived = true`).
17. **TRADE_GOLD** (VND → thỏi vàng, yêu cầu kích hoạt) không có NPC gọi; **TRADE_GEM** lại không yêu cầu kích hoạt.
18. Số dư VND trong session không đồng bộ với web: web cộng tiền khi người chơi online sẽ không hiện cho tới khi đăng nhập lại; `MrBlue` gán `actived = false` khi `vnd < 0` chỉ trong bộ nhớ.
19. `history_transaction` chỉ giữ **3 ngày** và không lưu số vàng trước/sau giao dịch dù hàm nhận tham số.
20. Hardcode nhiều giá trị: tỉ lệ đổi (1đ = 1 ngọc, 1.000đ = 4 thỏi), 37 triệu/thỏi, giá VIP, ngày mùa VIP, giá điểm sự kiện tab 59, `DOMAIN = "Server 1"` trong thông báo mở thành viên, phần thưởng điểm danh Bò Mộng (10.000 ngọc + 100 thỏi vàng/ngày – khá lớn so với tỉ lệ nạp 10.000đ = 10.000 ngọc).
