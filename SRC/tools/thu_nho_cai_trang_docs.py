#!/usr/bin/env python3
"""Thu nhỏ 7 cải trang của patch 94 cho bằng cỡ "Cải trang Hắc Y Song Đao" (patch 96).

Dùng:
    /usr/bin/python3 tools/thu_nho_cai_trang_docs.py            # chỉ in báo cáo
    /usr/bin/python3 tools/thu_nho_cai_trang_docs.py --apply    # ghi ảnh, sửa patch 94, ghi patch 96

Vì sao: ảnh Vương Lâm và 6 bộ x4 là ảnh HD, to gần gấp đôi nhân vật thường; Hắc Y Song Đao là ảnh
chuẩn của game (dx/dy gốc) nên vào game vừa cỡ. Mỗi bộ lại lệch tỉ lệ khác nhau giữa đầu / thân / chân
(Vương Lâm thân vừa nhưng đầu to gấp đôi), nên thu RIÊNG từng phần:

  1. Hệ số mỗi phần = trung bình của (tỉ lệ chiều cao) và (căn tỉ lệ diện tích) so với phần tương
     ứng của Hắc Y, lấy trung vị trên các khung.
  2. Căn lại từng khung theo chính khung đó của Hắc Y (dò độ lệch cho hai hình trùng khít nhất).
     Đầu căn theo đầu Hắc Y rồi nâng thêm (cột NANG_DAU) cho thân khỏi che mặt.
  3. Ảnh thu nhỏ mang SỐ ICON MỚI: server gửi phiên bản ảnh nhỏ toàn 0 (DataGame.sendSmallVersion)
     nên client không biết ảnh đổi, giữ số cũ thì người đã vào game vẫn thấy ảnh to trong cache.
  4. Xoá ảnh to cũ khỏi data/icon, sửa luôn part trong patch 94 (chạy lại 94 vẫn ra bản nhỏ),
     thư mục docs ghi đè bằng ảnh x4 đã thu nhỏ, đặt tên theo số mới (bản gốc còn ở ~/Downloads).
"""

import os
import re
import sys

import numpy as np
from PIL import Image

HERE = os.path.dirname(os.path.dirname(os.path.abspath(__file__)))          # .../SRC
ROOT = os.path.dirname(HERE)
sys.path.insert(0, os.path.join(HERE, "tools"))

from port_cai_trang_tu_docs import best_offset, TRANSPARENT, MAX_ICON_ID   # noqa: E402

OUR_ICON = HERE + "/data/icon"
PATCH_DIR = HERE + "/sql/patch"
P94 = PATCH_DIR + "/94-cai-trang-tu-docs.sql"
P96 = PATCH_DIR + "/96-thu-nho-cai-trang-tu-docs.sql"
DATAGAME = HERE + "/src/nro/models/data/DataGame.java"
ZOOMS = (1, 2, 3, 4)
MAU = 2661                     # part đầu của Hắc Y Song Đao; thân / chân là 2662 / 2663
DOCS = ROOT + "/docs/"
# id vật phẩm, part đầu, thư mục docs, nâng đầu (đơn vị x1)
BO = [
    (2291, 2664, DOCS + "vương lâm (tao)/", 0),
    (2292, 2667, DOCS + "x4/", 0),
    (2293, 2670, DOCS + "x4/", 0),
    (2294, 2673, DOCS + "x4/", 0),
    (2295, 2676, DOCS + "x4/", 2),
    (2296, 2679, DOCS + "x4/", 2),
    (2297, 2682, DOCS + "x4/", 1),
]


def doc_part():
    txt = open(P94, encoding="utf-8").read()
    return {int(i): [[int(a), int(b), int(c)] for a, b, c in re.findall(r"\[(-?\d+),(-?\d+),(-?\d+)\]", d)]
            for i, d in re.findall(r"^\((\d+), \d, '(\[.*?\])'\)", txt, re.M)}


def anh(i):
    return Image.open(OUR_ICON + "/x4/%d.png" % i).convert("RGBA")


def co(ims):
    hs, ar = [], []
    for im in ims:
        a = np.asarray(im)[:, :, 3] > 60
        ys = np.nonzero(a)[0]
        hs.append(np.ptp(ys) + 1)
        ar.append(a.sum())
    return np.median(hs), np.median(ar)


def thu(im, f):
    return im.resize((max(1, round(im.width * f)), max(1, round(im.height * f))), Image.LANCZOS)


def main(argv):
    apply = "--apply" in argv
    parts = doc_part()
    mau = [[p for p in parts[MAU + t] if p[0] != TRANSPARENT] for t in range(3)]
    mau_im = [[anh(i) for i, _, _ in m] for m in mau]

    taken = set()
    for z in ZOOMS:
        taken |= {int(f[:-4]) for f in os.listdir(OUR_ICON + "/x%d" % z) if f[:-4].isdigit()}
    from port_item_from_sumo import our_data
    taken |= our_data()[1]

    def cap_dai(n):
        run = 0
        for i in range(20000, MAX_ICON_ID + 1):
            run = run + 1 if i not in taken else 0
            if run == n:
                taken.update(range(i - n + 1, i + 1))
                return i - n + 1
        raise SystemExit("Hết dải icon trống")

    ket_qua = []           # (item, part đầu, [3 part mới], {số cũ: (số mới, ảnh x4)}, hệ số, thư mục)
    for item, p0, folder, nang in BO:
        cu = [[p for p in parts[p0 + t] if p[0] != TRANSPARENT] for t in range(3)]
        base = cap_dai(sum(len(c) for c in cu))
        doi, moi, hs, k = {}, [], [], 0
        for t in range(3):
            ims = [anh(i) for i, _, _ in cu[t]]
            h0, a0 = co(mau_im[t])
            h1, a1 = co(ims)
            f = (h0 / h1 + (a0 / a1) ** .5) / 2
            hs.append(f)
            nho = [thu(im, f) for im in ims]
            pcs = []
            if t == 0:
                ox, oy = best_offset(mau_im[0][0], nho[0])
                hx, hy = mau[0][0][1] + ox / 4, mau[0][0][2] + oy / 4
                ox2, oy2 = best_offset(nho[0], nho[1])
                xy = [(round(hx), round(hy) - nang), (round(hx + ox2 / 4), round(hy + oy2 / 4) - nang)]
            else:
                xy = []
                for (_, dx, dy), mi, im in zip(mau[t], mau_im[t], nho):
                    ox, oy = best_offset(mi, im)
                    xy.append((round(dx + ox / 4), round(dy + oy / 4)))
            for (old, _, _), im, (dx, dy) in zip(cu[t], nho, xy):
                doi[old] = (base + k, im)
                pcs.append([base + k, dx, dy])
                k += 1
            moi.append(pcs + [[TRANSPARENT, 0, 0]])
        ket_qua.append((item, p0, moi, doi, hs, folder))
        print("  %d  part %d-%d  hệ số đầu/thân/chân %.2f / %.2f / %.2f  icon mới %d..%d"
              % (item, p0, p0 + 2, *hs, base, base + k - 1))

    vs_data = int(re.search(r"public static byte vsData = (\d+);", open(DATAGAME, encoding="utf-8").read()).group(1)) + 1
    print("vsData -> %d" % vs_data)
    if not apply:
        print("\n(chạy lại với --apply để ghi)")
        return 0

    # Ảnh mới (x1..x3 thu từ bản x4 mới), xoá ảnh to cũ, docs ghi đè bằng bản x4 mới theo số mới
    for _, _, _, doi, _, folder in ket_qua:
        for old, (new, im4) in doi.items():
            for z in ZOOMS:
                im = im4 if z == 4 else thu(im4, z / 4)
                im.save(OUR_ICON + "/x%d/%d.png" % (z, new), optimize=True)
                os.remove(OUR_ICON + "/x%d/%d.png" % (z, old))
            im4.save(folder + "%d.png" % new, optimize=True)
            os.remove(folder + "%d.png" % old)

    # Sửa part trong patch 94 cho khớp
    txt = open(P94, encoding="utf-8").read()
    for _, p0, moi, _, _, _ in ket_qua:
        for t in range(3):
            data = "[" + ",".join("[%d,%d,%d]" % tuple(p) for p in moi[t]) + "]"
            txt, n = re.subn(r"^\(%d, %d, '\[.*?\]'\)" % (p0 + t, t), "(%d, %d, '%s')" % (p0 + t, t, data), txt, flags=re.M)
            assert n == 1, (p0 + t)
    if "Patch 96 thu nhỏ" not in txt:
        txt = txt.replace("-- * 5 bộ x4 sau không có icon / avatar",
                          "-- * Patch 96 thu nhỏ 7 bộ không có dx/dy cho bằng cỡ Hắc Y Song Đao; part dưới đây\n"
                          "--   đã là bản nhỏ (chạy lại 94 vẫn ra đúng).\n"
                          "-- * 5 bộ x4 sau không có icon / avatar", 1)
    open(P94, "w", encoding="utf-8").write(txt)

    with open(P96, "w", encoding="utf-8") as f:
        f.write(HEADER % dict(vd=vs_data, ds="\n".join(
            "--   %d  part %d-%d  đầu %.2f, thân %.2f, chân %.2f" % (it, p0, p0 + 2, *hs)
            for it, p0, _, _, hs, _ in ket_qua)))
        for _, p0, moi, _, _, _ in ket_qua:
            for t in range(3):
                data = "[" + ",".join("[%d,%d,%d]" % tuple(p) for p in moi[t]) + "]"
                f.write("UPDATE `part` SET `DATA` = '%s' WHERE `id` = %d AND `TYPE` = %d;\n" % (data, p0 + t, t))
        f.write("\nSELECT `id`, LEFT(`DATA`, 60) FROM `part` WHERE `id` BETWEEN %d AND %d;\n"
                % (BO[0][1], BO[-1][1] + 2))
    print("Đã ghi ảnh, sửa patch 94, ghi patch 96.")
    return 0


HEADER = """-- =====================================================================
-- 96 — THU NHỎ 7 CẢI TRANG CỦA PATCH 94 CHO BẰNG CỠ HẮC Y SONG ĐAO (2026-10-23)
--      (sinh bởi tools/thu_nho_cai_trang_docs.py)
-- =====================================================================
-- Ảnh Vương Lâm và 6 bộ x4 là ảnh HD, vào game to gần gấp đôi nhân vật thường. Thu riêng
-- đầu / thân / chân theo phần tương ứng của Hắc Y Song Đao (2290), rồi căn lại từng khung
-- theo chính Hắc Y. Hệ số thu:
%(ds)s
--
-- Ảnh nhỏ mang SỐ ICON MỚI (client không tải lại ảnh cũ cùng số vì phiên bản ảnh nhỏ luôn 0).
-- Icon vật phẩm và avatar không đổi.
--
-- Chạy SAU patch 94 (patch 94 cũng đã sửa theo, chạy lại 94 vẫn ra bản nhỏ).
-- Cùng jar có DataGame.vsData = %(vd)d. Chạy lại vẫn an toàn. Khởi động lại server.
-- =====================================================================

"""


if __name__ == "__main__":
    sys.exit(main(sys.argv[1:]))
