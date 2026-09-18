package nro.models.npc_list;

import nro.models.consts.ConstNpc;
import nro.models.npc.Npc;
import nro.models.player.Player;
import nro.models.services.Service;
import nro.models.services.TaskService;
import nro.models.consts.ConstTask;

/**
 *
 * @author By Mr Blue
 * 
 */

public class DaiThienSu extends Npc {

    public DaiThienSu(int mapId, int status, int cx, int cy, int tempId, int avartar) {
        super(mapId, status, cx, cy, tempId, avartar);
    }

    /** TUYẾN MỚI: B14 — id menu riêng cho điểm rẽ nhánh 3 (NV 47 ↔ NV 50). */
    private static final int MENU_RE_NHANH_NV47 = 2202;

    @Override
    public void openBaseMenu(Player player) {
        // FIX: NPC Đại Thiên Sứ trước đây menu rỗng nên không mở được hội thoại
        // (tuyến nhiệm vụ mới dùng NPC này ở nhiệm vụ 45, 46 và đoạn kết)
        if (canOpenNpc(player)) {
            TaskService.gI().checkDoneTaskTalkNpc(player, this);
            // TUYẾN MỚI: B14 — đang ở bước TASK_47_0 thì mở menu 2 nút rẽ nhánh cuối tuyến.
            if (this.mapId == 145 && TaskService.gI().getIdTask(player) == ConstTask.TASK_47_0) {
                this.createOtherMenu(player, MENU_RE_NHANH_NV47,
                        "Lõi Hư Không đang nằm trong tay ngươi.\nTrả lại ký ức cho vũ trụ, hay giữ Lõi cho riêng mình?",
                        "Trả ký ức", "Giữ Lõi");
                return;
            }
            this.createOtherMenu(player, ConstNpc.BASE_MENU,
                    "Ta là Đại Thiên Sứ, người trông coi trật tự của các vũ trụ.\nNgươi cần gì ở ta?",
                    "Từ chối");
        }
    }

    @Override
    public void confirmMenu(Player player, int select) {
        // FIX: đóng hội thoại khi người chơi chọn "Từ chối"
        if (canOpenNpc(player)) {
            // TUYẾN MỚI: B14 — chuyển lựa chọn sang TaskService, mọi luật rẽ nhánh nằm ở đó.
            if (player.idMark.getIndexMenu() == MENU_RE_NHANH_NV47) {
                TaskService.gI().checkDoneTaskConfirmMenuNpc(player, this, (byte) select);
                return;
            }
            if (player.idMark.isBaseMenu() && select == 0) {
                Service.gI().hideWaitDialog(player);
            }
        }
    }
}
