# 00 — Tổng quan game & dự án

> Tài liệu tổng quan cho server **Ngọc Rồng Online (NRO)** – bản "Teamobi2026".
> Toàn bộ thông tin lấy từ mã nguồn trong `SRC/src/nro/models/**`, file cấu hình trong `SRC/` và file dump `database team2026.sql`.
> Đường dẫn trong tài liệu tính từ thư mục gốc dự án `Teamobi2026/`.

## Mục lục

1. [Giới thiệu nhanh](#1-giới-thiệu-nhanh)
2. [Công nghệ & thư viện](#2-công-nghệ--thư-viện)
3. [Cấu trúc thư mục dự án](#3-cấu-trúc-thư-mục-dự-án)
4. [Vai trò từng package trong `nro/models`](#4-vai-trò-từng-package-trong-nromodels)
5. [Luồng khởi động server](#5-luồng-khởi-động-server)
6. [Danh sách thread nền khi server chạy](#6-danh-sách-thread-nền-khi-server-chạy)
7. [Build & chạy server](#7-build--chạy-server)
8. [Cấu hình `Config.properties` (giải thích từng key)](#8-cấu-hình-configproperties)
9. [Bảo trì: thủ công, tự động, `maintenanceConfig.txt`](#9-bảo-trì)
10. [Lệnh console của server](#10-lệnh-console-của-server)
11. [Thư mục `SRC/data` – tài nguyên gửi cho client](#11-thư-mục-srcdata--tài-nguyên-gửi-cho-client)
12. [Cơ sở dữ liệu (tóm tắt)](#12-cơ-sở-dữ-liệu-tóm-tắt)
13. [Log & file sinh ra khi chạy](#13-log--file-sinh-ra-khi-chạy)
14. [Ghi chú / điểm cần lưu ý](#14-ghi-chú--điểm-cần-lưu-ý)

---

## 1. Giới thiệu nhanh

| Mục | Giá trị |
|---|---|
| Thể loại | Server game MMORPG 2D "Ngọc Rồng Online" (dựa trên giao thức client TeaMobi), viết lại bằng Java |
| Tác giả ghi trong code | `@author By Mr Blue` (hầu hết file) |
| Main class | `nro.models.server.ServerManager` (`SRC/nbproject/project.properties` dòng 98, manifest của `SRC/20.jar`) |
| Cổng mặc định | `14445` (TCP, `server.port`) |
| CSDL | MySQL/MariaDB, tên DB `team2026` (dump tạo bằng phpMyAdmin 5.2.1, MariaDB 10.4.32) |
| Client | Unity (thư mục `LÂU CỒ MOD`, binary – không mô tả ở đây) |
| Số file Java | 548 file trong `SRC/src/nro/models` + 5 file cũ trong `SRC/src/models` |
| Hệ điều hành mục tiêu | Windows (file `.bat`, lệnh `cmd /c ...` trong code) |

Game gồm 3 hành tinh / 3 phái: **Trái Đất (gender 0)**, **Namếc (gender 1)**, **Xayda (gender 2)** (`Manager.loadDatabase`, đoạn load `skill_template`, `SRC/src/nro/models/server/Manager.java` dòng 422).

---

## 2. Công nghệ & thư viện

### 2.1. Ngôn ngữ & nền tảng

| Thành phần | Chi tiết | Nguồn |
|---|---|---|
| Java | **Java 17** (`javac.source=17`, `javac.target=17`). Code dùng `switch ->`, pattern matching `instanceof` (Java 16+), `OperatingSystemMXBean.getTotalMemorySize()/getCpuLoad()` (JDK 14+) | `SRC/nbproject/project.properties` dòng 71-72; `SRC/src/nro/models/utils/SystemMetrics.java` |
| Build tool | NetBeans Ant project (`build.xml` + `nbproject/build-impl.xml`) | `SRC/build.xml` |
| JDK build `dist` | `17.0.10+11-LTS-240 (Oracle)`, Ant 1.10.8 | manifest `SRC/dist/NgocRongOnline.jar` |
| Network | Java NIO `ServerSocketChannel` + `Selector` để accept, mỗi kết nối dùng socket blocking với 2 thread (đọc/ghi) | `SRC/src/nro/models/network/Network.java`, `Session.java` |
| Kết nối DB | HikariCP connection pool + MySQL Connector/J 5.1.23 (`com.mysql.jdbc.Driver`) | `SRC/src/nro/models/data/LocalManager.java` |
| Định dạng dữ liệu | Nhiều cột DB lưu JSON (parse bằng json-simple), một số chỗ dùng Gson | `Manager.java`, `MrBlue.java`, `PlayerDAO.java` |

### 2.2. Thư viện trong `SRC/lib`

| File JAR | Dùng trong code? | Ghi chú |
|---|---|---|
| `HikariCP-5.1.0.jar` | Có (`com.zaxxer.hikari`, 2 file) | Connection pool – `LocalManager.createConfig` |
| `mysql-connector-java8-5.1.23.jar` | Có (`com.mysql.jdbc`, 3 file) | JDBC driver |
| `json-simple-1.1.jar` | Có (`org.json.simple`, 31 file) | Parse các cột JSON trong DB |
| `gson-2.8.2.jar` | Có (`com.google.gson`, 4 file) | VD: lưu `nhiem_vu_kol` (`PlayerDAO.createNewPlayer`) |
| `lombok.jar` | Có (`@Getter/@Setter/@Data/@Builder/@NonNull`…) | Cần bật annotation processing khi build |
| `apache-commons-lang.jar` | Có (1 file `org.apache.commons`) | |
| `slf4j-api-2.0.0-alpha1.jar`, `slf4j-simple-2.0.0-alpha1.jar` | Gián tiếp (HikariCP log) | |
| `log4j-1.2.17.jar` | Không thấy import trực tiếp | Logging dùng lớp tự viết `nro.models.utils.Logger` |
| `java-json.jar` | Không thấy import `org.json.*` (ngoài `org.json.simple`) | Có thể thừa |
| `mongodb-driver-core-5.1.3.jar`, `mongodb-driver-sync-5.1.3.jar`, `bson-5.1.3.jar` | **Không** có import `com.mongodb`/`org.bson` trong source | Được đóng gói vào `20.jar` (1284 class `com/mongodb`, 394 class `org/bson`) nhưng không dùng |

---

## 3. Cấu trúc thư mục dự án

```
Teamobi2026/
├── LÂU CỒ MOD/              # Client Unity (binary)
├── Lệnh admin.docx          # Danh sách lệnh admin (xem docs/15-lenh-admin-gm.md)
├── database team2026.sql    # Dump MySQL (41 bảng)
├── item.xlsx                # Bảng tính item (tham khảo)
├── docs/                    # Tài liệu spec
└── SRC/                     # Thư mục làm việc (working dir) của server
    ├── 20.jar               # Fat-jar đang được run.bat chạy (4600 entry, ~8,3 MB)
    ├── Config.properties    # Cấu hình server + DB
    ├── maintenanceConfig.txt
    ├── run.bat              # Chạy server
    ├── restart_server.bat   # Đợi 5 phút rồi gọi run.bat
    ├── build.xml, manifest.mf, nbproject/   # Project NetBeans/Ant
    ├── build/               # Output compile (classes)
    ├── dist/                # Output "Clean & Build": NgocRongOnline.jar + dist/lib/*.jar
    ├── lib/                 # Thư viện (mục 2.2)
    ├── data/                # Tài nguyên gửi client (mục 11)
    ├── sql/nro1.sql         # Một dump SQL khác (DB tên `a`, ~1 MB, ngày 06/09/2025)
    ├── test/                # (trống)
    └── src/
        ├── models/          # 5 lớp cũ, package `models` (chỉ ShenronEvent còn được import)
        └── nro/models/      # Toàn bộ source chính (mục 4)
```

> **Quan trọng:** mọi đường dẫn file trong code là **tương đối** (`Config.properties`, `data/...`, `log/...`), nên server phải được chạy với working directory = `SRC/`.

---

## 4. Vai trò từng package trong `nro/models`

Số file trong ngoặc là số file `.java` (kể cả thư mục con).

| Package | Số file | Vai trò | File tiêu biểu |
|---|---|---|---|
| `server` | 11 | Điểm vào, khởi động, quản lý client, controller xử lý message, lệnh chat admin, bảo trì | `ServerManager`, `Manager`, `Controller`, `Client`, `Command`, `MenuController`, `Maintenance`, `AutoMaintenance`, `ServerNotify`, `ServerLog`, `AutoUpdate_NhanQuaFree` |
| `network` | 12 | Tầng mạng: accept socket, session, mã hoá XOR, đọc/ghi message | `Network`, `Session`, `MySession`, `Message`, `MessageSendCollect`, `Collector`, `Sender`, `KeyHandler`, `SessionManager` |
| `interfaces` | 11 | Interface cho network (`ISession`, `INetwork`, `IMessage`…), boss (`IBoss`, `IEventBoss`), PVP (`IPVP`) | |
| `data` | 5 | Truy cập DB (Hikari pool, ResultSet copy vào bộ nhớ) + gửi dữ liệu game cho client | `LocalManager`, `ResultSetImpl`, `DataGame`, `ItemData` |
| `database` | 7 | DAO: login/load player, lưu player, shop, siêu hạng, lịch sử giao dịch, sự kiện | `MrBlue` (login + loadPlayer), `PlayerDAO`, `ShopDAO`, `SuperRankDAO`, `HistoryTransactionDAO`, `EventDAO`, `TraningDAO` |
| `consts` | 21 | Hằng số: cmd message, NPC, map, item, player, task, boss… | `Cmd_message`, `ConstNpc`, `ConstMap`, `ConstPlayer`, `ConstTask`, `ConstItem` |
| `managers` | 8 | Manager chạy nền/giữ trạng thái: giftcode, PVP, rồng thần sự kiện, siêu hạng, bảng top phó bản | `GiftCodeManager`, `PVPManager`, `SuperRankManager`, `ShenronEventManager`, `TopBanDoKhoBau`, `TopKhiGasHuyDiet`, `TopConDuongRanDoc`, `MyClanTopBanDoKhoBau` |
| `utils` | 9 | Tiện ích: random/tỉ lệ, thời gian, log màu, đọc file, số liệu hệ thống, skill | `Util`, `TimeUtil`, `Logger`, `FileIO`, `Functions`, `SkillUtil`, `SystemMetrics`, `StringUtil`, `FileRunner` |
| `player` | 27 | Thực thể người chơi và dữ liệu con: chỉ số, túi đồ, đệ tử, bùa, hiệu ứng, bạn/thù, hợp thể, luyện tập… | `Player`, `NPoint`, `Inventory`, `Pet`, `NewPet`, `Charms`, `EffectSkill`, `IDMark`, `Fusion` |
| `player_system` | 3 | Template dữ liệu tĩnh (item/map/mob/npc/skill/part…), chống đăng nhập sai, giftcode | `Template`, `AntiLogin`, `GiftCode` |
| `player_badges` | 4 | Hệ thống danh hiệu (badges) | `BadgesService`, `BagesTemplate` |
| `item` | 2 | Item & option, item có thời gian | `Item`, `ItemTime` |
| `skill` | 3 | Skill, lớp nhân vật (NClass), skill của player | `Skill`, `NClass`, `PlayerSkill` |
| `intrinsic` | 2 | Nội tại | `Intrinsic`, `IntrinsicPlayer` |
| `map` (+ `map/phoban`, `map/service`) | 17 | Map, zone (khu), item rơi, waypoint, bẫy; dịch vụ chuyển map, NPC manager, phó bản | `Map`, `Zone`, `ItemMap`, `ChangeMapService`, `MapService`, `NpcManager`, `NpcService` |
| `mob` | 5 | Quái thường, quái của người chơi (trứng/MobMe), BigBoss | `Mob`, `MobPoint`, `MobMe`, `BigBoss` |
| `mob_bigboss` | 9 | Các "big boss" dạng mob (Hirudegarn, Vua Bạch Tuộc, Robot Bảo Vệ, Máy Đo Sức Mạnh…) | |
| `boss` (+ ~35 thư mục con) | 159 | Toàn bộ boss: Android, Cell, Frieza, Broly, Black Goku, Majin Buu 12h/14h, boss sự kiện (Tết, Noel, Trung Thu, Hùng Vương, Halloween), phó bản (Doanh trại, Bản đồ kho báu, Khí gas), `Boss_Manager` (16 manager) | `Boss`, `BossData`, `BossesData`, `BossID`, `Boss_Manager/BossManager` |
| `boss_con_duong_ran_doc` | 3 | Boss phó bản Con đường rắn độc (Cadich, Nadic, Saibamen) | |
| `npc` | 8 | Lớp NPC gốc, factory tạo NPC, cây đậu thần, trứng Mabu/Dưa hấu, menu Con Mèo/Rồng Thiêng | `Npc`, `NpcFactory`, `MagicTree`, `MabuEgg`, `DuaHauEgg`, `NonInteractiveNPC` |
| `npc_list` | 59 | Mỗi NPC một class (Quy Lão Kame, Bulma, Santa, Whis, Bill, Vados, Ký Gửi…) | |
| `services` (+ `services/shenron`) | 27 | Dịch vụ gameplay dùng chung: gửi gói tin (`Service`), túi đồ, item, skill, clan, task, chat thế giới, đệ tử, radar, nội tại, rồng thần | `Service`, `PlayerService`, `InventoryService`, `ItemService`, `SkillService`, `ClanService`, `TaskService`, `ChatGlobalService`, `PetService` |
| `services_func` | 8 | Chức năng: dùng item, giao dịch, form nhập liệu (Input), vòng quay may mắn, minigame, mua lại | `UseItem`, `Trade`, `TransactionService`, `Input`, `LuckyRound`, `MiniGame` |
| `services_dungeon` | 10 | Dịch vụ phó bản/sự kiện map: NR Sao Đen, NR Namếc, Doanh trại, Bản đồ kho báu, Khí gas, Con đường rắn độc, Majin Buu, Siêu thần thuỷ, luyện tập | `BlackBallWarService`, `NgocRongNamecService`, `RedRibbonHQService`, `TreasureUnderSeaService`, `DestronGasService`, `SnakeWayService`, `MajinBuuService` |
| `combine` | 32 | Pha lê hoá, ép sao, nâng cấp, bông tai, sách tuyệt kỹ, trang bị thiên sứ, chuyển hoá… (NPC Bà Hạt Mít, Thần Vũ Trụ…) | `CombineService`, `CombineSystem`, `NangCapVatPham`, `PhaLeHoaTrangBi`… |
| `shop` | 12 | Cửa hàng NPC và các loại tab | `ShopService`, `Shop`, `TabShop*` |
| `shop_ky_gui` | 3 | Shop ký gửi | `ConsignShopService`, `ConsignShopManager`, `ConsignItem` |
| `clan` | 3 | Bang hội | `Clan`, `ClanMember`, `ClanMessage` |
| `task` | 10 | Nhiệm vụ chính, phụ, bang, danh hiệu | `TaskMain`, `SubTaskMain`, `SideTask`, `ClanTask`, `BadgesTaskService` |
| `matches` (+ `dai_hoi_vo_thuat`, `giai_dau`) | 22 | PVP, thách đấu, trả thù, luyện tập, Đại hội võ thuật, Siêu hạng, Võ đài sinh tử, giải đấu thế giới | `PVPService`, `ThachDau`, `SuperRank`, `The23rdMartialArtCongressManager`, `DeathOrAliveArenaManager`, `WorldMartialArtsTournamentManager` |
| `minigame` | 5 | Minigame "Chọn ai đây" và "Con số may mắn" (vàng/ngọc) | `ChonAiDay_Gem/Gold`, `ConSoMayManGem/Gold` |
| `event`, `event_list`, `ievent` | 5 + 8 + 1 | Khung sự kiện (`EventManager`) và các sự kiện cụ thể (Tết, 8/3, Halloween, Noel, Hùng Vương, Trung Thu, TopUp, Default) | `EventManager`, `HungVuong`, `TopUp` |
| `radar` | 3 | Thẻ radar (card sưu tầm) | `RadarCard`, `Card`, `OptionCard` |
| `daily_Giftcode` | 2 | Quà hằng ngày | `DailyGiftService`, `DailyGiftData` |
| `Bot` | 7 | Bot giả người chơi (đánh quái, bán item, săn boss, tấn công người chơi) | `BotManager`, `NewBot`, `Bot`, `BotPemQuai`, `BotGiaoDich`, `BotSanBoss`, `BotAttackplayer` |

Package cũ `SRC/src/models/` (package `models`): `AntiLogin`, `ConsignItem`, `GiftCode`, `ShenronEvent`, `Template`. Chỉ `models.ShenronEvent` còn được dùng (import trong `SRC/src/nro/models/managers/ShenronEventManager.java` dòng 3); các lớp còn lại trùng tên với lớp mới trong `nro.models.*`.

---

## 5. Luồng khởi động server

Nguồn: `SRC/src/nro/models/server/ServerManager.java`, `SRC/src/nro/models/server/Manager.java`.

### 5.1. Sơ đồ tổng

```
main()
 ├─ timeStart = "dd/MM/yyyy HH:mm:ss"
 ├─ new Thread("ServerMain") → ServerManager.gI().run()
 │     ServerManager.gI() → init():
 │        ├─ Manager.gI()  (constructor Manager)
 │        │    ├─ loadProperties()          ← đọc Config.properties
 │        │    ├─ loadDatabase()            ← nạp toàn bộ dữ liệu tĩnh từ DB (mục 5.2)
 │        │    ├─ NpcFactory.createNpcConMeo(), createNpcRongThieng()
 │        │    └─ initMap()                 ← dựng Map/Zone, mob, NPC; lịch update map 1s
 │        └─ HistoryTransactionDAO.deleteHistory()  ← xoá lịch sử giao dịch cũ hơn 3 ngày
 │     run():
 │        ├─ isRunning = true
 │        ├─ activeServerSocket()           ← mở cổng PORT, gắn Controller
 │        ├─ khởi động các thread dịch vụ (mục 6)
 │        ├─ BossManager.loadBoss(); MAPS.forEach(Map::initBoss); EventManager.init()
 │        ├─ khởi động các thread boss, bot, minigame
 │        └─ startTopUpdater()              ← mỗi 3000 ms cập nhật bảng TOP nếu có cờ thay đổi
 └─ activeCommandLine()  ← thread main đọc lệnh console (mục 10)
```

Nếu `loadProperties()` lỗi IO hoặc `loadDatabase()` ném exception → `System.exit(0)` (`Manager.java` dòng 172-175, 960-962).

### 5.2. Thứ tự nạp dữ liệu trong `Manager.loadDatabase()`

| # | Bảng / nguồn | Nạp vào | Ghi chú |
|---|---|---|---|
| 1 | `part` | danh sách Part → **ghi ra file `data/update_data/part`** | Mỗi lần khởi động file `part` được tạo lại từ DB (dòng 314-325) |
| 2 | `bg_item_template` | `Manager.BG_ITEMS` | |
| 3 | `array_head_2_frames` | `ARR_HEAD_2_FRAMES` | |
| 4 | `clan` | `CLANS` (+ thành viên JSON, thành tích BDKB) | `Clan.NEXT_ID = max(id)+1` |
| 5 | `skill_template` (order by `nclass_id, slot`) | `NCLASS` (3 lớp: Trái Đất / Namếc / Xayda) | |
| 6 | `head_avatar` | `HEAD_AVATARS` | |
| 7 | `flag_bag` | `FLAGS_BAGS` | |
| 8 | `intrinsic` | `INTRINSICS`, `INTRINSIC_TD/NM/XD` (gender khác 0-2 → cả 3 hệ) | |
| 9 | `task_main_template` JOIN `task_sub_template` | `TASKS` | |
| 10 | `side_task_template` | `SIDE_TASKS_TEMPLATE` (5 mức `max_count_lvN` dạng "a-b") | |
| 11 | `task_badges_template` | `TASKS_BADGES_TEMPLATE` | |
| 12 | `clan_task_template` | `CLAN_TASKS_TEMPLATE` | |
| 13 | `achievement_template` | `ACHIEVEMENT_TEMPLATE` | |
| 14 | `item_template` (LIMIT 750 OFFSET n, lặp) | `ITEM_TEMPLATES` | Nạp theo lô 750 |
| 15 | `item_option_template` | `ITEM_OPTION_TEMPLATES` | |
| 16 | `shop` (+ `tab_shop`, `item_shop`, `item_shop_option`) | `SHOPS` qua `ShopDAO.getShops` | |
| 17 | `notify` (order by id desc) | `NOTIFY` dạng `name<>text` | Tab thông báo |
| 18 | `img_by_name` | `IMAGES_BY_NAME` (tên → số frame) | |
| 19 | (tính) mount | `DataGame.MAP_MOUNT_NUM`: item type 23 có ảnh `mount_<part>_0` → `part + 30000` | |
| 20 | `shop_ky_gui` | `ConsignShopManager.listItem` | |
| 21 | `mob_template` | `MOB_TEMPLATES` | |
| 22 | `npc_template` | `NPC_TEMPLATES` | |
| 23 | `data_badges` | `BAGES_TEMPLATES` | |
| 24 | `map_template` | `MAP_TEMPLATES` (waypoints, mobs, npcs dạng JSON) | |
| 25 | `radar` | `RadarService.RADAR_TEMPLATE` | |
| 26 | `giftcode` | `GiftCodeManager.listGiftCode` (`count_left = -1` → 999.999.999) | |
| 27 | `player` (4 truy vấn TOP 100) | `Topsukien`, `Topsukien1`, `Topwhis`, `Topmaydam` | Sắp theo `point_sukien`, `point_sukien1`, `thachdauwhis`, `point_maydam` |

### 5.3. `initMap()`

- Đọc `data/map/tile_set_info` (loại tile TOP) và `data/map/tile_map_data/<mapId>` cho từng map (`Manager.readTileIndexTileType`, `readTileMap`).
- Với mỗi `MapTemplate`: tạo `nro.models.map.Map`, `initMob(...)`, `initNpc(...)`; sau đó `new NonInteractiveNPC().initNonInteractiveNPC()`.
- Lập lịch `scheduler.scheduleAtFixedRate(..., 0, 1, SECONDS)`: gom zone theo lô **10 zone/task**, chạy song song trên `ExecutorService` cố định số thread = số CPU, đợi tất cả xong (`Manager.java` dòng 199-239). ⇒ **Mỗi zone update 1 lần/giây.**

---

## 6. Danh sách thread nền khi server chạy

| Thread / Executor | Chu kỳ | Tạo ở | Chức năng |
|---|---|---|---|
| `ServerMain` | 1 lần | `ServerManager.main` | Khởi tạo & chạy `run()` |
| main thread | chặn đọc stdin | `activeCommandLine()` | Lệnh console |
| `Network` | `selector.select(500)` | `Network.init/start` | Accept kết nối |
| `Collector - IP : x` / `Sender - IP : x` | liên tục; sender nghỉ 10 ms khi hàng đợi rỗng | `Session` | **2 thread / 1 kết nối** |
| `Update Client` | 1000 ms | `Client` constructor | Đếm `timeWait` của session chưa login (mục 01) |
| Map scheduler | 1 s | `Manager.initMap` | Update toàn bộ zone |
| `Update NRNM` | – | `NgocRongNamecService` | Ngọc rồng Namếc |
| `Update Super Rank` | 500 ms | `SuperRankManager` | Ghép trận siêu hạng (map 113) |
| `Update DHVT23`, `Update Võ Đài Sinh Tử`, `Update WMAT` | – | `matches/giai_dau/*Manager` | Giải đấu |
| `Update Bảo Trì Tự Động` | 1000 ms | `AutoMaintenance` | Bảo trì tự động 04:30 |
| `Update Shenron` | 1000 ms | `ShenronEventManager` | Rồng thần sự kiện |
| `Update boss`, `yardart`, `final boss`, `skill-summoned`, `broly`, `other boss`, `red ribbon hq`, `treasure under sea`, `snake way`, `gas destroy` | BossManager: 1500 ms | `ServerManager.run` | 10 manager boss |
| `Thread Bot Game` | 150 ms | `BotManager` | Update bot |
| `Thread MiniGame` ×2, `ConSoMayManGoldThread`, `ConSoMayManGemThread` | – | `minigame/*` | Minigame |
| Top updater | 3000 ms | `ServerManager.startTopUpdater` | Nạp lại TOP khi có cờ `isTop*Changed` |
| `**Chat global` | 1000 ms | `ChatGlobalService` | Hàng đợi chat thế giới |
| `TransactionService` | 300 ms | `TransactionService.gI()` | Update giao dịch |
| `PVPManager` | 1000 ms | `PVPManager` | Update các trận PVP |
| `ServerNotify` | 1000 ms | `ServerNotify.gI()` (lazy) | Gửi thông báo chạy chữ (cmd 93); tự gửi "Chào mừng bạn đã đến server Ngọc Rồng Online" mỗi **500.000 ms (~8,3 phút)** |
| Thread riêng mỗi player | 1000 ms | `Player.start()` (gọi trong `Controller.sendInfo`) | `Player.update()` |

---

## 7. Build & chạy server

### 7.1. Yêu cầu

- JDK 17+.
- MySQL/MariaDB, import `database team2026.sql` vào DB tên `team2026` (hoặc sửa `database.name`).
- Windows (khuyến nghị) – do `run.bat`, `restart_server.bat`, lệnh `cmd /c` trong code.

### 7.2. Build

| Cách | Lệnh / thao tác | Kết quả |
|---|---|---|
| NetBeans | Mở `SRC/` là project → *Clean and Build* | `SRC/dist/NgocRongOnline.jar` + `SRC/dist/lib/*.jar` (manifest có `Class-Path: lib/...`) |
| Ant | `cd SRC && ant jar` (dùng `nbproject/build-impl.xml`) | Như trên |
| Fat-jar `20.jar` | Không có script tạo trong repo (được build riêng) | Chứa cả class `nro/models` (654 entry) và toàn bộ thư viện |

> `dist/README.TXT`: chạy bằng `java -jar "NgocRongOnline.jar"` trong thư mục `dist` – nhưng khi chạy ở `dist/` sẽ **không** thấy `Config.properties` và `data/` (đường dẫn tương đối). Nên copy jar ra `SRC/` hoặc chạy với working dir `SRC/`.

### 7.3. Chạy

`SRC/run.bat`:

```bat
@ECHO OFF
java -server -Dfile.encoding=UTF-8  -jar 20.jar
PAUSE
```

`SRC/restart_server.bat`:

```bat
@ECHO OFF
echo Sever Se Tu Khoi Dong Lai Sau 5 Phut Nua...
timeout /t 300
call run.bat
```

- Không có tham số `-Xmx`/`-Xms` → heap mặc định theo JVM.
- `-Dfile.encoding=UTF-8` cần thiết cho tiếng Việt.
- Sau khi bảo trì (`ServerManager.close()`), server tự gọi `cmd /c start restart_server.bat` → đợi **300 giây** → chạy lại `run.bat`.

---

## 8. Cấu hình `Config.properties`

File được đọc **2 lần**: `Manager.loadProperties()` (nhóm `server.*`) và static block của `LocalManager.loadProperties()` (nhóm `database.*`). File hiện tại có dòng kết thúc CRLF.

### 8.1. Nhóm `server.*` (`SRC/src/nro/models/server/Manager.java` hàm `loadProperties`, dòng ~1027-1076)

| Key | Giá trị hiện tại | Biến trong code (mặc định nếu thiếu key) | Tác dụng thực tế |
|---|---|---|---|
| `server.local` | `false` | `Manager.LOCAL` (false) | Nếu `true`: mọi lần login bị từ chối với thông báo "Server này chỉ để lưu dữ liệu\nVui lòng qua server khác" (`MySession.login` dòng 105-108) |
| `server.test` | `false` | `Manager.TEST` (false) | Nếu `true`: nhân vật mới được **1.000.000.000 ngọc xanh** (thay vì 10.000) và bắt đầu từ **nhiệm vụ id 28** (thay vì 0) (`PlayerDAO.createNewPlayer` dòng 40, 231) |
| `server.daoautoupdater` | `false` | `Manager.DAO_AUTO_UPDATER` (false) | Chỉ được đọc, **không dùng ở đâu** |
| `server.sv` | `1` | `Manager.SERVER` (1) | Chỉ được đọc, **không dùng ở đâu** |
| `server.name` | `NRO` | `ServerManager.NAME` ("Ngọc Rồng Online") | Tên server hiển thị trong danh sách server; nếu giá trị là `Local` → `" Local"`. Cũng dùng trong câu chào tutorial "Chào Mừng <tên> Đến Với: <NAME>" (`Controller.messageNotMap` case 13) |
| `server.ip` | `36.50.135.16` | `ServerManager.IP` ("36.50.135.149") | Ghép vào chuỗi danh sách server gửi client: `NAME:IP:PORT:0,` |
| `server.port` | `14445` | `ServerManager.PORT` (14445) | Cổng TCP lắng nghe (`Network.start(PORT)`) |
| `server.sv1` … `server.sv10` | `server.sv1=NroLight:fw.patus.tech:14449:0,0,0` | nối vào `DataGame.LINK_IP_PORT` | Thêm server khác vào danh sách server của client (mỗi key được nối thêm `":0,"`). Xem mục multi-server trong `01-kien-truc-server-network.md` |
| `server.waitlogin` | `3` | `Manager.SECOND_WAIT_LOGIN` (5) – kiểu `byte` | Số giây tối thiểu giữa 2 lần login / giữa logout và login lại (`MrBlue.login` dòng 88-103) |
| `server.maxperip` | `999` | `Manager.MAX_PER_IP` (1000) | Số kết nối đồng thời tối đa từ 1 IP (`ServerManager.canConnectWithIp`) |
| `server.maxplayer` | `1000` | `Manager.MAX_PLAYER` (2000) | Số người chơi online tối đa; vượt → "Máy chủ hiện đang quá tải..." (`MySession.login` dòng 113) |
| `server.expserver` | `3` | `Manager.RATE_EXP_SERVER` (1) – kiểu `byte` | Hệ số nhân **tiềm năng** nhận được khi đánh (`NPoint.java` dòng 1649: `tiemNang *= Manager.RATE_EXP_SERVER`) → server hiện **x3 TNSM** |

### 8.2. Nhóm `database.*` (`SRC/src/nro/models/data/LocalManager.java` dòng 58-99, 179-195)

| Key | Giá trị hiện tại | Tác dụng |
|---|---|---|
| `database.driver` | `com.mysql.jdbc.Driver` | `HikariConfig.setDriverClassName` |
| `database.host` | `localhost` | Host MySQL |
| `database.port` | `3306` | Port MySQL |
| `database.name` | `team2026` | Tên DB; JDBC URL: `jdbc:mysql://host:port/name?useUnicode=yes&characterEncoding=UTF-8` |
| `database.user` | `root` | User |
| `database.pass` | *(rỗng)* | Mật khẩu |
| `database.min` | `1` | `minimumIdle` của pool |
| `database.max` | `1` | `maximumPoolSize` – **pool chỉ có 1 kết nối** |
| `database.lifetime` | `120000` | `maxLifetime` (ms) = 2 phút |
| `database.log` | *(không có)* | Nếu `true` → log mọi câu SQL thực thi (`LocalManager.LOG_QUERY`) |

Thuộc tính Hikari cố định trong code: `poolName="User Management"`, `cachePrepStmts=true`, `prepStmtCacheSize=250`, `prepStmtCacheSqlLimit=2048`, `useServerPrepStmts=true`.

---

## 9. Bảo trì

### 9.1. `Maintenance` (bảo trì có đếm ngược)

`SRC/src/nro/models/server/Maintenance.java`

| Hàm | Hành vi |
|---|---|
| `startCountdown()` | Đếm ngược **60 giây** |
| `startSeconds(n)` | Đếm ngược `n` giây (lệnh console `bt` và menu admin "Bảo trì" dùng **5 giây**) |
| `startImmediately()` | Gọi `ServerManager.close()` ngay |
| `run()` | Mỗi giây gửi cho tất cả người chơi (cmd `-25`): "Hệ thống sẽ bảo trì sau N giây nữa. Hãy thoát game để tránh mất dữ liệu." → hết giờ gọi `ServerManager.gI().close()` |

Khi `Maintenance.isRunning = true`:
- Login bị chặn: "Server đang trong thời gian bảo trì, vui lòng quay lại sau" (`MySession.login`).
- Không tạo nhân vật (`Controller.createChar`), không mua/bán shop (cmd 6, 7), khoá/chấp nhận giao dịch bị huỷ (`TransactionService`).
- Nhiều vòng lặp nền dừng (`while (!Maintenance.isRunning)`): `ServerNotify`, `SuperRankManager`, `ShenronEventManager`, giải đấu, `TransactionService`, `ChatGlobalService`, thread của từng `Player`.

`ServerManager.close()`:
1. `isRunning = false` (dừng Client update, BossManager, BotManager…).
2. `ClanService.gI().close()` – lưu clan.
3. `ConsignShopManager.gI().save()` – lưu shop ký gửi.
4. `Client.gI().close()` – kick toàn bộ player (mỗi lần kick sẽ `PlayerDAO.updatePlayer` → lưu dữ liệu).
5. Chạy `cmd /c start restart_server.bat`, rồi `System.exit(0)`.

### 9.2. `AutoMaintenance` (bảo trì tự động hằng ngày)

`SRC/src/nro/models/server/AutoMaintenance.java`

| Thông số | Giá trị |
|---|---|
| Giờ bảo trì | `hours = 4`, `mins = 30` → **04:30** (giờ hệ thống) |
| Cửa sổ kích hoạt | từ 04:29:59 đến trước 04:35:00 |
| Thời gian báo trước | **5 phút** (`totalWait = 5*60*1000`) |
| Tần suất thông báo | mỗi **15 giây**: "Máy chủ sẽ bảo trì trong X phút Y giây nữa. Vui lòng thoát game để tránh mất dữ liệu." |
| Sau 5 phút | `Maintenance.gI().startCountdown()` → thêm **60 giây** đếm ngược → `close()` |
| Bật/tắt | `AutoMaintenance.AutoMaintenance` (bật mặc định trong `ServerManager.run`); lệnh console `bat` / `tat` |

Tổng thời gian: ~04:30 bắt đầu báo → ~04:35 bắt đầu đếm 60 s → ~04:36 tắt server → +300 s (`restart_server.bat`) → ~04:41 server khởi động lại.

### 9.3. `maintenanceConfig.txt`

Nội dung hiện tại (3 dòng): `21`, `25`, `0`.

**Không có đoạn code nào đọc file này** (grep `maintenanceConfig` trong `SRC/src` không có kết quả). Giờ bảo trì tự động được hard-code 04:30 trong `AutoMaintenance`. Nhiều khả năng đây là file cấu hình còn sót từ phiên bản cũ (giờ/phút/giây?).

### 9.4. Reset quà hằng ngày (code chết)

`AutoUpdate_NhanQuaFree` (lập lịch mỗi 24h, gọi `Client.close()` + `ServerManager.resetNhanQuaHangNgay()`) **không được khởi tạo ở đâu**. `resetNhanQuaHangNgay()` còn hard-code `jdbc:mysql://localhost:3306/ngocrong` user `root` pass rỗng, không dùng `Config.properties`.

---

## 10. Lệnh console của server

Đọc từ stdin trong `ServerManager.activeCommandLine()`:

| Lệnh | Tác dụng |
|---|---|
| `bt` | `Maintenance.gI().startSeconds(5)` – bảo trì sau 5 giây |
| `bat` | Bật bảo trì tự động (`AutoMaintenance.AutoMaintenance = true`) |
| `tat` | Tắt bảo trì tự động |
| `run` | Chạy `cmd /c run.bat` (khởi thêm 1 tiến trình server – cẩn thận trùng cổng) |
| khác | In "Lệnh không hợp lệ." |

---

## 11. Thư mục `SRC/data` – tài nguyên gửi cho client

Tài nguyên được đọc bởi `SRC/src/nro/models/data/DataGame.java` (và `Manager.initMap`). Nhiều thư mục chia theo **zoom level** `x1`–`x4`; zoom level do client gửi ở gói `-29/2` (`Service.setClientType` → `session.zoomLevel`).

| Thư mục | Kích thước / số file | Định dạng | Dùng bởi | Cmd gửi |
|---|---|---|---|---|
| `data/update_data/` | 6 file: `arrow`, `dart`, `effect`, `image`, `part`, `skill` (~208 KB) | nhị phân | `DataGame.updateData` – gửi cả 6 file trong 1 message | `-87` (`UPDATE_DATA`) |
| `data/map/tile_set_info` *(trên đĩa tên `tile_set_Info`)* | 1 file | nhị phân | `Manager.readTileIndexTileType`, `DataGame.sendTileSetInfo` | `-82` (`TILE_SET`) |
| `data/map/tile_map_data/<mapId>` | 179 file (~724 KB) | nhị phân (w,h,tile[]) | `Manager.readTileMap`, `DataGame.sendMapTemp` | `-28` sub `10` |
| `data/map/item_bg_map_data/` | 178 file | nhị phân | `Manager.MapBgDataManager` đọc `item_bg_map_data1/` (sai tên, và lớp không được gọi) | – |
| `data/map/eff_map/` | `126`, `eff_map.rar` | | Không thấy tham chiếu | – |
| `data/map/11` | 1 file lẻ | | Không thấy tham chiếu | – |
| `data/effdata/DataEffect_<id>` | 152 file (~608 KB) | nhị phân | `DataGame.sendEffectTemplate` | `-66` (`GET_EFFDATA`) |
| `data/effect/x{1..4}/ImgEffect_<id>.png` | 134–144 file/zoom (~30 MB) | PNG | `DataGame.sendEffectTemplate` | `-66` |
| `data/icon/x{1..4}/<id>.png` | 15.121–16.478 file/zoom (~415 MB) | PNG | `DataGame.sendIcon` | `-67` (`REQUEST_ICON`) |
| `data/img_by_name/x{1..4}/<tên>.png` | ~290 file/zoom (~143 MB) | PNG | `DataGame.sendImageByName` (kèm số frame từ bảng `img_by_name`) | `66` (`GET_IMG_BY_NAME`) |
| `data/item_bg_temp/x{1..4}/<id>.png` | ~455 file/zoom (~36 MB) | PNG | `DataGame.sendItemBGTemplate` | `-32` (`BACKGROUND_TEMPLATE`) |
| `data/mob/x{1..4}/<mobTempId>` | ~108 file/zoom (~40 MB) + `mob.rar` | nhị phân | `DataGame.requestMobTemplate` | `11` |
| `data/res/x{1..4}/*` | x1: 148, x2: 1413, x3: 1393, x4: 1354 file (~63 MB) | nhị phân/ảnh | `DataGame.sendSizeRes` (đếm file), `DataGame.sendRes` (gửi **tất cả** file của zoom) | `-74` sub 1/2/3 |

Phiên bản dữ liệu (hard-code trong `DataGame`):

| Biến | Giá trị | Ý nghĩa |
|---|---|---|
| `vsData` | 9 | Phiên bản gói `update_data` |
| `vsMap` | 2 | Phiên bản dữ liệu map/npc/mob template |
| `vsSkill` | 1 | Phiên bản skill |
| `vsItem` | 9 | Phiên bản item template |
| `vsRes` | 1 | Phiên bản resource |
| `maxSmallVersion` | 32767 | Số byte "small version" gửi mỗi lần login (cmd `-77`) |

Bảng mốc sức mạnh chuẩn gửi kèm `sendVersionGame` (22 mốc): 1.000; 3.000; 15.000; 40.000; 90.000; 170.000; 340.000; 700.000; 1.500.000; 15.000.000; 150.000.000; 1.500.000.000; 5.000.000.000; 10.000.000.000; 40.000.000.000; 50.010.000.000; 60.010.000.000; 70.010.000.000; 80.010.000.000; 100.010.000.000; 1.000.010.000.000; 10.000.010.000.000 (`DataGame.java` dòng 54-56).

---

## 12. Cơ sở dữ liệu (tóm tắt)

Dump `database team2026.sql` có **41 bảng**. Mức độ sử dụng bởi server (grep câu SQL trong `SRC/src`):

| Nhóm | Bảng |
|---|---|
| Dữ liệu tĩnh nạp khi khởi động | `part`, `bg_item_template`, `array_head_2_frames`, `skill_template`, `head_avatar`, `flag_bag`, `intrinsic`, `task_main_template`, `task_sub_template`, `side_task_template`, `task_badges_template`, `clan_task_template`, `achievement_template`, `item_template`, `item_option_template`, `shop`, `tab_shop`, `item_shop`, `item_shop_option`, `notify`, `img_by_name`, `mob_template`, `npc_template`, `data_badges`, `map_template`, `radar` |
| Dữ liệu động (đọc/ghi) | `account`, `player`, `clan`, `giftcode`, `shop_ky_gui`, `super_rank`, `history_transaction` |
| **Không** được server dùng (thường là web/panel nạp tiền) | `adminpanel`, `bank_transfers`, `comments`, `napthe`, `payments`, `phongchat`, `posts`, `settings` |

Chi tiết cấu trúc bảng: xem các tài liệu DB chuyên biệt trong `docs/`.

---

## 13. Log & file sinh ra khi chạy

| File | Tạo bởi | Nội dung |
|---|---|---|
| Console (màu ANSI) | `nro.models.utils.Logger` | Log khởi động, login ("Player Login: tên: N ms"), lưu player, lỗi |
| `data/update_data/part` | `Manager.loadDatabase` (mỗi lần khởi động), `Manager.loadPart` (khi chat `part`) | Dữ liệu part xuất từ bảng `part` |
| `log/Combine_dd_MM_yyyy.txt` | `ServerLog.logCombine` | Log ép/nâng cấp |
| `log/ItemDrop_dd_MM_yyyy.txt` | `ServerLog.logItemDrop` | Log rơi đồ |
| `log/<tênPlayer>_log.txt` | `Logger.fileLog` (tự tạo thư mục) | Log theo người chơi |
| `admin/AD_dd_MM_yyyy.txt` | `ServerLog.logAdmin` | **Không ghi gì** (hàm chỉ tạo tên file) |

---

## 14. Ghi chú / điểm cần lưu ý

1. **Pool DB chỉ 1 kết nối** (`database.max=1`) trong khi có hàng chục thread truy cập DB (login, lưu player, top, giftcode…) → dễ nghẽn. Nên tăng (VD 10–20).
2. **`maintenanceConfig.txt` không được đọc** – sửa file này không có tác dụng; giờ bảo trì tự động hard-code 04:30 (`AutoMaintenance.hours/mins`).
3. **`AutoMaintenance` chạy 2 lần song song**: `ServerManager.run()` vừa `new Thread(AutoMaintenance.gI(), ...).start()` vừa `AutoMaintenance.gI().start()` (lớp này `extends Thread`) → 2 vòng lặp cùng gửi thông báo (dòng 116-118).
4. `server.sv` và `server.daoautoupdater` được đọc nhưng không dùng.
5. `ServerManager.updateTop()`: khi `isTopSukien2Changed` lại gán `Topsukien1 = realTop(queryTopsukien1)` thay vì `Topsukien2` (copy-paste bug); `Topsukien2` cũng không được nạp lúc khởi động.
6. `Manager.isExactly11AM()` thực tế kiểm tra 08:13 và không được gọi; `ServerManager.resetNhanQuaHangNgay()` dùng DB `ngocrong` hard-code; `AutoUpdate_NhanQuaFree` không được khởi tạo → code chết.
7. Tên file trên đĩa là `data/map/tile_set_Info` nhưng code đọc `data/map/tile_set_info` → chạy trên Linux/macOS (phân biệt hoa thường) sẽ lỗi đọc tile; trên Windows không sao.
8. `Manager.MapBgDataManager` đọc `data/map/item_bg_map_data1/` (thư mục thực tế là `item_bg_map_data`) và không được gọi.
9. Thư mục `log/` không tồn tại sẵn; `ServerLog.logCombine/logItemDrop` dùng `FileWriter` không tạo thư mục → ném exception (bị catch, in stacktrace) cho tới khi tạo thư mục `SRC/log`.
10. `run.bat` chạy `20.jar` (fat-jar build riêng), không phải `dist/NgocRongOnline.jar` → sau khi sửa code cần build lại và thay `20.jar` (hoặc sửa `run.bat`).
11. Thư viện MongoDB/BSON, `log4j`, `java-json` không được dùng trong source nhưng vẫn đóng gói (tăng kích thước jar).
12. Lệnh console `run` khởi thêm một server mới trong khi server hiện tại vẫn chạy → sẽ lỗi bind cổng ("Error initializing server at port" → `System.exit(0)` trong tiến trình mới).
13. Thông báo big message lúc vào game hard-code "X3 Kinh nghiệm đến hết ngày 11/5. Sự kiện Goku Day…" (`Controller.sendThongBaoServer`) – cần sửa tay khi đổi sự kiện.
14. Trong `EventManager.init()` hiện chỉ bật `Default`, `HungVuong`, `TopUp`; các sự kiện khác (Tết, 8/3, Halloween, Noel, Trung Thu) bị comment dù cờ `= true`.
15. Bảng `adminpanel` trong dump chứa thông tin nhạy cảm dạng plaintext (mật khẩu, API key, token) và bảng `account` có tài khoản test `is_admin=1` mật khẩu yếu → không nên đưa dump này lên môi trường public.
16. `SRC/sql/nro1.sql` là một dump khác (DB tên `a`) – chưa rõ khác biệt với `database team2026.sql`.
