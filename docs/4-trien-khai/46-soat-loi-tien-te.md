# 46 — Soát lỗi tiền tệ toàn server (VND, Thỏi vàng, ngọc xanh, hồng ngọc, vàng)

> Yêu cầu chủ dự án: soát kỹ mọi lỗi liên quan đến tiền, ưu tiên VND, Thỏi vàng (item 457), ngọc xanh (`gem`), hồng ngọc (`ruby`) và vàng (`gold`).
> Đường dẫn viết tắt: `models/` = `SRC/src/nro/models/`. Số dòng là số dòng **sau khi sửa**. Mọi chỗ sửa trong code đều có comment `// FIX (46)` (hoặc `// FIX:` ngay cạnh).
> Không báo lại các lỗi đã sửa ở [24a](24a-sua-loi-kinh-te.md), [44](44-npc-admin-dep-trai.md), và 3 lỗi `BANSLL` / `FIND_PLAYER_GIFT_RUBY` / `TANG_NGOC_HONG` vừa sửa trong `Input.java`.
> **Đã biên dịch sạch** (JDK 17, 579 file).

## 0. Phạm vi đã soát

Tổng cộng khoảng **150 điểm vào** nơi người chơi gửi số hoặc chọn lựa lên server:

| Nhóm | Nội dung | Số điểm vào |
|---|---|---|
| Form nhập liệu | Mọi nhánh trong `services_func/Input.java` | 27 |
| Gói tin | `server/Controller.java`: shop 6/7, ký gửi -100, giao dịch -86, vòng quay -127, thách đấu -59, nâng cấp -81, nâng điểm -30/16, thành tựu -76, capsule -91, nhặt đồ -20, cây đậu 22, bang hội, radar… | ~55 |
| Nâng cấp | `combine/**` | 29 lớp |
| Menu NPC có tiền | `npc_list/**`, `npc/NpcFactory.java` | ~40 |
| Mua bán, ký gửi, giao dịch | `ShopService`, `ConsignShopService`, `Trade` / `TransactionService`, `BuyBackService` | — |
| Thưởng, minigame | Vòng quay, Chọn ai đây, Con số may mắn, PVP, rồng thần, giftcode, thành tựu | — |

Kiểu thông tin người chơi tự gửi lên dùng trong bài: **index** (vị trí món đồ trong túi), **select** (nút bấm trong menu), **count** (số lần quay), **point** (số điểm cộng).

**Kết quả: 29 lỗi.**

| Mức độ | Số lỗi | Đã sửa |
|---|---|---|
| Nghiêm trọng | 10 | 10 |
| Cao | 8 | 8 |
| Trung bình | 11 | 11 |

Còn **12 điểm cần chủ dự án quyết** (§3).

## 1. Bảng lỗi

Viết tắt: **NT** = Nghiêm trọng, **C** = Cao, **TB** = Trung bình.

### 1.1 Nghiêm trọng — tạo tiền hoặc nhân đồ không giới hạn

| # | Mức | File : dòng | Kịch bản khai thác | Hậu quả | Đã sửa |
|---|---|---|---|---|---|
| 1 | NT | `services_func/Trade.java` : 99–112, 426–432 | Nick A có 0 vàng mở giao dịch với nick B, gõ **10.000.000 vàng**, khoá, đồng ý. Server không bao giờ so số vàng đưa ra với ví thật: B nhận đủ 10 triệu, A xuống **−10 triệu**. Lặp lại bằng nhiều nick rác. | In vàng vô hạn | ✔ Kiểm tra đủ vàng lúc đặt và lúc chốt (`FAIL_NOT_ENOUGH_GOLD`) |
| 2 | NT | `services_func/Trade.java` : 374–424, 426–437 | Giao dịch chạy trên **bản sao túi đồ** lúc mở, xong thì **ghi đè** lên túi thật. Cách lợi dụng: mở form "bán Thỏi vàng" (BANSLL) trước, mời acc phụ giao dịch, gửi form bán 1.000 Thỏi vàng (+37 tỷ vàng), rồi hai bên khoá và đồng ý với túi rỗng. Túi bị ghi đè bằng bản sao **còn nguyên 1.000 Thỏi vàng**. Cách này dùng được với mọi đường tiêu đồ không bị chặn khi đang giao dịch: form -125, nâng cấp -81, nhặt đồ… | Nhân Thỏi vàng, nhân đồ | ✔ `sameBag`: lúc chốt, túi thật phải giống hệt ảnh chụp lúc mở, nếu không thì huỷ (`FAIL_BAG_CHANGED`) |
| 3 | NT | `services_func/LuckyRound.java` : 90–95, 120, 137 | Gửi gói **-127 [byte][count = 9]** mà không cần mở NPC, vì loại mặc định là vàng. `9 × 250.000.000` tràn `int` thành **−2.044.967.296**, qua được bước "đủ vàng", `gold -= số âm` thành **cộng ~2 tỷ vàng**, lại còn nhận 9 phần thưởng. `count` âm cũng cộng ngọc / vé. | Mỗi gói +2 tỷ vàng | ✔ Chặn `count` ngoài 1..7, tính giá bằng `long` |
| 4 | NT | `services/AchievementService.java` : 55–75 | Gửi lặp gói **-76 [select = 0]**. Server không gọi `canReward` (đã xong? đã nhận chưa?), cứ `gem += money` mỗi gói. | Ngọc xanh vô hạn | ✔ Bắt buộc `canReward`, chặn `select` ngoài phạm vi, khoá theo người chơi, chặn tràn |
| 5 | NT | `npc/MagicTree.java` : 195–199, 214–225, 249–253; `npc_list/DauThan.java` : 31–35 | Nâng cấp cây đậu cấp 9 (trả 300 triệu), chọn "Huỷ nâng cấp", rồi gửi lặp gói **22 [..][0]**. `unupgradeMagicTree` không kiểm tra `isUpgrade`, menu cũng không bị xoá, nên **mỗi gói hoàn lại 300 triệu**. Kể cả không nâng cấp: dùng ngọc rồng 4–7 sao ở nhà sẽ đặt `indexMenu` = 505 (trùng số với menu "Huỷ nâng cấp"). Tương tự, lặp gói "Nâng cấp nhanh" thì lên thẳng cấp 10 chỉ bằng ngọc. | Vàng vô hạn | ✔ Kiểm tra `isUpgrade` / `MAX_LEVEL`, xoá menu sau khi xác nhận |
| 6 | NT | `shop/ShopService.java` : 1226–1264 (`buyItemDaBan`), 432–437 (hiển thị) | Giá bán = `template.gold/4 × số lượng`, còn giá mua lại = `template.gold/2` **cố định**. Bán 10 "Hộp quà 5 sao" (item 736) được 500 triệu, mua lại cả chồng hết 100 triệu, **lời 400 triệu mỗi vòng**. "Máy dò Capsule" (379) cũng ăn được với chồng từ 3 cái. | Vàng vô hạn | ✔ Giá mua lại = 2 × giá đã bán. Món lẻ vẫn là `gold/2` như cũ. Kiểm tra túi trước khi trừ tiền |
| 7 | NT | `combine/CombineService.java` : 88–109, 184–245 | `showInfoCombine` chỉ lưu **tham chiếu** tới món đồ. Chọn nguyên liệu (-81), rồi **cất đá vào rương** / **mặc đồ lên người** / **giao dịch** (túi bị thay bằng bản sao), rồi bấm xác nhận. Lệnh trừ không tìm thấy món nên không trừ gì, nhưng vẫn ra thành phẩm. Ép sao, nhập ngọc rồng, phân rã, đánh bóng, tạo đá… đều **miễn phí nguyên liệu, lặp vô hạn**. Chỉ số lặp `[k, k, k, capsule]` còn đếm 1 chồng khoáng thành 3. | Nhân đồ / nguyên liệu | ✔ `nguyenLieuConHopLe`: lúc xác nhận món phải còn trong túi (so tham chiếu), không trùng, không đang giao dịch. Chặn chỉ số lặp / sai; xoá menu cũ ở mỗi lần xem |
| 8 | NT | `combine/ChuyenHoaTrangBi_Vang.java` / `_Ngoc.java` : 98–150 | Xem một cặp hợp lệ, rồi gửi lại -81 với `[X, X]`. Bước xem báo lỗi nhưng menu cũ vẫn còn. Xác nhận thì chuyển hoá **chính món X với chính nó**: chỉ số ×1,1^cấp, sao pha lê nhân đôi, lặp lại để tăng theo cấp số nhân. Ngoài ra trừ 2 tỷ vàng **không kiểm tra số dư** (vàng âm vẫn nhận đồ). | Đồ vô hạn, vàng âm | ✔ Kiểm tra lại toàn bộ điều kiện và số dư ở bước xác nhận, trừ đồ gốc trước khi thêm đồ mới |
| 9 | NT | `shop_ky_gui/ConsignShopService.java` : 120–124, 197, 234 | Mỗi phiên chạy trên một luồng riêng. Hai nick bấm "mua" cùng một món cùng lúc, hoặc người bán bấm "Huỷ" đúng lúc người khác mua: cả hai cùng thấy "chưa bán" và cùng nhận món. | Nhân đồ ký gửi | ✔ `synchronized` cho mua / huỷ / nhận tiền / đăng bán |
| 10 | NT | `services/shenron/SummonDragon.java` : 307–315, 491–502; `npc/NpcFactory.java` : 300 | Gọi rồng **3 sao**, mở cây đậu đầy hạt (`indexMenu` = 502 = `SHENRON_1_1`), gửi gói 32 `{RONG_THIENG, 0}` hai lần. Kết quả nhận điều ước **rồng 1 sao: +2 tỷ vàng**. Người khác đứng cạnh cũng chen vào đổi / xác nhận điều ước của người gọi. | +2 tỷ vàng mỗi lần gọi rồng rẻ | ✔ Chỉ người gọi rồng được chọn / xác nhận, và menu phải đúng loại rồng (`shenronStar`) |

### 1.2 Cao

| # | Mức | File : dòng | Kịch bản khai thác | Hậu quả | Đã sửa |
|---|---|---|---|---|---|
| 11 | C | `server/MenuController.java` : 47–60; `player/IDMark.java` : 21–28 | **Gốc của mọi lỗi "gửi lặp menu":** `indexMenu` không bao giờ bị xoá sau khi xác nhận. Gửi lại gói 32 `{npcId, select}` bao nhiêu lần cũng được xử lý lại (đổi quà, dịch chuyển, mua…). | Nhân mọi phần thưởng từ menu | ✔ `menuSeq`: xử lý xong mà không mở menu mới thì xoá menu. Mỗi menu chỉ xác nhận được 1 lần |
| 12 | C | `services_func/Input.java` : 106–110 | `typeInput` không bị xoá sau khi gửi form. Client tự chế gửi lại gói -125 của form cũ bất cứ lúc nào, xa NPC, sau nhiều giờ. | Dùng lại form bất kỳ | ✔ Xoá `typeInput` cho **mọi** form ngay khi nhận (các nhánh cần hỏi lại tự gọi `createForm`) |
| 13 | C | `player/NPoint.java` : 1904–1906 | Sức đánh gốc 21.000, gửi **-30 [16][type 2][short 1000]**. Giá tính bằng `int` nên 2,15 tỷ tràn thành **−2,14 tỷ**, trừ số âm thành **cộng ~2,1 tỷ tiềm năng** mà vẫn cộng 1.000 sức đánh. HP / KI cũng tràn khi chỉ số lớn. | Tiềm năng vô hạn | ✔ Tính `long`, `doUseTiemNang` từ chối giá ≤ 0 |
| 14 | C | `player/NPoint.java` : 1918–1946 | Giáp (type 3) và chí mạng (type 4) chỉ tính giá **1 điểm** nhưng cộng `+= point`. Gửi `point = 500` thì lên 500 giáp với giá của 1 điểm. | Chỉ số gần như miễn phí | ✔ Cộng dồn giá của từng điểm |
| 15 | C | `services_func/Trade.java` : 48–55, 346–373, 97–103 | (a) `accept++` không phân biệt ai gửi: **một người gửi ACCEPT 2 lần** là giao dịch chạy khi đối phương chưa đồng ý. (b) Không có trạng thái khoá: khoá với 10 triệu vàng cho đối phương xem, rồi **đổi thành 1 vàng** trước khi họ bấm đồng ý. | Lừa đảo giữa người chơi | ✔ Khoá / đồng ý tính theo từng người, bắt buộc cả hai khoá, cấm sửa sau khi khoá |
| 16 | C | `shop/ShopService.java` : 713–724 | Tiệm "Đổi Thưởng" (tab 49, shop QDDN) phát cải trang (CT Goku SSJ Blue…) **miễn phí**, không trừ giá 59–299 đã ghi trong `item_shop`. Hiện không NPC nào mở shop QDDN, nhưng bật lại là lộ ngay. | Đồ miễn phí | ✔ Trừ giá theo tiệm đặc biệt, kiểm tra túi |
| 17 | C | `shop_ky_gui/ConsignShopService.java` : 402–436 | Đăng ký gửi: server **không** gọi `itemCanConsign`. Client sửa gói -100 gửi chỉ số bất kỳ thì ký gửi được mọi món, kể cả món không được giao dịch (cải trang, đồ nhiệm vụ…), rồi dùng nick phụ mua lại để chuyển đồ. Ngoài ra thu 1 Thỏi vàng phí **trước** khi kiểm tra: chỉ số sai là mất phí. | Chuyển đồ cấm giao dịch | ✔ Kiểm tra món / giá / số lượng trước, thu phí sau |
| 18 | C | `npc/NpcFactory.java` : 572–596 | Gói "Hộp đựng quà" (99 Giấy màu): menu ghi **giá 2.000.000 vàng** nhưng code không trừ, và bấm **"Đóng"** cũng đổi. | Bỏ qua phí vàng | ✔ Chỉ nút "Đồng ý", trừ 2 triệu vàng, kiểm tra túi |

### 1.3 Trung bình

| # | Mức | File : dòng | Kịch bản / vấn đề | Hậu quả | Đã sửa |
|---|---|---|---|---|---|
| 19 | TB | `shop/ShopService.java` : 532–536 | `subMoneyByItemShop` kiểm tra **ngọc xanh** cho hàng giá hồng ngọc nhưng lại **trừ hồng ngọc**, nên hồng ngọc xuống âm. DB hiện không có hàng giá hồng ngọc (`type_sell = 3`), lỗi đang ngủ. | Hồng ngọc âm | ✔ |
| 20 | TB | `shop/ShopService.java` : 1015–1019 | Tiệm đặc biệt, hàng giá ngọc (icon item 77): **chỉ kiểm tra mà không trừ ngọc**. DB hiện chưa dùng icon này. | Hàng miễn phí | ✔ |
| 21 | TB | `shop/ShopService.java` : 493–497 | Tiệm tóc (SANTA_HEAD) nhận `tempId` bất kỳ, nên đổi được kiểu tóc của mọi món (đầu cải trang, đồ sự kiện…). | Ngoại hình trả phí thành miễn phí | ✔ Chỉ nhận món có trong tiệm |
| 22 | TB | `shop/ShopService.java` : 1286–1310 | Máy bán đồ Hủy Diệt (BILL): **trừ tiền + 99 thức ăn trước**, sau đó mới kiểm tra thức ăn / set thần. Thiếu điều kiện thì mất tiền mà không nhận đồ. | Người chơi mất tiền | ✔ Kiểm tra xong mới trừ |
| 23 | TB | `shop_ky_gui/ConsignShopService.java` : 139–143, 296–300, 324–331 | Mua khi túi đầy: trừ tiền nhưng đồ biến mất. Huỷ bán khi túi đầy: món bị xoá khỏi ký gửi mà không về túi. Nhận tiền ngọc: `gem` có thể tràn. | Người chơi mất tiền / đồ | ✔ |
| 24 | TB | `map/Zone.java` : 388–397 | Biến `picked` luôn là false lúc kiểm tra, nên `isPickedUp` không bao giờ được đặt. Hai gói nhặt cùng lúc (2 nick, hoặc nhặt tay + tự nhặt) đều nhận món. Cần căn thời gian chính xác. | Nhân vàng / ngọc / đồ rơi | ✔ Đặt `isPickedUp` ngay sau khi cộng vào túi |
| 25 | TB | `services_func/UseItem.java` : 1864–1871 | Viên Capsule (193) bị trừ lúc **mở** bảng chọn map, nhưng trạng thái `CHANGE_CAPSULE` còn nguyên. Sau đó gửi -91 [index] lúc nào cũng dịch chuyển miễn phí. | Dịch chuyển miễn phí | ✔ |
| 26 | TB | `npc/NpcFactory.java` : 697–711 | Dịch chuyển tới ngọc rồng Namek: `subGem(10)` **không kiểm tra số dư** (ngọc âm vẫn đi được). Người đang cầm ngọc bấm "Kết thúc" cũng bị trừ và dịch chuyển. | Ngọc âm | ✔ (giá giữ 10, xem §3) |
| 27 | TB | `minigame/ChonAiDay_Gold.java` : 17–18, 68–71, 89–92; `ChonAiDay_Gem.java` : 77–78, 106–107 | Tiền thưởng = `quỹ * 80 / 100` tính bằng `int`. Quỹ vượt ~27 triệu (27 lượt 1 triệu, hoặc 3 lượt VIP 10 triệu) thì tràn thành **âm**, người thắng **bị trừ** vàng. | Người chơi mất vàng | ✔ `long`, chặn trần |
| 28 | TB | `combine/PhanRaSach.java` : 61; `npc_list/BaHatMit.java` : 456–460 | Phân rã sách: kiểm tra 10 triệu vàng mà **không trừ**. Nâng cấp vật phẩm: nút **"Từ chối"** lại gọi thẳng `nangCapVatPham`, bỏ qua kiểm tra, nên người chơi vẫn mất vàng + đá. | Mất phí / người chơi thiệt | ✔ |
| 29 | TB | `services/GiftCodeService.java` : 52–61; `managers/GiftCodeManager.java` : 24 | Nhập giftcode có vàng / ngọc: trần cũ là **2 tỷ vàng / 200 triệu ngọc**, người đang có nhiều hơn bị **hạ xuống**. `countLeft` không đồng bộ nên nhập cùng lúc vượt số lượt. Ngoài ra `InventoryService.addItemList` (dòng 869) cộng dồn chồng Thỏi vàng không chặn tràn `int`. | Người chơi mất tiền, code vượt lượt | ✔ |

## 2. Ghi chú kỹ thuật về các chỗ sửa lớn

1. **Giao dịch (`Trade`).** Thêm `locked1/2`, `accepted1/2` và `acceptTrade(Player)`, bỏ `acceptTrade()`. `TransactionService` và `Bot/BotGiaoDich` đã đổi theo. Bot vẫn khoá rồi đồng ý như cũ.
   - Kèm theo đã sửa: giao dịch với **bot bán đồ** trước đây người chơi mất đồ mà không nhận gì (`!player2.isBot` đặt sai chỗ). Rương Gỗ (570) trước chỉ báo "không thể" mà vẫn cho vào.
   - **Test:**
     - Nick 0 vàng gõ 10 triệu → bị từ chối.
     - Mở form bán Thỏi vàng, rồi giao dịch, rồi bán, rồi đồng ý → "hành trang đã thay đổi".
     - Chưa khoá mà bấm đồng ý → "Cả hai bên cần khoá".
     - Khoá xong đổi số vàng → bị từ chối.
2. **Menu NPC một lần (`MenuController` + `IDMark.menuSeq`).** Lombok `@Data` không sinh `setIndexMenu` nữa vì đã viết tay, và `getMenuSeq` được sinh tự động. Menu mở lại chính nó (Chọn ai đây, Rương gỗ, danh sách điều ước…) vẫn chạy, vì các menu đó gọi lại `createMenu` nên `menuSeq` tăng. Gói 22 (cây đậu) không đi qua `MenuController`, nên xoá menu ngay trong `DauThan.confirmMenu`.
3. **Nâng cấp.** `startCombine` và `startCombineVip` từ chối nếu nguyên liệu không còn trong túi, bị trùng, hoặc người chơi đang giao dịch. Khi đó báo "Vật phẩm đã thay đổi, hãy chọn lại".
4. **Form nhập (`Input.doInput`).** Xoá `typeInput` ở đầu hàm cho mọi form. Các nhánh đã tự xoá từ trước (TRADE_GOLD, TRADE_GEM, BANSLL, tặng ngọc) vẫn giữ nguyên, không sửa. Hai lệnh buff admin (-2 / -3) chuyển sang `long` để chặn tràn.

## 3. Cần chủ dự án quyết

| # | Vấn đề | Hiện trạng sau đợt này | Đề xuất |
|---|---|---|---|
| 1 | **Chuyển hoá bằng Ngọc** (`ChuyenHoaTrangBi_Ngoc`): menu cũ ghi "Cần 5000 ngọc" nhưng code **trừ 2 tỷ vàng** | Giữ trừ 2 tỷ vàng (có kiểm tra số dư). Đổi chữ trong menu thành "Cần 2 tỷ vàng" cho khớp | Chốt giá thật: 5.000 ngọc hay 2 tỷ vàng? Nếu chọn ngọc thì thay `gold -=` bằng `gem` + kiểm tra |
| 2 | **Huỷ nâng cấp cây đậu**: menu ghi "hồi ½ vàng", code hoàn **100%** | Giữ 100% (đã hết khai thác được) | Hạ về 50% cho khớp chữ, hay sửa chữ? |
| 3 | **Dịch chuyển ngọc rồng Namek**: menu ghi "50 ngọc", code trừ **10** | Giữ 10 | Chốt 10 hay 50 |
| 4 | **Chọn ai đây – bản Ngọc**: thông báo ghi "hồng ngọc" nhưng cược và trả **ngọc xanh** | Chưa sửa chữ | Sửa chữ, hoặc đổi sang hồng ngọc thật |
| 5 | **Giá mua lại đồ đã bán** đổi công thức: 2 × giá bán thực (món lẻ không đổi). Món không có giá vàng vẫn là `số lượng × 100` | Đã áp dụng | Xác nhận công thức |
| 6 | **Tiệm tab 30** (BULMA_EVENT): 1 Phiếu giảm giá (459) đổi được **mọi** món trong tab, bất kể giá ghi 20–500 | Shop hiện không NPC nào mở, chưa sửa | Nếu bật lại, cần quy định giá |
| 7 | **Chế tạo đồ Thiên Sứ** (`CheTaoTrangBiThienSu`) chạy **ngay khi đặt đủ 4 món** vào ô, không có nút xác nhận. Người chơi dễ tốn 10 triệu vàng + 999 mảnh ngoài ý muốn | Chưa sửa (không phải lỗ hổng) | Chuyển sang có bước xác nhận như các kiểu nâng cấp khác |
| 8 | **Giao dịch khắt khe hơn**: bất kỳ thay đổi nào của túi đồ trong lúc giao dịch (kể cả tự nhặt đồ, tự ăn đậu) đều làm giao dịch thất bại | Đã áp dụng | Chấp nhận, hoặc mở rộng danh sách chặn hành động khi đang giao dịch |
| 9 | **Ký gửi chỉ nhận món giao diện cho phép** (`itemCanConsign`: đồ có option 86/87, loại 6/14/15, ngọc rồng 14–20) | Đã áp dụng | Muốn mở thêm loại nào thì sửa `itemCanConsign` |
| 10 | **Menu gắn với NPC**: client gửi được `select` tới **NPC khác** dùng trùng số `indexMenu` | **ĐÃ SỬA GỐC**: `IDMark.menuNpcId` được ghi ở cả 6 chỗ gửi menu (2 `NpcService`, `BaseMenu`, 3 trong `Npc`); `MenuController.doSelectMenu` bỏ qua lựa chọn nếu `npcId` khác NPC đã mở menu | Test: mọi menu NPC vẫn bấm được bình thường |
| 11 | **Dọn hậu quả**: các lỗi NT #1, #3, #4, #5, #6, #10 có thể đã bị khai thác | — | Rà DB: `player` có vàng gần 200 tỷ bất thường, vàng / ngọc **âm**, ngọc > 1 tỷ, Thỏi vàng > 1 triệu; đối chiếu `history_transaction` với nick 0 vàng mà giao dịch 10 triệu |
| 12 | **Ngoài phạm vi tiền, chỉ ghi chú**: item 726 (Mảnh giấy / Siêu thần thủy) không bị trừ khi dùng; vé KOL VIP 1825 chỉ kiểm tra có mà không trừ; `ToriBot.BuyVip` không kiểm tra `select` / ô trống; form `TANG_NGOC_HONG` không còn chỗ nào mở (`createFormTangRuby` không được gọi) | Chưa sửa | Xác nhận có phải cố ý không |

## 4. Đã soát, không thấy lỗi khai thác được

- **Thách đấu PVP** (`PVPService`, `ThachDau`): mức cược lấy từ bảng server, kiểm tra đủ vàng ở cả hai bên, bản sửa 24a §8 vẫn đúng.
- **Con số may mắn** (vàng / ngọc): số chọn nằm trong khoảng min..max, trả bằng Thỏi vàng từ túi, trừ trước khi ghi nhận.
- **Mua shop thường** (gói 6): giá và loại tiền lấy từ `item_shop` phía server, không nhận số lượng từ client. **Bán** (gói 7): Thỏi vàng buộc đi qua form, đồ có hạn và đồ nhiệm vụ bị chặn.
- **Ký gửi**: giá âm / 0 / vượt trần và số lượng ngoài 1..99 bị chặn (24a §2). Không mua được đồ của chính mình.
- **Bang hội** (xin / cho đậu, tạo bang, đổi cờ), **hồi sinh** (-16), **radar**, **nội tại**, **mã bảo vệ**, **cao thủ** (-118), **ToriBot** (VND): kiểm tra và trừ đúng loại tiền.
- **UseItem**: mọi món cho vàng / ngọc / đồ đều bị trừ trong cùng lần gọi. `ACCEPT_USE_ITEM` lấy lại món theo index lúc xác nhận.
- **Các lớp nâng cấp còn lại** (pha lê hoá, bông tai, sách, cường hoá lỗ): trừ đúng loại tiền, không hoàn nhiều hơn đã trả. Chi phí lớn nhất 2 tỷ, không tràn `int`.

## 5. Danh sách file đã sửa

`combine/CombineService.java`, `combine/ChuyenHoaTrangBi_Vang.java`, `combine/ChuyenHoaTrangBi_Ngoc.java`, `combine/PhanRaSach.java`, `managers/GiftCodeManager.java`, `map/Zone.java`, `minigame/ChonAiDay_Gold.java`, `minigame/ChonAiDay_Gem.java`, `npc/MagicTree.java`, `npc/NpcFactory.java`, `npc_list/BaHatMit.java`, `npc_list/DauThan.java`, `player/IDMark.java`, `player/NPoint.java`, `server/MenuController.java`, `services/AchievementService.java`, `services/GiftCodeService.java`, `services/InventoryService.java`, `services/shenron/SummonDragon.java`, `services_func/Input.java` (chỉ đầu `doInput` và nhánh buff admin), `services_func/LuckyRound.java`, `services_func/Trade.java`, `services_func/TransactionService.java`, `services_func/UseItem.java`, `shop/ShopService.java`, `shop_ky_gui/ConsignShopService.java`, `Bot/BotGiaoDich.java`.

**Không đụng**: `services/TaskService.java`, hai file `NpcService.java`, `cpanel`, `event/**`, `SRC/sql/**`, cùng 5 nhánh `BANSLL` / `FIND_PLAYER_GIFT_RUBY` / `TANG_NGOC_HONG` / `TRADE_GOLD` / `TRADE_GEM`.

## 6. Test thủ công gợi ý (sau khi build)

1. **Giao dịch**: làm các test ở §2.1. Giao dịch bình thường giữa 2 người (đồ + vàng) vẫn thành công.
2. **Vòng quay**: quay 1 lần và 7 lần bằng vàng / ngọc / vé bình thường. Gói count = 9 hoặc −1 thì không có tác dụng.
3. **Thành tựu**: nhận thưởng 1 lần; gửi lại lần 2 thì báo "Không thể nhận thưởng".
4. **Cây đậu**: nâng cấp, huỷ, được hoàn đúng 1 lần; gửi lại gói 22 không có tác dụng. Nâng cấp nhanh khi đang nâng cấp vẫn chạy.
5. **Mua lại đồ đã bán**: bán 10 Hộp quà 5 sao, danh sách "Mua lại" hiện giá 1 tỷ (2 × 500 triệu).
6. **Nâng cấp**: chọn đồ + đá, cất đá vào rương, bấm "Nâng cấp" → "Vật phẩm đã thay đổi". Luồng bình thường vẫn nâng cấp được. Chọn trùng một ô 2 lần thì không hiện menu.
7. **Menu NPC**: mua đồ / đổi quà ở các NPC như bình thường. Mở lại menu nhiều lần vẫn chạy.
8. **Nâng điểm**: cộng HP / KI / sức đánh +1 / +10 / +100 như cũ. Cộng giáp / chí mạng +1 như cũ.
9. **Rồng thần**: gọi rồng 1 / 2 / 3 sao và chọn điều ước bình thường. Người khác bấm vào rồng thì không làm gì được.

## 4. Chủ dự án đã chốt (2026-09-19) — đã áp dụng

| # | Quyết định | Code |
|---|---|---|
| 1 | Chuyển hoá bằng **Ngọc = 10.000 ngọc**, bằng **Vàng = 5 tỷ vàng** | `ChuyenHoaTrangBi_Ngoc` (`GEM_CHUYEN_HOA`, trừ `gem`), `ChuyenHoaTrangBi_Vang` (`long 5_000_000_000L`) — cả chữ menu lẫn phần trừ |
| 2 | Huỷ nâng cấp cây đậu hoàn **50%** | `MagicTree.getUnupgradeRefund()` dùng chung cho chữ menu và số hoàn (chữ cũ cấp 4 hiện "0 Tr" nay hiện "500 k") |
| 7 | Chế tạo đồ Thiên Sứ **có bước xác nhận** | `CheTaoTrangBiThienSu.showInfoCombine` chỉ hiện tỉ lệ + tiêu hao + nút "Chế tạo"; `cheTao()` kiểm tra lại toàn bộ rồi mới trừ. `CombineService.startCombine` gọi `cheTao` |
| — | Đổi VND ra Ngọc **cũng phải kích hoạt tài khoản** | `Input.TRADE_GEM`; câu trong "Xem số dư" của ADMIN Đẹp Trai cập nhật |
| 12 | Mảnh giấy 726 **bị trừ 1** khi vào Siêu Thần Thủy lượt mới (quay lại khu đang đánh dở không trừ) | `SuperDivineWaterService.joinMapThanhThuy` |
| 12 | Vé KOL VIP 1825 **bị trừ 1** mỗi lần nhận thưởng KOL VIP, trừ trước khi phát thưởng | `QuyLaoKame.handleKOLQuestRewardConfirm` |
