-- 62 — Aura 95 "Goku Purple"
-- Ảnh đã chép sẵn vào SRC/data/img_by_name/x1..x4 (aura_95_0.png, aura_95_1.png).
-- aura_95_0: 12 khung, mỗi khung 157x254 (x1) / 628x1016 (x4).
-- aura_95_1: ảnh 1x1 rỗng (lớp phụ không dùng), giống aura_94_0 và nhiều aura khác.

INSERT INTO img_by_name (NAME, n_frame) VALUES ('aura_95_0', 12)
    ON DUPLICATE KEY UPDATE n_frame = VALUES(n_frame);
INSERT INTO img_by_name (NAME, n_frame) VALUES ('aura_95_1', 1)
    ON DUPLICATE KEY UPDATE n_frame = VALUES(n_frame);

-- ==== CÁCH PHÁT AURA CHO NGƯỜI CHƠI (chọn 1 dòng rồi bỏ dấu -- ) ====
-- Hiện aura chỉ đến từ thẻ rađa, và code Player.getAura() chỉ đọc 6 thẻ:
--   956, 1204, 1791, 1792, 1793, 1142 — thẻ phải ở cấp > 1.
-- Muốn aura 95 chạy được thì gán vào MỘT trong các thẻ đó (thẻ đó mất aura cũ):
-- UPDATE radar SET aura_id = 95 WHERE id = 1793;  -- Thẻ Oozarun 2 (đang aura 4)
-- UPDATE radar SET aura_id = 95 WHERE id = 1791;  -- Thẻ Oozaru    (đang aura 2)
-- UPDATE radar SET aura_id = 95 WHERE id = 1204;  -- Thẻ Rồng Thần Namek (đang aura 1)
