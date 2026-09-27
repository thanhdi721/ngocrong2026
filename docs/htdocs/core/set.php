<?php
require_once 'config.php';
date_default_timezone_set('Asia/Ho_Chi_Minh');

// Kiểm tra và khởi động phiên làm việc nếu chưa được khởi động
// if (session_status() == PHP_SESSION_NONE) {
// 	session_start();
// }

function fetchUserData($conn, $username) {
    $stmt = $conn->prepare("SELECT * FROM account WHERE username = :username");
    $stmt->bindParam(":username", $username);
    $stmt->execute();
    $user_arr = $stmt->fetch(PDO::FETCH_ASSOC);

    if (!$user_arr) {
        header("Location: /logout.php");
        exit();
    }

    // CSDL team2026 đặt tên cột khác bản web gốc (`allstar`):
    //     gmail -> email   |   cash -> vnd   |   danap -> tongnap
    // và KHÔNG có cột `mkc2` (mật khẩu cấp 2 của web). Dùng ?? để trang vẫn chạy
    // nếu sau này cột bị đổi tên hay thiếu, thay vì văng "Undefined index".
    $_username  = htmlspecialchars($user_arr['username'] ?? '');
    $_password  = htmlspecialchars($user_arr['password'] ?? '');
    $_gmail     = htmlspecialchars($user_arr['email'] ?? '');
    $_gioithieu = htmlspecialchars((string) ($user_arr['gioithieu'] ?? ''));
    $_admin     = htmlspecialchars((string) ($user_arr['admin'] ?? 0));
    $_coin      = $user_arr['vnd'] ?? 0;
    $_tcoin     = htmlspecialchars((string) ($user_arr['tongnap'] ?? 0));
    $_status    = $user_arr['active'] ?? 1;
    $_thoivang  = $user_arr['thoi_vang'] ?? 0;

    // Mật khẩu cấp 2 của web đã bỏ (team2026 không có cột `mkc2`).
    $has_mkc2 = false;

    return [
        "_username" => $_username,
        "_password" => $_password,
        "_gmail" => $_gmail,
        "_gioithieu" => $_gioithieu,
        "_admin" => $_admin,
        "_coin" => $_coin,
        "_tcoin" => $_tcoin,
        "_status" => $_status,
        "_thoivang" => $_thoivang,
        "has_mkc2" => $has_mkc2,
    ];
}

$_login = $_login ?? null;
$_user = $_SESSION['account'] ?? null;

if ($_user !== null) {
    $_login = "on";
    $user_data = fetchUserData($conn, $_user);

    $_username = $user_data["_username"];
    $_password = $user_data["_password"];
    $_gmail = $user_data["_gmail"];
    $_gioithieu = $user_data["_gioithieu"];
    $_admin = $user_data["_admin"];
    $_coin = $user_data["_coin"];
    $_tcoin = $user_data["_tcoin"];
    $_status = $user_data["_status"];
    $_thoivang = $user_data["_thoivang"];
    $has_mkc2 = $user_data["has_mkc2"];
} else {
    $_login = null;
    // Chưa đăng nhập thì mấy biến dưới vẫn phải tồn tại: các trang admincp đọc $_admin
    // NGAY dòng đầu để quyết định đá về trang chủ. Thiếu là PHP 8 kêu "Undefined variable".
    $_username = $_password = $_gmail = $_gioithieu = '';
    $_admin = 0;
    $_coin = $_tcoin = $_thoivang = 0;
    $_status = 0;
    $has_mkc2 = false;
}

if (isset($_GET['out'])) {
    if ($_login == "on") {
        // Người dùng đã đăng nhập, không thực hiện logout
        header("Location: /");
        exit();
    } else {
        // Người dùng chưa đăng nhập, thực hiện logout
        session_destroy();
        header("Location: /");
        exit();
    }
}
?>
