package nro.models.data;

import nro.models.player_system.Template.HeadAvatar;
import nro.models.player_system.Template.MapTemplate;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

import nro.models.utils.FileIO;
import nro.models.services.Service;
import nro.models.skill.NClass;
import nro.models.skill.Skill;
import nro.models.player_system.Template.MobTemplate;
import nro.models.player_system.Template.NpcTemplate;
import nro.models.player_system.Template.SkillTemplate;
import nro.models.interfaces.ISession;
import java.io.ByteArrayOutputStream;
import nro.models.network.Message;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.file.StandardOpenOption;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import nro.models.server.Manager;
import nro.models.network.MySession;
import nro.models.utils.Logger;

import nro.models.player_system.Template.BgItem;

public class DataGame {

    // 9 -> 10: file part sửa lệch từ part 1999 (patch 14) — client phải tải lại part.
    // 10 -> 11: đổi hình NPC 76 Granola (patch 15).
    // 11 -> 12: thêm 114 dòng part của 38 cải trang mang từ NGOL (patch 35).
    // 12 -> 13: sửa part 1919 bị ghi nhầm 1949 (patch 38), part cải trang gỡ bỏ (patch 39).
    // 13 -> 14: thêm 25 part của 5 cải trang mới 2075–2079 (patch 40).
    public static byte vsData = 14;
    // 2 -> 3 vì thêm npc_template 85 "ADMIN Đẹp Trai" (SRC/sql/patch/09-npc-admin-dep-trai.sql).
    // Danh sách npc_template (tên + head/body/leg) đi trong gói vMap (updateMap);
    // client chỉ xin tải lại khi vsMap khác bản đang cache. Không tăng => client cũ
    // không biết NPC 85, NPC đứng ở nhà không hiện / hiện sai hình.
    // Mỗi lần thêm/sửa dòng npc_template hoặc tên map PHẢI tăng số này (tối đa 127).
    // 3 -> 4: đổi hình NPC 76 Granola (patch 15) + thêm cửa vào map 166 và NPC 83
    //         Dr. Myuu (patch 18). Hai thứ này đi trong gói vMap, không phải vsData.
    // 4 -> 5: thêm NPC 64 Thiên Sứ Whis vào map 155 (patch 30).
    // 5 -> 6: đổi bộ tile của map 166 Phòng thí nghiệm Myuu (patch 31).
    // 6 -> 7: đổi hình npc_template 108 Heart và 110 (patch 32).
    public static byte vsMap = 7;
    public static byte vsSkill = 1;
    // FIX: 9 -> 10 vì thêm 32 vật phẩm mới (id 2000..2031, SRC/sql/patch/01-vat-pham-moi.sql).
    // Client chỉ xin tải lại bảng item khi thấy vsItem khác bản nó đang cache.
    // Không tăng => item mới hiện tên rỗng / icon trắng với mọi người chơi cũ.
    // Mỗi lần thêm/bớt/sửa dòng item_template PHẢI tăng số này thêm 1 (tối đa 127).
    // 10 -> 11: thêm 43 vật phẩm id 2032–2074 mang từ NGOL (patch 35).
    // 11 -> 12: bỏ cắt gói vật phẩm thành nhiều khúc (cache NRitem2 của client chỉ giữ khúc
    //          cuối). Tăng để client xoá cache hỏng và tải lại đủ bộ.
    // 12 -> 13: patch 39 gỡ 11 cải trang thiếu ảnh (1841–1851).
    // 13 -> 14: thêm 5 cải trang 2075–2079 (patch 40).
    public static byte vsItem = 14;
    public static int vsRes = 1;
    public static short maxSmallVersion = 32767;

    public static String LINK_IP_PORT = "Ngọc Rồng Online:36.50.134.190:14445:0";
    public static Map<Object, Object> MAP_MOUNT_NUM = new HashMap<>();

    public static void sendVersionGame(MySession session) {
        Message msg;
        try {
            msg = Service.gI().messageNotMap((byte) 4);
            msg.writer().writeByte(vsData);
            msg.writer().writeByte(vsMap);
            msg.writer().writeByte(vsSkill);
            msg.writer().writeByte(vsItem);
            msg.writer().writeByte(0);

            long[] smtieuchuan = {1000L, 3000L, 15000L, 40000L, 90000L, 170000L, 340000L, 700000L,
                1500000L, 15000000L, 150000000L, 1500000000L, 5000000000L, 10000000000L, 40000000000L,
                50010000000L, 60010000000L, 70010000000L, 80010000000L, 100010000000L, 1000010000000L, 10000010000000L};
            msg.writer().writeByte(smtieuchuan.length);
            for (int i = 0; i < smtieuchuan.length; i++) {
                msg.writer().writeLong(smtieuchuan[i]);
            }
            session.sendMessage(msg);
            msg.cleanup();
        } catch (IOException e) {
        }
    }

    //vData
    public static void updateData(MySession session) {
        final byte[] dart = FileIO.readFile("data/update_data/dart");
        final byte[] arrow = FileIO.readFile("data/update_data/arrow");
        final byte[] effect = FileIO.readFile("data/update_data/effect");
        final byte[] image = FileIO.readFile("data/update_data/image");
        final byte[] part = FileIO.readFile("data/update_data/part");
        final byte[] skill = FileIO.readFile("data/update_data/skill");

        Message msg;
        try {
            msg = new Message(-87);
            msg.writer().writeByte(vsData);
            msg.writer().writeInt(dart.length);
            msg.writer().write(dart);
            msg.writer().writeInt(arrow.length);
            msg.writer().write(arrow);
            msg.writer().writeInt(effect.length);
            msg.writer().write(effect);
            msg.writer().writeInt(image.length);
            msg.writer().write(image);
            msg.writer().writeInt(part.length);
            msg.writer().write(part);
            msg.writer().writeInt(skill.length);
            msg.writer().write(skill);

            session.doSendMessage(msg);
            msg.cleanup();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    //vMap
    public static void updateMap(MySession session) {
        Message msg;
        try {
            msg = Service.gI().messageNotMap((byte) 6);
            msg.writer().writeByte(vsMap);
            msg.writer().writeByte(Manager.MAP_TEMPLATES.length);
            for (MapTemplate temp : Manager.MAP_TEMPLATES) {
                msg.writer().writeUTF(temp.name);
            }
            msg.writer().writeByte(Manager.NPC_TEMPLATES.size());
            for (NpcTemplate temp : Manager.NPC_TEMPLATES) {
                msg.writer().writeUTF(temp.name);
                msg.writer().writeShort(temp.head);
                msg.writer().writeShort(temp.body);
                msg.writer().writeShort(temp.leg);
                msg.writer().writeByte(0);
            }
            msg.writer().writeByte(Manager.MOB_TEMPLATES.size());
            for (MobTemplate temp : Manager.MOB_TEMPLATES) {
                msg.writer().writeByte(temp.type);
                msg.writer().writeUTF(temp.name);
                msg.writer().writeInt(temp.hp);
                msg.writer().writeByte(temp.rangeMove);
                msg.writer().writeByte(temp.speed);
                msg.writer().writeByte(temp.dartType);
            }
            session.sendMessage(msg);
            msg.cleanup();
        } catch (Exception e) {
            Logger.logException(DataGame.class, e);
        }
    }

    //vSkill
    public static void updateSkill(MySession session) {
        Message msg;
        try {
            msg = new Message(-28);

            msg.writer().writeByte(7);
            msg.writer().writeByte(vsSkill);
            msg.writer().writeByte(0); //count skill option

            msg.writer().writeByte(Manager.NCLASS.size());
            for (NClass nClass : Manager.NCLASS) {
                msg.writer().writeUTF(nClass.name);

                msg.writer().writeByte(nClass.skillTemplatess.size());
                for (SkillTemplate skillTemp : nClass.skillTemplatess) {
                    msg.writer().writeByte(skillTemp.id);
                    msg.writer().writeUTF(skillTemp.name);
                    msg.writer().writeByte(skillTemp.maxPoint);
                    msg.writer().writeByte(skillTemp.manaUseType);
                    msg.writer().writeByte(skillTemp.type);
                    msg.writer().writeShort(skillTemp.iconId);
                    msg.writer().writeUTF(skillTemp.damInfo);
                    msg.writer().writeUTF("null");
                    if (skillTemp.id != 0) {
                        msg.writer().writeByte(skillTemp.skillss.size());
                        for (Skill skill : skillTemp.skillss) {
                            msg.writer().writeShort(skill.skillId);
                            msg.writer().writeByte(skill.point);
                            msg.writer().writeLong(skill.powRequire);
                            msg.writer().writeShort(skill.manaUse);
                            msg.writer().writeInt(skill.coolDown);
                            msg.writer().writeShort(skill.dx);
                            msg.writer().writeShort(skill.dy);
                            msg.writer().writeByte(skill.maxFight);
                            msg.writer().writeShort(skill.damage);
                            msg.writer().writeShort(skill.price);
                            msg.writer().writeUTF(skill.moreInfo);
                        }
                    } else {
                        //Thêm 2 skill trống 105, 106
                        msg.writer().writeByte(skillTemp.skillss.size() + 2);
                        for (Skill skill : skillTemp.skillss) {
                            msg.writer().writeShort(skill.skillId);
                            msg.writer().writeByte(skill.point);
                            msg.writer().writeLong(skill.powRequire);
                            msg.writer().writeShort(skill.manaUse);
                            msg.writer().writeInt(skill.coolDown);
                            msg.writer().writeShort(skill.dx);
                            msg.writer().writeShort(skill.dy);
                            msg.writer().writeByte(skill.maxFight);
                            msg.writer().writeShort(skill.damage);
                            msg.writer().writeShort(skill.price);
                            msg.writer().writeUTF(skill.moreInfo);
                        }
                        for (int i = 105; i <= 106; i++) {
                            msg.writer().writeShort(i);
                            msg.writer().writeByte(0);
                            msg.writer().writeLong(0);
                            msg.writer().writeShort(0);
                            msg.writer().writeInt(0);
                            msg.writer().writeShort(0);
                            msg.writer().writeShort(0);
                            msg.writer().writeByte(0);
                            msg.writer().writeShort(0);
                            msg.writer().writeShort(0);
                            msg.writer().writeUTF("");
                        }
                    }
                }
            }
            session.doSendMessage(msg);
            msg.cleanup();
        } catch (Exception e) {
            Logger.logException(DataGame.class, e);
        }
    }

    public static void sendDataImageVersion(MySession session) {
        Message msg;
        try {
        } catch (Exception e) {
            Logger.logException(DataGame.class, e);
        }
    }

    public static void sendEffectTemplate(MySession session, int id, int... idtemp) {
        int idT = id;
        if (idtemp.length > 0 && idtemp[0] != 0) {
            idT = idtemp[0];
        }
        Message msg;
        try {
            final byte[] effData = FileIO.readFile("data/effdata/DataEffect_" + idT);
            final byte[] effImg = FileIO.readFile("data/effect/x" + session.zoomLevel + "/ImgEffect_" + idT + ".png");
            if (effData == null || effImg == null) {
                return;
            }
            msg = new Message(-66);
            msg.writer().writeShort(id);
            msg.writer().writeInt(effData.length);
            msg.writer().write(effData);
            if (session.version > 220) {
                msg.writer().write(idT == 60 ? 2 : 0);
            }
            msg.writer().writeInt(effImg.length);
            msg.writer().write(effImg);
            session.sendMessage(msg);
            msg.cleanup();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void sendBgItemVersion(MySession session) {
        Message msg;
        try {
            msg = new Message(-93);
            msg.writer().writeShort(Manager.BG_ITEMS.size());
            for (BgItem bgItem : Manager.BG_ITEMS) {
                msg.writer().writeByte(bgItem.id);
            }
            session.sendMessage(msg);
        } catch (Exception e) {
            Logger.logException(DataGame.class, e);
        }
    }

    public static void sendItemBGTemplate(MySession session, int id) {
        Message msg;
        try {
            final byte[] bg_temp = FileIO.readFile("data/item_bg_temp/x" + session.zoomLevel + "/" + id + ".png");
            if (bg_temp == null) {
                return;
            }
            msg = new Message(-32);
            msg.writer().writeShort(id);
            msg.writer().writeInt(bg_temp.length);
            msg.writer().write(bg_temp);
            session.sendMessage(msg);
            msg.cleanup();
        } catch (Exception e) {
            Logger.logException(DataGame.class, e);
        }
    }

    public static void sendDataItemBG(MySession session) {
        Message msg;
        try {
            msg = new Message(-31);
            msg.writer().writeShort(Manager.BG_ITEMS.size());
            for (BgItem bgItem : Manager.BG_ITEMS) {
                msg.writer().writeShort(bgItem.idImage);
                msg.writer().writeByte(bgItem.layer);
                msg.writer().writeShort(bgItem.dx);
                msg.writer().writeShort(bgItem.dy);
                msg.writer().writeByte(0);
            }
            session.sendMessage(msg);
            msg.cleanup();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /** Nhớ những icon client xin mà máy chủ không có, để chỉ báo một lần mỗi id. */
    private static final java.util.Set<Integer> MISSING_ICONS = java.util.concurrent.ConcurrentHashMap.newKeySet();

    public static void sendIcon(MySession session, int id) {
        Message msg;
        try {
            byte[] icon = FileIO.readFile("data/icon/x" + session.zoomLevel + "/" + id + ".png");

            // FIX: thiếu file ở đúng mức phóng to của client thì TRƯỚC ĐÂY server im lặng,
            // client chờ mãi -> khung giao diện trắng trơn. Nay thử các mức còn lại rồi mới bỏ,
            // và ghi log đúng id thiếu (mỗi id chỉ báo một lần).
            if (icon == null) {
                for (int z = 1; z <= 4 && icon == null; z++) {
                    if (z != session.zoomLevel) {
                        icon = FileIO.readFile("data/icon/x" + z + "/" + id + ".png");
                    }
                }
                if (icon != null && MISSING_ICONS.add(id)) {
                    Logger.warning("Thiếu icon " + id + " ở mức x" + session.zoomLevel
                            + ", đã gửi tạm ảnh ở mức khác\n");
                }
            }

            if (icon == null) {
                // Vẫn trả về ảnh trong suốt (icon 2955) mang đúng số client xin, để client không
                // chờ mãi / lỗi vẽ; chỉ là ô trống thay vì đơ.
                icon = FileIO.readFile("data/icon/x" + session.zoomLevel + "/2955.png");
                if (icon == null) {
                    icon = FileIO.readFile("data/icon/x2/2955.png");
                }
                if (MISSING_ICONS.add(id)) {
                    String ref = nro.models.server.Manager.ICON_REFS.get(id);
                    Logger.error("Client xin icon " + id + " nhưng KHÔNG có file data/icon/x*/"
                            + id + ".png -> " + (ref != null ? "đang được dùng bởi " + ref
                            : "không có part / vật phẩm / avatar / túi nào của máy chủ dùng icon này"
                            + " (client tự xin từ dữ liệu cache cũ hoặc dữ liệu riêng của client)")
                            + " — đã gửi ảnh trong suốt thay thế\n");
                }
                if (icon == null) {
                    return;
                }
            }

            msg = new Message(-67);
            msg.writer().writeInt(id);
            msg.writer().writeInt(icon.length);
            msg.writer().write(icon);
            session.sendMessage(msg);
            msg.cleanup();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void sendSmallVersion(MySession session) {
        Message msg;
        try {
            msg = new Message(-77);
            msg.writer().writeShort(maxSmallVersion);
            for (int i = 0; i < maxSmallVersion; i++) {
                msg.writer().writeByte(0);
            }
            session.sendMessage(msg);
            msg.cleanup();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void requestMobTemplate(MySession session, int id) {
        Message msg;
        try {
//            if (!session.check && id > 106) {
//                byte[] mob = FileIO.readFile("data/mob/x" + session.zoomLevel + "/" + 0);
//                msg = new Message(11);
//                msg.writer().writeByte(id);
//                msg.writer().write(mob);
//                session.sendMessage(msg);
//                msg.cleanup();
//                return;
//            }
            final byte[] mob = FileIO.readFile("data/mob/x" + session.zoomLevel + "/" + id);
            msg = new Message(11);
            msg.writer().writeByte(id);
            msg.writer().write(mob);
            session.sendMessage(msg);
            msg.cleanup();
        } catch (Exception e) {
        }
    }

    public static void sendTileSetInfo(MySession session) {
        Message msg;
        try {
            final byte[] data = FileIO.readFile("data/map/tile_set_info");
            msg = new Message(-82);
            msg.writer().write(data);
            session.sendMessage(msg);
            msg.cleanup();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    //data vẽ map
    public static void sendMapTemp(MySession session, int id) {
        Message msg;
        try {
            final byte[] data = FileIO.readFile("data/map/tile_map_data/" + id);
            if (data == null) {
                return;
            }
            msg = new Message(-28);
            msg.writer().writeByte(10);
            msg.writer().write(data);
            session.sendMessage(msg);
            msg.cleanup();
        } catch (Exception e) {
            Logger.logException(DataGame.class, e);
        }
    }

    //head-avatar
    public static void sendHeadAvatar(Message msg) {
        try {
            msg.writer().writeShort(Manager.HEAD_AVATARS.size());
            for (HeadAvatar ha : Manager.HEAD_AVATARS) {
                msg.writer().writeShort(ha.headId);
                msg.writer().writeShort(ha.avatarId);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void sendImageByName(MySession session, String imgName) {
        Message msg;
        try {
            msg = new Message(66);
            msg.writer().writeUTF(imgName);
            msg.writer().writeByte(Manager.getNFrameImageByName(imgName));

            final byte[] data = FileIO.readFile("data/img_by_name/x" + session.zoomLevel + "/" + imgName + ".png");

            if (data == null) {
                msg.writer().writeInt(0);
            } else {
                msg.writer().writeInt(data.length);
                msg.writer().write(data);
            }

            session.sendMessage(msg);
            msg.cleanup();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void sendVersionRes(ISession session) {
        Message msg;
        try {
            msg = new Message(-74);
            msg.writer().writeByte(0);
            msg.writer().writeInt(vsRes);
            session.sendMessage(msg);
            msg.cleanup();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void sendSizeRes(MySession session) {
        Message msg;
        try {
            msg = new Message(-74);
            msg.writer().writeByte(1);
            final File[] files = new File("data/res/x" + session.zoomLevel).listFiles();
            if (files != null) {
                msg.writer().writeShort(files.length);
            } else {
                msg.writer().writeShort(0);
            }
            session.sendMessage(msg);
            msg.cleanup();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void sendRes(MySession session) {
        Message msg;
        try {
            File dir = new File("data/res/x" + session.zoomLevel);
            File[] files = dir.listFiles();
            if (files != null) {
                for (final File fileEntry : files) {
                    String original = fileEntry.getName();
                    try (FileChannel fileChannel = FileChannel.open(fileEntry.toPath(), StandardOpenOption.READ)) {
                        ByteBuffer buffer = ByteBuffer.allocate(1024 * 1024);
                        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
                        int bytesRead;
                        while ((bytesRead = fileChannel.read(buffer)) > 0) {
                            buffer.flip();
                            byteArrayOutputStream.write(buffer.array(), 0, bytesRead);
                            buffer.clear();
                        }
                        byte[] res = byteArrayOutputStream.toByteArray();
                        msg = new Message(-74);
                        msg.writer().writeByte(2);
                        msg.writer().writeUTF(original);
                        msg.writer().writeInt(res.length);
                        msg.writer().write(res);
                        session.sendMessage(msg);
                        msg.cleanup();
                    } catch (IOException e) {
                        Logger.logException(DataGame.class, e);
                    }
                }
            }
            msg = new Message(-74);
            msg.writer().writeByte(3);
            msg.writer().writeInt(vsRes);
            session.sendMessage(msg);
            msg.cleanup();
        } catch (Exception e) {
            Logger.logException(DataGame.class, e);
        }
    }

    public static void sendLinkIP(MySession session) {
        Message msg;
        try {
            msg = new Message(-29);
            msg.writer().writeByte(2);
            msg.writer().writeUTF(LINK_IP_PORT + ",0,0");
            msg.writer().writeByte(1);
            session.sendMessage(msg);
            msg.cleanup();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
