package nro.models.cpanel;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.FlowLayout;
import java.awt.Window;
import java.util.ArrayList;
import java.util.List;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextArea;
import javax.swing.ListSelectionModel;
import javax.swing.SwingUtilities;
import javax.swing.table.AbstractTableModel;
import nro.models.boss.Boss;
import nro.models.boss.drop.BangRoiBoss;
import nro.models.boss.drop.MucRoi;

/**
 * Hộp thoại "Đồ rơi" của một boss: xem và sửa {@link BangRoiBoss} cho đúng con đó.
 *
 * <p>Mỗi dòng là một mục rơi. Cột <b>Nhóm</b> quyết định cách quay:
 * <ul>
 *   <li><b>0</b> — quay riêng: tung đúng {@code Tỉ lệ %} một lần, trúng thì rơi.</li>
 *   <li><b>&gt; 0</b> — quay chung: mọi dòng cùng số nhóm nằm trên MỘT vòng quay 100 %,
 *       {@code Tỉ lệ} là phần của vòng đó.</li>
 * </ul>
 *
 * <p>Cột <b>Id vật phẩm</b> nhận nhiều id cách nhau bởi dấu phẩy ("16,17,18"): lúc trúng thì
 * bốc ngẫu nhiên một cái.
 *
 * <p>Bấm "Áp dụng" là có hiệu lực ngay lần hạ boss kế tiếp; bấm "Áp dụng và lưu" thì ghi
 * {@code data/bossdrop_table.json} để khởi động lại vẫn còn.
 */
final class BossDropTableDialog extends JDialog {

    private final int bossId;
    private final JLabel trangThai = new JLabel();
    private final List<MucRoi> ds = new ArrayList<>();
    private final Model model = new Model();
    private final JTable table = new JTable(model);

    BossDropTableDialog(Component parent, Boss boss) {
        super(windowOf(parent), "Đồ rơi: " + boss.name + " (id " + boss.id + ")",
                ModalityType.APPLICATION_MODAL);
        this.bossId = (int) boss.id;
        for (MucRoi m : BangRoiBoss.cua(bossId)) {
            ds.add(new MucRoi(m.ids == null ? new int[0] : m.ids.clone(),
                    m.slMin, m.slMax, m.tiLe, m.nhom, m.ghiChu));
        }

        JPanel bac = new JPanel(new BorderLayout(0, 6));
        bac.setBorder(BorderFactory.createEmptyBorder(8, 8, 4, 8));
        bac.add(new JLabel("<html>"
                + "<b>Nhóm 0</b> = quay riêng từng dòng (tỉ lệ % độc lập).<br>"
                + "<b>Nhóm &gt; 0</b> = các dòng cùng số nhóm chung MỘT vòng quay 100 %, tỉ lệ là phần của vòng.<br>"
                + "<b>Id vật phẩm</b> nhận nhiều id cách nhau bởi dấu phẩy, trúng thì bốc ngẫu nhiên một cái."
                + "</html>"), BorderLayout.NORTH);
        bac.add(trangThai, BorderLayout.CENTER);
        capNhatTrangThai();

        // Phần đồ rơi GỐC — chỉ để xem, vì nó nằm trong reward() viết tay của lớp boss.
        JTextArea goc = new JTextArea(chuoiGoc(), 5, 80);
        goc.setEditable(false);
        goc.setLineWrap(true);
        goc.setWrapStyleWord(true);
        goc.setBackground(new java.awt.Color(0xF4, 0xF4, 0xF4));
        JScrollPane cuonGoc = new JScrollPane(goc);
        cuonGoc.setBorder(BorderFactory.createTitledBorder(
                "Đồ rơi GỐC của boss (viết trong code — chỉ để xem, sửa ở đây không được)"));
        bac.add(cuonGoc, BorderLayout.SOUTH);

        table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        table.setRowHeight(22);

        JPanel nut = new JPanel(new WrapLayout(FlowLayout.LEFT, 8, 8));
        JButton them = new JButton("Thêm dòng");
        JButton xoa = new JButton("Xoá dòng đã chọn");
        JButton apDung = new JButton("Áp dụng ngay");
        JButton luu = new JButton("Áp dụng và lưu vào file");
        JButton macDinh = new JButton("Về mặc định (cả server)");
        JButton dong = new JButton("Đóng");
        nut.add(them);
        nut.add(xoa);
        nut.add(apDung);
        nut.add(luu);
        nut.add(macDinh);
        nut.add(dong);

        them.addActionListener(e -> {
            ds.add(new MucRoi(new int[]{0}, 1, 1, 10, 0, ""));
            model.fireTableDataChanged();
            capNhatTrangThai();
        });
        xoa.addActionListener(e -> {
            int v = table.getSelectedRow();
            if (v < 0) {
                CPanel.error(this, "Hãy chọn một dòng.");
                return;
            }
            stopEdit();
            ds.remove(table.convertRowIndexToModel(v));
            model.fireTableDataChanged();
            capNhatTrangThai();
        });
        apDung.addActionListener(e -> apDung(false));
        luu.addActionListener(e -> apDung(true));
        macDinh.addActionListener(e -> {
            if (JOptionPane.showConfirmDialog(this,
                    "Bỏ toàn bộ chỉnh sửa của MỌI boss và quay về bảng gốc trong code?\n"
                    + "Thao tác này xoá luôn file data/bossdrop_table.json.",
                    "Xác nhận", JOptionPane.YES_NO_OPTION) != JOptionPane.YES_OPTION) {
                return;
            }
            BangRoiBoss.veMacDinh();
            ds.clear();
            for (MucRoi m : BangRoiBoss.cua(bossId)) {
                ds.add(new MucRoi(m.ids == null ? new int[0] : m.ids.clone(),
                        m.slMin, m.slMax, m.tiLe, m.nhom, m.ghiChu));
            }
            model.fireTableDataChanged();
            capNhatTrangThai();
        });
        dong.addActionListener(e -> dispose());

        setLayout(new BorderLayout(6, 6));
        add(bac, BorderLayout.NORTH);
        add(new JScrollPane(table), BorderLayout.CENTER);
        add(nut, BorderLayout.SOUTH);
        setSize(900, 560);
        setLocationRelativeTo(parent);
    }

    /**
     * Câu giải thích ngay dưới phần chú thích. Bảng rỗng là chuyện BÌNH THƯỜNG với boss cũ —
     * đồ rơi của chúng nằm trong {@code reward()} viết tay ở từng lớp boss (85 file, 237 lệnh
     * thả đồ), bảng này không đọc được vào. Không nói rõ thì người dùng mở ra thấy trống trơn
     * lại tưởng hỏng.
     */
    private void capNhatTrangThai() {
        if (ds.isEmpty()) {
            trangThai.setText("<html><span style='color:#b00'><b>Bảng đang TRỐNG.</b></span> "
                    + "Con này chưa khai báo dòng nào, nên đồ rơi hiện tại của nó "
                    + "<b>nằm trong code riêng của boss</b> — bảng này không đọc được vào và "
                    + "cũng không sửa được từ đây.<br>"
                    + "Bấm <b>Thêm dòng</b> để cho nó rơi <b>THÊM</b> món mới; phần đồ rơi cũ "
                    + "vẫn giữ nguyên, không mất.<br>"
                    + "<i>Chỉ 5 con mới (2 Fu, 2 Siêu Thần God, Lão Dê) là đã gỡ hết phần viết "
                    + "cứng, nên với chúng bảng này là toàn bộ đồ rơi.</i></html>");
        } else {
            trangThai.setText("<html><span style='color:#060'><b>Có " + ds.size()
                    + " dòng.</b></span> Đây là phần <b>THÊM</b>, chạy sau đồ rơi viết trong "
                    + "code của boss (trừ 5 con mới: 2 Fu, 2 Siêu Thần God, Lão Dê — với chúng "
                    + "bảng này là toàn bộ đồ rơi).</html>");
        }
    }

    /** Liệt kê đồ rơi gốc của boss thành chữ cho người xem. */
    private String chuoiGoc() {
        java.util.List<MucRoi> ds = BangRoiBoss.goc(bossId);
        if (ds.isEmpty()) {
            return "Con này chưa được khai báo đồ rơi gốc trong BangRoiBoss.khaiBaoGoc().\n"
                    + "Nếu nó vẫn rơi đồ trong game thì phần đó đang nằm trong reward() của lớp boss\n"
                    + "mà chưa ai chép sang, nên bảng ở đây chưa liệt kê được.";
        }
        StringBuilder sb = new StringBuilder();
        for (MucRoi m : ds) {
            sb.append("• ").append(m.tenVatPham());
            String sl = m.chuoiSoLuong();
            if (!sl.isEmpty()) {
                sb.append(" ").append(sl);
            }
            sb.append("  —  ").append(m.chuoiTiLe());
            if (m.ghiChu != null && !m.ghiChu.isEmpty()) {
                sb.append("   (").append(m.ghiChu).append(")");
            }
            sb.append("\n");
        }
        return sb.toString();
    }

    private void stopEdit() {
        if (table.isEditing()) {
            table.getCellEditor().stopCellEditing();
        }
    }

    private void apDung(boolean ghiFile) {
        stopEdit();
        for (MucRoi m : ds) {
            if (m.ids == null || m.ids.length == 0) {
                CPanel.error(this, "Có dòng chưa điền id vật phẩm.");
                return;
            }
            if (m.tiLe < 0 || m.tiLe > 100) {
                CPanel.error(this, "Tỉ lệ phải trong khoảng 0–100.");
                return;
            }
            if (m.slMin < 1 || m.slMax < m.slMin) {
                CPanel.error(this, "Số lượng không hợp lệ (nhỏ nhất ≥ 1 và ≤ lớn nhất).");
                return;
            }
        }
        // Cảnh báo (không chặn): tổng một nhóm quay chung vượt 100 thì mấy dòng cuối
        // không bao giờ trúng.
        for (int nhom = 1; nhom <= 9; nhom++) {
            int tong = 0;
            for (MucRoi m : ds) {
                if (m.nhom == nhom) {
                    tong += m.tiLe;
                }
            }
            if (tong > 100) {
                CPanel.error(this, "Nhóm " + nhom + " có tổng tỉ lệ " + tong
                        + " % (> 100): những dòng cuối nhóm sẽ không bao giờ trúng.");
                return;
            }
        }
        BangRoiBoss.dat(bossId, ds);
        if (ghiFile && !BangRoiBoss.luu()) {
            CPanel.error(this, "Đã áp dụng nhưng ghi file thất bại, xem log server.");
            return;
        }
        JOptionPane.showMessageDialog(this, ghiFile
                ? "Đã áp dụng và lưu vào data/bossdrop_table.json."
                : "Đã áp dụng. Chưa lưu file, khởi động lại sẽ mất.");
    }

    private static Window windowOf(Component c) {
        return c == null ? null : SwingUtilities.getWindowAncestor(c);
    }

    private final class Model extends AbstractTableModel {

        private final String[] cot = {"Id vật phẩm", "Tên", "SL nhỏ nhất", "SL lớn nhất",
            "Tỉ lệ %", "Nhóm", "Ghi chú"};

        @Override
        public int getRowCount() {
            return ds.size();
        }

        @Override
        public int getColumnCount() {
            return cot.length;
        }

        @Override
        public String getColumnName(int c) {
            return cot[c];
        }

        @Override
        public boolean isCellEditable(int r, int c) {
            return c != 1;      // cột "Tên" chỉ để xem
        }

        @Override
        public Object getValueAt(int r, int c) {
            MucRoi m = ds.get(r);
            return switch (c) {
                case 0 -> m.chuoiIds();
                case 1 -> m.tenVatPham();
                case 2 -> m.slMin;
                case 3 -> m.slMax;
                case 4 -> m.tiLe;
                case 5 -> m.nhom;
                default -> m.ghiChu == null ? "" : m.ghiChu;
            };
        }

        @Override
        public void setValueAt(Object v, int r, int c) {
            MucRoi m = ds.get(r);
            String s = v == null ? "" : String.valueOf(v).trim();
            try {
                switch (c) {
                    case 0 -> m.ids = MucRoi.docIds(s);
                    case 2 -> m.slMin = Integer.parseInt(s);
                    case 3 -> m.slMax = Integer.parseInt(s);
                    case 4 -> m.tiLe = Integer.parseInt(s);
                    case 5 -> m.nhom = Integer.parseInt(s);
                    default -> m.ghiChu = s;
                }
            } catch (NumberFormatException e) {
                // số nhập sai thì giữ nguyên giá trị cũ
            }
            fireTableRowsUpdated(r, r);
        }
    }
}
