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
import nro.models.boss.BossDropConfig;
import nro.models.boss.BossID;
import nro.models.map.ItemMap;
import nro.models.player.Player;
import nro.models.server.Manager;
import nro.models.services.Service;
import nro.models.tu_tien.LuyenDan;
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

        // --- Bộ Siêu Thần God + Lão Dê: một vòng quay 100 % (nhóm 1) + mấy dòng quay riêng.
        //     Ba dòng luyện đan để NHÓM 0 (quay riêng) chứ không nhét vào nhóm 1: nhóm 1 là
        //     một vòng 100 % đã chia đủ, thêm vào đó là mấy dòng cuối không bao giờ trúng.
        for (int id : new int[]{BossID.VEGETA_SIEU_THAN_GOD, BossID.GOKU_SIEU_THAN_GOD,
            BossID.LAO_DE_HOI_XUAN}) {
            List<MucRoi> ds = new ArrayList<>();
            ds.add(new MucRoi(new int[]{16, 17, 18}, 1, 1, 40, 1, "Ngọc Rồng 3-5 sao"));
            ds.add(new MucRoi(new int[]{702, 703, 704, 705, 706, 707, 708}, 1, 1, 40, 1, "Ngọc Rồng bí ngô 1-7 sao"));
            ds.add(new MucRoi(new int[]{1150, 1151, 1152, 1153}, 1, 1, 10, 1, "Bùa cấp 2"));
            ds.add(new MucRoi(new int[]{2264}, 1, 1, 5, 1, "Gậy Thông Thiên"));
            ds.add(new MucRoi(new int[]{2262}, 5, 5, 5, 1, "5 Đá Pháp Sư"));
            ds.add(new MucRoi(new int[]{2265}, 1, 1, 10, 0, "Còi Triệu Hồi Lão Dê"));
            ds.add(new MucRoi(new int[]{LuyenDan.DIA_HOA_TINH}, 2, 3, 100, 0, "Địa Hỏa Tinh"));
            ds.add(new MucRoi(new int[]{LuyenDan.DAN_PHUONG_TRUNG_CAP}, 1, 1, 15, 0, "Đan Phương Trung Cấp"));
            ds.add(new MucRoi(new int[]{LuyenDan.DAN_PHUONG_CAO_CAP}, 1, 1, 5, 0, "Đan Phương Cao Cấp"));
            m.put(id, ds);
        }

        // --- Hai boss Tây Du (patch 84): đường lấy nguyên liệu luyện đan cho người chơi yếu.
        //     Ít hơn và thưa hơn bộ Siêu Thần God, nhưng boss cũng dễ hơn hàng chục lần.
        List<MucRoi> batGioi = new ArrayList<>();
        batGioi.add(new MucRoi(new int[]{LuyenDan.DIA_HOA_TINH}, 1, 1, 40, 0, "Địa Hỏa Tinh"));
        batGioi.add(new MucRoi(new int[]{LuyenDan.DAN_PHUONG_SO_CAP}, 1, 1, 12, 0, "Đan Phương Sơ Cấp"));
        batGioi.add(new MucRoi(LuyenDan.LINH_THAO.clone(), 1, 2, 35, 0, "Linh thảo ngẫu nhiên"));
        m.put(BossID.TRU_BAT_GIOI, batGioi);

        List<MucRoi> ngoKhong = new ArrayList<>();
        ngoKhong.add(new MucRoi(new int[]{LuyenDan.DIA_HOA_TINH}, 1, 2, 60, 0, "Địa Hỏa Tinh"));
        ngoKhong.add(new MucRoi(new int[]{LuyenDan.DAN_PHUONG_SO_CAP}, 1, 1, 20, 0, "Đan Phương Sơ Cấp"));
        ngoKhong.add(new MucRoi(new int[]{LuyenDan.DAN_PHUONG_TRUNG_CAP}, 1, 1, 8, 0, "Đan Phương Trung Cấp"));
        ngoKhong.add(new MucRoi(LuyenDan.LINH_THAO.clone(), 2, 3, 50, 0, "Linh thảo ngẫu nhiên"));
        ngoKhong.add(new MucRoi(new int[]{16, 17, 18}, 1, 1, 30, 0, "Ngọc Rồng 3-5 sao"));
        m.put(BossID.TON_NGO_KHONG_GIA, ngoKhong);
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

    //================================ đồ rơi GỐC (chỉ để xem) ================================
    /** Trang bị rơi từ boss lớn — nhóm áo / quần / giày. */
    private static final int[] TB_AO_QUAN_GIAY = {230, 231, 232, 234, 235, 236, 238, 239, 240,
        242, 243, 244, 246, 247, 248, 250, 251, 252, 266, 267, 268, 270, 271, 272, 274, 275, 276};
    /** Trang bị rơi từ boss lớn — nhóm găng / rađa. */
    private static final int[] TB_GANG_RADA = {254, 255, 256, 258, 259, 260, 262, 263, 264,
        278, 279, 280};
    /** Ngọc Rồng 5 / 6 / 7 sao. */
    private static final int[] NR_567 = {18, 19, 20};

    /**
     * Khai báo <b>đồ rơi gốc</b> của các boss có sẵn — phần đang viết cứng trong {@code reward()}
     * của từng lớp boss.
     *
     * <p>Chỉ để <b>hiện lên cho người xem</b> (NPC Theo Dõi Boss và cpanel); {@link #roi} không
     * thả những dòng này, vì code của boss vẫn đang thả chúng. Thả nữa là rơi đôi.
     *
     * <p><b>Đụng vào {@code reward()} của boss nào thì nhớ sửa lại đây cho khớp</b> — đây là bản
     * chép tay, không đọc ngược được từ code.
     */
    private static List<MucRoi> khaiBaoGoc(int bossId) {
        List<MucRoi> ds = new ArrayList<>();
        switch (bossId) {
            // --- Bộ boss thường: 100 % vàng + 80 % một viên Ngọc Rồng 5/6/7 sao.
            case BossID.TIEU_DOI_TRUONG, BossID.KUKU, BossID.MAP_DAU_DINH, BossID.RAMBO,
                    BossID.FIDE, BossID.DR_KORE, BossID.ANDROID_14, BossID.KING_KONG,
                    BossID.XEN_BO_HUNG -> {
                ds.add(MucRoi.goc(new int[]{190}, 20000, 30000, 100, "Vàng"));
                ds.add(MucRoi.goc(NR_567, 1, 1, 80, "Một viên Ngọc Rồng"));
            }
            // --- Boss lớn: vàng + đồ Thần Linh + một món trang bị kèm chỉ số và sao pha lê.
            case BossID.SIEU_BO_HUNG -> {
                ds.add(MucRoi.goc(new int[]{190}, 20000, 30000, 100, "Vàng"));
                ds.add(MucRoi.thanLinh());
                ds.add(MucRoi.goc(TB_AO_QUAN_GIAY, 1, 1, 21, "Áo/quần/giày (30% × 70%), kèm chỉ số + sao pha lê"));
                ds.add(MucRoi.goc(TB_GANG_RADA, 1, 1, 9, "Găng/rađa (30% × 30%), kèm chỉ số + sao pha lê"));
            }
            case BossID.COOLER, BossID.BLACK_GOKU -> {
                ds.add(MucRoi.goc(new int[]{190}, 20000, 30000, 100, "Vàng"));
                ds.add(MucRoi.thanLinh());
                ds.add(MucRoi.goc(TB_AO_QUAN_GIAY, 1, 1, 3, "Áo/quần/giày (5% × 70%), kèm chỉ số + sao pha lê"));
                ds.add(MucRoi.goc(TB_GANG_RADA, 1, 1, 2, "Găng/rađa (5% × 30%), kèm chỉ số + sao pha lê"));
            }
            case BossID.CUMBER -> {
                ds.add(MucRoi.goc(new int[]{190}, 20000, 30000, 100, "Vàng"));
                ds.add(MucRoi.thanLinh());
                ds.add(MucRoi.goc(TB_AO_QUAN_GIAY, 1, 1, 3, "Áo/quần/giày (5% × 70%), kèm chỉ số + sao pha lê"));
                ds.add(MucRoi.goc(TB_GANG_RADA, 1, 1, 2, "Găng/rađa (5% × 30%), kèm chỉ số + sao pha lê"));
                ds.add(MucRoi.goc(new int[]{15, 16, 17, 18, 19, 20, 992}, 1, 3, 10, "Ngọc Rồng / Nhẫn thời không"));
            }
            case BossID.BABY -> {
                ds.add(MucRoi.goc(new int[]{190}, 20000, 30000, 100, "Vàng"));
                ds.add(MucRoi.thanLinh());
                ds.add(MucRoi.goc(new int[]{1785, 1786, 1788}, 1, 1, 1, "Cải trang kèm chỉ số"));
                ds.add(MucRoi.goc(TB_AO_QUAN_GIAY, 1, 1, 3, "Áo/quần/giày (5% × 70%), kèm chỉ số + sao pha lê"));
                ds.add(MucRoi.goc(TB_GANG_RADA, 1, 1, 2, "Găng/rađa (5% × 30%), kèm chỉ số + sao pha lê"));
            }
            // --- Tiểu đội sát thủ Namek / Bojack: rơi ngọc nhiều lần + cải trang + 2 viên Ngọc Rồng.
            case BossID.TIEU_DOI_TRUONG_NM -> {
                ds.add(MucRoi.goc(new int[]{77}, 1, 5, 100, "Ngọc, rơi thành nhiều đống"));
                ds.add(MucRoi.goc(new int[]{433}, 1, 1, 100, "Cải trang kèm chỉ số tiệm"));
                ds.add(MucRoi.goc(new int[]{19, 20}, 1, 1, 100, "Cả hai viên"));
            }
            case BossID.BOJACK -> {
                ds.add(MucRoi.goc(new int[]{77}, 5, 20, 100, "Ngọc, rơi thành nhiều đống"));
                ds.add(MucRoi.goc(new int[]{427}, 1, 1, 100, "Cải trang kèm chỉ số tiệm"));
                ds.add(MucRoi.goc(new int[]{19, 20}, 1, 1, 100, "Cả hai viên"));
            }
            case BossID.SUPER_BOJACK -> {
                ds.add(MucRoi.goc(new int[]{77}, 5, 15, 100, "Ngọc, rơi thành nhiều đống"));
                ds.add(MucRoi.goc(new int[]{428}, 1, 1, 100, "Cải trang kèm chỉ số tiệm"));
                ds.add(MucRoi.goc(new int[]{19, 20}, 1, 1, 100, "Cả hai viên"));
            }
            case BossID.GOLDEN_FRIEZA ->
                ds.add(MucRoi.goc(new int[]{629}, 1, 1, 100, "Cải trang Fide vàng kèm chỉ số"));
            case BossID.AN_TROM -> {
                ds.add(MucRoi.goc(new int[]{190}, 1, 1, 100, "Trả lại 80% số vàng vừa trộm, chia 5 đống"));
                ds.add(MucRoi.goc(new int[]{1591}, 1, 1, 5, "Hộp quà Goku Day"));
                ds.add(MucRoi.goc(new int[]{1594}, 1, 1, 5, "Hộp quà Goku Day"));
            }
            case BossID.O_DO1, BossID.SOI_HEC_QUYN1 -> {
                ds.add(MucRoi.goc(new int[]{1591}, 1, 1, 5, "Hộp quà Goku Day"));
                ds.add(MucRoi.goc(new int[]{1594}, 1, 1, 5, "Hộp quà Goku Day"));
            }
            case BossID.MAT_TROI ->
                ds.add(MucRoi.goc(new int[]{1562}, 1, 1, 50, "Mặt trời tí hon kèm chỉ số"));
            default -> {
            }
        }
        // --- Bộ Lốp Trưởng: đọc thẳng cpanel tab "Rơi đồ boss" nên không bao giờ lệch.
        if (bossId <= BossID.LOP_TRUONG && bossId > BossID.LOP_TRUONG - 8) {
            ds.add(MucRoi.goc(nro.models.boss.BossDropConfig.LT20_ID_BINH.ids(),
                    nro.models.boss.BossDropConfig.LT20_BINH_MIN.giaTri,
                    nro.models.boss.BossDropConfig.LT20_BINH_MAX.giaTri,
                    nro.models.boss.BossDropConfig.LT20_BINH.giaTri, "Đợt 20k — bình"));
            ds.add(MucRoi.goc(nro.models.boss.BossDropConfig.LT20_ID_BUA.ids(), 1, 1,
                    nro.models.boss.BossDropConfig.LT20_BUA.giaTri, "Đợt 20k — bùa"));
            ds.add(MucRoi.goc(nro.models.boss.BossDropConfig.LT20_ID_DA.ids(), 1, 1,
                    nro.models.boss.BossDropConfig.LT20_DA_BAO_VE.giaTri, "Đợt 20k — đá bảo vệ"));
            ds.add(MucRoi.goc(nro.models.boss.BossDropConfig.LT20_ID_SACH.ids(), 1, 1,
                    nro.models.boss.BossDropConfig.LT20_SACH_DE_TU.giaTri, "Đợt 20k — sách đệ tử"));
            ds.add(MucRoi.goc(nro.models.boss.BossDropConfig.LT20_ID_NGOC.ids(), 1, 1, 100,
                    "Đợt 20k — phần còn lại của vòng quay"));
            ds.add(MucRoi.goc(nro.models.boss.BossDropConfig.LT_ID_VANG.ids(),
                    nro.models.boss.BossDropConfig.LT_VANG_MIN.giaTri,
                    nro.models.boss.BossDropConfig.LT_VANG_MAX.giaTri, 100, "Đợt 2 tỷ — vàng"));
            ds.add(MucRoi.thanLinh());
            ds.add(MucRoi.goc(TB_AO_QUAN_GIAY, 1, 1,
                    nro.models.boss.BossDropConfig.LT_TRANG_BI.giaTri * 70 / 100, "Đợt 2 tỷ — áo/quần/giày"));
            ds.add(MucRoi.goc(TB_GANG_RADA, 1, 1,
                    nro.models.boss.BossDropConfig.LT_TRANG_BI.giaTri * 30 / 100, "Đợt 2 tỷ — găng/rađa"));
        }
        return ds;
    }

    /** Đồ rơi gốc của boss — chỉ để xem, {@link #roi} không thả. */
    public static List<MucRoi> goc(int bossId) {
        try {
            return khaiBaoGoc(bossId);
        } catch (Exception e) {
            return Collections.emptyList();
        }
    }

    /** Toàn bộ những gì hạ boss này CÓ THỂ rơi: đồ gốc + đồ thêm ở cpanel. */
    public static List<MucRoi> xem(int bossId) {
        return xem(bossId, 0);
    }

    /**
     * Bảng rơi để HIỆN cho người chơi, kèm cả luật rơi chung theo máu boss.
     *
     * @param mauMax máu tối đa của boss; để 0 nếu không biết thì bỏ qua luật chung
     */
    public static List<MucRoi> xem(int bossId, long mauMax) {
        List<MucRoi> rieng = cua(bossId);
        List<MucRoi> ra = new ArrayList<>(goc(bossId));
        ra.addAll(rieng);
        if (mauMax > 0) {
            ra.addAll(luatChung(mauMax, daKhaiBaoRieng(rieng)));
        }
        return ra;
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
        // Bảng riêng của con này (sửa được ở cpanel) CỘNG luật rơi chung theo máu boss.
        // Phải gộp chứ không "có bảng riêng thì thôi luật chung": gần như mọi boss cũ đều
        // KHÔNG có bảng riêng, thoát sớm ở đây là chúng chẳng rơi nguyên liệu luyện đan bao giờ.
        List<MucRoi> rieng = cua((int) boss.id);
        List<MucRoi> ds = new ArrayList<>(rieng);
        ds.addAll(luatChung(mauCua(boss), daKhaiBaoRieng(rieng) || boss.laLauLa()));
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

    //================================ luật rơi chung ================================
    /**
     * Dòng rơi <b>áp cho MỌI boss</b>, tính theo máu tối đa của con đó.
     *
     * <p>Lý do có hàm này: trước đây Địa Hỏa Tinh chỉ rơi ở năm con (hai Tây Du, bộ Siêu Thần
     * God, Lão Dê). Người chơi cày chay hạ được con boss nào khác cũng không tiến thêm được
     * bước nào trong tuyến luyện đan. Nay boss nào đủ máu cũng rơi, tỉ lệ tăng dần theo độ
     * trâu — chỉnh được hết ở cpanel tab "Rơi đồ boss".
     *
     * <p>Không áp cho: boss quá ít máu (Ăn Trộm 100 máu, Mặt Trời 500 máu ×20 bản — cày vô
     * hạn), <b>lâu la</b> đi kèm boss khác, và con nào đã có dòng Địa Hỏa Tinh viết tay trong
     * bảng riêng (dòng viết tay luôn thắng, để cpanel chỉnh được từng con).
     *
     * @param daCoRieng true thì trả về danh sách rỗng
     */
    private static List<MucRoi> luatChung(long mauMax, boolean daCoRieng) {
        List<MucRoi> ds = new ArrayList<>();
        if (daCoRieng || mauMax < Math.max(1, BossDropConfig.LD_MAU_MIN.giaTri)) {
            return ds;
        }
        int tiLe;
        int slMax = 1;
        if (mauMax >= 200_000_000L) {
            tiLe = BossDropConfig.LD_TI_LE_KHUNG.giaTri;
            slMax = 2;
        } else if (mauMax >= 50_000_000L) {
            tiLe = BossDropConfig.LD_TI_LE_LON.giaTri;
        } else if (mauMax >= 5_000_000L) {
            tiLe = BossDropConfig.LD_TI_LE_VUA.giaTri;
        } else {
            tiLe = BossDropConfig.LD_TI_LE_NHO.giaTri;
        }
        if (tiLe > 0) {
            ds.add(new MucRoi(new int[]{LuyenDan.DIA_HOA_TINH}, 1, slMax, tiLe, 0,
                    "Địa Hỏa Tinh (luật chung, theo máu boss)"));
        }
        int tiLeDanPhuong = BossDropConfig.LD_DAN_PHUONG.giaTri;
        if (mauMax >= 20_000_000L && tiLeDanPhuong > 0) {
            ds.add(new MucRoi(new int[]{LuyenDan.DAN_PHUONG_SO_CAP}, 1, 1, tiLeDanPhuong, 0,
                    "Đan Phương Sơ Cấp (luật chung)"));
        }
        return ds;
    }

    /** Bảng riêng của boss đã có dòng Địa Hỏa Tinh viết tay chưa. */
    private static boolean daKhaiBaoRieng(List<MucRoi> rieng) {
        for (MucRoi m : rieng) {
            if (m.ids == null) {
                continue;
            }
            for (int id : m.ids) {
                if (id == LuyenDan.DIA_HOA_TINH) {
                    return true;
                }
            }
        }
        return false;
    }

    private static long mauCua(Boss boss) {
        return boss.nPoint == null ? 0 : boss.nPoint.hpMax;
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
