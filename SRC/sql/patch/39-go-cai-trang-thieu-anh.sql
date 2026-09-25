-- =====================================================================
-- 39 — GỠ 11 CẢI TRANG THIẾU ẢNH KHỎI GAME (2026-09-21)
-- =====================================================================
-- Cải trang Gốc (1841–1850) và Broly Nes (1851) dùng các part mà ảnh gốc không
-- tồn tại ở bất kỳ source nào (đã tra NGOL, OnePuch). Không shop / boss / code
-- nào phát các món này. Chủ dự án chốt: gỡ khỏi game.
--
-- KHÔNG xoá dòng: client tra item_template và part THEO VỊ TRÍ, xoá một dòng là
-- mọi vật phẩm / part phía sau lệch id, hỏng toàn bộ hình. Thay vào đó:
--   1. Part của 11 món (chỉ những part KHÔNG món nào khác dùng) -> thay bằng mảnh
--      trong suốt 2955, giữ đúng số mảnh theo loại (đầu 3, thân 17, chân 14).
--   2. 11 món: đổi tên "(Đã gỡ)", bỏ head/body/leg (= -1). Ai lỡ đang giữ / mặc
--      thì chỉ hiện ngoại hình thường, không lỗi client.
--
-- Chạy SAU patch 38. Cùng jar có DataGame.vsItem = 13 và vsData = 13.
-- Chạy lại nhiều lần vẫn an toàn. Khởi động lại server.
-- =====================================================================

-- 1. Part (phải chạy TRƯỚC bước 2 vì tra head/body/leg của 11 món)
UPDATE `part` p
   SET p.`DATA` = CASE p.`TYPE`
       WHEN 0 THEN '[[2955,0,0],[2955,0,0],[2955,0,0]]'
       WHEN 1 THEN '[[2955,0,0],[2955,0,0],[2955,0,0],[2955,0,0],[2955,0,0],[2955,0,0],[2955,0,0],[2955,0,0],[2955,0,0],[2955,0,0],[2955,0,0],[2955,0,0],[2955,0,0],[2955,0,0],[2955,0,0],[2955,0,0],[2955,0,0]]'
       ELSE        '[[2955,0,0],[2955,0,0],[2955,0,0],[2955,0,0],[2955,0,0],[2955,0,0],[2955,0,0],[2955,0,0],[2955,0,0],[2955,0,0],[2955,0,0],[2955,0,0],[2955,0,0],[2955,0,0]]'
   END
 WHERE p.`id` IN (
         SELECT x.pid FROM (
           SELECT `head` AS pid FROM `item_template` WHERE `id` BETWEEN 1841 AND 1851 AND `head` >= 0
           UNION SELECT `body` FROM `item_template` WHERE `id` BETWEEN 1841 AND 1851 AND `body` >= 0
           UNION SELECT `leg`  FROM `item_template` WHERE `id` BETWEEN 1841 AND 1851 AND `leg`  >= 0
         ) x)
   AND p.`id` NOT IN (
         SELECT y.pid FROM (
           SELECT `head` AS pid FROM `item_template` WHERE `id` NOT BETWEEN 1841 AND 1851
           UNION SELECT `body` FROM `item_template` WHERE `id` NOT BETWEEN 1841 AND 1851
           UNION SELECT `leg`  FROM `item_template` WHERE `id` NOT BETWEEN 1841 AND 1851
           UNION SELECT `part` FROM `item_template` WHERE `id` NOT BETWEEN 1841 AND 1851
           UNION SELECT `head` FROM `npc_template`
           UNION SELECT `body` FROM `npc_template`
           UNION SELECT `leg`  FROM `npc_template`
         ) y);

-- 2. Vật phẩm
UPDATE `item_template`
   SET `NAME` = '(Đã gỡ)',
       `description` = 'Vật phẩm đã gỡ khỏi game',
       `head` = -1, `body` = -1, `leg` = -1
 WHERE `id` BETWEEN 1841 AND 1851;

-- 3. Gỡ khỏi shop (nếu DB live có bán)
DELETE FROM `item_shop` WHERE `temp_id` BETWEEN 1841 AND 1851;

-- KIỂM TRA: 11 món phải là "(Đã gỡ)" và head/body/leg = -1
SELECT `id`, `NAME`, `head`, `body`, `leg` FROM `item_template` WHERE `id` BETWEEN 1841 AND 1851;
