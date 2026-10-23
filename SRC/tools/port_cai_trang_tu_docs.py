#!/usr/bin/env python3
"""Gắn 8 cải trang từ các thư mục ảnh trong docs/ vào game (2 patch: cải trang + giftcode thử).

Dùng:
    /usr/bin/python3 tools/port_cai_trang_tu_docs.py            # chỉ in báo cáo
    /usr/bin/python3 tools/port_cai_trang_tu_docs.py --apply    # chép ảnh, ghi patch, đổi tên file trong docs

Nguồn:
  * docs/goi_cai_trang      1 bộ, có goi.json (dx/dy gốc) và ảnh đủ x1..x4.
  * docs/vương lâm (tao)    1 bộ, chỉ có ảnh x4 (2 đầu, 16 thân, 13 chân, icon, avatar).
  * docs/x4                 6 bộ, chỉ có ảnh x4; bộ đầu có icon + avatar, 5 bộ sau không có.

Việc nó làm:
  1. Bộ không có dx/dy: căn TỪNG khung thân / chân theo bộ Goku SSJ4 Huyết Hỏa (part 2221/2222,
     dx/dy gốc từ part.txt, cùng bộ dáng) bằng cách dò độ lệch cho hai hình trùng khít nhất.
     Đầu căn theo đầu Hắc Ảnh Sát Thần (part 2223, đã chỉnh trong game) rồi nâng thêm HEAD_UP
     để thân không che mặt (client vẽ đầu trước, thân đè lên). Khung đầu thứ 2 căn theo khung 1.
  2. Icon trùng số với ảnh mình đang có nên cấp số mới trong dải trống (trần 32767), mỗi bộ
     một dải liền. Mảnh trong suốt cuối mỗi part là 2955 như các bộ trước.
  3. x1..x3 thu từ x4 (1/4, 2/4, 3/4). Icon vật phẩm 96px, avatar 256px ở x4; bộ nào thiếu thì
     dựng từ khung đầu.
  4. Đổi tên file trong thư mục docs sang số mới (goi.json cũng ghi lại theo số mới), để thư mục
     nguồn khớp đúng với data/icon.
"""

import json
import os
import re
import sys

import numpy as np
from PIL import Image

HERE = os.path.dirname(os.path.dirname(os.path.abspath(__file__)))          # .../SRC
ROOT = os.path.dirname(HERE)
sys.path.insert(0, os.path.join(HERE, "tools"))

DOCS = ROOT + "/docs/"
OUR_ICON = HERE + "/data/icon"
PATCH_DIR = HERE + "/sql/patch"
MAX_ICON_ID = 32767
TRANSPARENT = 2955
ICON_PX, AVATAR_PX = 96, 256
ZOOMS = (1, 2, 3, 4)
GIFT_CODE = "ctdocs1"
DATAGAME = HERE + "/src/nro/models/data/DataGame.java"

REF_BODY = json.loads("[[30628,-3,-22],[30629,-8,-27],[30630,-10,-35],[30631,-14,-36],[30632,-2,-33],"
                      "[30633,-3,-35],[30634,-10,-32],[30635,-11,-33],[30636,-8,-35],[30637,-10,-35],"
                      "[30638,-8,-34],[30639,-12,-31],[30640,-8,-38],[30641,-12,-34],[30642,-7,-30],"
                      "[30643,-16,-24]]")
REF_LEG = json.loads("[[30644,-1,-5],[30645,-19,-19],[30646,-5,-18],[30647,-4,-25],[30648,-10,-21],"
                     "[30649,-13,-19],[30650,0,-17],[30651,-20,-11],[30652,-6,-16],[30653,-18,-17],"
                     "[30654,-12,-18],[30655,-6,-19],[30656,-12,-18]]")
REF_HEAD = [29768, -11, -49]

X4 = DOCS + "x4/"
VL = DOCS + "vương lâm (tao)/"
# (tên, mô tả, thư mục, 2 khung đầu, thân đầu tiên, chân đầu tiên, icon, avatar, nâng đầu)
BO = [
    ("Cải trang Vương Lâm", "Tóc bạch kim, vương miện vàng, ấn đỏ giữa trán",
     VL, (20277, 20278), 20279, 20295, 20308, 20309, 0),
    ("Cải trang Kiếm Sĩ Hồng Lôi", "Tóc bạc nhuộm hồng, vác trường kiếm, sấm hồng quanh tay",
     X4, (17719, 17720), 17722, 17738, 17751, 17752, 0),
    ("Cải trang Huyết Nhẫn Saiyan", "Băng trán ninja, hào quang máu đỏ, hai quả cầu đỏ",
     X4, (17753, 17754), 17755, 17771, None, None, 4),
    ("Cải trang Lam Hồn Chiến Thần", "Tóc bạc tai sói, lửa xanh lam bao quanh",
     X4, (17784, 17785), 17786, 17802, None, None, 4),
    ("Cải trang Tử Ma Chiến Thần", "Tóc bạc ánh tím, khói tím cuộn quanh người",
     X4, (17815, 17816), 17817, 17833, None, None, 6),
    ("Cải trang Ngọc Long Chiến Thần", "Sừng rồng, sấm xanh ngọc bao quanh",
     X4, (17846, 17847), 17848, 17864, None, None, 6),
    ("Cải trang Kim Lôi Chiến Thần", "Sừng vàng, lửa hoàng kim bao quanh",
     X4, (17877, 17878), 17879, 17895, None, None, 6),
]
GOI = DOCS + "goi_cai_trang/"
GOI_TRONG = (3000, 3001, 3002)          # mảnh trong suốt bên nguồn -> 2955
GOI_TEN = ("Cải trang Hắc Y Song Đao", "Áo khoác đen, hai tay cầm song đao tím")


# ----------------------------------------------------------------- căn khung
def mask(im):
    return (np.asarray(im.convert("RGBA"))[:, :, 3] > 80).astype(np.float32)


def best_offset(ref, new):
    """Độ lệch (x, y) góc trái-trên của `new` so với `ref`, px x4, để hai hình trùng khít nhất."""
    a, b = mask(ref), mask(new)
    H, W = a.shape[0] + b.shape[0], a.shape[1] + b.shape[1]
    c = np.fft.irfft2(np.fft.rfft2(a, (H, W)) * np.conj(np.fft.rfft2(b, (H, W))), (H, W))
    iy, ix = np.unravel_index(np.argmax(c), c.shape)
    oy = iy if iy < a.shape[0] else iy - H
    ox = ix if ix < a.shape[1] else ix - W
    return int(ox), int(oy)


def place(ref_piece, new_im):
    ic, dx, dy = ref_piece
    ox, oy = best_offset(Image.open(OUR_ICON + "/x4/%d.png" % ic), new_im)
    return round(dx + ox / 4), round(dy + oy / 4)


def can_bo(folder, heads, b0, l0, up):
    """[đầu, thân, chân] dạng [[ảnh x4, dx, dy], ...] (chưa có mảnh trong suốt cuối)."""
    op = lambda i: Image.open(folder + "%d.png" % i)
    h0 = op(heads[0])
    hx, hy = place(REF_HEAD, h0)
    ox, oy = best_offset(h0, op(heads[1]))
    head = [[heads[0], hx, hy - up], [heads[1], round(hx + ox / 4), round(hy + oy / 4) - up]]
    body = [[b0 + k, *place(r, op(b0 + k))] for k, r in enumerate(REF_BODY)]
    leg = [[l0 + k, *place(r, op(l0 + k))] for k, r in enumerate(REF_LEG)]
    return [head, body, leg]


# ----------------------------------------------------------------- ảnh
def vuong(im, px):
    """Cắt sát hình rồi đặt giữa ô vuông px x px."""
    im = im.convert("RGBA")
    bbox = im.getbbox()
    if bbox:
        im = im.crop(bbox)
    s = min(px / im.width, px / im.height)
    im = im.resize((max(1, round(im.width * s)), max(1, round(im.height * s))), Image.LANCZOS)
    can = Image.new("RGBA", (px, px), (0, 0, 0, 0))
    can.alpha_composite(im, ((px - im.width) // 2, (px - im.height) // 2))
    return can


def ghi_4_muc(im4, new_id):
    """im4 là bản x4; x1..x3 thu theo tỉ lệ."""
    for z in ZOOMS:
        f = z / 4
        im = im4 if z == 4 else im4.resize(
            (max(1, round(im4.width * f)), max(1, round(im4.height * f))), Image.LANCZOS)
        im.save(OUR_ICON + "/x%d/%d.png" % (z, new_id), optimize=True)


# ----------------------------------------------------------------- số tiếp theo
def so_patch_tiep():
    return max(int(f.split("-")[0]) for f in os.listdir(PATCH_DIR) if f[:2].isdigit()) + 1


def giftcode_id_tiep():
    ids = [0]
    for f in os.listdir(PATCH_DIR):
        if f.endswith(".sql"):
            txt = open(os.path.join(PATCH_DIR, f), encoding="utf-8", errors="replace").read()
            ids += [int(x) for x in re.findall(r"^\((\d+), '[A-Za-z0-9]+', -?\d+, '\[", txt, re.M)]
    return max(ids) + 1


def phien_ban(ten):
    return int(re.search(r"public static byte %s = (\d+);" % ten, open(DATAGAME, encoding="utf-8").read()).group(1))


# ----------------------------------------------------------------- chính
def main(argv):
    apply = "--apply" in argv
    from port_item_from_sumo import our_data

    _, our_icons, max_item, max_part, _ = our_data()
    taken = set(our_icons)
    for z in ZOOMS:
        taken |= {int(f[:-4]) for f in os.listdir(OUR_ICON + "/x%d" % z) if f[:-4].isdigit()}

    def cap_dai(n):
        """Dải n số liền nhau còn trống (tìm từ 20000 lên cho gần các bộ cũ)."""
        run = 0
        for i in range(20000, MAX_ICON_ID + 1):
            run = run + 1 if i not in taken else 0
            if run == n:
                taken.update(range(i - n + 1, i + 1))
                return i - n + 1
        raise SystemExit("Hết dải icon trống")

    # mỗi bộ: (tên, mô tả, [đầu, thân, chân] số cũ, nguồn ảnh theo (zoom, số cũ), icon cũ, avatar cũ)
    jobs = []
    g = json.load(open(GOI + "goi.json", encoding="utf-8"))
    gp = {p["type"]: [[a, b, c] for a, b, c in json.loads(p["data"]) if a not in GOI_TRONG] for p in g["parts"]}
    jobs.append((GOI_TEN[0], GOI_TEN[1], [gp[0], gp[1], gp[2]],
                 lambda z, i: GOI + "icon/x%d/%d.png" % (z, i), None, None, GOI))
    for ten, mota, folder, heads, b0, l0, ic, av, up in BO:
        jobs.append((ten, mota, can_bo(folder, heads, b0, l0, up),
                     lambda z, i, f=folder: f + "%d.png" % i, ic, av, folder))

    out_parts, out_items, out_avs, report, doi_ten = [], [], [], [], []
    item_id, part_id = max_item, max_part
    for ten, mota, parts, src, ic, av, folder in jobs:
        olds = [p[0] for pt in parts for p in pt]
        base = cap_dai(len(olds) + 2)
        m = {o: base + k for k, o in enumerate(olds)}
        new_icon, new_av = base + len(olds), base + len(olds) + 1
        item_id += 1
        ids = (part_id + 1, part_id + 2, part_id + 3)
        part_id += 3
        for pid, typ, pt in zip(ids, (0, 1, 2), parts):
            data = "[" + ",".join("[%d,%d,%d]" % (m[a], dx, dy) for a, dx, dy in pt) \
                   + ",[%d,0,0]]" % TRANSPARENT
            out_parts.append("(%d, %d, '%s')" % (pid, typ, data))
        out_avs.append("(%d, %d)" % (ids[0], new_av))
        out_items.append("(%d, 5, 3, '%s', '%s', 0, %d, %d, 0, 0, 0, 0, %d, %d, %d)"
                         % (item_id, ten, mota, new_icon, ids[0], *ids))
        report.append("  %d  %-32s part %d/%d/%d  icon %d..%d (vật phẩm %d, avatar %d)"
                      % (item_id, ten, *ids, base, new_av, new_icon, new_av))
        doi_ten.append((folder, src, m, parts, ic, av, new_icon, new_av))

    np1 = so_patch_tiep()
    np2, gift_id = np1 + 1, giftcode_id_tiep()
    vs_item, vs_data = phien_ban("vsItem") + 1, phien_ban("vsData") + 1
    print("Patch %d, %d | giftcode id %d | vsItem -> %d, vsData -> %d" % (np1, np2, gift_id, vs_item, vs_data))
    print("Vật phẩm %d..%d | part %d..%d" % (max_item + 1, item_id, max_part + 1, part_id))
    print("\n".join(report))
    if not apply:
        print("\n(chạy lại với --apply để chép ảnh, ghi patch và đổi tên file trong docs)")
        return 0

    n = 0
    for folder, src, m, parts, ic, av, new_icon, new_av in doi_ten:
        for old, new in m.items():
            for z in ZOOMS:
                p = src(z, old)
                if os.path.exists(p):
                    Image.open(p).convert("RGBA").save(OUR_ICON + "/x%d/%d.png" % (z, new), optimize=True)
                else:
                    im4 = Image.open(src(4, old)).convert("RGBA")
                    f = z / 4
                    im4.resize((max(1, round(im4.width * f)), max(1, round(im4.height * f))),
                               Image.LANCZOS).save(OUR_ICON + "/x%d/%d.png" % (z, new), optimize=True)
                n += 1
        head4 = Image.open(src(4, parts[0][0][0]))
        ghi_4_muc(vuong(Image.open(src(4, ic)) if ic else head4, ICON_PX), new_icon)
        ghi_4_muc(vuong(Image.open(src(4, av)) if av else head4, AVATAR_PX), new_av)
        n += 8
    print("Đã ghi %d file ảnh vào data/icon." % n)

    with open(PATCH_DIR + "/%d-cai-trang-tu-docs.sql" % np1, "w", encoding="utf-8") as f:
        f.write(HEADER % dict(np=np1, vi=vs_item, vd=vs_data,
                              i0=max_item + 1, i1=item_id, p0=max_part + 1, p1=part_id,
                              im=max_item, pm=max_part, ds="\n".join("--" + r for r in report)))
        f.write("INSERT INTO `part` (`id`, `TYPE`, `DATA`) VALUES\n" + ",\n".join(out_parts) + ";\n\n")
        f.write("INSERT INTO `head_avatar` (`head_id`, `avatar_id`) VALUES\n"
                + ",\n".join(out_avs) + ";\n\n")
        f.write("INSERT INTO `item_template` (`id`, `TYPE`, `gender`, `NAME`, `description`, `level`,"
                " `icon_id`, `part`, `is_up_to_up`, `power_require`, `gold`, `gem`, `head`, `body`, `leg`)"
                " VALUES\n" + ",\n".join(out_items) + ";\n\n")
        f.write("-- KIỂM TRA\nSELECT `id`, `NAME`, `icon_id`, `head`, `body`, `leg` FROM `item_template`"
                " WHERE `id` BETWEEN %d AND %d;\n" % (max_item + 1, item_id))
        f.write("SELECT COUNT(*) AS so_part_phai_%d FROM `part` WHERE `id` BETWEEN %d AND %d;\n"
                % (part_id - max_part, max_part + 1, part_id))

    detail = json.dumps([{"id": i, "quantity": 1, "options": []}
                         for i in range(max_item + 1, item_id + 1)], separators=(",", ":"))
    with open(PATCH_DIR + "/%d-giftcode-cai-trang-tu-docs.sql" % np2, "w", encoding="utf-8") as f:
        f.write(GIFT % dict(np=np2, np1=np1, code=GIFT_CODE, n=item_id - max_item, gid=gift_id, detail=detail,
                            ds="\n".join("--" + r for r in report)))
    print("Đã ghi patch %d, %d." % (np1, np2))

    # Đổi tên file trong docs sang số mới (đổi qua tên tạm để khỏi đè lên nhau)
    for folder, src, m, parts, ic, av, new_icon, new_av in doi_ten:
        ren = dict(m)
        if ic:
            ren[ic] = new_icon
        if av:
            ren[av] = new_av
        dirs = [folder + "icon/x%d/" % z for z in ZOOMS] if folder == GOI else [folder]
        for d in dirs:
            moved = []
            for old, new in ren.items():
                if os.path.exists(d + "%d.png" % old):
                    os.rename(d + "%d.png" % old, d + "_tmp_%d.png" % new)
                    moved.append(new)
            for new in moved:
                os.rename(d + "_tmp_%d.png" % new, d + "%d.png" % new)
        if folder == GOI:
            for p in g["parts"]:
                p["data"] = json.dumps([[ren.get(a, TRANSPARENT if a in GOI_TRONG else a), b, c]
                                        for a, b, c in json.loads(p["data"])], separators=(",", ":"))
                if "avatar" in p:
                    p["avatar"] = new_av
            g["icons"] = sorted(set(ren.values()))
            g["items"][0]["icon_id"] = str(new_icon)
            json.dump(g, open(GOI + "goi.json", "w", encoding="utf-8"), ensure_ascii=False)
    print("Đã đổi tên file trong docs theo số mới.")
    return 0


HEADER = """-- =====================================================================
-- %(np)d — THÊM 8 CẢI TRANG TỪ docs/goi_cai_trang, docs/vương lâm (tao), docs/x4
--      (sinh bởi tools/port_cai_trang_tu_docs.py) — 2026-10-23
-- =====================================================================
-- Vật phẩm %(i0)d..%(i1)d, part %(p0)d..%(p1)d:
%(ds)s
--
-- * Icon nguồn trùng số với ảnh mình đang có nên đã cấp số mới (mỗi bộ một dải liền),
--   ảnh đã chép vào data/icon/x1..x4. File trong thư mục docs cũng đã đổi tên theo số mới.
-- * goi_cai_trang dùng dx/dy trong goi.json; mảnh cuối 3000/3001/3002 đổi thành 2955.
-- * Vương Lâm và 6 bộ x4 không có dx/dy: thân / chân căn từng khung theo bộ Goku SSJ4
--   Huyết Hỏa (cùng bộ dáng), đầu căn theo Hắc Ảnh Sát Thần rồi nâng cho thân khỏi che mặt.
-- * 5 bộ x4 sau không có icon / avatar: dựng từ khung đầu.
-- * Chưa có chỉ số: phát qua shop / giftcode / cpanel "Buff đồ" rồi thêm option.
--
-- Chạy cùng jar có DataGame.vsItem = %(vi)d, vsData = %(vd)d. Khởi động lại server.
-- Chạy lại nhiều lần vẫn an toàn (tự xoá đúng dải của mình trước khi thêm).
-- TRƯỚC KHI CHẠY: hai câu dưới phải ra %(im)d và %(pm)d; khác thì DỪNG, báo lại.
-- =====================================================================
SELECT MAX(`id`) AS item_max FROM `item_template`;
SELECT MAX(`id`) AS part_max FROM `part`;

DELETE FROM `item_template` WHERE `id` BETWEEN %(i0)d AND %(i1)d;
DELETE FROM `part`          WHERE `id` BETWEEN %(p0)d AND %(p1)d;
DELETE FROM `head_avatar`   WHERE `head_id` BETWEEN %(p0)d AND %(p1)d;

"""

GIFT = """-- =====================================================================
-- %(np)d — GIFTCODE THỬ %(n)d CẢI TRANG CỦA PATCH %(np1)d (2026-10-23)
-- =====================================================================
-- Mã: %(code)s  (hành trang phải còn %(n)d ô trống). Mỗi tài khoản nhập được một lần.
-- Nhập ở NPC ADMIN Đẹp Trai -> "Nhập Giftcode".
-- count_left = 99999, KHÔNG để -1 (-1 làm mã chết ngay, xem patch 88).
%(ds)s
--
-- Chạy SAU patch %(np1)d. Chạy lại vẫn an toàn. Khởi động lại server.
-- =====================================================================

DELETE FROM `giftcode` WHERE `id` = %(gid)d OR `code` = '%(code)s';

INSERT INTO `giftcode` (`id`, `code`, `count_left`, `detail`, `datecreate`, `expired`) VALUES
(%(gid)d, '%(code)s', 99999, '%(detail)s', NOW(), '2037-12-31 17:00:00');

SELECT `code`, LEFT(`detail`, 60) FROM `giftcode` WHERE `code` = '%(code)s';
"""


if __name__ == "__main__":
    sys.exit(main(sys.argv[1:]))
