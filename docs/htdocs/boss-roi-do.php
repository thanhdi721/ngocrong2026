<?php
include_once 'core/head.php';

$duong = __DIR__ . '/data/boss_drop.php';
$BOSS  = is_file($duong) ? (require $duong) : [];

/** Dải máu để lọc nhanh. */
function hang_boss($mau)
{
    if ($mau >= 200000000) return ['Khủng', 'hang-4'];
    if ($mau >= 50000000)  return ['Lớn',   'hang-3'];
    if ($mau >= 5000000)   return ['Vừa',   'hang-2'];
    return ['Nhỏ', 'hang-1'];
}
?>
<style>
.brd-wrap{max-width:1100px;margin:0 auto;padding:12px}
.brd-loc{display:flex;gap:8px;flex-wrap:wrap;justify-content:center;margin:14px 0 22px}
.brd-loc button{background:linear-gradient(to bottom,#FFA500,#FF8C00);border:2px solid #FFD700;color:#fff;
  padding:7px 18px;font-size:14px;font-weight:700;border-radius:12px;text-shadow:1px 1px 1px #000;
  box-shadow:0 4px 0 #cc8400;cursor:pointer}
.brd-loc button.active{filter:brightness(1.25);box-shadow:0 2px 0 #aa6b00;transform:translateY(2px)}
.brd-card{background:rgba(0,0,0,.35);border:1px solid rgba(255,215,0,.35);border-radius:12px;
  margin-bottom:16px;overflow:hidden}
.brd-head{display:flex;flex-wrap:wrap;align-items:baseline;gap:10px;padding:10px 14px;
  background:rgba(255,165,0,.18);border-bottom:1px solid rgba(255,215,0,.25)}
.brd-ten{font-size:17px;font-weight:700;color:#ffd700}
.brd-mau{font-size:13px;color:#9fe08f}
.brd-map{font-size:12px;color:#cfd8dc;flex:1 1 100%}
.brd-ghi{font-size:12px;color:#ffcc80;flex:1 1 100%;font-style:italic}
.brd-tb{width:100%;border-collapse:collapse;font-size:13px}
.brd-tb th{background:rgba(255,255,255,.06);color:#ffd700;text-align:left;padding:7px 12px;font-weight:600}
.brd-tb td{padding:7px 12px;border-top:1px solid rgba(255,255,255,.07);color:#eceff1;vertical-align:top}
.brd-tb td:nth-child(2){white-space:nowrap;color:#81d4fa}
.brd-tb td:nth-child(3){white-space:nowrap;color:#ffab91;font-weight:600}
.brd-chung{font-size:11px;color:#b0bec5;background:rgba(255,255,255,.08);padding:1px 7px;border-radius:8px;margin-left:6px}
.brd-note{font-size:12px;color:#b0bec5}
.brd-trong{text-align:center;padding:40px;color:#ffcc80}
@media(max-width:600px){.brd-tb td,.brd-tb th{padding:6px 8px;font-size:12px}}
</style>

<div class="brd-wrap">
    <h3 style="text-align:center;color:#ffd700;margin:6px 0">Boss rơi đồ gì</h3>

    <?php if (!$BOSS): ?>
        <div class="brd-trong">
            Chưa có dữ liệu.<br>
            Chạy <code>php tools/sinh-boss-drop.php</code> để sinh file <code>data/boss_drop.php</code>.
        </div>
    <?php else: ?>
        <p class="brd-note" style="text-align:center">
            <?= count($BOSS) ?> boss. Dòng có nhãn <span class="brd-chung">luật chung</span>
            là đồ rơi áp cho mọi boss đủ máu, không phải riêng con này.
        </p>

        <div class="brd-loc">
            <button class="active" data-loc="all">Tất cả</button>
            <button data-loc="hang-1">Nhỏ (&lt; 5 triệu)</button>
            <button data-loc="hang-2">Vừa (5–50 triệu)</button>
            <button data-loc="hang-3">Lớn (50–200 triệu)</button>
            <button data-loc="hang-4">Khủng (&gt; 200 triệu)</button>
        </div>

        <?php foreach ($BOSS as $b):
            [$nhanHang, $lopHang] = hang_boss($b['mau']); ?>
            <div class="brd-card <?= $lopHang ?>">
                <div class="brd-head">
                    <span class="brd-ten"><?= htmlspecialchars($b['ten']) ?></span>
                    <span class="brd-mau"><?= number_format($b['mau'], 0, ',', '.') ?> máu · <?= $nhanHang ?></span>
                    <?php if ($b['map_ten']): ?>
                        <span class="brd-map">Map: <?= htmlspecialchars(implode(' · ', $b['map_ten'])) ?></span>
                    <?php endif; ?>
                    <?php if (!empty($b['ghi'])): ?>
                        <span class="brd-ghi"><?= htmlspecialchars($b['ghi']) ?></span>
                    <?php endif; ?>
                </div>
                <table class="brd-tb">
                    <tr><th>Vật phẩm</th><th>Số lượng</th><th>Tỉ lệ</th></tr>
                    <?php foreach ($b['roi'] as $d): ?>
                        <tr>
                            <td>
                                <?= htmlspecialchars($d['ten']) ?>
                                <?php if (!empty($d['ghi'])): ?>
                                    <span class="brd-note">— <?= htmlspecialchars($d['ghi']) ?></span>
                                <?php endif; ?>
                                <?php if (!empty($d['chung'])): ?>
                                    <span class="brd-chung">luật chung</span>
                                <?php endif; ?>
                            </td>
                            <td><?= htmlspecialchars($d['sl']) ?></td>
                            <td><?= htmlspecialchars($d['tile']) ?></td>
                        </tr>
                    <?php endforeach; ?>
                </table>
            </div>
        <?php endforeach; ?>
    <?php endif; ?>
</div>

<script>
document.querySelectorAll('.brd-loc button').forEach(function (nut) {
    nut.addEventListener('click', function () {
        document.querySelectorAll('.brd-loc button').forEach(b => b.classList.remove('active'));
        nut.classList.add('active');
        var loc = nut.dataset.loc;
        document.querySelectorAll('.brd-card').forEach(function (c) {
            c.style.display = (loc === 'all' || c.classList.contains(loc)) ? '' : 'none';
        });
    });
});
</script>

<?php include_once 'core/footer.php'; ?>
