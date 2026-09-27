<?php

$ip_sv = "14.225.203.122";
$port = "3306";
$user_sv = "tech_test2";
$pass_sv = "tech_test2";
$dbname_sv = "blue";
try {
    $conn = new PDO("mysql:host=$ip_sv;port=$port;dbname=$dbname_sv", $user_sv, $pass_sv);
    $conn->setAttribute(PDO::ATTR_ERRMODE, PDO::ERRMODE_EXCEPTION);
    echo "CARD SV111";
} catch (PDOException $e) {
    die("Lỗi kết nối Cơ sở dữ liệu: " . $e->getMessage());
}

$conn->exec("SET NAMES 'utf8'");
    function check_string($data)
    {
        return trim(htmlspecialchars(addslashes($data)));
    }
    if(isset($_POST['pin']) && isset($_POST['status'])){
        $status = check_string($_POST['status']);
        $pin = check_string($_POST['pin']);
        $content = check_string($_POST['content']); // request id
        $amount = check_string($_POST['amount']); //Giá trị khai báo
        $real_amount = check_string($_POST['real_amount']); //Số tiền nhận đượ
        $serial = check_string($_POST['serial']);
        $card_type = check_string($_POST['card_type']);

    $stmt = $conn->prepare("SELECT * FROM `trans_log` WHERE status = 0 AND trans_id = :content AND pin = :pin AND seri = :serial AND type = :card_type");
    $stmt->bindParam(':content', $content);
    $stmt->bindParam(':pin', $pin);
    $stmt->bindParam(':serial', $serial);
    $stmt->bindParam(':card_type', $card_type);
    $stmt->execute();
    
    if ($stmt->rowCount() > 0) {
        $result = $stmt->fetch(PDO::FETCH_ASSOC);
        print_r($result);
        if ($status == 'thanhcong') {

            // Fetch giatri from trans_log table
            $stmt = $conn->prepare("SELECT giatri FROM `trans_log` WHERE `id` = :id");
            $stmt->bindParam(':id', $result['id']);
            $stmt->execute();
            $row = $stmt->fetch(PDO::FETCH_ASSOC);
            $giatri = $row['giatri']; // Assuming giatri is a column in the trans_log table

            // //Xử lý nạp thẻ thành công tại đây.
            // $price = $amount * $giatri;
            $giatri = 1;
          $price1 = $amount * $giatri;
            // Tính toán mới dựa trên giá trị $amount
            if ($amount >= 100000 && $amount < 500000) {
            // Nếu $amount từ 100k đến 500k, thì áp dụng tỷ lệ 1.35
            $price1 =$price1 * 1.10;//120
            } elseif ($amount >= 500000) {
            // Nếu $amount trên 500k, thì áp dụng tỷ lệ 1.70
            $price1 = $price1 * 1.40;//150
            } else {
            // Áp dụng tỷ lệ cơ bản nếu không rơi vào hai khoảng trên
            $price1 = $price1 * 0.85; // Giữ nguyên giá trị $price nếu $amount dưới 100k
        }
            $stmt = $conn->prepare("UPDATE account SET danap = danap + :price * $giatri, cash = cash + :price1 WHERE username = :username");
            $stmt->bindParam(':price', $amount);
            $stmt->bindParam(':price1', $price1);
            $stmt->bindParam(':username', $result['name']);
            $stmt->execute();
            $stmt = $conn->prepare("UPDATE `trans_log` SET `status` = 1 WHERE `id` = :id");
            $stmt->bindParam(':id', $result['id']);
            $stmt->execute();
        } else if ($status == 'saimenhgia') {
            //Xử lý nạp thẻ sai mệnh giá tại đây.
            $stmt = $conn->prepare("UPDATE `trans_log` SET status = 3, `amount` = :amount WHERE `id` = :id");
            $stmt->bindParam(':amount', $amount);
            $stmt->bindParam(':id', $result['id']);
            $stmt->execute();
        } else {
            //Xử lý nạp thẻ thất bại tại đây.
            $stmt = $conn->prepare("UPDATE `trans_log` SET status = 2 WHERE `id` = :id");
            $stmt->bindParam(':id', $result['id']);
            $stmt->execute();
        }

        # Lưu log Nạp Thẻ
        $file = "card.log";
        $fh = fopen($file, 'a') or die("cant open file");
        fwrite($fh, "Tai khoan: " . $result['name'] . ", data: " . json_encode($_POST));
        fwrite($fh, "\r\n");
        fclose($fh);
    }
}