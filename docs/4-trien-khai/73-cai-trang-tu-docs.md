# 73 — 8 cải trang từ docs/goi_cai_trang, docs/vương lâm (tao), docs/x4 (2026-10-23)

Công cụ: `SRC/tools/port_cai_trang_tu_docs.py` (chạy bằng `/usr/bin/python3`, bản này có Pillow + numpy).
Patch: `SRC/sql/patch/94-cai-trang-tu-docs.sql` (vật phẩm + part + avatar), `95-giftcode-cai-trang-tu-docs.sql` (mã `ctdocs1`).
Jar: `DataGame.vsItem` 35 → 36, `vsData` 33 → 34.

| id | Tên | part đầu/thân/chân | icon vật phẩm | avatar | dải icon |
|---|---|---|---|---|---|
| 2290 | Cải trang Hắc Y Song Đao | 2661/2662/2663 | 20294 | 20295 | 20263–20295 |
| 2291 | Cải trang Vương Lâm | 2664/2665/2666 | 20350 | 20351 | 20319–20351 |
| 2292 | Cải trang Kiếm Sĩ Hồng Lôi | 2667/2668/2669 | 20383 | 20384 | 20352–20384 |
| 2293 | Cải trang Huyết Nhẫn Saiyan | 2670/2671/2672 | 20416 | 20417 | 20385–20417 |
| 2294 | Cải trang Lam Hồn Chiến Thần | 2673/2674/2675 | 20513 | 20514 | 20482–20514 |
| 2295 | Cải trang Tử Ma Chiến Thần | 2676/2677/2678 | 20546 | 20547 | 20515–20547 |
| 2296 | Cải trang Ngọc Long Chiến Thần | 2679/2680/2681 | 20579 | 20580 | 20548–20580 |
| 2297 | Cải trang Kim Lôi Chiến Thần | 2682/2683/2684 | 20612 | 20613 | 20581–20613 |

Số patch, id giftcode, vsItem / vsData do tool tự lấy số kế tiếp trên nhánh đang đứng (lần đầu làm trên `main`
ra 75/76 và part 2658 — trùng với Lò Luyện Đan của nhánh `nhiemvunew`, nên đã bỏ và chạy lại trên `nhiemvunew`).
Giftcode để `count_left = 99999`, không để -1 (xem patch 88).

## Đánh số lại icon

Mọi số icon bên nguồn (1412, 17719–17907, 18222–18252) đều đã có ảnh KHÁC trong `data/icon`, nên được cấp số mới,
mỗi bộ một dải liền (thứ tự trong dải: 2 khung đầu, 16 thân, 13 chân, icon vật phẩm, avatar).
File trong ba thư mục docs đã được đổi tên theo số mới; `goi.json` cũng ghi lại theo số mới.
Bản gốc chưa đổi tên vẫn còn ở `~/Downloads/x4`, `~/Downloads/vương lâm (tao)`, `~/Downloads/goi_cai_trang` (kèm .zip).

Trong `docs/vương lâm (tao)` còn 2 file `20275.png`, `20276.png` (hai icon ngôi sao) KHÔNG thuộc bộ cải trang nên
không mang vào game và giữ nguyên tên. Lưu ý: số 20275/20276 trong game giờ là 2 khung thân của Hắc Y Song Đao,
đừng chép hai file này vào `data/icon`.

## Căn dx/dy

* Hắc Y Song Đao: dùng dx/dy có sẵn trong `goi.json`; mảnh trong suốt 3000/3001/3002 đổi thành 2955.
  Icon vật phẩm gốc là 1412 (mặt một nhân vật khác, dùng chung) nên dựng icon riêng từ khung đầu.
* Vương Lâm và 6 bộ x4 không có dx/dy. Thân / chân căn TỪNG khung theo bộ Goku SSJ4 Huyết Hỏa (part 2221/2222,
  dx/dy gốc từ part.txt, cùng bộ dáng) bằng cách dò độ lệch để hai hình trùng khít nhất.
  Đầu căn theo Hắc Ảnh Sát Thần (part 2223) rồi nâng thêm (cột cuối của bảng `BO` trong tool): Huyết Nhẫn, Lam Hồn 4;
  Tử Ma, Ngọc Long, Kim Lôi 6 — client vẽ thân đè lên đầu nên đầu thấp sẽ bị che mặt.
* 5 bộ x4 sau không có icon / avatar: dựng từ khung đầu (icon 96px, avatar 256px ở x4).
* Ảnh nguồn là mức x4; x1..x3 thu nhỏ theo tỉ lệ 1/4, 2/4, 3/4.

Đã dựng hình kiểm tra dáng đứng của cả 8 bộ từ chính `data/icon` + part trong patch 94. Các khung đánh / bay chưa
được xem trong game; khung nào lệch thì chỉnh bằng patch như đợt 41–47.

## Chạy

1. Build jar với `DataGame.java` mới (vsItem 36, vsData 34).
2. Chạy patch 94 rồi 95 (trước khi chạy, hai câu SELECT đầu patch 94 phải ra 2289 và 2660).
3. Khởi động lại server, nhập mã `ctdocs1` ở NPC ADMIN Đẹp Trai (cần 8 ô trống).
