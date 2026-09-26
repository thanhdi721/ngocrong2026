package nro.models.tu_tien;

import nro.models.player.Player;
import nro.models.utils.Logger;

/**
 * Bộ đọc / ghi cột <b>{@code player.tu_tien}</b> — nơi gom mọi dữ liệu tu tiên theo từng
 * người chơi.
 *
 * <h3>Vì sao gom vào một cột</h3>
 * Bảng {@code player} đã sát trần 65.535 byte một dòng của InnoDB — patch 78 phải đổi
 * {@code data_card} từ VARCHAR(10000) sang TEXT mới có chỗ thêm {@code thong_dit}. Mỗi lần
 * thêm cột là một lần đánh cược với lỗi 1118, nên tính năng tu tiên mới <b>thêm khoá vào cột
 * này</b> chứ không thêm cột.
 *
 * <h3>Định dạng</h3>
 * <pre>
 *   khoa=giatri#khoa=giatri#...
 *
 *   ld=2276,1759000000000;0,0;...   Linh Điền — 6 ô ruộng      (xem {@link LinhDien})
 *   dp=2,0,1                        Đan phương — số mẻ còn dở  (xem {@link LuyenDan})
 * </pre>
 *
 * <b>Khoá lạ thì bỏ qua</b>, không xoá. Nhờ vậy chạy bản jar cũ trên dữ liệu bản mới cũng
 * không làm mất dữ liệu của bản mới... miễn là bản cũ ghi đè thì mất. Đổi lại, thêm khoá mới
 * không bao giờ làm hỏng khoá cũ.
 */
public final class CotTuTien {

    private CotTuTien() {
    }

    /** Đọc cả cột vào nhân vật. Chuỗi hỏng / rỗng / null đều không ném lỗi ra ngoài. */
    public static void doc(Player pl, String chuoi) {
        if (pl == null) {
            return;
        }
        LinhDien.datMacDinh(pl);
        LuyenDan.datMacDinhDanPhuong(pl);
        if (chuoi == null || chuoi.isEmpty()) {
            return;
        }
        for (String phan : chuoi.split("#")) {
            int dau = phan.indexOf('=');
            if (dau <= 0) {
                continue;
            }
            String khoa = phan.substring(0, dau).trim();
            String giaTri = phan.substring(dau + 1);
            try {
                switch (khoa) {
                    case LinhDien.KHOA ->
                        LinhDien.docPhan(pl, giaTri);
                    case LuyenDan.KHOA_DAN_PHUONG ->
                        LuyenDan.docPhanDanPhuong(pl, giaTri);
                    default -> {
                        // khoá của tính năng chưa có trong bản này — kệ nó
                    }
                }
            } catch (Exception e) {
                Logger.error("Loi doc cot tu_tien, khoa '" + khoa + "': " + e + "\n");
            }
        }
    }

    /** Chuỗi để ghi xuống cột. */
    public static String ghi(Player pl) {
        if (pl == null) {
            return "";
        }
        return LinhDien.KHOA + "=" + LinhDien.ghiPhan(pl)
                + "#" + LuyenDan.KHOA_DAN_PHUONG + "=" + LuyenDan.ghiPhanDanPhuong(pl);
    }
}
