#!/usr/bin/env python3
"""Mang đồ ĐEO LƯNG (flag_bag) từ source SUMO / Bun sang source hiện tại.

Dùng:
    python3 tools/port_flagbag_from_sumo.py            # chỉ in báo cáo
    python3 tools/port_flagbag_from_sumo.py --apply    # chép ảnh + ghi patch SQL

Hai bên cùng định dạng: `flag_bag.icon_data` là danh sách id icon cách nhau bằng dấu phẩy
(các khung hình của món đeo lưng), `item_template` loại 11 trỏ tới nó bằng cột `part`.

Lọc trùng KHÔNG theo tên mà theo ẢNH: món nào có dãy icon giống hệt (md5 từng file) một món
mình đang có thì bỏ, nên không sợ cùng một cái cờ mà hai bên đặt tên khác nhau.

Giới hạn: id flag_bag gửi cho client bằng MỘT byte (FlagBagService, Service.sendFlagBag)
nên không được vượt 255.
"""

import hashlib
import os
import re
import sys

HERE = os.path.dirname(os.path.dirname(os.path.abspath(__file__)))
ROOT = os.path.dirname(HERE)
sys.path.insert(0, os.path.join(HERE, "tools"))

NGUON = {
    "sumo": ("/Users/phanthanhdi/Downloads/SRC-SUMOV2/SRC JAV/huyensumo.sql",
             "/Users/phanthanhdi/Downloads/SRC-SUMOV2/SRC JAV/resources/normal/image/%s/icon/%d.png"),
    "bun": ("/Users/phanthanhdi/Downloads/SrcBun/bunthoi (4).sql",
            "/Users/phanthanhdi/Downloads/SrcBun/resources/normal/image/%s/icon/%d.png"),
}
OUR_ICON = HERE + "/data/icon"
PATCH_DIR = HERE + "/sql/patch"
OUR_SQL = ROOT + "/database team2026.sql"

MAX_ICON_ID = 32767
MAX_FLAGBAG_ID = 255
TRANSPARENT = 2955
ZOOMS = ("1", "2", "3", "4")
PATCH_NAME = "69-deo-lung-tu-sumo.sql"

DOI_TEN = {
    "V?ng thánh": "Vòng thánh",
    "Giỏ củ cảii trắng": "Giỏ củ cải trắng",
    "godki": "Hào quang Thần",
    "hit ki": "Hào quang Hit",
    "hào quang vip": "Hào quang VIP",
    "cá xám": "Cá xám",
    "pháp trận tru tiên": "Pháp trận Tru Tiên",
    "Fashion Wing 1": "Cánh thời trang 1",
    "Fashion Wing 3": "Cánh thời trang 3",
    "Fashion Wing 5": "Cánh thời trang 5",
    "Fashion Wing 6": "Cánh thời trang 6",
    "Fashion Wing 7": "Cánh thời trang 7",
    "Fashion Wing 8": "Cánh thời trang 8",
    "Cánh Vip": "Cánh VIP",
}
BO_QUA = {"flag_bag", "0", ""}


def md5f(p):
    with open(p, "rb") as f:
        return hashlib.md5(f.read()).hexdigest()


def ids(s):
    return [int(x) for x in re.findall(r"-?\d+", s or "")]


def main(argv):
    apply = "--apply" in argv
    from sqlparse import load_table
    from PIL import Image

    # ---------------- bên mình
    our_fb, our_items, our_icons = {}, [0], set()
    files = [OUR_SQL] + sorted(os.path.join(PATCH_DIR, f) for f in os.listdir(PATCH_DIR)
                               if f.endswith(".sql") and not f.startswith("69-"))
    for f in files:
        for r in load_table(f, "flag_bag")[1]:
            if r.get("id") and r["id"].lstrip("-").isdigit():
                our_fb[int(r["id"])] = r
        for r in load_table(f, "item_template")[1]:
            if r.get("id") and r["id"].isdigit():
                our_items.append(int(r["id"]))
            if r.get("icon_id") and r["icon_id"].lstrip("-").isdigit():
                our_icons.add(int(r["icon_id"]))
        for r in load_table(f, "part")[1]:
            d = r.get("DATA") or ""
            our_icons.update(int(x) for x in re.findall(r"\[\s*(-?\d+)\s*,", d))
        for r in load_table(f, "head_avatar")[1]:
            if r.get("avatar_id") and r["avatar_id"].lstrip("-").isdigit():
                our_icons.add(int(r["avatar_id"]))
    max_fb = max(our_fb)
    max_item = max(our_items)

    on_disk = set()
    for z in ZOOMS:
        on_disk |= {int(x[:-4]) for x in os.listdir(OUR_ICON + "/x" + z) if x[:-4].isdigit()}
    taken = on_disk | our_icons
    free_iter = (i for i in range(1, MAX_ICON_ID + 1) if i not in taken)

    def chu_ky_cua_minh(r):
        """Dãy md5 của các icon (mức x2) — dùng để nhận ra món trùng ảnh."""
        out = []
        for ic in ids(r.get("icon_data")):
            p = OUR_ICON + "/x2/%d.png" % ic
            out.append(md5f(p) if os.path.exists(p) else "?%d" % ic)
        return tuple(out)

    da_co = {chu_ky_cua_minh(r) for r in our_fb.values()}
    print("Bên mình: %d đồ đeo lưng (id lớn nhất %d), item lớn nhất %d" % (len(our_fb), max_fb, max_item))

    # ---------------- bên nguồn
    chon = []
    da_lay_ten = {(r.get('NAME') or '').strip().lower() for r in our_fb.values()}
    for ten in ("sumo", "bun"):
        sqlfile, iconfmt = NGUON[ten]
        fbs = load_table(sqlfile, "flag_bag")[1]
        its = load_table(sqlfile, "item_template")[1]
        item_theo_part = {}
        for r in its:
            if r["TYPE"] == "11" and r["part"].lstrip("-").isdigit():
                item_theo_part.setdefault(int(r["part"]), r)

        def best_zoom(i):
            for z in ("2", "3", "4", "1"):
                if os.path.exists(iconfmt % (z, i)):
                    return z
            return None

        for r in fbs:
            icl = ids(r.get("icon_data"))
            if not icl:
                continue
            if any(best_zoom(i) is None for i in icl):
                continue                      # thiếu ảnh -> bỏ, đừng để ô trắng
            ck = tuple(md5f(iconfmt % (best_zoom(i), i)) if best_zoom(i) == "2"
                       else "z%d" % i for i in icl)
            if ck in da_co:
                continue                      # trùng ảnh với món mình đang có
            it = item_theo_part.get(int(r["id"]))
            goc = ((it or {}).get("NAME") or r.get("NAME") or "").strip()
            ten_mon = DOI_TEN.get(goc, goc).strip()
            if ten_mon.lower() in BO_QUA or ten_mon.lower() in da_lay_ten:
                continue
            da_co.add(ck)
            da_lay_ten.add(ten_mon.lower())
            chon.append((ten, iconfmt, best_zoom, r, it, ten_mon, icl))

    print("Đồ đeo lưng lấy được: %d" % len(chon))
    if max_fb + len(chon) > MAX_FLAGBAG_ID:
        print("DỪNG: id flag_bag sẽ vượt %d (client đọc id bằng 1 byte)." % MAX_FLAGBAG_ID)
        return 1

    icon_map, copies = {}, []

    def map_icon(iconfmt, best_zoom, ic):
        key = (iconfmt, ic)
        if key in icon_map:
            return icon_map[key]
        z = best_zoom(ic)
        if z is None:
            icon_map[key] = ic if ic in on_disk else TRANSPARENT
            return icon_map[key]
        src = iconfmt % (z, ic)
        im = Image.open(src)
        if im.width <= 4 and im.height <= 4 and not im.convert("RGBA").getbbox():
            icon_map[key] = TRANSPARENT
            return TRANSPARENT
        dst = OUR_ICON + "/x%s/%d.png" % (z, ic)
        if os.path.exists(dst) and md5f(src) == md5f(dst):
            icon_map[key] = ic
        else:
            new = next(free_iter)
            taken.add(new)
            icon_map[key] = new
            copies.append((iconfmt, best_zoom, ic, new))
        return icon_map[key]

    out_fb, out_items, report = [], [], []
    fb_id, item_id = max_fb, max_item
    for ten, iconfmt, best_zoom, r, it, ten_mon, icl in chon:
        fb_id += 1
        item_id += 1
        moi = [map_icon(iconfmt, best_zoom, i) for i in icl]
        ic_goc = int((it or {}).get("icon_id", r.get("icon_id") or -1))
        icon = map_icon(iconfmt, best_zoom, ic_goc) if ic_goc >= 0 else moi[0]
        out_fb.append("(%d, '%s', '%s', -1, -1, %d)"
                      % (fb_id, ",".join(str(x) for x in moi), ten_mon.replace("'", "''"), icon))
        out_items.append("(%d, 11, 3, '%s', 'Vật phẩm đeo lưng', 0, %d, %d, 0, 0, 0, 0, -1, -1, -1)"
                         % (item_id, ten_mon.replace("'", "''"), icon, fb_id))
        report.append("  %-4s fb %3s -> %3d  item -> %4d  %-32s %d khung"
                      % (ten, r["id"], fb_id, item_id, ten_mon[:32], len(moi)))

    print("\n".join(report))
    print("\nIcon dùng tới: %d | phải chép và đánh số mới: %d" % (len(icon_map), len(copies)))
    print("flag_bag %d..%d | vật phẩm %d..%d" % (max_fb + 1, fb_id, max_item + 1, item_id))
    if not apply:
        print("\n(chạy lại với --apply để chép ảnh và ghi patch)")
        return 0

    n = 0
    for iconfmt, best_zoom, old, new in copies:
        z0 = best_zoom(old)
        base = Image.open(iconfmt % (z0, old)).convert("RGBA")
        for z in ZOOMS:
            dst = OUR_ICON + "/x%s/%d.png" % (z, new)
            if os.path.exists(dst):
                continue
            src = iconfmt % (z, old)
            if os.path.exists(src):
                im = Image.open(src).convert("RGBA")
            else:
                f = int(z) / int(z0)
                im = base.resize((max(1, round(base.width * f)), max(1, round(base.height * f))),
                                 Image.LANCZOS)
            im.save(dst, optimize=True)
            n += 1
    print("Đã chép %d file ảnh." % n)

    with open(PATCH_DIR + "/" + PATCH_NAME, "w", encoding="utf-8") as f:
        f.write(HEADER % (len(chon), max_fb + 1, fb_id, max_item + 1, item_id,
                          max_fb, max_item,
                          max_item + 1, item_id, max_fb + 1, fb_id))
        f.write("INSERT INTO `flag_bag` (`id`, `icon_data`, `NAME`, `gold`, `gem`, `icon_id`) VALUES\n"
                + ",\n".join(out_fb) + ";\n\n")
        f.write("INSERT INTO `item_template` (`id`, `TYPE`, `gender`, `NAME`, `description`, `level`,"
                " `icon_id`, `part`, `is_up_to_up`, `power_require`, `gold`, `gem`, `head`, `body`, `leg`)"
                " VALUES\n" + ",\n".join(out_items) + ";\n\n")
        f.write("SELECT `id`, `NAME`, `icon_id`, `part` FROM `item_template` WHERE `id` BETWEEN %d AND %d;\n"
                % (max_item + 1, item_id))
    print("Đã ghi", PATCH_DIR + "/" + PATCH_NAME)
    return 0


HEADER = """-- =====================================================================
-- 69 — MANG %d ĐỒ ĐEO LƯNG TỪ SOURCE SUMO / BUN (sinh bởi tools/port_flagbag_from_sumo.py)
-- =====================================================================
-- flag_bag %d..%d, vật phẩm %d..%d (loại 11, `part` = id flag_bag).
--
-- * Lọc trùng theo ẢNH (md5 từng khung) chứ không theo tên, nên món nào mình đã có
--   dù đặt tên khác cũng không bị nhập lại.
-- * Ảnh đã chép sẵn vào data/icon/x1..x4, icon trùng số thì đã đánh số lại.
-- * id flag_bag gửi cho client bằng MỘT byte nên tối đa 255 — còn nhiều chỗ.
-- * Chưa có chỉ số: phát qua shop / giftcode / cpanel "Buff đồ" rồi thêm option.
--
-- Chạy cùng jar có DataGame.vsItem đã tăng. Khởi động lại server.
-- TRƯỚC KHI CHẠY: hai câu dưới phải ra %d và %d; khác thì DỪNG, báo lại.
-- =====================================================================
SELECT MAX(`id`) AS flag_bag_max FROM `flag_bag`;
SELECT MAX(`id`) AS item_max FROM `item_template`;

DELETE FROM `item_template` WHERE `id` BETWEEN %d AND %d;
DELETE FROM `flag_bag`      WHERE `id` BETWEEN %d AND %d;

"""


if __name__ == "__main__":
    sys.exit(main(sys.argv[1:]))
