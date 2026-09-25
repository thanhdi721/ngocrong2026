#!/usr/bin/env python3
"""Mang cải trang / linh thú (kèm ảnh) từ source SUMO (và Bun) sang source hiện tại.

Dùng:
    python3 tools/port_item_from_sumo.py            # chỉ in báo cáo, KHÔNG đụng file nào
    python3 tools/port_item_from_sumo.py --apply    # chép ảnh + ghi patch SQL

Khác với source HUNR: SUMO/Bun cùng engine "Lord" nên `part` lưu ĐÚNG định dạng của mình
(`[[icon,dx,dy],…]`), lại có sẵn bảng `head_avatar`, nên không phải đổi định dạng và
hiếm khi phải tự dựng avatar.

Việc nó làm:
  1. Đọc `item_template` + `part` + `cai_trang` + `mini_pet` + `head_avatar` của source nguồn.
  2. Chỉ lấy món bên mình CHƯA có tên, có đủ 3 part đúng số mảnh (đầu 3 / thân 17 / chân 14).
  3. Đánh số lại icon: ảnh nào trùng y hệt ảnh mình đang có thì dùng lại số cũ, còn lại cấp
     số mới trong dải trống (trần 32767 vì Manager đọc icon bằng Short.parseShort).
  4. Mức phóng to nào nguồn thiếu ảnh thì suy ra từ mức lớn nhất đang có.
  5. Sinh patch SQL: item nối tiếp id lớn nhất hiện có, part cũng vậy (hai bảng phải LIÊN TỤC
     vì client tra theo thứ tự trong danh sách).
"""

import hashlib
import os
import re
import sys

HERE = os.path.dirname(os.path.dirname(os.path.abspath(__file__)))          # .../SRC
ROOT = os.path.dirname(HERE)                                                # .../Teamobi2026
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
TRANSPARENT = 2955
AVATAR_PX = 256
NEED = {0: 3, 1: 17, 2: 14}
ZOOMS = ("1", "2", "3", "4")
PATCH_NAME = "67-cai-trang-linh-thu-tu-sumo.sql"

# Tên rác bên nguồn, không mang về (đặt lại tên thì thêm vào DOI_TEN)
BO_QUA = {"0", "s", "test", "vegetaa"}
DOI_TEN = {
    "Pét Sóii": "Pét Sói Băng",
    "Siêu Thần xd": "Siêu Thần Xayda",
    "Androi 20": "Android 20",
    "Jren": "Jiren",
    "Trunk": "Trunks",
    "Trunk SSJ": "Trunks SSJ",
    "Cải trang vageta": "Vegeta Cổ Trang",
    "Vageta Untral": "Vegeta Ultra",
    "Vegito Untral": "Vegito Ultra",
    "Thần Hủy Diện Mini": "Thần Hủy Diệt Mini",
    "quy lão Hồi Xuân": "Quy Lão Hồi Xuân",
    "gogeta blue": "Gogeta Blue",
    "zeno sama": "Zeno Sama",
    "Goku ssj 4": "Goku SSJ4",
    "vegeta ssj 4": "Vegeta SSJ4",
    "Gogeta ssj": "Gogeta SSJ",
    "Siêu goten": "Siêu Goten",
    "Siêu octiu": "Siêu Ốc Tiêu",
    "thiên sứ Cognac": "Thiên Sứ Cognac",
    "Hoá Thần": "Hóa Thần",
    "Jren": "Jiren",
    "Siêu Jrien": "Siêu Jiren",
    "Majjn Bư": "Majin Bư",
    "Athur": "Arthur",
    "Siêu santa": "Siêu Santa",
    "Super fide": "Super Fide",
    "Pet fide": "Pét Fide",
    "Pét sói": "Pét Sói",
    "Obito pét": "Pét Obito",
    "Bánh Pao Pét": "Pét Bánh Pao",
    "Gotenkssu I": "Gotenks I",
    "Gotenkssu II": "Gotenks II",
    "Gotenkssu III": "Gotenks III",
    "Broly ssj3": "Broly SSJ3",
    "Super Broly ssj4": "Super Broly SSJ4",
    "Goku ssj 4 White Ultra": "Goku SSJ4 White Ultra",
    "Vegeta ssj 4 White Ultra": "Vegeta SSJ4 White Ultra",
    "Gohan ssj 4 White Ultra": "Gohan SSJ4 White Ultra",
    "Bardock Ssj1": "Bardock SSJ",
    "Chú Bồ Đội SuMo": "Chú Bộ Đội SuMo",
    "Chú Bồ Đội Siêu Việt": "Chú Bộ Đội Siêu Việt",
    "OG73.1 (Seventhree/73)": "OG73 Seventhree",
    "Siêu Goku vô cực": "Siêu Goku Vô Cực",
    "Vegeta evo": "Vegeta Evo",
    "Goku Mui": "Goku MUI",
    "Ba đinh": "Bá Đinh",
}


def read(p):
    with open(p, encoding="utf-8", errors="replace") as f:
        return f.read()


def norm(x):
    x = re.sub(r"\s+", " ", (x or "").strip().lower())
    return re.sub(r"^(cải trang|cai trang|ct)\s+", "", x)


def md5(p):
    with open(p, "rb") as f:
        return hashlib.md5(f.read()).hexdigest()


# ----------------------------------------------------------------- bên mình
def our_data():
    """(tên đã có, icon đang dùng, id item lớn nhất, id part lớn nhất, tên flag_bag đã có)."""
    from sqlparse import load_table
    files = [OUR_SQL] + sorted(
        os.path.join(PATCH_DIR, f) for f in os.listdir(PATCH_DIR) if f.endswith(".sql"))
    names, icons, items, parts, fbnames = set(), set(), [0], [0], set()
    for f in files:
        txt = read(f)
        for r in load_table(f, "item_template")[1]:
            nm, ic, i = r.get("NAME"), r.get("icon_id"), r.get("id")
            if nm:
                names.add(norm(nm))
            if ic and ic.lstrip("-").isdigit():
                icons.add(int(ic))
            if i and i.isdigit():
                items.append(int(i))
        for r in load_table(f, "part")[1]:
            i, d = r.get("id"), r.get("DATA") or r.get("data")
            if i and i.isdigit():
                parts.append(int(i))
            if d:
                icons.update(int(x) for x in re.findall(r"\[\s*(-?\d+)\s*,", d))
        for r in load_table(f, "head_avatar")[1]:
            a = r.get("avatar_id")
            if a and a.lstrip("-").isdigit():
                icons.add(int(a))
        for r in load_table(f, "flag_bag")[1]:
            if r.get("NAME"):
                fbnames.add(norm(r["NAME"]))
        # part/item bị xoá rồi thêm lại trong cùng patch vẫn tính là đã dùng
        for m in re.finditer(r"BETWEEN (\d+) AND (\d+)", txt):
            pass
    return names, icons, max(items), max(parts), fbnames


# ----------------------------------------------------------------- bên nguồn
def nguon_data(ten):
    from sqlparse import load_table
    sqlfile, iconfmt = NGUON[ten]
    parts = {}
    for r in load_table(sqlfile, "part")[1]:
        d = r.get("DATA") or r.get("data") or ""
        pieces = [(int(a), int(b), int(c)) for a, b, c in
                  re.findall(r"\[\s*(-?\d+)\s*,\s*(-?\d+)\s*,\s*(-?\d+)\s*\]", d)]
        parts[int(r["id"])] = (int(r["TYPE"]), pieces)
    items = {int(r["id"]): r for r in load_table(sqlfile, "item_template")[1]}
    ct = {int(r["id_temp"]): r for r in load_table(sqlfile, "cai_trang")[1]}
    mp = {int(r["id_temp"]): r for r in load_table(sqlfile, "mini_pet")[1]}
    av = {}
    for r in load_table(sqlfile, "head_avatar")[1]:
        try:
            av[int(r["head_id"])] = int(r["avatar_id"])
        except (TypeError, ValueError):
            pass
    return parts, items, ct, mp, av, iconfmt


def main(argv):
    apply = "--apply" in argv
    from PIL import Image

    our_names, our_icons, max_item, max_part, _ = our_data()
    print("Bên mình: %d tên vật phẩm, icon lớn nhất đang dùng %d, item max %d, part max %d"
          % (len(our_names), max(our_icons), max_item, max_part))

    on_disk = set()
    for z in ZOOMS:
        on_disk |= {int(f[:-4]) for f in os.listdir(OUR_ICON + "/x" + z) if f[:-4].isdigit()}
    taken = on_disk | our_icons
    free_iter = (i for i in range(1, MAX_ICON_ID + 1) if i not in taken)

    chon = []          # (nguồn, item row, [pieces head, body, leg], là linh thú)
    da_lay = set(our_names)
    for ten in ("sumo", "bun"):
        parts, items, ct, mp, av, iconfmt = nguon_data(ten)

        def best_zoom(i):
            for z in ("2", "3", "4", "1"):
                if os.path.exists(iconfmt % (z, i)):
                    return z
            return None

        for i, r in sorted(items.items()):
            la_pet = i in mp
            la_ct = r["TYPE"] == "5" and i in ct
            if not (la_ct or la_pet):
                continue
            goc = (r["NAME"] or "").strip()
            ten_mon = DOI_TEN.get(goc, goc).strip()
            if not ten_mon or ten_mon.lower() in BO_QUA or norm(ten_mon) in da_lay:
                continue
            src = ct[i] if la_ct else mp[i]
            pid = [int(src["head"]), int(src["body"]), int(src["leg"])]
            if min(pid) < 0:
                continue
            ps, hong = [], False
            for p, typ in zip(pid, (0, 1, 2)):
                got = parts.get(p)
                if not got or got[0] != typ or len(got[1]) != NEED[typ]:
                    hong = True
                    break
                ps.append(got[1])
            if hong:
                continue
            da_lay.add(norm(ten_mon))
            chon.append((ten, dict(r, NAME=ten_mon), ps, la_pet, pid[0], iconfmt, best_zoom, av))

    print("Lấy được %d món (%d cải trang, %d linh thú)"
          % (len(chon), sum(1 for c in chon if not c[3]), sum(1 for c in chon if c[3])))

    icon_map, copies = {}, []          # copies: (iconfmt, best_zoom, id cũ, id mới)

    def map_icon(iconfmt, best_zoom, ic):
        key = (iconfmt, ic)
        if key in icon_map:
            return icon_map[key]
        z = best_zoom(ic)
        if z is None:
            # Nguồn không có ảnh: nếu bên mình đã có đúng số đó thì dùng lại (icon gốc của game),
            # không thì thay bằng mảnh trong suốt để khỏi hiện ô trắng.
            icon_map[key] = ic if ic in on_disk else TRANSPARENT
            return icon_map[key]
        src = iconfmt % (z, ic)
        im = Image.open(src)
        if im.width <= 4 and im.height <= 4 and not im.convert("RGBA").getbbox():
            icon_map[key] = TRANSPARENT
            return TRANSPARENT
        dst = OUR_ICON + "/x%s/%d.png" % (z, ic)
        if os.path.exists(dst) and md5(src) == md5(dst):
            icon_map[key] = ic                     # y hệt ảnh mình đang có
        else:
            new = next(free_iter)
            taken.add(new)
            icon_map[key] = new
            copies.append((iconfmt, best_zoom, ic, new))
        return icon_map[key]

    out_items, out_parts, out_avs, report = [], [], [], []
    item_id, part_id = max_item, max_part
    tu_dung_avatar = []
    for ten, r, ps, la_pet, head_goc, iconfmt, best_zoom, av in chon:
        item_id += 1
        head_id, body_id, leg_id = part_id + 1, part_id + 2, part_id + 3
        part_id += 3
        for pid, typ, pieces in ((head_id, 0, ps[0]), (body_id, 1, ps[1]), (leg_id, 2, ps[2])):
            data = "[" + ",".join(
                "[%d,%d,%d]" % (map_icon(iconfmt, best_zoom, ic) if ic >= 0 else ic, dx, dy)
                for ic, dx, dy in pieces) + "]"
            out_parts.append("(%d, %d, '%s')" % (pid, typ, data))
        icon = map_icon(iconfmt, best_zoom, int(r["icon_id"]))
        if la_pet:
            av_icon = -1
        elif head_goc in av and best_zoom(av[head_goc]) is not None:
            av_icon = map_icon(iconfmt, best_zoom, av[head_goc])
        else:
            av_icon = next(free_iter)
            taken.add(av_icon)
            tu_dung_avatar.append((av_icon, ps[0], iconfmt, best_zoom))
        if av_icon >= 0:
            out_avs.append("(%d, %d)" % (head_id, av_icon))
        name = r["NAME"].replace("'", "''")
        out_items.append("(%d, %d, 3, '%s', '%s', 0, %d, %d, 0, 0, 0, 0, %d, %d, %d)"
                         % (item_id, 27 if la_pet else 5, name,
                            "Linh thú" if la_pet else "Cải trang",
                            icon, head_id, head_id, body_id, leg_id))
        report.append("  %-4s %4s -> %4d  %-38s part %d/%d/%d icon %5d avatar %5d"
                      % (ten, r["id"], item_id, r["NAME"][:38], head_id, body_id, leg_id, icon, av_icon))

    print("\n".join(report))
    print("\nIcon dùng tới: %d | phải chép và đánh số mới: %d | avatar tự dựng: %d"
          % (len(icon_map), len(copies), len(tu_dung_avatar)))
    print("Vật phẩm %d..%d | part %d..%d" % (max_item + 1, item_id, max_part + 1, part_id))
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

    for av_icon, head_pieces, iconfmt, best_zoom in tu_dung_avatar:
        make_avatar(av_icon, head_pieces, iconfmt, best_zoom)
    print("Đã dựng %d avatar." % len(tu_dung_avatar))

    out = PATCH_DIR + "/" + PATCH_NAME
    with open(out, "w", encoding="utf-8") as f:
        f.write(HEADER % (len(chon), sum(1 for c in chon if not c[3]), sum(1 for c in chon if c[3]),
                          max_item + 1, item_id, max_part + 1, part_id,
                          max_item, max_part,
                          max_item + 1, item_id, max_part + 1, part_id, max_part + 1, part_id))
        f.write("INSERT INTO `part` (`id`, `TYPE`, `DATA`) VALUES\n" + ",\n".join(out_parts) + ";\n\n")
        if out_avs:
            f.write("INSERT INTO `head_avatar` (`head_id`, `avatar_id`) VALUES\n"
                    + ",\n".join(out_avs) + ";\n\n")
        f.write("INSERT INTO `item_template` (`id`, `TYPE`, `gender`, `NAME`, `description`, `level`,"
                " `icon_id`, `part`, `is_up_to_up`, `power_require`, `gold`, `gem`, `head`, `body`, `leg`)"
                " VALUES\n" + ",\n".join(out_items) + ";\n\n")
        f.write("SELECT `id`, `NAME`, `TYPE`, `icon_id`, `head`, `body`, `leg` FROM `item_template`"
                " WHERE `id` BETWEEN %d AND %d;\n" % (max_item + 1, item_id))
    print("Đã ghi", out)
    return 0


def make_avatar(new_id, head_pieces, iconfmt, best_zoom):
    """Ghép các mảnh đầu theo dx/dy rồi phóng to thành avatar, ghi đủ 4 mức."""
    from PIL import Image
    imgs = []
    for ic, dx, dy in head_pieces:
        z = best_zoom(ic) if ic >= 0 else None
        if z is None:
            continue
        im = Image.open(iconfmt % (z, ic)).convert("RGBA")
        if z != "4":
            f = 4 / int(z)
            im = im.resize((max(1, round(im.width * f)), max(1, round(im.height * f))), Image.LANCZOS)
        imgs.append((im, dx * 4, dy * 4))
    if not imgs:
        return
    x0 = min(x for _, x, _ in imgs)
    y0 = min(y for _, _, y in imgs)
    x1 = max(x + im.width for im, x, _ in imgs)
    y1 = max(y + im.height for im, _, y in imgs)
    canvas = Image.new("RGBA", (x1 - x0, y1 - y0), (0, 0, 0, 0))
    for im, x, y in reversed(imgs):
        canvas.alpha_composite(im, (x - x0, y - y0))
    bbox = canvas.getbbox()
    if bbox:
        canvas = canvas.crop(bbox)
    s = AVATAR_PX / max(canvas.width, canvas.height)
    canvas = canvas.resize((max(1, round(canvas.width * s)), max(1, round(canvas.height * s))),
                           Image.LANCZOS)
    for z, f in (("4", 1.0), ("3", 0.75), ("2", 0.5), ("1", 0.25)):
        im = canvas if f == 1.0 else canvas.resize(
            (max(1, round(canvas.width * f)), max(1, round(canvas.height * f))), Image.LANCZOS)
        im.save(OUR_ICON + "/x%s/%d.png" % (z, new_id), optimize=True)


HEADER = """-- =====================================================================
-- 67 — MANG %d MÓN TỪ SOURCE SUMO / BUN (sinh bởi tools/port_item_from_sumo.py)
-- =====================================================================
-- %d cải trang + %d linh thú. Vật phẩm %d..%d, part %d..%d.
--
-- * Ảnh đã chép sẵn vào data/icon/x1..x4; icon nào trùng y hệt ảnh mình đang có thì
--   dùng lại số cũ, còn lại được cấp số mới trong dải trống (trần 32767).
-- * Avatar khung chat lấy từ bảng head_avatar của nguồn; món nào nguồn không có thì
--   tự ghép từ mảnh đầu rồi phóng to.
-- * Linh thú là type 27 có đủ part, đeo ở ô số 7 như các linh thú đã mang về trước đó.
-- * Chưa có chỉ số: phát qua shop / giftcode / cpanel "Buff đồ" rồi thêm option.
--
-- Chạy cùng jar có DataGame.vsItem và vsData đã tăng. Khởi động lại server.
-- Chạy lại nhiều lần vẫn an toàn (tự xoá đúng dải của mình trước khi thêm).
-- TRƯỚC KHI CHẠY: hai câu dưới phải ra %d và %d; khác thì DỪNG, báo lại.
-- =====================================================================
SELECT MAX(`id`) AS item_max FROM `item_template`;
SELECT MAX(`id`) AS part_max FROM `part`;

DELETE FROM `item_template` WHERE `id` BETWEEN %d AND %d;
DELETE FROM `part`          WHERE `id` BETWEEN %d AND %d;
DELETE FROM `head_avatar`   WHERE `head_id` BETWEEN %d AND %d;

"""


if __name__ == "__main__":
    sys.exit(main(sys.argv[1:]))
