<?php
require_once '../core/set.php';
require_once '../core/connect.php';
require_once '../core/head.php';
require_once '../core/cauhinh.php';

// Xác thực người dùng, nếu chưa đăng nhập, chuyển hướng đến trang đăng nhập
if ($_login === null) {
    echo '<script>window.location.href = "../dang-nhap.php";</script>';
    exit;
}

// Chỉ cho phép tài khoản có quyền admin = 1 truy cập vào trang quản trị
if ($_admin != 1) {
    echo '<script>window.location.href="/"</script>';
    exit;
}

// Lấy các giá trị cần thiết từ URL (nếu có)
$_active = $_active ?? null;
$_tcoin = $_tcoin ?? 0;
$serverIP = $serverIP ?? '';
$serverPort = $serverPort ?? '';

// Đếm số lượng tài khoản trong cơ sở dữ liệu
$id_user_query = "SELECT COUNT(id) AS id FROM account";
$statement = $conn->prepare($id_user_query);
$statement->execute();
$row = $statement->fetch(PDO::FETCH_ASSOC);
$id = $row['id'];

// Đếm số lượng tài khoản bị cấm (ban) và số lượng tài khoản đang hoạt động (active)
$ban_count_query = "SELECT COUNT(*) AS ban FROM account WHERE ban = 1";
$active_count_query = "SELECT COUNT(*) AS active FROM account WHERE active = 1";

$result = $conn->prepare($ban_count_query);
$result->execute();
$row_ban = $result->fetch(PDO::FETCH_ASSOC); // Use fetch() with PDO::FETCH_ASSOC

$result = $conn->prepare($active_count_query);
$result->execute();
$row_active = $result->fetch(PDO::FETCH_ASSOC); // Use fetch() with PDO::FETCH_ASSOC

$_tongban = $row_ban["ban"];
$_tongactive = $row_active["active"];

// Đếm số lượng tài khoản có giá trị recaf khác null
$recaf_count_query = "SELECT COUNT(*) AS recaf FROM account WHERE recaf IS NOT NULL";
$result = $conn->prepare($recaf_count_query);
$result->execute();
$row = $result->fetch(PDO::FETCH_ASSOC);
$_recaf = $row['recaf'];

function get_user_ip()
{
    if (!empty($_SERVER['HTTP_X_FORWARDED_FOR'])) {
        $addr = explode(",", $_SERVER['HTTP_X_FORWARDED_FOR']);
        return trim($addr[0]);
    } else {
        return $_SERVER['REMOTE_ADDR'];
    }
}
?>

                    <div class="ant-col ant-col-xs-24 ant-col-sm-24 ant-col-md-24">
                        <div class="page-layout-body">
                            <!-- load view -->
                            <div class="ant-row">
    <div class="row">
        <div class="col">
            <a href="/" style="color: black" class="ant-col ant-col-24 home_page_bodyTitleList__UdhN_">Quay lại diễn đàn</a>
        </div>
    </div>
</div>
<div class="ant-col ant-col-24">
    <div class="ant-list ant-list-split">
        <div class="ant-spin-nested-loading">
            <div class="ant-spin-container">
                <ul class="ant-list-items">
<div id="data_news">
<?php if ($_admin != 1) { ?>
    <p>Bạn không phải là admin! Không thể sài được chứ năng này</p>
<?php } else { ?>
    <div class="container pt-5 pb-5" id="pageHeader">
        <h4>MENU ADMIN</h4>
        <div class="row pb-2 pt-2">
            <div class="col-lg-6 text-left">
                <p>Tổng tài khoản:
                    <?php echo $id; ?>
                </p>
                <p>Thành viên:
                    <?php echo $_tongactive; ?>
                </p>
                <p>Tài khoản vi phạm:
                    <?php echo $_tongban; ?>
                </p>
                <!-- Trong phần HTML-->
                <div id="profile-update"></div>

                <script>
                    // Sử dụng JavaScript và AJAX để gửi yêu cầu đến máy chủ và cập nhật nội dung của vùng hiển thị kết quả
                    function updateRemainingTime() {
                        var xhttp = new XMLHttpRequest();
                        xhttp.onreadystatechange = function () {
                            if (this.readyState === 4 && this.status === 200) {
                                // Nhận phản hồi từ máy chủ và cập nhật nội dung của vùng hiển thị kết quả
                                document.getElementById("profile-update").innerHTML = this.responseText;
                            }
                        };
                        xhttp.open("GET", "../api/cauhinh/api-maychu.php", true); // Thay đổi đường dẫn đến tệp PHP xử lý
                        xhttp.send();
                    }

                    // Tự động cập nhật thời gian mỗi giây
                    setInterval(updateRemainingTime, 100);
                </script>
            </div>
            <div class="col-lg-6">
                <?php
                $month = date('m');
                $query = "SELECT SUM(amount) AS tongtiennap FROM trans_log WHERE status = 1 AND MONTH(date) = :month";
                $statement = $conn->prepare($query);
                $statement->bindParam(':month', $month, PDO::PARAM_INT);
                $statement->execute();
                $row = $statement->fetch(PDO::FETCH_ASSOC);
                $tongtienthangnay = $row['tongtiennap'];

                //$tienthangnay = number_format($tongtienthangnay);

                $query = "SELECT name, SUM(amount) AS topnap FROM trans_log WHERE status = 1 AND MONTH(date) = :month GROUP BY name ORDER BY topnap DESC LIMIT 3";
                $statement = $conn->prepare($query);
                $statement->bindParam(':month', $month, PDO::PARAM_INT);
                $statement->execute();

                if ($statement->rowCount() > 0) {
                    echo "<p class='mb-2'>Danh Sách Top Nạp Thẻ Tháng:</p>";
                    echo "<ol class='list-unstyled'>";

                    $count = 1; // Biến đếm

                    while ($row = $statement->fetch(PDO::FETCH_ASSOC)) {
                        $name = $row['name'];
                        $topnap = $row['topnap'];

                        $tinhtopnap = number_format($topnap);

                        echo "<li class='mb-1'>TOP $count: $name - Tổng nạp: <span class='amount'>$tinhtopnap VNĐ</span></li> ";

                        $count++; // Tăng biến đếm sau mỗi lần lặp
                    }
                    echo "</ol><hr>";
                    echo "<span class='mb-3'>- Tổng doanh thu tháng này: </span>$tienthangnay VNĐ";
                } else {
                    echo "<p>Không có tài khoản nạp vào tháng này.</p>";
                }
}
?>
        </div>
    </div>
</div>
    <div class="ant-col ant-col-xs-24 ant-col-sm-24 ant-col-md-24">
                                <div class="ant-row ant-row-space-around ant-row-middle header-menu">
                                    <div class="ant-col ant-col-24">
                                        <div class="row ant-space ant-space-horizontal ant-space-align-center space-header-menu d-flex justify-content-center" style="flex-wrap:wrap;margin-bottom:-10px">
                                            <div class="ant-space-item col-6 col-md-3 col-lg-2" style="padding-bottom:10px">
                                                <div>
                                                    <a href="thongtin-maychu.php">
                                                        <button type="button" class="ant-btn ant-btn-default header-menu-item header-menu-item-active w-100">
                                                            <b>THÔNG TIN MÁY CHỦ</b>
                                                        </button>
                                                    </a>
                                                </div>
                                            </div>                                      
                                            <div class="ant-space-item col-6 col-md-3 col-lg-2" style="padding-bottom:10px">
                                                <div>
                                                    <a href="vatpham.php">
                                                        <button type="button" class="ant-btn ant-btn-default header-menu-item header-menu-item-active w-100">
                                                            <b>BUFF VẬT PHẨM</b>
                                                        </button>
                                                    </a>
                                                </div>
                                            </div>
                                            <div class="ant-space-item col-6 col-md-3 col-lg-2" style="padding-bottom:10px">
                                                <div>
                                                    <a href="giatri.php">
                                                        <button type="button" class="ant-btn ant-btn-default header-menu-item w-100 /recharge">
                                                            <b>GT NẠP</b>
                                                        </button>
                                                    </a>
                                                </div>
                                            </div>
                                            <div class="ant-space-item col-6 col-md-3 col-lg-2" style="padding-bottom:10px">
                                                <div>
                                                    <a href="vipham.php">
                                                        <button type="button" class="ant-btn ant-btn-default header-menu-item w-100 /exchange">
                                                            <b>VI PHẠM</b>
                                                        </button>
                                                    </a>
                                                </div>
                                            </div>
                                            <div class="ant-space-item col-6 col-md-3 col-lg-2" style="padding-bottom:10px">
                                                <div>
                                                    <a href="chiso.php">
                                                        <button type="button" class="ant-btn ant-btn-default header-menu-item w-100 /fanpage">
                                                            <b>CỘNG CHỈ SỐ</b>
                                                        </button>
                                                    </a>
                                                </div>
                                            </div>
                                            <div class="ant-space-item col-6 col-md-3 col-lg-2" style="padding-bottom:10px">
                                                <div>
                                                    <a href="congvnd.php">
                                                        <button type="button" class="ant-btn ant-btn-default header-menu-item w-100 /fanpage">
                                                            <b>CỘNG TIỀN</b>
                                                        </button>
                                                    </a>
                                                </div>
                                            </div>
                                            <div class="ant-space-item col-6 col-md-3 col-lg-2" style="padding-bottom:10px">
                                                <div>
                                                    <a href="activetv.php">
                                                        <button type="button" class="ant-btn ant-btn-default header-menu-item w-100 /fanpage">
                                                            <b>KICH HOẠT PLAYER</b>
                                                        </button>
                                                    </a>
                                                </div>
                                            </div>
            </div>
        </div>
    </div>
</div>
                                                    </div>
                        <div id="paging" class="d-flex justify-content-end align-items-center flex-wrap">
                        </div>
                                    </ul>
            </div>
        </div>
    </div>
</div>
</div>                            <!-- end load view -->
                        </div>
                    </div>
<?php include_once '../core/footer.php'; ?>
                        </div>
                    </div>
                </div>
            </div>
        </main>
    </section>
    <!-- Modal -->
    <div class="modal right fade" id="form_login" tabindex="-1" aria-labelledby="exampleModalLabel" aria-hidden="true" data-mdb-backdrop="static" data-mdb-keyboard="true">
        <div class="modal-dialog modal-side modal-bottom-right ">
            <div class="modal-content modal-main">
                <div class="modal-body text-center">
                    <div class="ant-row ant-row-center ant-row-middle">
                        <div class="ant-col ant-col-xs-20 ant-col-xs-offset-1 ant-col-sm-20 ant-col-sm-offset-1 ant-col-md-24" 
                        style="text-align: center;">
                            <img src="/public/images/TW.svg" class="header-logo" style="height:auto; max-height: 120px; max-width: 70%;">
                        </div>
                    </div>
                    <div class="ant-spin-nested-loading">
                        <div class="ant-spin-container">
                            <div class="ant-row ant-row-center ant-row-middle">
                                <div class="ant-col ant-col-xs-20 ant-col-xs-offset-1 ant-col-sm-20 ant-col-sm-offset-1 ant-col-md-24">
                                    <div id="login_form_detail" class="ant-form ant-form-vertical" style="margin-top: 15px;">
                                        <!-- <form id="login_form_detail" action="http://nroreal.me/login" autocomplete="off" class="ant-form ant-form-vertical" style="margin-top: 15px;"> -->
                                        <div class="ant-form-item">
                                            <div class="ant-row ant-form-item-row">
                                                <div class="ant-col ant-form-item-label">
                                                    <label for="username" class="ant-form-item-required" title="Tên đăng nhập">Tên đăng nhập</label>
                                                </div>
                                                <div class="ant-col ant-form-item-control">
                                                    <div class="ant-form-item-control-input">
                                                        <div class="ant-form-item-control-input-content">
                                                            <input placeholder="Tên đăng nhập" id="username" aria-required="true" class="ant-input" type="text" value="" style="border-radius: 4px;">
                                                        </div>
                                                    </div>
                                                </div>
                                            </div>
                                        </div>
                                        <div class="ant-form-item">
                                            <div class="ant-row ant-form-item-row">
                                                <div class="ant-col ant-form-item-label">
                                                    <label for="password" class="ant-form-item-required" title="Mật khẩu">Mật khẩu</label>
                                                </div>
                                                <div class="ant-col ant-form-item-control">
                                                    <div class="ant-form-item-control-input">
                                                        <div class="ant-form-item-control-input-content">
                                                            <span class="ant-input-affix-wrapper ant-input-password">
                                                                <input placeholder="Nhập mật khẩu" id="password" aria-required="true" type="password" class="ant-input">
                                                                <span class="ant-input-suffix">
                                                                    <span role="img" aria-label="eye-invisible" tabindex="-1" class="anticon anticon-eye-invisible ant-input-password-icon">
                                                                        <svg viewBox="64 64 896 896" focusable="false" data-icon="eye-invisible" width="1em" height="1em" fill="currentColor" aria-hidden="true">
                                                                            <path d="M942.2 486.2Q889.47 375.11 816.7 305l-50.88 50.88C807.31 395.53 843.45 447.4 874.7 512 791.5 684.2 673.4 766 512 766q-72.67 0-133.87-22.38L323 798.75Q408 838 512 838q288.3 0 430.2-300.3a60.29 60.29 0 000-51.5zm-63.57-320.64L836 122.88a8 8 0 00-11.32 0L715.31 232.2Q624.86 186 512 186q-288.3 0-430.2 300.3a60.3 60.3 0 000 51.5q56.69 119.4 136.5 191.41L112.48 835a8 8 0 000 11.31L155.17 889a8 8 0 0011.31 0l712.15-712.12a8 8 0 000-11.32zM149.3 512C232.6 339.8 350.7 258 512 258c54.54 0 104.13 9.36 149.12 28.39l-70.3 70.3a176 176 0 00-238.13 238.13l-83.42 83.42C223.1 637.49 183.3 582.28 149.3 512zm246.7 0a112.11 112.11 0 01146.2-106.69L401.31 546.2A112 112 0 01396 512z"></path>
                                                                            <path d="M508 624c-3.46 0-6.87-.16-10.25-.47l-52.82 52.82a176.09 176.09 0 00227.42-227.42l-52.82 52.82c.31 3.38.47 6.79.47 10.25a111.94 111.94 0 01-112 112z"></path>
                                                                        </svg>
                                                                    </span>
                                                                </span>
                                                            </span>
                                                        </div>
                                                    </div>
                                                </div>
                                            </div>
                                        </div>
                                        <div class="ant-form-item login_btnFormLogin__SL9hu">
                                            <div class="ant-row ant-form-item-row">
                                                <div class="ant-col ant-form-item-control">
                                                    <div class="ant-form-item-control-input">
                                                        <div class="ant-form-item-control-input-content">
                                                            <button type="submit" id="submit_login" class="ant-btn ant-btn-primary login_btnFormLoginItem__kXCMP">
                                                                <span>Đăng nhập</span>
                                                            </button>
                                                            <button type="button" class="ant-btn ant-btn-danger login_btnFormLoginItem__kXCMP" data-bs-dismiss="modal">
                                                                <span>Hủy bỏ</span>
                                                            </button>
                                                        </div>
                                                    </div>
                                                </div>
                                            </div>
                                        </div>
                                        <div class="ant-form-item login_btnFormLogin__SL9hu">
                                            <div class="ant-row ant-form-item-row">
                                                <div class="ant-col ant-form-item-control">
                                                    <div class="ant-form-item-control-input">
                                                        <div class="ant-form-item-control-input-content">Bạn chưa có tài khoản?
                                                            <button type="button" id="register_now" class="ant-btn ant-btn-link" style="padding-left: 3px;">
                                                                <span>Đăng ký ngay</span>
                                                            </button>
                                                            <br>
                                                        </div>
                                                    </div>
                                                </div>
                                            </div>
                                        </div>
                                        <!-- </form> -->
                                    </div>
                                    <div id="register_form_detail" class="ant-form ant-form-vertical d-none" style="margin-top: 15px;">
                                        <!-- <form id="register_form_detail" action="http://nroreal.me/register" autocomplete="off" class="ant-form ant-form-vertical d-none" style="margin-top: 15px;"> -->
                                        <div class="ant-form-item">
                                            <div class="ant-row ant-form-item-row">
                                                <div class="ant-col ant-form-item-label">
                                                    <label for="username_register" class="ant-form-item-required" title="Tên đăng nhập">Tên đăng nhập</label>
                                                </div>
                                                <div class="ant-col ant-form-item-control">
                                                    <div class="ant-form-item-control-input">
                                                        <div class="ant-form-item-control-input-content">
                                                            <input placeholder="Tên đăng nhập" id="username_register" aria-required="true" class="ant-input" type="text" value="" style="border-radius: 4px;">
                                                        </div>
                                                    </div>
                                                </div>
                                            </div>
                                        </div>
                                        <div class="ant-form-item">
                                            <div class="ant-row ant-form-item-row">
                                                <div class="ant-col ant-form-item-label">
                                                    <label for="password_register" class="ant-form-item-required" title="Mật khẩu">Mật khẩu</label>
                                                </div>
                                                <div class="ant-col ant-form-item-control">
                                                    <div class="ant-form-item-control-input">
                                                        <div class="ant-form-item-control-input-content">
                                                            <span class="ant-input-affix-wrapper ant-input-password">
                                                                <input placeholder="Nhập mật khẩu" id="password_register" aria-required="true" type="password" class="ant-input">
                                                                <span class="ant-input-suffix">
                                                                    <span role="img" aria-label="eye-invisible" tabindex="-1" class="anticon anticon-eye-invisible ant-input-password-icon">
                                                                        <svg viewBox="64 64 896 896" focusable="false" data-icon="eye-invisible" width="1em" height="1em" fill="currentColor" aria-hidden="true">
                                                                            <path d="M942.2 486.2Q889.47 375.11 816.7 305l-50.88 50.88C807.31 395.53 843.45 447.4 874.7 512 791.5 684.2 673.4 766 512 766q-72.67 0-133.87-22.38L323 798.75Q408 838 512 838q288.3 0 430.2-300.3a60.29 60.29 0 000-51.5zm-63.57-320.64L836 122.88a8 8 0 00-11.32 0L715.31 232.2Q624.86 186 512 186q-288.3 0-430.2 300.3a60.3 60.3 0 000 51.5q56.69 119.4 136.5 191.41L112.48 835a8 8 0 000 11.31L155.17 889a8 8 0 0011.31 0l712.15-712.12a8 8 0 000-11.32zM149.3 512C232.6 339.8 350.7 258 512 258c54.54 0 104.13 9.36 149.12 28.39l-70.3 70.3a176 176 0 00-238.13 238.13l-83.42 83.42C223.1 637.49 183.3 582.28 149.3 512zm246.7 0a112.11 112.11 0 01146.2-106.69L401.31 546.2A112 112 0 01396 512z"></path>
                                                                            <path d="M508 624c-3.46 0-6.87-.16-10.25-.47l-52.82 52.82a176.09 176.09 0 00227.42-227.42l-52.82 52.82c.31 3.38.47 6.79.47 10.25a111.94 111.94 0 01-112 112z"></path>
                                                                        </svg>
                                                                    </span>
                                                                </span>
                                                            </span>
                                                        </div>
                                                    </div>
                                                </div>
                                            </div>
                                        </div>
                                        <div class="ant-form-item">
                                            <div class="ant-row ant-form-item-row">
                                                <div class="ant-col ant-form-item-label">
                                                    <label for="confirm_password" class="ant-form-item-required" title="Mật khẩu">Nhập lại mật khẩu</label>
                                                </div>
                                                <div class="ant-col ant-form-item-control">
                                                    <div class="ant-form-item-control-input">
                                                        <div class="ant-form-item-control-input-content">
                                                            <span class="ant-input-affix-wrapper ant-input-password">
                                                                <input placeholder="Nhập lại mật khẩu" id="confirm_password" aria-required="true" type="password" class="ant-input">
                                                                <span class="ant-input-suffix">
                                                                    <span role="img" aria-label="eye-invisible" tabindex="-1" class="anticon anticon-eye-invisible ant-input-password-icon">
                                                                        <svg viewBox="64 64 896 896" focusable="false" data-icon="eye-invisible" width="1em" height="1em" fill="currentColor" aria-hidden="true">
                                                                            <path d="M942.2 486.2Q889.47 375.11 816.7 305l-50.88 50.88C807.31 395.53 843.45 447.4 874.7 512 791.5 684.2 673.4 766 512 766q-72.67 0-133.87-22.38L323 798.75Q408 838 512 838q288.3 0 430.2-300.3a60.29 60.29 0 000-51.5zm-63.57-320.64L836 122.88a8 8 0 00-11.32 0L715.31 232.2Q624.86 186 512 186q-288.3 0-430.2 300.3a60.3 60.3 0 000 51.5q56.69 119.4 136.5 191.41L112.48 835a8 8 0 000 11.31L155.17 889a8 8 0 0011.31 0l712.15-712.12a8 8 0 000-11.32zM149.3 512C232.6 339.8 350.7 258 512 258c54.54 0 104.13 9.36 149.12 28.39l-70.3 70.3a176 176 0 00-238.13 238.13l-83.42 83.42C223.1 637.49 183.3 582.28 149.3 512zm246.7 0a112.11 112.11 0 01146.2-106.69L401.31 546.2A112 112 0 01396 512z"></path>
                                                                            <path d="M508 624c-3.46 0-6.87-.16-10.25-.47l-52.82 52.82a176.09 176.09 0 00227.42-227.42l-52.82 52.82c.31 3.38.47 6.79.47 10.25a111.94 111.94 0 01-112 112z"></path>
                                                                        </svg>
                                                                    </span>
                                                                </span>
                                                            </span>
                                                        </div>
                                                    </div>
                                                </div>
                                            </div>
                                        </div>
                                        <div class="ant-form-item login_btnFormLogin__SL9hu">
                                            <div class="ant-row ant-form-item-row">
                                                <div class="ant-col ant-form-item-control">
                                                    <div class="ant-form-item-control-input">
                                                        <div class="ant-form-item-control-input-content">
                                                            <button type="submit" id="submit_register" class="ant-btn ant-btn-primary login_btnFormLoginItem__kXCMP">
                                                                <span>Đăng ký</span>
                                                            </button>
                                                            <button type="button" class="ant-btn ant-btn-danger login_btnFormLoginItem__kXCMP" data-bs-dismiss="modal">
                                                                <span>Hủy bỏ</span>
                                                            </button>
                                                        </div>
                                                    </div>
                                                </div>
                                            </div>
                                        </div>
                                        <div class="ant-form-item login_btnFormLogin__SL9hu">
                                            <div class="ant-row ant-form-item-row">
                                                <div class="ant-col ant-form-item-control">
                                                    <div class="ant-form-item-control-input">
                                                        <div class="ant-form-item-control-input-content">Bạn chưa có tài khoản?
                                                            <button type="button" id="login_now" class="ant-btn ant-btn-link" style="padding-left: 3px;">
                                                                <span>Đăng nhập ngay</span>
                                                            </button>
                                                            <br>
                                                        </div>
                                                    </div>
                                                </div>
                                            </div>
                                        </div>
                                        <!-- </form> -->
                                    </div>
                                </div>
                            </div>
                            <div class="ant-row ant-row-center" style="margin-top: -20px;">
                                <div class="ant-col ant-col-20 ant-col-offset-1"></div>
                            </div>
                        </div>
                    </div>
                </div>
            </div>
        </div>
    </div>
</body>
</html>
<div class="d-none bg-handle-main" id="waiting_handle"></div>
<!-- <script src="http://nrotwitch.me/public/dist/js/bootstrap.min.js"></script> -->
<script>
    function show_background() {
        $("#waiting_handle").removeClass("d-none");
    }
    function hide_background() {
        $("#waiting_handle").addClass("d-none");
    }
    $(document).ready(function() {
        // $("#modal_main").modal('show');
        $("#show_form_login").click(function() {
            $('#form_login').modal('show');
            $("#login_form_detail").removeClass("d-none");
            $("#register_form_detail").addClass("d-none");
        });
        $("#show_form_register").click(function() {
            $('#form_login').modal('show');
            $("#login_form_detail").addClass("d-none");
            $("#register_form_detail").removeClass("d-none");
        });
        $(".ant-input-suffix").click(function() {
            $(this).siblings().prop("type", $(this).siblings().attr("type") == "text" ? "password" : "text");
        });
        $("#register_now").click(function() {
            $("#login_form_detail").addClass("d-none");
            $("#register_form_detail").removeClass("d-none");
        });
        $("#login_now").click(function() {
            $("#login_form_detail").removeClass("d-none");
            $("#register_form_detail").addClass("d-none");
        });
        $("#username, #password").keypress(function(event) {
            var keycode = (event.keyCode ? event.keyCode : event.which);
            if (keycode == '13') {
                login();
            }
        });
        $("#submit_login").click(function() {
            login();
        })
        $("#username_register, #password_register").keypress(function(event) {
            var keycode = (event.keyCode ? event.keyCode : event.which);
            if (keycode == '13') {
                register();
            }
        });
        $("#submit_register").click(function() {
            register();
        })
        // $("#register_now, #register").click(function() {
        //     $("#modal_login").modal("hide");
        //     $("#modal_register").modal("show");
        // })
        // $("#login_now2").click(function() {
        //     $("#modal_login").modal("show");
        //     $("#modal_register").modal("hide");
        // })
        $("#btn_login").click(function() {
            $("#btn_login").text("Đang đăng nhập");
            $("#btn_login").prop('disabled', true);
            const username = $("#username_login").val();
            const password = $("#password_login").val();
            if (!username || !password) {
                $("#error_login").text("Tài khoản hoặc mật khẩu không được bỏ trống");
                $("#error_login").removeClass("d-none");
                $("#btn_login").text("Đăng nhập");
                $("#btn_login").prop('disabled', false);
                return;
            } else {
                $("#error_login").addClass("d-none");
                // $("form_login").submit();
            }
            $.ajax({
                url: 'http://nroreal.me/ajax_login',
                type: 'POST',
                data: {
                    username: username,
                    password: password
                }
            }).done(function(data) {
                if (data == "success") {
                    Swal.fire({
                        icon: 'success',
                        title: "Đăng nhập thành công",
                    });
                } else {
                    $("#error_login").removeClass("d-none");
                    $("#error_login").text(data);
                    $("#btn_login").text("Đăng nhập");
                    $("#btn_login").prop('disabled', false);
                }
            });
        })
        $("#btn_register").click(function() {
            $("#btn_register").text("Đang đăng ký");
            $("#btn_register").prop('disabled', true);
            // $("#success_register").addClass("d-none");
            const username = $("#username_register").val();
            const password = $("#password_register").val();
            const confirm_password = $("#confirm_password").val();
            if (!username || !password || !confirm_password) {
                $("#error_register").text("Tài khoản mật khẩu không được bỏ trống");
                $("#error_register").removeClass("d-none");
                $("#btn_register").text("Đăng ký");
                $("#btn_register").prop('disabled', false);
                return;
            }
            if (username.length < 6 || password.length < 6) {
                $("#error_register").text("Tài khoản, mật khẩu có độ dài từ 6 kí tự");
                $("#error_register").removeClass("d-none");
                $("#btn_register").text("Đăng ký");
                $("#btn_register").prop('disabled', false);
                return;
            }
            if (password != confirm_password) {
                $("#error_register").text("Mật khẩu xác nhận không trùng khớp");
                $("#error_register").removeClass("d-none");
                $("#btn_register").text("Đăng ký");
                $("#btn_register").prop('disabled', false);
                return;
            }
            $("#error_register").addClass("d-none");
            $.ajax({
                url: 'http://nroreal.me/ajax_register',
                type: 'POST',
                data: {
                    username: username,
                    password: password,
                    confirm_password: confirm_password
                }
            }).done(function(data) {
                if (data == "success") {
                    $("#error_register").addClass("d-none");
                    $("#username_register").val("");
                    $("#password_register").val("");
                    $("#confirm_password").val("");
                    Swal.fire({
                        icon: 'success',
                        title: "Đăng ký tài khoản thành công",
                    });
                } else {
                    $("#error_register").removeClass("d-none");
                    $("#error_register").text(data);
                }
                $("#btn_register").text("Đăng ký");
                $("#btn_register").prop('disabled', false);
            });
        })
                    })
    function login() {
        Notiflix.Loading.hourglass();
        $("#submit_login").text("Đang thực hiện");
        $("#submit_login").prop('disabled', true);
        const username = $("#username").val();
        const password = $("#password").val();
        if (!username || !password) {
            Swal.fire({
                icon: 'error',
                title: 'Điền đầy đủ tài khoản và mật khẩu',
            })
            $("#submit_login").text("Đăng nhập");
            $("#submit_login").prop('disabled', false);
            Notiflix.Loading.remove();
            return;
        }
        $.ajax({
            url: 'http://nroreal.me/login',
            type: 'POST',
            data: {
                username: username,
                password: password
            }
        }).done(function(data) {
            Notiflix.Loading.remove();
            if (data == "success") {
            } else {
                Swal.fire({
                    icon: 'error',
                    title: data,
                });
            }
            $("#submit_login").text("Đăng nhập");
            $("#submit_login").prop('disabled', false);
        });
    }
    function register() {
        Notiflix.Loading.hourglass();
        $("#submit_register").text("Đang thực hiện");
        $("#submit_register").prop('disabled', true);
        const username = $("#username_register").val();
        const password = $("#password_register").val();
        const confirm_password = $("#confirm_password").val();
        if (!username || !password || !confirm_password) {
            Swal.fire({
                icon: 'error',
                title: "Điền đầy đủ tài khoản và mật khẩu",
            });
            $("#submit_register").text("Đăng ký");
            $("#submit_register").prop('disabled', false);
            Notiflix.Loading.remove();
            return;
        }
        if (username.length < 6 || password.length < 6) {
            Swal.fire({
                icon: 'error',
                title: "Độ dài tài khoản và mật khẩu ít nhất 6 kí tự",
            });
            $("#submit_register").text("Đăng ký");
            $("#submit_register").prop('disabled', false);
            Notiflix.Loading.remove();
            return;
        }
        if (password != confirm_password) {
            Swal.fire({
                icon: 'error',
                title: "Mật khẩu không trùng khớp",
            });
            $("#submit_register").text("Đăng ký");
            $("#submit_register").prop('disabled', false);
            Notiflix.Loading.remove();
            return;
        }
        $.ajax({
            url: 'http://nroreal.me/register',
            type: 'POST',
            data: {
                username: username,
                password: password,
                confirm_password: confirm_password
            }
        }).done(function(data) {
            Notiflix.Loading.remove();
            if (data == "success") {
                $("#username_register").val("");
                $("#password_register").val("");
                $("#confirm_password").val("");
                Swal.fire({
                    icon: 'success',
                    title: 'Đăng ký thành công',
                })
                Notiflix.Loading.hourglass();
                $.ajax({
                    url: 'http://nroreal.me/login',
                    type: 'POST',
                    data: {
                        username: username,
                        password: password
                    }
                }).done(function(data) {
                    Notiflix.Loading.remove();
                    if (data == "success") {
                    } else {
                        Swal.fire({
                            icon: 'error',
                            title: data,
                        });
                    }
                    $("#submit_login").text("Đăng nhập");
                    $("#submit_login").prop('disabled', false);
                });
            } else {
                Swal.fire({
                    icon: 'error',
                    title: data,
                });
            }
            $("#submit_register").text("Đăng ký");
            $("#submit_register").prop('disabled', false);
        });
    }
</script>