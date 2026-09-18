package nro.models.player_badges;

import nro.models.player.Player;

/**
 *
 * @author By Mr Blue
 * 
 */

public class BadgesData {

    public int idBadGes; // id danh hiệu
    public long timeofUseBadges; // hạn sử dụng danh hiệu
    public boolean isUse;

    public BadgesData() {
        idBadGes = -1;
        timeofUseBadges = -1;
        isUse = false;
    }

    public BadgesData(int id, long time, boolean isuse) {
        idBadGes = id;
        timeofUseBadges = time;
        isUse = isuse;
    }

    public BadgesData(Player player, int id, int days) {
        idBadGes = id;
        // FIX (rà soát 37): days * 24 * 60 * 60 là phép nhân KIỂU INT.
        // Với days = 36500 (danh hiệu "vĩnh viễn" của NV 47 / NV 50) kết quả là
        // 3.153.600.000 > Integer.MAX_VALUE -> tràn số âm -> hạn dùng rơi về năm 1934,
        // danh hiệu hết hạn ngay khi vừa được trao. Ép long ngay từ thừa số đầu.
        timeofUseBadges = System.currentTimeMillis() + (long) days * 24L * 60L * 60L * 1000L;
        if (player.dataBadges != null) {
            for (BadgesData data2 : player.dataBadges) {
                data2.isUse = false;
            }
        }
        isUse = true;
        player.dataBadges.add(this);
    }

    @Override
    public String toString() {
        final String n = "\"";
        return "{" + n + "idBadGes" + n + ":" + n + idBadGes + n + "," + n + "timeofUseBadges" + n + ":" + n + timeofUseBadges + n + "," + n + "isUse" + n + ":" + n + isUse + n + "}";
    }
}
