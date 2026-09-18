# 17 — Bang hội (Clan)

> Tài liệu spec hệ thống bang hội của server Ngọc Rồng Online (Teamobi2026).
> Nguồn: `SRC/src/nro/models/clan/*`, `services/ClanService.java`, `services/FlagBagService.java`, `npc_list/DrDrief.java`, `npc_list/GiuMaDauBo.java`, `npc_list/QuyLaoKame.java`, `mob_bigboss/GauTuongCuop.java`, `services/TaskService.java`, `shop/ShopService.java`, `services_func/Input.java`, `server/Manager.java` và file `database team2026.sql` (bảng `clan`, `clan_task_template`, `flag_bag`, `shop`, `tab_shop`, `item_shop`, `player`).
> Mọi con số dưới đây lấy trực tiếp từ code/DB. Chỗ nào code không có sẽ ghi rõ.

Tài liệu liên quan: `14-ban-do-pho-ban.md` (chi tiết từng phó bản bang), `18-npc.md` (menu NPC đầy đủ), `19-vip-nap-tien-tien-te.md` (mở thành viên / `actived`), tài liệu nhiệm vụ (nhiệm vụ chính yêu cầu vào bang).

---

## Mục lục

1. [Tổng quan kiến trúc](#1-tổng-quan-kiến-trúc)
2. [Mô hình dữ liệu](#2-mô-hình-dữ-liệu)
3. [Tạo bang & giá cờ bang](#3-tạo-bang--giá-cờ-bang)
4. [Chức vụ & quyền hạn](#4-chức-vụ--quyền-hạn)
5. [Gia nhập / mời / xin vào / rời / đuổi / giải tán](#5-gia-nhập--mời--xin-vào--rời--đuổi--giải-tán)
6. [Tin nhắn bang, xin đậu & cho đậu](#6-tin-nhắn-bang-xin-đậu--cho-đậu)
7. [Cấp bang & số thành viên tối đa](#7-cấp-bang--số-thành-viên-tối-đa)
8. [Capsule bang (điểm bang)](#8-capsule-bang-điểm-bang)
9. [Nhiệm vụ bang](#9-nhiệm-vụ-bang)
10. [Gấu Tướng Cướp (boss bang)](#10-gấu-tướng-cướp-boss-bang)
11. [Cửa hàng bang hội (SHOP_CLAN)](#11-cửa-hàng-bang-hội-shop_clan)
12. [Lãnh địa Bang Hội (map 153) & NPC liên quan](#12-lãnh-địa-bang-hội-map-153--npc-liên-quan)
13. [Phó bản / hoạt động dành cho bang](#13-phó-bản--hoạt-động-dành-cho-bang)
14. [Lợi ích khác khi ở trong bang](#14-lợi-ích-khác-khi-ở-trong-bang)
15. [Kho bang / rương bang / bùa bang](#15-kho-bang--rương-bang--bùa-bang)
16. [Lưu trữ DB & vòng đời dữ liệu](#16-lưu-trữ-db--vòng-đời-dữ-liệu)
17. [Ghi chú / điểm cần lưu ý](#17-ghi-chú--điểm-cần-lưu-ý)

---

## 1. Tổng quan kiến trúc

| Thành phần | File | Vai trò |
|---|---|---|
| `Clan` | `clan/Clan.java` | Đối tượng bang: thông tin, danh sách thành viên (`members`), thành viên online (`membersInGame`), tin nhắn, trạng thái các phó bản bang, insert/update/delete DB |
| `ClanMember` | `clan/ClanMember.java` | Một thành viên (id, head/body/leg, role, sức mạnh, donate, capsule cá nhân, thời gian vào bang, thời gian xin đậu) |
| `ClanMessage` | `clan/ClanMessage.java` | Tin nhắn trong bảng tin bang (chat / xin đậu / xin vào bang) |
| `ClanService` | `services/ClanService.java` | Xử lý toàn bộ packet bang: tạo, đổi cờ/khẩu hiệu, mời, xin vào, duyệt, kick, phong/cắt chức, nhường bang chủ, chat, xin/cho đậu, gửi thông tin bang, bảng giá nâng cấp |
| `FlagBagService` | `services/FlagBagService.java` | Danh sách cờ chọn khi tạo bang + giá (đọc từ bảng `flag_bag`) |
| Danh sách bang | `server/Manager.java` (`Manager.CLANS`, load ở khối `//load clan`) | Load toàn bộ bảng `clan` khi khởi động server, giữ trong RAM |

Các packet (message id) chính gửi về client trong `ClanService`:

| Msg id | Hàm | Nội dung |
|---|---|---|
| -46 | `FlagBagService.sendListFlagClan` | Danh sách cờ + giá vàng/ngọc |
| -47 | `sendListClan` | Danh sách bang tìm kiếm (tối đa 20) |
| -50 | `sendListMemberClan` | Danh sách thành viên của 1 bang |
| -51 | `Clan.sendMessageClan` | Đẩy 1 tin nhắn bang |
| -53 | `sendMyClan` | Toàn bộ thông tin bang của mình (kèm capsule bang, cấp, thành viên, 20 tin nhắn gần nhất) |
| -57 | `sendInviteClan` | Lời mời vào bang |
| -61 | `sendClanId` | Cập nhật clan id của người chơi cho cả map |

---

## 2. Mô hình dữ liệu

### 2.1 Trường của `Clan` (`clan/Clan.java`)

| Trường | Kiểu | Mặc định (constructor) | Ý nghĩa |
|---|---|---|---|
| `id` | int | `NEXT_ID++` | ID bang; `NEXT_ID` = `max(id)+1` khi load server |
| `name` | String | "" | Tên bang (≤ 30 ký tự) |
| `name2` | String | "" | Tên viết tắt (2–4 ký tự), hiển thị `[name2] Tên nhân vật` (`Service.java` ~dòng 803–806) |
| `slogan` | String | "" | Khẩu hiệu (cắt còn 250 ký tự) |
| `imgId` | int | — | ID cờ bang (`flag_bag.id`) |
| `createTime` | int (giây) | thời điểm tạo | |
| `powerPoint` | long | 0 | "Sức mạnh bang" — **không có code nào cập nhật**, luôn giữ giá trị trong DB |
| `maxMember` | byte | **10** | Số thành viên tối đa |
| `level` | int | **1** | Cấp bang |
| `capsuleClan` | int | 0 | Capsule bang (lưu cột `clan_point`) |
| `members` / `membersInGame` | List | rỗng | Toàn bộ thành viên / thành viên đang online |
| `clanMessages` | List | rỗng | Tối đa 20 tin gần nhất (`addClanMessage`) |
| `doanhTrai`, `haveGoneDoanhTrai`, `lastTimeOpenDoanhTrai`, `playerOpenDoanhTrai` | | | Trạng thái Doanh trại Độc Nhãn |
| `BanDoKhoBau`, `lastTimeOpenBanDoKhoBau`, `levelDoneBanDoKhoBau`, `thoiGianHoanThanhBDKB`… | | | Trạng thái Bản đồ kho báu + thành tích |
| `ConDuongRanDoc`, `levelDoneCDRD`, `thoiGianHoanThanhCDRD`… | | | Con đường rắn độc |
| `KhiGasHuyDiet`, `timesPerDayKGHD`, `levelDoneKhiGas`, `thoiGianHoanThanhKhiGas`… | | | Khí gas hủy diệt |
| `timeUpdateClan` | long | 0 | Cooldown đổi tên viết tắt: **60 giây** (`canUpdateClan`) |

### 2.2 Trường của `ClanMember` (`clan/ClanMember.java`)

| Trường | Ý nghĩa |
|---|---|
| `id`, `name`, `head`, `body`, `leg` | Thông tin nhân vật (chụp tại thời điểm vào bang) |
| `role` | 0 = Bang chủ, 1 = Phó bang, 2 = Thành viên |
| `powerPoint` | Sức mạnh (làm mới khi gửi danh sách nếu người chơi online) |
| `donate`, `receiveDonate` | Số đậu đã cho / đã nhận — **không có code nào tăng 2 giá trị này** |
| `memberPoint` | "Capsule cá nhân" (tổng capsule cá nhân đã đóng góp) |
| `clanPoint` | "Capsule cho bang" (tổng capsule đóng góp cho bang) |
| `joinTime` | Thời điểm vào bang (giây) — dùng cho điều kiện phó bản (`getNumDateFromJoinTimeToToday`) |
| `timeAskPea` | Thời điểm xin đậu gần nhất |

### 2.3 Bảng `clan` (DB)

| Cột | Kiểu | Default | Map sang code |
|---|---|---|---|
| `id` | int PK | | `id` |
| `NAME` | varchar(255) | | `name` |
| `NAME_2` | varchar(4) | | `name2` |
| `slogan` | varchar(255) | '' | `slogan` |
| `img_id` | int | 0 | `imgId` (đọc bằng `getByte`) |
| `power_point` | bigint | 0 | `powerPoint` |
| `max_member` | smallint | 10 | `maxMember` (đọc `getByte`) |
| `clan_point` | int | 0 | **`capsuleClan`** (capsule bang) |
| `LEVEL` | int | 1 | `level` (nếu < 1 thì ép = 1) |
| `members` | text | | JSON array các chuỗi JSON thành viên: `id, name, head, body, leg, role, donate, receive_donate, member_point, clan_point, join_time, ask_pea_time, power` |
| `tops` | text | | Luôn ghi chuỗi `"cc"` khi update (không dùng) |
| `create_time` | timestamp | now() | `createTime` |
| `thanhTichBDKB` | varchar(255) | '[0,0]' | `[levelDoneBanDoKhoBau, thoiGianHoanThanhBDKB]` |
| `thongTinLeader` | varchar(255) | '[0,0,0,0,0]' | `[leaderId, leaderName, head, body, leg]` |

Dữ liệu mẫu trong dump: 1 bang — id 0, tên `abc`, cờ 20 (Đao), 10 thành viên tối đa, cấp 1, 0 capsule, 1 thành viên là `admin` (id 1367, bang chủ).

Bảng `player` có các cột liên quan: `clan_id` (default -1), `data_clan_task` (JSON nhiệm vụ bang), `thanhTichBang`, `thanhTichKhiGas`, `thanhTichCDRD` (default `[0,0,0,0]`, lưu thành tích của bang chủ — xem `Clan.updatethanhTich*ForLeader`), `checkNhanQua` (chứa lượt nhận Capsule bang hằng ngày).

---

## 3. Tạo bang & giá cờ bang

### 3.1 Luồng tạo bang
`ClanService.getClan` → action `1` (REQUEST_FLAGS_CHOOSE_CREATE_CLAN) gửi danh sách cờ → action `2` (ACCEPT_CREATE_CLAN) gọi `createClan(player, imgId, name)`.

Điều kiện & xử lý (`ClanService.createClan`):

| Bước | Quy tắc |
|---|---|
| 1 | Người chơi **chưa có bang** (`player.clan == null`) |
| 2 | Tên bang **≤ 30 ký tự** ("Tên bang hội không được quá 30 ký tự"). Không kiểm tra rỗng, trùng tên hay ký tự đặc biệt |
| 3 | Cờ `imgId` phải tồn tại trong `Manager.FLAGS_BAGS` |
| 4 | Nếu `flag.gold > 0` → trừ vàng; thiếu thì báo "Bạn không đủ vàng, còn thiếu X vàng" |
| 5 | Nếu `flag.gem > 0` → trừ ngọc; thiếu thì báo thiếu ngọc |
| 6 | Tạo `Clan` (maxMember 10, level 1), người tạo là **Bang chủ**, `clan.insert()` ghi DB |
| 7 | Thông báo "Chúc mừng bạn đã tạo bang thành công." |

> Không có yêu cầu nhiệm vụ/sức mạnh khi **tạo** bang (khác với khi vào bang — xem mục 5).

### 3.2 Danh sách cờ có thể chọn & giá (`FlagBagService.getFlagsForChooseClan` + bảng `flag_bag`)

Thứ tự hiển thị theo mảng `flagsId = {0,8,7,6,5,4,3,2,1,18,17,16,15,14,13,12,11,10,9,27,26,25,24,23,36,32,33,34,35,19,22,21,20,29,37,38,69,70,71,77,78,79}`. Giá trị `-1` = không tính loại tiền đó.

| ID | Tên cờ | Vàng | Ngọc |
|---|---|---|---|
| 0 | Cờ xám | 10.000 | — |
| 1 | Cờ đen | 10.000 | — |
| 2 | Cờ xanh lá | 10.000 | — |
| 3 | Cờ xanh biển | 10.000 | — |
| 4 | Cờ hồng | 10.000 | — |
| 5 | Cờ cam | 10.000 | — |
| 6 | Cờ vàng | 10.000 | — |
| 7 | Cờ tím | 10.000 | — |
| 8 | Cờ xanh dạ | 10.000 | — |
| 9 | Cờ đỏ | 50.000 | — |
| 10 | Khăn xanh lá | 50.000 | — |
| 11 | Khăn xanh dương | 50.000 | — |
| 12 | Khăn vàng | 50.000 | — |
| 13 | Khăn tím | 50.000 | — |
| 14 | Khăn nâu | 50.000 | — |
| 15 | Khăn xám | 50.000 | — |
| 16 | Khăn đỏ | 50.000 | — |
| 17 | Khăn hồng | 50.000 | — |
| 18 | Khăn xanh dạ | 50.000 | — |
| 19 | Ba lô | — | 200 |
| 20 | Đao | — | 500 |
| 21 | Gậy | — | 400 |
| 22 | Mai rùa | — | 300 |
| 23 | Giỏ bơ | 100.000 | — |
| 24 | Giỏ dưa hấu | 100.000 | — |
| 25 | Giỏ củ cải trắng | 100.000 | — |
| 26 | Giỏ cà rốt | 100.000 | — |
| 27 | Giỏ chuối | 100.000 | — |
| 29 | Gậy phép | — | 600 |
| 32 | Lồng đèn Ông Sao | — | 50 |
| 33 | Lồng đèn Cá chép | — | 50 |
| 34 | Lồng đèn Kéo Quân | — | 50 |
| 35 | Lồng đèn Ông trăng | — | 50 |
| 36 | Lồng đèn Hội An | — | 50 |
| 37 | Gậy thần chết | 1.000.000.000 | — |
| 38 | Cánh dơi | 1.000.000.000 | — |
| 69 | Kiếm phát sáng | 1.000.000.000 | — |
| 70 | Búa Mjolnir | 1.000.000.000 | — |
| 71 | Búa Stormbreaker | 1.000.000.000 | — |
| 77 | Dao răng cưa | 1.000.000.000 | — |
| 78 | Cờ Hoa đăng | 1.000.000.000 | — |
| 79 | Cờ Hoa sen | 1.000.000.000 | — |

**Giá tạo bang rẻ nhất: 10.000 vàng** (cờ 0–8). Ngọc trừ ở đây là `inventory.gem` (ngọc xanh).

### 3.3 Đổi cờ / khẩu hiệu
`ClanService.changeInfoClan` (action `4`): nếu slogan khác rỗng → `changeSlogan` (chỉ bang chủ, cắt 250 ký tự, miễn phí); ngược lại → `changeFlag` (chỉ bang chủ, cờ mới khác cờ cũ, **trả lại đúng giá của cờ mới** như bảng trên).

### 3.4 Tên viết tắt
Qua NPC Dr. Brief tại map 153 (xem mục 12 — lưu ý nhánh này không truy cập được với DB hiện tại):
- "Đổi tên tên bang viết tắt" → form `Input.createFormBangHoi` → `Input` case `BANGHOI`: chỉ bang chủ, cooldown 60s, **2–4 ký tự**, không ký tự đặc biệt (`Util.haveSpecialCharacter`).
- "Chọn ngẫu nhiên tên bang viết tắt" → `Functions.generateRandomCharacters(Util.nextInt(2,4))`.

---

## 4. Chức vụ & quyền hạn

Hằng số trong `Clan`: `LEADER = 0` (Bang chủ), `DEPUTY = 1` (Phó bang), `MEMBER = 2` (Thành viên). Không giới hạn số phó bang.

| Hành động | Bang chủ | Phó bang | Thành viên | Hàm |
|---|:-:|:-:|:-:|---|
| Mời người chơi vào bang | ✔ | ✔ | ✘ | `sendInviteClan` |
| Duyệt / từ chối đơn xin vào | ✔ | ✘ | ✘ | `acceptAskJoinClan`, `cancelAskJoinClan` |
| Đuổi thành viên thường | ✔ | ✔ | ✘ | `kickOut` |
| Đuổi phó bang | ✔ | ✘ | ✘ | `kickOut` |
| Phong phó bang (cho MEMBER) | ✔ | ✔ | ✘ | `phongPho` |
| Cắt chức (về MEMBER) | ✔ | ✘ | ✘ | `catChuc` |
| Nhường bang chủ (chỉ cho **Phó bang**) | ✔ | ✘ | ✘ | `showMenuNhuongPc` → xác nhận `CONFIRM_NHUONG_PC` (511) → `phongPc` |
| Đổi cờ / khẩu hiệu | ✔ | ✘ | ✘ | `changeFlag`, `changeSlogan` |
| Đổi tên viết tắt, nâng cấp bang | ✔ | ✘ | ✘ | `DrDrief.confirmMenu` |
| Giải tán bang | ✔ | ✘ | ✘ | Quy Lão Kame → `Input` `DISSOLUTION_CLAN` |
| Rời bang | ✘ (phải nhường trước) | ✔ | ✔ | `leaveClan` |
| Gọi Gấu Tướng Cướp | ✔ | ✘ | ✘ | `GiuMaDauBo.confirmMenu` case 0 |
| Mở Khí gas hủy diệt | ✔ | ✘ | ✘ | `DestronGasService.openKhiGasHuyDiet` |
| Chat bang, xin đậu, cho đậu | ✔ | ✔ | ✔ | `chat`, `askForPea`, `clanDonate` |

Mã thao tác `clanRemote`: `-1` KICK_OUT, `0` PHONG_PC (nhường bang chủ), `1` PHONG_PHO, `2` CAT_CHUC.

Mọi thay đổi chức vụ đều tạo 1 `ClanMessage` màu đỏ ("Phong phó bang cho …", "Cắt chức phó bang của …", "Nhường chức bang chủ cho …", "Đuổi … ra khỏi bang.") và gọi `clan.update()`.

---

## 5. Gia nhập / mời / xin vào / rời / đuổi / giải tán

### 5.1 Mời vào bang (`sendInviteClan` → `acceptJoinClan`)

| Điều kiện kiểm tra | Thông báo |
|---|---|
| Người mời là bang chủ/phó bang | (im lặng nếu không) |
| Bang chưa đủ `maxMember` | "Bang đã đủ thành viên, không thể mời thêm." |
| Người được mời đã tới nhiệm vụ **≥ `ConstTask.TASK_10_0`** (nhiệm vụ chính id 10 "Nhiệm vụ thử sức", bước 0) | "X chưa thể vào bang lúc này" |
| Người được mời chưa có bang | "X đang ở trong bang nào đó, không thể mời" |
| Không đang giữ Ngọc rồng sao đen | "X đang giữ ngọc rồng sao đen, không thể mời" |

Khi đồng ý (`acceptJoinClan`, packet `clanInvite` action 1): kiểm tra lại nhiệm vụ ≥ TASK_10_0, không giữ NR sao đen, chưa có bang, bang chưa đầy → thêm với role MEMBER, `ItemTimeService.sendTextDoanhTrai`, `checkDoneTaskJoinClan` (nếu bang ≥ 2 thành viên thì mọi thành viên online được kiểm tra hoàn thành nhiệm vụ "vào bang"), `clan.update()`.

### 5.2 Xin vào bang (`askForJoinClan` → `acceptAskJoinClan` / `cancelAskJoinClan`)
- Người chơi chưa có bang gửi đơn → tạo `ClanMessage type=2, role=-1` hiển thị "Tên (sức mạnh)". Không tạo đơn trùng nếu đã có đơn trong 20 tin gần nhất.
- Đoạn kiểm tra "Bang hội đã đủ người" khi **xin** đã bị comment — có thể xin vào bang đầy (sẽ bị chặn khi duyệt).
- **Chỉ bang chủ** duyệt. Khi duyệt: người xin (online hoặc load từ DB bằng `MrBlue.loadById`) không giữ NR sao đen, chưa có bang, bang chưa đầy → vào bang. Nếu người đó offline thì ghi `notify` "Bạn đã gia nhập bang: …" và `PlayerDAO.updatePlayer`.
- Từ chối: tin nhắn đổi thành "Từ chối … vào bang.", người xin (nếu online) nhận "Bạn đã bị từ chối từ bang: …".
- Không kiểm tra yêu cầu nhiệm vụ TASK_10_0 ở luồng xin vào.

### 5.3 Rời bang (`showMenuLeaveClan` → `CONFIRM_LEAVE_CLAN` (510) → `leaveClan`)
- Bang chủ bị chặn: "Phải nhường chức bang chủ trước khi rời".
- Thành viên bị xóa khỏi `members`, gỡ cờ, gỡ text doanh trại, tin nhắn "X đã rời bang.".
- Không có thời gian chờ (cooldown) sau khi rời để vào bang khác; tuy nhiên `joinTime` reset khi vào bang mới nên các phó bản yêu cầu 1–2 ngày sẽ bị ảnh hưởng.

### 5.4 Đuổi (`kickOut`)
- Bang chủ đuổi được mọi người; phó bang chỉ đuổi được MEMBER.
- Người bị đuổi đang online: gỡ clan ngay; offline: `removeClanPlayer` chạy `update player set clan_id = -1`.

### 5.5 Giải tán bang
NPC **Quy Lão Kame** (map 5 Đảo Kamê) → menu "Giải tán Bang hội" (chỉ hiển thị với bang chủ) → xác nhận "Đồng ý" → form "Nhập OK để xác nhận giải tán bang hội." → `Input` case `DISSOLUTION_CLAN`: `clan.deleteDB(id)`, `Manager.CLANS.remove(clan)`, gỡ clan khỏi **chính bang chủ**, thông báo "Bang hội đã giải tán thành công." (xem Ghi chú về thành viên còn lại).

### 5.6 Tìm bang (`getClans` / `sendListClan`)
- Nếu tổng số bang ≤ 20: lọc toàn bộ theo `name.contains(keyword)`.
- Nếu > 20: chọn ngẫu nhiên vị trí bắt đầu `n ∈ [0, size-20]`, duyệt từ đó đến hết, lấy tối đa 20 bang khớp → kết quả **ngẫu nhiên**, có thể không thấy bang cần tìm.

---

## 6. Tin nhắn bang, xin đậu & cho đậu

| Loại (`ClanMessage.type`) | Tạo bởi | Nội dung |
|---|---|---|
| 0 — Chat / thông báo hệ thống | `chat`, các thao tác chức vụ | text + màu (`BLACK=0`, `RED=1`) |
| 1 — Xin đậu | `askForPea` | `receiveDonate / maxDonate` |
| 2 — Xin vào bang | `askForJoinClan` | tên + sức mạnh |

- Bảng tin giữ **tối đa 20 tin** (`addClanMessage`); chỉ lưu trong RAM, mất khi restart.
- **Xin đậu** (`askForPea`): cooldown **5 phút** (`timeAskPea + 5*60*1000`), mỗi lần xin tối đa **5 hạt** (`maxDonate = 5`). Thông báo "Vui lòng chờ … nữa để xin tiếp.".
- **Cho đậu** (`clanDonate`):
  - Lấy hạt đậu đầu tiên có `template.type == 6` trong **rương đồ** (`inventory.itemsBox`) của người cho — nếu không có: "Không tìm thấy đậu trong rương".
  - Trừ 1 hạt, tạo bản sao giữ nguyên option và thêm vào **hành trang** người nhận; người nhận được thông báo "X đã cho bạn <tên đậu>".
  - Người nhận phải còn trong bang (online, hoặc load được từ DB — offline thì ghi `notify` và lưu DB).
  - `cmg.receiveDonate++`, kiểm tra thành tựu `ConstAchievement.HO_TRO_DONG_DOI` cho người cho.
- Đậu thần/cây đậu: xem `18-npc.md` (MagicTree).

---

## 7. Cấp bang & số thành viên tối đa

- Bang mới: **cấp 1, tối đa 10 thành viên**.
- Nâng cấp: NPC Dr. Brief (nhánh `mapId == 153`) → "Chức năng bang hội" → "Nâng cấp Bang hội" → `ConstNpc.MENU_CLAN_UP` (`DrDrief.confirmMenu`). Chỉ bang chủ.
- Mỗi lần nâng: trừ capsule bang theo `ClanService.capsule(clan)`, `level++`, `maxMember++` (+1 thành viên).
- Chặn khi `clan.level > 10` ("Đang ở cấp độ cao nhất.") ⇒ **cấp tối đa thực tế là 11**, **thành viên tối đa 20**.
- Lời thoại NPC hứa "+1 ô trống tối đa rương bang" (khi level > 1) và "+Mở bán bùa bang cấp N" — **không có code triển khai** (xem mục 15).

| Nâng từ cấp → cấp | Capsule bang cần (`ClanService.capsule`) | Thành viên tối đa sau nâng | Capsule tích lũy |
|---|---|---|---|
| 1 → 2 | 100 | 11 | 100 |
| 2 → 3 | 300 | 12 | 400 |
| 3 → 4 | 500 | 13 | 900 |
| 4 → 5 | 700 | 14 | 1.600 |
| 5 → 6 | 900 | 15 | 2.500 |
| 6 → 7 | 1.100 | 16 | 3.600 |
| 7 → 8 | 1.300 | 17 | 4.900 |
| 8 → 9 | 1.500 | 18 | 6.400 |
| 9 → 10 | 1.700 | 19 | 8.100 |
| 10 → 11 | 1.900 | 20 | 10.000 |
| (cấp 11) | 2.100 — không bao giờ dùng vì bị chặn `level > 10` | — | — |
| cấp khác | 999.999 (giá trị mặc định) | — | — |

---

## 8. Capsule bang (điểm bang)

Capsule bang là "điểm bang" duy nhất có tác dụng thực tế; lưu ở `Clan.capsuleClan` (cột `clan.clan_point`). Mỗi `ClanMember` cũng ghi nhận `memberPoint` và `clanPoint` (đóng góp cá nhân, chỉ để hiển thị).

### 8.1 Nguồn nhận

| Nguồn | Lượng | Điều kiện | Nguồn code |
|---|---|---|---|
| Điểm danh tại Giu-ma Đầu Bò | **+1** capsule bang (+1 memberPoint/clanPoint) | Có bang; 1 lần/ngày (`player.event.luotNhanCapsuleBang`, reset về 1 trong `PlayerService.dailyLogin` khi sang ngày mới) | `GiuMaDauBo.confirmMenu` case 1 |
| Trả nhiệm vụ bang | **(level+1) × 10**: 10 / 20 / 30 / 40 / 50 | Hoàn thành nhiệm vụ bang | `TaskService.payClanTask` |
| Hạ Gấu Tướng Cướp | Tổng **50** capsule chia theo % sát thương | Người gây sát thương có bang | `GauTuongCuop.rewardContributors` |

### 8.2 Nơi tiêu

| Mục đích | Chi phí |
|---|---|
| Nâng cấp bang | Bảng mục 7 |
| Mua đồ trong Cửa hàng bang hội (tab 60/61/62) | Bảng mục 11 — **trừ vào capsule chung của bang**, bất kỳ thành viên nào cũng mua được |

---

## 9. Nhiệm vụ bang

Nguồn: `task/ClanTask.java`, `TaskService` (khối `CLAN TASK`), bảng `clan_task_template`, NPC Dr. Brief (map 153). Chi tiết hệ thống nhiệm vụ xem tài liệu nhiệm vụ.

| Thuộc tính | Giá trị |
|---|---|
| Số nhiệm vụ/ngày | **5** (`ConstTask.MAX_CLAN_TASK`), reset sau nửa đêm (`ClanTask.renew` → `Util.isAfterMidnight(receivedTime)`) |
| Nhận nhiệm vụ | Chọn **ngẫu nhiên 1 template** trong 59 mẫu, độ khó `level = Util.nextInt(5)` → 0..4 (dễ / bình thường / khó / rất khó / địa ngục) |
| Số lượng yêu cầu | Ngẫu nhiên trong khoảng `max_count_lv(level+1)` |
| Hủy nhiệm vụ | Mất 1 lượt trong ngày (`removeClanTask`) |
| Thưởng | **(level+1)×10 capsule bang** cho bang + cộng vào `memberPoint`/`clanPoint` của người trả; mất 1 lượt |
| Tiến độ | Thông báo mỗi mốc 10% (`notifyProcessClanTask`) |
| Lưu | Cột `player.data_clan_task` = `[templateId, receivedTime, count, maxCount, leftTask, level]` |

### 9.1 Mẫu nhiệm vụ (`clan_task_template`)

| ID | Tên | Dễ (lv1) | Bình thường (lv2) | Khó (lv3) | Rất khó (lv4) | Địa ngục (lv5) |
|---|---|---|---|---|---|---|
| 0–23 | Hạ %1 khủng long, lợn lòi, quỷ đất, khủng long mẹ, lợn lòi mẹ, quỷ đất mẹ, thằn lằn bay, phi long, quỷ bay, thằn lằn mẹ, phi long mẹ, quỷ bay mẹ, heo rừng, heo da xanh, heo xayda, ốc mượn hồn, ốc sên, heo xayda mẹ, không tặc, quỷ đầu to, quỷ địa ngục, heo rừng mẹ, heo xanh mẹ, alien | 1-20 | 20-100 | 100-500 | 500-2000 | 2000-5000 |
| 24–57 | Hạ %1 tambourine, drum, akkuman, nappa, soldier, appule, raspberry, thằn lằn xanh, quỷ đầu nhọn, quỷ đầu vàng, quỷ da tím, quỷ già, cá sấu, dơi da xanh, quỷ chim, lính đầu trọc, lính tai dài, lính vũ trụ, khỉ lông đen, khỉ giáp sắt, khỉ lông đỏ, khỉ lông vàng, xên con cấp 1–8, tai tím, abo, kado, da xanh | 1-5 | 5-20 | 20-100 | 100-500 | 500-1000 |
| 58 | Nhặt %1 vàng | 1000-3000 | 3000-20000 | 20000-100000 | 100000-10000000 | 10000000-100000000 |

- Nhiệm vụ 0–57: đếm khi giết quái có `tempId` tương ứng (`TaskService.checkDoneClanTaskKillMob`, gọi từ `Mob.java`).
- Nhiệm vụ 58: cộng `quantity` khi nhặt item `type == 9` (vàng) (`checkDoneClanTaskPickItem`, gọi từ `Zone.java`).

---

## 10. Gấu Tướng Cướp (boss bang)

Nguồn: `GiuMaDauBo.confirmMenu` case 0, `mob_bigboss/GauTuongCuop.java`.

| Thuộc tính | Giá trị |
|---|---|
| Người gọi | **Chỉ bang chủ** (so khớp theo `name`) |
| Điều kiện | Có ít nhất **3 thành viên bang online trong cùng khu** (tính cả bang chủ) |
| Vị trí | Xuất hiện ngay cạnh bang chủ (x ± 30) tại khu hiện tại |
| HP | **2.000.000.000** |
| Nhận sát thương | Chỉ **5%** sát thương gây ra (tối thiểu 1) |
| Sát thương đánh | **19.999.999** mỗi mục tiêu |
| Tốc độ đánh | Mỗi **300 ms**, đánh AOE: tầm 50/100/150/200 tùy action ngẫu nhiên 11–15; nếu không ai trong tầm thì lao tới 1 người ngẫu nhiên |
| Thưởng khi chết | Tổng **50 capsule bang**, mỗi người (có bang) nhận `floor(tỉ lệ sát thương × 50)`, tối thiểu 1; phần dư cộng cho người gây sát thương nhiều nhất. Capsule được cộng vào `capsuleClan` của **bang của từng người**, và `memberPoint`/`clanPoint` của họ |
| Cooldown / số lần/ngày | **Không có** trong code |

---

## 11. Cửa hàng bang hội (SHOP_CLAN)

- Mở từ NPC **Giu-ma Đầu Bò** (npc 47, map 153) → "Cửa Hàng Bang hội" → `ShopService.opendShop(player, "SHOP_CLAN", false)`.
- Bảng `shop`: id 35, npc_id 47, tag `SHOP_CLAN`, type_shop 3. Tab (`tab_shop`): **60** "Cửa Hàng Item", **61** "Cửa Hàng Pet", **62** "Cửa Hàng Ván Bay".
- Thanh toán xử lý riêng trong `ShopService` (khối "Đổi bằng điểm Capsule Bang"): **giá lấy từ switch cứng trong code**, không dùng cột `cost` của `item_shop`. Yêu cầu có bang, đủ capsule bang; trừ `player.clan.capsuleClan`. Không kiểm tra chức vụ, không giới hạn số lượng.

| Tab | Item ID | Tên (item_template) | Giá code (capsule bang) | `cost` trong DB |
|---|---|---|---|---|
| 60 | 1794 | Con dấu | 1 | 1 |
| 60 | 1204 | Mảnh Rồng thần Namếc | 1 | 1 |
| 60 | 1791 | Mảnh khỉ Oorazu | **3** | 2 |
| 60 | 1792 | Mảnh khỉ Oorazu 1 | **4** | 3 |
| 60 | 1793 | Mảnh khỉ Oorazu 2 | **không bán được** (không có trong switch → "Vật phẩm này không thể mua bằng điểm Capsule Bang.") | 4 |
| 60 | 1634 | Cápsule Vỡ | 10 | 10 |
| 60 | 1439 | Đá mài | 1 | 1 |
| 60 | 1438 | Dùi đục | 1 | 1 |
| 60 | 1423 | Hematite | 1 | 1 |
| 60 | 987 | Đá bảo vệ | 1 | 1 |
| 60 | 1635 | Cỏ bốn lá | 1 | 1 |
| — | 1790 | Mẹ Rồng | 2 (có trong code nhưng **không có trong item_shop**) | — |
| 61 | 1620 | Pet Baby Shark | 50 | 50 |
| 61 | 1748 | Pet tuần lộc | 50 | 50 |
| 61 | 1750 | Pet Khủng Long ngok | 50 | 50 |
| 61 | 1729 | Pet Rồng Xanh | 50 | 50 |
| 61 | 1727 | Pet Ma cầu mưa | 50 | 50 |
| 61 | 1714 | Pet Giru | 50 | 50 |
| 61 | 1682 | Pet Hải Ly | 50 | 50 |
| 61 | 1683 | Pet Hải Ly Ong Vàng | 50 | 50 |
| 61 | 1668 | Pet Capybara hồng | 50 | 50 |
| 61 | 1629 | Pet Capybara đeo ba lô | 50 | 50 |
| 61 | 1630 | Pet Capybara xì mũi | 50 | 50 |
| 61 | 1631 | Pet Zịt vàng bối rối | 50 | 50 |
| 61 | 1573 | Xe tăng Santa | 50 | 50 |
| 61 | 1550 | Pet Godzilla | 50 | 50 |
| 61 | 1551 | Pet Kong | 50 | 50 |
| 62 | 1541 | Môtô Bun ma | 50 | 50 |
| 62 | 1563 | Ván bay té nước | 50 | 50 |
| 62 | 1724 | Ván bay Sọ Dừa | 50 | 50 |
| 62 | 1733 | Thú cưỡi Thích Kim Quy | 50 | 50 |
| 62 | 1734 | Thú cưỡi Phong Xích Lan | 50 | 50 |
| 62 | 1749 | Thú cưỡi Cây thông | 50 | 50 |

Chỉ số/option của item lấy theo `item_shop_option` qua `ItemService.createItemFromItemShop` (xem tài liệu vật phẩm/shop).

---

## 12. Lãnh địa Bang Hội (map 153) & NPC liên quan

`map_template` id **153 "Lãnh địa Bang Hội"**: 10 khu, 15 người/khu, không waypoint, không quái, NPC duy nhất trong DB: **47 Giu-ma Đầu Bò** tại (369, 432). Map này nằm trong danh sách map không cho trọng tài/không hợp lệ ở `Zone.isKhongCoTrongTaiTrongKhu`. Không có code chặn người ngoài bang vào khu (chỉ NPC dẫn đường kiểm tra có bang).

| NPC (id) | Map | Chức năng liên quan bang | Nguồn |
|---|---|---|---|
| Quy Lão Kame (13) | 5 Đảo Kamê | "Về khu vực bang" → dịch chuyển tới map 153 tại x ∈ [100,200], y 432 (cần có bang); "Kho báu dưới biển" (Bản đồ kho báu, xem 14); "Giải tán Bang hội" (bang chủ) | `QuyLaoKame.openBaseMenu`, `handleClanMapChange`, `handleClanDissolution`, `handleMenu4` |
| Giu-ma Đầu Bò (47) | 153 Lãnh địa Bang Hội | Menu: "Khiêu chiến Boss" (Gấu Tướng Cướp, mục 10) · "Điểm danh +1 Capsule Bang" · "OK" (dịch chuyển — **yêu cầu sức mạnh ≥ 40.000.000.000**, `player.type = 5`, không liên quan bang; lời thoại nói về mảnh vỡ/mảnh hồn bông tai Porata) · "Cửa Hàng Bang hội" · "Từ chối" | `GiuMaDauBo` |
| Dr. Brief (10) — nhánh `mapId == 153` | *(DB đặt Dr. Brief ở map 24, 84 — không có ở 153)* | Menu bang chủ: "Chức năng bang hội" (Đổi tên viết tắt / Chọn ngẫu nhiên tên viết tắt / Nâng cấp Bang hội) · "Nhiệm vụ Bang [x/5]" · "Đảo Kame" (bay về map 5). Thành viên thường: "Nhiệm vụ Bang" · "Đảo Kame" | `DrDrief.openBaseMenu`/`confirmMenu` |
| Lính canh (25) | 27 Rừng Bamboo | Doanh trại Độc Nhãn | `LinhCanh` |
| Mr Popo (67) | 0 Làng Aru | Khí gas hủy diệt (Destron Gas), Top 100 bang | `MrPoPo` |
| Thần Vũ Trụ (20) | 48 Hành tinh Kaio (DB còn đặt ở 78) | Con đường rắn độc, Top bang CDRD | `ThanVuTru` |

---

## 13. Phó bản / hoạt động dành cho bang

Tóm tắt điều kiện gắn với bang; chi tiết quái, boss, phần thưởng, cấp độ xem **`14-ban-do-pho-ban.md`**.

| Phó bản | NPC | Điều kiện liên quan bang | Giới hạn | Thời lượng | Nguồn |
|---|---|---|---|---|---|
| Doanh trại Độc Nhãn (`RedRibbonHQ`) | Lính canh (map 27) | Có bang; tài khoản đã **mở thành viên** (`session.actived`); bang ≥ **5** thành viên (`N_PLAYER_CLAN`); vào bang ≥ **1 ngày**; có ≥ **1** đồng đội cùng bang đứng gần (x 1285–1645, `N_PLAYER_MAP`) | **1 lần/ngày/bang**; cá nhân 1 lần/ngày; tối đa 50 instance (`AVAILABLE`) | 30 phút (`TIME_DOANH_TRAI` 1.800.000 ms); thời gian nhặt 5 phút | `LinhCanh`, `RedRibbonHQService.joinDoanhTrai`, `RedRibbonHQ` |
| Bản đồ kho báu (`BanDoKhoBau`) | Quy Lão Kame (map 5) | Có bang; sức mạnh ≥ **2.000.000.000** (admin bỏ qua); tiêu **1 item 611** (bản đồ kho báu) để mở; cấp 1–110 | Cá nhân **3 lần/ngày**; 50 instance | 30 phút | `QuyLaoKame.handleMenuOpenDBKB`, `TreasureUnderSeaService.openBanDoKhoBau` |
| Con đường rắn độc (`SnakeWay`) | Thần Vũ Trụ (map 48) | Có bang; NPC kiểm tra vào bang ≥ 1 ngày, service yêu cầu ≥ **2 ngày**; cấp 1–110 | Cá nhân **1 lần/7 ngày** (`isTimeDifferenceGreaterThanNDays(lastTimeJoinCDRD, 7)`); 50 instance | 30 phút | `ThanVuTru`, `SnakeWayService.openConDuongRanDoc` |
| Khí gas hủy diệt (`DestronGas`) | Mr Popo (map 0) | Có bang; vào bang ≥ **2 ngày**; đã mở thành viên; **chỉ bang chủ mở**; `N_PLAYER_CLAN = 0` (thực tế không yêu cầu số thành viên); cấp 1–110 | Bang mở tối đa **3 lần/ngày** (`timesPerDayKGHD`); 50 instance | 30 phút | `MrPoPo`, `DestronGasService.openKhiGasHuyDiet` |
| Ngọc rồng sao đen (`BlackBallWar`) | — | Người đang giữ NR sao đen không thể mời/vào bang; cùng bang không đánh nhau trong map NRSĐ; khi 1 người giành ngọc, **mọi thành viên bang** (kể cả offline, load từ DB) nhận thưởng sao đen | 20h00 mở, 20h30 được nhặt, 21h00 đóng | | `BlackBallWar`, `SkillService.useSkill` |
| Ngọc rồng Namek | — | Cùng bang không gây sát thương cho nhau khi một bên cầm NR Namek (`idNRNM`) | | | `Player.injured`, `SkillService.useSkill`, `NgocRongNamecService` |

Thành tích & bảng xếp hạng bang: `Service.showTopClanBDKB`, `showMyTopClanBDKB`, `showTopClanKhiGas`, `showTopClanCDRD`; thành tích lưu vào `clan.thanhTichBDKB` và các cột `player.thanhTichBang/thanhTichKhiGas/thanhTichCDRD` của **bang chủ**. Khi hoàn thành cấp cao hơn (hoặc cùng cấp nhưng nhanh hơn) thì ghi đè (`BanDoKhoBau`, `DestronGas`, `SnakeWay` quanh dòng 145–160).

---

## 14. Lợi ích khác khi ở trong bang

| Lợi ích | Chi tiết | Nguồn |
|---|---|---|
| Chia sẻ tiềm năng | Khi người chơi nhận SM/TN "gốc" (`isOri = true` — đánh quái, TaoPaiPai, đệ tử, dùng item…), **mỗi thành viên bang khác online cùng khu** nhận **tiềm năng** (type 1) = `param / |chênh lệch cấp|`; nếu cùng cấp (chênh 0) thì nhận **100%** `param`. Cấp tính theo mốc sức mạnh `Service.getCurrLevel` | `Clan.addSMTNClan`, `Service.addSMTN` |
| Cờ trên lưng | Người chơi mang cờ bang (`Player` ~dòng 1017 trả `clan.imgId`) | `Service.sendFlagBag` |
| Tên viết tắt | Hiển thị `[name2] Tên` | `Service.java` ~803 |
| Không đánh nhau cùng bang | Trong map NR sao đen và khi cầm NR Namek: "Ê cùng bang mà" | `SkillService.useSkill`, `Player.injured` |
| Nhiệm vụ chính | Nhiệm vụ "vào bang" hoàn thành khi bang có ≥ 2 thành viên | `ClanService.checkDoneTaskJoinClan` |

---

## 15. Kho bang / rương bang / bùa bang

- **Không có** cài đặt kho bang/rương bang hay bùa bang trong source: không có trường/bảng/packet nào cho rương bang; grep "rương bang", "bùa bang" chỉ xuất hiện trong chuỗi lời thoại của Dr. Brief khi nâng cấp bang (`DrDrief.java` dòng ~149–151).
- "Cửa hàng bang hội" (mục 11) là cơ chế tiêu điểm bang duy nhất.

---

## 16. Lưu trữ DB & vòng đời dữ liệu

| Thời điểm | Hành vi | Nguồn |
|---|---|---|
| Khởi động server | `select * from clan` → dựng `Clan` + `ClanMember` từ JSON; `NEXT_ID = max(id)+1` | `Manager` (khối `//load clan`) |
| Người chơi login | Đọc `player.clan_id`, tìm bang bằng **tìm kiếm nhị phân** trên `Manager.CLANS` (giả định danh sách sắp xếp theo id) và gắn `player.clan`, `clanMember` | `MrBlue` ~dòng 201, `ClanService.getClanById` |
| Tạo bang | `Clan.insert()` | |
| Hầu hết thao tác thành viên/chức vụ/cờ/slogan/tên viết tắt | `Clan.update()` | |
| Tắt server | `ClanService.close()` batch update tất cả bang (không ghi `thanhTichBDKB`, `thongTinLeader`) | |
| Giải tán | `Clan.deleteDB` | |

---

## 17. Ghi chú / điểm cần lưu ý

1. **Chức năng nâng cấp bang / nhiệm vụ bang / đổi tên viết tắt không truy cập được với DB hiện tại**: toàn bộ nằm trong nhánh `DrDrief` `mapId == 153`, nhưng `map_template` 153 chỉ đặt NPC 47 (Giu-ma Đầu Bò); Dr. Brief (10) chỉ ở map 24 và 84. Muốn dùng phải thêm `[10,x,y]` vào cột `npcs` của map 153. Khi đó, người **không có bang** mở menu sẽ thấy "Đảo Kame/Từ chối" nhưng `confirmMenu` không xử lý (bấm không có tác dụng).
2. **Capsule bang không được lưu ngay**: `DrDrief` (nâng cấp), `TaskService.payClanTask`, `GiuMaDauBo` (điểm danh), `GauTuongCuop`, `ShopService` (mua shop bang) đều thay đổi `capsuleClan`/`level`/`maxMember` mà **không gọi `clan.update()`** — chỉ được ghi khi có thao tác khác gọi `update()` hoặc khi tắt server đúng cách (`ClanService.close`). Crash server = mất dữ liệu.
3. **Gấu Tướng Cướp không có cooldown**: bang chủ + 3 người trong khu có thể gọi liên tục, mỗi con thưởng 50 capsule → có thể farm capsule bang vô hạn (mua pet/thú cưỡi 50 capsule, nâng cấp bang). Ngoài ra kiểm tra bang chủ dựa trên `m.name.equals(player.name)` thay vì id.
4. **Bảo mật `acceptJoinClan`**: server không lưu/kiểm tra lời mời (mã `758435` gửi kèm là hằng số và không được xác minh). Client sửa đổi có thể gửi packet `clanInvite` action 1 với clanId bất kỳ để **tự vào bất kỳ bang nào chưa đầy**.
5. **Giải tán bang không dọn thành viên**: chỉ bang chủ bị gỡ; các thành viên khác online vẫn giữ tham chiếu `player.clan` tới object đã xóa, và cột `player.clan_id` của họ vẫn trỏ tới id cũ (khi login lại, `getClanById` ném exception và bị bỏ qua). Nếu `NEXT_ID` sau này tái sử dụng id (chỉ khi xóa bang có id lớn nhất rồi restart) thì người chơi cũ có thể "dính" vào bang mới nhưng không có trong `members`.
6. **NPE tiềm ẩn**: `kickOut` gọi `clan.getClanMember` trước khi kiểm tra `clan != null`; `acceptAskJoinClan` dùng `cmg.playerId` trong vòng lặp trước khi kiểm tra `cmg != null`; `showMenuNhuongPc` không kiểm tra `player.clan` null.
7. **Phó bang có thể phong phó bang** cho thành viên khác (`phongPho` cho phép `isDeputy`), nhưng không duyệt được đơn xin vào (chỉ bang chủ) — bất nhất quyền.
8. Luồng **xin vào bang** không kiểm tra điều kiện nhiệm vụ `TASK_10_0` (khác luồng mời), và đoạn chặn bang đầy khi xin đã bị comment.
9. `sendMyClan(player)` bị gọi trong vòng lặp thành viên thay vì `sendMyClan(pl)` ở `DrDrief` (MENU_CLAN_UP) và `TaskService.payClanTask` → chỉ người thao tác được refresh (gửi lặp nhiều lần), các thành viên khác không thấy cấp/capsule mới ngay.
10. `ClanMember.donate` / `receiveDonate` không bao giờ tăng; `Clan.powerPoint` không bao giờ được tính — client luôn hiển thị giá trị cũ (thường 0).
11. `Clan.updateThongTinLeader` ghi thông tin leader vào cột **`thanhTichBDKB`** (sai cột, đúng ra là `thongTinLeader`) và **được gọi** ở `BanDoKhoBau.sendThanhTichBanDoKhoBau` mỗi khi hoàn thành Bản đồ kho báu. Chuỗi ghi vào có dạng `[1367,admin,383,384,385]` (tên không có dấu nháy → JSON không hợp lệ). Nếu server khởi động lại trước khi có một `clan.update()` khác ghi đè cột này, `JSONValue.parse` trả `null` ở khối load clan trong `Manager` → `NullPointerException` khi gọi `dataArray.isEmpty()` → có thể làm hỏng quá trình load bang hội.
12. Cột `tops` luôn bị ghi đè bằng chuỗi `"cc"`; `insert()` ghi `"[]"`.
13. **Giá shop bang trong code lệch DB**: 1791 (code 3 / DB 2), 1792 (code 4 / DB 3), 1793 có trong shop nhưng không bán được, 1790 có giá trong code nhưng không có trong shop. Client hiển thị giá theo DB nên người chơi thấy giá sai.
14. Shop bang trừ capsule **chung của bang**, không giới hạn chức vụ → thành viên thường có thể tiêu hết capsule bang dành cho nâng cấp.
15. Cấp tối đa lệch lời thoại: `capsule()` định nghĩa giá cho cấp 11 (2.100) nhưng bị chặn `level > 10`; cấp tối đa thực tế 11, 20 thành viên. `maxMember` là `byte`.
16. Điều kiện thời gian vào bang không thống nhất: Thần Vũ Trụ kiểm tra ≥ 1 ngày nhưng `SnakeWayService` yêu cầu ≥ 2 ngày và `return` **không báo lỗi**; Mr Popo báo "ít nhất 5 thành viên" nhưng `DestronGas.N_PLAYER_CLAN = 0` nên kiểm tra luôn qua.
17. Chia sẻ tiềm năng `addSMTNClan`: thành viên cùng cấp nhận **100%** lượng TN gốc (không chia), dễ bị lợi dụng bằng cách đứng chung khu; khi thành viên offline không bị ảnh hưởng vì chỉ duyệt `membersInGame`.
18. `getClanById` dùng tìm kiếm nhị phân và giả định `Manager.CLANS` sắp xếp theo id; câu SQL load không có `ORDER BY` (thực tế InnoDB trả theo PK nên thường vẫn đúng).
19. Tìm bang khi có > 20 bang trả kết quả ngẫu nhiên (mục 5.6).
20. Tên bang không kiểm tra rỗng/trùng/ký tự đặc biệt; chat bang không giới hạn độ dài; tin nhắn bang chỉ lưu RAM (20 tin).
21. Kho bang / rương bang / bùa bang **chưa được cài đặt** dù lời thoại nâng cấp có nhắc tới.
