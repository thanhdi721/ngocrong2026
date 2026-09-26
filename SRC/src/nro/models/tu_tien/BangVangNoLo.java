package nro.models.tu_tien;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import nro.models.player.Player;
import nro.models.services.Service;
import nro.models.utils.Logger;
import org.json.simple.JSONObject;
import org.json.simple.JSONValue;

/**
 * Bảng vàng <b>"Vua Nổ Lò"</b> — đếm số lần nổ lò của từng người chơi.
 *
 * <p>Cố ý KHÔNG thêm cột vào bảng {@code player}: bảng đó đang sát trần 65.535 byte một dòng
 * của InnoDB (xem patch 78). Số đếm nằm trong bộ nhớ và được ghi ra
 * {@code data/no_lo.json} — đúng cách mà {@code boss.drop.BangRoiBoss} đang làm với bảng rơi
 * đồ boss. Mất file thì chỉ mất bảng vàng, không ảnh hưởng nhân vật.
 *
 * <p>Ghi file theo nhịp: mỗi lần nổ chỉ đánh dấu "bẩn", cứ {@value #NHIP_GHI_MS} mili giây mới
 * ghi một lần, để trăm người nổ lò cùng lúc không thành trăm lần mở file.
 */
public final class BangVangNoLo {

    private static final String DUONG_DAN = "data/no_lo.json";
    private static final long NHIP_GHI_MS = 60_000;
    /** Số dòng hiện trên bảng vàng. Gói tin -96 ghi số dòng bằng một byte nên phải < 127. */
    private static final int TOI_DA_DONG = 50;

    /** id nhân vật -> {tên lúc nổ gần nhất, số lần nổ}. */
    private static final Map<Long, Dong> BANG = new ConcurrentHashMap<>();
    private static volatile boolean daNap;
    private static volatile boolean ban;
    private static volatile long lanGhiCuoi;

    private BangVangNoLo() {
    }

    /** Một dòng bảng vàng. */
    public static final class Dong {

        public final long id;
        public volatile String ten;
        public volatile int soLan;
        /** Ngoại hình lúc nổ gần nhất, để khung danh sách có hình mà vẽ. */
        public volatile short head;
        public volatile short body;
        public volatile short leg;

        Dong(long id, String ten) {
            this.id = id;
            this.ten = ten;
        }
    }

    //================================ nạp / ghi ================================
    public static synchronized void baoDamDaNap() {
        if (daNap) {
            return;
        }
        daNap = true;
        File f = new File(DUONG_DAN);
        if (!f.exists()) {
            return;
        }
        try (FileReader r = new FileReader(f, java.nio.charset.StandardCharsets.UTF_8)) {
            StringBuilder sb = new StringBuilder();
            int c;
            while ((c = r.read()) != -1) {
                sb.append((char) c);
            }
            Object o = JSONValue.parse(sb.toString());
            if (!(o instanceof JSONObject)) {
                return;
            }
            JSONObject root = (JSONObject) o;
            for (Object k : root.keySet()) {
                long id;
                try {
                    id = Long.parseLong(String.valueOf(k).trim());
                } catch (NumberFormatException e) {
                    continue;
                }
                Object v = root.get(k);
                if (!(v instanceof JSONObject)) {
                    continue;
                }
                JSONObject j = (JSONObject) v;
                Dong d = new Dong(id, String.valueOf(j.getOrDefault("ten", "?")));
                d.soLan = soNguyen(j.get("soLan"));
                d.head = (short) soNguyen(j.get("head"));
                d.body = (short) soNguyen(j.get("body"));
                d.leg = (short) soNguyen(j.get("leg"));
                if (d.soLan > 0) {
                    BANG.put(id, d);
                }
            }
            Logger.success("Bang vang No Lo: nap " + BANG.size() + " dong\n");
        } catch (Exception e) {
            Logger.error("Khong nap duoc " + DUONG_DAN + ": " + e + "\n");
        }
    }

    private static int soNguyen(Object o) {
        try {
            return o == null ? 0 : (int) Long.parseLong(String.valueOf(o).trim());
        } catch (NumberFormatException e) {
            return 0;
        }
    }

    @SuppressWarnings("unchecked")
    public static synchronized boolean luu() {
        JSONObject root = new JSONObject();
        for (Dong d : BANG.values()) {
            JSONObject j = new JSONObject();
            j.put("ten", d.ten == null ? "?" : d.ten);
            j.put("soLan", d.soLan);
            j.put("head", (int) d.head);
            j.put("body", (int) d.body);
            j.put("leg", (int) d.leg);
            root.put(String.valueOf(d.id), j);
        }
        try {
            new File(DUONG_DAN).getAbsoluteFile().getParentFile().mkdirs();
            try (FileWriter w = new FileWriter(DUONG_DAN, java.nio.charset.StandardCharsets.UTF_8)) {
                w.write(root.toJSONString());
            }
            ban = false;
            lanGhiCuoi = System.currentTimeMillis();
            return true;
        } catch (Exception ex) {
            Logger.error("Khong ghi duoc " + DUONG_DAN + ": " + ex + "\n");
            return false;
        }
    }

    //================================ đếm ================================
    /** Gọi mỗi lần một người nổ lò. */
    public static void ghiNhan(Player pl) {
        if (pl == null) {
            return;
        }
        try {
            baoDamDaNap();
            Dong d = BANG.computeIfAbsent(pl.id, id -> new Dong(id, pl.name));
            d.ten = pl.name == null ? d.ten : pl.name;
            d.soLan++;
            try {
                d.head = pl.getHead();
                d.body = pl.getBody();
                d.leg = pl.getLeg();
            } catch (Exception e) {
            }
            ban = true;
            if (System.currentTimeMillis() - lanGhiCuoi > NHIP_GHI_MS) {
                luu();
            }
        } catch (Exception e) {
            Logger.error("Loi ghi nhan no lo: " + e + "\n");
        }
    }

    /** Gọi lúc tắt server để không mất mấy lần nổ cuối. */
    public static void luuNeuCan() {
        if (ban) {
            luu();
        }
    }

    public static int soLanCua(Player pl) {
        baoDamDaNap();
        Dong d = pl == null ? null : BANG.get(pl.id);
        return d == null ? 0 : d.soLan;
    }

    //================================ hiện bảng ================================
    public static void mo(Player player) {
        if (player == null) {
            return;
        }
        baoDamDaNap();
        List<Dong> ds = new ArrayList<>(BANG.values());
        ds.removeIf(d -> d == null || d.soLan <= 0);
        if (ds.isEmpty()) {
            Service.gI().sendThongBao(player,
                    "Chưa ai nổ lò cả. Bảng vàng còn trống, cơ hội của ngươi đấy.");
            return;
        }
        ds.sort(Comparator.comparingInt((Dong d) -> d.soLan).reversed());
        List<Service.DongDanhSach> dong = new ArrayList<>();
        for (int i = 0; i < ds.size() && i < TOI_DA_DONG; i++) {
            Dong d = ds.get(i);
            dong.add(new Service.DongDanhSach((int) d.id, d.head, d.body, d.leg,
                    d.ten == null ? "?" : d.ten,
                    "Nổ lò " + d.soLan + " lần",
                    i == 0 ? "Đương kim Vua Nổ Lò" : "Hạng " + (i + 1)));
        }
        Service.gI().showListNhanVat(player, "Vua Nổ Lò (" + ds.size() + ")", dong);
    }
}
