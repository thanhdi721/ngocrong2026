package nro.models.npc_list;

import nro.models.consts.ConstNpc;
import nro.models.consts.ConstTask;
import nro.models.npc.Npc;
import nro.models.player.Player;
import nro.models.services.Service;
import nro.models.services.TaskService;

/**
 * NPC 71 — Berry, Khu hang động (map 160).
 *
 * <p>
 * Đây là <b>điểm rẽ nhánh 1</b> của tuyến nhiệm vụ mới (20b §NV 20 / §NV 48):
 * ở bước {@code TASK_20_1} người chơi chọn đi theo thợ săn Granola (giữ nhiệm vụ
 * 20) hay báo cảnh sát vũ trụ Jaco (chuyển sang nhiệm vụ 48). Toàn bộ luật rẽ
 * nhánh nằm trong {@code TaskService.checkDoneTaskConfirmMenuNpc}; lớp này chỉ
 * dựng menu 2 nút và chuyển lựa chọn sang đó — giống hệt cách
 * {@code Potage} (rẽ nhánh 2) và {@code DaiThienSu} (rẽ nhánh 3) đang làm.
 *
 * <p>
 * Bảng `npc_template` đã có sẵn id 71 (head 1015 / body 1016 / leg 1017,
 * avatar 9076) và map 160 đã có sẵn {@code [71, 1239, 432]}, nhưng trước đây
 * NPC này rơi vào nhánh {@code default} của {@code NpcFactory.createNPC} nên
 * không có {@code openBaseMenu} riêng và không gọi được trigger nhiệm vụ nào.
 */
public class Berry extends Npc {

    /** TUYẾN MỚI: B14 — id menu riêng cho điểm rẽ nhánh 1 (NV 20 ↔ NV 48). */
    private static final int MENU_RE_NHANH_NV20 = 2200;

    public Berry(int mapId, int status, int cx, int cy, int tempId, int avartar) {
        super(mapId, status, cx, cy, tempId, avartar);
    }

    @Override
    public void openBaseMenu(Player player) {
        if (!canOpenNpc(player)) {
            return;
        }
        // Bước A3: TASK_20_0 / TASK_48_0 "Tìm kẻ lạ ở Khu hang động".
        // checkDoneTaskTalkNpc tự kiểm mapId == 160 và tự kiểm đúng bước.
        if (TaskService.gI().checkDoneTaskTalkNpc(player, this)) {
            return;
        }
        // Bước B14: TASK_20_1 — điểm rẽ nhánh. Thứ tự nút PHẢI khớp
        // TaskService.checkDoneTaskConfirmMenuNpc: 0 = Granola (giữ task 20),
        // 1 = Jaco (switchTaskBranch sang task 48).
        if (TaskService.gI().getIdTask(player) == ConstTask.TASK_20_1) {
            this.createOtherMenu(player, MENU_RE_NHANH_NV20,
                    "Ngươi chọn đi: đi với thợ săn, hay báo cảnh sát?\n"
                    + "Cả hai đều đúng, và đều mất một thứ.",
                    "Đi theo\nGranola", "Báo cho\nJaco");
            return;
        }
        this.createOtherMenu(player, ConstNpc.BASE_MENU,
                "Đừng lại gần! Anh ấy bắn trước, hỏi sau.\n"
                + "Mà dạo này anh ấy cũng chẳng hỏi nữa.",
                "Về Thung\nlũng Nappa", "Rời đi");
    }

    @Override
    public void confirmMenu(Player player, int select) {
        if (!canOpenNpc(player)) {
            return;
        }
        // TUYẾN MỚI: B14 — chuyển lựa chọn sang TaskService, mọi luật rẽ nhánh nằm ở đó.
        if (player.idMark.getIndexMenu() == MENU_RE_NHANH_NV20) {
            TaskService.gI().checkDoneTaskConfirmMenuNpc(player, this, (byte) select);
            return;
        }
        if (player.idMark.isBaseMenu()) {
            // Đường ra khỏi Khu hang động cho người chưa có Nhẫn thời không (NV 20 / NV 48).
            if (select == 0) {
                nro.models.map.service.ChangeMapService.gI().changeMapBySpaceShip(player, 68, -1, 90);
                return;
            }
            Service.gI().hideWaitDialog(player);
        }
    }
}
