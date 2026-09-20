package nro.models.boss.quest;

import nro.models.boss.BossDamageReduce;
import nro.models.boss.BossID;
import nro.models.boss.BossesData;

/**
 * NV 42 — "Cumber" bản nhiệm vụ (id -2105).
 *
 * <p>
 * Bản gốc {@code -203999} giữ nguyên HP 500M + 2 tỉ và bảng rơi đồ.
 * Cả hai cùng nằm ở map 155 Hành tinh ngục tù nhưng không bao giờ chung một khu
 * (xem {@link QuestBoss#isZoneOccupied}).
 */
public class CumberNhiemVu extends QuestBoss {

    private static final int[] WORLD_BOSS = {BossID.CUMBER};
    private static final int[] TASK = {42};

    public CumberNhiemVu() throws Exception {
        super(BossID.CUMBER_NV, WORLD_BOSS, TASK,
                BossesData.CUMBER_NV_1, BossesData.CUMBER_NV_2);
        this.damageReducePercentByLevel = BossDamageReduce.CUMBER_NV;
    }
}
