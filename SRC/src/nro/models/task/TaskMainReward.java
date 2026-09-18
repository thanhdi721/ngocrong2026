package nro.models.task;

import java.util.ArrayList;
import java.util.List;

/**
 * TUYẾN MỚI: một dòng của bảng `task_main_reward`.
 *
 * <p>
 * Trước đây phần thưởng nhiệm vụ chính nằm cứng trong `TaskService.rewardDoneTask`
 * (switch theo taskMain.id). Tuyến nhiệm vụ mới có 51 nhiệm vụ / 238 bước nên toàn bộ
 * phần thưởng chuyển sang bảng DB, nạp một lần lúc khởi động qua
 * {@link nro.models.database.TaskRewardDAO}.
 * </p>
 *
 * <ul>
 * <li>{@code subIndex == -1} : thưởng khi hoàn thành CẢ nhiệm vụ</li>
 * <li>{@code subIndex >= 0}  : thưởng khi xong đúng bước đó</li>
 * <li>{@code gender == -1}   : áp dụng cho cả 3 hành tinh (dòng duy nhất chứa sm/tn)</li>
 * <li>{@code gender 0/1/2}   : chỉ Trái Đất / Namếc / Xayda, chỉ chứa vật phẩm</li>
 * </ul>
 *
 * @author TUYẾN MỚI
 */
public class TaskMainReward {

    /**
     * Một mục vật phẩm trong cột `items` — JSON [[itemId, soLuong, [[optId, param], ...]], ...]
     */
    public static class RewardItem {

        public short templateId;
        public int quantity;
        /**
         * mỗi phần tử = {optionTemplateId, param}
         */
        public List<int[]> options = new ArrayList<>();

        public RewardItem(short templateId, int quantity) {
            this.templateId = templateId;
            this.quantity = quantity;
        }
    }

    public int taskId;
    public int subIndex;
    public byte gender;

    public long sm;
    public long tn;
    public long gold;
    public long gem;
    public long ruby;

    public final List<RewardItem> items = new ArrayList<>();

    public String text = "";

}
