package nro.models.npc;
import java.io.IOException;
import nro.models.player.Player;
import nro.models.network.Message;

public class BaseMenu {

    public int npcId;

    public String npcSay;

    public String[] menuSelect;

    public void openMenu(Player player) {
        Message msg;
        try {
            msg = new Message(32);
            msg.writer().writeShort(npcId);
            if (player.idMark != null) { player.idMark.setMenuNpcId(npcId); } // FIX: nhớ NPC mở menu
            msg.writer().writeUTF(npcSay);
            msg.writer().writeByte(menuSelect.length);
            for (String menu : menuSelect) {
                msg.writer().writeUTF(menu);
            }
            player.sendMessage(msg);
            msg.cleanup();
        } catch (IOException e) {
        }
    }
}
