-- =====================================================================
-- 66 — Hào quang 98: bản 75% của bản gốc (2026-09-25)
-- =====================================================================
-- Các cỡ đã thử: 95 gốc (khung x1 157x254) · 96 = 50% · 97 = 25% (nhỏ quá)
-- 98 = 75%: khung x1 118x190 · x2 236x381 · x3 353x572 · x4 471x762, 12 khung.
-- Ảnh đã chép sẵn vào SRC/data/img_by_name/x1..x4/aura_98_0.png (và _1 rỗng).
--
-- Mỗi lần đổi cỡ phải sang id mới chứ không ghi đè: client cache ảnh
-- img_by_name theo TÊN, máy nào tải rồi thì ghi đè bên server vẫn hiện ảnh cũ.
--
-- Đi kèm: npc_list/GokuNoiLoan.AURA_ID -> 98.
-- =====================================================================

SET NAMES utf8mb4;

INSERT INTO `img_by_name` (`NAME`, `n_frame`) VALUES ('aura_98_0', 12)
    ON DUPLICATE KEY UPDATE `n_frame` = VALUES(`n_frame`);
INSERT INTO `img_by_name` (`NAME`, `n_frame`) VALUES ('aura_98_1', 1)
    ON DUPLICATE KEY UPDATE `n_frame` = VALUES(`n_frame`);

-- Ai đang bật cỡ cũ thì chuyển sang cỡ đang dùng.
UPDATE `player` SET `aura_npc` = 98 WHERE `aura_npc` IN (95, 96, 97);

SELECT `NAME`, `n_frame` FROM `img_by_name` WHERE `NAME` LIKE 'aura_9%';
SELECT COUNT(*) AS `so_nguoi_dang_bat` FROM `player` WHERE `aura_npc` = 98;
