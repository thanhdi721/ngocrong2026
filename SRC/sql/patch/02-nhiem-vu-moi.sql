-- =====================================================================
-- 02-nhiem-vu-moi.sql  —  DỮ LIỆU TUYẾN NHIỆM VỤ CHÍNH MỚI "VẾT NỨT HƯ KHÔNG"
-- Database: team2026        Sinh ngày: 2026-09-18
--
-- Nguồn: docs/2-thiet-ke-nhiem-vu-moi/20, 20a, 20b, 20c
--        docs/4-trien-khai/22-san-sang-code.md (mục 0 — quyết định đã chốt)
--        docs/4-trien-khai/25-bang-id-vat-pham-moi.md (id vật phẩm 2000–2031)
-- Tài liệu đối chiếu bước <-> điều kiện: docs/4-trien-khai/27-du-lieu-nhiem-vu-moi.md
--
-- ---------------------------------------------------------------------
-- !!!!!!!!!!!!!!!!!!!!  CẢNH BÁO — FILE NÀY XÓA DỮ LIỆU  !!!!!!!!!!!!!!!!
-- ---------------------------------------------------------------------
-- File này XÓA TOÀN BỘ `task_main_template` và `task_sub_template` hiện có
-- (30 nhiệm vụ / 125 bước của tuyến cũ) rồi nạp lại 51 nhiệm vụ mới.
-- KHÔNG CÓ ĐƯỜNG LÙI nếu chưa sao lưu.
--
--   mysqldump -u root -p team2026 task_main_template task_sub_template \
--     > backup_task_$(date +%F).sql
--
-- Chạy file này XONG thì BẮT BUỘC chạy tiếp 03-reset-tien-do.sql, vì mọi
-- người chơi đang đứng ở taskId/index của tuyến CŨ; giữ nguyên `data_task`
-- sẽ làm `taskMain.subTasks.get(index)` ném IndexOutOfBoundsException lúc
-- nạp nhân vật (nhiệm vụ mới có số bước khác nhiệm vụ cũ cùng id).
--
-- Thứ tự đúng:
--   1) sao lưu DB
--   2) 01-vat-pham-moi.sql   (phải chạy TRƯỚC — bảng thưởng tham chiếu id 2000–2031)
--   3) 02-nhiem-vu-moi.sql   (file này)
--   4) 03-reset-tien-do.sql
--   5) build lại 20.jar + khởi động lại server
--
-- ---------------------------------------------------------------------
-- HAI THAY ĐỔI JAVA BẮT BUỘC ĐI KÈM FILE NÀY (nếu thiếu, mũi tên chỉ đường sai)
-- ---------------------------------------------------------------------
-- TaskService.transformMapId (SRC/src/nro/models/services/TaskService.java:1134)
--   a) MAP_VACH_NUI (-4) hiện trả 39 / 40 / 41. Tuyến mới dùng vách núi
--      42 Vách núi Aru / 43 Vách núi Moori / 44 Vách núi Kakarot
--      (đó mới là nơi có Jaco, Bà Hạt Mít, Quốc Vương) => đổi thành 42/43/44.
--   b) MAP_500 (-5) hiện KHÔNG được xử lý (trả nguyên -5 xuống client).
--      Tuyến mới dùng -5 cho 2 Thung lũng tre / 9 Thị trấn Moori / 16 Làng Plant
--      => thêm nhánh trả 2 / 9 / 16.
--   Không sửa hai chỗ này thì nhiệm vụ vẫn chạy nhưng mũi tên dẫn đường sai.
--
-- ---------------------------------------------------------------------
-- NGUYÊN TẮC PHẦN THƯỞNG (quyết định của chủ dự án — 22 §0)
-- ---------------------------------------------------------------------
--   * CHỈ sức mạnh, tiềm năng và vật phẩm.
--   * KHÔNG vàng, KHÔNG ngọc, KHÔNG hồng ngọc, KHÔNG Ngọc Rồng (14–20),
--     KHÔNG Thỏi vàng (457 — là tiền trá hình).
--   * Bộ Thần Linh chương 6 trao dưới dạng MẢNH (1066-1070): tuyến cho 150 mảnh MỖI LOẠI
--     (chủ dự án chốt), gom đủ 999 mảnh
--     mỗi loại mới đổi được 1 món ở Whis — tuyến chỉ cho ~400/999 mỗi loại.
--   * Ba cột gold / gem / ruby vẫn giữ trong schema (đúng §9.2) nhưng LUÔN = 0;
--     câu kiểm tra 5.6 ở cuối file bắt lỗi nếu có dòng nào khác 0.
-- =====================================================================

SET NAMES utf8mb4;
START TRANSACTION;

-- ---------------------------------------------------------------------
-- (1) XÓA DỮ LIỆU NHIỆM VỤ CŨ
--     Dùng DELETE + ALTER AUTO_INCREMENT thay cho TRUNCATE để chạy được
--     trong transaction (TRUNCATE tự commit ngầm trong MySQL).
-- ---------------------------------------------------------------------
DELETE FROM `task_sub_template`;
DELETE FROM `task_main_template`;

-- ---------------------------------------------------------------------
-- (2) 51 NHIỆM VỤ CHÍNH — id 0..47 (tuyến chính) + 48, 49, 50 (nhánh)
--     Cột: id, NAME, detail (varchar 500, placeholder %1..%14)
-- ---------------------------------------------------------------------
INSERT INTO `task_main_template` (`id`, `NAME`, `detail`) VALUES
(0, 'Người duy nhất còn nhớ', 'Ngươi tỉnh dậy và cả nhà đã quên tên ngươi.
Về nhà ở %1, mở rương lấy đồ, gặp %2
và ăn một hạt đậu thần.
Thưởng 2.000 sức mạnh
Thưởng 2.000 tiềm năng
Thưởng 5 Đậu thần cấp 1
Thưởng 1 Gói 10 viên Capsule'),
(1, 'Bài học của ông', 'Ông không nhớ tên ngươi nhưng tay ông vẫn nhớ cách dạy đánh.
Đánh 10 mộc nhân ở %1, về gặp %2 rồi cộng điểm tiềm năng.
Thưởng 3.000 sức mạnh
Thưởng 3.000 tiềm năng
Thưởng 1 Rada cấp 1'),
(2, 'Vết nứt đầu tiên', 'Bầu trời trên %3 rách một đường trắng đục.
Diệt 20 %4 đang phát điên và nhặt Mảnh Vỡ Hư Không.
Thưởng 4.000 sức mạnh
Thưởng 4.000 tiềm năng
Thưởng 10 Đậu thần cấp 1'),
(3, 'Cảnh sát vũ trụ Jaco', 'Jaco đáp xuống %5 và đo sức mạnh của ngươi.
Diệt 20 %4 cho hắn xem rồi nâng sức đánh gốc lên 30.
Thưởng 5.000 sức mạnh
Thưởng 5.000 tiềm năng
Thưởng 1 bộ trang bị cấp 1 theo hành tinh'),
(4, 'Thứ bò ra từ vết nứt', 'Lũ thú mẹ ở %6 biến dạng, mắt trắng dã.
Diệt 12 %4 và 15 quái mẹ rồi về kể cho %2.
Thưởng 6.000 sức mạnh
Thưởng 6.000 tiềm năng
Thưởng 2 Gói 10 viên Capsule'),
(5, 'Ký ức của ông', 'Ký ức bị rút đi luôn để lại một mỏ neo.
Tìm Kỷ Vật Của Ông rơi từ %9 trong %13, lau sạch rồi đưa cho %2.
Thưởng 8.000 sức mạnh
Thưởng 8.000 tiềm năng
Thưởng 1 Rada cấp 2
Thưởng 10 Đậu thần cấp 1'),
(6, 'Người thu gom', 'Một kẻ áo choàng xám đang hút thứ trong suốt ra khỏi xác thú.
Hạ 15 %9 trong rừng, hạ Kẻ Thu Gom rồi báo cho %2.
Thưởng 12.000 sức mạnh
Thưởng 12.000 tiềm năng
Thưởng 1 Gói 30 đậu thần cấp 3'),
(7, 'Chạy khỏi vết nứt', 'Vết nứt bắt đầu nuốt cả khu rừng.
Chặt 10 Vòi Hư Không trong 3 phút rồi chạy tới Trạm tàu vũ trụ gặp Jaco.
Thưởng 20.000 sức mạnh
Thưởng 20.000 tiềm năng
Thưởng 1 Mảnh Ký Ức 1
Thưởng 75 Gói 10 viên Capsule'),
(8, 'Máy dò ký ức', 'Bunma chế máy dò ký ức nhưng cần vật liệu.
Gặp Bunma ở Siêu Thị, mua 1 Rada cấp 1 và moi 25 lõi cảm biến từ quái mẹ.
Thưởng 50.000 sức mạnh
Thưởng 50.000 tiềm năng
Thưởng 1 Máy Dò Ký Ức
Thưởng 1 Gói 30 đậu thần cấp 3'),
(9, 'Chuyến bay đầu tiên', 'Máy dò chỉ về nơi có người già nhất hành tinh còn tỉnh táo.
Bay tới %11, diệt 20 %12 và gặp %10.
Thưởng 70.000 sức mạnh
Thưởng 70.000 tiềm năng
Thưởng 1 Rada cấp 3'),
(10, 'Bái sư', 'Sư phụ nhận ngươi làm đệ tử vì ông sắp quên còn ngươi thì chưa.
Học chưởng cấp 1 và đạt 250.000 sức mạnh.
Thưởng 100.000 sức mạnh
Thưởng 100.000 tiềm năng
Thưởng 1 sách đấm cấp 1 theo hành tinh'),
(11, 'Hạt giống hy vọng', 'Cây đậu nhà ngươi sắp chết vì đất cũng quên cách nuôi nó.
Thu hoạch 5 hạt đậu, gieo Hạt Giống Hy Vọng rồi khoe với %2.
Thưởng 140.000 sức mạnh
Thưởng 140.000 tiềm năng
Thưởng 1 Gói 30 đậu thần cấp 3'),
(12, 'Bạn đồng hành', 'Quả trứng ông để dành cho ai đó nứt ra.
Nhận đệ tử, cùng nó diệt 25 quái mẹ rồi về gặp %2.
Thưởng 200.000 sức mạnh
Thưởng 200.000 tiềm năng
Thưởng 1 Đổi đệ tử
Thưởng 1 Nâng kỹ năng 1 đệ tử'),
(13, 'Không ai đi một mình', 'Ký ức một người thì dễ lấy, ký ức cả bang thì khó nuốt.
Vào bang, cùng một người cùng bang diệt 30 quái mẹ rồi gặp Giu-ma Đầu Bò.
Thưởng 280.000 sức mạnh
Thưởng 280.000 tiềm năng
Thưởng 2 Gói 30 đậu thần cấp 3
Thưởng 2 Đá bảo vệ'),
(14, 'Chợ đen ký ức', 'Có kẻ đang đóng hộp ký ức đem bán.
Gặp Bunma ở Nhà Bunma, mua lại một món ở quầy Uron và chặn 20 con thú tải hàng.
Thưởng 400.000 sức mạnh
Thưởng 400.000 tiềm năng
Thưởng 1 Hộp Ký Ức Bị Đánh Cắp
Thưởng 10 Đá nâng cấp cấp 1
Thưởng 3 Đá bảo vệ'),
(15, 'Người bạn đã quên', 'Chữ trên vận đơn là chữ của Jaco.
Tới điểm hẹn, đánh thức Jaco đang bị xóa ký ức rồi gặp lại hắn ở Trạm tàu vũ trụ.
Thưởng 600.000 sức mạnh
Thưởng 600.000 tiềm năng
Thưởng 1 Mảnh Ký Ức 2
Thưởng 1 Gói 30 đậu thần cấp 3
Thưởng 5 Đá nâng cấp cấp 1'),
(16, 'Dấu vết dẫn về phía Nam', 'Máy dò ký ức chỉ về phía Nam.
Lần theo dấu vết, nhặt 5 Vỏ đạn khắc dấu và mang bằng chứng về cho %10.
Thưởng 3.000.000 sức mạnh
Thưởng 3.000.000 tiềm năng
Thưởng 20 Đá nâng cấp cấp 1'),
(17, 'Rèn lại vũ khí', 'Bà Hạt Mít dạy ngươi nâng cấp trang bị và tặng nguyên liệu để thử.
Nâng một trang bị lên +2 rồi dùng Búa rèn cũ.
Thưởng 5.000.000 sức mạnh
Thưởng 5.000.000 tiềm năng
Thưởng 30 Đá nâng cấp cấp 1
Thưởng 3 Đá bảo vệ'),
(18, 'Tapion', 'Ở Thành phố Vegeta có một người không ai nhớ nổi.
Gặp Tapion và đi cùng anh tới Thành phố Santa.
Thưởng 6.000.000 sức mạnh
Thưởng 6.000.000 tiềm năng
Thưởng 20 Đá nâng cấp cấp 2'),
(19, 'Trại lính hoang', 'Trại lính Nappa đã mất trí và tấn công tất cả.
Dọn sạch trại — và nhớ rủ thêm một người.
Thưởng 7.000.000 sức mạnh
Thưởng 7.000.000 tiềm năng
Thưởng 30 Đá nâng cấp cấp 2'),
(20, 'Kẻ săn tiền thưởng', 'Granola đề nghị hợp tác săn ba tay chân của Fide, không cần giấy phép.
Đây là điểm rẽ nhánh 1.
Thưởng 8.000.000 sức mạnh
Thưởng 8.000.000 tiềm năng
Thưởng 40 Đá nâng cấp cấp 2'),
(21, 'Doanh trại Độc Nhãn', 'Bản đồ hành quân của Fide nằm trong Doanh trại Độc Nhãn.
Phá phó bản cùng bang hội, hoặc đi một mình dọn 300 quái tuần tra ở cụm
Trại lính Fide (map 63-67) rồi hỏi lại Lính canh ở Rừng Bamboo.
Thưởng 9.000.000 sức mạnh
Thưởng 9.000.000 tiềm năng
Thưởng 20 Đá nâng cấp cấp 3
Thưởng 2 Bản đồ kho báu'),
(22, 'Tiểu đội sát thủ', 'Tiểu đội sát thủ bị Heart mua chuộc để săn người còn ký ức.
Hạ trọn tiểu đội và lấy Máy đo ký ức.
Thưởng 11.000.000 sức mạnh
Thưởng 11.000.000 tiềm năng
Thưởng 30 Đá nâng cấp cấp 3'),
(23, 'Fide đại ca', 'Fide gom ký ức cả một vùng để bán cho Heart.
Hạ cả ba hình dạng của hắn ở Núi khỉ vàng.
Thưởng 16.000.000 sức mạnh
Thưởng 16.000.000 tiềm năng
Thưởng 50 Đá nâng cấp cấp 3
Thưởng 5 Đá bảo vệ
Thưởng 1 Mảnh Ký Ức 3'),
(24, 'Tín hiệu lạ từ phương Bắc', 'Máy dò bắt được sóng cơ khí phát lại chính ký ức của ngươi.
Lần theo nó lên phía Bắc.
Thưởng 80.000.000 sức mạnh
Thưởng 80.000.000 tiềm năng
Thưởng 20 Đá nâng cấp cấp 4'),
(25, 'Android đầu tiên', 'Hai cỗ máy đầu tiên của Dr. Myuu vẫn còn nguyên dữ liệu.
Mang lõi năng lượng của chúng về cho Bunma.
Thưởng 100.000.000 sức mạnh
Thưởng 100.000.000 tiềm năng
Thưởng 30 Đá nâng cấp cấp 4'),
(26, 'Kim loại và ký ức', 'Sắt thì quên, pha lê thì nhớ.
Bà Hạt Mít dạy pha lê hóa và ép sao — nguyên liệu bà cho không.
Thưởng 130.000.000 sức mạnh
Thưởng 130.000.000 tiềm năng
Thưởng 2 Sao pha lê lục
Thưởng 3 Đá ngũ sắc'),
(27, 'Ba cỗ máy', 'Ba cỗ máy mẫu 13, 14, 15 không đánh để thắng — chúng đánh để ghi hình ngươi.
Thưởng 150.000.000 sức mạnh
Thưởng 150.000.000 tiềm năng
Thưởng 40 Đá nâng cấp cấp 4'),
(28, 'King Kong', 'King Kong không phải máy — nó là một sinh vật bị nhồi kim loại vào đầu.
Hạ nó để mở cửa hầm phòng thí nghiệm.
Thưởng 180.000.000 sức mạnh
Thưởng 180.000.000 tiềm năng
Thưởng 20 Đá nâng cấp cấp 5'),
(29, 'Phòng thí nghiệm Myuu', 'Hệ thống lọc khí chạy mỗi 6 phút.
Lấy 5 bản thiết kế trước khi nó cuốn ngươi ra ngoài.
Thưởng 200.000.000 sức mạnh
Thưởng 200.000.000 tiềm năng
Thưởng 25 Đá nâng cấp cấp 5
Thưởng 5 Đá bảo vệ'),
(30, 'Xên bọ hung', 'Xên bọ hung là mẫu thử gần hoàn thiện nhất của Dr. Myuu.
Hạ cả ba hình dạng của nó ở Thị trấn Ginder.
Thưởng 220.000.000 sức mạnh
Thưởng 220.000.000 tiềm năng
Thưởng 30 Đá nâng cấp cấp 5
Thưởng 2 Sao pha lê vàng'),
(31, 'Bản sao của chính ngươi', 'Mẫu 08 của Dr. Myuu chính là ngươi.
Tới Võ đài Xên và tiêu diệt nó. Đây là điểm rẽ nhánh 2.
Thưởng 230.000.000 sức mạnh
Thưởng 230.000.000 tiềm năng
Thưởng 50 Đá nâng cấp cấp 5
Thưởng 2 Sao pha lê cam
Thưởng 1 Mảnh Ký Ức 4'),
(32, 'Lời cảnh báo của Bardock', 'Bunma đưa bạn Nhẫn thời không sai lệch để tới Hành tinh thực vật.
Tìm Bardock và nhặt 3 Mảnh Ký Ức Vỡ mang về cho ông.
Thưởng 200.000.000 sức mạnh
Thưởng 200.000.000 tiềm năng
Thưởng 30 Đậu thần cấp 8'),
(33, 'Phá vỡ giới hạn', 'HP gốc của bạn đã chạm trần.
Tìm Quốc Vương ở vách núi để mở giới hạn sức mạnh lần đầu, rồi đạt 3 tỷ sức mạnh.
Thưởng 250.000.000 sức mạnh
Thưởng 250.000.000 tiềm năng
Thưởng 50 Đậu thần cấp 8
Thưởng 5 Đá bảo vệ'),
(34, 'Vùng đất băng giá', 'Một Mảnh Ký Ức bị đóng băng trong Hang băng, do Cooler canh giữ.
Map lạnh giảm 50% HP nếu bạn không có option chống lạnh.
Thưởng 300.000.000 sức mạnh
Thưởng 300.000.000 tiềm năng
Thưởng 1 Bông tai Porata'),
(35, 'Con đường rắn độc', 'Muốn biết Heart đem ký ức đi đâu thì phải hỏi người đã chết.
Đi Con đường rắn độc cùng ít nhất một thành viên bang, hoặc không có bang
thì dọn Dơi da xanh / Quỷ chim ở cụm Hang quỷ chim (map 73-82).
Thưởng 350.000.000 sức mạnh
Thưởng 350.000.000 tiềm năng
Thưởng 5 Đá nâng cấp cấp 5'),
(36, 'Cổng phi thuyền', 'Ký ức bị Heart gửi vào cỗ phi thuyền của Babiđây. Cổng chỉ mở 12h-12h59.
Tới sai giờ thì hỏi Ôsin đường vòng qua Sa mạc hoang vu.
Thưởng 400.000.000 sức mạnh
Thưởng 400.000.000 tiềm năng
Thưởng 50 Đậu thần cấp 8'),
(37, 'Mabư', 'Tầng cuối phi thuyền không phải kho — nó là cái bụng.
Hạ Mabư, nhặt Lõi Phép và mang tới Kibit ở Thánh địa Kaio.
Thưởng 450.000.000 sức mạnh
Thưởng 450.000.000 tiềm năng
Thưởng 1 Bông tai Porata'),
(38, 'Black Goku', 'Một kẻ mang khuôn mặt quen thuộc đang xóa sạch Tương lai.
Hạ Black Goku cả hai dạng và mang bằng chứng về cho Bunma.
Thưởng 550.000.000 sức mạnh
Thưởng 550.000.000 tiềm năng
Thưởng 2 Sao pha lê lục'),
(39, 'Cái giá của ký ức', 'Bardock nhìn thấy trước cái chết của bạn dưới tay Baby và chọn đổi chỗ cho bạn.
Gom 7 viên ngọc, ước một điều, rồi tới Làng Kakarot lần cuối.
Thưởng 1.000.000.000 sức mạnh
Thưởng 1.000.000.000 tiềm năng
Thưởng 1 Bông tai Porata
Thưởng 50 Đậu thần cấp 8'),
(40, 'Tổ Sư Kaio', 'Lõi Hư Không không phải vũ khí, nó là nơi chứa.
Lên Thánh địa Kaio, đưa Mảnh Ký Ức đầu tiên cho Tổ Sư Kaio.
Thưởng 550.000.000 sức mạnh
Thưởng 550.000.000 tiềm năng
Thưởng 150 Mảnh áo
Thưởng 50 Đậu thần cấp 8'),
(41, 'Cơn thịnh nộ Broly', 'Broly phát điên vì bị xóa ký ức.
Hạ Broly, rồi hạ tiếp Super Broly xuất hiện ngay tại chỗ.
Thưởng 650.000.000 sức mạnh
Thưởng 650.000.000 tiềm năng
Thưởng 150 Mảnh quần
Thưởng 50 Đậu thần cấp 8'),
(42, 'Hành tinh ngục tù', 'Heart nuôi Cumber trong Hành tinh ngục tù bằng ký ức của tù nhân.
Phá 60 lồng giam trong 10 phút, rồi hạ Cumber cả hai dạng.
Thưởng 750.000.000 sức mạnh
Thưởng 750.000.000 tiềm năng
Thưởng 150 Mảnh găng tay
Thưởng 50 Đậu thần cấp 8'),
(43, 'Khí gas hủy diệt', 'Heart đánh thức Dr Lychee và thả lại khí gas hủy diệt.
Cùng bang hội dọn sạch phó bản, hoặc đi một mình dọn quái ở Hành tinh ngục tù
và Hành tinh thực vật (map 155/160/161), rồi báo lại Thượng Đế.
Thưởng 850.000.000 sức mạnh
Thưởng 850.000.000 tiềm năng
Thưởng 150 Mảnh giầy
Thưởng 50 Đậu thần cấp 8'),
(44, 'Thử thách của Thần Hủy Diệt', 'Bill không cứu ai, ngài chỉ thử.
Thắng một trận đấu, hạ Whis, rồi mở giới hạn sức mạnh lần thứ hai.
Thưởng 900.000.000 sức mạnh
Thưởng 900.000.000 tiềm năng
Thưởng 150 Mảnh nhẫn
Thưởng 50 Đậu thần cấp 8'),
(45, 'Bảy mảnh hợp nhất', 'Ký ức cần hai người mới thành ký ức.
Tới Lãnh địa Fize, nhặt Mảnh Ký Ức thứ bảy và làm lễ hợp nhất cùng một người chơi khác.
Thưởng 950.000.000 sức mạnh
Thưởng 950.000.000 tiềm năng
Thưởng 5 Đá ngũ sắc
Thưởng 50 Đậu thần cấp 8'),
(46, 'Heart', 'Đối đầu Heart tại phòng thí nghiệm Myuu, rồi đuổi theo hắn tới Võ Đài Siêu Cấp và hạ cả ba dạng.
Thưởng 1.100.000.000 sức mạnh
Thưởng 1.100.000.000 tiềm năng
Thưởng 5 Đá ngũ sắc
Thưởng 10 Đá bảo vệ
Thưởng 100 Mảnh áo
Thưởng 100 Mảnh quần
Thưởng 100 Mảnh giầy
Thưởng 100 Mảnh nhẫn
Thưởng 100 Mảnh găng tay'),
(47, 'Trả lại hay giữ lấy', 'Bạn chọn trả toàn bộ ký ức về cho vũ trụ.
Dùng Lõi Hư Không ở Võ Đài Siêu Cấp, mở giới hạn lần cuối rồi hạ phần Hư Không còn sót lại.
Thưởng 1.500.000.000 sức mạnh
Thưởng 1.500.000.000 tiềm năng
Thưởng 1 Người Trả Ký Ức
Thưởng 150 Mảnh áo
Thưởng 150 Mảnh quần
Thưởng 150 Mảnh giầy
Thưởng 150 Mảnh nhẫn
Thưởng 150 Mảnh găng tay
Thưởng 20 Đá bảo vệ
Thưởng 99 Đậu thần cấp 8'),
(48, 'Kẻ săn tiền thưởng', 'Bạn chọn con đường có giấy tờ.
Báo cho cảnh sát vũ trụ Jaco và thi hành lệnh truy nã ba tay chân của Fide.
Thưởng 8.000.000 sức mạnh
Thưởng 8.000.000 tiềm năng
Thưởng 20 Đá nâng cấp cấp 1
Thưởng 8 Đá bảo vệ'),
(49, 'Bản sao của chính ngươi', 'Bạn chọn không giết.
Đánh gục bản sao rồi thu nhận nó làm đồng minh bằng Bình chứa Commeson.
Thưởng 230.000.000 sức mạnh
Thưởng 230.000.000 tiềm năng
Thưởng 3 Sao pha lê lục
Thưởng 5 Đá ngũ sắc
Thưởng 10 Đá bảo vệ
Thưởng 1 Mảnh Ký Ức 4'),
(50, 'Trả lại hay giữ lấy', 'Bạn chọn giữ Lõi Hư Không và trở thành người duy nhất còn nhớ.
Lõi kéo bạn về Hành tinh ngục tù để đối mặt phần còn sót lại của nó.
Thưởng 1.500.000.000 sức mạnh
Thưởng 1.500.000.000 tiềm năng
Thưởng 1 Kẻ Giữ Hư Không
Thưởng 150 Mảnh áo
Thưởng 150 Mảnh quần
Thưởng 150 Mảnh giầy
Thưởng 150 Mảnh nhẫn
Thưởng 150 Mảnh găng tay
Thưởng 20 Đá bảo vệ
Thưởng 99 Đậu thần cấp 8');

-- ---------------------------------------------------------------------
-- (3) BƯỚC CON — task_sub_template
--     Cột: task_main_id, NAME, max_count, notify, npc_id, map, ducvupro
--     ducvupro là khóa chính AUTO_INCREMENT, KHÔNG được code đọc, nhưng
--     Manager nạp task bằng JOIN KHÔNG CÓ ORDER BY và gom bước theo thứ tự
--     trả về => ghi ducvupro tăng dần đúng thứ tự (task_main_id, index).
--
--     npc_id / map âm là placeholder theo hành tinh (ConstTask):
--       map:  -2 MAP_NHA (21/22/23) · -3 MAP_200 (1/8/15) · -4 MAP_VACH_NUI (42/43/44*)
--             -5 MAP_500 (2/9/16*)  · -6 MAP_TTVT (24/25/26) · -7 MAP_QUAI_BAY_600 (3/11/17)
--             -8 MAP_LANG (0/7/14)  · -9 MAP_QUY_LAO (5/13/20) · -1 = không chỉ đường
--       npc:  -2 NPC_NHA (0/2/1) · -3 NPC_TTVT (10/11/12) · -4 NPC_SHOP_LANG (7/8/9)
--             -5 NPC_QUY_LAO (13/14/15) · -1 = không có NPC
--       (*) hai dòng đánh dấu cần sửa transformMapId — xem đầu file.
-- ---------------------------------------------------------------------
INSERT INTO `task_sub_template` (`task_main_id`, `NAME`, `max_count`, `notify`, `npc_id`, `map`, `ducvupro`) VALUES
(0, 'Đi về nhà %2', 1, 'Hãy đi về nhà %2 ở bên phải', -1, -2, 1),  -- TASK_0_0 = 0      A6   Đi về nhà %2
(0, 'Lấy đồ trong rương', 1, '', 3, -2, 2),  -- TASK_0_1 = 2      A8   Lấy đồ trong rương
(0, 'Nói chuyện với %2', 1, '', -2, -2, 3),  -- TASK_0_2 = 4      A3   Nói chuyện với %2
(0, 'Xem cây đậu thần', 1, '', 4, -2, 4),  -- TASK_0_3 = 6      A9   Xem cây đậu thần
(0, 'Ăn một hạt Đậu thần cấp 1', 1, '', -1, -2, 5),  -- TASK_0_4 = 8      A12  Ăn một hạt Đậu thần cấp 1
(1, 'Đập vỡ 10 mộc nhân ở %1', 10, 'Đánh ngã 10 mộc nhân ở %1', -1, -8, 6),  -- TASK_1_0 = 2048   A1   Đập vỡ 10 mộc nhân ở %1
(1, 'Về khoe với %2', 1, 'Quay về báo cho %2', -2, -2, 7),  -- TASK_1_1 = 2050   A3   Về khoe với %2
(1, 'Cộng điểm tiềm năng lần đầu', 1, '', -1, -1, 8),  -- TASK_1_2 = 2052   A7   Cộng điểm tiềm năng lần đầu
(2, 'Lên %3 xem chuyện gì xảy ra', 1, '', -1, -3, 9),  -- TASK_2_0 = 4096   A6   Lên %3 xem chuyện gì xảy ra
(2, 'Tiêu diệt 20 %4 đang phát điên', 20, 'Tiêu diệt 20 %4', -1, -3, 10),  -- TASK_2_1 = 4098   A1   Tiêu diệt 20 %4 đang phát điên
(2, 'Nhặt Mảnh Vỡ Hư Không', 1, '', -1, -3, 11),  -- TASK_2_2 = 4100   A4   Nhặt Mảnh Vỡ Hư Không
(3, 'Gặp Jaco ở %5', 1, '', 63, -4, 12),  -- TASK_3_0 = 6144   A3   Gặp Jaco ở %5
(3, 'Cho Jaco xem ngươi đánh 20 %4', 20, '', -1, -3, 13),  -- TASK_3_1 = 6146   A1   Cho Jaco xem ngươi đánh 20 %4
(3, 'Nâng sức đánh gốc lên 30', 1, '', -1, -1, 14),  -- TASK_3_2 = 6148   A11  Nâng sức đánh gốc lên 30
(4, 'Dọn đường vào %6: hạ 12 %4', 12, '', -1, -5, 15),  -- TASK_4_0 = 8192   A1   Dọn đường vào %6: hạ 12 %4
(4, 'Hạ 15 quái mẹ biến dạng', 15, '', -1, -5, 16),  -- TASK_4_1 = 8194   A1   Hạ 15 quái mẹ biến dạng
(4, 'Kể lại cho %2', 1, '', -2, -2, 17),  -- TASK_4_2 = 8196   A3   Kể lại cho %2
(5, 'Tìm Kỷ Vật Của Ông trong %13', 1, '', -1, -7, 18),  -- TASK_5_0 = 10240  A4   Tìm Kỷ Vật Của Ông trong %13
(5, 'Lau sạch kỷ vật', 1, '', -1, -1, 19),  -- TASK_5_1 = 10242  A12  Lau sạch kỷ vật
(5, 'Đưa kỷ vật cho %2', 1, '', -2, -2, 20),  -- TASK_5_2 = 10244  A3   Đưa kỷ vật cho %2
(6, 'Lần theo tiếng rít: hạ 15 %9', 15, '', -1, -1, 21),  -- TASK_6_0 = 12288  A1   Lần theo tiếng rít: hạ 15 %9
(6, 'Hạ Kẻ Thu Gom', 1, '', -1, -1, 22),  -- TASK_6_1 = 12290  A2   Hạ Kẻ Thu Gom
(6, 'Báo lại cho %2', 1, '', -2, -2, 23),  -- TASK_6_2 = 12292  A3   Báo lại cho %2
(7, 'Chặt 10 Vòi Hư Không trong 3 phút', 10, 'Còn 3 phút! Chặt hết vòi đi!', -1, -1, 24),  -- TASK_7_0 = 14336  B12  Chặt 10 Vòi Hư Không trong 3 phút
(7, 'Chạy tới Trạm tàu vũ trụ', 1, '', -3, -6, 25),  -- TASK_7_1 = 14338  A6   Chạy tới Trạm tàu vũ trụ
(7, 'Nói chuyện với Jaco', 1, '', 63, -6, 26),  -- TASK_7_2 = 14340  A3   Nói chuyện với Jaco
(8, 'Gặp Bunma ở Siêu Thị', 1, '', 7, 84, 27),  -- TASK_8_0 = 16384  A3   Gặp Bunma ở Siêu Thị
(8, 'Mua 1 Rada cấp 1 ở shop', 1, '', -4, 84, 28),  -- TASK_8_1 = 16386  B4   Mua 1 Rada cấp 1 ở shop
(8, 'Moi 25 lõi cảm biến từ quái mẹ', 25, '', -1, -1, 29),  -- TASK_8_2 = 16388  A1   Moi 25 lõi cảm biến từ quái mẹ
(9, 'Bay tới %11', 1, '', -1, -9, 30),  -- TASK_9_0 = 18432  A6   Bay tới %11
(9, 'Dọn sạch 20 %12 quanh nhà sư phụ', 20, '', -1, -9, 31),  -- TASK_9_1 = 18434  A1   Dọn sạch 20 %12 quanh nhà sư phụ
(9, 'Nói chuyện với %10', 1, '', -5, -9, 32),  -- TASK_9_2 = 18436  A3   Nói chuyện với %10
(10, 'Xin %10 nhận làm đệ tử', 1, '', -5, -9, 33),  -- TASK_10_0 = 20480  A3   Xin %10 nhận làm đệ tử
(10, 'Học chưởng cấp 1', 1, '', -5, -9, 34),  -- TASK_10_1 = 20482  B9   Học chưởng cấp 1
(10, 'Đạt 250.000 sức mạnh', 1, '', -1, -1, 35),  -- TASK_10_2 = 20484  A5   Đạt 250.000 sức mạnh
(11, 'Thu hoạch 5 hạt đậu thần', 5, '', 4, -2, 36),  -- TASK_11_0 = 22528  B7   Thu hoạch 5 hạt đậu thần
(11, 'Gieo Hạt Giống Hy Vọng', 1, '', 4, -2, 37),  -- TASK_11_1 = 22530  A12  Gieo Hạt Giống Hy Vọng
(11, 'Khoe cây mới với %2', 1, '', -2, -2, 38),  -- TASK_11_2 = 22532  A3   Khoe cây mới với %2
(12, 'Nhận đệ tử đầu tiên', 1, '', 50, -2, 39),  -- TASK_12_0 = 24576  B8   Nhận đệ tử đầu tiên
(12, 'Cùng đệ tử diệt 25 quái mẹ', 25, '', -1, -1, 40),  -- TASK_12_1 = 24578  A1   Cùng đệ tử diệt 25 quái mẹ
(12, 'Giới thiệu đệ tử với %2', 1, '', -2, -2, 41),  -- TASK_12_2 = 24580  A3   Giới thiệu đệ tử với %2
(13, 'Gia nhập một bang hội', 1, '', -1, -1, 42),  -- TASK_13_0 = 26624  A10  Gia nhập một bang hội
(13, 'Cùng bạn cùng bang diệt 30 quái mẹ', 30, 'Cần thêm 1 thành viên cùng bang ở cùng khu', -1, -1, 43),  -- TASK_13_1 = 26626  B13  Cùng bạn cùng bang diệt 30 quái mẹ
(13, 'Gặp Giu-ma Đầu Bò', 1, '', 47, 153, 44),  -- TASK_13_2 = 26628  A3   Gặp Giu-ma Đầu Bò
(14, 'Nghe Bunma báo tin ở Nhà Bunma', 1, '', 37, 102, 45),  -- TASK_14_0 = 28672  A3   Nghe Bunma báo tin ở Nhà Bunma
(14, 'Mua một món hàng ở quầy Uron', 1, '', 16, 84, 46),  -- TASK_14_1 = 28674  B4   Mua một món hàng ở quầy Uron
(14, 'Chặn 20 con thú tải hàng', 20, '', -1, -1, 47),  -- TASK_14_2 = 28676  A1   Chặn 20 con thú tải hàng
(15, 'Tới điểm hẹn: hạ 20 quái mẹ', 20, '', -1, -1, 48),  -- TASK_15_0 = 30720  A1   Tới điểm hẹn: hạ 20 quái mẹ
(15, 'Đánh thức Jaco', 1, '', -1, -1, 49),  -- TASK_15_1 = 30722  A2   Đánh thức Jaco
(15, 'Gặp lại Jaco ở Trạm tàu vũ trụ', 1, '', 63, -6, 50),  -- TASK_15_2 = 30724  A3   Gặp lại Jaco ở Trạm tàu vũ trụ
(16, 'Lần theo tín hiệu về phía Nam', 1, '', -1, -1, 51),  -- TASK_16_0 = 32768  A6   Lần theo tín hiệu về phía Nam
(16, 'Dọn sạch 40 quái chắn đường', 40, '', -1, -1, 52),  -- TASK_16_1 = 32770  A1   Dọn sạch 40 quái chắn đường
(16, 'Tiêu diệt 30 quái canh bờ biển', 30, '', -1, -1, 53),  -- TASK_16_2 = 32772  A1   Tiêu diệt 30 quái canh bờ biển
(16, 'Nhặt 5 Vỏ đạn khắc dấu', 5, '', -1, -1, 54),  -- TASK_16_3 = 32774  A4   Nhặt 5 Vỏ đạn khắc dấu
(16, 'Mang vỏ đạn về cho %10', 1, '', -5, -9, 55),  -- TASK_16_4 = 32776  A3   Mang vỏ đạn về cho %10
(17, 'Gặp Bà Hạt Mít ở %5', 1, '', 21, -4, 56),  -- TASK_17_0 = 34816  A3   Gặp Bà Hạt Mít ở %5
(17, 'Nâng một trang bị lên +2', 1, '', 21, -4, 57),  -- TASK_17_1 = 34818  B2   Nâng một trang bị lên +2
(17, 'Dùng Búa rèn cũ', 1, '', -1, -1, 58),  -- TASK_17_2 = 34820  A12  Dùng Búa rèn cũ
(17, 'Khoe vũ khí mới với %10', 1, '', -5, -9, 59),  -- TASK_17_3 = 34822  A3   Khoe vũ khí mới với %10
(18, 'Tới Thành phố Vegeta', 1, '', -1, 19, 60),  -- TASK_18_0 = 36864  A6   Tới Thành phố Vegeta
(18, 'Nói chuyện với người lạ thổi nhạc', 1, '', 53, 19, 61),  -- TASK_18_1 = 36866  A3   Nói chuyện với người lạ thổi nhạc
(18, 'Diệt 30 quái đang vây thành', 30, '', -1, -1, 62),  -- TASK_18_2 = 36868  A1   Diệt 30 quái đang vây thành
(18, 'Đi cùng Tapion tới Thành phố Santa', 1, '', -1, 126, 63),  -- TASK_18_3 = 36870  A6   Đi cùng Tapion tới Thành phố Santa
(18, 'Nghe Tapion kể chuyện', 1, '', 53, 126, 64),  -- TASK_18_4 = 36872  A3   Nghe Tapion kể chuyện
(19, 'Gặp Cui ở Thung lũng Nappa', 1, '', 12, 68, 65),  -- TASK_19_0 = 38912  A3   Gặp Cui ở Thung lũng Nappa
(19, 'Hạ 60 Nappa mất trí', 60, '', -1, 68, 66),  -- TASK_19_1 = 38914  A1   Hạ 60 Nappa mất trí
(19, 'Hạ 40 Soldier gác kho', 40, '', -1, 69, 67),  -- TASK_19_2 = 38916  A1   Hạ 40 Soldier gác kho
(19, 'Cùng người khác hạ 30 Appule', 30, 'Bước này cần làm cùng người khác', -1, 71, 68),  -- TASK_19_3 = 38918  B13  Cùng người khác hạ 30 Appule
(19, 'Báo cáo với Cui', 1, '', 12, 68, 69),  -- TASK_19_4 = 38920  A3   Báo cáo với Cui
(20, 'Tìm kẻ lạ ở Khu hang động', 1, '', 71, 160, 70),  -- TASK_20_0 = 40960  A3   Tìm kẻ lạ ở Khu hang động
(20, 'Chọn cách xử lý: Granola hay Jaco', 1, '', 71, 160, 71),  -- TASK_20_1 = 40962  B14  Chọn cách xử lý: Granola hay Jaco
(20, 'Bắt tay với Granola', 1, '', 76, 160, 72),  -- TASK_20_2 = 40964  A3   Bắt tay với Granola
(20, 'Thanh toán 3 tay chân của Fide', 3, '', -1, -1, 73),  -- TASK_20_3 = 40966  A2   Thanh toán 3 tay chân của Fide
(20, 'Nhặt 3 Thẻ tiền thưởng', 3, '', -1, -1, 74),  -- TASK_20_4 = 40968  A4   Nhặt 3 Thẻ tiền thưởng
(20, 'Nhận tiền thưởng từ Granola', 1, '', 76, 160, 75),  -- TASK_20_5 = 40970  A3   Nhận tiền thưởng từ Granola
(21, 'Gặp Lính canh ở Rừng Bamboo', 1, '', 25, 27, 76),  -- TASK_21_0 = 43008  A3   Gặp Lính canh ở Rừng Bamboo
(21, 'Phá Doanh trại Độc Nhãn cùng bang, hoặc diệt 300 quái cụm Trại lính Fide', 300, 'Không có bang? Diệt 300 quái ở map 63-67: Trại lính Fide, Núi dây leo, Núi cây quỷ, Trại quỷ già, Vực chết', -1, 53, 77),  -- TASK_21_1 = 43010  B1   Phá xong Doanh trại Độc Nhãn
(21, 'Lấy bản đồ hành quân từ Độc Nhãn, hoặc từ Lính canh ở Rừng Bamboo', 1, 'Không vào được doanh trại? Nói chuyện với Lính canh ở Rừng Bamboo (map 27) để lấy bản đồ', 26, 57, 78),  -- TASK_21_2 = 43012  A3   Lấy bản đồ hành quân từ Độc Nhãn
(21, 'Mang bản đồ về cho %10', 1, '', -5, -9, 79),  -- TASK_21_3 = 43014  A3   Mang bản đồ về cho %10
(22, 'Hỏi Tapion về máy đo lạ', 1, '', 53, 126, 80),  -- TASK_22_0 = 45056  A3   Hỏi Tapion về máy đo lạ
(22, 'Hạ 40 lính khỉ canh đường', 40, '', -1, 81, 81),  -- TASK_22_1 = 45058  A1   Hạ 40 lính khỉ canh đường
(22, 'Hạ trọn Tiểu đội sát thủ', 5, '', -1, 79, 82),  -- TASK_22_2 = 45060  A2   Hạ trọn Tiểu đội sát thủ
(22, 'Nhặt Máy đo ký ức', 1, '', -1, 79, 83),  -- TASK_22_3 = 45062  A4   Nhặt Máy đo ký ức
(22, 'Đưa máy đo cho Tapion', 1, '', 53, 126, 84),  -- TASK_22_4 = 45064  A3   Đưa máy đo cho Tapion
(23, 'Đạt 80.000.000 sức mạnh', 1, '', -1, -1, 85),  -- TASK_23_0 = 47104  A5   Đạt 80.000.000 sức mạnh
(23, 'Tới Núi khỉ vàng', 1, '', -1, 80, 86),  -- TASK_23_1 = 47106  A6   Tới Núi khỉ vàng
(23, 'Đốt kho tiếp tế: 25 Khỉ lông vàng trong 5 phút', 25, 'Còn 5 phút!', -1, 80, 87),  -- TASK_23_2 = 47108  B12  Đốt kho tiếp tế: 25 Khỉ lông vàng trong 5 phút
(23, 'Hạ Fide đại ca — hai dạng đầu', 2, '', -1, 80, 88),  -- TASK_23_3 = 47110  A2   Hạ Fide đại ca — hai dạng đầu
(23, 'Hạ Fide đại ca — dạng cuối', 1, '', -1, 80, 89),  -- TASK_23_4 = 47112  A2   Hạ Fide đại ca — dạng cuối
(23, 'Tuyên bố liên minh với %10', 1, '', -5, -9, 90),  -- TASK_23_5 = 47114  A3   Tuyên bố liên minh với %10
(24, 'Nghe Bunma nói về sóng cơ khí', 1, '', 37, 102, 91),  -- TASK_24_0 = 49152  A3   Nghe Bunma nói về sóng cơ khí
(24, 'Tới Thành phố phía đông', 1, '', -1, 92, 92),  -- TASK_24_1 = 49154  A6   Tới Thành phố phía đông
(24, 'Diệt 50 Xên con cấp 1-2', 50, '', -1, 92, 93),  -- TASK_24_2 = 49156  A1   Diệt 50 Xên con cấp 1-2
(24, 'Diệt 40 Xên con cấp 3-4', 40, '', -1, 94, 94),  -- TASK_24_3 = 49158  A1   Diệt 40 Xên con cấp 3-4
(24, 'Báo lại cho Bunma', 1, '', 37, 102, 95),  -- TASK_24_4 = 49160  A3   Báo lại cho Bunma
(25, 'Tới Cao nguyên tìm hai bác sĩ', 1, '', -1, 96, 96),  -- TASK_25_0 = 51200  A6   Tới Cao nguyên tìm hai bác sĩ
(25, 'Hạ Android 19 rồi Dr.Kôrê', 2, '', -1, 96, 97),  -- TASK_25_1 = 51202  A2   Hạ Android 19 rồi Dr.Kôrê
(25, 'Nhặt 3 Lõi năng lượng Android', 3, '', -1, 96, 98),  -- TASK_25_2 = 51204  A4   Nhặt 3 Lõi năng lượng Android
(25, 'Đưa lõi cho Bunma mổ xẻ', 1, '', 37, 102, 99),  -- TASK_25_3 = 51206  A3   Đưa lõi cho Bunma mổ xẻ
(26, 'Gặp Bà Hạt Mít ở Đảo Kamê', 1, '', 21, 5, 100),  -- TASK_26_0 = 53248  A3   Gặp Bà Hạt Mít ở Đảo Kamê
(26, 'Pha lê hóa một trang bị', 1, '', 21, 5, 101),  -- TASK_26_1 = 53250  B3   Pha lê hóa một trang bị
(26, 'Ép 1 Sao pha lê vào trang bị đó', 1, '', 21, 5, 102),  -- TASK_26_2 = 53252  B3   Ép 1 Sao pha lê vào trang bị đó
(26, 'Dùng Mẫu kim loại có ký ức', 1, '', -1, -1, 103),  -- TASK_26_3 = 53254  A12  Dùng Mẫu kim loại có ký ức
(26, 'Kể lại cho %10 những gì ngươi nghe được', 1, '', -5, -9, 104),  -- TASK_26_4 = 53256  A3   Kể lại cho %10 những gì ngươi nghe được
(27, 'Hỏi Ca Lích về container lạ', 1, '', 38, 102, 105),  -- TASK_27_0 = 55296  A3   Hỏi Ca Lích về container lạ
(27, 'Tới sân sau siêu thị', 1, '', -1, 104, 106),  -- TASK_27_1 = 55298  A6   Tới sân sau siêu thị
(27, 'Hạ ba cỗ máy mẫu', 3, '', -1, 104, 107),  -- TASK_27_2 = 55300  A2   Hạ ba cỗ máy mẫu
(27, 'Báo cáo với Ca Lích', 1, '', 38, 102, 108),  -- TASK_27_3 = 55302  A3   Báo cáo với Ca Lích
(28, 'Tới Thành phố phía bắc', 1, '', -1, 97, 109),  -- TASK_28_0 = 57344  A6   Tới Thành phố phía bắc
(28, 'Dọn 60 Xên con cấp 5-7 quanh cửa hầm', 60, '', -1, 97, 110),  -- TASK_28_1 = 57346  A1   Dọn 60 Xên con cấp 5-7 quanh cửa hầm
(28, 'Hạ Poc, Pic rồi King Kong', 3, '', -1, 97, 111),  -- TASK_28_2 = 57348  A2   Hạ Poc, Pic rồi King Kong
(28, 'Nhặt Mảnh giáp có khắc tên', 1, '', -1, 97, 112),  -- TASK_28_3 = 57350  A4   Nhặt Mảnh giáp có khắc tên
(28, 'Đưa mảnh giáp cho Bunma', 1, '', 37, 102, 113),  -- TASK_28_4 = 57352  A3   Đưa mảnh giáp cho Bunma
(29, 'Lấy thẻ từ giả của Bunma', 1, '', 37, 102, 114),  -- TASK_29_0 = 59392  A3   Lấy thẻ từ giả của Bunma
(29, 'Đột nhập Phòng thí nghiệm Myuu', 1, '', -1, 166, 115),  -- TASK_29_1 = 59394  A6   Đột nhập Phòng thí nghiệm Myuu
(29, 'Giật 5 Bản thiết kế trong 6 phút', 5, 'Hệ thống lọc khí sẽ chạy sau 6 phút', -1, 166, 116),  -- TASK_29_2 = 59396  B12  Giật 5 Bản thiết kế trong 6 phút
(29, 'Đối mặt Dr. Myuu', 1, '', 83, 166, 117),  -- TASK_29_3 = 59398  A3   Đối mặt Dr. Myuu
(30, 'Tới Thị trấn Ginder', 1, '', -1, 100, 118),  -- TASK_30_0 = 61440  A6   Tới Thị trấn Ginder
(30, 'Diệt 50 Xên con cấp 8', 50, '', -1, 100, 119),  -- TASK_30_1 = 61442  A1   Diệt 50 Xên con cấp 8
(30, 'Hạ Xên bọ hung — hai dạng đầu', 2, '', -1, 100, 120),  -- TASK_30_2 = 61444  A2   Hạ Xên bọ hung — hai dạng đầu
(30, 'Hạ Xên hoàn thiện', 1, '', -1, 100, 121),  -- TASK_30_3 = 61446  A2   Hạ Xên hoàn thiện
(30, 'Báo với Bunma về mẫu 07', 1, '', 37, 102, 122),  -- TASK_30_4 = 61448  A3   Báo với Bunma về mẫu 07
(31, 'Nghe Potage nói sự thật', 1, '', 62, 140, 123),  -- TASK_31_0 = 63488  A3   Nghe Potage nói sự thật
(31, 'Quyết định số phận bản sao', 1, '', 62, 140, 124),  -- TASK_31_1 = 63490  B14  Quyết định số phận bản sao
(31, 'Tới Võ đài Xên bọ hung', 1, '', -1, 103, 125),  -- TASK_31_2 = 63492  A6   Tới Võ đài Xên bọ hung
(31, 'Gọi một nhân chứng vào võ đài', 1, 'Võ đài cần một nhân chứng', -1, 103, 126),  -- TASK_31_3 = 63494  B13  Gọi một nhân chứng vào võ đài
(31, 'Tiêu diệt Bản sao của ngươi', 1, '', -1, 103, 127),  -- TASK_31_4 = 63496  A2   Tiêu diệt Bản sao của ngươi
(31, 'Đạt 2.000.000.000 sức mạnh', 1, '', -1, -1, 128),  -- TASK_31_5 = 63498  A5   Đạt 2.000.000.000 sức mạnh
(32, 'Gặp Bunma ở Nhà Bunma', 1, '', 37, 102, 129),  -- TASK_32_0 = 65536  A3   Gặp Bunma ở Nhà Bunma
(32, 'Dùng Nhẫn thời không sai lệch', 1, '', -1, -1, 130),  -- TASK_32_1 = 65538  A12  Dùng Nhẫn thời không sai lệch
(32, 'Nói chuyện với Bardock', 1, '', 70, 160, 131),  -- TASK_32_2 = 65540  A3   Nói chuyện với Bardock
(32, 'Dọn sạch hang động nguyên thủy', 40, '', -1, 160, 132),  -- TASK_32_3 = 65542  A1   Dọn sạch hang động nguyên thủy
(32, 'Nhặt 3 Mảnh Ký Ức Vỡ', 3, '', -1, 161, 133),  -- TASK_32_4 = 65544  A4   Nhặt 3 Mảnh Ký Ức Vỡ
(32, 'Báo cáo với Bardock', 1, '', 70, 160, 134),  -- TASK_32_5 = 65546  A3   Báo cáo với Bardock
(33, 'Tới vách núi của hành tinh bạn', 1, '', -1, -4, 135),  -- TASK_33_0 = 67584  A6   Tới vách núi của hành tinh bạn
(33, 'Nói chuyện với Quốc Vương', 1, '', 42, -4, 136),  -- TASK_33_1 = 67586  A3   Nói chuyện với Quốc Vương
(33, 'Nâng HP gốc chạm trần 220.000', 1, '', -1, -1, 137),  -- TASK_33_2 = 67588  B11  Nâng HP gốc chạm trần 220.000
(33, 'Mở giới hạn sức mạnh', 1, '', 42, -4, 138),  -- TASK_33_3 = 67590  B10  Mở giới hạn sức mạnh
(33, 'Đạt 3 tỷ sức mạnh', 1, '', -1, -1, 139),  -- TASK_33_4 = 67592  A5   Đạt 3 tỷ sức mạnh
(33, 'Báo cáo với Quốc Vương', 1, '', 42, -4, 140),  -- TASK_33_5 = 67594  A3   Báo cáo với Quốc Vương
(34, 'Tới Cánh đồng tuyết', 1, '', -1, 105, 141),  -- TASK_34_0 = 69632  A6   Tới Cánh đồng tuyết
(34, 'Diệt bọn canh băng', 50, '', -1, 105, 142),  -- TASK_34_1 = 69634  A1   Diệt bọn canh băng
(34, 'Hạ 20 Kado trong 5 phút', 20, 'Còn 5 phút!', -1, 108, 143),  -- TASK_34_2 = 69636  B12  Hạ 20 Kado trong 5 phút
(34, 'Nhặt Mảnh Ký Ức Đóng Băng', 1, '', -1, 110, 144),  -- TASK_34_3 = 69638  A4   Nhặt Mảnh Ký Ức Đóng Băng
(34, 'Hạ Cooler cả hai dạng', 2, '', -1, 110, 145),  -- TASK_34_4 = 69640  A2   Hạ Cooler cả hai dạng
(34, 'Báo cáo với Bardock', 1, '', 70, 160, 146),  -- TASK_34_5 = 69642  A3   Báo cáo với Bardock
(35, 'Nói chuyện với Thần Vũ Trụ', 1, '', 20, 48, 147),  -- TASK_35_0 = 71680  A3   Nói chuyện với Thần Vũ Trụ
(35, 'Vào Con đường rắn độc cùng bạn bang, hoặc sát cánh 1 người khác ở Hang quỷ chim', 1, 'Không có bang? Đứng cùng ít nhất 1 người chơi khác ở map 73/74/76/77/81/82', 20, 143, 148),  -- TASK_35_1 = 71682  B13  Vào Con đường rắn độc cùng bạn
(35, 'Dọn đường qua ba chặng, hoặc diệt 120 Dơi da xanh / Quỷ chim ngoài phó bản', 120, 'Trong phó bản mỗi mạng tính 2 điểm (đủ 60 con là xong); ngoài phó bản (map 73-82) mỗi mạng 1 điểm', -1, 141, 149),  -- TASK_35_2 = 71684  A1   Dọn đường qua ba chặng
(35, 'Hoàn thành Con đường rắn độc, hoặc diệt 200 Dơi da xanh / Quỷ chim', 200, 'Không có bang? Diệt 200 Dơi da xanh hoặc Quỷ chim ở map 73/74/76/77/81/82', -1, 144, 150),  -- TASK_35_3 = 71686  B1   Hoàn thành Con đường rắn độc
(35, 'Gặp Thượng Đế ở Thần điện', 1, '', 19, 45, 151),  -- TASK_35_4 = 71688  A3   Gặp Thượng Đế ở Thần điện
(36, 'Gặp Ôsin ở Đại hội võ thuật', 1, '', 44, 52, 152),  -- TASK_36_0 = 73728  A3   Gặp Ôsin ở Đại hội võ thuật
(36, 'Vào Cổng phi thuyền', 1, '', 44, 114, 153),  -- TASK_36_1 = 73730  A6   Vào Cổng phi thuyền
(36, 'Hạ Drabura hoặc 20 Cadic M', 20, '', -1, 114, 154),  -- TASK_36_2 = 73732  A2   Hạ Drabura hoặc 20 Cadic M
(36, 'Xuống tới Cửa Ải 1', 1, '', -1, 117, 155),  -- TASK_36_3 = 73734  A6   Xuống tới Cửa Ải 1
(36, 'Nói chuyện với Babiđây', 1, '', 46, 117, 156),  -- TASK_36_4 = 73736  A3   Nói chuyện với Babiđây
(37, 'Gặp Ôsin ở Đại hội võ thuật', 1, '', 44, 52, 157),  -- TASK_37_0 = 75776  A3   Gặp Ôsin ở Đại hội võ thuật
(37, 'Hạ Mabư', 1, '', -1, 120, 158),  -- TASK_37_1 = 75778  A2   Hạ Mabư
(37, 'Hạ Drabura 3 hoặc 30 Quỷ chim', 30, '', -1, 120, 159),  -- TASK_37_2 = 75780  A2   Hạ Drabura 3 hoặc 30 Quỷ chim
(37, 'Nhặt Lõi Phép Babiđây', 1, '', -1, 120, 160),  -- TASK_37_3 = 75782  A4   Nhặt Lõi Phép Babiđây
(37, 'Mang Lõi Phép cho Kibit', 1, '', 45, 50, 161),  -- TASK_37_4 = 75784  A3   Mang Lõi Phép cho Kibit
(38, 'Gặp Bunma ở Tương lai', 1, '', 37, 102, 162),  -- TASK_38_0 = 77824  A3   Gặp Bunma ở Tương lai
(38, 'Dọn sạch bọn Xên con phía bắc', 60, '', -1, 97, 163),  -- TASK_38_1 = 77826  A1   Dọn sạch bọn Xên con phía bắc
(38, 'Hạ Black Goku', 1, '', -1, 92, 164),  -- TASK_38_2 = 77828  A2   Hạ Black Goku
(38, 'Hạ Super Black Goku', 1, '', -1, 92, 165),  -- TASK_38_3 = 77830  A2   Hạ Super Black Goku
(38, 'Nhặt Nhẫn thời không sai lệch', 1, '', -1, 92, 166),  -- TASK_38_4 = 77832  A4   Nhặt Nhẫn thời không sai lệch
(38, 'Báo cáo với Bunma', 1, '', 37, 102, 167),  -- TASK_38_5 = 77834  A3   Báo cáo với Bunma
(39, 'Nói chuyện với Bardock', 1, '', 70, 160, 168),  -- TASK_39_0 = 79872  A3   Nói chuyện với Bardock
(39, 'Gom đủ 7 viên Ngọc Rồng', 7, '', -1, -1, 169),  -- TASK_39_1 = 79874  A4   Gom đủ 7 viên Ngọc Rồng
(39, 'Gọi Rồng Thần và ước', 1, '', 5, -8, 170),  -- TASK_39_2 = 79876  B6   Gọi Rồng Thần và ước
(39, 'Hỏi Rồng Omega về sao đen', 1, '', 29, -6, 171),  -- TASK_39_3 = 79878  A3   Hỏi Rồng Omega về sao đen
(39, 'Diệt bầy khỉ ở Núi khỉ vàng', 40, '', -1, 80, 172),  -- TASK_39_4 = 79880  A1   Diệt bầy khỉ ở Núi khỉ vàng
(39, 'Gặp Bardock ở Làng Kakarot', 1, '', 70, 14, 173),  -- TASK_39_5 = 79882  A3   Gặp Bardock ở Làng Kakarot
(39, 'Hạ Baby cả ba dạng', 3, '', -1, 14, 174),  -- TASK_39_6 = 79884  A2   Hạ Baby cả ba dạng
(40, 'Nói chuyện với Thần Vũ Trụ', 1, '', 20, 48, 175),  -- TASK_40_0 = 81920  A3   Nói chuyện với Thần Vũ Trụ
(40, 'Lên Thánh địa Kaio', 1, '', -1, 50, 176),  -- TASK_40_1 = 81922  A6   Lên Thánh địa Kaio
(40, 'Nói chuyện với Tổ Sư Kaio', 1, '', 43, 50, 177),  -- TASK_40_2 = 81924  A3   Nói chuyện với Tổ Sư Kaio
(40, 'Đưa Mảnh Ký Ức cho Tổ Sư Kaio', 1, '', 43, 50, 178),  -- TASK_40_3 = 81926  A12  Đưa Mảnh Ký Ức cho Tổ Sư Kaio
(40, 'Nghe Kibit kể phần còn lại', 1, '', 45, 50, 179),  -- TASK_40_4 = 81928  A3   Nghe Kibit kể phần còn lại
(41, 'Nhận lời dặn của Tổ Sư Kaio', 1, '', 43, 50, 180),  -- TASK_41_0 = 83968  A3   Nhận lời dặn của Tổ Sư Kaio
(41, 'Dọn dẹp vành đai rừng', 60, '', -1, 27, 181),  -- TASK_41_1 = 83970  A1   Dọn dẹp vành đai rừng
(41, 'Hạ Broly', 1, '', -1, 27, 182),  -- TASK_41_2 = 83972  A2   Hạ Broly
(41, 'Hạ Super Broly', 1, '', -1, 27, 183),  -- TASK_41_3 = 83974  A2   Hạ Super Broly
(41, 'Báo lại với Tổ Sư Kaio', 1, '', 43, 50, 184),  -- TASK_41_4 = 83976  A3   Báo lại với Tổ Sư Kaio
(42, 'Hỏi Ôsin đường tới ngục tù', 1, '', 44, 50, 185),  -- TASK_42_0 = 86016  A3   Hỏi Ôsin đường tới ngục tù
(42, 'Tới Hành tinh ngục tù', 1, '', -1, 155, 186),  -- TASK_42_1 = 86018  A6   Tới Hành tinh ngục tù
(42, 'Phá 60 lồng giam trong 10 phút', 60, 'Còn 10 phút!', -1, 155, 187),  -- TASK_42_2 = 86020  B12  Phá 60 lồng giam trong 10 phút
(42, 'Hạ Cumber', 1, '', -1, 155, 188),  -- TASK_42_3 = 86022  A2   Hạ Cumber
(42, 'Hạ Super Cumber', 1, '', -1, 155, 189),  -- TASK_42_4 = 86024  A2   Hạ Super Cumber
(42, 'Báo cáo với Ôsin', 1, '', 44, 155, 190),  -- TASK_42_5 = 86026  A3   Báo cáo với Ôsin
(43, 'Gặp Mr Popo ở Làng Aru', 1, '', 67, 0, 191),  -- TASK_43_0 = 88064  A3   Gặp Mr Popo ở Làng Aru
(43, 'Dọn sạch khí gas, hoặc diệt 160 quái ở Hành tinh ngục tù / thực vật', 160, 'Trong phó bản mỗi mạng tính 2 điểm (đủ 80 con là xong); ngoài phó bản (map 155/160/161) mỗi mạng 1 điểm', -1, 147, 192),  -- TASK_43_1 = 88066  A1   Dọn sạch khí gas
(43, 'Hạ Dr Lychee, hoặc diệt 50 quái ở map 155/160/161', 50, 'Không có bang? Diệt 50 Khỉ lông xanh / Taburine Đỏ (map 155) hoặc Cabira / Tobi (map 160, 161)', -1, 148, 193),  -- TASK_43_2 = 88068  A2   Hạ Dr Lychee
(43, 'Hạ Hatchiyack, hoặc diệt 70 quái ở map 155/160/161', 70, 'Không có bang? Diệt 70 Khỉ lông xanh / Taburine Đỏ (map 155) hoặc Cabira / Tobi (map 160, 161)', -1, 148, 194),  -- TASK_43_3 = 88070  A2   Hạ Hatchiyack
(43, 'Hoàn thành Khí gas hủy diệt, hoặc diệt 100 quái ở map 155/160/161', 100, 'Không có bang? Diệt 100 Khỉ lông xanh / Taburine Đỏ (map 155) hoặc Cabira / Tobi (map 160, 161)', -1, 147, 195),  -- TASK_43_4 = 88072  B1   Hoàn thành Khí gas hủy diệt
(43, 'Báo cáo với Thượng Đế', 1, '', 19, 45, 196),  -- TASK_43_5 = 88074  A3   Báo cáo với Thượng Đế
(44, 'Nhờ Ôsin đưa tới hành tinh Bill', 1, '', 44, 50, 197),  -- TASK_44_0 = 90112  A3   Nhờ Ôsin đưa tới hành tinh Bill
(44, 'Nói chuyện với Bill', 1, '', 55, 154, 198),  -- TASK_44_1 = 90114  A3   Nói chuyện với Bill
(44, 'Thắng một trận đấu', 1, '', 21, 112, 199),  -- TASK_44_2 = 90116  B5   Thắng một trận đấu
(44, 'Thách đấu Whis', 1, '', 56, 154, 200),  -- TASK_44_3 = 90118  A2   Thách đấu Whis
(44, 'Mở giới hạn sức mạnh lần hai', 1, '', 43, 50, 201),  -- TASK_44_4 = 90120  B10  Mở giới hạn sức mạnh lần hai
(44, 'Nghe Whis dặn dò', 1, '', 56, 154, 202),  -- TASK_44_5 = 90122  A3   Nghe Whis dặn dò
(45, 'Tới Lãnh địa Fize', 1, '', -1, 78, 203),  -- TASK_45_0 = 92160  A6   Tới Lãnh địa Fize
(45, 'Nhặt Mảnh Ký Ức thứ bảy', 1, '', -1, 78, 204),  -- TASK_45_1 = 92162  A4   Nhặt Mảnh Ký Ức thứ bảy
(45, 'Làm lễ hợp nhất cùng một người khác', 1, 'Cần thêm một người chơi trong khu', 64, 78, 205),  -- TASK_45_2 = 92164  B13  Làm lễ hợp nhất cùng một người khác
(45, 'Hợp nhất bảy mảnh', 1, '', 64, 78, 206),  -- TASK_45_3 = 92166  A12  Hợp nhất bảy mảnh
(45, 'Nghe Thiên Sứ Whis giải thích', 1, '', 64, 78, 207),  -- TASK_45_4 = 92168  A3   Nghe Thiên Sứ Whis giải thích
(46, 'Nói chuyện với Dr. Myuu', 1, '', 83, 166, 208),  -- TASK_46_0 = 94208  A3   Nói chuyện với Dr. Myuu
(46, 'Hạ Heart', 1, '', -1, 166, 209),  -- TASK_46_1 = 94210  A2   Hạ Heart
(46, 'Đuổi theo Heart tới Võ Đài Siêu Cấp', 1, '', 64, 145, 210),  -- TASK_46_2 = 94212  A6   Đuổi theo Heart tới Võ Đài Siêu Cấp
(46, 'Hạ Heart Hư Không', 1, '', -1, 145, 211),  -- TASK_46_3 = 94214  A2   Hạ Heart Hư Không
(46, 'Hạ Heart Toàn Ký', 1, '', -1, 145, 212),  -- TASK_46_4 = 94216  A2   Hạ Heart Toàn Ký
(46, 'Nói chuyện với Thiên Sứ Whis', 1, '', 64, 145, 213),  -- TASK_46_5 = 94218  A3   Nói chuyện với Thiên Sứ Whis
(47, 'Quyết định số phận của Lõi', 1, '', 64, 145, 214),  -- TASK_47_0 = 96256  B14  Quyết định số phận của Lõi
(47, 'Trả Lõi Hư Không', 1, '', 64, 145, 215),  -- TASK_47_1 = 96258  A12  Trả Lõi Hư Không
(47, 'Lên Thánh địa Kaio', 1, '', -1, 50, 216),  -- TASK_47_2 = 96260  A6   Lên Thánh địa Kaio
(47, 'Mở giới hạn sức mạnh lần cuối', 1, '', 43, 50, 217),  -- TASK_47_3 = 96262  B10  Mở giới hạn sức mạnh lần cuối
(47, 'Nghe Tổ Sư Kaio nói lời cuối', 1, '', 43, 50, 218),  -- TASK_47_4 = 96264  A3   Nghe Tổ Sư Kaio nói lời cuối
(47, 'Hạ Hư Không Vô Danh', 1, '', -1, 145, 219),  -- TASK_47_5 = 96266  A2   Hạ Hư Không Vô Danh
(48, 'Tìm kẻ lạ ở Khu hang động', 1, '', 71, 160, 220),  -- TASK_48_0 = 98304  A3   Tìm kẻ lạ ở Khu hang động
(48, 'Chọn cách xử lý: Granola hay Jaco', 1, '', 71, 160, 221),  -- TASK_48_1 = 98306  B14  Chọn cách xử lý: Granola hay Jaco
(48, 'Trình báo cảnh sát vũ trụ Jaco', 1, '', 63, 24, 222),  -- TASK_48_2 = 98308  A3   Trình báo cảnh sát vũ trụ Jaco
(48, 'Thi hành lệnh truy nã 3 mục tiêu', 3, '', -1, -1, 223),  -- TASK_48_3 = 98310  A2   Thi hành lệnh truy nã 3 mục tiêu
(48, 'Nhặt 3 Biên bản truy nã', 3, '', -1, -1, 224),  -- TASK_48_4 = 98312  A4   Nhặt 3 Biên bản truy nã
(48, 'Nộp biên bản cho Jaco', 1, '', 63, 24, 225),  -- TASK_48_5 = 98314  A3   Nộp biên bản cho Jaco
(49, 'Nghe Potage nói sự thật', 1, '', 62, 140, 226),  -- TASK_49_0 = 100352 A3   Nghe Potage nói sự thật
(49, 'Quyết định số phận bản sao', 1, '', 62, 140, 227),  -- TASK_49_1 = 100354 B14  Quyết định số phận bản sao
(49, 'Tới Võ đài Xên bọ hung', 1, '', -1, 103, 228),  -- TASK_49_2 = 100356 A6   Tới Võ đài Xên bọ hung
(49, 'Gọi một nhân chứng vào võ đài', 1, 'Võ đài cần một nhân chứng', -1, 103, 229),  -- TASK_49_3 = 100358 B13  Gọi một nhân chứng vào võ đài
(49, 'Đánh gục Bản sao của ngươi', 1, '', -1, 103, 230),  -- TASK_49_4 = 100360 A2   Đánh gục Bản sao của ngươi
(49, 'Thu nhận nó bằng Bình chứa Commeson', 1, '', -1, 103, 231),  -- TASK_49_5 = 100362 A12  Thu nhận nó bằng Bình chứa Commeson
(49, 'Đạt 2.000.000.000 sức mạnh', 1, '', -1, -1, 232),  -- TASK_49_6 = 100364 A5   Đạt 2.000.000.000 sức mạnh
(50, 'Quyết định số phận của Lõi', 1, '', 64, 145, 233),  -- TASK_50_0 = 102400 B14  Quyết định số phận của Lõi
(50, 'Hấp thụ Lõi Hư Không', 1, '', 64, 145, 234),  -- TASK_50_1 = 102402 A12  Hấp thụ Lõi Hư Không
(50, 'Về Hành tinh ngục tù', 1, '', -1, 155, 235),  -- TASK_50_2 = 102404 A6   Về Hành tinh ngục tù
(50, 'Mở giới hạn sức mạnh lần cuối', 1, '', 43, 50, 236),  -- TASK_50_3 = 102406 B10  Mở giới hạn sức mạnh lần cuối
(50, 'Nghe Ôsin nói lời cuối', 1, '', 44, 155, 237),  -- TASK_50_4 = 102408 A3   Nghe Ôsin nói lời cuối
(50, 'Hạ Hư Không Vô Danh', 1, '', -1, 155, 238);  -- TASK_50_5 = 102410 A2   Hạ Hư Không Vô Danh

-- ---------------------------------------------------------------------
-- (4) BẢNG THƯỞNG MỚI — task_main_reward  (theo §9.2 của tài liệu 20)
--
--     sub_index = -1  -> thưởng khi hoàn thành CẢ nhiệm vụ (rewardDoneTask)
--     sub_index >= 0  -> thưởng khi xong đúng bước đó     (addDoneSubTask)
--
--     gender = -1     -> áp dụng cho cả 3 hành tinh
--     gender = 0/1/2  -> CHỈ Trái Đất / Namếc / Xayda
--     DAO phải cộng dồn mọi dòng có gender IN (-1, player.gender).
--     Dòng gender >= 0 chỉ chứa vật phẩm, sm/tn luôn để ở dòng gender = -1
--     để không cộng đúp sức mạnh.
--
--     items: JSON [[itemId, soLuong, [option...]], ...]
--     text : dòng mô tả hiển thị, sinh TỪ CHÍNH các cột trên (không gõ tay)
--
--     ⚠️ HAI ID ĐẶC BIỆT TRONG CỘT `items`: 2030 và 2031 là DANH HIỆU (TYPE 36),
--        KHÔNG phải item hành trang. DAO gặp hai id này phải làm:
--            new BadgesData(player, 257, 36500);   // 2030 -> 257 ; 2031 -> 258
--            BadgesService.turnOnBadges(player, 257);
--        và TUYỆT ĐỐI không gọi thêm player.dataBadges.add(...) (constructor
--        BadgesData đã tự add — gọi lần nữa là nhân đôi chỉ số).
--        Gọi addItemBag cho 2030/2031 => người chơi có item vô dụng, KHÔNG có danh hiệu.
-- ---------------------------------------------------------------------
DROP TABLE IF EXISTS `task_main_reward`;
CREATE TABLE `task_main_reward` (
  `task_id`   int(11)     NOT NULL,
  `sub_index` int(11)     NOT NULL DEFAULT -1,
  `gender`    tinyint(4)  NOT NULL DEFAULT -1,
  `sm`        bigint(20)  NOT NULL DEFAULT 0,
  `tn`        bigint(20)  NOT NULL DEFAULT 0,
  `gold`      bigint(20)  NOT NULL DEFAULT 0,
  `gem`       bigint(20)  NOT NULL DEFAULT 0,
  `ruby`      bigint(20)  NOT NULL DEFAULT 0,
  `items`     text        NOT NULL,
  `text`      varchar(500) NOT NULL DEFAULT '',
  PRIMARY KEY (`task_id`, `sub_index`, `gender`),
  KEY `task_id` (`task_id`) USING BTREE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci ROW_FORMAT=DYNAMIC;

INSERT INTO `task_main_reward`
(`task_id`, `sub_index`, `gender`, `sm`, `tn`, `gold`, `gem`, `ruby`, `items`, `text`) VALUES
(0, -1, -1, 2000, 2000, 0, 0, 0, '[[13,5,[]],[193,1,[]]]', 'Thưởng 2.000 sức mạnh. Thưởng 2.000 tiềm năng. Thưởng 5 Đậu thần cấp 1, 1 Gói 10 viên Capsule'),
(0, 0, -1, 200, 200, 0, 0, 0, '[]', 'Thưởng 200 sức mạnh. Thưởng 200 tiềm năng'),
(0, 1, -1, 200, 200, 0, 0, 0, '[]', 'Thưởng 200 sức mạnh. Thưởng 200 tiềm năng'),
(0, 2, -1, 200, 200, 0, 0, 0, '[]', 'Thưởng 200 sức mạnh. Thưởng 200 tiềm năng'),
(0, 3, -1, 200, 200, 0, 0, 0, '[]', 'Thưởng 200 sức mạnh. Thưởng 200 tiềm năng'),
(0, 4, -1, 200, 200, 0, 0, 0, '[]', 'Thưởng 200 sức mạnh. Thưởng 200 tiềm năng'),
(1, -1, -1, 3000, 3000, 0, 0, 0, '[[12,1,[]]]', 'Thưởng 3.000 sức mạnh. Thưởng 3.000 tiềm năng. Thưởng 1 Rada cấp 1'),
(1, 0, -1, 300, 300, 0, 0, 0, '[]', 'Thưởng 300 sức mạnh. Thưởng 300 tiềm năng'),
(1, 1, -1, 300, 300, 0, 0, 0, '[]', 'Thưởng 300 sức mạnh. Thưởng 300 tiềm năng'),
(1, 2, -1, 300, 300, 0, 0, 0, '[]', 'Thưởng 300 sức mạnh. Thưởng 300 tiềm năng'),
(2, -1, -1, 4000, 4000, 0, 0, 0, '[[13,10,[]]]', 'Thưởng 4.000 sức mạnh. Thưởng 4.000 tiềm năng. Thưởng 10 Đậu thần cấp 1'),
(2, 0, -1, 400, 400, 0, 0, 0, '[]', 'Thưởng 400 sức mạnh. Thưởng 400 tiềm năng'),
(2, 1, -1, 400, 400, 0, 0, 0, '[]', 'Thưởng 400 sức mạnh. Thưởng 400 tiềm năng'),
(2, 2, -1, 400, 400, 0, 0, 0, '[]', 'Thưởng 400 sức mạnh. Thưởng 400 tiềm năng'),
(3, -1, -1, 5000, 5000, 0, 0, 0, '[]', 'Thưởng 5.000 sức mạnh. Thưởng 5.000 tiềm năng. Thưởng 1 bộ trang bị cấp 1 theo hành tinh'),
(3, -1, 0, 0, 0, 0, 0, 0, '[[0,1,[]],[6,1,[]],[21,1,[]],[27,1,[]]]', '1 Áo vải 3 lỗ, 1 Quần vải đen, 1 Găng vải đen, 1 Giầy nhựa'),
(3, -1, 1, 0, 0, 0, 0, 0, '[[1,1,[]],[7,1,[]],[22,1,[]],[28,1,[]]]', '1 Áo sợi len, 1 Quần sợi len, 1 Găng sợi len, 1 Giầy sợi len'),
(3, -1, 2, 0, 0, 0, 0, 0, '[[2,1,[]],[8,1,[]],[23,1,[]],[29,1,[]]]', '1 Áo vải thô, 1 Quần vải thô, 1 Găng vải thô, 1 Giầy vải thô'),
(3, 0, -1, 500, 500, 0, 0, 0, '[]', 'Thưởng 500 sức mạnh. Thưởng 500 tiềm năng'),
(3, 1, -1, 500, 500, 0, 0, 0, '[]', 'Thưởng 500 sức mạnh. Thưởng 500 tiềm năng'),
(3, 2, -1, 500, 500, 0, 0, 0, '[]', 'Thưởng 500 sức mạnh. Thưởng 500 tiềm năng'),
(4, -1, -1, 6000, 6000, 0, 0, 0, '[[193,2,[]]]', 'Thưởng 6.000 sức mạnh. Thưởng 6.000 tiềm năng. Thưởng 2 Gói 10 viên Capsule'),
(4, 0, -1, 600, 600, 0, 0, 0, '[]', 'Thưởng 600 sức mạnh. Thưởng 600 tiềm năng'),
(4, 1, -1, 600, 600, 0, 0, 0, '[]', 'Thưởng 600 sức mạnh. Thưởng 600 tiềm năng'),
(4, 2, -1, 600, 600, 0, 0, 0, '[]', 'Thưởng 600 sức mạnh. Thưởng 600 tiềm năng'),
(5, -1, -1, 8000, 8000, 0, 0, 0, '[[57,1,[]],[13,10,[]]]', 'Thưởng 8.000 sức mạnh. Thưởng 8.000 tiềm năng. Thưởng 1 Rada cấp 2, 10 Đậu thần cấp 1'),
(5, 0, -1, 800, 800, 0, 0, 0, '[]', 'Thưởng 800 sức mạnh. Thưởng 800 tiềm năng'),
(5, 1, -1, 800, 800, 0, 0, 0, '[]', 'Thưởng 800 sức mạnh. Thưởng 800 tiềm năng'),
(5, 2, -1, 800, 800, 0, 0, 0, '[]', 'Thưởng 800 sức mạnh. Thưởng 800 tiềm năng'),
(6, -1, -1, 12000, 12000, 0, 0, 0, '[[295,1,[]]]', 'Thưởng 12.000 sức mạnh. Thưởng 12.000 tiềm năng. Thưởng 1 Gói 30 đậu thần cấp 3'),
(6, 0, -1, 1200, 1200, 0, 0, 0, '[]', 'Thưởng 1.200 sức mạnh. Thưởng 1.200 tiềm năng'),
(6, 1, -1, 1200, 1200, 0, 0, 0, '[]', 'Thưởng 1.200 sức mạnh. Thưởng 1.200 tiềm năng'),
(6, 2, -1, 1200, 1200, 0, 0, 0, '[]', 'Thưởng 1.200 sức mạnh. Thưởng 1.200 tiềm năng'),
(7, -1, -1, 20000, 20000, 0, 0, 0, '[[2002,1,[]],[193,75,[]]]', 'Thưởng 20.000 sức mạnh. Thưởng 20.000 tiềm năng. Thưởng 1 Mảnh Ký Ức 1, 75 Gói 10 viên Capsule'),
(7, 0, -1, 2000, 2000, 0, 0, 0, '[]', 'Thưởng 2.000 sức mạnh. Thưởng 2.000 tiềm năng'),
(7, 1, -1, 2000, 2000, 0, 0, 0, '[]', 'Thưởng 2.000 sức mạnh. Thưởng 2.000 tiềm năng'),
(7, 2, -1, 2000, 2000, 0, 0, 0, '[]', 'Thưởng 2.000 sức mạnh. Thưởng 2.000 tiềm năng'),
(8, -1, -1, 50000, 50000, 0, 0, 0, '[[2011,1,[]],[295,1,[]]]', 'Thưởng 50.000 sức mạnh. Thưởng 50.000 tiềm năng. Thưởng 1 Máy Dò Ký Ức, 1 Gói 30 đậu thần cấp 3'),
(8, 0, -1, 5000, 5000, 0, 0, 0, '[]', 'Thưởng 5.000 sức mạnh. Thưởng 5.000 tiềm năng'),
(8, 1, -1, 5000, 5000, 0, 0, 0, '[]', 'Thưởng 5.000 sức mạnh. Thưởng 5.000 tiềm năng'),
(8, 2, -1, 5000, 5000, 0, 0, 0, '[]', 'Thưởng 5.000 sức mạnh. Thưởng 5.000 tiềm năng'),
(9, -1, -1, 70000, 70000, 0, 0, 0, '[[58,1,[]]]', 'Thưởng 70.000 sức mạnh. Thưởng 70.000 tiềm năng. Thưởng 1 Rada cấp 3'),
(9, 0, -1, 7000, 7000, 0, 0, 0, '[]', 'Thưởng 7.000 sức mạnh. Thưởng 7.000 tiềm năng'),
(9, 1, -1, 7000, 7000, 0, 0, 0, '[]', 'Thưởng 7.000 sức mạnh. Thưởng 7.000 tiềm năng'),
(9, 2, -1, 7000, 7000, 0, 0, 0, '[]', 'Thưởng 7.000 sức mạnh. Thưởng 7.000 tiềm năng'),
(10, -1, -1, 100000, 100000, 0, 0, 0, '[]', 'Thưởng 100.000 sức mạnh. Thưởng 100.000 tiềm năng. Thưởng 1 sách đấm cấp 1 theo hành tinh'),
(10, -1, 0, 0, 0, 0, 0, 0, '[[66,1,[]]]', '1 Sách đấm Dragon lv1'),
(10, -1, 1, 0, 0, 0, 0, 0, '[[79,1,[]]]', '1 Sách đấm Demon lv1'),
(10, -1, 2, 0, 0, 0, 0, 0, '[[87,1,[]]]', '1 Sách đấm Galick lv1'),
(10, 0, -1, 10000, 10000, 0, 0, 0, '[]', 'Thưởng 10.000 sức mạnh. Thưởng 10.000 tiềm năng. Thưởng 1 sách chưởng cấp 1 theo hành tinh'),
(10, 0, 0, 0, 0, 0, 0, 0, '[[94,1,[]]]', '1 Sách Kamejoko lv1'),
(10, 0, 1, 0, 0, 0, 0, 0, '[[101,1,[]]]', '1 Sách Masenko lv1'),
(10, 0, 2, 0, 0, 0, 0, 0, '[[108,1,[]]]', '1 Sách Antomic lv1'),
(10, 1, -1, 10000, 10000, 0, 0, 0, '[]', 'Thưởng 10.000 sức mạnh. Thưởng 10.000 tiềm năng'),
(10, 2, -1, 10000, 10000, 0, 0, 0, '[]', 'Thưởng 10.000 sức mạnh. Thưởng 10.000 tiềm năng'),
(11, -1, -1, 140000, 140000, 0, 0, 0, '[[295,1,[]]]', 'Thưởng 140.000 sức mạnh. Thưởng 140.000 tiềm năng. Thưởng 1 Gói 30 đậu thần cấp 3'),
(11, 0, -1, 14000, 14000, 0, 0, 0, '[[2013,1,[]]]', 'Thưởng 14.000 sức mạnh. Thưởng 14.000 tiềm năng. Thưởng 1 Hạt Giống Hy Vọng'),
(11, 1, -1, 14000, 14000, 0, 0, 0, '[]', 'Thưởng 14.000 sức mạnh. Thưởng 14.000 tiềm năng'),
(11, 2, -1, 14000, 14000, 0, 0, 0, '[]', 'Thưởng 14.000 sức mạnh. Thưởng 14.000 tiềm năng'),
(12, -1, -1, 200000, 200000, 0, 0, 0, '[[401,1,[]],[402,1,[]]]', 'Thưởng 200.000 sức mạnh. Thưởng 200.000 tiềm năng. Thưởng 1 Đổi đệ tử, 1 Nâng kỹ năng 1 đệ tử'),
(12, 0, -1, 20000, 20000, 0, 0, 0, '[]', 'Thưởng 20.000 sức mạnh. Thưởng 20.000 tiềm năng'),
(12, 1, -1, 20000, 20000, 0, 0, 0, '[]', 'Thưởng 20.000 sức mạnh. Thưởng 20.000 tiềm năng'),
(12, 2, -1, 20000, 20000, 0, 0, 0, '[]', 'Thưởng 20.000 sức mạnh. Thưởng 20.000 tiềm năng'),
(13, -1, -1, 280000, 280000, 0, 0, 0, '[[295,2,[]],[987,2,[]]]', 'Thưởng 280.000 sức mạnh. Thưởng 280.000 tiềm năng. Thưởng 2 Gói 30 đậu thần cấp 3, 2 Đá bảo vệ'),
(13, 0, -1, 28000, 28000, 0, 0, 0, '[]', 'Thưởng 28.000 sức mạnh. Thưởng 28.000 tiềm năng'),
(13, 1, -1, 28000, 28000, 0, 0, 0, '[]', 'Thưởng 28.000 sức mạnh. Thưởng 28.000 tiềm năng'),
(13, 2, -1, 28000, 28000, 0, 0, 0, '[]', 'Thưởng 28.000 sức mạnh. Thưởng 28.000 tiềm năng'),
(14, -1, -1, 400000, 400000, 0, 0, 0, '[[2012,1,[]],[1074,10,[]],[987,3,[]]]', 'Thưởng 400.000 sức mạnh. Thưởng 400.000 tiềm năng. Thưởng 1 Hộp Ký Ức Bị Đánh Cắp, 10 Đá nâng cấp cấp 1, 3 Đá bảo vệ'),
(14, 0, -1, 40000, 40000, 0, 0, 0, '[]', 'Thưởng 40.000 sức mạnh. Thưởng 40.000 tiềm năng'),
(14, 1, -1, 40000, 40000, 0, 0, 0, '[]', 'Thưởng 40.000 sức mạnh. Thưởng 40.000 tiềm năng'),
(14, 2, -1, 40000, 40000, 0, 0, 0, '[]', 'Thưởng 40.000 sức mạnh. Thưởng 40.000 tiềm năng'),
(15, -1, -1, 600000, 600000, 0, 0, 0, '[[2003,1,[]],[295,1,[]],[1074,5,[]]]', 'Thưởng 600.000 sức mạnh. Thưởng 600.000 tiềm năng. Thưởng 1 Mảnh Ký Ức 2, 1 Gói 30 đậu thần cấp 3, 5 Đá nâng cấp cấp 1'),
(15, 0, -1, 60000, 60000, 0, 0, 0, '[]', 'Thưởng 60.000 sức mạnh. Thưởng 60.000 tiềm năng'),
(15, 1, -1, 60000, 60000, 0, 0, 0, '[]', 'Thưởng 60.000 sức mạnh. Thưởng 60.000 tiềm năng'),
(15, 2, -1, 60000, 60000, 0, 0, 0, '[]', 'Thưởng 60.000 sức mạnh. Thưởng 60.000 tiềm năng'),
(16, -1, -1, 3000000, 3000000, 0, 0, 0, '[[1074,20,[]]]', 'Thưởng 3.000.000 sức mạnh. Thưởng 3.000.000 tiềm năng. Thưởng 20 Đá nâng cấp cấp 1'),
(16, 0, -1, 300000, 300000, 0, 0, 0, '[]', 'Thưởng 300.000 sức mạnh. Thưởng 300.000 tiềm năng'),
(16, 1, -1, 300000, 300000, 0, 0, 0, '[]', 'Thưởng 300.000 sức mạnh. Thưởng 300.000 tiềm năng'),
(16, 2, -1, 300000, 300000, 0, 0, 0, '[]', 'Thưởng 300.000 sức mạnh. Thưởng 300.000 tiềm năng'),
(16, 3, -1, 300000, 300000, 0, 0, 0, '[]', 'Thưởng 300.000 sức mạnh. Thưởng 300.000 tiềm năng'),
(16, 4, -1, 300000, 300000, 0, 0, 0, '[]', 'Thưởng 300.000 sức mạnh. Thưởng 300.000 tiềm năng'),
(17, -1, -1, 5000000, 5000000, 0, 0, 0, '[[1074,30,[]],[987,3,[]]]', 'Thưởng 5.000.000 sức mạnh. Thưởng 5.000.000 tiềm năng. Thưởng 30 Đá nâng cấp cấp 1, 3 Đá bảo vệ'),
(17, 0, -1, 500000, 500000, 0, 0, 0, '[[223,10,[]],[222,10,[]],[987,2,[]],[2015,1,[]]]', 'Thưởng 500.000 sức mạnh. Thưởng 500.000 tiềm năng. Thưởng 10 Đá Titan, 10 Đá Ruby, 2 Đá bảo vệ, 1 Búa rèn cũ'),
(17, 1, -1, 500000, 500000, 0, 0, 0, '[]', 'Thưởng 500.000 sức mạnh. Thưởng 500.000 tiềm năng'),
(17, 2, -1, 500000, 500000, 0, 0, 0, '[]', 'Thưởng 500.000 sức mạnh. Thưởng 500.000 tiềm năng'),
(17, 3, -1, 500000, 500000, 0, 0, 0, '[]', 'Thưởng 500.000 sức mạnh. Thưởng 500.000 tiềm năng'),
(18, -1, -1, 6000000, 6000000, 0, 0, 0, '[[1075,20,[]]]', 'Thưởng 6.000.000 sức mạnh. Thưởng 6.000.000 tiềm năng. Thưởng 20 Đá nâng cấp cấp 2'),
(18, 0, -1, 600000, 600000, 0, 0, 0, '[]', 'Thưởng 600.000 sức mạnh. Thưởng 600.000 tiềm năng'),
(18, 1, -1, 600000, 600000, 0, 0, 0, '[]', 'Thưởng 600.000 sức mạnh. Thưởng 600.000 tiềm năng'),
(18, 2, -1, 600000, 600000, 0, 0, 0, '[]', 'Thưởng 600.000 sức mạnh. Thưởng 600.000 tiềm năng'),
(18, 3, -1, 600000, 600000, 0, 0, 0, '[]', 'Thưởng 600.000 sức mạnh. Thưởng 600.000 tiềm năng'),
(18, 4, -1, 600000, 600000, 0, 0, 0, '[]', 'Thưởng 600.000 sức mạnh. Thưởng 600.000 tiềm năng'),
(19, -1, -1, 7000000, 7000000, 0, 0, 0, '[[1075,30,[]]]', 'Thưởng 7.000.000 sức mạnh. Thưởng 7.000.000 tiềm năng. Thưởng 30 Đá nâng cấp cấp 2'),
(19, 0, -1, 700000, 700000, 0, 0, 0, '[]', 'Thưởng 700.000 sức mạnh. Thưởng 700.000 tiềm năng'),
(19, 1, -1, 700000, 700000, 0, 0, 0, '[]', 'Thưởng 700.000 sức mạnh. Thưởng 700.000 tiềm năng'),
(19, 2, -1, 700000, 700000, 0, 0, 0, '[]', 'Thưởng 700.000 sức mạnh. Thưởng 700.000 tiềm năng'),
(19, 3, -1, 700000, 700000, 0, 0, 0, '[]', 'Thưởng 700.000 sức mạnh. Thưởng 700.000 tiềm năng'),
(19, 4, -1, 700000, 700000, 0, 0, 0, '[]', 'Thưởng 700.000 sức mạnh. Thưởng 700.000 tiềm năng'),
(20, -1, -1, 8000000, 8000000, 0, 0, 0, '[[1075,40,[]]]', 'Thưởng 8.000.000 sức mạnh. Thưởng 8.000.000 tiềm năng. Thưởng 40 Đá nâng cấp cấp 2'),
(20, 0, -1, 800000, 800000, 0, 0, 0, '[]', 'Thưởng 800.000 sức mạnh. Thưởng 800.000 tiềm năng'),
(20, 1, -1, 800000, 800000, 0, 0, 0, '[]', 'Thưởng 800.000 sức mạnh. Thưởng 800.000 tiềm năng'),
(20, 2, -1, 800000, 800000, 0, 0, 0, '[]', 'Thưởng 800.000 sức mạnh. Thưởng 800.000 tiềm năng'),
(20, 3, -1, 800000, 800000, 0, 0, 0, '[]', 'Thưởng 800.000 sức mạnh. Thưởng 800.000 tiềm năng'),
(20, 4, -1, 800000, 800000, 0, 0, 0, '[]', 'Thưởng 800.000 sức mạnh. Thưởng 800.000 tiềm năng'),
(20, 5, -1, 800000, 800000, 0, 0, 0, '[]', 'Thưởng 800.000 sức mạnh. Thưởng 800.000 tiềm năng'),
(21, -1, -1, 9000000, 9000000, 0, 0, 0, '[[1076,20,[]],[611,2,[]]]', 'Thưởng 9.000.000 sức mạnh. Thưởng 9.000.000 tiềm năng. Thưởng 20 Đá nâng cấp cấp 3, 2 Bản đồ kho báu'),
(21, 0, -1, 900000, 900000, 0, 0, 0, '[]', 'Thưởng 900.000 sức mạnh. Thưởng 900.000 tiềm năng'),
(21, 1, -1, 900000, 900000, 0, 0, 0, '[]', 'Thưởng 900.000 sức mạnh. Thưởng 900.000 tiềm năng'),
(21, 2, -1, 900000, 900000, 0, 0, 0, '[]', 'Thưởng 900.000 sức mạnh. Thưởng 900.000 tiềm năng'),
(21, 3, -1, 900000, 900000, 0, 0, 0, '[]', 'Thưởng 900.000 sức mạnh. Thưởng 900.000 tiềm năng'),
(22, -1, -1, 11000000, 11000000, 0, 0, 0, '[[1076,30,[]]]', 'Thưởng 11.000.000 sức mạnh. Thưởng 11.000.000 tiềm năng. Thưởng 30 Đá nâng cấp cấp 3'),
(22, 0, -1, 1100000, 1100000, 0, 0, 0, '[]', 'Thưởng 1.100.000 sức mạnh. Thưởng 1.100.000 tiềm năng'),
(22, 1, -1, 1100000, 1100000, 0, 0, 0, '[]', 'Thưởng 1.100.000 sức mạnh. Thưởng 1.100.000 tiềm năng'),
(22, 2, -1, 1100000, 1100000, 0, 0, 0, '[]', 'Thưởng 1.100.000 sức mạnh. Thưởng 1.100.000 tiềm năng'),
(22, 3, -1, 1100000, 1100000, 0, 0, 0, '[]', 'Thưởng 1.100.000 sức mạnh. Thưởng 1.100.000 tiềm năng'),
(22, 4, -1, 1100000, 1100000, 0, 0, 0, '[]', 'Thưởng 1.100.000 sức mạnh. Thưởng 1.100.000 tiềm năng'),
(23, -1, -1, 16000000, 16000000, 0, 0, 0, '[[1076,50,[]],[987,5,[]],[2004,1,[]]]', 'Thưởng 16.000.000 sức mạnh. Thưởng 16.000.000 tiềm năng. Thưởng 50 Đá nâng cấp cấp 3, 5 Đá bảo vệ, 1 Mảnh Ký Ức 3'),
(23, 0, -1, 1600000, 1600000, 0, 0, 0, '[]', 'Thưởng 1.600.000 sức mạnh. Thưởng 1.600.000 tiềm năng'),
(23, 1, -1, 1600000, 1600000, 0, 0, 0, '[]', 'Thưởng 1.600.000 sức mạnh. Thưởng 1.600.000 tiềm năng'),
(23, 2, -1, 1600000, 1600000, 0, 0, 0, '[]', 'Thưởng 1.600.000 sức mạnh. Thưởng 1.600.000 tiềm năng'),
(23, 3, -1, 1600000, 1600000, 0, 0, 0, '[]', 'Thưởng 1.600.000 sức mạnh. Thưởng 1.600.000 tiềm năng'),
(23, 4, -1, 1600000, 1600000, 0, 0, 0, '[]', 'Thưởng 1.600.000 sức mạnh. Thưởng 1.600.000 tiềm năng'),
(23, 5, -1, 1600000, 1600000, 0, 0, 0, '[]', 'Thưởng 1.600.000 sức mạnh. Thưởng 1.600.000 tiềm năng'),
(24, -1, -1, 80000000, 80000000, 0, 0, 0, '[[1077,20,[]]]', 'Thưởng 80.000.000 sức mạnh. Thưởng 80.000.000 tiềm năng. Thưởng 20 Đá nâng cấp cấp 4'),
(24, 0, -1, 8000000, 8000000, 0, 0, 0, '[]', 'Thưởng 8.000.000 sức mạnh. Thưởng 8.000.000 tiềm năng'),
(24, 1, -1, 8000000, 8000000, 0, 0, 0, '[]', 'Thưởng 8.000.000 sức mạnh. Thưởng 8.000.000 tiềm năng'),
(24, 2, -1, 8000000, 8000000, 0, 0, 0, '[]', 'Thưởng 8.000.000 sức mạnh. Thưởng 8.000.000 tiềm năng'),
(24, 3, -1, 8000000, 8000000, 0, 0, 0, '[]', 'Thưởng 8.000.000 sức mạnh. Thưởng 8.000.000 tiềm năng'),
(24, 4, -1, 8000000, 8000000, 0, 0, 0, '[]', 'Thưởng 8.000.000 sức mạnh. Thưởng 8.000.000 tiềm năng'),
(25, -1, -1, 100000000, 100000000, 0, 0, 0, '[[1077,30,[]]]', 'Thưởng 100.000.000 sức mạnh. Thưởng 100.000.000 tiềm năng. Thưởng 30 Đá nâng cấp cấp 4'),
(25, 0, -1, 10000000, 10000000, 0, 0, 0, '[]', 'Thưởng 10.000.000 sức mạnh. Thưởng 10.000.000 tiềm năng'),
(25, 1, -1, 10000000, 10000000, 0, 0, 0, '[]', 'Thưởng 10.000.000 sức mạnh. Thưởng 10.000.000 tiềm năng'),
(25, 2, -1, 10000000, 10000000, 0, 0, 0, '[]', 'Thưởng 10.000.000 sức mạnh. Thưởng 10.000.000 tiềm năng'),
(25, 3, -1, 10000000, 10000000, 0, 0, 0, '[]', 'Thưởng 10.000.000 sức mạnh. Thưởng 10.000.000 tiềm năng'),
(26, -1, -1, 130000000, 130000000, 0, 0, 0, '[[447,2,[]],[674,3,[]]]', 'Thưởng 130.000.000 sức mạnh. Thưởng 130.000.000 tiềm năng. Thưởng 2 Sao pha lê lục, 3 Đá ngũ sắc'),
(26, 0, -1, 13000000, 13000000, 0, 0, 0, '[[674,6,[]],[447,2,[]],[2020,1,[]]]', 'Thưởng 13.000.000 sức mạnh. Thưởng 13.000.000 tiềm năng. Thưởng 6 Đá ngũ sắc, 2 Sao pha lê lục, 1 Mẫu kim loại có ký ức'),
(26, 1, -1, 13000000, 13000000, 0, 0, 0, '[]', 'Thưởng 13.000.000 sức mạnh. Thưởng 13.000.000 tiềm năng'),
(26, 2, -1, 13000000, 13000000, 0, 0, 0, '[]', 'Thưởng 13.000.000 sức mạnh. Thưởng 13.000.000 tiềm năng'),
(26, 3, -1, 13000000, 13000000, 0, 0, 0, '[]', 'Thưởng 13.000.000 sức mạnh. Thưởng 13.000.000 tiềm năng'),
(26, 4, -1, 13000000, 13000000, 0, 0, 0, '[]', 'Thưởng 13.000.000 sức mạnh. Thưởng 13.000.000 tiềm năng'),
(27, -1, -1, 150000000, 150000000, 0, 0, 0, '[[1077,40,[]]]', 'Thưởng 150.000.000 sức mạnh. Thưởng 150.000.000 tiềm năng. Thưởng 40 Đá nâng cấp cấp 4'),
(27, 0, -1, 15000000, 15000000, 0, 0, 0, '[]', 'Thưởng 15.000.000 sức mạnh. Thưởng 15.000.000 tiềm năng'),
(27, 1, -1, 15000000, 15000000, 0, 0, 0, '[]', 'Thưởng 15.000.000 sức mạnh. Thưởng 15.000.000 tiềm năng'),
(27, 2, -1, 15000000, 15000000, 0, 0, 0, '[]', 'Thưởng 15.000.000 sức mạnh. Thưởng 15.000.000 tiềm năng'),
(27, 3, -1, 15000000, 15000000, 0, 0, 0, '[]', 'Thưởng 15.000.000 sức mạnh. Thưởng 15.000.000 tiềm năng'),
(28, -1, -1, 180000000, 180000000, 0, 0, 0, '[[1078,20,[]]]', 'Thưởng 180.000.000 sức mạnh. Thưởng 180.000.000 tiềm năng. Thưởng 20 Đá nâng cấp cấp 5'),
(28, 0, -1, 18000000, 18000000, 0, 0, 0, '[]', 'Thưởng 18.000.000 sức mạnh. Thưởng 18.000.000 tiềm năng'),
(28, 1, -1, 18000000, 18000000, 0, 0, 0, '[]', 'Thưởng 18.000.000 sức mạnh. Thưởng 18.000.000 tiềm năng'),
(28, 2, -1, 18000000, 18000000, 0, 0, 0, '[]', 'Thưởng 18.000.000 sức mạnh. Thưởng 18.000.000 tiềm năng'),
(28, 3, -1, 18000000, 18000000, 0, 0, 0, '[]', 'Thưởng 18.000.000 sức mạnh. Thưởng 18.000.000 tiềm năng'),
(28, 4, -1, 18000000, 18000000, 0, 0, 0, '[]', 'Thưởng 18.000.000 sức mạnh. Thưởng 18.000.000 tiềm năng'),
(29, -1, -1, 200000000, 200000000, 0, 0, 0, '[[1078,25,[]],[987,5,[]]]', 'Thưởng 200.000.000 sức mạnh. Thưởng 200.000.000 tiềm năng. Thưởng 25 Đá nâng cấp cấp 5, 5 Đá bảo vệ'),
(29, 0, -1, 20000000, 20000000, 0, 0, 0, '[[2022,1,[]]]', 'Thưởng 20.000.000 sức mạnh. Thưởng 20.000.000 tiềm năng. Thưởng 1 Thẻ từ phòng thí nghiệm'),
(29, 1, -1, 20000000, 20000000, 0, 0, 0, '[]', 'Thưởng 20.000.000 sức mạnh. Thưởng 20.000.000 tiềm năng'),
(29, 2, -1, 20000000, 20000000, 0, 0, 0, '[]', 'Thưởng 20.000.000 sức mạnh. Thưởng 20.000.000 tiềm năng'),
(29, 3, -1, 20000000, 20000000, 0, 0, 0, '[]', 'Thưởng 20.000.000 sức mạnh. Thưởng 20.000.000 tiềm năng'),
(30, -1, -1, 220000000, 220000000, 0, 0, 0, '[[1078,30,[]],[446,2,[]]]', 'Thưởng 220.000.000 sức mạnh. Thưởng 220.000.000 tiềm năng. Thưởng 30 Đá nâng cấp cấp 5, 2 Sao pha lê vàng'),
(30, 0, -1, 22000000, 22000000, 0, 0, 0, '[]', 'Thưởng 22.000.000 sức mạnh. Thưởng 22.000.000 tiềm năng'),
(30, 1, -1, 22000000, 22000000, 0, 0, 0, '[]', 'Thưởng 22.000.000 sức mạnh. Thưởng 22.000.000 tiềm năng'),
(30, 2, -1, 22000000, 22000000, 0, 0, 0, '[]', 'Thưởng 22.000.000 sức mạnh. Thưởng 22.000.000 tiềm năng'),
(30, 3, -1, 22000000, 22000000, 0, 0, 0, '[]', 'Thưởng 22.000.000 sức mạnh. Thưởng 22.000.000 tiềm năng'),
(30, 4, -1, 22000000, 22000000, 0, 0, 0, '[]', 'Thưởng 22.000.000 sức mạnh. Thưởng 22.000.000 tiềm năng'),
(31, -1, -1, 230000000, 230000000, 0, 0, 0, '[[1078,50,[]],[445,2,[]],[2005,1,[]]]', 'Thưởng 230.000.000 sức mạnh. Thưởng 230.000.000 tiềm năng. Thưởng 50 Đá nâng cấp cấp 5, 2 Sao pha lê cam, 1 Mảnh Ký Ức 4'),
(31, 0, -1, 23000000, 23000000, 0, 0, 0, '[]', 'Thưởng 23.000.000 sức mạnh. Thưởng 23.000.000 tiềm năng'),
(31, 1, -1, 23000000, 23000000, 0, 0, 0, '[]', 'Thưởng 23.000.000 sức mạnh. Thưởng 23.000.000 tiềm năng'),
(31, 2, -1, 23000000, 23000000, 0, 0, 0, '[]', 'Thưởng 23.000.000 sức mạnh. Thưởng 23.000.000 tiềm năng'),
(31, 3, -1, 23000000, 23000000, 0, 0, 0, '[]', 'Thưởng 23.000.000 sức mạnh. Thưởng 23.000.000 tiềm năng'),
(31, 4, -1, 23000000, 23000000, 0, 0, 0, '[]', 'Thưởng 23.000.000 sức mạnh. Thưởng 23.000.000 tiềm năng'),
(31, 5, -1, 23000000, 23000000, 0, 0, 0, '[]', 'Thưởng 23.000.000 sức mạnh. Thưởng 23.000.000 tiềm năng'),
(32, -1, -1, 200000000, 200000000, 0, 0, 0, '[[352,30,[]]]', 'Thưởng 200.000.000 sức mạnh. Thưởng 200.000.000 tiềm năng. Thưởng 30 Đậu thần cấp 8'),
(32, 0, -1, 20000000, 20000000, 0, 0, 0, '[[992,1,[]]]', 'Thưởng 20.000.000 sức mạnh. Thưởng 20.000.000 tiềm năng. Thưởng 1 Nhẫn thời không sai lệch'),
(32, 1, -1, 20000000, 20000000, 0, 0, 0, '[]', 'Thưởng 20.000.000 sức mạnh. Thưởng 20.000.000 tiềm năng'),
(32, 2, -1, 20000000, 20000000, 0, 0, 0, '[]', 'Thưởng 20.000.000 sức mạnh. Thưởng 20.000.000 tiềm năng'),
(32, 3, -1, 20000000, 20000000, 0, 0, 0, '[]', 'Thưởng 20.000.000 sức mạnh. Thưởng 20.000.000 tiềm năng'),
(32, 4, -1, 20000000, 20000000, 0, 0, 0, '[]', 'Thưởng 20.000.000 sức mạnh. Thưởng 20.000.000 tiềm năng'),
(32, 5, -1, 20000000, 20000000, 0, 0, 0, '[]', 'Thưởng 20.000.000 sức mạnh. Thưởng 20.000.000 tiềm năng'),
(33, -1, -1, 250000000, 250000000, 0, 0, 0, '[[352,50,[]],[987,5,[]]]', 'Thưởng 250.000.000 sức mạnh. Thưởng 250.000.000 tiềm năng. Thưởng 50 Đậu thần cấp 8, 5 Đá bảo vệ'),
(33, 0, -1, 25000000, 25000000, 0, 0, 0, '[]', 'Thưởng 25.000.000 sức mạnh. Thưởng 25.000.000 tiềm năng'),
(33, 1, -1, 25000000, 25000000, 0, 0, 0, '[]', 'Thưởng 25.000.000 sức mạnh. Thưởng 25.000.000 tiềm năng'),
(33, 2, -1, 25000000, 25000000, 0, 0, 0, '[]', 'Thưởng 25.000.000 sức mạnh. Thưởng 25.000.000 tiềm năng'),
(33, 3, -1, 25000000, 25000000, 0, 0, 0, '[]', 'Thưởng 25.000.000 sức mạnh. Thưởng 25.000.000 tiềm năng'),
(33, 4, -1, 25000000, 25000000, 0, 0, 0, '[]', 'Thưởng 25.000.000 sức mạnh. Thưởng 25.000.000 tiềm năng'),
(33, 5, -1, 25000000, 25000000, 0, 0, 0, '[]', 'Thưởng 25.000.000 sức mạnh. Thưởng 25.000.000 tiềm năng'),
(34, -1, -1, 300000000, 300000000, 0, 0, 0, '[[454,1,[]]]', 'Thưởng 300.000.000 sức mạnh. Thưởng 300.000.000 tiềm năng. Thưởng 1 Bông tai Porata'),
(34, 0, -1, 30000000, 30000000, 0, 0, 0, '[]', 'Thưởng 30.000.000 sức mạnh. Thưởng 30.000.000 tiềm năng'),
(34, 1, -1, 30000000, 30000000, 0, 0, 0, '[]', 'Thưởng 30.000.000 sức mạnh. Thưởng 30.000.000 tiềm năng'),
(34, 2, -1, 30000000, 30000000, 0, 0, 0, '[]', 'Thưởng 30.000.000 sức mạnh. Thưởng 30.000.000 tiềm năng'),
(34, 3, -1, 30000000, 30000000, 0, 0, 0, '[]', 'Thưởng 30.000.000 sức mạnh. Thưởng 30.000.000 tiềm năng'),
(34, 4, -1, 30000000, 30000000, 0, 0, 0, '[]', 'Thưởng 30.000.000 sức mạnh. Thưởng 30.000.000 tiềm năng'),
(34, 5, -1, 30000000, 30000000, 0, 0, 0, '[]', 'Thưởng 30.000.000 sức mạnh. Thưởng 30.000.000 tiềm năng'),
(35, -1, -1, 350000000, 350000000, 0, 0, 0, '[[1078,5,[]]]', 'Thưởng 350.000.000 sức mạnh. Thưởng 350.000.000 tiềm năng. Thưởng 5 Đá nâng cấp cấp 5'),
(35, 0, -1, 35000000, 35000000, 0, 0, 0, '[]', 'Thưởng 35.000.000 sức mạnh. Thưởng 35.000.000 tiềm năng'),
(35, 1, -1, 35000000, 35000000, 0, 0, 0, '[]', 'Thưởng 35.000.000 sức mạnh. Thưởng 35.000.000 tiềm năng'),
(35, 2, -1, 35000000, 35000000, 0, 0, 0, '[]', 'Thưởng 35.000.000 sức mạnh. Thưởng 35.000.000 tiềm năng'),
(35, 3, -1, 35000000, 35000000, 0, 0, 0, '[]', 'Thưởng 35.000.000 sức mạnh. Thưởng 35.000.000 tiềm năng'),
(35, 4, -1, 35000000, 35000000, 0, 0, 0, '[]', 'Thưởng 35.000.000 sức mạnh. Thưởng 35.000.000 tiềm năng'),
(36, -1, -1, 400000000, 400000000, 0, 0, 0, '[[352,50,[]]]', 'Thưởng 400.000.000 sức mạnh. Thưởng 400.000.000 tiềm năng. Thưởng 50 Đậu thần cấp 8'),
(36, 0, -1, 40000000, 40000000, 0, 0, 0, '[[1795,1,[]]]', 'Thưởng 40.000.000 sức mạnh. Thưởng 40.000.000 tiềm năng. Thưởng 1 Bình hút năng lượng'),
(36, 1, -1, 40000000, 40000000, 0, 0, 0, '[]', 'Thưởng 40.000.000 sức mạnh. Thưởng 40.000.000 tiềm năng'),
(36, 2, -1, 40000000, 40000000, 0, 0, 0, '[]', 'Thưởng 40.000.000 sức mạnh. Thưởng 40.000.000 tiềm năng'),
(36, 3, -1, 40000000, 40000000, 0, 0, 0, '[]', 'Thưởng 40.000.000 sức mạnh. Thưởng 40.000.000 tiềm năng'),
(36, 4, -1, 40000000, 40000000, 0, 0, 0, '[]', 'Thưởng 40.000.000 sức mạnh. Thưởng 40.000.000 tiềm năng'),
(37, -1, -1, 450000000, 450000000, 0, 0, 0, '[[921,1,[]]]', 'Thưởng 450.000.000 sức mạnh. Thưởng 450.000.000 tiềm năng. Thưởng 1 Bông tai Porata'),
(37, 0, -1, 45000000, 45000000, 0, 0, 0, '[]', 'Thưởng 45.000.000 sức mạnh. Thưởng 45.000.000 tiềm năng'),
(37, 1, -1, 45000000, 45000000, 0, 0, 0, '[]', 'Thưởng 45.000.000 sức mạnh. Thưởng 45.000.000 tiềm năng'),
(37, 2, -1, 45000000, 45000000, 0, 0, 0, '[]', 'Thưởng 45.000.000 sức mạnh. Thưởng 45.000.000 tiềm năng'),
(37, 3, -1, 45000000, 45000000, 0, 0, 0, '[]', 'Thưởng 45.000.000 sức mạnh. Thưởng 45.000.000 tiềm năng'),
(37, 4, -1, 45000000, 45000000, 0, 0, 0, '[]', 'Thưởng 45.000.000 sức mạnh. Thưởng 45.000.000 tiềm năng'),
(38, -1, -1, 550000000, 550000000, 0, 0, 0, '[[447,2,[]]]', 'Thưởng 550.000.000 sức mạnh. Thưởng 550.000.000 tiềm năng. Thưởng 2 Sao pha lê lục'),
(38, 0, -1, 55000000, 55000000, 0, 0, 0, '[]', 'Thưởng 55.000.000 sức mạnh. Thưởng 55.000.000 tiềm năng'),
(38, 1, -1, 55000000, 55000000, 0, 0, 0, '[]', 'Thưởng 55.000.000 sức mạnh. Thưởng 55.000.000 tiềm năng'),
(38, 2, -1, 55000000, 55000000, 0, 0, 0, '[]', 'Thưởng 55.000.000 sức mạnh. Thưởng 55.000.000 tiềm năng'),
(38, 3, -1, 55000000, 55000000, 0, 0, 0, '[]', 'Thưởng 55.000.000 sức mạnh. Thưởng 55.000.000 tiềm năng'),
(38, 4, -1, 55000000, 55000000, 0, 0, 0, '[]', 'Thưởng 55.000.000 sức mạnh. Thưởng 55.000.000 tiềm năng'),
(38, 5, -1, 55000000, 55000000, 0, 0, 0, '[]', 'Thưởng 55.000.000 sức mạnh. Thưởng 55.000.000 tiềm năng'),
(39, -1, -1, 1000000000, 1000000000, 0, 0, 0, '[[1819,1,[]],[352,50,[]]]', 'Thưởng 1.000.000.000 sức mạnh. Thưởng 1.000.000.000 tiềm năng. Thưởng 1 Bông tai Porata, 50 Đậu thần cấp 8'),
(39, 0, -1, 100000000, 100000000, 0, 0, 0, '[]', 'Thưởng 100.000.000 sức mạnh. Thưởng 100.000.000 tiềm năng'),
(39, 1, -1, 100000000, 100000000, 0, 0, 0, '[]', 'Thưởng 100.000.000 sức mạnh. Thưởng 100.000.000 tiềm năng'),
(39, 2, -1, 100000000, 100000000, 0, 0, 0, '[]', 'Thưởng 100.000.000 sức mạnh. Thưởng 100.000.000 tiềm năng'),
(39, 3, -1, 100000000, 100000000, 0, 0, 0, '[[2006,1,[]]]', 'Thưởng 100.000.000 sức mạnh. Thưởng 100.000.000 tiềm năng. Thưởng 1 Mảnh Ký Ức 5'),
(39, 4, -1, 100000000, 100000000, 0, 0, 0, '[]', 'Thưởng 100.000.000 sức mạnh. Thưởng 100.000.000 tiềm năng'),
(39, 5, -1, 100000000, 100000000, 0, 0, 0, '[[2007,1,[]]]', 'Thưởng 100.000.000 sức mạnh. Thưởng 100.000.000 tiềm năng. Thưởng 1 Mảnh Ký Ức 6'),
(39, 6, -1, 100000000, 100000000, 0, 0, 0, '[]', 'Thưởng 100.000.000 sức mạnh. Thưởng 100.000.000 tiềm năng'),
(40, -1, -1, 550000000, 550000000, 0, 0, 0, '[[1066,150,[]],[352,50,[]]]', 'Thưởng 550.000.000 sức mạnh. Thưởng 550.000.000 tiềm năng. Thưởng 150 Mảnh áo, 50 Đậu thần cấp 8'),
(40, 0, -1, 55000000, 55000000, 0, 0, 0, '[]', 'Thưởng 55.000.000 sức mạnh. Thưởng 55.000.000 tiềm năng'),
(40, 1, -1, 55000000, 55000000, 0, 0, 0, '[]', 'Thưởng 55.000.000 sức mạnh. Thưởng 55.000.000 tiềm năng'),
(40, 2, -1, 55000000, 55000000, 0, 0, 0, '[]', 'Thưởng 55.000.000 sức mạnh. Thưởng 55.000.000 tiềm năng'),
(40, 3, -1, 55000000, 55000000, 0, 0, 0, '[]', 'Thưởng 55.000.000 sức mạnh. Thưởng 55.000.000 tiềm năng'),
(40, 4, -1, 55000000, 55000000, 0, 0, 0, '[]', 'Thưởng 55.000.000 sức mạnh. Thưởng 55.000.000 tiềm năng'),
(41, -1, -1, 650000000, 650000000, 0, 0, 0, '[[1067,150,[]],[352,50,[]]]', 'Thưởng 650.000.000 sức mạnh. Thưởng 650.000.000 tiềm năng. Thưởng 150 Mảnh quần, 50 Đậu thần cấp 8'),
(41, 0, -1, 65000000, 65000000, 0, 0, 0, '[]', 'Thưởng 65.000.000 sức mạnh. Thưởng 65.000.000 tiềm năng'),
(41, 1, -1, 65000000, 65000000, 0, 0, 0, '[]', 'Thưởng 65.000.000 sức mạnh. Thưởng 65.000.000 tiềm năng'),
(41, 2, -1, 65000000, 65000000, 0, 0, 0, '[]', 'Thưởng 65.000.000 sức mạnh. Thưởng 65.000.000 tiềm năng'),
(41, 3, -1, 65000000, 65000000, 0, 0, 0, '[]', 'Thưởng 65.000.000 sức mạnh. Thưởng 65.000.000 tiềm năng'),
(41, 4, -1, 65000000, 65000000, 0, 0, 0, '[]', 'Thưởng 65.000.000 sức mạnh. Thưởng 65.000.000 tiềm năng'),
(42, -1, -1, 750000000, 750000000, 0, 0, 0, '[[1070,150,[]],[352,50,[]]]', 'Thưởng 750.000.000 sức mạnh. Thưởng 750.000.000 tiềm năng. Thưởng 150 Mảnh găng tay, 50 Đậu thần cấp 8'),
(42, 0, -1, 75000000, 75000000, 0, 0, 0, '[]', 'Thưởng 75.000.000 sức mạnh. Thưởng 75.000.000 tiềm năng'),
(42, 1, -1, 75000000, 75000000, 0, 0, 0, '[]', 'Thưởng 75.000.000 sức mạnh. Thưởng 75.000.000 tiềm năng'),
(42, 2, -1, 75000000, 75000000, 0, 0, 0, '[]', 'Thưởng 75.000.000 sức mạnh. Thưởng 75.000.000 tiềm năng'),
(42, 3, -1, 75000000, 75000000, 0, 0, 0, '[]', 'Thưởng 75.000.000 sức mạnh. Thưởng 75.000.000 tiềm năng'),
(42, 4, -1, 75000000, 75000000, 0, 0, 0, '[]', 'Thưởng 75.000.000 sức mạnh. Thưởng 75.000.000 tiềm năng'),
(42, 5, -1, 75000000, 75000000, 0, 0, 0, '[]', 'Thưởng 75.000.000 sức mạnh. Thưởng 75.000.000 tiềm năng'),
(43, -1, -1, 850000000, 850000000, 0, 0, 0, '[[1068,150,[]],[352,50,[]]]', 'Thưởng 850.000.000 sức mạnh. Thưởng 850.000.000 tiềm năng. Thưởng 150 Mảnh giầy, 50 Đậu thần cấp 8'),
(43, 0, -1, 85000000, 85000000, 0, 0, 0, '[]', 'Thưởng 85.000.000 sức mạnh. Thưởng 85.000.000 tiềm năng'),
(43, 1, -1, 85000000, 85000000, 0, 0, 0, '[]', 'Thưởng 85.000.000 sức mạnh. Thưởng 85.000.000 tiềm năng'),
(43, 2, -1, 85000000, 85000000, 0, 0, 0, '[]', 'Thưởng 85.000.000 sức mạnh. Thưởng 85.000.000 tiềm năng'),
(43, 3, -1, 85000000, 85000000, 0, 0, 0, '[]', 'Thưởng 85.000.000 sức mạnh. Thưởng 85.000.000 tiềm năng'),
(43, 4, -1, 85000000, 85000000, 0, 0, 0, '[]', 'Thưởng 85.000.000 sức mạnh. Thưởng 85.000.000 tiềm năng'),
(43, 5, -1, 85000000, 85000000, 0, 0, 0, '[]', 'Thưởng 85.000.000 sức mạnh. Thưởng 85.000.000 tiềm năng'),
(44, -1, -1, 900000000, 900000000, 0, 0, 0, '[[1069,150,[]],[352,50,[]]]', 'Thưởng 900.000.000 sức mạnh. Thưởng 900.000.000 tiềm năng. Thưởng 150 Mảnh nhẫn, 50 Đậu thần cấp 8'),
(44, 0, -1, 90000000, 90000000, 0, 0, 0, '[]', 'Thưởng 90.000.000 sức mạnh. Thưởng 90.000.000 tiềm năng'),
(44, 1, -1, 90000000, 90000000, 0, 0, 0, '[]', 'Thưởng 90.000.000 sức mạnh. Thưởng 90.000.000 tiềm năng'),
(44, 2, -1, 90000000, 90000000, 0, 0, 0, '[]', 'Thưởng 90.000.000 sức mạnh. Thưởng 90.000.000 tiềm năng'),
(44, 3, -1, 90000000, 90000000, 0, 0, 0, '[]', 'Thưởng 90.000.000 sức mạnh. Thưởng 90.000.000 tiềm năng'),
(44, 4, -1, 90000000, 90000000, 0, 0, 0, '[]', 'Thưởng 90.000.000 sức mạnh. Thưởng 90.000.000 tiềm năng'),
(44, 5, -1, 90000000, 90000000, 0, 0, 0, '[]', 'Thưởng 90.000.000 sức mạnh. Thưởng 90.000.000 tiềm năng'),
(45, -1, -1, 950000000, 950000000, 0, 0, 0, '[[674,5,[]],[352,50,[]]]', 'Thưởng 950.000.000 sức mạnh. Thưởng 950.000.000 tiềm năng. Thưởng 5 Đá ngũ sắc, 50 Đậu thần cấp 8'),
(45, 0, -1, 95000000, 95000000, 0, 0, 0, '[]', 'Thưởng 95.000.000 sức mạnh. Thưởng 95.000.000 tiềm năng'),
(45, 1, -1, 95000000, 95000000, 0, 0, 0, '[[2024,1,[]]]', 'Thưởng 95.000.000 sức mạnh. Thưởng 95.000.000 tiềm năng. Thưởng 1 Lõi Ký Ức chưa hoàn chỉnh'),
(45, 2, -1, 95000000, 95000000, 0, 0, 0, '[]', 'Thưởng 95.000.000 sức mạnh. Thưởng 95.000.000 tiềm năng'),
(45, 3, -1, 95000000, 95000000, 0, 0, 0, '[[2000,1,[]]]', 'Thưởng 95.000.000 sức mạnh. Thưởng 95.000.000 tiềm năng. Thưởng 1 Lõi Hư Không'),
(45, 4, -1, 95000000, 95000000, 0, 0, 0, '[]', 'Thưởng 95.000.000 sức mạnh. Thưởng 95.000.000 tiềm năng'),
(46, -1, -1, 1100000000, 1100000000, 0, 0, 0, '[[674,5,[]],[987,10,[]],[352,60,[]]]', 'Thưởng 1.100.000.000 sức mạnh. Thưởng 1.100.000.000 tiềm năng. Thưởng 5 Đá ngũ sắc, 10 Đá bảo vệ, 60 Đậu thần cấp 8'),
(46, 0, -1, 110000000, 110000000, 0, 0, 0, '[]', 'Thưởng 110.000.000 sức mạnh. Thưởng 110.000.000 tiềm năng'),
(46, 1, -1, 110000000, 110000000, 0, 0, 0, '[]', 'Thưởng 110.000.000 sức mạnh. Thưởng 110.000.000 tiềm năng'),
(46, 2, -1, 110000000, 110000000, 0, 0, 0, '[]', 'Thưởng 110.000.000 sức mạnh. Thưởng 110.000.000 tiềm năng'),
(46, 3, -1, 110000000, 110000000, 0, 0, 0, '[]', 'Thưởng 110.000.000 sức mạnh. Thưởng 110.000.000 tiềm năng'),
(46, 4, -1, 110000000, 110000000, 0, 0, 0, '[]', 'Thưởng 110.000.000 sức mạnh. Thưởng 110.000.000 tiềm năng'),
(46, 5, -1, 110000000, 110000000, 0, 0, 0, '[]', 'Thưởng 110.000.000 sức mạnh. Thưởng 110.000.000 tiềm năng'),
(47, -1, -1, 1500000000, 1500000000, 0, 0, 0, '[[2030,1,[]],[987,20,[]],[352,99,[]],[674,10,[]]]', 'Thưởng 1.500.000.000 sức mạnh. Thưởng 1.500.000.000 tiềm năng. Thưởng 1 Người Trả Ký Ức, 20 Đá bảo vệ, 99 Đậu thần cấp 8, 10 Đá ngũ sắc'),
(47, 0, -1, 150000000, 150000000, 0, 0, 0, '[]', 'Thưởng 150.000.000 sức mạnh. Thưởng 150.000.000 tiềm năng'),
(47, 1, -1, 150000000, 150000000, 0, 0, 0, '[[2001,1,[]]]', 'Thưởng 150.000.000 sức mạnh. Thưởng 150.000.000 tiềm năng. Thưởng 1 Vỏ Lõi rỗng'),
(47, 2, -1, 150000000, 150000000, 0, 0, 0, '[]', 'Thưởng 150.000.000 sức mạnh. Thưởng 150.000.000 tiềm năng'),
(47, 3, -1, 150000000, 150000000, 0, 0, 0, '[]', 'Thưởng 150.000.000 sức mạnh. Thưởng 150.000.000 tiềm năng'),
(47, 4, -1, 150000000, 150000000, 0, 0, 0, '[]', 'Thưởng 150.000.000 sức mạnh. Thưởng 150.000.000 tiềm năng'),
(47, 5, -1, 150000000, 150000000, 0, 0, 0, '[]', 'Thưởng 150.000.000 sức mạnh. Thưởng 150.000.000 tiềm năng'),
(48, -1, -1, 8000000, 8000000, 0, 0, 0, '[[1074,20,[]],[987,8,[]]]', 'Thưởng 8.000.000 sức mạnh. Thưởng 8.000.000 tiềm năng. Thưởng 20 Đá nâng cấp cấp 1, 8 Đá bảo vệ'),
(48, 0, -1, 800000, 800000, 0, 0, 0, '[]', 'Thưởng 800.000 sức mạnh. Thưởng 800.000 tiềm năng'),
(48, 1, -1, 800000, 800000, 0, 0, 0, '[]', 'Thưởng 800.000 sức mạnh. Thưởng 800.000 tiềm năng'),
(48, 2, -1, 800000, 800000, 0, 0, 0, '[]', 'Thưởng 800.000 sức mạnh. Thưởng 800.000 tiềm năng'),
(48, 3, -1, 800000, 800000, 0, 0, 0, '[]', 'Thưởng 800.000 sức mạnh. Thưởng 800.000 tiềm năng'),
(48, 4, -1, 800000, 800000, 0, 0, 0, '[]', 'Thưởng 800.000 sức mạnh. Thưởng 800.000 tiềm năng'),
(48, 5, -1, 800000, 800000, 0, 0, 0, '[]', 'Thưởng 800.000 sức mạnh. Thưởng 800.000 tiềm năng'),
(49, -1, -1, 230000000, 230000000, 0, 0, 0, '[[447,3,[]],[674,5,[]],[987,10,[]],[2005,1,[]]]', 'Thưởng 230.000.000 sức mạnh. Thưởng 230.000.000 tiềm năng. Thưởng 3 Sao pha lê lục, 5 Đá ngũ sắc, 10 Đá bảo vệ, 1 Mảnh Ký Ức 4'),
(49, 0, -1, 23000000, 23000000, 0, 0, 0, '[]', 'Thưởng 23.000.000 sức mạnh. Thưởng 23.000.000 tiềm năng'),
(49, 1, -1, 23000000, 23000000, 0, 0, 0, '[]', 'Thưởng 23.000.000 sức mạnh. Thưởng 23.000.000 tiềm năng'),
(49, 2, -1, 23000000, 23000000, 0, 0, 0, '[]', 'Thưởng 23.000.000 sức mạnh. Thưởng 23.000.000 tiềm năng'),
(49, 3, -1, 23000000, 23000000, 0, 0, 0, '[]', 'Thưởng 23.000.000 sức mạnh. Thưởng 23.000.000 tiềm năng'),
(49, 4, -1, 23000000, 23000000, 0, 0, 0, '[]', 'Thưởng 23.000.000 sức mạnh. Thưởng 23.000.000 tiềm năng'),
(49, 5, -1, 23000000, 23000000, 0, 0, 0, '[]', 'Thưởng 23.000.000 sức mạnh. Thưởng 23.000.000 tiềm năng'),
(49, 6, -1, 23000000, 23000000, 0, 0, 0, '[]', 'Thưởng 23.000.000 sức mạnh. Thưởng 23.000.000 tiềm năng'),
(50, -1, -1, 1500000000, 1500000000, 0, 0, 0, '[[2031,1,[]],[987,20,[]],[352,99,[]],[674,10,[]]]', 'Thưởng 1.500.000.000 sức mạnh. Thưởng 1.500.000.000 tiềm năng. Thưởng 1 Kẻ Giữ Hư Không, 20 Đá bảo vệ, 99 Đậu thần cấp 8, 10 Đá ngũ sắc'),
(50, 1, -1, 150000000, 150000000, 0, 0, 0, '[]', 'Thưởng 150.000.000 sức mạnh. Thưởng 150.000.000 tiềm năng'),
(50, 2, -1, 150000000, 150000000, 0, 0, 0, '[]', 'Thưởng 150.000.000 sức mạnh. Thưởng 150.000.000 tiềm năng'),
(50, 3, -1, 150000000, 150000000, 0, 0, 0, '[]', 'Thưởng 150.000.000 sức mạnh. Thưởng 150.000.000 tiềm năng'),
(50, 4, -1, 150000000, 150000000, 0, 0, 0, '[]', 'Thưởng 150.000.000 sức mạnh. Thưởng 150.000.000 tiềm năng'),
(50, 5, -1, 150000000, 150000000, 0, 0, 0, '[]', 'Thưởng 150.000.000 sức mạnh. Thưởng 150.000.000 tiềm năng');

COMMIT;

-- Đặt lại bộ đếm AUTO_INCREMENT (ALTER tự commit ngầm nên để NGOÀI transaction).
ALTER TABLE `task_sub_template` AUTO_INCREMENT = 239;

-- =====================================================================
-- (5) KIỂM TRA SAU KHI IMPORT — chạy cả 8 câu, TẤT CẢ phải đúng kỳ vọng.
--     Sai bất kỳ câu nào thì ĐỪNG khởi động server.
-- =====================================================================

-- 5.1 Đếm nhiệm vụ.  KỲ VỌNG: so_nhiem_vu = 51, nho_nhat = 0, lon_nhat = 50
SELECT COUNT(*) AS so_nhiem_vu, MIN(id) AS nho_nhat, MAX(id) AS lon_nhat
FROM `task_main_template`;

-- 5.2 Đếm bước con.  KỲ VỌNG: so_buoc = 238
SELECT COUNT(*) AS so_buoc FROM `task_sub_template`;

-- 5.3 Không nhiệm vụ nào được thiếu bước.  KỲ VỌNG: 0 dòng trả về.
SELECT m.id, m.`NAME`
FROM `task_main_template` m
LEFT JOIN `task_sub_template` s ON s.task_main_id = m.id
GROUP BY m.id, m.`NAME`
HAVING COUNT(s.ducvupro) = 0;

-- 5.4 Không được có bước mồ côi (trỏ tới nhiệm vụ không tồn tại).
--     KỲ VỌNG: 0 dòng trả về.
SELECT DISTINCT s.task_main_id
FROM `task_sub_template` s
LEFT JOIN `task_main_template` m ON m.id = s.task_main_id
WHERE m.id IS NULL;

-- 5.5 Số bước từng nhiệm vụ + thứ tự ducvupro phải liền mạch trong mỗi nhiệm vụ.
--     KỲ VỌNG: 0 dòng trả về (không nhiệm vụ nào bị chèn bước của nhiệm vụ khác
--     vào giữa — Manager gom bước theo thứ tự trả về, không theo index).
SELECT task_main_id,
       COUNT(*)                          AS so_buoc,
       MAX(ducvupro) - MIN(ducvupro) + 1 AS khoang_ducvupro
FROM `task_sub_template`
GROUP BY task_main_id
HAVING COUNT(*) <> MAX(ducvupro) - MIN(ducvupro) + 1;

-- 5.6 Bảng thưởng KHÔNG được có vàng / ngọc / hồng ngọc.  KỲ VỌNG: 0 dòng.
SELECT task_id, sub_index, gender, gold, gem, ruby
FROM `task_main_reward`
WHERE gold <> 0 OR gem <> 0 OR ruby <> 0;

-- 5.7 Mọi nhiệm vụ phải có ĐÚNG 1 dòng thưởng hoàn thành (sub_index = -1,
--     gender = -1), và mọi bước phải có dòng thưởng tương ứng.
--     KỲ VỌNG: 0 dòng trả về.
SELECT m.id, 'thieu dong thuong hoan thanh' AS loi
FROM `task_main_template` m
LEFT JOIN `task_main_reward` r
       ON r.task_id = m.id AND r.sub_index = -1 AND r.gender = -1
WHERE r.task_id IS NULL
UNION ALL
SELECT r.task_id, 'thua dong thuong cho buoc khong ton tai'
FROM `task_main_reward` r
JOIN (SELECT task_main_id, COUNT(*) AS n
      FROM `task_sub_template` GROUP BY task_main_id) c
  ON c.task_main_id = r.task_id
WHERE r.sub_index >= c.n;

-- 5.8 MỌI item id trong bảng thưởng phải tồn tại trong item_template.
--     KỲ VỌNG: 0 dòng trả về.
--     (Chạy được từ MySQL 8.0 / MariaDB 10.6 nhờ JSON_TABLE.)
SELECT DISTINCT jt.item_id
FROM `task_main_reward` r
JOIN JSON_TABLE(r.items, '$[*]' COLUMNS (item_id INT PATH '$[0]')) jt
  ON TRUE
LEFT JOIN `item_template` it ON it.id = jt.item_id
WHERE it.id IS NULL;

-- 5.8b Bản thay thế cho MySQL 5.7 (không có JSON_TABLE) — liệt kê thủ công
--      danh sách id đang dùng rồi đối chiếu. KỲ VỌNG: 0 dòng trả về.
SELECT x.id AS item_id_khong_ton_tai
FROM (SELECT 0 AS id UNION ALL SELECT 1 AS id UNION ALL SELECT 2 AS id UNION ALL SELECT 6 AS id UNION ALL SELECT 7 AS id UNION ALL SELECT 8 AS id UNION ALL SELECT 12 AS id UNION ALL SELECT 13 AS id UNION ALL SELECT 21 AS id UNION ALL SELECT 22 AS id UNION ALL SELECT 23 AS id UNION ALL SELECT 27 AS id UNION ALL SELECT 28 AS id UNION ALL SELECT 29 AS id UNION ALL SELECT 57 AS id UNION ALL SELECT 58 AS id UNION ALL SELECT 66 AS id UNION ALL SELECT 79 AS id UNION ALL SELECT 87 AS id UNION ALL SELECT 94 AS id UNION ALL SELECT 101 AS id UNION ALL SELECT 108 AS id UNION ALL SELECT 193 AS id UNION ALL SELECT 222 AS id UNION ALL SELECT 223 AS id UNION ALL SELECT 295 AS id UNION ALL SELECT 352 AS id UNION ALL SELECT 401 AS id UNION ALL SELECT 402 AS id UNION ALL SELECT 445 AS id UNION ALL SELECT 446 AS id UNION ALL SELECT 447 AS id UNION ALL SELECT 454 AS id UNION ALL SELECT 611 AS id UNION ALL SELECT 674 AS id UNION ALL SELECT 921 AS id UNION ALL SELECT 987 AS id UNION ALL SELECT 992 AS id UNION ALL SELECT 1066 AS id UNION ALL SELECT 1067 AS id UNION ALL SELECT 1068 AS id UNION ALL SELECT 1069 AS id UNION ALL SELECT 1070 AS id UNION ALL SELECT 1074 AS id UNION ALL SELECT 1075 AS id UNION ALL SELECT 1076 AS id UNION ALL SELECT 1077 AS id UNION ALL SELECT 1078 AS id UNION ALL SELECT 1795 AS id UNION ALL SELECT 1819 AS id UNION ALL SELECT 2000 AS id UNION ALL SELECT 2001 AS id UNION ALL SELECT 2002 AS id UNION ALL SELECT 2003 AS id UNION ALL SELECT 2004 AS id UNION ALL SELECT 2005 AS id UNION ALL SELECT 2006 AS id UNION ALL SELECT 2007 AS id UNION ALL SELECT 2011 AS id UNION ALL SELECT 2012 AS id UNION ALL SELECT 2013 AS id UNION ALL SELECT 2015 AS id UNION ALL SELECT 2020 AS id UNION ALL SELECT 2022 AS id UNION ALL SELECT 2024 AS id UNION ALL SELECT 2030 AS id UNION ALL SELECT 2031 AS id) x
LEFT JOIN `item_template` it ON it.id = x.id
WHERE it.id IS NULL;

-- =====================================================================
-- (6) GỠ BỎ — chỉ dùng khi cần lùi lại.
--     Phải nạp lại dump tuyến cũ, file này KHÔNG giữ bản sao dữ liệu cũ.
-- =====================================================================
-- START TRANSACTION;
-- DROP TABLE IF EXISTS `task_main_reward`;
-- DELETE FROM `task_sub_template`;
-- DELETE FROM `task_main_template`;
-- COMMIT;
-- SOURCE backup_task_YYYY-MM-DD.sql;
