-- =====================================================================
-- xem-du-lieu-de-tu.sql — XEM SỐ Ô TRANG BỊ / Ô CHIÊU THẬT CỦA ĐỆ TỬ
-- Chạy:  mysql -u root -p team2026 < xem-du-lieu-de-tu.sql
-- Chỉ ĐỌC, không sửa gì.
--
-- Cột `player`.`pet` là JSON lồng hai lớp nên phải UNQUOTE rồi mới EXTRACT.
--
-- ĐỐI CHIẾU với con số mà code đang dùng:
--   typePet 0 (đệ thường) : 7 ô trang bị · 4 ô chiêu
--   typePet 1 (Ma Bư)     : 7 ô trang bị · 4 ô chiêu
--   typePet 2 (Uub)       : 9 ô trang bị · 5 ô chiêu
--   typePet 3 (Kid Beer)  : 9 ô trang bị · 5 ô chiêu
--   typePet 4 (Kid Jiren) : 9 ô trang bị · 5 ô chiêu
-- =====================================================================
SELECT  p.name                                                                   AS nhan_vat,
        JSON_EXTRACT(JSON_UNQUOTE(JSON_EXTRACT(p.pet, '$[0]')), '$[0]')          AS typePet,
        JSON_UNQUOTE(JSON_EXTRACT(JSON_UNQUOTE(JSON_EXTRACT(p.pet, '$[0]')), '$[2]')) AS ten_de,
        JSON_LENGTH(JSON_UNQUOTE(JSON_EXTRACT(p.pet, '$[2]')))                   AS so_o_trang_bi,
        JSON_LENGTH(JSON_UNQUOTE(JSON_EXTRACT(p.pet, '$[3]')))                   AS so_o_chieu
FROM    player p
WHERE   p.pet IS NOT NULL AND p.pet <> '[]'
ORDER BY typePet DESC, p.name
LIMIT 40;
