package nro.models.npc_list;

import nro.models.consts.ConstNpc;
import nro.models.database.PlayerDAO;
import nro.models.npc.Npc;
import nro.models.player.Player;
import nro.models.services.Service;
import nro.models.services_func.Input;
import nro.models.utils.Util;

/**
 * NPC 85 — "ADMIN Đẹp Trai", đứng ở nhà cả 3 hành tinh (map 21 Nhà Gôhan,
 * 22 Nhà Moori, 23 Nhà Broly). Ngoại hình lấy từ cải trang "CT Goku SSJ Blue"
 * (item 1590: head 1457 / body 1459 / leg 1460).
 *
 * <p>
 * Chỉ là "cửa vào" cho hai form đổi VND đã có sẵn trong {@link Input}:
 * {@code createFormTradeGold} ({@code TRADE_GOLD}) và
 * {@code createFormTradeGem} ({@code TRADE_GEM}). Toàn bộ điều kiện (kích
 * hoạt tài khoản, min/max, trừ VND xuống DB trước khi phát) nằm trong
 * {@code Input.doInput} — lớp này KHÔNG trừ hay phát gì cả.
 *
 * <p>
 * Dữ liệu: {@code SRC/sql/patch/09-npc-admin-dep-trai.sql}. Tài liệu:
 * {@code docs/4-trien-khai/44-npc-admin-dep-trai.md}.
 */
public class AdminDepTrai extends Npc {

    /** Giá mở thành viên (kích hoạt tài khoản), tính bằng VND. */
    public static final int GIA_MO_THANH_VIEN = 10_000;

    /** Menu xác nhận mở thành viên. */
    private static final int MENU_MO_THANH_VIEN = 2230;

    private static final String[] MENU_CHINH = {
        "Đổi VND\nra Thỏi vàng", "Đổi VND\nra Ngọc", "Mở\nthành viên", "Nhập\nGiftcode", "Xem số dư", "Đóng"};

    private static final String[] MENU_SO_DU = {
        "Đổi VND\nra Thỏi vàng", "Đổi VND\nra Ngọc", "Đóng"};

    public AdminDepTrai(int mapId, int status, int cx, int cy, int tempId, int avartar) {
        super(mapId, status, cx, cy, tempId, avartar);
    }

    @Override
    public void openBaseMenu(Player player) {
        if (!canOpenNpc(player)) {
            return;
        }
        this.createOtherMenu(player, ConstNpc.BASE_MENU,
                "Chào " + player.name + "! Ta là ADMIN Đẹp Trai — đẹp trai nhất 3 hành tinh.\n"
                + "Có VND trong tài khoản thì ta đổi ra Thỏi vàng hoặc Ngọc cho, nhanh gọn lẹ!\n"
                + "10.000đ = 40 Thỏi vàng  |  10.000đ = 10.000 Ngọc\n"
                + "Mở thành viên: " + Util.numberFormat(GIA_MO_THANH_VIEN) + "đ  |  Nhập giftcode miễn phí",
                MENU_CHINH);
    }

    @Override
    public void confirmMenu(Player player, int select) {
        if (!canOpenNpc(player)) {
            return;
        }
        int indexMenu = player.idMark.getIndexMenu();
        if (indexMenu == ConstNpc.BASE_MENU) {
            switch (select) {
                case 0 -> moFormDoiThoiVang(player);
                case 1 -> moFormDoiNgoc(player);
                case 2 -> hoiMoThanhVien(player);
                case 3 -> Input.gI().createFormGiftCode(player);
                case 4 -> xemSoDu(player);
                default -> {
                }
            }
        } else if (indexMenu == MENU_MO_THANH_VIEN) {
            if (select == 0) {
                moThanhVien(player);
            }
        } else if (indexMenu == ConstNpc.MENU_ADMIN_DEP_TRAI_SO_DU) {
            switch (select) {
                case 0 -> moFormDoiThoiVang(player);
                case 1 -> moFormDoiNgoc(player);
                default -> {
                }
            }
        }
    }

    /**
     * Đọc lại số dư từ DB (tiền nạp qua web khi đang online) rồi mở form có sẵn.
     * Không kiểm điều kiện ở đây để khỏi lệch với phần xử lý trong Input.
     */
    private void moFormDoiThoiVang(Player player) {
        PlayerDAO.reloadVnd(player);
        Input.gI().createFormTradeGold(player);
    }

    private void moFormDoiNgoc(Player player) {
        PlayerDAO.reloadVnd(player);
        Input.gI().createFormTradeGem(player);
    }

    /** Hỏi xác nhận trước khi trừ tiền. */
    private void hoiMoThanhVien(Player player) {
        if (player.getSession() == null) {
            return;
        }
        if (player.getSession().actived) {
            Service.gI().sendThongBao(player, "Tài khoản của ngươi đã là thành viên rồi");
            return;
        }
        PlayerDAO.reloadVnd(player);
        int vnd = Math.max(0, player.getSession().vnd);
        this.createOtherMenu(player, MENU_MO_THANH_VIEN,
                "Mở thành viên để đổi được Thỏi vàng / Ngọc và vào các phó bản bang hội.\n"
                + "Giá: " + Util.numberFormat(GIA_MO_THANH_VIEN) + "đ\n"
                + "Số dư của ngươi: " + Util.numberFormat(vnd) + "đ",
                "Đồng ý", "Từ chối");
    }

    private void moThanhVien(Player player) {
        if (player.getSession() == null) {
            return;
        }
        if (player.getSession().actived) {
            Service.gI().sendThongBao(player, "Tài khoản của ngươi đã là thành viên rồi");
            return;
        }
        PlayerDAO.reloadVnd(player);
        if (player.getSession().vnd < GIA_MO_THANH_VIEN) {
            Service.gI().sendThongBao(player, "Không đủ " + Util.numberFormat(GIA_MO_THANH_VIEN)
                    + "đ, hãy nạp thêm rồi quay lại");
            return;
        }
        if (PlayerDAO.MuaThanhVien(player, GIA_MO_THANH_VIEN)) {
            Service.gI().sendThongBao(player, "Mở thành viên thành công! Còn "
                    + Util.numberFormat(Math.max(0, player.getSession().vnd)) + "đ");
        } else {
            Service.gI().sendThongBao(player, "Không mở được thành viên, số dư có thể vừa thay đổi. Thử lại nhé");
        }
    }

    private void xemSoDu(Player player) {
        if (player.getSession() == null) {
            return;
        }
        if (!PlayerDAO.reloadVnd(player)) {
            Service.gI().sendThongBao(player, "Không đọc được số dư, số hiển thị có thể chưa mới nhất");
        }
        int vnd = Math.max(0, player.getSession().vnd);
        int tongNap = Math.max(0, player.getSession().tongnap);
        String say = "Sổ sách của " + player.name + " đây, ADMIN không bao giờ tính sai:\n"
                + "Số dư hiện tại: " + Util.numberFormat(vnd) + "đ\n"
                + "Tổng đã nạp: " + Util.numberFormat(tongNap) + "đ\n"
                + "Tài khoản: " + (player.getSession().actived ? "đã kích hoạt" : "CHƯA kích hoạt")
                + "\n(Đổi Thỏi vàng và Ngọc đều cần kích hoạt; mỗi lần đổi 10.000đ – 5.000.000đ)";
        this.createOtherMenu(player, ConstNpc.MENU_ADMIN_DEP_TRAI_SO_DU, say, MENU_SO_DU);
    }
}
