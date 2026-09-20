-- =====================================================================
-- 31 — SỬA HIỂN THỊ MAP 166 "PHÒNG THÍ NGHIỆM MYUU" (2026-09-20)
-- =====================================================================
-- Map 166 đang dùng bộ tile số 23 (chung với map 97 Thành phố phía bắc).
-- Dữ liệu địa hình của 166 (data/map/tile_map_data/166) dùng tới ô tile số 29,
-- trong khi mọi map khác của bộ 23 chỉ dùng tới số 20 -> client không có hình cho
-- các ô 21–29 và vẽ ra một màn xám loang lổ.
--
-- Đổi sang bộ tile 30 + phông nền của Hành tinh ngục tù: bộ này có đủ tới ô 30
-- (map 155 đang dùng), tông tối, hợp với phòng thí nghiệm.
-- Cột `data` cũng ghi lại cho khớp (một số bản client đọc cột này).
--
-- DataGame.vsMap đã tăng 5 -> 6 trong jar để client tải lại dữ liệu map.
-- Chạy lại nhiều lần vẫn an toàn. Cần khởi động lại server.
-- =====================================================================

UPDATE `map_template`
   SET `tile_id` = 30,
       `bg_type` = 0,
       `bg_id`   = 17,
       `data`    = '[0,2,0,30,17]'
 WHERE `id` = 166;

-- KIỂM TRA: 166 phải giống 155 ở ba cột tile_id / bg_type / bg_id
SELECT `id`, `NAME`, `data`, `type`, `planet_id`, `bg_type`, `tile_id`, `bg_id`
  FROM `map_template` WHERE `id` IN (155, 166);
