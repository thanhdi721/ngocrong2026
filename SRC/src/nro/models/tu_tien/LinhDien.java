package nro.models.tu_tien;

import nro.models.item.Item;
import nro.models.player.Player;
import nro.models.server.Manager;
import nro.models.services.InventoryService;
import nro.models.services.ItemService;
import nro.models.services.Service;
import nro.models.utils.Logger;
import nro.models.utils.Util;

/**
 * Linh Điền — {@value #SO_O} ô ruộng riêng của từng người chơi, gieo bằng <b>vàng</b>, sau
 * {@value #GIO_CHIN} giờ hái ra linh thảo.
 *
 * <h3>Vì sao có</h3>
 * Sau khi rải Địa Hỏa Tinh và đan phương ra mọi boss (xem {@code BangRoiBoss.luatChung}), thứ
 * duy nhất còn chặn người cày chay là <b>linh thảo</b> — mà linh thảo chỉ rơi ở map Tu Tiên,
 * nơi quái 20 triệu máu và đánh 50.000 sát thương một đòn. Người yếu vào đó là chết.
 *
 * <p>Linh Điền là <b>sàn</b> cho những người đó: gieo bằng vàng, thứ ai cũng kiếm được, không
 * phải đánh nhau với cái gì cả.
 *
 * <h3>Vì sao không sợ nó giết map Tu Tiên</h3>
 * Cày map Tu Tiên ra khoảng <b>670 lá/giờ</b> (đo ở docs 70). Linh Điền đầy 6 ô ra nhiều nhất
 * {@code 6 × }{@value #HAI_MAX}{@code  = 30} lá mỗi {@value #GIO_CHIN} giờ, tức ~180 lá/ngày
 * nếu chăm chỉ vào đúng giờ. Map Tu Tiên vẫn hơn vài chục lần — Linh Điền không cạnh tranh,
 * nó chỉ là cái sàn, và là lý do để người ta đăng nhập lại lần thứ hai trong ngày.
 *
 * <h3>Lưu ở đâu</h3>
 * Cột {@code player.tu_tien} (TEXT), dưới khoá {@code ld} — xem {@link CotTuTien} để biết
 * định dạng chung của cột.
 */
public final class LinhDien {

    public static final int SO_O = 6;
    /** Giá gieo một ô, tính bằng vàng. */
    public static final long GIA_GIEO = 500_000;
    public static final int GIO_CHIN = 4;
    public static final long THOI_GIAN_CHIN = GIO_CHIN * 60L * 60L * 1000L;
    /** Một ô chín cho ngần này hạt, cùng MỘT loại linh thảo. */
    public static final int HAI_MIN = 3;
    public static final int HAI_MAX = 5;

    private LinhDien() {
    }

    //================================ đọc / ghi cột ================================
    /** Khoá của Linh Điền trong cột {@code player.tu_tien}. Xem {@link CotTuTien}. */
    static final String KHOA = "ld";

    /** Ruộng trống sạch. Gọi trước khi đọc cột. */
    static void datMacDinh(Player pl) {
        xoaSach(pl);
    }

    /**
     * Đọc PHẦN giá trị của khoá {@code ld} (đã bóc "ld=" ở {@link CotTuTien}).
     * Chuỗi hỏng thì trả về ruộng trống chứ không để dữ liệu nửa vời.
     */
    static void docPhan(Player pl, String giaTri) {
        try {
            String[] o = giaTri.split(";");
            for (int i = 0; i < SO_O && i < o.length; i++) {
                String[] p = o[i].split(",");
                if (p.length < 2) {
                    continue;
                }
                int hat = Integer.parseInt(p[0].trim());
                long luc = Long.parseLong(p[1].trim());
                if (laLinhThao(hat) && luc > 0) {
                    pl.linhDienHat[i] = hat;
                    pl.linhDienLuc[i] = luc;
                }
            }
        } catch (Exception e) {
            xoaSach(pl);
        }
    }

    /** Phần giá trị của khoá {@code ld}. */
    static String ghiPhan(Player pl) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < SO_O; i++) {
            if (i > 0) {
                sb.append(';');
            }
            sb.append(pl.linhDienHat[i]).append(',').append(pl.linhDienLuc[i]);
        }
        return sb.toString();
    }

    private static void xoaSach(Player pl) {
        for (int i = 0; i < SO_O; i++) {
            pl.linhDienHat[i] = 0;
            pl.linhDienLuc[i] = 0;
        }
    }

    private static boolean laLinhThao(int id) {
        for (int x : LuyenDan.LINH_THAO) {
            if (x == id) {
                return true;
            }
        }
        return false;
    }

    //================================ trạng thái ================================
    private static boolean oTrong(Player pl, int i) {
        return pl.linhDienHat[i] == 0 || pl.linhDienLuc[i] <= 0;
    }

    private static boolean oChin(Player pl, int i) {
        return !oTrong(pl, i) && System.currentTimeMillis() - pl.linhDienLuc[i] >= THOI_GIAN_CHIN;
    }

    /** Bảng trạng thái 6 ô để in lên menu NPC. */
    public static String bangTrangThai(Player pl) {
        if (pl == null) {
            return "";
        }
        StringBuilder sb = new StringBuilder();
        int trong = 0;
        int chin = 0;
        for (int i = 0; i < SO_O; i++) {
            sb.append("Ô ").append(i + 1).append(": ");
            if (oTrong(pl, i)) {
                sb.append("đất trống");
                trong++;
            } else if (oChin(pl, i)) {
                sb.append(LuyenDan.ten(pl.linhDienHat[i])).append(" — ĐÃ CHÍN");
                chin++;
            } else {
                sb.append(LuyenDan.ten(pl.linhDienHat[i])).append(" — còn ")
                        .append(conLai(pl, i));
            }
            sb.append('\n');
        }
        sb.append("Trống ").append(trong).append(", chín ").append(chin)
                .append(". Gieo ").append(dinhDang(GIA_GIEO)).append(" vàng/ô.");
        return sb.toString();
    }

    /** "2h15p" hoặc "8 phút". */
    private static String conLai(Player pl, int i) {
        long con = THOI_GIAN_CHIN - (System.currentTimeMillis() - pl.linhDienLuc[i]);
        if (con < 0) {
            con = 0;
        }
        long phut = con / 60_000;
        return phut >= 60 ? (phut / 60) + "h" + (phut % 60) + "p" : Math.max(1, phut) + " phút";
    }

    private static String dinhDang(long so) {
        return String.format("%,d", so).replace(',', '.');
    }

    //================================ gieo ================================
    /**
     * Gieo hết ô trống, hết vàng thì dừng.
     *
     * <p>Trừ vàng của từng ô <b>ngay sau khi</b> ô đó được gieo, không gom lại trừ một cục —
     * làm thế thì hỏng giữa chừng là lệch tiền.
     */
    public static void gieoHet(Player pl) {
        if (pl == null || pl.inventory == null) {
            return;
        }
        int dem = 0;
        long tieu = 0;
        for (int i = 0; i < SO_O; i++) {
            if (!oTrong(pl, i)) {
                continue;
            }
            if (pl.inventory.gold < GIA_GIEO) {
                break;
            }
            pl.inventory.gold -= GIA_GIEO;
            tieu += GIA_GIEO;
            pl.linhDienHat[i] = LuyenDan.LINH_THAO[Util.nextInt(0, LuyenDan.LINH_THAO.length - 1)];
            pl.linhDienLuc[i] = System.currentTimeMillis();
            dem++;
        }
        if (dem == 0) {
            boolean conTrong = false;
            for (int i = 0; i < SO_O; i++) {
                conTrong |= oTrong(pl, i);
            }
            Service.gI().sendThongBao(pl, conTrong
                    ? "Không đủ vàng. Một ô cần " + dinhDang(GIA_GIEO) + " vàng."
                    : "Ruộng đã kín, không còn ô trống.");
            return;
        }
        Service.gI().sendMoney(pl);
        Service.gI().sendThongBao(pl, "Đã gieo " + dem + " ô, tốn " + dinhDang(tieu)
                + " vàng. " + GIO_CHIN + " giờ nữa quay lại hái.");
    }

    //================================ hái ================================
    /**
     * Hái hết ô đã chín.
     *
     * <p>Mỗi ô ra {@value #HAI_MIN}–{@value #HAI_MAX} hạt của <b>một</b> loại, nên chỉ cần một
     * lần nhét túi. Nhét không được (túi đầy) thì <b>để nguyên ô đó</b> và dừng lại — cây vẫn
     * còn trên ruộng, người chơi không mất gì.
     */
    public static void haiHet(Player pl) {
        if (pl == null) {
            return;
        }
        int oDaHai = 0;
        int tongHat = 0;
        boolean tuiDay = false;
        for (int i = 0; i < SO_O; i++) {
            if (!oChin(pl, i)) {
                continue;
            }
            int idHat = pl.linhDienHat[i];
            if (idHat < 0 || idHat >= Manager.ITEM_TEMPLATES.size()) {
                pl.linhDienHat[i] = 0;      // chưa chạy patch — dọn ô cho sạch
                pl.linhDienLuc[i] = 0;
                continue;
            }
            int sl = Util.nextInt(HAI_MIN, HAI_MAX);
            Item it = ItemService.gI().createNewItem((short) idHat);
            if (it == null || it.template == null) {
                continue;
            }
            it.quantity = sl;
            it.itemOptions.clear();         // linh thảo cho giao dịch, không gắn dòng khoá
            if (!InventoryService.gI().addItemBag(pl, it)) {
                tuiDay = true;
                break;                      // để nguyên ô, cây vẫn còn
            }
            pl.linhDienHat[i] = 0;
            pl.linhDienLuc[i] = 0;
            oDaHai++;
            tongHat += sl;
        }
        if (oDaHai > 0) {
            InventoryService.gI().sendItemBags(pl);
            Service.gI().sendThongBao(pl, "Hái được " + tongHat + " linh thảo từ " + oDaHai + " ô."
                    + (tuiDay ? " Túi đầy nên còn ô chưa hái." : ""));
            return;
        }
        if (tuiDay) {
            Service.gI().sendThongBao(pl, "Hành trang đã đầy, dọn bớt rồi hái lại.");
            return;
        }
        Service.gI().sendThongBao(pl, "Chưa ô nào chín. Cây cần " + GIO_CHIN + " giờ.");
    }

    //================================ tiện ================================
    /** Có ô nào đã chín không — dùng để nhắc người chơi lúc bấm vào NPC. */
    public static boolean coOChin(Player pl) {
        if (pl == null) {
            return false;
        }
        try {
            for (int i = 0; i < SO_O; i++) {
                if (oChin(pl, i)) {
                    return true;
                }
            }
        } catch (Exception e) {
            Logger.error("Loi kiem tra linh dien: " + e + "\n");
        }
        return false;
    }
}
