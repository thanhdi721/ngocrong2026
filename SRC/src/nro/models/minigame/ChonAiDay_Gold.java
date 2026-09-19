package nro.models.minigame;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import nro.models.player.Player;
import nro.models.services.ChatGlobalService;
import nro.models.services.Service;
import nro.models.utils.Util;

/**
 *
 * @author Mr Blue
 */
public class ChonAiDay_Gold implements Runnable {

    public long goldNormar; // FIX (46): long, tránh tràn quỹ
    public long goldVip; // FIX (46): long
    public long lastTimeEnd;
    public static final int TIME_CHONAIDAY = 300000;
    public List<Player> PlayersNormar = new ArrayList<>();
    public List<Player> PlayersVIP = new ArrayList<>();
    private static ChonAiDay_Gold instance;

    public static ChonAiDay_Gold gI() {
        if (instance == null) {
            instance = new ChonAiDay_Gold();
        }
        return instance;
    }

    public void addPlayerVIP(Player pl) {
        if (!PlayersVIP.contains(pl)) {
            PlayersVIP.add(pl);
        }
    }

    public void addPlayerNormar(Player pl) {
        if (!PlayersNormar.contains(pl)) {
            PlayersNormar.add(pl);
        }
    }

    public void removePlayerVIP(Player pl) {
        PlayersVIP.removeIf(player -> player == pl);
    }

    public void removePlayerNormar(Player pl) {
        PlayersNormar.removeIf(player -> player == pl);
    }

    @Override
    public void run() {
        while (true) {
            try {
                if ((lastTimeEnd - System.currentTimeMillis()) / 1000 <= 0) {
                    List<Player> listN = new ArrayList<>();

                    PlayersNormar.stream().filter(p -> p != null && p.goldNormar != 0)
                            .sorted(Comparator.comparing(p -> Math.ceil(((double) p.goldNormar / goldNormar) * 100), Comparator.reverseOrder()))
                            .forEach(listN::add);

                    if (!listN.isEmpty()) {
                        int numWinners = Math.min(listN.size(), 5);
                        Player pl = listN.get(Util.nextInt(0, numWinners - 1));
                        if (pl != null) {
                            String chatMessage = pl.name + " đã chiến thắng Chọn ai đây giải thưởng";
                            // FIX (46): trước đây tính int: quỹ > ~24–27 triệu thì goldNormar * 80 TRÀN thành âm
                            // => người thắng bị TRỪ vàng. Nay tính long và chặn trần vàng.
                            long goldC = (long) goldNormar * 80 / 100;
                            Service.gI().sendThongBao(pl, "Chúc mừng bạn đã dành chiến thắng và nhận được " + Util.numberToMoney(goldC) + " vàng");
                            pl.inventory.gold = Math.min(pl.inventory.gold + goldC, nro.models.player.Inventory.LIMIT_GOLD);
                            Service.gI().sendMoney(pl);
                            ChatGlobalService.gI().chat(pl, chatMessage);
                        }
                    }

                    listN.clear();

                    PlayersVIP.stream().filter(p -> p != null && p.goldVIP != 0)
                            .sorted(Comparator.comparing(p -> Math.ceil(((double) p.goldVIP / goldVip) * 100), Comparator.reverseOrder()))
                            .forEach(listN::add);

                    if (!listN.isEmpty()) {
                        int numWinners = Math.min(listN.size(), 5);
                        Player pl = listN.get(Util.nextInt(0, numWinners - 1));
                        if (pl != null && pl.inventory != null) {
                            String chatMessage = pl.name + " đã chiến thắng Chọn ai đây giải VIP";
                            // FIX (46): trước đây tính int: quỹ > ~24–27 triệu thì goldVip * 90 TRÀN thành âm
                            // => người thắng bị TRỪ vàng. Nay tính long và chặn trần vàng.
                            long goldC = (long) goldVip * 90 / 100;
                            Service.gI().sendThongBao(pl, "Chúc mừng bạn đã dành chiến thắng và nhận được " + Util.numberToMoney(goldC) + " vàng");
                            pl.inventory.gold = Math.min(pl.inventory.gold + goldC, nro.models.player.Inventory.LIMIT_GOLD);
                            Service.gI().sendMoney(pl);
                            ChatGlobalService.gI().chat(pl, chatMessage);
                        }
                    }

                    resetPlayers(PlayersNormar);
                    resetPlayers(PlayersVIP);
                    resetChonAiDay();
                }
                Thread.sleep(1000);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    private void resetPlayers(List<Player> players) {
        players.forEach(player -> {
            if (player != null) {
                player.goldVIP = 0;
                player.goldNormar = 0;
            }
        });
        players.clear();
    }

    private void resetChonAiDay() {
        goldNormar = 0;
        goldVip = 0;
        lastTimeEnd = System.currentTimeMillis() + TIME_CHONAIDAY;
    }
}
