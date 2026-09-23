package nro.models.services_func;

import nro.models.item.Item;
import java.io.IOException;
import java.util.ArrayList;
import nro.models.player.Player;
import nro.models.network.Message;
import nro.models.services.RewardService;
import nro.models.services.Service;
import java.util.List;
import nro.models.services.InventoryService;
import nro.models.services.ItemService;

/**
 *
 * @author By Mr Blue
 * 
 */

public class LuckyRound {

    private static final byte MAX_ITEM_IN_BOX = 100;

    public static final byte USING_GEM = 7;
    public static final byte USING_GOLD = 0;
    public static final byte USING_TICKET = 1;

    /** Giá quay: 1 Thỏi vàng (vật phẩm 457) cho 1 lượt. Chỉ còn MỘT vòng quay. */
    private static final int THOI_VANG = 457;
    private static final int PRICE_THOI_VANG = 1;

    /** Mốc lượt quay -> quà. Mỗi mốc nhận một lần, quà vào rương phụ và khoá giao dịch. */
    private static final int[][] MOC_QUA = {
        // {số lượt, id vật phẩm, sức đánh %, HP %, KI %, chí mạng %, sức đánh chí mạng %}
        {1_000, 1677, 10, 10, 10, 5, 0},    // Xe xanh Chi Chi
        {3_000, 1699, 10, 10, 10, 5, 0},    // Bồ cào 9 răng
        {5_000, 1678, 20, 20, 20, 10, 10},  // Xe đỏ Bun ma
        {5_000, 1502, 20, 20, 20, 10, 10},  // Thanh Long Yển Nguyệt đao
        {10_000, 2075, 40, 60, 60, 15, 30}, // Cải trang Goku SSJ3 Hắc Kim
        {12_000, 2076, 45, 70, 70, 25, 40}, // Cải trang Goku SSJ4 Huyết Hỏa
    };

    private static LuckyRound instance;

    public static LuckyRound gI() {
        if (instance == null) {
            instance = new LuckyRound();
        }
        return instance;
    }

    public void openCrackBallUI(Player pl) {
        openCrackBallUI(pl, USING_TICKET);
    }

    public void openCrackBallUI(Player pl, byte type) {
        pl.idMark.setTypeLuckyRound(USING_TICKET);
        Message msg = null;
        try {
            msg = new Message(-127);
            msg.writer().writeByte(0);
            msg.writer().writeByte(7);
            for (int i = 0; i < 7; i++) {
                msg.writer().writeShort(419 + i);
            }
            msg.writer().writeByte(USING_TICKET);
            msg.writer().writeInt(PRICE_THOI_VANG);
            msg.writer().writeShort(-1);
            pl.sendMessage(msg);
        } catch (IOException e) {
        } finally {
            if (msg != null) {
                msg.cleanup();
            }
        }
    }

    public void readOpenBall(Player player, Message msg) {
        try {
            msg.reader().readByte();
            byte count = msg.reader().readByte();
            // FIX (46): số lần quay là byte do client gửi, trước đây không chặn. count âm => giá âm =>
            // trừ số âm = CỘNG ngọc / vàng / vé; count >= 9 với vàng => 9 × 250 triệu tràn int thành
            // -2,04 tỷ => được CỘNG ~2 tỷ vàng + 9 phần thưởng. Giao diện chỉ có 7 viên.
            if (count < 1 || count > 7) {
                return;
            }
            openBallByThoiVang(player, count);
        } catch (Exception e) {
            openCrackBallUI(player);
        }
    }

    /** Tổng số Thỏi vàng trong hành trang (có thể nằm ở nhiều chồng). */
    private int demThoiVang(Player player) {
        int tong = 0;
        for (Item it : player.inventory.itemsBag) {
            if (it != null && it.isNotNullItem() && it.template.id == THOI_VANG) {
                tong += it.quantity;
            }
        }
        return tong;
    }

    /** Trừ Thỏi vàng, lấy lần lượt từ các chồng trong hành trang. */
    private void truThoiVang(Player player, int soLuong) {
        int conLai = soLuong;
        for (Item it : new ArrayList<>(player.inventory.itemsBag)) {
            if (conLai <= 0) {
                break;
            }
            if (it != null && it.isNotNullItem() && it.template.id == THOI_VANG) {
                int tru = Math.min(conLai, it.quantity);
                InventoryService.gI().subQuantityItemsBag(player, it, tru);
                conLai -= tru;
            }
        }
        InventoryService.gI().sendItemBags(player);
    }

    /** Quay bằng Thỏi vàng: 1 thỏi / 1 lượt, tối đa 7 lượt mỗi lần bấm. */
    private void openBallByThoiVang(Player player, byte count) {
        int need = count * PRICE_THOI_VANG;
        if (demThoiVang(player) < need) {
            Service.gI().sendThongBao(player, "Bạn không đủ Thỏi vàng để quay (cần " + need + ")");
            sendReward(player, new ArrayList<>());
            return;
        }
        if (count + player.inventory.itemsBoxCrackBall.size() > MAX_ITEM_IN_BOX) {
            Service.gI().sendThongBao(player, "Rương phụ đã đầy");
            sendReward(player, new ArrayList<>());
            return;
        }
        // Quay ra thưởng TRƯỚC rồi mới trừ thỏi vàng: lỗi giữa chừng thì người chơi không mất đồ.
        List<Item> list = RewardService.gI().getListItemLuckyRound(player, count, false);
        truThoiVang(player, need);
        addItemToBox(player, list);
        sendReward(player, list);
        Service.gI().sendMoney(player);

        player.vqtdSpin += count;
        traoQuaMoc(player);
    }

    /** Trao quà mốc lượt quay. Mỗi mốc một lần, quà rơi vào rương phụ. */
    public void traoQuaMoc(Player player) {
        for (int i = 0; i < MOC_QUA.length; i++) {
            int[] moc = MOC_QUA[i];
            if (player.vqtdSpin < moc[0] || (player.vqtdClaim & (1 << i)) != 0) {
                continue;
            }
            if (player.inventory.itemsBoxCrackBall.size() >= MAX_ITEM_IN_BOX) {
                Service.gI().sendThongBao(player, "Rương phụ đã đầy, hãy dọn bớt để nhận quà mốc "
                        + moc[0] + " lượt quay");
                return;
            }
            Item qua;
            try {
                qua = ItemService.gI().createNewItem((short) moc[1]);
            } catch (Exception e) {
                Service.gI().sendThongBao(player, "Quà mốc " + moc[0] + " lượt chưa có trong dữ liệu, báo admin");
                continue;
            }
            if (qua == null || qua.template == null) {
                continue;
            }
            qua.quantity = 1;
            qua.itemOptions.clear();
            if (moc[2] > 0) {
                qua.itemOptions.add(new Item.ItemOption(50, moc[2]));   // sức đánh %
            }
            if (moc[3] > 0) {
                qua.itemOptions.add(new Item.ItemOption(77, moc[3]));   // HP %
            }
            if (moc[4] > 0) {
                qua.itemOptions.add(new Item.ItemOption(103, moc[4]));  // KI %
            }
            if (moc[5] > 0) {
                qua.itemOptions.add(new Item.ItemOption(14, moc[5]));   // chí mạng %
            }
            if (moc[6] > 0) {
                qua.itemOptions.add(new Item.ItemOption(5, moc[6]));    // sức đánh chí mạng %
            }
            qua.itemOptions.add(new Item.ItemOption(30, 0));            // khoá giao dịch
            player.inventory.itemsBoxCrackBall.add(qua);
            player.vqtdClaim |= (1 << i);
            Service.gI().sendThongBao(player, "Chúc mừng! Đạt " + moc[0] + " lượt quay, nhận "
                    + qua.template.name + " (đã vào rương phụ)");
        }
    }

    /** Số lượt cho nút quay nhanh. Client chỉ quay được 7 viên mỗi lần nên phần này chạy ở server. */
    public static final int[] QUAY_NHANH = {10, 20, 30, 50, 100, 200};

    /**
     * Quay nhiều lượt một lúc, không qua giao diện vòng quay: trừ thỏi vàng, dồn phần thưởng
     * vào rương phụ. Trả về bảng tổng kết để NPC hiện ngay trong menu (khỏi bấm "Tiếp tục"
     * từng dòng như hộp thoại), hoặc null nếu không quay được.
     */
    public String quayNhanh(Player player, int count) {
        if (count < 1 || count > 200) {
            return null;
        }
        int need = count * PRICE_THOI_VANG;
        if (demThoiVang(player) < need) {
            Service.gI().sendThongBao(player, "Bạn không đủ Thỏi vàng để quay (cần " + need + ")");
            return null;
        }

        List<Item> list = RewardService.gI().getListItemLuckyRound(player, count, false);

        // Gộp các phần thưởng cùng loại (không có option) thành một chồng cho đỡ chật rương phụ.
        List<Item> gop = new ArrayList<>();
        for (Item it : list) {
            if (it == null || it.template == null) {
                continue;
            }
            Item chung = null;
            // Chỉ gộp món CHỒNG ĐƯỢC và không mang option, để lúc lấy ra khỏi rương không sai
            // số lượng (vd Bùa x2 đệ tử 1628 không chồng được).
            if (it.itemOptions.isEmpty() && it.template.isUpToUp) {
                for (Item g : gop) {
                    if (g.template.id == it.template.id && g.itemOptions.isEmpty() && g.template.isUpToUp) {
                        chung = g;
                        break;
                    }
                }
            }
            if (chung != null) {
                chung.quantity += Math.max(1, it.quantity);
            } else {
                gop.add(it);
            }
        }

        int choTrong = MAX_ITEM_IN_BOX - player.inventory.itemsBoxCrackBall.size();
        if (choTrong < gop.size()) {
            Service.gI().sendThongBao(player, "Rương phụ chỉ còn " + choTrong
                    + " chỗ, cần " + gop.size() + " chỗ. Hãy dọn bớt rồi quay tiếp");
            return null;
        }

        truThoiVang(player, need);
        addItemToBox(player, gop);
        Service.gI().sendMoney(player);
        player.vqtdSpin += count;
        traoQuaMoc(player);

        // Gộp 3 món mỗi dòng cho khỏi tràn khung menu khi quay 100–200 lượt.
        StringBuilder sb = new StringBuilder("Quay " + count + " lượt, tốn " + need + " Thỏi vàng:");
        int cot = 0;
        for (Item it : gop) {
            sb.append(cot % 3 == 0 ? "\n" : ", ").append(it.template.name).append(" x").append(it.quantity);
            cot++;
        }
        sb.append("\n(đã vào rương phụ)");
        return sb.toString();
    }

    /** Bảng mốc quà, hiện khi bấm "Mốc quà". */
    public String bangMoc(Player player) {
        StringBuilder sb = new StringBuilder("Mốc quà vòng quay\nĐã quay: " + player.vqtdSpin + " lượt\n");
        for (int i = 0; i < MOC_QUA.length; i++) {
            int[] moc = MOC_QUA[i];
            String ten = "vật phẩm " + moc[1];
            Item m = ItemService.gI().createNewItem((short) moc[1]);
            if (m != null && m.template != null) {
                ten = m.template.name;
            }
            boolean nhan = (player.vqtdClaim & (1 << i)) != 0;
            sb.append("\n").append(moc[0]).append(" lượt: ").append(ten)
              .append(nhan ? " (đã nhận)" : (player.vqtdSpin >= moc[0] ? " (đủ điều kiện)" : ""));
        }
        return sb.toString();
    }

    /** Dòng mô tả tiến độ mốc, hiện trên menu Thượng Đế. */
    public String tienDoMoc(Player player) {
        for (int[] moc : MOC_QUA) {
            if (player.vqtdSpin < moc[0]) {
                return "Đã quay " + player.vqtdSpin + " lượt, còn " + (moc[0] - player.vqtdSpin)
                        + " lượt tới mốc " + moc[0];
            }
        }
        return "Đã quay " + player.vqtdSpin + " lượt, nhận đủ mọi mốc";
    }

    private void sendReward(Player player, List<Item> items) {
        Message msg = null;
        try {
            msg = new Message(-127);
            msg.writer().writeByte(1);
            msg.writer().writeByte(items.size());
            for (Item item : items) {
                msg.writer().writeShort(item.template.iconID);
            }
            player.sendMessage(msg);
        } catch (IOException e) {
        } finally {
            if (msg != null) {
                msg.cleanup();
            }
        }
    }

    private void addItemToBox(Player player, List<Item> items) {
        player.inventory.itemsBoxCrackBall.addAll(items);
    }
}
