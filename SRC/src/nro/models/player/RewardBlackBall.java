package nro.models.player;

import nro.models.services.Service;
import nro.models.utils.TimeUtil;

/**
 *
 * @author By Mr Blue
 *
 */

public class RewardBlackBall {

    /** Buff sống 22 giờ kể từ lúc thắng trận ngọc rồng sao đen. */
    private static final int TIME_REWARD = 79200000;

    // ======================================================================
    // MỨC BUFF CỦA TỪNG VIÊN NGỌC SAO ĐEN
    // ----------------------------------------------------------------------
    // Cộng cho NGƯỜI THẮNG VÀ CẢ BANG ngay lúc thắng, tự hết sau 22 giờ.
    // Chỗ cộng nằm trong NPoint (xem chú thích "ngọc rồng đen N sao").
    //
    // Mô tả trong `item_template` (vật phẩm 372-378) PHẢI khớp đúng mấy số này.
    // Sửa số ở đây thì chạy lại patch 92 với số mới, đừng để hai bên lệch nhau —
    // bản cũ code cộng 21/35/35/35/35/40/14 trong khi mô tả lại hứa phát đậu thần,
    // bùa, ngọc nâng cấp và vàng theo giờ (không hề có dòng code nào làm).
    //
    //   1 sao  +15% sát thương           -> NPoint.getDameAttack
    //   2 sao  +30% HP tối đa            -> NPoint (hpMax)
    //   3 sao  +35  hút HP               -> NPoint.setPointWhenWearClothes (tlHutHp)
    //   4 sao  +35  phản sát thương      -> NPoint.setPointWhenWearClothes (tlPST)
    //   5 sao  +20  sát thương chí mạng  -> NPoint.setPointWhenWearClothes (tlDameCrit + tlSDCM)
    //   6 sao  +30% KI tối đa            -> NPoint (mpMax)
    //   7 sao  +14  né đòn               -> NPoint.setPointWhenWearClothes (tlNeDon)
    // ======================================================================
    public static final int R1S_SAT_THUONG = 15;
    public static final int R2S_HP = 30;
    public static final int R3S_HUT_HP = 35;
    public static final int R4S_PHAN_SAT_THUONG = 35;
    public static final int R5S_CHI_MANG = 20;
    public static final int R6S_KI = 30;
    public static final int R7S_NE_DON = 14;

    public static long time8h;
    private Player player;

    public long[] timeOutOfDateReward;
    public int[] quantilyBlackBall;
    /**
     * Không còn dùng để tính gì, nhưng VẪN PHẢI GIỮ: PlayerDAO ghi nó vào ô giữa
     * của mỗi dòng `data_black_ball` và MrBlue đọc lại đúng ô đó. Bỏ đi là lệch
     * định dạng lưu của mọi nhân vật cũ.
     */
    public long[] lastTimeGetReward;

    public RewardBlackBall(Player player) {
        this.player = player;
        this.timeOutOfDateReward = new long[7];
        this.lastTimeGetReward = new long[7];
        this.quantilyBlackBall = new int[7];
        time8h = TimeUtil.getStartTimeBlackBallWar();
    }

    public void reward(byte star) {
        if (this.timeOutOfDateReward[star - 1] > time8h) {
            quantilyBlackBall[star - 1]++;
        }
        this.timeOutOfDateReward[star - 1] = System.currentTimeMillis() + TIME_REWARD;
        Service.gI().point(player);
    }

    // ĐÃ BỎ getReward / getRewardSelect: nút "Nhận thưởng" ở NPC Rồng Omega chỉ in
    // câu "Chỉ Số Tự Cộng Khi Nhặt xong" rồi thôi, không phát gì cả — người chơi cứ
    // tưởng còn phần thưởng chưa lấy. Buff vốn đã tự cộng ngay lúc thắng trong reward().

    public void dispose() {
        this.player = null;
    }
}
