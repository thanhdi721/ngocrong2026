package nro.models.npc_list;

import nro.models.consts.ConstNpc;
import nro.models.npc.Npc;
import nro.models.npc.NpcFactory;
import nro.models.player.Player;
import nro.models.tu_tien.BangVangNoLo;
import nro.models.tu_tien.LuyenDan;

/**
 * NPC "Lò Luyện Đan" ở đảo Kamê (map 5) — NPC dạng <b>vật thể</b>, không phải người.
 *
 * <p>Ngoại hình dựng theo đúng cách của "Cây thông Noel" (npc 79): part đầu vẽ một icon to
 * (icon 33001, ảnh lò), part thân và chân để trong suốt. Xem patch 84 khối (2b).
 *
 * <p>Ba menu: chọn công thức → xem nguyên liệu và tỉ lệ → luyện. Toàn bộ luật nằm ở
 * {@link LuyenDan}, lớp này chỉ lo phần bấm nút.
 */
public class LoLuyenDan extends Npc {

    public LoLuyenDan(int mapId, int status, int cx, int cy, int tempId, int avartar) {
        super(mapId, status, cx, cy, tempId, avartar);
    }

    @Override
    public void openBaseMenu(Player player) {
        if (!canOpenNpc(player)) {
            return;
        }
        createOtherMenu(player, ConstNpc.LO_LUYEN_DAN_MENU,
                "Lò của Lão Quân. Ba phần nguyên liệu, một phần số trời.\n"
                + "Địa Hỏa Tinh: " + LuyenDan.dem(player, LuyenDan.DIA_HOA_TINH) + "\n"
                + "Ngươi đã nổ lò " + BangVangNoLo.soLanCua(player) + " lần.",
                "Luyện đan", "Vua Nổ Lò", "Đóng");
    }

    @Override
    public void confirmMenu(Player player, int select) {
        if (!canOpenNpc(player)) {
            return;
        }
        int menu = player.idMark.getIndexMenu();
        if (menu == ConstNpc.LO_LUYEN_DAN_MENU) {
            if (select == 0) {
                moChonCongThuc(player);
            } else if (select == 1) {
                BangVangNoLo.mo(player);
            }
            return;
        }
        if (menu == ConstNpc.LO_LUYEN_DAN_CHON) {
            if (select < 0 || select >= LuyenDan.CONG_THUC.length) {
                openBaseMenu(player);       // nút "Quay lại" nằm ngay sau danh sách
                return;
            }
            moXacNhan(player, select);
            return;
        }
        if (menu == ConstNpc.LO_LUYEN_DAN_XAC_NHAN) {
            if (select != 0) {
                moChonCongThuc(player);     // "Quay lại"
                return;
            }
            LuyenDan.luyen(player, chiSoDaChon(player));
            // Mở lại bảng chọn để luyện mẻ tiếp theo mà không phải bấm vào lò lần nữa.
            moChonCongThuc(player);
        }
    }

    //================================ menu con ================================
    private void moChonCongThuc(Player player) {
        String[] nhan = LuyenDan.nhanCongThuc();
        String[] nut = new String[nhan.length + 1];
        System.arraycopy(nhan, 0, nut, 0, nhan.length);
        nut[nhan.length] = "Quay lại";
        createOtherMenu(player, ConstNpc.LO_LUYEN_DAN_CHON,
                "Luyện viên nào?", nut);
    }

    private void moXacNhan(Player player, int chiSo) {
        LuyenDan.CongThuc ct = LuyenDan.congThuc(chiSo);
        if (ct == null) {
            openBaseMenu(player);
            return;
        }
        // Nhớ công thức đang chọn để bước xác nhận biết luyện cái gì.
        createOtherMenu(player, ConstNpc.LO_LUYEN_DAN_XAC_NHAN,
                LuyenDan.moTa(player, chiSo)
                + "\nHỏng thì mất sạch nguyên liệu.",
                new String[]{"Luyện", "Quay lại"}, chiSo);
    }

    /** Công thức người chơi vừa chọn; hỏng thì trả -1 để {@code luyen} tự bỏ qua. */
    private static int chiSoDaChon(Player player) {
        try {
            Object o = NpcFactory.PLAYERID_OBJECT.get(player.id);
            return o == null ? -1 : Integer.parseInt(String.valueOf(o));
        } catch (Exception e) {
            return -1;
        }
    }
}
