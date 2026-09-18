package nro.models.boss.quest;

import nro.models.boss.BossDamageReduce;
import nro.models.boss.BossID;
import nro.models.boss.BossesData;

/**
 * NV 34 — "Cooler" bản nhiệm vụ (id -2101).
 *
 * <p>
 * Bản gốc {@code -29} (HP 200M/500M, nghỉ 30 phút, rơi đồ Thần Linh 5% + đồ cấp 5%
 * + Ngọc Rồng 80%) giữ nguyên làm boss thế giới — đây vẫn là nguồn đồ.
 * Bản nhiệm vụ này không rơi gì cả.
 */
public class CoolerNhiemVu extends QuestBoss {

    private static final int[] WORLD_BOSS = {BossID.COOLER};
    private static final int[] TASK = {34};

    public CoolerNhiemVu() throws Exception {
        super(BossID.COOLER_NV, WORLD_BOSS, TASK,
                BossesData.COOLER_NV_1, BossesData.COOLER_NV_2);
        this.damageReducePercentByLevel = BossDamageReduce.COOLER_NV;
    }
}
