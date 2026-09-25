-- =====================================================================
-- 53 — SỬA 6 LINH THÚ KHÔNG ĐEO ĐƯỢC (2026-09-23)
-- =====================================================================
-- Patch 51 để part / head / body / leg = -1, client không cho đeo vào ô 7.
-- Các linh thú đang chạy tốt (Bé Rồng Cute 1765, 1766, 1771) đều là LOẠI 27 và
-- CÓ part + head/body/leg trỏ vào 3 part của chính nó. Nay làm y hệt.
--
-- Không cần head_avatar: các linh thú cũ cũng không có.
-- Jar mới xử lý CHUNG mọi linh thú loại 27 có sẵn 3 part (UseItem + Player.sendNewPet),
-- nên sau này thêm linh thú chỉ cần thêm dòng trong item_template, không phải sửa code.
-- Chạy lại vẫn an toàn.
-- Patch 49 và 51 đã sửa theo -> chạy thứ tự nào cũng ra kết quả này.
-- =====================================================================

UPDATE `item_template` SET `TYPE` = 27, `description` = 'Linh thú đi theo', `part` = 2340, `head` = 2340, `body` = 2341, `leg` = 2342 WHERE `id` = 2114;
UPDATE `item_template` SET `TYPE` = 27, `description` = 'Linh thú đi theo', `part` = 2343, `head` = 2343, `body` = 2344, `leg` = 2345 WHERE `id` = 2115;
UPDATE `item_template` SET `TYPE` = 27, `description` = 'Linh thú đi theo', `part` = 2346, `head` = 2346, `body` = 2347, `leg` = 2348 WHERE `id` = 2116;
UPDATE `item_template` SET `TYPE` = 27, `description` = 'Linh thú đi theo', `part` = 2349, `head` = 2349, `body` = 2350, `leg` = 2351 WHERE `id` = 2117;
UPDATE `item_template` SET `TYPE` = 27, `description` = 'Linh thú đi theo', `part` = 2352, `head` = 2352, `body` = 2353, `leg` = 2354 WHERE `id` = 2118;
UPDATE `item_template` SET `TYPE` = 27, `description` = 'Linh thú đi theo', `part` = 2355, `head` = 2355, `body` = 2356, `leg` = 2357 WHERE `id` = 2119;

-- Nhân tiện sửa dữ liệu sai sẵn có: "Pet Thỏ mập" (1040) ghi chân = 1093, mà 1093 là part
-- THÂN; chân đúng là 1094 (part loại 2). Jar mới lấy part từ bảng nên phải sửa cho khớp.
UPDATE `item_template` SET `leg` = 1094 WHERE `id` = 1040 AND `leg` = 1093;

SELECT `id`, `NAME`, `TYPE`, `part`, `head`, `body`, `leg` FROM `item_template` WHERE `id` BETWEEN 2114 AND 2119 OR `id` = 1040;
