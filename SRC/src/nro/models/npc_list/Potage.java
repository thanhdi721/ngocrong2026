package nro.models.npc_list;
import nro.models.boss.Boss_Manager.BossManager;
import nro.models.consts.ConstNpc;
import nro.models.npc.Npc;
import nro.models.player.Player;
import nro.models.map.service.NpcService;
import nro.models.services.Service;
import nro.models.utils.Util;
import nro.models.consts.ConstTask;
import nro.models.services.TaskService;

public class Potage extends Npc {

    public Potage(int mapId, int status, int cx, int cy, int tempId, int avartar) {
        super(mapId, status, cx, cy, tempId, avartar);
    }

    /** TUYẾN MỚI: B14 — id menu riêng cho điểm rẽ nhánh 2 (NV 31 ↔ NV 49). */
    private static final int MENU_RE_NHANH_NV31 = 2201;

    @Override
    public void openBaseMenu(Player player) {
        // FIX: báo hệ thống nhiệm vụ khi người chơi nói chuyện với NPC này. Trước đây NPC
        // không gọi nên các bước nhiệm vụ "gặp / nói chuyện với" NPC này KHÔNG BAO GIỜ xong.
        // Chỉ dừng lại khi vừa hoàn thành một bước (TaskService tự gửi câu "Việc tiếp theo");
        // còn lại vẫn mở menu bình thường.
        if (canOpenNpc(player) && nro.models.services.TaskService.gI().checkDoneTaskTalkNpc(player, this)) {
            return;
        }
        if (canOpenNpc(player)) {
            // Lỡ dùng Bình chứa Commeson sớm thì xin lại ở đây (NV 49 bước 4 / bước 5).
            TaskService.gI().ensureCommesonBottle(player);
            // TUYẾN MỚI: B14 — đang ở bước TASK_31_1 thì mở menu 2 nút rẽ nhánh,
            // KHÔNG đụng vào menu Commeson cũ (dùng id menu riêng nên không lẫn).
            if (this.mapId == 140 && TaskService.gI().getIdTask(player) == ConstTask.TASK_31_1) {
                this.createOtherMenu(player, MENU_RE_NHANH_NV31,
                        "Bản sao của ngươi đã bị khuất phục.\nNgươi muốn tiêu diệt nó, hay thu nhận nó?",
                        "Tiêu diệt", "Thu nhận");
                return;
            }
            if (this.mapId == 140) {
                Player BossClone = BossManager.gI().findBossClone(player);
                if (BossClone != null) {
                    this.createOtherMenu(player, ConstNpc.BASE_MENU,
                            "Đang có 1 nhân bản của " + BossClone.name + " hãy chờ kết quả trận đấu",
                            "OK");
                } else {
                    this.createOtherMenu(player, ConstNpc.BASE_MENU, "Hãy giúp ta đánh bại bản sao\nNgươi chỉ có 5 phút để hạ hắn\nPhần thưởng cho ngươi là 1 bình Commeson",
                            "Hướng\ndẫn\nthêm", "OK", "Từ chối");
                }
            }
        }
    }

    @Override
    public void confirmMenu(Player player, int select) {
        if (canOpenNpc(player)) {
            // TUYẾN MỚI: B14 — chuyển lựa chọn sang TaskService, mọi luật rẽ nhánh nằm ở đó.
            if (player.idMark.getIndexMenu() == MENU_RE_NHANH_NV31) {
                TaskService.gI().checkDoneTaskConfirmMenuNpc(player, this, (byte) select);
                return;
            }
            if (this.mapId == 140) {
                if (player.idMark.isBaseMenu()) {
                    Player BossClone = BossManager.gI().findBossClone(player);
                    if (BossClone == null) {
                        switch (select) {
                            case 0 ->
                                NpcService.gI().createTutorial(player, tempId, this.avartar, "Thứ bị phong ấn tại đây là vũ khí có tên Commesonđược tạo ra nhằm bảo vệ cho hành tinh PotaufeuTuy nhiên nó đã tàn phá mọi thứ trong quá khứ\nKhiến cư dân Potaufeu niêm phong nó với cái giáphải trả là mạng sống của họTa, Potage là người duy nhất sống sótvà ta đã bảo vệ phong ấn hơn một trăm năm.\nTuy nhiên bọn xâm lượt Gryll đã đến và giải thoát CommesonHãy giúp ta tiêu diệt bản sao do Commeson tạo ravà niêm phong Commeson một lần và mãi mãi");
                            case 1 -> {
                                if (!Util.isAfterMidnight(player.lastPkCommesonTime) && !player.isAdmin()) {
                                    Service.gI().sendThongBao(player, "Hãy chờ đến ngày mai");
                                } else {
                                    Service.gI().callNhanBan(player);
                                }
                            }
                        }
                    }
                }
            }
        }
    }

}
