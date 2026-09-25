# 57 — Đổi đồ xong không tính set kích hoạt (2026-09-25)

## Có thật, hai chỗ hở

**1. Bộ đếm set kích hoạt không được tính lại khi đổi đồ.**

`SetClothes.setup()` là hàm đếm xem đang mặc mấy món của từng set (songoku, kirin, cadic,
thầnVũTrụKaio…). Các hiệu ứng set đọc thẳng mấy biến đếm này: sát thương / chí mạng trong
`NPoint`, kỹ năng trong `SkillService`, đệ tử trong `MobMe`.

Trước sửa, `setup()` chỉ chạy ở:

* `UseItem.getItem` — đường **kéo-thả** đồ trong hành trang;
* lúc đăng nhập (`Controller`);
* vài chỗ của giải Siêu Hạng.

Nghĩa là mặc / tháo bằng nút **"Sử dụng"** (`UseItem.doItem → useItem`, dùng cho đeo lưng,
cải trang, thú cưỡi, linh thú…) thì bộ đếm **giữ nguyên số cũ** — vừa đổi đồ xong game vẫn
tính theo bộ đồ trước đó, tới khi có thứ khác gọi lại `setup()` (đổi map, đăng nhập lại).

**2. Hiệu ứng set chỉ gửi lúc vào map.**

`Service.sendEffPlayer` (vầng sáng của set 5 món) chỉ được gọi trong
`ChangeMapService.finishLoadMap`. Mặc đủ set xong phải đổi map mới thấy, mà tháo một món
ra thì vầng sáng vẫn còn nguyên tới lúc đổi map.

## Đã sửa

* `InventoryService.capNhatDoTrenNguoi(player)` (mới): đếm lại set của người chơi và của đệ tử,
  rồi gọi `Service.capNhatHieuUngSet`. Gọi ở **cả 6 đường đổi đồ trên người**: `itemBagToBody`,
  `itemBodyToBag`, `itemBagToPetBody`, `itemPetBodyToBag`, `itemBoxToBodyOrBag`, `itemBodyToBox`
  — tức mọi đường, kể cả "Sử dụng", mua ở shop hay nhận thưởng, đều tính lại.
  Đặt **trước** `Service.point` vì vài chỉ số (set Nappa, set Cađíc M) đọc thẳng bộ đếm.
* `Service.capNhatHieuUngSet(player)` (mới): gỡ 4 id hiệu ứng set (1200, 10277, 5017, 1202)
  rồi gắn lại theo bộ đồ hiện tại.

Không đụng gì tới chỉ số cộng thêm của từng món — phần đó `Service.point → calPoint` vẫn
tính như cũ và vốn đã chạy đúng.

## Còn một lỗi khác ngay cạnh, CHƯA sửa

`InventoryService.itemBodyToBox` (cởi đồ bỏ thẳng vào rương) viết ngược điều kiện:

```java
if (index < 0 || index >= player.inventory.itemsBody.size()) {   // phải là index >= 0 && index < size
```

Nên đường này hoặc văng lỗi hoặc không làm gì. Sửa một dòng là xong nhưng sẽ **mở lại một
đường thao tác lâu nay không dùng được**, nên để chủ server quyết.

## Chưa kiểm tra được

Chưa thử trong game. Cách thử: mặc đủ 5 món set kích hoạt bằng nút "Sử dụng" → xem chỉ số và
vầng sáng lên ngay chưa (không cần đấm, không cần đổi map); tháo một món → vầng sáng tắt ngay.
