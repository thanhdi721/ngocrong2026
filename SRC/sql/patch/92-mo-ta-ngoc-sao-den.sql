-- =====================================================================
-- 92-mo-ta-ngoc-sao-den.sql — MÔ TẢ 7 VIÊN NGỌC SAO ĐEN GHI SAI HOÀN TOÀN
-- Database: team2026 (MariaDB 10.4)        Sinh ngày: 2026-09-28
--
-- ---------------------------------------------------------------------
-- VẤN ĐỀ
-- ---------------------------------------------------------------------
--   Mô tả vật phẩm 372-378 hứa một đằng, code làm một nẻo:
--
--     viên    mô tả CŨ trong CSDL                        code THẬT SỰ cộng
--     1 sao   +15% sức đánh cho toàn bang                +21% sát thương
--     2 sao   +20% HP và KI tối đa cho toàn bang         +35% HP (không có KI)
--     3 sao   Mỗi giờ 10 hạt đậu thần cấp 8              +35 hút HP
--     4 sao   Mỗi giờ 1 bùa 1h ngẫu nhiên                +35 phản sát thương
--     5 sao   Mỗi giờ 3 ngọc nâng cấp ngẫu nhiên         +35 sát thương chí mạng
--     6 sao   Mỗi giờ 200.000 vàng                       +40% KI tối đa
--     7 sao   Mỗi giờ 2 ngọc                             +14 né đòn
--
--   Từ viên 3 sao trở đi mô tả hứa PHÁT ĐỒ THEO GIỜ (đậu thần, bùa, ngọc nâng cấp,
--   vàng, ngọc) — trong toàn bộ mã nguồn KHÔNG có một dòng nào làm việc đó. Cái
--   thật sự chạy là buff chỉ số bị động, tự cộng cho người thắng VÀ CẢ BANG ngay
--   lúc thắng trận, sống 22 giờ (RewardBlackBall.TIME_REWARD = 79.200.000 ms).
--
-- ---------------------------------------------------------------------
-- CÁCH SỬA
-- ---------------------------------------------------------------------
--   Chốt theo mức chủ dự án chọn, sửa cả hai phía cho khớp nhau:
--
--     viên    mức chốt                    chỗ cộng trong code
--     1 sao   +15% sát thương             NPoint.getDameAttack (dame)
--     2 sao   +30% HP tối đa              NPoint (hpMax)
--     3 sao   +35  hút HP                 NPoint.setPointWhenWearClothes (tlHutHp)
--     4 sao   +35  phản sát thương        NPoint.setPointWhenWearClothes (tlPST)
--     5 sao   +20  sát thương chí mạng    NPoint.setPointWhenWearClothes (tlDameCrit + tlSDCM)
--     6 sao   +30% KI tối đa              NPoint (mpMax)
--     7 sao   +14  né đòn                 NPoint.setPointWhenWearClothes (tlNeDon)
--
--   Bản jar đi kèm đã sửa số trong `RewardBlackBall.java` đúng bảng trên. CHẠY PATCH
--   NÀY MÀ KHÔNG THAY JAR thì mô tả lại lệch tiếp (jar cũ vẫn cộng 21/35/35/35/35/40/14).
--
--   Cột `description` là VARCHAR(75) nên câu chữ phải gọn, mấy dòng dưới đều vừa.
--
--   PHẢI TĂNG `vsItem`? CÓ. Client nhớ bảng vật phẩm theo `vsItem`, không tăng thì
--   người chơi cũ vẫn đọc mô tả cũ. Bản jar đi kèm đã tăng 33 -> 34.
--
-- ---------------------------------------------------------------------
-- TRÌNH TỰ CHẠY
-- ---------------------------------------------------------------------
--   1) TẮT SERVER.
--   2) CHỌN ĐÚNG DATABASE `team2026`. Chắc ăn nhất là dòng lệnh:
--        mysql -u root -p team2026 < 92-mo-ta-ngoc-sao-den.sql
--   3) Chạy cả file. Chạy lại nhiều lần vẫn an toàn.
--   4) Xem khối (3): cột `dat` phải = 1.
--   5) Bật server bản jar mới (vsItem = 34).
-- =====================================================================

-- ---------------------------------------------------------------------
-- (0) CHẶN CHẠY NHẦM DATABASE
-- ---------------------------------------------------------------------
SELECT DATABASE() AS `database_dang_chon_phai_la_team2026`;
SET @co_bang := (SELECT COUNT(*) FROM `information_schema`.`tables`
                  WHERE `table_schema` = DATABASE() AND `table_name` = 'item_template');
SET @sql := IF(@co_bang = 0,
    'SELECT ''DUNG LAI: database dang chon KHONG co bang `item_template`. '
    'Hay bam vao team2026 o khung ben trai phpMyAdmin roi chay lai file nay. '
    'Cac lenh ben duoi se BAO LOI va KHONG ghi gi ca.'' AS `loi`',
    'SELECT ''database dung roi, chay tiep'' AS `ghi_chu`');
PREPARE st FROM @sql; EXECUTE st; DEALLOCATE PREPARE st;

-- ---------------------------------------------------------------------
-- (1) KIỂM TRA TRƯỚC — nhìn mô tả cũ.
-- ---------------------------------------------------------------------
SELECT `id`, `NAME`, `description` FROM `item_template`
 WHERE `id` BETWEEN 372 AND 378 ORDER BY `id`;

-- ---------------------------------------------------------------------
-- (2) SỬA.
-- ---------------------------------------------------------------------
UPDATE `item_template` SET `description` = '+15% sát thương cho toàn bang trong 22 giờ'          WHERE `id` = 372;
UPDATE `item_template` SET `description` = '+30% HP tối đa cho toàn bang trong 22 giờ'           WHERE `id` = 373;
UPDATE `item_template` SET `description` = '+35 hút HP cho toàn bang trong 22 giờ'               WHERE `id` = 374;
UPDATE `item_template` SET `description` = '+35 phản sát thương cho toàn bang trong 22 giờ'      WHERE `id` = 375;
UPDATE `item_template` SET `description` = '+20 sát thương chí mạng cho toàn bang trong 22 giờ'  WHERE `id` = 376;
UPDATE `item_template` SET `description` = '+30% KI tối đa cho toàn bang trong 22 giờ'           WHERE `id` = 377;
UPDATE `item_template` SET `description` = '+14 né đòn cho toàn bang trong 22 giờ'               WHERE `id` = 378;

-- ---------------------------------------------------------------------
-- (3) KIỂM TRA SAU — cột `dat` phải = 1.
-- ---------------------------------------------------------------------
SELECT 'ca 7 vien da doi mo ta' AS `muc`,
       ((SELECT COUNT(*) FROM `item_template`
          WHERE `id` BETWEEN 372 AND 378
            AND `description` LIKE '%22 giờ%') = 7) AS `dat`
UNION ALL
SELECT 'khong con cau hua phat do theo gio',
       ((SELECT COUNT(*) FROM `item_template`
          WHERE `id` BETWEEN 372 AND 378
            AND `description` LIKE 'Mỗi giờ%') = 0);

SELECT `id`, `NAME`, `description` FROM `item_template`
 WHERE `id` BETWEEN 372 AND 378 ORDER BY `id`;
