package nro.models.boss.quest;

import nro.models.boss.BossDamageReduce;
import nro.models.boss.BossID;
import nro.models.boss.BossesData;

/**
 * NV 37 — "Mabư 14h" bản nhiệm vụ (id -2102), 5 hình dạng.
 *
 * <p>
 * Bản gốc {@code -214} là boss phó bản khung 14h: ăn người chơi 20% mỗi 10 giây,
 * né 10%, trần 30 triệu sát thương mỗi đòn, và <b>hình dạng 5 chỉ chết bằng Quả cầu
 * kênh khí</b>. Ba cơ chế đó biến bước nhiệm vụ thành ngõ cụt cho phần lớn người chơi,
 * nên bản nhiệm vụ bỏ hết: đánh thường là hạ được, và không phụ thuộc khung giờ.
 */
public class MabuNhiemVu extends QuestBoss {

    private static final int[] WORLD_BOSS = {BossID.MABU, BossID.SUPERBU};
    private static final int[] TASK = {37};

    public MabuNhiemVu() throws Exception {
        super(BossID.MABU_14H_NV, WORLD_BOSS, TASK,
                BossesData.MABU_NV_1, BossesData.MABU_NV_2, BossesData.MABU_NV_3,
                BossesData.MABU_NV_4, BossesData.MABU_NV_5);
        this.damageReducePercentByLevel = BossDamageReduce.MABU_NV;
    }
}
