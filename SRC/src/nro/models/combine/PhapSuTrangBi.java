package nro.models.combine;

import java.util.ArrayList;
import java.util.List;

import nro.models.consts.ConstNpc;
import nro.models.item.Item;
import nro.models.player.Player;
import nro.models.services.InventoryService;
import nro.models.services.Service;
import nro.models.utils.Util;

/**
 * "Pháp sư trang bị" ở Bà Hạt Mít: nạp thêm chỉ số cho <b>áo, quần, găng, giày và rađa</b>.
 *
 * <p>Nâng: đặt 1 trang bị + {@value #DA_NANG} Đá Pháp Sư, trả {@value #GOLD_NANG} vàng.
 * Mỗi lần bốc ngẫu nhiên MỘT trong 7 dòng rồi cộng vào đó; trúng trùng dòng cũ thì cộng dồn.
 * Một món chỉ nâng được {@value #SO_LAN_TOI_DA} lần — ra chỉ số không ưng thì phải tẩy sạch
 * rồi nâng lại từ đầu, nên người chơi có cớ đi săn đá.
 *
 * <p>Tẩy: đặt trang bị đã pháp sư + {@value #DA_TAY} Đá Tẩy Pháp Sư, trả {@value #GEM_TAY}
 * ngọc, gỡ sạch mọi dòng Pháp Sư (không đụng các dòng khác của món đồ).
 *
 * <p><b>Bảy chỉ số dùng LẠI option có sẵn</b> (50 sức đánh %, 77 HP %, 103 KI %, 47 giáp,
 * 94 giảm sát thương, 108 né đòn, 98 + 99 xuyên giáp) chứ không đẻ dòng mới: gói tin gửi bảng
 * option ghi SỐ DÒNG bằng một byte và id mỗi dòng cũng một byte, bảng đang 251 dòng nên tổng
 * không được quá 255 — thêm 7 dòng là hỏng client.
 *
 * <p>Vì trang bị thường ĐÃ CÓ chỉ số riêng, lúc tẩy phải trừ đúng phần pháp sư cộng vào chứ
 * không được xoá cả dòng. Nên món đồ mang thêm hai tem:
 * <ul>
 * <li>{@value #TEM} "Pháp Sư cấp #" — số lần đã nâng, để người chơi nhìn thấy;</li>
 * <li>{@value #DAU} "Dấu Pháp Sư" — tên KHÔNG có dấu # nên client chỉ hiện chữ, còn
 * {@code param} âm thầm giữ số lần trúng của từng dòng (mã cơ số 7), tẩy đọc mã này để
 * trừ lại cho đúng.</li>
 * </ul>
 */
public class PhapSuTrangBi {

    public static final int DA_PHAP_SU = 2262;
    public static final int DA_TAY_PHAP_SU = 2263;

    private static final int GOLD_NANG = 200_000_000;
    private static final int DA_NANG = 20;
    private static final int GEM_TAY = 500;
    private static final int DA_TAY = 5;
    private static final int RATIO = 100;

    /**
     * Một món chỉ nâng được ngần này lần, KHÔNG phải trần từng dòng. Mỗi lần bốc lại trong
     * cả 7 dòng nên có thể trúng trùng dòng cũ (cộng dồn) — ai muốn dồn hết vào sức đánh thì
     * phải may, ra chỉ số không ưng thì chỉ còn cách tẩy sạch rồi nâng lại từ đầu.
     */
    private static final int SO_LAN_TOI_DA = 6;

    /** Tem hiện cho người chơi thấy: "Pháp Sư cấp #". */
    private static final int TEM = 251;
    /** Tem ngầm: tên không có "#" nên client không hiện số; param giữ mã cơ số 7. */
    private static final int DAU = 252;

    /**
     * Bảy chỉ số: {các id option, cộng ít nhất, cộng nhiều nhất, tên, hậu tố hiện ra}.
     *
     * <p>Mỗi lần trúng bốc một số trong khoảng; trúng LẠI cùng dòng thì phần cộng của lần đó
     * nhân thêm {@code 50%} cho mỗi lần đã có trước (lần 2 ×1,5 · lần 3 ×2 · lần 4 ×2,5…).
     *
     * <p>Sức đánh / HP / KI cộng THẲNG (option 0, 6, 7) chứ không theo phần trăm, đúng như
     * chủ dự án chốt. Muốn quay lại kiểu phần trăm thì đổi ba dòng đầu thành
     * {@code {new int[]{50}, 1, 3, "Sức đánh", "%"}}, {@code {new int[]{77}, …}},
     * {@code {new int[]{103}, …}} — phần còn lại của code không phải sửa gì.
     */
    private static final Object[][] DONG = {
        {new int[]{0}, 100, 1000, "Sức đánh", ""},
        {new int[]{6}, 1000, 2000, "HP", ""},
        {new int[]{7}, 1000, 2000, "KI", ""},
        {new int[]{47}, 100, 300, "Giáp", ""},
        {new int[]{94}, 1, 2, "Giảm sát thương", "%"},
        {new int[]{108}, 1, 2, "Né đòn", "%"},
        {new int[]{98, 99}, 1, 2, "Xuyên giáp", "%"},
    };

    /** Mỗi lần trúng lại cùng một dòng thì phần cộng nhân thêm ngần này. */
    private static final double TANG_MOI_CAP = 0.5;

    /** Hai tem của chức năng này. */
    private static final java.util.Set<Integer> ID_TEM = new java.util.HashSet<>(
            java.util.Arrays.asList(TEM, DAU));

    /**
     * Đồ cho phép pháp sư: cải trang (5), đeo lưng (11), linh thú (27 có đủ part).
     *
     * <p>Bỏ qua đồ GỘP CHỒNG được: 6 cải trang và 1 đeo lưng bên mình đang để
     * {@code is_up_to_up = 1}, nạp chỉ số lên một chồng nhiều cái vừa vô lý vừa dễ sinh
     * chuyện lúc gộp/tách. Hai viên đá cũng rơi vào nhánh này nên không bị nhận nhầm.
     */
    public static boolean phapSuDuoc(Item it) {
        if (it == null || it.template == null || it.template.isUpToUp) {
            return false;
        }
        // Ngọc bội của map Tu Tiên cũng là type 4 (ô rađa) nhưng là đồ của ĐỆ TỬ,
        // không cho pháp sư — chỉ số của nó do NPC Tu Tiên gắn cố định.
        if (nro.models.tu_tien.TuTien.laNgocBoi(it.template.id)) {
            return false;
        }
        int t = it.template.type;
        return t >= 0 && t <= 4;
    }

    private static boolean laTem(Item.ItemOption io) {
        return io != null && io.optionTemplate != null && ID_TEM.contains(io.optionTemplate.id);
    }

    private static Item.ItemOption timDong(Item it, int id) {
        for (Item.ItemOption io : it.itemOptions) {
            if (io.optionTemplate != null && io.optionTemplate.id == id) {
                return io;
            }
        }
        return null;
    }

    /** Hạt giống nằm ở tem ngầm; 0 nghĩa là chưa pháp sư lần nào. */
    private static int hatGiong(Item it) {
        Item.ItemOption dau = timDong(it, DAU);
        return dau == null ? 0 : dau.param;
    }

    /**
     * Phát lại đúng dãy bốc của món đồ: cùng hạt giống thì cho ra cùng kết quả, nên lúc tẩy
     * trừ lại được CHÍNH XÁC phần đã cộng mà không cần nhớ chỉ số gốc của trang bị.
     *
     * @return tổng đã cộng cho từng dòng sau {@code soLan} lần bốc
     */
    private static int[] phatLai(int hat, int soLan) {
        int[] tong = new int[DONG.length];
        int[] dem = new int[DONG.length];
        java.util.Random r = new java.util.Random(hat);
        for (int i = 0; i < soLan; i++) {
            int k = r.nextInt(DONG.length);
            int min = (int) DONG[k][1];
            int max = (int) DONG[k][2];
            int goc = min + r.nextInt(max - min + 1);
            tong[k] += (int) Math.round(goc * (1 + TANG_MOI_CAP * dem[k]));
            dem[k]++;
        }
        return tong;
    }

    /** Ghi lại hai tem. */
    private static void ghiTem(Item it, int hat, int soLan) {
        Item.ItemOption tem = timDong(it, TEM);
        if (tem == null) {
            it.itemOptions.add(new Item.ItemOption(TEM, soLan));
        } else {
            tem.param = soLan;
        }
        Item.ItemOption dau = timDong(it, DAU);
        if (dau == null) {
            it.itemOptions.add(new Item.ItemOption(DAU, hat));
        } else {
            dau.param = hat;
        }
    }

    /** Số lần đã nâng, đọc thẳng ở tem. */
    private static int soLanDaNang(Item it) {
        Item.ItemOption tem = timDong(it, TEM);
        return tem == null ? 0 : tem.param;
    }

    /** Cộng một dòng (có thể gồm nhiều id) vào món đồ. */
    private static void congDong(Item it, int[] ids, int moiLan) {
        for (int id : ids) {
            Item.ItemOption dangCo = null;
            for (Item.ItemOption io : it.itemOptions) {
                if (io.optionTemplate != null && io.optionTemplate.id == id) {
                    dangCo = io;
                    break;
                }
            }
            if (dangCo == null) {
                it.itemOptions.add(new Item.ItemOption(id, moiLan));
            } else {
                dangCo.param += moiLan;
            }
        }
    }

    /** Trừ lại một dòng, hết thì bỏ hẳn dòng đó. */
    private static void truDong(Item it, int[] ids, int bot) {
        for (int id : ids) {
            Item.ItemOption io = timDong(it, id);
            if (io == null) {
                continue;
            }
            io.param -= bot;
            if (io.param <= 0) {
                it.itemOptions.remove(io);
            }
        }
    }

    private static Item timTrangBi(Player player) {
        for (Item it : player.combineNew.itemsCombine) {
            if (phapSuDuoc(it)) {
                return it;
            }
        }
        return null;
    }

    /** Đếm số trang bị đã đặt, để không âm thầm chỉ xử món đầu khi người chơi đặt nhiều món. */
    private static int demTrangBi(Player player) {
        int n = 0;
        for (Item it : player.combineNew.itemsCombine) {
            if (phapSuDuoc(it)) {
                n++;
            }
        }
        return n;
    }

    private static int demDa(Player player, int idDa) {
        int n = 0;
        for (Item it : player.combineNew.itemsCombine) {
            if (it != null && it.template != null && it.template.id == idDa) {
                n += Math.max(1, it.quantity);
            }
        }
        return n;
    }

    /** Liệt kê phần Pháp Sư đã cộng, không lẫn chỉ số gốc của món đồ. */
    private static String bangChiSo(Item it) {
        int[] tong = phatLai(hatGiong(it), soLanDaNang(it));
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < DONG.length; i++) {
            if (tong[i] > 0) {
                sb.append("|0|").append(DONG[i][3]).append(" +").append(tong[i])
                        .append(DONG[i][4]).append("\n");
            }
        }
        return sb.length() == 0 ? "|0|(chưa pháp sư lần nào)\n" : sb.toString();
    }

    private static void baoLoi(Player player, String text) {
        CombineService.gI().baHatMit.createOtherMenu(player, ConstNpc.IGNORE_MENU, text, "Đóng");
    }

    /**
     * Dữ liệu đã có chưa. Chạy jar mới mà quên chạy patch 75 thì bảng option chưa có dòng
     * 251–257; lúc đó {@code ItemService.getItemOptionTemplate(251)} sẽ văng
     * IndexOutOfBounds (bảng tra theo CHỈ SỐ MẢNG), nên chặn từ đầu và báo cho rõ.
     */
    private static boolean chuaChayPatch(Player player) {
        if (nro.models.server.Manager.ITEM_OPTION_TEMPLATES.size() > DAU) {
            return false;
        }
        baoLoi(player, "Chức năng chưa dùng được:\nmáy chủ còn thiếu hai dòng tem Pháp Sư.\n"
                + "Hãy chạy patch 75 rồi khởi động lại.");
        nro.models.utils.Logger.error("Pháp sư trang bị: thiếu option " + TEM + "/" + DAU + ", chưa chạy patch 75\n");
        return true;
    }

    //====================================================== NÂNG

    public static void showInfoNang(Player player) {
        if (chuaChayPatch(player)) {
            return;
        }
        Item tb = timTrangBi(player);
        if (demTrangBi(player) > 1) {
            baoLoi(player, "Mỗi lần chỉ pháp sư được MỘT món.\nHãy bỏ bớt trang bị ra.");
            return;
        }
        int da = demDa(player, DA_PHAP_SU);
        if (tb == null) {
            baoLoi(player, "Đặt vào 1 áo / quần / găng / giày / rađa\nvà " + DA_NANG + " Đá Pháp Sư.");
            return;
        }
        if (da < DA_NANG) {
            baoLoi(player, "Thiếu Đá Pháp Sư!\n- Cần: " + DA_NANG + "\n- Đang có: " + da);
            return;
        }
        if (soLanDaNang(tb) >= SO_LAN_TOI_DA) {
            baoLoi(player, tb.template.name + "\nđã nâng đủ " + SO_LAN_TOI_DA + " lần.\n"
                    + "Muốn đổi chỉ số thì phải TẨY sạch rồi nâng lại từ đầu.");
            return;
        }

        player.combineNew.goldCombine = GOLD_NANG;
        player.combineNew.ratioCombine = RATIO;

        String npcSay = "|2|" + tb.template.name + " (" + soLanDaNang(tb) + "/" + SO_LAN_TOI_DA + " lần)\n"
                + bangChiSo(tb)
                + "|2|Mỗi lần bốc ngẫu nhiên 1 trong 7 dòng, trúng trùng thì cộng dồn\n"
                + "|7|Một món chỉ nâng được " + SO_LAN_TOI_DA + " lần, ra chỉ số không ưng phải tẩy làm lại\n"
                + "|7|Tỉ lệ thành công: " + RATIO + "%\n"
                + "|2|Cần: " + DA_NANG + " Đá Pháp Sư\n"
                + "|2|Cần: " + Util.numberToMoney(GOLD_NANG) + " vàng\n";

        if (player.inventory.gold < GOLD_NANG) {
            npcSay += "|7|Còn thiếu " + Util.powerToString(GOLD_NANG - player.inventory.gold) + " vàng\n";
            CombineService.gI().baHatMit.createOtherMenu(player, ConstNpc.IGNORE_MENU, npcSay, "Đóng");
        } else {
            CombineService.gI().baHatMit.createOtherMenu(player, ConstNpc.MENU_START_COMBINE, npcSay,
                    "Pháp sư\n" + Util.numberToMoney(GOLD_NANG) + " vàng", "Từ chối");
        }
    }

    public static void thucHienNang(Player player) {
        if (chuaChayPatch(player)) {
            return;
        }
        Item tb = timTrangBi(player);
        if (demTrangBi(player) > 1) {
            Service.gI().sendThongBao(player, "Mỗi lần chỉ pháp sư được một món!");
            return;
        }
        if (tb == null) {
            Service.gI().sendThongBao(player, "Thiếu trang bị!");
            return;
        }
        if (demDa(player, DA_PHAP_SU) < DA_NANG) {
            Service.gI().sendThongBao(player, "Không đủ Đá Pháp Sư!");
            return;
        }
        if (player.inventory.gold < GOLD_NANG) {
            Service.gI().sendThongBao(player, "Không đủ vàng để thực hiện!");
            return;
        }
        if (soLanDaNang(tb) >= SO_LAN_TOI_DA) {
            Service.gI().sendThongBao(player, "Trang bị đã nâng đủ " + SO_LAN_TOI_DA + " lần, phải tẩy mới nâng lại được!");
            return;
        }

        player.inventory.gold -= GOLD_NANG;
        truDa(player, DA_PHAP_SU, DA_NANG);

        int soLan = soLanDaNang(tb);
        int hat = hatGiong(tb);
        if (hat == 0) {
            hat = 1 + Util.nextInt(1_000_000_000);   // món mới: bốc hạt giống riêng
        }
        int[] truoc = phatLai(hat, soLan);
        int[] sau = phatLai(hat, soLan + 1);
        int k = -1;
        for (int i = 0; i < DONG.length; i++) {
            int them = sau[i] - truoc[i];
            if (them > 0) {
                congDong(tb, (int[]) DONG[i][0], them);
                k = i;
            }
        }
        ghiTem(tb, hat, soLan + 1);
        if (k < 0) {
            k = 0;   // không bao giờ xảy ra, nhưng để thông báo không văng
        }
        Object[] dong = DONG[k];
        int themCuoi = sau[k] - truoc[k];

        CombineService.gI().sendEffectSuccessCombine(player);
        Service.gI().sendThongBao(player, "Pháp sư thành công: " + dong[3] + " +" + themCuoi
                + dong[4] + " (đã nâng " + soLanDaNang(tb) + "/" + SO_LAN_TOI_DA + " lần)");

        InventoryService.gI().sendItemBags(player);
        Service.gI().sendMoney(player);
        Service.gI().point(player);
        CombineService.gI().reOpenItemCombine(player);
    }

    //====================================================== TẨY

    public static void showInfoTay(Player player) {
        if (chuaChayPatch(player)) {
            return;
        }
        Item tb = timTrangBi(player);
        if (demTrangBi(player) > 1) {
            baoLoi(player, "Mỗi lần chỉ tẩy được MỘT món.\nHãy bỏ bớt trang bị ra.");
            return;
        }
        int da = demDa(player, DA_TAY_PHAP_SU);
        if (tb == null) {
            baoLoi(player, "Đặt vào trang bị đã pháp sư\nvà " + DA_TAY + " Đá Tẩy Pháp Sư.");
            return;
        }
        if (da < DA_TAY) {
            baoLoi(player, "Thiếu Đá Tẩy Pháp Sư!\n- Cần: " + DA_TAY + "\n- Đang có: " + da);
            return;
        }
        if (!coDongPhapSu(tb)) {
            baoLoi(player, tb.template.name + "\nchưa có dòng Pháp Sư nào để tẩy.");
            return;
        }

        player.combineNew.gemCombine = GEM_TAY;
        player.combineNew.ratioCombine = RATIO;

        String npcSay = "|2|" + tb.template.name + " (" + soLanDaNang(tb) + "/" + SO_LAN_TOI_DA + " lần)\n"
                + bangChiSo(tb)
                + "|7|Tẩy xong mất SẠCH các dòng trên, nâng lại từ con số 0\n"
                + "|2|Cần: " + DA_TAY + " Đá Tẩy Pháp Sư\n"
                + "|2|Cần: " + GEM_TAY + " ngọc\n";

        if (player.inventory.gem < GEM_TAY) {
            npcSay += "|7|Còn thiếu " + (GEM_TAY - player.inventory.gem) + " ngọc\n";
            CombineService.gI().baHatMit.createOtherMenu(player, ConstNpc.IGNORE_MENU, npcSay, "Đóng");
        } else {
            CombineService.gI().baHatMit.createOtherMenu(player, ConstNpc.MENU_START_COMBINE, npcSay,
                    "Tẩy\n" + GEM_TAY + " ngọc", "Từ chối");
        }
    }

    private static boolean coDongPhapSu(Item tb) {
        return soLanDaNang(tb) > 0;
    }

    public static void thucHienTay(Player player) {
        if (chuaChayPatch(player)) {
            return;
        }
        Item tb = timTrangBi(player);
        if (demTrangBi(player) > 1) {
            Service.gI().sendThongBao(player, "Mỗi lần chỉ tẩy được một món!");
            return;
        }
        if (tb == null) {
            Service.gI().sendThongBao(player, "Thiếu trang bị!");
            return;
        }
        if (demDa(player, DA_TAY_PHAP_SU) < DA_TAY) {
            Service.gI().sendThongBao(player, "Không đủ Đá Tẩy Pháp Sư!");
            return;
        }
        if (player.inventory.gem < GEM_TAY) {
            Service.gI().sendThongBao(player, "Không đủ ngọc để thực hiện!");
            return;
        }
        if (!coDongPhapSu(tb)) {
            Service.gI().sendThongBao(player, "Trang bị chưa có dòng Pháp Sư nào!");
            return;
        }

        player.inventory.gem -= GEM_TAY;
        truDa(player, DA_TAY_PHAP_SU, DA_TAY);
        int[] tong = phatLai(hatGiong(tb), soLanDaNang(tb));
        for (int i = 0; i < DONG.length; i++) {
            if (tong[i] > 0) {
                truDong(tb, (int[]) DONG[i][0], tong[i]);
            }
        }
        tb.itemOptions.removeIf(PhapSuTrangBi::laTem);

        CombineService.gI().sendEffectSuccessCombine(player);
        Service.gI().sendThongBao(player, "Đã tẩy sạch chỉ số Pháp Sư của " + tb.template.name);

        InventoryService.gI().sendItemBags(player);
        Service.gI().sendMoney(player);
        Service.gI().point(player);
        CombineService.gI().reOpenItemCombine(player);
    }

    private static void truDa(Player player, int idDa, int can) {
        int conLai = can;
        for (Item it : new ArrayList<>(player.combineNew.itemsCombine)) {
            if (conLai <= 0) {
                break;
            }
            if (it != null && it.template != null && it.template.id == idDa) {
                int tru = Math.min(Math.max(1, it.quantity), conLai);
                InventoryService.gI().subQuantityItemsBag(player, it, tru);
                conLai -= tru;
            }
        }
    }
}
