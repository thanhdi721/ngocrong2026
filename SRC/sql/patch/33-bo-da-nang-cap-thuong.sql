-- =====================================================================
-- 33 — BỎ ĐÁ NÂNG CẤP 1–5 KHỎI THƯỞNG NHIỆM VỤ (2026-09-20)
-- =====================================================================
-- Chủ dự án chốt: thưởng nhiệm vụ không phát Đá nâng cấp cấp 1..5 (item 1074–1078) nữa.
-- Patch xoá các món đó khỏi cột `items` của bảng `task_main_reward`, đồng thời gỡ
-- câu nhắc thưởng trong cột `text` và trong mô tả nhiệm vụ `task_main_template`.`detail`.
--
-- Các phần thưởng khác (sức mạnh, tiềm năng, đậu thần, đá bảo vệ, đá ngũ sắc,
-- sao pha lê, mảnh ký ức...) giữ nguyên.
-- Chạy lại nhiều lần vẫn an toàn. Khởi động lại server (không cần jar mới cho phần này).
-- =====================================================================

-- (1) Gỡ vật phẩm khỏi danh sách thưởng
UPDATE `task_main_reward`
   SET `items` = REGEXP_REPLACE(`items`, '\\[107[4-8],[0-9]+,\\[\\]\\],?', '')
 WHERE `items` REGEXP '\\[107[4-8],';

-- dọn dấu phẩy thừa nếu món bị xoá nằm ở cuối danh sách
UPDATE `task_main_reward` SET `items` = REGEXP_REPLACE(`items`, ',\\]$', ']')
 WHERE `items` LIKE '%,]';

-- (2) Gỡ câu "Thưởng N Đá nâng cấp cấp X" trong dòng mô tả thưởng
UPDATE `task_main_reward`
   SET `text` = TRIM(REGEXP_REPLACE(`text`, '(\\. )?Thưởng [0-9]+ Đá nâng cấp cấp [0-9]+', ''))
 WHERE `text` LIKE '%Đá nâng cấp%';

-- (3) Gỡ trong mô tả nhiệm vụ (dòng "Thưởng: ...")
UPDATE `task_main_template`
   SET `detail` = REGEXP_REPLACE(`detail`, ', *[0-9]+ Đá nâng cấp( cấp)? [0-9]+', '')
 WHERE `detail` REGEXP '[0-9]+ Đá nâng cấp';

UPDATE `task_main_template`
   SET `detail` = REGEXP_REPLACE(`detail`, '[0-9]+ Đá nâng cấp( cấp)? [0-9]+, *', '')
 WHERE `detail` REGEXP '[0-9]+ Đá nâng cấp';

-- KIỂM TRA: cả ba câu dưới phải trả về 0 dòng
SELECT COUNT(*) AS `con_item_da_nang_cap` FROM `task_main_reward` WHERE `items` REGEXP '\\[107[4-8],';
SELECT COUNT(*) AS `con_chu_trong_thuong` FROM `task_main_reward` WHERE `text` LIKE '%Đá nâng cấp%';
SELECT COUNT(*) AS `con_chu_trong_mo_ta` FROM `task_main_template` WHERE `detail` LIKE '%Đá nâng cấp%';
