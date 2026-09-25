#!/usr/bin/env python3
"""Chuyển vật phẩm (kèm ảnh) từ source NRO NGOL sang source hiện tại.

Dùng:
    python3 tools/port_item_from_ngol.py 2147 2146 2140
    python3 tools/port_item_from_ngol.py --apply 2147      # chép ảnh thật + ghi file patch

Không có --apply thì chỉ in báo cáo, KHÔNG đụng vào file nào.

Việc nó làm:
  1. Đọc dòng `item_template` của id bên NGOL (schema 9 cột) và ánh xạ sang schema 15 cột của mình.
  2. Cải trang: tra bảng `cai_trang` để lấy 3 dòng `part` (đầu / thân / chân).
  3. Gom mọi icon mà vật phẩm + các part đó dùng; chép file PNG 4 mức phóng to
     từ resources/normal/image/{1,2,3,4}/icon/ sang SRC/data/icon/x{1,2,3,4}/.
  4. Icon trùng số với ảnh đang có mà NỘI DUNG KHÁC -> cấp số mới từ NEW_ICON_BASE
     và sửa lại dữ liệu part cho khớp.
  5. Sinh patch SQL: item_template nối tiếp id lớn nhất hiện có, part nối tiếp id part
     lớn nhất hiện có (cả hai bảng phải liên tục vì client tra theo thứ tự).
"""

import hashlib
import os
import re
import shutil
import sys

NGOL = "/Users/phanthanhdi/Downloads/NRO NGOL"
HERE = os.path.dirname(os.path.dirname(os.path.abspath(__file__)))  # .../SRC
NGOL_SQL = os.path.join(NGOL, "sql", "nrognol.sql")
OUR_SQL = os.path.join(os.path.dirname(HERE), "database team2026.sql")
OUR_ICON = os.path.join(HERE, "data", "icon")
NGOL_ICON = os.path.join(NGOL, "resources", "normal", "image")
PATCH_DIR = os.path.join(HERE, "sql", "patch")

# Dải số icon cấp cho ảnh mang từ nguồn khác về.
# BẮT BUỘC <= 32767: Manager.loadDatabase đọc id icon bằng Short.parseShort, số lớn hơn
# làm server không khởi động được ("Value out of range").
NEW_ICON_BASE = 32349
MAX_ICON_ID = 32767
ZOOMS = [("1", "x1"), ("2", "x2"), ("3", "x3"), ("4", "x4")]


def read(path):
    with open(path, encoding="utf-8", errors="replace") as f:
        return f.read()


def section(sql, table):
    i = sql.index("INSERT INTO `%s`" % table)
    return sql[i:sql.index("CREATE TABLE", i)]


def ngol_items(sql):
    out = {}
    for m in re.finditer(
            r"^\((\d+), (\d+), (-?\d+), '((?:[^'\\]|\\.)*)', '((?:[^'\\]|\\.)*)', (\d+), (-?\d+), (\d+), (\d+)\)",
            section(sql, "item_template"), re.M):
        out[int(m.group(1))] = dict(id=int(m.group(1)), type=int(m.group(2)), gender=int(m.group(3)),
                                    name=m.group(4), desc=m.group(5), icon=int(m.group(6)),
                                    part=int(m.group(7)), up=int(m.group(8)), power=int(m.group(9)))
    return out


def ngol_costume(sql):
    out = {}
    for m in re.finditer(r"^\((\d+), (-?\d+), (-?\d+), (-?\d+), (-?\d+)\)", section(sql, "cai_trang"), re.M):
        out[int(m.group(1))] = (int(m.group(2)), int(m.group(3)), int(m.group(4)))
    return out


def parts_of(sql, table_sql=None):
    out = {}
    for m in re.finditer(r"^\((\d+), (\d+), '(.*?)'\)[,;]", section(sql, "part"), re.M):
        out[int(m.group(1))] = (int(m.group(2)), m.group(3))
    return out


def icons_in_part(data):
    return [int(x) for x in re.findall(r"\[\s*(-?\d+)\s*,\s*-?\d+\s*,\s*-?\d+\s*\]", data)]


def our_used_icons(our_sql):
    """Mọi số icon mà dữ liệu hiện tại ĐANG dùng: icon vật phẩm, icon trong part, avatar.

    Không được cấp lại số này cho ảnh mang từ nguồn khác, kể cả khi máy chủ đang thiếu file:
    chép ảnh NGOL vào đó là đồ cũ của mình hiện sai hình.
    """
    used = set()
    for m in re.finditer(r"^\(\d+, \d+, -?\d+, '(?:[^'\\]|\\.)*', '(?:[^'\\]|\\.)*', \d+, (\d+),",
                         section(our_sql, "item_template"), re.M):
        used.add(int(m.group(1)))
    for m in re.finditer(r"^\(\d+, \d+, '(.*?)'\)", section(our_sql, "part"), re.M):
        used.update(int(x) for x in re.findall(r"\[\s*(-?\d+)\s*,", m.group(1)))
    used.update(int(m.group(1)) for m in re.finditer(r"^\(\d+, (\d+)\)", section(our_sql, "head_avatar"), re.M))
    return used


def ngol_head_avatar(sql):
    return {int(m.group(1)): int(m.group(2))
            for m in re.finditer(r"^\((\d+), (\d+)\)", section(sql, "head_avatar"), re.M)}


def our_max_ids(our_sql):
    items = [int(m.group(1)) for m in re.finditer(r"^\((\d+), \d+, -?\d+, '", section(our_sql, "item_template"), re.M)]
    parts = [int(m.group(1)) for m in re.finditer(r"^\((\d+), \d+, '", section(our_sql, "part"), re.M)]
    # patch 01 đã thêm vật phẩm 2000..2031
    patch01 = os.path.join(PATCH_DIR, "01-vat-pham-moi.sql")
    if os.path.exists(patch01):
        extra = [int(m.group(1)) for m in re.finditer(r"^\((\d+),\s+\d+, \d+, '", read(patch01), re.M)]
        items += extra
    return max(items), max(parts)


def file_hash(path):
    with open(path, "rb") as f:
        return hashlib.md5(f.read()).hexdigest()


def main(argv):
    apply = "--apply" in argv
    ids = [int(x) for x in argv if x.isdigit()]
    if not ids:
        print(__doc__)
        return 1

    ngol = read(NGOL_SQL)
    items = ngol_items(ngol)
    costumes = ngol_costume(ngol)
    nparts = parts_of(ngol)
    navatar = ngol_head_avatar(ngol)
    our = read(OUR_SQL)
    used_icons = our_used_icons(our)
    next_item, next_part = [x + 1 for x in our_max_ids(our)]
    avatar_sql = []

    icon_map = {}       # icon NGOL -> icon bên mình
    next_icon = NEW_ICON_BASE
    copies = []         # (icon NGOL, icon mới)
    item_sql, part_sql, report = [], [], []

    for iid in ids:
        it = items.get(iid)
        if not it:
            report.append("id %d: KHÔNG có trong item_template của NGOL" % iid)
            continue
        head = body = leg = -1
        used_parts = []
        if iid in costumes:
            used_parts = list(costumes[iid])

        need_icons = [it["icon"]]
        for p in used_parts:
            if p in nparts:
                need_icons += icons_in_part(nparts[p][1])
        old_head = used_parts[0] if used_parts else -1
        avatar_icon = navatar.get(old_head, -1)
        if avatar_icon >= 0:
            need_icons.append(avatar_icon)

        for ic in need_icons:
            if ic < 0 or ic in icon_map:
                continue
            src = os.path.join(NGOL_ICON, "2", "icon", "%d.png" % ic)
            dst = os.path.join(OUR_ICON, "x2", "%d.png" % ic)
            if not os.path.exists(src):
                icon_map[ic] = ic          # không có file -> giữ nguyên, báo cáo
                report.append("  thiếu file ảnh icon %d bên NGOL" % ic)
                continue
            # Giữ nguyên số CHỈ khi ảnh y hệt ảnh đang có. Số đang được dữ liệu mình dùng
            # (dù máy chủ thiếu file) thì luôn phải đánh số mới.
            same = os.path.exists(dst) and file_hash(src) == file_hash(dst)
            if not same and (ic in used_icons or os.path.exists(dst)):
                if next_icon > MAX_ICON_ID:
                    raise SystemExit("Hết chỗ đánh số icon mới (đã tới %d, trần là %d)" % (next_icon, MAX_ICON_ID))
                icon_map[ic] = next_icon    # trùng số, khác ảnh -> đánh số mới
                next_icon += 1
            else:
                icon_map[ic] = ic
            copies.append((ic, icon_map[ic]))

        new_id = next_item
        next_item += 1
        if used_parts:
            head, body, leg = next_part, next_part + 1, next_part + 2
            for slot, p in zip((head, body, leg), used_parts):
                ptype, pdata = nparts[p]
                for old, new in icon_map.items():
                    if old != new:
                        pdata = re.sub(r"\[\s*%d\s*," % old, "[%d," % new, pdata)
                part_sql.append("(%d, %d, '%s')" % (slot, ptype, pdata))
            next_part += 3
            if avatar_icon >= 0:
                avatar_sql.append("(%d, %d)" % (head, icon_map.get(avatar_icon, avatar_icon)))

        icon = icon_map.get(it["icon"], it["icon"])
        item_sql.append("(%d, %d, %d, '%s', '%s', 0, %d, %d, %d, %d, 0, 0, %d, %d, %d)" % (
            new_id, it["type"], it["gender"], it["name"].replace("'", "''"),
            it["desc"].replace("'", "''")[:70], icon, it["part"], it["up"], it["power"], head, body, leg))
        report.append("id NGOL %d '%s' -> id mới %d | icon %d -> %d | part %s" % (
            iid, it["name"], new_id, it["icon"], icon,
            ("%d/%d/%d" % (head, body, leg)) if used_parts else "không (không phải cải trang)"))

    print("\n".join(report))
    print("\nSố ảnh cần chép: %d (đánh số lại: %d)" % (len(copies), sum(1 for a, b in copies if a != b)))

    if not apply:
        print("\n(chạy lại với --apply để chép ảnh và ghi file patch)")
        return 0

    n = 0
    for old, new in copies:
        for zsrc, zdst in ZOOMS:
            src = os.path.join(NGOL_ICON, zsrc, "icon", "%d.png" % old)
            dst = os.path.join(OUR_ICON, zdst, "%d.png" % new)
            if os.path.exists(src) and not os.path.exists(dst):
                shutil.copy2(src, dst)
                n += 1
    print("Đã chép %d file ảnh." % n)

    out = os.path.join(PATCH_DIR, "9x-vat-pham-mang-tu-ngol.sql")
    with open(out, "w", encoding="utf-8") as f:
        f.write("-- Vật phẩm mang từ source NRO NGOL (sinh bởi tools/port_item_from_ngol.py)\n")
        f.write("-- Nhớ tăng DataGame.vsItem (item_template) và vsData (part) rồi build lại jar.\n\n")
        if part_sql:
            f.write("INSERT INTO `part` (`id`, `TYPE`, `DATA`) VALUES\n" + ",\n".join(part_sql) + ";\n\n")
        if avatar_sql:
            f.write("-- Ảnh đại diện (avatar) của cải trang: head_avatar tra theo id phần ĐẦU\n"
                    + "INSERT INTO `head_avatar` (`head_id`, `avatar_id`) VALUES\n"
                    + ",\n".join(avatar_sql) + ";\n\n")
        if item_sql:
            f.write("INSERT INTO `item_template` (`id`, `TYPE`, `gender`, `NAME`, `description`, `level`,"
                    " `icon_id`, `part`, `is_up_to_up`, `power_require`, `gold`, `gem`, `head`, `body`, `leg`) VALUES\n"
                    + ",\n".join(item_sql) + ";\n")
    print("Đã ghi", out)
    return 0


if __name__ == "__main__":
    sys.exit(main(sys.argv[1:]))
