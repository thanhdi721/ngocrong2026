<?php
/* =====================================================================
 * tools/sinh-boss-drop.php — SINH FILE DỮ LIỆU BẢNG RƠI ĐỒ BOSS
 * ---------------------------------------------------------------------
 * Chạy:  php tools/sinh-boss-drop.php
 * Kết quả: data/boss_drop.php  (trang boss-roi-do.php đọc file này)
 *
 * VÌ SAO SINH RA FILE TĨNH:
 *   Bảng rơi đồ nằm trong mã nguồn Java của server, web không đọc thẳng được.
 *   File này giữ phần "khai báo" ở dạng PHP cho dễ sửa, rồi tra TÊN vật phẩm
 *   thật từ bảng `item_template` trong CSDL.
 *
 * KHI NÀO PHẢI CHẠY LẠI:
 *   - sau khi sửa bảng rơi đồ trong cpanel của server game;
 *   - sau khi thêm/đổi boss hoặc vật phẩm.
 * ===================================================================== */

require_once __DIR__ . '/../core/cauhinh.php';

//========================= 1. KHAI BÁO BẢNG RƠI =========================
// Mỗi dòng: [ 'ids' => [id vật phẩm...], 'sl' => 'x1' hoặc 'x2-3', 'tile' => '40%' ]
// 'ids' nhiều id nghĩa là trúng thì bốc NGẪU NHIÊN một cái trong đó.

$NGOC_RONG_357   = [16, 17, 18];
$NR_BI_NGO       = [702, 703, 704, 705, 706, 707, 708];
$BUA_CAP_2       = [1150, 1151, 1152, 1153];
$NR_567          = [18, 19, 20];
$TB_AO_QUAN_GIAY = [230,231,232,234,235,236,238,239,240,242,243,244,246,247,248,250,251,252,266,267,268,270,271,272,274,275,276];
$TB_GANG_RADA    = [254,255,256,258,259,260,262,263,264,278,279,280];
$LINH_THAO       = [2276, 2277, 2278, 2279];

/** Bộ đồ rơi chuẩn của boss thường: vàng + một viên Ngọc Rồng. */
$BO_THUONG = [
    ['ids' => [190],     'sl' => 'x20.000-30.000', 'tile' => '100%'],
    ['ids' => $NR_567,   'sl' => 'x1',             'tile' => '80%'],
];

/** Bộ đồ rơi kiểu Black Goku / Heart. */
$BO_BLACK_GOKU = [
    ['ids' => [190],              'sl' => 'x20.000-30.000', 'tile' => '100%'],
    ['ids' => [],                 'sl' => '',               'tile' => '1-5% (theo máu boss)', 'ten' => 'Đồ Thần Linh ngẫu nhiên'],
    ['ids' => $TB_AO_QUAN_GIAY,   'sl' => 'x1',             'tile' => '3,5%', 'ghi' => 'kèm chỉ số + sao pha lê'],
    ['ids' => $TB_GANG_RADA,      'sl' => 'x1',             'tile' => '1,5%', 'ghi' => 'kèm chỉ số + sao pha lê'],
    ['ids' => [15,16,17,18,19,20,992], 'sl' => 'x1-3',      'tile' => '10%'],
];

$BOSS = [];

//--- Boss thế giới dùng bộ thường
foreach ([
    'Tiểu đội trưởng' => [50000000, [79,81,82,83]],
    'Kuku'            => [500000,   [68,69,70,71,72]],
    'Mập Đầu Đinh'    => [1000000,  [63,64,65,66,67]],
    'Rambo'           => [1500000,  [74,75,76,77]],
    'Fide đại ca'     => [30000000, [80]],
    'Dr.Kôrê'         => [2000000,  [96,94,93]],
    'Android 14'      => [4000000,  [104]],
    'King Kong'       => [20000000, [97,98,99]],
    'Xên bọ hung'     => [1060000,  [100]],
] as $ten => $x) {
    $BOSS[] = ['ten' => $ten, 'mau' => $x[0], 'map' => $x[1], 'roi' => $BO_THUONG];
}

//--- Boss lớn
$BOSS[] = ['ten' => 'Siêu bọ hung', 'mau' => 930000, 'map' => [100], 'roi' => [
    ['ids' => [190], 'sl' => 'x20.000-30.000', 'tile' => '100%'],
    ['ids' => [], 'sl' => '', 'tile' => '1-5% (theo máu boss)', 'ten' => 'Đồ Thần Linh ngẫu nhiên'],
    ['ids' => $TB_AO_QUAN_GIAY, 'sl' => 'x1', 'tile' => '21%', 'ghi' => 'kèm chỉ số + sao pha lê'],
    ['ids' => $TB_GANG_RADA,    'sl' => 'x1', 'tile' => '9%',  'ghi' => 'kèm chỉ số + sao pha lê'],
]];
$BOSS[] = ['ten' => 'Cooler',     'mau' => 500000000,  'map' => [110], 'roi' => $BO_BLACK_GOKU];
$BOSS[] = ['ten' => 'Black Goku', 'mau' => 500000000,  'map' => [102,92,93,94,96,97,98], 'roi' => $BO_BLACK_GOKU];
$BOSS[] = ['ten' => 'Super Black Goku', 'mau' => 2000000000, 'map' => [102,92,93,94,96,97,98], 'roi' => $BO_BLACK_GOKU];
$BOSS[] = ['ten' => 'Cumber', 'mau' => 500000000, 'map' => [155], 'roi' => array_merge($BO_BLACK_GOKU, [
    ['ids' => [15,16,17,18,19,20,992], 'sl' => 'x1-3', 'tile' => '10%', 'ghi' => 'Ngọc Rồng / Nhẫn thời không'],
])];
$BOSS[] = ['ten' => 'Baby', 'mau' => 1900000, 'map' => [14], 'roi' => array_merge($BO_BLACK_GOKU, [
    ['ids' => [1785,1786,1788], 'sl' => 'x1', 'tile' => '1%', 'ghi' => 'cải trang kèm chỉ số'],
])];

//--- Tiểu đội sát thủ Namek / Bojack
$BOSS[] = ['ten' => 'Tiểu đội trưởng Namek', 'mau' => 5000000, 'map' => [7,8,9,10,11,12,13], 'roi' => [
    ['ids' => [77],  'sl' => 'x1-5',  'tile' => '100%', 'ghi' => 'rơi thành nhiều đống'],
    ['ids' => [433], 'sl' => 'x1',    'tile' => '100%', 'ghi' => 'cải trang kèm chỉ số'],
    ['ids' => [19,20], 'sl' => 'cả hai viên', 'tile' => '100%'],
]];
$BOSS[] = ['ten' => 'Bojack', 'mau' => 100000000, 'map' => [97,98,99,100,105,106,107], 'roi' => [
    ['ids' => [77],  'sl' => 'x5-20', 'tile' => '100%', 'ghi' => 'rơi thành nhiều đống'],
    ['ids' => [427], 'sl' => 'x1',    'tile' => '100%', 'ghi' => 'cải trang kèm chỉ số'],
    ['ids' => [19,20], 'sl' => 'cả hai viên', 'tile' => '100%'],
]];
$BOSS[] = ['ten' => 'Siêu Bojack', 'mau' => 500000000, 'map' => [97,98,99,100,105,106,107], 'roi' => [
    ['ids' => [77],  'sl' => 'x5-15', 'tile' => '100%', 'ghi' => 'rơi thành nhiều đống'],
    ['ids' => [428], 'sl' => 'x1',    'tile' => '100%', 'ghi' => 'cải trang kèm chỉ số'],
    ['ids' => [19,20], 'sl' => 'cả hai viên', 'tile' => '100%'],
]];
$BOSS[] = ['ten' => 'Fide Vàng', 'mau' => 1000000000, 'map' => [6], 'roi' => [
    ['ids' => [629], 'sl' => 'x1', 'tile' => '100%', 'ghi' => 'cải trang Fide vàng kèm chỉ số'],
]];
$BOSS[] = ['ten' => 'Mặt trời', 'mau' => 100, 'map' => [], 'roi' => [
    ['ids' => [1562], 'sl' => 'x1', 'tile' => '50%', 'ghi' => 'Mặt trời tí hon kèm chỉ số'],
]];

//--- Hai boss Fu (Nam Kamê)
foreach (['Fu Thời Không', 'Fu Hợp Thể'] as $ten) {
    $BOSS[] = ['ten' => $ten, 'mau' => 1000000000, 'map' => [29], 'roi' => [
        ['ids' => [2262], 'sl' => 'x5', 'tile' => '100%'],
        ['ids' => [2263], 'sl' => 'x1', 'tile' => '100%'],
    ]];
}

//--- Bộ Siêu Thần God + Lão Dê (một vòng quay 100%)
$BO_SIEU_THAN_GOD = [
    ['ids' => $NGOC_RONG_357, 'sl' => 'x1', 'tile' => '40%', 'ghi' => 'một vòng quay chung 100%'],
    ['ids' => $NR_BI_NGO,     'sl' => 'x1', 'tile' => '40%', 'ghi' => 'một vòng quay chung 100%'],
    ['ids' => $BUA_CAP_2,     'sl' => 'x1', 'tile' => '10%', 'ghi' => 'một vòng quay chung 100%'],
    ['ids' => [2264],         'sl' => 'x1', 'tile' => '5%',  'ghi' => 'một vòng quay chung 100%'],
    ['ids' => [2262],         'sl' => 'x5', 'tile' => '5%',  'ghi' => 'một vòng quay chung 100%'],
    ['ids' => [2265],         'sl' => 'x1', 'tile' => '10%'],
    ['ids' => [2280],         'sl' => 'x2-3', 'tile' => '100%'],
    ['ids' => [2282],         'sl' => 'x1', 'tile' => '15%'],
    ['ids' => [2283],         'sl' => 'x1', 'tile' => '5%'],
];
$BOSS[] = ['ten' => 'Vegeta Siêu Thần God', 'mau' => 500000000, 'map' => [4,12,20], 'roi' => $BO_SIEU_THAN_GOD];
$BOSS[] = ['ten' => 'Goku Siêu Thần God',   'mau' => 500000000, 'map' => [4,12,20], 'roi' => $BO_SIEU_THAN_GOD];
$BOSS[] = ['ten' => 'Lão Dê Hồi Xuân',      'mau' => 500000000, 'map' => [],        'roi' => $BO_SIEU_THAN_GOD,
           'ghi' => 'Chỉ hiện khi có người thổi Còi Triệu Hồi Lão Dê'];

//--- Hai boss Tây Du (luyện đan)
$BOSS[] = ['ten' => 'Trư Bát Giới Ăn Vụng', 'mau' => 8000000, 'map' => [1,8,15], 'roi' => [
    ['ids' => [2280],       'sl' => 'x1',   'tile' => '40%'],
    ['ids' => [2281],       'sl' => 'x1',   'tile' => '12%'],
    ['ids' => $LINH_THAO,   'sl' => 'x1-2', 'tile' => '35%'],
]];
$BOSS[] = ['ten' => 'Tôn Ngộ Không Giả', 'mau' => 60000000, 'map' => [3,10,17], 'roi' => [
    ['ids' => [2280],         'sl' => 'x1-2', 'tile' => '60%'],
    ['ids' => [2281],         'sl' => 'x1',   'tile' => '20%'],
    ['ids' => [2282],         'sl' => 'x1',   'tile' => '8%'],
    ['ids' => $LINH_THAO,     'sl' => 'x2-3', 'tile' => '50%'],
    ['ids' => $NGOC_RONG_357, 'sl' => 'x1',   'tile' => '30%'],
]];

//--- Heart + nhóm boss nhiệm vụ
$NGOC_NHIEM_VU = ['ids' => [77], 'sl' => 'x100', 'tile' => '100%', 'ghi' => 'rơi thành 7 đống quanh xác'];
$BOSS[] = ['ten' => 'Heart', 'mau' => 2000000000, 'map' => [166,145,155],
    'ghi' => 'Bốn hình dạng. Ống nghiệm Myuu chỉ rơi ở hình dạng 3 (NV 46)',
    'roi' => array_merge($BO_BLACK_GOKU, [
        $NGOC_NHIEM_VU,
        ['ids' => [2029], 'sl' => 'x1', 'tile' => '100%', 'ghi' => 'chỉ hình dạng 3'],
    ])];
foreach ([
    'Cumber (nhiệm vụ)'      => 4400000,
    'Black Goku (nhiệm vụ)'  => 3600000,
    'Cooler (nhiệm vụ)'      => 3300000,
    'Jaco Vô Thức'           => 2500000,
    'Mabư mập (nhiệm vụ)'    => 2000000,
    'Baby (nhiệm vụ)'        => 1900000,
    'Xên bọ hung (nhiệm vụ)' => 1060000,
    'Kẻ Thu Gom'             => 80000,
] as $ten => $mau) {
    $BOSS[] = ['ten' => $ten, 'mau' => $mau, 'map' => [],
               'ghi' => 'Boss nhiệm vụ — ngoài ngọc chỉ rơi đồ nhiệm vụ theo bước',
               'roi' => [$NGOC_NHIEM_VU]];
}

//========================= 2. LUẬT RƠI CHUNG =========================
// Mọi boss đủ máu đều rơi thêm Địa Hỏa Tinh; trên 20 triệu máu rơi thêm Đan Phương Sơ Cấp.
// Boss nào đã có dòng Địa Hỏa Tinh viết tay ở trên thì KHÔNG cộng thêm.
function luat_chung($mau)
{
    if ($mau < 300000) { return []; }
    if     ($mau >= 200000000) { $tile = '60%'; $sl = 'x1-2'; }
    elseif ($mau >= 50000000)  { $tile = '40%'; $sl = 'x1'; }
    elseif ($mau >= 5000000)   { $tile = '25%'; $sl = 'x1'; }
    else                       { $tile = '15%'; $sl = 'x1'; }
    $ra = [['ids' => [2280], 'sl' => $sl, 'tile' => $tile, 'chung' => true]];
    if ($mau >= 20000000) {
        $ra[] = ['ids' => [2281], 'sl' => 'x1', 'tile' => '10%', 'chung' => true];
    }
    return $ra;
}

foreach ($BOSS as &$b) {
    $daCo = false;
    foreach ($b['roi'] as $d) { if (in_array(2280, $d['ids'], true)) { $daCo = true; break; } }
    if (!$daCo) { $b['roi'] = array_merge($b['roi'], luat_chung($b['mau'])); }
}
unset($b);

//========================= 3. TRA TÊN VẬT PHẨM + TÊN MAP =========================
$ITEM = [];
foreach ($conn->query("SELECT id, NAME, icon_id FROM item_template") as $r) {
    $ITEM[(int) $r['id']] = ['ten' => $r['NAME'], 'icon' => (int) $r['icon_id']];
}
$MAP = [];
foreach ($conn->query("SELECT id, NAME FROM map_template") as $r) {
    $MAP[(int) $r['id']] = $r['NAME'];
}

$thieu = [];
foreach ($BOSS as &$b) {
    $b['map_ten'] = array_values(array_map(
        fn($m) => $MAP[$m] ?? ('map ' . $m),
        $b['map']
    ));
    foreach ($b['roi'] as &$d) {
        if (!isset($d['ten'])) {
            $ten = [];
            foreach ($d['ids'] as $id) {
                if (isset($ITEM[$id])) { $ten[] = $ITEM[$id]['ten']; }
                else { $thieu[$id] = true; $ten[] = '#' . $id; }
            }
            $d['ten'] = count($ten) > 4
                ? ($ten[0] . ' và ' . (count($ten) - 1) . ' món khác')
                : implode(' / ', $ten);
        }
        $d['icon'] = isset($d['ids'][0], $ITEM[$d['ids'][0]]) ? $ITEM[$d['ids'][0]]['icon'] : 0;
    }
    unset($d);
}
unset($b);

//========================= 4. GHI FILE =========================
usort($BOSS, fn($x, $y) => $y['mau'] <=> $x['mau']);
$noiDung = "<?php\n/* SINH TỰ ĐỘNG bởi tools/sinh-boss-drop.php — ĐỪNG SỬA TAY.\n"
         . " * Sinh lúc: " . date('d/m/Y H:i') . "\n */\nreturn " . var_export($BOSS, true) . ";\n";

@mkdir(__DIR__ . '/../data', 0775, true);
file_put_contents(__DIR__ . '/../data/boss_drop.php', $noiDung);

echo "Đã ghi data/boss_drop.php — " . count($BOSS) . " boss.\n";
if ($thieu) {
    echo "CẢNH BÁO: " . count($thieu) . " id vật phẩm không có trong item_template: "
       . implode(', ', array_keys($thieu)) . "\n";
    echo "  -> có thể bạn chưa chạy hết patch SQL.\n";
}
