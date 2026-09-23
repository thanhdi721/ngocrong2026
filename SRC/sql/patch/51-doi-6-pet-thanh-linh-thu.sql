-- =====================================================================
-- 51 — 6 MÓN MANG TỪ HUNR LÀ LINH THÚ, KHÔNG PHẢI CẢI TRANG (2026-09-23)
-- =====================================================================
-- Bản đầu của patch 49 để chúng là cải trang (loại 5) nên mặc vào thì NHÂN VẬT
-- biến thành con thú. Thực tế bên HUNR chúng là pet đi theo.
--
-- Bên mình linh thú là vật phẩm LOẠI 27, đeo ở ô số 7; part khai trong jar
-- (Player.sendNewPet), cột head/body/leg để -1. Jar mới đã thêm 6 dòng đó và
-- sửa luôn lỗi phải thoát ra vào lại mới thấy linh thú.
--
-- 2114 Pet Lôi Thần   part 2340/2341/2342      2117 Cá cam         part 2349/2350/2351
-- 2115 King Kong      part 2343/2344/2345      2118 Pet Pikachu    part 2352/2353/2354
-- 2116 Cá xanh        part 2346/2347/2348      2119 Lân Linh Lung  part 2355/2356/2357
--
-- Patch 49 cũng đã sửa theo. Chạy cùng jar có DataGame.vsItem = 18.
-- Chạy lại nhiều lần vẫn an toàn. Khởi động lại server.
-- =====================================================================

UPDATE `item_template`
   SET `TYPE` = 27, `description` = 'Linh thú đi theo', `part` = -1,
       `head` = -1, `body` = -1, `leg` = -1
 WHERE `id` BETWEEN 2114 AND 2119;

DELETE FROM `head_avatar` WHERE `head_id` IN (2340, 2343, 2346, 2349, 2352, 2355);

SELECT `id`, `NAME`, `TYPE`, `icon_id`, `head`, `body`, `leg` FROM `item_template` WHERE `id` BETWEEN 2114 AND 2119;
