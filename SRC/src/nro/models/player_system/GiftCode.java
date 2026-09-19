package nro.models.player_system;

import nro.models.item.Item.ItemOption;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.HashMap;
import nro.models.player.Player;

/**
 *
 * @author By Mr Blue
 * 
 */

public class GiftCode {

    public String code;
    public int countLeft;
    public int id;
    public HashMap<Integer, Integer> detail = new HashMap<>();
    public HashMap<Integer, ArrayList<ItemOption>> option = new HashMap<>();
    public Timestamp datecreate;
    public Timestamp dateexpired;

    public boolean isUsedGiftCode(Player player) {
        return player.giftCode.isUsedGiftCode(code);
    }

    /**
     * Code đã hết hạn chưa.
     * FIX: trước đây so "ngày tạo > ngày hết hạn" — không liên quan gì tới giờ hiện tại, nên
     * code đặt hạn đúng cách (hạn sau ngày tạo) KHÔNG BAO GIỜ hết hạn. Nay so với giờ hiện tại.
     * (Cột datecreate trong DB còn có ON UPDATE current_timestamp() nên không dùng làm mốc được.)
     */
    public boolean timeCode() {
        return this.dateexpired != null && System.currentTimeMillis() > this.dateexpired.getTime();
    }
}
