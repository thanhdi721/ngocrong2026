-- =====================================================================
-- LÙI NGÀY GIA NHẬP BANG HỘI (dùng khi bị chặn "Gia nhập bang hội trên 1 ngày")
-- =====================================================================
-- Ngày gia nhập KHÔNG nằm ở cột riêng: nó nằm trong khoá "join_time" của cột
-- `clan`.`members` (một chuỗi JSON danh sách thành viên), tính bằng giây (unix).
-- Server nạp ở Manager (join_time) và so sánh trong ClanMember.getNumDateFromJoinTimeToToday().
--
-- !! PHẢI TẮT SERVER TRƯỚC KHI CHẠY. Server giữ bang hội trong bộ nhớ và ghi đè
--    cột `members` khi lưu, chạy lúc server đang bật là mất tác dụng.
--
-- Trình tự: tắt server -> chạy file này -> bật server.
-- =====================================================================

-- (1) XEM TRƯỚC: bang của nhân vật và dữ liệu thành viên hiện tại
SELECT c.`id`, c.`NAME`, c.`members`
  FROM `clan` c JOIN `player` p ON p.clan_id = c.id
 WHERE p.`name` = 'vvvvvv';

-- (2) LÙI ngày gia nhập của MỌI thành viên trong bang đó về 30 ngày trước
--     (đổi 'vvvvvv' thành tên nhân vật của bạn; đổi 30 thành số ngày muốn lùi)
UPDATE `clan`
   SET `members` = REGEXP_REPLACE(`members`,
                    '"join_time"[[:space:]]*:[[:space:]]*[0-9]+',
                    CONCAT('"join_time":', UNIX_TIMESTAMP(NOW() - INTERVAL 30 DAY)))
 WHERE `id` = (SELECT `clan_id` FROM `player` WHERE `name` = 'vvvvvv');

-- (2b) HOẶC lùi cho TẤT CẢ các bang trên server (bỏ chú thích nếu muốn)
-- UPDATE `clan`
--    SET `members` = REGEXP_REPLACE(`members`,
--                     '"join_time"[[:space:]]*:[[:space:]]*[0-9]+',
--                     CONCAT('"join_time":', UNIX_TIMESTAMP(NOW() - INTERVAL 30 DAY)));

-- (3) KIỂM TRA: join_time mới phải nhỏ hơn thời điểm hiện tại ít nhất 1 ngày
SELECT c.`id`, c.`NAME`, c.`members`
  FROM `clan` c JOIN `player` p ON p.clan_id = c.id
 WHERE p.`name` = 'vvvvvv';
