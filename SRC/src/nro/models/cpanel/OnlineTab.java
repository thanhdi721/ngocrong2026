package nro.models.cpanel;

import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.GridLayout;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.ListSelectionModel;
import javax.swing.SwingUtilities;
import javax.swing.table.DefaultTableModel;
import nro.models.player.Player;
import nro.models.server.Client;
import nro.models.services.Service;

/**
 * Tab "Người chơi online": danh sách tự làm mới 3 giây/lần (chỉ đọc bộ nhớ, trên luồng nền).
 */
final class OnlineTab extends JPanel {

    private static final int REFRESH_SECONDS = 3;
    private static final String[] COLS = {"ID", "Nhân vật", "Tài khoản", "Hành tinh", "Sức mạnh", "Bản đồ", "Khu"};

    private static final class Row {

        long id;
        String name;
        String account;
        int gender;
        long power;
        String map;
        int zone;
    }

    private final DefaultTableModel model = new DefaultTableModel(COLS, 0) {
        @Override
        public boolean isCellEditable(int r, int c) {
            return false;
        }

        @Override
        public Class<?> getColumnClass(int c) {
            return c == 0 || c == 4 ? Long.class : c == 6 ? Integer.class : String.class;
        }
    };
    private final JTable table = new JTable(model);
    private final JLabel countLabel = new JLabel(" ");
    private final List<Row> rows = new ArrayList<>();
    private final AtomicBoolean refreshing = new AtomicBoolean();
    private ScheduledFuture<?> future;

    OnlineTab() {
        super(new BorderLayout(6, 6));
        setBorder(BorderFactory.createEmptyBorder(8, 8, 8, 8));

        JPanel top = new JPanel(new FlowLayout(FlowLayout.LEFT));
        JButton btnRefresh = new JButton("Làm mới ngay");
        top.add(btnRefresh);
        top.add(countLabel);
        top.add(new JLabel("   (tự làm mới mỗi " + REFRESH_SECONDS + " giây)"));
        add(top, BorderLayout.NORTH);

        table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        table.setAutoCreateRowSorter(true);
        table.setRowHeight(22);
        add(new JScrollPane(table), BorderLayout.CENTER);

        JPanel side = new JPanel(new GridLayout(0, 1, 4, 6));
        side.setBorder(BorderFactory.createTitledBorder("Thao tác"));
        JButton btnKick = new JButton("Kick");
        JButton btnMsg = new JButton("Gửi thông báo tới người này");
        JButton btnAll = new JButton("Thông báo toàn server");
        side.add(btnKick);
        side.add(btnMsg);
        side.add(btnAll);
        JPanel sideWrap = new JPanel(new BorderLayout());
        sideWrap.add(side, BorderLayout.NORTH);
        add(sideWrap, BorderLayout.EAST);

        btnRefresh.addActionListener(e -> refresh());
        btnKick.addActionListener(e -> kick());
        btnMsg.addActionListener(e -> sendOne());
        btnAll.addActionListener(e -> sendAll());
    }

    void startAutoRefresh() {
        future = CPanel.TIMER.scheduleWithFixedDelay(this::refresh, 0, REFRESH_SECONDS, TimeUnit.SECONDS);
    }

    void stopAutoRefresh() {
        if (future != null) {
            future.cancel(false);
        }
    }

    /** Đọc danh sách online trên luồng nền (không phải luồng Swing), rồi đổ vào bảng. */
    private void refresh() {
        if (!refreshing.compareAndSet(false, true)) {
            return;
        }
        Runnable job = () -> {
            List<Row> list = new ArrayList<>();
            try {
                for (Player p : Client.gI().getPlayersSnapshot()) {
                    try {
                        if (!p.isPl() || p.name == null) {
                            continue;
                        }
                        Row r = new Row();
                        r.id = p.id;
                        r.name = p.name;
                        r.account = p.getSession() != null ? p.getSession().uu : "";
                        r.gender = p.gender;
                        r.power = p.nPoint != null ? p.nPoint.power : 0;
                        if (p.zone != null && p.zone.map != null) {
                            r.map = p.zone.map.mapName + " (" + p.zone.map.mapId + ")";
                            r.zone = p.zone.zoneId;
                        } else {
                            r.map = "(đang chuyển map)";
                            r.zone = -1;
                        }
                        list.add(r);
                    } catch (Exception ignored) {
                    }
                }
                list.sort((a, b) -> a.name.compareToIgnoreCase(b.name));
            } finally {
                SwingUtilities.invokeLater(() -> {
                    fill(list);
                    refreshing.set(false);
                });
            }
        };
        if (SwingUtilities.isEventDispatchThread()) {
            CPanel.TIMER.submit(job);
        } else {
            job.run();
        }
    }

    private void fill(List<Row> list) {
        Row sel = selected();
        Long keep = sel != null ? sel.id : null;
        rows.clear();
        rows.addAll(list);
        model.setRowCount(0);
        for (Row r : list) {
            model.addRow(new Object[]{r.id, r.name, r.account, CPanel.planet(r.gender), r.power, r.map, r.zone});
        }
        countLabel.setText("Đang online: " + list.size());
        if (keep != null) {
            for (int i = 0; i < rows.size(); i++) {
                if (rows.get(i).id == keep) {
                    int v = table.convertRowIndexToView(i);
                    if (v >= 0) {
                        table.setRowSelectionInterval(v, v);
                    }
                    break;
                }
            }
        }
    }

    private Row selected() {
        int v = table.getSelectedRow();
        if (v < 0) {
            return null;
        }
        int m = table.convertRowIndexToModel(v);
        return m >= 0 && m < rows.size() ? rows.get(m) : null;
    }

    private Row requireSelected() {
        Row r = selected();
        if (r == null) {
            CPanel.error(this, "Hãy chọn một người chơi trong bảng.");
        }
        return r;
    }

    private static Player online(long id) throws CPanel.CPanelException {
        Player p = Client.gI().getPlayer(id);
        if (p == null || p.getSession() == null) {
            throw new CPanel.CPanelException("Người chơi đã offline.");
        }
        return p;
    }

    private void kick() {
        Row r = requireSelected();
        if (r == null || !CPanel.confirm(this, "Kick \"" + r.name + "\" khỏi server?\n(Dữ liệu được lưu như khi thoát bình thường.)")) {
            return;
        }
        CPanel.async(this, "kick người chơi", () -> {
            Player p = online(r.id);
            // Giống menu admin "Kick": ngắt phiên, Client.remove() tự lưu dữ liệu
            Client.gI().kickSession(p.getSession());
            return p.name;
        }, name -> {
            CPanel.log("Kick người chơi " + name + " (id " + r.id + ")");
            refresh();
        });
    }

    private void sendOne() {
        Row r = requireSelected();
        if (r == null) {
            return;
        }
        String text = CPanel.askString(this, "Nội dung gửi tới \"" + r.name + "\":", "");
        if (text == null || text.isEmpty()) {
            return;
        }
        CPanel.async(this, "gửi thông báo", () -> {
            Player p = online(r.id);
            Service.gI().sendThongBaoOK(p, text);
            return p.name;
        }, name -> CPanel.log("Gửi thông báo tới " + name + ": " + text));
    }

    private void sendAll() {
        String text = CPanel.askString(this, "Nội dung thông báo toàn server:", "");
        if (text == null || text.isEmpty()) {
            return;
        }
        if (!CPanel.confirm(this, "Gửi tới TẤT CẢ người chơi đang online:\n\n" + text)) {
            return;
        }
        CPanel.async(this, "thông báo toàn server", () -> {
            Service.gI().sendThongBaoAllPlayer(text);
            return null;
        }, x -> CPanel.log("Thông báo toàn server: " + text));
    }
}
