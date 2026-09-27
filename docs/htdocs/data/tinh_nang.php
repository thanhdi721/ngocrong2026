<?php
/* =====================================================================
 * data/tinh_nang.php — DANH SÁCH TÍNH NĂNG HIỆN CÓ CỦA MÁY CHỦ
 * ---------------------------------------------------------------------
 * File dữ liệu thuần. Thêm / sửa / xoá tính năng thì sửa THẲNG ở đây,
 * trang tinh-nang.php tự hiện theo. Không phải đụng HTML.
 *
 * 'moi' => true  thì hiện nhãn MỚI.
 * ===================================================================== */
return [
    [
        'nhom' => 'Tu tiên & Luyện đan',
        'mo_ta' => 'Tuyến nội dung mới nhất: cày linh thạch, luyện đan, trồng linh thảo.',
        'muc' => [
            ['ten' => 'Map Tu Tiên — Nam Thiên Môn', 'moi' => true, 'chi_tiet' => [
                'Vào qua NPC Tu Tiên ở đảo Kamê.',
                'Quái 20 triệu máu, mỗi đòn của bạn bị chặn ở 10 triệu nên mạnh mấy cũng phải 2 đòn.',
                'Đánh quái KHÔNG nhận kinh nghiệm và KHÔNG nhận vàng.',
                'Vào map là tự bật cờ đen. Chết thì không hồi sinh tại chỗ, bị đưa thẳng về nhà.',
                'Quái rơi Linh Thạch (~288 viên/giờ) và 4 loại linh thảo (~150 lá/giờ).',
            ]],
            ['ten' => 'Shop Tu Tiên', 'moi' => true, 'chi_tiet' => [
                '5 loại đan giá 50 Linh Thạch: sức đánh +20%, HP +30%, KI +30%, chịu đòn −50%, chí mạng +10% — mỗi viên 10 phút.',
                '3 loại ngọc bội giá 200 Linh Thạch, CHỈ đệ tử đeo được.',
                'Tụ Linh Phù giá 5 thỏi vàng: +50% tỉ lệ rơi Linh Thạch trong 30 phút.',
            ]],
            ['ten' => 'Lò Luyện Đan', 'moi' => true, 'chi_tiet' => [
                'NPC hình cái lò ở đảo Kamê. Ghép linh thảo + Địa Hỏa Tinh + đan phương ra đan thượng phẩm.',
                '5 công thức xếp từ dễ tới khó: chịu đòn −60% → chí mạng +15% → KI +45% → HP +45% → sức đánh +30%.',
                'Đan thượng phẩm kéo 20 phút, mạnh hơn đan mua ở tiệm.',
                'Tỉ lệ thành từ 80% xuống 40% theo độ khó. Hỏng thì NỔ LÒ: mất sạch nguyên liệu, được một cục Đan Phế và cả server được cười.',
                'Một quyển đan phương dùng được 3 mẻ.',
                'Có bảng vàng "Vua Nổ Lò" xem ai nổ nhiều nhất.',
            ]],
            ['ten' => 'Linh Điền — trồng linh thảo', 'moi' => true, 'chi_tiet' => [
                '6 ô ruộng riêng của từng người, mở ở NPC Tu Tiên.',
                'Gieo 500.000 vàng một ô, 4 giờ sau hái được 3–5 linh thảo.',
                'Dành cho ai chưa đủ sức cày map Tu Tiên: gieo bằng vàng, không phải đánh nhau với gì cả.',
            ]],
            ['ten' => 'Mỗi lúc chỉ được một loại đan', 'chi_tiet' => [
                'Đang có một loại đan mà cắn thêm loại khác thì chỉ 30% chịu được.',
                'Trượt là BẠO THỂ MÀ CHẾT và mất luôn viên đan đó.',
                'Ăn đè đúng loại đang dùng thì chỉ là làm mới hiệu lực, không tính.',
            ]],
        ],
    ],
    [
        'nhom' => 'Boss',
        'mo_ta' => 'Xem chi tiết con nào rơi gì ở trang "Boss rơi đồ".',
        'muc' => [
            ['ten' => 'Vegeta & Goku Siêu Thần God', 'moi' => true, 'chi_tiet' => [
                '500 triệu máu, sát thương 50.000, đi vòng ba hành tinh (Rừng xương / Vực Maima / Vách núi đen).',
                'Cứ 10 phút ra CẢ HAI con, nhưng luôn ở hai map khác nhau.',
                'Rơi Ngọc Rồng, bùa cấp 2, Gậy Thông Thiên, Đá Pháp Sư, Còi Triệu Hồi Lão Dê.',
            ]],
            ['ten' => 'Lão Dê Hồi Xuân', 'moi' => true, 'chi_tiet' => [
                'Không tự ra map. Chỉ hiện khi có người thổi Còi Triệu Hồi, và ra ngay chỗ người thổi.',
                'Máu và đồ rơi bằng đúng bộ Siêu Thần God.',
            ]],
            ['ten' => 'Trư Bát Giới & Tôn Ngộ Không Giả', 'moi' => true, 'chi_tiet' => [
                'Hai con boss dễ: 8 triệu và 60 triệu máu, sát thương 5.000 và 15.000.',
                'Đứng ở map ĐẦU của mỗi hành tinh, để người mới cũng săn được.',
                'Rơi Địa Hỏa Tinh và đan phương — đường lấy nguyên liệu luyện đan cho người cày chay.',
            ]],
            ['ten' => 'Mọi boss đều rơi nguyên liệu luyện đan', 'moi' => true, 'chi_tiet' => [
                'Boss trên 300.000 máu đều rơi Địa Hỏa Tinh, tỉ lệ 15% → 60% tăng dần theo máu.',
                'Boss trên 20 triệu máu rơi thêm Đan Phương Sơ Cấp 10%.',
                'Nghĩa là hạ con boss nào cũng có tiến độ, không cần phải săn được boss khủng.',
            ]],
            ['ten' => 'Heart và nhóm boss nhiệm vụ', 'moi' => true, 'chi_tiet' => [
                'Heart (2 tỷ máu, 4 hình dạng) nay rơi đồ y hệt Super Black Goku.',
                'Mọi boss nhiệm vụ rơi thêm 100 ngọc, rải thành nhiều đống quanh xác.',
            ]],
            ['ten' => 'NPC Theo Dõi Boss', 'chi_tiet' => [
                'Đứng ở đảo Kamê. Xem toàn bộ boss trong server: con nào đang ra map, ở map nào, con nào đang nghỉ.',
                'Xem được cả bảng rơi đồ của từng con.',
            ]],
            ['ten' => 'Hai boss Fu ở Nam Kamê', 'chi_tiet' => [
                '1 tỷ máu, sát thương 200.000. Cứ 15 phút ra một con.',
                'Rơi 5 Đá Pháp Sư và 1 Đá Tẩy Pháp Sư — nguồn đá chính cho pháp sư.',
            ]],
            ['ten' => 'Bộ Lốp Trưởng', 'chi_tiet' => [
                '8 con Lốp Trưởng cùng Nữ Thần Băng Tinh đứng cổ vũ.',
            ]],
        ],
    ],
    [
        'nhom' => 'Tính năng vui',
        'muc' => [
            ['ten' => 'Gậy Thông Thiên & Bà Mối', 'moi' => true, 'chi_tiet' => [
                'Cầm Gậy Thông Thiên, đứng sát người khác rồi bấm vào họ để mời.',
                'Người thông được +1% HP / KI / sức đánh mỗi lần, tối đa 10 lần.',
                'Người bị thông tối đa 20 lần, cứ 2 lần mới được 1%.',
                'NPC Bà Mối ở đảo Kamê giữ sổ: ai thông bao nhiêu lần, ai bị thông bao nhiêu lần, có bảng vàng hẳn hoi.',
            ]],
            ['ten' => 'Pháp sư trang bị', 'chi_tiet' => [
                'Nâng cấp trang bị bằng Đá Pháp Sư, tẩy bằng Đá Tẩy Pháp Sư.',
            ]],
        ],
    ],
    [
        'nhom' => 'Nội dung cơ bản',
        'muc' => [
            ['ten' => 'Nhiệm vụ cốt truyện', 'chi_tiet' => [
                'Tuyến nhiệm vụ đầy đủ, boss nhiệm vụ luôn xuất hiện — ai vào được map là đánh được, không cần đúng bước.',
                'Mỗi loại boss nhiệm vụ có nhiều bản nên đông người cũng không phải xếp hàng.',
            ]],
            ['ten' => 'Đại hội võ thuật / Võ đài Hạt Mít', 'chi_tiet' => ['Giải đấu theo lịch, có phần thưởng riêng.']],
            ['ten' => 'Phó bản', 'chi_tiet' => ['Bản đồ kho báu, Con đường rắn độc, Khí gas huỷ diệt, Doanh trại.']],
            ['ten' => 'Bang hội, siêu hạng, shop ký gửi, đệ tử, linh thú', 'chi_tiet' => []],
        ],
    ],
];
