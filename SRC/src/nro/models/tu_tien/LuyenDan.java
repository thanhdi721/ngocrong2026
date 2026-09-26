package nro.models.tu_tien;

import java.util.ArrayList;
import java.util.List;
import nro.models.item.Item;
import nro.models.item.ItemTime;
import nro.models.map.ItemMap;
import nro.models.mob.Mob;
import nro.models.player.Player;
import nro.models.server.Manager;
import nro.models.services.InventoryService;
import nro.models.services.ItemService;
import nro.models.services.Service;
import nro.models.utils.Logger;
import nro.models.utils.Util;

/**
 * Luyện đan — patch 84.
 *
 * <p>Vòng chơi khép kín, cố ý bắt người chơi đi qua cả hai tính năng đã có:
 * <ul>
 *   <li><b>Linh thảo</b> ({@value #THANH_VAN_THAO}…{@value #KIM_NHUNG_QUA}) rơi từ quái map
 *       Tu Tiên. KHÔNG dính trần 500 Linh Thạch/ngày, nên cày hết Linh Thạch rồi map vẫn còn
 *       lý do để ở lại. Cho giao dịch để tự mọc ra cái chợ giữa người chơi.</li>
 *   <li><b>Địa Hỏa Tinh</b> ({@value #DIA_HOA_TINH}) và <b>đan phương</b> chỉ rơi từ boss, và
 *       rơi ở CẢ BA hạng boss (xem {@code BangRoiBoss.macDinh}) nên mem yếu vẫn có đường.</li>
 *   <li><b>Lò Luyện Đan</b> (NPC 90, đảo Kamê) ghép chúng lại, có tỉ lệ nổ.</li>
 * </ul>
 *
 * <p>Đan luyện ra mạnh hơn đan tiệm và kéo {@value #PHUT} phút thay vì 10. Mức cộng và thời
 * hạn đi theo viên đan chứ không viết cứng trong {@code NPoint} — xem {@link ItemTime}.
 */
public final class LuyenDan {

    //========================= vật phẩm (patch 84) =========================
    public static final int THANH_VAN_THAO = 2276;
    public static final int NGOC_DIEP_THAO = 2277;
    public static final int HAN_TINH_QUA = 2278;
    public static final int KIM_NHUNG_QUA = 2279;
    public static final int DIA_HOA_TINH = 2280;
    public static final int DAN_PHUONG_SO_CAP = 2281;
    public static final int DAN_PHUONG_TRUNG_CAP = 2282;
    public static final int DAN_PHUONG_CAO_CAP = 2283;
    public static final int DAN_PHE = 2284;
    public static final int DAN_TP_SUC_DANH = 2285;
    public static final int DAN_TP_HP = 2286;
    public static final int DAN_TP_KI = 2287;
    public static final int DAN_TP_GIAP = 2288;
    public static final int DAN_TP_CHI_MANG = 2289;

    /** Bốn loại linh thảo, theo đúng thứ tự id. */
    public static final int[] LINH_THAO = {THANH_VAN_THAO, NGOC_DIEP_THAO, HAN_TINH_QUA, KIM_NHUNG_QUA};

    //========================= sức mạnh của đan thượng phẩm =========================
    public static final int PHUT = 20;
    /** Thời hạn của đan thượng phẩm, mili giây. */
    public static final int THOI_GIAN = PHUT * 60 * 1000;

    public static final int MUC_SUC_DANH = 30;
    public static final int MUC_HP = 45;
    public static final int MUC_KI = 45;
    public static final int MUC_GIAP = 60;
    public static final int MUC_CHI_MANG = 15;

    //========================= tỉ lệ rơi linh thảo =========================
    /**
     * Tỉ lệ rơi của từng loại linh thảo, đọc từ cpanel (tab "Rơi đồ boss", khoá
     * {@code tt_*}), đơn vị <b>phần nghìn</b>. Bốn dòng quay RIÊNG nhau nên một con quái có
     * thể rơi nhiều loại cùng lúc.
     *
     * <p>Trần cày thật của map là <b>1.920 con/giờ</b> — không phải 2.860 con/giờ (tốc độ
     * ĐÁNH), vì 16 con hồi sinh 30 giây một lượt đã chặn ở đó (bảng trong docs 69 §"3 giây hồi
     * sinh quá nhanh"). Với mức mặc định, một người cày một mình được khoảng:
     * <pre>
     *   Thanh Vân Thảo  24‰  ->  46 lá/giờ      Hàn Tinh Quả   18‰  ->  35 lá/giờ
     *   Ngọc Diệp Thảo  24‰  ->  46 lá/giờ      Kim Nhung Quả  12‰  ->  23 lá/giờ
     *                                           TỔNG                -> 150 lá/giờ
     * </pre>
     *
     * <p>Trung bình một mẻ luyện tốn ~11 lá, nên 150 lá/giờ đủ cho ~13 mẻ. Hiếm nhất là
     * <b>Kim Nhung Quả</b>, món mà Phá Quân Đan cần tới 6 cái — chỉnh núm đó trước nếu thấy tắc.
     *
     * <p>Người KHÔNG cày nổi map này (quái đánh 50.000 một đòn) lấy linh thảo ở
     * {@link LinhDien} — xem lý do ở đó.
     */
    private static int tiLePhanNghin(int idLinhThao) {
        if (idLinhThao == THANH_VAN_THAO) {
            return nro.models.boss.BossDropConfig.TT_THANH_VAN_THAO.giaTri;
        }
        if (idLinhThao == NGOC_DIEP_THAO) {
            return nro.models.boss.BossDropConfig.TT_NGOC_DIEP_THAO.giaTri;
        }
        if (idLinhThao == HAN_TINH_QUA) {
            return nro.models.boss.BossDropConfig.TT_HAN_TINH_QUA.giaTri;
        }
        if (idLinhThao == KIM_NHUNG_QUA) {
            return nro.models.boss.BossDropConfig.TT_KIM_NHUNG_QUA.giaTri;
        }
        return 0;
    }

    private LuyenDan() {
    }

    //========================= đan phương dùng được nhiều mẻ =========================
    /**
     * Một quyển đan phương xé ra dùng được bấy nhiêu <b>mẻ</b>, kể cả mẻ nổ lò.
     *
     * <p>Vì sao không gắn số lượt lên từng vật phẩm: đan phương <b>xếp chồng</b> trong hành
     * trang ({@code is_up_to_up = 1}), mà vật phẩm xếp chồng thì mọi cái trong một ô dùng
     * CHUNG một bộ dòng chỉ số — gắn "còn 2 lần" lên một cái là cả chồng cùng còn 2 lần. Tách
     * ra không cho xếp chồng thì đầy túi, mà thêm dòng chỉ số mới thì bảng
     * {@code item_option_template} chỉ còn 2 ô trống trên tổng 255.
     *
     * <p>Nên số mẻ còn dở nằm ở <b>nhân vật</b>, lưu trong cột {@code player.tu_tien} dưới
     * khoá {@code dp} (xem {@link CotTuTien}). Luyện mà còn mẻ dở thì trừ mẻ; hết mẻ dở thì
     * mới xé một quyển mới trong túi ra.
     */
    public static final int SO_ME_MOI_DAN_PHUONG = 3;

    /** Khoá của số mẻ còn dở trong cột {@code player.tu_tien}. */
    static final String KHOA_DAN_PHUONG = "dp";

    /** Ba bậc đan phương, đúng thứ tự chỉ số dùng trong {@code Player.meDanPhuongConLai}. */
    public static final int[] DAN_PHUONG = {DAN_PHUONG_SO_CAP, DAN_PHUONG_TRUNG_CAP, DAN_PHUONG_CAO_CAP};

    /** Chỉ số của một bậc đan phương, -1 nếu không phải đan phương. */
    public static int bacDanPhuong(int idVatPham) {
        for (int i = 0; i < DAN_PHUONG.length; i++) {
            if (DAN_PHUONG[i] == idVatPham) {
                return i;
            }
        }
        return -1;
    }

    static void datMacDinhDanPhuong(Player pl) {
        for (int i = 0; i < pl.meDanPhuongConLai.length; i++) {
            pl.meDanPhuongConLai[i] = 0;
        }
    }

    /** Đọc phần giá trị của khoá {@code dp}: "2,0,1". */
    static void docPhanDanPhuong(Player pl, String giaTri) {
        try {
            String[] p = giaTri.split(",");
            for (int i = 0; i < pl.meDanPhuongConLai.length && i < p.length; i++) {
                int v = Integer.parseInt(p[i].trim());
                // Kẹp lại: dữ liệu hỏng hoặc sửa tay không được biến thành kho mẻ vô hạn.
                pl.meDanPhuongConLai[i] = Math.max(0, Math.min(v, SO_ME_MOI_DAN_PHUONG));
            }
        } catch (Exception e) {
            datMacDinhDanPhuong(pl);
        }
    }

    static String ghiPhanDanPhuong(Player pl) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < pl.meDanPhuongConLai.length; i++) {
            if (i > 0) {
                sb.append(',');
            }
            sb.append(pl.meDanPhuongConLai[i]);
        }
        return sb.toString();
    }

    /** Còn luyện được mẻ nào với bậc đan phương này không (tính cả quyển chưa xé trong túi). */
    private static boolean conDuocMe(Player pl, int idDanPhuong) {
        int bac = bacDanPhuong(idDanPhuong);
        if (bac < 0) {
            return false;
        }
        return pl.meDanPhuongConLai[bac] > 0 || dem(pl, idDanPhuong) > 0;
    }

    /**
     * Tiêu một mẻ. Còn mẻ dở thì trừ mẻ dở; hết thì xé một quyển mới trong túi ra và giữ lại
     * {@code SO_ME_MOI_DAN_PHUONG - 1} mẻ.
     *
     * @return false nếu không còn gì để tiêu (đã chặn từ trước, đây chỉ là lưới an toàn)
     */
    private static boolean tieuMotMe(Player pl, int idDanPhuong) {
        int bac = bacDanPhuong(idDanPhuong);
        if (bac < 0) {
            return false;
        }
        if (pl.meDanPhuongConLai[bac] > 0) {
            pl.meDanPhuongConLai[bac]--;
            return true;
        }
        if (dem(pl, idDanPhuong) <= 0) {
            return false;
        }
        truItem(pl, idDanPhuong, 1);
        pl.meDanPhuongConLai[bac] = SO_ME_MOI_DAN_PHUONG - 1;
        return true;
    }

    /** "3 quyển (+2 mẻ đang dở)" — để in lên menu. */
    private static String moTaDanPhuong(Player pl, int idDanPhuong) {
        int bac = bacDanPhuong(idDanPhuong);
        int doDang = bac < 0 ? 0 : pl.meDanPhuongConLai[bac];
        String s = dem(pl, idDanPhuong) + " quyển";
        if (doDang > 0) {
            s += " (+" + doDang + " mẻ đang dở)";
        }
        return s;
    }

    //========================= công thức =========================
    /** Một công thức luyện đan. */
    public static final class CongThuc {

        /** Vật phẩm luyện ra khi thành công. */
        public final int ra;
        /** Đan phương phải có, tiêu hao một cái mỗi mẻ. */
        public final int danPhuong;
        /** Tỉ lệ thành công, %. Còn lại là nổ lò. */
        public final int tiLeThanh;
        /** Nguyên liệu: mỗi dòng {id vật phẩm, số lượng}. */
        public final int[][] nguyenLieu;
        /** Nhãn ngắn để in lên nút menu, tối đa hai dòng. */
        public final String nhan;

        CongThuc(int ra, int danPhuong, int tiLeThanh, String nhan, int[][] nguyenLieu) {
            this.ra = ra;
            this.danPhuong = danPhuong;
            this.tiLeThanh = tiLeThanh;
            this.nhan = nhan;
            this.nguyenLieu = nguyenLieu;
        }
    }

    /**
     * Năm công thức, xếp từ dễ tới khó. Chỉ số trong mảng này chính là số hiệu công thức mà
     * menu của NPC dùng, nên ĐỪNG đảo thứ tự khi thêm công thức mới — thêm vào cuối.
     */
    public static final CongThuc[] CONG_THUC = {
        new CongThuc(DAN_TP_SUC_DANH, DAN_PHUONG_SO_CAP, 75, "Luyện Khí\nĐan TP",
        new int[][]{{THANH_VAN_THAO, 5}, {KIM_NHUNG_QUA, 3}, {DIA_HOA_TINH, 1}}),
        new CongThuc(DAN_TP_HP, DAN_PHUONG_SO_CAP, 75, "Hộ Thể\nĐan TP",
        new int[][]{{NGOC_DIEP_THAO, 5}, {HAN_TINH_QUA, 3}, {DIA_HOA_TINH, 1}}),
        new CongThuc(DAN_TP_KI, DAN_PHUONG_TRUNG_CAP, 60, "Tụ Khí\nĐan TP",
        new int[][]{{HAN_TINH_QUA, 6}, {THANH_VAN_THAO, 4}, {DIA_HOA_TINH, 2}}),
        new CongThuc(DAN_TP_CHI_MANG, DAN_PHUONG_TRUNG_CAP, 60, "Phá Quân\nĐan TP",
        new int[][]{{KIM_NHUNG_QUA, 6}, {NGOC_DIEP_THAO, 4}, {DIA_HOA_TINH, 2}}),
        new CongThuc(DAN_TP_GIAP, DAN_PHUONG_CAO_CAP, 45, "Kim Cương\nĐan TP",
        new int[][]{{THANH_VAN_THAO, 5}, {NGOC_DIEP_THAO, 5}, {HAN_TINH_QUA, 5},
        {KIM_NHUNG_QUA, 5}, {DIA_HOA_TINH, 3}})
    };

    /** Nhãn của năm nút chọn công thức, đúng thứ tự {@link #CONG_THUC}. */
    public static String[] nhanCongThuc() {
        String[] ra = new String[CONG_THUC.length];
        for (int i = 0; i < ra.length; i++) {
            ra[i] = CONG_THUC[i].nhan;
        }
        return ra;
    }

    //========================= quái map Tu Tiên rơi linh thảo =========================
    /**
     * Thả thêm linh thảo vào danh sách đồ rơi của một con quái map Tu Tiên.
     *
     * <p>Gọi từ {@link TuTien#roiLinhThach}. Quay riêng từng loại nên một con có thể rơi hai
     * ba loại cùng lúc; đây là chủ ý, vì công thức nào cũng cần ít nhất hai loại.
     *
     * <p>KHÔNG ăn theo Tụ Linh Phù: bùa đó mua bằng thỏi vàng và chỉ hứa tăng tỉ lệ
     * <b>Linh Thạch</b>, cho nó tăng cả linh thảo là lặng lẽ đổi giá trị món đã bán.
     */
    static void themLinhThao(Mob mob, Player plKill, int x, int y, List<ItemMap> ds) {
        if (mob == null || mob.zone == null || plKill == null || ds == null) {
            return;
        }
        for (int i = 0; i < LINH_THAO.length; i++) {
            int id = LINH_THAO[i];
            if (id < 0 || id >= Manager.ITEM_TEMPLATES.size()) {
                return;     // chưa chạy patch 84 — thà không rơi còn hơn văng lỗi
            }
            int tiLe = tiLePhanNghin(id);
            if (tiLe <= 0 || !Util.isTrue(tiLe, 1000)) {
                continue;
            }
            try {
                // Linh thảo CHO giao dịch (không gắn dòng khoá 30) — đây là chỗ để người
                // chơi tự buôn bán với nhau.
                ds.add(new ItemMap(mob.zone, id, 1, x, y, plKill.id));
            } catch (Exception e) {
                Logger.error("Loi tha linh thao: " + e + "\n");
            }
        }
    }

    //========================= luyện =========================
    /** Trong túi đang có bao nhiêu cái vật phẩm này. */
    public static int dem(Player pl, int idVatPham) {
        Item it = InventoryService.gI().findItemBag(pl, idVatPham);
        return it == null ? 0 : it.quantity;
    }

    public static String ten(int idVatPham) {
        try {
            if (idVatPham >= 0 && idVatPham < Manager.ITEM_TEMPLATES.size()) {
                String t = Manager.ITEM_TEMPLATES.get(idVatPham).name;
                if (t != null && !t.isEmpty()) {
                    return t;
                }
            }
        } catch (Exception e) {
        }
        return "#" + idVatPham;
    }

    /** Bảng nguyên liệu của một công thức, kèm số đang có — để in ra menu xác nhận. */
    public static String moTa(Player pl, int chiSo) {
        CongThuc ct = congThuc(chiSo);
        if (ct == null) {
            return "";
        }
        StringBuilder sb = new StringBuilder();
        sb.append("Luyện: ").append(ten(ct.ra)).append('\n');
        sb.append("Tỉ lệ thành: ").append(ct.tiLeThanh).append("%\n");
        sb.append("Cần:\n");
        for (int[] nl : ct.nguyenLieu) {
            sb.append("- ").append(ten(nl[0])).append(' ').append(dem(pl, nl[0]))
                    .append('/').append(nl[1]).append('\n');
        }
        sb.append("- ").append(ten(ct.danPhuong)).append(": ").append(moTaDanPhuong(pl, ct.danPhuong))
                .append("\n1 quyển = ").append(SO_ME_MOI_DAN_PHUONG).append(" mẻ");
        return sb.toString();
    }

    public static CongThuc congThuc(int chiSo) {
        if (chiSo < 0 || chiSo >= CONG_THUC.length) {
            return null;
        }
        return CONG_THUC[chiSo];
    }

    /**
     * Luyện một mẻ.
     *
     * <p>Thứ tự cố ý: <b>kiểm đủ nguyên liệu → quay → bỏ sản phẩm vào túi → mới trừ nguyên
     * liệu</b>. Nhét túi hỏng (túi đầy) thì thoát sớm và người chơi chưa mất gì — không bao
     * giờ có chuyện mất nguyên liệu mà không nhận được gì.
     */
    public static void luyen(Player pl, int chiSo) {
        if (pl == null) {
            return;
        }
        CongThuc ct = congThuc(chiSo);
        if (ct == null) {
            return;
        }
        if (ct.ra >= Manager.ITEM_TEMPLATES.size() || DAN_PHE >= Manager.ITEM_TEMPLATES.size()) {
            Service.gI().sendThongBao(pl, "Lò chưa được lắp đặt xong (thiếu dữ liệu vật phẩm).");
            return;
        }

        // 1) Đủ nguyên liệu chưa.
        for (int[] nl : ct.nguyenLieu) {
            if (dem(pl, nl[0]) < nl[1]) {
                Service.gI().sendThongBao(pl, "Thiếu " + ten(nl[0]) + ": cần " + nl[1]
                        + ", đang có " + dem(pl, nl[0]) + ".");
                return;
            }
        }
        if (!conDuocMe(pl, ct.danPhuong)) {
            Service.gI().sendThongBao(pl, "Chưa có " + ten(ct.danPhuong) + ". Đi đánh boss đi.");
            return;
        }

        // 2) Quay.
        boolean thanh = Util.isTrue(ct.tiLeThanh, 100);
        int idRa = thanh ? ct.ra : DAN_PHE;

        // 3) Bỏ sản phẩm vào túi TRƯỚC.
        Item sp = ItemService.gI().createNewItem((short) idRa);
        if (sp == null || sp.template == null) {
            Service.gI().sendThongBao(pl, "Vật phẩm chưa có trong cơ sở dữ liệu.");
            return;
        }
        sp.quantity = 1;
        sp.itemOptions.clear();
        // Đan (kể cả Đan Phế) KHÔNG gắn dòng khoá giao dịch — giống đan tiệm.
        if (!InventoryService.gI().addItemBag(pl, sp)) {
            Service.gI().sendThongBao(pl, "Hành trang đã đầy, dọn bớt rồi luyện lại.");
            return;
        }

        // 4) Giờ mới trừ nguyên liệu. Đan phương tiêu theo MẺ, không theo quyển: một quyển
        //    xé ra dùng được SO_ME_MOI_DAN_PHUONG mẻ, kể cả mẻ nổ lò.
        for (int[] nl : ct.nguyenLieu) {
            truItem(pl, nl[0], nl[1]);
        }
        tieuMotMe(pl, ct.danPhuong);
        InventoryService.gI().sendItemBags(pl);

        // 5) Kể chuyện.
        if (thanh) {
            Service.gI().sendThongBao(pl, "Lò reo một tiếng. Ra lò: " + ten(ct.ra) + "!");
            try {
                Service.gI().chat(pl, "Đan thành rồi!");
            } catch (Exception e) {
            }
        } else {
            noLo(pl, ct);
        }
    }

    private static void truItem(Player pl, int idVatPham, int soLuong) {
        Item it = InventoryService.gI().findItemBag(pl, idVatPham);
        if (it != null) {
            InventoryService.gI().subQuantityItemsBag(pl, it, soLuong);
        }
    }

    //========================= nổ lò =========================
    private static final String[] LOI_NO = {
        "vừa cho nổ lò, khói bay tới tận Namếc",
        "luyện đan xong thì cháy sạch lông mày",
        "nổ lò cái đùng, Lão Quân phải chạy ra dập lửa",
        "đốt hết nguyên liệu mà chỉ ra được cục than",
        "nổ lò lần nữa. Bà Mối bảo thằng này luyện đan còn dở hơn thông đít"
    };

    /**
     * Nổ lò: mất sạch nguyên liệu, được một cục {@link #DAN_PHE}, và cả server được cười.
     *
     * <p>CỐ Ý không trừ máu, không đá về nhà, không debuff: nổ lò phải vui chứ không được
     * làm gián đoạn người chơi đang làm việc khác.
     */
    private static void noLo(Player pl, CongThuc ct) {
        Service.gI().sendThongBao(pl, "ĐÙNG!!! Lò nổ. Mất sạch nguyên liệu, còn mỗi cục Đan Phế.");
        try {
            Service.gI().chat(pl, "Khoan... cái này không phải lỗi của tôi!");
        } catch (Exception e) {
        }
        try {
            String ten = pl.name == null ? "Một đạo hữu" : pl.name;
            Service.gI().sendThongBaoAllPlayer(ten + " " + LOI_NO[Util.nextInt(0, LOI_NO.length - 1)] + ".");
        } catch (Exception e) {
        }
        BangVangNoLo.ghiNhan(pl);
    }

    //========================= đan thượng phẩm =========================
    /** Có phải một trong năm viên đan thượng phẩm không. */
    public static boolean laDanThuongPham(int idVatPham) {
        return idVatPham >= DAN_TP_SUC_DANH && idVatPham <= DAN_TP_CHI_MANG;
    }

    /**
     * Bật hiệu lực của một viên đan thượng phẩm.
     *
     * <p>Ghi đè cả ba ô (bật / thời hạn / mức cộng) nên ăn viên thượng phẩm đè lên viên tiệm
     * đang chạy là được nâng cấp, còn ăn viên tiệm đè lên viên thượng phẩm là bị hạ xuống —
     * cả hai đều KHÔNG cộng dồn, đúng như đan tiệm vẫn xử sự với nhau.
     */
    public static void dungDanThuongPham(Player pl, int idVatPham) {
        if (pl == null || pl.itemTime == null) {
            return;
        }
        long luc = System.currentTimeMillis();
        switch (idVatPham) {
            case DAN_TP_SUC_DANH -> {
                pl.itemTime.isUseDanSucDanh = true;
                pl.itemTime.lastTimeDanSucDanh = luc;
                pl.itemTime.hanDanSucDanh = THOI_GIAN;
                pl.itemTime.mucDanSucDanh = MUC_SUC_DANH;
                Service.gI().point(pl);
            }
            case DAN_TP_HP -> {
                pl.itemTime.isUseDanHp = true;
                pl.itemTime.lastTimeDanHp = luc;
                pl.itemTime.hanDanHp = THOI_GIAN;
                pl.itemTime.mucDanHp = MUC_HP;
                Service.gI().point(pl);
            }
            case DAN_TP_KI -> {
                pl.itemTime.isUseDanKi = true;
                pl.itemTime.lastTimeDanKi = luc;
                pl.itemTime.hanDanKi = THOI_GIAN;
                pl.itemTime.mucDanKi = MUC_KI;
                Service.gI().point(pl);
            }
            case DAN_TP_GIAP -> {
                pl.itemTime.isUseDanGiap = true;
                pl.itemTime.lastTimeDanGiap = luc;
                pl.itemTime.hanDanGiap = THOI_GIAN;
                pl.itemTime.mucDanGiap = MUC_GIAP;
            }
            case DAN_TP_CHI_MANG -> {
                pl.itemTime.isUseDanChiMang = true;
                pl.itemTime.lastTimeDanChiMang = luc;
                pl.itemTime.hanDanChiMang = THOI_GIAN;
                pl.itemTime.mucDanChiMang = MUC_CHI_MANG;
                Service.gI().point(pl);
            }
            default -> {
                return;
            }
        }
        Service.gI().sendThongBao(pl, ten(idVatPham) + ": hiệu lực " + PHUT + " phút.");
    }

    /** Đan Phế ăn vào không được gì, chỉ được một câu. */
    private static final String[] LOI_DAN_PHE = {
        "Vị như than tổ ong trộn đất.",
        "Nuốt xong mới nhớ ra đây là cục lò cháy.",
        "Không tăng chỉ số nào cả. Nhưng răng thì chắc hơn hẳn.",
        "Lão Quân nhìn thấy sẽ rất buồn."
    };

    public static void anDanPhe(Player pl) {
        if (pl == null) {
            return;
        }
        Service.gI().sendThongBao(pl, LOI_DAN_PHE[Util.nextInt(0, LOI_DAN_PHE.length - 1)]);
    }

    //========================= danh sách công thức để xem =========================
    /** Toàn bộ công thức, mỗi công thức một đoạn — dùng cho menu "Xem công thức". */
    public static List<String> bangCongThuc(Player pl) {
        List<String> ds = new ArrayList<>();
        for (int i = 0; i < CONG_THUC.length; i++) {
            ds.add(moTa(pl, i));
        }
        return ds;
    }
}
