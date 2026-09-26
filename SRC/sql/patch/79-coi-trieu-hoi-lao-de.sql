-- =====================================================================
-- 79-coi-trieu-hoi-lao-de.sql — CÒI TRIỆU HỒI LÃO DÊ (vật phẩm 2265)
-- Database: team2026 (MariaDB 10.4)        Sinh ngày: 2026-09-26
--
-- Code đi kèm:
--   boss/BossID.LAO_DE_HOI_XUAN, boss/BossesData.LAO_DE_HOI_XUAN,
--   boss/lao_de/BossLaoDe, boss/sieu_than_god/BangRoi (bảng rơi dùng chung),
--   boss/sieu_than_god/BossSieuThanGod (chuyển sang dùng BangRoi),
--   boss/Boss_Manager/BossManager.loadBoss, services_func/UseItem (case 2265),
--   data/DataGame (vsItem 27 -> 28).
--
-- Phải chạy TRƯỚC: patch 75 (Đá Pháp Sư 2262), patch 78 (Gậy Thông Thiên 2264).
--
-- ---------------------------------------------------------------------
-- FILE NÀY LÀM GÌ
-- ---------------------------------------------------------------------
--  Thêm đúng MỘT dòng `item_template`: 2265 "Còi Triệu Hồi Lão Dê".
--  icon 12316 — dùng lại ảnh cái chuông vàng đã có sẵn trong data/icon,
--  KHÔNG cần thêm file ảnh nào. is_up_to_up = 1 để gộp chồng trong hành trang.
--
--  Thổi còi ở map thường -> boss "Lão Dê Hồi Xuân" hiện ra ngay cạnh người thổi.
--  Hạ lão rơi đồ theo ĐÚNG bảng của hai boss Siêu Thần God (BangRoi).
--  Thổi hụt (map cấm, hết lượt) thì KHÔNG mất còi.
--
--  Boss không cần dòng SQL nào: BossesData khai trong code, ngoại hình dùng lại
--  part của cải trang "Quy Lão Hồi Xuân" (item 2142, part 2424/2425/2426, đã có
--  từ patch 67) và hào quang 109 Hồng Vân (patch 74).
--
-- VÌ SAO LÀ ITEM 2265:
--  ITEM_TEMPLATES là ArrayList tra theo chỉ số => id phải liên tục. Id lớn nhất
--  hiện nay là 2264 (patch 78), nên 2265 là ô kế tiếp.
--
-- NGÂN SÁCH GÓI TIN: sau patch này bảng vật phẩm nặng ~63,1 KB mỗi gói trên
-- trần 65,0 KB. Còn chỗ cho khoảng 40 vật phẩm nữa thôi.
--
-- ---------------------------------------------------------------------
-- TRÌNH TỰ CHẠY
-- ---------------------------------------------------------------------
--   1) TẮT SERVER (item_template chỉ đọc lúc khởi động).
--   2) mysqldump -u root -p team2026 item_template > backup_79.sql
--   3) Chạy cả file. Chạy lại nhiều lần vẫn an toàn.
--   4) Xem khối (3): mọi cột `dat` phải = 1.
--   5) Bật server bản code mới (vsItem = 28) -> client tự tải lại bảng vật phẩm.
-- =====================================================================

SET NAMES utf8mb4;

-- ---------------------------------------------------------------------
-- (1) KIỂM TRA TRƯỚC
-- ---------------------------------------------------------------------
-- Phải là 2264 (patch 78 đã chạy) thì mới thêm 2265 được.
SELECT MAX(`id`) AS `item_id_lon_nhat_phai_la_2264` FROM `item_template`;

-- Cải trang gốc của Lão Dê phải có sẵn (patch 67).
SELECT `id`, `NAME`, `head`, `body`, `leg` FROM `item_template` WHERE `id` = 2142;

-- Hào quang 109 phải có dòng img_by_name (patch 74).
SELECT `NAME`, `n_frame` FROM `img_by_name` WHERE `NAME` LIKE 'aura_109%';

-- ---------------------------------------------------------------------
-- (2) CẬP NHẬT
-- ---------------------------------------------------------------------
DELETE FROM `item_template` WHERE `id` = 2265;
INSERT INTO `item_template` (`id`, `TYPE`, `gender`, `NAME`, `description`, `level`, `icon_id`,
                             `part`, `is_up_to_up`, `power_require`, `gold`, `gem`, `head`, `body`, `leg`) VALUES
(2265, 27, 3, 'Còi Triệu Hồi Lão Dê', 'Thổi lên là cụ tới. Cụ không vui đâu', 0, 12316, -1, 1, 0, 0, 0, -1, -1, -1);

-- ---------------------------------------------------------------------
-- (3) KIỂM TRA SAU — mọi cột `dat` phải = 1.
-- ---------------------------------------------------------------------
SELECT 'item 2265 Coi Trieu Hoi Lao De' AS `muc`,
       (SELECT COUNT(*) FROM `item_template`
         WHERE `id` = 2265 AND `NAME` = 'Còi Triệu Hồi Lão Dê'
           AND `icon_id` = 12316 AND `is_up_to_up` = 1) AS `dat`
UNION ALL
SELECT 'item_template id lien tuc toi 2265',
       ((SELECT COUNT(*) FROM `item_template` WHERE `id` BETWEEN 0 AND 2265) = 2266)
UNION ALL
SELECT 'cai trang Quy Lao Hoi Xuan con nguyen',
       (SELECT COUNT(*) FROM `item_template`
         WHERE `id` = 2142 AND `head` = 2424 AND `body` = 2425 AND `leg` = 2426)
UNION ALL
SELECT 'du 3 part cua Lao De',
       ((SELECT COUNT(*) FROM `part` WHERE `id` IN (2424, 2425, 2426)) = 3);

-- ---------------------------------------------------------------------
-- (4) LÙI LẠI  (bỏ comment rồi chạy; TẮT SERVER trước)
-- ---------------------------------------------------------------------
-- DELETE FROM `item_template` WHERE `id` = 2265;
