package nro.models.npc_list;import nro.models.npc.Npc;
import nro.models.player.Player;
import nro.models.map.service.ChangeMapService;
import nro.models.utils.Util;

import java.time.LocalTime;
import java.util.Calendar;
import nro.models.services.Service;
import nro.models.utils.TimeUtil;

public class Tapion extends Npc {

    public Tapion(int mapId, int status, int cx, int cy, int tempId, int avartar) {
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
            if (mapId == 19) {
                this.createOtherMenu(player, 0, "Ác quỷ truyền thuyết Hirudegarn\nđã thoát khỏi phong ấn ngàn năm\nHãy giúp tôi chế ngự nó", "OK", "Từ chối");
            } else if (mapId == 126) {
                this.createOtherMenu(player, 0, "Tôi sẽ đưa bạn về", "OK", "Từ chối");
            }
        }
    }

    @Override
    public void confirmMenu(Player player, int select) {
        if (canOpenNpc(player)) {
            switch (select) {
                case 0 -> {
                    if (mapId == 19) {
                        Calendar calendar = Calendar.getInstance();
                        int hour = calendar.get(Calendar.HOUR_OF_DAY);
                        if (hour >= 1 && hour < 23) {
                            ChangeMapService.gI().changeMapNonSpaceship(player, 126, 200 + Util.nextInt(-100, 100), 360);
                        }else{
                            Service.gI().sendThongBao(player, "Vui lòng quay lại vào lúc 22h");
                        }
                    } else if (mapId == 126) {
                        ChangeMapService.gI().changeMapNonSpaceship(player, 19, 1000 + Util.nextInt(-100, 100), 360);
                    }
                }
            }
        }
    }
}