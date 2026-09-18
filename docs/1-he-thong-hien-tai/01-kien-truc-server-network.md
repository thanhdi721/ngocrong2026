# 01 — Kiến trúc server & tầng network

> Mô tả kiến trúc runtime của server NRO Teamobi2026: tầng mạng, giao thức gói tin, `Controller` xử lý từng cmd, luồng đăng nhập / tạo nhân vật / thoát, lưu dữ liệu, manager nền, interface, tiện ích, gửi dữ liệu game cho client, chống spam, bot và multi-server.
> Nguồn: `SRC/src/nro/models/**`. Xem thêm `00-tong-quan.md` (khởi động, cấu hình) và `15-lenh-admin-gm.md` (lệnh admin).

## Mục lục

1. [Sơ đồ kiến trúc tổng](#1-sơ-đồ-kiến-trúc-tổng)
2. [Tầng network](#2-tầng-network)
   - 2.1 [Các lớp chính](#21-các-lớp-chính)
   - 2.2 [Vòng đời một kết nối](#22-vòng-đời-một-kết-nối)
   - 2.3 [Định dạng gói tin & mã hoá](#23-định-dạng-gói-tin--mã-hoá)
   - 2.4 [Bắt tay (handshake) key](#24-bắt-tay-handshake-key)
3. [Controller – bảng cmd client → server](#3-controller--bảng-cmd-client--server)
   - 3.1 [Cmd chính](#31-cmd-chính)
   - 3.2 [Sub-command của `-29` NOT_LOGIN](#32-sub-command-của--29-not_login)
   - 3.3 [Sub-command của `-28` NOT_MAP](#33-sub-command-của--28-not_map)
   - 3.4 [Sub-command của `-30` SUB_COMMAND](#34-sub-command-của--30-sub_command)
   - 3.5 [Toàn bộ hằng số `Cmd_message`](#35-toàn-bộ-hằng-số-cmd_message)
4. [Luồng đăng nhập](#4-luồng-đăng-nhập)
5. [Luồng tạo nhân vật](#5-luồng-tạo-nhân-vật)
6. [Luồng vào map sau đăng nhập](#6-luồng-vào-map-sau-đăng-nhập)
7. [Luồng thoát / mất kết nối](#7-luồng-thoát--mất-kết-nối)
8. [Lưu dữ liệu](#8-lưu-dữ-liệu)
9. [Gửi dữ liệu game cho client (`DataGame`, `ItemData`)](#9-gửi-dữ-liệu-game-cho-client)
10. [Tầng truy cập DB (`LocalManager`, `ResultSetImpl`)](#10-tầng-truy-cập-db)
11. [Package `managers`](#11-package-managers)
12. [Package `interfaces`](#12-package-interfaces)
13. [Package `utils`](#13-package-utils)
14. [Chống spam, giới hạn IP, giới hạn tần suất](#14-chống-spam-giới-hạn-ip-giới-hạn-tần-suất)
15. [Bot (package `Bot`)](#15-bot-package-bot)
16. [Multi-server](#16-multi-server)
17. [Ghi chú / điểm cần lưu ý](#17-ghi-chú--điểm-cần-lưu-ý)

---

## 1. Sơ đồ kiến trúc tổng

```
                    ┌──────────────────────────── ServerManager ───────────────────────────┐
 Client (Unity) ──TCP:14445──► Network (NIO accept) ──► SessionFactory → MySession            │
                    │           │ canConnectWithIp()                                        │
                    │           ▼                                                           │
                    │   Collector thread ── MessageSendCollect.readMessage (XOR) ──►        │
                    │        cmd -27 ⇒ session.sendKey()                                     │
                    │        cmd khác ⇒ Controller.onMessage(session, msg)                   │
                    │                          │                                             │
                    │                          ├─► Service / PlayerService / *Service ...    │
                    │                          ├─► MenuController → Npc.confirmMenu          │
                    │                          ├─► Command.chat (lệnh admin)                 │
                    │                          └─► DataGame / ItemData (gửi tài nguyên)      │
                    │   Sender thread ◄── session.sendMessage(msg) (hàng đợi LinkedBlockingDeque)
                    │                                                                        │
                    │ Client (singleton): danh sách Player online, kick, timeWait            │
                    │ Manager: dữ liệu tĩnh, MAPS/Zone update 1s                             │
                    │ LocalManager (HikariCP) ◄──► MySQL                                     │
                    └────────────────────────────────────────────────────────────────────────┘
```

---

## 2. Tầng network

### 2.1. Các lớp chính

| Lớp | File | Vai trò |
|---|---|---|
| `Network` (singleton, `implements INetwork, Runnable`) | `network/Network.java` | Mở `ServerSocketChannel` non-blocking, đăng ký `OP_ACCEPT` (giá trị 16), vòng lặp `selector.select(500)`; với mỗi socket: `setTcpNoDelay(true)`, tạo session qua `SessionFactory`, gọi `acceptHandler.sessionInit`, `SessionManager.putSession` |
| `SessionFactory` | `network/SessionFactory.java` | Tạo session bằng reflection: `clazz.getConstructor(Socket.class).newInstance(socket)`; lớp được set là `MySession` (`ServerManager.activeServerSocket`) |
| `Session` (`implements ISession`) | `network/Session.java` | Id tăng dần (`ID_INIT++`), IP, key mã hoá mặc định `"NRO".getBytes()`, buffer gửi/nhận `0x100000` (1 MiB), tạo 2 thread `Sender - IP : x` và `Collector - IP : x` |
| `MySession extends Session` | `network/MySession.java` | Thêm dữ liệu tài khoản: `player`, `userId`, `uu` (username), `pp` (password), `isAdmin`, `typeClient`, `zoomLevel`, `version`, `vnd`, `tongnap`, `vip`, `goldBar`, `luotquay`, `timeWait` (mặc định **100**), `joinedGame`… và hàm `login()` |
| `Collector` | `network/Collector.java` | Thread đọc: lặp `readMessage`; cmd `-27` → `session.sendKey()`, còn lại → `messageHandler.onMessage` (tức `Controller`). Khi lỗi/đóng → `acceptHandler.sessionDisconnect(session)` rồi `session.disconnect()` |
| `Sender` | `network/Sender.java` | Thread ghi: hàng đợi `LinkedBlockingDeque<Message>`, poll tối đa 5 s, rỗng thì ngủ 10 ms. `doSendMessage` là `synchronized` |
| `MessageSendCollect` (`implements IMessageSendCollect`) | `network/MessageSendCollect.java` | Đóng gói/giải gói byte, mã hoá XOR (mục 2.3). Mỗi session có 1 instance riêng (con trỏ `curR`, `curW`) |
| `Message` (`implements IMessage`) | `network/Message.java` | `command` (byte) + `DataOutputStream` (ghi) hoặc `DataInputStream` (đọc); hỗ trợ `readImage/writeImage` |
| `KeyHandler` / `MyKeyHandler` | `network/KeyHandler.java`, `MyKeyHandler.java` | Gửi key cho client (mục 2.4) |
| `SessionManager` | `network/SessionManager.java` | `CopyOnWriteArrayList<ISession>`; `findByID`, `getNumSession`; có `startCleanupThread()` (10 s) nhưng **không được gọi** |
| `QueueHandler` | `network/QueueHandler.java` | Hàng đợi xử lý message (giới hạn 500, nghỉ 33 ms) – **không được dùng** |

### 2.2. Vòng đời một kết nối

1. `Network.run` accept socket → `SessionFactory.cloneSession(MySession.class, socket)`.
2. `ServerManager.activeServerSocket` → `sessionInit(is)`:
   - `canConnectWithIp(ip)`: nếu số kết nối của IP ≥ `Manager.MAX_PER_IP` → `is.disconnect()`.
   - Gắn `Controller.gI()` làm message handler, `new MessageSendCollect()`, `new MyKeyHandler()`, rồi **chỉ** `startCollect()` (thread Sender chưa chạy).
3. Client gửi `-27` → `MySession.sendKey()` → gửi key (đồng bộ) → `startSend()` bật thread Sender.
4. `Client.update()` (mỗi 1 s) giảm `session.timeWait` (bắt đầu 100); chạm 0 → `kickSession`. Sau khi login thành công `timeWait = 0` ⇒ **session không đăng nhập trong ~100 giây sẽ bị kick** (`server/Client.java` dòng 159-174).
5. Khi socket lỗi/đóng: `Collector` → `sessionDisconnect` → `Client.kickSession` + `ServerManager.disconnect` (giảm đếm IP) → `session.disconnect()` (đóng socket, `SessionManager.removeSession`).

### 2.3. Định dạng gói tin & mã hoá

Nguồn: `network/MessageSendCollect.java`.

**Client → Server** (`readMessage`):

| Trường | Chưa có key (`sentKey=false`) | Đã có key |
|---|---|---|
| cmd | 1 byte | 1 byte XOR key |
| size | 2 byte `unsigned short` big-endian | 2 byte, mỗi byte XOR key: `size = (b1<<8) | b2` |
| data | `size` byte | mỗi byte XOR key |

⇒ gói client gửi lên tối đa **65.535 byte**.

**Server → Client** (`doSendMessage`):

| Trường | Giá trị |
|---|---|
| cmd | 1 byte (XOR key nếu `sentKey`) |
| size – với cmd **`-32, -66, -74, 11, -67, -87, 66`** (gói tài nguyên lớn) | **3 byte**: `writeKey(size) - 128`, `writeKey(size>>8) - 128`, `writeKey(size>>16) - 128` |
| size – các cmd khác, đã có key | 2 byte XOR key (`size>>8`, `size&0xFF`) |
| size – chưa có key | `writeShort(size)` |
| data | mỗi byte XOR key nếu `sentKey` |
| Message rỗng (`data == null`) | `writeShort(0)` |

**Thuật toán XOR**: `b' = key[cur] ^ b`, `cur = (cur+1) % key.length`; con trỏ đọc `curR` và ghi `curW` độc lập. Key mặc định `"NRO"` = `{78, 82, 79}`; `Network.randomKey` mặc định `false` (không có chỗ bật) nên key luôn là `"NRO"`.

### 2.4. Bắt tay (handshake) key

1. Client gửi cmd `-27` (`GET_SESSION_ID`). `Collector` chặn cmd này và gọi `session.sendKey()` (không qua `Controller`).
2. `Session.sendKey()` → `MyKeyHandler.sendKey()`:
   - `KeyHandler.sendKey`: tạo message `-27`: `byte len`, `byte key[0]`, rồi `key[i] ^ key[i-1]` với i = 1..len-1; gửi **đồng bộ** qua `doSendMessage` (chưa mã hoá), sau đó `setSentKey(true)`.
   - `DataGame.sendDataImageVersion(session)` (thân hàm rỗng).
   - `DataGame.sendVersionRes(session)`: cmd `-74`, `byte 0`, `int vsRes (=1)`.
3. `MySession.sendKey()` gọi `startSend()` để chạy thread Sender.

> `MySession.sendSessionKey()` (key `{0}`) và `case -27` trong `Controller.onMessage` là code không bao giờ chạy (Collector đã chặn -27).

---

## 3. Controller – bảng cmd client → server

Nguồn: `SRC/src/nro/models/server/Controller.java`, hàm `onMessage` (dòng 76-717). Tên hằng lấy từ `SRC/src/nro/models/consts/Cmd_message.java`. Cột "Cần player" = `player != null` (đã vào game).

Các kiểm tra lặp lại:
- **TX** = `TransactionService.gI().check(player)` – đang giao dịch thì báo "Không thể thực hiện".
- **BV** = `player.baovetaikhoan` – bật bảo vệ tài khoản thì báo "Chức năng bảo vệ đã được bật…".
- **BT** = `!Maintenance.isRunning`.

Mọi exception trong `onMessage` chỉ được log khi biến đếm `errors < 5` (biến instance của singleton ⇒ cả server chỉ log 5 lỗi đầu tiên).

### 3.1. Cmd chính

| Cmd | Hằng `Cmd_message` | Cần player | Kiểm tra | Xử lý | Chức năng |
|---|---|---|---|---|---|
| `-100` | `KIGUI` | ✔ | TX, BV | `ConsignShopService`: action `0` ký gửi (`short idItem, byte moneyType, int money, quantity` = `int` nếu `version ≥ 220` ngược lại `byte`), `1`/`2` `claimOrDel`, `3` `buyItem`, `4` `openShopKyGui(moneyType, page)`, `5` `upItemToTop`; khác → "Không thể thực hiện" | Shop ký gửi |
| `127` | `RADA_CARD` | ✔ | – | action `0` `RadarService.sendRadar`; `1` bật/tắt thẻ `short idC` (chỉ được dùng **1 thẻ** cùng lúc; thẻ `Level == 0` không dùng được) | Radar card |
| `-105` | `TRANSPORT` | ✔ | – | Theo `player.type`/`maxTime`: `0/30` → tàu tới map 102 (tương lai); `1/5` → BDKB map 135; `2/5` → map 80 (nếu đang ở hành tinh thực vật) hoặc 160; `3/5` → quay lại zone Khí gas đã lưu; `4/5` → map 149 (KGHĐ); `5/5` → map 156 | Dịch chuyển sau màn hình đếm |
| `42` | `USER_INFO` | – | – | Đoạn "đăng ký nhanh" bị comment và **không có `break`** ⇒ rơi xuống `-127` | (chết) |
| `-127` | `LUCKY_ROUND` | ✔ | – | `LuckyRound.readOpenBall` | Vòng quay / mở bóng may mắn |
| `-125` | `CLIENT_INPUT` | ✔ | – | `Input.doInput` (form nhập liệu, loại form theo `idMark.typeInput`) | Nhập liệu form |
| `112` | `SPEACIAL_SKILL` | ✔ | – | `IntrinsicService.showMenu` | Menu nội tại |
| `-34` | `MAGIC_TREE` | ✔ | – | `1` `openMenuTree`, `2` `loadMagicTree` | Cây đậu thần |
| `-99` | `ENEMY_LIST` | ✔ | – | `FriendAndEnemyService.controllerEnemy` | Danh sách kẻ thù |
| `18` | `GOTO_PLAYER` | ✔ | – | `changeMapVIP = true`; `FriendAndEnemyService.goToPlayerWithYardrat` | Dịch chuyển tới người chơi (Yardrat/admin) |
| `-72` | `CHAT_PLAYER` | ✔ | – | `FriendAndEnemyService.chatPrivate` | Chat riêng |
| `-80` | `FRIEND` | ✔ | – | `FriendAndEnemyService.controllerFriend` | Bạn bè |
| `-59` | `PLAYER_VS_PLAYER` | ✔ | BV | `PVPService.controllerThachDau` | Thách đấu |
| `-86` | `GIAO_DICH` | ✔ | (BV trong service) | `TransactionService.controller`: `0` mời, `1` chấp nhận, `2` thêm item, `3` huỷ, `5` khoá, `7` đồng ý | Giao dịch |
| `-107` | `PET_INFO` | ✔ | – | `Service.showInfoPet` | Thông tin đệ tử |
| `-108` | `PET_STATUS` | ✔ + có pet | – | `pet.changeStatus(byte)` | Trạng thái đệ tử |
| `6` | `ITEM_BUY` | ✔ | BT, TX, BV | `ShopService.takeItem(typeBuy, tempId)` | Mua item |
| `7` | `ITEM_SALE` | ✔ | BT, TX, BV | action `0` → `showConfirmSellItem(type, index)`; khác → `sellItem` | Bán item |
| `29` | `OPEN_UI_ZONE` | ✔ | – | `ChangeMapService.openZoneUI` | Mở danh sách khu |
| `21` | `ZONE_CHANGE` | ✔ | – | `ChangeMapService.changeZone(byte zoneId)` | Đổi khu (giới hạn 5 s, xem mục 14) |
| `-71` | `CHAT_THEGIOI_CLIENT` | ✔ | TX | `ChatGlobalService.chat(UTF)` | Chat thế giới |
| `-79` | `PLAYER_MENU` | ✔ | – | `Service.getPlayerMenu(int playerId)` | Xem info người chơi |
| `-113` | `CHANGE_ONSKILL` | ✔ | – | Đọc 10 byte phím tắt skill → `sendSkillShortCut` | Phím tắt skill |
| `-101` | `LOGIN2` | – | – | Trả "Truy Cập: `ServerManager.DOMAIN` Đề Đăng Ký & Tải Game" | Login nhanh (không hỗ trợ) |
| `-103` | `CHANGE_FLAG` | ✔ | – | `0` `openFlagUI`, `1` `chooseFlag(byte)` | Cờ PK |
| `-7` | `PLAYER_MOVE` | ✔ | – | Chết → `charDie`; đang dính hiệu ứng skill → bỏ qua; đọc `byte b, short x, [short y]`; ở map NR Sao Đen mà di chuyển > **500 px** → bỏ qua; `b == 1` → `AchievementService.checkDoneTaskFly`; `PlayerService.playerMove` | Di chuyển |
| `-74` | `GET_IMAGE_SOURCE` | – | – | Log "Địa chỉ … đang tải dữ liệu"; type `1` `DataGame.sendSizeRes`; `2` `DataGame.sendRes` | Tải resource |
| `-81` | `COMBINNE` | ✔ | – | Đọc `byte`, `byte n`, n × `byte index` → `CombineService.showInfoCombine` | Pha lê / nâng cấp |
| `-87` | `UPDATE_DATA` | – | – | `DataGame.updateData` | Gửi 6 file `update_data` |
| `-67` | `REQUEST_ICON` | – | – | `DataGame.sendIcon(int id)` | Icon |
| `66` | `GET_IMG_BY_NAME` | – | – | `DataGame.sendImageByName(UTF)` | Ảnh theo tên |
| `-66` | `GET_EFFDATA` | ✔ + zone | – | `short effId`; nếu `effId == 25` và zone đang có rồng thần (`shenronType != -1`) ở map khác 0/7/14 → đổi sang eff 59 (type 0,1) hoặc 60 → `DataGame.sendEffectTemplate` | Hiệu ứng |
| `-62` | `CLAN_IMAGE` | ✔ | – | `FlagBagService.sendIconFlagChoose(byte)` | Icon cờ bang |
| `-63` | `GET_BAG` | ✔ | – | `FlagBagService.sendIconEffectFlag(byte & 0xFF)` | Hiệu ứng túi/cờ |
| `-32` | `BACKGROUND_TEMPLATE` | – | – | `DataGame.sendItemBGTemplate(short)` | Ảnh item nền map |
| `22` | `MENU` | ✔ | – | Bỏ 1 byte, `NpcManager.getNpc(DAU_THAN).confirmMenu(byte)` | Menu đậu thần |
| `-33` / `-23` | `MAP_OFFLINE` / `MAP_CHANGE` | ✔ | – | `ChangeMapService.changeMapWaypoint` + `hideWaitDialog` | Qua map theo waypoint |
| `-45` | `SKILL_NOT_FOCUS` | ✔ | TX | `SkillService.useSkill(player, null, null, status, msg)` | Dùng skill không mục tiêu |
| `-46` | `CLAN_CREATE_INFO` | ✔ | – | `ClanService.getClan` | Tạo/xem bang |
| `-51` | `CLAN_MESSAGE` | ✔ | – | `ClanService.clanMessage` | Chat/xin đậu bang |
| `-54` | `CLAN_DONATE` | ✔ | – | `ClanService.clanDonate` | Cho đậu |
| `-49` | `CLAN_JOIN` | ✔ | – | `ClanService.joinClan` | Xin vào bang |
| `-50` | `CLAN_MEMBER` | ✔ | – | `ClanService.sendListMemberClan(int)` | Danh sách thành viên |
| `-56` | `CLAN_REMOTE` | ✔ | – | `ClanService.clanRemote` | Quản lý thành viên |
| `-47` | `CLAN_SEARCH` | ✔ | – | `ClanService.sendListClan(UTF)` | Tìm bang |
| `-55` | `CLAN_LEAVE` | ✔ | – | `ClanService.showMenuLeaveClan` | Rời bang |
| `-57` | `CLAN_INVITE` | ✔ | – | `ClanService.clanInvite` | Mời vào bang |
| `-40` | `GET_ITEM` | ✔ | TX | `UseItem.getItem`: đọc `type, index` – rương↔túi/người, túi↔người, túi↔đệ tử | Di chuyển item |
| `-41` | `UPDATE_CAPTION` | – | – | `Service.sendCaption(session, byte gender)` | Danh hiệu cấp sức mạnh |
| `-43` | `USE_ITEM` | ✔ | TX, BV | `UseItem.doItem` (`type, where, index`) | Dùng / vứt item |
| `-91` | `MAP_TRASPORT` | ✔ | – | Theo `idMark.typeChangeMap`: `CHANGE_CAPSULE` → `UseItem.choseMapCapsule(byte)`; `CHANGE_BLACK_BALL` → `BlackBallWarService.changeMap(byte)` | Chọn map capsule / NRSĐ |
| `-39` | `FINISH_LOADMAP` | ✔ | – | `ChangeMapService.finishLoadMap` | Client tải map xong |
| `11` | `REQUEST_NPCTEMPLATE` (thực tế: mob) | – | – | `DataGame.requestMobTemplate(byte id)` | Dữ liệu quái |
| `44` | `CHAT_MAP` | ✔ | TX | `Command.gI().chat(player, UTF)` → lệnh admin / lệnh đệ tử / `Service.chat` | Chat map |
| `32` | `OPEN_UI_CONFIRM` | ✔ | – | `MenuController.doSelectMenu(short npcId, byte select)` | Chọn menu NPC |
| `33` | `OPEN_UI_MENU` | ✔ | – | `MenuController.openMenuNPC(short npcId)` | Mở menu NPC |
| `34` | `SKILL_SELECT` | ✔ | – | `SkillService.selectSkill(short)` | Chọn skill |
| `54` | `PLAYER_ATTACK_NPC` | ✔ | – | `byte mobId`; nếu `-1` đọc thêm `int masterId` (đánh quái của người) → `Service.attackMob` | Đánh quái |
| `-60` | `PLAYER_ATTACK_PLAYER` | ✔ | – | `Service.attackPlayer(int playerId)` | Đánh người |
| `-27` | `GET_SESSION_ID` | – | – | `sendKey` + `sendVersionRes` (không bao giờ tới – Collector chặn) | Key |
| `-111` | `GET_IMAGE_SOURCE2` | – | – | `DataGame.sendDataImageVersion` (rỗng) | – |
| `-20` | `ITEMMAP_MYPICK` | ✔ + còn sống | – | `ItemMapService.pickItem(short itemMapId, false)` | Nhặt đồ |
| `-28` | `NOT_MAP` | – | – | `messageNotMap` (mục 3.3) | |
| `-29` | `NOT_LOGIN` | – | – | `messageNotLogin` (mục 3.2) | |
| `-30` | `SUB_COMMAND` | – | – | `messageSubCommand` (mục 3.4) | |
| `-15` | `ME_BACK` | ✔ | – | Tàu về nhà: map `gender + 21` (21/22/23), nếu đang ở map Mabu → map 114 | Về nhà |
| `-16` | `ME_LIVE` | ✔ | không trong PK ĐHVT | `PlayerService.hoiSinh` (tốn 1 ngọc; NRSĐ 50.000 vàng; không hồi sinh ở map 51; cooldown 1,5 s) | Hồi sinh |
| `-104` | `LOCK_INVENTORY` | ✔ | – | `Service.mabaove(int)` | Mã bảo vệ tài khoản |
| `-118` | `THACHDAU` | ✔ | – | `menuType` 0/1/2 → `SuperRankService.competing(int id)`; menuType khác (3 = danh sách boss admin) → **chỉ admin**: dịch chuyển tới boss index `id` trong `BossManager` | Siêu hạng / admin tới boss |
| `-38` | `FINISH_UPDATE` | ✔ | – | `session.finishUpdate = true` | Cập nhật dữ liệu xong |
| `126` | `ANDROID_PACK` | – | – | bỏ qua | |
| `-78` | `CHECK_MOVE` | – | – | đọc 1 `int`, bỏ qua | |
| `-114` | `REQUEST_PEAN` | – | – | bỏ qua | |
| `27` | `OPEN_MENU_ID` | – | – | bỏ qua | |
| `-76` | `ARCHIVEMENT` | (không check null) | – | `AchievementService.confirmAchievement(byte)` | Nhận thưởng thành tựu |
| khác | | | | bỏ qua (log bị comment) | |

### 3.2. Sub-command của `-29` NOT_LOGIN

`Controller.messageNotLogin` (dòng 719-738). IOException khi đọc → `session.disconnect()`.

| Sub | Hằng | Dữ liệu | Xử lý |
|---|---|---|---|
| `0` | `LOGIN` | `UTF username`, `UTF password` | `MySession.login` (mục 4) |
| `2` | `CLIENT_INFO` | `byte typeClient`, `byte zoomLevel`, `bool isGprs`, `int width`, `int height`, `bool isQwerty`, `bool isTouch`, `UTF platform` | `Service.setClientType`: lưu `typeClient`, `zoomLevel`; `version = int(platform.split("|")[1] bỏ dấu ".")` (VD `"…|2.2.0"` → 220); cuối cùng `DataGame.sendLinkIP` |

### 3.3. Sub-command của `-28` NOT_MAP

`Controller.messageNotMap` (dòng 740-819).

| Sub | Hằng | Xử lý |
|---|---|---|
| `2` | `CREATE_PLAYER` | `createChar` (mục 5) |
| `6` | `UPDATE_MAP` | `DataGame.updateMap` – tên map, NPC template, mob template |
| `7` | `UPDATE_SKILL` | `DataGame.updateSkill` |
| `8` | `UPDATE_ITEM` | `ItemData.updateItem` |
| `10` | `REQUEST_MAPTEMPLATE` | `DataGame.sendMapTemp(unsignedByte mapId)` |
| `13` | `CLIENT_OK` | Nếu `player.isPl()`: gửi info nhân vật, cải trang, cờ/túi (`-64`), phím tắt skill (`-113`), item time, big message server (`-70`), tutorial nhiệm vụ đầu (nếu task `TASK_0_0`), chibi (nếu `itemsBody[10]` có đồ), `zone.mapInfo`, cấp skill đặc biệt (type 4, khi `version ≥ 220`), thời gian skill, TNSM luyện tập, đệ tử mới; nếu `tongnap > 0` → thành tựu `LAN_DAU_NAP_NGOC` |

### 3.4. Sub-command của `-30` SUB_COMMAND

`Controller.messageSubCommand` (dòng 821-847).

| Sub | Hằng | Dữ liệu | Xử lý |
|---|---|---|---|
| `16` | `POTENTIAL_UP` | `byte type`, `short point` | `nPoint.increasePoint(type, point)` – cộng tiềm năng |
| `64` | `PLAYER_MENU_ACTION` | `int playerId`, `short menuId` | `SubMenuService.controller` – `500` BAN, `501` BUFF_PET, `502` OTT (oẳn tù tì cược 5.000.000 vàng), `503` CUU_SAT, `505` BUY_BACK (comment) |

### 3.5. Toàn bộ hằng số `Cmd_message`

File `SRC/src/nro/models/consts/Cmd_message.java` định nghĩa hằng theo **nhiều không gian tên** (lệnh chính, sub của `-28`, `-29`, `-30`…), nên cùng một giá trị có nhiều tên. Tổng hợp:

| Giá trị | Tên hằng |
|---|---|
| -128 | `CHAR_EFFECT` |
| -127 | `LUCKY_ROUND` |
| -126 | `QUAYSO` |
| -125 | `CLIENT_INPUT` |
| -124 | `HOLD` |
| -123 | `UPDATECHAR_MP` |
| -122 | `DUAHAU` |
| -121 | `CHECK_MAP` |
| -120 | `CHECK_CONTROLLER` |
| -119 | `THELUC` |
| -118 | `THACHDAU` |
| -117 | `MABU` |
| -116 | `AUTOPLAY` |
| -115 | `POWER_INFO` |
| -114 | `REQUEST_PEAN` |
| -113 | `CHANGE_ONSKILL` |
| -112 | `CHAGE_MOD_BODY` |
| -111 | `GET_IMAGE_SOURCE2` |
| -110 | `SERVER_DATA` |
| -108 | `PET_STATUS` |
| -107 | `PET_INFO` |
| -106 | `ITEM_TIME` |
| -105 | `TRANSPORT` |
| -104 | `LOCK_INVENTORY` |
| -103 | `CHANGE_FLAG` |
| -102 | `LOGINFAIL` |
| -101 | `LOGIN2` |
| -100 | `KIGUI` |
| -99 | `ENEMY_LIST` |
| -98 | `ANDROID_IAP` |
| -97 | `UPDATE_ACTIVEPOINT` |
| -96 | `TOP` |
| -95 | `MOB_ME_UPDATE` |
| -94 | `UPDATE_COOLDOWN` |
| -93 | `BGITEM_VERSION` |
| -92 | `SET_CLIENTTYPE` |
| -91 | `MAP_TRASPORT` |
| -90 | `UPDATE_BODY` |
| -89 | `OPEN3HOUR` |
| -88 | `SERVERSCREEN` |
| -87 | `UPDATE_DATA` |
| -86 | `GIAO_DICH` |
| -85 | `MOB_CAPCHA` |
| -84 | `MOB_MAX_HP` |
| -83 | `CALL_DRAGON` |
| -82 | `TILE_SET` |
| -81 | `COMBINNE` |
| -80 | `FRIEND` |
| -79 | `PLAYER_MENU` |
| -78 | `CHECK_MOVE` |
| -77 | `SMALLIMAGE_VERSION` |
| -76 | `ARCHIVEMENT` |
| -75 | `NPC_BOSS` |
| -74 | `GET_IMAGE_SOURCE` |
| -73 | `NPC_ADD_REMOVE` |
| -72 | `CHAT_PLAYER` |
| -71 | `CHAT_THEGIOI_CLIENT` |
| -70 | `BIG_MESSAGE` |
| -69 | `MAXSTAMINA` |
| -68 | `STAMINA` |
| -67 | `REQUEST_ICON` |
| -66 | `GET_EFFDATA` |
| -65 | `TELEPORT` |
| -64 | `UPDATE_BAG` |
| -63 | `GET_BAG` |
| -62 | `CLAN_IMAGE` |
| -61 | `UPDATE_CLANID` |
| -60 | `PLAYER_ATTACK_PLAYER` |
| -59 | `PLAYER_VS_PLAYER` |
| -58 | `CLAN_PARTY` |
| -57 | `CLAN_INVITE` |
| -56 | `CLAN_REMOTE` |
| -55 | `CLAN_LEAVE` |
| -54 | `CLAN_DONATE` |
| -53 | `CLAN_INFO` |
| -52 | `CLAN_UPDATE` |
| -51 | `CLAN_MESSAGE` |
| -50 | `CLAN_MEMBER` |
| -49 | `CLAN_JOIN` |
| -47 | `CLAN_SEARCH` |
| -46 | `CLAN_CREATE_INFO` |
| -45 | `SKILL_NOT_FOCUS` |
| -44 | `SHOP` |
| -43 | `USE_ITEM` |
| -42 | `ME_LOAD_POINT` |
| -41 | `UPDATE_CAPTION` |
| -40 | `GET_ITEM` |
| -39 | `FINISH_LOADMAP` |
| -38 | `FINISH_UPDATE` |
| -37 | `BODY` |
| -36 | `BAG` |
| -35 | `BOX` |
| -34 | `MAGIC_TREE` |
| -33 | `MAP_OFFLINE` |
| -32 | `BACKGROUND_TEMPLATE` |
| -31 | `ITEM_BACKGROUND` |
| -30 | `SUB_COMMAND` |
| -29 | `NOT_LOGIN` |
| -28 | `NOT_MAP` |
| -27 | `GET_SESSION_ID` |
| -26 | `DIALOG_MESSAGE` |
| -25 | `SERVER_MESSAGE` |
| -24 | `MAP_INFO` |
| -23 | `MAP_CHANGE` |
| -22 | `MAP_CLEAR` |
| -21 | `ITEMMAP_REMOVE` |
| -20 | `ITEMMAP_MYPICK` |
| -19 | `ITEMMAP_PLAYERPICK` |
| -18 | `ME_THROW` |
| -17 | `ME_DIE` |
| -16 | `ME_LIVE` |
| -15 | `ME_BACK` |
| -14 | `PLAYER_THROW` |
| -13 | `NPC_LIVE` |
| -12 | `NPC_DIE` |
| -11 | `NPC_ATTACK_ME` |
| -10 | `NPC_ATTACK_PLAYER` |
| -9 | `MOB_HP` |
| -8 | `PLAYER_DIE` |
| -7 | `PLAYER_MOVE` |
| -6 | `PLAYER_REMOVE` |
| -5 | `PLAYER_ADD` |
| -4 | `PLAYER_ATTACK_N_P` |
| -3 | `PLAYER_UP_EXP` |
| -2 | `ME_UP_COIN_LOCK` |
| -1 | `ME_CHANGE_COIN` |
| 0 | `LOGIN`, `LOGOUT`, `ME_LOAD_ALL` |
| 1 | `REGISTER`, `SELECT_PLAYER`, `ME_LOAD_CLASS` |
| 2 | `CLIENT_INFO`, `CREATE_PLAYER`, `ME_LOAD_SKILL` |
| 3 | `SEND_SMS`, `DELETE_PLAYER` |
| 4 | `REGISTER_IMAGE`, `UPDATE_VERSION`, `ME_LOAD_INFO` |
| 5 | `ME_LOAD_HP` |
| 6 | `UPDATE_MAP`, `ME_LOAD_MP`, `ITEM_BUY` |
| 7 | `UPDATE_SKILL`, `PLAYER_LOAD_ALL`, `ITEM_SALE` |
| 8 | `UPDATE_ITEM`, `PLAYER_SPEED` |
| 9 | `REQUEST_SKILL`, `PLAYER_LOAD_LEVEL` |
| 10 | `REQUEST_MAPTEMPLATE`, `PLAYER_LOAD_VUKHI` |
| 11 | `REQUEST_NPCTEMPLATE`, `PLAYER_LOAD_AO` |
| 12 | `REQUEST_NPCPLAYER`, `PLAYER_LOAD_QUAN` |
| 13 | `CLIENT_OK`, `PLAYER_LOAD_BODY`, `UPPEARL_LOCK` |
| 14 | `CLIENT_OK_INMAP`, `PLAYER_LOAD_HP`, `UPGRADE` |
| 15 | `UPDATE_VERSION_OK`, `PLAYER_LOAD_LIVE` |
| 16 | `INPUT_CARD`, `POTENTIAL_UP`, `PLEASE_INPUT_PARTY` |
| 17 | `CLEAR_TASK`, `SKILL_UP`, `ACCEPT_PLEASE_PARTY` |
| 18 | `CHANGE_NAME`, `GOTO_PLAYER`, `BAG_SORT`, `REQUEST_PLAYERS` |
| 19 | `BOX_SORT`, `UPDATE_ACHIEVEMENT` |
| 20 | `UPDATE_PK`, `PHUBANG_INFO` |
| 21 | `CREATE_CLAN`, `BOX_COIN_OUT`, `ZONE_CHANGE` |
| 22 | `REQUEST_ITEM`, `MENU` |
| 23 | `ME_ADD_SKILL`, `OPEN_UI` |
| 24 | `OPTION_HAT` |
| 25 | `OPEN_UI_PT` |
| 26 | `OPEN_UI_SHOP` |
| 27 | `OPEN_MENU_ID` |
| 28 | `OPEN_UI_COLLECT` |
| 29 | `OPEN_UI_ZONE` |
| 30 | `OPEN_UI_TRADE` |
| 31 | `STATUS_PET` |
| 32 | `OPEN_UI_CONFIRM` |
| 33 | `CONVERT_UPGRADE`, `OPEN_UI_MENU` |
| 34 | `INVITE_CLANDUN`, `SKILL_SELECT` |
| 35 | `UPDATE_TYPE_PK`, `NOT_USEACC`, `REQUEST_ITEM_INFO` |
| 36 | `ME_LOAD_ACTIVE`, `TRADE_INVITE` |
| 37 | `ME_ACTIVE`, `TRADE_INVITE_ACCEPT` |
| 38 | `ME_UPDATE_ACTIVE`, `TRADE_LOCK_ITEM`, `OPEN_UI_SAY` |
| 39 | `ME_OPEN_LOCK`, `TRADE_ACCEPT` |
| 40 | `ITEM_SPLIT`, `TASK_GET` |
| 41 | `ME_CLEAR_LOCK`, `TASK_NEXT` |
| 42 | `USER_INFO` |
| 43 | `USE_BOOK_SKILL`, `TASK_UPDATE` |
| 44 | `CHAT_MAP` |
| 45 | `NPC_MISS` |
| 46 | `RESET_POINT` |
| 47 | `ALERT_MESSAGE` |
| 48 | `AUTO_SERVER` |
| 49 | `ALERT_SEND_SMS` |
| 50 | `GAME_INFO`, `TRADE_INVITE_CANCEL` |
| 51 | `BOSS_SKILL` |
| 52 | `MABU_HOLD` |
| 53 | `FRIEND_INVITE` |
| 54 | `PLAYER_ATTACK_NPC` |
| 56 | `HAVE_ATTACK_PLAYER` |
| 57 | `OPEN_UI_NEWMENU` |
| 58 | `MOVE_FAST` |
| 59 | `TEST_INVITE` |
| 60 | `SAVE_RMS` |
| 61 | `LOAD_RMS` |
| 62 | `ME_UPDATE_SKILL`, `ADD_CUU_SAT` |
| 63 | `GET_PLAYER_MENU`, `ME_CUU_SAT` |
| 64 | `PLAYER_MENU_ACTION`, `CLEAR_CUU_SAT` |
| 65 | `MESSAGE_TIME`, `PLAYER_UP_EXPDOWN` |
| 66 | `GET_IMG_BY_NAME`, `ME_DIE_EXP_DOWN` |
| 67 | `PLAYER_ATTACK_P_N` |
| 68 | `ITEMMAP_ADD` |
| 69 | `DEL_ACC` |
| 70 | `USE_SKILL_MY_BUFF` |
| 74 | `NPC_CHANGE` |
| 75 | `PARTY_INVITE` |
| 76 | `PARTY_ACCEPT` |
| 77 | `PARTY_CANCEL` |
| 78 | `PLAYER_IN_PARTY` |
| 79 | `PARTY_OUT` |
| 80 | `FRIEND_ADD` |
| 81 | `NPC_IS_DISABLE` |
| 82 | `NPC_IS_MOVE` |
| 83 | `SUMON_ATTACK` |
| 84 | `RETURN_POINT_MAP` |
| 85 | `NPC_IS_FIRE` |
| 86 | `NPC_IS_ICE` |
| 87 | `NPC_IS_WIND` |
| 88 | `OPEN_TEXT_BOX_ID` |
| 90 | `REQUEST_ITEM_PLAYER` |
| 91 | `CHAT_PRIVATE` |
| 92 | `CHAT_THEGIOI_SERVER` |
| 93 | `CHAT_VIP` |
| 94 | `SERVER_ALERT` |
| 95 | `ME_UP_COIN_BAG` |
| 96 | `GET_TASK_ORDER` |
| 97 | `GET_TASK_UPDATE` |
| 98 | `CLEAR_TASK_ORDER` |
| 99 | `ADD_ITEM_MAP` |
| 100 | `REFRESH_ITEM` |
| 101 | `BIG_BOSS` |
| 102 | `BIG_BOSS_2` |
| 103 | `MOVE_ITEM_TO_SUU_TAM_BOX` |
| 112 | `SPEACIAL_SKILL` |
| 113 | `SERVER_EFFECT` |
| 114 | `INAPP` |
| 121 | `SHOW_ADS` |
| 122 | `LOGIN_DE` |
| 123 | `SET_POS` |
| 124 | `NPC_CHAT` |
| 125 | `FUSION` |
| 126 | `ANDROID_PACK` |
| 127 | `RADA_CARD` (`Byte.MAX_VALUE`) |

Một số cmd server → client quan trọng thấy trong code: `-25` thông báo nhỏ (`sendThongBao`), `-26` thông báo OK, `-70` big message (`sendThongBaoFromAdmin` dùng icon 1139), `92` chat thế giới, `93` chữ chạy (`ServerNotify`), `44` chat map, `122` chờ đăng nhập (`sendWaitToLogin`), `-102` login fail, `2` chuyển sang màn tạo nhân vật, `-96` bảng TOP / danh sách boss, `-94` cooldown skill, `123` set vị trí.

---

## 4. Luồng đăng nhập

Nguồn: `network/MySession.java` (`login`), `database/MrBlue.java` (`login`, `loadPlayer`).

```
Client                                      Server
  │ -27 (key)                                 │ → gửi key, -74 vsRes
  │ -29/2 (client info)                       │ → lưu zoom/version, -29/2 danh sách server (LINK_IP_PORT)
  │ -29/0 (username, password)                │ → MySession.login()
```

### 4.1. `MySession.login(username, password)` – các bước kiểm tra

| # | Điều kiện | Kết quả |
|---|---|---|
| 1 | `AntiLogin` theo **IP** (map tĩnh `ANTILOGIN`): sai ≥ **5 lần** → khoá **60.000 ms** | Thông báo "Bạn đã đăng nhập tài khoản sai quá nhiều lần. Vui lòng thử lại sau N giây." |
| 2 | `Manager.LOCAL == true` | "Server này chỉ để lưu dữ liệu\nVui lòng qua server khác" |
| 3 | `Maintenance.isRunning` | "Server đang trong thời gian bảo trì, vui lòng quay lại sau" |
| 4 | `!isAdmin && players.size() >= MAX_PLAYER` | "Máy chủ hiện đang quá tải, cư dân vui lòng di chuyển sang máy chủ khác." |
| 5 | `this.player == null` | lưu `uu`, `pp` → `MrBlue.login(session, al)` |

### 4.2. `MrBlue.login`

1. `select * from account where username = ? and password = ?` (**mật khẩu so sánh plaintext**).
2. Không có dòng → "Thông tin tài khoản hoặc mật khẩu không chính xác", gửi `-102`, `al.wrong()`.
3. Có dòng → nạp vào session: `userId`, `isAdmin` (`is_admin`), `lastTimeLogout`, `actived` (`active`), `goldBar` (`thoi_vang`), `luotquay`, `gold` (`vang`), `eventPoint`, `bdPlayer`, `vnd`, `tongnap`, `vip`.
4. Rẽ nhánh:

| Điều kiện | Hành vi |
|---|---|
| `ban = 1` | "Tài khoản này đang bị khóa. Liên hệ Admin để biết thêm thông tin" |
| Chưa đủ `SECOND_WAIT_LOGIN` giây kể từ `last_time_login` | Gửi cmd `122` (số giây phải chờ) và trả `null` |
| `last_time_login > last_time_logout` **và** tài khoản đang online (`Client.getPlayerByUser`) | Kick phiên cũ; trả `null` (người chơi phải bấm đăng nhập lại) |
| Chưa đủ `SECOND_WAIT_LOGIN` giây kể từ `last_time_logout` | Gửi cmd `122` |
| Không có dòng `player` với `account_id` | `sendVersionGame` (`-28/4`), `sendDataItemBG` (`-31`), `switchToCreateChar` (cmd `2`) → client hiện màn tạo nhân vật |
| Có nhân vật | Kick phiên cũ nếu còn; `loadPlayer(rs, false)`; set `isPlayer`, `deltaTime` (giây từ lúc tạo tài khoản), điểm sự kiện, `thachdauwhis`, `point_maydam`, `total_damage_maydam`; `isNewMember = tạo tài khoản chưa quá 35 ngày`; `update account set last_time_login = now, ip_address = IP` |

5. `al.reset()` sau mọi trường hợp tìm thấy tài khoản.

### 4.3. Sau khi `MrBlue.login` trả `Player` (tiếp trong `MySession.login`)

1. `DataGame.sendSmallVersion` (`-77`, 32.767 byte 0) và `DataGame.sendBgItemVersion` (`-93`).
2. `timeWait = 0`, `joinedGame = true`.
3. `nPoint.calPoint()`, set HP/MP; `pl.zone.addPlayer(pl)`; tính chỉ số đệ tử (nếu có).
4. `pl.setSession(this)`, `Client.gI().put(pl)` (map theo id, name, userId + list).
5. `DataGame.sendVersionGame` (`-28/4`), `DataGame.sendDataItemBG` (`-31`).
6. `Controller.sendInfo(session)`: tile set (`-82`), nội tại, chỉ số (`point`), nhiệm vụ chính, `clearMap` (`-22`), bang hội, thể lực max/hiện tại, "đánh quái nhận ngọc", năng động, có đệ tử, top rank, siêu hạng (nếu `rank < 1` → lấy rank mới và `SuperRankDAO.insertData`), tab thông báo (`50` sub `10`), `setClothes.setup()`, `sendCanAutoPlay`, **`player.start()`** (thread update 1 s).
7. Log `"[HH:mm] - Player Login: <tên>: N ms"`; nếu `player.notify` khác rỗng → gửi thông báo rồi xoá.

---

## 5. Luồng tạo nhân vật

`Controller.createChar` (dòng 857-900) – cmd `-28` sub `2`: `UTF name`, `byte gender`, `byte hair`.

| Kiểm tra | Thông báo |
|---|---|
| `Maintenance.isRunning` | (bỏ qua im lặng) |
| Độ dài tên ngoài **5–10** ký tự | "Tên nhân vật chỉ đồng ý các ký tự a-z, 0-9 và chiều dài từ 5 đến 10 ký tự" |
| `select * from player where name = ?` có kết quả | "Tên nhân vật đã tồn tại" |
| `Util.haveSpecialCharacter(name)` | "Tên nhân vật không được chứa ký tự đặc biệt" |
| Tên nằm trong `ConstIgnoreName.IGNORE_NAME` (hiện **rỗng**) | "Tên nhân vật đã tồn tại" |

Thành công → `PlayerDAO.createNewPlayer(userId, name.toLowerCase(), gender, hair)` rồi `session.login(uu, pp)` lại.

### 5.1. Dữ liệu khởi tạo (`PlayerDAO.createNewPlayer`, dòng 37-350)

| Trường | Giá trị |
|---|---|
| `head` | `hair` client gửi |
| `data_inventory` | `[vàng 2000, ngọc xanh 10000 (TEST: 1.000.000.000), hồng ngọc 0, point 0, event 0]` |
| `data_location` | map `39 + gender` (39/40/41), x `100`, y `384` |
| `data_point` | giới hạn SM `0`; sức mạnh `2000`; tiềm năng `2000`; thể lực `1000`/`1000`; HP gốc `200` (Trái Đất) / `100`; KI gốc `200` (Namếc) / `100`; sức đánh gốc `15` (Xayda) / `10`; giáp, chí mạng `0`; HP/KI hiện tại = HP/KI gốc |
| `data_magic_tree` | cấp `1`, `5` hạt đậu, không đang nâng cấp |
| `items_body` (11 ô) | ô 0 áo id `0/1/2` (theo gender) option `47` (giáp) = `2` (Xayda `3`); ô 1 quần id `6/7/8` option `6` (HP) = `30` (Trái Đất) / `20`; còn lại trống |
| `items_bag` (30 ô) | ô 0: item id `63` × `10`, option `2` = `8` (comment code ghi "thỏi vàng") |
| `items_box` (30 ô) | ô 0: item id `12` (rada) option `14` = `1` |
| `items_box_lucky_round`, `items_daban` | 110 ô trống mỗi loại |
| `data_task` | nhiệm vụ id `0` (TEST: `28`), index 0, count 0 |
| `data_charm` | 10 loại bùa, thời gian = lúc tạo |
| `skills` | Trái Đất `{0,1,6,9,10,20,22,19}`, Namếc `{2,3,7,11,12,17,18,19}`, Xayda `{4,5,8,13,14,21,23,19}`; skill đầu cấp 1, còn lại cấp 0 |
| `skills_shortcut` | `[0/2/4, -1 × 9]` |
| `data_black_ball` | 7 × `[0,0,0]` |
| `data_side_task` | `[-1, 0, 0, 0, 20 (số NV còn nhận), 0]` |
| `BoughtSkill` | `[0/2/4]` |
| `nhiem_vu_kol` | JSON Gson từ `KOLProgressData` mặc định |

---

## 6. Luồng vào map sau đăng nhập

Thứ tự điển hình (client điều khiển phần lớn):

1. Server gửi `-28/4` (phiên bản data/map/skill/item + mốc sức mạnh). Client so sánh với cache:
   - khác `vsData` → client gửi `-87` → `DataGame.updateData`;
   - khác `vsMap` → `-28/6`; khác `vsSkill` → `-28/7`; khác `vsItem` → `-28/8`.
2. Resource: `-74` type `1` (số file) → type `2` (gửi toàn bộ file `data/res/x<zoom>`, kết thúc bằng `-74` type `3` + `vsRes`).
3. Client yêu cầu dần tài nguyên khi cần: `-67` icon, `66` ảnh theo tên, `-66` hiệu ứng, `-32` ảnh item nền, `11` quái, `-28/10` tile map.
4. Client gửi `-28/13` (CLIENT_OK) → server gửi toàn bộ trạng thái nhân vật (mục 3.3).
5. `-39` FINISH_LOADMAP → `ChangeMapService.finishLoadMap`; `-38` FINISH_UPDATE.

---

## 7. Luồng thoát / mất kết nối

Server không có cmd "logout" riêng; thoát = đóng socket.

```
Collector.run() kết thúc (IOException)
  └─ Network.getAcceptHandler().sessionDisconnect(session)      (ServerManager.activeServerSocket)
        ├─ Client.gI().kickSession(session)
        │     ├─ Client.remove(session)
        │     │     ├─ Client.remove(player)
        │     │     │     ├─ xoá khỏi players_id / players_name / players_userId / players
        │     │     │     ├─ (lần đầu) beforeDispose = true; mapIdBeforeLogout = map hiện tại
        │     │     │     ├─ đang giữ NR Namếc (idNRNM != -1) → rơi viên ngọc xuống map, reset chủ
        │     │     │     ├─ ChangeMapService.exitMap(player); TransactionService.cancelTrade
        │     │     │     ├─ clan.removeMemberOnline; cờ disconnect cho SummonDragon/SummonDragonNamek/shenronEvent
        │     │     │     ├─ mobMe (trứng) chết; pet.mobMe chết; pet exitMap
        │     │     │     └─ PlayerDAO.updatePlayer(player)       ← LƯU DỮ LIỆU
        │     │     ├─ player.dispose()
        │     │     ├─ nếu joinedGame: update account set last_time_logout = now
        │     │     └─ ServerManager.disconnect(session)          ← giảm đếm IP
        │     └─ session.disconnect()
        └─ ServerManager.disconnect(session)                      ← giảm đếm IP lần 2
  └─ session.disconnect()  (đóng sender/collector/socket, SessionManager.removeSession)
```

Các nguồn kick khác cùng đi qua `Client.kickSession`: login trùng tài khoản, `timeWait` hết (~100 s chưa login), ban (sau 5 s, xem `15-lenh-admin-gm.md`), admin kick, `TransactionService` phát hiện `last_time_logout > last_time_login` khi mời giao dịch, bảo trì (`Client.close()`).

---

## 8. Lưu dữ liệu

### 8.1. Lưu người chơi

**Không có cơ chế auto-save định kỳ** cho người chơi online (không tìm thấy lời gọi `PlayerDAO.updatePlayer` theo chu kỳ trong `Player.update`, `Client` hay scheduler nào). Dữ liệu người chơi được ghi DB khi:

| Thời điểm | Nguồn |
|---|---|
| Thoát / mất kết nối / bị kick | `Client.remove(Player)` → `PlayerDAO.updatePlayer` |
| Bảo trì (kick toàn bộ) | `ServerManager.close()` → `Client.close()` |
| Một số sự kiện với người chơi **offline** được nạp tạm bằng `MrBlue.loadById` (sau khi lưu thì `dispose`) | `Whis.java` (dòng 104), `BlackBallWar.java` (122), `ClanService.java` (203, 401), `SummonDragonNamek.java` (164, 218, 258) |
| Lưu từng cột lẻ | `TraningDAO` (`data_luyentap`), `SuperRankDAO` (`data_inventory` khi thưởng siêu hạng, bảng `super_rank`), `Clan` (`thanhTichBang`, `thanhTichKhiGas`, `thanhTichCDRD`), `Service` (`point_maydam`), `Input` (đổi tên), `ClanService` (`clan_id = -1`) |

`PlayerDAO.updatePlayer(player)` (dòng 352-1063):
- Chỉ chạy nếu `player.idMark.isLoadedAllDataPlayer()`.
- Vàng bị chặn trên `Inventory.LIMIT_GOLD = 200.000.000.000`.
- Vị trí lưu: `mapIdBeforeLogout`, `x`, `y`, HP/MP hiện tại. Nếu **đã chết** → map `gender + 21`, (300, 336), HP = MP = 1. Nếu đang ở **Doanh trại**, **NR Sao Đen**, hoặc map không vào được (`checkMapCanJoin == null`) → map `gender + 21`, (300, 336).
- 1 câu `update player set ... where id = ?` gồm ~62 cột: `head, have_tennis_space_ship, clan_id, data_inventory, data_location, data_point, data_magic_tree, items_body, items_bag, items_box, items_box_lucky_round, items_daban, friends, enemies, data_intrinsic, data_item_time, data_task, data_mabu_egg, pet, data_black_ball, data_side_task, data_charm, skills, skills_shortcut, notify, baovetaikhoan, data_card, lasttimepkcommeson, bandokhobau, doanhtrai, conduongrandoc, masterDoesNotAttack, nhanthoivang, ruonggo, sieuthanthuy, vodaisinhtu, rongxuong, data_item_event, data_luyentap, data_clan_task, data_vip, rank, data_achievement, giftcode, event_point, data_event, dataBadges, dataTaskBadges, BoughtSkill, LearnSkill, firstTimeLogin, dailyGift, point_sukien, thachdauwhis, point_sukien1, point_maydam, total_damage_maydam, data_duahau_egg, checkNhanQua, nhiem_vu_kol, point_sukien2`.
- Sau đó `SuperRankDAO.updateData(player)`; nếu `player.isOffline` → `dispose()`.

### 8.2. Lưu dữ liệu khác

| Dữ liệu | Khi nào | Nguồn |
|---|---|---|
| Clan | Khi bảo trì (`ClanService.close()`), và các thao tác bang | `ServerManager.close` |
| Shop ký gửi | Khi bảo trì (`ConsignShopManager.save()`) | `ServerManager.close` |
| Giftcode `count_left` | Ngay mỗi lần dùng code | `GiftCodeManager.updateGiftCode` |
| `account.last_time_login`, `ip_address` | Khi login | `MrBlue.login` |
| `account.last_time_logout` | Khi thoát | `Client.remove(MySession)` |
| `account.password` | Đổi mật khẩu (≥ 6 ký tự) | `Service.changePassword` |
| `account.vnd` | Đổi VND lấy vàng/ngọc | `PlayerDAO.subvnd`, `MuaThanhVien` |
| `history_transaction` | Sau mỗi giao dịch; xoá bản ghi > **3 ngày** khi khởi động | `HistoryTransactionDAO.insert`, `deleteHistory` |

---

## 9. Gửi dữ liệu game cho client

Nguồn: `data/DataGame.java`, `data/ItemData.java`. Đường dẫn file: xem `00-tong-quan.md` mục 11.

| Hàm | Cmd | Payload |
|---|---|---|
| `sendVersionGame` | `-28` sub `4` | `byte vsData(9), vsMap(2), vsSkill(1), vsItem(9), 0`, `byte 22`, 22 × `long` mốc sức mạnh |
| `updateData` | `-87` | `byte vsData`; 6 khối `int len + bytes`: dart, arrow, effect, image, part, skill (gửi **đồng bộ** `doSendMessage`) |
| `updateMap` | `-28` sub `6` | `byte vsMap`; `byte số map` + tên; `byte số NPC` + (tên, head, body, leg, 0); `byte số mob` + (type, tên, hp, rangeMove, speed, dartType) |
| `updateSkill` | `-28` sub `7` | `byte vsSkill, 0`; `byte số NClass`; mỗi lớp: tên, danh sách SkillTemplate (id, tên, maxPoint, manaUseType, type, iconId, damInfo, "null", các Skill). Template id `0` được thêm 2 skill trống id `105`, `106` |
| `ItemData.updateItem` | `-28` sub `8` (4 message) | (a) option template: `byte vsItem, 0, byte count` + (tên, type); (b) `100` arr head 2 frames; (c) `1` item template `0..749`; (d) `2` item template `750..size-1` |
| `sendDataItemBG` | `-31` | `short count` + (idImage, layer, dx, dy, 0) |
| `sendBgItemVersion` | `-93` | `short count` + `byte id` |
| `sendItemBGTemplate` | `-32` | `short id, int len, png` |
| `sendIcon` | `-67` | `int id, int len, png` |
| `sendImageByName` | `66` | `UTF name, byte nFrame, int len, png` (không có file → `int 0`) |
| `sendEffectTemplate` | `-66` | `short id, int len, DataEffect`, (nếu `version > 220`: 1 byte `2` khi eff 60, ngược lại `0`), `int len, png` |
| `requestMobTemplate` | `11` | `byte id` + nội dung file `data/mob/x<zoom>/<id>` |
| `sendTileSetInfo` | `-82` | nội dung `data/map/tile_set_info` |
| `sendMapTemp` | `-28` sub `10` | nội dung `data/map/tile_map_data/<id>` |
| `sendSmallVersion` | `-77` | `short 32767` + 32.767 byte `0` |
| `sendVersionRes` | `-74` | `byte 0, int vsRes` |
| `sendSizeRes` | `-74` | `byte 1, short số file` |
| `sendRes` | `-74` | mỗi file: `byte 2, UTF tên, int len, bytes`; kết thúc `byte 3, int vsRes` |
| `sendLinkIP` | `-29` | `byte 2, UTF (LINK_IP_PORT + ",0,0"), byte 1` |
| `sendHeadAvatar(msg)` | (ghi vào message có sẵn) | `short count` + (headId, avatarId) |
| `sendDataImageVersion` | – | **thân hàm rỗng** |

---

## 10. Tầng truy cập DB

`data/LocalManager.java`:
- Static block đọc `Config.properties` (nhóm `database.*`) và tạo `HikariDataSource` ngay khi lớp được nạp.
- `getConnection()`; `executeQuery(sql, params...)` trả `LocalResultSet`; `executeUpdate(sql, params...)`. Nếu câu lệnh bắt đầu bằng `insert` và kết thúc bằng `()`, `executeUpdate` tự thay `()` bằng `(?,?,…)` theo số tham số (dùng trong `PlayerDAO.createNewPlayer`).
- `database.log=true` → log mọi SQL.

`data/ResultSetImpl.java`: **copy toàn bộ ResultSet vào RAM** (`rs.last()` lấy số dòng → mảng `HashMap` theo tên cột viết thường, cả dạng `table.column`) rồi đóng statement/ResultSet ⇒ connection trả về pool ngay; điều hướng bằng `next/first/gotoResult…`.

---

## 11. Package `managers`

| Lớp | Kiểu chạy | Chức năng |
|---|---|---|
| `GiftCodeManager` | Singleton, không thread | Giữ `listGiftCode`. `checkUseGiftCode(player, code)`: hết lượt (`countLeft <= 0`) → "Giftcode đã hết"; đã dùng → "Tham lam!"; thiếu ô trống (< số loại quà) → "Cần tối thiểu N ô hành trang trống"; thành công → `countLeft - 1`, thêm code vào `player.giftCode`, update DB. `checkInfomationGiftCode` liệt kê code (không thấy nơi gọi) |
| `PVPManager` | Executor 1 thread, tick 1000 ms | Danh sách `PVP` đang diễn ra, gọi `pvp.update()` |
| `ShenronEventManager` | Executor 1 thread, tick 1000 ms (dừng khi bảo trì) | Update các `models.ShenronEvent` (rồng thần sự kiện) |
| `SuperRankManager` | Thread "Update Super Rank", tick 500 ms | Hàng chờ siêu hạng: người chơi ở **map 113** và zone chưa có trận → tạo `SuperRank`; `canCompete`, `currentlyCompeting`, `awaitingCompetition` |
| `TopBanDoKhoBau`, `TopKhiGasHuyDiet`, `TopConDuongRanDoc` | Singleton `getInstance()`, `load()` | Truy vấn TOP 100 (`ORDER BY so2 DESC, so1 ASC LIMIT 100`) thành tích phó bản |
| `MyClanTopBanDoKhoBau` | `load2(idLeader)` | TOP BDKB theo bang của người chơi |

---

## 12. Package `interfaces`

| Interface | Phương thức chính | Hiện thực |
|---|---|---|
| `INetwork` | `init, start(port), setAcceptHandler, close, dispose, setDoSomeThingWhenClose, randomKey, setTypeSessioClone, getAcceptHandler, isRandomKey, stopConnect` | `Network` |
| `ISession` | `setSendCollect, setMessageHandler, setKeyHandler, startSend, startCollect, start, getIP, isConnected, getID, sendMessage, doSendMessage, disconnect, dispose, getNumMessages, sendKey, getKey, sentKey, setSentKey` | `Session`, `MySession` |
| `ISessionAcceptHandler` | `sessionInit(ISession)`, `sessionDisconnect(ISession)` | Lớp ẩn danh trong `ServerManager.activeServerSocket` |
| `IMessageHandler` | `onMessage(ISession, Message)` | `Controller` |
| `IMessageSendCollect` | `readMessage, readKey, doSendMessage, writeKey` | `MessageSendCollect` |
| `IKeySessionHandler` | `sendKey(ISession)` | `KeyHandler`, `MyKeyHandler` |
| `IMessage` | Đọc/ghi kiểu nguyên thuỷ, UTF, ảnh; `writer, reader, getData, cleanup, dispose` | `Message` |
| `IServerClose` | `serverClose()` | Lambda log "SERVER CLOSE" + `System.exit(0)` |
| `IBoss` | `update, updateInfo, initBase, changeStatus, getPlayerAttack, changeToTypePK/NonPK, moveToPlayer, moveTo, checkPlayerDie, wakeupAnotherBoss…, reward, attack, rest, respawn, joinMap, chatS/doneChatS, active, chatM, die, chatE/doneChatE, leaveMap, autoLeaveMap, afk, getHead/Body/Leg/FlagBag/Aura/EffFront` | `Boss` |
| `IEventBoss` | `init, npc, createNpc, boss, createBoss, itemMap, itemBoss` | Sự kiện (`event_list`) |
| `IPVP` | `start, finish, dispose, update, reward, sendResult, lose, isInPVP` | `matches/PVP` |

---

## 13. Package `utils`

| Lớp | Hàm/tính năng đáng chú ý |
|---|---|
| `Util` | `nextInt(from,to)`, `nextInt(max)`, `nextLong`, `nextInt(int[] percen)` (chọn theo trọng số), `isTrue(ratio, total)` (tỉ lệ %), `canDoWithTime(lastTime, ms)` (cooldown), `getDistance(...)`, `numberToMoney`, `formatNumber`, `powerToString`, `haveSpecialCharacter`, `removeAccent`, `isAfterMidnight`, `isTimeDifferenceGreaterThanNDays`, `isSameDay`, `randomBossId`, `createIdBossClone`, `threadPool`, `setTimeout`, `spl` (tạo ItemMap) |
| `TimeUtil` | Múi giờ `Asia/Ho_Chi_Minh`; `getTimeNow(format)`, `getTimeLeft`, `diffDate`, `isTimeNowInRange`, `isMabuOpen`, `isMabu14HOpen`, `is21H`, `isBlackBallWarOpen`, `isBlackBallWarCanPick`, `getSecondsUntilCanPick`… |
| `Logger` | Màu ANSI; `log/success/warning/error/primary`; `logException(clazz, ex, msg...)` in stacktrace + tên hàm; `fileLog(playerName, text)` ghi `log/<tên>_log.txt` (tạo thư mục) |
| `FileIO` | `readFile(url)` → `byte[]` (null nếu lỗi), `loadFile`, `writeFile`, `getFiles` |
| `Functions` | `isSpam(player, text)` (regex từ cấm, mục 14), `maxint`, `generateRandomCharacters`, `sleep` |
| `SkillUtil` | Tạo skill theo template/level; thời gian/tỉ lệ các skill (khỉ, choáng, khiên, trói, thôi miên, trị thương, trứng…); map item sách → skill |
| `SystemMetrics` | `ToString()` → "Memory: (used/total) GB", "RAM: %", "CPU: %" (dùng trong menu admin) |
| `StringUtil` | `randomText(length)` (a-zA-Z0-9) |
| `FileRunner` | `runBatchFile(path)` → `cmd /c start <path>` (không thấy nơi gọi) |

---

## 14. Chống spam, giới hạn IP, giới hạn tần suất

| Cơ chế | Giá trị | Nguồn |
|---|---|---|
| Giới hạn kết nối/IP | `MAX_PER_IP` (config 999). Đếm trong `ServerManager.CLIENTS` (HashMap IP → số) | `ServerManager.canConnectWithIp`, `disconnect` |
| Giới hạn người online | `MAX_PLAYER` (config 1000); không áp dụng nếu `session.isAdmin` | `MySession.login` |
| Sai mật khẩu | 5 lần / IP → khoá 60 s | `player_system/AntiLogin.java` (`MAX_WRONG = 5`, `TIME_ANTI = 60000`) |
| Chờ giữa các lần login | `SECOND_WAIT_LOGIN` (config 3 s) tính từ `last_time_login` và `last_time_logout` | `MrBlue.login` |
| Session không login | Kick sau `timeWait = 100` tick × 1 s | `MySession.timeWait`, `Client.update` |
| Chat thế giới | Cần **≥ 5 ngọc** (trừ **1**), sức mạnh **> 1.500.000**, cooldown **30 s**; admin miễn cooldown & sức mạnh; nội dung cắt còn **100** ký tự; bỏ qua nếu trùng nội dung đang hiển thị; hàng đợi tối đa **100** (`COUNT_WAIT`) → "Kênh thế giới hiện đang quá tải…"; tối đa 100 dòng đang hiển thị, mỗi dòng tồn tại ≥ 1 s | `services/ChatGlobalService.java` |
| Lọc từ cấm | Regex (không phân biệt hoa thường) gồm các từ tục tiếng Việt/Anh và đuôi tên miền (`.com`, `.net`, `.vn`, `.io`…); nếu khớp → **không gửi** tin (trừ người chơi tên `spamcc`) | `utils/Functions.isSpam`, dùng ở `Service.chatPrivate` |
| Chat map chứa "thang" | Không phát cho map, chỉ lưu `lastChatMessage` (dùng cho boss sự kiện `LanCon`) | `Service.chat` dòng 682-694 |
| Mời giao dịch | Cooldown **10 s** cho cả 2 bên; kiểm tra DB `last_time_logout > last_time_login` → kick (chống clone/dupe) | `TransactionService` (`TIME_DELAY_TRADE = 10000`) |
| Đổi khu | Cooldown **5 s** (admin/boss miễn); khu đầy → "Khu vực này đã đầy" | `ChangeMapService.changeZone` |
| Di chuyển ở NR Sao Đen | Bước > 500 px bị bỏ qua | `Controller` cmd `-7` |
| Bảo vệ tài khoản | Kích hoạt tốn **500.000 vàng**; khi bật chặn ký gửi, mua/bán shop, dùng item, thách đấu, giao dịch | `NpcFactory` case `MA_BAO_VE`, `Controller` |
| Đang giao dịch | Chặn ký gửi, mua/bán, chat thế giới, chat map, dùng skill, di chuyển item, dùng item | `Controller` (TX) |

---

## 15. Bot (package `Bot`)

| Lớp | Vai trò |
|---|---|
| `BotManager` | Singleton `Runnable` (thread "Thread Bot Game"), danh sách `bot`; tick **150 ms** gọi `bot.update()` trên bản copy danh sách |
| `NewBot` | Factory: `runBot(type, BotGiaoDich shop, slot)` tạo `slot` bot. Ngoại hình lấy ngẫu nhiên từ item type 5 (cải trang) có head/body/leg hợp lệ (`leg != 194`); tên ngẫu nhiên `mrblue1`…`mrblue50000`; cờ ngẫu nhiên từ `FLAGS_BAGS`. Chỉ số: `limitPower 8`, `power = 1000 + rand(50.000.000)`, `tiemNang = 20.000.000 + rand`, `dameg 600.000`, `hpg 675.000`, `hp 600.000`, `hpMax 10.000`, `mpg/mp 600.000`, `mpMax 2.000.000.000`, thể lực 20.000, `critg 10`, `defg 10`; bùa thu hút 30 ngày; 500 ô túi trống; học skill cấp 7 (`leakSkill`) |
| `Bot extends Player` | `isBot = true`, id ngẫu nhiên `< 2.000.000.000`. `joinMap()` chọn map theo sức mạnh (< 100.000.000 → danh sách map theo hành tinh; các nhánh khác không bao giờ chạy vì cùng điều kiện) và zone trống/không đầy; thử 20 lần thất bại → tự xoá. `update()`: `Player.update`, tự cộng điểm (50% cộng 100 HP gốc, 50% cộng 10 sức đánh nếu đủ tiềm năng), theo `type`: `0` → `BotPemQuai`, `1` → `BotGiaoDich`, `2` → `BotSanBoss`; chết thì hồi sinh full |
| `BotPemQuai` (type 0) | Đánh quái ngẫu nhiên trong zone, nhặt đồ, đổi map mỗi ~150–300 s, **tự chat mỗi 3 s** |
| `BotGiaoDich` (type 1) | Đứng ở **map 84**, rao bán item `idItem` đổi lấy `slot` × `idItTd` (chat thế giới mỗi 100–200 s, chat map mỗi 5–10 s). Khi người chơi mời giao dịch (`TransactionService.sendInviteTrade` phát hiện `isBot`) → mở giao dịch; nếu người chơi đặt đủ item trao đổi → bot đưa `round(sl / slot)` item và tự khoá/chấp nhận |
| `BotSanBoss` (type 2) | Chọn boss ngẫu nhiên trong `BossManager` (không ở Doanh trại, NRSĐ, BDKB, Mabu, CĐRĐ; zone chưa đầy, có quái) → bay tới đánh; thử 3 lần thất bại → tự xoá; skill đặc biệt mỗi ~50 s (Xayda: biến khỉ, Trái Đất: quả cầu kênh khí, Namếc: makankosappo) |
| `BotAttackplayer` (type 3) | HP 500.000, MP 200.000, dame 100.000, có 24 skill cấp 7; tìm người chơi gần nhất cùng map, bám theo và tấn công mỗi **500 ms** |

Cách tạo bot: xem lệnh admin `1` và `2` trong `15-lenh-admin-gm.md`.

---

## 16. Multi-server

- **Không có** giao tiếp giữa các server (không RPC/socket server-server; `consts/SocketType` có `CLIENT/SERVER` nhưng không dùng). Mỗi server là một tiến trình độc lập; muốn nhiều server thì chạy nhiều tiến trình (cổng khác nhau).
- Danh sách server cho client: `Manager.loadProperties` tạo `DataGame.LINK_IP_PORT`:
  - Server hiện tại: `server.name:server.ip:server.port:0,`
  - Thêm lần lượt `server.sv1` … `server.sv10` (mỗi giá trị nối thêm `":0,"`), cắt dấu `,` cuối.
  - Gửi cho client qua `-29` sub `2` (`DataGame.sendLinkIP`) ngay sau khi client gửi thông tin thiết bị, nối thêm `",0,0"`.
- Với config hiện tại: `NRO:36.50.135.16:14445:0,NroLight:fw.patus.tech:14449:0,0,0:0` (+ `",0,0"` khi gửi).
- Nếu nhiều server dùng **chung một DB**: `MrBlue.login` dựa vào `last_time_login`/`last_time_logout` và chỉ kick được phiên trên **chính server đó** (`Client.getPlayerByUser`), nên không ngăn được đăng nhập song song ở server khác; `TransactionService` có kiểm tra `last_time_logout > last_time_login` để kick khi giao dịch. Cột `account.server_login` có trong DB nhưng **không được dùng**.
- `server.local=true` biến server thành "server chỉ lưu dữ liệu" (chặn login).

---

## 17. Ghi chú / điểm cần lưu ý

1. **Không auto-save** người chơi: nếu tiến trình bị kill (crash, tắt cửa sổ console) thì toàn bộ tiến trình chơi kể từ lần đăng nhập bị mất. Chỉ `Client.close()` khi bảo trì mới lưu an toàn.
2. **Đếm kết nối theo IP bị trừ 2 lần** khi ngắt kết nối: `sessionDisconnect` gọi `Client.kickSession` (bên trong `Client.remove(session)` đã gọi `ServerManager.disconnect`) rồi lại gọi `disconnect(session)`. Giá trị bị chặn ≥ 0 nên giới hạn IP thực tế lỏng hơn cấu hình.
3. `ServerManager.CLIENTS` (HashMap), `Client.players` (ArrayList) và các map trong `Client` **không thread-safe** nhưng được truy cập từ nhiều thread (Network, Collector của từng session, Client update).
4. Kiểm tra `MAX_PLAYER` dùng `this.isAdmin` **trước** khi tài khoản được nạp (luôn `false` ở bước này) → admin cũng bị chặn khi server đầy.
5. Mật khẩu lưu và so sánh **plaintext** (`account.password`); câu `update account set last_time_login = '...', ip_address = '...' where id = ...` nối chuỗi trực tiếp (IP lấy từ socket nên rủi ro thấp, nhưng không nhất quán).
6. Kiểm tra trùng tên khi tạo nhân vật dùng tên gốc, nhưng lưu `name.toLowerCase()` → "AbcDe" và "abcde" có thể không bị phát hiện trùng nếu collation DB phân biệt hoa thường (với `utf8mb4_general_ci` thì không phân biệt nên an toàn).
7. `case 42` trong `Controller` thiếu `break` → rơi vào xử lý `-127` (LuckyRound).
8. `Controller.errors` chỉ log 5 lỗi đầu tiên cho **toàn bộ vòng đời server** → sau đó mọi lỗi xử lý message bị nuốt im lặng.
9. `-76` (`ARCHIVEMENT`) không kiểm tra `player != null` → có thể NPE (bị catch) nếu gửi trước khi vào game.
10. `-30` sub `64` với `menuId` 500 (BAN) / 501 (BUFF_PET) **không kiểm tra quyền admin** trong `SubMenuService.controller` – client chỉnh sửa có thể mở menu ban/phát đệ tử (chi tiết trong `15-lenh-admin-gm.md`).
11. `ItemData.updateItem` giả định có **> 750** item template (gửi cứng `0..749`), ít hơn sẽ `IndexOutOfBounds`.
12. `sendSmallVersion` gửi 32.767 byte 0 mỗi lần login; `sendRes` gửi toàn bộ thư mục resource (x2: 1.413 file ~13 MB) qua 1 hàng đợi → tốn băng thông/RAM khi nhiều người tải cùng lúc.
13. `SessionManager.startCleanupThread()` và `QueueHandler` không được dùng; `MySession.sendSessionKey`, `KEYS = {0}` là code chết.
14. `Session.ID_INIT++` không đồng bộ (chỉ gọi từ thread Network nên hiện tại ổn).
15. `Bot.MapToPow()`: 3 nhánh `else if (power < 100000000)` trùng điều kiện với nhánh đầu → chỉ nhánh đầu có tác dụng.
16. Nội dung chat tự động của `BotPemQuai` / `Bot.getChat()` chứa **lời lẽ tục tĩu, xúc phạm** – nên thay trước khi bật bot trên server thật.
17. Bot tạo từ `NewBot` có `hpMax = 10.000` nhưng `hp = 600.000` (không nhất quán).
18. `Maintenance` là `Thread` singleton: sau khi đã `start()` một lần không thể start lại (nhưng server sẽ `exit` nên không ảnh hưởng thực tế).
19. Chat thế giới kiểm tra `gem >= 5` nhưng chỉ trừ 1 ngọc.
20. Chuỗi `LINK_IP_PORT` với `server.sv1=NroLight:fw.patus.tech:14449:0,0,0` sinh ra phần thừa `,0,0:0` – cần kiểm tra client có parse đúng không; định dạng khuyến nghị cho mỗi `server.svN` là `Tên:host:port`.
