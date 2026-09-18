package nro.models.boss;

import nro.models.utils.Util;

/**
 * TỈ LỆ RƠI ĐỒ THẦN LINH — tính theo SỨC MẠNH THẬT của boss, gom về một chỗ.
 *
 * <p>
 * <b>Vấn đề trước đây:</b> 14 chỗ gọi {@code ItemService.randDoTLBoss(...)} đều bọc
 * trong một {@code Util.isTrue(X, 100)} với số cứng tuỳ tiện, không liên quan gì tới
 * độ khó thật: Baby 10%, Cooler 5%, Cumber 5%, Black Goku 5%, Siêu Bọ Hung 5%, còn cả
 * chuỗi Mabư 12h thì 1% bất kể con dai hay con giấy. Hậu quả là Cooler (máu hiệu dụng
 * 200 triệu) rơi đồ ngang Black Goku hình dạng 2 (máu hiệu dụng hơn 5 tỉ), và Baby —
 * con khó nhất game — lại rơi gấp đôi mọi con khác.
 *
 * <p>
 * <b>Cách làm mới:</b> tỉ lệ được suy ra từ chính chỉ số boss tại thời điểm chết, nên
 * chỉnh máu boss hay chỉnh {@link BossDamageReduce} là tỉ lệ tự đổi theo — <b>không có
 * bảng tra id cứng ở đây</b>.
 *
 * <pre>
 *   máu hiệu dụng = máu danh nghĩa của hình dạng đang đánh ÷ (1 − tổng % giảm sát thương)
 * </pre>
 *
 * Tổng % giảm gộp <b>hai lớp</b>, nhân tiếp nhau đúng như thứ tự trong {@code injured()}:
 * <ul>
 * <li>Lớp cũ viết cứng trong {@code injured()} của từng boss ({@code damage/2},
 * {@code ×0,7/2}, {@code damage/3}…) — khai báo qua
 * {@link Boss#getLegacyDamageReducePercent()}.</li>
 * <li>Lớp mới theo bảng {@link BossDamageReduce} — qua
 * {@link Boss#getDamageReducePercent()}.</li>
 * </ul>
 *
 * <p>
 * <b>Thang bậc</b> (xem docs/4-trien-khai/35-ti-le-roi-do.md để biết vì sao chọn các mốc này):
 * <table border="1">
 * <caption>Máu hiệu dụng → tỉ lệ</caption>
 * <tr><th>Máu hiệu dụng</th><th>Tỉ lệ</th></tr>
 * <tr><td>&lt; 50 triệu</td><td>1%</td></tr>
 * <tr><td>50 – 200 triệu</td><td>2%</td></tr>
 * <tr><td>200 – 700 triệu</td><td>3%</td></tr>
 * <tr><td>700 triệu – 2 tỉ</td><td>4%</td></tr>
 * <tr><td>&ge; 2 tỉ</td><td>5%</td></tr>
 * </table>
 *
 * <p>
 * Kết quả <b>luôn</b> bị kẹp trong {@link #MIN_PERCENT}…{@link #MAX_PERCENT} (1–5%), kể
 * cả khi ai đó nhập máu âm, máu 0, hay % giảm ≥ 100.
 *
 * <p>
 * <b>Phạm vi:</b> chỉ dùng cho boss thế giới / boss sự kiện có rơi đồ Thần Linh. Boss bản
 * nhiệm vụ ({@code boss/quest/**}) và Heart ({@code boss/heart/**}) <b>không</b> rơi đồ
 * Thần Linh nên không gọi lớp này.
 */
public final class BossDropRate {

    private BossDropRate() {
    }

    // =====================================================================
    // GIỚI HẠN
    // =====================================================================
    /** Sàn tuyệt đối: boss dễ nhất vẫn có 1% cơ hội rơi. */
    public static final int MIN_PERCENT = 1;

    /** Trần tuyệt đối: boss khó nhất cũng chỉ 5%. */
    public static final int MAX_PERCENT = 5;

    // =====================================================================
    // MỐC MÁU HIỆU DỤNG — đơn vị: máu
    // =====================================================================
    /** Từ mốc này trở lên: 2%. */
    public static final long TIER_2_HP = 50_000_000L;
    /** Từ mốc này trở lên: 3%. */
    public static final long TIER_3_HP = 200_000_000L;
    /** Từ mốc này trở lên: 4%. */
    public static final long TIER_4_HP = 700_000_000L;
    /** Từ mốc này trở lên: 5%. */
    public static final long TIER_5_HP = 2_000_000_000L;

    /** Máu dùng tạm khi không đọc được chỉ số boss (hỏng dữ liệu) → rơi vào bậc 1%. */
    private static final long HP_FALLBACK = 0L;

    // =====================================================================
    // API
    // =====================================================================
    /**
     * Máu danh nghĩa của hình dạng boss đang bị đánh.
     *
     * <p>
     * Ưu tiên {@code nPoint.hpg} vì đó là con số đã được bốc ra thật cho lượt này
     * ({@code Boss.initBase} bốc ngẫu nhiên trong mảng {@code BossData.hp}). Khi
     * {@code nPoint} chưa khởi tạo thì quay về đọc thẳng {@code BossData} của hình dạng
     * hiện tại, lấy phần tử lớn nhất cho chắc.
     */
    public static long nominalHp(Boss boss) {
        if (boss == null) {
            return HP_FALLBACK;
        }
        if (boss.nPoint != null && boss.nPoint.hpg > 0) {
            return boss.nPoint.hpg;
        }
        BossData[] data = boss.data;
        if (data == null || data.length == 0) {
            return HP_FALLBACK;
        }
        int level = boss.currentLevel;
        if (level < 0) {
            level = 0;
        } else if (level >= data.length) {
            level = data.length - 1;
        }
        int[] hps = data[level].getHp();
        if (hps == null || hps.length == 0) {
            return HP_FALLBACK;
        }
        long max = 0L;
        for (int hp : hps) {
            if (hp > max) {
                max = hp;
            }
        }
        return max;
    }

    /**
     * Tổng % sát thương bị chặn của hình dạng hiện tại, gộp lớp cũ và lớp mới.
     *
     * <p>
     * Hai lớp nhân tiếp nhau chứ không cộng: còn lại = (1−a)(1−b). Ví dụ Black Goku
     * hình dạng 2 có lớp cũ {@code damage/=2} (50%) và bảng {@code BLACK_GOKU_TG[1]=25}
     * → còn lại 0,5 × 0,75 = 0,375 → tổng chặn 62%.
     *
     * @return 0…99 (kẹp ở 99 để phép chia ở {@link #effectiveHp(Boss)} không nổ)
     */
    public static int totalDamageReducePercent(Boss boss) {
        if (boss == null) {
            return 0;
        }
        int legacy = clampPercent(boss.getLegacyDamageReducePercent());
        int table = clampPercent(boss.getDamageReducePercent());
        // keep tính trên thang 10.000 để không mất số lẻ khi chia.
        long keep = (100L - legacy) * (100L - table);
        int total = (int) (100L - keep / 100L);
        return clampPercent(total);
    }

    /** Kẹp một % về khoảng 0…99. */
    private static int clampPercent(int percent) {
        if (percent < 0) {
            return 0;
        }
        return percent > 99 ? 99 : percent;
    }

    /**
     * Máu hiệu dụng = máu danh nghĩa ÷ (1 − tổng % giảm).
     *
     * <p>
     * Tính bằng {@code long} và nhân trước chia sau. Máu danh nghĩa trần là 2,147 tỉ,
     * nhân 100 vẫn chỉ ~2,1×10^11 nên còn xa {@code Long.MAX_VALUE}.
     */
    public static long effectiveHp(Boss boss) {
        long hp = nominalHp(boss);
        if (hp <= 0L) {
            return 0L;
        }
        int keep = 100 - totalDamageReducePercent(boss);
        if (keep <= 0) {
            keep = 1;
        }
        return hp * 100L / keep;
    }

    /** Thang bậc thuần tuý: máu hiệu dụng → tỉ lệ %. Tách riêng để kiểm thử dễ. */
    public static int percentForEffectiveHp(long effectiveHp) {
        if (effectiveHp >= TIER_5_HP) {
            return 5;
        }
        if (effectiveHp >= TIER_4_HP) {
            return 4;
        }
        if (effectiveHp >= TIER_3_HP) {
            return 3;
        }
        if (effectiveHp >= TIER_2_HP) {
            return 2;
        }
        return MIN_PERCENT;
    }

    /**
     * Tỉ lệ rơi đồ Thần Linh của boss này, tính theo hình dạng đang bị đánh.
     *
     * @return số nguyên trong khoảng {@link #MIN_PERCENT}…{@link #MAX_PERCENT}
     */
    public static int percentFor(Boss boss) {
        int percent = percentForEffectiveHp(effectiveHp(boss));
        if (percent < MIN_PERCENT) {
            return MIN_PERCENT;
        }
        return percent > MAX_PERCENT ? MAX_PERCENT : percent;
    }

    /**
     * Gieo xúc xắc một lần: có rơi đồ Thần Linh hay không.
     *
     * <p>
     * Đây là hàm duy nhất mà các lớp boss cần gọi, thay cho
     * {@code Util.isTrue(<số cứng>, 100)} trước đây.
     */
    public static boolean rollDoThanLinh(Boss boss) {
        return Util.isTrue(percentFor(boss), 100);
    }
}
