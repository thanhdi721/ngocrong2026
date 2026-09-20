package nro.models.matches;
import nro.models.consts.ConstAchievement;
import nro.models.matches.PVP;
import nro.models.matches.TYPE_LOSE_PVP;
import nro.models.matches.TYPE_PVP;
import nro.models.services.AchievementService;
import nro.models.player.Inventory;
import nro.models.player.Player;
import nro.models.server.Client;
import nro.models.services.Service;
import nro.models.utils.Util;
import nro.models.services.TaskService;

public class ThachDau extends PVP {

    private int goldThachDau;
    private long goldReward;
    // FIX: cờ theo dõi tiền cược đã được giữ / đã được quyết toán
    private boolean goldTaken;
    private boolean settled;

    public ThachDau(Player p1, Player p2, int goldThachDau) {
        super(TYPE_PVP.THACH_DAU, p1, p2);
        this.goldThachDau = goldThachDau;
        this.goldReward = goldThachDau / 100 * 80;
        // FIX: super(...) gọi start() TRƯỚC khi goldThachDau được gán nên trước đây luôn trừ 0 vàng.
        // Trừ tiền cược của cả hai ngay tại đây, sau khi các trường đã có giá trị.
        takeGold();
    }

    private void takeGold() {
        if (this.goldTaken || this.goldThachDau <= 0) {
            return;
        }
        subGold(this.p1);
        subGold(this.p2);
        this.goldTaken = true;
    }

    private void subGold(Player pl) {
        if (pl == null || pl.inventory == null) {
            return;
        }
        pl.inventory.gold -= this.goldThachDau;
        if (pl.inventory.gold < 0) {
            pl.inventory.gold = 0;
        }
        Service.gI().sendMoney(pl);
    }

    private void addGold(Player pl, long amount) {
        if (pl == null || pl.inventory == null || amount <= 0) {
            return;
        }
        if (pl.inventory.gold > Inventory.LIMIT_GOLD - amount) {
            pl.inventory.gold = Inventory.LIMIT_GOLD;
        } else {
            pl.inventory.gold += amount;
        }
        Service.gI().sendMoney(pl);
    }

    @Override
    public void start() {
        // FIX: không trừ cược ở đây nữa (xem takeGold() trong constructor)
        super.start();
    }

    @Override
    public void finish() {

    }

    @Override
    public void dispose() {
        // FIX: trận kết thúc mà chưa quyết toán (huỷ / một bên rời game) => hoàn lại tiền cược cho cả hai
        if (this.goldTaken && !this.settled) {
            this.settled = true;
            addGold(this.p1, this.goldThachDau);
            addGold(this.p2, this.goldThachDau);
        }
        super.dispose();
    }

    @Override
    public void update() {
    }

    @Override
    public void reward(Player plWin) {
        // TUYẾN MỚI: B5 — thắng một trận thách đấu PvP -> ghi nhận TASK_44_2
        TaskService.gI().checkDoneTaskWinMatch(plWin, 0);
        // FIX: hoàn lại tiền cược người thắng đã đặt + 80% cược lấy từ tiền người thua đã đặt.
        // Kết quả ròng vẫn như cũ (thắng +80% cược, thua −100% cược) nhưng tiền lấy từ quỹ cược
        // chứ không sinh ra từ không khí.
        this.settled = true;
        addGold(plWin, (this.goldTaken ? this.goldThachDau : 0) + this.goldReward);
    }

    @Override
    public void sendResult(Player plLose, TYPE_LOSE_PVP typeLose) {
        if (typeLose == TYPE_LOSE_PVP.RUNS_AWAY) {
            Player plL = Client.gI().getPlayer(plLose.id);
            if (plL == null) {
                Service.gI().sendThongBao(p1.equals(plLose) ? p2 : p1, "Đối thủ rời game, bạn thắng được " + Util.numberToMoney(this.goldReward) + " vàng");
            } else {
                Service.gI().sendThongBao(p1.equals(plLose) ? p2 : p1, "Đối thủ sợ quá bỏ chạy, bạn thắng được " + Util.numberToMoney(this.goldReward) + " vàng");
            }
            Service.gI().sendThongBao(p1.equals(plLose) ? p1 : p2, "Bạn bị xử thua vì đã bỏ chạy");
            // FIX: bỏ trừ lần 2 - tiền cược đã bị giữ từ lúc bắt đầu trận
        } else if (typeLose == TYPE_LOSE_PVP.DEAD) {
            Service.gI().sendThongBao(p1.equals(plLose) ? p2 : p1, "Đối thủ đã kiệt sức, bạn thắng được " + Util.numberToMoney(this.goldReward) + " vàng");
            Service.gI().sendThongBao(p1.equals(plLose) ? p1 : p2, "Bạn đã thua vì đã kiệt sức");
            // FIX: bỏ trừ lần 2 - tiền cược đã bị giữ từ lúc bắt đầu trận
        }
        Service.gI().sendMoney(p1.equals(plLose) ? p1 : p2);
        if (!p1.equals(plLose)) {
            AchievementService.gI().checkDoneTask(p1, ConstAchievement.TRAM_TRAN_TRAM_THANG);
        }
    }

}
