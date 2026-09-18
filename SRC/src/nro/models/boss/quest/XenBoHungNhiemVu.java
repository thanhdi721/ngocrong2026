package nro.models.boss.quest;

import nro.models.boss.BossDamageReduce;
import nro.models.boss.BossID;
import nro.models.boss.BossesData;

/**
 * NV 30 — "Xên bọ hung" bản nhiệm vụ (id -2100).
 *
 * <p>
 * Bản gốc {@code -100} (HP 50M/100M/150M, {@code dame/2}, nghỉ 30 phút) giữ nguyên
 * làm boss thế giới. Bản này chỉ mở ra cho người chơi đang ở nhiệm vụ 30 và không
 * rơi bất cứ thứ gì ngoài tín hiệu hoàn thành nhiệm vụ.
 */
public class XenBoHungNhiemVu extends QuestBoss {

    private static final int[] WORLD_BOSS = {BossID.XEN_BO_HUNG};
    private static final int[] TASK = {30};

    public XenBoHungNhiemVu() throws Exception {
        super(BossID.XEN_BO_HUNG_NV, WORLD_BOSS, TASK,
                BossesData.XEN_BO_HUNG_NV_1, BossesData.XEN_BO_HUNG_NV_2, BossesData.XEN_BO_HUNG_NV_3);
        this.damageReducePercentByLevel = BossDamageReduce.XEN_BO_HUNG_NV;
    }
}
