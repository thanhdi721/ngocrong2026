# 55 — Aura 95 "Goku Purple" (2026-09-24)

Nguồn: `docs/aura Goku Purple/aura Goku Purple/x1..x4`.

## Đã làm

* Chép 8 file vào `SRC/data/img_by_name/x1..x4`. File gốc `aura_95_1 .png` **dư một dấu cách**
  trước `.png`, đã đổi thành `aura_95_1.png` khi chép (để nguyên là client xin không ra file).
* `SRC/sql/patch/62-aura-95-goku-purple.sql`: thêm 2 dòng `img_by_name`
  (`aura_95_0` = 12 khung, `aura_95_1` = 1). Đã chạy trên DB local.

## Số đo

| | x1 | x2 | x3 | x4 |
|---|---|---|---|---|
| aura_95_0 | 157×3048 | 314×6096 | 471×9144 | 628×12192 |
| một khung (12 khung) | 157×254 | 314×508 | 471×762 | 628×1016 |
| nặng | 845 KB | 2.7 MB | 5.4 MB | 8.7 MB |

Số khung dò bằng chu kỳ lặp của ảnh: cắt 12 thì mỗi khung ra đúng một hình trọn vẹn.
Nặng ngang `aura_77_0` (x2 2.6 MB) đang có sẵn, nên không phải chuyện mới.

`aura_95_1` chỉ là ảnh 1×1 rỗng — lớp phụ bỏ trống, giống `aura_94_0`, `aura_15_1`…

## Ai nhận được aura này

NPC **GoKu Nỗi Loạn** ở đảo Kamê bật/tắt cho người chơi — xem
[56-npc-goku-noi-loan.md](56-npc-goku-noi-loan.md).

Ngoài NPC đó, `Player.getAura()` chỉ đọc aura từ **thẻ rađa**, và chỉ với 6 thẻ ghi cứng
trong code (956, 1204, 1791, 1792, 1793, 1142), thẻ phải **cấp > 1**. Muốn aura 95 rơi vào
một thẻ nữa thì bỏ chú thích một dòng `UPDATE radar SET aura_id = 95 WHERE id = …` ở cuối
patch 62 — thẻ được chọn sẽ mất aura cũ của nó.

## Chưa kiểm tra được

Chưa xem trong game: cần soi lại hình có bị lệch khung / to quá so với nhân vật không
(một khung x1 cao 254 trong khi nhân vật cao ~50).
