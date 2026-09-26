-- =====================================================================
-- 85-linh-dien.sql — CỘT `player`.`tu_tien` CHO LINH ĐIỀN
-- Database: team2026 (MariaDB 10.4)        Sinh ngày: 2026-09-27
--
-- Bản thiết kế: docs/4-trien-khai/71-luyen-dan.md mục 9.
--
-- Code đi kèm:
--   tu_tien/LinhDien (6 ô ruộng, gieo bằng vàng, 4 giờ chín),
--   npc_list/TuTienNPC (mục "Linh điền"), consts/ConstNpc (LINH_DIEN_MENU),
--   player/Player (hai mảng 6 ô), server/Manager (tự thêm cột nếu quên chạy file này),
--   database/MrBlue (đọc cột), database/PlayerDAO (ghi cột).
--
-- Phải chạy TRƯỚC: patch 78 (đã đổi `data_card` sang TEXT) và patch 84.
--
-- ---------------------------------------------------------------------
-- FILE NÀY LÀM GÌ
-- ---------------------------------------------------------------------
--  Thêm đúng MỘT cột `player`.`tu_tien` TEXT NULL.
--
-- VÌ SAO CHỈ MỘT CỘT, VÀ VÌ SAO TÊN LÀ `tu_tien` CHỨ KHÔNG PHẢI `linh_dien`:
--  Bảng `player` đã sát trần 65.535 byte một dòng của InnoDB — patch 78 phải đổi
--  `data_card` từ VARCHAR(10000) sang TEXT mới có chỗ thêm `thong_dit`. Mỗi lần thêm
--  cột là một lần đánh cược với lỗi 1118 "Row size too large", nên cột này cố ý là
--  cột GOM: nội dung ghi dạng `khoa=giatri#khoa=giatri`, hiện chỉ có một khoá `ld`
--  (linh điền). Tính năng tu tiên sau này nhét thêm khoá vào đây, KHÔNG thêm cột nữa.
--
--  Ví dụ giá trị: ld=2276,1759000000000;0,0;2277,1759000500000;0,0;0,0;0,0
--                    │    └ lúc gieo (mili giây)      └ ô trống
--                    └ id linh thảo đang gieo
--
-- CHẠY FILE NÀY CÓ BẮT BUỘC KHÔNG:
--  Không bắt buộc. `Manager.ensureSchema` tự thêm cột lúc server khởi động nếu thiếu
--  (giống `vqtd`, `aura_npc`, `thong_dit`). Chạy tay ở đây để thấy lỗi NGAY nếu bảng
--  hết chỗ, thay vì phát hiện lúc server đang chạy và người chơi mất dữ liệu ruộng.
--
-- ---------------------------------------------------------------------
-- TRÌNH TỰ CHẠY
-- ---------------------------------------------------------------------
--   1) TẮT SERVER.
--   2) CHỌN ĐÚNG DATABASE `team2026` TRƯỚC KHI CHẠY. Trong phpMyAdmin: bấm vào tên
--      `team2026` ở khung bên trái, rồi mới mở thẻ SQL / Import. Đứng ở màn hình gốc
--      hay ở `information_schema` mà chạy là câu ALTER nhắm sai chỗ (xem khối (2)).
--      Dòng lệnh: mysql -u root -p team2026 < 85-linh-dien.sql
--   3) Chạy cả file. Chạy lại nhiều lần vẫn an toàn.
--   4) Xem khối (3): cả hai cột `dat` phải = 1.
-- =====================================================================

-- ---------------------------------------------------------------------
-- (1) KIỂM TRA TRƯỚC.
-- ---------------------------------------------------------------------
-- Dòng này PHẢI ra `team2026`. Ra `information_schema` hay NULL nghĩa là đang chọn
-- sai database — xem khối (2), file sẽ tự dừng chứ không làm bậy.
SELECT DATABASE() AS `database_dang_chon_phai_la_team2026`;

-- `data_card` PHẢI là `text`. Còn là `varchar` nghĩa là chưa chạy patch 78 —
-- thêm cột lúc đó sẽ văng lỗi 1118 "Row size too large".
SELECT `column_name`, `data_type`
  FROM `information_schema`.`columns`
 WHERE `table_schema` = DATABASE() AND `table_name` = 'player'
   AND `column_name` IN ('data_card', 'thong_dit', 'tu_tien');

-- ---------------------------------------------------------------------
-- (2) THÊM CỘT — bỏ qua nếu đã có, tự dừng nếu chọn sai database.
-- ---------------------------------------------------------------------
-- Viết kiểu PREPARE/EXECUTE (giống patch 58 / 63 / 78) để chạy được cả MariaDB lẫn
-- MySQL 8 — MySQL 8 không hiểu "ADD COLUMN IF NOT EXISTS".
--
-- CHẶN SAI DATABASE: mọi câu ở đây bám vào DATABASE(), tức database ĐANG CHỌN. Nếu
-- trong phpMyAdmin bạn đang đứng ở `information_schema` (hoặc bất kỳ database nào
-- khác) thì `@coCot` = 0, câu ALTER sẽ nhắm vào bảng `player` của database ĐÓ, và
-- MySQL trả về "#1044 Access denied ... to database 'information_schema'". Không hỏng
-- gì, nhưng cũng không thêm được cột. Vì vậy phải kiểm bảng `player` có tồn tại trong
-- database đang chọn hay không TRƯỚC, rồi mới quyết định chạy ALTER.
SET @coBang := (SELECT COUNT(*) FROM `information_schema`.`tables`
                 WHERE `table_schema` = DATABASE() AND `table_name` = 'player');
SET @coCot := (SELECT COUNT(*) FROM `information_schema`.`columns`
                WHERE `table_schema` = DATABASE() AND `table_name` = 'player'
                  AND `column_name` = 'tu_tien');
SET @sql := CASE
              WHEN @coBang = 0 THEN
                  'SELECT ''DUNG LAI: database dang chon KHONG co bang `player`. '
                  'Hay bam vao team2026 o khung ben trai phpMyAdmin roi chay lai file nay.'' AS `loi`'
              WHEN @coCot > 0 THEN
                  'SELECT ''cot tu_tien da co, khong lam gi'' AS `ghi_chu`'
              ELSE
                  'ALTER TABLE `player` ADD COLUMN `tu_tien` TEXT NULL'
            END;
PREPARE st FROM @sql;
EXECUTE st;
DEALLOCATE PREPARE st;

-- ---------------------------------------------------------------------
-- (3) KIỂM TRA SAU — mọi cột `dat` phải = 1.
-- ---------------------------------------------------------------------
SELECT 'cot player.tu_tien da co' AS `muc`,
       ((SELECT COUNT(*) FROM `information_schema`.`columns`
          WHERE `table_schema` = DATABASE() AND `table_name` = 'player'
            AND `column_name` = 'tu_tien' AND `data_type` = 'text') = 1) AS `dat`
UNION ALL
SELECT 'data_card van la TEXT (dieu kien cua patch 78)',
       ((SELECT COUNT(*) FROM `information_schema`.`columns`
          WHERE `table_schema` = DATABASE() AND `table_name` = 'player'
            AND `column_name` = 'data_card' AND `data_type` = 'text') = 1);

-- ---------------------------------------------------------------------
-- (4) GỠ BỎ — chỉ chạy khi muốn quay lại trước patch này.
-- ---------------------------------------------------------------------
-- ALTER TABLE `player` DROP COLUMN `tu_tien`;
