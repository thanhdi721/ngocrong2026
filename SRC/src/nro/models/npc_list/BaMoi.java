package nro.models.npc_list;

import nro.models.consts.ConstNpc;
import nro.models.npc.Npc;
import nro.models.player.Player;
import nro.models.services.ThongDitService;
import nro.models.utils.Util;

/**
 * NPC "Bà Mối" đứng cạnh GoKu Nỗi Loạn ở đảo Kamê (map 5), ngoại hình lấy từ cải trang
 * "Cải trang Bunma rực rỡ" (item 1476, part 1380 / 1381 / 1382).
 *
 * <p>Bà không bán gì, không nhận nhiệm vụ. Bà chỉ giữ một cuốn sổ: ai đã thông ai bao nhiêu
 * lần, và ai bị thông bao nhiêu lần — số liệu lấy thẳng từ {@link ThongDitService}.
 */
public class BaMoi extends Npc {

    /** Câu chào, bốc ngẫu nhiên cho đỡ nhàm. */
    private static final String[] CHAO = {
        "Lại đây con, bà xem sổ cho.",
        "Bà làm nghề này ba mươi năm, chưa sai ai bao giờ.",
        "Đứng xa ra tí, bà vừa lau sổ xong.",
        "Nghe tiếng con từ đầu đảo Kamê rồi đó."
    };

    /** Xếp hạng theo số lần ĐI THÔNG. */
    private static final String[] DANH_HIEU_THONG = {
        "Trai tân đảo Kamê",             // 0
        "Tập sự, tay còn run",           // 1-2
        "Thợ lành nghề",                 // 3-5
        "Cao thủ có số má",              // 6-9
        "ĐẠI SƯ CHÍ TÔN"                 // 10
    };

    /** Xếp hạng theo số lần BỊ THÔNG. */
    private static final String[] DANH_HIEU_BI_THONG = {
        "Còn nguyên vẹn",                // 0
        "Hơi ê ẩm",                      // 1-4
        "Đi đứng hơi lạ",                // 5-9
        "Chai sạn theo năm tháng",       // 10-15
        "HUYỀN THOẠI SỐNG"               // 16-20
    };

    public BaMoi(int mapId, int status, int cx, int cy, int tempId, int avartar) {
        super(mapId, status, cx, cy, tempId, avartar);
    }

    private static String hangThong(int n) {
        if (n <= 0) {
            return DANH_HIEU_THONG[0];
        }
        if (n <= 2) {
            return DANH_HIEU_THONG[1];
        }
        if (n <= 5) {
            return DANH_HIEU_THONG[2];
        }
        if (n <= 9) {
            return DANH_HIEU_THONG[3];
        }
        return DANH_HIEU_THONG[4];
    }

    private static String hangBiThong(int n) {
        if (n <= 0) {
            return DANH_HIEU_BI_THONG[0];
        }
        if (n <= 4) {
            return DANH_HIEU_BI_THONG[1];
        }
        if (n <= 9) {
            return DANH_HIEU_BI_THONG[2];
        }
        if (n <= 15) {
            return DANH_HIEU_BI_THONG[3];
        }
        return DANH_HIEU_BI_THONG[4];
    }

    @Override
    public void openBaseMenu(Player player) {
        if (canOpenNpc(player)) {
            createOtherMenu(player, ConstNpc.BA_MOI_MENU,
                    CHAO[Util.nextInt(0, CHAO.length - 1)] + "\nCon muốn hỏi gì?",
                    "Xem sổ\ncủa con", "Luật\nlệ", "Thôi");
        }
    }

    @Override
    public void confirmMenu(Player player, int select) {
        if (!canOpenNpc(player) || player.idMark.getIndexMenu() != ConstNpc.BA_MOI_MENU) {
            return;
        }
        switch (select) {
            case 0 ->
                xemSo(player);
            case 1 ->
                xemLuat(player);
            default -> {
            }
        }
    }

    private void xemSo(Player player) {
        int thong = Math.max(0, Math.min(player.soLanThong, ThongDitService.TOI_DA_THONG));
        int bi = Math.max(0, Math.min(player.soLanBiThong, ThongDitService.TOI_DA_BI_THONG));
        int phanTram = ThongDitService.phanTram(player);

        StringBuilder sb = new StringBuilder();
        sb.append("SỔ HỘ TỊCH CỦA BÀ MỐI\n");
        sb.append("Đương sự: ").append(player.name).append("\n\n");
        sb.append("Đã thông người ta: ").append(thong).append("/")
                .append(ThongDitService.TOI_DA_THONG).append(" lần\n");
        sb.append("  → ").append(hangThong(thong)).append("\n\n");
        sb.append("Bị người ta thông: ").append(bi).append("/")
                .append(ThongDitService.TOI_DA_BI_THONG).append(" lần\n");
        sb.append("  → ").append(hangBiThong(bi)).append("\n\n");
        sb.append("Cộng lại: +").append(phanTram).append("% HP, KI và sức đánh.\n");
        if (thong == 0 && bi == 0) {
            sb.append("Sổ trắng tinh. Trong sáng đến mức bà thấy thương.");
        } else if (phanTram >= 20) {
            sb.append("Kịch trần cả hai chiều. Bà lạy con.");
        } else if (thong >= bi) {
            sb.append("Cho nhiều hơn nhận. Bà ghi nhận tấm lòng.");
        } else {
            sb.append("Nhận nhiều hơn cho. Cũng là một cách sống.");
        }
        createOtherMenu(player, ConstNpc.IGNORE_MENU, sb.toString(), "Bà giữ\nkín giùm con");
    }

    private void xemLuat(Player player) {
        createOtherMenu(player, ConstNpc.IGNORE_MENU,
                "LUẬT CỦA BÀ:\n"
                + "1. Phải có Nhẫn Chí Tôn trong túi, mỗi lần mất 1 cái.\n"
                + "   Nhẫn rơi 5% từ hai boss Siêu Thần God.\n"
                + "2. Hai đứa phải đứng SÁT nhau, bấm vào người kia,\n"
                + "   vào menu thách đấu, chọn \"Thông đít\".\n"
                + "3. Bên kia phải ĐỒNG Ý. Bà không dung túng cưỡng ép.\n"
                + "4. Đi thông: tối đa " + ThongDitService.TOI_DA_THONG
                + " lần, mỗi lần +1%.\n"
                + "5. Bị thông: tối đa " + ThongDitService.TOI_DA_BI_THONG
                + " lần, 2 lần mới +1%.\n"
                + "   Chịu đủ " + ThongDitService.TOI_DA_BI_THONG + " lần cũng được +10%.\n"
                + "6. Đừng hỏi bà vì sao. Bà cũng không biết.",
                "Con hiểu rồi");
    }
}
