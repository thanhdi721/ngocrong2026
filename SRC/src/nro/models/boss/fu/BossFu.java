package nro.models.boss.fu;

import nro.models.boss.Boss;
import nro.models.boss.BossID;
import nro.models.boss.BossesData;
import nro.models.consts.BossStatus;
import nro.models.consts.ConstPlayer;
import nro.models.player.Player;
import nro.models.services.EffectSkillService;
import nro.models.services.SkillService;
import nro.models.utils.Util;

/**
 * Hai boss ở Nam Kamê (map 29): <b>Fu Thời Không</b> và <b>Fu Hợp Thể</b>.
 *
 * <p>Cứ {@value #PHUT_CHO} phút ra MỘT con, bốc ngẫu nhiên một trong hai — không bao giờ có
 * hai con cùng lúc. Con nào chết (hoặc bỏ đi) thì tính lại từ đầu {@value #PHUT_CHO} phút.
 *
 * <p>Máu 1 tỷ, sát thương 200.000, bộ chiêu và cách đánh lấy y như Cumber. Hạ được thì rơi
 * 5 Đá Pháp Sư và 1 Đá Tẩy Pháp Sư — nguồn đá chính để pháp sư trang bị ở Bà Hạt Mít.
 * Bảng rơi nằm ở {@link nro.models.boss.drop.BangRoiBoss} nên sửa được từ cpanel.
 */
public class BossFu extends Boss {

    /** Phút chờ giữa hai lượt, tính từ lúc con trước rời map. */
    private static final int PHUT_CHO = 15;

    //========================== điều phối: mỗi lượt chỉ một con ==========================
    private static BossFu fu;
    private static BossFu hopThe;
    /** Con đã được bốc cho lượt này; null nghĩa là chưa bốc. */
    private static BossFu sapRa;
    /** Lúc con gần nhất rời map, để đếm 15 phút. */
    private static long lanKetThuc;

    public static void taoCaHai() throws Exception {
        if (fu == null) {
            fu = new BossFu(BossID.FU, BossesData.FU);
        }
        if (hopThe == null) {
            hopThe = new BossFu(BossID.FU_HOP_THE, BossesData.FU_HOP_THE);
        }
        lanKetThuc = System.currentTimeMillis();
    }

    /** Con đang đứng ngoài map (null nếu cả hai đang nghỉ). */
    private static BossFu dangRaNgoai() {
        if (fu != null && fu.bossStatus != BossStatus.REST) {
            return fu;
        }
        if (hopThe != null && hopThe.bossStatus != BossStatus.REST) {
            return hopThe;
        }
        return null;
    }

    private BossFu(int id, nro.models.boss.BossData data) throws Exception {
        super(id, data);
        // Đặt tên ngay lúc dựng: trước lần ra map đầu tiên thì initBase chưa chạy, cpanel
        // tab "Boss" sẽ hiện dấu "?" thay vì tên, nhìn tưởng chưa có boss.
        this.name = data.getName();
        this.nPoint.hpg = data.getHp()[0];
        this.nPoint.hpMax = data.getHp()[0];
        this.nPoint.dameg = data.getDame();
    }

    /**
     * Thay cho nhịp nghỉ mặc định của từng con: chỉ cho ra khi đã đủ giờ VÀ con kia đang
     * không ở ngoài map, và mỗi lượt chỉ bốc đúng một con.
     */
    @Override
    public void rest() {
        if (dangRaNgoai() != null) {
            return;
        }
        if (!Util.canDoWithTime(lanKetThuc, PHUT_CHO * 60_000L)) {
            return;
        }
        if (sapRa == null) {
            sapRa = Util.isTrue(1, 2) ? fu : hopThe;
        }
        if (sapRa != this) {
            return;
        }
        sapRa = null;
        this.changeStatus(BossStatus.RESPAWN);
    }

    @Override
    public void leaveMap() {
        super.leaveMap();
        lanKetThuc = System.currentTimeMillis();
        sapRa = null;
    }

    @Override
    public void die(Player plKill) {
        super.die(plKill);
        lanKetThuc = System.currentTimeMillis();
        sapRa = null;
    }

    /** Cách đánh lấy y như Cumber: xa thì bay lại, gần thì né qua né lại rồi tung chiêu. */
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
            this.chat("Chậm quá");
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
