package nro.models.npc_list;
import nro.models.consts.ConstMap;
import nro.models.consts.ConstNpc;
import nro.models.npc.Npc;
import nro.models.player.Player;
import nro.models.map.service.NpcService;
import nro.models.map.service.ChangeMapService;
import nro.models.utils.Logger;
import nro.models.utils.TimeUtil;

public class RongOmega extends Npc {

    public RongOmega(int mapId, int status, int cx, int cy, int tempId, int avartar) {
        super(mapId, status, cx, cy, tempId, avartar);
    }

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
            if (this.mapId == 24 || this.mapId == 25 || this.mapId == 26) {
                try {
                    // ĐÃ BỎ nút "Nhận thưởng": nó gọi RewardBlackBall.getReward(), mà hàm
                    // đó chỉ in "Chỉ Số Tự Cộng Khi Nhặt xong" rồi thôi — không phát gì cả.
                    // Buff 22 giờ vốn đã tự cộng cho cả bang ngay lúc thắng trận.
                    if (TimeUtil.isBlackBallWarOpen()) {
                        this.createOtherMenu(player, ConstNpc.MENU_OPEN_BDW, "Đường đến với ngọc rồng sao đen đã mở, "
                                + "ngươi có muốn tham gia không?",
                                "Hướng\ndẫn\nthêm", "Tham gia", "Từ chối");
                    } else {
                        this.createOtherMenu(player, ConstNpc.MENU_NOT_OPEN_BDW,
                                "Ta có thể giúp gì cho ngươi?", "Hướng\ndẫn\nthêm", "Từ chối");
                    }
                } catch (Exception ex) {
                    Logger.error("Lỗi mở menu rồng Omega");
                }
            }
        }
    }

    @Override
    public void confirmMenu(Player player, int select) {
        if (canOpenNpc(player)) {
            switch (player.idMark.getIndexMenu()) {
                case ConstNpc.MENU_OPEN_BDW -> {
                    switch (select) {
                        case 0 ->
                            NpcService.gI().createTutorial(player, tempId, this.avartar, ConstNpc.HUONG_DAN_BLACK_BALL_WAR);
                        case 1 -> {
                            player.idMark.setTypeChangeMap(ConstMap.CHANGE_BLACK_BALL);
                            ChangeMapService.gI().openChangeMapTab(player);
                        }
                        // select 2 = "Từ chối"
                        default -> {
                        }
                    }
                }
                case ConstNpc.MENU_NOT_OPEN_BDW -> {
                    // select 1 = "Từ chối"
                    if (select == 0) {
                        NpcService.gI().createTutorial(player, tempId, this.avartar, ConstNpc.HUONG_DAN_BLACK_BALL_WAR);
                    }
                }
            }
        }
    }
}
