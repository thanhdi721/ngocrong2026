# Tài liệu spec — Ngọc Rồng Online (Teamobi2026)

Bộ tài liệu mô tả toàn bộ chức năng, cơ chế và dữ liệu của server game trong `SRC/` và file `database team2026.sql`.
Mọi con số (công thức, tỉ lệ, giá, id) đều lấy từ code thực tế, có dẫn nguồn file Java. Cuối mỗi file có mục **"Ghi chú / điểm cần lưu ý"** liệt kê bug, code chết và chỗ code không khớp dữ liệu.

## Cấu trúc thư mục

```
docs/
├── README.md                  ← file này, mục lục chung
├── 1-he-thong-hien-tai/       22 file: mô tả game đang chạy (đọc code + DB thật)
├── 2-thiet-ke-nhiem-vu-moi/    4 file: tuyến nhiệm vụ mới "Vết Nứt Hư Không"
├── 3-kiem-tra-can-bang/        4 file: kiểm tra vật phẩm, item mới, cân bằng boss
├── 4-trien-khai/              nhật ký thi công: checklist, tạo ảnh, từng đợt sửa code
└── 5-tra-cuu/                 bảng tra cứu: tất cả boss trong game
```

> **Tra cứu boss:** [5-tra-cuu/34-tat-ca-boss.md](5-tra-cuu/34-tat-ca-boss.md) — 147 boss, đủ máu, sát thương, kỹ năng, map, thời gian hồi sinh, đồ rơi.

> **Bắt đầu từ đâu:** muốn biết còn thiếu gì để code và cần chốt những gì, đọc [4-trien-khai/22-san-sang-code.md](4-trien-khai/22-san-sang-code.md).

## 1. Hệ thống hiện tại

| # | File | Nội dung |
|---|------|----------|
| 00 | [00-tong-quan.md](1-he-thong-hien-tai/00-tong-quan.md) | Công nghệ, thư viện, cấu trúc package, luồng khởi động, build/chạy, `Config.properties`, bảo trì |
| 01 | [01-kien-truc-server-network.md](1-he-thong-hien-tai/01-kien-truc-server-network.md) | Network, gói tin, mã hoá, bảng cmd → hàm xử lý, login/tạo nhân vật, lưu dữ liệu, chống spam, Bot |
| 02 | [02-database.md](1-he-thong-hien-tai/02-database.md) | 41 bảng: cột, kiểu, ý nghĩa, index, code đọc/ghi, sơ đồ ER, cấu trúc JSON bảng `player`, `settings` |
| 02b | [02b-database-du-lieu-template.md](1-he-thong-hien-tai/02b-database-du-lieu-template.md) | Dữ liệu template: 2000 item, 251 option, 27 skill, 169 map, 119 quái, 93 NPC, nội tại, rada, shop |
| 03 | [03-nhan-vat-chi-so.md](1-he-thong-hien-tai/03-nhan-vat-chi-so.md) | 3 hành tinh, HP/KI/sức đánh/giáp/chí mạng, cộng tiềm năng, công thức sát thương, TNSM, bùa, PK, danh hiệu, nội tại |
| 04 | [04-ky-nang.md](1-he-thong-hien-tai/04-ky-nang.md) | 27 kỹ năng theo từng cấp, công thức, hiệu ứng đặc biệt, học/nâng skill, tuyệt kỹ |
| 05 | [05-de-tu-pet.md](1-he-thong-hien-tai/05-de-tu-pet.md) | Các loại đệ tử, chỉ số, AI, mở skill, Porata / hợp thể, linh thú |
| 06 | [06-vat-pham.md](1-he-thong-hien-tai/06-vat-pham.md) | Cấu trúc item, TYPE, option, hành trang/rương, set kích hoạt, đồ Thần Linh/Hủy Diệt/Thiên Sứ, từng item dùng được |
| 07 | [07-shop-giao-dich-ky-gui.md](1-he-thong-hien-tai/07-shop-giao-dich-ky-gui.md) | 32 shop / 58 tab / 826 item kèm giá, giao dịch người chơi, ký gửi, thẻ sưu tầm |
| 08 | [08-nang-cap-do-dap-do.md](1-he-thong-hien-tai/08-nang-cap-do-dap-do.md) | Ép sao, pha lê hoá, nâng cấp +1→+8, bông tai, nhập ngọc rồng, sách tuyệt kỹ, chế đồ Thiên Sứ — tỉ lệ & giá |
| 09 | [09-quai-roi-do-exp.md](1-he-thong-hien-tai/09-quai-roi-do-exp.md) | Quái, big boss map, hồi sinh, công thức EXP, bảng rơi đồ theo map |
| 10 | [10-rong-than.md](1-he-thong-hien-tai/10-rong-than.md) | Rồng thần 1/2/3 sao, Rồng Băng, Rồng Namếc, Ngọc rồng sao đen |
| 11 | [11-boss.md](1-he-thong-hien-tai/11-boss.md) | Cơ chế boss chung, lịch xuất hiện, từng boss thế giới (HP, dame, skill, map, thưởng) |
| 11b | [11b-boss-su-kien-pho-ban.md](1-he-thong-hien-tai/11b-boss-su-kien-pho-ban.md) | Boss phó bản, đại hội võ thuật, siêu hạng, nhân bản, boss sự kiện |
| 12 | [12-nhiem-vu-chinh.md](1-he-thong-hien-tai/12-nhiem-vu-chinh.md) | Cơ chế nhiệm vụ chính, 30 nhiệm vụ / 125 bước, phần thưởng, chức năng mở khoá |
| 13 | [13-nhiem-vu-phu-thanh-tich-danh-hieu.md](1-he-thong-hien-tai/13-nhiem-vu-phu-thanh-tich-danh-hieu.md) | Nhiệm vụ hằng ngày Bò Mộng, nhiệm vụ bang, 20 thành tích, danh hiệu |
| 14 | [14-ban-do-pho-ban.md](1-he-thong-hien-tai/14-ban-do-pho-ban.md) | Map/zone theo hành tinh, dịch chuyển, từng phó bản (điều kiện, giờ, thưởng) |
| 15 | [15-lenh-admin-gm.md](1-he-thong-hien-tai/15-lenh-admin-gm.md) | Lệnh admin (chat, menu, console), quyền admin |
| 16 | [16-su-kien-minigame-giai-dau.md](1-he-thong-hien-tai/16-su-kien-minigame-giai-dau.md) | Sự kiện, minigame, đại hội võ thuật, siêu hạng, giftcode, quà hằng ngày |
| 17 | [17-bang-hoi.md](1-he-thong-hien-tai/17-bang-hoi.md) | Bang hội: tạo, cấp, chức vụ, capsule bang, shop bang |
| 18 | [18-npc.md](1-he-thong-hien-tai/18-npc.md) | Từng NPC và menu, cây đậu thần |
| 19 | [19-vip-nap-tien-tien-te.md](1-he-thong-hien-tai/19-vip-nap-tien-tien-te.md) | VIP, nạp tiền, quy đổi, các loại tiền tệ |

## 2. Thiết kế tuyến nhiệm vụ mới (đang chờ duyệt)

| # | File | Nội dung |
|---|------|----------|
| 20 | [20-thiet-ke-nhiem-vu-moi.md](2-thiet-ke-nhiem-vu-moi/20-thiet-ke-nhiem-vu-moi.md) | **Bản thiết kế tổng** "Vết Nứt Hư Không": cốt truyện mới, 6 chương / 48 nhiệm vụ, bộ trigger, đường cong thưởng, nhánh lựa chọn, khóa map, kế hoạch triển khai |
| 20a | [20a-chi-tiet-nhiem-vu-00-15.md](2-thiet-ke-nhiem-vu-moi/20a-chi-tiet-nhiem-vu-00-15.md) | Chi tiết từng bước NV 0–15 (chương 1–2) |
| 20b | [20b-chi-tiet-nhiem-vu-16-31.md](2-thiet-ke-nhiem-vu-moi/20b-chi-tiet-nhiem-vu-16-31.md) | Chi tiết từng bước NV 16–31 (chương 3–4) + 2 nhánh |
| 20c | [20c-chi-tiet-nhiem-vu-32-47.md](2-thiet-ke-nhiem-vu-moi/20c-chi-tiet-nhiem-vu-32-47.md) | Chi tiết từng bước NV 32–47 (chương 5–6) + nhánh kết + boss Heart |
| **21** | [21-tong-hop-viec-can-sua.md](3-kiem-tra-can-bang/21-tong-hop-viec-can-sua.md) | **Đọc trước khi code**: danh sách việc phải sửa, xếp theo mức độ, và những điều cần chủ dự án quyết |
| 21a | [21a-kiem-toan-vat-pham-phan-thuong.md](3-kiem-tra-can-bang/21a-kiem-toan-vat-pham-phan-thuong.md) | Kiểm toán 100 vật phẩm thưởng: nguồn hiện có, ảnh hưởng khi phát free, quy đổi ra tiền |
| 21b | [21b-item-moi-dac-ta.md](3-kiem-tra-can-bang/21b-item-moi-dac-ta.md) | Đặc tả 32 item mới (đủ 15 cột), quy trình tạo item chuẩn, mẫu prompt dùng lại |
| 21c | [21c-boss-roi-do-can-bang.md](3-kiem-tra-can-bang/21c-boss-roi-do-can-bang.md) | Xếp hạng độ khó 38 boss, đối chiếu với đồ rơi và mốc nhiệm vụ, đề xuất chỉnh |

## Tra cứu nhanh theo chủ đề

- **HP / dame / công thức chỉ số** → 03, 04
- **Đập đồ, tỉ lệ nâng cấp** → 08
- **Boss** → 11, 11b
- **Vật phẩm / hộp quà / set kích hoạt** → 06
- **Rơi đồ** → 09
- **Nhiệm vụ** → 12, 13
- **VIP / nạp** → 19
- **Database** → 02, 02b

## Vấn đề nghiêm trọng nên xử lý trước

Tổng hợp từ mục "Ghi chú" các file (chi tiết và vị trí code xem trong file tương ứng):

| Mức | Vấn đề | File |
|-----|--------|------|
| Bảo mật | Mật khẩu tài khoản, mật khẩu ngân hàng, API key lưu plain text trong DB; không public file dump | 02, 19 |
| Bảo mật | Gói tin -30/64 mở menu ban/phát đệ tử không kiểm tra quyền admin; lệnh chat `part` ai cũng ghi đè file được | 15 |
| Kinh tế | Ký gửi: người mua trả vàng, người bán nhận thỏi vàng → tạo vàng vô hạn | 07 |
| Kinh tế | Mua VIP không lưu trừ VND vào DB → đăng nhập lại được hoàn tiền | 19 |
| Kinh tế | Rồng thần 2/3 sao đẩy vàng lên 200 tỷ; rồng 1 sao gán vàng = 2 tỷ thay vì cộng | 10 |
| Kinh tế | Con số may mắn trả thưởng nhân theo số vé; PVP không trừ tiền cược | 16 |
| Dữ liệu | Không có auto-save định kỳ — server crash mất tiến trình | 01 |
| Dữ liệu | `data_item_time` / `data_item_event` ghi và đọc lệch thứ tự | 02 |
| Chức năng | Ban người chơi ghi `ban = 0` nên không có tác dụng | 01, 15 |
| Chức năng | Luồng boss sự kiện và Rồng Băng không được khởi động | 11, 10 |
| Ổn định | `getZone()` lặp vô hạn khi mọi khu đầy; `getAllMaps()` đệ quy vô hạn | 14 |
| Hiển thị | Tỉ lệ pha lê hoá / cường hoá hiển thị khác tỉ lệ thực | 08 |

> Lưu ý: có 2 đoạn khoá IP (`ShopTab.loadItem()` và trong `TaskService.loadTask()`) tắt server nếu IP máy khác cấu hình. Hiện cả hai lời gọi đều đang bị comment, nhưng cần chú ý nếu bật lại.
