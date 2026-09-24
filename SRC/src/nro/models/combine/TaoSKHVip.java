package nro.models.combine;

import java.util.ArrayList;
import java.util.List;

import nro.models.consts.ConstNpc;
import nro.models.item.Item;
import nro.models.player.Player;
import nro.models.services.InventoryService;
import nro.models.services.ItemService;
import nro.models.services.Service;
import nro.models.utils.Util;

/**
 * "Tạo SKH VIP" ở Bà Hạt Mít (đảo Kame).
 *
 * <p>Bỏ vào <b>3 món đồ Thần</b> (id 555–567, chỉ nhận đồ Thần) rồi ép, nhận lại
 * <b>một trang bị kích hoạt ngẫu nhiên</b> cùng loại với MÓN ĐẦU TIÊN, từ món cùi nhất
 * lên tới cấp Thần.
 *
 * <p>Hệ của món nhận được:
 * <ul>
 * <li>món đầu là áo / quần / găng / giày → lấy theo hệ của chính món đó
 * (ví dụ Găng Thần Namếc + 2 Giày Thần → ra một Găng kích hoạt hệ Namếc);</li>
 * <li>món đầu là Nhẫn Thần Linh (ô rađa) → rađa không chia hệ, nên lấy theo
 * hệ của người chơi.</li>
 * </ul>
 *
 * <p>Mỗi lần ép tốn {@value #GEM_TAO} ngọc và 1 tỷ vàng, tỉ lệ thành công 100%.
 */
public class TaoSKHVip {

    private static final int GOLD_TAO = 1_000_000_000;
    private static final int GEM_TAO = 500;
    private static final int RATIO_TAO = 100;
    private static final int SO_MON_CAN = 3;

    /** Đồ Thần: 555–567. */
    private static final int THAN_MIN = 555;
    private static final int THAN_MAX = 567;

    /** Đồ Thần theo [hệ][ô]: Trái Đất / Namếc / Xayda × áo, quần, găng, giày, rađa. */
    private static final int[][] DO_THAN = {
        {555, 556, 562, 563, 561},
        {557, 558, 564, 565, 561},
        {559, 560, 566, 567, 561},
    };

    /**
     * Dải trang bị kích hoạt theo [hệ][ô], lấy đúng như capsule kích hoạt
     * ({@code ItemService.OpenSKH}), có thêm món Thần ở cuối cho đủ "tới cấp Thần".
     */
    private static final int[][][] KHO_DO = {
        { // Trái Đất
            {0, 3, 33, 34, 136, 137, 138, 139, 230, 231, 232, 233, 555},
            {6, 9, 35, 36, 140, 141, 142, 143, 242, 243, 244, 245, 556},
            {21, 24, 37, 38, 144, 145, 146, 147, 254, 256, 257, 562},
            {27, 30, 39, 40, 148, 149, 150, 151, 266, 267, 268, 269, 563},
            {12, 57, 58, 59, 184, 185, 186, 187, 278, 279, 280, 281, 561}
        },
        { // Namếc
            {1, 4, 41, 42, 152, 153, 154, 155, 235, 236, 237, 557},
            {7, 10, 43, 44, 156, 157, 158, 159, 246, 247, 248, 249, 558},
            {22, 25, 45, 46, 160, 161, 162, 163, 259, 260, 261, 564},
            {28, 31, 47, 48, 164, 165, 166, 167, 270, 271, 272, 273, 565},
            {12, 57, 58, 59, 184, 185, 186, 187, 278, 279, 280, 281, 561}
        },
        { // Xayda
            {2, 5, 49, 50, 168, 169, 170, 171, 238, 239, 240, 241, 559},
            {8, 11, 51, 52, 172, 173, 174, 174, 250, 251, 252, 253, 560},
            {23, 26, 53, 54, 176, 177, 178, 179, 262, 263, 264, 265, 566},
            {29, 32, 55, 56, 180, 181, 182, 183, 274, 275, 276, 277, 567},
            {12, 57, 58, 59, 184, 185, 186, 187, 278, 279, 280, 281, 561}
        }
    };

    /** Option bộ kích hoạt theo hệ, y như capsule kích hoạt. */
    private static final int[][] OPTION_SKH = {
        {128, 129, 127, 233, 245},
        {130, 131, 132, 233, 237},
        {133, 135, 134, 233, 241}
    };

    private static final String[] TEN_O = {"Áo", "Quần", "Găng", "Giày", "Rađa"};
    private static final String[] TEN_HE = {"Trái Đất", "Namếc", "Xayda"};

    private static boolean laDoThan(Item it) {
        return it != null && it.template != null
                && it.template.id >= THAN_MIN && it.template.id <= THAN_MAX;
    }

    /** Ô trang bị của một món Thần: 0 áo, 1 quần, 2 găng, 3 giày, 4 rađa; -1 nếu không phải đồ Thần. */
    private static int oCuaDoThan(int id) {
        for (int he = 0; he < DO_THAN.length; he++) {
            for (int o = 0; o < DO_THAN[he].length; o++) {
                if (DO_THAN[he][o] == id) {
                    return o;
                }
            }
        }
        return -1;
    }

    /** Hệ của một món Thần; -1 với Nhẫn Thần Linh vì rađa không chia hệ. */
    private static int heCuaDoThan(int id) {
        if (id == 561) {
            return -1;
        }
        for (int he = 0; he < DO_THAN.length; he++) {
            for (int o = 0; o < DO_THAN[he].length; o++) {
                if (DO_THAN[he][o] == id) {
                    return he;
                }
            }
        }
        return -1;
    }

    private static int heCuaNguoiChoi(Player player) {
        int he = player.gender;
        return he < 0 ? 0 : (he > 2 ? 2 : he);
    }

    public static void showInfoCombine(Player player) {
        List<Item> list = player.combineNew.itemsCombine;
        if (list.isEmpty()) {
            baoLoi(player, "Cần đặt đủ 3 món đồ Thần!");
            return;
        }
        for (Item it : list) {
            if (!laDoThan(it)) {
                baoLoi(player, "Chỉ nhận ĐỒ THẦN (áo, quần, găng, giày, nhẫn Thần)!\n"
                        + "Món không hợp lệ: " + (it != null && it.template != null ? it.template.name : "?"));
                return;
            }
        }
        int tong = demSoMon(list);
        if (tong < SO_MON_CAN) {
            baoLoi(player, "Thiếu vật phẩm!\n- Cần: " + SO_MON_CAN + " món đồ Thần\n- Đang có: " + tong);
            return;
        }

        int o = oCuaDoThan(list.get(0).template.id);
        int he = heCuaDoThan(list.get(0).template.id);
        boolean theoNguoiChoi = he < 0;
        if (theoNguoiChoi) {
            he = heCuaNguoiChoi(player);
        }

        player.combineNew.goldCombine = GOLD_TAO;
        player.combineNew.gemCombine = GEM_TAO;
        player.combineNew.ratioCombine = RATIO_TAO;

        String npcSay = "|2|Tỉ lệ thành công: " + RATIO_TAO + "%\n"
                + "|2|Món đầu tiên: " + list.get(0).template.name + "\n"
                + "|2|Sẽ ra: " + TEN_O[o] + " kích hoạt hệ " + TEN_HE[he]
                + (theoNguoiChoi ? " (rađa lấy theo hệ của ngươi)" : "") + "\n"
                + "|2|Từ món thường nhất tới cấp Thần, ngẫu nhiên\n"
                + "|2|Cần: " + SO_MON_CAN + " món đồ Thần\n"
                + "|2|Cần: " + GEM_TAO + " ngọc\n"
                + "|2|Cần: " + Util.numberToMoney(GOLD_TAO) + " vàng\n";

        if (player.inventory.gold < GOLD_TAO) {
            npcSay += "|7|Còn thiếu " + Util.powerToString(GOLD_TAO - player.inventory.gold) + " vàng\n";
            CombineService.gI().baHatMit.createOtherMenu(player, ConstNpc.IGNORE_MENU, npcSay, "Đóng");
        } else if (player.inventory.gem < GEM_TAO) {
            npcSay += "|7|Còn thiếu " + (GEM_TAO - player.inventory.gem) + " ngọc\n";
            CombineService.gI().baHatMit.createOtherMenu(player, ConstNpc.IGNORE_MENU, npcSay, "Đóng");
        } else {
            CombineService.gI().baHatMit.createOtherMenu(player, ConstNpc.MENU_START_COMBINE, npcSay,
                    "Tạo\n" + GEM_TAO + " ngọc\n+ " + Util.numberToMoney(GOLD_TAO) + " vàng", "Từ chối");
        }
    }

    public static void thucHienTao(Player player) {
        List<Item> list = player.combineNew.itemsCombine;
        if (list.isEmpty()) {
            Service.gI().sendThongBao(player, "Cần đặt đủ 3 món đồ Thần!");
            return;
        }
        for (Item it : list) {
            if (!laDoThan(it)) {
                Service.gI().sendThongBao(player, "Chỉ nhận đồ Thần!");
                return;
            }
        }
        if (demSoMon(list) < SO_MON_CAN) {
            Service.gI().sendThongBao(player, "Không đủ 3 món đồ Thần!");
            return;
        }
        if (player.inventory.gold < GOLD_TAO) {
            Service.gI().sendThongBao(player, "Không đủ vàng để thực hiện!");
            return;
        }
        if (player.inventory.gem < GEM_TAO) {
            Service.gI().sendThongBao(player, "Không đủ ngọc để thực hiện!");
            return;
        }
        if (InventoryService.gI().getCountEmptyBag(player) <= 0) {
            Service.gI().sendThongBao(player, "Cần ít nhất 1 ô trống trong hành trang!");
            return;
        }

        int o = oCuaDoThan(list.get(0).template.id);
        int he = heCuaDoThan(list.get(0).template.id);
        if (he < 0) {
            he = heCuaNguoiChoi(player);   // nhẫn / rađa: theo hệ người chơi
        }
        if (o < 0) {
            Service.gI().sendThongBao(player, "Không đọc được loại trang bị!");
            return;
        }

        player.inventory.gold -= GOLD_TAO;
        player.inventory.gem -= GEM_TAO;
        truVatPham(player, SO_MON_CAN);

        int[] kho = KHO_DO[he][o];
        int idMoi = kho[Util.nextInt(kho.length)];
        int optionSKH = OPTION_SKH[he][Util.nextInt(OPTION_SKH[he].length)];
        Item moi = ItemService.gI().createItemSKH(idMoi, optionSKH);
        if (moi != null && moi.template != null) {
            InventoryService.gI().addItemBag(player, moi);
            CombineService.gI().sendEffectSuccessCombine(player);
            Service.gI().sendThongBao(player, "Tạo thành công: " + moi.template.name);
        } else {
            CombineService.gI().sendEffectFailCombine(player);
            Service.gI().sendThongBao(player, "Tạo thất bại!");
        }

        InventoryService.gI().sendItemBags(player);
        Service.gI().sendMoney(player);
        CombineService.gI().reOpenItemCombine(player);
    }

    private static int demSoMon(List<Item> list) {
        int n = 0;
        for (Item it : list) {
            if (laDoThan(it)) {
                n += Math.max(1, it.quantity);
            }
        }
        return n;
    }

    private static void truVatPham(Player player, int can) {
        int conLai = can;
        for (Item it : new ArrayList<>(player.combineNew.itemsCombine)) {
            if (conLai <= 0) {
                break;
            }
            if (laDoThan(it)) {
                int tru = Math.min(Math.max(1, it.quantity), conLai);
                InventoryService.gI().subQuantityItemsBag(player, it, tru);
                conLai -= tru;
            }
        }
    }

    private static void baoLoi(Player player, String text) {
        CombineService.gI().baHatMit.createOtherMenu(player, ConstNpc.IGNORE_MENU, text, "Đóng");
    }
}
