package nro.models.event;

import java.util.ArrayList;
import java.util.Collections;
import java.util.IdentityHashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Properties;
import java.util.Set;
import java.util.function.Supplier;
import nro.models.boss.Boss_Manager.BossManager;
import nro.models.boss.Boss_Manager.ChristmasEventManager;
import nro.models.boss.Boss_Manager.HalloweenEventManager;
import nro.models.boss.Boss_Manager.HungVuongEventManager;
import nro.models.boss.Boss_Manager.LunarNewYearEventManager;
import nro.models.boss.Boss_Manager.TrungThuEventManager;
import nro.models.event_list.TopUp;
import nro.models.event_list.TrungThu;
import nro.models.event_list.HungVuong;
import nro.models.event_list.Christmas;
import nro.models.event_list.Halloween;
import nro.models.event_list.LunarNewYear;
import nro.models.event_list.Default;
import nro.models.event_list.InternationalWomensDay;
import nro.models.utils.Logger;

/**
 * Quản lý sự kiện.
 *
 * Bật/tắt: khóa {@code event.<key>} trong Config.properties (đọc lúc khởi động).
 * Thiếu khóa -> dùng mặc định = đúng những sự kiện code cũ đang chạy
 * (Default, Hùng Vương, TopUp).
 *
 * Lúc server đang chạy (cpanel): {@link #setEnabled} bật/tắt ngay những sự kiện
 * {@link Def#runtime} = true (chỉ gồm boss / NPC do chính sự kiện tạo ra, gỡ được sạch);
 * sự kiện còn lại chỉ lưu cờ, có hiệu lực từ lần khởi động sau.
 *
 * SỬA LỖI boss sự kiện đứng im: boss có BossType sự kiện được đưa vào manager riêng
 * (HungVuongEventManager...), nhưng trước đây không ai start luồng của các manager đó.
 * {@link #startBossManagers()} (gọi từ ServerManager.run) start luồng cho manager của
 * sự kiện đang bật, mỗi manager đúng 1 lần.
 */
public class EventManager {

    private static EventManager instance;

    // Giữ lại các cờ cũ để tương thích; giá trị được nạp từ Config.properties trong init().
    public static boolean LUNNAR_NEW_YEAR = false;

    public static boolean INTERNATIONAL_WOMANS_DAY = false;

    public static boolean CHRISTMAS = false;

    public static boolean HALLOWEEN = false;

    public static boolean HUNG_VUONG = true;

    public static boolean TRUNG_THU = false;

    public static boolean TOP_UP = true;
    /** Quái rơi Tayaki / Kẹo táo / Kem que đôi / Mochi / Ramen / Khúc mía. Mặc định TẮT. */
    public static boolean THUC_AN_CHO_THAN = false;

    /** Mô tả 1 sự kiện có trong code. */
    public static final class Def {

        public final String key;
        public final String name;
        public final boolean defaultOn;
        /** true = bật/tắt ngay được khi server đang chạy. */
        public final boolean runtime;
        public final String note;
        final Supplier<Event> factory;
        final Supplier<BossManager> manager;

        Def(String key, String name, boolean defaultOn, boolean runtime, Supplier<Event> factory,
                Supplier<BossManager> manager, String note) {
            this.key = key;
            this.name = name;
            this.defaultOn = defaultOn;
            this.runtime = runtime;
            this.factory = factory;
            this.manager = manager;
            this.note = note;
        }

        public String configKey() {
            return "event." + key;
        }

        public boolean hasBossManager() {
            return manager != null;
        }
    }

    /** Trạng thái để hiển thị. */
    public static final class Status {

        public Def def;
        public boolean running;
        public boolean nextBoot;
        public int bosses;
        public int bossesOnMap;
        public boolean managerThread;
    }

    public static final List<Def> DEFS;

    static {
        List<Def> l = new ArrayList<>();
        l.add(new Def("default", "Mặc định (30 Broly)", true, false, Default::new, null,
                "Broly thuộc BrolyManager chung với boss khác; Broly có thể biến Super Broly -> không gỡ sạch được lúc chạy."));
        l.add(new Def("hung_vuong", "Hùng Vương (Thủy Tinh + Sơn Tinh)", true, true, HungVuong::new,
                HungVuongEventManager::gI, "10 Thủy Tinh, mỗi con kèm Sơn Tinh. NPC Hùng Vương (map 183-185) có sẵn trong map, không phụ thuộc cờ này."));
        l.add(new Def("halloween", "Halloween (Bí ma, Ma trơi, Dơi)", false, true, Halloween::new,
                HalloweenEventManager::gI, "10 mỗi loại, rơi Bí ngô (585)."));
        l.add(new Def("trung_thu", "Trung thu (Khỉ đột, Nguyệt thần + Nhật thần)", false, true, TrungThu::new,
                TrungThuEventManager::gI, "Nguyệt/Nhật thần rơi item 2123/2124 - kiểm tra có trong item_template trước khi bật."));
        l.add(new Def("christmas", "Noel (Ông già Noel)", false, true, Christmas::new,
                ChristmasEventManager::gI, "30 Ông già Noel, thả Hộp quà giáng sinh (648)."));
        l.add(new Def("lunar_new_year", "Tết (Lân con + NPC Đường Tăng)", false, true, LunarNewYear::new,
                LunarNewYearEventManager::gI, "Thêm NPC 49 ở map 0: người đang đứng ở map 0 phải đổi map mới thấy/mất NPC."));
        l.add(new Def("womens_day", "8/3 (Quốc tế phụ nữ)", false, false, InternationalWomensDay::new, null,
                "Chỉ đọc bảng `event` (không có trong DB) và dữ liệu không được dùng -> không có hiệu lực thực tế."));
        l.add(new Def("top_up", "TopUp (nạp)", true, false, TopUp::new, null,
                "Lớp rỗng, không làm gì."));
        l.add(new Def("thuc_an_cho_than", "Thức ăn cho thần (quái rơi Tayaki, Kẹo táo, Kem que đôi, Mochi, Ramen, Khúc mía)",
                false, true, nro.models.event_list.ThucAnChoThan::new, null,
                "Chỉ là cờ tỉ lệ rơi đồ ở mọi map, bật/tắt có hiệu lực ngay. Mặc định TẮT."));
        DEFS = Collections.unmodifiableList(l);
    }

    /** Sự kiện đang chạy: key -> instance (để gỡ lúc chạy). */
    private final java.util.Map<String, Event> running = new LinkedHashMap<>();
    /** Manager boss sự kiện đã được start luồng (chống start trùng). */
    private final Set<BossManager> startedManagers = Collections.newSetFromMap(new IdentityHashMap<>());
    private boolean initialized;

    public static EventManager gI() {
        if (instance == null) {
            instance = new EventManager();
        }
        return instance;
    }

    public static Def find(String key) {
        for (Def d : DEFS) {
            if (d.key.equals(key)) {
                return d;
            }
        }
        return null;
    }

    public synchronized void init() {
        if (initialized) {
            return;
        }
        initialized = true;
        Properties p = EventConfig.load();
        for (Def d : DEFS) {
            boolean on = EventConfig.getBool(p, d.configKey(), d.defaultOn);
            setLegacyFlag(d.key, on);
            if (on) {
                startEvent(d);
            }
        }
        Logger.success("Su kien dang bat: " + running.keySet() + "\n");
    }

    private void startEvent(Def d) {
        try {
            Event e = d.factory.get();
            e.init();
            running.put(d.key, e);
        } catch (Exception ex) {
            Logger.error("Loi khoi dong su kien " + d.key + ": " + ex + "\n");
        }
    }

    /**
     * Start luồng update cho manager boss của các sự kiện đang bật.
     * Gọi từ ServerManager.run() cùng chỗ với các BossManager khác. Gọi lại nhiều lần vẫn an toàn.
     */
    public synchronized void startBossManagers() {
        for (Def d : DEFS) {
            if (running.containsKey(d.key) && d.hasBossManager()) {
                ensureManagerThread(d);
            }
        }
    }

    private void ensureManagerThread(Def d) {
        BossManager m = d.manager.get();
        if (startedManagers.add(m)) {
            new Thread(m, "Update " + d.key + " event boss").start();
            Logger.success("Da start luong boss su kien: " + d.key + "\n");
        }
    }

    public synchronized boolean isRunning(String key) {
        return running.containsKey(key);
    }

    public synchronized List<Status> getStatus() {
        Properties p = EventConfig.load();
        List<Status> out = new ArrayList<>();
        for (Def d : DEFS) {
            Status s = new Status();
            s.def = d;
            Event e = running.get(d.key);
            s.running = e != null;
            s.nextBoot = EventConfig.getBool(p, d.configKey(), d.defaultOn);
            if (e != null) {
                s.bosses = e.getAllBosses().size();
                s.bossesOnMap = e.countBossesOnMap();
            }
            s.managerThread = d.hasBossManager() && startedManagers.contains(d.manager.get());
            out.add(s);
        }
        return out;
    }

    /**
     * Bật/tắt 1 sự kiện: luôn lưu cờ vào Config.properties (lần khởi động sau);
     * nếu {@code applyNow} và sự kiện cho phép thì áp dụng ngay.
     *
     * @return mô tả kết quả
     */
    public synchronized String setEnabled(String key, boolean on, boolean applyNow) throws Exception {
        Def d = find(key);
        if (d == null) {
            throw new IllegalArgumentException("Không có sự kiện " + key);
        }
        EventConfig.set(d.configKey(), String.valueOf(on), "Su kien " + d.key + " (cpanel)");
        setLegacyFlag(key, on);
        StringBuilder sb = new StringBuilder("Đã lưu " + d.configKey() + "=" + on + " (lần khởi động sau).");
        if (!applyNow) {
            return sb.toString();
        }
        if (!d.runtime) {
            sb.append(" Sự kiện này KHÔNG bật/tắt được lúc đang chạy -> cần khởi động lại server.");
            return sb.toString();
        }
        if (on) {
            if (running.containsKey(key)) {
                sb.append(" Sự kiện đã đang chạy.");
            } else {
                startEvent(d);
                if (d.hasBossManager()) {
                    ensureManagerThread(d);
                }
                Event e = running.get(key);
                sb.append(" Đã bật ngay: ").append(e == null ? 0 : e.getAllBosses().size()).append(" boss.");
            }
        } else {
            Event e = running.remove(key);
            if (e == null) {
                sb.append(" Sự kiện không chạy.");
            } else {
                int n = e.stop(d.hasBossManager() ? d.manager.get() : null);
                sb.append(" Đã tắt ngay: gỡ ").append(n).append(" boss.");
            }
        }
        return sb.toString();
    }

    private static void setLegacyFlag(String key, boolean on) {
        switch (key) {
            case "lunar_new_year" ->
                LUNNAR_NEW_YEAR = on;
            case "womens_day" ->
                INTERNATIONAL_WOMANS_DAY = on;
            case "christmas" ->
                CHRISTMAS = on;
            case "halloween" ->
                HALLOWEEN = on;
            case "hung_vuong" ->
                HUNG_VUONG = on;
            case "trung_thu" ->
                TRUNG_THU = on;
            case "top_up" ->
                TOP_UP = on;
            case "thuc_an_cho_than" ->
                THUC_AN_CHO_THAN = on;
            default -> {
            }
        }
    }
}
