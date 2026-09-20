package nro.models.boss.heart;

import nro.models.boss.BossDamageReduce;
import nro.models.boss.BossID;
import nro.models.boss.BossesData;
import nro.models.boss.quest.QuestBoss;

/**
 * BOSS MỚI — <b>Heart (-108108)</b>, phản diện chính của tuyến nhiệm vụ mới.
 * Đặc tả gốc: docs/2-thiet-ke-nhiem-vu-moi/20c-chi-tiet-nhiem-vu-32-47.md §5.
 *
 * <p>
 * Bốn hình dạng, dùng chung một id, đổi map giữa chừng:
 * <table>
 * <tr><th>currentLevel</th><th>Tên</th><th>Map</th><th>Dùng ở</th></tr>
 * <tr><td>0</td><td>Heart</td><td>166 Phòng thí nghiệm Myuu</td><td>NV 46 bước 1</td></tr>
 * <tr><td>1</td><td>Heart Hư Không</td><td>145 Võ Đài Siêu Cấp</td><td>NV 46 bước 3</td></tr>
 * <tr><td>2</td><td>Heart Toàn Ký</td><td>145 Võ Đài Siêu Cấp</td><td>NV 46 bước 4</td></tr>
 * <tr><td>3</td><td>Hư Không Vô Danh</td><td>145 hoặc 155</td><td>NV 47 bước 5 / NV 50 bước 5</td></tr>
 * </table>
 *
 * <p>
 * Tạo hình mượn nguyên {@code npc_template} id 108 (head 2109 / body 2110 / leg 2111),
 * nên <b>không cần thêm tài nguyên client</b>.
 *
 * <p>
 * Ba điểm đáng chú ý trong cách cài đặt:
 * <ul>
 * <li>Hình dạng 0 ở map 166, hình dạng 1–2 ở map 145. {@link QuestBoss#joinMap()}
 * kiểm tra map của {@code zoneFinal} có nằm trong {@code mapJoin} của hình dạng
 * hiện tại hay không; nếu không thì <b>giữ nguyên trạng thái</b> và chờ, đúng bằng
 * quãng thời gian người chơi đi từ 166 sang 145 (NV 46 bước 2).</li>
 * <li>Hình dạng 4 có {@code mapJoin = {145, 155}}. Map thật do <b>người chơi</b>
 * quyết định: ai đang ở NV 47 thì gặp Heart ở 145, ai chọn nhánh NV 50 thì gặp ở 155.
 * Không cần code chọn map riêng — boss đi vào đúng khu của người chơi hợp lệ.</li>
 * <li>{@code isNotifyDisabled = true} (đặt trong {@link QuestBoss}) để không bắn
 * thông báo toàn server 4 lần liên tiếp mỗi khi đổi hình dạng.</li>
 * </ul>
 */
public class Heart extends QuestBoss {

    /** NV 46: gặp Heart ở hình dạng 1, 2, 3. */
    private static final int[] TASK_HEART = {46};

    /** NV 47 (nhánh A) và NV 50 (nhánh B): gặp "Hư Không Vô Danh". */
    private static final int[] TASK_VO_DANH = {47, 50};

    /** Heart không có bản gốc ở thế giới — không cần tránh boss nào. */
    private static final int[] NO_WORLD_BOSS = {};

    /** Ống nghiệm Myuu — docs/4-trien-khai/25-bang-id-vat-pham-moi.md §3.5. */
    private static final int ONG_NGHIEM_MYUU = 2029;

    public Heart() throws Exception {
        super(BossID.HEART, NO_WORLD_BOSS, TASK_HEART,
                BossesData.HEART, BossesData.HEART_2, BossesData.HEART_3, BossesData.HEART_4);
        this.damageReducePercentByLevel = BossDamageReduce.HEART;
    }

    @Override
    protected int[] getTaskIdRequired(int level) {
        return level >= 3 ? TASK_VO_DANH : TASK_HEART;
    }

    @Override
    protected int getQuestItemId(int level) {
        // NV 46 bước 4: hạ "Heart Toàn Ký" (hình dạng 3, currentLevel = 2) -> Ống nghiệm Myuu.
        // Các hình dạng còn lại không rơi gì: Heart nằm trong bước nhiệm vụ bắt buộc.
        return level == 2 ? ONG_NGHIEM_MYUU : -1;
    }
}
