package nro.models.cpanel;

import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSpinner;
import javax.swing.JTable;
import javax.swing.SpinnerNumberModel;
import javax.swing.table.AbstractTableModel;
import org.json.simple.JSONArray;
import nro.models.data.LocalManager;

/**
 * Tab "Top vàng/ngọc": xếp hạng số dư của nhân vật để soi nguồn lạm phát và dò
 * tài khoản có số dư bất thường (dấu hiệu bị khai thác lỗi).
 *
 * <p>Chỉ ĐỌC dữ liệu, không sửa gì. Muốn sửa số dư thì dùng tab "Nhân vật".
 */
final class TopTab extends JPanel {

    /** Ngưỡng coi là bất thường. */
    private static final long MAX_GOLD = 100_000_000_000L; // 100 tỷ
    private static final long MAX_GEM = 1_000_000_000L;    // 1 tỷ
    private static final long MAX_RUBY = 1_000_000_000L;   // 1 tỷ
    private static final long MAX_THOI_VANG = 1_000_000L;  // 1 triệu thỏi

    /** Id vật phẩm Thỏi vàng (457 bản thường, 1535 bản sự kiện). */
    private static final int[] THOI_VANG_IDS = {457, 1535};

    private static final String[] METRICS = {
        "Vàng", "Ngọc", "Hồng ngọc", "Thỏi vàng (quét túi + rương)", "Sức mạnh", "VNĐ trong tài khoản"
    };

    private final JComboBox<String> cbMetric = new JComboBox<>(METRICS);
    private final JSpinner spLimit = new JSpinner(new SpinnerNumberModel(50, 5, 500, 5));
    private final JLabel lblTotal = new JLabel(" ");
    private final Model model = new Model();
    private final JTable table = new JTable(model);

    TopTab() {
        super(new BorderLayout(6, 6));
        setBorder(BorderFactory.createEmptyBorder(8, 8, 8, 8));

        JPanel top = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 8));
        top.setBorder(BorderFactory.createTitledBorder("Xếp hạng (chỉ đọc dữ liệu)"));
        top.add(new JLabel("Xếp theo:"));
        top.add(cbMetric);
        top.add(new JLabel("Số dòng:"));
        top.add(spLimit);
        JButton btnLoad = new JButton("Xem bảng xếp hạng");
        JButton btnCheck = new JButton("Kiểm tra số dư bất thường");
        JButton btnSum = new JButton("Tổng vàng / ngọc toàn server");
        top.add(btnLoad);
        top.add(btnCheck);
        top.add(btnSum);

        JPanel north = new JPanel(new BorderLayout());
        north.add(top, BorderLayout.NORTH);
        lblTotal.setBorder(BorderFactory.createEmptyBorder(2, 6, 6, 6));
        north.add(lblTotal, BorderLayout.SOUTH);

        JLabel hint = new JLabel("Số liệu đọc từ database. Người chơi đang online có thể chênh cho tới lần lưu dữ liệu kế tiếp "
                + "(tab Server có nút \"Lưu toàn bộ dữ liệu\").");
        hint.setBorder(BorderFactory.createEmptyBorder(4, 6, 4, 6));

        add(north, BorderLayout.NORTH);
        add(new JScrollPane(table), BorderLayout.CENTER);
        add(hint, BorderLayout.SOUTH);

        btnLoad.addActionListener(e -> load());
        btnCheck.addActionListener(e -> check());
        btnSum.addActionListener(e -> sum());
    }

    // =====================================================================
    // XẾP HẠNG
    // =====================================================================
    private void load() {
        final int metric = cbMetric.getSelectedIndex();
        final int limit = (Integer) spLimit.getValue();
        CPanel.async(this, "lấy bảng xếp hạng", () -> query(metric, limit), rows -> {
            model.set(rows, METRICS[metric]);
            lblTotal.setText(" " + rows.size() + " dòng — " + METRICS[metric]);
        });
    }

    private List<Row> query(int metric, int limit) throws Exception {
        if (metric == 3) {
            return topThoiVang(limit);
        }
        String expr;
        switch (metric) {
            case 0:
                expr = "CAST(JSON_EXTRACT(p.data_inventory, '$[0]') AS SIGNED)";
                break;
            case 1:
                expr = "CAST(JSON_EXTRACT(p.data_inventory, '$[1]') AS SIGNED)";
                break;
            case 2:
                expr = "CAST(JSON_EXTRACT(p.data_inventory, '$[2]') AS SIGNED)";
                break;
            case 4:
                expr = "CAST(JSON_EXTRACT(p.data_point, '$[1]') AS SIGNED)";
                break;
            default:
                expr = "a.vnd";
                break;
        }
        String sql = "SELECT p.id, p.name, a.username, " + expr + " AS v "
                + "FROM player p LEFT JOIN account a ON a.id = p.account_id "
                + "ORDER BY v DESC LIMIT " + limit;
        List<Row> out = new ArrayList<>();
        try (Connection con = LocalManager.getConnection();
                PreparedStatement ps = con.prepareStatement(sql);
                ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                Row r = new Row();
                r.id = rs.getLong("id");
                r.name = rs.getString("name");
                r.username = rs.getString("username");
                r.value = rs.getLong("v");
                r.note = noteFor(metric, r.value);
                out.add(r);
            }
        }
        return out;
    }

    private static String noteFor(int metric, long v) {
        if (v < 0) {
            return "ÂM — bất thường";
        }
        switch (metric) {
            case 0:
                return v > MAX_GOLD ? "Vàng vượt " + CPanel.num(MAX_GOLD) : "";
            case 1:
                return v > MAX_GEM ? "Ngọc vượt " + CPanel.num(MAX_GEM) : "";
            case 2:
                return v > MAX_RUBY ? "Hồng ngọc vượt " + CPanel.num(MAX_RUBY) : "";
            default:
                return "";
        }
    }

    // =====================================================================
    // THỎI VÀNG (phải mở từng túi ra đếm)
    // =====================================================================
    private static final class Holder {

        long id;
        String name;
        String username;
        long gold;
        long gem;
        long ruby;
        long thoiVang;
    }

    private List<Holder> scanAll() throws Exception {
        List<Holder> out = new ArrayList<>();
        String sql = "SELECT p.id, p.name, a.username, p.data_inventory, p.items_bag, p.items_box "
                + "FROM player p LEFT JOIN account a ON a.id = p.account_id";
        try (Connection con = LocalManager.getConnection();
                PreparedStatement ps = con.prepareStatement(sql);
                ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                Holder h = new Holder();
                h.id = rs.getLong("id");
                h.name = rs.getString("name");
                h.username = rs.getString("username");
                JSONArray inv = CharacterOps.parseArray(rs.getString("data_inventory"));
                h.gold = CharacterOps.getLong(inv, 0);
                h.gem = CharacterOps.getLong(inv, 1);
                h.ruby = CharacterOps.getLong(inv, 2);
                h.thoiVang = countThoiVang(rs.getString("items_bag")) + countThoiVang(rs.getString("items_box"));
                out.add(h);
            }
        }
        return out;
    }

    /** Cộng số lượng Thỏi vàng trong một chuỗi JSON danh sách vật phẩm. */
    private static long countThoiVang(String json) {
        JSONArray list = CharacterOps.parseArray(json);
        long total = 0;
        for (Object o : list) {
            JSONArray it = CharacterOps.parseArray(String.valueOf(o));
            if (it.isEmpty()) {
                continue;
            }
            long id = CharacterOps.getLong(it, 0);
            for (int t : THOI_VANG_IDS) {
                if (id == t) {
                    total += CharacterOps.getLong(it, 1);
                    break;
                }
            }
        }
        return total;
    }

    private List<Row> topThoiVang(int limit) throws Exception {
        List<Holder> all = scanAll();
        all.sort(Comparator.comparingLong((Holder h) -> h.thoiVang).reversed());
        List<Row> out = new ArrayList<>();
        for (int i = 0; i < Math.min(limit, all.size()); i++) {
            Holder h = all.get(i);
            Row r = new Row();
            r.id = h.id;
            r.name = h.name;
            r.username = h.username;
            r.value = h.thoiVang;
            r.note = h.thoiVang > MAX_THOI_VANG ? "Vượt " + CPanel.num(MAX_THOI_VANG) + " thỏi" : "";
            out.add(r);
        }
        return out;
    }

    // =====================================================================
    // DÒ BẤT THƯỜNG
    // =====================================================================
    private void check() {
        CPanel.async(this, "kiểm tra số dư bất thường", () -> {
            List<Holder> all = scanAll();
            List<Row> out = new ArrayList<>();
            for (Holder h : all) {
                StringBuilder sb = new StringBuilder();
                long shown = h.gold;
                if (h.gold < 0) {
                    sb.append("vàng ÂM; ");
                } else if (h.gold > MAX_GOLD) {
                    sb.append("vàng ").append(CPanel.num(h.gold)).append("; ");
                }
                if (h.gem < 0) {
                    sb.append("ngọc ÂM; ");
                } else if (h.gem > MAX_GEM) {
                    sb.append("ngọc ").append(CPanel.num(h.gem)).append("; ");
                }
                if (h.ruby < 0) {
                    sb.append("hồng ngọc ÂM; ");
                } else if (h.ruby > MAX_RUBY) {
                    sb.append("hồng ngọc ").append(CPanel.num(h.ruby)).append("; ");
                }
                if (h.thoiVang > MAX_THOI_VANG) {
                    sb.append("thỏi vàng ").append(CPanel.num(h.thoiVang)).append("; ");
                }
                if (sb.length() > 0) {
                    Row r = new Row();
                    r.id = h.id;
                    r.name = h.name;
                    r.username = h.username;
                    r.value = shown;
                    r.note = sb.toString();
                    out.add(r);
                }
            }
            out.sort(Comparator.comparingLong((Row r) -> r.value).reversed());
            return out;
        }, rows -> {
            model.set(rows, "Vàng");
            lblTotal.setText(rows.isEmpty()
                    ? " Không thấy nhân vật nào có số dư bất thường."
                    : " Có " + rows.size() + " nhân vật số dư bất thường (ngưỡng: vàng > "
                    + CPanel.num(MAX_GOLD) + ", ngọc/hồng ngọc > " + CPanel.num(MAX_GEM)
                    + ", thỏi vàng > " + CPanel.num(MAX_THOI_VANG) + ", hoặc số âm).");
            CPanel.log("Kiểm tra số dư bất thường: " + rows.size() + " nhân vật.");
        });
    }

    // =====================================================================
    // TỔNG TOÀN SERVER
    // =====================================================================
    private void sum() {
        CPanel.async(this, "tính tổng vàng/ngọc toàn server", () -> {
            String sql = "SELECT COUNT(*) n, "
                    + "SUM(CAST(JSON_EXTRACT(data_inventory, '$[0]') AS SIGNED)) g, "
                    + "SUM(CAST(JSON_EXTRACT(data_inventory, '$[1]') AS SIGNED)) m, "
                    + "SUM(CAST(JSON_EXTRACT(data_inventory, '$[2]') AS SIGNED)) r FROM player";
            try (Connection con = LocalManager.getConnection();
                    PreparedStatement ps = con.prepareStatement(sql);
                    ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return new long[]{rs.getLong("n"), rs.getLong("g"), rs.getLong("m"), rs.getLong("r")};
                }
            }
            return new long[]{0, 0, 0, 0};
        }, v -> {
            String s = " " + CPanel.num(v[0]) + " nhân vật — tổng vàng: " + CPanel.num(v[1])
                    + " · tổng ngọc: " + CPanel.num(v[2]) + " · tổng hồng ngọc: " + CPanel.num(v[3]);
            lblTotal.setText(s);
            CPanel.log("Tổng số dư toàn server:" + s);
        });
    }

    // =====================================================================
    // BẢNG
    // =====================================================================
    private static final class Row {

        long id;
        String name;
        String username;
        long value;
        String note;
    }

    private static final class Model extends AbstractTableModel {

        private List<Row> rows = new ArrayList<>();
        private String valueTitle = "Giá trị";

        void set(List<Row> r, String title) {
            this.rows = r;
            this.valueTitle = title;
            fireTableStructureChanged();
        }

        @Override
        public int getRowCount() {
            return rows.size();
        }

        @Override
        public int getColumnCount() {
            return 6;
        }

        @Override
        public String getColumnName(int c) {
            switch (c) {
                case 0:
                    return "#";
                case 1:
                    return "ID nhân vật";
                case 2:
                    return "Tên nhân vật";
                case 3:
                    return "Tài khoản";
                case 4:
                    return valueTitle;
                default:
                    return "Ghi chú";
            }
        }

        @Override
        public Object getValueAt(int r, int c) {
            Row x = rows.get(r);
            switch (c) {
                case 0:
                    return r + 1;
                case 1:
                    return x.id;
                case 2:
                    return x.name;
                case 3:
                    return x.username == null ? "-" : x.username;
                case 4:
                    return CPanel.num(x.value);
                default:
                    return x.note == null ? "" : x.note;
            }
        }
    }
}
