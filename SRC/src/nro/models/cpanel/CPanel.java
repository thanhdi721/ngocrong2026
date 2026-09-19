package nro.models.cpanel;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.GraphicsEnvironment;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.FileInputStream;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Enumeration;
import java.util.Properties;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Consumer;
import javax.swing.BorderFactory;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTabbedPane;
import javax.swing.JTextArea;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import javax.swing.plaf.FontUIResource;
import javax.swing.text.DefaultCaret;
import nro.models.server.ServerManager;
import nro.models.utils.Logger;

/**
 * Bảng điều khiển (cpanel) dạng cửa sổ Swing, tự bật khi chạy server.
 *
 * Nguyên tắc:
 * - Mọi truy vấn DB / thao tác nặng chạy trên luồng nền {@link #WORKER} (1 luồng,
 *   daemon) -> không chặn luồng giao diện, không chặn luồng game, và cpanel chỉ
 *   chiếm tối đa 1 kết nối của pool HikariCP tại một thời điểm.
 * - Máy headless (VPS không màn hình) hoặc tắt bằng khóa {@code server.cpanel=false}
 *   trong Config.properties -> bỏ qua, server vẫn chạy bình thường.
 * - Đóng cửa sổ KHÔNG tắt server.
 */
public final class CPanel {

    /** Khóa trong Config.properties để bật/tắt cpanel (mặc định bật). */
    public static final String CONFIG_KEY = "server.cpanel";

    /** Luồng nền duy nhất cho mọi thao tác ghi/đọc DB của cpanel (tuần tự, daemon). */
    static final ExecutorService WORKER = Executors.newSingleThreadExecutor(r -> {
        Thread t = new Thread(r, "CPanel Worker");
        t.setDaemon(true);
        return t;
    });

    /** Luồng nền cho việc làm mới định kỳ (chỉ đọc bộ nhớ) và hẹn giờ kick sau khi ban. */
    static final ScheduledExecutorService TIMER = Executors.newSingleThreadScheduledExecutor(r -> {
        Thread t = new Thread(r, "CPanel Timer");
        t.setDaemon(true);
        return t;
    });

    private static final AtomicInteger PENDING = new AtomicInteger();
    private static volatile boolean started;
    private static JFrame frame;
    private static JLabel statusLabel;
    private static JTextArea logArea;
    private static OnlineTab onlineTab;
    private static ServerTab serverTab;

    private CPanel() {
    }

    // =====================================================================
    // KHỞI ĐỘNG
    // =====================================================================
    /**
     * Gọi 1 lần từ ServerManager.main(). Không bao giờ ném lỗi ra ngoài.
     */
    public static void startIfEnabled() {
        try {
            if (started) {
                return;
            }
            if (!isEnabledInConfig()) {
                log("Cpanel đang tắt (" + CONFIG_KEY + "=false trong Config.properties).");
                return;
            }
            if (GraphicsEnvironment.isHeadless()) {
                log("Máy chủ không có màn hình (headless) -> bỏ qua cpanel, server vẫn chạy bình thường.");
                return;
            }
            started = true;
            SwingUtilities.invokeLater(() -> {
                try {
                    createAndShow();
                    log("Đã mở bảng điều khiển.");
                } catch (Throwable t) {
                    // VD: DISPLAY đặt sai, thiếu thư viện đồ họa... -> không được làm sập server
                    log("Không mở được cpanel: " + t + " (server vẫn chạy bình thường).");
                }
            });
        } catch (Throwable t) {
            log("Không khởi động được cpanel: " + t + " (server vẫn chạy bình thường).");
        }
    }

    private static boolean isEnabledInConfig() {
        Properties p = new Properties();
        try (FileInputStream in = new FileInputStream("Config.properties")) {
            p.load(in);
        } catch (Exception e) {
            return true; // không đọc được file -> dùng mặc định: bật
        }
        String v = p.getProperty(CONFIG_KEY, "true").trim();
        return !(v.equalsIgnoreCase("false") || v.equals("0") || v.equalsIgnoreCase("off") || v.equalsIgnoreCase("no"));
    }

    private static void createAndShow() {
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Exception ignored) {
        }
        applyVietnameseFont();

        frame = new JFrame("NRO CPanel - " + ServerManager.NAME + " - cổng " + ServerManager.PORT);
        frame.setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
        frame.addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                onClose();
            }
        });

        JTabbedPane tabs = new JTabbedPane();
        onlineTab = new OnlineTab();
        serverTab = new ServerTab();
        tabs.addTab("Tài khoản", new AccountTab());
        tabs.addTab("Người chơi online", onlineTab);
        tabs.addTab("Nhân vật", new CharacterTab());
        tabs.addTab("Server", serverTab);
        tabs.addTab("Buff đồ", new BuffTab());
        tabs.addTab("Sự kiện", new EventTab());

        logArea = new JTextArea(6, 80);
        logArea.setEditable(false);
        ((DefaultCaret) logArea.getCaret()).setUpdatePolicy(DefaultCaret.ALWAYS_UPDATE);
        JScrollPane logScroll = new JScrollPane(logArea);
        logScroll.setBorder(BorderFactory.createTitledBorder("Nhật ký thao tác (cũng in ra console)"));

        statusLabel = new JLabel(" Sẵn sàng");
        statusLabel.setBorder(BorderFactory.createEmptyBorder(2, 6, 2, 6));

        JPanel south = new JPanel(new BorderLayout());
        south.add(logScroll, BorderLayout.CENTER);
        south.add(statusLabel, BorderLayout.SOUTH);

        frame.getContentPane().setLayout(new BorderLayout());
        frame.getContentPane().add(tabs, BorderLayout.CENTER);
        frame.getContentPane().add(south, BorderLayout.SOUTH);
        frame.setMinimumSize(new Dimension(900, 600));
        frame.setSize(1100, 720);
        frame.setLocationByPlatform(true);
        frame.setVisible(true);

        onlineTab.startAutoRefresh();
        serverTab.startAutoRefresh();
    }

    private static void onClose() {
        Object[] options = {"Thu nhỏ", "Đóng bảng điều khiển", "Hủy"};
        int c = JOptionPane.showOptionDialog(frame,
                "Đóng bảng điều khiển KHÔNG tắt server.\n"
                + "Nếu đóng hẳn, muốn mở lại phải khởi động lại server.",
                "Đóng cpanel", JOptionPane.DEFAULT_OPTION, JOptionPane.QUESTION_MESSAGE,
                null, options, options[0]);
        if (c == 0) {
            frame.setState(JFrame.ICONIFIED);
        } else if (c == 1) {
            if (onlineTab != null) {
                onlineTab.stopAutoRefresh();
            }
            if (serverTab != null) {
                serverTab.stopAutoRefresh();
            }
            frame.dispose();
            log("Đã đóng bảng điều khiển (server vẫn chạy).");
        }
    }

    /** Chọn font hiển thị được tiếng Việt có dấu và áp dụng cho toàn bộ Swing. */
    private static void applyVietnameseFont() {
        String sample = "Tiếng Việt: ắằẳẵặấầẩẫậđươỳỹ";
        String chosen = Font.DIALOG;
        String[] candidates = {"Segoe UI", "Tahoma", "Arial", "Noto Sans", "DejaVu Sans", "Liberation Sans"};
        try {
            java.util.Set<String> names = new java.util.HashSet<>(java.util.Arrays.asList(
                    GraphicsEnvironment.getLocalGraphicsEnvironment().getAvailableFontFamilyNames()));
            for (String n : candidates) {
                if (names.contains(n) && new Font(n, Font.PLAIN, 13).canDisplayUpTo(sample) == -1) {
                    chosen = n;
                    break;
                }
            }
        } catch (Throwable ignored) {
        }
        FontUIResource f = new FontUIResource(chosen, Font.PLAIN, 13);
        Enumeration<Object> keys = UIManager.getDefaults().keys();
        while (keys.hasMoreElements()) {
            Object k = keys.nextElement();
            if (UIManager.get(k) instanceof FontUIResource) {
                UIManager.put(k, f);
            }
        }
    }

    // =====================================================================
    // TIỆN ÍCH CHUNG
    // =====================================================================
    /** Công việc chạy nền, được phép ném lỗi. */
    public interface Task<T> {

        T run() throws Exception;
    }

    /** Lỗi nghiệp vụ hiển thị thẳng cho admin (không in stack trace). */
    public static class CPanelException extends Exception {

        public CPanelException(String msg) {
            super(msg);
        }
    }

    /** Thao tác offline gặp tình huống cần admin xác nhận rồi chạy lại với force = true. */
    public static class NeedConfirmException extends Exception {

        public NeedConfirmException(String msg) {
            super(msg);
        }
    }

    /** Chạy task trên luồng nền; kết quả / lỗi được trả về luồng giao diện. */
    public static <T> void async(Component parent, String what, Task<T> task, Consumer<T> onDone) {
        async(parent, what, task, onDone, null);
    }

    public static <T> void async(Component parent, String what, Task<T> task, Consumer<T> onDone,
            Consumer<Throwable> onError) {
        setStatus(PENDING.incrementAndGet());
        WORKER.submit(() -> {
            try {
                T r = task.run();
                SwingUtilities.invokeLater(() -> {
                    setStatus(PENDING.decrementAndGet());
                    if (onDone != null) {
                        onDone.accept(r);
                    }
                });
            } catch (Throwable e) {
                if (!(e instanceof CPanelException) && !(e instanceof NeedConfirmException)) {
                    log("LỖI khi " + what + ": " + e);
                    e.printStackTrace();
                }
                SwingUtilities.invokeLater(() -> {
                    setStatus(PENDING.decrementAndGet());
                    if (onError != null) {
                        onError.accept(e);
                    } else {
                        error(parent, "Lỗi khi " + what + ":\n" + messageOf(e));
                    }
                });
            }
        });
    }

    private static void setStatus(int pending) {
        if (statusLabel == null) {
            return;
        }
        Runnable r = () -> statusLabel.setText(pending > 0 ? " Đang xử lý (" + pending + " việc)..." : " Sẵn sàng");
        if (SwingUtilities.isEventDispatchThread()) {
            r.run();
        } else {
            SwingUtilities.invokeLater(r);
        }
    }

    static String messageOf(Throwable e) {
        String m = e.getMessage();
        return m == null || m.isEmpty() ? e.toString() : m;
    }

    /** Ghi log ra console (và khung nhật ký của cpanel nếu đang mở). */
    public static void log(String msg) {
        String line = "[CPANEL " + new SimpleDateFormat("HH:mm:ss").format(new Date()) + "] " + msg;
        try {
            Logger.log(Logger.CYAN, line + "\n");
        } catch (Throwable t) {
            System.out.println(line);
        }
        JTextArea area = logArea;
        if (area != null) {
            SwingUtilities.invokeLater(() -> {
                area.append(line + "\n");
                if (area.getLineCount() > 1000) {
                    try {
                        area.replaceRange("", 0, area.getLineEndOffset(200));
                    } catch (Exception ignored) {
                    }
                }
            });
        }
    }

    static boolean confirm(Component parent, String msg) {
        Object[] options = {"Đồng ý", "Hủy"};
        int c = JOptionPane.showOptionDialog(parent, msg, "Xác nhận",
                JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE, null, options, options[1]);
        return c == 0;
    }

    static void info(Component parent, String msg) {
        JOptionPane.showMessageDialog(parent, msg, "Thông báo", JOptionPane.INFORMATION_MESSAGE);
    }

    static void error(Component parent, String msg) {
        JOptionPane.showMessageDialog(parent, msg, "Lỗi", JOptionPane.ERROR_MESSAGE);
    }

    /** Hỏi một chuỗi; trả về null nếu hủy. */
    static String askString(Component parent, String msg, String init) {
        Object v = JOptionPane.showInputDialog(parent, msg, "Nhập", JOptionPane.QUESTION_MESSAGE, null, null, init);
        return v == null ? null : v.toString().trim();
    }

    /** Hỏi một số nguyên (có thể âm); trả về null nếu hủy hoặc nhập sai. */
    static Long askLong(Component parent, String msg, String init) {
        String s = askString(parent, msg, init);
        if (s == null || s.isEmpty()) {
            return null;
        }
        try {
            return Long.parseLong(s.replace(".", "").replace(",", "").replace(" ", ""));
        } catch (NumberFormatException e) {
            error(parent, "Giá trị không hợp lệ: " + s);
            return null;
        }
    }

    private static final DecimalFormatSymbols SYM = new DecimalFormatSymbols();

    static {
        SYM.setGroupingSeparator('.');
    }

    static String num(long v) {
        return new DecimalFormat("#,##0", SYM).format(v);
    }

    static String planet(int gender) {
        switch (gender) {
            case 0:
                return "Trái Đất";
            case 1:
                return "Namếc";
            case 2:
                return "Xayda";
            default:
                return "?" + gender;
        }
    }
}
