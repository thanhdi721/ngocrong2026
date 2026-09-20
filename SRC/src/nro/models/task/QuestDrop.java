package nro.models.task;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.WeakHashMap;

import nro.models.boss.Boss;
import nro.models.boss.BossID;
import nro.models.consts.ConstMob;
import nro.models.consts.ConstTask;
import nro.models.item.Item;
import nro.models.map.ItemMap;
import nro.models.map.Zone;
import nro.models.mob.Mob;
import nro.models.player.Player;
import nro.models.server.Manager;
import nro.models.services.ItemService;
import nro.models.services.Service;
import nro.models.services.TaskService;
import nro.models.utils.Util;

/**
 * BẢNG RƠI VẬT PHẨM NHIỆM VỤ DÙNG CHUNG (docs/4-trien-khai/42-roi-vat-pham-nhiem-vu.md).
 *
 * <p>
 * Mỗi dòng {@link Rule}: bước nhiệm vụ ({@code TASK_x_y}) — nguồn (quái / boss / sinh sẵn
 * trong map) — id vật phẩm — tỉ lệ (%) — số lượng. Vật phẩm CHỈ rơi khi người kết liễu
 * (hoặc người đứng trong map, với nguồn sinh sẵn) đang ở ĐÚNG bước đó, gắn chủ là người đó,
 * và chỉ người đó thấy (khi vào map) và nhặt được (xem {@link #canPick}).
 *
 * <p>
 * Ba điểm móc:
 * <ul>
 * <li>Quái chết — {@code Mob.dropItemTask} gọi {@link #onMobKilled}.</li>
 * <li>Boss chết — {@code TaskService.checkDoneTaskKillBoss} gọi {@link #onBossKilled} SAU khi
 * đã cộng bước hạ boss, nên đòn kết liễu chuyển sang bước "nhặt" là rơi luôn. Mọi boss
 * (thế giới lẫn bản nhiệm vụ {@code QuestBoss}) đều đi qua hàm này trong {@code reward()}.</li>
 * <li>Sinh sẵn trong map không có quái (map 166) — {@code Zone.update} gọi {@link #updateZone}.</li>
 * </ul>
 *
 * <p>
 * Id vật phẩm theo bảng CHỐT docs/4-trien-khai/25-bang-id-vat-pham-moi.md (2000–2031), KHÔNG
 * theo id cũ trong 20b/20c. Vật phẩm trong dải đó luôn được gắn option 30 "Không thể giao dịch".
 */
public final class QuestDrop {

    public enum SourceType {
        /** Quái có tempId trong {@code sourceIds}; {@code mapIds} rỗng = mọi map. */
        MOB,
        /** Boss có id trong {@code sourceIds} (mọi hình dạng). */
        BOSS,
        /** Sinh sẵn cho người đang ở bước, trong các map {@code mapIds} (map không có quái). */
        MAP_SPAWN
    }

    public static final class Rule {

        public final int step;
        public final SourceType type;
        public final int[] sourceIds;
        public final int[] mapIds;
        public final int itemId;
        public final int ratePercent;
        public final int quantity;

        Rule(int step, SourceType type, int[] sourceIds, int[] mapIds, int itemId, int ratePercent, int quantity) {
            this.step = step;
            this.type = type;
            this.sourceIds = sourceIds == null ? new int[0] : sourceIds;
            this.mapIds = mapIds == null ? new int[0] : mapIds;
            this.itemId = itemId;
            this.ratePercent = ratePercent;
            this.quantity = quantity;
        }

        boolean matchSource(int id) {
            for (int s : sourceIds) {
                if (s == id) {
                    return true;
                }
            }
            return false;
        }

        boolean matchMap(int mapId) {
            if (mapIds.length == 0) {
                return true;
            }
            for (int m : mapIds) {
                if (m == mapId) {
                    return true;
                }
            }
            return false;
        }
    }

    private static int[] ids(int... v) {
        return v;
    }

    private static final int[] ANY_MAP = {};

    /** Số vật phẩm sinh sẵn giữ trên mặt đất cho mỗi người (nguồn MAP_SPAWN). */
    private static final int MAP_SPAWN_KEEP = 3;
    /** Giãn cách giữa 2 lần sinh cho cùng một khu (ms). */
    private static final long MAP_SPAWN_INTERVAL = 3000L;

    // =====================================================================
    // BẢNG DỮ LIỆU — mỗi dòng: bước, nguồn, id nguồn, map, vật phẩm, tỉ lệ %, số lượng
    // =====================================================================
    public static final List<Rule> RULES = Collections.unmodifiableList(java.util.Arrays.asList(
            // NV 5 b0 — Kỷ Vật Của Ông (chuyển từ Mob.dropItemTask, giữ nguyên 100%)
            new Rule(ConstTask.TASK_5_0, SourceType.MOB,
                    ids(ConstMob.THAN_LAN_BAY, ConstMob.PHI_LONG, ConstMob.QUY_BAY), ANY_MAP, 2010, 100, 1),
            // NV 16 b3 — Vỏ đạn khắc dấu, 25% (chuyển từ Mob.dropItemTask)
            new Rule(ConstTask.TASK_16_3, SourceType.MOB,
                    ids(ConstMob.BULON, ConstMob.UKULELE, ConstMob.QUY_MAP), ANY_MAP, 2014, 25, 1),
            // NV 20 b3+b4 — Thẻ tiền thưởng Granola, 100% từ Kuku / Mập Đầu Đinh / Rambo.
            // Rơi cả ở bước 3 (đang hạ boss): trước đây chỉ rơi ở bước 4 nên 3 boss vừa hạ không
            // cho thẻ nào, người chơi phải hạ thêm. Thẻ nhặt ở bước 3 được cộng khi sang bước 4.
            new Rule(ConstTask.TASK_20_3, SourceType.BOSS,
                    ids(BossID.KUKU, BossID.MAP_DAU_DINH, BossID.RAMBO), ANY_MAP, 2016, 100, 1),
            new Rule(ConstTask.TASK_48_3, SourceType.BOSS,
                    ids(BossID.KUKU, BossID.MAP_DAU_DINH, BossID.RAMBO), ANY_MAP, 2017, 100, 1),
            new Rule(ConstTask.TASK_20_4, SourceType.BOSS,
                    ids(BossID.KUKU, BossID.MAP_DAU_DINH, BossID.RAMBO), ANY_MAP, 2016, 100, 1),
            // NV 48 b4 — Biên bản truy nã Ngân Hà, 100% từ cùng 3 boss (nhánh Jaco)
            new Rule(ConstTask.TASK_48_4, SourceType.BOSS,
                    ids(BossID.KUKU, BossID.MAP_DAU_DINH, BossID.RAMBO), ANY_MAP, 2017, 100, 1),
            // NV 22 b3 — Máy đo ký ức, 100% từ tên bị hạ cuối của Tiểu đội sát thủ (bất kỳ tên nào)
            new Rule(ConstTask.TASK_22_3, SourceType.BOSS,
                    // FIX: tính cả bản Namek, khớp với bước 22.2 (TaskService.checkDoneTaskKillBoss)
                    ids(BossID.TIEU_DOI_TRUONG, BossID.SO_1, BossID.SO_2, BossID.SO_3, BossID.SO_4,
                            BossID.TIEU_DOI_TRUONG_NM, BossID.SO_1_NM, BossID.SO_2_NM, BossID.SO_3_NM, BossID.SO_4_NM),
                    ANY_MAP, 2018, 100, 1),
            // NV 25 b2 — Lõi năng lượng Android, 100% từ Android 19 / Dr.Kôrê
            new Rule(ConstTask.TASK_25_2, SourceType.BOSS,
                    ids(BossID.ANDROID_19, BossID.DR_KORE), ANY_MAP, 2019, 100, 1),
            // NV 28 b3 — Mảnh giáp khắc tên, 100% từ King Kong
            new Rule(ConstTask.TASK_28_3, SourceType.BOSS,
                    ids(BossID.KING_KONG), ANY_MAP, 2021, 100, 1),
            // NV 29 b2 — Bản thiết kế bản sao: map 166 KHÔNG có quái => sinh sẵn cho riêng người chơi
            new Rule(ConstTask.TASK_29_2, SourceType.MAP_SPAWN,
                    null, ids(166), 2023, 100, 1),
            // NV 32 b4 — Mảnh Ký Ức Vỡ, 100% từ Tobi (chuyển từ Mob.dropItemTask)
            new Rule(ConstTask.TASK_32_4, SourceType.MOB,
                    ids(ConstMob.TOBI), ANY_MAP, 2025, 100, 1),
            // NV 34 b3 — Mảnh Ký Ức Đóng Băng: thay "rải sẵn" bằng rơi 25% từ quái Hang băng (110)
            new Rule(ConstTask.TASK_34_3, SourceType.MOB,
                    ids(ConstMob.KADO, ConstMob.DA_XANH), ids(110), 2026, 25, 1),
            // NV 36 b3 (đường vòng) — Mảnh Bùa Babiđây, 25% từ Cadic M ở Sa mạc hoang vu (165)
            new Rule(ConstTask.TASK_36_3, SourceType.MOB,
                    ids(ConstMob.THO_CON, ConstMob.CADIC_M), ids(165), 2027, 25, 1),
            // NV 37 b3 — Lõi Phép Babiđây, 100% cho người kết liễu nguồn của bước 2 (và bước 1)
            new Rule(ConstTask.TASK_37_3, SourceType.BOSS,
                    ids(BossID.DRABURA_3, BossID.SUPERBU, BossID.MABU_12H, BossID.MABU, BossID.MABU_14H_NV),
                    ANY_MAP, 2028, 100, 1),
            new Rule(ConstTask.TASK_37_3, SourceType.MOB,
                    ids(ConstMob.QUY_CHIM, ConstMob.HIRUDEGARN), ANY_MAP, 2028, 100, 1),
            new Rule(ConstTask.TASK_37_3, SourceType.MOB,
                    ids(ConstMob.THO_CON, ConstMob.CADIC_M), ids(165), 2028, 100, 1),
            // NV 38 b4 — Nhẫn thời không sai lệch, 100% từ Black Goku (bản NV -2103 và bản thế giới)
            new Rule(ConstTask.TASK_38_4, SourceType.BOSS,
                    ids(BossID.BLACK_GOKU_NV, BossID.BLACK_GOKU), ANY_MAP, 992, 100, 1),
            // NV 45 b1 — Mảnh Ký Ức 7: thay "rải sẵn" bằng rơi 30% từ quái Lãnh địa Fize (78)
            new Rule(ConstTask.TASK_45_1, SourceType.MOB,
                    ids(ConstMob.MOC_NHAN), ids(78), 2008, 30, 1)
    ));

    /** Map có nguồn MAP_SPAWN — tra nhanh trong Zone.update. */
    private static final Set<Integer> MAP_SPAWN_MAPS = new HashSet<>();

    static {
        for (Rule r : RULES) {
            if (r.type == SourceType.MAP_SPAWN) {
                for (int m : r.mapIds) {
                    MAP_SPAWN_MAPS.add(m);
                }
            }
        }
    }

    /**
     * Chủ nhân của từng vật phẩm nhiệm vụ đang nằm trên đất. Không dựa vào
     * {@code ItemMap.playerId} vì {@code ItemMap.update} xóa chủ sau 45 giây.
     */
    private static final Map<ItemMap, Long> OWNERS = Collections.synchronizedMap(new WeakHashMap<>());

    /** Lần sinh gần nhất theo khu (nguồn MAP_SPAWN). */
    private static final Map<Zone, Long> LAST_SPAWN = Collections.synchronizedMap(new WeakHashMap<>());

    private QuestDrop() {
    }

    // =====================================================================
    // TẠO VẬT PHẨM
    // =====================================================================
    /**
     * Tạo ItemMap vật phẩm nhiệm vụ gắn chủ {@code owner} (đã tự thêm vào khu).
     * Gắn option 30 "Không thể giao dịch" cho dải 2000–2031 (doc 25 §3).
     */
    public static ItemMap create(Zone zone, Player owner, int itemId, int quantity, int x, int y) {
        if (zone == null || owner == null || itemId < 0 || itemId >= Manager.ITEM_TEMPLATES.size()) {
            return null;
        }
        ItemMap itemMap = new ItemMap(zone, itemId, Math.max(1, quantity), x, y, owner.id);
        if (ItemService.isTaskItem(itemId)) {
            itemMap.options.add(new Item.ItemOption(30, 0)); // Không thể giao dịch
        }
        OWNERS.put(itemMap, owner.id);
        return itemMap;
    }

    /** Tạo rồi hiện vật phẩm CHỈ cho {@code owner} (người khác trong map không thấy). */
    public static ItemMap dropForPlayer(Zone zone, Player owner, int itemId, int quantity, int x, int y) {
        ItemMap itemMap = create(zone, owner, itemId, quantity, x, y);
        if (itemMap != null && owner.zone == zone) {
            Service.gI().dropItemMapForMe(owner, itemMap);
        }
        return itemMap;
    }

    // =====================================================================
    // ĐIỂM MÓC
    // =====================================================================
    /**
     * Quái chết. Trả về ItemMap (chưa gửi) để {@code Mob} gộp vào gói rơi đồ — gói đó ghi
     * kèm playerId nên chỉ chủ nhân nhặt được; null nếu không rơi.
     */
    public static ItemMap onMobKilled(Player killer, Mob mob) {
        if (killer == null || mob == null || mob.zone == null || mob.zone.map == null || !killer.isPl()) {
            return null;
        }
        int step = TaskService.gI().getIdTask(killer);
        int mapId = mob.zone.map.mapId;
        for (Rule r : RULES) {
            if (r.type != SourceType.MOB || r.step != step || !r.matchSource(mob.tempId) || !r.matchMap(mapId)) {
                continue;
            }
            if (!Util.isTrue(r.ratePercent, 100)) {
                return null;
            }
            return create(mob.zone, killer, r.itemId, r.quantity, mob.location.x, mob.location.y);
        }
        return null;
    }

    /**
     * Boss chết — gọi SAU khi đã cộng tiến độ bước hạ boss (TaskService.checkDoneTaskKillBoss).
     */
    public static void onBossKilled(Player killer, Boss boss) {
        if (killer == null || boss == null || !killer.isPl()) {
            return;
        }
        Zone zone = boss.zone != null ? boss.zone : killer.zone;
        if (zone == null || zone.map == null) {
            return;
        }
        int step = TaskService.gI().getIdTask(killer);
        for (Rule r : RULES) {
            if (r.type != SourceType.BOSS || r.step != step || !r.matchSource((int) boss.id)
                    || !r.matchMap(zone.map.mapId)) {
                continue;
            }
            if (!Util.isTrue(r.ratePercent, 100)) {
                return;
            }
            int x = boss.location != null ? boss.location.x : killer.location.x;
            int yBase = boss.location != null ? boss.location.y : killer.location.y;
            int y = zone.map.yPhysicInTop(x, yBase - 24);
            dropForPlayer(zone, killer, r.itemId, r.quantity, x, y);
            return;
        }
    }

    /**
     * Gọi mỗi tick từ Zone.update: sinh sẵn vật phẩm cho người đang ở đúng bước, trong
     * map không có quái (map 166). Giữ tối đa {@link #MAP_SPAWN_KEEP} món mỗi người trên
     * đất; món cũ tự biến mất sau 50 giây (ItemMap.update) nên luôn được bù lại.
     */
    public static void updateZone(Zone zone) {
        if (zone == null || zone.map == null || !MAP_SPAWN_MAPS.contains(zone.map.mapId)) {
            return;
        }
        Long last = LAST_SPAWN.get(zone);
        if (last != null && !Util.canDoWithTime(last, MAP_SPAWN_INTERVAL)) {
            return;
        }
        LAST_SPAWN.put(zone, System.currentTimeMillis());
        List<Player> players = snapshot(zone.players);
        for (Player pl : players) {
            if (pl == null || !pl.isPl() || pl.zone != zone) {
                continue;
            }
            int step = TaskService.gI().getIdTask(pl);
            for (Rule r : RULES) {
                if (r.type != SourceType.MAP_SPAWN || r.step != step || !r.matchMap(zone.map.mapId)) {
                    continue;
                }
                if (countOwnedOnGround(zone, pl, r.itemId) >= MAP_SPAWN_KEEP) {
                    continue;
                }
                int width = zone.map.mapWidth > 200 ? zone.map.mapWidth : 1000;
                int x = pl.location.x + Util.nextInt(-240, 240);
                x = Math.max(60, Math.min(width - 60, x));
                int y = zone.map.yPhysicInTop(x, pl.location.y - 24);
                dropForPlayer(zone, pl, r.itemId, r.quantity, x, y);
            }
        }
    }

    private static int countOwnedOnGround(Zone zone, Player pl, int itemId) {
        int n = 0;
        List<ItemMap> items = snapshot(zone.items);
        for (ItemMap it : items) {
            if (it == null || it.itemTemplate == null || it.isPickedUp || it.itemTemplate.id != itemId) {
                continue;
            }
            Long owner = OWNERS.get(it);
            if (owner != null && owner == pl.id) {
                n++;
            }
        }
        return n;
    }

    /** Chép danh sách của Zone (không đồng bộ) theo chỉ số, bỏ qua lỗi sửa đồng thời. */
    private static <T> List<T> snapshot(List<T> src) {
        List<T> out = new ArrayList<>();
        try {
            for (int i = src.size() - 1; i >= 0; i--) {
                if (i < src.size()) {
                    T t = src.get(i);
                    if (t != null) {
                        out.add(t);
                    }
                }
            }
        } catch (Exception ignored) {
            // danh sách đang bị luồng khác sửa — dùng phần đã chép, tick sau thử lại
        }
        return out;
    }

    // =====================================================================
    // AI ĐƯỢC THẤY / NHẶT
    // =====================================================================
    /** Vật phẩm nhiệm vụ của người khác — ẩn khi gửi danh sách đồ trên đất lúc vào map. */
    public static boolean isHiddenFor(ItemMap itemMap, Player player) {
        if (itemMap == null || player == null) {
            return false;
        }
        Long owner = OWNERS.get(itemMap);
        return owner != null && owner != player.id;
    }

    /** Chỉ chủ nhân nhặt được vật phẩm nhiệm vụ do bảng này sinh ra (kể cả sau 45 giây). */
    public static boolean canPick(Player player, ItemMap itemMap) {
        return !isHiddenFor(itemMap, player);
    }

    /** Vật phẩm này do bảng rơi nhiệm vụ sinh ra. */
    public static boolean isQuestDrop(ItemMap itemMap) {
        return itemMap != null && OWNERS.containsKey(itemMap);
    }
}
