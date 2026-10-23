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

## Thu nhỏ cho bằng cỡ Hắc Y Song Đao (patch 96)

Vào game thử thì chỉ Hắc Y Song Đao (ảnh chuẩn của game) vừa cỡ; Vương Lâm và 6 bộ x4 là ảnh HD nên to gần
gấp đôi. Công cụ `SRC/tools/thu_nho_cai_trang_docs.py`:

* Thu RIÊNG đầu / thân / chân cho bằng phần tương ứng của Hắc Y (mỗi bộ lệch tỉ lệ khác nhau, ví dụ Vương Lâm
  thân vừa nhưng đầu to gấp đôi). Hệ số ghi ở đầu patch 96.
* Căn lại từng khung theo chính Hắc Y; đầu nâng thêm: Tử Ma 2, Ngọc Long 2, Kim Lôi 1.
* Ảnh nhỏ mang số icon MỚI 20614–20830 (mỗi bộ 31 số liền: 2 đầu, 16 thân, 13 chân). Lý do: server gửi phiên bản
  ảnh nhỏ toàn 0 (`DataGame.sendSmallVersion`) nên client không biết ảnh đổi — giữ số cũ thì ai đã vào game vẫn
  thấy ảnh to trong cache. Ảnh to cũ đã xoá khỏi `data/icon`; icon vật phẩm và avatar giữ nguyên.
* Patch 94 đã sửa theo (chạy lại 94 vẫn ra bản nhỏ). Thư mục docs ghi đè bằng bản x4 đã thu nhỏ, tên theo số mới
  (bản gốc HD còn ở `~/Downloads/x4`, `~/Downloads/vương lâm (tao)`).
* `vsData` 34 → 35.
* KHÔNG chạy lại tool này lần nữa: nó đọc ảnh đang có trong `data/icon` nên sẽ thu nhỏ thêm một lần.

Chạy: build jar (vsData 35), chạy patch 96, khởi động lại server.

## Sửa lại: cao bằng Hắc Y, giữ tỉ lệ gốc (patch 97)

Bản patch 96 thu riêng đầu / thân / chân nên vào game trông nhỏ và lệch tỉ lệ. Chủ dự án chốt: mỗi bộ thu ĐỀU
cả người một hệ số, cho chiều cao dáng đứng (đầu + thân + chân) bằng Hắc Y Song Đao, không tăng giảm riêng phần nào.

* `SRC/tools/thu_nho_cai_trang_docs.py` viết lại theo cách này. Hệ số: Vương Lâm 0.57, Kiếm Sĩ 0.78, Huyết Nhẫn 0.63,
  Lam Hồn 0.51, Tử Ma 0.60, Ngọc Long 0.60, Kim Lôi 0.54.
* dx/dy thu quanh điểm chân nhân vật: `d' = s*(CI + d) - CI` (CI dáng đứng: đầu -13,34 / thân -9,16 / chân -8,10),
  nên cách ghép đầu - thân - chân giữ y bản gốc.
* Tool luôn đọc ảnh HD gốc + dx/dy gốc từ commit `2027d8c02`, nên chạy lại vẫn ra cùng kết quả (mục "KHÔNG chạy lại"
  ở trên không còn đúng).
* Số icon mới 20831–21047. Dải 20257–20830 coi như đã dùng (client có thể còn cache ảnh cũ ở các số đó), tool không
  cấp lại. Ảnh của bản 96 (20614–20830) đã xoá.
* Patch 94 và 96 sửa theo, chạy thứ tự nào cũng ra kết quả của 97. `vsData` 35 → 36.

Chạy: build jar (vsData 36), chạy patch 97, khởi động lại server.

## Sửa lần 3: to thêm 15%, chạm đất (patch 98)

Bản 97 cao bằng Hắc Y nhưng chiều cao tính cả tóc dựng + hào quang nên người vẫn nhỏ; lại có bộ lơ lửng (Kiếm Sĩ)
hoặc lún dưới đất (Lam Hồn, Ngọc Long, Kim Lôi) vì dx/dy gốc căn theo bộ VIP, đáy chân mỗi bộ một khác.

* Hệ số = cỡ cao bằng Hắc Y × 1.15 (`PHONG` trong tool): Vương Lâm 0.66, Kiếm Sĩ 0.89, Huyết Nhẫn 0.72, Lam Hồn 0.59,
  Tử Ma 0.69, Ngọc Long 0.69, Kim Lôi 0.62. Vẫn thu đều cả người, giữ tỉ lệ gốc.
* Dời CẢ BỘ (mọi mảnh, mọi khung) cho đáy phần đặc của khung chân đứng chạm đất như Hắc Y; Huyết Nhẫn, Lam Hồn hạ thêm
  1 (`CHINH_DAT`) vì có hào quang dưới bàn chân. Đã soi phóng to từng đôi chân.
* Số icon mới 21048–21202, 21525–21586; dải 20257–21047 không cấp lại (client có thể còn cache).
* Patch 94, 96, 97 sửa theo, chạy thứ tự nào cũng ra kết quả của 98. `vsData` 36 → 37.

Chạy: build jar (vsData 37), chạy patch 98, khởi động lại server.

## Sửa lần 4: Vương Lâm, Kiếm Sĩ to thêm (patch 99)

Sau patch 98 hai bộ này nhỏ hơn 5 bộ còn lại: Vương Lâm thân hẹp (rộng 89 so với 118–150), tóc dài chiếm chiều cao;
Kiếm Sĩ có kiếm + sấm hồng tính vào chiều cao. `PHONG_RIENG` trong tool: Vương Lâm ×1.2 (hệ số 0.79), Kiếm Sĩ ×1.15
(hệ số 1.03), chân vẫn chạm đất.

* Tool giờ so kết quả với bản đang dùng: bộ nào không đổi thì giữ nguyên số icon, không ghi lại ảnh. Lần này chỉ
  2 bộ đổi, số icon mới 21587–21648; dải 20257–21586 không cấp lại.
* Patch 94, 96, 97, 98 sửa theo. `vsData` 37 → 38.

Chạy: build jar (vsData 38), chạy patch 99, khởi động lại server.

## Sửa lần 5: hạ đầu cho khỏi lơ lửng (patch 100)

Ở dáng đứng nhìn vẫn liền cổ nhưng vào game đầu còn lơ lửng. Đã ghép thử các khung 0..7 (đứng, chạy, nhảy) theo bảng
`CharInfo` của client (bảng `CHARINFO` trong tool; đã thử trên Hắc Y + Goku SSJ4 có dx/dy gốc thì ghép liền mạch, khung 8
trở đi chưa chắc nên không dùng) và đo khe giữa đáy đầu với mép trên thân (px x4, dương = hở):

| Bộ | trước (khung 0 / 7) | sau |
|---|---|---|
| Hắc Y Song Đao (gốc) | +2 / −31 | — |
| Kiếm Sĩ Hồng Lôi | **+15 / +22** | −9 / −2 |
| Tử Ma Chiến Thần | +4 / **+11** | −8 / −1 |
| Ngọc Long, Huyết Nhẫn, Kim Lôi | +1..+7 | ≤ 0 |

Tool tự hạ từng mảnh đầu đến khi hết hở ở mọi khung dùng mảnh đó: Kiếm Sĩ 6, Tử Ma 3, Ngọc Long 2 (+1 mảnh đầu thứ 2),
Huyết Nhẫn 1, Kim Lôi 1. Chỉ đổi dy của đầu nên ảnh và số icon giữ nguyên. Patch 94, 96–99 sửa theo. `vsData` 38 → 39.

Chạy: build jar (vsData 39), chạy patch 100, khởi động lại server.

## Sửa lần 6: chỉnh tay đầu theo chủ dự án (patch 101)

Chủ dự án xem hình ghép các khung rồi chỉnh (cả 2 mảnh đầu, mọi động tác), ghi ở `CHINH_DAU` trong tool, cộng sau bước
tự hạ đầu: Vương Lâm hạ 3; Kiếm Sĩ hạ 1, trái 1; Huyết Nhẫn lên 1, phải 1; Lam Hồn lên 1, phải 1; Tử Ma hạ 1.
Chỉ đổi dx/dy của đầu, ảnh và số icon giữ nguyên. Patch 94, 96–100 sửa theo. `vsData` 39 → 40.

Chạy: build jar (vsData 40), chạy patch 101, khởi động lại server.
