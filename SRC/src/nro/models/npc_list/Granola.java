package nro.models.npc_list;

import nro.models.consts.ConstNpc;
import nro.models.consts.ConstTask;
import nro.models.npc.Npc;
import nro.models.player.Player;
import nro.models.services.Service;
import nro.models.services.TaskService;

/**
 * NPC 76 — Granola, Khu hang động (map 160).
 *
 * <p>
 * Dùng ở nhánh thợ săn của NV 20 (20b §NV 20):
 * <ul>
 * <li>{@code TASK_20_2} "Bắt tay với Granola",</li>
 * <li>{@code TASK_20_5} "Nhận tiền thưởng từ Granola".</li>
 * </ul>
 * Cả hai đều là bước A3, điều kiện ({@code npc.tempId == 76} và
 * {@code mapId == 160}) nằm sẵn trong {@code TaskService.checkDoneTaskTalkNpc},
 * nên ở đây chỉ cần gọi hàm đó rồi mở một menu tối thiểu.
 *
 * <p>
 * Bảng `npc_template` đã có sẵn id 76 (head 2018 / body 2019 / leg 2020,
 * avatar 15233) — <b>không cần thêm tài nguyên client</b>. Nhưng NPC này
 * chưa xuất hiện trên bất kỳ map nào: phải chạy
 * {@code SRC/sql/patch/04-npc-tren-map.sql} để thêm {@code [76, x, y]} vào cột
 * {@code npcs} của map 160, nếu không NV 20 bước 2 kẹt cứng.
 */
public class Granola extends Npc {

    public Granola(int mapId, int status, int cx, int cy, int tempId, int avartar) {
        super(mapId, status, cx, cy, tempId, avartar);
    }

    @Override
    public void openBaseMenu(Player player) {
        if (!canOpenNpc(player)) {
            return;
        }
        // A3: TASK_20_2 / TASK_20_5 — hàm tự kiểm mapId == 160 và đúng bước.
        if (TaskService.gI().checkDoneTaskTalkNpc(player, this)) {
            return;
        }
        int taskId = TaskService.gI().getIdTask(player);
        String say;
        if (taskId == ConstTask.TASK_20_3 || taskId == ConstTask.TASK_20_4) {
            say = "Ba cái đầu, chia đôi tiền. Ta đã nói rõ rồi.\n"
                    + "Quay lại khi trong tay ngươi có đủ ba tấm thẻ.";
        } else {
            say = "Heart xóa hành tinh ta.\n"
                    + "Giờ ta không nhớ nổi mẹ ta trông ra sao. Ngươi hiểu chưa?";
        }
        this.createOtherMenu(player, ConstNpc.BASE_MENU, say, "Rời đi");
    }

    @Override
    public void confirmMenu(Player player, int select) {
        if (!canOpenNpc(player)) {
            return;
        }
        if (player.idMark.isBaseMenu()) {
            Service.gI().hideWaitDialog(player);
        }
    }
}
