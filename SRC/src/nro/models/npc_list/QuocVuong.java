package nro.models.npc_list;

import nro.models.consts.ConstNpc;
import nro.models.item.Item;
import nro.models.npc.Npc;
import nro.models.player.NPoint;
import nro.models.player.Player;
import nro.models.services.OpenPowerService;
import nro.models.services.Service;
import nro.models.utils.Util;
import nro.models.services.TaskService;

public class QuocVuong extends Npc {

    private static final byte MAX_LIMIT_CUSTOM = 5;

    public QuocVuong(int mapId, int status, int cx, int cy, int tempId, int avartar) {
        super(mapId, status, cx, cy, tempId, avartar);
    }

    @Override
    public void openBaseMenu(Player player) {
        // TUYẾN MỚI: B10 — chỉ hiện nút "Phá giới hạn (nhiệm vụ)" khi người chơi
        // đang đứng ĐÚNG bước nhiệm vụ và đúng bậc giới hạn. Nút mới chèn ở vị trí 2,
        // "Từ chối" đẩy xuống vị trí 3 (vị trí này vốn không có case xử lý nên không đổi hành vi).
        if (TaskService.gI().canOpenPowerByTask(player)) {
            this.createOtherMenu(player, ConstNpc.BASE_MENU,
                    "Con muốn nâng giới hạn sức mạnh cho bản thân hay đệ tử?",
                    "Bản thân", "Đệ tử", "Phá giới hạn\n(nhiệm vụ)", "Từ chối");
            return;
        }
        this.createOtherMenu(player, ConstNpc.BASE_MENU,
                "Con muốn nâng giới hạn sức mạnh cho bản thân hay đệ tử?",
                "Bản thân", "Đệ tử", "Từ chối");
    }

    @Override
    public void confirmMenu(Player player, int select) {
        if (canOpenNpc(player)) {
            if (player.idMark.isBaseMenu()) {
                switch (select) {

                    case 0 -> {
                        if (player.nPoint.limitPower < MAX_LIMIT_CUSTOM) {
                            this.createOtherMenu(player, ConstNpc.OPEN_POWER_MYSEFT,
                                    "Ta sẽ truyền năng lượng giúp con mở giới hạn sức mạnh của bản thân lên "
                                    + Util.numberToMoney(player.nPoint.getPowerNextLimit()),
                                    "Nâng\ngiới hạn\nsức mạnh",
                                    "Nâng ngay\n" + Util.numberToMoney(OpenPowerService.COST_SPEED_OPEN_LIMIT_POWER) + " vàng",
                                    "Đóng");
                        } else {
                            this.createOtherMenu(player, ConstNpc.IGNORE_MENU,
                                    "Sức mạnh của con đã đạt tới giới hạn hiện tại",
                                    "Đóng");
                        }
                    }

                    case 1 -> {
                        if (player.pet != null) {
                            if (player.pet.nPoint.limitPower < MAX_LIMIT_CUSTOM) {
                                this.createOtherMenu(player, ConstNpc.OPEN_POWER_PET,
                                        "Ta sẽ truyền năng lượng giúp con mở giới hạn sức mạnh của đệ tử lên "
                                        + Util.numberToMoney(player.pet.nPoint.getPowerNextLimit()),
                                        "Nâng ngay\n" + Util.numberToMoney(OpenPowerService.COST_SPEED_OPEN_LIMIT_POWER) + " ngọc", "Đóng");
                            } else {
                                this.createOtherMenu(player, ConstNpc.IGNORE_MENU,
                                        "Sức mạnh của đệ con đã đạt tới giới hạn hiện tại",
                                        "Đóng");
                            }
                        } else {
                            Service.gI().sendThongBao(player, "Không thể thực hiện");
                        }
                    }

                    // TUYẾN MỚI: B10 — mở giới hạn miễn phí theo nhiệm vụ.
                    // canOpenPowerByTask được hỏi lại ở đây nên nếu người chơi không ở
                    // đúng bước (nút này là "Từ chối") thì không có gì xảy ra.
                    case 2 -> {
                        if (TaskService.gI().canOpenPowerByTask(player)) {
                            OpenPowerService.gI().openPowerByTask(player);
                        }
                    }
                }
            } else if (player.idMark.getIndexMenu() == ConstNpc.OPEN_POWER_MYSEFT) {
                switch (select) {
                    case 0 -> {
                        if (player.nPoint.limitPower < MAX_LIMIT_CUSTOM) {
                            OpenPowerService.gI().openPowerBasic(player);
                        } else {
                            Service.gI().sendThongBao(player, "Giới hạn sức mạnh của bạn đã đạt tới mức tối đa hiện tại");
                        }
                    }
                    case 1 -> {
                        if (player.nPoint.limitPower < MAX_LIMIT_CUSTOM) {
                            if (player.inventory.gold >= OpenPowerService.COST_SPEED_OPEN_LIMIT_POWER) {
                                if (OpenPowerService.gI().openPowerSpeed(player)) {
                                    player.inventory.gold -= OpenPowerService.COST_SPEED_OPEN_LIMIT_POWER;
                                    Service.gI().sendMoney(player);
                                }
                            } else {
                                Service.gI().sendThongBao(player,
                                        "Bạn không đủ vàng để mở, còn thiếu "
                                        + Util.numberToMoney((OpenPowerService.COST_SPEED_OPEN_LIMIT_POWER - player.inventory.gold)) + " ngọc");
                            }
                        } else {
                            Service.gI().sendThongBao(player, "Giới hạn sức mạnh của bạn đã đạt tới mức tối đa hiện tại");
                        }
                    }
                }
            } else if (player.idMark.getIndexMenu() == ConstNpc.OPEN_POWER_PET) {
                if (select == 0) {
                    if (player.pet.nPoint.limitPower < MAX_LIMIT_CUSTOM) {
                        if (player.inventory.gold >= OpenPowerService.COST_SPEED_OPEN_LIMIT_POWER) {
                            if (OpenPowerService.gI().openPowerSpeed(player.pet)) {
                                player.inventory.gold -= OpenPowerService.COST_SPEED_OPEN_LIMIT_POWER;
                                Service.gI().sendMoney(player);
                            }
                        } else {
                            Service.gI().sendThongBao(player,
                                    "Bạn không đủ vàng để mở, còn thiếu "
                                    + Util.numberToMoney((OpenPowerService.COST_SPEED_OPEN_LIMIT_POWER - player.inventory.gold)) + " ngọc");
                        }
                    } else {
                        Service.gI().sendThongBao(player, "Giới hạn sức mạnh của đệ tử đã đạt tới mức tối đa hiện tại");
                    }
                }
            }
        }
    }
}
