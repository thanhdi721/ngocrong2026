package nro.models.services;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import nro.models.data.LocalManager;
import nro.models.data.LocalResultSet;
import nro.models.player.Player;
import nro.models.server.Manager;
import nro.models.server.ServerNotify;
import nro.models.utils.Logger;

/**
 * Bảng vàng của Bà Mối — hai bảng xếp hạng của {@link ThongDitService}:
 *
 * <ul>
 *   <li><b>ĐẠI SƯ CHÍ TÔN</b> — thông người khác nhiều nhất.</li>
 *   <li><b>CHIẾN BINH QUẢ CẢM</b> — bị thông nhiều nhất.</li>
 * </ul>
 *
 * <p>Số liệu nạp <b>một lần</b> từ cột {@code player.thong_dit} lúc ai đó mở bảng lần đầu,
 * sau đó chỉ {@link #capNhat(Player)} cập nhật — vì đây là con đường DUY NHẤT hai con số ấy
 * thay đổi. Nhờ vậy không phải hỏi lại cơ sở dữ liệu lần nào nữa, và số của người đang online
 * luôn đúng (cột trong CSDL chỉ được ghi theo nhịp lưu định kỳ nên đọc lại sẽ ra số cũ).
 *
 * <p>Ai soán ngôi đầu bảng thì server loa cho cả thế giới biết.
 */
public class BangVangThongDit {

    /** Số dòng hiện trong một bảng. */
    public static final int TOP = 10;

    /** Một dòng của bảng. */
    public static class Dong {

        public final long id;
        public String ten;
        public int thong;
        public int bi;

        Dong(long id, String ten, int thong, int bi) {
            this.id = id;
            this.ten = ten;
            this.thong = thong;
            this.bi = bi;
        }

        public int so(boolean theoThong) {
            return theoThong ? thong : bi;
        }
    }

    private static BangVangThongDit instance;

    public static BangVangThongDit gI() {
        if (instance == null) {
            instance = new BangVangThongDit();
        }
        return instance;
    }

    private final Map<Long, Dong> bang = new ConcurrentHashMap<>();
    private volatile boolean daNap;
    /** Người đang giữ ngôi đầu mỗi bảng, để biết lúc nào có kẻ soán ngôi. */
    private volatile long idDauThong = -1;
    private volatile long idDauBi = -1;

    //================================ nạp dữ liệu ================================
    private synchronized void baoDamDaNap() {
        if (daNap) {
            return;
        }
        daNap = true;   // đặt trước: có hỏng CSDL thì cũng không thử lại mỗi lần bấm menu
        if (!Manager.HAS_THONG_DIT) {
            return;
        }
        try {
            LocalResultSet rs = LocalManager.executeQuery(
                    "select id, name, thong_dit from player"
                    + " where thong_dit is not null and thong_dit <> '0|0'");
            while (rs.next()) {
                long id = rs.getLong("id");
                String ten = rs.getString("name");
                int[] so = tach(rs.getString("thong_dit"));
                if (so[0] > 0 || so[1] > 0) {
                    bang.put(id, new Dong(id, ten == null ? "?" : ten, so[0], so[1]));
                }
            }
            rs.dispose();
            Logger.success("Bang vang thong dit: nap " + bang.size() + " dong\n");
        } catch (Exception e) {
            Logger.error("Khong nap duoc bang vang thong dit: " + e + "\n");
        }
        idDauThong = idDangDau(true);
        idDauBi = idDangDau(false);
    }

    /** Đọc "a|b"; hỏng thì trả {0, 0}. Kẹp trong khoảng hợp lệ như {@link ThongDitService}. */
    private static int[] tach(String chuoi) {
        if (chuoi == null || !chuoi.contains("|")) {
            return new int[]{0, 0};
        }
        try {
            String[] sp = chuoi.split("\\|");
            int a = Math.max(0, Math.min(Integer.parseInt(sp[0].trim()), ThongDitService.TOI_DA_THONG));
            int b = Math.max(0, Math.min(Integer.parseInt(sp[1].trim()), ThongDitService.TOI_DA_BI_THONG));
            return new int[]{a, b};
        } catch (Exception e) {
            return new int[]{0, 0};
        }
    }

    //================================ cập nhật ================================
    /** Gọi sau mỗi lần thông thành công, cho CẢ hai người. */
    public void capNhat(Player pl) {
        if (pl == null || !pl.isPl()) {
            return;
        }
        baoDamDaNap();
        Dong d = bang.get(pl.id);
        if (d == null) {
            d = new Dong(pl.id, pl.name, pl.soLanThong, pl.soLanBiThong);
            bang.put(pl.id, d);
        } else {
            d.ten = pl.name;        // người chơi có thể đã đổi tên
            d.thong = pl.soLanThong;
            d.bi = pl.soLanBiThong;
        }
        kiemTraSoanNgoi(d, true);
        kiemTraSoanNgoi(d, false);
    }

    private long idDangDau(boolean theoThong) {
        Dong nhat = null;
        for (Dong d : bang.values()) {
            if (d.so(theoThong) > 0 && (nhat == null || d.so(theoThong) > nhat.so(theoThong))) {
                nhat = d;
            }
        }
        return nhat == null ? -1 : nhat.id;
    }

    /** Vượt được người đang đứng đầu thì loa cả server. Bằng điểm thì người cũ giữ ngôi. */
    private void kiemTraSoanNgoi(Dong d, boolean theoThong) {
        long idCu = theoThong ? idDauThong : idDauBi;
        if (d.id == idCu || d.so(theoThong) <= 0) {
            return;
        }
        Dong cu = idCu >= 0 ? bang.get(idCu) : null;
        int soCu = cu == null ? 0 : cu.so(theoThong);
        if (d.so(theoThong) <= soCu) {
            return;
        }
        if (theoThong) {
            idDauThong = d.id;
        } else {
            idDauBi = d.id;
        }
        String loa;
        if (theoThong) {
            loa = cu == null
                    ? d.ten + " là người đầu tiên leo lên bảng vàng ĐẠI SƯ CHÍ TÔN. Bà Mối rưng rưng."
                    : d.ten + " vừa vượt mặt " + cu.ten + ", chiếm ngôi ĐẠI SƯ CHÍ TÔN với "
                    + d.so(true) + " lần. Cả ba hành tinh nín thở.";
        } else {
            loa = cu == null
                    ? d.ten + " mở hàng bảng CHIẾN BINH QUẢ CẢM. Một sự hy sinh thầm lặng."
                    : d.ten + " soán ngôi CHIẾN BINH QUẢ CẢM của " + cu.ten + " sau "
                    + d.so(false) + " lần. Xin nghiêng mình.";
        }
        ServerNotify.gI().notify(loa);
    }

    //================================ đọc bảng ================================
    /** {@value #TOP} người đứng đầu bảng, nhiều nhất trước. */
    public List<Dong> top(boolean theoThong) {
        baoDamDaNap();
        List<Dong> ds = new ArrayList<>();
        for (Dong d : bang.values()) {
            if (d.so(theoThong) > 0) {
                ds.add(d);
            }
        }
        ds.sort(Comparator.<Dong>comparingInt(x -> x.so(theoThong)).reversed()
                .thenComparing(x -> x.ten == null ? "" : x.ten));
        return ds.size() > TOP ? ds.subList(0, TOP) : ds;
    }

    /** Hạng của {@code pl} trong bảng (1 là cao nhất); 0 nghĩa là chưa có tên trên bảng. */
    public int hang(Player pl, boolean theoThong) {
        if (pl == null) {
            return 0;
        }
        baoDamDaNap();
        int cua = theoThong ? pl.soLanThong : pl.soLanBiThong;
        if (cua <= 0) {
            return 0;
        }
        int hon = 0;
        for (Dong d : bang.values()) {
            if (d.id != pl.id && d.so(theoThong) > cua) {
                hon++;
            }
        }
        return hon + 1;
    }

    /** Tổng số người đã có tên trên bảng. */
    public int soNguoi(boolean theoThong) {
        baoDamDaNap();
        int n = 0;
        for (Dong d : bang.values()) {
            if (d.so(theoThong) > 0) {
                n++;
            }
        }
        return n;
    }
}
