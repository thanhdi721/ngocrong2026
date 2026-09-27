<?php
require_once '../core/set.php';
require_once '../core/connect.php';
require_once '../core/head.php';
require_once '../core/cauhinh.php';

if ($_login === null) {
    echo '<script>window.location.href = "../dang-nhap.php";</script>';
    exit;
}

// chỉ cho phép tài khoản có admin = 1 truy cập
if ($_admin != 1) {
    echo '<script>window.location.href="/"</script>';
    exit;
}

$_active = $_active ?? null;
$_tcoin = $_tcoin ?? 0;
$serverIP = $serverIP ?? '';
$serverPort = $serverPort ?? '';

$id_user_query = "SELECT COUNT(id) AS id FROM account";
$statement = $conn->prepare($id_user_query);
$statement->execute();
$row = $statement->fetch(PDO::FETCH_ASSOC);
$id = $row['id'];

$result = _select("COUNT(*) as ban", "account", "ban = 1");
$row = _fetch($result);
$_tongban = $row["ban"];

$result2 = _select("COUNT(*) as active", "account", "active = 1");
$row = _fetch($result2);
$_tongactive = $row["active"];

$sql = "SELECT COUNT(*) AS recaf FROM account WHERE recaf IS NOT NULL";
$statement = $conn->prepare($sql);
$statement->execute();
$row = $statement->fetch(PDO::FETCH_ASSOC);
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
            <a href="/admincp" style="color: black" class="ant-col ant-col-24 home_page_bodyTitleList__UdhN_">Quay lại Cpanel</a>
        </div>
    </div>
</div>
<div class="ant-col ant-col-24">
    <div class="ant-list ant-list-split">
        <div class="ant-spin-nested-loading">
            <div class="ant-spin-container">
                <ul class="ant-list-items">
<div id="data_news">
<div class="container pt-5 pb-5">
    <div class="row">
        <div class="col-lg-6 offset-lg-3">
            <h4>VẬT PHẨM</h4><br>
            <?php if ($_admin != 1) { ?>
                <p>Bạn không phải là admin! Không thể sài được chứ năng này</p>
            <?php } else { ?>
                <b class="text text-danger">Phổ Biến Thông Tin: </b><br>
                <b>- Ví Dụ:
                    <br>
                    - ID: 457 (Thỏi Vàng)
                    <br>
                    - Số Lượng: 1 (Đây Là Số Lượng Vật Phẩm)
                    <br>
                    - Chỉ Số: Tấn Công (Chọn Chỉ Số Bất Kì)
                    <br>
                    - Phần Trăm Chỉ Số: 10 (Đây Là 10% Chỉ Số)
                    <br>
                    <br>
                    <br>
                    <?php

                    $_alert = '';

                    // Xử lý dữ liệu form khi submit
                    if ($_SERVER["REQUEST_METHOD"] == "POST") {
                        // Lấy giá trị từ form
                        $name = $_POST["name"];
                        $sucmanh = isset($_POST["sucmanh"]) ? intval($_POST["sucmanh"]) : 0;
                        $tiemnang = isset($_POST["tiemnang"]) ? intval($_POST["tiemnang"]) : 0;
                        $hp = isset($_POST["hp"]) ? intval($_POST["hp"]) : 0;
                        $mp = isset($_POST["mp"]) ? intval($_POST["mp"]) : 0;
                        $sdg = isset($_POST["sdg"]) ? intval($_POST["sdg"]) : 0;
                        $giapgoc = isset($_POST["giapgoc"]) ? intval($_POST["giapgoc"]) : 0;
                        $chimang = isset($_POST["chimang"]) ? intval($_POST["chimang"]) : 0;

                        // Kiểm tra tính hợp lệ của dữ liệu
                        if (!empty($name)) {

                            // Tìm nhân vật trong CSDL
                            $sql = "SELECT * FROM player WHERE name=:name";
                            $statement = $conn->prepare($sql);
                            $statement->bindParam(':name', $name, PDO::PARAM_STR);
                            $statement->execute();

                            if ($statement->rowCount() > 0) {
                                // Nhân vật tồn tại, cộng chỉ số cho nhân vật
                                $row = $statement->fetch(PDO::FETCH_ASSOC);
                                $data_point = json_decode($row["data_point"], true); // Chuyển đổi JSON thành mảng

                                $select_property = isset($_POST["select-property"]) ? $_POST["select-property"] : "";

                                // cập nhật giá trị mới cho các chỉ số trong mảng $data_point
                                switch ($select_property) {
                                    case 'sucmanh':
                                        $data_point[1] += $sucmanh;
                                        break;
                                    case 'tiemnang':
                                        $data_point[2] += $tiemnang;
                                        break;
                                    case 'hp':
                                        $data_point[5] += $hp;
                                        break;
                                    case 'mp':
                                        $data_point[6] += $mp;
                                        break;
                                    case 'sdg':
                                        $data_point[7] += $sdg;
                                        break;
                                    case 'giapgoc':
                                        $data_point[8] += $giapgoc;
                                        break;
                                    case 'chimang':
                                        $data_point[9] += $chimang;
                                        break;
                                    case 'congtoanbo':
                                        $data_point[1] += isset($_POST["congtoanbo-sucmanh"]) ? intval($_POST["congtoanbo-sucmanh"]) : 0;
                                        $data_point[2] += isset($_POST["congtoanbo-tiemnang"]) ? intval($_POST["congtoanbo-tiemnang"]) : 0;
                                        $data_point[5] += isset($_POST["congtoanbo-hp"]) ? intval($_POST["congtoanbo-hp"]) : 0;
                                        $data_point[6] += isset($_POST["congtoanbo-mp"]) ? intval($_POST["congtoanbo-mp"]) : 0;
                                        $data_point[7] += isset($_POST["congtoanbo-sdg"]) ? intval($_POST["congtoanbo-sdg"]) : 0;
                                        $data_point[8] += isset($_POST["congtoanbo-giapgoc"]) ? intval($_POST["congtoanbo-giapgoc"]) : 0;
                                        $data_point[9] += isset($_POST["congtoanbo-chimang"]) ? intval($_POST["congtoanbo-chimang"]) : 0;
                                        break;
                                    default:
                                        break;
                                }

                                // Chuyển đổi lại thành JSON
                                $updated_data_point = json_encode($data_point);

                                // Cập nhật chỉ số mới vào CSDL
                                $sql = "UPDATE player SET data_point=:data_point WHERE name=:name";
                                $statement_update = $conn->prepare($sql);
                                $statement_update->bindParam(':data_point', $updated_data_point, PDO::PARAM_STR);
                                $statement_update->bindParam(':name', $name, PDO::PARAM_STR);

                                if ($statement_update->execute()) {
                                    $_alert = '<div class="alert alert-success">Cộng chỉ số thành công!</div>';
                                } else {
                                    $_alert = '<div class="alert alert-danger">Lỗi kết nối đến máy chủ!</div>';
                                }

                            } else {
                                // Nhân vật không tồn tại
                                $_alert = '<div class="alert alert-warning">Nhân vật không tồn tại!</div>';
                            }
                        } else {
                            // Tên tài khoản không được để trống
                            $_alert = '<div class="alert alert-warning">Vui lòng nhập tên nhân vật!</div>';
                        }

                        // Ngắt kết nối CSDL
                        $conn = null;
                    }
                    ?>

                    <!-- Hiển thị biến $_alert -->
                    <?php
                    echo $_alert;
                    ?>
                    <form method="POST">
                        <div class="mb-3">
                            <label class="font-weight-bold">Tên Tài Khoản:</label>
                            <input type="name" class="form-control" name="name" id="name" placeholder="Nhập tên tài khoản"
                                required autocomplete="name">
                        </div>
                        <div class="mb-3">
                            <label class="font-weight-bold">Chỉ Số:</label>
                            <select class="form-control" id="select-property" name="select-property">
                                <option value="none">Chọn Chỉ Số</option>
                                <option value="sucmanh">Sức Mạnh</option>
                                <option value="sucmanh">Tiềm Năng</option>
                                <option value="hp">HP</option>
                                <option value="mp">MP</option>
                                <option value="sdg">Sức Đánh Gốc</option>
                                <option value="giapgoc">Giáp Gốc</option>
                                <option value="chimang">Chí Mạng</option>
                                <option value="congtoanbo">Cộng Toàn Bộ Chỉ Số</option>
                            </select>
                        </div>

                        <div class="mb-3" id="sucmanh-input" style="display:none;">
                            <label class="font-weight-bold">Sức Mạnh:</label>
                            <input type="sucmanh" class="form-control" name="sucmanh" id="sucmanh"
                                placeholder="Nhập chỉ số Sức Mạnh" required autocomplete="sucmanh">
                        </div>

                        <div class="mb-3" id="tiemnang-input" style="display:none;">
                            <label class="font-weight-bold">Sức Mạnh:</label>
                            <input type="tiemnang" class="form-control" name="tiemnang" id="tiemnang"
                                placeholder="Nhập tiềm năng cần cộng" required autocomplete="tiemnang">
                        </div>

                        <div class="mb-3" id="hp-input" style="display:none;">
                            <label class="font-weight-bold">HP:</label>
                            <input type="hp" class="form-control" name="hp" id="hp" placeholder="Nhập chỉ số HP" required
                                autocomplete="hp">
                        </div>

                        <div class="mb-3" id="mp-input" style="display:none;">
                            <label class="font-weight-bold">MP:</label>
                            <input type="mp" class="form-control" name="mp" id="mp" placeholder="Nhập chỉ số MP" required
                                autocomplete="mp">
                        </div>

                        <div class="mb-3" id="sdg-input" style="display:none;">
                            <label class="font-weight-bold">Sức Đánh Gốc:</label>
                            <input type="sdg" class="form-control" name="sdg" id="sdg"
                                placeholder="Nhập chỉ số Sức Đánh Gốc" required autocomplete="sdg">
                        </div>

                        <div class="mb-3" id="giapgoc-input" style="display:none;">
                            <label class="font-weight-bold">Giáp Gốc:</label>
                            <input type="giapgoc" class="form-control" name="giapgoc" id="giapgoc"
                                placeholder="Nhập chỉ số Giáp Gốc" required autocomplete="giapgoc">
                        </div>

                        <div class="mb-3" id="chimang-input" style="display:none;">
                            <label class="font-weight-bold">Chí Mạng:</label>
                            <input type="chimang" class="form-control" name="chimang" id="chimang"
                                placeholder="Nhập chỉ số Chí Mạng" required autocomplete="chimang">
                        </div>

                        <div class="mb-3" id="congtoanbo-input" style="display:none;">
                            <!-- Cộng Chỉ Số Sức Mạnh -->
                            <label class="font-weight-bold">Sức Mạnh:</label>
                            <input type="sucmanh" class="form-control" name="congtoanbo-sucmanh" id="congtoanbo-sucmanh"
                                placeholder="Nhập chỉ số Sức Mạnh" required autocomplete="sucmanh">
                            <!-- Cộng Chỉ Số Tiềm Năng -->
                            <label class="font-weight-bold">Tiềm Năng:</label>
                            <input type="tiemnang" class="form-control" name="congtoanbo-tiemnang" id="congtoanbo-tiemnang"
                                placeholder="Nhập chỉ số Tiềm Năng" required autocomplete="tiemnang">
                            <!-- Cộng Chỉ Số HP-->
                            <label class="font-weight-bold">HP:</label>
                            <input type="hp" class="form-control" name="congtoanbo-hp" id="congtoanbo-hp"
                                placeholder="Nhập chỉ số HP" required autocomplete="hp">
                            <!-- Cộng Chỉ Số MP -->
                            <label class="font-weight-bold">MP:</label>
                            <input type="mp" class="form-control" name="congtoanbo-mp" id="congtoanbo-mp"
                                placeholder="Nhập chỉ số MP" required autocomplete="mp">
                            <!-- Cộng Chỉ Số Sức Đánh Gốc -->
                            <label class="font-weight-bold">Sức Đánh Gốc:</label>
                            <input type="sdg" class="form-control" name="congtoanbo-sdg" id="congtoanbo-sdg"
                                placeholder="Nhập chỉ số Sức Đánh Gốc" required autocomplete="sdg">
                            <!-- Cộng Chỉ Số Giáp Gốc -->
                            <label class="font-weight-bold">Giáp Gốc:</label>
                            <input type="giapgoc" class="form-control" name="congtoanbo-giapgoc" id="congtoanbo-giapgoc"
                                placeholder="Nhập chỉ số Giáp Gốc" required autocomplete="giapgoc">
                            <!-- Cộng Chỉ Số Chí Mạng -->
                            <label class="font-weight-bold">Chí Mạng:</label>
                            <input type="chimang" class="form-control" name="congtoanbo-chimang" id="congtoanbo-chimang"
                                placeholder="Nhập chỉ số Chí Mạng" required autocomplete="chimang">
                        </div>
                        <button class="ant-btn ant-btn-default header-menu-item header-menu-item-active w-50" type="submit">Thực Hiện</button>
                    </form>

                    <script>
                        document.getElementById('select-property').addEventListener('change', function () {
                            var value = this.value;
                            switch (value) {
                                case 'none':
                                    hideAllInputs();
                                    break;
                                case 'sucmanh':
                                    hideAllInputs();
                                    showInput('sucmanh-input');
                                    break;
                                case 'tiemnang':
                                    hideAllInputs();
                                    showInput('tiemnang-input');
                                    break;
                                case 'hp':
                                    hideAllInputs();
                                    showInput('hp-input');
                                    break;
                                case 'mp':
                                    hideAllInputs();
                                    showInput('mp-input');
                                    break;
                                case 'sdg':
                                    hideAllInputs();
                                    showInput('sdg-input');
                                    break;
                                case 'giapgoc':
                                    hideAllInputs();
                                    showInput('giapgoc-input');
                                    break;
                                case 'chimang':
                                    hideAllInputs();
                                    showInput('chimang-input');
                                    break;
                                case 'congtoanbo':
                                    hideAllInputs();
                                    showInput('congtoanbo-input');
                                    break;
                                default:
                                    break;
                            }
                        });

                        function hideAllInputs() {
                            var inputs = document.querySelectorAll('#sucmanh-input, #tiemnang-input, #hp-input, #mp-input, #sdg-input, #giapgoc-input, #chimang-input, #congtoanbo-input');
                            inputs.forEach(function (input) {
                                input.style.display = 'none';
                            });
                        }

                        function showInput(inputId) {
                            var input = document.getElementById(inputId);
                            input.style.display = 'block';
                        }
                    </script>
                <?php } ?>
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