<?php
// Kiểm tra xem phiên làm việc đã được khởi động hay chưa
if (session_status() == PHP_SESSION_NONE) {
	// Nếu chưa khởi động, tiến hành khởi động phiên làm việc
	session_start();
}

require_once '../core/set.php';
require_once '../core/connect.php';

if (!isset($_POST['pin'], $_POST['serial'], $_POST['card_type'], $_POST['card_amount'])) {
	exit('<script type="text/javascript">toastr.error("Vui Lòng Nhập Đầy Đủ Thông Tin !");</script>');
}
$apikey = 'FBFE9A4E1DE8EFDA9C7FBCC5FB11B87C'; 
$apikey1 = '4E321C244AAF8BF17BB347D72404A3FF'; 
// $apikey2 = 'BE9C642F7A1A763D9303DBC749B7B3D2'; 
$sv = $_POST['sv'];
if ($sv == 2){
    $apikey = $apikey1;
// }elseif ($sv == 3){
//     $apikey = $apikey2;
}else {
    $apikey = $apikey;
}
$content = md5(time() . rand(0, 999999) . microtime(true));
$seri = $_POST['serial'];
$pin = $_POST['pin'];
$loaithe = $_POST['card_type'];
$menhgia = $_POST['card_amount'];
$username = $_username; // sử dụng biến phiên

$url = "https://thesieutoc.net/chargingws/v2?APIkey=" . $apikey . "&mathe=" . $pin . "&seri=" . $seri . "&type=" . $loaithe . "&menhgia=" . $menhgia . "&content=" . $content;
$ch = curl_init();
curl_setopt($ch, CURLOPT_URL, $url);
curl_setopt($ch, CURLOPT_CAINFO, __DIR__ . '/../api/curl-ca-bundle.crt');
curl_setopt($ch, CURLOPT_CAPATH, __DIR__ . '/../api/curl-ca-bundle.crt');
curl_setopt($ch, CURLOPT_RETURNTRANSFER, true);
$out = json_decode(curl_exec($ch));
$http_code = 0;
if (isset($out->status)) {
    $http_code = 200;
}
curl_close($ch);

if ($http_code == 200) {
    if ($out->status == '00' || $out->status == 'thanhcong') {
        try {
            $stmt = $conn->prepare("INSERT INTO trans_log (name, trans_id, amount, pin, seri, type) VALUES (:username, :content, :menhgia, :pin, :seri, :loaithe)");
            $stmt->bindParam(':username', $username);
            $stmt->bindParam(':content', $content);
            $stmt->bindParam(':menhgia', $menhgia);
            $stmt->bindParam(':pin', $pin);
            $stmt->bindParam(':seri', $seri);
            $stmt->bindParam(':loaithe', $loaithe);
            $stmt->execute();

            echo '<script type="text/javascript">swal("Thành Công", "' . $out->msg . '", "success");</script>';
        } catch (PDOException $e) {
            echo '<script type="text/javascript">toastr.error("Database error: ' . $e->getMessage() . '");</script>';
        }
    } else if ($out->status != '00' && $out->status != 'thanhcong') {
        echo '<script type="text/javascript">toastr.error("' . $out->msg . '");</script>';
    }
} else {
    echo '<script type="text/javascript">toastr.error("Có lỗi máy chủ vui lòng thử lại sau!");</script>';
}
?>