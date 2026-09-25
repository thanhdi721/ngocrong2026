#!/usr/bin/env python3
"""Mang cải trang (kèm ảnh) từ source HUNR sang source hiện tại.

Dùng:
    python3 tools/port_item_from_hunr.py            # chỉ in báo cáo, KHÔNG đụng file nào
    python3 tools/port_item_from_hunr.py --apply    # chép ảnh + ghi patch SQL

Việc nó làm:
  1. Đọc `nr_item` + `nr_part` bên HUNR (part lưu JSON {"id","dx","dy"}, thứ tự khoá không cố định).
  2. Chỉ lấy cải trang (type 5) mà bên mình CHƯA có tên, và có đủ 3 part đúng số mảnh
     (đầu 3 / thân 17 / chân 14) cùng đủ ảnh 4 mức phóng to.
  3. Đánh số lại icon: số nào bên mình đang dùng (dù thiếu file) hoặc đã có file thì cấp số mới
     từ dải trống, trần 32767 (Manager.loadDatabase đọc id icon bằng Short.parseShort).
  4. Avatar: lấy từ bảng `nr_others` key 'avatar' của HUNR; món nào không có thì tự ghép
     mảnh đầu rồi phóng to ~256px.
  5. Sinh patch SQL: item nối tiếp id lớn nhất hiện có, part nối tiếp part lớn nhất hiện có
     (hai bảng phải liên tục vì client tra theo thứ tự).
"""

import hashlib
import json
import os
import re
import sys

HUNR = "/Users/phanthanhdi/Downloads/HUNR_Server/Hunr2026"
HERE = os.path.dirname(os.path.dirname(os.path.abspath(__file__)))          # .../SRC
ROOT = os.path.dirname(HERE)                                                # .../Teamobi2026
HUNR_SQL = HUNR + "/sql/SQL_HUNR_2025.sql"
HUNR_ICON = HUNR + "/resources/image/%s/small/Small%d.png"
OUR_ICON = HERE + "/data/icon"
PATCH_DIR = HERE + "/sql/patch"
OUR_SQL = ROOT + "/database team2026.sql"
OUR_PATCHES = ["01-vat-pham-moi", "35-vat-pham-tu-ngol", "40-cai-trang-vip-moi", "48-cai-trang-tu-hunr",
               "49-cai-trang-pet-tu-hunr"]

MAX_ICON_ID = 32767
AVATAR_PX = 256        # cỡ avatar ở mức x4, theo các avatar sẵn có (248–256 px)
NEED = {0: 3, 1: 17, 2: 14}
ZOOMS = ("1", "2", "3", "4")
# Tên rác / trùng bên HUNR, không mang về
SKIP_NAMES = {"dsdd", "ư3ewqe", "434343", "s", "cải trang sự kiện"}
# Loại vật phẩm bên HUNR lấy được: 5 cải trang; 18/19/21/38 là pet của họ — bên mình không có
# hệ pet riêng nên mang về dưới dạng CẢI TRANG (đủ 3 part nên mặc được, đệ tử mặc cũng được).
HUNR_TYPES = (5, 18, 19, 21, 27, 38)
PATCH_NAME = "54-linh-thu-them-tu-hunr.sql"


def read(p):
    with open(p, encoding="utf-8", errors="replace") as f:
        return f.read()


def hunr_section(sql, table):
    i = sql.index("INSERT INTO `%s`" % table)
    return sql[i:sql.index("-- ----------------------------", i + 10)]


def hunr_parts(sql):
    out = {}
    for m in re.finditer(r"VALUES \((\d+), (\d+), '(\[.*?\])', '", hunr_section(sql, "nr_part")):
        try:
            pieces = json.loads(m.group(3).replace('\\"', '"'))
        except ValueError:
            continue
        out[int(m.group(1))] = (int(m.group(2)),
                                [(int(p["id"]), int(p["dx"]), int(p["dy"])) for p in pieces])
    return out


def hunr_avatars(sql):
    """HUNR để avatar trong nr_others key 'avatar': [{"head": <id part đầu>, "avatar": <id icon>}]."""
    import json as _json
    i = sql.index("INSERT INTO `nr_others`")
    sec = sql[i:sql.index("-- ----------------------------", i + 10)]
    m = re.search(r"\(\d+, 'avatar', '(\[.*?\])'\)", sec)
    if not m:
        return {}
    return {int(x["head"]): int(x["avatar"]) for x in _json.loads(m.group(1).replace('\\"', '"'))}


def hunr_items(sql):
    rows = re.finditer(
        r"\((\d+), '((?:[^'\\]|\\.)*)', (\d+), (-?\d+), '((?:[^'\\]|\\.)*)', (\d+), (-?\d+), (-?\d+),"
        r" (-?\d+), (-?\d+), (\d+), (-?\d+), (-?\d+), (-?\d+), (?:'(?:[^'\\]|\\.)*'|NULL), (-?\d+)",
        hunr_section(sql, "nr_item"))
    return [dict(id=int(m.group(1)), name=m.group(2), type=int(m.group(3)), icon=int(m.group(9)),
                 head=int(m.group(12)), body=int(m.group(13)), leg=int(m.group(14))) for m in rows]


def our_data():
    """Trả về (tên vật phẩm đã có, icon đang được dùng, id vật phẩm lớn nhất, id part lớn nhất)."""
    sql = read(OUR_SQL)
    sec = lambda t: sql[sql.index("INSERT INTO `%s`" % t):sql.index("CREATE TABLE", sql.index("INSERT INTO `%s`" % t))]
    blocks = [sec("item_template"), sec("part"), sec("head_avatar")]
    for p in OUR_PATCHES:
        blocks.append(read(PATCH_DIR + "/%s.sql" % p))
    names, icons, items, parts = set(), set(), [], []
    for b in blocks:
        for m in re.finditer(r"^\((\d+), *(\d+), *-?\d+, *'((?:[^'\\]|\\.)*)', *'(?:[^'\\]|\\.)*', *\d+, *(\d+),", b, re.M):
            names.add(norm(m.group(3)))
            icons.add(int(m.group(4)))
            items.append(int(m.group(1)))
        for m in re.finditer(r"^\((\d+), *[012], *'(\[\[.*?)'\)", b, re.M):
            parts.append(int(m.group(1)))
            icons.update(int(x) for x in re.findall(r"\[\s*(-?\d+)\s*,", m.group(2)))
        for m in re.finditer(r"^\((\d+), (\d+)\)[,;]", b, re.M):
            icons.add(int(m.group(2)))
    return names, icons, max(items), max(parts)


def norm(x):
    return re.sub(r"\s+", " ", x.strip().lower())


def best_zoom(i):
    """Mức phóng to lớn nhất mà HUNR có ảnh cho icon này (None nếu không có mức nào)."""
    for z in ("4", "3", "2", "1"):
        if os.path.exists(HUNR_ICON % (z, i)):
            return z
    return None


def icon_files_ok(i):
    # HUNR thiếu nhiều ảnh mức x1 (9.427 / 12.557). Chỉ cần có MỘT mức, các mức còn lại
    # tự phóng / thu theo tỉ lệ khi chép.
    return best_zoom(i) is not None


def md5(p):
    with open(p, "rb") as f:
        return hashlib.md5(f.read()).hexdigest()


def main(argv):
    apply = "--apply" in argv
    sql = read(HUNR_SQL)
    parts, items = hunr_parts(sql), hunr_items(sql)
    hunr_av = hunr_avatars(sql)
    our_names, our_icons, max_item, max_part = our_data()

    chosen = []
    for it in items:
        if it["type"] not in HUNR_TYPES or norm(it["name"]) in our_names or norm(it["name"]) in SKIP_NAMES:
            continue
        if min(it["head"], it["body"], it["leg"]) < 0:
            continue
        ps, bad = [], False
        for pid, typ in ((it["head"], 0), (it["body"], 1), (it["leg"], 2)):
            p = parts.get(pid)
            if not p or p[0] != typ or len(p[1]) != NEED[typ]:
                bad = True
                break
            if any(ic >= 0 and not icon_files_ok(ic) for ic, _, _ in p[1]):
                bad = True
                break
            ps.append(p[1])
        if bad or it["icon"] < 0 or not icon_files_ok(it["icon"]):
            continue
        chosen.append((it, ps))

    # Dải số icon còn trống bên mình: chưa có file ở mức nào và dữ liệu cũng không dùng
    on_disk = set()
    for z in ZOOMS:
        on_disk |= {int(f[:-4]) for f in os.listdir(OUR_ICON + "/x" + z) if f[:-4].isdigit()}
    taken = on_disk | our_icons
    free = (i for i in range(1, MAX_ICON_ID + 1) if i not in taken)

    icon_map, copies = {}, []
    # Mảnh trong suốt: HUNR mã hoá PNG khác mình nên md5 không khớp, đừng cấp số mới cho nó.
    TRANSPARENT = 2955
    def map_icon(ic):
        if ic in icon_map:
            return icon_map[ic]
        z = best_zoom(ic)
        from PIL import Image as _I
        a = _I.open(HUNR_ICON % (z, ic)).convert("RGBA")
        if a.width <= 4 and a.height <= 4 and not a.getbbox():
            icon_map[ic] = TRANSPARENT
            return TRANSPARENT
        src, dst = HUNR_ICON % (z, ic), OUR_ICON + "/x%s/%d.png" % (z, ic)
        same = os.path.exists(dst) and md5(src) == md5(dst)
        if same:
            icon_map[ic] = ic
        else:
            new = next(free)
            taken.add(new)
            icon_map[ic] = new
            copies.append((ic, new))
        return icon_map[ic]

    out_items, out_parts, out_avs, report = [], [], [], []
    item_id, part_id = max_item, max_part
    avatars = []          # (icon id mới, part đầu bên HUNR) -> dựng ảnh lúc --apply
    for it, ps in chosen:
        item_id += 1
        head_id, body_id, leg_id = part_id + 1, part_id + 2, part_id + 3
        part_id += 3
        for pid, typ, pieces in ((head_id, 0, ps[0]), (body_id, 1, ps[1]), (leg_id, 2, ps[2])):
            data = "[" + ",".join("[%d,%d,%d]" % (map_icon(ic) if ic >= 0 else ic, dx, dy)
                                  for ic, dx, dy in pieces) + "]"
            out_parts.append("(%d, %d, '%s')" % (pid, typ, data))
        is_pet = it["type"] != 5          # 18/19/21/27/38 bên HUNR đều là linh thú đi theo
        real_av = None if is_pet else hunr_av.get(it["head"])
        if real_av is not None and icon_files_ok(real_av):
            av_icon = map_icon(real_av)          # avatar thật bên HUNR, chỉ cần chép ảnh
        elif is_pet:
            av_icon = -1                         # linh thú không cần avatar
        else:
            av_icon = next(free)                 # không có thì tự ghép từ mảnh đầu
            taken.add(av_icon)
            avatars.append((av_icon, ps[0]))
        if av_icon >= 0:
            out_avs.append("(%d, %d)" % (head_id, av_icon))
        icon = map_icon(it["icon"])
        name = it["name"].strip().replace("'", "''")
        out_items.append("(%d, %d, 3, '%s', '%s', 0, %d, %d, 0, 0, 0, 0, %d, %d, %d)"
                         % (item_id, 27 if is_pet else 5, name,
                            "Linh thú đi theo" if is_pet else "Cải trang",
                            icon, head_id, head_id, body_id, leg_id))
        report.append("  %4d -> %4d  %-42s part %d/%d/%d  icon %d  avatar %d"
                      % (it["id"], item_id, it["name"][:42], head_id, body_id, leg_id, icon, av_icon))

    print("Cải trang mang về: %d" % len(chosen))
    print("\n".join(report))
    print("\nẢnh cần chép: %d (đánh số mới: %d) | avatar tự dựng: %d"
          % (len(icon_map), sum(1 for a, b in copies if a != b), len(avatars)))
    print("Vật phẩm %d..%d | part %d..%d" % (max_item + 1, item_id, max_part + 1, part_id))
    if not apply:
        print("\n(chạy lại với --apply để chép ảnh và ghi patch)")
        return 0

    from PIL import Image
    n = 0
    for old, new in copies:
        z0 = best_zoom(old)
        base = Image.open(HUNR_ICON % (z0, old)).convert("RGBA")
        for z in ZOOMS:
            dst = OUR_ICON + "/x%s/%d.png" % (z, new)
            if os.path.exists(dst):
                continue
            src = HUNR_ICON % (z, old)
            if os.path.exists(src):
                im = Image.open(src).convert("RGBA")
            else:                       # mức thiếu: suy ra từ mức lớn nhất đang có
                f = int(z) / int(z0)
                im = base.resize((max(1, round(base.width * f)), max(1, round(base.height * f))), Image.LANCZOS)
            im.save(dst, optimize=True)
            n += 1
    print("Đã chép %d file ảnh." % n)

    for av_icon, head_pieces in avatars:
        make_avatar(av_icon, head_pieces)
    print("Đã dựng %d avatar." % len(avatars))

    out = PATCH_DIR + "/" + PATCH_NAME
    with open(out, "w", encoding="utf-8") as f:
        f.write(HEADER % (len(chosen), max_item + 1, item_id, max_part + 1, part_id,
                          max_item, max_part, max_item + 1, item_id,
                          max_part + 1, part_id, max_part + 1, part_id))
        f.write("INSERT INTO `part` (`id`, `TYPE`, `DATA`) VALUES\n" + ",\n".join(out_parts) + ";\n\n")
        if out_avs:
            f.write("INSERT INTO `head_avatar` (`head_id`, `avatar_id`) VALUES\n" + ",\n".join(out_avs) + ";\n\n")
        f.write("INSERT INTO `item_template` (`id`, `TYPE`, `gender`, `NAME`, `description`, `level`,"
                " `icon_id`, `part`, `is_up_to_up`, `power_require`, `gold`, `gem`, `head`, `body`, `leg`)"
                " VALUES\n" + ",\n".join(out_items) + ";\n\n")
        f.write("SELECT `id`, `NAME`, `icon_id`, `head`, `body`, `leg` FROM `item_template`"
                " WHERE `id` BETWEEN %d AND %d;\n" % (max_item + 1, item_id))
    print("Đã ghi", out)
    return 0


def make_avatar(new_id, head_pieces):
    """Ghép các mảnh đầu (mức x4) theo dx/dy rồi phóng to thành avatar, ghi đủ 4 mức."""
    from PIL import Image
    imgs = []
    for ic, dx, dy in head_pieces:
        z = best_zoom(ic)
        if ic < 0 or z is None:
            continue
        im = Image.open(HUNR_ICON % (z, ic)).convert("RGBA")
        if z != "4":                    # quy về cùng mức x4 rồi mới ghép
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
    for im, x, y in reversed(imgs):          # mảnh 0 (mặt) vẽ sau cùng cho nổi lên trên
        canvas.alpha_composite(im, (x - x0, y - y0))
    bbox = canvas.getbbox()
    if bbox:
        canvas = canvas.crop(bbox)
    s = AVATAR_PX / max(canvas.width, canvas.height)
    canvas = canvas.resize((max(1, round(canvas.width * s)), max(1, round(canvas.height * s))), Image.LANCZOS)
    for z, f in (("4", 1.0), ("3", 0.75), ("2", 0.5), ("1", 0.25)):
        im = canvas if f == 1.0 else canvas.resize(
            (max(1, round(canvas.width * f)), max(1, round(canvas.height * f))), Image.LANCZOS)
        im.save(OUR_ICON + "/x%s/%d.png" % (z, new_id), optimize=True)


HEADER = """-- =====================================================================
-- MANG %d CẢI TRANG / PET TỪ SOURCE HUNR (sinh bởi tools/port_item_from_hunr.py)
-- =====================================================================
-- Vật phẩm %d..%d, part %d..%d, avatar tự dựng từ mảnh đầu.
--
-- * Ảnh đã chép sẵn vào data/icon/x1..x4, icon trùng số với mình thì đã đánh số lại.
-- * HUNR không có bảng head_avatar nên avatar được ghép từ mảnh đầu rồi phóng to.
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
