package nro.models.cpanel;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import nro.models.cpanel.CPanel.CPanelException;
import nro.models.data.LocalManager;
import nro.models.event.EventConfig;
import nro.models.item.Item;
import nro.models.managers.GiftCodeManager;
import nro.models.player_system.GiftCode;
import nro.models.player_system.Template;
import nro.models.server.Manager;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.JSONValue;

/**
 * Nghiệp vụ tab "Sự kiện": giftcode (bảng {@code giftcode} + danh sách trong bộ nhớ
 * {@link GiftCodeManager#listGiftCode}), tỉ lệ EXP server. Chỉ gọi trên luồng nền cpanel.
 */
final class EventOps {

    /** Giới hạn cột TIMESTAMP của MariaDB. */
    static final Timestamp MAX_TS = Timestamp.valueOf("2038-01-19 00:00:00");

    private EventOps() {
    }

    static final class GiftRow {

        int id;
        String code;
        int countLeft;
        String detail;
        Timestamp created;
        Timestamp expired;
        boolean inMemory;
        int memCountLeft = -1;
    }

    /** 1 dòng quà: id (-1 vàng, -2 ngọc, -3 hồng ngọc, khác = item), số lượng, option. */
    static final class GiftEntry {

        int id;
        int quantity;
        List<int[]> options = new ArrayList<>();
    }

    // =====================================================================
    // GIFTCODE
    // =====================================================================
    static List<GiftRow> listGiftcodes() throws Exception {
        List<GiftRow> out = new ArrayList<>();
        try (Connection con = LocalManager.getConnection();
                PreparedStatement ps = con.prepareStatement(
                        "SELECT id, code, count_left, detail, datecreate, expired FROM giftcode ORDER BY id DESC LIMIT 1000");
                ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                GiftRow r = new GiftRow();
                r.id = rs.getInt("id");
                r.code = rs.getString("code");
                r.countLeft = rs.getInt("count_left");
                r.detail = rs.getString("detail");
                r.created = rs.getTimestamp("datecreate");
                r.expired = rs.getTimestamp("expired");
                out.add(r);
            }
        }
        List<GiftCode> mem = memorySnapshot();
        for (GiftRow r : out) {
            for (GiftCode g : mem) {
                if (g.id == r.id) {
                    r.inMemory = true;
                    r.memCountLeft = g.countLeft;
                }
            }
        }
        return out;
    }

    private static List<GiftCode> memorySnapshot() {
        List<GiftCode> l = GiftCodeManager.gI().listGiftCode;
        synchronized (l) {
            return new ArrayList<>(l);
        }
    }

    /** Tóm tắt JSON detail thành chữ dễ đọc. */
    static String summarize(String detail) {
        Object o;
        try {
            o = JSONValue.parse(detail == null ? "" : detail);
        } catch (Exception e) {
            return "(detail lỗi JSON)";
        }
        if (!(o instanceof JSONArray)) {
            return "(detail lỗi JSON)";
        }
        StringBuilder sb = new StringBuilder();
        for (Object x : (JSONArray) o) {
            if (!(x instanceof JSONObject)) {
                continue;
            }
            JSONObject j = (JSONObject) x;
            int id = (int) toLong(j.get("id"));
            long q = toLong(j.get("quantity"));
            if (sb.length() > 0) {
                sb.append("; ");
            }
            sb.append(CPanel.num(q)).append(' ').append(giftName(id));
            Object ops = j.get("options");
            if (ops instanceof JSONArray && !((JSONArray) ops).isEmpty()) {
                sb.append(" {");
                boolean first = true;
                for (Object op : (JSONArray) ops) {
                    if (op instanceof JSONObject) {
                        if (!first) {
                            sb.append(',');
                        }
                        first = false;
                        sb.append(toLong(((JSONObject) op).get("id"))).append(':').append(toLong(((JSONObject) op).get("param")));
                    }
                }
                sb.append('}');
            }
        }
        return sb.toString();
    }

    static String giftName(int id) {
        switch (id) {
            case -1:
                return "vàng";
            case -2:
                return "ngọc";
            case -3:
                return "hồng ngọc";
            default:
                Template.ItemTemplate t = BuffOps.itemOrNull(id);
                return (t == null ? "?" : t.name) + " [" + id + "]";
        }
    }

    private static long toLong(Object o) {
        try {
            return Long.parseLong(String.valueOf(o).trim());
        } catch (Exception e) {
            return 0;
        }
    }

    /**
     * Đọc nội dung quà dạng chữ, mỗi dòng: {@code <id> <số lượng> [optId:param ...]}.
     * id -1 = vàng, -2 = ngọc, -3 = hồng ngọc. Dòng trống / bắt đầu bằng # bị bỏ qua.
     */
    static List<GiftEntry> parseGifts(String text) throws CPanelException {
        List<GiftEntry> out = new ArrayList<>();
        Set<Integer> ids = new HashSet<>();
        String[] lines = text == null ? new String[0] : text.split("\\r?\\n");
        for (int ln = 0; ln < lines.length; ln++) {
            String s = lines[ln].trim();
            if (s.isEmpty() || s.startsWith("#")) {
                continue;
            }
            String[] p = s.split("\\s+");
            String where = "Dòng " + (ln + 1) + " (\"" + s + "\"): ";
            if (p.length < 2) {
                throw new CPanelException(where + "cần ít nhất <id> <số lượng>.");
            }
            GiftEntry g = new GiftEntry();
            try {
                g.id = Integer.parseInt(p[0]);
                g.quantity = Integer.parseInt(p[1].replace(".", "").replace(",", ""));
            } catch (NumberFormatException e) {
                throw new CPanelException(where + "id / số lượng không phải số.");
            }
            if (g.quantity <= 0) {
                throw new CPanelException(where + "số lượng phải > 0.");
            }
            if (g.id < -3 || (g.id >= 0 && BuffOps.itemOrNull(g.id) == null)) {
                throw new CPanelException(where + "id " + g.id + " không phải -1/-2/-3 và không có trong item_template.");
            }
            if (!ids.add(g.id)) {
                // GiftCode.detail là HashMap<id, số lượng> -> 2 dòng cùng id sẽ đè nhau
                throw new CPanelException(where + "id " + g.id + " bị lặp (game lưu quà theo id, dòng sau sẽ đè dòng trước).");
            }
            for (int i = 2; i < p.length; i++) {
                String[] kv = p[i].split(":");
                if (kv.length != 2) {
                    throw new CPanelException(where + "option phải dạng id:param (VD 30:0).");
                }
                int oid, par;
                try {
                    oid = Integer.parseInt(kv[0]);
                    par = Integer.parseInt(kv[1]);
                } catch (NumberFormatException e) {
                    throw new CPanelException(where + "option \"" + p[i] + "\" không phải số.");
                }
                if (BuffOps.optionOrNull(oid) == null) {
                    throw new CPanelException(where + "option id " + oid + " không có trong item_option_template.");
                }
                g.options.add(new int[]{oid, par});
            }
            out.add(g);
        }
        if (out.isEmpty()) {
            throw new CPanelException("Chưa nhập quà nào.");
        }
        if (out.size() > 20) {
            throw new CPanelException("Tối đa 20 dòng quà (người chơi cần đủ ô trống bằng số dòng).");
        }
        return out;
    }

    /** JSON đúng định dạng Manager.java đang đọc: [{"id":..,"quantity":..,"options":[{"id":..,"param":..}]}]. */
    @SuppressWarnings("unchecked")
    static String toDetailJson(List<GiftEntry> gifts) {
        JSONArray arr = new JSONArray();
        for (GiftEntry g : gifts) {
            JSONObject j = new JSONObject();
            j.put("id", g.id);
            j.put("quantity", g.quantity);
            JSONArray ops = new JSONArray();
            for (int[] o : g.options) {
                JSONObject oj = new JSONObject();
                oj.put("id", o[0]);
                oj.put("param", o[1]);
                ops.add(oj);
            }
            j.put("options", ops);
            arr.add(j);
        }
        return arr.toJSONString();
    }

    /**
     * Tạo giftcode: INSERT vào DB rồi thêm luôn vào danh sách trong bộ nhớ (người chơi nhập được
     * ngay, không cần restart - trước đây phải restart vì code chỉ nạp giftcode lúc khởi động).
     *
     * @param countLeft số lượt; -1 = không giới hạn (game coi là 999.999.999)
     */
    static String createGiftcode(String code, int countLeft, Timestamp expired, List<GiftEntry> gifts) throws Exception {
        if (code == null || !code.matches("[A-Za-z0-9_]{3,40}")) {
            throw new CPanelException("Mã chỉ gồm A-Z a-z 0-9 _ , dài 3–40 ký tự (cột code là latin1, game so khớp phân biệt hoa/thường).");
        }
        if (countLeft < -1 || countLeft == 0) {
            throw new CPanelException("Số lượt phải ≥ 1, hoặc -1 = không giới hạn.");
        }
        Timestamp now = new Timestamp(System.currentTimeMillis());
        if (expired == null || !expired.after(now) || expired.after(MAX_TS)) {
            throw new CPanelException("Hạn dùng phải sau hiện tại và trước 2038-01-19 (giới hạn TIMESTAMP).");
        }
        String detail = toDetailJson(gifts);
        for (GiftCode g : memorySnapshot()) {
            if (code.equals(g.code)) {
                throw new CPanelException("Mã \"" + code + "\" đã có trong bộ nhớ server.");
            }
        }
        int newId;
        try (Connection con = LocalManager.getConnection()) {
            try (PreparedStatement ps = con.prepareStatement("SELECT id FROM giftcode WHERE code = ? LIMIT 1")) {
                ps.setString(1, code);
                try (ResultSet rs = ps.executeQuery()) {
                    if (rs.next()) {
                        throw new CPanelException("Mã \"" + code + "\" đã có trong DB (id " + rs.getInt(1) + ").");
                    }
                }
            }
            try (PreparedStatement ps = con.prepareStatement(
                    "INSERT INTO giftcode (code, count_left, detail, datecreate, expired) VALUES (?, ?, ?, ?, ?)",
                    Statement.RETURN_GENERATED_KEYS)) {
                ps.setString(1, code);
                ps.setInt(2, countLeft);
                ps.setString(3, detail);
                ps.setTimestamp(4, now);
                ps.setTimestamp(5, expired);
                ps.executeUpdate();
                try (ResultSet rs = ps.getGeneratedKeys()) {
                    if (!rs.next()) {
                        throw new CPanelException("Không lấy được id giftcode vừa tạo.");
                    }
                    newId = rs.getInt(1);
                }
            }
        }
        // Nạp vào bộ nhớ đúng như Manager.java lúc khởi động
        GiftCode g = new GiftCode();
        g.id = newId;
        g.code = code;
        g.countLeft = countLeft == -1 ? 999999999 : countLeft;
        g.datecreate = now;
        g.dateexpired = expired;
        for (GiftEntry e : gifts) {
            ArrayList<Item.ItemOption> ol = new ArrayList<>();
            for (int[] o : e.options) {
                ol.add(new Item.ItemOption(o[0], o[1]));
            }
            g.option.put(e.id, ol);
            g.detail.put(e.id, e.quantity);
        }
        List<GiftCode> l = GiftCodeManager.gI().listGiftCode;
        synchronized (l) {
            l.add(g);
        }
        String msg = "Tạo giftcode \"" + code + "\" (id " + newId + ", " + (countLeft == -1 ? "không giới hạn" : countLeft + " lượt")
                + ", hạn " + expired + "): " + summarize(detail);
        CPanel.log(msg);
        return msg;
    }

    static String deleteGiftcode(int id, String code) throws Exception {
        int n;
        try (Connection con = LocalManager.getConnection();
                PreparedStatement ps = con.prepareStatement("DELETE FROM giftcode WHERE id = ?")) {
            ps.setInt(1, id);
            n = ps.executeUpdate();
        }
        boolean mem;
        List<GiftCode> l = GiftCodeManager.gI().listGiftCode;
        synchronized (l) {
            mem = l.removeIf(g -> g.id == id);
        }
        String msg = "Xóa giftcode \"" + code + "\" (id " + id + "): DB " + (n > 0 ? "đã xóa" : "không thấy dòng")
                + ", bộ nhớ " + (mem ? "đã gỡ" : "không có");
        CPanel.log(msg);
        return msg;
    }

    // =====================================================================
    // TỈ LỆ EXP SERVER
    // =====================================================================
    /** Đổi Manager.RATE_EXP_SERVER ngay (NPoint đọc lại mỗi lần tính tiềm năng) và lưu server.expserver. */
    static String setExpRate(int rate) throws Exception {
        if (rate < 1 || rate > 100) {
            throw new CPanelException("Tỉ lệ EXP phải từ 1 đến 100 (kiểu byte trong code).");
        }
        int old = Manager.RATE_EXP_SERVER;
        Manager.RATE_EXP_SERVER = (byte) rate;
        EventConfig.set("server.expserver", String.valueOf(rate), null);
        String msg = "Đổi tỉ lệ EXP server x" + old + " -> x" + rate + " (áp dụng ngay, đã lưu server.expserver)";
        CPanel.log(msg);
        return msg;
    }
}
