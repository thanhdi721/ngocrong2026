package nro.models.npc_list;

import nro.models.consts.ConstNpc;
import nro.models.npc.Npc;
import nro.models.player.Player;
import nro.models.map.service.ChangeMapService;
import nro.models.services.Service;
import nro.models.services.TaskService;

/**
 *
 * @author By Mr Blue
 *
 * TUYẾN MỚI: Jaco (NPC 63) là NPC dẫn chuyện của cả chương 1–2 và của nhánh
 * NV 48. Trước đây lớp này chỉ phục vụ hai map 24 và 139 (đưa người chơi đi
 * hành tinh khác) và KHÔNG gọi trigger nhiệm vụ nào, nên các bước sau kẹt cứng:
 *   TASK_3_0  — gặp Jaco ở Vách núi   (map 42 / 43 / 44 theo hành tinh)
 *   TASK_7_2  — gặp Jaco ở Trạm tàu vũ trụ (map 24 / 25 / 26 theo hành tinh)
 *   TASK_15_2 — gặp lại Jaco ở Trạm tàu vũ trụ
 *   TASK_48_2 / TASK_48_5 — trình báo và nộp biên bản, CHỈ ở map 24
 * Mọi điều kiện (đúng map theo hành tinh, đúng bước) nằm trong
 * TaskService.checkDoneTaskTalkNpc; ở đây chỉ cần gọi nó TRƯỚC khi mở menu cũ
 * và thoát sớm nếu nó đã xử lý xong.
 *
 * Jaco phải có mặt trên map 25, 26, 42, 43, 44 — chạy
 * SRC/sql/patch/04-npc-tren-map.sql, nếu không người chơi Namếc / Xayda
 * không qua nổi NV 3.
 */
public class Jaco extends Npc {

    public Jaco(int mapId, int status, int cx, int cy, int tempId, int avartar) {
        super(mapId, status, cx, cy, tempId, avartar);
    }

    @Override
    public void openBaseMenu(Player player) {
        if (!canOpenNpc(player)) {
            return;
        }
        // TUYẾN MỚI: trigger A3 trước tiên. Trả true = vừa hoàn thành một bước,
        // không mở thêm menu nữa để người chơi thấy ngay thông báo nhiệm vụ.
        if (TaskService.gI().checkDoneTaskTalkNpc(player, this)) {
            return;
        }
        switch (this.mapId) {
            case 24 ->
                this.createOtherMenu(player, ConstNpc.BASE_MENU,
                        "Gô Tên, Calích và Monaka đang gặp chuyện ở hành tinh\nPotaufeu\nHãy đến đó ngay", "Đến\nPotaufeu", "Từ chối");
            case 139 ->
                this.createOtherMenu(player, ConstNpc.BASE_MENU,
                        "Tàu Vũ Trụ của ta có thể đưa cậu đến hành tinh khác chỉ trong 3 giây.\nCậu muốn đi đâu?", "Đến\nTrái Đất", "Đến\nNamếc", "Đến\nXayda", "Từ chối");
            // TUYẾN MỚI: Trạm tàu vũ trụ Namếc / Xayda — Jaco đứng đây cho NV 7 và NV 15.
            case 25, 26 ->
                this.createOtherMenu(player, ConstNpc.BASE_MENU,
                        "Cảnh sát vũ trụ đây. Ta đang ghi lại mọi thứ hành tinh này còn nhớ được.\nNgươi cứ đi việc của ngươi.",
                        "Rời đi");
            // TUYẾN MỚI: Vách núi — nơi Jaco hạ cánh ở NV 3.
            case 42, 43, 44 ->
                this.createOtherMenu(player, ConstNpc.BASE_MENU,
                        "Máy của ta đọc ngươi ra ba con số.\nHai con số bình thường. Con số thứ ba thì... không nên tồn tại.",
                        "Rời đi");
            default -> {
            }
        }
    }

    @Override
    public void confirmMenu(Player player, int select) {
        if (canOpenNpc(player)) {
            if (player.idMark.isBaseMenu()) {
                switch (this.mapId) {
                    case 24 -> {
                        if (select == 0) {
                            ChangeMapService.gI().goToPotaufeu(player);
                        }
                    }
                    case 139 -> {
                        switch (select) {
                            case 0 ->
                                ChangeMapService.gI().changeMapBySpaceShip(player, 24, -1, -1);
                            case 1 ->
                                ChangeMapService.gI().changeMapBySpaceShip(player, 25, -1, -1);
                            case 2 ->
                                ChangeMapService.gI().changeMapBySpaceShip(player, 26, -1, -1);
                        }
                    }
                    // TUYẾN MỚI: các map chỉ có thoại, đóng hội thoại khi bấm "Rời đi".
                    case 25, 26, 42, 43, 44 ->
                        Service.gI().hideWaitDialog(player);
                    default -> {
                    }
                }
            }
        }
    }
}
