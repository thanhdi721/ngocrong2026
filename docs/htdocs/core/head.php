<?php
require_once 'cauhinh.php';
require_once 'set.php';
require_once 'connect.php';
// session_start(); // Khởi động session
// Logo / favicon lấy từ core/cauhinh.php, không tra bảng `adminpanel` nữa:
// hai biến $logo và $domain lấy từ đó trước giờ KHÔNG hề được dùng ở đâu, mà câu
// truy vấn lại làm chết cả trang nếu bảng thiếu.
?>
<!DOCTYPE html>
<html lang="en">
<head>
	<meta http-equiv="Content-Type" content="text/html; charset=UTF-8"/>
    <title><?= htmlspecialchars($_title) ?></title>
	<link rel="canonical" href="<?= htmlspecialchars($_domain) ?>" />
    <meta name="viewport" content="width=device-width, initial-scale=1">
    <meta property="og:type" content="website" />
    <meta property="og:url" content="<?= htmlspecialchars($_domain) ?>" />
    <meta property="og:title" content="<?= htmlspecialchars($_title) ?>" />
    <meta property="og:description" content="<?= htmlspecialchars($_tenmaychu) ?> — máy chủ Ngọc Rồng Online nhập vai trực tuyến trên máy tính và điện thoại." />
    <meta property="og:image" content="" />
    <link rel="shortcut icon" href="<?= htmlspecialchars($_favicon) ?>">
    <link rel="apple-touch-icon" href="/image/favicon-192.png">
    <meta name="description" content="<?= htmlspecialchars($_tenmaychu) ?> — máy chủ Ngọc Rồng Online nhập vai trực tuyến trên máy tính và điện thoại.">
    <meta name="keywords" content="ngoc rong mobile, game ngoc rong, game 7 vien ngoc rong, game bay vien ngoc rong">
    <link rel="stylesheet" href="/public/dist/css/style.css">
    <link rel="stylesheet" href="/public/dist/css/main.css" />
    <link rel="stylesheet" href="/public/dist/css/main2.css" />
    <link rel="stylesheet" href="/public/dist/css/bootstrap.min.css">
    <link rel="stylesheet" href="/public/dist/css/all.min.css" />
    <link rel="stylesheet" href="/public/dist/css/sweetalert2.min.css" />
    <link rel="stylesheet" href="/public/dist/css/notiflix-3.2.6.min.css" />
    <!-- <script src="http://nroblue.fun/public/dist/js/bootstrap.min.js"></script> -->
    <script src="/public/dist/js/bootstrap.bundle.min.js"></script>
    <script src="/public/dist/js/jquery-3.6.0.min.js"></script>
    <script src="/public/dist/js/sweetalert2.min.js"></script>
    <script src="/public/dist/js/notiflix-3.2.6.min.js"></script>  
</head>
<body>
	<iframe src="music.php" style="display: none;" allow="autoplay"></iframe>
    <section class="ant-layout page-layout-color body-bg">
	<div class="clouds">
	<div class="cloud"></div>
	<div class="cloud"></div>
	</div>
        <main class="ant-layout-content page-body page-layout-color">
            <div class="page-layout-content">
                <div class="ant-row ant-row-space-around">
                    <div class="ant-col page-layout-header ant-col-xs-24 ant-col-sm-24 ant-col-md-24">
                        <div class="page-layout-header-content">
                            <a href="/">
                                <img src="<?= htmlspecialchars($_logo) ?>"
                                     srcset="<?= htmlspecialchars($_logo) ?> 1x, <?= htmlspecialchars($_logo_2x) ?> 2x"
                                     alt="<?= htmlspecialchars($_tenmaychu) ?>" class="header-logo"
                                     style="display:block;margin-left:auto;margin-right:auto;max-height:150px;max-width:340px;width:100%">

                                <!-- style="max-height: 120px; max-width: 70%" / -->
                            </a>
                            <div>
                   <?php
    if ($_login === null) {
        ?>
        <div class="container color-main2 pb-2">
            <div class="text-center">
                <div class="row">
                    <div class="col pr-0">
					<a type="button" href="/" class="ant-btn ant-btn-default header-btn-login mt-3 me-2">
                                        <span>Trang Chủ</span>
                                    </a>
                   <a type="button" href="/dang-nhap.php" class="ant-btn ant-btn-default header-btn-login mt-3 me-2">
                                        <span>Đăng Nhập</span>
                                    </a>
                    
                   <a type="button" href="/dang-ky.php" class="ant-btn ant-btn-default header-btn-login mt-3">
                                        <span>Đăng Ký</span>
                                    </a>
                </div>
            </div>
        </div>
    <?php } else {
        if ($_admin == 1) { // Kiểm tra quyền truy cập
            ?>
            <div class="container color-main2 pb-2">
                <div class="text-center">
                    <div class="row">
                    </div>
                </div>
            </div>
            <div class="container color-main pt-3 pb-4">
                <div class="text-center">
                    <div id="header-update"></div>
                
<div class="row ant-space ant-space-horizontal ant-space-align-center space-header-menu d-flex justify-content-center" style="flex-wrap:wrap;margin-bottom:-10px">
<div class="ant-space-item col-36 col-md-3 col-lg-2" style="padding-bottom:10px"><div>
<a href="../admincp"><button type="button" class="ant-btn ant-btn-default header-menu-item header-menu-item-active w-100">
<i class="fa fa-cog fa-spin fa-1x fa-fw"></i> <b>Cpanel</b></button></a></div></div><?php
/* Nút menu dựng bằng PHP cho gọn — trước đây mỗi nút là 9 dòng HTML chép tay.
   Link để RỖNG thì nút không hiện (dùng cho Fanpage/Zalo… trong core/cauhinh.php). */
if (!function_exists('nut_menu')) {
    function nut_menu($href, $nhan, $moi_tab = false)
    {
        if ($href === '' || $href === null) { return; }
        $tab = $moi_tab ? ' target="_blank" rel="noopener"' : '';
        echo '<div class="ant-space-item col-6 col-md-3 col-lg-2" style="padding-bottom:10px"><div>'
           . '<a href="' . htmlspecialchars($href, ENT_QUOTES) . '"' . $tab . '>'
           . '<button type="button" class="ant-btn ant-btn-default header-menu-item header-menu-item-active w-100">'
           . '<b>' . htmlspecialchars($nhan) . '</b></button></a></div></div>';
    }
}
?>
<?php
nut_menu('../profile.php', 'Trang cá nhân');
nut_menu('../bang-xep-hang.php', 'Bảng xếp hạng');
nut_menu('../tinh-nang.php', 'Tính năng');
nut_menu('../boss-roi-do.php', 'Boss rơi đồ');
nut_menu('../doi-mat-khau.php', 'Đổi mật khẩu');
nut_menu('../logout.php', 'Đăng xuất');
?>
                    <script>
                        function updateRemainingTime() {
                            fetch('../api/cauhinh/api-head.php')
                                .then(response => response.text())
                                .then(data => {
                                    document.getElementById("header-update").innerHTML = data;
                                })
                                .catch(error => console.error(error));
                        }

                        setInterval(updateRemainingTime, 500); // Cập nhật mỗi giây (1000ms)
                    </script>
                                                        
                </div>
            </div>
          
            <?php
        } else { ?>
            <div class="container color-main2 pb-2">
                <div class="text-center">
                    <div class="row">
                    
                    </div>
                </div>
            </div>
            <div class="container color-main pt-3 pb-4">
                <div class="text-center">
                    <!-- Trong phần HTML-->
                    <div id="header-update"></div>

                    <script>
                        // Sử dụng JavaScript và AJAX để gửi yêu cầu đến máy chủ và cập nhật nội dung của vùng hiển thị kết quả
                        function updateRemainingTime() {
                            var xhttp = new XMLHttpRequest();
                            xhttp.onreadystatechange = function () {
                                if (this.readyState === 4 && this.status === 200) {
                                    // Nhận phản hồi từ máy chủ và cập nhật nội dung của vùng hiển thị kết quả
                                    document.getElementById("header-update").innerHTML = this.responseText;
                                }
                            };
                            xhttp.open("GET", "../api/cauhinh/api-head.php", true); // Thay đổi đường dẫn đến tệp PHP xử lý
                            xhttp.send();
                        }

                        // Tự động cập nhật thời gian mỗi giây
                        setInterval(updateRemainingTime, 100);
                    </script>

                    <div class="row ant-space ant-space-horizontal ant-space-align-center space-header-menu d-flex justify-content-center" style="flex-wrap:wrap;margin-bottom:-10px">
<?php
/* Nút menu dựng bằng PHP cho gọn — trước đây mỗi nút là 9 dòng HTML chép tay.
   Link để RỖNG thì nút không hiện (dùng cho Fanpage/Zalo… trong core/cauhinh.php). */
if (!function_exists('nut_menu')) {
    function nut_menu($href, $nhan, $moi_tab = false)
    {
        if ($href === '' || $href === null) { return; }
        $tab = $moi_tab ? ' target="_blank" rel="noopener"' : '';
        echo '<div class="ant-space-item col-6 col-md-3 col-lg-2" style="padding-bottom:10px"><div>'
           . '<a href="' . htmlspecialchars($href, ENT_QUOTES) . '"' . $tab . '>'
           . '<button type="button" class="ant-btn ant-btn-default header-menu-item header-menu-item-active w-100">'
           . '<b>' . htmlspecialchars($nhan) . '</b></button></a></div></div>';
    }
}
?>
<?php
nut_menu('../profile.php', 'Trang cá nhân');
nut_menu('../bang-xep-hang.php', 'Bảng xếp hạng');
nut_menu('../tinh-nang.php', 'Tính năng');
nut_menu('../boss-roi-do.php', 'Boss rơi đồ');
nut_menu('../doi-mat-khau.php', 'Đổi mật khẩu');
nut_menu('../logout.php', 'Đăng xuất');
?>
                    </div>
                    <div class="row mb-3">
                        <div class="col text-center">
                 </div>
                </div>
            </div>
            <?php
        }
    }
    ?>
                            </div>
                            <div class="ant-col ant-col-xs-24 ant-col-sm-24 ant-col-md-24">
                                <div class="ant-row ant-row-space-around ant-row-middle header-menu">
                                    <div class="ant-col ant-col-24">
                                        <div class="row ant-space ant-space-horizontal ant-space-align-center space-header-menu d-flex justify-content-center" style="flex-wrap:wrap;margin-bottom:-10px">
<?php
/* Nút menu dựng bằng PHP cho gọn — trước đây mỗi nút là 9 dòng HTML chép tay.
   Link để RỖNG thì nút không hiện (dùng cho Fanpage/Zalo… trong core/cauhinh.php). */
if (!function_exists('nut_menu')) {
    function nut_menu($href, $nhan, $moi_tab = false)
    {
        if ($href === '' || $href === null) { return; }
        $tab = $moi_tab ? ' target="_blank" rel="noopener"' : '';
        echo '<div class="ant-space-item col-6 col-md-3 col-lg-2" style="padding-bottom:10px"><div>'
           . '<a href="' . htmlspecialchars($href, ENT_QUOTES) . '"' . $tab . '>'
           . '<button type="button" class="ant-btn ant-btn-default header-menu-item header-menu-item-active w-100">'
           . '<b>' . htmlspecialchars($nhan) . '</b></button></a></div></div>';
    }
}
?>
<?php
nut_menu('../dang-ky.php', 'Đăng ký');
nut_menu('../dang-nhap.php', 'Đăng nhập');
nut_menu('../doi-mat-khau.php', 'Đổi mật khẩu');
nut_menu('../bang-xep-hang.php', 'Bảng xếp hạng');
nut_menu('../tinh-nang.php', 'Tính năng');
nut_menu('../boss-roi-do.php', 'Boss rơi đồ');
// Mấy nút dưới chỉ hiện khi bạn điền link trong core/cauhinh.php.
nut_menu($_link_fanpage,  'Fanpage',      true);
nut_menu($_link_zalo,     'Nhóm Zalo',    true);
nut_menu($_link_telegram, 'Telegram',     true);
nut_menu($_link_discord,  'Discord',      true);
?>
                                        </div>
                                    </div>
                                </div>
                            </div>
                        </div>
                    </div>
                    <div class="marquee-container">
						<div class="marquee-wrapper">
							<div class="marquee-text"><?= htmlspecialchars($_chu_chay) ?></div>
						</div>
					</div>
                    <div class="ant-col ant-col-xs-24 ant-col-sm-24 ant-col-md-24">
                        <div class="ant-row ant-row-space-around ant-row-middle header-menu">
                            <div class="ant-col ant-col-24">
                                <div class="row ant-space ant-space-horizontal ant-space-align-center space-header-menu d-flex justify-content-center" style="flex-wrap:wrap;margin-bottom:-10px">
                                    <div class="ant-space-item col-6 col-md-3 col-lg-2" style="padding-bottom:10px">
                                        <div>
                                            <a href="../download/windows.php">
                                                <button style="height:45px" type="button" class="ant-btn ant-btn-default header-menu-item header-menu-item-active w-100">
                                                    <img src="/public/images/0hrzmer.png" style="width:97px" />
                                                </button>
                                            </a>
                                        </div>
                                    </div>
                                    <div class="ant-space-item col-6 col-md-3 col-lg-2" style="padding-bottom:10px">
                                        <div>
                                            <a href="../download/android.php">
                                                <button style="height:45px" type="button" class="ant-btn ant-btn-default header-menu-item header-menu-item-active w-100">
                                                    <img src="/public/images/RAGk2Dn.png" style="width:97px" />
                                                </button>
                                            </a>
                                        </div>
                                    </div>
                                    <div class="ant-space-item col-6 col-md-3 col-lg-2" style="padding-bottom:10px">
                                        <div>
                                            <a href="../download/iphone.php">
                                                <button style="height:45px" type="button" class="ant-btn ant-btn-default header-menu-item header-menu-item-active w-100">
                                                    <img src="/public/images/XnpBrRa.png" style="width:97px" />
                                                </button>
                                            </a>
                                        </div>
                                    </div>
                                    <div class="ant-space-item col-6 col-md-3 col-lg-2" style="padding-bottom:10px">
										<div>
											<a href="../download/jar.php">
												<button style="height:45px; display: flex; justify-content: center; align-items: center; position: relative;" type="button" class="ant-btn ant-btn-default header-menu-item header-menu-item-active w-100">
												<!--<span style="position: absolute;">Jar</span>-->
													<img src="/public/images/java.png" style="width:97px; " />
												</button>
											</a>
										</div>
									</div>
									<div class="ant-space-item col-6 col-md-3 col-lg-2" style="padding-bottom:10px">
                                        <div>
                                            <a href="../bang-xep-hang.php">
                                                <button style="height:45px" type="button" class="ant-btn ant-btn-default header-menu-item header-menu-item-active w-100">
                                                    <b>Bảng Xếp Hạng</b>
                                                </button>
                                            </a>
                                        </div>
                                    </div>
                                    <!-- <div class="ant-space-item" style="padding-bottom:10px">
                                        <div>
                                            <a href="">
                                                <button style="height:45px" type="button" class="ant-btn ant-btn-default header-menu-item header-menu-item-active">
                                                    <img src="http://nroreal.me/public/images/qEPYtv1.png" style="width:97px" />
                                                </button>
                                            </a>
                                        </div>
                                    </div> -->
                                </div>
                            </div>
                        </div>
                    </div>
