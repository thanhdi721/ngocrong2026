-- =====================================================================
-- 65 — Hào quang 97: bản thu nhỏ còn 25% của bản gốc (2026-09-25)
-- =====================================================================
-- 95 = bản gốc (khung x1 157x254, quá to), 96 = bản 50%, 97 = bản 25%:
--   khung x1  39x64 · x2  78x127 · x3 118x190 · x4 157x254, vẫn 12 khung.
-- Ảnh đã chép sẵn vào SRC/data/img_by_name/x1..x4/aura_97_0.png (và _1 rỗng).
--
-- Mỗi lần đổi cỡ phải sang id mới chứ không ghi đè: client cache ảnh
-- img_by_name theo TÊN, máy nào tải rồi thì ghi đè bên server vẫn hiện ảnh cũ.
--
-- Đi kèm: npc_list/GokuNoiLoan.AURA_ID -> 97, Player.autoSendBadges (đang bật
-- hào quang thì không hiện danh hiệu nữa; chỉ số danh hiệu vẫn giữ nguyên).
-- =====================================================================

SET NAMES utf8mb4;

INSERT INTO `img_by_name` (`NAME`, `n_frame`) VALUES ('aura_97_0', 12)
    ON DUPLICATE KEY UPDATE `n_frame` = VALUES(`n_frame`);
INSERT INTO `img_by_name` (`NAME`, `n_frame`) VALUES ('aura_97_1', 1)
    ON DUPLICATE KEY UPDATE `n_frame` = VALUES(`n_frame`);

-- Ai đang bật bản to / bản 50% thì chuyển sang bản 25%.
UPDATE `player` SET `aura_npc` = 97 WHERE `aura_npc` IN (95, 96);

SELECT `NAME`, `n_frame` FROM `img_by_name` WHERE `NAME` LIKE 'aura_9%';
SELECT COUNT(*) AS `so_nguoi_dang_bat` FROM `player` WHERE `aura_npc` = 97;
