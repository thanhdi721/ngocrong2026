package nro.models.event;

import java.util.ArrayList;
import java.util.Collections;
import java.util.IdentityHashMap;
import java.util.List;
import java.util.Set;
import nro.models.boss.Boss;
import nro.models.boss.Boss_Manager.BossManager;
import nro.models.ievent.IEvent;
import nro.models.map.Map;
import nro.models.map.service.ChangeMapService;
import nro.models.map.service.MapService;
import nro.models.npc.Npc;
import nro.models.npc.NpcFactory;
import nro.models.utils.Logger;

public abstract class Event implements IEvent {

    /** Boss do sự kiện này tạo ra (để tắt sự kiện lúc đang chạy). */
    private final List<Boss> createdBosses = Collections.synchronizedList(new ArrayList<>());
    /** NPC do sự kiện này đặt vào map: {Map, Npc}. */
    private final List<Object[]> createdNpcs = Collections.synchronizedList(new ArrayList<>());

    @Override
    public void init() {
        npc();
        boss();
        itemMap();
        itemBoss();
    }

    @Override
    public void npc() {

    }

    @Override
    public void createNpc(int mapId, int npcId, int x, int y) {
        Map map = MapService.gI().getMapById(mapId);
        Npc npc = NpcFactory.createNPC(mapId, 1, x, y, npcId);
        map.npcs.add(npc);
        createdNpcs.add(new Object[]{map, npc});
    }

    @Override
    public void boss() {

    }

    @Override
    public void createBoss(int bossId, int... total) {
        int len = 1;
        if (total.length > 0) {
            len = total[0];
        }
        try {
            for (int i = 0; i < len; i++) {
                Boss b = BossManager.gI().createBoss(bossId);
                if (b != null) {
                    createdBosses.add(b);
                }
            }
        } catch (Exception e) {
            Logger.error(e + "\n");
        }
    }

    @Override
    public void itemMap() {

    }

    @Override
    public void itemBoss() {

    }

    /** Toàn bộ boss của sự kiện, gồm cả boss "xuất hiện cùng" (VD Sơn Tinh đi kèm Thủy Tinh). */
    public List<Boss> getAllBosses() {
        Set<Boss> seen = Collections.newSetFromMap(new IdentityHashMap<>());
        List<Boss> out = new ArrayList<>();
        List<Boss> roots;
        synchronized (createdBosses) {
            roots = new ArrayList<>(createdBosses);
        }
        for (Boss b : roots) {
            collect(b, seen, out);
        }
        return out;
    }

    private static void collect(Boss b, Set<Boss> seen, List<Boss> out) {
        if (b == null || !seen.add(b)) {
            return;
        }
        out.add(b);
        if (b.bossAppearTogether != null) {
            for (Boss[] arr : b.bossAppearTogether) {
                if (arr != null) {
                    for (Boss c : arr) {
                        collect(c, seen, out);
                    }
                }
            }
        }
    }

    /** Số boss của sự kiện đang đứng trên map (còn sống). */
    public int countBossesOnMap() {
        int n = 0;
        for (Boss b : getAllBosses()) {
            if (b.zone != null && !b.isDie()) {
                n++;
            }
        }
        return n;
    }

    /**
     * Gỡ sự kiện khi server đang chạy:
     * 1) bỏ boss khỏi manager (manager ngừng gọi update(), boss không hồi sinh nữa),
     * 2) chờ quá 1 nhịp manager (1,5 giây) để không có update() nào đang chạy dở,
     * 3) đưa boss ra khỏi map (ChangeMapService.exitMap - cách game dùng khi boss rời map),
     * 4) gỡ NPC sự kiện khỏi danh sách NPC của map.
     *
     * @param manager manager chứa boss của sự kiện (null = sự kiện không có boss riêng)
     * @return số boss đã gỡ
     */
    public int stop(BossManager manager) {
        List<Boss> all = getAllBosses();
        if (manager != null) {
            for (Boss b : all) {
                manager.removeBoss(b);
            }
        }
        if (!all.isEmpty()) {
            try {
                Thread.sleep(1600);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }
        for (Boss b : all) {
            try {
                if (b.zone != null) {
                    ChangeMapService.gI().exitMap(b);
                }
            } catch (Exception e) {
                Logger.error("Loi go boss su kien " + b.name + ": " + e + "\n");
            }
        }
        synchronized (createdNpcs) {
            for (Object[] o : createdNpcs) {
                try {
                    ((Map) o[0]).npcs.remove((Npc) o[1]);
                } catch (Exception e) {
                    Logger.error("Loi go NPC su kien: " + e + "\n");
                }
            }
            createdNpcs.clear();
        }
        createdBosses.clear();
        return all.size();
    }
}
