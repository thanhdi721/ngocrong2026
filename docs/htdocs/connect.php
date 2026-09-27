<?php
/* Kết nối kiểu mysqli cho mấy trang cũ. Thông số lấy từ core/cauhinh.php. */
require_once __DIR__ . '/core/cauhinh.php';

$ip_sv     = $db_host;
$dbname_sv = $db_name;
$user_sv   = $db_user;
$pass_sv   = $db_pass;

$conn_mysqli = new mysqli($db_host, $db_user, $db_pass, $db_name, (int) $db_port);
if ($conn_mysqli->connect_error) {
    die('Không kết nối được CSDL: ' . $conn_mysqli->connect_error);
}
$conn_mysqli->set_charset('utf8mb4');
