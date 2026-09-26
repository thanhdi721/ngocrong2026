package nro.models.boss.sieu_than_god;

import nro.models.boss.Boss;
import nro.models.boss.BossData;
import nro.models.boss.BossID;
import nro.models.boss.BossesData;
import nro.models.consts.BossStatus;
import nro.models.consts.ConstPlayer;
import nro.models.map.Zone;
import nro.models.map.service.MapService;
import nro.models.player.Player;
import nro.models.services.EffectSkillService;
import nro.models.services.SkillService;
import nro.models.utils.Util;

/**
 * Hai boss "Siêu Thần God" đi vòng ba hành tinh: <b>Vegeta Siêu Thần God</b> và
 * <b>Goku Siêu Thần God</b>.
 *
 * <p>Khác hẳn cặp Fu ở Nam Kamê (mỗi lượt chỉ một con): ở đây cứ {@value #PHUT_CHO} phút là
 * ra <b>cả hai</b>, nhưng hàm {@link #getMapJoin()} bảo đảm hai con không bao giờ rơi vào
 * cùng một map. Lượt mới chỉ bắt đầu đếm khi CẢ HAI đã rời map (chết hoặc bỏ đi), nên không
 * có chuyện một con chết rồi hồi sinh liên tục trong lúc con kia còn đứng đó.
 *
 * <p>Máu 500 triệu, sát thương 50.000, bộ chiêu và cách đánh y như cặp Fu. Rơi đồ theo bảng
 * ở bảng {@link nro.models.boss.drop.BangRoiBoss} (sửa được từ cpanel, xem được ở NPC
 * "Theo Dõi Boss"), không viết cứng trong lớp này.
 */
public class BossSieuThanGod extends Boss {

    /** Phút chờ giữa hai lượt, tính từ lúc con cuối cùng của lượt trước rời map. */
    private static final int PHUT_CHO = 10;

    //========================== điều phối: mỗi lượt ra cả hai ==========================
    private static BossSieuThanGod vegeta;
    private static BossSieuThanGod goku;
    /** Lúc con CUỐI CÙNG của lượt trước rời map, để đếm {@value #PHUT_CHO} phút. */
    private static long lanKetThuc;

    public static void taoCaHai() throws Exception {
        if (vegeta == null) {
            vegeta = new BossSieuThanGod(BossID.VEGETA_SIEU_THAN_GOD, BossesData.VEGETA_SIEU_THAN_GOD);
        }
        if (goku == null) {
            goku = new BossSieuThanGod(BossID.GOKU_SIEU_THAN_GOD, BossesData.GOKU_SIEU_THAN_GOD);
        }
        lanKetThuc = System.currentTimeMillis();
    }

    /** Con còn lại của cặp (null lúc server chưa dựng xong). */
    private BossSieuThanGod conKia() {
        return this == vegeta ? goku : vegeta;
    }

    private BossSieuThanGod(int id, BossData data) throws Exception {
        super(id, data);
        // Đặt tên/máu/sát thương ngay lúc dựng: trước lần ra map đầu tiên thì initBase chưa
        // chạy, cpanel tab "Boss" sẽ hiện dấu "?" thay vì tên, nhìn tưởng chưa có boss.
        this.name = data.getName();
        this.nPoint.hpg = data.getHp()[0];
        this.nPoint.hpMax = data.getHp()[0];
        this.nPoint.dameg = data.getDame();
    }

    /** Con này đã ra map trong lượt hiện tại chưa. Chưa xoá thì không được ra lần nữa. */
    private boolean daRaLuotNay;

    /**
     * Đủ {@value #PHUT_CHO} phút thì CẢ HAI cùng ra; không chặn nhau như cặp Fu.
     *
     * <p>Cờ {@link #daRaLuotNay} là thứ giữ nhịp: con nào chết trước sẽ nằm nghỉ chờ con kia,
     * chứ không hồi sinh ngay (nếu chỉ so {@code lanKetThuc} thì mốc giờ cũ đã quá 10 phút,
     * con chết trước sẽ ra lại liên tục trong lúc con kia còn đứng ngoài map).
     */
    @Override
    public void rest() {
        BossSieuThanGod kia = conKia();
        if (daRaLuotNay) {
            boolean kiaDaXong = kia == null || kia == this
                    || (kia.bossStatus == BossStatus.REST && kia.daRaLuotNay);
            if (kiaDaXong) {
                // Cả hai đã rời map -> chốt lượt, bấm giờ cho lượt kế tiếp.
                lanKetThuc = System.currentTimeMillis();
                this.daRaLuotNay = false;
                if (kia != null && kia != this) {
                    kia.daRaLuotNay = false;
                }
            }
            return;
        }
        if (Util.canDoWithTime(lanKetThuc, PHUT_CHO * 60_000L)) {
            this.daRaLuotNay = true;
            this.changeStatus(BossStatus.RESPAWN);
        }
    }

    /**
     * Bốc map sao cho KHÁC map con kia đang đứng. Thử tối đa 12 lần rồi mới chịu thua —
     * ba map mà bốc ngẫu nhiên thì 12 lần hụt hết là gần như không thể.
     */
    @Override
    public Zone getMapJoin() {
        BossSieuThanGod kia = conKia();
        int mapKia = -1;
        if (kia != null && kia != this && kia.zone != null && kia.zone.map != null) {
            mapKia = kia.zone.map.mapId;
        }
        int[] maps = this.data[this.currentLevel].getMapJoin();
        for (int i = 0; i < 12; i++) {
            Zone z = MapService.gI().getMapWithRandZone(maps[Util.nextInt(0, maps.length - 1)]);
            if (z != null && z.map != null && z.map.mapId != mapKia) {
                return z;
            }
        }
        return super.getMapJoin();
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
