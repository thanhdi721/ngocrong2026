-- =====================================================================
-- 07-ro-map-va-boss.sql  —  GHI RÕ MAP / BOSS CHO BƯỚC NHIỆM VỤ + SỬA MŨI TÊN CHỈ ĐƯỜNG
-- Database: team2026        Chạy trên: MariaDB 10.4 (chỉ UPDATE / SELECT cơ bản)
-- Tài liệu: docs/4-trien-khai/41-ro-map-va-boss-nhiem-vu.md
--
-- Vì sao: người chơi báo NV 6 "Hạ 15 thằn lằn bay" đánh ở Rừng nấm KHÔNG được tính,
-- phải sang Rừng xương — chữ không nói map nào; "Hạ Kẻ Thu Gom" không nói là boss,
-- ở đâu. Rà cả 237 bước: server nay tính quái ở MỌI map (TaskService bản doc 41),
-- file này ghi rõ map / boss vào tên bước, câu nhắc (notify), dòng mục tiêu (detail)
-- và đặt lại mũi tên chỉ đường (cột `map`) cho đúng hành tinh.
--
-- DÀNH CHO SERVER ĐÃ CHẠY 02 + 05 + 06. Cài mới thì 02 đã chứa sẵn nội dung này,
-- nhưng nếu chạy 02 -> 05 -> 06 thì 06 ghi đè lại chữ cũ => luôn chạy 07 SAU CÙNG.
--
-- BẮT BUỘC ĐI KÈM JAVA (build lại jar): cột `map` dùng placeholder MỚI -11..-14 và chữ
-- dùng %15..%20. Jar cũ không biết các placeholder này => mũi tên trả nguyên -11 và chữ
-- hiện nguyên "%15". Đưa file này lên CÙNG LÚC với jar mới (ConstTask + TaskService doc 41).
--
-- AN TOÀN:
--   * CHỈ UPDATE: task_sub_template (NAME / notify / npc_id / map) khớp theo
--     task_main_id + ducvupro; task_main_template.detail khớp theo id.
--   * KHÔNG đụng max_count, ducvupro, số bước, bảng thưởng, data_task người chơi
--     => tiến độ người chơi giữ nguyên.
--   * Chạy lại bao nhiêu lần cũng cho cùng kết quả.
--   * Sau file này, câu K2 của 06 (dấu vân tay chữ 06) sẽ KHÔNG còn = 1 — đúng kỳ vọng.
--
-- CÁCH CHẠY:
--   mysqldump -u root -p team2026 task_main_template task_sub_template > backup_truoc_07_$(date +%F).sql
--   mysql -u root -p team2026 < SRC/sql/patch/07-ro-map-va-boss.sql
--   rồi KHỞI ĐỘNG LẠI server bằng jar mới (Manager chỉ nạp nhiệm vụ lúc khởi động;
--   người chơi đang online nhận chữ / mũi tên mới khi đăng nhập lại).
-- =====================================================================

SET NAMES utf8mb4;
START TRANSACTION;

-- ---------------------------------------------------------------------
-- (1) task_sub_template — 65 bước (tên / câu nhắc / NPC / mũi tên)
--     Placeholder mũi tên mới: -11 = 4/12/18 · -12 = 27/31/35 · -13 = 29/33/37 · -14 = 30/34/38
-- ---------------------------------------------------------------------
-- NV 3 bước 1: Tìm vật thể lạ rơi xuống -> Tìm đồ lạ ở %5
UPDATE `task_sub_template` SET `NAME` = 'Tìm đồ lạ ở %5'
WHERE `task_main_id` = 3 AND `ducvupro` = 12;
-- NV 4 bước 0: Hạ 12 %4
UPDATE `task_sub_template` SET `notify` = '%4 có ở %3 và %6. Hạ ở map nào cũng tính'
WHERE `task_main_id` = 4 AND `ducvupro` = 15;
-- NV 4 bước 1: Hạ 15 %4 mẹ
UPDATE `task_sub_template` SET `notify` = '%4 mẹ có ở %6 (và các khu rừng phía sau). Hạ ở map nào cũng tính'
WHERE `task_main_id` = 4 AND `ducvupro` = 16;
-- NV 5 bước 0: Tìm Kỷ Vật Của Ông
UPDATE `task_sub_template` SET `notify` = 'Hạ %9 ở %13 hoặc %15, kỷ vật sẽ rơi ra'
WHERE `task_main_id` = 5 AND `ducvupro` = 18;
-- NV 6 bước 0: Hạ 15 %9
UPDATE `task_sub_template` SET `notify` = '%9 có ở %13 và %15. Hạ ở map nào cũng tính', `map` = -7
WHERE `task_main_id` = 6 AND `ducvupro` = 21;
-- NV 6 bước 1: Hạ Kẻ Thu Gom -> Hạ boss Kẻ Thu Gom
UPDATE `task_sub_template` SET `NAME` = 'Hạ boss Kẻ Thu Gom', `notify` = 'Boss Kẻ Thu Gom xuất hiện ở %15, hồi sinh 15–30 phút', `map` = -11
WHERE `task_main_id` = 6 AND `ducvupro` = 22;
-- NV 7 bước 0: Hạ 10 quái mẹ trong 3 phút
UPDATE `task_sub_template` SET `notify` = 'Còn 3 phút! %14 có ở %15; hết giờ phải đếm lại từ đầu', `map` = -11
WHERE `task_main_id` = 7 AND `ducvupro` = 24;
-- NV 8 bước 2: Hạ 25 quái mẹ lấy lõi
UPDATE `task_sub_template` SET `notify` = 'Thằn lằn mẹ, phi long mẹ, quỷ bay mẹ đều tính; gần nhất ở %15', `map` = -11
WHERE `task_main_id` = 8 AND `ducvupro` = 29;
-- NV 9 bước 1: Hạ 20 %12
UPDATE `task_sub_template` SET `notify` = '%12 có ở %11, quanh nhà sư phụ. Hạ 20 con'
WHERE `task_main_id` = 9 AND `ducvupro` = 31;
-- NV 12 bước 1: Cùng đệ tử hạ 25 quái mẹ
UPDATE `task_sub_template` SET `notify` = 'Dẫn đệ tử đi hạ %14 ở %15; quái mẹ loại nào cũng tính', `map` = -11
WHERE `task_main_id` = 12 AND `ducvupro` = 40;
-- NV 13 bước 1: Cùng bạn bang hạ 30 quái mẹ
UPDATE `task_sub_template` SET `notify` = 'Quái mẹ ở %15. Cần 1 bạn cùng bang cùng khu; từ 3 người mỗi con tính 2', `map` = -11
WHERE `task_main_id` = 13 AND `ducvupro` = 43;
-- NV 14 bước 2: Hạ 20 heo chở hàng
UPDATE `task_sub_template` SET `notify` = 'Heo rừng, heo da xanh, heo Xayda đều tính; gần nhất ở %16', `map` = -12
WHERE `task_main_id` = 14 AND `ducvupro` = 47;
-- NV 15 bước 0: Hạ 20 quái mẹ ở điểm hẹn -> Hạ 20 quái mẹ
UPDATE `task_sub_template` SET `NAME` = 'Hạ 20 quái mẹ', `notify` = 'Điểm hẹn là %16; quái mẹ ở %15 cũng tính', `map` = -12
WHERE `task_main_id` = 15 AND `ducvupro` = 48;
-- NV 15 bước 1: Đánh bại Jaco mất ký ức -> Hạ boss Jaco mất ký ức
UPDATE `task_sub_template` SET `NAME` = 'Hạ boss Jaco mất ký ức', `notify` = 'Boss Jaco ở %16, hạ cả 2 dạng; hồi sinh 15–30 phút', `map` = -12
WHERE `task_main_id` = 15 AND `ducvupro` = 49;
-- NV 16 bước 0: Đi về vùng đất phía Nam -> Tới %17
UPDATE `task_sub_template` SET `NAME` = 'Tới %17', `notify` = 'Vùng phía Nam của hành tinh ngươi là %17', `map` = -13
WHERE `task_main_id` = 16 AND `ducvupro` = 51;
-- NV 16 bước 1: Hạ 40 quái chắn đường -> Hạ 40 %19
UPDATE `task_sub_template` SET `NAME` = 'Hạ 40 %19', `notify` = '%19 có ở %17 và %18; không tặc, quỷ đầu to, quỷ địa ngục đều tính', `map` = -13
WHERE `task_main_id` = 16 AND `ducvupro` = 52;
-- NV 16 bước 2: Hạ 30 quái canh bờ biển -> Hạ 30 %20
UPDATE `task_sub_template` SET `NAME` = 'Hạ 30 %20', `notify` = '%20 có ở %18; bulon, ukulele, quỷ mập đều tính', `map` = -14
WHERE `task_main_id` = 16 AND `ducvupro` = 53;
-- NV 16 bước 3: Nhặt 5 Vỏ đạn khắc dấu
UPDATE `task_sub_template` SET `notify` = 'Vỏ đạn rơi khi hạ %20 ở %18 (bulon, ukulele, quỷ mập đều rơi)', `map` = -14
WHERE `task_main_id` = 16 AND `ducvupro` = 54;
-- NV 18 bước 2: Hạ 30 quái vây thành
UPDATE `task_sub_template` SET `notify` = 'Akkuman ở Thành phố Vegeta; Tambourine ở Đông Karin, Drum ở Thung lũng Namếc', `map` = 19
WHERE `task_main_id` = 18 AND `ducvupro` = 62;
-- NV 19 bước 3: Cùng bạn hạ 30 Appule
UPDATE `task_sub_template` SET `notify` = 'Appule ở Núi Appule và vùng Raspberry. Cần 1 người khác cùng khu, mỗi con tính 2'
WHERE `task_main_id` = 19 AND `ducvupro` = 68;
-- NV 20 bước 3: Hạ 3 tay chân của Fide -> Hạ 3 boss tay chân Fide
UPDATE `task_sub_template` SET `NAME` = 'Hạ 3 boss tay chân Fide', `notify` = 'Boss Kuku ở Thung lũng Nappa, Mập Đầu Đinh ở Trại lính Fide, Rambo ở Đồi cây Fide', `map` = 68
WHERE `task_main_id` = 20 AND `ducvupro` = 73;
-- NV 20 bước 4: Nhặt 3 Thẻ tiền thưởng
UPDATE `task_sub_template` SET `map` = 68
WHERE `task_main_id` = 20 AND `ducvupro` = 74;
-- NV 21 bước 1: Phá trại hoặc hạ 300 quái
UPDATE `task_sub_template` SET `notify` = 'Không có bang? Chỉ tính quái ở Trại lính Fide, Núi dây leo, Núi cây quỷ, Trại quỷ già, Vực chết', `map` = 63
WHERE `task_main_id` = 21 AND `ducvupro` = 77;
-- NV 21 bước 2: Lấy bản đồ hành quân
UPDATE `task_sub_template` SET `notify` = 'Hỏi Lính canh ở Rừng Bamboo, hoặc Độc Nhãn khi đã phá xong doanh trại', `npc_id` = 25, `map` = 27
WHERE `task_main_id` = 21 AND `ducvupro` = 78;
-- NV 22 bước 1: Hạ 40 lính khỉ canh đường
UPDATE `task_sub_template` SET `notify` = 'Khỉ lông đen, Khỉ giáp sắt ở Hang quỷ chim, Núi khỉ đen, Hang khỉ đen'
WHERE `task_main_id` = 22 AND `ducvupro` = 81;
-- NV 22 bước 2: Hạ 5 tên Tiểu đội sát thủ -> Hạ 5 boss Tiểu đội sát thủ
UPDATE `task_sub_template` SET `NAME` = 'Hạ 5 boss Tiểu đội sát thủ', `notify` = 'Boss ở Núi khỉ đỏ, Hang quỷ chim, Núi khỉ đen, Hang khỉ đen; hồi sinh 5 phút'
WHERE `task_main_id` = 22 AND `ducvupro` = 82;
-- NV 23 bước 3: Hạ 2 dạng đầu của Fide -> Hạ boss Fide dạng 1 và 2
UPDATE `task_sub_template` SET `NAME` = 'Hạ boss Fide dạng 1 và 2', `notify` = 'Boss Fide đại ca ở Núi khỉ vàng, hồi sinh 10 phút'
WHERE `task_main_id` = 23 AND `ducvupro` = 88;
-- NV 23 bước 4: Hạ Fide dạng cuối -> Hạ boss Fide dạng cuối
UPDATE `task_sub_template` SET `NAME` = 'Hạ boss Fide dạng cuối', `notify` = 'Fide biến hình lần cuối ở Núi khỉ vàng, hạ hắn đi'
WHERE `task_main_id` = 23 AND `ducvupro` = 89;
-- NV 25 bước 1: Hạ 2 boss: Android 19, Kôrê
UPDATE `task_sub_template` SET `notify` = 'Boss ở Cao nguyên, Đảo Balê, Thành phố phía nam; hồi sinh 10 phút'
WHERE `task_main_id` = 25 AND `ducvupro` = 97;
-- NV 27 bước 2: Hạ 3 Android 13, 14, 15 -> Hạ 3 boss Android 13-14-15
UPDATE `task_sub_template` SET `NAME` = 'Hạ 3 boss Android 13-14-15', `notify` = 'Ba boss Android ở Sân sau siêu thị, hồi sinh 10 phút'
WHERE `task_main_id` = 27 AND `ducvupro` = 107;
-- NV 28 bước 2: Hạ 3 tên Poc, Pic, King Kong -> Hạ 3 boss Poc/Pic/King Kong
UPDATE `task_sub_template` SET `NAME` = 'Hạ 3 boss Poc/Pic/King Kong', `notify` = 'Boss ở Thành phố, Ngọn núi, Thung lũng phía bắc; hồi sinh 10 phút'
WHERE `task_main_id` = 28 AND `ducvupro` = 111;
-- NV 30 bước 2: Hạ 2 dạng đầu Xên bọ hung -> Hạ boss Xên bọ hung dạng 1-2
UPDATE `task_sub_template` SET `NAME` = 'Hạ boss Xên bọ hung dạng 1-2', `notify` = 'Boss Xên bọ hung ở Thị trấn Ginder, hồi sinh 15–30 phút'
WHERE `task_main_id` = 30 AND `ducvupro` = 120;
-- NV 30 bước 3: Hạ Xên hoàn thiện -> Hạ boss Xên hoàn thiện
UPDATE `task_sub_template` SET `NAME` = 'Hạ boss Xên hoàn thiện'
WHERE `task_main_id` = 30 AND `ducvupro` = 121;
-- NV 32 bước 3: Hạ 40 Cabira hoặc Tobi
UPDATE `task_sub_template` SET `notify` = 'Cabira, Tobi ở Khu hang động, Bìa rừng, Rừng nguyên thủy, Làng Plant nguyên thủy'
WHERE `task_main_id` = 32 AND `ducvupro` = 132;
-- NV 34 bước 1: Hạ 50 Tai tím hoặc Abo
UPDATE `task_sub_template` SET `notify` = 'Tai tím, Abo ở Cánh đồng tuyết, Rừng tuyết, Núi tuyết, Dòng sông băng'
WHERE `task_main_id` = 34 AND `ducvupro` = 142;
-- NV 34 bước 2: Hạ 20 Kado trong 5 phút
UPDATE `task_sub_template` SET `notify` = 'Còn 5 phút! Kado ở Dòng sông băng, Rừng băng, Hang băng; hết giờ phải đếm lại'
WHERE `task_main_id` = 34 AND `ducvupro` = 143;
-- NV 34 bước 4: Hạ Cooler cả 2 dạng -> Hạ boss Cooler cả 2 dạng
UPDATE `task_sub_template` SET `NAME` = 'Hạ boss Cooler cả 2 dạng', `notify` = 'Boss Cooler canh giữ Hang băng, hồi sinh 15–30 phút'
WHERE `task_main_id` = 34 AND `ducvupro` = 145;
-- NV 35 bước 1: Rủ 1 người đi cùng
UPDATE `task_sub_template` SET `npc_id` = -1, `map` = 81
WHERE `task_main_id` = 35 AND `ducvupro` = 148;
-- NV 35 bước 2: Tích 120 điểm diệt quái
UPDATE `task_sub_template` SET `notify` = 'Phó bản: mỗi quái 2 điểm. Ngoài: chỉ Dơi da xanh, Quỷ chim ở map 73/74/76/77/81/82, 1 điểm', `map` = 81
WHERE `task_main_id` = 35 AND `ducvupro` = 149;
-- NV 35 bước 3: Qua rắn độc hoặc hạ 200 quái
UPDATE `task_sub_template` SET `map` = 81
WHERE `task_main_id` = 35 AND `ducvupro` = 150;
-- NV 36 bước 2: Hạ Drabura hoặc 20 Cadic M -> Hạ boss Drabura/20 Cadic M
UPDATE `task_sub_template` SET `NAME` = 'Hạ boss Drabura/20 Cadic M', `notify` = 'Boss Drabura trong phi thuyền (12h-12h59), hoặc 20 Cadic M ở Sa mạc hoang vu'
WHERE `task_main_id` = 36 AND `ducvupro` = 154;
-- NV 37 bước 1: Hạ Mabư -> Hạ boss Mabư
UPDATE `task_sub_template` SET `NAME` = 'Hạ boss Mabư', `notify` = 'Boss Mabư trong phi thuyền, hoặc hạ quái Hirudegarn ở Thành phố Santa (24/7)', `map` = 126
WHERE `task_main_id` = 37 AND `ducvupro` = 158;
-- NV 37 bước 2: Hạ Drabura 3 / 30 Quỷ chim
UPDATE `task_sub_template` SET `notify` = 'Hạ boss Drabura 3, hoặc 30 Quỷ chim (Thành phố Santa, Hang quỷ chim, Núi đá...)', `map` = 126
WHERE `task_main_id` = 37 AND `ducvupro` = 159;
-- NV 37 bước 3: Nhặt Lõi Phép Babiđây
UPDATE `task_sub_template` SET `map` = 126
WHERE `task_main_id` = 37 AND `ducvupro` = 160;
-- NV 38 bước 2: Hạ Black Goku -> Hạ boss Black Goku
UPDATE `task_sub_template` SET `NAME` = 'Hạ boss Black Goku', `notify` = 'Boss Black Goku ở Nhà Bunma và các map Tương lai 92-100; hồi sinh 15–30 phút'
WHERE `task_main_id` = 38 AND `ducvupro` = 164;
-- NV 38 bước 3: Hạ Super Black Goku -> Hạ boss Super Black Goku
UPDATE `task_sub_template` SET `NAME` = 'Hạ boss Super Black Goku'
WHERE `task_main_id` = 38 AND `ducvupro` = 165;
-- NV 38 bước 4: Nhặt Nhẫn thời không
UPDATE `task_sub_template` SET `notify` = 'Nhẫn thời không sai lệch rơi ngẫu nhiên khi hạ boss Black Goku'
WHERE `task_main_id` = 38 AND `ducvupro` = 166;
-- NV 39 bước 2: Gọi Rồng Thần và ước
UPDATE `task_sub_template` SET `npc_id` = -1
WHERE `task_main_id` = 39 AND `ducvupro` = 170;
-- NV 39 bước 6: Hạ Baby cả 3 dạng -> Hạ boss Baby cả 3 dạng
UPDATE `task_sub_template` SET `NAME` = 'Hạ boss Baby cả 3 dạng', `notify` = 'Boss Baby xuất hiện ở Làng Kakarot, hồi sinh 15–30 phút'
WHERE `task_main_id` = 39 AND `ducvupro` = 174;
-- NV 41 bước 1: Hạ 60 quái vành đai rừng
UPDATE `task_sub_template` SET `notify` = 'Mọi quái ở vùng rừng quanh %16 (map 27-38, cả 3 hành tinh) đều tính', `map` = -12
WHERE `task_main_id` = 41 AND `ducvupro` = 181;
-- NV 41 bước 2: Hạ Broly -> Hạ boss Broly
UPDATE `task_sub_template` SET `NAME` = 'Hạ boss Broly', `notify` = 'Boss Broly lang thang ở %11 và vùng rừng map 27-38 (khu 2 trở lên)', `map` = -12
WHERE `task_main_id` = 41 AND `ducvupro` = 182;
-- NV 41 bước 3: Hạ Super Broly -> Hạ boss Super Broly
UPDATE `task_sub_template` SET `NAME` = 'Hạ boss Super Broly', `notify` = 'Super Broly hiện ra ngay tại chỗ Broly biến mất (vùng rừng map 27-38)', `map` = -12
WHERE `task_main_id` = 41 AND `ducvupro` = 183;
-- NV 42 bước 3: Hạ Cumber -> Hạ boss Cumber
UPDATE `task_sub_template` SET `NAME` = 'Hạ boss Cumber', `notify` = 'Boss Cumber ở Hành tinh ngục tù, hồi sinh 15–30 phút'
WHERE `task_main_id` = 42 AND `ducvupro` = 188;
-- NV 42 bước 4: Hạ Super Cumber -> Hạ boss Super Cumber
UPDATE `task_sub_template` SET `NAME` = 'Hạ boss Super Cumber'
WHERE `task_main_id` = 42 AND `ducvupro` = 189;
-- NV 43 bước 1: Tích 160 điểm diệt quái
UPDATE `task_sub_template` SET `notify` = 'Phó bản: mỗi quái 2 điểm. Ngoài: chỉ quái ở Hành tinh ngục tù, Khu hang động, Bìa rừng', `map` = 155
WHERE `task_main_id` = 43 AND `ducvupro` = 192;
-- NV 43 bước 2: Hạ Dr Lychee hoặc 50 quái -> Hạ boss Lychee hoặc 50 quái
UPDATE `task_sub_template` SET `NAME` = 'Hạ boss Lychee hoặc 50 quái', `map` = 155
WHERE `task_main_id` = 43 AND `ducvupro` = 193;
-- NV 43 bước 3: Hạ Hatchiyack hoặc 70 quái -> Hạ boss Hatchiyack/70 quái
UPDATE `task_sub_template` SET `NAME` = 'Hạ boss Hatchiyack/70 quái', `map` = 155
WHERE `task_main_id` = 43 AND `ducvupro` = 194;
-- NV 43 bước 4: Xong khí gas hoặc 100 quái
UPDATE `task_sub_template` SET `map` = 155
WHERE `task_main_id` = 43 AND `ducvupro` = 195;
-- NV 46 bước 1: Hạ Heart -> Hạ boss Heart
UPDATE `task_sub_template` SET `NAME` = 'Hạ boss Heart', `notify` = 'Boss Heart ở Phòng thí nghiệm Myuu, hồi sinh 15–30 phút'
WHERE `task_main_id` = 46 AND `ducvupro` = 209;
-- NV 46 bước 3: Hạ Heart Hư Không -> Hạ boss Heart Hư Không
UPDATE `task_sub_template` SET `NAME` = 'Hạ boss Heart Hư Không'
WHERE `task_main_id` = 46 AND `ducvupro` = 211;
-- NV 46 bước 4: Hạ Heart Toàn Ký -> Hạ boss Heart Toàn Ký
UPDATE `task_sub_template` SET `NAME` = 'Hạ boss Heart Toàn Ký'
WHERE `task_main_id` = 46 AND `ducvupro` = 212;
-- NV 47 bước 5: Hạ Hư Không Vô Danh -> Hạ boss Hư Không Vô Danh
UPDATE `task_sub_template` SET `NAME` = 'Hạ boss Hư Không Vô Danh'
WHERE `task_main_id` = 47 AND `ducvupro` = 219;
-- NV 48 bước 3: Hạ 3 tên bị truy nã -> Hạ 3 boss bị truy nã
UPDATE `task_sub_template` SET `NAME` = 'Hạ 3 boss bị truy nã', `notify` = 'Boss Kuku ở Thung lũng Nappa, Mập Đầu Đinh ở Trại lính Fide, Rambo ở Đồi cây Fide', `map` = 68
WHERE `task_main_id` = 48 AND `ducvupro` = 223;
-- NV 48 bước 4: Nhặt 3 Biên bản truy nã
UPDATE `task_sub_template` SET `map` = 68
WHERE `task_main_id` = 48 AND `ducvupro` = 224;
-- NV 50 bước 5: Hạ Hư Không Vô Danh -> Hạ boss Hư Không Vô Danh
UPDATE `task_sub_template` SET `NAME` = 'Hạ boss Hư Không Vô Danh'
WHERE `task_main_id` = 50 AND `ducvupro` = 238;

-- ---------------------------------------------------------------------
-- (2) task_main_template.detail — 18 nhiệm vụ: dòng mục tiêu nhắc tên map chính
-- ---------------------------------------------------------------------
UPDATE `task_main_template` SET `detail` = 'Ký ức bị rút đi luôn để lại một mỏ neo: kỷ vật của ông.
Hạ %9 ở %13 lấy kỷ vật, lau sạch, đưa cho ông.
Thưởng: 8.000 SM, 8.000 TN, 1 Rada cấp 2, 10 Đậu thần cấp 1'
WHERE `id` = 5;
UPDATE `task_main_template` SET `detail` = 'Kẻ áo choàng xám đang hút thứ trong suốt ra khỏi xác thú.
Hạ %9, diệt boss Kẻ Thu Gom ở %15, báo ông.
Thưởng: 12.000 SM, 12.000 TN, 1 Gói 30 đậu cấp 3'
WHERE `id` = 6;
UPDATE `task_main_template` SET `detail` = 'Vết nứt bắt đầu nuốt cả khu rừng. Không còn thời gian!
Hạ 10 quái mẹ ở %15 trong 3 phút, rồi tới trạm tàu.
Thưởng: 20.000 SM, 20.000 TN, 1 Mảnh Ký Ức 1, 75 Gói Capsule'
WHERE `id` = 7;
UPDATE `task_main_template` SET `detail` = 'Bunma chế được máy dò ký ức, chỉ còn thiếu vật liệu.
Gặp Bunma ở Siêu Thị, mua Rada cấp 1, hạ quái mẹ ở %15.
Thưởng: 50.000 SM, 50.000 TN, 1 Máy Dò Ký Ức, 1 Gói 30 đậu cấp 3'
WHERE `id` = 8;
UPDATE `task_main_template` SET `detail` = 'Quả trứng ông để dành cho ai đó bỗng nứt ra.
Nở trứng nhận đệ tử, cùng nó hạ quái mẹ ở %15 rồi về nhà.
Thưởng: 200.000 SM, 200.000 TN, 1 Đổi đệ tử, 1 Nâng kỹ năng 1 đệ tử'
WHERE `id` = 12;
UPDATE `task_main_template` SET `detail` = 'Ký ức một người thì dễ lấy, ký ức cả bang thì khó nuốt.
Vào bang, cùng bang hạ quái mẹ ở %15, gặp Giu-ma Đầu Bò.
Thưởng: 280.000 SM, 280.000 TN, 2 Gói 30 đậu cấp 3, 2 Đá bảo vệ'
WHERE `id` = 13;
UPDATE `task_main_template` SET `detail` = 'Có kẻ đang đóng hộp ký ức đem bán như hàng hóa.
Gặp Bunma, mua ở quầy Uron, chặn heo chở hàng ở %16.
Thưởng: 400.000 SM, 400.000 TN, 1 Hộp Ký Ức Bị Đánh Cắp, 10 Đá nâng cấp 1, 3 Đá bảo vệ'
WHERE `id` = 14;
UPDATE `task_main_template` SET `detail` = 'Chữ trên vận đơn là của Jaco, nhưng Jaco đã quên ngươi.
Tới %16, hạ boss Jaco mất ký ức rồi gặp lại hắn.
Thưởng: 600.000 SM, 600.000 TN, 1 Mảnh Ký Ức 2, 1 Gói 30 đậu cấp 3, 5 Đá nâng cấp 1'
WHERE `id` = 15;
UPDATE `task_main_template` SET `detail` = 'Máy dò ký ức rung lên, kim chỉ thẳng về phía Nam.
Dọn quái %17, %18, nhặt 5 Vỏ đạn, về gặp sư phụ.
Thưởng: 3 triệu SM, 3 triệu TN, 20 Đá nâng cấp 1'
WHERE `id` = 16;
UPDATE `task_main_template` SET `detail` = 'Trại lính Nappa đã mất trí và tấn công bất cứ ai.
Gặp Cui ở Thung lũng Nappa, dọn trại lính, rủ bạn hạ Appule.
Thưởng: 7 triệu SM, 7 triệu TN, 30 Đá nâng cấp 2'
WHERE `id` = 19;
UPDATE `task_main_template` SET `detail` = 'Heart mua chuộc Tiểu đội sát thủ để săn người còn ký ức.
Hạ trọn Tiểu đội sát thủ ở Núi khỉ đỏ, lấy máy đo cho Tapion.
Thưởng: 11 triệu SM, 11 triệu TN, 30 Đá nâng cấp 3'
WHERE `id` = 22;
UPDATE `task_main_template` SET `detail` = 'Máy dò bắt được sóng cơ khí phát lại chính ký ức của ngươi.
Dọn Xên con ở Thành phố phía đông rồi báo Bunma.
Thưởng: 80 triệu SM, 80 triệu TN, 20 Đá nâng cấp 4'
WHERE `id` = 24;
UPDATE `task_main_template` SET `detail` = 'Hai cỗ máy đầu tiên của Dr. Myuu vẫn còn nguyên dữ liệu.
Hạ Android 19, Dr.Kôrê ở Cao nguyên, mang 3 lõi về cho Bunma.
Thưởng: 100 triệu SM, 100 triệu TN, 30 Đá nâng cấp 4'
WHERE `id` = 25;
UPDATE `task_main_template` SET `detail` = 'King Kong không phải máy, nó là sinh vật bị nhồi kim loại vào đầu.
Dọn Xên con ở Thành phố phía bắc, hạ Poc, Pic, King Kong.
Thưởng: 180 triệu SM, 180 triệu TN, 20 Đá nâng cấp 5'
WHERE `id` = 28;
UPDATE `task_main_template` SET `detail` = 'Nhẫn thời không sai lệch đưa ngươi về hành tinh thực vật cổ xưa.
Tìm Bardock ở Khu hang động, dọn quái, nhặt 3 Mảnh Ký Ức Vỡ.
Thưởng: 200 triệu SM, 200 triệu TN, 30 Đậu thần cấp 8'
WHERE `id` = 32;
UPDATE `task_main_template` SET `detail` = 'Một kẻ mang khuôn mặt quen thuộc đang xóa sạch Tương lai.
Hạ boss Black Goku ở Tương lai, nhặt nhẫn về cho Bunma.
Thưởng: 550 triệu SM, 550 triệu TN, 2 Sao pha lê lục'
WHERE `id` = 38;
UPDATE `task_main_template` SET `detail` = 'Broly phát điên vì bị xóa sạch ký ức.
Dọn vành đai rừng quanh %16, hạ boss Broly và Super Broly.
Thưởng: 650 triệu SM, 650 triệu TN, 150 Mảnh quần, 50 Đậu thần cấp 8'
WHERE `id` = 41;
UPDATE `task_main_template` SET `detail` = 'Heart nuôi Cumber bằng ký ức của tù nhân.
Phá 60 lồng giam ở Hành tinh ngục tù, rồi hạ boss Cumber.
Thưởng: 750 triệu SM, 750 triệu TN, 150 Mảnh găng tay, 50 Đậu thần cấp 8'
WHERE `id` = 42;

COMMIT;

-- =====================================================================
-- (3) KIỂM TRA — chạy xong phần trên rồi xem kết quả
-- =====================================================================

-- K1: tổng số nhiệm vụ / bước không đổi.  KỲ VỌNG: 51 và 237
SELECT (SELECT COUNT(*) FROM `task_main_template`) AS so_nhiem_vu,
       (SELECT COUNT(*) FROM `task_sub_template`)  AS so_buoc;

-- K2: dấu vân tay của 65 bước + 18 mô tả vừa ghi.  KỲ VỌNG: dung_sub = 1, dung_main = 1
SELECT (SELECT SUM(CRC32(CONCAT_WS('|', task_main_id, ducvupro, `NAME`, notify, npc_id, `map`)))
          FROM `task_sub_template`
         WHERE (task_main_id, ducvupro) IN ((3,12), (4,15), (4,16), (5,18), (6,21), (6,22), (7,24), (8,29), (9,31), (12,40), (13,43), (14,47), (15,48), (15,49), (16,51), (16,52), (16,53), (16,54), (18,62), (19,68), (20,73), (20,74), (21,77), (21,78), (22,81), (22,82), (23,88), (23,89), (25,97), (27,107), (28,111), (30,120), (30,121), (32,132), (34,142), (34,143), (34,145), (35,148), (35,149), (35,150), (36,154), (37,158), (37,159), (37,160), (38,164), (38,165), (38,166), (39,170), (39,174), (41,181), (41,182), (41,183), (42,188), (42,189), (43,192), (43,193), (43,194), (43,195), (46,209), (46,211), (46,212), (47,219), (48,223), (48,224), (50,238))) = 140344485516 AS dung_sub,
       (SELECT SUM(CRC32(detail)) FROM `task_main_template` WHERE id IN (5, 6, 7, 8, 12, 13, 14, 15, 16, 19, 22, 24, 25, 28, 32, 38, 41, 42)) = 39243130036 AS dung_main;

-- K3: số bước dùng placeholder mũi tên mới.  KỲ VỌNG: -11 = 5, -12 = 6, -13 = 2, -14 = 2
SELECT `map`, COUNT(*) AS so_buoc FROM `task_sub_template`
WHERE `map` IN (-11, -12, -13, -14) GROUP BY `map` ORDER BY `map` DESC;

-- K4: bước đánh boss phải có chữ "boss" trong tên (trừ bản sao / Whis đánh qua NPC).
--     KỲ VỌNG: 0 dòng
SELECT task_main_id, ducvupro, `NAME` FROM `task_sub_template`
WHERE `NAME` LIKE 'Hạ %' AND `NAME` NOT LIKE '%boss%'
  AND (task_main_id, ducvupro) IN ((6,22),(15,49),(20,73),(22,82),(23,88),(23,89),(25,97),(27,107),(28,111),
      (30,120),(30,121),(34,145),(36,154),(37,158),(38,164),(38,165),(39,174),(41,182),(41,183),
      (42,188),(42,189),(43,193),(43,194),(46,209),(46,211),(46,212),(47,219),(48,223),(50,238));

-- K5: tên bước dài nhất (đo trên chữ còn placeholder).  KỲ VỌNG: <= 28
SELECT MAX(CHAR_LENGTH(`NAME`)) AS ten_dai_nhat FROM `task_sub_template`;
