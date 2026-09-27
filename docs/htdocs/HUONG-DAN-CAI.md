# Cài web lên XAMPP

Web này đọc **thẳng CSDL của server game** (`team2026`). Không có CSDL riêng.

---

## 1. Chép thư mục

Chép cả thư mục `htdocs` này đè lên `C:\xampp\htdocs` trên máy server.

Nếu muốn giữ `htdocs` cũ thì chép vào `C:\xampp\htdocs\nro` rồi vào bằng
`http://localhost/nro/`.

## 2. Bật XAMPP

Mở XAMPP Control Panel → **Start** cả **Apache** và **MySQL**.

## 3. Sửa đúng MỘT file: `core/cauhinh.php`

```php
$db_name = 'team2026';   // tên CSDL của server game
$db_user = 'root';       // XAMPP mặc định là root
$db_pass = '';           // XAMPP mặc định để trống

$_domain    = 'http://localhost/';   // đổi thành tên miền khi có
$_tenmaychu = 'NRO SkyRain';

$game_ip   = '157.10.44.25';   // IP client game kết nối vào
$game_port = '14445';
```

> `$game_ip` phải **khớp** `DataGame.LINK_IP_PORT` bên server game, hiện là
> `NRO SkyRain:157.10.44.25:14445:0`. Đổi một bên thì đổi cả bên kia.

Mấy nút Fanpage / Zalo / Telegram / Discord **để rỗng là không hiện**. Muốn hiện thì
điền link vào `$_link_fanpage`, `$_link_zalo`…

## 4. Vào thử

`http://localhost/` — nếu ra trang chủ là xong.

---

## Web có những trang gì

| Trang | Việc |
|---|---|
| `index.php` | Trang chủ |
| `dang-ky.php` · `dang-nhap.php` | Đăng ký / đăng nhập |
| `doi-mat-khau.php` | Đổi mật khẩu |
| `forgot-password.php` | Quên mật khẩu (cần cấu hình SMTP, xem dưới) |
| `bang-xep-hang.php` | Xếp hạng sức mạnh / nhiệm vụ |
| **`tinh-nang.php`** | **Danh sách tính năng máy chủ** |
| **`boss-roi-do.php`** | **Boss nào rơi đồ gì** |
| `profile.php` | Trang cá nhân |
| `download/` | Tải game |
| `admincp/` | Trang quản trị (cần tài khoản có `account.admin = 1`) |

---

## Sửa nội dung

### Tính năng máy chủ
Sửa file **`data/tinh_nang.php`**. Đó là một mảng PHP thuần, thêm/xoá mục là trang tự đổi
theo, không phải đụng HTML. Đặt `'moi' => true` là mục đó có nhãn **MỚI**.

### Bảng rơi đồ boss
Bảng rơi khai báo trong **`tools/sinh-boss-drop.php`**. Sửa xong chạy:

```
C:\xampp\php\php.exe tools\sinh-boss-drop.php
```

Nó tra tên vật phẩm thật từ bảng `item_template` rồi ghi ra `data/boss_drop.php`.

**Phải chạy lại** sau khi: sửa bảng rơi đồ trong cpanel của server game, hoặc thêm boss /
vật phẩm mới.

### Số tài khoản tối đa cho một IP
```php
$_max_acc_moi_ip = 5;   // để 0 là bỏ chặn
```

> **Coi chừng khi chạy sau Cloudflare / nginx proxy:** `$_SERVER['REMOTE_ADDR']` lúc đó là IP
> của *cái proxy*, không phải của người chơi — cả máy chủ dùng chung một IP nên chỉ đăng ký
> được đúng 5 tài khoản rồi tắc. Gặp vậy thì để **0**, hoặc sửa `$ip_address` trong
> `dang-ky.php` đọc `HTTP_CF_CONNECTING_IP` / `X-Forwarded-For`.

### Logo
Ba dòng trong `core/cauhinh.php`:

```php
$_logo    = '/image/logo-skyrain.png';      // logo ngang đầu trang, 600×198
$_logo_2x = '/image/logo-skyrain@2x.png';   // bản nét gấp đôi cho màn retina
$_favicon = '/image/favicon-64.png';        // ảnh nhỏ trên tab trình duyệt
```

Ảnh gốc `docs/logo600x200.png` là **3584 px / 7 MB** — đã thu về cỡ web (600 px / 242 KB).
Đừng trỏ thẳng vào ảnh gốc: trang nào cũng sẽ phải tải thêm 7 MB.

Đổi logo mới thì thay file trong `image/` rồi sửa ba dòng trên.

### Tin trang chủ
Sửa mảng `$TIN_TUC` ở đầu `index.php`.

---

## Quên mật khẩu (tuỳ chọn)

Mặc định **TẮT**. Muốn bật thì điền vào `core/cauhinh.php` mục 7:

```php
$smtp_user = 'mail-cua-ban@gmail.com';
$smtp_pass = 'abcd efgh ijkl mnop';   // "mật khẩu ứng dụng" 16 ký tự của Gmail
```

Là **mật khẩu ứng dụng**, không phải mật khẩu Gmail. Lấy ở
Google Account → Bảo mật → Xác minh 2 bước → Mật khẩu ứng dụng.

---

## Đã bỏ những gì

Theo yêu cầu, **toàn bộ phần nạp tiền đã gỡ**: nạp thẻ, nạp ATM, MBBank, Momo, đổi thỏi
vàng, lịch sử giao dịch, mở thành viên, top nạp. Kèm theo đó là mọi thông tin của chủ web
cũ: số tài khoản ngân hàng, số điện thoại Momo, tên chủ tài khoản, token MBBank, link
nhóm Zalo, link Fanpage, mật khẩu ứng dụng Gmail.

Lý do gỡ hẳn chứ không giữ khung: mấy trang đó cần 5 bảng CSDL mà `team2026` **không có**
(`the`, `trans_log`, `cpanel`, `mbbank`, `atm_lichsu`), và đều phải cắm API ngân hàng
riêng mới chạy được.

File cũ **không xoá hẳn** mà dồn vào thư mục **`_da_go/`**. Chạy ổn rồi thì xoá thư mục đó
đi cho nhẹ. Tin tức cũ của máy chủ trước cũng nằm trong `_da_go/news/`.

---

## Mấy chỗ khác biệt so với bản web gốc

Web này viết cho CSDL tên `allstar` có cấu trúc khác, đã sửa lại cho khớp `team2026`:

| Bản gốc | `team2026` |
|---|---|
| `account.gmail` | `account.email` |
| `account.cash` | `account.vnd` |
| `account.danap` | `account.tongnap` |
| `account.mkc2` | **không có** — mật khẩu cấp 2 của web đã bỏ |

Thêm nữa: `account.email`, `newpass`, `token`, `xsrf_token` là `NOT NULL` và không có giá
trị mặc định, nên `dang-ky.php` phải điền sẵn — thiếu là lỗi `1364` và **không đăng ký
được**.

---

## Lưu ý bảo mật

- **Mật khẩu lưu dạng chữ thường, không mã hoá.** Đây là cố ý: server game so sánh
  `select * from account where username = ? and password = ?`, mã hoá là hỏng đăng nhập game.
- `admincp/` chỉ chặn bằng `account.admin = 1`. Nên đổi mật khẩu MySQL `root` và không mở
  cổng 3306 ra ngoài.
- Đã xoá `phpinfo.php` — file đó lộ toàn bộ cấu hình máy chủ.
