package nro.models.cpanel;

import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.GridLayout;
import java.util.List;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;
import nro.models.database.PlayerDAO;
import nro.models.network.SessionManager;
import nro.models.player.Player;
import nro.models.server.AutoMaintenance;
import nro.models.server.Client;
import nro.models.server.Maintenance;
import nro.models.server.ServerManager;
import nro.models.services.ClanService;
import nro.models.shop_ky_gui.ConsignShopManager;

/**
 * Tab "Server": thông số, bảo trì (gọi đúng Maintenance có sẵn), lưu toàn bộ dữ liệu.
 */
final class ServerTab extends JPanel {

    private final JLabel lblOnline = new JLabel("-");
    private final JLabel lblSessions = new JLabel("-");
    private final JLabel lblMemory = new JLabel("-");
    private final JLabel lblThreads = new JLabel("-");
    private final JLabel lblStart = new JLabel("-");
    private final JLabel lblMaint = new JLabel("-");
    private final JCheckBox chkAuto = new JCheckBox("Bảo trì tự động hằng ngày (04:30)");
    private ScheduledFuture<?> future;

    ServerTab() {
        super(new BorderLayout(6, 6));
        setBorder(BorderFactory.createEmptyBorder(8, 8, 8, 8));

        JPanel info = new JPanel(new GridLayout(0, 2, 8, 6));
        info.setBorder(BorderFactory.createTitledBorder("Thông số (tự làm mới mỗi 2 giây)"));
        info.add(new JLabel("Người chơi online:"));
        info.add(lblOnline);
        info.add(new JLabel("Số phiên kết nối:"));
        info.add(lblSessions);
        info.add(new JLabel("Bộ nhớ (đang dùng / đã cấp / tối đa):"));
        info.add(lblMemory);
        info.add(new JLabel("Số luồng:"));
        info.add(lblThreads);
        info.add(new JLabel("Khởi động lúc:"));
        info.add(lblStart);
        info.add(new JLabel("Trạng thái bảo trì:"));
        info.add(lblMaint);

        JPanel actions = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 8));
        actions.setBorder(BorderFactory.createTitledBorder("Thao tác"));
        JButton btnSave = new JButton("Lưu toàn bộ dữ liệu");
        JButton btnMaint = new JButton("Bảo trì...");
        JButton btnGc = new JButton("Dọn bộ nhớ (GC)");
        actions.add(btnSave);
        actions.add(btnMaint);
        actions.add(btnGc);
        actions.add(chkAuto);

        JPanel north = new JPanel(new BorderLayout(6, 6));
        north.add(info, BorderLayout.NORTH);
        north.add(actions, BorderLayout.CENTER);
        add(north, BorderLayout.NORTH);

        btnSave.addActionListener(e -> saveAll());
        btnMaint.addActionListener(e -> maintenance());
        btnGc.addActionListener(e -> {
            CPanel.async(this, "dọn bộ nhớ", () -> {
                System.gc();
                return null;
            }, x -> CPanel.log("Đã gọi System.gc()"));
        });
        chkAuto.setSelected(AutoMaintenance.AutoMaintenance);
        chkAuto.addActionListener(e -> {
            AutoMaintenance.AutoMaintenance = chkAuto.isSelected();
            CPanel.log(chkAuto.isSelected() ? "Đã bật chế độ bảo trì tự động." : "Đã tắt chế độ bảo trì tự động.");
        });
    }

    void startAutoRefresh() {
        future = CPanel.TIMER.scheduleWithFixedDelay(this::refresh, 0, 2, TimeUnit.SECONDS);
    }

    void stopAutoRefresh() {
        if (future != null) {
            future.cancel(false);
        }
    }

    /** Chạy trên luồng CPanel Timer, chỉ đọc số liệu trong bộ nhớ. */
    private void refresh() {
        try {
            int online = 0;
            for (Player p : Client.gI().getPlayersSnapshot()) {
                if (p.isPl()) {
                    online++;
                }
            }
            int sessions = SessionManager.gI().getSessions().size();
            Runtime rt = Runtime.getRuntime();
            long mb = 1024L * 1024L;
            String mem = (rt.totalMemory() - rt.freeMemory()) / mb + " MB / " + rt.totalMemory() / mb + " MB / "
                    + rt.maxMemory() / mb + " MB";
            int threads = Thread.activeCount();
            String maint = Maintenance.isRunning ? "ĐANG BẢO TRÌ / ĐẾM NGƯỢC" : "Bình thường";
            boolean auto = AutoMaintenance.AutoMaintenance;
            int onl = online;
            SwingUtilities.invokeLater(() -> {
                lblOnline.setText(String.valueOf(onl));
                lblSessions.setText(String.valueOf(sessions));
                lblMemory.setText(mem);
                lblThreads.setText(String.valueOf(threads));
                lblStart.setText(ServerManager.timeStart == null ? "-" : ServerManager.timeStart);
                lblMaint.setText(maint);
                if (chkAuto.isSelected() != auto) {
                    chkAuto.setSelected(auto);
                }
            });
        } catch (Throwable ignored) {
        }
    }

    private void saveAll() {
        if (!CPanel.confirm(this, "Lưu ngay dữ liệu của mọi người chơi online, bang hội và shop ký gửi xuống DB?\n"
                + "(Chạy nền, có thể mất vài giây khi đông người.)")) {
            return;
        }
        CPanel.async(this, "lưu toàn bộ dữ liệu", () -> {
            if (Maintenance.isRunning) {
                throw new CPanel.CPanelException("Server đang bảo trì, dữ liệu đang được lưu bởi tiến trình bảo trì.");
            }
            CPanel.log("Bắt đầu lưu toàn bộ dữ liệu...");
            long st = System.currentTimeMillis();
            int ok = 0;
            int fail = 0;
            List<Player> players = Client.gI().getPlayersSnapshot();
            for (Player p : players) {
                if (p == null || !p.isPl() || p.beforeDispose || p.isOffline) {
                    continue;
                }
                try {
                    // Dùng đúng hàm tự lưu có sẵn (có khóa saveLock, bỏ qua người đang thoát)
                    PlayerDAO.autoSavePlayer(p);
                    p.lastTimeAutoSave = System.currentTimeMillis();
                    ok++;
                } catch (Exception e) {
                    fail++;
                    CPanel.log("Lỗi lưu " + p.name + ": " + e.getMessage());
                }
            }
            String clan = "ok";
            try {
                ClanService.gI().close(); // tên hàm là close nhưng chỉ ghi dữ liệu bang xuống DB
            } catch (Exception e) {
                clan = "lỗi: " + e.getMessage();
            }
            String consign = "ok";
            try {
                ConsignShopManager.gI().save();
            } catch (Exception e) {
                consign = "lỗi: " + e.getMessage();
            }
            return "Đã lưu " + ok + " nhân vật" + (fail > 0 ? " (" + fail + " lỗi)" : "") + ", bang hội: " + clan
                    + ", shop ký gửi: " + consign + " trong " + (System.currentTimeMillis() - st) + " ms.";
        }, msg -> {
            CPanel.log(msg);
            CPanel.info(this, msg);
        });
    }

    private void maintenance() {
        if (Maintenance.isRunning) {
            CPanel.error(this, "Server đang bảo trì / đếm ngược rồi.");
            return;
        }
        Long sec = CPanel.askLong(this, "Bảo trì sau bao nhiêu giây? (người chơi được báo mỗi giây)\n"
                + "Hết giờ: lưu dữ liệu, kick tất cả, chạy restart_server.bat rồi TẮT server.", "60");
        if (sec == null) {
            return;
        }
        if (sec < 5 || sec > 3600) {
            CPanel.error(this, "Chỉ nhận từ 5 đến 3600 giây.");
            return;
        }
        if (!CPanel.confirm(this, "BẢO TRÌ server sau " + sec + " giây?\nKhông hủy được sau khi bắt đầu.")) {
            return;
        }
        int s = sec.intValue();
        CPanel.async(this, "bảo trì", () -> {
            Maintenance.gI().startSeconds(s);
            return null;
        }, x -> CPanel.log("Admin cpanel bắt đầu bảo trì sau " + s + " giây."));
    }
}
