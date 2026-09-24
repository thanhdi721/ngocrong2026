package nro.models.npc_list;

import nro.models.consts.ConstNpc;
import nro.models.map.service.ChangeMapService;
import nro.models.network.Message;
import nro.models.npc.Npc;
import nro.models.player.Player;
import nro.models.services.Service;

/**
 * NPC "GoKu Nỗi Loạn" đứng cạnh Chi Chi ở đảo Kamê (map 5).
 *
 * <p>Chỉ có một việc: bật / tắt hào quang {@value #AURA_ID} ("Goku Purple") cho người chơi.
 * Bật rồi thì {@link Player#getAura()} trả về hào quang này, đè lên hào quang của thẻ rađa,
 * và trạng thái được lưu ở cột {@code player.aura_npc} nên đăng nhập lại vẫn còn.
 */
public class GokuNoiLoan extends Npc {

    /**
     * Hào quang Goku Purple — ảnh {@code data/img_by_name/x1..x4/aura_96_0.png}, 12 khung.
     * 96 là bản thu nhỏ 50% của 95; đổi id chứ không ghi đè 95 vì client cache ảnh
     * img_by_name theo TÊN, ghi đè thì máy đã tải rồi vẫn hiện ảnh cũ.
     */
    public static final int AURA_ID = 96;

    public GokuNoiLoan(int mapId, int status, int cx, int cy, int tempId, int avartar) {
        super(mapId, status, cx, cy, tempId, avartar);
    }

    private boolean dangBat(Player player) {
        return player.auraNpc == AURA_ID;
    }

    @Override
    public void openBaseMenu(Player player) {
        if (canOpenNpc(player)) {
            if (dangBat(player)) {
                createOtherMenu(player, ConstNpc.BASE_MENU,
                        "Hào quang của ta đang cháy trên người ngươi đó.\nMuốn tắt đi thì nói một tiếng.",
                        "Tắt\nhào quang", "Đóng");
            } else {
                createOtherMenu(player, ConstNpc.BASE_MENU,
                        "Ngươi muốn mượn hào quang của ta không?\nKhoác vào cho ra dáng một chút.",
                        "Bật\nhào quang", "Đóng");
            }
        }
    }

    @Override
    public void confirmMenu(Player player, int select) {
        if (canOpenNpc(player) && player.idMark.getIndexMenu() == ConstNpc.BASE_MENU && select == 0) {
            boolean bat = !dangBat(player);
            player.auraNpc = bat ? AURA_ID : -1;
            guiHaoQuang(player);
            veLaiKhu(player);
            Service.gI().sendThongBao(player, bat ? "Đã bật hào quang" : "Đã tắt hào quang");
        }
    }

    /**
     * Báo cho mọi người trong khu biết hào quang vừa đổi. Gói tin 127/4 chính là gói
     * {@code RadarService.sendAura} của hệ thống thẻ rađa, chỉ khác là gửi trong khu
     * thay vì cả server.
     */
    private void guiHaoQuang(Player player) {
        try {
            Message msg = new Message(127);
            msg.writer().writeByte(4);
            msg.writer().writeInt((int) player.id);
            msg.writer().writeShort(player.getAura());
            msg.writer().writeByte(player.getEffFront());
            msg.writer().flush();
            Service.gI().sendMessAllPlayerInMap(player, msg);
        } catch (Exception e) {
        }
    }

    /**
     * Vào lại đúng khu đang đứng. Gói tin ở trên chỉ đổi hào quang tại chỗ; vào lại khu
     * thì client dựng lại nhân vật từ đầu nên chắc chắn thấy hào quang mới, kể cả khi
     * bản client không xử lý gói 127/4.
     */
    private void veLaiKhu(Player player) {
        try {
            if (player.zone != null && player.zone.map != null) {
                // Dùng thẳng changeMap chứ không qua changeZone: changeZone có nhịp chờ 5 giây
                // và sẽ nhả câu "Chưa thể chuyển khu vực lúc này" làm người chơi tưởng hỏng.
                ChangeMapService.gI().changeMap(player, player.zone, player.location.x, player.location.y);
            }
        } catch (Exception e) {
        }
    }
}
