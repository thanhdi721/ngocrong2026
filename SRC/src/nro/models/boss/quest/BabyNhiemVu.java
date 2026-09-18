package nro.models.boss.quest;

import nro.models.boss.BossDamageReduce;
import nro.models.boss.BossID;
import nro.models.boss.BossesData;

/**
 * NV 39 — "Baby" bản nhiệm vụ (id -2104), 3 hình dạng.
 *
 * <p>
 * Bản gốc {@code -925} là boss khó nhất server (17,1 tỉ HP hiệu dụng vì
 * {@code dame × 0,7 / 2}). Bản nhiệm vụ bỏ hệ số giảm sát thương và hạ HP về
 * 1,9 triệu mỗi hình dạng; bản gốc giữ nguyên cả HP lẫn 1% cải trang Baby.
 */
public class BabyNhiemVu extends QuestBoss {

    private static final int[] WORLD_BOSS = {BossID.BABY};
    private static final int[] TASK = {39};

    public BabyNhiemVu() throws Exception {
        super(BossID.BABY_NV, WORLD_BOSS, TASK,
                BossesData.BABY_NV_1, BossesData.BABY_NV_2, BossesData.BABY_NV_3);
        this.damageReducePercentByLevel = BossDamageReduce.BABY_NV;
    }
}
