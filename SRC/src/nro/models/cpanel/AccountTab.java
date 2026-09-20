package nro.models.cpanel;

import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.GridLayout;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.ListSelectionModel;
import javax.swing.table.DefaultTableModel;
import nro.models.cpanel.AccountDao.AccountRow;
import nro.models.player.Player;
import nro.models.server.Client;
import nro.models.services.PlayerService;

/**
 * Tab "Tài khoản": danh sách / tìm kiếm / tạo / sửa tài khoản.
 */
final class AccountTab extends JPanel {

    private static final String[] COLS = {"ID", "Tên đăng nhập", "Admin", "Bị ban", "VND", "Tổng nạp",
        "Kích hoạt", "Ngày tạo", "Đang online"};

    private final JTextField searchField = new JTextField(20);
    private final DefaultTableModel model = new DefaultTableModel(COLS, 0) {
        @Override
        public boolean isCellEditable(int r, int c) {
            return false;
        }

        @Override
        public Class<?> getColumnClass(int c) {
            return c == 0 || c == 4 || c == 5 ? Integer.class : String.class;
        }
    };
    private final JTable table = new JTable(model);
    private final JLabel countLabel = new JLabel(" ");
    private final List<AccountRow> rows = new ArrayList<>();
    private final List<JButton> needSelection = new ArrayList<>();

    AccountTab() {
        super(new BorderLayout(6, 6));
        setBorder(BorderFactory.createEmptyBorder(8, 8, 8, 8));

        JPanel top = new JPanel(new FlowLayout(FlowLayout.LEFT));
        top.add(new JLabel("Tên đăng nhập:"));
        top.add(searchField);
        JButton btnSearch = new JButton("Tìm / Làm mới");
        top.add(btnSearch);
        top.add(countLabel);
        add(top, BorderLayout.NORTH);

        table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        table.setAutoCreateRowSorter(true);
        table.setRowHeight(22);
        add(new JScrollPane(table), BorderLayout.CENTER);

        JPanel side = new JPanel(new GridLayout(0, 1, 4, 6));
        side.setBorder(BorderFactory.createTitledBorder("Thao tác"));
        JButton btnCreate = new JButton("Tạo tài khoản mới");
        side.add(btnCreate);
        side.add(sel(new JButton("Đổi mật khẩu"), this::changePassword));
        side.add(sel(new JButton("Bật / tắt admin"), this::toggleAdmin));
        side.add(sel(new JButton("Ban / gỡ ban"), this::toggleBan));
        side.add(sel(new JButton("Cộng / trừ VND"), this::addVnd));
        side.add(sel(new JButton("Kích hoạt / hủy kích hoạt"), this::toggleActive));
        JPanel sideWrap = new JPanel(new BorderLayout());
        sideWrap.add(side, BorderLayout.NORTH);
        add(sideWrap, BorderLayout.EAST);

        btnSearch.addActionListener(e -> reload());
        searchField.addActionListener(e -> reload());
        btnCreate.addActionListener(e -> createAccount());
        table.getSelectionModel().addListSelectionListener(e -> updateButtons());
        updateButtons();
        reload();
    }

    private JButton sel(JButton b, Runnable action) {
        b.addActionListener(e -> action.run());
        needSelection.add(b);
        return b;
    }

    private void updateButtons() {
        boolean has = selected() != null;
        for (JButton b : needSelection) {
            b.setEnabled(has);
        }
    }

    private AccountRow selected() {
        int v = table.getSelectedRow();
        if (v < 0) {
            return null;
        }
        int m = table.convertRowIndexToModel(v);
        return m >= 0 && m < rows.size() ? rows.get(m) : null;
    }

    void reload() {
        String kw = searchField.getText();
        Integer keepId = selected() != null ? selected().id : null;
        CPanel.async(this, "tải danh sách tài khoản", () -> AccountDao.search(kw), list -> {
            rows.clear();
            rows.addAll(list);
            model.setRowCount(0);
            SimpleDateFormat df = new SimpleDateFormat("dd/MM/yyyy HH:mm");
            for (AccountRow r : list) {
                model.addRow(new Object[]{r.id, r.username, r.admin ? "Có" : "", r.ban ? "BỊ BAN" : "",
                    r.vnd, r.tongnap, r.active ? "Đã kích hoạt" : "Chưa",
                    r.createTime == null ? "" : df.format(r.createTime), r.online == null ? "" : r.online});
            }
            countLabel.setText(list.size() + " tài khoản" + (list.size() >= AccountDao.MAX_ROWS
                    ? " (chỉ hiện " + AccountDao.MAX_ROWS + " dòng mới nhất, hãy lọc theo tên)" : ""));
            if (keepId != null) {
                for (int i = 0; i < rows.size(); i++) {
                    if (rows.get(i).id == keepId) {
                        int v = table.convertRowIndexToView(i);
                        if (v >= 0) {
                            table.setRowSelectionInterval(v, v);
                        }
                        break;
                    }
                }
            }
            updateButtons();
        });
    }

    private void createAccount() {
        JTextField user = new JTextField(16);
        JPasswordField pass = new JPasswordField(16);
        JPasswordField pass2 = new JPasswordField(16);
        JCheckBox active = new JCheckBox("Kích hoạt ngay", true);
        JPanel p = new JPanel(new GridLayout(0, 2, 4, 4));
        p.add(new JLabel("Tên đăng nhập:"));
        p.add(user);
        p.add(new JLabel("Mật khẩu:"));
        p.add(pass);
        p.add(new JLabel("Nhập lại mật khẩu:"));
        p.add(pass2);
        p.add(new JLabel(""));
        p.add(active);
        int c = JOptionPane.showConfirmDialog(this, p, "Tạo tài khoản mới", JOptionPane.OK_CANCEL_OPTION,
                JOptionPane.PLAIN_MESSAGE);
        if (c != JOptionPane.OK_OPTION) {
            return;
        }
        String u = user.getText().trim();
        String pw = new String(pass.getPassword());
        if (!pw.equals(new String(pass2.getPassword()))) {
            CPanel.error(this, "Hai lần nhập mật khẩu không khớp.");
            return;
        }
        boolean act = active.isSelected();
        CPanel.async(this, "tạo tài khoản", () -> AccountDao.create(u, pw, act), id -> {
            CPanel.log("Tạo tài khoản \"" + u + "\" (id " + id + ", kích hoạt=" + act + ")");
            CPanel.info(this, "Đã tạo tài khoản \"" + u + "\" (id " + id + ").");
            searchField.setText(u);
            reload();
        });
    }

    private void changePassword() {
        AccountRow r = selected();
        JPasswordField pass = new JPasswordField(16);
        JPasswordField pass2 = new JPasswordField(16);
        JPanel p = new JPanel(new GridLayout(0, 2, 4, 4));
        p.add(new JLabel("Mật khẩu mới:"));
        p.add(pass);
        p.add(new JLabel("Nhập lại:"));
        p.add(pass2);
        int c = JOptionPane.showConfirmDialog(this, p, "Đổi mật khẩu \"" + r.username + "\"",
                JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);
        if (c != JOptionPane.OK_OPTION) {
            return;
        }
        String pw = new String(pass.getPassword());
        if (!pw.equals(new String(pass2.getPassword()))) {
            CPanel.error(this, "Hai lần nhập mật khẩu không khớp.");
            return;
        }
        CPanel.async(this, "đổi mật khẩu", () -> {
            AccountDao.changePassword(r.id, pw);
            return null;
        }, x -> {
            CPanel.log("Đổi mật khẩu tài khoản \"" + r.username + "\" (id " + r.id + ")");
            CPanel.info(this, "Đã đổi mật khẩu cho \"" + r.username + "\".");
        });
    }

    private void toggleAdmin() {
        AccountRow r = selected();
        boolean newVal = !r.admin;
        String msg = newVal
                ? "CẤP QUYỀN ADMIN cho \"" + r.username + "\"?\nAdmin dùng được mọi lệnh GM trong game (tạo đồ, bảo trì...)."
                : "Gỡ quyền admin của \"" + r.username + "\"?";
        if (!CPanel.confirm(this, msg)) {
            return;
        }
        CPanel.async(this, "đổi quyền admin", () -> {
            AccountDao.setAdmin(r.id, newVal);
            return null;
        }, x -> {
            CPanel.log((newVal ? "Cấp" : "Gỡ") + " quyền admin tài khoản \"" + r.username + "\" (id " + r.id + ")");
            reload();
        });
    }

    private void toggleBan() {
        AccountRow r = selected();
        boolean ban = !r.ban;
        if (ban && !CPanel.confirm(this, "BAN tài khoản \"" + r.username + "\"?\n"
                + "Không đăng nhập được nữa; nếu đang online sẽ bị ngắt kết nối sau 5 giây.")) {
            return;
        }
        if (!ban && !CPanel.confirm(this, "Gỡ ban tài khoản \"" + r.username + "\"?")) {
            return;
        }
        CPanel.async(this, ban ? "ban tài khoản" : "gỡ ban", () -> {
            AccountDao.setBan(r.id, ban);
            if (ban) {
                Player p = Client.gI().getPlayerByUser(r.id);
                if (p != null && p.getSession() != null) {
                    // Tái dùng hàm ban có sẵn (ghi ban=1 + báo cho người chơi) rồi tự kick sau 5 giây,
                    // vì Player.update() chỉ kick khi người chơi KHÔNG đứng ở nhà.
                    PlayerService.gI().banPlayer(p);
                    CPanel.TIMER.schedule(() -> CPanel.WORKER.submit(() -> {
                        Player still = Client.gI().getPlayerByUser(r.id);
                        if (still != null && still.getSession() != null) {
                            Client.gI().kickSession(still.getSession());
                            CPanel.log("Đã ngắt kết nối " + still.name + " (tài khoản bị ban).");
                        }
                    }), 5, TimeUnit.SECONDS);
                }
            }
            return null;
        }, x -> {
            CPanel.log((ban ? "BAN" : "Gỡ ban") + " tài khoản \"" + r.username + "\" (id " + r.id + ")");
            reload();
        });
    }

    private void addVnd() {
        AccountRow r = selected();
        Long v = CPanel.askLong(this, "Số VND cộng thêm cho \"" + r.username + "\" (hiện có " + CPanel.num(r.vnd)
                + ").\nNhập số âm để trừ.", "0");
        if (v == null || v == 0) {
            return;
        }
        if (v > Integer.MAX_VALUE || v < Integer.MIN_VALUE) {
            CPanel.error(this, "Số quá lớn.");
            return;
        }
        int delta = v.intValue();
        if (delta < 0 && !CPanel.confirm(this, "TRỪ " + CPanel.num(-delta) + " VND của \"" + r.username + "\"?")) {
            return;
        }
        CPanel.async(this, "cập nhật VND", () -> AccountDao.addVnd(r.id, delta), nv -> {
            CPanel.log((delta >= 0 ? "Cộng " : "Trừ ") + CPanel.num(Math.abs(delta)) + " VND tài khoản \""
                    + r.username + "\" (id " + r.id + ") -> số dư " + CPanel.num(nv));
            reload();
        });
    }

    private void toggleActive() {
        AccountRow r = selected();
        boolean act = !r.active;
        if (!act && !CPanel.confirm(this, "Hủy kích hoạt tài khoản \"" + r.username + "\"?")) {
            return;
        }
        CPanel.async(this, "đổi trạng thái kích hoạt", () -> {
            AccountDao.setActive(r.id, act);
            return null;
        }, x -> {
            CPanel.log((act ? "Kích hoạt" : "Hủy kích hoạt") + " tài khoản \"" + r.username + "\" (id " + r.id + ")");
            reload();
        });
    }
}
