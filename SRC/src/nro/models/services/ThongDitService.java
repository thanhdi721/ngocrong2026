package nro.models.services;

import nro.models.consts.ConstNpc;
import nro.models.item.Item;
import nro.models.map.service.NpcService;
import nro.models.player.Player;
import nro.models.server.Client;
import nro.models.utils.Util;

/**
 * "Thông đít" — công dụng duy nhất của <b>Gậy Thông Thiên</b> (item 2264, rơi 5 % từ hai boss
 * Siêu Thần God).
 *
 * <p>Hai người đứng sát nhau, bấm vào người kia → menu thách đấu → chọn "Thông đít".
 * Người bị bấm phải ĐỒNG Ý thì mới tính, lời mời để quá {@value #GIAY_CHO} giây thì bỏ.
 *
 * <p>Ăn chia:
 * <ul>
 *   <li>Người đi thông: tối đa {@value #TOI_DA_THONG} lần, <b>mỗi lần +1 %</b> HP / KI / sức
 *       đánh → kịch trần +10 %.</li>
 *   <li>Người bị thông: tối đa {@value #TOI_DA_BI_THONG} lần, <b>2 lần mới được 1 %</b>
 *       → kịch trần cũng +10 %, nhưng phải chịu gấp đôi số lần.</li>
 * </ul>
 * Cộng dồn lại thành {@link #phanTram(Player)}, được {@code NPoint} nhân vào hpMax / mpMax /
 * dame, và lưu ở cột {@code player.thong_dit} nên thoát game vào lại vẫn còn.
 */
public class ThongDitService {

    /** Vật phẩm bắt buộc, tiêu 1 cái mỗi lần thông. */
    public static final int ID_GAY_THONG_THIEN = 2264;

    public static final int TOI_DA_THONG = 10;
    public static final int TOI_DA_BI_THONG = 20;

    /** Khoảng cách tối đa giữa hai người, tính bằng pixel. */
    private static final int KHOANG_CACH = 80;
    /** Lời mời sống được bấy nhiêu giây. */
    private static final int GIAY_CHO = 30;
    /** Giãn cách giữa hai lần ngỏ lời, chặn spam menu vào mặt người khác. */
    private static final long NGHI_MOI = 8_000L;

    private static final String[] LOI_KHEN = {
        "Trời đất chứng giám, hai người vừa nên duyên.",
        "Một tiếng \"pực\" vang vọng ba hành tinh.",
        "Rồng thần cũng phải quay mặt đi chỗ khác.",
        "Gậy vào như ý, ra cũng như ý. Đúng tên.",
        "Kỹ thuật sạch sẽ, dứt khoát, không rườm rà.",
        "Hội đồng Kaioshin đã ghi nhận thành tích này."
    };

    private static ThongDitService instance;

    public static ThongDitService gI() {
        if (instance == null) {
            instance = new ThongDitService();
        }
        return instance;
    }

    //================================ phần chỉ số ================================
    /** Tổng % cộng vào HP / KI / sức đánh của {@code pl} (0 … 20). */
    public static int phanTram(Player pl) {
        if (pl == null || !pl.isPl()) {
            return 0;
        }
        int thong = Math.max(0, Math.min(pl.soLanThong, TOI_DA_THONG));
        int bi = Math.max(0, Math.min(pl.soLanBiThong, TOI_DA_BI_THONG));
        return thong + bi / 2;
    }

    /** Đọc cột `thong_dit` dạng "a|b"; hỏng hay rỗng thì coi như 0|0. */
    public static void doc(Player pl, String chuoi) {
        pl.soLanThong = 0;
        pl.soLanBiThong = 0;
        if (chuoi == null || !chuoi.contains("|")) {
            return;
        }
        try {
            String[] sp = chuoi.split("\\|");
            pl.soLanThong = Math.max(0, Math.min(Integer.parseInt(sp[0].trim()), TOI_DA_THONG));
            pl.soLanBiThong = Math.max(0, Math.min(Integer.parseInt(sp[1].trim()), TOI_DA_BI_THONG));
        } catch (Exception e) {
        }
    }

    /** Ghi ra cột `thong_dit`. */
    public static String ghi(Player pl) {
        return pl.soLanThong + "|" + pl.soLanBiThong;
    }

    //================================ luồng chính ================================
    /**
     * Có đủ điều kiện ngỏ lời với {@code idDoiTac} không — hỏi thầm, không nhả thông báo.
     * Dùng để quyết định có hiện mục "Thông đít" trong menu "Kết bạn" hay không.
     */
    public boolean thongDuoc(Player pl, long idDoiTac) {
        if (pl == null || pl.zone == null) {
            return false;
        }
        return kiemTra(pl, pl.zone.getPlayerInMap(idDoiTac), false);
    }

    /** Người chơi vừa chọn "Thông đít" ở menu bấm vào {@code idDoiTac}. */
    public void moiThongDit(Player pl, long idDoiTac) {
        if (pl == null || pl.zone == null) {
            return;
        }
        Player doiTac = pl.zone.getPlayerInMap(idDoiTac);
        if (!kiemTra(pl, doiTac, true)) {
            return;
        }
        if (!Util.canDoWithTime(pl.lucNgoLoiThongDit, NGHI_MOI)) {
            Service.gI().sendThongBao(pl, "Từ từ thôi, để người ta suy nghĩ đã");
            return;
        }
        pl.lucNgoLoiThongDit = System.currentTimeMillis();
        doiTac.idMoiThongDit = pl.id;
        doiTac.lucMoiThongDit = System.currentTimeMillis();
        Service.gI().sendThongBao(pl, "Đã ngỏ lời với " + doiTac.name + ", chờ người ta gật đầu...");
        NpcService.gI().createMenuConMeo(doiTac, ConstNpc.THONG_DIT_XAC_NHAN, -1,
                pl.name + " đang cầm Gậy Thông Thiên đứng sau lưng bạn,\n"
                + "tay xoay xoay, mắt nhìn xa xăm.\n"
                + "Bạn đã bị thông " + doiTac.soLanBiThong + "/" + TOI_DA_BI_THONG + " lần.\n"
                + "Cho phép không?",
                new String[]{"Thôi được\nvì chỉ số", "Không,\ntôi còn\ndanh dự"});
    }

    /** Người bị mời bấm "Đồng ý". */
    public void dongY(Player pl) {
        if (pl == null) {
            return;
        }
        long idNguoiThong = pl.idMoiThongDit;
        long lucMoi = pl.lucMoiThongDit;
        pl.idMoiThongDit = 0;          // dùng một lần, bấm lại không ăn thêm
        pl.lucMoiThongDit = 0;
        if (idNguoiThong == 0 || lucMoi <= 0
                || System.currentTimeMillis() - lucMoi > GIAY_CHO * 1000L) {
            Service.gI().sendThongBao(pl, "Lời mời đã nguội mất rồi, bảo người ta mời lại đi");
            return;
        }
        if (pl.zone == null) {
            return;
        }
        Player nguoiThong = pl.zone.getPlayerInMap(idNguoiThong);
        if (!kiemTra(nguoiThong, pl, false)) {
            Service.gI().sendThongBao(pl, "Không thực hiện được, đối phương đã đi chỗ khác");
            return;
        }

        // Trừ nhẫn TRƯỚC khi cộng chỉ số: hết nhẫn giữa chừng thì không ai được gì.
        Item gay = InventoryService.gI().findItemBag(nguoiThong, ID_GAY_THONG_THIEN);
        if (gay == null || gay.quantity < 1) {
            Service.gI().sendThongBao(nguoiThong, "Gậy rơi đâu mất rồi, tìm lại đi");
            Service.gI().sendThongBao(pl, "Đối phương làm rơi nhẫn, hẹn dịp khác");
            return;
        }
        InventoryService.gI().subQuantityItemsBag(nguoiThong, gay, 1);
        InventoryService.gI().sendItemBags(nguoiThong);

        nguoiThong.soLanThong++;
        pl.soLanBiThong++;
        capNhatChiSo(nguoiThong);
        capNhatChiSo(pl);
        // Bảng vàng của Bà Mối: đây là chỗ DUY NHẤT hai con số này đổi, nên chỉ cần
        // báo ở đây là bảng luôn đúng, khỏi hỏi lại cơ sở dữ liệu.
        BangVangThongDit.gI().capNhat(nguoiThong);
        BangVangThongDit.gI().capNhat(pl);

        String khen = LOI_KHEN[Util.nextInt(0, LOI_KHEN.length - 1)];
        Service.gI().sendThongBao(nguoiThong, khen + "\nBạn đã thông "
                + nguoiThong.soLanThong + "/" + TOI_DA_THONG + " lần, đang +"
                + phanTram(nguoiThong) + "% HP/KI/sức đánh.");
        Service.gI().sendThongBao(pl, khen + "\nBạn đã bị thông "
                + pl.soLanBiThong + "/" + TOI_DA_BI_THONG + " lần, đang +"
                + phanTram(pl) + "% HP/KI/sức đánh.");
        Service.gI().chat(nguoiThong, "Cảm ơn nhé " + pl.name + "!");
        Service.gI().chat(pl, "Đau... nhưng mạnh lên thật.");
    }

    /** Người bị mời bấm "Từ chối" hoặc đóng menu. */
    public void tuChoi(Player pl) {
        if (pl == null) {
            return;
        }
        Player nguoiThong = pl.zone != null ? pl.zone.getPlayerInMap(pl.idMoiThongDit) : null;
        pl.idMoiThongDit = 0;
        pl.lucMoiThongDit = 0;
        Service.gI().sendThongBao(pl, "Bạn đã giữ vững lập trường. Đáng nể.");
        if (nguoiThong != null) {
            Service.gI().sendThongBao(nguoiThong, pl.name + " từ chối. Nhẫn vẫn còn, lòng thì tan nát.");
        }
    }

    //================================ phụ trợ ================================
    /**
     * Kiểm tra đủ điều kiện. {@code baoLoi} = true thì nhả thông báo cho người đi thông
     * (lúc mới bấm menu), false thì im lặng (lúc xác nhận lại, người kia đã nhận thông báo).
     */
    private boolean kiemTra(Player pl, Player doiTac, boolean baoLoi) {
        if (pl == null || !pl.isPl() || pl.zone == null) {
            return false;
        }
        if (doiTac == null || !doiTac.isPl() || doiTac.zone == null) {
            if (baoLoi) {
                Service.gI().sendThongBao(pl, "Đối phương đã rời map");
            }
            return false;
        }
        if (doiTac == pl || doiTac.id == pl.id) {
            if (baoLoi) {
                Service.gI().sendThongBao(pl, "Tay ngắn lắm, không tự thông được đâu");
            }
            return false;
        }
        if (Client.gI().getPlayer(doiTac.id) == null) {
            if (baoLoi) {
                Service.gI().sendThongBao(pl, "Đối phương đã thoát game");
            }
            return false;
        }
        if (!pl.zone.equals(doiTac.zone)) {
            if (baoLoi) {
                Service.gI().sendThongBao(pl, "Hai người không đứng chung khu vực");
            }
            return false;
        }
        if (Util.getDistance(pl, doiTac) > KHOANG_CACH) {
            if (baoLoi) {
                Service.gI().sendThongBao(pl, "Đứng sát vào, cách xa vậy với tay không tới");
            }
            return false;
        }
        if (pl.isDie() || doiTac.isDie()) {
            if (baoLoi) {
                Service.gI().sendThongBao(pl, "Có người đang nằm sàn, để người ta dậy đã");
            }
            return false;
        }
        if (pl.pvp != null || doiTac.pvp != null) {
            if (baoLoi) {
                Service.gI().sendThongBao(pl, "Đang giao đấu, không làm chuyện riêng tư được");
            }
            return false;
        }
        if (pl.soLanThong >= TOI_DA_THONG) {
            if (baoLoi) {
                Service.gI().sendThongBao(pl, "Bạn đã thông đủ " + TOI_DA_THONG
                        + " lần, chỉ số kịch trần rồi, nghỉ đi");
            }
            return false;
        }
        if (doiTac.soLanBiThong >= TOI_DA_BI_THONG) {
            if (baoLoi) {
                Service.gI().sendThongBao(pl, doiTac.name + " đã bị thông đủ "
                        + TOI_DA_BI_THONG + " lần, xin hãy tha cho người ta");
            }
            return false;
        }
        Item gay = InventoryService.gI().findItemBag(pl, ID_GAY_THONG_THIEN);
        if (gay == null || gay.quantity < 1) {
            if (baoLoi) {
                Service.gI().sendThongBao(pl, "Cần 1 Gậy Thông Thiên mới thông được (hạ boss Siêu Thần God để kiếm)");
            }
            return false;
        }
        return true;
    }

    /** Tính lại chỉ số và đẩy xuống client. */
    private void capNhatChiSo(Player pl) {
        try {
            pl.nPoint.calPoint();
            Service.gI().point(pl);
            Service.gI().Send_Info_NV(pl);
        } catch (Exception e) {
        }
    }
}
