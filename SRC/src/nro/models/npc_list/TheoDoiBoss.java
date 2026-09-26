package nro.models.npc_list;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import nro.models.boss.Boss;
import nro.models.boss.Boss_Manager.BossManager;
import nro.models.boss.drop.BangRoiBoss;
import nro.models.boss.drop.MucRoi;
import nro.models.consts.BossStatus;
import nro.models.consts.ConstNpc;
import nro.models.item.Item;
import nro.models.map.Zone;
import nro.models.npc.Npc;
import nro.models.npc.NpcFactory;
import nro.models.player.Player;
import nro.models.server.Manager;
import nro.models.services.ItemService;
import nro.models.services.Service;
import nro.models.shop.ShopService;
import nro.models.utils.Util;

/**
 * NPC "Theo Dõi Boss" đứng bên trái Santa ở đảo Kamê (map 5), ngoại hình lấy từ cải trang
 * "Siêu Goku Vô Cực" (item 2150, part 2448 / 2449 / 2450).
 *
 * <p>Cho người chơi xem <b>toàn bộ boss trong server</b>: con nào đang ra map (và ở map nào),
 * con nào đang nghỉ, và <b>bảng rơi đồ kèm tỉ lệ</b> đọc thẳng từ {@link BangRoiBoss} — tức là
 * đúng bảng mà quản trị sửa ở cpanel, không phải bảng chép tay.
 *
 * <p>Chỉ hiện <b>tên map</b>, không hiện khu, để không thành công cụ canh boss quá dễ.
 */
public class TheoDoiBoss extends Npc {

    /** Số boss xếp trong một trang menu. */
    private static final int MOI_TRANG = 8;
    /** Số dòng tối đa khi in danh sách boss đang ra map. */
    private static final int TOI_DA_DONG = 14;

    private static final String[] CHAO = {
        "Ta thấy hết. Ngươi muốn biết con nào?",
        "Đừng hỏi ta ở khu nào, ta không nói đâu.",
        "Sổ theo dõi đây. Xem nhanh rồi đi đánh.",
        "Boss nào cũng có giờ của nó cả."
    };

    public TheoDoiBoss(int mapId, int status, int cx, int cy, int tempId, int avartar) {
        super(mapId, status, cx, cy, tempId, avartar);
    }

    //================================ gom dữ liệu ================================
    /** Một dòng gộp theo TÊN boss (nhiều bản cùng tên thì gộp làm một). */
    private static final class Dong {

        String ten;
        int bossId;
        int tong;
        int dangRa;
        long mauMax;
        /** Tên các map đang có con này, không kèm khu. */
        final List<String> map = new ArrayList<>();
    }

    /** Gom toàn bộ boss đang có trong bộ nhớ, gộp theo tên, xếp con đang ra map lên trước. */
    private static List<Dong> gom() {
        Map<String, Dong> gop = new LinkedHashMap<>();
        List<Boss> ds;
        try {
            ds = new ArrayList<>(BossManager.gI().getBosses());
        } catch (Exception e) {
            return new ArrayList<>();
        }
        for (Boss b : ds) {
            if (b == null) {
                continue;
            }
            try {
                String ten = b.name == null || b.name.isEmpty() ? "?" : b.name;
                Dong d = gop.get(ten);
                if (d == null) {
                    d = new Dong();
                    d.ten = ten;
                    d.bossId = (int) b.id;
                    gop.put(ten, d);
                }
                d.tong++;
                if (b.nPoint != null && b.nPoint.hpMax > d.mauMax) {
                    d.mauMax = b.nPoint.hpMax;
                }
                Zone z = b.zone;
                boolean raMap = z != null && z.map != null
                        && b.bossStatus != BossStatus.REST && b.bossStatus != BossStatus.DIE;
                if (raMap) {
                    d.dangRa++;
                    String tenMap = z.map.mapName == null ? ("map " + z.map.mapId) : z.map.mapName;
                    if (!d.map.contains(tenMap)) {
                        d.map.add(tenMap);
                    }
                }
            } catch (Exception e) {
            }
        }
        List<Dong> ra = new ArrayList<>(gop.values());
        ra.sort(Comparator.comparing((Dong d) -> d.dangRa == 0).thenComparing(d -> d.ten));
        return ra;
    }

    private static int soTrang(int soDong) {
        return Math.max(1, (soDong + MOI_TRANG - 1) / MOI_TRANG);
    }

    //================================ menu ================================
    @Override
    public void openBaseMenu(Player player) {
        if (!canOpenNpc(player)) {
            return;
        }
        List<Dong> ds = gom();
        int dangRa = 0;
        for (Dong d : ds) {
            dangRa += d.dangRa;
        }
        createOtherMenu(player, ConstNpc.THEO_DOI_BOSS,
                CHAO[Util.nextInt(0, CHAO.length - 1)] + "\n"
                + "Đang ra map: " + dangRa + " con.\n"
                + "Tổng cộng: " + ds.size() + " loại boss.",
                "Boss đang\nra map", "Danh sách\ntất cả", "Đóng");
    }

    @Override
    public void confirmMenu(Player player, int select) {
        if (!canOpenNpc(player)) {
            return;
        }
        int menu = player.idMark.getIndexMenu();
        if (menu == ConstNpc.THEO_DOI_BOSS) {
            switch (select) {
                case 0 ->
                    moTrang(player, 0, true);
                case 1 ->
                    moTrang(player, 0, false);
                default -> {
                }
            }
            return;
        }
        boolean chiRa = menu >= ConstNpc.THEO_DOI_BOSS_TRANG_RA;
        int trang = menu - (chiRa ? ConstNpc.THEO_DOI_BOSS_TRANG_RA : ConstNpc.THEO_DOI_BOSS_TRANG);
        if (trang < 0 || trang > 19) {
            return;
        }
        // Tên các boss của đúng trang vừa mở được gửi kèm lúc mở menu, khỏi phải gom lại
        // rồi lệch danh sách nếu có boss vừa được thêm/bớt.
        Object o = NpcFactory.PLAYERID_OBJECT.get(player.id);
        String[] ten = o instanceof String[] ? (String[]) o : new String[0];
        if (select < ten.length) {
            xemChiTiet(player, ten[select]);
        } else if (select == ten.length) {
            moTrang(player, trang + 1, chiRa);
        }
    }

    /**
     * Menu chọn boss của trang {@code trang}.
     *
     * <p>Danh sách boss dựng bằng <b>menu nhiều trang</b> chứ không in một khối chữ: server có
     * hơn trăm bản boss, in hết ra là tràn khung và phải cắt bớt ("… và 5 loại nữa"), người chơi
     * không bấm vào con nào được.
     *
     * @param chiRa true thì chỉ liệt kê con đang đứng ngoài map (kèm tên map trên nút)
     */
    private void moTrang(Player player, int trang, boolean chiRa) {
        List<Dong> tatCa = gom();
        List<Dong> ds = new ArrayList<>();
        for (Dong d : tatCa) {
            if (!chiRa || d.dangRa > 0) {
                ds.add(d);
            }
        }
        if (ds.isEmpty()) {
            createOtherMenu(player, ConstNpc.IGNORE_MENU,
                    "Không có con nào ngoài map.\nĐợi giờ đi.", "Đóng");
            return;
        }
        int tong = soTrang(ds.size());
        trang = ((trang % tong) + tong) % tong;
        if (trang > 19) {
            trang = 0;      // dải menu chỉ có 20 ô, quá thì quay về đầu
        }
        int dau = trang * MOI_TRANG;
        int cuoi = Math.min(dau + MOI_TRANG, ds.size());

        List<String> ten = new ArrayList<>();
        List<String> muc = new ArrayList<>();
        for (int i = dau; i < cuoi; i++) {
            Dong d = ds.get(i);
            ten.add(d.ten);
            if (chiRa) {
                String map = d.map.isEmpty() ? "?" : d.map.get(0);
                muc.add(d.ten + (d.dangRa > 1 ? " x" + d.dangRa : "") + "\n" + map);
            } else {
                muc.add((d.dangRa > 0 ? "● " : "○ ") + d.ten);
            }
        }
        if (tong > 1) {
            muc.add("Xem tiếp\n(trang " + (trang + 2 > tong ? 1 : trang + 2) + "/" + tong + ")");
        }
        muc.add("Đóng");
        createOtherMenu(player,
                (chiRa ? ConstNpc.THEO_DOI_BOSS_TRANG_RA : ConstNpc.THEO_DOI_BOSS_TRANG) + trang,
                (chiRa ? "BOSS ĐANG RA MAP" : "TẤT CẢ BOSS") + "\nTrang " + (trang + 1) + "/" + tong
                + (chiRa ? "" : "\n● đang ra map   ○ đang nghỉ"),
                muc.toArray(new String[0]), ten.toArray(new String[0]));
    }

    /**
     * Chi tiết một boss. Bảng rơi hiện bằng <b>giao diện tiệm</b> (icon + tên vật phẩm + dòng
     * chữ màu), không phải chữ chay: {@link ShopService#moBangXem} dựng khung "chỉ để xem",
     * bấm vào dòng nào cũng không nhận được gì.
     *
     * <p>Tên boss và map nằm ở <b>chữ trên nút tab</b> — trong gói tin tiệm, tiêu đề mỗi dòng
     * bắt buộc là TÊN VẬT PHẨM (client tra từ item_template), nên không đặt tên boss vào dòng
     * được; chỗ đặt chữ tự do duy nhất là tên tab và dòng chữ màu của từng dòng.
     */
    private void xemChiTiet(Player player, String tenBoss) {
        Dong d = null;
        for (Dong x : gom()) {
            if (x.ten.equals(tenBoss)) {
                d = x;
                break;
            }
        }
        if (d == null) {
            createOtherMenu(player, ConstNpc.IGNORE_MENU, "Không còn thấy con này nữa.", "Đóng");
            return;
        }

        StringBuilder sb = new StringBuilder();
        sb.append(d.ten).append("\n");
        if (d.dangRa > 0) {
            sb.append("ĐANG RA MAP: ").append(String.join(", ", d.map)).append("\n");
            if (d.tong > 1) {
                sb.append("(").append(d.dangRa).append("/").append(d.tong).append(" bản đang ra)\n");
            }
        } else {
            sb.append("Đang nghỉ, chưa ra map.\n");
        }
        if (d.mauMax > 0) {
            sb.append("Máu: ").append(dinhDang(d.mauMax)).append("\n");
        }

        List<MucRoi> bang = BangRoiBoss.xem(d.bossId);
        if (bang.isEmpty()) {
            sb.append("\nChưa khai báo bảng rơi.\nCon này rơi theo luật riêng của nó.");
            createOtherMenu(player, ConstNpc.IGNORE_MENU, sb.toString(), "Đóng");
            return;
        }
        sb.append("\nBấm xem bảng đồ rơi bên dưới.");
        moBangDoRoi(player, d, bang);
    }

    /** Số ô tối đa của bảng — gói tin ghi số ô bằng một byte, và dài quá cũng khó xem. */
    private static final int TOI_DA_O = 60;

    /** Dựng khung tiệm "chỉ để xem" cho bảng rơi của một boss. */
    private void moBangDoRoi(Player player, Dong d, List<MucRoi> bang) {
        List<Item> ds = new ArrayList<>();
        List<String> nhan = new ArrayList<>();
        StringBuilder themVao = new StringBuilder();
        int bo = 0;

        for (MucRoi m : bang) {
            if (m.loai == 1) {
                // Đồ Thần Linh không có id cố định (bốc ngẫu nhiên lúc rơi) nên không đặt
                // vào ô được — nhắc bằng dòng thông báo.
                themVao.append("\n• Đồ Thần Linh ngẫu nhiên");
                continue;
            }
            if (m.ids == null || m.ids.length == 0) {
                continue;
            }
            // Một dòng có nhiều id (kiểu "Ngọc Rồng 3-5 sao") thì hiện thành nhiều ô,
            // mỗi ô một vật phẩm thật, để người chơi thấy đúng cái mình sắp nhặt.
            for (int id : m.ids) {
                if (ds.size() >= TOI_DA_O) {
                    bo++;
                    continue;
                }
                if (id < 0 || id >= Manager.ITEM_TEMPLATES.size()) {
                    continue;
                }
                Item it;
                try {
                    it = ItemService.gI().createNewItem((short) id);
                } catch (Exception e) {
                    continue;
                }
                if (it == null || it.template == null) {
                    continue;
                }
                it.quantity = Math.max(1, m.slMin);
                // Không gắn dòng tỉ lệ: chủ dự án chốt bảng chỉ liệt kê MÓN, xem tỉ lệ thì
                // vào cpanel. Giữ nguyên chỉ số sẵn có của vật phẩm cho đúng hàng thật.
                ds.add(it);
                nhan.add("");
            }
        }

        if (ds.isEmpty() && themVao.length() == 0) {
            createOtherMenu(player, ConstNpc.IGNORE_MENU,
                    "Chưa khai báo đồ rơi cho con này.", "Đóng");
            return;
        }

        // Tên boss và trạng thái gửi kèm bằng thông báo, vì khung tiệm chỉ có chỗ cho
        // chữ trên nút tab.
        StringBuilder tin = new StringBuilder(d.ten);
        tin.append(d.dangRa > 0 ? " — đang ra ở " + String.join(", ", d.map) : " — đang nghỉ");
        if (themVao.length() > 0) {
            tin.append(themVao);
        }
        if (bo > 0) {
            tin.append("\n(còn ").append(bo).append(" món nữa không đủ chỗ hiện)");
        }
        Service.gI().sendThongBao(player, tin.toString());

        if (ds.isEmpty()) {
            createOtherMenu(player, ConstNpc.IGNORE_MENU, tin.toString(), "Đóng");
            return;
        }
        String tenTab = d.ten + "\n" + (d.dangRa > 0 ? String.join(", ", d.map) : "đang nghỉ");
        ShopService.gI().moBangXem(player, tenTab, ds, nhan.toArray(new String[0]));
    }

    /** 500000000 -> "500.000.000" */
    private static String dinhDang(long n) {
        String s = String.valueOf(n);
        StringBuilder sb = new StringBuilder();
        int dem = 0;
        for (int i = s.length() - 1; i >= 0; i--) {
            sb.append(s.charAt(i));
            if (++dem % 3 == 0 && i > 0) {
                sb.append('.');
            }
        }
        return sb.reverse().toString();
    }
}
