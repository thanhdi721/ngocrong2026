package nro.models.cpanel;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;
import nro.models.cpanel.CPanel.CPanelException;
import nro.models.cpanel.CPanel.NeedConfirmException;
import nro.models.data.LocalManager;
import nro.models.item.Item;
import nro.models.map.service.ItemMapService;
import nro.models.player.Inventory;
import nro.models.player.Player;
import nro.models.player_system.Template;
import nro.models.server.Client;
import nro.models.server.Manager;
import nro.models.services.InventoryService;
import nro.models.services.ItemService;
import nro.models.services.Service;
import nro.models.services.TaskService;
import nro.models.task.SubTaskMain;
import nro.models.task.TaskMain;
import org.json.simple.JSONArray;
import org.json.simple.JSONValue;

/**
 * Thao tác trên nhân vật (bảng {@code player}). Chỉ gọi trên luồng nền của cpanel.
 *
 * QUY TẮC CHỐNG GHI ĐÈ:
 * - Nhân vật ĐANG ONLINE: chỉ sửa đối tượng Player trong bộ nhớ (dùng hàm có sẵn của
 *   game), hệ thống sẽ tự lưu (tự lưu 5 phút / khi thoát / khi bảo trì). KHÔNG ghi DB,
 *   vì lần lưu sau của game sẽ đè mất.
 * - Nhân vật OFFLINE: sửa DB trong 1 transaction có SELECT ... FOR UPDATE; kiểm tra lại
 *   trạng thái online SAU khi khóa dòng; nếu nhân vật vừa vào game thì hủy transaction và
 *   chuyển sang đường "online". Nếu account cho thấy phiên trước chưa thoát xong
 *   (last_time_login > last_time_logout: đang lưu dữ liệu lúc thoát, hoặc server từng sập)
 *   thì hỏi admin xác nhận trước khi ghi.
 */
final class CharacterOps {

    static final int GOLD = 0, GEM = 1, RUBY = 2;
    static final String[] CURRENCY_NAME = {"vàng", "ngọc", "hồng ngọc"};

    private CharacterOps() {
    }

    static final class CharRow {

        long id;
        String name;
        int gender;
        int accountId;
        String username;
        long power;
        long gold;
        long gem;
        long ruby;
        int taskId;
        int taskIndex;
        int taskCount;
        boolean online;
        String map;
    }

    // =====================================================================
    // ĐỌC
    // =====================================================================
    static List<CharRow> search(String name) throws SQLException {
        List<CharRow> list = new ArrayList<>();
        String sql = "SELECT p.id, p.name, p.gender, p.account_id, a.username, p.data_point, p.data_inventory, p.data_task"
                + " FROM player p LEFT JOIN account a ON a.id = p.account_id"
                + " WHERE p.name LIKE ? ORDER BY p.id LIMIT 100";
        try (Connection con = LocalManager.getConnection();
                PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, "%" + AccountDao.likeEscape(name == null ? "" : name.trim()) + "%");
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    list.add(readRow(rs));
                }
            }
        }
        return list;
    }

    static CharRow load(long playerId) throws Exception {
        String sql = "SELECT p.id, p.name, p.gender, p.account_id, a.username, p.data_point, p.data_inventory, p.data_task"
                + " FROM player p LEFT JOIN account a ON a.id = p.account_id WHERE p.id = ?";
        try (Connection con = LocalManager.getConnection();
                PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setLong(1, playerId);
            try (ResultSet rs = ps.executeQuery()) {
                if (!rs.next()) {
                    throw new CPanelException("Không tìm thấy nhân vật id " + playerId);
                }
                return readRow(rs);
            }
        }
    }

    private static CharRow readRow(ResultSet rs) throws SQLException {
        CharRow r = new CharRow();
        r.id = rs.getLong("id");
        r.name = rs.getString("name");
        r.gender = rs.getInt("gender");
        r.accountId = rs.getInt("account_id");
        r.username = rs.getString("username");
        JSONArray point = parseArray(rs.getString("data_point"));
        JSONArray inv = parseArray(rs.getString("data_inventory"));
        JSONArray task = parseArray(rs.getString("data_task"));
        r.power = getLong(point, 1);
        r.gold = getLong(inv, 0);
        r.gem = getLong(inv, 1);
        r.ruby = getLong(inv, 2);
        r.taskId = (int) getLong(task, 0);
        r.taskIndex = (int) getLong(task, 1);
        r.taskCount = (int) getLong(task, 2);
        // Đang online -> số liệu trong bộ nhớ mới là số thật (DB có thể cũ tới 5 phút)
        Player p = Client.gI().getPlayer(r.id);
        if (p != null && p.isPl()) {
            r.online = true;
            try {
                r.power = p.nPoint.power;
                r.gold = p.inventory.gold;
                r.gem = p.inventory.gem;
                r.ruby = p.inventory.ruby;
                if (p.playerTask != null && p.playerTask.taskMain != null) {
                    r.taskId = p.playerTask.taskMain.id;
                    r.taskIndex = p.playerTask.taskMain.index;
                    r.taskCount = p.playerTask.taskMain.subTasks.get(r.taskIndex).count;
                }
                if (p.zone != null && p.zone.map != null) {
                    r.map = p.zone.map.mapName + " (" + p.zone.map.mapId + ") khu " + p.zone.zoneId;
                }
            } catch (Exception ignored) {
            }
        }
        return r;
    }

    static String taskName(int id) {
        for (TaskMain tm : Manager.TASKS) {
            if (tm.id == id) {
                return tm.name;
            }
        }
        return "(không có trong task_main_template)";
    }

    // =====================================================================
    // XÁC ĐỊNH ONLINE
    // =====================================================================
    /**
     * Trả về Player đang online (đã nạp xong dữ liệu) hoặc null nếu offline.
     * Ném lỗi nếu nhân vật đang ở trạng thái chuyển tiếp (đang vào game / đang thoát).
     */
    static Player findOnline(long playerId) throws CPanelException {
        Player p = Client.gI().getPlayer(playerId);
        if (p == null) {
            for (Player x : Client.gI().getPlayersSnapshot()) {
                if (x.id == playerId && x.isPl()) {
                    p = x;
                    break;
                }
            }
        }
        if (p == null) {
            return null;
        }
        if (p.beforeDispose || p.isOffline) {
            throw new CPanelException("Nhân vật đang thoát game và được lưu dữ liệu. Thử lại sau vài giây.");
        }
        if (p.idMark == null || !p.idMark.isLoadedAllDataPlayer()) {
            throw new CPanelException("Nhân vật đang vào game (chưa nạp xong dữ liệu). Thử lại sau vài giây.");
        }
        return p;
    }

    private static boolean isOnlineNow(long playerId, int accountId) {
        if (Client.gI().getPlayer(playerId) != null) {
            return true;
        }
        if (accountId > 0 && Client.gI().getPlayerByUser(accountId) != null) {
            return true;
        }
        for (Player x : Client.gI().getPlayersSnapshot()) {
            if (x.id == playerId) {
                return true;
            }
        }
        return false;
    }

    /** Biến đổi giá trị 1 cột JSON; ném CPanelException để hủy. */
    interface Mutator {

        String apply(String oldValue) throws Exception;
    }

    private enum Offline {
        OK, ONLINE
    }

    /**
     * Sửa 1 cột JSON của nhân vật offline trong transaction.
     * @param column chỉ nhận hằng số trong code (không bao giờ là dữ liệu người dùng nhập)
     */
    private static Offline modifyOffline(long playerId, String column, boolean force, Mutator mutator) throws Exception {
        if (!column.equals("data_inventory") && !column.equals("data_task") && !column.equals("items_bag")) {
            throw new IllegalArgumentException("Cột không được phép: " + column);
        }
        try (Connection con = LocalManager.getConnection()) {
            con.setAutoCommit(false);
            try {
                String oldValue;
                int accountId;
                Timestamp login;
                Timestamp logout;
                try (PreparedStatement ps = con.prepareStatement("SELECT p." + column
                        + " AS v, p.account_id, a.last_time_login, a.last_time_logout"
                        + " FROM player p LEFT JOIN account a ON a.id = p.account_id WHERE p.id = ? FOR UPDATE")) {
                    ps.setLong(1, playerId);
                    try (ResultSet rs = ps.executeQuery()) {
                        if (!rs.next()) {
                            throw new CPanelException("Không tìm thấy nhân vật id " + playerId);
                        }
                        oldValue = rs.getString("v");
                        accountId = rs.getInt("account_id");
                        login = rs.getTimestamp("last_time_login");
                        logout = rs.getTimestamp("last_time_logout");
                    }
                }
                // Kiểm tra lại SAU khi đã khóa dòng
                if (isOnlineNow(playerId, accountId)) {
                    con.rollback();
                    return Offline.ONLINE;
                }
                if (!force && login != null && logout != null && login.after(logout)) {
                    con.rollback();
                    throw new NeedConfirmException("Tài khoản của nhân vật này có lần đăng nhập (" + login
                            + ") SAU lần thoát gần nhất (" + logout + ").\n"
                            + "Có thể nhân vật vừa thoát và game đang lưu dữ liệu, hoặc server từng bị sập.\n"
                            + "Nếu vừa có người thoát game, hãy chờ vài giây rồi làm lại.\n\n"
                            + "Vẫn ghi thẳng vào DB?");
                }
                String newValue = mutator.apply(oldValue);
                try (PreparedStatement ps = con.prepareStatement("UPDATE player SET " + column + " = ? WHERE id = ?")) {
                    ps.setString(1, newValue);
                    ps.setLong(2, playerId);
                    ps.executeUpdate();
                }
                con.commit();
                return Offline.OK;
            } catch (Exception e) {
                try {
                    con.rollback();
                } catch (SQLException ignored) {
                }
                throw e;
            } finally {
                try {
                    con.setAutoCommit(true);
                } catch (SQLException ignored) {
                }
            }
        }
    }

    private static String afterOffline(long playerId, String name, String msg) {
        CPanel.log(msg);
        // Nếu ngay sau khi ghi mà nhân vật lại online -> có thể game đã đọc bản cũ
        if (isOnlineNow(playerId, -1)) {
            String warn = "CẢNH BÁO: " + name + " vừa vào game ngay lúc ghi DB, thay đổi có thể bị ghi đè. Hãy kiểm tra lại.";
            CPanel.log(warn);
            return msg + "\n\n" + warn;
        }
        return msg;
    }

    // =====================================================================
    // CỘNG / TRỪ TIỀN
    // =====================================================================
    static String addCurrency(long playerId, String name, int kind, long delta, boolean force) throws Exception {
        Player p = findOnline(playerId);
        if (p == null) {
            Offline r = modifyOffline(playerId, "data_inventory", force, old -> {
                JSONArray arr = parseArray(old);
                while (arr.size() < 5) {
                    arr.add(0);
                }
                long cur = getLong(arr, kind);
                long nv = clampCurrency(kind, cur + delta);
                arr.set(kind, nv);
                return arr.toJSONString();
            });
            if (r == Offline.OK) {
                return afterOffline(playerId, name, "[OFFLINE/DB] " + (delta >= 0 ? "Cộng " : "Trừ ")
                        + CPanel.num(Math.abs(delta)) + " " + CURRENCY_NAME[kind] + " cho " + name + " (id " + playerId + ")");
            }
            p = findOnline(playerId); // vừa vào game -> đi đường online
            if (p == null) {
                throw new CPanelException("Nhân vật đang chuyển trạng thái online/offline. Thử lại sau vài giây.");
            }
        }
        long after;
        switch (kind) {
            case GOLD:
                p.inventory.gold = clampCurrency(GOLD, p.inventory.gold + delta);
                after = p.inventory.gold;
                break;
            case GEM:
                p.inventory.gem = (int) clampCurrency(GEM, (long) p.inventory.gem + delta);
                after = p.inventory.gem;
                break;
            default:
                p.inventory.ruby = (int) clampCurrency(RUBY, (long) p.inventory.ruby + delta);
                after = p.inventory.ruby;
                break;
        }
        Service.gI().sendMoney(p);
        Service.gI().sendThongBao(p, "Quản trị viên đã " + (delta >= 0 ? "cộng " : "trừ ")
                + CPanel.num(Math.abs(delta)) + " " + CURRENCY_NAME[kind]);
        String msg = "[ONLINE/BỘ NHỚ] " + (delta >= 0 ? "Cộng " : "Trừ ") + CPanel.num(Math.abs(delta)) + " "
                + CURRENCY_NAME[kind] + " cho " + p.name + " -> còn " + CPanel.num(after) + " (game sẽ tự lưu)";
        CPanel.log(msg);
        return msg;
    }

    static long clampCurrency(int kind, long v) {
        long max = kind == GOLD ? Inventory.LIMIT_GOLD : Integer.MAX_VALUE;
        return Math.max(0, Math.min(max, v));
    }

    // =====================================================================
    // ĐẶT LẠI NHIỆM VỤ
    // =====================================================================
    static String resetTask(long playerId, String name, int taskId, boolean force) throws Exception {
        boolean exists = false;
        for (TaskMain tm : Manager.TASKS) {
            if (tm.id == taskId) {
                exists = true;
                break;
            }
        }
        if (!exists) {
            throw new CPanelException("Nhiệm vụ id " + taskId + " không có trong task_main_template.");
        }
        Player p = findOnline(playerId);
        if (p == null) {
            Offline r = modifyOffline(playerId, "data_task", force, old -> {
                JSONArray arr = new JSONArray();
                arr.add(taskId);
                arr.add(0);
                arr.add(0);
                arr.add(System.currentTimeMillis());
                return arr.toJSONString();
            });
            if (r == Offline.OK) {
                return afterOffline(playerId, name, "[OFFLINE/DB] Đặt nhiệm vụ của " + name + " về NV " + taskId
                        + " bước 0 (không trao thưởng)");
            }
            p = findOnline(playerId);
            if (p == null) {
                throw new CPanelException("Nhân vật đang chuyển trạng thái online/offline. Thử lại sau vài giây.");
            }
        }
        // Tái dùng hàm có sẵn: đổi nhiệm vụ, xóa tiến độ, gửi lại cho client, KHÔNG trao thưởng
        TaskService.gI().switchTaskMain(p, taskId, 0);
        if (p.playerTask == null || p.playerTask.taskMain == null || p.playerTask.taskMain.id != taskId) {
            throw new CPanelException("Không đổi được nhiệm vụ cho " + p.name + ".");
        }
        String msg = "[ONLINE/BỘ NHỚ] Đặt nhiệm vụ của " + p.name + " về NV " + taskId + " bước 0 (game sẽ tự lưu)";
        CPanel.log(msg);
        return msg;
    }

    // =====================================================================
    // TẶNG VẬT PHẨM
    // =====================================================================
    static Template.ItemTemplate template(int itemId) throws CPanelException {
        if (itemId < 0 || itemId >= Manager.ITEM_TEMPLATES.size() || Manager.ITEM_TEMPLATES.get(itemId) == null) {
            throw new CPanelException("Không có vật phẩm id " + itemId + ".");
        }
        Template.ItemTemplate t = ItemService.gI().getTemplate(itemId);
        if (t == null || t.id != itemId) {
            throw new CPanelException("Không có vật phẩm id " + itemId + ".");
        }
        return t;
    }

    static String giveItem(long playerId, String name, int itemId, int quantity, boolean force) throws Exception {
        Template.ItemTemplate t = template(itemId);
        if (quantity <= 0) {
            throw new CPanelException("Số lượng phải lớn hơn 0.");
        }
        if (t.isUpToUp && quantity > 99_999_999) {
            throw new CPanelException("Số lượng tối đa 99.999.999.");
        }
        if (!t.isUpToUp && quantity > Inventory.MAX_ITEMS_BAG) {
            throw new CPanelException("Vật phẩm không cộng dồn: tối đa " + Inventory.MAX_ITEMS_BAG + " cái mỗi lần.");
        }
        Player p = findOnline(playerId);
        if (p == null) {
            if (isSpecialItem(t)) {
                throw new CPanelException("\"" + t.name + "\" là vật phẩm đặc biệt (tiền tệ / ngọc rồng / mở rộng ô)."
                        + "\nChỉ tặng được khi người chơi đang online, hoặc dùng nút Cộng vàng/ngọc.");
            }
            Offline r = modifyOffline(playerId, "items_bag", force, old -> addToBagJson(old, t, quantity));
            if (r == Offline.OK) {
                return afterOffline(playerId, name, "[OFFLINE/DB] Tặng " + quantity + " x " + t.name + " [" + itemId
                        + "] vào hành trang " + name);
            }
            p = findOnline(playerId);
            if (p == null) {
                throw new CPanelException("Nhân vật đang chuyển trạng thái online/offline. Thử lại sau vài giây.");
            }
        }
        // Đường online: tái dùng ItemService/InventoryService như lệnh admin "i" / form "Tặng vật phẩm"
        int given = 0;
        if (t.isUpToUp) {
            Item item = ItemService.gI().createNewItem((short) itemId, quantity);
            item.itemOptions.addAll(ItemService.gI().getListOptionItemShop((short) itemId));
            if (InventoryService.gI().addItemBag(p, item)) {
                given = quantity;
            }
        } else {
            if (InventoryService.gI().getCountEmptyBag(p) < quantity) {
                throw new CPanelException("Hành trang của " + p.name + " không đủ " + quantity + " ô trống.");
            }
            for (int i = 0; i < quantity; i++) {
                Item item = ItemService.gI().createNewItem((short) itemId, 1);
                item.itemOptions.addAll(ItemService.gI().getListOptionItemShop((short) itemId));
                if (!InventoryService.gI().addItemBag(p, item)) {
                    break;
                }
                given++;
            }
        }
        InventoryService.gI().sendItemBags(p);
        if (given == 0) {
            throw new CPanelException("Không thêm được vật phẩm (hành trang đầy hoặc vượt giới hạn).");
        }
        Service.gI().sendThongBao(p, "Bạn nhận được " + given + " " + t.name + " từ Quản trị viên");
        String msg = "[ONLINE/BỘ NHỚ] Tặng " + given + "/" + quantity + " x " + t.name + " [" + itemId + "] cho " + p.name
                + " (game sẽ tự lưu)";
        CPanel.log(msg);
        return msg;
    }

    private static boolean isSpecialItem(Template.ItemTemplate t) {
        ItemMapService ims = ItemMapService.gI();
        return t.type == 9 || t.type == 10 || t.type == 34
                || t.id == 517 || t.id == 518
                || ims.isBlackBall(t.id) || ims.isNamecBall(t.id) || ims.isNamecBallStone(t.id);
    }

    /** Thêm vật phẩm vào chuỗi JSON items_bag đúng định dạng PlayerDAO/MrBlue đang dùng. */
    @SuppressWarnings("unchecked")
    private static String addToBagJson(String old, Template.ItemTemplate t, int quantity) throws CPanelException {
        JSONArray bag = parseArray(old);
        // option: giống lệnh "i" (option shop mặc định), rỗng thì [73,0] như InventoryService.addItemList
        JSONArray options = new JSONArray();
        for (Item.ItemOption io : ItemService.gI().getListOptionItemShop(t.id)) {
            JSONArray opt = new JSONArray();
            opt.add(io.optionTemplate.id);
            opt.add(io.param);
            options.add(opt.toJSONString());
        }
        if (options.isEmpty()) {
            JSONArray opt = new JSONArray();
            opt.add(73);
            opt.add(0);
            options.add(opt.toJSONString());
        }
        String optStr = options.toJSONString();
        long now = System.currentTimeMillis();

        List<Integer> empty = new ArrayList<>();
        for (int i = 0; i < bag.size(); i++) {
            JSONArray it = parseArray(String.valueOf(bag.get(i)));
            int id = it.isEmpty() ? -1 : (int) getLong(it, 0);
            if (id == -1) {
                empty.add(i);
            } else if (t.isUpToUp && id == t.id && it.size() >= 3
                    && normalizeOpt(String.valueOf(it.get(2))).equals(normalizeOpt(optStr))) {
                long q = getLong(it, 1) + quantity;
                if (q < 100_000_000L) {
                    it.set(1, q);
                    bag.set(i, it.toJSONString());
                    return bag.toJSONString();
                }
            }
        }
        int need = t.isUpToUp ? 1 : quantity;
        if (empty.size() < need) {
            throw new CPanelException("Hành trang không đủ " + need + " ô trống (còn " + empty.size() + ").");
        }
        for (int k = 0; k < need; k++) {
            JSONArray it = new JSONArray();
            it.add((int) t.id);
            it.add(t.isUpToUp ? quantity : 1);
            it.add(optStr);
            it.add(now);
            bag.set(empty.get(k), it.toJSONString());
        }
        return bag.toJSONString();
    }

    private static String normalizeOpt(String s) {
        return s.replace("\"", "").replace("\\", "").replace(" ", "");
    }

    // =====================================================================
    // JSON
    // =====================================================================
    static JSONArray parseArray(String s) {
        if (s == null || s.isEmpty()) {
            return new JSONArray();
        }
        try {
            Object o = JSONValue.parse(s);
            return o instanceof JSONArray ? (JSONArray) o : new JSONArray();
        } catch (Exception e) {
            return new JSONArray();
        }
    }

    static long getLong(JSONArray a, int i) {
        if (a == null || i >= a.size() || a.get(i) == null) {
            return 0;
        }
        try {
            return Long.parseLong(String.valueOf(a.get(i)));
        } catch (NumberFormatException e) {
            try {
                return (long) Double.parseDouble(String.valueOf(a.get(i)));
            } catch (NumberFormatException e2) {
                return 0;
            }
        }
    }

    static String subName(int taskId, int index) {
        for (TaskMain tm : Manager.TASKS) {
            if (tm.id == taskId && tm.subTasks != null && index >= 0 && index < tm.subTasks.size()) {
                SubTaskMain s = tm.subTasks.get(index);
                return s.name;
            }
        }
        return "";
    }
}
