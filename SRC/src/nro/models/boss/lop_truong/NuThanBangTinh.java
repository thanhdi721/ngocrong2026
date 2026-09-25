package nro.models.boss.lop_truong;

import nro.models.boss.Boss;
import nro.models.boss.BossData;
import nro.models.boss.BossID;
import nro.models.consts.BossStatus;
import nro.models.consts.ConstPlayer;
import nro.models.map.Zone;
import nro.models.map.service.ChangeMapService;
import nro.models.player.Player;

/**
 * Nữ Thần Băng Tinh — nhân vật nữ đứng giữa map trong màn "Lốp Trưởng".
 * Không đánh ai, không bị đánh, chỉ nói chuyện và cổ vũ. Hình lấy từ cải trang 2079.
 */
public class NuThanBangTinh extends Boss {

    /**
     * Part BẢN NHỎ của Nữ Thần Băng Tinh (patch 60, bằng 55% cải trang 2079).
     *
     * <p>Client vẽ bong bóng thoại ở độ cao cố định phía trên mốc chân, nên dùng hình cải
     * trang gốc (cao gần gấp đôi người thường) thì bóng thoại rơi xuống ngang chân.
     */
    private static final short[] HINH = {2367, 2368, 2369};

    public NuThanBangTinh() throws Exception {
        super(BossID.NU_THAN_BANG_TINH, true, true, new BossData(
                "Nữ Thần Băng Tinh",
                ConstPlayer.TRAI_DAT,
                new short[]{HINH[0], HINH[1], HINH[2], -1, -1, -1},
                0,                                   // không gây sát thương
                new int[]{1_000_000_000},            // máu chỉ để không chết vì lạc đạn
                new int[]{102},
                new int[][]{},                       // không có kỹ năng
                new String[]{}, new String[]{}, new String[]{},
                Integer.MAX_VALUE / 1000));          // không tự hồi sinh, do điều phối gọi
    }

    /** Điều phối gọi khi cặp Lốp Trưởng ra map. */
    public void raMap(Zone zone) {
        this.zoneFinal = zone;
        this.currentLevel = -1;
        this.changeStatus(BossStatus.RESPAWN);
    }

    /** Điều phối gọi khi hết lượt. */
    public void veNha() {
        if (this.zone != null) {
            ChangeMapService.gI().exitMap(this);
        }
        this.zoneFinal = null;
        this.changeStatus(BossStatus.REST);
    }

    /** Vào map là đứng ngay giữa, không rơi ngẫu nhiên như boss thường. */
    @Override
    public void joinMapByZone(Zone zone) {
        if (zone == null) {
            return;
        }
        this.zone = zone;
        int[] cho = LopTruong.choDung(zone, 0);
        ChangeMapService.gI().changeMap(this, zone, cho[0], cho[1]);
    }

    @Override
    public void rest() {
        // chỉ ra map khi phần điều phối gọi raMap()
    }

    @Override
    public void attack() {
        // đứng yên, không đánh ai
    }

    @Override
    public void active() {
        // không chuyển sang chế độ đánh nhau
    }

    @Override
    public synchronized int injured(Player plAtt, long damage, boolean piercing, boolean isMobAttack) {
        return 0;   // bất tử, đánh vào không ăn thua
    }

    @Override
    public void leaveMap() {
        veNha();
    }

    @Override
    public void reward(Player plKill) {
        // không rơi gì
    }
}
