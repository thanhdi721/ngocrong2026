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
import nro.models.map.Zone;
import nro.models.npc.Npc;
import nro.models.npc.NpcFactory;
import nro.models.player.Player;
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
                    xemDangRa(player);
                case 1 ->
                    moTrang(player, 0);
                default -> {
                }
            }
            return;
        }
        int trang = menu - ConstNpc.THEO_DOI_BOSS_TRANG;
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
            moTrang(player, trang + 1);
        }
    }

    /** Danh sách boss đang đứng ngoài map, kèm tên map. */
    private void xemDangRa(Player player) {
        List<Dong> ds = gom();
        StringBuilder sb = new StringBuilder("BOSS ĐANG RA MAP\n\n");
        int in = 0;
        int con = 0;
        for (Dong d : ds) {
            if (d.dangRa <= 0) {
                continue;
            }
            if (in < TOI_DA_DONG) {
                sb.append(d.ten);
                if (d.dangRa > 1) {
                    sb.append(" x").append(d.dangRa);
                }
                sb.append(" — ").append(String.join(", ", d.map)).append("\n");
                in++;
            } else {
                con++;
            }
        }
        if (in == 0) {
            sb.append("Không có con nào ngoài map.\nĐợi giờ đi.");
        } else if (con > 0) {
            sb.append("\n... và ").append(con).append(" loại nữa.");
        }
        createOtherMenu(player, ConstNpc.IGNORE_MENU, sb.toString(), "Đóng");
    }

    /** Menu chọn boss của trang {@code trang}. */
    private void moTrang(Player player, int trang) {
        List<Dong> ds = gom();
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
            muc.add((d.dangRa > 0 ? "● " : "○ ") + d.ten);
        }
        if (tong > 1) {
            muc.add("Xem tiếp\n(trang " + (trang + 2 > tong ? 1 : trang + 2) + "/" + tong + ")");
        }
        muc.add("Đóng");
        createOtherMenu(player, ConstNpc.THEO_DOI_BOSS_TRANG + trang,
                "Trang " + (trang + 1) + "/" + tong + "\n● đang ra map   ○ đang nghỉ",
                muc.toArray(new String[0]), ten.toArray(new String[0]));
    }

    /** Chi tiết một boss: trạng thái, map đang ở, và bảng rơi đồ kèm tỉ lệ. */
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

        sb.append("\nĐỒ RƠI:\n");
        List<MucRoi> bang = BangRoiBoss.cua(d.bossId);
        if (bang.isEmpty()) {
            sb.append("Chưa khai báo bảng rơi.\nCon này rơi theo luật riêng của nó.");
        } else {
            for (MucRoi m : bang) {
                sb.append("• ").append(m.tenVatPham());
                String sl = m.chuoiSoLuong();
                if (!sl.isEmpty()) {
                    sb.append(" ").append(sl);
                }
                sb.append(" — ").append(m.tiLe).append("%");
                if (m.nhom > 0) {
                    sb.append(" (chung lượt ").append(m.nhom).append(")");
                }
                sb.append("\n");
            }
            sb.append("\nCác dòng \"chung lượt\" tranh nhau\nmột lần quay, chỉ một cái trúng.");
        }
        createOtherMenu(player, ConstNpc.IGNORE_MENU, sb.toString(), "Đóng");
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
