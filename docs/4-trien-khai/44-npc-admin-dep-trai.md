# 44 — NPC "ADMIN Đẹp Trai": đổi VND ra Thỏi vàng / Ngọc

> Yêu cầu của chủ dự án: người chơi không tìm được chỗ đổi tiền nạp (VND). Cần một NPC tên **"ADMIN Đẹp Trai"** đứng ở **nhà cả 3 hành tinh**, mặc ngoại hình cải trang **"CT Goku SSJ Blue"**.
> Đường dẫn Java viết tắt `models/` = `SRC/src/nro/models/`.
> Đã **biên dịch sạch** (JDK 17, 579 file) và **chạy thử SQL** trên DB tạm (dump → 01 → 02 → 04 → 09, chạy 09 hai lần, chạy khối lùi lại).

## 1. Nguyên nhân "mất chỗ đổi"

Hai form đổi VND đã có sẵn trong `models/services_func/Input.java`:

| Hàm bọc | Hằng form | Tỉ lệ |
|---|---|---|
| `createFormTradeGold(Player)` | `TRADE_GOLD` (5100) | 10.000đ = 40 Thỏi vàng (item 457) + 10 vé (item 718) + 50 điểm sự kiện |
| `createFormTradeGem(Player)` | `TRADE_GEM` (5101) | 10.000đ = 10.000 ngọc + 10 vé (item 718) + 50 điểm sự kiện |

- **Không NPC nào mở `TRADE_GOLD`**, nên đổi ra Thỏi vàng hoàn toàn không có lối vào.
- `TRADE_GEM` chỉ mở được qua Bò Mộng ở map **47 / 84** (mục "Nạp Ngọc"). Bò Mộng đứng ở nhà (map 21–23) thì **không có menu** (code `BoMong` chỉ xử lý map 47/84), nên người chơi ở nhà không thấy gì.

## 2. NPC mới

| Mục | Giá trị |
|---|---|
| Id `npc_template` | **85** (`ConstNpc.ADMIN_DEP_TRAI`) |
| Tên | ADMIN Đẹp Trai |
| head / body / leg | **1457 / 1459 / 1460** — lấy từ `item_template` 1590 "CT Goku SSJ Blue" |
| avatar | **13268** — tra `head_avatar` theo head 1457 (có sẵn, không cần phương án dự phòng) |
| Lớp Java | `models/npc_list/AdminDepTrai.java`, đăng ký trong `models/npc/NpcFactory.java` |

### Vì sao id 85 mà không phải 111

Đề bài gợi ý "id cao nhất đang là 110". Đọc code thì **không dùng được 111**:

1. `Manager` nạp `npc_template` vào **một `List`** (`NPC_TEMPLATES.add`), `NpcFactory.createNPC` lấy avatar bằng `NPC_TEMPLATES.get(tempId)` — tức **theo vị trí**, không theo id.
2. `DataGame.updateMap` gửi xuống client **toàn bộ danh sách theo thứ tự** (không gửi id), client cũng đánh chỉ số theo vị trí.
3. Id trong DB hiện liên tục **0..84**, rồi **nhảy cóc** sang 103..110. Nên id 111 sẽ nằm ở vị trí 93: `get(111)` văng `IndexOutOfBoundsException` lúc khởi động map, client vẽ sai hình.
4. `Zone` ghi `npc.tempId` bằng `writeByte`, `Manager` đọc bằng `getByte` / `Byte.parseByte` ⇒ id phải **≤ 127**.

⇒ Chọn **85** = ô trống đầu tiên, id = vị trí, ≤ 127. Tổng số template sau khi thêm là 94 (≤ 127, vừa với `writeByte(size)`).

**Hệ quả phụ (không gây hại):** 8 dòng 103..110 (Chú Bé Đần, Khá BảnH, Tiến Bry, Bulma Tết, Bill Bí Ngô, Heart, Bulma Bunny, Bunma Rực Rỡ) vốn đã lệch vị trí (đang ở 85..92) nên **chưa bao giờ đặt lên map được**; nay dời sang 86..93, vẫn không dùng được như trước. Không map nào, không dòng code nào tham chiếu các id đó. Muốn dùng chúng sau này thì phải đổi id cho liên tục (86, 87, …).

### Thay đổi phòng hờ trong `Manager`

- Câu nạp đổi thành `select * from npc_template order by id` (trước không có `ORDER BY`, thứ tự phụ thuộc engine).
- Khi nạp, nếu id ≠ vị trí thì in **một** cảnh báo: `npc_template: id hở từ id … trở đi`. Với DB hiện tại cảnh báo này sẽ in ra cho id 103 — đó là dữ liệu cũ nói ở trên, **không phải lỗi của NPC 85**.

## 3. Toạ độ đặt NPC

Kiểm bằng đúng thuật toán server: đọc `data/map/tile_map_data/<map>` như `Manager.readTileMap`, lấy tile "top" (loại 2) từ `data/map/tile_set_Info` như `readTileIndexTileType`, rồi áp `Map.yPhysicInTop`. Cả ba map rộng 32 ô (768 px), **nền phẳng y = 336 liên tục từ x = 24 đến x = 743**, `yPhysicInTop(x, 336) = 336` ⇒ NPC đứng đúng mặt đất.

| Map | Bộ ba thêm vào | NPC gần nhất | Cửa ra (waypoint) |
|---|---|---|---|
| 21 Nhà Gôhan | `[85,636,336]` | Bò Mộng x=573 (63 px), Quả trứng x=700 (64 px) | x 456–528 — không đè |
| 22 Nhà Moori | `[85,444,336]` | Đậu thần x=372 (72 px), Ông Moori x=516 (72 px) | x 168–240 — không đè |
| 23 Nhà Broly | `[85,636,336]` | Dưa hấu / Bò Mộng x=570 (66 px), Quả trứng x=700 (64 px) | x 432–504 — không đè |

Mọi khoảng cách > 60 px (bán kính `Map.getNpc`) nên không bấm nhầm NPC. **Toàn bộ NPC cũ giữ nguyên** (ông, rương, đậu thần, Bò Mộng, Dưa hấu, Quả trứng); bộ ba mới được **nối vào cuối** chuỗi `npcs`.

Map 21–23 là map nhà/offline: NPC nằm trong `map.npcs` chung, `NpcManager.getNpcsByMapPlayer` chỉ lọc Quả trứng / Dưa hấu / Ca Lích / Quốc Vương ⇒ NPC 85 **luôn hiện** ở cả ba nhà, với mọi hành tinh.

## 4. Menu

Câu chào: *"Chào <tên>! Ta là ADMIN Đẹp Trai — đẹp trai nhất 3 hành tinh. Có VND trong tài khoản thì ta đổi ra Thỏi vàng hoặc Ngọc cho, nhanh gọn lẹ! 10.000đ = 40 Thỏi vàng | 10.000đ = 10.000 Ngọc"*

| Nút | Làm gì |
|---|---|
| **Đổi VND ra Thỏi vàng** | đọc lại số dư từ DB rồi gọi `Input.gI().createFormTradeGold(player)` (form `TRADE_GOLD` có sẵn) |
| **Đổi VND ra Ngọc** | đọc lại số dư rồi gọi `Input.gI().createFormTradeGem(player)` (form `TRADE_GEM` có sẵn) |
| **Xem số dư** | đọc lại `vnd`, `tongnap` từ bảng `account`, hiện số dư, tổng nạp, trạng thái kích hoạt; menu con có sẵn 2 nút đổi |
| Đóng | — |

NPC **không tự trừ hay phát gì**; mọi điều kiện nằm trong `Input.doInput` (không nới lỏng):

- `TRADE_GOLD`: bắt buộc **đã kích hoạt tài khoản**; 10.000đ ≤ số tiền ≤ 5.000.000đ.
- `TRADE_GEM`: 10.000đ ≤ số tiền ≤ 5.000.000đ (code gốc **không** đòi kích hoạt — giữ nguyên, xem §7).

"Đọc lại số dư" (`PlayerDAO.reloadVnd`, hàm mới, chỉ SELECT) cần vì `session.vnd` chỉ nạp lúc đăng nhập: tiền nạp qua web khi đang online trước đây không hiện cho tới khi thoát vào lại.

## 5. Tỉ lệ đổi (không đổi so với code cũ)

| Nạp | Thỏi vàng | Ngọc | Vé 718 | Điểm sự kiện |
|---|---|---|---|---|
| 10.000đ | 40 | 10.000 | 10 | 50 |
| 100.000đ | 400 | 100.000 | 100 | 500 |
| 5.000.000đ (tối đa/lần) | 20.000 | 5.000.000 | 5.000 | 25.000 |

Công thức: Thỏi vàng = `(tiền / 1000) × 4`; ngọc = tiền; vé = `(tiền / 10000) × 10`; điểm = `(tiền / 10000) × 50`. Cả hai đường đổi đều cộng huy hiệu `DAI_GIA_MOI_NHU` và `EM_XINH_EM_DEP`.

## 6. Kiểm tra an toàn kinh tế

Đối chiếu với các lỗi VND đã sửa ở [24a](24a-sua-loi-kinh-te.md) (§6 Mua VIP). Kết quả:

| # | Kiểm tra | Trước | Sau |
|---|---|---|---|
| 1 | Trừ VND xuống DB **trước** rồi mới phát | Có gọi `PlayerDAO.subvnd` trước, **nhưng bỏ qua giá trị trả về** ⇒ DB lỗi / trừ thất bại **vẫn phát** Thỏi vàng / ngọc | `if (!PlayerDAO.subvnd(...)) { báo lỗi; break; }` — trừ thất bại thì **không phát** |
| 2 | Trừ nguyên tử, không âm | `subvnd` chạy `vnd = vnd - ?` không điều kiện, không xem số dòng ⇒ nếu `session.vnd` trong RAM cao hơn DB (vd. 2 phiên) thì DB bị trừ thành **âm** mà vẫn phát | `update account set vnd = vnd - ? where id = ? and vnd >= ?`, bắt buộc **đúng 1 dòng** bị ảnh hưởng; khoá `synchronized(session)`; từ chối `num <= 0` |
| 3 | Số âm | Chặn bởi `< 10000` | Giữ nguyên; thêm `num <= 0` trong `subvnd` (phòng mọi nơi gọi khác) |
| 4 | Số tràn | `Integer.parseInt` ném lỗi với số > 2.147.483.647 (bị `catch` bên ngoài nuốt, không phát gì); trần 5.000.000 | Giữ nguyên; `text[0].trim()` để khỏi lỗi vì dấu cách |
| 5 | Bấm liên tục / gửi lại gói để nhận 2 lần | Mỗi lần gửi đều trừ tiền riêng nên không "nhận 2 lần 1 khoản tiền"; nhưng `typeInput` **không bị xoá** sau khi xử lý ⇒ client tự chế gửi lại gói nhập bất cứ lúc nào, không cần đứng cạnh NPC | Đầu mỗi `case` gọi `player.idMark.setTypeInput(-1)`: **mỗi lần mở form chỉ gửi được 1 lần**. Hai lệnh song song cùng tài khoản: DB chỉ trừ được khi đủ tiền cho từng lệnh (#2) |
| 6 | Túi đầy mất đồ | `addItemBag` trả `false` khi hết ô ⇒ **đã trừ VND mà Thỏi vàng / vé biến mất** | Kiểm **trước khi trừ**: Thỏi vàng cần ≥ 2 ô trống (457 + 718), Ngọc cần ≥ 1 ô (718) |
| 7 | Tràn ngọc | `inventory.gem += quantity` (kiểu `int`) — gần 2,1 tỷ thì cộng thêm thành **số âm** | Từ chối nếu `gem + quantity > 2.000.000.000` (cùng trần với lệnh buff admin), kiểm trước khi trừ |
| 8 | Mất phần lẻ | `TRADE_GOLD` tính theo từng 1.000đ: nhập 15.500đ bị trừ 15.500đ nhưng chỉ đổi 15.000đ | `TRADE_GOLD` đòi số tiền là **bội số 1.000đ**. (`TRADE_GEM` đổi 1:1 nên không mất; vé / điểm sự kiện là quà làm tròn xuống như cũ) |

Các thay đổi #5–#8 là **siết chặt**, không nới điều kiện nào. File đã sửa: `models/services_func/Input.java` (hai `case TRADE_GOLD` / `TRADE_GEM`), `models/database/PlayerDAO.java` (`subvnd` viết lại, thêm `reloadVnd`). `subvnd` cũng được `ToriBot.BuyVip` dùng — hành vi với người chơi bình thường không đổi, chỉ chặt hơn khi DB không đủ tiền.

**Rủi ro còn lại:** chưa có transaction bao cả "trừ VND + phát đồ" — nếu server sập đúng giữa hai bước, người chơi mất VND mà không nhận đồ (xác suất rất thấp; đồ được lưu theo chu kỳ lưu `Player` bình thường). Giống ghi chú ở 24a §6.

## 7. Điểm cần chủ dự án quyết

1. **`TRADE_GEM` không đòi kích hoạt tài khoản** trong khi `TRADE_GOLD` có. Code gốc như vậy, đợt này giữ nguyên. Muốn thống nhất thì thêm khối `if (!player.getSession().actived)` như `TRADE_GOLD`.
2. Bò Mộng ở map 47/84 vẫn còn nút "Nạp Ngọc" mở cùng form `TRADE_GEM` — hai lối vào cùng một logic, không có hại.
3. Thông báo lỗi mới (tiếng Việt, có thể đổi chữ): "Không trừ được số dư, vui lòng thử lại sau!", "Hành trang cần ít nhất 2 ô trống" / "1 ô trống", "Số tiền phải là bội số của 1.000Đ", "Ngọc sau khi đổi vượt giới hạn 2 tỷ, hãy tiêu bớt trước".

## 8. Triển khai

1. **Tắt server.**
2. Sao lưu: `mysqldump -u root -p team2026 npc_template map_template > backup_09.sql`.
3. Chạy `SRC/sql/patch/09-npc-admin-dep-trai.sql` (sau 01, 02, 04 nếu chưa chạy). Chạy lại nhiều lần vẫn an toàn: `npc_template` dùng `ON DUPLICATE KEY UPDATE`, `npcs` chỉ nối khi chưa có `[85,`. Khối (1) kiểm tra trước, khối (3) kiểm tra sau (mọi cột `dat` = 1), khối (4) lùi lại (đang để comment).
4. Build & chạy bản code mới.
5. **Client phải tải lại dữ liệu map/NPC — tự động:** danh sách `npc_template` nằm trong gói vMap (`DataGame.updateMap`). Đã tăng **`DataGame.vsMap` 2 → 3**; khi đăng nhập client thấy số phiên bản khác bản đang cache sẽ tự xin tải lại. Người chơi không phải làm gì. **Không cần** thêm ảnh / tài nguyên client: head 1457, body 1459, leg 1460 và avatar 13268 đều là của cải trang có sẵn.
6. Kiểm tra log khởi động: `Successfully loaded npc template (94)` (+ một dòng cảnh báo id hở từ 103, xem §2).

### Test thủ công

1. `update account set vnd = 100000, active = 1 where username = '<tk>';`
2. Vào game, về nhà (Gôhan / Moori / Broly) — thấy NPC mặc Goku SSJ Blue tên "ADMIN Đẹp Trai".
3. **Xem số dư** → 100.000đ. Nạp thêm bằng SQL khi đang online (`vnd = vnd + 50000`), bấm lại → 150.000đ.
4. **Đổi VND ra Thỏi vàng**, nhập 20000 → +80 Thỏi vàng, +20 vé, +100 điểm; `account.vnd` trong DB giảm đúng 20.000.
5. Nhập 15500 → bị từ chối (bội số 1.000). Nhập -1 / 9999 / 6000000 → bị từ chối. Tài khoản chưa kích hoạt → "Vui lòng kích hoạt tài khoản!".
6. Làm đầy túi còn 1 ô → đổi Thỏi vàng bị từ chối, VND không đổi.
7. **Đổi VND ra Ngọc**, nhập 10000 → +10.000 ngọc, +10 vé.
8. Mở form đổi (số dư 100.000đ), **chưa bấm gửi**; dùng SQL đặt `account.vnd = 0`; rồi gửi 10000 → session vẫn nhớ số cũ nhưng DB không đủ ⇒ "Không trừ được số dư, vui lòng thử lại sau!", **không** nhận gì, `account.vnd` vẫn 0 (không âm).
9. Gửi xong một lần, dùng client tự chế gửi lại gói nhập → không có tác dụng (`typeInput` đã bị xoá).
