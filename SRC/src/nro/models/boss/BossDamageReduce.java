package nro.models.boss;

/**
 * BẢNG % GIẢM SÁT THƯƠNG NHẬN VÀO CỦA BOSS — gom một chỗ để chỉnh cân bằng.
 *
 * <p>
 * Lý do tồn tại: máu boss là {@code int} ({@code BossData.hp} khai báo
 * {@code new int[]{...}}, {@code NPoint.hp}/{@code NPoint.hpg} cũng là {@code int})
 * nên <b>trần máu tuyệt đối là 2.147.483.647 cho mỗi hình dạng</b>. Boss kết truyện
 * Heart đã chạm trần ở cả 3 hình dạng cuối. Muốn trận đánh dài hơn thì không còn
 * cách tăng máu — phải giảm sát thương nhận vào. Đây đúng là khuyến nghị của
 * {@code docs/3-kiem-tra-can-bang/21c-boss-roi-do-can-bang.md} §6.2:
 * <i>"Muốn boss dai hơn thì tăng hệ số giảm sát thương trong injured, không tăng HP."</i>
 *
 * <p>
 * <b>Cách đọc mỗi bảng:</b> mảng {@code int[]} chạy theo {@code Boss.currentLevel}
 * (hình dạng). Phần tử {@code i} là % sát thương bị chặn ở hình dạng {@code i}.
 * Mảng ngắn hơn số hình dạng thì phần tử cuối được dùng cho các hình dạng còn lại.
 * {@code null} hoặc mảng rỗng = không giảm gì.
 *
 * <p>
 * <b>Máu hiệu dụng = máu danh nghĩa ÷ (1 − %/100)</b>. Bảng đầy đủ kèm thời gian
 * hạ boss ước tính: {@code docs/4-trien-khai/33-giam-sat-thuong-boss.md}.
 *
 * <p>
 * <b>Công tắc:</b> {@link #WORLD_BOSS_ENABLED} bật/tắt riêng phần boss thế giới
 * (phần duy nhất đụng tới người chơi đang cày). Đặt {@code false} là mọi boss thế
 * giới quay về đúng hành vi cũ, không cần sửa chỗ nào khác.
 */
public final class BossDamageReduce {

    private BossDamageReduce() {
    }

    // =====================================================================
    // CÔNG TẮC
    // =====================================================================
    /**
     * Bật/tắt toàn bộ phần giảm sát thương của <b>boss thế giới</b>
     * (Black Goku -203, Cumber -203999, Baby -925, Cooler -29).
     *
     * <p>
     * {@code false} → các bảng {@code *_TG} bên dưới trả về {@code null}, boss thế
     * giới nhận sát thương y như trước khi có tính năng này. Boss nhiệm vụ và Heart
     * <b>không</b> chịu ảnh hưởng của công tắc này.
     */
    public static final boolean WORLD_BOSS_ENABLED = true;

    /**
     * Trần % được phép. Chặn ở 90% để không bao giờ tạo ra boss gần như bất tử vì
     * gõ nhầm số (ví dụ 100 hay 250).
     */
    public static final int MAX_PERCENT = 90;

    // =====================================================================
    // BOSS CỐT TRUYỆN — dải -2000…-2001 (chương 1–2)
    // Người chơi mới, sức mạnh còn thấp: KHÔNG giảm gì.
    // =====================================================================
    /** Kẻ Thu Gom (-2000), NV 6. */
    public static final int[] KE_THU_GOM = {0};

    /** Jaco Vô Thức (-2001), NV 15 — 2 hình dạng. */
    public static final int[] JACO_VO_THUC = {0, 0};

    // =====================================================================
    // BOSS BẢN NHIỆM VỤ — dải -2100…-2105 (chương 4–6)
    // Tăng dần theo chương. Máu các con này đã được hạ xuống "thang cốt truyện"
    // (21c §6.2.a) nên % ở đây chỉ để kéo trận đánh về vùng mục tiêu 3–7 phút solo.
    // =====================================================================
    /** Xên bọ hung bản nhiệm vụ (-2100), NV 30 — 3 hình dạng. */
    public static final int[] XEN_BO_HUNG_NV = {8, 10, 12};

    /** Cooler bản nhiệm vụ (-2101), NV 34 — 2 hình dạng. */
    public static final int[] COOLER_NV = {10, 15};

    /** Mabư bản nhiệm vụ (-2102), NV 37 — 5 hình dạng. */
    public static final int[] MABU_NV = {12, 14, 16, 18, 20};

    /** Black Goku bản nhiệm vụ (-2103), NV 38 — 2 hình dạng. */
    public static final int[] BLACK_GOKU_NV = {15, 20};

    /** Baby bản nhiệm vụ (-2104), NV 39 — 3 hình dạng. */
    public static final int[] BABY_NV = {18, 22, 25};

    /** Cumber bản nhiệm vụ (-2105), NV 42 — 2 hình dạng. */
    public static final int[] CUMBER_NV = {28, 32};

    // =====================================================================
    // HEART (-108108) — boss kết truyện
    // 4 hình dạng: 1,5 tỉ + 2 tỉ + 2 tỉ + 2 tỉ = 7,5 tỉ danh nghĩa (đã chạm trần int).
    // % tăng dần → máu hiệu dụng ≈ 14,67 tỉ, gấp gần đúng 2 lần con số danh nghĩa.
    // =====================================================================
    public static final int[] HEART = {20, 35, 50, 65};

    // =====================================================================
    // BOSS THẾ GIỚI — phần DUY NHẤT đụng tới người chơi đang cày.
    // Chịu công tắc WORLD_BOSS_ENABLED.
    //
    // Lưu ý quan trọng: ba con dưới đây ĐÃ có sẵn cơ chế chia sát thương trong
    // injured của riêng chúng. % ở đây CỘNG THÊM lên trên cơ chế cũ, không thay thế:
    //   - Black Goku / Cumber hình dạng 2: damage /= 2  (tương đương 50%)
    //   - Baby mọi hình dạng:              damage *0,7 /2 (tương đương ~65%)
    // =====================================================================
    /**
     * Super Black Goku (-203) — 500 triệu + 2 tỉ (hình dạng 2 chạm trần).
     * Hình dạng 1 chưa có cơ chế giảm nào → 30%. Hình dạng 2 đã có sẵn 50% → cộng
     * thêm 25% thành 62,5% tổng.
     */
    public static final int[] BLACK_GOKU_TG = world(new int[]{30, 25});

    /** Cumber (-203999) — chỉ số và cơ chế y hệt Black Goku. */
    public static final int[] CUMBER_TG = world(new int[]{30, 25});

    /**
     * Baby (-925) — 2 tỉ × 3 hình dạng, chạm trần cả ba.
     *
     * <p>
     * <b>Cố ý để 0.</b> Baby đã là boss khó nhất game theo 21c §2.2 (17,1 tỉ máu
     * hiệu dụng, 5.270 "người-phút", gấp 4,3 lần Black Goku) vì {@code injured} của
     * nó nhân sát thương với 0,7 rồi chia 2 — tức <b>đã giảm sẵn ~65%</b>, cao hơn
     * cả mức 25–40% được đề nghị. Cộng thêm nữa là biến Baby thành không thể hạ.
     * Muốn bật thì đổi mảng dưới đây thành {@code {25, 30, 35}}.
     */
    public static final int[] BABY_TG = world(new int[]{0, 0, 0});

    /**
     * Cooler (-29) — 200 triệu + 500 triệu, <b>không</b> chạm trần.
     *
     * <p>
     * <b>Cố ý để 0.</b> 21c §5.2 ② xếp Cooler là boss "thưởng quá hậu so với độ khó"
     * nặng nhất trong nhóm rơi đồ Thần Linh, nhưng cách sửa mà 21c §6.3 đề xuất là
     * <b>hạ phần thưởng</b>, không phải tăng độ khó; tăng độ khó Cooler chỉ nới rộng
     * thêm khoảng cách với Black Goku chứ không khép lại. Đã nối sẵn dây, muốn bật
     * thì đổi mảng thành {@code {25, 35}} (máu hiệu dụng ≈ 1,05 tỉ).
     */
    public static final int[] COOLER_TG = world(new int[]{0, 0});

    /**
     * Trả về bảng nếu công tắc boss thế giới đang bật, ngược lại trả {@code null}
     * (= không giảm gì).
     */
    private static int[] world(int[] table) {
        return WORLD_BOSS_ENABLED ? table : null;
    }
}
