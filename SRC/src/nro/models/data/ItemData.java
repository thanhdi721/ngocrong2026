package nro.models.data;

import nro.models.player_system.Template.ArrHead2Frames;
import nro.models.player_system.Template.ItemOptionTemplate;
import nro.models.server.Manager;
import nro.models.network.Message;
import nro.models.network.MySession;
import nro.models.player_system.Template;

public class ItemData {

    public static void updateItem(MySession session) {
        updateItemOptionItemplate(session);
        updateItemArrHead2FTemplate(session);
        updateItemTemplate(session, 750);
        updateItemTemplate(session, 750, Manager.ITEM_TEMPLATES.size());
    }

    private static void updateItemOptionItemplate(MySession session) {
        Message msg;
        try {
            msg = new Message(-28);
            msg.writer().writeByte(8);
            msg.writer().writeByte(DataGame.vsItem); //vcitem
            msg.writer().writeByte(0); //update option
            msg.writer().writeByte(Manager.ITEM_OPTION_TEMPLATES.size());
            for (ItemOptionTemplate io : Manager.ITEM_OPTION_TEMPLATES) {
                msg.writer().writeUTF(io.name);
                msg.writer().writeByte(io.type);
            }
            session.doSendMessage(msg);
            msg.cleanup();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /** Gói "nạp lại" 750 vật phẩm đầu — đã đo ~35 KB, vẫn dưới trần 2 byte. */
    private static void updateItemTemplate(MySession session, int count) {
        Message msg;
        try {
            msg = new Message(-28);
            msg.writer().writeByte(8);

            msg.writer().writeByte(DataGame.vsItem); //vcitem
            msg.writer().writeByte(1); //reload itemtemplate
            msg.writer().writeShort(count);
            for (int i = 0; i < count; i++) {
                Template.ItemTemplate itemTemplate = Manager.ITEM_TEMPLATES.get(i);
                msg.writer().writeByte(itemTemplate.type);
                msg.writer().writeByte(itemTemplate.gender);
                msg.writer().writeUTF(itemTemplate.name);
                msg.writer().writeUTF(itemTemplate.description);
                msg.writer().writeByte(itemTemplate.level);
                msg.writer().writeInt(itemTemplate.strRequire);
                msg.writer().writeShort(itemTemplate.iconID);
                msg.writer().writeShort(itemTemplate.part);
                msg.writer().writeBoolean(itemTemplate.isUpToUp);
            }
            session.doSendMessage(msg);
            msg.cleanup();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * Độ dài gói tin lệnh -28 chỉ được ghi bằng 2 BYTE (xem MessageSendCollect.doSendMessage:
     * chỉ các lệnh -32, -66, -74, 11, -67, -87, 66 mới dùng 3 byte). Gói quá 65.535 byte sẽ bị
     * ghi sai độ dài -> client đọc lệch cả luồng và đứng ở màn "Xin chờ".
     *
     * <p>Với dữ liệu gốc, gói "thêm vật phẩm" đã nặng ~62 KB, tức gần chạm trần. Thêm vài chục
     * vật phẩm nữa là tràn. Nay gói được CẮT thành nhiều phần, mỗi phần tối đa MAX_PACKET_BYTES.
     */
    private static final int MAX_PACKET_BYTES = 45_000;

    /** Số byte một vật phẩm chiếm trong gói tin (tính cả 2 byte độ dài của mỗi chuỗi UTF). */
    private static int sizeOf(Template.ItemTemplate t) {
        int nameLen = t.name == null ? 0 : t.name.getBytes(java.nio.charset.StandardCharsets.UTF_8).length;
        int descLen = t.description == null ? 0 : t.description.getBytes(java.nio.charset.StandardCharsets.UTF_8).length;
        return 1 + 1 + (2 + nameLen) + (2 + descLen) + 1 + 4 + 2 + 2 + 1;
    }

    private static void updateItemTemplate(MySession session, int start, int end) {
        int i = start;
        while (i < end) {
            int size = 8;
            int j = i;
            while (j < end) {
                int s = sizeOf(Manager.ITEM_TEMPLATES.get(j));
                if (size + s > MAX_PACKET_BYTES && j > i) {
                    break;
                }
                size += s;
                j++;
            }
            sendItemTemplateChunk(session, i, j);
            i = j;
        }
    }

    private static void sendItemTemplateChunk(MySession session, int start, int end) {
        Message msg;
        try {
            msg = new Message(-28);
            msg.writer().writeByte(8);

            msg.writer().writeByte(DataGame.vsItem); //vcitem
            msg.writer().writeByte(2); //add itemtemplate
            msg.writer().writeShort(start);
            msg.writer().writeShort(end);
            for (int i = start; i < end; i++) {
                msg.writer().writeByte(Manager.ITEM_TEMPLATES.get(i).type);
                msg.writer().writeByte(Manager.ITEM_TEMPLATES.get(i).gender);
                msg.writer().writeUTF(Manager.ITEM_TEMPLATES.get(i).name);
                msg.writer().writeUTF(Manager.ITEM_TEMPLATES.get(i).description);
                msg.writer().writeByte(Manager.ITEM_TEMPLATES.get(i).level);
                msg.writer().writeInt(Manager.ITEM_TEMPLATES.get(i).strRequire);
                msg.writer().writeShort(Manager.ITEM_TEMPLATES.get(i).iconID);
                msg.writer().writeShort(Manager.ITEM_TEMPLATES.get(i).part);
                msg.writer().writeBoolean(Manager.ITEM_TEMPLATES.get(i).isUpToUp);
            }
            session.doSendMessage(msg);
            msg.cleanup();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private static void updateItemArrHead2FTemplate(MySession session) {
        Message msg;
        try {
            msg = new Message(-28);
            msg.writer().writeByte(8);
            msg.writer().writeByte(DataGame.vsItem); //vcitem
            msg.writer().writeByte(100);
            msg.writer().writeShort(Manager.ARR_HEAD_2_FRAMES.size());
            for (ArrHead2Frames io : Manager.ARR_HEAD_2_FRAMES) {
                msg.writer().writeByte(io.frames.size());
                for (int i : io.frames) {
                    msg.writer().writeShort(i);
                }
            }
            session.doSendMessage(msg);
            msg.cleanup();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
