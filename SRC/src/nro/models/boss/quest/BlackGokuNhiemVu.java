package nro.models.boss.quest;

import nro.models.boss.BossDamageReduce;
import nro.models.boss.BossID;
import nro.models.boss.BossesData;

/**
 * NV 38 — "Black Goku" bản nhiệm vụ (id -2103).
 *
 * <p>
 * Bản gốc {@code -203} (HP 500M + 2 tỉ, {@code dame/2} ở hình dạng 2) giữ nguyên.
 * Bản nhiệm vụ bỏ luôn Tái tạo năng lượng để trận đánh không kéo dài vô tận
 * ở mốc sức mạnh 7 tỉ của NV 38.
 */
public class BlackGokuNhiemVu extends QuestBoss {

    private static final int[] WORLD_BOSS = {BossID.BLACK_GOKU};
    private static final int[] TASK = {38};

    public BlackGokuNhiemVu() throws Exception {
        super(BossID.BLACK_GOKU_NV, WORLD_BOSS, TASK,
                BossesData.BLACK_GOKU_NV_1, BossesData.BLACK_GOKU_NV_2);
        this.damageReducePercentByLevel = BossDamageReduce.BLACK_GOKU_NV;
    }
}
