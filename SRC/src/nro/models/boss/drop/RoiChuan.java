package nro.models.boss.drop;

import java.util.List;
import nro.models.boss.Boss;
import nro.models.boss.BossDropConfig;
import nro.models.boss.BossDropRate;
import nro.models.item.Item;
import nro.models.map.ItemMap;
import nro.models.player.Player;
import nro.models.server.Manager;
import nro.models.services.ItemService;
import nro.models.services.Service;
import nro.models.utils.Logger;
import nro.models.utils.Util;

/**
 * Mấy kiểu rơi đồ <b>dùng chung cho nhiều boss</b>, gom về một chỗ thay vì chép đi chép lại
 * trong từng lớp boss.
 *
 * <p>Viết ở đây chứ không nhét vào {@link BangRoiBoss} vì hai thứ này không làm được bằng bảng
 * cpanel: đồ Thần Linh tính tỉ lệ theo máu boss, còn trang bị thì phải gắn chỉ số shop và sao
 * pha lê lúc rơi.
 */
public final class RoiChuan {

    private RoiChuan() {
    }

    /** Toạ độ rơi chuẩn của một boss: ngay trên mặt đất dưới chân nó. */
    private static int yDat(Boss boss, int x) {
        return boss.zone.map.yPhysicInTop(x, boss.location.y - 24);
    }

    //================================ ngọc rơi đầy sàn ================================
    /**
     * Thả {@code tongNgoc} viên ngọc (vật phẩm 77) thành <b>nhiều đống rải quanh xác boss</b>,
     * đúng kiểu Bojack — nhìn "đầy sàn" chứ không phải một cục.
     *
     * <p>Chia thành {@value #SO_DONG_NGOC} đống, phần dư dồn vào đống cuối nên tổng LUÔN đúng
     * bằng {@code tongNgoc}, không phụ thuộc may rủi.
     */
    private static final int SO_DONG_NGOC = 7;
    private static final int ID_NGOC = 77;

    public static void roiNgocDaySan(Boss boss, Player plKill, int tongNgoc) {
        if (boss == null || plKill == null || boss.zone == null || boss.zone.map == null
                || boss.location == null || tongNgoc <= 0) {
            return;
        }
        try {
            int moiDong = Math.max(1, tongNgoc / SO_DONG_NGOC);
            int con = tongNgoc;
            for (int i = 0; i < SO_DONG_NGOC && con > 0; i++) {
                // Đống cuối ôm hết phần dư; các đống trước không bao giờ được lấy quá phần
                // còn lại — nếu không thì đặt nv_ngoc nhỏ hơn số đống sẽ rơi THỪA
                // (ví dụ nv_ngoc = 5 mà 7 đống thì thành 6 viên).
                int sl = (i == SO_DONG_NGOC - 1) ? con : Math.min(moiDong, con);
                con -= sl;
                // Rải đều hai bên xác boss rồi xê dịch ngẫu nhiên một chút cho khỏi thẳng hàng.
                int lech = (i - SO_DONG_NGOC / 2) * 18 + Util.nextInt(-8, 8);
                int x = boss.location.x + lech;
                Service.gI().dropItemMap(boss.zone,
                        new ItemMap(boss.zone, ID_NGOC, sl, x, yDat(boss, x), plKill.id));
            }
        } catch (Exception e) {
            Logger.error("Loi tha ngoc day san cho boss " + boss.name + ": " + e + "\n");
        }
    }

    //================================ kiểu Black Goku ================================
    /** Trang bị rơi từ boss lớn — nhóm áo / quần / giày. */
    private static final int[] TB_AO_QUAN_GIAY = {230, 231, 232, 234, 235, 236, 238, 239, 240,
        242, 243, 244, 246, 247, 248, 250, 251, 252, 266, 267, 268, 270, 271, 272, 274, 275, 276};
    /** Trang bị rơi từ boss lớn — nhóm găng / rađa. */
    private static final int[] TB_GANG_RADA = {254, 255, 256, 258, 259, 260, 262, 263, 264,
        278, 279, 280};
    /** Ngọc Rồng 1–7 sao + Nhẫn thời không. */
    private static final int[] NGOC_RONG = {15, 16, 17, 18, 19, 20, 992};

    /**
     * Bộ đồ rơi <b>y hệt Black Goku / Super Black Goku</b>:
     * <ul>
     *   <li>100 % vàng 20.000–30.000</li>
     *   <li>đồ Thần Linh, tỉ lệ tính theo máu boss ({@link BossDropRate}, 1–5 %)</li>
     *   <li>5 % một món trang bị — 70 % áo/quần/giày, 30 % găng/rađa — kèm chỉ số shop nhân
     *       1,00–1,15 và sao pha lê (80 % sao 1–3, 17 % sao 4–5, 3 % sao 6)</li>
     *   <li>10 % Ngọc Rồng / Nhẫn thời không, số lượng 1–3</li>
     * </ul>
     *
     * <p>Giữ nguyên từng con số của {@code Black_Goku/BlackGoku.reward} để "giống Super Black
     * Goku" là giống thật, không phải xấp xỉ.
     */
    public static void roiKieuBlackGoku(Boss boss, Player plKill) {
        if (boss == null || plKill == null || boss.zone == null || boss.zone.map == null
                || boss.location == null) {
            return;
        }
        try {
            int x = boss.location.x;
            int y = yDat(boss, x);

            // 100% vàng
            Service.gI().dropItemMap(boss.zone,
                    new ItemMap(boss.zone, 190, Util.nextInt(20000, 30000), x, y, plKill.id));

            // đồ Thần Linh — tỉ lệ theo máu boss
            if (BossDropRate.rollDoThanLinh(boss)) {
                ItemMap it = ItemService.gI().randDoTLBoss(boss.zone, 1, x, y, plKill.id);
                if (it != null) {
                    Service.gI().dropItemMap(boss.zone, it);
                }
            }

            // 5% một món trang bị kèm chỉ số + sao pha lê
            if (Util.isTrue(5, 100)) {
                int[] nhom = Util.nextInt(1, 100) <= 70 ? TB_AO_QUAN_GIAY : TB_GANG_RADA;
                int id = nhom[Util.nextInt(0, nhom.length - 1)];
                if (id >= 0 && id < Manager.ITEM_TEMPLATES.size()) {
                    ItemMap tb = new ItemMap(boss.zone, id, 1, x, y, plKill.id);
                    List<Item.ItemOption> ops = ItemService.gI().getListOptionItemShop((short) id);
                    ops.forEach(o -> o.param = (int) (o.param * Util.nextInt(100, 115) / 100.0));
                    tb.options.addAll(ops);
                    int r = Util.nextInt(1, 100);
                    int sao = r <= 80 ? Util.nextInt(1, 3) : r <= 97 ? Util.nextInt(4, 5) : 6;
                    tb.options.add(new Item.ItemOption(107, sao));
                    Service.gI().dropItemMap(boss.zone, tb);
                }
            }

            // 10% Ngọc Rồng / Nhẫn thời không
            if (Util.isTrue(10, 100)) {
                int id = NGOC_RONG[Util.nextInt(0, NGOC_RONG.length - 1)];
                if (id >= 0 && id < Manager.ITEM_TEMPLATES.size()) {
                    Service.gI().dropItemMap(boss.zone,
                            new ItemMap(boss.zone, id, Util.nextInt(1, 3), x, y, plKill.id));
                }
            }
        } catch (Exception e) {
            Logger.error("Loi tha do kieu Black Goku cho boss " + boss.name + ": " + e + "\n");
        }
    }

    /** Số ngọc mà mọi boss nhiệm vụ rơi thêm, chỉnh được ở cpanel. */
    public static int ngocBossNhiemVu() {
        return Math.max(0, BossDropConfig.NV_NGOC.giaTri);
    }
}
