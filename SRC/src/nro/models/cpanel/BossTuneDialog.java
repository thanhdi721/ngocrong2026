package nro.models.cpanel;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.FlowLayout;
import java.awt.GridLayout;
import java.awt.Window;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.SwingUtilities;
import nro.models.boss.Boss;
import nro.models.boss.BossData;
import nro.models.boss.BossDamageReduce;
import nro.models.boss.BossTuning;

/**
 * Hộp thoại chỉnh số của một boss: máu, sát thương, % chặn sát thương và thời gian
 * nghỉ — theo từng hình dạng (boss nhiều dạng thì mỗi dạng một dòng).
 *
 * <p>Bấm "Áp dụng" là có hiệu lực ngay; bấm "Áp dụng và lưu" thì ghi thêm vào
 * {@code data/bosstuning.properties} để giữ sau khi khởi động lại.
 */
final class BossTuneDialog extends JDialog {

    private final Boss boss;
    private final JTextField[] hp;
    private final JTextField[] dame;
    private final JTextField[] reduce;
    private final JTextField[] rest;

    BossTuneDialog(Component parent, Boss boss) {
        super(windowOf(parent), "Chỉnh số boss: " + boss.name + " (id " + boss.id + ")",
                ModalityType.APPLICATION_MODAL);
        this.boss = boss;
        int forms = boss.data == null ? 0 : boss.data.length;
        hp = new JTextField[forms];
        dame = new JTextField[forms];
        reduce = new JTextField[forms];
        rest = new JTextField[forms];

        JPanel grid = new JPanel(new GridLayout(0, 5, 8, 6));
        grid.setBorder(BorderFactory.createTitledBorder("Số theo từng hình dạng"));
        grid.add(new JLabel("Hình dạng"));
        grid.add(new JLabel("Máu"));
        grid.add(new JLabel("Sát thương"));
        grid.add(new JLabel("% chặn sát thương (0–" + BossDamageReduce.MAX_PERCENT + ")"));
        grid.add(new JLabel("Nghỉ (giây)"));

        int[] table = boss.getDamageReducePercentByLevel();
        for (int i = 0; i < forms; i++) {
            BossData d = boss.data[i];
            grid.add(new JLabel(i + (i == boss.currentLevel ? " (đang dùng)" : "")
                    + (d.getName() == null ? "" : " - " + d.getName())));
            hp[i] = new JTextField(String.valueOf(d.getHp() == null || d.getHp().length == 0 ? 0 : d.getHp()[0]), 10);
            dame[i] = new JTextField(String.valueOf(d.getDame()), 8);
            int pct = table != null && i < table.length ? table[i] : 0;
            reduce[i] = new JTextField(String.valueOf(pct), 5);
            rest[i] = new JTextField(String.valueOf(d.getSecondsRest()), 6);
            grid.add(hp[i]);
            grid.add(dame[i]);
            grid.add(reduce[i]);
            grid.add(rest[i]);
        }

        JTextArea note = new JTextArea(5, 70);
        note.setEditable(false);
        note.setLineWrap(true);
        note.setWrapStyleWord(true);
        note.setText("• Sát thương chiêu của boss tính từ ô \"Sát thương\", nên hạ ô này là hạ luôn sát thương mọi chiêu.\n"
                + "• \"% chặn sát thương\" là phần sát thương boss chặn bớt khi bị đánh — tăng lên thì boss trâu hơn mà không cần tăng máu.\n"
                + "• Boss đang đứng ở map được cập nhật ngay, máu hiện tại giữ đúng tỉ lệ cũ (không hồi đầy).\n"
                + "• Số này dùng chung cho mọi bản sao của cùng một boss (các khu khác nhau).\n"
                + "• Máu gốc của vài boss là một dải ngẫu nhiên; đặt số ở đây là cố định đúng một mức máu.\n"
                + "• \"Áp dụng\" chỉ có hiệu lực tới lần khởi động lại; muốn giữ lâu dài thì bấm \"Áp dụng và lưu\".");

        JPanel actions = new JPanel(new WrapLayout(FlowLayout.RIGHT, 8, 8));
        JButton btnApply = new JButton("Áp dụng");
        JButton btnSave = new JButton("Áp dụng và lưu");
        JButton btnReset = new JButton("Bỏ chỉnh số của boss này");
        JButton btnClose = new JButton("Đóng");
        actions.add(btnReset);
        actions.add(btnApply);
        actions.add(btnSave);
        actions.add(btnClose);

        JPanel content = new JPanel(new BorderLayout(6, 6));
        content.setBorder(BorderFactory.createEmptyBorder(8, 8, 8, 8));
        content.add(grid, BorderLayout.NORTH);
        content.add(new JScrollPane(note), BorderLayout.CENTER);
        content.add(actions, BorderLayout.SOUTH);
        setContentPane(content);
        pack();
        setLocationRelativeTo(parent);

        btnApply.addActionListener(e -> apply(false));
        btnSave.addActionListener(e -> apply(true));
        btnReset.addActionListener(e -> reset());
        btnClose.addActionListener(e -> dispose());
    }

    private static Window windowOf(Component c) {
        return c == null ? null : SwingUtilities.getWindowAncestor(c);
    }

    private Integer read(JTextField f, String what) {
        try {
            return Integer.parseInt(f.getText().trim().replace(".", "").replace(",", "").replace(" ", ""));
        } catch (NumberFormatException e) {
            CPanel.error(this, "Giá trị không hợp lệ ở ô " + what + ": " + f.getText());
            return null;
        }
    }

    private void apply(boolean saveToFile) {
        int bossId = (int) boss.id;
        int forms = hp.length;
        if (forms == 0) {
            CPanel.error(this, "Boss này không có dữ liệu hình dạng nào để chỉnh.");
            return;
        }
        int[][] vals = new int[forms][4];
        for (int i = 0; i < forms; i++) {
            Integer h = read(hp[i], "Máu (hình dạng " + i + ")");
            Integer d = read(dame[i], "Sát thương (hình dạng " + i + ")");
            Integer p = read(reduce[i], "% chặn sát thương (hình dạng " + i + ")");
            Integer r = read(rest[i], "Nghỉ (hình dạng " + i + ")");
            if (h == null || d == null || p == null || r == null) {
                return;
            }
            if (h < 1) {
                CPanel.error(this, "Máu của hình dạng " + i + " phải từ 1 trở lên.");
                return;
            }
            if (d < 0 || r < 0) {
                CPanel.error(this, "Sát thương và thời gian nghỉ không được âm (hình dạng " + i + ").");
                return;
            }
            if (p < 0 || p > BossDamageReduce.MAX_PERCENT) {
                CPanel.error(this, "% chặn sát thương phải trong khoảng 0–" + BossDamageReduce.MAX_PERCENT
                        + " (hình dạng " + i + ").");
                return;
            }
            vals[i] = new int[]{h, d, p, r};
        }
        for (int i = 0; i < forms; i++) {
            BossTuning.set(bossId, i, BossTuning.HP, vals[i][0]);
            BossTuning.set(bossId, i, BossTuning.DAME, vals[i][1]);
            BossTuning.set(bossId, i, BossTuning.REDUCE, vals[i][2]);
            BossTuning.set(bossId, i, BossTuning.REST, vals[i][3]);
        }
        CPanel.log("Đã chỉnh số boss " + boss.name + " (id " + bossId + "): "
                + forms + " hình dạng, máu dạng 0 = " + CPanel.num(vals[0][0])
                + ", sát thương = " + CPanel.num(vals[0][1])
                + ", chặn " + vals[0][2] + "%, nghỉ " + vals[0][3] + "s.");
        if (!saveToFile) {
            CPanel.info(this, "Đã áp dụng. Khởi động lại server sẽ quay về số cũ nếu không lưu.");
            return;
        }
        CPanel.async(this, "lưu chỉnh số boss", () -> {
            BossTuning.save();
            return null;
        }, x -> {
            CPanel.log("Đã lưu data/bosstuning.properties.");
            CPanel.info(this, "Đã áp dụng và lưu vào data/bosstuning.properties.");
        });
    }

    private void reset() {
        if (!CPanel.confirm(this, "Bỏ toàn bộ chỉnh số đã lưu của boss này?\n"
                + "Boss quay về số gốc trong mã nguồn sau khi khởi động lại server.")) {
            return;
        }
        int bossId = (int) boss.id;
        for (int i = 0; i < hp.length; i++) {
            BossTuning.set(bossId, i, BossTuning.HP, null);
            BossTuning.set(bossId, i, BossTuning.DAME, null);
            BossTuning.set(bossId, i, BossTuning.REDUCE, null);
            BossTuning.set(bossId, i, BossTuning.REST, null);
        }
        CPanel.async(this, "lưu chỉnh số boss", () -> {
            BossTuning.save();
            return null;
        }, x -> {
            CPanel.log("Đã bỏ chỉnh số của boss " + boss.name + " (id " + bossId + ").");
            CPanel.info(this, "Đã bỏ chỉnh số. Số gốc có hiệu lực sau khi khởi động lại server.");
            dispose();
        });
    }
}
