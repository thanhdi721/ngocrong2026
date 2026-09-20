
package nro.models.combine;

import nro.models.consts.ConstNpc;
import nro.models.item.Item;
import nro.models.item.Item.ItemOption;
import java.util.ArrayList;
import java.util.Arrays;
import nro.models.player.Player;
import nro.models.services.ItemService;
import nro.models.services.Service;
import nro.models.services.InventoryService;
import nro.models.utils.Util;

/**
 *
 * @author By Mr Blue
 */

public class CheTaoTrangBiThienSu {
    private static final long GOLD_CHE_TAO = 10_000_000;

    // Kiểm tra đủ 4 món (Công thức VIP, 999 Mảnh Thiên Sứ, Đá nâng cấp, Đá may mắn).
    // Trả về null nếu hợp lệ, ngược lại là câu báo lỗi.
    private static String validate(Player player) {
        if (player.combineNew.itemsCombine.size() != 4) {
            return "Thiếu vật phẩm, vui lòng thêm vào";
        } else if (player.combineNew.itemsCombine.stream().filter(item -> item.isNotNullItem() && item.isCongThucVip()).count() != 1) {
            return "Thiếu Công Thức Vip";
        } else if (player.combineNew.itemsCombine.stream().filter(item -> item.isNotNullItem() && item.isManhTS() && item.quantity >= 999).count() != 1) {
            return "Thiếu Mảnh Thiên Sứ";
        } else if (player.combineNew.itemsCombine.stream().filter(item -> item.isNotNullItem() && item.isDaNangCap1()).count() != 1) {
            return "Thiếu Đá Nâng Cấp";
        } else if (player.combineNew.itemsCombine.stream().filter(item -> item.isNotNullItem() && item.isDaMayMan()).count() != 1) {
            return "Thiếu Đá May Mắn";
        }
        return null;
    }

    // Chủ dự án chốt: đặt đủ 4 món chỉ HIỆN thông tin + nút xác nhận, không chế tạo ngay
    // (trước đây chạy luôn, người chơi dễ mất 10 triệu vàng + 999 mảnh ngoài ý muốn).
    public static void showInfoCombine(Player player) {
        String err = validate(player);
        if (err != null) {
            Service.gI().sendThongBao(player, err);
            return;
        }
        Item mTS = player.combineNew.itemsCombine.stream().filter(item -> item.isNotNullItem() && item.isManhTS() && item.quantity >= 999).findFirst().get();
        Item daNC = player.combineNew.itemsCombine.stream().filter(item -> item.isNotNullItem() && item.isDaNangCap1()).findFirst().get();
        Item daMM = player.combineNew.itemsCombine.stream().filter(item -> item.isNotNullItem() && item.isDaMayMan()).findFirst().get();
        int tile = Math.min(100, 90 + (daNC.template.id - 1073));
        String npcSay = "|2|Chế tạo trang bị Thiên Sứ\n"
                + "|1|Tỉ lệ thành công: " + tile + "%\n"
                + "|0|Tiêu hao: 1 Công thức VIP, 999 " + mTS.template.name + ", 1 " + daNC.template.name + ", 1 " + daMM.template.name + "\n"
                + "|7|Thất bại vẫn mất nguyên liệu\n"
                + "|2|Cần " + Util.numberToMoney(GOLD_CHE_TAO) + " vàng";
        CombineService.gI().whis.createOtherMenu(player, CombineService.CHE_TAO_TRANG_BI_THIEN_SU, npcSay, "Chế tạo", "Từ chối");
    }

    // Chạy khi bấm "Chế tạo". Kiểm tra lại toàn bộ vì đồ có thể đã thay đổi sau khi xem.
    public static void cheTao(Player player) {
        String err = validate(player);
        if (err != null) {
            Service.gI().sendThongBao(player, err);
            return;
        }
        Item mTS = player.combineNew.itemsCombine.stream().filter(item -> item.isNotNullItem() && item.isManhTS() && item.quantity >= 999).findFirst().get();
        Item daNC = player.combineNew.itemsCombine.stream().filter(item -> item.isNotNullItem() && item.isDaNangCap1()).findFirst().get();
        Item daMM = player.combineNew.itemsCombine.stream().filter(item -> item.isNotNullItem() && item.isDaMayMan()).findFirst().get();
        Item CtVip = player.combineNew.itemsCombine.stream().filter(item -> item.isNotNullItem() && item.isCongThucVip()).findFirst().get();
        if (InventoryService.gI().getCountEmptyBag(player) <= 0) {
            Service.gI().sendThongBao(player, "Bạn phải có ít nhất 1 ô trống hành trang");
            return;
        }
        if (player.inventory.gold < GOLD_CHE_TAO) {
            Service.gI().sendThongBao(player, "Không đủ vàng để thực hiện");
            return;
        }
        player.inventory.gold -= GOLD_CHE_TAO;

        int tilemacdinh = 90 + (daNC.template.id - 1073);
        int tileLucky = 5;
        tileLucky += tileLucky * (daMM.template.id - 1078);

        if (Util.nextInt(0, 100) < tilemacdinh) {
            tilemacdinh = 100;
            short[][] itemIds = {{1048, 1051, 1054, 1057, 1060}, {1049, 1052, 1055, 1058, 1061}, {1050, 1053, 1056, 1059, 1062}}; // thứ tự td - 0,nm - 1, xd - 2

            Item itemTS = ItemService.gI().DoThienSu(itemIds[CtVip.template.gender > 2 ? player.gender : CtVip.template.gender][mTS.typeIdManh()], CtVip.template.gender);

            for (byte w = 0; w < itemTS.itemOptions.size(); w++) {
                if (itemTS.itemOptions.get(w).optionTemplate.id != 0 && itemTS.itemOptions.get(w).optionTemplate.id != 20) {
                    itemTS.itemOptions.get(w).param += (itemTS.itemOptions.get(w).param * tilemacdinh / 100);
                }
            }
            tilemacdinh = Util.nextInt(0, 50);

            if (tilemacdinh <= tileLucky) {
                if (tilemacdinh >= (tileLucky - 3)) {
                    tileLucky = 3;
                } else if (tilemacdinh <= (tileLucky - 4) && tilemacdinh >= (tileLucky - 10)) {
                    tileLucky = 2;
                } else {
                    tileLucky = 1;
                }
                itemTS.itemOptions.add(new ItemOption(15, tileLucky));
                ArrayList<Integer> listOptionBonus = new ArrayList<>(Arrays.asList(50, 77, 103, 94, 5));
                for (int j = 0; j < tileLucky; j++) {
                    tilemacdinh = Util.nextInt(0, listOptionBonus.size() - 1);
                    itemTS.itemOptions.add(new ItemOption(listOptionBonus.get(tilemacdinh), Util.nextInt(1, 3)));
                    listOptionBonus.remove(tilemacdinh);
                }
            }

            InventoryService.gI().addItemBag(player, itemTS);
            CombineService.gI().sendEffectSuccessCombine(player);
        } else {
            CombineService.gI().sendEffectFailCombine(player);
        }
        InventoryService.gI().subQuantityItemsBag(player, CtVip, 1);
        InventoryService.gI().subQuantityItemsBag(player, daNC, 1);
        InventoryService.gI().subQuantityItemsBag(player, mTS, 999);
        InventoryService.gI().subQuantityItemsBag(player, daMM, 1);

        InventoryService.gI().sendItemBags(player);
        Service.gI().sendMoney(player);
        CombineService.gI().reOpenItemCombine(player);
    }

    public static void CheTaoTS(Player player) {
         if (player.combineNew.itemsCombine.size() != 4) {
            Service.gI().sendThongBao(player, "Thiếu đồ");
            return;
        }
        if (player.inventory.gold < 500_000_000) {
            Service.gI().sendThongBao(player, "Ảo ít thôi con...");
            return;
        }
        if (InventoryService.gI().getCountEmptyBag(player) < 1) {
            Service.gI().sendThongBao(player, "Bạn phải có ít nhất 1 ô trống hành trang");
            return;
        }
        Item itemTL = player.combineNew.itemsCombine.stream().filter(item -> item.isNotNullItem() && item.isDHD()).findFirst().get();
        Item itemManh = player.combineNew.itemsCombine.stream().filter(item -> item.isNotNullItem() && item.isManhTS() && item.quantity >= 5).findFirst().get();

        player.inventory.gold -= 500_000_000;
          CombineService.gI().sendEffectSuccessCombine(player);
        short[][] itemIds = {{1048, 1051, 1054, 1057, 1060}, {1049, 1052, 1055, 1058, 1061}, {1050, 1053, 1056, 1059, 1062}}; // thứ tự td - 0,nm - 1, xd - 2

        Item itemTS = ItemService.gI().DoThienSu(itemIds[itemTL.template.gender > 2 ? player.gender : itemTL.template.gender][itemManh.typeIdManh()], itemTL.template.gender);
        InventoryService.gI().addItemBag(player, itemTS);

        InventoryService.gI().subQuantityItemsBag(player, itemTL, 1);
        InventoryService.gI().subQuantityItemsBag(player, itemManh, 99);
        InventoryService.gI().sendItemBags(player);
        Service.gI().sendMoney(player);
        Service.gI().sendThongBao(player, "Bạn đã nhận được " + itemTS.template.name);
        player.combineNew.itemsCombine.clear();
          CombineService.gI().reOpenItemCombine(player);
    }

    
}
