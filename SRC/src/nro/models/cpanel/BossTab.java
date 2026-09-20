package nro.models.cpanel;

import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.ListSelectionModel;
import javax.swing.SwingUtilities;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.TableRowSorter;
import nro.models.boss.Boss;
import nro.models.boss.BossTuning;
import nro.models.boss.Boss_Manager.BossManager;
import nro.models.consts.BossStatus;
import nro.models.map.Zone;

/**
 * Tab "Boss": xem toàn bộ boss đang có trong bộ nhớ, lọc theo tên / map, và
 * ép hồi sinh (reset) hoặc ép biến mất từng con / cả một map.
 *
 * <p>Mọi thao tác chỉ đổi trạng thái boss đúng như vòng đời sẵn có trong
 * {@link Boss} (REST -> RESPAWN -> JOIN_MAP...), không tự dựng boss mới.
 */
final class BossTab extends JPanel {

    private final Model model = new Model();
    private final JTable table = new JTable(model);
    private final JTextField txtFilter = new JTextField(16);
    private final JCheckBox chkAlive = new JCheckBox("Chỉ boss đang ở map");
    private final JLabel lblCount = new JLabel("-");
    private ScheduledFuture<?> future;

    BossTab() {
        super(new BorderLayout(6, 6));
        setBorder(BorderFactory.createEmptyBorder(8, 8, 8, 8));

        JPanel top = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 8));
        top.setBorder(BorderFactory.createTitledBorder("Lọc (tự làm mới mỗi 3 giây)"));
        top.add(new JLabel("Tên boss hoặc số map:"));
        top.add(txtFilter);
        top.add(chkAlive);
        JButton btnRefresh = new JButton("Làm mới ngay");
        top.add(btnRefresh);
        top.add(lblCount);

        JPanel actions = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 8));
        actions.setBorder(BorderFactory.createTitledBorder("Thao tác"));
        JButton btnRespawn = new JButton("Hồi sinh ngay (boss đã chọn)");
        JButton btnLeave = new JButton("Ép biến mất (cho nghỉ)");
        JButton btnRespawnMap = new JButton("Hồi sinh toàn bộ boss của map...");
        JButton btnLeaveAll = new JButton("Ép biến mất toàn bộ boss đang lọc");
        JButton btnTune = new JButton("Chỉnh máu / sát thương...");
        JButton btnBulk = new JButton("Chỉnh hàng loạt theo %...");
        actions.add(btnRespawn);
        actions.add(btnLeave);
        actions.add(btnRespawnMap);
        actions.add(btnLeaveAll);
        actions.add(btnTune);
        actions.add(btnBulk);

        JPanel north = new JPanel(new BorderLayout());
        north.add(top, BorderLayout.NORTH);
        north.add(actions, BorderLayout.CENTER);

        table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        table.setAutoCreateRowSorter(false);
        table.setRowSorter(new TableRowSorter<>(model));
        add(north, BorderLayout.NORTH);
        add(new JScrollPane(table), BorderLayout.CENTER);

        btnRefresh.addActionListener(e -> refresh());
        txtFilter.addActionListener(e -> refresh());
        chkAlive.addActionListener(e -> refresh());
        btnRespawn.addActionListener(e -> respawnSelected());
        btnLeave.addActionListener(e -> leaveSelected());
        btnRespawnMap.addActionListener(e -> respawnMap());
        btnLeaveAll.addActionListener(e -> leaveFiltered());
        btnTune.addActionListener(e -> tuneSelected());
        btnBulk.addActionListener(e -> tuneBulk());
        table.addMouseListener(new java.awt.event.MouseAdapter() {
            @Override
            public void mouseClicked(java.awt.event.MouseEvent e) {
                if (e.getClickCount() == 2) {
                    tuneSelected();
                }
            }
        });
    }

    void startAutoRefresh() {
        future = CPanel.TIMER.scheduleWithFixedDelay(
                () -> SwingUtilities.invokeLater(this::refresh), 0, 3, TimeUnit.SECONDS);
    }

    void stopAutoRefresh() {
        if (future != null) {
            future.cancel(false);
        }
    }

    // =====================================================================
    // DỮ LIỆU
    // =====================================================================
    private static final class Row {

        Boss boss;
        int id;
        String name;
        String status;
        int mapId;
        String mapName;
        int zoneId;
        long hp;
        long hpFull;
        int level;
        boolean inMap;
    }

    private List<Row> snapshot() {
        List<Row> out = new ArrayList<>();
        String f = txtFilter.getText() == null ? "" : txtFilter.getText().trim().toLowerCase();
        List<Boss> bosses;
        try {
            bosses = new ArrayList<>(BossManager.gI().getBosses());
        } catch (Throwable t) {
            return out;
        }
        for (Boss b : bosses) {
            if (b == null) {
                continue;
            }
            Row r = new Row();
            r.boss = b;
            try {
                r.id = (int) b.id;
                r.name = b.name == null ? "?" : b.name;
                r.status = b.bossStatus == null ? "?" : b.bossStatus.name();
                r.level = b.currentLevel;
                Zone z = b.zone;
                r.inMap = z != null && b.bossStatus != BossStatus.REST && b.bossStatus != BossStatus.DIE;
                r.mapId = z != null && z.map != null ? z.map.mapId : -1;
                r.mapName = z != null && z.map != null ? z.map.mapName : "(không ở map)";
                r.zoneId = z != null ? z.zoneId : -1;
                r.hp = b.nPoint != null ? b.nPoint.hp : 0;
                r.hpFull = b.nPoint != null ? b.nPoint.hpMax : 0;
            } catch (Throwable t) {
                continue;
            }
            if (chkAlive.isSelected() && !r.inMap) {
                continue;
            }
            if (!f.isEmpty()) {
                boolean hit = r.name.toLowerCase().contains(f)
                        || String.valueOf(r.mapId).equals(f)
                        || r.mapName.toLowerCase().contains(f)
                        || String.valueOf(r.id).equals(f);
                if (!hit) {
                    continue;
                }
            }
            out.add(r);
        }
        out.sort(Comparator.comparing((Row r) -> !r.inMap).thenComparing(r -> r.name));
        return out;
    }

    private void refresh() {
        List<Row> rows = snapshot();
        model.set(rows);
        long inMap = rows.stream().filter(r -> r.inMap).count();
        lblCount.setText("  Tổng: " + rows.size() + " boss, đang ở map: " + inMap);
    }

    private Row selected() {
        int v = table.getSelectedRow();
        if (v < 0) {
            CPanel.error(this, "Hãy chọn một boss trong bảng.");
            return null;
        }
        return model.get(table.convertRowIndexToModel(v));
    }

    // =====================================================================
    // THAO TÁC
    // =====================================================================
    /** Ép boss ra map ngay: đặt về hình dạng đầu rồi cho hồi sinh. */
    private static void doRespawn(Boss b) {
        if (b == null) {
            return;
        }
        b.currentLevel = -1;
        b.changeStatus(BossStatus.RESPAWN);
    }

    /** Ép boss rời map và chuyển sang nghỉ (sẽ tự hồi sinh theo thời gian nghỉ của nó). */
    private static void doLeave(Boss b) {
        if (b == null) {
            return;
        }
        try {
            b.leaveMap();
        } catch (Throwable ignored) {
        }
        b.changeStatus(BossStatus.REST);
    }

    private void respawnSelected() {
        Row r = selected();
        if (r == null) {
            return;
        }
        CPanel.async(this, "hồi sinh boss", () -> {
            doRespawn(r.boss);
            return null;
        }, x -> {
            CPanel.log("Đã ép hồi sinh boss " + r.name + " (id " + r.id + ").");
            refresh();
        });
    }

    private void leaveSelected() {
        Row r = selected();
        if (r == null) {
            return;
        }
        CPanel.async(this, "ép boss biến mất", () -> {
            doLeave(r.boss);
            return null;
        }, x -> {
            CPanel.log("Đã ép boss " + r.name + " (id " + r.id + ") rời map.");
            refresh();
        });
    }

    private void respawnMap() {
        String s = CPanel.askString(this, "Hồi sinh toàn bộ boss có map xuất hiện là map số:", "");
        if (s == null || s.isEmpty()) {
            return;
        }
        final int mapId;
        try {
            mapId = Integer.parseInt(s.trim());
        } catch (NumberFormatException e) {
            CPanel.error(this, "Số map không hợp lệ: " + s);
            return;
        }
        CPanel.async(this, "hồi sinh boss theo map", () -> {
            int n = 0;
            for (Boss b : new ArrayList<>(BossManager.gI().getBosses())) {
                if (b == null || b.data == null || b.data.length == 0) {
                    continue;
                }
                boolean match = false;
                for (int m : b.data[0].getMapJoin()) {
                    if (m == mapId) {
                        match = true;
                        break;
                    }
                }
                if (match) {
                    doRespawn(b);
                    n++;
                }
            }
            return n;
        }, n -> {
            CPanel.log("Đã ép hồi sinh " + n + " boss của map " + mapId + ".");
            CPanel.info(this, "Đã ép hồi sinh " + n + " boss của map " + mapId + ".");
            refresh();
        });
    }

    private void leaveFiltered() {
        List<Row> rows = snapshot();
        if (rows.isEmpty()) {
            CPanel.error(this, "Danh sách đang trống.");
            return;
        }
        if (!CPanel.confirm(this, "Ép " + rows.size() + " boss đang hiển thị rời map?")) {
            return;
        }
        CPanel.async(this, "ép boss biến mất", () -> {
            for (Row r : rows) {
                doLeave(r.boss);
            }
            return rows.size();
        }, n -> {
            CPanel.log("Đã ép " + n + " boss rời map.");
            refresh();
        });
    }

    // =====================================================================
    // CHỈNH SỐ BOSS
    // =====================================================================
    /** Mở hộp thoại chỉnh máu / sát thương / % chặn sát thương / thời gian nghỉ. */
    private void tuneSelected() {
        Row r = selected();
        if (r == null) {
            return;
        }
        new BossTuneDialog(this, r.boss).setVisible(true);
        refresh();
    }

    /**
     * Nhân máu / sát thương của toàn bộ boss đang hiển thị theo phần trăm.
     * 100 = giữ nguyên, 50 = còn một nửa, 200 = gấp đôi.
     */
    private void tuneBulk() {
        List<Row> rows = snapshot();
        if (rows.isEmpty()) {
            CPanel.error(this, "Danh sách đang trống.");
            return;
        }
        Long hpPct = CPanel.askLong(this, "Máu của " + rows.size() + " boss đang hiển thị sẽ còn bao nhiêu %?\n"
                + "(100 = giữ nguyên, 50 = một nửa, 200 = gấp đôi)", "100");
        if (hpPct == null) {
            return;
        }
        Long damePct = CPanel.askLong(this, "Sát thương sẽ còn bao nhiêu %?\n"
                + "(hạ ô này là hạ luôn sát thương mọi chiêu của boss)", "100");
        if (damePct == null) {
            return;
        }
        if (hpPct < 1 || hpPct > 10000 || damePct < 0 || damePct > 10000) {
            CPanel.error(this, "Phần trăm phải trong khoảng: máu 1–10000, sát thương 0–10000.");
            return;
        }
        if (!CPanel.confirm(this, "Đổi máu về " + hpPct + "% và sát thương về " + damePct + "% cho "
                + rows.size() + " boss đang hiển thị?")) {
            return;
        }
        CPanel.async(this, "chỉnh số boss hàng loạt", () -> {
            java.util.Set<Integer> done = new java.util.HashSet<>();
            for (Row r : rows) {
                Boss b = r.boss;
                if (b == null || b.data == null || !done.add((int) b.id)) {
                    continue;
                }
                for (int form = 0; form < b.data.length; form++) {
                    nro.models.boss.BossData d = b.data[form];
                    if (d == null) {
                        continue;
                    }
                    int baseHp = d.getHp() == null || d.getHp().length == 0 ? 0 : d.getHp()[0];
                    long newHp = Math.max(1, baseHp * hpPct / 100);
                    long newDame = Math.max(0, (long) d.getDame() * damePct / 100);
                    BossTuning.set((int) b.id, form, BossTuning.HP, (int) Math.min(Integer.MAX_VALUE, newHp));
                    BossTuning.set((int) b.id, form, BossTuning.DAME, (int) Math.min(Integer.MAX_VALUE, newDame));
                }
            }
            BossTuning.save();
            return done.size();
        }, n -> {
            CPanel.log("Đã chỉnh hàng loạt " + n + " boss: máu " + hpPct + "%, sát thương " + damePct + "%.");
            CPanel.info(this, "Đã chỉnh " + n + " boss và lưu vào data/bosstuning.properties.");
            refresh();
        });
    }

    // =====================================================================
    // BẢNG
    // =====================================================================
    private static final class Model extends AbstractTableModel {

        private final String[] cols = {"ID", "Tên boss", "Trạng thái", "Hình dạng", "Map", "Khu", "HP"};
        private List<Row> rows = new ArrayList<>();

        void set(List<Row> r) {
            this.rows = r;
            fireTableDataChanged();
        }

        Row get(int i) {
            return rows.get(i);
        }

        @Override
        public int getRowCount() {
            return rows.size();
        }

        @Override
        public int getColumnCount() {
            return cols.length;
        }

        @Override
        public String getColumnName(int c) {
            return cols[c];
        }

        @Override
        public Object getValueAt(int r, int c) {
            Row x = rows.get(r);
            switch (c) {
                case 0:
                    return x.id;
                case 1:
                    return x.name;
                case 2:
                    return statusText(x);
                case 3:
                    return x.level < 0 ? "-" : String.valueOf(x.level);
                case 4:
                    return x.inMap ? x.mapName + " (" + x.mapId + ")" : "-";
                case 5:
                    return x.inMap ? String.valueOf(x.zoneId) : "-";
                default:
                    return x.inMap ? CPanel.num(x.hp) + " / " + CPanel.num(x.hpFull) : "-";
            }
        }

        private static String statusText(Row x) {
            switch (x.status) {
                case "REST":
                    return "Đang nghỉ";
                case "RESPAWN":
                    return "Sắp ra";
                case "JOIN_MAP":
                    return "Đang vào map";
                case "CHAT_S":
                    return "Đang nói (vào)";
                case "ACTIVE":
                    return "Đang ở map";
                case "DIE":
                    return "Đã chết";
                case "CHAT_E":
                    return "Đang nói (chết)";
                case "LEAVE_MAP":
                    return "Đang rời map";
                case "AFK":
                    return "Đứng yên";
                default:
                    return x.status;
            }
        }
    }
}
