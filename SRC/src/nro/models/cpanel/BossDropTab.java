package nro.models.cpanel;

import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.GridLayout;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.SwingUtilities;

import nro.models.boss.BossDropConfig;
import nro.models.boss.lop_truong.LopTruong;

/**
 * Tab "Rơi đồ boss": sửa tỉ lệ rơi đồ Thần Linh, đồ kích hoạt, bảng rơi của bộ boss
 * Lốp Trưởng và nhịp ra boss. Sửa xong bấm áp dụng là có hiệu lực ngay, muốn giữ sau
 * khi khởi động lại thì bấm lưu (ghi {@code data/bossdrop.properties}).
 *
 * <p>Phần dưới là điều khiển nhanh bộ Lốp Trưởng: cho ra ngay, kết thúc lượt, xem
 * trạng thái.
 */
final class BossDropTab extends JPanel {

    private final JTextField[] o = new JTextField[BossDropConfig.danhSach().length];
    private final JTextField[] oChu = new JTextField[BossDropConfig.danhSachChu().length];
    private final JLabel trangThai = new JLabel("-");
    private final JTextArea ghiChu = new JTextArea(7, 80);

    BossDropTab() {
        super(new BorderLayout(6, 6));
        setBorder(BorderFactory.createEmptyBorder(8, 8, 8, 8));

        JPanel luoi = new JPanel(new GridLayout(0, 3, 8, 6));
        luoi.setBorder(BorderFactory.createTitledBorder("Tỉ lệ rơi đồ — sửa xong nhớ bấm áp dụng"));
        luoi.add(new JLabel("Mục"));
        luoi.add(new JLabel("Giá trị"));
        luoi.add(new JLabel("Giải thích"));

        BossDropConfig.Muc[] ds = BossDropConfig.danhSach();
        for (int i = 0; i < ds.length; i++) {
            luoi.add(new JLabel(ds[i].nhan));
            o[i] = new JTextField(8);
            luoi.add(o[i]);
            luoi.add(new JLabel(ds[i].chuThich));
        }

        BossDropConfig.MucChu[] dsChu = BossDropConfig.danhSachChu();
        for (int i = 0; i < dsChu.length; i++) {
            luoi.add(new JLabel(dsChu[i].nhan));
            oChu[i] = new JTextField(14);
            luoi.add(oChu[i]);
            luoi.add(new JLabel(dsChu[i].chuThich));
        }

        JPanel nut = new JPanel(new WrapLayout(FlowLayout.LEFT, 8, 8));
        JButton apDung = new JButton("Áp dụng ngay");
        JButton luu = new JButton("Áp dụng và lưu vào file");
        JButton docLai = new JButton("Đọc lại giá trị đang chạy");
        JButton macDinh = new JButton("Về mặc định");
        JButton raNgay = new JButton("Lốp Trưởng: cho ra ngay");
        JButton ketThuc = new JButton("Lốp Trưởng: kết thúc lượt");
        nut.add(apDung);
        nut.add(luu);
        nut.add(docLai);
        nut.add(macDinh);
        nut.add(raNgay);
        nut.add(ketThuc);

        apDung.addActionListener(e -> apDung(false));
        luu.addActionListener(e -> apDung(true));
        docLai.addActionListener(e -> doc());
        macDinh.addActionListener(e -> {
            BossDropConfig.Muc[] all = BossDropConfig.danhSach();
            for (int i = 0; i < all.length; i++) {
                o[i].setText(String.valueOf(all[i].macDinh));
            }
            BossDropConfig.MucChu[] allChu = BossDropConfig.danhSachChu();
            for (int i = 0; i < allChu.length; i++) {
                oChu[i].setText(allChu[i].macDinh);
            }
        });
        raNgay.addActionListener(e -> {
            String kq = LopTruong.epRaNgay();
            capNhatTrangThai();
            JOptionPane.showMessageDialog(this, kq);
        });
        ketThuc.addActionListener(e -> {
            String kq = LopTruong.epKetThuc();
            capNhatTrangThai();
            JOptionPane.showMessageDialog(this, kq);
        });

        ghiChu.setEditable(false);
        ghiChu.setLineWrap(true);
        ghiChu.setWrapStyleWord(true);
        ghiChu.setText(
                "• Đồ Thần Linh: mọi boss đều dùng sàn/trần này, tỉ lệ thật tính theo máu hiệu dụng của từng con.\n"
                + "• Đồ kích hoạt: rơi 'rate' lần trên mỗi 'per' con quái, ở 9 map đầu game và Map riêng tư. "
                + "Mặc định 6/99990 ≈ đủ một set sau khoảng 15 ngày cày. Để 12/99990 là nhanh gấp đôi.\n"
                + "• Bảng Lốp Trưởng 20k cộng lại nên bằng 100. Thiếu thì phần dư rơi vào ngọc rồng, "
                + "thừa thì mấy mục cuối gần như không ra.\n"
                + "• Nhịp ra boss và trần sát thương đổi là ăn ngay ở lượt kế tiếp.\n"
                + "• Tỉ lệ vàng quái rơi nằm ở tab \"Vàng rơi\"; máu và sát thương từng boss nằm ở tab \"Boss\".");

        JPanel duoi = new JPanel(new BorderLayout());
        JPanel tt = new JPanel(new WrapLayout(FlowLayout.LEFT, 8, 4));
        tt.add(new JLabel("Lốp Trưởng:"));
        tt.add(trangThai);
        duoi.add(tt, BorderLayout.NORTH);
        duoi.add(new JScrollPane(ghiChu), BorderLayout.CENTER);

        add(new JScrollPane(luoi), BorderLayout.CENTER);
        add(nut, BorderLayout.NORTH);
        add(duoi, BorderLayout.SOUTH);

        doc();
        capNhatTrangThai();
        ScheduledExecutorService timer = Executors.newSingleThreadScheduledExecutor(r -> {
            Thread t = new Thread(r, "cpanel-bossdrop");
            t.setDaemon(true);
            return t;
        });
        timer.scheduleAtFixedRate(() -> SwingUtilities.invokeLater(this::capNhatTrangThai), 3, 3, TimeUnit.SECONDS);
    }

    private void doc() {
        BossDropConfig.Muc[] ds = BossDropConfig.danhSach();
        for (int i = 0; i < ds.length; i++) {
            o[i].setText(String.valueOf(ds[i].giaTri));
        }
        BossDropConfig.MucChu[] dsChu = BossDropConfig.danhSachChu();
        for (int i = 0; i < dsChu.length; i++) {
            oChu[i].setText(dsChu[i].giaTri);
        }
    }

    private void apDung(boolean luuFile) {
        BossDropConfig.Muc[] ds = BossDropConfig.danhSach();
        StringBuilder loi = new StringBuilder();
        int[] moi = new int[ds.length];
        for (int i = 0; i < ds.length; i++) {
            try {
                moi[i] = Integer.parseInt(o[i].getText().trim());
                if (moi[i] < 0) {
                    loi.append("\n• ").append(ds[i].nhan).append(": không được âm");
                }
            } catch (NumberFormatException ex) {
                loi.append("\n• ").append(ds[i].nhan).append(": phải là số nguyên");
            }
        }
        if (loi.length() > 0) {
            JOptionPane.showMessageDialog(this, "Chưa áp dụng, có ô sai:" + loi);
            return;
        }
        BossDropConfig.MucChu[] dsChu = BossDropConfig.danhSachChu();
        for (int i = 0; i < dsChu.length; i++) {
            String v = oChu[i].getText().trim();
            if (v.isEmpty() || !v.matches("\\d+(\\s*,\\s*\\d+)*")) {
                JOptionPane.showMessageDialog(this, "Chưa áp dụng: ô \"" + dsChu[i].nhan
                        + "\" phải là các id cách nhau bằng dấu phẩy, ví dụ 1150,1152,1151");
                return;
            }
        }
        for (int i = 0; i < ds.length; i++) {
            ds[i].giaTri = moi[i];
        }
        for (int i = 0; i < dsChu.length; i++) {
            dsChu[i].giaTri = oChu[i].getText().trim();
        }
        int tong = BossDropConfig.LT20_BINH.giaTri + BossDropConfig.LT20_BUA.giaTri
                + BossDropConfig.LT20_DA_BAO_VE.giaTri + BossDropConfig.LT20_SACH_DE_TU.giaTri
                + BossDropConfig.LT20_NGOC_RONG.giaTri;
        String canhBao = tong == 100 ? "" : "\n(Bảng Lốp Trưởng 20k đang cộng lại bằng " + tong + ", không phải 100)";
        if (luuFile) {
            BossDropConfig.save();
            JOptionPane.showMessageDialog(this, "Đã áp dụng và lưu vào data/bossdrop.properties" + canhBao);
        } else {
            JOptionPane.showMessageDialog(this, "Đã áp dụng cho tới khi khởi động lại" + canhBao);
        }
    }

    private void capNhatTrangThai() {
        trangThai.setText(LopTruong.moTaTrangThai());
    }
}
