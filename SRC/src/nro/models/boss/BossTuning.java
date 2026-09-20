package nro.models.boss;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;
import nro.models.boss.Boss_Manager.BossManager;
import nro.models.utils.Logger;

/**
 * Chỉnh số của boss lúc server đang chạy (tab "Boss" của cpanel): máu, sát thương,
 * % chặn sát thương và thời gian nghỉ — theo từng hình dạng.
 *
 * <p>Giá trị đã đổi được lưu ở {@code data/bosstuning.properties} theo khoá
 * {@code <id boss>.<hình dạng>.<mục>} và nạp lại sau khi khởi động, nên không cần
 * sửa {@link BossesData} rồi build lại.
 *
 * <p>Chỉ ghi đè những mục admin thực sự đổi; mục nào không có trong file thì giữ
 * nguyên giá trị gốc trong mã nguồn.
 */
public final class BossTuning {

    private static final String FILE = "data/bosstuning.properties";

    public static final String HP = "hp";
    public static final String DAME = "dame";
    public static final String REST = "rest";
    public static final String REDUCE = "reduce";

    private static final Properties P = new Properties();

    private BossTuning() {
    }

    // =====================================================================
    // NẠP / LƯU
    // =====================================================================
    /** Gọi sau khi BossManager đã tạo xong boss. Thiếu file thì không làm gì. */
    public static void load() {
        File f = new File(FILE);
        if (!f.exists()) {
            return;
        }
        synchronized (P) {
            P.clear();
            try (FileInputStream in = new FileInputStream(f)) {
                P.load(in);
            } catch (Exception e) {
                Logger.error("Không đọc được " + FILE + ": " + e + "\n");
                return;
            }
        }
        int n = applyAll();
        Logger.success("Đã nạp chỉnh số boss từ " + FILE + " (" + n + " boss)\n");
    }

    public static synchronized void save() throws Exception {
        File f = new File(FILE);
        if (f.getParentFile() != null) {
            f.getParentFile().mkdirs();
        }
        try (FileOutputStream out = new FileOutputStream(f)) {
            synchronized (P) {
                P.store(out, "Chinh so boss (hp / dame / rest / reduce) - sua trong tab 'Boss' cua cpanel");
            }
        }
    }

    /** Xoá toàn bộ chỉnh số đã lưu (boss quay về số gốc sau khi khởi động lại). */
    public static void clearAll() {
        synchronized (P) {
            P.clear();
        }
    }

    // =====================================================================
    // ĐỌC / GHI GIÁ TRỊ
    // =====================================================================
    private static String key(int bossId, int form, String what) {
        return bossId + "." + form + "." + what;
    }

    /** Giá trị admin đã đặt, hoặc null nếu chưa đặt. */
    public static Integer get(int bossId, int form, String what) {
        String v;
        synchronized (P) {
            v = P.getProperty(key(bossId, form, what));
        }
        if (v == null) {
            return null;
        }
        try {
            return Integer.valueOf(v.trim());
        } catch (NumberFormatException e) {
            return null;
        }
    }

    /**
     * Đặt một mục cho một hình dạng của boss và áp dụng ngay cho mọi boss cùng id.
     *
     * @param value null = bỏ ghi đè, trả về số gốc (chỉ có hiệu lực sau khi khởi động lại)
     */
    public static void set(int bossId, int form, String what, Integer value) {
        synchronized (P) {
            if (value == null) {
                P.remove(key(bossId, form, what));
            } else {
                P.setProperty(key(bossId, form, what), String.valueOf(value));
            }
        }
        for (Boss b : bossesById(bossId)) {
            apply(b);
        }
    }

    private static List<Boss> bossesById(int bossId) {
        List<Boss> out = new ArrayList<>();
        try {
            for (Boss b : new ArrayList<>(BossManager.gI().getBosses())) {
                if (b != null && (int) b.id == bossId) {
                    out.add(b);
                }
            }
        } catch (Throwable ignored) {
        }
        return out;
    }

    // =====================================================================
    // ÁP DỤNG
    // =====================================================================
    /** Áp dụng cho toàn bộ boss đang có; trả về số boss bị đổi. */
    public static int applyAll() {
        int n = 0;
        try {
            for (Boss b : new ArrayList<>(BossManager.gI().getBosses())) {
                if (b != null && apply(b)) {
                    n++;
                }
            }
        } catch (Throwable t) {
            Logger.error("Lỗi áp dụng chỉnh số boss: " + t + "\n");
        }
        return n;
    }

    /**
     * Áp dụng phần đã lưu cho một boss. Sửa thẳng {@link BossData} nên có hiệu lực
     * từ lần ra map kế tiếp; nếu boss đang ở map thì cập nhật luôn máu / sát thương
     * hiện tại (máu đang có được giữ theo đúng tỉ lệ, không hồi đầy).
     */
    public static boolean apply(Boss b) {
        if (b == null || b.data == null) {
            return false;
        }
        int bossId = (int) b.id;
        boolean changed = false;
        int[] reduce = b.getDamageReducePercentByLevel();
        for (int form = 0; form < b.data.length; form++) {
            BossData d = b.data[form];
            if (d == null) {
                continue;
            }
            Integer hp = get(bossId, form, HP);
            if (hp != null && hp > 0) {
                d.setHp(new int[]{hp});
                changed = true;
            }
            Integer dame = get(bossId, form, DAME);
            if (dame != null && dame >= 0) {
                d.setDame(dame);
                changed = true;
            }
            Integer rest = get(bossId, form, REST);
            if (rest != null && rest >= 0) {
                d.setSecondsRest(rest);
                if (form == 0) {
                    b.setSecondsRest(rest);
                }
                changed = true;
            }
            Integer pct = get(bossId, form, REDUCE);
            if (pct != null) {
                if (pct < 0) {
                    pct = 0;
                } else if (pct > BossDamageReduce.MAX_PERCENT) {
                    pct = BossDamageReduce.MAX_PERCENT;
                }
                if (reduce == null || reduce.length < b.data.length) {
                    int[] bigger = new int[b.data.length];
                    if (reduce != null) {
                        System.arraycopy(reduce, 0, bigger, 0, reduce.length);
                    }
                    reduce = bigger;
                }
                reduce[form] = pct;
                changed = true;
            }
        }
        if (reduce != null) {
            b.setDamageReducePercentByLevel(reduce);
        }
        if (changed) {
            refreshLive(b);
        }
        return changed;
    }

    /** Cập nhật máu / sát thương của boss đang đứng ở map theo số mới. */
    private static void refreshLive(Boss b) {
        try {
            int form = b.currentLevel;
            if (form < 0 || form >= b.data.length || b.nPoint == null) {
                return;
            }
            BossData d = b.data[form];
            int newHpFull = d.getHp()[0];
            int oldHpFull = b.nPoint.hpg;
            int oldHp = b.nPoint.hp;
            b.nPoint.hpg = newHpFull;
            b.nPoint.dameg = d.getDame();
            b.nPoint.calPoint();
            if (oldHpFull > 0 && oldHp > 0) {
                // giữ đúng tỉ lệ máu đang còn
                long keep = (long) newHpFull * oldHp / oldHpFull;
                b.nPoint.hp = (int) Math.max(1, Math.min(newHpFull, keep));
            } else {
                b.nPoint.hp = newHpFull;
            }
        } catch (Throwable ignored) {
        }
    }
}
