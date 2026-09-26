package nro.models.boss.drop;

import java.util.ArrayList;
import java.util.List;
import nro.models.server.Manager;

/**
 * Một dòng trong bảng rơi đồ của boss.
 *
 * <p>{@link #ids} có thể là nhiều vật phẩm: lúc trúng thì bốc ngẫu nhiên một cái trong đó
 * (dùng cho kiểu "Ngọc Rồng 3–5 sao" — một dòng, ba id).
 *
 * <p>{@link #nhom} quyết định cách quay:
 * <ul>
 *   <li><b>0</b> — quay riêng: tung {@link #tiLe} % một lần, trúng thì rơi, không ảnh hưởng
 *       dòng nào khác.</li>
 *   <li><b>&gt; 0</b> — quay chung: tất cả dòng cùng số nhóm nằm trên MỘT vòng quay 100 %,
 *       {@link #tiLe} là phần của vòng đó. Tổng các dòng trong nhóm nên đúng 100; thiếu thì
 *       phần còn lại là "không rơi gì", thừa thì mấy dòng cuối không bao giờ trúng.</li>
 * </ul>
 */
public final class MucRoi {

    /** Danh sách id vật phẩm; trúng thì bốc ngẫu nhiên một cái. */
    public int[] ids;
    public int slMin = 1;
    public int slMax = 1;
    public int tiLe = 100;
    public int nhom;
    public String ghiChu = "";

    /**
     * true = <b>đồ rơi gốc</b>, tức phần viết cứng trong {@code reward()} của lớp boss.
     * Dòng loại này CHỈ ĐỂ XEM: {@code BangRoiBoss.roi} không thả nó (thả nữa là rơi đôi),
     * cpanel không cho sửa, và cũng không ghi ra file.
     */
    public boolean goc;

    /** 0 = vật phẩm thường · 1 = đồ Thần Linh ngẫu nhiên (tỉ lệ tính theo máu boss). */
    public int loai;

    public MucRoi() {
    }

    public MucRoi(int[] ids, int slMin, int slMax, int tiLe, int nhom, String ghiChu) {
        this.ids = ids;
        this.slMin = slMin;
        this.slMax = slMax;
        this.tiLe = tiLe;
        this.nhom = nhom;
        this.ghiChu = ghiChu;
    }

    /** Dòng "đồ rơi gốc" — chỉ để xem, không thả. */
    public static MucRoi goc(int[] ids, int slMin, int slMax, int tiLe, String ghiChu) {
        MucRoi m = new MucRoi(ids, slMin, slMax, tiLe, 0, ghiChu);
        m.goc = true;
        return m;
    }

    /** Dòng "đồ Thần Linh ngẫu nhiên" — chỉ để xem, tỉ lệ tính theo máu boss. */
    public static MucRoi thanLinh() {
        MucRoi m = new MucRoi(new int[0], 1, 1, 0, 0, "Đồ Thần Linh ngẫu nhiên");
        m.goc = true;
        m.loai = 1;
        return m;
    }

    /** Chữ hiện cho tỉ lệ: đồ Thần Linh thì tính theo máu nên ghi khoảng. */
    public String chuoiTiLe() {
        if (loai == 1) {
            return "theo máu boss (" + nro.models.boss.BossDropRate.MIN_PERCENT + "-"
                    + nro.models.boss.BossDropRate.MAX_PERCENT + "%)";
        }
        return tiLe + "%";
    }

    /** "16,17,18" */
    public String chuoiIds() {
        if (ids == null || ids.length == 0) {
            return "";
        }
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < ids.length; i++) {
            if (i > 0) {
                sb.append(",");
            }
            sb.append(ids[i]);
        }
        return sb.toString();
    }

    /** Đọc "16, 17,18" thành mảng; bỏ qua phần không phải số. */
    public static int[] docIds(String chuoi) {
        List<Integer> ds = new ArrayList<>();
        if (chuoi != null) {
            for (String s : chuoi.split(",")) {
                try {
                    ds.add(Integer.parseInt(s.trim()));
                } catch (NumberFormatException e) {
                }
            }
        }
        int[] ra = new int[ds.size()];
        for (int i = 0; i < ra.length; i++) {
            ra[i] = ds.get(i);
        }
        return ra;
    }

    /** "Ngọc Rồng 3 sao / Ngọc Rồng 4 sao / Ngọc Rồng 5 sao" — để hiện cho người đọc. */
    public String tenVatPham() {
        if (loai == 1) {
            return "Đồ Thần Linh ngẫu nhiên";
        }
        if (ids == null || ids.length == 0) {
            return "?";
        }
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < ids.length; i++) {
            if (i > 0) {
                sb.append(" / ");
            }
            sb.append(ten(ids[i]));
        }
        return sb.toString();
    }

    /** Tên một vật phẩm, tra thẳng bảng đang chạy; id lạ thì trả về "#id". */
    public static String ten(int id) {
        try {
            if (id >= 0 && id < Manager.ITEM_TEMPLATES.size()) {
                String t = Manager.ITEM_TEMPLATES.get(id).name;
                if (t != null && !t.isEmpty()) {
                    return t;
                }
            }
        } catch (Exception e) {
        }
        return "#" + id;
    }

    /** "x5" hoặc "x5-10", rỗng nếu chỉ một cái. */
    public String chuoiSoLuong() {
        if (slMax <= 1 && slMin <= 1) {
            return "";
        }
        return slMin == slMax ? "x" + slMin : "x" + slMin + "-" + slMax;
    }
}
