# 24b — Sửa nhóm lỗi chặn nhiệm vụ (boss chết không báo nhiệm vụ + menu NPC hỏng)

> Đợt sửa này chuẩn bị nền cho việc thay toàn bộ tuyến nhiệm vụ chính (thiết kế ở `docs/2-thiet-ke-nhiem-vu-moi/`).
> Nguồn lỗi: [21c §7.2](../3-kiem-tra-can-bang/21c-boss-roi-do-can-bang.md#72-boss-không-gọi-checkdonetaskkillboss) và [21c §5.4](../3-kiem-tra-can-bang/21c-boss-roi-do-can-bang.md#54-boss-thế-giới-nằm-đè-lên-map-tuyến-chính-nguy-hiểm-chết-người).
>
> **Phạm vi đã giữ đúng:** không đổi HP / dame của bất kỳ boss nào; không đụng `shop_ky_gui/**`, `shop/ShopService.java`, `services/shenron/**`, `npc_list/ToriBot.java`, `minigame/**`, `matches/**`, `services/PlayerService.java`, `server/Controller.java`, `services_func/Input.java`, `services/Service.java`.
>
> **Chưa biên dịch kiểm tra** (máy chưa cài xong JDK). Mọi thay đổi đã được soát cú pháp bằng tay + kiểm tra cân bằng ngoặc.

## Mục lục

- [0. Bảng tổng hợp](#0-bảng-tổng-hợp)
- [1. Thiếu `checkDoneTaskKillBoss` khi boss chết](#1-thiếu-checkdonetaskkillboss-khi-boss-chết)
- [2. `Broly.die()` không gọi `reward()`](#2-brolydie-không-gọi-reward)
- [3. Drabura 2 không bao giờ chạy qua `die()`](#3-drabura-2-không-bao-giờ-chạy-qua-die)
- [4. Menu Ôsin không xuống được tầng Mabư](#4-menu-ôsin-không-xuống-được-tầng-mabư)
- [5. NPC Đại Thiên Sứ (id 64) menu rỗng](#5-npc-đại-thiên-sứ-id-64-menu-rỗng)
- [6. Boss sát thương lớn spawn đè map đầu game](#6-boss-sát-thương-lớn-spawn-đè-map-đầu-game)
- [7. `Functions.sleep` làm đứng luồng Con đường rắn độc](#7-functionssleep-làm-đứng-luồng-con-đường-rắn-độc)
- [8. Điểm chưa chắc / việc còn lại](#8-điểm-chưa-chắc--việc-còn-lại)

---

## 0. Bảng tổng hợp

| # | Lỗi | File | Trạng thái |
|---|---|---|---|
| 1 | Thiếu `checkDoneTaskKillBoss` | `Cooler`, `Baby`, `SuperBroly`, `DrLychee`, `Hatchiyack`, `NhanBan` | ✅ đã sửa |
| 1b | Drabura 2 thiếu lời gọi | `MajinBuu_12h/Drabura2.java` | ⚪ **đã có sẵn** — không cần sửa (xem §1.2) |
| 2 | `Broly.die()` không trao thưởng | `Broly/Broly.java` | ✅ đã sửa |
| 3 | Drabura 2 không chạy qua `die()` | `MajinBuu_12h/Drabura2.java` | ✅ đã sửa |
| 4 | Menu Ôsin tầng Mabư | `npc_list/Osin.java` | ✅ đã sửa |
| 5 | Đại Thiên Sứ menu rỗng | `npc_list/DaiThienSu.java` | ✅ đã sửa |
| 6 | Boss đè map đầu game | `boss/BossesData.java` | ✅ đã sửa (12 khối `mapJoin`) |
| 7 | `sleep` trong luồng CDRD | `SAIBAMEN.java`, `CADICH.java` | ✅ đã sửa |

**Tổng: 7/7 mục yêu cầu đã xử lý** (mục 1 có một boss trong danh sách vốn đã đúng, ghi rõ ở §1.2).

---

## 1. Thiếu `checkDoneTaskKillBoss` khi boss chết

### 1.1 Mẫu đúng dùng để bắt chước

`Boss.reward(Player)` (`SRC/src/nro/models/boss/Boss.java:651–654`) — bản mặc định:

```java
@Override
public void reward(Player plKill) {
    TaskService.gI().checkDoneTaskKillBoss(plKill, this);
}
```

Mọi lớp con **override `reward()` mà không gọi `super.reward()`** đều mất lời gọi này. Boss làm đúng, ví dụ `Android/KingKong.java:27`, đặt lời gọi ngay đầu `reward()` rồi mới rơi đồ. Đợt này làm y hệt.

`TaskService.checkDoneTaskKillBoss` (`services/TaskService.java:394–395`) đã tự chặn `player == null`, `isBot`, `isBoss`, `isPet` nên thêm lời gọi không sinh NPE.

### 1.2 Danh sách đã sửa

| Boss | File + hàm | Dòng chèn |
|---|---|---:|
| Cooler (-29) | `boss/Cold/Cooler.java` → `reward()` | 32–33 |
| Baby (-925) | `boss/Baby/Baby.java` → `reward()` | 26–27 |
| Super Broly (-82282) | `boss/Broly/SuperBroly.java` → `reward()` | 54–55 |
| Dr Lychee | `boss/khi_gas/DrLychee.java` → `reward()` | 88–89 |
| Hatchiyack | `boss/khi_gas/Hatchiyack.java` → `reward()` | 87–88 |
| Nhân bản (NhanBan) | `boss/nhan_ban/NhanBan.java` → `reward()` | 36–37 |
| Broly (-1822) | không override `reward()` → dùng `Boss.reward()`; xem [§2](#2-brolydie-không-gọi-reward) | — |

> **Drabura 2 — không khớp mô tả, không sửa phần này.** `boss/MajinBuu_12h/Drabura2.java` **đã có sẵn** `TaskService.gI().checkDoneTaskKillBoss(plKill, this);` ở cuối `reward()` (nay là dòng 104). Vấn đề thật của Drabura 2 là `reward()` **không bao giờ được gọi** — xử lý ở [§3](#3-drabura-2-không-bao-giờ-chạy-qua-die).

### 1.3 Code trước / sau (mẫu chung)

**Trước** — ví dụ `Cooler.reward()`:

```java
@Override
public void reward(Player plKill) {
    BadgesTaskService.updateCountBagesTask(plKill, ConstTaskBadges.TRUM_SAN_BOSS, 1);
    int diem = 5;
    ...
```

**Sau:**

```java
@Override
public void reward(Player plKill) {
    BadgesTaskService.updateCountBagesTask(plKill, ConstTaskBadges.TRUM_SAN_BOSS, 1);
    // FIX: boss chết nhưng không báo hệ thống nhiệm vụ — thêm checkDoneTaskKillBoss cho người kết liễu
    TaskService.gI().checkDoneTaskKillBoss(plKill, this);
    int diem = 5;
    ...
```

**Import thêm** `import nro.models.services.TaskService;` ở 5 file: `Baby.java`, `SuperBroly.java`, `DrLychee.java`, `Hatchiyack.java`, `NhanBan.java` (`Cooler.java` đã có sẵn).

### 1.4 Lý do

Bước "giết boss" của tuyến nhiệm vụ mới (NV 31, 34, 39, 41, 43) hoàn toàn dựa vào `checkDoneTaskKillBoss`. Thiếu lời gọi thì người chơi giết được boss mà nhiệm vụ **không nhích**, tuyến đứt.

### 1.5 Rủi ro

- **Thấp.** Lời gọi chỉ đọc `boss.id` và so với bảng `switch` sẵn có. Boss nào chưa có `case` thì không có gì xảy ra.
- `NhanBan` có `id = Util.createIdBossClone(player.id)` — **không trùng** bất kỳ `case` nào hiện tại, nên hiện tại là lời gọi trống; nó sẽ có tác dụng khi bảng `checkDoneTaskKillBoss` được viết lại theo đánh số NV mới.
- `DrLychee`/`Hatchiyack` chỉ gọi `reward()` khi `plKill != null` (trong `die()`), nên vẫn an toàn.

### 1.6 Cách test

1. Vào `BossID` lấy id của boss cần thử, thêm tạm một `case` in log trong `TaskService.checkDoneTaskKillBoss`.
2. Dùng lệnh admin triệu hồi boss, giết bằng 1 tài khoản thật (không phải bot).
3. Kỳ vọng: log in ra đúng 1 lần, kèm phần thưởng rơi như trước.
4. Kiểm tra bằng bot: `player.isBot = true` → phải **không** có log (đã bị chặn sẵn trong `TaskService`).

---

## 2. `Broly.die()` không gọi `reward()`

**File / hàm:** `SRC/src/nro/models/boss/Broly/Broly.java` → `die(Player)`, dòng 175–184.

**Trước:**

```java
@Override
public void die(Player plKill) {
    this.changeStatus(BossStatus.DIE);
}
```

**Sau:**

```java
@Override
public void die(Player plKill) {
    // FIX: die() cũ không gọi reward() nên người giết không nhận thưởng và không được tính nhiệm vụ
    // (Boss.reward mặc định gọi TaskService.checkDoneTaskKillBoss). Giữ nguyên việc leaveMap() sinh Super Broly.
    if (plKill != null && !plKill.isBot) {
        reward(plKill);
    }
    this.changeStatus(BossStatus.DIE);
}
```

**Lý do:** `Broly` **không** override `reward()`, nên `Boss.reward()` (chỉ gọi `checkDoneTaskKillBoss`) là thứ cần chạy. Điều kiện `plKill != null && !plKill.isBot` sao chép đúng theo `Boss.die()` gốc (`Boss.java:632–648`).

**Giữ nguyên có chủ ý:** `Broly.leaveMap()` (dòng 188–201) vẫn `new SuperBroly(zone, x, y)` — hành vi "Broly chết vẫn sinh Super Broly" là thiết kế của nhiệm vụ 41, **không đụng tới**. Luồng sau khi chết vẫn là `DIE → CHAT_E → LEAVE_MAP → leaveMap()`.

**Rủi ro:** thấp. Broly hiện không rơi vật phẩm nào, nên thay đổi chỉ thêm một lời gọi nhiệm vụ. Nếu sau này gắn phần thưởng cho Broly thì nhớ nó được gọi **mỗi lần chết** (Broly chết rất nhanh vì HP ngẫu nhiên 500–100.000).

**Cách test:** giết Broly bằng tài khoản thật → phải vẫn thấy Super Broly xuất hiện tại chỗ, đồng thời `checkDoneTaskKillBoss` được gọi (thêm log tạm như §1.6).

---

## 3. Drabura 2 không bao giờ chạy qua `die()`

**File / hàm:** `SRC/src/nro/models/boss/MajinBuu_12h/Drabura2.java` → `injured(...)`, dòng 127–136.

**Nguyên nhân (đã xác nhận trong code):** khi đòn đánh đủ giết boss, `injured()` chuyển thẳng trạng thái sang `AFK` và ép `damage = 0`. Vì `damage = 0` nên `nPoint.subHP(0)` → `isDie()` **false** → `setDie()` và `die()` **không chạy**. Sang vòng cập nhật sau, `Boss.update()` gặp trạng thái `AFK` → gọi `afk()` (dòng 178–182) → `changeStatus(BossStatus.DIE)`. Kết quả: boss biến mất mà `reward()` không bao giờ được gọi → **không rơi đồ, không cộng điểm Mabư, không tính nhiệm vụ**.

**Trước:**

```java
if (damage >= this.nPoint.hp) {
    this.changeStatus(BossStatus.AFK);
    damage = 0;
}

this.nPoint.subHP(damage);

if (isDie()) {
    this.setDie(plAtt);
    die(plAtt);
}
```

**Sau:**

```java
// FIX: trước đây đòn chí mạng chuyển thẳng sang AFK -> DIE, bỏ qua die() nên không trao thưởng
// và không gọi checkDoneTaskKillBoss. Nay chặn đòn bằng đúng HP còn lại để chạy qua die() bình thường.
if (damage >= this.nPoint.hp) {
    damage = this.nPoint.hp;
}

this.nPoint.subHP(damage);

if (isDie()) {
    this.setDie(plAtt);
    die(plAtt);
}
```

**Lý do:** đây đúng là luồng chết chuẩn mà `Drabura3.java` (anh em cùng phó bản) đang dùng — `Drabura3.injured()` chỉ chặn trần sát thương rồi để `isDie()` kích hoạt `die()`. `Drabura2` **không** override `die()`, nên `Boss.die()` sẽ gọi `reward(plKill)` (đã sẵn `plKill.fightMabu.changePoint((byte) 10)` + `checkDoneTaskKillBoss` ở dòng 102–103) rồi `changeStatus(DIE)`.

**Rủi ro:**
- `afk()` override của `Drabura2` (dòng ~178) trở thành **mã chết** — để nguyên, vô hại.
- `Boss.die()` sẽ phát `ServerNotify` "Đã tiêu diệt được …" cho toàn server — trước đây Drabura 2 chết im lặng. Nếu không muốn thông báo này thì override `die()` trong `Drabura2` (chưa làm, vì đây là hành vi chuẩn của mọi boss khác).
- Boss sẽ đi qua `DIE → CHAT_E → LEAVE_MAP`, tức có pha chat kết. `BossesData.DRABURA_2` cần có mảng "text chat 3"; nếu rỗng thì `chatE()` trả `true` ngay, không kẹt.

**Cách test:** mở phó bản Mabư 12h, hạ Drabura 2 bằng một đòn lớn (≥ HP còn lại) và bằng nhiều đòn nhỏ. Cả hai trường hợp đều phải: rơi vàng/đồ, `fightMabu` +10 điểm, boss rời map bình thường, và `checkDoneTaskKillBoss` chạy đúng 1 lần.

---

## 4. Menu Ôsin không xuống được tầng Mabư

**File / hàm:** `SRC/src/nro/models/npc_list/Osin.java` → `confirmMenu(Player, int)`.

**Nguyên nhân:** `confirmMenu` có cấu trúc `switch (mapId) { case 52 -> { switch (indexMenu) { … } } }`. Hai nhánh `case 114, 115, 117, 118, 119, 120` và `case 127` **là mã map**, nhưng bị đặt trong `switch (indexMenu)` của map 52. Vì `indexMenu` chỉ nhận các hằng `ConstNpc` (`BASE_MENU = 31072002`, `GO_UPSTAIRS_MENU = 10000`, `MENU_OPEN_MMB_* = 1001–1004`, `BUA_HO_TRO = 1005`, `BINH_HUT_NANG_LUONG = 1006`) nên **không bao giờ** bằng 114…120 hay 127 → hai nhánh này là mã chết. Hệ quả: đứng ở tầng Mabư bấm "Xuống Tầng dưới" không có phản ứng; menu phù hộ ở map 127 cũng chết.

**Trước (rút gọn):**

```java
switch (mapId) {
    ...
    case 52 -> {
        switch (indexMenu) {
            case ConstNpc.MENU_OPEN_MMB_WITH_JAR -> { ... }
            case ConstNpc.MENU_OPEN_MMB_NO_JAR -> { ... }
            case ConstNpc.MENU_NOT_OPEN_MMB_WITH_JAR -> { ... }
            case ConstNpc.BINH_HUT_NANG_LUONG -> { ... }

            case 114, 115, 117, 118, 119, 120 -> { ... }   // <-- mã map, không bao giờ khớp
            case 127 -> { ... }                            // <-- mã map, không bao giờ khớp
        }
    }
    case 165 -> { ... }
}
```

**Sau (rút gọn) — `Osin.java:189–261`:**

```java
switch (mapId) {
    ...
    case 52 -> {
        switch (indexMenu) {
            case ConstNpc.MENU_OPEN_MMB_WITH_JAR -> { ... }
            case ConstNpc.MENU_OPEN_MMB_NO_JAR -> { ... }
            case ConstNpc.MENU_NOT_OPEN_MMB_WITH_JAR -> { ... }
            case ConstNpc.BINH_HUT_NANG_LUONG -> { ... }
        }
    }

    // FIX: 114..120 và 127 là MÃ MAP nhưng trước đây bị lồng trong switch(indexMenu) của map 52,
    // nên menu Ôsin ở các tầng Mabư không bao giờ chạy (không xuống được tầng dưới).
    // Nay đưa ra thành nhánh của switch(mapId) và kiểm tra indexMenu đúng menu đã mở.
    case 114, 115, 117, 118, 119, 120 -> {
        if (indexMenu == ConstNpc.GO_UPSTAIRS_MENU) {
            if (player.cFlag != 9) {
                return;
            }
            switch (select) { case 0 -> ...; case 1 -> ...; case 2 -> ...; }
        }
    }

    case 127 -> {
        if (player.idMark.isBaseMenu()) {
            switch (select) { case 0 -> ...; case 1 -> ...; case 2 -> ...; }
        }
    }

    case 165 -> { ... }
}
```

**Lý do chọn điều kiện lọc:**
- `openBaseMenu` với map 114…120 mở menu bằng `ConstNpc.GO_UPSTAIRS_MENU` (`Osin.java:93`) → kiểm `indexMenu == ConstNpc.GO_UPSTAIRS_MENU`.
- `openBaseMenu` với map 127 mở bằng `ConstNpc.BASE_MENU` (`Osin.java:104`) → kiểm `player.idMark.isBaseMenu()`, giống các nhánh 50/154/155 đang dùng.
- **Nội dung thân từng `case` giữ nguyên 100%**, chỉ thụt lề lại. Logic chỉ số `select` (0 = hướng dẫn, 1 = giải trừ phép thuật *hoặc* xuống tầng nếu đã giải trừ, 2 = xuống tầng) không đổi, khớp với thứ tự mục menu mà `openBaseMenu` dựng.

**Rủi ro:**
- Map **116** (Thánh địa Kaio trong dải Mabư) không nằm trong danh sách — giữ nguyên như code cũ, đúng ý đồ gốc (map 116 không phải tầng đánh).
- Nhánh 114…120 nay **thật sự chạy** → cần xác nhận `map.mapIdNextMabu()` trả đúng map kế tiếp, vì trước đây chưa bao giờ được gọi từ đường này.

**Cách test:**
1. Vào phó bản Mabư 12h (`52 → 114`), đánh quái tới khi `fightMabu.pointMabu >= POINT_MAX`.
2. Nói chuyện Ôsin ở tầng hiện tại → menu phải có mục "Xuống\nTầng dưới".
3. Chọn mục đó → phải nhảy sang map kế tiếp (114→115→117→…→120).
4. Ở map 120 mục này phải **không** hiện (điều kiện `mapId != 120`).
5. Thử cả 2 trạng thái `isUseGTPT` (đã/chưa giải trừ phép thuật) để chắc chỉ số `select` 1 và 2 đều đúng.
6. Ở map 127 nói chuyện Ôsin: mua phù hộ 10 ngọc, và chọn "Về Đại Hội Võ Thuật" → phải về map 52.

---

## 5. NPC Đại Thiên Sứ (id 64) menu rỗng

**File:** `SRC/src/nro/models/npc_list/DaiThienSu.java`. NPC được đăng ký ở `npc/NpcFactory.java:198–199` qua `ConstNpc.DAI_THIEN_SU = 64` (`consts/ConstNpc.java:142`).

**Trước:** cả `openBaseMenu` lẫn `confirmMenu` đều là thân rỗng → nhấn vào NPC không có gì xảy ra, người chơi bị kẹt hộp thoại chờ.

```java
@Override
public void openBaseMenu(Player player) {

}

@Override
public void confirmMenu(Player player, int select) {
    if (canOpenNpc(player)) {

    }
}
```

**Sau:**

```java
@Override
public void openBaseMenu(Player player) {
    // FIX: NPC Đại Thiên Sứ trước đây menu rỗng nên không mở được hội thoại
    // (tuyến nhiệm vụ mới dùng NPC này ở nhiệm vụ 45, 46 và đoạn kết)
    if (canOpenNpc(player)) {
        TaskService.gI().checkDoneTaskTalkNpc(player, this);
        this.createOtherMenu(player, ConstNpc.BASE_MENU,
                "Ta là Đại Thiên Sứ, người trông coi trật tự của các vũ trụ.\nNgươi cần gì ở ta?",
                "Từ chối");
    }
}

@Override
public void confirmMenu(Player player, int select) {
    // FIX: đóng hội thoại khi người chơi chọn "Từ chối"
    if (canOpenNpc(player)) {
        if (player.idMark.isBaseMenu() && select == 0) {
            Service.gI().hideWaitDialog(player);
        }
    }
}
```

Import thêm: `nro.models.consts.ConstNpc`, `nro.models.services.Service`, `nro.models.services.TaskService`.

**Lý do:** tuyến mới dùng NPC này ở NV 45, 46 và đoạn kết. `checkDoneTaskTalkNpc` được gọi trước khi dựng menu — đúng mẫu `Osin.openBaseMenu` (`Osin.java:29`) — nên khi bảng nhiệm vụ mới thêm bước "nói chuyện Đại Thiên Sứ" thì nó chạy ngay, không phải sửa lại lớp này.

**Rủi ro:** thấp. Menu chỉ có 1 mục "Từ chối"; chưa gắn chức năng nào. Khi làm NV 45/46 sẽ bổ sung mục và nhánh xử lý tương ứng.

**Cách test:** đứng cạnh NPC id 64, nhấn vào → phải hiện hộp thoại có lời chào và nút "Từ chối"; nhấn "Từ chối" → hộp thoại đóng, không treo. Đứng xa NPC nhấn vào → thông báo "Không thể thực hiện khi đứng quá xa" (do `canOpenNpc`).

---

## 6. Boss sát thương lớn spawn đè map đầu game

**File:** `SRC/src/nro/models/boss/BossesData.java` — chỉ sửa trường `mapJoin`, **không đụng HP / dame**.

### 6.1 Nhóm Bojack (7 khối `BossData`)

`BUJIN`, `KOGU`, `ZANGYA`, `BIDO`, `BOJACK`, `SUPER_BOJACK`, `SUPER_BOJACK_2` — dame **170.000 – 300.000**.

**Trước** (cả 7 khối):

```java
new int[]{3, 4, 5, 6, 27, 28, 29, 30}, //map join
```

**Sau:**

```java
// FIX: bỏ map đầu game (3 Rừng nấm, 4 Rừng xương, 5 Đảo Kamê, 6 Đông Karin,
// 27 Rừng Bamboo, 28 Rừng dương xỉ, 29 Nam Kamê, 30 Đảo Bulông) vì nhóm Bojack
// dame 170.000–300.000 giết người chơi mới 1 đòn; chuyển sang map cuối game (NV ≥ 24–27)
new int[]{97, 98, 99, 100, 105, 106, 107, 108, 109}, //map join
```

Map mới: 97 Thành phố phía bắc, 98 Ngọn núi phía bắc, 99 Thung lũng phía bắc, 100 Thị trấn Ginder (nhóm "Tương lai", yêu cầu NV ≥ 24) và 105–109 Hành tinh Cold (NV ≥ 27). Cố ý **bỏ map 110 Hang băng** vì đó là map của Cooler (`BossesData.COOLER`/`COOLER_2`), tránh hai boss lớn chồng chỗ.

### 6.2 Tiểu đội sát thủ Namek (5 khối `BossData`)

`SO_4_NM`, `SO_3_NM`, `SO_2_NM`, `SO_1_NM`, `TIEU_DOI_TRUONG_NM` — dame **10.000 – 15.000**.

**Trước** (cả 5 khối):

```java
new int[]{7, 8, 9, 10, 11, 12, 13, 25, 34, 33, 43}, //map join
```

**Sau:**

```java
// FIX: bỏ map đầu game Namếc (7, 8, 9, 10, 11, 12, 13 Đảo Guru, 25 Trạm tàu vũ trụ,
// 33 Nam Guru, 34 Đông Nam Guru, 43 Vách núi Moori) vì tiểu đội dame 10.000–15.000
// giết người chơi đang làm nhiệm vụ đầu 1 đòn; chuyển sang khu Fide/Nappa (NV ≥ 19)
new int[]{73, 74, 75, 76, 77}, //map join
```

Map mới: 73 Thung lũng chết, 74 Đồi cây Fide, 75 Khe núi tử thần, 76 Núi đá, 77 Rừng đá — đều yêu cầu **NV ≥ 19**, cùng vùng với `RAMBO` (dame 12.400, map 74–77) nên đúng mức sức mạnh. Cố ý **không** dùng 79/81/82/83 vì đó là chỗ của Tiểu đội sát thủ bản Trái Đất (`SO_1`…`TIEU_DOI_TRUONG`).

### 6.3 Lý do

Theo [21c §5.4](../3-kiem-tra-can-bang/21c-boss-roi-do-can-bang.md#54-boss-thế-giới-nằm-đè-lên-map-tuyến-chính-nguy-hiểm-chết-người): tuyến mới đưa NV 3 (map 43), NV 7 (map 25), NV 9–10 (map 5 và 13), NV 15 (map 27) vào đúng những map các boss này spawn. Người chơi ở mốc đó có **~800 – 12.899 HP**, bị một đòn là chết, và boss hồi sinh mỗi 5–15 phút.

### 6.4 Rủi ro

- Boss chuyển sang map yêu cầu nhiệm vụ cao hơn → **người chơi thấp không còn gặp** (đúng ý đồ), nhưng cũng nghĩa là nguồn rơi đồ của nhóm này dịch lên cuối game. Việc chỉnh phần thưởng thuộc bước sau (21c §6).
- `getMapJoin()` (`Boss.java:290–294`) chọn ngẫu nhiên trong mảng rồi `MapService.getMapWithRandZone(mapId)`. Nếu một map trong danh sách mới không tồn tại hoặc bị `isMapOffline` thì `zone == null` → boss chuyển `RESPAWN` và thử lại; không crash. Tất cả 14 map mới đều là map thường (`type 0`) theo docs/14.
- **Chưa đổi** `mapJoin` của Broly / Super Broly (`5, 13, 20, 27–38`) dù 21c §5.4 có nhắc Super Broly one-shot — xem [§8](#8-điểm-chưa-chắc--việc-còn-lại).

### 6.5 Cách test

1. Dùng lệnh admin ép Bojack spawn nhiều lần, ghi map thực tế → phải luôn thuộc {97, 98, 99, 100, 105…109}.
2. Tạo nhân vật mới, đi hết map 3, 4, 5, 6, 27–30 trong 30 phút → **không** gặp Bojack/Bujin/Kogu/Zangya/Bido.
3. Tương tự với Tiểu đội Namek: map 7–13, 25, 33, 34, 43 phải sạch; map 73–77 phải có.
4. Kiểm tra 4 đệ của Bojack (`AppearType.APPEAR_WITH_ANOTHER`) vẫn spawn **cùng map** với Bojack.

---

## 7. `Functions.sleep` làm đứng luồng Con đường rắn độc

**Bối cảnh:** `boss/Boss_Manager/SnakeWayManager.java:37` chạy **một luồng duy nhất** cập nhật boss CDRD của **mọi bang**, nhịp 150 ms. Mọi lời gọi `Functions.sleep` bên trong `afk()` / `attack()` của boss CDRD sẽ chặn luồng đó → toàn bộ boss CDRD của tất cả bang đứng yên trong suốt thời gian ngủ.

### 7.1 `SAIBAMEN.afk()`

**File / hàm:** `boss_con_duong_ran_doc/SAIBAMEN.java` → `afk()`, dòng 61–89.

**Trước:**

```java
if (this.idboss == 1) {
    Player pl = getPlayerAttack();
    if (pl == null || pl.isDie()) {
        return;
    }

    this.changeToTypePK();
    Functions.sleep(1500);
    this.changeStatus(BossStatus.ACTIVE);
} else if (...) {
```

**Sau:**

```java
if (this.idboss == 1) {
    // FIX: Functions.sleep(1500) ở đây làm đứng luồng SnakeWayManager dùng chung cho mọi bang.
    // Thay bằng mốc thời gian: lần cập nhật sau mới chuyển sang ACTIVE.
    if (this.timeChangeToActive == 0) {
        Player pl = getPlayerAttack();
        if (pl == null || pl.isDie()) {
            return;
        }
        this.changeToTypePK();
        this.timeChangeToActive = System.currentTimeMillis();
        return;
    }
    if (Util.canDoWithTime(this.timeChangeToActive, 1500)) {
        this.changeStatus(BossStatus.ACTIVE);
    }
} else if (...) {
```

Thêm trường (dòng 28–30):

```java
// FIX: mốc thời gian thay cho lời gọi ngủ trong afk()
private long timeChangeToActive;
```

Bỏ `import nro.models.utils.Functions;` (không còn dùng trong file).

**Ngữ nghĩa giữ nguyên:** vẫn "đổi sang PK → chờ 1,5 s → ACTIVE", chỉ khác là 1,5 s đo bằng đồng hồ chứ không ngủ. Mốc chỉ đặt **một lần** (`timeChangeToActive == 0`) nên không lặp `changeToTypePK()`.

### 7.2 `CADICH.attack()`

**File / hàm:** `boss_con_duong_ran_doc/CADICH.java` → `attack()` (dòng 142–…) và hàm mới `doneGongBienKhi()` (dòng 204–…).

**Trước:**

```java
@Override
public void attack() {
    if (!gongBienKhi && !this.effectSkill.isCharging && Util.canDoWithTime(this.lastTimeAttack, 100) && this.typePk == ConstPlayer.PK_ALL) {
        this.lastTimeAttack = System.currentTimeMillis();
        try {
            Player pl = getPlayerAttack();
            if (pl == null || pl.isDie()) { return; }
            if (this.nPoint.hp < this.nPoint.hpMax / 2 && !bienKhi) {
                this.chat("Ha ha ha, ha ha ha");
                this.bienKhi = true;
                this.gongBienKhi = true;
                EffectSkillService.gI().sendEffectMonkey(this);
                Functions.sleep(2000);
                this.chat("Thế nào " + pl.name + "? Mi đã thấy phép biến hình của người Xayda rồi chứ?");
                this.gongBienKhi = false;
                ... (bật isMonkey, nhân đôi hpMax, gửi lại hiệu ứng) ...
                return;
            }
            ...
```

**Sau:**

```java
@Override
public void attack() {
    // FIX: Functions.sleep(2000) trong lúc gồng biến khỉ làm đứng luồng SnakeWayManager dùng chung
    // cho mọi bang. Nay gồng được bấm giờ, lần cập nhật sau mới hoàn tất biến khỉ.
    if (this.gongBienKhi) {
        if (Util.canDoWithTime(this.timeGongBienKhi, 2000)) {
            doneGongBienKhi();
        }
        return;
    }
    if (!this.effectSkill.isCharging && Util.canDoWithTime(this.lastTimeAttack, 100) && this.typePk == ConstPlayer.PK_ALL) {
        this.lastTimeAttack = System.currentTimeMillis();
        try {
            Player pl = getPlayerAttack();
            if (pl == null || pl.isDie()) { return; }
            if (this.nPoint.hp < this.nPoint.hpMax / 2 && !bienKhi) {
                // FIX: chỉ bắt đầu gồng rồi trả luồng về, phần còn lại chạy ở doneGongBienKhi()
                this.chat("Ha ha ha, ha ha ha");
                this.bienKhi = true;
                this.gongBienKhi = true;
                this.timeGongBienKhi = System.currentTimeMillis();
                EffectSkillService.gI().sendEffectMonkey(this);
                return;
            }
            ...
}

// FIX: phần sau của pha biến khỉ, trước đây nằm sau Functions.sleep(2000) trong attack()
private void doneGongBienKhi() {
    Player pl = getPlayerAttack();
    this.chat("Thế nào " + (pl != null ? pl.name : "ngươi") + "? Mi đã thấy phép biến hình của người Xayda rồi chứ?");
    this.gongBienKhi = false;
    int timeMonkey = 100000;
    this.effectSkill.isMonkey = true;
    this.effectSkill.timeMonkey = timeMonkey;
    this.effectSkill.lastTimeUpMonkey = System.currentTimeMillis();
    this.effectSkill.levelMonkey = 1;
    long hpmax = (long) this.nPoint.hpMax * 2L;
    this.nPoint.hpMax = (int) Math.min(hpmax, 2_000_000_000);
    this.nPoint.setHp(((int) this.nPoint.hpMax));
    EffectSkillService.gI().sendEffectMonkey(this);
    Service.gI().Send_Caitrang(this);
    Service.gI().point(this);
    Service.gI().Send_Info_NV(this);
    Service.gI().sendInfoPlayerEatPea(this);
}
```

Thêm trường (dòng 31–32):

```java
// FIX: mốc thời gian thay cho lời gọi ngủ khi gồng biến khỉ
private long timeGongBienKhi;
```

Bỏ `import nro.models.utils.Functions;`.

**Ghi chú thiết kế:** điều kiện `gongBienKhi` được **đưa lên đầu** `attack()` thay vì nằm trong biểu thức `if` cũ, để lần cập nhật kế tiếp vẫn vào được hàm và hoàn tất pha biến khỉ (nếu để nguyên `!gongBienKhi && …` thì boss sẽ kẹt bất tử vĩnh viễn, vì `injured()` trả 0 khi `gongBienKhi == true`). `doneGongBienKhi()` lấy lại `pl` bằng `getPlayerAttack()` và có phòng `null` cho câu chat.

**Lý do chung:** bỏ 3,5 s ngủ/lượt khỏi luồng dùng chung → boss CDRD của mọi bang chạy mượt, đồng thời không còn nguy cơ nhiều bang đánh CDRD cùng lúc làm dồn độ trễ.

### 7.3 Rủi ro

- **CADICH:** trong lúc gồng, boss hoàn toàn không đánh và bất tử (`injured()` trả 0) — **giống hệt trước**, chỉ khác là luồng không bị chặn.
- `Boss.update()` chỉ gọi `active() → attack()` khi trạng thái là `ACTIVE`; nếu boss bị đổi trạng thái đúng lúc đang gồng thì `gongBienKhi` sẽ giữ `true` tới khi vào lại `ACTIVE`. Trường hợp này trước đây không xảy ra được (vì sleep chạy liền mạch) — cần theo dõi khi test.
- **SAIBAMEN:** nếu `getPlayerAttack()` luôn `null` (không ai trong khu) thì mốc không được đặt và boss ở yên `AFK` — đúng như hành vi cũ.

### 7.4 Cách test

1. Mở CDRD ở **hai bang khác nhau cùng lúc**, quan sát boss cả hai bên: trước khi sửa, nhóm còn lại khựng ~1,5–2 s; sau khi sửa phải chạy đều.
2. `SAIBAMEN` số 1: vào map 144 → boss phải chuyển PK rồi khoảng 1,5 s sau bắt đầu đánh. Rời map trước khi hết 1,5 s rồi quay lại → không được đánh ngay lập tức nhiều lần.
3. `CADICH`: đánh xuống dưới 50% HP → phải thấy hiệu ứng khỉ, ~2 s sau có câu chat "Thế nào … ?" và HP tối đa nhân đôi, sau đó boss đánh lại bình thường.
4. Trong 2 s gồng, đánh boss → sát thương hiển thị 0 (đúng như trước).

---

## 8. Điểm chưa chắc / việc còn lại

| Điểm | Ghi chú |
|---|---|
| **Chưa biên dịch** | Máy chưa cài xong JDK. Đã soát tay + kiểm tra cân bằng `{}` / `()` trên cả 13 file. Cần chạy `ant`/`javac` xác nhận. |
| **Drabura 2 — `ServerNotify`** | Sau khi sửa, boss đi qua `Boss.die()` nên phát thông báo toàn server. Nếu không muốn, cần override `die()` trong `Drabura2`. Chưa làm vì đây là hành vi chuẩn của mọi boss khác. |
| **Drabura 2 — `afk()` thành mã chết** | Giữ nguyên, vô hại. Có thể dọn ở đợt sau. |
| **Broly / Super Broly `mapJoin`** | 21c §5.4 có nêu Super Broly (dame tới 160.707) spawn ở map 5/13/20/27–38, nhưng yêu cầu đợt này chỉ liệt kê Bojack và Tiểu đội Namek nên **chưa đụng**. Đề nghị xử lý ở bước chỉnh cân bằng. |
| **Ăn Trộm / Ở Dơ / Sói hẹc quyn** | Cũng đè map đầu game theo 21c §5.4, ngoài phạm vi đợt này. |
| **Bảng `checkDoneTaskKillBoss`** | Vẫn là bảng của **tuyến cũ** (task 19–28). Các lời gọi vừa thêm chỉ có tác dụng thật khi bảng được viết lại theo đánh số NV mới (21c §7.2). |
| **Chỉ người kết liễu nhận thưởng** | Không sửa trong đợt này (21c §7.1 đề xuất bảng đóng góp sát thương). Đây vẫn là lỗi chặn lớn của tuyến mới. |
| **Map mới cho Bojack / TĐST Namek** | Chọn theo mức yêu cầu nhiệm vụ trong docs/14 (NV ≥ 19 / ≥ 24 / ≥ 27). Nếu chủ dự án muốn giữ boss ở đúng hành tinh cốt truyện (Bojack ở Trái Đất, TĐST Namek ở Namếc) thì cần bảng map khác — Namếc **không có** map cuối game nào nên tiểu đội Namek buộc phải dời sang khu Fide/Nappa. |
| **Osin map 116** | Không nằm trong danh sách `114, 115, 117, 118, 119, 120` — giữ nguyên như code cũ (map 116 là Thánh địa Kaio, không phải tầng đánh). |
