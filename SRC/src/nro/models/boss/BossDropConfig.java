package nro.models.boss;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Properties;

import nro.models.utils.Logger;

/**
 * Cấu hình rơi đồ của boss + đồ kích hoạt + bộ boss Lốp Trưởng, sửa được lúc server
 * đang chạy (tab "Rơi đồ boss" của cpanel) và lưu vào {@code data/bossdrop.properties}.
 *
 * <p>Giá trị mặc định đúng bằng số cũ nằm rải rác trong code, nên không sửa gì thì
 * mọi thứ chạy y như trước.
 */
public final class BossDropConfig {

    /** Một ô chỉnh được: khoá lưu file, nhãn hiện trên cpanel, giá trị, và giải thích. */
    public static final class Muc {

        public final String key;
        public final String nhan;
        public final String chuThich;
        public volatile int giaTri;
        public final int macDinh;

        Muc(String key, String nhan, int giaTri, String chuThich) {
            this.key = key;
            this.nhan = nhan;
            this.giaTri = giaTri;
            this.macDinh = giaTri;
            this.chuThich = chuThich;
        }
    }

    private static final Map<String, Muc> TAT_CA = new LinkedHashMap<>();

    private static Muc them(String key, String nhan, int giaTri, String chuThich) {
        Muc m = new Muc(key, nhan, giaTri, chuThich);
        TAT_CA.put(key, m);
        return m;
    }

    //================== Đồ Thần Linh rơi từ mọi boss ==================
    public static final Muc THAN_LINH_MIN = them("than_linh_min",
            "Đồ Thần Linh — sàn (%)", 1, "Boss yếu nhất vẫn có ngần này % rơi");
    public static final Muc THAN_LINH_MAX = them("than_linh_max",
            "Đồ Thần Linh — trần (%)", 5, "Boss trâu nhất cũng chỉ tới ngần này %");

    //================== Đồ kích hoạt rơi từ quái ==================
    public static final Muc KICH_HOAT_RATE = them("kich_hoat_rate",
            "Đồ kích hoạt — rơi", 6, "Rơi 'rate' lần trên mỗi 'per' con quái");
    public static final Muc KICH_HOAT_PER = them("kich_hoat_per",
            "Đồ kích hoạt — trên mỗi", 99990, "Mặc định 6/99990 ≈ đủ 1 set sau ~15 ngày cày");

    //================== Lốp Trưởng — đợt 2 tỷ máu ==================
    public static final Muc LT_VANG_MIN = them("lt_vang_min",
            "Lốp Trưởng 2 tỷ — vàng tối thiểu", 20000, "Vàng rơi mỗi lần hạ");
    public static final Muc LT_VANG_MAX = them("lt_vang_max",
            "Lốp Trưởng 2 tỷ — vàng tối đa", 30000, "");
    public static final Muc LT_TRANG_BI = them("lt_trang_bi",
            "Lốp Trưởng 2 tỷ — rơi trang bị (%)", 5, "Cơ hội rơi thêm một món trang bị");

    //================== Lốp Trưởng — đợt 20k máu ==================
    public static final Muc LT20_BINH = them("lt20_binh",
            "Lốp Trưởng 20k — bình hỗ trợ (%)", 50, "Cuồng nộ 2 / Bổ huyết 2 / Bổ khí 2");
    public static final Muc LT20_BINH_MIN = them("lt20_binh_min",
            "Lốp Trưởng 20k — bình, số lượng ít nhất", 1, "");
    public static final Muc LT20_BINH_MAX = them("lt20_binh_max",
            "Lốp Trưởng 20k — bình, số lượng nhiều nhất", 3, "");
    public static final Muc LT20_BUA = them("lt20_bua",
            "Lốp Trưởng 20k — bùa x2 đệ tử (%)", 20, "");
    public static final Muc LT20_DA_BAO_VE = them("lt20_da_bao_ve",
            "Lốp Trưởng 20k — đá bảo vệ (%)", 10, "");
    public static final Muc LT20_SACH_DE_TU = them("lt20_sach_de_tu",
            "Lốp Trưởng 20k — sách kỹ năng đệ tử (%)", 10, "Nâng kỹ năng 2 / 3 / 4 / 5");
    public static final Muc LT20_NGOC_RONG = them("lt20_ngoc_rong",
            "Lốp Trưởng 20k — ngọc rồng 3 sao (%)", 10, "Phần còn lại của bảng");

    //================== Lốp Trưởng — nhịp ra boss ==================
    public static final Muc LT_CHO_RA_LAI = them("lt_cho_ra_lai",
            "Lốp Trưởng — phút chờ ra lại", 15, "Tính từ lúc kết thúc lượt trước");
    public static final Muc LT_TU_DI = them("lt_tu_di",
            "Lốp Trưởng — phút vắng người thì đi", 30, "");
    public static final Muc LT_TRAN_DAME = them("lt_tran_dame",
            "Lốp Trưởng 20k — trần sát thương người chơi", 100, "Mỗi đòn không vượt quá số này");

    //================== Vật phẩm rơi (sửa được id) ==================
    /** Ô nhập dạng chữ: danh sách id vật phẩm, cách nhau bằng dấu phẩy. */
    public static final class MucChu {

        public final String key;
        public final String nhan;
        public final String chuThich;
        public volatile String giaTri;
        public final String macDinh;

        MucChu(String key, String nhan, String giaTri, String chuThich) {
            this.key = key;
            this.nhan = nhan;
            this.giaTri = giaTri;
            this.macDinh = giaTri;
            this.chuThich = chuThich;
        }

        /** Đọc ra mảng id; ô trống hoặc sai định dạng thì trả về mặc định. */
        public int[] ids() {
            int[] r = doc(giaTri);
            return r.length > 0 ? r : doc(macDinh);
        }

        private static int[] doc(String v) {
            if (v == null) {
                return new int[0];
            }
            String[] phan = v.split(",");
            int[] r = new int[phan.length];
            int n = 0;
            for (String x : phan) {
                try {
                    r[n++] = Integer.parseInt(x.trim());
                } catch (NumberFormatException ex) {
                    return new int[0];
                }
            }
            return n == r.length ? r : new int[0];
        }
    }

    private static final Map<String, MucChu> TAT_CA_CHU = new LinkedHashMap<>();

    private static MucChu themChu(String key, String nhan, String giaTri, String chuThich) {
        MucChu m = new MucChu(key, nhan, giaTri, chuThich);
        TAT_CA_CHU.put(key, m);
        return m;
    }

    public static final MucChu LT20_ID_BINH = themChu("lt20_id_binh",
            "Lốp Trưởng 20k — id bình hỗ trợ", "1150,1152,1151", "Cuồng nộ 2, Bổ huyết 2, Bổ khí 2");
    public static final MucChu LT20_ID_BUA = themChu("lt20_id_bua",
            "Lốp Trưởng 20k — id bùa", "1628", "Bùa x2 tn,sm đệ tử");
    public static final MucChu LT20_ID_DA = themChu("lt20_id_da",
            "Lốp Trưởng 20k — id đá bảo vệ", "987", "");
    public static final MucChu LT20_ID_SACH = themChu("lt20_id_sach",
            "Lốp Trưởng 20k — id sách đệ tử", "403,404,759,2123", "Nâng kỹ năng 2/3/4/5");
    public static final MucChu LT20_ID_NGOC = themChu("lt20_id_ngoc",
            "Lốp Trưởng 20k — id ngọc rồng", "16", "Ngọc Rồng 3 sao");
    public static final MucChu LT_ID_VANG = themChu("lt_id_vang",
            "Lốp Trưởng 2 tỷ — id vàng", "190", "Vật phẩm vàng thỏi rơi ra");

    public static MucChu[] danhSachChu() {
        return TAT_CA_CHU.values().toArray(new MucChu[0]);
    }

    public static Muc[] danhSach() {
        return TAT_CA.values().toArray(new Muc[0]);
    }

    private static final String FILE = "data/bossdrop.properties";

    private BossDropConfig() {
    }

    /** Gọi một lần lúc khởi động. Thiếu file thì giữ mặc định. */
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
        for (MucChu m : TAT_CA_CHU.values()) {
            String v = p.getProperty(m.key);
            if (v != null && !v.trim().isEmpty()) {
                m.giaTri = v.trim();
            }
        }
        for (Muc m : TAT_CA.values()) {
            String v = p.getProperty(m.key);
            if (v == null) {
                continue;
            }
            try {
                m.giaTri = Integer.parseInt(v.trim());
            } catch (NumberFormatException ex) {
                Logger.error(FILE + ": giá trị sai ở " + m.key + " = " + v + "\n");
            }
        }
        Logger.success("Đã nạp cấu hình rơi đồ boss từ " + FILE + "\n");
    }

    /** Ghi lại file để giữ sau khi khởi động lại. */
    public static void save() {
        Properties p = new Properties();
        for (Muc m : TAT_CA.values()) {
            p.setProperty(m.key, String.valueOf(m.giaTri));
        }
        for (MucChu m : TAT_CA_CHU.values()) {
            p.setProperty(m.key, m.giaTri);
        }
        File f = new File(FILE);
        if (f.getParentFile() != null) {
            f.getParentFile().mkdirs();
        }
        try (FileOutputStream out = new FileOutputStream(f)) {
            p.store(out, "Ti le roi do boss / do kich hoat / bo boss Lop Truong");
        } catch (Exception e) {
            Logger.error("Không ghi được " + FILE + ": " + e + "\n");
        }
    }
}
