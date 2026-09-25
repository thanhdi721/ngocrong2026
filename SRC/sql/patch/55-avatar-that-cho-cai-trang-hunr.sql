-- =====================================================================
-- 55 — DÙNG AVATAR THẬT CỦA HUNR CHO 34 CẢI TRANG (2026-09-23)
-- =====================================================================
-- Lúc đầu tôi tưởng HUNR không có bảng avatar nên tự ghép mảnh đầu rồi phóng to,
-- ảnh bị mờ. Thật ra họ để trong bảng `nr_others`, key 'avatar'.
-- Nay dùng đúng ảnh avatar gốc; ảnh tự dựng đã xoá khỏi data/icon.
-- Patch 48 đã sửa theo. Chạy lại vẫn an toàn. Không cần tăng vsItem / vsData
-- (head_avatar đi trong gói thông tin nhân vật, gửi lại mỗi lần đăng nhập).
-- =====================================================================

UPDATE `head_avatar` SET `avatar_id` = 17755 WHERE `head_id` = 2238;
UPDATE `head_avatar` SET `avatar_id` = 17756 WHERE `head_id` = 2241;
UPDATE `head_avatar` SET `avatar_id` = 17757 WHERE `head_id` = 2244;
UPDATE `head_avatar` SET `avatar_id` = 17758 WHERE `head_id` = 2247;
UPDATE `head_avatar` SET `avatar_id` = 17759 WHERE `head_id` = 2250;
UPDATE `head_avatar` SET `avatar_id` = 17760 WHERE `head_id` = 2253;
UPDATE `head_avatar` SET `avatar_id` = 17761 WHERE `head_id` = 2256;
UPDATE `head_avatar` SET `avatar_id` = 17762 WHERE `head_id` = 2259;
UPDATE `head_avatar` SET `avatar_id` = 17763 WHERE `head_id` = 2262;
-- 2089 Cải trang Fide đen: avatar bên HUNR trỏ nhầm vào ĐÔI GIÀY, nên tự ghép từ mảnh đầu.
UPDATE `head_avatar` SET `avatar_id` = 14739 WHERE `head_id` = 2265;
UPDATE `head_avatar` SET `avatar_id` = 17765 WHERE `head_id` = 2268;
UPDATE `head_avatar` SET `avatar_id` = 17766 WHERE `head_id` = 2271;
UPDATE `head_avatar` SET `avatar_id` = 17767 WHERE `head_id` = 2274;
UPDATE `head_avatar` SET `avatar_id` = 17768 WHERE `head_id` = 2277;
UPDATE `head_avatar` SET `avatar_id` = 17769 WHERE `head_id` = 2280;
UPDATE `head_avatar` SET `avatar_id` = 17770 WHERE `head_id` = 2283;
UPDATE `head_avatar` SET `avatar_id` = 17771 WHERE `head_id` = 2286;
UPDATE `head_avatar` SET `avatar_id` = 17772 WHERE `head_id` = 2289;
UPDATE `head_avatar` SET `avatar_id` = 17773 WHERE `head_id` = 2292;
UPDATE `head_avatar` SET `avatar_id` = 17774 WHERE `head_id` = 2295;
UPDATE `head_avatar` SET `avatar_id` = 17775 WHERE `head_id` = 2298;
UPDATE `head_avatar` SET `avatar_id` = 17776 WHERE `head_id` = 2301;
UPDATE `head_avatar` SET `avatar_id` = 17777 WHERE `head_id` = 2304;
UPDATE `head_avatar` SET `avatar_id` = 17778 WHERE `head_id` = 2307;
UPDATE `head_avatar` SET `avatar_id` = 17779 WHERE `head_id` = 2310;
UPDATE `head_avatar` SET `avatar_id` = 17780 WHERE `head_id` = 2313;
UPDATE `head_avatar` SET `avatar_id` = 17781 WHERE `head_id` = 2316;
UPDATE `head_avatar` SET `avatar_id` = 17782 WHERE `head_id` = 2319;
UPDATE `head_avatar` SET `avatar_id` = 17783 WHERE `head_id` = 2322;
UPDATE `head_avatar` SET `avatar_id` = 17784 WHERE `head_id` = 2325;
UPDATE `head_avatar` SET `avatar_id` = 17785 WHERE `head_id` = 2328;
UPDATE `head_avatar` SET `avatar_id` = 17786 WHERE `head_id` = 2331;
UPDATE `head_avatar` SET `avatar_id` = 17787 WHERE `head_id` = 2334;
UPDATE `head_avatar` SET `avatar_id` = 17788 WHERE `head_id` = 2337;

SELECT `head_id`, `avatar_id` FROM `head_avatar` WHERE `head_id` BETWEEN 2238 AND 2339;
