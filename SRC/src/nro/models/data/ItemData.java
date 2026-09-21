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
        int cut = splitIndex();
        updateItemTemplate(session, cut);
        sendItemTemplateChunk(session, cut, Manager.ITEM_TEMPLATES.size());
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
     * <p>Client LƯU CACHE mỗi loại gói đúng MỘT bản (NRitem1 = gói "nạp lại", NRitem2 = gói
     * "thêm vật phẩm"). Bản trước cắt gói "thêm" thành nhiều khúc -> cache chỉ giữ khúc cuối,
     * lần đăng nhập sau (cùng vsItem) client nạp cache thiếu cả trăm vật phẩm -> lỗi hình / xin
     * icon rác. Nay luôn gửi đúng 1 gói "nạp lại" + 1 gói "thêm", chỉ dời điểm chia sao cho
     * hai gói nặng xấp xỉ nhau (mỗi gói ~56 KB với ~2.075 vật phẩm).
     */
    private static final int MAX_PACKET_BYTES = 65_000;

    /** Số byte một vật phẩm chiếm trong gói tin (tính cả 2 byte độ dài của mỗi chuỗi UTF). */
    private static int sizeOf(Template.ItemTemplate t) {
        int nameLen = t.name == null ? 0 : t.name.getBytes(java.nio.charset.StandardCharsets.UTF_8).length;
        int descLen = t.description == null ? 0 : t.description.getBytes(java.nio.charset.StandardCharsets.UTF_8).length;
        return 1 + 1 + (2 + nameLen) + (2 + descLen) + 1 + 4 + 2 + 2 + 1;
    }

    private static volatile int cachedSplit = -1;
    private static volatile int cachedSplitSize = -1;

    /** Điểm chia giữa gói "nạp lại" [0, cut) và gói "thêm" [cut, size) để hai gói cân nhau. */
    private static int splitIndex() {
        int n = Manager.ITEM_TEMPLATES.size();
        if (cachedSplit >= 0 && cachedSplitSize == n) {
            return cachedSplit;
        }
        int total = 0;
        for (int i = 0; i < n; i++) {
            total += sizeOf(Manager.ITEM_TEMPLATES.get(i));
        }
        int acc = 0;
        int cut = 0;
        while (cut < n && acc + sizeOf(Manager.ITEM_TEMPLATES.get(cut)) <= total / 2) {
            acc += sizeOf(Manager.ITEM_TEMPLATES.get(cut));
            cut++;
        }
        int first = 8 + acc;
        int second = 8 + total - acc;
        if (first > MAX_PACKET_BYTES || second > MAX_PACKET_BYTES) {
            nro.models.utils.Logger.error("Dữ liệu vật phẩm quá lớn cho 2 gói tin (" + first + " + " + second
                    + " byte, trần mỗi gói " + MAX_PACKET_BYTES + "): rút gọn mô tả vật phẩm!\n");
        }
        cachedSplit = cut;
        cachedSplitSize = n;
        return cut;
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
