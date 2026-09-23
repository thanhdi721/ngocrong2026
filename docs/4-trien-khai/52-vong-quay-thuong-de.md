# 52 — Vòng quay Thượng Đế kiểu mới (2026-09-23)

Chạy kèm `SRC/sql/patch/58-vong-quay-thuong-de-moi.sql` và jar mới (`vsItem` = 22).

## Thay đổi

* Bỏ vòng quay thứ hai ("Vòng quay đặc biệt"), chỉ còn **một vòng quay**.
* Giá: **1 Thỏi vàng (vật phẩm 457) cho 1 lượt**. Bấm một lần quay tối đa 7 lượt (giới hạn của giao diện).
* Bỏ hẳn cách trả bằng vàng (250 triệu/lượt) và bằng ngọc.
* Phần thưởng vẫn rơi vào **rương phụ** như cũ, sức chứa 100 món.

## Bảng thưởng — `RewardService.rollLuckyRound()`

Tính theo phần vạn, tổng đúng 10.000 (100%). Sửa tỉ lệ thì đổi các hằng `RATE_*` ở đầu
`RewardService`, nhớ giữ tổng bằng 10.000.

| Phần thưởng | Tỉ lệ | Vật phẩm |
|---|---|---|
| Vàng 5.000–50.000 | 73,20% | 189 |
| Mảnh Đội trưởng Vàng | 1,88% | 956 |
| Đá nâng cấp ×1–5 | 0,42% | 220 lục bảo, 221 saphia, 222 ruby, 223 titan, 224 thạch anh |
| Ngọc Rồng 5–6 sao ×1–5 | 0,12% | 18, 19 |
| Mảnh thú cưỡi ×1–5 | 0,12% | 828–835 (khủng long, phi long…) |
| Nâng kỹ năng 2 / 3 / 4 / 5 đệ tử | 1,00% mỗi loại | 403, 404, 759, **2123 (mới)** |
| Cỏ bốn lá ×1–5 | 4,00% | 1635 |
| Cuồng nộ 2 / Bổ huyết 2 / Bổ khí 2 / Giáp Xên bọ hung 2 | 3,50% mỗi loại | 1150, 1152, 1151, 1153 |
| Bùa x2 tn, sm đệ tử | 2,26% | 1628 |

Đã mô phỏng 1 triệu lượt: sai lệch so với bảng dưới 0,05 điểm phần trăm.

## Mốc lượt quay — `LuckyRound.MOC_QUA`

Quà vào **rương phụ**, mỗi mốc nhận đúng một lần, tất cả đều **khoá giao dịch** (option 30).

| Mốc | Quà | Chỉ số |
|---|---|---|
| 1.000 | Xe xanh Chi Chi (1677) | 10% SĐ, 10% HP, 10% KI, 5% chí mạng |
| 3.000 | Bồ cào 9 răng (1699) | 10% SĐ, 10% HP, 10% KI, 5% chí mạng |
| 5.000 | Xe đỏ Bun ma (1678) | 20% SĐ, 20% HP, 20% KI, 10% chí mạng, 10% SĐ chí mạng |
| 5.000 | Thanh Long Yển Nguyệt đao (1502) | 20% SĐ, 20% HP, 20% KI, 10% chí mạng, 10% SĐ chí mạng |
| 10.000 | Cải trang Goku SSJ3 Hắc Kim (2075) | 40% SĐ, 60% HP, 60% KI, 15% chí mạng, 30% SĐ chí mạng |
| 12.000 | Cải trang Goku SSJ4 Huyết Hỏa (2076) | 45% SĐ, 70% HP, 70% KI, 25% chí mạng, 40% SĐ chí mạng |

Rương phụ đầy thì mốc chưa được ghi nhận, server nhắc dọn rương rồi trả lại ở lượt quay sau.

## Lưu số lượt

* Cột mới `player.vqtd`, dạng `"<số lượt>|<cờ mốc đã nhận>"`.
* `Manager.ensureSchema()` tự thêm cột lúc khởi động nếu thiếu, nên chạy jar trước patch cũng không
  hỏng phần lưu nhân vật.
* Đọc ở `MrBlue.loadPlayer`, ghi ở `PlayerDAO.updatePlayer`.

## Giao diện NPC Thượng Đế (map 45)

Menu "Quay ngọc May mắn" nay gồm: **Quay (1 thỏi vàng mỗi lượt)** · **Quay nhanh 10–50 lượt** ·
Rương phụ · Xóa hết trong rương · **Mốc quà** · Đóng.
Dòng đầu menu hiện tiến độ, ví dụ "Đã quay 320 lượt, còn 680 lượt tới mốc 1000".

## Quay nhanh

Giao diện vòng quay của client chỉ có 7 viên nên tối đa 7 lượt mỗi lần bấm. Nút **Quay nhanh** chạy
hẳn ở server, không qua giao diện đó: chọn **10 / 20 / 30 / 50 / 100 / 200 lượt**, trừ thỏi vàng một
lần, phần thưởng vào thẳng rương phụ.

Kết quả hiện **ngay trong menu** rồi hỏi luôn "Quay tiếp?", nên bấm quay liên tục không phải đóng mở
hộp thoại. Trước đây dùng hộp thoại `createTutorial`, client tách mỗi dòng thành một trang nên phải
bấm "Tiếp tục" nhiều lần. Bảng kết quả gộp 3 món mỗi dòng cho khỏi tràn khung: quay 200 lượt nhiều
nhất khoảng 11 dòng.

Sửa các mức lượt ở mảng `LuckyRound.QUAY_NHANH`.

Phần thưởng cùng loại được **gộp thành một chồng**, và chỉ gộp món chồng được, không mang chỉ số
(Bùa x2 đệ tử không chồng được nên để riêng). Mô phỏng 20.000 lần quay 50 lượt: trung bình chỉ tốn
8,8 ô rương phụ, nhiều nhất 17 ô; quay 100 lượt trung bình 12 ô, quay 200 lượt trung bình 16 ô và
nhiều nhất 26 ô trên tổng 100. Không đủ chỗ thì server báo trước và **không trừ thỏi vàng**.

## Vật phẩm mới

`2123 Nâng kỹ năng 5 đệ tử` (loại 27). Dùng sẽ nâng chiêu thứ 5 của đệ tử, xử lý trong
`UseItem.upSkillPet` giống các sách 402/403/404/759. Đệ tử chưa có chiêu 5 thì báo
"Không thể thực hiện", giống hành vi sẵn có của các sách cũ.

## Chưa kiểm tra được

Giao diện vòng quay của client khi trả bằng vật phẩm: server gửi kiểu thanh toán `USING_TICKET`
và giá 1. Nhánh này trước giờ chưa dùng bao giờ, nên cần vào game xem client hiện đúng "1 thỏi vàng"
hay không. Nếu hiện sai, chỗ cần sửa là `LuckyRound.openCrackBallUI`.
