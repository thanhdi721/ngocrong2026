package nro.models.boss.sieu_than_god;

import nro.models.boss.Boss;
import nro.models.map.ItemMap;
import nro.models.player.Player;
import nro.models.server.Manager;
import nro.models.services.Service;
import nro.models.utils.Logger;
import nro.models.utils.Util;

/**
 * Bảng rơi đồ dùng chung cho bộ ba boss cùng bậc: <b>Vegeta Siêu Thần God</b>,
 * <b>Goku Siêu Thần God</b> và <b>Lão Dê Hồi Xuân</b> (con triệu hồi bằng Còi).
 *
 * <p>Để chung một chỗ để ba con <b>chắc chắn</b> cùng một tỉ lệ — sửa ở đây là sửa cả ba,
 * không còn cảnh chép qua chép lại rồi lệch nhau.
 *
 * <p>Mỗi lần hạ boss quay <b>hai lượt độc lập</b>:
 * <ol>
 *   <li>Lượt chính, tổng đúng 100 %: 40 % Ngọc Rồng 3–5 sao · 40 % Ngọc Rồng bí ngô 1–7 sao ·
 *       10 % một lá bùa cấp 2 · 5 % Gậy Thông Thiên · 5 % 5 viên Đá Pháp Sư.</li>
 *   <li>Lượt phụ {@value #TI_LE_COI} %: thêm một cái Còi Triệu Hồi Lão Dê. Lượt này
 *       <b>không đụng</b> tới bảng 100 % ở trên.</li>
 * </ol>
 */
public final class BangRoi {

    /** Ngọc Rồng 3 / 4 / 5 sao. */
    private static final int[] NGOC_RONG = {16, 17, 18};
    /** Ngọc Rồng bí ngô 1 … 7 sao. */
    private static final int[] BI_NGO = {702, 703, 704, 705, 706, 707, 708};
    /** Cuồng nộ 2, Bổ khí 2, Bổ huyết 2, Giáp Xên bọ hung 2. */
    private static final int[] BUA = {1150, 1151, 1152, 1153};

    private static final int ID_GAY_THONG_THIEN = 2264;
    private static final int ID_DA_PHAP_SU = 2262;
    private static final int SO_DA_PHAP_SU = 5;
    public static final int ID_COI_LAO_DE = 2265;

    /** Tỉ lệ rơi thêm Còi Triệu Hồi Lão Dê, quay riêng ngoài bảng 100 %. */
    private static final int TI_LE_COI = 10;

    private BangRoi() {
    }

    /** Rơi đồ cho {@code plKill}. Gọi trong {@code reward()} của boss. */
    public static void roi(Boss boss, Player plKill) {
        if (boss == null || plKill == null || boss.zone == null || boss.zone.map == null) {
            return;     // boss vừa rời map ngay lúc chết — không có chỗ để rơi đồ
        }
        int x = boss.location.x;
        int y = boss.zone.map.yPhysicInTop(x, boss.location.y - 24);

        int quay = Util.nextInt(0, 99);
        int idRoi;
        int soLuong = 1;
        if (quay < 40) {
            idRoi = NGOC_RONG[Util.nextInt(0, NGOC_RONG.length - 1)];
        } else if (quay < 80) {
            idRoi = BI_NGO[Util.nextInt(0, BI_NGO.length - 1)];
        } else if (quay < 90) {
            idRoi = BUA[Util.nextInt(0, BUA.length - 1)];
        } else if (quay < 95) {
            idRoi = ID_GAY_THONG_THIEN;
        } else {
            idRoi = ID_DA_PHAP_SU;
            soLuong = SO_DA_PHAP_SU;
        }
        tha(boss, plKill, idRoi, soLuong, x, y);

        if (Util.isTrue(TI_LE_COI, 100)) {
            tha(boss, plKill, ID_COI_LAO_DE, 1, x + 20, y);
        }
    }

    /** Thả một món ra đất, gán cho người hạ boss. */
    private static void tha(Boss boss, Player plKill, int idRoi, int soLuong, int x, int y) {
        if (idRoi >= Manager.ITEM_TEMPLATES.size()) {
            // Chưa chạy patch: ITEM_TEMPLATES tra theo chỉ số, lấy id vượt cỡ là văng
            // IndexOutOfBounds ngay trong luồng boss. Thà không rơi gì.
            Logger.error("Boss " + boss.name + " khong roi duoc vat pham " + idRoi
                    + ": chua co trong item_template\n");
            return;
        }
        Service.gI().dropItemMap(boss.zone, new ItemMap(boss.zone, idRoi, soLuong, x, y, plKill.id));
    }
}
