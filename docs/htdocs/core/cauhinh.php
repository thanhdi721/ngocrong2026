<?php
/* =====================================================================
 * cauhinh.php — FILE CẤU HÌNH DUY NHẤT CỦA WEB
 * ---------------------------------------------------------------------
 * Sửa ở ĐÂY là đủ, không phải đi sửa từng trang.
 *
 * Bản này đã:
 *   - trỏ về CSDL `team2026` của server (trước đây là `allstar`);
 *   - bỏ toàn bộ phần nạp tiền: số tài khoản, số điện thoại, tên chủ tài khoản,
 *     token MBBank / Momo của chủ web cũ;
 *   - bỏ truy vấn `SELECT token FROM cpanel` — bảng `cpanel` KHÔNG có trong
 *     team2026 nên câu đó làm chết cả trang.
 * ===================================================================== */

//======================= 1. KẾT NỐI CƠ SỞ DỮ LIỆU =======================
// Đúng CSDL mà server game đang dùng. Trên XAMPP để nguyên localhost/root/rỗng.
$db_host = 'localhost';
$db_port = '3306';
$db_name = 'team2026';
$db_user = 'root';
$db_pass = '';

date_default_timezone_set('Asia/Ho_Chi_Minh');

// `connect.php` dựng sẵn $conn rồi; ở đây chỉ dựng nếu chưa có, để file này
// đứng một mình (cron, api…) cũng chạy được.
if (!isset($conn) || !($conn instanceof PDO)) {
    try {
        $conn = new PDO(
            "mysql:host=$db_host;port=$db_port;dbname=$db_name;charset=utf8mb4",
            $db_user,
            $db_pass
        );
        $conn->setAttribute(PDO::ATTR_ERRMODE, PDO::ERRMODE_EXCEPTION);
    } catch (PDOException $e) {
        die('Không kết nối được CSDL. Kiểm tra lại $db_name / $db_user / $db_pass trong core/cauhinh.php');
    }
}

//======================= 2. THÔNG TIN MÁY CHỦ =======================
// Địa chỉ web. Chạy trên XAMPP ở chính máy server thì để http://localhost/
$_domain     = 'http://localhost/';
$_tenmaychu  = 'NRO SkyRain';
$_mienmaychu = 'NRO SkyRain';
$_title      = 'NRO SkyRain — Ngọc Rồng Online';
$_IP         = $_SERVER['REMOTE_ADDR'] ?? '';

// IP : cổng mà CLIENT GAME kết nối vào.
// PHẢI KHỚP DataGame.LINK_IP_PORT bên server game, nếu không client nối sai chỗ.
$game_ip   = '157.10.44.25';
$game_port = '14445';

// Logo. Ảnh gốc 3584 px / 7 MB đã được thu về cỡ web (600 px, 242 KB) —
// đưa ảnh 7 MB lên đầu trang thì trang nào cũng phải tải thêm 7 MB.
$_logo      = '/image/logo-skyrain.png';      // logo ngang ở đầu trang
$_logo_2x   = '/image/logo-skyrain@2x.png';   // bản nét gấp đôi cho màn hình retina
$_favicon   = '/image/favicon-64.png';        // ảnh nhỏ trên tab trình duyệt

// Máy chủ web dùng cho trang "tình trạng máy chủ" (api/cauhinh). Để rỗng là tắt.
$serverIP   = '';
$serverPort = '';

//======================= 3. PHIÊN BẢN FILE GAME =======================
$_android = '2.3.0';
$_windows = '2.2.5';
$_java    = '2.2.1';
$_iphone  = '2.2.2';

//======================= 4. LIÊN HỆ / CỘNG ĐỒNG =======================
// ĐỂ RỖNG là nút đó KHÔNG hiện lên web. Điền link của bạn vào nếu muốn hiện.
// (Link Zalo / Fanpage / số điện thoại của chủ web cũ đã bị xoá hết.)
$_link_fanpage  = '';
$_link_zalo     = '';
$_link_telegram = '';
$_link_discord  = '';

//======================= 5. CHỮ CHẠY Ở ĐẦU TRANG =======================
$_chu_chay = 'Chào mừng các bạn đến với NRO SkyRain. Tải game và chiến ngay!';

//======================= 6. reCAPTCHA (để rỗng là tắt) =======================
$w_api_recaptcha         = '';
$w_api_recaptcha_private = '';

//======================= 7. GỬI MAIL (chức năng Quên mật khẩu) =======================
// ĐỂ RỖNG $smtp_user là TẮT hẳn chức năng quên mật khẩu (trang sẽ báo chưa cấu hình).
// Dùng Gmail thì $smtp_pass là "mật khẩu ứng dụng" 16 ký tự, KHÔNG phải mật khẩu Gmail.
// (Mật khẩu ứng dụng của chủ web cũ đã bị xoá khỏi mã nguồn.)
$smtp_host   = 'smtp.gmail.com';
$smtp_port   = 587;
$smtp_secure = 'tls';
$smtp_user   = '';
$smtp_pass   = '';
$smtp_ten    = $_tenmaychu;

//======================= 8. TIỆN ÍCH =======================
function CreateToken()
{
    return md5(uniqid((string) rand(), true));
}

/** Có bật mục liên hệ nào không (dùng để ẩn cả cụm nút nếu để rỗng hết). */
function co_link_lien_he()
{
    global $_link_fanpage, $_link_zalo, $_link_telegram, $_link_discord;
    return $_link_fanpage !== '' || $_link_zalo !== '' || $_link_telegram !== '' || $_link_discord !== '';
}
