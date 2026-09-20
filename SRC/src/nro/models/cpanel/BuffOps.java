package nro.models.cpanel;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.text.Normalizer;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import nro.models.cpanel.CPanel.CPanelException;
import nro.models.cpanel.CharacterOps.Offline;
import nro.models.item.Item;
import nro.models.player.Inventory;
import nro.models.player.Player;
import nro.models.player_system.Template;
import nro.models.server.Client;
import nro.models.server.Manager;
import nro.models.services.InventoryService;
import nro.models.services.ItemService;
import nro.models.services.Service;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.JSONValue;

/**
 * Nghiệp vụ tab "Buff đồ": tra vật phẩm / option, tạo vật phẩm có option tùy chỉnh và phát
 * cho 1 nhân vật (online: bộ nhớ, offline: DB - cùng cơ chế CharacterOps) hoặc tất cả người online.
 * Mẫu buff lưu ở file cục bộ {@link #PRESET_FILE} (không đụng DB).
 */
final class BuffOps {

    /** Vật phẩm nhiệm vụ (patch nhiệm vụ mới) - phát tay có thể làm lệch tiến độ nhiệm vụ. */
    static final int QUEST_ITEM_MIN = 2000, QUEST_ITEM_MAX = 2031;
    static final String PRESET_FILE = "cpanel_buff_presets.json";

    private BuffOps() {
    }

    // =====================================================================
    // TRA CỨU
    // =====================================================================
    /** Tìm vật phẩm theo id (chính xác) hoặc một phần tên (không phân biệt dấu/hoa thường). */
    static List<Template.ItemTemplate> searchItems(String kw, int limit) {
        List<Template.ItemTemplate> out = new ArrayList<>();
        String k = kw == null ? "" : kw.trim();
        List<Template.ItemTemplate> all = Manager.ITEM_TEMPLATES;
        if (k.matches("\\d+")) {
            int id = Integer.parseInt(k);
            Template.ItemTemplate t = itemOrNull(id);
            if (t != null) {
                out.add(t);
            }
        }
        String nk = fold(k);
        for (int i = 0; i < all.size() && out.size() < limit; i++) {
            Template.ItemTemplate t = all.get(i);
            if (t == null || t.name == null || t.name.trim().isEmpty()) {
                continue;
            }
            if (!out.isEmpty() && out.get(0) == t) {
                continue;
            }
            if (nk.isEmpty() || fold(t.name).contains(nk)) {
                out.add(t);
            }
        }
        return out;
    }

    /** Tra template theo id; id ngoài biên / lệch vị trí -> null (ItemService.getTemplate tra theo vị trí mảng). */
    static Template.ItemTemplate itemOrNull(int id) {
        List<Template.ItemTemplate> all = Manager.ITEM_TEMPLATES;
        if (id < 0 || id >= all.size()) {
            return null;
        }
        Template.ItemTemplate t = all.get(id);
        return t != null && t.id == id ? t : null;
    }

    /** Tra option theo id (ItemService.getItemOptionTemplate cũng tra theo vị trí mảng). */
    static Template.ItemOptionTemplate optionOrNull(int id) {
        List<Template.ItemOptionTemplate> all = Manager.ITEM_OPTION_TEMPLATES;
        if (id < 0 || id >= all.size()) {
            return null;
        }
        Template.ItemOptionTemplate t = all.get(id);
        return t != null && t.id == id ? t : null;
    }

    static List<Template.ItemOptionTemplate> searchOptions(String kw) {
        List<Template.ItemOptionTemplate> out = new ArrayList<>();
        String k = kw == null ? "" : kw.trim();
        String nk = fold(k);
        for (Template.ItemOptionTemplate o : Manager.ITEM_OPTION_TEMPLATES) {
            if (o == null) {
                continue;
            }
            if (nk.isEmpty() || String.valueOf(o.id).equals(k) || fold(o.name == null ? "" : o.name).contains(nk)) {
                out.add(o);
            }
        }
        return out;
    }

    static String optionText(int id, int param) {
        Template.ItemOptionTemplate o = optionOrNull(id);
        if (o == null) {
            return "(option " + id + " không tồn tại)";
        }
        String n = o.name == null || o.name.isEmpty() ? "(dòng trống)" : o.name;
        return n.replace("#", String.valueOf(param));
    }

    static String typeName(int type) {
        switch (type) {
            case 0: return "Áo";
            case 1: return "Quần";
            case 2: return "Găng";
            case 3: return "Giày";
            case 4: return "Rada/Nhẫn";
            case 5: return "Cải trang";
            case 6: return "Đậu thần";
            case 7: return "Sách KN";
            case 8: return "VP nhiệm vụ";
            case 9: return "Vàng";
            case 10: return "Ngọc";
            case 11: return "Đeo lưng";
            case 12: return "Ngọc rồng";
            case 13: return "Bùa";
            case 14: return "Đá nâng cấp";
            case 23: case 24: return "Thú cưỡi";
            case 27: return "Hỗ trợ/SK";
            case 29: return "Dùng theo TG";
            case 30: return "Sao pha lê";
            case 32: return "Giáp tập";
            case 33: return "Thẻ";
            case 34: return "Hồng ngọc";
            case 36: return "Danh hiệu";
            default: return "";
        }
    }

    static String genderName(int g) {
        return g == 3 ? "Chung" : CPanel.planet(g);
    }

    static boolean isQuestItem(int id) {
        return id >= QUEST_ITEM_MIN && id <= QUEST_ITEM_MAX;
    }

    /** Bỏ dấu tiếng Việt + chữ thường để tìm kiếm. */
    static String fold(String s) {
        String n = Normalizer.normalize(s, Normalizer.Form.NFD).replaceAll("\\p{M}+", "");
        return n.replace('đ', 'd').replace('Đ', 'D').toLowerCase(Locale.ROOT);
    }

    // =====================================================================
    // KIỂM TRA
    // =====================================================================
    static Template.ItemTemplate validate(int itemId, int quantity, List<int[]> opts) throws CPanelException {
        Template.ItemTemplate t = itemOrNull(itemId);
        if (t == null) {
            throw new CPanelException("Không có vật phẩm id " + itemId + " (ngoài biên item_template hoặc không tồn tại).");
        }
        if (quantity <= 0) {
            throw new CPanelException("Số lượng phải lớn hơn 0.");
        }
        if (t.isUpToUp && quantity > 99_999_999) {
            throw new CPanelException("Số lượng tối đa 99.999.999.");
        }
        if (!t.isUpToUp && quantity > Inventory.MAX_ITEMS_BAG) {
            throw new CPanelException("Vật phẩm không cộng dồn: tối đa " + Inventory.MAX_ITEMS_BAG + " cái mỗi lần.");
        }
        if (opts.size() > 60) {
            throw new CPanelException("Tối đa 60 option.");
        }
        for (int[] o : opts) {
            if (optionOrNull(o[0]) == null) {
                throw new CPanelException("Option id " + o[0] + " không có trong item_option_template.");
            }
        }
        return t;
    }

    static String describe(Template.ItemTemplate t, int quantity, List<int[]> opts) {
        StringBuilder sb = new StringBuilder();
        sb.append(quantity).append(" x ").append(t.name).append(" [").append(t.id).append("]");
        if (!opts.isEmpty()) {
            sb.append(" {");
            for (int i = 0; i < opts.size(); i++) {
                if (i > 0) {
                    sb.append(", ");
                }
                sb.append(opts.get(i)[0]).append(':').append(opts.get(i)[1]);
            }
            sb.append('}');
        }
        return sb.toString();
    }

    // =====================================================================
    // PHÁT
    // =====================================================================
    /** Tạo 1 vật phẩm bằng hàm có sẵn của game và gắn option. */
    private static Item newItem(Template.ItemTemplate t, int quantity, List<int[]> opts) {
        Item item = ItemService.gI().createNewItem(t.id, quantity);
        for (int[] o : opts) {
            item.itemOptions.add(new Item.ItemOption(o[0], o[1]));
        }
        return item;
    }

    /** Phát cho 1 người đang online (gọi trên luồng cpanel, giống lệnh admin SEND_ITEM_OP). Trả về số đã phát. */
    private static int giveOnline(Player p, Template.ItemTemplate t, int quantity, List<int[]> opts) throws CPanelException {
        int given = 0;
        if (t.isUpToUp) {
            if (InventoryService.gI().addItemBag(p, newItem(t, quantity, opts))) {
                given = quantity;
            }
        } else {
            if (InventoryService.gI().getCountEmptyBag(p) < quantity) {
                throw new CPanelException("Hành trang của " + p.name + " không đủ " + quantity + " ô trống.");
            }
            for (int i = 0; i < quantity; i++) {
                if (!InventoryService.gI().addItemBag(p, newItem(t, 1, opts))) {
                    break;
                }
                given++;
            }
        }
        InventoryService.gI().sendItemBags(p);
        if (given > 0) {
            Service.gI().sendThongBao(p, "Bạn nhận được " + given + " " + t.name + " từ Quản trị viên");
        }
        return given;
    }

    static String giveToCharacter(long playerId, String name, int itemId, int quantity, List<int[]> opts, boolean force)
            throws Exception {
        Template.ItemTemplate t = validate(itemId, quantity, opts);
        String what = describe(t, quantity, opts);
        Player p = CharacterOps.findOnline(playerId);
        if (p == null) {
            if (CharacterOps.isSpecialItem(t)) {
                throw new CPanelException("\"" + t.name + "\" là vật phẩm đặc biệt (tiền tệ / ngọc rồng / mở rộng ô)."
                        + "\nChỉ phát được khi người chơi đang online.");
            }
            Offline r = CharacterOps.modifyOffline(playerId, "items_bag", force,
                    old -> CharacterOps.addToBagJson(old, t, quantity, opts));
            if (r == Offline.OK) {
                return CharacterOps.afterOffline(playerId, name, "[BUFF][OFFLINE/DB] Phát " + what + " vào hành trang " + name);
            }
            p = CharacterOps.findOnline(playerId);
            if (p == null) {
                throw new CPanelException("Nhân vật đang chuyển trạng thái online/offline. Thử lại sau vài giây.");
            }
        }
        int given = giveOnline(p, t, quantity, opts);
        if (given == 0) {
            throw new CPanelException("Không thêm được vật phẩm cho " + p.name + " (hành trang đầy hoặc vượt giới hạn).");
        }
        String msg = "[BUFF][ONLINE/BỘ NHỚ] Phát " + given + "/" + quantity + " - " + what + " cho " + p.name + " (game sẽ tự lưu)";
        CPanel.log(msg);
        return msg;
    }

    static String giveToAllOnline(int itemId, int quantity, List<int[]> opts) throws Exception {
        Template.ItemTemplate t = validate(itemId, quantity, opts);
        int ok = 0, fail = 0, skip = 0;
        List<String> failed = new ArrayList<>();
        for (Player p : Client.gI().getPlayersSnapshot()) {
            if (p == null || !p.isPl()) {
                continue;
            }
            if (p.beforeDispose || p.isOffline || p.idMark == null || !p.idMark.isLoadedAllDataPlayer()) {
                skip++;
                continue;
            }
            try {
                if (giveOnline(p, t, quantity, opts) > 0) {
                    ok++;
                } else {
                    fail++;
                    failed.add(p.name);
                }
            } catch (Exception e) {
                fail++;
                failed.add(p.name);
            }
        }
        String msg = "[BUFF][TẤT CẢ ONLINE] Phát " + describe(t, quantity, opts) + ": thành công " + ok
                + " người, thất bại " + fail + ", bỏ qua (đang vào/thoát) " + skip;
        CPanel.log(msg);
        if (!failed.isEmpty()) {
            String list = String.join(", ", failed.subList(0, Math.min(30, failed.size())));
            CPanel.log("[BUFF] Không phát được (hành trang đầy?): " + list + (failed.size() > 30 ? "..." : ""));
            msg += "\nKhông phát được: " + list + (failed.size() > 30 ? "..." : "");
        }
        return msg;
    }

    // =====================================================================
    // MẪU BUFF (file cục bộ)
    // =====================================================================
    static final class Preset {

        String name;
        int itemId;
        int quantity;
        List<int[]> options = new ArrayList<>();

        Preset(String name, int itemId, int quantity, int[]... opts) {
            this.name = name;
            this.itemId = itemId;
            this.quantity = quantity;
            for (int[] o : opts) {
                options.add(o);
            }
        }

        @Override
        public String toString() {
            return name;
        }
    }

    /** Mẫu mặc định khi chưa có file (id/option tra theo docs 06 §5, §9.5; ItemService.createItemSKH). */
    static List<Preset> defaultPresets() {
        List<Preset> l = new ArrayList<>();
        // Set kích hoạt Sôngôku (129) + dòng mô tả 141 (getOptionIdsBySKH) + 30 như createItemSKH; áo 0 giáp 2 (doc 06 §2.3)
        l.add(new Preset("Set kích hoạt - Áo vải 3 lỗ Sôngôku (TĐ)", 0, 1,
                new int[]{47, 2}, new int[]{129, 1}, new int[]{141, 1}, new int[]{30, 1}));
        // Đồ Thần Linh: áo TL TĐ giáp 800 (tiLe 100), yêu cầu SM 15 tỉ (doc 06 §9.2)
        l.add(new Preset("Đồ Thần Linh - Áo TL Trái Đất", 555, 1, new int[]{47, 800}, new int[]{21, 15}));
        // Hộp quà sự kiện (1776), không giao dịch
        l.add(new Preset("Hộp quà sự kiện x10", 1776, 10, new int[]{30, 0}));
        return l;
    }

    static List<Preset> loadPresets() {
        Path p = Paths.get(PRESET_FILE);
        if (!Files.exists(p)) {
            return defaultPresets();
        }
        List<Preset> out = new ArrayList<>();
        try {
            Object root = JSONValue.parse(new String(Files.readAllBytes(p), StandardCharsets.UTF_8));
            if (root instanceof JSONArray) {
                for (Object o : (JSONArray) root) {
                    if (!(o instanceof JSONObject)) {
                        continue;
                    }
                    JSONObject j = (JSONObject) o;
                    Preset pr = new Preset(String.valueOf(j.get("name")),
                            (int) CharacterOps.getLong(arr(j.get("item")), 0),
                            (int) CharacterOps.getLong(arr(j.get("item")), 1));
                    JSONArray ops = arr(j.get("options"));
                    for (Object x : ops) {
                        JSONArray a = x instanceof JSONArray ? (JSONArray) x : new JSONArray();
                        if (a.size() >= 2) {
                            pr.options.add(new int[]{(int) CharacterOps.getLong(a, 0), (int) CharacterOps.getLong(a, 1)});
                        }
                    }
                    out.add(pr);
                }
            }
        } catch (Exception e) {
            CPanel.log("Không đọc được " + PRESET_FILE + ": " + e + " -> dùng mẫu mặc định.");
            return defaultPresets();
        }
        return out;
    }

    private static JSONArray arr(Object o) {
        return o instanceof JSONArray ? (JSONArray) o : new JSONArray();
    }

    @SuppressWarnings("unchecked")
    static void savePresets(List<Preset> list) throws IOException {
        JSONArray root = new JSONArray();
        for (Preset pr : list) {
            JSONObject j = new JSONObject();
            j.put("name", pr.name);
            JSONArray item = new JSONArray();
            item.add(pr.itemId);
            item.add(pr.quantity);
            j.put("item", item);
            JSONArray ops = new JSONArray();
            for (int[] o : pr.options) {
                JSONArray a = new JSONArray();
                a.add(o[0]);
                a.add(o[1]);
                ops.add(a);
            }
            j.put("options", ops);
            root.add(j);
        }
        Path tmp = Paths.get(PRESET_FILE + ".tmp");
        Files.write(tmp, root.toJSONString().getBytes(StandardCharsets.UTF_8));
        Files.move(tmp, Paths.get(PRESET_FILE), StandardCopyOption.REPLACE_EXISTING);
    }
}
