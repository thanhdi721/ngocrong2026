<?php
ini_set('display_errors', 1);
ini_set('display_startup_errors', 1);
error_reporting(E_ALL);
require_once ('../core/connect.php');
require_once ('../core/cauhinh.php');
// require_once '../core//set.php';
session_start();
include ('ApiMB.php');
define('POINTS_PER_TOPUP', 1);
if (!isset($_GET['users']) || $_GET['users'] !== $userloginmbbank_config) {
    exit('Không tìm thấy key! Không thể truy cập.');
}

function getUserDataFromToken($_token, $conn)
{
    $sql = "SELECT * FROM cpanel WHERE token = ?";
    $stmt = $conn->prepare($sql);
    $stmt->execute([$_token]);
    return $stmt->fetch(PDO::FETCH_ASSOC);
}

function updateUserData($userData, $MB, $conn)
{
    $MB->deviceIdCommon_goc = $MB->generateImei();
    $MB->user = $userData['userlogin'];
    $MB->pass = $userData['password'];

    $textCaptcha = $MB->bypass_captcha_web2m('413145b2f6d981e32d0ee69a56b0e839');
    $loginResult = json_decode($MB->login($textCaptcha), true);

    if (in_array($loginResult['result']['message'], ["Capcha code is invalid", "Customer is invalid"])) {
        exit(json_encode(array('status' => '1', 'msg' => 'Captcha hoặc thông tin không chính xác')));
    }

    $sql = "UPDATE cpanel SET name = ?, password = ?, sessionId = ?, deviceId = ?, time = ? WHERE userlogin = ?";
    $stmt = $conn->prepare($sql);
    $stmt->execute([$loginResult['cust']['nm'], $userData['password'], $loginResult['sessionId'], $MB->deviceIdCommon_goc, time(), $userData['userlogin']]);
}

function processTransaction($transaction, $conn)
{
    $noidungnap = $_SESSION['noidungnap'];
    // echo "nội dung nạp: " . $noidungnap;
    $description = $transaction['description'];
    // echo $description."</br>";
    $transactionDate = $transaction['transactionDate'];


    // if (preg_match('/blue\s+(\d+(?:\s\d+)*)/', rtrim($description, '.'), $matches)) {
  if (isset($noidungnap) && preg_match('/'.$noidungnap.'\s+(\d+(?:\s\d+)*)/', rtrim($description, '.'), $matches)) {
        $username = str_replace(['-', '-'], '', preg_replace('/\D/', '', $matches[1]));
        // echo "username: " . $username . "</br>";
        $amount = $transaction['creditAmount'];
        $refNo = $transaction['refNo'];
        $receiverAccountName = $transaction['benAccountName'] ?: "Ngân Hàng Quân Đội - MBBANK";
        $bankName = $transaction['bankName'] ?: "Ngọc Rồng Donuts";
        $accountNo = $transaction['accountNo'];
        
         $amount2 = $amount;
        if ($amount >= 100000 && $amount < 500000) {
            $Price = $amount2 * 1.20;
        } elseif ($amount >= 500000) {
            $Price = $amount2 * 1.50;
        } else {
            $Price = $amount2* 1.00;
        }

// echo $amount . "</br>";

        if ($amount >= 2000 && !isTransactionExist($refNo, $conn)) {
            updateAccountBalance($username, $Price, $amount, $amount2, $conn);
            insertTransactionHistory($username, $refNo, $transactionDate, $amount, $receiverAccountName, $accountNo, $bankName, $conn);
            insertCheckTransaction($refNo, $conn);
            // echo $description."</br>";
        }
    }else {
        
    //   echo "Không tìm thấy thông tin hoặc biến \$noidungnap chưa được gán giá trị."; 
    //   echo $description . "</br>";
    
    }
}

function isTransactionExist($refNo, $conn)
{
    $checkTransactionSql = "SELECT tranid FROM atm_check WHERE tranid = ?";
    $stmt = $conn->prepare($checkTransactionSql);
    $stmt->execute([$refNo]);
    return $stmt->rowCount() > 0;
}

function updateAccountBalance($username, $Price, $amount, $amount2, $conn)
{
    $updateAccountSql = "UPDATE account SET cash = cash + ?, danap = danap + ? WHERE id = ?";
    $stmt = $conn->prepare($updateAccountSql);
    $stmt->execute([$Price, $amount, $username]);
}

function insertTransactionHistory($username, $refNo, $transactionDate, $amount, $receiverAccountName, $accountNo, $bankName, $conn)
{
    $insertTransactionSql = "INSERT INTO atm_lichsu (user_nap, magiaodich, thoigian, sotien, status, benAccountName, accountNo, bankName) VALUES (?, ?, ?, ?, ?, ?, ?, ?)";
    $stmt = $conn->prepare($insertTransactionSql);
    // $stmt->execute([$username, $refNo, $transactionDate, $Price, 1, $receiverAccountName, $accountNo, $bankName]);
     $stmt->execute([$username, $refNo, $transactionDate, $amount, 1, $receiverAccountName, $accountNo, $bankName]);
}

function insertCheckTransaction($refNo, $conn)
{
    $insertCheckSql = "INSERT INTO atm_check (tranid) VALUES (?)";
    $stmt = $conn->prepare($insertCheckSql);
    $stmt->execute([$refNo]);
}

if (isset($_token) && !empty($_token)) {
    // echo "vô đây 2222";
    $userData = getUserDataFromToken($_token, $conn);
    if ($userData) {
        // echo "vô đây 111";
        $MB = new MBBANK;
        $transactionHistory = json_decode($MB->get_lsgd($userData['userlogin'], $userData['sessionId'], $userData['deviceId'], $userData['stk'], 2), true);
        
        if ($userData['time'] < time() - 60 && isset($transactionHistory['result']) && $transactionHistory['result']['message'] == 'Session invalid') {
            updateUserData($userData, $MB, $conn);
        }

        if (isset($transactionHistory['transactionHistoryList']) && is_array($transactionHistory['transactionHistoryList'])) {
            $_SESSION['noidungnap'] = "emti";
            foreach ($transactionHistory['transactionHistoryList'] as $transaction) {
                // echo
                processTransaction($transaction, $conn);
            }
            global $conn1;
            $_SESSION['noidungnap'] = "beo";
            foreach ($transactionHistory['transactionHistoryList'] as $transaction) {
                // echo
                processTransaction($transaction, $conn1);
            }
            // global $conn2;
            // $_SESSION['noidungnap'] = "genki";
            // foreach ($transactionHistory['transactionHistoryList'] as $transaction) {
            //     // echo
            //     processTransaction($transaction, $conn2);
            // }
            //  global $conn3;
            // $_SESSION['noidungnap'] = "genki";
            // foreach ($transactionHistory['transactionHistoryList'] as $transaction) {
            //     // echo
            //     processTransaction($transaction, $conn3);
            // }
        }

        $balanceInfo = json_decode($MB->get_balance($userData['userlogin'], $userData['sessionId'], $userData['deviceId']), true);
        if ($userData['time'] < time() - 60 && $balanceInfo['result']['message'] == 'Session invalid') {
            updateUserData($userData, $MB, $conn);
        }

        $currentBalance = null;

        if (isset($balanceInfo['result']) && $balanceInfo['result']['message'] == 'OK') {
            foreach ($balanceInfo['acct_list'] as $accountInfo) {
                if ($accountInfo['acctNo'] == $userData['stk']) {
                    $currentBalance = $accountInfo['currentBalance'];
                    break;
                }
            }
        } else {
            updateUserData($userData, $MB, $conn);
        }
    }
}

echo '<p>SoDu: ' . formatMoney($currentBalance) . '</p><hr>';
echo "server 1 </br>";
$sql = "SELECT * FROM atm_lichsu";
$stmt = $conn->prepare($sql);
$stmt->execute();
$transactionHistory = $stmt->fetchAll(PDO::FETCH_ASSOC);
foreach ($transactionHistory as $transaction) {
    $status = $transaction['status'] == 1 ? 'Thành công' : 'Thất bại';
    echo "ID: {$transaction['user_nap']} | GiaTri: {$transaction['sotien']} | TrangThai: $status | TaiKhoan: {$transaction['accountNo']} | MaGiaoDich: {$transaction['magiaodich']} | ThoiGian: {$transaction['thoigian']}<br>";
}

echo "server 2 </br>";
$sql = "SELECT * FROM atm_lichsu";
$stmt = $conn1->prepare($sql);
$stmt->execute();
$transactionHistory = $stmt->fetchAll(PDO::FETCH_ASSOC);
foreach ($transactionHistory as $transaction) {
    $status = $transaction['status'] == 1 ? 'Thành công' : 'Thất bại';
    echo "ID: {$transaction['user_nap']} | GiaTri: {$transaction['sotien']} | TrangThai: $status | TaiKhoan: {$transaction['accountNo']} | MaGiaoDich: {$transaction['magiaodich']} | ThoiGian: {$transaction['thoigian']}<br>";
}

// echo "server 3 </br>";
// $sql = "SELECT * FROM atm_lichsu";
// $stmt = $conn2->prepare($sql);
// $stmt->execute();
// $transactionHistory = $stmt->fetchAll(PDO::FETCH_ASSOC);
// foreach ($transactionHistory as $transaction) {
//     $status = $transaction['status'] == 1 ? 'Thành công' : 'Thất bại';
//     echo "ID: {$transaction['user_nap']} | GiaTri: {$transaction['sotien']} | TrangThai: $status | TaiKhoan: {$transaction['accountNo']} | MaGiaoDich: {$transaction['magiaodich']} | ThoiGian: {$transaction['thoigian']}<br>";
// }
// echo "server 2 </br>";
// $sql = "SELECT * FROM atm_lichsu";
// $stmt = $conn3->prepare($sql);
// $stmt->execute();
// $transactionHistory = $stmt->fetchAll(PDO::FETCH_ASSOC);
// foreach ($transactionHistory as $transaction) {
//     $status = $transaction['status'] == 1 ? 'Thành công' : 'Thất bại';
//     echo "ID: {$transaction['user_nap']} | GiaTri: {$transaction['sotien']} | TrangThai: $status | TaiKhoan: {$transaction['accountNo']} | MaGiaoDich: {$transaction['magiaodich']} | ThoiGian: {$transaction['thoigian']}<br>";
// }