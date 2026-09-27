<?php
require_once 'core/connect.php';


ob_clean(); // clear output buffer
//header("Location: ../nap-mbbank"); // redirect to "nap-mbbank.php"
error_reporting(0);
$token_bot = "6486474915:AAGpuO7aqOWL1G8D8bLuoCbuBdZWV8pZa-8";
$chat_id = "1259736350";
function parse_order_id($des, $MEMO_PREFIX)
{
    $re = '/'.$MEMO_PREFIX.'\d+/im';
    preg_match_all($re, $des, $matches, PREG_SET_ORDER, 0);
    if (count($matches) == 0) {
        return null;
    }
    // Print the entire match result
    $orderCode = $matches[0][0];
    $prefixLength = strlen($MEMO_PREFIX);
    $orderId = intval(substr($orderCode, $prefixLength));
    return $orderId ;
}
// Lấy dữ liệu từ API Mbbank
$url = "https://api.sieuthicode.net/historyapimbbank/IgqlotacUneH-XOUuzo-ASrk-BUIW-vgQJ";
$ch = curl_init();
curl_setopt($ch, CURLOPT_URL, $url);
curl_setopt($ch, CURLOPT_RETURNTRANSFER, true);
$data = curl_exec($ch);
curl_close($ch);

$response = json_decode($data, true);
$tranList = $response['TranList'];
$count = count($tranList);

for ($x = 0; $x < $count; $x++) {
    $tranId = $tranList[$x]['refNo'];
    $io = $tranList[$x]['availableBalance'];
    $amount = $tranList[$x]['creditAmount'];
    $description = $tranList[$x]['description'];
 //   $user_id = isset(explode(' ', $description)[1]) ? explode(' ', $description)[1] : "-"; 
    $user_id        = parse_order_id($description,'nremti');
    $user_id = strtolower($user_id);
    $account_query = "SELECT * FROM mbbank WHERE tranId = :tranId ";
                                $stmt = $conn2->prepare($account_query);
                                $stmt->bindParam(":tranId", $tranId);
                                $stmt->execute();
    print_r($tranId);
    print_r($amount);
    echo "--";
     print_r($user_id);
     echo "</br>";
        if ($stmt->rowCount() > 0) {
        $sl =$stmt->rowCount();
        echo"Không có giao dịch mới $sl "  ;
        return;
      //  continue;
    } else {
         echo"Xử lý thành công 1 đơn hàng</br>";
         //Xử lý nạp thẻ thành công tại đây.
         $giatri = 2;
            $price1 = $amount * $giatri;
            // Tính toán mới dựa trên giá trị $amount
            if ($amount >= 100000 && $amount < 500000) {
            // Nếu $amount từ 100k đến 500k, thì áp dụng tỷ lệ 1.35
            $price1 =$price1 * 120;//120
            } elseif ($amount >= 500000) {
            // Nếu $amount trên 500k, thì áp dụng tỷ lệ 1.70
            $price1 = $price1  * 150;//150
            } else {
            // Áp dụng tỷ lệ cơ bản nếu không rơi vào hai khoảng trên
            $price1 = $price1  * 100; // Giữ nguyên giá trị $price nếu $amount dưới 100k
        }
        //     $price1 = $amount * $giatri;
        //     // Tính toán mới dựa trên giá trị $amount
        //     if ($amount >= 100000 && $amount < 500000) {
        //     // Nếu $amount từ 100k đến 500k, thì áp dụng tỷ lệ 1.35
        //     $price1 =$price1 * 300;
        //     } elseif ($amount >= 500000) {
        //     // Nếu $amount trên 500k, thì áp dụng tỷ lệ 1.70
        //     $price1 = $price1 * 400;
        //     } else {
        //     // Áp dụng tỷ lệ cơ bản nếu không rơi vào hai khoảng trên
        //     $price1 = $price1 * 200; // Giữ nguyên giá trị $price nếu $amount dưới 100k
        // }
        try {
         $stmt = $conn2->prepare("UPDATE account SET tongnap = tongnap + :price*$giatri, tongnap2 = tongnap2 + :price* $giatri, tongnap3 = tongnap3 + :price, vnd = vnd + :price1 WHERE id = :username");
            $stmt->bindParam(":price", $amount);
            $stmt->bindParam(":price1", $price1);
            $stmt->bindParam(':username', $user_id);
            $stmt->execute();
        } catch (PDOException $e) {
    echo "Error: " . $e->getMessage();
}

         $update_query1 = "INSERT INTO mbbank (tranId, io, amount, comment)
                         VALUES (:tranId, :io, :amount, :user_id)";
                                                $stmt = $conn2->prepare($update_query1);
                                                $stmt->bindParam(':tranId', $tranId);
                                                $stmt->bindParam(":io", $io, PDO::PARAM_INT);
                                                $stmt->bindParam(":amount", $amount, PDO::PARAM_INT);
                                                $stmt->bindParam(":user_id", $user_id, PDO::PARAM_INT);
                                                $stmt->execute();  
        //  $text_succes = "𝙉𝙊𝙏𝙄𝙁𝙄𝘾𝘼𝙏𝙄𝙊𝙉(nronight.com) \nXử lý thành công 1 đơn hàng !!\n—————————————————————\n<strong>User ID:</strong> $user_id\n—————————————————————\n<strong>Số dư :</strong> $io\n—————————————————————\n<strong>Số Tiền:</strong> " . number_format($amount) . " VNĐ\n—————————————————————\n<strong>Thanh Toán:</strong> MBbank\n—————————————————————\n";
        //             $data = [
        //                 'text' => $text_succes,
        //                 'chat_id' => $chat_id,
        //                 'parse_mode' => 'HTML',
        //             ];
        //             file_get_contents("https://api.telegram.org/bot$token_bot/sendMessage?" . http_build_query($data));
    }
}
mysqli_close($conn2);
?>

<?php
exit(); // exit script
?>