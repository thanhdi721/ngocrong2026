package nro.models.boss.quest;

import java.util.List;

import nro.models.boss.Boss;
import nro.models.boss.BossData;
import nro.models.consts.AppearType;
import nro.models.consts.BossStatus;
import nro.models.item.Item;
import nro.models.map.ItemMap;
import nro.models.map.service.ChangeMapService;
import nro.models.map.Zone;
import nro.models.player.Player;
import nro.models.server.Client;
import nro.models.server.Manager;
import nro.models.services.EffectSkillService;
import nro.models.services.Service;
import nro.models.services.TaskService;
import nro.models.utils.Util;

/**
 * Lớp nền cho toàn bộ BOSS BẢN NHIỆM VỤ (dải id -2100 … -2199) và boss Heart.
 *
 * <p>
 * Ba nguyên tắc đã chốt (docs/4-trien-khai/22-san-sang-code.md §0 mục 2 & 11,
 * docs/3-kiem-tra-can-bang/21c-boss-roi-do-can-bang.md §6.1 và §6.4 phương án C):
 * <ul>
 * <li><b>Chỉ rơi đồ nhiệm vụ.</b> Không vàng, không trang bị, không Ngọc Rồng,
 * không đồ Thần Linh — toàn bộ nằm ở {@link #reward(Player)} bên dưới.</li>
 * <li><b>Không đè lên boss thế giới.</b> Boss chỉ vào khu nào <i>không có</i> boss
 * nào khác đang đứng (kể cả boss thế giới bản gốc cùng tạo hình).</li>
 * <li><b>Luôn có mặt, ai vào được map là đánh được.</b> (Chủ dự án chốt 18/09/2026 —
 * thay cho luật cũ "chỉ hiện khi có người đang làm đúng nhiệm vụ".) Không sợ farm
 * vì boss chỉ rơi đồ nhiệm vụ.</li>
 * </ul>
 *
 * <p>
 * Cách hoạt động: chết xong boss nghỉ <b>ngẫu nhiên 15–30 phút</b>, rồi hiện lại ở
 * một <b>khu bất kỳ</b> trong danh sách map của hình dạng đó. Khu chọn được đặt vào
 * {@code zoneFinal} nên {@code Boss.joinMap()} đi thẳng vào khu đó. Khu vắng người
 * boss vẫn đứng nguyên, không tự bỏ đi.
 */
public abstract class QuestBoss extends Boss {

    /** Giãn cách giữa 2 lần quét danh sách người chơi (ms). */
    private static final long SCAN_INTERVAL = 3000L;

    /** Quá thời gian này mà vẫn không tìm được khu hợp lệ thì bỏ lượt, về nghỉ (ms). */
    private static final long JOIN_TIMEOUT = 600_000L;

    /**
     * KHÔNG CÒN DÙNG từ 18/09/2026 (boss luôn đứng lại, không tự rời map khi khu vắng).
     * Giữ lại để bật nhanh nếu sau này muốn boss bỏ đi khi không có ai.
     */
    @SuppressWarnings("unused")
    private static final long IDLE_LEAVE = 300_000L;

    /** Id các nhiệm vụ chính cho phép boss này xuất hiện. */
    protected final int[] taskIdRequired;

    /** Id các boss thế giới tương ứng (để chắc chắn không đứng chung khu với bản gốc). */
    protected final int[] worldBossIds;

    /** Chết xong nghỉ ngẫu nhiên trong khoảng này rồi hiện lại (chủ dự án chốt: 15–30 phút). */
    protected static final int REST_MIN_SECONDS = 15 * 60;
    protected static final int REST_MAX_SECONDS = 30 * 60;

    /** Thời gian nghỉ của lượt hiện tại, bốc ngẫu nhiên mỗi lần chết. */
    private int nextRestSeconds;

    private long lastTimeScan;
    private long lastTimeFailJoin;
    private long lastTimeHavePlayer;

    protected QuestBoss(int id, int[] worldBossIds, int[] taskIdRequired, BossData... data) throws Exception {
        // isNotifyDisabled = true: không thông báo toàn server, tránh kéo đám đông tới farm.
        super(id, true, false, data);
        this.worldBossIds = worldBossIds == null ? new int[0] : worldBossIds;
        this.taskIdRequired = taskIdRequired;
    }

    // ================= gác cửa: ai được thấy boss này =================
    /**
     * Id nhiệm vụ cho phép hình dạng {@code level} xuất hiện. Lớp con ghi đè khi
     * mỗi hình dạng thuộc một nhiệm vụ khác nhau (trường hợp Heart).
     */
    protected int[] getTaskIdRequired(int level) {
        return this.taskIdRequired;
    }

    /**
     * KHÔNG CÒN ĐƯỢC GỌI từ 18/09/2026 — boss nay xuất hiện cho mọi người, không lọc
     * theo bước nhiệm vụ. Giữ lại (cùng {@link #getTaskIdRequired(int)}) để bật lại
     * luật "chỉ người đang làm nhiệm vụ mới thấy boss" khi cần.
     */
    protected boolean isPlayerOnQuestStep(Player pl, int level) {
        if (pl == null || pl.isBot || pl.isBoss || pl.isPet || pl.isNewPet) {
            return false;
        }
        if (pl.playerTask == null || pl.playerTask.taskMain == null) {
            return false;
        }
        int[] tasks = getTaskIdRequired(level);
        if (tasks == null) {
            return false;
        }
        for (int taskId : tasks) {
            if (taskId == pl.playerTask.taskMain.id) {
                return true;
            }
        }
        return false;
    }

    protected boolean isMapAllowed(int level, int mapId) {
        int[] maps = this.data[safeLevel(level)].getMapJoin();
        for (int m : maps) {
            if (m == mapId) {
                return true;
            }
        }
        return false;
    }

    protected int safeLevel(int level) {
        if (level < 0 || level >= this.data.length) {
            return 0;
        }
        return level;
    }

    /**
     * Khu đang có boss thế giới bản gốc (hoặc một bản nhiệm vụ khác cùng id) thì
     * KHÔNG được vào — đây là chốt chặn "boss bản nhiệm vụ không đè lên boss thế giới".
     * Boss lang thang (Ăn Trộm, Ở Dơ…) không tính, nếu không boss nhiệm vụ sẽ gần
     * như không bao giờ xuất hiện được ở các map Trái Đất đông boss vặt.
     */
    protected boolean isZoneOccupied(Zone zone) {
        for (Player b : zone.getBosses()) {
            if (b == this) {
                continue;
            }
            if (b.id == this.id) {
                return true;
            }
            for (int worldBossId : this.worldBossIds) {
                if (b.id == worldBossId) {
                    return true;
                }
            }
        }
        return false;
    }

    /**
     * Chọn NGẪU NHIÊN một khu trong các map của hình dạng này.
     *
     * <p>
     * CHỦ DỰ ÁN CHỐT (18/09/2026): boss nhiệm vụ <b>luôn xuất hiện</b>, ai vào được map
     * là đánh được, không cần đang làm nhiệm vụ. Trước đây boss chỉ hồi sinh khi quét
     * thấy người đang ở đúng bước — nay bỏ hẳn cách đó.
     * <p>
     * Vẫn giữ {@link #isZoneOccupied(Zone)} để boss nhiệm vụ không đứng chung khu với
     * boss thế giới bản gốc. Không sợ bị farm: boss nhiệm vụ chỉ rơi đồ nhiệm vụ,
     * không vàng, không trang bị (xem {@link #reward(Player)}).
     */
    /**
     * Sắp xếp các map theo số bản boss CÙNG ID đang đứng (hoặc sắp vào) ở map đó, ít nhất trước.
     * Các map bằng nhau thì trộn ngẫu nhiên. Đọc danh sách boss qua bản sao để không đụng độ
     * với luồng khác đang sửa danh sách.
     */
    private int[] mapsByFewestInstances(int[] maps) {
        java.util.Map<Integer, Integer> count = new java.util.HashMap<>();
        for (int m : maps) {
            count.put(m, 0);
        }
        try {
            List<Boss> snapshot = new java.util.ArrayList<>(
                    nro.models.boss.Boss_Manager.BossManager.gI().getBosses());
            for (Boss b : snapshot) {
                if (b == null || b == this || b.id != this.id) {
                    continue;
                }
                Zone z = b.zone != null ? b.zone : ((b instanceof QuestBoss) ? ((QuestBoss) b).zoneFinal : null);
                if (z != null && z.map != null && count.containsKey(z.map.mapId)) {
                    count.merge(z.map.mapId, 1, Integer::sum);
                }
            }
        } catch (Exception ignored) {
            // danh sách đang bị sửa ở luồng khác — bỏ qua, dùng thứ tự ngẫu nhiên
        }
        List<Integer> list = new java.util.ArrayList<>(count.keySet());
        java.util.Collections.shuffle(list);
        list.sort(java.util.Comparator.comparingInt(count::get));
        int[] out = new int[list.size()];
        for (int i = 0; i < out.length; i++) {
            out[i] = list.get(i);
        }
        return out;
    }

    protected Zone findRandomZone(int level) {
        int[] maps = this.data[safeLevel(level)].getMapJoin();
        if (maps == null || maps.length == 0) {
            return null;
        }
        // FIX: RẢI ĐỀU các bản boss ra từng map, ưu tiên map đang có ÍT bản cùng loại nhất.
        // Trước đây mỗi bản bốc ngẫu nhiên 1 map trong mapJoin một cách độc lập. Với boss có
        // mapJoin trải 3 hành tinh (Kẻ Thu Gom: 4/12/18, Jaco: 27/31/35) và 3 bản, xác suất cả
        // 3 bản cùng rơi vào map của hành tinh khác là (2/3)^3 ≈ 30% → người chơi hành tinh đó
        // đi tìm mãi không thấy boss, kẹt nhiệm vụ.
        int[] order = mapsByFewestInstances(maps);
        // Thử nhiều lượt cho tới khi vớ được khu trống; hết lượt thì thôi, tick sau thử lại.
        for (int attempt = 0; attempt < 30; attempt++) {
            // 2/3 số lượt đầu chỉ thử map ít bản nhất; sau đó mới mở rộng ra mọi map.
            int mapId = attempt < 20 ? order[0] : maps[Util.nextInt(0, maps.length - 1)];
            nro.models.map.Map map = nro.models.map.service.MapService.gI().getMapById(mapId);
            if (map == null || map.zones == null || map.zones.isEmpty()) {
                continue;
            }
            Zone zone = map.zones.get(Util.nextInt(0, map.zones.size() - 1));
            if (zone == null || zone.map == null) {
                continue;
            }
            if (isZoneOccupied(zone)) {
                continue;
            }
            return zone;
        }
        return null;
    }

    // ================= vòng đời =================
    @Override
    public void rest() {
        int nextLevel = this.currentLevel + 1;
        if (nextLevel >= this.data.length) {
            nextLevel = 0;
        }
        if (this.data[nextLevel].getTypeAppear() != AppearType.DEFAULT_APPEAR) {
            return;
        }
        // CHỦ DỰ ÁN CHỐT: chết xong nghỉ NGẪU NHIÊN 15–30 phút rồi hiện lại ở một khu bất kỳ.
        if (this.nextRestSeconds <= 0) {
            this.nextRestSeconds = Util.nextInt(REST_MIN_SECONDS, REST_MAX_SECONDS);
        }
        if (!Util.canDoWithTime(this.lastTimeRest, (long) this.nextRestSeconds * 1000L)) {
            return;
        }
        if (!Util.canDoWithTime(this.lastTimeScan, SCAN_INTERVAL)) {
            return;
        }
        this.lastTimeScan = System.currentTimeMillis();
        Zone zone = findRandomZone(nextLevel);
        if (zone == null) {
            return;
        }
        this.nextRestSeconds = 0;
        this.zoneFinal = zone;
        this.changeStatus(BossStatus.RESPAWN);
    }

    @Override
    public void joinMap() {
        int level = safeLevel(this.currentLevel);
        boolean valid = this.zoneFinal != null && this.zoneFinal.map != null
                && isMapAllowed(level, this.zoneFinal.map.mapId)
                && !isZoneOccupied(this.zoneFinal);
        if (!valid && Util.canDoWithTime(this.lastTimeScan, SCAN_INTERVAL)) {
            this.lastTimeScan = System.currentTimeMillis();
            this.zoneFinal = findRandomZone(level);
            valid = this.zoneFinal != null;
        }
        if (!valid) {
            // Đang đứng ở map không còn hợp lệ cho hình dạng mới (Heart: 166 -> 145) thì
            // rời map ngay, đừng để một "Heart Hư Không" đứng trơ trong phòng thí nghiệm.
            if (this.zone != null && this.zone.map != null && !isMapAllowed(level, this.zone.map.mapId)) {
                ChangeMapService.gI().exitMap(this);
                this.zone = null;
                this.lastZone = null;
            }
            // Đổi hình dạng thường kèm đổi map (Heart: 166 -> 145). Giữ nguyên trạng thái
            // JOIN_MAP và thử lại ở tick sau, để người chơi kịp di chuyển sang map mới.
            if (this.lastTimeFailJoin == 0) {
                this.lastTimeFailJoin = System.currentTimeMillis();
            } else if (Util.canDoWithTime(this.lastTimeFailJoin, JOIN_TIMEOUT)) {
                this.lastTimeFailJoin = 0;
                this.zoneFinal = null;
                this.leaveMapNew();
            }
            return;
        }
        this.lastTimeFailJoin = 0;
        this.lastTimeHavePlayer = System.currentTimeMillis();
        super.joinMap();
    }

    @Override
    public void autoLeaveMap() {
        // CHỦ DỰ ÁN CHỐT: boss nhiệm vụ LUÔN có mặt, không tự bỏ đi khi khu vắng người.
        // Trước đây khu trống 5 phút là boss rời map, khiến người tới sau không thấy boss đâu.
        this.lastTimeHavePlayer = System.currentTimeMillis();
    }

    @Override
    public void leaveMap() {
        // Hết chuỗi hình dạng thì quên khu cũ đi, lượt sau phải tìm người chơi mới.
        if (this.currentLevel >= this.data.length - 1) {
            this.zoneFinal = null;
        }
        super.leaveMap();
    }

    @Override
    public void die(Player plKill) {
        // Không dùng ServerNotify như boss thế giới: boss cốt truyện chết liên tục,
        // thông báo toàn server chỉ tạo tiếng ồn và kéo người tới cướp đòn cuối.
        if (plKill != null && !plKill.isBot) {
            reward(plKill);
        }
        this.changeStatus(BossStatus.DIE);
    }

    // ================= sát thương =================
    @Override
    public synchronized int injured(Player plAtt, long damage, boolean piercing, boolean isMobAttack) {
        if (this.isDie()) {
            return 0;
        }
        if (!piercing && Util.isTrue(this.nPoint.tlNeDon, 1000)) {
            this.chat("Xí hụt");
            return 0;
        }
        if (plAtt != null && plAtt.idNRNM != -1) {
            return 1;
        }
        damage = this.nPoint.subDameInjureWithDeff(damage);
        if (!piercing && this.effectSkill != null && this.effectSkill.isShielding) {
            if (damage > this.nPoint.hpMax) {
                EffectSkillService.gI().breakShield(this);
            }
            damage = 1;
        }
        // Giảm sát thương nhận vào đặt SAU khiên/giáp và NGAY TRƯỚC khi trừ máu,
        // nên không phá hai nhánh đặc biệt ở trên (né đòn, khiên năng lượng).
        damage = applyDamageReduce(damage);
        this.nPoint.subHP(damage);
        if (isDie()) {
            this.setDie(plAtt);
            die(plAtt);
        }
        return (int) damage;
    }

    // ================= phần thưởng =================
    /**
     * Id vật phẩm nhiệm vụ rơi ra khi hạ hình dạng {@code level}; -1 = không rơi gì.
     * Bảng đối chiếu id vật phẩm: docs/4-trien-khai/25-bang-id-vat-pham-moi.md.
     */
    protected int getQuestItemId(int level) {
        return -1;
    }

    @Override
    public void reward(Player plKill) {
        // (1) Luôn báo cho hệ thống nhiệm vụ. Bảng đối chiếu bossId -> bước nhiệm vụ
        //     nằm trong TaskService.checkDoneTaskKillBoss (do nhóm khác cập nhật).
        //     Hàm đó cũng gọi nro.models.task.QuestDrop.onBossKilled — rơi đồ nhiệm vụ THEO
        //     BƯỚC (ví dụ Nhẫn thời không 992 cho người đang ở TASK_38_4 khi hạ Black Goku -2103).
        TaskService.gI().checkDoneTaskKillBoss(plKill, this);
        // (2) Chỉ rơi đồ nhiệm vụ. KHÔNG vàng, KHÔNG trang bị, KHÔNG Ngọc Rồng,
        //     KHÔNG đồ Thần Linh — đồ xịn vẫn chỉ đến từ boss thế giới bản gốc.
        dropQuestItem(plKill);
    }

    protected void dropQuestItem(Player plKill) {
        if (plKill == null || this.zone == null || this.location == null) {
            return;
        }
        int itemId = getQuestItemId(safeLevel(this.currentLevel));
        if (itemId < 0 || itemId >= Manager.ITEM_TEMPLATES.size()) {
            // Vật phẩm nhiệm vụ chưa được nạp vào item_template thì bỏ qua,
            // tránh IndexOutOfBounds như cảnh báo ở docs/4-trien-khai/25 §4.
            return;
        }
        int x = this.location.x;
        int y = this.zone.map.yPhysicInTop(x, this.location.y - 24);
        // doc 42: dùng chung cơ chế QuestDrop — gắn chủ + option 30, chỉ người kết liễu thấy/nhặt.
        nro.models.task.QuestDrop.dropForPlayer(this.zone, plKill, itemId, 1, x, y);
    }
}
