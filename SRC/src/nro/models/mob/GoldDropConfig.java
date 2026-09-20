package nro.models.mob;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.util.Properties;
import nro.models.utils.Logger;

/**
 * Cấu hình vàng rơi từ quái, tách khỏi {@link Mob} để sửa được lúc server đang
 * chạy (tab "Vàng rơi" của cpanel) và lưu lại vào {@code data/golddrop.properties}.
 *
 * <p>Mỗi nhóm map có: tỉ lệ rơi dạng {@code rate / per} (ví dụ 1/20 = 5%) và
 * khoảng vàng {@code min..max} mỗi lần rơi. Giá trị mặc định dưới đây đúng bằng
 * bản gốc trong {@code Mob.dropItemMap} trước khi tách.
 */
public final class GoldDropConfig {

    /** Một nhóm map. */
    public static final class Group {

        public final String key;
        public final String label;
        public volatile int rate;
        public volatile int per;
        public volatile int min;
        public volatile int max;

        Group(String key, String label, int rate, int per, int min, int max) {
            this.key = key;
            this.label = label;
            this.rate = rate;
            this.per = per;
            this.min = min;
            this.max = max;
        }

        /** Vàng trung bình mỗi con quái bị hạ. */
        public double avgPerKill() {
            if (per <= 0) {
                return 0;
            }
            return (double) rate / per * ((min + max) / 2.0);
        }
    }

    public static final Group THREE_PLANETS = new Group("three_planets", "3 hành tinh (map thường)", 1, 20, 500, 3000);
    public static final Group NAPPA = new Group("nappa", "Khu Nappa / Fide", 1, 100, 2000, 6000);
    public static final Group COLD = new Group("cold", "Map Băng (105–110)", 30, 100, 150000, 250000);
    public static final Group FUTURE = new Group("future", "Map Tương Lai", 15, 100, 80000, 150000);
    public static final Group DUNGEON = new Group("dungeon", "Phó bản", 1, 100, 80000, 200000);

    public static final Group[] ALL = {THREE_PLANETS, NAPPA, COLD, FUTURE, DUNGEON};

    private static final String FILE = "data/golddrop.properties";

    private GoldDropConfig() {
    }

    /** Gọi 1 lần lúc khởi động server. Thiếu file thì giữ giá trị mặc định. */
    public static void load() {
        File f = new File(FILE);
        if (!f.exists()) {
            return;
        }
        Properties p = new Properties();
        try (FileInputStream in = new FileInputStream(f)) {
            p.load(in);
        } catch (Exception e) {
            Logger.error("Không đọc được " + FILE + ": " + e + "\n");
            return;
        }
        for (Group g : ALL) {
            g.rate = readInt(p, g.key + ".rate", g.rate);
            g.per = readInt(p, g.key + ".per", g.per);
            g.min = readInt(p, g.key + ".min", g.min);
            g.max = readInt(p, g.key + ".max", g.max);
            normalize(g);
        }
        Logger.success("Đã nạp cấu hình vàng rơi từ " + FILE + "\n");
    }

    /** Ghi lại cấu hình hiện tại ra file để giữ sau khi khởi động lại. */
    public static synchronized void save() throws Exception {
        Properties p = new Properties();
        for (Group g : ALL) {
            p.setProperty(g.key + ".rate", String.valueOf(g.rate));
            p.setProperty(g.key + ".per", String.valueOf(g.per));
            p.setProperty(g.key + ".min", String.valueOf(g.min));
            p.setProperty(g.key + ".max", String.valueOf(g.max));
        }
        File f = new File(FILE);
        if (f.getParentFile() != null) {
            f.getParentFile().mkdirs();
        }
        try (FileOutputStream out = new FileOutputStream(f)) {
            p.store(out, "Ti le vang roi tu quai - sua trong tab 'Vang roi' cua cpanel");
        }
    }

    /** Đặt giá trị mới cho một nhóm (dùng từ cpanel). Tự kẹp về khoảng hợp lệ. */
    public static void set(Group g, int rate, int per, int min, int max) {
        g.rate = rate;
        g.per = per;
        g.min = min;
        g.max = max;
        normalize(g);
    }

    private static void normalize(Group g) {
        if (g.per < 1) {
            g.per = 1;
        }
        if (g.rate < 0) {
            g.rate = 0;
        }
        if (g.rate > g.per) {
            g.rate = g.per;
        }
        if (g.min < 0) {
            g.min = 0;
        }
        if (g.max < g.min) {
            g.max = g.min;
        }
    }

    private static int readInt(Properties p, String key, int def) {
        try {
            String v = p.getProperty(key);
            return v == null ? def : Integer.parseInt(v.trim());
        } catch (NumberFormatException e) {
            return def;
        }
    }
}
