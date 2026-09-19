package nro.models.server;

import java.io.IOException;
import java.util.Objects;
import nro.models.consts.ConstNpc;
import nro.models.npc.Npc;
import nro.models.map.service.NpcManager;
import nro.models.network.MySession;
import nro.models.player.Player;
import nro.models.services.Service;
import nro.models.services_func.TransactionService;

/**
 *
 * @author By Mr Blue
 * 
 */

public class MenuController {

    private static MenuController instance;

    public static MenuController gI() {
        if (instance == null) {
            instance = new MenuController();
        }
        return instance;
    }

    public void openMenuNPC(MySession session, int idnpc, Player player) {
        TransactionService.gI().cancelTrade(player);
        Npc npc;
        if (idnpc == ConstNpc.CALICK && player.zone.map.mapId != 102) {
            npc = NpcManager.getNpc(ConstNpc.CALICK);
        } else if (idnpc == ConstNpc.LY_TIEU_NUONG) {
            npc = NpcManager.getNpc(ConstNpc.LY_TIEU_NUONG);
        } else {
            npc = player.zone.map.getNpc(player, idnpc);
        }
        if (npc != null) {
            npc.openBaseMenu(player);
        } else {
            Service.gI().hideWaitDialog(player);
        }
    }

    public void doSelectMenu(Player player, int npcId, int select) throws IOException {
        TransactionService.gI().cancelTrade(player);
        // FIX: chỉ nhận lựa chọn gửi tới ĐÚNG NPC đã mở menu đang hiển thị. Trước đây client chế
        // tác gửi được số thứ tự menu (indexMenu) của NPC này sang một NPC khác có trùng số menu
        // (ví dụ BASE_MENU) → kích hoạt chức năng/đổi quà của NPC kia mà không qua menu của nó.
        if (player.idMark == null || player.idMark.getMenuNpcId() != npcId) {
            Service.gI().hideWaitDialog(player);
            return;
        }
        // FIX (46): trước đây indexMenu KHÔNG bao giờ bị xoá sau khi xác nhận => client gửi lặp gói 32
        // {npcId, select} bao nhiêu lần cũng được xử lý lại (hoàn tiền cây đậu, đổi quà, dịch chuyển...).
        // Nay: nếu xử lý xong mà không có menu mới được mở thì xoá menu hiện tại.
        int seq = player.idMark.getMenuSeq();
        try {
            doSelectMenu0(player, npcId, select);
        } finally {
            if (player.idMark != null && player.idMark.getMenuSeq() == seq) {
                player.idMark.setIndexMenu(ConstNpc.IGNORE_MENU);
            }
        }
    }

    private void doSelectMenu0(Player player, int npcId, int select) throws IOException {
        switch (npcId) {
            case ConstNpc.RONG_THIENG, ConstNpc.CON_MEO ->
                Objects.requireNonNull(NpcManager.getNpc((byte) npcId)).confirmMenu(player, select);
            default -> {
                Npc npc = null;
                if (npcId == ConstNpc.CALICK && player.zone.map.mapId != 102) {
                    npc = NpcManager.getNpc(ConstNpc.CALICK);
                } else if (npcId == ConstNpc.LY_TIEU_NUONG) {
                    npc = NpcManager.getNpc(ConstNpc.LY_TIEU_NUONG);
                } else if (player.zone != null) {
                    npc = player.zone.map.getNpc(player, npcId);
                }
                if (npc != null) {
                    npc.confirmMenu(player, select);
                } else {
                    Service.gI().hideWaitDialog(player);
                }
            }
        }

    }
}
