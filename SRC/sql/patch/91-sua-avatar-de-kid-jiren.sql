-- =====================================================================
-- 91-sua-avatar-de-kid-jiren.sql — BẤM "THÔNG TIN" ĐỆ KID JIREN LÀ LỖI CLIENT
-- Database: team2026 (MariaDB 10.4)        Sinh ngày: 2026-09-28
--
-- ---------------------------------------------------------------------
-- TRIỆU CHỨNG
-- ---------------------------------------------------------------------
--   Đệ Kid Jiren: bấm vào bảng thông tin đệ -> client văng / trắng bảng.
--   Đệ thường, Ma Bư, Uub, Kid Beer thì bình thường.
--   Máy chủ KHÔNG ghi log gì cả -> lỗi nằm hoàn toàn ở phía client khi VẼ.
--
-- ---------------------------------------------------------------------
-- TRUY RA NGUYÊN NHÂN
-- ---------------------------------------------------------------------
--   Đã dò từng nhánh code rẽ theo `typePet`. Kid Beer (typePet 3) và Kid Jiren
--   (typePet 4) giống nhau HOÀN TOÀN:
--     * PetService.createNewPet : cùng 400.000 hp/mp, 20.000 dame, def 9-50,
--       crit 0-2, sức mạnh 40 tỷ, 9 ô trang bị, 5 ô chiêu;
--     * MrBlue.loadPlayer       : cùng requiredSize 9 và maxSkillCount 5;
--     * NPoint                  : cùng +20% hp/mp/dame khi hợp thể Porata;
--     * Service.showInfoPet     : gói tin -107 ghi ĐÚNG CÙNG SỐ TRƯỜNG, cùng kiểu.
--
--   => Trong cả gói tin thông tin đệ, CHỈ CÓ ĐÚNG MỘT GIÁ TRỊ KHÁC NHAU:
--        Service.java:1640   msg.writer().writeShort(pl.pet.getAvatar());
--        Kid Beer  -> 1422        Kid Jiren -> 876
--
--   Tra bảng `head_avatar`:
--        head 297  (Ma Bư)   -> avatar 4674    OK
--        head 946  (Uub)     -> avatar 11656   OK
--        head 1422 (Kid Beer)-> KHÔNG CÓ DÒNG NÀO  => client bỏ qua nhánh avatar
--        head 876  (Jiren)   -> avatar 8094    <== chỉ Jiren mới đi vào nhánh này
--
--   Đo lại file ảnh trong `SRC/data/icon`. Mọi ảnh đúng chuẩn phải là x1 nhân
--   2 / 3 / 4. Icon 8094 thì KHÔNG:
--
--        icon    x1        x2         x3         x4          đúng chuẩn?
--        4674    86x56     172x112    258x168    344x224     đúng (Ma Bư)
--        11656   22x18     44x36      66x54      88x72       đúng (Uub)
--        8094    51x44     306x264    459x396    612x528     SAI — to gấp 3!
--                          (phải là   102x88 / 153x132 / 204x176)
--
--   Cả 31 icon của part Jiren (8063-8093) lẫn 31 icon part Kid Beer
--   (12793-12823) đều đúng chuẩn — nên đệ Jiren chạy nhảy ngoài map vẫn bình
--   thường, chỉ vỡ đúng ở bảng thông tin, đúng chỗ duy nhất dùng tới avatar.
--
--   File x4 còn nặng 99.482 byte, gấp gần 7 lần ảnh avatar bình thường.
--
-- ---------------------------------------------------------------------
-- CÁCH SỬA
-- ---------------------------------------------------------------------
--   Đã dựng icon MỚI id 32704 trong `SRC/data/icon/x1..x4`, thu nhỏ đúng 3 lần
--   từ chính ảnh cũ nên nét vẫn giữ nguyên:
--        x1 51x44 · x2 102x88 · x3 153x132 · x4 204x176
--
--   Vì sao phải ĐỔI SỐ ID chứ không ghi đè lên 8094: client lưu ảnh theo id vào
--   bộ nhớ đệm. Ghi đè thì máy đã từng mở Jiren vẫn dùng ảnh hỏng trong cache.
--   (File 8094 cũ vẫn để nguyên, không xoá, phòng khi còn chỗ nào khác dùng tới.)
--
--   PHẢI BUILD LẠI JAR? KHÔNG. Bảng `head_avatar` đi trong gói thông tin nhân vật
--   lúc đăng nhập (Service.java:974), không nằm trong gói cache `vsData`, nên
--   chỉ cần khởi động lại server rồi đăng nhập lại là ăn ngay.
--
-- ---------------------------------------------------------------------
-- TRÌNH TỰ CHẠY
-- ---------------------------------------------------------------------
--   1) TẮT SERVER.
--   2) Chép đè cả thư mục `SRC/data/icon` (đã có 4 file 32704.png mới).
--   3) CHỌN ĐÚNG DATABASE `team2026`. Chắc ăn nhất là dòng lệnh:
--        mysql -u root -p team2026 < 91-sua-avatar-de-kid-jiren.sql
--   4) Chạy cả file. Chạy lại nhiều lần vẫn an toàn.
--   5) Xem khối (3): cột `dat` phải = 1.
--   6) Bật server, ĐĂNG NHẬP LẠI, bấm thông tin đệ Jiren.
-- =====================================================================

-- ---------------------------------------------------------------------
-- (0) CHẶN CHẠY NHẦM DATABASE
-- ---------------------------------------------------------------------
SELECT DATABASE() AS `database_dang_chon_phai_la_team2026`;
SET @co_bang := (SELECT COUNT(*) FROM `information_schema`.`tables`
                  WHERE `table_schema` = DATABASE() AND `table_name` = 'head_avatar');
SET @sql := IF(@co_bang = 0,
    'SELECT ''DUNG LAI: database dang chon KHONG co bang `head_avatar`. '
    'Hay bam vao team2026 o khung ben trai phpMyAdmin roi chay lai file nay. '
    'Cac lenh ben duoi se BAO LOI va KHONG ghi gi ca.'' AS `loi`',
    'SELECT ''database dung roi, chay tiep'' AS `ghi_chu`');
PREPARE st FROM @sql; EXECUTE st; DEALLOCATE PREPARE st;

-- ---------------------------------------------------------------------
-- (1) KIỂM TRA TRƯỚC — nhìn avatar của 4 loại đệ.
--     head 1422 (Kid Beer) không có dòng là ĐÚNG, không cần thêm.
-- ---------------------------------------------------------------------
SELECT `head_id`, `avatar_id`,
       CASE `head_id` WHEN 297 THEN 'De Ma Bu'
                      WHEN 946 THEN 'De Uub'
                      WHEN 876 THEN 'De Kid Jiren'
                      ELSE '' END AS `ghi_chu`
  FROM `head_avatar` WHERE `head_id` IN (297, 946, 876, 1422) ORDER BY `head_id`;

-- ---------------------------------------------------------------------
-- (2) SỬA — trỏ avatar đệ Jiren sang ảnh mới đúng kích thước.
-- ---------------------------------------------------------------------
UPDATE `head_avatar` SET `avatar_id` = 32704 WHERE `head_id` = 876;

-- ---------------------------------------------------------------------
-- (3) KIỂM TRA SAU — cột `dat` phải = 1.
-- ---------------------------------------------------------------------
SELECT 'avatar de Jiren da tro sang 32704' AS `muc`,
       ((SELECT COUNT(*) FROM `head_avatar`
          WHERE `head_id` = 876 AND `avatar_id` = 32704) = 1) AS `dat`
UNION ALL
SELECT 'khong con dong nao tro ve anh hong 8094',
       ((SELECT COUNT(*) FROM `head_avatar` WHERE `avatar_id` = 8094) = 0);

SELECT `head_id`, `avatar_id` FROM `head_avatar`
 WHERE `head_id` IN (297, 946, 876, 1422) ORDER BY `head_id`;

-- ---------------------------------------------------------------------
-- (4) NẾU CHẠY XONG VẪN LỖI — phép thử dứt điểm, chỉ một dòng.
-- ---------------------------------------------------------------------
--   Xoá hẳn dòng avatar của head 876. Lúc đó Jiren đi ĐÚNG nhánh của Kid Beer
--   (không có avatar riêng). Hết lỗi => chắc chắn thủ phạm là đường avatar.
--   Vẫn lỗi => thủ phạm nằm chỗ khác, báo lại để đào tiếp.
--
--   Lưu ý: head 876 cũng là đầu của vật phẩm 1808 "Kid jiren", nên xoá dòng này
--   thì ảnh đại diện của cải trang đó cũng mất theo. Chỉ dùng để THỬ.
--
-- DELETE FROM `head_avatar` WHERE `head_id` = 876;
--
--   Trả lại như cũ:
-- INSERT INTO `head_avatar` (`head_id`, `avatar_id`) VALUES (876, 32704);
