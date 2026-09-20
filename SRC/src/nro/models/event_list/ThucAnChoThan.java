package nro.models.event_list;

import nro.models.event.Event;

/**
 * Sự kiện "Thức ăn cho thần": quái ở mọi map rơi Tayaki (1798), Kẹo táo (1799),
 * Kem que đôi (1800), Mochi (1801), Ramen (1802) và Khúc mía (1612).
 *
 * Không có NPC hay boss riêng — chỉ là cờ bật/tắt tỉ lệ rơi, được kiểm tra trong
 * {@code Mob} qua {@code EventManager.THUC_AN_CHO_THAN}. Trước đây các món này rơi
 * ở mọi map mà không gắn với sự kiện nào nên không tắt được.
 */
public class ThucAnChoThan extends Event {

    @Override
    public void npc() {
    }

}
