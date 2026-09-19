package nro.models.services_func;

import nro.models.database.HistoryTransactionDAO;
import nro.models.item.Item;
import nro.models.player.Inventory;
import nro.models.player.Player;
import nro.models.network.Message;
import nro.models.services.ItemService;
import nro.models.services.PlayerService;
import nro.models.services.Service;
import nro.models.services.InventoryService;
import nro.models.utils.Logger;
import nro.models.utils.Util;
import java.util.ArrayList;
import java.util.List;
import nro.models.Bot.Bot;
import nro.models.server.ServerManager;

/**
 *
 * @author By Mr Blue
 * 
 */

public class Trade {

    public static final int TIME_TRADE = 180000;
    public static final int QUANLITY_MAX = 2_000_000_000;
    public static final int MAX_GOLD_TRADE_PER_TIME = 10_000_000;

    private Player player1;
    private Player player2;

    private long gold1Before;
    private long gold2Before;
    private List<Item> bag1Before;
    private List<Item> bag2Before;

    private List<Item> itemsBag1;
    private List<Item> itemsBag2;

    private List<Item> itemsTrade1;
    private List<Item> itemsTrade2;
    private int goldTrade1;
    private int goldTrade2;

    public byte accept;
    // FIX (46 §Trade): theo dõi khoá / đồng ý THEO TỪNG NGƯỜI. Trước đây `accept++` mỗi lần có gói
    // ACCEPT bất kể ai gửi => một người gửi ACCEPT 2 lần là giao dịch chạy mà đối phương chưa đồng ý;
    // và không có trạng thái khoá => sau khi khoá (đối phương đã xem) vẫn đổi được số vàng.
    private boolean locked1;
    private boolean locked2;
    private boolean accepted1;
    private boolean accepted2;

    private long lastTimeStart;
    private boolean start;

    public Trade(Player pl1, Player pl2) {
        this.player1 = pl1;
        this.player2 = pl2;
        this.gold1Before = pl1.inventory.gold;
        this.gold2Before = pl2.inventory.gold;
        this.bag1Before = InventoryService.gI().copyItemsBag(player1);
        this.bag2Before = InventoryService.gI().copyItemsBag(player2);
        this.itemsBag1 = InventoryService.gI().copyItemsBag(player1);
        this.itemsBag2 = InventoryService.gI().copyItemsBag(player2);
        this.itemsTrade1 = new ArrayList<>();
        this.itemsTrade2 = new ArrayList<>();
        TransactionService.PLAYER_TRADE.put(pl1, this);
        TransactionService.PLAYER_TRADE.put(pl2, this);
    }

    public void openTabTrade() {
        player1.idMark.setAcpTrade(true);
        player2.idMark.setAcpTrade(true);
        this.lastTimeStart = System.currentTimeMillis();
        this.start = true;
        Message msg = null;
        try {
            msg = new Message(-86);
            msg.writer().writeByte(1);
            msg.writer().writeInt((int) player1.id);
            player2.sendMessage(msg);
            msg.cleanup();
            msg = new Message(-86);
            msg.writer().writeByte(1);
            msg.writer().writeInt((int) player2.id);
            player1.sendMessage(msg);
        } catch (Exception ignored) {
        } finally {
            if (msg != null) {
                msg.cleanup();
            }
        }
    }

    public void addItemTrade(Player pl, byte index, int quantity) {
        // FIX: đã khoá thì không được thêm đồ / đổi số vàng nữa (đối phương đã xem bảng khoá).
        if ((pl.equals(this.player1) && locked1) || (pl.equals(this.player2) && locked2)) {
            Service.gI().sendThongBao(pl, "Đã khoá giao dịch, không thể thay đổi");
            return;
        }
        if (pl.getSession().actived) {
            if (index == -1) { // Giao dịch vàng
                if (quantity > MAX_GOLD_TRADE_PER_TIME || quantity < 0) {
                    Service.gI().sendThongBao(pl, "Số vàng giao dịch không được vượt quá " + MAX_GOLD_TRADE_PER_TIME + " vàng.");
                    sendUpdateGoldTrade(pl); // Cập nhật lại số vàng hiển thị về 0 hoặc giá trị hợp lệ
                    return;
                }
                // FIX: trước đây KHÔNG kiểm tra người đưa có đủ vàng => nick 0 vàng đưa 10 triệu,
                // đối phương nhận đủ 10 triệu, nick đưa bị âm vàng. Lặp lại = in vàng vô hạn.
                if (pl.inventory.gold < quantity) {
                    Service.gI().sendThongBao(pl, "Không đủ vàng để giao dịch");
                    sendUpdateGoldTrade(pl);
                    return;
                }
                if (pl.equals(this.player1)) {
                    goldTrade1 = quantity;
                } else {
                    goldTrade2 = quantity;
                }
            } else { // Giao dịch vật phẩm
                Item item = null;
                if (pl.equals(this.player1)) {
                    item = itemsBag1.get(index);
                } else {
                    item = itemsBag2.get(index);
                }
                if (item.template.id == 570) {
                    Service.gI().sendThongBao(pl, "Không thể giao dịch Rương Gỗ");
                    removeItemTrade2(pl, index); // FIX: trước đây chỉ báo mà vẫn cho giao dịch
                    return;
                }
                if (quantity > item.quantity || quantity < 0) {
                    return;
                }
                if (isItemCannotTran(item)) {
                    removeItemTrade(pl, index);
                } else {
                    if (quantity > 99) {
                        int n = quantity / 99;
                        int left = quantity % 99;
                        for (int i = 0; i < n; i++) {
                            Item itemTrade = ItemService.gI().copyItem(item);
                            itemTrade.quantity = 99;
                            itemTrade.quantityGD = itemTrade.quantity;
                            if (pl.equals(this.player1)) {
                                InventoryService.gI().subQuantityItem(itemsBag1, item, itemTrade.quantity);
                                itemsTrade1.add(itemTrade);
                            } else {
                                InventoryService.gI().subQuantityItem(itemsBag2, item, itemTrade.quantity);
                                itemsTrade2.add(itemTrade);
                            }
                        }
                        if (left > 0) {
                            Item itemTrade = ItemService.gI().copyItem(item);
                            itemTrade.quantity = left;
                            itemTrade.quantityGD = itemTrade.quantity;
                            if (pl.equals(this.player1)) {
                                InventoryService.gI().subQuantityItem(itemsBag1, item, itemTrade.quantity);
                                itemsTrade1.add(itemTrade);
                            } else {
                                InventoryService.gI().subQuantityItem(itemsBag2, item, itemTrade.quantity);
                                itemsTrade2.add(itemTrade);
                            }
                        }
                    } else {
                        Item itemTrade = ItemService.gI().copyItem(item);
                        itemTrade.quantity = quantity != 0 ? quantity : 1;
                        itemTrade.quantityGD = itemTrade.quantity;
                        if (pl.equals(this.player1)) {
                            InventoryService.gI().subQuantityItem(itemsBag1, item, itemTrade.quantity);
                            itemsTrade1.add(itemTrade);
                        } else {
                            InventoryService.gI().subQuantityItem(itemsBag2, item, itemTrade.quantity);
                            itemsTrade2.add(itemTrade);
                        }
                    }
                }
            }
        } else {
            Service.gI().sendThongBaoFromAdmin(pl,
                    "|5|VUI LÒNG KÍCH HOẠT TÀI KHOẢN TẠI\n|7|Liên Hệ Admin\n|5 ĐỂ MỞ GIAO DỊCH");
            removeItemTrade(pl, index);
        }
    }

    private void sendUpdateGoldTrade(Player pl) {
        Message msg = null;
        try {
            msg = new Message(-86);
            msg.writer().writeByte(5); // Mã lệnh để cập nhật vàng giao dịch
            if (pl.equals(player1)) {
                msg.writer().writeInt(0); // Đặt vàng về 0
            } else {
                msg.writer().writeInt(0); // Đặt vàng về 0
            }
            pl.sendMessage(msg);
        } catch (Exception e) {
            Logger.logException(Trade.class, e);
        } finally {
            if (msg != null) {
                msg.cleanup();
            }
        }
    }
    
    // Các phương thức khác giữ nguyên
    private void removeItemTrade(Player pl, byte index) {
        Message msg = null;
        try {
            msg = new Message(-86);
            msg.writer().writeByte(2);
            msg.writer().write(index);
            pl.sendMessage(msg);
            Service.gI().sendThongBao(pl, "Không thể giao dịch vật phẩm này");
        } catch (Exception e) {
        } finally {
            if (msg != null) {
                msg.cleanup();
            }
        }
    }

    private void removeItemTrade2(Player pl, byte index) {
        Message msg = null;
        try {
            msg = new Message(-86);
            msg.writer().writeByte(2);
            msg.writer().write(index);
            pl.sendMessage(msg);
        } catch (Exception e) {
        } finally {
            if (msg != null) {
                msg.cleanup();
            }
        }
    }

    private boolean isItemCannotTran(Item item) {
        for (Item.ItemOption io : item.itemOptions) {
            if (io.optionTemplate.id == 30) {
                return true;
            }
        }
        switch (item.template.id) {
            case 454:
            case 921:
                return true;
        }
        switch (item.template.type) {
            case 27: //
                if (item.template.id == 590) {
                    return true;
                } else {
                    return false;
                }
            case 5: //cải trang
            case 6: //đậu thần
            case 7: //sách skill
            case 8: //vật phẩm nhiệm vụ
            case 11: //flag bag
            case 13: //bùa
            case 22: //vệ tinh
            case 23: //ván bay
            case 24: //ván bay vip
            case 28: //cờ
            case 31: //bánh trung thu, bánh tết
            case 32: //giáp tập luyện
                return true;
            default:
                return false;
        }
    }

    public void cancelTrade() {
        String notifiText = "Giao dịch bị hủy bỏ";
        Service.gI().sendThongBao(player1, notifiText);
        Service.gI().sendThongBao(player2, notifiText);
        closeTab();
        dispose();
    }

    private void closeTab() {
        Message msg = null;
        try {
            msg = new Message(-86);
            msg.writer().writeByte(7);
            player1.sendMessage(msg);
            player2.sendMessage(msg);
        } catch (Exception e) {
        } finally {
            if (msg != null) {
                msg.cleanup();
            }
        }
    }

    public void dispose() {
        player1.idMark.setPlayerTradeId(-1);
        player2.idMark.setPlayerTradeId(-1);
        TransactionService.PLAYER_TRADE.remove(player1);
        TransactionService.PLAYER_TRADE.remove(player2);
        this.player1 = null;
        this.player2 = null;
        this.itemsBag1 = null;
        this.itemsBag2 = null;
        this.itemsTrade1 = null;
        this.itemsTrade2 = null;
    }

    public void lockTran(Player pl) {
        // FIX: ghi nhận khoá theo từng người (xem khai báo locked1/locked2).
        if (pl.equals(player1)) {
            locked1 = true;
        } else if (pl.equals(player2)) {
            locked2 = true;
        }
        Message msg = null;
        try {
            msg = new Message(-86);
            msg.writer().writeByte(6);
            if (pl.equals(player1)) {
                msg.writer().writeInt(goldTrade1);
                msg.writer().writeByte(itemsTrade1.size());
                for (Item item : itemsTrade1) {
                    msg.writer().writeShort(item.template.id);
                    if (player1.getSession().version < 222) {
                        msg.writer().writeByte(item.quantity > Byte.MAX_VALUE ? Byte.MAX_VALUE : item.quantity);
                    } else {
                        msg.writer().writeInt(item.quantity);
                    }
                    msg.writer().writeByte(item.itemOptions.size());
                    for (Item.ItemOption io : item.itemOptions) {
                        msg.writer().writeByte(io.optionTemplate.id);
                        msg.writer().writeShort(io.param);
                    }
                }
                player2.sendMessage(msg);
            } else {
                msg.writer().writeInt(goldTrade2);
                msg.writer().writeByte(itemsTrade2.size());
                for (Item item : itemsTrade2) {
                    msg.writer().writeShort(item.template.id);
                    if (player2.getSession().version < 222) {
                        msg.writer().writeByte(item.quantity > Byte.MAX_VALUE ? Byte.MAX_VALUE : item.quantity);
                    } else {
                        msg.writer().writeInt(item.quantity);
                    }
                    msg.writer().writeByte(item.itemOptions.size());
                    for (Item.ItemOption io : item.itemOptions) {
                        msg.writer().writeByte(io.optionTemplate.id);
                        msg.writer().writeShort(io.param);
                    }
                }
                player1.sendMessage(msg);
            }
        } catch (Exception e) {
            Logger.logException(Trade.class, e);
        } finally {
            if (msg != null) {
                msg.cleanup();
            }
            if (player2.isBot) {
                if (pl.equals(player1)) {
                    ((Bot) player2).shop.CheckTraDe(itemsTrade1);
                }
            }
        }
    }

    /**
     * FIX: mỗi người chỉ được tính đồng ý MỘT lần và chỉ sau khi CẢ HAI đã khoá.
     * Trước đây `accept++` không phân biệt ai gửi.
     */
    public void acceptTrade(Player pl) {
        if (!locked1 || !locked2) {
            Service.gI().sendThongBao(pl, "Cả hai bên cần khoá giao dịch trước");
            return;
        }
        if (pl.equals(player1)) {
            accepted1 = true;
        } else if (pl.equals(player2)) {
            accepted2 = true;
        } else {
            return;
        }
        this.accept = (byte) ((accepted1 ? 1 : 0) + (accepted2 ? 1 : 0));
        if (this.accept == 2) {
            this.startTrade();
        }
    }

    /**
     * FIX: so hành trang THẬT với ảnh chụp lúc mở giao dịch. Giao dịch chạy trên bản sao
     * hành trang rồi GHI ĐÈ lên hành trang thật; trước đây nếu trong lúc giao dịch người chơi
     * tiêu / bán / đổi đồ qua đường không bị chặn (menu NPC, form nhập "bán Thỏi vàng",
     * nâng cấp...) thì khi giao dịch xong đồ đó được "hồi sinh" => nhân đồ / nhân Thỏi vàng.
     */
    private static boolean sameBag(List<Item> now, List<Item> before) {
        if (now == null || before == null || now.size() != before.size()) {
            return false;
        }
        for (int i = 0; i < now.size(); i++) {
            Item a = now.get(i);
            Item b = before.get(i);
            boolean na = a == null || !a.isNotNullItem();
            boolean nb = b == null || !b.isNotNullItem();
            if (na || nb) {
                if (na != nb) {
                    return false;
                }
                continue;
            }
            if (a.template.id != b.template.id || a.quantity != b.quantity
                    || !InventoryService.checkListsEqual(a.itemOptions, b.itemOptions)) {
                return false;
            }
        }
        return true;
    }

    private void startTrade() {
        byte tradeStatus = SUCCESS;
        // FIX: kiểm tra lại đủ vàng lúc chốt (vàng có thể bị tiêu sau khi đặt) và hành trang
        // không bị đổi trong lúc giao dịch (xem sameBag).
        if (player1.inventory.gold < goldTrade1 || player2.inventory.gold < goldTrade2) {
            tradeStatus = FAIL_NOT_ENOUGH_GOLD;
        } else if (!sameBag(player1.inventory.itemsBag, bag1Before)
                || (!player2.isBot && !sameBag(player2.inventory.itemsBag, bag2Before))) {
            tradeStatus = FAIL_BAG_CHANGED;
        } else if (player1.inventory.gold + goldTrade2 > Inventory.LIMIT_GOLD) {
            tradeStatus = FAIL_MAX_GOLD_PLAYER1;
        } else if (player2.inventory.gold + goldTrade1 > Inventory.LIMIT_GOLD) {
            tradeStatus = FAIL_MAX_GOLD_PLAYER2;
        }
        if (tradeStatus != SUCCESS) {
            sendNotifyTrade(tradeStatus);
        } else {
            for (Item item : itemsTrade1) {
                if (!player2.isBot) {
                    if (!InventoryService.gI().addItemList(itemsBag2, item)) {
                        tradeStatus = FAIL_NOT_ENOUGH_BAG_P1;
                        break;
                    }
                }
            }
            if (tradeStatus != SUCCESS) {
                sendNotifyTrade(tradeStatus);
            } else {
                for (Item item : itemsTrade2) {
                    // FIX: trước đây điều kiện là !player2.isBot => giao dịch với bot bán đồ, người
                    // chơi mất đồ đưa mà KHÔNG nhận được đồ của bot. player1 luôn là người thật.
                    {
                        if (!InventoryService.gI().addItemList(itemsBag1, item)) {
                            tradeStatus = FAIL_NOT_ENOUGH_BAG_P2;
                            break;
                        }
                    }
                }
                if (tradeStatus == SUCCESS) {
                    player1.inventory.gold += goldTrade2;
                    player2.inventory.gold += goldTrade1;
                    player1.inventory.gold -= goldTrade1;
                    player2.inventory.gold -= goldTrade2;
                    player1.inventory.itemsBag = itemsBag1;
                    player2.inventory.itemsBag = itemsBag2;

                    InventoryService.gI().sendItemBags(player1);
                    InventoryService.gI().sendItemBags(player2);
                    PlayerService.gI().sendInfoHpMpMoney(player1);
                    PlayerService.gI().sendInfoHpMpMoney(player2);

                    HistoryTransactionDAO.insert(player1, player2, goldTrade1, goldTrade2, itemsTrade1, itemsTrade2,
                            bag1Before, bag2Before, this.player1.inventory.itemsBag, this.player2.inventory.itemsBag,
                            gold1Before, gold2Before, this.player1.inventory.gold, this.player2.inventory.gold);
                }
                sendNotifyTrade(tradeStatus);
            }
        }

    }

    private static final byte SUCCESS = 0;
    private static final byte FAIL_MAX_GOLD_PLAYER1 = 1;
    private static final byte FAIL_MAX_GOLD_PLAYER2 = 2;
    private static final byte FAIL_NOT_ENOUGH_BAG_P1 = 3;
    private static final byte FAIL_NOT_ENOUGH_BAG_P2 = 4;
    private static final byte FAIL_ACTVIE = 5;
    private static final byte FAIL_NOT_ENOUGH_GOLD = 6;
    private static final byte FAIL_BAG_CHANGED = 7;

    private void sendNotifyTrade(byte status) {
        player1.idMark.setLastTimeTrade(System.currentTimeMillis());
        player2.idMark.setLastTimeTrade(System.currentTimeMillis());
        switch (status) {
            case SUCCESS:
                Service.gI().sendThongBao(player1, "Giao dịch thành công");
                Service.gI().sendThongBao(player2, "Giao dịch thành công");
                break;
            case FAIL_MAX_GOLD_PLAYER1:
                Service.gI().sendThongBao(player1, "Giao dịch thất bại do số lượng vàng sau giao dịch vượt tối đa");
                Service.gI().sendThongBao(player2, "Giao dịch thất bại do số lượng vàng " + player1.name + " sau giao dịch vượt tối đa");
                break;
            case FAIL_MAX_GOLD_PLAYER2:
                Service.gI().sendThongBao(player2, "Giao dịch thất bại do số lượng vàng sau giao dịch vượt tối đa");
                Service.gI().sendThongBao(player1, "Giao dịch thất bại do số lượng vàng " + player2.name + " sau giao dịch vượt tối đa");
                break;
            case FAIL_NOT_ENOUGH_BAG_P1:
                Service.gI().sendThongBao(player1, "Giao dịch thất bại vì " + player1.name + " không đủ chỗ chứa");
                Service.gI().sendThongBao(player2, "Giao dịch thất bại vì " + player1.name + " không đủ chỗ chứa");
                break;
            case FAIL_NOT_ENOUGH_BAG_P2:
                Service.gI().sendThongBao(player1, "Giao dịch thất bại vì " + player2.name + " không đủ chỗ chứa");
                Service.gI().sendThongBao(player2, "Giao dịch thất bại vì " + player2.name + " không đủ chỗ chứa");
                break;
            case FAIL_NOT_ENOUGH_GOLD:
                Service.gI().sendThongBao(player1, "Giao dịch thất bại vì một bên không đủ vàng");
                Service.gI().sendThongBao(player2, "Giao dịch thất bại vì một bên không đủ vàng");
                break;
            case FAIL_BAG_CHANGED:
                Service.gI().sendThongBao(player1, "Giao dịch thất bại vì hành trang đã thay đổi trong lúc giao dịch");
                Service.gI().sendThongBao(player2, "Giao dịch thất bại vì hành trang đã thay đổi trong lúc giao dịch");
                break;
            case FAIL_ACTVIE:
                Service.gI().sendThongBao(player1,
                        "Truy Cập: " + ServerManager.DOMAIN + "\n Để Mở Thành Viên");
                Service.gI().sendThongBao(player2,
                        "Truy Cập: " + ServerManager.DOMAIN + "\n Để Mở Thành Viên");
                break;
        }
    }

    public void addItemBot(Item it) {
        itemsTrade2.add(it);
    }

    public void update() {
        if (this.start && Util.canDoWithTime(lastTimeStart, TIME_TRADE)) {
            this.cancelTrade();
        }
    }
}
