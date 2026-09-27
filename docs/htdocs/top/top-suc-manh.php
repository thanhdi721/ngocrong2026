<?php
include_once '../core/head.php';
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

.top-container-nv {
    display: flex;
    justify-content: center;
    align-items: flex-end;
    margin-top: 20px;
    gap: 20px;
}
.top-box-nv {
    text-align: center;
}
.top-box-nv img {
    border-radius: 50%;
}
.top-nv1 img {
    width: 100px;
    height: 100px;
    border-radius: 50%;
    padding: 4px;
    background: linear-gradient(135deg, #ffd700, #fff1a8, #d4af37);
    box-shadow: 0 0 10px rgba(255, 204, 0, 0.9),
                0 0 20px rgba(255, 100, 0, 0.8),
                0 0 40px rgba(255, 0, 0, 0.6);
    animation: pulse-glow 2s infinite ease-in-out;
    margin: 0 auto;
}
.top-nv2 img {
    width: 80px;
    height: 80px;
    border: 4px solid silver;
}
.top-nv3 img {
    width: 80px;
    height: 80px;
    border: 4px solid #cd7f32;
}
.top-rank-label {
    font-weight: bold;
    font-size: 14px;
    color: red;
    margin-top: 5px;
}

</style>

<div class="card">
    <div class="card-body">
        <div class="card-body py-4">
            <div class="text-center mt-4">
			<a href="../bang-xep-hang.php">
    <div class="title-top-rank"> BẢNG XẾP HẠNG ĐUA TOP </div>
	</a>
</div>
            <?php
// Truy vấn top 10 sức mạnh
$query = "SELECT name, gender, items_body, CAST(JSON_EXTRACT(data_point, '$[1]') AS UNSIGNED) AS sm 
          FROM player 
          INNER JOIN account ON account.id = player.account_id 
          WHERE account.ban = 0 
          ORDER BY sm DESC 
          LIMIT 10";
$stmt = $conn->prepare($query);
$stmt->execute();
$players = $stmt->fetchAll(PDO::FETCH_ASSOC);

// Tách top 1, 2, 3
$top3 = array_slice($players, 0, 3);
$top4_10 = array_slice($players, 3);

// Server mới có dưới 3 nhân vật thì $top3[1] / $top3[2] không tồn tại -> vỡ bục podium.
// Lấp bằng ô trống để trang vẫn hiện bình thường.
for ($i = 0; $i < 3; $i++) {
    if (!isset($top3[$i])) {
        $top3[$i] = ['name' => '—', 'sm' => 0, 'gender' => 0, 'items_body' => '[]'];
    }
}
?>

<!-- PHẦN TOP 1-2-3 -->
<div class="top-container-nv">
    <!-- TOP 2 -->
    <div class="top-box-nv top-nv2">
        <img src="../image/avatar1.png"class="avatar-img" alt="Top 2">
        <div class="top-rank-label">TOP 2</div>
        <div><?= htmlspecialchars($top3[1]['name']) ?></div>
        <div><?= number_format($top3[1]['sm'], 0, ',', ',') ?> SM</div>
    </div>

    <!-- TOP 1 -->
    <div class="top-box-nv top-nv1">
        <img src="../image/avatar0.png"class="avatar-img" alt="Top 1">
        <div class="top-rank-label">TOP 1</div>
        <div><strong><?= htmlspecialchars($top3[0]['name']) ?></strong></div>
        <div><?= number_format($top3[0]['sm'], 0, ',', ',') ?> SM</div>
    </div>

    <!-- TOP 3 -->
    <div class="top-box-nv top-nv3">
        <img src="../image/avatar2.png"class="avatar-img" alt="Top 3">
        <div class="top-rank-label">TOP 3</div>
        <div><?= htmlspecialchars($top3[2]['name']) ?></div>
        <div><?= number_format($top3[2]['sm'], 0, ',', ',') ?> SM</div>
    </div>
</div>

<!-- BẢNG TOP 4-10 -->
<table class="table table-striped table-hover table-bordered table-responsive mt-3 text-center" id="caothu1">
    <thead>
        <tr>
            <th>TOP</th>
            <th>Nhân vật</th>
            <th>Sức Mạnh</th>
            <th>Danh Sách Quà Top</th>
        </tr>
    </thead>
    <tbody>
        <?php
        $rank = 4;
        foreach ($top4_10 as $player) {
            $formatted_power = number_format($player['sm'], 0, ',', ',');
            echo "<tr>
                <td class='text-danger fw-bold'>{$rank}</td>
                <td class='text-danger fw-bold'>" . htmlspecialchars($player['name']) . "</td>
                <td class='text-danger fw-bold'>{$formatted_power}</td>
                <td class='text-danger fw-bold'>" . getGiftsForTopSucManh($rank) . "</td>
            </tr>";
            $rank++;
        }

        function getGiftsForTopSucManh($topRank) {
            return "<li>Chưa cập nhật</li><li></li>";
        }
        ?>
    </tbody>
</table>
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
        buttons[0].click();
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
include_once('../core/footer.php');
?>