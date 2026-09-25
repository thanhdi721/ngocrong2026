-- =====================================================================
-- 64 — Hào quang 96: bản thu nhỏ 50% của hào quang 95 (2026-09-25)
-- =====================================================================
-- Bản 95 vào game to gấp mấy lần nhân vật. Ảnh 96 = 95 thu nhỏ còn một nửa,
-- vẫn 12 khung: khung x1 78x127 (trước là 157x254), x4 314x508.
-- Ảnh đã chép sẵn vào SRC/data/img_by_name/x1..x4/aura_96_0.png (và _1 rỗng).
--
-- Vì sao đổi id chứ không ghi đè 95: client cache ảnh img_by_name theo TÊN,
-- máy nào tải aura_95_0 rồi thì ghi đè file bên server vẫn hiện ảnh cũ.
-- File 95 cứ để đó, không ai dùng nữa cũng không sao.
--
-- Đi kèm: npc_list/GokuNoiLoan.AURA_ID 95 -> 96.
-- =====================================================================

SET NAMES utf8mb4;

INSERT INTO `img_by_name` (`NAME`, `n_frame`) VALUES ('aura_96_0', 12)
    ON DUPLICATE KEY UPDATE `n_frame` = VALUES(`n_frame`);
INSERT INTO `img_by_name` (`NAME`, `n_frame`) VALUES ('aura_96_1', 1)
    ON DUPLICATE KEY UPDATE `n_frame` = VALUES(`n_frame`);

-- Ai đang bật bản to thì chuyển sang bản nhỏ.
UPDATE `player` SET `aura_npc` = 96 WHERE `aura_npc` = 95;

SELECT `NAME`, `n_frame` FROM `img_by_name` WHERE `NAME` LIKE 'aura_9%';
SELECT COUNT(*) AS `so_nguoi_dang_bat` FROM `player` WHERE `aura_npc` = 96;
