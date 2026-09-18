-- =====================================================================
-- 01-vat-pham-moi.sql  —  VẬT PHẨM MỚI CHO TUYẾN NHIỆM VỤ CHÍNH MỚI
-- Database: team2026        Sinh ngày: 2026-09-18
--
-- Nguồn: docs/2-thiet-ke-nhiem-vu-moi/20, 20a, 20b, 20c
--        docs/3-kiem-tra-can-bang/21b-item-moi-dac-ta.md
--        docs/4-trien-khai/22-san-sang-code.md (mục 0 — quyết định đã chốt)
-- Bảng chốt id + danh sách code cần vá:
--        docs/4-trien-khai/25-bang-id-vat-pham-moi.md
--
-- ---------------------------------------------------------------------
-- ĐỌC TRƯỚC KHI CHẠY
-- ---------------------------------------------------------------------
-- 1. SAO LƯU TRƯỚC:
--      mysqldump -u root -p team2026 item_template data_badges item_shop \
--        > backup_item_$(date +%F).sql
--
-- 2. LUẬT SẮT — ID PHẢI LIÊN TỤC, KHÔNG ĐƯỢC HỞ MỘT SỐ NÀO:
--      nro/models/services/ItemService.java:360
--          public Template.ItemTemplate getTemplate(int id) {
--              return Manager.ITEM_TEMPLATES.get(id);   // <-- CHỈ SỐ MẢNG
--          }
--      nro/models/data/ItemData.java:37-93 gửi bảng item xuống client THEO
--      THỨ TỰ CHỈ SỐ MẢNG và KHÔNG KÈM id (chỉ 9 trường: type, gender, name,
--      description, level, strRequire, iconID, part, isUpToUp).
--    => Hở 1 id là toàn bộ bảng item từ đó về sau lệch một bậc: client hiện
--       sai tên/icon mọi item phía sau, và item cuối ném IndexOutOfBoundsException.
--    DB hiện tại: đúng 2000 dòng, id 0..1999, liên tục, không trùng (đã kiểm tra).
--    File này thêm ĐÚNG 32 dòng, id 2000..2031, liên tục => tổng 2032 dòng.
--    KHÔNG cần dòng trống đệm nào (khác đề xuất cũ ở 21b §3.6, vì id đã được
--    nén lại thành một khối liền).
--
-- 3. SAU KHI IMPORT, BẮT BUỘC (nếu thiếu thì item mới hiện tên rỗng/icon trắng):
--      a) nro/models/data/DataGame.java:37   vsItem = 9  ->  10
--         (đã sửa sẵn trong bản giao này — kiểm tra lại trước khi build)
--      b) Build lại 20.jar và KHỞI ĐỘNG LẠI server.
--
-- 4. Ảnh icon (đã kiểm tra tồn tại đủ ở x1, x2, x3, x4):
--      - 2000..2008 dùng icon RIÊNG 20000..20008 (SRC/data/icon/x1..x4/2000x.png)
--      - 2009..2031 MƯỢN icon của item sẵn có, ghi rõ ở comment từng dòng.
--
-- 5. Chặn bán / chặn vứt KHÔNG làm được bằng SQL:
--    ShopService.sellItem có  if (cost == 0) cost = 1;  nên gold = 0 VẪN bán
--    được 1 vàng/cái. Việc chặn nằm ở code — xem doc 25 mục 4.
-- =====================================================================

START TRANSACTION;

-- ---------------------------------------------------------------------
-- (1) 32 VẬT PHẨM MỚI — id 2000..2031, LIÊN TỤC
--     Cột: id, TYPE, gender, NAME, description, level, icon_id,
--          part, is_up_to_up, power_require, gold, gem, head, body, leg
--
--     Quy ước chung cho MỌI dòng:
--       gender        = 3   dùng chung 3 hành tinh (0/1/2 sẽ kẹt 2 hành tinh)
--       power_require = 0   UseItem.useItem:267 chặn nút "Dùng" nếu > sức mạnh
--       gold, gem     = 0   không tạo nguồn in vàng (bán vẫn ra 1 vàng -> chặn ở code)
--       head/body/leg = -1  không phải cải trang
--       part          = -1  trừ 2 danh hiệu TYPE 36 (part = idEffect, thông lệ DB)
--       level         = 1   theo thông lệ item 992/796/674; danh hiệu để 0
--
--     TYPE 8  = "vật phẩm nhiệm vụ" đúng nghĩa:
--               KHÔNG mặc được   (InventoryService.putItemBody:297 từ chối)
--               KHÔNG giao dịch  (Trade.isItemCannotTran case 8)
--               KHÔNG ký gửi     (ConsignShopService.itemCanConsign)
--               vẫn rơi xuống switch(template.id) trong UseItem nếu sau này cần.
--     TYPE 27 = CHỈ dùng cho 7 món BẮT BUỘC bấm "Dùng" (trigger A12):
--               2000, 2002, 2010, 2013, 2015, 2020, 2024.
--               TYPE 27 MẶC ĐỊNH GIAO DỊCH ĐƯỢC và mặc được vào ô 7 (ô pet)
--               => mọi nơi tạo item PHẢI gắn option 30 "Không thể giao dịch";
--                  putItemBody đã được vá để không cho mặc (doc 25 mục 4).
--     TYPE 36 = danh hiệu, không nằm trong hành trang (xem mục 2 và 3 bên dưới).
--     KHÔNG dùng TYPE 11: TYPE 11 trong server này là "đồ đeo lưng / flag bag",
--     part là id trong bảng flag_bag; part = -1 sẽ gây lỗi hiển thị ở client.
-- ---------------------------------------------------------------------
INSERT INTO `item_template`
(`id`, `TYPE`, `gender`, `NAME`, `description`, `level`, `icon_id`, `part`, `is_up_to_up`, `power_require`, `gold`, `gem`, `head`, `body`, `leg`)
VALUES

-- 2000 Lõi Hư Không — thưởng NV 45 bước 3. Ảnh RIÊNG (icon 20000).
--      NV 47 (nhánh A): dùng ở map 145 -> TRỪ ĐI, trao 2001 Vỏ Lõi rỗng.
--      NV 50 (nhánh B): dùng ở map 145 -> GIỮ LẠI vĩnh viễn làm cờ hậu truyện
--      => BẮT BUỘC chặn bán + chặn vứt, nếu không người chơi tự xóa cờ của mình.
(2000, 27, 3, 'Lõi Hư Không', 'Lõi chứa toàn bộ ký ức vũ trụ. Ngươi quyết định số phận nó.', 1, 20000, -1, 0, 0, 0, 0, -1, -1, -1),

-- 2001 Vỏ Lõi rỗng — trao ở NV 47 bước 1 (nhánh A). Ảnh RIÊNG (icon 20001).
--      Kỷ vật vĩnh viễn, không tiêu đi => BẮT BUỘC chặn bán + chặn vứt.
(2001,  8, 3, 'Vỏ Lõi rỗng', 'Vỏ lõi sau khi ký ức đã trả về vũ trụ. Kỷ vật.', 1, 20001, -1, 0, 0, 0, 0, -1, -1, -1),

-- 2002 Mảnh Ký Ức 1 — thưởng NV 7. Ảnh RIÊNG (icon 20002).
--      TYPE 27 (khác 6 mảnh còn lại) vì NV 40 bước 3 bắt phải bấm "Dùng"
--      (trigger A12 checkDoneTaskUseItem).
(2002, 27, 3, 'Mảnh Ký Ức 1', 'Một mảnh ký ức của vũ trụ. Cần đủ bảy mảnh.', 1, 20002, -1, 0, 0, 0, 0, -1, -1, -1),

-- 2003 Mảnh Ký Ức 2 — thưởng NV 15. Ảnh RIÊNG (icon 20003).
(2003,  8, 3, 'Mảnh Ký Ức 2', 'Một mảnh ký ức của vũ trụ. Cần đủ bảy mảnh.', 1, 20003, -1, 0, 0, 0, 0, -1, -1, -1),

-- 2004 Mảnh Ký Ức 3 — thưởng NV 23. Ảnh RIÊNG (icon 20004).
(2004,  8, 3, 'Mảnh Ký Ức 3', 'Một mảnh ký ức của vũ trụ. Cần đủ bảy mảnh.', 1, 20004, -1, 0, 0, 0, 0, -1, -1, -1),

-- 2005 Mảnh Ký Ức 4 — thưởng NV 31 hoặc NV 49 (điểm rẽ nhánh). Ảnh RIÊNG (icon 20005).
(2005,  8, 3, 'Mảnh Ký Ức 4', 'Một mảnh ký ức của vũ trụ. Cần đủ bảy mảnh.', 1, 20005, -1, 0, 0, 0, 0, -1, -1, -1),

-- 2006 Mảnh Ký Ức 5 — NPC 29 Rồng Omega trao ở NV 39 bước 3. Ảnh RIÊNG (icon 20006).
--   !! id 2006 đang bị hardcode làm "thẻ đổi tên" ở UseItem.java:878 và
--      Input.java:431 — ĐÃ VÁ trong bản giao này (xem doc 25 mục 4).
(2006,  8, 3, 'Mảnh Ký Ức 5', 'Một mảnh ký ức của vũ trụ. Cần đủ bảy mảnh.', 1, 20006, -1, 0, 0, 0, 0, -1, -1, -1),

-- 2007 Mảnh Ký Ức 6 — NPC 70 Bardock trao ở NV 39 bước 5. Ảnh RIÊNG (icon 20007).
(2007,  8, 3, 'Mảnh Ký Ức 6', 'Một mảnh ký ức của vũ trụ. Cần đủ bảy mảnh.', 1, 20007, -1, 0, 0, 0, 0, -1, -1, -1),

-- 2008 Mảnh Ký Ức 7 — nhặt trên map 78, chỉ hiện với người ở TASK_45_1.
--      Ảnh RIÊNG (icon 20008).
(2008,  8, 3, 'Mảnh Ký Ức 7', 'Một mảnh ký ức của vũ trụ. Cần đủ bảy mảnh.', 1, 20008, -1, 0, 0, 0, 0, -1, -1, -1),

-- 2009 Mảnh Vỡ Hư Không — NV 2 bước 2. Rơi từ mob 1/2/3 khi ở TASK_2_2.
--      Mượn icon 1421 của item 225 "Mảnh đá vụn".
--      is_up_to_up = 1 vì phải nhặt nhiều cái (hành trang tân thủ chỉ 30 ô).
(2009,  8, 3, 'Mảnh Vỡ Hư Không', 'Mảnh vỡ lạ rơi ra từ quái. Mang về cho người cần nó.', 1, 1421, -1, 1, 0, 0, 0, -1, -1, -1),

-- 2010 Kỷ Vật Của Ông — NV 5 bước 0-2. Rơi từ mob 7/8/9 khi ở TASK_5_0.
--      Mượn icon 9067 của item 992 "Nhẫn thời không sai lệch".
--      TYPE 27: NV 5 bước 1 là trigger A12, phải bấm "Dùng" được.
(2010, 27, 3, 'Kỷ Vật Của Ông', 'Chiếc nhẫn cũ. Lau sạch rồi đưa cho người trong nhà.', 1, 9067, -1, 0, 0, 0, 0, -1, -1, -1),

-- 2011 Máy Dò Ký Ức — thưởng hoàn thành NV 8. Mượn icon 1089 của item 12 "Rada cấp 1".
--      Là vật chứng giữ trong hành trang cho chuỗi sau, KHÔNG tiêu đi
--      => BẮT BUỘC chặn bán + chặn vứt, mất là kẹt.
(2011,  8, 3, 'Máy Dò Ký Ức', 'Thiết bị dò tìm dấu vết ký ức bị đánh cắp.', 1, 1089, -1, 0, 0, 0, 0, -1, -1, -1),

-- 2012 Hộp Ký Ức Bị Đánh Cắp — thưởng NV 14, nộp ở NV 15.
--      Mượn icon 7222 của item 796 "Hộp Capsule".
(2012,  8, 3, 'Hộp Ký Ức Bị Đánh Cắp', 'Vật chứng lấy từ kẻ thu gom. Dẫn tới nhiệm vụ 15.', 1, 7222, -1, 0, 0, 0, 0, -1, -1, -1),

-- 2013 Hạt Giống Hy Vọng — trao ở thưởng bước 0 của NV 11.
--      Mượn icon 241 của item 13 "Đậu thần cấp 1".
--      TYPE 27 + power_require = 0 BẮT BUỘC: NV 11 ở chương 2, sức mạnh còn thấp;
--      UseItem.useItem:267 chặn nếu power_require > sức mạnh => kẹt vĩnh viễn.
(2013, 27, 3, 'Hạt Giống Hy Vọng', 'Gieo tại nhà để cây đậu thần lớn thêm một cấp.', 1, 241, -1, 0, 0, 0, 0, -1, -1, -1),

-- 2014 Vỏ đạn khắc dấu — NV 16 bước 3. Rơi 25% từ mob 22/23/24 khi ở TASK_16_3.
--      Mượn icon 1421 của item 225 "Mảnh đá vụn".
--      (20b đặt id 2040 — id đó trùng mảng hardcode ItemService.vatphamsk, đã dời.)
(2014,  8, 3, 'Vỏ đạn khắc dấu', 'Vỏ đạn có khắc ký hiệu lạ. Nhặt đủ 5 cái.', 1, 1421, -1, 1, 0, 0, 0, -1, -1, -1),

-- 2015 Búa rèn cũ — NPC trao ở bước 0 của NV 17. Mượn icon 1417 của item 223 "Đá Titan".
--      TYPE 27: NV 17 bước 2 là trigger A12 (dùng búa để rèn lại vũ khí).
(2015, 27, 3, 'Búa rèn cũ', 'Búa rèn của người thợ già. Dùng một lần rồi hỏng.', 1, 1417, -1, 0, 0, 0, 0, -1, -1, -1),

-- 2016 Thẻ tiền thưởng Granola — NV 20 bước 4. Rơi 100% từ boss -20/-21/-22 ở TASK_20_4.
--      Mượn icon 5428 của item 611 "Bản đồ kho báu".
(2016,  8, 3, 'Thẻ tiền thưởng Granola', 'Thẻ tiền thưởng của thợ săn. Nhặt đủ 3 cái.', 1, 5428, -1, 1, 0, 0, 0, -1, -1, -1),

-- 2017 Biên bản truy nã Ngân Hà — NV 48 bước 4. Rơi 100% từ boss -20/-21/-22 ở TASK_48_4.
--      Mượn icon 5207 của item 590 "Bí kiếp".
--      LƯU Ý: 21b đề xuất icon 5428 — ĐÃ ĐỔI sang 5207 để KHÔNG trùng icon với 2016.
(2017,  8, 3, 'Biên bản truy nã Ngân Hà', 'Biên bản truy nã của cảnh sát vũ trụ. Nhặt đủ 3 cái.', 1, 5207, -1, 1, 0, 0, 0, -1, -1, -1),

-- 2018 Máy đo ký ức — NV 22 bước 3. Rơi 100% từ boss -27 Tiểu đội trưởng ở TASK_22_3.
--      Mượn icon 6467 của item 674 "Đá ngũ sắc".
(2018,  8, 3, 'Máy đo ký ức', 'Máy đo do Tiểu đội trưởng mang theo.', 1, 6467, -1, 0, 0, 0, 0, -1, -1, -1),

-- 2019 Lõi năng lượng Android — NV 25 bước 2. Rơi 100% từ boss -30/-31 ở TASK_25_2.
--      Mượn icon 8620 của item 935 "Đá xanh lam".
--   !! id 2019 nằm trong mảng hardcode ItemService.vatphamsk:638 — ĐÃ VÁ (doc 25 mục 4).
(2019,  8, 3, 'Lõi năng lượng Android', 'Lõi năng lượng moi từ người máy. Nhặt đủ 3 cái.', 1, 8620, -1, 1, 0, 0, 0, -1, -1, -1),

-- 2020 Mẫu kim loại có ký ức — NPC 21 Bà Hạt Mít trao ở bước 0 NV 26.
--      Mượn icon 2288 của item 362 "Hóa thạch Ngọc Rồng".
--      TYPE 27: bấm "Dùng" -> phát 3 dòng thoại -> tự xóa.
--   !! id 2020 nằm trong mảng hardcode ItemService.vatphamsk:638 — ĐÃ VÁ.
(2020, 27, 3, 'Mẫu kim loại có ký ức', 'Mẩu kim loại còn lưu ký ức. Dùng để nghe lại.', 1, 2288, -1, 0, 0, 0, 0, -1, -1, -1),

-- 2021 Mảnh giáp khắc tên — NV 28 bước 3. Rơi 100% từ boss -37 King Kong ở TASK_28_3.
--      Mượn icon 10197 của item 1066 "Mảnh áo".
--   !! id 2021 nằm trong mảng hardcode ItemService.vatphamsk:638 — ĐÃ VÁ.
(2021,  8, 3, 'Mảnh giáp khắc tên', 'Mảnh giáp có khắc một cái tên đã mờ.', 1, 10197, -1, 0, 0, 0, 0, -1, -1, -1),

-- 2022 Thẻ từ phòng thí nghiệm — NPC 37 Bunma trao ở bước 0 NV 29; điều kiện vào map 166.
--      Mượn icon 9406 của item 987 "Đá bảo vệ".
--   !! id 2022 nằm trong mảng hardcode ItemService.vatphamsk:638 — ĐÃ VÁ.
(2022,  8, 3, 'Thẻ từ phòng thí nghiệm', 'Thẻ từ giả của Bunma. Cần có để vào phòng thí nghiệm.', 1, 9406, -1, 0, 0, 0, 0, -1, -1, -1),

-- 2023 Bản thiết kế bản sao — NV 29. Sinh sẵn 5-8 ItemMap trên map 166, nhặt trong 6 phút.
--      Mượn icon 12846 của item 1560 "Rương ngọc rồng".
--      (20b đặt id 2074 — id đó trùng hardcode InventoryService.addItemList:842, đã dời.)
--   !! id 2023 nằm trong mảng hardcode ItemService.vatphamsk:638 — ĐÃ VÁ.
(2023,  8, 3, 'Bản thiết kế bản sao', 'Bản thiết kế đánh cắp được. Nhặt đủ 5 bản trong 6 phút.', 1, 12846, -1, 1, 0, 0, 0, -1, -1, -1),

-- 2024 Lõi Ký Ức chưa hoàn chỉnh — trao ở NV 45 bước 1.
--      Mượn icon 9650 của item 1015 "Ngọc rồng Siêu Cấp".
--      TYPE 27: bấm "Dùng" -> kiểm tra đủ 7 mảnh 2002..2008 -> trừ cả 7 + trừ 2024
--      -> trao 2000 Lõi Hư Không.
--   !! id 2024 nằm trong mảng hardcode ItemService.vatphamsk:638 — ĐÃ VÁ.
(2024, 27, 3, 'Lõi Ký Ức chưa hoàn chỉnh', 'Lõi rỗng. Dùng khi đã đủ bảy Mảnh Ký Ức.', 1, 9650, -1, 0, 0, 0, 0, -1, -1, -1),

-- 2025 Mảnh Ký Ức Vỡ — NV 32 bước 4-5. Rơi từ mob 81 Tobi khi ở TASK_32_4;
--      nộp NPC 70 Bardock ở map 160. Mượn icon 6467 của item 674 "Đá ngũ sắc".
--   !! id 2025 nằm trong mảng hardcode ItemService.vatphamsk:638 — ĐÃ VÁ.
(2025,  8, 3, 'Mảnh Ký Ức Vỡ', 'Mảnh vỡ nhặt ở làng Plant. Nhặt đủ 3 cái.', 1, 6467, -1, 1, 0, 0, 0, -1, -1, -1),

-- 2026 Mảnh Ký Ức Đóng Băng — NV 34 bước 3-5. Rơi ở map 110, chỉ hiện với người ở TASK_34_3.
--      Mượn icon 8620 của item 935 "Đá xanh lam".
--   !! id 2026 nằm trong mảng hardcode ItemService.vatphamsk:638 — ĐÃ VÁ.
(2026,  8, 3, 'Mảnh Ký Ức Đóng Băng', 'Mảnh ký ức bị đóng băng trong hang.', 1, 8620, -1, 0, 0, 0, 0, -1, -1, -1),

-- 2027 Mảnh Bùa Babiđây — NV 36 bước 3 (đường vòng khi ngoài giờ phó bản).
--      Rơi từ Cadic M ở map 165. Mượn icon 7743 của item 861 "Hồng ngọc".
(2027,  8, 3, 'Mảnh Bùa Babiđây', 'Mảnh bùa phép của Babiđây. Đường vòng xuống cửa ải.', 1, 7743, -1, 0, 0, 0, 0, -1, -1, -1),

-- 2028 Lõi Phép Babiđây — NV 37 bước 3-4. Rơi 100% cho người kết liễu Mabư ở bước 2.
--      Mượn icon 5829 của item 638 "Bình chứa Commeson".
(2028,  8, 3, 'Lõi Phép Babiđây', 'Lõi phép rơi ra khi Mabư gục xuống.', 1, 5829, -1, 0, 0, 0, 0, -1, -1, -1),

-- 2029 Ống nghiệm Myuu — NV 46 bước 4. Rơi từ Heart form 3 (boss -108108).
--      Mượn icon 6849 của item 727 "Siêu thần thủy".
(2029,  8, 3, 'Ống nghiệm Myuu', 'Ống nghiệm của Dr. Myuu, lấy từ Heart.', 1, 6849, -1, 0, 0, 0, 0, -1, -1, -1),

-- 2030 DANH HIỆU "Người Trả Ký Ức" — trao khi xong NV 47 (nhánh A).
--      TYPE 36. part = 257 = idEffect (thông lệ DB: item 1291 part=220, 1293 part=222).
--      Mượn icon 11614 (đang là icon của item 1293 "Cao thủ siêu hạng").
(2030, 36, 3, 'Người Trả Ký Ức', 'Danh hiệu cho người đã trả ký ức về cho vũ trụ.', 0, 11614, 257, 0, 0, 0, 0, -1, -1, -1),

-- 2031 DANH HIỆU "Kẻ Giữ Hư Không" — trao khi xong NV 50 (nhánh B).
--      TYPE 36. part = 258 = idEffect.
--      Mượn icon 11617 (đang là icon của item 1291 "Trùm săn Boss").
(2031, 36, 3, 'Kẻ Giữ Hư Không', 'Danh hiệu cho người đã giữ lại Lõi Hư Không.', 0, 11617, 258, 0, 0, 0, 0, -1, -1, -1);
-- ---------------------------------------------------------------------
-- (2) 2 DÒNG data_badges CHO 2 DANH HIỆU — BẮT BUỘC
--     Thiếu 2 dòng này thì BagesTemplate.fineIdEffectbyIdItem() trả -1 và
--     tab shop 44/45 luôn hiện 0% (đúng lỗi đang xảy ra với 1286/1287/1300).
--     data_badges hiện có 16 dòng, id lớn nhất = 18.
--
--     idEffect 257/258: đã kiểm tra thực tế — 257 và 258 HOÀN TOÀN TRỐNG
--     (không có data/effdata/DataEffect_257|258, không có
--      data/effect/x1..x4/ImgEffect_257|258.png).
--     20c đề xuất 260/261 nhưng 260/261 CŨNG không có tài nguyên.
--     => PHẢI VẼ 10 FILE (2 DataEffect + 8 ImgEffect x1..x4) trước khi phát hành,
--        nếu không danh hiệu vẫn cộng chỉ số nhưng KHÔNG hiện hiệu ứng trên đầu
--        (DataGame.sendEffectTemplate:264-290 lặng lẽ return khi thiếu file).
--
--     Chỉ số 12/12/12 = ngang danh hiệu "X-mas" (id 15) — mức cao nhất hiện có.
--     Hai danh hiệu CỐ Ý bằng nhau để việc chọn nhánh là lựa chọn kể chuyện.
--     Option 50 (Sức đánh %), 77 (HP %), 103 (KI %) là 3 option duy nhất NPoint
--     thực sự áp dụng cho danh hiệu.
--     CỐ Ý BỎ option 93: đây là danh hiệu kết thúc tuyến, phải trao VĨNH VIỄN
--     bằng  new BadgesData(player, 257|258, 36500)  trong doneTask, KHÔNG đi qua
--     BadgesTaskService (vốn cứng 30 ngày).
--     LƯU Ý khi code: constructor BadgesData(Player,int,int) dòng 39 ĐÃ TỰ
--     player.dataBadges.add(this) — gọi add() lần nữa sẽ nhân đôi chỉ số.
-- ---------------------------------------------------------------------
INSERT INTO `data_badges` (`id`, `idEffect`, `idItem`, `NAME`, `Options`) VALUES
(19, 257, 2030, 'Người Trả Ký Ức', '[{"param":12,"id":50},{"param":12,"id":77},{"param":12,"id":103}]'),
(20, 258, 2031, 'Kẻ Giữ Hư Không', '[{"param":12,"id":50},{"param":12,"id":77},{"param":12,"id":103}]');

-- ---------------------------------------------------------------------
-- (3) 4 DÒNG item_shop ĐỂ 2 DANH HIỆU HIỆN Ở NPC 39 SANTA
--     tab 44 = "Danh<>Hiệu", tab 45 = "Sở Hữu<>", cả hai thuộc shop id 26.
--     Không thêm thì người chơi KHÔNG xem/không bật được danh hiệu mới.
--     is_sell = 0 => chỉ hiển thị, không bán bằng tiền (danh hiệu trao qua nhiệm vụ).
--     item_shop hiện có 826 dòng, id lớn nhất = 998.
--     Cột create_time có DEFAULT current_timestamp nên không cần khai.
-- ---------------------------------------------------------------------
INSERT INTO `item_shop` (`id`, `tab_id`, `temp_id`, `is_new`, `is_sell`, `type_sell`, `cost`, `icon_spec`) VALUES
(999,  44, 2030, 1, 0, 0, 0, 0),
(1000, 44, 2031, 1, 0, 0, 0, 0),
(1001, 45, 2030, 1, 0, 0, 0, 0),
(1002, 45, 2031, 1, 0, 0, 0, 0);

COMMIT;

-- =====================================================================
-- (4) KIỂM TRA SAU KHI IMPORT — chạy cả 5 câu, TẤT CẢ phải đúng kỳ vọng.
--     Nếu 4.1 hoặc 4.2 sai thì ĐỪNG khởi động server: bảng item sẽ lệch.
-- =====================================================================

-- 4.1 Tổng số dòng và biên id.
--     KỲ VỌNG: so_dong = 2032, nho_nhat = 0, lon_nhat = 2031, lech = 0
SELECT COUNT(*)                 AS so_dong,
       MIN(id)                  AS nho_nhat,
       MAX(id)                  AS lon_nhat,
       COUNT(*) - (MAX(id) + 1) AS lech_phai_bang_0
FROM `item_template`;

-- 4.2 Không được hở id nào.  KỲ VỌNG: 0 dòng trả về.
SELECT a.id + 1 AS id_bi_thieu
FROM `item_template` a
LEFT JOIN `item_template` b ON b.id = a.id + 1
WHERE b.id IS NULL
  AND a.id < (SELECT MAX(id) FROM `item_template`)
ORDER BY 1;

-- 4.3 Không được trùng id.  KỲ VỌNG: 0 dòng trả về.
SELECT id, COUNT(*) AS so_lan
FROM `item_template`
GROUP BY id HAVING COUNT(*) > 1;

-- 4.4 Đếm đúng 32 vật phẩm mới và kiểm tra quy ước.
--     KỲ VỌNG: so_item_moi = 32, so_dong_sai = 0
--     (description NULL sẽ làm ItemData.writeUTF ném NPE -> client TREO ở màn
--      hình tải dữ liệu; gold/gem > 0 tạo nguồn in vàng; power_require > 0
--      chặn nút "Dùng" -> kẹt nhiệm vụ; gender <> 3 kẹt 2 hành tinh)
SELECT COUNT(*) AS so_item_moi,
       SUM(description IS NULL
           OR CHAR_LENGTH(description) > 75
           OR gender <> 3
           OR power_require <> 0
           OR gold <> 0
           OR gem <> 0
           OR head <> -1 OR body <> -1 OR leg <> -1
           OR `TYPE` NOT IN (8, 27, 36)) AS so_dong_sai
FROM `item_template`
WHERE id BETWEEN 2000 AND 2031;

-- 4.5 Hai danh hiệu phải khớp cả 3 bảng.
--     KỲ VỌNG: đúng 2 dòng; part = idEffect; so_dong_shop = 2 ở mỗi dòng.
SELECT it.id     AS item_id,
       it.`NAME` AS ten_item,
       it.part   AS part_phai_bang_idEffect,
       db.idEffect,
       (SELECT COUNT(*) FROM `item_shop` s WHERE s.temp_id = it.id) AS so_dong_shop
FROM `item_template` it
JOIN `data_badges` db ON db.idItem = it.id
WHERE it.id IN (2030, 2031);

-- =====================================================================
-- (5) GỠ BỎ — chỉ dùng khi cần lùi lại, và CHỈ KHI chưa ai nhận item mới.
--     Xóa item mà người chơi đang giữ trong items_bag/items_box sẽ làm
--     getTemplate() ném IndexOutOfBoundsException lúc nạp nhân vật.
-- =====================================================================
-- START TRANSACTION;
-- DELETE FROM `item_shop`     WHERE id BETWEEN 999 AND 1002;
-- DELETE FROM `data_badges`   WHERE id IN (19, 20);
-- DELETE FROM `item_template` WHERE id BETWEEN 2000 AND 2031;
-- COMMIT;
-- Sau đó trả DataGame.vsItem về 9 và build lại 20.jar.
