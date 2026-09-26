package nro.models.npc_list;

import nro.models.consts.ConstNpc;
import nro.models.item.Item;
import nro.models.map.service.ChangeMapService;
import nro.models.npc.Npc;
import nro.models.player.Player;
import nro.models.services.InventoryService;
import nro.models.services.ItemService;
import nro.models.services.Service;
import nro.models.tu_tien.TuTien;
import nro.models.utils.Util;

/**
 * NPC "Tu Tiên" ở đảo Kamê (map 5), ngoại hình cải trang "Goku Thiên Sứ".
 *
 * <p>Hai việc: đưa người chơi vào <b>map Tu Tiên</b> (Nam Thiên Môn) và bán đồ lấy
 * <b>Linh Thạch</b>.
 *
 * <p>Tiệm dựng bằng menu NPC chứ không dùng gói tin tiệm: gói tiệm phải đăng ký
 * {@code idMark.setShopOpen} rồi đi qua {@code ShopService.buyItem}, mà món ở đây trả bằng
 * Linh Thạch chứ không phải vàng/ngọc — nhét vào đó vừa rối vừa dễ hở đường mua lậu.
 * Trừ tiền và trao đồ ở ngay đây thì kiểm soát được từng bước.
 */
public class TuTienNPC extends Npc {

    /** Đan: {id vật phẩm, tên hiện trên nút}. */
    private static final Object[][] DAN = {
        {TuTien.DAN_SUC_DANH, "Luyện Khí Đan", "+20% sức đánh"},
        {TuTien.DAN_HP, "Hộ Thể Đan", "+30% HP"},
        {TuTien.DAN_KI, "Tụ Khí Đan", "+30% KI"},
        {TuTien.DAN_GIAP, "Kim Cương Đan", "giảm 50% sát thương"},
        {TuTien.DAN_CHI_MANG, "Phá Quân Đan", "+10% chí mạng"}
    };

    /** Ngọc bội: {id vật phẩm, tên, id dòng chỉ số, giá trị}. */
    private static final Object[][] NGOC = {
        {TuTien.NGOC_BOI_HP, "Ngọc Bội Hộ Mệnh", 6, 10000},
        {TuTien.NGOC_BOI_KI, "Ngọc Bội Tụ Linh", 7, 10000},
        {TuTien.NGOC_BOI_SUC_DANH, "Ngọc Bội Phá Quân", 0, 5000}
    };

    public TuTienNPC(int mapId, int status, int cx, int cy, int tempId, int avartar) {
        super(mapId, status, cx, cy, tempId, avartar);
    }

    @Override
    public void openBaseMenu(Player player) {
        if (!canOpenNpc(player)) {
            return;
        }
        createOtherMenu(player, ConstNpc.TU_TIEN_MENU,
                "Muốn trường sinh thì phải chịu khổ.\nLinh Thạch đang có: "
                + demLinhThach(player) + " viên.",
                "Vào map\ntu tiên", "Shop\ntu tiên", "Đóng");
    }

    @Override
    public void confirmMenu(Player player, int select) {
        if (!canOpenNpc(player)) {
            return;
        }
        switch (player.idMark.getIndexMenu()) {
            case ConstNpc.TU_TIEN_MENU -> {
                if (select == 0) {
                    vaoMap(player);
                } else if (select == 1) {
                    moShop(player);
                }
            }
            case ConstNpc.TU_TIEN_SHOP -> {
                switch (select) {
                    case 0 ->
                        moDan(player);
                    case 1 ->
                        moNgocBoi(player);
                    case 2 ->
                        muaBua(player);
                    default -> {
                    }
                }
            }
            case ConstNpc.TU_TIEN_DAN -> {
                if (select >= 0 && select < DAN.length) {
                    muaDan(player, select);
                }
            }
            case ConstNpc.TU_TIEN_NGOC_BOI -> {
                if (select >= 0 && select < NGOC.length) {
                    muaNgocBoi(player, select);
                }
            }
            default -> {
            }
        }
    }

    //================================ vào map ================================
    private void vaoMap(Player player) {
        if (player.zone == null) {
            return;
        }
        try {
            // Rải người chơi ra khắp dải nền cho khỏi dồn một chỗ.
            ChangeMapService.gI().changeMap(player, TuTien.MAP_ID, -1,
                    Util.nextInt(150, 1050), 432);
        } catch (Exception e) {
            Service.gI().sendThongBao(player, "Cổng Nam Thiên Môn đang đóng, thử lại sau.");
        }
    }

    //================================ tiệm ================================
    private void moShop(Player player) {
        createOtherMenu(player, ConstNpc.TU_TIEN_SHOP,
                "Linh Thạch đang có: " + demLinhThach(player) + " viên.\nNgươi muốn xem gì?",
                "Đan dược\n" + TuTien.GIA_DAN + " LT", "Ngọc bội\n" + TuTien.GIA_NGOC_BOI + " LT",
                "Tụ Linh Phù\n" + TuTien.GIA_BUA_THOI_VANG + " thỏi vàng", "Đóng");
    }

    private void moDan(Player player) {
        String[] muc = new String[DAN.length + 1];
        for (int i = 0; i < DAN.length; i++) {
            muc[i] = DAN[i][1] + "\n" + TuTien.GIA_DAN + " LT";
        }
        muc[DAN.length] = "Đóng";
        StringBuilder sb = new StringBuilder("Đan dược — hiệu lực 10 phút, "
                + TuTien.GIA_DAN + " Linh Thạch một viên.\n");
        for (Object[] d : DAN) {
            sb.append("\n").append(d[1]).append(": ").append(d[2]);
        }
        createOtherMenu(player, ConstNpc.TU_TIEN_DAN, sb.toString(), muc);
    }

    private void moNgocBoi(Player player) {
        String[] muc = new String[NGOC.length + 1];
        for (int i = 0; i < NGOC.length; i++) {
            muc[i] = NGOC[i][1] + "\n" + TuTien.GIA_NGOC_BOI + " LT";
        }
        muc[NGOC.length] = "Đóng";
        createOtherMenu(player, ConstNpc.TU_TIEN_NGOC_BOI,
                "Ngọc bội — CHỈ ĐỆ TỬ đeo được, đeo vào ô rađa của đệ.\n"
                + "Mỗi viên " + TuTien.GIA_NGOC_BOI + " Linh Thạch.\n\n"
                + "Hộ Mệnh: HP +10.000\nTụ Linh: KI +10.000\nPhá Quân: Sức đánh +5.000\n\n"
                + "Mua về có 10% ra bản VĨNH VIỄN,\ncòn lại hạn dùng 1–3 ngày.",
                muc);
    }

    //================================ mua ================================
    private void muaDan(Player player, int chiSo) {
        int id = (int) DAN[chiSo][0];
        if (!truLinhThach(player, TuTien.GIA_DAN)) {
            return;
        }
        Item it = ItemService.gI().createNewItem((short) id);
        if (it == null || it.template == null) {
            hoanLinhThach(player, TuTien.GIA_DAN, "vật phẩm chưa có trong cơ sở dữ liệu");
            return;
        }
        it.quantity = 1;
        it.itemOptions.clear();
        // Đan là món DUY NHẤT được giao dịch — không gắn dòng khóa.
        if (!InventoryService.gI().addItemBag(player, it)) {
            hoanLinhThach(player, TuTien.GIA_DAN, "hành trang đã đầy");
            return;
        }
        InventoryService.gI().sendItemBags(player);
        Service.gI().sendThongBao(player, "Nhận " + DAN[chiSo][1] + ".");
        moDan(player);
    }

    private void muaNgocBoi(Player player, int chiSo) {
        int id = (int) NGOC[chiSo][0];
        int idChiSo = (int) NGOC[chiSo][2];
        int giaTri = (int) NGOC[chiSo][3];
        if (!truLinhThach(player, TuTien.GIA_NGOC_BOI)) {
            return;
        }
        Item it = ItemService.gI().createNewItem((short) id);
        if (it == null || it.template == null) {
            hoanLinhThach(player, TuTien.GIA_NGOC_BOI, "vật phẩm chưa có trong cơ sở dữ liệu");
            return;
        }
        it.quantity = 1;
        it.itemOptions.clear();
        it.itemOptions.add(new Item.ItemOption(idChiSo, giaTri));
        it.itemOptions.add(new Item.ItemOption(30, 0));     // khóa giao dịch
        boolean vinhVien = Util.isTrue(10, 100);
        if (vinhVien) {
            it.itemOptions.add(new Item.ItemOption(73, 0));
        } else {
            it.itemOptions.add(new Item.ItemOption(93, Util.nextInt(1, 3)));
        }
        if (!InventoryService.gI().addItemBag(player, it)) {
            hoanLinhThach(player, TuTien.GIA_NGOC_BOI, "hành trang đã đầy");
            return;
        }
        InventoryService.gI().sendItemBags(player);
        Service.gI().sendThongBao(player, "Nhận " + NGOC[chiSo][1]
                + (vinhVien ? " — VĨNH VIỄN! Phúc lớn." : " — hạn dùng vài ngày."));
        moNgocBoi(player);
    }

    private void muaBua(Player player) {
        Item thoiVang = InventoryService.gI().findItemBag(player, TuTien.ID_THOI_VANG);
        if (thoiVang == null || thoiVang.quantity < TuTien.GIA_BUA_THOI_VANG) {
            Service.gI().sendThongBao(player, "Cần " + TuTien.GIA_BUA_THOI_VANG + " thỏi vàng.");
            return;
        }
        Item it = ItemService.gI().createNewItem((short) TuTien.TU_LINH_PHU);
        if (it == null || it.template == null) {
            Service.gI().sendThongBao(player, "Vật phẩm chưa có trong cơ sở dữ liệu.");
            return;
        }
        it.quantity = 1;
        it.itemOptions.clear();
        it.itemOptions.add(new Item.ItemOption(30, 0));     // khóa giao dịch
        if (!InventoryService.gI().addItemBag(player, it)) {
            Service.gI().sendThongBao(player, "Hành trang đã đầy.");
            return;
        }
        // Trừ tiền SAU khi chắc chắn nhét được vào túi.
        InventoryService.gI().subQuantityItemsBag(player, thoiVang, TuTien.GIA_BUA_THOI_VANG);
        InventoryService.gI().sendItemBags(player);
        Service.gI().sendThongBao(player, "Nhận Tụ Linh Phù.");
        moShop(player);
    }

    //================================ tiền nong ================================
    private static int demLinhThach(Player player) {
        Item lt = InventoryService.gI().findItemBag(player, TuTien.LINH_THACH);
        return lt == null ? 0 : lt.quantity;
    }

    /** Trừ Linh Thạch; thiếu thì báo và trả false. */
    private boolean truLinhThach(Player player, int gia) {
        Item lt = InventoryService.gI().findItemBag(player, TuTien.LINH_THACH);
        if (lt == null || lt.quantity < gia) {
            Service.gI().sendThongBao(player, "Cần " + gia + " Linh Thạch, đang có "
                    + (lt == null ? 0 : lt.quantity) + ".");
            return false;
        }
        InventoryService.gI().subQuantityItemsBag(player, lt, gia);
        InventoryService.gI().sendItemBags(player);
        return true;
    }

    /** Trả lại Linh Thạch khi trao đồ hỏng giữa chừng — không để người chơi mất trắng. */
    private void hoanLinhThach(Player player, int gia, String vi) {
        Item bu = ItemService.gI().createNewItem((short) TuTien.LINH_THACH);
        if (bu != null && bu.template != null) {
            bu.quantity = gia;
            bu.itemOptions.clear();
            bu.itemOptions.add(new Item.ItemOption(30, 0));
            InventoryService.gI().addItemBag(player, bu);
            InventoryService.gI().sendItemBags(player);
        }
        Service.gI().sendThongBao(player, "Không mua được (" + vi + "), đã hoàn lại Linh Thạch.");
    }
}
