-- =====================================================================
-- 32 — SỬA HÌNH BOSS HEART (CLIENT ĐƠ Ở KHU CÓ BOSS) — 2026-09-20
-- =====================================================================
-- Boss Heart (cả 4 hình dạng) và npc_template 108 dùng part 2109 / 2110 / 2111,
-- nhưng bảng `part` chỉ có tới id 2098 -> client không tìm thấy hình, đứng im,
-- không đánh được, boss không hiện. Server KHÔNG báo lỗi vì lỗi nằm ở client.
--
-- Đổi sang bộ part của "Cải trang Zamasu" (903 / 904 / 905) — có sẵn trong DB,
-- tạo hình phản diện áo choàng, hợp với Heart.
-- Jar mới đã đổi trong BossesData; patch này sửa nốt npc_template cho khớp.
--
-- Nếu sau này bạn có bộ hình riêng cho Heart thì thêm 3 dòng part mới rồi trỏ lại.
-- Chạy lại nhiều lần vẫn an toàn. Khởi động lại server.
-- =====================================================================

UPDATE `npc_template`
   SET `head` = 903, `body` = 904, `leg` = 905, `avatar` = 8211
 WHERE `id` = 108;

-- Bunma Rực Rỡ (110) cũng trỏ vào part không tồn tại (2123-2125) nhưng chưa đặt
-- trên map nào; sửa luôn để sau này dùng không bị đơ client.
UPDATE `npc_template`
   SET `head` = 409, `body` = 410, `leg` = 411, `avatar` = 4119
 WHERE `id` = 110 AND `head` = 2123;

-- KIỂM TRA: không còn npc_template nào trỏ vào part > id lớn nhất của bảng part
SELECT `id`, `NAME`, `head`, `body`, `leg` FROM `npc_template`
 WHERE `head` > (SELECT MAX(`id`) FROM `part`)
    OR `body` > (SELECT MAX(`id`) FROM `part`)
    OR `leg`  > (SELECT MAX(`id`) FROM `part`);
