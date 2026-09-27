<?php
// require_once 'core/set.php';
require_once 'core/connect.php';
$_alert = null;
// session_start();
require_once 'core/head.php';
if ($_login === null) {
    echo '<script>window.location.href = "../dang-nhap.php";</script>';
}
        $user_id = $_SESSION['id'];
        $sv = $_SESSION['sv'];
        $tentaikhoan = 'TA QUANG HA';
        // $sotaikhoan ='0365996497';
        // $noidungnap ='ALLSTAR';
        $sotaikhoan ='';
        $noidungnap ='';
        if($sv == 1 ){
            $sotaikhoan = '101878725709';
            $noidungnap ='ALLSTAR V2';
            $conn = $conn;
        }
        if($sv == 2 ){
            $sotaikhoan = '0365996497';
            $noidungnap ='ALLSTAR V2';
            $conn = $conn1;
        }
        // if($sv == 3 ){
        //     $sotaikhoan = '101878725709';
        //     $noidungnap ='ALLSTAR';
        //     $conn = $conn2;
        // }
        // if($sv == 4 ){
        //     $sotaikhoan = '101878725709';
        //     $noidungnap ='ALLSTAR';
        //     $conn = $conn3;
        // }
        
        
      
 
  function qrbank($type, $stk, $accountname, $amount, $comment)
{
    if ($type == 'MOMO') {
        $result = 'data:image/png;base64,' . base64_encode(file_get_contents("https://chart.googleapis.com/chart?chs=500x500&cht=qr&chl=2|99|$stk|||0|0|$amount|$comment|transfer_myqr"));
    } else {
        $result = "https://api.vietqr.io/$type/$stk/0/$comment/vietqr_net_2.jpg?accountName=$accountname";
    }
    return $result;
}
  	?>
	
		<div class="p-1 mt-1 ibox-content" style="border-radius: 7px; box-shadow: 0px 0px 5px black;">
                <div class="card">
                  <div class="card-header">
                    <!-- <b>Nạp Tiền MoMo(AUTO)</b> -->
                    <br>
                    <a href="/nap-so-du.php"><b class="btn btn-action text-white" style="background-color: rgb(243, 146, 101);">THẺ CÀO AUTO</b></a>
                    <a href="/nap-momo.php"><b class="btn btn-action text-white" style="background-color: rgb(243, 146, 101);">MOMO AUTO <b></b> </b></a>
                    <a href="/nap-atm.php"><b class="btn btn-action text-white" style="background-color: rgb(101, 160, 243);">ATM AUTO <b></b> </b></a>
                            </div>					
							<div class="table-responsive">
    							<div class="card-header">
                                        <center><img class="mb-3"
    									src="<?=qrbank('Vietinbank',"$sotaikhoan","$tentaikhoan",100000,$noidungnap.' '. $user_id)?>"
    									width="200px" height="200px"></center>
    									- Mỗi khi nạp 20K sẽ nhận 20K COIN, nạp 100K trở lên x1.5 lần, 500K trở lên x1.8 lần
                                </div>
								<center>
                                    <ul class="list-group mb-2">
                                        <li class="list-group-item">Số tài khoản: <b id="copySTK11" style="color: green;"><? echo $sotaikhoan ?></b> 
                                        </li>
                                        <li class="list-group-item">Chủ tài khoản: <b><? echo $tentaikhoan ?></b>
                                        </li>
                                        <li class="list-group-item">Ngân hàng: <b>Vietinbank</b></li>
                                        <!--<div class="form-group mt-2">-->
                                        <!--<label>-->
                                        <!--<b>Server:</b>-->
                                        <!--</label>-->
                                        <!--<select class="form-control mt-1" name="sv" style="border-radius: 7px; box-shadow: red 0px 0px 5px;">-->
                                        <!--<option value="1">Server 1</option>-->
                                        <!--<option value="2">Server 2</option>-->
                                        <!--</select>-->
                                        <!--</div>      -->
                                        <li class="list-group-item">Nội dung nạp: <b id="copyNoiDung11" style="color: red;"><?= $noidungnap.' ' . $user_id ?></b>
                                        <!--<li class="list-group-item">Nội dung nạp: <b id="copyNoiDung11" style="color: red;"><?= $noidungnap . $user_id ?></b>-->

                                        </li>
                                        
                                    </ul>
                                    <center><i><i class="fa fa-spinner fa-spin"></i> Xử lý giao dịch tự động trong vài
                                            giây...</i></center>
							<center>
						<hr>
        		<div class="table-responsive">
              <div style="line-height: 15px;font-size: 12px;padding-right: 5px;margin-bottom: 8px;padding-top: 2px;" class="text-center">
			  <p><i>Chân Thành Cảm Ơn Vì Đã Ủng Hộ Chúng Tôi.</i></p>
				
                <tbody style="border-color: black;">
					</div>
				</div>
			
			</div>
				
            </div>
            <?php include_once 'core/footer.php'; ?>
                        <!--</div> -->
            <!--hết momo-->
            <div class="table-responsive">
              <div style="line-height: 15px;font-size: 12px;padding-right: 5px;margin-bottom: 8px;padding-top: 2px;" class="text-center">
                <span class="text-black" style="vertical-align: middle;">Hãy <a href="nap-atm.php"><b><u>Loading</u></b></a> lại website để cập nhật!</span>
              </div>
              <!-- <table class="table table-hover table-nowrap">
                <tbody style="border-color: black;">
                  <tr>
                    <th scope="col">STT</th>
                    <th scope="col">Mã GD</th>
                    <th scope="col">SỐ TIỀN</th>
                    <th scope="col">INGAME</th>

                  </tr>
            <?php
            $page = isset($_GET['page']) ? intval($_GET['page']) : 1;
            $recordsPerPage = 5;
            $startFrom = ($page - 1) * $recordsPerPage;
            $query = "SELECT * FROM mbbank WHERE comment = '$user_id' LIMIT $startFrom, $recordsPerPage";
            $result = mysqli_query($conn, $query);
            $hasData = false;
            
            $sql1 = "SELECT char_name FROM user WHERE id = '$user_id'";
            $result2 = $conn->query($sql1);
            if ($result2->num_rows > 0) {
              $row_hvd2 = $result2->fetch_assoc();
              $char_name = $row_hvd2["char_name"];
            }
            while ($row = mysqli_fetch_assoc($result)) {
                $hasData = true;
        ?>

                    <tr>
                              <td><b>#<?=$row['id'];?></b></td>
                              <td><b><?=$row['tranId'];?><b></td>
                              <td><b><?=$row['amount'];?></td>
                              <td><b><?=$char_name?></td>

                   </tr> 
                    <?php } 
                                
          // Kiểm tra biến cờ và hiển thị thông báo "Lịch Sử Trống" nếu không có dữ liệu
                    if (!$hasData) {
                       echo ' <tr>
                                 <td colspan="6" align="center"><span style="font-size:100%;"><< Lịch Sử Nạp Trống >></span></td>
                               </tr>';
                   }
                   ?>
                </tbody>
              </table> -->
           <?php
          // ...
          // Tính tổng số trang dựa trên số lượng bản ghi và số lượng bản ghi hiển thị trên mỗi trang
          $totalPages = ceil(mysqli_num_rows(mysqli_query($conn, "SELECT * FROM mbbank WHERE comment = '$user_id'")) / $recordsPerPage);
          ?>

          <!-- Thêm vào sau phần hiển thị lịch sử nạp thẻ -->
          <div class="pagination">
              <?php
              // Hiển thị nút Previous (trang trước)
              if ($page > 1) {
                  echo '<a href="atm_bank?page=' . ($page - 1) . '"><< Trước</a>';
              }

              // Hiển thị các nút trang
              for ($i = 1; $i <= $totalPages; $i++) {
                  echo '<a href="atm_bank?page=' . $i . '">' . $i . '</a>';
              }

              // Hiển thị nút Next (trang kế tiếp)
              if ($page < $totalPages) {
                  echo '<a href="atm_bank?page=' . ($page + 1) . '">Sau >></a>';
              }
              ?>
          </div>
          <style>
              .pagination {
                  display: flex;
                  justify-content: center;
              }

              .pagination a {
                  color: black;
                  padding: 8px 16px;
                  text-decoration: none;
                  border: 1px solid #ddd;
                  margin: 0 4px;
              }

              .pagination a.active {
                  background-color: #4CAF50;
                  color: white;
              }

              .pagination a:hover:not(.active) {
                  background-color: #ddd;
              }
          </style>
            </div>
            </div>
            
            
		<div>
   	</div>
			
<?php require_once('../core/end.php'); ?>