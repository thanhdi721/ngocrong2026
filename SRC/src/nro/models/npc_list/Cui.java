package nro.models.npc_list;

import nro.models.boss.Boss;
import nro.models.boss.BossID;
import nro.models.boss.Boss_Manager.BossManager;
import nro.models.consts.ConstNpc;
import nro.models.consts.ConstTask;
import nro.models.map.Zone;
import nro.models.npc.Npc;
import nro.models.player.Player;
import nro.models.map.service.MapService;
import nro.models.map.service.NpcService;
import nro.models.services.Service;
import nro.models.services.TaskService;
import nro.models.map.service.ChangeMapService;
import nro.models.utils.Util;

/**
 *
 * @author By Mr Blue
 * 
 */

public class Cui extends Npc {

    private final int COST_FIND_BOSS = 50000000;

    public Cui(int mapId, int status, int cx, int cy, int tempId, int avartar) {
        super(mapId, status, cx, cy, tempId, avartar);
    }

    @Override
    public void openBaseMenu(Player pl) {
        if (canOpenNpc(pl)) {
            if (!TaskService.gI().checkDoneTaskTalkNpc(pl, this)) {
                // TUYẾN MỚI: bỏ nhánh thoại nhiệm vụ 7 tuyến cũ ("cứu đứa bé"), xem ghi chú ở Cargo.java
                {
                    switch (this.mapId) {
                        case 19 -> {
                            int taskId = TaskService.gI().getIdTask(pl);
                            // TUYẾN MỚI: mốc cũ TASK_19_0 / TASK_19_1 / TASK_19_2 (mỗi mốc một boss riêng)
                            // -> gộp thành MỘT bước TASK_20_3 (nhánh Granola) và TASK_20_3 tương đương TASK_48_3
                            // (nhánh Jaco) của NV 20 "Kẻ săn tiền thưởng": đếm chung 3 boss Kuku / Mập đầu đinh / Rambo,
                            // không bắt thứ tự nên menu cho chọn cả ba.
                            // Cả bước nhặt thẻ / biên bản (20_4, 48_4) vì vật phẩm rơi từ chính 3 boss này.
                            if (taskId == ConstTask.TASK_20_3 || taskId == ConstTask.TASK_48_3
                                    || taskId == ConstTask.TASK_20_4 || taskId == ConstTask.TASK_48_4) {
                                this.createOtherMenu(pl, ConstNpc.MENU_FIND_KUKU,
                                        "Đội quân của Fide đang ở Thung lũng Nappa, ta sẽ đưa ngươi đến đó",
                                        "Đến chỗ\nKuku\n(" + Util.numberToMoney(COST_FIND_BOSS) + " vàng)",
                                        "Đến chỗ\nMập đầu đinh\n(" + Util.numberToMoney(COST_FIND_BOSS) + " vàng)",
                                        "Đến chỗ\nRambo\n(" + Util.numberToMoney(COST_FIND_BOSS) + " vàng)",
                                        "Đến Cold", "Đến\nNappa", "Từ chối");
                            } else {
                                this.createOtherMenu(pl, ConstNpc.BASE_MENU,
                                        "Đội quân của Fide đang ở Thung lũng Nappa, ta sẽ đưa ngươi đến đó",
                                        "Đến Cold", "Đến\nNappa", "Từ chối");
                            }
                        }
                        case 68 ->
                            this.createOtherMenu(pl, ConstNpc.BASE_MENU,
                                    "Ngươi muốn về Thành Phố Vegeta", "Về TP\nVegeta", "Từ chối");
                        default ->
                            this.createOtherMenu(pl, ConstNpc.BASE_MENU,
                                    "Tàu vũ trụ Xayda sử dụng công nghệ mới nhất, "
                                    + "có thể đưa ngươi đi bất kỳ đâu, chỉ cần trả tiền là được.",
                                    "Đến\nTrái Đất", "Đến\nNamếc", "Siêu thị");
                    }
                }
            }
        }
    }

    /**
     * Đưa người chơi tới chỗ boss đang sống, thu COST_FIND_BOSS vàng.
     * Gộp từ 3 khối trùng lặp của tuyến cũ (Kuku / Mập đầu đinh / Rambo).
     */
    private void goToBoss(Player player, int bossId) {
        Boss boss = BossManager.gI().getBossById(bossId);
        if (boss != null && !boss.isDie() && boss.zone != null && !MapService.gI().isMapPhoBan(boss.zone.map.mapId)) {
            if (player.inventory.gold >= COST_FIND_BOSS) {
                Zone z = MapService.gI().getMapCanJoin(player, boss.zone.map.mapId, boss.zone.zoneId);
                if (z.getNumOfPlayers() < z.maxPlayer) {
                    player.inventory.gold -= COST_FIND_BOSS;
                    ChangeMapService.gI().changeMap(player, boss.zone, boss.location.x, boss.location.y);
                    Service.gI().sendMoney(player);
                } else {
                    Service.gI().sendThongBao(player, "Khu vực đang full.");
                }
            } else {
                Service.gI().sendThongBao(player, "Không đủ vàng, còn thiếu "
                        + Util.numberToMoney(COST_FIND_BOSS - player.inventory.gold) + " vàng");
            }
            return;
        }
        Service.gI().sendThongBao(player, "Chết rồi ba...");
    }

    @Override
    public void confirmMenu(Player player, int select) {
        if (canOpenNpc(player)) {
            if (this.mapId == 26) {
                if (player.idMark.isBaseMenu()) {
                    switch (select) {
                        case 0 ->
                            ChangeMapService.gI().changeMapBySpaceShip(player, 24, -1, -1);
                        case 1 ->
                            ChangeMapService.gI().changeMapBySpaceShip(player, 25, -1, -1);
                        case 2 ->
                            ChangeMapService.gI().changeMapBySpaceShip(player, 84, -1, -1);
                    }
                }
            }
            if (this.mapId == 19) {
                if (player.idMark.isBaseMenu()) {
                    switch (select) {
                        case 0 ->
                            ChangeMapService.gI().changeMapBySpaceShip(player, 109, -1, 295);
                        case 1 ->
                            ChangeMapService.gI().changeMapBySpaceShip(player, 68, -1, 90);
                    }
                } else if (player.idMark.getIndexMenu() == ConstNpc.MENU_FIND_KUKU) {
                    // TUYẾN MỚI: một menu duy nhất cho cả 3 boss của NV 20 / NV 48.
                    // Hai nhánh MENU_FIND_MAP_DAU_DINH và MENU_FIND_RAMBO của tuyến cũ đã được gộp vào đây.
                    switch (select) {
                        case 0 ->
                            goToBoss(player, BossID.KUKU);
                        case 1 ->
                            goToBoss(player, BossID.MAP_DAU_DINH);
                        case 2 ->
                            goToBoss(player, BossID.RAMBO);
                        case 3 ->
                            ChangeMapService.gI().changeMapBySpaceShip(player, 109, -1, 295);
                        case 4 ->
                            ChangeMapService.gI().changeMapBySpaceShip(player, 68, -1, 90);
                    }
                }
            }
            if (this.mapId == 68) {
                if (player.idMark.isBaseMenu()) {
                    switch (select) {
                        case 0 ->
                            ChangeMapService.gI().changeMapBySpaceShip(player, 19, -1, 1100);
                    }
                }
            }
        }
    }
}
