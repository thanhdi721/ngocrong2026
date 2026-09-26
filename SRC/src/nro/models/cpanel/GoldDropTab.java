package nro.models.cpanel;

import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.GridLayout;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import nro.models.mob.GoldDropConfig;

/**
 * Tab "Vàng rơi": xem và sửa tỉ lệ vàng quái rơi theo từng nhóm map, có hiệu lực
 * ngay (không cần khởi động lại), và lưu vào {@code data/golddrop.properties}.
 *
 * <p>Cột "ước tính / giờ" tính theo {@link #KILLS_PER_HOUR} con quái mỗi giờ để
 * so sánh nhanh giữa các khu.
 */
final class GoldDropTab extends JPanel {

    /** Số quái một người chơi hạ được trong 1 giờ, chỉ dùng để ước tính. */
    private static final int KILLS_PER_HOUR = 1500;

    private final JTextField[] rate = new JTextField[GoldDropConfig.ALL.length];
    private final JTextField[] per = new JTextField[GoldDropConfig.ALL.length];
    private final JTextField[] min = new JTextField[GoldDropConfig.ALL.length];
    private final JTextField[] max = new JTextField[GoldDropConfig.ALL.length];
    private final JLabel[] est = new JLabel[GoldDropConfig.ALL.length];
    private final JTextArea note = new JTextArea(6, 80);

    GoldDropTab() {
        super(new BorderLayout(6, 6));
        setBorder(BorderFactory.createEmptyBorder(8, 8, 8, 8));

        JPanel grid = new JPanel(new GridLayout(0, 6, 8, 6));
        grid.setBorder(BorderFactory.createTitledBorder(
                "Vàng rơi từ quái — tỉ lệ ghi dạng 'rơi / số lần' (ví dụ 1 / 20 = 5%)"));
        grid.add(new JLabel("Nhóm map"));
        grid.add(new JLabel("Rơi"));
        grid.add(new JLabel("Trên mỗi"));
        grid.add(new JLabel("Vàng tối thiểu"));
        grid.add(new JLabel("Vàng tối đa"));
        grid.add(new JLabel("Ước tính / giờ"));

        for (int i = 0; i < GoldDropConfig.ALL.length; i++) {
            GoldDropConfig.Group g = GoldDropConfig.ALL[i];
            grid.add(new JLabel(g.label));
            rate[i] = new JTextField(5);
            per[i] = new JTextField(5);
            min[i] = new JTextField(8);
            max[i] = new JTextField(8);
            est[i] = new JLabel("-");
            grid.add(rate[i]);
            grid.add(per[i]);
            grid.add(min[i]);
            grid.add(max[i]);
            grid.add(est[i]);
        }

        JPanel actions = new JPanel(new WrapLayout(FlowLayout.LEFT, 8, 8));
        JButton btnApply = new JButton("Áp dụng ngay");
        JButton btnSave = new JButton("Áp dụng và lưu vào file");
        JButton btnReload = new JButton("Đọc lại giá trị đang chạy");
        JButton btnHalf = new JButton("Giảm một nửa số vàng (tất cả nhóm)");
        actions.add(btnApply);
        actions.add(btnSave);
        actions.add(btnReload);
        actions.add(btnHalf);

        note.setEditable(false);
        note.setLineWrap(true);
        note.setWrapStyleWord(true);
        note.setText("• Sửa xong bấm \"Áp dụng ngay\" là có hiệu lực với quái chết sau đó, không cần khởi động lại.\n"
                + "• Muốn giữ sau khi khởi động lại thì bấm \"Áp dụng và lưu vào file\" (ghi data/golddrop.properties).\n"
                + "• Ước tính / giờ = tỉ lệ × vàng trung bình × " + KILLS_PER_HOUR + " quái mỗi giờ, chỉ để so sánh giữa các khu.\n"
                + "• Vàng boss rơi (20.000–30.000 mỗi con) và vàng thưởng nhiệm vụ không nằm trong bảng này.");
        JPanel south = new JPanel(new BorderLayout());
        south.add(new JScrollPane(note), BorderLayout.CENTER);

        JPanel north = new JPanel(new BorderLayout(6, 6));
        north.add(grid, BorderLayout.NORTH);
        north.add(actions, BorderLayout.CENTER);
        add(north, BorderLayout.NORTH);
        add(south, BorderLayout.CENTER);

        btnApply.addActionListener(e -> apply(false));
        btnSave.addActionListener(e -> apply(true));
        btnReload.addActionListener(e -> reload());
        btnHalf.addActionListener(e -> halve());

        reload();
    }

    private void reload() {
        for (int i = 0; i < GoldDropConfig.ALL.length; i++) {
            GoldDropConfig.Group g = GoldDropConfig.ALL[i];
            rate[i].setText(String.valueOf(g.rate));
            per[i].setText(String.valueOf(g.per));
            min[i].setText(String.valueOf(g.min));
            max[i].setText(String.valueOf(g.max));
        }
        updateEstimates();
    }

    private void updateEstimates() {
        for (int i = 0; i < GoldDropConfig.ALL.length; i++) {
            GoldDropConfig.Group g = GoldDropConfig.ALL[i];
            long perHour = (long) (g.avgPerKill() * KILLS_PER_HOUR);
            double pct = g.per <= 0 ? 0 : 100.0 * g.rate / g.per;
            est[i].setText(String.format("%.2f%% · %s vàng/giờ", pct, CPanel.num(perHour)));
        }
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
        int[][] vals = new int[GoldDropConfig.ALL.length][4];
        for (int i = 0; i < GoldDropConfig.ALL.length; i++) {
            String label = GoldDropConfig.ALL[i].label;
            Integer a = read(rate[i], "Rơi (" + label + ")");
            Integer b = read(per[i], "Trên mỗi (" + label + ")");
            Integer c = read(min[i], "Vàng tối thiểu (" + label + ")");
            Integer d = read(max[i], "Vàng tối đa (" + label + ")");
            if (a == null || b == null || c == null || d == null) {
                return;
            }
            if (b < 1) {
                CPanel.error(this, "Ô \"Trên mỗi\" của " + label + " phải từ 1 trở lên.");
                return;
            }
            if (a > b) {
                CPanel.error(this, label + ": số lần rơi không được lớn hơn ô \"Trên mỗi\".");
                return;
            }
            if (d < c) {
                CPanel.error(this, label + ": vàng tối đa phải lớn hơn hoặc bằng vàng tối thiểu.");
                return;
            }
            vals[i] = new int[]{a, b, c, d};
        }
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < GoldDropConfig.ALL.length; i++) {
            GoldDropConfig.Group g = GoldDropConfig.ALL[i];
            GoldDropConfig.set(g, vals[i][0], vals[i][1], vals[i][2], vals[i][3]);
            sb.append(g.label).append(": ").append(g.rate).append('/').append(g.per)
                    .append(" × ").append(CPanel.num(g.min)).append('–').append(CPanel.num(g.max)).append("; ");
        }
        reload();
        CPanel.log("Đã đổi tỉ lệ vàng rơi — " + sb);
        if (!saveToFile) {
            CPanel.info(this, "Đã áp dụng. Khởi động lại server sẽ quay về giá trị cũ nếu không lưu vào file.");
            return;
        }
        CPanel.async(this, "lưu cấu hình vàng rơi", () -> {
            GoldDropConfig.save();
            return null;
        }, x -> {
            CPanel.log("Đã lưu data/golddrop.properties.");
            CPanel.info(this, "Đã áp dụng và lưu vào data/golddrop.properties.");
        });
    }

    private void halve() {
        if (!CPanel.confirm(this, "Chia đôi vàng tối thiểu / tối đa của tất cả nhóm map?")) {
            return;
        }
        for (int i = 0; i < GoldDropConfig.ALL.length; i++) {
            Integer c = read(min[i], "Vàng tối thiểu");
            Integer d = read(max[i], "Vàng tối đa");
            if (c == null || d == null) {
                return;
            }
            min[i].setText(String.valueOf(c / 2));
            max[i].setText(String.valueOf(d / 2));
        }
        CPanel.info(this, "Đã chia đôi trong ô nhập. Bấm \"Áp dụng ngay\" hoặc \"Áp dụng và lưu vào file\" để có hiệu lực.");
    }
}
