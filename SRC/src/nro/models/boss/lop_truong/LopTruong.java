package nro.models.boss.lop_truong;

import java.util.ArrayList;
import java.util.List;

import nro.models.boss.Boss;
import nro.models.boss.BossData;
import nro.models.boss.BossDropConfig;
import nro.models.boss.BossDropRate;
import nro.models.boss.BossID;
import nro.models.consts.BossStatus;
import nro.models.consts.ConstPlayer;
import nro.models.map.ItemMap;
import nro.models.map.Zone;
import nro.models.map.service.MapService;
import nro.models.player.Player;
import nro.models.services.ItemService;
import nro.models.services.Service;
import nro.models.skill.Skill;
import nro.models.utils.Util;

/**
 * Bộ boss "Lốp Trưởng" — 8 con, mỗi lượt chỉ ra 2 con cùng một khu, kèm Nữ Thần Băng Tinh
 * đứng giữa. Hai con cãi nhau tranh gái rồi lao vào đánh nhau, nhưng KHÔNG trừ máu nhau
 * (chỉ là diễn) và KHÔNG đánh người chơi. Người chơi mới là bên hạ được boss.
 *
 * Hai đợt luân phiên:
 *   đợt A — 2 tỷ máu, giảm 50% sát thương, rơi đồ như Super Black Goku;
 *   đợt B —  20.000 máu, sát thương người chơi bị chặn tối đa 100 (cho người cày chay).
 *
 * Hết lượt: 15 phút sau ra lại. Không ai vào khu trong 30 phút thì tự đi.
 */
public class LopTruong extends Boss {

    public static final int SO_LUONG = 8;

    /** Hình lấy từ 8 cải trang Goku Super Saiyan (patch 48): {đầu, thân, chân}. */
    private static final short[][] HINH = {
        {2313, 2314, 2315},  // White
        {2316, 2317, 2318},  // Red
        {2319, 2320, 2321},  // God
        {2322, 2323, 2324},  // Orange
        {2325, 2326, 2327},  // Purple
        {2328, 2329, 2330},  // Blue
        {2331, 2332, 2333},  // Light Blue
        {2334, 2335, 2336},  // Green
    };

    /** Khu boss Black Goku. */
    private static final int[] MAP_JOIN = {102, 92, 93, 94, 96, 97, 98, 99, 100};

    private static final int[][] SKILL = {
        {Skill.LIEN_HOAN, 7, 800},          // đấm liên hoàn — đánh gần, nhìn có động tác
        {Skill.LIEN_HOAN_CHUONG, 7, 2000},  // Cadic liên hoàn chưởng
        {Skill.SUPER_KAME, 7, 3000},        // Super Kamejoko
        {Skill.TAI_TAO_NANG_LUONG, 2, 60000},
    };



    public static final int DOT_2_TY = 0;
    public static final int DOT_20K = 1;

    // Ba số này sửa được trong cpanel, tab "Rơi đồ boss".
    private static long choRaLai() {
        return Math.max(1, BossDropConfig.LT_CHO_RA_LAI.giaTri) * 60_000L;
    }

    private static long tuDi() {
        return Math.max(1, BossDropConfig.LT_TU_DI.giaTri) * 60_000L;
    }

    private static int tranDame() {
        return Math.max(1, BossDropConfig.LT_TRAN_DAME.giaTri);
    }

    //========================== phần điều phối (dùng chung cho cả 8 con) ==========================
    private static final LopTruong[] TAT_CA = new LopTruong[SO_LUONG];
    private static NuThanBangTinh nuThan;
    private static LopTruong[] cap;               // 2 con đang ra
    private static int dotKeTiep = DOT_2_TY;      // luân phiên
    private static long lanKetThuc;               // lúc lượt trước kết thúc
    private static long lanCoNguoi;               // lần cuối thấy người chơi trong khu
    private static int sanX;                      // tâm sân đấu (giữa map lúc ra)
    private static int sanY;
    private static boolean daVaoMap;              // cả hai đã thật sự ra map chưa
    private static boolean daXepCho;              // đã kéo 2 boss về đứng cạnh nữ thần chưa
    private static int buocThoai = -1;            // -1: chưa diễn, >= 0: đang diễn
    private static long lanThoai;
    private static int buocKetThuc = -1;          // các bước của màn kết thúc
    private static long lanKetThucThoai;
    private static LopTruong conSong;
    private static long lanChui;                  // nhịp hai boss chửi nhau lúc đánh
    private static long lanCoVu;                  // nhịp nữ thần cổ vũ
    private static int luotChui;                  // để hai con nói luân phiên
    private static final java.util.Set<Long> DA_CHAO = new java.util.HashSet<>();

    private static final int KHOANG_CACH = 70;     // hai boss đứng cách nữ thần bao xa (pixel)
    private static final int BAN_KINH_SAN = 130;   // hai boss chỉ quần nhau trong bán kính này
    private static final int KHOANG_BAM = 60;      // xa hơn ngần này mới bước lại gần đối thủ
    private static final long NHIP_THOAI = 4000;   // mỗi câu trong màn cãi nhau (4 giây cho kịp đọc)
    private static final long NHIP_CHUI = 8000;    // vừa đánh vừa chửi (8 giây một câu)
    private static final long NHIP_CO_VU = 9000;   // nữ thần cổ vũ (9 giây một câu)
    private static final long NHIP_KET_THUC = 4000; // mỗi câu trong màn kết thúc (4 giây)

    /** Màn cãi nhau lúc mới gặp: {ai nói (0 / 1 = hai boss, 2 = nữ thần), câu nói}. */
    private static final Object[][] KICH_BAN = {
        {0, "Đứng lại đó! Nàng ấy chào ta trước, ngươi chen vào làm gì"},
        {1, "Chào ngươi? Nàng vẫy tay đuổi ruồi, ngươi tưởng vẫy mình à"},
        {0, "Ruồi? Ngươi soi gương chưa, tóc dựng như bị điện giật"},
        {1, "Tóc ta dựng vì nội lực. Còn ngươi dựng vì... đứng gần ổ điện"},
        {2, "Hai anh ơi, em chỉ đi ngang thôi mà..."},
        {0, "Nàng nói 'đi ngang' là còn ngại, ta hiểu tâm ý phụ nữ lắm"},
        {1, "Hiểu? Đời ngươi hiểu mỗi hai việc: ăn no và ngủ nướng"},
        {0, "Ít ra ta không mặc đồ trùng màu với con quái sau lưng"},
        {1, "Áo ta là màu huyền thoại! Áo ngươi là màu... nùi giẻ lau"},
        {2, "Thôi mà, hai anh cãi nhau chán chết, em về nhé"},
        {0, "Khoan! Nàng ở lại xem ta dạy nó một bài"},
        {1, "Dạy ta? Lần trước ngươi đánh con Quỷ Đất còn hụt hơi"},
        {0, "Hụt hơi vì ta nhường! Ngươi thì đánh hụt cả cái cây"},
        {1, "Cái cây đó né giỏi, không như cái mặt ngươi"},
        {2, "Ơ kìa, đánh thì ra chỗ trống, đừng làm gãy cây của em"},
        {0, "Nghe chưa? Nàng bênh ta. Vào đây!"},
        {1, "Nàng bảo tránh cây, chứ có bảo bênh ngươi đâu, ảo tưởng vừa thôi"},
        {0, "Nói nhiều. Đấm trước, nói sau!"},
        {1, "Được, ai thua thì tự đi nhặt răng"},
    };

    /** Vừa đánh vừa chửi — nói luân phiên trong lúc quần nhau. */
    private static final String[] CHUI_LUC_DANH = {
        "Đòn đó gãi ngứa à?",
        "Ngươi đấm chậm như rùa bò lên dốc",
        "Né đi chứ, đứng im cho ta đánh hoài ngại lắm",
        "Đấy là chiêu hay là ngươi đang tập thể dục?",
        "Ta mà nghiêm túc là ngươi bay về làng rồi",
        "Nàng đang nhìn kìa, gắng lên chút coi được không",
        "Ngươi hét to thế, chắc để át tiếng đau",
        "Cái chiêu này ta học hồi còn bú bình",
        "Đứng vững đi, ngã là mất mặt trước nàng đó",
        "Ta còn chưa dùng tay phải đâu nhé",
    };

    /** Nữ thần cổ vũ liên tục lúc hai con đánh nhau. */
    private static final String[] CO_VU = {
        "Cố lên! Ai thắng em... vỗ tay cho một tràng",
        "Ôi hai anh đẹp trai mà đánh dở ghê",
        "Đánh nhau vì em thì nhớ đừng làm hỏng tóc nha",
        "Anh áo sáng ơi, cằm anh kia đang hở kìa!",
        "Em đứng đây coi thôi, đừng ai kéo em vào",
        "Trời ơi hụt rồi, mắt để đâu vậy trời",
        "Hai anh nghỉ tay uống miếng nước không?",
        "Em thấy mấy con quái còn đánh hay hơn đó",
        "Đừng đánh nữa, em đói bụng rồi nè",
        "Ai thắng em cũng không yêu đâu, đánh cho vui thôi nha",
    };

    /** Câu chào khi có người chơi mới bước vào khu. */
    private static final String[] CHAO_NGUOI_CHOI = {
        "Này %s, tránh xa ra, chuyện người lớn",
        "%s tới đúng lúc, đứng đó xem ta dạy nó",
        "%s ơi, làm chứng giùm ta với, nó đánh lén",
        "Lại thêm khán giả, %s ngồi xuống coi cho vui",
    };

    private static final String[] NU_THAN_CHAO = {
        "%s ơi, cứu em với, hai anh này cãi nhau cả buổi rồi",
        "Chào %s, đừng học theo hai anh này nha",
        "%s vào đúng lúc, coi hai anh này làm trò nè",
    };

    private static final String[] SI_NHUC_THEM = {
        "Tưởng ngon lắm, hoá ra nằm cũng nhanh",
        "Về luyện thêm vài trăm năm rồi hẵng quay lại",
        "Đứng dậy nổi không, ta đỡ cho một tay?",
        "Từ nay nàng là của ta, ngươi nằm im đó",
    };

    private static final String[] NU_THAN_CHIA_TAY = {
        "Thôi hai anh nghỉ đi, em về đây",
        "Em không theo ai hết đâu, đánh nhau xấu lắm",
        "Hẹn gặp lại, nhớ đừng đánh nhau nữa nha",
        "Em đi mua mì đây, hai anh tự lo nhé",
    };

    private static final String[] SI_NHUC = {
        "Nằm đó mà mơ tiếp đi, giấc mơ vô địch",
        "Nói nhiều, đánh thì dở, ta nói rồi mà",
        "Ta thắng rồi nhé nàng ơi... ơ, nàng đi mất rồi",
        "Hôm nay ngươi nằm, mai ta cho nằm tiếp",
        "Dậy đi, còn nợ ta một bữa mì đó",
    };

    private final int chiSo;

    public LopTruong(int chiSo) throws Exception {
        super(BossID.LOP_TRUONG - chiSo, true, true, taoData(chiSo, DOT_2_TY), taoData(chiSo, DOT_20K));
        this.chiSo = chiSo;
        // Đợt 2 tỷ chặn 50% sát thương; đợt 20k không chặn (đã có trần 100 mỗi đòn).
        this.damageReducePercentByLevel = new int[]{50, 0};
        TAT_CA[chiSo] = this;
    }

    private static BossData taoData(int chiSo, int dot) {
        short[] h = HINH[chiSo];
        boolean nho = dot == DOT_20K;
        return new BossData(
                "Lốp Trưởng " + (chiSo + 1),
                ConstPlayer.TRAI_DAT,
                new short[]{h[0], h[1], h[2], -1, -1, -1},
                nho ? 10 : 2000,
                new int[]{nho ? 20_000 : 2_000_000_000},
                MAP_JOIN,
                SKILL,
                new String[]{}, new String[]{}, new String[]{},
                Math.max(60, BossDropConfig.LT_CHO_RA_LAI.giaTri * 60));
    }

    /** Tạo đủ 8 con + nữ thần. Gọi một lần lúc khởi động. */
    public static void taoTatCa() throws Exception {
        for (int i = 0; i < SO_LUONG; i++) {
            if (TAT_CA[i] == null) {
                new LopTruong(i);
            }
        }
        if (nuThan == null) {
            nuThan = new NuThanBangTinh();
        }
        lanKetThuc = System.currentTimeMillis();
    }

    //========================== điều khiển từ cpanel ==========================
    /** Cho cặp boss ra ngay, bỏ qua thời gian chờ. */
    public static synchronized String epRaNgay() {
        if (cap != null) {
            return "Đang có Lốp Trưởng " + (cap[0].chiSo + 1) + " và " + (cap[1].chiSo + 1)
                    + " ngoài map rồi, kết thúc lượt trước đã.";
        }
        lanKetThuc = 0;
        raCapMoi();
        return cap == null ? "Chưa ra được, thử lại sau vài giây."
                : "Đã cho Lốp Trưởng " + (cap[0].chiSo + 1) + " và " + (cap[1].chiSo + 1)
                  + " ra map " + (cap[0].zoneFinal != null ? cap[0].zoneFinal.map.mapName : "?")
                  + " (đợt " + (cap[0].currentLevel + 1 == DOT_20K ? "20k máu" : "2 tỷ máu") + ").";
    }

    /** Kết thúc lượt đang chạy, cho cả hai và nữ thần biến mất. */
    public static synchronized String epKetThuc() {
        if (cap == null) {
            return "Đang không có lượt nào.";
        }
        ketThuc(null);
        return "Đã kết thúc lượt, " + BossDropConfig.LT_CHO_RA_LAI.giaTri + " phút nữa ra lại.";
    }

    /** Một dòng trạng thái cho cpanel. */
    public static String moTaTrangThai() {
        if (cap == null) {
            long conLai = choRaLai() - (System.currentTimeMillis() - lanKetThuc);
            if (conLai < 0) {
                conLai = 0;
            }
            return "đang nghỉ, còn " + (conLai / 1000) + " giây nữa ra lượt mới (đợt kế: "
                    + (dotKeTiep == DOT_20K ? "20k máu" : "2 tỷ máu") + ")";
        }
        String map = cap[0].zone != null ? cap[0].zone.map.mapName : "chưa vào map";
        String dot = cap[0].currentLevel == DOT_20K ? "20k máu" : "2 tỷ máu";
        String buoc = buocThoai < 0 ? "chờ người vào khu"
                : (buocThoai < KICH_BAN.length ? "đang cãi nhau (" + buocThoai + "/" + KICH_BAN.length + ")"
                   : "đang đánh nhau");
        return "Lốp Trưởng " + (cap[0].chiSo + 1) + " và " + (cap[1].chiSo + 1) + " ở " + map
                + ", đợt " + dot + ", " + buoc;
    }

    public static NuThanBangTinh getNuThan() {
        return nuThan;
    }

    public int getDot() {
        return this.currentLevel;
    }

    //========================== điều phối ==========================
    @Override
    public void update() {
        if (this.chiSo == 0) {
            try {
                dieuPhoi();
            } catch (Exception e) {
                nro.models.utils.Logger.error("Lốp Trưởng — lỗi điều phối: " + e + "\n");
            }
        }
        super.update();
    }

    private static void dieuPhoi() {
        long now = System.currentTimeMillis();
        if (cap == null) {
            if (now - lanKetThuc >= choRaLai()) {
                raCapMoi();
            }
            return;
        }
        // Màn kết thúc chạy TRƯỚC mọi kiểm tra khác: con vừa chết đã rời map nên zone của nó
        // thành rỗng, nếu chặn ở dưới thì mấy câu sỉ nhục không bao giờ được nói.
        if (buocKetThuc >= 0) {
            chayManKetThuc(now);
            return;
        }
        // Chờ cả hai thật sự đứng trong map rồi mới xét sống chết: ngay sau lệnh cho ra, máu
        // chưa khởi tạo nên isDie() còn true, xét sớm là kết thúc lượt oan.
        if (!daVaoMap) {
            if (cap[0].zone != null && cap[1].zone != null && !cap[0].isDie() && !cap[1].isDie()) {
                daVaoMap = true;
            }
            return;
        }
        if (cap[0].isDie() || cap[1].isDie()) {
            conSong = cap[0].isDie() ? cap[1] : cap[0];
            buocKetThuc = 0;
            lanKetThucThoai = 0;
            return;
        }
        if (cap[0].zone == null || cap[1].zone == null) {
            return;
        }
        Zone zone = cap[0].zone;
        if (!daXepCho) {
            xepChoDung(zone);
        }
        boolean coNguoi = coNguoiChoi(zone);
        if (coNguoi) {
            lanCoNguoi = now;
        }
        // Không ai vào khu 30 phút -> tự đi
        if (!coNguoi && now - lanCoNguoi >= tuDi()) {
            ketThuc(null);
            return;
        }
        // Có người vào khu thì bắt đầu màn cãi nhau
        if (buocThoai == -1 && coNguoi) {
            buocThoai = 0;
            lanThoai = 0;
        }
        if (buocThoai >= 0 && buocThoai < KICH_BAN.length) {
            if (now - lanThoai >= NHIP_THOAI) {
                Object[] dong = KICH_BAN[buocThoai];
                int ai = (Integer) dong[0];
                String cau = (String) dong[1];
                if (ai == 2) {
                    if (nuThan != null) {
                        nuThan.chat(cau);
                    }
                } else {
                    cap[ai].chat(cau);
                }
                lanThoai = now;
                buocThoai++;
                if (buocThoai == KICH_BAN.length) {
                    for (LopTruong b : cap) {
                        b.batDauDanhNhau();
                    }
                }
            }
        } else if (buocThoai >= KICH_BAN.length) {
            // Đang quần nhau: vừa đánh vừa chửi, nữ thần cổ vũ liên tục.
            if (now - lanChui >= NHIP_CHUI) {
                cap[luotChui % 2].chat(CHUI_LUC_DANH[Util.nextInt(0, CHUI_LUC_DANH.length - 1)]);
                luotChui++;
                lanChui = now;
            }
            if (nuThan != null && now - lanCoVu >= NHIP_CO_VU) {
                nuThan.chat(CO_VU[Util.nextInt(0, CO_VU.length - 1)]);
                lanCoVu = now;
            }
        }
        chaoNguoiMoi(zone);
    }

    /** Ai mới bước vào khu thì được boss hoặc nữ thần réo tên một câu. */
    private static void chaoNguoiMoi(Zone zone) {
        if (cap == null || zone == null) {
            return;
        }
        for (Player pl : zone.getPlayers()) {
            if (pl == null || !pl.isPl() || pl.isBot || DA_CHAO.contains(pl.id)) {
                continue;
            }
            DA_CHAO.add(pl.id);
            String ten = pl.name == null ? "bạn" : pl.name;
            if (nuThan != null && Util.isTrue(1, 2)) {
                nuThan.chat(String.format(NU_THAN_CHAO[Util.nextInt(0, NU_THAN_CHAO.length - 1)], ten));
            } else {
                cap[Util.nextInt(0, 1)].chat(
                        String.format(CHAO_NGUOI_CHOI[Util.nextInt(0, CHAO_NGUOI_CHOI.length - 1)], ten));
            }
        }
    }

    /**
     * Kéo hai boss về đứng hai bên nữ thần ở giữa map. Ba nhân vật cách nhau {@link #KHOANG_CACH}
     * để bong bóng thoại của cả ba cùng lọt vào màn hình người chơi.
     */
    private static void xepChoDung(Zone zone) {
        int[] tam = choDung(zone, 0);
        sanX = tam[0];
        sanY = tam[1];
        if (nuThan != null && nuThan.zone == zone) {
            int[] cho = choDung(zone, 0);
            nro.models.services.PlayerService.gI().playerMove(nuThan, cho[0], cho[1]);
        }
        int[] trai = choDung(zone, -KHOANG_CACH);
        int[] phai = choDung(zone, KHOANG_CACH);
        nro.models.services.PlayerService.gI().playerMove(cap[0], trai[0], trai[1]);
        nro.models.services.PlayerService.gI().playerMove(cap[1], phai[0], phai[1]);
        daXepCho = true;
    }

    private static boolean coNguoiChoi(Zone zone) {
        if (zone == null) {
            return false;
        }
        for (Player pl : zone.getPlayers()) {
            if (pl != null && pl.isPl() && !pl.isBot) {
                return true;
            }
        }
        return false;
    }

    private static void raCapMoi() {
        List<Integer> con = new ArrayList<>();
        for (int i = 0; i < SO_LUONG; i++) {
            if (TAT_CA[i] != null) {
                con.add(i);
            }
        }
        if (con.size() < 2) {
            return;
        }
        int a = con.remove(Util.nextInt(0, con.size() - 1));
        int b = con.remove(Util.nextInt(0, con.size() - 1));
        Zone zone = MapService.gI().getMapWithRandZone(MAP_JOIN[Util.nextInt(0, MAP_JOIN.length - 1)]);
        if (zone == null) {
            return;
        }
        cap = new LopTruong[]{TAT_CA[a], TAT_CA[b]};
        int dot = dotKeTiep;
        dotKeTiep = (dotKeTiep == DOT_2_TY ? DOT_20K : DOT_2_TY);
        buocThoai = -1;
        daVaoMap = false;
        daXepCho = false;
        lanChui = 0;
        lanCoVu = 0;
        luotChui = 0;
        DA_CHAO.clear();
        lanCoNguoi = System.currentTimeMillis();
        for (LopTruong boss : cap) {
            boss.doiThu = null;
            boss.zoneFinal = zone;
            boss.currentLevel = dot - 1;           // respawn() sẽ +1 thành đúng đợt
            boss.changeStatus(BossStatus.RESPAWN);
        }
        cap[0].doiThu = cap[1];
        cap[1].doiThu = cap[0];
        if (nuThan != null) {
            nuThan.raMap(zone);
        }
        Service.gI().sendThongBaoAllPlayer("Lốp Trưởng " + (a + 1) + " và Lốp Trưởng " + (b + 1)
                + " lại gặp nhau ở " + zone.map.mapName + (dot == DOT_20K ? " (bản yếu)" : ""));
    }

    /**
     * Màn kết thúc: con còn sống sỉ nhục hai câu, nữ thần nói một câu, mỗi câu cách nhau
     * {@link #NHIP_KET_THUC}. Xong xuôi mới cho cả ba biến mất — trước đây chat xong là
     * biến ngay nên người chơi không kịp thấy câu nào.
     */
    private static void chayManKetThuc(long now) {
        if (now - lanKetThucThoai < NHIP_KET_THUC) {
            return;
        }
        lanKetThucThoai = now;
        switch (buocKetThuc) {
            case 0 -> {
                if (conSong != null && !conSong.isDie()) {
                    conSong.chat(SI_NHUC[Util.nextInt(0, SI_NHUC.length - 1)]);
                }
            }
            case 1 -> {
                if (conSong != null && !conSong.isDie()) {
                    conSong.chat(SI_NHUC_THEM[Util.nextInt(0, SI_NHUC_THEM.length - 1)]);
                }
            }
            case 2 -> {
                if (nuThan != null) {
                    nuThan.chat(NU_THAN_CHIA_TAY[Util.nextInt(0, NU_THAN_CHIA_TAY.length - 1)]);
                }
            }
            default -> {
                ketThuc(null);
                return;
            }
        }
        buocKetThuc++;
    }

    private static void ketThuc(LopTruong song) {
        if (song != null) {
            song.chat(SI_NHUC[Util.nextInt(0, SI_NHUC.length - 1)]);
        }
        if (nuThan != null) {
            nuThan.veNha();
        }
        if (cap != null) {
            for (LopTruong boss : cap) {
                boss.doiThu = null;
                if (!boss.isDie() && boss.zone != null) {
                    boss.leaveMapNew();
                }
            }
        }
        cap = null;
        conSong = null;
        buocKetThuc = -1;
        buocThoai = -1;
        daVaoMap = false;
        daXepCho = false;
        DA_CHAO.clear();
        lanKetThuc = System.currentTimeMillis();
    }

    /** Toạ độ đứng: nữ thần giữa map, hai boss hai bên, cùng một mặt nền phía trên. */
    static int[] choDung(Zone zone, int lech) {
        int x = zone.map.mapWidth / 2 + lech;
        if (x < 60) {
            x = 60;
        } else if (x > zone.map.mapWidth - 60) {
            x = zone.map.mapWidth - 60;
        }
        return new int[]{x, zone.map.yPhysicInTop(x, 100)};
    }

    /** Vào map là đứng sẵn cạnh nữ thần, khỏi phải kéo về sau. */
    @Override
    public void joinMapByZone(Zone zone) {
        if (zone == null) {
            return;
        }
        this.zone = zone;
        int lech = (cap != null && cap.length == 2 && cap[1] == this) ? KHOANG_CACH : -KHOANG_CACH;
        int[] cho = choDung(zone, lech);
        nro.models.map.service.ChangeMapService.gI().changeMap(this, zone, cho[0], cho[1]);
    }

    //========================== hành vi từng con ==========================
    private LopTruong doiThu;
    private boolean dangDanhNhau;

    private void batDauDanhNhau() {
        this.dangDanhNhau = true;
    }

    /**
     * Chỉ đánh đối thủ, tuyệt đối không đánh người chơi.
     *
     * <p>Dùng y hệt cách đánh của lớp Boss gốc — {@code moveTo} bước 40–60 pixel, nhịp 100ms,
     * hồi chiêu do từng chiêu quyết định — nên hiệu ứng, animation và độ "nhúng nhúng" giống hệt
     * các boss khác. Bản trước tôi tự đặt toạ độ mỗi 0,6 giây nên boss nhảy giật một cái là
     * người chơi đánh hụt.
     */
    @Override
    public void attack() {
        if (!dangDanhNhau || doiThu == null || doiThu.isDie() || doiThu.zone != this.zone) {
            return;
        }
        if (!Util.canDoWithTime(this.lastTimeAttack, 100) || this.typePk != ConstPlayer.PK_ALL) {
            return;
        }
        this.lastTimeAttack = System.currentTimeMillis();
        try {
            this.playerSkill.skillSelect = chonChieu();
            int kc = Util.getDistance(this, doiThu);
            // Chỉ di chuyển khi CẦN áp sát, và không nhảy đổi chỗ vặt nữa.
            //
            // Server tính đòn đấm của người chơi là HỤT nếu lúc chạm đòn khoảng cách > 100
            // (SkillService: RANGE_ATTACK_CHIEU_DAM). Hai con này rượt nhau chứ không bám người
            // chơi, nên mỗi lần nhảy là người đang đánh bị hụt. Nay chúng bám dính nhau ở giữa
            // sân, gần như đứng yên một chỗ.
            if (kc > KHOANG_BAM) {
                this.moveToPlayer(doiThu);
                kc = Util.getDistance(this, doiThu);
            }
            if (kc <= getRangeCanAttackWithSkillSelect()) {
                nro.models.services.SkillService.gI().useSkill(this, doiThu, null, -1, null);
            }
        } catch (Exception e) {
        }
    }

    /**
     * Giữ boss trong sân đấu quanh chỗ xuất hiện.
     *
     * <p>Hai con này rượt nhau chứ không bám người chơi như boss thường, nên nếu thả tự do
     * chúng trôi dần khỏi chỗ người chơi đứng đánh và đòn của người chơi thành hụt liên tục.
     */
    private int trongSan(int x) {
        if (sanX <= 0) {
            return x;
        }
        if (x < sanX - BAN_KINH_SAN) {
            return sanX - BAN_KINH_SAN;
        }
        return x > sanX + BAN_KINH_SAN ? sanX + BAN_KINH_SAN : x;
    }

    @Override
    public void moveTo(int x, int y) {
        super.moveTo(trongSan(x), y);
        if (sanX > 0 && Math.abs(this.location.x - sanX) > BAN_KINH_SAN) {
            nro.models.services.PlayerService.gI().playerMove(this, trongSan(this.location.x), this.location.y);
        }
    }

    /**
     * Chọn chiêu có trọng số: đấm liên hoàn là chính (đánh gần, người chơi đứng đánh không bị
     * hụt), chưởng chỉ điểm xuyết cho đẹp mắt.
     */
    private nro.models.skill.Skill chonChieu() {
        int r = Util.nextInt(1, 100);
        int muon;
        if (r <= 70) {
            muon = Skill.LIEN_HOAN;            // 70% đấm liên hoàn
        } else if (r <= 85) {
            muon = Skill.LIEN_HOAN_CHUONG;     // 15%
        } else if (r <= 95) {
            muon = Skill.SUPER_KAME;           // 10%
        } else {
            muon = Skill.TAI_TAO_NANG_LUONG;   //  5%
        }
        for (nro.models.skill.Skill sk : this.playerSkill.skills) {
            if (sk != null && sk.template != null && sk.template.id == muon) {
                return sk;
            }
        }
        return this.playerSkill.skills.get(Util.nextInt(0, this.playerSkill.skills.size() - 1));
    }

    /**
     * Đòn của boss kia chỉ là diễn, không trừ máu. Ở đợt 20k, sát thương người chơi bị
     * chặn tối đa 100 để người cày chay cũng hạ được mà người mạnh không một phát là xong.
     */
    @Override
    public synchronized int injured(Player plAtt, long damage, boolean piercing, boolean isMobAttack) {
        if (plAtt instanceof Boss) {
            return 0;
        }
        if (this.currentLevel == DOT_20K && damage > tranDame()) {
            damage = tranDame();
        }
        return super.injured(plAtt, damage, piercing, isMobAttack);
    }

    /**
     * Bỏ hồi sinh mặc định của lớp Boss: bộ này chỉ ra map khi phần điều phối cho phép,
     * nếu không mỗi con sẽ tự ra sau secondsRest và thành 8 con cùng lúc.
     */
    @Override
    public void rest() {
    }

    /** Hết lượt thì về nghỉ, chờ điều phối cho ra lại, không tự nhảy sang đợt khác. */
    @Override
    public void leaveMap() {
        if (this.zone != null) {
            nro.models.map.service.ChangeMapService.gI().exitMap(this);
        }
        this.zoneFinal = null;
        this.dangDanhNhau = false;
        this.changeStatus(BossStatus.REST);
        this.lastTimeRest = System.currentTimeMillis();
    }

    @Override
    public void reward(Player plKill) {
        super.reward(plKill);
        if (plKill == null || this.zone == null) {
            return;
        }
        int x = this.location.x;
        int y = this.zone.map.yPhysicInTop(x, this.location.y - 24);
        if (this.currentLevel == DOT_20K) {
            roiDot20k(plKill, x, y);
        } else {
            roiDot2Ty(plKill, x, y);
        }
    }

    /** Đợt 2 tỷ: rơi như Super Black Goku (vàng + cơ hội đồ Thần Linh + trang bị). */
    private void roiDot2Ty(Player plKill, int x, int y) {
        int vangMin = Math.max(0, BossDropConfig.LT_VANG_MIN.giaTri);
        int vangMax = Math.max(vangMin, BossDropConfig.LT_VANG_MAX.giaTri);
        Service.gI().dropItemMap(this.zone,
                new ItemMap(this.zone, motTrong(BossDropConfig.LT_ID_VANG.ids()),
                        Util.nextInt(vangMin, vangMax), x, y, plKill.id));
        if (BossDropRate.rollDoThanLinh(this)) {
            ItemMap it = ItemService.gI().randDoTLBoss(this.zone, 1, x, y, plKill.id);
            if (it != null) {
                Service.gI().dropItemMap(this.zone, it);
            }
        }
        if (Util.isTrue(Math.max(0, BossDropConfig.LT_TRANG_BI.giaTri), 100)) {
            int[][] nhom = {
                {230, 231, 232, 234, 235, 236, 238, 239, 240, 242, 243, 244, 246, 247, 248, 250, 251, 252,
                 266, 267, 268, 270, 271, 272, 274, 275, 276},
                {254, 255, 256, 258, 259, 260, 262, 263, 264, 278, 279, 280}
            };
            int g = Util.nextInt(1, 100) <= 70 ? 0 : 1;
            int id = nhom[g][Util.nextInt(0, nhom[g].length - 1)];
            Service.gI().dropItemMap(this.zone, new ItemMap(this.zone, id, 1, x, y, plKill.id));
        }
    }

    /**
     * Đợt 20k: bảng rơi riêng cho người cày chay. Các mốc lấy từ cpanel; nếu tổng chưa tới 100
     * thì phần dư rơi vào ngọc rồng, tổng vượt 100 thì mấy mục cuối bị lép.
     */
    private void roiDot20k(Player plKill, int x, int y) {
        int binh = BossDropConfig.LT20_BINH.giaTri;
        int bua = binh + BossDropConfig.LT20_BUA.giaTri;
        int da = bua + BossDropConfig.LT20_DA_BAO_VE.giaTri;
        int sach = da + BossDropConfig.LT20_SACH_DE_TU.giaTri;
        int roll = Util.nextInt(1, 100);
        if (roll <= binh) {
            int[] ds = BossDropConfig.LT20_ID_BINH.ids();
            int min = Math.max(1, BossDropConfig.LT20_BINH_MIN.giaTri);
            int max = Math.max(min, BossDropConfig.LT20_BINH_MAX.giaTri);
            roi(plKill, ds[Util.nextInt(0, ds.length - 1)], Util.nextInt(min, max), x, y);
        } else if (roll <= bua) {
            roi(plKill, motTrong(BossDropConfig.LT20_ID_BUA.ids()), 1, x, y);
        } else if (roll <= da) {
            roi(plKill, motTrong(BossDropConfig.LT20_ID_DA.ids()), 1, x, y);
        } else if (roll <= sach) {
            roi(plKill, motTrong(BossDropConfig.LT20_ID_SACH.ids()), 1, x, y);
        } else {
            roi(plKill, motTrong(BossDropConfig.LT20_ID_NGOC.ids()), 1, x, y);
        }
    }

    private static int motTrong(int[] ds) {
        return ds.length == 0 ? 190 : ds[Util.nextInt(0, ds.length - 1)];
    }

    private void roi(Player plKill, int tempId, int soLuong, int x, int y) {
        try {
            Service.gI().dropItemMap(this.zone, new ItemMap(this.zone, tempId, soLuong, x, y, plKill.id));
        } catch (Exception e) {
            nro.models.utils.Logger.error("Lốp Trưởng — không rơi được vật phẩm " + tempId + ": " + e + "\n");
        }
    }
}
