# 21 — Tổng hợp việc cần sửa trước khi mở tuyến nhiệm vụ mới

> Gộp kết quả 3 đợt kiểm tra: [21a vật phẩm phần thưởng](21a-kiem-toan-vat-pham-phan-thuong.md) · [21b đặc tả item mới](21b-item-moi-dac-ta.md) · [21c cân bằng boss & rơi đồ](21c-boss-roi-do-can-bang.md).
> Tất cả đều là **phát hiện, chưa sửa code**. Mỗi mục ghi mức độ và việc cụ thể.

## Mục lục

1. [Nhóm A — Chặn cứng: không sửa thì tuyến không chạy được](#nhóm-a--chặn-cứng)
2. [Nhóm B — Phá vỡ kinh tế: tuyến phát quá tay](#nhóm-b--phá-vỡ-kinh-tế)
3. [Nhóm C — Đồ chết: phát ra nhưng vô dụng](#nhóm-c--đồ-chết)
4. [Nhóm D — Cân bằng boss sẵn có (ngoài tuyến nhiệm vụ)](#nhóm-d--cân-bằng-boss-sẵn-có)
5. [Những điều cần bạn quyết](#5-những-điều-cần-bạn-quyết)

---

## Nhóm A — Chặn cứng

| # | Vấn đề | Hậu quả | Việc cần làm | Nguồn |
|---|---|---|---|---|
| A1 | **Id vật phẩm phải liên tục.** `ItemService.getTemplate(id)` lấy theo vị trí trong danh sách, `ItemData` gửi client theo thứ tự không kèm id | Thiếu 1 id là cả bảng item lệch một bậc: trả nhầm đồ, client sai tên/icon, lỗi tràn mảng | Đánh số lại 32 item mới thành **khối liền từ 2000**; thêm `ORDER BY id` khi nạp; tăng phiên bản bảng item để client tải lại | 21b |
| A2 | **23 id ≥ 2000 đã bị hardcode trong code** nhưng DB không có (di sản server khác) | Thêm item mới ở dải đó sẽ "đánh thức" code chết, phát nhầm vật phẩm nhiệm vụ | Rà và vá từng chỗ trước khi thêm item | 21b |
| A3 | **7 bước nhiệm vụ không thể hoàn thành**: Cooler, Baby, Broly, Dr Lychee, Hatchiyack, bản sao, Drabura 2 đều không gọi `checkDoneTaskKillBoss` khi chết | Người chơi giết boss xong nhiệm vụ vẫn đứng yên, kẹt vĩnh viễn | Thêm lời gọi ở từng `reward()` / `die()` | 21c |
| A4 | **Boss quá khó so với mốc sức mạnh của nhiệm vụ**: Baby gấp ~100 lần mức hợp lý, Black Goku ~34, Cumber ~20, Xên ~9, Cooler ~7. Mabư 14h và Baby còn **giết người chơi trong 1 đòn** | Chương 5–6 không ai qua nổi | Dựng **bản nhiệm vụ riêng** của 6 boss then chốt (id −2100…−2199), HP giảm mạnh, chỉ rơi đồ nhiệm vụ; giữ nguyên boss thế giới cho endgame | 21c |
| A5 | **Boss thế giới sát thương lớn đang spawn đè map chương 1–2** (Bojack ở Đảo Kamê, Tiểu đội sát thủ Namek ở map 13/25/43) | Tân thủ bị giết ngay khi làm nhiệm vụ 3, 7, 9, 10, 15 | Loại các map nhiệm vụ đầu game khỏi `mapJoin` của các boss này | 21c |
| A6 | **Xên bọ hung chỉ 1 con toàn server, nghỉ 30 phút, chỉ người kết liễu được tính** | 200 người làm nhiệm vụ 30 phải xếp hàng khoảng 100 giờ | Tăng số lượng boss, và tính hoàn thành cho mọi người gây ≥ 5% sát thương | 21c |

## Nhóm B — Phá vỡ kinh tế

| # | Vấn đề | Con số | Việc cần làm | Nguồn |
|---|---|---|---|---|
| B1 | Tuyến thưởng **3 bộ Ngọc Rồng** | Gọi Rồng Thần 3 lần → **30.000 ngọc**. Lỗ rò lớn nhất, lớn hơn cả tiền bán đồ | Bỏ Ngọc Rồng khỏi thưởng, hoặc chỉ cho 6 viên để không gọi được rồng | 21a |
| B2 | **Bán sạch đồ thưởng ra tiền** | ≈ **553 triệu vàng**; xấu nhất **2,4 tỷ** nếu dùng lỗi bán Thỏi vàng | Thay bằng vật phẩm không bán được; vá lỗi bán Thỏi vàng | 21a |
| B3 | **Đặt giá bán 0 không chặn được bán** — code ép giá tối thiểu lên 1 vàng. Option "Không thể giao dịch" và "Không thể bán lại" cũng không chặn bán | Vật phẩm nhiệm vụ vẫn bán mất được | Vá 3 chỗ: bán, vứt, mặc | 21a, 21b |
| B4 | **Phát trọn bộ 5 món Thần Linh** ở nhiệm vụ 40–44 | Thay thế khoảng **1.500 lượt giết boss**, mở sớm cả chuỗi Bill → Hủy Diệt → Thiên Sứ | Gắn hạn sử dụng 30 ngày, hoặc đổi thành "mảnh" phải gom đủ mới đổi được | 21a |
| B5 | **461 viên đá nâng cấp sai hệ thống** (1074–1078) | Đập đồ +1→+8 **không dùng loại đá này**, chúng chỉ để chế đồ Thiên Sứ | Đổi sang đá nâng cấp thật (220–224), liều lượng bám bảng ở `08` | 21a |
| B6 | **Bông tai cấp 3** phát ở nhiệm vụ 39 | Mảnh ghép ra nó **không rơi ở đâu trong game** — nhiệm vụ phát món bình thường không ai có | Bỏ, hoặc mở nguồn rơi cho mảnh trước | 21a |
| B7 | **Loại vật phẩm chọn sai**: loại 11 thực ra là đồ đeo lưng, loại 27 mặc định giao dịch được | Item nhiệm vụ lỗi khi mặc, hoặc đem bán/trao được | Đổi loại theo khuyến nghị ở 21b | 21b |

## Nhóm C — Đồ chết

Ba vật phẩm đang nằm trong bảng thưởng nhưng **phát ra cũng vô nghĩa**:

| Vật phẩm | Vì sao vô dụng | Hệ quả với thiết kế |
|---|---|---|
| **Đá ngũ sắc (674)** | Pha lê hóa **không ăn nguyên liệu**, chỉ trừ vàng + ngọc, tỉ lệ thật 50% | Phương án chống kẹt nhiệm vụ 26 (trao 6 Đá ngũ sắc) **không có tác dụng** — phải làm lại |
| **Capsule Vàng (574)** | Không có code xử lý khi dùng | Thưởng nhiệm vụ 23 vô nghĩa |
| **Sao pha lê đen cấp 2 (964)** | Không có option gốc, ép vào cộng 0 | Thưởng vô nghĩa |

## Nhóm D — Cân bằng boss sẵn có

Không bắt buộc cho tuyến mới, nhưng là **lỗi cân bằng đang tồn tại** trên server:

| Boss | Vấn đề | Đề xuất |
|---|---|---|
| **Nhóm Mabư 12h** | Thưởng gấp ~9,7 lần mức chuẩn, hồi sinh 60 giây vô hạn → máy in đồ Thần Linh | Giảm tỉ lệ rơi, tăng thời gian hồi sinh |
| **Cooler** | Dễ hơn Black Goku 5,7 lần nhưng bảng thưởng y hệt | Hạ thưởng hoặc tăng độ khó |
| **Siêu Bọ Hung** (form 1 tên "Xên Hoàn Thiện") | Rơi đồ cấp 30%, gấp 6 lần Cooler/Black Goku, gấp 30 lần Mabư | Đây chính là con bạn nói. Nên giữ nó là nguồn đồ xịn nhưng **tăng độ khó cho xứng**, và **không đưa vào bước nhiệm vụ bắt buộc** |
| **Xên bọ hung (−100)** | Ngược lại: 600 triệu HP mà chỉ rơi 25.000 vàng, bèo nhất game | Tăng thưởng cho tương xứng |
| **Tiểu đội sát thủ Namek, Bojack** | In ra Ngọc xanh (≈100 và ≈500 mỗi lượt) | Giảm mạnh số ngọc rơi |
| `BABY_3.dame = 30000` | Gần như chắc chắn thiếu một số 0 | Xác nhận lại |

**Nguyên tắc đề xuất, đúng ý bạn:** đồ xịn chỉ rơi từ boss khó và **nằm ngoài tuyến nhiệm vụ**; boss trong bước nhiệm vụ bắt buộc **chỉ rơi đồ nhiệm vụ**. Muốn đồ xịn thì phải quay lại đánh boss thế giới bản gốc.

---

## 5. Những điều cần bạn quyết

| # | Câu hỏi | Khuyến nghị của tôi |
|---|---|---|
| 1 | **Ngọc Rồng có nằm trong thưởng nhiệm vụ không?** | Bỏ. Đây là lỗ rò lớn nhất (30.000 ngọc) |
| 2 | **Có dựng bản nhiệm vụ riêng cho 6 boss then chốt không?** (id −2100…−2199, HP thấp, chỉ rơi đồ nhiệm vụ) | Có. Đây là cách duy nhất vừa giữ được cốt truyện vừa không phá cân bằng boss thế giới |
| 3 | **Vật phẩm nhiệm vụ: tạo item mới hay dùng đồ có sẵn?** | Tạo mới, đánh số liền từ 2000. Dùng đồ có sẵn sẽ lẫn với đồ sự kiện và bán được ra tiền |
| 4 | **Bộ Thần Linh ở chương 6: giữ, gắn hạn 30 ngày, hay đổi thành mảnh?** | Đổi thành mảnh phải gom đủ |
| 5 | **Cày bao lâu thì xong tuyến?** (2 tuần / 1 tháng / 3 tháng) | Chưa trả lời từ lượt trước |
| 6 | **Giữ đủ 3 điểm rẽ nhánh?** | Chưa trả lời từ lượt trước |
| 7 | **Nhiệm vụ 12 cần đệ tử, server không có nguồn miễn phí** | Cho NPC sư phụ tặng 1 đệ tử |
| 8 | **Có sửa luôn nhóm D (cân bằng boss sẵn có) không?** | Nên, nhưng có thể làm sau khi tuyến chạy ổn |
