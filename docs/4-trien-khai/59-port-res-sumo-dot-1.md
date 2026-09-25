# 59 — Mang res từ SUMO/Bun về, đợt 1 (2026-09-25)

Làm theo thứ tự đã đề nghị ở [58-khao-sat-res-sumo-bun.md](58-khao-sat-res-sumo-bun.md):
cải trang → đeo lưng → thú cưỡi. Linh thú đi kèm luôn vì cùng một đường port.

## Đã mang về

| Loại | Số lượng | Vật phẩm | Part / flag_bag |
|---|---|---|---|
| Cải trang | **72** | 2124–2219 (chung với linh thú) | part 2370–2657 |
| Linh thú (type 27) | **24** | như trên | như trên |
| Đeo lưng (type 11) | **40** | 2220–2259 | flag_bag 157–196 |
| Thú cưỡi | **2** | 2260–2261 | `mount_53`, `mount_54` |

Ảnh: **2.380 icon mới** chép vào `data/icon/x1..x4` (9.520 file), cộng 16 file
`img_by_name` cho hai thú cưỡi. Bản x1 bên nguồn thiếu thì thu nhỏ từ x2.

Hai thú cưỡi bên SUMO đều tên "Thú Cưỡi cực vip" (trùng món mình đã có) nên đặt lại:
**Kỳ Lân Bạch Kim** (kỳ lân trắng vàng có cánh) và **Phượng Hoàng Thất Sắc**.

## Patch và công cụ

| File | Việc |
|---|---|
| `SRC/tools/port_item_from_sumo.py` | sinh patch 67 (cải trang + linh thú) |
| `SRC/tools/port_flagbag_from_sumo.py` | sinh patch 69 (đeo lưng) |
| `SRC/tools/sqlparse.py` | đọc INSERT trong file .sql của nguồn |
| `sql/patch/67-cai-trang-linh-thu-tu-sumo.sql` | 96 vật phẩm + 288 part + 72 head_avatar |
| `sql/patch/68-giftcode-res-sumo.sql` | `sumoct1..8` (cải trang), `sumolt1..3` (linh thú) |
| `sql/patch/69-deo-lung-tu-sumo.sql` | 40 flag_bag + 40 vật phẩm |
| `sql/patch/70-thu-cuoi-tu-sumo.sql` | 4 dòng img_by_name + 2 vật phẩm |
| `sql/patch/71-giftcode-deo-lung-thu-cuoi-sumo.sql` | `sumodl1..4`, `sumotc1` |
| `sql/patch/72-giftcode-res-sumo-dot-2.sql` | `sumovip` (10 món tuyển) + bộ `sumo2*` (bản sao để thử lần 2) |

Code: `DataGame.vsData` 28 → **29**, `vsItem` 23 → **24** (một lần cho cả bốn patch).

## Cách chọn icon (chỗ dễ hỏng nhất)

* Icon nào có ảnh **giống hệt** (md5) ảnh mình đang có thì dùng lại số cũ, không chép.
* Còn lại cấp số mới trong dải trống. "Trống" nghĩa là **vừa không có file, vừa không
  được nhắc tới trong bất kỳ dữ liệu nào** (part, item, head_avatar, flag_bag) — vì bên
  mình có hơn 200 icon được nhắc tới mà thiếu file; cấp trùng vào đó là đè lên đồ cũ.
  Trần 32.767 (`Short.parseShort`), sau đợt này còn trống khoảng 12.000 chỗ.
* Đeo lưng lọc trùng **theo ảnh chứ không theo tên**, nên món mình đã có dù đặt tên khác
  cũng không bị nhập lại (đã loại được 10 món kiểu đó).

## Đã kiểm tra

* Dựng DB nháp từ `database team2026.sql` rồi chạy **toàn bộ** patch 01→71 theo thứ tự:
  không còn câu nào lỗi.
* Part 2370–2657 liên tục, kiểu lặp đúng chu kỳ 0/1/2, số mảnh đúng 3/17/14.
* Vật phẩm 2124–2261 liên tục, không trùng tên món cũ.
* Mọi icon được nhắc tới đều có đủ file ở cả x1, x2, x3, x4; không icon nào vượt 32.767.
* Ghép thử mảnh đầu/thân/chân của 10 bộ ra ảnh xem: hình đúng, không lẫn mảnh.

Hai lỗi phát hiện nhờ bước chạy thử, đã sửa:

1. `69` ban đầu ghi `DELETE FROM item_template WHERE id BETWEEN 157 AND 196` (nhầm dải
   flag_bag sang bảng vật phẩm) — chạy thật là **xoá mất 40 món đồ gốc của game**.
2. Hai icon thú cưỡi thoạt đầu được cấp số 15199/15200 — hai số này đang được part 2017
   dùng nhưng thiếu file, cấp vào đó là đè lên quần của một bộ đồ cũ.

## Trước khi chạy trên server thật

1. Backup: `mysqldump -u root -p team2026 item_template part head_avatar flag_bag img_by_name giftcode > backup_truoc_67.sql`
2. Chạy phần "KIỂM TRA TRƯỚC" của patch 67: `item_max` phải là **2123**, `part_max` **2369**,
   và `so_dong` phải **bằng** `so_id` (không được hở id vật phẩm — `ItemService.getTemplate(id)`
   lấy theo chỉ số mảng, hở một id là mọi món phía sau lệch hết).
3. Tắt server → chạy 67, 68, 69, 70, 71 → bật server bằng jar mới.
4. Nhập mã để thử — **33 mã, đủ cả 138 món**:
   * cải trang `sumoct1`–`sumoct8` · linh thú `sumolt1`–`sumolt3`
   * đeo lưng `sumodl1`–`sumodl4` · thú cưỡi `sumotc1`
   * 10 món tuyển: `sumovip`
   * bộ lặp lại (mỗi tài khoản chỉ nhập mỗi mã một lần, muốn thử lại thì dùng bộ này):
     `sumo2c1`–`sumo2c8`, `sumo2l1`–`sumo2l3`, `sumo2d1`–`sumo2d4`, `sumo2t1`

## Cần biết: gói vật phẩm sắp đầy

Dữ liệu vật phẩm gửi cho client đi trong **đúng hai gói tin**, mỗi gói tối đa 65.535 byte.
Sau đợt này: **2.218 món ≈ 121 KB**, chia hai gói ≈ 60,5 KB mỗi gói. Còn thừa khoảng
**9 KB ≈ 200 món** nữa là kịch trần. Muốn thêm nhiều nữa thì phải rút gọn `description`
của các món cũ (vài món đang dài 100–150 ký tự) hoặc sửa cách gửi.

## Chưa làm

* 3 map "Núi Hủy Diệt" (mục 5 trong kế hoạch).
* Pet đi theo (`pet_follow`) — chưa có cơ chế bên mình, cần thử nghiệm riêng.
* Hai hào quang bên nguồn: `aura_99` của SUMO thực ra là **bảng danh hiệu "ĐẠI GIA
  CHECKVAR"** của họ, không dùng được; `aura_79` bên Bun cắt khung không ra số chẵn
  (1406 chỉ chia hết cho 2 và 19) và thiếu bản x1 — để lại, không mang về.
* Đồ mới **chưa có chỉ số**: phát qua shop / giftcode / cpanel "Buff đồ" rồi thêm option.

## Chưa kiểm tra được

Chưa vào game: cần xem cải trang mặc lên có lệch mảnh không, linh thú đeo ô 7 có hiện,
đeo lưng có vẽ đúng khung, và hai thú cưỡi có bay đúng.
