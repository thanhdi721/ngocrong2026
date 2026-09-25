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
 * "Pháp sư trang bị" ở Bà Hạt Mít: nạp chỉ số cho <b>cải trang, đeo lưng và linh thú</b> —
 * ba loại đồ vốn không có chỉ số gì, nhất là 138 món vừa mang từ source SUMO về.
 *
 * <p>Nâng: đặt 1 trang bị + {@value #DA_NANG} Đá Pháp Sư, trả {@value #GOLD_NANG} vàng.
 * Mỗi lần bốc ngẫu nhiên MỘT trong 7 dòng rồi cộng vào đó; trúng trùng dòng cũ thì cộng dồn.
 * Một món chỉ nâng được {@value #SO_LAN_TOI_DA} lần — ra chỉ số không ưng thì phải tẩy sạch
 * rồi nâng lại từ đầu, nên người chơi có cớ đi săn đá.
 *
 * <p>Tẩy: đặt trang bị đã pháp sư + {@value #DA_TAY} Đá Tẩy Pháp Sư, trả {@value #GEM_TAY}
 * ngọc, gỡ sạch mọi dòng Pháp Sư (không đụng các dòng khác của món đồ).
 *
 * <p>Bảy dòng là option 251–257, {@code NPoint.addOption} cộng chúng vào đúng chỗ của các
 * dòng tương đương sẵn có, nên không phải sửa chỗ tính chỉ số nào khác.
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

    /** {id option, mỗi lần trúng cộng bao nhiêu, tên ngắn để ghi thông báo}. */
    private static final Object[][] DONG = {
        {251, 2, "Sức đánh"},
        {252, 2, "HP"},
        {253, 2, "KI"},
        {254, 100, "Giáp"},
        {255, 1, "Giảm sát thương"},
        {256, 1, "Né đòn"},
        {257, 1, "Xuyên giáp"},
    };

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
        int t = it.template.type;
        if (t == 5 || t == 11) {
            return true;
        }
        return t == 27 && it.template.head >= 0 && it.template.body >= 0 && it.template.leg >= 0;
    }

    private static boolean laDongPhapSu(Item.ItemOption io) {
        return io != null && io.optionTemplate != null
                && io.optionTemplate.id >= 251 && io.optionTemplate.id <= 257;
    }

    /** Số lần đã nâng của một dòng (param chia cho mức cộng mỗi lần). */
    private static int soLanCuaDong(Item it, int idOption, int moiLan) {
        for (Item.ItemOption io : it.itemOptions) {
            if (io.optionTemplate != null && io.optionTemplate.id == idOption) {
                return io.param / moiLan;
            }
        }
        return 0;
    }

    /** Tổng số lần đã nâng của món đồ, cộng hết 7 dòng lại. */
    private static int soLanDaNang(Item it) {
        int n = 0;
        for (Object[] d : DONG) {
            n += soLanCuaDong(it, (int) d[0], (int) d[1]);
        }
        return n;
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

    private static String bangChiSo(Item it) {
        StringBuilder sb = new StringBuilder();
        for (Item.ItemOption io : it.itemOptions) {
            if (laDongPhapSu(io)) {
                sb.append("|0|").append(io.getOptionString()).append("\n");
            }
        }
        return sb.length() == 0 ? "|0|(chưa có dòng Pháp Sư nào)\n" : sb.toString();
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
        if (nro.models.server.Manager.ITEM_OPTION_TEMPLATES.size() > 257) {
            return false;
        }
        baoLoi(player, "Chức năng chưa dùng được:\nmáy chủ còn thiếu 7 dòng chỉ số Pháp Sư.\n"
                + "Hãy chạy patch 75 rồi khởi động lại.");
        nro.models.utils.Logger.error("Pháp sư trang bị: thiếu option 251-257, chưa chạy patch 75\n");
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
            baoLoi(player, "Đặt vào 1 cải trang / đeo lưng / linh thú\nvà " + DA_NANG + " Đá Pháp Sư.");
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

        Object[] dong = DONG[Util.nextInt(DONG.length)];
        int idOption = (int) dong[0];
        int moiLan = (int) dong[1];
        Item.ItemOption dangCo = null;
        for (Item.ItemOption io : tb.itemOptions) {
            if (io.optionTemplate != null && io.optionTemplate.id == idOption) {
                dangCo = io;
                break;
            }
        }
        if (dangCo == null) {
            tb.itemOptions.add(new Item.ItemOption(idOption, moiLan));
        } else {
            dangCo.param += moiLan;
        }

        CombineService.gI().sendEffectSuccessCombine(player);
        Service.gI().sendThongBao(player, "Pháp sư thành công: " + dong[2] + " +" + moiLan
                + " (đã nâng " + soLanDaNang(tb) + "/" + SO_LAN_TOI_DA + " lần)");

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
        for (Item.ItemOption io : tb.itemOptions) {
            if (laDongPhapSu(io)) {
                return true;
            }
        }
        return false;
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
        tb.itemOptions.removeIf(PhapSuTrangBi::laDongPhapSu);

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
