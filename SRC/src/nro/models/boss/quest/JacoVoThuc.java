package nro.models.boss.quest;

import nro.models.boss.BossDamageReduce;
import nro.models.boss.BossID;
import nro.models.boss.BossesData;

/**
 * NV 15 bước 1 — "Jaco Mất Ký Ức" → "Jaco Vô Thức" (id -2001), 2 hình dạng.
 *
 * <p>
 * Boss cốt truyện hoàn toàn mới, không có bản gốc trong game, nên
 * {@code worldBossIds} rỗng (xem {@link KeThuGom}).
 *
 * <p>
 * Chỉ hình dạng CUỐI ({@code currentLevel == 1}) mới làm xong bước
 * {@code TASK_15_1} — điều kiện đó nằm trong
 * {@code TaskService.checkDoneTaskKillBoss}, ở đây không cần làm gì thêm:
 * {@link QuestBoss#reward} luôn gọi hàm ấy cho cả hai hình dạng và
 * {@code TaskService} tự lọc.
 *
 * <p>
 * Rơi đồ: KHÔNG rơi gì. 20a §NV 15 ghi rõ "Mảnh Ký Ức #2 trao qua
 * {@code rewardDoneTask}, không trao qua {@code Boss.reward}, tránh người chơi
 * nhặt hụt".
 */
public class JacoVoThuc extends QuestBoss {

    /** Không có boss thế giới tương ứng. */
    private static final int[] WORLD_BOSS = {};

    /** Nhiệm vụ dùng boss này: số 15 ("Người bạn đã quên"). Boss vẫn luôn xuất hiện cho mọi người. */
    private static final int[] TASK = {15};

    public JacoVoThuc() throws Exception {
        super(BossID.JACO_VO_THUC, WORLD_BOSS, TASK,
                BossesData.JACO_VO_THUC_1, BossesData.JACO_VO_THUC_2);
        this.damageReducePercentByLevel = BossDamageReduce.JACO_VO_THUC;
    }
}
