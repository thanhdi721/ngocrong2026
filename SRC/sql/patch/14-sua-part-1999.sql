-- =====================================================================
-- 14 — SỬA DÒNG `part` 1999 LÀM HỎNG HÌNH MỌI PART TỪ 2000 TRỞ ĐI (2026-09-19)
-- =====================================================================
-- Client đọc file part với số mảnh CỐ ĐỊNH theo loại: đầu (type 0) = 3,
-- thân (type 1) = 17, chân (type 2) = 14 — không có byte đếm.
-- Part 1999 (type 0, dùng cho NPC 82 Rương Sưu Tầm) chỉ có 2 mảnh => client
-- đọc lệch toàn bộ part 2000–2098: Granola (part 2018–2020) chỉ còn cái bóng,
-- các cải trang / NPC dùng part >= 2000 cũng hỏng hình.
-- Đã đối chiếu dump `database team2026.sql`: 2099 dòng, đúng 88.121 byte như
-- file data/update_data/part, và chỉ có DUY NHẤT dòng 1999 sai.
--
-- Code đi kèm: Manager.writePartData tự đệm / cắt đúng số mảnh khi ghi file
-- (dòng sai về sau không làm hỏng cả file nữa) + đọc part theo `order by id`;
-- DataGame.vsData 9 -> 10 để client tải lại part.
-- Chạy lại nhiều lần vẫn an toàn. Cần khởi động lại server.
-- =====================================================================

UPDATE `part`
   SET `DATA` = '[[15288,0,10],[2955,0,0],[2955,0,0]]'
 WHERE `id` = 1999 AND `TYPE` = 0;

-- KIỂM TRA
SELECT `id`, `TYPE`, `DATA` FROM `part` WHERE `id` = 1999;
