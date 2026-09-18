package nro.models.services;

import nro.models.player.NPoint;
import nro.models.player.Pet;
import nro.models.player.Player;

/**
 *
 * @author By Mr Blue
 * 
 */

public class OpenPowerService {

    public static final int COST_SPEED_OPEN_LIMIT_POWER = 50000000;

    private static OpenPowerService i;

    private OpenPowerService() {

    }

    public static OpenPowerService gI() {
        if (i == null) {
            i = new OpenPowerService();
        }
        return i;
    }

    public boolean openPowerBasic(Player player) {
        byte curLimit = player.nPoint.limitPower;
        if (curLimit < NPoint.MAX_LIMIT) {
            if (!player.itemTime.isOpenPower && player.nPoint.canOpenPower()) {
                player.itemTime.isOpenPower = true;
                player.itemTime.lastTimeOpenPower = System.currentTimeMillis();
                ItemTimeService.gI().sendAllItemTime(player);
                return true;
            } else {
                Service.gI().sendThongBao(player, "Sức mạnh của bạn không đủ để thực hiện");
                return false;
            }
        } else {
            Service.gI().sendThongBao(player, "Sức mạnh của bạn đã đạt tới mức tối đa");
            return false;
        }
    }

    /**
     * TUYẾN MỚI: B10 — mở giới hạn sức mạnh MIỄN PHÍ MỘT LẦN theo nhiệm vụ.
     *
     * <p>Khác hai hàm trên ở ba điểm: <b>không trừ tiền</b>, <b>không phải chờ 2,4 giờ</b>
     * (không dùng {@code itemTime.isOpenPower}), và <b>không cần chạm trần sức mạnh</b>.</p>
     *
     * <p>Chỉ chạy khi {@code TaskService.canOpenPowerByTask} trả true — hàm đó ràng
     * cặp <i>bước nhiệm vụ ↔ bậc giới hạn hiện tại</i> (TASK_33_3 ↔ 0, TASK_44_4 ↔ 1,
     * TASK_47_3 / TASK_50_3 ↔ 2) nên sau khi tăng một bậc thì lần gọi kế tiếp tự thất bại:
     * mỗi bước nhiệm vụ chỉ mở được đúng một lần.</p>
     *
     * @return true nếu vừa mở thành công
     */
    public boolean openPowerByTask(Player player) {
        if (player == null || !player.isPl() || player.nPoint == null) {
            return false;
        }
        if (!TaskService.gI().canOpenPowerByTask(player)) {
            Service.gI().sendThongBao(player, "Con chưa đến lúc phá giới hạn sức mạnh");
            return false;
        }
        if (player.nPoint.limitPower >= NPoint.MAX_LIMIT) {
            Service.gI().sendThongBao(player, "Sức mạnh của bạn đã đạt tới mức tối đa");
            return false;
        }
        player.nPoint.limitPower++;
        Service.gI().sendThongBao(player, "Giới hạn sức mạnh của bạn đã được phá vỡ, tăng lên 1 bậc");
        // Gọi SAU khi limitPower đã tăng, để bước nhiệm vụ được tính là đã xong.
        TaskService.gI().checkDoneTaskOpenPower(player);
        return true;
    }

    public boolean openPowerSpeed(Player player) {
        if (player.nPoint.limitPower < NPoint.MAX_LIMIT) {
            player.nPoint.limitPower++;
            if (!player.isPet) {
                Service.gI().sendThongBao(player, "Giới hạn sức mạnh của bạn đã được tăng lên 1 bậc");
            } else {
                Service.gI().sendThongBao(((Pet) player).master, "Giới hạn sức mạnh của đệ tử đã được tăng lên 1 bậc");
            }
            return true;
        } else {
            if (!player.isPet) {
                Service.gI().sendThongBao(player, "Sức mạnh của bạn đã đạt tới mức tối đa");
            } else {
                Service.gI().sendThongBao(((Pet) player).master, "Sức mạnh của đệ tử đã đạt tới mức tối đa");
            }
            return false;
        }
    }

}
