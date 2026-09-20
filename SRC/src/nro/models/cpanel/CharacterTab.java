package nro.models.cpanel;

import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.GridLayout;
import java.util.ArrayList;
import java.util.List;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTable;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.ListSelectionModel;
import javax.swing.table.DefaultTableModel;
import nro.models.cpanel.CPanel.NeedConfirmException;
import nro.models.cpanel.CPanel.Task;
import nro.models.cpanel.CharacterOps.CharRow;

/**
 * Tab "Nhân vật": tìm theo tên, xem chỉ số, cộng tiền / đặt lại nhiệm vụ / tặng vật phẩm.
 * Online -> sửa Player trong bộ nhớ; offline -> sửa DB (xem CharacterOps).
 */
final class CharacterTab extends JPanel {

    private static final String[] COLS = {"ID", "Tên nhân vật", "Tài khoản", "Hành tinh", "Trạng thái"};

    private final JTextField searchField = new JTextField(18);
    private final DefaultTableModel model = new DefaultTableModel(COLS, 0) {
        @Override
        public boolean isCellEditable(int r, int c) {
            return false;
        }

        @Override
        public Class<?> getColumnClass(int c) {
            return c == 0 ? Long.class : String.class;
        }
    };
    private final JTable table = new JTable(model);
    private final JTextArea detail = new JTextArea(12, 40);
    private final List<CharRow> rows = new ArrayList<>();
    private final List<JButton> needSelection = new ArrayList<>();
    private CharRow current;

    /** Công việc có tham số force (true = admin đã xác nhận ghi DB dù nghi phiên chưa đóng). */
    private interface ForceTask {

        String run(boolean force) throws Exception;
    }

    CharacterTab() {
        super(new BorderLayout(6, 6));
        setBorder(BorderFactory.createEmptyBorder(8, 8, 8, 8));

        JPanel top = new JPanel(new FlowLayout(FlowLayout.LEFT));
        top.add(new JLabel("Tên nhân vật:"));
        top.add(searchField);
        JButton btnSearch = new JButton("Tìm");
        top.add(btnSearch);
        top.add(new JLabel("   (nhập một phần tên; để trống = 100 nhân vật đầu)"));
        add(top, BorderLayout.NORTH);

        table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        table.setAutoCreateRowSorter(true);
        table.setRowHeight(22);

        detail.setEditable(false);
        JScrollPane detailScroll = new JScrollPane(detail);
        detailScroll.setBorder(BorderFactory.createTitledBorder("Chi tiết nhân vật"));

        JSplitPane split = new JSplitPane(JSplitPane.VERTICAL_SPLIT, new JScrollPane(table), detailScroll);
        split.setResizeWeight(0.55);
        add(split, BorderLayout.CENTER);

        JPanel side = new JPanel(new GridLayout(0, 1, 4, 6));
        side.setBorder(BorderFactory.createTitledBorder("Thao tác"));
        side.add(sel(new JButton("Tải lại chi tiết"), this::reloadDetail));
        side.add(sel(new JButton("Cộng / trừ vàng"), () -> addCurrency(CharacterOps.GOLD)));
        side.add(sel(new JButton("Cộng / trừ ngọc"), () -> addCurrency(CharacterOps.GEM)));
        side.add(sel(new JButton("Cộng / trừ hồng ngọc"), () -> addCurrency(CharacterOps.RUBY)));
        side.add(sel(new JButton("Đặt lại nhiệm vụ"), this::resetTask));
        side.add(sel(new JButton("Tặng vật phẩm"), this::giveItem));
        JPanel sideWrap = new JPanel(new BorderLayout());
        sideWrap.add(side, BorderLayout.NORTH);
        add(sideWrap, BorderLayout.EAST);

        btnSearch.addActionListener(e -> search());
        searchField.addActionListener(e -> search());
        table.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) {
                onSelect();
            }
        });
        updateButtons();
    }

    private JButton sel(JButton b, Runnable action) {
        b.addActionListener(e -> action.run());
        needSelection.add(b);
        return b;
    }

    private void updateButtons() {
        boolean has = current != null;
        for (JButton b : needSelection) {
            b.setEnabled(has);
        }
    }

    private void search() {
        String kw = searchField.getText();
        CPanel.async(this, "tìm nhân vật", () -> CharacterOps.search(kw), list -> {
            rows.clear();
            rows.addAll(list);
            model.setRowCount(0);
            for (CharRow r : list) {
                model.addRow(new Object[]{r.id, r.name, r.username == null ? "(id " + r.accountId + ")" : r.username,
                    CPanel.planet(r.gender), r.online ? "ONLINE" : "offline"});
            }
            current = null;
            detail.setText(list.isEmpty() ? "Không tìm thấy nhân vật nào." : "Chọn một nhân vật để xem chi tiết.");
            updateButtons();
        });
    }

    private void onSelect() {
        int v = table.getSelectedRow();
        if (v < 0) {
            return;
        }
        int m = table.convertRowIndexToModel(v);
        if (m < 0 || m >= rows.size()) {
            return;
        }
        current = rows.get(m);
        reloadDetail();
    }

    private void reloadDetail() {
        if (current == null) {
            return;
        }
        long id = current.id;
        CPanel.async(this, "tải chi tiết nhân vật", () -> CharacterOps.load(id), r -> {
            if (current == null || current.id != r.id) {
                return;
            }
            current = r;
            showDetail(r);
            updateButtons();
        });
    }

    private void showDetail(CharRow r) {
        StringBuilder sb = new StringBuilder();
        sb.append("Nhân vật   : ").append(r.name).append("  (id ").append(r.id).append(")\n");
        sb.append("Tài khoản  : ").append(r.username == null ? "?" : r.username).append("  (id ").append(r.accountId).append(")\n");
        sb.append("Hành tinh  : ").append(CPanel.planet(r.gender)).append("\n");
        sb.append("Trạng thái : ").append(r.online
                ? "ĐANG ONLINE - " + (r.map == null ? "" : r.map) + "\n             (số liệu lấy từ bộ nhớ; thao tác sẽ sửa trực tiếp trong game)"
                : "offline (số liệu lấy từ DB; thao tác sẽ ghi thẳng DB)").append("\n\n");
        sb.append("Sức mạnh   : ").append(CPanel.num(r.power)).append("\n");
        sb.append("Vàng       : ").append(CPanel.num(r.gold)).append("\n");
        sb.append("Ngọc       : ").append(CPanel.num(r.gem)).append("\n");
        sb.append("Hồng ngọc  : ").append(CPanel.num(r.ruby)).append("\n\n");
        sb.append("Nhiệm vụ   : NV ").append(r.taskId).append(" - ").append(CharacterOps.taskName(r.taskId)).append("\n");
        sb.append("Bước       : ").append(r.taskIndex).append(" - ").append(CharacterOps.subName(r.taskId, r.taskIndex))
                .append("  (đã làm ").append(r.taskCount).append(")\n");
        detail.setText(sb.toString());
        detail.setCaretPosition(0);
        // cập nhật cột trạng thái trong bảng
        int idx = -1;
        for (int i = 0; i < rows.size(); i++) {
            if (rows.get(i).id == r.id) {
                rows.set(i, r);
                idx = i;
                break;
            }
        }
        String st = r.online ? "ONLINE" : "offline";
        if (idx >= 0 && idx < model.getRowCount() && !st.equals(model.getValueAt(idx, 4))) {
            model.setValueAt(st, idx, 4);
        }
    }

    /** Chạy thao tác; nếu cần xác nhận (phiên trước chưa đóng) thì hỏi rồi chạy lại với force. */
    private void runOp(String what, ForceTask op) {
        Task<String> first = () -> op.run(false);
        CPanel.async(this, what, first, this::done, e -> {
            if (e instanceof NeedConfirmException) {
                if (CPanel.confirm(this, e.getMessage())) {
                    CPanel.async(this, what, () -> op.run(true), this::done);
                }
            } else {
                CPanel.error(this, "Lỗi khi " + what + ":\n" + CPanel.messageOf(e));
            }
        });
    }

    private void done(String msg) {
        CPanel.info(this, msg);
        reloadDetail();
    }

    private void addCurrency(int kind) {
        CharRow r = current;
        String cname = CharacterOps.CURRENCY_NAME[kind];
        long cur = kind == CharacterOps.GOLD ? r.gold : kind == CharacterOps.GEM ? r.gem : r.ruby;
        Long v = CPanel.askLong(this, "Số " + cname + " cộng cho \"" + r.name + "\" (hiện có " + CPanel.num(cur)
                + ").\nNhập số âm để trừ.", "0");
        if (v == null || v == 0) {
            return;
        }
        long delta = v;
        if (delta < 0 && !CPanel.confirm(this, "TRỪ " + CPanel.num(-delta) + " " + cname + " của \"" + r.name + "\"?")) {
            return;
        }
        if (delta > 0 && kind == CharacterOps.GOLD && delta >= 10_000_000_000L
                && !CPanel.confirm(this, "Cộng số vàng rất lớn (" + CPanel.num(delta) + ")?")) {
            return;
        }
        runOp("cộng/trừ " + cname, force -> CharacterOps.addCurrency(r.id, r.name, kind, delta, force));
    }

    private void resetTask() {
        CharRow r = current;
        Long v = CPanel.askLong(this, "Đặt nhiệm vụ chính của \"" + r.name + "\" về NV số (bước 0, xóa tiến độ, KHÔNG trao thưởng).\n"
                + "Hiện tại: NV " + r.taskId + " bước " + r.taskIndex + ". Để nguyên = làm lại nhiệm vụ hiện tại từ đầu.",
                String.valueOf(r.taskId));
        if (v == null) {
            return;
        }
        int taskId = v.intValue();
        if (!CPanel.confirm(this, "ĐẶT LẠI nhiệm vụ của \"" + r.name + "\" về NV " + taskId + " - "
                + CharacterOps.taskName(taskId) + ", bước 0?\nTiến độ hiện tại sẽ mất.")) {
            return;
        }
        runOp("đặt lại nhiệm vụ", force -> CharacterOps.resetTask(r.id, r.name, taskId, force));
    }

    private void giveItem() {
        CharRow r = current;
        JTextField idField = new JTextField(8);
        JTextField qtyField = new JTextField("1", 8);
        JLabel nameLabel = new JLabel(" ");
        idField.addCaretListener(e -> {
            try {
                nameLabel.setText(CharacterOps.template(Integer.parseInt(idField.getText().trim())).name);
            } catch (Exception ex) {
                nameLabel.setText("(id không hợp lệ)");
            }
        });
        JPanel p = new JPanel(new GridLayout(0, 2, 4, 4));
        p.add(new JLabel("ID vật phẩm:"));
        p.add(idField);
        p.add(new JLabel("Tên:"));
        p.add(nameLabel);
        p.add(new JLabel("Số lượng:"));
        p.add(qtyField);
        int c = JOptionPane.showConfirmDialog(this, p, "Tặng vật phẩm cho \"" + r.name + "\"",
                JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);
        if (c != JOptionPane.OK_OPTION) {
            return;
        }
        int itemId;
        int qty;
        try {
            itemId = Integer.parseInt(idField.getText().trim());
            qty = Integer.parseInt(qtyField.getText().trim().replace(".", ""));
        } catch (NumberFormatException e) {
            CPanel.error(this, "ID hoặc số lượng không hợp lệ.");
            return;
        }
        runOp("tặng vật phẩm", force -> CharacterOps.giveItem(r.id, r.name, itemId, qty, force));
    }
}
