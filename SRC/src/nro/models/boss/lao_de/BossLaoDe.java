package nro.models.boss.lao_de;

import java.util.ArrayList;
import java.util.List;
import nro.models.boss.Boss;
import nro.models.boss.BossID;
import nro.models.boss.BossesData;
import nro.models.consts.BossStatus;
import nro.models.consts.ConstPlayer;
import nro.models.map.Zone;
import nro.models.map.service.ChangeMapService;
import nro.models.map.service.MapService;
import nro.models.player.Player;
import nro.models.services.EffectSkillService;
import nro.models.services.Service;
import nro.models.services.SkillService;
import nro.models.utils.Util;

/**
 * <b>Lão Dê Hồi Xuân</b> — boss duy nhất của server <b>không tự ra map</b>.
 *
 * <p>Muốn gặp lão thì phải có <b>Còi Triệu Hồi Lão Dê</b> (vật phẩm {@value #ID_COI}) rồi thổi
 * ngay tại chỗ mình đứng: lão hiện ra đúng khu đó cho cả khu xúm vào đánh.
 *
 * <p>Máu, sát thương, bộ chiêu và bảng rơi đồ đều bằng đúng bộ Siêu Thần God. Đồ rơi nằm ở
 * {@link nro.models.boss.drop.BangRoiBoss} chứ không viết cứng ở đây, nên sửa được từ cpanel.
 *
 * <p>Trò riêng của lão: cứ {@value #GIAY_NHIN_TROM} giây lại <b>nhìn trộm</b> một người trong
 * khu, người đó choáng {@value #GIAY_CHOANG} giây.
 */
public class BossLaoDe extends Boss {

    public static final int ID_COI = 2265;

    /** Số bản dựng sẵn — bấy nhiêu người có thể triệu hồi cùng lúc ở các khu khác nhau. */
    private static final int SO_BAN = 5;

    private static final int GIAY_NHIN_TROM = 10;
    private static final int GIAY_CHOANG = 2;

    private static final String[] CAU_NHIN_TROM = {
        "Đứng yên cho lão ngắm cái nào",
        "Ơ hay, con bé này xinh phết",
        "Lão chỉ nhìn thôi, không làm gì đâu",
        "Cái áo đó... mua ở đâu thế cháu?",
        "Hồi xuân rồi, mắt lão vẫn tinh lắm"
    };

    private static final List<BossLaoDe> DAN = new ArrayList<>();

    /** Dựng sẵn {@value #SO_BAN} bản, tất cả nằm nghỉ chờ người thổi còi. */
    public static void taoDan() throws Exception {
        if (!DAN.isEmpty()) {
            return;
        }
        for (int i = 0; i < SO_BAN; i++) {
            DAN.add(new BossLaoDe());
        }
    }

    /**
     * Thổi còi: gọi lão ra đúng khu {@code pl} đang đứng.
     *
     * <p>{@code synchronized} vì hàm này chạy trên luồng của người chơi: hai người thổi còi
     * cùng lúc mà không khoá thì cả hai cùng nhìn thấy một ông lão đang nghỉ, cùng đặt chỗ
     * hẹn, rồi một người mất còi mà chẳng gọi được ai.
     *
     * @return null nếu gọi được, còn không thì câu báo lỗi để hiện cho người thổi
     */
    public static synchronized String trieuHoi(Player pl) {
        if (pl == null || pl.zone == null || pl.zone.map == null) {
            return "Không thổi được ở đây";
        }
        int mapId = pl.zone.map.mapId;
        if (MapService.gI().isMapOffline(mapId) || MapService.gI().isMapPhoBan(mapId)
                || MapService.gI().isMapMaBu(mapId) || MapService.gI().isMapBlackBallWar(mapId)
                || MapService.gI().isMapDoanhTrai(mapId) || MapService.gI().isMapBanDoKhoBau(mapId)
                || mapId == 140 || mapId == 111) {
            return "Lão không vào chỗ này đâu, ra map thường mà thổi";
        }
        for (BossLaoDe laoDe : DAN) {
            if (laoDe.zone == pl.zone && laoDe.bossStatus != BossStatus.REST) {
                return "Lão đang ở ngay đây rồi, thổi nữa lão giận";
            }
        }
        for (BossLaoDe laoDe : DAN) {
            if (laoDe.bossStatus == BossStatus.REST) {
                laoDe.zoneFinal = pl.zone;
                laoDe.xTrieuHoi = pl.location.x;
                laoDe.changeStatus(BossStatus.RESPAWN);
                return null;
            }
        }
        return "Cả 5 ông lão đang bận hết rồi, lát nữa thổi lại";
    }

    /** Toạ độ ngang của người thổi còi, để lão hiện ngay cạnh chứ không rơi tuốt cuối map. */
    private int xTrieuHoi;
    private long lanNhinTrom;

    private BossLaoDe() throws Exception {
        super(BossID.LAO_DE_HOI_XUAN, BossesData.LAO_DE_HOI_XUAN);
        // Đặt sẵn để cpanel tab "Boss" có cái mà hiện trước lần ra map đầu tiên.
        this.name = BossesData.LAO_DE_HOI_XUAN.getName();
        this.nPoint.hpg = BossesData.LAO_DE_HOI_XUAN.getHp()[0];
        this.nPoint.hpMax = BossesData.LAO_DE_HOI_XUAN.getHp()[0];
        this.nPoint.dameg = BossesData.LAO_DE_HOI_XUAN.getDame();
    }

    /** Không bao giờ tự ra map. Chỉ {@link #trieuHoi(Player)} mới gọi được lão. */
    @Override
    public void rest() {
    }

    /** Ra đúng khu người thổi còi đang đứng, ngay cạnh người đó. */
    @Override
    public void joinMap() {
        Zone z = this.zoneFinal;
        if (z == null || z.map == null) {
            this.changeStatus(BossStatus.REST);
            return;
        }
        this.zone = z;
        int rong = z.map.mapWidth;
        int x = rong > 200 ? Math.max(100, Math.min(xTrieuHoi + Util.nextInt(-40, 40), rong - 100))
                : Util.nextInt(100);
        int y = z.map.yPhysicInTop(x, 100);
        ChangeMapService.gI().changeMap(this, this.zone, x, y);
        this.notifyJoinMap();
        this.changeStatus(BossStatus.CHAT_S);
        this.wakeupAnotherBossWhenAppear();
    }

    @Override
    public void leaveMap() {
        super.leaveMap();
        // Xoá chỗ hẹn, nếu không lần sau lão lại mò về đúng khu cũ dù ai gọi ở đâu.
        this.zoneFinal = null;
    }

    /** Cách đánh lấy y như bộ Siêu Thần God, thêm trò nhìn trộm. */
    @Override
    public void attack() {
        nhinTrom();
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

    /** Cứ {@value #GIAY_NHIN_TROM} giây nhìn trộm một người trong khu, người đó choáng. */
    private void nhinTrom() {
        if (!Util.canDoWithTime(lanNhinTrom, GIAY_NHIN_TROM * 1000L)) {
            return;
        }
        lanNhinTrom = System.currentTimeMillis();
        if (this.zone == null) {
            return;
        }
        try {
            Player nanNhan = this.zone.getRandomPlayerInMap();
            if (nanNhan == null || !nanNhan.isPl() || nanNhan.isDie()) {
                return;
            }
            EffectSkillService.gI().startStun(nanNhan, System.currentTimeMillis(), GIAY_CHOANG * 1000);
            this.chat(CAU_NHIN_TROM[Util.nextInt(0, CAU_NHIN_TROM.length - 1)]);
            Service.gI().sendThongBao(nanNhan, "Lão Dê vừa nhìn bạn một cái. Bạn đứng hình "
                    + GIAY_CHOANG + " giây.");
        } catch (Exception e) {
        }
    }

    @Override
    public synchronized int injured(Player plAtt, long damage, boolean piercing, boolean isMobAttack) {
        if (this.isDie()) {
            return 0;
        }
        if (!piercing && Util.isTrue(this.nPoint.tlNeDon, 1000)) {
            this.chat("Lão né đấy, nhanh không?");
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
