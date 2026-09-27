<?php
require_once 'connect.php';
session_start();
if( @$_SESSION['sv'] == 1){
    $conn = $conn;
}
if( @$_SESSION['sv'] == 2){
    $conn = $conn1;
}
// if( @$_SESSION['sv'] == 3){
//     $conn = $conn2;
// }
// if( @$_SESSION['sv'] == 4){
//     $conn = $conn3;
// }

function _query($sql)
{
    global $conn;
    return $conn->query($sql);
}

function _fetch($sql)
{
    return _query($sql)->fetch(PDO::FETCH_ASSOC);
}

function isset_sql($txt)
{
    global $conn;
    return $conn->quote($txt);
}

function _insert($table, $input, $output)
{
    return "INSERT INTO $table($input) VALUES($output)";
}

function _select($select, $from, $where)
{
    return "SELECT $select FROM $from WHERE $where";
}

function _update($tabname, $input_output, $where)
{
    return "UPDATE $tabname SET $input_output WHERE $where";
}

function _delete($table, $condition)
{
    global $conn;
    _query("DELETE FROM $table WHERE $condition");
}

function show_alert($alert)
{
    echo '<div class="' . $alert[0] . '">' . $alert[1] . '</div>';
}

function _num_rows($result)
{
    return $result->rowCount();
}

function has_mkc2($username)
{
    // CSDL team2026 KHÔNG có cột `account`.`mkc2` — tính năng mật khẩu cấp 2 của WEB
    // đã bỏ. Giữ lại hàm để mấy trang cũ gọi vào không chết.
    // (Mã bảo vệ TRONG GAME là thứ khác, nằm ở `player`, web không đụng tới.)
    return false;
}
?>