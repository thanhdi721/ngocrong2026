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

    /** Part của cải trang Nữ Thần Băng Tinh (patch 40 + các patch chỉnh sau). */
    private static final short[] HINH = {2233, 2236, 2237};

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

    @Override
    public void rest() {
        // chỉ ra map khi phần điều phối gọi raMap()
    }

    private boolean daDungGiua;

    @Override
    public void update() {
        super.update();
        // Đứng giữa map cho hai anh kia đánh nhau xung quanh.
        if (this.zone != null && !daDungGiua) {
            int x = this.zone.map.mapWidth / 2;
            int y = this.zone.map.yPhysicInTop(x, 0);
            this.moveTo(x, y);
            daDungGiua = true;
        } else if (this.zone == null) {
            daDungGiua = false;
        }
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
