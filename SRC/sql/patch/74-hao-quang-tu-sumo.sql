-- =====================================================================
-- 74 — 17 HÀO QUANG MANG TỪ SUMO / BUN (2026-09-25)
-- =====================================================================
-- Ảnh đã chép sẵn vào data/img_by_name/x1..x4 với id mới 99–115; mức phóng to nào
-- nguồn không có thì suy ra từ mức lớn nhất đang có (giữ chiều cao chia hết số khung).
--
-- Bên nguồn các hào quang này mang số trùng với hào quang của mình nhưng ẢNH KHÁC,
-- nên phải đánh số lại. Danh sách (id mới — tên trong menu NPC — số khung — nguồn):
--
--    99  Khí Xanh Dương   6 khung   (SUMO aura_10_0)
--   100  Khí Vàng         6 khung   (SUMO aura_11_0)
--   101  Khí Kim Quang    6 khung   (SUMO aura_12_0)
--   102  Khí Bạch Kim     6 khung   (SUMO aura_13_0)
--   103  Khí Huyết Đỏ     6 khung   (SUMO aura_14_0)
--   104  Khí Tử Điện      6 khung   (SUMO aura_15_0)
--   105  Khí Ngọc Bích    6 khung   (SUMO aura_31_0)
--   106  Khí Xích Long    6 khung   (SUMO aura_7_0)
--   107  Khí Lục Diệp     6 khung   (SUMO aura_8_0)
--   108  Khí Bạch Vân     6 khung   (SUMO aura_9_0)
--   109  Hồng Vân         5 khung   (SUMO aura_22_0)
--   110  Cột Sáng Hồng    4 khung   (BUN aura_22_0)
--   111  Hỏa Diệm Đỏ      4 khung   (BUN aura_16_0)
--   112  Lam Diệm         4 khung   (BUN aura_25_0)
--   113  Tử Diệm          4 khung   (BUN aura_56_0)
--   114  Hỏa Diệm Cam     4 khung   (BUN aura_80_0)
--   115  Tử Quang         6 khung   (BUN aura_83_0)
--
-- Chưa lấy: aura_99 của SUMO thực ra là bảng danh hiệu "ĐẠI GIA CHECKVAR" của họ;
-- aura_57 và aura_79 bên Bun cắt khung không ra số chẵn (713 = 23×31, 1406 chỉ chia
-- hết cho 2 và 19) nên bỏ.
--
-- Đi kèm: npc_list/GokuNoiLoan có menu chọn hào quang (2 trang, 18 loại kể cả
-- Goku Purple 98), consts/ConstNpc.CHON_HAO_QUANG.
-- Không cần tăng vsData/vsItem: ảnh img_by_name client xin theo tên khi cần.
-- Chạy lại nhiều lần vẫn an toàn.
-- =====================================================================

SET NAMES utf8mb4;

INSERT INTO `img_by_name` (`NAME`, `n_frame`) VALUES
('aura_99_0', 6),
('aura_99_1', 1),
('aura_100_0', 6),
('aura_100_1', 1),
('aura_101_0', 6),
('aura_101_1', 1),
('aura_102_0', 6),
('aura_102_1', 1),
('aura_103_0', 6),
('aura_103_1', 1),
('aura_104_0', 6),
('aura_104_1', 1),
('aura_105_0', 6),
('aura_105_1', 1),
('aura_106_0', 6),
('aura_106_1', 1),
('aura_107_0', 6),
('aura_107_1', 1),
('aura_108_0', 6),
('aura_108_1', 1),
('aura_109_0', 5),
('aura_109_1', 1),
('aura_110_0', 4),
('aura_110_1', 1),
('aura_111_0', 4),
('aura_111_1', 1),
('aura_112_0', 4),
('aura_112_1', 1),
('aura_113_0', 4),
('aura_113_1', 1),
('aura_114_0', 4),
('aura_114_1', 1),
('aura_115_0', 6),
('aura_115_1', 1)
ON DUPLICATE KEY UPDATE `n_frame` = VALUES(`n_frame`);

SELECT `NAME`, `n_frame` FROM `img_by_name` WHERE `NAME` REGEXP '^aura_(9[5-9]|1[0-1][0-9])_' ORDER BY `NAME`;
