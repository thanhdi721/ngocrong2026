package nro.models.services;

import nro.models.consts.ConstPlayer;
import nro.models.item.Item;
import static nro.models.item.ItemTime.*;
import nro.models.player.Fusion;
import nro.models.player.Player;
import nro.models.network.Message;
import java.io.IOException;
import nro.models.map.phoban.BanDoKhoBau;
import nro.models.map.phoban.SnakeWay;
import nro.models.map.phoban.DestronGas;
import nro.models.map.phoban.RedRibbonHQ;
import nro.models.utils.Logger;

/**
 *
 * @author By Mr Blue
 *
 */
public class ItemTimeService {

    private static ItemTimeService i;

    public static ItemTimeService gI() {
        if (i == null) {
            i = new ItemTimeService();
        }
        return i;
    }

    /** Số giây còn lại của một hiệu lực, không bao giờ âm. */
    private static int conLai(long lucBat, long hanDung) {
        long con = (hanDung - (System.currentTimeMillis() - lucBat)) / 1000;
        return (int) Math.max(0, con);
    }

    /** Icon thật của một vật phẩm; chưa chạy patch thì trả 0 để client khỏi xin icon rác. */
    private static int iconDan(int idVatPham) {
        try {
            if (idVatPham >= 0 && idVatPham < nro.models.server.Manager.ITEM_TEMPLATES.size()) {
                return nro.models.server.Manager.ITEM_TEMPLATES.get(idVatPham).iconID;
            }
        } catch (Exception e) {
        }
        return 0;
    }

    public void sendAllItemTime(Player player) {
        ItemTimeService.gI().sendTextBanDoKhoBau(player);
        ItemTimeService.gI().sendTextDoanhTrai(player);
        ItemTimeService.gI().sendTextConDuongRanDoc(player);
        ItemTimeService.gI().sendTextKhiGasHuyDiet(player);
        ItemTimeService.gI().sendTextTimePickDoanhTrai(player);
        if (player.fusion.typeFusion == ConstPlayer.LUONG_LONG_NHAT_THE) {
            sendItemTime(player, player.gender == ConstPlayer.NAMEC ? 3901 : 3790,
                    (int) ((Fusion.TIME_FUSION - (System.currentTimeMillis() - player.fusion.lastTimeFusion)) / 1000));
        }
        if (player.itemTime.isUseBoHuyet) {
            sendItemTime(player, 2755, (int) ((TIME_ITEM - (System.currentTimeMillis() - player.itemTime.lastTimeBoHuyet)) / 1000));
        }
        if (player.itemTime.isUseBoKhi) {
            sendItemTime(player, 2756, (int) ((TIME_ITEM - (System.currentTimeMillis() - player.itemTime.lastTimeBoKhi)) / 1000));
        }
        if (player.itemTime.isUseGiapXen) {
            sendItemTime(player, 2757, (int) ((TIME_ITEM - (System.currentTimeMillis() - player.itemTime.lastTimeGiapXen)) / 1000));
        }
        if (player.itemTime.isUseCuongNo) {
            sendItemTime(player, 2754, (int) ((TIME_ITEM - (System.currentTimeMillis() - player.itemTime.lastTimeCuongNo)) / 1000));
        }

        //===== đan của map Tu Tiên (patch 83) + đan thượng phẩm (patch 84) =====
        // Icon TRA THEO VẬT PHẨM chứ không viết cứng: đan tiệm và đan thượng phẩm dùng
        // chung một ô hiệu lực, viết cứng thì hiện nhầm ảnh. Thời gian cũng lấy `hanDanX`
        // chứ không lấy TIME_ITEM, nếu không viên 20 phút sẽ đếm lùi thành số âm sau 10 phút.
        if (player.itemTime.isUseDanSucDanh) {
            sendItemTime(player, iconDan(player.itemTime.mucDanSucDanh > MUC_GOC_SUC_DANH
                    ? nro.models.tu_tien.LuyenDan.DAN_TP_SUC_DANH
                    : nro.models.tu_tien.TuTien.DAN_SUC_DANH),
                    conLai(player.itemTime.lastTimeDanSucDanh, player.itemTime.hanDanSucDanh));
        }
        if (player.itemTime.isUseDanHp) {
            sendItemTime(player, iconDan(player.itemTime.mucDanHp > MUC_GOC_HP
                    ? nro.models.tu_tien.LuyenDan.DAN_TP_HP
                    : nro.models.tu_tien.TuTien.DAN_HP),
                    conLai(player.itemTime.lastTimeDanHp, player.itemTime.hanDanHp));
        }
        if (player.itemTime.isUseDanKi) {
            sendItemTime(player, iconDan(player.itemTime.mucDanKi > MUC_GOC_KI
                    ? nro.models.tu_tien.LuyenDan.DAN_TP_KI
                    : nro.models.tu_tien.TuTien.DAN_KI),
                    conLai(player.itemTime.lastTimeDanKi, player.itemTime.hanDanKi));
        }
        if (player.itemTime.isUseDanGiap) {
            sendItemTime(player, iconDan(player.itemTime.mucDanGiap > MUC_GOC_GIAP
                    ? nro.models.tu_tien.LuyenDan.DAN_TP_GIAP
                    : nro.models.tu_tien.TuTien.DAN_GIAP),
                    conLai(player.itemTime.lastTimeDanGiap, player.itemTime.hanDanGiap));
        }
        if (player.itemTime.isUseDanChiMang) {
            sendItemTime(player, iconDan(player.itemTime.mucDanChiMang > MUC_GOC_CHI_MANG
                    ? nro.models.tu_tien.LuyenDan.DAN_TP_CHI_MANG
                    : nro.models.tu_tien.TuTien.DAN_CHI_MANG),
                    conLai(player.itemTime.lastTimeDanChiMang, player.itemTime.hanDanChiMang));
        }
        if (player.itemTime.isUseTuLinhPhu) {
            sendItemTime(player, 30202, (int) ((nro.models.tu_tien.TuTien.THOI_GIAN_BUA
                    - (System.currentTimeMillis() - player.itemTime.lastTimeTuLinhPhu)) / 1000));
        }

        if (player.itemTime.isUseAnDanh) {
            sendItemTime(player, 2760, (int) ((TIME_ITEM - (System.currentTimeMillis() - player.itemTime.lastTimeAnDanh)) / 1000));
        }
        if (player.itemTime.isUseBoHuyet2) {
            sendItemTime(player, 10714, (int) ((TIME_ITEM - (System.currentTimeMillis() - player.itemTime.lastTimeBoHuyet2)) / 1000));
        }
        if (player.itemTime.isUseBoKhi2) {
            sendItemTime(player, 10715, (int) ((TIME_ITEM - (System.currentTimeMillis() - player.itemTime.lastTimeBoKhi2)) / 1000));
        }
        if (player.itemTime.isUseGiapXen2) {
            sendItemTime(player, 10712, (int) ((TIME_ITEM - (System.currentTimeMillis() - player.itemTime.lastTimeGiapXen2)) / 1000));
        }
        if (player.itemTime.isUseCuongNo2) {
            sendItemTime(player, 10716, (int) ((TIME_ITEM - (System.currentTimeMillis() - player.itemTime.lastTimeCuongNo2)) / 1000));
        }

        if (player.itemTime.isUseAnDanh2) {
            sendItemTime(player, 10717, (int) ((TIME_ITEM - (System.currentTimeMillis() - player.itemTime.lastTimeAnDanh2)) / 1000));
        }
        if (player.itemTime.isUseCMS) {
            sendItemTime(player, 5829, (int) ((TIME_CMS - (System.currentTimeMillis() - player.itemTime.lastTimeUseCMS)) / 1000));
        }
        if (player.itemTime.isUseNCD) {
            sendItemTime(player, 11173, (int) ((TIME_NCD - (System.currentTimeMillis() - player.itemTime.lastTimeUseNCD)) / 1000));
        }
        if (player.itemTime.isUseGTPT) {
            sendItemTime(player, 3778, (int) ((TIME_ITEM - (System.currentTimeMillis() - player.itemTime.lastTimeUseGTPT)) / 1000));
        }
        if (player.itemTime.isUseDK) {
            sendItemTime(player, 5072, (int) ((TIME_DK - (System.currentTimeMillis() - player.itemTime.lastTimeUseDK)) / 1000));
        }
        if (player.itemTime.isOpenPower) {
            sendItemTime(player, 3783, (int) ((TIME_OPEN_POWER - (System.currentTimeMillis() - player.itemTime.lastTimeOpenPower)) / 1000));
        }
        if (player.itemTime.isUseMayDo) {
            sendItemTime(player, 2758, (int) ((TIME_MAY_DO - (System.currentTimeMillis() - player.itemTime.lastTimeUseMayDo)) / 1000));
        }
        if (player.itemTime.isUseCoBonLa) {
            sendItemTime(player, 13618, (int) ((TIME_CO_BON_LA - (System.currentTimeMillis() - player.itemTime.lastTimeUseCoBonLa)) / 1000));
        }
        if (player.itemTime.isUseNuocMia1) {
            sendItemTime(player, 13462, (int) ((TIME_NUOC_MIA1 - (System.currentTimeMillis() - player.itemTime.lastTimeUseNuocMia1)) / 1000));
        }
        if (player.itemTime.isUseNuocMia2) {
            sendItemTime(player, 13463, (int) ((TIME_NUOC_MIA2 - (System.currentTimeMillis() - player.itemTime.lastTimeUseNuocMia2)) / 1000));
        }
        if (player.itemTime.isUseNuocMia2) {
            sendItemTime(player, 13464, (int) ((TIME_NUOC_MIA3 - (System.currentTimeMillis() - player.itemTime.lastTimeUseNuocMia3)) / 1000));
        }
        if (player.itemTime.isUseKilis) {
            long remainingTime = (player.itemTime.lastTimeUseKilis + player.itemTime.timeLengthKilis) - System.currentTimeMillis();
            if (remainingTime > 0) {
                sendItemTime(player, 15359, (int) (remainingTime / 1000));
            }
        }
        if (player.itemTime.isUseKhoBauX2) {
        //    sendItemTime(player, 12834, (int) ((TIME_MAY_DO - (System.currentTimeMillis() - player.itemTime.lastTimeUseKhoBauX2)) / 1000));
        }
        if (player.itemTime.isUseBuaSanta) {
            sendItemTime(player, 13540, (int) ((TIME_BUA_SANTA - (System.currentTimeMillis() - player.itemTime.lastTimeBuaSanta)) / 1000));
        }
        if (player.itemTime.isEatMeal) {
            sendItemTime(player, player.itemTime.iconMeal, (int) ((TIME_EAT_MEAL - (System.currentTimeMillis() - player.itemTime.lastTimeEatMeal)) / 1000));
        }
        if (player.itemTime.isEatMeal2) {
            sendItemTime(player, player.itemTime.iconMeal2, (int) ((TIME_EAT_MEAL - (System.currentTimeMillis() - player.itemTime.lastTimeEatMeal2)) / 1000));
        }
        if (player.itemTime.isUseTDLT) {
            sendItemTime(player, 4387, player.itemTime.timeTDLT / 1000);
        }
        if (player.itemTime.isUseRX) {
            sendItemTime(player, 8579, player.itemTime.timeRX / 1000);
        }
    }

    public void turnOnTDLT(Player player, Item item) {
        int min = 0;
        int minleft = 0;
        for (Item.ItemOption io : item.itemOptions) {
            if (io.optionTemplate.id == 1) {
                min = io.param;
                minleft = Math.max((min * 60 - 30000) / 60, 0);
                io.param = minleft > 0 ? minleft : 0;
                break;
            }
        }

        player.itemTime.isUseTDLT = true;

        int timeTDLTInMinutes = min - minleft;
        int timeTDLTInSeconds = timeTDLTInMinutes * 60;
        int maxTimeInSeconds = 30000;
        int timeTDLTInSecondsLimited = Math.min(timeTDLTInSeconds, maxTimeInSeconds);

        player.itemTime.timeTDLT = timeTDLTInSecondsLimited * 1000;

        player.itemTime.lastTimeUseTDLT = System.currentTimeMillis();
        sendCanAutoPlay(player);
        sendItemTime(player, 4387, timeTDLTInSecondsLimited);
        InventoryService.gI().sendItemBags(player);
    }

    public void turnOffTDLT(Player player, Item item) {
        player.itemTime.isUseTDLT = false;
        for (Item.ItemOption io : item.itemOptions) {
            if (io.optionTemplate.id == 1) {
                io.param += (short) ((player.itemTime.timeTDLT - (System.currentTimeMillis() - player.itemTime.lastTimeUseTDLT)) / 60 / 1000);
                break;
            }
        }
        sendCanAutoPlay(player);
        removeItemTime(player, 4387);
        InventoryService.gI().sendItemBags(player);
    }

    public void sendCanAutoPlay(Player player) {
        Message msg;
        try {
            msg = new Message(-116);
            msg.writer().writeByte(player.itemTime.isUseTDLT ? 1 : 0);
            player.sendMessage(msg);
        } catch (IOException e) {
            Logger.logException(ItemTimeService.class, e);
        }
    }

    public void sendTextDoanhTrai(Player player) {
        if (player.clan != null && !player.clan.haveGoneDoanhTrai
                && player.clan.lastTimeOpenDoanhTrai != 0) {
            int secondPassed = (int) ((System.currentTimeMillis() - player.clan.lastTimeOpenDoanhTrai) / 1000);
            int secondsLeft = (RedRibbonHQ.TIME_DOANH_TRAI / 1000) - secondPassed;
            if (secondsLeft < 0 || secondsLeft > 1800) {
                return;
            }
            sendTextTime(player, DOANH_TRAI, "Trại độc nhãn:", secondsLeft);
        }
    }

    public void sendTextTimePickDoanhTrai(Player player) {
        if (player.clan != null && player.clan.doanhTrai != null && player.clan.doanhTrai.isTimePicking) {
            int secondPassed = (int) ((System.currentTimeMillis() - player.clan.doanhTrai.lastTimePick) / 1000);
            int secondsLeft = (RedRibbonHQ.TIME_PICK_DOANH_TRAI / 1000) - secondPassed;
            if (secondsLeft < 0 || secondsLeft > 1800) {
                return;
            }
            sendTextTime(player, DOANH_TRAI, "Trại độc nhãn:", secondsLeft);
        }
    }

    public void sendTextBanDoKhoBau(Player player) {
        if (player.clan != null
                && player.clan.lastTimeOpenBanDoKhoBau != 0) {
            int secondPassed = (int) ((System.currentTimeMillis() - player.clan.lastTimeOpenBanDoKhoBau) / 1000);
            int secondsLeft = (BanDoKhoBau.TIME_BAN_DO_KHO_BAU / 1000) - secondPassed;
            if (secondsLeft < 0 || secondsLeft > 1800) {
                return;
            }
            sendTextTime(player, BAN_DO_KHO_BAU, "Hang kho báu:", secondsLeft);
        }
    }

    public void sendTextXinbato(Player player) {
        sendTextTime(player, BAN_DO_KHO_BAU, "Tìm nước cho Xinbatô ở đảo Kame hoặc đảo Guru", 30);
    }

    public void sendTextConDuongRanDoc(Player player) {
        if (player.clan != null
                && player.clan.lastTimeOpenConDuongRanDoc != 0) {
            int secondPassed = (int) ((System.currentTimeMillis() - player.clan.lastTimeOpenConDuongRanDoc) / 1000);
            int secondsLeft = (SnakeWay.TIME_CON_DUONG_RAN_DOC / 1000) - secondPassed;
            if (secondsLeft < 0 || secondsLeft > 1800) {
                return;
            }
            sendTextTime(player, CON_DUONG_RAN_DOC, "Con đường rắn độc:", secondsLeft);
        }
    }

    public void sendTextKhiGasHuyDiet(Player player) {
        if (player.clan != null
                && player.clan.lastTimeOpenKhiGasHuyDiet != 0) {
            int secondPassed = (int) ((System.currentTimeMillis() - player.clan.lastTimeOpenKhiGasHuyDiet) / 1000);
            int secondsLeft = (DestronGas.TIME_KHI_GAS_HUY_DIET / 1000) - secondPassed;
            if (secondsLeft < 0 || secondsLeft > 1800) {
                return;
            }
            sendTextTime(player, KHI_GAS_HUY_DIET, "Khí gas hủy diệt:", secondsLeft);
        }
    }

    public void sendTextTimeKeoBuaBao(Player player, int time) {
        sendTextTime(player, TIME_KEO_BUA_BAO, "Thời gian : ", time);
    }

    public void removeTextDoanhTrai(Player player) {
        removeTextTime(player, DOANH_TRAI);
    }

    public void removeTextBanDoKhoBau(Player player) {
        removeTextTime(player, BAN_DO_KHO_BAU);
    }

    public void removeTextConDuongRanDoc(Player player) {
        removeTextTime(player, CON_DUONG_RAN_DOC);
    }

    public void removeTextKhiGasHuyDiet(Player player) {
        removeTextTime(player, KHI_GAS_HUY_DIET);
    }

    public void removeTextTime(Player player, byte id) {
        sendTextTime(player, id, null, 0);
    }

    public void sendTextTime(Player player, byte id, String text, int seconds) {
        Message msg;
        try {
            msg = new Message(65);
            msg.writer().writeByte(id);
            msg.writer().writeUTF(text == null ? "" : text);
            msg.writer().writeShort(seconds);
            player.sendMessage(msg);
            msg.cleanup();
        } catch (IOException e) {
        }
    }

    public void sendItemTime(Player player, int itemId, int time) {
        Message msg;
        try {
            msg = new Message(-106);
            msg.writer().writeShort(itemId);
            msg.writer().writeShort(time);
            player.sendMessage(msg);
            msg.cleanup();
        } catch (IOException e) {
        }
    }

    public void removeItemTime(Player player, int itemTime) {
        sendItemTime(player, itemTime, 0);
    }

}
