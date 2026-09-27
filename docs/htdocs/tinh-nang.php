<?php
include_once 'core/head.php';
$NHOM = require __DIR__ . '/data/tinh_nang.php';
?>
<style>
.tn-wrap{max-width:1000px;margin:0 auto;padding:12px}
.tn-nhom{margin-bottom:26px}
.tn-nhom-ten{font-size:19px;font-weight:700;color:#ffd700;border-bottom:2px solid rgba(255,215,0,.35);
  padding-bottom:6px;margin-bottom:6px}
.tn-nhom-mota{font-size:13px;color:#b0bec5;margin-bottom:12px;font-style:italic}
.tn-muc{background:rgba(0,0,0,.35);border:1px solid rgba(255,215,0,.22);border-radius:10px;
  margin-bottom:10px;overflow:hidden}
.tn-dau{display:flex;align-items:center;gap:9px;padding:10px 14px;cursor:pointer;
  background:rgba(255,165,0,.14);user-select:none}
.tn-dau:hover{background:rgba(255,165,0,.24)}
.tn-ten{font-weight:700;color:#fff3e0;font-size:15px;flex:1}
.tn-moi{background:#e53935;color:#fff;font-size:10px;font-weight:700;padding:2px 7px;border-radius:8px;letter-spacing:.5px}
.tn-mui{color:#ffd700;font-size:13px;transition:transform .18s}
.tn-muc.mo .tn-mui{transform:rotate(90deg)}
.tn-than{display:none;padding:4px 14px 12px 30px}
.tn-muc.mo .tn-than{display:block}
.tn-than li{color:#eceff1;font-size:13.5px;line-height:1.75;margin-bottom:3px}
.tn-trong{color:#b0bec5;font-size:13px;padding:2px 0}
</style>

<div class="tn-wrap">
    <h3 style="text-align:center;color:#ffd700;margin:6px 0">Máy chủ có những gì</h3>
    <p style="text-align:center;color:#b0bec5;font-size:13px">Bấm vào từng mục để xem chi tiết.</p>

    <?php foreach ($NHOM as $nhom): ?>
        <div class="tn-nhom">
            <div class="tn-nhom-ten"><?= htmlspecialchars($nhom['nhom']) ?></div>
            <?php if (!empty($nhom['mo_ta'])): ?>
                <div class="tn-nhom-mota"><?= htmlspecialchars($nhom['mo_ta']) ?></div>
            <?php endif; ?>

            <?php foreach ($nhom['muc'] as $muc): ?>
                <div class="tn-muc">
                    <div class="tn-dau">
                        <span class="tn-mui">&#9654;</span>
                        <span class="tn-ten"><?= htmlspecialchars($muc['ten']) ?></span>
                        <?php if (!empty($muc['moi'])): ?><span class="tn-moi">MỚI</span><?php endif; ?>
                    </div>
                    <div class="tn-than">
                        <?php if (!empty($muc['chi_tiet'])): ?>
                            <ul><?php foreach ($muc['chi_tiet'] as $d): ?>
                                <li><?= htmlspecialchars($d) ?></li>
                            <?php endforeach; ?></ul>
                        <?php else: ?>
                            <div class="tn-trong">Có sẵn trong game.</div>
                        <?php endif; ?>
                    </div>
                </div>
            <?php endforeach; ?>
        </div>
    <?php endforeach; ?>

    <p style="text-align:center;margin-top:24px">
        <a href="boss-roi-do.php" style="color:#ffd700;font-weight:700">Xem bảng rơi đồ của từng boss &rarr;</a>
    </p>
</div>

<script>
document.querySelectorAll('.tn-dau').forEach(function (d) {
    d.addEventListener('click', function () { d.parentElement.classList.toggle('mo'); });
});
</script>

<?php include_once 'core/footer.php'; ?>
