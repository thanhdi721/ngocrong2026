package nro.models.shop_ky_gui;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import nro.models.consts.ConstNpc;
import nro.models.item.Item;
import nro.models.item.Item.ItemOption;
import nro.models.map.service.NpcService;
import nro.models.network.Message;
import nro.models.player.Inventory;
import nro.models.player.Player;
import nro.models.services.InventoryService;
import nro.models.services.ItemService;
import nro.models.services.Service;

/**
 *
 * @author By Mr Blue
 * 
 */

public class ConsignShopService {

    // FIX: trần giá ký gửi (trước đây các điều kiện kiểm tra dùng && nên vô hiệu)
    private static final int MAX_GOLD_CONSIGN = 200_000_000;
    private static final int MAX_GEM_CONSIGN = 1_000_000;

    private static ConsignShopService instance;

    public static ConsignShopService gI() {
        if (instance == null) {
            instance = new ConsignShopService();
        }
        return instance;
    }

    private List<ConsignItem> getItemKyGui2(Player pl, byte tab, byte to, byte max) {
        List<ConsignItem> filtered = ConsignShopManager.gI().listItem.stream()
                .filter(it -> it != null && it.tab == tab && !it.isBuy)
                .sorted(Comparator.comparingInt((ConsignItem it) -> it.isUpTop).reversed())
                .collect(Collectors.toList());

        List<ConsignItem> result = new ArrayList<>();
        for (int i = to; i <= max && i < filtered.size(); i++) {
            result.add(filtered.get(i));
        }
        return result;
    }

    private List<ConsignItem> getItemKyGui(Player pl, byte tab, byte... max) {
        List<ConsignItem> filtered = ConsignShopManager.gI().listItem.stream()
                .filter(it -> it != null && it.tab == tab && !it.isBuy && it.player_sell != pl.id)
                .sorted(Comparator.comparingInt((ConsignItem it) -> it.isUpTop).reversed())
                .collect(Collectors.toList());

        List<ConsignItem> result = new ArrayList<>();

        if (max.length == 2) {
            int from = max[0], to = max[1];
            for (int i = from; i < to && i < filtered.size(); i++) {
                result.add(filtered.get(i));
            }
        } else if (max.length == 1) {
            int limit = max[0];
            for (int i = 0; i < limit && i < filtered.size(); i++) {
                result.add(filtered.get(i));
            }
        } else {
            return filtered;
        }

        return result;
    }

    private List<ConsignItem> getItemKyGui() {
        return ConsignShopManager.gI().listItem.stream()
                .filter(it -> it != null && !it.isBuy)
                .sorted(Comparator.comparingInt((ConsignItem it) -> it.isUpTop).reversed())
                .collect(Collectors.toList());
    }

    private boolean isKyGui(Item item) {
        switch (item.template.type) {
            case 27:
                switch (item.template.id) {
                    case 921:
                    case 1155:
                    case 1156:
                    case 568:
                        return true;
                }
                return false;
            case 21:
            case 72:
                return true;
        }
        for (int i = 0; i < item.itemOptions.size(); i++) {
            if (item.itemOptions.get(i).optionTemplate.id == 86) {
                return true;
            }
        }
        return false;
    }

    private boolean SubThoiVang(Player pl, int quatity) {
        for (Item item : pl.inventory.itemsBag) {
            if (item.isNotNullItem() && item.template.id == 457 && item.quantity >= quatity) {
                nro.models.services.InventoryService.gI().subQuantityItemsBag(pl, item, quatity);
                return true;
            }
        }
        return false;
    }

    // FIX (46 §Ký gửi): mỗi phiên chạy trên luồng riêng. Trước đây hai người cùng bấm mua một món
    // (hoặc người bán bấm Huỷ đúng lúc người khác mua) thì cả hai cùng thấy "chưa bán" => món bị
    // phát HAI lần (nhân đồ). Nay mua / huỷ / nhận tiền / đăng bán đều khoá chung trên singleton.
    public synchronized void buyItem(Player pl, int id) {
        if (pl.nPoint.power < 17000000000L) {
            Service.gI().sendThongBao(pl, "Yêu cầu sức mạnh lớn hơn 17 tỷ");
            openShopKyGui(pl);
            return;
        }
        ConsignItem it = getItemBuy(id);
        if (it == null || it.isBuy) {
            Service.gI().sendThongBao(pl, "Vật phẩm không tồn tại hoặc đã được bán");
            return;
        }
        if (it.player_sell == pl.id) {
            Service.gI().sendThongBao(pl, "Không thể mua vật phẩm bản thân đăng bán");
            openShopKyGui(pl);
            return;
        }
        // FIX: kiểm tra chỗ trống TRƯỚC khi trừ tiền (trước đây túi đầy => mất tiền, mất đồ).
        if (InventoryService.gI().getCountEmptyBag(pl) == 0) {
            Service.gI().sendThongBao(pl, "Hành trang đã đầy");
            return;
        }
        boolean isBuy = false;
        if (it.goldSell > 0) {
            if (pl.inventory.gold >= it.goldSell) {
                pl.inventory.gold -= it.goldSell;
                isBuy = true;
            } else {
                Service.gI().sendThongBao(pl, "Bạn không đủ vàng để mua vật phẩm");
                isBuy = false;
            }
        } else if (it.gemSell > 0) {
            if (pl.inventory.gem >= it.gemSell) {
                pl.inventory.gem -= it.gemSell;
                isBuy = true;
            } else {
                Service.gI().sendThongBao(pl, "Bạn không đủ Ngọc Xanh để mua vật phẩm này!");
                isBuy = false;
            }
        }
        Service.gI().sendMoney(pl);
        if (isBuy) {
            Item item = ItemService.gI().createNewItem(it.itemId);
            item.quantity = it.quantity;
            item.itemOptions.addAll(it.options);
            it.isBuy = true;
            if (it.isBuy) {
                InventoryService.gI().addItemBag(pl, item);
                InventoryService.gI().sendItemBags(pl);
                Service.gI().sendThongBao(pl, "Bạn đã nhận được " + item.template.name);
                ConsignShopManager.gI().save();
                openShopKyGui(pl);
            }
        }
    }

    public ConsignItem getItemBuy(int id) {
        for (ConsignItem it : getItemKyGui()) {
            if (it != null && it.id == id) {
                return it;
            }
        }
        return null;
    }

    public ConsignItem getItemBuy(Player pl, int id) {
        for (ConsignItem it : ConsignShopManager.gI().listItem) {
            if (it != null && it.id == id && it.player_sell == pl.id) {
                return it;
            }
        }
        return null;
    }

    public void openShopKyGui(Player pl, byte index, int page) {
        if (page > getItemKyGui(pl, index).size()) {
            return;
        }
        Message msg = null;
        try {
            msg = new Message(-100);
            msg.writer().writeByte(index);
            List<ConsignItem> items = getItemKyGui(pl, index);
            List<ConsignItem> itemsSend = getItemKyGui2(pl, index, (byte) (page * 20), (byte) (page * 20 + 20));
            byte tab = (byte) (items.size() / 20 > 0 ? (items.size() / 20) + 1 : 1);
            msg.writer().writeByte(tab); // max page
            msg.writer().writeByte(page);
            msg.writer().writeByte(itemsSend.size());
            for (int j = 0; j < itemsSend.size(); j++) {
                ConsignItem itk = itemsSend.get(j);
                Item it = ItemService.gI().createNewItem(itk.itemId);
                it.itemOptions.clear();
                if (itk.options.isEmpty()) {
                    it.itemOptions.add(new ItemOption(73, 0));
                } else {
                    it.itemOptions.addAll(itk.options);
                }
                msg.writer().writeShort(it.template.id);
                msg.writer().writeShort(itk.id);
                msg.writer().writeInt(itk.goldSell);
                msg.writer().writeInt(itk.gemSell);
                msg.writer().writeByte(0); // buy type
                if (pl.getSession().version >= 222) {
                    msg.writer().writeInt(itk.quantity);
                } else {
                    msg.writer().writeByte(itk.quantity);
                }
                msg.writer().writeByte(itk.player_sell == pl.id ? 1 : 0); // isMe
                msg.writer().writeByte(it.itemOptions.size());
                for (int a = 0; a < it.itemOptions.size(); a++) {
                    msg.writer().writeByte(it.itemOptions.get(a).optionTemplate.id);
                    msg.writer().writeShort(it.itemOptions.get(a).param);
                }
                msg.writer().writeByte(0);
            }
            pl.sendMessage(msg);
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (msg != null) {
                msg.cleanup();
                msg = null;
            }
        }
    }

    public void upItemToTop(Player pl, int id) {
        ConsignItem it = getItemBuy(id);
        if (it == null || it.isBuy) {
            Service.gI().sendThongBao(pl, "Vật phẩm không tồn tại hoặc đã được bán");
            return;
        }
        if (it.player_sell != pl.id) {
            Service.gI().sendThongBao(pl, "Vật phẩm không thuộc quyền sở hữu");
            openShopKyGui(pl);
            return;
        }
        pl.idMark.setIdItemUpTop(id);
        NpcService.gI().createMenuConMeo(pl, ConstNpc.UP_TOP_ITEM, -1, "Bạn có muốn đưa vật phẩm ['" + ItemService.gI().createNewItem(it.itemId).template.name + "'] của bản thân lên trang đầu?\nYêu cầu 5 Ngọc Xanh.", "Đồng ý", "Từ Chối");
    }

    public void StartupItemToTop(Player pl) {
        if (!SubThoiVang(pl, 2)) {
            Service.gI().sendThongBao(pl, "Bạn cần có ít nhất 2 thỏi vàng đưa vật phẩm lên trang đầu");
            return;
        }

        for (ConsignItem its : ConsignShopManager.gI().listItem) {
            if (its.id == pl.idMark.getIdItemUpTop()) {
                its.isUpTop = 1;
                Service.gI().sendThongBao(pl, "Đưa vật phẩm lên trang đầu thành công");
                break;
            }
        }

        ConsignShopManager.gI().listItem.sort(Comparator.comparingInt((ConsignItem it) -> it.isUpTop).reversed());

        ConsignShopManager.gI().save();
        openShopKyGui(pl);
    }

    public synchronized void claimOrDel(Player pl, byte action, int id) {
        ConsignItem it = getItemBuy(pl, id);
        switch (action) {
            case 1: // hủy vật phẩm
                if (it == null || it.isBuy) {
                    Service.gI().sendThongBao(pl, "Vật phẩm không tồn tại hoặc đã được bán");
                    return;
                }
                if (it.player_sell != pl.id) {
                    Service.gI().sendThongBao(pl, "Vật phẩm không thuộc quyền sở hữu");
                    openShopKyGui(pl);
                    return;
                }
                // FIX: túi đầy thì addItemBag thất bại => đồ bị xoá khỏi ký gửi mà không về túi.
                if (InventoryService.gI().getCountEmptyBag(pl) == 0) {
                    Service.gI().sendThongBao(pl, "Hành trang đã đầy");
                    return;
                }
                Item item = ItemService.gI().createNewItem(it.itemId);
                item.quantity = it.quantity;
                item.itemOptions.addAll(it.options);
                if (ConsignShopManager.gI().listItem.remove(it)) {
                    InventoryService.gI().addItemBag(pl, item);
                    InventoryService.gI().sendItemBags(pl);
                    Service.gI().sendMoney(pl);
                    ConsignShopManager.gI().save();
                    Service.gI().sendThongBao(pl, "Hủy bán vật phẩm thành công");
                    openShopKyGui(pl);
                }
                break;
            case 2: // nhận tiền
                if (it == null || !it.isBuy) {
                    Service.gI().sendThongBao(pl, "Vật phẩm không tồn tại hoặc chưa được bán");
                    return;
                }
                if (it.player_sell != pl.id) {
                    Service.gI().sendThongBao(pl, "Vật phẩm không thuộc quyền sở hữu");
                    openShopKyGui(pl);
                    return;
                }
                if (it.goldSell > 0) {
                    // FIX: người mua trả vàng nên người bán phải nhận lại vàng (trước đây nhận Thỏi vàng => tạo vàng vô hạn)
                    long goldReceive = (long) it.goldSell - (long) it.goldSell * 10 / 100;
                    pl.inventory.gold += goldReceive;
                    if (pl.inventory.gold > Inventory.LIMIT_GOLD) {
                        pl.inventory.gold = Inventory.LIMIT_GOLD;
                    }
                } else if (it.gemSell > 0) {
                    // FIX: gem là int — chặn tràn số.
                    pl.inventory.gem = (int) Math.min((long) pl.inventory.gem + (it.gemSell - it.gemSell * 10 / 100), 2_000_000_000L);
                }
                if (ConsignShopManager.gI().listItem.remove(it)) {
                    Service.gI().sendMoney(pl);
                    ConsignShopManager.gI().save();
                    Service.gI().sendThongBao(pl, "Bạn đã bán vật phẩm thành công");
                    openShopKyGui(pl);

                }
                break;
        }
    }

    public List<ConsignItem> getItemCanKiGui(Player pl) {
        List<ConsignItem> its = new ArrayList<>();
        ConsignShopManager.gI().listItem.stream().filter((it) -> (it != null && it.player_sell == pl.id)).forEachOrdered((it) -> {
            its.add(it);
        });
        pl.inventory.itemsBag.stream().filter((it) -> (itemCanConsign(it))).forEachOrdered((it) -> {
            its.add(new ConsignItem(InventoryService.gI().getIndexBag(pl, it), it.template.id, (int) pl.id, (byte) 4, -1, -1, it.quantity, (byte) -1, it.itemOptions, false));
        });
        return its;
    }

    public boolean itemCanConsign(Item it) {
        if (it != null && it.template != null) {
            if (it.itemOptions.stream().anyMatch(op -> op.optionTemplate.id == 86)
                    || it.itemOptions.stream().anyMatch(op -> op.optionTemplate.id == 87)
                    || it.template.type == 14
                    || it.template.type == 15
                    || it.template.type == 6
                    || it.template.id >= 14 && it.template.id <= 20) {
                return true;
            }
        }
        return false;
    }

    public int getMaxId() {
        try {
            List<Integer> id = new ArrayList<>();
            ConsignShopManager.gI().listItem.stream().filter((it) -> (it != null)).forEachOrdered((it) -> {
                id.add(it.id);
            });

            if (id.isEmpty()) {
                return 0;
            }

            return Collections.max(id);
        } catch (Exception e) {
            e.printStackTrace();
            return 0;
        }
    }

    public byte getTabKiGui(Item it) {
        if (it.template.type >= 0 && it.template.type <= 2) {
            return 0;
        } else if ((it.template.type >= 3 && it.template.type <= 4)) {
            return 1;
        } else if (it.template.type == 29) {
            return 2;
        } else {
            return 3;
        }
    }

    public synchronized void KiGui(Player pl, int id, int money, byte moneyType, int quantity) {
        try {
            // FIX: kiểm tra ô hành trang / món hợp lệ TRƯỚC khi thu phí (trước đây thu 1 Thỏi vàng rồi
            // mới kiểm tra => gửi chỉ số sai là mất phí), và chỉ nhận món mà giao diện cho phép ký gửi
            // (trước đây client sửa gói gửi được BẤT KỲ món nào trong túi, kể cả món không giao dịch được).
            if (id < 0 || id >= pl.inventory.itemsBag.size()) {
                openShopKyGui(pl);
                return;
            }
            Item goc = pl.inventory.itemsBag.get(id);
            if (goc == null || !goc.isNotNullItem() || !itemCanConsign(goc)) {
                Service.gI().sendThongBao(pl, "Vật phẩm không thể kí gửi");
                openShopKyGui(pl);
                return;
            }
            if (goc.template.id == 457) {
                // Thỏi vàng vừa là món vừa là phí: phải còn đủ cả hai.
                if (goc.quantity < (long) quantity + 1) {
                    Service.gI().sendThongBao(pl, "Không đủ Thỏi vàng");
                    openShopKyGui(pl);
                    return;
                }
            }
            if (money <= 0 || quantity < 1 || quantity > 99 || quantity > goc.quantity
                    || (moneyType == 0 && money > MAX_GOLD_CONSIGN) || (moneyType == 1 && money > MAX_GEM_CONSIGN)
                    || (moneyType != 0 && moneyType != 1)) {
                Service.gI().sendThongBao(pl, "Thông tin ký gửi không hợp lệ");
                openShopKyGui(pl);
                return;
            }
            if (!SubThoiVang(pl, 1)) {
                Service.gI().sendThongBao(pl, "Bạn cần có ít nhất 1 thỏi vàng để làm phí đăng bán");
                return;
            }

            Item it = ItemService.gI().copyItem(pl.inventory.itemsBag.get(id));
            for (Item.ItemOption daubuoi : it.itemOptions) {
                if (daubuoi.optionTemplate.id == 30) {
                    Service.gI().sendThongBao(pl, "Vật phẩm không thể kí gửi");
                    openShopKyGui(pl);
                    return;
                }
            }
            if (money <= 0 || quantity > it.quantity) {
                openShopKyGui(pl);
                return;
            }

            // FIX: điều kiện cũ dùng && nên không bao giờ đúng => giới hạn số lượng vô hiệu
            if (quantity < 1 || quantity > 99) {
                Service.gI().sendThongBao(pl, "Ký gửi tối đa x99");
                openShopKyGui(pl);
                return;
            }
            switch (moneyType) {
                case 0:// vàng
                    // FIX: điều kiện cũ dùng && nên không bao giờ đúng => không có giới hạn giá.
                    // Mốc trần lấy theo hướng dẫn của NPC Ký gửi ("10k-200Tr vàng").
                    if (money < 1 || money > MAX_GOLD_CONSIGN) {
                        Service.gI().sendThongBao(pl, "Giá ký gửi bằng vàng phải từ 1 đến 200.000.000");
                    } else {
                        InventoryService.gI().subQuantityItemsBag(pl, pl.inventory.itemsBag.get(id), quantity);
                        ConsignShopManager.gI().listItem.add(new ConsignItem(getMaxId() + 1, it.template.id, (int) pl.id, getTabKiGui(it), money, -1, quantity, (byte) 0, it.itemOptions, false));
                        InventoryService.gI().sendItemBags(pl);
                        openShopKyGui(pl);
                        Service.gI().sendMoney(pl);
                        Service.gI().sendThongBao(pl, "Đăng bán thành công");
                        ConsignShopManager.gI().save();
                    }
                    break;
                case 1:// Ngọc Xanh
                    // FIX: điều kiện cũ dùng && nên không bao giờ đúng => không có giới hạn giá
                    if (money < 1 || money > MAX_GEM_CONSIGN) {
                        Service.gI().sendThongBao(pl, "không thể ký gửi quá 1000000 ngọc");
                    } else {
                        InventoryService.gI().subQuantityItemsBag(pl, pl.inventory.itemsBag.get(id), quantity);
                        ConsignShopManager.gI().listItem.add(new ConsignItem(getMaxId() + 1, it.template.id, (int) pl.id, getTabKiGui(it), -1, money, quantity, (byte) 0, it.itemOptions, false));
                        InventoryService.gI().sendItemBags(pl);
                        openShopKyGui(pl);
                        Service.gI().sendMoney(pl);
                        Service.gI().sendThongBao(pl, "Đăng bán thành công");
                        ConsignShopManager.gI().save();
                    }

                    break;
                default:
                    Service.gI().sendThongBao(pl, "Có lỗi xảy ra");
                    openShopKyGui(pl);
                    break;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void openShopKyGui(Player pl) {
        Message msg = null;
        try {
            msg = new Message(-44);
            msg.writer().writeByte(2);
            msg.writer().writeByte(5);
            for (byte i = 0; i < 5; i++) {
                if (i == 4) {
                    msg.writer().writeUTF(ConsignShopManager.gI().tabName[i]);
                    msg.writer().writeByte(0);
                    msg.writer().writeByte(getItemCanKiGui(pl).size());
                    for (int j = 0; j < getItemCanKiGui(pl).size(); j++) {
                        ConsignItem itk = getItemCanKiGui(pl).get(j);
                        if (itk == null) {
                            continue;
                        }
                        Item it = ItemService.gI().createNewItem(itk.itemId);
                        it.itemOptions.clear();
                        if (itk.options.isEmpty()) {
                            it.itemOptions.add(new ItemOption(73, 0));
                        } else {
                            it.itemOptions.addAll(itk.options);
                        }
                        msg.writer().writeShort(it.template.id);
                        msg.writer().writeShort(itk.id);
                        msg.writer().writeInt(itk.goldSell);
                        msg.writer().writeInt(itk.gemSell);
                        if (getItemBuy(pl, itk.id) == null) {
                            msg.writer().writeByte(0); // buy type
                        } else if (itk.isBuy) {
                            msg.writer().writeByte(2);
                        } else {
                            msg.writer().writeByte(1);
                        }
                        msg.writer().writeInt(itk.quantity);
                        msg.writer().writeByte(1); // isMe
                        msg.writer().writeByte(it.itemOptions.size());
                        for (int a = 0; a < it.itemOptions.size(); a++) {
                            msg.writer().writeByte(it.itemOptions.get(a).optionTemplate.id);
                            msg.writer().writeShort(it.itemOptions.get(a).param);
                        }
                        msg.writer().writeByte(0);
                        msg.writer().writeByte(0);
                    }
                } else {
                    List<ConsignItem> items = getItemKyGui(pl, i);
                    List<ConsignItem> itemsSend = getItemKyGui2(pl, i, (byte) 0, (byte) 20);
                    msg.writer().writeUTF(ConsignShopManager.gI().tabName[i]);
                    byte tab = (byte) (items.size() / 20 > 0 ? (items.size() / 20) + 1 : 1);
                    msg.writer().writeByte(tab); // max page
                    msg.writer().writeByte(itemsSend.size());
                    for (int j = 0; j < itemsSend.size(); j++) {
                        ConsignItem itk = itemsSend.get(j);
                        Item it = ItemService.gI().createNewItem(itk.itemId);
                        it.itemOptions.clear();
                        if (itk.options.isEmpty()) {
                            it.itemOptions.add(new ItemOption(73, 0));
                        } else {
                            it.itemOptions.addAll(itk.options);
                        }
                        msg.writer().writeShort(it.template.id);
                        msg.writer().writeShort(itk.id);
                        msg.writer().writeInt(itk.goldSell);
                        msg.writer().writeInt(itk.gemSell);
                        msg.writer().writeByte(0); // buy type
                        msg.writer().writeInt(itk.quantity);
                        msg.writer().writeByte(itk.player_sell == pl.id ? 1 : 0); // isMe     
                        msg.writer().writeByte(it.itemOptions.size());
                        for (int a = 0; a < it.itemOptions.size(); a++) {
                            msg.writer().writeByte(it.itemOptions.get(a).optionTemplate.id);
                            msg.writer().writeShort(it.itemOptions.get(a).param);
                        }
                        msg.writer().writeByte(0);
                        msg.writer().writeByte(0);
                    }
                }
            }
            pl.sendMessage(msg);
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (msg != null) {
                msg.cleanup();
                msg = null;
            }
        }
    }
}
