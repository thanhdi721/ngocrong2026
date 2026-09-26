package nro.models.item;

import nro.models.player.NPoint;
import nro.models.player.Player;
import nro.models.services.Service;
import nro.models.utils.Util;
import nro.models.services.ItemTimeService;

public class ItemTime {

    public static final byte DOANH_TRAI = 0;
    public static final byte BAN_DO_KHO_BAU = 1;
    public static final byte CON_DUONG_RAN_DOC = 2;
    public static final byte KHI_GAS_HUY_DIET = 3;
    public static final byte TIME_KEO_BUA_BAO = 4;
    public static final byte TEXT_NHAN_BUA_MIEN_PHI = 5;

    public static final int TIME_ITEM = 600000;
    public static final int TIME_OPEN_POWER = 8640000;
    public static final int TIME_MAY_DO = 1800000;
    public static final int TIME_MAY_DO2 = 1800000;
    public static final long TIME_CO_BON_LA = 30 * 60 * 1000L;
    public static final int TIME_KILIS = 3600000;
    public static final int TIME_NUOC_MIA1 = 600_000;
    public static final int TIME_NUOC_MIA2 = 600_000;
    public static final int TIME_NUOC_MIA3 = 600_000;

    public static final int TIME_BUA_SANTA = 1800000;
    public static final int TIME_EAT_MEAL = 600000;
    public static final int TIME_CMS = 3600000;
    public static final int TIME_DK = 1800000;
    public static final int TIME_NCD = 1800000;

    private Player player;

    public boolean isUseBoHuyet;
    public boolean isUseBoKhi;
    public boolean isUseGiapXen;
    public boolean isUseCuongNo;
    public boolean isUseAnDanh;
    public boolean isUseBoHuyet2;
    public boolean isUseBoKhi2;
    public boolean isUseGiapXen2;
    public boolean isUseCuongNo2;
    public boolean isUseAnDanh2;

    public long lastTimeBoHuyet;
    public long lastTimeBoKhi;
    public long lastTimeGiapXen;
    public long lastTimeCuongNo;
    public long lastTimeAnDanh;

    public long lastTimeBoHuyet2;
    public long lastTimeBoKhi2;
    public long lastTimeGiapXen2;
    public long lastTimeCuongNo2;
    public long lastTimeAnDanh2;

    //================= Map Tu Tiên (patch 83) + luyện đan (patch 84) =================
    /**
     * 5 loại đan. Mỗi loại có BA ô: đang bật / mốc giờ bật / thời hạn / mức cộng.
     *
     * <p>Thời hạn và mức cộng để riêng vì từ patch 84 có hai hạng đan dùng chung một ô:
     * đan mua ở tiệm ({@link #TIME_ITEM} = 10 phút, mức gốc) và <b>đan thượng phẩm</b> luyện
     * ở Lò ({@link nro.models.tu_tien.LuyenDan#THOI_GIAN} = 20 phút, mức cao hơn). Ăn viên nào
     * thì viên đó ghi đè cả ba ô, nên không bao giờ cộng dồn hai hạng.
     */
    public boolean isUseDanSucDanh;
    public long lastTimeDanSucDanh;
    public long hanDanSucDanh = TIME_ITEM;
    public int mucDanSucDanh = MUC_GOC_SUC_DANH;
    public boolean isUseDanHp;
    public long lastTimeDanHp;
    public long hanDanHp = TIME_ITEM;
    public int mucDanHp = MUC_GOC_HP;
    public boolean isUseDanKi;
    public long lastTimeDanKi;
    public long hanDanKi = TIME_ITEM;
    public int mucDanKi = MUC_GOC_KI;
    public boolean isUseDanGiap;
    public long lastTimeDanGiap;
    public long hanDanGiap = TIME_ITEM;
    public int mucDanGiap = MUC_GOC_GIAP;
    public boolean isUseDanChiMang;
    public long lastTimeDanChiMang;
    public long hanDanChiMang = TIME_ITEM;
    public int mucDanChiMang = MUC_GOC_CHI_MANG;

    /** Mức cộng của đan mua ở tiệm Tu Tiên (%). Đan thượng phẩm xem LuyenDan.MUC_*. */
    public static final int MUC_GOC_SUC_DANH = 20;
    public static final int MUC_GOC_HP = 30;
    public static final int MUC_GOC_KI = 30;
    public static final int MUC_GOC_GIAP = 50;
    public static final int MUC_GOC_CHI_MANG = 10;
    /** Tụ Linh Phù — tăng tỉ lệ rơi Linh Thạch, 30 phút. */
    public boolean isUseTuLinhPhu;
    public long lastTimeTuLinhPhu;

    public boolean isUseMayDo;
    public long lastTimeUseMayDo;
    public boolean isUseKhoBauX2;
    public long lastTimeUseKhoBauX2;
    public boolean isUseBuaSanta;
    public long lastTimeBuaSanta;

    public long lastTimeUseCoBonLa;
    public boolean isUseCoBonLa;

    public boolean isUseKilis;
    public long lastTimeUseKilis;

    public boolean isUseNuocMia1;
    public long lastTimeUseNuocMia1;
    public boolean isUseNuocMia2;
    public long lastTimeUseNuocMia2;
    public boolean isUseNuocMia3;
    public long lastTimeUseNuocMia3;

    public boolean isOpenPower;
    public long lastTimeOpenPower;

    public boolean isUseTDLT;
    public long lastTimeUseTDLT;
    public int timeTDLT;

    public boolean isUseRX;
    public long lastTimeUseRX;
    public int timeRX;

    public boolean isUseCMS;
    public long lastTimeUseCMS;

    public boolean isUseNCD;
    public long lastTimeUseNCD;

    public boolean isUseGTPT;
    public long lastTimeUseGTPT;

    public boolean isUseDK;
    public long lastTimeUseDK;

    public boolean isEatMeal;
    public long lastTimeEatMeal;
    public int iconMeal;

    public boolean isEatMeal2;
    public long lastTimeEatMeal2;
    public int iconMeal2;
    public long lastTimeKhauTrang;
    public boolean isUseKhauTrang;
    public long timeLengthKilis;
    public long totalCoBonLaTime;
    public long timeLengthCoBonLa;

    public ItemTime(Player player) {
        this.player = player;
    }

    public void update() {
        if (isEatMeal) {
            if (Util.canDoWithTime(lastTimeEatMeal, TIME_EAT_MEAL)) {
                isEatMeal = false;
                Service.gI().point(player);
            }
        }
        if (isEatMeal2) {
            if (Util.canDoWithTime(lastTimeEatMeal2, TIME_EAT_MEAL)) {
                isEatMeal2 = false;
                Service.gI().point(player);
            }
        }
        if (isUseBoHuyet) {
            if (Util.canDoWithTime(lastTimeBoHuyet, TIME_ITEM)) {
                isUseBoHuyet = false;
                Service.gI().point(player);
            }
        }

        if (isUseBoKhi) {
            if (Util.canDoWithTime(lastTimeBoKhi, TIME_ITEM)) {
                isUseBoKhi = false;
                Service.gI().point(player);
            }
        }

        if (isUseGiapXen) {
            if (Util.canDoWithTime(lastTimeGiapXen, TIME_ITEM)) {
                isUseGiapXen = false;
            }
        }
        if (isUseCuongNo) {
            if (Util.canDoWithTime(lastTimeCuongNo, TIME_ITEM)) {
                isUseCuongNo = false;
                Service.gI().point(player);
            }
        }
        if (isUseAnDanh) {
            if (Util.canDoWithTime(lastTimeAnDanh, TIME_ITEM)) {
                isUseAnDanh = false;
            }
        }

        if (isUseBoHuyet2) {
            if (Util.canDoWithTime(lastTimeBoHuyet2, TIME_ITEM)) {
                isUseBoHuyet2 = false;
                Service.gI().point(player);
            }
        }

        if (isUseBoKhi2) {
            if (Util.canDoWithTime(lastTimeBoKhi2, TIME_ITEM)) {
                isUseBoKhi2 = false;
                Service.gI().point(player);
            }
        }
        if (isUseGiapXen2) {
            if (Util.canDoWithTime(lastTimeGiapXen2, TIME_ITEM)) {
                isUseGiapXen2 = false;
            }
        }
        if (isUseCuongNo2) {
            if (Util.canDoWithTime(lastTimeCuongNo2, TIME_ITEM)) {
                isUseCuongNo2 = false;
                Service.gI().point(player);
            }
        }
        if (isUseAnDanh2) {
            if (Util.canDoWithTime(lastTimeAnDanh2, TIME_ITEM)) {
                isUseAnDanh2 = false;
            }
        }
        if (isUseCMS) {
            if (Util.canDoWithTime(lastTimeUseCMS, TIME_CMS)) {
                isUseCMS = false;
            }
        }
        // ===== đan và bùa của map Tu Tiên =====
        // Hết hạn thì trả cả mức cộng về mức gốc, để viên tiệm ăn sau đó không thừa hưởng
        // nhầm mức của viên thượng phẩm vừa hết.
        if (isUseDanSucDanh && Util.canDoWithTime(lastTimeDanSucDanh, hanDanSucDanh)) {
            isUseDanSucDanh = false;
            hanDanSucDanh = TIME_ITEM;
            mucDanSucDanh = MUC_GOC_SUC_DANH;
            Service.gI().point(player);
        }
        if (isUseDanHp && Util.canDoWithTime(lastTimeDanHp, hanDanHp)) {
            isUseDanHp = false;
            hanDanHp = TIME_ITEM;
            mucDanHp = MUC_GOC_HP;
            Service.gI().point(player);
        }
        if (isUseDanKi && Util.canDoWithTime(lastTimeDanKi, hanDanKi)) {
            isUseDanKi = false;
            hanDanKi = TIME_ITEM;
            mucDanKi = MUC_GOC_KI;
            Service.gI().point(player);
        }
        if (isUseDanGiap && Util.canDoWithTime(lastTimeDanGiap, hanDanGiap)) {
            isUseDanGiap = false;
            hanDanGiap = TIME_ITEM;
            mucDanGiap = MUC_GOC_GIAP;
        }
        if (isUseDanChiMang && Util.canDoWithTime(lastTimeDanChiMang, hanDanChiMang)) {
            isUseDanChiMang = false;
            hanDanChiMang = TIME_ITEM;
            mucDanChiMang = MUC_GOC_CHI_MANG;
            Service.gI().point(player);
        }
        if (isUseTuLinhPhu
                && Util.canDoWithTime(lastTimeTuLinhPhu, nro.models.tu_tien.TuTien.THOI_GIAN_BUA)) {
            isUseTuLinhPhu = false;
        }
        if (isUseGTPT) {
            if (Util.canDoWithTime(lastTimeUseGTPT, TIME_ITEM)) {
                isUseGTPT = false;
            }
        }
        if (isUseDK) {
            if (Util.canDoWithTime(lastTimeUseDK, TIME_DK)) {
                isUseDK = false;
            }
        }
        if (isOpenPower) {
            if (Util.canDoWithTime(lastTimeOpenPower, TIME_OPEN_POWER)) {
                player.nPoint.limitPower++;
                if (player.nPoint.limitPower > NPoint.MAX_LIMIT) {
                    player.nPoint.limitPower = NPoint.MAX_LIMIT;
                }
                Service.gI().sendThongBao(player, "Giới hạn sức mạnh của bạn đã được tăng lên 1 bậc");
                isOpenPower = false;
            }
        }
        if (isUseMayDo) {
            if (Util.canDoWithTime(lastTimeUseMayDo, TIME_MAY_DO)) {
                isUseMayDo = false;
            }
        }
        if (isUseCoBonLa) {
            long now = System.currentTimeMillis();
            long timeLeft = (lastTimeUseCoBonLa + timeLengthCoBonLa) - now;
            if (timeLeft <= 0) {
                isUseCoBonLa = false;
                timeLengthCoBonLa = 0;
            }
        }
        if (isUseKilis) {
            if (Util.canDoWithTime(lastTimeUseKilis, TIME_KILIS)) {
                isUseKilis = false;
            }
        }
        if (isUseNuocMia1) {
            if (Util.canDoWithTime(lastTimeUseNuocMia1, TIME_NUOC_MIA1)) {
                isUseNuocMia1 = false;
                Service.gI().point(player);
            }
        }
        if (isUseNuocMia2) {
            if (Util.canDoWithTime(lastTimeUseNuocMia1, TIME_NUOC_MIA2)) {
                isUseNuocMia2 = false;
                Service.gI().point(player);
            }
        }
        if (isUseNuocMia3) {
            if (Util.canDoWithTime(lastTimeUseNuocMia1, TIME_NUOC_MIA3)) {
                isUseNuocMia3 = false;
                Service.gI().point(player);
            }
        }
        if (isUseBuaSanta) {
            if (Util.canDoWithTime(lastTimeBuaSanta, TIME_BUA_SANTA)) {
                isUseBuaSanta = false;
            }
        }
        if (isUseKhoBauX2) {
            if (Util.canDoWithTime(lastTimeUseKhoBauX2, TIME_MAY_DO2)) {
                isUseKhoBauX2 = false;
            }
        }
        if (isUseTDLT) {
            if (Util.canDoWithTime(lastTimeUseTDLT, timeTDLT)) {
                this.isUseTDLT = false;
                ItemTimeService.gI().sendCanAutoPlay(this.player);
            }
        }
        if (isUseRX) {
            if (Util.canDoWithTime(lastTimeUseRX, timeRX)) {
                isUseRX = false;
            }
        }
    }

    public void dispose() {
        this.player = null;
    }
}
