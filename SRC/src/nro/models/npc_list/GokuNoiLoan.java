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
 * <p>Chỉ có một việc: cho người chơi <b>chọn hào quang</b> trong danh sách {@link #HAO_QUANG}
 * hoặc tắt đi. Hào quang đang chọn lưu ở cột {@code player.aura_npc} nên đăng nhập lại vẫn còn,
 * và {@link Player#getAura()} trả về nó, đè lên hào quang của thẻ rađa.
 *
 * <p>Ảnh nằm ở {@code data/img_by_name/x1..x4/aura_[id]_0.png}; mỗi id phải có một dòng trong
 * bảng {@code img_by_name} khai đúng số khung (patch 62, 66, 74).
 */
public class GokuNoiLoan extends Npc {

    /** Mỗi dòng: {id hào quang, tên hiện trong menu}. id phải ≤ 127 vì getAura trả về byte. */
    private static final Object[][] HAO_QUANG = {
        {98, "Goku Purple"},
        {99, "Khí Xanh Dương"},
        {100, "Khí Vàng"},
        {101, "Khí Kim Quang"},
        {102, "Khí Bạch Kim"},
        {103, "Khí Huyết Đỏ"},
        {104, "Khí Tử Điện"},
        {105, "Khí Ngọc Bích"},
        {106, "Khí Xích Long"},
        {107, "Khí Lục Diệp"},
        {108, "Khí Bạch Vân"},
        {109, "Hồng Vân"},
        {110, "Cột Sáng Hồng"},
        {111, "Hỏa Diệm Đỏ"},
        {112, "Lam Diệm"},
        {113, "Tử Diệm"},
        {114, "Hỏa Diệm Cam"},
        {115, "Tử Quang"},
    };

    /** Số hào quang xếp trong một trang menu; dư thì thêm nút "Xem tiếp". */
    private static final int MOI_TRANG = 8;

    public GokuNoiLoan(int mapId, int status, int cx, int cy, int tempId, int avartar) {
        super(mapId, status, cx, cy, tempId, avartar);
    }

    private static int soTrang() {
        return (HAO_QUANG.length + MOI_TRANG - 1) / MOI_TRANG;
    }

    private static String ten(int id) {
        for (Object[] hq : HAO_QUANG) {
            if ((int) hq[0] == id) {
                return (String) hq[1];
            }
        }
        return "?";
    }

    @Override
    public void openBaseMenu(Player player) {
        if (canOpenNpc(player)) {
            if (player.auraNpc >= 0) {
                createOtherMenu(player, ConstNpc.BASE_MENU,
                        "Ngươi đang khoác \"" + ten(player.auraNpc) + "\".\nĐổi cái khác hay tắt đi?",
                        "Đổi\nhào quang", "Tắt\nhào quang", "Đóng");
            } else {
                createOtherMenu(player, ConstNpc.BASE_MENU,
                        "Ta có " + HAO_QUANG.length + " loại hào quang.\nNgươi muốn khoác cái nào?",
                        "Chọn\nhào quang", "Đóng");
            }
        }
    }

    /** Mở menu trang {@code trang}: 8 hào quang + nút xem tiếp (nếu có nhiều trang) + đóng. */
    private void moTrang(Player player, int trang) {
        int tong = soTrang();
        trang = ((trang % tong) + tong) % tong;
        int dau = trang * MOI_TRANG;
        int cuoi = Math.min(dau + MOI_TRANG, HAO_QUANG.length);
        String[] muc = new String[cuoi - dau + (tong > 1 ? 2 : 1)];
        for (int i = dau; i < cuoi; i++) {
            muc[i - dau] = (String) HAO_QUANG[i][1];
        }
        if (tong > 1) {
            muc[muc.length - 2] = "Xem tiếp\n(trang " + (trang + 2 > tong ? 1 : trang + 2) + "/" + tong + ")";
        }
        muc[muc.length - 1] = "Đóng";
        // Nhớ trang đang xem bằng cách cộng vào số hiệu menu, lúc bấm trừ ra là biết trang nào.
        createOtherMenu(player, ConstNpc.CHON_HAO_QUANG + trang,
                "Hào quang — trang " + (trang + 1) + "/" + tong, muc);
    }

    @Override
    public void confirmMenu(Player player, int select) {
        if (!canOpenNpc(player)) {
            return;
        }
        int menu = player.idMark.getIndexMenu();
        if (menu == ConstNpc.BASE_MENU) {
            if (select == 0) {
                moTrang(player, 0);
            } else if (select == 1 && player.auraNpc >= 0) {
                doiHaoQuang(player, -1);
            }
            return;
        }
        int trang = menu - ConstNpc.CHON_HAO_QUANG;
        if (trang < 0 || trang >= soTrang()) {
            return;
        }
        int dau = trang * MOI_TRANG;
        int soMuc = Math.min(MOI_TRANG, HAO_QUANG.length - dau);
        if (select < soMuc) {
            doiHaoQuang(player, (int) HAO_QUANG[dau + select][0]);
        } else if (soTrang() > 1 && select == soMuc) {
            moTrang(player, trang + 1);
        }
    }

    /** Đổi sang hào quang {@code id} (-1 là tắt) rồi báo cho cả khu thấy. */
    private void doiHaoQuang(Player player, int id) {
        player.auraNpc = id;
        guiHaoQuang(player);
        veLaiKhu(player);
        Service.gI().sendThongBao(player, id >= 0 ? "Đã khoác hào quang " + ten(id) : "Đã tắt hào quang");
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
