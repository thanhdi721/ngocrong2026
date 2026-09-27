<?php

$ip_sv = "14.225.203.122";
$port = "3306";
$user_sv = "tech_test2";
$pass_sv = "tech_test2";
$dbname_sv = "blueemti";

try {
    $conn = new PDO("mysql:host=$ip_sv;port=$port;dbname=$dbname_sv", $user_sv, $pass_sv);
    $conn->setAttribute(PDO::ATTR_ERRMODE, PDO::ERRMODE_EXCEPTION);
    echo "CARD SV2";
} catch (PDOException $e) {
    die("Lỗi kết nối Cơ sở dữ liệu: " . $e->getMessage());
}

$conn->exec("SET NAMES 'utf8'");

// $validate = ValidateCallback($POST);
//if ($validate != false) { //Nếu xác thực callback đúng thì chạy vào đây.
    // $status = $validate['status']; //Trạng thái thẻ nạp, thẻ thành công = thanhcong , Thẻ sai, thẻ sai mệnh giá = thatbai
    // $serial = $validate['serial']; //Số serial của thẻ.
    // $pin = $validate['pin']; //Mã pin của thẻ.
    // $card_type = $validate['card_type']; //Loại thẻ. vd: Viettel, Mobifone, Vinaphone.
    // $amount = $validate['amount']; //Mệnh giá của thẻ. nếu bạn sài thêm hàm sai mệnh giá vui lòng sử dụng thêm hàm này tự cập nhật mệnh giá thật kèm theo desc là mệnh giá củ
    // $real_amount = $validate['real_amount']; // thực nhận đã trừ chiết khấu
    // $content = $validate['content']; // id transaction

    function check_string($data)
    {
        return trim(htmlspecialchars(addslashes($data)));
        //return str_replace(array('<',"'",'>','?','/',"\\",'--','eval(','<php'),array('','','','','','','','',''),htmlspecialchars(addslashes(strip_tags($data))));
    }
    /** CALLBACK */

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
              $giatri = 2;
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
        //  //Xử lý nạp thẻ thành công tại đây.
        //     $price = $amount * $giatri;
        //     // Tính toán mới dựa trên giá trị $amount
        //     if ($amount >= 100000 && $amount < 500000) {
        //     // Nếu $amount từ 100k đến 500k, thì áp dụng tỷ lệ 1.35
        //     $price1 = $amount * $giatri * 270;
        //     } elseif ($amount >= 500000) {
        //     // Nếu $amount trên 500k, thì áp dụng tỷ lệ 1.70
        //     $price1 = $amount * $giatri * 340;
        //     } else {
        //     // Áp dụng tỷ lệ cơ bản nếu không rơi vào hai khoảng trên
        //     $price1 = $price * 170; // Giữ nguyên giá trị $price nếu $amount dưới 100k
        // }
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

function ValidateCallback($out)
{ //Hàm kiểm tra callback từ server
    if (
        isset(
        $out['status'],
        $out['serial'],
        $out['pin'],
        $out['card_type'],
        $out['amount'],
        $out['content'],
        $out['real_amount']
    )
    ) {
        echo "xác thực ok";
        print_r($out);
        return $out; //xác thực thành công, return mảng dữ liệu từ server trả về.
    } else {
        return false; //Xác thực callback thất bại.
        echo "xác thực thất bại";
    }
}