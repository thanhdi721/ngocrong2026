package nro.models.services;

import nro.models.consts.ConstMap;
import nro.models.consts.ConstMob;
import nro.models.consts.ConstNpc;
import nro.models.consts.ConstPlayer;
import nro.models.player.Player;
import nro.models.consts.ConstTask;
import nro.models.boss.Boss;
import nro.models.boss.BossID;
import nro.models.clan.ClanMember;
import nro.models.consts.ConstAchievement;
import nro.models.consts.ConstTaskBadges;
import nro.models.database.TaskRewardDAO;
import nro.models.item.Item;
import java.io.IOException;
import java.net.InetAddress;
import nro.models.map.ItemMap;
import nro.models.map.Zone;
import nro.models.mob.Mob;
import nro.models.npc.Npc;
import nro.models.player_badges.BadgesData;
import nro.models.player_badges.BadgesService;
import nro.models.skill.Skill;
import nro.models.task.SideTaskTemplate;
import nro.models.task.SubTaskMain;
import nro.models.task.TaskMain;
import nro.models.task.TaskMainReward;
import nro.models.server.Manager;
import nro.models.network.Message;
import nro.models.utils.Logger;
import nro.models.utils.Util;
import java.util.ArrayList;
import java.util.List;
import nro.models.server.Client;
import nro.models.task.BadgesTaskService;
import nro.models.task.ClanTaskTemplate;

/**
 *
 * @author By Mr Blue
 *
 * TUYẾN MỚI (doc 27 / 29): toàn bộ bảng điều kiện của nhiệm vụ chính đã được viết lại
 * cho tuyến 51 nhiệm vụ / 238 bước. Phần thưởng đọc từ bảng `task_main_reward`.
 */
public class TaskService {

    /**
     * Làm cùng số người trong bang
     */
    private static final byte NMEMBER_DO_TASK_TOGETHER = 2;
    private boolean canNhanCayThong = true;

    // ---- TUYẾN MỚI: NPC chưa có hằng trong ConstNpc -----------------------
    /**
     * NPC 71 Berry — Khu hang động (map 160), điểm rẽ nhánh 1
     */
    public static final byte NPC_BERRY = 71;
    /**
     * NPC 76 Granola — Khu hang động (map 160)
     */
    public static final byte NPC_GRANOLA = 76;

    // ---- TUYẾN MỚI: boss riêng của chương 1–2 -----------------------------
    /**
     * Kẻ Thu Gom — NV 6 bước 1, mapJoin {4, 12, 18}
     */
    public static final int BOSS_KE_THU_GOM = -2000;
    /**
     * Jaco Vô Thức — NV 15 bước 1 (hình dạng cuối currentLevel == 1)
     */
    public static final int BOSS_JACO_VO_THUC = -2001;

    // ---- TUYẾN MỚI: id option dùng cho trigger B2 / B3 ---------------------
    /**
     * Option 72 — mức nâng cấp trang bị (+1, +2, …)
     */
    public static final int OPTION_UPGRADE_LEVEL = 72;
    /**
     * Option 107 — trang bị đã pha lê hóa
     */
    public static final int OPTION_PHA_LE_HOA = 107;
    /**
     * Option 102 — trang bị đã ép sao pha lê
     */
    public static final int OPTION_EP_SAO = 102;

    /**
     * Loại ghép của trigger B3
     */
    public static final int COMBINE_PHA_LE_HOA = 0;
    public static final int COMBINE_EP_SAO = 1;

    // ---- TUYẾN MỚI: danh hiệu kết tuyến ----------------------------------
    /**
     * Item 2030 "Người Trả Ký Ức" (nhánh NV 47) — danh hiệu TYPE 36, KHÔNG phải item túi
     */
    public static final int ITEM_BADGE_NGUOI_TRA_KY_UC = 2030;
    /**
     * Item 2031 "Kẻ Giữ Hư Không" (nhánh NV 50)
     */
    public static final int ITEM_BADGE_KE_GIU_HU_KHONG = 2031;
    public static final int BADGE_EFFECT_NGUOI_TRA_KY_UC = 257;
    public static final int BADGE_EFFECT_KE_GIU_HU_KHONG = 258;
    /**
     * 36500 ngày ~ 100 năm, coi như vĩnh viễn
     */
    public static final int BADGE_DAYS_FOREVER = 36500;

    // ---- ĐƯỜNG VÒNG CHO NGƯỜI CHƠI LẺ (doc 32) ---------------------------
    /**
     * Hệ số quy đổi quái TRONG phó bản của hai bước "đếm quái" (TASK_35_2 và
     * TASK_43_1). max_count của hai bước đó đã được nhân 2 trong SQL để chứa
     * đường vòng, nên quái trong phó bản phải cộng 2 điểm mới giữ nguyên khối
     * lượng cũ của người đi phó bản (60 quái CĐRĐ, 80 quái Khí gas).
     */
    private static final int WEIGHT_MOB_TRONG_PHO_BAN = 2;

    private static nro.models.services.TaskService i;

    public static nro.models.services.TaskService gI() {
        if (i == null) {
            i = new nro.models.services.TaskService();
        }
        return i;
    }

    public TaskMain getTaskMainById(Player player, int id) {
        for (TaskMain tm : Manager.TASKS) {
            if (tm.id == id) {
                TaskMain newTaskMain = new TaskMain(tm);
                newTaskMain.detail = transformName(player, newTaskMain.detail);
                for (SubTaskMain stm : newTaskMain.subTasks) {
                    stm.mapId = (short) transformMapId(player, stm.mapId);
                    stm.npcId = (byte) transformNpcId(player, stm.npcId);
                    stm.notify = transformName(player, stm.notify);
                    stm.name = transformName(player, stm.name);
                }
                return newTaskMain;
            }
        }
        return player.playerTask.taskMain;
    }

    public void sendTaskMain(Player player) {
        Message msg = null;
        try {
            msg = new Message(40);
            msg.writer().writeShort(player.playerTask.taskMain.id);
            msg.writer().writeByte(player.playerTask.taskMain.index);
            msg.writer().writeUTF(player.playerTask.taskMain.name);
            msg.writer().writeUTF(player.playerTask.taskMain.detail);
            msg.writer().writeByte(player.playerTask.taskMain.subTasks.size());
            for (SubTaskMain stm : player.playerTask.taskMain.subTasks) {
                msg.writer().writeUTF(stm.name);
                msg.writer().writeByte(stm.npcId);
                msg.writer().writeShort(stm.mapId);
                if (stm.notify.isEmpty()) {
                    msg.writer().writeUTF("");
                } else {
                    msg.writer().writeUTF(stm.notify);
                }
            }
            msg.writer().writeShort(player.playerTask.taskMain.subTasks.get(player.playerTask.taskMain.index).count);
            for (SubTaskMain stm : player.playerTask.taskMain.subTasks) {
                msg.writer().writeShort(stm.maxCount);
            }
            player.sendMessage(msg);
        } catch (Exception e) {
            Logger.logException(TaskService.class, e);
        } finally {
            if (msg != null) {
                msg.cleanup();
            }
        }
    }

    // ======================================================================
    // TUYẾN MỚI — bảng chuyển tiếp task id (doc 27 §5)
    //   20 -> 21 · 48 -> 21 · 31 -> 32 · 49 -> 32 · 47 -> HẾT · 50 -> HẾT
    //   còn lại -> id + 1
    // Tuyến cũ rẽ theo hành tinh sau NV 3 (id == 3 -> gender + 4). Tuyến mới KHÔNG
    // rẽ theo hành tinh nữa: NV 3 và NV 10 chỉ khác nhau ở PHẦN THƯỞNG (cột gender
    // của task_main_reward), còn bước con dùng chung một bản ghi cho cả 3 hành tinh.
    // ======================================================================
    /**
     * Id nhiệm vụ tiếp theo, -1 nghĩa là hết tuyến.
     */
    public int getNextTaskMainId(int currentId) {
        switch (currentId) {
            case 20:
            case 48:
                return 21;
            case 31:
            case 49:
                return 32;
            case 47:
            case 50:
                return -1; // kết tuyến — giữ nguyên taskMain để người chơi xem lại
            default:
                return currentId + 1;
        }
    }

    public void sendNextTaskMain(Player player) {
        if (player == null || player.playerTask == null || player.playerTask.taskMain == null) {
            return;
        }
        int doneId = player.playerTask.taskMain.id;
        rewardDoneTask(player);
        int nextId = getNextTaskMainId(doneId);
        if (nextId == -1) {
            // TUYẾN MỚI: NV 47 / NV 50 là hai kết của tuyến. KHÔNG gọi getTaskMainById(id + 1)
            // (48 là nhánh của NV 20, 51 không tồn tại). Giữ nguyên taskMain, chỉ báo kết thúc.
            player.playerTask.taskMain.index = player.playerTask.taskMain.subTasks.size() - 1;
            sendTaskMain(player);
            Service.gI().sendThongBao(player, "Bạn đã hoàn thành toàn bộ tuyến nhiệm vụ chính!");
            return;
        }
        TaskMain next = getTaskMainById(player, nextId);
        // FIX (rà soát 37): getTaskMainById trả về CHÍNH taskMain hiện tại khi không tìm thấy id.
        // Nếu không kiểm id, nhiệm vụ thiếu trong DB sẽ khiến người chơi bị đặt lại index = 0
        // của đúng nhiệm vụ vừa xong -> làm lại và NHẬN THƯỞNG LẠI vô hạn.
        if (next == null || next.id != nextId) {
            Service.gI().sendThongBao(player, "Không tìm thấy nhiệm vụ tiếp theo (id " + nextId
                    + "), vui lòng báo quản trị viên");
            Logger.error("sendNextTaskMain: thieu task id " + nextId + " trong Manager.TASKS\n");
            sendTaskMain(player);
            return;
        }
        player.playerTask.taskMain = next;
        player.playerTask.taskMain.index = 0;
        player.playerTask.taskMain.lastTime = 0;
        sendTaskMain(player);
        if (player.playerTask.taskMain.index < player.playerTask.taskMain.subTasks.size()) {
            Service.gI().sendThongBao(player, "Nhiệm vụ tiếp theo của bạn là "
                    + player.playerTask.taskMain.subTasks.get(player.playerTask.taskMain.index).name);
        }
        // Các bước "đạt X sức mạnh" phải kiểm lại NGAY khi vừa sang bước, nếu không
        // người chơi đã đủ sức mạnh sẽ đứng im cho tới lần powerUp kế tiếp.
        recheckPassiveSubTask(player);
    }

    public void sendUpdateCountSubTask(Player player) {
        Message msg = null;
        try {
            msg = new Message(43);
            msg.writer().writeShort(player.playerTask.taskMain.subTasks.get(player.playerTask.taskMain.index).count);
            player.sendMessage(msg);
        } catch (IOException e) {
        } finally {
            if (msg != null) {
                msg.cleanup();
            }
        }
    }

    public void sendNextSubTask(Player player) {
        Message msg = null;
        try {
            msg = new Message(41);
            player.sendMessage(msg);
        } catch (Exception e) {
        } finally {
            if (msg != null) {
                msg.cleanup();
            }
        }
        recheckPassiveSubTask(player);
    }

    public void sendInfoCurrentTask(Player player) {
        Service.gI().sendThongBao(player, "Nhiệm vụ hiện tại của bạn là "
                + player.playerTask.taskMain.subTasks.get(player.playerTask.taskMain.index).name);
    }

    /**
     * TUYẾN MỚI: các bước "thụ động" (mốc sức mạnh, đã đủ 7 viên ngọc…)
     * phải được kiểm lại ngay khi vừa bước sang, không chờ sự kiện kế tiếp.
     */
    public void recheckPassiveSubTask(Player player) {
        if (player == null || !player.isPl() || player.nPoint == null) {
            return;
        }
        int idTask = getIdTask(player);
        switch (idTask) {
            case ConstTask.TASK_10_2:
            case ConstTask.TASK_23_0:
            case ConstTask.TASK_31_5:
            case ConstTask.TASK_33_4:
            case ConstTask.TASK_49_6:
                checkDoneTaskPower(player, player.nPoint.power);
                break;
            // doc 43: bỏ B8 (TASK_12_0 "có đệ tử") — NV 12 không còn bước đệ tử.
            case ConstTask.TASK_39_1:
                checkDoneTaskCollect7Stars(player);
                break;
            default:
                break;
        }
    }

    // ======================================================================
    // A3 — NÓI CHUYỆN NPC
    // Bảng viết lại theo doc 27 §4. Hai sửa lỗi cũ:
    //  - TASK_13_0 (gia nhập bang) KHÔNG còn nằm ở đây, nó thuộc checkDoneTaskJoinClan.
    //  - Không còn khối QUY_LAO_KAME lọt ra ngoài ngoặc kiểm tra gender.
    // ======================================================================
    /**
     * Nói chuyện với NPC. Trả true nếu vừa hoàn thành một bước nhiệm vụ.
     *
     * FIX "Xin chờ" quay mãi: khi hàm này trả true, lớp NPC KHÔNG mở menu nữa (mẫu chung
     * {@code if (!checkDoneTaskTalkNpc(...)) openMenu}). Client lúc bấm NPC đã hiện "Xin chờ"
     * và đợi server trả một hộp thoại. Các bước tuyến cũ luôn có câu thoại trong
     * {@link #doneTask}, nhưng nhiều bước tuyến mới thì không → client chờ mãi.
     * Nay nếu bước vừa xong mà chưa có hộp thoại nào được gửi, tự gửi một câu báo việc tiếp theo.
     */
    public boolean checkDoneTaskTalkNpc(Player player, Npc npc) {
        int before = nro.models.map.service.NpcService.DIALOG_COUNT.get()[0];
        boolean done = checkDoneTaskTalkNpcInner(player, npc);
        if (done && npc != null && player != null && nro.models.map.service.NpcService.DIALOG_COUNT.get()[0] == before) {
            NpcService.gI().createTutorial(player, npc.tempId, npc.avartar, nextStepText(player));
        }
        return done;
    }

    /** Câu báo "việc tiếp theo" dùng khi bước vừa xong không có lời thoại riêng. */
    private String nextStepText(Player player) {
        try {
            TaskMain tm = player.playerTask.taskMain;
            if (tm != null && tm.subTasks != null && tm.index >= 0 && tm.index < tm.subTasks.size()) {
                return "Tốt lắm!\nViệc tiếp theo: " + tm.subTasks.get(tm.index).name;
            }
        } catch (Exception ignored) {
        }
        return "Tốt lắm! Con đã hoàn thành nhiệm vụ.";
    }

    private boolean checkDoneTaskTalkNpcInner(Player player, Npc npc) {
        if (player == null || npc == null || !player.isPl()) {
            return false;
        }
        int mapId = (player.zone != null && player.zone.map != null) ? player.zone.map.mapId : -1;
        switch (npc.tempId) {
            // ---- Ông nội theo hành tinh: 0 Gôhan / 2 Moori / 1 Paragus -------
            case ConstNpc.ONG_GOHAN:
            case ConstNpc.ONG_MOORI:
            case ConstNpc.ONG_PARAGUS: {
                // TUYẾN MỚI: BẮT BUỘC đúng ông của hành tinh mình (lỗi cũ không kiểm)
                if (npc.tempId != transformNpcId(player, ConstTask.NPC_NHA)) {
                    return false;
                }
                // NV 0–3: khôi phục NGUYÊN cơ chế tuyến gốc (doc 39) — client hướng dẫn
                // tân thủ viết cứng theo đúng các bước này.
                return doneTask(player, ConstTask.TASK_0_2)
                        || doneTask(player, ConstTask.TASK_0_5)
                        || doneTask(player, ConstTask.TASK_1_1)
                        || doneTask(player, ConstTask.TASK_2_1)
                        || doneTask(player, ConstTask.TASK_3_2)
                        || doneTask(player, ConstTask.TASK_4_2)
                        || doneTask(player, ConstTask.TASK_5_2)
                        || doneTask(player, ConstTask.TASK_6_2)
                        || doneTask(player, ConstTask.TASK_11_2)
                        || doneTask(player, ConstTask.TASK_12_2);
            }
            // ---- Quy Lão / Trưởng lão Guru / Vua Vegeta ----------------------
            case ConstNpc.QUY_LAO_KAME:
            case ConstNpc.TRUONG_LAO_GURU:
            case ConstNpc.VUA_VEGETA: {
                // npc.tempId == 13 + player.gender — BẮT BUỘC kiểm gender
                if (npc.tempId != transformNpcId(player, ConstTask.NPC_QUY_LAO)) {
                    return false;
                }
                return doneTask(player, ConstTask.TASK_9_2)
                        || doneTask(player, ConstTask.TASK_10_0)
                        || doneTask(player, ConstTask.TASK_16_4)
                        || doneTask(player, ConstTask.TASK_17_3)
                        || doneTask(player, ConstTask.TASK_21_3)
                        || doneTask(player, ConstTask.TASK_23_5)
                        || doneTask(player, ConstTask.TASK_26_4);
            }
            // ---- 63 Jaco ----------------------------------------------------
            case ConstNpc.JACO: {
                // doc 39: bỏ TASK_3_0 "Gặp Jaco ở vách núi" — NV 3 đã trả về cơ chế gốc.
                if (isMapTTVT(player, mapId)) {
                    // doc 43: NV 12 "Bạn đồng hành" — bạn đồng hành là Jaco, không phải đệ tử.
                    return doneTask(player, ConstTask.TASK_7_2)
                            || doneTask(player, ConstTask.TASK_12_0)
                            || doneTask(player, ConstTask.TASK_15_2);
                }
                if (mapId == 24) { // Jaco của nhánh 48 chỉ đứng ở map 24
                    return doneTask(player, ConstTask.TASK_48_2)
                            || doneTask(player, ConstTask.TASK_48_5);
                }
                return false;
            }
            // ---- 7 Bunma (Siêu thị 84) --------------------------------------
            case ConstNpc.BUNMA: {
                if (mapId == 84) {
                    return doneTask(player, ConstTask.TASK_8_0);
                }
                return false;
            }
            // ---- 47 Giu-ma Đầu Bò (map 153) ---------------------------------
            case ConstNpc.GIUMA_DAU_BO: {
                if (mapId == 153) {
                    return doneTask(player, ConstTask.TASK_13_2);
                }
                return false;
            }
            // ---- 37 Bunma tương lai (Nhà Bunma 102) -------------------------
            case ConstNpc.BUNMA_TL: {
                if (mapId != 102) {
                    return false;
                }
                return doneTask(player, ConstTask.TASK_14_0)
                        || doneTask(player, ConstTask.TASK_24_0)
                        || doneTask(player, ConstTask.TASK_24_4)
                        || doneTask(player, ConstTask.TASK_25_3)
                        || doneTask(player, ConstTask.TASK_28_4)
                        || doneTask(player, ConstTask.TASK_29_0)
                        || doneTask(player, ConstTask.TASK_30_4)
                        || doneTask(player, ConstTask.TASK_32_0)
                        || doneTask(player, ConstTask.TASK_38_0)
                        || doneTask(player, ConstTask.TASK_38_5);
            }
            // ---- 21 Bà Hạt Mít ----------------------------------------------
            case ConstNpc.BA_HAT_MIT: {
                if (isMapVachNuiLang(player, mapId)) {
                    return doneTask(player, ConstTask.TASK_17_0);
                }
                if (mapId == 5) {
                    return doneTask(player, ConstTask.TASK_26_0);
                }
                return false;
            }
            // ---- 53 Tapion --------------------------------------------------
            case ConstNpc.TAPION: {
                if (mapId == 19) {
                    return doneTask(player, ConstTask.TASK_18_1);
                }
                if (mapId == 126) {
                    return doneTask(player, ConstTask.TASK_18_4)
                            || doneTask(player, ConstTask.TASK_22_0)
                            || doneTask(player, ConstTask.TASK_22_4);
                }
                return false;
            }
            // ---- 12 Cui (Thung lũng Nappa 68) -------------------------------
            case ConstNpc.CUI: {
                if (mapId == 68) {
                    return doneTask(player, ConstTask.TASK_19_0)
                            || doneTask(player, ConstTask.TASK_19_4);
                }
                return false;
            }
            // ---- 71 Berry (Khu hang động 160) — chưa có hằng ConstNpc -------
            case NPC_BERRY: {
                if (mapId == 160) {
                    return doneTask(player, ConstTask.TASK_20_0)
                            || doneTask(player, ConstTask.TASK_48_0);
                }
                return false;
            }
            // ---- 76 Granola (Khu hang động 160) — chưa có hằng ConstNpc -----
            case NPC_GRANOLA: {
                if (mapId == 160) {
                    return doneTask(player, ConstTask.TASK_20_2)
                            || doneTask(player, ConstTask.TASK_20_5);
                }
                return false;
            }
            // ---- 25 Lính canh (Rừng Bamboo 27) ------------------------------
            case ConstNpc.LINH_CANH: {
                if (mapId == 27) {
                    // ĐƯỜNG VÒNG (doc 32): người chơi KHÔNG CÓ BANG không vào được map 57
                    // để gặp Độc Nhãn (26), nên Lính canh nhận luôn bản đồ hành quân lột
                    // từ xác lính tuần tra mà người chơi vừa dọn ở cụm map 63–67.
                    // Người có bang vẫn có thể lấy ở map 57 như cũ — hai cửa cùng một bước.
                    return doneTask(player, ConstTask.TASK_21_0)
                            || doneTask(player, ConstTask.TASK_21_2);
                }
                return false;
            }
            // ---- 26 Độc Nhãn (map 57, chỉ khi đã phá xong doanh trại) -------
            case ConstNpc.DOC_NHAN: {
                if (mapId == 57 && isWinDoanhTrai(player)) {
                    return doneTask(player, ConstTask.TASK_21_2);
                }
                return false;
            }
            // ---- 38 Ca Lích (Nhà Bunma 102) ---------------------------------
            case ConstNpc.CALICK: {
                if (mapId == 102) {
                    return doneTask(player, ConstTask.TASK_27_0)
                            || doneTask(player, ConstTask.TASK_27_3);
                }
                return false;
            }
            // ---- 83 Dr. Myuu (Phòng thí nghiệm 166) -------------------------
            case ConstNpc.DR_MYUU: {
                if (mapId == 166) {
                    return doneTask(player, ConstTask.TASK_29_3)
                            || doneTask(player, ConstTask.TASK_46_0);
                }
                return false;
            }
            // ---- 62 Potage (map 140) ----------------------------------------
            case ConstNpc.POTAGE: {
                if (mapId == 140) {
                    return doneTask(player, ConstTask.TASK_31_0)
                            || doneTask(player, ConstTask.TASK_49_0);
                }
                return false;
            }
            // ---- 70 Bardock --------------------------------------------------
            case ConstNpc.BARDOCK: {
                if (mapId == 160) {
                    return doneTask(player, ConstTask.TASK_32_2)
                            || doneTask(player, ConstTask.TASK_32_5)
                            || doneTask(player, ConstTask.TASK_34_5)
                            || doneTask(player, ConstTask.TASK_39_0);
                }
                if (mapId == 14) {
                    return doneTask(player, ConstTask.TASK_39_5);
                }
                return false;
            }
            // ---- 42 Quốc Vương (vách núi 42/43/44) --------------------------
            case ConstNpc.QUOC_VUONG: {
                if (isMapVachNuiLang(player, mapId)) {
                    return doneTask(player, ConstTask.TASK_33_1)
                            || doneTask(player, ConstTask.TASK_33_5);
                }
                return false;
            }
            // ---- 20 Thần Vũ Trụ (map 48) ------------------------------------
            case ConstNpc.THAN_VU_TRU: {
                if (mapId == 48) {
                    return doneTask(player, ConstTask.TASK_35_0)
                            || doneTask(player, ConstTask.TASK_40_0);
                }
                return false;
            }
            // ---- 19 Thượng Đế (Thần điện 45) --------------------------------
            case ConstNpc.THUONG_DE: {
                if (mapId == 45) {
                    return doneTask(player, ConstTask.TASK_35_4)
                            || doneTask(player, ConstTask.TASK_43_5);
                }
                return false;
            }
            // ---- 44 Ôsin ----------------------------------------------------
            case ConstNpc.OSIN: {
                switch (mapId) {
                    case 52:
                        return doneTask(player, ConstTask.TASK_36_0)
                                || doneTask(player, ConstTask.TASK_37_0);
                    case 165: // đường vòng 24/7 của NV 36
                        return doneTask(player, ConstTask.TASK_36_4);
                    case 50:
                        return doneTask(player, ConstTask.TASK_42_0)
                                || doneTask(player, ConstTask.TASK_44_0);
                    case 155:
                        return doneTask(player, ConstTask.TASK_42_5)
                                || doneTask(player, ConstTask.TASK_50_4);
                    default:
                        return false;
                }
            }
            // ---- 46 Babiđây (Cửa Ải 1 — map 117) ----------------------------
            case ConstNpc.BABIDAY: {
                if (mapId == 117) {
                    return doneTask(player, ConstTask.TASK_36_4);
                }
                return false;
            }
            // ---- 45 Kibit (Thánh địa Kaio 50) -------------------------------
            case ConstNpc.KIBIT: {
                if (mapId == 50) {
                    return doneTask(player, ConstTask.TASK_37_4)
                            || doneTask(player, ConstTask.TASK_40_4);
                }
                return false;
            }
            // ---- 29 Rồng Omega (Trạm tàu vũ trụ) ----------------------------
            case ConstNpc.RONG_OMEGA: {
                if (isMapTTVT(player, mapId)) {
                    return doneTask(player, ConstTask.TASK_39_3);
                }
                return false;
            }
            // ---- 43 Tổ Sư Kaio (Thánh địa Kaio 50) --------------------------
            case ConstNpc.TO_SU_KAIO: {
                if (mapId == 50) {
                    return doneTask(player, ConstTask.TASK_40_2)
                            || doneTask(player, ConstTask.TASK_41_0)
                            || doneTask(player, ConstTask.TASK_41_4)
                            || doneTask(player, ConstTask.TASK_47_4);
                }
                return false;
            }
            // ---- 67 Mr Popo (Làng Aru — map 0) ------------------------------
            case ConstNpc.MR_POPO: {
                if (mapId == 0) {
                    return doneTask(player, ConstTask.TASK_43_0);
                }
                return false;
            }
            // ---- 55 Bill (map 154) ------------------------------------------
            case ConstNpc.BILL: {
                if (mapId == 154) {
                    return doneTask(player, ConstTask.TASK_44_1);
                }
                return false;
            }
            // ---- 56 Whis (map 154) ------------------------------------------
            case ConstNpc.WHIS: {
                if (mapId == 154) {
                    return doneTask(player, ConstTask.TASK_44_5);
                }
                return false;
            }
            // ---- 64 Thiên Sứ Whis -------------------------------------------
            case ConstNpc.DAI_THIEN_SU: {
                if (mapId == 78) {
                    return doneTask(player, ConstTask.TASK_45_4);
                }
                if (mapId == 145) {
                    return doneTask(player, ConstTask.TASK_46_5);
                }
                return false;
            }
            default:
                return false;
        }
    }

    // ======================================================================
    // A10 — GIA NHẬP BANG HỘI
    // ======================================================================
    public void checkDoneTaskJoinClan(Player player) {
        if (player != null && player.isPl() && player.clan != null) {
            doneTask(player, ConstTask.TASK_13_0);
        }
    }

    // ======================================================================
    // A8 — LẤY ĐỒ TRONG RƯƠNG
    // ======================================================================
    public void checkDoneTaskGetItemBox(Player player) {
        if (player != null && player.isPl()) {
            // doc 39: tuyến gốc — "Mở rương đồ" là bước 3 của NV 0
            doneTask(player, ConstTask.TASK_0_3);
        }
    }

    // ======================================================================
    // A5 — MỐC SỨC MẠNH
    // Lỗi cũ đã bỏ: dòng "power >= 40000 -> TASK_11_0" thừa (trùng mốc của TASK_8_0
    // và mâu thuẫn với mốc 500.000 ngay bên dưới).
    // ======================================================================
    public void checkDoneTaskPower(Player player, long power) {
        if (player == null || !player.isPl()) {
            return;
        }
        if (power >= 250_000L) {
            doneTask(player, ConstTask.TASK_10_2);
        }
        if (power >= 80_000_000L) {
            doneTask(player, ConstTask.TASK_23_0);
        }
        if (power >= 2_000_000_000L) {
            doneTask(player, ConstTask.TASK_31_5);
            doneTask(player, ConstTask.TASK_49_6);
        }
        if (power >= 3_000_000_000L) {
            doneTask(player, ConstTask.TASK_33_4);
        }
    }

    // ======================================================================
    // A7 — CỘNG ĐIỂM TIỀM NĂNG LẦN ĐẦU
    // ======================================================================
    public void checkDoneTaskUseTiemNang(Player player) {
        if (player != null && player.isPl()) {
            // doc 39: tuyến gốc — "Sử dụng tiềm năng" là bước 0 của NV 3
            doneTask(player, ConstTask.TASK_3_0);
        }
    }

    // ======================================================================
    // A11 — NÂNG SỨC ĐÁNH GỐC  (NPoint.increasePoint type 2)
    // ======================================================================
    public void checkDoneTaskNangCS(Player player) {
        if (player == null || !player.isPl() || player.nPoint == null) {
            return;
        }
        // doc 39: đã bỏ mốc "dameg >= 30 -> TASK_3_2". TASK_3_2 nay lại là bước
        // "Báo cáo với ông" của tuyến gốc (nói chuyện NPC), không được hoàn thành ở đây.
        // Hiện không bước nào của tuyến mới dùng trigger này.
    }

    // ======================================================================
    // B11 — NÂNG CHỈ SỐ GỐC CHẠM TRẦN  (NPoint.increasePoint mọi type)
    // type: 0 = HP, 1 = KI, 2 = sức đánh, 3 = giáp, 4 = chí mạng
    // ======================================================================
    public void checkDoneTaskBasePoint(Player player, int type) {
        if (player == null || !player.isPl() || player.nPoint == null) {
            return;
        }
        if (type == 2) {
            checkDoneTaskNangCS(player);
            return;
        }
        if (type == 0 && isCurrentTask(player, ConstTask.TASK_33_2)) {
            if (player.nPoint.hpg >= player.nPoint.getHpMpLimit()) {
                doneTask(player, ConstTask.TASK_33_2);
            }
        }
    }

    // ======================================================================
    // A12 — DÙNG VẬT PHẨM  (services_func/UseItem.java)
    // Trả về true nếu vật phẩm đã bị tiêu vào nhiệm vụ (nơi gọi không trừ lại nữa).
    // ======================================================================
    public void checkDoneTaskUseItem(Player player, Item item) {
        if (player == null || !player.isPl() || item == null || !item.isNotNullItem()) {
            return;
        }
        int mapId = (player.zone != null && player.zone.map != null) ? player.zone.map.mapId : -1;
        switch (item.template.id) {
            // doc 39: bỏ "case 13 (ăn Đậu thần) -> TASK_0_4". TASK_0_4 trả về bước gốc
            // "Thu hoạch đậu thần" (checkDoneTaskConfirmMenuNpc). Bước ăn đậu cũ bắt người mới
            // mở túi đồ trong lúc client còn ẩn giao diện -> kẹt vĩnh viễn.
            case 2010: // Kỷ Vật Của Ông — KHÔNG trừ ở bước này, trừ khi nộp ở TASK_5_2
                doneTask(player, ConstTask.TASK_5_1);
                break;
            case 2013: // Hạt Giống Hy Vọng — nâng cây đậu thần lên cấp 2
                if (isMapNha(player, mapId)) {
                    if (isCurrentTask(player, ConstTask.TASK_11_1)) {
                        if (player.magicTree != null && player.magicTree.level < 2) {
                            player.magicTree.level = 2;
                            player.magicTree.loadMagicTree();
                        }
                        subItem(player, 2013, 1);
                        doneTask(player, ConstTask.TASK_11_1);
                    }
                }
                break;
            case 2015: // Búa rèn cũ
                if (isCurrentTask(player, ConstTask.TASK_17_2)) {
                    subItem(player, 2015, 1);
                    doneTask(player, ConstTask.TASK_17_2);
                }
                break;
            case 2020: // Mẫu kim loại có ký ức — 3 dòng thoại rồi tự xóa
                if (isCurrentTask(player, ConstTask.TASK_26_3)) {
                    npcSay(player, ConstNpc.BA_HAT_MIT,
                            "Kim loại này còn ấm...\n"
                            + "Trong nó có tiếng người — tiếng của chính ngươi, từ một đời khác\n"
                            + "Ai đó đã chép ký ức của ngươi vào sắt thép");
                    subItem(player, 2020, 1);
                    doneTask(player, ConstTask.TASK_26_3);
                }
                break;
            case 992: // Nhẫn thời không sai lệch
                doneTask(player, ConstTask.TASK_32_1);
                break;
            case 2002: // Mảnh Ký Ức 1 — KHÔNG trừ, phải giữ tới NV 45
                if (mapId == 50) {
                    doneTask(player, ConstTask.TASK_40_3);
                }
                break;
            case 2024: // Lõi Ký Ức chưa hoàn chỉnh — lễ hợp nhất 7 mảnh
                if (isCurrentTask(player, ConstTask.TASK_45_3)) {
                    if (!hasAllMemoryShards(player)) {
                        Service.gI().sendThongBao(player,
                                "Bạn cần đủ 7 Mảnh Ký Ức (2002 đến 2008) mới làm lễ hợp nhất được");
                        break;
                    }
                    for (int id = 2002; id <= 2008; id++) {
                        subItem(player, id, 1);
                    }
                    subItem(player, 2024, 1);
                    addItemToBag(player, (short) 2000, 1);
                    doneTask(player, ConstTask.TASK_45_3);
                }
                break;
            case 2000: // Lõi Hư Không — hai nhánh kết
                if (mapId == 145) {
                    if (isCurrentTask(player, ConstTask.TASK_47_1)) {
                        // Nhánh A: trả lại — trừ Lõi, trao Vỏ Lõi rỗng
                        subItem(player, 2000, 1);
                        addItemToBag(player, (short) 2001, 1);
                        doneTask(player, ConstTask.TASK_47_1);
                    } else if (isCurrentTask(player, ConstTask.TASK_50_1)) {
                        // Nhánh B: hấp thụ — GIỮ NGUYÊN item 2000 làm cờ hậu truyện
                        doneTask(player, ConstTask.TASK_50_1);
                    }
                }
                break;
            case 638: // Bình chứa Commeson (nhánh NV 49)
                if (mapId == 103) {
                    doneTask(player, ConstTask.TASK_49_5);
                }
                break;
            default:
                break;
        }
    }

    // ======================================================================
    // A6 — VÀO MAP
    // ======================================================================
    public void checkDoneTaskGoToMap(Player player, Zone zoneJoin) {
        if (player == null || !player.isPl() || zoneJoin == null || zoneJoin.map == null) {
            return;
        }
        int mapId = zoneJoin.map.mapId;
        switch (mapId) {
            // doc 39: khôi phục NGUYÊN hai nhánh NV 0 của tuyến gốc. Hàm này được gọi cả từ
            // ChangeMapService (vào map) lẫn PlayerService.playerMove (mỗi lần di chuyển),
            // nên điều kiện x >= 635 được xét liên tục khi người chơi đi về phía mũi tên.
            case 39:
            case 40:
            case 41: // vách núi khởi đầu (PlayerDAO.createNewPlayer đặt ở 39 + gender)
                if (player.location != null && player.location.x >= 635) {
                    doneTask(player, ConstTask.TASK_0_0);
                }
                break;
            case 21:
            case 22:
            case 23: // MAP_NHA
                doneTask(player, ConstTask.TASK_0_1);
                break;
            case 24:
            case 25:
            case 26: // MAP_TTVT
                if (isMapTTVT(player, mapId)) {
                    doneTask(player, ConstTask.TASK_7_1);
                }
                break;
            case 5:
            case 13:
            case 20: // MAP_QUY_LAO
                if (isMapQuyLao(player, mapId)) {
                    doneTask(player, ConstTask.TASK_9_0);
                }
                break;
            case 29: // Nam TĐ
                if (player.gender == ConstPlayer.TRAI_DAT) {
                    doneTask(player, ConstTask.TASK_16_0);
                }
                break;
            case 33: // Nam NM
                if (player.gender == ConstPlayer.NAMEC) {
                    doneTask(player, ConstTask.TASK_16_0);
                }
                break;
            case 37: // Nam XD
                if (player.gender == ConstPlayer.XAYDA) {
                    doneTask(player, ConstTask.TASK_16_0);
                }
                break;
            case 19:
                doneTask(player, ConstTask.TASK_18_0);
                break;
            case 126:
                doneTask(player, ConstTask.TASK_18_3);
                break;
            case 80:
                doneTask(player, ConstTask.TASK_23_1);
                break;
            case 92:
                doneTask(player, ConstTask.TASK_24_1);
                break;
            case 96:
                doneTask(player, ConstTask.TASK_25_0);
                break;
            case 104:
                doneTask(player, ConstTask.TASK_27_1);
                break;
            case 97:
                doneTask(player, ConstTask.TASK_28_0);
                break;
            case 166:
                doneTask(player, ConstTask.TASK_29_1);
                // TUYẾN MỚI: đồng hồ 6 phút của NV 29 bước 2 bắt đầu khi vừa vào map
                startTimedSubTask(player, ConstTask.TASK_29_2);
                break;
            case 100:
                doneTask(player, ConstTask.TASK_30_0);
                break;
            case 103:
                doneTask(player, ConstTask.TASK_31_2);
                doneTask(player, ConstTask.TASK_49_2);
                break;
            case 42:
            case 43:
            case 44: // MAP_VACH_NUI_LANG (-10) — doc 39
                if (isMapVachNuiLang(player, mapId)) {
                    doneTask(player, ConstTask.TASK_33_0);
                }
                break;
            case 105:
                doneTask(player, ConstTask.TASK_34_0);
                break;
            case 114:
            case 165: // Cổng phi thuyền (khung 12h) HOẶC Sa mạc hoang vu (đường vòng 24/7)
                doneTask(player, ConstTask.TASK_36_1);
                break;
            case 117:
                doneTask(player, ConstTask.TASK_36_3);
                break;
            case 50:
                doneTask(player, ConstTask.TASK_40_1);
                doneTask(player, ConstTask.TASK_47_2);
                break;
            case 155:
                doneTask(player, ConstTask.TASK_42_1);
                doneTask(player, ConstTask.TASK_50_2);
                break;
            case 78:
                doneTask(player, ConstTask.TASK_45_0);
                break;
            case 145:
                doneTask(player, ConstTask.TASK_46_2);
                break;
            default:
                break;
        }
        // TUYẾN MỚI: rời map 166 -> đồng hồ NV 29 bước 2 reset về 0
        if (mapId != 166 && isCurrentTask(player, ConstTask.TASK_29_2)) {
            resetTimedSubTask(player);
        }
    }

    // ======================================================================
    // A4 — NHẶT VẬT PHẨM
    // ======================================================================
    public void checkDoneTaskPickItem(Player player, ItemMap item) {
        if (player == null || !player.isPl() || item == null || item.itemTemplate == null) {
            return;
        }
        switch (item.itemTemplate.id) {
            // doc 39: khôi phục vật phẩm nhiệm vụ gốc của NV 2 / NV 3.
            // (Bỏ "2009 Mảnh Vỡ Hư Không -> TASK_2_2": NV 2 gốc chỉ có 2 bước.)
            case 73: // đùi gà — Mob.dropItemTask rơi khi TASK_2_0
                doneTask(player, ConstTask.TASK_2_0);
                break;
            case 78: // "đứa bé" / vật thể lạ — Map.initItem rải sẵn, Zone chỉ hiện khi TASK_3_1
                doneTask(player, ConstTask.TASK_3_1);
                break;
            case 2010: // Kỷ Vật Của Ông
                doneTask(player, ConstTask.TASK_5_0);
                break;
            case 2014: // Vỏ đạn khắc dấu
                doneTask(player, ConstTask.TASK_16_3);
                break;
            case 2016: // Thẻ tiền thưởng Granola
                doneTask(player, ConstTask.TASK_20_4);
                break;
            case 2017: // Biên bản truy nã Ngân Hà (nhánh 48)
                doneTask(player, ConstTask.TASK_48_4);
                break;
            case 2018: // Máy đo ký ức
                doneTask(player, ConstTask.TASK_22_3);
                break;
            case 2019: // Lõi năng lượng Android
                doneTask(player, ConstTask.TASK_25_2);
                break;
            case 2021: // Mảnh giáp khắc tên
                doneTask(player, ConstTask.TASK_28_3);
                break;
            case 2023: // Bản thiết kế bản sao — bước có giới hạn 6 phút
                if (isCurrentTask(player, ConstTask.TASK_29_2)) {
                    if (updateTimedSubTask(player, ConstTask.TASK_29_2)) {
                        doneTask(player, ConstTask.TASK_29_2);
                    }
                }
                break;
            case 2025: // Mảnh Ký Ức Vỡ
                doneTask(player, ConstTask.TASK_32_4);
                break;
            case 2026: // Mảnh Ký Ức Đóng Băng
                doneTask(player, ConstTask.TASK_34_3);
                break;
            case 2027: // Mảnh Bùa Babiđây — cửa thứ hai của NV 36 bước 3
                doneTask(player, ConstTask.TASK_36_3);
                break;
            case 2028: // Lõi Phép Babiđây
                doneTask(player, ConstTask.TASK_37_3);
                break;
            case 2008: // Mảnh Ký Ức 7
                doneTask(player, ConstTask.TASK_45_1);
                break;
            case 992: // Nhẫn thời không sai lệch rơi từ Black Goku
                doneTask(player, ConstTask.TASK_38_4);
                break;
            case 14:
            case 15:
            case 16:
            case 17:
            case 18:
            case 19:
            case 20: // 7 viên Ngọc Rồng
                checkDoneTaskCollect7Stars(player);
                break;
            case 77:
                AchievementService.gI().checkDoneTask(player, ConstAchievement.TRUM_NHAT_NGOC);
                break;
            default:
                break;
        }
        Service.gI().sendFlagBag(player);
    }

    /**
     * TUYẾN MỚI (TASK_39_1): "gom đủ 7 viên Ngọc Rồng" — mỗi loại 14..20 tính đúng 1 lần,
     * đếm lại từ hành trang chứ không cộng dồn theo lượt nhặt.
     */
    public void checkDoneTaskCollect7Stars(Player player) {
        if (player == null || !player.isPl() || !isCurrentTask(player, ConstTask.TASK_39_1)) {
            return;
        }
        int have = 0;
        for (int id = 14; id <= 20; id++) {
            if (InventoryService.gI().findItemBag(player, id) != null) {
                have++;
            }
        }
        SubTaskMain stm = getCurrentSubTask(player);
        if (stm == null) {
            return;
        }
        if (have > stm.count) {
            addDoneSubTask(player, have - stm.count);
        }
    }

    /**
     * Giữ lại cho tương thích: tuyến cũ dùng cho bước "tìm ngọc 7 sao".
     */
    public void checkDoneTaskFind7Stars(Player player) {
        checkDoneTaskCollect7Stars(player);
    }

    // ======================================================================
    // A9 / B14 — CHỌN MỤC MENU NPC
    // ======================================================================
    public void checkDoneTaskConfirmMenuNpc(Player player, Npc npc, byte select) {
        if (player == null || npc == null || !player.isPl()) {
            return;
        }
        int mapId = (player.zone != null && player.zone.map != null) ? player.zone.map.mapId : -1;
        switch (npc.tempId) {
            // ---- A9: xem cây đậu thần (NPC 4), chọn bất kỳ mục menu nào ----
            case ConstNpc.DAU_THAN: {
                // doc 39: tuyến gốc — "Thu hoạch đậu thần" (TASK_0_4) chỉ xong khi bấm mục 0
                // của menu cây còn đậu / đầy đậu.
                switch (player.idMark.getIndexMenu()) {
                    case ConstNpc.MAGIC_TREE_NON_UPGRADE_LEFT_PEA, ConstNpc.MAGIC_TREE_NON_UPGRADE_FULL_PEA -> {
                        if (select == 0) {
                            doneTask(player, ConstTask.TASK_0_4);
                        }
                    }
                    default -> {
                    }
                }
                break;
            }
            // ---- B14 điểm rẽ 1: NPC 71 Berry, map 160, bước TASK_20_1 -------
            case NPC_BERRY: {
                if (mapId == 160 && isCurrentTask(player, ConstTask.TASK_20_1)) {
                    if (select == 0) {
                        doneTask(player, ConstTask.TASK_20_1); // đi theo Granola -> giữ task 20
                    } else {
                        switchTaskBranch(player, 48); // báo Jaco -> nhánh 48
                    }
                }
                break;
            }
            // ---- B14 điểm rẽ 2: NPC 62 Potage, map 140, bước TASK_31_1 ------
            case ConstNpc.POTAGE: {
                if (mapId == 140 && isCurrentTask(player, ConstTask.TASK_31_1)) {
                    if (select == 0) {
                        doneTask(player, ConstTask.TASK_31_1); // tiêu diệt -> giữ task 31
                    } else {
                        switchTaskBranch(player, 49); // thu nhận -> nhánh 49
                    }
                }
                break;
            }
            // ---- B14 điểm rẽ 3: NPC 64 Thiên Sứ Whis, map 145, TASK_47_0 ----
            case ConstNpc.DAI_THIEN_SU: {
                if (mapId == 145 && isCurrentTask(player, ConstTask.TASK_47_0)) {
                    if (select == 0) {
                        doneTask(player, ConstTask.TASK_47_0); // trả ký ức -> giữ task 47
                    } else {
                        switchTaskMain(player, 50, 1); // giữ Lõi -> task 50 index 1
                    }
                }
                break;
            }
            default:
                break;
        }
    }

    // ======================================================================
    // A2 — HẠ BOSS
    // Bao gồm cả boss bản nhiệm vụ mới (-2100…-2105) và Heart (-108108).
    // ======================================================================
    public void checkDoneTaskKillBoss(Player player, Boss boss) {
        if (player == null || boss == null || !player.isPl()) {
            return;
        }
        AchievementService.gI().checkDoneTask(player, ConstAchievement.TRUM_KET_LIEU_BOSS);
        int mapId = (player.zone != null && player.zone.map != null) ? player.zone.map.mapId : -1;

        // ---- Bản sao của chính người chơi (NV 31 / NV 49) --------------------
        if (boss.id == Util.createIdBossClone((int) player.id)) {
            doneTask(player, ConstTask.TASK_31_4);
            doneTask(player, ConstTask.TASK_49_4);
            return;
        }

        switch ((int) boss.id) {
            // ---- Chương 1–2: hai boss mới của tuyến ------------------------
            case BOSS_KE_THU_GOM: // -2000
                doneTask(player, ConstTask.TASK_6_1);
                break;
            case BOSS_JACO_VO_THUC: // -2001
                if (boss.currentLevel == 1) {
                    doneTask(player, ConstTask.TASK_15_1);
                }
                break;
            // ---- NV 20 / NV 48: 3 tay chân của Fide, đếm CHUNG -------------
            case BossID.KUKU:
            case BossID.MAP_DAU_DINH:
            case BossID.RAMBO:
                doneTask(player, ConstTask.TASK_20_3);
                doneTask(player, ConstTask.TASK_48_3);
                break;
            // ---- NV 22: Tiểu đội sát thủ, đếm CHUNG 5 con -----------------
            case BossID.SO_4:
            case BossID.SO_3:
            case BossID.SO_2:
            case BossID.SO_1:
            case BossID.TIEU_DOI_TRUONG:
            // FIX: tính cả Tiểu đội sát thủ bản Namek — trước đây chỉ bản Trái Đất được tính,
            // người chơi hạ bản Namek (map 73–77) không nhận tiến độ.
            case BossID.SO_4_NM:
            case BossID.SO_3_NM:
            case BossID.SO_2_NM:
            case BossID.SO_1_NM:
            case BossID.TIEU_DOI_TRUONG_NM:
                doneTask(player, ConstTask.TASK_22_2);
                break;
            // ---- NV 23: Fide đại ca ---------------------------------------
            case BossID.FIDE:
                // Mã nguồn dùng currentLevel 0/1/2 (doc 27 ghi 1/2/3) — tin mã nguồn.
                if (boss.currentLevel == 2) {
                    doneTask(player, ConstTask.TASK_23_4);
                } else {
                    doneTask(player, ConstTask.TASK_23_3);
                }
                break;
            // ---- NV 25: Android 19 + Dr.Kôrê, đếm CHUNG -------------------
            case BossID.ANDROID_19:
            case BossID.DR_KORE:
                doneTask(player, ConstTask.TASK_25_1);
                break;
            // ---- NV 27: ba cỗ máy mẫu, đếm CHUNG --------------------------
            case BossID.ANDROID_13:
            case BossID.ANDROID_14:
            case BossID.ANDROID_15:
                doneTask(player, ConstTask.TASK_27_2);
                break;
            // ---- NV 28: Poc, Pic, King Kong, đếm CHUNG --------------------
            case BossID.POC:
            case BossID.PIC:
            case BossID.KING_KONG:
                doneTask(player, ConstTask.TASK_28_2);
                break;
            // ---- NV 30: Xên bọ hung (bản thế giới và bản nhiệm vụ) --------
            case BossID.XEN_BO_HUNG:
            case BossID.XEN_BO_HUNG_NV:
                if (boss.currentLevel == 2) {
                    doneTask(player, ConstTask.TASK_30_3);
                } else {
                    doneTask(player, ConstTask.TASK_30_2);
                }
                break;
            // ---- NV 34: Cooler --------------------------------------------
            case BossID.COOLER:
            case BossID.COOLER_NV:
                doneTask(player, ConstTask.TASK_34_4);
                break;
            // ---- NV 36 bước 2: Drabura -> cộng trọn gói 20 ----------------
            case BossID.DRABURA:
                if (isCurrentTask(player, ConstTask.TASK_36_2)) {
                    addDoneSubTask(player, 20);
                }
                break;
            // ---- NV 37 bước 1: Mabư ---------------------------------------
            case BossID.MABU_12H:
                doneTask(player, ConstTask.TASK_37_1);
                break;
            case BossID.MABU:
            case BossID.MABU_14H_NV:
                if (boss.currentLevel == 4) {
                    doneTask(player, ConstTask.TASK_37_1);
                }
                break;
            // ---- NV 37 bước 2: Drabura 3 / Super Bư -> cộng trọn gói 30 ---
            case BossID.DRABURA_3:
            case BossID.SUPERBU:
                if (isCurrentTask(player, ConstTask.TASK_37_2)) {
                    addDoneSubTask(player, 30);
                }
                break;
            // ---- NV 38: Black Goku (2 bước ĐỘC LẬP) -----------------------
            case BossID.BLACK_GOKU:
            case BossID.BLACK_GOKU_NV:
                if (boss.currentLevel == 1) {
                    doneTask(player, ConstTask.TASK_38_3);
                } else {
                    doneTask(player, ConstTask.TASK_38_2);
                }
                break;
            // ---- NV 39: Baby cả ba dạng, đếm CHUNG ------------------------
            case BossID.BABY:
            case BossID.BABY_NV:
                doneTask(player, ConstTask.TASK_39_6);
                break;
            // ---- NV 41: Broly / Super Broly -------------------------------
            case BossID.BROLY:
                doneTask(player, ConstTask.TASK_41_2);
                break;
            case BossID.SUPER_BROLY:
                doneTask(player, ConstTask.TASK_41_3);
                break;
            // ---- NV 42: Cumber (2 bước ĐỘC LẬP) ---------------------------
            case BossID.CUMBER:
            case BossID.CUMBER_NV:
                if (boss.currentLevel == 1) {
                    doneTask(player, ConstTask.TASK_42_4);
                } else {
                    doneTask(player, ConstTask.TASK_42_3);
                }
                break;
            // ---- NV 43: Dr Lychee / Hatchiyack ----------------------------
            // max_count của hai bước này đã nâng lên 50 / 70 để chứa đường vòng
            // (doc 32), nên hạ boss phải cộng TRỌN GÓI một lần — giống cách
            // Drabura cộng trọn 20 ở TASK_36_2 (doc 27 §6.1).
            case BossID.DR_LYCHEE:
                doneTaskAtOnce(player, ConstTask.TASK_43_2);
                break;
            case BossID.HATCHIYACK:
                doneTaskAtOnce(player, ConstTask.TASK_43_3);
                break;
            // ---- NV 44: Whis ----------------------------------------------
            case BossID.WHIS:
                doneTask(player, ConstTask.TASK_44_3);
                break;
            // ---- NV 46 / 47 / 50: Heart 4 hình dạng -----------------------
            case BossID.HEART:
                switch (boss.currentLevel) {
                    case 0:
                        doneTask(player, ConstTask.TASK_46_1);
                        break;
                    case 1:
                        doneTask(player, ConstTask.TASK_46_3);
                        break;
                    case 2:
                        doneTask(player, ConstTask.TASK_46_4);
                        break;
                    case 3:
                        if (mapId == 155) {
                            doneTask(player, ConstTask.TASK_50_5);
                        } else {
                            doneTask(player, ConstTask.TASK_47_5);
                        }
                        break;
                    default:
                        break;
                }
                break;
            default:
                break;
        }
        // doc 42: rơi vật phẩm nhiệm vụ từ boss (bảng chung nro.models.task.QuestDrop).
        // Đặt SAU switch: đòn kết liễu vừa đưa người chơi sang bước "nhặt" thì rơi luôn.
        // Mọi boss (thế giới lẫn QuestBoss) đều gọi hàm này trong reward().
        nro.models.task.QuestDrop.onBossKilled(player, boss);
    }

    // ======================================================================
    // A1 — ĐÁNH QUÁI
    // RÕ MAP (docs/4-trien-khai/41): mọi bước đánh quái nay TÍNH THEO LOẠI QUÁI Ở BẤT KỲ
    // MAP NÀO quái đó xuất hiện. Trước đây nhiều bước chỉ đếm ở đúng một map (ví dụ NV 6
    // bước 0 chỉ đếm Thằn lằn bay ở Rừng xương, đánh ở Rừng nấm không tính) trong khi chữ
    // không nói gì về map — người chơi không thể biết.
    // Ràng buộc map CHỈ còn ở các ngoại lệ có lý do, đánh dấu "NGOẠI LỆ GIỮ MAP":
    //   * TASK_21_1 / TASK_35_2-3 / TASK_43_1-4: đường vòng thay phó bản (doc 32) — phải cày
    //     đúng cụm map thay thế, quái trong phó bản cộng 2 điểm.
    //   * TASK_41_1 "vành đai rừng": bước theo VÙNG (map 27–38) — nay MỌI quái trong vùng đều
    //     tính (trước chỉ 5 loài, Heo da xanh / Heo Xayda / Không tặc… không tính).
    //   * TASK_36_2: mob 95 dùng chung id với Thỏ con của sự kiện — chỉ tính ở map 165.
    // ======================================================================
    public void checkDoneTaskKillMob(Player player, Mob mob) {
        if (player == null || mob == null || !player.isPl()) {
            return;
        }
        int mapId = (player.zone != null && player.zone.map != null) ? player.zone.map.mapId : -1;
        // NGOẠI LỆ GIỮ MAP — NV 41 bước 1: mọi quái ở vành đai rừng 27–38 (cả 3 hành tinh).
        if (isMapVanhDaiRung(mapId)) {
            doneTask(player, ConstTask.TASK_41_1);
        }
        switch (mob.tempId) {
            case ConstMob.MOC_NHAN: // 0
                // doc 39: tuyến gốc — mộc nhân ở map nào cũng tính
                doneTask(player, ConstTask.TASK_1_0);
                break;
            case ConstMob.KHUNG_LONG: // 1
            case ConstMob.LON_LOI: // 2
            case ConstMob.QUY_DAT: // 3
                // doc 39: bỏ TASK_2_1 / TASK_3_1 ở đây — NV 2 gốc là NHẶT đùi gà (Mob.dropItemTask),
                // TASK_2_1 là "Báo cáo với ông", TASK_3_1 là nhặt vật thể lạ.
                // doc 41: bỏ ràng buộc map 2/9/16 — có ở đồi 1/8/15, thung lũng 2/9/16, rừng 3/11/17.
                doneTask(player, ConstTask.TASK_4_0);
                break;
            case ConstMob.KHUNG_LONG_ME: // 4
            case ConstMob.LON_LOI_ME: // 5
            case ConstMob.QUY_DAT_ME: // 6
                // doc 41: bỏ ràng buộc map 2/9/16 — có ở 2-4 / 9, 11, 12 / 16-18.
                doneTask(player, ConstTask.TASK_4_1);
                break;
            case ConstMob.THAN_LAN_BAY: // 7
            case ConstMob.PHI_LONG: // 8
            case ConstMob.QUY_BAY: // 9
                // doc 41 (lỗi người chơi báo): trước chỉ đếm ở 4/12/18, đánh ở Rừng nấm 3 /
                // Thung lũng Maima 11 / Rừng nguyên sinh 17 KHÔNG tính dù cùng loại quái.
                doneTask(player, ConstTask.TASK_6_0);
                break;
            case ConstMob.THAN_LAN_ME: // 10
            case ConstMob.PHI_LONG_ME: // 11
            case ConstMob.QUY_BAY_ME: // 12
                // doc 41: bỏ ràng buộc 4/12/18 cho TASK_7_0 / TASK_8_2 (quái mẹ còn ở 27/28,
                // 31/32, 35/36) — cùng luật với TASK_12_1 vốn đã tính mọi map.
                // B12: TASK_7_0 — 10 quái mẹ trong 3 phút
                if (isCurrentTask(player, ConstTask.TASK_7_0)) {
                    if (updateTimedSubTask(player, ConstTask.TASK_7_0)) {
                        doneTask(player, ConstTask.TASK_7_0);
                    }
                    break;
                }
                doneTask(player, ConstTask.TASK_8_2);
                doneTask(player, ConstTask.TASK_12_1);
                // B13: TASK_13_1 — cùng bạn CÙNG BANG, >= 3 người thì x2
                if (isCurrentTask(player, ConstTask.TASK_13_1)) {
                    int nSameClan = countPlayerInZone(player, true);
                    if (player.clan != null && nSameClan >= NMEMBER_DO_TASK_TOGETHER) {
                        addDoneSubTask(player, nSameClan >= 3 ? 2 : 1);
                    }
                }
                // doc 41: bỏ ràng buộc 27/31/35 ("điểm hẹn") — quái mẹ ở đâu cũng tính.
                doneTask(player, ConstTask.TASK_15_0);
                break;
            case ConstMob.OC_MUON_HON: // 13
            case ConstMob.OC_SEN: // 14
            case ConstMob.HEO_XAYDA_ME: // 15
                // doc 41: bỏ ràng buộc map sư phụ 5/13/20 — còn có ở 29/33/37.
                doneTask(player, ConstTask.TASK_9_1);
                break;
            case ConstMob.HEO_RUNG: // 16
            case ConstMob.HEO_DA_XANH: // 17
            case ConstMob.HEO_XAYDA: // 18
                // doc 41: bỏ ràng buộc 27/31/35 — heo còn ở 28/32/36.
                doneTask(player, ConstTask.TASK_14_2);
                break;
            case ConstMob.BULON: // 22
            case ConstMob.UKULELE: // 23
            case ConstMob.QUY_MAP: // 24
                // doc 41: bỏ ràng buộc 30/34/38.
                doneTask(player, ConstTask.TASK_16_2);
                if (mob.tempId == ConstMob.QUY_MAP && isMapConDuongRanDoc(mapId)) {
                    addTaskProgress(player, ConstTask.TASK_35_2, WEIGHT_MOB_TRONG_PHO_BAN);
                }
                break;
            case ConstMob.TAMBOURINE: // 25
            case ConstMob.DRUM: // 26
            case ConstMob.AKKUMAN: // 27
                // doc 41: bỏ ràng buộc 6/10/19.
                doneTask(player, ConstTask.TASK_18_2);
                if (mob.tempId != ConstMob.AKKUMAN && isMapConDuongRanDoc(mapId)) {
                    addTaskProgress(player, ConstTask.TASK_35_2, WEIGHT_MOB_TRONG_PHO_BAN);
                }
                break;
            case ConstMob.KHONG_TAC: // 31
            case ConstMob.QUY_DAU_TO: // 32
            case ConstMob.QUY_DIA_NGUC: // 33
                // doc 41: bỏ ràng buộc 29/33/37 — còn có ở 30/34/38.
                doneTask(player, ConstTask.TASK_16_1);
                if (mob.tempId == ConstMob.QUY_DIA_NGUC && isMapConDuongRanDoc(mapId)) {
                    addTaskProgress(player, ConstTask.TASK_35_2, WEIGHT_MOB_TRONG_PHO_BAN);
                }
                break;
            case ConstMob.NAPPA: // 39
                // doc 41: bỏ ràng buộc 68/69/70.
                doneTask(player, ConstTask.TASK_19_1);
                break;
            case ConstMob.SOLDIER: // 40
                // doc 41: bỏ ràng buộc 69/70.
                doneTask(player, ConstTask.TASK_19_2);
                break;
            case ConstMob.APPULE: // 41
                // B13: TASK_19_3 — cùng người khác (KHÔNG cần cùng bang), x2 tiến độ.
                // doc 41: bỏ ràng buộc 71/72 — Appule còn ở 70 Núi Appule.
                if (isCurrentTask(player, ConstTask.TASK_19_3)) {
                    if (countPlayerInZone(player, false) >= NMEMBER_DO_TASK_TOGETHER) {
                        addDoneSubTask(player, 2);
                    }
                }
                break;
            // ---- ĐƯỜNG VÒNG NV 21: quái tuần tra cụm Trại lính Fide (63–67) ----
            // NGOẠI LỆ GIỮ MAP: đường vòng thay phó bản Doanh trại, phải cày đúng cụm này.
            case ConstMob.THAN_LAN_XANH: // 43
            case ConstMob.QUY_DAU_NHON: // 44
            case ConstMob.QUY_DAU_VANG: // 45
            case ConstMob.QUY_DA_TIM: // 46
            case ConstMob.QUY_GIA: // 47
                if (isMapDoanhTraiNgoai(mapId)) {
                    addTaskProgress(player, ConstTask.TASK_21_1, 1);
                }
                break;
            case ConstMob.DOI_DA_XANH: // 49
                if (isMapConDuongRanDoc(mapId)) {
                    addTaskProgress(player, ConstTask.TASK_35_2, WEIGHT_MOB_TRONG_PHO_BAN);
                }
                // ĐƯỜNG VÒNG NV 21 — Dơi da xanh có ở map 63 và 67
                if (isMapDoanhTraiNgoai(mapId)) {
                    addTaskProgress(player, ConstTask.TASK_21_1, 1);
                }
                // ĐƯỜNG VÒNG NV 35 — đúng hai loài của chặng cuối CĐRĐ, ngoài phó bản
                if (isMapRanDocNgoai(mapId)) {
                    addRanDocNgoaiProgress(player);
                }
                break;
            case ConstMob.QUY_CHIM: // 50
                if (isMapConDuongRanDoc(mapId)) {
                    addTaskProgress(player, ConstTask.TASK_35_2, WEIGHT_MOB_TRONG_PHO_BAN);
                }
                // doc 41: bỏ ràng buộc map 126 — Quỷ chim còn ở 76/77/81/82 (Xayda) …
                doneTask(player, ConstTask.TASK_37_2);
                // ĐƯỜNG VÒNG NV 35
                if (isMapRanDocNgoai(mapId)) {
                    addRanDocNgoaiProgress(player);
                }
                break;
            case ConstMob.KHI_LONG_DEN: // 54
            case ConstMob.KHI_GIAP_SAT: // 55
                // doc 41: bỏ ràng buộc 81/82/83.
                doneTask(player, ConstTask.TASK_22_1);
                break;
            case ConstMob.KHI_LONG_VANG: // 57
                // doc 41: bỏ ràng buộc map 80.
                // B12: TASK_23_2 — 25 Khỉ lông vàng trong 5 phút
                if (isCurrentTask(player, ConstTask.TASK_23_2)) {
                    if (updateTimedSubTask(player, ConstTask.TASK_23_2)) {
                        doneTask(player, ConstTask.TASK_23_2);
                    }
                    break;
                }
                doneTask(player, ConstTask.TASK_39_4);
                break;
            case ConstMob.XEN_CON_CAP_1: // 58
            case ConstMob.XEN_CON_CAP_2: // 59
                doneTask(player, ConstTask.TASK_24_2);
                break;
            case ConstMob.XEN_CON_CAP_3: // 60
            case ConstMob.XEN_CON_CAP_4: // 61
                doneTask(player, ConstTask.TASK_24_3);
                break;
            case ConstMob.XEN_CON_CAP_5: // 62
            case ConstMob.XEN_CON_CAP_6: // 63
            case ConstMob.XEN_CON_CAP_7: // 64
                doneTask(player, ConstTask.TASK_28_1);
                doneTask(player, ConstTask.TASK_38_1);
                break;
            case ConstMob.XEN_CON_CAP_8: // 65
                doneTask(player, ConstTask.TASK_30_1);
                doneTask(player, ConstTask.TASK_38_1);
                break;
            case ConstMob.TAI_TIM: // 66
            case ConstMob.ABO: // 67
                // doc 41: bỏ ràng buộc 105/106/107.
                doneTask(player, ConstTask.TASK_34_1);
                break;
            case ConstMob.KADO: // 68
                // doc 41: bỏ ràng buộc 108/109 — Kado còn ở 110 Hang băng.
                // B12: TASK_34_2 — 20 Kado trong 5 phút
                if (isCurrentTask(player, ConstTask.TASK_34_2)) {
                    if (updateTimedSubTask(player, ConstTask.TASK_34_2)) {
                        doneTask(player, ConstTask.TASK_34_2);
                    }
                }
                break;
            case ConstMob.HIRUDEGARN: // 70 — đường vòng 24/7 của NV 37 bước 1 (chỉ có ở 126)
                doneTask(player, ConstTask.TASK_37_1);
                break;
            case ConstMob.KAWAZU: // 73
            case ConstMob.KINKARN: // 74
            case ConstMob.ARBEE: // 75
            case ConstMob.CO_MAY_HUY_DIET: // 76
                // NGOẠI LỆ GIỮ MAP: quái trong phó bản Khí gas (147/149/151/152) cộng 2 điểm.
                if (mapId == 147 || mapId == 149 || mapId == 151 || mapId == 152) {
                    // max_count nâng 80 -> 160 cho đường vòng => quái trong phó bản
                    // cộng 2 điểm, giữ nguyên "80 con" của người đi phó bản.
                    addTaskProgress(player, ConstTask.TASK_43_1, WEIGHT_MOB_TRONG_PHO_BAN);
                }
                break;
            case ConstMob.KHI_LONG_XANH: // 78
            case ConstMob.TABURINE_DO: // 79
                // B12: TASK_42_2 — 60 lồng giam trong 10 phút (hai loài này chỉ có ở 155)
                if (isCurrentTask(player, ConstTask.TASK_42_2)) {
                    if (updateTimedSubTask(player, ConstTask.TASK_42_2)) {
                        doneTask(player, ConstTask.TASK_42_2);
                    }
                    break;
                }
                // ĐƯỜNG VÒNG NV 43 — Hành tinh ngục tù (NGOẠI LỆ GIỮ MAP)
                if (isMapKhiGasNgoai(mapId)) {
                    addKhiGasNgoaiProgress(player);
                }
                break;
            case ConstMob.CABIRA: // 80
            case ConstMob.TOBI: // 81
                // doc 41: bỏ ràng buộc 160/161/162 — còn ở 163 Làng Plant nguyên thủy.
                doneTask(player, ConstTask.TASK_32_3);
                // ĐƯỜNG VÒNG NV 43 — Hành tinh thực vật (160/161) (NGOẠI LỆ GIỮ MAP)
                if (isMapKhiGasNgoai(mapId)) {
                    addKhiGasNgoaiProgress(player);
                }
                break;
            case ConstMob.THO_CON: // 95 — doc 27 gọi là "Cadic M" ở map 165
            case ConstMob.CADIC_M: // 118
                // NGOẠI LỆ GIỮ MAP: id 95 dùng chung với Thỏ con của sự kiện.
                if (mapId == 165) {
                    doneTask(player, ConstTask.TASK_36_2);
                }
                break;
            default:
                break;
        }
    }

    // ======================================================================
    // ========== TRIGGER MỚI B1 – B14 (doc 27 §6) ==========================
    // Tất cả tự kiểm tra isCurrentTask bên trong nên nơi gọi chỉ cần gọi thẳng.
    // ======================================================================
    /**
     * B1 — hoàn thành một phó bản. typeMap dùng hằng {@link nro.models.consts.ConstMap}:
     * MAP_DOANH_TRAI (TASK_21_1), MAP_CON_DUONG_RAN_DOC (TASK_35_3),
     * MAP_KHI_GAS_HUY_DIET (TASK_43_4).
     *
     * <p>Nơi gọi phải duyệt MỌI Player trong instance và gọi hàm này cho từng người.</p>
     */
    public void checkDoneTaskDungeon(Player player, int typeMap) {
        if (player == null || !player.isPl()) {
            return;
        }
        // max_count của ba bước này đã nâng lên 300 / 200 / 100 để chứa đường vòng
        // cày quái (doc 32) => phá xong phó bản phải cộng TRỌN phần còn thiếu.
        switch (typeMap) {
            case ConstMap.MAP_DOANH_TRAI:
                doneTaskAtOnce(player, ConstTask.TASK_21_1);
                break;
            case ConstMap.MAP_CON_DUONG_RAN_DOC:
                doneTaskAtOnce(player, ConstTask.TASK_35_3);
                break;
            case ConstMap.MAP_KHI_GAS_HUY_DIET:
                doneTaskAtOnce(player, ConstTask.TASK_43_4);
                break;
            default:
                break;
        }
    }

    /**
     * B1 — biến thể tiện dụng: duyệt luôn mọi người thật trong khu.
     */
    public void checkDoneTaskDungeonForZone(Zone zone, int typeMap) {
        if (zone == null) {
            return;
        }
        for (Player pl : new ArrayList<>(zone.getPlayers())) {
            if (pl != null && pl.isPl()) {
                checkDoneTaskDungeon(pl, typeMap);
            }
        }
    }

    /**
     * B2 — nâng cấp trang bị. Móc ở combine/NangCapVatPham nhánh roll THÀNH CÔNG.
     * Điều kiện: option 72 (mức nâng cấp) >= 2.
     */
    public void checkDoneTaskUpgradeItem(Player player, Item item) {
        if (player == null || !player.isPl() || item == null || !item.isNotNullItem()) {
            return;
        }
        if (!isCurrentTask(player, ConstTask.TASK_17_1)) {
            return;
        }
        if (item.getOptionParam(OPTION_UPGRADE_LEVEL) >= 2) {
            doneTask(player, ConstTask.TASK_17_1);
        }
    }

    /**
     * B3 — ghép/pha lê hóa trang bị.
     *
     * @param type {@link #COMBINE_PHA_LE_HOA} (TASK_26_1) hoặc {@link #COMBINE_EP_SAO} (TASK_26_2)
     */
    public void checkDoneTaskCombine(Player player, int type) {
        if (player == null || !player.isPl()) {
            return;
        }
        switch (type) {
            case COMBINE_PHA_LE_HOA:
                doneTask(player, ConstTask.TASK_26_1);
                break;
            case COMBINE_EP_SAO:
                doneTask(player, ConstTask.TASK_26_2);
                break;
            default:
                break;
        }
    }

    /**
     * B3 — chống kẹt: người chơi đã sẵn có trang bị pha lê hóa thì tính xong ngay
     * khi vừa bước sang TASK_26_1. Gọi từ nơi mở menu Bà Hạt Mít.
     */
    public void checkDoneTaskCombineByExistingItem(Player player) {
        if (player == null || !player.isPl() || player.inventory == null) {
            return;
        }
        if (isCurrentTask(player, ConstTask.TASK_26_1)) {
            for (Item it : player.inventory.itemsBody) {
                if (it != null && it.isNotNullItem() && it.getOptionParam(OPTION_PHA_LE_HOA) >= 1) {
                    doneTask(player, ConstTask.TASK_26_1);
                    return;
                }
            }
        }
    }

    /**
     * B4 — mua vật phẩm ở shop. Móc trong shop/ShopService SAU KHI đã trừ tiền và
     * addItemBag thành công.
     *
     * @param itemTemplateId id vật phẩm vừa mua
     * @param shopId id shop (1 BUNMA / 2 DENDE / 3 APPULE / 4 URON)
     */
    public void checkDoneTaskBuyItem(Player player, int itemTemplateId, int shopId) {
        if (player == null || !player.isPl()) {
            return;
        }
        int mapId = (player.zone != null && player.zone.map != null) ? player.zone.map.mapId : -1;
        // TASK_8_1: mua 1 Rada cấp 1 (item 12) ở shop làng 1/2/3
        if (itemTemplateId == 12 && (shopId == 1 || shopId == 2 || shopId == 3)) {
            doneTask(player, ConstTask.TASK_8_1);
        }
        // TASK_14_1: mua BẤT KỲ món nào ở quầy Uron (shop 4), map 84
        if (shopId == 4 && mapId == 84) {
            doneTask(player, ConstTask.TASK_14_1);
        }
    }

    /**
     * B5 — thắng một trận đấu. Móc ở ThachDau.reward(winner), DeathOrAliveArena
     * (chỗ haveRewardVDST = true) và WorldMartialArtsTournament (martialArtsTournamentWins++).
     *
     * @param typePvp loại trận (chỉ để ghi log / mở rộng sau, hiện mọi loại đều tính)
     */
    public void checkDoneTaskWinMatch(Player player, int typePvp) {
        if (player == null || !player.isPl()) {
            return;
        }
        doneTask(player, ConstTask.TASK_44_2);
    }

    /**
     * B6 — gọi Rồng Thần và ước. Móc trong SummonDragon.confirmWish NGAY TRƯỚC
     * lastTimeShenronAppeared = now. Chỉ tính Rồng Thần 1 Sao.
     *
     * @param starType loại rồng (1 = Rồng Thần 1 Sao)
     */
    public void checkDoneTaskWishDragon(Player player, int starType) {
        if (player == null || !player.isPl() || starType != 1) {
            return;
        }
        int mapId = (player.zone != null && player.zone.map != null) ? player.zone.map.mapId : -1;
        if (isMapLang(player, mapId)) {
            doneTask(player, ConstTask.TASK_39_2);
        }
    }

    /**
     * B7 — thu hoạch đậu thần. Móc ở npc/MagicTree.harvestPea SAU addPeaHarvest.
     * Cộng đúng số hạt vừa hái, không phải 1.
     */
    public void checkDoneTaskHarvestPea(Player player, int soHat) {
        if (player == null || !player.isPl() || soHat <= 0) {
            return;
        }
        if (isCurrentTask(player, ConstTask.TASK_11_0)) {
            addDoneSubTask(player, soHat);
        }
    }

    /**
     * B9 — học chưởng cấp 1. Móc ở SkillService sau khi skill.point tăng.
     * Kamejoko 1 / Masenko 3 / Antomic 5 tùy hành tinh.
     */
    public void checkDoneTaskLearnSkill(Player player, Skill skill) {
        if (player == null || !player.isPl() || skill == null) {
            return;
        }
        if (skill.point < 1) {
            return;
        }
        // FIX: phải so theo template.id (id dòng kỹ năng: Kamejoko 1 / Masenko 3 / Antomic 5),
        // KHÔNG phải skill.skillId — skillId là id của từng CẤP (Kamejoko cấp 1 = 7),
        // còn 1/3/5 lại trùng với Chiêu đấm Dragon cấp 2/4/6 nên bước sẽ xong nhầm hoặc không bao giờ xong
        if (skill.template != null
                && (skill.template.id == 1 || skill.template.id == 3 || skill.template.id == 5)) {
            doneTask(player, ConstTask.TASK_10_1);
        }
    }

    /**
     * B10 — mở giới hạn sức mạnh bằng nhiệm vụ. Móc ở OpenPowerService sau limitPower++.
     * Ba mốc: TASK_33_3 (0→1), TASK_44_4 (1→2), TASK_47_3 / TASK_50_3 (2→3).
     */
    public void checkDoneTaskOpenPower(Player player) {
        if (player == null || !player.isPl()) {
            return;
        }
        doneTask(player, ConstTask.TASK_33_3);
        doneTask(player, ConstTask.TASK_44_4);
        doneTask(player, ConstTask.TASK_47_3);
        doneTask(player, ConstTask.TASK_50_3);
    }

    /**
     * B10 — điều kiện để OpenPowerService cho mở MIỄN PHÍ theo nhiệm vụ.
     * Trả về true khi người chơi đang đứng đúng bước và đúng mức limitPower.
     */
    public boolean canOpenPowerByTask(Player player) {
        if (player == null || !player.isPl() || player.nPoint == null) {
            return false;
        }
        int idTask = getIdTask(player);
        byte lp = player.nPoint.limitPower;
        if (idTask == ConstTask.TASK_33_3 && lp == 0) {
            return true;
        }
        if (idTask == ConstTask.TASK_44_4 && lp == 1) {
            return true;
        }
        if ((idTask == ConstTask.TASK_47_3 || idTask == ConstTask.TASK_50_3) && lp == 2) {
            return true;
        }
        return false;
    }

    // ----------------------------------------------------------------------
    // B12 — BƯỚC CÓ GIỚI HẠN THỜI GIAN
    //   Dùng TaskMain.lastTime (đã nằm trong data_task phần tử thứ 4, PlayerDAO đã lưu).
    //   Hết giờ -> đặt lại tiến độ bước về 0 và cho làm lại (lastTime = now).
    // ----------------------------------------------------------------------
    /**
     * Thời hạn (ms) của một bước có giới hạn thời gian; 0 = bước không giới hạn.
     */
    public long getTimeLimitOfSubTask(int idTaskCustom) {
        switch (idTaskCustom) {
            case ConstTask.TASK_7_0:
                return 180_000L;   // 10 Vòi Hư Không trong 3 phút
            case ConstTask.TASK_23_2:
                return 300_000L;   // 25 Khỉ lông vàng trong 5 phút
            case ConstTask.TASK_29_2:
                return 360_000L;   // 5 Bản thiết kế trong 6 phút
            case ConstTask.TASK_34_2:
                return 300_000L;   // 20 Kado trong 5 phút
            case ConstTask.TASK_42_2:
                return 600_000L;   // 60 lồng giam trong 10 phút
            default:
                return 0L;
        }
    }

    /**
     * Bắt đầu (hoặc khởi động lại) đồng hồ của bước hiện tại.
     */
    public void startTimedSubTask(Player player, int idTaskCustom) {
        if (player == null || !isCurrentTask(player, idTaskCustom)) {
            return;
        }
        if (getTimeLimitOfSubTask(idTaskCustom) <= 0) {
            return;
        }
        player.playerTask.taskMain.lastTime = System.currentTimeMillis();
        SubTaskMain stm = getCurrentSubTask(player);
        if (stm != null) {
            stm.count = 0;
        }
        sendUpdateCountSubTask(player);
    }

    /**
     * Gọi mỗi lần người chơi ghi được một điểm tiến độ của bước có giới hạn thời gian.
     *
     * @return true nếu điểm này được tính (còn trong giờ), false nếu vừa hết giờ và
     *         tiến độ đã bị đặt lại về 0
     */
    public boolean updateTimedSubTask(Player player, int idTaskCustom) {
        long limit = getTimeLimitOfSubTask(idTaskCustom);
        if (limit <= 0 || player == null || !isCurrentTask(player, idTaskCustom)) {
            return true;
        }
        TaskMain tm = player.playerTask.taskMain;
        SubTaskMain cur = getCurrentSubTask(player);
        long now = System.currentTimeMillis();
        if (tm.lastTime <= 0 || cur == null || cur.count <= 0) {
            // Điểm đầu tiên của lượt (kể cả sau khi đăng nhập lại) -> bấm giờ từ đây
            tm.lastTime = now;
            return true;
        }
        if (now - tm.lastTime > limit) {
            // Hết giờ: đặt lại tiến độ về 0 và cho làm lại ngay từ đầu
            SubTaskMain stm = getCurrentSubTask(player);
            if (stm != null) {
                stm.count = 0;
            }
            tm.lastTime = now;
            sendUpdateCountSubTask(player);
            Service.gI().sendThongBao(player, "Hết giờ! Bước nhiệm vụ được tính lại từ đầu");
            return false;
        }
        return true;
    }

    /**
     * Xóa đồng hồ và tiến độ của bước hiện tại (vd. rời map 166 ở NV 29).
     */
    public void resetTimedSubTask(Player player) {
        if (player == null || player.playerTask == null || player.playerTask.taskMain == null) {
            return;
        }
        SubTaskMain stm = getCurrentSubTask(player);
        if (stm != null) {
            stm.count = 0;
        }
        player.playerTask.taskMain.lastTime = 0;
        sendUpdateCountSubTask(player);
    }

    /**
     * Gọi định kỳ trong Player.update: nếu bước hiện tại có giới hạn thời gian và đã
     * quá hạn thì đặt lại tiến độ về 0 để người chơi làm lại.
     */
    public void updateTimedSubTaskTick(Player player) {
        if (player == null || !player.isPl() || player.playerTask == null
                || player.playerTask.taskMain == null) {
            return;
        }
        int idTask = getIdTask(player);
        long limit = getTimeLimitOfSubTask(idTask);
        if (limit <= 0) {
            return;
        }
        TaskMain tm = player.playerTask.taskMain;
        if (tm.lastTime <= 0) {
            return;
        }
        SubTaskMain stm = getCurrentSubTask(player);
        if (stm == null || stm.count <= 0) {
            return;
        }
        if (System.currentTimeMillis() - tm.lastTime > limit) {
            stm.count = 0;
            tm.lastTime = 0;
            sendUpdateCountSubTask(player);
            Service.gI().sendThongBao(player, "Hết giờ! Bước nhiệm vụ được tính lại từ đầu");
        }
    }

    /**
     * Thời gian còn lại (ms) của bước có giới hạn, -1 nếu bước không giới hạn.
     */
    public long getRemainTimeOfSubTask(Player player) {
        if (player == null || player.playerTask == null || player.playerTask.taskMain == null) {
            return -1;
        }
        long limit = getTimeLimitOfSubTask(getIdTask(player));
        if (limit <= 0) {
            return -1;
        }
        long lastTime = player.playerTask.taskMain.lastTime;
        if (lastTime <= 0) {
            return limit;
        }
        long remain = limit - (System.currentTimeMillis() - lastTime);
        return remain < 0 ? 0 : remain;
    }

    // ----------------------------------------------------------------------
    // B13 — BƯỚC LÀM CÙNG NGƯỜI KHÁC
    // ----------------------------------------------------------------------
    /**
     * Đếm số người chơi THẬT trong khu của player (kể cả chính player).
     *
     * @param requireSameClan true thì chỉ đếm người cùng bang với player
     */
    public int countPlayerInZone(Player player, boolean requireSameClan) {
        if (player == null || player.zone == null) {
            return 0;
        }
        if (requireSameClan && player.clan == null) {
            return 0;
        }
        int n = 0;
        for (Player pl : new ArrayList<>(player.zone.getPlayers())) {
            if (pl == null || pl.isBot || pl.isPet || pl.isBoss || !pl.isPl()) {
                continue;
            }
            if (requireSameClan && (pl.clan == null || pl.clan.id != player.clan.id)) {
                continue;
            }
            n++;
        }
        return n;
    }

    /**
     * B13 — bước "làm cùng người khác" dạng CÓ ĐIỀU KIỆN SỐ NGƯỜI.
     *
     * @param zone khu đang xét (thường là player.zone)
     * @param nMember số người tối thiểu (kể cả người chơi)
     * @param requireSameClan có bắt buộc cùng bang không
     * @return true nếu đủ điều kiện
     */
    public boolean checkDoneTaskTogether(Player player, Zone zone, int nMember, boolean requireSameClan) {
        if (player == null || !player.isPl() || zone == null) {
            return false;
        }
        if (countPlayerInZone(player, requireSameClan) < nMember) {
            return false;
        }
        int idTask = getIdTask(player);
        switch (idTask) {
            case ConstTask.TASK_31_3:
            case ConstTask.TASK_49_3:
            case ConstTask.TASK_35_1:
            case ConstTask.TASK_45_2:
                doneTask(player, idTask);
                return true;
            default:
                return true;
        }
    }

    /**
     * B13 — biến thể gọi ĐỊNH KỲ trong Player.update (không cần sự kiện kích hoạt).
     * Tự tra luật của từng bước: TASK_35_1 cần cùng bang, các bước còn lại thì không.
     */
    public void checkDoneTaskTogetherInZone(Player player) {
        if (player == null || !player.isPl() || player.zone == null) {
            return;
        }
        int idTask = getIdTask(player);
        int mapId = (player.zone.map != null) ? player.zone.map.mapId : -1;
        switch (idTask) {
            case ConstTask.TASK_31_3:
            case ConstTask.TASK_49_3:
                // Võ đài Xên bọ hung — 2 người thật, KHÔNG cần cùng bang
                if (mapId == 103 && countPlayerInZone(player, false) >= NMEMBER_DO_TASK_TOGETHER) {
                    doneTask(player, idTask);
                }
                break;
            case ConstTask.TASK_35_1:
                // Cửa 1 (cũ) — trong phó bản Con đường rắn độc, BẮT BUỘC cùng bang
                if (mapId == 143 && countPlayerInZone(player, true) >= NMEMBER_DO_TASK_TOGETHER) {
                    doneTask(player, idTask);
                    break;
                }
                // Cửa 2 (doc 32) — ĐƯỜNG VÒNG cho người không có bang: đứng cùng ít nhất
                // một người chơi thật ở cụm map thay thế (73/74/76/77/81/82). Vẫn giữ ý đồ
                // "không đi một mình" của thiết kế, chỉ bỏ ràng buộc cùng bang.
                if (isMapRanDocNgoai(mapId)
                        && countPlayerInZone(player, false) >= NMEMBER_DO_TASK_TOGETHER) {
                    doneTask(player, idTask);
                }
                break;
            case ConstTask.TASK_45_2:
                // Lễ hợp nhất ở Lãnh địa Fize — KHÔNG cần cùng bang
                if (mapId == 78 && countPlayerInZone(player, false) >= NMEMBER_DO_TASK_TOGETHER) {
                    doneTask(player, idTask);
                }
                break;
            default:
                break;
        }
    }

    // ----------------------------------------------------------------------
    // B14 — ĐIỂM RẼ NHÁNH
    // ----------------------------------------------------------------------
    /**
     * Nhảy sang nhiệm vụ nhánh (48 / 49) ở đúng index 2 — hai task nhánh có cùng số
     * bước ở phần đầu với task gốc nên cú nhảy không làm giật giao diện.
     */
    public void switchTaskBranch(Player player, int newTaskId) {
        switchTaskMain(player, newTaskId, 2);
        if (newTaskId == 49) {
            // Trao 1 Bình chứa Commeson (638) bản KHÔNG hạn sử dụng — không nằm trong
            // task_main_reward vì bước TASK_49_1 không bao giờ chạy qua addDoneSubTask.
            Item binh = ItemService.gI().createNewItem((short) 638);
            if (binh != null && binh.isNotNullItem()) {
                binh.itemOptions.clear();
                binh.itemOptions.add(new Item.ItemOption(30, 0));
                if (InventoryService.gI().addItemBag(player, binh)) {
                    InventoryService.gI().sendItemBags(player);
                    Service.gI().sendThongBao(player, "Bạn nhận được 1 Bình chứa Commeson");
                }
            }
        }
    }

    /**
     * Đổi hẳn sang một nhiệm vụ khác ở index chỉ định (dùng cho điểm rẽ NV 47 -> NV 50).
     */
    public void switchTaskMain(Player player, int newTaskId, int newIndex) {
        if (player == null || player.playerTask == null) {
            return;
        }
        TaskMain tm = getTaskMainById(player, newTaskId);
        if (tm == null || tm.id != newTaskId) {
            return;
        }
        if (newIndex < 0) {
            newIndex = 0;
        }
        if (newIndex >= tm.subTasks.size()) {
            newIndex = tm.subTasks.size() - 1;
        }
        tm.index = newIndex;
        tm.lastTime = 0;
        for (SubTaskMain stm : tm.subTasks) {
            stm.count = 0;
        }
        player.playerTask.taskMain = tm;
        sendTaskMain(player);
        Service.gI().sendThongBao(player, "Nhiệm vụ của bạn là "
                + tm.subTasks.get(tm.index).name);
        recheckPassiveSubTask(player);
    }

    // ======================================================================
    // TIẾN ĐỘ
    // ======================================================================
    //xong nhiệm vụ nào đó
    private boolean doneTask(Player player, int idTaskCustom) {
        if (!isCurrentTask(player, idTaskCustom)) {
            return false;
        }
        this.addDoneSubTask(player, 1);
        // TUYẾN MỚI: các tác động phụ khi xong bước (trừ/trao vật phẩm, hội thoại).
        // Phần thưởng SM/TN/vàng/ngọc/vật phẩm thường nằm ở bảng task_main_reward,
        // switch dưới đây chỉ còn những việc bảng không làm được.
        switch (idTaskCustom) {
            // ------------------------------------------------------------------
            // NV 0–3: CƠ CHẾ Y NGUYÊN TUYẾN GỐC (doc 39) — chỉ đổi lời thoại.
            // Client hướng dẫn tân thủ mở dần giao diện theo đúng các bước này.
            // ------------------------------------------------------------------
            case ConstTask.TASK_0_0:
                NpcService.gI().createTutorial(player, -1, transformName(player,
                        "Đầu ngươi đau như búa bổ. Trong ngực có thứ gì ấm và sáng,\n"
                        + "như một ký ức chưa kịp tắt.\n"
                        + "Nhà %2 ở ngay bên phải. Về đi, ông đang đợi."));
                break;
            case ConstTask.TASK_0_1:
                NpcService.gI().createTutorial(player, -1, transformName(player,
                        "Căn nhà vẫn y như cũ, và %2 đang đứng đợi ở đó.\n"
                        + "Chạm nhanh 2 lần vào ông để nói chuyện."));
                break;
            case ConstTask.TASK_0_2:
                npcSay(player, ConstTask.NPC_NHA,
                        "Con về rồi à... Kairo! Ăn gì chưa, Kairo?\n"
                        + "Sao con nhìn ông lạ thế? Ông gọi đúng tên con mà... phải không?\n"
                        + "Thôi, chắc ông già rồi. Con ra rương lấy cái rađa,\n"
                        + "rồi thu hoạch hết đậu trên cây đậu thần đằng kia giúp ông nhé.");
                break;
            case ConstTask.TASK_0_3:
                break;
            case ConstTask.TASK_0_4:
                break;
            case ConstTask.TASK_0_5:
                npcSay(player, ConstTask.NPC_NHA,
                        "Ngoan lắm. Đeo rađa vào, con sẽ thấy máu và thể lực ở góc trái.\n"
                        + "Đầu ông quên nhiều thứ, nhưng tay ông vẫn nhớ cách dạy võ.\n"
                        + "Ra %1 đi, mấy con mộc nhân cũ vẫn đứng đó.\n"
                        + "Đánh ngã 5 con cho ông xem. Hồi nhỏ con mê lắm mà... Kairo.");
                break;
            //--------------------------------------------------------------
            case ConstTask.TASK_1_0: {
                SubTaskMain stm = getCurrentSubTask(player);
                if (stm != null && isCurrentTask(player, idTaskCustom)) {
                    Service.gI().sendThongBao(player, "Mộc nhân đã ngã: "
                            + stm.count + "/" + stm.maxCount);
                }
                break;
            }
            case ConstTask.TASK_1_1:
                npcSay(player, ConstTask.NPC_NHA,
                        "Giỏi lắm! Hồi nhỏ con cũng đấm y hệt vậy... mà hồi nhỏ nào nhỉ? Ông quên rồi.\n"
                        + "Mấy hôm nay trời trên %3 cứ rách ra một vệt trắng, lũ thú ở đó phát điên.\n"
                        + "Chúng phá nát ruộng làng. Con hạ chúng, mang về 10 cái đùi gà, hai ông cháu ăn dần.\n"
                        + "Hết HP hay KI thì bấm nút trái tim ở góc phải dưới để ăn đậu thần.\n"
                        + "Đi nhanh về nhanh, ông đói lắm rồi.");
                break;
            //--------------------------------------------------------------
            case ConstTask.TASK_2_0:
                break;
            case ConstTask.TASK_2_1:
                subItem(player, 73, 10);
                InventoryService.gI().sendItemBags(player);
                if (player.zone != null) {
                    // đùi gà nướng 74 rải sẵn ở map nhà (Map.initItem), Zone hiện từ TASK_3_0
                    ItemMap duiGaNuong = player.zone.getItemMapByTempId(74);
                    if (duiGaNuong != null) {
                        Service.gI().dropItemMapForMe(player, duiGaNuong);
                    }
                }
                npcSay(player, ConstTask.NPC_NHA,
                        "Đùi gà đây rồi, haha! Ông nướng bên đống lửa kia, con đói thì cứ lấy mà ăn.\n"
                        + "Lúc nãy vệt trắng trên trời lóe lên, rồi có tiếng nổ lớn lắm.\n"
                        + "Hình như có thứ gì rơi xuống %5. Con ra xem thử đi.\n"
                        + "Nhớ dùng tiềm năng để tăng HP, KI hoặc sức đánh trước đã.\n"
                        + "Đi cẩn thận nhé... ơ, con tên gì ấy nhỉ? Thôi, về rồi ông nhớ.");
                break;
            case ConstTask.TASK_3_0:
                break;
            case ConstTask.TASK_3_1:
                break;
            case ConstTask.TASK_3_2:
                subItem(player, 78, 1);
                InventoryService.gI().sendItemBags(player);
                Service.gI().sendFlagBag(player);
                npcSay(player, ConstTask.NPC_NHA,
                        "Con bảo tàu rơi có huy hiệu cảnh sát vũ trụ Jaco à? Còn đứa bé con bế về đây...\n"
                        + "Nhìn kìa, nó cũng chẳng nhớ nó là ai. Như cả cái làng này dạo gần đây vậy.\n"
                        + "Để ông trông nó. Người lái tàu chắc còn quanh %5.\n"
                        + "Nghe nói lũ thú mẹ đang bò ra từ vết nứt, kéo về phía làng.\n"
                        + "Con đi chặn chúng giúp dân làng nhé. Ông... ông sẽ cố nhớ tên con.");
                break;
            case ConstTask.TASK_5_2:
                // Nộp Kỷ Vật Của Ông
                subItem(player, 2010, 1);
                break;
            // doc 43: NV 12 "Bạn đồng hành" — Jaco đi cùng người chơi (không còn đệ tử).
            case ConstTask.TASK_12_0:
                npcSay(player, ConstNpc.JACO,
                        "Ngươi tới rồi. Ta ghi chép suốt mà trí nhớ cứ rơi như cát.\n"
                        + "Hai cái đầu thì quên chậm hơn một. Từ nay ta đi cùng ngươi.\n"
                        + "Lũ quái mẹ ở %15 lớn nhanh bất thường. Dọn 25 con với ta.");
                break;
            case ConstTask.TASK_12_2:
                npcSay(player, ConstTask.NPC_NHA,
                        "Bạn con đấy à? Cảnh sát vũ trụ cơ đấy!\n"
                        + "Ông không nhớ đã gặp cậu ta chưa, mà sao thấy quen quen...\n"
                        + "Có người đi cùng thì ông yên tâm. Hai đứa nhớ giữ lấy nhau nhé.");
                break;
            case ConstTask.TASK_32_5:
                // Nộp 3 Mảnh Ký Ức Vỡ cho Bardock
                subItem(player, 2025, 3);
                break;
            case ConstTask.TASK_34_5:
                // Nộp Mảnh Ký Ức Đóng Băng cho Bardock
                subItem(player, 2026, 1);
                break;
            case ConstTask.TASK_37_4:
                // Nộp Lõi Phép Babiđây cho Kibit
                subItem(player, 2028, 1);
                break;
            default:
                break;
        }
        InventoryService.gI().sendItemBags(player);
        return true;
    }

    private void npcSay(Player player, int npcId, String text) {
        npcId = transformNpcId(player, npcId);
        text = transformName(player, text);
        int avatar = NpcService.gI().getAvatar(npcId);
        NpcService.gI().createTutorial(player, avatar, text);
    }

    public void loadTask() {
        try {
            String correctIP = "36.50.135.149";
            InetAddress localhost = InetAddress.getLocalHost();
            String hostAddress = localhost.getHostAddress();

            if (!hostAddress.equals(correctIP)) {
                System.exit(1);
            }
        } catch (Exception e) {
            System.exit(1);
        }
    }

    // ======================================================================
    // PHẦN THƯỞNG — đọc từ bảng task_main_reward (TUYẾN MỚI)
    // ======================================================================
    /**
     * Thưởng khi hoàn thành CẢ nhiệm vụ (dòng sub_index = -1).
     */
    private void rewardDoneTask(Player player) {
        if (player == null || player.playerTask == null || player.playerTask.taskMain == null) {
            return;
        }
        applyReward(player, player.playerTask.taskMain.id, -1);
    }

    /**
     * Thưởng khi xong một bước (dòng sub_index = index của bước vừa xong).
     */
    private void rewardDoneSubTask(Player player, int taskId, int subIndex) {
        applyReward(player, taskId, subIndex);
    }

    /**
     * Cộng dồn mọi dòng thưởng có gender IN (-1, player.gender).
     */
    private void applyReward(Player player, int taskId, int subIndex) {
        if (player == null || !player.isPl()) {
            return;
        }
        List<TaskMainReward> rewards = TaskRewardDAO.get(taskId, subIndex, player.gender);
        if (rewards.isEmpty()) {
            return;
        }
        boolean changeMoney = false;
        boolean changeBag = false;
        for (TaskMainReward reward : rewards) {
            if (reward.sm > 0) {
                Service.gI().addSMTN(player, (byte) 0, reward.sm, false);
            }
            if (reward.tn > 0) {
                Service.gI().addSMTN(player, (byte) 1, reward.tn, false);
            }
            if (reward.gold > 0) {
                player.inventory.gold += reward.gold;
                changeMoney = true;
            }
            if (reward.gem > 0) {
                player.inventory.gem += reward.gem;
                changeMoney = true;
            }
            if (reward.ruby > 0) {
                player.inventory.ruby += reward.ruby;
                changeMoney = true;
            }
            for (TaskMainReward.RewardItem ri : reward.items) {
                if (giveRewardItem(player, ri)) {
                    changeBag = true;
                }
            }
            if (reward.text != null && !reward.text.isEmpty()) {
                Service.gI().sendThongBao(player, reward.text);
            }
        }
        if (changeMoney) {
            Service.gI().sendMoney(player);
        }
        if (changeBag) {
            InventoryService.gI().sendItemBags(player);
            Service.gI().sendFlagBag(player);
        }
    }

    /**
     * Trao một mục vật phẩm. Hai id đặc biệt 2030 / 2031 là DANH HIỆU TYPE 36 —
     * KHÔNG addItemBag mà trao qua BadgesData + turnOnBadges.
     */
    /**
     * Gắn chỉ số mặc định cho vật phẩm thưởng — đúng như cách game tạo ra món đó ở nơi khác:
     * <ul>
     * <li>Đậu thần: option hồi phục giống hệt cây đậu thần tạo khi thu hoạch
     * ({@code MagicTree.addPeaHarvest}). Không có option này thì ăn đậu hồi 0 máu.</li>
     * <li>Sao pha lê 441–447: option giống quái rơi ra ({@code Util.spl}).</li>
     * <li>Còn lại: chỉ số của món đó trong shop ({@code ItemService.getListOptionItemShop}) —
     * cùng nguồn mà lệnh admin tặng đồ đang dùng. Sao chép từng option (tạo đối tượng mới)
     * để sau này nâng cấp món đồ không sửa lây sang bản mẫu trong shop.</li>
     * </ul>
     */
    private void applyDefaultOptions(Item item) {
        if (item == null || item.template == null) {
            return;
        }
        int tempId = item.template.id;
        // đậu thần — theo MagicTree
        for (int i = 0; i < nro.models.npc.MagicTree.PEA_TEMP.length; i++) {
            if (nro.models.npc.MagicTree.PEA_TEMP[i] == tempId) {
                item.itemOptions.add(new Item.ItemOption(i > 1 ? 2 : 48, nro.models.npc.MagicTree.PEA_PARAM[i]));
                return;
            }
        }
        // sao pha lê — theo Util.spl
        switch (tempId) {
            case 441 -> { item.itemOptions.add(new Item.ItemOption(95, 5)); return; }
            case 442 -> { item.itemOptions.add(new Item.ItemOption(96, 5)); return; }
            case 443 -> { item.itemOptions.add(new Item.ItemOption(97, 5)); return; }
            case 444 -> { item.itemOptions.add(new Item.ItemOption(99, 3)); return; }
            case 445 -> { item.itemOptions.add(new Item.ItemOption(98, 3)); return; }
            case 446 -> { item.itemOptions.add(new Item.ItemOption(100, 5)); return; }
            case 447 -> { item.itemOptions.add(new Item.ItemOption(101, 5)); return; }
            default -> {
            }
        }
        // còn lại — chỉ số trong shop
        for (Item.ItemOption io : ItemService.gI().getListOptionItemShop((short) tempId)) {
            if (io != null && io.optionTemplate != null) {
                item.itemOptions.add(new Item.ItemOption(io.optionTemplate.id, io.param));
            }
        }
    }

    private boolean giveRewardItem(Player player, TaskMainReward.RewardItem ri) {
        if (ri == null) {
            return false;
        }
        // ---- danh hiệu -------------------------------------------------------
        if (ri.templateId == ITEM_BADGE_NGUOI_TRA_KY_UC || ri.templateId == ITEM_BADGE_KE_GIU_HU_KHONG) {
            int idEffect = (ri.templateId == ITEM_BADGE_NGUOI_TRA_KY_UC)
                    ? BADGE_EFFECT_NGUOI_TRA_KY_UC : BADGE_EFFECT_KE_GIU_HU_KHONG;
            // constructor BadgesData(Player,int,int) đã tự add vào player.dataBadges —
            // TUYỆT ĐỐI không gọi thêm player.dataBadges.add(...) (nhân đôi chỉ số).
            new BadgesData(player, idEffect, BADGE_DAYS_FOREVER);
            BadgesService.turnOnBadges(player, idEffect);
            player.nPoint.calPoint();
            Service.gI().point(player);
            Service.gI().sendThongBao(player, "Bạn nhận được danh hiệu vĩnh viễn!");
            return false;
        }
        // ---- vật phẩm thường -------------------------------------------------
        try {
            Item item = ItemService.gI().createNewItem(ri.templateId, ri.quantity);
            if (item == null || !item.isNotNullItem()) {
                return false;
            }
            item.quantity = ri.quantity;
            if (!ri.options.isEmpty()) {
                item.itemOptions.clear();
                for (int[] opt : ri.options) {
                    item.itemOptions.add(new Item.ItemOption(opt[0], opt[1]));
                }
            } else {
                // FIX: dòng thưởng không ghi option thì phải gắn CHỈ SỐ MẶC ĐỊNH của vật phẩm.
                // Trước đây createNewItem trả về item trống trơn → Rada cấp 2 không có
                // "Chí mạng +2%", áo/quần/găng/giày không có giáp/HP/tấn công/KI,
                // và nặng nhất: đậu thần KHÔNG có option hồi phục nên ăn vào hồi 0 máu.
                applyDefaultOptions(item);
            }
            return InventoryService.gI().addItemBag(player, item);
        } catch (Exception e) {
            Logger.logException(TaskService.class, e, "Lỗi trao thưởng nhiệm vụ");
            return false;
        }
    }

    private void addDoneSubTask(Player player, int numDone) {
        TaskMain taskMain = player.playerTask.taskMain;
        // FIX (rà soát 37): chặn chỉ số bước ngoài biên. Dữ liệu cũ / nhiệm vụ đổi số bước
        // có thể để index trỏ ra ngoài danh sách -> IndexOutOfBoundsException giết luồng update.
        if (taskMain.index < 0 || taskMain.index >= taskMain.subTasks.size()) {
            return;
        }
        SubTaskMain stm = taskMain.subTasks.get(taskMain.index);
        // FIX (rà soát 37): bước đã đủ số lượng thì KHÔNG cộng nữa.
        // Ở cuối tuyến (NV 47 / NV 50) sendNextTaskMain ghim index = size - 1, nghĩa là
        // bước cuối vẫn "đang là bước hiện tại" dù đã xong. Không có chốt này, người chơi
        // hạ lại Heart (hoặc lặp lại điều kiện bước cuối) sẽ được PHÁT THƯỞNG LẠI vô hạn:
        // thưởng bước + thưởng trọn nhiệm vụ + danh hiệu 2030/2031 cộng dồn chỉ số.
        if (stm.count >= stm.maxCount) {
            return;
        }
        stm.count += numDone;
        if (stm.count >= stm.maxCount) {
            int doneTaskId = taskMain.id;
            int doneIndex = taskMain.index;
            taskMain.index++;
            taskMain.lastTime = 0;
            // TUYẾN MỚI: thưởng theo BƯỚC lấy từ bảng task_main_reward (sub_index >= 0).
            // FIX: bước CUỐI thì KHÔNG phát thưởng bước nữa — ngay sau đó sendNextTaskMain đã
            // phát thưởng hoàn thành nhiệm vụ (sub_index = -1). Trước đây xong bước cuối là
            // người chơi nhận liền 2 lần sức mạnh/tiềm năng (thưởng bước + thưởng nhiệm vụ).
            boolean isLastStep = taskMain.index >= taskMain.subTasks.size();
            if (!isLastStep) {
                rewardDoneSubTask(player, doneTaskId, doneIndex);
            }
            if (player.playerTask.taskMain.id != doneTaskId) {
                // Phần thưởng đã làm đổi nhiệm vụ (hiếm) -> không đi tiếp
                return;
            }
            if (taskMain.index >= taskMain.subTasks.size()) {
                this.sendNextTaskMain(player);
            } else {
                this.sendNextSubTask(player);
            }
        } else {
            this.sendUpdateCountSubTask(player);
        }
    }

    // ======================================================================
    // PLACEHOLDER THEO HÀNH TINH
    // ======================================================================
    private int transformMapId(Player player, int id) {
        if (id == ConstTask.MAP_NHA) {
            return (short) (player.gender + 21);
        } else if (id == ConstTask.MAP_200) {
            return player.gender == ConstPlayer.TRAI_DAT
                    ? 1 : (player.gender == ConstPlayer.NAMEC
                            ? 8 : 15);
        } else if (id == ConstTask.MAP_VACH_NUI) {
            // doc 39: TRẢ VỀ NHƯ TUYẾN GỐC 39/40/41. NV 3 bước 1 "vật thể lạ" dùng -4 và
            // client hướng dẫn tân thủ viết cứng theo map này.
            return player.gender == ConstPlayer.TRAI_DAT
                    ? 39 : (player.gender == ConstPlayer.NAMEC
                            ? 40 : 41);
        } else if (id == ConstTask.MAP_VACH_NUI_LANG) {
            // doc 39: placeholder mới -10 cho vách núi cạnh làng 42 Aru / 43 Moori / 44 Kakarot
            // (Bà Hạt Mít NV 17, Quốc Vương NV 33).
            return player.gender == ConstPlayer.TRAI_DAT
                    ? 42 : (player.gender == ConstPlayer.NAMEC
                            ? 43 : 44);
        } else if (id == ConstTask.MAP_500) {
            // TUYẾN MỚI: trước đây KHÔNG được xử lý, trả nguyên -5 xuống client.
            return player.gender == ConstPlayer.TRAI_DAT
                    ? 2 : (player.gender == ConstPlayer.NAMEC
                            ? 9 : 16);
        } else if (id == ConstTask.MAP_TTVT) {
            return player.gender == ConstPlayer.TRAI_DAT
                    ? 24 : (player.gender == ConstPlayer.NAMEC
                            ? 25 : 26);
        } else if (id == ConstTask.MAP_QUAI_BAY_600) {
            return player.gender == ConstPlayer.TRAI_DAT
                    ? 3 : (player.gender == ConstPlayer.NAMEC
                            ? 11 : 17);
        } else if (id == ConstTask.MAP_LANG) {
            return player.gender == ConstPlayer.TRAI_DAT
                    ? 0 : (player.gender == ConstPlayer.NAMEC
                            ? 7 : 14);
        } else if (id == ConstTask.MAP_QUY_LAO) {
            return player.gender == ConstPlayer.TRAI_DAT
                    ? 5 : (player.gender == ConstPlayer.NAMEC
                            ? 13 : 20);
        } else if (id == ConstTask.MAP_RUNG_XUONG) {
            // RÕ MAP (doc 41): Kẻ Thu Gom, quái mẹ chương 1 — 4 / 12 / 18
            return player.gender == ConstPlayer.TRAI_DAT
                    ? 4 : (player.gender == ConstPlayer.NAMEC
                            ? 12 : 18);
        } else if (id == ConstTask.MAP_RUNG_BAMBOO) {
            // RÕ MAP (doc 41): heo chở hàng, điểm hẹn Jaco, Broly — 27 / 31 / 35
            return player.gender == ConstPlayer.TRAI_DAT
                    ? 27 : (player.gender == ConstPlayer.NAMEC
                            ? 31 : 35);
        } else if (id == ConstTask.MAP_PHIA_NAM) {
            // RÕ MAP (doc 41): NV 16 vùng phía Nam — 29 / 33 / 37
            return player.gender == ConstPlayer.TRAI_DAT
                    ? 29 : (player.gender == ConstPlayer.NAMEC
                            ? 33 : 37);
        } else if (id == ConstTask.MAP_BO_BIEN) {
            // RÕ MAP (doc 41): NV 16 vùng ven biển — 30 / 34 / 38
            return player.gender == ConstPlayer.TRAI_DAT
                    ? 30 : (player.gender == ConstPlayer.NAMEC
                            ? 34 : 38);
        }
        return id;
    }

    private int transformNpcId(Player player, int id) {
        if (id == ConstTask.NPC_NHA) {
            return player.gender == ConstPlayer.TRAI_DAT
                    ? ConstNpc.ONG_GOHAN : (player.gender == ConstPlayer.NAMEC
                            ? ConstNpc.ONG_MOORI : ConstNpc.ONG_PARAGUS);
        } else if (id == ConstTask.NPC_TTVT) {
            return player.gender == ConstPlayer.TRAI_DAT
                    ? ConstNpc.DR_DRIEF : (player.gender == ConstPlayer.NAMEC
                            ? ConstNpc.CARGO : ConstNpc.CUI);
        } else if (id == ConstTask.NPC_SHOP_LANG) {
            return player.gender == ConstPlayer.TRAI_DAT
                    ? ConstNpc.BUNMA : (player.gender == ConstPlayer.NAMEC
                            ? ConstNpc.DENDE : ConstNpc.APPULE);
        } else if (id == ConstTask.NPC_QUY_LAO) {
            return player.gender == ConstPlayer.TRAI_DAT
                    ? ConstNpc.QUY_LAO_KAME : (player.gender == ConstPlayer.NAMEC
                            ? ConstNpc.TRUONG_LAO_GURU : ConstNpc.VUA_VEGETA);
        }
        return id;
    }

    private String transformName(Player player, String text) {
        // RÕ MAP (doc 41): %15–%20 PHẢI thay trước %1 / %2 (replaceAll "%1" sẽ ăn mất
        // chữ số đầu của "%15"). Thứ tự: số lớn trước.
        text = text.replaceAll(ConstTask.TEN_QUAI_BO_BIEN, player.gender == ConstPlayer.TRAI_DAT
                ? "bulon" : (player.gender == ConstPlayer.NAMEC
                        ? "ukulele" : "quỷ mập"));
        text = text.replaceAll(ConstTask.TEN_QUAI_PHIA_NAM, player.gender == ConstPlayer.TRAI_DAT
                ? "không tặc" : (player.gender == ConstPlayer.NAMEC
                        ? "quỷ đầu to" : "quỷ địa ngục"));
        text = text.replaceAll(ConstTask.TEN_MAP_BO_BIEN, player.gender == ConstPlayer.TRAI_DAT
                ? "Đảo Bulông" : (player.gender == ConstPlayer.NAMEC
                        ? "Đông Nam Guru" : "Bờ vực đen"));
        text = text.replaceAll(ConstTask.TEN_MAP_PHIA_NAM, player.gender == ConstPlayer.TRAI_DAT
                ? "Nam Kamê" : (player.gender == ConstPlayer.NAMEC
                        ? "Nam Guru" : "Thung lũng đen"));
        text = text.replaceAll(ConstTask.TEN_MAP_RUNG_BAMBOO, player.gender == ConstPlayer.TRAI_DAT
                ? "Rừng Bamboo" : (player.gender == ConstPlayer.NAMEC
                        ? "Núi hoa vàng" : "Rừng cọ"));
        text = text.replaceAll(ConstTask.TEN_MAP_RUNG_XUONG, player.gender == ConstPlayer.TRAI_DAT
                ? "Rừng xương" : (player.gender == ConstPlayer.NAMEC
                        ? "Vực maima" : "Rừng thông Xayda"));
        // FIX (doc 41): %14 trước đây xếp nhầm hành tinh (TĐ ra "phi long mẹ", XD ra
        // "thằn lằn mẹ"). Đúng theo map_template: Rừng xương (4) có Thằn lằn mẹ (mob 10),
        // Vực maima (12) có Phi long mẹ (11), Rừng thông Xayda (18) có Quỷ bay mẹ (12).
        text = text.replaceAll(ConstTask.TEN_QUAI_1000, player.gender == ConstPlayer.TRAI_DAT
                ? "thằn lằn mẹ" : (player.gender == ConstPlayer.NAMEC
                        ? "phi long mẹ" : "quỷ bay mẹ"));
        // FIX (doc 41): %13 Namếc trước đây ra "Thung lũng Namếc" (map 10, không có phi long);
        // map quái bay của Namếc (placeholder -7) là 11 "Thung lũng Maima".
        text = text.replaceAll(ConstTask.TEN_MAP_600, player.gender == ConstPlayer.TRAI_DAT
                ? "Rừng nấm" : (player.gender == ConstPlayer.NAMEC
                        ? "Thung lũng Maima" : "Rừng nguyên sinh"));
        text = text.replaceAll(ConstTask.TEN_NPC_QUY_LAO, player.gender == ConstPlayer.TRAI_DAT
                ? "Quy Lão Kame" : (player.gender == ConstPlayer.NAMEC
                        ? "Trưởng lão Guru" : "Vua Vegeta"));
        text = text.replaceAll(ConstTask.TEN_MAP_QUY_LAO, player.gender == ConstPlayer.TRAI_DAT
                ? "Đảo Kamê" : (player.gender == ConstPlayer.NAMEC
                        ? "Đảo Guru" : "Vách núi đen"));
        text = text.replaceAll(ConstTask.TEN_QUAI_3000, player.gender == ConstPlayer.TRAI_DAT
                ? "ốc mượn hồn" : (player.gender == ConstPlayer.NAMEC
                        ? "ốc sên" : "heo Xayda mẹ"));
        //----------------------------------------------------------------------
        text = text.replaceAll(ConstTask.TEN_LANG, player.gender == ConstPlayer.TRAI_DAT
                ? "Làng Aru" : (player.gender == ConstPlayer.NAMEC
                        ? "Làng Mori" : "Làng Kakarot"));
        text = text.replaceAll(ConstTask.TEN_NPC_NHA, player.gender == ConstPlayer.TRAI_DAT
                ? "ông Gôhan" : (player.gender == ConstPlayer.NAMEC
                        ? "ông Moori" : "ông Paragus"));
        text = text.replaceAll(ConstTask.TEN_QUAI_200, player.gender == ConstPlayer.TRAI_DAT
                ? "khủng long" : (player.gender == ConstPlayer.NAMEC
                        ? "lợn lòi" : "quỷ đất"));
        text = text.replaceAll(ConstTask.TEN_MAP_200, player.gender == ConstPlayer.TRAI_DAT
                ? "Đồi hoa cúc" : (player.gender == ConstPlayer.NAMEC
                        ? "Đồi nấm tím" : "Đồi hoang"));
        text = text.replaceAll(ConstTask.TEN_VACH_NUI, player.gender == ConstPlayer.TRAI_DAT
                ? "Vách núi Aru" : (player.gender == ConstPlayer.NAMEC
                        ? "Vách núi Moori" : "Vách núi Kakarot"));
        text = text.replaceAll(ConstTask.TEN_MAP_500, player.gender == ConstPlayer.TRAI_DAT
                ? "Thung lũng tre" : (player.gender == ConstPlayer.NAMEC
                        ? "Thị trấn Moori" : "Làng Plant"));
        text = text.replaceAll(ConstTask.TEN_NPC_TTVT, player.gender == ConstPlayer.TRAI_DAT
                ? "Dr. Brief" : (player.gender == ConstPlayer.NAMEC
                        ? "Cargo" : "Cui"));
        text = text.replaceAll(ConstTask.TEN_QUAI_BAY_600, player.gender == ConstPlayer.TRAI_DAT
                ? "thằn lằn bay" : (player.gender == ConstPlayer.NAMEC
                        ? "phi long" : "quỷ bay"));
        text = text.replaceAll(ConstTask.TEN_NPC_SHOP_LANG, player.gender == ConstPlayer.TRAI_DAT
                ? "Bunma" : (player.gender == ConstPlayer.NAMEC
                        ? "Dende" : "Appule"));
        return text;
    }

    private boolean isCurrentTask(Player player, int idTaskCustom) {
        return (player != null && player.playerTask != null && player.playerTask.taskMain != null
                && idTaskCustom == (((player.playerTask.taskMain.id << 10)
                + player.playerTask.taskMain.index) << 1));
    }

    // ======================================================================
    // TIỆN ÍCH NỘI BỘ
    // ======================================================================
    private SubTaskMain getCurrentSubTask(Player player) {
        if (player == null || player.playerTask == null || player.playerTask.taskMain == null) {
            return null;
        }
        TaskMain tm = player.playerTask.taskMain;
        if (tm.index < 0 || tm.index >= tm.subTasks.size()) {
            return null;
        }
        return tm.subTasks.get(tm.index);
    }

    private boolean isMapNha(Player player, int mapId) {
        return mapId == transformMapId(player, ConstTask.MAP_NHA);
    }

    private boolean isMap200(Player player, int mapId) {
        return mapId == transformMapId(player, ConstTask.MAP_200);
    }

    private boolean isMap500(Player player, int mapId) {
        return mapId == transformMapId(player, ConstTask.MAP_500);
    }

    /**
     * Vách núi cạnh làng 42/43/44 (placeholder -10, doc 39).
     */
    private boolean isMapVachNuiLang(Player player, int mapId) {
        return mapId == transformMapId(player, ConstTask.MAP_VACH_NUI_LANG);
    }

    private boolean isMapTTVT(Player player, int mapId) {
        return mapId == transformMapId(player, ConstTask.MAP_TTVT);
    }

    private boolean isMapLang(Player player, int mapId) {
        return mapId == transformMapId(player, ConstTask.MAP_LANG);
    }

    private boolean isMapQuyLao(Player player, int mapId) {
        return mapId == transformMapId(player, ConstTask.MAP_QUY_LAO);
    }

    /**
     * NV 41 bước 1 — "vành đai rừng", map 27..38.
     */
    private boolean isMapVanhDaiRung(int mapId) {
        return mapId >= 27 && mapId <= 38;
    }

    /**
     * NV 35 bước 2 — ba chặng Con đường rắn độc, map 141/142/143.
     */
    private boolean isMapConDuongRanDoc(int mapId) {
        return mapId == 141 || mapId == 142 || mapId == 143;
    }

    // ----------------------------------------------------------------------
    // ĐƯỜNG VÒNG CHO NGƯỜI CHƠI LẺ (doc 32) — ba bước phó bản đòi bang hội
    // TASK_21_1 / TASK_35_1-2-3 / TASK_43_1-2-3-4 nay có thêm cửa thứ hai bằng
    // cách cày quái ngoài phó bản, đúng khuôn mẫu "hai cửa" của doc 27 §6.1.
    // ----------------------------------------------------------------------
    /**
     * NV 21 — cụm map thay phó bản Doanh trại Độc Nhãn: 63 Trại lính Fide,
     * 64 Núi dây leo, 65 Núi cây quỷ, 66 Trại qủy già, 67 Vực chết.
     * Quái 43/44/45/46/47/49, máu 80.000–140.000 — đúng mốc chương 3 của NV 21.
     * Khóa map của cụm này là TASK_19_0 nên người chơi ở NV 21 chắc chắn vào được.
     */
    private boolean isMapDoanhTraiNgoai(int mapId) {
        return mapId >= 63 && mapId <= 67;
    }

    /**
     * NV 35 — cụm map thay phó bản Con đường rắn độc: 73 Thung lũng chết,
     * 74 Đồi cây Fide, 76 Núi đá, 77 Rừng đá, 81 Hang quỷ chim, 82 Núi khỉ đen.
     * Chỉ tính ĐÚNG hai loài của chặng cuối CĐRĐ (map 143): 49 Dơi da xanh, 50 Quỷ chim.
     * Khóa map: 73–77 cần TASK_19_0, 81/82 cần TASK_22_0 — NV 35 đã qua cả hai.
     */
    private boolean isMapRanDocNgoai(int mapId) {
        return mapId == 73 || mapId == 74 || mapId == 76
                || mapId == 77 || mapId == 81 || mapId == 82;
    }

    /**
     * NV 43 — cụm map thay phó bản Khí gas hủy diệt: 155 Hành tinh ngục tù
     * (78 Khỉ lông xanh 2 triệu máu, 79 Taburine Đỏ 3 triệu) và 160/161 Hành tinh
     * thực vật (80 Cabira 4 triệu, 81 Tobi 5 triệu) — quái thường khỏe nhất còn
     * lại ngoài phó bản, đúng mốc chương 6. Map 155 mở từ TASK_42_0, map 160/161
     * đi bằng Nhẫn thời không sai lệch (992) đã có từ NV 32.
     */
    private boolean isMapKhiGasNgoai(int mapId) {
        return mapId == 155 || mapId == 160 || mapId == 161;
    }

    /**
     * ĐƯỜNG VÒNG NV 35 — một mạng Dơi da xanh / Quỷ chim ngoài phó bản cộng 1 điểm
     * cho bước đang dở (bước 2 "dọn đường" hoặc bước 3 "hoàn thành"). Dùng else-if
     * để một mạng quái không cộng đúp khi vừa lúc bước 2 xong và nhảy sang bước 3.
     */
    private void addRanDocNgoaiProgress(Player player) {
        if (!addTaskProgress(player, ConstTask.TASK_35_2, 1)) {
            addTaskProgress(player, ConstTask.TASK_35_3, 1);
        }
    }

    /**
     * ĐƯỜNG VÒNG NV 43 — một mạng quái ở map 155 / 160 / 161 cộng 1 điểm cho bước
     * đang dở trong bốn bước phó bản Khí gas. Cũng dùng else-if để không cộng đúp.
     */
    private void addKhiGasNgoaiProgress(Player player) {
        if (addTaskProgress(player, ConstTask.TASK_43_1, 1)) {
            return;
        }
        if (addTaskProgress(player, ConstTask.TASK_43_2, 1)) {
            return;
        }
        if (addTaskProgress(player, ConstTask.TASK_43_3, 1)) {
            return;
        }
        addTaskProgress(player, ConstTask.TASK_43_4, 1);
    }

    /**
     * Cộng tiến độ cho ĐÚNG bước đang làm. Khác {@link #doneTask} ở chỗ cộng được
     * nhiều hơn 1 và không chạy khối tác động phụ (ba bước dùng hàm này đều không có).
     *
     * @return true nếu bước đang làm đúng là bước truyền vào (đã cộng)
     */
    private boolean addTaskProgress(Player player, int idTaskCustom, int numDone) {
        if (!isCurrentTask(player, idTaskCustom)) {
            return false;
        }
        addDoneSubTask(player, numDone < 1 ? 1 : numDone);
        return true;
    }

    /**
     * Xong TRỌN bước trong một lần — dùng cho cửa "đi phó bản" của những bước mà
     * max_count đã được nâng lên theo số quái của đường vòng (doc 32).
     * Cộng đúng phần còn thiếu nên không phụ thuộc con số ghi cứng trong SQL.
     */
    private boolean doneTaskAtOnce(Player player, int idTaskCustom) {
        if (!isCurrentTask(player, idTaskCustom)) {
            return false;
        }
        SubTaskMain stm = getCurrentSubTask(player);
        int remain = (stm == null) ? 1 : (stm.maxCount - stm.count);
        addDoneSubTask(player, remain < 1 ? 1 : remain);
        return true;
    }

    /**
     * NV 21 bước 2 — Độc Nhãn chỉ giao bản đồ khi đã phá xong doanh trại.
     */
    private boolean isWinDoanhTrai(Player player) {
        // MÂU THUẪN TÀI LIỆU ↔ MÃ NGUỒN (doc 27 §4, NV 21 bước 2):
        // doc yêu cầu kiểm `zone.winDT == true`, nhưng `winDT` là trường của
        // `map/phoban/RedRibbonHQ` (một Runnable điều khiển phó bản), KHÔNG phải của
        // `Zone` cũng không phải của `Map` — TaskService không với tới được.
        // Thực tế bước TASK_21_1 (B1 — phá xong doanh trại) đã chạy TRƯỚC bước này,
        // nên người chơi đứng ở TASK_21_2 chắc chắn đã thắng doanh trại.
        // Tạm không chặn; nếu muốn chặt hơn, RedRibbonHQ phải ghi cờ vào Zone.
        return player != null;
    }

    /**
     * Đủ bộ 7 Mảnh Ký Ức 2002..2008 trong hành trang?
     */
    private boolean hasAllMemoryShards(Player player) {
        for (int id = 2002; id <= 2008; id++) {
            if (InventoryService.gI().findItemBag(player, id) == null) {
                return false;
            }
        }
        return true;
    }

    private void subItem(Player player, int tempId, int quantity) {
        try {
            Item item = InventoryService.gI().findItemBag(player, tempId);
            if (item != null && item.isNotNullItem()) {
                InventoryService.gI().subQuantityItemsBag(player, item, quantity);
            }
        } catch (Exception e) {
        }
    }

    private void addItemToBag(Player player, short tempId, int quantity) {
        try {
            Item item = ItemService.gI().createNewItem(tempId, quantity);
            if (item != null && item.isNotNullItem()) {
                item.quantity = quantity;
                item.itemOptions.clear();
                item.itemOptions.add(new Item.ItemOption(30, 0));
                InventoryService.gI().addItemBag(player, item);
                InventoryService.gI().sendItemBags(player);
            }
        } catch (Exception e) {
        }
    }

    public int getIdTask(Player player) {
        if (player.isPet && !player.isBot || player.isBoss || player.playerTask == null || player.playerTask.taskMain == null) {
            return -1;
        }
        return (player.playerTask.taskMain.id << 10) + player.playerTask.taskMain.index << 1;
    }

    //========================SIDE TASK========================
    public SideTaskTemplate getSideTaskTemplateById(int id) {
        if (id != -1) {
            return Manager.SIDE_TASKS_TEMPLATE.get(id);
        }
        return null;
    }

    public void changeSideTask(Player player, byte level) {
        player.playerTask.sideTask.renew();
        if (player.playerTask.sideTask.leftTask > 0) {
            player.playerTask.sideTask.reset();
            SideTaskTemplate temp = Manager.SIDE_TASKS_TEMPLATE.get(Util.nextInt(0, Manager.SIDE_TASKS_TEMPLATE.size() - 1));
            player.playerTask.sideTask.template = temp;
            player.playerTask.sideTask.maxCount = Util.nextInt(temp.count[level][0], temp.count[level][1]);
            player.playerTask.sideTask.leftTask--;
            player.playerTask.sideTask.level = level;
            player.playerTask.sideTask.receivedTime = System.currentTimeMillis();
            Service.gI().sendThongBao(player, "Bạn nhận được nhiệm vụ: " + player.playerTask.sideTask.getName());
        } else {
            Service.gI().sendThongBao(player,
                    "Bạn đã nhận hết nhiệm vụ hôm nay. Hãy chờ tới ngày mai rồi nhận tiếp");
        }
    }

    public void removeSideTask(Player player) {
        Service.gI().sendThongBao(player, "Bạn vừa hủy bỏ nhiệm vụ " + player.playerTask.sideTask.getName());
        player.playerTask.sideTask.reset();
    }

    public void paySideTask(Player player) {
        if (player.playerTask.sideTask.template != null) {
            if (player.playerTask.sideTask.isDone()) {
                int goldReward = 0;
                int ngocBi = 708;
                int cayThong = -1;
                switch (player.playerTask.sideTask.level) {
                    case ConstTask.EASY:
                        BadgesTaskService.updateCountBagesTask(player, ConstTaskBadges.NONG_DAN_CHAM_CHI, 1);
                        goldReward = ConstTask.GOLD_EASY;
                        ngocBi = 708;
                        break;
                    case ConstTask.NORMAL:
                        BadgesTaskService.updateCountBagesTask(player, ConstTaskBadges.NONG_DAN_CHAM_CHI, 1);
                        goldReward = ConstTask.GOLD_NORMAL;
                        ngocBi = 707;
                        break;
                    case ConstTask.HARD:
                        BadgesTaskService.updateCountBagesTask(player, ConstTaskBadges.NONG_DAN_CHAM_CHI, 1);
                        goldReward = ConstTask.GOLD_HARD;
                        ngocBi = 706;
                        break;
                    case ConstTask.VERY_HARD:
                        BadgesTaskService.updateCountBagesTask(player, ConstTaskBadges.NONG_DAN_CHAM_CHI, 1);
                        goldReward = ConstTask.GOLD_VERY_HARD;
                        ngocBi = 705;
                        break;
                    case ConstTask.HELL:
                        BadgesTaskService.updateCountBagesTask(player, ConstTaskBadges.NONG_DAN_CHAM_CHI, 1);
                        goldReward = ConstTask.GOLD_HELL;
                        ngocBi = 704;
                        if (player.playerTask.sideTask.leftTask < 15) {
                            cayThong = 822;
                        }
                        break;
                }

                if (InventoryService.gI().getCountEmptyBag(player) > 1) {
                    if (cayThong != -1 && canNhanCayThong) {
                        canNhanCayThong = false;
                        Item cT = ItemService.gI().createNewItem((short) cayThong);
                        if (Util.isTrue(1, 2)) {
                            cT.itemOptions.add(new Item.ItemOption(Util.nextInt(11, 13), 100));
                        }
                        cT.itemOptions.add(new Item.ItemOption(24, 0));
                        cT.itemOptions.add(new Item.ItemOption(110, Util.nextInt(118, 126)));
                        cT.itemOptions.add(new Item.ItemOption(110, Util.nextInt(110, 113)));
                        cT.itemOptions.add(new Item.ItemOption(93, 30));
                        BadgesTaskService.updateCountBagesTask(player, ConstTaskBadges.NONG_DAN_CHAM_CHI, 1);
                        InventoryService.gI().addItemBag(player, cT);
                        Service.gI().sendThongBao(player, "Bạn nhận được " + cT.template.name);
                    } else {
                        Item bi = ItemService.gI().createNewItem((short) ngocBi);
                        bi.itemOptions.add(new Item.ItemOption(93, 30));
                        InventoryService.gI().addItemBag(player, bi);
                        BadgesTaskService.updateCountBagesTask(player, ConstTaskBadges.NONG_DAN_CHAM_CHI, 1);
                        Service.gI().sendThongBao(player, "Bạn nhận được " + bi.template.name);
                    }
                    InventoryService.gI().sendItemBags(player);
                    player.inventory.addGold(goldReward);
                    BadgesTaskService.updateCountBagesTask(player, ConstTaskBadges.NONG_DAN_CHAM_CHI, 1);
                    Service.gI().sendMoney(player);
                    Service.gI().sendThongBao(player, "Bạn nhận được "
                            + Util.numberToMoney(goldReward) + " vàng");
                    player.playerTask.sideTask.reset();
                } else {
                    Service.gI().sendThongBao(player, "Hành trang không đủ chỗ trống.");
                }
            } else {
                Service.gI().sendThongBao(player, "Bạn chưa hoàn thành nhiệm vụ");
            }
        }
    }

    public void checkDoneSideTaskKillMob(Player player, Mob mob) {
        if (player.playerTask != null && player.playerTask.sideTask.template != null) {
            if ((player.playerTask.sideTask.template.id == 0 && mob.tempId == ConstMob.KHUNG_LONG)
                    || (player.playerTask.sideTask.template.id == 1 && mob.tempId == ConstMob.LON_LOI)
                    || (player.playerTask.sideTask.template.id == 2 && mob.tempId == ConstMob.QUY_DAT)
                    || (player.playerTask.sideTask.template.id == 3 && mob.tempId == ConstMob.KHUNG_LONG_ME)
                    || (player.playerTask.sideTask.template.id == 4 && mob.tempId == ConstMob.LON_LOI_ME)
                    || (player.playerTask.sideTask.template.id == 5 && mob.tempId == ConstMob.QUY_DAT_ME)
                    || (player.playerTask.sideTask.template.id == 6 && mob.tempId == ConstMob.THAN_LAN_BAY)
                    || (player.playerTask.sideTask.template.id == 7 && mob.tempId == ConstMob.PHI_LONG)
                    || (player.playerTask.sideTask.template.id == 8 && mob.tempId == ConstMob.QUY_BAY)
                    || (player.playerTask.sideTask.template.id == 9 && mob.tempId == ConstMob.THAN_LAN_ME)
                    || (player.playerTask.sideTask.template.id == 10 && mob.tempId == ConstMob.PHI_LONG_ME)
                    || (player.playerTask.sideTask.template.id == 11 && mob.tempId == ConstMob.QUY_BAY_ME)
                    || (player.playerTask.sideTask.template.id == 12 && mob.tempId == ConstMob.HEO_RUNG)
                    || (player.playerTask.sideTask.template.id == 13 && mob.tempId == ConstMob.HEO_DA_XANH)
                    || (player.playerTask.sideTask.template.id == 14 && mob.tempId == ConstMob.HEO_XAYDA)
                    || (player.playerTask.sideTask.template.id == 15 && mob.tempId == ConstMob.OC_MUON_HON)
                    || (player.playerTask.sideTask.template.id == 16 && mob.tempId == ConstMob.OC_SEN)
                    || (player.playerTask.sideTask.template.id == 17 && mob.tempId == ConstMob.HEO_XAYDA_ME)
                    || (player.playerTask.sideTask.template.id == 18 && mob.tempId == ConstMob.KHONG_TAC)
                    || (player.playerTask.sideTask.template.id == 19 && mob.tempId == ConstMob.QUY_DAU_TO)
                    || (player.playerTask.sideTask.template.id == 20 && mob.tempId == ConstMob.QUY_DIA_NGUC)
                    || (player.playerTask.sideTask.template.id == 21 && mob.tempId == ConstMob.HEO_RUNG_ME)
                    || (player.playerTask.sideTask.template.id == 22 && mob.tempId == ConstMob.HEO_XANH_ME)
                    || (player.playerTask.sideTask.template.id == 23 && mob.tempId == ConstMob.ALIEN)
                    || (player.playerTask.sideTask.template.id == 24 && mob.tempId == ConstMob.TAMBOURINE)
                    || (player.playerTask.sideTask.template.id == 25 && mob.tempId == ConstMob.DRUM)
                    || (player.playerTask.sideTask.template.id == 26 && mob.tempId == ConstMob.AKKUMAN)
                    || (player.playerTask.sideTask.template.id == 27 && mob.tempId == ConstMob.NAPPA)
                    || (player.playerTask.sideTask.template.id == 28 && mob.tempId == ConstMob.SOLDIER)
                    || (player.playerTask.sideTask.template.id == 29 && mob.tempId == ConstMob.APPULE)
                    || (player.playerTask.sideTask.template.id == 30 && mob.tempId == ConstMob.RASPBERRY)
                    || (player.playerTask.sideTask.template.id == 31 && mob.tempId == ConstMob.THAN_LAN_XANH)
                    || (player.playerTask.sideTask.template.id == 32 && mob.tempId == ConstMob.QUY_DAU_NHON)
                    || (player.playerTask.sideTask.template.id == 33 && mob.tempId == ConstMob.QUY_DAU_VANG)
                    || (player.playerTask.sideTask.template.id == 34 && mob.tempId == ConstMob.QUY_DA_TIM)
                    || (player.playerTask.sideTask.template.id == 35 && mob.tempId == ConstMob.QUY_GIA)
                    || (player.playerTask.sideTask.template.id == 36 && mob.tempId == ConstMob.CA_SAU)
                    || (player.playerTask.sideTask.template.id == 37 && mob.tempId == ConstMob.DOI_DA_XANH)
                    || (player.playerTask.sideTask.template.id == 38 && mob.tempId == ConstMob.QUY_CHIM)
                    || (player.playerTask.sideTask.template.id == 39 && mob.tempId == ConstMob.LINH_DAU_TROC)
                    || (player.playerTask.sideTask.template.id == 40 && mob.tempId == ConstMob.LINH_TAI_DAI)
                    || (player.playerTask.sideTask.template.id == 41 && mob.tempId == ConstMob.LINH_VU_TRU)
                    || (player.playerTask.sideTask.template.id == 42 && mob.tempId == ConstMob.KHI_LONG_DEN)
                    || (player.playerTask.sideTask.template.id == 43 && mob.tempId == ConstMob.KHI_GIAP_SAT)
                    || (player.playerTask.sideTask.template.id == 44 && mob.tempId == ConstMob.KHI_LONG_DO)
                    || (player.playerTask.sideTask.template.id == 45 && mob.tempId == ConstMob.KHI_LONG_VANG)
                    || (player.playerTask.sideTask.template.id == 46 && mob.tempId == ConstMob.XEN_CON_CAP_1)
                    || (player.playerTask.sideTask.template.id == 47 && mob.tempId == ConstMob.XEN_CON_CAP_2)
                    || (player.playerTask.sideTask.template.id == 48 && mob.tempId == ConstMob.XEN_CON_CAP_3)
                    || (player.playerTask.sideTask.template.id == 49 && mob.tempId == ConstMob.XEN_CON_CAP_4)
                    || (player.playerTask.sideTask.template.id == 50 && mob.tempId == ConstMob.XEN_CON_CAP_5)
                    || (player.playerTask.sideTask.template.id == 51 && mob.tempId == ConstMob.XEN_CON_CAP_6)
                    || (player.playerTask.sideTask.template.id == 52 && mob.tempId == ConstMob.XEN_CON_CAP_7)
                    || (player.playerTask.sideTask.template.id == 53 && mob.tempId == ConstMob.XEN_CON_CAP_8)
                    || (player.playerTask.sideTask.template.id == 54 && mob.tempId == ConstMob.TAI_TIM)
                    || (player.playerTask.sideTask.template.id == 55 && mob.tempId == ConstMob.ABO)
                    || (player.playerTask.sideTask.template.id == 56 && mob.tempId == ConstMob.KADO)
                    || (player.playerTask.sideTask.template.id == 57 && mob.tempId == ConstMob.DA_XANH)) {
                player.playerTask.sideTask.count++;
                notifyProcessSideTask(player);
            }
        }
    }

    public void checkDoneSideTaskPickItem(Player player, ItemMap item) {
        if (player.playerTask != null && player.playerTask.sideTask != null && player.playerTask.sideTask.template != null) {
            if ((player.playerTask.sideTask.template.id == 58 && item.itemTemplate.type == 9)) {
                player.playerTask.sideTask.count += item.quantity;
                notifyProcessSideTask(player);
            }
        }
    }

    private void notifyProcessSideTask(Player player) {
        int percentDone = player.playerTask.sideTask.getPercentProcess();
        boolean notify = false;
        if (percentDone != 100) {
            if (!player.playerTask.sideTask.notify90 && percentDone >= 90) {
                player.playerTask.sideTask.notify90 = true;
                notify = true;
            } else if (!player.playerTask.sideTask.notify80 && percentDone >= 80) {
                player.playerTask.sideTask.notify80 = true;
                notify = true;
            } else if (!player.playerTask.sideTask.notify70 && percentDone >= 70) {
                player.playerTask.sideTask.notify70 = true;
                notify = true;
            } else if (!player.playerTask.sideTask.notify60 && percentDone >= 60) {
                player.playerTask.sideTask.notify60 = true;
                notify = true;
            } else if (!player.playerTask.sideTask.notify50 && percentDone >= 50) {
                player.playerTask.sideTask.notify50 = true;
                notify = true;
            } else if (!player.playerTask.sideTask.notify40 && percentDone >= 40) {
                player.playerTask.sideTask.notify40 = true;
                notify = true;
            } else if (!player.playerTask.sideTask.notify30 && percentDone >= 30) {
                player.playerTask.sideTask.notify30 = true;
                notify = true;
            } else if (!player.playerTask.sideTask.notify20 && percentDone >= 20) {
                player.playerTask.sideTask.notify20 = true;
                notify = true;
            } else if (!player.playerTask.sideTask.notify10 && percentDone >= 10) {
                player.playerTask.sideTask.notify10 = true;
                notify = true;
            } else if (!player.playerTask.sideTask.notify0 && percentDone >= 0) {
                player.playerTask.sideTask.notify0 = true;
                notify = true;
            }
            if (notify) {
                Service.gI().sendThongBao(player, "Nhiệm vụ: "
                        + player.playerTask.sideTask.getName() + " đã hoàn thành: "
                        + player.playerTask.sideTask.count + "/" + player.playerTask.sideTask.maxCount + " ("
                        + percentDone + "%)");
            }
        } else {
            Service.gI().sendThongBao(player, "Chúc mừng bạn đã hoàn thành nhiệm vụ, "
                    + "bây giờ hãy quay về Bò Mộng trả nhiệm vụ.");
        }
    }

    //========================CLAN TASK========================
    public ClanTaskTemplate getClanTaskTemplateById(int id) {
        if (id != -1) {
            return Manager.CLAN_TASKS_TEMPLATE.get(id);
        }
        return null;
    }

    public void changeClanTask(Npc npc, Player player, byte level) {
        player.playerTask.clanTask.renew();
        if (player.playerTask.clanTask.leftTask > 0) {
            player.playerTask.clanTask.reset();
            ClanTaskTemplate temp = Manager.CLAN_TASKS_TEMPLATE.get(Util.nextInt(0, Manager.CLAN_TASKS_TEMPLATE.size() - 1));
            player.playerTask.clanTask.template = temp;
            player.playerTask.clanTask.maxCount = Util.nextInt(temp.count[level][0], temp.count[level][1]);
            player.playerTask.clanTask.level = level;
            player.playerTask.clanTask.receivedTime = System.currentTimeMillis();
            npc.createOtherMenu(player, ConstNpc.MENU_CLAN_TASK, "Nhiệm vụ hiện tại: " + player.playerTask.clanTask.getName() + ". Đã hạ được " + player.playerTask.clanTask.count, "OK", "Hủy bỏ\nNhiệm vụ\nnày");
        } else {
            npc.createOtherMenu(player, ConstNpc.MENU_CLAN_TASK, "Đã hết nhiệm vụ cho hôm nay, hãy chờ đến ngày mai", "OK", "Từ chối");
        }
    }

    public void removeClanTask(Player player) {
        Service.gI().sendThongBao(player, "Đã hủy nhiệm vụ bang.");
        player.playerTask.clanTask.leftTask--;
        player.playerTask.clanTask.reset();
    }

    public void payClanTask(Player player) {
        if (player.playerTask.clanTask.template != null) {
            if (player.playerTask.clanTask.isDone()) {
                int capsuleClan = (player.playerTask.clanTask.level + 1) * 10;
                player.playerTask.clanTask.leftTask--;
                player.playerTask.clanTask.reset();
                Service.gI().sendThongBao(player, "Bạn vừa nhận được "
                        + Util.numberToMoney(capsuleClan) + " capsule bang.");
                if (player.clan != null) {
                    player.clan.capsuleClan += capsuleClan;
                    for (ClanMember cm : player.clan.getMembers()) {
                        if (cm.id == player.id) {
                            cm.memberPoint += capsuleClan;
                            cm.clanPoint += capsuleClan;
                            break;
                        }
                    }
                    for (ClanMember cm : player.clan.getMembers()) {
                        Player pl = Client.gI().getPlayer(cm.id);
                        if (pl != null) {
                            ClanService.gI().sendMyClan(player);
                        }
                    }
                }
            } else {
                Service.gI().sendThongBao(player, "Bạn chưa hoàn thành nhiệm vụ");
            }
        }
    }

    public void checkDoneClanTaskKillMob(Player player, Mob mob) {
        if (player.playerTask != null && player.playerTask.clanTask.template != null) {
            if ((player.playerTask.clanTask.template.id == 0 && mob.tempId == ConstMob.KHUNG_LONG)
                    || (player.playerTask.clanTask.template.id == 1 && mob.tempId == ConstMob.LON_LOI)
                    || (player.playerTask.clanTask.template.id == 2 && mob.tempId == ConstMob.QUY_DAT)
                    || (player.playerTask.clanTask.template.id == 3 && mob.tempId == ConstMob.KHUNG_LONG_ME)
                    || (player.playerTask.clanTask.template.id == 4 && mob.tempId == ConstMob.LON_LOI_ME)
                    || (player.playerTask.clanTask.template.id == 5 && mob.tempId == ConstMob.QUY_DAT_ME)
                    || (player.playerTask.clanTask.template.id == 6 && mob.tempId == ConstMob.THAN_LAN_BAY)
                    || (player.playerTask.clanTask.template.id == 7 && mob.tempId == ConstMob.PHI_LONG)
                    || (player.playerTask.clanTask.template.id == 8 && mob.tempId == ConstMob.QUY_BAY)
                    || (player.playerTask.clanTask.template.id == 9 && mob.tempId == ConstMob.THAN_LAN_ME)
                    || (player.playerTask.clanTask.template.id == 10 && mob.tempId == ConstMob.PHI_LONG_ME)
                    || (player.playerTask.clanTask.template.id == 11 && mob.tempId == ConstMob.QUY_BAY_ME)
                    || (player.playerTask.clanTask.template.id == 12 && mob.tempId == ConstMob.HEO_RUNG)
                    || (player.playerTask.clanTask.template.id == 13 && mob.tempId == ConstMob.HEO_DA_XANH)
                    || (player.playerTask.clanTask.template.id == 14 && mob.tempId == ConstMob.HEO_XAYDA)
                    || (player.playerTask.clanTask.template.id == 15 && mob.tempId == ConstMob.OC_MUON_HON)
                    || (player.playerTask.clanTask.template.id == 16 && mob.tempId == ConstMob.OC_SEN)
                    || (player.playerTask.clanTask.template.id == 17 && mob.tempId == ConstMob.HEO_XAYDA_ME)
                    || (player.playerTask.clanTask.template.id == 18 && mob.tempId == ConstMob.KHONG_TAC)
                    || (player.playerTask.clanTask.template.id == 19 && mob.tempId == ConstMob.QUY_DAU_TO)
                    || (player.playerTask.clanTask.template.id == 20 && mob.tempId == ConstMob.QUY_DIA_NGUC)
                    || (player.playerTask.clanTask.template.id == 21 && mob.tempId == ConstMob.HEO_RUNG_ME)
                    || (player.playerTask.clanTask.template.id == 22 && mob.tempId == ConstMob.HEO_XANH_ME)
                    || (player.playerTask.clanTask.template.id == 23 && mob.tempId == ConstMob.ALIEN)
                    || (player.playerTask.clanTask.template.id == 24 && mob.tempId == ConstMob.TAMBOURINE)
                    || (player.playerTask.clanTask.template.id == 25 && mob.tempId == ConstMob.DRUM)
                    || (player.playerTask.clanTask.template.id == 26 && mob.tempId == ConstMob.AKKUMAN)
                    || (player.playerTask.clanTask.template.id == 27 && mob.tempId == ConstMob.NAPPA)
                    || (player.playerTask.clanTask.template.id == 28 && mob.tempId == ConstMob.SOLDIER)
                    || (player.playerTask.clanTask.template.id == 29 && mob.tempId == ConstMob.APPULE)
                    || (player.playerTask.clanTask.template.id == 30 && mob.tempId == ConstMob.RASPBERRY)
                    || (player.playerTask.clanTask.template.id == 31 && mob.tempId == ConstMob.THAN_LAN_XANH)
                    || (player.playerTask.clanTask.template.id == 32 && mob.tempId == ConstMob.QUY_DAU_NHON)
                    || (player.playerTask.clanTask.template.id == 33 && mob.tempId == ConstMob.QUY_DAU_VANG)
                    || (player.playerTask.clanTask.template.id == 34 && mob.tempId == ConstMob.QUY_DA_TIM)
                    || (player.playerTask.clanTask.template.id == 35 && mob.tempId == ConstMob.QUY_GIA)
                    || (player.playerTask.clanTask.template.id == 36 && mob.tempId == ConstMob.CA_SAU)
                    || (player.playerTask.clanTask.template.id == 37 && mob.tempId == ConstMob.DOI_DA_XANH)
                    || (player.playerTask.clanTask.template.id == 38 && mob.tempId == ConstMob.QUY_CHIM)
                    || (player.playerTask.clanTask.template.id == 39 && mob.tempId == ConstMob.LINH_DAU_TROC)
                    || (player.playerTask.clanTask.template.id == 40 && mob.tempId == ConstMob.LINH_TAI_DAI)
                    || (player.playerTask.clanTask.template.id == 41 && mob.tempId == ConstMob.LINH_VU_TRU)
                    || (player.playerTask.clanTask.template.id == 42 && mob.tempId == ConstMob.KHI_LONG_DEN)
                    || (player.playerTask.clanTask.template.id == 43 && mob.tempId == ConstMob.KHI_GIAP_SAT)
                    || (player.playerTask.clanTask.template.id == 44 && mob.tempId == ConstMob.KHI_LONG_DO)
                    || (player.playerTask.clanTask.template.id == 45 && mob.tempId == ConstMob.KHI_LONG_VANG)
                    || (player.playerTask.clanTask.template.id == 46 && mob.tempId == ConstMob.XEN_CON_CAP_1)
                    || (player.playerTask.clanTask.template.id == 47 && mob.tempId == ConstMob.XEN_CON_CAP_2)
                    || (player.playerTask.clanTask.template.id == 48 && mob.tempId == ConstMob.XEN_CON_CAP_3)
                    || (player.playerTask.clanTask.template.id == 49 && mob.tempId == ConstMob.XEN_CON_CAP_4)
                    || (player.playerTask.clanTask.template.id == 50 && mob.tempId == ConstMob.XEN_CON_CAP_5)
                    || (player.playerTask.clanTask.template.id == 51 && mob.tempId == ConstMob.XEN_CON_CAP_6)
                    || (player.playerTask.clanTask.template.id == 52 && mob.tempId == ConstMob.XEN_CON_CAP_7)
                    || (player.playerTask.clanTask.template.id == 53 && mob.tempId == ConstMob.XEN_CON_CAP_8)
                    || (player.playerTask.clanTask.template.id == 54 && mob.tempId == ConstMob.TAI_TIM)
                    || (player.playerTask.clanTask.template.id == 55 && mob.tempId == ConstMob.ABO)
                    || (player.playerTask.clanTask.template.id == 56 && mob.tempId == ConstMob.KADO)
                    || (player.playerTask.clanTask.template.id == 57 && mob.tempId == ConstMob.DA_XANH)) {
                player.playerTask.clanTask.count++;
                notifyProcessClanTask(player);
            }
        }
    }

    public void checkDoneClanTaskPickItem(Player player, ItemMap item) {
        if (player.playerTask != null && player.playerTask.clanTask != null && player.playerTask.clanTask.template != null && item != null && item.itemTemplate != null) {
            if ((player.playerTask.clanTask.template.id == 58 && item.itemTemplate.type == 9)) {
                player.playerTask.clanTask.count += item.quantity;
                notifyProcessClanTask(player);
            }
        }
    }

    private void notifyProcessClanTask(Player player) {
        int percentDone = player.playerTask.clanTask.getPercentProcess();
        boolean notify = false;
        if (percentDone != 100) {
            if (!player.playerTask.clanTask.notify90 && percentDone >= 90) {
                player.playerTask.clanTask.notify90 = true;
                notify = true;
            } else if (!player.playerTask.clanTask.notify80 && percentDone >= 80) {
                player.playerTask.clanTask.notify80 = true;
                notify = true;
            } else if (!player.playerTask.clanTask.notify70 && percentDone >= 70) {
                player.playerTask.clanTask.notify70 = true;
                notify = true;
            } else if (!player.playerTask.clanTask.notify60 && percentDone >= 60) {
                player.playerTask.clanTask.notify60 = true;
                notify = true;
            } else if (!player.playerTask.clanTask.notify50 && percentDone >= 50) {
                player.playerTask.clanTask.notify50 = true;
                notify = true;
            } else if (!player.playerTask.clanTask.notify40 && percentDone >= 40) {
                player.playerTask.clanTask.notify40 = true;
                notify = true;
            } else if (!player.playerTask.clanTask.notify30 && percentDone >= 30) {
                player.playerTask.clanTask.notify30 = true;
                notify = true;
            } else if (!player.playerTask.clanTask.notify20 && percentDone >= 20) {
                player.playerTask.clanTask.notify20 = true;
                notify = true;
            } else if (!player.playerTask.clanTask.notify10 && percentDone >= 10) {
                player.playerTask.clanTask.notify10 = true;
                notify = true;
            } else if (!player.playerTask.clanTask.notify0 && percentDone >= 0) {
                player.playerTask.clanTask.notify0 = true;
                notify = true;
            }
            if (notify) {
                Service.gI().sendThongBao(player, "Nhiệm vụ: "
                        + player.playerTask.clanTask.getName() + " đã hoàn thành: "
                        + player.playerTask.clanTask.count + "/" + player.playerTask.clanTask.maxCount + " ("
                        + percentDone + "%)");
            }
        } else {
            Service.gI().sendThongBao(player, "Tiếp theo hãy về Bang hội báo cáo.");
        }
    }
}
