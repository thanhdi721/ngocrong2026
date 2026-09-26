package nro.models.tu_tien;

import nro.models.boss.BossDropConfig;
import nro.models.item.Item;
import nro.models.map.ItemMap;
import nro.models.map.Zone;
import nro.models.map.service.ChangeMapService;
import nro.models.mob.Mob;
import nro.models.player.Player;
import nro.models.server.Manager;
import nro.models.services.Service;
import nro.models.utils.Logger;
import nro.models.utils.Util;

/**
 * Map Tu Tiên — <b>Nam Thiên Môn</b> (map {@value #MAP_ID}).
 *
 * <p>Luật riêng của map này, gom hết về một chỗ cho dễ sửa:
 * <ul>
 *   <li>Quái 20 triệu máu, sát thương {@link BossDropConfig#TT_SAT_THUONG_QUAI}, mỗi đòn của
 *       người chơi bị chặn ở {@link BossDropConfig#TT_TRAN_SAT_THUONG} ⇒ mạnh mấy cũng phải
 *       2 đòn, yếu hơn thì nhiều đòn hơn.</li>
 *   <li>Đánh quái <b>không nhận kinh nghiệm, không nhận vàng</b>.</li>
 *   <li>Quái chỉ rơi <b>Linh Thạch</b>, tỉ lệ {@link BossDropConfig#TT_TI_LE_LINH_THACH}.</li>
 *   <li>Quái hồi sinh chậm ({@link BossDropConfig#TT_HOI_SINH} giây thay vì 3 giây) để đặt
 *       trần tốc độ cày — xem docs/4-trien-khai/69.</li>
 *   <li>Vào map là <b>tự bật cờ đen</b>; chết thì <b>không hồi sinh tại chỗ</b>, tự đưa về nhà.</li>
 * </ul>
 */
public final class TuTien {

    /** Map "Nam Thiên Môn" — patch 83. */
    public static final int MAP_ID = 208;

    //========================= vật phẩm (patch 83) =========================
    public static final int LINH_THACH = 2266;
    public static final int DAN_SUC_DANH = 2267;
    public static final int DAN_HP = 2268;
    public static final int DAN_KI = 2269;
    public static final int DAN_GIAP = 2270;
    public static final int DAN_CHI_MANG = 2271;
    public static final int NGOC_BOI_HP = 2272;
    public static final int NGOC_BOI_KI = 2273;
    public static final int NGOC_BOI_SUC_DANH = 2274;
    public static final int TU_LINH_PHU = 2275;

    /** Ba viên ngọc bội — chỉ đệ tử đeo được. */
    public static final int[] NGOC_BOI = {NGOC_BOI_HP, NGOC_BOI_KI, NGOC_BOI_SUC_DANH};

    /** Giá mua ở NPC Tu Tiên. */
    public static final int GIA_DAN = 50;
    public static final int GIA_NGOC_BOI = 200;
    /** Bùa Tụ Linh bán bằng thỏi vàng (item 457), không bằng linh thạch. */
    public static final int ID_THOI_VANG = 457;
    public static final int GIA_BUA_THOI_VANG = 5;

    /**
     * Đan dùng thẳng nhịp 10 phút sẵn có của khung bùa ({@code ItemTime.TIME_ITEM}) — trỏ vào
     * đúng hằng số đó để sau này ai sửa nhịp chung thì đan đi theo, không lệch.
     */
    public static final int THOI_GIAN_DAN = nro.models.item.ItemTime.TIME_ITEM;
    /** Tụ Linh Phù: 30 phút. */
    public static final int THOI_GIAN_BUA = 30 * 60 * 1000;

    /** Chờ bấy nhiêu mili giây sau khi chết rồi mới đá về nhà. */
    private static final long CHO_TRUOC_KHI_VE = 3000;

    private TuTien() {
    }

    /** Có phải một trong ba viên ngọc bội không (chỉ đệ tử đeo được). */
    public static boolean laNgocBoi(int idVatPham) {
        for (int id : NGOC_BOI) {
            if (id == idVatPham) {
                return true;
            }
        }
        return false;
    }

    //========================= nhận biết map =========================
    public static boolean laMapTuTien(int mapId) {
        return mapId == MAP_ID;
    }

    public static boolean dangOMapTuTien(Player pl) {
        return pl != null && pl.zone != null && pl.zone.map != null
                && laMapTuTien(pl.zone.map.mapId);
    }

    public static boolean laQuaiTuTien(Mob mob) {
        return mob != null && mob.zone != null && mob.zone.map != null
                && laMapTuTien(mob.zone.map.mapId);
    }

    //========================= quái =========================
    /**
     * Đặt sát thương cho toàn bộ quái trong map, gọi một lần lúc server dựng xong bản đồ.
     *
     * <p>Cần làm vì {@code map_template.mobs} không có cột sát thương: để trống thì
     * {@code MobPoint.getDameAttack()} lấy <b>5% máu tối đa</b> — với 20 triệu máu thành
     * 1 triệu sát thương một đòn, một phát là chết người chơi.
     */
    public static void chuanBiQuai() {
        try {
            // Duyệt thẳng Manager.MAPS chứ không qua MapService: hàm này chạy ngay trong
            // hàm dựng của Manager, gọi service khác lúc đó dễ vòng khởi tạo.
            nro.models.map.Map map = null;
            for (nro.models.map.Map m : Manager.MAPS) {
                if (m != null && m.mapId == MAP_ID) {
                    map = m;
                    break;
                }
            }
            if (map == null) {
                Logger.error("Map Tu Tien " + MAP_ID + " chua co trong CSDL, chua chay patch 83?\n");
                return;
            }
            int dame = Math.max(1, BossDropConfig.TT_SAT_THUONG_QUAI.giaTri);
            int dem = 0;
            for (Zone z : map.zones) {
                for (Mob mob : z.mobs) {
                    mob.point.dame = dame;
                    dem++;
                }
            }
            Logger.success("Map Tu Tien: dat sat thuong " + dame + " cho " + dem + " con quai\n");
        } catch (Exception e) {
            Logger.error("Khong chuan bi duoc quai map Tu Tien: " + e + "\n");
        }
    }

    /** Trần sát thương mỗi đòn lên quái trong map này. */
    public static long chanSatThuong(long damage) {
        long tran = Math.max(1, BossDropConfig.TT_TRAN_SAT_THUONG.giaTri);
        return Math.min(damage, tran);
    }

    /** Quái map này hồi sinh chậm hơn map thường, tính bằng mili giây. */
    public static long nhipHoiSinh() {
        return Math.max(1, BossDropConfig.TT_HOI_SINH.giaTri) * 1000L;
    }

    /**
     * Danh sách đồ rơi của quái map Tu Tiên: <b>chỉ Linh Thạch</b>, không vàng, không đồ thường.
     *
     * <p>Gọi ngay đầu {@code Mob.getItemMobReward} và trả về luôn — chặn ở đây là chặn sạch mọi
     * thứ khác (vàng, ngọc, đồ sự kiện…), khỏi phải đi vá từng nhánh.
     *
     * <p>Bùa Tụ Linh cộng <b>tương đối</b>: tỉ lệ gốc 15% + bùa 50% = 22,5% (làm tròn lên).
     */
    public static java.util.List<ItemMap> roiLinhThach(Mob mob, Player plKill, int x, int y) {
        java.util.List<ItemMap> ds = new java.util.ArrayList<>();
        if (plKill == null || mob == null || mob.zone == null || mob.zone.map == null) {
            return ds;
        }
        if (LINH_THACH < 0 || LINH_THACH >= Manager.ITEM_TEMPLATES.size()) {
            return ds;     // chưa chạy patch 83 — thà không rơi còn hơn văng lỗi
        }
        int tiLe = Math.max(0, Math.min(100, BossDropConfig.TT_TI_LE_LINH_THACH.giaTri));
        if (dangDungBua(plKill)) {
            tiLe = Math.min(100, (int) Math.ceil(tiLe
                    * (100.0 + Math.max(0, BossDropConfig.TT_BUA_CONG_THEM.giaTri)) / 100.0));
        }
        if (tiLe <= 0 || !Util.isTrue(tiLe, 100)) {
            return ds;
        }
        try {
            ItemMap it = new ItemMap(mob.zone, LINH_THACH, 1, x, y, plKill.id);
            it.options.add(new Item.ItemOption(30, 0));   // khóa giao dịch ngay từ lúc rơi
            ds.add(it);
        } catch (Exception e) {
            Logger.error("Loi tha Linh Thach: " + e + "\n");
        }
        return ds;
    }

    private static boolean dangDungBua(Player pl) {
        return pl.itemTime != null && pl.itemTime.isUseTuLinhPhu
                && !Util.canDoWithTime(pl.itemTime.lastTimeTuLinhPhu, THOI_GIAN_BUA);
    }

    //========================= luật map =========================
    /**
     * Người chơi vừa vào map: bật cờ đen. Gọi ở cuối {@code ChangeMapService.changeMap}.
     */
    public static void vaoMap(Player pl) {
        if (pl == null || !pl.isPl() || !dangOMapTuTien(pl)) {
            return;     // đệ tử / bot đi theo chủ thì bỏ qua
        }
        try {
            if (pl.typePk != nro.models.consts.ConstPlayer.PK_ALL) {
                nro.models.services.PlayerService.gI()
                        .changeAndSendTypePK(pl, nro.models.consts.ConstPlayer.PK_ALL);
            }
            Service.gI().sendThongBao(pl,
                    "Nam Thiên Môn: cờ đen tự bật, chết là bị đá về nhà, không hồi sinh tại chỗ.");
        } catch (Exception e) {
        }
    }

    /**
     * Chết trong map tu tiên thì không hồi sinh tại chỗ — sau {@value #CHO_TRUOC_KHI_VE} mili
     * giây tự đá về nhà và hồi đầy máu.
     */
    public static void kiemTraChet(Player pl) {
        if (pl == null || !pl.isPl()) {
            return;
        }
        if (!pl.isDie() || !dangOMapTuTien(pl)) {
            pl.lucChetTuTien = 0;       // sống lại hoặc đã ra khỏi map -> quên mốc giờ
            return;
        }
        if (pl.lucChetTuTien == 0) {
            pl.lucChetTuTien = System.currentTimeMillis();   // vừa thấy chết, bắt đầu đếm
            return;
        }
        if (!Util.canDoWithTime(pl.lucChetTuTien, CHO_TRUOC_KHI_VE)) {
            return;                     // chưa đủ 3 giây
        }
        pl.lucChetTuTien = 0;
        try {
            Service.gI().sendThongBao(pl, "Ngươi chưa đủ đạo hạnh. Về luyện lại đi.");
            ChangeMapService.gI().changeMapBySpaceShip(pl, pl.gender + 21, -1, 100);
            Service.gI().hsChar(pl, pl.nPoint.hpMax, pl.nPoint.mpMax);
            Service.gI().point(pl);
            Service.gI().Send_Info_NV(pl);
        } catch (Exception e) {
            Logger.error("Khong dua duoc nguoi choi ve nha tu map Tu Tien: " + e + "\n");
        }
    }
}
