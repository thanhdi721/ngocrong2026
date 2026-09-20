package nro.models.boss.quest;

import nro.models.boss.BossDamageReduce;
import nro.models.boss.BossID;
import nro.models.boss.BossesData;

/**
 * NV 6 bước 1 — "Kẻ Thu Gom" (id -2000), 1 hình dạng.
 *
 * <p>
 * Boss cốt truyện hoàn toàn mới, KHÔNG có bản gốc trong game (khác 6 con
 * -2100…-2105 vốn là bản nhiệm vụ của boss thế giới). Vì vậy {@code worldBossIds}
 * rỗng: không có boss thế giới nào cần tránh đè lên, chỉ cần tránh hai bản
 * cùng id -2000 đứng chung một khu — việc đó {@link QuestBoss#isZoneOccupied}
 * đã lo sẵn.
 *
 * <p>
 * Boss luôn xuất hiện (chủ dự án chốt 18/09/2026): ai vào được map là đánh được,
 * chết xong nghỉ ngẫu nhiên 15–30 phút rồi hiện lại ở một khu bất kỳ.
 *
 * <p>
 * Rơi đồ: KHÔNG rơi gì ({@link QuestBoss#getQuestItemId} mặc định trả -1).
 * Phần thưởng của NV 6 trao qua {@code rewardDoneTask}, không qua boss.
 */
public class KeThuGom extends QuestBoss {

    /** Không có boss thế giới tương ứng. */
    private static final int[] WORLD_BOSS = {};

    /** Nhiệm vụ dùng boss này: số 6 ("Người thu gom"). Boss vẫn luôn xuất hiện cho mọi người. */
    private static final int[] TASK = {6};

    public KeThuGom() throws Exception {
        super(BossID.KE_THU_GOM, WORLD_BOSS, TASK,
                BossesData.KE_THU_GOM);
        this.damageReducePercentByLevel = BossDamageReduce.KE_THU_GOM;
    }
}
