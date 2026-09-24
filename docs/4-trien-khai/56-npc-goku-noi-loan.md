# 56 — NPC "GoKu Nỗi Loạn" ở đảo Kamê (2026-09-24)

NPC đứng cạnh Chi Chi (map 5), chỉ làm một việc: **bật / tắt hào quang Goku Purple
(aura 97)** cho người chơi. Bật rồi thì đi đâu cũng còn, đăng xuất vào lại vẫn còn.

## Vị trí và ngoại hình

| | |
|---|---|
| npc_template | id **86**, tên "GoKu Nỗi Loạn" |
| ngoại hình | cải trang **Goku Nổi Loạn** (item 2103): head 2307 / body 2308 / leg 2309 |
| avatar khung chat | 17778 (head_avatar của 2307) |
| đứng ở | map 5 Đảo Kamê, **(310, 288)** — Chi Chi ở (240, 288), cách 70 px |

Id 86 vì client và `NpcFactory` tra npc_template **theo vị trí trong danh sách**, nên
id phải bằng vị trí: 0..85 đang liên tục (85 là ADMIN Đẹp Trai), 86 là ô trống kế tiếp.
Thêm dòng npc_template nên `DataGame.vsMap` 7 → 8, client mới tải lại danh sách NPC.

## Menu

Bấm vào NPC:

* chưa bật → "Bật hào quang" / "Đóng"
* đang bật → "Tắt hào quang" / "Đóng"

Miễn phí, không giới hạn số lần. Bật xong server làm hai việc: gửi gói `127/4`
(đúng gói hào quang của hệ thống thẻ rađa) rồi **cho vào lại đúng khu đang đứng**
— vào lại khu thì client dựng lại nhân vật nên chắc chắn thấy hào quang, kể cả khi
bản client không xử lý gói 127/4. Đổi lại là màn hình chớp một cái như khi đổi khu.

## Lưu ở đâu

Cột mới `player`.`aura_npc`: -1 là tắt, 95 là đang bật. Server tự thêm cột lúc khởi
động (`Manager.ensureSchema`), patch 63 cũng thêm cho chắc. `Player.getAura()` xét
cột này **trước** thẻ rađa, nên đang bật thì hào quang thẻ rađa tạm bị đè.

## Ba thứ NPC KHÔNG mang được

Gói tin NPC chỉ có `head`, `body`, `leg` (xem `DataGame` dòng ~156 và `Zone` dòng ~735):

* **không đeo được Thanh Long Yển Nguyệt đao** (đồ đeo lưng, item 1502);
* **bản thân NPC không có hào quang**;
* không có hiệu ứng set.

Muốn một nhân vật đứng đó vừa có kiếm vừa có hào quang thì phải dựng dạng "người chơi
giả" (`NonInteractiveNPC`, như Mr.PôPô / Trọng Tài) — nhưng người chơi bấm vào **không
ra menu**, vì menu chỉ mở được từ NPC thật (gói 33 gửi lên là npc id). Hai thứ này hiện
không ghép chung được, trừ khi chồng hai thực thể lên nhau (hình sẽ bị vẽ đôi, lệch pha).

## Cỡ hào quang

Bản gốc (aura 95) vào game to gấp mấy lần nhân vật, nên thu nhỏ dần:

| id | tỉ lệ | khung x1 | khung x4 | file x4 |
|---|---|---|---|---|
| 95 | gốc | 157×254 | 628×1016 | 8.7 MB |
| 96 | 50% | 78×127 | 314×508 | 2.8 MB |
| **97** | **25%** (đang dùng) | **39×64** | **157×254** | **864 KB** |

Đổi id chứ không ghi đè file cũ vì **client cache ảnh `img_by_name` theo tên** — máy nào tải
`aura_95_0` rồi thì ghi đè bên server vẫn hiện ảnh cũ. Muốn chỉnh cỡ lần nữa thì làm y vậy:
sinh ảnh mới thành `aura_98_0`, thêm dòng `img_by_name`, đổi `GokuNoiLoan.AURA_ID`.

## Danh hiệu

Đang bật hào quang thì **không hiện danh hiệu** nữa (`Player.autoSendBadges` bỏ phần gửi
hiệu ứng), vì hai thứ chồng lên đầu nhân vật rối mắt. Chỉ bỏ phần HIỆN — chỉ số mà danh hiệu
cộng vẫn giữ nguyên, vì chỉ số tính theo `dataBadges.isUse` chứ không theo hiệu ứng.
Tắt hào quang là danh hiệu hiện lại (chậm nhất 10 giây sau).

## Cần chạy

1. Patch 62 (ảnh hào quang 95 + dòng `img_by_name`) — nếu chưa chạy.
2. Patch 63 (`SRC/sql/patch/63-npc-goku-noi-loan.sql`), tắt server trước.
3. Patch 64 rồi 65 (`64-aura-96-nho-lai.sql`, `65-aura-97-nho-25-phan-tram.sql`) — hào quang bản nhỏ.
4. Bật lại server bằng jar mới (`vsMap` = 8).

## Chưa kiểm tra được

Chưa vào game: cần xem NPC đứng đúng chỗ (không lọt nền, không đè Chi Chi), bấm ra
menu, và hào quang 95 hiện lên người chơi đúng như hình.
