package nro.models.boss.drop;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.concurrent.ConcurrentHashMap;
import nro.models.boss.Boss;
import nro.models.boss.BossID;
import nro.models.map.ItemMap;
import nro.models.player.Player;
import nro.models.server.Manager;
import nro.models.services.Service;
import nro.models.utils.Logger;
import nro.models.utils.Util;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.JSONValue;

/**
 * Bảng rơi đồ của boss, sửa được từ cpanel và xem được ở NPC "Theo Dõi Boss".
 *
 * <p>Mỗi boss (tra theo {@code boss.id}, tức hằng số trong {@link BossID}) có một danh sách
 * {@link MucRoi}. Mỗi lần hạ boss, {@link #roi(Boss, Player)} chạy qua danh sách đó và thả đồ
 * ra đất, gán cho người hạ.
 *
 * <p><b>Quan hệ với phần rơi đồ viết cứng trong từng lớp boss:</b> bảng này là phần <b>THÊM</b>,
 * chạy sau {@code reward()} của boss. Những boss cũ vẫn giữ nguyên đồ rơi viết trong code của
 * chúng; thêm dòng ở đây là boss đó rơi thêm, không mất gì. Riêng năm con mới (hai Fu, hai Siêu
 * Thần God, Lão Dê) <b>không</b> viết cứng gì cả — toàn bộ đồ rơi của chúng nằm ở bảng này nên
 * sửa từ cpanel là đổi được hết.
 *
 * <p>Sửa xong bấm "áp dụng" là có hiệu lực ngay; bấm "lưu" thì ghi {@code data/bossdrop_table.json}
 * để khởi động lại vẫn còn. File không có thì dùng bảng mặc định ở {@link #macDinh()}.
 */
public final class BangRoiBoss {

    private static final String DUONG_DAN = "data/bossdrop_table.json";

    private static final Map<Integer, List<MucRoi>> BANG = new ConcurrentHashMap<>();
    private static volatile boolean daNap;

    private BangRoiBoss() {
    }

    //================================ bảng mặc định ================================
    /**
     * Bảng gốc của năm con boss mới. Viết ở đây chứ không rải trong từng lớp boss để cpanel
     * sửa được và NPC đọc được.
     */
    private static Map<Integer, List<MucRoi>> macDinh() {
        Map<Integer, List<MucRoi>> m = new LinkedHashMap<>();

        // --- Hai boss Fu (Nam Kamê): rơi chắc chắn, không quay.
        for (int id : new int[]{BossID.FU, BossID.FU_HOP_THE}) {
            List<MucRoi> ds = new ArrayList<>();
            ds.add(new MucRoi(new int[]{2262}, 5, 5, 100, 0, "Đá Pháp Sư"));
            ds.add(new MucRoi(new int[]{2263}, 1, 1, 100, 0, "Đá Tẩy Pháp Sư"));
            m.put(id, ds);
        }

        // --- Bộ Siêu Thần God + Lão Dê: một vòng quay 100 % (nhóm 1) + còi quay riêng.
        for (int id : new int[]{BossID.VEGETA_SIEU_THAN_GOD, BossID.GOKU_SIEU_THAN_GOD,
            BossID.LAO_DE_HOI_XUAN}) {
            List<MucRoi> ds = new ArrayList<>();
            ds.add(new MucRoi(new int[]{16, 17, 18}, 1, 1, 40, 1, "Ngọc Rồng 3-5 sao"));
            ds.add(new MucRoi(new int[]{702, 703, 704, 705, 706, 707, 708}, 1, 1, 40, 1, "Ngọc Rồng bí ngô 1-7 sao"));
            ds.add(new MucRoi(new int[]{1150, 1151, 1152, 1153}, 1, 1, 10, 1, "Bùa cấp 2"));
            ds.add(new MucRoi(new int[]{2264}, 1, 1, 5, 1, "Gậy Thông Thiên"));
            ds.add(new MucRoi(new int[]{2262}, 5, 5, 5, 1, "5 Đá Pháp Sư"));
            ds.add(new MucRoi(new int[]{2265}, 1, 1, 10, 0, "Còi Triệu Hồi Lão Dê"));
            m.put(id, ds);
        }
        return m;
    }

    //================================ nạp / lưu ================================
    public static synchronized void baoDamDaNap() {
        if (daNap) {
            return;
        }
        daNap = true;
        BANG.clear();
        BANG.putAll(macDinh());
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
                int bossId;
                try {
                    bossId = Integer.parseInt(String.valueOf(k).trim());
                } catch (NumberFormatException e) {
                    continue;
                }
                Object v = root.get(k);
                if (!(v instanceof JSONArray)) {
                    continue;
                }
                List<MucRoi> ds = new ArrayList<>();
                for (Object e : (JSONArray) v) {
                    if (!(e instanceof JSONObject)) {
                        continue;
                    }
                    JSONObject j = (JSONObject) e;
                    MucRoi mr = new MucRoi();
                    mr.ids = MucRoi.docIds(String.valueOf(j.get("ids")));
                    mr.slMin = so(j.get("slMin"), 1);
                    mr.slMax = so(j.get("slMax"), 1);
                    mr.tiLe = so(j.get("tiLe"), 100);
                    mr.nhom = so(j.get("nhom"), 0);
                    mr.ghiChu = j.get("ghiChu") == null ? "" : String.valueOf(j.get("ghiChu"));
                    if (mr.ids.length > 0) {
                        ds.add(mr);
                    }
                }
                // File ghi đè hẳn bảng mặc định của boss đó (kể cả khi danh sách rỗng,
                // nghĩa là quản trị đã cố ý xoá sạch đồ rơi của con này).
                BANG.put(bossId, ds);
            }
            Logger.success("Bang roi do boss: nap " + BANG.size() + " con tu " + DUONG_DAN + "\n");
        } catch (Exception e) {
            Logger.error("Khong doc duoc " + DUONG_DAN + ": " + e + "\n");
        }
    }

    private static int so(Object o, int mac) {
        if (o == null) {
            return mac;
        }
        try {
            return (int) Double.parseDouble(String.valueOf(o).trim());
        } catch (NumberFormatException e) {
            return mac;
        }
    }

    /** Ghi toàn bộ bảng đang chạy ra file. */
    @SuppressWarnings("unchecked")
    public static synchronized boolean luu() {
        baoDamDaNap();
        JSONObject root = new JSONObject();
        for (Map.Entry<Integer, List<MucRoi>> e : new TreeMap<>(BANG).entrySet()) {
            JSONArray arr = new JSONArray();
            for (MucRoi m : e.getValue()) {
                JSONObject j = new JSONObject();
                j.put("ids", m.chuoiIds());
                j.put("slMin", m.slMin);
                j.put("slMax", m.slMax);
                j.put("tiLe", m.tiLe);
                j.put("nhom", m.nhom);
                j.put("ghiChu", m.ghiChu == null ? "" : m.ghiChu);
                arr.add(j);
            }
            root.put(String.valueOf(e.getKey()), arr);
        }
        try {
            new File(DUONG_DAN).getAbsoluteFile().getParentFile().mkdirs();
            try (FileWriter w = new FileWriter(DUONG_DAN, java.nio.charset.StandardCharsets.UTF_8)) {
                w.write(root.toJSONString());
            }
            return true;
        } catch (Exception ex) {
            Logger.error("Khong ghi duoc " + DUONG_DAN + ": " + ex + "\n");
            return false;
        }
    }

    /** Xoá file và quay về bảng mặc định trong code. */
    public static synchronized void veMacDinh() {
        BANG.clear();
        BANG.putAll(macDinh());
        new File(DUONG_DAN).delete();
    }

    //================================ đọc / sửa ================================
    /** Bảng của một boss; trả về bản sao chỉ để đọc, sửa thì dùng {@link #dat}. */
    public static List<MucRoi> cua(int bossId) {
        baoDamDaNap();
        List<MucRoi> ds = BANG.get(bossId);
        return ds == null ? Collections.emptyList() : Collections.unmodifiableList(ds);
    }

    /** Thay cả bảng của một boss. Có hiệu lực ngay lần hạ boss kế tiếp. */
    public static void dat(int bossId, List<MucRoi> ds) {
        baoDamDaNap();
        BANG.put(bossId, new ArrayList<>(ds));
    }

    /** Có khai báo dòng nào cho boss này không. */
    public static boolean co(int bossId) {
        return !cua(bossId).isEmpty();
    }

    //================================ rơi đồ ================================
    /**
     * Thả đồ theo bảng. Gọi ở {@link Boss#die(Player)}, ngay sau {@code reward()} của boss.
     */
    public static void roi(Boss boss, Player plKill) {
        if (boss == null || plKill == null || boss.zone == null || boss.zone.map == null) {
            return;     // boss vừa rời map ngay lúc chết — không có chỗ để rơi đồ
        }
        List<MucRoi> ds = cua((int) boss.id);
        if (ds.isEmpty()) {
            return;
        }
        int lech = 0;
        // Nhóm 0: mỗi dòng một lần tung riêng.
        for (MucRoi m : ds) {
            if (m.nhom == 0 && Util.isTrue(m.tiLe, 100)) {
                tha(boss, plKill, m, lech);
                lech += 20;
            }
        }
        // Nhóm > 0: mỗi nhóm một vòng quay 100.
        for (int nhom : cacNhom(ds)) {
            int r = Util.nextInt(0, 99);
            int cong = 0;
            for (MucRoi m : ds) {
                if (m.nhom != nhom) {
                    continue;
                }
                cong += m.tiLe;
                if (r < cong) {
                    tha(boss, plKill, m, lech);
                    lech += 20;
                    break;
                }
            }
        }
    }

    private static List<Integer> cacNhom(List<MucRoi> ds) {
        List<Integer> ra = new ArrayList<>();
        for (MucRoi m : ds) {
            if (m.nhom > 0 && !ra.contains(m.nhom)) {
                ra.add(m.nhom);
            }
        }
        return ra;
    }

    private static void tha(Boss boss, Player plKill, MucRoi m, int lech) {
        if (m.ids == null || m.ids.length == 0) {
            return;
        }
        int id = m.ids[Util.nextInt(0, m.ids.length - 1)];
        if (id < 0 || id >= Manager.ITEM_TEMPLATES.size()) {
            // ITEM_TEMPLATES tra theo chỉ số: id vượt cỡ (chưa chạy patch) là văng
            // IndexOutOfBounds ngay trong luồng boss. Thà không rơi gì.
            Logger.error("Bang roi boss " + boss.name + ": vat pham " + id
                    + " khong co trong item_template, bo qua\n");
            return;
        }
        int sl = m.slMax > m.slMin ? Util.nextInt(m.slMin, m.slMax) : Math.max(1, m.slMin);
        try {
            int x = boss.location.x + lech;
            int y = boss.zone.map.yPhysicInTop(boss.location.x, boss.location.y - 24);
            Service.gI().dropItemMap(boss.zone, new ItemMap(boss.zone, id, sl, x, y, plKill.id));
        } catch (Exception e) {
            Logger.error("Bang roi boss " + boss.name + ": loi tha vat pham " + id + ": " + e + "\n");
        }
    }
}
