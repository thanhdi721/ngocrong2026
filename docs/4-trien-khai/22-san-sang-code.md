# 22 — Đã đủ để bắt tay viết code chưa?

> Trả lời cho câu hỏi: tài liệu hiện có đã đủ chưa, còn thiếu gì, còn phải xác nhận gì, và những lỗi nào cần lưu ý.
> Liên quan: [21 — Tổng hợp việc cần sửa](../3-kiem-tra-can-bang/21-tong-hop-viec-can-sua.md) · [20 — Thiết kế tuyến mới](../2-thiet-ke-nhiem-vu-moi/20-thiet-ke-nhiem-vu-moi.md)

## Mục lục

1. [Trả lời ngắn](#1-trả-lời-ngắn)
2. [Những gì đã có, dùng được ngay](#2-những-gì-đã-có-dùng-được-ngay)
3. [Còn thiếu — môi trường](#3-còn-thiếu--môi-trường)
4. [Còn thiếu — quyết định của bạn](#4-còn-thiếu--quyết-định-của-bạn)
5. [Còn thiếu — dữ liệu và tài liệu phải làm trước](#5-còn-thiếu--dữ-liệu-và-tài-liệu-phải-làm-trước)
6. [Danh sách lỗi cần lưu ý](#6-danh-sách-lỗi-cần-lưu-ý)
7. [Thứ tự thi công đề xuất](#7-thứ-tự-thi-công-đề-xuất)
8. [Bảng xác nhận — bạn điền vào đây](#8-bảng-xác-nhận--bạn-điền-vào-đây)

---

## 0. QUYẾT ĐỊNH ĐÃ CHỐT (18/09/2026) — code bám theo bảng này

| # | Vấn đề | Chốt |
|---|---|---|
| 1 | Ngọc Rồng trong thưởng nhiệm vụ | **Bỏ**. Ngoài ra **giảm ngọc từ điều ước rồng thần xuống 2.000 mỗi lần** |
| 2 | Boss bản nhiệm vụ riêng (id −2100…−2199) | **Có** — chỉ rơi đồ nhiệm vụ, HP theo mốc nhiệm vụ |
| 3 | Vật phẩm nhiệm vụ | **Tạo mới**, id liền mạch từ 2000 |
| 4 | Bộ Thần Linh chương 6 | **Đổi thành mảnh**, gom đủ mới đổi được 1 món |
| 5 | Thời gian cày hết tuyến | **1 – 2 tháng** |
| 6 | Điểm rẽ nhánh | **Giữ cả 3** (NV 20/48, NV 31/49, NV 47/50) |
| 7 | Đệ tử ở NV 12 | **NPC sư phụ tặng 1 con** |
| 8 | Cân bằng boss sẵn có (nhóm D) | **Làm luôn** |
| 9 | Lỗi kinh tế cũ (ký gửi, VIP, rồng thần…) | **Làm luôn** |
| 10 | Quà bù khi reset người chơi | **Không có** |
| 11 | Boss nhiệm vụ rơi vàng | **Không** |

**Môi trường đã chốt:**

- Máy này **chỉ để code**. Build và test ở đây, xong bàn giao `SRC/` cho chủ dự án đưa lên server.
- Database thật là **`team2026`** (file `database team2026.sql` ở thư mục gốc). File `SRC/sql/nro1.sql` là dump cũ của database tên `a`, **không dùng**.
- SQL giao dưới dạng **file patch riêng**, chủ dự án tự import.
- Không có quà bù, không cần thông báo trước cho người chơi.

**Vật phẩm mới — id và ảnh đã chốt:**

| Item id | Tên | icon_id | Ghi chú |
|---|---|---|---|
| 2000 | Lõi Hư Không | 20000 | Ảnh do chủ dự án tạo |
| 2001 | Vỏ Lõi rỗng | 20001 | |
| 2002 → 2008 | Mảnh Ký Ức #1 → #7 | 20002 → 20008 | |
| 2009 trở đi | Các vật phẩm nhiệm vụ còn lại | mượn icon sẵn có | Đánh số tiếp, **không được hở id** |

Ảnh đã chuẩn hóa đúng bội số x1/x2/x3/x4 và chép vào `SRC/data/icon/`. Bản gốc lưu ở `assets-moi/icon-goc-cua-ban/`.

## 1. Trả lời ngắn

**Phần thiết kế: đủ.** Cốt truyện, 48 nhiệm vụ, hơn 210 bước, lời thoại, phần thưởng, id map/quái/NPC/boss — đều đã có và đã đối chiếu với code thật.

**Nhưng chưa thể bắt đầu viết code**, vì còn vướng 3 nhóm:

| Nhóm | Nội dung | Ai làm |
|---|---|---|
| Môi trường | Máy này **chưa cài Java**, nên không build và không chạy thử được. DB game cũng chưa nạp vào MySQL | Bạn (hoặc bạn cho tôi biết máy chạy server thật) |
| Quyết định | 11 câu chưa chốt, trong đó vài câu **đổi hẳn nội dung phần thưởng và cách đánh số vật phẩm** | Bạn |
| Sửa lỗi chặn | 6 nhóm lỗi khiến tuyến mới **không chạy nổi** dù code đúng | Tôi, sau khi bạn chốt |

Nói thẳng: nếu code ngay bây giờ thì sẽ phải viết lại kha khá, vì phần thưởng và id vật phẩm còn phụ thuộc quyết định của bạn.

## 2. Những gì đã có, dùng được ngay

| Hạng mục | Trạng thái |
|---|---|
| Tài liệu hệ thống hiện tại (22 file) | Xong — mọi công thức, id, tỉ lệ đều tra từ code và DB thật |
| Thiết kế tuyến nhiệm vụ mới (4 file) | Xong — 48 nhiệm vụ, hơn 210 bước, đủ lời thoại |
| Kiểm tra cân bằng (4 file) | Xong — 100 vật phẩm, 38 boss, 32 item mới |
| Danh sách trigger cần thêm | Xong — A1–A12 dùng lại, B1–B14 viết mới, đã chỉ rõ móc vào hàm nào |
| Bảng khóa map mới | Xong — khoảng 30 chỗ, đã có bảng đối chiếu cũ → mới |
| Đặc tả item mới | Xong — đủ 15 cột, kèm câu lệnh SQL mẫu |
| Kế hoạch reset người chơi | Có khung, chưa có file SQL thật |

## 3. Còn thiếu — môi trường

Đã kiểm tra trên máy này:

| Thứ | Trạng thái | Ảnh hưởng |
|---|---|---|
| **Java** | **Chưa cài** (`java -version` báo không tìm thấy) | Không build được `20.jar`, không chạy thử được. **Chặn cứng** |
| **Ant** | Chưa có | Dự án là NetBeans/Ant (`build.xml`, `nbproject/`). Có thể build tay bằng `javac` với `SRC/lib/*.jar`, nhưng có ant thì gọn hơn |
| **MySQL** | Có, đang chạy | Nhưng **chưa nạp database `team2026`** — cần import file dump để test |
| **Cách chạy server** | `run.bat` → `java -jar 20.jar` (Windows) | Cần biết server thật chạy Windows hay Linux. **Lưu ý**: có lỗi phân biệt hoa/thường ở tên thư mục `tile_set_Info`, chạy Linux/macOS sẽ lỗi |
| **Client** | Bản Unity trong thư mục `LÂU CỒ MOD`, dạng đã đóng gói | Thêm vật phẩm mới **bắt buộc tăng phiên bản bảng item** để client tải lại. Cần biết: ai phát hành client cho người chơi, có build lại được không |
| **Sao lưu DB** | Chưa có | Bắt buộc sao lưu trước khi chạy SQL reset |

**Cần bạn cho biết:** bạn sẽ build và chạy thử ở đâu — máy này, hay VPS riêng? Nếu ở đây thì cần cài Java (bản 17 trở lên) và nạp DB.

## 4. Còn thiếu — quyết định của bạn

Gom hết các câu còn treo từ những lượt trước:

| # | Câu hỏi | Khuyến nghị | Ảnh hưởng nếu chưa chốt |
|---|---|---|---|
| 1 | Ngọc Rồng có nằm trong thưởng nhiệm vụ không? | **giảm ngọc ước lại cho 2k mỗi lần ước thôi** — lỗ rò 30.000 ngọc | Phải sửa lại bảng thưởng 3 nhiệm vụ |
| 2 | Có dựng bản nhiệm vụ riêng cho 6 boss then chốt không? | **Có** | Quyết định cả kiến trúc boss, không làm sau được |
| 3 | Vật phẩm nhiệm vụ: tạo mới hay dùng đồ có sẵn? | **Tạo mới**, đánh số liền từ 2000 | Quyết định toàn bộ file SQL item |
| 4 | Bộ Thần Linh chương 6: giữ / gắn hạn 30 ngày / đổi thành mảnh? | **Đổi thành mảnh** | Sửa thưởng 5 nhiệm vụ |
| 5 | Cày bao lâu thì xong tuyến? 2 tuần / 1 tháng / 3 tháng | — | Quyết định toàn bộ mốc sức mạnh và số lượng quái | **1 tháng -3 tháng***
| 6 | Giữ đủ 3 điểm rẽ nhánh? | **Giữ** | Ảnh hưởng 3 task id phụ và bảng chuyển tiếp |
| 7 | Nhiệm vụ 12 cần đệ tử — cho NPC tặng 1 con? | **Có** | Không thì người chơi kẹt |
| 8 | Có sửa luôn cân bằng boss sẵn có (nhóm D) không? | Nên, làm sau cũng được | — |
| 9 | Có sửa luôn các lỗi kinh tế cũ không (ký gửi, VIP, rồng thần)? | **Nên sửa trước** — chúng phá server nặng hơn cả tuyến nhiệm vụ | — |
| 10 | Reset người chơi: có quà bù không, bù bao nhiêu? | Bù theo tiến độ cũ | Cần trước khi viết SQL migration | **không**
| 11 | Boss nhiệm vụ có rơi vàng không? (hiện vẫn rơi theo cơ chế cũ) | **Không**, để đúng nguyên tắc | Sửa bảng thưởng boss |

## 5. Còn thiếu — dữ liệu và tài liệu phải làm trước

Những thứ này tôi làm được, nhưng **phải chờ bạn chốt mục 4** vì nội dung phụ thuộc vào đó:

| # | Việc | Phụ thuộc |
|---|---|---|
| 1 | **Bảng đánh số vật phẩm cuối cùng** — khối liền từ 2000, không hở | Câu 1, 3, 4 |
| 2 | **Bảng thưởng cuối cùng** cho 48 nhiệm vụ + 3 nhánh | Câu 1, 4, 5 |
| 3 | **Schema + DAO bảng `task_main_reward`** | Câu 2 |
| 4 | **Bảng đối chiếu hằng số nhiệm vụ cũ → mới** cho khoảng 30 chỗ khóa map/tính năng | — (làm được ngay) |
| 5 | **File SQL**: dữ liệu nhiệm vụ, vật phẩm mới, migration reset người chơi | 1, 2, 3 |
| 6 | **Bảng chỉ số boss bản nhiệm vụ** (6 boss, id −2100…−2199) | Câu 2, 5 |
| 7 | **Kế hoạch test**: 48 nhiệm vụ × 3 hành tinh × 3 nhánh, ai test, test ở đâu | Môi trường |

## 6. Danh sách lỗi cần lưu ý

### 6.1 Chặn cứng tuyến mới — không sửa thì code đúng cũng vô dụng

| Lỗi | Hậu quả | Nguồn |
|---|---|---|
| **Id vật phẩm phải liên tục** — hệ thống lấy đồ theo vị trí, không theo id | Hở 1 id là cả bảng item lệch, client sai tên và icon | [21b](../3-kiem-tra-can-bang/21b-item-moi-dac-ta.md) |
| **23 id ≥ 2000 đã bị hardcode** trong code mà DB không có | Thêm item mới sẽ đánh thức code chết, phát nhầm đồ | 21b |
| **7 boss chết không báo cho hệ thống nhiệm vụ** (Cooler, Baby, Broly, Dr Lychee, Hatchiyack, bản sao, Drabura 2) | 7 bước nhiệm vụ không bao giờ xong | [21c](../3-kiem-tra-can-bang/21c-boss-roi-do-can-bang.md) |
| **Menu Ôsin không xuống được tầng Mabư**; **NPC Đại Thiên Sứ menu rỗng** | Kẹt nhiệm vụ 36, 45, 46 và cả đoạn kết | [20c](../2-thiet-ke-nhiem-vu-moi/20c-chi-tiet-nhiem-vu-32-47.md) |
| **Boss quá khó so với mốc nhiệm vụ**: Baby gấp 100 lần, Black Goku 34 lần; Mabư 14h và Baby giết người chơi trong 1 đòn | Chương 5–6 không ai qua nổi | 21c |
| **Boss mạnh spawn đè map chương 1–2** (Bojack ở Đảo Kamê…) | Tân thủ bị giết khi làm nhiệm vụ 3, 7, 9, 10, 15 | 21c |
| **Xên bọ hung 1 con/server, nghỉ 30 phút, chỉ người kết liễu được tính** | 200 người làm nhiệm vụ 30 phải xếp hàng ~100 giờ | 21c |

### 6.2 Lỗi liên quan trực tiếp tới vật phẩm và phần thưởng

| Lỗi | Hậu quả |
|---|---|
| **Giá bán 0 không chặn được bán** — code ép giá tối thiểu lên 1 vàng | Vật phẩm nhiệm vụ bán mất được |
| **Option "Không thể giao dịch" và "Không thể bán lại" không chặn bán** | Như trên |
| **Đá ngũ sắc là đồ chết** — pha lê hóa không ăn nguyên liệu, chỉ trừ vàng và ngọc | Phương án chống kẹt nhiệm vụ 26 vô hiệu |
| **Capsule Vàng không có code xử lý khi dùng** | Thưởng nhiệm vụ 23 vô nghĩa |
| **Sao pha lê đen cấp 2 cộng 0** | Thưởng vô nghĩa |
| **Bông tai cấp 3**: mảnh ghép ra nó không rơi ở đâu | Nhiệm vụ 39 phát món mà bình thường không ai có |
| **Đá nâng cấp 1074–1078 không dùng để đập đồ** (chỉ để chế đồ Thiên Sứ) | 461 viên thưởng sai hệ thống |
| **Loại vật phẩm chọn sai** trong thiết kế (loại 11 là đồ đeo lưng, loại 27 mặc định giao dịch được) | Item nhiệm vụ lỗi khi mặc, hoặc trao/bán được |
| **5 dòng shop bán đồ Thần Linh giá 100–555 vàng** ở 2 tab không mở được | Dữ liệu chết, nhưng nếu ai đó mở tab lên thì thành lỗ hổng |

### 6.3 Lỗi kinh tế sẵn có — không chặn tuyến mới nhưng phá server nặng hơn

| Lỗi | Hậu quả |
|---|---|
| **Ký gửi**: người mua trả vàng, người bán nhận thỏi vàng | Tạo vàng vô hạn | bạn phải chặn vụ này cần có sức mạnh đạt và nhiệm vụ tối thiểu và mở thành viên mới cho sài ký gửi thì giá bán 100 thì thực nhận về 80
| **Thỏi vàng bán qua gói tin sửa được 500 triệu** thay vì 37 triệu | Tạo vàng | sửa lại 37 triệu
| **Rồng thần 2/3 sao đẩy vàng lên 200 tỷ** | Phá kinh tế | giảm lại 5 tyr thôi
| **Mua VIP không lưu trừ tiền** | Đăng nhập lại là hoàn tiền | check và fix cái này
| **Con số may mắn nhân thưởng theo số vé** | Khai thác được |
| **PVP không trừ tiền cược lúc bắt đầu** | Khai thác được |
| **Mabư 12h là máy in đồ Thần Linh** (hồi sinh 60 giây vô hạn) | Lạm phát đồ xịn | giảm lại tỉ lệ 
| **Bò Mộng điểm danh 10.000 ngọc + 100 thỏi vàng mỗi ngày** | Phát quá tay | giảm lại 100 ngọc và 5tv

### 6.4 Lỗi ổn định và bảo mật

| Lỗi | Hậu quả |
|---|---|
| **Không tự lưu định kỳ** | Server sập là mất tiến trình người chơi — nguy hiểm nhất khi vừa mở tuyến mới | bạn xem thử còn cách nào khác không có thể 5p lưu 1 lần hay gì đó 
| **`getZone()` lặp vô hạn khi mọi khu đầy**, `getAllMaps()` đệ quy vô hạn | Treo server |
| **Con đường rắn độc gọi `sleep` trong luồng chung** | Đứng phó bản của mọi bang 1,5–2 giây |
| **Gói tin mở menu ban người chơi không kiểm tra quyền admin** | Ai cũng ban được người khác | chỉ admin mứoi làm được
| **Lệnh chat `part` cho bất kỳ ai ghi đè file trên server** | Lỗ hổng nghiêm trọng | check và fix lại
| **Ban người chơi ghi `ban = 0`** | Khóa tài khoản không có tác dụng | bị ban là không login được và thông báo
| **Mật khẩu, mã thẻ, API key lưu không mã hóa** | Lộ dữ liệu |
| **Dữ liệu nhân vật ghi/đọc lệch** (`data_item_time`, `data_item_event`) | Buff gán nhầm, item sự kiện reset |

## 7. Thứ tự thi công đề xuất

| GĐ | Việc | Điều kiện bắt đầu |
|---|---|---|
| 0 | Dựng môi trường: cài Java, nạp DB, build thử `20.jar`, **sao lưu DB** | Ngay |
| 1 | Sửa nhóm lỗi chặn ở 6.1 (trừ phần id vật phẩm) | Sau GĐ 0 |
| 2 | Chốt bảng đánh số vật phẩm + bảng thưởng cuối cùng | Sau khi bạn trả lời mục 4 |
| 3 | Viết SQL: vật phẩm mới, dữ liệu nhiệm vụ, bảng thưởng | Sau GĐ 2 |
| 4 | Sinh lại `ConstTask`, viết lại `TaskService`, thêm trigger B1–B14 | Sau GĐ 3 |
| 5 | Cập nhật khoảng 30 chỗ khóa map/tính năng | Cùng GĐ 4 |
| 6 | Boss bản nhiệm vụ + boss Heart | Sau GĐ 4 |
| 7 | Test 48 nhiệm vụ × 3 hành tinh × 3 nhánh | Sau GĐ 6 |
| 8 | Reset người chơi + thông báo + quà bù | Sau khi test xong |

Có thể làm GĐ 1 song song với việc bạn suy nghĩ mục 4 — nhóm lỗi đó cần sửa dù có tuyến mới hay không.

## 8. Bảng xác nhận — bạn điền vào đây

Trả lời theo số cho nhanh, không cần viết dài:

```
MÔI TRƯỜNG
[ ] Build và test ở đâu?        → máy này / VPS Windows / VPS Linux
[ ] Ai cập nhật client?          → tôi tự làm được / chưa có ai / không sửa được client
[ ] Đã sao lưu DB chưa?          → rồi / chưa

QUYẾT ĐỊNH
1.  Ngọc Rồng trong thưởng?              → bỏ 
2.  Dựng boss bản nhiệm vụ riêng?        → có 
3.  Vật phẩm nhiệm vụ?                   → dùng đồ có sẵn hoặc tạo mới khi cần thiết và phải list cần tạo và promt như nào để tạo chuẩn nhất
4.  Bộ Thần Linh?                        → đổi thành mảnh / giữ nguyên
5.  Cày bao lâu xong tuyến?              → 1 tháng - 2 tháng
6.  Giữ 3 điểm rẽ nhánh?                 → giữ / bớt còn 1 / bỏ hết
7.  NPC tặng đệ tử ở nhiệm vụ 12?        → có
8.  Sửa cân bằng boss sẵn có?            → làm luôn 
9.  Sửa lỗi kinh tế cũ?                  → làm luôn
10. Quà bù khi reset?                     →  không
11. Boss nhiệm vụ có rơi vàng?            → không
```
