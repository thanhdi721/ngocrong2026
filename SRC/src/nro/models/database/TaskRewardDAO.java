package nro.models.database;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import nro.models.data.LocalManager;
import nro.models.task.TaskMainReward;
import nro.models.utils.Logger;
import org.json.simple.JSONArray;
import org.json.simple.JSONValue;

/**
 * TUYẾN MỚI: DAO nạp bảng `task_main_reward` một lần lúc khởi động.
 *
 * <p>
 * Thay cho `switch` hardcode trong `TaskService.rewardDoneTask`. Cách nạp bám theo
 * cách `Manager` nạp các bảng template khác: một `PreparedStatement`, đọc hết vào bộ nhớ,
 * sau đó tra bằng `HashMap` trong lúc chơi (không đụng DB nữa).
 * </p>
 *
 * <p>
 * Khóa tra = {@code taskId * 1000 + (subIndex + 1)} — subIndex hợp lệ là -1..238 nên
 * không có va chạm khóa.
 * </p>
 *
 * @author TUYẾN MỚI
 */
public class TaskRewardDAO {

    private static final Map<Integer, List<TaskMainReward>> REWARDS = new HashMap<>();

    private static int loadedRows = 0;

    private static int key(int taskId, int subIndex) {
        return taskId * 1000 + (subIndex + 1);
    }

    public static int getLoadedRows() {
        return loadedRows;
    }

    public static boolean isLoaded() {
        return loadedRows > 0;
    }

    /**
     * Nạp bằng kết nối riêng (dùng khi không có sẵn Connection).
     */
    public static void load() {
        try (Connection con = LocalManager.getConnection()) {
            load(con);
        } catch (Exception e) {
            Logger.logException(TaskRewardDAO.class, e, "Lỗi nạp task_main_reward");
        }
    }

    /**
     * Nạp bằng Connection sẵn có của `Manager`.
     */
    public static void load(Connection con) {
        REWARDS.clear();
        loadedRows = 0;
        if (con == null) {
            return;
        }
        try (PreparedStatement ps = con.prepareStatement(
                "select task_id, sub_index, gender, sm, tn, gold, gem, ruby, items, text "
                + "from task_main_reward order by task_id, sub_index, gender")) {
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    TaskMainReward reward = new TaskMainReward();
                    reward.taskId = rs.getInt("task_id");
                    reward.subIndex = rs.getInt("sub_index");
                    reward.gender = rs.getByte("gender");
                    reward.sm = rs.getLong("sm");
                    reward.tn = rs.getLong("tn");
                    reward.gold = rs.getLong("gold");
                    reward.gem = rs.getLong("gem");
                    reward.ruby = rs.getLong("ruby");
                    reward.text = rs.getString("text");
                    if (reward.text == null) {
                        reward.text = "";
                    }
                    parseItems(reward, rs.getString("items"));
                    REWARDS.computeIfAbsent(key(reward.taskId, reward.subIndex),
                            k -> new ArrayList<>()).add(reward);
                    loadedRows++;
                }
            }
        } catch (Exception e) {
            // Bảng chưa được import (server cũ) -> chạy tiếp, TaskService tự bỏ qua phần thưởng.
            Logger.logException(TaskRewardDAO.class, e, "Lỗi nạp task_main_reward");
        }
        Logger.success(Logger.PURPLE + "Successfully loaded task main reward (" + loadedRows + ")\n");
    }

    /**
     * Cột `items` là JSON: [[itemId, soLuong, [[optionId, param], ...]], ...]
     */
    private static void parseItems(TaskMainReward reward, String json) {
        if (json == null || json.trim().isEmpty()) {
            return;
        }
        try {
            Object parsed = JSONValue.parse(json);
            if (!(parsed instanceof JSONArray)) {
                return;
            }
            JSONArray arr = (JSONArray) parsed;
            for (Object o : arr) {
                if (!(o instanceof JSONArray)) {
                    continue;
                }
                JSONArray row = (JSONArray) o;
                if (row.size() < 2) {
                    continue;
                }
                short tempId = (short) toInt(row.get(0));
                int quantity = toInt(row.get(1));
                if (quantity <= 0) {
                    quantity = 1;
                }
                TaskMainReward.RewardItem item = new TaskMainReward.RewardItem(tempId, quantity);
                if (row.size() >= 3 && row.get(2) instanceof JSONArray) {
                    for (Object oo : (JSONArray) row.get(2)) {
                        if (oo instanceof JSONArray && ((JSONArray) oo).size() >= 2) {
                            JSONArray opt = (JSONArray) oo;
                            item.options.add(new int[]{toInt(opt.get(0)), toInt(opt.get(1))});
                        }
                    }
                }
                reward.items.add(item);
            }
        } catch (Exception e) {
            Logger.logException(TaskRewardDAO.class, e, "Lỗi đọc cột items của task_main_reward");
        }
    }

    private static int toInt(Object o) {
        if (o instanceof Number) {
            return ((Number) o).intValue();
        }
        try {
            return Integer.parseInt(String.valueOf(o).trim());
        } catch (Exception e) {
            return 0;
        }
    }

    /**
     * Mọi dòng thưởng của (taskId, subIndex) — gồm cả dòng gender = -1 lẫn dòng theo hành tinh.
     * Nơi gọi phải tự lọc {@code gender IN (-1, player.gender)}.
     */
    public static List<TaskMainReward> get(int taskId, int subIndex) {
        List<TaskMainReward> list = REWARDS.get(key(taskId, subIndex));
        return list == null ? Collections.emptyList() : list;
    }

    /**
     * Các dòng áp dụng cho đúng một hành tinh: gender = -1 hoặc gender = genderPlayer.
     */
    public static List<TaskMainReward> get(int taskId, int subIndex, byte genderPlayer) {
        List<TaskMainReward> all = get(taskId, subIndex);
        if (all.isEmpty()) {
            return all;
        }
        List<TaskMainReward> list = new ArrayList<>(2);
        for (TaskMainReward r : all) {
            if (r.gender == -1 || r.gender == genderPlayer) {
                list.add(r);
            }
        }
        return list;
    }

}
