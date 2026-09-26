package nro.models.boss.tay_du;

import nro.models.boss.Boss;
import nro.models.boss.BossData;
import nro.models.boss.BossID;
import nro.models.boss.BossesData;
import nro.models.consts.BossStatus;
import nro.models.consts.ConstPlayer;
import nro.models.player.Player;
import nro.models.services.EffectSkillService;
import nro.models.services.SkillService;
import nro.models.utils.Util;

/**
 * Hai boss Tây Du của tính năng luyện đan: <b>Trư Bát Giới Ăn Vụng</b> và
 * <b>Tôn Ngộ Không Giả</b>.
 *
 * <p>Lý do tồn tại: <b>cân bằng đường lấy Địa Hỏa Tinh</b>. Trước patch 84 chỉ có bộ Siêu
 * Thần God (500 triệu máu, 50.000 sát thương) rơi nguyên liệu quý, tức là người chơi yếu
 * không có cửa. Hai con này máu thấp, sát thương thấp, đứng ở map ĐẦU của mỗi hành tinh, và
 * cũng rơi Địa Hỏa Tinh + đan phương — chỉ ít hơn và thưa hơn.
 *
 * <p>Mỗi con tự chạy nhịp riêng theo {@code secondsRest} của {@link BossesData} (5 phút cho
 * Bát Giới, 10 phút cho Ngộ Không Giả), KHÔNG phối hợp với nhau như cặp Fu hay cặp Siêu Thần
 * God — hai con ở hai dải map khác nhau nên không việc gì phải chặn nhau.
 *
 * <p>Toàn bộ đồ rơi nằm ở {@link nro.models.boss.drop.BangRoiBoss} (sửa được từ cpanel, xem
 * được ở NPC "Theo Dõi Boss"), lớp này cố ý KHÔNG viết cứng {@code reward()}.
 */
public class BossTayDu extends Boss {

    /** Đứng ngoài map quá lâu mà không ai đánh thì tự bỏ đi, nhường chỗ cho lượt sau. */
    private static final long TU_BO_DI_MS = 15 * 60_000L;

    private long lucRaMap;

    public static void taoCaHai() throws Exception {
        new BossTayDu(BossID.TRU_BAT_GIOI, BossesData.TRU_BAT_GIOI);
        new BossTayDu(BossID.TON_NGO_KHONG_GIA, BossesData.TON_NGO_KHONG_GIA);
    }

    private BossTayDu(int id, BossData data) throws Exception {
        // Dùng đúng hàm dựng 2 tham số như cặp Fu / Siêu Thần God: KHÔNG bật
        // isZone01SpawnDisabled, vì hai con này cố ý phải ra ngay khu 0/1 — nơi người chơi
        // mới đứng đông nhất. Thông báo toàn server nay xét theo máu (Boss.canSendNotify),
        // 8 triệu / 60 triệu đều trên ngưỡng nên vẫn có tiếng.
        super(id, data);
        // Đặt tên/máu/sát thương ngay lúc dựng: trước lần ra map đầu tiên thì initBase chưa
        // chạy, cpanel tab "Boss" sẽ hiện dấu "?" thay vì tên, nhìn tưởng chưa có boss.
        this.name = data.getName();
        this.nPoint.hpg = data.getHp()[0];
        this.nPoint.hpMax = data.getHp()[0];
        this.nPoint.dameg = data.getDame();
    }

    @Override
    public void joinMap() {
        super.joinMap();
        this.lucRaMap = System.currentTimeMillis();
    }

    @Override
    public void autoLeaveMap() {
        if (Util.canDoWithTime(this.lucRaMap, TU_BO_DI_MS)) {
            this.changeStatus(BossStatus.LEAVE_MAP);
        }
    }

    /** Cách đánh lấy y như cặp Fu: xa thì bay lại, gần thì né qua né lại rồi tung chiêu. */
    @Override
    public void attack() {
        if (Util.canDoWithTime(this.lastTimeAttack, 100) && this.typePk == ConstPlayer.PK_ALL) {
            this.lastTimeAttack = System.currentTimeMillis();
            try {
                Player pl = getPlayerAttack();
                if (pl == null || pl.isDie()) {
                    return;
                }
                this.playerSkill.skillSelect = this.playerSkill.skills.get(
                        Util.nextInt(0, this.playerSkill.skills.size() - 1));
                int dis = Util.getDistance(this, pl);
                if (dis > 450) {
                    move(pl.location.x - 24, pl.location.y);
                } else if (dis > 100) {
                    int dir = (this.location.x - pl.location.x < 0 ? 1 : -1);
                    int buoc = Util.nextInt(50, 100);
                    move(this.location.x + (dir == 1 ? buoc : -buoc), pl.location.y);
                } else {
                    if (Util.isTrue(30, 100)) {
                        int buoc = Util.nextInt(50);
                        move(pl.location.x + (Util.nextInt(0, 1) == 1 ? buoc : -buoc), this.location.y);
                    }
                    SkillService.gI().useSkill(this, pl, null, -1, null);
                    checkPlayerDie(pl);
                }
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        }
    }

    @Override
    public synchronized int injured(Player plAtt, long damage, boolean piercing, boolean isMobAttack) {
        if (this.isDie()) {
            return 0;
        }
        if (!piercing && Util.isTrue(this.nPoint.tlNeDon, 1000)) {
            this.chat("Hụt rồi");
            return 0;
        }
        damage = this.nPoint.subDameInjureWithDeff(damage);
        if (!piercing && effectSkill.isShielding) {
            if (damage > nPoint.hpMax) {
                EffectSkillService.gI().breakShield(this);
            }
            damage = 1;
        }
        damage = applyDamageReduce(damage);
        this.nPoint.subHP(damage);
        if (isDie()) {
            this.setDie(plAtt);
            die(plAtt);
        }
        return (int) damage;
    }
}
