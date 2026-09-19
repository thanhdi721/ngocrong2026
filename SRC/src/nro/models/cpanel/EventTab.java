package nro.models.cpanel;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.GridLayout;
import java.sql.Timestamp;
import java.text.SimpleDateFormat;
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
import nro.models.cpanel.EventOps.GiftEntry;
import nro.models.cpanel.EventOps.GiftRow;
import nro.models.event.EventManager;
import nro.models.event.EventManager.Status;
import nro.models.server.Manager;
import nro.models.services.Service;

/**
 * Tab "Sự kiện": bật/tắt sự kiện, giftcode, thông báo toàn server, điểm sự kiện, tỉ lệ EXP.
 */
final class EventTab extends JPanel {

    private static final String[] EV_COLS = {"Khóa", "Sự kiện", "Đang chạy", "Lần khởi động sau",
        "Bật/tắt lúc chạy", "Boss (trên map / tổng)", "Luồng boss", "Ghi chú"};
    private static final String[] GC_COLS = {"ID", "Code", "Lượt còn (DB)", "Lượt còn (bộ nhớ)", "Ngày tạo", "Hết hạn", "Quà"};

    private final DefaultTableModel evModel = model(EV_COLS);
    private final JTable evTable = new JTable(evModel);
    private final List<Status> evRows = new ArrayList<>();

    private final DefaultTableModel gcModel = model(GC_COLS);
    private final JTable gcTable = new JTable(gcModel);
    private final List<GiftRow> gcRows = new ArrayList<>();

    private final JLabel expLabel = new JLabel();

    private static DefaultTableModel model(String[] cols) {
        return new DefaultTableModel(cols, 0) {
            @Override
            public boolean isCellEditable(int r, int c) {
                return false;
            }
        };
    }

    EventTab() {
        super(new BorderLayout(6, 6));
        setBorder(BorderFactory.createEmptyBorder(8, 8, 8, 8));

        // ---------- sự kiện ----------
        evTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        evTable.setRowHeight(22);
        evTable.getColumnModel().getColumn(7).setPreferredWidth(420);
        JPanel evPanel = new JPanel(new BorderLayout(4, 4));
        evPanel.setBorder(BorderFactory.createTitledBorder("Sự kiện có trong code (lưu ở Config.properties: event.<khóa>)"));
        evPanel.add(new JScrollPane(evTable), BorderLayout.CENTER);
        JPanel evBtns = new JPanel(new FlowLayout(FlowLayout.LEFT));
        evBtns.add(btn("Làm mới", this::refreshEvents));
        evBtns.add(btn("BẬT sự kiện", () -> toggle(true)));
        evBtns.add(btn("TẮT sự kiện", () -> toggle(false)));
        evBtns.add(new JLabel("  Bảng không tự làm mới — bấm Làm mới để xem số boss."));
        evPanel.add(evBtns, BorderLayout.SOUTH);

        // ---------- giftcode ----------
        gcTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        gcTable.setRowHeight(22);
        gcTable.setAutoCreateRowSorter(true);
        gcTable.getColumnModel().getColumn(6).setPreferredWidth(420);
        JPanel gcPanel = new JPanel(new BorderLayout(4, 4));
        gcPanel.setBorder(BorderFactory.createTitledBorder("Giftcode (bảng giftcode + bộ nhớ server)"));
        gcPanel.add(new JScrollPane(gcTable), BorderLayout.CENTER);
        JPanel gcBtns = new JPanel(new FlowLayout(FlowLayout.LEFT));
        gcBtns.add(btn("Tải lại", this::refreshGiftcodes));
        gcBtns.add(btn("Tạo code mới...", this::createGiftcode));
        gcBtns.add(btn("Xóa code", this::deleteGiftcode));
        gcPanel.add(gcBtns, BorderLayout.SOUTH);

        JSplitPane split = new JSplitPane(JSplitPane.VERTICAL_SPLIT, evPanel, gcPanel);
        split.setResizeWeight(0.5);
        add(split, BorderLayout.CENTER);

        // ---------- khác ----------
        JPanel side = new JPanel(new GridLayout(0, 1, 4, 6));
        side.setBorder(BorderFactory.createTitledBorder("Khác"));
        side.add(btn("Thông báo sự kiện toàn server", this::announce));
        side.add(btn("Cộng / trừ điểm sự kiện", this::eventPoint));
        side.add(btn("Đổi tỉ lệ EXP server", this::expRate));
        side.add(expLabel);
        JPanel sideWrap = new JPanel(new BorderLayout());
        sideWrap.add(side, BorderLayout.NORTH);
        add(sideWrap, BorderLayout.EAST);

        refreshExpLabel();
        // Lần đầu: im lặng nếu lỗi (cpanel mở lúc server có thể chưa nạp xong DB/sự kiện)
        CPanel.async(this, "đọc trạng thái sự kiện", () -> EventManager.gI().getStatus(), this::showEvents,
                e -> CPanel.log("Chưa đọc được trạng thái sự kiện: " + CPanel.messageOf(e)));
        CPanel.async(this, "đọc giftcode", EventOps::listGiftcodes, this::showGiftcodes,
                e -> CPanel.log("Chưa đọc được giftcode: " + CPanel.messageOf(e) + " (bấm Tải lại sau)"));
    }

    private static JButton btn(String t, Runnable r) {
        JButton b = new JButton(t);
        b.addActionListener(e -> r.run());
        return b;
    }

    private void refreshExpLabel() {
        expLabel.setText("EXP hiện tại: x" + Manager.RATE_EXP_SERVER);
    }

    // =====================================================================
    // SỰ KIỆN
    // =====================================================================
    private void refreshEvents() {
        CPanel.async(this, "đọc trạng thái sự kiện", () -> EventManager.gI().getStatus(), this::showEvents);
    }

    private void showEvents(List<Status> list) {
        int sel = evTable.getSelectedRow();
        evRows.clear();
        evRows.addAll(list);
        evModel.setRowCount(0);
        for (Status s : list) {
            evModel.addRow(new Object[]{s.def.key, s.def.name, s.running ? "BẬT" : "tắt", s.nextBoot ? "bật" : "tắt",
                s.def.runtime ? "được" : "không (khởi động lại)",
                s.running && s.def.hasBossManager() ? s.bossesOnMap + " / " + s.bosses : "-",
                s.def.hasBossManager() ? (s.managerThread ? "đang chạy" : "chưa start") : "-",
                s.def.note});
        }
        if (sel >= 0 && sel < evModel.getRowCount()) {
            evTable.setRowSelectionInterval(sel, sel);
        }
    }

    private void toggle(boolean on) {
        int r = evTable.getSelectedRow();
        if (r < 0 || r >= evRows.size()) {
            CPanel.error(this, "Chọn một sự kiện trước.");
            return;
        }
        Status s = evRows.get(r);
        String key = s.def.key;
        boolean applyNow;
        if (s.def.runtime) {
            Object[] options = {"Áp dụng NGAY + lưu", "Chỉ lưu (lần khởi động sau)", "Hủy"};
            String warn = "";
            if (on && key.equals("trung_thu")
                    && (BuffOps.itemOrNull(2123) == null || BuffOps.itemOrNull(2124) == null)) {
                warn = "\n\nCẢNH BÁO: Nguyệt thần/Nhật thần rơi item 2123/2124 nhưng item_template KHÔNG có 2 id này"
                        + "\n-> rơi đồ có thể lỗi. Nên thêm item trước khi bật.";
            }
            if (!on) {
                warn = "\n\nTắt ngay sẽ gỡ toàn bộ boss của sự kiện khỏi map (người đang đánh sẽ mất boss)"
                        + (key.equals("lunar_new_year") ? " và gỡ NPC Đường Tăng ở map 0" : "") + ".";
            }
            int c = JOptionPane.showOptionDialog(this, (on ? "BẬT" : "TẮT") + " sự kiện \"" + s.def.name + "\"?" + warn,
                    "Xác nhận", JOptionPane.DEFAULT_OPTION, JOptionPane.WARNING_MESSAGE, null, options, options[2]);
            if (c != 0 && c != 1) {
                return;
            }
            applyNow = c == 0;
        } else {
            if (!CPanel.confirm(this, (on ? "BẬT" : "TẮT") + " sự kiện \"" + s.def.name + "\"?\n\n"
                    + "Sự kiện này KHÔNG bật/tắt được khi server đang chạy.\n"
                    + "Chỉ lưu vào Config.properties (event." + key + "=" + on + "), có hiệu lực ở lần khởi động sau.\n\n"
                    + "Lý do: " + s.def.note)) {
                return;
            }
            applyNow = false;
        }
        CPanel.async(this, (on ? "bật" : "tắt") + " sự kiện", () -> EventManager.gI().setEnabled(key, on, applyNow), msg -> {
            CPanel.log("Sự kiện " + key + ": " + msg);
            CPanel.info(this, msg);
            refreshEvents();
        });
    }

    // =====================================================================
    // GIFTCODE
    // =====================================================================
    private void refreshGiftcodes() {
        CPanel.async(this, "đọc giftcode", EventOps::listGiftcodes, this::showGiftcodes);
    }

    private void showGiftcodes(List<GiftRow> list) {
        gcRows.clear();
        gcRows.addAll(list);
        gcModel.setRowCount(0);
        for (GiftRow g : list) {
            gcModel.addRow(new Object[]{g.id, g.code, g.countLeft == -1 ? "không giới hạn" : String.valueOf(g.countLeft),
                g.inMemory ? (g.memCountLeft >= 999999999 ? "không giới hạn" : String.valueOf(g.memCountLeft)) : "CHƯA NẠP",
                g.created, g.expired, EventOps.summarize(g.detail)});
        }
    }

    private void createGiftcode() {
        JTextField code = new JTextField(16);
        JTextField count = new JTextField("100", 8);
        String def = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new java.util.Date(System.currentTimeMillis() + 30L * 86400000L));
        JTextField exp = new JTextField(def, 16);
        JTextArea gifts = new JTextArea("# <id> <số lượng> [optId:param ...]   (-1 vàng, -2 ngọc, -3 hồng ngọc)\n"
                + "457 10 30:0\n-2 100\n", 8, 40);
        JPanel form = new JPanel(new GridLayout(0, 2, 4, 4));
        form.add(new JLabel("Mã (A-Z a-z 0-9 _):"));
        form.add(code);
        form.add(new JLabel("Số lượt (-1 = không giới hạn):"));
        form.add(count);
        form.add(new JLabel("Hết hạn (yyyy-MM-dd HH:mm:ss):"));
        form.add(exp);
        JPanel p = new JPanel(new BorderLayout(4, 4));
        p.add(form, BorderLayout.NORTH);
        JScrollPane gs = new JScrollPane(gifts);
        gs.setBorder(BorderFactory.createTitledBorder("Nội dung quà (mỗi dòng 1 món; option 30:0 = không giao dịch)"));
        gs.setPreferredSize(new Dimension(520, 200));
        p.add(gs, BorderLayout.CENTER);
        p.add(new JLabel("<html>Người chơi cần số ô trống ≥ số dòng quà. Mỗi nhân vật nhập 1 lần.</html>"), BorderLayout.SOUTH);
        int c = JOptionPane.showConfirmDialog(this, p, "Tạo giftcode", JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);
        if (c != JOptionPane.OK_OPTION) {
            return;
        }
        String cd = code.getText().trim();
        int cnt;
        Timestamp ts;
        List<GiftEntry> list;
        try {
            cnt = Integer.parseInt(count.getText().trim().replace(".", ""));
            ts = Timestamp.valueOf(exp.getText().trim());
            list = EventOps.parseGifts(gifts.getText());
        } catch (IllegalArgumentException e) {
            CPanel.error(this, "Số lượt hoặc ngày hết hạn không hợp lệ (định dạng yyyy-MM-dd HH:mm:ss).");
            return;
        } catch (Exception e) {
            CPanel.error(this, CPanel.messageOf(e));
            return;
        }
        String json = EventOps.toDetailJson(list);
        if (!CPanel.confirm(this, "Tạo giftcode \"" + cd + "\", " + (cnt == -1 ? "không giới hạn" : cnt + " lượt") + ", hết hạn " + ts
                + "\nQuà: " + EventOps.summarize(json))) {
            return;
        }
        CPanel.async(this, "tạo giftcode", () -> EventOps.createGiftcode(cd, cnt, ts, list), msg -> {
            CPanel.info(this, msg + "\n\nNgười chơi nhập được ngay (đã nạp vào bộ nhớ).");
            refreshGiftcodes();
        });
    }

    private void deleteGiftcode() {
        int v = gcTable.getSelectedRow();
        if (v < 0) {
            CPanel.error(this, "Chọn một giftcode trước.");
            return;
        }
        int m = gcTable.convertRowIndexToModel(v);
        if (m < 0 || m >= gcRows.size()) {
            return;
        }
        GiftRow g = gcRows.get(m);
        if (!CPanel.confirm(this, "XÓA giftcode \"" + g.code + "\" (id " + g.id + ")?\n"
                + "Xóa khỏi DB và khỏi bộ nhớ server: người chơi không nhập được nữa. Không hoàn tác.")) {
            return;
        }
        CPanel.async(this, "xóa giftcode", () -> EventOps.deleteGiftcode(g.id, g.code), msg -> {
            CPanel.info(this, msg);
            refreshGiftcodes();
        });
    }

    // =====================================================================
    // KHÁC
    // =====================================================================
    private void announce() {
        String text = CPanel.askString(this, "Nội dung thông báo sự kiện gửi TOÀN SERVER:", "");
        if (text == null || text.isEmpty()) {
            return;
        }
        if (!CPanel.confirm(this, "Gửi thông báo tới tất cả người đang online?\n\n" + text)) {
            return;
        }
        CPanel.async(this, "thông báo toàn server", () -> {
            Service.gI().sendThongBaoAllPlayer(text);
            CPanel.log("Thông báo sự kiện toàn server: " + text);
            return null;
        }, x -> CPanel.info(this, "Đã gửi thông báo."));
    }

    private void eventPoint() {
        String name = CPanel.askString(this, "Tên nhân vật:", "");
        if (name == null || name.isEmpty()) {
            return;
        }
        BuffTab.pickCharacter(this, name, r -> {
            Long v = CPanel.askLong(this, "Số điểm sự kiện cộng cho \"" + r.name + "\" (" + (r.online ? "ONLINE" : "offline")
                    + ").\nNhập số âm để trừ (không xuống dưới 0).", "0");
            if (v == null || v == 0) {
                return;
            }
            if (v > Integer.MAX_VALUE || v < -Integer.MAX_VALUE) {
                CPanel.error(this, "Giá trị quá lớn.");
                return;
            }
            int d = v.intValue();
            if (!CPanel.confirm(this, (d > 0 ? "CỘNG " : "TRỪ ") + CPanel.num(Math.abs(d)) + " điểm sự kiện của \"" + r.name + "\"?")) {
                return;
            }
            BuffTab.runOp(this, "cộng/trừ điểm sự kiện", force -> CharacterOps.addEventPoint(r.id, r.name, d, force),
                    msg -> CPanel.info(this, msg));
        });
    }

    private void expRate() {
        Long v = CPanel.askLong(this, "Tỉ lệ EXP (tiềm năng) server, 1–100.\nHiện tại: x" + Manager.RATE_EXP_SERVER
                + "\nÁp dụng ngay cho mọi lần nhận tiềm năng sau đó và lưu server.expserver vào Config.properties.",
                String.valueOf(Manager.RATE_EXP_SERVER));
        if (v == null) {
            return;
        }
        if (v < 1 || v > 100) {
            CPanel.error(this, "Tỉ lệ phải từ 1 đến 100.");
            return;
        }
        int rate = v.intValue();
        if (!CPanel.confirm(this, "Đổi tỉ lệ EXP server từ x" + Manager.RATE_EXP_SERVER + " thành x" + rate + "?")) {
            return;
        }
        CPanel.async(this, "đổi tỉ lệ EXP", () -> EventOps.setExpRate(rate), msg -> {
            refreshExpLabel();
            CPanel.info(this, msg);
        });
    }
}
