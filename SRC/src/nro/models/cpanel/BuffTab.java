package nro.models.cpanel;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.GridLayout;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;
import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.ButtonGroup;
import javax.swing.DefaultListModel;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTable;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.ListSelectionModel;
import javax.swing.table.DefaultTableModel;
import nro.models.cpanel.BuffOps.Preset;
import nro.models.cpanel.CPanel.NeedConfirmException;
import nro.models.cpanel.CPanel.Task;
import nro.models.cpanel.CharacterOps.CharRow;
import nro.models.item.Item;
import nro.models.player_system.Template;
import nro.models.services.ItemService;

/**
 * Tab "Buff đồ": tìm vật phẩm, dựng danh sách option, xem trước, phát cho 1 nhân vật
 * hoặc tất cả người online; mẫu buff lưu nhanh trong file cục bộ.
 */
final class BuffTab extends JPanel {

    private static final String[] ITEM_COLS = {"ID", "Tên", "Loại (TYPE)", "Hành tinh", "Icon", "Cộng dồn"};
    private static final String[] OPT_COLS = {"Option ID", "Tên option", "Giá trị"};

    private final JTextField searchField = new JTextField(16);
    private final DefaultTableModel itemModel = readOnly(ITEM_COLS, 0);
    private final JTable itemTable = new JTable(itemModel);
    private final List<Template.ItemTemplate> itemRows = new ArrayList<>();

    private final JLabel selLabel = new JLabel("Chưa chọn vật phẩm");
    private final JTextField qtyField = new JTextField("1", 8);
    private final DefaultTableModel optModel = readOnly(OPT_COLS, 2);
    private final JTable optTable = new JTable(optModel);
    private final List<int[]> opts = new ArrayList<>();
    private final JTextArea preview = new JTextArea(7, 30);

    private final JRadioButton rbOne = new JRadioButton("Một nhân vật:", true);
    private final JRadioButton rbAll = new JRadioButton("TẤT CẢ người đang online");
    private final JTextField targetField = new JTextField(14);

    private final JComboBox<Preset> presetBox = new JComboBox<>();
    private final List<Preset> presets = new ArrayList<>();

    private Template.ItemTemplate selected;

    private static DefaultTableModel readOnly(String[] cols, int intCols) {
        return new DefaultTableModel(cols, 0) {
            @Override
            public boolean isCellEditable(int r, int c) {
                return false;
            }

            @Override
            public Class<?> getColumnClass(int c) {
                if (cols.length == 6) {
                    return c == 0 || c == 4 ? Integer.class : String.class;
                }
                return c == 1 ? String.class : Integer.class;
            }
        };
    }

    BuffTab() {
        super(new BorderLayout(6, 6));
        setBorder(BorderFactory.createEmptyBorder(8, 8, 8, 8));

        // ---------- trái: tìm vật phẩm ----------
        JPanel left = new JPanel(new BorderLayout(4, 4));
        JPanel top = new JPanel(new FlowLayout(FlowLayout.LEFT));
        top.add(new JLabel("Tên hoặc ID vật phẩm:"));
        top.add(searchField);
        JButton btnSearch = new JButton("Tìm");
        top.add(btnSearch);
        left.add(top, BorderLayout.NORTH);
        itemTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        itemTable.setAutoCreateRowSorter(true);
        itemTable.setRowHeight(22);
        left.add(new JScrollPane(itemTable), BorderLayout.CENTER);
        left.add(new JLabel("Tìm trong item_template đã nạp (bỏ dấu, tối đa 300 dòng)."), BorderLayout.SOUTH);

        // ---------- phải: dựng vật phẩm ----------
        JPanel right = new JPanel(new BorderLayout(4, 4));

        JPanel head = new JPanel(new GridLayout(0, 1, 2, 2));
        head.add(selLabel);
        JPanel qty = new JPanel(new FlowLayout(FlowLayout.LEFT, 4, 0));
        qty.add(new JLabel("Số lượng:"));
        qty.add(qtyField);
        head.add(qty);
        right.add(head, BorderLayout.NORTH);

        optTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        optTable.setRowHeight(22);
        JScrollPane optScroll = new JScrollPane(optTable);
        optScroll.setPreferredSize(new Dimension(380, 160));
        optScroll.setBorder(BorderFactory.createTitledBorder("Option sẽ gắn (đúng thứ tự)"));

        JPanel optBtns = new JPanel(new GridLayout(0, 3, 4, 4));
        optBtns.add(btn("Thêm option...", this::addOptionDialog));
        optBtns.add(btn("Sửa giá trị", this::editOption));
        optBtns.add(btn("Xóa option", this::removeOption));
        optBtns.add(btn("Xóa hết", () -> {
            opts.clear();
            refreshOpts();
        }));
        optBtns.add(btn("Nạp option shop", this::loadShopOptions));
        optBtns.add(btn("Chỉ số Thiên Sứ", this::thienSu));
        optBtns.setBorder(BorderFactory.createTitledBorder("Sửa option"));

        JPanel quick = new JPanel(new GridLayout(0, 3, 4, 4));
        quick.setBorder(BorderFactory.createTitledBorder("Nút nhanh (id theo docs 06 §5)"));
        quick.add(btn("Số lỗ sao pha lê (107)", () -> quickAsk(107, "Số lỗ sao pha lê (1-7)", "7", 0, 1000)));
        quick.add(btn("Sao đã ép (102)", () -> quickAsk(102, "Số sao pha lê đã ép", "7", 0, 1000)));
        quick.add(btn("Cấp +N (72)", () -> quickAsk(72, "Cấp nâng cấp (+1…+8)", "8", 1, 8)));
        quick.add(btn("Hạn sử dụng ngày (93)", () -> quickAsk(93, "Hạn sử dụng (số ngày, ≥1)", "7", 1, 100000)));
        quick.add(btn("Không thể giao dịch (30)", () -> putOption(30, 0)));
        quick.add(btn("Yêu cầu SM tỉ (21)", () -> quickAsk(21, "Yêu cầu sức mạnh (tỉ)", "15", 0, 1000)));
        quick.add(btn("Sức đánh % (50)", () -> quickAsk(50, "Sức đánh +%", "10", 0, 1_000_000)));
        quick.add(btn("HP % (77)", () -> quickAsk(77, "HP +%", "10", 0, 1_000_000)));
        quick.add(btn("KI % (103)", () -> quickAsk(103, "KI +%", "10", 0, 1_000_000)));
        quick.add(btn("Chí mạng % (14)", () -> quickAsk(14, "Chí mạng +%", "5", 0, 1_000_000)));
        quick.add(btn("Giảm sát thương % (94)", () -> quickAsk(94, "Giảm % sát thương", "5", 0, 100)));
        quick.add(btn("Set kích hoạt...", this::skh));

        JPanel mid = new JPanel();
        mid.setLayout(new BoxLayout(mid, BoxLayout.Y_AXIS));
        mid.add(optScroll);
        mid.add(optBtns);
        mid.add(quick);
        preview.setEditable(false);
        preview.setLineWrap(true);
        preview.setWrapStyleWord(true);
        JScrollPane pv = new JScrollPane(preview);
        pv.setBorder(BorderFactory.createTitledBorder("Xem trước"));
        mid.add(pv);
        right.add(mid, BorderLayout.CENTER);

        JPanel bottom = new JPanel(new GridLayout(0, 1, 2, 2));
        JPanel pre = new JPanel(new FlowLayout(FlowLayout.LEFT, 4, 0));
        pre.add(new JLabel("Mẫu:"));
        presetBox.setPrototypeDisplayValue(new Preset("Set kích hoạt - Áo vải 3 lỗ Sôngôku (TĐ)", 0, 1));
        pre.add(presetBox);
        pre.add(btn("Nạp mẫu", this::applyPreset));
        pre.add(btn("Lưu thành mẫu...", this::savePreset));
        pre.add(btn("Xóa mẫu", this::deletePreset));
        bottom.add(pre);
        ButtonGroup g = new ButtonGroup();
        g.add(rbOne);
        g.add(rbAll);
        JPanel tgt = new JPanel(new FlowLayout(FlowLayout.LEFT, 4, 0));
        tgt.add(rbOne);
        tgt.add(targetField);
        tgt.add(new JLabel("(tên nhân vật)"));
        tgt.add(rbAll);
        bottom.add(tgt);
        JPanel go = new JPanel(new FlowLayout(FlowLayout.LEFT, 4, 0));
        go.add(btn("PHÁT VẬT PHẨM", this::give));
        bottom.add(go);
        bottom.setBorder(BorderFactory.createTitledBorder("Mẫu buff / người nhận"));
        right.add(bottom, BorderLayout.SOUTH);

        JScrollPane rightScroll = new JScrollPane(right);
        rightScroll.setBorder(null);
        rightScroll.getVerticalScrollBar().setUnitIncrement(16);
        JSplitPane split = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, left, rightScroll);
        split.setResizeWeight(0.45);
        add(split, BorderLayout.CENTER);

        btnSearch.addActionListener(e -> search());
        searchField.addActionListener(e -> search());
        qtyField.addCaretListener(e -> refreshPreview());
        itemTable.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) {
                onSelectItem();
            }
        });
        refreshOpts();
        reloadPresets();
    }

    private static JButton btn(String text, Runnable r) {
        JButton b = new JButton(text);
        b.addActionListener(e -> r.run());
        return b;
    }

    // ---------------------------------------------------------------- vật phẩm
    private void search() {
        List<Template.ItemTemplate> list = BuffOps.searchItems(searchField.getText(), 300);
        itemRows.clear();
        itemRows.addAll(list);
        itemModel.setRowCount(0);
        for (Template.ItemTemplate t : list) {
            itemModel.addRow(new Object[]{(int) t.id, t.name, t.type + " " + BuffOps.typeName(t.type),
                BuffOps.genderName(t.gender), (int) t.iconID, t.isUpToUp ? "có" : "không"});
        }
        if (list.size() == 1) {
            itemTable.setRowSelectionInterval(0, 0);
        }
    }

    private void onSelectItem() {
        int v = itemTable.getSelectedRow();
        if (v < 0) {
            return;
        }
        int m = itemTable.convertRowIndexToModel(v);
        if (m >= 0 && m < itemRows.size()) {
            selectItem(itemRows.get(m));
        }
    }

    private void selectItem(Template.ItemTemplate t) {
        selected = t;
        selLabel.setText("<html><b>[" + t.id + "] " + esc(t.name) + "</b> — TYPE " + t.type + " " + BuffOps.typeName(t.type)
                + ", " + BuffOps.genderName(t.gender) + ", icon " + t.iconID + (t.isUpToUp ? ", cộng dồn" : ", không cộng dồn")
                + (BuffOps.isQuestItem(t.id) ? " <font color='red'>(VẬT PHẨM NHIỆM VỤ)</font>" : "") + "</html>");
        refreshPreview();
    }

    private static String esc(String s) {
        return s == null ? "" : s.replace("&", "&amp;").replace("<", "&lt;").replace(">", "&gt;");
    }

    // ---------------------------------------------------------------- option
    private void refreshOpts() {
        optModel.setRowCount(0);
        for (int[] o : opts) {
            Template.ItemOptionTemplate t = BuffOps.optionOrNull(o[0]);
            optModel.addRow(new Object[]{o[0], t == null ? "?" : (t.name == null || t.name.isEmpty() ? "(dòng trống)" : t.name), o[1]});
        }
        refreshPreview();
    }

    private void refreshPreview() {
        StringBuilder sb = new StringBuilder();
        if (selected == null) {
            sb.append("Chọn vật phẩm ở bảng bên trái.\n");
        } else {
            sb.append(qtyField.getText().trim()).append(" x ").append(selected.name).append(" [").append(selected.id).append("]\n");
            if (BuffOps.isQuestItem(selected.id)) {
                sb.append("⚠ VẬT PHẨM NHIỆM VỤ (2000–2031): phát tay có thể làm lệch tiến độ nhiệm vụ.\n");
            }
        }
        if (opts.isEmpty()) {
            sb.append("(không option - game tự thêm option 73 dòng trống)\n");
        }
        for (int[] o : opts) {
            sb.append(" • ").append(BuffOps.optionText(o[0], o[1])).append("   [").append(o[0]).append(':').append(o[1]).append("]\n");
        }
        preview.setText(sb.toString());
        preview.setCaretPosition(0);
    }

    /** Thêm option; nếu đã có option cùng id (trừ dòng mô tả set) thì thay giá trị. */
    private void putOption(int id, int param) {
        if (BuffOps.optionOrNull(id) == null) {
            CPanel.error(this, "Option id " + id + " không có trong item_option_template.");
            return;
        }
        for (int[] o : opts) {
            if (o[0] == id) {
                o[1] = param;
                refreshOpts();
                return;
            }
        }
        opts.add(new int[]{id, param});
        refreshOpts();
    }

    private void quickAsk(int id, String msg, String init, long min, long max) {
        Long v = CPanel.askLong(this, msg + " - option " + id + " \"" + optName(id) + "\"", init);
        if (v == null) {
            return;
        }
        if (v < min || v > max) {
            CPanel.error(this, "Giá trị phải trong khoảng " + min + " … " + max + ".");
            return;
        }
        putOption(id, v.intValue());
    }

    private static String optName(int id) {
        Template.ItemOptionTemplate t = BuffOps.optionOrNull(id);
        return t == null ? "?" : t.name;
    }

    private void addOptionDialog() {
        JTextField kw = new JTextField(18);
        DefaultListModel<Template.ItemOptionTemplate> lm = new DefaultListModel<>();
        JList<Template.ItemOptionTemplate> list = new JList<>(lm);
        list.setCellRenderer((l, value, index, sel, focus) -> {
            JLabel lb = new JLabel(value.id + " - " + (value.name == null || value.name.isEmpty() ? "(dòng trống)" : value.name));
            lb.setOpaque(true);
            lb.setBackground(sel ? l.getSelectionBackground() : l.getBackground());
            lb.setForeground(sel ? l.getSelectionForeground() : l.getForeground());
            return lb;
        });
        Runnable filter = () -> {
            lm.clear();
            for (Template.ItemOptionTemplate o : BuffOps.searchOptions(kw.getText())) {
                lm.addElement(o);
            }
        };
        filter.run();
        kw.addCaretListener(e -> filter.run());
        JTextField val = new JTextField("0", 10);
        JPanel p = new JPanel(new BorderLayout(4, 4));
        JPanel n = new JPanel(new FlowLayout(FlowLayout.LEFT));
        n.add(new JLabel("Tìm theo tên / id:"));
        n.add(kw);
        p.add(n, BorderLayout.NORTH);
        JScrollPane sp = new JScrollPane(list);
        sp.setPreferredSize(new Dimension(420, 300));
        p.add(sp, BorderLayout.CENTER);
        JPanel s = new JPanel(new FlowLayout(FlowLayout.LEFT));
        s.add(new JLabel("Giá trị (#):"));
        s.add(val);
        p.add(s, BorderLayout.SOUTH);
        int c = JOptionPane.showConfirmDialog(this, p, "Thêm option", JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);
        if (c != JOptionPane.OK_OPTION) {
            return;
        }
        Template.ItemOptionTemplate o = list.getSelectedValue();
        if (o == null) {
            CPanel.error(this, "Chưa chọn option.");
            return;
        }
        int param;
        try {
            param = Integer.parseInt(val.getText().trim().replace(".", ""));
        } catch (NumberFormatException e) {
            CPanel.error(this, "Giá trị không hợp lệ.");
            return;
        }
        opts.add(new int[]{o.id, param});
        refreshOpts();
    }

    private int selectedOpt() {
        int r = optTable.getSelectedRow();
        if (r < 0 || r >= opts.size()) {
            CPanel.error(this, "Chọn một dòng option trước.");
            return -1;
        }
        return r;
    }

    private void editOption() {
        int r = selectedOpt();
        if (r < 0) {
            return;
        }
        int[] o = opts.get(r);
        Long v = CPanel.askLong(this, "Giá trị mới cho option " + o[0] + " (" + BuffOps.optionText(o[0], o[1]) + ")", String.valueOf(o[1]));
        if (v == null) {
            return;
        }
        if (v < Integer.MIN_VALUE || v > Integer.MAX_VALUE) {
            CPanel.error(this, "Giá trị vượt giới hạn int.");
            return;
        }
        o[1] = v.intValue();
        refreshOpts();
    }

    private void removeOption() {
        int r = selectedOpt();
        if (r >= 0) {
            opts.remove(r);
            refreshOpts();
        }
    }

    private boolean needItem() {
        if (selected == null) {
            CPanel.error(this, "Chọn vật phẩm trước.");
            return false;
        }
        return true;
    }

    /** Option mặc định trong shop (ItemService.getListOptionItemShop - giống lệnh admin i / GIVE_IT). */
    private void loadShopOptions() {
        if (!needItem()) {
            return;
        }
        List<Item.ItemOption> list = ItemService.gI().getListOptionItemShop(selected.id);
        if (list.isEmpty()) {
            CPanel.info(this, "Vật phẩm này không bán trong shop nào -> không có option mặc định.");
            return;
        }
        for (Item.ItemOption io : list) {
            if (io.optionTemplate != null) {
                opts.add(new int[]{io.optionTemplate.id, io.param});
            }
        }
        refreshOpts();
    }

    /** Sinh chỉ số Thiên Sứ bằng ItemService.DoThienSu (chỉ id 1048–1062). */
    private void thienSu() {
        if (!needItem()) {
            return;
        }
        if (selected.id < 1048 || selected.id > 1062) {
            CPanel.error(this, "Chỉ dùng cho Đồ Thiên Sứ (id 1048–1062).");
            return;
        }
        Item it = ItemService.gI().DoThienSu(selected.id, selected.gender);
        opts.clear();
        for (Item.ItemOption io : it.itemOptions) {
            if (io.optionTemplate != null) {
                opts.add(new int[]{io.optionTemplate.id, io.param});
            }
        }
        refreshOpts();
    }

    /** Set kích hoạt: option set (param 1) + dòng mô tả (ItemService.getOptionIdsBySKH) + 30, như createItemSKH. */
    private void skh() {
        int[][] sets = {{127}, {128}, {129}, {245}, {130}, {131}, {132}, {237}, {133}, {134}, {135}, {241}, {233}};
        String[] planet = {"TĐ", "TĐ", "TĐ", "TĐ", "NM", "NM", "NM", "NM", "XD", "XD", "XD", "XD", "Chung"};
        String[] names = new String[sets.length];
        for (int i = 0; i < sets.length; i++) {
            names[i] = planet[i] + " - " + sets[i][0] + " " + BuffOps.optionText(sets[i][0], 1);
        }
        Object c = JOptionPane.showInputDialog(this, "Chọn set kích hoạt (docs 06 §9.5).\n"
                + "Sẽ thêm: option set (1) + dòng mô tả + 30 Không thể giao dịch.",
                "Set kích hoạt", JOptionPane.QUESTION_MESSAGE, null, names, names[0]);
        if (c == null) {
            return;
        }
        int idx = java.util.Arrays.asList(names).indexOf(c.toString());
        int skhId = sets[idx][0];
        // bỏ option set cũ (127–135, 233, 237, 241, 245 và dòng mô tả) để không bị 2 set
        opts.removeIf(o -> (o[0] >= 127 && o[0] <= 144) || (o[0] >= 233 && o[0] <= 248));
        opts.add(new int[]{skhId, 1});
        for (int sub : ItemService.gI().getOptionIdsBySKH(skhId)) {
            opts.add(new int[]{sub, 1});
        }
        putOption(30, 1);
    }

    // ---------------------------------------------------------------- mẫu
    private void reloadPresets() {
        CPanel.async(this, "đọc mẫu buff", BuffOps::loadPresets, list -> {
            presets.clear();
            presets.addAll(list);
            refreshPresetBox();
        });
    }

    private void refreshPresetBox() {
        presetBox.removeAllItems();
        for (Preset p : presets) {
            presetBox.addItem(p);
        }
    }

    private void applyPreset() {
        Preset p = (Preset) presetBox.getSelectedItem();
        if (p == null) {
            return;
        }
        Template.ItemTemplate t = BuffOps.itemOrNull(p.itemId);
        if (t == null) {
            CPanel.error(this, "Mẫu \"" + p.name + "\" dùng vật phẩm id " + p.itemId + " không tồn tại.");
            return;
        }
        selectItem(t);
        qtyField.setText(String.valueOf(p.quantity));
        opts.clear();
        for (int[] o : p.options) {
            opts.add(new int[]{o[0], o[1]});
        }
        refreshOpts();
    }

    private void savePreset() {
        if (!needItem()) {
            return;
        }
        Integer q = parseQty();
        if (q == null) {
            return;
        }
        String name = CPanel.askString(this, "Tên mẫu:", selected.name);
        if (name == null || name.isEmpty()) {
            return;
        }
        Preset p = new Preset(name, selected.id, q);
        for (int[] o : opts) {
            p.options.add(new int[]{o[0], o[1]});
        }
        presets.removeIf(x -> x.name.equals(name));
        presets.add(p);
        persistPresets("Lưu mẫu buff \"" + name + "\"");
    }

    private void deletePreset() {
        Preset p = (Preset) presetBox.getSelectedItem();
        if (p == null || !CPanel.confirm(this, "Xóa mẫu \"" + p.name + "\"?")) {
            return;
        }
        presets.remove(p);
        persistPresets("Xóa mẫu buff \"" + p.name + "\"");
    }

    private void persistPresets(String what) {
        List<Preset> copy = new ArrayList<>(presets);
        CPanel.async(this, "lưu mẫu buff", () -> {
            BuffOps.savePresets(copy);
            return null;
        }, x -> {
            CPanel.log(what + " (file " + BuffOps.PRESET_FILE + ")");
            refreshPresetBox();
        });
    }

    // ---------------------------------------------------------------- phát
    private Integer parseQty() {
        try {
            int q = Integer.parseInt(qtyField.getText().trim().replace(".", "").replace(",", ""));
            if (q <= 0) {
                throw new NumberFormatException();
            }
            return q;
        } catch (NumberFormatException e) {
            CPanel.error(this, "Số lượng không hợp lệ.");
            return null;
        }
    }

    private void give() {
        if (!needItem()) {
            return;
        }
        Integer q = parseQty();
        if (q == null) {
            return;
        }
        Template.ItemTemplate t = selected;
        List<int[]> copy = new ArrayList<>();
        for (int[] o : opts) {
            copy.add(new int[]{o[0], o[1]});
        }
        try {
            BuffOps.validate(t.id, q, copy);
        } catch (Exception e) {
            CPanel.error(this, CPanel.messageOf(e));
            return;
        }
        String what = BuffOps.describe(t, q, copy);
        if (BuffOps.isQuestItem(t.id) && !CPanel.confirm(this, "CẢNH BÁO: \"" + t.name + "\" [" + t.id + "] là VẬT PHẨM NHIỆM VỤ (2000–2031).\n"
                + "Phát tay có thể làm người chơi bỏ qua / kẹt bước nhiệm vụ.\nVẫn phát?")) {
            return;
        }
        int qty = q;
        if (rbAll.isSelected()) {
            if (!CPanel.confirm(this, "PHÁT CHO TẤT CẢ người đang online:\n" + what + "\n\nMỗi người nhận 1 phần. Không hoàn tác được. Tiếp tục?")) {
                return;
            }
            CPanel.async(this, "phát vật phẩm cho tất cả", () -> BuffOps.giveToAllOnline(t.id, qty, copy), msg -> CPanel.info(this, msg));
            return;
        }
        String name = targetField.getText().trim();
        if (name.isEmpty()) {
            CPanel.error(this, "Nhập tên nhân vật nhận.");
            return;
        }
        pickCharacter(this, name, r -> {
            if (!CPanel.confirm(this, "Phát cho \"" + r.name + "\" (" + (r.online ? "ONLINE" : "offline") + "):\n" + what)) {
                return;
            }
            runOp(this, "phát vật phẩm", force -> BuffOps.giveToCharacter(r.id, r.name, t.id, qty, copy, force), msg -> CPanel.info(this, msg));
        });
    }

    // ---------------------------------------------------------------- tiện ích dùng chung
    /** Tìm nhân vật theo tên (trùng khớp tuyệt đối được ưu tiên; nhiều kết quả -> cho chọn). */
    static void pickCharacter(Component parent, String name, Consumer<CharRow> cb) {
        CPanel.async(parent, "tìm nhân vật", () -> CharacterOps.search(name), list -> {
            if (list.isEmpty()) {
                CPanel.error(parent, "Không tìm thấy nhân vật nào có tên chứa \"" + name + "\".");
                return;
            }
            for (CharRow r : list) {
                if (r.name != null && r.name.equalsIgnoreCase(name)) {
                    cb.accept(r);
                    return;
                }
            }
            if (list.size() == 1) {
                cb.accept(list.get(0));
                return;
            }
            String[] names = new String[list.size()];
            for (int i = 0; i < list.size(); i++) {
                CharRow r = list.get(i);
                names[i] = r.name + "  (id " + r.id + ", " + (r.online ? "ONLINE" : "offline") + ")";
            }
            Object c = JOptionPane.showInputDialog(parent, "Có " + list.size() + " nhân vật khớp, chọn một:",
                    "Chọn nhân vật", JOptionPane.QUESTION_MESSAGE, null, names, names[0]);
            if (c != null) {
                cb.accept(list.get(java.util.Arrays.asList(names).indexOf(c.toString())));
            }
        });
    }

    /** Chạy thao tác nhân vật; nếu cần xác nhận (phiên trước chưa đóng) thì hỏi rồi chạy lại với force. */
    static void runOp(Component parent, String what, ForceTaskPublic op, Consumer<String> done) {
        Task<String> first = () -> op.run(false);
        CPanel.async(parent, what, first, done, e -> {
            if (e instanceof NeedConfirmException) {
                if (CPanel.confirm(parent, e.getMessage())) {
                    CPanel.async(parent, what, () -> op.run(true), done);
                }
            } else {
                CPanel.error(parent, "Lỗi khi " + what + ":\n" + CPanel.messageOf(e));
            }
        });
    }

    /** Công việc có tham số force (true = admin đã xác nhận ghi DB dù nghi phiên chưa đóng). */
    interface ForceTaskPublic {

        String run(boolean force) throws Exception;
    }
}
