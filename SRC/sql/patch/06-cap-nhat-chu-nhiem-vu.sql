-- =====================================================================
-- 06-cap-nhat-chu-nhiem-vu.sql  —  VIẾT LẠI CHỮ BẢNG NHIỆM VỤ (51 NV / 237 bước)
-- Database: team2026        Chạy trên: MariaDB 10.4 (chỉ UPDATE / SELECT cơ bản)
-- Tài liệu: docs/4-trien-khai/40-chu-bang-nhiem-vu.md
--
-- Vì sao: client tự nối tiến độ "(x/y)" vào cuối dòng bước hiện tại, khung
-- chỉ hiện ~46 ký tự/dòng. Tên bước cũ dài tới 35+ ký tự nên "(0/12)" bị đẩy
-- ra ngoài khung. File này rút tên bước về <= 28 ký tự (tính cả placeholder ở
-- phương án dài nhất), viết lại mô tả gọn 3 phần và câu nhắc.
--
-- DÀNH CHO SERVER ĐÃ IMPORT 02 + 05. Server cài mới đã có chữ mới trong 02/05,
-- chạy thêm file này cũng không sao.
--
-- AN TOÀN:
--   * CHỈ có UPDATE các cột chữ: task_main_template.NAME / detail,
--     task_sub_template.NAME / notify. Không DELETE, không INSERT.
--   * KHÔNG đụng max_count, npc_id, map, ducvupro, bảng thưởng, data_task
--     của người chơi => tiến độ người chơi giữ nguyên.
--   * Chạy lại bao nhiêu lần cũng cho cùng một kết quả.
--
-- CÁCH CHẠY:
--   mysql -u root -p team2026 < SRC/sql/patch/06-cap-nhat-chu-nhiem-vu.sql
--   (hoặc dán vào tab SQL của phpMyAdmin). Sau đó KHỞI ĐỘNG LẠI server —
--   Manager chỉ nạp nhiệm vụ lúc khởi động. Không cần build lại jar.
--   Có thể chạy khi server đang bật; chữ mới hiện sau lần khởi động kế tiếp.
-- =====================================================================

SET NAMES utf8mb4;
START TRANSACTION;

-- ---------------------------------------------------------------------
-- (1) task_main_template — tên + mô tả (51 dòng, khớp theo id)
-- ---------------------------------------------------------------------
UPDATE `task_main_template` SET `NAME` = 'Người duy nhất còn nhớ', `detail` = 'Ngươi tỉnh dậy bên vách núi, trong ngực le lói ánh sáng lạ.
Về nhà %2, lấy rađa trong rương rồi hái đậu thần.
Thưởng: 2.000 SM, 2.000 TN, 5 Đậu thần cấp 1, 1 Gói Capsule'
WHERE `id` = 0;
UPDATE `task_main_template` SET `NAME` = 'Bài học của ông', `detail` = 'Ông quên tên ngươi, nhưng tay ông vẫn nhớ cách dạy võ.
Đánh ngã 5 mộc nhân ở %1 rồi về khoe với %2.
Thưởng: 3.000 SM, 3.000 TN, 1 Rada cấp 1'
WHERE `id` = 1;
UPDATE `task_main_template` SET `NAME` = 'Vết nứt đầu tiên', `detail` = 'Trời trên %3 rách toạc, lũ %4 phát điên phá nát ruộng làng.
Hạ %4, nhặt 10 đùi gà mang về cho %2.
Thưởng: 4.000 SM, 4.000 TN, 10 Đậu thần cấp 1'
WHERE `id` = 2;
UPDATE `task_main_template` SET `NAME` = 'Cảnh sát vũ trụ Jaco', `detail` = 'Tiếng nổ vang từ vách núi, có thứ gì vừa rơi xuống.
Cộng tiềm năng, tìm vật thể lạ rồi mang về cho %2.
Thưởng: 5.000 SM, 5.000 TN, 1 bộ đồ vải cấp 1'
WHERE `id` = 3;
UPDATE `task_main_template` SET `NAME` = 'Thứ bò ra từ vết nứt', `detail` = 'Lũ thú ở %6 biến dạng, mắt trắng dã như bị rút hồn.
Dọn bầy %4 ở %6 rồi về kể cho %2.
Thưởng: 6.000 SM, 6.000 TN, 2 Gói Capsule'
WHERE `id` = 4;
UPDATE `task_main_template` SET `NAME` = 'Ký ức của ông', `detail` = 'Ký ức bị rút đi luôn để lại một mỏ neo: kỷ vật của ông.
Tìm kỷ vật rơi từ %9, lau sạch rồi đưa cho %2.
Thưởng: 8.000 SM, 8.000 TN, 1 Rada cấp 2, 10 Đậu thần cấp 1'
WHERE `id` = 5;
UPDATE `task_main_template` SET `NAME` = 'Người thu gom', `detail` = 'Kẻ áo choàng xám đang hút thứ trong suốt ra khỏi xác thú.
Hạ %9 trong rừng, diệt Kẻ Thu Gom rồi báo cho %2.
Thưởng: 12.000 SM, 12.000 TN, 1 Gói 30 đậu cấp 3'
WHERE `id` = 6;
UPDATE `task_main_template` SET `NAME` = 'Chạy khỏi vết nứt', `detail` = 'Vết nứt bắt đầu nuốt cả khu rừng. Không còn thời gian!
Hạ 10 quái mẹ trong 3 phút, rồi chạy tới Trạm tàu vũ trụ gặp Jaco.
Thưởng: 20.000 SM, 20.000 TN, 1 Mảnh Ký Ức 1, 75 Gói Capsule'
WHERE `id` = 7;
UPDATE `task_main_template` SET `NAME` = 'Máy dò ký ức', `detail` = 'Bunma chế được máy dò ký ức, chỉ còn thiếu vật liệu.
Gặp Bunma ở Siêu Thị, mua Rada cấp 1, hạ quái mẹ lấy lõi.
Thưởng: 50.000 SM, 50.000 TN, 1 Máy Dò Ký Ức, 1 Gói 30 đậu cấp 3'
WHERE `id` = 8;
UPDATE `task_main_template` SET `NAME` = 'Chuyến bay đầu tiên', `detail` = 'Máy dò chỉ về người già nhất hành tinh còn tỉnh táo.
Bay tới %11, dọn %12 rồi gặp %10.
Thưởng: 70.000 SM, 70.000 TN, 1 Rada cấp 3'
WHERE `id` = 9;
UPDATE `task_main_template` SET `NAME` = 'Bái sư', `detail` = 'Sư phụ nhận ngươi vì ông sắp quên, còn ngươi thì chưa.
Bái %10 làm thầy, học chưởng cấp 1, đạt 250.000 sức mạnh.
Thưởng: 100.000 SM, 100.000 TN, 1 sách đấm lv1'
WHERE `id` = 10;
UPDATE `task_main_template` SET `NAME` = 'Hạt giống hy vọng', `detail` = 'Cây đậu nhà ngươi sắp chết, vì đất cũng quên cách nuôi nó.
Hái 5 hạt đậu, gieo Hạt Giống Hy Vọng rồi khoe với %2.
Thưởng: 140.000 SM, 140.000 TN, 1 Gói 30 đậu cấp 3'
WHERE `id` = 11;
UPDATE `task_main_template` SET `NAME` = 'Bạn đồng hành', `detail` = 'Quả trứng ông để dành cho ai đó bỗng nứt ra.
Nở trứng nhận đệ tử, cùng nó hạ quái mẹ rồi về gặp %2.
Thưởng: 200.000 SM, 200.000 TN, 1 Đổi đệ tử, 1 Nâng kỹ năng 1 đệ tử'
WHERE `id` = 12;
UPDATE `task_main_template` SET `NAME` = 'Không ai đi một mình', `detail` = 'Ký ức một người thì dễ lấy, ký ức cả bang thì khó nuốt.
Vào bang, cùng bạn bang hạ quái mẹ rồi gặp Giu-ma Đầu Bò.
Thưởng: 280.000 SM, 280.000 TN, 2 Gói 30 đậu cấp 3, 2 Đá bảo vệ'
WHERE `id` = 13;
UPDATE `task_main_template` SET `NAME` = 'Chợ đen ký ức', `detail` = 'Có kẻ đang đóng hộp ký ức đem bán như hàng hóa.
Gặp Bunma, mua một món ở quầy Uron, chặn đoàn heo chở hàng.
Thưởng: 400.000 SM, 400.000 TN, 1 Hộp Ký Ức Bị Đánh Cắp, 10 Đá nâng cấp 1, 3 Đá bảo vệ'
WHERE `id` = 14;
UPDATE `task_main_template` SET `NAME` = 'Người bạn đã quên', `detail` = 'Chữ trên vận đơn là của Jaco, nhưng Jaco đã quên ngươi.
Tới điểm hẹn, đánh bại Jaco mất ký ức rồi gặp lại hắn.
Thưởng: 600.000 SM, 600.000 TN, 1 Mảnh Ký Ức 2, 1 Gói 30 đậu cấp 3, 5 Đá nâng cấp 1'
WHERE `id` = 15;
UPDATE `task_main_template` SET `NAME` = 'Dấu vết dẫn về phía Nam', `detail` = 'Máy dò ký ức rung lên, kim chỉ thẳng về phía Nam.
Dọn quái phía Nam, nhặt 5 Vỏ đạn khắc dấu rồi về gặp %10.
Thưởng: 3 triệu SM, 3 triệu TN, 20 Đá nâng cấp 1'
WHERE `id` = 16;
UPDATE `task_main_template` SET `NAME` = 'Rèn lại vũ khí', `detail` = 'Bà Hạt Mít bảo vũ khí cũng biết quên, phải rèn lại cho nó nhớ.
Gặp Bà Hạt Mít, nâng 1 trang bị lên +2 rồi dùng Búa rèn cũ.
Thưởng: 5 triệu SM, 5 triệu TN, 30 Đá nâng cấp 1, 3 Đá bảo vệ'
WHERE `id` = 17;
UPDATE `task_main_template` SET `NAME` = 'Tapion', `detail` = 'Ở Thành phố Vegeta có một người mà không ai nhớ nổi.
Tìm Tapion, giải vây thành rồi cùng anh tới Thành phố Santa.
Thưởng: 6 triệu SM, 6 triệu TN, 20 Đá nâng cấp 2'
WHERE `id` = 18;
UPDATE `task_main_template` SET `NAME` = 'Trại lính hoang', `detail` = 'Trại lính Nappa đã mất trí và tấn công bất cứ ai.
Gặp Cui, dọn sạch trại lính, rủ thêm một người hạ Appule.
Thưởng: 7 triệu SM, 7 triệu TN, 30 Đá nâng cấp 2'
WHERE `id` = 19;
UPDATE `task_main_template` SET `NAME` = 'Kẻ săn tiền thưởng', `detail` = 'Granola rủ ngươi săn ba tay chân của Fide, không cần giấy phép.
Chọn phe ở Khu hang động; theo Granola thì hạ 3 tay chân Fide.
Thưởng: 8 triệu SM, 8 triệu TN, 40 Đá nâng cấp 2'
WHERE `id` = 20;
UPDATE `task_main_template` SET `NAME` = 'Doanh trại Độc Nhãn', `detail` = 'Bản đồ hành quân của Fide nằm trong Doanh trại Độc Nhãn.
Phá doanh trại cùng bang, hoặc một mình hạ 300 quái Trại lính Fide.
Thưởng: 9 triệu SM, 9 triệu TN, 20 Đá nâng cấp 3, 2 Bản đồ kho báu'
WHERE `id` = 21;
UPDATE `task_main_template` SET `NAME` = 'Tiểu đội sát thủ', `detail` = 'Heart mua chuộc Tiểu đội sát thủ để săn người còn ký ức.
Hạ trọn Tiểu đội sát thủ, lấy Máy đo ký ức đưa cho Tapion.
Thưởng: 11 triệu SM, 11 triệu TN, 30 Đá nâng cấp 3'
WHERE `id` = 22;
UPDATE `task_main_template` SET `NAME` = 'Fide đại ca', `detail` = 'Fide gom ký ức cả một vùng để bán cho Heart.
Đốt kho tiếp tế ở Núi khỉ vàng, rồi hạ Fide cả ba dạng.
Thưởng: 16 triệu SM, 16 triệu TN, 50 Đá nâng cấp 3, 5 Đá bảo vệ, 1 Mảnh Ký Ức 3'
WHERE `id` = 23;
UPDATE `task_main_template` SET `NAME` = 'Tín hiệu lạ từ phương Bắc', `detail` = 'Máy dò bắt được sóng cơ khí phát lại chính ký ức của ngươi.
Lần theo sóng, dọn Xên con ở phía đông rồi báo Bunma.
Thưởng: 80 triệu SM, 80 triệu TN, 20 Đá nâng cấp 4'
WHERE `id` = 24;
UPDATE `task_main_template` SET `NAME` = 'Android đầu tiên', `detail` = 'Hai cỗ máy đầu tiên của Dr. Myuu vẫn còn nguyên dữ liệu.
Hạ Android 19 và Dr.Kôrê, mang 3 lõi năng lượng về cho Bunma.
Thưởng: 100 triệu SM, 100 triệu TN, 30 Đá nâng cấp 4'
WHERE `id` = 25;
UPDATE `task_main_template` SET `NAME` = 'Kim loại và ký ức', `detail` = 'Sắt thì quên, pha lê thì nhớ.
Học Bà Hạt Mít pha lê hóa, ép sao, rồi kể lại cho %10.
Thưởng: 130 triệu SM, 130 triệu TN, 2 Sao pha lê lục, 3 Đá ngũ sắc'
WHERE `id` = 26;
UPDATE `task_main_template` SET `NAME` = 'Ba cỗ máy', `detail` = 'Ba cỗ máy mẫu không đánh để thắng, chúng đánh để ghi hình ngươi.
Hỏi Ca Lích, tới sân sau siêu thị hạ Android 13, 14, 15.
Thưởng: 150 triệu SM, 150 triệu TN, 40 Đá nâng cấp 4'
WHERE `id` = 27;
UPDATE `task_main_template` SET `NAME` = 'King Kong', `detail` = 'King Kong không phải máy, nó là sinh vật bị nhồi kim loại vào đầu.
Dọn Xên con phía bắc, hạ Poc, Pic, King Kong rồi lấy mảnh giáp.
Thưởng: 180 triệu SM, 180 triệu TN, 20 Đá nâng cấp 5'
WHERE `id` = 28;
UPDATE `task_main_template` SET `NAME` = 'Phòng thí nghiệm Myuu', `detail` = 'Hệ thống lọc khí trong phòng thí nghiệm chạy mỗi 6 phút.
Lẻn vào bằng thẻ từ giả, lấy 5 bản thiết kế rồi đối mặt Dr. Myuu.
Thưởng: 200 triệu SM, 200 triệu TN, 25 Đá nâng cấp 5, 5 Đá bảo vệ'
WHERE `id` = 29;
UPDATE `task_main_template` SET `NAME` = 'Xên bọ hung', `detail` = 'Xên bọ hung là mẫu thử gần hoàn thiện nhất của Dr. Myuu.
Tới Thị trấn Ginder, hạ Xên con rồi hạ Xên bọ hung cả ba dạng.
Thưởng: 220 triệu SM, 220 triệu TN, 30 Đá nâng cấp 5, 2 Sao pha lê vàng'
WHERE `id` = 30;
UPDATE `task_main_template` SET `NAME` = 'Bản sao của chính ngươi', `detail` = 'Mẫu 08 của Dr. Myuu mang khuôn mặt của chính ngươi.
Nghe Potage, chọn số phận bản sao rồi đấu nó ở Võ đài Xên.
Thưởng: 230 triệu SM, 230 triệu TN, 50 Đá nâng cấp 5, 2 Sao pha lê cam, 1 Mảnh Ký Ức 4'
WHERE `id` = 31;
UPDATE `task_main_template` SET `NAME` = 'Lời cảnh báo của Bardock', `detail` = 'Nhẫn thời không sai lệch đưa ngươi về hành tinh thực vật cổ xưa.
Tìm Bardock, dọn hang động, nhặt 3 Mảnh Ký Ức Vỡ cho ông.
Thưởng: 200 triệu SM, 200 triệu TN, 30 Đậu thần cấp 8'
WHERE `id` = 32;
UPDATE `task_main_template` SET `NAME` = 'Phá vỡ giới hạn', `detail` = 'HP gốc của ngươi đã chạm trần, cơ thể không lớn thêm được nữa.
Gặp Quốc Vương, mở giới hạn sức mạnh, đạt 3 tỷ sức mạnh.
Thưởng: 250 triệu SM, 250 triệu TN, 50 Đậu thần cấp 8, 5 Đá bảo vệ'
WHERE `id` = 33;
UPDATE `task_main_template` SET `NAME` = 'Vùng đất băng giá', `detail` = 'Một Mảnh Ký Ức bị đóng băng trong Hang băng, Cooler canh giữ.
Vượt vùng tuyết, nhặt Mảnh Ký Ức Đóng Băng rồi hạ Cooler.
Thưởng: 300 triệu SM, 300 triệu TN, 1 Bông tai Porata'
WHERE `id` = 34;
UPDATE `task_main_template` SET `NAME` = 'Con đường rắn độc', `detail` = 'Muốn biết Heart đem ký ức đi đâu, phải hỏi người đã chết.
Đi Con đường rắn độc cùng bạn, rồi gặp Thượng Đế ở Thần điện.
Thưởng: 350 triệu SM, 350 triệu TN, 5 Đá nâng cấp 5'
WHERE `id` = 35;
UPDATE `task_main_template` SET `NAME` = 'Cổng phi thuyền', `detail` = 'Heart gửi ký ức vào phi thuyền của Babiđây, cổng mở 12h-12h59.
Gặp Ôsin, vào phi thuyền (hoặc đi vòng Sa mạc), gặp Babiđây.
Thưởng: 400 triệu SM, 400 triệu TN, 50 Đậu thần cấp 8'
WHERE `id` = 36;
UPDATE `task_main_template` SET `NAME` = 'Mabư', `detail` = 'Tầng cuối phi thuyền không phải kho, nó là cái bụng.
Hạ Mabư, lấy Lõi Phép Babiđây mang tới Kibit ở Thánh địa Kaio.
Thưởng: 450 triệu SM, 450 triệu TN, 1 Bông tai Porata'
WHERE `id` = 37;
UPDATE `task_main_template` SET `NAME` = 'Black Goku', `detail` = 'Một kẻ mang khuôn mặt quen thuộc đang xóa sạch Tương lai.
Hạ Black Goku cả hai dạng, nhặt nhẫn về cho Bunma.
Thưởng: 550 triệu SM, 550 triệu TN, 2 Sao pha lê lục'
WHERE `id` = 38;
UPDATE `task_main_template` SET `NAME` = 'Cái giá của ký ức', `detail` = 'Bardock thấy trước cái chết của ngươi và chọn đổi chỗ cho ngươi.
Gom 7 viên Ngọc Rồng, ước, rồi hạ Baby ở Làng Kakarot.
Thưởng: 1 tỷ SM, 1 tỷ TN, 1 Bông tai Porata, 50 Đậu thần cấp 8'
WHERE `id` = 39;
UPDATE `task_main_template` SET `NAME` = 'Tổ Sư Kaio', `detail` = 'Lõi Hư Không không phải vũ khí, nó là nơi chứa.
Lên Thánh địa Kaio, trao Mảnh Ký Ức 1 cho Tổ Sư Kaio.
Thưởng: 550 triệu SM, 550 triệu TN, 150 Mảnh áo, 50 Đậu thần cấp 8'
WHERE `id` = 40;
UPDATE `task_main_template` SET `NAME` = 'Cơn thịnh nộ Broly', `detail` = 'Broly phát điên vì bị xóa sạch ký ức.
Dọn vành đai rừng, hạ Broly rồi hạ tiếp Super Broly.
Thưởng: 650 triệu SM, 650 triệu TN, 150 Mảnh quần, 50 Đậu thần cấp 8'
WHERE `id` = 41;
UPDATE `task_main_template` SET `NAME` = 'Hành tinh ngục tù', `detail` = 'Heart nuôi Cumber bằng ký ức của tù nhân.
Phá 60 lồng giam trong 10 phút, rồi hạ Cumber cả hai dạng.
Thưởng: 750 triệu SM, 750 triệu TN, 150 Mảnh găng tay, 50 Đậu thần cấp 8'
WHERE `id` = 42;
UPDATE `task_main_template` SET `NAME` = 'Khí gas hủy diệt', `detail` = 'Heart đánh thức Dr Lychee và thả lại khí gas hủy diệt.
Dọn khí gas cùng bang, hoặc một mình hạ quái ngục tù và thực vật.
Thưởng: 850 triệu SM, 850 triệu TN, 150 Mảnh giầy, 50 Đậu thần cấp 8'
WHERE `id` = 43;
UPDATE `task_main_template` SET `NAME` = 'Thử thách của Thần Hủy Diệt', `detail` = 'Bill không cứu ai, ngài chỉ thử.
Thắng 1 trận đấu, hạ Whis, rồi mở giới hạn sức mạnh lần hai.
Thưởng: 900 triệu SM, 900 triệu TN, 150 Mảnh nhẫn, 50 Đậu thần cấp 8'
WHERE `id` = 44;
UPDATE `task_main_template` SET `NAME` = 'Bảy mảnh hợp nhất', `detail` = 'Ký ức cần hai người mới thành ký ức.
Nhặt mảnh thứ bảy ở Lãnh địa Fize, hợp nhất cùng một người khác.
Thưởng: 950 triệu SM, 950 triệu TN, 5 Đá ngũ sắc, 50 Đậu thần cấp 8'
WHERE `id` = 45;
UPDATE `task_main_template` SET `NAME` = 'Heart', `detail` = 'Heart lộ mặt ở phòng thí nghiệm Myuu. Trận cuối đã tới.
Hạ Heart, đuổi tới Võ Đài Siêu Cấp và hạ hắn cả ba dạng.
Thưởng: 1,1 tỷ SM, 1,1 tỷ TN, 5 Đá ngũ sắc, 10 Đá bảo vệ, 60 Đậu thần cấp 8'
WHERE `id` = 46;
UPDATE `task_main_template` SET `NAME` = 'Trả lại hay giữ lấy', `detail` = 'Ngươi chọn trả hết ký ức cho vũ trụ.
Trả Lõi, mở giới hạn lần cuối, hạ Hư Không Vô Danh.
Thưởng: 1,5 tỷ SM, 1,5 tỷ TN, 1 danh hiệu Người Trả Ký Ức, 20 Đá bảo vệ, 99 Đậu thần cấp 8, 10 Đá ngũ sắc'
WHERE `id` = 47;
UPDATE `task_main_template` SET `NAME` = 'Kẻ săn tiền thưởng', `detail` = 'Ngươi chọn con đường có giấy tờ.
Báo Jaco, hạ 3 tên bị truy nã rồi nộp biên bản cho Jaco.
Thưởng: 8 triệu SM, 8 triệu TN, 20 Đá nâng cấp 1, 8 Đá bảo vệ'
WHERE `id` = 48;
UPDATE `task_main_template` SET `NAME` = 'Bản sao của chính ngươi', `detail` = 'Ngươi chọn không giết.
Đánh gục bản sao, rồi thu nhận nó bằng Bình chứa Commeson.
Thưởng: 230 triệu SM, 230 triệu TN, 3 Sao pha lê lục, 5 Đá ngũ sắc, 10 Đá bảo vệ, 1 Mảnh Ký Ức 4'
WHERE `id` = 49;
UPDATE `task_main_template` SET `NAME` = 'Trả lại hay giữ lấy', `detail` = 'Ngươi giữ Lõi, thành người duy nhất còn nhớ.
Về ngục tù, mở giới hạn, hạ Hư Không Vô Danh.
Thưởng: 1,5 tỷ SM, 1,5 tỷ TN, 1 danh hiệu Kẻ Giữ Hư Không, 20 Đá bảo vệ, 99 Đậu thần cấp 8, 10 Đá ngũ sắc'
WHERE `id` = 50;

-- ---------------------------------------------------------------------
-- (2) task_sub_template — tên bước + câu nhắc (237 dòng, khớp theo
--     task_main_id + ducvupro). KHÔNG đụng max_count / npc_id / map.
-- ---------------------------------------------------------------------
-- NV 0
UPDATE `task_sub_template` SET `NAME` = 'Đi theo mũi tên chỉ dẫn', `notify` = '' WHERE `task_main_id` = 0 AND `ducvupro` = 1;
UPDATE `task_sub_template` SET `NAME` = 'Về nhà %2', `notify` = '' WHERE `task_main_id` = 0 AND `ducvupro` = 2;
UPDATE `task_sub_template` SET `NAME` = 'Gặp %2', `notify` = '' WHERE `task_main_id` = 0 AND `ducvupro` = 3;
UPDATE `task_sub_template` SET `NAME` = 'Mở rương đồ lấy rađa', `notify` = '' WHERE `task_main_id` = 0 AND `ducvupro` = 4;
UPDATE `task_sub_template` SET `NAME` = 'Thu hoạch đậu thần', `notify` = '' WHERE `task_main_id` = 0 AND `ducvupro` = 5;
UPDATE `task_sub_template` SET `NAME` = 'Về báo %2', `notify` = '' WHERE `task_main_id` = 0 AND `ducvupro` = 6;
-- NV 1
UPDATE `task_sub_template` SET `NAME` = 'Đánh ngã 5 mộc nhân', `notify` = 'Mộc nhân cũ vẫn đứng ở %1. Đánh ngã 5 con cho ông xem' WHERE `task_main_id` = 1 AND `ducvupro` = 7;
UPDATE `task_sub_template` SET `NAME` = 'Về khoe với %2', `notify` = 'Mộc nhân đổ cả rồi. Về khoe với %2 thôi' WHERE `task_main_id` = 1 AND `ducvupro` = 8;
-- NV 2
UPDATE `task_sub_template` SET `NAME` = 'Hạ %4 lấy 10 đùi gà', `notify` = 'Lên %3 hạ lũ %4, nhặt đủ 10 đùi gà' WHERE `task_main_id` = 2 AND `ducvupro` = 9;
UPDATE `task_sub_template` SET `NAME` = 'Đưa đùi gà cho %2', `notify` = 'Đủ 10 đùi gà rồi. Mang về cho %2 kẻo ông đói' WHERE `task_main_id` = 2 AND `ducvupro` = 10;
-- NV 3
UPDATE `task_sub_template` SET `NAME` = 'Cộng điểm tiềm năng', `notify` = '' WHERE `task_main_id` = 3 AND `ducvupro` = 11;
UPDATE `task_sub_template` SET `NAME` = 'Tìm vật thể lạ rơi xuống', `notify` = '' WHERE `task_main_id` = 3 AND `ducvupro` = 12;
UPDATE `task_sub_template` SET `NAME` = 'Đưa vật lạ cho %2', `notify` = 'Mang thứ vừa tìm được về cho %2 xem' WHERE `task_main_id` = 3 AND `ducvupro` = 13;
-- NV 4
UPDATE `task_sub_template` SET `NAME` = 'Hạ 12 %4', `notify` = 'Tới %6, hạ 12 %4 đang phát điên' WHERE `task_main_id` = 4 AND `ducvupro` = 15;
UPDATE `task_sub_template` SET `NAME` = 'Hạ 15 %4 mẹ', `notify` = 'Hạ 15 %4 mẹ ở %6, chúng cũng biến dạng rồi' WHERE `task_main_id` = 4 AND `ducvupro` = 16;
UPDATE `task_sub_template` SET `NAME` = 'Về kể cho %2', `notify` = 'Về nhà kể cho %2 chuyện ở %6' WHERE `task_main_id` = 4 AND `ducvupro` = 17;
-- NV 5
UPDATE `task_sub_template` SET `NAME` = 'Tìm Kỷ Vật Của Ông', `notify` = 'Hạ %9 trong rừng, kỷ vật sẽ rơi ra' WHERE `task_main_id` = 5 AND `ducvupro` = 18;
UPDATE `task_sub_template` SET `NAME` = 'Lau sạch Kỷ Vật', `notify` = 'Mở hành trang, dùng Kỷ Vật Của Ông để lau sạch' WHERE `task_main_id` = 5 AND `ducvupro` = 19;
UPDATE `task_sub_template` SET `NAME` = 'Đưa kỷ vật cho %2', `notify` = 'Mang kỷ vật về nhà đưa cho %2' WHERE `task_main_id` = 5 AND `ducvupro` = 20;
-- NV 6
UPDATE `task_sub_template` SET `NAME` = 'Hạ 15 %9', `notify` = 'Lần theo tiếng rít vào rừng sâu, hạ 15 %9' WHERE `task_main_id` = 6 AND `ducvupro` = 21;
UPDATE `task_sub_template` SET `NAME` = 'Hạ Kẻ Thu Gom', `notify` = 'Kẻ Thu Gom đang lảng vảng trong rừng sâu' WHERE `task_main_id` = 6 AND `ducvupro` = 22;
UPDATE `task_sub_template` SET `NAME` = 'Về báo %2', `notify` = 'Về nhà báo cho %2 biết chuyện' WHERE `task_main_id` = 6 AND `ducvupro` = 23;
-- NV 7
UPDATE `task_sub_template` SET `NAME` = 'Hạ 10 quái mẹ trong 3 phút', `notify` = 'Còn 3 phút! Hạ quái mẹ ở rừng sâu, hết giờ phải đếm lại từ đầu' WHERE `task_main_id` = 7 AND `ducvupro` = 24;
UPDATE `task_sub_template` SET `NAME` = 'Chạy tới Trạm tàu vũ trụ', `notify` = 'Chạy ngay tới Trạm tàu vũ trụ' WHERE `task_main_id` = 7 AND `ducvupro` = 25;
UPDATE `task_sub_template` SET `NAME` = 'Gặp Jaco', `notify` = 'Jaco đang đợi ở Trạm tàu vũ trụ' WHERE `task_main_id` = 7 AND `ducvupro` = 26;
-- NV 8
UPDATE `task_sub_template` SET `NAME` = 'Gặp Bunma ở Siêu Thị', `notify` = 'Bunma đang đợi ở Siêu Thị' WHERE `task_main_id` = 8 AND `ducvupro` = 27;
UPDATE `task_sub_template` SET `NAME` = 'Mua 1 Rada cấp 1', `notify` = 'Mua 1 Rada cấp 1 ở cửa hàng trong Siêu Thị' WHERE `task_main_id` = 8 AND `ducvupro` = 28;
UPDATE `task_sub_template` SET `NAME` = 'Hạ 25 quái mẹ lấy lõi', `notify` = 'Hạ thằn lằn mẹ, phi long mẹ hoặc quỷ bay mẹ ở rừng sâu' WHERE `task_main_id` = 8 AND `ducvupro` = 29;
-- NV 9
UPDATE `task_sub_template` SET `NAME` = 'Bay tới %11', `notify` = 'Bay tới %11 tìm sư phụ' WHERE `task_main_id` = 9 AND `ducvupro` = 30;
UPDATE `task_sub_template` SET `NAME` = 'Hạ 20 %12', `notify` = '%12 bám đầy quanh nhà sư phụ. Hạ 20 con' WHERE `task_main_id` = 9 AND `ducvupro` = 31;
UPDATE `task_sub_template` SET `NAME` = 'Gặp %10', `notify` = '%10 đang đợi ngươi' WHERE `task_main_id` = 9 AND `ducvupro` = 32;
-- NV 10
UPDATE `task_sub_template` SET `NAME` = 'Bái %10 làm thầy', `notify` = 'Nói chuyện với %10 để xin làm đệ tử' WHERE `task_main_id` = 10 AND `ducvupro` = 33;
UPDATE `task_sub_template` SET `NAME` = 'Học chưởng cấp 1', `notify` = 'Học chưởng cấp 1 từ %10' WHERE `task_main_id` = 10 AND `ducvupro` = 34;
UPDATE `task_sub_template` SET `NAME` = 'Đạt 250.000 sức mạnh', `notify` = 'Luyện tập đến khi đạt 250.000 sức mạnh' WHERE `task_main_id` = 10 AND `ducvupro` = 35;
-- NV 11
UPDATE `task_sub_template` SET `NAME` = 'Thu hoạch 5 hạt đậu', `notify` = 'Về nhà hái đậu thần trên cây' WHERE `task_main_id` = 11 AND `ducvupro` = 36;
UPDATE `task_sub_template` SET `NAME` = 'Gieo Hạt Giống Hy Vọng', `notify` = 'Dùng Hạt Giống Hy Vọng khi đang ở nhà' WHERE `task_main_id` = 11 AND `ducvupro` = 37;
UPDATE `task_sub_template` SET `NAME` = 'Khoe cây mới với %2', `notify` = 'Cây đã khỏe lại. Khoe với %2 thôi' WHERE `task_main_id` = 11 AND `ducvupro` = 38;
-- NV 12
UPDATE `task_sub_template` SET `NAME` = 'Nở trứng nhận đệ tử', `notify` = 'Chạm vào Quả trứng ở nhà, chọn Nở trứng' WHERE `task_main_id` = 12 AND `ducvupro` = 39;
UPDATE `task_sub_template` SET `NAME` = 'Cùng đệ tử hạ 25 quái mẹ', `notify` = 'Dẫn đệ tử đi hạ quái mẹ' WHERE `task_main_id` = 12 AND `ducvupro` = 40;
UPDATE `task_sub_template` SET `NAME` = 'Dẫn đệ tử gặp %2', `notify` = 'Về nhà giới thiệu đệ tử với %2' WHERE `task_main_id` = 12 AND `ducvupro` = 41;
-- NV 13
UPDATE `task_sub_template` SET `NAME` = 'Gia nhập 1 bang hội', `notify` = 'Tạo bang hoặc xin vào một bang hội' WHERE `task_main_id` = 13 AND `ducvupro` = 42;
UPDATE `task_sub_template` SET `NAME` = 'Cùng bạn bang hạ 30 quái mẹ', `notify` = 'Cần ít nhất 1 bạn cùng bang ở cùng khu; từ 3 người mỗi con tính 2' WHERE `task_main_id` = 13 AND `ducvupro` = 43;
UPDATE `task_sub_template` SET `NAME` = 'Gặp Giu-ma Đầu Bò', `notify` = 'Giu-ma Đầu Bò ở Lãnh địa Bang Hội' WHERE `task_main_id` = 13 AND `ducvupro` = 44;
-- NV 14
UPDATE `task_sub_template` SET `NAME` = 'Gặp Bunma ở Nhà Bunma', `notify` = 'Bunma có tin mới, gặp cô ở Nhà Bunma' WHERE `task_main_id` = 14 AND `ducvupro` = 45;
UPDATE `task_sub_template` SET `NAME` = 'Mua 1 món ở quầy Uron', `notify` = 'Uron bán hàng ở Siêu Thị. Mua món gì cũng được' WHERE `task_main_id` = 14 AND `ducvupro` = 46;
UPDATE `task_sub_template` SET `NAME` = 'Hạ 20 heo chở hàng', `notify` = 'Heo chở hàng đi qua Rừng Bamboo, Núi hoa vàng, Rừng cọ' WHERE `task_main_id` = 14 AND `ducvupro` = 47;
-- NV 15
UPDATE `task_sub_template` SET `NAME` = 'Hạ 20 quái mẹ ở điểm hẹn', `notify` = 'Điểm hẹn ở Rừng Bamboo, Núi hoa vàng hoặc Rừng cọ' WHERE `task_main_id` = 15 AND `ducvupro` = 48;
UPDATE `task_sub_template` SET `NAME` = 'Đánh bại Jaco mất ký ức', `notify` = 'Jaco đã bị xóa ký ức. Đánh gục hắn để hắn tỉnh lại' WHERE `task_main_id` = 15 AND `ducvupro` = 49;
UPDATE `task_sub_template` SET `NAME` = 'Gặp Jaco ở Trạm tàu vũ trụ', `notify` = 'Jaco đã tỉnh, hắn đợi ngươi ở Trạm tàu vũ trụ' WHERE `task_main_id` = 15 AND `ducvupro` = 50;
-- NV 16
UPDATE `task_sub_template` SET `NAME` = 'Đi về vùng đất phía Nam', `notify` = 'Tới Nam Kamê, Nam Guru hoặc Thung lũng đen (theo hành tinh)' WHERE `task_main_id` = 16 AND `ducvupro` = 51;
UPDATE `task_sub_template` SET `NAME` = 'Hạ 40 quái chắn đường', `notify` = 'Hạ Không tặc, Quỷ đầu to hoặc Quỷ địa ngục ở vùng phía Nam' WHERE `task_main_id` = 16 AND `ducvupro` = 52;
UPDATE `task_sub_template` SET `NAME` = 'Hạ 30 quái canh bờ biển', `notify` = 'Hạ Bulon, Ukulele hoặc Quỷ mập ở vùng ven biển' WHERE `task_main_id` = 16 AND `ducvupro` = 53;
UPDATE `task_sub_template` SET `NAME` = 'Nhặt 5 Vỏ đạn khắc dấu', `notify` = 'Vỏ đạn rơi từ Bulon, Ukulele, Quỷ mập' WHERE `task_main_id` = 16 AND `ducvupro` = 54;
UPDATE `task_sub_template` SET `NAME` = 'Về gặp %10', `notify` = 'Mang vỏ đạn về cho %10 xem' WHERE `task_main_id` = 16 AND `ducvupro` = 55;
-- NV 17
UPDATE `task_sub_template` SET `NAME` = 'Gặp Bà Hạt Mít', `notify` = 'Bà Hạt Mít đang đợi ở %5' WHERE `task_main_id` = 17 AND `ducvupro` = 56;
UPDATE `task_sub_template` SET `NAME` = 'Nâng 1 trang bị lên +2', `notify` = 'Nhờ Bà Hạt Mít nâng cấp, nguyên liệu bà đã cho' WHERE `task_main_id` = 17 AND `ducvupro` = 57;
UPDATE `task_sub_template` SET `NAME` = 'Dùng Búa rèn cũ', `notify` = 'Mở hành trang, dùng Búa rèn cũ' WHERE `task_main_id` = 17 AND `ducvupro` = 58;
UPDATE `task_sub_template` SET `NAME` = 'Về khoe với %10', `notify` = 'Mang vũ khí mới về khoe với %10' WHERE `task_main_id` = 17 AND `ducvupro` = 59;
-- NV 18
UPDATE `task_sub_template` SET `NAME` = 'Tới Thành phố Vegeta', `notify` = 'Tới Thành phố Vegeta' WHERE `task_main_id` = 18 AND `ducvupro` = 60;
UPDATE `task_sub_template` SET `NAME` = 'Gặp người lạ thổi nhạc', `notify` = 'Người thổi nhạc đứng trong Thành phố Vegeta' WHERE `task_main_id` = 18 AND `ducvupro` = 61;
UPDATE `task_sub_template` SET `NAME` = 'Hạ 30 quái vây thành', `notify` = 'Hạ Tambourine, Drum hoặc Akkuman đang vây thành' WHERE `task_main_id` = 18 AND `ducvupro` = 62;
UPDATE `task_sub_template` SET `NAME` = 'Tới Thành phố Santa', `notify` = 'Tapion đi trước rồi. Theo anh tới Thành phố Santa' WHERE `task_main_id` = 18 AND `ducvupro` = 63;
UPDATE `task_sub_template` SET `NAME` = 'Nghe Tapion kể chuyện', `notify` = 'Tapion đợi ngươi ở Thành phố Santa' WHERE `task_main_id` = 18 AND `ducvupro` = 64;
-- NV 19
UPDATE `task_sub_template` SET `NAME` = 'Gặp Cui ở Thung lũng Nappa', `notify` = 'Cui đang đợi ở Thung lũng Nappa' WHERE `task_main_id` = 19 AND `ducvupro` = 65;
UPDATE `task_sub_template` SET `NAME` = 'Hạ 60 Nappa mất trí', `notify` = 'Nappa ở Thung lũng Nappa, Vực cấm, Núi Appule' WHERE `task_main_id` = 19 AND `ducvupro` = 66;
UPDATE `task_sub_template` SET `NAME` = 'Hạ 40 Soldier gác kho', `notify` = 'Soldier ở Vực cấm và Núi Appule' WHERE `task_main_id` = 19 AND `ducvupro` = 67;
UPDATE `task_sub_template` SET `NAME` = 'Cùng bạn hạ 30 Appule', `notify` = 'Appule ở vùng Raspberry. Cần 1 người chơi khác cùng khu, mỗi con tính 2' WHERE `task_main_id` = 19 AND `ducvupro` = 68;
UPDATE `task_sub_template` SET `NAME` = 'Báo cáo với Cui', `notify` = 'Về Thung lũng Nappa báo cáo với Cui' WHERE `task_main_id` = 19 AND `ducvupro` = 69;
-- NV 20
UPDATE `task_sub_template` SET `NAME` = 'Gặp Berry ở Khu hang động', `notify` = 'Berry đứng ở Khu hang động' WHERE `task_main_id` = 20 AND `ducvupro` = 70;
UPDATE `task_sub_template` SET `NAME` = 'Chọn: Granola hay Jaco', `notify` = 'Điểm rẽ nhánh: chọn đi theo Granola hoặc báo Jaco' WHERE `task_main_id` = 20 AND `ducvupro` = 71;
UPDATE `task_sub_template` SET `NAME` = 'Bắt tay với Granola', `notify` = 'Granola ở ngay Khu hang động' WHERE `task_main_id` = 20 AND `ducvupro` = 72;
UPDATE `task_sub_template` SET `NAME` = 'Hạ 3 tay chân của Fide', `notify` = 'Săn Kuku, Mập Đầu Đinh và Rambo' WHERE `task_main_id` = 20 AND `ducvupro` = 73;
UPDATE `task_sub_template` SET `NAME` = 'Nhặt 3 Thẻ tiền thưởng', `notify` = 'Thẻ rơi khi hạ Kuku, Mập Đầu Đinh, Rambo' WHERE `task_main_id` = 20 AND `ducvupro` = 74;
UPDATE `task_sub_template` SET `NAME` = 'Nhận thưởng từ Granola', `notify` = 'Về Khu hang động nhận tiền từ Granola' WHERE `task_main_id` = 20 AND `ducvupro` = 75;
-- NV 21
UPDATE `task_sub_template` SET `NAME` = 'Gặp Lính canh ở Rừng Bamboo', `notify` = 'Lính canh đứng ở Rừng Bamboo' WHERE `task_main_id` = 21 AND `ducvupro` = 76;
UPDATE `task_sub_template` SET `NAME` = 'Phá trại hoặc hạ 300 quái', `notify` = 'Không có bang? Hạ 300 quái ở Trại lính Fide, Núi dây leo, Núi cây quỷ, Trại quỷ già, Vực chết' WHERE `task_main_id` = 21 AND `ducvupro` = 77;
UPDATE `task_sub_template` SET `NAME` = 'Lấy bản đồ hành quân', `notify` = 'Lấy từ Độc Nhãn trong doanh trại, hoặc hỏi Lính canh ở Rừng Bamboo' WHERE `task_main_id` = 21 AND `ducvupro` = 78;
UPDATE `task_sub_template` SET `NAME` = 'Về gặp %10', `notify` = 'Mang bản đồ hành quân về cho %10' WHERE `task_main_id` = 21 AND `ducvupro` = 79;
-- NV 22
UPDATE `task_sub_template` SET `NAME` = 'Hỏi Tapion về máy đo lạ', `notify` = 'Tapion ở Thành phố Santa' WHERE `task_main_id` = 22 AND `ducvupro` = 80;
UPDATE `task_sub_template` SET `NAME` = 'Hạ 40 lính khỉ canh đường', `notify` = 'Hạ Khỉ lông đen, Khỉ giáp sắt ở Hang quỷ chim, Núi khỉ đen, Hang khỉ đen' WHERE `task_main_id` = 22 AND `ducvupro` = 81;
UPDATE `task_sub_template` SET `NAME` = 'Hạ 5 tên Tiểu đội sát thủ', `notify` = 'Tiểu đội sát thủ xuất hiện ở Núi khỉ đỏ' WHERE `task_main_id` = 22 AND `ducvupro` = 82;
UPDATE `task_sub_template` SET `NAME` = 'Nhặt Máy đo ký ức', `notify` = 'Máy đo rơi khi hạ tên cuối của tiểu đội' WHERE `task_main_id` = 22 AND `ducvupro` = 83;
UPDATE `task_sub_template` SET `NAME` = 'Đưa máy đo cho Tapion', `notify` = 'Mang máy đo về Thành phố Santa cho Tapion' WHERE `task_main_id` = 22 AND `ducvupro` = 84;
-- NV 23
UPDATE `task_sub_template` SET `NAME` = 'Đạt 80.000.000 sức mạnh', `notify` = 'Cần 80.000.000 sức mạnh mới đủ sức đấu Fide' WHERE `task_main_id` = 23 AND `ducvupro` = 85;
UPDATE `task_sub_template` SET `NAME` = 'Tới Núi khỉ vàng', `notify` = 'Tới Núi khỉ vàng' WHERE `task_main_id` = 23 AND `ducvupro` = 86;
UPDATE `task_sub_template` SET `NAME` = 'Hạ 25 Khỉ lông vàng, 5 phút', `notify` = 'Còn 5 phút! Hết giờ phải đếm lại' WHERE `task_main_id` = 23 AND `ducvupro` = 87;
UPDATE `task_sub_template` SET `NAME` = 'Hạ 2 dạng đầu của Fide', `notify` = 'Fide đại ca xuất hiện ở Núi khỉ vàng' WHERE `task_main_id` = 23 AND `ducvupro` = 88;
UPDATE `task_sub_template` SET `NAME` = 'Hạ Fide dạng cuối', `notify` = 'Fide biến hình lần cuối, hạ hắn đi' WHERE `task_main_id` = 23 AND `ducvupro` = 89;
UPDATE `task_sub_template` SET `NAME` = 'Về gặp %10', `notify` = 'Ba hành tinh đã liên minh. Về báo tin cho %10' WHERE `task_main_id` = 23 AND `ducvupro` = 90;
-- NV 24
UPDATE `task_sub_template` SET `NAME` = 'Gặp Bunma ở Nhà Bunma', `notify` = 'Bunma đợi ở Nhà Bunma' WHERE `task_main_id` = 24 AND `ducvupro` = 91;
UPDATE `task_sub_template` SET `NAME` = 'Tới Thành phố phía đông', `notify` = 'Tới Thành phố phía đông' WHERE `task_main_id` = 24 AND `ducvupro` = 92;
UPDATE `task_sub_template` SET `NAME` = 'Hạ 50 Xên con cấp 1-2', `notify` = 'Xên con cấp 1-2 ở Thành phố phía đông và phía nam' WHERE `task_main_id` = 24 AND `ducvupro` = 93;
UPDATE `task_sub_template` SET `NAME` = 'Hạ 40 Xên con cấp 3-4', `notify` = 'Xên con cấp 3-4 ở Đảo Balê và Cao nguyên' WHERE `task_main_id` = 24 AND `ducvupro` = 94;
UPDATE `task_sub_template` SET `NAME` = 'Báo lại cho Bunma', `notify` = 'Về Nhà Bunma báo lại' WHERE `task_main_id` = 24 AND `ducvupro` = 95;
-- NV 25
UPDATE `task_sub_template` SET `NAME` = 'Tới Cao nguyên', `notify` = 'Hai bác sĩ máy đang ở Cao nguyên' WHERE `task_main_id` = 25 AND `ducvupro` = 96;
UPDATE `task_sub_template` SET `NAME` = 'Hạ 2 boss: Android 19, Kôrê', `notify` = 'Hạ Android 19 rồi Dr.Kôrê ở Cao nguyên' WHERE `task_main_id` = 25 AND `ducvupro` = 97;
UPDATE `task_sub_template` SET `NAME` = 'Nhặt 3 Lõi năng lượng', `notify` = 'Lõi rơi khi hạ Android 19 và Dr.Kôrê' WHERE `task_main_id` = 25 AND `ducvupro` = 98;
UPDATE `task_sub_template` SET `NAME` = 'Đưa lõi cho Bunma', `notify` = 'Mang lõi về Nhà Bunma' WHERE `task_main_id` = 25 AND `ducvupro` = 99;
-- NV 26
UPDATE `task_sub_template` SET `NAME` = 'Gặp Bà Hạt Mít ở Đảo Kamê', `notify` = 'Bà Hạt Mít đợi ở Đảo Kamê, nguyên liệu bà cho không' WHERE `task_main_id` = 26 AND `ducvupro` = 100;
UPDATE `task_sub_template` SET `NAME` = 'Pha lê hóa 1 trang bị', `notify` = 'Nhờ Bà Hạt Mít pha lê hóa một trang bị' WHERE `task_main_id` = 26 AND `ducvupro` = 101;
UPDATE `task_sub_template` SET `NAME` = 'Ép 1 Sao pha lê vào đồ', `notify` = 'Nhờ Bà Hạt Mít ép sao pha lê vào trang bị vừa pha lê hóa' WHERE `task_main_id` = 26 AND `ducvupro` = 102;
UPDATE `task_sub_template` SET `NAME` = 'Dùng Mẫu kim loại có ký ức', `notify` = 'Mở hành trang, dùng Mẫu kim loại có ký ức' WHERE `task_main_id` = 26 AND `ducvupro` = 103;
UPDATE `task_sub_template` SET `NAME` = 'Về kể cho %10', `notify` = 'Kể cho %10 những gì ngươi nghe được' WHERE `task_main_id` = 26 AND `ducvupro` = 104;
-- NV 27
UPDATE `task_sub_template` SET `NAME` = 'Hỏi Ca Lích về container', `notify` = 'Ca Lích ở Nhà Bunma' WHERE `task_main_id` = 27 AND `ducvupro` = 105;
UPDATE `task_sub_template` SET `NAME` = 'Tới Sân sau siêu thị', `notify` = 'Tới Sân sau siêu thị' WHERE `task_main_id` = 27 AND `ducvupro` = 106;
UPDATE `task_sub_template` SET `NAME` = 'Hạ 3 Android 13, 14, 15', `notify` = 'Ba cỗ máy mẫu đang ở Sân sau siêu thị' WHERE `task_main_id` = 27 AND `ducvupro` = 107;
UPDATE `task_sub_template` SET `NAME` = 'Báo cáo với Ca Lích', `notify` = 'Về Nhà Bunma báo cáo với Ca Lích' WHERE `task_main_id` = 27 AND `ducvupro` = 108;
-- NV 28
UPDATE `task_sub_template` SET `NAME` = 'Tới Thành phố phía bắc', `notify` = 'Tới Thành phố phía bắc' WHERE `task_main_id` = 28 AND `ducvupro` = 109;
UPDATE `task_sub_template` SET `NAME` = 'Hạ 60 Xên con cấp 5-7', `notify` = 'Xên con cấp 5-7 ở Thành phố, Ngọn núi, Thung lũng phía bắc' WHERE `task_main_id` = 28 AND `ducvupro` = 110;
UPDATE `task_sub_template` SET `NAME` = 'Hạ 3 tên Poc, Pic, King Kong', `notify` = 'Hạ Poc, Pic rồi King Kong ở Thành phố phía bắc' WHERE `task_main_id` = 28 AND `ducvupro` = 111;
UPDATE `task_sub_template` SET `NAME` = 'Nhặt Mảnh giáp khắc tên', `notify` = 'Mảnh giáp rơi khi hạ King Kong' WHERE `task_main_id` = 28 AND `ducvupro` = 112;
UPDATE `task_sub_template` SET `NAME` = 'Đưa mảnh giáp cho Bunma', `notify` = 'Mang mảnh giáp về Nhà Bunma' WHERE `task_main_id` = 28 AND `ducvupro` = 113;
-- NV 29
UPDATE `task_sub_template` SET `NAME` = 'Lấy thẻ từ giả của Bunma', `notify` = 'Bunma đợi ở Nhà Bunma' WHERE `task_main_id` = 29 AND `ducvupro` = 114;
UPDATE `task_sub_template` SET `NAME` = 'Vào Phòng thí nghiệm Myuu', `notify` = 'Mang theo thẻ từ giả để vào Phòng thí nghiệm Myuu' WHERE `task_main_id` = 29 AND `ducvupro` = 115;
UPDATE `task_sub_template` SET `NAME` = 'Nhặt 5 Bản thiết kế, 6 phút', `notify` = 'Còn 6 phút trước khi lọc khí chạy! Rời phòng là phải lấy lại' WHERE `task_main_id` = 29 AND `ducvupro` = 116;
UPDATE `task_sub_template` SET `NAME` = 'Đối mặt Dr. Myuu', `notify` = 'Dr. Myuu đang ở trong phòng thí nghiệm' WHERE `task_main_id` = 29 AND `ducvupro` = 117;
-- NV 30
UPDATE `task_sub_template` SET `NAME` = 'Tới Thị trấn Ginder', `notify` = 'Tới Thị trấn Ginder' WHERE `task_main_id` = 30 AND `ducvupro` = 118;
UPDATE `task_sub_template` SET `NAME` = 'Hạ 50 Xên con cấp 8', `notify` = 'Xên con cấp 8 ở Thị trấn Ginder' WHERE `task_main_id` = 30 AND `ducvupro` = 119;
UPDATE `task_sub_template` SET `NAME` = 'Hạ 2 dạng đầu Xên bọ hung', `notify` = 'Xên bọ hung xuất hiện ở Thị trấn Ginder' WHERE `task_main_id` = 30 AND `ducvupro` = 120;
UPDATE `task_sub_template` SET `NAME` = 'Hạ Xên hoàn thiện', `notify` = 'Xên đã hoàn thiện, hạ nó đi' WHERE `task_main_id` = 30 AND `ducvupro` = 121;
UPDATE `task_sub_template` SET `NAME` = 'Báo cho Bunma về mẫu 07', `notify` = 'Về Nhà Bunma báo tin' WHERE `task_main_id` = 30 AND `ducvupro` = 122;
-- NV 31
UPDATE `task_sub_template` SET `NAME` = 'Nghe Potage kể sự thật', `notify` = 'Potage ở Hang động Potaufeu' WHERE `task_main_id` = 31 AND `ducvupro` = 123;
UPDATE `task_sub_template` SET `NAME` = 'Chọn số phận bản sao', `notify` = 'Điểm rẽ nhánh: tiêu diệt hay thu nhận bản sao' WHERE `task_main_id` = 31 AND `ducvupro` = 124;
UPDATE `task_sub_template` SET `NAME` = 'Tới Võ đài Xên bọ hung', `notify` = 'Tới Võ đài Xên bọ hung' WHERE `task_main_id` = 31 AND `ducvupro` = 125;
UPDATE `task_sub_template` SET `NAME` = 'Rủ 1 người vào võ đài', `notify` = 'Võ đài cần một nhân chứng: 1 người chơi khác cùng khu' WHERE `task_main_id` = 31 AND `ducvupro` = 126;
UPDATE `task_sub_template` SET `NAME` = 'Hạ bản sao của ngươi', `notify` = 'Bản sao đang đợi ngươi trên võ đài' WHERE `task_main_id` = 31 AND `ducvupro` = 127;
UPDATE `task_sub_template` SET `NAME` = 'Đạt 2 tỷ sức mạnh', `notify` = 'Luyện tập đến khi đạt 2 tỷ sức mạnh' WHERE `task_main_id` = 31 AND `ducvupro` = 128;
-- NV 32
UPDATE `task_sub_template` SET `NAME` = 'Gặp Bunma ở Nhà Bunma', `notify` = 'Bunma có món đồ đưa ngươi, gặp cô ở Nhà Bunma' WHERE `task_main_id` = 32 AND `ducvupro` = 129;
UPDATE `task_sub_template` SET `NAME` = 'Dùng Nhẫn thời không', `notify` = 'Mở hành trang, dùng Nhẫn thời không sai lệch' WHERE `task_main_id` = 32 AND `ducvupro` = 130;
UPDATE `task_sub_template` SET `NAME` = 'Gặp Bardock', `notify` = 'Bardock ở Khu hang động' WHERE `task_main_id` = 32 AND `ducvupro` = 131;
UPDATE `task_sub_template` SET `NAME` = 'Hạ 40 Cabira hoặc Tobi', `notify` = 'Cabira, Tobi ở Khu hang động, Bìa rừng và Rừng nguyên thủy' WHERE `task_main_id` = 32 AND `ducvupro` = 132;
UPDATE `task_sub_template` SET `NAME` = 'Nhặt 3 Mảnh Ký Ức Vỡ', `notify` = 'Mảnh vỡ chỉ rơi khi hạ Tobi' WHERE `task_main_id` = 32 AND `ducvupro` = 133;
UPDATE `task_sub_template` SET `NAME` = 'Báo cáo với Bardock', `notify` = 'Về Khu hang động gặp Bardock' WHERE `task_main_id` = 32 AND `ducvupro` = 134;
-- NV 33
UPDATE `task_sub_template` SET `NAME` = 'Tới %5', `notify` = 'Quốc Vương đợi ở %5' WHERE `task_main_id` = 33 AND `ducvupro` = 135;
UPDATE `task_sub_template` SET `NAME` = 'Gặp Quốc Vương', `notify` = 'Quốc Vương đứng ở %5' WHERE `task_main_id` = 33 AND `ducvupro` = 136;
UPDATE `task_sub_template` SET `NAME` = 'Nâng HP gốc lên 220.000', `notify` = 'Cộng tiềm năng vào HP đến khi HP gốc đạt 220.000' WHERE `task_main_id` = 33 AND `ducvupro` = 137;
UPDATE `task_sub_template` SET `NAME` = 'Mở giới hạn sức mạnh', `notify` = 'Nhờ Quốc Vương mở giới hạn, lần này miễn phí' WHERE `task_main_id` = 33 AND `ducvupro` = 138;
UPDATE `task_sub_template` SET `NAME` = 'Đạt 3 tỷ sức mạnh', `notify` = 'Luyện tập đến khi đạt 3 tỷ sức mạnh' WHERE `task_main_id` = 33 AND `ducvupro` = 139;
UPDATE `task_sub_template` SET `NAME` = 'Báo cáo với Quốc Vương', `notify` = 'Về %5 báo cáo với Quốc Vương' WHERE `task_main_id` = 33 AND `ducvupro` = 140;
-- NV 34
UPDATE `task_sub_template` SET `NAME` = 'Tới Cánh đồng tuyết', `notify` = 'Map lạnh trừ 50% HP nếu không có đồ chống lạnh' WHERE `task_main_id` = 34 AND `ducvupro` = 141;
UPDATE `task_sub_template` SET `NAME` = 'Hạ 50 Tai tím hoặc Abo', `notify` = 'Tai tím, Abo ở Cánh đồng tuyết, Rừng tuyết, Núi tuyết' WHERE `task_main_id` = 34 AND `ducvupro` = 142;
UPDATE `task_sub_template` SET `NAME` = 'Hạ 20 Kado trong 5 phút', `notify` = 'Còn 5 phút! Kado ở Dòng sông băng, hết giờ phải đếm lại' WHERE `task_main_id` = 34 AND `ducvupro` = 143;
UPDATE `task_sub_template` SET `NAME` = 'Nhặt Mảnh Ký Ức Đóng Băng', `notify` = 'Mảnh ký ức nằm trong Hang băng' WHERE `task_main_id` = 34 AND `ducvupro` = 144;
UPDATE `task_sub_template` SET `NAME` = 'Hạ Cooler cả 2 dạng', `notify` = 'Cooler canh giữ Hang băng' WHERE `task_main_id` = 34 AND `ducvupro` = 145;
UPDATE `task_sub_template` SET `NAME` = 'Báo cáo với Bardock', `notify` = 'Về Khu hang động gặp Bardock' WHERE `task_main_id` = 34 AND `ducvupro` = 146;
-- NV 35
UPDATE `task_sub_template` SET `NAME` = 'Gặp Thần Vũ Trụ', `notify` = 'Thần Vũ Trụ ở Hành tinh Kaio' WHERE `task_main_id` = 35 AND `ducvupro` = 147;
UPDATE `task_sub_template` SET `NAME` = 'Rủ 1 người đi cùng', `notify` = 'Vào Con đường rắn độc cùng bạn bang, hoặc đứng cùng 1 người ở Hang quỷ chim' WHERE `task_main_id` = 35 AND `ducvupro` = 148;
UPDATE `task_sub_template` SET `NAME` = 'Tích 120 điểm diệt quái', `notify` = 'Trong phó bản mỗi quái 2 điểm; ngoài phó bản (map 73-82) mỗi quái 1 điểm' WHERE `task_main_id` = 35 AND `ducvupro` = 149;
UPDATE `task_sub_template` SET `NAME` = 'Qua rắn độc hoặc hạ 200 quái', `notify` = 'Không có bang? Hạ 200 Dơi da xanh hoặc Quỷ chim ở map 73/74/76/77/81/82' WHERE `task_main_id` = 35 AND `ducvupro` = 150;
UPDATE `task_sub_template` SET `NAME` = 'Gặp Thượng Đế ở Thần điện', `notify` = 'Thượng Đế đợi ở Thần điện' WHERE `task_main_id` = 35 AND `ducvupro` = 151;
-- NV 36
UPDATE `task_sub_template` SET `NAME` = 'Gặp Ôsin ở Đại hội võ thuật', `notify` = 'Ôsin ở Đại hội võ thuật' WHERE `task_main_id` = 36 AND `ducvupro` = 152;
UPDATE `task_sub_template` SET `NAME` = 'Vào Cổng phi thuyền', `notify` = 'Cổng mở 12h-12h59. Sai giờ thì nhờ Ôsin đưa qua Sa mạc hoang vu' WHERE `task_main_id` = 36 AND `ducvupro` = 153;
UPDATE `task_sub_template` SET `NAME` = 'Hạ Drabura hoặc 20 Cadic M', `notify` = 'Hạ Drabura trong phi thuyền, hoặc 20 Cadic M ở Sa mạc hoang vu' WHERE `task_main_id` = 36 AND `ducvupro` = 154;
UPDATE `task_sub_template` SET `NAME` = 'Xuống Cửa Ải 1', `notify` = 'Xuống Cửa Ải 1, hoặc nhặt đồ rơi từ Cadic M ở Sa mạc hoang vu' WHERE `task_main_id` = 36 AND `ducvupro` = 155;
UPDATE `task_sub_template` SET `NAME` = 'Nói chuyện với Babiđây', `notify` = 'Babiđây ở Cửa Ải 1; đi đường vòng thì gặp Ôsin ở Sa mạc hoang vu' WHERE `task_main_id` = 36 AND `ducvupro` = 156;
-- NV 37
UPDATE `task_sub_template` SET `NAME` = 'Gặp Ôsin ở Đại hội võ thuật', `notify` = 'Ôsin ở Đại hội võ thuật' WHERE `task_main_id` = 37 AND `ducvupro` = 157;
UPDATE `task_sub_template` SET `NAME` = 'Hạ Mabư', `notify` = 'Hạ Mabư trong phi thuyền, hoặc Hirudegarn ở Thành phố Santa' WHERE `task_main_id` = 37 AND `ducvupro` = 158;
UPDATE `task_sub_template` SET `NAME` = 'Hạ Drabura 3 / 30 Quỷ chim', `notify` = 'Hạ Drabura 3, hoặc 30 Quỷ chim ở Thành phố Santa' WHERE `task_main_id` = 37 AND `ducvupro` = 159;
UPDATE `task_sub_template` SET `NAME` = 'Nhặt Lõi Phép Babiđây', `notify` = 'Lõi Phép rơi cho người kết liễu ở bước trước' WHERE `task_main_id` = 37 AND `ducvupro` = 160;
UPDATE `task_sub_template` SET `NAME` = 'Mang Lõi Phép cho Kibit', `notify` = 'Kibit ở Thánh địa Kaio' WHERE `task_main_id` = 37 AND `ducvupro` = 161;
-- NV 38
UPDATE `task_sub_template` SET `NAME` = 'Gặp Bunma ở Nhà Bunma', `notify` = 'Bunma đợi ở Nhà Bunma' WHERE `task_main_id` = 38 AND `ducvupro` = 162;
UPDATE `task_sub_template` SET `NAME` = 'Hạ 60 Xên con phía bắc', `notify` = 'Xên con cấp 5-8 ở vùng phía bắc và Thị trấn Ginder' WHERE `task_main_id` = 38 AND `ducvupro` = 163;
UPDATE `task_sub_template` SET `NAME` = 'Hạ Black Goku', `notify` = 'Black Goku xuất hiện ở Thành phố phía đông' WHERE `task_main_id` = 38 AND `ducvupro` = 164;
UPDATE `task_sub_template` SET `NAME` = 'Hạ Super Black Goku', `notify` = 'Black Goku đã biến hình, hạ hắn đi' WHERE `task_main_id` = 38 AND `ducvupro` = 165;
UPDATE `task_sub_template` SET `NAME` = 'Nhặt Nhẫn thời không', `notify` = 'Nhẫn thời không sai lệch rơi khi hạ Black Goku' WHERE `task_main_id` = 38 AND `ducvupro` = 166;
UPDATE `task_sub_template` SET `NAME` = 'Báo cáo với Bunma', `notify` = 'Về Nhà Bunma báo cáo' WHERE `task_main_id` = 38 AND `ducvupro` = 167;
-- NV 39
UPDATE `task_sub_template` SET `NAME` = 'Gặp Bardock', `notify` = 'Bardock ở Khu hang động' WHERE `task_main_id` = 39 AND `ducvupro` = 168;
UPDATE `task_sub_template` SET `NAME` = 'Gom đủ 7 viên Ngọc Rồng', `notify` = 'Đủ 7 viên từ 1 đến 7 sao trong hành trang' WHERE `task_main_id` = 39 AND `ducvupro` = 169;
UPDATE `task_sub_template` SET `NAME` = 'Gọi Rồng Thần và ước', `notify` = 'Gọi Rồng Thần ở làng quê nhà rồi ước một điều' WHERE `task_main_id` = 39 AND `ducvupro` = 170;
UPDATE `task_sub_template` SET `NAME` = 'Hỏi Rồng Omega về sao đen', `notify` = 'Rồng Omega ở Trạm tàu vũ trụ' WHERE `task_main_id` = 39 AND `ducvupro` = 171;
UPDATE `task_sub_template` SET `NAME` = 'Hạ 40 Khỉ lông vàng', `notify` = 'Khỉ lông vàng ở Núi khỉ vàng' WHERE `task_main_id` = 39 AND `ducvupro` = 172;
UPDATE `task_sub_template` SET `NAME` = 'Gặp Bardock ở Làng Kakarot', `notify` = 'Bardock đợi ngươi ở Làng Kakarot' WHERE `task_main_id` = 39 AND `ducvupro` = 173;
UPDATE `task_sub_template` SET `NAME` = 'Hạ Baby cả 3 dạng', `notify` = 'Baby xuất hiện ở Làng Kakarot' WHERE `task_main_id` = 39 AND `ducvupro` = 174;
-- NV 40
UPDATE `task_sub_template` SET `NAME` = 'Gặp Thần Vũ Trụ', `notify` = 'Thần Vũ Trụ ở Hành tinh Kaio' WHERE `task_main_id` = 40 AND `ducvupro` = 175;
UPDATE `task_sub_template` SET `NAME` = 'Lên Thánh địa Kaio', `notify` = 'Lên Thánh địa Kaio' WHERE `task_main_id` = 40 AND `ducvupro` = 176;
UPDATE `task_sub_template` SET `NAME` = 'Gặp Tổ Sư Kaio', `notify` = 'Tổ Sư Kaio ở Thánh địa Kaio' WHERE `task_main_id` = 40 AND `ducvupro` = 177;
UPDATE `task_sub_template` SET `NAME` = 'Dùng Mảnh Ký Ức 1', `notify` = 'Dùng Mảnh Ký Ức 1 ở Thánh địa Kaio. Mảnh vẫn giữ lại' WHERE `task_main_id` = 40 AND `ducvupro` = 178;
UPDATE `task_sub_template` SET `NAME` = 'Nghe Kibit kể tiếp', `notify` = 'Kibit ở Thánh địa Kaio' WHERE `task_main_id` = 40 AND `ducvupro` = 179;
-- NV 41
UPDATE `task_sub_template` SET `NAME` = 'Nghe Tổ Sư Kaio dặn', `notify` = 'Tổ Sư Kaio ở Thánh địa Kaio' WHERE `task_main_id` = 41 AND `ducvupro` = 180;
UPDATE `task_sub_template` SET `NAME` = 'Hạ 60 quái vành đai rừng', `notify` = 'Hạ quái ở vùng map 27-38, bắt đầu từ Rừng Bamboo' WHERE `task_main_id` = 41 AND `ducvupro` = 181;
UPDATE `task_sub_template` SET `NAME` = 'Hạ Broly', `notify` = 'Broly đang nổi điên ở Rừng Bamboo' WHERE `task_main_id` = 41 AND `ducvupro` = 182;
UPDATE `task_sub_template` SET `NAME` = 'Hạ Super Broly', `notify` = 'Super Broly xuất hiện ngay tại chỗ' WHERE `task_main_id` = 41 AND `ducvupro` = 183;
UPDATE `task_sub_template` SET `NAME` = 'Báo lại với Tổ Sư Kaio', `notify` = 'Về Thánh địa Kaio báo lại' WHERE `task_main_id` = 41 AND `ducvupro` = 184;
-- NV 42
UPDATE `task_sub_template` SET `NAME` = 'Hỏi Ôsin đường tới ngục tù', `notify` = 'Ôsin ở Thánh địa Kaio' WHERE `task_main_id` = 42 AND `ducvupro` = 185;
UPDATE `task_sub_template` SET `NAME` = 'Tới Hành tinh ngục tù', `notify` = 'Nhờ Ôsin đưa tới Hành tinh ngục tù' WHERE `task_main_id` = 42 AND `ducvupro` = 186;
UPDATE `task_sub_template` SET `NAME` = 'Phá 60 lồng giam, 10 phút', `notify` = 'Còn 10 phút! Hạ Khỉ lông xanh, Taburine Đỏ; hết giờ phải đếm lại' WHERE `task_main_id` = 42 AND `ducvupro` = 187;
UPDATE `task_sub_template` SET `NAME` = 'Hạ Cumber', `notify` = 'Cumber ở Hành tinh ngục tù' WHERE `task_main_id` = 42 AND `ducvupro` = 188;
UPDATE `task_sub_template` SET `NAME` = 'Hạ Super Cumber', `notify` = 'Cumber đã biến hình, hạ hắn đi' WHERE `task_main_id` = 42 AND `ducvupro` = 189;
UPDATE `task_sub_template` SET `NAME` = 'Báo cáo với Ôsin', `notify` = 'Ôsin đợi ở Hành tinh ngục tù' WHERE `task_main_id` = 42 AND `ducvupro` = 190;
-- NV 43
UPDATE `task_sub_template` SET `NAME` = 'Gặp Mr Popo ở Làng Aru', `notify` = 'Mr Popo ở Làng Aru' WHERE `task_main_id` = 43 AND `ducvupro` = 191;
UPDATE `task_sub_template` SET `NAME` = 'Tích 160 điểm diệt quái', `notify` = 'Trong phó bản mỗi quái 2 điểm; ngoài phó bản (map 155/160/161) mỗi quái 1' WHERE `task_main_id` = 43 AND `ducvupro` = 192;
UPDATE `task_sub_template` SET `NAME` = 'Hạ Dr Lychee hoặc 50 quái', `notify` = 'Không có bang? Hạ 50 quái ở Hành tinh ngục tù hoặc Khu hang động, Bìa rừng' WHERE `task_main_id` = 43 AND `ducvupro` = 193;
UPDATE `task_sub_template` SET `NAME` = 'Hạ Hatchiyack hoặc 70 quái', `notify` = 'Không có bang? Hạ 70 quái ở Hành tinh ngục tù hoặc Khu hang động, Bìa rừng' WHERE `task_main_id` = 43 AND `ducvupro` = 194;
UPDATE `task_sub_template` SET `NAME` = 'Xong khí gas hoặc 100 quái', `notify` = 'Không có bang? Hạ 100 quái ở Hành tinh ngục tù hoặc Khu hang động, Bìa rừng' WHERE `task_main_id` = 43 AND `ducvupro` = 195;
UPDATE `task_sub_template` SET `NAME` = 'Báo cáo với Thượng Đế', `notify` = 'Thượng Đế đợi ở Thần điện' WHERE `task_main_id` = 43 AND `ducvupro` = 196;
-- NV 44
UPDATE `task_sub_template` SET `NAME` = 'Nhờ Ôsin tới hành tinh Bill', `notify` = 'Ôsin ở Thánh địa Kaio' WHERE `task_main_id` = 44 AND `ducvupro` = 197;
UPDATE `task_sub_template` SET `NAME` = 'Nói chuyện với Bill', `notify` = 'Bill ở Hành tinh Bill' WHERE `task_main_id` = 44 AND `ducvupro` = 198;
UPDATE `task_sub_template` SET `NAME` = 'Thắng 1 trận đấu', `notify` = 'Thắng một trận thách đấu, võ đài hoặc Đại hội võ thuật' WHERE `task_main_id` = 44 AND `ducvupro` = 199;
UPDATE `task_sub_template` SET `NAME` = 'Đánh bại Whis', `notify` = 'Thách đấu Whis ở Hành tinh Bill' WHERE `task_main_id` = 44 AND `ducvupro` = 200;
UPDATE `task_sub_template` SET `NAME` = 'Mở giới hạn lần hai', `notify` = 'Nhờ Tổ Sư Kaio mở giới hạn, lần này miễn phí' WHERE `task_main_id` = 44 AND `ducvupro` = 201;
UPDATE `task_sub_template` SET `NAME` = 'Nghe Whis dặn dò', `notify` = 'Whis ở Hành tinh Bill' WHERE `task_main_id` = 44 AND `ducvupro` = 202;
-- NV 45
UPDATE `task_sub_template` SET `NAME` = 'Tới Lãnh địa Fize', `notify` = 'Tới Lãnh địa Fize' WHERE `task_main_id` = 45 AND `ducvupro` = 203;
UPDATE `task_sub_template` SET `NAME` = 'Nhặt Mảnh Ký Ức thứ bảy', `notify` = 'Mảnh ký ức nằm ở Lãnh địa Fize' WHERE `task_main_id` = 45 AND `ducvupro` = 204;
UPDATE `task_sub_template` SET `NAME` = 'Rủ 1 người làm lễ hợp nhất', `notify` = 'Cần thêm 1 người chơi khác cùng khu' WHERE `task_main_id` = 45 AND `ducvupro` = 205;
UPDATE `task_sub_template` SET `NAME` = 'Hợp nhất 7 mảnh', `notify` = 'Dùng Lõi Ký Ức chưa hoàn chỉnh khi đủ 7 Mảnh Ký Ức' WHERE `task_main_id` = 45 AND `ducvupro` = 206;
UPDATE `task_sub_template` SET `NAME` = 'Nghe Thiên Sứ Whis', `notify` = 'Thiên Sứ Whis ở Lãnh địa Fize' WHERE `task_main_id` = 45 AND `ducvupro` = 207;
-- NV 46
UPDATE `task_sub_template` SET `NAME` = 'Gặp Dr. Myuu', `notify` = 'Dr. Myuu ở Phòng thí nghiệm Myuu' WHERE `task_main_id` = 46 AND `ducvupro` = 208;
UPDATE `task_sub_template` SET `NAME` = 'Hạ Heart', `notify` = 'Heart ở Phòng thí nghiệm Myuu' WHERE `task_main_id` = 46 AND `ducvupro` = 209;
UPDATE `task_sub_template` SET `NAME` = 'Đuổi tới Võ Đài Siêu Cấp', `notify` = 'Heart bỏ chạy tới Võ Đài Siêu Cấp' WHERE `task_main_id` = 46 AND `ducvupro` = 210;
UPDATE `task_sub_template` SET `NAME` = 'Hạ Heart Hư Không', `notify` = 'Heart biến hình ở Võ Đài Siêu Cấp' WHERE `task_main_id` = 46 AND `ducvupro` = 211;
UPDATE `task_sub_template` SET `NAME` = 'Hạ Heart Toàn Ký', `notify` = 'Dạng cuối của Heart, hạ hắn đi' WHERE `task_main_id` = 46 AND `ducvupro` = 212;
UPDATE `task_sub_template` SET `NAME` = 'Gặp Thiên Sứ Whis', `notify` = 'Thiên Sứ Whis ở Võ Đài Siêu Cấp' WHERE `task_main_id` = 46 AND `ducvupro` = 213;
-- NV 47
UPDATE `task_sub_template` SET `NAME` = 'Chọn số phận của Lõi', `notify` = 'Điểm rẽ nhánh: trả ký ức cho vũ trụ hay giữ Lõi Hư Không' WHERE `task_main_id` = 47 AND `ducvupro` = 214;
UPDATE `task_sub_template` SET `NAME` = 'Trả Lõi Hư Không', `notify` = 'Dùng Lõi Hư Không ở Võ Đài Siêu Cấp' WHERE `task_main_id` = 47 AND `ducvupro` = 215;
UPDATE `task_sub_template` SET `NAME` = 'Lên Thánh địa Kaio', `notify` = 'Lên Thánh địa Kaio' WHERE `task_main_id` = 47 AND `ducvupro` = 216;
UPDATE `task_sub_template` SET `NAME` = 'Mở giới hạn lần cuối', `notify` = 'Nhờ Tổ Sư Kaio mở giới hạn, lần này miễn phí' WHERE `task_main_id` = 47 AND `ducvupro` = 217;
UPDATE `task_sub_template` SET `NAME` = 'Nghe lời cuối Tổ Sư Kaio', `notify` = 'Tổ Sư Kaio ở Thánh địa Kaio' WHERE `task_main_id` = 47 AND `ducvupro` = 218;
UPDATE `task_sub_template` SET `NAME` = 'Hạ Hư Không Vô Danh', `notify` = 'Hư Không Vô Danh ở Võ Đài Siêu Cấp' WHERE `task_main_id` = 47 AND `ducvupro` = 219;
-- NV 48
UPDATE `task_sub_template` SET `NAME` = 'Gặp Berry ở Khu hang động', `notify` = 'Berry đứng ở Khu hang động' WHERE `task_main_id` = 48 AND `ducvupro` = 220;
UPDATE `task_sub_template` SET `NAME` = 'Chọn: Granola hay Jaco', `notify` = 'Điểm rẽ nhánh: chọn đi theo Granola hoặc báo Jaco' WHERE `task_main_id` = 48 AND `ducvupro` = 221;
UPDATE `task_sub_template` SET `NAME` = 'Gặp Jaco ở Trạm tàu vũ trụ', `notify` = 'Jaco ở Trạm tàu vũ trụ Trái Đất' WHERE `task_main_id` = 48 AND `ducvupro` = 222;
UPDATE `task_sub_template` SET `NAME` = 'Hạ 3 tên bị truy nã', `notify` = 'Truy nã Kuku, Mập Đầu Đinh và Rambo' WHERE `task_main_id` = 48 AND `ducvupro` = 223;
UPDATE `task_sub_template` SET `NAME` = 'Nhặt 3 Biên bản truy nã', `notify` = 'Biên bản rơi khi hạ Kuku, Mập Đầu Đinh, Rambo' WHERE `task_main_id` = 48 AND `ducvupro` = 224;
UPDATE `task_sub_template` SET `NAME` = 'Nộp biên bản cho Jaco', `notify` = 'Về Trạm tàu vũ trụ Trái Đất gặp Jaco' WHERE `task_main_id` = 48 AND `ducvupro` = 225;
-- NV 49
UPDATE `task_sub_template` SET `NAME` = 'Nghe Potage kể sự thật', `notify` = 'Potage ở Hang động Potaufeu' WHERE `task_main_id` = 49 AND `ducvupro` = 226;
UPDATE `task_sub_template` SET `NAME` = 'Chọn số phận bản sao', `notify` = 'Điểm rẽ nhánh: tiêu diệt hay thu nhận bản sao' WHERE `task_main_id` = 49 AND `ducvupro` = 227;
UPDATE `task_sub_template` SET `NAME` = 'Tới Võ đài Xên bọ hung', `notify` = 'Tới Võ đài Xên bọ hung' WHERE `task_main_id` = 49 AND `ducvupro` = 228;
UPDATE `task_sub_template` SET `NAME` = 'Rủ 1 người vào võ đài', `notify` = 'Võ đài cần một nhân chứng: 1 người chơi khác cùng khu' WHERE `task_main_id` = 49 AND `ducvupro` = 229;
UPDATE `task_sub_template` SET `NAME` = 'Đánh gục bản sao', `notify` = 'Đánh gục bản sao, đừng để nó chết' WHERE `task_main_id` = 49 AND `ducvupro` = 230;
UPDATE `task_sub_template` SET `NAME` = 'Dùng Bình chứa Commeson', `notify` = 'Dùng Bình chứa Commeson khi bản sao đã gục trên võ đài' WHERE `task_main_id` = 49 AND `ducvupro` = 231;
UPDATE `task_sub_template` SET `NAME` = 'Đạt 2 tỷ sức mạnh', `notify` = 'Luyện tập đến khi đạt 2 tỷ sức mạnh' WHERE `task_main_id` = 49 AND `ducvupro` = 232;
-- NV 50
UPDATE `task_sub_template` SET `NAME` = 'Chọn số phận của Lõi', `notify` = 'Điểm rẽ nhánh: trả ký ức cho vũ trụ hay giữ Lõi Hư Không' WHERE `task_main_id` = 50 AND `ducvupro` = 233;
UPDATE `task_sub_template` SET `NAME` = 'Hấp thụ Lõi Hư Không', `notify` = 'Dùng Lõi Hư Không ở Võ Đài Siêu Cấp, Lõi vẫn ở lại với ngươi' WHERE `task_main_id` = 50 AND `ducvupro` = 234;
UPDATE `task_sub_template` SET `NAME` = 'Về Hành tinh ngục tù', `notify` = 'Lõi kéo ngươi về Hành tinh ngục tù' WHERE `task_main_id` = 50 AND `ducvupro` = 235;
UPDATE `task_sub_template` SET `NAME` = 'Mở giới hạn lần cuối', `notify` = 'Nhờ Tổ Sư Kaio ở Thánh địa Kaio mở giới hạn, lần này miễn phí' WHERE `task_main_id` = 50 AND `ducvupro` = 236;
UPDATE `task_sub_template` SET `NAME` = 'Nghe lời cuối của Ôsin', `notify` = 'Ôsin ở Hành tinh ngục tù' WHERE `task_main_id` = 50 AND `ducvupro` = 237;
UPDATE `task_sub_template` SET `NAME` = 'Hạ Hư Không Vô Danh', `notify` = 'Hư Không Vô Danh ở Hành tinh ngục tù' WHERE `task_main_id` = 50 AND `ducvupro` = 238;

COMMIT;

-- =====================================================================
-- KIỂM TRA — chạy xong phải đúng kỳ vọng
-- =====================================================================

-- K1. Số nhiệm vụ / số bước.  KỲ VỌNG: 51 và 237
SELECT (SELECT COUNT(*) FROM `task_main_template`) AS so_nhiem_vu,
       (SELECT COUNT(*) FROM `task_sub_template`)  AS so_buoc;

-- K2. Dấu vân tay chữ mới (CRC32 cộng dồn).  KỲ VỌNG: dung_main = 1, dung_sub = 1
--     Nếu = 0: có dòng chưa được cập nhật (sai id / ducvupro) hoặc bị sửa tay.
SELECT (SELECT SUM(CRC32(CONCAT(`id`, '|', `NAME`, '|', `detail`)))
          FROM `task_main_template` WHERE `id` BETWEEN 0 AND 50) = 108651080850 AS dung_main,
       (SELECT SUM(CRC32(CONCAT(`task_main_id`, '|', `ducvupro`, '|', `NAME`, '|', `notify`)))
          FROM `task_sub_template`) = 505559561915 AS dung_sub;

-- K3. Tên bước dài nhất (chưa thay placeholder, chỉ để xem).  KỲ VỌNG: <= 28
SELECT MAX(CHAR_LENGTH(`NAME`)) AS ten_buoc_dai_nhat FROM `task_sub_template`;

-- K4. Mô tả dài nhất (chưa thay placeholder).  KỲ VỌNG: <= 200
SELECT MAX(CHAR_LENGTH(`detail`)) AS mo_ta_dai_nhat FROM `task_main_template`;

-- K5. Không còn ký tự '#'.  KỲ VỌNG: 0
SELECT COUNT(*) AS co_dau_thang FROM `task_sub_template`
WHERE `NAME` LIKE '%#%' OR `notify` LIKE '%#%';
