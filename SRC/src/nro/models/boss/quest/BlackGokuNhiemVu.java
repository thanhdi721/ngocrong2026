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
 *
 * <p>
 * doc 42: rơi 100% Nhẫn thời không sai lệch (992) cho người kết liễu đang ở {@code TASK_38_4}
 * — khai báo trong bảng chung {@code nro.models.task.QuestDrop} (móc ở
 * {@code TaskService.checkDoneTaskKillBoss}), KHÔNG qua {@code getQuestItemId} vì chỉ được rơi
 * khi đang ở đúng bước. Đòn kết liễu Super Black Goku (xong 38.3 → sang 38.4) rơi luôn.
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
