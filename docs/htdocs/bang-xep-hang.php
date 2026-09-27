<?php
include_once 'core/head.php';
?>
<style>
.dragonball-group {
    gap: 10px;
    justify-content: center;
    flex-wrap: wrap;
    display: flex;
}

.dragonball-btn {
    background: linear-gradient(to bottom, #FFA500, #FF8C00);
    border: 2px solid #FFD700;
    color: white;
    padding: 8px 20px;
    font-size: 14px;
    font-weight: bold;
    border-radius: 12px;
    text-shadow: 1px 1px 1px #000;
    box-shadow: 0 4px 0 #cc8400;
    transition: all 0.2s ease-in-out;
    min-width: 120px;
    max-width: 130px;
    height: 42px;
}

.dragonball-btn:hover {
    transform: scale(1.05);
    box-shadow: 0 5px 0 #aa6b00;
}

.dragonball-btn.active {
    background: linear-gradient(to bottom, #FF4500, #FF6347);
    box-shadow: 0 5px 0 #8B0000;
    border-color: #FFD700;
}


.title-top-rank {
    font-size: 2.2rem;
    font-weight: 900;
    text-transform: uppercase;
    letter-spacing: 3px;
    padding: 25px 40px;
    border-radius: 20px;
    display: inline-block;
    color: #fff;
    background: linear-gradient(90deg, #ff6b00, #ffcc00, #ff6b00);
    background-size: 300% 300%;
    border: 3px solid #fff;
    box-shadow:
        0 0 15px rgba(255, 106, 0, 0.8),
        0 0 30px rgba(255, 193, 7, 0.6),
        inset 0 0 10px rgba(255, 255, 255, 0.2);
    animation: glowMove 6s ease infinite, pulseDragon 3s ease-in-out infinite;
}

/* Gradient chuyển động ánh sáng */
@keyframes glowMove {
    0% { background-position: 0% 50%; }
    50% { background-position: 100% 50%; }
    100% { background-position: 0% 50%; }
}

/* Nhấp nháy nhẹ aura phát sáng */
@keyframes pulseDragon {
    0%, 100% {
        box-shadow:
            0 0 15px rgba(255, 106, 0, 0.8),
            0 0 30px rgba(255, 193, 7, 0.6),
            inset 0 0 10px rgba(255, 255, 255, 0.2);
    }
    50% {
        box-shadow:
            0 0 25px rgba(255, 165, 0, 0.9),
            0 0 45px rgba(255, 220, 0, 0.8),
            inset 0 0 12px rgba(255, 255, 255, 0.3);
    }
}

.thumbnail-box {
    width: 332px;
    height: 751px;
    background: #1a1a1a;
    border-radius: 16px;
    box-shadow: 0 0 20px rgba(255, 215, 0, 0.8);
    overflow: hidden;
    display: flex;
    flex-direction: column;
    align-items: center;
    padding: 15px;
    color: #fff;
    user-select: none;
    transition: transform 0.3s ease;
}

.thumbnail-box:hover {
    transform: scale(1.05);
}

.thumbnail-box img {
    width: 100%;
    height: calc(100% - 50px); /* dành chỗ cho caption */
    object-fit: cover;          /* ảnh phủ đầy khung, có thể bị cắt */
    border-radius: 12px;
    margin-bottom: 10px;
}

.caption {
    font-weight: 700;
    font-size: 1.3rem;
    text-align: center;
}


</style>

<div class="card">
    <div class="card-body py-4">
        <div class="text-center mt-4">
            <div class="title-top-rank">BẢNG XẾP HẠNG ĐUA TOP</div>

            <!-- Nhóm nút -->
            <div class="btn-group dragonball-group mt-3">
                <a href="/top/top-suc-manh.php">
                    <button type="button" class="btn dragonball-btn active" id="caothu" data-target="caothu1">Sức Mạnh</button>
                </a>
                <a href="/top/top-nhiem-vu.php">
                    <button type="button" class="btn dragonball-btn" id="nhiemvu" data-target="nhiemvu1">Nhiệm Vụ</button>
                </a>
            </div>
			
			<div class="image-thumbnails d-flex justify-content-center gap-3 mt-4">
			<div class="thumbnail-box text-center">
				<img src="/image/berus.png" alt="Sức Mạnh" />
				<div class="caption">Sức Mạnh</div>
			</div>
			<div class="thumbnail-box text-center">
				<img src="/image/bu.png" alt="Top Nạp" />
				<div class="caption">Top Nạp</div>
			</div>
			<div class="thumbnail-box text-center">
				<img src="/image/gohan.png" alt="Nhiệm Vụ" />
				<div class="caption">Nhiệm Vụ</div>
			</div>
			</div>

            <p class="mt-3 font-weight-bold">
                Chào mừng đến với <strong>Bảng Xếp Hạng Cao Thủ</strong> – nơi vinh danh những chiến binh vĩ đại nhất hành tinh!<br>
                Bảng xếp hạng được cập nhật liên tục dựa trên <strong>sức mạnh</strong> và <strong>nhiệm vụ hoàn thành</strong>, phản ánh chân thực sức mạnh và sự nỗ lực của từng người chơi trong hành trình chinh phục vũ trụ Ngọc Rồng.
            </p>
        </div>
    </div>
</div>

<script>
    // Bổ sung mã JavaScript để quản lý sự kiện và hiển thị bảng tương ứng
    document.addEventListener('DOMContentLoaded', function() {
        var buttons = document.querySelectorAll('.btn-group button');
        buttons.forEach(function(button) {
            button.addEventListener('click', function() {
                var target = this.getAttribute('data-target');
                document.querySelectorAll('.table').forEach(function(table) {
                    table.style.display = 'none'; // Ẩn tất cả các bảng
                });
                document.getElementById(target).style.display = 'table'; // Hiển thị bảng được chọn
            });
        });

        // Hiển thị bảng đầu tiên theo mặc định
    });
// Lắng nghe sự kiện nhấp vào nút
    document.querySelectorAll('.btn-group button').forEach(function (button) {
        button.addEventListener('click', function () {
            // Đặt lại màu nền cho tất cả các nút
            document.querySelectorAll('.btn-group button').forEach(function (btn) {
                btn.classList.remove('active');
                btn.style.backgroundColor = "#f3e6f5"; // Màu nền mặc định
                btn.style.color = "#9e09e8"; // Màu chữ mặc định
            });

            // Đổi màu cho nút đang được nhấn
            this.classList.add('active');
            this.style.backgroundColor = "#ffcc00"; // Màu nền khi nhấn
            this.style.color = "#fff"; // Màu chữ khi nhấn
        });

        // Thêm sự kiện rê chuột để đổi màu nền
        button.addEventListener('mouseover', function () {
            if (!this.classList.contains('active')) {
                this.style.backgroundColor = "#ffcc00"; // Màu nền khi rê chuột
                this.style.color = "#fff"; // Màu chữ khi rê chuột
            }
        });

        // Khôi phục màu nền khi không rê chuột
        button.addEventListener('mouseout', function () {
            if (!this.classList.contains('active')) {
                this.style.backgroundColor = "#f3e6f5"; // Màu nền mặc định
                this.style.color = "#9e09e8"; // Màu chữ mặc định
            }
        });
    });
</script>
<?php
include_once('core/footer.php');
?>