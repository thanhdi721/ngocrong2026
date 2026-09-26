package nro.models.npc_list;

import nro.models.consts.ConstNpc;
import nro.models.item.Item;
import nro.models.map.service.ChangeMapService;
import nro.models.npc.Npc;
import nro.models.player.Player;
import nro.models.services.InventoryService;
import nro.models.services.Service;
import nro.models.tu_tien.TuTien;
import nro.models.utils.Util;

/**
 * NPC "Tu Tiên" ở đảo Kamê (map 5), ngoại hình cải trang "Goku Thiên Sứ".
 *
 * <p>Hai việc: đưa người chơi vào <b>map Tu Tiên</b> (Nam Thiên Môn) và bán đồ lấy
 * <b>Linh Thạch</b>.
 *
 * <p>Tiệm dùng ĐÚNG giao diện tiệm của client (icon + tên + giá + dòng chữ xanh), dựng trong
 * {@link TuTien#moTiem}. Hàng trả bằng Linh Thạch / thỏi vàng nên không nằm trong bảng `shop`
 * của CSDL được; {@code ShopService.buyItem} chặn tab của tiệm này lại và gọi
 * {@link TuTien#mua} trừ đúng thứ tiền cần trừ.
 */
public class TuTienNPC extends Npc {

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
        if (!canOpenNpc(player) || player.idMark.getIndexMenu() != ConstNpc.TU_TIEN_MENU) {
            return;
        }
        if (select == 0) {
            vaoMap(player);
        } else if (select == 1) {
            // Tiệm dựng bằng ĐÚNG gói tin tiệm (icon + tên + giá + dòng chữ xanh), không
            // phải menu chữ — xem TuTien.moTiem.
            TuTien.moTiem(player);
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

    //================================ tiền nong ================================
    private static int demLinhThach(Player player) {
        Item lt = InventoryService.gI().findItemBag(player, TuTien.LINH_THACH);
        return lt == null ? 0 : lt.quantity;
    }

}
